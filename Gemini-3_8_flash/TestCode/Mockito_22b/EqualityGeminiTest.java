package org.mockito.internal.matchers;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Class Under Test: org.mockito.internal.matchers.Equality
 * 
 * Branch / Decision Coverage Map:
 * 1. areEqual(o1, o2):
 *    - Branch: o1 == null || o2 == null (T) -> o1 == null && o2 == null (T: both null => true)
 *    - Branch: o1 == null || o2 == null (T) -> o1 == null && o2 == null (F: o1 null, o2 not => false)
 *    - Branch: o1 == null || o2 == null (T) -> o1 == null && o2 == null (F: o1 not, o2 null => false)
 *    - Branch: o1 != null && o2 != null (F) -> isArray(o1) (T) -> isArray(o2) (T) -> areArraysEqual
 *    - Branch: o1 != null && o2 != null (F) -> isArray(o1) (T) -> isArray(o2) (F) => false
 *    - Branch: o1 != null && o2 != null (F) -> isArray(o1) (F) -> o1.equals(o2)
 *
 * 2. areArraysEqual(o1, o2):
 *    - areArrayLengthsEqual && areArrayElementsEqual
 *    - Short-circuit: false if lengths differ
 *    - Evaluated: true only if both lengths and all elements match
 *
 * 3. areArrayLengthsEqual(o1, o2):
 *    - Array.getLength(o1) == Array.getLength(o2) (T / F)
 *
 * 4. areArrayElementsEqual(o1, o2):
 *    - Loop condition: i < length (0 iterations for empty array)
 *    - Loop body: !areEqual(elem1, elem2) (T => return false immediately, early termination)
 *    - Loop completion: all elements equal => return true
 *    - Recursive array-in-array resolution (multi-dimensional arrays)
 *
 * 5. isArray(o):
 *    - Array instance vs non-array instance
 *
 * Defects4J Ground Truth Target:
 * - org.mockito.internal.matchers.EqualityTest::shouldKnowIfObjectsAreEqual
 *   Targeting the unhandled RuntimeException during equality comparison when an object's
 *   equals(Object) method throws a RuntimeException. Correct behavior mandates returning false.
 */
