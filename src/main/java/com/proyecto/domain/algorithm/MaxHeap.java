package com.proyecto.domain.algorithm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Cola de prioridad genérica implementada como un Binary Max-Heap sobre un array contiguo.
 *
 * <p>Mantiene la propiedad de Max-Heap (el padre es mayor o igual que sus hijos)
 * y soporta reconstrucción en tiempo O(n) mediante el algoritmo de Floyd.</p>
 *
 * @param <T> tipo de los elementos, debe implementar {@link Comparable}
 * @author scheduler-concurrente
 */
public final class MaxHeap<T extends Comparable<T>> {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    private Object[] queue;
    private int size;

    /**
     * Crea un heap vacío con capacidad inicial por defecto.
     */
    public MaxHeap() {
        this.queue = new Object[DEFAULT_INITIAL_CAPACITY];
        this.size = 0;
    }

    /**
     * Crea un heap vacío con la capacidad inicial especificada.
     *
     * @param initialCapacity capacidad inicial del array interno, debe ser {@code > 0}
     * @throws IllegalArgumentException si {@code initialCapacity <= 0}
     */
    public MaxHeap(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad inicial debe ser > 0, recibido: " + initialCapacity);
        }
        this.queue = new Object[initialCapacity];
        this.size = 0;
    }

    /**
     * Crea un heap a partir de una lista usando el algoritmo de Floyd O(n).
     *
     * @param elements lista de elementos, no nula
     * @throws NullPointerException si la lista o algún elemento es nulo
     */
    public MaxHeap(List<T> elements) {
        Objects.requireNonNull(elements, "La lista de elementos no puede ser nula");
        this.size = elements.size();
        this.queue = new Object[Math.max(DEFAULT_INITIAL_CAPACITY, size)];
        for (int i = 0; i < size; i++) {
            queue[i] = Objects.requireNonNull(elements.get(i),
                    "Elemento en posición " + i + " es nulo");
        }
        rebuildHeap();
    }

    /**
     * Retorna la cantidad actual de elementos en el heap.
     *
     * @return tamaño del heap
     */
    public int size() {
        return size;
    }

    /**
     * Inserta un nuevo elemento en el heap.
     *
     * <p>Complejidad: O(log n)</p>
     *
     * @param element elemento a insertar, no nulo
     * @throws NullPointerException si el elemento es nulo
     */
    public void insert(T element) {
        Objects.requireNonNull(element, "El elemento no puede ser nulo");
        ensureCapacity();
        queue[size] = element;
        siftUp(size);
        size++;
    }

    /**
     * Extrae el elemento máximo del heap (la raíz).
     *
     * <p>Complejidad: O(log n)</p>
     *
     * @return un {@link Optional} conteniendo el elemento máximo,
     *         o vacío si el heap está vacío
     */
    public Optional<T> extractMax() {
        if (size == 0) {
            return Optional.empty();
        }
        int lastIndex = size - 1;
        T result = elementAt(0);
        queue[0] = queue[lastIndex];
        queue[lastIndex] = null; // Evitar fugas de memoria (memory leaks)
        size--;
        if (size > 0) {
            siftDown(0);
        }
        return Optional.of(result);
    }

    /**
     * Retorna el elemento máximo del heap sin extraerlo.
     *
     * <p>Complejidad: O(1)</p>
     *
     * @return un {@link Optional} conteniendo el elemento máximo,
     *         o vacío si el heap está vacío
     */
    public Optional<T> peekMax() {
        if (size == 0) {
            return Optional.empty();
        }
        return Optional.of(elementAt(0));
    }

    /**
     * Accede al elemento en la posición indicada del array interno.
     *
     * <p>Uso principal: verificación de invariantes en pruebas.</p>
     *
     * @param index índice basado en cero
     * @return el elemento en esa posición
     * @throws IndexOutOfBoundsException si el índice está fuera de [0, size)
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    "Índice " + index + " fuera de rango [0, " + size + ")");
        }
        return elementAt(index);
    }

    /**
     * Restaura la propiedad del heap en O(n) utilizando el algoritmo de Floyd.
     *
     * <p>Debe ser llamado tras mutar prioridades de forma masiva externamente.</p>
     */
    public void rebuildHeap() {
        int half = (size >>> 1) - 1;
        for (int i = half; i >= 0; i--) {
            siftDown(i);
        }
    }

    /**
     * Obtiene una copia del estado interno para propósitos de restauración (Command).
     *
     * @return arreglo con los elementos activos
     */
    public Object[] getState() {
        Object[] state = new Object[size];
        System.arraycopy(queue, 0, state, 0, size);
        return state;
    }

    /**
     * Restaura el estado interno del heap desde un snapshot (Command).
     *
     * @param state arreglo de elementos activos
     */
    public void restoreState(Object[] state) {
        Objects.requireNonNull(state, "El estado no puede ser nulo");
        this.queue = new Object[Math.max(DEFAULT_INITIAL_CAPACITY, state.length)];
        System.arraycopy(state, 0, this.queue, 0, state.length);
        this.size = state.length;
    }

    // ── Helper Methods (Complejidad Ciclomática <= 10) ──────────────

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) >>> 1;
            if (compare(index, parent) <= 0) {
                break;
            }
            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index) {
        int half = size >>> 1;
        while (index < half) {
            int left = (index << 1) + 1;
            int right = left + 1;
            int largest = left;
            if (right < size && compare(right, left) > 0) {
                largest = right;
            }
            if (compare(largest, index) <= 0) {
                break;
            }
            swap(index, largest);
            index = largest;
        }
    }

    private void ensureCapacity() {
        if (size == queue.length) {
            int newCapacity = queue.length + (queue.length >>> 1);
            if (newCapacity < DEFAULT_INITIAL_CAPACITY) {
                newCapacity = DEFAULT_INITIAL_CAPACITY;
            }
            Object[] oldQueue = queue;
            queue = new Object[newCapacity];
            System.arraycopy(oldQueue, 0, queue, 0, oldQueue.length);
        }
    }

    @SuppressWarnings("unchecked")
    private int compare(int i, int j) {
        return ((T) queue[i]).compareTo((T) queue[j]);
    }

    private void swap(int i, int j) {
        Object temp = queue[i];
        queue[i] = queue[j];
        queue[j] = temp;
    }

    @SuppressWarnings("unchecked")
    private T elementAt(int index) {
        return (T) queue[index];
    }
}
