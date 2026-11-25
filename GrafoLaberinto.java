import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GrafoLaberinto {
    private char[][] matriz;
    private int filas;
    private int columnas;
    private Nodo entrada;
    private Nodo salida;
    private final Map<Nodo, List<Arista>> adyacencia;

    public GrafoLaberinto() {
        this.adyacencia = new HashMap<>();
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
            throw new Exception("El primer renglón está vacío (longitud 0).");
        }

        for (int i = 0; i < lineas.size(); i++) {
            if (lineas.get(i).length() != ancho) {
                throw new Exception("El renglón " + (i + 1) + " tiene longitud " + lineas.get(i).length() +
                        ", pero todos deben medir " + ancho + ".");
            }
        }

        int nFilas = lineas.size();
        char[][] matrizTemp = new char[nFilas][ancho];
        Nodo entradaTemp = null;
        Nodo salidaTemp = null;
        String validos = "+-| #ES";

        for (int r = 0; r < nFilas; r++) {
            for (int c = 0; c < ancho; c++) {
                char ch = lineas.get(r).charAt(c);
                if (ch == '\t') {
                    throw new Exception("El archivo contiene tabulaciones (renglón " + (r + 1) + "); usa espacios.");
                }
                if (validos.indexOf(ch) == -1) {
                    throw new Exception("Carácter inválido '" + ch + "' en renglón " + (r + 1) + ", columna " + (c + 1) + ".");
                }
                matrizTemp[r][c] = ch;
                if (ch == 'E') {
                    if (entradaTemp != null) throw new Exception("Hay más de una entrada 'E'.");
                    entradaTemp = new Nodo(r, c);
                } else if (ch == 'S') {
                    if (salidaTemp != null) throw new Exception("Hay más de una salida 'S'.");
                    salidaTemp = new Nodo(r, c);
                }
            }
        }

        if (entradaTemp != null || salidaTemp != null) {
            if (entradaTemp == null || salidaTemp == null) {
                throw new Exception("Si usas marcas explícitas, debe haber exactamente una 'E' y una 'S'.");
            }
        } else {
            List<Nodo> aberturasBorde = new ArrayList<>();
            for (int r = 0; r < nFilas; r++) {
                for (int c = 0; c < ancho; c++) {
                    boolean esBorde = (r == 0 || r == nFilas - 1 || c == 0 || c == ancho - 1);
                    if (esBorde && !esPared(matrizTemp[r][c])) {
                        aberturasBorde.add(new Nodo(r, c));
                    }
                }
            }
            if (aberturasBorde.size() != 2) {
                throw new Exception("Sin marcas 'E' y 'S', el perímetro debe tener exactamente 2 posiciones transitables (se encontraron " + aberturasBorde.size() + ").");
            }
            entradaTemp = aberturasBorde.get(0);
            salidaTemp = aberturasBorde.get(aberturasBorde.size() - 1);
        }

        this.matriz = matrizTemp;
        this.filas = nFilas;
        this.columnas = ancho;
        this.entrada = entradaTemp;
        this.salida = salidaTemp;
        construirGrafo();
    }

    private boolean esPared(char ch) {
        return ch == '+' || ch == '-' || ch == '|';
    }

    private void construirGrafo() {
        adyacencia.clear();
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
                            vecinos.add(new Arista(new Nodo(nr, nc), 1));
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
                } else if (pos.equals(entrada)) {
                    sb.append('E');
                } else if (pos.equals(salida)) {
                    sb.append('S');
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
        if (!padres.containsKey(salida) && !entrada.equals(salida)) return camino;
        Nodo curr = salida;
        while (curr != null) {
            camino.add(curr);
            curr = padres.get(curr);
        }
        Collections.reverse(camino);
        return camino;
    }

    public ResultadoAlgoritmo resolver(String algoritmo, boolean pasoAPaso, int pausaMs) {
        long tInicio = System.nanoTime();
        Set<Nodo> visitadosSet = new HashSet<>();
        List<Nodo> ordenVisita = new ArrayList<>();
        Map<Nodo, Nodo> padres = new HashMap<>();
        padres.put(entrada, null);
        boolean encontrado = false;

        switch (algoritmo) {
            case "DFS": {
                Deque<Nodo> pila = new ArrayDeque<>();
                pila.push(entrada);
                while (!pila.isEmpty()) {
                    Nodo actual = pila.pop();
                    if (visitadosSet.contains(actual)) continue;

                    visitadosSet.add(actual);
                    ordenVisita.add(actual);

                    if (pasoAPaso) imprimirFrame(algoritmo, visitadosSet, actual, ordenVisita.size(), pausaMs);

                    if (actual.equals(salida)) {
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
                cola.offer(entrada);
                visitadosSet.add(entrada);

                while (!cola.isEmpty()) {
                    Nodo actual = cola.poll();
                    ordenVisita.add(actual);

                    if (pasoAPaso) imprimirFrame(algoritmo, visitadosSet, actual, ordenVisita.size(), pausaMs);

                    if (actual.equals(salida)) {
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
                dist.put(entrada, 0);

                PriorityQueue<Object[]> pq =
                        new PriorityQueue<>(Comparator.comparingInt(a -> (int) a[1]));
                pq.offer(new Object[]{entrada, 0});

                while (!pq.isEmpty()) {
                    Object[] entry = pq.poll();
                    Nodo actual = (Nodo) entry[0];
                    int costoAct = (int) entry[1];

                    if (visitadosSet.contains(actual)) continue;
                    visitadosSet.add(actual);
                    ordenVisita.add(actual);

                    if (pasoAPaso) imprimirFrame(algoritmo, visitadosSet, actual, ordenVisita.size(), pausaMs);

                    if (actual.equals(salida)) {
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
                                pq.offer(new Object[]{vec, nuevoCosto});
                            }
                        }
                    }
                }
                break;
            }
        }

        double tiempoMs = (System.nanoTime() - tInicio) / 1_000_000.0;
        List<Nodo> camino = encontrado ? reconstruirCamino(padres) : Collections.emptyList();
        int costoTotal = camino.isEmpty() ? 0 : camino.size() - 1;

        return new ResultadoAlgoritmo(algoritmo, encontrado, ordenVisita, camino, costoTotal, tiempoMs);
    }

    private void imprimirFrame(String algoritmo, Set<Nodo> visitados, Nodo actual, int paso, int pausaMs) {
        System.out.println("\n=== Explorando con " + algoritmo + " | Paso #" + paso + " ===");
        System.out.println("Leyenda: E=Entrada | S=Salida | @=Actual | ·=Visitado");
        System.out.println(renderizar(visitados, actual, null));
        try {
            Thread.sleep(pausaMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}