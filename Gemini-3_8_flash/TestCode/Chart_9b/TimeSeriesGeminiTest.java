package org.jfree.data.time;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.general.SeriesException;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 * Targets: org.jfree.data.time.TimeSeries
 *
 * 1. Defect-Targeted Zone (Defects4J Bug 1864222):
 *    - createCopy(RegularTimePeriod, RegularTimePeriod) when the requested period range
 *      falls strictly between two existing data points (start > end indices in binarySearch).
 *      Prior to the fix, this causes createCopy(int, int) to be called with startIndex > endIndex,
 *      triggering IllegalArgumentException: Requires start <= end.
 *
 * 2. Partition A: Core Functional Logic & State Transitions:
 *    - Constructors: (Comparable), (Comparable, Class), (Comparable, String, String, Class).
 *    - Domain & Range descriptions and property change events.
 *    - Sequential item addition (add with double, Number, TimeSeriesDataItem, notify flags).
 *    - Ordered insertion vs duplicate prevention (SeriesException).
 *    - Updating values via index and via RegularTimePeriod (found vs not found).
 *    - addOrUpdate() updating existing vs inserting new; checking returned overwritten item.
 *    - addAndOrUpdate() merging two series.
 *    - Deletion by period, index range, and clear().
 *
 * 3. Partition B: Boundary Value Analysis (BVA) & Extremes:
 *    - maximumItemCount enforcement (FIFO trimming when capacity is exceeded).
 *    - maximumItemAge aging mechanism: removeAgedItems(boolean) and removeAgedItems(long, boolean).
 *    - Index queries for empty series, single element, boundary elements, non-existent elements.
 *    - getNextTimePeriod() boundary checks.
 *
 * 4. Partition C: Exception & Defensive Guard Paths:
 *    - Null parameters across constructors and methods (period, item, start, end).
 *    - Incompatible time period classes (SeriesException).
 *    - Negative maximumItemCount and negative maximumItemAge (IllegalArgumentException).
 *    - Inverted index ranges in delete() and createCopy() (IllegalArgumentException).
 *
 * 5. Partition D: Object Lifecycle & Contract Integrity:
 *    - equals() symmetry, reflexivity, type mismatch, and all property field divergence branches.
 *    - hashCode() calculation branches for count = 0, 1, 2, 3+.
 *    - clone() independence (deep-copy verification of internal List).
 *    - Full serialization / deserialization loop.
 */
public class TimeSeriesGeminiTest {

    // Helper listener to record events
    private static class TestSeriesChangeListener implements SeriesChangeListener {
        private int eventCount = 0;
        private SeriesChangeEvent lastEvent = null;

        @Override
        public void seriesChanged(SeriesChangeEvent event) {
            this.eventCount++;
            this.lastEvent = event;
        }

        public int getEventCount() {
            return this.eventCount;
        }

