package org.jfree.data.time;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.general.SeriesException;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * 1. Defect-Targeted Zone (Bug 1832432):
 *    - TimeSeries.clone() delegates to createCopy(0, getItemCount() - 1).
 *    - When series is empty, getItemCount() == 0, delegating to createCopy(0, -1).
 *    - createCopy(int, int) enforces `end < start` check throwing IllegalArgumentException.
 *    - Expected: Cloning an empty series should return a valid cloned empty TimeSeries.
 *
 * 2. Structural & Branch Coverage Targets:
 *    - Constructors: (Comparable), (Comparable, Class), (Comparable, String, String, Class)
 *    - Domain/Range descriptions: getters, setters, listener notifications
 *    - MaximumItemCount: valid positive, zero, negative (< 0 throws IllegalArgumentException),
 *      truncation of existing elements (count > maximum)
 *    - MaximumItemAge: valid age, negative (< 0 throws IllegalArgumentException),
 *      automatic ageing in add / addOrUpdate / setMaximumItemAge
 *    - Data retrieval: getDataItem(int), getDataItem(RegularTimePeriod), getValue(int),
 *      getValue(RegularTimePeriod), getTimePeriod(int), getNextTimePeriod(), getTimePeriods(),
 *      getTimePeriodsUniqueToOtherSeries()
 *    - Binary Search & Add:
 *      - Add into empty series
 *      - Add at end (period > last)
 *      - Add in middle (sorted insertion)
 *      - Duplicate period detection (throws SeriesException)
 *      - Incompatible period class (throws SeriesException)
 *      - Null item (throws IllegalArgumentException)
 *      - notify flag: true vs false
 *    - Update & addOrUpdate:
 *      - update(period, value) for existing vs non-existing (throws SeriesException)
 *      - update(int, value)
 *      - addOrUpdate(period, value) overwriting existing item vs adding new item
 *      - addOrUpdate with null period (throws IllegalArgumentException)
 *      - addAndOrUpdate(TimeSeries) merge functionality
 *    - Aging & Deletion:
 *      - removeAgedItems(boolean) with count <= 1, count > 1 with & without aged items
 *      - removeAgedItems(long, boolean) reflection-based ageing with timestamp
 *      - clear() on empty and non-empty
 *      - delete(RegularTimePeriod) existing and non-existing
 *      - delete(int, int) valid range and invalid range (end < start throws)
 *    - Copy & Clone:
 *      - createCopy(int, int) with invalid start (< 0) or invalid end (end < start)
 *      - createCopy(RegularTimePeriod, RegularTimePeriod) with nulls, start > end,
 *        start after end of data, end before start of data, and standard ranges
 *    - Contract Integrity:
 *      - equals(): reflexive, symmetric, comparison with null/other types, different
 *        domain, range, class, maxItemAge, maxItemCount, items count, item values
 *      - hashCode(): 0 items, 1 item, 2 items, 3+ items (testing branch counts 0, 1, 2, >2)
 * =========================================================================
 */
public class TimeSeriesGeminiTest {

