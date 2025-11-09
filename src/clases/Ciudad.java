package clases;

import algoritmo.RoutePlanner;

import java.util.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;

/*
 * Representa la ciudad completa con calles, intersecciones, semáforos y vehículos
 */
public class Ciudad {
    private final int ancho;
    private final int alto;
    private Interseccion[][] intersecciones;
    private List<Calle> calles;
    private List<Semaforo> semaforos;
    private List<Vehiculo> vehiculos;
    private StatsManager statsManager;
    private ExecutorService executorVehiculos;
    private boolean simulacionActiva;

    // Nombres de calles para darle realismo
    private static final String[] NOMBRES_CALLES_HORIZONTALES = {
            "Avenida Central", "Calle Norte", "Avenida del Sol", "Calle Este",
            "Avenida Libertad", "Calle Oeste", "Avenida Parque", "Calle Sur",
            "Avenida Universidad", "Calle Comercio", "Avenida Industrial", "Calle Jardín"
    };

    private static final String[] NOMBRES_CALLES_VERTICALES = {
            "Avenida Principal", "Calle 1", "Avenida 2", "Calle 3",
            "Avenida 4", "Calle 5", "Avenida 6", "Calle 7",
            "Avenida 8", "Calle 9", "Avenida 10", "Calle 11"
    };

    public Ciudad(int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;
        this.intersecciones = new Interseccion[ancho][alto];
        this.calles = new ArrayList<>();
        this.semaforos = new ArrayList<>();
        this.vehiculos = new ArrayList<>();
        this.statsManager = new StatsManager(this);
        this.executorVehiculos = Executors.newCachedThreadPool();
        this.simulacionActiva = false;

        inicializarIntersecciones();
    }

    /**
     * Inicializa todas las intersecciones de la ciudad
     */
    private void inicializarIntersecciones() {
        for (int x = 0; x < ancho; x++) {
            for (int y = 0; y < alto; y++) {
                intersecciones[x][y] = new Interseccion(x, y);
            }
        }
    }

    /*
     * Inicializa las calles de la ciudad (horizontales y verticales)
     */
    public void inicializarCalles() {
        calles.clear();

        // Calles horizontales (de izquierda a derecha)
        for (int y = 0; y < alto; y++) {
            String nombre = y < NOMBRES_CALLES_HORIZONTALES.length ?
                    NOMBRES_CALLES_HORIZONTALES[y] : "Calle " + (y + 1);

            for (int x = 0; x < ancho - 1; x++) {
                Interseccion inicio = intersecciones[x][y];
                Interseccion fin = intersecciones[x + 1][y];
                boolean dobleSentido = (y % 2 == 0); // Calles pares son doble sentido

                Calle calle = new Calle(inicio, fin, Direccion.DERECHA, dobleSentido, nombre);
                calles.add(calle);
            }
        }

        // Calles verticales (de arriba a abajo)
        for (int x = 0; x < ancho; x++) {
            String nombre = x < NOMBRES_CALLES_VERTICALES.length ?
                    NOMBRES_CALLES_VERTICALES[x] : "Avenida " + (x + 1);

            for (int y = 0; y < alto - 1; y++) {
                Interseccion inicio = intersecciones[x][y];
                Interseccion fin = intersecciones[x][y + 1];
                boolean dobleSentido = (x % 2 == 1); // Avenidas impares son doble sentido

                Calle calle = new Calle(inicio, fin, Direccion.ABAJO, dobleSentido, nombre);
                calles.add(calle);
            }
        }

        System.out.println("Ciudad inicializada: " + calles.size() + " calles creadas");
    }

    /*
     * Inicializa los semáforos en intersecciones estratégicas
     */
    public void inicializarSemaforos(int tiempoVerde, int tiempoAmarillo, int tiempoRojo) {
        semaforos.clear();

        // Colocar semáforos en intersecciones principales (cada 2 calles)
        int contadorSemaforos = 0;
        for (int x = 1; x < ancho - 1; x += 2) {
            for (int y = 1; y < alto - 1; y += 2) {
                if (contadorSemaforos < 20) { // Máximo 20 semáforos
                    Interseccion inter = intersecciones[x][y];
                    Semaforo semaforo = new Semaforo(inter, tiempoVerde, tiempoAmarillo, tiempoRojo);
                    semaforos.add(semaforo);
                    contadorSemaforos++;
                }
            }
        }

        System.out.println("Semáforos inicializados: " + semaforos.size() + " semáforos creados");
    }

