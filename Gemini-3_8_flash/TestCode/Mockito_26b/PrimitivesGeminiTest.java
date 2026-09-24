package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.internal.util.Primitives
 * 
 * Decision Branches:
 * 1. primitiveTypeOf(Class<T> clazz):
 *    - Branch: clazz.isPrimitive() == true -> returns clazz directly.
 *    - Branch: clazz.isPrimitive() == false -> queries primitiveTypes map (wrapper to primitive or null).
 *    - Exception Path: clazz == null -> NullPointerException on clazz.isPrimitive().
 * 2. isPrimitiveWrapper(Class<?> type):
 *    - Branch: wrapperReturnValues.containsKey(type) == true -> returns true for all 8 wrapper classes.
 *    - Branch: wrapperReturnValues.containsKey(type) == false -> returns false for primitives, reference types, void, null.
 * 3. primitiveWrapperOf(Class<T> type):
 *    - Branch: wrapper type present in wrapperReturnValues -> returns corresponding boxed zero/false/'\u0000'.
 *    - Branch: unknown type or null -> returns null.
 * 4. primitiveValueOrNullFor(Class<T> primitiveType):
 *    - Branch: primitive type present in primitiveValues -> returns boxed zero/false/'\u0000'.
 *    - Branch: unknown type or null -> returns null.
 *
 * Defect-Targeted Branch Zone (Defects4J Known Defect):
 * - primitiveValues.put(double.class, 0); in static initializer.
 *   The literal '0' creates an Integer instead of Double.
 *   Calling Primitives.primitiveValueOrNullFor(double.class) causes a ClassCastException
 *   (java.lang.Integer cannot be cast to java.lang.Double) or assertion failure comparing to 0.0D.
 */
public class PrimitivesGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrimitiveTypeOfWithPrimitiveTypesReturnsSelf() {
        assertEquals(boolean.class, Primitives.primitiveTypeOf(boolean.class));
        assertEquals(char.class, Primitives.primitiveTypeOf(char.class));
        assertEquals(byte.class, Primitives.primitiveTypeOf(byte.class));
        assertEquals(short.class, Primitives.primitiveTypeOf(short.class));
        assertEquals(int.class, Primitives.primitiveTypeOf(int.class));
        assertEquals(long.class, Primitives.primitiveTypeOf(long.class));
        assertEquals(float.class, Primitives.primitiveTypeOf(float.class));
        assertEquals(double.class, Primitives.primitiveTypeOf(double.class));
    }

    @Test(timeout = 4000)
    public void testPrimitiveTypeOfWithWrapperTypesReturnsPrimitive() {
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
    public void testIsPrimitiveWrapperWithAllWrappers() {
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
    public void testPrimitiveWrapperOfValues() {
        assertEquals(Boolean.FALSE, Primitives.primitiveWrapperOf(Boolean.class));
        assertEquals(Character.valueOf('\u0000'), Primitives.primitiveWrapperOf(Character.class));
        assertEquals(Byte.valueOf((byte) 0), Primitives.primitiveWrapperOf(Byte.class));
        assertEquals(Short.valueOf((short) 0), Primitives.primitiveWrapperOf(Short.class));
        assertEquals(Integer.valueOf(0), Primitives.primitiveWrapperOf(Integer.class));
        assertEquals(Long.valueOf(0L), Primitives.primitiveWrapperOf(Long.class));
        assertEquals(Float.valueOf(0F), Primitives.primitiveWrapperOf(Float.class));
        assertEquals(Double.valueOf(0D), Primitives.primitiveWrapperOf(Double.class));
    }

    @Test(timeout = 4000)
    public void testPrimitiveValueOrNullForNonDoublePrimitives() {
        assertEquals(Boolean.FALSE, Primitives.primitiveValueOrNullFor(boolean.class));
        assertEquals(Character.valueOf('\u0000'), Primitives.primitiveValueOrNullFor(char.class));
        assertEquals(Byte.valueOf((byte) 0), Primitives.primitiveValueOrNullFor(byte.class));
        assertEquals(Short.valueOf((short) 0), Primitives.primitiveValueOrNullFor(short.class));
        assertEquals(Integer.valueOf(0), Primitives.primitiveValueOrNullFor(int.class));
        assertEquals(Long.valueOf(0L), Primitives.primitiveValueOrNullFor(long.class));
        assertEquals(Float.valueOf(0F), Primitives.primitiveValueOrNullFor(float.class));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrimitiveTypeOfWithNonWrapperReferenceTypesReturnsNull() {
        assertNull(Primitives.primitiveTypeOf(String.class));
        assertNull(Primitives.primitiveTypeOf(Object.class));
        assertNull(Primitives.primitiveTypeOf(Void.class));
    }

    @Test(timeout = 4000)
    public void testIsPrimitiveWrapperReturnsFalseForNonWrappers() {
        assertFalse(Primitives.isPrimitiveWrapper(boolean.class));
        assertFalse(Primitives.isPrimitiveWrapper(int.class));
        assertFalse(Primitives.isPrimitiveWrapper(double.class));
        assertFalse(Primitives.isPrimitiveWrapper(String.class));
        assertFalse(Primitives.isPrimitiveWrapper(Object.class));
        assertFalse(Primitives.isPrimitiveWrapper(Void.class));
        assertFalse(Primitives.isPrimitiveWrapper(void.class));
        assertFalse(Primitives.isPrimitiveWrapper(null));
    }

    @Test(timeout = 4000)
    public void testPrimitiveWrapperOfReturnsNullForNonWrappers() {
        assertNull(Primitives.primitiveWrapperOf(String.class));
        assertNull(Primitives.primitiveWrapperOf(Object.class));
        assertNull(Primitives.primitiveWrapperOf(int.class));
        assertNull(Primitives.primitiveWrapperOf(null));
    }

    @Test(timeout = 4000)
    public void testPrimitiveValueOrNullForReturnsNullForNonPrimitives() {
        assertNull(Primitives.primitiveValueOrNullFor(Integer.class));
        assertNull(Primitives.primitiveValueOrNullFor(Double.class));
        assertNull(Primitives.primitiveValueOrNullFor(String.class));
        assertNull(Primitives.primitiveValueOrNullFor(Object.class));
        assertNull(Primitives.primitiveValueOrNullFor(void.class));
        assertNull(Primitives.primitiveValueOrNullFor(null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Defect)
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrimitiveValueOrNullForDoubleReturnsDoubleTypeAndValue() {
        // Targets defect where primitiveValues.put(double.class, 0) put Integer(0) instead of Double(0D).
        // This causes ClassCastException when assigning directly to Double or when checked for instance type.
        Double doubleDefault = Primitives.primitiveValueOrNullFor(double.class);
        assertNotNull("Default primitive value for double.class must not be null", doubleDefault);
        assertEquals("Default primitive value for double.class must be 0.0D", Double.valueOf(0.0D), doubleDefault);
        assertTrue("Default primitive value for double.class must be an instance of Double",
                (Object) doubleDefault instanceof Double);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPrimitiveTypeOfWithNullThrowsNullPointerException() {
        Primitives.primitiveTypeOf(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCanInstantiatePrimitivesClass() {
        Primitives primitives = new Primitives();
        assertNotNull(primitives);
    }
}