package org.jfree.data.category;

import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: DefaultIntervalCategoryDataset
 *
 * 1. Constructor Branch Analysis:
 *    - double[][] vs Number[][] delegating constructors.
 *    - seriesCount != ends.length -> throws IllegalArgumentException.
 *    - seriesCount > 0 vs seriesCount == 0 (Empty dataset handling - Defect Zone).
 *    - seriesKeys != null (length matches vs length mismatch -> IllegalArgumentException).
 *    - seriesKeys == null -> auto-generated default prefix.
 *    - categoryCount != ends[0].length -> throws IllegalArgumentException.
 *    - categoryKeys != null (length matches vs length mismatch -> IllegalArgumentException).
 *    - categoryKeys == null -> auto-generated default prefix.
 *    - starts == null || ends == null -> leaves fields uninitialized without crashing constructor.
 *
 * 2. Defect-Targeted Zone (Known Defects4J Failures on Empty Datasets):
 *    - When instantiated with empty 2D arrays (e.g. Number[0][0] or double[0][0]):
 *      - getRowCount() triggers NPE if seriesKeys is null (expected: 0).
 *      - getColumnCount() triggers NPE if categoryKeys is null (expected: 0).
 *      - getSeriesIndex(Comparable) triggers NPE if seriesKeys is null (expected: -1).
 *      - getCategoryIndex(Comparable) triggers NPE if categoryKeys is null (expected: -1).
 *      - getColumnIndex(Comparable) triggers NPE if categoryKeys is null (expected: -1).
 *      - getRowIndex(Comparable) triggers NPE if seriesKeys is null (expected: -1).
 *      - clone() triggers NPE if seriesKeys or categoryKeys is null.
 *
 * 3. Range & Bound Analysis:
 *    - getSeriesKey: series < 0, series >= getSeriesCount() -> IllegalArgumentException.
 *    - getStartValue / getEndValue (int, int): series/category < 0, >= count -> IllegalArgumentException.
 *    - setStartValue / setEndValue: series < 0, series >= count, unknown category -> IllegalArgumentException.
 *    - setSeriesKeys: null, length mismatch -> IllegalArgumentException.
 *    - setCategoryKeys: null, length mismatch, null element in array -> IllegalArgumentException.
 *    - getColumnIndex: null argument -> IllegalArgumentException.
 *    - getRowKey: row < 0, row >= getRowCount() -> IllegalArgumentException.
 *    - getValue / getStartValue / getEndValue (Comparable, Comparable): unknown key -> UnknownKeyException.
 *
 * 4. Contract & Lifecycle Integrity:
 *    - equals(): reflexive, symmetric, non-DefaultIntervalCategoryDataset, diff series, diff categories,
 *      diff startData, diff endData, null handling in helper equal().
 *    - clone(): independent deep copy of 2D data arrays and keys.
 *    - Event listener notification on setSeriesKeys, setCategoryKeys, setStartValue, setEndValue.
 */
public class DefaultIntervalCategoryDatasetGeminiTest {

    // =========================================================================
    // Helper Listener for Change Event Verifications
    // =========================================================================
    private static class TestDatasetChangeListener implements DatasetChangeListener {
        private int eventCount = 0;

        @Override
        public void datasetChanged(DatasetChangeEvent event) {
            this.eventCount++;
        }

