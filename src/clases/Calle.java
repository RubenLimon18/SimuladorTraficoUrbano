package clases;

public class Calle {

    // Atributos
    private final Interseccion inicio;
    private final Interseccion fin;
    private final Direccion direccion;
    private final boolean dobleSentido;
    private final String nombre;
    private int vehiculosTransitando;

    // Constructor
    public Calle(Interseccion inicio, Interseccion fin, Direccion direccion, boolean dobleSentido, String nombre) {
        this.inicio = inicio;
        this.fin = fin;
        this.direccion = direccion;
        this.dobleSentido = dobleSentido;
        this.nombre = nombre;
        this.vehiculosTransitando = 0;
    }

    // Métodos

    @Override
    public String toString() {
        return String.format("Calle %s: %s -> %s %s", nombre, inicio, fin, dobleSentido ? "(Doble)" : "(Un sentido)");
    }

}
