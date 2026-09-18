/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jfree.data.time.TimePeriodValues
 * 
 * 1. Defect-Targeted Branch Zone:
 *    - updateBounds(): In the maxMiddleIndex branch (this.maxMiddleIndex >= 0), the method incorrectly
 *      calculates maxMiddle using this.minMiddleIndex rather than this.maxMiddleIndex:
 *        long s = getDataItem(this.minMiddleIndex).getPeriod().getStart().getTime();
 *      When adding a subsequent item with a middle value lower than current max middle but higher than min middle,
 *      maxMiddleIndex is erroneously overwritten.
 *      Ground truth defect test: testGetMaxMiddleIndex() asserts expected index 1, exposing the bug where it returns 3.
 *
 * 2. Equivalence Partitioning & Boundary Value Analysis (BVA):
 *    - Constructors: 1-arg constructor defaulting domain/range; 3-arg constructor with custom or null strings.
 *    - Domain/Range Properties: Getters, setters, and PropertyChangeEvent firing with old/new values.
 *    - Bound Index Accessors (min/max for Start, Middle, End):
 *      - Initial state on empty series (all -1).
 *      - Single element added (all 0).
 *      - Monotonically increasing periods.
 *      - Monotonically decreasing periods.
 *      - Mixed periods updating min and max bounds independently.
 *    - add() variations: add(TimePeriodValue), add(TimePeriod, double), add(TimePeriod, Number).
 *      - null TimePeriodValue check throws IllegalArgumentException.
 *      - null Number value allowed.
 *    - update(int, Number): mutating an item's value and firing series change events.
 *    - delete(int, int): single item, sub-range, entire range; triggering recalculateBounds().
 *    - createCopy(int, int) & clone(): copying subset, cloning empty series, cloning multi-item series.
 *    - equals() & hashCode():
 *      - Same reference, null reference, incompatible type, super mismatch (different series keys).
 *      - Domain differences (null vs non-null, different values).
 *      - Range differences (null vs non-null, different values).
 *      - Item count mismatch and individual data item mismatches.
 *      - Symmetric and reflexive contracts.
 */

package org.jfree.data.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;

public class TimePeriodValuesGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the known defect where updateBounds() calculates maxMiddle using minMiddleIndex
     * instead of maxMiddleIndex.
     * Sequence:
     * - Item 0: Middle = 500
     * - Item 1: Middle = 1000 (becomes maxMiddleIndex = 1)
     * - Item 2: Middle = 100  (becomes minMiddleIndex = 2)
     * - Item 3: Middle = 600  (middle < maxMiddle(1000), but middle > minMiddle(100))
     * Due to the defect, maxMiddleIndex becomes 3 instead of remaining 1.
     */
    @Test(timeout = 4000)
    public void testGetMaxMiddleIndex() {
        TimePeriodValues s = new TimePeriodValues("Test Series");
        s.add(new SimpleTimePeriod(400L, 600L), 1.0);  // item 0: middle = 500
        s.add(new SimpleTimePeriod(900L, 1100L), 2.0); // item 1: middle = 1000 (max)
        s.add(new SimpleTimePeriod(50L, 150L), 3.0);   // item 2: middle = 100 (min)
        s.add(new SimpleTimePeriod(550L, 650L), 4.0);  // item 3: middle = 600

        assertEquals("maxMiddleIndex should remain at index 1", 1, s.getMaxMiddleIndex());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndDefaults() {
        TimePeriodValues series = new TimePeriodValues("Series A");
        assertEquals("Series A", series.getKey());
        assertEquals("Time", series.getDomainDescription());
        assertEquals("Value", series.getRangeDescription());
        assertEquals(0, series.getItemCount());
        assertEquals(-1, series.getMinStartIndex());
        assertEquals(-1, series.getMaxStartIndex());
        assertEquals(-1, series.getMinMiddleIndex());
        assertEquals(-1, series.getMaxMiddleIndex());
        assertEquals(-1, series.getMinEndIndex());
        assertEquals(-1, series.getMaxEndIndex());

        TimePeriodValues customSeries = new TimePeriodValues("Custom", "CustomDomain", "CustomRange");
        assertEquals("Custom", customSeries.getKey());
        assertEquals("CustomDomain", customSeries.getDomainDescription());
        assertEquals("CustomRange", customSeries.getRangeDescription());
    }

    @Test(timeout = 4000)
    public void testSetDomainAndRangeDescriptions() {
        TimePeriodValues series = new TimePeriodValues("S");
        final boolean[] propFired = new boolean[]{false, false};

        series.addPropertyChangeListener(evt -> {
            if ("Domain".equals(evt.getPropertyName())) {
                propFired[0] = true;
            } else if ("Range".equals(evt.getPropertyName())) {
                propFired[1] = true;
            }
        });

        series.setDomainDescription("NewDomain");
        assertEquals("NewDomain", series.getDomainDescription());
        assertTrue(propFired[0]);

        series.setRangeDescription("NewRange");
        assertEquals("NewRange", series.getRangeDescription());
        assertTrue(propFired[1]);

        series.setDomainDescription(null);
        assertNull(series.getDomainDescription());

        series.setRangeDescription(null);
        assertNull(series.getRangeDescription());
    }

    @Test(timeout = 4000)
    public void testAddAndAccessors() {
        TimePeriodValues series = new TimePeriodValues("S");
        SimpleTimePeriod p1 = new SimpleTimePeriod(100L, 200L);
        series.add(p1, 55.5);

        assertEquals(1, series.getItemCount());
        assertEquals(p1, series.getTimePeriod(0));
        assertEquals(55.5, series.getValue(0).doubleValue(), 1e-9);

        TimePeriodValue item = series.getDataItem(0);
        assertEquals(p1, item.getPeriod());
        assertEquals(55.5, item.getValue().doubleValue(), 1e-9);

        SimpleTimePeriod p2 = new SimpleTimePeriod(300L, 400L);
        series.add(p2, (Number) null);
        assertEquals(2, series.getItemCount());
        assertEquals(p2, series.getTimePeriod(1));
        assertNull(series.getValue(1));
    }

    @Test(timeout = 4000)
    public void testSeriesChangeEventOnAdd() {
        TimePeriodValues series = new TimePeriodValues("S");
        final int[] eventsCount = new int[]{0};

        series.addChangeListener(new SeriesChangeListener() {
            public void seriesChanged(SeriesChangeEvent event) {
                eventsCount[0]++;
            }
        });

        series.add(new SimpleTimePeriod(100L, 200L), 10.0);
        assertEquals(1, eventsCount[0]);

        series.add(new TimePeriodValue(new SimpleTimePeriod(200L, 300L), 20.0));
        assertEquals(2, eventsCount[0]);
    }

    @Test(timeout = 4000)
    public void testUpdateValue() {
        TimePeriodValues series = new TimePeriodValues("S");
        series.add(new SimpleTimePeriod(100L, 200L), 10.0);

        final boolean[] changed = new boolean[]{false};
        series.addChangeListener(new SeriesChangeListener() {
            public void seriesChanged(SeriesChangeEvent event) {
                changed[0] = true;
            }
        });

        series.update(0, 99.9);
        assertEquals(99.9, series.getValue(0).doubleValue(), 1e-9);
        assertTrue(changed[0]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBoundsSingleItem() {
        TimePeriodValues series = new TimePeriodValues("Single");
        series