package com.proyecto.domain.model;

/**
 * Configuración inmutable del scheduler con parámetros de aging y espera.
 *
 * <p>Define los factores que controlan el envejecimiento de prioridades
 * y los umbrales de espera aceptable. Usa {@link #withDefaults()} para
 * obtener una configuración con valores razonables por defecto.</p>
 *
 * @param agingFactor      factor de envejecimiento en {@code (0.0, 1.0]}
 * @param agingInterval    intervalo de aging en ms, rango [{@value #MIN_AGING_INTERVAL}, {@value #MAX_AGING_INTERVAL}]
 * @param maxAcceptableWait tiempo máximo de espera aceptable en ms, debe ser {@code > 0}
 *
 * @author scheduler-concurrente
 */
public record SchedulerConfig(
        double agingFactor,
        long agingInterval,
        long maxAcceptableWait
) {

    // ── Valores por defecto ────────────────────────────────────────

    /** Factor de aging por defecto. */
    public static final double DEFAULT_AGING_FACTOR = 0.5;

    /** Intervalo de aging por defecto en milisegundos. */
    public static final long DEFAULT_AGING_INTERVAL = 100;

    /** Tiempo máximo de espera aceptable por defecto en milisegundos. */
    public static final long DEFAULT_MAX_ACCEPTABLE_WAIT = 5_000;

    /** Prioridad base máxima (estilo UNIX). */
    public static final int MAX_BASE_PRIORITY = 39;

    // ── Límites de validación (sin números mágicos) ────────────────

    /** Intervalo de aging mínimo permitido en milisegundos. */
    private static final long MIN_AGING_INTERVAL = 10;

    /** Intervalo de aging máximo permitido en milisegundos. */
    private static final long MAX_AGING_INTERVAL = 5_000;

    /**
     * Constructor compacto — valida los rangos de todos los parámetros.
     *
     * @throws IllegalArgumentException si {@code agingFactor <= 0.0} o {@code agingFactor > 1.0}
     * @throws IllegalArgumentException si {@code agingInterval} no está en
     *         [{@value #MIN_AGING_INTERVAL}, {@value #MAX_AGING_INTERVAL}]
     * @throws IllegalArgumentException si {@code maxAcceptableWait <= 0}
     */
    public SchedulerConfig(double agingFactor, long agingInterval, long maxAcceptableWait) {
        if (agingFactor <= 0.0 || agingFactor > 1.0) {
            throw new IllegalArgumentException(
                    "agingFactor debe estar en (0.0, 1.0], recibido: " + agingFactor);
        }
        if (agingInterval < MIN_AGING_INTERVAL || agingInterval > MAX_AGING_INTERVAL) {
            throw new IllegalArgumentException(
                    "agingInterval debe estar en [" + MIN_AGING_INTERVAL + ", "
                            + MAX_AGING_INTERVAL + "], recibido: " + agingInterval);
        }
        if (maxAcceptableWait <= 0) {
            throw new IllegalArgumentException(
                    "maxAcceptableWait debe ser > 0, recibido: " + maxAcceptableWait);
        }
        this.agingFactor = agingFactor;
        this.agingInterval = agingInterval;
        this.maxAcceptableWait = maxAcceptableWait;
    }

    /**
     * Crea una configuración con valores por defecto razonables.
     *
     * @return nueva instancia con {@link #DEFAULT_AGING_FACTOR},
     *         {@link #DEFAULT_AGING_INTERVAL} y {@link #DEFAULT_MAX_ACCEPTABLE_WAIT}
     */
    public static SchedulerConfig withDefaults() {
        return new SchedulerConfig(
                DEFAULT_AGING_FACTOR,
                DEFAULT_AGING_INTERVAL,
                DEFAULT_MAX_ACCEPTABLE_WAIT
        );
    }
}
