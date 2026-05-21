package com.proyecto.domain.service.impl;

import com.proyecto.domain.model.ExecutionRecord;
import com.proyecto.domain.model.SchedulerMetrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StreamingMetricsTest {

    @Test
    void constructorAndRecorderRejectInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new StreamingMetrics(0L));

        StreamingMetrics metrics = new StreamingMetrics(10L);
        assertThrows(NullPointerException.class, () -> metrics.recordExecution(null));
    }

    @Test
    void computeMetricsReturnsZerosWhenNothingWasProcessed() {
        StreamingMetrics metrics = new StreamingMetrics(10L);

        SchedulerMetrics result = metrics.computeMetrics(25L);

        assertEquals(0.0, result.throughput());
        assertEquals(0.0, result.avgWaitTime());
        assertEquals(0.0, result.starvationRate());
        assertEquals(0, result.processedCount());
        assertEquals(25L, result.totalElapsedTimeMs());
    }

    @Test
    void computeMetricsHandlesZeroElapsedTimeAndStarvationRate() {
        StreamingMetrics metrics = new StreamingMetrics(100L);
        metrics.recordExecution(new ExecutionRecord(1L, 0L, 10L, 50L));
        metrics.recordExecution(new ExecutionRecord(2L, 10L, 20L, 150L));

        SchedulerMetrics result = metrics.computeMetrics(0L);

        assertEquals(0.0, result.throughput());
        assertEquals(100.0, result.avgWaitTime());
        assertEquals(0.5, result.starvationRate());
        assertEquals(2, result.processedCount());
        assertEquals(0L, result.totalElapsedTimeMs());
    }
}
