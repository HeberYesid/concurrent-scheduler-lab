package com.proyecto.domain.algorithm;

import com.proyecto.domain.model.SchedulerConfig;
import java.util.Objects;

/**
 * Motor encargado de aplicar envejecimiento (aging) masivo a la cola de prioridad.
 *
 * <p>Actualiza la prioridad efectiva de cada proceso en espera y restaura
 * la propiedad del heap en tiempo O(n).</p>
 *
 * @author scheduler-concurrente
 */
public final class AgingEngine {

    private final PriorityCalculator calculator;

    /**
     * Crea un motor de aging con la estrategia de prioridad dada.
     *
     * @param calculator estrategia de cálculo de prioridad, no nula
     */
    public AgingEngine(PriorityCalculator calculator) {
        this.calculator = Objects.requireNonNull(calculator, "calculator no puede ser nula");
    }

    /**
     * Obtiene la estrategia de prioridad activa.
     *
     * @return priority calculator strategy
     */
    public PriorityCalculator getCalculator() {
        return calculator;
    }

    /**
     * Aplica el ciclo de envejecimiento sobre todos los procesos activos en el heap.
     *
     * <p>Complejidad: O(n) lineal + Floyd rebuild</p>
     *
     * @param heap        cola de prioridad, no nula
     * @param currentTime instante de simulación actual en milisegundos
     * @param config      configuración del scheduler, no nula
     */
    public void applyAging(MaxHeap<SchedulableProcess> heap, long currentTime, SchedulerConfig config) {
        Objects.requireNonNull(heap, "El heap no puede ser nulo");
        Objects.requireNonNull(config, "La configuración no puede ser nula");

        Object[] elements = heap.getState();
        for (Object obj : elements) {
            if (obj instanceof SchedulableProcess process) {
                double priority = calculator.calculate(process.getTask(), currentTime, config);
                process.setEffectivePriority(priority);
            }
        }
        heap.rebuildHeap();
    }
}
