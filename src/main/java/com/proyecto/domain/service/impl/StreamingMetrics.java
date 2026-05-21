package com.proyecto.domain.service.impl;

import com.proyecto.domain.model.ExecutionRecord;
import com.proyecto.domain.model.SchedulerMetrics;
import com.proyecto.domain.service.MetricsCollector;
import java.util.Objects;

/**
 * Recolector de métricas que computa los resultados de rendimiento de forma online y eficiente.
 *
 * <p>Mantiene acumuladores simples en O(1) de memoria para evitar retener
 * todos los objetos {@link ExecutionRecord} en simulaciones masivas.</p>
 *
 * @author scheduler-concurrente
 */
public final class StreamingMetrics implements MetricsCollector {

    private final long maxAcceptableWait;
    private int processedCount;
    private long totalWaitTime;
    private int starvedCount;

    /**
     * Crea el recolector con un umbral de starvation específico.
     *
     * @param maxAcceptableWait tiempo de espera máximo tolerable antes de starvation, en ms
     * @throws IllegalArgumentException si {@code maxAcceptableWait <= 0}
     */
    public StreamingMetrics(long maxAcceptableWait) {
        if (maxAcceptableWait <= 0) {
            throw new IllegalArgumentException(
                    "maxAcceptableWait debe ser > 0, recibido: " + maxAcceptableWait);
        }
        this.maxAcceptableWait = maxAcceptableWait;
    }

    @Override
    public void recordExecution(ExecutionRecord record) {
        Objects.requireNonNull(record, "El registro de ejecución no puede ser nulo");
        processedCount++;
        totalWaitTime += record.waitTime();
        if (record.waitTime() > maxAcceptableWait) {
            starvedCount++;
        }
    }

    @Override
    public SchedulerMetrics computeMetrics(long totalElapsedTimeMs) {
        long finalElapsedTime = Math.max(0L, totalElapsedTimeMs);
        if (processedCount == 0) {
            return new SchedulerMetrics(0.0, 0.0, 0.0, 0, finalElapsedTime);
        }
        double elapsedSeconds = finalElapsedTime / 1000.0;
        double throughput = elapsedSeconds <= 0.0 ? 0.0 : (double) processedCount / elapsedSeconds;
        double avgWaitTime = (double) totalWaitTime / processedCount;
        double starvationRate = (double) starvedCount / processedCount;
        return new SchedulerMetrics(
                throughput,
                avgWaitTime,
                starvationRate,
                processedCount,
                finalElapsedTime
        );
    }
}
