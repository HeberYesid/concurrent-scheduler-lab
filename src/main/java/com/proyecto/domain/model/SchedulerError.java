package com.proyecto.domain.model;

/**
 * Enumeración de errores del scheduler con mensajes descriptivos.
 *
 * <p>Cada caso representa una condición de error específica que puede
 * ocurrir durante la planificación de procesos. Se usa junto con
 * {@link Result} para manejo explícito de errores.</p>
 *
 * @author scheduler-concurrente
 */
public enum SchedulerError {

    /** La lista de procesos proporcionada está vacía. */
    EMPTY_PROCESS_LIST("La lista de procesos está vacía"),

    /** La configuración del scheduler contiene valores inválidos. */
    INVALID_CONFIGURATION("Configuración del scheduler inválida"),

    /** Se detectó un proceso con ID duplicado en la lista. */
    DUPLICATE_PROCESS_ID("ID de proceso duplicado detectado"),

    /** El factor de aging debe ser estrictamente positivo. */
    AGING_FACTOR_ZERO("Factor de aging debe ser > 0");

    private final String message;

    SchedulerError(String message) {
        this.message = message;
    }

    /**
     * Obtiene el mensaje descriptivo del error.
     *
     * @return mensaje en español describiendo la condición de error
     */
    public String getMessage() {
        return message;
    }
}
