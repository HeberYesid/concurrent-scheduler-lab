package com.proyecto.infrastructure.persistence;

import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.service.ProcessRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Adaptador de persistencia en memoria utilizando un mapa sincronizado.
 *
 * @author scheduler-concurrente
 */
public final class InMemoryProcessRepository implements ProcessRepository {

    private final Map<Long, ProcessTask> storage = Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    public List<ProcessTask> findAll() {
        synchronized (storage) {
            return new ArrayList<>(storage.values());
        }
    }

    @Override
    public void save(ProcessTask process) {
        Objects.requireNonNull(process, "El proceso no puede ser nulo");
        if (storage.containsKey(process.id())) {
            throw new IllegalArgumentException(
                    "Ya existe un proceso con id: " + process.id());
        }
        storage.put(process.id(), process);
    }

    @Override
    public boolean deleteById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El id debe ser > 0, recibido: " + id);
        }
        return storage.remove(id) != null;
    }
}
