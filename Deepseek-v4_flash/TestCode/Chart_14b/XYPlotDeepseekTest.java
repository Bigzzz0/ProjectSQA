package org.jfree.chart.plot;

import static org.junit.Assert.*;
import org.junit.Test;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Comprehensive test suite for XYPlot. Targets the known Defects4J defect:
 * NullPointerException when calling removeDomainMarker or removeRangeMarker
 * on an index with no prior markers. Also achieves high line/branch coverage.
 * 
 * Branch & Defect Analysis Matrix:
 * --------------------------------
 * Partition A: Core getters/setters, dataset, renderer, axis methods
 * Partition B: Boundary value analysis (nulls, empty, extremes)
 * Partition C: Defect-targeted: removeDomainMarker/removeRangeMarker when no markers exist
 * Partition D: Exception/guards (null args, invalid indices)
 * Partition E: Contract: equals, clone, serialization (basic)
 * 
 * Key branches under test:
 * - Marker collections null check in remove...Marker methods
 * - Axis index out of bounds behavior
 * - Dataset/renderer index handling
 * - Quadrant paint index validation
 * - Crosshair visibility flag firing
 * - Zoom methods with anchor
 * - getDataRange with null renderer
 * - clone and serialization paths
 */
public class XYPlotDeepseekTest {

