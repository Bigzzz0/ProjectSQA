package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box JUnit 4 test suite for {@link Primitives}.
 * Covers all methods, boundary conditions, and the known defect where
 * primitiveValues.put(double.class, 0) stores an Integer instead of Double.
 *
 * /* [Branch & Defect Analysis Matrix] */
 * - primitiveTypeOf:
 *   Branch if clazz.isPrimitive() -> true: return clazz (int.class, double.class etc.)
 *   Branch else -> lookup primitiveTypes map: returns primitive class for valid wrapper (Boolean→boolean)
 *   else returns null (e.g., String.class)
 * - isPrimitiveWrapper: checks wrapperReturnValues keys -> true for Boolean, false for int, false for null
 * - primitiveWrapperOf: returns default value from wrapperReturnValues -> false for Boolean, 0 for int, null for unknown
 * - primitiveValueOrNullFor: returns default from primitiveValues -> bug on double.class returns Integer(0) instead of Double(0.0)
 *   Null input causes NullPointerException
 *   Unknown type returns null
 *
 * Partition structure:
 *   A: Core functional logic (all methods on valid primitives/wrappers)
 *   B: Boundary values (null, unknown types)
 *   C: Defect-targeted (double.class behaviour)
 *   D: Exception/defensive (null arguments)
 */
public class PrimitivesDeepseekTest {

    // ======================== Partition A: Core Functional ========================

    @Test(timeout = 4000)
    public void primitiveTypeOf_whenPrimitiveType_returnsSame() {
        assertEquals(int.class, Primitives.primitiveTypeOf(int.class));
        assertEquals(double.class, Primitives.primitiveTypeOf(double.class));
        assertEquals(boolean.class, Primitives.primitiveTypeOf(boolean.class));
        assertEquals(char.class, Primitives.primitiveTypeOf(char.class));
        assertEquals(byte.class, Primitives.primitiveTypeOf(byte.class));
        assertEquals(short.class, Primitives.primitiveTypeOf(short.class));
        assertEquals(long.class, Primitives.primitiveTypeOf(long.class));
        assertEquals(float.class, Primitives.primitiveTypeOf(float.class));
    }

    @Test(timeout = 4000)
    public void primitiveTypeOf_whenWrapperType_returnsCorrespondingPrimitive() {
        assertEquals(boolean.class, Primitives.primitiveTypeOf(Boolean.class));
        assertEquals(char.class, Primitives.primitiveTypeOf(Character.class));
        assertEquals(byte.class, Primitives.primitiveTypeOf(Byte.class));
        assertEquals(short.class, Primitives.primitiveTypeOf(Short.class));
        assertEquals(int.class, Primitives.primitiveTypeOf(Integer.class));
        assertEquals(long.class, Primitives.primitiveTypeOf(Long.class));
        assertEquals(float.class, Primitives.primitiveTypeOf(Float.class));
        assertEquals(double.class, Primitives.primitiveTypeOf(Double.class));
    }

    @Test(timeout = 4000)
    public void isPrimitiveWrapper_whenWrapper_returnsTrue() {
        assertTrue(Primitives.isPrimitiveWrapper(Boolean.class));
        assertTrue(Primitives.isPrimitiveWrapper(Character.class));
        assertTrue(Primitives.isPrimitiveWrapper(Byte.class));
        assertTrue(Primitives.isPrimitiveWrapper(Short.class));
        assertTrue(Primitives.isPrimitiveWrapper(Integer.class));
        assertTrue(Primitives.isPrimitiveWrapper(Long.class));
        assertTrue(Primitives.isPrimitiveWrapper(Float.class));
        assertTrue(Primitives.isPrimitiveWrapper(Double.class));
    }

    @Test(timeout = 4000)
    public void isPrimitiveWrapper_whenPrimitive_returnsFalse() {
        assertFalse(Primitives.isPrimitiveWrapper(boolean.class));
        assertFalse(Primitives.isPrimitiveWrapper(int.class));
        assertFalse(Primitives.isPrimitiveWrapper(double.class));
    }

    @Test(timeout = 4000)
    public void isPrimitiveWrapper_whenNonPrimitiveNonWrapper_returnsFalse() {
        assertFalse(Primitives.isPrimitiveWrapper(String.class));
        assertFalse(Primitives.isPrimitiveWrapper(Object.class));
    }

