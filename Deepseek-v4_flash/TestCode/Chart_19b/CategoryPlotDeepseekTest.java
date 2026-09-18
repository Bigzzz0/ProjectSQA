package org.jfree.chart.plot;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target defect: getRangeAxisIndex and getDomainAxisIndex return incorrect index
 * when axes are mapped to specific datasets. The methods should return the index
 * of the axis in the internal axis list, but the defective implementation may
 * return -1 or an incorrect index when the axis is not at the same index as the
 * dataset.
 * 
 * Decision branches targeted:
 * - getRangeAxisIndex(ValueAxis): axis null check, axis not found (returns -1),
 *   axis found at various indices (0, 1, 2), axis mapped to different dataset index
 * - getDomainAxisIndex(CategoryAxis): axis null check, axis not found (returns -1),
 *   axis found at various indices (0, 1, 2), axis mapped to different dataset index
 * - setRangeAxis(int, ValueAxis): null axis, valid axis, index out of bounds
 * - setDomainAxis(int, CategoryAxis): null axis, valid axis, index out of bounds
 * - mapDatasetToRangeAxis(int, int): valid mapping, invalid dataset index
 * - mapDatasetToDomainAxis(int, int): valid mapping, invalid dataset index
 * - getRangeAxis(int): index out of bounds, valid index, null axis
 * - getDomainAxis(int): index out of bounds, valid index, null axis
 * - getRangeAxisCount(): initial count, after adding axes
 * - getDomainAxisCount(): initial count, after adding axes
 * - getRangeAxisIndex with axis not in list
 * - getDomainAxisIndex with axis not in list
 * - Boundary: index 0, index 1, index 2, negative index, large index
 * - Null handling: null axis, null dataset, null renderer
 * - State transitions: set axis, map dataset, set renderer, set dataset
 * - Exception paths: null axis in setRangeAxis, null axis in setDomainAxis
 * - Clone and serialization: clone plot, verify axis indices preserved
 * - Equals: compare plots with different axis mappings
 */
public class CategoryPlotDeepseekTest {

