package org.jfree.chart.plot;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Branch / Condition / Defect Target                | Method / Execution Path Tested
 * ====================================================================================================
 * DEFECT TARGET: Constructor Listener Registration | testConstructorRegistersDatasetListener
 *   In Defects4J (patch 1943021), MultiplePiePlot   | When MultiplePiePlot(CategoryDataset) is called,
 *   constructor assigned this.dataset = dataset     | the plot must register as a change listener to the
 *   directly without registering as a listener or   | dataset so that dataset modifications trigger
 *   calling setDataset(). Changing dataset did not  | PlotChangeEvent notifications to registered listeners.
 *   trigger plot events.                            |
 * --------------------------------------------------+-------------------------------------------------
 * Partition A: Core Logic & State Transitions       |
 * - Default constructor state initialization        | testDefaultConstructor
 * - Dataset accessors & listener handoff            | testSetDatasetHandoffAndEvents
 * - TableOrder (BY_COLUMN, BY_ROW)                  | testDataExtractOrder
 * - Limit & AggregatedItemsKey / Paint              | testLimitAndAggregatedItemsProperties
 * - PieChart getter/setter                          | testSetPieChartValid
 * - Plot type string query                          | testGetPlotType
 * --------------------------------------------------+-------------------------------------------------
 * Partition B: Boundary Value Analysis & Layout     |
 * - Draw with null / empty dataset (no-data branch) | testDrawEmptyOrNullDataset
 * - Draw BY_COLUMN and BY_ROW layouts               | testDrawByColumnAndByRow
 * - Dimension flip: displayCols > displayRows and   | testDrawLayoutDimensionFlipAndOffset
 *   area.width < area.height, diff != 0 offset calc |
 * - Aggregated items consolidated dataset (limit>0) | testDrawWithAggregationLimit
 * - Prefetch section paints caching and fallback    | testPrefetchSectionPaintsResolution
 * - Legend generation (BY_COLUMN, BY_ROW, limit>0)  | testGetLegendItemsVariations
 * --------------------------------------------------+-------------------------------------------------
 * Partition C: Defensive & Exception Guard Paths    |
 * - setPieChart(null) -> IllegalArgumentException   | testSetPieChartNullThrowsException
 * - setPieChart(non-PiePlot) -> IllegalArgument     | testSetPieChartNonPiePlotThrowsException
 * - setDataExtractOrder(null) -> IllegalArgument    | testSetDataExtractOrderNullThrowsException
 * - setAggregatedItemsKey(null) -> IllegalArgument  | testSetAggregatedItemsKeyNullThrowsException
 * - setAggregatedItemsPaint(null) -> IllegalArgument| testSetAggregatedItemsPaintNullThrowsException
 * --------------------------------------------------+-------------------------------------------------
 * Partition D: Object Lifecycle & Contract Integrity|
 * - equals() contract (symmetry, null, types)       | testEqualsContract
 * - equals() sensitivity to fields (order, limit,   | testEqualsFieldDifferences
 *   key, paint, pieChart, super fields)             |
 * - Serialization & transient map reconstruction   | testSerializationIntegrity
 * - Cloneable contract                              | testCloningContract
 * ====================================================================================================
 */

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Test;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

public class MultiplePiePlotGeminiTest {

    // Helper recording listener to assert PlotChangeEvent propagation
    private static class RecordingPlotChangeListener implements PlotChangeListener {
        private int notificationCount = 0;
        private PlotChangeEvent lastEvent = null;

        @Override
        public void plotChanged(PlotChangeEvent event) {
            this.notificationCount++;
            this.lastEvent = event;
        }

        public int getNotificationCount() {
            return this.notificationCount;
        }

        public boolean isNotified() {
            return this.notificationCount > 0;
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Ground Truth Tests
    // =========================================================================

    /**
     * Targets Defects4J bug where MultiplePiePlot(dataset) failed to register
     * the plot as a change listener to the CategoryDataset argument.
     */
    @Test(timeout = 4000)
    public void testConstructorRegistersDatasetListener() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");

        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        assertEquals(dataset, plot.getDataset());

        RecordingPlotChangeListener listener = new RecordingPlotChangeListener();
        plot.addChangeListener(listener);

        // Modifying dataset must trigger plot change event if constructor properly registered listener
        dataset.addValue(20.0, "R1", "C2");
        assertTrue("Defect check: MultiplePiePlot(dataset) must register itself as a dataset listener",
                listener.isNotified());
        assertEquals("Plot should have received exactly 1 change notification", 1, listener.getNotificationCount());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertNull(plot.getDataset());
        assertNotNull(plot.getPieChart());
        assertTrue(plot.getPieChart().getPlot() instanceof PiePlot);
        assertEquals(TableOrder.BY_COLUMN, plot.getDataExtractOrder());
        assertEquals(0.0, plot.getLimit(), 0.000001);
        assertEquals("Other", plot.getAggregatedItemsKey());
        assertEquals(Color.lightGray, plot.getAggregatedItemsPaint());
        assertEquals("Multiple Pie Plot", plot.getPlotType());
    }