    @Test(timeout = 4000)
    public void primitiveWrapperOf_returnsCorrectDefaultValues() {
        assertEquals(false, (boolean) Primitives.primitiveWrapperOf(Boolean.class));
        assertEquals('\u0000', (char) Primitives.primitiveWrapperOf(Character.class));
        assertEquals((byte) 0, (byte) Primitives.primitiveWrapperOf(Byte.class));
        assertEquals((short) 0, (short) Primitives.primitiveWrapperOf(Short.class));
        assertEquals(0, (int) Primitives.primitiveWrapperOf(Integer.class));
        assertEquals(0L, (long) Primitives.primitiveWrapperOf(Long.class));
        assertEquals(0F, (float) Primitives.primitiveWrapperOf(Float.class));
        assertEquals(0D, (double) Primitives.primitiveWrapperOf(Double.class));
    }

    @Test(timeout = 4000)
    public void primitiveValueOrNullFor_returnsCorrectDefaults() {
        assertEquals(false, (boolean) Primitives.primitiveValueOrNullFor(boolean.class));
        assertEquals('\u0000', (char) Primitives.primitiveValueOrNullFor(char.class));
        assertEquals((byte) 0, (byte) Primitives.primitiveValueOrNullFor(byte.class));
        assertEquals((short) 0, (short) Primitives.primitiveValueOrNullFor(short.class));
        assertEquals(0, (int) Primitives.primitiveValueOrNullFor(int.class));
        assertEquals(0L, (long) Primitives.primitiveValueOrNullFor(long.class));
        assertEquals(0F, (float) Primitives.primitiveValueOrNullFor(float.class));
        // Note: double is handled in defect-targeted test
    }

    // ======================== Partition B: Boundary Values ========================

    @Test(timeout = 4000)
    public void primitiveTypeOf_withNull_throwsNullPointerException() {
        try {
            Primitives.primitiveTypeOf(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void primitiveTypeOf_withNonPrimitiveNonWrapper_returnsNull() {
        assertNull(Primitives.primitiveTypeOf(String.class));
        assertNull(Primitives.primitiveTypeOf(Object.class));
    }

    @Test(timeout = 4000)
    public void isPrimitiveWrapper_withNull_returnsFalse() {
        assertFalse(Primitives.isPrimitiveWrapper(null));
    }

    @Test(timeout = 4000)
    public void primitiveWrapperOf_withNull_throwsNullPointerException() {
        try {
            Primitives.primitiveWrapperOf(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void primitiveWrapperOf_withUnknownType_returnsNull() {
        assertNull(Primitives.primitiveWrapperOf(String.class));
    }

    @Test(timeout = 4000)
    public void primitiveValueOrNullFor_withNull_throwsNullPointerException() {
        try {
            Primitives.primitiveValueOrNullFor(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void primitiveValueOrNullFor_withUnknownType_returnsNull() {
        assertNull(Primitives.primitiveValueOrNullFor(String.class));
    }

    // ======================== Partition C: Defect-Targeted (double.class) ========================

    @Test(timeout = 4000)
    public void primitiveValueOrNullFor_doubleClass_returnsDoubleZero() {
        // This test reveals the bug: primitiveValues.put(double.class, 0) stores Integer(0) instead of Double(0.0).
        Object result = Primitives.primitiveValueOrNullFor(double.class);
        assertNotNull("Result for double.class should not be null", result);
        assertTrue("Result must be a Double instance", result instanceof Double);
        assertEquals("Value should be 0.0", 0.0D, ((Double) result).doubleValue(), 0.0);
    }

    // Additional regression: also check float is correct (no bug)
    @Test(timeout = 4000)
    public void primitiveValueOrNullFor_floatClass_returnsFloatZero() {
        Object result = Primitives.primitiveValueOrNullFor(float.class);
        assertTrue("Result must be a Float instance", result instanceof Float);
        assertEquals(0.0F, ((Float) result).floatValue(), 0.0);
    }

    // ======================== Partition D: Exception & Defensive ========================

    // Already covered above (null arguments)

    // Also ensure that for unknown types, methods do not throw unexpected exceptions
    @Test(timeout = 4000)
    public void allMethodsHandleUnknownTypesGracefully() {
        // These should not throw
        assertNull(Primitives.primitiveTypeOf(String.class));
        assertFalse(Primitives.isPrimitiveWrapper(String.class));
        assertNull(Primitives.primitiveWrapperOf(String.class));
        assertNull(Primitives.primitiveValueOrNullFor(String.class));
    }
}