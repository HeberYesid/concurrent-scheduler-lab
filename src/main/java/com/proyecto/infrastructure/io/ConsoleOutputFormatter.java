package com.proyecto.infrastructure.io;

import com.proyecto.domain.model.SchedulerMetrics;
import java.util.Locale;
import java.util.Objects;

/**
 * Formateador de salida para consola.
 *
 * <p>Toma un objeto {@link SchedulerMetrics} y lo presenta en formato ASCII
 * tabular altamente legible, formateando correctamente números y porcentajes.</p>
 *
 * @author scheduler-concurrente
 */
public final class ConsoleOutputFormatter {

    /**
     * Formatea las métricas del scheduler en una tabla ASCII limpia.
     *
     * @param metrics métricas a formatear, no debe ser null
     * @return representación en cadena de la tabla ASCII
     * @throws NullPointerException si metrics es null
     */
    public String format(SchedulerMetrics metrics) {
        Objects.requireNonNull(metrics, "Las métricas no pueden ser null");

        StringBuilder sb = new StringBuilder();
        sb.append("+------------------------------------------------------------+\n");
        sb.append("|            METRICAS DE RENDIMIENTO DEL SCHEDULER           |\n");
        sb.append("+------------------------------------------------------------+\n");
        sb.append(String.format(Locale.US, "| %-30s | %-23s |\n", "Métrica", "Valor"));
        sb.append("+------------------------------------------------------------+\n");
        sb.append(String.format(Locale.US, "| %-30s | %,23d |\n", "Procesos Atendidos", metrics.processedCount()));
        sb.append(String.format(Locale.US, "| %-30s | %,20d ms |\n", "Tiempo Total Simulación", metrics.totalElapsedTimeMs()));
        sb.append(String.format(Locale.US, "| %-30s | %,20.2f p/s |\n", "Throughput (Procesos/Seg)", metrics.throughput()));
        sb.append(String.format(Locale.US, "| %-30s | %,20.2f ms |\n", "Tiempo Espera Promedio", metrics.avgWaitTime()));
        sb.append(String.format(Locale.US, "| %-30s | %22.2f%% |\n", "Tasa de Inanición (Starvation)", metrics.starvationRate() * 100.0));
        sb.append("+------------------------------------------------------------+\n");
        return sb.toString();
    }
}
