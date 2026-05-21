package com.proyecto.domain.algorithm;

import com.proyecto.domain.model.SchedulerConfig;
import java.util.Objects;

/**
 * Comando que encapsula la operación de envejecimiento masivo de prioridades.
 *
 * <p>Mantiene copias de seguridad de las prioridades y del estado de ordenación
 * del heap para permitir una restauración exacta en caso de undo.</p>
 *
 * @author scheduler-concurrente
 */
public final class ApplyAgingCommand implements SchedulerCommand {

    private final AgingEngine engine;
    private final MaxHeap<SchedulableProcess> heap;
    private final long currentTime;
    private final SchedulerConfig config;

    private Object[] heapSnapshot;
    private double[] prioritySnapshot;

    /**
     * Crea el comando de envejecimiento.
     *
     * @param engine      motor de aging, no nulo
     * @param heap        cola de prioridad, no nula
     * @param currentTime instante actual de simulación
     * @param config      configuración del planificador, no nula
     */
    public ApplyAgingCommand(
            AgingEngine engine,
            MaxHeap<SchedulableProcess> heap,
            long currentTime,
            SchedulerConfig config
    ) {
        this.engine = Objects.requireNonNull(engine, "engine no puede ser nulo");
        this.heap = Objects.requireNonNull(heap, "heap no puede ser nulo");
        this.currentTime = currentTime;
        this.config = Objects.requireNonNull(config, "config no puede ser nulo");
    }

    @Override
    public void execute() {
        this.heapSnapshot = heap.getState();
        this.prioritySnapshot = new double[heapSnapshot.length];
        for (int i = 0; i < heapSnapshot.length; i++) {
            if (heapSnapshot[i] instanceof SchedulableProcess process) {
                prioritySnapshot[i] = process.getEffectivePriority();
            }
        }
        engine.applyAging(heap, currentTime, config);
    }

    @Override
    public void undo() {
        if (heapSnapshot != null) {
            for (int i = 0; i < heapSnapshot.length; i++) {
                if (heapSnapshot[i] instanceof SchedulableProcess process) {
                    process.setEffectivePriority(prioritySnapshot[i]);
                }
            }
            heap.restoreState(heapSnapshot);
        }
    }
}
