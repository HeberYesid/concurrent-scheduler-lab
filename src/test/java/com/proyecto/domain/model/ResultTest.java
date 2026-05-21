package com.proyecto.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultTest {

    @Test
    void okSupportsTransformationsAndRejectsErrorAccess() {
        Result<Integer, SchedulerError> result = Result.ok(21);

        assertTrue(result.isOk());
        assertEquals(42, result.map(value -> value * 2).getValue());
        assertEquals(22, result.flatMap(value -> Result.ok(value + 1)).getValue());
        assertThrows(IllegalStateException.class, result::getError);
    }

    @Test
    void errPreservesErrorAndRejectsValueAccess() {
        Result<Integer, SchedulerError> result = Result.err(SchedulerError.INVALID_CONFIGURATION);

        assertTrue(result.isErr());
        assertEquals(SchedulerError.INVALID_CONFIGURATION, result.getError());
        assertEquals(SchedulerError.INVALID_CONFIGURATION, result.map(value -> value * 2).getError());
        assertEquals(SchedulerError.INVALID_CONFIGURATION, result.flatMap(value -> Result.ok(value + 1)).getError());
        assertThrows(IllegalStateException.class, result::getValue);
    }

    @Test
    void factoriesAndMappersRejectNulls() {
        assertThrows(NullPointerException.class, () -> Result.ok(null));
        assertThrows(NullPointerException.class, () -> Result.err(null));

        Result<Integer, SchedulerError> ok = Result.ok(1);
        Result<Integer, SchedulerError> err = Result.err(SchedulerError.EMPTY_PROCESS_LIST);

        assertThrows(NullPointerException.class, () -> ok.map(null));
        assertThrows(NullPointerException.class, () -> ok.flatMap(null));
        assertThrows(NullPointerException.class, () -> err.map(null));
        assertThrows(NullPointerException.class, () -> err.flatMap(null));
    }
}
