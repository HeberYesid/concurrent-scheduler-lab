package com.proyecto.domain.algorithm;

import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.SchedulerConfig;
import java.util.Objects;

/**
 * Calculadora de prioridad efectiva que aplica envejecimiento logarítmico/exponencial atenuado.
 *
 * <p>Fórmula: {@code (39 - basePriority) + agingFactor * log2(1 + waitTime)}</p>
 *
 * @author scheduler-concurrente
 */
public final class ExponentialAgingCalculator implements PriorityCalculator {

    private static final double LN_2 = Math.log(2.0);

    @Override
    public double calculate(ProcessTask process, long currentTime, SchedulerConfig config) {
        Objects.requireNonNull(process, "El proceso no puede ser nulo");
        Objects.requireNonNull(config, "La configuración no puede ser nula");

        if (currentTime < process.arrivalTime()) {
            throw new IllegalArgumentException(
                    "currentTime (" + currentTime + ") no puede ser menor que arrivalTime ("
                            + process.arrivalTime() + ")");
        }
        long waitTime = currentTime - process.arrivalTime();
        double log2Value = Math.log(1.0 + waitTime) / LN_2;
        double invertedBase = 39.0 - process.basePriority();
        return invertedBase + config.agingFactor() * log2Value;
    }
}
