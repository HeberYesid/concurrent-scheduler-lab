package com.proyecto.domain.algorithm;

import java.util.Objects;

/**
 * Comando que encapsula la extracción del proceso con mayor prioridad efectiva.
 *
 * @author scheduler-concurrente
 */
public final class ExtractMaxCommand implements SchedulerCommand {

    private final MaxHeap<SchedulableProcess> heap;
    private SchedulableProcess extracted;
    private Object[] snapshot;

    /**
     * Crea el comando de extracción.
     *
     * @param heap cola de prioridad, no nula
     */
    public ExtractMaxCommand(MaxHeap<SchedulableProcess> heap) {
        this.heap = Objects.requireNonNull(heap, "El heap no puede ser nulo");
    }

    @Override
    public void execute() {
        this.snapshot = heap.getState();
        this.extracted = heap.extractMax().orElse(null);
    }

    @Override
    public void undo() {
        if (snapshot != null) {
            heap.restoreState(snapshot);
        }
    }

    /**
     * Obtiene el proceso que fue extraído tras llamar a {@link #execute()}.
     *
     * @return el proceso extraído, o nulo si no se ha ejecutado o el heap estaba vacío
     */
    public SchedulableProcess getExtracted() {
        return extracted;
    }
}
