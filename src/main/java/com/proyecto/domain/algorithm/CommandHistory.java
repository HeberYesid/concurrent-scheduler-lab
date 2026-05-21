package com.proyecto.domain.algorithm;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Historial de comandos que permite deshacer operaciones (undo) y realizar replay.
 *
 * @author scheduler-concurrente
 */
public final class CommandHistory {

    private final Deque<SchedulerCommand> history = new ArrayDeque<>();

    /**
     * Ejecuta un comando y lo almacena en el historial.
     *
     * @param cmd comando a ejecutar y almacenar, no nulo
     */
    public void executeAndRecord(SchedulerCommand cmd) {
        Objects.requireNonNull(cmd, "El comando no puede ser nulo");
        cmd.execute();
        history.push(cmd);
    }

    /**
     * Deshace el último comando ejecutado y lo elimina del historial.
     */
    public void undoLast() {
        if (!history.isEmpty()) {
            SchedulerCommand cmd = history.pop();
            cmd.undo();
        }
    }

    /**
     * Limpia el historial de comandos acumulados.
     */
    public void clear() {
        history.clear();
    }

    /**
     * Obtiene el tamaño actual del historial.
     *
     * @return número de comandos en el historial
     */
    public int size() {
        return history.size();
    }
}