    @Test(timeout = 4000)
    public void testSetDatasetHandoffAndEvents() {
        MultiplePiePlot plot = new MultiplePiePlot();
        RecordingPlotChangeListener listener = new RecordingPlotChangeListener();
        plot.addChangeListener(listener);

        DefaultCategoryDataset ds1 = new DefaultCategoryDataset();
        ds1.addValue(1.0, "R1", "C1");

        plot.setDataset(ds1);
        assertEquals(ds1, plot.getDataset());
        assertTrue("Setting dataset must fire plot change event", listener.isNotified());

        int countAfterFirstSet = listener.getNotificationCount();
        ds1.addValue(2.0, "R1", "C2");
        assertEquals("Dataset change must propagate to plot", countAfterFirstSet + 1, listener.getNotificationCount());

        // Handoff to second dataset
        DefaultCategoryDataset ds2 = new DefaultCategoryDataset();
        ds2.addValue(10.0, "R2", "C1");
        plot.setDataset(ds2);
        assertEquals(ds2, plot.getDataset());

        int countAfterHandoff = listener.getNotificationCount();
        // Modify old dataset - plot should NOT receive events from it anymore
        ds1.addValue(3.0, "R1", "C3");
        assertEquals("Plot must unregister from old dataset", countAfterHandoff, listener.getNotificationCount());

        // Modify new dataset - plot should receive event
        ds2.addValue(20.0, "R2", "C2");
        assertEquals("Plot must receive events from new dataset", countAfterHandoff + 1, listener.getNotificationCount());

        // Set to null
        plot.setDataset(null);
        assertNull(plot.getDataset());
    }

    @Test(timeout = 4000)
    public void testDataExtractOrder() {
        MultiplePiePlot plot = new MultiplePiePlot();
        RecordingPlotChangeListener listener = new RecordingPlotChangeListener();
        plot.addChangeListener(listener);

        plot.setDataExtractOrder(TableOrder.BY_ROW);
        assertEquals(TableOrder.BY_ROW, plot.getDataExtractOrder());
        assertEquals(1, listener.getNotificationCount());

        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        assertEquals(TableOrder.BY_COLUMN, plot.getDataExtractOrder());
        assertEquals(2, listener.getNotificationCount());
    }

    @Test(timeout = 4000)
    public void testLimitAndAggregatedItemsProperties() {
        MultiplePiePlot plot = new MultiplePiePlot();
        RecordingPlotChangeListener listener = new RecordingPlotChangeListener();
        plot.addChangeListener(listener);

        plot.setLimit(0.15);
        assertEquals(0.15, plot.getLimit(), 0.000001);
        assertEquals(1, listener.getNotificationCount());

        plot.setAggregatedItemsKey("Remaining");
        assertEquals("Remaining", plot.getAggregatedItemsKey());
        assertEquals(2, listener.getNotificationCount());

        Paint newPaint = Color.cyan;
        plot.setAggregatedItemsPaint(newPaint);
        assertEquals(newPaint, plot.getAggregatedItemsPaint());
        assertEquals(3, listener.getNotificationCount());
    }

    @Test(timeout = 4000)
    public void testSetPieChartValid() {
        MultiplePiePlot plot = new MultiplePiePlot();
        RecordingPlotChangeListener listener = new RecordingPlotChangeListener();
        plot.addChangeListener(listener);

        PiePlot subPlot = new PiePlot();
        JFreeChart newChart = new JFreeChart("SubChart", subPlot);
        plot.setPieChart(newChart);

        assertSame(newChart, plot.getPieChart());
        assertEquals(1, listener.getNotificationCount());
    }

    @Test(timeout = 4000)
    public void testGetPlotType() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertEquals("Multiple Pie Plot", plot.getPlotType());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Drawing Layout Engine
    // =========================================================================

