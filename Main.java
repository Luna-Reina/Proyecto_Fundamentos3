import java.util.*;

public class Main {

    private static Set<Integer> extraerIds(List<Vertice> lista) {
        Set<Integer> ids = new HashSet<>();
        for (Vertice v : lista) {
            ids.add(v.getId());
        }
        return ids;
    }

    private static void mostrarResultadoIndividual(Grafo grafo, ResultadoAlgoritmo res) {
        System.out.println("\n--- Resultado " + res.nombre + " ---");
        Set<Integer> visitadosIds = extraerIds(res.ordenVisita);

        if (!res.encontrado) {
            System.out.println(grafo.renderizar(visitadosIds, null, null));
            System.out.println("\n[SIN SOLUCIÓN] No existe camino entre la entrada y la salida.");
            System.out.println("* Vértices visitados : " + res.ordenVisita.size());
            System.out.printf("* Tiempo de ejecución   : %.4f ms%n", res.tiempoMs);
            return;
        }

        Set<Integer> caminoIds = extraerIds(res.camino);
        System.out.println(grafo.renderizar(visitadosIds, null, caminoIds));
        System.out.println("\n* Vértices visitados     : " + res.ordenVisita.size());
        System.out.println("* Longitud (movimientos) : " + (res.camino.size() - 1));
        System.out.printf("* Tiempo ejecución       : %.4f ms%n", res.tiempoMs);
    }

    private static void compararSoluciones(Grafo grafo, Map<String, ResultadoAlgoritmo> resultados) {
        System.out.println("----------------------------------------------------------");
        System.out.println("           COMPARACIÓN DE LAS TRES SOLUCIONES");
        System.out.println("----------------------------------------------------------");

        if (!resultados.containsKey("DFS")) {
            grafo.DFS();
            resultados.put("DFS", grafo.getUltimoResultado());
        }
        if (!resultados.containsKey("BFS")) {
            grafo.BFS();
            resultados.put("BFS", grafo.getUltimoResultado());
        }
        if (!resultados.containsKey("Dijkstra")) {
            grafo.caminoMasCorto();
            resultados.put("Dijkstra", grafo.getUltimoResultado());
        }

        String[] nombres = {"DFS", "BFS", "Dijkstra"};
        for (String nombre : nombres) {
            ResultadoAlgoritmo res = resultados.get(nombre);
            System.out.println("\n>>> Mapa solución " + nombre + " (* = camino final, · = visitado):");
            if (res.encontrado) {
                System.out.println(grafo.renderizar(extraerIds(res.ordenVisita), null, extraerIds(res.camino)));
            } else {
                System.out.println(grafo.renderizar(extraerIds(res.ordenVisita), null, null));
                System.out.println("No existe camino hacia la salida.");
            }
        }

        System.out.println("\n----------------------------------------------------------------");
        System.out.printf("%-12s | %-10s | %-12s | %-18s%n",
                "Algoritmo", "¿Solución?", "Visitados", "Movimientos ruta");
        System.out.println("----------------------------------------------------------------");
        for (String nombre : nombres) {
            ResultadoAlgoritmo r = resultados.get(nombre);
            String sol = r.encontrado ? "Sí" : "No";
            String movs = r.encontrado ? String.valueOf(r.camino.size() - 1) : "Sin camino";
            System.out.printf("%-12s | %-10s | %-12d | %-18s%n",
                    nombre, sol, r.ordenVisita.size(), movs);
        }
        System.out.println("----------------------------------------------------------------");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Grafo grafo = new Grafo();
        boolean laberintoCargado = false;
        Map<String, ResultadoAlgoritmo> resultados = new HashMap<>();

        while (true) {
            System.out.println("\n------------------------------------------");
            System.out.println("      RESOLUTOR DE LABERINTOS (GRAFOS)");
            System.out.println("--------------------------------------------");
            System.out.println("1. Cargar laberinto");
            System.out.println("2. Resolver laberinto con DFS");
            System.out.println("3. Resolver laberinto con BFS");
            System.out.println("4. Resolver laberinto con Dijkstra (camino más corto)");
            System.out.println("5. Mostrar las tres soluciones");
            System.out.println("6. Salir");
            System.out.println("---------------------------------------------");
            System.out.print("Selecciona una opción (1-6): ");

            String opcion = scanner.nextLine().trim();

            if (opcion.equals("1")) {
                System.out.print("Ingresa la ruta o nombre del archivo (ej. maze.txt): ");
                String ruta = scanner.nextLine().trim();
                try {
                    Grafo nuevoGrafo = new Grafo();
                    nuevoGrafo.cargarDesdeArchivo(ruta);
                    grafo = nuevoGrafo;
                    laberintoCargado = true;
                    resultados.clear();
                    System.out.println("\n[OK] Laberinto cargado correctamente y resultados previos descartados:");
                    System.out.println(grafo.renderizar(null, null, null));
                } catch (Exception e) {
                    System.out.println("\n[ERROR] No se pudo cargar el laberinto: " + e.getMessage());
                    if (laberintoCargado) {
                        System.out.println("[INFO] Se conserva el laberinto cargado previamente.");
                    }
                }

            } else if (opcion.equals("2") || opcion.equals("3") || opcion.equals("4") || opcion.equals("5")) {
                if (!laberintoCargado) {
                    System.out.println("\n[AVISO] Primero debes cargar un laberinto válido (Opción 1).");
                    continue;
                }

                if (opcion.equals("2")) {
                    grafo.ejecutarDFS(true, 120);
                    ResultadoAlgoritmo res = grafo.getUltimoResultado();
                    resultados.put("DFS", res);
                    mostrarResultadoIndividual(grafo, res);

                } else if (opcion.equals("3")) {
                    grafo.ejecutarBFS(true, 120);
                    ResultadoAlgoritmo res = grafo.getUltimoResultado();
                    resultados.put("BFS", res);
                    mostrarResultadoIndividual(grafo, res);

                } else if (opcion.equals("4")) {
                    grafo.ejecutarDijkstra(true, 120);
                    ResultadoAlgoritmo res = grafo.getUltimoResultado();
                    resultados.put("Dijkstra", res);
                    mostrarResultadoIndividual(grafo, res);

                } else {
                    compararSoluciones(grafo, resultados);
                }

            } else if (opcion.equals("6")) {
                System.out.println("\nSaliendo del programa... ¡Éxito en su entrega!");
                break;

            } else {
                System.out.println("\n[ERROR] Opción no válida. Por favor ingresa un número del 1 al 6.");
            }
        }
        scanner.close();
    }
}