    // Helper listener for testing event dispatch
    private static class EventCounter implements SeriesChangeListener {
        int count = 0;
        @Override
        public void seriesChanged(SeriesChangeEvent event) {
            this.count++;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndPropertyDefaults() {
        TimeSeries s1 = new TimeSeries("DefaultSeries");
        assertEquals("DefaultSeries", s1.getKey());
        assertEquals("Time", s1.getDomainDescription());
        assertEquals("Value", s1.getRangeDescription());
        assertEquals(Day.class, s1.getTimePeriodClass());
        assertEquals(Integer.MAX_VALUE, s1.getMaximumItemCount());
        assertEquals(Long.MAX_VALUE, s1.getMaximumItemAge());
        assertEquals(0, s1.getItemCount());

        TimeSeries s2 = new TimeSeries("YearSeries", Year.class);
        assertEquals(Year.class, s2.getTimePeriodClass());

        TimeSeries s3 = new TimeSeries("CustomSeries", "DomainD", "RangeD", Month.class);
        assertEquals("DomainD", s3.getDomainDescription());
        assertEquals("RangeD", s3.getRangeDescription());
        assertEquals(Month.class, s3.getTimePeriodClass());
    }

    @Test(timeout = 4000)
    public void testSetDomainAndRangeDescription() {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.setDomainDescription("NewDomain");
        assertEquals("NewDomain", series.getDomainDescription());
        series.setDomainDescription(null);
        assertNull(series.getDomainDescription());

        series.setRangeDescription("NewRange");
        assertEquals("NewRange", series.getRangeDescription());
        series.setRangeDescription(null);
        assertNull(series.getRangeDescription());
    }

    @Test(timeout = 4000)
    public void testAddAndOrderMaintenance() {
        TimeSeries series = new TimeSeries("OrderTest", Year.class);
        Year y2002 = new Year(2002);
        Year y2000 = new Year(2000);
        Year y2001 = new Year(2001);

        series.add(y2002, 20.0); // Count == 0
        series.add(y2000, 10.0); // Insert before (binary search index < 0)
        series.add(y2001, 15.0); // Insert middle

        assertEquals(3, series.getItemCount());
        assertEquals(y2000, series.getTimePeriod(0));
        assertEquals(y2001, series.getTimePeriod(1));
        assertEquals(y2002, series.getTimePeriod(2));

        // Test appending to end
        Year y2003 = new Year(2003);
        series.add(y2003, 30.0);
        assertEquals(4, series.getItemCount());
        assertEquals(y2003, series.getTimePeriod(3));
    }

    @Test(timeout = 4000)
    public void testAddVariantsAndNotifications() {
        TimeSeries series = new TimeSeries("NotifyTest", Year.class);
        EventCounter listener = new EventCounter();
        series.addChangeListener(listener);

        // add(RegularTimePeriod, double, boolean notify)
        series.add(new Year(2001), 10.0, false);
        assertEquals(0, listener.count);

        series.add(new Year(2002), 20.0, true);
        assertEquals(1, listener.count);

        // add(RegularTimePeriod, Number)
        series.add(new Year(2003), null);
        assertEquals(2, listener.count);
        assertNull(series.getValue(new Year(2003)));

        // add(RegularTimePeriod, Number, boolean)
        series.add(new Year(2004), new Double(40.0), false);
        assertEquals(2, listener.count);

        // add(TimeSeriesDataItem, boolean)
        series.add(new TimeSeriesDataItem(new Year(2005), 50.0), true);
        assertEquals(3, listener.count);
    }

    @Test(timeout = 4000)
    public void testUpdateMethods() {
        TimeSeries series = new TimeSeries("UpdateTest", Year.class);
        Year y1 = new Year(2001);
        series.add(y1, 100.0);

        EventCounter listener = new EventCounter();
        series.addChangeListener(listener);

        series.update(y1, 150.0);
        assertEquals(1, listener.count);
        assertEquals(150.0, series.getValue(y1).doubleValue(), 0.0001);

        series.update(0, 200.0);
        assertEquals(2, listener.count);
        assertEquals(200.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testAddOrUpdate() {
        TimeSeries series = new TimeSeries("AddOrUpdateTest", Year.class);
        Year y1 = new Year(2001);
        Year y2 = new Year(2002);

        // Fresh add
        TimeSeriesDataItem old1 = series.addOrUpdate(y1, 10.0);
        assertNull(old1);
        assertEquals(1, series.getItemCount());
        assertEquals(10.0, series.getValue(y1).doubleValue(), 0.0001);

        // Update existing via double overload
        TimeSeriesDataItem old2 = series.addOrUpdate(y1, 20.0);
        assertNotNull(old2);
        assertEquals(10.0, old2.getValue().doubleValue(), 0.0001);
        assertEquals(20.0, series.getValue(y1).doubleValue(), 0.0001);

        // Add second item via Number overload
        TimeSeriesDataItem old3 = series.addOrUpdate(y2, new Double(30.0));
        assertNull(old3);
        assertEquals(2, series.getItemCount());
    }

    @Test(timeout = 4000)
    public void testAddAndOrUpdate() {
        TimeSeries series1 = new TimeSeries("Series1", Year.class);
        series1.add(new Year(2001), 10.0);
        series1.add(new Year(2002), 20.0);

        TimeSeries series2 = new TimeSeries("Series2", Year.class);
        series2.add(new Year(2002), 25.0); // Overwrite
        series2.add(new Year(2003), 30.0); // New

        TimeSeries overwritten = series1.addAndOrUpdate(series2);
        assertEquals(1, overwritten.getItemCount());
        assertEquals(new Year(2002), overwritten.getTimePeriod(0));
        assertEquals(20.0, overwritten.getValue(0).doubleValue(), 0.0001);

        assertEquals(3, series1.getItemCount());
        assertEquals(25.0, series1.getValue(new Year(2002)).doubleValue(), 0.0001);
        assertEquals(30.0, series1.getValue(new Year(2003)).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testDataRetrievalAndCollectionQueries() {
        TimeSeries series = new TimeSeries("QueryTest", Year.class);
        Year y1 = new Year(2001);
        Year y2 = new Year(2002);
        series.add(y1, 10.0);
        series.add(y2, 20.0);

        assertEquals(y1, series.getDataItem(0).getPeriod());
        assertEquals(y2, series.getDataItem(y2).getPeriod());
        assertNull(series.getDataItem(new Year(2005)));

        assertEquals(new Year(2003), series.getNextTimePeriod());

        Collection periods = series.getTimePeriods();
        assertEquals(2, periods.size());
        assertTrue(periods.contains(y1));
        assertTrue(periods.contains(y2));

        TimeSeries otherSeries = new TimeSeries("Other", Year.class);
        otherSeries.add(y2, 50.0);
        otherSeries.add(new Year(2004), 60.0);

        Collection unique = series.getTimePeriodsUniqueToOtherSeries(otherSeries);
        assertEquals(1, unique.size());
        assertTrue(unique.contains(new Year(2004)));
        assertFalse(unique.contains(y2));
    }

    @Test(timeout = 4000)
    public void testDeletionMethods() {
        TimeSeries series = new TimeSeries("DeleteTest", Year.class);
        series.add(new Year(2001), 1.0);
        series.add(new Year(2002), 2.0);
        series.add(new Year(2003), 3.0);
        series.add(new Year(2004), 4.0);

        EventCounter listener = new EventCounter();
        series.addChangeListener(listener);

        // Delete non-existing period does nothing
        series.delete(new Year(1999));
        assertEquals(0, listener.count);
        assertEquals(4, series.getItemCount());

        // Delete existing period
        series.delete(new Year(2002));
        assertEquals(1, listener.count);
        assertEquals(3, series.getItemCount());
        assertEquals(-1, series.getIndex(new Year(2002)));

        // Delete index range
        series.delete(0, 1); // Deletes 2001 and 2003
        assertEquals(2, listener.count);
        assertEquals(1, series.getItemCount());
        assertEquals(new Year(2004), series.getTimePeriod(0));

        // Clear
        series.clear();
        assertEquals(3, listener.count);
        assertEquals(0, series.getItemCount());

        // Clearing an empty series should not fire change event
        series.clear();
        assertEquals(3, listener.count);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Ageing / Capacity Limits
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetMaximumItemCountTruncation() {
        TimeSeries series = new TimeSeries("MaxCountTest", Year.class);
        for (int i = 2000; i < 2010; i++) {
            series.add(new Year(i), (double) i);
        }
        assertEquals(10, series.getItemCount());

        series.setMaximumItemCount(5);
        assertEquals(5, series.getItemCount());
        assertEquals(new Year(2005), series.getTimePeriod(0));
        assertEquals(new Year(2009), series.getTimePeriod(4));

        // Adding an item when at capacity drops oldest
        series.add(new Year(2010), 2010.0);
        assertEquals(5, series.getItemCount());
        assertEquals(new Year(2006), series.getTimePeriod(0));
        assertEquals(new Year(2010), series.getTimePeriod(4));

        // addOrUpdate dropping oldest when at capacity
        series.addOrUpdate(new Year(2011), 2011.0);
        assertEquals(5, series.getItemCount());
        assertEquals(new Year(2007), series.getTimePeriod(0));
    }

    @Test(timeout = 4000)
    public void testMaximumItemAgeMechanism() {
        TimeSeries series = new TimeSeries("AgeTest", Year.class);
        series.setMaximumItemAge(2); // Maximum age 2 years

        series.add(new Year(2000), 10.0);
        series.add(new Year(2001), 20.0);
        series.add(new Year(2002), 30.0);
        assertEquals(3, series.getItemCount());

        // Adding 2003 makes 2003 - 2000 = 3 > 2 -> removes 2000
        series.add(new Year(2003), 40.0);
        assertEquals(3, series.getItemCount());
        assertEquals(new Year(2001), series.getTimePeriod(0));

        // Change maxItemAge to 1 -> triggers removeAgedItems(true)
        EventCounter listener = new EventCounter();
        series.addChangeListener(listener);
        series.setMaximumItemAge(1);
        assertEquals(1, listener.count);
        assertEquals(2, series.getItemCount());
        assertEquals(new Year(2002), series.getTimePeriod(0));
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItemsByTimestamp() {
        TimeSeries series = new TimeSeries("AgeTimeTest", Day.class);
        Day d1 = new Day(1, 1, 2000);
        Day d2 = new Day(2, 1, 2000);
        Day d3 = new Day(10, 1, 2000);

        series.add(d1, 1.0);
        series.add(d2, 2.0);
        series.add(d3, 3.0);
        series.setMaximumItemAge(3); // 3 days

        EventCounter listener = new EventCounter();
        series.addChangeListener(listener);

        // Remove aged items relative to d3 timestamp
        series.removeAgedItems(d3.getFirstMillisecond(), true);
        assertTrue(listener.count > 0);
        assertEquals(1, series.getItemCount());
        assertEquals(d3, series.getTimePeriod(0));
    }

    @Test(timeout = 4000)
    public void testGetItemsUnmodifiable() {
        TimeSeries series = new TimeSeries("UnmodifiableTest", Year.class);
        series.add(new Year(2000), 10.0);
        List items = series.getItems();
        try {
            items.add(new TimeSeriesDataItem(new Year(2001), 20.0));
            fail("Expected UnsupportedOperationException when modifying getItems()");
        } catch (UnsupportedOperationException expected) {
            // Expected
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Bug 1832432)
    // =========================================================================

    /**
     * Targets Defects4J bug 1832432:
     * TimeSeries.clone() on an empty series calls createCopy(0, -1),
     * which fails the validation `if (end < start)` and throws
     * java.lang.IllegalArgumentException: Requires start <= end.
     */
    @Test(timeout = 4000)
    public void testBug1832432_cloneEmptySeries() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("EmptySeries", Year.class);
        assertEquals(0, series.getItemCount());

        Object clonedObj = series.clone();
        assertNotNull(clonedObj);
        assertTrue(clonedObj instanceof TimeSeries);
        TimeSeries clone = (TimeSeries) clonedObj;
        assertEquals(0, clone.getItemCount());
        assertEquals(series.getKey(), clone.getKey());
        assertEquals(series.getTimePeriodClass(), clone.getTimePeriodClass());
    }

    @Test(timeout = 4000)
    public void testCloneNonEmptySeries() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("NonEmptySeries", Year.class);
        series.add(new Year(2001), 10.0);
        series.add(new Year(2002), 20.0);

        TimeSeries clone = (TimeSeries) series.clone();
        assertEquals(2, clone.getItemCount());
        assertEquals(series, clone);
        assertNotSame(series, clone);

        // Ensure deep copy of items
        clone.update(0, 99.0);
        assertFalse(series.equals(clone));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetMaximumItemCountNegativeThrows() {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.setMaximumItemCount(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetMaximumItemAgeNegativeThrows() {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.setMaximumItemAge(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetIndexNullThrows() {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.getIndex(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddNullItemThrows() {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.add((TimeSeriesDataItem) null);
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testAddIncompatiblePeriodClassThrows() {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.add(new Day(1, 1, 2000), 10.0);
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testAddDuplicatePeriodThrows() {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.add(new Year(2001), 10.0);
        series.add(new Year(2001), 20.0);
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testUpdateNonExistentThrows() {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.update(new Year(2001), 10.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddOrUpdateNullPeriodThrows() {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.addOrUpdate(null, 10.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDeleteInvalidRangeThrows() {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.add(new Year(2000), 1.0);
        series.delete(2, 1); // end < start
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyNegativeStartThrows() throws Exception {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.createCopy(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyEndLessThanStartThrows() throws Exception {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.createCopy(2, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyNullStartPeriodThrows() throws Exception {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.createCopy(null, new Year(2001));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyNullEndPeriodThrows() throws Exception {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.createCopy(new Year(2001), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyStartAfterEndPeriodThrows() throws Exception {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.createCopy(new Year(2002), new Year(2001));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (Equals, HashCode, Copy)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateCopyByPeriods() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Test", Year.class);
        series.add(new Year(2001), 10.0);
        series.add(new Year(2002), 20.0);
        series.add(new Year(2003), 30.0);

        // Exact range match
        TimeSeries sub1 = series.createCopy(new Year(2001), new Year(2002));
        assertEquals(2, sub1.getItemCount());
        assertEquals(new Year(2001), sub1.getTimePeriod(0));
        assertEquals(new Year(2002), sub1.getTimePeriod(1));

        // Range before all items -> empty range
        TimeSeries subEmptyBefore = series.createCopy(new Year(1990), new Year(1995));
        assertEquals(0, subEmptyBefore.getItemCount());

        // Range after all items -> empty range
        TimeSeries subEmptyAfter = series.createCopy(new Year(2010), new Year(2015));
        assertEquals(0, subEmptyAfter.getItemCount());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        TimeSeries s1 = new TimeSeries("Name", "Domain", "Range", Year.class);
        TimeSeries s2 = new TimeSeries("Name", "Domain", "Range", Year.class);

        // Reflexive & Symmetric on empty
        assertTrue(s1.equals(s1));
        assertTrue(s1.equals(s2));
        assertTrue(s2.equals(s1));
        assertEquals(s1.hashCode(), s2.hashCode());

        // Null / Non-TimeSeries
        assertFalse(s1.equals(null));
        assertFalse(s1.equals("Some String"));

        // Domain difference
        s2.setDomainDescription("OtherDomain");
        assertFalse(s1.equals(s2));
        s2.setDomainDescription("Domain");

        // Range difference
        s2.setRangeDescription("OtherRange");
        assertFalse(s1.equals(s2));
        s2.setRangeDescription("Range");

        // MaximumItemAge difference
        s2.setMaximumItemAge(50);
        assertFalse(s1.equals(s2));
        s2.setMaximumItemAge(Long.MAX_VALUE);

        // MaximumItemCount difference
        s2.setMaximumItemCount(50);
        assertFalse(s1.equals(s2));
        s2.setMaximumItemCount(Integer.MAX_VALUE);

        // Item count / content differences
        s1.add(new Year(2001), 10.0);
        assertFalse(s1.equals(s2));

        s2.add(new Year(2001), 20.0);
        assertFalse(s1.equals(s2)); // Same count, different value

        s2.update(0, 10.0);
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeCoverageBranches() {
        // Test hashCode paths for 0, 1, 2, and >2 items
        TimeSeries s = new TimeSeries("HashCodeTest", Year.class);
        int h0 = s.hashCode();

        s.add(new Year(2000), 1.0);
        int h1 = s.hashCode();
        assertNotEquals(h0, h1);

        s.add(new Year(2001), 2.0);
        int h2 = s.hashCode();
        assertNotEquals(h1, h2);

        s.add(new Year(2002), 3.0);
        int h3 = s.hashCode();
        assertNotEquals(h2, h3);

        s.add(new Year(2003), 4.0);
        int h4 = s.hashCode();
        assertNotEquals(h3, h4);
    }
}