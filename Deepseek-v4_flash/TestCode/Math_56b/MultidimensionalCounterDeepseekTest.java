package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: MultidimensionalCounter
 * 
 * Key Branches & Conditions:
 * 1. Constructor: size array validation (NotStrictlyPositiveException for zero/negative sizes)
 * 2. Constructor: totalSize <= 0 check (overflow detection)
 * 3. Constructor: uniCounterOffset calculation for each dimension
 * 4. getCount(int... c): dimension mismatch check (DimensionMismatchException)
 * 5. getCount(int... c): out-of-range index check per dimension (OutOfRangeException)
 * 6. getCount(int... c): count calculation formula: count += uniCounterOffset[i] * c[i]; return count + c[last]
 * 7. getCounts(int index): out-of-range index check (OutOfRangeException)
 * 8. getCounts(int index): loop logic for computing multidimensional indices
 * 9. Iterator.hasNext(): checks all dimensions against size[i]-1
 * 10. Iterator.next(): decrement loop from last to 0, resetting and incrementing
 * 11. Iterator.next(): count increment after loop
 * 12. Iterator.getCount(): returns current unidimensional count
 * 13. Iterator.getCount(int dim): returns counter[dim]
 * 14. Iterator.getCounts(): returns copy of counter array
 * 15. toString(): uses getCount(i) which is incorrect (should use size[i])
 * 
 * Defect-Targeted Branch (from Defects4J):
 * - getCounts(int index) method has a bug in the last dimension calculation.
 *   The while loop uses "count += idx; ++idx;" which is incorrect.
 *   For index values that fall in the last dimension, the calculation produces wrong results.
 *   Specifically, for a 2D counter with sizes [4, 3], index 14 should give [3][2] but gives [3][1].
 *   The fix should use "count += 1; ++idx;" or simply compute indices[last] = index - count.
 * 
 * Test Strategy:
 * - Partition A: Core functional tests for getCount, getCounts, iterator
 * - Partition B: Boundary tests (zero, negative, max values, overflow)
 * - Partition C: Defect-targeted tests for the known bug
 * - Partition D: Exception path tests
 * - Partition E: Object contract tests (toString, iterator behavior)
 */
public class MultidimensionalCounterDeepseekTest {

    /* ========== Partition A: Core Functional Logic & State Transitions ========== */

