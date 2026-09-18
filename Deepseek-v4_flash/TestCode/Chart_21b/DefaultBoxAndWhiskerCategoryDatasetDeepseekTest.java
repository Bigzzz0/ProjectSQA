package org.jfree.data.statistics;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: DefaultBoxAndWhiskerCategoryDataset
 * 
 * Key decision branches and boundary conditions exercised:
 * 1. Constructor: initial state - min/max = NaN, row/col = -1, rangeBounds = [0,0]
 * 2. add(List, Comparable, Comparable) -> delegates to add(BoxAndWhiskerItem,...)
 * 3. add(BoxAndWhiskerItem,...):
 *    - Branch: if (maxRow==r && maxCol==c) || (minRow==r && minCol==c) -> updateBounds()
 *    - Branch: if (item.getMinOutlier() != null) -> minval = minOutlier
 *    - Branch: if (item.getMaxOutlier() != null) -> maxval = maxOutlier
 *    - Branch: if (Double.isNaN(maximumRangeValue)) -> set max
 *    - Branch: else if (maxval > maximumRangeValue) -> update max
 *    - Branch: if (Double.isNaN(minimumRangeValue)) -> set min
 *    - Branch: else if (minval < minimumRangeValue) -> update min
 *    - Always: rangeBounds = new Range(min, max)
 * 4. getRangeLowerBound/getRangeUpperBound/getRangeBounds - return cached values
 * 5. equals() - object identity, type check, data equality
 * 6. clone() - shallow copy of data
 * 
 * Defect targeted (from Defects4J):
 * - testGetRangeBounds failure: expected Range[8.5,9.6] but was Range[8.6,9.6]
 *   This indicates that when adding a new item that replaces an existing cell,
 *   the cached minimumRangeValue is not correctly updated when the new item's
 *   minOutlier is larger than the old one but still the overall minimum.
 *   The bug is in the add method: when the cell already exists and is the
 *   current min/max holder, updateBounds() is called, but then the code
 *   continues to update min/max based on the new item's values, potentially
 *   leaving stale values if the new item's min is larger than the old min.
 *   The fix should recalculate bounds from scratch when the cell is replaced.
 * 
 * Test strategy:
 * - Partition A: Core functional logic - add items, get values, get keys
 * - Partition B: Boundary values - empty list, null outliers, NaN handling
 * - Partition C: Defect-targeted - replace an existing cell that holds min/max
 * - Partition D: Exception paths - invalid indices, null keys
 * - Partition E: equals/clone contract
 */
public class DefaultBoxAndWhiskerCategoryDatasetDeepseekTest {

    // Helper to create a BoxAndWhiskerItem with specified outliers
    private BoxAndWhiskerItem createItem(double minOutlier, double maxOutlier, 
                                          double mean, double median, 
                                          double q1, double q3, 
                                          double minRegular, double maxRegular) {
        List<Double> outliers = new ArrayList<Double>();
        outliers.add(minOutlier);
        outliers.add(maxOutlier);
        return new BoxAndWhiskerItem(mean, median, q1, q3, 
                                     minRegular, maxRegular, 
                                     minOutlier, maxOutlier, 
                                     outliers);
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testInitialState() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        assertEquals(0, d.getRowCount());
        assertEquals(0, d.getColumnCount());
        assertTrue(Double.isNaN(d.getRangeLowerBound(true)));
        assertTrue(Double.isNaN(d.getRangeUpperBound(true)));
        assertEquals(new Range(0.0, 0.0), d.getRangeBounds(true));
        assertEquals(-1, d.getRowIndex("row"));
        assertEquals(-1, d.getColumnIndex("col"));
    }

    @Test(timeout = 4000)
    public void testAddAndRetrieveValues() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        List<Double> values = new ArrayList<Double>();
        values.add(1.0);
        values.add(2.0);
        values.add(3.0);
        values.add(4.0);
        values.add(5.0);
        d.add(values, "R1", "C1");
        
        assertEquals(1, d.getRowCount());
        assertEquals(1, d.getColumnCount());
        assertEquals("R1", d.getRowKey(0));
        assertEquals("C1", d.getColumnKey(0));
        assertEquals(0, d.getRowIndex("R1"));
        assertEquals(0, d.getColumnIndex("C1"));
        
