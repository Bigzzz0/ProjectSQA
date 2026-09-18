/* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Class: org.jfree.data.xy.XYSeries
 * Target Environment: Java 8 / JUnit 4 / Defects4J Benchmark
 *
 * Decision / Condition Matrix:
 * -------------------------------------------------------------------------
 * Method                   Branch / Condition Tested
 * -------------------------------------------------------------------------
 * XYSeries(...)            - Constructors with default and custom flags
 *                          - Key validation (inherited from Series)
 * getItems()               - Returns unmodifiable list; mutation throws UnsupportedOperationException
 * setMaximumItemCount(int) - maximum < current size (removes items from head, fires event)
 *                          - maximum >= current size (no removal, no event)
 * add(XYDataItem, boolean) - item == null (IllegalArgumentException)
 *                          - autoSort=true, index < 0 (insert at -index-1)
 *                          - autoSort=true, index >= 0, allowDuplicate=true (insert after duplicate chain)
 *                            * duplicate chain middle insertion vs end-of-list insertion
 *                          - autoSort=true, index >= 0, allowDuplicate=false (SeriesException)
 *                          - autoSort=false, allowDuplicate=false, existing key (SeriesException)
 *                          - autoSort=false, allowDuplicate=false, new key (appends)
 *                          - autoSort=false, allowDuplicate=true (appends duplicates)
 *                          - size > maximumItemCount (removes oldest item at index 0)
 *                          - notify=true (fires event) vs notify=false (silent)
 * delete(int, int)         - Iterative range deletion, event notification
 * remove(int)              - By index, returns removed item, fires event
 * remove(Number)           - By X-value, delegates to indexOf, fires event
 * clear()                  - size == 0 (no-op, no event) vs size > 0 (clears, fires event)
 * updateByIndex(int, Num)  - Modifies Y-value in place, fires event
 * update(Number, Number)   - Non-existent X (SeriesException)
 *                          - Existing X (updates Y, fires event)
 * addOrUpdate(Number, Num) - x == null (IllegalArgumentException)
 *                          - Existing X and allowDuplicate=false (updates Y, returns cloned item)
 *                          - Unsorted append / Sorted insertion
 *                          - DEFECT BUG 1955483: autoSort=true, allowDuplicate=true, adding duplicate X
 *                            triggers IndexOutOfBoundsException (-index - 1 evaluates to -1 when index >= 0)
 * indexOf(Number)          - autoSort=true (binary search hit / miss)
 *                          - autoSort=false (linear scan hit / miss)
 * toArray()                - Empty series [2][0]
 *                          - Non-null Y coordinates vs null Y coordinates (converted to Double.NaN)
 * clone() & createCopy()   - Deep copy verification (subsequent mutations are independent)
 * equals() & hashCode()    - Identity, non-XYSeries, super inequality, maxItemCount divergence,
 *                            autoSort divergence, duplicate flag divergence, data list divergence,
 *                            hashCode() branches for itemCount = 0, 1, 2, 3+.
 * =========================================================================
 */

package org.jfree.data.xy;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.general.SeriesException;

public class XYSeriesGeminiTest {

    // Helper listener to verify event dispatch semantics
    private static class EventCounter implements SeriesChangeListener {
        private int count = 0;

        @Override
        public void seriesChanged(SeriesChangeEvent event) {
            this.count++;
        }

        public int getCount() {
            return this.count;
        }

        public void reset() {
            this.count = 0;
        }
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndInitialState() {
        XYSeries series1 = new XYSeries("S1");
        assertEquals("S1", series1.getKey());
        assertTrue(series1.getAutoSort());
        assertTrue(series1.getAllowDuplicateXValues());
        assertEquals(0, series1.getItemCount());
        assertEquals(Integer.MAX_VALUE, series1.getMaximumItemCount());

        XYSeries series2 = new XYSeries("S2", false);
        assertFalse(series2.getAutoSort());
        assertTrue(series2.getAllowDuplicateXValues());

        XYSeries series3 = new XYSeries("S3", false, false);
        assertFalse(series3.getAutoSort());
        assertFalse(series3.getAllowDuplicateXValues());
    }

