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

    // Se verifica si el movimiento se permite en la direccion dada
    public boolean permiteDireccion (Direccion dir){
        if(dobleSentido){
            return dir == direccion || dir == direccion.opuesta();
        }
        return dir == direccion;
    }


    /*
     * Obtiene la intersección destino desde una intersección dada
    */
    public Interseccion getDestinoDesde(Interseccion actual){
        if (actual.equals(inicio)){
            return fin;
        } else if (dobleSentido && actual.equals(fin)){
            return inicio;
        }
        return null;
    }

    /*
     * Obtiene la dirección de movimiento desde una intersección dada
     */
    public Direccion getDireccionDesde(Interseccion actual){
        if(actual.equals(inicio)){
            return direccion;
        } else if (dobleSentido && actual.equals(fin)){
            return direccion.opuesta();
        }
        return null;
    }

    // Getters
    public Interseccion getInicio() { return inicio; }
    public Interseccion getFin() { return fin; }
    public Direccion getDireccion() { return direccion; }
    public boolean isDobleSentido() { return dobleSentido; }
    public String getNombre() { return nombre; }
    public int getVehiculosTransitando() { return vehiculosTransitando; }

    public void incrementarTrafico() { vehiculosTransitando++; }
    public void decrementarTrafico() { vehiculosTransitando = Math.max(0, vehiculosTransitando - 1); }

    @Override
    public String toString() {
        return String.format("Calle %s: %s -> %s %s", nombre, inicio, fin, dobleSentido ? "(Doble)" : "(Un sentido)");
    }

}
