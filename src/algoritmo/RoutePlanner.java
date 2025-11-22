package algoritmo;

import clases.Interseccion;
import clases.Ciudad;
import clases.Direccion;
import java.util.*;

public class RoutePlanner {
    private Ciudad ciudad; // Ciudad sobre la que se calcularan las rutas
    private Random random; // Se usa para generar decisiones aleatorias en rutas simples

    public RoutePlanner(Ciudad ciudad) {
        this.ciudad = ciudad;
        this.random = new Random();
    }


    /*
    * Calcula la ruta entre dos intersecciones usando A*
    * Si A* falla (No hay ruta posible), usa un método simple alternativo
    * */
    public List<Interseccion> calcularRuta(Interseccion inicio, Interseccion destino) {
        System.out.println("Calculando ruta desde " + inicio + " hasta " + destino);

        // Primero intenta usar A* (la búsqueda inteligente)
        List<Interseccion> ruta = aStar(inicio, destino);

        // Si A* no encuentra una ruta, se usa una ruta más simple y directa
        if (ruta == null || ruta.isEmpty()) {
            ruta = calcularRutaSimple(inicio, destino);
        }

        System.out.println("Ruta calculada: " + ruta.size() + " intersecciones");
        return ruta;
    }


    /*
    * Implementación del algoritmo A* para encontrar la ruta óptima
    * considerando distancia + congestión + semáforos.
    * */

    private List<Interseccion> aStar(Interseccion inicio, Interseccion destino) {

        // Guarda de qué intersección proviene cada nodo (para reconstruir la ruta)
        Map<Interseccion, Interseccion> cameFrom = new HashMap<>();

        // gScore = coste desde el inicio hasta el nodo actual
        Map<Interseccion, Double> gScore = new HashMap<>();

        // fScore = gScore + heurística (lo que falta hasta el destino)
        Map<Interseccion, Double> fScore = new HashMap<>();

        // Conjunto abierto (nodos que faltan por evaluar)
        // Priorizado por el fScore más bajo
        PriorityQueue<Interseccion> openSet = new PriorityQueue<>(
                (a, b) -> Double.compare(fScore.getOrDefault(a, Double.MAX_VALUE),
                        fScore.getOrDefault(b, Double.MAX_VALUE))
        );


        // Conjunto cerrado (nodos ya evaluados)
        Set<Interseccion> closedSet = new HashSet<>();

        // Inicialización del algoritmo
        gScore.put(inicio, 0.0); // Coste inicial = 0
        fScore.put(inicio, heuristic(inicio, destino)); // fScore = heurística
        openSet.add(inicio); // Comienza desde 'inicio'


        // Bucle principal de A*
        while (!openSet.isEmpty()) {

            // Extrae el nodo con menor fScore (más prometedor)
            Interseccion current = openSet.poll();

            // Si ya llegamos al objetivo, reconstruimos la ruta
            if (current.equals(destino)) {
                return reconstruirRuta(cameFrom, current);
            }


            // Marcamos como visitado
            closedSet.add(current);


            // Iteramos por todos los vecinos válidos (arriba, abajo, izquierda, derecha)
            for (Interseccion neighbor : getVecinosValidos(current)) {

                // Saltar vecinos ya evaluados
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                // Coste acumulado si fuéramos desde current → neighbor
                double tentativeGScore = gScore.get(current) + calcularCosto(current, neighbor);


                // Si es un nodo nuevo, o encontramos un camino más barato:
                if (!openSet.contains(neighbor) || tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {

                    // Guardamos de donde venimos
                    cameFrom.put(neighbor, current);

                    // Actualizamos los costes
                    gScore.put(neighbor, tentativeGScore);
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
     * Costo de atravesar una intersección.
     * Incluye congestión y semáforo.
     */
    private double calcularCosto(Interseccion desde, Interseccion hacia) {
        double costoBase = 1.0;

        // Costo por congestión actual
        double costoCcongestion = hacia.getVehiculosEnEspera() * 2.0;

        // Costo fijo por semáforos
        double costoSemaforo = 0.5;

        // El coste total afecta qué rutas prefiere A*
        return costoBase + costoCcongestion + costoSemaforo;
    }


    /**
     * Devuelve las intersecciones adyacentes que existen en el mapa.
     */
    private List<Interseccion> getVecinosValidos(Interseccion actual) {
        List<Interseccion> vecinos = new ArrayList<>();
        int x = actual.getX();
        int y = actual.getY();

        // Vecino a la izquierda
        if (x > 0) {
            Interseccion vecino = ciudad.getInterseccion(x - 1, y);
            if (vecino != null) vecinos.add(vecino);
        }

        // Vecino a la derecha
        if (x < ciudad.getAncho() - 1) {
            Interseccion vecino = ciudad.getInterseccion(x + 1, y);
            if (vecino != null) vecinos.add(vecino);
        }

        // Vecino arriba
        if (y > 0) {
            Interseccion vecino = ciudad.getInterseccion(x, y - 1);
            if (vecino != null) vecinos.add(vecino);
        }

        // Vecino abajo
        if (y < ciudad.getAlto() - 1) {
            Interseccion vecino = ciudad.getInterseccion(x, y + 1);
            if (vecino != null) vecinos.add(vecino);
        }

        return vecinos;
    }


    /**
     * Heurística usada por A*.
     */
    private double heuristic(Interseccion a, Interseccion b) {
        // Distancia Manhattan
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    /**
     * Reconstruye la ruta final desde el destino hacia atrás usando la tabla cameFrom.
     */
    private List<Interseccion> reconstruirRuta(Map<Interseccion, Interseccion> cameFrom, Interseccion current) {
        List<Interseccion> ruta = new LinkedList<>();

        // Retrocede desde el destino hasta el inicio
        while (current != null) {
            ruta.add(0, current); // Insertar al inicio para mantener el orden
            current = cameFrom.get(current);
        }
        return ruta;
    }



    /**
     * Ruta alternativa si A* falla.
     * Avanza eligiendo un camino simple
     */
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

        // Movimiento simple hacia el destino
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
            System.out.println("Ruta simple alcanzó límite de pasos");
        }

        return ruta;
    }

}