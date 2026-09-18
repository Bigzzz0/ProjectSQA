package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
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
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Area            | Decision / Branch Condition                     | Tested In Method
 * ---------------------------------------------------------------------------------------------------------
 * Defect Zone: Markers   | removeDomainMarker with non-existent marker     | testRemoveDomainMarkerUnknownDefect
 * Defect Zone: Markers   | removeRangeMarker with non-existent marker      | testRemoveRangeMarkerUnknownDefect
 * Core: Domain Axes      | setDomainAxis / getDomainAxis / Multi-Axes      | testDomainAxesManagement
 * Core: Range Axes       | setRangeAxis / getRangeAxis / Multi-Axes        | testRangeAxesManagement
 * Core: Datasets/Render  | Dataset-to-Axis mapping, Renderer mapping       | testDatasetAndRendererMapping
 * Core: Drawing pipeline | draw() with forward/reverse orders & orientation| testDrawPipelineVerticalAndHorizontal
 * Core: Rendering Order  | Column/Row rendering sort order modifications   | testRenderingOrderConfigurations
 * BVA: Zooming           | zoom(), zoomRangeAxes() percent & anchor values | testZoomMethods
 * BVA: Axis Locations    | AxisLocation resolve & opposite mappings        | testAxisLocationsAndEdges
 * Defensive: Null Guards | Null argument validation on setters             | testSettersNullGuards
 * Contract: Lifecycle    | equals(), hashCode(), clone(), serialization    | testCloningAndSerializationIntegrity
 * ---------------------------------------------------------------------------------------------------------
 */
