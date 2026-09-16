package org.jfree.data.xy;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Comprehensive JUnit 4 test suite for XYSeries, targeting maximum line/branch coverage
 * and the known defect bug 1955483 (IndexOutOfBoundsException in addOrUpdate when
 * autoSort=true, allowDuplicateXValues=true, and x-value already exists).
 *
 * <p>Branch & Defect Analysis Matrix:</p>
 * <ul>
 *   <li>Constructors: key null, autoSort, allowDuplicateXValues combinations</li>
 *   <li>add(XYDataItem) – null item, autoSort true/false, duplicate allowed/not</li>
 *   <li>add(Number,Number) – null x, null y, duplicate handling</li>
 *   <li>add(double,double) – primitive overloads</li>
 *   <li>addOrUpdate – existing x with duplicates allowed (bug trigger), existing x with duplicates not allowed, new x</li>
 *   <li>remove(int), remove(Number) – valid/invalid indices</li>
 *   <li>delete(int,int) – range deletion</li>
 *   <li>clear – empty vs non-empty</li>
 *   <li>update(Number,Number) – existing/non-existing x</li>
 *   <li>updateByIndex – valid index</li>
 *   <li>indexOf – sorted/unsorted, present/absent</li>
 *   <li>getItems – unmodifiable</li>
 *   <li>setMaximumItemCount – trimming</li>
 *   <li>toArray – null y values</li>
 *   <li>clone, createCopy, equals, hashCode</li>
 *   <li>Exception paths: null key, null item, duplicate x when not allowed, update non-existent</li>
 * </ul>
 */
