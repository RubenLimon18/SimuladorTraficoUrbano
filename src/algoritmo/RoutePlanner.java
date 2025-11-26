package algoritmo;

import clases.Interseccion;
import clases.Ciudad;
import clases.Direccion;
import java.util.*;

/**
 * RoutePlanner
 * Clase encargada de calcular rutas dentro de una ciudad utilizando
 * A* como algoritmo principal y un método simple como respaldo.
 */
public class RoutePlanner {
    private Ciudad ciudad; // Ciudad sobre la que se calcularan las rutas
    private Random random; // Se usa para generar decisiones aleatorias en rutas simples

    public RoutePlanner(Ciudad ciudad) {
        this.ciudad = ciudad;
        this.random = new Random();
    }

    /**
     * Calcula la ruta desde una intersección inicial hasta una de destino.
     * Utiliza A*, y si falla usa una ruta simple como alternativa.
     */
    public List<Interseccion> calcularRuta(Interseccion inicio, Interseccion destino) {
        System.out.println("Calculando ruta desde " + inicio + " hasta " + destino);

        // Intentar A*
        List<Interseccion> ruta = aStar(inicio, destino);

        // Si A* no encuentra ruta, usar método simple
        if (ruta == null || ruta.isEmpty()) {
            ruta = calcularRutaSimple(inicio, destino);
        }

        System.out.println("Ruta calculada con " + ruta.size() + " intersecciones");
        return ruta;
    }

    /**
     * Implementación del algoritmo A* para encontrar la ruta óptima.
     */
    private List<Interseccion> aStar(Interseccion inicio, Interseccion destino) {

        // Guarda de qué intersección proviene cada nodo (para reconstruir la ruta)
        Map<Interseccion, Interseccion> cameFrom = new HashMap<>();

        // gScore = coste desde el inicio hasta el nodo actual
        Map<Interseccion, Double> gScore = new HashMap<>();

        // fScore = gScore + heurística (lo que falta hasta el destino)
        Map<Interseccion, Double> fScore = new HashMap<>();

        // PriorityQueue ordenada por menor fScore
        PriorityQueue<Interseccion> openSet = new PriorityQueue<>(
                (a, b) -> Double.compare(
                        fScore.getOrDefault(a, Double.MAX_VALUE),
                        fScore.getOrDefault(b, Double.MAX_VALUE)
                )
        );


        // Conjunto cerrado (nodos ya evaluados)
        Set<Interseccion> closedSet = new HashSet<>();

        // Inicializar valores del nodo inicial
        gScore.put(inicio, 0.0);
        fScore.put(inicio, heuristic(inicio, destino));
        openSet.add(inicio);

        while (!openSet.isEmpty()) {
            // Se toma la intersección con menor fScore
            Interseccion current = openSet.poll();

            // Si llegamos al destino, reconstruimos la ruta
            if (current.equals(destino)) {
                return reconstruirRuta(cameFrom, current);
            }


            // Marcamos como visitado
            closedSet.add(current);

            // Explorar vecinos
            for (Interseccion neighbor : getVecinosValidos(current)) {

                // Saltar vecinos ya evaluados
                if (closedSet.contains(neighbor)) {
                    continue; // Ignorar si ya se procesó
                }

                // Costo tentativo desde el inicio hasta el vecino
                double tentativeGScore = gScore.get(current) + calcularCosto(current, neighbor);

                // Si es un mejor camino, actualizar
                if (!openSet.contains(neighbor) ||
                    tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {

                    cameFrom.put(neighbor, current);

                    // Actualizamos los costes
                    gScore.put(neighbor, tentativeGScore);

                    // fScore = costo recorrido + heurística al destino
                    fScore.put(neighbor, tentativeGScore + heuristic(neighbor, destino));

                    // Si es la primera vez que vemos al vecino, lo añadimos a openSet
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }

                }
            }
        }

        return null; // No se encontró ruta
    }

