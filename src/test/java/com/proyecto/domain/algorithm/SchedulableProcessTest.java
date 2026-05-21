package com.proyecto.domain.algorithm;

import com.proyecto.domain.model.ProcessTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchedulableProcessTest {

    @Test
    void testConstructorAndGetters() {
        ProcessTask t = new ProcessTask(1L, 10, 5L, 15L);
        SchedulableProcess p = new SchedulableProcess(t);

        assertEquals(t, p.getTask());
        assertEquals(29.0, p.getEffectivePriority()); // 39.0 - 10 = 29.0
    }

    @Test
    void testSetEffectivePriority() {
        ProcessTask t = new ProcessTask(1L, 10, 5L, 15L);
        SchedulableProcess p = new SchedulableProcess(t);
        p.setEffectivePriority(45.5);
        assertEquals(45.5, p.getEffectivePriority());
    }

    @Test
    void testCompareToSameInstance() {
        ProcessTask t = new ProcessTask(1L, 10, 5L, 15L);
        SchedulableProcess p = new SchedulableProcess(t);
        assertEquals(0, p.compareTo(p));
    }

    @Test
    void testCompareToDifferentPriority() {
        ProcessTask t1 = new ProcessTask(1L, 10, 5L, 15L);
        ProcessTask t2 = new ProcessTask(2L, 20, 5L, 15L);
        SchedulableProcess p1 = new SchedulableProcess(t1); // 29.0
        SchedulableProcess p2 = new SchedulableProcess(t2); // 19.0

        assertTrue(p1.compareTo(p2) > 0);
        assertTrue(p2.compareTo(p1) < 0);
    }

    @Test
    void testCompareToSamePriorityDifferentArrival() {
        ProcessTask t1 = new ProcessTask(1L, 10, 2L, 15L);
        ProcessTask t2 = new ProcessTask(2L, 10, 5L, 15L);
        SchedulableProcess p1 = new SchedulableProcess(t1); // 29.0, arrival 2
        SchedulableProcess p2 = new SchedulableProcess(t2); // 29.0, arrival 5

        // Menor instante de llegada primero (desempate por arrivalTime ascendente) -> p1 es mayor/superior
        assertTrue(p1.compareTo(p2) > 0);
        assertTrue(p2.compareTo(p1) < 0);
    }

    @Test
    void testCompareToSamePrioritySameArrivalDifferentId() {
        ProcessTask t1 = new ProcessTask(1L, 10, 5L, 15L);
        ProcessTask t2 = new ProcessTask(2L, 10, 5L, 15L);
        SchedulableProcess p1 = new SchedulableProcess(t1); // 29.0, arrival 5, id 1
        SchedulableProcess p2 = new SchedulableProcess(t2); // 29.0, arrival 5, id 2

        // Menor identificador primero (desempate determinista por processId ascendente) -> p1 es mayor/superior
        assertTrue(p1.compareTo(p2) > 0);
        assertTrue(p2.compareTo(p1) < 0);
    }

    @Test
    void testEqualsAndHashCode() {
        ProcessTask t1 = new ProcessTask(1L, 10, 5L, 15L);
        ProcessTask t2 = new ProcessTask(1L, 20, 10L, 25L);
        ProcessTask t3 = new ProcessTask(3L, 10, 5L, 15L);

        SchedulableProcess p1 = new SchedulableProcess(t1);
        SchedulableProcess p2 = new SchedulableProcess(t2);
        SchedulableProcess p3 = new SchedulableProcess(t3);

        assertEquals(p1, p1);
        assertEquals(p1, p2); // Same ID
        assertNotEquals(p1, p3); // Different ID
        assertNotEquals(p1, null);
        assertNotEquals(p1, "someString");

        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1.hashCode(), p3.hashCode());
    }

    @Test
    void testToString() {
        ProcessTask t = new ProcessTask(1L, 10, 5L, 15L);
        SchedulableProcess p = new SchedulableProcess(t);
        String s = p.toString();
        assertTrue(s.contains("id=1"));
        assertTrue(s.contains("effectivePriority=29.0"));
    }

    @Test
    void testNullValidation() {
        assertThrows(NullPointerException.class, () -> new SchedulableProcess(null));
    }
}
