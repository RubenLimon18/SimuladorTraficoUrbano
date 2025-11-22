package interfaz;

import configuracion.SimulacionConfiguracion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Panel de control con botones para gestionar la simulación
 */
public class ControlPanel extends JPanel {
    private MainFrame mainFrame;
    private SimulacionConfiguracion config;

    // Botones de control
    private JButton btnIniciar;
    private JButton btnPausar;
    private JButton btnDetener;
    private JButton btnConfigurar;
    private JButton btnSalir;

    // Controles de velocidad
    private JSlider sliderVelocidad;
    private JLabel lblVelocidad;

    // Indicadores de estado
    private JLabel lblEstado;
    private JProgressBar progressBar;

    public ControlPanel(MainFrame mainFrame, SimulacionConfiguracion config) {
        this.mainFrame = mainFrame;
        this.config = config;

        inicializarComponentes();
        configurarLayout();
        configurarEventos();
        actualizarEstado("Listo para iniciar");
    }

    private void inicializarComponentes() {
        // Botones principales
        btnIniciar = crearBoton("Iniciar", Color.GREEN, "Iniciar simulación");
        btnPausar = crearBoton("Pausar", Color.YELLOW, "Pausar/Reanudar simulación");
        btnDetener = crearBoton("Detener", Color.RED, "Detener simulación");
        btnConfigurar = crearBoton("⚙ Configurar", Color.CYAN, "Configurar simulación");
        btnSalir = crearBoton("Salir", Color.LIGHT_GRAY, "Salir de la aplicación");

        // Deshabilitar botones inicialmente
        btnPausar.setEnabled(false);
        btnDetener.setEnabled(false);

        // Control de velocidad
        sliderVelocidad = new JSlider(JSlider.HORIZONTAL, 0, 5000, config.getVelocidadSimulacion());
        sliderVelocidad.setMajorTickSpacing(1000);
        sliderVelocidad.setMinorTickSpacing(500);
        sliderVelocidad.setPaintTicks(true);
        sliderVelocidad.setPaintLabels(true);
        sliderVelocidad.setToolTipText("Velocidad de simulación (ms entre actualizaciones)");

        lblVelocidad = new JLabel("Velocidad: " + config.getVelocidadSimulacion() + "ms");

        // Indicadores
        lblEstado = new JLabel("Estado: ");
        lblEstado.setFont(new Font("Arial", Font.BOLD, 12));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
    }

    private JButton crearBoton(String texto, Color color, String tooltip) {
        JButton boton = new JButton(texto);
        boton.setBackground(color);
        boton.setForeground(Color.BLACK);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setToolTipText(tooltip);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createRaisedBevelBorder());
        return boton;
    }

    private void configurarLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 245, 245));

        // Panel de botones principales
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBotones.setBackground(new Color(245, 245, 245));
        panelBotones.add(btnIniciar);
        panelBotones.add(btnPausar);
        panelBotones.add(btnDetener);
        panelBotones.add(btnConfigurar);
        panelBotones.add(btnSalir);

        // Panel de control de velocidad
        JPanel panelVelocidad = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelVelocidad.setBackground(new Color(245, 245, 245));
        panelVelocidad.add(new JLabel("Velocidad Simulación:"));
        panelVelocidad.add(sliderVelocidad);
        panelVelocidad.add(lblVelocidad);

        // Panel de estado
        JPanel panelEstado = new JPanel(new BorderLayout(10, 5));
        panelEstado.setBackground(new Color(245, 245, 245));
        panelEstado.add(lblEstado, BorderLayout.NORTH);
        panelEstado.add(progressBar, BorderLayout.CENTER);

        // Panel principal de controles
        JPanel panelControles = new JPanel(new GridLayout(3, 1, 5, 5));
        panelControles.setBackground(new Color(245, 245, 245));
        panelControles.add(panelBotones);
        panelControles.add(panelVelocidad);
        panelControles.add(panelEstado);

        add(panelControles, BorderLayout.CENTER);
    }

    private void configurarEventos() {
        // Botón Iniciar
        btnIniciar.addActionListener((ActionEvent e) -> {
            mainFrame.iniciarSimulacion();
        });

        // Botón Pausar/Reanudar
        btnPausar.addActionListener((ActionEvent e) -> {
            mainFrame.pausarReanudarSimulacion();
        });

        // Botón Detener
        btnDetener.addActionListener((ActionEvent e) -> {
            int confirm = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "¿Estás seguro de que quieres detener la simulación?",
                    "Confirmar detención",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.detenerSimulacion();
            }
        });

        // Botón Configurar
        btnConfigurar.addActionListener((ActionEvent e) -> {
            mainFrame.abrirConfiguracion();
        });

        // Botón Salir
        btnSalir.addActionListener((ActionEvent e) -> {
            mainFrame.dispatchEvent(new java.awt.event.WindowEvent(
                    mainFrame, java.awt.event.WindowEvent.WINDOW_CLOSING));
        });

        // Slider de velocidad
        sliderVelocidad.addChangeListener(e -> {
            int velocidad = sliderVelocidad.getValue();
            config.setVelocidadSimulacion(velocidad);
            lblVelocidad.setText("Velocidad: " + velocidad + "ms");
        });
    }

    // Métodos para cambiar estado de los botones

    public void simulacionIniciada() {
        btnIniciar.setEnabled(false);
        btnPausar.setEnabled(true);
        btnDetener.setEnabled(true);
        btnConfigurar.setEnabled(false);
        sliderVelocidad.setEnabled(false);

        btnPausar.setText("⏸ Pausar");
        btnPausar.setBackground(Color.YELLOW);

        actualizarEstado("Simulación en ejecución");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
    }

    public void simulacionPausada() {
        btnPausar.setText("▶ Reanudar");
        btnPausar.setBackground(Color.GREEN);
        actualizarEstado("Simulación PAUSADA");
        progressBar.setIndeterminate(false);
        progressBar.setValue(50);
    }

    public void simulacionReanudada() {
        btnPausar.setText("⏸ Pausar");
        btnPausar.setBackground(Color.YELLOW);
        actualizarEstado("Simulación en ejecución");
        progressBar.setIndeterminate(true);
    }

    public void simulacionDetenida() {
        btnIniciar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnDetener.setEnabled(false);
        btnConfigurar.setEnabled(true);
        sliderVelocidad.setEnabled(true);

        btnPausar.setText("⏸ Pausar");
        btnPausar.setBackground(Color.YELLOW);

        actualizarEstado("Simulación finalizada");
        progressBar.setVisible(false);
    }

    public void actualizarEstado(String estado) {
        lblEstado.setText("Estado: " + estado);
    }

}