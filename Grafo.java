import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Grafo {
    private char[][] matriz;
    private int filas;
    private int columnas;
    private Vertice inicio;
    private Vertice fin;

    private final Vertices vertices;
    private final Hashtable<Integer, LinkedList<Arcos>> listasAdyacencia;

    // Estado de la última ejecución (Issues #4, #5 y #6)
    private List<Vertice> ultimoCamino;
    private boolean solucionEncontrada;
    private ResultadoAlgoritmo ultimoResultado;

    public Grafo() {
        this.vertices = new Vertices();
        this.listasAdyacencia = new Hashtable<>();
        this.ultimoCamino = new ArrayList<>();
        this.solucionEncontrada = false;
    }

    public List<Vertice> getUltimoCamino() {
        return Collections.unmodifiableList(ultimoCamino);
    }

    public boolean isSolucionEncontrada() {
        return solucionEncontrada;
    }

    public ResultadoAlgoritmo getUltimoResultado() {
        return ultimoResultado;
    }

    public Vertices getVertices() {
        return vertices;
    }

    public Hashtable<Integer, LinkedList<Arcos>> getListasAdyacencia() {
        return listasAdyacencia;
    }

    public void cargarDesdeArchivo(String ruta) throws Exception {
        File archivo = new File(ruta);
        if (!archivo.exists() || !archivo.isFile() || !archivo.canRead()) {
            throw new IOException("El archivo '" + ruta + "' no existe o no se puede leer.");
        }

        List<String> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea);
            }
        }

        while (!lineas.isEmpty() && lineas.get(lineas.size() - 1).isEmpty()) {
            lineas.remove(lineas.size() - 1);
        }

        if (lineas.isEmpty()) {
            throw new Exception("El archivo está vacío.");
        }

        int ancho = lineas.get(0).length();
        if (ancho == 0) {
            throw new Exception("El primer renglón tiene longitud 0.");
        }

        for (int i = 0; i < lineas.size(); i++) {
            if (lineas.get(i).length() != ancho) {
                throw new Exception("La fila " + (i + 1) + " mide " + lineas.get(i).length() +
                        " caracteres, se esperaba " + ancho + ".");
            }
        }

        int nFilas = lineas.size();
        char[][] matrizTemp = new char[nFilas][ancho];
        int[] coordInicio = null;
        int[] coordFin = null;
        String validos = "+-| #ES";

        for (int r = 0; r < nFilas; r++) {
            for (int c = 0; c < ancho; c++) {
                char ch = lineas.get(r).charAt(c);
                if (ch == '\t') {
                    throw new Exception("El archivo contiene tabulaciones en fila " + (r + 1) + "; usa espacios.");
                }
                if (validos.indexOf(ch) == -1) {
                    throw new Exception("Carácter inválido '" + ch + "' en (" + r + "," + c + ").");
                }
                matrizTemp[r][c] = ch;
                if (ch == 'E') {
                    if (coordInicio != null) throw new Exception("Hay más de una entrada 'E'.");
                    coordInicio = new int[]{r, c};
                } else if (ch == 'S') {
                    if (coordFin != null) throw new Exception("Hay más de una salida 'S'.");
                    coordFin = new int[]{r, c};
                }
            }
        }

        if (coordInicio != null || coordFin != null) {
            if (coordInicio == null || coordFin == null) {
                throw new Exception("Debe existir tanto 'E' (entrada) como 'S' (salida).");
            }
        } else {
            List<int[]> aberturas = new ArrayList<>();
            for (int r = 0; r < nFilas; r++) {
                for (int c = 0; c < ancho; c++) {
                    boolean esBorde = (r == 0 || r == nFilas - 1 || c == 0 || c == ancho - 1);
                    if (esBorde && !esPared(matrizTemp[r][c])) {
                        aberturas.add(new int[]{r, c});
                    }
                }
            }
            if (aberturas.size() != 2) {
                throw new Exception("Sin marcas 'E'/'S', debe haber exactamente 2 aberturas en el borde (se hallaron " + aberturas.size() + ").");
            }
            coordInicio = aberturas.get(0);
            coordFin = aberturas.get(1);
        }

        this.matriz = matrizTemp;
        this.filas = nFilas;
        this.columnas = ancho;
        this.ultimoCamino = new ArrayList<>();
        this.solucionEncontrada = false;
        this.ultimoResultado = null;
        construirGrafo(coordInicio, coordFin);
    }

    private boolean esPared(char ch) {
        return ch == '+' || ch == '-' || ch == '|';
    }

    private void construirGrafo(int[] coordInicio, int[] coordFin) {
        vertices.getTabla().clear();
        listasAdyacencia.clear();

        int contadorId = 1;
        Vertice[][] mapaTemp = new Vertice[filas][columnas];

        for (int r = 0; r < filas; r++) {
            for (int c = 0; c < columnas; c++) {
                if (!esPared(matriz[r][c])) {
                    Vertice v = new Vertice(contadorId++, r, c);
                    vertices.agregar(v);
                    listasAdyacencia.put(v.getId(), new LinkedList<>());
                    mapaTemp[r][c] = v;

                    if (r == coordInicio[0] && c == coordInicio[1]) this.inicio = v;
                    if (r == coordFin[0] && c == coordFin[1]) this.fin = v;
                }
            }
        }

        // Orden  Issue 4 Arriba, Derecha, Abajo, Izquierda
        int[][] dirs = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        for (int r = 0; r < filas; r++) {
            for (int c = 0; c < columnas; c++) {
                Vertice actual = mapaTemp[r][c];
                if (actual != null) {
                    for (int[] d : dirs) {
                        int nr = r + d[0];
                        int nc = c + d[1];
                        if (nr >= 0 && nr < filas && nc >= 0 && nc < columnas && mapaTemp[nr][nc] != null) {
                            Vertice vecino = mapaTemp[nr][nc];
                            listasAdyacencia.get(actual.getId()).add(new Arcos(actual, vecino, 1));
                        }
                    }
                }
            }
        }
    }



    public List<Vertice> DFS() {
        long tInicio = System.nanoTime();
        this.ultimoCamino = new ArrayList<>();
        this.solucionEncontrada = false;

        Set<Integer> visitadosIds = new HashSet<>();
        List<Vertice> ordenVisita = new ArrayList<>();
        Map<Integer, Integer> padres = new HashMap<>();
        padres.put(inicio.getId(), null);

        Deque<Vertice> pila = new ArrayDeque<>();
        pila.push(inicio);

        while (!pila.isEmpty()) {
            Vertice actual = pila.pop();
            if (visitadosIds.contains(actual.getId())) continue;

            visitadosIds.add(actual.getId());
            ordenVisita.add(actual);

            if (actual.equals(fin)) {
                this.solucionEncontrada = true;
                break;
            }

            //  orden inverso  Arriba, Derecha, Abajo, Izquierda
            LinkedList<Arcos> vecinos = listasAdyacencia.get(actual.getId());
            for (int i = vecinos.size() - 1; i >= 0; i--) {
                Vertice vec = vecinos.get(i).getDestino();
                if (!visitadosIds.contains(vec.getId())) {
                    padres.put(vec.getId(), actual.getId());
                    pila.push(vec);
                }
            }
        }

        this.ultimoCamino = this.solucionEncontrada ? reconstruirCamino(padres) : Collections.emptyList();
        double tiempoMs = (System.nanoTime() - tInicio) / 1_000_000.0;
        int movimientos = this.solucionEncontrada ? Math.max(0, this.ultimoCamino.size() - 1) : -1;
        this.ultimoResultado = new ResultadoAlgoritmo("DFS", this.solucionEncontrada, ordenVisita, this.ultimoCamino, movimientos, tiempoMs);
        return ordenVisita;
    }

    public List<Vertice> BFS() {
        long tInicio = System.nanoTime();
        this.ultimoCamino = new ArrayList<>();
        this.solucionEncontrada = false;

        Set<Integer> encoladosIds = new HashSet<>();
        List<Vertice> ordenVisita = new ArrayList<>();
        Map<Integer, Integer> padres = new HashMap<>();
        padres.put(inicio.getId(), null);

        Queue<Vertice> cola = new LinkedList<>();
        cola.offer(inicio);
        encoladosIds.add(inicio.getId());

        while (!cola.isEmpty()) {
            Vertice actual = cola.poll();
            ordenVisita.add(actual);

            if (actual.equals(fin)) {
                this.solucionEncontrada = true;
                break;
            }

            for (Arcos arco : listasAdyacencia.get(actual.getId())) {
                Vertice vec = arco.getDestino();
                if (!encoladosIds.contains(vec.getId())) {
                    encoladosIds.add(vec.getId());
                    padres.put(vec.getId(), actual.getId());
                    cola.offer(vec);
                }
            }
        }

        this.ultimoCamino = this.solucionEncontrada ? reconstruirCamino(padres) : Collections.emptyList();
        double tiempoMs = (System.nanoTime() - tInicio) / 1_000_000.0;
        int movimientos = this.solucionEncontrada ? Math.max(0, this.ultimoCamino.size() - 1) : -1;
        this.ultimoResultado = new ResultadoAlgoritmo("BFS", this.solucionEncontrada, ordenVisita, this.ultimoCamino, movimientos, tiempoMs);
        return ordenVisita;
    }

    public List<Vertice> caminoMasCorto() {
        long tInicio = System.nanoTime();
        this.ultimoCamino = new ArrayList<>();
        this.solucionEncontrada = false;

        Set<Integer> visitadosIds = new HashSet<>();
        List<Vertice> ordenVisita = new ArrayList<>();
        Map<Integer, Integer> padres = new HashMap<>();
        Map<Integer, Integer> dist = new HashMap<>();

        for (Vertice v : vertices.todos()) {
            dist.put(v.getId(), Integer.MAX_VALUE);
        }
        dist.put(inicio.getId(), 0);
        padres.put(inicio.getId(), null);

        
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> {
            int cmpDist = Integer.compare(a[1], b[1]);
            return (cmpDist != 0) ? cmpDist : Integer.compare(a[0], b[0]);
        });
        pq.offer(new int[]{inicio.getId(), 0});

        while (!pq.isEmpty()) {
            int[] entry = pq.poll();
            int actualId = entry[0];
            int costoAct = entry[1];

            if (visitadosIds.contains(actualId)) continue;
            visitadosIds.add(actualId);
            Vertice actual = vertices.obtenerPorId(actualId);
            ordenVisita.add(actual);

            if (actual.equals(fin)) {
                this.solucionEncontrada = true;
                break;
            }

            for (Arcos arco : listasAdyacencia.get(actualId)) {
                int vecId = arco.getDestino().getId();
                if (!visitadosIds.contains(vecId)) {
                    int nuevoCosto = costoAct + arco.getCosto();
                    if (nuevoCosto < dist.get(vecId)) {
                        dist.put(vecId, nuevoCosto);
                        padres.put(vecId, actualId);
                        pq.offer(new int[]{vecId, nuevoCosto});
                    }
                }
            }
        }

        this.ultimoCamino = this.solucionEncontrada ? reconstruirCamino(padres) : Collections.emptyList();
        double tiempoMs = (System.nanoTime() - tInicio) / 1_000_000.0;
        int movimientos = this.solucionEncontrada ? Math.max(0, this.ultimoCamino.size() - 1) : -1;
        this.ultimoResultado = new ResultadoAlgoritmo("Dijkstra", this.solucionEncontrada, ordenVisita, this.ultimoCamino, movimientos, tiempoMs);
        return ordenVisita;
    }



    public void despliegaLaberinto(List<Vertice> ordenVisita) {
        despliegaLaberinto(ordenVisita, 120);
    }

    public void despliegaLaberinto(List<Vertice> ordenVisita, int pausaMs) {
        String nombreAlg = (ultimoResultado != null) ? ultimoResultado.nombre : "Algoritmo";
        Set<Integer> visitadosIds = new HashSet<>();

        for (int i = 0; i < ordenVisita.size(); i++) {
            Vertice actual = ordenVisita.get(i);
            visitadosIds.add(actual.getId());

            System.out.println("\n=== Explorando con " + nombreAlg + " | Paso #" + (i + 1) + " de " + ordenVisita.size() + " ===");
            System.out.println("Leyenda: E=Entrada | S=Salida | @=Vértice actual | ·=Visitado | *=Camino final");
            System.out.println(renderizar(visitadosIds, actual, null));

            try {
                Thread.sleep(pausaMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\n=== Resultado final (" + nombreAlg + ") ===");
        System.out.println("Leyenda: E=Entrada | S=Salida | ·=Visitado | *=Camino final");

        if (solucionEncontrada) {
            Set<Integer> caminoIds = new HashSet<>();
            for (Vertice v : ultimoCamino) {
                caminoIds.add(v.getId());
            }
            System.out.println(renderizar(visitadosIds, null, caminoIds));
            System.out.println("\n* Vértices visitados     : " + ordenVisita.size());
            System.out.println("* Longitud (movimientos) : " + (ultimoCamino.size() - 1));
        } else {
            System.out.println(renderizar(visitadosIds, null, null));
            System.out.println("\n[SIN SOLUCIÓN] No existe camino entre la entrada y la salida.");
            System.out.println("* Vértices visitados     : " + ordenVisita.size());
        }

        if (ultimoResultado != null) {
            System.out.printf("* Tiempo puro ejecución  : %.4f ms%n", ultimoResultado.tiempoMs);
        }
    }

    private List<Vertice> reconstruirCamino(Map<Integer, Integer> padres) {
        List<Vertice> camino = new ArrayList<>();
        if (!padres.containsKey(fin.getId()) && !inicio.equals(fin)) return camino;
        Integer currId = fin.getId();
        while (currId != null) {
            camino.add(vertices.obtenerPorId(currId));
            currId = padres.get(currId);
        }
        Collections.reverse(camino);
        return camino;
    }

    public String renderizar(Set<Integer> visitadosIds, Vertice actual, Set<Integer> caminoIds) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < filas; r++) {
            for (int c = 0; c < columnas; c++) {
                char orig = matriz[r][c];
                if (actual != null && actual.getFila() == r && actual.getColumna() == c) {
                    sb.append('@');
                } else if (inicio.getFila() == r && inicio.getColumna() == c) {
                    sb.append('E');
                } else if (fin.getFila() == r && fin.getColumna() == c) {
                    sb.append('S');
                } else {
                    Vertice v = vertices.buscarPorCoordenada(r, c);
                    if (v != null && caminoIds != null && caminoIds.contains(v.getId())) {
                        sb.append('*');
                    } else if (v != null && visitadosIds != null && visitadosIds.contains(v.getId())) {
                        sb.append('·');
                    } else {
                        sb.append(orig);
                    }
                }
            }
            if (r < filas - 1) sb.append('\n');
        }
        return sb.toString();
    }

  
    // extra caracteres Unicode alternativos (┌ ┐ └ ┘ ─ │ █ ·)

    public String renderizarEstilizado(Set<Integer> visitadosIds, Vertice actual, Set<Integer> caminoIds) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < filas; r++) {
            for (int c = 0; c < columnas; c++) {
                char orig = matriz[r][c];
                if (actual != null && actual.getFila() == r && actual.getColumna() == c) {
                    sb.append('@');
                } else if (inicio.getFila() == r && inicio.getColumna() == c) {
                    sb.append('E');
                } else if (fin.getFila() == r && fin.getColumna() == c) {
                    sb.append('S');
                } else {
                    Vertice v = vertices.buscarPorCoordenada(r, c);
                    if (v != null && caminoIds != null && caminoIds.contains(v.getId())) {
                        sb.append('*');
                    } else if (v != null && visitadosIds != null && visitadosIds.contains(v.getId())) {
                        sb.append('·');
                    } else if (esPared(orig)) {
                        sb.append(obtenerCaracterParedUnicode(r, c, orig));
                    } else {
                        sb.append(' ');
                    }
                }
            }
            if (r < filas - 1) sb.append('\n');
        }
        return sb.toString();
    }

    private char obtenerCaracterParedUnicode(int r, int c, char orig) {
        boolean arriba = (r > 0) && esPared(matriz[r - 1][c]);
        boolean abajo = (r < filas - 1) && esPared(matriz[r + 1][c]);
        boolean izq = (c > 0) && esPared(matriz[r][c - 1]);
        boolean der = (c < columnas - 1) && esPared(matriz[r][c + 1]);

        if (orig == '+') {
            if (abajo && der && !arriba && !izq) return '┌';
            if (abajo && izq && !arriba && !der) return '┐';
            if (arriba && der && !abajo && !izq) return '└';
            if (arriba && izq && !abajo && !der) return '┘';
            return '█';
        }
        if (orig == '-') return '─';
        if (orig == '|') return '│';
        return '█';
    }
}