public class EqualityGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAreEqualWithIdenticalObjects() {
        String s1 = "hello";
        String s2 = "hello";
        assertTrue(Equality.areEqual(s1, s2));
    }

    @Test(timeout = 4000)
    public void testAreEqualWithDifferentObjects() {
        assertFalse(Equality.areEqual("hello", "world"));
    }

    @Test(timeout = 4000)
    public void testAreEqualWithMatchingPrimitiveArrays() {
        int[] arr1 = new int[]{1, 2, 3};
        int[] arr2 = new int[]{1, 2, 3};
        assertTrue(Equality.areEqual(arr1, arr2));
    }

    @Test(timeout = 4000)
    public void testAreEqualWithDifferentElementPrimitiveArrays() {
        int[] arr1 = new int[]{1, 2, 3};
        int[] arr2 = new int[]{1, 2, 4};
        assertFalse(Equality.areEqual(arr1, arr2));
    }

    @Test(timeout = 4000)
    public void testAreEqualWithMatchingObjectArrays() {
        String[] arr1 = new String[]{"foo", "bar"};
        String[] arr2 = new String[]{"foo", "bar"};
        assertTrue(Equality.areEqual(arr1, arr2));
    }

    @Test(timeout = 4000)
    public void testAreEqualWithDifferentElementObjectArrays() {
        String[] arr1 = new String[]{"foo", "bar"};
        String[] arr2 = new String[]{"foo", "baz"};
        assertFalse(Equality.areEqual(arr1, arr2));
    }

    @Test(timeout = 4000)
    public void testAreEqualWithMultiDimensionalArrays() {
        Object[][] nested1 = new Object[][]{{1, "a"}, {2, "b"}};
        Object[][] nested2 = new Object[][]{{1, "a"}, {2, "b"}};
        assertTrue(Equality.areEqual(nested1, nested2));

        Object[][] nested3 = new Object[][]{{1, "a"}, {2, "c"}};
        assertFalse(Equality.areEqual(nested1, nested3));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAreEqualBothNull() {
        assertTrue(Equality.areEqual(null, null));
    }

    @Test(timeout = 4000)
    public void testAreEqualFirstNullSecondNonNull() {
        assertFalse(Equality.areEqual(null, "nonNull"));
    }

    @Test(timeout = 4000)
    public void testAreEqualFirstNonNullSecondNull() {
        assertFalse(Equality.areEqual("nonNull", null));
    }

    @Test(timeout = 4000)
    public void testAreEqualEmptyArrays() {
        int[] empty1 = new int[0];
        int[] empty2 = new int[0];
        assertTrue(Equality.areEqual(empty1, empty2));
    }

    @Test(timeout = 4000)
    public void testAreEqualArraysOfDifferentLengths() {
        int[] arr1 = new int[]{1, 2};
        int[] arr2 = new int[]{1, 2, 3};
        assertFalse(Equality.areEqual(arr1, arr2));
        assertFalse(Equality.areEqual(arr2, arr1));
    }

    @Test(timeout = 4000)
    public void testAreEqualArrayAndNonArray() {
        int[] arr = new int[]{1, 2};
        String nonArr = "notAnArray";
        assertFalse(Equality.areEqual(arr, nonArr));
        assertFalse(Equality.areEqual(nonArr, arr));
    }

    @Test(timeout = 4000)
    public void testAreEqualArraysWithNullElements() {
        Object[] arr1 = new Object[]{"a", null, "b"};
        Object[] arr2 = new Object[]{"a", null, "b"};
        assertTrue(Equality.areEqual(arr1, arr2));

        Object[] arr3 = new Object[]{"a", "notNull", "b"};
        assertFalse(Equality.areEqual(arr1, arr3));
        assertFalse(Equality.areEqual(arr3, arr1));
    }

    @Test(timeout = 4000)
    public void testAreEqualDifferentPrimitiveArrayTypes() {
        int[] intArr = new int[]{1, 2};
        long[] longArr = new long[]{1L, 2L};
        // Array.get will box to Integer and Long respectively, which are not equals
        assertFalse(Equality.areEqual(intArr, longArr));
    }

    @Test(timeout = 4000)
    public void testAreEqualBooleanArrays() {
        boolean[] b1 = new boolean[]{true, false};
        boolean[] b2 = new boolean[]{true, false};
        boolean[] b3 = new boolean[]{true, true};
        assertTrue(Equality.areEqual(b1, b2));
        assertFalse(Equality.areEqual(b1, b3));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect in Defects4J:
     * org.mockito.internal.matchers.EqualityTest::shouldKnowIfObjectsAreEqual
     * 
     * In Mockito, when an object's equals(Object) method throws a RuntimeException,
     * Equality.areEqual must gracefully handle it and return false instead of propagating
     * the RuntimeException.
     */
    @Test(timeout = 4000)
    public void shouldKnowIfObjectsAreEqual() {
        Object faultyObject = new Object() {
            @Override
            public boolean equals(Object obj) {
                throw new RuntimeException("Deliberate failure in equals");
            }
        };

        // Must safely return false and not throw RuntimeException
        assertFalse(Equality.areEqual(faultyObject, "someOtherObject"));
    }

    // =========================================================================
    // Partition D: Package-Private Helper Method Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsArrayHelper() {
        assertTrue(Equality.isArray(new int[0]));
        assertTrue(Equality.isArray(new Object[0]));
        assertFalse(Equality.isArray("nonArray"));
        assertFalse(Equality.isArray(123));
    }

    @Test(timeout = 4000)
    public void testAreArrayLengthsEqualHelper() {
        assertTrue(Equality.areArrayLengthsEqual(new int[]{1, 2}, new String[]{"a", "b"}));
        assertFalse(Equality.areArrayLengthsEqual(new int[]{1}, new String[]{"a", "b"}));
    }

    @Test(timeout = 4000)
    public void testAreArrayElementsEqualHelper() {
        assertTrue(Equality.areArrayElementsEqual(new int[]{1, 2}, new int[]{1, 2}));
        assertFalse(Equality.areArrayElementsEqual(new int[]{1, 2}, new int[]{1, 9}));
    }

    @Test(timeout = 4000)
    public void testAreArraysEqualHelper() {
        assertTrue(Equality.areArraysEqual(new double[]{1.5, 2.5}, new double[]{1.5, 2.5}));
        assertFalse(Equality.areArraysEqual(new double[]{1.5}, new double[]{1.5, 2.5}));
        assertFalse(Equality.areArraysEqual(new double[]{1.5, 3.0}, new double[]{1.5, 2.5}));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testInstantiation() {
        Equality instance = new Equality();
        assertNotNull(instance);
    }
}