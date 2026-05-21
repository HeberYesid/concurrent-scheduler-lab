package com.proyecto.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Resultado completo de una corrida del scheduler.
 *
 * <p>Expone tanto las métricas agregadas como la traza detallada de ejecución
 * para auditoría, validación y análisis del orden de planificación.</p>
 *
 * @param metrics        métricas agregadas de la simulación, no nulas
 * @param executionTrace secuencia de ejecución de procesos, no nula
 */
public record SchedulerRun(
        SchedulerMetrics metrics,
        List<ExecutionRecord> executionTrace
) {

    /**
     * Constructor canónico con validaciones de contrato.
     *
     * @throws NullPointerException si {@code metrics} o {@code executionTrace} son nulos
     */
    public SchedulerRun(SchedulerMetrics metrics, List<ExecutionRecord> executionTrace) {
        this.metrics = Objects.requireNonNull(metrics, "metrics no puede ser null");
        this.executionTrace = List.copyOf(
                Objects.requireNonNull(executionTrace, "executionTrace no puede ser null")
        );
    }
}
