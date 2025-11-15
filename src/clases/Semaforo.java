package clases;
import java.awt.Color;

/*
 * Semáforo que controla una intersección
 * Es un hilo independiente que cambia de estado automáticamente
 */
public class Semaforo extends Thread {

    public enum EstadoSemaforo { VERDE, AMARILLO, ROJO };

    // Atributos
    private final Interseccion interseccion;
    private EstadoSemaforo estadoSemaforoActual;
    private final int tiempoVerde;
    private final int tiempoAmarillo;
    private final int tiempoRojo;
    private boolean ejecutando;
    private long tiempoUltimoCambio;

    // Control de direcciones permitidas - CAMBIADO
    private boolean direccionHorizontal;

    // Constructor
    public Semaforo(Interseccion interseccion, int tiempoVerde, int tiempoAmarillo, int tiempoRojo){

        this.interseccion = interseccion;
        this.tiempoVerde = tiempoVerde * 1000; // Se convierte a ms
        this.tiempoAmarillo = tiempoAmarillo * 1000; // Se convierte a ms
        this.tiempoRojo = tiempoRojo * 1000; // Se convierte a ms
        this.estadoSemaforoActual = EstadoSemaforo.ROJO;
        this.ejecutando = true;
        this.tiempoUltimoCambio = System.currentTimeMillis();
        this.direccionHorizontal = true; // Comienza con direcciones horizontales

        interseccion.setSemaforo(this);
        this.setDaemon(true);
        this.setName("Semaforo-" + interseccion.getX() + "-" + interseccion.getY());
    }

    // Se crea el método run del hilo
    @Override
    public void run() {
        while (ejecutando) {
            try {
                verificarCambioColor();
                Thread.sleep(100); // Revisar cada 100ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // Metodo para verificar el cambio de color
    private void verificarCambioColor(){
        long tiempoActual = System.currentTimeMillis();
        long tiempoTranscurrido = tiempoActual - tiempoUltimoCambio;

        switch (estadoSemaforoActual){
            case VERDE:
                if (tiempoTranscurrido >= tiempoVerde) {
                    cambiarColor(EstadoSemaforo.AMARILLO);
                }
                break;
            case AMARILLO:
                if (tiempoTranscurrido >= tiempoAmarillo) {
                    cambiarColor(EstadoSemaforo.ROJO);
                    alternarDirecciones(); // CAMBIADO
                }
                break;
            case ROJO:
                if (tiempoTranscurrido >= tiempoRojo) {
                    cambiarColor(EstadoSemaforo.VERDE);
                }
                break;
        }
    }

    // Método para cambiar de color el semaforo
    private void cambiarColor(EstadoSemaforo nuevoEstadoSemaforo) {
        this.estadoSemaforoActual = nuevoEstadoSemaforo;
        this.tiempoUltimoCambio = System.currentTimeMillis();
        System.out.println("Semáforo " + interseccion + " cambió a " + nuevoEstadoSemaforo +
                " - Direcciones: " + (direccionHorizontal ? "HORIZONTAL" : "VERTICAL"));
    }

    // CAMBIADO: Alternar entre direcciones horizontales y verticales
    private void alternarDirecciones(){
        direccionHorizontal = !direccionHorizontal;
    }

    // CAMBIADO: Lógica de paso más flexible
    public boolean puedePasar(Direccion direccion){
        if(estadoSemaforoActual == EstadoSemaforo.VERDE){
            // Si es horizontal: permitir DERECHA e IZQUIERDA
            if (direccionHorizontal) {
                return direccion == Direccion.DERECHA || direccion == Direccion.IZQUIERDA;
            }
            // Si es vertical: permitir ARRIBA y ABAJO
            else {
                return direccion == Direccion.ARRIBA || direccion == Direccion.ABAJO;
            }
        } else if (estadoSemaforoActual == EstadoSemaforo.AMARILLO){
            return true; // En amarillo todos pueden pasar con precaución
        }
        return false; // En rojo nadie pasa
    }

    public void detener() {
        this.ejecutando = false;
        this.interrupt();
    }

    // Getters
    public EstadoSemaforo getEstadoActual() { return estadoSemaforoActual; }
    public Interseccion getInterseccion() { return interseccion; }
    public boolean isDireccionHorizontal() { return direccionHorizontal; } // NUEVO

    public Color getColorVisual() {
        switch (estadoSemaforoActual) {
            case VERDE: return Color.GREEN;
            case AMARILLO: return Color.YELLOW;
            case ROJO: return Color.RED;
            default: return Color.GRAY;
        }
    }
}