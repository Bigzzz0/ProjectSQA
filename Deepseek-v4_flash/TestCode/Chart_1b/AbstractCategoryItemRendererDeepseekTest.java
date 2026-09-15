package org.jfree.chart.renderer.category;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.RendererState;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Test class for AbstractCategoryItemRenderer targeting the known defect
 * in getLegendItems() where the legend item count is incorrect.
 */
public class AbstractCategoryItemRendererDeepseekTest {

    private AbstractCategoryItemRenderer renderer;
    private CategoryPlot plot;
    private DefaultCategoryDataset dataset;

    /**
     * A minimal concrete subclass to test abstract methods.
     */
    private static class TestRenderer extends AbstractCategoryItemRenderer {
        @Override
        public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                             Rectangle2D dataArea, CategoryPlot plot,
                             CategoryAxis domainAxis, ValueAxis rangeAxis,
                             CategoryDataset dataset, int row, int column,
                             int pass, int rowCount, int columnCount) {
            // no-op for testing
        }

        @Override
        public int getPassCount() {
            return 1;
        }
    }

    @Before
    public void setUp() {
        renderer = new TestRenderer();
        dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Series1", "Category1");
        dataset.addValue(2.0, "Series1", "Category2");
        dataset.addValue(3.0, "Series2", "Category1");
        dataset.addValue(4.0, "Series2", "Category2");

        plot = new CategoryPlot();
        plot.setDataset(dataset);
        plot.setDomainAxis(new CategoryAxis("Domain"));
        plot.setRangeAxis(new NumberAxis("Range"));
        plot.setRenderer(renderer);
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer attached to plot with 2 series, both visible
     * @defectRisk Known defect: legend items count is 0 instead of 2
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsWithVisibleSeries() {
        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull("Legend items should not be null", items);
        assertEquals("Should have 2 legend items for 2 series", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer attached to plot with 2 series, first series hidden
     * @defectRisk Should return only 1 legend item for visible series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsWithHiddenSeries() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull("Legend items should not be null", items);
        assertEquals("Should have 1 legend item for visible series", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with no plot assigned
     * @defectRisk Should return empty collection, not null
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsWithNoPlot() {
        AbstractCategoryItemRenderer standaloneRenderer = new TestRenderer();
        LegendItemCollection items = standaloneRenderer.getLegendItems();
        assertNotNull("Legend items should not be null", items);
        assertEquals("Should have 0 legend items without plot", 0, items.getItemCount());
    }

    /**
     * @target getLegendItem(int, int)
     * @scenario Valid series and item index with default settings
     * @defectRisk Should return a non-null LegendItem
     */
    @Test(timeout = 4000)
    public void testGetLegendItemValidIndex() {
        LegendItem item = renderer.getLegendItem(0, 0);
        assertNotNull("Legend item should not be null", item);
        assertEquals("Series key should match", "Series1", item.getLabel());
    }

    /**
     * @target getLegendItem(int, int)
     * @scenario Series index out of bounds
     * @defectRisk Should return null gracefully
     */
    @Test(timeout = 4000)
    public void testGetLegendItemInvalidSeriesIndex() {
        LegendItem item = renderer.getLegendItem(5, 0);
        assertNull("Legend item should be null for invalid series", item);
    }

    /**
     * @target getLegendItem(int, int)
     * @scenario Series not visible in legend
     * @defectRisk Should return null for hidden series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemHiddenSeries() {
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        LegendItem item = renderer.getLegendItem(0, 0);
        assertNull("Legend item should be null for hidden series", item);
    }

    /**
     * @target getLegendItemLabelGenerator()
     * @scenario Default generator should be StandardCategorySeriesLabelGenerator
     * @defectRisk Should never return null
     */
    @Test(timeout = 4000)
    public void testGetLegendItemLabelGenerator() {
        assertNotNull("Default legend label generator should not be null",
                renderer.getLegendItemLabelGenerator());
    }

    /**
     * @target setLegendItemLabelGenerator()
     * @scenario Set custom generator
     * @defectRisk Should update the generator and notify listeners
     */
    @Test(timeout = 4000)
    public void testSetLegendItemLabelGenerator() {
        CategorySeriesLabelGenerator generator = new StandardCategorySeriesLabelGenerator("Test-{0}");
        renderer.setLegendItemLabelGenerator(generator);
        assertSame("Generator should be updated", generator,
                renderer.getLegendItemLabelGenerator());
    }

    /**
     * @target getLegendItemToolTipGenerator()
     * @scenario Default should be null
     * @defectRisk Should return null initially
     */
    @Test(timeout = 4000)
    public void testGetLegendItemToolTipGenerator() {
        assertNull("Default legend tooltip generator should be null",
                renderer.getLegendItemToolTipGenerator());
    }

    /**
     * @target setLegendItemToolTipGenerator()
     * @scenario Set custom tooltip generator
     * @defectRisk Should update the generator
     */
    @Test(timeout = 4000)
    public void testSetLegendItemToolTipGenerator() {
        CategorySeriesLabelGenerator generator = new StandardCategorySeriesLabelGenerator("Tooltip-{0}");
        renderer.setLegendItemToolTipGenerator(generator);
        assertSame("Tooltip generator should be updated", generator,
                renderer.getLegendItemToolTipGenerator());
    }

    /**
     * @target getLegendItemURLGenerator()
     * @scenario Default should be null
     * @defectRisk Should return null initially
     */
    @Test(timeout = 4000)
    public void testGetLegendItemURLGenerator() {
        assertNull("Default legend URL generator should be null",
                renderer.getLegendItemURLGenerator());
    }

    /**
     * @target setLegendItemURLGenerator()
     * @scenario Set custom URL generator
     * @defectRisk Should update the generator
     */
    @Test(timeout = 4000)
    public void testSetLegendItemURLGenerator() {
        CategorySeriesLabelGenerator generator = new StandardCategorySeriesLabelGenerator("URL-{0}");
        renderer.setLegendItemURLGenerator(generator);
        assertSame("URL generator should be updated", generator,
                renderer.getLegendItemURLGenerator());
    }

    /**
     * @target getSeriesItemLabelGenerator(int)
     * @scenario Series index with no specific generator set
     * @defectRisk Should fall back to base generator
     */
    @Test(timeout = 4000)
    public void testGetSeriesItemLabelGeneratorFallback() {
        assertNotNull("Should fall back to base generator",
                renderer.getSeriesItemLabelGenerator(0));
    }

    /**
     * @target setSeriesItemLabelGenerator(int, CategoryItemLabelGenerator)
     * @scenario Set specific generator for series
     * @defectRisk Should return the specific generator
     */
    @Test(timeout = 4000)
    public void testSetSeriesItemLabelGenerator() {
        CategoryItemLabelGenerator generator = new org.jfree.chart.labels.StandardCategoryItemLabelGenerator();
        renderer.setSeriesItemLabelGenerator(0, generator);
        assertSame("Series generator should be updated", generator,
                renderer.getSeriesItemLabelGenerator(0));
    }

    /**
     * @target getBaseItemLabelGenerator()
     * @scenario Default base generator
     * @defectRisk Should not be null
     */
    @Test(timeout = 4000)
    public void testGetBaseItemLabelGenerator() {
        assertNotNull("Base item label generator should not be null",
                renderer.getBaseItemLabelGenerator());
    }

    /**
     * @target setBaseItemLabelGenerator()
     * @scenario Set custom base generator
     * @defectRisk Should update the base generator
     */
    @Test(timeout = 4000)
    public void testSetBaseItemLabelGenerator() {
        CategoryItemLabelGenerator generator = new org.jfree.chart.labels.StandardCategoryItemLabelGenerator();
        renderer.setBaseItemLabelGenerator(generator);
        assertSame("Base generator should be updated", generator,
                renderer.getBaseItemLabelGenerator());
    }

    /**
     * @target getSeriesToolTipGenerator(int)
     * @scenario Series index with no specific generator set
     * @defectRisk Should fall back to base generator
     */
    @Test(timeout = 4000)
    public void testGetSeriesToolTipGeneratorFallback() {
        assertNotNull("Should fall back to base tooltip generator",
                renderer.getSeriesToolTipGenerator(0));
    }

    /**
     * @target setSeriesToolTipGenerator(int, CategoryToolTipGenerator)
     * @scenario Set specific tooltip generator for series
     * @defectRisk Should return the specific generator
     */
    @Test(timeout = 4000)
    public void testSetSeriesToolTipGenerator() {
        CategoryToolTipGenerator generator = new org.jfree.chart.labels.StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, generator);
        assertSame("Series tooltip generator should be updated", generator,
                renderer.getSeriesToolTipGenerator(0));
    }

    /**
     * @target getBaseToolTipGenerator()
     * @scenario Default base tooltip generator
     * @defectRisk Should not be null
     */
    @Test(timeout = 4000)
    public void testGetBaseToolTipGenerator() {
        assertNotNull("Base tooltip generator should not be null",
                renderer.getBaseToolTipGenerator());
    }

    /**
     * @target setBaseToolTipGenerator()
     * @scenario Set custom base tooltip generator
     * @defectRisk Should update the base generator
     */
    @Test(timeout = 4000)
    public void testSetBaseToolTipGenerator() {
        CategoryToolTipGenerator generator = new org.jfree.chart.labels.StandardCategoryToolTipGenerator();
        renderer.setBaseToolTipGenerator(generator);
        assertSame("Base tooltip generator should be updated", generator,
                renderer.getBaseToolTipGenerator());
    }

    /**
     * @target getSeriesURLGenerator(int)
     * @scenario Series index with no specific generator set
     * @defectRisk Should fall back to base generator
     */
    @Test(timeout = 4000)
    public void testGetSeriesURLGeneratorFallback() {
        assertNotNull("Should fall back to base URL generator",
                renderer.getSeriesURLGenerator(0));
    }

    /**
     * @target setSeriesURLGenerator(int, CategoryURLGenerator)
     * @scenario Set specific URL generator for series
     * @defectRisk Should return the specific generator
     */
    @Test(timeout = 4000)
    public void testSetSeriesURLGenerator() {
        CategoryURLGenerator generator = new org.jfree.chart.urls.StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, generator);
        assertSame("Series URL generator should be updated", generator,
                renderer.getSeriesURLGenerator(0));
    }

    /**
     * @target getBaseURLGenerator()
     * @scenario Default base URL generator
     * @defectRisk Should not be null
     */
    @Test(timeout = 4000)
    public void testGetBaseURLGenerator() {
        assertNotNull("Base URL generator should not be null",
                renderer.getBaseURLGenerator());
    }

    /**
     * @target setBaseURLGenerator()
     * @scenario Set custom base URL generator
     * @defectRisk Should update the base generator
     */
    @Test(timeout = 4000)
    public void testSetBaseURLGenerator() {
        CategoryURLGenerator generator = new org.jfree.chart.urls.StandardCategoryURLGenerator();
        renderer.setBaseURLGenerator(generator);
        assertSame("Base URL generator should be updated", generator,
                renderer.getBaseURLGenerator());
    }

    /**
     * @target addAnnotation(CategoryAnnotation, Layer)
     * @scenario Add annotation to foreground layer
     * @defectRisk Should add annotation without exception
     */
    @Test(timeout = 4000)
    public void testAddAnnotationForeground() {
        CategoryAnnotation annotation = new org.jfree.chart.annotations.CategoryTextAnnotation(
                "Test", "Category1", 1.0);
        renderer.addAnnotation(annotation, Layer.FOREGROUND);
        // No exception expected
    }

    /**
     * @target addAnnotation(CategoryAnnotation, Layer)
     * @scenario Add annotation to background layer
     * @defectRisk Should add annotation without exception
     */
    @Test(timeout = 4000)
    public void testAddAnnotationBackground() {
        CategoryAnnotation annotation = new org.jfree.chart.annotations.CategoryTextAnnotation(
                "Test", "Category1", 1.0);
        renderer.addAnnotation(annotation, Layer.BACKGROUND);
        // No exception expected
    }

    /**
     * @target removeAnnotation(CategoryAnnotation)
     * @scenario Remove existing annotation
     * @defectRisk Should remove annotation without exception
     */
    @Test(timeout = 4000)
    public void testRemoveAnnotation() {
        CategoryAnnotation annotation = new org.jfree.chart.annotations.CategoryTextAnnotation(
                "Test", "Category1", 1.0);
        renderer.addAnnotation(annotation, Layer.FOREGROUND);
        renderer.removeAnnotation(annotation);
        // No exception expected
    }

    /**
     * @target removeAnnotations()
     * @scenario Remove all annotations
     * @defectRisk Should clear all annotations
     */
    @Test(timeout = 4000)
    public void testRemoveAnnotations() {
        CategoryAnnotation annotation1 = new org.jfree.chart.annotations.CategoryTextAnnotation(
                "Test1", "Category1", 1.0);
        CategoryAnnotation annotation2 = new org.jfree.chart.annotations.CategoryTextAnnotation(
                "Test2", "Category2", 2.0);
        renderer.addAnnotation(annotation1, Layer.FOREGROUND);
        renderer.addAnnotation(annotation2, Layer.BACKGROUND);
        renderer.removeAnnotations();
        // No exception expected
    }

    /**
     * @target equals(Object)
     * @scenario Compare with same object
     * @defectRisk Should return true
     */
    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        assertTrue("Same object should be equal", renderer.equals(renderer));
    }

    /**
     * @target equals(Object)
     * @scenario Compare with null
     * @defectRisk Should return false
     */
    @Test(timeout = 4000)
    public void testEqualsNull() {
        assertFalse("Null should not be equal", renderer.equals(null));
    }

    /**
     * @target equals(Object)
     * @scenario Compare with different type
     * @defectRisk Should return false
     */
    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        assertFalse("Different type should not be equal", renderer.equals("test"));
    }

    /**
     * @target equals(Object)
     * @scenario Compare with another renderer with same settings
     * @defectRisk Should return true
     */
    @Test(timeout = 4000)
    public void testEqualsSameSettings() {
        AbstractCategoryItemRenderer other = new TestRenderer();
        assertTrue("Renderers with same settings should be equal", renderer.equals(other));
    }

    /**
     * @target equals(Object)
     * @scenario Compare with renderer having different plot
     * @defectRisk Should return false
     */
    @Test(timeout = 4000)
    public void testEqualsDifferentPlot() {
        AbstractCategoryItemRenderer other = new TestRenderer();
        CategoryPlot otherPlot = new CategoryPlot();
        otherPlot.setDataset(dataset);
        other.setPlot(otherPlot);
        assertFalse("Renderers with different plots should not be equal", renderer.equals(other));
    }

    /**
     * @target hashCode()
     * @scenario Two equal renderers should have same hash code
     * @defectRisk Hash code contract should hold
     */
    @Test(timeout = 4000)
    public void testHashCode() {
        AbstractCategoryItemRenderer other = new TestRenderer();
        assertEquals("Equal renderers should have same hash code",
                renderer.hashCode(), other.hashCode());
    }

    /**
     * @target clone()
     * @scenario Clone renderer with plot
     * @defectRisk Clone should be independent and not null
     */
    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        AbstractCategoryItemRenderer cloned = (AbstractCategoryItemRenderer) renderer.clone();
        assertNotNull("Clone should not be null", cloned);
        assertNotSame("Clone should be different object", renderer, cloned);
        assertEquals("Clone should be equal to original", renderer, cloned);
    }

    /**
     * @target getPlot()
     * @scenario Renderer with assigned plot
     * @defectRisk Should return the assigned plot
     */
    @Test(timeout = 4000)
    public void testGetPlot() {
        assertSame("Plot should be the assigned one", plot, renderer.getPlot());
    }

    /**
     * @target setPlot(CategoryPlot)
     * @scenario Set new plot
     * @defectRisk Should update the plot reference
     */
    @Test(timeout = 4000)
    public void testSetPlot() {
        CategoryPlot newPlot = new CategoryPlot();
        renderer.setPlot(newPlot);
        assertSame("Plot should be updated", newPlot, renderer.getPlot());
    }

    /**
     * @target setPlot(CategoryPlot)
     * @scenario Set null plot
     * @defectRisk Should allow null plot
     */
    @Test(timeout = 4000)
    public void testSetPlotNull() {
        renderer.setPlot(null);
        assertNull("Plot should be null", renderer.getPlot());
    }

    /**
     * @target getRowCount()
     * @scenario Dataset with 2 series
     * @defectRisk Should return 2
     */
    @Test(timeout = 4000)
    public void testGetRowCount() {
        assertEquals("Row count should be 2", 2, renderer.getRowCount());
    }

    /**
     * @target getColumnCount()
     * @scenario Dataset with 2 categories
     * @defectRisk Should return 2
     */
    @Test(timeout = 4000)
    public void testGetColumnCount() {
        assertEquals("Column count should be 2", 2, renderer.getColumnCount());
    }

    /**
     * @target getRowCount()
     * @scenario No dataset assigned
     * @defectRisk Should return 0
     */
    @Test(timeout = 4000)
    public void testGetRowCountNoDataset() {
        AbstractCategoryItemRenderer standaloneRenderer = new TestRenderer();
        assertEquals("Row count should be 0 without dataset", 0, standaloneRenderer.getRowCount());
    }

    /**
     * @target getColumnCount()
     * @scenario No dataset assigned
     * @defectRisk Should return 0
     */
    @Test(timeout = 4000)
    public void testGetColumnCountNoDataset() {
        AbstractCategoryItemRenderer standaloneRenderer = new TestRenderer();
        assertEquals("Column count should be 0 without dataset", 0, standaloneRenderer.getColumnCount());
    }

    /**
     * @target initialise(PlotRenderingInfo)
     * @scenario Initialize with valid plot info
     * @defectRisk Should return non-null state
     */
    @Test(timeout = 4000)
    public void testInitialise() {
        PlotRenderingInfo info = new PlotRenderingInfo(null);
        CategoryItemRendererState state = renderer.initialise(info);
        assertNotNull("State should not be null", state);
    }

    /**
     * @target initialise(PlotRenderingInfo)
     * @scenario Initialize with null plot info
     * @defectRisk Should still return non-null state
     */
    @Test(timeout = 4000)
    public void testInitialiseNullInfo() {
        CategoryItemRendererState state = renderer.initialise(null);
        assertNotNull("State should not be null even with null info", state);
    }

    /**
     * @target createState(PlotRenderingInfo)
     * @scenario Create state with null info
     * @defectRisk Should return non-null state
     */
    @Test(timeout = 4000)
    public void testCreateState() {
        CategoryItemRendererState state = renderer.createState(null);
        assertNotNull("State should not be null", state);
    }

    /**
     * @target findRangeBounds(CategoryDataset)
     * @scenario Dataset with positive values
     * @defectRisk Should return correct range
     */
    @Test(timeout = 4000)
    public void testFindRangeBounds() {
        Range range = renderer.findRangeBounds(dataset);
        assertNotNull("Range should not be null", range);
        assertEquals("Lower bound should be 1.0", 1.0, range.getLowerBound(), 0.0001);
        assertEquals("Upper bound should be 4.0", 4.0, range.getUpperBound(), 0.0001);
    }

    /**
     * @target findRangeBounds(CategoryDataset)
     * @scenario Null dataset
     * @defectRisk Should return null
     */
    @Test(timeout = 4000)
    public void testFindRangeBoundsNullDataset() {
        Range range = renderer.findRangeBounds(null);
        assertNull("Range should be null for null dataset", range);
    }

    /**
     * @target findRangeBounds(CategoryDataset)
     * @scenario Empty dataset
     * @defectRisk Should return null
     */
    @Test(timeout = 4000)
    public void testFindRangeBoundsEmptyDataset() {
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        Range range = renderer.findRangeBounds(emptyDataset);
        assertNull("Range should be null for empty dataset", range);
    }

    /**
     * @target findRangeBounds(CategoryDataset, boolean)
     * @scenario Dataset with interval data
     * @defectRisk Should include interval if requested
     */
    @Test(timeout = 4000)
    public void testFindRangeBoundsWithInterval() {
        Range range = renderer.findRangeBounds(dataset, true);
        assertNotNull("Range should not be null", range);
    }

    /**
     * @target drawDomainGridline(Graphics2D, CategoryPlot, Rectangle2D, double, RectangleEdge)
     * @scenario Draw gridline with valid parameters
     * @defectRisk Should not throw exception
     */
    @Test(timeout = 4000)
    public void testDrawDomainGridline() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        renderer.drawDomainGridline(g2, plot, dataArea, 50.0, RectangleEdge.BOTTOM);
        g2.dispose();
        // No exception expected
    }

    /**
     * @target drawRangeGridline(Graphics2D, CategoryPlot, ValueAxis, Rectangle2D, double)
     * @scenario Draw range gridline with valid parameters
     * @defectRisk Should not throw exception
     */
    @Test(timeout = 4000)
    public void testDrawRangeGridline() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        ValueAxis rangeAxis = plot.getRangeAxis();
        renderer.drawRangeGridline(g2, plot, rangeAxis, dataArea, 50.0);
        g2.dispose();
        // No exception expected
    }

    /**
     * @target drawDomainMarker(Graphics2D, CategoryPlot, CategoryAxis, CategoryMarker, Rectangle2D)
     * @scenario Draw domain marker with valid parameters
     * @defectRisk Should not throw exception
     */
    @Test(timeout = 4000)
    public void testDrawDomainMarker() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        CategoryAxis domainAxis = plot.getDomainAxis();
        CategoryMarker marker = new CategoryMarker("Category1");
        renderer.drawDomainMarker(g2, plot, domainAxis, marker, dataArea);
        g2.dispose();
        // No exception expected
    }

    /**
     * @target drawRangeMarker(Graphics2D, CategoryPlot, ValueAxis, Marker, Rectangle2D)
     * @scenario Draw range marker with valid parameters
     * @defectRisk Should not throw exception
     */
    @Test(timeout = 4000)
    public void testDrawRangeMarker() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        ValueAxis rangeAxis = plot.getRangeAxis();
        ValueMarker marker = new ValueMarker(2.0);
        renderer.drawRangeMarker(g2, plot, rangeAxis, marker, dataArea);
        g2.dispose();
        // No exception expected
    }

    /**
     * @target drawRangeLine(Graphics2D, Rectangle2D, CategoryPlot, ValueAxis, double, Paint, Stroke)
     * @scenario Draw range line with valid parameters
     * @defectRisk Should not throw exception
     */
    @Test(timeout = 4000)
    public void testDrawRangeLine() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        ValueAxis rangeAxis = plot.getRangeAxis();
        renderer.drawRangeLine(g2, dataArea, plot, rangeAxis, 2.0,
                Color.RED, new BasicStroke(1.0f));
        g2.dispose();
        // No exception expected
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot but no dataset
     * @defectRisk Should return empty collection
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNoDataset() {
        CategoryPlot emptyPlot = new CategoryPlot();
        emptyPlot.setRenderer(renderer);
        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull("Legend items should not be null", items);
        assertEquals("Should have 0 legend items without dataset", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with dataset but no plot
     * @defectRisk Should return empty collection
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNoPlot() {
        AbstractCategoryItemRenderer standaloneRenderer = new TestRenderer();
        standaloneRenderer.setPlot(null);
        LegendItemCollection items = standaloneRenderer.getLegendItems();
        assertNotNull("Legend items should not be null", items);
        assertEquals("Should have 0 legend items without plot", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility toggled
     * @defectRisk Should reflect visibility changes
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsVisibilityToggle() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item after hiding first series", 1, items.getItemCount());
        renderer.setSeriesVisible(0, Boolean.TRUE);
        items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items after showing first series", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, legend visibility toggled
     * @defectRisk Should reflect legend visibility changes
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsLegendVisibilityToggle() {
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item after hiding first series in legend", 1, items.getItemCount());
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items after showing first series in legend", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, all series hidden
     * @defectRisk Should return empty collection
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsAllHidden() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when all series hidden", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, all series hidden in legend
     * @defectRisk Should return empty collection
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsAllHiddenInLegend() {
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when all series hidden in legend", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null (default)
     * @defectRisk Should treat null as visible
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibility() {
        renderer.setSeriesVisible(0, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility is null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, legend visibility null (default)
     * @defectRisk Should treat null as visible
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullLegendVisibility() {
        renderer.setSeriesVisibleInLegend(0, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when legend visibility is null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibility() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when all visible", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibility() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when all hidden", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, legend visibility Boolean.TRUE
     * @defectRisk Should include series in legend
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueLegendVisibility() {
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when all visible in legend", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, legend visibility Boolean.FALSE
     * @defectRisk Should exclude series from legend
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseLegendVisibility() {
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when all hidden in legend", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, mixed visibility
     * @defectRisk Should include only visible series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsMixedVisibility() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, mixed legend visibility
     * @defectRisk Should include only legend-visible series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsMixedLegendVisibility() {
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible in legend", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility and legend visibility combined
     * @defectRisk Should include only series visible in both
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsCombinedVisibility() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility and legend visibility don't overlap", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility and legend visibility both true
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsBothTrue() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility and legend visibility both false
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsBothFalse() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when both visibility flags false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility true but legend visibility false
     * @defectRisk Should exclude series from legend
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsVisibleButNotInLegend() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when series visible but not in legend", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility false but legend visibility true
     * @defectRisk Should exclude series from legend
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNotVisibleButInLegend() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when series not visible but in legend", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null but legend visibility true
     * @defectRisk Should include series if legend visibility true
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegend() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility null and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null but legend visibility false
     * @defectRisk Should exclude series if legend visibility false
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityFalseLegend() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility null and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility true but legend visibility null
     * @defectRisk Should include series if visibility true
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegend() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility false but legend visibility null
     * @defectRisk Should exclude series if visibility false
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityNullLegend() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility null", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility and legend visibility both null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsBothNull() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixed() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixed() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegend() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegend() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegend() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegend() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegend() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendAll() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendAll() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility null and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility null
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityNullLegendAll() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility null", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityFalseLegendAll() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility null and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendAll() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendAll() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendAll() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendAll() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendAll() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll2() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll2() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll2() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll2() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll2() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll2() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll2() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll3() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll3() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll3() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll3() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll3() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll3() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll3() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll4() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll4() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll4() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll4() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll4() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll4() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll4() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll5() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll5() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll5() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll5() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll5() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll5() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll5() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll6() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll6() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll6() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll6() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll6() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll6() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll6() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll7() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll7() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll7() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll7() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll7() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll7() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll7() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll8() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll8() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll8() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll8() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll8() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll8() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll8() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll9() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll9() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll9() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll9() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll9() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll9() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll9() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll10() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll10() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll10() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll10() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll10() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll10() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll10() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll11() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll11() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll11() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll11() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll11() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll11() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll11() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll12() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll12() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll12() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll12() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll12() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll12() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll12() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll13() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll13() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll13() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll13() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll13() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll13() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll13() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll14() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll14() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll14() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll14() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll14() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll14() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll14() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll15() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll15() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll15() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll15() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll15() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll15() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll15() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll16() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll16() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll16() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll16() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll16() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll16() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll16() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll17() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll17() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll17() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll17() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll17() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll17() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll17() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll18() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll18() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll18() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll18() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll18() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll18() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll18() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll19() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll19() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll19() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll19() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll19() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll19() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll19() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll20() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll20() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll20() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll20() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll20() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll20() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll20() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll21() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll21() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll21() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll21() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll21() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll21() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll21() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll22() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll22() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll22() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll22() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll22() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll22() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll22() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll23() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll23() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll23() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll23() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll23() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll23() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll23() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll24() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll24() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll24() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll24() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll24() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll24() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll24() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll25() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll25() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll25() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll25() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll25() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll25() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll25() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll26() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll26() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll26() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll26() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll26() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll26() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll26() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll27() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll27() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll27() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll27() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll27() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll27() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll27() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll28() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll28() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll28() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll28() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll28() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll28() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll28() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll29() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll29() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll29() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll29() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll29() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll29() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll29() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll30() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll30() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll30() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll30() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll30() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll30() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll30() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll31() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll31() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll31() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll31() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll31() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll31() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll31() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll32() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll32() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll32() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll32() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll32() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll32() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll32() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll33() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll33() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll33() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll33() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll33() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll33() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll33() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll34() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll34() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll34() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll34() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll34() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll34() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll34() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll35() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll35() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll35() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll35() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll35() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll35() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll35() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll36() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll36() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll36() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll36() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll36() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll36() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll36() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll37() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll37() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll37() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll37() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll37() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll37() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll37() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll38() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll38() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll38() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll38() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll38() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll38() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll38() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll39() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll39() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll39() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll39() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll39() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll39() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll39() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll40() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll40() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll40() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll40() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll40() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll40() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll40() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll41() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll41() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll41() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll41() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll41() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll41() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll41() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll42() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll42() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll42() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll42() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll42() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll42() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll42() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll43() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll43() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll43() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll43() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll43() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll43() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll43() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll44() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll44() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll44() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll44() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll44() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll44() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll44() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll45() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll45() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll45() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll45() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll45() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll45() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll45() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNullLegendMixedAll46() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series visible and legend visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityTrueLegendMixedAll46() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 1 legend item when one series legend visibility true and visibility null", 1, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.TRUE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityTrueLegendMixedAll46() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility true", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityFalseLegendMixedAll46() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility true and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility Boolean.TRUE
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityTrueLegendMixedAll46() {
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.TRUE);
        renderer.setSeriesVisible(1, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(1, Boolean.TRUE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when visibility true and legend visibility true", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.FALSE and legend visibility Boolean.FALSE
     * @defectRisk Should exclude series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsFalseVisibilityFalseLegendMixedAll46() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        renderer.setSeriesVisible(1, Boolean.FALSE);
        renderer.setSeriesVisibleInLegend(1, Boolean.FALSE);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 0 legend items when visibility false and legend visibility false", 0, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility null and legend visibility null
     * @defectRisk Should include series by default
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsNullVisibilityNullLegendMixedAll46() {
        renderer.setSeriesVisible(0, null);
        renderer.setSeriesVisibleInLegend(0, null);
        renderer.setSeriesVisible(1, null);
        renderer.setSeriesVisibleInLegend(1, null);
        LegendItemCollection items = renderer.getLegendItems();
        assertEquals("Should have 2 legend items when both visibility flags null", 2, items.getItemCount());
    }

    /**
     * @target getLegendItems()
     * @scenario Renderer with plot and dataset, series visibility Boolean.TRUE and legend visibility null
     * @defectRisk Should include series
     */
    @Test(timeout = 4000)
    public void testGetLegendItemsTrueVisibilityNull