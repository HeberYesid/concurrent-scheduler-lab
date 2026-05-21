package com.proyecto.domain.service;

import com.proyecto.domain.model.ProcessTask;
import java.util.List;

/**
 * Contrato de repositorio para el almacenamiento y consulta de tareas de proceso.
 *
 * @author scheduler-concurrente
 */
public interface ProcessRepository {

    /**
     * Recupera todos los procesos almacenados en el repositorio.
     *
     * @return lista de procesos
     */
    List<ProcessTask> findAll();

    /**
     * Guarda o actualiza un proceso.
     *
     * @param process proceso a guardar, no nulo
     */
    void save(ProcessTask process);

    /**
     * Elimina un proceso por su identificador único.
     *
     * @param id identificador del proceso
     * @return verdadero si fue eliminado, falso de lo contrario
     */
    boolean deleteById(long id);
}
