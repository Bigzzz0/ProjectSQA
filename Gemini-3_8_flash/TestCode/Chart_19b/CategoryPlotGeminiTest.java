package org.jfree.chart.plot;

import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 * Target Class: CategoryPlot (JFreeChart)
 *
 * 1. Defect-Targeted Branches (Defects4J Chart Ground Truth):
 *    - getDomainAxisIndex(CategoryAxis): Javadoc specifies null is not permitted. Defect exists where null
 *      is passed through to ObjectList.indexOf() returning -1 rather than throwing IllegalArgumentException.
 *    - getRangeAxisIndex(ValueAxis): Similarly fails to validate null 'axis' argument against specification.
 *
 * 2. Core Functional Logic & State Transitions:
 *    - Axis management (get/set/clear DomainAxes, RangeAxes, multiple axes at arbitrary indexes).
 *    - Dataset & Renderer mapping (mapDatasetToDomainAxis, mapDatasetToRangeAxis, datasetCount, rendererCount).
 *    - Rendering order (DatasetRenderingOrder FORWARD vs REVERSE; SortOrder for Row & Column).
 *    - Domain/Range Gridlines (toggle visibility, change Stroke and Paint, position anchors).
 *    - Range Crosshair functionality (visibility, lockOnData, values, stroke, paint).
 *    - Markers (add/get/clear CategoryMarker & ValueMarker/IntervalMarker across FOREGROUND and BACKGROUND layers).
 *    - Annotations (add/remove/clear CategoryAnnotation).
 *    - Legend items retrieval (fixed vs dynamic generation).
 *    - Zooming (isDomainZoomable == false, isRangeZoomable == true, zoom(), zoomRangeAxes()).
 *
 * 3. Boundary & Extreme Value Analysis:
 *    - Zero/negative dimension graphics rendering (MINIMUM_WIDTH_TO_DRAW, MINIMUM_HEIGHT_TO_DRAW).
 *    - Empty datasets vs populated datasets rendering.
 *    - Orientation switching (HORIZONTAL vs VERTICAL) affecting line drawing and axis spaces.
 *    - Null index 0 axis locations (prohibited) vs higher index null locations.
 *
 * 4. Object Contract & Lifecycle:
 *    - Full deep-clone integrity test ensuring decoupled state for axes, datasets, renderers, and markers.
 *    - Java Serialization/Deserialization round-trip verifying transient Paint/Stroke restoration and listener wiring.
 *    - Symmetric, reflexive, and unequal branches in equals().
 */
public class CategoryPlotGeminiTest {

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets defect in getDomainAxisIndex(CategoryAxis):
     * Contract requires IllegalArgumentException when argument is null.
     */
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetDomainAxisIndexNullDefect() {
        CategoryPlot plot = new CategoryPlot();
        plot.getDomainAxisIndex(null);
    }

    /**
     * Targets defect in getRangeAxisIndex(ValueAxis):
     * Contract requires IllegalArgumentException when argument is null.
     */
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetRangeAxisIndexNullDefect() {
        CategoryPlot plot = new CategoryPlot();
        plot.getRangeAxisIndex(null);
    }

    @Test(timeout = 4000)
    public void testGetDomainAxisIndexValid() {
        CategoryAxis c1 = new CategoryAxis("C1");
        CategoryAxis c2 = new CategoryAxis("C2");
        CategoryPlot plot = new CategoryPlot();
        plot.setDomainAxis(0, c1);
        plot.setDomainAxis(1, c2);

        assertEquals(0, plot.getDomainAxisIndex(c1));
        assertEquals(1, plot.getDomainAxisIndex(c2));
        assertEquals(-1, plot.getDomainAxisIndex(new CategoryAxis("C3")));
    }

    @Test(timeout = 4000)
    public void testGetRangeAxisIndexValid() {
        NumberAxis a1 = new NumberAxis("A1");
        NumberAxis a2 = new NumberAxis("A2");
        CategoryPlot plot = new CategoryPlot();
        plot.setRangeAxis(0, a1);
        plot.setRangeAxis(1, a2);

        assertEquals(0, plot.getRangeAxisIndex(a1));
        assertEquals(1, plot.getRangeAxisIndex(a2));
        assertEquals(-1, plot.getRangeAxisIndex(new NumberAxis("A3")));
    }

