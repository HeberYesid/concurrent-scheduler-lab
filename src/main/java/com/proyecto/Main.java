package com.proyecto;

import com.proyecto.domain.algorithm.ExponentialAgingCalculator;
import com.proyecto.domain.algorithm.LinearAgingCalculator;
import com.proyecto.domain.algorithm.PriorityCalculator;
import com.proyecto.domain.algorithm.StepAgingCalculator;
import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.Result;
import com.proyecto.domain.model.SchedulerConfig;
import com.proyecto.domain.model.SchedulerError;
import com.proyecto.domain.model.SchedulerMetrics;
import com.proyecto.domain.service.SchedulerService;
import com.proyecto.domain.service.impl.AgingSchedulerService;
import com.proyecto.infrastructure.io.ConsoleOutputFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Entry point del sistema de planificación.
 *
 * <p>Demuestra y compara el comportamiento de los tres algoritmos de aging
 * (lineal, exponencial y step) utilizando factores de escala y cargas mixtas.</p>
 *
 * @author scheduler-concurrente
 */
public final class Main {

    private Main() {
        // Clase de utilidad no instanciable
    }

    /**
     * Punto de entrada principal.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("  INICIANDO SIMULACIONES COMPARATIVAS DE AGING SCHEDULER SYSTEM ");
        System.out.println("================================================================\n");

        int n = 20_000; // Escala por defecto
        long seed = 42L; // Semilla fija para reproducibilidad

        System.out.printf("Generando %d procesos mixtos (semilla = %d)...\n", n, seed);
        List<ProcessTask> tasks = generateMixProcesses(n, seed);

        SchedulerConfig config = new SchedulerConfig(0.5, 100, 3_000); // 3s max wait antes de inanición
        ConsoleOutputFormatter formatter = new ConsoleOutputFormatter();

        // 1. Simulación con Linear Aging
        System.out.println("\n>>> [1/3] Ejecutando simulación: LINEAR AGING Strategy");
        runSimulationAndPrint(tasks, config, new LinearAgingCalculator(), formatter);

        // 2. Simulación con Exponential Aging
        System.out.println("\n>>> [2/3] Ejecutando simulación: EXPONENTIAL AGING Strategy");
        runSimulationAndPrint(tasks, config, new ExponentialAgingCalculator(), formatter);

        // 3. Simulación con Step Aging
        System.out.println("\n>>> [3/3] Ejecutando simulación: STEP AGING Strategy");
        runSimulationAndPrint(tasks, config, new StepAgingCalculator(), formatter);
    }

    private static void runSimulationAndPrint(
            List<ProcessTask> tasks,
            SchedulerConfig config,
            PriorityCalculator calculator,
            ConsoleOutputFormatter formatter
    ) {
        SchedulerService scheduler = new AgingSchedulerService(calculator);
        long startSimTime = System.currentTimeMillis();
        Result<SchedulerMetrics, SchedulerError> result = scheduler.schedule(tasks, config);
        long endSimTime = System.currentTimeMillis();

        if (result.isOk()) {
            SchedulerMetrics metrics = result.getValue();
            System.out.println(formatter.format(metrics));
            System.out.printf("Simulación completada en %d ms reales de CPU.\n", (endSimTime - startSimTime));
        } else {
            System.err.println("Error en simulación: " + result.getError().getMessage());
        }
    }

    private static List<ProcessTask> generateMixProcesses(int n, long seed) {
        Random random = new Random(seed);
        List<ProcessTask> tasks = new ArrayList<>(n);

        for (int i = 1; i <= n; i++) {
            long id = i;
            // 70% procesos prioridad media-baja (10-39), 30% alta prioridad (0-9)
            int priority = random.nextDouble() < 0.3 
                    ? random.nextInt(10) 
                    : 10 + random.nextInt(30);

            // Llegadas dispersas en los primeros 10,000 ms
            long arrivalTime = random.nextInt(10_000);

            // Ráfagas de ejecución entre 10 ms y 200 ms
            long burstTime = 10 + random.nextInt(190);

            tasks.add(new ProcessTask(id, priority, arrivalTime, burstTime));
        }
        return tasks;
    }
}