    /**
     * Crea los vehículos con puntos de partida y destinos
     */
    public void crearVehiculos(int cantidad, RoutePlanner routePlanner) {
        vehiculos.clear();
        Random random = new Random();

        for (int i = 0; i < cantidad; i++) {
            // Puntos de partida en los bordes
            Interseccion partida = generarPuntoEnBorde(random);
            Interseccion destino = generarPuntoEnBorde(random);

            // Asegurar que partida y destino sean diferentes
            while (destino.equals(partida)) {
                destino = generarPuntoEnBorde(random);
            }

            Vehiculo vehiculo = new Vehiculo(i + 1, partida, destino, this, routePlanner);
            vehiculos.add(vehiculo);
        }

        System.out.println("Vehículos creados: " + vehiculos.size());
    }

    /**
     * Genera un punto de partida/destino en el borde de la ciudad
     */
    private Interseccion generarPuntoEnBorde(Random random) {
        int borde = random.nextInt(4); // 0: arriba, 1: derecha, 2: abajo, 3: izquierda

        switch (borde) {
            case 0: // Arriba
                return intersecciones[random.nextInt(ancho)][0];
            case 1: // Derecha
                return intersecciones[ancho - 1][random.nextInt(alto)];
            case 2: // Abajo
                return intersecciones[random.nextInt(ancho)][alto - 1];
            case 3: // Izquierda
                return intersecciones[0][random.nextInt(alto)];
            default:
                return intersecciones[0][0];
        }
    }

    /**
     * Inicia todos los semáforos
     */
    public void iniciarSemaforos() {
        for (Semaforo semaforo : semaforos) {
            semaforo.start();
        }
        System.out.println("Semáforos iniciados");
    }

    /**
     * Inicia todos los vehículos
     */
    public void iniciarVehiculos() {
        simulacionActiva = true;
        statsManager.iniciarSimulacion();

        for (Vehiculo vehiculo : vehiculos) {
            executorVehiculos.execute(vehiculo);
        }
        System.out.println("Vehículos iniciados");
    }

    /**
     * Pausa todos los vehículos
     */
    public void pausarVehiculos() {
        for (Vehiculo vehiculo : vehiculos) {
            vehiculo.pausar();
        }
    }

    /**
     * Reanuda todos los vehículos
     */
    public void reanudarVehiculos() {
        for (Vehiculo vehiculo : vehiculos) {
            vehiculo.reanudar();
        }
    }

    /**
     * Pausa todos los semáforos
     */
    public void pausarSemaforos() {
        for (Semaforo semaforo : semaforos) {
            // Los semáforos se pausan automáticamente cuando su thread se interrumpe
        }
    }

    /**
     * Reanuda todos los semáforos
     */
    public void reanudarSemaforos() {
        // Los semáforos se reanudan automáticamente
    }

    /**
     * Detiene toda la simulación
     */
    public void detenerVehiculos() {
        simulacionActiva = false;
        for (Vehiculo vehiculo : vehiculos) {
            vehiculo.detener();
        }
        executorVehiculos.shutdownNow();
    }

    /**
     * Detiene todos los semáforos
     */
    public void detenerSemaforos() {
        for (Semaforo semaforo : semaforos) {
            semaforo.detener();
        }
    }

    /**
     * Verifica si todos los vehículos han llegado a su destino
     */
    public boolean todosVehiculosLlegaron() {
        for (Vehiculo vehiculo : vehiculos) {
            if (!vehiculo.haLlegado()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Obtiene las calles conectadas a una intersección
     */
    public List<Calle> getCallesDesde(Interseccion interseccion) {
        List<Calle> callesConectadas = new ArrayList<>();
        for (Calle calle : calles) {
            if (calle.getInicio().equals(interseccion) ||
                    (calle.isDobleSentido() && calle.getFin().equals(interseccion))) {
                callesConectadas.add(calle);
            }
        }
        return callesConectadas;
    }

    /**
     * Obtiene una intersección por coordenadas
     */
    public Interseccion getInterseccion(int x, int y) {
        if (x >= 0 && x < ancho && y >= 0 && y < alto) {
            return intersecciones[x][y];
        }
        return null;
    }

    // Getters
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    public List<Calle> getCalles() { return calles; }
    public List<Semaforo> getSemaforos() { return semaforos; }
    public List<Vehiculo> getVehiculos() { return vehiculos; }
    public StatsManager getStatsManager() { return statsManager; }
    public boolean isSimulacionActiva() { return simulacionActiva; }

    /**
     * Obtiene el número de vehículos activos (que no han llegado)
     */
    public int getVehiculosActivos() {
        int activos = 0;
        for (Vehiculo vehiculo : vehiculos) {
            if (!vehiculo.haLlegado()) {
                activos++;
            }
        }
        return activos;
    }
}