package org.jfree.chart.renderer.category;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.jfree.chart.renderer.category.AbstractCategoryItemRenderer
 * Target Bug: Defects4J Chart-1 (Inverted dataset null check in getLegendItems())
 *
 * Branch & Condition Coverage Matrix:
 * 1. getLegendItems():
 *    - Branch: this.plot == null -> returns empty LegendItemCollection.
 *    - Branch: dataset == null (DEFECT: coded as dataset != null) -> returns empty collection.
 *    - Branch: dataset != null with series -> iterates series and collects visible items.
 *    - Branch: rowRenderingOrder == SortOrder.ASCENDING vs SortOrder.DESCENDING -> iteration order.
 *    - Condition: isSeriesVisibleInLegend(i) true vs false.
 * 2. getLegendItem(int, int):
 *    - Branch: plot == null -> returns null.
 *    - Branch: !isSeriesVisible(s) || !isSeriesVisibleInLegend(s) -> returns null.
 *    - Branch: custom legend tool tip and URL generators (null vs non-null).
 *    - Branch: legend text paint (null vs non-null).
 * 3. setPlot(CategoryPlot):
 *    - Branch: plot == null -> throws IllegalArgumentException.
 *    - Branch: plot != null -> assigns field.
 * 4. initialise(...):
 *    - Branch: dataset == null (rowCount=0, colCount=0) vs dataset != null (rowCount, colCount assigned).
 * 5. findRangeBounds(CategoryDataset, boolean):
 *    - Branch: dataset == null -> returns null.
 *    - Branch: getDataBoundsIncludesVisibleSeriesOnly() true vs false.
 * 6. drawDomainLine(...) & drawRangeLine(...):
 *    - Branch: paint == null || stroke == null -> throws IllegalArgumentException.
 *    - Branch: PlotOrientation.HORIZONTAL vs PlotOrientation.VERTICAL.
 *    - Branch: range.contains(value) false -> early return.
 * 7. Annotations:
 *    - Branch: null annotation -> IllegalArgumentException.
 *    - Branch: Layer.FOREGROUND vs Layer.BACKGROUND vs unknown layer.
 *    - Branch: removeAnnotation -> removes from both layers.
 * 8. Object contracts & Generators:
 *    - clone(), equals(), hashCode(), serialization roundtrip.
 *    - Series generator fallback to base generator when series generator is null.
 * ====================================================================================================
 */

import org.junit.Test;
import static org.junit.Assert.*;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategorySeriesLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryCrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.Range;

public class AbstractCategoryItemRendererGeminiTest {

