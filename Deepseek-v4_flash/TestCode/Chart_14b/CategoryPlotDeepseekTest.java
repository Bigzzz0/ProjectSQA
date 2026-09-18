package org.jfree.chart.plot;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.List;
import java.util.Collection;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.SortOrder;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.LegendItemCollection;
import org.jfree.data.category.DefaultCategoryDataset;

/* [Branch & Defect Analysis Matrix]
 * 
 * PARTITION A: Core Functional Logic & State Transitions
 * - Constructor with null args, full args
 * - get/setOrientation, get/setAxisOffset
 * - get/setDomainAxis, get/setRangeAxis with various indices
 * - get/setDataset, dataset mapping methods
 * - get/setRenderer, renderer index
 * - get/setDatasetRenderingOrder, column/row rendering order
 * - get/setDomainGridlinesVisible, get/setRangeGridlinesVisible
 * - get/setDomainGridlinePosition, get/setDomainGridlineStroke/Paint
 * - get/setRangeGridlineStroke/Paint
 * - Marker management (add/get/clear/remove for domain/range)
 * - Annotation management (add/remove/clear/get)
 * - Anchor value, crosshair get/set
 * - Fixed legend items
 * - Weight, fixed axis space
 * 
 * PARTITION B: Boundary Value Analysis (BVA) & Extremes
 * - Null dataset, null axis, null renderer in constructor
 * - Negative/zero indices for axes/datasets
 * - Empty dataset, single row/column dataset
 * - Setting axis at high indices (beyond current size)
 * - getDomainAxisIndex with null axis (expect exception)
 * - getRangeAxisIndex with null axis (expect exception)
 * - Null arguments to setOrientation, setAxisOffset, setLocation methods
 * - Null marker/layer added to plot
 * - Null annotation added/removed
 * 
 * PARTITION C: Defect-Targeted Branch Zone
 * - **KNOWN DEFECT**: removeDomainMarker and removeRangeMarker methods
 *   cause NullPointerException when markers collection is null.
 *   Test: Remove a marker that was never added → should return false without NPE
 *   Test: Remove a marker from empty plot → should return false without NPE
 *   Test: Remove a marker from a plot with no markers → should return false
 *   
 * PARTITION D: Exception & Defensive Guard Paths
 * - setOrientation(null) → IllegalArgumentException
 * - setAxisOffset(null) → IllegalArgumentException
 * - setDomainAxisLocation(0, null) → IllegalArgumentException
 * - setRangeAxisLocation(0, null) → IllegalArgumentException
 * - setDomainGridlinePosition(null) → IllegalArgumentException
 * - setDomainGridlineStroke(null) → IllegalArgumentException
 * - setDomainGridlinePaint(null) → IllegalArgumentException
 * - setRangeGridlineStroke(null) → IllegalArgumentException
 * - setRangeGridlinePaint(null) → IllegalArgumentException
 * - setRangeCrosshairStroke(null) → IllegalArgumentException
 * - setRangeCrosshairPaint(null) → IllegalArgumentException
 * - addDomainMarker with null marker → IllegalArgumentException
 * - addDomainMarker with null layer → IllegalArgumentException
 * - addRangeMarker with null marker → IllegalArgumentException
 * - addRangeMarker with null layer → IllegalArgumentException
 * - removeRangeMarker with null marker → IllegalArgumentException
 * - addAnnotation with null → IllegalArgumentException
 * - removeAnnotation with null → IllegalArgumentException
 * - setDatasetRenderingOrder(null) → IllegalArgumentException
 * - setColumnRenderingOrder(null) → IllegalArgumentException
 * - setRowRenderingOrder(null) → IllegalArgumentException
 * - getDomainAxisIndex(null) → IllegalArgumentException
 * - getRangeAxisIndex(null) → IllegalArgumentException
 * 
 * PARTITION E: Object Lifecycle & Contract Integrity
 * - equals() with self, null, different type
 * - clone() returns non-null, independent copy
 * - Serialization support (writeObject/readObject)
 * - PublicCloneable interface implementation
 */

public class CategoryPlotDeepseekTest {
    
