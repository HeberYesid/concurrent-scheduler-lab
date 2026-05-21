package com.proyecto.domain.algorithm;

import java.util.List;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas basadas en propiedades utilizando jqwik para verificar los
 * invariantes matemáticos del Max-Heap.
 *
 * @author scheduler-concurrente
 */
public class MaxHeapPropertyTest {

    @Property(seed = "2026052101")
    void heapPropertyIsMaintainedAfterInsertions(@ForAll("listOfIntegers") List<Integer> list) {
        MaxHeap<Integer> heap = new MaxHeap<>();
        for (Integer val : list) {
            heap.insert(val);
        }
        verifyHeapInvariant(heap);
    }

    @Property(seed = "2026052102")
    void floydHeapifyCreatesValidHeap(@ForAll("listOfIntegers") List<Integer> list) {
        MaxHeap<Integer> heap = new MaxHeap<>(list);
        verifyHeapInvariant(heap);
    }

    @Property(seed = "2026052103")
    void extractedElementsAreSortedDescending(@ForAll("listOfIntegers") List<Integer> list) {
        MaxHeap<Integer> heap = new MaxHeap<>(list);
        Integer last = null;
        while (heap.size() > 0) {
            Integer current = heap.extractMax().orElseThrow();
            if (last != null) {
                assertTrue(last.compareTo(current) >= 0,
                        "Los elementos no se extrajeron en orden descendente. Previo: " 
                                + last + ", Actual: " + current);
            }
            last = current;
        }
    }

    @Provide
    Arbitrary<List<Integer>> listOfIntegers() {
        return Arbitraries.integers().list().ofMinSize(0).ofMaxSize(150);
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
