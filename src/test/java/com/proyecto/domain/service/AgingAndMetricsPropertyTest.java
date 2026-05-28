package com.proyecto.domain.service;

import com.proyecto.domain.algorithm.AgingEngine;
import com.proyecto.domain.algorithm.LinearAgingCalculator;
import com.proyecto.domain.algorithm.MaxHeap;
import com.proyecto.domain.algorithm.SchedulableProcess;
import com.proyecto.domain.model.ExecutionRecord;
import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.SchedulerConfig;
import com.proyecto.domain.model.SchedulerMetrics;
import com.proyecto.domain.service.impl.StreamingMetrics;
import java.util.ArrayList;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.Size;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgingAndMetricsPropertyTest {

    @Property(seed = "2026052701")
    void agingPreservesMaxHeapInvariant(
            @ForAll("agingConfigs") SchedulerConfig config,
            @ForAll("processLists") List<ProcessTask> processes
    ) {
        MaxHeap<SchedulableProcess> heap = new MaxHeap<>();
        for (ProcessTask task : processes) {
            heap.insert(new SchedulableProcess(task));
        }

        AgingEngine engine = new AgingEngine(new LinearAgingCalculator());
        long agingTime = processes.stream()
                .mapToLong(ProcessTask::arrivalTime)
                .max()
                .orElse(0L) + config.agingInterval() * 2;

        engine.applyAging(heap, agingTime, config);

        verifyHeapInvariant(heap);
    }

    @Property(seed = "2026052702")
    void streamingMetricsAreAlwaysValid(
            @ForAll @Size(max = 200) List<@LongRange(min = 1, max = 100_000) Long> waitTimes,
            @ForAll @LongRange(min = 1, max = 5_000) long maxAcceptableWait,
            @ForAll @LongRange(min = 0, max = 1_000_000) long totalElapsedTime
    ) {
        StreamingMetrics metrics = new StreamingMetrics(maxAcceptableWait);
        for (int i = 0; i < waitTimes.size(); i++) {
            metrics.recordExecution(new ExecutionRecord(i + 1, 0L, waitTimes.get(i), waitTimes.get(i)));
        }

        SchedulerMetrics result = metrics.computeMetrics(totalElapsedTime);
        double epsilon = 1e-9;

        assertTrue(result.throughput() >= 0.0);
        assertTrue(result.avgWaitTime() >= 0.0);
        assertTrue(result.starvationRate() >= 0.0);
        assertTrue(result.starvationRate() <= 1.0 + epsilon);
        assertTrue(result.processedCount() >= 0);
        assertTrue(result.totalElapsedTimeMs() >= 0);

        assertEquals(waitTimes.size(), result.processedCount());

        if (waitTimes.isEmpty()) {
            assertEquals(0.0, result.throughput(), epsilon);
            assertEquals(0.0, result.avgWaitTime(), epsilon);
            assertEquals(0.0, result.starvationRate(), epsilon);
        }
    }

    @Property(seed = "2026052703")
    void starvationRateMatchesDefinition(
            @ForAll @Size(max = 150) List<@LongRange(min = 1, max = 10_000) Long> waitTimes
    ) {
        long maxAcceptableWait = 500;
        StreamingMetrics metrics = new StreamingMetrics(maxAcceptableWait);

        long expectedStarved = waitTimes.stream()
                .filter(w -> w > maxAcceptableWait)
                .count();

        for (int i = 0; i < waitTimes.size(); i++) {
            metrics.recordExecution(new ExecutionRecord(i + 1, 0L, waitTimes.get(i), waitTimes.get(i)));
        }

        SchedulerMetrics result = metrics.computeMetrics(60_000L);

        double expectedRate = waitTimes.isEmpty()
                ? 0.0
                : (double) expectedStarved / waitTimes.size();

        assertEquals(expectedRate, result.starvationRate(), 1e-9);
    }

    @Provide
    Arbitrary<SchedulerConfig> agingConfigs() {
        return Arbitraries.of(
                new SchedulerConfig(0.5, 100, 5000),
                new SchedulerConfig(0.1, 50, 1000),
                new SchedulerConfig(1.0, 500, 10000)
        );
    }

    @Provide
    Arbitrary<List<ProcessTask>> processLists() {
        return Arbitraries.longs().between(1, 10_000)
                .flatMap(arrivalTime ->
                        Arbitraries.integers().between(0, 39)
                                .flatMap(priority ->
                                        Arbitraries.longs().between(1, 5000)
                                                .map(burstTime ->
                                                        new ProcessTask(
                                                                Math.abs(arrivalTime.hashCode()) % 10000 + 1,
                                                                priority,
                                                                arrivalTime,
                                                                burstTime
                                                        )
                                                )
                                )
                )
                .list().ofMinSize(1).ofMaxSize(100);
    }

    private <T extends Comparable<T>> void verifyHeapInvariant(MaxHeap<T> heap) {
        int size = heap.size();
        for (int i = 0; i < size; i++) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;

            if (left < size) {
                assertTrue(heap.get(i).compareTo(heap.get(left)) >= 0,
                        "Invariante de heap violado: padre (" + heap.get(i)
                                + ") es menor que hijo izquierdo (" + heap.get(left) + ")");
            }
            if (right < size) {
                assertTrue(heap.get(i).compareTo(heap.get(right)) >= 0,
                        "Invariante de heap violado: padre (" + heap.get(i)
                                + ") es menor que hijo derecho (" + heap.get(right) + ")");
            }
        }
    }
}