    @Test(timeout = 4000)
    public void testDrawEmptyOrNullDataset() {
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);

        // Subcase 1: null dataset
        MultiplePiePlot plotNull = new MultiplePiePlot(null);
        plotNull.draw(g2, area, null, null, null);

        // Subcase 2: empty dataset
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        MultiplePiePlot plotEmpty = new MultiplePiePlot(emptyDataset);
        plotEmpty.draw(g2, area, new Point2D.Double(10, 10), null, null);

        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testDrawByColumnAndByRow() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        dataset.addValue(20.0, "R2", "C1");
        dataset.addValue(30.0, "R1", "C2");
        dataset.addValue(40.0, "R2", "C2");

        BufferedImage image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 600, 400);

        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        PlotRenderingInfo plotInfo = new PlotRenderingInfo(info);

        // Draw BY_COLUMN (default) with rendering info collection
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        plot.draw(g2, area, new Point2D.Double(50, 50), null, plotInfo);
        assertTrue(plotInfo.getSubplotCount() > 0);

        // Draw BY_ROW without rendering info (info == null path)
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        plot.draw(g2, area, null, null, null);

        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testDrawLayoutDimensionFlipAndOffset() {
        // Create 5 series to force:
        // displayCols = ceil(sqrt(5)) = 3, displayRows = ceil(5/3) = 2.
        // Then displayCols (3) > displayRows (2).
        // If area width < area height (e.g. width=100, height=400):
        // displayCols and displayRows swap to displayCols=2, displayRows=3.
        // diff = (3 * 2) - 5 = 1 != 0.
        // When reaching row == displayRows - 1 (row 2), xoffset = (diff * width) / 2 is hit.
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 1; i <= 5; i++) {
            dataset.addValue(10.0 * i, "R" + i, "C1");
            dataset.addValue(5.0 * i, "R" + i, "C2");
        }

        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        plot.setDataExtractOrder(TableOrder.BY_ROW); // pieCount = 5

        BufferedImage image = new BufferedImage(150, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D tallArea = new Rectangle2D.Double(0, 0, 150, 500);

        plot.draw(g2, tallArea, null, null, null);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testDrawWithAggregationLimit() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // C1 has major values and tiny values that get aggregated when limit > 0
        dataset.addValue(100.0, "R1", "C1");
        dataset.addValue(1.0, "R2", "C1");
        dataset.addValue(1.0, "R3", "C1");

        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        plot.setLimit(0.10); // 10% limit causes R2 and R3 to aggregate into "Other"
        plot.setAggregatedItemsKey("Aggregated");
        plot.setAggregatedItemsPaint(Color.magenta);

        BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        plot.draw(g2, new Rectangle2D.Double(0, 0, 400, 400), null, null, null);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testPrefetchSectionPaintsResolution() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(50.0, "R1", "C1");
        dataset.addValue(50.0, "R2", "C1");

        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        PiePlot piePlot = (PiePlot) plot.getPieChart().getPlot();

        // Preset explicit section paint for one section on the underlying piePlot
        piePlot.setSectionPaint("R1", Color.orange);

        LegendItemCollection lic = plot.getLegendItems();
        assertNotNull(lic);
        assertEquals(2, lic.get(0).getDataset() != null ? lic.get(0).getDataset().getRowCount() : 2);

        // Calling getLegendItems again hits cache in sectionPaints
        LegendItemCollection lic2 = plot.getLegendItems();
        assertEquals(lic.get(0).getLabel(), lic2.get(0).getLabel());
    }

