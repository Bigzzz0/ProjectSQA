package org.jfree.chart.plot;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYDataImageAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * /* [Branch & Defect Analysis Matrix]
 *  * Partition A: Core Functional Logic & State Transitions
 *  *   - Axis mappings (datasetToDomainAxesMap, datasetToRangeAxesMap, multi-axis mapping)
 *  *   - Gridlines (major and minor for domain and range, baseline rendering)
 *  *   - Crosshair manipulations (locked/unlocked, visible/invisible, orientation variations)
 *  *   - Markers (foreground, background, layer routing, clearing, removal)
 *  *   - Annotations lifecycle (add, remove, clear, retrieve)
 *  *   - Quadrant settings (origin, colors per quadrant)
 *  *   - Rendering & Zooming (forward/reverse dataset order, forward/reverse series order)
 *  *   - Panning logic (domain/range pannable flags, inverted axis percentage panning)
 *  *
 *  * Partition B: Boundary Value Analysis (BVA) & Extremes
 *  *   - Empty datasets, single-item series, null axes, null renderers
 *  *   - Minimum dimension boundaries in draw() (MINIMUM_WIDTH_TO_DRAW, MINIMUM_HEIGHT_TO_DRAW)
 *  *   - Index boundaries for quadrants (0-3 valid, <0 or >3 throws exception)
 *  *
 *  * Partition C: Defect-Targeted Branch Zone
 *  *   - Known Defect: getDataRange(ValueAxis) unconditionally queries r.getAnnotations() when r == null,
 *  *     causing NullPointerException across auto-ranging, serialization, and dataset replacement.
 *  *   - Targeting getDataRange() with null renderer on populated dataset.
 *  *
 *  * Partition D: Exception & Defensive Guard Paths
 *  *   - Null parameter validations (orientation, stroke, paint, location, axisOffset)
 *  *   - Negative index checks in axis mapping
 *  *   - Duplicate or non-Integer elements in checkAxisIndices()
 *  *
 *  * Partition E: Object Lifecycle & Contract Integrity
 *  *   - Deep equals() and hashCode symmetry across multiple properties
 *  *   - Cloning integrity (independent copies of axes, maps, markers, renderer list)
 *  *   - Java serialization / deserialization roundtrip and listener re-registration
 */
