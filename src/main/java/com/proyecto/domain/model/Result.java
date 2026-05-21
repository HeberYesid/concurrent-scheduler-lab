package com.proyecto.domain.model;

import java.util.Objects;
import java.util.function.Function;

/**
 * Tipo resultado sellado que encapsula éxito ({@link Ok}) o error ({@link Err}).
 *
 * <p>Implementa un patrón monádico para manejo explícito de errores sin
 * excepciones. Inspirado en {@code Result<T, E>} de Rust.</p>
 *
 * <p>Ejemplo de uso:</p>
 * <pre>{@code
 * Result<ProcessTask, SchedulerError> result = Result.ok(task);
 * Result<String, SchedulerError> mapped = result.map(t -> t.toString());
 * }</pre>
 *
 * @param <T> tipo del valor exitoso
 * @param <E> tipo del error
 *
 * @author scheduler-concurrente
 */
public sealed interface Result<T, E> permits Result.Ok, Result.Err {

    // ── Factories ──────────────────────────────────────────────────

    /**
     * Crea un resultado exitoso.
     *
     * @param value valor no nulo del resultado
     * @param <T>   tipo del valor
     * @param <E>   tipo del error
     * @return instancia {@link Ok} conteniendo el valor
     * @throws NullPointerException si {@code value} es {@code null}
     */
    static <T, E> Result<T, E> ok(T value) {
        Objects.requireNonNull(value, "El valor de Ok no puede ser null");
        return new Ok<>(value);
    }

    /**
     * Crea un resultado de error.
     *
     * @param error error no nulo
     * @param <T>   tipo del valor
     * @param <E>   tipo del error
     * @return instancia {@link Err} conteniendo el error
     * @throws NullPointerException si {@code error} es {@code null}
     */
    static <T, E> Result<T, E> err(E error) {
        Objects.requireNonNull(error, "El error de Err no puede ser null");
        return new Err<>(error);
    }

    // ── Consultas ──────────────────────────────────────────────────

    /**
     * Indica si este resultado es exitoso.
     *
     * @return {@code true} si es {@link Ok}, {@code false} si es {@link Err}
     */
    boolean isOk();

    /**
     * Indica si este resultado es un error.
     *
     * @return {@code true} si es {@link Err}, {@code false} si es {@link Ok}
     */
    boolean isErr();

    /**
     * Obtiene el valor exitoso.
     *
     * @return el valor contenido
     * @throws IllegalStateException si este resultado es {@link Err}
     */
    T getValue();

    /**
     * Obtiene el error.
     *
     * @return el error contenido
     * @throws IllegalStateException si este resultado es {@link Ok}
     */
    E getError();

    // ── Transformaciones ───────────────────────────────────────────

    /**
     * Transforma el valor exitoso usando la función dada.
     *
     * <p>Si este resultado es {@link Err}, devuelve el mismo error sin aplicar la función.</p>
     *
     * @param mapper función de transformación, no debe ser {@code null}
     * @param <R>    tipo del nuevo valor
     * @return nuevo {@link Result} con el valor transformado o el error original
     * @throws NullPointerException si {@code mapper} es {@code null}
     */
    <R> Result<R, E> map(Function<T, R> mapper);

    /**
     * Transforma el valor exitoso usando una función que retorna otro {@link Result}.
     *
     * <p>Permite encadenar operaciones que pueden fallar. Si este resultado
     * es {@link Err}, devuelve el mismo error sin aplicar la función.</p>
     *
     * @param mapper función de transformación monádica, no debe ser {@code null}
     * @param <R>    tipo del nuevo valor
     * @return el {@link Result} producido por el mapper o el error original
     * @throws NullPointerException si {@code mapper} es {@code null}
     */
    <R> Result<R, E> flatMap(Function<T, Result<R, E>> mapper);

    // ══════════════════════════════════════════════════════════════
    //  IMPLEMENTACIONES INTERNAS
    // ══════════════════════════════════════════════════════════════

    /**
     * Representa un resultado exitoso.
     *
     * @param value el valor exitoso, nunca {@code null}
     * @param <T>   tipo del valor
     * @param <E>   tipo del error (no utilizado)
     */
    record Ok<T, E>(T value) implements Result<T, E> {

        /** Constructor compacto — garantiza no-nulidad. */
        public Ok {
            Objects.requireNonNull(value, "El valor de Ok no puede ser null");
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public boolean isErr() {
            return false;
        }

        @Override
        public T getValue() {
            return value;
        }

        /**
         * {@inheritDoc}
         *
         * @throws IllegalStateException siempre, porque este resultado es Ok
         */
        @Override
        public E getError() {
            throw new IllegalStateException(
                    "No se puede obtener error de un Result.Ok");
        }

        @Override
        public <R> Result<R, E> map(Function<T, R> mapper) {
            Objects.requireNonNull(mapper, "mapper no puede ser null");
            return Result.ok(mapper.apply(value));
        }

        @Override
        public <R> Result<R, E> flatMap(Function<T, Result<R, E>> mapper) {
            Objects.requireNonNull(mapper, "mapper no puede ser null");
            return mapper.apply(value);
        }
    }

    /**
     * Representa un resultado de error.
     *
     * @param error el error contenido, nunca {@code null}
     * @param <T>   tipo del valor (no utilizado)
     * @param <E>   tipo del error
     */
    record Err<T, E>(E error) implements Result<T, E> {

        /** Constructor compacto — garantiza no-nulidad. */
        public Err {
            Objects.requireNonNull(error, "El error de Err no puede ser null");
        }

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public boolean isErr() {
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * @throws IllegalStateException siempre, porque este resultado es Err
         */
        @Override
        public T getValue() {
            throw new IllegalStateException(
                    "No se puede obtener valor de un Result.Err: " + error);
        }

        @Override
        public E getError() {
            return error;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <R> Result<R, E> map(Function<T, R> mapper) {
            Objects.requireNonNull(mapper, "mapper no puede ser null");
            return (Result<R, E>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <R> Result<R, E> flatMap(Function<T, Result<R, E>> mapper) {
            Objects.requireNonNull(mapper, "mapper no puede ser null");
            return (Result<R, E>) this;
        }
    }
}
