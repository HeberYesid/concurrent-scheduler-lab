package com.proyecto.benchmark;

import com.proyecto.domain.algorithm.AgingEngine;
import com.proyecto.domain.algorithm.LinearAgingCalculator;
import com.proyecto.domain.algorithm.MaxHeap;
import com.proyecto.domain.algorithm.SchedulableProcess;
import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.SchedulerConfig;
import com.proyecto.domain.model.Result;
import com.proyecto.domain.model.SchedulerError;
import com.proyecto.domain.model.SchedulerRun;
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
    private LinearAgingCalculator calculator;
    private AgingEngine agingEngine;

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
        calculator = new LinearAgingCalculator();
        agingEngine = new AgingEngine(calculator);
        schedulerService = new AgingSchedulerService(calculator);
    }

    /**
     * Mide el tiempo del ciclo de scheduling completo para N elementos.
     *
     * @param bh blackhole para consumir el resultado
     */
    @Benchmark
    public void benchmarkFullCycle(Blackhole bh) {
        Result<SchedulerRun, SchedulerError> result = schedulerService.schedule(tasks, config);
        bh.consume(result);
    }

    /**
     * Mide el costo combinado de insertar todos los procesos en el heap y extraerlos.
     *
     * @param bh blackhole para consumir resultados intermedios
     */
    @Benchmark
    public void benchmarkInsertAndExtract(Blackhole bh) {
        MaxHeap<SchedulableProcess> heap = new MaxHeap<>(Math.max(16, tasks.size()));
        for (ProcessTask task : tasks) {
            heap.insert(toSchedulableProcess(task));
        }
        while (heap.size() > 0) {
            bh.consume(heap.extractMax().orElseThrow());
        }
    }

    /**
     * Mide el costo de recalcular prioridades y reconstruir el heap.
     *
     * @param bh blackhole para consumir el estado final del heap
     */
    @Benchmark
    public void benchmarkAgingRebuild(Blackhole bh) {
        MaxHeap<SchedulableProcess> heap = new MaxHeap<>(buildSchedulableProcesses());
        agingEngine.applyAging(heap, 10_000L, config);
        bh.consume(heap.peekMax().orElse(null));
    }

    private List<SchedulableProcess> buildSchedulableProcesses() {
        List<SchedulableProcess> processes = new ArrayList<>(tasks.size());
        for (ProcessTask task : tasks) {
            processes.add(toSchedulableProcess(task));
        }
        return processes;
    }

    private SchedulableProcess toSchedulableProcess(ProcessTask task) {
        SchedulableProcess process = new SchedulableProcess(task);
        process.setEffectivePriority(calculator.calculate(task, task.arrivalTime(), config));
        return process;
    }
}
