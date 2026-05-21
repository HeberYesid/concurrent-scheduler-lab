package com.proyecto.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainModelValidationTest {

    @Test
    void processTaskRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new ProcessTask(0L, 10, 0L, 10L));
        assertThrows(IllegalArgumentException.class, () -> new ProcessTask(1L, -1, 0L, 10L));
        assertThrows(IllegalArgumentException.class, () -> new ProcessTask(1L, 40, 0L, 10L));
        assertThrows(IllegalArgumentException.class, () -> new ProcessTask(1L, 10, -1L, 10L));
        assertThrows(IllegalArgumentException.class, () -> new ProcessTask(1L, 10, 0L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new ProcessTask(1L, 10, 0L, 60_001L));
    }

    @Test
    void schedulerConfigRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new SchedulerConfig(0.0, 100L, 1_000L));
        assertThrows(IllegalArgumentException.class, () -> new SchedulerConfig(1.1, 100L, 1_000L));
        assertThrows(IllegalArgumentException.class, () -> new SchedulerConfig(0.5, 9L, 1_000L));
        assertThrows(IllegalArgumentException.class, () -> new SchedulerConfig(0.5, 5_001L, 1_000L));
        assertThrows(IllegalArgumentException.class, () -> new SchedulerConfig(0.5, 100L, 0L));
    }

    @Test
    void schedulerMetricsRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new SchedulerMetrics(-1.0, 0.0, 0.0, 0, 0L));
        assertThrows(IllegalArgumentException.class, () -> new SchedulerMetrics(1.0, -1.0, 0.0, 0, 0L));
        assertThrows(IllegalArgumentException.class, () -> new SchedulerMetrics(1.0, 0.0, -0.1, 0, 0L));
        assertThrows(IllegalArgumentException.class, () -> new SchedulerMetrics(1.0, 0.0, 1.1, 0, 0L));
        assertThrows(IllegalArgumentException.class, () -> new SchedulerMetrics(1.0, 0.0, 0.5, -1, 0L));
        assertThrows(IllegalArgumentException.class, () -> new SchedulerMetrics(1.0, 0.0, 0.5, 1, -1L));
    }

    @Test
    void executionRecordRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new ExecutionRecord(0L, 0L, 1L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionRecord(1L, 10L, 9L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionRecord(1L, 0L, 1L, -1L));
    }

    @Test
    void withDefaultsCreatesValidConfiguration() {
        SchedulerConfig config = SchedulerConfig.withDefaults();

        assertEquals(SchedulerConfig.DEFAULT_AGING_FACTOR, config.agingFactor());
        assertEquals(SchedulerConfig.DEFAULT_AGING_INTERVAL, config.agingInterval());
        assertEquals(SchedulerConfig.DEFAULT_MAX_ACCEPTABLE_WAIT, config.maxAcceptableWait());
    }
}
