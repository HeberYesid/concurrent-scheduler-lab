package com.proyecto.domain.algorithm;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de caja negra y caja blanca para {@link MaxHeap}
 * garantizando la robustez ante casos de borde y el manejo de excepciones.
 *
 * @author scheduler-concurrente
 */
public class MaxHeapUnitTest {

    @Test
    void testEmptyHeapBehavior() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        assertEquals(0, heap.size());
        assertTrue(heap.peekMax().isEmpty());
        assertTrue(heap.extractMax().isEmpty());
    }

    @Test
    void testInvalidInitialCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new MaxHeap<>(0));
        assertThrows(IllegalArgumentException.class, () -> new MaxHeap<>(-5));
    }

    @Test
    void testNullInsertion() {
        MaxHeap<String> heap = new MaxHeap<>();
        assertThrows(NullPointerException.class, () -> heap.insert(null));
    }

    @Test
    void testOutOfBoundsAccess() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        assertThrows(IndexOutOfBoundsException.class, () -> heap.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> heap.get(-1));

        heap.insert(10);
        assertEquals(10, heap.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> heap.get(1));
    }

    @Test
    void testHeapResizingAndSorting() {
        // Forzamos resizing insertando más del tamaño inicial por defecto
        MaxHeap<Integer> heap = new MaxHeap<>(2);
        heap.insert(15);
        heap.insert(40);
        heap.insert(10);
        heap.insert(30);
        heap.insert(50);

        assertEquals(5, heap.size());
        assertEquals(50, heap.peekMax().orElseThrow());

        assertEquals(50, heap.extractMax().orElseThrow());
        assertEquals(40, heap.extractMax().orElseThrow());
        assertEquals(30, heap.extractMax().orElseThrow());
        assertEquals(15, heap.extractMax().orElseThrow());
        assertEquals(10, heap.extractMax().orElseThrow());
        assertEquals(0, heap.size());
    }
}
