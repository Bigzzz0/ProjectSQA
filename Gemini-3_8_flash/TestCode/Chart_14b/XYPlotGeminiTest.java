package org.jfree.chart.plot;

import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Defect (Defects4J):
 *   - XYPlot.removeDomainMarker(int, Marker, Layer, boolean) and
 *     XYPlot.removeRangeMarker(int, Marker, Layer, boolean) fail with NullPointerException
 *     when attempting to remove a marker from an index/layer with no markers previously registered.
 *   - Decision branches in removeDomainMarker / removeRangeMarker:
 *       Layer == Layer.FOREGROUND vs Layer.BACKGROUND
 *       markers collection is null vs populated
 *
 * Structural Branches & Boundaries Covered:
 *   - Partition A: Core Functional Logic & State Transitions
 *       * Default and parametric constructor initialization
 *       * Axis mappings (Dataset to Domain/Range Axis, secondary axes)
 *       * Dataset and Renderer synchronization and index resolution
 *       * Zooming calculations (factor, percentage, anchor coordinates, horizontal vs vertical)
 *       * Click handling, crosshair updates, and baseline rendering
 *   - Partition B: Boundary Value Analysis (BVA) & Extremes
 *       * Empty dataset, single-item dataset, multi-pass rendering
 *       * Quadrant index boundaries (0, 1, 2, 3 valid; < 0 and > 3 invalid)
 *       * Minimum draw dimensions threshold (MINIMUM_WIDTH_TO_DRAW, MINIMUM_HEIGHT_TO_DRAW)
 *       * Zero baselines toggle, tick band paints, fixed AxisSpace
 *   - Partition C: Defect-Targeted Branch Zone
 *       * testRemoveDomainMarkerFromEmptyPlot (exposing NPE on foreground/background map lookup)
 *       * testRemoveRangeMarkerFromEmptyPlot (exposing NPE on foreground/background map lookup)
 *       * testRemoveDomainAndRangeMarkerWithMultiLayer (unregistered index and layer verification)
 *   - Partition D: Defensive Guard Paths & Exception Specifications
 *       * Null argument rejection across axis, orientation, renderer, and marker setters
 *       * Index out-of-bounds guards on axis dataset mapping
 *   - Partition E: Object Lifecycle & Contract Integrity
 *       * equals() reflexivity, symmetry, and field mutation discriminators
 *       * deep clone() independence and plot reference rewiring
 *       * Serialization read/write roundtrip and state restoration
 * ---------------------------------------------------------------------------------------------------------
 */
