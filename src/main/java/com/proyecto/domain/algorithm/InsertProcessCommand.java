package com.proyecto.domain.algorithm;

import java.util.Objects;

/**
 * Comando que encapsula la inserción de un proceso en el heap.
 *
 * @author scheduler-concurrente
 */
public final class InsertProcessCommand implements SchedulerCommand {

    private final MaxHeap<SchedulableProcess> heap;
    private final SchedulableProcess process;
    private Object[] snapshot;

    /**
     * Crea el comando de inserción.
     *
     * @param heap    cola de prioridad, no nula
     * @param process proceso a insertar, no nulo
     */
    public InsertProcessCommand(MaxHeap<SchedulableProcess> heap, SchedulableProcess process) {
        this.heap = Objects.requireNonNull(heap, "El heap no puede ser nulo");
        this.process = Objects.requireNonNull(process, "El proceso no puede ser nulo");
    }

    @Override
    public void execute() {
        this.snapshot = heap.getState();
        heap.insert(process);
    }

    @Override
    public void undo() {
        if (snapshot != null) {
            heap.restoreState(snapshot);
        }
    }
}
