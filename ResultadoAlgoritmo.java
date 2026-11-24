import java.util.List;

public class ResultadoAlgoritmo {
    public final String nombre;
    public final boolean encontrado;
    public final List<Nodo> ordenVisita;
    public final List<Nodo> camino;
    public final int costoTotal;
    public final double tiempoMs;

    public ResultadoAlgoritmo(String nombre, boolean encontrado, List<Nodo> ordenVisita,
                              List<Nodo> camino, int costoTotal, double tiempoMs) {
        this.nombre = nombre;
        this.encontrado = encontrado;
        this.ordenVisita = ordenVisita;
        this.camino = camino;
        this.costoTotal = costoTotal;
        this.tiempoMs = tiempoMs;
    }
}