public class XYSeriesDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructorDefaults() {
        XYSeries s = new XYSeries("S1");
        assertTrue("autoSort should be true", s.getAutoSort());
        assertTrue("allowDuplicateXValues should be true", s.getAllowDuplicateXValues());
        assertEquals("item count should be 0", 0, s.getItemCount());
    }

    @Test(timeout = 4000)
    public void testConstructorAutoSortFalse() {
        XYSeries s = new XYSeries("S1", false);
        assertFalse("autoSort should be false", s.getAutoSort());
        assertTrue("allowDuplicateXValues should be true", s.getAllowDuplicateXValues());
    }

    @Test(timeout = 4000)
    public void testConstructorAllFlags() {
        XYSeries s = new XYSeries("S1", false, false);
        assertFalse("autoSort should be false", s.getAutoSort());
        assertFalse("allowDuplicateXValues should be false", s.getAllowDuplicateXValues());
    }

    @Test(timeout = 4000)
    public void testAddXYDataItemSortedNoDuplicates() {
        XYSeries s = new XYSeries("S1", true, false);
        s.add(new XYDataItem(2.0, 20.0));
        s.add(new XYDataItem(1.0, 10.0));
        assertEquals("item count", 2, s.getItemCount());
        assertEquals("first x", 1.0, s.getX(0).doubleValue(), 0.0);
        assertEquals("second x", 2.0, s.getX(1).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddXYDataItemUnsorted() {
        XYSeries s = new XYSeries("S1", false, true);
        s.add(new XYDataItem(2.0, 20.0));
        s.add(new XYDataItem(1.0, 10.0));
        assertEquals("item count", 2, s.getItemCount());
        assertEquals("first x (unsorted)", 2.0, s.getX(0).doubleValue(), 0.0);
        assertEquals("second x (unsorted)", 1.0, s.getX(1).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddDuplicateAllowed() {
        XYSeries s = new XYSeries("S1", true, true);
        s.add(1.0, 10.0);
        s.add(1.0, 20.0);
        assertEquals("item count", 2, s.getItemCount());
        assertEquals("first y", 10.0, s.getY(0).doubleValue(), 0.0);
        assertEquals("second y", 20.0, s.getY(1).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddDuplicateNotAllowed() {
        XYSeries s = new XYSeries("S1", true, false);
        s.add(1.0, 10.0);
        try {
            s.add(1.0, 20.0);
            fail("Expected SeriesException for duplicate x");
        } catch (SeriesException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateNewX() {
        XYSeries s = new XYSeries("S1", true, false);
        XYDataItem result = s.addOrUpdate(1.0, 10.0);
        assertNull("no overwritten item", result);
        assertEquals("item count", 1, s.getItemCount());
        assertEquals("x", 1.0, s.getX(0).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateExistingXNoDuplicates() {
        XYSeries s = new XYSeries("S1", true, false);
        s.add(1.0, 10.0);
        XYDataItem result = s.addOrUpdate(1.0, 20.0);
        assertNotNull("should return overwritten item", result);
        assertEquals("overwritten y", 10.0, result.getY().doubleValue(), 0.0);
        assertEquals("item count unchanged", 1, s.getItemCount());
        assertEquals("updated y", 20.0, s.getY(0).doubleValue(), 0.0);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testAddNullY() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, (Number) null);
        assertEquals("item count", 1, s.getItemCount());
        assertNull("y should be null", s.getY(0));
    }

    @Test(timeout = 4000)
    public void testAddNullX() {
        XYSeries s = new XYSeries("S1");
        try {
            s.add((Number) null, 1.0);
            fail("Expected IllegalArgumentException for null x");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMaximumItemCountTrimming() {
        XYSeries s = new XYSeries("S1");
        s.setMaximumItemCount(2);
        s.add(1.0, 10.0);
        s.add(2.0, 20.0);
        s.add(3.0, 30.0); // should remove first
        assertEquals("item count", 2, s.getItemCount());
        assertEquals("first x after trim", 2.0, s.getX(0).doubleValue(), 0.0);
        assertEquals("second x after trim", 3.0, s.getX(1).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetItemsUnmodifiable() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        List items = s.getItems();
        try {
            items.add(new XYDataItem(2.0, 20.0));
            fail("List should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIndexOfSortedPresent() {
        XYSeries s = new XYSeries("S1", true, false);
        s.add(1.0, 10.0);
        s.add(3.0, 30.0);
        assertEquals("index of 1.0", 0, s.indexOf(1.0));
        assertEquals("index of 3.0", 1, s.indexOf(3.0));
    }

    @Test(timeout = 4000)
    public void testIndexOfSortedAbsent() {
        XYSeries s = new XYSeries("S1", true, false);
        s.add(1.0, 10.0);
        s.add(3.0, 30.0);
        assertTrue("index of 2.0 should be negative", s.indexOf(2.0) < 0);
    }

    @Test(timeout = 4000)
    public void testIndexOfUnsorted() {
        XYSeries s = new XYSeries("S1", false, true);
        s.add(3.0, 30.0);
        s.add(1.0, 10.0);
        assertEquals("index of 3.0", 0, s.indexOf(3.0));
        assertEquals("index of 1.0", 1, s.indexOf(1.0));
        assertEquals("index of absent", -1, s.indexOf(2.0));
    }

    @Test(timeout = 4000)
    public void testToArrayWithNullY() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, (Number) null);
        s.add(2.0, 20.0);
        double[][] arr = s.toArray();
        assertEquals("x[0]", 1.0, arr[0][0], 0.0);
        assertTrue("y[0] should be NaN", Double.isNaN(arr[1][0]));
        assertEquals("x[1]", 2.0, arr[0][1], 0.0);
        assertEquals("y[1]", 20.0, arr[1][1], 0.0);
    }

    // ========== Partition C: Defect-Targeted Branch Zone (Bug 1955483) ==========

    @Test(timeout = 4000)
    public void testBug1955483() {
        // Trigger: autoSort=true, allowDuplicateXValues=true, addOrUpdate with existing x
        XYSeries s = new XYSeries("S1", true, true);
        s.add(1.0, 10.0);
        // This call should not throw IndexOutOfBoundsException
        XYDataItem result = s.addOrUpdate(1.0, 20.0);
        // Expected behavior: since duplicates allowed, a new item should be added
        // (the method name "addOrUpdate" is ambiguous, but the bug is the crash)
        // We assert that no exception occurred and the series now has 2 items.
        assertEquals("item count should be 2", 2, s.getItemCount());
        // The first item should be the original (1.0,10.0), second the new (1.0,20.0)
        assertEquals("first x", 1.0, s.getX(0).doubleValue(), 0.0);
        assertEquals("first y", 10.0, s.getY(0).doubleValue(), 0.0);
        assertEquals("second x", 1.0, s.getX(1).doubleValue(), 0.0);
        assertEquals("second y", 20.0, s.getY(1).doubleValue(), 0.0);
        // The returned overwritten item should be null because no item was overwritten
        assertNull("overwritten should be null", result);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        XYSeries s = new XYSeries("S1");
        s.add((XYDataItem) null);
    }

    @Test(timeout = 4000, expected = SeriesException.class)
    public void testAddDuplicateNotAllowedSorted() {
        XYSeries s = new XYSeries("S1", true, false);
        s.add(1.0, 10.0);
        s.add(1.0, 20.0); // should throw
    }

    @Test(timeout = 4000, expected = SeriesException.class)
    public void testAddDuplicateNotAllowedUnsorted() {
        XYSeries s = new XYSeries("S1", false, false);
        s.add(1.0, 10.0);
        s.add(1.0, 20.0); // should throw
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddOrUpdateNullX() {
        XYSeries s = new XYSeries("S1");
        s.addOrUpdate(null, 1.0);
    }

    @Test(timeout = 4000, expected = SeriesException.class)
    public void testUpdateNonExistentX() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        s.update(2.0, 20.0); // no such x
    }

    @Test(timeout = 4000)
    public void testRemoveByIndex() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        s.add(2.0, 20.0);
        XYDataItem removed = s.remove(0);
        assertEquals("removed x", 1.0, removed.getX().doubleValue(), 0.0);
        assertEquals("remaining count", 1, s.getItemCount());
        assertEquals("remaining x", 2.0, s.getX(0).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testRemoveByX() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        s.add(2.0, 20.0);
        XYDataItem removed = s.remove(1.0);
        assertNotNull("removed item", removed);
        assertEquals("remaining count", 1, s.getItemCount());
    }

    @Test(timeout = 4000)
    public void testDeleteRange() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        s.add(2.0, 20.0);
        s.add(3.0, 30.0);
        s.delete(0, 1);
        assertEquals("remaining count", 1, s.getItemCount());
        assertEquals("remaining x", 3.0, s.getX(0).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testClearNonEmpty() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        s.clear();
        assertEquals("count after clear", 0, s.getItemCount());
    }

    @Test(timeout = 4000)
    public void testClearEmpty() {
        XYSeries s = new XYSeries("S1");
        s.clear(); // should not fire event
        assertEquals("count still 0", 0, s.getItemCount());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        s.add(2.0, 20.0);
        XYSeries clone = (XYSeries) s.clone();
        assertNotSame("different object", s, clone);
        assertEquals("same key", s.getKey(), clone.getKey());
        assertEquals("same item count", s.getItemCount(), clone.getItemCount());
        assertEquals("same x[0]", s.getX(0), clone.getX(0));
        // modify clone, original unchanged
        clone.add(3.0, 30.0);
        assertEquals("original count unchanged", 2, s.getItemCount());
    }

    @Test(timeout = 4000)
    public void testCreateCopy() throws CloneNotSupportedException {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        s.add(2.0, 20.0);
        s.add(3.0, 30.0);
        XYSeries copy = s.createCopy(0, 1);
        assertEquals("copy count", 2, copy.getItemCount());
        assertEquals("copy x[0]", 1.0, copy.getX(0).doubleValue(), 0.0);
        assertEquals("copy x[1]", 2.0, copy.getX(1).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testEquals() {
        XYSeries s1 = new XYSeries("S1", true, true);
        XYSeries s2 = new XYSeries("S1", true, true);
        assertEquals("equal empty series", s1, s2);
        s1.add(1.0, 10.0);
        assertFalse("different after add", s1.equals(s2));
        s2.add(1.0, 10.0);
        assertEquals("equal after same add", s1, s2);
        // different maximumItemCount
        s2.setMaximumItemCount(100);
        assertFalse("different max count", s1.equals(s2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        int h1 = s.hashCode();
        s.add(2.0, 20.0);
        int h2 = s.hashCode();
        assertNotEquals("hashCode should change when data changes", h1, h2);
    }

    @Test(timeout = 4000)
    public void testUpdateByIndex() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        s.updateByIndex(0, 99.0);
        assertEquals("updated y", 99.0, s.getY(0).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateUnsortedNoDuplicates() {
        XYSeries s = new XYSeries("S1", false, false);
        s.add(2.0, 20.0);
        s.add(1.0, 10.0);
        // update existing x=2.0
        XYDataItem result = s.addOrUpdate(2.0, 200.0);
        assertNotNull("overwritten", result);
        assertEquals("item count unchanged", 2, s.getItemCount());
        assertEquals("updated y", 200.0, s.getY(0).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateUnsortedNewX() {
        XYSeries s = new XYSeries("S1", false, true);
        s.add(2.0, 20.0);
        XYDataItem result = s.addOrUpdate(1.0, 10.0);
        assertNull("no overwrite", result);
        assertEquals("item count", 2, s.getItemCount());
        assertEquals("first x (unsorted)", 2.0, s.getX(0).doubleValue(), 0.0);
        assertEquals("second x", 1.0, s.getX(1).doubleValue(), 0.0);
    }
}