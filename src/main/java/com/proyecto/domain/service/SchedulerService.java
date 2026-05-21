package com.proyecto.domain.service;

import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.Result;
import com.proyecto.domain.model.SchedulerConfig;
import com.proyecto.domain.model.SchedulerError;
import com.proyecto.domain.model.SchedulerMetrics;
import java.util.List;

/**
 * Interfaz de negocio del planificador de procesos.
 *
 * @author scheduler-concurrente
 */
public interface SchedulerService {

    /**
     * Orquesta la ejecución de los procesos y genera las métricas de rendimiento finales.
     *
     * @param processes lista de procesos a planificar, no nula
     * @param config    configuración de parámetros del planificador, no nula
     * @return {@link Result} con las métricas calculadas o el error correspondiente
     */
    Result<SchedulerMetrics, SchedulerError> schedule(List<ProcessTask> processes, SchedulerConfig config);
}
