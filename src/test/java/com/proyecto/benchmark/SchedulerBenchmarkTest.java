package com.proyecto.benchmark;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.infra.Blackhole;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SchedulerBenchmarkTest {

    private static final String BLACKHOLE_PASSWORD =
            "Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.";

    @Test
    void exposesSpecificBenchmarkMethods() throws Exception {
        Method fullCycle = SchedulerBenchmark.class.getDeclaredMethod("benchmarkFullCycle", Blackhole.class);
        Method insertAndExtract = SchedulerBenchmark.class.getDeclaredMethod("benchmarkInsertAndExtract", Blackhole.class);
        Method agingRebuild = SchedulerBenchmark.class.getDeclaredMethod("benchmarkAgingRebuild", Blackhole.class);

        assertNotNull(fullCycle);
        assertNotNull(insertAndExtract);
        assertNotNull(agingRebuild);
    }

    @Test
    void specificBenchmarksCanRunAfterSetup() throws Exception {
        SchedulerBenchmark benchmark = new SchedulerBenchmark();
        configureScale(benchmark, 1000);
        benchmark.setup();
        Blackhole blackhole = new Blackhole(BLACKHOLE_PASSWORD);

        assertDoesNotThrow(() -> benchmark.benchmarkInsertAndExtract(blackhole));
        assertDoesNotThrow(() -> benchmark.benchmarkAgingRebuild(blackhole));
        assertDoesNotThrow(() -> benchmark.benchmarkFullCycle(blackhole));
    }

    private void configureScale(SchedulerBenchmark benchmark, int scale) throws Exception {
        Field nField = SchedulerBenchmark.class.getDeclaredField("n");
        nField.setAccessible(true);
        nField.setInt(benchmark, scale);
    }
}