        public int getEventCount() {
            return this.eventCount;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoubleArrayConstructorAndBasicGetters() {
        double[][] starts = new double[][] {{0.0, 1.0}, {2.0, 3.0}};
        double[][] ends = new double[][] {{0.5, 1.5}, {2.5, 3.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals(2, dataset.getSeriesCount());
        assertEquals(2, dataset.getCategoryCount());
        assertEquals(2, dataset.getRowCount());
        assertEquals(2, dataset.getColumnCount());

        assertEquals(new Double(0.0), dataset.getStartValue(0, 0));
        assertEquals(new Double(1.0), dataset.getStartValue(0, 1));
        assertEquals(new Double(0.5), dataset.getEndValue(0, 0));
        assertEquals(new Double(1.5), dataset.getEndValue(0, 1));
        assertEquals(new Double(0.5), dataset.getValue(0, 0));
    }

    @Test(timeout = 4000)
    public void testCustomKeysConstructorAndValueByKey() {
        String[] series = new String[] {"S1", "S2"};
        String[] categories = new String[] {"C1", "C2", "C3"};
        Number[][] starts = new Number[][] {
                {1, 2, 3},
                {4, 5, 6}
        };
        Number[][] ends = new Number[][] {
                {10, 20, 30},
                {40, 50, 60}
        };

        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(series, categories, starts, ends);

        assertEquals("S1", dataset.getSeriesKey(0));
        assertEquals("S2", dataset.getSeriesKey(1));
        assertEquals("C1", dataset.getColumnKey(0));
        assertEquals("C2", dataset.getColumnKey(1));
        assertEquals("C3", dataset.getColumnKey(2));

        assertEquals(0, dataset.getSeriesIndex("S1"));
        assertEquals(1, dataset.getSeriesIndex("S2"));
        assertEquals(-1, dataset.getSeriesIndex("NonExistentSeries"));

        assertEquals(0, dataset.getCategoryIndex("C1"));
        assertEquals(1, dataset.getCategoryIndex("C2"));
        assertEquals(2, dataset.getCategoryIndex("C3"));
        assertEquals(-1, dataset.getCategoryIndex("NonExistentCat"));

        assertEquals(0, dataset.getColumnIndex("C1"));
        assertEquals(0, dataset.getRowIndex("S1"));

        assertEquals(1, dataset.getStartValue("S1", "C1"));
        assertEquals(10, dataset.getEndValue("S1", "C1"));
        assertEquals(10, dataset.getValue("S1", "C1"));
        assertEquals(6, dataset.getStartValue("S2", "C3"));
        assertEquals(60, dataset.getEndValue("S2", "C3"));
    }

    @Test(timeout = 4000)
    public void testSeriesNamesOnlyConstructor() {
        String[] seriesNames = new String[] {"Alpha", "Beta"};
        Number[][] starts = new Number[][] {{1.0}, {2.0}};
        Number[][] ends = new Number[][] {{1.5}, {2.5}};

        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(seriesNames, starts, ends);

        assertEquals(2, dataset.getSeriesCount());
        assertEquals("Alpha", dataset.getSeriesKey(0));
        assertEquals("Beta", dataset.getSeriesKey(1));
        assertEquals("Category 1", dataset.getColumnKey(0));
    }

    @Test(timeout = 4000)
    public void testKeyModificationAndEventFiring() {
        Number[][] starts = new Number[][] {{1.0, 2.0}};
        Number[][] ends = new Number[][] {{1.5, 2.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        TestDatasetChangeListener listener = new TestDatasetChangeListener();
        dataset.addChangeListener(listener);

        Comparable[] newSeries = new Comparable[] {"NewSeries"};
        dataset.setSeriesKeys(newSeries);
        assertEquals("NewSeries", dataset.getSeriesKey(0));
        assertEquals(1, listener.getEventCount());

        Comparable[] newCategories = new Comparable[] {"CatA", "CatB"};
        dataset.setCategoryKeys(newCategories);
        assertEquals("CatA", dataset.getColumnKey(0));
        assertEquals("CatB", dataset.getColumnKey(1));
        assertEquals(2, listener.getEventCount());
    }

    @Test(timeout = 4000)
    public void testUpdateValuesAndEvents() {
        Number[][] starts = new Number[][] {{10.0, 20.0}};
        Number[][] ends = new Number[][] {{15.0, 25.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        TestDatasetChangeListener listener = new TestDatasetChangeListener();
        dataset.addChangeListener(listener);

        Comparable catKey = dataset.getColumnKey(0);
        dataset.setStartValue(0, catKey, 99.0);
        assertEquals(99.0, dataset.getStartValue(0, 0));
        assertEquals(1, listener.getEventCount());

        dataset.setEndValue(0, catKey, 109.0);
        assertEquals(109.0, dataset.getEndValue(0, 0));
        assertEquals(2, listener.getEventCount());
    }

    @Test(timeout = 4000)
    public void testGetRowAndColumnKeys() {
        String[] series = new String[] {"Row0", "Row1"};
        String[] categories = new String[] {"Col0", "Col1"};
        Number[][] starts = new Number[][] {{1, 2}, {3, 4}};
        Number[][] ends = new Number[][] {{5, 6}, {7, 8}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(series, categories, starts, ends);

        List rowKeys = dataset.getRowKeys();
        assertEquals(2, rowKeys.size());
        assertEquals("Row0", rowKeys.get(0));
        assertEquals("Row1", rowKeys.get(1));

        List colKeys = dataset.getColumnKeys();
        assertEquals(2, colKeys.size());
        assertEquals("Col0", colKeys.get(0));
        assertEquals("Col1", colKeys.get(1));

        assertEquals("Row0", dataset.getRowKey(0));
        assertEquals("Row1", dataset.getRowKey(1));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Null/Zero Inputs
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullDataConstructor() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((Number[][]) null, null);
        assertEquals(0, dataset.getSeriesCount());
        assertEquals(0, dataset.getCategoryCount());
        assertTrue(dataset.getRowKeys().isEmpty());
        assertTrue(dataset.getColumnKeys().isEmpty());
    }

    @Test(timeout = 4000)
    public void testNullStartsOrEnds() {
        Number[][] valid = new Number[][] {{1}};
        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset(valid, null);
        assertEquals(1, dataset1.getSeriesCount());
        assertEquals(0, dataset1.getCategoryCount());

        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset(null, valid);
        assertEquals(0, dataset2.getSeriesCount());
        assertEquals(0, dataset2.getCategoryCount());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Known Defects4J Failures on Empty Datasets)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyDatasetRowCountDefect() {
        Number[][] starts = new Number[0][0];
        Number[][] ends = new Number[0][0];
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        // Ground Truth Bug: dataset.getRowCount() throws NullPointerException when seriesKeys is null
        assertEquals(0, dataset.getRowCount());
    }

    @Test(timeout = 4000)
    public void testEmptyDatasetColumnCountDefect() {
        Number[][] starts = new Number[0][0];
        Number[][] ends = new Number[0][0];
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        // Ground Truth Bug: dataset.getColumnCount() throws NullPointerException when categoryKeys is null
        assertEquals(0, dataset.getColumnCount());
    }

    @Test(timeout = 4000)
    public void testEmptyDatasetGetSeriesIndexDefect() {
        Number[][] starts = new Number[0][0];
        Number[][] ends = new Number[0][0];
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        // Ground Truth Bug: dataset.getSeriesIndex(...) throws NullPointerException when seriesKeys is null
        assertEquals(-1, dataset.getSeriesIndex("AnySeries"));
    }

    @Test(timeout = 4000)
    public void testEmptyDatasetGetRowIndexDefect() {
        Number[][] starts = new Number[0][0];
        Number[][] ends = new Number[0][0];
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        // Ground Truth Bug: dataset.getRowIndex(...) delegates to getSeriesIndex, throwing NPE
        assertEquals(-1, dataset.getRowIndex("AnySeries"));
    }

    @Test(timeout = 4000)
    public void testEmptyDatasetGetCategoryIndexDefect() {
        Number[][] starts = new Number[0][0];
        Number[][] ends = new Number[0][0];
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        // Ground Truth Bug: dataset.getCategoryIndex(...) throws NullPointerException when categoryKeys is null
        assertEquals(-1, dataset.getCategoryIndex("AnyCategory"));
    }

    @Test(timeout = 4000)
    public void testEmptyDatasetGetColumnIndexDefect() {
        Number[][] starts = new Number[0][0];
        Number[][] ends = new Number[0][0];
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        // Ground Truth Bug: dataset.getColumnIndex(...) delegates to getCategoryIndex, throwing NPE
        assertEquals(-1, dataset.getColumnIndex("AnyCategory"));
    }

    @Test(timeout = 4000)
    public void testEmptyDatasetCloneDefect() throws CloneNotSupportedException {
        Number[][] starts = new Number[0][0];
        Number[][] ends = new Number[0][0];
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        // Ground Truth Bug: dataset.clone() attempts to clone null seriesKeys and categoryKeys -> NPE
        DefaultIntervalCategoryDataset cloned = (DefaultIntervalCategoryDataset) dataset.clone();
        assertNotNull(cloned);
        assertEquals(0, cloned.getSeriesCount());
        assertEquals(0, cloned.getCategoryCount());
    }

    @Test(timeout = 4000)
    public void testEmptyDatasetSetCategoryKeysDefect() {
        Number[][] starts = new Number[0][0];
        Number[][] ends = new Number[0][0];
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        // Ground Truth Bug: accessing this.startData[0].length throws ArrayIndexOutOfBoundsException
        try {
            dataset.setCategoryKeys(new Comparable[0]);
            assertEquals(0, dataset.getColumnKeys().size());
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            fail("setCategoryKeys on an empty dataset threw ArrayIndexOutOfBoundsException");
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorSeriesCountMismatch() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}, {2.5}};
        new DefaultIntervalCategoryDataset(starts, ends);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorCategoryCountMismatch() {
        Number[][] starts = new Number[][] {{1.0, 2.0}};
        Number[][] ends = new Number[][] {{1.5}};
        new DefaultIntervalCategoryDataset(starts, ends);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorSeriesKeysMismatch() {
        Comparable[] series = new Comparable[] {"S1"};
        Number[][] starts = new Number[][] {{1.0}, {2.0}};
        Number[][] ends = new Number[][] {{1.5}, {2.5}};
        new DefaultIntervalCategoryDataset(series, null, starts, ends);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorCategoryKeysMismatch() {
        Comparable[] cats = new Comparable[] {"C1"};
        Number[][] starts = new Number[][] {{1.0, 2.0}};
        Number[][] ends = new Number[][] {{1.5, 2.5}};
        new DefaultIntervalCategoryDataset(null, cats, starts, ends);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetSeriesKeyOutOfBoundsLower() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getSeriesKey(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetSeriesKeyOutOfBoundsUpper() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getSeriesKey(1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetSeriesKeysNull() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setSeriesKeys(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetSeriesKeysLengthMismatch() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setSeriesKeys(new Comparable[] {"S1", "S2"});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetCategoryKeysNull() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setCategoryKeys(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetCategoryKeysLengthMismatch() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setCategoryKeys(new Comparable[] {"C1", "C2"});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetCategoryKeysWithNullElement() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setCategoryKeys(new Comparable[] {null});
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testGetValueUnknownSeries() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getValue("UnknownSeries", dataset.getColumnKey(0));
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testGetValueUnknownCategory() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getValue(dataset.getSeriesKey(0), "UnknownCategory");
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testGetStartValueUnknownSeries() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue("UnknownSeries", dataset.getColumnKey(0));
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testGetStartValueUnknownCategory() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue(dataset.getSeriesKey(0), "UnknownCategory");
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testGetEndValueUnknownSeries() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getEndValue("UnknownSeries", dataset.getColumnKey(0));
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testGetEndValueUnknownCategory() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getEndValue(dataset.getSeriesKey(0), "UnknownCategory");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetStartValueNegativeSeries() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetStartValueSeriesOutOfBounds() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue(1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetStartValueNegativeCategory() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue(0, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetStartValueCategoryOutOfBounds() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue(0, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetEndValueNegativeSeries() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getEndValue(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetEndValueSeriesOutOfBounds() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getEndValue(1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetEndValueNegativeCategory() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getEndValue(0, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetEndValueCategoryOutOfBounds() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getEndValue(0, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetStartValueInvalidSeries() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setStartValue(1, dataset.getColumnKey(0), 10.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetStartValueInvalidCategory() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setStartValue(0, "InvalidCategory", 10.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetEndValueInvalidSeries() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setEndValue(-1, dataset.getColumnKey(0), 10.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetEndValueInvalidCategory() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setEndValue(0, "InvalidCategory", 10.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetColumnIndexNull() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getColumnIndex(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetRowKeyOutOfBounds() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getRowKey(5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetRowKeyNegative() {
        Number[][] starts = new Number[][] {{1.0}};
        Number[][] ends = new Number[][] {{1.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getRowKey(-1);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (equals, clone)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndCloneContract() throws CloneNotSupportedException {
        Number[][] s1 = new Number[][] {{1.0, 2.0}, {3.0, 4.0}};
        Number[][] e1 = new Number[][] {{1.5, 2.5}, {3.5, 4.5}};
        DefaultIntervalCategoryDataset d1 = new DefaultIntervalCategoryDataset(s1, e1);

        // Reflexive
        assertTrue(d1.equals(d1));
        assertFalse(d1.equals(null));
        assertFalse(d1.equals("NotADataset"));

        // Same content
        Number[][] s2 = new Number[][] {{1.0, 2.0}, {3.0, 4.0}};
        Number[][] e2 = new Number[][] {{1.5, 2.5}, {3.5, 4.5}};
        DefaultIntervalCategoryDataset d2 = new DefaultIntervalCategoryDataset(s2, e2);
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));

        // Different seriesKeys
        d2.setSeriesKeys(new Comparable[] {"Diff1", "Diff2"});
        assertFalse(d1.equals(d2));

        // Different categoryKeys
        d2 = new DefaultIntervalCategoryDataset(s2, e2);
        d2.setCategoryKeys(new Comparable[] {"DiffCat1", "DiffCat2"});
        assertFalse(d1.equals(d2));

        // Different start values
        d2 = new DefaultIntervalCategoryDataset(s2, e2);
        d2.setStartValue(0, d2.getColumnKey(0), 999.0);
        assertFalse(d1.equals(d2));

        // Different end values
        d2 = new DefaultIntervalCategoryDataset(s2, e2);
        d2.setEndValue(0, d2.getColumnKey(0), 999.0);
        assertFalse(d1.equals(d2));

        // Clone independence
        DefaultIntervalCategoryDataset cloned = (DefaultIntervalCategoryDataset) d1.clone();
        assertTrue(d1.equals(cloned));
        assertNotSame(d1, cloned);

        cloned.setStartValue(0, cloned.getColumnKey(0), -999.0);
        assertFalse(d1.equals(cloned));
        assertEquals(1.0, d1.getStartValue(0, 0));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullData() {
        DefaultIntervalCategoryDataset d1 = new DefaultIntervalCategoryDataset((Number[][]) null, null);
        DefaultIntervalCategoryDataset d2 = new DefaultIntervalCategoryDataset((Number[][]) null, null);
        assertTrue(d1.equals(d2));

        Number[][] s = new Number[][] {{1.0}};
        Number[][] e = new Number[][] {{2.0}};
        DefaultIntervalCategoryDataset d3 = new DefaultIntervalCategoryDataset(s, e);
        assertFalse(d1.equals(d3));
        assertFalse(d3.equals(d1));
    }
}