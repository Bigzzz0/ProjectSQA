package org.jfree.data.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jfree.data.event.SeriesChangeEvent;
import org.jfree.data.event.SeriesChangeListener;
import org.jfree.data.general.SeriesException;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.jfree.data.time.TimeSeries
 * Defect ID: testCreateCopy3 (Defects4J ground truth: expected:<101.0> but was:<102.0>)
 * Root Cause in Target Code: createCopy(int, int) and createCopy(RegularTimePeriod, RegularTimePeriod)
 * clone via super.clone() which retains stale minY and maxY values from the source series without
 * resetting them to Double.NaN prior to adding copied items.
 *
 * Decision / Branch Coverage Points Targeted:
 * - Constructors: null handling, default vs custom domain/range strings.
 * - setDomainDescription & setRangeDescription: PropertyChangeEvent trigger and equality logic.
 * - setMaximumItemCount: negative validation (< 0), truncation branch (count > maximum).
 * - setMaximumItemAge: negative validation (< 0), aging eviction branch.
 * - add(TimeSeriesDataItem, boolean):
 *     - null check
 *     - timePeriodClass initialization vs mismatch check (SeriesException)
 *     - insertion at count == 0
 *     - insertion appending at end (item > last)
 *     - insertion in middle (binarySearch < 0)
 *     - duplicate insertion throwing SeriesException (binarySearch >= 0)
 *     - item capacity eviction (getItemCount() > maximumItemCount)
 *     - age eviction via removeAgedItems
 *     - notify = true vs false
 * - update(RegularTimePeriod, Number) & update(int, Number):
 *     - key not found (SeriesException)
 *     - boundary recalculation (oldY <= minY || oldY >= maxY)
 *     - non-boundary recalculation (updating minY / maxY)
 * - addOrUpdate methods:
 *     - null check
 *     - class mismatch check
 *     - existing item update branch (clone returned, iterate vs direct min/max update)
 *     - new item add branch (eviction on maximumItemCount)
 * - addAndOrUpdate: merging series and returning overwritten elements.
 * - removeAgedItems(boolean) & removeAgedItems(long, boolean):
 *     - empty data return
 *     - loop eviction based on maximumItemAge
 *     - reflection invocation of RegularTimePeriod.createInstance
 * - delete(RegularTimePeriod), delete(int, end, boolean):
 *     - end < start exception check
 *     - clearing timePeriodClass when dataset becomes empty
 *     - bounds recalculation after item removal
 * - clear(): empty vs non-empty reset of class and bounds.
 * - createCopy(int, int) and createCopy(RegularTimePeriod, RegularTimePeriod):
 *     - start < 0, end < start checks
 *     - null checks for periods, inverted start/end checks
 *     - range partially outside, emptyRange branches
 *     - cache clearing defect verification
 * - equals & hashCode:
 *     - identity, non-instance, domain/range mismatch, class mismatch, age mismatch,
 *       count mismatch, data mismatch, 0/1/2/3 item hashCode branches.
 * - Serialization & Cloning: Deep copy integrity of underlying data list.
 */
public class TimeSeriesGeminiTest {