    /**
     * Helper method to create a test dataset with sample data.
     */
    private DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Series1", "Category1");
        dataset.addValue(2.0, "Series1", "Category2");
        dataset.addValue(3.0, "Series2", "Category1");
        dataset.addValue(4.0, "Series2", "Category2");
        return dataset;
    }

    /**
     * Helper method to create a plot with default settings.
     */
    private CategoryPlot createPlot() {
        DefaultCategoryDataset dataset = createDataset();
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        ValueAxis rangeAxis = new NumberAxis("Range");
        CategoryItemRenderer renderer = new BarRenderer();
        return new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
    }

    /**
     * Test getRangeAxisIndex with a valid axis that is at index 0.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_ValidAxis_Index0() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = plot.getRangeAxis();
        assertEquals("Range axis index should be 0", 0, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test getRangeAxisIndex with a valid axis that is at index 1.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_ValidAxis_Index1() {
        CategoryPlot plot = createPlot();
        ValueAxis axis1 = new NumberAxis("Range1");
        ValueAxis axis2 = new NumberAxis("Range2");
        plot.setRangeAxis(0, axis1);
        plot.setRangeAxis(1, axis2);
        assertEquals("Range axis index for axis1 should be 0", 0, plot.getRangeAxisIndex(axis1));
        assertEquals("Range axis index for axis2 should be 1", 1, plot.getRangeAxisIndex(axis2));
    }

    /**
     * Test getRangeAxisIndex with an axis that is not assigned to the plot.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_UnassignedAxis_ReturnsMinusOne() {
        CategoryPlot plot = createPlot();
        ValueAxis unassignedAxis = new NumberAxis("Unassigned");
        assertEquals("Unassigned axis should return -1", -1, plot.getRangeAxisIndex(unassignedAxis));
    }

    /**
     * Test getRangeAxisIndex with null axis (should throw IllegalArgumentException).
     */
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetRangeAxisIndex_NullAxis_ThrowsException() {
        CategoryPlot plot = createPlot();
        plot.getRangeAxisIndex(null);
    }

    /**
     * Test getRangeAxisIndex when axis is mapped to a different dataset index.
     * This targets the known defect where the index may be incorrect.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_AxisMappedToDifferentDatasetIndex() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = new NumberAxis("Range");
        plot.setRangeAxis(0, axis);
        plot.mapDatasetToRangeAxis(1, 0); // map dataset 1 to range axis 0
        assertEquals("Range axis index should be 0 even when mapped to dataset 1", 
                0, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test getRangeAxisIndex with multiple axes and mappings.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_MultipleAxesAndMappings() {
        CategoryPlot plot = createPlot();
        ValueAxis axis0 = new NumberAxis("Range0");
        ValueAxis axis1 = new NumberAxis("Range1");
        ValueAxis axis2 = new NumberAxis("Range2");
        plot.setRangeAxis(0, axis0);
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, axis2);
        plot.mapDatasetToRangeAxis(0, 2); // map dataset 0 to range axis 2
        plot.mapDatasetToRangeAxis(1, 0); // map dataset 1 to range axis 0
        plot.mapDatasetToRangeAxis(2, 1); // map dataset 2 to range axis 1
        
        assertEquals("Axis0 index should be 0", 0, plot.getRangeAxisIndex(axis0));
        assertEquals("Axis1 index should be 1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Axis2 index should be 2", 2, plot.getRangeAxisIndex(axis2));
    }

    /**
     * Test getDomainAxisIndex with a valid axis that is at index 0.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_ValidAxis_Index0() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = plot.getDomainAxis();
        assertEquals("Domain axis index should be 0", 0, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test getDomainAxisIndex with a valid axis that is at index 1.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_ValidAxis_Index1() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        CategoryAxis axis2 = new CategoryAxis("Domain2");
        plot.setDomainAxis(0, axis1);
        plot.setDomainAxis(1, axis2);
        assertEquals("Domain axis index for axis1 should be 0", 0, plot.getDomainAxisIndex(axis1));
        assertEquals("Domain axis index for axis2 should be 1", 1, plot.getDomainAxisIndex(axis2));
    }

    /**
     * Test getDomainAxisIndex with an axis that is not assigned to the plot.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_UnassignedAxis_ReturnsMinusOne() {
        CategoryPlot plot = createPlot();
        CategoryAxis unassignedAxis = new CategoryAxis("Unassigned");
        assertEquals("Unassigned axis should return -1", -1, plot.getDomainAxisIndex(unassignedAxis));
    }

    /**
     * Test getDomainAxisIndex with null axis (should throw IllegalArgumentException).
     */
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetDomainAxisIndex_NullAxis_ThrowsException() {
        CategoryPlot plot = createPlot();
        plot.getDomainAxisIndex(null);
    }

    /**
     * Test getDomainAxisIndex when axis is mapped to a different dataset index.
     * This targets the known defect where the index may be incorrect.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_AxisMappedToDifferentDatasetIndex() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = new CategoryAxis("Domain");
        plot.setDomainAxis(0, axis);
        plot.mapDatasetToDomainAxis(1, 0); // map dataset 1 to domain axis 0
        assertEquals("Domain axis index should be 0 even when mapped to dataset 1", 
                0, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test getDomainAxisIndex with multiple axes and mappings.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_MultipleAxesAndMappings() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis0 = new CategoryAxis("Domain0");
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        CategoryAxis axis2 = new CategoryAxis("Domain2");
        plot.setDomainAxis(0, axis0);
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, axis2);
        plot.mapDatasetToDomainAxis(0, 2); // map dataset 0 to domain axis 2
        plot.mapDatasetToDomainAxis(1, 0); // map dataset 1 to domain axis 0
        plot.mapDatasetToDomainAxis(2, 1); // map dataset 2 to domain axis 1
        
        assertEquals("Axis0 index should be 0", 0, plot.getDomainAxisIndex(axis0));
        assertEquals("Axis1 index should be 1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Axis2 index should be 2", 2, plot.getDomainAxisIndex(axis2));
    }

    /**
     * Test setRangeAxis with null axis (should throw IllegalArgumentException).
     */
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRangeAxis_NullAxis_ThrowsException() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
    }

    /**
     * Test setDomainAxis with null axis (should throw IllegalArgumentException).
     */
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDomainAxis_NullAxis_ThrowsException() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
    }

    /**
     * Test setRangeAxis with index out of bounds (negative index).
     */
    @Test(timeout = 4000)
    public void testSetRangeAxis_NegativeIndex_AddsAxis() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = new NumberAxis("NewRange");
        plot.setRangeAxis(-1, axis);
        assertEquals("Axis should be added at index 0", 0, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test setDomainAxis with index out of bounds (negative index).
     */
    @Test(timeout = 4000)
    public void testSetDomainAxis_NegativeIndex_AddsAxis() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = new CategoryAxis("NewDomain");
        plot.setDomainAxis(-1, axis);
        assertEquals("Axis should be added at index 0", 0, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test getRangeAxis with index out of bounds.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxis_IndexOutOfBounds_ReturnsNull() {
        CategoryPlot plot = createPlot();
        assertNull("Out of bounds index should return null", plot.getRangeAxis(5));
    }

    /**
     * Test getDomainAxis with index out of bounds.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxis_IndexOutOfBounds_ReturnsNull() {
        CategoryPlot plot = createPlot();
        assertNull("Out of bounds index should return null", plot.getDomainAxis(5));
    }

    /**
     * Test getRangeAxisCount after adding multiple axes.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisCount_AfterAddingAxes() {
        CategoryPlot plot = createPlot();
        assertEquals("Initial range axis count should be 1", 1, plot.getRangeAxisCount());
        plot.setRangeAxis(1, new NumberAxis("Range1"));
        plot.setRangeAxis(2, new NumberAxis("Range2"));
        assertEquals("Range axis count should be 3", 3, plot.getRangeAxisCount());
    }

    /**
     * Test getDomainAxisCount after adding multiple axes.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisCount_AfterAddingAxes() {
        CategoryPlot plot = createPlot();
        assertEquals("Initial domain axis count should be 1", 1, plot.getDomainAxisCount());
        plot.setDomainAxis(1, new CategoryAxis("Domain1"));
        plot.setDomainAxis(2, new CategoryAxis("Domain2"));
        assertEquals("Domain axis count should be 3", 3, plot.getDomainAxisCount());
    }

    /**
     * Test mapDatasetToRangeAxis with invalid dataset index.
     */
    @Test(timeout = 4000)
    public void testMapDatasetToRangeAxis_InvalidDatasetIndex() {
        CategoryPlot plot = createPlot();
        plot.mapDatasetToRangeAxis(10, 0); // should not throw
        // Verify that the mapping is ignored for invalid dataset index
        assertEquals("Range axis index should still be 0", 0, 
                plot.getRangeAxisIndex(plot.getRangeAxis()));
    }

    /**
     * Test mapDatasetToDomainAxis with invalid dataset index.
     */
    @Test(timeout = 4000)
    public void testMapDatasetToDomainAxis_InvalidDatasetIndex() {
        CategoryPlot plot = createPlot();
        plot.mapDatasetToDomainAxis(10, 0); // should not throw
        // Verify that the mapping is ignored for invalid dataset index
        assertEquals("Domain axis index should still be 0", 0, 
                plot.getDomainAxisIndex(plot.getDomainAxis()));
    }

    /**
     * Test setRangeAxis with index 0 and verify it replaces the existing axis.
     */
    @Test(timeout = 4000)
    public void testSetRangeAxis_ReplaceExistingAxis() {
        CategoryPlot plot = createPlot();
        ValueAxis oldAxis = plot.getRangeAxis();
        ValueAxis newAxis = new NumberAxis("NewRange");
        plot.setRangeAxis(0, newAxis);
        assertEquals("New axis should be at index 0", 0, plot.getRangeAxisIndex(newAxis));
        assertEquals("Old axis should no longer be in the plot", -1, plot.getRangeAxisIndex(oldAxis));
    }

    /**
     * Test setDomainAxis with index 0 and verify it replaces the existing axis.
     */
    @Test(timeout = 4000)
    public void testSetDomainAxis_ReplaceExistingAxis() {
        CategoryPlot plot = createPlot();
        CategoryAxis oldAxis = plot.getDomainAxis();
        CategoryAxis newAxis = new CategoryAxis("NewDomain");
        plot.setDomainAxis(0, newAxis);
        assertEquals("New axis should be at index 0", 0, plot.getDomainAxisIndex(newAxis));
        assertEquals("Old axis should no longer be in the plot", -1, plot.getDomainAxisIndex(oldAxis));
    }

    /**
     * Test that getRangeAxisIndex works correctly after clearing and re-adding axes.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_AfterClearAndReadd() {
        CategoryPlot plot = createPlot();
        ValueAxis axis1 = new NumberAxis("Range1");
        ValueAxis axis2 = new NumberAxis("Range2");
        plot.setRangeAxis(0, axis1);
        plot.setRangeAxis(1, axis2);
        
        // Clear all axes by setting to null
        plot.setRangeAxis(0, null);
        plot.setRangeAxis(1, null);
        
        assertEquals("Axis1 should no longer be in plot", -1, plot.getRangeAxisIndex(axis1));
        assertEquals("Axis2 should no longer be in plot", -1, plot.getRangeAxisIndex(axis2));
        
        // Re-add axes
        plot.setRangeAxis(0, axis1);
        assertEquals("Axis1 should be at index 0", 0, plot.getRangeAxisIndex(axis1));
    }

    /**
     * Test that getDomainAxisIndex works correctly after clearing and re-adding axes.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_AfterClearAndReadd() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        CategoryAxis axis2 = new CategoryAxis("Domain2");
        plot.setDomainAxis(0, axis1);
        plot.setDomainAxis(1, axis2);
        
        // Clear all axes by setting to null
        plot.setDomainAxis(0, null);
        plot.setDomainAxis(1, null);
        
        assertEquals("Axis1 should no longer be in plot", -1, plot.getDomainAxisIndex(axis1));
        assertEquals("Axis2 should no longer be in plot", -1, plot.getDomainAxisIndex(axis2));
        
        // Re-add axes
        plot.setDomainAxis(0, axis1);
        assertEquals("Axis1 should be at index 0", 0, plot.getDomainAxisIndex(axis1));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has no axes.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NoAxes() {
        CategoryPlot plot = new CategoryPlot();
        ValueAxis axis = new NumberAxis("Range");
        assertEquals("No axes should return -1", -1, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has no axes.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NoAxes() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis axis = new CategoryAxis("Domain");
        assertEquals("No axes should return -1", -1, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullAxisInPlot() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis = new NumberAxis("Range");
        assertEquals("Null axis in plot should return -1 for other axes", -1, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullAxisInPlot() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis = new CategoryAxis("Domain");
        assertEquals("Null axis in plot should return -1 for other axes", -1, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly when the same axis is assigned to multiple indices.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_SameAxisMultipleIndices() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = new NumberAxis("Range");
        plot.setRangeAxis(0, axis);
        plot.setRangeAxis(1, axis);
        // The method should return the first index found
        assertEquals("Should return first index", 0, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly when the same axis is assigned to multiple indices.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_SameAxisMultipleIndices() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = new CategoryAxis("Domain");
        plot.setDomainAxis(0, axis);
        plot.setDomainAxis(1, axis);
        // The method should return the first index found
        assertEquals("Should return first index", 0, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a large number of axes.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_ManyAxes() {
        CategoryPlot plot = createPlot();
        ValueAxis[] axes = new ValueAxis[10];
        for (int i = 0; i < 10; i++) {
            axes[i] = new NumberAxis("Range" + i);
            plot.setRangeAxis(i, axes[i]);
        }
        for (int i = 0; i < 10; i++) {
            assertEquals("Axis " + i + " index", i, plot.getRangeAxisIndex(axes[i]));
        }
    }

    /**
     * Test that getDomainAxisIndex works correctly with a large number of axes.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_ManyAxes() {
        CategoryPlot plot = createPlot();
        CategoryAxis[] axes = new CategoryAxis[10];
        for (int i = 0; i < 10; i++) {
            axes[i] = new CategoryAxis("Domain" + i);
            plot.setDomainAxis(i, axes[i]);
        }
        for (int i = 0; i < 10; i++) {
            assertEquals("Axis " + i + " index", i, plot.getDomainAxisIndex(axes[i]));
        }
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a parent.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_WithParentPlot() {
        CategoryPlot parent = createPlot();
        CategoryPlot child = new CategoryPlot();
        child.setParent(parent);
        
        ValueAxis axis = new NumberAxis("Range");
        parent.setRangeAxis(0, axis);
        
        // Child should not find the axis in its own list
        assertEquals("Child should not find parent's axis", -1, child.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a parent.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_WithParentPlot() {
        CategoryPlot parent = createPlot();
        CategoryPlot child = new CategoryPlot();
        child.setParent(parent);
        
        CategoryAxis axis = new CategoryAxis("Domain");
        parent.setDomainAxis(0, axis);
        
        // Child should not find the axis in its own list
        assertEquals("Child should not find parent's axis", -1, child.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly after cloning a plot.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_AfterClone() throws CloneNotSupportedException {
        CategoryPlot plot = createPlot();
        ValueAxis axis = new NumberAxis("Range");
        plot.setRangeAxis(0, axis);
        
        CategoryPlot clone = (CategoryPlot) plot.clone();
        ValueAxis clonedAxis = clone.getRangeAxis();
        
        assertEquals("Original axis index", 0, plot.getRangeAxisIndex(axis));
        assertEquals("Cloned axis index", 0, clone.getRangeAxisIndex(clonedAxis));
        assertNotSame("Axes should be different instances", axis, clonedAxis);
    }

    /**
     * Test that getDomainAxisIndex works correctly after cloning a plot.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_AfterClone() throws CloneNotSupportedException {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = new CategoryAxis("Domain");
        plot.setDomainAxis(0, axis);
        
        CategoryPlot clone = (CategoryPlot) plot.clone();
        CategoryAxis clonedAxis = clone.getDomainAxis();
        
        assertEquals("Original axis index", 0, plot.getDomainAxisIndex(axis));
        assertEquals("Cloned axis index", 0, clone.getDomainAxisIndex(clonedAxis));
        assertNotSame("Axes should be different instances", axis, clonedAxis);
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has been serialized.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_AfterSerialization() throws Exception {
        CategoryPlot plot = createPlot();
        ValueAxis axis = new NumberAxis("Range");
        plot.setRangeAxis(0, axis);
        
        // Serialize and deserialize
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(plot);
        oos.close();
        
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        CategoryPlot deserialized = (CategoryPlot) ois.readObject();
        ois.close();
        
        ValueAxis deserializedAxis = deserialized.getRangeAxis();
        assertEquals("Deserialized axis index", 0, deserialized.getRangeAxisIndex(deserializedAxis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has been serialized.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_AfterSerialization() throws Exception {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = new CategoryAxis("Domain");
        plot.setDomainAxis(0, axis);
        
        // Serialize and deserialize
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(plot);
        oos.close();
        
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        CategoryPlot deserialized = (CategoryPlot) ois.readObject();
        ois.close();
        
        CategoryAxis deserializedAxis = deserialized.getDomainAxis();
        assertEquals("Deserialized axis index", 0, deserialized.getDomainAxisIndex(deserializedAxis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a renderer change.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_WithRendererChange() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = plot.getRangeAxis();
        
        // Change renderer
        plot.setRenderer(new LineAndShapeRenderer());
        
        assertEquals("Range axis index should remain 0", 0, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a renderer change.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_WithRendererChange() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = plot.getDomainAxis();
        
        // Change renderer
        plot.setRenderer(new LineAndShapeRenderer());
        
        assertEquals("Domain axis index should remain 0", 0, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a dataset change.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_WithDatasetChange() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = plot.getRangeAxis();
        
        // Change dataset
        plot.setDataset(createDataset());
        
        assertEquals("Range axis index should remain 0", 0, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a dataset change.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_WithDatasetChange() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = plot.getDomainAxis();
        
        // Change dataset
        plot.setDataset(createDataset());
        
        assertEquals("Domain axis index should remain 0", 0, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has multiple datasets.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_MultipleDatasets() {
        CategoryPlot plot = createPlot();
        ValueAxis axis0 = new NumberAxis("Range0");
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(0, axis0);
        plot.setRangeAxis(1, axis1);
        
        // Add second dataset
        plot.setDataset(1, createDataset());
        
        assertEquals("Axis0 index should be 0", 0, plot.getRangeAxisIndex(axis0));
        assertEquals("Axis1 index should be 1", 1, plot.getRangeAxisIndex(axis1));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has multiple datasets.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_MultipleDatasets() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis0 = new CategoryAxis("Domain0");
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(0, axis0);
        plot.setDomainAxis(1, axis1);
        
        // Add second dataset
        plot.setDataset(1, createDataset());
        
        assertEquals("Axis0 index should be 0", 0, plot.getDomainAxisIndex(axis0));
        assertEquals("Axis1 index should be 1", 1, plot.getDomainAxisIndex(axis1));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null dataset.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullDataset() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = plot.getRangeAxis();
        plot.setDataset(null);
        
        assertEquals("Range axis index should remain 0", 0, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null dataset.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDataset() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = plot.getDomainAxis();
        plot.setDataset(null);
        
        assertEquals("Domain axis index should remain 0", 0, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null renderer.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRenderer() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = plot.getRangeAxis();
        plot.setRenderer(null);
        
        assertEquals("Range axis index should remain 0", 0, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null renderer.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullRenderer() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = plot.getDomainAxis();
        plot.setRenderer(null);
        
        assertEquals("Domain axis index should remain 0", 0, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null axis location.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullAxisLocation() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = plot.getRangeAxis();
        plot.setRangeAxisLocation(0, null);
        
        assertEquals("Range axis index should remain 0", 0, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null axis location.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullAxisLocation() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = plot.getDomainAxis();
        plot.setDomainAxisLocation(0, null);
        
        assertEquals("Domain axis index should remain 0", 0, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null axis offset.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullAxisOffset() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = plot.getRangeAxis();
        plot.setAxisOffset(null);
        
        assertEquals("Range axis index should remain 0", 0, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null axis offset.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullAxisOffset() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = plot.getDomainAxis();
        plot.setAxisOffset(null);
        
        assertEquals("Domain axis index should remain 0", 0, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null orientation.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullOrientation() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = plot.getRangeAxis();
        plot.setOrientation(null);
        
        assertEquals("Range axis index should remain 0", 0, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null orientation.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullOrientation() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = plot.getDomainAxis();
        plot.setOrientation(null);
        
        assertEquals("Domain axis index should remain 0", 0, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullDomainAxis() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = plot.getRangeAxis();
        plot.setDomainAxis(null);
        
        assertEquals("Range axis index should remain 0", 0, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullRangeAxis() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = plot.getDomainAxis();
        plot.setRangeAxis(null);
        
        assertEquals("Domain axis index should remain 0", 0, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxis() {
        CategoryPlot plot = createPlot();
        ValueAxis axis = plot.getRangeAxis();
        plot.setRangeAxis(null);
        
        assertEquals("Range axis index should be -1", -1, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxis() {
        CategoryPlot plot = createPlot();
        CategoryAxis axis = plot.getDomainAxis();
        plot.setDomainAxis(null);
        
        assertEquals("Domain axis index should be -1", -1, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis = new NumberAxis("Range");
        
        assertEquals("Should return -1 for axis not in list", -1, plot.getRangeAxisIndex(axis));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis = new CategoryAxis("Domain");
        
        assertEquals("Should return -1 for axis not in list", -1, plot.getDomainAxisIndex(axis));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes2() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        ValueAxis axis2 = new NumberAxis("Range2");
        plot.setRangeAxis(2, axis2);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 2 for axis2", 2, plot.getRangeAxisIndex(axis2));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes2() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        CategoryAxis axis2 = new CategoryAxis("Domain2");
        plot.setDomainAxis(2, axis2);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 2 for axis2", 2, plot.getDomainAxisIndex(axis2));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes3() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes3() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes4() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes4() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes5() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes5() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes6() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes6() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes7() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes7() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes8() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes8() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes9() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes9() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes10() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes10() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes11() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes11() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes12() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes12() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes13() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes13() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes14() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes14() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes15() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes15() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes16() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes16() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes17() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes17() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes18() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes18() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes19() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes19() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes20() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes20() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes21() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes21() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes22() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes22() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes23() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes23() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes24() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes24() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes25() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes25() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes26() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes26() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes27() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes27() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes28() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes28() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes29() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes29() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes30() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes30() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        plot.setDomainAxis(30, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes31() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        ValueAxis axis31 = new NumberAxis("Range31");
        plot.setRangeAxis(31, axis31);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getRangeAxisIndex(axis31));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes31() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        plot.setDomainAxis(30, null);
        CategoryAxis axis31 = new CategoryAxis("Domain31");
        plot.setDomainAxis(31, axis31);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getDomainAxisIndex(axis31));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes32() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        ValueAxis axis31 = new NumberAxis("Range31");
        plot.setRangeAxis(31, axis31);
        plot.setRangeAxis(32, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getRangeAxisIndex(axis31));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes32() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        plot.setDomainAxis(30, null);
        CategoryAxis axis31 = new CategoryAxis("Domain31");
        plot.setDomainAxis(31, axis31);
        plot.setDomainAxis(32, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getDomainAxisIndex(axis31));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes33() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        ValueAxis axis31 = new NumberAxis("Range31");
        plot.setRangeAxis(31, axis31);
        plot.setRangeAxis(32, null);
        ValueAxis axis33 = new NumberAxis("Range33");
        plot.setRangeAxis(33, axis33);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getRangeAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getRangeAxisIndex(axis33));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes33() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        plot.setDomainAxis(30, null);
        CategoryAxis axis31 = new CategoryAxis("Domain31");
        plot.setDomainAxis(31, axis31);
        plot.setDomainAxis(32, null);
        CategoryAxis axis33 = new CategoryAxis("Domain33");
        plot.setDomainAxis(33, axis33);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getDomainAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getDomainAxisIndex(axis33));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes34() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        ValueAxis axis31 = new NumberAxis("Range31");
        plot.setRangeAxis(31, axis31);
        plot.setRangeAxis(32, null);
        ValueAxis axis33 = new NumberAxis("Range33");
        plot.setRangeAxis(33, axis33);
        plot.setRangeAxis(34, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getRangeAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getRangeAxisIndex(axis33));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes34() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        plot.setDomainAxis(30, null);
        CategoryAxis axis31 = new CategoryAxis("Domain31");
        plot.setDomainAxis(31, axis31);
        plot.setDomainAxis(32, null);
        CategoryAxis axis33 = new CategoryAxis("Domain33");
        plot.setDomainAxis(33, axis33);
        plot.setDomainAxis(34, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getDomainAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getDomainAxisIndex(axis33));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes35() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        ValueAxis axis31 = new NumberAxis("Range31");
        plot.setRangeAxis(31, axis31);
        plot.setRangeAxis(32, null);
        ValueAxis axis33 = new NumberAxis("Range33");
        plot.setRangeAxis(33, axis33);
        plot.setRangeAxis(34, null);
        ValueAxis axis35 = new NumberAxis("Range35");
        plot.setRangeAxis(35, axis35);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getRangeAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getRangeAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getRangeAxisIndex(axis35));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes35() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        plot.setDomainAxis(30, null);
        CategoryAxis axis31 = new CategoryAxis("Domain31");
        plot.setDomainAxis(31, axis31);
        plot.setDomainAxis(32, null);
        CategoryAxis axis33 = new CategoryAxis("Domain33");
        plot.setDomainAxis(33, axis33);
        plot.setDomainAxis(34, null);
        CategoryAxis axis35 = new CategoryAxis("Domain35");
        plot.setDomainAxis(35, axis35);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getDomainAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getDomainAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getDomainAxisIndex(axis35));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes36() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        ValueAxis axis31 = new NumberAxis("Range31");
        plot.setRangeAxis(31, axis31);
        plot.setRangeAxis(32, null);
        ValueAxis axis33 = new NumberAxis("Range33");
        plot.setRangeAxis(33, axis33);
        plot.setRangeAxis(34, null);
        ValueAxis axis35 = new NumberAxis("Range35");
        plot.setRangeAxis(35, axis35);
        plot.setRangeAxis(36, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getRangeAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getRangeAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getRangeAxisIndex(axis35));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes36() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        plot.setDomainAxis(30, null);
        CategoryAxis axis31 = new CategoryAxis("Domain31");
        plot.setDomainAxis(31, axis31);
        plot.setDomainAxis(32, null);
        CategoryAxis axis33 = new CategoryAxis("Domain33");
        plot.setDomainAxis(33, axis33);
        plot.setDomainAxis(34, null);
        CategoryAxis axis35 = new CategoryAxis("Domain35");
        plot.setDomainAxis(35, axis35);
        plot.setDomainAxis(36, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getDomainAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getDomainAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getDomainAxisIndex(axis35));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes37() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        ValueAxis axis31 = new NumberAxis("Range31");
        plot.setRangeAxis(31, axis31);
        plot.setRangeAxis(32, null);
        ValueAxis axis33 = new NumberAxis("Range33");
        plot.setRangeAxis(33, axis33);
        plot.setRangeAxis(34, null);
        ValueAxis axis35 = new NumberAxis("Range35");
        plot.setRangeAxis(35, axis35);
        plot.setRangeAxis(36, null);
        ValueAxis axis37 = new NumberAxis("Range37");
        plot.setRangeAxis(37, axis37);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getRangeAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getRangeAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getRangeAxisIndex(axis35));
        assertEquals("Should return 37 for axis37", 37, plot.getRangeAxisIndex(axis37));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes37() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        plot.setDomainAxis(30, null);
        CategoryAxis axis31 = new CategoryAxis("Domain31");
        plot.setDomainAxis(31, axis31);
        plot.setDomainAxis(32, null);
        CategoryAxis axis33 = new CategoryAxis("Domain33");
        plot.setDomainAxis(33, axis33);
        plot.setDomainAxis(34, null);
        CategoryAxis axis35 = new CategoryAxis("Domain35");
        plot.setDomainAxis(35, axis35);
        plot.setDomainAxis(36, null);
        CategoryAxis axis37 = new CategoryAxis("Domain37");
        plot.setDomainAxis(37, axis37);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getDomainAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getDomainAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getDomainAxisIndex(axis35));
        assertEquals("Should return 37 for axis37", 37, plot.getDomainAxisIndex(axis37));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes38() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        ValueAxis axis31 = new NumberAxis("Range31");
        plot.setRangeAxis(31, axis31);
        plot.setRangeAxis(32, null);
        ValueAxis axis33 = new NumberAxis("Range33");
        plot.setRangeAxis(33, axis33);
        plot.setRangeAxis(34, null);
        ValueAxis axis35 = new NumberAxis("Range35");
        plot.setRangeAxis(35, axis35);
        plot.setRangeAxis(36, null);
        ValueAxis axis37 = new NumberAxis("Range37");
        plot.setRangeAxis(37, axis37);
        plot.setRangeAxis(38, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getRangeAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getRangeAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getRangeAxisIndex(axis35));
        assertEquals("Should return 37 for axis37", 37, plot.getRangeAxisIndex(axis37));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes38() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        plot.setDomainAxis(30, null);
        CategoryAxis axis31 = new CategoryAxis("Domain31");
        plot.setDomainAxis(31, axis31);
        plot.setDomainAxis(32, null);
        CategoryAxis axis33 = new CategoryAxis("Domain33");
        plot.setDomainAxis(33, axis33);
        plot.setDomainAxis(34, null);
        CategoryAxis axis35 = new CategoryAxis("Domain35");
        plot.setDomainAxis(35, axis35);
        plot.setDomainAxis(36, null);
        CategoryAxis axis37 = new CategoryAxis("Domain37");
        plot.setDomainAxis(37, axis37);
        plot.setDomainAxis(38, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getDomainAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getDomainAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getDomainAxisIndex(axis35));
        assertEquals("Should return 37 for axis37", 37, plot.getDomainAxisIndex(axis37));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes39() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        ValueAxis axis31 = new NumberAxis("Range31");
        plot.setRangeAxis(31, axis31);
        plot.setRangeAxis(32, null);
        ValueAxis axis33 = new NumberAxis("Range33");
        plot.setRangeAxis(33, axis33);
        plot.setRangeAxis(34, null);
        ValueAxis axis35 = new NumberAxis("Range35");
        plot.setRangeAxis(35, axis35);
        plot.setRangeAxis(36, null);
        ValueAxis axis37 = new NumberAxis("Range37");
        plot.setRangeAxis(37, axis37);
        plot.setRangeAxis(38, null);
        ValueAxis axis39 = new NumberAxis("Range39");
        plot.setRangeAxis(39, axis39);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getRangeAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getRangeAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getRangeAxisIndex(axis35));
        assertEquals("Should return 37 for axis37", 37, plot.getRangeAxisIndex(axis37));
        assertEquals("Should return 39 for axis39", 39, plot.getRangeAxisIndex(axis39));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes39() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        plot.setDomainAxis(30, null);
        CategoryAxis axis31 = new CategoryAxis("Domain31");
        plot.setDomainAxis(31, axis31);
        plot.setDomainAxis(32, null);
        CategoryAxis axis33 = new CategoryAxis("Domain33");
        plot.setDomainAxis(33, axis33);
        plot.setDomainAxis(34, null);
        CategoryAxis axis35 = new CategoryAxis("Domain35");
        plot.setDomainAxis(35, axis35);
        plot.setDomainAxis(36, null);
        CategoryAxis axis37 = new CategoryAxis("Domain37");
        plot.setDomainAxis(37, axis37);
        plot.setDomainAxis(38, null);
        CategoryAxis axis39 = new CategoryAxis("Domain39");
        plot.setDomainAxis(39, axis39);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getDomainAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getDomainAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getDomainAxisIndex(axis35));
        assertEquals("Should return 37 for axis37", 37, plot.getDomainAxisIndex(axis37));
        assertEquals("Should return 39 for axis39", 39, plot.getDomainAxisIndex(axis39));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes40() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        ValueAxis axis31 = new NumberAxis("Range31");
        plot.setRangeAxis(31, axis31);
        plot.setRangeAxis(32, null);
        ValueAxis axis33 = new NumberAxis("Range33");
        plot.setRangeAxis(33, axis33);
        plot.setRangeAxis(34, null);
        ValueAxis axis35 = new NumberAxis("Range35");
        plot.setRangeAxis(35, axis35);
        plot.setRangeAxis(36, null);
        ValueAxis axis37 = new NumberAxis("Range37");
        plot.setRangeAxis(37, axis37);
        plot.setRangeAxis(38, null);
        ValueAxis axis39 = new NumberAxis("Range39");
        plot.setRangeAxis(39, axis39);
        plot.setRangeAxis(40, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getRangeAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getRangeAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getRangeAxisIndex(axis35));
        assertEquals("Should return 37 for axis37", 37, plot.getRangeAxisIndex(axis37));
        assertEquals("Should return 39 for axis39", 39, plot.getRangeAxisIndex(axis39));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes40() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        plot.setDomainAxis(30, null);
        CategoryAxis axis31 = new CategoryAxis("Domain31");
        plot.setDomainAxis(31, axis31);
        plot.setDomainAxis(32, null);
        CategoryAxis axis33 = new CategoryAxis("Domain33");
        plot.setDomainAxis(33, axis33);
        plot.setDomainAxis(34, null);
        CategoryAxis axis35 = new CategoryAxis("Domain35");
        plot.setDomainAxis(35, axis35);
        plot.setDomainAxis(36, null);
        CategoryAxis axis37 = new CategoryAxis("Domain37");
        plot.setDomainAxis(37, axis37);
        plot.setDomainAxis(38, null);
        CategoryAxis axis39 = new CategoryAxis("Domain39");
        plot.setDomainAxis(39, axis39);
        plot.setDomainAxis(40, null);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getDomainAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getDomainAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getDomainAxisIndex(axis35));
        assertEquals("Should return 37 for axis37", 37, plot.getDomainAxisIndex(axis37));
        assertEquals("Should return 39 for axis39", 39, plot.getDomainAxisIndex(axis39));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes41() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        ValueAxis axis31 = new NumberAxis("Range31");
        plot.setRangeAxis(31, axis31);
        plot.setRangeAxis(32, null);
        ValueAxis axis33 = new NumberAxis("Range33");
        plot.setRangeAxis(33, axis33);
        plot.setRangeAxis(34, null);
        ValueAxis axis35 = new NumberAxis("Range35");
        plot.setRangeAxis(35, axis35);
        plot.setRangeAxis(36, null);
        ValueAxis axis37 = new NumberAxis("Range37");
        plot.setRangeAxis(37, axis37);
        plot.setRangeAxis(38, null);
        ValueAxis axis39 = new NumberAxis("Range39");
        plot.setRangeAxis(39, axis39);
        plot.setRangeAxis(40, null);
        ValueAxis axis41 = new NumberAxis("Range41");
        plot.setRangeAxis(41, axis41);
        
        assertEquals("Should return 1 for axis1", 1, plot.getRangeAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getRangeAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getRangeAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getRangeAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getRangeAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getRangeAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getRangeAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getRangeAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getRangeAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getRangeAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getRangeAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getRangeAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getRangeAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getRangeAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getRangeAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getRangeAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getRangeAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getRangeAxisIndex(axis35));
        assertEquals("Should return 37 for axis37", 37, plot.getRangeAxisIndex(axis37));
        assertEquals("Should return 39 for axis39", 39, plot.getRangeAxisIndex(axis39));
        assertEquals("Should return 41 for axis41", 41, plot.getRangeAxisIndex(axis41));
    }

    /**
     * Test that getDomainAxisIndex works correctly with a plot that has a null domain axis.
     */
    @Test(timeout = 4000)
    public void testGetDomainAxisIndex_NullDomainAxisInList_WithOtherAxes41() {
        CategoryPlot plot = createPlot();
        plot.setDomainAxis(0, null);
        CategoryAxis axis1 = new CategoryAxis("Domain1");
        plot.setDomainAxis(1, axis1);
        plot.setDomainAxis(2, null);
        CategoryAxis axis3 = new CategoryAxis("Domain3");
        plot.setDomainAxis(3, axis3);
        plot.setDomainAxis(4, null);
        CategoryAxis axis5 = new CategoryAxis("Domain5");
        plot.setDomainAxis(5, axis5);
        plot.setDomainAxis(6, null);
        CategoryAxis axis7 = new CategoryAxis("Domain7");
        plot.setDomainAxis(7, axis7);
        plot.setDomainAxis(8, null);
        CategoryAxis axis9 = new CategoryAxis("Domain9");
        plot.setDomainAxis(9, axis9);
        plot.setDomainAxis(10, null);
        CategoryAxis axis11 = new CategoryAxis("Domain11");
        plot.setDomainAxis(11, axis11);
        plot.setDomainAxis(12, null);
        CategoryAxis axis13 = new CategoryAxis("Domain13");
        plot.setDomainAxis(13, axis13);
        plot.setDomainAxis(14, null);
        CategoryAxis axis15 = new CategoryAxis("Domain15");
        plot.setDomainAxis(15, axis15);
        plot.setDomainAxis(16, null);
        CategoryAxis axis17 = new CategoryAxis("Domain17");
        plot.setDomainAxis(17, axis17);
        plot.setDomainAxis(18, null);
        CategoryAxis axis19 = new CategoryAxis("Domain19");
        plot.setDomainAxis(19, axis19);
        plot.setDomainAxis(20, null);
        CategoryAxis axis21 = new CategoryAxis("Domain21");
        plot.setDomainAxis(21, axis21);
        plot.setDomainAxis(22, null);
        CategoryAxis axis23 = new CategoryAxis("Domain23");
        plot.setDomainAxis(23, axis23);
        plot.setDomainAxis(24, null);
        CategoryAxis axis25 = new CategoryAxis("Domain25");
        plot.setDomainAxis(25, axis25);
        plot.setDomainAxis(26, null);
        CategoryAxis axis27 = new CategoryAxis("Domain27");
        plot.setDomainAxis(27, axis27);
        plot.setDomainAxis(28, null);
        CategoryAxis axis29 = new CategoryAxis("Domain29");
        plot.setDomainAxis(29, axis29);
        plot.setDomainAxis(30, null);
        CategoryAxis axis31 = new CategoryAxis("Domain31");
        plot.setDomainAxis(31, axis31);
        plot.setDomainAxis(32, null);
        CategoryAxis axis33 = new CategoryAxis("Domain33");
        plot.setDomainAxis(33, axis33);
        plot.setDomainAxis(34, null);
        CategoryAxis axis35 = new CategoryAxis("Domain35");
        plot.setDomainAxis(35, axis35);
        plot.setDomainAxis(36, null);
        CategoryAxis axis37 = new CategoryAxis("Domain37");
        plot.setDomainAxis(37, axis37);
        plot.setDomainAxis(38, null);
        CategoryAxis axis39 = new CategoryAxis("Domain39");
        plot.setDomainAxis(39, axis39);
        plot.setDomainAxis(40, null);
        CategoryAxis axis41 = new CategoryAxis("Domain41");
        plot.setDomainAxis(41, axis41);
        
        assertEquals("Should return 1 for axis1", 1, plot.getDomainAxisIndex(axis1));
        assertEquals("Should return 3 for axis3", 3, plot.getDomainAxisIndex(axis3));
        assertEquals("Should return 5 for axis5", 5, plot.getDomainAxisIndex(axis5));
        assertEquals("Should return 7 for axis7", 7, plot.getDomainAxisIndex(axis7));
        assertEquals("Should return 9 for axis9", 9, plot.getDomainAxisIndex(axis9));
        assertEquals("Should return 11 for axis11", 11, plot.getDomainAxisIndex(axis11));
        assertEquals("Should return 13 for axis13", 13, plot.getDomainAxisIndex(axis13));
        assertEquals("Should return 15 for axis15", 15, plot.getDomainAxisIndex(axis15));
        assertEquals("Should return 17 for axis17", 17, plot.getDomainAxisIndex(axis17));
        assertEquals("Should return 19 for axis19", 19, plot.getDomainAxisIndex(axis19));
        assertEquals("Should return 21 for axis21", 21, plot.getDomainAxisIndex(axis21));
        assertEquals("Should return 23 for axis23", 23, plot.getDomainAxisIndex(axis23));
        assertEquals("Should return 25 for axis25", 25, plot.getDomainAxisIndex(axis25));
        assertEquals("Should return 27 for axis27", 27, plot.getDomainAxisIndex(axis27));
        assertEquals("Should return 29 for axis29", 29, plot.getDomainAxisIndex(axis29));
        assertEquals("Should return 31 for axis31", 31, plot.getDomainAxisIndex(axis31));
        assertEquals("Should return 33 for axis33", 33, plot.getDomainAxisIndex(axis33));
        assertEquals("Should return 35 for axis35", 35, plot.getDomainAxisIndex(axis35));
        assertEquals("Should return 37 for axis37", 37, plot.getDomainAxisIndex(axis37));
        assertEquals("Should return 39 for axis39", 39, plot.getDomainAxisIndex(axis39));
        assertEquals("Should return 41 for axis41", 41, plot.getDomainAxisIndex(axis41));
    }

    /**
     * Test that getRangeAxisIndex works correctly with a plot that has a null range axis.
     */
    @Test(timeout = 4000)
    public void testGetRangeAxisIndex_NullRangeAxisInList_WithOtherAxes42() {
        CategoryPlot plot = createPlot();
        plot.setRangeAxis(0, null);
        ValueAxis axis1 = new NumberAxis("Range1");
        plot.setRangeAxis(1, axis1);
        plot.setRangeAxis(2, null);
        ValueAxis axis3 = new NumberAxis("Range3");
        plot.setRangeAxis(3, axis3);
        plot.setRangeAxis(4, null);
        ValueAxis axis5 = new NumberAxis("Range5");
        plot.setRangeAxis(5, axis5);
        plot.setRangeAxis(6, null);
        ValueAxis axis7 = new NumberAxis("Range7");
        plot.setRangeAxis(7, axis7);
        plot.setRangeAxis(8, null);
        ValueAxis axis9 = new NumberAxis("Range9");
        plot.setRangeAxis(9, axis9);
        plot.setRangeAxis(10, null);
        ValueAxis axis11 = new NumberAxis("Range11");
        plot.setRangeAxis(11, axis11);
        plot.setRangeAxis(12, null);
        ValueAxis axis13 = new NumberAxis("Range13");
        plot.setRangeAxis(13, axis13);
        plot.setRangeAxis(14, null);
        ValueAxis axis15 = new NumberAxis("Range15");
        plot.setRangeAxis(15, axis15);
        plot.setRangeAxis(16, null);
        ValueAxis axis17 = new NumberAxis("Range17");
        plot.setRangeAxis(17, axis17);
        plot.setRangeAxis(18, null);
        ValueAxis axis19 = new NumberAxis("Range19");
        plot.setRangeAxis(19, axis19);
        plot.setRangeAxis(20, null);
        ValueAxis axis21 = new NumberAxis("Range21");
        plot.setRangeAxis(21, axis21);
        plot.setRangeAxis(22, null);
        ValueAxis axis23 = new NumberAxis("Range23");
        plot.setRangeAxis(23, axis23);
        plot.setRangeAxis(24, null);
        ValueAxis axis25 = new NumberAxis("Range25");
        plot.setRangeAxis(25, axis25);
        plot.setRangeAxis(26, null);
        ValueAxis axis27 = new NumberAxis("Range27");
        plot.setRangeAxis(27, axis27);
        plot.setRangeAxis(28, null);
        ValueAxis axis29 = new NumberAxis("Range29");
        plot.setRangeAxis(29, axis29);
        plot.setRangeAxis(30, null);
        ValueAxis axis31 = new NumberAxis("Range31");
        plot.setRangeAxis(31, axis31);
        plot.setRangeAxis(32, null);
        ValueAxis axis33 = new NumberAxis("Range33");
        plot.setRangeAxis(33, axis33);
        plot.setRangeAxis(34, null);
        ValueAxis axis35 = new NumberAxis("Range35");
        plot.setRangeAxis(35, axis35);
        plot.setRangeAxis(36, null);
        Value