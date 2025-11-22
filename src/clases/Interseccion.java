package clases;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Interseccion {
    private final int x;
    private final int y;
    private final ReentrantLock lock;
    private Vehiculo vehiculoActual;
    private Semaforo semaforo;
    private final AtomicInteger vehiculosEnEspera;
    private long tiempoTotalEspera;
    private int vehiculosAtendidos;
    private static final int TIEMPO_ESPERA_MAXIMO = 3000;

    public Interseccion(int x, int y){
        this.x = x;
        this.y = y;
        this.lock = new ReentrantLock(true);
        this.vehiculoActual = null;
        this.semaforo = null;
        this.vehiculosEnEspera = new AtomicInteger(0);
        this.tiempoTotalEspera = 0;
        this.vehiculosAtendidos = 0;
    }

    /**
     * Intenta adquirir el lock de la intersección con timeout
     */
    public boolean intentarEntrar(Vehiculo vehiculo, Direccion direccion){
        if (!semaforoPermitePaso(direccion)) {
            System.out.println("🚦 " + vehiculo + " esperando en semáforo rojo en " + this);
            return false;
        }

        if (lock.isHeldByCurrentThread()) {
            System.out.println("⚠️ " + vehiculo + " ya tiene el lock de " + this);
            return true;
        }

        long tiempoInicioEspera = System.currentTimeMillis();
        boolean exito = false;
        boolean estabaEsperando = false;

        try {
            vehiculosEnEspera.incrementAndGet();
            estabaEsperando = true;

            exito = lock.tryLock(TIEMPO_ESPERA_MAXIMO, TimeUnit.MILLISECONDS);

            if(exito){
                this.vehiculoActual = vehiculo;
                long tiempoEspera = System.currentTimeMillis() - tiempoInicioEspera;
                tiempoTotalEspera += tiempoEspera;
                vehiculosAtendidos++;

                System.out.println("✅ " + vehiculo + " entró en " + this +
                        " después de " + tiempoEspera + "ms");
            }
            else{
                System.out.println("❌ " + vehiculo + " TIMEOUT en " + this +
                        " - vehículos esperando: " + vehiculosEnEspera.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("⏹️ " + vehiculo + " interrumpido esperando en " + this);
        } finally {
            if (estabaEsperando) {
                vehiculosEnEspera.decrementAndGet();
            }
        }

        return exito;
    }

    /**
     * Libera el lock de la intersección
     */
    public void salir(Vehiculo vehiculo){
        if (lock.isHeldByCurrentThread()) {
            this.vehiculoActual = null;
            lock.unlock();
            System.out.println("🔓 " + vehiculo + " liberó " + this);
        } else {
            System.err.println("❌ ERROR: " + vehiculo + " intentó liberar lock sin tenerlo en " + this);
        }
    }

    /**
     * Libera el lock sin parámetros (para compatibilidad)
     */
    public void salir(){
        if (lock.isHeldByCurrentThread()) {
            this.vehiculoActual = null;
            lock.unlock();
            System.out.println("🔓 Lock liberado en " + this);
        }
    }

    /**
     * Intenta liberar el lock de manera segura
     */
    public boolean intentarSalir(Vehiculo vehiculo) {
        if (lock.isHeldByCurrentThread() && this.vehiculoActual == vehiculo) {
            this.vehiculoActual = null;
            lock.unlock();
            System.out.println("🔓 " + vehiculo + " liberó " + this);
            return true;
        }
        return false;
    }

    // ✅ GETTER PARA SEMÁFORO - AÑADIR ESTE MÉTODO
    public Semaforo getSemaforo() {
        return semaforo;
    }

    // ✅ SETTER PARA SEMÁFORO - AÑADIR ESTE MÉTODO TAMBIÉN
    public void setSemaforo(Semaforo semaforo) {
        this.semaforo = semaforo;
    }

    public boolean semaforoPermitePaso(Direccion direccion){
        if (semaforo == null) return true;
        return semaforo.puedePasar(direccion);
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public Vehiculo getVehiculoActual() { return vehiculoActual; }
    public int getVehiculosEnEspera() { return vehiculosEnEspera.get(); }

    public double getTiempoPromedioEspera() {
        return vehiculosAtendidos > 0 ? (double)tiempoTotalEspera / vehiculosAtendidos : 0;
    }

    @Override
    public String toString() {
        return String.format("Interseccion(%d,%d)", x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Interseccion that = (Interseccion) obj;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }
}