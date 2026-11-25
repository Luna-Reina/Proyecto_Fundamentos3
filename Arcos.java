public class Arcos {
    private final Vertice origen;
    private final Vertice destino;
    private final int costo;

    public Arcos(Vertice origen, Vertice destino, int costo) {
        this.origen = origen;
        this.destino = destino;
        this.costo = costo;
    }

    public Vertice getOrigen() {
        return origen;
    }

    public Vertice getDestino() {
        return destino;
    }

    public int getCosto() {
        return costo;
    }

    @Override
    public String toString() {
        return origen + " <-> " + destino + " [costo=" + costo + "]";
    }
}