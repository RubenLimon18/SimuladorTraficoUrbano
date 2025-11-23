package interfaz;

import clases.*;
import configuracion.SimulacionConfiguracion;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Panel encargado de dibujar toda la ciudad: intersecciones, calles,
 * vehículos y semáforos. Aquí es donde se procesa toda la parte visual.
 */
public class CityPanel extends JPanel {

    private Ciudad ciudad;
    private SimulacionConfiguracion config;

    // Constantes de dibujo (mantengo los tamaños originales porque funcionan bien visualmente)
    private static final int MARGIN = 50;
    private static final int TAMANO_CELDA = 60;
    private static final int ANCHO_CALLE = 4;
    private static final int RADIO_INTERSECCION = 8;
    private static final int RADIO_VEHICULO = 6;
    private static final int RADIO_SEMAFORO = 5;

    // Colores predefinidos para uniformidad visual
    private static final Color COLOR_CALLE = new Color(200, 200, 200);
    private static final Color COLOR_CALLE_PRINCIPAL = new Color(150, 150, 150);
    private static final Color COLOR_FONDO = new Color(240, 240, 240);
    private static final Color COLOR_INTERSECCION = new Color(100, 100, 100);
    private static final Color COLOR_TEXTO = Color.BLACK;

    // Cache donde guardo las posiciones de cada intersección para no recalcular en cada frame
    private Map<Interseccion, Point> posicionesCache;

    public CityPanel(Ciudad ciudad, SimulacionConfiguracion config) {
        this.ciudad = ciudad;
        this.config = config;
        this.posicionesCache = new HashMap<>();

        // Fondo del panel
        setBackground(COLOR_FONDO);

        // El tamaño depende del número de celdas
        setPreferredSize(calcularTamanoPreferido());

        // Precalculo todas las posiciones de la ciudad
        calcularPosicionesCache();
    }

    // Determino el tamaño que debe tener el panel según el tamaño de la ciudad
    private Dimension calcularTamanoPreferido() {
        int ancho = ciudad.getAncho() * TAMANO_CELDA + 2 * MARGIN;
        int alto = ciudad.getAlto() * TAMANO_CELDA + 2 * MARGIN;
        return new Dimension(ancho, alto);
    }

    // Calculo la posición en píxeles de cada intersección solo una vez
    private void calcularPosicionesCache() {
        posicionesCache.clear();
        for (int x = 0; x < ciudad.getAncho(); x++) {
            for (int y = 0; y < ciudad.getAlto(); y++) {
                Interseccion inter = ciudad.getInterseccion(x, y);

                // Mapeo la intersección a su posición en la pantalla
                Point punto = new Point(
                        MARGIN + x * TAMANO_CELDA,
                        MARGIN + y * TAMANO_CELDA
                );
                posicionesCache.put(inter, punto);
            }
        }
    }

