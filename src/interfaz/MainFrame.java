package interfaz;
import clases.*;
import configuracion.SimulacionConfiguracion;
import algoritmo.RoutePlanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainFrame extends JFrame{

    // Atributos
    private SimulacionConfiguracion config;
    private Ciudad ciudad;
    private RoutePlanner routePlanner; // ← CORREGIDO: era "routePlaner"
    private volatile boolean hiloActivo = false;
    private Thread hiloSimulacion;

    // Componentes de la Interfaz
    private CityPanel cityPanel;
    private ControlPanel controlPanel;
    private StatsPanel statsPanel;
    private JSplitPane splitPane;
    private JScrollPane scrollPane; // ← AGREGADO: JScrollPane

    // Estado de la simulación
    private boolean simulacionEjecutandose = false;
    private boolean simulacionPausada = false;

    // Constructor
    public MainFrame(){
        this.config = new SimulacionConfiguracion();
        inicializarCiudad();
        inicializarGUI();
        configurarVentana();
    }

    // Métodos
    private void inicializarCiudad(){
        int tamanio = config.getCiudadTamanio();
        this.ciudad = new Ciudad(tamanio, tamanio);
        this.routePlanner = new RoutePlanner(ciudad); // ← CORREGIDO

        // Se inicializa la ciudad con las calles y semaforos
        ciudad.inicializarCalles();
        ciudad.inicializarSemaforos(config.getSemaforoVerde(), config.getSemaforoAmarillo(), config.getSemaforoRojo());
    }

    private void inicializarGUI(){
        // Se crean los paneles
        cityPanel = new CityPanel(ciudad, config);
        controlPanel = new ControlPanel(this, config);
        statsPanel = new StatsPanel();

        // ← AGREGADO: Crear JScrollPane para el CityPanel
        scrollPane = new JScrollPane(cityPanel);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Configurar split pane para estadísticas - USAR SCROLLPANE
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(scrollPane); // ← CAMBIADO: usar scrollPane en lugar de cityPanel
        splitPane.setBottomComponent(statsPanel);
        splitPane.setResizeWeight(0.7);

        add(splitPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // Actualizar estadísticas iniciales
        actualizarEstadisticas();
    }

    private void configurarVentana(){
        setTitle("🚦 Simulador de Tráfico Urbano - Ciudad " + config.getCiudadTamanio() + "x" + config.getCiudadTamanio());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() { // ← CORREGIDO: usar WindowListener
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarSimulacion();
            }
        });

        pack();
        setLocationRelativeTo(null);

        // Configurar el divider después de pack()
        SwingUtilities.invokeLater(() -> {
            splitPane.setDividerLocation(0.7);
        });

        // No maximizar completamente para mejor visualización con scroll
        setExtendedState(JFrame.NORMAL);
        setSize(1000, 800); // Tamaño inicial adecuado
    }

    /*
     * Inicia la simulación con los parámetros configurados
     */
    public void iniciarSimulacion() {
        if (simulacionEjecutandose) {
            JOptionPane.showMessageDialog(this, "La simulación ya está en ejecución");
            return;
        }

        try {
            System.out.println("=== INICIANDO SIMULACIÓN ===");

            // Crear vehículos
            ciudad.crearVehiculos(config.getNumVehiculos(), routePlanner); // ← CORREGIDO
            System.out.println("Vehículos creados, total: " + ciudad.getVehiculos().size());

            // DEBUG: Verificar vehículos inmediatamente después de crearlos
            System.out.println("DEBUG - Vehículos en ciudad: " + ciudad.getVehiculos().size());
            for (Vehiculo v : ciudad.getVehiculos()) {
                System.out.println("Vehículo " + v.getIdVehiculo() + " - Intersección: " + v.getInterseccionActual());
            }

            // FORZAR ACTUALIZACIÓN INMEDIATA DEL PANEL
            cityPanel.repaint();
            SwingUtilities.invokeLater(() -> {
                cityPanel.repaint();
            });

            // Iniciar semáforos
            ciudad.iniciarSemaforos();

            // Iniciar vehículos
            ciudad.iniciarVehiculos();
            System.out.println("Vehículos iniciados");

            simulacionEjecutandose = true;
            simulacionPausada = false;
            hiloActivo = true;

            // Iniciar hilo de actualización de GUI
            hiloSimulacion = new Thread(this::ejecutarCicloSimulacion, "Simulacion-GUI");
            hiloSimulacion.start();

            controlPanel.simulacionIniciada();
            actualizarEstadisticas();

            // FORZAR OTRO REPAINT DESPUÉS DE INICIAR
            SwingUtilities.invokeLater(() -> {
                cityPanel.repaint();
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al iniciar simulación: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /*
     * Pausa o reanuda la simulación
     */
    public void pausarReanudarSimulacion(){
        if(!simulacionEjecutandose) return;

        simulacionPausada = !simulacionPausada;

        if(simulacionPausada){
            ciudad.pausarVehiculos();
            ciudad.pausarSemaforos();
            controlPanel.simulacionPausada();
        } else {
            ciudad.reanudarVehiculos();
            ciudad.reanudarSemaforos();
            controlPanel.simulacionReanudada();
        }
    }

    /*
     * Detiene la simulación actual
     */
    public void detenerSimulacion() {
        if (!simulacionEjecutandose) return;

        simulacionEjecutandose = false;
        simulacionPausada = false;
        hiloActivo = false;

        // Detener el hilo de manera segura
        if (hiloSimulacion != null && hiloSimulacion.isAlive()) {
            hiloSimulacion.interrupt();
        }

        ciudad.detenerVehiculos();
        ciudad.detenerSemaforos();

        controlPanel.simulacionDetenida();
        mostrarResumenFinal();
    }

    /*
     * Ciclo principal de actualización de la simulación
     */
    private AtomicBoolean resumenMostrado = new AtomicBoolean(false);
    private void ejecutarCicloSimulacion() {
        int ciclo = 0;
        while (simulacionEjecutandose && hiloActivo) {
            if (!simulacionPausada) {
                final int cicloActual = ciclo;  // ← COPIA EFECTIVAMENTE FINAL
                SwingUtilities.invokeLater(() -> {
                    try {
                        System.out.println("=== CICLO " + (cicloActual) + " ===");
                        System.out.println("Vehículos en ciudad: " + ciudad.getVehiculos().size());

                        cityPanel.repaint();
                        actualizarEstadisticas();

                        if (ciudad.todosVehiculosLlegaron() && !resumenMostrado.get()) {
                            detenerSimulacion();
                            resumenMostrado.set(true);
                        }
                    } catch (Exception e) {
                        System.err.println("Error en ciclo de simulación: " + e.getMessage());
                    }
                });
                ciclo++;
            }

            try {
                Thread.sleep(config.getVelocidadSimulacion());
            } catch (InterruptedException e) {
                System.out.println("Hilo de simulación interrumpido");
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Hilo de simulación terminado");
    }

    /*
     * Actualiza el panel de estadísticas
     */
    private void actualizarEstadisticas(){
        StatsManager stats = ciudad.getStatsManager();
        statsPanel.actualizarEstadisticas(ciudad);
    }

    /*
     * Abre el diálogo de configuración
     */
    public void abrirConfiguracion(){
        ConfigDialog dialog = new ConfigDialog(this, config);
        dialog.setVisible(true);

        if(dialog.isConfiguracionActualizada()){
            // Reiniciar con nueva configuracion
            inicializarCiudad();

            cityPanel.actualizarCiudad(ciudad);
            actualizarEstadisticas();
            actualizarTitulo();

            // ← AGREGADO: Actualizar el scroll pane
            scrollPane.setViewportView(cityPanel);
            scrollPane.revalidate();
            scrollPane.repaint();
        }
    }

    private void actualizarTitulo() {
        setTitle("🚦 Simulador de Tráfico Urbano - Ciudad " + config.getCiudadTamanio() + "x" + config.getCiudadTamanio());
    }

    /*
     * Cierra la aplicación de manera controlada
     */
    private void cerrarSimulacion(){
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que quieres salir?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Detener simulación si está en ejecución
            if (simulacionEjecutandose) {
                detenerSimulacion();
            }
            System.exit(0);
        }
    }

    /**
     * Muestra el resumen final de la simulación
     */
    private void mostrarResumenFinal() {
        StatsManager stats = ciudad.getStatsManager();
        statsPanel.mostrarResumenFinal(stats);

        // Mostrar resumen final
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    stats.generarResumenCompleto(),
                    "🏁 Resumen de Simulación",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // Getters para los paneles
    public CityPanel getCityPanel() { return cityPanel; }
    public StatsPanel getStatsPanel() { return statsPanel; }
    public Ciudad getCiudad() { return ciudad; }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}