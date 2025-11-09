package clases;

/*Direcciones posibles para las calles
*  La clase -enum- se usa para definir constantes
* Direccion representa las posibles direcciones, NORTE,SUR,ESTE,OESTE
*
* COMO SE USA:
* Direccion dir = Direccion.ESTE;
*
* Para la direccion opuesta:
* Direccion opuesta = dir.opuesta(); // opuesta = OESTE
 * */

public enum Direccion{
    ARRIBA(0, -1), // Moverse arriba
    ABAJO(0, 1), // Moverse abajo
    DERECHA(1, 0), // Moverse a la derecha
    IZQUIERDA(-1, 0); // Moverse a la izquierda

    // Atributos
    private final int dx;
    private final int dy;

    // Constructor
    Direccion(int dx, int dy){
        this.dx = dx;
        this.dy = dy;
    }

    // Métodos
    public int getDx() { return this.dx; }
    public int getDy() { return this.dy; }

    // En caso de que sea la dirección opuesta
    public Direccion opuesta(){
        switch (this) {
            case ARRIBA: return ABAJO;
            case ABAJO: return ARRIBA;
            case DERECHA: return IZQUIERDA;
            case IZQUIERDA: return DERECHA;
            default: return null;
        }
    }
}

