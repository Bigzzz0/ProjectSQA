package org.jfree.data.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TimeSeries.java from Defects4J
 * Known Defect: testCreateCopy3 - expected:<101.0> but was:<102.0>
 *   Root Cause: In createCopy(RegularTimePeriod start, RegularTimePeriod end),
 *   when end period is not in the series, the endIndex calculation is off by one,
 *   causing an extra item to be included in the copy.
 * 
 * Branches targeted:
 * 1. add() - empty series, append, insert, duplicate detection
 * 2. addOrUpdate() - update existing, insert new, bounds recalculation
 * 3. delete() - single period, range, bounds update
 * 4. createCopy(int,int) - normal, edge indices
 * 5. createCopy(RegularTimePeriod,RegularTimePeriod) - exact match, start/end not in series, empty range
 * 6. update() - update value, bounds recalculation
 * 7. removeAgedItems() - age-based removal
 * 8. setMaximumItemCount() - truncation
 * 9. getMinY()/getMaxY() - NaN handling, bounds tracking
 * 10. equals()/hashCode() - contract testing
 * 11. clone() - deep copy verification
 * 12. Exception paths - null arguments, invalid indices, duplicate periods
 */
public class TimeSeriesDeepseekTest {

    // ==================== PART A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testEmptySeriesState() {
        TimeSeries series = new TimeSeries("Test");
        assertEquals("Test", series.getKey());
        assertEquals("Time", series.getDomainDescription());
        assertEquals("Value", series.getRangeDescription());
        assertEquals(0, series.getItemCount());
        assertNull(series.getTimePeriodClass());
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
        assertEquals(Integer.MAX_VALUE, series.getMaximumItemCount());
        assertEquals(Long.MAX_VALUE, series.getMaximumItemAge());
    }

    @Test(timeout = 4000)
    public void testAddSingleItem() {
        TimeSeries series = new TimeSeries("Test");
        Day day = new Day(1, 1, 2020);
        series.add(day, 100.0);
        assertEquals(1, series.getItemCount());
        assertEquals(Day.class, series.getTimePeriodClass());
        assertEquals(100.0, series.getMinY(), 0.0001);
        assertEquals(100.0, series.getMaxY(), 0.0001);
        assertEquals(100.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddMultipleItemsAppend() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.add(new Day(3, 1, 2020), 150.0);
        assertEquals(3, series.getItemCount());
        assertEquals(100.0, series.getMinY(), 0.0001);
        assertEquals(200.0, series.getMaxY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddInsertInMiddle() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(3, 1, 2020), 300.0);
        series.add(new Day(2, 1, 2020), 200.0);
        assertEquals(3, series.getItemCount());
        assertEquals(100.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(200.0, series.getValue(1).doubleValue(), 0.0001);
        assertEquals(300.0, series.getValue(2).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000, expected = SeriesException.class)
    public void testAddDuplicatePeriod() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(1, 1, 2020), 200.0);
    }

    @Test(timeout = 4000, expected = SeriesException.class)
    public void testAddWrongPeriodClass() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Month(1, 2020), 200.0);
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateNewItem() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        TimeSeriesDataItem result = series.addOrUpdate(new Day(2, 1, 2020), 200.0);
        assertNull(result);
        assertEquals(2, series.getItemCount());
        assertEquals(200.0, series.getValue(1).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateExistingItem() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        TimeSeriesDataItem result = series.addOrUpdate(new Day(1, 1, 2020), 200.0);
        assertNotNull(result);
        assertEquals(100.0, result.getValue().doubleValue(), 0.0001);
        assertEquals(1, series.getItemCount());
        assertEquals(200.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateWithNullValue() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        TimeSeriesDataItem result = series.addOrUpdate(new Day(1, 1, 2020), (Number) null);
        assertNotNull(result);
        assertNull(series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testUpdateExistingItem() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.update(0, 150.0);
        assertEquals(150.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000, expected = SeriesException.class)
    public void testUpdateNonExistentPeriod() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.update(new Day(2, 1, 2020), 200.0);
    }

    @Test(timeout = 4000)
    public void testDeleteSinglePeriod() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.delete(new Day(1, 1, 2020));
        assertEquals(1, series.getItemCount());
        assertEquals(200.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testDeleteRange() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.add(new Day(3, 1, 2020), 300.0);
        series.delete(0, 1);
        assertEquals(1, series.getItemCount());
        assertEquals(300.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testDeleteAllItems() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.delete(0, 0);
        assertEquals(0, series.getItemCount());
        assertNull(series.getTimePeriodClass());
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
    }

    @Test(timeout = 4000)
    public void testClear() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.clear();
        assertEquals(0, series.getItemCount());
        assertNull(series.getTimePeriodClass());
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
    }

    // ==================== PART B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testSetMaximumItemCountTruncation() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.add(new Day(3, 1, 2020), 300.0);
        series.setMaximumItemCount(2);
        assertEquals(2, series.getItemCount());
        assertEquals(200.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(300.0, series.getValue(1).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetMaximumItemCountNegative() {
        TimeSeries series = new TimeSeries("Test");
        series.setMaximumItemCount(-1);
    }

    @Test(timeout = 4000)
    public void testSetMaximumItemAgeTriggersRemoval() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.add(new Day(3, 1, 2020), 300.0);
        series.setMaximumItemAge(1);
        // After setting age to 1, items older than 1 period from latest should be removed
        // Latest is Day(3,1,2020), serial index difference > 1 for Day(1,1,2020)
        assertEquals(2, series.getItemCount());
        assertEquals(200.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(300.0, series.getValue(1).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetMaximumItemAgeNegative() {
        TimeSeries series = new TimeSeries("Test");
        series.setMaximumItemAge(-1);
    }

    @Test(timeout = 4000)
    public void testGetMinYWithNaNValues() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), Double.NaN);
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
    }

    @Test(timeout = 4000)
    public void testGetMinYWithNullValues() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), (Number) null);
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
    }

    @Test(timeout = 4000)
    public void testBoundsAfterUpdate() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.update(0, 300.0);
        assertEquals(200.0, series.getMinY(), 0.0001);
        assertEquals(300.0, series.getMaxY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testBoundsAfterUpdateToMin() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.update(1, 50.0);
        assertEquals(50.0, series.getMinY(), 0.0001);
        assertEquals(100.0, series.getMaxY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testGetDataItemReturnsClone() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        TimeSeriesDataItem item = series.getDataItem(0);
        item.setValue(999.0);
        assertEquals(100.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testGetDataItemByPeriodNotFound() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        assertNull(series.getDataItem(new Day(2, 1, 2020)));
    }

    @Test(timeout = 4000)
    public void testGetIndex() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(3, 1, 2020), 300.0);
        assertEquals(0, series.getIndex(new Day(1, 1, 2020)));
        assertEquals(1, series.getIndex(new Day(3, 1, 2020)));
        assertEquals(-3, series.getIndex(new Day(2, 1, 2020))); // insertion point
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetIndexNullPeriod() {
        TimeSeries series = new TimeSeries("Test");
        series.getIndex(null);
    }

    @Test(timeout = 4000)
    public void testGetTimePeriods() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        Collection periods = series.getTimePeriods();
        assertEquals(2, periods.size());
    }

    @Test(timeout = 4000)
    public void testGetTimePeriodsUniqueToOtherSeries() {
        TimeSeries series1 = new TimeSeries("S1");
        series1.add(new Day(1, 1, 2020), 100.0);
        series1.add(new Day(2, 1, 2020), 200.0);
        TimeSeries series2 = new TimeSeries("S2");
        series2.add(new Day(2, 1, 2020), 300.0);
        series2.add(new Day(3, 1, 2020), 400.0);
        Collection unique = series1.getTimePeriodsUniqueToOtherSeries(series2);
        assertEquals(1, unique.size());
        assertTrue(unique.contains(new Day(3, 1, 2020)));
    }

    @Test(timeout = 4000)
    public void testGetNextTimePeriod() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        RegularTimePeriod next = series.getNextTimePeriod();
        assertEquals(new Day(2, 1, 2020), next);
    }

    // ==================== PART C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodExactMatch() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 101.0);
        series.add(new Day(3, 1, 2020), 102.0);
        series.add(new Day(4, 1, 2020), 103.0);
        try {
            TimeSeries copy = series.createCopy(new Day(2, 1, 2020), new Day(3, 1, 2020));
            assertEquals(2, copy.getItemCount());
            assertEquals(101.0, copy.getValue(0).doubleValue(), 0.0001);
            assertEquals(102.0, copy.getValue(1).doubleValue(), 0.0001);
        } catch (CloneNotSupportedException e) {
            fail("CloneNotSupportedException should not be thrown");
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodStartNotInSeries() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(3, 1, 2020), 102.0);
        series.add(new Day(4, 1, 2020), 103.0);
        try {
            // Start period (Day 2) is not in series, should start from first item after it (Day 3)
            TimeSeries copy = series.createCopy(new Day(2, 1, 2020), new Day(4, 1, 2020));
            assertEquals(2, copy.getItemCount());
            assertEquals(102.0, copy.getValue(0).doubleValue(), 0.0001);
            assertEquals(103.0, copy.getValue(1).doubleValue(), 0.0001);
        } catch (CloneNotSupportedException e) {
            fail("CloneNotSupportedException should not be thrown");
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodEndNotInSeries() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 101.0);
        series.add(new Day(3, 1, 2020), 102.0);
        series.add(new Day(5, 1, 2020), 104.0);
        try {
            // End period (Day 4) is not in series, should end at last item before it (Day 3)
            TimeSeries copy = series.createCopy(new Day(1, 1, 2020), new Day(4, 1, 2020));
            assertEquals(3, copy.getItemCount());
            assertEquals(100.0, copy.getValue(0).doubleValue(), 0.0001);
            assertEquals(101.0, copy.getValue(1).doubleValue(), 0.0001);
            assertEquals(102.0, copy.getValue(2).doubleValue(), 0.0001);
        } catch (CloneNotSupportedException e) {
            fail("CloneNotSupportedException should not be thrown");
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodBothNotInSeries() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(3, 1, 2020), 102.0);
        series.add(new Day(5, 1, 2020), 104.0);
        try {
            // Start (Day 2) not in series, end (Day 4) not in series
            // Should include items from Day 3 to Day 3 (since Day 4 is before Day 5)
            TimeSeries copy = series.createCopy(new Day(2, 1, 2020), new Day(4, 1, 2020));
            assertEquals(1, copy.getItemCount());
            assertEquals(102.0, copy.getValue(0).doubleValue(), 0.0001);
        } catch (CloneNotSupportedException e) {
            fail("CloneNotSupportedException should not be thrown");
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodEmptyRange() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(3, 1, 2020), 102.0);
        try {
            // Start after end in terms of data items
            TimeSeries copy = series.createCopy(new Day(4, 1, 2020), new Day(5, 1, 2020));
            assertEquals(0, copy.getItemCount());
        } catch (CloneNotSupportedException e) {
            fail("CloneNotSupportedException should not be thrown");
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodStartAfterEnd() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        try {
            series.createCopy(new Day(3, 1, 2020), new Day(1, 1, 2020));
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (CloneNotSupportedException e) {
            fail("Wrong exception type");
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodStartNull() {
        TimeSeries series = new TimeSeries("Test");
        try {
            series.createCopy(null, new Day(1, 1, 2020));
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (CloneNotSupportedException e) {
            fail("Wrong exception type");
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodEndNull() {
        TimeSeries series = new TimeSeries("Test");
        try {
            series.createCopy(new Day(1, 1, 2020), null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (CloneNotSupportedException e) {
            fail("Wrong exception type");
        }
    }

    // ==================== PART D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        TimeSeries series = new TimeSeries("Test");
        series.add((TimeSeriesDataItem) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddNullPeriod() {
        TimeSeries series = new TimeSeries("Test");
        series.add((RegularTimePeriod) null, 100.0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullName() {
        new TimeSeries(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDeleteInvalidRange() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.delete(1, 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCreateCopyInvalidIndices() {
        TimeSeries series = new TimeSeries("Test");
        try {
            series.createCopy(-1, 0);
        } catch (CloneNotSupportedException e) {
            fail("Should throw IllegalArgumentException first");
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCreateCopyStartGreaterThanEnd() {
        TimeSeries series = new TimeSeries("Test");
        try {
            series.createCopy(1, 0);
        } catch (CloneNotSupportedException e) {
            fail("Should throw IllegalArgumentException first");
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetDataItemNegativeIndex() {
        TimeSeries series = new TimeSeries("Test");
        series.getDataItem(-1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetDataItemOutOfBounds() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.getDataItem(5);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDeleteNullPeriod() {
        TimeSeries series = new TimeSeries("Test");
        series.delete((RegularTimePeriod) null);
    }

    // ==================== PART E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        TimeSeries series = new TimeSeries("Test");
        assertTrue(series.equals(series));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        TimeSeries series = new TimeSeries("Test");
        assertFalse(series.equals("Not a TimeSeries"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentDomain() {
        TimeSeries series1 = new TimeSeries("Test", "Domain1", "Range");
        TimeSeries series2 = new TimeSeries("Test", "Domain2", "Range");
        assertFalse(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentRange() {
        TimeSeries series1 = new TimeSeries("Test", "Domain", "Range1");
        TimeSeries series2 = new TimeSeries("Test", "Domain", "Range2");
        assertFalse(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentMaxItemCount() {
        TimeSeries series1 = new TimeSeries("Test");
        series1.setMaximumItemCount(10);
        TimeSeries series2 = new TimeSeries("Test");
        series2.setMaximumItemCount(20);
        assertFalse(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentMaxItemAge() {
        TimeSeries series1 = new TimeSeries("Test");
        series1.setMaximumItemAge(100);
        TimeSeries series2 = new TimeSeries("Test");
        series2.setMaximumItemAge(200);
        assertFalse(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentData() {
        TimeSeries series1 = new TimeSeries("Test");
        series1.add(new Day(1, 1, 2020), 100.0);
        TimeSeries series2 = new TimeSeries("Test");
        series2.add(new Day(1, 1, 2020), 200.0);
        assertFalse(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testEqualsSameData() {
        TimeSeries series1 = new TimeSeries("Test");
        series1.add(new Day(1, 1, 2020), 100.0);
        TimeSeries series2 = new TimeSeries("Test");
        series2.add(new Day(1, 1, 2020), 100.0);
        assertTrue(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        int hash1 = series.hashCode();
        int hash2 = series.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        TimeSeries clone = (TimeSeries) series.clone();
        assertNotSame(series, clone);
        assertEquals(series.getItemCount(), clone.getItemCount());
        assertEquals(series.getValue(0), clone.getValue(0));
        assertEquals(series.getValue(1), clone.getValue(1));
        // Verify deep copy - modifying clone should not affect original
        clone.add(new Day(3, 1, 2020), 300.0);
        assertEquals(2, series.getItemCount());
        assertEquals(3, clone.getItemCount());
    }

    @Test(timeout = 4000)
    public void testAddAndOrUpdate() {
        TimeSeries series1 = new TimeSeries("S1");
        series1.add(new Day(1, 1, 2020), 100.0);
        series1.add(new Day(2, 1, 2020), 200.0);
        TimeSeries series2 = new TimeSeries("S2");
        series2.add(new Day(2, 1, 2020), 300.0);
        series2.add(new Day(3, 1, 2020), 400.0);
        TimeSeries overwritten = series1.addAndOrUpdate(series2);
        assertEquals(3, series1.getItemCount());
        assertEquals(100.0, series1.getValue(0).doubleValue(), 0.0001);
        assertEquals(300.0, series1.getValue(1).doubleValue(), 0.0001);
        assertEquals(400.0, series1.getValue(2).doubleValue(), 0.0001);
        assertEquals(1, overwritten.getItemCount());
        assertEquals(200.0, overwritten.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItemsWithNotify() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.add(new Day(3, 1, 2020), 300.0);
        series.setMaximumItemAge(1);
        // removeAgedItems(true) is called internally by setMaximumItemAge
        assertEquals(2, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItemsWithLatest() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.add(new Day(3, 1, 2020), 300.0);
        series.setMaximumItemAge(1);
        // Use a date far in the future to avoid removal
        series.removeAgedItems(new java.util.Date(200, 0, 1).getTime(), true);
        // Items should remain as latest is far in future
        assertEquals(2, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItemsEmptySeries() {
        TimeSeries series = new TimeSeries("Test");
        series.removeAgedItems(true);
        assertEquals(0, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testGetItemsUnmodifiable() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        List items = series.getItems();
        try {
            items.add(new TimeSeriesDataItem(new Day(2, 1, 2020), 200.0));
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetDomainDescription() {
        TimeSeries series = new TimeSeries("Test");
        series.setDomainDescription("New Domain");
        assertEquals("New Domain", series.getDomainDescription());
    }

    @Test(timeout = 4000)
    public void testSetRangeDescription() {
        TimeSeries series = new TimeSeries("Test");
        series.setRangeDescription("New Range");
        assertEquals("New Range", series.getRangeDescription());
    }

    @Test(timeout = 4000)
    public void testGetRawDataItemByIndex() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        TimeSeriesDataItem raw = series.getRawDataItem(0);
        assertNotNull(raw);
        assertEquals(100.0, raw.getValue().doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testGetRawDataItemByPeriod() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        TimeSeriesDataItem raw = series.getRawDataItem(new Day(1, 1, 2020));
        assertNotNull(raw);
        assertNull(series.getRawDataItem(new Day(2, 1, 2020)));
    }

    @Test(timeout = 4000)
    public void testGetValueByPeriod() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        assertEquals(100.0, series.getValue(new Day(1, 1, 2020)).doubleValue(), 0.0001);
        assertNull(series.getValue(new Day(2, 1, 2020)));
    }

    @Test(timeout = 4000)
    public void testAddWithNotifyFalse() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0, false);
        assertEquals(1, series.getItemCount());
        assertEquals(100.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddWithNumberValue() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), new Double(100.0));
        assertEquals(100.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddWithNullNumberValue() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), (Number) null);
        assertNull(series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateWithDoubleValue() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        TimeSeriesDataItem result = series.addOrUpdate(new Day(1, 1, 2020), 200.0);
        assertNotNull(result);
        assertEquals(200.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateWithTimeSeriesDataItem() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        TimeSeriesDataItem newItem = new TimeSeriesDataItem(new Day(1, 1, 2020), 200.0);
        TimeSeriesDataItem result = series.addOrUpdate(newItem);
        assertNotNull(result);
        assertEquals(200.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddOrUpdateNullItem() {
        TimeSeries series = new TimeSeries("Test");
        series.addOrUpdate((TimeSeriesDataItem) null);
    }

    @Test(timeout = 4000)
    public void testDeleteWithNotifyFalse() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.delete(0, 0, false);
        assertEquals(1, series.getItemCount());
        assertEquals(200.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testCreateCopyByIndex() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.add(new Day(3, 1, 2020), 300.0);
        TimeSeries copy = series.createCopy(0, 1);
        assertEquals(2, copy.getItemCount());
        assertEquals(100.0, copy.getValue(0).doubleValue(), 0.0001);
        assertEquals(200.0, copy.getValue(1).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testCreateCopyEmptySeries() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test");
        TimeSeries copy = series.createCopy(0, 0);
        assertEquals(0, copy.getItemCount());
    }

    @Test(timeout = 4000)
    public void testGetTimePeriod() {
        TimeSeries series = new TimeSeries("Test");
        Day day = new Day(1, 1, 2020);
        series.add(day, 100.0);
        assertEquals(day, series.getTimePeriod(0));
    }

    @Test(timeout = 4000)
    public void testConstructorWithDomainAndRange() {
        TimeSeries series = new TimeSeries("Test", "My Domain", "My Range");
        assertEquals("My Domain", series.getDomainDescription());
        assertEquals("My Range", series.getRangeDescription());
    }

    @Test(timeout = 4000)
    public void testBoundsAfterDeleteAllItems() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.clear();
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
    }

    @Test(timeout = 4000)
    public void testBoundsAfterDeleteSingleItem() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.add(new Day(3, 1, 2020), 150.0);
        series.delete(new Day(2, 1, 2020));
        assertEquals(100.0, series.getMinY(), 0.0001);
        assertEquals(150.0, series.getMaxY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testBoundsAfterDeleteMinItem() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.delete(new Day(1, 1, 2020));
        assertEquals(200.0, series.getMinY(), 0.0001);
        assertEquals(200.0, series.getMaxY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testBoundsAfterDeleteMaxItem() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.delete(new Day(2, 1, 2020));
        assertEquals(100.0, series.getMinY(), 0.0001);
        assertEquals(100.0, series.getMaxY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddWithMaximumItemCountExceeded() {
        TimeSeries series = new TimeSeries("Test");
        series.setMaximumItemCount(2);
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.add(new Day(3, 1, 2020), 300.0);
        assertEquals(2, series.getItemCount());
        assertEquals(200.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(300.0, series.getValue(1).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateWithMaximumItemCountExceeded() {
        TimeSeries series = new TimeSeries("Test");
        series.setMaximumItemCount(2);
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        series.addOrUpdate(new Day(3, 1, 2020), 300.0);
        assertEquals(2, series.getItemCount());
        assertEquals(200.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(300.0, series.getValue(1).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testUpdateWithBoundsRecalculation() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);
        // Update min value to be even smaller
        series.update(0, 50.0);
        assertEquals(50.0, series.getMinY(), 0.0001);
        assertEquals(200.0, series.getMaxY(), 0.0001);
        // Update max value to be even larger
        series.update(1, 300.0);
        assertEquals(50.0, series.getMinY(), 0.0001);
        assertEquals(300.0, series.getMaxY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testUpdateWithNullValue() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 100.0);
        series.update(0, (Number) null);
        assertNull(series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItemsEmptyData() {
        TimeSeries series = new TimeSeries("Test");
        series.removeAgedItems(1000L, true);
        assertEquals(0, series.getItemCount());
    }
}