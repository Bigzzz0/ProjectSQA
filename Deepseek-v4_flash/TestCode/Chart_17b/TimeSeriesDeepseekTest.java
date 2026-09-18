package org.jfree.data.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.jfree.data.time.TimeSeries
 * 
 * Defect: testBug1832432 - clone() throws IllegalArgumentException "Requires start <= end."
 * Root cause: clone() calls createCopy(0, getItemCount() - 1). When series is empty,
 * getItemCount() returns 0, so createCopy(0, -1) is invoked, which throws
 * IllegalArgumentException because end < start.
 * 
 * Branch coverage targets:
 * - Constructor variants (name only, name+class, full)
 * - getDomainDescription/setDomainDescription (null and non-null)
 * - getRangeDescription/setRangeDescription (null and non-null)
 * - getItemCount (empty, non-empty)
 * - getItems (unmodifiable check)
 * - getMaximumItemCount/setMaximumItemCount (normal, negative, trimming)
 * - getMaximumItemAge/setMaximumItemAge (normal, negative, aging)
 * - getTimePeriodClass
 * - getDataItem(int) - valid index, out of bounds
 * - getDataItem(RegularTimePeriod) - found, not found, null period
 * - getTimePeriod(int)
 * - getNextTimePeriod
 * - getTimePeriods
 * - getTimePeriodsUniqueToOtherSeries
 * - getIndex - found, not found, null period
 * - getValue(int), getValue(RegularTimePeriod) - found, not found
 * - add(TimeSeriesDataItem) - null, wrong class, empty series, append, insert, duplicate
 * - add(RegularTimePeriod, double/Number) with notify flags
 * - update(RegularTimePeriod, Number) - found, not found
 * - update(int, Number)
 * - addAndOrUpdate
 * - addOrUpdate - update existing, add new, null period
 * - removeAgedItems(boolean) - with/without aging
 * - removeAgedItems(long, boolean)
 * - clear - empty, non-empty
 * - delete(RegularTimePeriod) - found, not found
 * - delete(int, int) - valid, invalid range
 * - clone - empty series (defect), non-empty series
 * - createCopy(int, int) - valid, invalid start, invalid end
 * - createCopy(RegularTimePeriod, RegularTimePeriod) - null args, invalid range, empty range
 * - equals - same object, different class, different fields, same fields
 * - hashCode - empty, non-empty
 */
public class TimeSeriesDeepseekTest {

    /* ==================== Partition A: Core Functional Logic & State Transitions ==================== */

    @Test(timeout = 4000)
    public void testConstructorAndBasicGetters() {
        TimeSeries series = new TimeSeries("Test");
        assertEquals("Test", series.getKey());
        assertEquals("Time", series.getDomainDescription());
        assertEquals("Value", series.getRangeDescription());
        assertEquals(Day.class, series.getTimePeriodClass());
        assertEquals(0, series.getItemCount());
        assertEquals(Integer.MAX_VALUE, series.getMaximumItemCount());
        assertEquals(Long.MAX_VALUE, series.getMaximumItemAge());
    }

