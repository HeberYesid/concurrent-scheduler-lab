package com.proyecto.domain.algorithm;

import com.proyecto.domain.model.ProcessTask;
import java.util.Objects;

/**
 * Wrapper mutable de {@link ProcessTask} utilizado para el ordenamiento en el Heap.
 *
 * <p>Mantiene la prioridad efectiva calculada dinámicamente y expone la lógica
 * de comparación para el desempate determinista.</p>
 *
 * @author scheduler-concurrente
 */
public final class SchedulableProcess implements Comparable<SchedulableProcess> {

    private final ProcessTask task;
    private double effectivePriority;

    /**
     * Crea un wrapper para un proceso.
     *
     * @param task proceso inmutable, no nulo
     */
    public SchedulableProcess(ProcessTask task) {
        this.task = Objects.requireNonNull(task, "El proceso no puede ser nulo");
        this.effectivePriority = 39.0 - task.basePriority();
    }

    /**
     * Obtiene el proceso inmutable subyacente.
     *
     * @return la tarea {@link ProcessTask}
     */
    public ProcessTask getTask() {
        return task;
    }

    /**
     * Obtiene la prioridad efectiva actual del proceso.
     *
     * @return prioridad efectiva
     */
    public double getEffectivePriority() {
        return effectivePriority;
    }

    /**
     * Establece la prioridad efectiva del proceso.
     *
     * @param effectivePriority nueva prioridad efectiva
     */
    public void setEffectivePriority(double effectivePriority) {
        this.effectivePriority = effectivePriority;
    }

    /**
     * Compara este proceso con otro.
     *
     * <p>Criterios de ordenación (Max-Heap):</p>
     * <ol>
     *   <li>Mayor prioridad efectiva primero (prioridad efectiva descendente).</li>
     *   <li>Menor instante de llegada primero (desempate por arrivalTime ascendente).</li>
     *   <li>Menor identificador primero (desempate determinista por processId ascendente).</li>
     * </ol>
     */
    @Override
    public int compareTo(SchedulableProcess other) {
        if (this == other) {
            return 0;
        }
        int comp = Double.compare(this.effectivePriority, other.effectivePriority);
        if (comp != 0) {
            return comp;
        }
        int arrivalComp = Long.compare(other.task.arrivalTime(), this.task.arrivalTime());
        if (arrivalComp != 0) {
            return arrivalComp;
        }
        return Long.compare(other.task.id(), this.task.id());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SchedulableProcess other) {
            return this.task.id() == other.task.id();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(task.id());
    }

    @Override
    public String toString() {
        return "SchedulableProcess{id=" + task.id()
                + ", basePriority=" + task.basePriority()
                + ", arrivalTime=" + task.arrivalTime()
                + ", effectivePriority=" + effectivePriority + "}";
    }
}
