package clases;

import algoritmo.RoutePlanner;


import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Representa un vehículo que se mueve por la ciudad
 * Es un hilo independiente que sigue una ruta hacia su destino
 */
public class Vehiculo extends Thread {
    private final int id;
    private final Interseccion partida;
    private final Interseccion destino;
    private final Ciudad ciudad;
    private final RoutePlanner routePlanner;

    private Interseccion posicionActual;
    private List<Interseccion> ruta;
    private int indiceRutaActual;
    private boolean haLlegado;
    private boolean pausado;
    private boolean ejecutando;

    // Estadísticas
    private long tiempoInicioViaje;
    private long tiempoFinViaje;
    private long tiempoTotalEspera;
    private long tiempoUltimaEspera;

    // Sincronización
    private final ReentrantLock lockMovimiento;
    private final Random random;

    public Vehiculo(int id, Interseccion partida, Interseccion destino,
                    Ciudad ciudad, RoutePlanner routePlanner) {
        this.id = id;
        this.partida = partida;
        this.destino = destino;
        this.ciudad = ciudad;
        this.routePlanner = routePlanner;

        this.posicionActual = partida;
        this.haLlegado = false;
        this.pausado = false;
        this.ejecutando = true;
        this.tiempoTotalEspera = 0;
        this.lockMovimiento = new ReentrantLock();
        this.random = new Random();

        // Calcular ruta inicial
        calcularRuta();

        this.setName("Vehiculo-" + id);
        this.setDaemon(true);
    }

    @Override
    public void run() {
        tiempoInicioViaje = System.currentTimeMillis();
        System.out.println("Vehículo " + id + " inició viaje de " + partida + " a " + destino);

        while (ejecutando && !haLlegado && indiceRutaActual < ruta.size()) {
            try {
                if (pausado) {
                    Thread.sleep(100);
                    continue;
                }

                moverASiguienteInterseccion();
                Thread.sleep(500 + random.nextInt(500)); // Velocidad variable

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (haLlegado) {
            tiempoFinViaje = System.currentTimeMillis();
            System.out.println("Vehículo " + id + " llegó a destino en " + getTiempoViaje() + "ms");
        }
    }

    /**
     * Intenta moverse a la siguiente intersección en la ruta
     */
    private void moverASiguienteInterseccion() {
        if (indiceRutaActual >= ruta.size() - 1) {
            haLlegado = true;
            return;
        }

        Interseccion siguiente = ruta.get(indiceRutaActual + 1);

        // Verificar si hay semáforo y si permite el paso
        if (siguiente.getSemaforo() != null) {
            Direccion direccionMovimiento = obtenerDireccionMovimiento(posicionActual, siguiente);
            if (!siguiente.getSemaforo().puedePasar(direccionMovimiento)) {
                // Semáforo en rojo, esperar
                esperarEnSemaforo();
                return;
            }
        }

        // Intentar entrar a la intersección
        if (siguiente.intentarEntrar(this)) {
            // Éxito: moverse a la siguiente intersección
            lockMovimiento.lock();
            try {
                posicionActual.salir(); // Salir de la intersección actual
                posicionActual = siguiente;
                indiceRutaActual++;

                // Verificar si llegó al destino
                if (posicionActual.equals(destino)) {
                    haLlegado = true;
                    posicionActual.salir(); // Liberar la intersección destino
                }
            } finally {
                lockMovimiento.unlock();
            }
        } else {
            // Intersección ocupada, esperar
            esperarEnInterseccion();
        }
    }

    /**
     * Espera cuando el semáforo está en rojo
     */
    private void esperarEnSemaforo() {
        try {
            tiempoUltimaEspera = System.currentTimeMillis();
            Thread.sleep(1000); // Esperar 1 segundo
            tiempoTotalEspera += (System.currentTimeMillis() - tiempoUltimaEspera);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Espera cuando la intersección está ocupada
     */
    private void esperarEnInterseccion() {
        try {
            tiempoUltimaEspera = System.currentTimeMillis();
            Thread.sleep(500 + random.nextInt(1000)); // Espera variable
            tiempoTotalEspera += (System.currentTimeMillis() - tiempoUltimaEspera);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Calcula la ruta desde la posición actual hasta el destino
     */
    private void calcularRuta() {
        this.ruta = routePlanner.calcularRuta(posicionActual, destino);
        this.indiceRutaActual = 0;

        if (ruta.isEmpty()) {
            System.err.println("Vehículo " + id + ": No se pudo calcular ruta");
            haLlegado = true;
        }
    }

    /**
     * Obtiene la dirección de movimiento entre dos intersecciones
     */
    private Direccion obtenerDireccionMovimiento(Interseccion actual, Interseccion siguiente) {
        int dx = siguiente.getX() - actual.getX();
        int dy = siguiente.getY() - actual.getY();

        if (dx > 0) return Direccion.DERECHA;
        if (dx < 0) return Direccion.IZQUIERDA;
        if (dy > 0) return Direccion.ABAJO;
        if (dy < 0) return Direccion.ARRIBA;

        return Direccion.ARRIBA; // Default
    }

    /**
     * Pausa el vehículo
     */
    public void pausar() {
        this.pausado = true;
    }

    /**
     * Reanuda el vehículo
     */
    public void reanudar() {
        this.pausado = false;
    }

    /**
     * Detiene el vehículo permanentemente
     */
    public void detener() {
        this.ejecutando = false;
        this.interrupt();
        if (posicionActual != null) {
            posicionActual.salir();
        }
    }

    // Getters para estado del vehículo
    public int getIdVehiculo() { return id; }
    public Interseccion getPartida() { return partida; }
    public Interseccion getDestino() { return destino; }
    public Interseccion getInterseccionActual() { return posicionActual; }
    public boolean haLlegado() { return haLlegado; }
    public boolean estaEsperando() { return pausado || tiempoUltimaEspera > 0; }
    public boolean estaEnInterseccion() { return posicionActual != null; }

    // Getters para estadísticas
    public long getTiempoViaje() {
        if (tiempoInicioViaje == 0) return 0;
        if (tiempoFinViaje == 0) return System.currentTimeMillis() - tiempoInicioViaje;
        return tiempoFinViaje - tiempoInicioViaje;
    }

    public long getTiempoEsperaTotal() { return tiempoTotalEspera; }

    public double getProgreso() {
        if (ruta == null || ruta.size() <= 1) return 0.0;
        return (double) indiceRutaActual / (ruta.size() - 1);
    }

    /**
     * Obtiene la posición actual para visualización
     */
    public String getPosicionActual() {
        if (posicionActual == null) return "N/A";
        return String.format("(%d,%d)", posicionActual.getX(), posicionActual.getY());
    }

    @Override
    public String toString() {
        return String.format("Vehículo %d [%s -> %s] %s",
                id, partida, destino, haLlegado ? "🏁" : "🚗");
    }
}