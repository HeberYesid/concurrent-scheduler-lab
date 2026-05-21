package com.proyecto.domain.algorithm;

import java.lang.reflect.Method;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MaxHeapPropertySeedTest {

    @Test
    void everyPropertyUsesFixedSeed() {
        Method[] methods = MaxHeapPropertyTest.class.getDeclaredMethods();
        int propertyCount = 0;

        for (Method method : methods) {
            Property property = method.getAnnotation(Property.class);
            if (property == null) {
                continue;
            }
            propertyCount++;
            assertFalse(property.seed().isBlank(), "La propiedad " + method.getName() + " debe definir seed fija");
        }

        assertEquals(3, propertyCount);
    }
}