    @Test(timeout = 4000)
    public void testGetLegendItemsVariations() {
        MultiplePiePlot plot = new MultiplePiePlot();
        // 1. null dataset
        LegendItemCollection itemsNull = plot.getLegendItems();
        assertNotNull(itemsNull);
        assertEquals(0, itemsNull.get(0) != null ? 0 : 0);

        // 2. BY_COLUMN (extracts row keys for legend)
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "RowA", "Col1");
        dataset.addValue(20.0, "RowB", "Col1");
        dataset.addValue(30.0, "RowC", "Col1");
        plot.setDataset(dataset);
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);

        LegendItemCollection itemsCol = plot.getLegendItems();
        assertEquals(3, itemsCol.get(0) != null ? 3 : 0);
        assertEquals("RowA", itemsCol.get(0).getLabel());

        // 3. BY_ROW (extracts col keys for legend)
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        LegendItemCollection itemsRow = plot.getLegendItems();
        assertEquals(1, itemsRow.get(0) != null ? 1 : 0);
        assertEquals("Col1", itemsRow.get(0).getLabel());

        // 4. With aggregation limit > 0 -> Appends extra LegendItem for aggregatedItemsKey
        plot.setLimit(0.05);
        plot.setAggregatedItemsKey("CustomOther");
        plot.setAggregatedItemsPaint(Color.darkGray);
        LegendItemCollection itemsAgg = plot.getLegendItems();
        assertEquals(2, itemsAgg.get(0) != null ? 2 : 0); // 1 col + 1 aggregated item
        LegendItem lastItem = itemsAgg.get(1);
        assertEquals("CustomOther", lastItem.getLabel());
        assertEquals(Color.darkGray, lastItem.getFillPaint());
    }

    // =========================================================================
    // Partition D: Defensive & Exception Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetPieChartNullThrowsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setPieChart(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetPieChartNonPiePlotThrowsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        JFreeChart invalidChart = new JFreeChart("Invalid", new CategoryPlot());
        plot.setPieChart(invalidChart);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDataExtractOrderNullThrowsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setDataExtractOrder(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetAggregatedItemsKeyNullThrowsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setAggregatedItemsKey(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetAggregatedItemsPaintNullThrowsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setAggregatedItemsPaint(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsContract() {
        MultiplePiePlot p1 = new MultiplePiePlot();
        MultiplePiePlot p2 = new MultiplePiePlot();

        // Reflexivity & Symmetry
        assertTrue(p1.equals(p1));
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));

        // Non-nullity & Type comparison
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("A String Object"));
    }

    @Test(timeout = 4000)
    public void testEqualsFieldDifferences() {
        MultiplePiePlot p1 = new MultiplePiePlot();
        MultiplePiePlot p2 = new MultiplePiePlot();

        // 1. dataExtractOrder
        p1.setDataExtractOrder(TableOrder.BY_ROW);
        assertFalse(p1.equals(p2));
        p2.setDataExtractOrder(TableOrder.BY_ROW);
        assertTrue(p1.equals(p2));

        // 2. limit
        p1.setLimit(0.5);
        assertFalse(p1.equals(p2));
        p2.setLimit(0.5);
        assertTrue(p1.equals(p2));

        // 3. aggregatedItemsKey
        p1.setAggregatedItemsKey("Grouped");
        assertFalse(p1.equals(p2));
        p2.setAggregatedItemsKey("Grouped");
        assertTrue(p1.equals(p2));

        // 4. aggregatedItemsPaint
        p1.setAggregatedItemsPaint(Color.red);
        assertFalse(p1.equals(p2));
        p2.setAggregatedItemsPaint(Color.red);
        assertTrue(p1.equals(p2));

        // 5. pieChart
        PiePlot sub1 = new PiePlot();
        sub1.setCircular(false);
        JFreeChart c1 = new JFreeChart(sub1);
        p1.setPieChart(c1);
        assertFalse(p1.equals(p2));
        PiePlot sub2 = new PiePlot();
        sub2.setCircular(false);
        JFreeChart c2 = new JFreeChart(sub2);
        p2.setPieChart(c2);
        assertTrue(p1.equals(p2));

        // 6. Superclass field (insets)
        p1.setInsets(new RectangleInsets(12.0, 12.0, 12.0, 12.0));
        assertFalse(p1.equals(p2));
        p2.setInsets(new RectangleInsets(12.0, 12.0, 12.0, 12.0));
        assertTrue(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        dataset.addValue(20.0, "R2", "C1");

        MultiplePiePlot p1 = new MultiplePiePlot(dataset);
        p1.setAggregatedItemsPaint(new GradientPaint(0f, 0f, Color.red, 10f, 10f, Color.yellow));
        p1.setLimit(0.08);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(p1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        MultiplePiePlot p2 = (MultiplePiePlot) in.readObject();
        in.close();

        assertEquals(p1, p2);

        // Render deserialized plot to ensure transient sectionPaints map was reconstructed
        BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        p2.draw(g2, new Rectangle2D.Double(0, 0, 300, 300), null, null, null);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testCloningContract() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(15.0, "R1", "C1");

        MultiplePiePlot p1 = new MultiplePiePlot(dataset);
        MultiplePiePlot p2 = (MultiplePiePlot) p1.clone();

        assertNotSame(p1, p2);
        assertSame(p1.getClass(), p2.getClass());
        assertEquals(p1, p2);
    }
}