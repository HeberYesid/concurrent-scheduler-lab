package com.proyecto.domain.algorithm;

import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.SchedulerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandPatternTest {

    private MaxHeap<SchedulableProcess> heap;
    private CommandHistory history;

    @BeforeEach
    void setUp() {
        heap = new MaxHeap<>();
        history = new CommandHistory();
    }

    @Test
    void testCommandHistoryExecuteAndUndo() {
        ProcessTask t = new ProcessTask(1L, 10, 0L, 5L);
        SchedulableProcess p = new SchedulableProcess(t);
        SchedulerCommand insert = new InsertProcessCommand(heap, p);

        assertEquals(0, history.size());
        history.executeAndRecord(insert);

        assertEquals(1, history.size());
        assertEquals(1, heap.size());
        assertEquals(p, heap.peekMax().orElse(null));

        history.undoLast();
        assertEquals(0, history.size());
        assertEquals(0, heap.size());
    }

    @Test
    void testCommandHistoryUndoOnEmpty() {
        assertDoesNotThrow(() -> history.undoLast());
    }

    @Test
    void testInsertUndoWithoutExecuteDoesNothing() {
        InsertProcessCommand command = new InsertProcessCommand(
                heap,
                new SchedulableProcess(new ProcessTask(1L, 10, 0L, 5L))
        );

        assertDoesNotThrow(command::undo);
        assertEquals(0, heap.size());
    }

    @Test
    void testCommandHistoryClear() {
        ProcessTask t = new ProcessTask(1L, 10, 0L, 5L);
        SchedulableProcess p = new SchedulableProcess(t);
        history.executeAndRecord(new InsertProcessCommand(heap, p));
        assertEquals(1, history.size());

        history.clear();
        assertEquals(0, history.size());
    }

    @Test
    void testExtractMaxCommand() {
        ProcessTask t1 = new ProcessTask(1L, 10, 0L, 5L);
        ProcessTask t2 = new ProcessTask(2L, 20, 0L, 5L);
        SchedulableProcess p1 = new SchedulableProcess(t1);
        SchedulableProcess p2 = new SchedulableProcess(t2);

        // p1 priority = 39 - 10 = 29
        // p2 priority = 39 - 20 = 19
        heap.insert(p1);
        heap.insert(p2);

        ExtractMaxCommand extractCmd = new ExtractMaxCommand(heap);
        history.executeAndRecord(extractCmd);

        assertEquals(p1, extractCmd.getExtracted());
        assertEquals(1, heap.size());
        assertEquals(p2, heap.peekMax().orElse(null));

        history.undoLast();
        assertEquals(2, heap.size());
        assertEquals(p1, heap.peekMax().orElse(null));
    }

    @Test
    void testExtractMaxCommandEmptyHeap() {
        ExtractMaxCommand extractCmd = new ExtractMaxCommand(heap);
        history.executeAndRecord(extractCmd);
        assertNull(extractCmd.getExtracted());

        history.undoLast();
        assertEquals(0, heap.size());
    }

    @Test
    void testExtractUndoWithoutExecuteDoesNothing() {
        ExtractMaxCommand extractCmd = new ExtractMaxCommand(heap);

        assertDoesNotThrow(extractCmd::undo);
        assertEquals(0, heap.size());
    }

    @Test
    void testApplyAgingCommand() {
        ProcessTask t1 = new ProcessTask(1L, 10, 0L, 5L); // base priority 10, inverted = 29
        SchedulableProcess p1 = new SchedulableProcess(t1);
        heap.insert(p1);

        PriorityCalculator calculator = new LinearAgingCalculator();
        AgingEngine engine = new AgingEngine(calculator);
        SchedulerConfig config = new SchedulerConfig(1.0, 10L, 25L);

        // At time 10, wait time is 10. Linear aging = 29 + 1.0 * 10 = 39.0
        ApplyAgingCommand agingCmd = new ApplyAgingCommand(engine, heap, 10L, config);
        history.executeAndRecord(agingCmd);

        assertEquals(39.0, p1.getEffectivePriority());

        history.undoLast();
        assertEquals(29.0, p1.getEffectivePriority());
    }

    @Test
    void testApplyAgingUndoWithoutExecuteDoesNothing() {
        ApplyAgingCommand agingCmd = new ApplyAgingCommand(
                new AgingEngine(new LinearAgingCalculator()),
                heap,
                10L,
                new SchedulerConfig(1.0, 10L, 25L)
        );

        assertDoesNotThrow(agingCmd::undo);
        assertEquals(0, heap.size());
    }

    @Test
    void testCommandNullValidations() {
        assertThrows(NullPointerException.class, () -> new CommandHistory().executeAndRecord(null));
        assertThrows(NullPointerException.class, () -> new InsertProcessCommand(null, new SchedulableProcess(new ProcessTask(1L, 10, 0L, 5L))));
        assertThrows(NullPointerException.class, () -> new InsertProcessCommand(heap, null));
        assertThrows(NullPointerException.class, () -> new ExtractMaxCommand(null));
        assertThrows(NullPointerException.class, () -> new ApplyAgingCommand(null, heap, 0L, new SchedulerConfig(1.0, 10L, 10L)));
        assertThrows(NullPointerException.class, () -> new ApplyAgingCommand(new AgingEngine(new LinearAgingCalculator()), null, 0L, new SchedulerConfig(1.0, 10L, 10L)));
        assertThrows(NullPointerException.class, () -> new ApplyAgingCommand(new AgingEngine(new LinearAgingCalculator()), heap, 0L, null));
    }
}
