package com.proyecto.domain.algorithm;

/**
 * Interfaz que define el contrato de un comando para operaciones del heap.
 *
 * <p>Sigue el patrón de diseño Command para permitir la trazabilidad
 * y reversión (undo) de operaciones en el heap.</p>
 *
 * @author scheduler-concurrente
 */
public interface SchedulerCommand {

    /**
     * Ejecuta la operación en el heap.
     */
    void execute();

    /**
     * Deshace la operación en el heap, restaurando su estado previo.
     */
    void undo();
}
