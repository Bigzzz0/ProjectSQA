package org.mockito.internal.matchers;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for {@link Equality} targeting maximum line/branch coverage
 * and the known Defects4J defect (RuntimeException in shouldKnowIfObjectsAreEqual).
 *
 * [Branch & Defect Analysis Matrix]
 * ==================================
 * Branch 1: o1 == null || o2 == null → true/false
 *   - Sub-branch: return o1 == null && o2 == null
 * Branch 2: isArray(o1) → true/false
 *   - Sub-branch: return isArray(o2) && areArraysEqual(o1, o2)
 * Branch 3: else → return o1.equals(o2)
 * Branch 4: areArraysEqual → areArrayLengthsEqual && areArrayElementsEqual
 * Branch 5: areArrayLengthsEqual → Array.getLength(o1) == Array.getLength(o2)
 * Branch 6: areArrayElementsEqual → loop over indices, recursive areEqual
 *   - Sub-branch: if (!areEqual(...)) return false
 * Branch 7: isArray → o.getClass().isArray()
 *
 * Defect: When o1 is a non-array object whose equals() throws RuntimeException,
 * the code does not catch it, causing the exception to propagate.
 * The test shouldKnowIfObjectsAreEqual triggers this.
 *
 * Coverage targets:
 * - All null combinations (both null, one null, none null)
 * - Primitive arrays (int[], long[], etc.)
 * - Object arrays (String[], Integer[], etc.)
 * - Multi-dimensional arrays
 * - Arrays with null elements
 * - Arrays of different lengths
 * - Non-array objects (String, Integer, custom with safe equals)
 * - Custom object with throwing equals (defect trigger)
 * - Empty arrays
 * - Single-element arrays
 */