    @Test(timeout = 4000)
    public void testConstructorWithTimePeriodClass() {
        TimeSeries series = new TimeSeries("Test", Year.class);
        assertEquals(Year.class, series.getTimePeriodClass());
        assertEquals(0, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testConstructorWithDescriptions() {
        TimeSeries series = new TimeSeries("Test", "Domain", "Range", Month.class);
        assertEquals("Domain", series.getDomainDescription());
        assertEquals("Range", series.getRangeDescription());
        assertEquals(Month.class, series.getTimePeriodClass());
    }

    @Test(timeout = 4000)
    public void testSetDomainDescription() {
        TimeSeries series = new TimeSeries("Test");
        series.setDomainDescription("New Domain");
        assertEquals("New Domain", series.getDomainDescription());
        series.setDomainDescription(null);
        assertNull(series.getDomainDescription());
    }

    @Test(timeout = 4000)
    public void testSetRangeDescription() {
        TimeSeries series = new TimeSeries("Test");
        series.setRangeDescription("New Range");
        assertEquals("New Range", series.getRangeDescription());
        series.setRangeDescription(null);
        assertNull(series.getRangeDescription());
    }

    @Test(timeout = 4000)
    public void testAddAndGetItemCount() {
        TimeSeries series = new TimeSeries("Test");
        Day day1 = new Day(1, 1, 2020);
        Day day2 = new Day(2, 1, 2020);
        series.add(day1, 10.0);
        series.add(day2, 20.0);
        assertEquals(2, series.getItemCount());
        assertEquals(10.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(20.0, series.getValue(1).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testGetItemsReturnsUnmodifiableList() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        List items = series.getItems();
        assertEquals(1, items.size());
        try {
            items.add(new TimeSeriesDataItem(new Day(2, 1, 2020), 20.0));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetDataItemByIndex() {
        TimeSeries series = new TimeSeries("Test");
        Day day = new Day(1, 1, 2020);
        series.add(day, 10.0);
        TimeSeriesDataItem item = series.getDataItem(0);
        assertEquals(day, item.getPeriod());
        assertEquals(10.0, item.getValue().doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testGetDataItemByPeriod() {
        TimeSeries series = new TimeSeries("Test");
        Day day1 = new Day(1, 1, 2020);
        Day day2 = new Day(2, 1, 2020);
        series.add(day1, 10.0);
        series.add(day2, 20.0);
        assertEquals(day1, series.getDataItem(day1).getPeriod());
        assertNull(series.getDataItem(new Day(3, 1, 2020)));
    }

    @Test(timeout = 4000)
    public void testGetTimePeriod() {
        TimeSeries series = new TimeSeries("Test");
        Day day = new Day(1, 1, 2020);
        series.add(day, 10.0);
        assertEquals(day, series.getTimePeriod(0));
    }

    @Test(timeout = 4000)
    public void testGetNextTimePeriod() {
        TimeSeries series = new TimeSeries("Test");
        Day day = new Day(1, 1, 2020);
        series.add(day, 10.0);
        RegularTimePeriod next = series.getNextTimePeriod();
        assertEquals(new Day(2, 1, 2020), next);
    }

    @Test(timeout = 4000)
    public void testGetTimePeriods() {
        TimeSeries series = new TimeSeries("Test");
        Day day1 = new Day(1, 1, 2020);
        Day day2 = new Day(2, 1, 2020);
        series.add(day1, 10.0);
        series.add(day2, 20.0);
        java.util.Collection periods = series.getTimePeriods();
        assertEquals(2, periods.size());
        assertTrue(periods.contains(day1));
        assertTrue(periods.contains(day2));
    }

    @Test(timeout = 4000)
    public void testGetTimePeriodsUniqueToOtherSeries() {
        TimeSeries series1 = new TimeSeries("Test1");
        TimeSeries series2 = new TimeSeries("Test2");
        Day day1 = new Day(1, 1, 2020);
        Day day2 = new Day(2, 1, 2020);
        Day day3 = new Day(3, 1, 2020);
        series1.add(day1, 10.0);
        series1.add(day2, 20.0);
        series2.add(day2, 30.0);
        series2.add(day3, 40.0);
        java.util.Collection unique = series1.getTimePeriodsUniqueToOtherSeries(series2);
        assertEquals(1, unique.size());
        assertTrue(unique.contains(day1));
    }

    @Test(timeout = 4000)
    public void testGetIndex() {
        TimeSeries series = new TimeSeries("Test");
        Day day1 = new Day(1, 1, 2020);
        Day day2 = new Day(2, 1, 2020);
        series.add(day1, 10.0);
        series.add(day2, 20.0);
        assertEquals(0, series.getIndex(day1));
        assertEquals(1, series.getIndex(day2));
        assertEquals(-3, series.getIndex(new Day(3, 1, 2020)));
    }

    @Test(timeout = 4000)
    public void testGetValueByIndex() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        assertEquals(10.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testGetValueByPeriod() {
        TimeSeries series = new TimeSeries("Test");
        Day day = new Day(1, 1, 2020);
        series.add(day, 10.0);
        assertEquals(10.0, series.getValue(day).doubleValue(), 0.0001);
        assertNull(series.getValue(new Day(2, 1, 2020)));
    }

    @Test(timeout = 4000)
    public void testUpdateByPeriod() {
        TimeSeries series = new TimeSeries("Test");
        Day day = new Day(1, 1, 2020);
        series.add(day, 10.0);
        series.update(day, 20.0);
        assertEquals(20.0, series.getValue(day).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testUpdateByIndex() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        series.update(0, 30.0);
        assertEquals(30.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddAndOrUpdate() {
        TimeSeries series1 = new TimeSeries("Test1");
        TimeSeries series2 = new TimeSeries("Test2");
        Day day1 = new Day(1, 1, 2020);
        Day day2 = new Day(2, 1, 2020);
        series1.add(day1, 10.0);
        series2.add(day1, 20.0);
        series2.add(day2, 30.0);
        TimeSeries overwritten = series1.addAndOrUpdate(series2);
        assertEquals(2, series1.getItemCount());
        assertEquals(20.0, series1.getValue(day1).doubleValue(), 0.0001);
        assertEquals(30.0, series1.getValue(day2).doubleValue(), 0.0001);
        assertEquals(1, overwritten.getItemCount());
        assertEquals(10.0, overwritten.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateExisting() {
        TimeSeries series = new TimeSeries("Test");
        Day day = new Day(1, 1, 2020);
        series.add(day, 10.0);
        TimeSeriesDataItem overwritten = series.addOrUpdate(day, 20.0);
        assertNotNull(overwritten);
        assertEquals(10.0, overwritten.getValue().doubleValue(), 0.0001);
        assertEquals(20.0, series.getValue(day).doubleValue(), 0.0001);
        assertEquals(1, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateNew() {
        TimeSeries series = new TimeSeries("Test");
        Day day = new Day(1, 1, 2020);
        TimeSeriesDataItem overwritten = series.addOrUpdate(day, 10.0);
        assertNull(overwritten);
        assertEquals(10.0, series.getValue(day).doubleValue(), 0.0001);
        assertEquals(1, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testClear() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        series.clear();
        assertEquals(0, series.getItemCount());
        series.clear(); // should not throw
    }

    @Test(timeout = 4000)
    public void testDeleteByPeriod() {
        TimeSeries series = new TimeSeries("Test");
        Day day1 = new Day(1, 1, 2020);
        Day day2 = new Day(2, 1, 2020);
        series.add(day1, 10.0);
        series.add(day2, 20.0);
        series.delete(day1);
        assertEquals(1, series.getItemCount());
        assertEquals(day2, series.getTimePeriod(0));
        series.delete(new Day(3, 1, 2020)); // no-op
        assertEquals(1, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testDeleteByRange() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(3, 1, 2020), 30.0);
        series.delete(0, 1);
        assertEquals(1, series.getItemCount());
        assertEquals(new Day(3, 1, 2020), series.getTimePeriod(0));
    }

    @Test(timeout = 4000)
    public void testSetMaximumItemCount() {
        TimeSeries series = new TimeSeries("Test");
        series.setMaximumItemCount(2);
        assertEquals(2, series.getMaximumItemCount());
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(3, 1, 2020), 30.0);
        assertEquals(2, series.getItemCount());
        assertEquals(new Day(2, 1, 2020), series.getTimePeriod(0));
    }

    @Test(timeout = 4000)
    public void testSetMaximumItemAge() {
        TimeSeries series = new TimeSeries("Test");
        series.setMaximumItemAge(2);
        assertEquals(2, series.getMaximumItemAge());
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(4, 1, 2020), 30.0);
        assertEquals(2, series.getItemCount());
        assertEquals(new Day(2, 1, 2020), series.getTimePeriod(0));
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItems() {
        TimeSeries series = new TimeSeries("Test");
        series.setMaximumItemAge(2);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(4, 1, 2020), 30.0);
        series.removeAgedItems(true);
        assertEquals(2, series.getItemCount());
        assertEquals(new Day(2, 1, 2020), series.getTimePeriod(0));
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItemsWithTimestamp() {
        TimeSeries series = new TimeSeries("Test");
        series.setMaximumItemAge(2);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(4, 1, 2020), 30.0);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(2020, 0, 5);
        series.removeAgedItems(cal.getTimeInMillis(), true);
        assertEquals(2, series.getItemCount());
        assertEquals(new Day(2, 1, 2020), series.getTimePeriod(0));
    }

    /* ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ==================== */

    @Test(timeout = 4000)
    public void testEmptySeriesOperations() {
        TimeSeries series = new TimeSeries("Test");
        assertEquals(0, series.getItemCount());
        assertEquals(0, series.getTimePeriods().size());
        try {
            series.getTimePeriod(0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            series.getValue(0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddNullItem() {
        TimeSeries series = new TimeSeries("Test");
        try {
            series.add((TimeSeriesDataItem) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddWrongTimePeriodClass() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        try {
            series.add(new Year(2020), 10.0);
            fail("Expected SeriesException");
        } catch (SeriesException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddDuplicatePeriod() {
        TimeSeries series = new TimeSeries("Test");
        Day day = new Day(1, 1, 2020);
        series.add(day, 10.0);
        try {
            series.add(day, 20.0);
            fail("Expected SeriesException");
        } catch (SeriesException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddInsertInMiddle() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(3, 1, 2020), 30.0);
        series.add(new Day(2, 1, 2020), 20.0);
        assertEquals(3, series.getItemCount());
        assertEquals(new Day(1, 1, 2020), series.getTimePeriod(0));
        assertEquals(new Day(2, 1, 2020), series.getTimePeriod(1));
        assertEquals(new Day(3, 1, 2020), series.getTimePeriod(2));
    }

    @Test(timeout = 4000)
    public void testSetMaximumItemCountNegative() {
        TimeSeries series = new TimeSeries("Test");
        try {
            series.setMaximumItemCount(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetMaximumItemAgeNegative() {
        TimeSeries series = new TimeSeries("Test");
        try {
            series.setMaximumItemAge(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetIndexNullPeriod() {
        TimeSeries series = new TimeSeries("Test");
        try {
            series.getIndex(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateNullPeriod() {
        TimeSeries series = new TimeSeries("Test");
        try {
            series.addOrUpdate(null, 10.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testUpdateNonExistentPeriod() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        try {
            series.update(new Day(2, 1, 2020), 20.0);
            fail("Expected SeriesException");
        } catch (SeriesException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDeleteInvalidRange() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        try {
            series.delete(1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyInvalidStart() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        try {
            series.createCopy(-1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (CloneNotSupportedException e) {
            fail("Unexpected CloneNotSupportedException");
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyInvalidEnd() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        try {
            series.createCopy(1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (CloneNotSupportedException e) {
            fail("Unexpected CloneNotSupportedException");
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodNullStart() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        try {
            series.createCopy(null, new Day(2, 1, 2020));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (CloneNotSupportedException e) {
            fail("Unexpected CloneNotSupportedException");
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodNullEnd() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        try {
            series.createCopy(new Day(1, 1, 2020), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (CloneNotSupportedException e) {
            fail("Unexpected CloneNotSupportedException");
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodInvalidRange() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        try {
            series.createCopy(new Day(2, 1, 2020), new Day(1, 1, 2020));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (CloneNotSupportedException e) {
            fail("Unexpected CloneNotSupportedException");
        }
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodEmptyRange() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        TimeSeries copy = series.createCopy(new Day(2, 1, 2020), new Day(3, 1, 2020));
        assertEquals(0, copy.getItemCount());
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodAfterLast() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        TimeSeries copy = series.createCopy(new Day(2, 1, 2020), new Day(3, 1, 2020));
        assertEquals(0, copy.getItemCount());
    }

    /* ==================== Partition C: Defect-Targeted Branch Zone ==================== */

    @Test(timeout = 4000)
    public void testCloneEmptySeries() {
        TimeSeries series = new TimeSeries("Test");
        try {
            Object clone = series.clone();
            assertNotNull(clone);
            assertTrue(clone instanceof TimeSeries);
            TimeSeries clonedSeries = (TimeSeries) clone;
            assertEquals(0, clonedSeries.getItemCount());
            assertEquals("Test", clonedSeries.getKey());
        } catch (CloneNotSupportedException e) {
            fail("CloneNotSupportedException should not be thrown");
        } catch (IllegalArgumentException e) {
            fail("IllegalArgumentException should not be thrown for empty series clone: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCloneNonEmptySeries() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        try {
            TimeSeries clone = (TimeSeries) series.clone();
            assertEquals(2, clone.getItemCount());
            assertEquals(series.getTimePeriod(0), clone.getTimePeriod(0));
            assertEquals(series.getValue(0), clone.getValue(0));
            assertEquals(series.getTimePeriod(1), clone.getTimePeriod(1));
            assertEquals(series.getValue(1), clone.getValue(1));
        } catch (CloneNotSupportedException e) {
            fail("CloneNotSupportedException should not be thrown");
        }
    }

    /* ==================== Partition D: Exception & Defensive Guard Paths ==================== */

    @Test(timeout = 4000)
    public void testAddWithNotifyFalse() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0, false);
        assertEquals(1, series.getItemCount());
        series.add(new Day(2, 1, 2020), 20.0, false);
        assertEquals(2, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testAddNumberValue() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), new Double(10.0));
        assertEquals(10.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddNumberValueWithNotify() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), new Double(10.0), false);
        assertEquals(1, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateDoubleValue() {
        TimeSeries series = new TimeSeries("Test");
        Day day = new Day(1, 1, 2020);
        series.add(day, 10.0);
        TimeSeriesDataItem overwritten = series.addOrUpdate(day, 20.0);
        assertNotNull(overwritten);
        assertEquals(20.0, series.getValue(day).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItemsNoRemoval() {
        TimeSeries series = new TimeSeries("Test");
        series.setMaximumItemAge(10);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.removeAgedItems(true);
        assertEquals(2, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItemsWithTimestampNoRemoval() {
        TimeSeries series = new TimeSeries("Test");
        series.setMaximumItemAge(10);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(2020, 0, 3);
        series.removeAgedItems(cal.getTimeInMillis(), true);
        assertEquals(2, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testDeletePeriodNotFound() {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        series.delete(new Day(2, 1, 2020));
        assertEquals(1, series.getItemCount());
    }

    /* ==================== Partition E: Object Lifecycle & Contract Integrity ==================== */

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        TimeSeries series = new TimeSeries("Test");
        assertTrue(series.equals(series));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        TimeSeries series = new TimeSeries("Test");
        assertFalse(series.equals("Test"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentDomain() {
        TimeSeries series1 = new TimeSeries("Test", "Domain1", "Range", Day.class);
        TimeSeries series2 = new TimeSeries("Test", "Domain2", "Range", Day.class);
        assertFalse(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentRange() {
        TimeSeries series1 = new TimeSeries("Test", "Domain", "Range1", Day.class);
        TimeSeries series2 = new TimeSeries("Test", "Domain", "Range2", Day.class);
        assertFalse(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentMaxItemCount() {
        TimeSeries series1 = new TimeSeries("Test");
        TimeSeries series2 = new TimeSeries("Test");
        series1.setMaximumItemCount(10);
        series2.setMaximumItemCount(20);
        assertFalse(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentMaxItemAge() {
        TimeSeries series1 = new TimeSeries("Test");
        TimeSeries series2 = new TimeSeries("Test");
        series1.setMaximumItemAge(10);
        series2.setMaximumItemAge(20);
        assertFalse(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentItemCount() {
        TimeSeries series1 = new TimeSeries("Test");
        TimeSeries series2 = new TimeSeries("Test");
        series1.add(new Day(1, 1, 2020), 10.0);
        assertFalse(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentItems() {
        TimeSeries series1 = new TimeSeries("Test");
        TimeSeries series2 = new TimeSeries("Test");
        series1.add(new Day(1, 1, 2020), 10.0);
        series2.add(new Day(1, 1, 2020), 20.0);
        assertFalse(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testEqualsSameSeries() {
        TimeSeries series1 = new TimeSeries("Test");
        TimeSeries series2 = new TimeSeries("Test");
        series1.add(new Day(1, 1, 2020), 10.0);
        series2.add(new Day(1, 1, 2020), 10.0);
        assertTrue(series1.equals(series2));
    }

    @Test(timeout = 4000)
    public void testHashCodeEmptySeries() {
        TimeSeries series1 = new TimeSeries("Test");
        TimeSeries series2 = new TimeSeries("Test");
        assertEquals(series1.hashCode(), series2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeNonEmptySeries() {
        TimeSeries series1 = new TimeSeries("Test");
        TimeSeries series2 = new TimeSeries("Test");
        series1.add(new Day(1, 1, 2020), 10.0);
        series2.add(new Day(1, 1, 2020), 10.0);
        assertEquals(series1.hashCode(), series2.hashCode());
    }

    @Test(timeout = 4000)
    public void testCreateCopyValidRange() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test");
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(3, 1, 2020), 30.0);
        TimeSeries copy = series.createCopy(1, 2);
        assertEquals(2, copy.getItemCount());
        assertEquals(new Day(2, 1, 2020), copy.getTimePeriod(0));
        assertEquals(new Day(3, 1, 2020), copy.getTimePeriod(1));
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriodValidRange() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test");
        Day day1 = new Day(1, 1, 2020);
        Day day2 = new Day(2, 1, 2020);
        Day day3 = new Day(3, 1, 2020);
        series.add(day1, 10.0);
        series.add(day2, 20.0);
        series.add(day3, 30.0);
        TimeSeries copy = series.createCopy(day1, day2);
        assertEquals(2, copy.getItemCount());
        assertEquals(day1, copy.getTimePeriod(0));
        assertEquals(day2, copy.getTimePeriod(1));
    }

    @Test(timeout = 4000)
    public void testGetTimePeriodsUniqueToOtherSeriesEmpty() {
        TimeSeries series1 = new TimeSeries("Test1");
        TimeSeries series2 = new TimeSeries("Test2");
        series1.add(new Day(1, 1, 2020), 10.0);
        java.util.Collection unique = series1.getTimePeriodsUniqueToOtherSeries(series2);
        assertEquals(1, unique.size());
    }
}