package com.proyecto.benchmark;

import com.proyecto.domain.algorithm.LinearAgingCalculator;
import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.SchedulerConfig;
import com.proyecto.domain.model.SchedulerMetrics;
import com.proyecto.domain.model.Result;
import com.proyecto.domain.model.SchedulerError;
import com.proyecto.domain.service.impl.AgingSchedulerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmark JMH para evaluar empíricamente el rendimiento a escala del scheduler.
 *
 * <p>Mide el tiempo de ejecución promedio del ciclo completo de scheduling para
 * diferentes escalas de N (desde 1.000 hasta 500.000 procesos activos).</p>
 *
 * @author scheduler-concurrente
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class SchedulerBenchmark {

    @Param({"1000", "10000", "100000", "500000"})
    private int n;

    private List<ProcessTask> tasks;
    private SchedulerConfig config;
    private AgingSchedulerService schedulerService;

    /**
     * Prepara el entorno antes de cada benchmark.
     */
    @Setup
    public void setup() {
        Random random = new Random(42L);
        tasks = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            long id = i;
            int priority = random.nextInt(40);
            long arrivalTime = random.nextInt(Math.max(10, n / 10));
            long burstTime = 1 + random.nextInt(100);
            tasks.add(new ProcessTask(id, priority, arrivalTime, burstTime));
        }
        config = new SchedulerConfig(0.5, 100, 5000);
        schedulerService = new AgingSchedulerService(new LinearAgingCalculator());
    }

    /**
     * Mide el tiempo del ciclo de scheduling completo para N elementos.
     *
     * @param bh blackhole para consumir el resultado
     */
    @Benchmark
    public void benchmarkFullCycle(Blackhole bh) {
        Result<SchedulerMetrics, SchedulerError> result = schedulerService.schedule(tasks, config);
        bh.consume(result);
    }
}
