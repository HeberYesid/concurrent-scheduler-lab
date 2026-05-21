package com.proyecto.domain.service.impl;

import com.proyecto.domain.algorithm.AgingEngine;
import com.proyecto.domain.algorithm.LinearAgingCalculator;
import com.proyecto.domain.algorithm.MaxHeap;
import com.proyecto.domain.algorithm.PriorityCalculator;
import com.proyecto.domain.algorithm.SchedulableProcess;
import com.proyecto.domain.model.ExecutionRecord;
import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.Result;
import com.proyecto.domain.model.SchedulerConfig;
import com.proyecto.domain.model.SchedulerError;
import com.proyecto.domain.model.SchedulerMetrics;
import com.proyecto.domain.model.SchedulerRun;
import com.proyecto.domain.service.SchedulerService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Implementación principal de {@link SchedulerService} que simula la ejecución
 * de procesos mediante un Max-Heap y un motor de envejecimiento (Aging).
 *
 * <p>Diseñada bajo principios de complejidad ciclomática PMD V(G) <= 10 por método.</p>
 *
 * @author scheduler-concurrente
 */
public final class AgingSchedulerService implements SchedulerService {

    private final PriorityCalculator calculator;

    /**
     * Crea un scheduler con envejecimiento lineal por defecto.
     */
    public AgingSchedulerService() {
        this.calculator = new LinearAgingCalculator();
    }

    /**
     * Crea un scheduler con una estrategia de cálculo de prioridad específica.
     *
     * @param calculator estrategia de prioridad a utilizar, no nula
     */
    public AgingSchedulerService(PriorityCalculator calculator) {
        this.calculator = Objects.requireNonNull(calculator, "calculator no puede ser nula");
    }

    @Override
    public Result<SchedulerRun, SchedulerError> schedule(List<ProcessTask> processes, SchedulerConfig config) {
        if (processes == null || processes.isEmpty()) {
            return Result.err(SchedulerError.EMPTY_PROCESS_LIST);
        }
        if (config == null) {
            return Result.err(SchedulerError.INVALID_CONFIGURATION);
        }
        if (hasDuplicateIds(processes)) {
            return Result.err(SchedulerError.DUPLICATE_PROCESS_ID);
        }

        List<ProcessTask> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingLong(ProcessTask::arrivalTime));

        StreamingMetrics metrics = new StreamingMetrics(config.maxAcceptableWait());
        SimulationOutcome outcome = runSimulation(sorted, config, metrics);
        SchedulerMetrics schedulerMetrics = metrics.computeMetrics(outcome.finalTime());

        return Result.ok(new SchedulerRun(schedulerMetrics, outcome.executionTrace()));
    }

    // ── Helper Methods (Complejidad Ciclomática <= 10) ──────────────

    private boolean hasDuplicateIds(List<ProcessTask> processes) {
        Set<Long> ids = new HashSet<>();
        for (ProcessTask p : processes) {
            if (!ids.add(p.id())) {
                return true;
            }
        }
        return false;
    }

    private SimulationOutcome runSimulation(List<ProcessTask> sorted, SchedulerConfig config, StreamingMetrics metrics) {
        long now = 0;
        int nextIndex = 0;
        long lastAgingTime = 0;
        MaxHeap<SchedulableProcess> heap = new MaxHeap<>();
        List<ExecutionRecord> executionTrace = new ArrayList<>();

        while (nextIndex < sorted.size() || heap.size() > 0) {
            now = adjustLogicalClock(heap, sorted, nextIndex, now);
            nextIndex = insertArrivedProcesses(heap, sorted, nextIndex, now, config);
            lastAgingTime = applyAgingIfRequired(heap, now, lastAgingTime, config);
            now = executeNextProcess(heap, now, metrics, executionTrace);
        }
        return new SimulationOutcome(now, executionTrace);
    }

    private long adjustLogicalClock(MaxHeap<SchedulableProcess> heap, List<ProcessTask> sorted, int index, long now) {
        if (heap.size() == 0 && index < sorted.size()) {
            long nextArrival = sorted.get(index).arrivalTime();
            if (nextArrival > now) {
                return nextArrival;
            }
        }
        return now;
    }

    private int insertArrivedProcesses(
            MaxHeap<SchedulableProcess> heap,
            List<ProcessTask> sorted,
            int index,
            long now,
            SchedulerConfig config
    ) {
        int nextIndex = index;
        while (nextIndex < sorted.size() && sorted.get(nextIndex).arrivalTime() <= now) {
            ProcessTask task = sorted.get(nextIndex);
            SchedulableProcess sp = new SchedulableProcess(task);
            sp.setEffectivePriority(calculator.calculate(task, now, config));
            heap.insert(sp);
            nextIndex++;
        }
        return nextIndex;
    }

    private long applyAgingIfRequired(
            MaxHeap<SchedulableProcess> heap,
            long now,
            long lastAgingTime,
            SchedulerConfig config
    ) {
        if (now - lastAgingTime >= config.agingInterval()) {
            AgingEngine engine = new AgingEngine(calculator);
            engine.applyAging(heap, now, config);
            return now;
        }
        return lastAgingTime;
    }

    private long executeNextProcess(
            MaxHeap<SchedulableProcess> heap,
            long now,
            StreamingMetrics metrics,
            List<ExecutionRecord> executionTrace
    ) {
        Optional<SchedulableProcess> nextOpt = heap.extractMax();
        if (nextOpt.isPresent()) {
            SchedulableProcess sp = nextOpt.get();
            ProcessTask task = sp.getTask();
            long startTime = now;
            long endTime = startTime + task.burstTime();
            long waitTime = startTime - task.arrivalTime();
            ExecutionRecord record = new ExecutionRecord(task.id(), startTime, endTime, waitTime);
            metrics.recordExecution(record);
            executionTrace.add(record);
            return endTime;
        }
        return now;
    }

    private record SimulationOutcome(long finalTime, List<ExecutionRecord> executionTrace) {
    }
}