        // Verify values are calculated (using BoxAndWhiskerCalculator)
        Number median = d.getMedianValue(0, 0);
        assertNotNull(median);
        assertEquals(3.0, median.doubleValue(), 0.0001);
        
        Number mean = d.getMeanValue(0, 0);
        assertNotNull(mean);
        assertEquals(3.0, mean.doubleValue(), 0.0001);
        
        Number q1 = d.getQ1Value(0, 0);
        assertNotNull(q1);
        assertEquals(2.0, q1.doubleValue(), 0.0001);
        
        Number q3 = d.getQ3Value(0, 0);
        assertNotNull(q3);
        assertEquals(4.0, q3.doubleValue(), 0.0001);
        
        Number minReg = d.getMinRegularValue(0, 0);
        assertNotNull(minReg);
        assertEquals(1.0, minReg.doubleValue(), 0.0001);
        
        Number maxReg = d.getMaxRegularValue(0, 0);
        assertNotNull(maxReg);
        assertEquals(5.0, maxReg.doubleValue(), 0.0001);
        
        Number minOut = d.getMinOutlier(0, 0);
        assertNotNull(minOut);
        assertEquals(1.0, minOut.doubleValue(), 0.0001);
        
        Number maxOut = d.getMaxOutlier(0, 0);
        assertNotNull(maxOut);
        assertEquals(5.0, maxOut.doubleValue(), 0.0001);
        
