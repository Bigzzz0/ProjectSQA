package org.jfree.data.statistics;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jfree.data.Range;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

/* [Branch & Defect Analysis Matrix]
 *
 * TARGET CLASS:
 *   org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
 *
 * DEFECT TARGETED (from Defects4J ground truth):
 *   - Bug: In add(BoxAndWhiskerItem, Comparable, Comparable), when replacing or updating an
 *     item at the cell holding the cached maximum or minimum range bounds, updateBounds()
 *     resets minimumRangeValue and maximumRangeValue to Double.NaN without re-scanning the
 *     entire 2D table. The bounds are then only populated using the newly added item, causing
 *     the dataset to discard the valid minimum/maximum from existing untouched cells.
 *   - Test Vector: testGetRangeBounds() fails with:
 *     "expected:<Range[8.5,9.6]> but was:<Range[8.6,9.6]>" when cell with max is updated.
 *
 * BRANCH COVERAGE ANALYSIS MATRIX:
 *   1. DefaultBoxAndWhiskerCategoryDataset()
 *      - Verify initial state: NaN bounds, Range(0,0), 0 rows, 0 columns.
 *   2. add(List, Comparable, Comparable)
 *      - Delegates to BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(list).
 *   3. add(BoxAndWhiskerItem, Comparable, Comparable)
 *      - Branch: (maxRow == r && maxCol == c) || (minRow == r && minCol == c) -> updateBounds()
 *      - Branch: item.getMinOutlier() != null vs null
 *      - Branch: item.getMaxOutlier() != null vs null
 *      - Branch: Double.isNaN(this.maximumRangeValue) -> true / false
 *      - Branch: maxval > this.maximumRangeValue -> true / false
 *      - Branch: Double.isNaN(this.minimumRangeValue) -> true / false
 *      - Branch: minval < this.minimumRangeValue -> true / false
 *      - Notification: DatasetChangeEvent fired upon addition.
 *   4. Item and Value Getters (by index and by Comparable keys):
 *      - getItem(int, int)
 *      - getValue(int, int), getValue(Comparable, Comparable)
 *      - getMeanValue(int, int), getMeanValue(Comparable, Comparable)
 *      - getMedianValue(int, int), getMedianValue(Comparable, Comparable)
 *      - getQ1Value(int, int), getQ1Value(Comparable, Comparable)
 *      - getQ3Value(int, int), getQ3Value(Comparable, Comparable)
 *      - getMinRegularValue(int, int), getMinRegularValue(Comparable, Comparable)
 *      - getMaxRegularValue(int, int), getMaxRegularValue(Comparable, Comparable)
 *      - getMinOutlier(int, int), getMinOutlier(Comparable, Comparable)
 *      - getMaxOutlier(int, int), getMaxOutlier(Comparable, Comparable)
 *      - getOutliers(int, int), getOutliers(Comparable, Comparable)
 *      - For each getter: Branch item != null vs item == null (sparse table lookup).
 *   5. Dimension & Key Query Methods:
 *      - getRowCount(), getColumnCount(), getRowKeys(), getColumnKeys()
 *      - getRowKey(int), getColumnKey(int), getRowIndex(Comparable), getColumnIndex(Comparable)
 *   6. RangeInfo Contract:
 *      - getRangeLowerBound(boolean), getRangeUpperBound(boolean), getRangeBounds(boolean)
 *   7. Object Lifecycle & Integrity:
 *      - equals(Object): this == obj, !(obj instanceof Dataset), identical, distinct datasets
 *      - clone(): deep copy of internal KeyedObjects2D structure
 *      - Serializable: round-trip serialization preservation
 */
public class DefaultBoxAndWhiskerCategoryDatasetGeminiTest {

    // Helper method to construct standard BoxAndWhiskerItem instances
    private BoxAndWhiskerItem createItem(double mean, double median, double q1, double q3,
                                         double minRegular, double maxRegular,
                                         Double minOutlier, Double maxOutlier,
                                         List<Double> outliers) {
        return new BoxAndWhiskerItem(
                Double.valueOf(mean),
                Double.valueOf(median),
                Double.valueOf(q1),
                Double.valueOf(q3),
                Double.valueOf(minRegular),
                Double.valueOf(maxRegular),
                minOutlier,
                maxOutlier,
                outliers != null ? outliers : Collections.emptyList()
        );
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialEmptyDatasetState() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

        assertEquals(0, dataset.getRowCount());
        assertEquals(0, dataset.getColumnCount());
        assertTrue(dataset.getRowKeys().isEmpty());
        assertTrue(dataset.getColumnKeys().isEmpty());
        assertTrue(Double.isNaN(dataset.getRangeLowerBound(true)));
        assertTrue(Double.isNaN(dataset.getRangeUpperBound(true)));
        assertEquals(new Range(0.0, 0.0), dataset.getRangeBounds(true));
        assertEquals(new Range(0.0, 0.0), dataset.getRangeBounds(false));
    }

    @Test(timeout = 4000)
    public void testAddAndRetrieveSingleItem() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        List<Double> outliers = Arrays.asList(0.5, 12.0);
        BoxAndWhiskerItem item = createItem(5.0, 4.5, 3.0, 6.0, 2.0, 8.0, 0.5, 12.0, outliers);

        dataset.add(item, "Row1", "Col1");

        assertEquals(1, dataset.getRowCount());
        assertEquals(1, dataset.getColumnCount());
        assertEquals("Row1", dataset.getRowKey(0));
        assertEquals("Col1", dataset.getColumnKey(0));
        assertEquals(0, dataset.getRowIndex("Row1"));
        assertEquals(0, dataset.getColumnIndex("Col1"));

        assertSame(item, dataset.getItem(0, 0));
        assertEquals(Double.valueOf(4.5), dataset.getValue(0, 0));
        assertEquals(Double.valueOf(4.5), dataset.getValue("Row1", "Col1"));
        assertEquals(Double.valueOf(5.0), dataset.getMeanValue(0, 0));
        assertEquals(Double.valueOf(5.0), dataset.getMeanValue("Row1", "Col1"));
        assertEquals(Double.valueOf(4.5), dataset.getMedianValue(0, 0));
        assertEquals(Double.valueOf(4.5), dataset.getMedianValue("Row1", "Col1"));
        assertEquals(Double.valueOf(3.0), dataset.getQ1Value(0, 0));
        assertEquals(Double.valueOf(3.0), dataset.getQ1Value("Row1", "Col1"));
        assertEquals(Double.valueOf(6.0), dataset.getQ3Value(0, 0));
        assertEquals(Double.valueOf(6.0), dataset.getQ3Value("Row1", "Col1"));
        assertEquals(Double.valueOf(2.0), dataset.getMinRegularValue(0, 0));
        assertEquals(Double.valueOf(2.0), dataset.getMinRegularValue("Row1", "Col1"));
        assertEquals(Double.valueOf(8.0), dataset.getMaxRegularValue(0, 0));
        assertEquals(Double.valueOf(8.0), dataset.getMaxRegularValue("Row1", "Col1"));
        assertEquals(Double.valueOf(0.5), dataset.getMinOutlier(0, 0));
        assertEquals(Double.valueOf(0.5), dataset.getMinOutlier("Row1", "Col1"));
        assertEquals(Double.valueOf(12.0),