public class EqualityDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void bothNullAreEqual() {
        assertTrue(Equality.areEqual(null, null));
    }

    @Test(timeout = 4000)
    public void firstNullSecondNonNullAreNotEqual() {
        assertFalse(Equality.areEqual(null, "abc"));
    }

    @Test(timeout = 4000)
    public void firstNonNullSecondNullAreNotEqual() {
        assertFalse(Equality.areEqual("abc", null));
    }

    @Test(timeout = 4000)
    public void equalStrings() {
        assertTrue(Equality.areEqual("hello", "hello"));
    }

    @Test(timeout = 4000)
    public void unequalStrings() {
        assertFalse(Equality.areEqual("hello", "world"));
    }

    @Test(timeout = 4000)
    public void equalIntegers() {
        assertTrue(Equality.areEqual(42, 42));
    }

    @Test(timeout = 4000)
    public void unequalIntegers() {
        assertFalse(Equality.areEqual(42, 43));
    }

    @Test(timeout = 4000)
    public void equalPrimitiveIntArrays() {
        int[] a = {1, 2, 3};
        int[] b = {1, 2, 3};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void unequalPrimitiveIntArrays() {
        int[] a = {1, 2, 3};
        int[] b = {1, 2, 4};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void equalObjectArrays() {
        String[] a = {"x", "y"};
        String[] b = {"x", "y"};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void unequalObjectArrays() {
        String[] a = {"x", "y"};
        String[] b = {"x", "z"};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void equalMultiDimensionalArrays() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{1, 2}, {3, 4}};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void unequalMultiDimensionalArrays() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{1, 2}, {3, 5}};
        assertFalse(Equality.areEqual(a, b));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void emptyArraysAreEqual() {
        int[] a = {};
        int[] b = {};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void emptyArrayVsNonEmptyArray() {
        int[] a = {};
        int[] b = {1};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void arraysOfDifferentLengths() {
        int[] a = {1, 2};
        int[] b = {1, 2, 3};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void arraysWithNullElements() {
        String[] a = {"a", null, "c"};
        String[] b = {"a", null, "c"};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void arraysWithNullElementVsNonNull() {
        String[] a = {"a", null, "c"};
        String[] b = {"a", "b", "c"};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void singleElementArraysEqual() {
        int[] a = {5};
        int[] b = {5};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void singleElementArraysUnequal() {
        int[] a = {5};
        int[] b = {6};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void arrayVsNonArray() {
        int[] a = {1};
        String b = "not array";
        assertFalse(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void nonArrayVsArray() {
        String a = "not array";
        int[] b = {1};
        // This goes to else branch: a.equals(b) – String.equals returns false
        assertFalse(Equality.areEqual(a, b));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Targets the known Defects4J defect: when a non-array object's equals() throws
     * RuntimeException, the method should handle it gracefully (return false) but
     * instead propagates the exception. This test fails on the defective version.
     */
    @Test(timeout = 4000)
    public void shouldNotThrowWhenEqualsThrowsRuntimeException() {
        final Object throwing = new Object() {
            @Override
            public boolean equals(Object obj) {
                throw new RuntimeException("equals threw");
            }
        };
        try {
            boolean result = Equality.areEqual(throwing, "anything");
            // If we reach here, the bug is fixed – method should return false
            assertFalse("Expected false when equals throws", result);
        } catch (RuntimeException e) {
            // Bug revealed: exception was thrown
            fail("areEqual should not throw RuntimeException when equals() throws");
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void isArrayOnNullNotCalledDueToNullCheck() {
        // o1 is null, o2 is array – null check returns false, no isArray call
        assertFalse(Equality.areEqual(null, new int[]{1}));
    }

    @Test(timeout = 4000)
    public void isArrayOnNullNotCalledDueToNullCheck2() {
        // o1 is array, o2 is null – null check returns false, no isArray call
        assertFalse(Equality.areEqual(new int[]{1}, null));
    }

    // Additional coverage for isArray with various types
    @Test(timeout = 4000)
    public void isArrayOnPrimitiveArrayReturnsTrue() {
        assertTrue(Equality.isArray(new int[]{1}));
    }

    @Test(timeout = 4000)
    public void isArrayOnObjectArrayReturnsTrue() {
        assertTrue(Equality.isArray(new String[]{"a"}));
    }

    @Test(timeout = 4000)
    public void isArrayOnNonArrayReturnsFalse() {
        assertFalse(Equality.isArray("string"));
    }

    @Test(timeout = 4000)
    public void isArrayOnNullThrowsNPEButNotCalled() {
        // isArray is never called with null in areEqual due to null check
        // but we can test directly that it throws NPE
        try {
            Equality.isArray(null);
            fail("isArray(null) should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    // (Not applicable – Equality is a utility class with static methods)

    // Additional coverage for recursive array element comparison
    @Test(timeout = 4000)
    public void nestedArraysWithNulls() {
        Object[] a = {new int[]{1, null}, "hello"};
        Object[] b = {new int[]{1, null}, "hello"};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void nestedArraysUnequal() {
        Object[] a = {new int[]{1, 2}, "hello"};
        Object[] b = {new int[]{1, 3}, "hello"};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void primitiveVsWrapperArray() {
        int[] a = {1, 2};
        Integer[] b = {1, 2};
        // Both are arrays, lengths equal, elements compare int vs Integer – equals works
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void primitiveVsWrapperArrayUnequal() {
        int[] a = {1, 2};
        Integer[] b = {1, 3};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void longArraysEqual() {
        long[] a = {100L, 200L};
        long[] b = {100L, 200L};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void booleanArraysEqual() {
        boolean[] a = {true, false};
        boolean[] b = {true, false};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void charArraysEqual() {
        char[] a = {'a', 'b'};
        char[] b = {'a', 'b'};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void doubleArraysEqual() {
        double[] a = {1.0, 2.0};
        double[] b = {1.0, 2.0};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void floatArraysEqual() {
        float[] a = {1.0f, 2.0f};
        float[] b = {1.0f, 2.0f};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void shortArraysEqual() {
        short[] a = {1, 2};
        short[] b = {1, 2};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test(timeout = 4000)
    public void byteArraysEqual() {
        byte[] a = {1, 2};
        byte[] b = {1, 2};
        assertTrue(Equality.areEqual(a, b));
    }
}