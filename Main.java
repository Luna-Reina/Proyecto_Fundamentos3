import java.util.*;

public class Main {

    private static Set<Integer> extraerIds(List<Vertice> lista) {
        Set<Integer> ids = new HashSet<>();
        for (Vertice v : lista) ids.add(v.getId());
        return ids;
    }

    private static void mostrarResultadoIndividual(Grafo grafo, ResultadoAlgoritmo res) {
        System.out.println("\n--- Resultado " + res.nombre + " ---");
        if (!res.encontrado) {
            System.out.println("No se encontró un camino válido hacia la salida 'E'.");
            System.out.println("Nodos explorados: " + res.ordenVisita.size());
            return;
        }
        System.out.println(grafo.renderizar(extraerIds(res.ordenVisita), null, extraerIds(res.camino)));
        System.out.println("\n* Nodos explorados : " + res.ordenVisita.size());
        System.out.println("* Pasos del camino : " + (res.camino.size() - 1));
        System.out.println("* Costo total      : " + res.costoTotal);
        System.out.printf("* Tiempo ejecución : %.4f ms%n", res.tiempoMs);
    }

    private static void compararSoluciones(Grafo grafo, Map<String, ResultadoAlgoritmo> resultados) {
        System.out.println("\n========================================================");
        System.out.println("           COMPARACIÓN DE LAS TRES SOLUCIONES");
        System.out.println("========================================================");

        if (!resultados.containsKey("DFS")) resultados.put("DFS", grafo.DFS(false, 0));
        if (!resultados.containsKey("BFS")) resultados.put("BFS", grafo.BFS(false, 0));
        if (!resultados.containsKey("Dijkstra")) resultados.put("Dijkstra", grafo.caminoMasCorto(false, 0));

        String[] nombres = {"DFS", "BFS", "Dijkstra"};
        for (String nombre : nombres) {
            ResultadoAlgoritmo res = resultados.get(nombre);
            System.out.println("\n>>> Mapa solución " + nombre + " (* = ruta final, · = explorado):");
            if (res.encontrado) {
                System.out.println(grafo.renderizar(extraerIds(res.ordenVisita), null, extraerIds(res.camino)));
            } else {
                System.out.println("Sin solución.");
            }
        }

        System.out.println("\n----------------------------------------------------------------");
        System.out.printf("%-12s | %-8s | %-11s | %-11s | %-7s%n",
                "Algoritmo", "¿Llegó?", "Explorados", "Pasos ruta", "Costo");
        System.out.println("----------------------------------------------------------------");
        for (String nombre : nombres) {
            ResultadoAlgoritmo r = resultados.get(nombre);
            String llego = r.encontrado ? "Sí" : "No";
            String pasos = r.encontrado ? String.valueOf(r.camino.size() - 1) : "-";
            String costo = r.encontrado ? String.valueOf(r.costoTotal) : "-";
            System.out.printf("%-12s | %-8s | %-11d | %-11s | %-7s%n",
                    nombre, llego, r.ordenVisita.size(), pasos, costo);
        }
        System.out.println("----------------------------------------------------------------");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Grafo grafo = new Grafo();
        boolean laberintoCargado = false;
        Map<String, ResultadoAlgoritmo> resultados = new HashMap<>();

        while (true) {
            System.out.println("\n========================================");
            System.out.println("      RESOLUTOR DE LABERINTOS (GRAFOS)");
            System.out.println("========================================");
            System.out.println("1. Cargar laberinto");
            System.out.println("2. Resolver laberinto con DFS");
            System.out.println("3. Resolver laberinto con BFS");
            System.out.println("4. Resolver laberinto con Dijkstra (camino más corto)");
            System.out.println("5. Mostrar las tres soluciones");
            System.out.println("6. Salir");
            System.out.println("========================================");
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
                    System.out.println("\n[OK] Laberinto cargado (" + grafo.getVertices().cantidad() +
                            " vértices) y resultados previos descartados:");
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
                    ResultadoAlgoritmo res = grafo.DFS(true, 120);
                    resultados.put("DFS", res);
                    mostrarResultadoIndividual(grafo, res);
                } else if (opcion.equals("3")) {
                    ResultadoAlgoritmo res = grafo.BFS(true, 120);
                    resultados.put("BFS", res);
                    mostrarResultadoIndividual(grafo, res);
                } else if (opcion.equals("4")) {
                    ResultadoAlgoritmo res = grafo.caminoMasCorto(true, 120);
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