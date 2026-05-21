package com.proyecto.domain.model;

/**
 * Métricas inmutables de rendimiento del scheduler.
 *
 * <p>Captura throughput, tiempo promedio de espera, tasa de starvation,
 * cantidad de procesos atendidos y el tiempo total transcurrido.</p>
 *
 * @param throughput         procesos completados por segundo, debe ser {@code >= 0}
 * @param avgWaitTime        tiempo de espera promedio en ms, debe ser {@code >= 0}
 * @param starvationRate     tasa de starvation en {@code [0.0, 1.0]}
 * @param processedCount     cantidad de procesos atendidos, debe ser {@code >= 0}
 * @param totalElapsedTimeMs tiempo total transcurrido en ms, debe ser {@code >= 0}
 *
 * @author scheduler-concurrente
 */
public record SchedulerMetrics(
        double throughput,
        double avgWaitTime,
        double starvationRate,
        int processedCount,
        long totalElapsedTimeMs
) {

    /** Valor máximo de starvation rate (100%). */
    private static final double MAX_STARVATION_RATE = 1.0;

    /** Valor mínimo de starvation rate (0%). */
    private static final double MIN_STARVATION_RATE = 0.0;

    /**
     * Constructor compacto — valida rangos de todas las métricas.
     *
     * @throws IllegalArgumentException si {@code throughput < 0}
     * @throws IllegalArgumentException si {@code avgWaitTime < 0}
     * @throws IllegalArgumentException si {@code starvationRate} no está en [0.0, 1.0]
     * @throws IllegalArgumentException si {@code processedCount < 0}
     * @throws IllegalArgumentException si {@code totalElapsedTimeMs < 0}
     */
    public SchedulerMetrics(
            double throughput,
            double avgWaitTime,
            double starvationRate,
            int processedCount,
            long totalElapsedTimeMs
    ) {
        validateThroughput(throughput);
        validateAvgWaitTime(avgWaitTime);
        validateStarvationRate(starvationRate);
        validateProcessedCount(processedCount);
        validateTotalElapsedTime(totalElapsedTimeMs);
        this.throughput = throughput;
        this.avgWaitTime = avgWaitTime;
        this.starvationRate = starvationRate;
        this.processedCount = processedCount;
        this.totalElapsedTimeMs = totalElapsedTimeMs;
    }

    private static void validateThroughput(double throughput) {
        if (throughput < 0) {
            throw new IllegalArgumentException(
                    "throughput debe ser >= 0, recibido: " + throughput);
        }
    }

    private static void validateAvgWaitTime(double avgWaitTime) {
        if (avgWaitTime < 0) {
            throw new IllegalArgumentException(
                    "avgWaitTime debe ser >= 0, recibido: " + avgWaitTime);
        }
    }

    private static void validateStarvationRate(double starvationRate) {
        if (starvationRate < MIN_STARVATION_RATE || starvationRate > MAX_STARVATION_RATE) {
            throw new IllegalArgumentException(
                    "starvationRate debe estar en [" + MIN_STARVATION_RATE + ", "
                            + MAX_STARVATION_RATE + "], recibido: " + starvationRate);
        }
    }

    private static void validateProcessedCount(int processedCount) {
        if (processedCount < 0) {
            throw new IllegalArgumentException(
                    "processedCount debe ser >= 0, recibido: " + processedCount);
        }
    }

    private static void validateTotalElapsedTime(long totalElapsedTimeMs) {
        if (totalElapsedTimeMs < 0) {
            throw new IllegalArgumentException(
                    "totalElapsedTimeMs debe ser >= 0, recibido: " + totalElapsedTimeMs);
        }
    }
}
