package com.proyecto.domain.algorithm;

import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.SchedulerConfig;

/**
 * Estrategia para calcular la prioridad efectiva de un proceso.
 *
 * <p>Permite desacoplar la fórmula matemática de envejecimiento (aging)
 * de la cola de prioridad y del planificador principal.</p>
 *
 * @author scheduler-concurrente
 */
@FunctionalInterface
public interface PriorityCalculator {

    /**
     * Calcula la prioridad efectiva de un proceso en un instante de tiempo.
     *
     * @param process     el proceso bajo análisis, no nulo
     * @param currentTime instante actual de la simulación, en milisegundos
     * @param config      configuración del scheduler, no nula
     * @return la prioridad efectiva calculada
     */
    double calculate(ProcessTask process, long currentTime, SchedulerConfig config);
}