        public void reset() {
            this.eventCount = 0;
            this.lastEvent = null;
        }
    }

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Bug 1864222)
    // =========================================================================

    /**
     * Targets Bug 1864222:
     * When start and end periods are valid and start <= end, but both fall between
     * existing consecutive items in the series, startIndex becomes greater than endIndex.
     * The method must not throw IllegalArgumentException("Requires start <= end"),
     * but instead detect the empty range and return an empty series copy.
     */
    @Test(timeout = 4000)
    public void testBug1864222CreateCopyEmptySubRangeBetweenPoints() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Bug1864222Test", Day.class);
        series.add(new Day(1, 1, 2008), 10.0);
        series.add(new Day(10, 1, 2008), 20.0);

        // Requested range is between Jan 3 and Jan 5, 2008 (no points exist here)
        Day start = new Day(3, 1, 2008);
        Day end = new Day(5, 1, 2008);

        TimeSeries subset = series.createCopy(start, end);
        assertNotNull("Created copy should not be null", subset);
        assertEquals("Subset should be empty", 0, subset.getItemCount());
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndPropertyChange() {
        TimeSeries s1 = new TimeSeries("Series 1");
        assertEquals("Time", s1.getDomainDescription());
        assertEquals("Value", s1.getRangeDescription());
        assertEquals(Day.class, s1.getTimePeriodClass());
        assertEquals(0, s1.getItemCount());
        assertEquals(Integer.MAX_VALUE, s1.getMaximumItemCount());
        assertEquals(Long.MAX_VALUE, s1.getMaximumItemAge());

        TimeSeries s2 = new TimeSeries("Series 2", Year.class);
        assertEquals(Year.class, s2.getTimePeriodClass());

        TimeSeries s3 = new TimeSeries("Series 3", "CustomDomain", "CustomRange", Month.class);
        assertEquals("CustomDomain", s3.getDomainDescription());
        assertEquals("CustomRange", s3.getRangeDescription());
        assertEquals(Month.class, s3.getTimePeriodClass());

        s3.setDomainDescription("NewDomain");
        assertEquals("NewDomain", s3.getDomainDescription());
        s3.setRangeDescription("NewRange");
        assertEquals("NewRange", s3.getRangeDescription());
    }

    @Test(timeout = 4000)
    public void testAddAndNotificationControl() {
        TimeSeries series = new TimeSeries("Series", Day.class);
        TestSeriesChangeListener listener = new TestSeriesChangeListener();
        series.addChangeListener(listener);

        Day day1 = new Day(1, 1, 2020);
        Day day2 = new Day(2, 1, 2020);
        Day day3 = new Day(3, 1, 2020);

        // Add with notify = false
        series.add(day1, 100.0, false);
        assertEquals(1, series.getItemCount());
        assertEquals(0, listener.getEventCount());

        // Add with notify = true
        series.add(day2, 200.0, true);
        assertEquals(2, series.getItemCount());
        assertEquals(1, listener.getEventCount());

        // Add with Number and default notify = true
        series.add(day3, (Number) 300.0);
        assertEquals(3, series.getItemCount());
        assertEquals(2, listener.getEventCount());

        // Verify values
        assertEquals(100.0, series.getValue(0).doubleValue(), 1e-9);
        assertEquals(200.0, series.getValue(day2).doubleValue(), 1e-9);
        assertNull(series.getValue(new Day(4, 1, 2020)));
    }

    @Test(timeout = 4000)
    public void testInOrderAndMiddleInsertion() {
        TimeSeries series = new TimeSeries("InsertSeries", Day.class);
        Day d1 = new Day(1, 1, 2020);
        Day d3 = new Day(3, 1, 2020);
        Day d2 = new Day(2, 1, 2020);

        series.add(d1, 1.0);
        series.add(d3, 3.0);
        // Insert in middle
        series.add(d2, 2.0);

        assertEquals(3, series.getItemCount());
        assertEquals(d1, series.getTimePeriod(0));
        assertEquals(d2, series.getTimePeriod(1));
        assertEquals(d3, series.getTimePeriod(2));
    }

    @Test(timeout = 4000)
    public void testUpdateMethods() {
        TimeSeries series = new TimeSeries("UpdateTest", Day.class);
        TestSeriesChangeListener listener = new TestSeriesChangeListener();
        series.addChangeListener(listener);

        Day day = new Day(1, 5, 2020);
        series.add(day, 10.0);
        assertEquals(1, listener.getEventCount());

        series.update(0, 15.0);
        assertEquals(15.0, series.getValue(0).doubleValue(), 1e-9);
        assertEquals(2, listener.getEventCount());

        series.update(day, 20.0);
        assertEquals(20.0, series.getValue(day).doubleValue(), 1e-9);
        assertEquals(3, listener.getEventCount());
    }

    @Test(timeout = 4000)
    public void testAddOrUpdate() {
        TimeSeries series = new TimeSeries("AddOrUpdateTest", Day.class);
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);

        // Add new item
        TimeSeriesDataItem item1 = series.addOrUpdate(d1, 100.0);
        assertNull("Overwritten item should be null for brand new item", item1);
        assertEquals(1, series.getItemCount());
        assertEquals(100.0, series.getValue(d1).doubleValue(), 1e-9);

        // Update existing item
        TimeSeriesDataItem overwritten = series.addOrUpdate(d1, 150.0);
        assertNotNull("Overwritten item should not be null", overwritten);
        assertEquals(100.0, overwritten.getValue().doubleValue(), 1e-9);
        assertEquals(150.0, series.getValue(d1).doubleValue(), 1e-9);
        assertEquals(1, series.getItemCount());

        // Add another with Number (null value permitted)
        TimeSeriesDataItem item2 = series.addOrUpdate(d2, (Number) null);
        assertNull(item2);
        assertEquals(2, series.getItemCount());
        assertNull(series.getValue(d2));
    }

    @Test(timeout = 4000)
    public void testAddAndOrUpdate() {
        TimeSeries s1 = new TimeSeries("Series1", Day.class);
        s1.add(new Day(1, 1, 2020), 10.0);
        s1.add(new Day(2, 1, 2020), 20.0);

        TimeSeries s2 = new TimeSeries("Series2", Day.class);
        s2.add(new Day(2, 1, 2020), 25.0);
        s2.add(new Day(3, 1, 2020), 30.0);

        TimeSeries overwritten = s1.addAndOrUpdate(s2);
        assertEquals(3, s1.getItemCount());
        assertEquals(1, overwritten.getItemCount());
        assertEquals(20.0, overwritten.getValue(0).doubleValue(), 1e-9);
        assertEquals(25.0, s1.getValue(new Day(2, 1, 2020)).doubleValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testQueriesAndCollections() {
        TimeSeries series = new TimeSeries("QuerySeries", Day.class);
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        series.add(d1, 10.0);
        series.add(d2, 20.0);

        assertEquals(d2.next(), series.getNextTimePeriod());

        Collection periods = series.getTimePeriods();
        assertEquals(2, periods.size());
        assertTrue(periods.contains(d1));
        assertTrue(periods.contains(d2));

        List items = series.getItems();
        assertEquals(2, items.size());
        try {
            items.add(new TimeSeriesDataItem(new Day(3, 1, 2020), 30.0));
            fail("getItems() should return an unmodifiable list");
        } catch (UnsupportedOperationException expected) {
            // Expected
        }

        TimeSeries other = new TimeSeries("Other", Day.class);
        other.add(d2, 20.0);
        Day d3 = new Day(3, 1, 2020);
        other.add(d3, 30.0);

        Collection unique = series.getTimePeriodsUniqueToOtherSeries(other);
        assertEquals(1, unique.size());
        assertTrue(unique.contains(d3));

        assertNotNull(series.getDataItem(d1));
        assertNull(series.getDataItem(d3));
    }

    @Test(timeout = 4000)
    public void testDeleteAndClear() {
        TimeSeries series = new TimeSeries("DeleteTest", Day.class);
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);
        series.add(d1, 10.0);
        series.add(d2, 20.0);
        series.add(d3, 30.0);

        // Delete non-existent period (should do nothing)
        series.delete(new Day(4, 1, 2020));
        assertEquals(3, series.getItemCount());

        // Delete existing period
        series.delete(d2);
        assertEquals(2, series.getItemCount());
        assertEquals(-1, series.getIndex(d2));

        // Delete range
        series.delete(0, 1);
        assertEquals(0, series.getItemCount());

        // Clear empty
        series.clear();
        assertEquals(0, series.getItemCount());

        // Clear non-empty
        series.add(d1, 10.0);
        series.clear();
        assertEquals(0, series.getItemCount());
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & CAPACITY/AGING
    // =========================================================================

    @Test(timeout = 4000)
    public void testMaximumItemCountEnforcement() {
        TimeSeries series = new TimeSeries("MaxItemTest", Day.class);
        series.setMaximumItemCount(2);
        assertEquals(2, series.getMaximumItemCount());

        series.add(new Day(1, 1, 2020), 1.0);
        series.add(new Day(2, 1, 2020), 2.0);
        assertEquals(2, series.getItemCount());

        // Adding 3rd item should evict 1st
        series.add(new Day(3, 1, 2020), 3.0);
        assertEquals(2, series.getItemCount());
        assertEquals(new Day(2, 1, 2020), series.getTimePeriod(0));
        assertEquals(new Day(3, 1, 2020), series.getTimePeriod(1));

        // addOrUpdate triggering maximumItemCount
        series.addOrUpdate(new Day(4, 1, 2020), 4.0);
        assertEquals(2, series.getItemCount());
        assertEquals(new Day(3, 1, 2020), series.getTimePeriod(0));

        // Reducing maximumItemCount on non-empty list
        series.add(new Day(5, 1, 2020), 5.0); // Now [4, 5]
        series.setMaximumItemCount(1);
        assertEquals(1, series.getItemCount());
        assertEquals(new Day(5, 1, 2020), series.getTimePeriod(0));
    }

    @Test(timeout = 4000)
    public void testMaximumItemAgeAndAging() {
        TimeSeries series = new TimeSeries("AgeTest", Day.class);
        series.setMaximumItemAge(2); // Age <= 2 days difference

        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d4 = new Day(4, 1, 2020); // 4 - 1 = 3 > 2 -> d1 must be removed

        series.add(d1, 10.0);
        series.add(d2, 20.0);
        series.add(d4, 40.0);

        assertEquals(2, series.getItemCount());
        assertEquals(d2, series.getTimePeriod(0));
        assertEquals(d4, series.getTimePeriod(1));

        // Test aging via setMaximumItemAge
        series.setMaximumItemAge(1); // 4 - 2 = 2 > 1 -> d2 must be removed
        assertEquals(1, series.getItemCount());
        assertEquals(d4, series.getTimePeriod(0));

        // Test removeAgedItems(long latest, boolean notify)
        Day d10 = new Day(10, 1, 2020);
        series.add(d10, 100.0);
        TestSeriesChangeListener listener = new TestSeriesChangeListener();
        series.addChangeListener(listener);

        // Age against a fixed timestamp: Jan 15, 2020
        Day d15 = new Day(15, 1, 2020);
        series.removeAgedItems(d15.getFirstMillisecond(), true);
        assertEquals(0, series.getItemCount());
        assertTrue(listener.getEventCount() > 0);
    }

    @Test(timeout = 4000)
    public void testCreateCopyRanges() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("CopyTest", Day.class);
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);
        Day d4 = new Day(4, 1, 2020);
        series.add(d1, 10.0);
        series.add(d2, 20.0);
        series.add(d3, 30.0);
        series.add(d4, 40.0);

        // Copy by index
        TimeSeries copyIndices = series.createCopy(1, 2);
        assertEquals(2, copyIndices.getItemCount());
        assertEquals(d2, copyIndices.getTimePeriod(0));
        assertEquals(d3, copyIndices.getTimePeriod(1));

        // Copy by periods: fully encompassing
        TimeSeries copyPeriods = series.createCopy(d2, d3);
        assertEquals(2, copyPeriods.getItemCount());
        assertEquals(d2, copyPeriods.getTimePeriod(0));

        // Copy by periods: outside right boundary
        TimeSeries copyRight = series.createCopy(new Day(5, 1, 2020), new Day(10, 1, 2020));
        assertEquals(0, copyRight.getItemCount());

        // Copy by periods: outside left boundary
        TimeSeries copyLeft = series.createCopy(new Day(10, 1, 2019), new Day(20, 1, 2019));
        assertEquals(0, copyLeft.getItemCount());
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullNameInConstructor() {
        new TimeSeries(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNegativeMaximumItemCount() {
        TimeSeries s = new TimeSeries("S");
        s.setMaximumItemCount(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNegativeMaximumItemAge() {
        TimeSeries s = new TimeSeries("S");
        s.setMaximumItemAge(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetIndexNullPeriod() {
        TimeSeries s = new TimeSeries("S");
        s.getIndex(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddNullItem() {
        TimeSeries s = new TimeSeries("S");
        s.add((TimeSeriesDataItem) null);
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testAddIncompatiblePeriodClass() {
        TimeSeries s = new TimeSeries("YearSeries", Year.class);
        s.add(new TimeSeriesDataItem(new Day(1, 1, 2020), 10.0));
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testAddDuplicatePeriod() {
        TimeSeries s = new TimeSeries("S", Day.class);
        Day d = new Day(1, 1, 2020);
        s.add(d, 10.0);
        s.add(d, 20.0); // Duplicate!
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testUpdateNonExistentPeriod() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.update(new Day(1, 1, 2020), 10.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDeleteInvertedRange() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.delete(2, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyInvalidStartIndex() throws CloneNotSupportedException {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.createCopy(-1, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyInvertedIndices() throws CloneNotSupportedException {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.createCopy(3, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyNullStartPeriod() throws CloneNotSupportedException {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.createCopy((RegularTimePeriod) null, new Day(1, 1, 2020));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyNullEndPeriod() throws CloneNotSupportedException {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.createCopy(new Day(1, 1, 2020), (RegularTimePeriod) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyInvertedPeriods() throws CloneNotSupportedException {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.createCopy(new Day(5, 1, 2020), new Day(1, 1, 2020));
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        TimeSeries s1 = new TimeSeries("Series", "D", "R", Day.class);
        TimeSeries s2 = new TimeSeries("Series", "D", "R", Day.class);

        // Reflexive
        assertTrue(s1.equals(s1));
        // Symmetric
        assertTrue(s1.equals(s2) && s2.equals(s1));
        assertEquals(s1.hashCode(), s2.hashCode());

        // Type mismatch & null
        assertFalse(s1.equals(null));
        assertFalse(s1.equals("Not a TimeSeries"));

        // Domain mismatch
        s2.setDomainDescription("OtherDomain");
        assertFalse(s1.equals(s2));
        s2.setDomainDescription("D");

        // Range mismatch
        s2.setRangeDescription("OtherRange");
        assertFalse(s1.equals(s2));
        s2.setRangeDescription("R");

        // MaximumItemAge mismatch
        s2.setMaximumItemAge(50);
        assertFalse(s1.equals(s2));
        s2.setMaximumItemAge(Long.MAX_VALUE);

        // MaximumItemCount mismatch
        s2.setMaximumItemCount(10);
        assertFalse(s1.equals(s2));
        s2.setMaximumItemCount(Integer.MAX_VALUE);

        // Item count / content mismatch
        s1.add(new Day(1, 1, 2020), 10.0);
        assertFalse(s1.equals(s2));
        s2.add(new Day(1, 1, 2020), 10.0);
        assertTrue(s1.equals(s2));

        // Data item value mismatch
        s2.update(0, 99.0);
        assertFalse(s1.equals(s2));

        // Hashcode coverage across counts 0, 1, 2, 3+
        TimeSeries h = new TimeSeries("HashTest", Day.class);
        int hc0 = h.hashCode();
        h.add(new Day(1, 1, 2020), 1.0);
        int hc1 = h.hashCode();
        h.add(new Day(2, 1, 2020), 2.0);
        int hc2 = h.hashCode();
        h.add(new Day(3, 1, 2020), 3.0);
        int hc3 = h.hashCode();

        assertNotEquals(hc0, hc1);
        assertNotEquals(hc1, hc2);
        assertNotEquals(hc2, hc3);
    }

    @Test(timeout = 4000)
    public void testCloneIndependence() throws CloneNotSupportedException {
        TimeSeries original = new TimeSeries("Original", Day.class);
        original.add(new Day(1, 1, 2020), 100.0);
        original.add(new Day(2, 1, 2020), 200.0);

        TimeSeries cloned = (TimeSeries) original.clone();
        assertEquals(original, cloned);

        // Mutating cloned series must not affect original
        cloned.add(new Day(3, 1, 2020), 300.0);
        assertEquals(2, original.getItemCount());
        assertEquals(3, cloned.getItemCount());

        cloned.update(0, 999.0);
        assertEquals(100.0, original.getValue(0).doubleValue(), 1e-9);
        assertEquals(999.0, cloned.getValue(0).doubleValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSerializationLoop() throws Exception {
        TimeSeries original = new TimeSeries("SerializedSeries", "Days", "Values", Day.class);
        original.add(new Day(1, 1, 2020), 10.0);
        original.add(new Day(2, 1, 2020), 20.0);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(original);

        ByteArrayInputStream inBuffer = new ByteArrayInputStream(buffer.toByteArray());
        ObjectInputStream in = new ObjectInputStream(inBuffer);
        TimeSeries deserialized = (TimeSeries) in.readObject();

        assertEquals(original, deserialized);
        assertEquals(original.getDomainDescription(), deserialized.getDomainDescription());
        assertEquals(original.getRangeDescription(), deserialized.getRangeDescription());
        assertEquals(original.getItemCount(), deserialized.getItemCount());
    }
}