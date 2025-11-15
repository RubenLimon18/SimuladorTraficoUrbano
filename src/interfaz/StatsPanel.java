package interfaz;


import clases.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel que muestra estadísticas y métricas en tiempo real de la simulación
 */
public class StatsPanel extends JPanel {
    private JTabbedPane tabbedPane;

    // Pestañas
    private JPanel panelResumen;
    private JPanel panelVehiculos;
    private JPanel panelSemaforos;
    private JPanel panelMetricas;

    // Componentes de Resumen
    private JLabel lblTiempoTotal;
    private JLabel lblVehiculosActivos;
    private JLabel lblVehiculosCompletados;
    private JLabel lblCongestionTotal;
    private JLabel lblPrimerVehiculo;
    private JLabel lblUltimoVehiculo;

    // Tablas
    private JTable tablaVehiculos;
    private JTable tablaSemaforos;
    private JTable tablaMetricas;

    // Modelos de tabla
    private DefaultTableModel modelVehiculos;
    private DefaultTableModel modelSemaforos;
    private DefaultTableModel modelMetricas;

    public StatsPanel() {
        inicializarComponentes();
        configurarLayout();
        inicializarTablas();
    }

    private void inicializarComponentes() {
        tabbedPane = new JTabbedPane();

        // Inicializar paneles
        panelResumen = new JPanel();
        panelVehiculos = new JPanel();
        panelSemaforos = new JPanel();
        panelMetricas = new JPanel();

        // Componentes de resumen
        lblTiempoTotal = crearEtiquetaEstadistica("0s");
        lblVehiculosActivos = crearEtiquetaEstadistica("0");
        lblVehiculosCompletados = crearEtiquetaEstadistica("0");
        lblCongestionTotal = crearEtiquetaEstadistica("0");
        lblPrimerVehiculo = crearEtiquetaEstadistica("N/A");
        lblUltimoVehiculo = crearEtiquetaEstadistica("N/A");
    }

