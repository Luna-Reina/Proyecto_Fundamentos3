# Proyecto_Fundamentos3

# Resolutor de Laberintos con Grafos (Proyecto_Fundamentos3)

Aplicación de consola en Java que modela un laberinto leído desde un archivo de texto plano como un grafo no dirigido ponderado (costo unitario = `1`) y permite resolverlo, visualizar su exploración paso a paso y comparar el desempeño de tres algoritmos clásicos de búsqueda y caminos mínimos: DFS (Búsqueda en Profundidad), BFS (Búsqueda en Anchura) y Dijkstra.

## Autores y Fecha de Entrega

* **Integrante 1:** Luna Camila Reina Santana (GitHub: @Luna-Reina)
* **Integrante 2:** Karol Elizza Cervantes Carballo (GitHub: @karoliiii)
* **Materia:** Fundamentos de Computación III
* **Fecha de entrega:** 28 de noviembre de 2026

## Requisitos, Versión de Java y Dependencias

* **Versión de Java:** Java SE Development Kit (**JDK 17** o superior).
* **Dependencias externas:** -
* **Herramientas recomendadas:** Terminal (PowerShell, CMD, Bash) o Visual Studio Code con Extension Pack for Java.


## Instrucciones para Compilar y Ejecutar

   -Clonar el repositorio y entrar a la carpeta del proyecto:
   -```bash
   -git clone [https://github.com/Luna-Reina/Proyecto_Fundamentos3.git](https://github.com/Luna-Reina/Proyecto_Fundamentos3.git)
   -cd Proyecto_Fundamentos3
    -compilar los archivos: javac *.java
    -ejecutar: java Main

## Responsabilidades de Cada Integrante
  
   Luna Camila Reina Santana (GitHub: @Luna-Reina)
   • Diseño del menú interactivo (Issue #1)
   • Diseño e implementación de la arquitectura de clases del grafo (Vertice, Vertices, Arcos, Grafo) y listas ligadas de adyacencia (Issue #3).
   • Implementación de la visualización paso a paso en consola mediante despliegaLaberinto(List<Vertice>) y renderizado de estados (Issue #5).
   
   Karol Elizza Cervantes Carballo (GitHub: @karoliiii)
   • Validación estricta de archivos de entrada (Issue #2).
   • Implementación de los algoritmos de búsqueda DFS(), BFS() y caminoMasCorto() (Dijkstra) con orden ortogonal y desempate por ID (Issue #4).
   • Implementación de la comparación simultánea sin animación de las tres soluciones y medición de tiempos de ejecución con System.nanoTime() (Issue #6 y Extras).

## Declaración de Uso de Inteligencia Artificial
En cumplimiento con las normas de integridad académica del curso, se declara el uso de herramientas de Inteligencia Artificial generativa como apoyo durante el desarrollo:

Empresa y Modelo: Google, Gemini.

Propósito de uso: Apoyo en la resolución de dudas de sintaxis en Java, depuración de errores de compilación tras fusiones de ramas en Git (git merge/git pull), verificación de casos borde en la validación de archivos .txt y estructuración de la documentación técnica del proyecto (README.md). Todo el código fue revisado, probado y adaptado por los integrantes del equipo.


   
