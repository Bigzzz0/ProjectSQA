package org.apache.commons.math.util;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.math.util.MultidimensionalCounter
 *
 * Decision Branches & Condition Coverage:
 * 1. MultidimensionalCounter(int ... size)
 *    - Loop offset calculation: 0 <= i < last, i + 1 <= j < dimension
 *    - Boundary validation: tS <= 0 (throws NotStrictlyPositiveException)
 *    - Normal multi-dimension vs. single-dimension (last = 0, loops skipped)
 * 2. Iterator:
 *    - hasNext():
 *      * Returns true if any counter[i] != size[i] - 1
 *      * Returns false if all counter[i] == size[i] - 1 (end of iteration)
 *    - next():
 *      * Roll-over condition: counter[i] == size[i] - 1 resets to 0 and carries over
 *      * Non-roll-over condition: ++counter[i] and break
 *    - getCount(), getCounts(), getCount(int dim)
 *    - remove(): throws UnsupportedOperationException
 * 3. getDimension(), getSize(), getSizes()
 *    - Immutability check: verifying modifications to returned array don't affect internal state
 * 4. getCounts(int index):
 *    - Bounds check: index < 0 || index >= totalSize (throws OutOfRangeException)
 *    - Higher dimensions decomposition: count <= index logic
 *    - [DEFECT ZONE] Last dimension index calculation:
 *      * Defective version uses:
 *          int idx = 1; while (count < index) { count += idx; ++idx; } --idx; indices[last] = idx;
 *        which treats last index with triangular accumulation instead of linear offset!
 *      * Expected correct behavior: indices[last] == index - count
 * 5. getCount(int ... c):
 *    - Length check: c.length != dimension (throws DimensionMismatchException)
 *    - Bounds check: c[i] < 0 || c[i] >= size[i] (throws OutOfRangeException)
 *    - Correct unidimensional index accumulation
 * 6. toString():
 *    - Single-dimension vs. multi-dimension invocation
 */

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.apache.commons.math.exception.NotStrictlyPositiveException;