    // Método principal de dibujo (Swing llama a esto automáticamente)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Activar suavizado para que todo se vea más limpio
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Llamo a cada parte del dibujo por separado para mantener el orden y claridad
        dibujarCalles(g2d);
        dibujarIntersecciones(g2d);
        dibujarSemaforos(g2d);
        dibujarVehiculos(g2d);
        dibujarInformacionAdicional(g2d);
    }

    // Dibujo toda la malla de calles horizontales y verticales
    private void dibujarCalles(Graphics2D g2d) {
        g2d.setColor(COLOR_CALLE);
        g2d.setStroke(new BasicStroke(ANCHO_CALLE));

        // Líneas horizontales
        for (int y = 0; y <= ciudad.getAlto(); y++) {
            int yPixel = MARGIN + y * TAMANO_CELDA;
            g2d.drawLine(MARGIN, yPixel, MARGIN + ciudad.getAncho() * TAMANO_CELDA, yPixel);
        }

        // Líneas verticales
        for (int x = 0; x <= ciudad.getAncho(); x++) {
            int xPixel = MARGIN + x * TAMANO_CELDA;
            g2d.drawLine(xPixel, MARGIN, xPixel, MARGIN + ciudad.getAlto() * TAMANO_CELDA);
        }

        // Calles principales (cada 3 bloques)
        g2d.setColor(COLOR_CALLE_PRINCIPAL);
        g2d.setStroke(new BasicStroke(ANCHO_CALLE + 2));

        for (int y = 0; y <= ciudad.getAlto(); y += 3) {
            int yPixel = MARGIN + y * TAMANO_CELDA;
            g2d.drawLine(MARGIN, yPixel, MARGIN + ciudad.getAncho() * TAMANO_CELDA, yPixel);
        }

        for (int x = 0; x <= ciudad.getAncho(); x += 3) {
            int xPixel = MARGIN + x * TAMANO_CELDA;
            g2d.drawLine(xPixel, MARGIN, xPixel, MARGIN + ciudad.getAlto() * TAMANO_CELDA);
        }
    }

    // Dibujo cada intersección como un pequeño círculo
    private void dibujarIntersecciones(Graphics2D g2d) {
        g2d.setColor(COLOR_INTERSECCION);

        for (Point punto : posicionesCache.values()) {
            g2d.fillOval(
                    punto.x - RADIO_INTERSECCION / 2,
                    punto.y - RADIO_INTERSECCION / 2,
                    RADIO_INTERSECCION,
                    RADIO_INTERSECCION
            );
        }
    }

    // Dibujo todos los semáforos de la ciudad
    private void dibujarSemaforos(Graphics2D g2d) {
        for (Semaforo semaforo : ciudad.getSemaforos()) {
            Interseccion inter = semaforo.getInterseccion();
            Point punto = posicionesCache.get(inter);

            if (punto != null) {
                // Uso el color del semáforo según su estado
                Color colorSemaforo = semaforo.getColorVisual();
                g2d.setColor(colorSemaforo);

                // Círculo más grande para distinguirlos de las intersecciones
                g2d.fillOval(
                        punto.x - RADIO_SEMAFORO,
                        punto.y - RADIO_SEMAFORO,
                        RADIO_SEMAFORO * 2,
                        RADIO_SEMAFORO * 2
                );

                // Borde para contraste
                g2d.setColor(Color.BLACK);
                g2d.drawOval(
                        punto.x - RADIO_SEMAFORO,
                        punto.y - RADIO_SEMAFORO,
                        RADIO_SEMAFORO * 2,
                        RADIO_SEMAFORO * 2
                );
            }
        }
    }

    // Dibujo todos los vehículos según su estado actual
    private void dibujarVehiculos(Graphics2D g2d) {
        try {
            List<Vehiculo> vehiculos = ciudad.getVehiculos();

            // Contadores que uso para debug
            int vehiculosDibujados = 0;
            int vehiculosLlegados = 0;
            int vehiculosConProblemas = 0;

            for (Vehiculo vehiculo : vehiculos) {
                if (vehiculo == null) {
                    System.out.println("Vehículo nulo en la lista");
                    continue;
                }

                // Si ya llegó, no lo dibujo
                if (vehiculo.haLlegado()) {
                    vehiculosLlegados++;
                    continue;
                }

                Point punto = obtenerPosicionVehiculo(vehiculo);

                if (punto != null && punto.x >= 0 && punto.y >= 0) {

                    // Color según estado del vehículo
                    if (vehiculo.estaEsperando()) {
                        g2d.setColor(Color.ORANGE);
                    } else if (vehiculo.estaEnInterseccion()) {
                        g2d.setColor(Color.RED);
                    } else {
                        g2d.setColor(Color.BLUE);
                    }

                    // Dibujo del vehículo (círculo)
                    g2d.fillOval(
                            punto.x - RADIO_VEHICULO,
                            punto.y - RADIO_VEHICULO,
                            RADIO_VEHICULO * 2,
                            RADIO_VEHICULO * 2
                    );

                    // Si hay pocos vehículos, le pongo su ID encima
                    if (config.getNumVehiculos() <= 30) {
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Arial", Font.BOLD, 8));

                        String id = String.valueOf(vehiculo.getIdVehiculo());
                        FontMetrics fm = g2d.getFontMetrics();
                        int textWidth = fm.stringWidth(id);

                        g2d.drawString(id, punto.x - textWidth / 2, punto.y + 3);
                    }

                    vehiculosDibujados++;

                } else {

                    // Algo salió mal, lo registro
                    vehiculosConProblemas++;
                }
            }

        } catch (Exception e) {
            System.err.println("ERROR en dibujarVehiculos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Devuelvo la posición del vehículo según la intersección donde está
    private Point obtenerPosicionVehiculo(Vehiculo vehiculo) {
        Interseccion inter = vehiculo.getInterseccionActual();

        // Si no tiene intersección, no lo puedo dibujar
        if (inter != null) {
            return posicionesCache.get(inter);
        }
        return null;
    }

    // Dibujo números, coordenadas y resumen del estado de la simulación
    private void dibujarInformacionAdicional(Graphics2D g2d) {
        g2d.setColor(COLOR_TEXTO);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        // Coordenadas arriba y abajo
        for (int x = 0; x < ciudad.getAncho(); x++) {
            int xPixel = MARGIN + x * TAMANO_CELDA;
            g2d.drawString(String.valueOf(x), xPixel - 5, MARGIN - 10);
            g2d.drawString(String.valueOf(x), xPixel - 5,
                    MARGIN + ciudad.getAlto() * TAMANO_CELDA + 15);
        }

        // Coordenadas izquierda y derecha
        for (int y = 0; y < ciudad.getAlto(); y++) {
            int yPixel = MARGIN + y * TAMANO_CELDA;
            g2d.drawString(String.valueOf(y), MARGIN - 15, yPixel + 5);
            g2d.drawString(String.valueOf(y),
                    MARGIN + ciudad.getAncho() * TAMANO_CELDA + 10, yPixel + 5);
        }

        // Información global de la simulación
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.DARK_GRAY);

        String estado = "Vehículos: " + ciudad.getVehiculosActivos() + " / "
                + ciudad.getVehiculos().size()
                + " | Semáforos: " + ciudad.getSemaforos().size()
                + " | Tamaño: " + ciudad.getAncho() + "x" + ciudad.getAlto();

        g2d.drawString(estado, MARGIN, 20);
    }

    /**
     * Método para actualizar la ciudad cuando se reinicia la simulación
     * o cambia la configuración. Redibujo y actualizo todo.
     */
    public void actualizarCiudad(Ciudad nuevaCiudad) {
        this.ciudad = nuevaCiudad;
        calcularPosicionesCache();
        setPreferredSize(calcularTamanoPreferido());
        revalidate();
        repaint();
    }

}
