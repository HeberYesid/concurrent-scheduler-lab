package com.proyecto.benchmark;

import com.proyecto.domain.algorithm.AgingEngine;
import com.proyecto.domain.algorithm.LinearAgingCalculator;
import com.proyecto.domain.algorithm.MaxHeap;
import com.proyecto.domain.algorithm.SchedulableProcess;
import com.proyecto.domain.model.ExecutionRecord;
import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.SchedulerConfig;
import com.proyecto.domain.model.Result;
import com.proyecto.domain.model.SchedulerError;
import com.proyecto.domain.model.SchedulerMetrics;
import com.proyecto.domain.model.SchedulerRun;
import com.proyecto.domain.service.impl.AgingSchedulerService;
import com.proyecto.domain.service.impl.StreamingMetrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
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
@Fork(2)
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
    @Setup(Level.Iteration)
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
     * Mide un baseline con {@link PriorityQueue} estándar y prioridad estática.
     *
     * <p>Este baseline elimina el costo del aging para ofrecer un punto de comparación
     * directo contra el scheduler principal, mientras que el costo del aging se mide
     * de forma aislada en {@link #benchmarkAgingRebuild(Blackhole)}.</p>
     *
     * @param bh blackhole para consumir el resultado
     */
    @Benchmark
    public void benchmarkBaselineCycle(Blackhole bh) {
        Result<SchedulerRun, SchedulerError> result = runBaselineScheduler(tasks, config);
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
        agingEngine.applyAging(heap, computeSafeBenchmarkTime(), config);
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

    private long computeSafeBenchmarkTime() {
        long maxArrivalTime = 0L;
        for (ProcessTask task : tasks) {
            if (task.arrivalTime() > maxArrivalTime) {
                maxArrivalTime = task.arrivalTime();
            }
        }
        return maxArrivalTime + config.agingInterval();
    }

    private Result<SchedulerRun, SchedulerError> runBaselineScheduler(
            List<ProcessTask> processes,
            SchedulerConfig schedulerConfig
    ) {
        if (processes == null || processes.isEmpty()) {
            return Result.err(SchedulerError.EMPTY_PROCESS_LIST);
        }

        List<ProcessTask> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingLong(ProcessTask::arrivalTime));
        PriorityQueue<SchedulableProcess> readyQueue =
                new PriorityQueue<>(Collections.reverseOrder());
        List<ExecutionRecord> executionTrace = new ArrayList<>();
        StreamingMetrics metrics = new StreamingMetrics(schedulerConfig.maxAcceptableWait());

        long now = 0L;
        int nextIndex = 0;
        while (nextIndex < sorted.size() || !readyQueue.isEmpty()) {
            if (readyQueue.isEmpty() && nextIndex < sorted.size()) {
                now = Math.max(now, sorted.get(nextIndex).arrivalTime());
            }

            while (nextIndex < sorted.size() && sorted.get(nextIndex).arrivalTime() <= now) {
                readyQueue.add(toStaticPriorityProcess(sorted.get(nextIndex)));
                nextIndex++;
            }

            SchedulableProcess nextProcess = readyQueue.remove();

            ProcessTask task = nextProcess.getTask();
            long startTime = now;
            long endTime = startTime + task.burstTime();
            long waitTime = startTime - task.arrivalTime();
            ExecutionRecord record = new ExecutionRecord(task.id(), startTime, endTime, waitTime);
            executionTrace.add(record);
            metrics.recordExecution(record);
            now = endTime;
        }

        SchedulerMetrics schedulerMetrics = metrics.computeMetrics(now);
        return Result.ok(new SchedulerRun(schedulerMetrics, executionTrace));
    }

    private SchedulableProcess toStaticPriorityProcess(ProcessTask task) {
        SchedulableProcess process = new SchedulableProcess(task);
        process.setEffectivePriority(task.basePriority());
        return process;
    }
}
