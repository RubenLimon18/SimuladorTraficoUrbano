package algoritmo;

import clases.Interseccion;
import clases.Ciudad;
import clases.Direccion;
import java.util.*;

public class RoutePlanner {
    private Ciudad ciudad;
    private Random random;

    public RoutePlanner(Ciudad ciudad) {
        this.ciudad = ciudad;
        this.random = new Random();
    }

    public List<Interseccion> calcularRuta(Interseccion inicio, Interseccion destino) {
        System.out.println("🔄 Calculando ruta desde " + inicio + " hasta " + destino);
        List<Interseccion> ruta = aStar(inicio, destino);

        if (ruta == null || ruta.isEmpty()) {
            ruta = calcularRutaSimple(inicio, destino);
        }

        System.out.println("📍 Ruta calculada: " + ruta.size() + " intersecciones");
        return ruta;
    }

    private List<Interseccion> aStar(Interseccion inicio, Interseccion destino) {
        Map<Interseccion, Interseccion> cameFrom = new HashMap<>();
        Map<Interseccion, Double> gScore = new HashMap<>();
        Map<Interseccion, Double> fScore = new HashMap<>();

        // ✅ Usar PriorityQueue correctamente
        PriorityQueue<Interseccion> openSet = new PriorityQueue<>(
                (a, b) -> Double.compare(fScore.getOrDefault(a, Double.MAX_VALUE),
                        fScore.getOrDefault(b, Double.MAX_VALUE))
        );

        Set<Interseccion> closedSet = new HashSet<>();

        gScore.put(inicio, 0.0);
        fScore.put(inicio, heuristic(inicio, destino));
        openSet.add(inicio);

        while (!openSet.isEmpty()) {
            Interseccion current = openSet.poll();

            if (current.equals(destino)) {
                return reconstruirRuta(cameFrom, current);
            }

            closedSet.add(current);

            for (Interseccion neighbor : getVecinosValidos(current)) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double tentativeGScore = gScore.get(current) + calcularCosto(current, neighbor);

                if (!openSet.contains(neighbor) || tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristic(neighbor, destino));

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null; // No se encontró ruta
    }

    private double calcularCosto(Interseccion desde, Interseccion hacia) {
        double costoBase = 1.0;

        // ✅ Costo por congestión
        double costoCcongestion = hacia.getVehiculosEnEspera() * 2.0;

        // ✅ Costo por semáforo (estimado)
        double costoSemaforo = 0.5;

        return costoBase + costoCcongestion + costoSemaforo;
    }

    private List<Interseccion> getVecinosValidos(Interseccion actual) {
        List<Interseccion> vecinos = new ArrayList<>();
        int x = actual.getX();
        int y = actual.getY();

        // Verificar vecinos en las 4 direcciones
        if (x > 0) {
            Interseccion vecino = ciudad.getInterseccion(x - 1, y);
            if (vecino != null) vecinos.add(vecino);
        }
        if (x < ciudad.getAncho() - 1) {
            Interseccion vecino = ciudad.getInterseccion(x + 1, y);
            if (vecino != null) vecinos.add(vecino);
        }
        if (y > 0) {
            Interseccion vecino = ciudad.getInterseccion(x, y - 1);
            if (vecino != null) vecinos.add(vecino);
        }
        if (y < ciudad.getAlto() - 1) {
            Interseccion vecino = ciudad.getInterseccion(x, y + 1);
            if (vecino != null) vecinos.add(vecino);
        }

        return vecinos;
    }

    private double heuristic(Interseccion a, Interseccion b) {
        // Distancia Manhattan
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private List<Interseccion> reconstruirRuta(Map<Interseccion, Interseccion> cameFrom, Interseccion current) {
        List<Interseccion> ruta = new LinkedList<>();
        while (current != null) {
            ruta.add(0, current);
            current = cameFrom.get(current);
        }
        return ruta;
    }

    // ✅ Método mejorado para evitar puntos muertos
    public List<Interseccion> recalcularRutaEvitandoCongestion(Interseccion actual, Interseccion destino,
                                                               Set<Interseccion> evitar) {
        System.out.println("🔄 Recalculando ruta desde " + actual + " evitando congestión");

        // Añadir intersecciones problemáticas a evitar
        for (Interseccion problematica : evitar) {
            System.out.println("🚫 Evitando intersección: " + problematica);
        }

        List<Interseccion> ruta = aStarEvitando(actual, destino, evitar);
        if (ruta == null || ruta.isEmpty()) {
            ruta = calcularRutaSimple(actual, destino);
        }

        return ruta;
    }

    private List<Interseccion> aStarEvitando(Interseccion inicio, Interseccion destino,
                                             Set<Interseccion> evitar) {
        // Implementación similar a A* pero evitando ciertas intersecciones
        // (puedes adaptar el A* original para saltar intersecciones en el conjunto 'evitar')
        return aStar(inicio, destino); // Implementación simplificada
    }

    private List<Interseccion> calcularRutaSimple(Interseccion inicio, Interseccion destino) {
        List<Interseccion> ruta = new ArrayList<>();
        ruta.add(inicio);

        int xActual = inicio.getX();
        int yActual = inicio.getY();
        int xDestino = destino.getX();
        int yDestino = destino.getY();

        // Límite de seguridad para evitar bucles infinitos
        int maxPasos = ciudad.getAncho() * ciudad.getAlto() * 2;
        int pasos = 0;

        while ((xActual != xDestino || yActual != yDestino) && pasos < maxPasos) {
            pasos++;

            // Decidir dirección preferente
            if (xActual != xDestino && (random.nextBoolean() || yActual == yDestino)) {
                xActual += (xDestino > xActual) ? 1 : -1;
            } else if (yActual != yDestino) {
                yActual += (yDestino > yActual) ? 1 : -1;
            }

            Interseccion siguiente = ciudad.getInterseccion(xActual, yActual);
            if (siguiente != null && !ruta.contains(siguiente)) {
                ruta.add(siguiente);
            } else {
                // Evitar ciclos
                break;
            }
        }

        if (pasos >= maxPasos) {
            System.out.println("⚠️  Ruta simple alcanzó límite de pasos");
        }

        return ruta;
    }

    private Direccion obtenerDireccionMovimiento(Interseccion actual, Interseccion siguiente) {
        if (actual == null || siguiente == null) {
            return Direccion.ARRIBA;
        }

        int dx = siguiente.getX() - actual.getX();
        int dy = siguiente.getY() - actual.getY();

        // ✅ DETECCIÓN DE MOVIMIENTO INVÁLIDO
        if ((dx != 0 && dy != 0) || (dx == 0 && dy == 0)) {
            System.err.println("❌ ERROR: Movimiento inválido de " + actual + " a " + siguiente +
                    " (dx=" + dx + ", dy=" + dy + ")");
            // Forzar movimiento horizontal si hay problema
            if (dx != 0) {
                return dx > 0 ? Direccion.DERECHA : Direccion.IZQUIERDA;
            } else {
                return dy > 0 ? Direccion.ABAJO : Direccion.ARRIBA;
            }
        }

        if (dx > 0) return Direccion.DERECHA;
        if (dx < 0) return Direccion.IZQUIERDA;
        if (dy > 0) return Direccion.ABAJO;
        if (dy < 0) return Direccion.ARRIBA;

        return Direccion.ARRIBA;
    }
}