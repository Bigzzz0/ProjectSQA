package org.jfree.data.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

/**
 * Advanced white-box test suite for TimeSeries.
 * Targets line/branch coverage and the known Defects4J bug #1864222.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (add, addOrUpdate, update, delete, getters)
 * - Partition B: Boundary values (empty series, null arguments, max/min counts/ages)
 * - Partition C: Defect-targeted branch zone (createCopy with equal missing periods)
 * - Partition D: Exception paths (null args, invalid indices, type mismatch)
 * - Partition E: Object contract (equals, hashCode, clone)
 *
 * Known defect: createCopy(RegularTimePeriod, RegularTimePeriod) throws
 * IllegalArgumentException when start and end are equal and not in the series.
 * Expected: return empty series.
 */
public class TimeSeriesDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testAddAndGetItemCount() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        assertEquals(0, series.getItemCount());
        series.add(new Day(1, 1, 2020), 10.0);
        assertEquals(1, series.getItemCount());
        series.add(new Day(2, 1, 2020), 20.0);
        assertEquals(2, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testAddInOrder() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(3, 1, 2020), 30.0);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        assertEquals(3, series.getItemCount());
        assertEquals(10.0, series.getValue(0));
        assertEquals(20.0, series.getValue(1));
        assertEquals(30.0, series.getValue(2));
    }

    @Test(timeout = 4000)
    public void testAddDuplicateThrows() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        try {
            series.add(new Day(1, 1, 2020), 20.0);
            fail("Expected SeriesException for duplicate period");
        } catch (SeriesException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateNewPeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        TimeSeriesDataItem overwritten = series.addOrUpdate(new Day(1, 1, 2020), 10.0);
        assertNull(overwritten);
        assertEquals(1, series.getItemCount());
        assertEquals(10.0, series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateExistingPeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        TimeSeriesDataItem overwritten = series.addOrUpdate(new Day(1, 1, 2020), 20.0);
        assertNotNull(overwritten);
        assertEquals(10.0, overwritten.getValue().doubleValue(), 0.0);
        assertEquals(20.0, series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testUpdateByPeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.update(new Day(1, 1, 2020), 99.0);
        assertEquals(99.0, series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testUpdateByIndex() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.update(0, 88.0);
        assertEquals(88.0, series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testDeleteByPeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.delete(new Day(1, 1, 2020));
        assertEquals(1, series.getItemCount());
        assertEquals(20.0, series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testDeleteByRange() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(3, 1, 2020), 30.0);
        series.delete(0, 1);
        assertEquals(1, series.getItemCount());
        assertEquals(30.0, series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testGetDataItemByIndex() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        TimeSeriesDataItem item = series.getDataItem(0);
        assertNotNull(item);
        assertEquals(new Day(1, 1, 2020), item.getPeriod());
        assertEquals(10.0, item.getValue());
    }

    @Test(timeout = 4000)
    public void testGetDataItemByPeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        TimeSeriesDataItem item = series.getDataItem(new Day(1, 1, 2020));
        assertNotNull(item);
        assertNull(series.getDataItem(new Day(2, 1, 2020)));
    }

    @Test(timeout = 4000)
    public void testGetTimePeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        assertEquals(new Day(1, 1, 2020), series.getTimePeriod(0));
    }

    @Test(timeout = 4000)
    public void testGetNextTimePeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        RegularTimePeriod next = series.getNextTimePeriod();
        assertEquals(new Day(2, 1, 2020), next);
    }

    @Test(timeout = 4000)
    public void testGetTimePeriods() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        assertEquals(2, series.getTimePeriods().size());
    }

    @Test(timeout = 4000)
    public void testGetTimePeriodsUniqueToOtherSeries() {
        TimeSeries s1 = new TimeSeries("S1", Day.class);
        s1.add(new Day(1, 1, 2020), 10.0);
        s1.add(new Day(2, 1, 2020), 20.0);
        TimeSeries s2 = new TimeSeries("S2", Day.class);
        s2.add(new Day(2, 1, 2020), 30.0);
        s2.add(new Day(3, 1, 2020), 40.0);
        Collection unique = s1.getTimePeriodsUniqueToOtherSeries(s2);
        assertEquals(1, unique.size());
        assertTrue(unique.contains(new Day(1, 1, 2020)));
    }

    @Test(timeout = 4000)
    public void testGetIndex() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(3, 1, 2020), 30.0);
        assertEquals(0, series.getIndex(new Day(1, 1, 2020)));
        assertEquals(1, series.getIndex(new Day(3, 1, 2020)));
        assertTrue(series.getIndex(new Day(2, 1, 2020)) < 0);
    }

    @Test(timeout = 4000)
    public void testGetValueByIndex() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        assertEquals(10.0, series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testGetValueByPeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        assertEquals(10.0, series.getValue(new Day(1, 1, 2020)));
        assertNull(series.getValue(new Day(2, 1, 2020)));
    }

    @Test(timeout = 4000)
    public void testClear() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.clear();
        assertEquals(0, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testAddAndOrUpdate() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        TimeSeries other = new TimeSeries("Other", Day.class);
        other.add(new Day(1, 1, 2020), 99.0);
        other.add(new Day(2, 1, 2020), 20.0);
        TimeSeries overwritten = series.addAndOrUpdate(other);
        assertEquals(2, series.getItemCount());
        assertEquals(99.0, series.getValue(0));
        assertEquals(20.0, series.getValue(1));
        assertEquals(1, overwritten.getItemCount());
        assertEquals(10.0, overwritten.getValue(0));
    }

    // ==================== Partition B: Boundary Values ====================

    @Test(timeout = 4000)
    public void testMaximumItemCount() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.setMaximumItemCount(2);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(3, 1, 2020), 30.0);
        assertEquals(2, series.getItemCount());
        assertEquals(20.0, series.getValue(0));
        assertEquals(30.0, series.getValue(1));
    }

    @Test(timeout = 4000)
    public void testMaximumItemAge() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.setMaximumItemAge(1); // 1 time period
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(3, 1, 2020), 30.0); // age > 1, should remove first
        assertEquals(1, series.getItemCount());
        assertEquals(30.0, series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItemsWithNotify() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(10, 1, 2020), 100.0);
        series.setMaximumItemAge(5);
        series.removeAgedItems(true);
        assertEquals(1, series.getItemCount());
        assertEquals(100.0, series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItemsWithLatest() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.setMaximumItemAge(1);
        // latest = 2020-01-03 in millis
        long latest = new Day(3, 1, 2020).getFirstMillisecond();
        series.removeAgedItems(latest, false);
        assertEquals(1, series.getItemCount());
        assertEquals(20.0, series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testEmptySeriesCreateCopy() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test", Day.class);
        TimeSeries copy = series.createCopy(0, 0);
        assertEquals(0, copy.getItemCount());
    }

    @Test(timeout = 4000)
    public void testCreateCopySubset() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(3, 1, 2020), 30.0);
        TimeSeries copy = series.createCopy(1, 2);
        assertEquals(2, copy.getItemCount());
        assertEquals(20.0, copy.getValue(0));
        assertEquals(30.0, copy.getValue(1));
    }

    @Test(timeout = 4000)
    public void testCreateCopyByPeriod() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(3, 1, 2020), 30.0);
        TimeSeries copy = series.createCopy(new Day(1, 1, 2020), new Day(2, 1, 2020));
        assertEquals(2, copy.getItemCount());
        assertEquals(10.0, copy.getValue(0));
        assertEquals(20.0, copy.getValue(1));
    }

    @Test(timeout = 4000)
    public void testCreateCopyWithPeriodsNotInSeries() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(4, 1, 2020), 40.0);
        // start and end both not in series, start < end, should return empty
        TimeSeries copy = series.createCopy(new Day(1, 1, 2020), new Day(3, 1, 2020));
        assertEquals(0, copy.getItemCount());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Targets Defects4J bug #1864222:
     * createCopy(RegularTimePeriod, RegularTimePeriod) throws IllegalArgumentException
     * when start and end are equal and not in the series.
     * Expected: return empty series.
     */
    @Test(timeout = 4000)
    public void testBug1864222() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        // start and end are the same period, not in series
        TimeSeries copy = series.createCopy(new Day(2, 1, 2020), new Day(2, 1, 2020));
        assertEquals(0, copy.getItemCount());
    }

    // Additional edge: start and end equal and in series
    @Test(timeout = 4000)
    public void testCreateCopyEqualPeriodInSeries() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        TimeSeries copy = series.createCopy(new Day(1, 1, 2020), new Day(1, 1, 2020));
        assertEquals(1, copy.getItemCount());
        assertEquals(10.0, copy.getValue(0));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullName() {
        new TimeSeries(null, Day.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullTimePeriodClass() {
        new TimeSeries("Test", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddNullItem() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add((TimeSeriesDataItem) null);
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testAddWrongPeriodType() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Hour(1, new Day(1, 1, 2020)), 10.0);
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testAddDuplicatePeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(1, 1, 2020), 20.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetIndexNullPeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.getIndex(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetMaximumItemCountNegative() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.setMaximumItemCount(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDeleteInvalidRange() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.delete(1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyInvalidIndices() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.createCopy(1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyNegativeStart() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.createCopy(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyByPeriodStartAfterEnd() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.createCopy(new Day(2, 1, 2020), new Day(1, 1, 2020));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddOrUpdateNullPeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.addOrUpdate(null, 10.0);
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testUpdateNonExistentPeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.update(new Day(2, 1, 2020), 20.0);
    }

    // ==================== Partition E: Object Contract ====================

    @Test(timeout = 4000)
    public void testEquals() {
        TimeSeries s1 = new TimeSeries("S", Day.class);
        TimeSeries s2 = new TimeSeries("S", Day.class);
        assertTrue(s1.equals(s2));
        s1.add(new Day(1, 1, 2020), 10.0);
        assertFalse(s1.equals(s2));
        s2.add(new Day(1, 1, 2020), 10.0);
        assertTrue(s1.equals(s2));
        s1.setDomainDescription("Domain");
        assertFalse(s1.equals(s2));
        s2.setDomainDescription("Domain");
        assertTrue(s1.equals(s2));
        s1.setRangeDescription("Range");
        assertFalse(s1.equals(s2));
        s2.setRangeDescription("Range");
        assertTrue(s1.equals(s2));
        s1.setMaximumItemCount(100);
        assertFalse(s1.equals(s2));
        s2.setMaximumItemCount(100);
        assertTrue(s1.equals(s2));
        s1.setMaximumItemAge(50);
        assertFalse(s1.equals(s2));
        s2.setMaximumItemAge(50);
        assertTrue(s1.equals(s2));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        TimeSeries s1 = new TimeSeries("S", Day.class);
        TimeSeries s2 = new TimeSeries("S", Day.class);
        assertEquals(s1.hashCode(), s2.hashCode());
        s1.add(new Day(1, 1, 2020), 10.0);
        assertNotEquals(s1.hashCode(), s2.hashCode());
    }

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        TimeSeries s1 = new TimeSeries("S", Day.class);
        s1.add(new Day(1, 1, 2020), 10.0);
        TimeSeries s2 = (TimeSeries) s1.clone();
        assertNotSame(s1, s2);
        assertEquals(s1, s2);
        // modify clone should not affect original
        s2.add(new Day(2, 1, 2020), 20.0);
        assertEquals(1, s1.getItemCount());
        assertEquals(2, s2.getItemCount());
    }

    @Test(timeout = 4000)
    public void testSerialization() {
        // Not fully testing serialization, but ensure clone works
        // (TimeSeries implements Serializable)
        TimeSeries s1 = new TimeSeries("S", Day.class);
        s1.add(new Day(1, 1, 2020), 10.0);
        TimeSeries s2 = null;
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(s1);
            oos.flush();
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
            s2 = (TimeSeries) ois.readObject();
        } catch (Exception e) {
            fail("Serialization failed: " + e.getMessage());
        }
        assertEquals(s1, s2);
    }

    // Additional coverage for getters/setters
    @Test(timeout = 4000)
    public void testDomainRangeDescriptions() {
        TimeSeries series = new TimeSeries("Test", "Domain", "Range", Day.class);
        assertEquals("Domain", series.getDomainDescription());
        assertEquals("Range", series.getRangeDescription());
        series.setDomainDescription("NewDomain");
        assertEquals("NewDomain", series.getDomainDescription());
        series.setRangeDescription("NewRange");
        assertEquals("NewRange", series.getRangeDescription());
    }

    @Test(timeout = 4000)
    public void testGetTimePeriodClass() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        assertEquals(Day.class, series.getTimePeriodClass());
    }

    @Test(timeout = 4000)
    public void testGetItemsUnmodifiable() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        List items = series.getItems();
        try {
            items.add(new TimeSeriesDataItem(new Day(2, 1, 2020), 20.0));
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddWithNotifyFalse() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0, false);
        assertEquals(1, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateWithDouble() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        TimeSeriesDataItem overwritten = series.addOrUpdate(new Day(1, 1, 2020), 5.0);
        assertNull(overwritten);
        assertEquals(5.0, series.getValue(0));
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItemsNoRemoval() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.setMaximumItemAge(100);
        series.removeAgedItems(true);
        assertEquals(1, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testDeletePeriodNotInSeries() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.delete(new Day(2, 1, 2020)); // should do nothing
        assertEquals(1, series.getItemCount());
    }
}