    private static final double EPSILON = 1e-9;

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Defect)
    // =========================================================================

    /**
     * Directly reproduces the defect reported in testCreateCopy3.
     * When createCopy is invoked on a sub-range of a TimeSeries, the cloned instance
     * must reset its cached bounds (minY, maxY). In the unpatched code, maxY remains
     * 102.0 from the original series instead of reflecting the copy's actual max (101.0).
     */
    @Test(timeout = 4000)
    public void testDefectCreateCopyMaxYCache() throws CloneNotSupportedException {
        TimeSeries s1 = new TimeSeries("S1");
        s1.add(new Year(2009), 100.0);
        s1.add(new Year(2010), 101.0);
        s1.add(new Year(2011), 102.0);

        TimeSeries s2 = s1.createCopy(new Year(2009), new Year(2010));
        assertEquals(2, s2.getItemCount());
        assertEquals(100.0, s2.getMinY(), EPSILON);
        // Reveals defect: will fail on defective version expecting 101.0 but returning 102.0
        assertEquals(101.0, s2.getMaxY(), EPSILON);
    }

    /**
     * Target minY cache preservation defect when creating copy with index range.
     */
    @Test(timeout = 4000)
    public void testDefectCreateCopyIndexMinYCache() throws CloneNotSupportedException {
        TimeSeries s1 = new TimeSeries("S1");
        s1.add(new Year(2008), 50.0);
        s1.add(new Year(2009), 150.0);
        s1.add(new Year(2010), 200.0);

        TimeSeries s2 = s1.createCopy(1, 2);
        assertEquals(2, s2.getItemCount());
        // Defective version retains 50.0 as minY from s1
        assertEquals(150.0, s2.getMinY(), EPSILON);
        assertEquals(200.0, s2.getMaxY(), EPSILON);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndPropertyChangeEvents() {
        TimeSeries series = new TimeSeries("TestSeries");
        assertEquals("TestSeries", series.getKey());
        assertEquals("Time", series.getDomainDescription());
        assertEquals("Value", series.getRangeDescription());
        assertNull(series.getTimePeriodClass());
        assertEquals(0, series.getItemCount());
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));

        final boolean[] domainFired = {false};
        final boolean[] rangeFired = {false};
        series.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("Domain".equals(evt.getPropertyName())) {
                    domainFired[0] = true;
                } else if ("Range".equals(evt.getPropertyName())) {
                    rangeFired[0] = true;
                }
            }
        });

        series.setDomainDescription("NewDomain");
        assertEquals("NewDomain", series.getDomainDescription());
        assertTrue(domainFired[0]);

        series.setRangeDescription("NewRange");
        assertEquals("NewRange", series.getRangeDescription());
        assertTrue(rangeFired[0]);
    }

    @Test(timeout = 4000)
    public void testAddAndQueryOperations() {
        TimeSeries series = new TimeSeries("Series1");
        final int[] changeCount = {0};
        series.addChangeListener(new SeriesChangeListener() {
            @Override
            public void seriesChanged(SeriesChangeEvent event) {
                changeCount[0]++;
            }
        });

        Year y2005 = new Year(2005);
        series.add(y2005, 10.5, true);
        assertEquals(1, series.getItemCount());
        assertEquals(1, changeCount[0]);
        assertEquals(Year.class, series.getTimePeriodClass());
        assertEquals(10.5, series.getMinY(), EPSILON);
        assertEquals(10.5, series.getMaxY(), EPSILON);

        // Append to the end
        Year y2007 = new Year(2007);
        series.add(y2007, 30.0, false);
        assertEquals(2, series.getItemCount());
        assertEquals(1, changeCount[0]); // notify was false
        assertEquals(30.0, series.getMaxY(), EPSILON);

        // Insert in middle
        Year y2006 = new Year(2006);
        series.add(new TimeSeriesDataItem(y2006, 20.0));
        assertEquals(3, series.getItemCount());
        assertEquals(2, changeCount[0]);

        // Check index ordering: 2005, 2006, 2007
        assertEquals(y2005, series.getTimePeriod(0));
        assertEquals(y2006, series.getTimePeriod(1));
        assertEquals(y2007, series.getTimePeriod(2));
        assertEquals(20.0, series.getValue(1).doubleValue(), EPSILON);

        // Check getIndex and getValue(RegularTimePeriod)
        assertEquals(1, series.getIndex(y2006));
        assertEquals(20.0, series.getValue(y2006).doubleValue(), EPSILON);
        assertNull(series.getValue(new Year(2000)));

        // getNextTimePeriod
        RegularTimePeriod next = series.getNextTimePeriod();
        assertEquals(new Year(2008), next);

        // getDataItem clones
        TimeSeriesDataItem item = series.getDataItem(1);
        item.setValue(999.0);
        assertEquals(20.0, series.getValue(1).doubleValue(), EPSILON);

        // getDataItem(RegularTimePeriod)
        assertNotNull(series.getDataItem(y2005));
        assertNull(series.getDataItem(new Year(1999)));

        // raw data items
        assertNotNull(series.getRawDataItem(0));
        assertNotNull(series.getRawDataItem(y2005));
        assertNull(series.getRawDataItem(new Year(1999)));
    }

    @Test(timeout = 4000)
    public void testUpdateOperations() {
        TimeSeries series = new TimeSeries("S");
        series.add(new Year(2000), 10.0);
        series.add(new Year(2001), 20.0);
        series.add(new Year(2002), 30.0);

        // Update non-boundary: change 20.0 to 25.0
        series.update(1, 25.0);
        assertEquals(25.0, series.getValue(1).doubleValue(), EPSILON);
        assertEquals(10.0, series.getMinY(), EPSILON);
        assertEquals(30.0, series.getMaxY(), EPSILON);

        // Update boundary: change minY from 10.0 to 15.0 -> requires full iteration
        series.update(new Year(2000), 15.0);
        assertEquals(15.0, series.getMinY(), EPSILON);

        // Update with null value
        series.update(new Year(2001), (Number) null);
        assertNull(series.getValue(new Year(2001)));
        assertEquals(15.0, series.getMinY(), EPSILON);
        assertEquals(30.0, series.getMaxY(), EPSILON);

        // Update boundary to higher value
        series.update(2, 40.0);
        assertEquals(40.0, series.getMaxY(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAddOrUpdateBranches() {
        TimeSeries series = new TimeSeries("S");
        assertNull(series.addOrUpdate(new Year(2000), 10.0));
        assertNull(series.addOrUpdate(new Year(2002), 30.0));
        assertNull(series.addOrUpdate(new Year(2001), 20.0));

        assertEquals(3, series.getItemCount());
        assertEquals(10.0, series.getMinY(), EPSILON);
        assertEquals(30.0, series.getMaxY(), EPSILON);

        // Overwrite existing element
        TimeSeriesDataItem overwritten = series.addOrUpdate(new Year(2001), 25.0);
        assertNotNull(overwritten);
        assertEquals(20.0, overwritten.getValue().doubleValue(), EPSILON);
        assertEquals(25.0, series.getValue(new Year(2001)).doubleValue(), EPSILON);

        // Overwrite boundary (minY from 10.0 to 5.0)
        overwritten = series.addOrUpdate(new Year(2000), 5.0);
        assertNotNull(overwritten);
        assertEquals(10.0, overwritten.getValue().doubleValue(), EPSILON);
        assertEquals(5.0, series.getMinY(), EPSILON);

        // Overwrite with Double.NaN
        series.addOrUpdate(new Year(2000), Double.NaN);
        assertEquals(25.0, series.getMinY(), EPSILON);

        // Overwrite with null
        series.addOrUpdate(new Year(2002), (Number) null);
        assertNull(series.getValue(new Year(2002)));
    }

    @Test(timeout = 4000)
    public void testAddAndOrUpdateSeries() {
        TimeSeries s1 = new TimeSeries("Source1");
        s1.add(new Year(2000), 10.0);
        s1.add(new Year(2001), 20.0);

        TimeSeries s2 = new TimeSeries("Source2");
        s2.add(new Year(2001), 200.0);
        s2.add(new Year(2002), 300.0);

        TimeSeries overwritten = s1.addAndOrUpdate(s2);
        assertEquals(3, s1.getItemCount());
        assertEquals(1, overwritten.getItemCount());
        assertEquals(new Year(2001), overwritten.getTimePeriod(0));
        assertEquals(20.0, overwritten.getValue(0).doubleValue(), EPSILON);
        assertEquals(200.0, s1.getValue(new Year(2001)).doubleValue(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDeleteMethods() {
        TimeSeries series = new TimeSeries("S");
        series.add(new Year(2000), 10.0);
        series.add(new Year(2001), 20.0);
        series.add(new Year(2002), 30.0);
        series.add(new Year(2003), 40.0);

        // Delete non-existent period (no-op)
        series.delete(new Year(1999));
        assertEquals(4, series.getItemCount());

        // Delete single period
        series.delete(new Year(2000));
        assertEquals(3, series.getItemCount());
        assertEquals(20.0, series.getMinY(), EPSILON);

        // Delete range (start, end)
        series.delete(0, 1, true);
        assertEquals(1, series.getItemCount());
        assertEquals(new Year(2003), series.getTimePeriod(0));
        assertEquals(40.0, series.getMinY(), EPSILON);
        assertEquals(40.0, series.getMaxY(), EPSILON);

        // Delete remaining
        series.delete(new Year(2003));
        assertEquals(0, series.getItemCount());
        assertNull(series.getTimePeriodClass());
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
    }

    @Test(timeout = 4000)
    public void testClear() {
        TimeSeries series = new TimeSeries("S");
        // Clear empty series
        series.clear();
        assertEquals(0, series.getItemCount());

        series.add(new Year(2000), 10.0);
        series.add(new Year(2001), 20.0);
        assertEquals(2, series.getItemCount());

        series.clear();
        assertEquals(0, series.getItemCount());
        assertNull(series.getTimePeriodClass());
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Ageing / Bounds
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetMaximumItemCountEviction() {
        TimeSeries series = new TimeSeries("S");
        series.setMaximumItemCount(2);
        assertEquals(2, series.getMaximumItemCount());

        series.add(new Year(2000), 10.0);
        series.add(new Year(2001), 20.0);
        assertEquals(2, series.getItemCount());

        // Addition exceeds capacity -> drops 2000
        series.add(new Year(2002), 30.0);
        assertEquals(2, series.getItemCount());
        assertEquals(new Year(2001), series.getTimePeriod(0));
        assertEquals(new Year(2002), series.getTimePeriod(1));
        assertEquals(20.0, series.getMinY(), EPSILON);

        // Shrinking maximum item count below existing size
        series.add(new Year(2003), 40.0);
        series.setMaximumItemCount(1);
        assertEquals(1, series.getItemCount());
        assertEquals(new Year(2003), series.getTimePeriod(0));
        assertEquals(40.0, series.getMinY(), EPSILON);
        assertEquals(40.0, series.getMaxY(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAgeEvictionOnAdd() {
        TimeSeries series = new TimeSeries("S");
        series.setMaximumItemAge(2); // Keep within 2 periods
        assertEquals(2, series.getMaximumItemAge());

        series.add(new Year(2000), 10.0);
        series.add(new Year(2001), 20.0);
        series.add(new Year(2002), 30.0);
        assertEquals(3, series.getItemCount());

        // Adding 2003: timespan 2003 - 2000 = 3 > 2 -> removes 2000
        series.add(new Year(2003), 40.0);
        assertEquals(3, series.getItemCount());
        assertEquals(new Year(2001), series.getTimePeriod(0));
        assertEquals(20.0, series.getMinY(), EPSILON);

        // Changing maximumItemAge triggers eviction
        series.setMaximumItemAge(1);
        assertEquals(2, series.getItemCount());
        assertEquals(new Year(2002), series.getTimePeriod(0));
    }

    @Test(timeout = 4000)
    public void testRemoveAgedItemsByTimestamp() {
        TimeSeries series = new TimeSeries("S");
        // Calling with empty series should be a safe no-op
        series.removeAgedItems(100000L, true);
        assertEquals(0, series.getItemCount());

        series.setMaximumItemAge(2);
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(5, 1, 2020);

        series.add(d1, 10.0);
        series.add(d2, 20.0);

        // Age items against d3 timestamp
        long d3Middle = d3.getMiddleMillisecond();
        series.removeAgedItems(d3Middle, true);

        // Both d1 and d2 are > 2 days older than d3
        assertEquals(0, series.getItemCount());
        assertTrue(Double.isNaN(series.getMinY()));
    }

    @Test(timeout = 4000)
    public void testUniqueTimePeriods() {
        TimeSeries s1 = new TimeSeries("S1");
        TimeSeries s2 = new TimeSeries("S2");

        s1.add(new Year(2000), 1.0);
        s1.add(new Year(2001), 2.0);

        s2.add(new Year(2001), 20.0);
        s2.add(new Year(2002), 30.0);
        s2.add(new Year(2003), 40.0);

        Collection uniqueToS2 = s1.getTimePeriodsUniqueToOtherSeries(s2);
        assertEquals(2, uniqueToS2.size());
        assertTrue(uniqueToS2.contains(new Year(2002)));
        assertTrue(uniqueToS2.contains(new Year(2003)));
        assertFalse(uniqueToS2.contains(new Year(2001)));

        Collection periodsS1 = s1.getTimePeriods();
        assertEquals(2, periodsS1.size());
    }

    @Test(timeout = 4000)
    public void testCreateCopyVariousRanges() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("S");
        series.add(new Year(2001), 10.0);
        series.add(new Year(2003), 30.0);
        series.add(new Year(2005), 50.0);

        // Exact match copy
        TimeSeries c1 = series.createCopy(new Year(2001), new Year(2003));
        assertEquals(2, c1.getItemCount());
        assertEquals(new Year(2001), c1.getTimePeriod(0));
        assertEquals(new Year(2003), c1.getTimePeriod(1));

        // Start and end outside range
        TimeSeries c2 = series.createCopy(new Year(2000), new Year(2004));
        assertEquals(2, c2.getItemCount());
        assertEquals(new Year(2001), c2.getTimePeriod(0));
        assertEquals(new Year(2003), c2.getTimePeriod(1));

        // Start after all data items -> empty series
        TimeSeries c3 = series.createCopy(new Year(2006), new Year(2010));
        assertEquals(0, c3.getItemCount());

        // Range between existing items without matching any -> empty series
        TimeSeries c4 = series.createCopy(new Year(2002), new Year(2002));
        assertEquals(0, c4.getItemCount());

        // Range before any items
        TimeSeries c5 = series.createCopy(new Year(1990), new Year(1995));
        assertEquals(0, c5.getItemCount());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNegativeMaximumItemCount() {
        TimeSeries series = new TimeSeries("S");
        series.setMaximumItemCount(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNegativeMaximumItemAge() {
        TimeSeries series = new TimeSeries("S");
        series.setMaximumItemAge(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddNullItem() {
        TimeSeries series = new TimeSeries("S");
        series.add((TimeSeriesDataItem) null);
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testAddPeriodClassMismatch() {
        TimeSeries series = new TimeSeries("S");
        series.add(new Year(2000), 10.0);
        series.add(new Day(1, 1, 2001), 20.0); // Mismatch: Day vs Year
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testAddDuplicatePeriod() {
        TimeSeries series = new TimeSeries("S");
        series.add(new Year(2000), 10.0);
        series.add(new Year(2000), 15.0); // Duplicate
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testUpdateMissingPeriod() {
        TimeSeries series = new TimeSeries("S");
        series.add(new Year(2000), 10.0);
        series.update(new Year(2001), 20.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetIndexNull() {
        TimeSeries series = new TimeSeries("S");
        series.getIndex(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDeleteInvertedIndex() {
        TimeSeries series = new TimeSeries("S");
        series.delete(5, 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyInvalidIndex() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("S");
        series.createCopy(-1, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyInvertedIndex() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("S");
        series.createCopy(2, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyNullStart() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("S");
        series.createCopy(null, new Year(2000));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyNullEnd() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("S");
        series.createCopy(new Year(2000), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCopyInvertedPeriods() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("S");
        series.createCopy(new Year(2005), new Year(2000));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddOrUpdateNullItem() {
        TimeSeries series = new TimeSeries("S");
        series.addOrUpdate((TimeSeriesDataItem) null);
    }

    @Test(expected = SeriesException.class, timeout = 4000)
    public void testAddOrUpdateClassMismatch() {
        TimeSeries series = new TimeSeries("S");
        series.addOrUpdate(new Year(2000), 1.0);
        series.addOrUpdate(new Day(1, 1, 2001), 2.0);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Equals, HashCode, Clone & Serialization
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        TimeSeries s1 = new TimeSeries("Series", "Domain", "Range");
        TimeSeries s2 = new TimeSeries("Series", "Domain", "Range");

        // Identity
        assertTrue(s1.equals(s1));
        assertFalse(s1.equals(null));
        assertFalse(s1.equals("Not a TimeSeries"));

        // Match
        assertTrue(s1.equals(s2));
        assertTrue(s2.equals(s1));
        assertEquals(s1.hashCode(), s2.hashCode());

        // Domain mismatch
        s2.setDomainDescription("OtherDomain");
        assertFalse(s1.equals(s2));
        s2.setDomainDescription("Domain");
        assertTrue(s1.equals(s2));

        // Range mismatch
        s2.setRangeDescription("OtherRange");
        assertFalse(s1.equals(s2));
        s2.setRangeDescription("Range");
        assertTrue(s1.equals(s2));

        // MaximumItemAge mismatch
        s2.setMaximumItemAge(50);
        assertFalse(s1.equals(s2));
        s2.setMaximumItemAge(s1.getMaximumItemAge());
        assertTrue(s1.equals(s2));

        // MaximumItemCount mismatch
        s2.setMaximumItemCount(10);
        assertFalse(s1.equals(s2));
        s2.setMaximumItemCount(s1.getMaximumItemCount());
        assertTrue(s1.equals(s2));

        // Data mismatch & hashCode branches for 1, 2, and 3+ items
        s1.add(new Year(2001), 10.0);
        assertFalse(s1.equals(s2));
        s2.add(new Year(2001), 10.0);
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(new Year(2002), 20.0);
        s2.add(new Year(2002), 20.0);
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(new Year(2003), 30.0);
        s2.add(new Year(2003), 30.0);
        assertEquals(s1.hashCode(), s2.hashCode());

        // Value mismatch within data item
        s2.update(2, 99.0);
        assertFalse(s1.equals(s2));
    }

    @Test(timeout = 4000)
    public void testCloneDeepCopyIntegrity() throws CloneNotSupportedException {
        TimeSeries s1 = new TimeSeries("Series");
        s1.add(new Year(2000), 100.0);
        s1.add(new Year(2001), 200.0);

        TimeSeries clone = (TimeSeries) s1.clone();
        assertNotSame(s1, clone);
        assertEquals(s1, clone);

        // Modify original, clone should remain isolated
        s1.add(new Year(2002), 300.0);
        assertEquals(3, s1.getItemCount());
        assertEquals(2, clone.getItemCount());

        // Modify item in clone, original untouched
        clone.update(0, 999.0);
        assertEquals(100.0, s1.getValue(0).doubleValue(), EPSILON);
        assertEquals(999.0, clone.getValue(0).doubleValue(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        TimeSeries s1 = new TimeSeries("Series", "D", "R");
        s1.add(new Year(2000), 10.0);
        s1.add(new Year(2001), 20.0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(s1);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        TimeSeries s2 = (TimeSeries) ois.readObject();

        assertEquals(s1, s2);
        assertEquals(10.0, s2.getMinY(), EPSILON);
        assertEquals(20.0, s2.getMaxY(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testGetItemsUnmodifiable() {
        TimeSeries s = new TimeSeries("S");
        s.add(new Year(2000), 10.0);
        List items = s.getItems();
        try {
            items.clear();
            fail("getItems() should return an unmodifiable list");
        } catch (UnsupportedOperationException expected) {
            // Success
        }
    }
}