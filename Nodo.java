import java.util.Objects;

public class Nodo {
    public final int fila;
    public final int col;

    public Nodo(int fila, int col) {
        this.fila = fila;
        this.col = col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nodo)) return false;
        Nodo nodo = (Nodo) o;
        return fila == nodo.fila && col == nodo.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fila, col);
    }

    @Override
    public String toString() {
        return "(" + fila + "," + col + ")";
    }
}