    /**
     * Calcula el costo entre dos intersecciones.
     * Considera:
     *  - Costo base fijo.
     *  - Congestión en la intersección.
     *  - Tiempo estimado por semáforo.
     */
    private double calcularCosto(Interseccion desde, Interseccion hacia) {
        double costoBase = 1.0;

        // Costo adicional por congestión (vehículos esperando)
        double costoCcongestion = hacia.getVehiculosEnEspera() * 2.0;

        // Costo estimado por semáforo
        double costoSemaforo = 0.5;

        // El coste total afecta qué rutas prefiere A*
        return costoBase + costoCcongestion + costoSemaforo;
    }

    /**
     * Obtiene los vecinos válidos en las 4 direcciones cardinales.
     */
    private List<Interseccion> getVecinosValidos(Interseccion actual) {
        List<Interseccion> vecinos = new ArrayList<>();
        int x = actual.getX();
        int y = actual.getY();

        // Izquierda
        if (x > 0) {
            Interseccion vecino = ciudad.getInterseccion(x - 1, y);
            if (vecino != null) vecinos.add(vecino);
        }

        // Derecha
        if (x < ciudad.getAncho() - 1) {
            Interseccion vecino = ciudad.getInterseccion(x + 1, y);
            if (vecino != null) vecinos.add(vecino);
        }

        // Arriba
        if (y > 0) {
            Interseccion vecino = ciudad.getInterseccion(x, y - 1);
            if (vecino != null) vecinos.add(vecino);
        }

        // Abajo
        if (y < ciudad.getAlto() - 1) {
            Interseccion vecino = ciudad.getInterseccion(x, y + 1);
            if (vecino != null) vecinos.add(vecino);
        }

        return vecinos;
    }

    /**
     * Función heurística del A* (distancia Manhattan).
     */
    private double heuristic(Interseccion a, Interseccion b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    /**
     * Reconstruye la ruta final a partir del mapa de nodos padre.
     */
    private List<Interseccion> reconstruirRuta(Map<Interseccion, Interseccion> cameFrom, Interseccion current) {
        List<Interseccion> ruta = new LinkedList<>();

        while (current != null) {
            ruta.add(0, current); // Insertar al inicio
            current = cameFrom.get(current);
        }

        return ruta;
    }

    /**
     * A* alternativo para evitar intersecciones específicas.
     * (Implementación simplificada por ahora).
     */
    private List<Interseccion> aStarEvitando(Interseccion inicio, Interseccion destino,
                                             Set<Interseccion> evitar) {
        return aStar(inicio, destino);
    }

    /**
     * Método simple para calcular una ruta cuando A* falla.
     * Avanza de forma determinista o aleatoria hacia el destino.
     */
    private List<Interseccion> calcularRutaSimple(Interseccion inicio, Interseccion destino) {
        List<Interseccion> ruta = new ArrayList<>();
        ruta.add(inicio);

        int xActual = inicio.getX();
        int yActual = inicio.getY();
        int xDestino = destino.getX();
        int yDestino = destino.getY();

        // Límite de seguridad para evitar bucles infinitos
        int maxPasos = 2000; //ciudad.getAncho() * ciudad.getAlto() * 2;
        int pasos = 0;

        // Avanzar hasta llegar o agotar intentos
        while ((xActual != xDestino || yActual != yDestino) && pasos < maxPasos) {
            pasos++;

            // Elegir dirección prioritaria
            if (xActual != xDestino && (random.nextBoolean() || yActual == yDestino)) {
                xActual += (xDestino > xActual) ? 1 : -1;
            } else if (yActual != yDestino) {
                yActual += (yDestino > yActual) ? 1 : -1;
            }

            Interseccion siguiente = ciudad.getInterseccion(xActual, yActual);

            // Evitar ciclos
            if (siguiente != null && !ruta.contains(siguiente)) {
                ruta.add(siguiente);
            } else {
                break;
            }
        }

        if (pasos >= maxPasos) {
            System.out.println("Ruta simple alcanzó límite de pasos");
            System.out.println("Ruta simple alcanzó límite de pasos");
        }

        return ruta;
    }
}
