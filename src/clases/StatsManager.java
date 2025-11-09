package clases;

import java.util.*;

public class StatsManager {
    private Ciudad ciudad;
    private long tiempoInicioSimulacion;

    public StatsManager(Ciudad ciudad) {
        this.ciudad = ciudad;
    }

    public void iniciarSimulacion() {
        tiempoInicioSimulacion = System.currentTimeMillis();
    }

    // Métodos básicos para las estadísticas
    public long getTiempoTotalSimulacion() {
        return (System.currentTimeMillis() - tiempoInicioSimulacion) / 1000;
    }

    public int getVehiculosActivos() {
        return ciudad.getVehiculosActivos();
    }

    public int getVehiculosCompletados() {
        return ciudad.getVehiculos().size() - ciudad.getVehiculosActivos();
    }

    // ... otros métodos de estadísticas
}