    /* ======================================================
     * PARTITION A: Core Functional Logic & State Transitions
     * ====================================================== */
    
    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        CategoryPlot plot = new CategoryPlot();
        assertNotNull("Plot should be created", plot);
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        assertNotNull("Domain axis should exist", plot.getDomainAxis());
        assertNotNull("Range axis should exist", plot.getRangeAxis());
        assertNull("Dataset should be null", plot.getDataset());
        assertNull("Renderer should be null", plot.getRenderer());
        assertFalse("Domain gridlines should not be visible by default", 
                plot.isDomainGridlinesVisible());
        assertTrue("Range gridlines should be visible by default", 
                plot.isRangeGridlinesVisible());
        assertEquals("Default anchor value should be 0.0", 0.0, plot.getAnchorValue(), 0.0);
        assertFalse("Crosshair should not be visible by default", 
                plot.isRangeCrosshairVisible());
        assertEquals("Crosshair value should be 0.0", 0.0, plot.getRangeCrosshairValue(), 0.0);
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithAllParams() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Row1", "Col1");
        dataset.addValue(2.0, "Row1", "Col2");
        
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        NumberAxis rangeAxis = new NumberAxis("Range");
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        
        assertSame("Dataset should be set", dataset, plot.getDataset());
        assertSame("Domain axis should be set", domainAxis, plot.getDomainAxis());
        assertSame("Range axis should be set", rangeAxis, plot.getRangeAxis());
        assertSame("Renderer should be set", renderer, plot.getRenderer());
    }
    
    @Test(timeout = 4000)
    public void testSetAndGetOrientation() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(PlotOrientation.HORIZONTAL, plot.getOrientation());
        
        plot.setOrientation(PlotOrientation.VERTICAL);
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
    }
    
    @Test(timeout = 4000)
    public void testSetAndGetAxisOffset() {
        CategoryPlot plot = new CategoryPlot();
        RectangleInsets offset = new RectangleInsets(2.0, 2.0, 2.0, 2.0);
        plot.setAxisOffset(offset);
        assertSame("Axis offset should be set", offset, plot.getAxisOffset());
    }
    
    @Test(timeout = 4000)
    public void testSetAndGetDomainAxis() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis axis = new CategoryAxis("TestAxis");
        plot.setDomainAxis(axis);
        assertSame("Domain axis should be set", axis, plot.getDomainAxis());
    }
    
    @Test(timeout = 4000)
    public void testSetAndGetDomainAxisAtIndex() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis axis1 = new CategoryAxis("Axis1");
        CategoryAxis axis2 = new CategoryAxis("Axis2");
        
        plot.setDomainAxis(0, axis1);
        plot.setDomainAxis(1, axis2);
        
        assertSame(axis1, plot.getDomainAxis(0));
        assertSame(axis2, plot.getDomainAxis(1));
        assertEquals("Should have 2 domain axes", 2, plot.getDomainAxisCount());
    }
    
    @Test(timeout = 4000)
    public void testSetAndGetRangeAxis() {
        CategoryPlot plot = new CategoryPlot();
        NumberAxis axis = new NumberAxis("TestRange");
        plot.setRangeAxis(axis);
        assertSame("Range axis should be set", axis, plot.getRangeAxis());
    }
    
    @Test(timeout = 4000)
    public void testSetAndGetRangeAxisAtIndex() {
        CategoryPlot plot = new CategoryPlot();
        NumberAxis axis1 = new NumberAxis("Range1");
        NumberAxis axis2 = new NumberAxis("Range2");
        
        plot.setRangeAxis(0, axis1);
        plot.setRangeAxis(1, axis2);
        
        assertSame(axis1, plot.getRangeAxis(0));
        assertSame(axis2, plot.getRangeAxis(1));
        assertEquals("Should have 2 range axes", 2, plot.getRangeAxisCount());
    }
    
    @Test(timeout = 4000)
    public void testSetAndGetDataset() {
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        
        plot.setDataset(dataset);
        assertSame("Dataset should be set", dataset, plot.getDataset());
        assertEquals("Dataset count should be 1", 1, plot.getDatasetCount());
    }
    
    @Test(timeout = 4000)
    public void testSetAndGetDatasetAtIndex() {
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(1.0, "R1", "C1");
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        dataset2.addValue(2.0, "R2", "C2");
        
        plot.setDataset(0, dataset1);
        plot.setDataset(1, dataset2);
        
        assertSame(dataset1, plot.getDataset(0));
        assertSame(dataset2, plot.getDataset(1));
    }
    
    @Test(timeout = 4000)
    public void testGetDomainAxisForDataset() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis axis1 = new CategoryAxis("D1");
        CategoryAxis axis2 = new CategoryAxis("D2");
        
        plot.setDomainAxis(0, axis1);
        plot.setDomainAxis(1, axis2);
        
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToDomainAxis(1, 1);
        
        assertSame(axis1, plot.getDomainAxisForDataset(0));
        assertSame(axis2, plot.getDomainAxisForDataset(1));
    }
    
    @Test(timeout = 4000)
    public void testGetRangeAxisForDataset() {
        CategoryPlot plot = new CategoryPlot();
        NumberAxis axis1 = new NumberAxis("R1");
        NumberAxis axis2 = new NumberAxis("R2");
        
        plot.setRangeAxis(0, axis1);
        plot.setRangeAxis(1, axis2);
        
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);
        
        assertSame(axis1, plot.getRangeAxisForDataset(0));
        assertSame(axis2, plot.getRangeAxisForDataset(1));
    }
    
    @Test(timeout = 4000)
    public void testSetAndGetRenderer() {
        CategoryPlot plot = new CategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        
        plot.setRenderer(renderer);
        assertSame("Renderer should be set", renderer, plot.getRenderer());
    }
    
    @Test(timeout = 4000)
    public void testSetAndGetRendererAtIndex() {
        CategoryPlot plot = new CategoryPlot();
        LineAndShapeRenderer r1 = new LineAndShapeRenderer();
        LineAndShapeRenderer r2 = new LineAndShapeRenderer();
        
        plot.setRenderer(0, r1);
        plot.setRenderer(1, r2);
        
        assertSame(r1, plot.getRenderer(0));
        assertSame(r2, plot.getRenderer(1));
    }
    
    @Test(timeout = 4000)
    public void testGetRendererForDataset() {
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        
        plot.setDataset(dataset);
        plot.setRenderer(renderer);
        
        assertSame(renderer, plot.getRendererForDataset(dataset));
    }
    
    @Test(timeout = 4000)
    public void testGetIndexOfRenderer() {
        CategoryPlot plot = new CategoryPlot();
        LineAndShapeRenderer r1 = new LineAndShapeRenderer();
        LineAndShapeRenderer r2 = new LineAndShapeRenderer();
        LineAndShapeRenderer r3 = new LineAndShapeRenderer();
        
        plot.setRenderer(0, r1);
        plot.setRenderer(2, r2);
        
        assertEquals(0, plot.getIndexOf(r1));
        assertEquals(2, plot.getIndexOf(r2));
        assertEquals(-1, plot.getIndexOf(r3));
    }
    
    @Test(timeout = 4000)
    public void testSetAndGetDatasetRenderingOrder() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(DatasetRenderingOrder.REVERSE, plot.getDatasetRenderingOrder());
        
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        assertEquals(DatasetRenderingOrder.FORWARD, plot.getDatasetRenderingOrder());
        
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        assertEquals(DatasetRenderingOrder.REVERSE, plot.getDatasetRenderingOrder());
    }
    
    @Test(timeout = 4000)
    public void testColumnAndRowRenderingOrder() {
        CategoryPlot plot = new CategoryPlot();
        
        assertEquals(SortOrder.ASCENDING, plot.getColumnRenderingOrder());
        assertEquals(SortOrder.ASCENDING, plot.getRowRenderingOrder());
        
        plot.setColumnRenderingOrder(SortOrder.DESCENDING);
        plot.setRowRenderingOrder(SortOrder.DESCENDING);
        
        assertEquals(SortOrder.DESCENDING, plot.getColumnRenderingOrder());
        assertEquals(SortOrder.DESCENDING, plot.getRowRenderingOrder());
        
        plot.setColumnRenderingOrder(SortOrder.ASCENDING);
        assertEquals(SortOrder.ASCENDING, plot.getColumnRenderingOrder());
    }
    
    @Test(timeout = 4000)
    public void testDomainGridlinesVisibility() {
        CategoryPlot plot = new CategoryPlot();
        assertFalse("Default should be false", plot.isDomainGridlinesVisible());
        
        plot.setDomainGridlinesVisible(true);
        assertTrue(plot.isDomainGridlinesVisible());
        
        plot.setDomainGridlinesVisible(false);
        assertFalse(plot.isDomainGridlinesVisible());
    }
    
    @Test(timeout = 4000)
    public void testRangeGridlinesVisibility() {
        CategoryPlot plot = new CategoryPlot();
        assertTrue("Default should be true", plot.isRangeGridlinesVisible());
        
        plot.setRangeGridlinesVisible(false);
        assertFalse(plot.isRangeGridlinesVisible());
        
        plot.setRangeGridlinesVisible(true);
        assertTrue(plot.isRangeGridlinesVisible());
    }
    
    @Test(timeout = 4000)
    public void testDomainGridlinePosition() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(CategoryAnchor.MIDDLE, plot.getDomainGridlinePosition());
        
        plot.setDomainGridlinePosition(CategoryAnchor.START);
        assertEquals(CategoryAnchor.START, plot.getDomainGridlinePosition());
        
        plot.setDomainGridlinePosition(CategoryAnchor.END);
        assertEquals(CategoryAnchor.END, plot.getDomainGridlinePosition());
    }
    
    @Test(timeout = 4000)
    public void testDomainGridlineStrokeAndPaint() {
        CategoryPlot plot = new CategoryPlot();
        Stroke stroke = new BasicStroke(2.0f);
        Color paint = Color.RED;
        
        plot.setDomainGridlineStroke(stroke);
        plot.setDomainGridlinePaint(paint);
        
        assertSame(stroke, plot.getDomainGridlineStroke());
        assertSame(paint, plot.getDomainGridlinePaint());
    }
    
    @Test(timeout = 4000)
    public void testRangeGridlineStrokeAndPaint() {
        CategoryPlot plot = new CategoryPlot();
        Stroke stroke = new BasicStroke(1.5f);
        Color paint = Color.BLUE;
        
        plot.setRangeGridlineStroke(stroke);
        plot.setRangeGridlinePaint(paint);
        
        assertSame(stroke, plot.getRangeGridlineStroke());
        assertSame(paint, plot.getRangeGridlinePaint());
    }
    
    @Test(timeout = 4000)
    public void testAnchorValue() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(0.0, plot.getAnchorValue(), 0.0);
        
        plot.setAnchorValue(50.0);
        assertEquals(50.0, plot.getAnchorValue(), 0.0);
    }
    
    @Test(timeout = 4000)
    public void testRangeCrosshairValue() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(0.0, plot.getRangeCrosshairValue(), 0.0);
        
        plot.setRangeCrosshairValue(100.0);
        assertEquals(100.0, plot.getRangeCrosshairValue(), 0.0);
    }
    
    @Test(timeout = 4000)
    public void testRangeCrosshairVisibility() {
        CategoryPlot plot = new CategoryPlot();
        assertFalse(plot.isRangeCrosshairVisible());
        
        plot.setRangeCrosshairVisible(true);
        assertTrue(plot.isRangeCrosshairVisible());
    }
    
    @Test(timeout = 4000)
    public void testRangeCrosshairLockedOnData() {
        CategoryPlot plot = new CategoryPlot();
        assertTrue(plot.isRangeCrosshairLockedOnData());
        
        plot.setRangeCrosshairLockedOnData(false);
        assertFalse(plot.isRangeCrosshairLockedOnData());
    }
    
    @Test(timeout = 4000)
    public void testRangeCrosshairStrokeAndPaint() {
        CategoryPlot plot = new CategoryPlot();
        Stroke stroke = new BasicStroke(3.0f);
        Color paint = Color.GREEN;
        
        plot.setRangeCrosshairStroke(stroke);
        plot.setRangeCrosshairPaint(paint);
        
        assertSame(stroke, plot.getRangeCrosshairStroke());
        assertSame(paint, plot.getRangeCrosshairPaint());
    }
    
    @Test(timeout = 4000)
    public void testWeight() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(0, plot.getWeight());
        
        plot.setWeight(5);
        assertEquals(5, plot.getWeight());
    }
    
    @Test(timeout = 4000)
    public void testFixedLegendItems() {
        CategoryPlot plot = new CategoryPlot();
        assertNull(plot.getFixedLegendItems());
        
        LegendItemCollection items = new LegendItemCollection();
        plot.setFixedLegendItems(items);
        assertSame(items, plot.getFixedLegendItems());
    }
    
    @Test(timeout = 4000)
    public void testGetLegendItems() {
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Row1", "Col1");
        
        plot.setDataset(dataset);
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        plot.setRenderer(renderer);
        
        LegendItemCollection items = plot.getLegendItems();
        assertNotNull(items);
        assertTrue(items.getItemCount() > 0);
    }
    
    @Test(timeout = 4000)
    public void testGetLegendItemsWithFixed() {
        CategoryPlot plot = new CategoryPlot();
        LegendItemCollection fixed = new LegendItemCollection();
        plot.setFixedLegendItems(fixed);
        
        assertSame(fixed, plot.getLegendItems());
    }
    
    @Test(timeout = 4000)
    public void testDrawSharedDomainAxis() {
        CategoryPlot plot = new CategoryPlot();
        assertFalse(plot.getDrawSharedDomainAxis());
        
        plot.setDrawSharedDomainAxis(true);
        assertTrue(plot.getDrawSharedDomainAxis());
    }
    
    @Test(timeout = 4000)
    public void testIsDomainZoomable() {
        CategoryPlot plot = new CategoryPlot();
        assertFalse(plot.isDomainZoomable());
    }
    
    @Test(timeout = 4000)
    public void testIsRangeZoomable() {
        CategoryPlot plot = new CategoryPlot();
        assertTrue(plot.isRangeZoomable());
    }
    
    /* ======================================================
     * PARTITION B: Boundary Value Analysis (BVA) & Extremes
     * ====================================================== */
    
    @Test(timeout = 4000)
    public void testConstructorWithNullDataset() {
        CategoryPlot plot = new CategoryPlot(null, null, null, null);
        assertNull(plot.getDataset());
        assertNull(plot.getRenderer());
    }
    
    @Test(timeout = 4000)
    public void testSetNullDataset() {
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(null);
        assertNull(plot.getDataset());
    }
    
    @Test(timeout = 4000)
    public void testGetDomainAxisWithNegativeIndex() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis axis = plot.getDomainAxis(-1);
        // Should not throw exception, likely returns null or from parent
        assertNull(axis);
    }
    
    @Test(timeout = 4000)
    public void testGetRangeAxisWithNegativeIndex() {
        CategoryPlot plot = new CategoryPlot();
        ValueAxis axis = plot.getRangeAxis(-1);
        assertNull(axis);
    }
    
    @Test(timeout = 4000)
    public void testGetDatasetWithNonExistingIndex() {
        CategoryPlot plot = new CategoryPlot();
        assertNull(plot.getDataset(99));
    }
    
    @Test(timeout = 4000)
    public void testGetRendererWithNonExistingIndex() {
        CategoryPlot plot = new CategoryPlot();
        assertNull(plot.getRenderer(99));
    }
    
    @Test(timeout = 4000)
    public void testClearDomainAxes() {
        CategoryPlot plot = new CategoryPlot();
        plot.setDomainAxis(new CategoryAxis("Test"));
        assertEquals(1, plot.getDomainAxisCount());
        
        plot.clearDomainAxes();
        assertEquals(0, plot.getDomainAxisCount());
    }
    
    @Test(timeout = 4000)
    public void testClearRangeAxes() {
        CategoryPlot plot = new CategoryPlot();
        plot.setRangeAxis(new NumberAxis("Test"));
        assertEquals(1, plot.getRangeAxisCount());
        
        plot.clearRangeAxes();
        assertEquals(0, plot.getRangeAxisCount());
    }
    
    @Test(timeout = 4000)
    public void testGetCategoriesWhenDatasetNull() {
        CategoryPlot plot = new CategoryPlot();
        assertNull(plot.getCategories());
    }
    
    @Test(timeout = 4000)
    public void testGetCategoriesWithDataset() {
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R1", "C2");
        plot.setDataset(dataset);
        
        List categories = plot.getCategories();
        assertNotNull(categories);
        assertEquals(2, categories.size());
        assertTrue(categories.contains("C1"));
        assertTrue(categories.contains("C2"));
    }
    
    @Test(timeout = 4000)
    public void testGetCategoriesForAxis() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis axis = new CategoryAxis("Test");
        plot.setDomainAxis(axis);
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "Col1");
        dataset.addValue(2.0, "R1", "Col2");
        plot.setDataset(dataset);
        plot.mapDatasetToDomainAxis(0, 0);
        
        List categories = plot.getCategoriesForAxis(axis);
        assertEquals(2, categories.size());
    }
    
    @Test(timeout = 4000)
    public void testConfigureDomainAxes() {
        CategoryPlot plot = new CategoryPlot();
        // Should not throw exception even with no axes
        plot.configureDomainAxes();
    }
    
    @Test(timeout = 4000)
    public void testConfigureRangeAxes() {
        CategoryPlot plot = new CategoryPlot();
        plot.configureRangeAxes();
    }
    
    @Test(timeout = 4000)
    public void testGetPlotType() {
        CategoryPlot plot = new CategoryPlot();
        assertNotNull(plot.getPlotType());
    }
    
    /* ======================================================
     * PARTITION C: Defect-Targeted Branch Zone
     * ====================================================== */
    
    @Test(timeout = 4000)
    public void testRemoveDomainMarkerFromEmptyPlot() {
        CategoryPlot plot = new CategoryPlot();
        
        // Should return false without throwing NullPointerException
        // This targets the known defect: removeDomainMarker with null markers map
        Marker marker = new ValueMarker(5.0);
        boolean removed = plot.removeDomainMarker(marker);
        assertFalse("Removing marker from empty plot should return false", removed);
    }
    
    @Test(timeout = 4000)
    public void testRemoveDomainMarkerFromEmptyLayer() {
        CategoryPlot plot = new CategoryPlot();
        
        // Test remove from FOREGROUND layer when no markers exist
        Marker marker = new ValueMarker(10.0);
        boolean removed = plot.removeDomainMarker(marker, Layer.FOREGROUND);
        assertFalse("Removing marker from empty foreground should return false", removed);
        
        // Test remove from BACKGROUND layer when no markers exist
        removed = plot.removeDomainMarker(marker, Layer.BACKGROUND);
        assertFalse("Removing marker from empty background should return false", removed);
    }
    
    @Test(timeout = 4000)
    public void testRemoveDomainMarkerWithIndexFromEmptyPlot() {
        CategoryPlot plot = new CategoryPlot();
        
        Marker marker = new ValueMarker(15.0);
        boolean removed = plot.removeDomainMarker(0, marker, Layer.FOREGROUND);
        assertFalse("Removing marker by index from empty plot should return false", removed);
    }
    
    @Test(timeout = 4000)
    public void testRemoveDomainMarkerWithNotifyFromEmptyPlot() {
        CategoryPlot plot = new CategoryPlot();
        
        Marker marker = new ValueMarker(20.0);
        boolean removed = plot.removeDomainMarker(0, marker, Layer.FOREGROUND, true);
        assertFalse("Removing marker with notify from empty plot should return false", removed);
        
        removed = plot.removeDomainMarker(0, marker, Layer.BACKGROUND, false);
        assertFalse("Removing marker with no notify from empty plot should return false", removed);
    }
    
    @Test(timeout = 4000)
    public void testRemoveDomainMarkerAfterAddAndRemove() {
        CategoryPlot plot = new CategoryPlot();
        
        Marker marker1 = new ValueMarker(1.0);
        Marker marker2 = new ValueMarker(2.0);
        
        // Add then remove the marker
        plot.addDomainMarker(marker1, Layer.FOREGROUND);
        boolean removed1 = plot.removeDomainMarker(marker1, Layer.FOREGROUND);
        assertTrue("Should successfully remove the marker that was added", removed1);
        
        // Now try to remove again - should return false without NPE
        boolean removed2 = plot.removeDomainMarker(marker1, Layer.FOREGROUND);
        assertFalse("Removing already removed marker should return false", removed2);
        
        // Try to remove a different marker that was never added
        boolean removed3 = plot.removeDomainMarker(marker2, Layer.FOREGROUND);
        assertFalse("Removing non-existent marker should return false", removed3);
    }
    
    @Test(timeout = 4000)
    public void testRemoveDomainMarkerFromBackgroundLayer() {
        CategoryPlot plot = new CategoryPlot();
        
        Marker marker = new ValueMarker(25.0);
        
        // Add to background
        plot.addDomainMarker(0, marker, Layer.BACKGROUND);
        
        // Try removing from foreground should fail
        boolean removedForeground = plot.removeDomainMarker(marker, Layer.FOREGROUND);
        assertFalse("Marker in background should not be removable from foreground", removedForeground);
        
        // Remove from background should succeed
        boolean removedBackground = plot.removeDomainMarker(marker, Layer.BACKGROUND);
        assertTrue("Marker in background should be removable from background", removedBackground);
        
        // Second remove should fail
        boolean removedAgain = plot.removeDomainMarker(marker, Layer.BACKGROUND);
        assertFalse("Already removed marker should not be removable again", removedAgain);
    }
    
    @Test(timeout = 4000)
    public void testRemoveRangeMarkerFromEmptyPlot() {
        CategoryPlot plot = new CategoryPlot();
        
        // This targets the known defect: removeRangeMarker with null markers map
        Marker marker = new ValueMarker(5.0);
        boolean removed = plot.removeRangeMarker(marker);
        assertFalse("Removing range marker from empty plot should return false", removed);
    }
    
    @Test(timeout = 4000)
    public void testRemoveRangeMarkerFromEmptyLayer() {
        CategoryPlot plot = new CategoryPlot();
        
        Marker marker = new ValueMarker(10.0);
        boolean removed = plot.removeRangeMarker(marker, Layer.FOREGROUND);
        assertFalse("Removing range marker from empty foreground should return false", removed);
        
        removed = plot.removeRangeMarker(marker, Layer.BACKGROUND);
        assertFalse("Removing range marker from empty background should return false", removed);
    }
    
    @Test(timeout = 4000)
    public void testRemoveRangeMarkerWithIndexFromEmptyPlot() {
        CategoryPlot plot = new CategoryPlot();
        
        Marker marker = new ValueMarker(15.0);
        boolean removed = plot.removeRangeMarker(0, marker, Layer.FOREGROUND);
        assertFalse("Removing range marker by index from empty plot should return false", removed);
    }
    
    @Test(timeout = 4000)
    public void testRemoveRangeMarkerWithNotify() {
        CategoryPlot plot = new CategoryPlot();
        
        Marker marker = new ValueMarker(20.0);
        boolean removed = plot.removeRangeMarker(0, marker, Layer.FOREGROUND, true);
        assertFalse("Removing range marker with notify from empty plot should return false", removed);
        
        removed = plot.removeRangeMarker(0, marker, Layer.BACKGROUND, false);
        assertFalse("Removing range marker with no notify from empty plot should return false", removed);
    }
    
    @Test(timeout = 4000)
    public void testRemoveRangeMarkerAfterAddAndRemove() {
        CategoryPlot plot = new CategoryPlot();
        
        Marker marker1 = new ValueMarker(1.0);
        Marker marker2 = new ValueMarker(2.0);
        
        plot.addRangeMarker(marker1, Layer.FOREGROUND);
        boolean removed1 = plot.removeRangeMarker(marker1, Layer.FOREGROUND);
        assertTrue("Should successfully remove the range marker that was added", removed1);
        
        boolean removed2 = plot.removeRangeMarker(marker1, Layer.FOREGROUND);
        assertFalse("Removing already removed range marker should return false", removed2);
        
        boolean removed3 = plot.removeRangeMarker(marker2, Layer.FOREGROUND);
        assertFalse("Removing non-existent range marker should return false", removed3);
    }
    
    @Test(timeout = 4000)
    public void testRemoveRangeMarkerFromBackgroundLayer() {
        CategoryPlot plot = new CategoryPlot();
        
        Marker marker = new ValueMarker(30.0);
        
        plot.addRangeMarker(0, marker, Layer.BACKGROUND);
        
        boolean removedForeground = plot.removeRangeMarker(marker, Layer.FOREGROUND);
        assertFalse("Range marker in background should not be removable from foreground", removedForeground);
        
        boolean removedBackground = plot.removeRangeMarker(marker, Layer.BACKGROUND);
        assertTrue("Range marker in background should be removable from background", removedBackground);
        
        boolean removedAgain = plot.removeRangeMarker(marker, Layer.BACKGROUND);
        assertFalse("Already removed range marker should not be removable again", removedAgain);
    }
    
    @Test(timeout = 4000)
    public void testAddAndRemoveDomainMarkerLifecycle() {
        CategoryPlot plot = new CategoryPlot();
        
        Marker marker = new ValueMarker(100.0);
        
        // Add marker
        plot.addDomainMarker(marker);
        
        // Verify it's in the foreground
        Collection foregroundMarkers = plot.getDomainMarkers(Layer.FOREGROUND);
        assertNotNull(foregroundMarkers);
        assertTrue(foregroundMarkers.contains(marker));
        
        // Remove marker
        boolean removed = plot.removeDomainMarker(marker);
        assertTrue("Should remove the marker that was added", removed);
        
        // Verify it's gone
        foregroundMarkers = plot.getDomainMarkers(Layer.FOREGROUND);
        assertNotNull(foregroundMarkers);
        assertFalse(foregroundMarkers.contains(marker));
    }
    
    @Test(timeout = 4000)
    public void testAddAndRemoveRangeMarkerLifecycle() {
        CategoryPlot plot = new CategoryPlot();
        
        Marker marker = new ValueMarker(200.0);
        
        plot.addRangeMarker(marker);
        
        Collection foregroundMarkers = plot.getRangeMarkers(Layer.FOREGROUND);
        assertNotNull(foregroundMarkers);
        assertTrue(foregroundMarkers.contains(marker));
        
        boolean removed = plot.removeRangeMarker(marker);
        assertTrue("Should remove the range marker that was added", removed);
        
        foregroundMarkers = plot.getRangeMarkers(Layer.FOREGROUND);
        assertNotNull(foregroundMarkers);
        assertFalse(foregroundMarkers.contains(marker));
    }
    
    @Test(timeout = 4000)
    public void testClearDomainMarkers() {
        CategoryPlot plot = new CategoryPlot();
        
        plot.addDomainMarker(new ValueMarker(1.0), Layer.FOREGROUND);
        plot.addDomainMarker(new ValueMarker(2.0), Layer.BACKGROUND);
        
        plot.clearDomainMarkers();
        
        Collection fg = plot.getDomainMarkers(Layer.FOREGROUND);
        Collection bg = plot.getDomainMarkers(Layer.BACKGROUND);
        
        assertTrue("Foreground should be empty after clear", fg == null || fg.isEmpty());
        assertTrue("Background should be empty after clear", bg == null || bg.isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testClearRangeMarkers() {
        CategoryPlot plot = new CategoryPlot();
        
        plot.addRangeMarker(new ValueMarker(1.0), Layer.FOREGROUND);
        plot.addRangeMarker(new ValueMarker(2.0), Layer.BACKGROUND);
        
        plot.clearRangeMarkers();
        
        Collection fg = plot.getRangeMarkers(Layer.FOREGROUND);
        Collection bg = plot.getRangeMarkers(Layer.BACKGROUND);
        
        assertTrue("Foreground range should be empty after clear", fg == null || fg.isEmpty());
        assertTrue("Background range should be empty after clear", bg == null || bg.isEmpty());
    }
    
    /* ======================================================
     * PARTITION D: Exception & Defensive Guard Paths
     * ====================================================== */
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetOrientationNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.setOrientation(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetAxisOffsetNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.setAxisOffset(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainAxisLocationIndex0Null() {
        CategoryPlot plot = new CategoryPlot();
        plot.setDomainAxisLocation(0, null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeAxisLocationIndex0Null() {
        CategoryPlot plot = new CategoryPlot();
        plot.setRangeAxisLocation(0, null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainGridlinePositionNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.setDomainGridlinePosition(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainGridlineStrokeNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.setDomainGridlineStroke(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainGridlinePaintNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.setDomainGridlinePaint(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeGridlineStrokeNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.setRangeGridlineStroke(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeGridlinePaintNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.setRangeGridlinePaint(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeCrosshairStrokeNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.setRangeCrosshairStroke(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeCrosshairPaintNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.setRangeCrosshairPaint(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddDomainMarkerNullMarker() {
        CategoryPlot plot = new CategoryPlot();
        plot.addDomainMarker(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddDomainMarkerNullLayer() {
        CategoryPlot plot = new CategoryPlot();
        plot.addDomainMarker(new ValueMarker(5.0), null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddRangeMarkerNullMarker() {
        CategoryPlot plot = new CategoryPlot();
        plot.addRangeMarker(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddRangeMarkerNullLayer() {
        CategoryPlot plot = new CategoryPlot();
        plot.addRangeMarker(new ValueMarker(5.0), null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveRangeMarkerNullMarker() {
        CategoryPlot plot = new CategoryPlot();
        plot.removeRangeMarker(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddAnnotationNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.addAnnotation(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveAnnotationNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.removeAnnotation(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDatasetRenderingOrderNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.setDatasetRenderingOrder(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetColumnRenderingOrderNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.setColumnRenderingOrder(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRowRenderingOrderNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.setRowRenderingOrder(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetDomainAxisIndexNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.getDomainAxisIndex(null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetRangeAxisIndexNull() {
        CategoryPlot plot = new CategoryPlot();
        plot.getRangeAxisIndex(null);
    }
    
    /* ======================================================
     * PARTITION E: Object Lifecycle & Contract Integrity
     * ====================================================== */
    
    @Test(timeout = 4000)
    public void testEqualsSelf() {
        CategoryPlot plot = new CategoryPlot();
        assertTrue("Plot should equal itself", plot.equals(plot));
    }
    
    @Test(timeout = 4000)
    public void testEqualsNull() {
        CategoryPlot plot = new CategoryPlot();
        assertFalse("Plot should not equal null", plot.equals(null));
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        CategoryPlot plot = new CategoryPlot();
        assertFalse("Plot should not equal a different type", plot.equals("string"));
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentOrientation() {
        CategoryPlot plot1 = new CategoryPlot();
        CategoryPlot plot2 = new CategoryPlot();
        
        assertEquals("Two default plots should be equal", plot1, plot2);
        
        plot1.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse("Plots with different orientation should not be equal", plot1.equals(plot2));
        
        plot2.setOrientation(PlotOrientation.HORIZONTAL);
        assertTrue("Plots with same orientation should be equal again", plot1.equals(plot2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithDifferentAxes() {
        CategoryPlot plot1 = new CategoryPlot();
        CategoryPlot plot2 = new CategoryPlot();
        
        plot1.setDomainAxis(new CategoryAxis("Axis1"));
        assertFalse("Plots with different domain axes should not be equal", plot1.equals(plot2));
    }
    
    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        plot.setDataset(dataset);
        
        CategoryPlot clone = (CategoryPlot) plot.clone();
        assertNotNull("Clone should not be null", clone);
        assertNotSame("Clone should be different object", plot, clone);
        assertEquals("Clone should be equal to original (structurally)", plot, clone);
    }
    
    @Test(timeout = 4000)
    public void testCloneWithAllComponents() throws CloneNotSupportedException {
        CategoryPlot plot = new CategoryPlot();
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        dataset.addValue(20.0, "R1", "C2");
        
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        NumberAxis rangeAxis = new NumberAxis("Range");
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        
        plot.setDataset(dataset);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.setRenderer(renderer);
        
        CategoryPlot clone = (CategoryPlot) plot.clone();
        
        assertNotNull(clone.getDataset());
        assertNotNull(clone.getDomainAxis());
        assertNotNull(clone.getRangeAxis());
        assertNotNull(clone.getRenderer());
        
        // Cloned axes should be independent
        assertNotSame("Domain axis should be cloned", plot.getDomainAxis(), clone.getDomainAxis());
        assertNotSame("Range axis should be cloned", plot.getRangeAxis(), clone.getRangeAxis());
    }
    
    @Test(timeout = 4000)
    public void testPublicCloneableInterface() {
        CategoryPlot plot = new CategoryPlot();
        assertTrue("CategoryPlot should implement PublicCloneable", plot instanceof PublicCloneable);
    }
    
    @Test(timeout = 4000)
    public void testAnnotationsListNotNull() {
        CategoryPlot plot = new CategoryPlot();
        assertNotNull("Annotations list should never be null", plot.getAnnotations());
    }
    
    @Test(timeout = 4000)
    public void testAddAndGetAnnotations() {
        CategoryPlot plot = new CategoryPlot();
        
        CategoryTextAnnotation annotation = new CategoryTextAnnotation("Test", "Category", 1.0);
        plot.addAnnotation(annotation);
        
        List annotations = plot.getAnnotations();
        assertEquals(1, annotations.size());
        assertSame(annotation, annotations.get(0));
    }
    
    @Test(timeout = 4000)
    public void testRemoveAnnotation() {
        CategoryPlot plot = new CategoryPlot();
        CategoryTextAnnotation annotation = new CategoryTextAnnotation("Test", "Cat", 1.0);
        
        plot.addAnnotation(annotation);
        assertTrue("Should remove annotation", plot.removeAnnotation(annotation));
        assertTrue("Annotations should be empty", plot.getAnnotations().isEmpty());
        
        assertFalse("Removing non-existent annotation should return false", 
                plot.removeAnnotation(annotation));
    }
    
    @Test(timeout = 4000)
    public void testClearAnnotations() {
        CategoryPlot plot = new CategoryPlot();
        
        plot.addAnnotation(new CategoryTextAnnotation("A", "C1", 1.0));
        plot.addAnnotation(new CategoryTextAnnotation("B", "C2", 2.0));
        
        assertEquals(2, plot.getAnnotations().size());
        plot.clearAnnotations();
        assertTrue(plot.getAnnotations().isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testGetRangeAxisEdge() {
        CategoryPlot plot = new CategoryPlot();
        assertNotNull("Range axis edge should not be null", plot.getRangeAxisEdge());
        assertNotNull("Range axis edge for index 0 should not be null", plot.getRangeAxisEdge(0));
    }
    
    @Test(timeout = 4000)
    public void testGetDomainAxisLocationWithIndex() {
        CategoryPlot plot = new CategoryPlot();
        assertNotNull("Domain axis location should not be null", plot.getDomainAxisLocation(0));
        
        plot.setDomainAxisLocation(1, AxisLocation.TOP_OR_RIGHT);
        assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getDomainAxisLocation(1));
    }
    
    @Test(timeout = 4000)
    public void testGetRangeAxisLocationWithIndex() {
        CategoryPlot plot = new CategoryPlot();
        assertNotNull("Range axis location should not be null", plot.getRangeAxisLocation(0));
        
        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getRangeAxisLocation(1));
    }
}