    @Test(timeout = 4000)
    public void testGetRangeAxisIndexParentLookup() {
        NumberAxis a1 = new NumberAxis("ParentAxis");
        CategoryPlot parent = new CategoryPlot();
        parent.setRangeAxis(0, a1);

        CategoryPlot child = new CategoryPlot();
        child.setParent(parent);

        assertEquals(0, child.getRangeAxisIndex(a1));
        assertEquals(-1, child.getRangeAxisIndex(new NumberAxis("NotFound")));
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndInitialState() {
        CategoryPlot plot = new CategoryPlot();

        assertEquals("Category Plot", plot.getPlotType());
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        assertEquals(new RectangleInsets(4.0, 4.0, 4.0, 4.0), plot.getAxisOffset());
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getDomainAxisLocation());
        assertEquals(AxisLocation.TOP_OR_LEFT, plot.getRangeAxisLocation());
        assertFalse(plot.isDomainGridlinesVisible());
        assertTrue(plot.isRangeGridlinesVisible());
        assertEquals(CategoryAnchor.MIDDLE, plot.getDomainGridlinePosition());
        assertFalse(plot.isRangeCrosshairVisible());
        assertTrue(plot.isRangeCrosshairLockedOnData());
        assertEquals(0.0, plot.getRangeCrosshairValue(), 1e-9);
        assertEquals(DatasetRenderingOrder.REVERSE, plot.getDatasetRenderingOrder());
        assertEquals(SortOrder.ASCENDING, plot.getColumnRenderingOrder());
        assertEquals(SortOrder.ASCENDING, plot.getRowRenderingOrder());
        assertFalse(plot.getDrawSharedDomainAxis());
    }

    @Test(timeout = 4000)
    public void testDomainAndRangeAxesConfiguration() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis domainAxis1 = new CategoryAxis("D1");
        CategoryAxis domainAxis2 = new CategoryAxis("D2");
        plot.setDomainAxes(new CategoryAxis[]{domainAxis1, domainAxis2});

        assertEquals(2, plot.getDomainAxisCount());
        assertSame(domainAxis1, plot.getDomainAxis(0));
        assertSame(domainAxis2, plot.getDomainAxis(1));
        assertSame(plot, domainAxis1.getPlot());

        ValueAxis rangeAxis1 = new NumberAxis("R1");
        ValueAxis rangeAxis2 = new NumberAxis("R2");
        plot.setRangeAxes(new ValueAxis[]{rangeAxis1, rangeAxis2});

        assertEquals(2, plot.getRangeAxisCount());
        assertSame(rangeAxis1, plot.getRangeAxis(0));
        assertSame(rangeAxis2, plot.getRangeAxis(1));
        assertSame(plot, rangeAxis1.getPlot());

        plot.clearDomainAxes();
        assertEquals(0, plot.getDomainAxisCount());
        assertNull(plot.getDomainAxis(0));