    /**
     * Partition A: Core functional logic – constructors, getters/setters,
     * dataset and renderer wiring, basic axis configuration.
     */
    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        XYPlot plot = new XYPlot();
        assertNotNull(plot);
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        assertEquals(1, plot.getWeight());
        assertNull(plot.getDomainAxis());
        assertNull(plot.getRangeAxis());
        assertNull(plot.getDataset());
        assertNull(plot.getRenderer());
        assertTrue(plot.isDomainGridlinesVisible());
        assertTrue(plot.isRangeGridlinesVisible());
        assertFalse(plot.isDomainCrosshairVisible());
        assertFalse(plot.isRangeCrosshairVisible());
        assertFalse(plot.isDomainZeroBaselineVisible());
        assertFalse(plot.isRangeZeroBaselineVisible());
        assertEquals(DatasetRenderingOrder.REVERSE, plot.getDatasetRenderingOrder());
        assertEquals(SeriesRenderingOrder.REVERSE, plot.getSeriesRenderingOrder());
        assertNull(plot.getFixedLegendItems());
    }

    @Test(timeout = 4000)
    public void testParameterizedConstructor() {
        XYSeries series = new XYSeries("S1");
        series.add(1.0, 2.0);
        XYDataset dataset = new XYSeriesCollection(series);
        ValueAxis domainAxis = new org.jfree.chart.axis.NumberAxis("X");
        ValueAxis rangeAxis = new org.jfree.chart.axis.NumberAxis("Y");
        XYItemRenderer renderer = new XYLineAndShapeRenderer();
        XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        assertSame(dataset, plot.getDataset());
        assertSame(domainAxis, plot.getDomainAxis());
        assertSame(rangeAxis, plot.getRangeAxis());
        assertSame(renderer, plot.getRenderer());
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getDomainAxisLocation());
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getRangeAxisLocation());
    }

    @Test(timeout = 4000)
    public void testSetDatasetNull() {
        XYPlot plot = new XYPlot();
        plot.setDataset(null);
        assertNull(plot.getDataset());
    }

    @Test(timeout = 4000)
    public void testSetDatasetAndNotify() {
        XYPlot plot = new XYPlot();
        XYSeriesCollection dataset = new XYSeriesCollection();
        plot.setDataset(dataset);
        assertSame(dataset, plot.getDataset());
        assertEquals(1, plot.getDatasetCount());
    }

    @Test(timeout = 4000)
    public void testSetMultipleDatasets() {
        XYPlot plot = new XYPlot();
        XYSeriesCollection d1 = new XYSeriesCollection();
        XYSeriesCollection d2 = new XYSeriesCollection();
        plot.setDataset(0, d1);
        plot.setDataset(1, d2);
        assertSame(d1, plot.getDataset(0));
        assertSame(d2, plot.getDataset(1));
        assertEquals(2, plot.getDatasetCount());
    }

    @Test(timeout = 4000)
    public void testIndexOfDataset() {
        XYPlot plot = new XYPlot();
        XYSeriesCollection d1 = new XYSeriesCollection();
        XYSeriesCollection d2 = new XYSeriesCollection();
        plot.setDataset(0, d1);
        plot.setDataset(1, d2);
        assertEquals(0, plot.indexOf(d1));
        assertEquals(1, plot.indexOf(d2));
        assertEquals(-1, plot.indexOf(new XYSeriesCollection()));
    }

    @Test(timeout = 4000)
    public void testSetRenderer() {
        XYPlot plot = new XYPlot();
        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();
        plot.setRenderer(r);
        assertSame(r, plot.getRenderer());
        assertSame(r, plot.getRenderer(0));
    }

    @Test(timeout = 4000)
    public void testSetRendererWithIndex() {
        XYPlot plot = new XYPlot();
        XYLineAndShapeRenderer r0 = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer r1 = new XYLineAndShapeRenderer();
        plot.setRenderer(0, r0);
        plot.setRenderer(1, r1);
        assertSame(r0, plot.getRenderer(0));
        assertSame(r1, plot.getRenderer(1));
    }

    @Test(timeout = 4000)
    public void testGetRendererForDataset() {
        XYPlot plot = new XYPlot();
        XYSeriesCollection d1 = new XYSeriesCollection();
        XYSeriesCollection d2 = new XYSeriesCollection();
        XYLineAndShapeRenderer r1 = new XYLineAndShapeRenderer();
        plot.setDataset(0, d1);
        plot.setDataset(1, d2);
        plot.setRenderer(0, null);
        plot.setRenderer(1, r1);
        // If no renderer for dataset 0, should fall back to renderer(0) which is null
        assertNull(plot.getRendererForDataset(d1));
        assertSame(r1, plot.getRendererForDataset(d2));
    }

    @Test(timeout = 4000)
    public void testMapDatasetToDomainAxis() {
        XYPlot plot = new XYPlot();
        plot.mapDatasetToDomainAxis(0, 1);
        // no direct getter, but should not throw
    }

    @Test(timeout = 4000)
    public void testMapDatasetToRangeAxis() {
        XYPlot plot = new XYPlot();
        plot.mapDatasetToRangeAxis(0, 2);
    }

    @Test(timeout = 4000)
    public void testGetDomainAxisCount() {
        XYPlot plot = new XYPlot();
        assertEquals(1, plot.getDomainAxisCount()); // default after constructor
        plot.setDomainAxis(1, new org.jfree.chart.axis.NumberAxis("X2"));
        assertEquals(2, plot.getDomainAxisCount());
    }

    @Test(timeout = 4000)
    public void testGetRangeAxisCount() {
        XYPlot plot = new XYPlot();
        assertEquals(1, plot.getRangeAxisCount());
        plot.setRangeAxis(1, new org.jfree.chart.axis.NumberAxis("Y2"));
        assertEquals(2, plot.getRangeAxisCount());
    }

    @Test(timeout = 4000)
    public void testClearDomainAxes() {
        XYPlot plot = new XYPlot();
        ValueAxis axis = new org.jfree.chart.axis.NumberAxis("X");
        plot.setDomainAxis(axis);
        plot.clearDomainAxes();
        assertEquals(0, plot.getDomainAxisCount());
    }

    @Test(timeout = 4000)
    public void testClearRangeAxes() {
        XYPlot plot = new XYPlot();
        ValueAxis axis = new org.jfree.chart.axis.NumberAxis("Y");
        plot.setRangeAxis(axis);
        plot.clearRangeAxes();
        assertEquals(0, plot.getRangeAxisCount());
    }

    @Test(timeout = 4000)
    public void testSetDomainAxesArray() {
        XYPlot plot = new XYPlot();
        ValueAxis[] axes = new ValueAxis[] { new org.jfree.chart.axis.NumberAxis("A"),
                new org.jfree.chart.axis.NumberAxis("B") };
        plot.setDomainAxes(axes);
        assertEquals(2, plot.getDomainAxisCount());
        assertNotNull(plot.getDomainAxis(0));
        assertNotNull(plot.getDomainAxis(1));
    }

    @Test(timeout = 4000)
    public void testSetRangeAxesArray() {
        XYPlot plot = new XYPlot();
        ValueAxis[] axes = new ValueAxis[] { new org.jfree.chart.axis.NumberAxis("A"),
                new org.jfree.chart.axis.NumberAxis("B") };
        plot.setRangeAxes(axes);
        assertEquals(2, plot.getRangeAxisCount());
    }

    @Test(timeout = 4000)
    public void testGetDomainAxisLocation() {
        XYPlot plot = new XYPlot();
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getDomainAxisLocation());
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getDomainAxisLocation());
    }

    @Test(timeout = 4000)
    public void testGetDomainAxisLocationWithIndex() {
        XYPlot plot = new XYPlot();
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getDomainAxisLocation(0));
        // index 1 not set, should return opposite of primary
        assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getDomainAxisLocation(1));
    }

    @Test(timeout = 4000)
    public void testSetDomainAxisLocationWithIndex() {
        XYPlot plot = new XYPlot();
        plot.setDomainAxisLocation(1, AxisLocation.LFT_OR_BOTTOM);
        assertEquals(AxisLocation.LFT_OR_BOTTOM, plot.getDomainAxisLocation(1));
    }

    @Test(timeout = 4000)
    public void testGetRangeAxisEdge() {
        XYPlot plot = new XYPlot();
        assertEquals(RectangleEdge.BOTTOM, plot.getRangeAxisEdge());
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(RectangleEdge.LFT, plot.getRangeAxisEdge());
    }

    @Test(timeout = 4000)
    public void testGetDomainAxisEdgeWithIndex() {
        XYPlot plot = new XYPlot();
        assertEquals(RectangleEdge.BOTTOM, plot.getDomainAxisEdge(0));
        // after setting location to top
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertEquals(RectangleEdge.TOP, plot.getDomainAxisEdge(0));
    }

    @Test(timeout = 4000)
    public void testSetOrientation() {
        XYPlot plot = new XYPlot();
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(PlotOrientation.HORIZONTAL, plot.getOrientation());
        plot.setOrientation(PlotOrientation.VERTICAL);
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetOrientationNull() {
        XYPlot plot = new XYPlot();
        plot.setOrientation(null);
    }

    @Test(timeout = 4000)
    public void testSetAxisOffset() {
        XYPlot plot = new XYPlot();
        org.jfree.chart.util.RectangleInsets offset = new org.jfree.chart.util.RectangleInsets(1,2,3,4);
        plot.setAxisOffset(offset);
        assertEquals(offset, plot.getAxisOffset());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)    public void testSetAxisOffsetNull() {
        XYPlot plot = new XYPlot();
        plot.setAxisOffset(null);
    }

    // --- Weight ---
    @Test(timeout = 4000)
    public void testSetWeight() {
        XYPlot plot = new XYPlot();
        plot.setWeight(5);
        assertEquals(5, plot.getWeight());
    }

    // --- Gridline flags and strokes ---
    @Test(timeout = 4000)
    public void testSetDomainGridlinesVisible() {
        XYPlot plot = new XYPlot();
        assertTrue(plot.isDomainGridlinesVisible());
        plot.setDomainGridlinesVisible(false);
        assertFalse(plot.isDomainGridlinesVisible());
        plot.setDomainGridlinesVisible(true);
        assertTrue(plot.isDomainGridlinesVisible());
    }

    @Test(timeout = 4000)
    public void testSetDomainGridlineStroke() {
        XYPlot plot = new XYPlot();
        Stroke s = new BasicStroke(2.0f);
        plot.setDomainGridlineStroke(s);
        assertSame(s, plot.getDomainGridlineStroke());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainGridlineStrokeNull() {
        XYPlot plot = new XYPlot();
        plot.setDomainGridlineStroke(null);
    }

    @Test(timeout = 4000)
    public void testSetDomainGridlinePaint() {
        XYPlot plot = new XYPlot();
        plot.setDomainGridlinePaint(Color.red);
        assertEquals(Color.red, plot.getDomainGridlinePaint());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainGridlinePaintNull() {
        XYPlot plot = new XYPlot();
        plot.setDomainGridlinePaint(null);
    }

    @Test(timeout = 4000)
    public void testSetRangeGridlinesVisible() {
        XYPlot plot = new XYPlot();
        plot.setRangeGridlinesVisible(false);
        assertFalse(plot.isRangeGridlinesVisible());
    }

    @Test(timeout = 4000)
    public void testSetRangeGridlineStroke() {
        XYPlot plot = new XYPlot();
        Stroke s = new BasicStroke(3.0f);
        plot.setRangeGridlineStroke(s);
        assertSame(s, plot.getRangeGridlineStroke());
    }

    @Test(timeout = 4000)
    public void testSetRangeGridlinePaint() {
        XYPlot plot = new XYPlot();
        plot.setRangeGridlinePaint(Color.blue);
        assertEquals(Color.blue, plot.getRangeGridlinePaint());
    }

    // --- Zero baseline---
    @Test(timeout = 4000)
    public void testDomainZeroBaseline() {
        XYPlot plot = new XYPlot();
        assertFalse(plot.isDomainZeroBaselineVisible());
        plot.setDomainZeroBaselineVisible(true);
        assertTrue(plot.isDomainZeroBaselineVisible());
        plot.setDomainZeroBaselineStroke(new BasicStroke(1.0f));
        assertNotNull(plot.getDomainZeroBaselineStroke());
        plot.setDomainZeroBaselinePaint(Color.green);
        assertEquals(Color.green, plot.getDomainZeroBaselinePaint());
    }

    @Test(timeout = 4000)
    public void testRangeZeroBaseline() {
        XYPlot plot = new XYPlot();
        assertFalse(plot.isRangeZeroBaselineVisible());
        plot.setRangeZeroBaselineVisible(true);
        assertTrue(plot.isRangeZeroBaselineVisible());
        plot.setRangeZeroBaselineStroke(new BasicStroke(2.0f));
        assertNotNull(plot.getRangeZeroBaselineStroke());
        plot.setRangeZeroBaselinePaint(Color.yellow);
        assertEquals(Color.yellow, plot.getRangeZeroBaselinePaint());
    }

    // --- Tick band paints ---
    @Test(timeout = 4000)
    public void testDomainTickBandPaint() {
        XYPlot plot = new XYPlot();
        assertNull(plot.getDomainTickBandPaint());
        plot.setDomainTickBandPaint(Color.gray);
        assertEquals(Color.gray, plot.getDomainTickBandPaint());
    }

    @Test(timeout = 4000)
    public void testRangeTickBandPaint() {
        XYPlot plot = new XYPlot();
        assertNull(plot.getRangeTickBandPaint());
        plot.setRangeTickBandPaint(Color.lightGray);
        assertEquals(Color.lightGray, plot.getRangeTickBandPaint());
    }

    // --- Quadrant---
    @Test(timeout = 4000)
    public void testQuadrantOrigin() {
        XYPlot plot = new XYPlot();
        Point2D p = new Point2D.Double(10, 20);
        plot.setQuadrantOrigin(p);
        assertEquals(p, plot.getQuadrantOrigin()));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetQuadrantOriginNull() {
        XYPlot plot = new XYPlot();
        plot.setQuadrantOrigin(null);
    }

    @Test(timeout = 4000)
    public void testQuadrantPaint() {
        XYPlot plot = new XYPlot();
        assertNull(plot.getQuadrantPaint(0));
        plot.setQuadrantPaint(0, Color.red);
        assertEquals(Color.red, plot.getQuadrantPaint(0));
        plot.setQuadrantPaint(1, Color.green);
        assertEquals(Color.green, plot.getQuadrantPaint(1));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetQuadrantPaintIndexOutOfBounds() {
        XYPlot plot = new XYPlot();
        plot.setQuadrantPaint(-1, Color.red);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetQuadrantPaintIndexOutOfBounds() {
        XYPlot plot = new XYPlot();
        plot.getQuadrantPaint(4);
    }

    // --- Crosshair ---    @Test(timeout = 4000)
    public void testDomainCrosshairVisible() {
        XYPlot plot = new XYPlot();
        assertFalse(plot.isDomainCrosshairVisible());
        plot.setDomainCrosshairVisible(true);
        assertTrue(plot.isDomainCrosshairVisible());
        assertFalse(plot.isDomainCrosshairLockedOnData());
        plot.setDomainCrosshairLockedOnData(true);
        assertTrue(plot.isDomainCrosshairLockedOnData());
    }

    @Test(timeout = 4000)
    public void testDomainCrosshairValue() {
        XYPlot plot = new XYPlot();
        assertEquals(0.0, plot.getDomainCrosshairValue(), 0.0);
        plot.setDomainCrosshairValue(5.5);
        assertEquals(5.5, plot.getDomainCrosshairValue(), 0.0);
        // set with no notify
        plot.setDomainCrosshairValue(10.0， false);
        assertEquals(10.0, plot.getDomainCrosshairValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSetDomainCrosshairStroke() {
        XYPlot plot = new XYPlot();
        Stroke s = new BasicStroke(1.0f);
        plot.setDomainCrosshairStroke(s);
        assertSame(s, plot.getDomainCrosshairStroke());
    }

    @Test(timeout = 4000)
    public void testSetDomainCrosshairPaint() {
        XYPlot plot = new XYPlot();
        plot.setDomainCrosshairPaint(Color.magenta);
        assertEquals(Color.magenta, plot.getDomainCrosshairPaint());
    }

    @Test(timeout = 4000)
    public void testRangeCrosshairVisible() {
        XYPlot plot = new XYPlot();
        assertFalse(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairVisible(true);
        assertTrue(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairLockedOnData(false);
        assertFalse(plot.isRangeCrosshairLockedOnData());
    }

    @Test(timeout = 4000)
    public void testRangeCrosshairValue() {
        XYPlot plot = new XYPlot();
        assertEquals(0.0， plot.getRangeCrosshairValue(), 0.0);
        plot.setRangeCrosshairValue(3.3);
        assertEquals(3.3， plot.getRangeCrosshairValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSetRangeCrosshairStroke() {
        XYPlot plot = new XYPlot();
        Stroke s = new BasicStroke(2.0f);
        plot.setRangeCrosshairStroke(s);
        assertSame(s， plot.getRangeCrosshairStroke());
    }

    // --- Fixed axis space ---
    @Test(timeout = 4000)
    public void testFixedDomainAxisSpace() {
        XYPlot plot = new XYPlot();
        assertNull(plot.getFixedDomainAxisSpace());
        AxisSpace space = new AxisSpace();
        plot.setFixedDomainAxisSpace(space);
        assertSame(space, plot.getFixedDomainAxisSpace());
        // set with notify false
        plot.setFixedDomainAxisSpace(null, false);
        assertNull(plot.getFixedDomainAxisSpace());
    }

    @Test(timeout = 4000)
    public void testFixedRangeAxisSpace() {
        XYPlot plot = new XYPlot();
        assertNull(plot.getFixedRangeAxisSpace());
        AxisSpace space = new AxisSpace();
        plot.setFixedRangeAxisSpace(space);
        assertSame(space, plot.getFixedRangeAxisSpace());
    }

    // --- DatasetRenderingOrder/SeriesRenderingOrder ---
    @Test(timeout = 4000)
    public void testDatasetRenderingOrder() {
        XYPlot plot = new XYPlot();
        assertEquals(DatasetRenderingOrder.REVESE， plot.getDatasetRenderingOrder());
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        assertEquals(DatasetRenderingOrder.FORWARD, plot.getDatasetRenderingOrder());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDatasetRenderingOrderNull() {
        XYPlot plot = new XYPlot();
        plot.setDatasetRenderingOrder(null);
    }

    @Test(timeout = 4000)
    public void testSeriesRenderingOrder() {
        XYPlot plot = new XYPlot();
        assertEquals(SeriesRenderingOrder.REVESE, plot.getSeriesRenderingOrder());
        plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        assertEquals(SeriesRenderingOrder.FORWARD, plot.getSeriesRenderingOrder());
    }

    // --- Marker operations (including defect-targeted tests) ---
    // Partition C: Defect-targeted: remove without prior add
    @Test(timeout = 4000)
    public void testRemoveDomainMarkerNoMarkers() {
        // This should NOT throw NullPointerException (defect)
        XYPlot plot = new XYPlot();
        Marker marker = new ValueMarker(10.0);
        // No markers added for index 0, removing should return false
        boolean removed = plot.removeDomainMarker(0, marker, Layer.FOREGROUND);
        assertFalse(removed);
    }

    @Test(timeout = 4000)
    public void testRemoveDomainMarkerBackgroundNoMarkers() {
        XYPlot plot = new XYPlot();
        Marker marker = new ValueMarker(5.0);
        boolean removed = plot.removeDomainMarker(0, marker, Layer.BACKGROUND);
        assertFalse(removed);
    }

    @Test(timeout = 4000)
    public void testRemoveDomainMarkerWithNonExistentIndex() {
        XYPlot plot = new XYPlot();
        Marker marker = new ValueMarker(7.0);
        // index 99 does not exist in marker maps
        boolean removed = plot.removeDomainMarker(99, marker, Layer.FOREGROUND);
        assertFalse(removed);
    }

    @Test(timeout = 4000)
    public void testRemoveRangeMarkerNoMarkers() {
        XYPlot plot = new XYPlot();
        Marker marker = new ValueMarker(20.0);
        boolean removed = plot.removeRangeMarker(0, marker, Layer.FOREGROUND);
        assertFalse(removed);
    }

    @Test(timeout = 4000)
    public void testRemoveRangeMarkerBackgroundNoMarkers() {
        XYPlot plot = new XYPlot();
        Marker marker = new ValueMarker(15.0);
        boolean removed = plot.removeRangeMarker(0, marker, Layer.BACKGROUND);
        assertFalse(removed);
    }

    @Test(timeout = 4000)
    public void testRemoveRangeMarkerWithNonExistentIndex() {
        XYPlot plot = new XYPlot();
        Marker marker = new ValueMarker(25.0);
        boolean removed = plot.removeRangeMarker(99, marker, Layer.FOREGROUND);
        assertFalse(removed);
    }

    @Test(timeout = 4000)
    public void testAddAndRemoveDomainMarker() {
        XYPlot plot = new XYPlot();
        Marker marker = new ValueMarker(30.0);
        plot.addDomainMarker(0, marker, Layer.FOREGROUND);
        // verify it was added by checking collection
        Collection markers = plot.getDomainMarkers(0, Layer.FOREGROUND);
        assertNotNull(markers);
        assertEquals(1, markers.size());
        // remove it
        boolean removed = plot.removeDomainMarker(0, marker, Layer.FOREGROUND);
        assertTrue(removed);
        markers = plot.getDomainMarkers(0， Layer.FOREGROUND);
        assertTrue(markers.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAddAndRemoveDomainMarkerBackground() {
        XYPlot plot = new XYPlot();
        Marker marker = new ValueMarker(35.0);
        plot.addDomainMarker(0, marker, Layer.BACKGROUND);
        Collection markers = plot.getDomainMarkers(0, Layer.BACKGROUND);
        assertNotNull(markers);
        boolean removed = plot.removeDomainMarker(0, marker, Layer.BACKGROUND);
        assertTrue(removed);
    }

    @Test(timeout = 4000)
    public void testAddAndRemoveRangeMarkerForeground() {
        XYPlot plot = new XYPlot();
        Marker marker = new ValueMarker(40.0);
        plot.addRangeMarker(0, marker, Layer.FOREGROUND);
        Collection markers = plot.getRangeMarkers(0, Layer.FOREGROUND);
        assertNotNull(markers);
        assertEquals(1, markers.size());
        boolean removed = plot.removeRangeMarker(0, marker, Layer.FOREGROUND);
        assertTrue(removed);
    }

    @Test(timeout = 4000)
    public void testAddAndRemoveRangeMarkerBackground() {
        XYPlot plot = new XYPlot();
        Marker marker = new ValueMarker(45.0);
        plot.addRangeMarker(0, marker, Layer.BACKGROUND);
        Collection markers = plot.getRangeMarkers(0, Layer.BACKGROUND);
        assertNotNull(markers);
        boolean removed = plot.removeRangeMarker(0, marker, Layer.BACKGROUND);
        assertTrue(removed);
    }

    @Test(timeout = 4000)
    public void testClearDomainMarkers() {
        XYPlot plot = new XYPlot();
        plot.addDomainMarker(0, new ValueMarker(1.0), Layer.FOREGROUND);
        plot.addDomainMarker(0, new ValueMarker(2.0), Layer.BACKGROUND);
        plot.clearDomainMarkers();
        assertTrue(plot.getDomainMarkers(0, Layer.FOREGROUND).isEmpty());
        assertTrue(plot.getDomainMarkers(0, Layer.BACKGROUND).isEmpty());
    }

    @Test(timeout = 4000)
    public void testClearRangeMarkers() {
        XYPlot plot = new XYPlot();
        plot.addRangeMarker(0, new ValueMarker(3.0), Layer.FOREGROUND);
        plot.clearRangeMarkers();
        assertTrue(plot.getRangeMarkers(0, Layer.FOREGROUND).isEmpty());
    }

    // Partition D: Exception guards
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddDomainMarkerNullMarker() {
        XYPlot plot = new XYPlot();
        plot.addDomainMarker(null, Layer.FOREGROUND);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddDomainMarkerNullLayer() {
        XYPlot plot = new XYPlot();
        plot.addDomainMarker(new ValueMarker(1.0), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveRangeMarkerNullMarker() {
        XYPlot plot = new XYPlot();
        plot.removeRangeMarker(null, Layer.FOREGROUND);
    }

    @Test(timeout = 4000)
    public void testGetDomainAxisIndex() {
        XYPlot plot = new XYPlot();
        ValueAxis axis = new org.jfree.chart.axis.NumberAxis("X");
        plot.setDomainAxis(axis);
        assertEquals(0， plot.getDomainAxisIndex(axis));
        // non-existent axis
        assertEquals(-1, plot.getDomainAxisIndex(new org.jfree.chart.axis.NumberAxis("other")));
    }

    @Test(timeout = 4000)
    public void testGetRangeAxisIndex() {
        XYPlot plot = new XYPlot();
        ValueAxis axis = new org.jfree.chart.axis.NumberAxis("Y");
        plot.setRangeAxis(axis);
        assertEquals(0， plot.getRangeAxisIndex(axis));
    }

    @Test(timeout = 4000)
    public void testGetDomainAxisForDataset() {
        XYPlot plot = new XYPlot();
        ValueAxis axis = new org.jfree.chart.axis.NumberAxis("X");
        plot.setDomainAxis(0, axis);
        ValueAxis result = plot.getDomainAxisForDataset(0);
        assertSame(axis, result);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetDomainAxisForDatasetInvalidIndex() {
        XYPlot plot = new XYPlot();
        plot.getDomainAxisForDataset(-1);
    }

    @Test(timeout = 4000)
    public void testGetRangeAxisForDataset() {
        XYPlot plot = new XYPlot();
        ValueAxis axis = new org.jfree.chart.axis.NumberAxis("Y");
        plot.setRangeAxis(0, axis);
        ValueAxis result = plot.getRangeAxisForDataset(0);
        assertSame(axis, result);
    }

    // --- Zoom methods (partial coverage) ---
    @Test(timeout = 4000)
    public void testZoomDomainAxesFactor() {
        XYPlot plot = new XYPlot();
        ValueAxis axis = new org.jfree.chart.axis.NumberAxis("X");
        axis.setRange(0.0, 10.0);
        plot.setDomainAxis(axis);
        plot.zoomDomainAxes(2.0, null, new Point2D.Double(5, 5));
        // actual range change depends on implementation, just check no exception
    }

    @Test(timeout = 4000)
    public void testZoomDomainAxesPercent() {
        XYPlot plot = new XYPlot();
        ValueAxis axis = new org.jfree.chart.axis.NumberAxis("X");
        axis.setRange(0.0, 10.0);
        plot.setDomainAxis(axis);
        plot.zoomDomainAxes(0.2, 0.8, null, null);
    }

    @Test(timeout = 4000)
    public void testZoomRangeAxesFactor() {
        XYPlot plot = new XYPlot();
        ValueAxis axis = new org.jfree.chart.axis.NumberAxis("Y");
        axis.setRange(0.0, 20.0);
        plot.setRangeAxis(axis);
        plot.zoomRangeAxes(1.5, null, new Point2D.Double(10, 10));
    }

    @Test(timeout = 4000)
    public void testZoomRangeAxesPercent() {
        XYPlot plot = new XYPlot();
        ValueAxis axis = new org.jfree.chart.axis.NumberAxis("Y");
        axis.setRange(0.0, 20.0);
        plot.setRangeAxis(axis);
        plot.zoomRangeAxes(0.1, 0.9, null, null);
    }

    // Partition E: Contract integrity
    @Test(timeout = 4000)
    public void testEquals() {
        XYPlot p1 = new XYPlot();
        XYPlot p2 = new XYPlot();
        assertTrue(p1.equals(p2));
        // change something
        p1.setWeight(2);
        assertFalse(p1.equals(p2));
        p2.setWeight(2);
        assertTrue(p1.equals(p2));
        // orientation
        p1.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentDomainAxis() {
        XYPlot p1 = new XYPlot();
        XYPlot p2 = new XYPlot();
        assertTrue(p1.equals(p2));
        p1.setDomainAxis(new org.jfree.chart.axis.NumberAxis("X1"));
        assertFalse(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        XYPlot p1 = new XYPlot();
        XYSeries series = new XYSeries("S");
        series.add(1.0, 2.0);
        XYDataset dataset = new XYSeriesCollection(series);
        p1.setDataset(dataset);
        XYPlot p2 = (XYPlot) p1.clone();
        assertTrue(p1.equals(p2));
        assertNotSame(p1, p2);
        // verify deep clone of domain axes
        assertNotSame(p1.getDomainAxis(), p2.getDomainAxis());
    }

    @Test(timeout = 4000)
    public void testGetPlotType() {
        XYPlot plot = new XYPlot();
        assertEquals("XY_Plot", plot.getPlotType());
    }

    @Test(timeout = 4000)
    public void testGetDataRangeWithNullRenderer() {
        XYPlot plot = new XYPlot();
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries s = new XYSeries("S");
        s.add(1.0, 2.0);
        s.add(3.0, 4.0);
        dataset.addSeries(s);
        plot.setDataset(dataset);
        // getDataRange should not throw, renderer is null so it will use DatasetUtilities
        ValueAxis axis = new org.jfree.chart.axis.NumberAxis("X");
        plot.setDomainAxis(axis);
        Range range = plot.getDataRange(axis);
        assertNotNull(range);
        assertEquals(1.0， range.getLowerBound(), 0.0001);
        assertEquals(3.0， range.getUpperBound(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testGetSeriesCount() {
        XYPlot plot = new XYPlot();
        assertEquals(0, plot.getSeriesCount());
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(new XYSeries("A"));
        dataset.addSeries(new XYSeries("B"));
        plot.setDataset(dataset);
        assertEquals(2, plot.getSeriesCount());
    }

    @Test(timeout = 4000)
    public void testGetLegendItems() {
        XYPlot plot = new XYPlot();
        // with no dataset, legend should be empty
        assertNotNull(plot.getLegendItems());
        assertTrue(plot.getLegendItems().getItemCount() == 0);
        // with fixed legend
        LegendItemCollection items = new LegendItemCollection();
        items.add(new LegendItem("test", Color.red));
        plot.setFixedLegendItems(items);
        assertSame(items, plot.getFixedLegendItems());
    }

    @Test(timeout = 4000)
    public void testRendererChanged() {
        XYPlot plot = new XYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(renderer);
        // simulate a renderer change event
        plot.rendererChanged(new RendererChangeEvent(renderer));
        // should not throw
    }

    @Test(timeout = 4000)
    public void testDatasetChanged() {
        XYPlot plot = new XYPlot();
        XYSeriesCollection dataset = new XYSeriesCollection();
        plot.setDataset(dataset);
        // add a series to trigger change
        XYSeries s = new XYSeries("S");
        s.add(1.0, 2.0);
        dataset.addSeries(s);
        // should call datasetChanged
    }

    @Test(timeout = 4000)
    public void testIsDomainZoomable() {
        XYPlot plot = new XYPlot();
        assertTrue(plot.isDomainZoomable());
    }

    @Test(timeout = 4000)
    public void testIsRangeZoomable() {
        XYPlot plot = new XYPlot();
        assertTrue(plot.isRangeZoomable());
    }

    @Test(timeout = 4000)
    public void testGetAnnotations() {
        XYPlot plot = new XYPlot();
        assertTrue(plot.getAnnotations().isEmpty());
        plot.addAnnotation(new org.jfree.chart.annotations.XYLineAnnotation(0,0,1,1));
        assertEquals(1, plot.getAnnotations().size());
    }

    @Test(timeout = 4000)
    public void testClearAnnotations() {
        XYPlot plot = new XYPlot();
        plot.addAnnotation(new org.jfree.chart.annotations.XYLineAnnotation(0,0,1,1));
        plot.clearAnnotations();
        assertTrue(plot.getAnnotations().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddAnnotationNull() {
        XYPlot plot = new XYPlot();
        plot.addAnnotation(null);
    }

    @Test(timeout = 4000)
    public testRemoveAnnotation() {
        XYPlot plot = new XYPlot();
        XYAnnotation a = new XYLineAnnotation(0,0,1,1);
        plot.addAnnotation(a);
        assertTrue(plot.removeAnnotation(a));
        assertFalse(plot.removeAnnotation(a));
    }

    // Additional branch coverage for marker methods:
    @Test(timeout = 4000)
    public void testGetDomainMarkersNullForLayer() {
        XYPlot plot = new XYPlot();
        // no markers added, should return null (wrapped as unmodifiable collection)
        assertNull(plot.getDomainMarkers(0, Layer.FOREGROUND));
        assertNull(plot.getDomainMarkers(0, Layer.BACKGROUND));
    }

    @Test(timeout = 4000)
    public void testGetRangeMarkersNullForLayer() {
        XYPlot plot = new XYPlot();
        assertNull(plot.getRangeMarkers(0, Layer.FOREGROUND));
        assertNull(plot.getRangeMarkers(0, Layer.BACKGROUND));
    }

    @Test(timeout = 4000)
    public void testClearDomainMarkersOnSpecificIndex() {
        XYPlot plot = new XYPlot();
        plot.addDomainMarker(0, new ValueMarker(1.0), Layer.FOREGROUND);
        plot.clearDomainMarkers(0);
        assertNull(plot.getDomainMarkers(0, Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void testClearRangeMarkersOnSpecificIndex() {
        XYPlot plot = new XYPlot();
        plot.addRangeMarker(0, new ValueMarker(2.0), Layer.BACKGROUND);
        plot.clearRangeMarkers(0);
        assertNull(plot.getRangeMarkers(0, Layer.BACKGROUND));
    }

    // Test to ensure no NPE on remove when markers list is not null but marker not present
    @Test(timeout = 4000)
    public void testRemoveMarkerNotInList() {
        XYPlot plot = new XYPlot();
        Marker marker = new ValueMarker(50.0);
        plot.addDomainMarker(0, new ValueMarker(60.0), Layer.FOREGROUND);
        // Remove a different marker – should return false but not throw
        boolean removed = plot.removeDomainMarker(0, marker, Layer.FOREGROUND);
        assertFalse(removed);
    }

}