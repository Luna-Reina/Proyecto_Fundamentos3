import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GrafoLaberinto {
    private char[][] matriz;
    private int filas;
    private int columnas;
    private Nodo inicio;
    private Nodo fin;
    private final Map<Nodo, List<Arista>> adyacencia;

    public GrafoLaberinto() {
        this.adyacencia = new HashMap<>();
    }

    public void cargarDesdeArchivo(String ruta) throws Exception {
        File archivo = new File(ruta);
        if (!archivo.exists() || !archivo.isFile()) {
            throw new IOException("El archivo '" + ruta + "' no existe o no es una ruta válida.");
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
                        " caracteres, pero se esperaba " + ancho + ".");
            }
        }

        int nFilas = lineas.size();
        char[][] matrizTemp = new char[nFilas][ancho];
        Nodo inicioTemp = null;
        Nodo finTemp = null;
        String validos = "#+-|. SE123456789";

        for (int r = 0; r < nFilas; r++) {
            for (int c = 0; c < ancho; c++) {
                char ch = lineas.get(r).charAt(c);
                if (validos.indexOf(ch) == -1) {
                    throw new Exception("Carácter inválido '" + ch + "' en fila " + r + ", columna " + c + ".");
                }
                matrizTemp[r][c] = ch;
                if (ch == 'S') {
                    if (inicioTemp != null) throw new Exception("El laberinto tiene más de un inicio 'S'.");
                    inicioTemp = new Nodo(r, c);
                } else if (ch == 'E') {
                    if (finTemp != null) throw new Exception("El laberinto tiene más de una salida 'E'.");
                    finTemp = new Nodo(r, c);
                }
            }
        }

        // Mecanismo automático si el archivo viene directo de delorie.com sin 'S' ni 'E'
        if (inicioTemp == null && finTemp == null) {
            List<Nodo> aberturasBorde = new ArrayList<>();
            for (int r = 0; r < nFilas; r++) {
                for (int c = 0; c < ancho; c++) {
                    boolean esBorde = (r == 0 || r == nFilas - 1 || c == 0 || c == ancho - 1);
                    if (esBorde && !esPared(matrizTemp[r][c])) {
                        aberturasBorde.add(new Nodo(r, c));
                    }
                }
            }
            if (aberturasBorde.size() < 2) {
                throw new Exception("No se encontraron marcadores 'S'/'E' ni dos aberturas en los bordes.");
            }
            inicioTemp = aberturasBorde.get(0);
            finTemp = aberturasBorde.get(aberturasBorde.size() - 1);
        } else if (inicioTemp == null || finTemp == null) {
            throw new Exception("Falta uno de los marcadores: debe haber tanto 'S' como 'E'.");
        }

        this.matriz = matrizTemp;
        this.filas = nFilas;
        this.columnas = ancho;
        this.inicio = inicioTemp;
        this.fin = finTemp;
        construirGrafo();
    }

    private boolean esPared(char ch) {
        return ch == '#' || ch == '+' || ch == '-' || ch == '|';
    }

    private int costoCelda(char ch) {
        return Character.isDigit(ch) ? Character.getNumericValue(ch) : 1;
    }

    private void construirGrafo() {
        adyacencia.clear();
        // Orden definido: Arriba, Abajo, Izquierda, Derecha
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int r = 0; r < filas; r++) {
            for (int c = 0; c < columnas; c++) {
                if (!esPared(matriz[r][c])) {
                    Nodo actual = new Nodo(r, c);
                    List<Arista> vecinos = new ArrayList<>();
                    for (int[] d : dirs) {
                        int nr = r + d[0];
                        int nc = c + d[1];
                        if (nr >= 0 && nr < filas && nc >= 0 && nc < columnas && !esPared(matriz[nr][nc])) {
                            vecinos.add(new Arista(new Nodo(nr, nc), costoCelda(matriz[nr][nc])));
                        }
                    }
                    adyacencia.put(actual, vecinos);
                }
            }
        }
    }

    public String renderizar(Set<Nodo> visitados, Nodo actual, Set<Nodo> camino) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < filas; r++) {
            for (int c = 0; c < columnas; c++) {
                Nodo pos = new Nodo(r, c);
                char orig = matriz[r][c];
                if (actual != null && pos.equals(actual)) {
                    sb.append('@');
                } else if (pos.equals(inicio)) {
                    sb.append('S');
                } else if (pos.equals(fin)) {
                    sb.append('E');
                } else if (camino != null && camino.contains(pos)) {
                    sb.append('*');
                } else if (visitados != null && visitados.contains(pos)) {
                    sb.append('·');
                } else {
                    sb.append(orig);
                }
            }
            if (r < filas - 1) sb.append('\n');
        }
        return sb.toString();
    }

    private List<Nodo> reconstruirCamino(Map<Nodo, Nodo> padres) {
        List<Nodo> camino = new ArrayList<>();
        if (!padres.containsKey(fin) && !inicio.equals(fin)) return camino;
        Nodo curr = fin;
        while (curr != null) {
            camino.add(curr);
            curr = padres.get(curr);
        }
        Collections.reverse(camino);
        return camino;
    }

    private int calcularCostoCamino(List<Nodo> camino) {
        int total = 0;
        for (int i = 1; i < camino.size(); i++) {
            Nodo n = camino.get(i);
            total += costoCelda(matriz[n.fila][n.col]);
        }
        return total;
    }

    public ResultadoAlgoritmo resolver(String algoritmo, boolean pasoAPaso, int pausaMs) {
        long tInicio = System.nanoTime();
        Set<Nodo> visitadosSet = new HashSet<>();
        List<Nodo> ordenVisita = new ArrayList<>();
        Map<Nodo, Nodo> padres = new HashMap<>();
        padres.put(inicio, null);
        boolean encontrado = false;

        switch (algoritmo) {
            case "DFS": {
                Deque<Nodo> pila = new ArrayDeque<>();
                pila.push(inicio);
                while (!pila.isEmpty()) {
                    Nodo actual = pila.pop();
                    if (visitadosSet.contains(actual)) continue;

                    visitadosSet.add(actual);
                    ordenVisita.add(actual);

                    if (pasoAPaso) imprimirFrame(algoritmo, visitadosSet, actual, ordenVisita.size(), pausaMs);

                    if (actual.equals(fin)) {
                        encontrado = true;
                        break;
                    }

                    List<Arista> vecinos = adyacencia.getOrDefault(actual, Collections.emptyList());
                    for (int i = vecinos.size() - 1; i >= 0; i--) {
                        Nodo vec = vecinos.get(i).destino;
                        if (!visitadosSet.contains(vec)) {
                            padres.putIfAbsent(vec, actual);
                            pila.push(vec);
                        }
                    }
                }
                break;
            }
            case "BFS": {
                Queue<Nodo> cola = new LinkedList<>();
                cola.offer(inicio);
                visitadosSet.add(inicio);

                while (!cola.isEmpty()) {
                    Nodo actual = cola.poll();
                    ordenVisita.add(actual);

                    if (pasoAPaso) imprimirFrame(algoritmo, visitadosSet, actual, ordenVisita.size(), pausaMs);

                    if (actual.equals(fin)) {
                        encontrado = true;
                        break;
                    }

                    for (Arista arista : adyacencia.getOrDefault(actual, Collections.emptyList())) {
                        Nodo vec = arista.destino;
                        if (!visitadosSet.contains(vec)) {
                            visitadosSet.add(vec);
                            padres.put(vec, actual);
                            cola.offer(vec);
                        }
                    }
                }
                break;
            }
            case "Dijkstra": {
                Map<Nodo, Integer> dist = new HashMap<>();
                for (Nodo n : adyacencia.keySet()) dist.put(n, Integer.MAX_VALUE);
                dist.put(inicio, 0);

                PriorityQueue<Map.Entry<Nodo, Integer>> pq =
                        new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
                pq.offer(new AbstractMap.SimpleEntry<>(inicio, 0));

                while (!pq.isEmpty()) {
                    Map.Entry<Nodo, Integer> entry = pq.poll();
                    Nodo actual = entry.getKey();
                    int costoAct = entry.getValue();

                    if (visitadosSet.contains(actual)) continue;
                    visitadosSet.add(actual);
                    ordenVisita.add(actual);

                    if (pasoAPaso) imprimirFrame(algoritmo, visitadosSet, actual, ordenVisita.size(), pausaMs);

                    if (actual.equals(fin)) {
                        encontrado = true;
                        break;
                    }

                    for (Arista arista : adyacencia.getOrDefault(actual, Collections.emptyList())) {
                        Nodo vec = arista.destino;
                        if (!visitadosSet.contains(vec)) {
                            int nuevoCosto = costoAct + arista.peso;
                            if (nuevoCosto < dist.get(vec)) {
                                dist.put(vec, nuevoCosto);
                                padres.put(vec, actual);
                                pq.offer(new AbstractMap.SimpleEntry<>(vec, nuevoCosto));
                            }
                        }
                    }
                }
                break;
            }
        }

        double tiempoMs = (System.nanoTime() - tInicio) / 1_000_000.0;
        List<Nodo> camino = encontrado ? reconstruirCamino(padres) : Collections.emptyList();
        int costoTotal = calcularCostoCamino(camino);

        return new ResultadoAlgoritmo(algoritmo, encontrado, ordenVisita, camino, costoTotal, tiempoMs);
    }

    private void imprimirFrame(String algoritmo, Set<Nodo> visitados, Nodo actual, int paso, int pausaMs) {
        System.out.println("\n=== Explorando con " + algoritmo + " | Paso #" + paso + " ===");
        System.out.println("Leyenda: S=Inicio | E=Salida | @=Actual | ·=Visitado");
        System.out.println(renderizar(visitados, actual, null));
        try {
            Thread.sleep(pausaMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}