public class XYPlotGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndInitialState() {
        XYPlot plot = new XYPlot();
        assertEquals("XY Plot", plot.getPlotType());
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        assertEquals(1, plot.getWeight());
        assertEquals(new RectangleInsets(4.0, 4.0, 4.0, 4.0), plot.getAxisOffset());
        assertNull(plot.getDataset());
        assertNull(plot.getDomainAxis());
        assertNull(plot.getRangeAxis());
        assertNull(plot.getRenderer());
        assertTrue(plot.isDomainGridlinesVisible());
        assertTrue(plot.isRangeGridlinesVisible());
        assertFalse(plot.isDomainZeroBaselineVisible());
        assertFalse(plot.isRangeZeroBaselineVisible());
        assertFalse(plot.isDomainCrosshairVisible());
        assertFalse(plot.isRangeCrosshairVisible());
        assertTrue(plot.isDomainZoomable());
        assertTrue(plot.isRangeZoomable());
    }

    @Test(timeout = 4000)
    public void testAxisAndRendererSetup() {
        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        XYSeriesCollection dataset = new XYSeriesCollection();
        StandardXYItemRenderer renderer = new StandardXYItemRenderer();

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

        assertSame(dataset, plot.getDataset());
        assertSame(xAxis, plot.getDomainAxis());
        assertSame(yAxis, plot.getRangeAxis());
        assertSame(renderer, plot.getRenderer());
        assertSame(plot, xAxis.getPlot());
        assertSame(plot, yAxis.getPlot());
        assertSame(plot, renderer.getPlot());

        assertEquals(0, plot.getDomainAxisIndex(xAxis));
        assertEquals(0, plot.getRangeAxisIndex(yAxis));
        assertEquals(0, plot.indexOf(dataset));
        assertEquals(0, plot.getIndexOf(renderer));
    }

    @Test(timeout = 4000)
    public void testSecondaryAxesAndMappings() {
        XYPlot plot = new XYPlot();
        NumberAxis x0 = new NumberAxis("X0");
        NumberAxis x1 = new NumberAxis("X1");
        NumberAxis y0 = new NumberAxis("Y0");
        NumberAxis y1 = new NumberAxis("Y1");

        plot.setDomainAxes(new ValueAxis[] {x0, x1});
        plot.setRangeAxes(new ValueAxis[] {y0, y1});

        assertEquals(2, plot.getDomainAxisCount());
        assertEquals(2, plot.getRangeAxisCount());
        assertSame(x1, plot.getDomainAxis(1));
        assertSame(y1, plot.getRangeAxis(1));

        XYSeriesCollection ds0 = new XYSeriesCollection();
        XYSeriesCollection ds1 = new XYSeriesCollection();
        plot.setDataset(0, ds0);
        plot.setDataset(1, ds1);

        plot.mapDatasetToDomainAxis(1, 1);
        plot.mapDatasetToRangeAxis(1, 1);

        assertSame(x0, plot.getDomainAxisForDataset(0));
        assertSame(x1, plot.getDomainAxisForDataset(1));
        assertSame(y0, plot.getRangeAxisForDataset(0));
        assertSame(y1, plot.getRangeAxisForDataset(1));
    }

    @Test(timeout = 4000)
    public void testAxisLocationsAndEdges() {
        XYPlot plot = new XYPlot();
        plot.setDomainAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
        plot.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);

        assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getDomainAxisLocation());
        assertEquals(AxisLocation.TOP_OR_LEFT, plot.getRangeAxisLocation());

        assertEquals(RectangleEdge.BOTTOM, plot.getDomainAxisEdge());
        assertEquals(RectangleEdge.LEFT, plot.getRangeAxisEdge());

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(RectangleEdge.RIGHT, plot.getDomainAxisEdge());
        assertEquals(RectangleEdge.TOP, plot.getRangeAxisEdge());

        // Default opposite locations for secondary axes
        assertEquals(AxisLocation.TOP_OR_LEFT, plot.getDomainAxisLocation(1));
        assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getRangeAxisLocation(1));
    }

    @Test(timeout = 4000)
    public void testZoomDomainAndRangeAxes() {
        NumberAxis xAxis = new NumberAxis("X");
        xAxis.setRange(0.0, 100.0);
        NumberAxis yAxis = new NumberAxis("Y");
        yAxis.setRange(0.0, 100.0);
        XYPlot plot = new XYPlot(null, xAxis, yAxis, null);

        PlotRenderingInfo info = new PlotRenderingInfo(null);
        info.setDataArea(new Rectangle2D.Double(0.0, 0.0, 100.0, 100.0));

        // Zoom by factor
        plot.zoomDomainAxes(0.5, info, new Point2D.Double(50.0, 50.0));
        Range xRange = xAxis.getRange();
        assertEquals(25.0, xRange.getLowerBound(), 1e-6);
        assertEquals(75.0, xRange.getUpperBound(), 1e-6);

        plot.zoomRangeAxes(0.5, info, new Point2D.Double(50.0, 50.0));
        Range yRange = yAxis.getRange();
        assertEquals(25.0, yRange.getLowerBound(), 1e-6);
        assertEquals(75.0, yRange.getUpperBound(), 1e-6);

        // Zoom by percentages
        plot.zoomDomainAxes(0.1, 0.9, info, new Point2D.Double(0.0, 0.0));
        assertEquals(30.0, xAxis.getRange().getLowerBound(), 1e-6);
        assertEquals(70.0, xAxis.getRange().getUpperBound(), 1e-6);

        plot.zoomRangeAxes(0.2, 0.8, info, new Point2D.Double(0.0, 0.0));
        assertEquals(35.0, yAxis.getRange().getLowerBound(), 1e-6);
        assertEquals(65.0, yAxis.getRange().getUpperBound(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testZoomAxesWithAnchorAndOrientation() {
        NumberAxis xAxis = new NumberAxis("X");
        xAxis.setRange(0.0, 100.0);
        NumberAxis yAxis = new NumberAxis("Y");
        yAxis.setRange(0.0, 100.0);
        XYPlot plot = new XYPlot(null, xAxis, yAxis, null);
        plot.setOrientation(PlotOrientation.HORIZONTAL);

        PlotRenderingInfo info = new PlotRenderingInfo(null);
        info.setDataArea(new Rectangle2D.Double(0.0, 0.0, 100.0, 100.0));

        Point2D anchor = new Point2D.Double(30.0, 70.0);
        plot.zoomDomainAxes(0.5, info, anchor, true);
        plot.zoomRangeAxes(0.5, info, anchor, true);

        assertTrue(xAxis.getRange().getLength() < 100.0);
        assertTrue(yAxis.getRange().getLength() < 100.0);
    }

    @Test(timeout = 4000)
    public void testRenderingPipelineAndDrawOperations() {
        NumberAxis xAxis = new NumberAxis("Domain");
        NumberAxis yAxis = new NumberAxis("Range");
        XYSeries series = new XYSeries("S1");
        series.add(10.0, 20.0);
        series.add(20.0, 30.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        StandardXYItemRenderer renderer = new StandardXYItemRenderer();

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setQuadrantOrigin(new Point2D.Double(15.0, 25.0));
        plot.setQuadrantPaint(0, new Color(255, 0, 0, 50));
        plot.setQuadrantPaint(1, new Color(0, 255, 0, 50));
        plot.setQuadrantPaint(2, new Color(0, 0, 255, 50));
        plot.setQuadrantPaint(3, new Color(255, 255, 0, 50));

        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setDomainCrosshairValue(15.0);
        plot.setRangeCrosshairValue(25.0);
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);

        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        PlotRenderingInfo info = new PlotRenderingInfo(null);

        plot.draw(g2, new Rectangle2D.Double(0, 0, 400, 300), new Point2D.Double(150, 150), null, info);
        g2.dispose();

        assertNotNull(info.getDataArea());
        assertTrue(info.getDataArea().getWidth() > 0);
        assertTrue(info.getDataArea().getHeight() > 0);
    }

    @Test(timeout = 4000)
    public void testHandleClick() {
        NumberAxis xAxis = new NumberAxis("X");
        xAxis.setRange(0.0, 100.0);
        NumberAxis yAxis = new NumberAxis("Y");
        yAxis.setRange(0.0, 100.0);
        XYPlot plot = new XYPlot(null, xAxis, yAxis, null);

        PlotRenderingInfo info = new PlotRenderingInfo(null);
        info.setDataArea(new Rectangle2D.Double(10, 10, 100, 100));

        plot.handleClick(60, 60, info);
        assertTrue(plot.getDomainCrosshairValue() > 0.0);
        assertTrue(plot.getRangeCrosshairValue() > 0.0);

        // Click outside dataArea should not modify values
        double prevDomain = plot.getDomainCrosshairValue();
        double prevRange = plot.getRangeCrosshairValue();
        plot.handleClick(5, 5, info);
        assertEquals(prevDomain, plot.getDomainCrosshairValue(), 1e-6);
        assertEquals(prevRange, plot.getRangeCrosshairValue(), 1e-6);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDrawMinimumDimensionBoundary() {
        XYPlot plot = new XYPlot();
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        PlotRenderingInfo info = new PlotRenderingInfo(null);

        // Width <= MINIMUM_WIDTH_TO_DRAW (10)
        plot.draw(g2, new Rectangle2D.Double(0, 0, 10, 100), null, null, info);
        assertNull(info.getDataArea());

        // Height <= MINIMUM_HEIGHT_TO_DRAW (10)
        plot.draw(g2, new Rectangle2D.Double(0, 0, 100, 10), null, null, info);
        assertNull(info.getDataArea());

        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testQuadrantIndexBoundaries() {
        XYPlot plot = new XYPlot();
        plot.setQuadrantPaint(0, Color.RED);
        plot.setQuadrantPaint(1, Color.GREEN);
        plot.setQuadrantPaint(2, Color.BLUE);
        plot.setQuadrantPaint(3, Color.YELLOW);

        assertEquals(Color.RED, plot.getQuadrantPaint(0));
        assertEquals(Color.GREEN, plot.getQuadrantPaint(1));
        assertEquals(Color.BLUE, plot.getQuadrantPaint(2));
        assertEquals(Color.YELLOW, plot.getQuadrantPaint(3));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testQuadrantIndexNegativeBoundary() {
        XYPlot plot = new XYPlot();
        plot.getQuadrantPaint(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testQuadrantIndexOverflowBoundary() {
        XYPlot plot = new XYPlot();
        plot.setQuadrantPaint(4, Color.BLACK);
    }

    @Test(timeout = 4000)
    public void testFixedAxisSpaceCoverage() {
        XYPlot plot = new XYPlot();
        AxisSpace domainSpace = new AxisSpace();
        domainSpace.setTop(15.0);
        domainSpace.setBottom(15.0);
        plot.setFixedDomainAxisSpace(domainSpace);

        AxisSpace rangeSpace = new AxisSpace();
        rangeSpace.setLeft(25.0);
        rangeSpace.setRight(25.0);
        plot.setFixedRangeAxisSpace(rangeSpace);

        assertSame(domainSpace, plot.getFixedDomainAxisSpace());
        assertSame(rangeSpace, plot.getFixedRangeAxisSpace());

        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        AxisSpace calcSpace = plot.calculateAxisSpace(g2, new Rectangle2D.Double(0, 0, 200, 200));
        g2.dispose();

        assertEquals(15.0, calcSpace.getTop(), 1e-6);
        assertEquals(15.0, calcSpace.getBottom(), 1e-6);
        assertEquals(25.0, calcSpace.getLeft(), 1e-6);
        assertEquals(25.0, calcSpace.getRight(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testGetDataRangeEmptyAndPopulated() {
        NumberAxis axis = new NumberAxis("X");
        XYPlot plot = new XYPlot();
        plot.setDomainAxis(axis);

        assertNull(plot.getDataRange(axis));

        XYSeries series = new XYSeries("S");
        series.add(5.0, 10.0);
        series.add(15.0, 20.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        plot.setDataset(dataset);
        plot.setRenderer(new DefaultXYItemRenderer());

        Range range = plot.getDataRange(axis);
        assertNotNull(range);
        assertEquals(5.0, range.getLowerBound(), 1e-6);
        assertEquals(15.0, range.getUpperBound(), 1e-6);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    @Test(timeout = 4000)
    public void testRemoveDomainMarkerFromEmptyPlot() {
        XYPlot plot = new XYPlot();
        ValueMarker marker = new ValueMarker(50.0);

        // Ground Truth Bug: If no markers have been added for index 0 and Layer.FOREGROUND,
        // plot.removeDomainMarker() attempts ArrayList markers = foregroundDomainMarkers.get(...)
        // which returns null, then invokes markers.remove(marker), triggering a NullPointerException.
        // On a fixed version, this safely returns false.
        boolean removed = plot.removeDomainMarker(marker);
        assertFalse(removed);
    }

    @Test(timeout = 4000)
    public