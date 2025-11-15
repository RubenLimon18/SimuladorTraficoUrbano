//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import interfaz.MainFrame;
import javax.swing.*;

/**
 * Clase principal para ejecutar la simulación de tráfico
 */
public class Main {
    public static void main(String[] args) {
        // Establecer el look and feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Mejorar la apariencia en sistemas Windows
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                UIManager.put("Button.arc", 10);
                UIManager.put("Component.arc", 10);
                UIManager.put("ProgressBar.arc", 10);
                UIManager.put("TextComponent.arc", 5);
            }

        } catch (Exception e) {
            System.err.println("Error al establecer el look and feel: " + e.getMessage());
        }

        // Ejecutar la aplicación en el EDT
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al iniciar la aplicación: " + e.getMessage(),
                        "Error Crítico",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}
