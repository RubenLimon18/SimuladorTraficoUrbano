package interfaz;

import clases.*;
import configuracion.SimulacionConfiguracion;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Panel que visualiza la ciudad, calles, intersecciones, vehículos y semáforos
 */
public class CityPanel extends JPanel {
    private Ciudad ciudad;
    private SimulacionConfiguracion config;

    // Constantes de dibujo - MANTENEMOS TAMAÑO ORIGINAL
    private static final int MARGIN = 50;
    private static final int TAMANO_CELDA = 60;
    private static final int ANCHO_CALLE = 4;
    private static final int RADIO_INTERSECCION = 8;
    private static final int RADIO_VEHICULO = 6;
    private static final int RADIO_SEMAFORO = 5;

    // Colores
    private static final Color COLOR_CALLE = new Color(200, 200, 200);
    private static final Color COLOR_CALLE_PRINCIPAL = new Color(150, 150, 150);
    private static final Color COLOR_FONDO = new Color(240, 240, 240);
    private static final Color COLOR_INTERSECCION = new Color(100, 100, 100);
    private static final Color COLOR_TEXTO = Color.BLACK;

    // Cache para posiciones calculadas
    private Map<Interseccion, Point> posicionesCache;

    public CityPanel(Ciudad ciudad, SimulacionConfiguracion config) {
        this.ciudad = ciudad;
        this.config = config;
        this.posicionesCache = new HashMap<>();

        setBackground(COLOR_FONDO);
        setPreferredSize(calcularTamanoPreferido());
        calcularPosicionesCache();
    }

    private Dimension calcularTamanoPreferido() {
        int ancho = ciudad.getAncho() * TAMANO_CELDA + 2 * MARGIN;
        int alto = ciudad.getAlto() * TAMANO_CELDA + 2 * MARGIN;
        return new Dimension(ancho, alto);
    }

