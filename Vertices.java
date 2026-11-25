import java.util.Collection;
import java.util.Hashtable;

public class Vertices {
    private final Hashtable<Integer, Vertice> tablaVertices;

    public Vertices() {
        this.tablaVertices = new Hashtable<>();
    }

    public void agregar(Vertice v) {
        tablaVertices.put(v.getId(), v);
    }

    public Vertice obtenerPorId(int id) {
        return tablaVertices.get(id);
    }

    public Vertice buscarPorCoordenada(int fila, int columna) {
        for (Vertice v : tablaVertices.values()) {
            if (v.getFila() == fila && v.getColumna() == columna) {
                return v;
            }
        }
        return null;
    }

    public boolean existe(int id) {
        return tablaVertices.containsKey(id);
    }

    public int cantidad() {
        return tablaVertices.size();
    }

    public Collection<Vertice> todos() {
        return tablaVertices.values();
    }

    public Hashtable<Integer, Vertice> getTabla() {
        return tablaVertices;
    }
}