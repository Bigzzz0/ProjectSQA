package org.jfree.data.category;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class DefaultIntervalCategoryDatasetDeepseekTest {

    /* [Branch & Defect Analysis Matrix] */
    // Target defect: getCategoryIndex() throws NullPointerException when category is null
    // instead of returning -1 (as documented in getColumnIndex/getRowIndex).
    // Also targets: setCategoryKeys() validation, getSeriesIndex() with null key,
    // getRowCount()/getColumnCount() on empty dataset, getRowIndex() with null key,
    // getColumnCount() on empty dataset, getColumnIndex() with null key, clone() deep copy.
    //
    // Branches targeted:
    // - Constructor: null starts/ends, mismatched series/category counts, null keys
    // - getSeriesCount(): null startData vs non-null
    // - getSeriesIndex(): null key, key not found, key found
    // - getCategoryIndex(): null category (defect), category not found, category found
    // - setCategoryKeys(): null array, wrong length, null element
    // - setStartValue/setEndValue: out-of-range series, invalid category
    // - getRowCount/getColumnCount: empty vs non-empty dataset
    // - getRowIndex/getColumnIndex: null key handling
    // - clone(): deep copy of arrays, independent modification
    // - equals(): null, same object, different type, different data

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testGetValueWithValidIndices() {
        Number[][] starts = {{1.0, 2.0}, {3.0, 4.0}};
        Number[][] ends = {{5.0, 6.0}, {7.0, 8.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Start value (0,0)", 1.0, dataset.getStartValue(0, 0));
        assertEquals("End value (0,0)", 5.0, dataset.getEndValue(0, 0));
        assertEquals("Value (0,0) should be end value", 5.0, dataset.getValue(0, 0));
        assertEquals("Start value (1,1)", 4.0, dataset.getStartValue(1, 1));
        assertEquals("End value (1,1)", 8.0, dataset.getValue(1, 1));
    }

    @Test(timeout = 4000)
    public void testGetValueWithComparableKeys() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        String[] seriesNames = {"S1"};
        String[] categoryNames = {"C1", "C2"};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(seriesNames, categoryNames, starts, ends);

        assertEquals("Start value by key", 1.0, dataset.getStartValue("S1", "C1"));
        assertEquals("End value by key", 4.0, dataset.getEndValue("S1", "C2"));
        assertEquals("Value by key", 3.0, dataset.getValue("S1", "C1"));
    }

    @Test(timeout = 4000)
    public void testSetStartAndEndValue() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        dataset.setStartValue(0, "Category 1", 10.0);
        dataset.setEndValue(0, "Category 1", 20.0);

        assertEquals("Updated start value", 10.0, dataset.getStartValue(0, 0));
        assertEquals("Updated end value", 20.0, dataset.getEndValue(0, 0));
    }

    @Test(timeout = 4000)
    public void testGetRowAndColumnKeys() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        List rowKeys = dataset.getRowKeys();
        List columnKeys = dataset.getColumnKeys();

        assertEquals("Row keys size", 1, rowKeys.size());
        assertEquals("Column keys size", 2, columnKeys.size());
        assertEquals("Row key 0", "Row 1", rowKeys.get(0));
        assertEquals("Column key 0", "Category 1", columnKeys.get(0));
        assertEquals("Column key 1", "Category 2", columnKeys.get(1));
    }

    @Test(timeout = 4000)
    public void testGetRowAndColumnKeyByIndex() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Row key 0", "Row 1", dataset.getRowKey(0));
        assertEquals("Column key 0", "Category 1", dataset.getColumnKey(0));
        assertEquals("Column key 1", "Category 2", dataset.getColumnKey(1));
    }

    @Test(timeout = 4000)
    public void testSetSeriesKeysAndCategoryKeys() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        Comparable[] newSeriesKeys = {"NewSeries"};
        Comparable[] newCategoryKeys = {"NewCat1", "NewCat2"};

        dataset.setSeriesKeys(newSeriesKeys);
        dataset.setCategoryKeys(newCategoryKeys);

        assertEquals("Updated series key", "NewSeries", dataset.getRowKey(0));
        assertEquals("Updated category key", "NewCat1", dataset.getColumnKey(0));
        assertEquals("Updated category key", "NewCat2", dataset.getColumnKey(1));
    }

    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        assertEquals("Series count should be 0", 0, dataset.getSeriesCount());
        assertEquals("Row count should be 0", 0, dataset.getRowCount());
        assertEquals("Column count should be 0", 0, dataset.getColumnCount());
        assertEquals("Category count should be 0", 0, dataset.getCategoryCount());
        assertTrue("Row keys should be empty", dataset.getRowKeys().isEmpty());
        assertTrue("Column keys should be empty", dataset.getColumnKeys().isEmpty());
    }

    @Test(timeout = 4000)
    public void testSingleElementDataset() {
        Number[][] starts = {{5.0}};
        Number[][] ends = {{10.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Series count", 1, dataset.getSeriesCount());
        assertEquals("Category count", 1, dataset.getCategoryCount());
        assertEquals("Start value", 5.0, dataset.getStartValue(0, 0));
        assertEquals("End value", 10.0, dataset.getEndValue(0, 0));
    }

    @Test(timeout = 4000)
    public void testNullValuesInData() {
        Number[][] starts = {{null, 2.0}};
        Number[][] ends = {{3.0, null}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertNull("Start value can be null", dataset.getStartValue(0, 0));
        assertNull("End value can be null", dataset.getEndValue(0, 1));
        assertEquals("Non-null start", 2.0, dataset.getStartValue(0, 1));
        assertEquals("Non-null end", 3.0, dataset.getEndValue(0, 0));
    }

    @Test(timeout = 4000)
    public void testLargeDatasetBoundary() {
        int size = 100;
        Number[][] starts = new Number[size][size];
        Number[][] ends = new Number[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                starts[i][j] = (double) i;
                ends[i][j] = (double) j;
            }
        }
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Series count", size, dataset.getSeriesCount());
        assertEquals("Category count", size, dataset.getCategoryCount());
        assertEquals("Last start value", (double) size - 1, dataset.getStartValue(size - 1, size - 1));
        assertEquals("Last end value", (double) size - 1, dataset.getEndValue(size - 1, size - 1));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Targets the known defect: getCategoryIndex(null) should return -1 (as per
     * CategoryDataset interface contract) but throws NullPointerException in the
     * defective version.
     */
    @Test(timeout = 4000)
    public void testGetCategoryIndexWithNullCategory() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        // Expected: -1 (not found) instead of NullPointerException
        assertEquals("Null category should return -1", -1, dataset.getCategoryIndex(null));
    }

    /**
     * Targets the known defect in setCategoryKeys: should validate null elements
     * in the array and throw IllegalArgumentException.
     */
    @Test(timeout = 4000)
    public void testSetCategoryKeysWithNullElement() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setCategoryKeys(new Comparable[]{"Valid", null});
            fail("Expected IllegalArgumentException for null category key");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Targets the known defect in getSeriesIndex: should handle null key gracefully
     * (return -1) instead of throwing NullPointerException.
     */
    @Test(timeout = 4000)
    public void testGetSeriesIndexWithNullKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Null series key should return -1", -1, dataset.getSeriesIndex(null));
    }

    /**
     * Targets the known defect in getRowCount: should return 0 for empty dataset
     * instead of throwing NullPointerException.
     */
    @Test(timeout = 4000)
    public void testGetRowCountOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        assertEquals("Row count should be 0", 0, dataset.getRowCount());
    }

    /**
     * Targets the known defect in getRowIndex: should handle null key gracefully
     * (return -1) instead of throwing NullPointerException.
     */
    @Test(timeout = 4000)
    public void testGetRowIndexWithNullKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Null row key should return -1", -1, dataset.getRowIndex(null));
    }

    /**
     * Targets the known defect in getColumnCount: should return 0 for empty dataset
     * instead of throwing NullPointerException.
     */
    @Test(timeout = 4000)
    public void testGetColumnCountOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        assertEquals("Column count should be 0", 0, dataset.getColumnCount());
    }

    /**
     * Targets the known defect in getColumnIndex: should handle null key gracefully
     * (return -1) instead of throwing NullPointerException.
     */
    @Test(timeout = 4000)
    public void testGetColumnIndexWithNullKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Null column key should return -1", -1, dataset.getColumnIndex(null));
    }

    /**
     * Targets the known defect in clone(): should perform deep copy of arrays
     * so that modifying the clone doesn't affect the original.
     */
    @Test(timeout = 4000)
    public void testCloneDeepCopy() throws CloneNotSupportedException {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        DefaultIntervalCategoryDataset cloned = (DefaultIntervalCategoryDataset) dataset.clone();

        // Modify the clone
        cloned.setStartValue(0, 0, 99.0);
        cloned.setEndValue(0, 1, 88.0);

        // Original should be unaffected
        assertEquals("Original start value unchanged", 1.0, dataset.getStartValue(0, 0));
        assertEquals("Original end value unchanged", 4.0, dataset.getEndValue(0, 1));
        assertEquals("Clone start value modified", 99.0, cloned.getStartValue(0, 0));
        assertEquals("Clone end value modified", 88.0, cloned.getEndValue(0, 1));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithMismatchedSeriesCount() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}, {3.0}};
        new DefaultIntervalCategoryDataset(starts, ends);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithMismatchedCategoryCount() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0}};
        new DefaultIntervalCategoryDataset(starts, ends);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithMismatchedSeriesKeys() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        new DefaultIntervalCategoryDataset(new Comparable[]{"S1", "S2"}, new Comparable[]{"C1"}, starts, ends);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithMismatchedCategoryKeys() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        new DefaultIntervalCategoryDataset(new Comparable[]{"S1"}, new Comparable[]{"C1", "C2"}, starts, ends);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetSeriesKeyWithInvalidIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getSeriesKey(1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetRowKeyWithInvalidIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getRowKey(1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetSeriesKeysWithNullArray() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setSeriesKeys(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetSeriesKeysWithWrongLength() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setSeriesKeys(new Comparable[]{"S1", "S2"});
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetCategoryKeysWithNullArray() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setCategoryKeys(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetCategoryKeysWithWrongLength() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setCategoryKeys(new Comparable[]{"C1"});
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetStartValueWithInvalidSeriesIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue(1, 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetStartValueWithInvalidCategoryIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getStartValue(0, 1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetStartValueWithInvalidSeriesIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setStartValue(1, "Category 1", 5.0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetStartValueWithInvalidCategory() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.setStartValue(0, "NonExistent", 5.0);
    }

    @Test(timeout = 4000, expected = UnknownKeyException.class)
    public void testGetValueWithUnknownSeriesKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getValue("UnknownSeries", "Category 1");
    }

    @Test(timeout = 4000, expected = UnknownKeyException.class)
    public void testGetValueWithUnknownCategoryKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getValue("Row 1", "UnknownCategory");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetColumnIndexWithNullKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);
        dataset.getColumnIndex(null);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEquals() {
        Number[][] starts1 = {{1.0, 2.0}};
        Number[][] ends1 = {{3.0, 4.0}};
        Number[][] starts2 = {{1.0, 2.0}};
        Number[][] ends2 = {{3.0, 4.0}};
        Number[][] starts3 = {{1.0, 2.0}};
        Number[][] ends3 = {{3.0, 5.0}};

        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset(starts1, ends1);
        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset(starts2, ends2);
        DefaultIntervalCategoryDataset dataset3 = new DefaultIntervalCategoryDataset(starts3, ends3);

        assertTrue("Same object should be equal", dataset1.equals(dataset1));
        assertTrue("Equal datasets should be equal", dataset1.equals(dataset2));
        assertFalse("Different data should not be equal", dataset1.equals(dataset3));
        assertFalse("Null should not be equal", dataset1.equals(null));
        assertFalse("Different type should not be equal", dataset1.equals("Not a dataset"));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentSeriesKeys() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset(
                new Comparable[]{"S1"}, new Comparable[]{"C1"}, starts, ends);
        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset(
                new Comparable[]{"S2"}, new Comparable[]{"C1"}, starts, ends);

        assertFalse("Different series keys should not be equal", dataset1.equals(dataset2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentCategoryKeys() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset(
                new Comparable[]{"S1"}, new Comparable[]{"C1"}, starts, ends);
        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset(
                new Comparable[]{"S1"}, new Comparable[]{"C2"}, starts, ends);

        assertFalse("Different category keys should not be equal", dataset1.equals(dataset2));
    }

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        Number[][] starts = {{1.0, 2.0}, {3.0, 4.0}};
        Number[][] ends = {{5.0, 6.0}, {7.0, 8.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        DefaultIntervalCategoryDataset cloned = (DefaultIntervalCategoryDataset) dataset.clone();

        assertNotSame("Clone should be different object", dataset, cloned);
        assertEquals("Clone should be equal to original", dataset, cloned);
        assertNotSame("Series keys array should be cloned", dataset.seriesKeys, cloned.seriesKeys);
        assertNotSame("Category keys array should be cloned", dataset.categoryKeys, cloned.categoryKeys);
        assertNotSame("Start data array should be cloned", dataset.startData, cloned.startData);
        assertNotSame("End data array should be cloned", dataset.endData, cloned.endData);
    }

    @Test(timeout = 4000)
    public void testGetSeriesCountWithNullData() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);
        assertEquals("Series count should be 0", 0, dataset.getSeriesCount());
    }

    @Test(timeout = 4000)
    public void testGetCategoryCountWithNullData() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);
        assertEquals("Category count should be 0", 0, dataset.getCategoryCount());
    }

    @Test(timeout = 4000)
    public void testGetRowKeysWithNullData() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);
        assertTrue("Row keys should be empty", dataset.getRowKeys().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetColumnKeysWithNullData() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);
        assertTrue("Column keys should be empty", dataset.getColumnKeys().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetColumnKeyWithValidIndex() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Column key 0", "Category 1", dataset.getColumnKey(0));
        assertEquals("Column key 1", "Category 2", dataset.getColumnKey(1));
    }

    @Test(timeout = 4000)
    public void testGetRowIndexWithValidKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Row index for Row 1", 0, dataset.getRowIndex("Row 1"));
    }

    @Test(timeout = 4000)
    public void testGetColumnIndexWithValidKey() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Column index for Category 1", 0, dataset.getColumnIndex("Category 1"));
        assertEquals("Column index for Category 2", 1, dataset.getColumnIndex("Category 2"));
    }

    @Test(timeout = 4000)
    public void testGetSeriesIndexWithValidKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Series index for Row 1", 0, dataset.getSeriesIndex("Row 1"));
    }

    @Test(timeout = 4000)
    public void testGetCategoryIndexWithValidKey() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Category index for Category 1", 0, dataset.getCategoryIndex("Category 1"));
        assertEquals("Category index for Category 2", 1, dataset.getCategoryIndex("Category 2"));
    }

    @Test(timeout = 4000)
    public void testGetCategoryIndexWithUnknownKey() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Unknown category should return -1", -1, dataset.getCategoryIndex("Unknown"));
    }

    @Test(timeout = 4000)
    public void testGetSeriesIndexWithUnknownKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Unknown series should return -1", -1, dataset.getSeriesIndex("Unknown"));
    }

    @Test(timeout = 4000)
    public void testGetRowIndexWithUnknownKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Unknown row should return -1", -1, dataset.getRowIndex("Unknown"));
    }

    @Test(timeout = 4000)
    public void testGetColumnIndexWithUnknownKey() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Unknown column should return -1", -1, dataset.getColumnIndex("Unknown"));
    }

    @Test(timeout = 4000)
    public void testGetValueWithUnknownSeriesKeyReturnsUnknownKeyException() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getValue("Unknown", "Category 1");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetValueWithUnknownCategoryKeyReturnsUnknownKeyException() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getValue("Row 1", "Unknown");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetStartValueWithUnknownSeriesKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getStartValue("Unknown", "Category 1");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetEndValueWithUnknownSeriesKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getEndValue("Unknown", "Category 1");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetStartValueWithUnknownCategoryKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getStartValue("Row 1", "Unknown");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetEndValueWithUnknownCategoryKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getEndValue("Row 1", "Unknown");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetEndValueWithInvalidSeriesIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setEndValue(1, "Category 1", 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetEndValueWithInvalidCategory() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setEndValue(0, "Unknown", 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetColumnCountWithData() {
        Number[][] starts = {{1.0, 2.0, 3.0}};
        Number[][] ends = {{4.0, 5.0, 6.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Column count", 3, dataset.getColumnCount());
    }

    @Test(timeout = 4000)
    public void testGetRowCountWithData() {
        Number[][] starts = {{1.0}, {2.0}, {3.0}};
        Number[][] ends = {{4.0}, {5.0}, {6.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Row count", 3, dataset.getRowCount());
    }

    @Test(timeout = 4000)
    public void testGetSeriesCountWithData() {
        Number[][] starts = {{1.0}, {2.0}};
        Number[][] ends = {{3.0}, {4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Series count", 2, dataset.getSeriesCount());
    }

    @Test(timeout = 4000)
    public void testGetCategoryCountWithData() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Category count", 2, dataset.getCategoryCount());
    }

    @Test(timeout = 4000)
    public void testGetRowKeysWithData() {
        Number[][] starts = {{1.0}, {2.0}};
        Number[][] ends = {{3.0}, {4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        List rowKeys = dataset.getRowKeys();
        assertEquals("Row keys size", 2, rowKeys.size());
        assertEquals("Row key 0", "Row 1", rowKeys.get(0));
        assertEquals("Row key 1", "Row 2", rowKeys.get(1));
    }

    @Test(timeout = 4000)
    public void testGetColumnKeysWithData() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        List columnKeys = dataset.getColumnKeys();
        assertEquals("Column keys size", 2, columnKeys.size());
        assertEquals("Column key 0", "Category 1", columnKeys.get(0));
        assertEquals("Column key 1", "Category 2", columnKeys.get(1));
    }

    @Test(timeout = 4000)
    public void testGetRowKeyWithValidIndex() {
        Number[][] starts = {{1.0}, {2.0}};
        Number[][] ends = {{3.0}, {4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Row key 0", "Row 1", dataset.getRowKey(0));
        assertEquals("Row key 1", "Row 2", dataset.getRowKey(1));
    }

    @Test(timeout = 4000)
    public void testGetSeriesKeyWithValidIndex() {
        Number[][] starts = {{1.0}, {2.0}};
        Number[][] ends = {{3.0}, {4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Series key 0", "Row 1", dataset.getSeriesKey(0));
        assertEquals("Series key 1", "Row 2", dataset.getSeriesKey(1));
    }

    @Test(timeout = 4000)
    public void testGetColumnKeyWithValidIndex2() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Column key 0", "Category 1", dataset.getColumnKey(0));
        assertEquals("Column key 1", "Category 2", dataset.getColumnKey(1));
    }

    @Test(timeout = 4000)
    public void testGetRowIndexWithValidKey2() {
        Number[][] starts = {{1.0}, {2.0}};
        Number[][] ends = {{3.0}, {4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Row index for Row 1", 0, dataset.getRowIndex("Row 1"));
        assertEquals("Row index for Row 2", 1, dataset.getRowIndex("Row 2"));
    }

    @Test(timeout = 4000)
    public void testGetColumnIndexWithValidKey2() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Column index for Category 1", 0, dataset.getColumnIndex("Category 1"));
        assertEquals("Column index for Category 2", 1, dataset.getColumnIndex("Category 2"));
    }

    @Test(timeout = 4000)
    public void testGetSeriesIndexWithValidKey2() {
        Number[][] starts = {{1.0}, {2.0}};
        Number[][] ends = {{3.0}, {4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Series index for Row 1", 0, dataset.getSeriesIndex("Row 1"));
        assertEquals("Series index for Row 2", 1, dataset.getSeriesIndex("Row 2"));
    }

    @Test(timeout = 4000)
    public void testGetCategoryIndexWithValidKey2() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Category index for Category 1", 0, dataset.getCategoryIndex("Category 1"));
        assertEquals("Category index for Category 2", 1, dataset.getCategoryIndex("Category 2"));
    }

    @Test(timeout = 4000)
    public void testGetValueWithValidKeys() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Value for Row 1, Category 1", 3.0, dataset.getValue("Row 1", "Category 1"));
        assertEquals("Value for Row 1, Category 2", 4.0, dataset.getValue("Row 1", "Category 2"));
    }

    @Test(timeout = 4000)
    public void testGetStartValueWithValidKeys() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Start value for Row 1, Category 1", 1.0, dataset.getStartValue("Row 1", "Category 1"));
        assertEquals("Start value for Row 1, Category 2", 2.0, dataset.getStartValue("Row 1", "Category 2"));
    }

    @Test(timeout = 4000)
    public void testGetEndValueWithValidKeys() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("End value for Row 1, Category 1", 3.0, dataset.getEndValue("Row 1", "Category 1"));
        assertEquals("End value for Row 1, Category 2", 4.0, dataset.getEndValue("Row 1", "Category 2"));
    }

    @Test(timeout = 4000)
    public void testSetStartValueWithValidKeys() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        dataset.setStartValue(0, "Category 1", 10.0);
        assertEquals("Updated start value", 10.0, dataset.getStartValue(0, 0));
    }

    @Test(timeout = 4000)
    public void testSetEndValueWithValidKeys() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        dataset.setEndValue(0, "Category 2", 20.0);
        assertEquals("Updated end value", 20.0, dataset.getEndValue(0, 1));
    }

    @Test(timeout = 4000)
    public void testSetSeriesKeysWithValidKeys() {
        Number[][] starts = {{1.0}, {2.0}};
        Number[][] ends = {{3.0}, {4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        dataset.setSeriesKeys(new Comparable[]{"NewRow1", "NewRow2"});
        assertEquals("Updated series key 0", "NewRow1", dataset.getRowKey(0));
        assertEquals("Updated series key 1", "NewRow2", dataset.getRowKey(1));
    }

    @Test(timeout = 4000)
    public void testSetCategoryKeysWithValidKeys() {
        Number[][] starts = {{1.0, 2.0}};
        Number[][] ends = {{3.0, 4.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        dataset.setCategoryKeys(new Comparable[]{"NewCat1", "NewCat2"});
        assertEquals("Updated category key 0", "NewCat1", dataset.getColumnKey(0));
        assertEquals("Updated category key 1", "NewCat2", dataset.getColumnKey(1));
    }

    @Test(timeout = 4000)
    public void testGetColumnIndexWithNullKeyThrowsException() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getColumnIndex(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetRowIndexWithNullKeyReturnsMinusOne() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Null row key should return -1", -1, dataset.getRowIndex(null));
    }

    @Test(timeout = 4000)
    public void testGetSeriesIndexWithNullKeyReturnsMinusOne() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Null series key should return -1", -1, dataset.getSeriesIndex(null));
    }

    @Test(timeout = 4000)
    public void testGetCategoryIndexWithNullKeyReturnsMinusOne() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Null category key should return -1", -1, dataset.getCategoryIndex(null));
    }

    @Test(timeout = 4000)
    public void testGetValueWithNullSeriesKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getValue(null, "Category 1");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetValueWithNullCategoryKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getValue("Row 1", null);
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetStartValueWithNullSeriesKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getStartValue(null, "Category 1");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetStartValueWithNullCategoryKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getStartValue("Row 1", null);
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetEndValueWithNullSeriesKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getEndValue(null, "Category 1");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetEndValueWithNullCategoryKey() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getEndValue("Row 1", null);
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetStartValueWithNullCategory() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setStartValue(0, null, 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetEndValueWithNullCategory() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setEndValue(0, null, 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetStartValueWithNullValue() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        dataset.setStartValue(0, "Category 1", null);
        assertNull("Start value should be null", dataset.getStartValue(0, 0));
    }

    @Test(timeout = 4000)
    public void testSetEndValueWithNullValue() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        dataset.setEndValue(0, "Category 1", null);
        assertNull("End value should be null", dataset.getEndValue(0, 0));
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullSeriesKeys() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(null, new Comparable[]{"C1"}, starts, ends);

        assertEquals("Series key should be generated", "Row 1", dataset.getRowKey(0));
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullCategoryKeys() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(new Comparable[]{"S1"}, null, starts, ends);

        assertEquals("Category key should be generated", "Category 1", dataset.getColumnKey(0));
    }

    @Test(timeout = 4000)
    public void testConstructorWithBothNullKeys() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(null, null, starts, ends);

        assertEquals("Series key should be generated", "Row 1", dataset.getRowKey(0));
        assertEquals("Category key should be generated", "Category 1", dataset.getColumnKey(0));
    }

    @Test(timeout = 4000)
    public void testGetRowKeysReturnsUnmodifiableList() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        List rowKeys = dataset.getRowKeys();
        try {
            rowKeys.add("NewRow");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetColumnKeysReturnsUnmodifiableList() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        List columnKeys = dataset.getColumnKeys();
        try {
            columnKeys.add("NewCategory");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetRowKeysWithNullDataReturnsEmptyList() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        List rowKeys = dataset.getRowKeys();
        assertTrue("Row keys should be empty", rowKeys.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetColumnKeysWithNullDataReturnsEmptyList() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        List columnKeys = dataset.getColumnKeys();
        assertTrue("Column keys should be empty", columnKeys.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetRowKeyWithNegativeIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getRowKey(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetColumnKeyWithNegativeIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getColumnKey(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSeriesKeyWithNegativeIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getSeriesKey(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetRowKeyWithOutOfBoundsIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getRowKey(1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetColumnKeyWithOutOfBoundsIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getColumnKey(1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSeriesKeyWithOutOfBoundsIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getSeriesKey(1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetStartValueWithNegativeSeriesIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getStartValue(-1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetStartValueWithNegativeCategoryIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getStartValue(0, -1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetEndValueWithNegativeSeriesIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getEndValue(-1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetEndValueWithNegativeCategoryIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getEndValue(0, -1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetStartValueWithOutOfBoundsSeriesIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getStartValue(1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetStartValueWithOutOfBoundsCategoryIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getStartValue(0, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetEndValueWithOutOfBoundsSeriesIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getEndValue(1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetEndValueWithOutOfBoundsCategoryIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getEndValue(0, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetStartValueWithNegativeSeriesIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setStartValue(-1, "Category 1", 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetEndValueWithNegativeSeriesIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setEndValue(-1, "Category 1", 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetStartValueWithOutOfBoundsSeriesIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setStartValue(1, "Category 1", 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetEndValueWithOutOfBoundsSeriesIndex() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setEndValue(1, "Category 1", 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetStartValueWithUnknownCategory() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setStartValue(0, "Unknown", 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetEndValueWithUnknownCategory() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setEndValue(0, "Unknown", 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetSeriesKeysWithNullElement() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setSeriesKeys(new Comparable[]{null});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetCategoryKeysWithNullElement() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.setCategoryKeys(new Comparable[]{null});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullElementInSeriesKeys() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        try {
            new DefaultIntervalCategoryDataset(new Comparable[]{null}, new Comparable[]{"C1"}, starts, ends);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullElementInCategoryKeys() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        try {
            new DefaultIntervalCategoryDataset(new Comparable[]{"S1"}, new Comparable[]{null}, starts, ends);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullStartData() {
        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);
        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        assertTrue("Both null start data should be equal", dataset1.equals(dataset2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullEndData() {
        Number[][] starts = {{1.0}};
        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset(starts, null);
        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset(starts, null);

        assertTrue("Both null end data should be equal", dataset1.equals(dataset2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentStartData() {
        Number[][] starts1 = {{1.0}};
        Number[][] starts2 = {{2.0}};
        Number[][] ends = {{3.0}};
        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset(starts1, ends);
        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset(starts2, ends);

        assertFalse("Different start data should not be equal", dataset1.equals(dataset2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentEndData() {
        Number[][] starts = {{1.0}};
        Number[][] ends1 = {{2.0}};
        Number[][] ends2 = {{3.0}};
        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset(starts, ends1);
        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset(starts, ends2);

        assertFalse("Different end data should not be equal", dataset1.equals(dataset2));
    }

    @Test(timeout = 4000)
    public void testCloneWithNullData() throws CloneNotSupportedException {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);
        DefaultIntervalCategoryDataset clone = (DefaultIntervalCategoryDataset) dataset.clone();

        assertNotSame("Clone should be different", dataset, clone);
        assertEquals("Clone should be equal", dataset, clone);
    }

    @Test(timeout = 4000)
    public void testGetValueWithNullSeriesAndCategory() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getValue(null, null);
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetStartValueWithNullSeriesAndCategory() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getStartValue(null, null);
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetEndValueWithNullSeriesAndCategory() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        try {
            dataset.getEndValue(null, null);
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetRowIndexWithNullKeyOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        assertEquals("Null row key on empty dataset should return -1", -1, dataset.getRowIndex(null));
    }

    @Test(timeout = 4000)
    public void testGetColumnIndexWithNullKeyOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.getColumnIndex(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSeriesIndexWithNullKeyOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        assertEquals("Null series key on empty dataset should return -1", -1, dataset.getSeriesIndex(null));
    }

    @Test(timeout = 4000)
    public void testGetCategoryIndexWithNullKeyOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        assertEquals("Null category key on empty dataset should return -1", -1, dataset.getCategoryIndex(null));
    }

    @Test(timeout = 4000)
    public void testGetRowKeyOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.getRowKey(0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetColumnKeyOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.getColumnKey(0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSeriesKeyOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.getSeriesKey(0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetStartValueOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.getStartValue(0, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetEndValueOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.getEndValue(0, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetStartValueOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.setStartValue(0, "Category 1", 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetEndValueOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.setEndValue(0, "Category 1", 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetSeriesKeysOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.setSeriesKeys(new Comparable[]{"S1"});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetCategoryKeysOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.setCategoryKeys(new Comparable[]{"C1"});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetRowIndexOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        assertEquals("Row index on empty dataset should be -1", -1, dataset.getRowIndex("Row 1"));
    }

    @Test(timeout = 4000)
    public void testGetColumnIndexOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.getColumnIndex("Category 1");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSeriesIndexOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        assertEquals("Series index on empty dataset should be -1", -1, dataset.getSeriesIndex("Row 1"));
    }

    @Test(timeout = 4000)
    public void testGetCategoryIndexOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        assertEquals("Category index on empty dataset should be -1", -1, dataset.getCategoryIndex("Category 1"));
    }

    @Test(timeout = 4000)
    public void testGetValueOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.getValue("Row 1", "Category 1");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetStartValueOnEmptyDatasetWithKeys() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.getStartValue("Row 1", "Category 1");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetEndValueOnEmptyDatasetWithKeys() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        try {
            dataset.getEndValue("Row 1", "Category 1");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetRowKeysOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        List rowKeys = dataset.getRowKeys();
        assertTrue("Row keys should be empty", rowKeys.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetColumnKeysOnEmptyDataset() {
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset((double[][]) null, (double[][]) null);

        List columnKeys = dataset.getColumnKeys();
        assertTrue("Column keys should be empty", columnKeys.isEmpty());
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentSeriesKeys() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset(
                new Comparable[]{"S1"}, new Comparable[]{"C1"}, starts, ends);
        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset(
                new Comparable[]{"S2"}, new Comparable[]{"C1"}, starts, ends);

        assertFalse("Different series keys should not be equal", dataset1.equals(dataset2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentCategoryKeys() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset(
                new Comparable[]{"S1"}, new Comparable[]{"C1"}, starts, ends);
        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset(
                new Comparable[]{"S1"}, new Comparable[]{"C2"}, starts, ends);

        assertFalse("Different category keys should not be equal", dataset1.equals(dataset2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameDataAndKeys() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset(
                new Comparable[]{"S1"}, new Comparable[]{"C1"}, starts, ends);
        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset(
                new Comparable[]{"S1"}, new Comparable[]{"C1"}, starts, ends);

        assertTrue("Same data and keys should be equal", dataset1.equals(dataset2));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset(starts, ends);
        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset(starts, ends);

        assertEquals("Equal objects should have same hash code", dataset1.hashCode(), dataset2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Number[][] starts = {{1.0}};
        Number[][] ends = {{2.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(starts, ends);

        assertNotNull("toString should not be null", dataset.toString());
        assertTrue("toString should contain class name", dataset.toString().contains("DefaultIntervalCategoryDataset"));
    }
}