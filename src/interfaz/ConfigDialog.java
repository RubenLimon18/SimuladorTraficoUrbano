package interfaz;

import configuracion.SimulacionConfiguracion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Diálogo de configuración para modificar parámetros de la simulación
 */
public class ConfigDialog extends JDialog {
    private SimulacionConfiguracion config;
    private boolean configuracionActualizada = false;

    // Componentes de configuración
    private JSpinner spnTamanoCiudad;
    private JSpinner spnNumVehiculos;
    private JSpinner spnSemaforoVerde;
    private JSpinner spnSemaforoAmarillo;
    private JSpinner spnSemaforoRojo;
    private JSpinner spnVelocidadSimulacion;

    // Botones
    private JButton btnAplicar;
    private JButton btnCancelar;
    private JButton btnRestaurar;

    public ConfigDialog(Frame parent, SimulacionConfiguracion config) {
        super(parent, "Configuración de Simulación", true);
        this.config = config;

        inicializarComponentes();
        configurarLayout();
        configurarEventos();
        cargarConfiguracionActual();

        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void inicializarComponentes() {
        // Spinners con valores mínimos y máximos
        spnTamanoCiudad = new JSpinner(new SpinnerNumberModel(
                config.getCiudadTamanio(), 4, 20, 1));
        spnTamanoCiudad.setToolTipText("Tamaño de la rejilla de la ciudad (n x n)");

        spnNumVehiculos = new JSpinner(new SpinnerNumberModel(
                config.getNumVehiculos(), 20, 200, 5));
        spnNumVehiculos.setToolTipText("Número total de vehículos en la simulación");

        spnSemaforoVerde = new JSpinner(new SpinnerNumberModel(
                config.getSemaforoVerde(), 1, 30, 1));
        spnSemaforoVerde.setToolTipText("Duración de la luz verde en segundos");

        spnSemaforoAmarillo = new JSpinner(new SpinnerNumberModel(
                config.getSemaforoAmarillo(), 1, 10, 1));
        spnSemaforoAmarillo.setToolTipText("Duración de la luz amarilla en segundos");

        spnSemaforoRojo = new JSpinner(new SpinnerNumberModel(
                config.getSemaforoRojo(), 1, 30, 1));
        spnSemaforoRojo.setToolTipText("Duración de la luz roja en segundos");

        spnVelocidadSimulacion = new JSpinner(new SpinnerNumberModel(
                config.getVelocidadSimulacion(), 10, 5000, 100));
        spnVelocidadSimulacion.setToolTipText("Velocidad de la simulación (ms entre actualizaciones)");

        // Botones
        btnAplicar = new JButton("Aplicar");
        btnAplicar.setBackground(new Color(76, 175, 80));
        btnAplicar.setForeground(Color.BLACK);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(new Color(244, 67, 54));
        btnCancelar.setForeground(Color.BLACK);

        btnRestaurar = new JButton("Restaurar Valores por Defecto");
        btnRestaurar.setBackground(new Color(33, 150, 243));
        btnRestaurar.setForeground(Color.BLACK);
    }

    private void configurarLayout() {
        JPanel contentPane = (JPanel) this.getContentPane();
        contentPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPane.setLayout(new BorderLayout(10, 10));

        // Panel principal de configuración
        JPanel panelConfig = new JPanel(new GridLayout(6, 2, 10, 10));

        // Ciudad
        panelConfig.add(crearEtiquetaConfig("Tamaño Ciudad:"));
        panelConfig.add(spnTamanoCiudad);

        panelConfig.add(crearEtiquetaConfig("Número de Vehículos:"));
        panelConfig.add(spnNumVehiculos);

        // Semáforos
        panelConfig.add(crearEtiquetaConfig("Semáforo - Verde (s):"));
        panelConfig.add(spnSemaforoVerde);

        panelConfig.add(crearEtiquetaConfig("Semáforo - Amarillo (s):"));
        panelConfig.add(spnSemaforoAmarillo);

        panelConfig.add(crearEtiquetaConfig("Semáforo - Rojo (s):"));
        panelConfig.add(spnSemaforoRojo);

        // Simulación
        panelConfig.add(crearEtiquetaConfig("Velocidad Simulación (ms):"));
        panelConfig.add(spnVelocidadSimulacion);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.add(btnRestaurar);
        panelBotones.add(btnAplicar);
        panelBotones.add(btnCancelar);

        add(panelConfig, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private JLabel crearEtiquetaConfig(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        return label;
    }

    private void configurarEventos() {
        // Botón Aplicar
        btnAplicar.addActionListener((ActionEvent e) -> {
            aplicarConfiguracion();
        });

        // Botón Cancelar
        btnCancelar.addActionListener((ActionEvent e) -> {
            configuracionActualizada = false;
            dispose();
        });

        // Botón Restaurar
        btnRestaurar.addActionListener((ActionEvent e) -> {
            restaurarValoresPorDefecto();
        });

        // Enter en spinners aplica la configuración
        spnTamanoCiudad.addChangeListener(e -> {
            // Validar que el número de vehículos no sea mayor que el máximo posible
            int maxVehiculos = (int) spnTamanoCiudad.getValue() * (int) spnTamanoCiudad.getValue() * 2;
            ((SpinnerNumberModel) spnNumVehiculos.getModel()).setMaximum(maxVehiculos);
        });
    }

    private void cargarConfiguracionActual() {
        spnTamanoCiudad.setValue(config.getCiudadTamanio());
        spnNumVehiculos.setValue(config.getNumVehiculos());
        spnSemaforoVerde.setValue(config.getSemaforoVerde());
        spnSemaforoAmarillo.setValue(config.getSemaforoAmarillo());
        spnSemaforoRojo.setValue(config.getSemaforoRojo());
        spnVelocidadSimulacion.setValue(config.getVelocidadSimulacion());
    }

    private void aplicarConfiguracion() {
        try {
            // Validaciones
            int tamano = (int) spnTamanoCiudad.getValue();
            int vehiculos = (int) spnNumVehiculos.getValue();
            int verde = (int) spnSemaforoVerde.getValue();
            int amarillo = (int) spnSemaforoAmarillo.getValue();
            int rojo = (int) spnSemaforoRojo.getValue();
            int velocidad = (int) spnVelocidadSimulacion.getValue();

            // Validar que los tiempos de semáforo sean razonables
            if (verde < 1 || amarillo < 1 || rojo < 1) {
                JOptionPane.showMessageDialog(this,
                        "Los tiempos de semáforo deben ser al menos 1 segundo",
                        "Error de Validación",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (vehiculos > tamano * tamano * 2) {
                JOptionPane.showMessageDialog(this,
                        "Demasiados vehículos para el tamaño de ciudad seleccionado",
                        "Error de Validación",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Aplicar cambios
            config.setCiudadTamano(tamano);
            config.setNumVehiculos(vehiculos);
            config.setSemaforoVerde(verde);
            config.setSemaforoAmarillo(amarillo);
            config.setSemaforoRojo(rojo);
            config.setVelocidadSimulacion(velocidad);

            configuracionActualizada = true;
            config.guardarConfiguracion();

            JOptionPane.showMessageDialog(this,
                    "Configuración aplicada correctamente.\nLa simulación se reiniciará con los nuevos parámetros.",
                    "Configuración Aplicada",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al aplicar configuración: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restaurarValoresPorDefecto() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Restaurar todos los valores por defecto?\nSe perderán los cambios no guardados.",
                "Confirmar Restauración",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Valores por defecto
            spnTamanoCiudad.setValue(12);
            spnNumVehiculos.setValue(50);
            spnSemaforoVerde.setValue(5);
            spnSemaforoAmarillo.setValue(2);
            spnSemaforoRojo.setValue(6);
            spnVelocidadSimulacion.setValue(1000);

            JOptionPane.showMessageDialog(this,
                    "Valores por defecto restaurados.\nRecuerda hacer clic en 'Aplicar' para guardar los cambios.",
                    "Valores Restaurados",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public boolean isConfiguracionActualizada() {
        return configuracionActualizada;
    }

    // Getters para testing
//    public JSpinner getSpnTamanoCiudad() { return spnTamanoCiudad; }
//    public JSpinner getSpnNumVehiculos() { return spnNumVehiculos; }
//    public JButton getBtnAplicar() { return btnAplicar; }
}
