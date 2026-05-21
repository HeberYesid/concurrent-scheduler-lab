package com.proyecto.benchmark;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SchedulerBenchmarkTest {

    private static final String BLACKHOLE_PASSWORD =
            "Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.";

    @Test
    void exposesSpecificBenchmarkMethods() throws Exception {
        Method fullCycle = SchedulerBenchmark.class.getDeclaredMethod("benchmarkFullCycle", Blackhole.class);
        Method baselineCycle = SchedulerBenchmark.class.getDeclaredMethod("benchmarkBaselineCycle", Blackhole.class);
        Method insertAndExtract = SchedulerBenchmark.class.getDeclaredMethod("benchmarkInsertAndExtract", Blackhole.class);
        Method agingRebuild = SchedulerBenchmark.class.getDeclaredMethod("benchmarkAgingRebuild", Blackhole.class);

        assertNotNull(fullCycle);
        assertNotNull(baselineCycle);
        assertNotNull(insertAndExtract);
        assertNotNull(agingRebuild);
    }

    @Test
    void benchmarkConfigurationMatchesRubric() throws Exception {
        Fork fork = SchedulerBenchmark.class.getAnnotation(Fork.class);
        Setup setup = SchedulerBenchmark.class.getDeclaredMethod("setup").getAnnotation(Setup.class);

        assertNotNull(fork);
        assertEquals(2, fork.value());
        assertNotNull(setup);
        assertEquals(Level.Iteration, setup.value());
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
        assertDoesNotThrow(() -> benchmark.benchmarkBaselineCycle(blackhole));
    }

    private void configureScale(SchedulerBenchmark benchmark, int scale) throws Exception {
        Field nField = SchedulerBenchmark.class.getDeclaredField("n");
        nField.setAccessible(true);
        nField.setInt(benchmark, scale);
    }
}