    @Test(timeout = 4000)
    public void testBasicConstructionAndGetters() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 4);
        assertEquals(3, counter.getDimension());
        assertEquals(24, counter.getSize());
        assertArrayEquals(new int[]{2, 3, 4}, counter.getSizes());
    }

    @Test(timeout = 4000)
    public void testGetCountBasic() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 4);
        // (0,0,0) -> 0
        assertEquals(0, counter.getCount(0, 0, 0));
        // (0,0,1) -> 1
        assertEquals(1, counter.getCount(0, 0, 1));
        // (0,0,2) -> 2
        assertEquals(2, counter.getCount(0, 0, 2));
        // (0,0,3) -> 3
        assertEquals(3, counter.getCount(0, 0, 3));
        // (0,1,0) -> 4
        assertEquals(4, counter.getCount(0, 1, 0));
        // (1,0,0) -> 12
        assertEquals(12, counter.getCount(1, 0, 0));
        // (1,2,3) -> 23
        assertEquals(23, counter.getCount(1, 2, 3));
    }

    @Test(timeout = 4000)
    public void testGetCountsBasic() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 4);
        assertArrayEquals(new int[]{0, 0, 0}, counter.getCounts(0));
        assertArrayEquals(new int[]{0, 0, 1}, counter.getCounts(1));
        assertArrayEquals(new int[]{0, 0, 2}, counter.getCounts(2));
        assertArrayEquals(new int[]{0, 0, 3}, counter.getCounts(3));
        assertArrayEquals(new int[]{0, 1, 0}, counter.getCounts(4));
        assertArrayEquals(new int[]{1, 0, 0}, counter.getCounts(12));
        assertArrayEquals(new int[]{1, 2, 3}, counter.getCounts(23));
    }

    @Test(timeout = 4000)
    public void testGetCountAndGetCountsRoundTrip() {
        MultidimensionalCounter counter = new MultidimensionalCounter(3, 5, 7);
        for (int i = 0; i < counter.getSize(); i++) {
            int[] indices = counter.getCounts(i);
            int back = counter.getCount(indices);
            assertEquals("Round-trip failed for index " + i, i, back);
        }
    }

    @Test(timeout = 4000)
    public void testIteratorBasicIteration() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        MultidimensionalCounter.Iterator iter = counter.iterator();
        int expectedCount = 0;
        while (iter.hasNext()) {
            int val = iter.next();
            assertEquals(expectedCount, val);
            assertEquals(expectedCount, iter.getCount());
            expectedCount++;
        }
        assertEquals(6, expectedCount);
    }

    @Test(timeout = 4000)
    public void testIteratorGetCounts() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        MultidimensionalCounter.Iterator iter = counter.iterator();
        int[][] expected = {
            {0, 0}, {0, 1}, {0, 2},
            {1, 0}, {1, 1}, {1, 2}
        };
        int idx = 0;
        while (iter.hasNext()) {
            iter.next();
            assertArrayEquals("Failed at index " + idx, expected[idx], iter.getCounts());
            idx++;
        }
    }

    @Test(timeout = 4000)
    public void testIteratorGetCountByDimension() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        MultidimensionalCounter.Iterator iter = counter.iterator();
        int[][] expected = {
            {0, 0}, {0, 1}, {0, 2},
            {1, 0}, {1, 1}, {1, 2}
        };
        int idx = 0;
        while (iter.hasNext()) {
            iter.next();
            for (int d = 0; d < 2; d++) {
                assertEquals("Dimension " + d + " at index " + idx,
                    expected[idx][d], iter.getCount(d));
            }
            idx++;
        }
    }

    @Test(timeout = 4000)
    public void testSingleDimension() {
        MultidimensionalCounter counter = new MultidimensionalCounter(5);
        assertEquals(1, counter.getDimension());
        assertEquals(5, counter.getSize());
        for (int i = 0; i < 5; i++) {
            assertEquals(i, counter.getCount(i));
            assertArrayEquals(new int[]{i}, counter.getCounts(i));
        }
    }

    @Test(timeout = 4000)
    public void testLargeDimensions() {
        MultidimensionalCounter counter = new MultidimensionalCounter(10, 10, 10);
        assertEquals(1000, counter.getSize());
        assertEquals(999, counter.getCount(9, 9, 9));
        assertArrayEquals(new int[]{9, 9, 9}, counter.getCounts(999));
    }

    /* ========== Partition B: Boundary Value Analysis & Extremes ========== */

    @Test(timeout = 4000)
    public void testZeroIndex() {
        MultidimensionalCounter counter = new MultidimensionalCounter(3, 4);
        assertEquals(0, counter.getCount(0, 0));
        assertArrayEquals(new int[]{0, 0}, counter.getCounts(0));
    }

    @Test(timeout = 4000)
    public void testMaxIndex() {
        MultidimensionalCounter counter = new MultidimensionalCounter(3, 4);
        int maxIndex = counter.getSize() - 1;
        assertEquals(11, maxIndex);
        assertEquals(maxIndex, counter.getCount(2, 3));
        assertArrayEquals(new int[]{2, 3}, counter.getCounts(maxIndex));
    }

    @Test(timeout = 4000)
    public void testSingleElementCounter() {
        MultidimensionalCounter counter = new MultidimensionalCounter(1, 1, 1);
        assertEquals(1, counter.getSize());
        assertEquals(0, counter.getCount(0, 0, 0));
        assertArrayEquals(new int[]{0, 0, 0}, counter.getCounts(0));
    }

    @Test(timeout = 4000)
    public void testLargeSingleDimension() {
        MultidimensionalCounter counter = new MultidimensionalCounter(1000);
        assertEquals(1000, counter.getSize());
        assertEquals(999, counter.getCount(999));
        assertArrayEquals(new int[]{999}, counter.getCounts(999));
    }

    @Test(timeout = 4000)
    public void testSizeOverflow() {
        // This should throw NotStrictlyPositiveException due to overflow
        try {
            new MultidimensionalCounter(Integer.MAX_VALUE, 2);
            fail("Expected NotStrictlyPositiveException for overflow");
        } catch (NotStrictlyPositiveException e) {
            // expected
        }
    }

    /* ========== Partition C: Defect-Targeted Branch Zone ========== */

    /**
     * This test directly targets the known Defects4J defect.
     * For a 2D counter with sizes [4, 3], index 14 should map to [3][2].
     * The buggy version produces [3][1] due to incorrect last dimension calculation.
     */
    @Test(timeout = 4000)
    public void testIterationConsistency() {
        MultidimensionalCounter counter = new MultidimensionalCounter(4, 3);
        // Total size = 12, indices 0..11
        // Expected mapping for index 11: [3][2]
        int[] indices = counter.getCounts(11);
        assertEquals("Wrong multidimensional index for [3][2] expected:<3> but was:<" + indices[0] + ">",
            3, indices[0]);
        assertEquals("Wrong multidimensional index for [3][2] expected:<2> but was:<" + indices[1] + ">",
            2, indices[1]);
    }

    @Test(timeout = 4000)
    public void testDefectSpecificCase() {
        // Test the exact case from the defect report
        MultidimensionalCounter counter = new MultidimensionalCounter(4, 3);
        // Index 11 should be [3][2]
        int[] result = counter.getCounts(11);
        assertArrayEquals("Defect: getCounts(11) should be [3, 2]", new int[]{3, 2}, result);
        
        // Verify round-trip
        int back = counter.getCount(result);
        assertEquals("Round-trip failed", 11, back);
    }

    @Test(timeout = 4000)
    public void testDefectMultipleCases() {
        MultidimensionalCounter counter = new MultidimensionalCounter(4, 3);
        // Test all indices to ensure correctness
        int[][] expected = {
            {0, 0}, {0, 1}, {0, 2},
            {1, 0}, {1, 1}, {1, 2},
            {2, 0}, {2, 1}, {2, 2},
            {3, 0}, {3, 1}, {3, 2}
        };
        for (int i = 0; i < 12; i++) {
            assertArrayEquals("Failed at index " + i, expected[i], counter.getCounts(i));
        }
    }

    @Test(timeout = 4000)
    public void testDefectThreeDimensions() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 4);
        // Test last index: 23 -> [1][2][3]
        assertArrayEquals(new int[]{1, 2, 3}, counter.getCounts(23));
        // Test some middle indices
        assertArrayEquals(new int[]{0, 2, 3}, counter.getCounts(11));
        assertArrayEquals(new int[]{1, 0, 0}, counter.getCounts(12));
        assertArrayEquals(new int[]{1, 0, 3}, counter.getCounts(15));
    }

    /* ========== Partition D: Exception & Defensive Guard Paths ========== */

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testZeroSize() {
        new MultidimensionalCounter(0, 3);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testNegativeSize() {
        new MultidimensionalCounter(-1, 3);
    }

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testGetCountWrongDimension() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCount(1, 2, 3);
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testGetCountNegativeIndex() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCount(-1, 0);
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testGetCountIndexTooLarge() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCount(2, 0);
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testGetCountsNegativeIndex() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCounts(-1);
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testGetCountsIndexTooLarge() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCounts(6);
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testGetCountsIndexEqualToSize() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCounts(6);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testIteratorRemove() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        MultidimensionalCounter.Iterator iter = counter.iterator();
        iter.remove();
    }

    /* ========== Partition E: Object Lifecycle & Contract Integrity ========== */

    @Test(timeout = 4000)
    public void testToString() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        String str = counter.toString();
        // toString uses getCount(i) which returns dimension count, not size
        // For a 2D counter, getCount(0) returns 2, getCount(1) returns 3
        assertEquals("[2][3]", str);
    }

    @Test(timeout = 4000)
    public void testIteratorHasNextAtEnd() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        MultidimensionalCounter.Iterator iter = counter.iterator();
        while (iter.hasNext()) {
            iter.next();
        }
        assertFalse(iter.hasNext());
    }

    @Test(timeout = 4000)
    public void testIteratorCountAfterFullIteration() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        MultidimensionalCounter.Iterator iter = counter.iterator();
        while (iter.hasNext()) {
            iter.next();
        }
        assertEquals(5, iter.getCount());
    }

    @Test(timeout = 4000)
    public void testIteratorGetCountsAfterFullIteration() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        MultidimensionalCounter.Iterator iter = counter.iterator();
        while (iter.hasNext()) {
            iter.next();
        }
        assertArrayEquals(new int[]{1, 2}, iter.getCounts());
    }

    @Test(timeout = 4000)
    public void testIteratorGetCountByDimensionAfterFullIteration() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        MultidimensionalCounter.Iterator iter = counter.iterator();
        while (iter.hasNext()) {
            iter.next();
        }
        assertEquals(1, iter.getCount(0));
        assertEquals(2, iter.getCount(1));
    }

    @Test(timeout = 4000)
    public void testMultipleIterators() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        MultidimensionalCounter.Iterator iter1 = counter.iterator();
        MultidimensionalCounter.Iterator iter2 = counter.iterator();
        
        // Advance iter1 partially
        iter1.next();
        iter1.next();
        
        // iter2 should start fresh
        assertEquals(-1, iter2.getCount());
        assertEquals(0, iter2.next().intValue());
    }

    @Test(timeout = 4000)
    public void testGetSizesImmutability() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        int[] sizes = counter.getSizes();
        sizes[0] = 100;
        assertArrayEquals(new int[]{2, 3}, counter.getSizes());
    }

    @Test(timeout = 4000)
    public void testIteratorGetCountsImmutability() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        MultidimensionalCounter.Iterator iter = counter.iterator();
        iter.next();
        int[] counts = iter.getCounts();
        counts[0] = 100;
        assertArrayEquals(new int[]{0, 0}, iter.getCounts());
    }
}