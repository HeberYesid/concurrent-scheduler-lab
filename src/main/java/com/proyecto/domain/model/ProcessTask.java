package com.proyecto.domain.model;

/**
 * Representa una tarea de proceso inmutable con prioridad base y tiempos de ejecución.
 *
 * <p>Sigue convenciones UNIX donde prioridad 0 es la máxima urgencia
 * y 39 la mínima. Todos los campos se validan en el constructor compacto
 * para garantizar invariantes desde la creación.</p>
 *
 * @param id           identificador único del proceso, debe ser {@code > 0}
 * @param basePriority prioridad base en el rango [{@value #MIN_PRIORITY}, {@value #MAX_PRIORITY}]
 * @param arrivalTime  instante de llegada en milisegundos, debe ser {@code >= 0}
 * @param burstTime    tiempo de ráfaga en milisegundos, rango [{@value #MIN_BURST_TIME}, {@value #MAX_BURST_TIME}]
 *
 * @author scheduler-concurrente
 */
public record ProcessTask(long id, int basePriority, long arrivalTime, long burstTime) {

    // ── Constantes con nombre (sin números mágicos) ────────────────

    /** Prioridad mínima numérica = máxima urgencia (estilo UNIX). */
    public static final int MIN_PRIORITY = 0;

    /** Prioridad máxima numérica = mínima urgencia. */
    public static final int MAX_PRIORITY = 39;

    /** Tiempo de ráfaga mínimo en milisegundos. */
    public static final long MIN_BURST_TIME = 1;

    /** Tiempo de ráfaga máximo en milisegundos. */
    public static final long MAX_BURST_TIME = 60_000;

    /**
     * Constructor compacto — valida todos los invariantes del proceso.
     *
     * @throws IllegalArgumentException si {@code id <= 0}
     * @throws IllegalArgumentException si {@code basePriority} no está en
     *         [{@value #MIN_PRIORITY}, {@value #MAX_PRIORITY}]
     * @throws IllegalArgumentException si {@code arrivalTime < 0}
     * @throws IllegalArgumentException si {@code burstTime} no está en
     *         [{@value #MIN_BURST_TIME}, {@value #MAX_BURST_TIME}]
     */
    public ProcessTask(long id, int basePriority, long arrivalTime, long burstTime) {
        validateId(id);
        validateBasePriority(basePriority);
        validateArrivalTime(arrivalTime);
        validateBurstTime(burstTime);
        this.id = id;
        this.basePriority = basePriority;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
    }

    private static void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El id del proceso debe ser > 0, recibido: " + id);
        }
    }

    private static void validateBasePriority(int basePriority) {
        if (basePriority < MIN_PRIORITY || basePriority > MAX_PRIORITY) {
            throw new IllegalArgumentException(
                    "basePriority debe estar en [" + MIN_PRIORITY + ", " + MAX_PRIORITY
                            + "], recibido: " + basePriority);
        }
    }

    private static void validateArrivalTime(long arrivalTime) {
        if (arrivalTime < 0) {
            throw new IllegalArgumentException(
                    "arrivalTime debe ser >= 0, recibido: " + arrivalTime);
        }
    }

    private static void validateBurstTime(long burstTime) {
        if (burstTime < MIN_BURST_TIME || burstTime > MAX_BURST_TIME) {
            throw new IllegalArgumentException(
                    "burstTime debe estar en [" + MIN_BURST_TIME + ", " + MAX_BURST_TIME
                            + "], recibido: " + burstTime);
        }
    }
}
