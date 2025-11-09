package clases;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
* Esta clase representa una intersección en la ciudad con control de concurrencia
* Cada intersección es un recurso crítico que requiere sincronización
* */
public class Interseccion {
    private final int x;
    private final int y;
    private final Lock lock;
    private Vehiculo vehiculoActual;
    private Semaforo semaforo;
    private int vehiculosEnEspera;
    private long tiempoTotalEspera;
    private int vehiculosAtendidos;


    // Constructor
    public Interseccion(int x, int y){
        this.x = x;
        this.y = y;
        this.lock = new ReentrantLock(true);
        this.vehiculoActual = null;
        this.semaforo = null;
        this.vehiculosEnEspera = 0;
        this.tiempoTotalEspera = 0;
        this.vehiculosAtendidos = 0;
    }


    // Métodos

    /*
     * Intenta adquirir el lock de la intersección
     * @param vehiculo El vehículo que intenta entrar
     * @return true si se adquirió el lock, false en caso contrario
    */
    public boolean intentarEntrar (Vehiculo vehiculo){
        long tiempoInicioEspera = System.currentTimeMillis();
        boolean exito = lock.tryLock();

        // Se verifica si se puede obtener el lock
        if(exito){
            this.vehiculoActual = vehiculo;
            long tiempoEspera = System.currentTimeMillis() - tiempoInicioEspera;
            tiempoTotalEspera += tiempoEspera;
            vehiculosAtendidos ++;
            vehiculosEnEspera = Math.max(0, vehiculosEnEspera - 1);
        }
        else{
            vehiculosEnEspera ++ ;
        }

        return exito;
    }


    /*
    * Libera el lock de la intersección
    * */
    public void salir(){
        this.vehiculoActual = null;
        lock.unlock();
    }

    /*
    * Verifica si la intersección esta ocupada
    * */
    public boolean estaOcupada(){
        return vehiculoActual != null;
    }


    /*
    * Verifica si el semáforo permite el paso en la direccion dada
    * */
    public boolean semaforoPermitePaso(Direccion direccion){
        if (semaforo == null) return true; // Intersección sin semáforo
        return semaforo.puedePasar(direccion);
    }


    // Getters y Setters
    public int getX() { return x; }
    public int getY() { return y; }
    public Vehiculo getVehiculoActual() { return vehiculoActual; }
    public Semaforo getSemaforo() { return semaforo; }
    public void setSemaforo(Semaforo semaforo) { this.semaforo = semaforo; }
    public int getVehiculosEnEspera() { return vehiculosEnEspera; }
    public double getTiempoPromedioEspera() {
        return vehiculosAtendidos > 0 ? (double)tiempoTotalEspera / vehiculosAtendidos : 0;
    }

    // Para logs
    @Override
    public String toString() {
        return String.format("Interseccion(%d,%d)", x, y);
    }

}