public class XYPlotGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndInitialState() {
        XYPlot plot = new XYPlot();
        assertNull(plot.getDataset());
        assertNull(plot.getDomainAxis());
        assertNull(plot.getRangeAxis());
        assertNull(plot.getRenderer());
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        assertEquals("XY Plot", plot.getPlotType());
        assertTrue(plot.isDomainGridlinesVisible());
        assertTrue(plot.isRangeGridlinesVisible());
        assertFalse(plot.isDomainMinorGridlinesVisible());
        assertFalse(plot.isRangeMinorGridlinesVisible());
        assertFalse(plot.isDomainZeroBaselineVisible());
        assertFalse(plot.isRangeZeroBaselineVisible());
        assertFalse(plot.isDomainCrosshairVisible());
        assertFalse(plot.isRangeCrosshairVisible());
        assertTrue(plot.isDomainCrosshairLockedOnData());
        assertTrue(plot.isRangeCrosshairLockedOnData());
        assertEquals(1, plot.getWeight());
    }

    @Test(timeout = 4000)
    public void testDomainAndRangeAxisConfiguration() {
        XYPlot plot = new XYPlot();
        NumberAxis xAxis0 = new NumberAxis("X0");
        NumberAxis xAxis1 = new NumberAxis("X1");
        NumberAxis yAxis0 = new NumberAxis("Y0");
        NumberAxis yAxis1 = new NumberAxis("Y1");

        plot.setDomainAxis(0, xAxis0);
        plot.setDomainAxis(1, xAxis1);
        plot.setRangeAxis(0, yAxis0);
        plot.setRangeAxis(1, yAxis1);

        assertEquals(2, plot.getDomainAxisCount());
        assertEquals(2, plot.getRangeAxisCount());
        assertSame(xAxis0, plot.getDomainAxis(0));
        assertSame(xAxis1, plot.getDomainAxis(1));
        assertSame(yAxis0, plot.getRangeAxis(0));
        assertSame(yAxis1, plot.getRangeAxis(1));

        assertEquals(0, plot.getDomainAxisIndex(xAxis0));
        assertEquals(1, plot.getDomainAxisIndex(xAxis1));
        assertEquals(0, plot.getRangeAxisIndex(yAxis0));
        assertEquals(1, plot.getRangeAxisIndex(yAxis1));

        // Test clear
        plot.clearDomainAxes();
        assertEquals(0, plot.getDomainAxisCount());
        plot.clearRangeAxes();
        assertEquals(0, plot.getRangeAxisCount());
    }

    @Test(timeout = 4000)
    public void testAxisLocationsAndEdges() {
        XYPlot plot = new XYPlot();
        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);

        plot.setDomainAxisLocation(AxisLocation.TOP_OR_LEFT);
        assertEquals(AxisLocation.TOP_OR_LEFT, plot.getDomainAxisLocation());
        assertEquals(RectangleEdge.TOP, plot.getDomainAxisEdge());

        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getRangeAxisLocation());
        assertEquals(RectangleEdge.RIGHT, plot.getRangeAxisEdge());

        // Horizontal flip orientation
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(RectangleEdge.LEFT, plot.getDomainAxisEdge());
        assertEquals(RectangleEdge.BOTTOM, plot.getRangeAxisEdge());

        // Location fallback to opposite when index > 0 is not explicitly defined
        assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getDomainAxisLocation(1));
    }

    @Test(timeout = 4000)
    public void testDatasetToAxesMapping() {
        XYPlot plot = new XYPlot();
        NumberAxis x0 = new NumberAxis("X0");
        NumberAxis x1 = new NumberAxis("X1");
        NumberAxis y0 = new NumberAxis("Y0");
        NumberAxis y1 = new NumberAxis("Y1");
        plot.setDomainAxes(new ValueAxis[] {x0, x1});
        plot.setRangeAxes(new ValueAxis[] {y0, y1});

        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToDomainAxes(1, Arrays.asList(new Integer(1)));
        plot.mapDatasetToRangeAxes(1, Arrays.asList(new Integer(1)));

        assertSame(x0, plot.getDomainAxisForDataset(0));
        assertSame(y0, plot.getRangeAxisForDataset(0));
        assertSame(x1, plot.getDomainAxisForDataset(1));
        assertSame(y1, plot.getRangeAxisForDataset(1));
    }

    @Test(timeout = 4000)
    public void testMarkersAddClearAndRemove() {
        XYPlot plot = new XYPlot();
        Marker dMarker1 = new ValueMarker(1.0);
        Marker dMarker2 = new IntervalMarker(2.0, 3.0);
        Marker rMarker1 = new ValueMarker(10.0);
        Marker rMarker2 = new IntervalMarker(20.0, 30.0);

        plot.addDomainMarker(dMarker1, Layer.FOREGROUND);
        plot.addDomainMarker(dMarker2, Layer.BACKGROUND);
        plot.addRangeMarker(rMarker1, Layer.FOREGROUND);
        plot.addRangeMarker(rMarker2, Layer.BACKGROUND);

        Collection fgDomain = plot.getDomainMarkers(Layer.FOREGROUND);
        Collection bgDomain = plot.getDomainMarkers(Layer.BACKGROUND);
        Collection fgRange = plot.getRangeMarkers(Layer.FOREGROUND);
        Collection bgRange = plot.getRangeMarkers(Layer.BACKGROUND);

        assertTrue(fgDomain.contains(dMarker1));
        assertTrue(bgDomain.contains(dMarker2));
        assertTrue(fgRange.contains(rMarker1));
        assertTrue(bgRange.contains(rMarker2));

        assertTrue(plot.removeDomainMarker(dMarker1, Layer.FOREGROUND));
        assertFalse(plot.removeDomainMarker(dMarker1, Layer.FOREGROUND));
        assertTrue(plot.removeRangeMarker(rMarker1, Layer.FOREGROUND));
        assertFalse(plot.removeRangeMarker(rMarker1, Layer.FOREGROUND));

        plot.clearDomainMarkers();
        plot.clearRangeMarkers();
        assertNull(plot.getDomainMarkers(Layer.FOREGROUND));
        assertNull(plot.getRangeMarkers(Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void testAnnotationsLifecycle() {
        XYPlot plot = new XYPlot();
        XYTextAnnotation a1 = new XYTextAnnotation("A1", 10.0, 20.0);
        XYTextAnnotation a2 = new XYTextAnnotation("A2", 30.0, 40.0);

        plot.addAnnotation(a1);
        plot.addAnnotation(a2, true);

        List annotations = plot.getAnnotations();
        assertEquals(2, annotations.size());
        assertTrue(annotations.contains(a1));
        assertTrue(annotations.contains(a2));

        assertTrue(plot.removeAnnotation(a1));
        assertFalse(plot.removeAnnotation(a1));
        assertEquals(1, plot.getAnnotations().size());

        plot.clearAnnotations();
        assertTrue(plot.getAnnotations().isEmpty());
    }

    @Test(timeout = 4000)
    public void testQuadrantsConfiguration() {
        XYPlot plot = new XYPlot();
        Point2D origin = new Point2D.Double(5.0, 5.0);
        plot.setQuadrantOrigin(origin);
        assertEquals(origin, plot.getQuadrantOrigin());

        plot.setQuadrantPaint(0, Color.RED);
        plot.setQuadrantPaint(1, Color.GREEN);
        plot.setQuadrantPaint(2, Color.BLUE);
        plot.setQuadrantPaint(3, Color.YELLOW);

        assertEquals(Color.RED, plot.getQuadrantPaint(0));
        assertEquals(Color.GREEN, plot.getQuadrantPaint(1));
        assertEquals(Color.BLUE, plot.getQuadrantPaint(2));
        assertEquals(Color.YELLOW, plot.getQuadrantPaint(3));
    }

    @Test(timeout = 4000)
    public void testCrosshairSettersAndGetters() {
        XYPlot plot = new XYPlot();
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairValue(42.5);
        plot.setDomainCrosshairLockedOnData(false);
        plot.setDomainCrosshairPaint(Color.MAGENTA);
        Stroke stroke = new BasicStroke(2.0f);
        plot.setDomainCrosshairStroke(stroke);

        assertTrue(plot.isDomainCrosshairVisible());
        assertEquals(42.5, plot.getDomainCrosshairValue(), 1e-9);
        assertFalse(plot.isDomainCrosshairLockedOnData());
        assertEquals(Color.MAGENTA, plot.getDomainCrosshairPaint());
        assertEquals(stroke, plot.getDomainCrosshairStroke());

        plot.setRangeCrosshairVisible(true);
        plot.setRangeCrosshairValue(84.0);
        plot.setRangeCrosshairLockedOnData(false);
        plot.setRangeCrosshairPaint(Color.CYAN);
        plot.setRangeCrosshairStroke(stroke);

        assertTrue(plot.isRangeCrosshairVisible());
        assertEquals(84.0, plot.getRangeCrosshairValue(), 1e-9);
        assertFalse(plot.isRangeCrosshairLockedOnData());
        assertEquals(Color.CYAN, plot.getRangeCrosshairPaint());
        assertEquals(stroke, plot.getRangeCrosshairStroke());
    }

    @Test(timeout = 4000)
    public void testPanningDomainAndRangeAxes() {
        XYPlot plot = new XYPlot();
        NumberAxis xAxis = new NumberAxis("X");
        xAxis.setRange(0.0, 100.0);
        NumberAxis yAxis = new NumberAxis("Y");
        yAxis.setRange(0.0, 100.0);

        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);

        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        assertTrue(plot.isDomainPannable());
        assertTrue(plot.isRangePannable());

        plot.panDomainAxes(0.10, null, new Point2D.Double(0, 0));
        assertEquals(10.0, xAxis.getLowerBound(), 1e-6);
        assertEquals(110.0, xAxis.getUpperBound(), 1e-6);

        plot.panRangeAxes(-0.10, null, new Point2D.Double(0, 0));
        assertEquals(-10.0, yAxis.getLowerBound(), 1e-6);
        assertEquals(90.0, yAxis.getUpperBound(), 1e-6);

        // Test with inverted axis
        xAxis.setInverted(true);
        plot.panDomainAxes(0.10, null, new Point2D.Double(0, 0));
        assertEquals(0.0, xAxis.getLowerBound(), 1e-6);
        assertEquals(100.0, xAxis.getUpperBound(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testZoomDomainAndRangeAxes() {
        XYPlot plot = new XYPlot();
        NumberAxis xAxis = new NumberAxis("X");
        xAxis.setRange(0.0, 100.0);
        NumberAxis yAxis = new NumberAxis("Y");
        yAxis.setRange(0.0, 100.0);

        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);
        assertTrue(plot.isDomainZoomable());
        assertTrue(plot.isRangeZoomable());

        plot.zoomDomainAxes(0.5, null, new Point2D.Double(0, 0));
        assertEquals(25.0, xAxis.getLowerBound(), 1e-6);
        assertEquals(75.0, xAxis.getUpperBound(), 1e-6);

        plot.zoomRangeAxes(0.2, 0.8, null, new Point2D.Double(0, 0));
        assertEquals(20.0, yAxis.getLowerBound(), 1e-6);
        assertEquals(80.0, yAxis.getUpperBound(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testFixedLegendItems() {
        XYPlot plot = new XYPlot();
        assertNull(plot.getFixedLegendItems());

        LegendItemCollection coll = new LegendItemCollection();
        coll.add(new LegendItem("Test Item"));
        plot.setFixedLegendItems(coll);

        assertSame(coll, plot.getFixedLegendItems());
        assertSame(coll, plot.getLegendItems());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDrawWithMinimumDimensionsDoesNotThrow() {
        XYPlot plot = new XYPlot();
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // Width <= MINIMUM_WIDTH_TO_DRAW (10)
        Rectangle2D areaTooNarrow = new Rectangle2D.Double(0, 0, 9, 50);
        plot.draw(g2, areaTooNarrow, null, null, null);

        // Height <= MINIMUM_HEIGHT_TO_DRAW (10)
        Rectangle2D areaTooShort = new Rectangle2D.Double(0, 0, 50, 9);
        plot.draw(g2, areaTooShort, null, null, null);

        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testCompleteDrawExecutionWithEmptyAndPopulatedData() {
        XYSeries series = new XYSeries("Series 1");
        series.add(10.0, 20.0);
        series.add(20.0, 30.0);
        series.add(30.0, 15.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);
        plot.setQuadrantPaint(0, new Color(255, 0, 0, 50));
        plot.setQuadrantPaint(1, new Color(0, 255, 0, 50));
        plot.setQuadrantPaint(2, new Color(0, 0, 255, 50));
        plot.setQuadrantPaint(3, new Color(255, 255, 0, 50));

        BufferedImage img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);

        PlotRenderingInfo info = new PlotRenderingInfo(null);
        plot.draw(g2, area, new Point2D.Double(200, 150), null, info);

        assertNotNull(info.getDataArea());
        assertTrue(info.getDataArea().getWidth() > 0);

        // Test handleClick
        plot.handleClick((int) info.getDataArea().getCenterX(),
                         (int) info.getDataArea().getCenterY(), info);

        // Test Reverse Orders
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        plot.draw(g2, area, null, null, null);

        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testDrawHorizontalAndVerticalWithOrientationHorizontal() {
        XYSeries series = new XYSeries("S");
        series.add(1.0, 1.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 300),
                  new Point2D.Double(150, 150), null, null);
        g2.dispose();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defects4J)
    // =========================================================================

    /**
     * TARGETED FAULT: In XYPlot.getDataRange(ValueAxis axis), when iterating over mapped datasets,
     * it invokes `r.getAnnotations()` unconditionally, where `XYItemRenderer r = getRendererForDataset(d);`.
     * If the renderer is null (either explicitly set to null or not yet initialized), this causes a
     * java.lang.NullPointerException.
     * The correct behavior should gracefully retrieve data bounds without failing when renderer is null.
     */
    @Test(timeout = 4000)
    public void testGetDataRangeWithNullRendererDefect() {
        XYSeries series = new XYSeries("TestSeries");
        series.add(10.0, 20.0);
        series.add(30.0, 40.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");

        // Explicitly instantiate XYPlot with null renderer
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);

        // This call will trigger getDataRange() on domain axis and range axis
        Range xRange = plot.getDataRange(xAxis);
        assertNotNull("Domain data range should not be null", xRange);
        assertEquals(10.0, xRange.getLowerBound(), 1e-9);
        assertEquals(30.0, xRange.getUpperBound(), 1e-9);

        Range yRange = plot.getDataRange(yAxis);
        assertNotNull("Range data range should not be null", yRange);
        assertEquals(20.0, yRange.getLowerBound(), 1e-9);
        assertEquals(40.0, yRange.getUpperBound(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testReplaceDatasetWithNullRendererTriggersAutoRange() {
        XYPlot plot = new XYPlot();
        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);
        plot.setRenderer(null);

        XYSeries series = new XYSeries("S");
        series.add(1.0, 2.0);
        series.add(5.0, 10.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Setting dataset triggers datasetChanged -> configureDomainAxes / configureRangeAxes -> getDataRange
        plot.setDataset(dataset);

        Range range = plot.getDataRange(yAxis);
        assertNotNull(range);
        assertEquals(2.0, range.getLowerBound(), 1e-9);
        assertEquals(10.0, range.getUpperBound(), 1e-9);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetOrientationNullThrowsException() {
        XYPlot plot = new XYPlot();
        plot.setOrientation(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetAxisOffsetNullThrowsException() {
        XYPlot plot = new XYPlot();
        plot.setAxisOffset(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainAxisLocationIndexZeroNullThrows() {
        XYPlot plot = new XYPlot();
        plot.setDomainAxisLocation(0, null, true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeAxisLocationIndexZeroNullThrows() {
        XYPlot plot = new XYPlot();
        plot.setRangeAxisLocation(0, null, true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMapDatasetToDomainAxesNegativeIndexThrows() {
        XYPlot plot = new XYPlot();
        plot.mapDatasetToDomainAxes(-1, Collections.singletonList(new Integer(0)));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMapDatasetToRangeAxesNegativeIndexThrows() {
        XYPlot plot = new XYPlot();
        plot.mapDatasetToRangeAxes(-1, Collections.singletonList(new Integer(0)));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMapDatasetToDomainAxesEmptyListThrows() {
        XYPlot plot = new XYPlot();
        plot.mapDatasetToDomainAxes(0, Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMapDatasetToDomainAxesNonIntegerListThrows() {
        XYPlot plot = new XYPlot();
        List invalidList = new ArrayList();
        invalidList.add("zero");
        plot.mapDatasetToDomainAxes(0, invalidList);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMapDatasetToDomainAxesDuplicatesThrows() {
        XYPlot plot = new XYPlot();
        List duplicates = Arrays.asList(new Integer(1), new Integer(1));
        plot.mapDatasetToDomainAxes(0, duplicates);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetQuadrantPaintInvalidLowIndexThrows() {
        XYPlot plot = new XYPlot();
        plot.setQuadrantPaint(-1, Color.BLACK);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetQuadrantPaintInvalidHighIndexThrows() {
        XYPlot plot = new XYPlot();
        plot.setQuadrantPaint(4, Color.BLACK);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetQuadrantPaintInvalidIndexThrows() {
        XYPlot plot = new XYPlot();
        plot.getQuadrantPaint(5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDatasetRenderingOrderNullThrows() {
        XYPlot plot = new XYPlot();
        plot.setDatasetRenderingOrder(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetSeriesRenderingOrderNullThrows() {
        XYPlot plot = new XYPlot();
        plot.setSeriesRenderingOrder(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainGridlineStrokeNullThrows() {
        XYPlot plot = new XYPlot();
        plot.setDomainGridlineStroke(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainGridlinePaintNullThrows() {
        XYPlot plot = new XYPlot();
        plot.setDomainGridlinePaint(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (Equals, HashCode, Clone, Serialization)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        XYPlot plot1 = new XYPlot(null, new NumberAxis("X"), new NumberAxis("Y"), new StandardXYItemRenderer());
        XYPlot plot2 = new XYPlot(null, new NumberAxis("X"), new NumberAxis("Y"), new StandardXYItemRenderer());

        assertTrue(plot1.equals(plot1));
        assertTrue(plot1.equals(plot2));
        assertTrue(plot2.equals(plot1));
        assertFalse(plot1.equals(null));
        assertFalse(plot1.equals("A String"));

        // Change orientation
        plot2.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse(plot1.equals(plot2));
        plot2.setOrientation(PlotOrientation.VERTICAL);
        assertTrue(plot1.equals(plot2));

        // Change weight
        plot2.setWeight(5);
        assertFalse(plot1.equals(plot2));
        plot2.setWeight(plot1.getWeight());
        assertTrue(plot1.equals(plot2));

        // Change domain gridline visibility
        plot2.setDomainGridlinesVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlinesVisible(true);
        assertTrue(plot1.equals(plot2));

        // Change range crosshair visibility
        plot2.setRangeCrosshairVisible(true);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeCrosshairVisible(false);
        assertTrue(plot1.equals(plot2));
    }

    @Test(timeout = 4000)
    public void testCloningIntegrity() throws CloneNotSupportedException {
        XYSeries series = new XYSeries("S1");
        series.add(1.0, 2.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        StandardXYItemRenderer renderer = new StandardXYItemRenderer();

        XYPlot original = new XYPlot(dataset, xAxis, yAxis, renderer);
        original.addAnnotation(new XYTextAnnotation("Anno", 1.0, 2.0));
        original.addDomainMarker(new ValueMarker(1.5), Layer.FOREGROUND);

        XYPlot cloned = (XYPlot) original.clone();

        assertNotSame(original, cloned);
        assertSame(original.getClass(), cloned.getClass());
        assertEquals(original, cloned);

        // Verify deep copy of components
        assertNotSame(original.getDomainAxis(), cloned.getDomainAxis());
        assertNotSame(original.getRangeAxis(), cloned.getRangeAxis());
        assertNotSame(original.getAnnotations(), cloned.getAnnotations());

        // Modifying clone does not alter original
        cloned.addAnnotation(new XYTextAnnotation("Anno2", 3.0, 4.0));
        assertEquals(1, original.getAnnotations().size());
        assertEquals(2, cloned.getAnnotations().size());
    }

    @Test(timeout = 4000)
    public void testSerializationRoundtrip() throws Exception {
        XYSeries series = new XYSeries("Data");
        series.add(5.0, 10.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        NumberAxis xAxis = new NumberAxis("Domain");
        NumberAxis yAxis = new NumberAxis("Range");
        StandardXYItemRenderer renderer = new StandardXYItemRenderer();

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairValue(5.0);
        plot.setQuadrantPaint(0, Color.LIGHT_GRAY);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(plot);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        XYPlot deserialized = (XYPlot) in.readObject();
        in.close();

        assertEquals(plot, deserialized);
        assertEquals(5.0, deserialized.getDomainCrosshairValue(), 1e-9);
        assertTrue(deserialized.isDomainCrosshairVisible());
    }

    @Test(timeout = 4000)
    public void testListenerNotificationOnDatasetChange() {
        XYPlot plot = new XYPlot();
        final boolean[] notified = new boolean[] {false};
        plot.addChangeListener(new PlotChangeListener() {
            public void plotChanged(PlotChangeEvent event) {
                notified[0] = true;
            }
        });

        XYSeries series = new XYSeries("S");
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        plot.setDataset(dataset);
        assertTrue("Setting dataset must fire a change event", notified[0]);

        notified[0] = false;
        series.add(10.0, 20.0);
        assertTrue("Dataset modification must trigger plot notification", notified[0]);
    }
}