    private void calcularPosicionesCache() {
        posicionesCache.clear();
        for (int x = 0; x < ciudad.getAncho(); x++) {
            for (int y = 0; y < ciudad.getAlto(); y++) {
                Interseccion inter = ciudad.getInterseccion(x, y);
                Point punto = new Point(
                        MARGIN + x * TAMANO_CELDA,
                        MARGIN + y * TAMANO_CELDA
                );
                posicionesCache.put(inter, punto);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Configurar calidad de renderizado
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        dibujarCalles(g2d);
        dibujarIntersecciones(g2d);
        dibujarSemaforos(g2d);
        dibujarVehiculos(g2d);
        dibujarInformacionAdicional(g2d);
    }

    private void dibujarCalles(Graphics2D g2d) {
        g2d.setColor(COLOR_CALLE);
        g2d.setStroke(new BasicStroke(ANCHO_CALLE));

        // Dibujar calles horizontales
        for (int y = 0; y <= ciudad.getAlto(); y++) {
            int yPixel = MARGIN + y * TAMANO_CELDA;
            g2d.drawLine(MARGIN, yPixel, MARGIN + ciudad.getAncho() * TAMANO_CELDA, yPixel);
        }

        // Dibujar calles verticales
        for (int x = 0; x <= ciudad.getAncho(); x++) {
            int xPixel = MARGIN + x * TAMANO_CELDA;
            g2d.drawLine(xPixel, MARGIN, xPixel, MARGIN + ciudad.getAlto() * TAMANO_CELDA);
        }

        // Dibujar calles principales más oscuras
        g2d.setColor(COLOR_CALLE_PRINCIPAL);
        g2d.setStroke(new BasicStroke(ANCHO_CALLE + 2));

        // Calles principales cada 3 calles
        for (int y = 0; y <= ciudad.getAlto(); y += 3) {
            int yPixel = MARGIN + y * TAMANO_CELDA;
            g2d.drawLine(MARGIN, yPixel, MARGIN + ciudad.getAncho() * TAMANO_CELDA, yPixel);
        }

        for (int x = 0; x <= ciudad.getAncho(); x += 3) {
            int xPixel = MARGIN + x * TAMANO_CELDA;
            g2d.drawLine(xPixel, MARGIN, xPixel, MARGIN + ciudad.getAlto() * TAMANO_CELDA);
        }
    }

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

    private void dibujarSemaforos(Graphics2D g2d) {
        for (Semaforo semaforo : ciudad.getSemaforos()) {
            Interseccion inter = semaforo.getInterseccion();
            Point punto = posicionesCache.get(inter);

            if (punto != null) {
                Color colorSemaforo = semaforo.getColorVisual();
                g2d.setColor(colorSemaforo);

                // Dibujar semáforo como un círculo más grande
                g2d.fillOval(
                        punto.x - RADIO_SEMAFORO,
                        punto.y - RADIO_SEMAFORO,
                        RADIO_SEMAFORO * 2,
                        RADIO_SEMAFORO * 2
                );

                // Borde negro
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

    private void dibujarVehiculos(Graphics2D g2d) {
        try {
            List<Vehiculo> vehiculos = ciudad.getVehiculos();

            int vehiculosDibujados = 0;
            int vehiculosLlegados = 0;
            int vehiculosConProblemas = 0;

            for (Vehiculo vehiculo : vehiculos) {
                if (vehiculo == null) {
                    System.out.println("❌ Vehículo nulo en la lista");
                    continue;
                }

                if (vehiculo.haLlegado()) {
                    vehiculosLlegados++;
                    System.out.println("⏭️ Saltando vehículo " + vehiculo.getIdVehiculo() + " - ya llegó");
                    continue;
                }

                Point punto = obtenerPosicionVehiculo(vehiculo);
                if (punto != null && punto.x >= 0 && punto.y >= 0) {
                    System.out.println("🚗 Dibujando vehículo " + vehiculo.getIdVehiculo() +
                            " en posición: " + punto +
                            " | Intersección: " + vehiculo.getInterseccionActual());

                    // Color según el estado del vehículo
                    if (vehiculo.estaEsperando()) {
                        g2d.setColor(Color.ORANGE); // Esperando
                    } else if (vehiculo.estaEnInterseccion()) {
                        g2d.setColor(Color.RED); // En intersección
                    } else {
                        g2d.setColor(Color.BLUE); // Moviéndose
                    }

                    g2d.setColor(Color.BLUE);

                    g2d.fillOval(
                            punto.x - RADIO_VEHICULO,
                            punto.y - RADIO_VEHICULO,
                            RADIO_VEHICULO * 2,
                            RADIO_VEHICULO * 2
                    );

                    // Borde negro
                    /*g2d.setColor(Color.BLACK);
                    g2d.drawOval(
                            punto.x - RADIO_VEHICULO,
                            punto.y - RADIO_VEHICULO,
                            RADIO_VEHICULO * 2,
                            RADIO_VEHICULO * 2
                    );*/

                    // ID del vehículo
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
                    vehiculosConProblemas++;
                    System.out.println("❌ Problema con vehículo " + vehiculo.getIdVehiculo() +
                            " - punto: " + punto +
                            " - intersección: " + vehiculo.getInterseccionActual());
                }
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR en dibujarVehiculos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Point obtenerPosicionVehiculo(Vehiculo vehiculo) {
        Interseccion inter = vehiculo.getInterseccionActual();
        if (inter != null) {
            Point punto = posicionesCache.get(inter);
            if (punto == null) {
                System.out.println("❌ No se encontró punto en cache para: " + inter);
            }
            return punto;
        } else {
            System.out.println("❌ Vehículo " + vehiculo.getIdVehiculo() + " tiene intersección actual NULL");
        }
        return null;
    }

    private void dibujarInformacionAdicional(Graphics2D g2d) {
        g2d.setColor(COLOR_TEXTO);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        // Dibujar coordenadas en los bordes
        for (int x = 0; x < ciudad.getAncho(); x++) {
            int xPixel = MARGIN + x * TAMANO_CELDA;
            g2d.drawString(String.valueOf(x), xPixel - 5, MARGIN - 10);
            g2d.drawString(String.valueOf(x), xPixel - 5, MARGIN + ciudad.getAlto() * TAMANO_CELDA + 15);
        }

        for (int y = 0; y < ciudad.getAlto(); y++) {
            int yPixel = MARGIN + y * TAMANO_CELDA;
            g2d.drawString(String.valueOf(y), MARGIN - 15, yPixel + 5);
            g2d.drawString(String.valueOf(y), MARGIN + ciudad.getAncho() * TAMANO_CELDA + 10, yPixel + 5);
        }

        // Información de estado de la simulación
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.DARK_GRAY);

        String estado = "Vehículos: " + ciudad.getVehiculosActivos() + " / " + ciudad.getVehiculos().size() +
                " | Semáforos: " + ciudad.getSemaforos().size() +
                " | Tamaño: " + ciudad.getAncho() + "x" + ciudad.getAlto();

        g2d.drawString(estado, MARGIN, 20);
    }

    /**
     * Actualiza la ciudad visualizada
     */
    public void actualizarCiudad(Ciudad nuevaCiudad) {
        this.ciudad = nuevaCiudad;
        calcularPosicionesCache();
        setPreferredSize(calcularTamanoPreferido());
        revalidate();
        repaint();
    }

    /**
     * Obtiene la intersección en una posición de pixel
     */
    public Interseccion getInterseccionEnPosicion(Point punto) {
        for (Map.Entry<Interseccion, Point> entry : posicionesCache.entrySet()) {
            Point pos = entry.getValue();
            double distancia = punto.distance(pos);
            if (distancia <= RADIO_INTERSECCION) {
                return entry.getKey();
            }
        }
        return null;
    }
}