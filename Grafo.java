import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * Representación de un laberinto como grafo no dirigido ponderado (costo = 1).
 * Alternativa elegida para adyacencia: LISTAS LIGADAS DE ADYACENCIA
 * (Hashtable<Integer, LinkedList<Arcos>>), ya que un laberinto es un grafo disperso
 * (máximo 4 vecinos por celda) y evita desperdiciar memoria O(V^2) de una matriz.
 */
public class Grafo {
    private char[][] matriz;
    private int filas;
    private int columnas;
    private Vertice inicio;
    private Vertice fin;

    private final Vertices vertices;
    private final Hashtable<Integer, LinkedList<Arcos>> listasAdyacencia;

    public Grafo() {
        this.vertices = new Vertices();
        this.listasAdyacencia = new Hashtable<>();
    }

    public void cargarDesdeArchivo(String ruta) throws Exception {
        File archivo = new File(ruta);
        if (!archivo.exists() || !archivo.isFile()) {
            throw new IOException("El archivo '" + ruta + "' no existe.");
        }

        List<String> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.isEmpty()) {
                    lineas.add(linea);
                }
            }
        }

        if (lineas.isEmpty()) {
            throw new Exception("El archivo está vacío.");
        }

        int ancho = lineas.get(0).length();
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
        String validos = "#+-|. SE123456789";

        for (int r = 0; r < nFilas; r++) {
            for (int c = 0; c < ancho; c++) {
                char ch = lineas.get(r).charAt(c);
                if (validos.indexOf(ch) == -1) {
                    throw new Exception("Carácter inválido '" + ch + "' en (" + r + "," + c + ").");
                }
                matrizTemp[r][c] = ch;
                if (ch == 'S') {
                    if (coordInicio != null) throw new Exception("Hay más de un inicio 'S'.");
                    coordInicio = new int[]{r, c};
                } else if (ch == 'E') {
                    if (coordFin != null) throw new Exception("Hay más de una salida 'E'.");
                    coordFin = new int[]{r, c};
                }
            }
        }

        // Mecanismo automático en bordes si no vienen 'S' y 'E' explícitas
        if (coordInicio == null && coordFin == null) {
            List<int[]> aberturas = new ArrayList<>();
            for (int r = 0; r < nFilas; r++) {
                for (int c = 0; c < ancho; c++) {
                    boolean esBorde = (r == 0 || r == nFilas - 1 || c == 0 || c == ancho - 1);
                    if (esBorde && !esPared(matrizTemp[r][c])) {
                        aberturas.add(new int[]{r, c});
                    }
                }
            }
            if (aberturas.size() < 2) {
                throw new Exception("No se encontraron 'S'/'E' ni al menos dos aberturas en los bordes.");
            }
            coordInicio = aberturas.get(0);
            coordFin = aberturas.get(aberturas.size() - 1);
        } else if (coordInicio == null || coordFin == null) {
            throw new Exception("Debe existir tanto 'S' como 'E'.");
        }

        this.matriz = matrizTemp;
        this.filas = nFilas;
        this.columnas = ancho;
        construirGrafo(coordInicio, coordFin);
    }

    private boolean esPared(char ch) {
        return ch == '#' || ch == '+' || ch == '-' || ch == '|';
    }

    private void construirGrafo(int[] coordInicio, int[] coordFin) {
        vertices.getTabla().clear();
        listasAdyacencia.clear();

        int contadorId = 1;
        Vertice[][] mapaTemp = new Vertice[filas][columnas];

        // 1. Crear cada posición transitable como un Vertice con identificador único
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

        // 2. Conectar vecinos horizontales o verticales (sin diagonales) como grafo no dirigido con costo 1
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Arriba, Abajo, Izquierda, Derecha
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

    // =========================================================================
    // MÉTODOS REQUERIDOS POR EL ISSUE #3: DFS(), BFS() y caminoMasCorto()
    // =========================================================================

    public ResultadoAlgoritmo DFS(boolean pasoAPaso, int pausaMs) {
        long tInicio = System.nanoTime();
        Set<Integer> visitadosIds = new HashSet<>();
        List<Vertice> ordenVisita = new ArrayList<>();
        Map<Integer, Integer> padres = new HashMap<>();
        padres.put(inicio.getId(), null);
        boolean encontrado = false;

        Deque<Vertice> pila = new ArrayDeque<>();
        pila.push(inicio);

        while (!pila.isEmpty()) {
            Vertice actual = pila.pop();
            if (visitadosIds.contains(actual.getId())) continue;

            visitadosIds.add(actual.getId());
            ordenVisita.add(actual);

            if (pasoAPaso) imprimirFrame("DFS", visitadosIds, actual, ordenVisita.size(), pausaMs);

            if (actual.equals(fin)) {
                encontrado = true;
                break;
            }

            LinkedList<Arcos> vecinos = listasAdyacencia.get(actual.getId());
            for (int i = vecinos.size() - 1; i >= 0; i--) {
                Vertice vec = vecinos.get(i).getDestino();
                if (!visitadosIds.contains(vec.getId())) {
                    padres.putIfAbsent(vec.getId(), actual.getId());
                    pila.push(vec);
                }
            }
        }

        double tiempoMs = (System.nanoTime() - tInicio) / 1_000_000.0;
        List<Vertice> camino = encontrado ? reconstruirCamino(padres) : Collections.emptyList();
        int costoTotal = camino.isEmpty() ? 0 : camino.size() - 1;
        return new ResultadoAlgoritmo("DFS", encontrado, ordenVisita, camino, costoTotal, tiempoMs);
    }

    public ResultadoAlgoritmo BFS(boolean pasoAPaso, int pausaMs) {
        long tInicio = System.nanoTime();
        Set<Integer> visitadosIds = new HashSet<>();
        List<Vertice> ordenVisita = new ArrayList<>();
        Map<Integer, Integer> padres = new HashMap<>();
        padres.put(inicio.getId(), null);
        boolean encontrado = false;

        Queue<Vertice> cola = new LinkedList<>();
        cola.offer(inicio);
        visitadosIds.add(inicio.getId());

        while (!cola.isEmpty()) {
            Vertice actual = cola.poll();
            ordenVisita.add(actual);

            if (pasoAPaso) imprimirFrame("BFS", visitadosIds, actual, ordenVisita.size(), pausaMs);

            if (actual.equals(fin)) {
                encontrado = true;
                break;
            }

            for (Arcos arco : listasAdyacencia.get(actual.getId())) {
                Vertice vec = arco.getDestino();
                if (!visitadosIds.contains(vec.getId())) {
                    visitadosIds.add(vec.getId());
                    padres.put(vec.getId(), actual.getId());
                    cola.offer(vec);
                }
            }
        }

        double tiempoMs = (System.nanoTime() - tInicio) / 1_000_000.0;
        List<Vertice> camino = encontrado ? reconstruirCamino(padres) : Collections.emptyList();
        int costoTotal = camino.isEmpty() ? 0 : camino.size() - 1;
        return new ResultadoAlgoritmo("BFS", encontrado, ordenVisita, camino, costoTotal, tiempoMs);
    }

    // Dijkstra para el camino más corto
    public ResultadoAlgoritmo caminoMasCorto(boolean pasoAPaso, int pausaMs) {
        long tInicio = System.nanoTime();
        Set<Integer> visitadosIds = new HashSet<>();
        List<Vertice> ordenVisita = new ArrayList<>();
        Map<Integer, Integer> padres = new HashMap<>();
        Map<Integer, Integer> dist = new HashMap<>();

        for (Vertice v : vertices.todos()) {
            dist.put(v.getId(), Integer.MAX_VALUE);
        }
        dist.put(inicio.getId(), 0);
        padres.put(inicio.getId(), null);

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
pq.offer(new int[]{inicio.getId(), 0});
        boolean encontrado = false;

        while (!pq.isEmpty()) {
            int[] entry = pq.poll();
            int actualId = entry[0];
            int costoAct = entry[1] ;

            if (visitadosIds.contains(actualId)) continue;
            visitadosIds.add(actualId);
            Vertice actual = vertices.obtenerPorId(actualId);
            ordenVisita.add(actual);

            if (pasoAPaso) imprimirFrame("Dijkstra", visitadosIds, actual, ordenVisita.size(), pausaMs);

            if (actual.equals(fin)) {
                encontrado = true;
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

        double tiempoMs = (System.nanoTime() - tInicio) / 1_000_000.0;
        List<Vertice> camino = encontrado ? reconstruirCamino(padres) : Collections.emptyList();
        int costoTotal = camino.isEmpty() ? 0 : dist.get(fin.getId());
        return new ResultadoAlgoritmo("Dijkstra", encontrado, ordenVisita, camino, costoTotal, tiempoMs);
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
                    sb.append('S');
                } else if (fin.getFila() == r && fin.getColumna() == c) {
                    sb.append('E');
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

    private void imprimirFrame(String algoritmo, Set<Integer> visitadosIds, Vertice actual, int paso, int pausaMs) {
        System.out.println("\n=== Explorando con " + algoritmo + " | Paso #" + paso + " ===");
        System.out.println("Leyenda: S=Inicio | E=Salida | @=Actual | ·=Visitado");
        System.out.println(renderizar(visitadosIds, actual, null));
        try {
            Thread.sleep(pausaMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Vertices getVertices() {
        return vertices;
    }

    public Hashtable<Integer, LinkedList<Arcos>> getListasAdyacencia() {
        return listasAdyacencia;
    }
}