    @Test(timeout = 4000)
    public void testAutoSortAscendingOrder() {
        XYSeries series = new XYSeries("Sorted", true, false);
        series.add(5.0, 50.0);
        series.add(1.0, 10.0);
        series.add(3.0, 30.0);

        assertEquals(3, series.getItemCount());
        assertEquals(new Double(1.0), series.getX(0));
        assertEquals(new Double(10.0), series.getY(0));
        assertEquals(new Double(3.0), series.getX(1));
        assertEquals(new Double(30.0), series.getY(1));
        assertEquals(new Double(5.0), series.getX(2));
        assertEquals(new Double(50.0), series.getY(2));
    }

    @Test(timeout = 4000)
    public void testUnsortedInsertionOrder() {
        XYSeries series = new XYSeries("Unsorted", false, true);
        series.add(5.0, 50.0);
        series.add(1.0, 10.0);
        series.add(3.0, 30.0);

        assertEquals(3, series.getItemCount());
        assertEquals(new Double(5.0), series.getX(0));
        assertEquals(new Double(1.0), series.getX(1));
        assertEquals(new Double(3.0), series.getX(2));
    }

    @Test(timeout = 4000)
    public void testAddVariantsAndNotification() {
        XYSeries series = new XYSeries("Events");
        EventCounter listener = new EventCounter();
        series.addChangeListener(listener);

        series.add(1.0, 2.0); // notify=true by default
        assertEquals(1, listener.getCount());

        series.add(2.0, 3.0, false); // notify=false
        assertEquals(1, listener.getCount());

        series.add(3.0, (Number) null); // null y
        assertEquals(2, listener.getCount());
        assertNull(series.getY(2));

        series.add(4.0, (Number) null, false);
        assertEquals(2, listener.getCount());
        assertNull(series.getY(3));

        series.add(new Double(5.0), new Double(50.0));
        assertEquals(3, listener.getCount());

        series.add(new Double(6.0), new Double(60.0), false);
        assertEquals(3, listener.getCount());
    }

    @Test(timeout = 4000)
    public void testUpdateByIndexAndNotification() {
        XYSeries series = new XYSeries("UpdateByIndex");
        EventCounter listener = new EventCounter();
        series.add(10.0, 20.0);
        series.addChangeListener(listener);

        series.updateByIndex(0, 99.0);
        assertEquals(new Double(99.0), series.getY(0));
        assertEquals(1, listener.getCount());

        series.updateByIndex(0, null);
        assertNull(series.getY(0));
        assertEquals(2, listener.getCount());
    }

    @Test(timeout = 4000)
    public void testUpdateByXValue() {
        XYSeries series = new XYSeries("Update", true, false);
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);

        EventCounter listener = new EventCounter();
        series.addChangeListener(listener);

        series.update(new Double(2.0), new Double(200.0));
        assertEquals(new Double(200.0), series.getY(1));
        assertEquals(1, listener.getCount());
    }

    @Test(timeout = 4000)
    public void testDeleteAndRemove() {
        XYSeries series = new XYSeries("Delete", true, true);
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        series.add(3.0, 30.0);
        series.add(4.0, 40.0);

        EventCounter listener = new EventCounter();
        series.addChangeListener(listener);

        XYDataItem removedItem = series.remove(1);
        assertEquals(new Double(2.0), removedItem.getX());
        assertEquals(new Double(20.0), removedItem.getY());
        assertEquals(3, series.getItemCount());
        assertEquals(1, listener.getCount());

        XYDataItem removedByX = series.remove(new Double(4.0));
        assertEquals(new Double(4.0), removedByX.getX());
        assertEquals(2, series.getItemCount());
        assertEquals(2, listener.getCount());

        series.delete(0, 1);
        assertEquals(0, series.getItemCount());
        assertEquals(3, listener.getCount());
    }

    @Test(timeout = 4000)
    public void testClearBehavior() {
        XYSeries series = new XYSeries("Clear");
        EventCounter listener = new EventCounter();
        series.addChangeListener(listener);

        series.clear(); // empty clear: no-op, no event
        assertEquals(0, listener.getCount());

        series.add(1.0, 10.0);
        assertEquals(1, listener.getCount());

        series.clear(); // non-empty clear: fires event
        assertEquals(0, series.getItemCount());
        assertEquals(2, listener.getCount());
    }

