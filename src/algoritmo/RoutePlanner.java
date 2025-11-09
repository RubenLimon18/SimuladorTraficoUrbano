package algoritmo;

import clases.Interseccion;
import clases.Ciudad;

import java.util.*;

public class RoutePlanner {
    private Ciudad ciudad;

    public RoutePlanner(Ciudad ciudad) {
        this.ciudad = ciudad;
    }

    public List<Interseccion> calcularRuta(Interseccion inicio, Interseccion destino) {
        // Implementación simple por ahora - ruta directa
        List<Interseccion> ruta = new ArrayList<>();
        ruta.add(inicio);

        int x = inicio.getX();
        int y = inicio.getY();
        int destX = destino.getX();
        int destY = destino.getY();

        // Mover horizontalmente primero, luego verticalmente
        while (x != destX) {
            x += (destX > x) ? 1 : -1;
            ruta.add(ciudad.getInterseccion(x, y));
        }

        while (y != destY) {
            y += (destY > y) ? 1 : -1;
            ruta.add(ciudad.getInterseccion(x, y));
        }

        return ruta;
    }
}