public class MultidimensionalCounterGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesAndDimensions() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 4);
        assertEquals(3, counter.getDimension());
        assertEquals(24, counter.getSize());

        final int[] sizes = counter.getSizes();
        assertEquals(3, sizes.length);
        assertEquals(2, sizes[0]);
        assertEquals(3, sizes[1]);
        assertEquals(4, sizes[2]);
    }

    @Test(timeout = 4000)
    public void testGetCountMultiDimensionalToUniDimensional() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 4, 3);
        // (0, 0, 0) -> 0
        assertEquals(0, counter.getCount(0, 0, 0));
        // (0, 0, 1) -> 1
        assertEquals(1, counter.getCount(0, 0, 1));
        // (0, 0, 2) -> 2
        assertEquals(2, counter.getCount(0, 0, 2));
        // (0, 1, 0) -> 3
        assertEquals(3, counter.getCount(0, 1, 0));
        // (1, 0, 0) -> 12
        assertEquals(12, counter.getCount(1, 0, 0));
        // (1, 3, 2) -> 23
        assertEquals(23, counter.getCount(1, 3, 2));
    }

    @Test(timeout = 4000)
    public void testIteratorTraversalAndState() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        final MultidimensionalCounter.Iterator iter = counter.iterator();

        assertEquals(-1, iter.getCount());
        assertTrue(iter.hasNext());

        // Step 0: [0, 0]
        Integer nextVal0 = iter.next();
        assertEquals(Integer.valueOf(0), nextVal0);
        assertEquals(0, iter.getCount());
        assertEquals(0, iter.getCount(0));
        assertEquals(0, iter.getCount(1));
        assertArrayEquals(new int[]{0, 0}, iter.getCounts());
        assertTrue(iter.hasNext());

        // Step 1: [0, 1]
        Integer nextVal1 = iter.next();
        assertEquals(Integer.valueOf(1), nextVal1);
        assertEquals(1, iter.getCount());
        assertEquals(0, iter.getCount(0));
        assertEquals(1, iter.getCount(1));
        assertArrayEquals(new int[]{0, 1}, iter.getCounts());
        assertTrue(iter.hasNext());

        // Step 2: [1, 0]
        Integer nextVal2 = iter.next();
        assertEquals(Integer.valueOf(2), nextVal2);
        assertEquals(2, iter.getCount());
        assertEquals(1, iter.getCount(0));
        assertEquals(0, iter.getCount(1));
        assertArrayEquals(new int[]{1, 0}, iter.getCounts());
        assertTrue(iter.hasNext());

        // Step 3: [1, 1]
        Integer nextVal3 = iter.next();
        assertEquals(Integer.valueOf(3), nextVal3);
        assertEquals(3, iter.getCount());
        assertEquals(1, iter.getCount(0));
        assertEquals(1, iter.getCount(1));
        assertArrayEquals(new int[]{1, 1}, iter.getCounts());

        // Iteration complete
        assertFalse(iter.hasNext());
    }

    @Test(timeout = 4000)
    public void testSingleDimensionCounter() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(3);
        assertEquals(1, counter.getDimension());
        assertEquals(3, counter.getSize());
        assertArrayEquals(new int[]{3}, counter.getSizes());

        assertEquals(0, counter.getCount(0));
        assertEquals(1, counter.getCount(1));
        assertEquals(2, counter.getCount(2));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingleSlotCounter() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(1);
        assertEquals(1, counter.getDimension());
        assertEquals(1, counter.getSize());
        assertEquals(0, counter.getCount(0));

        final MultidimensionalCounter.Iterator iter = counter.iterator();
        assertTrue(iter.hasNext());
        assertEquals(Integer.valueOf(0), iter.next());
        assertFalse(iter.hasNext());
        assertEquals(0, iter.getCount());
        assertArrayEquals(new int[]{0}, iter.getCounts());
    }

    @Test(timeout = 4000)
    public void testMinimalMultiDimensionalCounter() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(1, 1, 1);
        assertEquals(3, counter.getDimension());
        assertEquals(1, counter.getSize());
        assertEquals(0, counter.getCount(0, 0, 0));

        final MultidimensionalCounter.Iterator iter = counter.iterator();
        assertTrue(iter.hasNext());
        assertEquals(Integer.valueOf(0), iter.next());
        assertFalse(iter.hasNext());
        assertArrayEquals(new int[]{0, 0, 0}, iter.getCounts());
    }

    @Test(timeout = 4000)
    public void testGetCountsUpperAndLowerBoundaries() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        // index = 0 -> [0, 0]
        assertArrayEquals(new int[]{0, 0}, counter.getCounts(0));
        // index = 1 -> [0, 1]
        assertArrayEquals(new int[]{0, 1}, counter.getCounts(1));
        // index = 2 -> [0, 2]
        assertArrayEquals(new int[]{0, 2}, counter.getCounts(2));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where getCounts(index) incorrectly computes the last
     * dimension using a triangular progression rather than a linear offset.
     * Specifically, when the index in the last dimension is 3, the defective code
     * calculates 2 instead of 3 (AssertionFailedError: expected:<3> but was:<2>).
     */
    @Test(timeout = 4000)
    public void testDefectGetCountsLastDimension() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 4);

        // For index = 3, expected coordinates are [0, 3].
        // Defective version produces [0, 2] because of flawed while-loop.
        final int[] countsAt3 = counter.getCounts(3);
        assertEquals("Wrong multidimensional index for dimension 0 at index 3", 0, countsAt3[0]);
        assertEquals("Wrong multidimensional index for dimension 1 at index 3", 3, countsAt3[1]);

        // Verify round-trip mapping for index = 3
        assertEquals(3, counter.getCount(countsAt3));
    }

    /**
     * Comprehensive consistency test across full iteration to expose any
     * discrepancy between iterator state and getCounts(int).
     */
    @Test(timeout = 4000)
    public void testIterationConsistency() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 4);
        final MultidimensionalCounter.Iterator iter = counter.iterator();

        while (iter.hasNext()) {
            final int unidimIndex = iter.next();
            final int[] iterCounts = iter.getCounts();
            final int[] convertedCounts = counter.getCounts(unidimIndex);

            assertEquals("Uni-dimensional count mismatch", unidimIndex, iter.getCount());
            for (int dim = 0; dim < counter.getDimension(); dim++) {
                assertEquals("Wrong multidimensional index for [" + unidimIndex + "][" + dim + "]",
                        iterCounts[dim], convertedCounts[dim]);
            }
            assertEquals("Reverse mapping mismatch", unidimIndex, counter.getCount(convertedCounts));
        }
    }

    /**
     * Explicit check for 1-dimensional counter with index >= 3 where
     * the defective triangular step corrupts 1D conversion as well.
     */
    @Test(timeout = 4000)
    public void testDefectSingleDimensionGetCounts() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(5);
        for (int i = 0; i < 5; i++) {
            final int[] counts = counter.getCounts(i);
            assertEquals("Mismatch for single dimension at index " + i, i, counts[0]);
        }
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorZeroSizeThrowsException() {
        new MultidimensionalCounter(0);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorNegativeSizeThrowsException() {
        new MultidimensionalCounter(-1);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorMultiDimensionZeroThrowsException() {
        new MultidimensionalCounter(2, 0, 3);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorMultiDimensionNegativeThrowsException() {
        new MultidimensionalCounter(2, 3, -1);
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testGetCountsNegativeIndexThrowsException() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        counter.getCounts(-1);
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testGetCountsIndexEqualToTotalSizeThrowsException() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        counter.getCounts(4); // totalSize is 4, valid indices are 0..3
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testGetCountsIndexGreaterThanTotalSizeThrowsException() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        counter.getCounts(5);
    }

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testGetCountFewerDimensionsThrowsException() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCount(1);
    }

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testGetCountMoreDimensionsThrowsException() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCount(1, 2, 0);
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testGetCountNegativeCoordinateThrowsException() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCount(-1, 0);
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testGetCountSecondDimensionNegativeThrowsException() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCount(0, -1);
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testGetCountCoordinateEqualsDimensionSizeThrowsException() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCount(2, 0); // max index for dim 0 is 1
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testGetCountSecondDimensionEqualsSizeThrowsException() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCount(0, 3); // max index for dim 1 is 2
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testIteratorRemoveThrowsException() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        final MultidimensionalCounter.Iterator iter = counter.iterator();
        iter.remove();
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testIteratorGetCountOutOfBoundsNegative() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        final MultidimensionalCounter.Iterator iter = counter.iterator();
        iter.getCount(-1);
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testIteratorGetCountOutOfBoundsPositive() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        final MultidimensionalCounter.Iterator iter = counter.iterator();
        iter.getCount(2);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle, Immutability & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testImmutabilityOfGetSizes() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        final int[] sizes = counter.getSizes();
        sizes[0] = 99;
        sizes[1] = 99;

        final int[] freshSizes = counter.getSizes();
        assertEquals(2, freshSizes[0]);
        assertEquals(3, freshSizes[1]);
    }

    @Test(timeout = 4000)
    public void testImmutabilityOfIteratorGetCounts() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        final MultidimensionalCounter.Iterator iter = counter.iterator();
        iter.next();

        final int[] counts = iter.getCounts();
        counts[0] = 99;

        final int[] freshCounts = iter.getCounts();
        assertEquals(0, freshCounts[0]);
    }

    @Test(timeout = 4000)
    public void testToStringSingleDimension() {
        final MultidimensionalCounter counter = new MultidimensionalCounter(1);
        final String str = counter.toString();
        assertEquals("[0]", str);
    }

    @Test(timeout = 4000)
    public void testToStringMultiDimensionThrowsDueToDesign() {
        // In the target implementation, toString() calls getCount(i), passing a single
        // argument. When dimension > 1, getCount(int... c) receives c.length == 1,
        // which triggers DimensionMismatchException.
        final MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        try {
            counter.toString();
            fail("Expected DimensionMismatchException when calling toString() on multi-dimensional counter");
        } catch (DimensionMismatchException e) {
            assertEquals(1, e.getArgument());
            assertEquals(2, e.getDimension());
        }
    }
}