    @Test(timeout = 4000)
    public void testSetMaximumItemCount() {
        XYSeries series = new XYSeries("MaxCount");
        EventCounter listener = new EventCounter();

        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        series.add(3.0, 30.0);

        series.addChangeListener(listener);

        // Setting higher limit should not remove items or fire event
        series.setMaximumItemCount(5);
        assertEquals(5, series.getMaximumItemCount());
        assertEquals(3, series.getItemCount());
        assertEquals(0, listener.getCount());

        // Setting lower limit should trim oldest items from head and fire event
        series.setMaximumItemCount(2);
        assertEquals(2, series.getMaximumItemCount());
        assertEquals(2, series.getItemCount());
        assertEquals(new Double(2.0), series.getX(0));
        assertEquals(new Double(3.0), series.getX(1));
        assertEquals(1, listener.getCount());

        // Adding while at capacity should pop the head
        series.add(4.0, 40.0);
        assertEquals(2, series.getItemCount());
        assertEquals(new Double(3.0), series.getX(0));
        assertEquals(new Double(4.0), series.getX(1));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testToArrayWithNullAndValidCoordinates() {
        XYSeries series = new XYSeries("ToArray");
        double[][] emptyArr = series.toArray();
        assertEquals(2, emptyArr.length);
        assertEquals(0, emptyArr[0].length);
        assertEquals(0, emptyArr[1].length);

        series.add(1.5, 3.5);
        series.add(2.5, (Number) null);

        double[][] arr = series.toArray();
        assertEquals(2, arr.length);
        assertEquals(2, arr[0].length);
        assertEquals(2, arr[1].length);

        assertEquals(1.5, arr[0][0], 1e-9);
        assertEquals(3.5, arr[1][0], 1e-9);

        assertEquals(2.5, arr[0][1], 1e-9);
        assertTrue(Double.isNaN(arr[1][1]));
    }

    @Test(timeout = 4000)
    public void testIndexOfSortedAndUnsorted() {
        XYSeries sorted = new XYSeries("Sorted", true, true);
        sorted.add(10.0, 1.0);
        sorted.add(20.0, 2.0);
        sorted.add(30.0, 3.0);

        assertEquals(0, sorted.indexOf(new Double(10.0)));
        assertEquals(1, sorted.indexOf(new Double(20.0)));
        assertEquals(2, sorted.indexOf(new Double(30.0)));
        assertTrue(sorted.indexOf(new Double(25.0)) < 0);

        XYSeries unsorted = new XYSeries("Unsorted", false, true);
        unsorted.add(30.0, 3.0);
        unsorted.add(10.0, 1.0);
        unsorted.add(20.0, 2.0);

        assertEquals(0, unsorted.indexOf(new Double(30.0)));
        assertEquals(1, unsorted.indexOf(new Double(10.0)));
        assertEquals(2, unsorted.indexOf(new Double(20.0)));
        assertEquals(-1, unsorted.indexOf(new Double(99.0)));
    }

    @Test(timeout = 4000)
    public void testAutoSortMultipleDuplicateChain() {
        // Test insertion logic when multiple duplicate keys already exist
        XYSeries series = new XYSeries("Duplicates", true, true);
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        series.add(2.0, 21.0);
        series.add(3.0, 30.0);

        // Add duplicate into middle of list
        series.add(2.0, 22.0);
        assertEquals(5, series.getItemCount());
        assertEquals(new Double(2.0), series.getX(1));
        assertEquals(new Double(2.0), series.getX(2));
        assertEquals(new Double(2.0), series.getX(3));
        assertEquals(new Double(3.0), series.getX(4));

        // Add duplicate at the very end of list
        series.add(3.0, 31.0);
        assertEquals(6, series.getItemCount());
        assertEquals(new Double(3.0), series.getX(4));
        assertEquals(new Double(3.0), series.getX(5));
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateUnsortedUnique() {
        XYSeries series = new XYSeries("AddOrUpdateUnsorted", false, false);
        assertNull(series.addOrUpdate(10.0, 100.0));
        assertNull(series.addOrUpdate(5.0, 50.0));
        assertEquals(2, series.getItemCount());

        XYDataItem overwritten = series.addOrUpdate(10.0, 150.0);
        assertNotNull(overwritten);
        assertEquals(new Double(10.0), overwritten.getX());
        assertEquals(new Double(100.0), overwritten.getY());
        assertEquals(2, series.getItemCount());
        assertEquals(new Double(150.0), series.getY(0));
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateSortedUniqueExceedingCapacity() {
        XYSeries series = new XYSeries("AddOrUpdateSortedCap", true, false);
        series.setMaximumItemCount(2);

        assertNull(series.addOrUpdate(1.0, 10.0));
        assertNull(series.addOrUpdate(3.0, 30.0));
        assertEquals(2, series.getItemCount());

        // Adding 2.0 inserts in middle, then item count is 3 > 2 -> head (1.0) dropped
        assertNull(series.addOrUpdate(2.0, 20.0));
        assertEquals(2, series.getItemCount());
        assertEquals(new Double(2.0), series.getX(0));
        assertEquals(new Double(3.0), series.getX(1));

        // Update existing item 3.0 -> no capacity trimming
        XYDataItem overwritten = series.addOrUpdate(3.0, 300.0);
        assertNotNull(overwritten);
        assertEquals(new Double(30.0), overwritten.getY());
        assertEquals(2, series.getItemCount());
        assertEquals(new Double(300.0), series.getY(1));
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateUnsortedAllowDuplicates() {
        XYSeries series = new XYSeries("UnsortedAllowDup", false, true);
        assertNull(series.addOrUpdate(1.0, 10.0));
        assertNull(series.addOrUpdate(1.0, 20.0));
        assertEquals(2, series.getItemCount());
        assertEquals(new Double(10.0), series.getY(0));
        assertEquals(new Double(20.0), series.getY(1));
    }

    @Test(timeout = 4000)
    public void testGetItemsImmutability() {
        XYSeries series = new XYSeries("Items");
        series.add(1.0, 10.0);
        List items = series.getItems();
        assertEquals(1, items.size());

        try {
            items.add(new XYDataItem(2.0, 20.0));
            fail("getItems() must return an unmodifiable list.");
        } catch (UnsupportedOperationException expected) {
            // Expected
        }
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Bug 1955483)
    // =========================================================================

    /**
     * Directly targets Defects4J bug 1955483.
     * When autoSort=true AND allowDuplicateXValues=true, invoking addOrUpdate()
     * with an existing duplicate X-value causes indexOf(x) to return >= 0.
     * The flawed code executes: this.data.add(-index - 1, ...), which translates to
     * index -1, resulting in java.lang.IndexOutOfBoundsException: Index: -1, Size: 1.
     * Correct behavior must cleanly append or insert the duplicate without throwing IndexOutOfBoundsException.
     */
    @Test(timeout = 4000)
    public void testBug1955483() {
        XYSeries series = new XYSeries("Series", true, true);
        series.addOrUpdate(1.0, 1.0);
        series.addOrUpdate(1.0, 2.0);

        assertEquals(2, series.getItemCount());
        assertEquals(new Double(1.0), series.getX(0));
        assertEquals(new Double(1.0), series.getY(0));
        assertEquals(new Double(1.0), series.getX(1));
        assertEquals(new Double(2.0), series.getY(1));
    }

    @Test(timeout = 4000)
    public void testBug1955483WithMultipleDuplicates() {
        XYSeries series = new XYSeries("SeriesMulti", true, true);
        series.addOrUpdate(new Double(5.0), new Double(10.0));
        series.addOrUpdate(new Double(5.0), new Double(20.0));
        series.addOrUpdate(new Double(5.0), new Double(30.0));

        assertEquals(3, series.getItemCount());
        assertEquals(new Double(10.0), series.getY(0));
        assertEquals(new Double(20.0), series.getY(1));
        assertEquals(new Double(30.0), series.getY(2));
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullKey() {
        new XYSeries(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddNullItem() {
        XYSeries series = new XYSeries("NullItem");
        series.add((XYDataItem) null);
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testAddDuplicateSortedNotAllowed() {
        XYSeries series = new XYSeries("NoDuplicates", true, false);
        series.add(1.0, 10.0);
        series.add(1.0, 20.0);
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testAddDuplicateUnsortedNotAllowed() {
        XYSeries series = new XYSeries("NoDuplicatesUnsorted", false, false);
        series.add(5.0, 50.0);
        series.add(2.0, 20.0);
        series.add(5.0, 99.0);
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testUpdateNonExistentX() {
        XYSeries series = new XYSeries("UpdateNonExistent");
        series.add(1.0, 10.0);
        series.update(new Double(99.0), new Double(20.0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddOrUpdateNullX() {
        XYSeries series = new XYSeries("NullX");
        series.addOrUpdate((Number) null, new Double(1.0));
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetDataItemIndexOutOfBounds() {
        XYSeries series = new XYSeries("OutOfBounds");
        series.getDataItem(0);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloneIndependence() throws CloneNotSupportedException {
        XYSeries s1 = new XYSeries("Original", true, true);
        s1.add(1.0, 10.0);
        s1.add(2.0, 20.0);

        XYSeries s2 = (XYSeries) s1.clone();
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());

        // Mutate original
        s1.add(3.0, 30.0);
        assertEquals(3, s1.getItemCount());
        assertEquals(2, s2.getItemCount());
        assertFalse(s1.equals(s2));

        // Mutate clone
        s2.updateByIndex(0, 999.0);
        assertNotEquals(s1.getY(0), s2.getY(0));
    }

    @Test(timeout = 4000)
    public void testCreateCopy() throws CloneNotSupportedException {
        XYSeries s1 = new XYSeries("Original", true, false);
        s1.add(1.0, 10.0);
        s1.add(2.0, 20.0);
        s1.add(3.0, 30.0);
        s1.add(4.0, 40.0);

        XYSeries copy = s1.createCopy(1, 2);
        assertEquals(2, copy.getItemCount());
        assertEquals(new Double(2.0), copy.getX(0));
        assertEquals(new Double(20.0), copy.getY(0));
        assertEquals(new Double(3.0), copy.getX(1));
        assertEquals(new Double(30.0), copy.getY(1));

        // Modifying copy does not mutate original
        copy.updateByIndex(0, 777.0);
        assertEquals(new Double(20.0), s1.getY(1));
    }

    @Test(timeout = 4000)
    public void testCreateCopyEmptySeries() throws CloneNotSupportedException {
        XYSeries empty = new XYSeries("Empty");
        XYSeries copy = empty.createCopy(0, 0);
        assertEquals(0, copy.getItemCount());
        assertEquals("Empty", copy.getKey());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        XYSeries s1 = new XYSeries("Series", true, true);
        XYSeries s2 = new XYSeries("Series", true, true);

        // Identity and null check
        assertTrue(s1.equals(s1));
        assertFalse(s1.equals(null));
        assertFalse(s1.equals("NotAnXYSeries"));

        // Match check
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());

        // Diverge Key
        XYSeries diffKey = new XYSeries("DifferentKey", true, true);
        assertFalse(s1.equals(diffKey));

        // Diverge autoSort
        XYSeries diffAutoSort = new XYSeries("Series", false, true);
        assertFalse(s1.equals(diffAutoSort));

        // Diverge allowDuplicateXValues
        XYSeries diffAllowDup = new XYSeries("Series", true, false);
        assertFalse(s1.equals(diffAllowDup));

        // Diverge maximumItemCount
        s2.setMaximumItemCount(10);
        assertFalse(s1.equals(s2));
        s2.setMaximumItemCount(Integer.MAX_VALUE);
        assertTrue(s1.equals(s2));

        // Diverge Data items
        s1.add(1.0, 10.0);
        assertFalse(s1.equals(s2));
        s2.add(1.0, 10.0);
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(2.0, 20.0);
        s2.add(2.0, 99.0);
        assertFalse(s1.equals(s2));
    }

    @Test(timeout = 4000)
    public void testHashCodeBranchCoverageForSizes() {
        // Covers itemCount == 0
        XYSeries s0 = new XYSeries("H0");
        int h0 = s0.hashCode();

        // Covers itemCount == 1 (count > 0 branch)
        XYSeries s1 = new XYSeries("H1");
        s1.add(1.0, 10.0);
        int h1 = s1.hashCode();
        assertNotEquals(h0, h1);

        // Covers itemCount == 2 (count > 1 branch)
        XYSeries s2 = new XYSeries("H2");
        s2.add(1.0, 10.0);
        s2.add(2.0, 20.0);
        int h2 = s2.hashCode();
        assertNotEquals(h1, h2);

        // Covers itemCount == 3+ (count > 2 branch)
        XYSeries s3 = new XYSeries("H3");
        s3.add(1.0, 10.0);
        s3.add(2.0, 20.0);
        s3.add(3.0, 30.0);
        int h3 = s3.hashCode();
        assertNotEquals(h2, h3);
    }
}