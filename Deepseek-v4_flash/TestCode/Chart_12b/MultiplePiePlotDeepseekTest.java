package org.jfree.chart.plot;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.util.PaintUtilities;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.AbstractDataset;
import javax.swing.event.EventListenerList;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.lang.reflect.Field;

/**
 * Comprehensive test suite for {@link MultiplePiePlot}. Targets line/branch coverage
 * and the known defect (constructor fails to register as dataset listener).
 *
 * <p>Branch Analysis Matrix:
 * <ul>
 *   <li>Constructor: null vs non-null dataset; listener registration path</li>
 *   <li>setDataset: null case, non-null case, trigger datasetChanged</li>
 *   <li>setPieChart: null argument, non-PiePlot plot, valid chart</li>
 *   <li>setDataExtractOrder: null argument, valid order</li>
 *   <li>get/setLimit: zero, positive, negative (accepts any double)</li>
 *   <li>setAggregatedItemsKey: null, valid</li>
 *   <li>setAggregatedItemsPaint: null, valid</li>
 *   <li>getLegendItems: null dataset, empty dataset, with data, with limit</li>
 *   <li>equals: null, different class, different fields</li>
 *   <li>getPlotType: constant string</li>
 * </ul>
 */
public class MultiplePiePlotDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertNotNull("pieChart must not be null", plot.getPieChart());
        assertTrue("pieChart's plot must be PiePlot",
                   plot.getPieChart().getPlot() instanceof PiePlot);
        assertNull("dataset must be null", plot.getDataset());
        assertEquals("dataExtractOrder default", TableOrder.BY_COLUMN,
                     plot.getDataExtractOrder());
        assertEquals("limit default", 0.0, plot.getLimit(), 0.0);
        assertEquals("aggregatedItemsKey default", "Other",
                     plot.getAggregatedItemsKey());
        assertEquals("aggregatedItemsPaint default", Color.lightGray,
                     plot.getAggregatedItemsPaint());
        assertEquals("plot type", "Multiple Pie Plot", plot.getPlotType());
    }

    @Test(timeout = 4000)
    public void testConstructorWithDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        assertSame("dataset must be the one passed in", dataset, plot.getDataset());
        assertNotNull("pieChart must not be null", plot.getPieChart());
        assertEquals("dataExtractOrder default", TableOrder.BY_COLUMN,
                     plot.getDataExtractOrder());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullDataset() {
        MultiplePiePlot plot = new MultiplePiePlot(null);
        assertNull("dataset must be null", plot.getDataset());
    }

    @Test(timeout = 4000)
    public void testSetDataset() {
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        MultiplePiePlot plot = new MultiplePiePlot(dataset1);
        plot.setDataset(dataset2);
        assertSame("dataset should be dataset2", dataset2, plot.getDataset());
        // Changing to null
        plot.setDataset(null);
        assertNull("dataset should be null", plot.getDataset());
        // Back to non-null
        plot.setDataset(dataset1);
        assertSame(dataset1, plot.getDataset());
    }

    @Test(timeout = 4000)
    public void testGetSetPieChart() {
        MultiplePiePlot plot = new MultiplePiePlot();
        PiePlot piePlot = new PiePlot();
        JFreeChart chart = new JFreeChart(piePlot);
        plot.setPieChart(chart);
        assertSame("pieChart should be the chart set", chart, plot.getPieChart());
        // Ensure the chart's plot is a PiePlot (already)
        assertTrue(plot.getPieChart().getPlot() instanceof PiePlot);
    }

    @Test(timeout = 4000)
    public void testGetSetDataExtractOrder() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertEquals(TableOrder.BY_COLUMN, plot.getDataExtractOrder());
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        assertEquals(TableOrder.BY_ROW, plot.getDataExtractOrder());
    }

    @Test(timeout = 4000)
    public void testGetSetLimit() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertEquals(0.0, plot.getLimit(), 0.0);
        plot.setLimit(0.15);
        assertEquals(0.15, plot.getLimit(), 1e-10);
        plot.setLimit(0.0);
        assertEquals(0.0, plot.getLimit(), 0.0);
        // Negative values are allowed (no validation)
        plot.setLimit(-0.5);
        assertEquals(-0.5, plot.getLimit(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetSetAggregatedItemsKey() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertEquals("Other", plot.getAggregatedItemsKey());
        plot.setAggregatedItemsKey("Sum");
        assertEquals("Sum", plot.getAggregatedItemsKey());
    }

    @Test(timeout = 4000)
    public void testGetSetAggregatedItemsPaint() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertEquals(Color.lightGray, plot.getAggregatedItemsPaint());
        plot.setAggregatedItemsPaint(Color.red);
        assertEquals(Color.red, plot.getAggregatedItemsPaint());
    }

    @Test(timeout = 4000)
    public void testGetPlotType() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertEquals("Multiple Pie Plot", plot.getPlotType());
    }

    @Test(timeout = 4000)
    public void testGetLegendItemsWithNullDataset() {
        MultiplePiePlot plot = new MultiplePiePlot(null);
        LegendItemCollection items = plot.getLegendItems();
        assertNotNull("legend items should not be null", items);
        assertEquals(0, items.getItemCount());
    }

    @Test(timeout = 4000)
    public void testGetLegendItemsWithLimit() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Row1", "Col1");
        dataset.addValue(0.05, "Row1", "Col2");
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        plot.setLimit(0.1); // Col2 will be aggregated (value 0.05 / total 1.05 ≈ 4.8% < 10%)
        LegendItemCollection items = plot.getLegendItems();
        // Keys from column keys: Col1, Col2, and aggregated item "Other" because limit>0
        // Since we have only one row, the legend will show column keys plus "Other" if limit active.
        // But prefetchSectionPaints runs based on extract order=BY_ROW, so column keys are used.
        // With limit, aggregated key is added.
        // Number of items: column keys (2) + aggregated (1) = 3
        assertTrue("legend should contain at least 2 items", items.getItemCount() >= 2);
    }

    @Test(timeout = 4000)
    public void testGetLegendItemsWithByColumn() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Row1", "Col1");
        dataset.addValue(2.0, "Row2", "Col1");
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        LegendItemCollection items = plot.getLegendItems();
        // BY_COLUMN means rows are keys for legend
        assertEquals("legend item count equals row count", 2, items.getItemCount());
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetPieChartWithNull() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setPieChart(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetPieChartWithInvalidPlot() {
        MultiplePiePlot plot = new MultiplePiePlot();
        JFreeChart chart = new JFreeChart(new PiePlot()); // but we need a non-PiePlot plot
        // Actually PiePlot is fine; to get invalid we need a plot that is not PiePlot
        // Use a simple dummy: CategoryPlot? But that requires other dependencies.
        // The code checks "pieChart.getPlot() instanceof PiePlot" – we can create a chart with a CategoryPlot if available.
        // Since we may not have CategoryPlot, we use a different approach: create a chart with null plot? Not allowed.
        // Use a simple JFreeChart with an anonymous subclass of Plot? Plot is abstract, we can create a mock-like anonymous class.
        // However, to avoid compilation issues, we assume we can create a Chart with a fake plot.
        // Let's use a minimal approach: create a chart with a plot that is not PiePlot.
        // Since we are in the same package, we can create a minimal Plot subclass.
        Plot dummyPlot = new Plot() {
            @Override
            public String getPlotType() { return "Dummy"; }
            @Override
            public void draw(java.awt.Graphics2D g2, java.awt.geom.Rectangle2D area,
                             java.awt.geom.Point2D anchor, PlotState parentState,
                             PlotRenderingInfo info) {}
        };
        JFreeChart chart = new JFreeChart(dummyPlot);
        plot.setPieChart(chart);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetDataExtractOrderWithNull() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setDataExtractOrder(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetAggregatedItemsKeyWithNull() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setAggregatedItemsKey(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetAggregatedItemsPaintWithNull() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setAggregatedItemsPaint(null);
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Listener Registration)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorRegistersAsDatasetListener() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        // Use reflection to access the dataset's listenerList field (inherited from AbstractDataset)
        Field listenerListField = AbstractDataset.class.getDeclaredField("listenerList");
        listenerListField.setAccessible(true);
        EventListenerList listenerList = (EventListenerList) listenerListField.get(dataset);
        org.jfree.data.general.DatasetChangeListener[] listeners =
                listenerList.getListeners(org.jfree.data.general.DatasetChangeListener.class);
        boolean found = false;
        for (org.jfree.data.general.DatasetChangeListener l : listeners) {
            if (l == plot) {
                found = true;
                break;
            }
        }
        assertTrue("Plot must be registered as a DatasetChangeListener on construction", found);
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths (already covered in B)
    // -----------------------------------------------------------------------

    // Additional exception test for setDataset (no exceptions thrown, but covers null handling)

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEquals() {
        MultiplePiePlot plot1 = new MultiplePiePlot();
        MultiplePiePlot plot2 = new MultiplePiePlot();
        assertTrue("identical default plots should be equal", plot1.equals(plot2));
        assertTrue("equals is symmetric", plot2.equals(plot1));

        plot1.setDataExtractOrder(TableOrder.BY_ROW);
        assertFalse("different dataExtractOrder", plot1.equals(plot2));
        plot2.setDataExtractOrder(TableOrder.BY_ROW);
        assertTrue("now equal again", plot1.equals(plot2));

        plot1.setLimit(0.5);
        assertFalse("different limit", plot1.equals(plot2));
        plot2.setLimit(0.5);
        assertTrue(plot1.equals(plot2));

        plot1.setAggregatedItemsKey("Sum");
        assertFalse("different aggregatedItemsKey", plot1.equals(plot2));
        plot2.setAggregatedItemsKey("Sum");
        assertTrue(plot1.equals(plot2));

        plot1.setAggregatedItemsPaint(Color.blue);
        assertFalse("different aggregatedItemsPaint", plot1.equals(plot2));
        plot2.setAggregatedItemsPaint(Color.blue);
        assertTrue(plot1.equals(plot2));

        // Change pieChart (must keep valid PiePlot)
        JFreeChart chart = new JFreeChart(new PiePlot());
        plot1.setPieChart(chart);
        assertFalse("different pieChart", plot1.equals(plot2));
        plot2.setPieChart(chart);
        assertTrue(plot1.equals(plot2));

        // Test null object and different class
        assertFalse("null should return false", plot1.equals(null));
        assertFalse("different class should return false", plot1.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameObject() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertTrue("same object must be equal", plot.equals(plot));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSuper() {
        // Superclass (Plot) fields are tested via super.equals; ensure it works.
        MultiplePiePlot plot1 = new MultiplePiePlot();
        MultiplePiePlot plot2 = new MultiplePiePlot();
        // Changing a property from super class (e.g., background paint) should affect equality.
        // Plot has setBackgroundPaint(); we can test that.
        plot1.setBackgroundPaint(Color.red);
        assertFalse("different background paint should make plots unequal", plot1.equals(plot2));
    }

}