package com.proyecto.domain.service;

import com.proyecto.domain.model.ExecutionRecord;
import com.proyecto.domain.model.SchedulerMetrics;

/**
 * Interfaz para recolectar registros de ejecución y calcular métricas agregadas.
 *
 * @author scheduler-concurrente
 */
public interface MetricsCollector {

    /**
     * Registra el resultado de ejecución de un proceso.
     *
     * @param record registro de ejecución, no nulo
     */
    void recordExecution(ExecutionRecord record);

    /**
     * Calcula las métricas acumuladas de rendimiento.
     *
     * @param totalElapsedTimeMs tiempo total transcurrido de la simulación en ms
     * @return métricas de rendimiento calculadas {@link SchedulerMetrics}
     */
    SchedulerMetrics computeMetrics(long totalElapsedTimeMs);
}