        plot.clearRangeAxes();
        assertEquals(0, plot.getRangeAxisCount());
        assertNull(plot.getRangeAxis(0));
    }

    @Test(timeout = 4000)
    public void testDatasetAndRendererMapping() {
        DefaultCategoryDataset dataset0 = new DefaultCategoryDataset();
        dataset0.addValue(1.0, "R1", "C1");
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(5.0, "R2", "C2");

        CategoryItemRenderer renderer0 = new BarRenderer();
        CategoryItemRenderer renderer1 = new LineAndShapeRenderer();

        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(0, dataset0);
        plot.setDataset(1, dataset1);
        plot.setRenderers(new CategoryItemRenderer[]{renderer0, renderer1});

        assertEquals(2, plot.getDatasetCount());
        assertSame(dataset0, plot.getDataset(0));
        assertSame(dataset1, plot.getDataset(1));
        assertSame(renderer0, plot.getRenderer(0));
        assertSame(renderer1, plot.getRenderer(1));
        assertSame(renderer0, plot.getRendererForDataset(dataset0));
        assertSame(renderer1, plot.getRendererForDataset(dataset1));
        assertEquals(0, plot.getIndexOf(renderer0));
        assertEquals(1, plot.getIndexOf(renderer1));

        plot.mapDatasetToDomainAxis(1, 1);
        plot.mapDatasetToRangeAxis(1, 1);

        CategoryAxis cAxis1 = new CategoryAxis("Domain1");
        ValueAxis rAxis1 = new NumberAxis("Range1");
        plot.setDomainAxis(1, cAxis1);
        plot.setRangeAxis(1, rAxis1);

        assertSame(cAxis1, plot.getDomainAxisForDataset(1));
        assertSame(rAxis1, plot.getRangeAxisForDataset(1));
    }

    @Test(timeout = 4000)
    public void testMarkerManagement() {
        CategoryPlot plot = new CategoryPlot();
        CategoryMarker domMarker = new CategoryMarker("C1");
        ValueMarker rngMarker = new ValueMarker(15.0);

        plot.addDomainMarker(domMarker, Layer.FOREGROUND);
        plot.addDomainMarker(0, new CategoryMarker("C2"), Layer.BACKGROUND);
        plot.addRangeMarker(rngMarker, Layer.FOREGROUND);
        plot.addRangeMarker(0, new ValueMarker(20.0), Layer.BACKGROUND);

        Collection fgDomain = plot.getDomainMarkers(Layer.FOREGROUND);
        Collection bgDomain = plot.getDomainMarkers(0, Layer.BACKGROUND);
        Collection fgRange = plot.getRangeMarkers(Layer.FOREGROUND);
        Collection bgRange = plot.getRangeMarkers(0, Layer.BACKGROUND);

        assertNotNull(fgDomain);
        assertEquals(1, fgDomain.size());
        assertNotNull(bgDomain);
        assertEquals(1, bgDomain.size());
        assertNotNull(fgRange);
        assertEquals(1, fgRange.size());
        // CategoryPlot constructor adds 1 baseline marker to Layer.BACKGROUND of range markers
        assertNotNull(bgRange);
        assertEquals(2, bgRange.size());

        plot.clearDomainMarkers(0);
        assertTrue(plot.getDomainMarkers(0, Layer.FOREGROUND).isEmpty());
        assertTrue(plot.getDomainMarkers(0, Layer.BACKGROUND).isEmpty());

        plot.clearRangeMarkers(0);
        assertTrue(plot.getRangeMarkers(0, Layer.FOREGROUND).isEmpty());
        assertTrue(plot.getRangeMarkers(0, Layer.BACKGROUND).isEmpty());

        plot.addDomainMarker(domMarker);
        plot.addRangeMarker(rngMarker);
        plot.clearDomainMarkers();
        plot.clearRangeMarkers();
        assertNull(plot.getDomainMarkers(Layer.FOREGROUND));
        assertNull(plot.getRangeMarkers(Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void testAnnotationsManagement() {
        CategoryPlot plot = new CategoryPlot();
        CategoryTextAnnotation annotation1 = new CategoryTextAnnotation("Note1", "C1", 10.0);
        CategoryTextAnnotation annotation2 = new CategoryTextAnnotation("Note2", "C2", 20.0);

        plot.addAnnotation(annotation1);
        plot.addAnnotation(annotation2);

        List annotations = plot.getAnnotations();
        assertEquals(2, annotations.size());
        assertTrue(annotations.contains(annotation1));

        assertTrue(plot.removeAnnotation(annotation1));
        assertFalse(plot.removeAnnotation(annotation1));
        assertEquals(1, plot.getAnnotations().size());

        plot.clearAnnotations();
        assertEquals(0, plot.getAnnotations().size());
    }

    @Test(timeout = 4000)
    public void testZoomMechanics() {
        NumberAxis rangeAxis = new NumberAxis("Y");
        rangeAxis.setRange(0.0, 100.0);
        CategoryPlot plot = new CategoryPlot(null, new CategoryAxis("X"), rangeAxis, null);

        assertFalse(plot.isDomainZoomable());
        assertTrue(plot.isRangeZoomable());

        // Domain zoom should be a no-op and not crash
        plot.zoomDomainAxes(0.5, new PlotRenderingInfo(null), new Point2D.Double(10, 10));
        plot.zoomDomainAxes(0.1, 0.9, new PlotRenderingInfo(null), new Point2D.Double(10, 10));
        plot.zoomDomainAxes(0.5, new PlotRenderingInfo(null), new Point2D.Double(10, 10), true);

        // Range zoom
        plot.setAnchorValue(50.0);
        plot.zoom(0.5);
        Range range = rangeAxis.getRange();
        assertEquals(25.0, range.getLowerBound(), 1e-9);
        assertEquals(75.0, range.getUpperBound(), 1e-9);

        // Zoom 0.0 resets auto-range
        plot.zoom(0.0);
        assertTrue(rangeAxis.isAutoRange());

        // zoomRangeAxes with and without anchor
        PlotRenderingInfo info = new PlotRenderingInfo(null);
        info.setDataArea(new Rectangle2D.Double(0, 0, 100, 100));
        plot.zoomRangeAxes(0.5, info, new Point2D.Double(50, 50), false);
        plot.zoomRangeAxes(0.5, info, new Point2D.Double(50, 50), true);
        plot.zoomRangeAxes(0.2, 0.8, info, new Point2D.Double(50, 50));
    }

    @Test(timeout = 4000)
    public void testGetDataRange() {
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(10.0, "R1", "C1");
        dataset1.addValue(20.0, "R1", "C2");

        NumberAxis rangeAxis = new NumberAxis("Range");
        BarRenderer renderer = new BarRenderer();
        CategoryPlot plot = new CategoryPlot(dataset1, new CategoryAxis("Domain"), rangeAxis, renderer);

        Range bounds = plot.getDataRange(rangeAxis);
        assertNotNull(bounds);
        assertEquals(10.0, bounds.getLowerBound(), 1e-9);
        assertEquals(20.0, bounds.getUpperBound(), 1e-9);

        // Unrelated axis
        NumberAxis unrelatedAxis = new NumberAxis("Unrelated");
        assertNull(plot.getDataRange(unrelatedAxis));
    }

    @Test(timeout = 4000)
    public void testGetCategoriesAndCategoriesForAxis() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "Apple");
        dataset.addValue(2.0, "R1", "Banana");
        CategoryAxis domainAxis = new CategoryAxis("Fruits");
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, new NumberAxis("Values"), new BarRenderer());

        List categories = plot.getCategories();
        assertNotNull(categories);
        assertEquals(2, categories.size());
        assertEquals("Apple", categories.get(0));
        assertEquals("Banana", categories.get(1));

        List axisCategories = plot.getCategoriesForAxis(domainAxis);
        assertEquals(2, axisCategories.size());
        assertTrue(axisCategories.contains("Apple"));
        assertTrue(axisCategories.contains("Banana"));
    }

    @Test(timeout = 4000)
    public void testLegendItems() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Series1", "C1");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), new NumberAxis("Y"), new BarRenderer());

        LegendItemCollection items = plot.getLegendItems();
        assertEquals(1, items.getItemCount());

        LegendItemCollection fixed = new LegendItemCollection();
        fixed.add(new LegendItem("FixedItem"));
        plot.setFixedLegendItems(fixed);
        assertSame(fixed, plot.getFixedLegendItems());
        assertEquals(1, plot.getLegendItems().getItemCount());
        assertEquals("FixedItem", plot.getLegendItems().get(0).getLabel());
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDrawWithZeroOrNegativeDimensions() {
        CategoryPlot plot = new CategoryPlot(new DefaultCategoryDataset(), new CategoryAxis("X"), new NumberAxis("Y"), new BarRenderer());
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // Zero width
        plot.draw(g2, new Rectangle2D.Double(0, 0, 0, 100), null, null, null);
        // Zero height
        plot.draw(g2, new Rectangle2D.Double(0, 0, 100, 0), null, null, null);
        // Negative dimensions
        plot.draw(g2, new Rectangle2D.Double(0, 0, -10, -10), null, null, null);

        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testDrawRenderingVariants() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5.0, "R1", "C1");
        dataset.addValue(-2.0, "R1", "C2");

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), new NumberAxis("Y"), new BarRenderer());
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRangeCrosshairValue(2.0);

        BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // 1. Vertical orientation, FORWARD rendering order
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.setColumnRenderingOrder(SortOrder.DESCENDING);
        plot.setRowRenderingOrder(SortOrder.DESCENDING);
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 300), new Point2D.Double(150, 150), null, new PlotRenderingInfo(null));

        // 2. Horizontal orientation, REVERSE rendering order
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        plot.setColumnRenderingOrder(SortOrder.ASCENDING);
        plot.setRowRenderingOrder(SortOrder.ASCENDING);
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 300), new Point2D.Double(150, 150), null, new PlotRenderingInfo(null));

        // 3. Null renderer fallback branch
        plot.setRenderer(null);
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 300), null, null, null);

        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testHandleClick() {
        CategoryPlot plot = new CategoryPlot(new DefaultCategoryDataset(), new CategoryAxis("X"), new NumberAxis("Y"), new BarRenderer());
        PlotRenderingInfo info = new PlotRenderingInfo(null);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        info.setDataArea(dataArea);

        // Click outside data area
        plot.handleClick(5, 5, info);
        assertEquals(0.0, plot.getAnchorValue(), 1e-9);

        // Click inside vertical
        plot.handleClick(50, 50, info);
        assertNotEquals(0.0, plot.getAnchorValue(), 1e-9);

        // Click inside horizontal
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.handleClick(50, 50, info);
        assertNotEquals(0.0, plot.getAnchorValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testAxisOffsetsAndLocations() {
        CategoryPlot plot = new CategoryPlot();
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

        assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getDomainAxisLocation());
        assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getRangeAxisLocation());
        assertEquals(RectangleEdge.TOP, plot.getDomainAxisEdge());
        assertEquals(RectangleEdge.RIGHT, plot.getRangeAxisEdge());

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(RectangleEdge.RIGHT, plot.getDomainAxisEdge());
        assertEquals(RectangleEdge.BOTTOM, plot.getRangeAxisEdge());

        // Test opposite resolution for index > 0 when unset
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getDomainAxisLocation(1));
        assertEquals(AxisLocation.TOP_OR_LEFT, plot.getRangeAxisLocation(1));
    }

    @Test(timeout = 4000)
    public void testFixedAxisSpace() {
        CategoryPlot plot = new CategoryPlot();
        AxisSpace domainSpace = new AxisSpace();
        domainSpace.setTop(12.0);
        domainSpace.setBottom(14.0);
        plot.setFixedDomainAxisSpace(domainSpace);
        assertSame(domainSpace, plot.getFixedDomainAxisSpace());

        AxisSpace rangeSpace = new AxisSpace();
        rangeSpace.setLeft(15.0);
        rangeSpace.setRight(18.0);
        plot.setFixedRangeAxisSpace(rangeSpace);
        assertSame(rangeSpace, plot.getFixedRangeAxisSpace());

        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        AxisSpace calculatedSpace = plot.calculateAxisSpace(g2, new Rectangle2D.Double(0, 0, 200, 200));
        assertEquals(12.0, calculatedSpace.getTop(), 1e-9);
        assertEquals(14.0, calculatedSpace.getBottom(), 1e-9);
        assertEquals(15.0, calculatedSpace.getLeft(), 1e-9);
        assertEquals(18.0, calculatedSpace.getRight(), 1e-9);
        g2.dispose();
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetOrientationNull() {
        new CategoryPlot().setOrientation(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetAxisOffsetNull() {
        new CategoryPlot().setAxisOffset(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainAxisLocationIndex0Null() {
        new CategoryPlot().setDomainAxisLocation(0, null, true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeAxisLocationIndex0Null() {
        new CategoryPlot().setRangeAxisLocation(0, null, true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDatasetRenderingOrderNull() {
        new CategoryPlot().setDatasetRenderingOrder(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetColumnRenderingOrderNull() {
        new CategoryPlot().setColumnRenderingOrder(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRowRenderingOrderNull() {
        new CategoryPlot().setRowRenderingOrder(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainGridlinePositionNull() {
        new CategoryPlot().setDomainGridlinePosition(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainGridlineStrokeNull() {
        new CategoryPlot().setDomainGridlineStroke(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainGridlinePaintNull() {
        new CategoryPlot().setDomainGridlinePaint(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeGridlineStrokeNull() {
        new CategoryPlot().setRangeGridlineStroke(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeGridlinePaintNull() {
        new CategoryPlot().setRangeGridlinePaint(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeCrosshairStrokeNull() {
        new CategoryPlot().setRangeCrosshairStroke(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeCrosshairPaintNull() {
        new CategoryPlot().setRangeCrosshairPaint(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddDomainMarkerNull() {
        new CategoryPlot().addDomainMarker(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddDomainMarkerNullLayer() {
        new CategoryPlot().addDomainMarker(0, new CategoryMarker("C1"), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddAnnotationNull() {
        new CategoryPlot().addAnnotation(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveAnnotationNull() {
        new CategoryPlot().removeAnnotation(null);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity (Equals, Clone, Serialize)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeIntegrity() {
        CategoryPlot p1 = new CategoryPlot();
        CategoryPlot p2 = new CategoryPlot();

        assertTrue(p1.equals(p1));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("NotAPlot"));
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));

        p1.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse(p1.equals(p2));
        p2.setOrientation(PlotOrientation.HORIZONTAL);
        assertTrue(p1.equals(p2));

        p1.setAxisOffset(new RectangleInsets(1, 2, 3, 4));
        assertFalse(p1.equals(p2));
        p2.setAxisOffset(new RectangleInsets(1, 2, 3, 4));
        assertTrue(p1.equals(p2));

        p1.setDomainGridlinesVisible(true);
        assertFalse(p1.equals(p2));
        p2.setDomainGridlinesVisible(true);
        assertTrue(p1.equals(p2));

        p1.setRangeCrosshairValue(99.0);
        p1.setRangeCrosshairVisible(true);
        assertFalse(p1.equals(p2));
        p2.setRangeCrosshairValue(99.0);
        p2.setRangeCrosshairVisible(true);
        assertTrue(p1.equals(p2));

        p1.setWeight(5);
        assertFalse(p1.equals(p2));
        p2.setWeight(5);
        assertTrue(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testCloneIndependence() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        ValueAxis rangeAxis = new NumberAxis("Range");
        BarRenderer renderer = new BarRenderer();

        CategoryPlot p1 = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        p1.addAnnotation(new CategoryTextAnnotation("Note", "C1", 10.0));
        p1.setFixedDomainAxisSpace(new AxisSpace());
        p1.setFixedRangeAxisSpace(new AxisSpace());

        CategoryPlot p2 = (CategoryPlot) p1.clone();

        assertNotSame(p1, p2);
        assertSame(p1.getClass(), p2.getClass());
        assertTrue(p1.equals(p2));

        // Ensure axes are deeply cloned
        assertNotSame(p1.getDomainAxis(), p2.getDomainAxis());
        assertNotSame(p1.getRangeAxis(), p2.getRangeAxis());

        // Modify p2 axes and assert divergence
        p2.getDomainAxis().setLabel("Modified Domain");
        assertFalse(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(12.3, "Row1", "Col1");
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        ValueAxis rangeAxis = new NumberAxis("Range");
        BarRenderer renderer = new BarRenderer();

        CategoryPlot p1 = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        p1.setDomainGridlinePaint(new Color(10, 20, 30));
        p1.setRangeCrosshairPaint(new Color(40, 50, 60));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(p1);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        CategoryPlot p2 = (CategoryPlot) ois.readObject();

        assertNotNull(p2);
        assertTrue(p1.equals(p2));
        assertEquals(p1.getDomainGridlinePaint(), p2.getDomainGridlinePaint());
        assertEquals(p1.getRangeCrosshairPaint(), p2.getRangeCrosshairPaint());
    }

    @Test(timeout = 4000)
    public void testNotificationEvents() {
        CategoryPlot plot = new CategoryPlot();
        final int[] eventCount = new int[1];
        plot.addChangeListener(new PlotChangeListener() {
            public void plotChanged(PlotChangeEvent event) {
                eventCount[0]++;
            }
        });

        plot.setAnchorValue(15.0);
        assertEquals(1, eventCount[0]);

        plot.setDrawSharedDomainAxis(true);
        assertEquals(2, eventCount[0]);

        plot.datasetChanged(new DatasetChangeEvent(this, null));
        assertEquals(3, eventCount[0]);

        plot.rendererChanged(new RendererChangeEvent(new BarRenderer()));
        assertEquals(4, eventCount[0]);
    }
}