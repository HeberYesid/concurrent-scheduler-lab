package com.proyecto.domain.model;

/**
 * Registro inmutable que captura el resultado de ejecución de un proceso.
 *
 * <p>Almacena los tiempos de inicio, fin y espera asociados a un proceso
 * específico. Los invariantes se validan en el constructor compacto.</p>
 *
 * @param processId identificador del proceso ejecutado, debe ser {@code > 0}
 * @param startTime instante de inicio en milisegundos, debe ser {@code >= 0}
 * @param endTime   instante de fin en milisegundos, debe ser {@code >= startTime}
 * @param waitTime  tiempo de espera en milisegundos, debe ser {@code >= 0}
 *
 * @author scheduler-concurrente
 */
public record ExecutionRecord(long processId, long startTime, long endTime, long waitTime) {

    /**
     * Constructor compacto — valida la coherencia temporal del registro.
     *
     * @throws IllegalArgumentException si {@code processId <= 0}
     * @throws IllegalArgumentException si {@code endTime < startTime}
     * @throws IllegalArgumentException si {@code waitTime < 0}
     */
    public ExecutionRecord(long processId, long startTime, long endTime, long waitTime) {
        if (processId <= 0) {
            throw new IllegalArgumentException(
                    "processId debe ser > 0, recibido: " + processId);
        }
        if (endTime < startTime) {
            throw new IllegalArgumentException(
                    "endTime (" + endTime + ") no puede ser menor que startTime (" + startTime + ")");
        }
        if (waitTime < 0) {
            throw new IllegalArgumentException(
                    "waitTime debe ser >= 0, recibido: " + waitTime);
        }
        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.waitTime = waitTime;
    }
}
