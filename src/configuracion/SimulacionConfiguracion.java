// Importación de librerias y paquetes
package configuracion;
import java.io.*;
import java.util.Properties;

// Configuración de la simulación
public class SimulacionConfiguracion {

    // Atributos
    private Properties propiedades;
    private static final String CONFIG = "simulacion.config";

    // Valores de la simulación
    private int ciudadTamanio = 12;
    private int numVehiculos = 20;
    private int semaforoVerde = 5;
    private int semaforoAmarillo = 2;
    private int semaforoRojo = 6;
    private int velocidadSimulacion = 1000;

    // Constructor
    public SimulacionConfiguracion(){
        propiedades = new Properties();
        cargarConfiguracion();
    }

    // Método para cargar la configuración
    private void cargarConfiguracion() {
        try (InputStream input = new FileInputStream(CONFIG)) {
            propiedades.load(input);
            ciudadTamanio = Integer.parseInt(propiedades.getProperty("ciudad.tamanio", "12"));
            numVehiculos = Integer.parseInt(propiedades.getProperty("vehiculos.cantidad", "50"));
            semaforoVerde = Integer.parseInt(propiedades.getProperty("semaforo.verde", "5"));
            semaforoAmarillo = Integer.parseInt(propiedades.getProperty("semaforo.amarillo", "2"));
            semaforoRojo = Integer.parseInt(propiedades.getProperty("semaforo.rojo", "6"));
            velocidadSimulacion = Integer.parseInt(propiedades.getProperty("simulacion.velocidad", "1000"));
        } catch (IOException e) {
            System.out.println("Archivo de configuración no encontrado, usando valores por defecto");
        }
    }


    // Método para guardar configuracion
    public void guardarConfiguracion(){
        try (OutputStream output = new FileOutputStream(CONFIG)) {
            propiedades.setProperty("ciudad.tamanio", String.valueOf(ciudadTamanio));
            propiedades.setProperty("vehiculos.cantidad", String.valueOf(numVehiculos));
            propiedades.setProperty("semaforo.verde", String.valueOf(semaforoVerde));
            propiedades.setProperty("semaforo.amarillo", String.valueOf(semaforoAmarillo));
            propiedades.setProperty("semaforo.rojo", String.valueOf(semaforoRojo));
            propiedades.setProperty("simulacion.velocidad", String.valueOf(velocidadSimulacion));
            propiedades.store(output, "Configuración de Simulación de Tráfico");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Getters y Setters
    public int getCiudadTamano() { return ciudadTamanio; }
    public void setCiudadTamano(int ciudadTamano) { this.ciudadTamanio = ciudadTamano; }

    public int getNumVehiculos() { return numVehiculos; }
    public void setNumVehiculos(int numVehiculos) { this.numVehiculos = numVehiculos; }

    public int getSemaforoVerde() { return semaforoVerde; }
    public void setSemaforoVerde(int semaforoVerde) { this.semaforoVerde = semaforoVerde; }

    public int getSemaforoAmarillo() { return semaforoAmarillo; }
    public void setSemaforoAmarillo(int semaforoAmarillo) { this.semaforoAmarillo = semaforoAmarillo; }

    public int getSemaforoRojo() { return semaforoRojo; }
    public void setSemaforoRojo(int semaforoRojo) { this.semaforoRojo = semaforoRojo; }

    public int getVelocidadSimulacion() { return velocidadSimulacion; }
    public void setVelocidadSimulacion(int velocidadSimulacion) { this.velocidadSimulacion = velocidadSimulacion; }
}
