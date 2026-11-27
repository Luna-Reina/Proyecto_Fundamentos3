import java.util.*;

public class Main {

    private static Set<Integer> extraerIds(List<Vertice> lista) {
        Set<Integer> ids = new HashSet<>();
        for (Vertice v : lista) {
            ids.add(v.getId());
        }
        return ids;
    }

    private static void ejecutarYDesplegarPasoAPaso(Grafo grafo, ResultadoAlgoritmo res, Scanner scanner) {
        System.out.println("\n¿Cómo deseas visualizar la exploración paso a paso?");
        System.out.println("1. Pausa automática (configurable en milisegundos)");
        System.out.println("2. Manual (avanzar presionando ENTER)");
        System.out.print("Elige modo (1 o 2): ");
        String modo = scanner.nextLine().trim();

        if (modo.equals("2")) {
            grafo.despliegaLaberinto(res, true, 0, scanner);
        } else {
            System.out.print("Ingresa la pausa en milisegundos (ej. 150, Enter para 150): ");
            String pausaStr = scanner.nextLine().trim();
            int pausaMs = 150;
            if (!pausaStr.isEmpty()) {
                try {
                    pausaMs = Math.max(0, Integer.parseInt(pausaStr));
                } catch (NumberFormatException e) {
                    System.out.println("[INFO] Valor no numérico; usando 150 ms por defecto.");
                }
            }
            grafo.despliegaLaberinto(res, false, pausaMs, scanner);
        }
    }

    private static void compararSoluciones(Grafo grafo, Map<String, ResultadoAlgoritmo> resultados) {
        System.out.println("COMPARACIÓN DE LAS TRES SOLUCIONES");

        if (!resultados.containsKey("DFS")) resultados.put("DFS", grafo.DFS());
        if (!resultados.containsKey("BFS")) resultados.put("BFS", grafo.BFS());
        if (!resultados.containsKey("Dijkstra")) resultados.put("Dijkstra", grafo.caminoMasCorto());

        String[] nombres = {"DFS", "BFS", "Dijkstra"};
        for (String nombre : nombres) {
            ResultadoAlgoritmo res = resultados.get(nombre);
            System.out.println("\n>>> Mapa solución " + nombre + " (* = camino final, · = visitado):");
            if (res.encontrado) {
                System.out.println(grafo.renderizar(extraerIds(res.ordenVisita), null, extraerIds(res.camino)));
            } else {
                System.out.println("Sin solución (explorados = ·):");
                System.out.println(grafo.renderizar(extraerIds(res.ordenVisita), null, null));
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
            System.out.println("RESOLUTOR DE LABERINTOS (GRAFOS)");
            System.out.println("1. Cargar laberinto");
            System.out.println("2. Resolver laberinto con DFS");
            System.out.println("3. Resolver laberinto con BFS");
            System.out.println("4. Resolver laberinto con Dijkstra (camino más corto)");
            System.out.println("5. Mostrar las tres soluciones");
            System.out.println("6. Salir");
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
                    ResultadoAlgoritmo res = grafo.DFS();
                    resultados.put("DFS", res);
                    ejecutarYDesplegarPasoAPaso(grafo, res, scanner);
                } else if (opcion.equals("3")) {
                    ResultadoAlgoritmo res = grafo.BFS();
                    resultados.put("BFS", res);
                    ejecutarYDesplegarPasoAPaso(grafo, res, scanner);
                } else if (opcion.equals("4")) {
                    ResultadoAlgoritmo res = grafo.caminoMasCorto();
                    resultados.put("Dijkstra", res);
                    ejecutarYDesplegarPasoAPaso(grafo, res, scanner);
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