public class CategoryPlotGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Known Regressions)
    // =========================================================================

    /**
     * Targets Defects4J bug where calling removeDomainMarker for a marker that does
     * not exist in the map results in a NullPointerException instead of returning false.
     */
    @Test(timeout = 4000)
    public void testRemoveDomainMarkerUnknownDefect() {
        CategoryPlot plot = new CategoryPlot();
        CategoryMarker marker = new CategoryMarker("Category 1");
        // Layer.FOREGROUND has no collection created yet for index 0
        boolean removedForeground = plot.removeDomainMarker(0, marker, Layer.FOREGROUND, true);
        assertFalse("Removing non-existent domain marker in FOREGROUND must return false, not throw NPE", removedForeground);

        // Layer.BACKGROUND has no collection created yet for index 0
        boolean removedBackground = plot.removeDomainMarker(0, marker, Layer.BACKGROUND, true);
        assertFalse("Removing non-existent domain marker in BACKGROUND must return false, not throw NPE", removedBackground);

        // Helper delegator
        boolean removedShort = plot.removeDomainMarker(marker);
        assertFalse("Helper removeDomainMarker(Marker) must return false, not throw NPE", removedShort);
    }

    /**
     * Targets Defects4J bug where calling removeRangeMarker for a marker that does
     * not exist in foreground/background maps results in a NullPointerException.
     */
    @Test(timeout = 4000)
    public void testRemoveRangeMarkerUnknownDefect() {
        CategoryPlot plot = new CategoryPlot();
        ValueMarker marker = new ValueMarker(42.0);

        // FOREGROUND collection is empty/null initially for index 0
        boolean removedForeground = plot.removeRangeMarker(0, marker, Layer.FOREGROUND, true);
        assertFalse("Removing non-existent range marker in FOREGROUND must return false, not throw NPE", removedForeground);

        // Ensure secondary index does not crash with NPE
        boolean removedSecondary = plot.removeRangeMarker(1, marker, Layer.BACKGROUND, true);
        assertFalse("Removing range marker on uninitialized secondary index must return false", removedSecondary);

        // Helper delegator
        boolean removedShort = plot.removeRangeMarker(marker);
        assertFalse("Helper removeRangeMarker(Marker) must return false, not throw NPE", removedShort);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testDomainAxesManagement() {
        CategoryAxis axis1 = new CategoryAxis("Axis 1");
        CategoryAxis axis2 = new CategoryAxis("Axis 2");
        CategoryPlot plot = new CategoryPlot(null, axis1, null, null);

        assertEquals(axis1, plot.getDomainAxis(0));
        assertEquals(0, plot.getDomainAxisIndex(axis1));
        assertEquals(-1, plot.getDomainAxisIndex(axis2));

        plot.setDomainAxis(1, axis2);
        assertEquals(2, plot.getDomainAxisCount());
        assertEquals(axis2, plot.getDomainAxis(1));
        assertEquals(1, plot.getDomainAxisIndex(axis2));

        plot.clearDomainAxes();
        assertNull(plot.getDomainAxis(0));
        assertEquals(0, plot.getDomainAxisCount());
    }

    @Test(timeout = 4000)
    public void testRangeAxesManagement() {
        NumberAxis rangeAxis1 = new NumberAxis("Range 1");
        NumberAxis rangeAxis2 = new NumberAxis("Range 2");
        CategoryPlot plot = new CategoryPlot(null, null, rangeAxis1, null);

        assertEquals(rangeAxis1, plot.getRangeAxis(0));
        assertEquals(0, plot.getRangeAxisIndex(rangeAxis1));
        assertEquals(-1, plot.getRangeAxisIndex(rangeAxis2));

        plot.setRangeAxis(1, rangeAxis2);
        assertEquals(2, plot.getRangeAxisCount());
        assertEquals(rangeAxis2, plot.getRangeAxis(1));
        assertEquals(1, plot.getRangeAxisIndex(rangeAxis2));

        plot.clearRangeAxes();
        assertNull(plot.getRangeAxis(0));
        assertEquals(0, plot.getRangeAxisCount());
    }

    @Test(timeout = 4000)
    public void testDatasetAndRendererMapping() {
        DefaultCategoryDataset ds0 = new DefaultCategoryDataset();
        ds0.addValue(1.0, "R0", "C0");
        DefaultCategoryDataset ds1 = new DefaultCategoryDataset();
        ds1.addValue(2.0, "R1", "C1");

        BarRenderer renderer0 = new BarRenderer();
        LineAndShapeRenderer renderer1 = new LineAndShapeRenderer();

        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(0, ds0);
        plot.setDataset(1, ds1);
        plot.setRenderer(0, renderer0);
        plot.setRenderer(1, renderer1);

        assertEquals(2, plot.getDatasetCount());
        assertEquals(ds0, plot.getDataset(0));
        assertEquals(ds1, plot.getDataset(1));
        assertEquals(renderer0, plot.getRenderer(0));
        assertEquals(renderer1, plot.getRenderer(1));

        assertEquals(renderer0, plot.getRendererForDataset(ds0));
        assertEquals(renderer1, plot.getRendererForDataset(ds1));
        assertEquals(0, plot.getIndexOf(renderer0));
        assertEquals(1, plot.getIndexOf(renderer1));

        plot.mapDatasetToDomainAxis(1, 1);
        plot.mapDatasetToRangeAxis(1, 1);
        CategoryAxis domainAxis1 = new CategoryAxis("D1");
        ValueAxis rangeAxis1 = new NumberAxis("R1");
        plot.setDomainAxis(1, domainAxis1);
        plot.setRangeAxis(1, rangeAxis1);

        assertEquals(domainAxis1, plot.getDomainAxisForDataset(1));
        assertEquals(rangeAxis1, plot.getRangeAxisForDataset(1));
    }

    @Test(timeout = 4000)
    public void testRenderingOrderConfigurations() {
        CategoryPlot plot = new CategoryPlot();

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        assertEquals(DatasetRenderingOrder.FORWARD, plot.getDatasetRenderingOrder());

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        assertEquals(DatasetRenderingOrder.REVERSE, plot.getDatasetRenderingOrder());

        plot.setColumnRenderingOrder(SortOrder.DESCENDING);
        assertEquals(SortOrder.DESCENDING, plot.getColumnRenderingOrder());

        plot.setRowRenderingOrder(SortOrder.DESCENDING);
        assertEquals(SortOrder.DESCENDING, plot.getRowRenderingOrder());
    }

    @Test(timeout = 4000)
    public void testGridlinesAndCrosshairProperties() {
        CategoryPlot plot = new CategoryPlot();

        plot.setDomainGridlinesVisible(true);
        assertTrue(plot.isDomainGridlinesVisible());
        plot.setDomainGridlinePosition(CategoryAnchor.START);
        assertEquals(CategoryAnchor.START, plot.getDomainGridlinePosition());
        Stroke stroke = new BasicStroke(2.0f);
        plot.setDomainGridlineStroke(stroke);
        assertEquals(stroke, plot.getDomainGridlineStroke());
        plot.setDomainGridlinePaint(Color.RED);
        assertEquals(Color.RED, plot.getDomainGridlinePaint());

        plot.setRangeGridlinesVisible(false);
        assertFalse(plot.isRangeGridlinesVisible());
        plot.setRangeGridlineStroke(stroke);
        assertEquals(stroke, plot.getRangeGridlineStroke());
        plot.setRangeGridlinePaint(Color.GREEN);
        assertEquals(Color.GREEN, plot.getRangeGridlinePaint());

        plot.setRangeCrosshairVisible(true);
        assertTrue(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairValue(5.5, true);
        assertEquals(5.5, plot.getRangeCrosshairValue(), 1e-9);
        plot.setRangeCrosshairLockedOnData(false);
        assertFalse(plot.isRangeCrosshairLockedOnData());
        plot.setRangeCrosshairStroke(stroke);
        assertEquals(stroke, plot.getRangeCrosshairStroke());
        plot.setRangeCrosshairPaint(Color.YELLOW);
        assertEquals(Color.YELLOW, plot.getRangeCrosshairPaint());
    }

    @Test(timeout = 4000)
    public void testAnnotationsAndLegend() {
        CategoryPlot plot = new CategoryPlot();
        CategoryTextAnnotation annotation = new CategoryTextAnnotation("Note", "Cat1", 2.0);

        plot.addAnnotation(annotation);
        assertEquals(1, plot.getAnnotations().size());
        assertTrue(plot.getAnnotations().contains(annotation));

        boolean removed = plot.removeAnnotation(annotation);
        assertTrue(removed);
        assertEquals(0, plot.getAnnotations().size());

        plot.addAnnotation(annotation);
        plot.clearAnnotations();
        assertEquals(0, plot.getAnnotations().size());

        LegendItemCollection fixedLegends = new LegendItemCollection();
        fixedLegends.add(new LegendItem("Series 1"));
        plot.setFixedLegendItems(fixedLegends);
        assertEquals(fixedLegends, plot.getFixedLegendItems());
        assertEquals(1, plot.getLegendItems().getItemCount());
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testZoomMethods() {
        CategoryPlot plot = new CategoryPlot();
        NumberAxis axis = new NumberAxis("Range");
        axis.setRange(0.0, 100.0);
        plot.setRangeAxis(axis);
        plot.setAnchorValue(50.0);

        assertTrue(plot.isRangeZoomable());
        assertFalse(plot.isDomainZoomable());

        // Zoom in by factor 0.5
        plot.zoom(0.5);
        Range range = axis.getRange();
        assertEquals(25.0, range.getLength(), 1e-9);
        assertEquals(37.5, range.getLowerBound(), 1e-9);
        assertEquals(62.5, range.getUpperBound(), 1e-9);

        // Zoom 0.0 restores auto-range
        plot.zoom(0.0);
        assertTrue(axis.isAutoRange());

        // Zoom range with anchor
        plot.zoomRangeAxes(0.5, new PlotRenderingInfo(null), new Point2D.Double(0.0, 50.0), false);
        plot.zoomRangeAxes(0.2, 0.8, new PlotRenderingInfo(null), new Point2D.Double(0.0, 50.0));
    }

    @Test(timeout = 4000)
    public void testAxisLocationsAndEdges() {
        CategoryPlot plot = new CategoryPlot();

        plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getDomainAxisLocation());
        assertEquals(RectangleEdge.TOP, plot.getDomainAxisEdge());

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(RectangleEdge.RIGHT, plot.getDomainAxisEdge());

        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getRangeAxisLocation());
        assertEquals(RectangleEdge.BOTTOM, plot.getRangeAxisEdge());

        plot.setOrientation(PlotOrientation.VERTICAL);
        assertEquals(RectangleEdge.LEFT, plot.getRangeAxisEdge());

        // Secondary axis opposite resolving
        assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getRangeAxisLocation(1));
    }

    @Test(timeout = 4000)
    public void testDrawPipelineVerticalAndHorizontal() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        dataset.addValue(20.0, "R1", "C2");

        CategoryAxis domainAxis = new CategoryAxis("Categories");
        NumberAxis rangeAxis = new NumberAxis("Values");
        BarRenderer renderer = new BarRenderer();

        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        plot.addDomainMarker(new CategoryMarker("C1"), Layer.FOREGROUND);
        plot.addRangeMarker(new ValueMarker(15.0), Layer.FOREGROUND);
        plot.setRangeCrosshairVisible(true);
        plot.setRangeCrosshairValue(15.0);

        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);

        // Vertical rendering with Forward dataset order
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.draw(g2, area, new Point2D.Double(100, 100), null, new PlotRenderingInfo(null));

        // Horizontal rendering with Reverse dataset order
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        plot.draw(g2, area, new Point2D.Double(100, 100), null, new PlotRenderingInfo(null));

        // Degenerate small draw area branch (< MINIMUM_WIDTH_TO_DRAW)
        Rectangle2D tinyArea = new Rectangle2D.Double(0, 0, 5, 5);
        plot.draw(g2, tinyArea, null, null, null);

        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testGetDataRangeCalculations() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(-5.0, "R1", "C1");
        dataset.addValue(25.0, "R1", "C2");

        CategoryAxis domainAxis = new CategoryAxis("Domain");
        NumberAxis rangeAxis = new NumberAxis("Range");
        BarRenderer renderer = new BarRenderer();

        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);

        Range dataRange = plot.getDataRange(rangeAxis);
        assertNotNull(dataRange);
        assertEquals(-5.0, dataRange.getLowerBound(), 1e-9);
        assertEquals(25.0, dataRange.getUpperBound(), 1e-9);

        NumberAxis unmappedAxis = new NumberAxis("Unmapped");
        Range nullRange = plot.getDataRange(unmappedAxis);
        assertNull(nullRange);
    }

    @Test(timeout = 4000)
    public void testMarkersAdditionAndClearance() {
        CategoryPlot plot = new CategoryPlot();
        CategoryMarker domainMarker1 = new CategoryMarker("C1");
        CategoryMarker domainMarker2 = new CategoryMarker("C2");
        ValueMarker rangeMarker1 = new ValueMarker(10.0);

        plot.addDomainMarker(0, domainMarker1, Layer.FOREGROUND, true);
        plot.addDomainMarker(0, domainMarker2, Layer.BACKGROUND, true);
        plot.addRangeMarker(0, rangeMarker1, Layer.FOREGROUND, true);

        Collection fgDomain = plot.getDomainMarkers(0, Layer.FOREGROUND);
        assertEquals(1, fgDomain.size());
        assertTrue(fgDomain.contains(domainMarker1));

        Collection bgDomain = plot.getDomainMarkers(0, Layer.BACKGROUND);
        assertEquals(1, bgDomain.size());
        assertTrue(bgDomain.contains(domainMarker2));

        Collection fgRange = plot.getRangeMarkers(0, Layer.FOREGROUND);
        assertEquals(1, fgRange.size());
        assertTrue(fgRange.contains(rangeMarker1));

        // Successfully remove existing
        assertTrue(plot.removeDomainMarker(0, domainMarker1, Layer.FOREGROUND, true));
        assertTrue(plot.removeRangeMarker(0, rangeMarker1, Layer.FOREGROUND, true));

        // Clear all
        plot.addDomainMarker(domainMarker1);
        plot.clearDomainMarkers();
        assertNull(plot.getDomainMarkers(Layer.FOREGROUND));

        plot.clearRangeMarkers();
        assertNull(plot.getRangeMarkers(Layer.BACKGROUND));
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
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
    public void testGetDomainAxisIndexNull() {
        new CategoryPlot().getDomainAxisIndex(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetRangeAxisIndexNull() {
        new CategoryPlot().getRangeAxisIndex(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainAxisLocationIndexZeroNull() {
        new CategoryPlot().setDomainAxisLocation(0, null, true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeAxisLocationIndexZeroNull() {
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
    public void testAddAnnotationNull() {
        new CategoryPlot().addAnnotation(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveAnnotationNull() {
        new CategoryPlot().removeAnnotation(null);
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        CategoryPlot plot1 = new CategoryPlot();
        CategoryPlot plot2 = new CategoryPlot();

        assertEquals(plot1, plot1);
        assertEquals(plot1, plot2);
        assertEquals(plot1.hashCode(), plot2.hashCode());

        plot1.setOrientation(PlotOrientation.HORIZONTAL);
        assertNotEquals(plot1, plot2);
        plot2.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(plot1, plot2);

        plot1.setWeight(5);
        assertNotEquals(plot1, plot2);
        plot2.setWeight(5);
        assertEquals(plot1, plot2);

        plot1.setAnchorValue(12.34);
        assertNotEquals(plot1, plot2);
        plot2.setAnchorValue(12.34);
        assertEquals(plot1, plot2);
    }

    @Test(timeout = 4000)
    public void testCloningAndSerializationIntegrity() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5.0, "Row", "Col");
        CategoryAxis domainAxis = new CategoryAxis("D");
        NumberAxis rangeAxis = new NumberAxis("R");
        BarRenderer renderer = new BarRenderer();

        CategoryPlot original = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        original.setFixedDomainAxisSpace(new AxisSpace());
        original.setFixedRangeAxisSpace(new AxisSpace());

        // Clone test
        CategoryPlot cloned = (CategoryPlot) original.clone();
        assertNotSame(original, cloned);
        assertSame(cloned.getClass(), original.getClass());
        assertEquals(original, cloned);

        // Verify deep copy of domain and range axes
        assertNotSame(original.getDomainAxis(), cloned.getDomainAxis());
        assertNotSame(original.getRangeAxis(), cloned.getRangeAxis());

        // Serialization test
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
            out.writeObject(original);
        }

        CategoryPlot deserialized;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
            deserialized = (CategoryPlot) in.readObject();
        }

        assertNotSame(original, deserialized);
        assertEquals(original, deserialized);
        assertEquals(original.getDataset().getValue("Row", "Col"),
                     deserialized.getDataset().getValue("Row", "Col"));
    }

    @Test(timeout = 4000)
    public void testPlotChangeListenerNotification() {
        CategoryPlot plot = new CategoryPlot();
        final int[] eventCount = new int[1];

        plot.addChangeListener(new PlotChangeListener() {
            @Override
            public void plotChanged(PlotChangeEvent event) {
                eventCount[0]++;
            }
        });

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(1, eventCount[0]);

        plot.setDomainGridlinesVisible(true);
        assertEquals(2, eventCount[0]);

        plot.setRangeGridlinesVisible(false);
        assertEquals(3, eventCount[0]);

        plot.datasetChanged(new DatasetChangeEvent(this, null));
        assertEquals(4, eventCount[0]);
    }

    @Test(timeout = 4000)
    public void testGetCategoriesAndCategoriesForAxis() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "Alpha");
        dataset.addValue(2.0, "R1", "Beta");

        CategoryAxis domainAxis = new CategoryAxis("X");
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, new NumberAxis("Y"), new BarRenderer());

        List categories = plot.getCategories();
        assertNotNull(categories);
        assertEquals(2, categories.size());
        assertTrue(categories.contains("Alpha"));
        assertTrue(categories.contains("Beta"));

        List categoriesForAxis = plot.getCategoriesForAxis(domainAxis);
        assertEquals(2, categoriesForAxis.size());
        assertTrue(categoriesForAxis.contains("Alpha"));
        assertTrue(categoriesForAxis.contains("Beta"));
    }

    @Test(timeout = 4000)
    public void testHandleClick() {
        CategoryPlot plot = new CategoryPlot(null, new CategoryAxis("X"), new NumberAxis("Y"), new BarRenderer());
        PlotRenderingInfo info = new PlotRenderingInfo(null);
        info.setDataArea(new Rectangle2D.Double(0.0, 0.0, 200.0, 200.0));

        plot.handleClick(50, 50, info);
        // Anchor value should be updated based on 50 java2D Y-coordinate
        assertNotEquals(0.0, plot.getAnchorValue());
    }
}