package clases;

import algoritmo.RoutePlanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Representa un vehículo que se mueve por la ciudad
 * Es un hilo independiente que sigue una ruta hacia su destino
 */
public class Vehiculo extends Thread {
    private final int id;
    private final Interseccion partida;
    private final Interseccion destino;
    private final Ciudad ciudad;
    private final RoutePlanner routePlanner;

    private Interseccion posicionActual;
    private List<Interseccion> ruta;
    private int indiceRutaActual;
    private boolean haLlegado;
    private boolean pausado;
    private boolean ejecutando;

    // Estadísticas
    private long tiempoInicioViaje;
    private long tiempoFinViaje;
    private long tiempoTotalEspera;
    private long tiempoUltimaEspera;

    // Sincronización
    private final ReentrantLock lockMovimiento;
    private final Random random;

    // Métodos para congestión
    private int intentosFallidosConsecutivos;
    private static final int MAX_INTENTOS_CONSECUTIVOS = 5;
    private static final int TIEMPO_MAXIMO_ESPERA = 10000; // 10 segundos
    private long tiempoInicioEsperaActual;

    // Prevención de deadlocks
    private int totalRecalculaciones;
    private static final int MAX_RECALCULACIONES = 10;
    private long tiempoUltimoMovimiento;
    private static final long TIEMPO_MAXIMO_SIN_MOVER = 30000; // 30 segundos

    public Vehiculo(int id, Interseccion partida, Interseccion destino,
                    Ciudad ciudad, RoutePlanner routePlanner) {
        this.id = id;
        this.partida = partida;
        this.destino = destino;
        this.ciudad = ciudad;
        this.routePlanner = routePlanner;

        this.posicionActual = partida;
        this.haLlegado = false;
        this.pausado = false;
        this.ejecutando = true;
        this.tiempoTotalEspera = 0;
        this.lockMovimiento = new ReentrantLock();
        this.random = new Random();

        // Inicializar variables de congestión
        this.intentosFallidosConsecutivos = 0;
        this.tiempoInicioEsperaActual = System.currentTimeMillis();
        this.totalRecalculaciones = 0;
        this.tiempoUltimoMovimiento = System.currentTimeMillis();

        // Calcular ruta inicial
        calcularRuta();

        this.setName("Vehiculo-" + id);
        this.setDaemon(true);
    }

