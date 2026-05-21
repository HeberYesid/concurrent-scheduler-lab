package com.proyecto.infrastructure.io;

import com.proyecto.domain.model.ProcessTask;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Analizador/Parser de archivos o cadenas en formato CSV.
 *
 * <p>Lee líneas en el formato {@code id,basePriority,arrivalTime,burstTime}
 * y las transforma en una lista inmutable de {@link ProcessTask}.
 * Soporta de forma robusta la omisión de cabeceras, líneas vacías y comentarios.</p>
 *
 * @author scheduler-concurrente
 */
public final class CsvProcessParser {

    /**
     * Parsea una cadena de texto conteniendo múltiples líneas CSV.
     *
     * @param csvContent contenido del CSV completo, no debe ser null
     * @return lista de tareas procesadas
     * @throws NullPointerException si csvContent es null
     * @throws IllegalArgumentException si alguna línea tiene formato incorrecto o valores fuera de límite
     */
    public List<ProcessTask> parse(String csvContent) {
        Objects.requireNonNull(csvContent, "El contenido CSV no puede ser null");
        List<ProcessTask> tasks = new ArrayList<>();
        String[] lines = csvContent.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (shouldSkipLine(trimmed)) {
                continue;
            }
            tasks.add(parseLine(trimmed));
        }
        return tasks;
    }

    /**
     * Parsea una lista de líneas CSV.
     *
     * @param lines lista de líneas individuales, no debe ser null
     * @return lista de tareas procesadas
     * @throws NullPointerException si lines es null
     */
    public List<ProcessTask> parseLines(List<String> lines) {
        Objects.requireNonNull(lines, "La lista de líneas no puede ser null");
        List<ProcessTask> tasks = new ArrayList<>();
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            String trimmed = line.trim();
            if (shouldSkipLine(trimmed)) {
                continue;
            }
            tasks.add(parseLine(trimmed));
        }
        return tasks;
    }

    // ── Métodos auxiliares para mantener V(G) <= 10 ──────────────────

    private boolean shouldSkipLine(String trimmedLine) {
        return trimmedLine.isEmpty() 
                || trimmedLine.startsWith("#") 
                || trimmedLine.startsWith("id") 
                || trimmedLine.startsWith("ID");
    }

    private ProcessTask parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 4) {
            throw new IllegalArgumentException(
                    "Formato de línea CSV inválido, se esperaban 4 columnas: " + line);
        }

        try {
            long id = Long.parseLong(parts[0].trim());
            int basePriority = Integer.parseInt(parts[1].trim());
            long arrivalTime = Long.parseLong(parts[2].trim());
            long burstTime = Long.parseLong(parts[3].trim());
            return new ProcessTask(id, basePriority, arrivalTime, burstTime);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Error al parsear valores numéricos de la línea CSV: " + line, e);
        }
    }
}