    private JLabel crearEtiquetaEstadistica(String valor) {
        JLabel label = new JLabel(valor);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.BLUE);
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        return label;
    }

    private void configurarLayout() {
        setLayout(new BorderLayout());

        configurarPanelResumen();
        configurarPanelVehiculos();
        configurarPanelSemaforos();
        configurarPanelMetricas();

        tabbedPane.addTab("📊 Resumen", panelResumen);
        tabbedPane.addTab("🚗 Vehículos", panelVehiculos);
        tabbedPane.addTab("🚦 Semáforos", panelSemaforos);
        tabbedPane.addTab("📈 Métricas", panelMetricas);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void configurarPanelResumen() {
        panelResumen.setLayout(new GridLayout(6, 2, 10, 5));
        panelResumen.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelResumen.add(crearEtiquetaDescriptiva("Tiempo Total Simulación:"));
        panelResumen.add(lblTiempoTotal);

        panelResumen.add(crearEtiquetaDescriptiva("Vehículos Activos:"));
        panelResumen.add(lblVehiculosActivos);

        panelResumen.add(crearEtiquetaDescriptiva("Vehículos Completados:"));
        panelResumen.add(lblVehiculosCompletados);

        panelResumen.add(crearEtiquetaDescriptiva("Congestión Total:"));
        panelResumen.add(lblCongestionTotal);

        panelResumen.add(crearEtiquetaDescriptiva("Primer Vehículo en Llegar:"));
        panelResumen.add(lblPrimerVehiculo);

        panelResumen.add(crearEtiquetaDescriptiva("Último Vehículo en Llegar:"));
        panelResumen.add(lblUltimoVehiculo);
    }

    private JLabel crearEtiquetaDescriptiva(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        return label;
    }

    private void configurarPanelVehiculos() {
        panelVehiculos.setLayout(new BorderLayout());

        String[] columnas = {"ID", "Posición", "Destino", "Estado", "Tiempo Viaje", "Tiempo Espera", "Progreso"};
        modelVehiculos = new DefaultTableModel(columnas, 0);
        tablaVehiculos = new JTable(modelVehiculos);

        JScrollPane scrollPane = new JScrollPane(tablaVehiculos);
        panelVehiculos.add(scrollPane, BorderLayout.CENTER);
    }

    private void configurarPanelSemaforos() {
        panelSemaforos.setLayout(new BorderLayout());

        String[] columnas = {"Ubicación", "Estado", "Dirección Verde", "Vehículos Espera", "Tiempo Avg Espera"};
        modelSemaforos = new DefaultTableModel(columnas, 0);
        tablaSemaforos = new JTable(modelSemaforos);

        JScrollPane scrollPane = new JScrollPane(tablaSemaforos);
        panelSemaforos.add(scrollPane, BorderLayout.CENTER);
    }

    private void configurarPanelMetricas() {
        panelMetricas.setLayout(new BorderLayout());

        String[] columnas = {"Métrica", "Valor", "Unidad", "Descripción"};
        modelMetricas = new DefaultTableModel(columnas, 0);
        tablaMetricas = new JTable(modelMetricas);

        JScrollPane scrollPane = new JScrollPane(tablaMetricas);
        panelMetricas.add(scrollPane, BorderLayout.CENTER);
    }

    private void inicializarTablas() {
        // Configurar tablas para no ser editables
        tablaVehiculos.setDefaultEditor(Object.class, null);
        tablaSemaforos.setDefaultEditor(Object.class, null);
        tablaMetricas.setDefaultEditor(Object.class, null);

        // Configurar selección simple
        tablaVehiculos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaSemaforos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaMetricas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * Actualiza todas las estadísticas con los datos de la ciudad
     */
    public void actualizarEstadisticas(Ciudad ciudad) {
        if (ciudad == null) return;

        actualizarResumen(ciudad);
        actualizarTablaVehiculos(ciudad);
        actualizarTablaSemaforos(ciudad);
        actualizarTablaMetricas(ciudad);
    }

    private void actualizarResumen(Ciudad ciudad) {
        StatsManager stats = ciudad.getStatsManager();

        lblTiempoTotal.setText(stats.getTiempoTotalSimulacion() + "s");
        lblVehiculosActivos.setText(stats.getVehiculosActivos() + "");
        lblVehiculosCompletados.setText(stats.getVehiculosCompletados() + "");
        lblCongestionTotal.setText(stats.getCongestionTotal() + "");

        Vehiculo primero = stats.getPrimerVehiculoEnLlegar();
        Vehiculo ultimo = stats.getUltimoVehiculoEnLlegar();

        lblPrimerVehiculo.setText(primero != null ?
                "Vehículo " + primero.getId() + " (" + stats.getTiempoPrimerVehiculo() + "s)" : "N/A");
        lblUltimoVehiculo.setText(ultimo != null ?
                "Vehículo " + ultimo.getId() : "N/A");
    }

    private void actualizarTablaVehiculos(Ciudad ciudad) {
        modelVehiculos.setRowCount(0);

        for (Vehiculo vehiculo : ciudad.getVehiculos()) {
            Object[] fila = {
                    vehiculo.getId(),
                    vehiculo.getPosicionActual(),
                    vehiculo.getDestino(),
                    obtenerEstadoVehiculo(vehiculo),
                    vehiculo.getTiempoViaje() + "s",
                    vehiculo.getTiempoEsperaTotal() + "s",
                    String.format("%.1f%%", vehiculo.getProgreso() * 100)
            };
            modelVehiculos.addRow(fila);
        }
    }

    private String obtenerEstadoVehiculo(Vehiculo vehiculo) {
        if (vehiculo.haLlegado()) return "🏁 Llegó";
        if (vehiculo.estaEsperando()) return "⏳ Esperando";
        if (vehiculo.estaEnInterseccion()) return "🚦 En Intersección";
        return "🚗 Moviéndose";
    }

    private void actualizarTablaSemaforos(Ciudad ciudad) {
        modelSemaforos.setRowCount(0);

        for (Semaforo semaforo : ciudad.getSemaforos()) {
            Interseccion inter = semaforo.getInterseccion();

            // ✅ CONVERTIR boolean a texto legible
            String direccionesPermitidas;
            if (semaforo.isDireccionHorizontal()) {
                direccionesPermitidas = "DERECHA/IZQUIERDA";
            } else {
                direccionesPermitidas = "ARRIBA/ABAJO";
            }

            Object[] fila = {
                    String.format("(%d,%d)", inter.getX(), inter.getY()),
                    semaforo.getEstadoActual().toString(),
                    direccionesPermitidas, // ✅ TEXTO LEGIBLE en lugar de true/false
                    inter.getVehiculosEnEspera(),
                    String.format("%.1fs", inter.getTiempoPromedioEspera() / 1000.0)
            };
            modelSemaforos.addRow(fila);
        }
    }

    private void actualizarTablaMetricas(Ciudad ciudad) {
        modelMetricas.setRowCount(0);

        StatsManager stats = ciudad.getStatsManager();

        Object[][] metricas = {
                {"Tiempo Promedio Viaje", String.format("%.2f", stats.getTiempoPromedioViaje()), "segundos", "Tiempo promedio de viaje por vehículo"},
                {"Tiempo Max Espera", String.format("%.2f", stats.getTiempoMaximoEspera()), "segundos", "Tiempo máximo de espera en un vehículo"},
                {"Eficiencia Semáforos", String.format("%.1f", stats.getEficienciaSemaforos()), "%", "Porcentaje de tiempo útil de semáforos"},
                {"Velocidad Promedio", String.format("%.2f", stats.getVelocidadPromedio()), "celdas/s", "Velocidad promedio de vehículos"},
                {"Colisiones Evitadas", stats.getColisionesEvitadas(), "eventos", "Número de colisiones prevenidas por sincronización"},
                {"Uso CPU Paralelo", String.format("%.1f", stats.getPorcentajeMejoraParalelo()), "%", "Mejora usando planificación paralela"}
        };

        for (Object[] metrica : metricas) {
            modelMetricas.addRow(metrica);
        }
    }

    /**
     * Muestra el resumen final de la simulación
     */
    public void mostrarResumenFinal(StatsManager stats) {
        tabbedPane.setSelectedIndex(0); // Cambiar a pestaña de resumen

        // Actualizar con datos finales
        actualizarResumen(stats.getCiudad());

        // Mostrar mensaje de resumen
        JOptionPane.showMessageDialog(this,
                stats.generarResumenCompleto(),
                "🏁 Simulación Completada",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Limpia todas las estadísticas
     */
    public void limpiarEstadisticas() {
        modelVehiculos.setRowCount(0);
        modelSemaforos.setRowCount(0);
        modelMetricas.setRowCount(0);

        lblTiempoTotal.setText("0s");
        lblVehiculosActivos.setText("0");
        lblVehiculosCompletados.setText("0");
        lblCongestionTotal.setText("0");
        lblPrimerVehiculo.setText("N/A");
        lblUltimoVehiculo.setText("N/A");
    }
}