    /**
     * Concrete test implementation extending AbstractCategoryItemRenderer.
     */
    private static class TestRenderer extends AbstractCategoryItemRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                Rectangle2D dataArea, CategoryPlot plot,
                CategoryAxis domainAxis, ValueAxis rangeAxis,
                CategoryDataset dataset, int row, int column, int pass) {
            // Minimal no-op implementation for testing
        }

        public void callUpdateCrosshairValues(CategoryCrosshairState crosshairState,
                Comparable rowKey, Comparable columnKey, double value,
                int datasetIndex, double transX, double transY, PlotOrientation orientation) {
            super.updateCrosshairValues(crosshairState, rowKey, columnKey, value,
                    datasetIndex, transX, transY, orientation);
        }
    }

    /**
     * Listener helper to verify event notification propagation.
     */
    private static class EventCounterListener implements RendererChangeListener {
        private int count = 0;

        @Override
        public void rendererChanged(RendererChangeEvent event) {
            this.count++;
        }

        public int getCount() {
            return this.count;
        }
    }

    // =========================================================================
    // PARTITION A: Legend Items & Chart-1 Critical Bug Zone
    // =========================================================================

    /**
     * Exposes Chart-1 defect:
     * In defective AbstractCategoryItemRenderer, `if (dataset != null)` incorrectly
     * causes getLegendItems() to immediately return an empty collection when a dataset exists.
     */
    @Test(timeout = 4000)
    public void testChart1ExposeInvertedDatasetNullCheckInLegendItems() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "Series 1", "Category 1");

        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);

        TestRenderer renderer = new TestRenderer();
        plot.setRenderer(renderer);

        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull("Legend item collection should not be null", items);
        assertEquals("Renderer should return 1 legend item for 1 visible series",
                1, items.getItemCount());
    }

    @Test(timeout = 4000)
    public void testGetLegendItemsWithNullPlotReturnsEmptyCollection() {
        TestRenderer renderer = new TestRenderer();
        assertNull(renderer.getPlot());
        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(0, items.getItemCount());
    }

    @Test(timeout = 4000)
    public void testGetLegendItemsSeriesVisibility() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        dataset.addValue(2.0, "S2", "C1");

        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        TestRenderer renderer = new TestRenderer();
        plot.setRenderer(renderer);

        // Hide series 0 in legend
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);

        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(1, items.getItemCount());
        assertEquals("S2", items.get(0).getLabel());
    }

    @Test(timeout = 4000)
    public void testGetLegendItemsDescendingOrder() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        dataset.addValue(2.0, "S2", "C1");

        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        plot.setRowRenderingOrder(SortOrder.DESCENDING);

        TestRenderer renderer = new TestRenderer();
        plot.setRenderer(renderer);

        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(2, items.getItemCount());
        assertEquals("S2", items.get(0).getLabel());
        assertEquals("S1", items.get(1).getLabel());
    }

    @Test(timeout = 4000)
    public void testGetLegendItemDirectLookup() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5.0, "SeriesA", "CatA");

        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);

        TestRenderer renderer = new TestRenderer();
        plot.setRenderer(renderer);

        // Valid visible item
        LegendItem item = renderer.getLegendItem(0, 0);
        assertNotNull(item);
        assertEquals("SeriesA", item.getLabel());
        assertEquals(0, item.getSeriesIndex());
        assertEquals(0, item.getDatasetIndex());

        // Invisible series check
        renderer.setSeriesVisible(0, Boolean.FALSE);
        assertNull(renderer.getLegendItem(0, 0));

        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        assertNull(renderer.getLegendItem(0, 0));

        // When unassigned from plot
        TestRenderer unattachedRenderer = new TestRenderer();
        assertNull(unattachedRenderer.getLegendItem(0, 0));
    }

    @Test(timeout = 4000)
    public void testGetLegendItemGeneratorsAndAttributes() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "Series1", "Cat1");

        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);

        TestRenderer renderer = new TestRenderer();
        plot.setRenderer(renderer);

        renderer.setLegendItemToolTipGenerator(new StandardCategorySeriesLabelGenerator("Tip: {0}"));
        renderer.setLegendItemURLGenerator(new StandardCategorySeriesLabelGenerator("http://{0}"));
        renderer.setBaseLegendTextPaint(Color.BLUE);
        renderer.setBaseLegendTextFont(new Font("SansSerif", Font.BOLD, 12));

        LegendItem item = renderer.getLegendItem(0, 0);
        assertNotNull(item);
        assertEquals("Tip: Series1", item.getToolTipText());
        assertEquals("http://Series1", item.getURLText());
        assertEquals(Color.BLUE, item.getLabelPaint());
        assertEquals(new Font("SansSerif", Font.BOLD, 12), item.getLabelFont());
    }

    // =========================================================================
    // PARTITION B: Plot & Dataset Interaction
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetPlotSuccess() {
        TestRenderer renderer = new TestRenderer();
        CategoryPlot plot = new CategoryPlot();
        renderer.setPlot(plot);
        assertSame(plot, renderer.getPlot());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetPlotNullThrowsException() {
        TestRenderer renderer = new TestRenderer();
        renderer.setPlot(null);
    }

    @Test(timeout = 4000)
    public void testGetPassCount() {
        TestRenderer renderer = new TestRenderer();
        assertEquals(1, renderer.getPassCount());
    }

    @Test(timeout = 4000)
    public void testInitialiseWithAndWithoutDataset() {
        TestRenderer renderer = new TestRenderer();
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R2", "C1");

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);

        CategoryItemRendererState state = renderer.initialise(g2, dataArea, plot, dataset, null);
        assertNotNull(state);
        assertEquals(2, renderer.getRowCount());
        assertEquals(1, renderer.getColumnCount());
        assertNotNull(state.getVisibleSeriesArray());
        assertEquals(2, state.getVisibleSeriesArray().length);

        // Null dataset re-initialisation
        CategoryItemRendererState stateNull = renderer.initialise(g2, dataArea, plot, null, null);
        assertNotNull(stateNull);
        assertEquals(0, renderer.getRowCount());
        assertEquals(0, renderer.getColumnCount());
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testFindRangeBounds() {
        TestRenderer renderer = new TestRenderer();
        assertNull(renderer.findRangeBounds(null));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        assertNull(renderer.findRangeBounds(dataset));

        dataset.addValue(2.5, "R1", "C1");
        dataset.addValue(7.5, "R1", "C2");
        Range bounds = renderer.findRangeBounds(dataset);
        assertNotNull(bounds);
        assertEquals(2.5, bounds.getLowerBound(), 1e-9);
        assertEquals(7.5, bounds.getUpperBound(), 1e-9);

        // Bounds with visible series only
        renderer.setDataBoundsIncludesVisibleSeriesOnly(true);
        renderer.setSeriesVisible(0, Boolean.FALSE);
        assertNull(renderer.findRangeBounds(dataset));

        renderer.setSeriesVisible(0, Boolean.TRUE);
        Range boundsVisible = renderer.findRangeBounds(dataset);
        assertNotNull(boundsVisible);
        assertEquals(2.5, boundsVisible.getLowerBound(), 1e-9);
        assertEquals(7.5, boundsVisible.getUpperBound(), 1e-9);
    }

    // =========================================================================
    // PARTITION C: Item Rendering Attributes & Generators
    // =========================================================================

    @Test(timeout = 4000)
    public void testItemLabelGeneratorHierarchy() {
        TestRenderer renderer = new TestRenderer();
        CategoryItemLabelGenerator baseGen = new StandardCategoryItemLabelGenerator();
        CategoryItemLabelGenerator seriesGen = new StandardCategoryItemLabelGenerator();

        renderer.setBaseItemLabelGenerator(baseGen);
        assertSame(baseGen, renderer.getItemLabelGenerator(0, 0, false));

        renderer.setSeriesItemLabelGenerator(0, seriesGen);
        assertSame(seriesGen, renderer.getItemLabelGenerator(0, 0, false));
        assertSame(baseGen, renderer.getItemLabelGenerator(1, 0, false));
    }

    @Test(timeout = 4000)
    public void testToolTipGeneratorHierarchy() {
        TestRenderer renderer = new TestRenderer();
        CategoryToolTipGenerator baseGen = new StandardCategoryToolTipGenerator();
        CategoryToolTipGenerator seriesGen = new StandardCategoryToolTipGenerator();

        renderer.setBaseToolTipGenerator(baseGen);
        assertSame(baseGen, renderer.getToolTipGenerator(0, 0, false));

        renderer.setSeriesToolTipGenerator(0, seriesGen);
        assertSame(seriesGen, renderer.getToolTipGenerator(0, 0, false));
        assertSame(baseGen, renderer.getToolTipGenerator(1, 0, false));
    }

    @Test(timeout = 4000)
    public void testURLGeneratorHierarchy() {
        TestRenderer renderer = new TestRenderer();
        CategoryURLGenerator baseURL = new StandardCategoryURLGenerator();
        CategoryURLGenerator seriesURL = new StandardCategoryURLGenerator();

        renderer.setBaseURLGenerator(baseURL);
        assertSame(baseURL, renderer.getURLGenerator(0, 0, false));

        renderer.setSeriesURLGenerator(0, seriesURL);
        assertSame(seriesURL, renderer.getURLGenerator(0, 0, false));
        assertSame(baseURL, renderer.getURLGenerator(1, 0, false));
    }

    @Test(timeout = 4000)
    public void testGeneratorNotificationEvents() {
        TestRenderer renderer = new TestRenderer();
        EventCounterListener listener = new EventCounterListener();
        renderer.addChangeListener(listener);

        CategoryItemLabelGenerator itemGen = new StandardCategoryItemLabelGenerator();
        renderer.setSeriesItemLabelGenerator(0, itemGen, false);
        assertEquals(0, listener.getCount());
        renderer.setSeriesItemLabelGenerator(0, itemGen, true);
        assertEquals(1, listener.getCount());

        renderer.setBaseItemLabelGenerator(itemGen, false);
        assertEquals(1, listener.getCount());
        renderer.setBaseItemLabelGenerator(itemGen, true);
        assertEquals(2, listener.getCount());

        CategoryToolTipGenerator tipGen = new StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tipGen, false);
        assertEquals(2, listener.getCount());
        renderer.setSeriesToolTipGenerator(0, tipGen, true);
        assertEquals(3, listener.getCount());

        renderer.setBaseToolTipGenerator(tipGen, false);
        assertEquals(3, listener.getCount());
        renderer.setBaseToolTipGenerator(tipGen, true);
        assertEquals(4, listener.getCount());

        CategoryURLGenerator urlGen = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, urlGen, false);
        assertEquals(4, listener.getCount());
        renderer.setSeriesURLGenerator(0, urlGen, true);
        assertEquals(5, listener.getCount());

        renderer.setBaseURLGenerator(urlGen, false);
        assertEquals(5, listener.getCount());
        renderer.setBaseURLGenerator(urlGen, true);
        assertEquals(6, listener.getCount());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetLegendItemLabelGeneratorNullThrowsException() {
        TestRenderer renderer = new TestRenderer();
        renderer.setLegendItemLabelGenerator(null);
    }

    @Test(timeout = 4000)
    public void testGetItemMiddle() {
        TestRenderer renderer = new TestRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        CategoryAxis axis = new CategoryAxis("Category");
        Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 50);

        double middle = renderer.getItemMiddle("R1", "C1", dataset, axis, area, RectangleEdge.BOTTOM);
        assertTrue("Item middle should be inside bounds", middle >= 0.0 && middle <= 100.0);
    }

    // =========================================================================
    // PARTITION D: Annotations, Lines, and Markers
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddAnnotationNullThrowsException() {
        TestRenderer renderer = new TestRenderer();
        renderer.addAnnotation(null);
    }

    @Test(timeout = 4000)
    public void testAddAndRemoveAnnotations() {
        TestRenderer renderer = new TestRenderer();
        EventCounterListener listener = new EventCounterListener();
        renderer.addChangeListener(listener);

        CategoryAnnotation annotation1 = new CategoryTextAnnotation("A1", "C1", 10.0);
        CategoryAnnotation annotation2 = new CategoryTextAnnotation("A2", "C1", 20.0);

        renderer.addAnnotation(annotation1, Layer.FOREGROUND);
        assertEquals(1, listener.getCount());

        renderer.addAnnotation(annotation2, Layer.BACKGROUND);
        assertEquals(2, listener.getCount());

        assertTrue(renderer.removeAnnotation(annotation1));
        assertEquals(3, listener.getCount());

        renderer.removeAnnotations();
        assertEquals(4, listener.getCount());
        assertFalse(renderer.removeAnnotation(annotation1));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDrawDomainLineNullPaintThrowsException() {
        TestRenderer renderer = new TestRenderer();
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        renderer.drawDomainLine(g2, new CategoryPlot(), new Rectangle2D.Double(), 0.0,
                null, new BasicStroke(1.0f));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDrawDomainLineNullStrokeThrowsException() {
        TestRenderer renderer = new TestRenderer();
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        renderer.drawDomainLine(g2, new CategoryPlot(), new Rectangle2D.Double(), 0.0,
                Color.BLACK, null);
    }

    @Test(timeout = 4000)
    public void testDrawDomainAndRangeLinesOrientations() {
        TestRenderer renderer = new TestRenderer();
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 100);

        CategoryPlot plot = new CategoryPlot();
        ValueAxis axis = new NumberAxis("Range");
        axis.setRange(0.0, 100.0);

        // Domain line: HORIZONTAL & VERTICAL
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        renderer.drawDomainLine(g2, plot, area, 50.0, Color.BLACK, new BasicStroke(1.0f));

        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawDomainLine(g2, plot, area, 50.0, Color.BLACK, new BasicStroke(1.0f));

        // Range line: Value in range vs out of range
        renderer.drawRangeLine(g2, plot, axis, area, 150.0, Color.RED, new BasicStroke(1.0f)); // out of range
        renderer.drawRangeLine(g2, plot, axis, area, 50.0, Color.RED, new BasicStroke(1.0f)); // in range (VERTICAL)

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        renderer.drawRangeLine(g2, plot, axis, area, 50.0, Color.RED, new BasicStroke(1.0f)); // in range (HORIZONTAL)

        g2.dispose();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUpdateCrosshairValuesNullOrientationThrowsException() {
        TestRenderer renderer = new TestRenderer();
        renderer.callUpdateCrosshairValues(null, "R1", "C1", 1.0, 0, 0.0, 0.0, null);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testCreateHotSpotShapeThrowsNotImplemented() {
        TestRenderer renderer = new TestRenderer();
        renderer.createHotSpotShape(null, null, null, null, null, null, 0, 0, false, null);
    }

    @Test(timeout = 4000)
    public void testHotSpotBoundsAndHitTest() {
        TestRenderer renderer = new TestRenderer();
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(null, "R1", "C1");

        CategoryAxis domainAxis = new CategoryAxis("Domain");
        ValueAxis rangeAxis = new NumberAxis("Range");
        rangeAxis.setRange(0.0, 20.0);
        Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 100);

        // Null data value returns null bounds
        Rectangle2D boundsNull = renderer.createHotSpotBounds(null, area, plot, domainAxis,
                rangeAxis, dataset, 0, 0, false, null, null);
        assertNull(boundsNull);
        assertFalse(renderer.hitTest(50.0, 50.0, null, area, plot, domainAxis,
                rangeAxis, dataset, 0, 0, false, null));

        // Non-null data value creates valid bounds
        dataset.setValue(10.0, "R1", "C1");
        Rectangle2D bounds = renderer.createHotSpotBounds(null, area, plot, domainAxis,
                rangeAxis, dataset, 0, 0, false, null, null);
        assertNotNull(bounds);
        assertEquals(4.0, bounds.getWidth(), 1e-9);
        assertEquals(4.0, bounds.getHeight(), 1e-9);

        assertTrue(renderer.hitTest(bounds.getCenterX(), bounds.getCenterY(), null,
                area, plot, domainAxis, rangeAxis, dataset, 0, 0, false, null));
        assertFalse(renderer.hitTest(-500.0, -500.0, null,
                area, plot, domainAxis, rangeAxis, dataset, 0, 0, false, null));
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contracts
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        TestRenderer r1 = new TestRenderer();
        TestRenderer r2 = new TestRenderer();
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        // Self and type checks
        assertTrue(r1.equals(r1));
        assertFalse(r1.equals(null));
        assertFalse(r1.equals("Not a renderer"));

        // Differences in label generator
        r1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        assertFalse(r1.equals(r2));
        r2.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        assertEquals(r1, r2);

        // Differences in tooltip generator
        r1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        assertFalse(r1.equals(r2));
        r2.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        assertEquals(r1, r2);

        // Differences in URL generator
        r1.setBaseURLGenerator(new StandardCategoryURLGenerator());
        assertFalse(r1.equals(r2));
        r2.setBaseURLGenerator(new StandardCategoryURLGenerator());
        assertEquals(r1, r2);

        // Differences in annotations
        CategoryAnnotation annotation = new CategoryTextAnnotation("Text", "C1", 1.0);
        r1.addAnnotation(annotation);
        assertFalse(r1.equals(r2));
        r2.addAnnotation(annotation);
        assertEquals(r1, r2);
    }

    @Test(timeout = 4000)
    public void testCloneIntegrity() throws CloneNotSupportedException {
        TestRenderer r1 = new TestRenderer();
        r1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        r1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        r1.setBaseURLGenerator(new StandardCategoryURLGenerator());

        TestRenderer r2 = (TestRenderer) r1.clone();
        assertNotSame(r1, r2);
        assertSame(r1.getClass(), r2.getClass());
        assertEquals(r1, r2);

        // Modifying clone should not mutate original
        r2.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
        assertFalse(r1.equals(r2));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundtrip() throws Exception {
        TestRenderer r1 = new TestRenderer();
        r1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        r1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        r1.setBaseURLGenerator(new StandardCategoryURLGenerator());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(r1);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        TestRenderer r2 = (TestRenderer) ois.readObject();

        assertEquals(r1, r2);
    }
}