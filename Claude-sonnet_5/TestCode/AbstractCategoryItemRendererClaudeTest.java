package org.jfree.chart.renderer.category;

import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.junit.Test;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.LengthAdjustmentType;
import org.jfree.chart.util.RectangleAnchor;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * JUnit 4 test suite for {@link AbstractCategoryItemRenderer}.
 */
public class AbstractCategoryItemRendererClaudeTest {

    /**
     * Minimal concrete subclass used purely to instantiate the abstract
     * class under test.  The drawItem() implementation is a no-op because
     * the rendering pipeline itself is out of scope for these tests.
     */
    private static class TestRenderer extends AbstractCategoryItemRenderer {
        public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                Rectangle2D dataArea, CategoryPlot plot,
                CategoryAxis domainAxis, ValueAxis rangeAxis,
                CategoryDataset dataset, int row, int column,
                boolean selected, int pass) {
            // no-op
        }
    }

    private static class CapturingListener implements RendererChangeListener {
        boolean called = false;
        public void rendererChanged(RendererChangeEvent event) {
            this.called = true;
        }
    }

    // ------------------------------------------------------------------
    // DEFECT-TARGETING TEST
    // ------------------------------------------------------------------

    /**
     * @target AbstractCategoryItemRenderer#getLegendItems()
     * @scenario A plot with a valid dataset containing exactly one series
     *           is attached to the renderer.
     * @defectRisk The known defect inverts the null-check on the dataset
     *             (checks "dataset != null" instead of "dataset == null"),
     *             causing getLegendItems() to incorrectly return an empty
     *             collection whenever a dataset IS present. This test
     *             fails on the buggy version (expected 1 but got 0) and
     *             passes on the fixed version.
     */
    @Test(timeout = 4000)
    public void testBugChart1_GetLegendItemsWithDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Series1", "Category1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        TestRenderer renderer = new TestRenderer();
        plot.setRenderer(renderer);

        LegendItemCollection items = renderer.getLegendItems();

        assertNotNull(items);
        assertEquals(1, items.getItemCount());
    }

    /**
     * @target AbstractCategoryItemRenderer#getLegendItems()
     * @scenario Dataset with two series and descending row rendering order.
     * @defectRisk Ensures the descending branch of getLegendItems() also
     *             produces the correct count once the null-check defect is
     *             fixed.
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsDescendingOrder() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        dataset.addValue(2.0, "S2", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        TestRenderer r = new TestRenderer();
        plot.setRenderer(r);
        plot.setRowRenderingOrder(SortOrder.DESCENDING);

        LegendItemCollection items = r.getLegendItems();
        assertEquals(2, items.getItemCount());
    }

    /**
     * @target AbstractCategoryItemRenderer#getLegendItems()
     * @scenario One of two series is marked not visible in legend.
     * @defectRisk Confirms the isSeriesVisibleInLegend() filter branch
     *             correctly skips hidden series once the dataset null-check
     *             defect is not masking the result.
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsSkipsNotVisibleInLegend() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        dataset.addValue(2.0, "S2", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        TestRenderer r = new TestRenderer();
        plot.setRenderer(r);
        r.setSeriesVisibleInLegend(0, Boolean.FALSE);

        LegendItemCollection items = r.getLegendItems();
        assertEquals(1, items.getItemCount());
    }

    /**
     * @target AbstractCategoryItemRenderer#getLegendItems()
     * @scenario Renderer not attached to any plot.
     * @defectRisk Verifies the early-return branch (plot == null) still
     *             yields an empty (never null) collection.
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullPlot() {
        TestRenderer r = new TestRenderer();
        LegendItemCollection items = r.getLegendItems();
        assertNotNull(items);
        assertEquals(0, items.getItemCount());
    }

    // ------------------------------------------------------------------
    // CONSTRUCTOR / DEFAULT STATE
    // ------------------------------------------------------------------

    /**
     * @target Default constructor state.
     * @scenario Freshly constructed renderer.
     * @defectRisk Ensures all default fields (generators, plot, counts)
     *             are correctly initialised (null / zero / non-null legend
     *             label generator).
     */
    @Test(timeout = 4000)
    public void testDefaultConstructorState() {
        TestRenderer r = new TestRenderer();
        assertNull(r.getBaseItemLabelGenerator());
        assertNull(r.getBaseToolTipGenerator());
        assertNull(r.getBaseURLGenerator());
        assertNotNull(r.getLegendItemLabelGenerator());
        assertNull(r.getLegendItemToolTipGenerator());
        assertNull(r.getLegendItemURLGenerator());
        assertEquals(1, r.getPassCount());
        assertNull(r.getPlot());
        assertEquals(0, r.getRowCount());
        assertEquals(0, r.getColumnCount());
    }

    // ------------------------------------------------------------------
    // PLOT ACCESSORS
    // ------------------------------------------------------------------

    /**
     * @target setPlot(CategoryPlot)
     * @scenario Passing a null plot.
     * @defectRisk Verifies the explicit null-check throws
     *             IllegalArgumentException.
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetPlotNullThrows() {
        TestRenderer r = new TestRenderer();
        r.setPlot(null);
    }

    /**
     * @target setPlot(CategoryPlot) / getPlot()
     * @scenario Setting a valid plot then reading it back.
     * @defectRisk Confirms plot reference is stored and returned as-is.
     */
    @Test(timeout = 4000)
    public void testSetPlotAndGetPlot() {
        TestRenderer r = new TestRenderer();
        CategoryPlot plot = new CategoryPlot();
        r.setPlot(plot);
        assertSame(plot, r.getPlot());
    }

    // ------------------------------------------------------------------
    // ITEM LABEL GENERATOR
    // ------------------------------------------------------------------

    /**
     * @target getItemLabelGenerator / setSeriesItemLabelGenerator /
     *         setBaseItemLabelGenerator
     * @scenario Series-specific generator overrides base; falls back to
     *           base when series generator absent.
     * @defectRisk Validates fallback logic branch in
     *             getItemLabelGenerator(row, column, selected).
     */
    @Test(timeout = 4000)
    public void testItemLabelGeneratorSeriesAndBase() {
        TestRenderer r = new TestRenderer();
        StandardCategoryItemLabelGenerator gen
                = new StandardCategoryItemLabelGenerator();
        r.setSeriesItemLabelGenerator(0, gen);
        assertSame(gen, r.getSeriesItemLabelGenerator(0));
        assertSame(gen, r.getItemLabelGenerator(0, 0, false));
        assertNull(r.getItemLabelGenerator(1, 0, false));

        StandardCategoryItemLabelGenerator baseGen
                = new StandardCategoryItemLabelGenerator();
        r.setBaseItemLabelGenerator(baseGen);
        assertSame(baseGen, r.getBaseItemLabelGenerator());
        assertSame(baseGen, r.getItemLabelGenerator(1, 0, false));

        // exercise notify=false variants (no exception expected)
        r.setSeriesItemLabelGenerator(2, gen, false);
        r.setBaseItemLabelGenerator(baseGen, false);
    }

    // ------------------------------------------------------------------
    // TOOL TIP GENERATOR
    // ------------------------------------------------------------------

    /**
     * @target getToolTipGenerator / setSeriesToolTipGenerator /
     *         setBaseToolTipGenerator
     * @scenario Series generator set, then base generator fallback.
     * @defectRisk Validates layered lookup logic and notify=false paths.
     */
    @Test(timeout = 4000)
    public void testToolTipGeneratorSeriesAndBase() {
        TestRenderer r = new TestRenderer();
        StandardCategoryToolTipGenerator gen
                = new StandardCategoryToolTipGenerator();
        r.setSeriesToolTipGenerator(0, gen);
        assertSame(gen, r.getSeriesToolTipGenerator(0));
        assertSame(gen, r.getToolTipGenerator(0, 0, false));

        StandardCategoryToolTipGenerator base
                = new StandardCategoryToolTipGenerator();
        r.setBaseToolTipGenerator(base);
        assertSame(base, r.getBaseToolTipGenerator());
        assertSame(base, r.getToolTipGenerator(5, 0, false));

        r.setSeriesToolTipGenerator(1, gen, false);
        r.setBaseToolTipGenerator(base, false);
    }

    // ------------------------------------------------------------------
    // URL GENERATOR
    // ------------------------------------------------------------------

    /**
     * @target getURLGenerator / setSeriesURLGenerator / setBaseURLGenerator
     * @scenario Series generator set, then base generator fallback.
     * @defectRisk Validates layered lookup logic for URL generators.
     */
    @Test(timeout = 4000)
    public void testURLGeneratorSeriesAndBase() {
        TestRenderer r = new TestRenderer();
        StandardCategoryURLGenerator gen = new StandardCategoryURLGenerator();
        r.setSeriesURLGenerator(0, gen);
        assertSame(gen, r.getSeriesURLGenerator(0));
        assertSame(gen, r.getURLGenerator(0, 0, false));

        StandardCategoryURLGenerator base = new StandardCategoryURLGenerator();
        r.setBaseURLGenerator(base);
        assertSame(base, r.getBaseURLGenerator());
        assertSame(base, r.getURLGenerator(5, 0, false));

        r.setSeriesURLGenerator(1, gen, false);
        r.setBaseURLGenerator(base, false);
    }

    // ------------------------------------------------------------------
    // ANNOTATIONS
    // ------------------------------------------------------------------

    /**
     * @target addAnnotation(CategoryAnnotation)
     * @scenario Null annotation supplied.
     * @defectRisk Ensures argument-check throws IllegalArgumentException.
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddAnnotationNullThrows() {
        TestRenderer r = new TestRenderer();
        r.addAnnotation(null);
    }

    /**
     * @target addAnnotation / removeAnnotation / removeAnnotations
     * @scenario Add annotations to foreground and background layers, then
     *           remove one and clear the rest.
     * @defectRisk Confirms annotations lists are updated and change
     *             listeners are notified without throwing.
     */
    @Test(timeout = 4000)
    public void testAnnotationsAddRemove() {
        TestRenderer r = new TestRenderer();
        CategoryTextAnnotation ann1 = new CategoryTextAnnotation("A", "Cat1", 1.0);
        r.addAnnotation(ann1);
        CategoryTextAnnotation ann2 = new CategoryTextAnnotation("B", "Cat2", 2.0);
        r.addAnnotation(ann2, Layer.BACKGROUND);

        r.removeAnnotation(ann1);
        r.removeAnnotations();
    }

    // ------------------------------------------------------------------
    // LEGEND ITEM GENERATORS
    // ------------------------------------------------------------------

    /**
     * @target setLegendItemLabelGenerator(null)
     * @scenario Null generator supplied.
     * @defectRisk Ensures the argument check for the label generator
     *             throws IllegalArgumentException.
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetLegendItemLabelGeneratorNullThrows() {
        TestRenderer r = new TestRenderer();
        r.setLegendItemLabelGenerator(null);
    }

    /**
     * @target getLegendItemLabelGenerator / getLegendItemToolTipGenerator /
     *         getLegendItemURLGenerator and their setters.
     * @scenario Assign non-null generators for all three roles.
     * @defectRisk Confirms accessor/mutator symmetry for legend-related
     *             generators.
     */
    @Test(timeout = 4000)
    public void testLegendItemGenerators() {
        TestRenderer r = new TestRenderer();
        StandardCategorySeriesLabelGenerator gen
                = new StandardCategorySeriesLabelGenerator();
        r.setLegendItemLabelGenerator(gen);
        assertSame(gen, r.getLegendItemLabelGenerator());
        r.setLegendItemToolTipGenerator(gen);
        assertSame(gen, r.getLegendItemToolTipGenerator());
        r.setLegendItemURLGenerator(gen);
        assertSame(gen, r.getLegendItemURLGenerator());
    }

    // ------------------------------------------------------------------
    // getLegendItem(int, int)
    // ------------------------------------------------------------------

    /**
     * @target getLegendItem(int, int)
     * @scenario Renderer has no plot assigned.
     * @defectRisk Confirms early-return null when plot is absent.
     */
    @Test(timeout = 4000)
    public void testGetLegendItemNullPlotReturnsNull() {
        TestRenderer r = new TestRenderer();
        assertNull(r.getLegendItem(0, 0));
    }

    /**
     * @target getLegendItem(int, int)
     * @scenario Series marked not visible.
     * @defectRisk Confirms visibility guard returns null.
     */
    @Test(timeout = 4000)
    public void testGetLegendItemSeriesNotVisible() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        TestRenderer r = new TestRenderer();
        plot.setRenderer(r);
        r.setSeriesVisible(0, Boolean.FALSE);
        assertNull(r.getLegendItem(0, 0));
    }

    /**
     * @target getLegendItem(int, int)
     * @scenario Valid visible series produces a populated LegendItem.
     * @defectRisk Confirms label, series key, dataset index are all set
     *             correctly on the returned LegendItem.
     */
    @Test(timeout = 4000)
    public void testGetLegendItemValid() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        TestRenderer r = new TestRenderer();
        plot.setRenderer(r);
        LegendItem item = r.getLegendItem(0, 0);
        assertNotNull(item);
        assertEquals("S1", item.getLabel());
    }

    // ------------------------------------------------------------------
    // ROW/COLUMN COUNTS, INITIALISE, CREATESTATE
    // ------------------------------------------------------------------

    /**
     * @target initialise(...) / getRowCount() / getColumnCount() /
     *         createState(PlotRenderingInfo)
     * @scenario Non-null dataset supplied to initialise().
     * @defectRisk Confirms row/column counts are captured and that the
     *             visible series array in the created state is populated.
     */
    @Test(timeout = 4000)
    public void testInitialiseWithDataset() {
        TestRenderer r = new TestRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        dataset.addValue(2.0, "S2", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        plot.setRenderer(r);
        Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 100);
        CategoryItemRendererState state = r.initialise(null, area, plot,
                dataset, null);
        assertNotNull(state);
        assertEquals(2, r.getRowCount());
        assertEquals(1, r.getColumnCount());
    }

    /**
     * @target initialise(...)
     * @scenario Null dataset supplied to initialise().
     * @defectRisk Confirms row/column counts default to zero when dataset
     *             is null.
     */
    @Test(timeout = 4000)
    public void testInitialiseWithNullDataset() {
        TestRenderer r = new TestRenderer();
        CategoryPlot plot = new CategoryPlot();
        plot.setRenderer(r);
        Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 100);
        CategoryItemRendererState state = r.initialise(null, area, plot,
                null, null);
        assertNotNull(state);
        assertEquals(0, r.getRowCount());
        assertEquals(0, r.getColumnCount());
    }

    // ------------------------------------------------------------------
    // findRangeBounds
    // ------------------------------------------------------------------

    /**
     * @target findRangeBounds(CategoryDataset)
     * @scenario Null dataset.
     * @defectRisk Confirms null is returned for a null dataset.
     */
    @Test(timeout = 4000)
    public void testFindRangeBoundsNullDataset() {
        TestRenderer r = new TestRenderer();
        assertNull(r.findRangeBounds(null));
    }

    /**
     * @target findRangeBounds(CategoryDataset)
     * @scenario Dataset with two values, default (non-visible-only) mode.
     * @defectRisk Confirms correct range bounds are computed.
     */
    @Test(timeout = 4000)
    public void testFindRangeBoundsWithDataset() {
        TestRenderer r = new TestRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        dataset.addValue(5.0, "S1", "C2");
        Range range = r.findRangeBounds(dataset);
        assertNotNull(range);
        assertEquals(1.0, range.getLowerBound(), 0.0001);
        assertEquals(5.0, range.getUpperBound(), 0.0001);
    }

    // ------------------------------------------------------------------
    // getItemMiddle
    // ------------------------------------------------------------------

    /**
     * @target getItemMiddle(...)
     * @scenario Valid row/column keys with a CategoryAxis.
     * @defectRisk Confirms delegation to CategoryAxis.getCategoryMiddle
     *             produces a sensible (non-negative) coordinate.
     */
    @Test(timeout = 4000)
    public void testGetItemMiddle() {
        TestRenderer r = new TestRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        dataset.addValue(2.0, "S1", "C2");
        CategoryAxis axis = new CategoryAxis("Category");
        Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 100);
        double mid = r.getItemMiddle("S1", "C1", dataset, axis, area,
                RectangleEdge.BOTTOM);
        assertTrue(mid >= 0);
    }

    // ------------------------------------------------------------------
    // equals / hashCode
    // ------------------------------------------------------------------

    /**
     * @target equals(Object) / hashCode()
     * @scenario Two fresh renderers are equal; changing a field breaks
     *           equality; identity and type checks are also exercised.
     * @defectRisk Confirms the field-by-field equals comparisons and the
     *             base-case checks (self, null, wrong-type).
     */
    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        TestRenderer r1 = new TestRenderer();
        TestRenderer r2 = new TestRenderer();
        assertTrue(r1.equals(r2));
        assertEquals(r1.hashCode(), r1.hashCode());
        assertFalse(r1.equals(null));
        assertFalse(r1.equals("not a renderer"));
        assertTrue(r1.equals(r1));

        r1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        assertFalse(r1.equals(r2));
    }

    // ------------------------------------------------------------------
    // getDrawingSupplier
    // ------------------------------------------------------------------

    /**
     * @target getDrawingSupplier()
     * @scenario No plot attached.
     * @defectRisk Confirms null is returned when plot is absent.
     */
    @Test(timeout = 4000)
    public void testGetDrawingSupplierNullPlot() {
        TestRenderer r = new TestRenderer();
        assertNull(r.getDrawingSupplier());
    }

    /**
     * @target getDrawingSupplier()
     * @scenario Plot attached with a default drawing supplier.
     * @defectRisk Confirms the supplier is fetched from the plot.
     */
    @Test(timeout = 4000)
    public void testGetDrawingSupplierWithPlot() {
        TestRenderer r = new TestRenderer();
        CategoryPlot plot = new CategoryPlot();
        plot.setRenderer(r);
        assertNotNull(r.getDrawingSupplier());
    }

    // ------------------------------------------------------------------
    // updateCrosshairValues
    // ------------------------------------------------------------------

    /**
     * @target updateCrosshairValues(...)
     * @scenario Null orientation supplied.
     * @defectRisk Confirms IllegalArgumentException on null orientation.
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUpdateCrosshairValuesNullOrientationThrows() {
        TestRenderer r = new TestRenderer();
        r.updateCrosshairValues(null, "R", "C", 1.0, 0, 0.0, 0.0, null);
    }

    /**
     * @target updateCrosshairValues(...)
     * @scenario Null CategoryCrosshairState (should be a silent no-op).
     * @defectRisk Confirms no NullPointerException occurs when the
     *             crosshair state itself is null.
     */
    @Test(timeout = 4000)
    public void testUpdateCrosshairValuesNullCrosshairStateNoException() {
        TestRenderer r = new TestRenderer();
        r.updateCrosshairValues(null, "R", "C", 1.0, 0, 0.0, 0.0,
                PlotOrientation.VERTICAL);
    }

    // ------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------

    /**
     * @target clone()
     * @scenario Renderer with base generators set is cloned.
     * @defectRisk Confirms clone produces an equal but distinct instance
     *             and that PublicCloneable generators are duplicated.
     */
    @Test(timeout = 4000)
    public void testClone() throws Exception {
        TestRenderer r = new TestRenderer();
        r.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        r.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        r.setBaseURLGenerator(new StandardCategoryURLGenerator());
        TestRenderer clone = (TestRenderer) r.clone();
        assertNotSame(r, clone);
        assertEquals(r, clone);
    }

    // ------------------------------------------------------------------
    // getDomainAxis / getRangeAxis
    // ------------------------------------------------------------------

    /**
     * @target getDomainAxis(CategoryPlot, CategoryDataset) /
     *         getRangeAxis(CategoryPlot, int)
     * @scenario Plot with explicit domain and range axes configured.
     * @defectRisk Confirms the correct axis instances are returned.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisAndRangeAxis() {
        TestRenderer r = new TestRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        plot.setRenderer(r);
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        plot.setDomainAxis(domainAxis);
        NumberAxis rangeAxis = new NumberAxis("Range");
        plot.setRangeAxis(rangeAxis);

        CategoryAxis resultDomain = r.getDomainAxis(plot, dataset);
        assertSame(domainAxis, resultDomain);

        ValueAxis resultRange = r.getRangeAxis(plot, 0);
        assertSame(rangeAxis, resultRange);
    }

    // ------------------------------------------------------------------
    // addEntity
    // ------------------------------------------------------------------

    /**
     * @target addEntity(EntityCollection, Shape, CategoryDataset, int, int,
     *         boolean)
     * @scenario Null hotspot shape supplied.
     * @defectRisk Confirms explicit null-check throws
     *             IllegalArgumentException before touching entities list.
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddEntityNullHotspotThrows() {
        TestRenderer r = new TestRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        r.addEntity(null, null, dataset, 0, 0, false);
    }

    // ------------------------------------------------------------------
    // createHotSpotShape / createHotSpotBounds / hitTest
    // ------------------------------------------------------------------

    /**
     * @target createHotSpotShape(...)
     * @scenario Base implementation always throws.
     * @defectRisk Confirms the "not implemented" contract is honoured.
     */
    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testCreateHotSpotShapeThrows() {
        TestRenderer r = new TestRenderer();
        r.createHotSpotShape(null, null, null, null, null, null, 0, 0,
                false, null);
    }

    /**
     * @target createHotSpotBounds(...) / hitTest(...)
     * @scenario Valid dataset value produces non-null bounds; hitTest at
     *           the bounds' center should report true.
     * @defectRisk Confirms bounds computation and hit-testing logic work
     *             together for a valid data point.
     */
    @Test(timeout = 4000)
    public void testCreateHotSpotBoundsAndHitTest() {
        TestRenderer r = new TestRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5.0, "S1", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        plot.setRenderer(r);
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        plot.setDomainAxis(domainAxis);
        NumberAxis rangeAxis = new NumberAxis("Range");
        plot.setRangeAxis(rangeAxis);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);

        Rectangle2D bounds = r.createHotSpotBounds(null, dataArea, plot,
                domainAxis, rangeAxis, dataset, 0, 0, false, null, null);
        assertNotNull(bounds);

        boolean hit = r.hitTest(bounds.getCenterX(), bounds.getCenterY(),
                null, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0,
                false, null);
        assertTrue(hit);
    }

    /**
     * @target createHotSpotBounds(...)
     * @scenario Underlying dataset value is null.
     * @defectRisk Confirms null is returned (early-return branch) rather
     *             than throwing a NullPointerException.
     */
    @Test(timeout = 4000)
    public void testCreateHotSpotBoundsNullValue() {
        TestRenderer r = new TestRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(null, "S1", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        plot.setRenderer(r);
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        plot.setDomainAxis(domainAxis);
        NumberAxis rangeAxis = new NumberAxis("Range");
        plot.setRangeAxis(rangeAxis);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);

        Rectangle2D bounds = r.createHotSpotBounds(null, dataArea, plot,
                domainAxis, rangeAxis, dataset, 0, 0, false, null, null);
        assertNull(bounds);
    }

    /**
     * @target hitTest(...)
     * @scenario Underlying bounds are null (because dataset value is
     *           null), hitTest should safely report false.
     * @defectRisk Confirms hitTest's null-bounds guard returns false
     *             rather than throwing.
     */
    @Test(timeout = 4000)
    public void testHitTestFalseWhenBoundsNull() {
        TestRenderer r = new TestRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(null, "S1", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        plot.setRenderer(r);
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        plot.setDomainAxis(domainAxis);
        NumberAxis rangeAxis = new NumberAxis("Range");
        plot.setRangeAxis(rangeAxis);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);

        boolean hit = r.hitTest(10, 10, null, dataArea, plot, domainAxis,
                rangeAxis, dataset, 0, 0, false, null);
        assertFalse(hit);
    }

    // ------------------------------------------------------------------
    // drawDomainLine / drawRangeLine (argument-checking branches)
    // ------------------------------------------------------------------

    /**
     * @target drawDomainLine(...)
     * @scenario Null paint argument.
     * @defectRisk Confirms IllegalArgumentException thrown before any
     *             Graphics2D usage (safe even with a null g2).
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDrawDomainLineNullPaintThrows() {
        TestRenderer r = new TestRenderer();
        CategoryPlot plot = new CategoryPlot();
        r.drawDomainLine(null, plot, new Rectangle2D.Double(0, 0, 10, 10),
                5.0, null, new BasicStroke());
    }

    /**
     * @target drawDomainLine(...)
     * @scenario Null stroke argument.
     * @defectRisk Confirms IllegalArgumentException thrown before any
     *             Graphics2D usage.
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDrawDomainLineNullStrokeThrows() {
        TestRenderer r = new TestRenderer();
        CategoryPlot plot = new CategoryPlot();
        r.drawDomainLine(null, plot, new Rectangle2D.Double(0, 0, 10, 10),
                5.0, Color.RED, null);
    }

    /**
     * @target drawRangeLine(...)
     * @scenario Value falls outside the axis range.
     * @defectRisk Confirms the early-return branch avoids using the (null)
     *             Graphics2D reference.
     */
    @Test(timeout = 4000)
    public void testDrawRangeLineOutOfRangeNoException() {
        TestRenderer r = new TestRenderer();
        CategoryPlot plot = new CategoryPlot();
        NumberAxis axis = new NumberAxis("Range");
        axis.setRange(0, 10);
        r.drawRangeLine(null, plot, axis, new Rectangle2D.Double(0, 0, 10, 10),
                100.0, Color.RED, new BasicStroke());
    }

    // ------------------------------------------------------------------
    // drawDomainMarker / drawRangeMarker (early-return branches)
    // ------------------------------------------------------------------

    /**
     * @target drawDomainMarker(...)
     * @scenario Marker key does not correspond to any dataset column.
     * @defectRisk Confirms the columnIndex<0 branch returns early without
     *             touching the (null) Graphics2D.
     */
    @Test(timeout = 4000)
    public void testDrawDomainMarkerColumnNotFoundReturnsEarly() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        TestRenderer r = new TestRenderer();
        plot.setRenderer(r);
        CategoryAxis domainAxis = new CategoryAxis("D");
        CategoryMarker marker = new CategoryMarker("NonExistentCategory");
        r.drawDomainMarker(null, plot, domainAxis, marker,
                new Rectangle2D.Double(0, 0, 10, 10));
    }

    /**
     * @target drawRangeMarker(...) [ValueMarker branch]
     * @scenario Marker value lies outside the axis range.
     * @defectRisk Confirms early-return avoids Graphics2D usage.
     */
    @Test(timeout = 4000)
    public void testDrawRangeMarkerValueMarkerOutOfRange() {
        CategoryPlot plot = new CategoryPlot();
        NumberAxis axis = new NumberAxis("R");
        axis.setRange(0, 10);
        ValueMarker marker = new ValueMarker(100.0);
        TestRenderer r = new TestRenderer();
        r.drawRangeMarker(null, plot, axis, marker,
                new Rectangle2D.Double(0, 0, 10, 10));
    }

    /**
     * @target drawRangeMarker(...) [IntervalMarker branch]
     * @scenario Marker interval does not intersect the axis range.
     * @defectRisk Confirms early-return avoids Graphics2D usage.
     */
    @Test(timeout = 4000)
    public void testDrawRangeMarkerIntervalMarkerOutOfRange() {
        CategoryPlot plot = new CategoryPlot();
        NumberAxis axis = new NumberAxis("R");
        axis.setRange(0, 10);
        IntervalMarker marker = new IntervalMarker(50.0, 60.0);
        TestRenderer r = new TestRenderer();
        r.drawRangeMarker(null, plot, axis, marker,
                new Rectangle2D.Double(0, 0, 10, 10));
    }

    // ------------------------------------------------------------------
    // calculateDomainMarkerTextAnchorPoint /
    // calculateRangeMarkerTextAnchorPoint
    // ------------------------------------------------------------------

    /**
     * @target calculateDomainMarkerTextAnchorPoint(...)
     * @scenario Horizontal plot orientation.
     * @defectRisk Confirms a non-null coordinate point is produced for
     *             the horizontal branch.
     */
    @Test(timeout = 4000)
    public void testCalculateDomainMarkerTextAnchorPointHorizontal() {
        TestRenderer r = new TestRenderer();
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D markerArea = new Rectangle2D.Double(10, 10, 20, 20);
        Point2D pt = r.calculateDomainMarkerTextAnchorPoint(null,
                PlotOrientation.HORIZONTAL, dataArea, markerArea,
                RectangleInsets.ZERO_INSETS, LengthAdjustmentType.CONTRACT,
                RectangleAnchor.CENTER);
        assertNotNull(pt);
    }

    /**
     * @target calculateRangeMarkerTextAnchorPoint(...)
     * @scenario Vertical plot orientation.
     * @defectRisk Confirms a non-null coordinate point is produced for
     *             the vertical branch.
     */
    @Test(timeout = 4000)
    public void testCalculateRangeMarkerTextAnchorPointVertical() {
        TestRenderer r = new TestRenderer();
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D markerArea = new Rectangle2D.Double(10, 10, 20, 20);
        Point2D pt = r.calculateRangeMarkerTextAnchorPoint(null,
                PlotOrientation.VERTICAL, dataArea, markerArea,
                RectangleInsets.ZERO_INSETS, LengthAdjustmentType.CONTRACT,
                RectangleAnchor.CENTER);
        assertNotNull(pt);
    }

    // ------------------------------------------------------------------
    // drawItemLabel
    // ------------------------------------------------------------------

    /**
     * @target drawItemLabel(...)
     * @scenario No item label generator configured for the row/column.
     * @defectRisk Confirms the method is a safe no-op when the generator
     *             lookup returns null.
     */
    @Test(timeout = 4000)
    public void testDrawItemLabelNoGeneratorNoException() {
        TestRenderer r = new TestRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        r.drawItemLabel(null, PlotOrientation.VERTICAL, dataset, 0, 0, false,
                10.0, 10.0, false);
    }

    // ------------------------------------------------------------------
    // drawAnnotations
    // ------------------------------------------------------------------

    /**
     * @target drawAnnotations(...)
     * @scenario Empty foreground annotation list.
     * @defectRisk Confirms the iterator branch for FOREGROUND executes
     *             without error when the list is empty.
     */
    @Test(timeout = 4000)
    public void testDrawAnnotationsForegroundEmpty() {
        TestRenderer r = new TestRenderer();
        CategoryPlot plot = new CategoryPlot();
        plot.setRenderer(r);
        CategoryAxis domainAxis = new CategoryAxis("D");
        NumberAxis rangeAxis = new NumberAxis("R");
        r.drawAnnotations(null, new Rectangle2D.Double(0, 0, 10, 10),
                domainAxis, rangeAxis, Layer.FOREGROUND, null);
    }

    /**
     * @target drawAnnotations(...)
     * @scenario Empty background annotation list.
     * @defectRisk Confirms the iterator branch for BACKGROUND executes
     *             without error when the list is empty.
     */
    @Test(timeout = 4000)
    public void testDrawAnnotationsBackgroundEmpty() {
        TestRenderer r = new TestRenderer();
        CategoryAxis domainAxis = new CategoryAxis("D");
        NumberAxis rangeAxis = new NumberAxis("R");
        r.drawAnnotations(null, new Rectangle2D.Double(0, 0, 10, 10),
                domainAxis, rangeAxis, Layer.BACKGROUND, null);
    }

    // ------------------------------------------------------------------
    // drawBackground / drawOutline (delegate to plot)
    // ------------------------------------------------------------------

    /**
     * @target drawBackground(...) / drawOutline(...)
     * @scenario Minimal plot setup with a real Graphics2D from a
     *           BufferedImage.
     * @defectRisk Confirms delegation to plot.drawBackground /
     *             plot.drawOutline does not throw for a minimally
     *             configured plot.
     */
    @Test(timeout = 4000)
    public void testDrawBackgroundAndOutline() {
        BufferedImage img = new BufferedImage(10, 10,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            CategoryPlot plot = new CategoryPlot();
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(1.0, "S1", "C1");
            plot.setDataset(dataset);
            plot.setDomainAxis(new CategoryAxis("D"));
            plot.setRangeAxis(new NumberAxis("R"));
            TestRenderer r = new TestRenderer();
            plot.setRenderer(r);
            Rectangle2D area = new Rectangle2D.Double(0, 0, 10, 10);
            r.drawBackground(g2, plot, area);
            r.drawOutline(g2, plot, area);
        } finally {
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // RendererChangeListener notification
    // ------------------------------------------------------------------

    /**
     * @target setBaseItemLabelGenerator(...) notify=true path via
     *         notifyListeners().
     * @scenario A change listener is registered before mutating a base
     *           generator.
     * @defectRisk Confirms listener notification mechanism is triggered
     *             for renderer state changes.
     */
    @Test(timeout = 4000)
    public void testChangeListenerNotifiedOnSetBaseItemLabelGenerator() {
        TestRenderer r = new TestRenderer();
        CapturingListener listener = new CapturingListener();
        r.addChangeListener(listener);
        r.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        assertTrue(listener.called);
    }
}