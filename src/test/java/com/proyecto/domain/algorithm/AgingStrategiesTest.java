package com.proyecto.domain.algorithm;

import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.SchedulerConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para las estrategias de cálculo de prioridad y el AgingEngine.
 *
 * @author scheduler-concurrente
 */
public class AgingStrategiesTest {

    @Test
    void testLinearAgingCalculator() {
        PriorityCalculator calculator = new LinearAgingCalculator();
        ProcessTask process = new ProcessTask(1L, 20, 1000L, 50L);
        SchedulerConfig config = new SchedulerConfig(0.5, 100, 3000);

        // Sin espera
        double priorityAtArrival = calculator.calculate(process, 1000L, config);
        assertEquals(19.0, priorityAtArrival); // (39 - 20) + 0.5 * 0

        // Con espera de 100 ms
        double priorityAfterWait = calculator.calculate(process, 1100L, config);
        assertEquals(69.0, priorityAfterWait); // 19.0 + 0.5 * 100

        // Validaciones de contrato
        assertThrows(NullPointerException.class, () -> calculator.calculate(null, 1000L, config));
        assertThrows(NullPointerException.class, () -> calculator.calculate(process, 1000L, null));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(process, 500L, config));
    }

    @Test
    void testExponentialAgingCalculator() {
        PriorityCalculator calculator = new ExponentialAgingCalculator();
        ProcessTask process = new ProcessTask(1L, 20, 1000L, 50L);
        SchedulerConfig config = new SchedulerConfig(0.5, 100, 3000);

        // Sin espera
        double priorityAtArrival = calculator.calculate(process, 1000L, config);
        assertEquals(19.0, priorityAtArrival);

        // Con espera de 3 ms -> waitTime = 3, log2(1 + 3) = log2(4) = 2.0
        double priorityAfterWait = calculator.calculate(process, 1003L, config);
        assertEquals(20.0, priorityAfterWait); // 19.0 + 0.5 * 2.0

        // Validaciones de contrato
        assertThrows(NullPointerException.class, () -> calculator.calculate(null, 1000L, config));
        assertThrows(NullPointerException.class, () -> calculator.calculate(process, 1000L, null));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(process, 500L, config));
    }

    @Test
    void testStepAgingCalculator() {
        PriorityCalculator calculator = new StepAgingCalculator();
        ProcessTask process = new ProcessTask(1L, 20, 1000L, 50L);
        SchedulerConfig config = new SchedulerConfig(0.5, 100, 3000); // interval = 100

        // Sin espera
        double priorityAtArrival = calculator.calculate(process, 1000L, config);
        assertEquals(19.0, priorityAtArrival);

        // Con espera de 99 ms (no alcanza el escalón de 100)
        double priorityBeforeStep = calculator.calculate(process, 1099L, config);
        assertEquals(19.0, priorityBeforeStep);

        // Con espera de 100 ms (primer escalón)
        double priorityAtStep = calculator.calculate(process, 1100L, config);
        assertEquals(19.5, priorityAtStep); // 19.0 + 0.5 * 1.0

        // Con espera de 250 ms (segundo escalón)
        double priorityAtSecondStep = calculator.calculate(process, 1250L, config);
        assertEquals(20.0, priorityAtSecondStep); // 19.0 + 0.5 * 2.0

        // Validaciones de contrato
        assertThrows(NullPointerException.class, () -> calculator.calculate(null, 1000L, config));
        assertThrows(NullPointerException.class, () -> calculator.calculate(process, 1000L, null));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(process, 500L, config));
    }

    @Test
    void testSchedulableProcessComparison() {
        ProcessTask t1 = new ProcessTask(1L, 20, 1000L, 50L);
        ProcessTask t2 = new ProcessTask(2L, 20, 1000L, 50L);
        ProcessTask t3 = new ProcessTask(3L, 20, 500L, 50L);

        SchedulableProcess p1 = new SchedulableProcess(t1);
        SchedulableProcess p2 = new SchedulableProcess(t2);
        SchedulableProcess p3 = new SchedulableProcess(t3);

        // Por prioridad efectiva
        p1.setEffectivePriority(25.0);
        p2.setEffectivePriority(30.0);
        assertTrue(p1.compareTo(p2) < 0); // p2 (30.0) es mayor en compareTo que p1 (25.0)

        // Empate de prioridad efectiva, decide tiempo de llegada (FIFO)
        p1.setEffectivePriority(30.0);
        p3.setEffectivePriority(30.0);
        // p3 llegó antes (500L) que p1 (1000L), por tanto p3 tiene mayor urgencia (es mayor)
        assertTrue(p1.compareTo(p3) < 0);

        // Empate de prioridad y llegada, decide ID del proceso
        // p1 (ID 1) tiene menor ID que p2 (ID 2), por tanto p1 tiene mayor urgencia (es mayor)
        assertTrue(p1.compareTo(p2) > 0);
    }

    @Test
    void testAgingEngineApplyAging() {
        PriorityCalculator calculator = new LinearAgingCalculator();
        AgingEngine engine = new AgingEngine(calculator);
        
        MaxHeap<SchedulableProcess> heap = new MaxHeap<>();
        ProcessTask t1 = new ProcessTask(1L, 30, 1000L, 50L);
        ProcessTask t2 = new ProcessTask(2L, 10, 1000L, 50L);
        
        heap.insert(new SchedulableProcess(t1));
        heap.insert(new SchedulableProcess(t2));
        
        SchedulerConfig config = new SchedulerConfig(0.5, 100, 3000);
        
        // Aplicamos aging a los 1200 ms
        engine.applyAging(heap, 1200L, config);
        
        // p1: base = 9 (39 - 30) + 0.5 * 200 = 109
        // p2: base = 29 (39 - 10) + 0.5 * 200 = 129
        assertEquals(129.0, heap.get(0).getEffectivePriority());
        assertEquals(109.0, heap.get(1).getEffectivePriority());
    }
}