    @Override
    public void run() {
        tiempoInicioViaje = System.currentTimeMillis();
        System.out.println("🚗 Vehículo " + id + " INICIÓ viaje de " + partida + " a " + destino);

        while (ejecutando && !haLlegado && indiceRutaActual < ruta.size()) {
            try {
                if (pausado) {
                    Thread.sleep(100);
                    continue;
                }

                // ✅ Verificar reset total si es necesario
                if (debeHacerResetTotal()) {
                    resetTotal();
                }

                moverASiguienteInterseccion();
                Thread.sleep(500 + random.nextInt(500)); // Velocidad variable

            } catch (InterruptedException e) {
                System.out.println("⏹️ Vehículo " + id + " interrumpido");
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (haLlegado) {
            tiempoFinViaje = System.currentTimeMillis();
            System.out.println("🎉 Vehículo " + id + " LLEGÓ a destino en " + getTiempoViaje() + "ms");
        } else {
            System.out.println("⏹️ Vehículo " + id + " detenido sin llegar al destino");
        }
    }

    /**
     * Intenta moverse a la siguiente intersección - VERSIÓN MEJORADA CON PREVENCIÓN DE DEADLOCKS
     */
    private void moverASiguienteInterseccion() {
        // ✅ DETECCIÓN DE DEADLOCK
        long tiempoActual = System.currentTimeMillis();
        if (tiempoActual - tiempoUltimoMovimiento > TIEMPO_MAXIMO_SIN_MOVER) {
            System.err.println("🚨 Vehículo " + id + " posible DEADLOCK detectado - " +
                    (tiempoActual - tiempoUltimoMovimiento) + "ms sin moverse");
            recuperarDeDeadlock();
            return;
        }

        if (indiceRutaActual >= ruta.size() - 1) {
            haLlegado = true;
            System.out.println("🎉 Vehículo " + id + " LLEGÓ al destino final!");
            return;
        }

        Interseccion siguiente = ruta.get(indiceRutaActual + 1);

        // ✅ VERIFICAR que no sea la misma intersección
        if (siguiente.equals(posicionActual)) {
            indiceRutaActual++;
            System.out.println("➡️ Vehículo " + id + " saltó a siguiente en ruta (misma intersección)");
            tiempoUltimoMovimiento = System.currentTimeMillis();
            return;
        }

        // ✅ VERIFICAR TIMEOUT GLOBAL
        if (System.currentTimeMillis() - tiempoInicioEsperaActual > TIEMPO_MAXIMO_ESPERA) {
            System.out.println("⏰ Vehículo " + id + " TIMEOUT global, recalculando ruta...");
            recalcularRutaPorCongestion();
            tiempoInicioEsperaActual = System.currentTimeMillis();
            return;
        }

        Direccion direccionMovimiento = obtenerDireccionMovimiento(posicionActual, siguiente);

        // Verificar semáforo
        if (siguiente.getSemaforo() != null &&
                !siguiente.getSemaforo().puedePasar(direccionMovimiento)) {
            System.out.println("🔴 Vehículo " + id + " esperando en semáforo " + siguiente);
            esperarEnSemaforo();
            intentosFallidosConsecutivos = 0;
            return;
        }

        // ✅ INTENTAR ENTRAR con mejor manejo de errores
        boolean exito = false;
        try {
            exito = siguiente.intentarEntrar(this, direccionMovimiento);
        } catch (Exception e) {
            System.err.println("❌ ERROR crítico en intentarEntrar para vehículo " + id + ": " + e.getMessage());
            recalcularRutaPorCongestion();
            return;
        }

        if (exito) {
            // ✅ ÉXITO: Movimiento seguro con manejo CORREGIDO de locks
            lockMovimiento.lock();
            try {
                System.out.println("🚗 Vehículo " + id + " se movió de " + posicionActual + " a " + siguiente);

                // ✅ CORRECCIÓN CRÍTICA: Solo liberar si tenemos el lock de la intersección anterior
                if (posicionActual != null && !posicionActual.equals(siguiente)) {
                    liberarInterseccionAnterior(posicionActual);
                }

                // ✅ ACTUALIZAR posición
                posicionActual = siguiente;
                indiceRutaActual++;
                intentosFallidosConsecutivos = 0;
                tiempoInicioEsperaActual = System.currentTimeMillis();
                tiempoUltimoMovimiento = System.currentTimeMillis();

                // Verificar destino
                if (posicionActual.equals(destino)) {
                    haLlegado = true;
                    System.out.println("🎉 Vehículo " + id + " LLEGÓ al destino final: " + destino);
                }
            } finally {
                lockMovimiento.unlock();
            }
        } else {
            // ❌ FALLÓ: Manejo inteligente
            intentosFallidosConsecutivos++;
            System.out.println("⏳ Vehículo " + id + " esperando en " + siguiente +
                    " (intento " + intentosFallidosConsecutivos + "/" + MAX_INTENTOS_CONSECUTIVOS + ")");

            esperarEnInterseccion();

            if (intentosFallidosConsecutivos >= MAX_INTENTOS_CONSECUTIVOS) {
                System.out.println("🔄 Vehículo " + id + " recalcula por " +
                        intentosFallidosConsecutivos + " intentos fallidos consecutivos");
                recalcularRutaPorCongestion();
                intentosFallidosConsecutivos = 0;
            }

            if (intentosFallidosConsecutivos == 1) {
                tiempoInicioEsperaActual = System.currentTimeMillis();
            }
        }
    }


    private void liberarInterseccionAnterior(Interseccion interseccionAnterior) {
        try {
            // ✅ VERIFICAR que no estamos intentando liberar la partida o el destino final
            if (interseccionAnterior.equals(partida) || interseccionAnterior.equals(destino)) {
                return; // No liberar partida o destino
            }

            // ✅ VERIFICAR que tenemos el lock antes de intentar liberar
            if (tieneMetodoIntentarSalir(interseccionAnterior)) {
                boolean liberado = interseccionAnterior.intentarSalir(this);
                if (liberado) {
                    System.out.println("🔓 Vehículo " + id + " liberó " + interseccionAnterior);
                } else {
                    // ❌ NO imprimir error - es normal no tener el lock después de reset
                }
            } else {
                // Método tradicional con verificación
                if (interseccionAnterior.equals(partida)) {
                    // No liberar la partida
                    return;
                } else {
                    try {
                        interseccionAnterior.salir(this);
                        System.out.println("🔓 Vehículo " + id + " liberó " + interseccionAnterior);
                    } catch (Exception e) {
                        // ❌ NO imprimir error - es normal después de reset
                    }
                }
            }
        } catch (Exception e) {
            // ❌ NO imprimir error - silenciar completamente los errores de liberación
        }
    }


    /**
     * Verifica si la intersección tiene el método intentarSalir
     */
    private boolean tieneMetodoIntentarSalir(Interseccion interseccion) {
        try {
            // Verificar por reflexión si el método existe
            interseccion.getClass().getMethod("intentarSalir", Vehiculo.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Espera cuando el semáforo está en rojo
     */
    private void esperarEnSemaforo() {
        try {
            tiempoUltimaEspera = System.currentTimeMillis();
            Thread.sleep(800 + random.nextInt(400)); // Esperar 0.8-1.2 segundos
            tiempoTotalEspera += (System.currentTimeMillis() - tiempoUltimaEspera);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Espera cuando la intersección está ocupada
     */
    private void esperarEnInterseccion() {
        try {
            tiempoUltimaEspera = System.currentTimeMillis();
            Thread.sleep(300 + random.nextInt(700)); // Espera variable 0.3-1.0 segundos
            tiempoTotalEspera += (System.currentTimeMillis() - tiempoUltimaEspera);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Valida que la ruta sea físicamente posible
     */
    private boolean validarRuta(List<Interseccion> ruta) {
        if (ruta == null || ruta.size() < 2) {
            return false;
        }

        for (int i = 0; i < ruta.size() - 1; i++) {
            Interseccion actual = ruta.get(i);
            Interseccion siguiente = ruta.get(i + 1);

            int dx = Math.abs(siguiente.getX() - actual.getX());
            int dy = Math.abs(siguiente.getY() - actual.getY());

            // ✅ VERIFICAR que el movimiento sea válido (solo 1 coordenada cambia)
            if (dx + dy != 1) {
                System.err.println("❌ RUTA INVÁLIDA: Movimiento imposible de " + actual + " a " + siguiente +
                        " (dx=" + dx + ", dy=" + dy + ")");
                return false;
            }
        }
        return true;
    }

    /**
     * Ruta simple de emergencia - garantiza movimientos válidos
     */
    private List<Interseccion> calcularRutaSimpleEmergencia(Interseccion inicio, Interseccion destino) {
        List<Interseccion> ruta = new ArrayList<>();
        ruta.add(inicio);

        int xActual = inicio.getX();
        int yActual = inicio.getY();
        int xDestino = destino.getX();
        int yDestino = destino.getY();

        int maxPasos = ciudad.getAncho() * ciudad.getAlto();
        int pasos = 0;

        // ✅ MOVIMIENTO GARANTIZADO VÁLIDO: Primero X, luego Y
        while (xActual != xDestino && pasos < maxPasos) {
            xActual += (xDestino > xActual) ? 1 : -1;
            Interseccion siguiente = ciudad.getInterseccion(xActual, yActual);
            if (siguiente != null) {
                ruta.add(siguiente);
            }
            pasos++;
        }

        while (yActual != yDestino && pasos < maxPasos) {
            yActual += (yDestino > yActual) ? 1 : -1;
            Interseccion siguiente = ciudad.getInterseccion(xActual, yActual);
            if (siguiente != null) {
                ruta.add(siguiente);
            }
            pasos++;
        }

        if (pasos >= maxPasos) {
            System.err.println("❌ Ruta de emergencia alcanzó límite de pasos");
        }

        return ruta;
    }

    /**
     * Calcula la ruta desde la posición actual hasta el destino
     */
    private void calcularRuta() {
        try {
            this.ruta = routePlanner.calcularRuta(posicionActual, destino);
            this.indiceRutaActual = 0;

            if (ruta == null || ruta.isEmpty()) {
                System.err.println("❌ Vehículo " + id + ": No se pudo calcular ruta");
                haLlegado = true;
                return;
            }

            // ✅ VALIDAR Ruta ANTES de usarla
            if (!validarRuta(ruta)) {
                System.err.println("❌ Vehículo " + id + ": Ruta inválida, recalculando con emergencia...");
                // Forzar recálculo con ruta simple de emergencia
                this.ruta = calcularRutaSimpleEmergencia(posicionActual, destino);

                if (!validarRuta(ruta)) {
                    System.err.println("❌ Vehículo " + id + ": Ruta de emergencia también inválida");
                    haLlegado = true;
                    return;
                }
            }

            System.out.println("📍 Vehículo " + id + " ruta calculada: " + ruta.size() + " pasos");

        } catch (Exception e) {
            System.err.println("❌ ERROR calculando ruta para vehículo " + id + ": " + e.getMessage());
            haLlegado = true;
        }
    }

    /**
     * Obtiene la dirección de movimiento entre dos intersecciones
     */
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

    /**
     * Recalcula ruta de manera más inteligente con reset total si es necesario
     */
    private void recalcularRutaPorCongestion() {
        totalRecalculaciones++;

        if (totalRecalculaciones >= MAX_RECALCULACIONES) {
            System.out.println("🔄🔄🔄 Vehículo " + id + " RESET TOTAL por demasiadas recalculaciones");
            resetTotal();
            return;
        }

        System.out.println("🔄 Vehículo " + id + " recalculando ruta por congestión (" +
                totalRecalculaciones + "/" + MAX_RECALCULACIONES + ")");

        // ✅ Liberar intersección actual antes de recalcular
        if (posicionActual != null && !posicionActual.equals(partida)) {
            try {
                if (tieneMetodoIntentarSalir(posicionActual)) {
                    posicionActual.intentarSalir(this);
                } else {
                    posicionActual.salir(this);
                }
                System.out.println("🔓 Vehículo " + id + " liberó intersección para recálculo");
            } catch (Exception e) {
                System.err.println("⚠️ Error al liberar intersección para recálculo: " + e.getMessage());
            }
        }

        calcularRuta();
        intentosFallidosConsecutivos = 0;
        tiempoInicioEsperaActual = System.currentTimeMillis();
    }

    /**
     * Reset total del vehículo - última medida para evitar deadlocks
     */
    /**
     * Reset total del vehículo - VERSIÓN MEJORADA
     */
    private void resetTotal() {
        System.out.println("🔄🔄🔄 RESET TOTAL para Vehículo " + id);

        // ✅ Liberar cualquier intersección de manera SILENCIOSA
        if (posicionActual != null && !posicionActual.equals(partida)) {
            try {
                liberarInterseccionAnterior(posicionActual);
            } catch (Exception e) {
                // Ignorar completamente cualquier error
            }
        }

        // ✅ Volver al punto de partida
        this.posicionActual = partida;
        this.indiceRutaActual = 0;
        this.totalRecalculaciones = 0;
        this.intentosFallidosConsecutivos = 0;
        this.tiempoInicioEsperaActual = System.currentTimeMillis();
        this.tiempoUltimoMovimiento = System.currentTimeMillis();

        // ✅ Calcular nueva ruta
        calcularRuta();

        System.out.println("✅ Vehículo " + id + " reset completo, reiniciando desde " + partida);
    }

    /**
     * Verifica si debe hacer un reset total
     */
    private boolean debeHacerResetTotal() {
        // ✅ Solo reset después de MUCHAS recalculaciones O tiempo muy largo
        boolean porRecalculaciones = totalRecalculaciones >= MAX_RECALCULACIONES;
        boolean porTiempo = (System.currentTimeMillis() - tiempoInicioViaje) > 120000; // 2 minutos

        // ✅ Evitar reset demasiado frecuente
        boolean tiempoDesdeUltimoReset = (System.currentTimeMillis() - tiempoUltimoMovimiento) > 15000; // 15 seg mínimo

        return (porRecalculaciones || porTiempo) && tiempoDesdeUltimoReset;
    }

    /**
     * Recuperación agresiva de deadlock
     */
    private void recuperarDeDeadlock() {
        System.err.println("🔄🔄🔄 RECUPERACIÓN DE DEADLOCK para Vehículo " + id);

        // ✅ Liberar cualquier recurso bloqueado
        if (posicionActual != null && !posicionActual.equals(partida)) {
            try {
                if (tieneMetodoIntentarSalir(posicionActual)) {
                    posicionActual.intentarSalir(this);
                } else {
                    posicionActual.salir(this);
                }
                System.out.println("🔓 Vehículo " + id + " liberó intersección en recuperación");
            } catch (Exception e) {
                // Ignorar errores en recuperación
            }
        }

        // ✅ Saltar a una posición segura
        if (ruta != null && indiceRutaActual < ruta.size() - 1) {
            // Saltar varios pasos en la ruta
            int saltos = Math.min(3, ruta.size() - indiceRutaActual - 1);
            indiceRutaActual += saltos;
            posicionActual = ruta.get(indiceRutaActual);
            System.out.println("➡️ Vehículo " + id + " saltó " + saltos + " pasos a " + posicionActual);
        } else {
            // Reset completo
            resetTotal();
        }

        tiempoUltimoMovimiento = System.currentTimeMillis();
        intentosFallidosConsecutivos = 0;
    }

    /**
     * SOLUCIÓN DE EMERGENCIA - Ejecutar esto para vehículos atascados
     */

    /**
     * Pausa el vehículo
     */
    public void pausar() {
        this.pausado = true;
        System.out.println("⏸️ Vehículo " + id + " pausado");
    }

    /**
     * Reanuda el vehículo
     */
    public void reanudar() {
        this.pausado = false;
        System.out.println("▶️ Vehículo " + id + " reanudado");
    }

    /**
     * Detiene el vehículo permanentemente - VERSIÓN MEJORADA
     */
    public void detener() {
        this.ejecutando = false;
        this.interrupt();

        // ✅ Liberar la intersección actual de manera segura
        if (posicionActual != null) {
            try {
                if (tieneMetodoIntentarSalir(posicionActual)) {
                    posicionActual.intentarSalir(this);
                } else {
                    if (posicionActual.equals(partida)) {
                        posicionActual.salir();
                    } else {
                        posicionActual.salir(this);
                    }
                }
                System.out.println("🔓 Vehículo " + id + " liberó intersección al detenerse");
            } catch (Exception e) {
                System.err.println("❌ ERROR liberando intersección al detener vehículo " + id + ": " + e.getMessage());
            }
        }

        System.out.println("⏹️ Vehículo " + id + " detenido");
    }

    // Getters para estado del vehículo
    public int getIdVehiculo() { return id; }
    public Interseccion getDestino() { return destino; }
    public Interseccion getInterseccionActual() { return posicionActual; }
    public boolean haLlegado() { return haLlegado; }
    public boolean estaEsperando() { return pausado || tiempoUltimaEspera > 0; }
    public boolean estaEnInterseccion() { return posicionActual != null; }

    // Getters para estadísticas
    public long getTiempoViaje() {
        if (tiempoInicioViaje == 0) return 0;
        if (tiempoFinViaje == 0) return System.currentTimeMillis() - tiempoInicioViaje;
        return tiempoFinViaje - tiempoInicioViaje;
    }

    public long getTiempoEsperaTotal() { return tiempoTotalEspera; }

    public double getProgreso() {
        if (ruta == null || ruta.size() <= 1) return 0.0;
        return (double) indiceRutaActual / (ruta.size() - 1);
    }

    /**
     * Obtiene la posición actual para visualización
     */
    public String getPosicionActual() {
        if (posicionActual == null) return "N/A";
        return String.format("(%d,%d)", posicionActual.getX(), posicionActual.getY());
    }

    @Override
    public String toString() {
        return String.format("Vehículo %d [%s -> %s] %s",
                id, partida, destino, haLlegado ? "🏁" : "🚗");
    }
}