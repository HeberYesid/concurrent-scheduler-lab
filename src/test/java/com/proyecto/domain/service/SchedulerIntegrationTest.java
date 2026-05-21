package com.proyecto.domain.service;

import com.proyecto.domain.algorithm.LinearAgingCalculator;
import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.Result;
import com.proyecto.domain.model.SchedulerConfig;
import com.proyecto.domain.model.SchedulerError;
import com.proyecto.domain.model.SchedulerMetrics;
import com.proyecto.domain.service.impl.AgingSchedulerService;
import com.proyecto.infrastructure.io.CsvProcessParser;
import com.proyecto.infrastructure.persistence.InMemoryProcessRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración de punta a punta del planificador concurrentemente simulado.
 *
 * @author scheduler-concurrente
 */
public class SchedulerIntegrationTest {

    @Test
    void testInMemoryProcessRepository() {
        ProcessRepository repo = new InMemoryProcessRepository();
        ProcessTask p1 = new ProcessTask(1L, 20, 0L, 50L);
        ProcessTask p2 = new ProcessTask(2L, 10, 10L, 30L);

        repo.save(p1);
        repo.save(p2);

        assertEquals(2, repo.findAll().size());
        assertThrows(IllegalArgumentException.class, () -> repo.save(p1)); // Duplicado

        assertTrue(repo.deleteById(1L));
        assertEquals(1, repo.findAll().size());
        assertFalse(repo.deleteById(1L)); // Ya no existe
        assertThrows(IllegalArgumentException.class, () -> repo.deleteById(0L));
    }

    @Test
    void testCsvProcessParser() {
        CsvProcessParser parser = new CsvProcessParser();
        String csv = "id,basePriority,arrivalTime,burstTime\n"
                   + "1,20,100,500\n"
                   + "  2 , 10 , 200 , 300 \n"
                   + "# comentario línea\n"
                   + "3,39,300,100";

        List<ProcessTask> tasks = parser.parse(csv);
        assertEquals(3, tasks.size());
        assertEquals(1L, tasks.get(0).id());
        assertEquals(20, tasks.get(0).basePriority());
        assertEquals(100L, tasks.get(0).arrivalTime());
        assertEquals(500L, tasks.get(0).burstTime());

        assertEquals(2L, tasks.get(1).id());
        assertEquals(10, tasks.get(1).basePriority());
        assertEquals(200L, tasks.get(1).arrivalTime());
        assertEquals(300L, tasks.get(1).burstTime());

        assertThrows(IllegalArgumentException.class, () -> parser.parse("invalid,csv,data"));
    }

    @Test
    void testSchedulerFailFastValidation() {
        SchedulerService scheduler = new AgingSchedulerService(new LinearAgingCalculator());
        SchedulerConfig config = SchedulerConfig.withDefaults();

        // Lista vacía
        Result<SchedulerMetrics, SchedulerError> r1 = scheduler.schedule(List.of(), config);
        assertTrue(r1.isErr());
        assertEquals(SchedulerError.EMPTY_PROCESS_LIST, r1.getError());

        // Configuración nula
        Result<SchedulerMetrics, SchedulerError> r2 = scheduler.schedule(
                List.of(new ProcessTask(1L, 20, 0L, 100L)), null);
        assertTrue(r2.isErr());
        assertEquals(SchedulerError.INVALID_CONFIGURATION, r2.getError());

        // ID duplicado
        Result<SchedulerMetrics, SchedulerError> r3 = scheduler.schedule(
                List.of(
                        new ProcessTask(1L, 20, 0L, 100L),
                        new ProcessTask(1L, 10, 50L, 200L)
                ),
                config
        );
        assertTrue(r3.isErr());
        assertEquals(SchedulerError.DUPLICATE_PROCESS_ID, r3.getError());
    }

    @Test
    void testSchedulerSimulationCorrectness() {
        SchedulerService scheduler = new AgingSchedulerService(new LinearAgingCalculator());
        // maxAcceptableWait = 100 ms para ver inanición
        SchedulerConfig config = new SchedulerConfig(0.5, 10, 100);

        List<ProcessTask> processes = List.of(
                new ProcessTask(1L, 30, 0L, 200L), // Ejecuta primero a t=0. Termina a t=200
                new ProcessTask(2L, 10, 10L, 50L), // Llega a t=10. Espera en cola
                new ProcessTask(3L, 35, 20L, 100L)  // Llega a t=20. Espera en cola
        );

        Result<SchedulerMetrics, SchedulerError> result = scheduler.schedule(processes, config);
        assertTrue(result.isOk());
        SchedulerMetrics metrics = result.getValue();

        assertEquals(3, metrics.processedCount());
        // t_total = 350 ms
        assertEquals(350L, metrics.totalElapsedTimeMs());
        // wait_t1 = 0
        // wait_t2 (t=200 start) = 190 ms (starved! wait > 100)
        // wait_t3 (t=250 start) = 230 ms (starved! wait > 100)
        // avg_wait = (0 + 190 + 230) / 3 = 140 ms
        assertEquals(140.0, metrics.avgWaitTime(), 0.001);
        assertEquals(2.0 / 3.0, metrics.starvationRate(), 0.001);
        
        // throughput = 3 procesos / 0.35 segundos = 8.5714 procesos/segundo
        assertEquals(3.0 / 0.35, metrics.throughput(), 0.001);
    }
}
