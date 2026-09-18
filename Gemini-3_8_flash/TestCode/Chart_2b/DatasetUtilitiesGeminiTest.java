/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jfree.data.general.DatasetUtilities
 *
 * Branch Coverage & Decision Points:
 * 1. calculatePieDatasetTotal:
 *    - null dataset -> IllegalArgumentException
 *    - keys iterator with null key, key with null value, negative value, zero value, positive value
 * 2. createPieDatasetForRow / createPieDatasetForColumn:
 *    - CategoryDataset row index / column index mapping
 *    - CategoryDataset row key / column key lookup
 * 3. createConsolidatedPieDataset:
 *    - 3-arg and 4-arg overloads
 *    - values below / above minimumPercent threshold
 *    - item count meeting minItems threshold vs below minItems
 * 4. createCategoryDataset:
 *    - prefixes with double[][] and Number[][]
 *    - rowKeys, columnKeys, double[][] checks (null keys, duplicates, dimension mismatches)
 *    - KeyedValues to CategoryDataset (null rowKey, null rowData, valid mapping)
 * 5. sampleFunction2D / sampleFunction2DToSeries:
 *    - null f, null seriesKey -> IllegalArgumentException
 *    - start >= end -> IllegalArgumentException
 *    - samples < 2 -> IllegalArgumentException
 *    - normal function sampling
 * 6. isEmptyOrNull:
 *    - PieDataset: null, empty keys, all null or <=0 values, positive value
 *    - CategoryDataset: null, 0 rows, 0 cols, all null values, valid non-null value
 *    - XYDataset: null, 0 series, series with 0 items, series with > 0 items
 * 7. findDomainBounds / iterateDomainBounds:
 *    - null dataset -> IllegalArgumentException
 *    - DomainInfo vs non-DomainInfo
 *    - XYDomainInfo vs iterateToFindDomainBounds
 *    - IntervalXYDataset with startX/endX vs standard XYDataset with getXValue
 *    - empty or all NaN values -> returns null
 *    - visibleSeriesKeys filtering
 * 8. findRangeBounds / iterateRangeBounds:
 *    - CategoryDataset:
 *      * RangeInfo shortcut vs iterateRangeBounds
 *      * CategoryRangeInfo vs iterateToFindRangeBounds
 *      * IntervalCategoryDataset (testBug2849731: null values, startValue, endValue)
 *      * BoxAndWhiskerCategoryDataset
 *      * MultiValueCategoryDataset
 *      * StatisticalCategoryDataset (mean with null/NaN std dev)
 *    - XYDataset:
 *      * RangeInfo shortcut vs iterateRangeBounds
 *      * XYRangeInfo vs iterateToFindRangeBounds (with xRange filtering)
 *      * IntervalXYDataset (startY, endY)
 *      * OHLCDataset (high, low)
 *      * BoxAndWhiskerXYDataset (minRegular, maxRegular)
 *      * Standard XYDataset (y-value NaN vs valid)
 * 9. Stacked Bounds & Cumulative:
 *    - findStackedRangeBounds(CategoryDataset, base)
 *    - findStackedRangeBounds(CategoryDataset, KeyToGroupMap)
 *    - findMinimumStackedRangeValue / findMaximumStackedRangeValue
 *    - findStackedRangeBounds(TableXYDataset, base) & calculateStackTotal
 *    - findCumulativeRangeBounds: null handling, running totals, all nulls -> null
 *
 * Defects4J Bug Target:
 * - Bug 2849731: NPE in iterateRangeBounds / findRangeBounds when an IntervalCategoryDataset
 *   returns null for startValue or endValue.
 */

package org.jfree.data.general;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jfree.data.DomainInfo;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.KeyedValues;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.CategoryRangeInfo;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.function.Function2D;
import org.jfree.data.pie.DefaultPieDataset;
import org.jfree.data.pie.PieDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerXYDataset;
import org.jfree.data.statistics.DefaultMultiValueCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.MultiValueCategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.data.xy.DefaultIntervalXYDataset;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYDomainInfo;
import org.jfree.data.xy.XYRangeInfo;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class DatasetUtilitiesGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCalculatePieDatasetTotal() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", 25.5);
        dataset.setValue("C", 0.0);
        dataset.setValue("D", -5.0); // negative ignored
        dataset.setValue("E", null); // null ignored

        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        assertEquals(35.5, total, 1e-9);
    }

    @Test(timeout = 4000)
    public void testCreatePieDatasetForRowAndColumn() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R1", "C2");
        dataset.addValue(3.0, "R2", "C1");
        dataset.addValue(4.0, "R2", "C2");

        PieDataset pieRowKey = DatasetUtilities.createPieDatasetForRow(dataset, "R1");
        assertEquals(2, pieRowKey.getItemCount());
        assertEquals(1.0, pieRowKey.getValue("C1").doubleValue(), 1e-9);
        assertEquals(2.0, pieRowKey.getValue("C2").doubleValue(), 1e-9);

        PieDataset pieRowIdx = DatasetUtilities.createPieDatasetForRow(dataset, 1);
        assertEquals(3.0, pieRowIdx.getValue("C1").doubleValue(), 1e-9);
        assertEquals(4.0, pieRowIdx.getValue("C2").doubleValue(), 1e-9);

        PieDataset pieColKey = DatasetUtilities.createPieDatasetForColumn(dataset, "C2");
        assertEquals(2, pieColKey.getItemCount());
        assertEquals(2.0, pieColKey.getValue("R1").doubleValue(), 1e-9);
        assertEquals(4.0, pieColKey.getValue("R2").doubleValue(), 1e-9);

        PieDataset pieColIdx = DatasetUtilities.createPieDatasetForColumn(dataset, 0);
        assertEquals(1.0, pieColIdx.getValue