        List outliers = d.getOutliers(0, 0);
        assertNotNull(outliers);
        assertEquals(2, outliers.size());
    }

    @Test(timeout = 4000)
    public void testGetValueDelegatesToMedian() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        List<Double> values = new ArrayList<Double>();
        values.add(10.0);
        values.add(20.0);
        values.add(30.0);
        d.add(values, "R", "C");
        
        assertEquals(d.getMedianValue(0, 0), d.getValue(0, 0));
        assertEquals(d.getMedianValue("R", "C"), d.getValue("R", "C"));
    }

    @Test(timeout = 4000)
    public void testGetItemByIndexAndKey() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        List<Double> values = new ArrayList<Double>();
        values.add(1.0);
        values.add(2.0);
        d.add(values, "R", "C");
        
        BoxAndWhiskerItem item1 = d.getItem(0, 0);
        BoxAndWhiskerItem item2 = d.getItem("R", "C");
        assertNotNull(item1);
        assertNotNull(item2);
        assertEquals(item1, item2);
    }

    @Test(timeout = 4000)
    public void testGetRowKeysAndColumnKeys() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        List<Double> v1 = new ArrayList<Double>();
        v1.add(1.0);
        List<Double> v2 = new ArrayList<Double>();
        v2.add(2.0);
        d.add(v1, "R1", "C1");
        d.add(v2, "R2", "C2");
        
        List rowKeys = d.getRowKeys();
        List colKeys = d.getColumnKeys();
        assertEquals(2, rowKeys.size());
        assertEquals(2, colKeys.size());
        assertTrue(rowKeys.contains("R1"));
        assertTrue(rowKeys.contains("R2"));
        assertTrue(colKeys.contains("C1"));
        assertTrue(colKeys.contains("C2"));
    }

    // ==================== Partition B: Boundary Values & Extremes ====================

    @Test(timeout = 4000)
    public void testAddEmptyList() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        List<Double> empty = new ArrayList<Double>();
        d.add(empty, "R", "C");
        
        // Should not throw, but values may be null or NaN
        assertNotNull(d.getItem(0, 0));
        assertNull(d.getMedianValue(0, 0));
        assertNull(d.getMeanValue(0, 0));
        assertNull(d.getQ1Value(0, 0));
        assertNull(d.getQ3Value(0, 0));
        assertNull(d.getMinRegularValue(0, 0));
        assertNull(d.getMaxRegularValue(0, 0));
        assertNull(d.getMinOutlier(0, 0));
        assertNull(d.getMaxOutlier(0, 0));
        assertNull(d.getOutliers(0, 0));
    }

    @Test(timeout = 4000)
    public void testAddNullItem() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        // Adding null item should not throw immediately
        d.add((BoxAndWhiskerItem) null, "R", "C");
        // But retrieving should return null
        assertNull(d.getItem(0, 0));
    }

    @Test(timeout = 4000)
    public void testAddItemWithNullOutliers() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(1.0, 2.0, 3.0, 4.0, 
                                                      5.0, 6.0, null, null, null);
        d.add(item, "R", "C");
        
        assertNull(d.getMinOutlier(0, 0));
        assertNull(d.getMaxOutlier(0, 0));
        assertNull(d.getOutliers(0, 0));
        // Bounds should still be calculated from regular values
        assertEquals(5.0, d.getRangeLowerBound(true), 0.0001);
        assertEquals(6.0, d.getRangeUpperBound(true), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddMultipleItemsAndBounds() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        
        List<Double> v1 = new ArrayList<Double>();
        v1.add(1.0);
        v1.add(2.0);
        v1.add(3.0);
        d.add(v1, "R1", "C1");
        
        List<Double> v2 = new ArrayList<Double>();
        v2.add(10.0);
        v2.add(20.0);
        v2.add(30.0);
        d.add(v2, "R2", "C2");
        
        // Bounds should reflect min and max across all items
        assertEquals(1.0, d.getRangeLowerBound(true), 0.0001);
        assertEquals(30.0, d.getRangeUpperBound(true), 0.0001);
        Range range = d.getRangeBounds(true);
        assertEquals(1.0, range.getLowerBound(), 0.0001);
        assertEquals(30.0, range.getUpperBound(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddItemWithNegativeValues() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        List<Double> values = new ArrayList<Double>();
        values.add(-10.0);
        values.add(-5.0);
        values.add(-1.0);
        d.add(values, "R", "C");
        
        assertEquals(-10.0, d.getRangeLowerBound(true), 0.0001);
        assertEquals(-1.0, d.getRangeUpperBound(true), 0.0001);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * This test directly targets the known defect:
     * When adding a new item to an existing cell that currently holds the minimum
     * range value, and the new item's minimum is LARGER than the old minimum,
     * the cached minimumRangeValue is not correctly updated.
     * 
     * Expected: After replacing the cell, the minimum should be recalculated
     * from the remaining items (or the new item if it's the only one).
     * 
     * The defect causes the minimum to remain at the old (now stale) value.
     */
    @Test(timeout = 4000)
    public void testGetRangeBoundsWhenReplacingMinValueCell() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        
        // First add an item with min outlier 8.5
        List<Double> v1 = new ArrayList<Double>();
        v1.add(8.5);
        v1.add(9.0);
        v1.add(9.5);
        d.add(v1, "R1", "C1");
        
        // Add another item with larger values
        List<Double> v2 = new ArrayList<Double>();
        v2.add(9.0);
        v2.add(9.5);
        v2.add(10.0);
        d.add(v2, "R2", "C2");
        
        // Now replace the first cell (R1,C1) with a new item whose min is 8.6
        // This should update the minimum to 8.6 (since R2's min is 9.0)
        List<Double> v3 = new ArrayList<Double>();
        v3.add(8.6);
        v3.add(9.1);
        v3.add(9.6);
        d.add(v3, "R1", "C1");
        
        // The minimum should now be 8.6 (from the new R1) and max 10.0 (from R2)
        Range range = d.getRangeBounds(true);
        assertEquals(8.6, range.getLowerBound(), 0.0001);
        assertEquals(10.0, range.getUpperBound(), 0.0001);
        
        // Also test the specific case from the defect: expected Range[8.5,9.6] but was Range[8.6,9.6]
        // This is a different scenario - let's construct it exactly:
        DefaultBoxAndWhiskerCategoryDataset d2 = new DefaultBoxAndWhiskerCategoryDataset();
        
        // Add item with min 8.5, max 9.5
        List<Double> a1 = new ArrayList<Double>();
        a1.add(8.5);
        a1.add(9.0);
        a1.add(9.5);
        d2.add(a1, "R1", "C1");
        
        // Add item with min 9.0, max 9.6
        List<Double> a2 = new ArrayList<Double>();
        a2.add(9.0);
        a2.add(9.3);
        a2.add(9.6);
        d2.add(a2, "R2", "C2");
        
        // Replace R1,C1 with new item that has min 8.6, max 9.4
        List<Double> a3 = new ArrayList<Double>();
        a3.add(8.6);
        a3.add(9.0);
        a3.add(9.4);
        d2.add(a3, "R1", "C1");
        
        // Expected: min should be 8.6 (new R1), max should be 9.6 (from R2)
        Range r2 = d2.getRangeBounds(true);
        assertEquals(8.6, r2.getLowerBound(), 0.0001);
        assertEquals(9.6, r2.getUpperBound(), 0.0001);
        
        // Now the defect scenario: if we replace the cell that holds the min
        // with a new item that has a HIGHER min, the old min should be discarded
        DefaultBoxAndWhiskerCategoryDataset d3 = new DefaultBoxAndWhiskerCategoryDataset();
        
        List<Double> b1 = new ArrayList<Double>();
        b1.add(8.5);
        b1.add(9.0);
        b1.add(9.5);
        d3.add(b1, "R1", "C1");
        
        List<Double> b2 = new ArrayList<Double>();
        b2.add(9.0);
        b2.add(9.3);
        b2.add(9.6);
        d3.add(b2, "R2", "C2");
        
        // Replace R1,C1 with a new item that has min 8.6 (higher than 8.5)
        // The correct behavior: minimum should become 8.6 (from new R1) 
        // because R2's min is 9.0, so 8.6 is the new minimum
        List<Double> b3 = new ArrayList<Double>();
        b3.add(8.6);
        b3.add(9.0);
        b3.add(9.4);
        d3.add(b3, "R1", "C1");
        
        Range r3 = d3.getRangeBounds(true);
        // The bug would keep 8.5 as the minimum, but correct is 8.6
        assertEquals(8.6, r3.getLowerBound(), 0.0001);
        assertEquals(9.6, r3.getUpperBound(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testReplaceCellThatHoldsMaximum() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        
        List<Double> v1 = new ArrayList<Double>();
        v1.add(1.0);
        v1.add(2.0);
        v1.add(3.0);
        d.add(v1, "R1", "C1");
        
        List<Double> v2 = new ArrayList<Double>();
        v2.add(4.0);
        v2.add(5.0);
        v2.add(6.0);
        d.add(v2, "R2", "C2");
        
        // Replace R2,C2 (which holds max) with a lower max
        List<Double> v3 = new ArrayList<Double>();
        v3.add(3.0);
        v3.add(3.5);
        v3.add(4.0);
        d.add(v3, "R2", "C2");
        
        // Now max should be 4.0 (from new R2) and min 1.0 (from R1)
        Range range = d.getRangeBounds(true);
        assertEquals(1.0, range.getLowerBound(), 0.0001);
        assertEquals(4.0, range.getUpperBound(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testReplaceOnlyCell() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        
        List<Double> v1 = new ArrayList<Double>();
        v1.add(1.0);
        v1.add(2.0);
        v1.add(3.0);
        d.add(v1, "R", "C");
        
        // Replace the only cell with different values
        List<Double> v2 = new ArrayList<Double>();
        v2.add(5.0);
        v2.add(6.0);
        v2.add(7.0);
        d.add(v2, "R", "C");
        
        Range range = d.getRangeBounds(true);
        assertEquals(5.0, range.getLowerBound(), 0.0001);
        assertEquals(7.0, range.getUpperBound(), 0.0001);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetItemInvalidIndex() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        d.getItem(0, 0); // Should throw
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetMedianValueInvalidIndex() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        d.getMedianValue(5, 5);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetColumnKeyInvalidIndex() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        d.getColumnKey(0);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetRowKeyInvalidIndex() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        d.getRowKey(0);
    }

    @Test(timeout = 4000)
    public void testGetValueWithNullKeys() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        // Should return null, not throw
        assertNull(d.getValue(null, null));
        assertNull(d.getMedianValue(null, null));
        assertNull(d.getMeanValue(null, null));
        assertNull(d.getQ1Value(null, null));
        assertNull(d.getQ3Value(null, null));
        assertNull(d.getMinRegularValue(null, null));
        assertNull(d.getMaxRegularValue(null, null));
        assertNull(d.getMinOutlier(null, null));
        assertNull(d.getMaxOutlier(null, null));
        assertNull(d.getOutliers(null, null));
    }

    @Test(timeout = 4000)
    public void testAddWithNullRowKey() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        List<Double> values = new ArrayList<Double>();
        values.add(1.0);
        // Should not throw, but may create a null key entry
        d.add(values, null, "C");
        assertEquals(1, d.getRowCount());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEquals() {
        DefaultBoxAndWhiskerCategoryDataset d1 = new DefaultBoxAndWhiskerCategoryDataset();
        DefaultBoxAndWhiskerCategoryDataset d2 = new DefaultBoxAndWhiskerCategoryDataset();
        
        // Empty datasets are equal
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));
        
        // Same object
        assertTrue(d1.equals(d1));
        
        // Different types
        assertFalse(d1.equals("not a dataset"));
        assertFalse(d1.equals(null));
        
        // Add data to one
        List<Double> v1 = new ArrayList<Double>();
        v1.add(1.0);
        v1.add(2.0);
        d1.add(v1, "R", "C");
        
        assertFalse(d1.equals(d2));
        assertFalse(d2.equals(d1));
        
        // Add same data to other
        List<Double> v2 = new ArrayList<Double>();
        v2.add(1.0);
        v2.add(2.0);
        d2.add(v2, "R", "C");
        
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));
    }

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        List<Double> v1 = new ArrayList<Double>();
        v1.add(1.0);
        v1.add(2.0);
        v1.add(3.0);
        d.add(v1, "R", "C");
        
        DefaultBoxAndWhiskerCategoryDataset clone = (DefaultBoxAndWhiskerCategoryDataset) d.clone();
        
        // Should be equal but not same instance
        assertNotSame(d, clone);
        assertTrue(d.equals(clone));
        
        // Modify original, clone should be unaffected
        List<Double> v2 = new ArrayList<Double>();
        v2.add(10.0);
        v2.add(20.0);
        d.add(v2, "R2", "C2");
        
        assertFalse(d.equals(clone));
        assertEquals(1, clone.getRowCount());
        assertEquals(2, d.getRowCount());
    }

    @Test(timeout = 4000)
    public void testRangeBoundsConsistency() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        
        // Initially NaN
        assertTrue(Double.isNaN(d.getRangeLowerBound(true)));
        assertTrue(Double.isNaN(d.getRangeUpperBound(true)));
        
        // Add first item
        List<Double> v1 = new ArrayList<Double>();
        v1.add(2.0);
        v1.add(3.0);
        v1.add(4.0);
        d.add(v1, "R1", "C1");
        
        assertEquals(2.0, d.getRangeLowerBound(true), 0.0001);
        assertEquals(4.0, d.getRangeUpperBound(true), 0.0001);
        
        // Add second item with wider range
        List<Double> v2 = new ArrayList<Double>();
        v2.add(1.0);
        v2.add(3.0);
        v2.add(5.0);
        d.add(v2, "R2", "C2");
        
        assertEquals(1.0, d.getRangeLowerBound(true), 0.0001);
        assertEquals(5.0, d.getRangeUpperBound(true), 0.0001);
        
        // Add third item with narrower range (should not affect)
        List<Double> v3 = new ArrayList<Double>();
        v3.add(2.5);
        v3.add(3.0);
        v3.add(3.5);
        d.add(v3, "R3", "C3");
        
        assertEquals(1.0, d.getRangeLowerBound(true), 0.0001);
        assertEquals(5.0, d.getRangeUpperBound(true), 0.0001);
    }

    @Test(timeout = 4000)
    public void testGetRangeBoundsWithIncludeIntervalFlag() {
        DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
        List<Double> v1 = new ArrayList<Double>();
        v1.add(1.0);
        v1.add(2.0);
        v1.add(3.0);
        d.add(v1, "R", "C");
        
        // Both flags should return same result since we don't have interval data
        assertEquals(d.getRangeLowerBound(true), d.getRangeLowerBound(false), 0.0001);
        assertEquals(d.getRangeUpperBound(true), d.getRangeUpperBound(false), 0.0001);
        assertEquals(d.getRangeBounds(true), d.getRangeBounds(false));
    }
}