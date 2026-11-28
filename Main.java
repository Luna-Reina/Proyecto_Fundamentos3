import java.util.*;

public class Main {

    private static Set<Integer> extraerIds(List<Vertice> lista) {
        Set<Integer> ids = new HashSet<>();
        for (Vertice v : lista) {
            ids.add(v.getId());
        }
        return ids;
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
            System.out.println("\n>>> " + nombre + " (Leyenda: E=Entrada, S=Salida, *=Camino final, ·=Visitado):");
            if (res.encontrado) {
                System.out.println(grafo.renderizar(extraerIds(res.ordenVisita), null, extraerIds(res.camino)));
                System.out.println("Vértices visitados: " + res.ordenVisita.size() +
                        " | Movimientos: " + (res.camino.size() - 1) +
                        String.format(" | Tiempo: %.4f ms", res.tiempoMs));
            } else {
                System.out.println(grafo.renderizar(extraerIds(res.ordenVisita), null, null));
                System.out.println("Aviso: No hay solución hacia la salida. | Vértices visitados: " +
                        res.ordenVisita.size() + String.format(" | Tiempo: %.4f ms", res.tiempoMs));
            }
        }

        System.out.println("\n-----------------------------------------------------------------------------");
        System.out.printf("%-10s | %-10s | %-11s | %-18s | %-14s%n",
                "Algoritmo", "¿Solución?", "Visitados", "Movimientos ruta", "Tiempo (ms)");
        System.out.println("-----------------------------------------------------------------------------");
        for (String nombre : nombres) {
            ResultadoAlgoritmo r = resultados.get(nombre);
            String sol = r.encontrado ? "Sí" : "No";
            String movs = r.encontrado ? String.valueOf(r.camino.size() - 1) : "Sin camino";
            System.out.printf("%-10s | %-10s | %-11d | %-18s | %-14.4f%n",
                    nombre, sol, r.ordenVisita.size(), movs, r.tiempoMs);
        }
        System.out.println("-----------------------------------------------------------------------------");
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
                    List<Vertice> ordenVisita = grafo.DFS();
                    resultados.put("DFS", grafo.getUltimoResultado());
                    grafo.despliegaLaberinto(ordenVisita);

                } else if (opcion.equals("3")) {
                    List<Vertice> ordenVisita = grafo.BFS();
                    resultados.put("BFS", grafo.getUltimoResultado());
                    grafo.despliegaLaberinto(ordenVisita);

                } else if (opcion.equals("4")) {
                    List<Vertice> ordenVisita = grafo.caminoMasCorto();
                    resultados.put("Dijkstra", grafo.getUltimoResultado());
                    grafo.despliegaLaberinto(ordenVisita);

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