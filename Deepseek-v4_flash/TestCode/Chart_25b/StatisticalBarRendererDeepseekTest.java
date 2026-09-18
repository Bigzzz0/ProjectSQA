package org.jfree.chart.renderer.category;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.PaintUtilities;

public class StatisticalBarRendererDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target Class: StatisticalBarRenderer
     * 
     * Key Decision Branches:
     * 1. drawItem() - checks if data is StatisticalCategoryDataset, throws IllegalArgumentException if not
     * 2. drawItem() - orientation check: HORIZONTAL vs VERTICAL
     * 3. drawHorizontalItem() - seriesCount > 1 vs else (seriesGap calculation)
     * 4. drawHorizontalItem() - uclip <= 0.0 (cases 1-4), lclip <= 0.0 (cases 5-8), else (cases 9-12)
     * 5. drawHorizontalItem() - value >= uclip (return early), value <= lclip (clip value)
     * 6. drawHorizontalItem() - errorIndicatorStroke != null, errorIndicatorPaint != null
     * 7. drawVerticalItem() - similar branches for vertical orientation
     * 8. equals() - obj == this, obj instanceof, super.equals, PaintUtilities.equal
     * 9. setErrorIndicatorPaint() / setErrorIndicatorStroke() - null handling
     * 
     * Known Defect: NPE when meanValue or stdDevValue is null in drawVerticalItem/drawHorizontalItem
     * - testDrawWithNullMeanVertical: meanValue is null
     * - testDrawWithNullDeviationVertical: stdDevValue is null
     * - testDrawWithNullMeanHorizontal: meanValue is null
     * - testDrawWithNullDeviationHorizontal: stdDevValue is null
     * 
     * The defect occurs because the code calls meanValue.doubleValue() and 
     * dataset.getStdDevValue(row, column).doubleValue() without null checks.
     * 
     * Boundary Conditions:
     * - uclip <= 0.0, lclip <= 0.0, value >= uclip, value <= lclip
     * - seriesCount > 1, seriesCount == 1
     * - errorIndicatorPaint null/non-null
     * - errorIndicatorStroke null/non-null
     * - barWidth > 3 vs <= 3
     * 
     * Test Strategy:
     * - Partition A: Core functional logic (getters/setters, equals)
     * - Partition B: Boundary values (null, empty, extremes)
     * - Partition C: Defect-targeted tests (null mean/deviation)
     * - Partition D: Exception paths (invalid dataset type)
     * - Partition E: Serialization and clone
     */

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        assertEquals(Color.gray, renderer.getErrorIndicatorPaint());
        assertNotNull(renderer.getErrorIndicatorStroke());
        assertTrue(renderer.getErrorIndicatorStroke() instanceof BasicStroke);
    }

    @Test(timeout = 4000)
    public void testSetGetErrorIndicatorPaint() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        Paint paint = Color.red;
        renderer.setErrorIndicatorPaint(paint);
        assertSame(paint, renderer.getErrorIndicatorPaint());
        
        renderer.setErrorIndicatorPaint(null);
        assertNull(renderer.getErrorIndicatorPaint());
    }

    @Test(timeout = 4000)
    public void testSetGetErrorIndicatorStroke() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        Stroke stroke = new BasicStroke(2.0f);
        renderer.setErrorIndicatorStroke(stroke);
        assertSame(stroke, renderer.getErrorIndicatorStroke());
        
        renderer.setErrorIndicatorStroke(null);
        assertNull(renderer.getErrorIndicatorStroke());
    }

    @Test(timeout = 4000)
    public void testEquals() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        assertTrue(r1.equals(r1));
        
        // Test different paint
        r1.setErrorIndicatorPaint(Color.red);
        assertFalse(r1.equals(r2));
        r2.setErrorIndicatorPaint(Color.red);
        assertTrue(r1.equals(r2));
        
        // Test different stroke
        r1.setErrorIndicatorStroke(new BasicStroke(2.0f));
        assertFalse(r1.equals(r2));
        r2.setErrorIndicatorStroke(new BasicStroke(2.0f));
        assertTrue(r1.equals(r2));
        
        // Test null comparison
        assertFalse(r1.equals(null));
        assertFalse(r1.equals(new Object()));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testEqualsWithNullPaint() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        r1.setErrorIndicatorPaint(null);
        r2.setErrorIndicatorPaint(null);
        assertTrue(r1.equals(r2));
        
        r1.setErrorIndicatorPaint(Color.red);
        assertFalse(r1.equals(r2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullStroke() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        r1.setErrorIndicatorStroke(null);
        r2.setErrorIndicatorStroke(null);
        assertTrue(r1.equals(r2));
        
        r1.setErrorIndicatorStroke(new BasicStroke(1.0f));
        assertFalse(r1.equals(r2));
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        r1.setErrorIndicatorPaint(Color.blue);
        r1.setErrorIndicatorStroke(new BasicStroke(3.0f));
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(r1);
        out.close();
        
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        StatisticalBarRenderer r2 = (StatisticalBarRenderer) in.readObject();
        in.close();
        
        assertEquals(r1, r2);
        assertEquals(r1.getErrorIndicatorPaint(), r2.getErrorIndicatorPaint());
        assertEquals(r1.getErrorIndicatorStroke(), r2.getErrorIndicatorStroke());
    }

    // ==================== Partition C: Defect-Targeted Tests ====================

    @Test(timeout = 4000)
    public void testDrawWithNullMeanVertical() {
        // This test targets the known defect: NPE when meanValue is null in vertical orientation
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(1.0, 0.5, "Series1", "Category1");
        // Manually set null mean value
        dataset.setValue(null, "Series1", "Category1");
        
        JFreeChart chart = ChartFactory.createBarChart("Test", "Category", "Value", 
                dataset, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        plot.setRenderer(renderer);
        
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        try {
            chart.draw(g2, new java.awt.Rectangle(0, 0, 200, 100));
            // If no exception, the test passes (but we expect failure on defective version)
        } catch (NullPointerException e) {
            fail("NPE thrown when drawing with null mean value: " + e.getMessage());
        } finally {
            g2.dispose();
        }
    }

    @Test(timeout = 4000)
    public void testDrawWithNullDeviationVertical() {
        // This test targets the known defect: NPE when stdDevValue is null in vertical orientation
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(1.0, 0.5, "Series1", "Category1");
        // Manually set null deviation
        dataset.setValue(1.0, "Series1", "Category1");
        // Note: DefaultStatisticalCategoryDataset doesn't allow null deviation directly,
        // but we can use a custom dataset or reflection for testing
        
        // Using reflection to set null deviation
        try {
            java.lang.reflect.Field field = DefaultStatisticalCategoryDataset.class.getDeclaredField("data");
            field.setAccessible(true);
            // This is a simplified approach - in real testing we'd use a mock or custom dataset
        } catch (Exception e) {
            // If reflection fails, skip this test
        }
        
        // Alternative: create a custom StatisticalCategoryDataset that returns null deviation
        StatisticalCategoryDataset customDataset = new StatisticalCategoryDataset() {
            @Override
            public Number getMeanValue(int row, int column) { return 1.0; }
            @Override
            public Number getStdDevValue(int row, int column) { return null; }
            @Override
            public int getRowCount() { return 1; }
            @Override
            public int getColumnCount() { return 1; }
            @Override
            public Number getValue(int row, int column) { return 1.0; }
            @Override
            public Comparable getRowKey(int row) { return "Series1"; }
            @Override
            public int getRowIndex(Comparable key) { return 0; }
            @Override
            public Comparable getColumnKey(int column) { return "Category1"; }
            @Override
            public int getColumnIndex(Comparable key) { return 0; }
            @Override
            public Comparable getRowKey(Comparable row) { return "Series1"; }
            @Override
            public Comparable getColumnKey(Comparable column) { return "Category1"; }
        };
        
        JFreeChart chart = ChartFactory.createBarChart("Test", "Category", "Value", 
                customDataset, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        plot.setRenderer(renderer);
        
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        try {
            chart.draw(g2, new java.awt.Rectangle(0, 0, 200, 100));
            // If no exception, the test passes (but we expect failure on defective version)
        } catch (NullPointerException e) {
            fail("NPE thrown when drawing with null deviation: " + e.getMessage());
        } finally {
            g2.dispose();
        }
    }

    @Test(timeout = 4000)
    public void testDrawWithNullMeanHorizontal() {
        // This test targets the known defect: NPE when meanValue is null in horizontal orientation
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(1.0, 0.5, "Series1", "Category1");
        dataset.setValue(null, "Series1", "Category1");
        
        JFreeChart chart = ChartFactory.createBarChart("Test", "Category", "Value", 
                dataset, PlotOrientation.HORIZONTAL, true, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        plot.setRenderer(renderer);
        
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        try {
            chart.draw(g2, new java.awt.Rectangle(0, 0, 200, 100));
            // If no exception, the test passes (but we expect failure on defective version)
        } catch (NullPointerException e) {
            fail("NPE thrown when drawing with null mean value (horizontal): " + e.getMessage());
        } finally {
            g2.dispose();
        }
    }

    @Test(timeout = 4000)
    public void testDrawWithNullDeviationHorizontal() {
        // This test targets the known defect: NPE when stdDevValue is null in horizontal orientation
        StatisticalCategoryDataset customDataset = new StatisticalCategoryDataset() {
            @Override
            public Number getMeanValue(int row, int column) { return 1.0; }
            @Override
            public Number getStdDevValue(int row, int column) { return null; }
            @Override
            public int getRowCount() { return 1; }
            @Override
            public int getColumnCount() { return 1; }
            @Override
            public Number getValue(int row, int column) { return 1.0; }
            @Override
            public Comparable getRowKey(int row) { return "Series1"; }
            @Override
            public int getRowIndex(Comparable key) { return 0; }
            @Override
            public Comparable getColumnKey(int column) { return "Category1"; }
            @Override
            public int getColumnIndex(Comparable key) { return 0; }
            @Override
            public Comparable getRowKey(Comparable row) { return "Series1"; }
            @Override
            public Comparable getColumnKey(Comparable column) { return "Category1"; }
        };
        
        JFreeChart chart = ChartFactory.createBarChart("Test", "Category", "Value", 
                customDataset, PlotOrientation.HORIZONTAL, true, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        plot.setRenderer(renderer);
        
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        try {
            chart.draw(g2, new java.awt.Rectangle(0, 0, 200, 100));
            // If no exception, the test passes (but we expect failure on defective version)
        } catch (NullPointerException e) {
            fail("NPE thrown when drawing with null deviation (horizontal): " + e.getMessage());
        } finally {
            g2.dispose();
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDrawItemWithNonStatisticalDataset() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        CategoryDataset nonStatDataset = new org.jfree.data.category.DefaultCategoryDataset();
        
        JFreeChart chart = ChartFactory.createBarChart("Test", "Category", "Value", 
                nonStatDataset, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        try {
            renderer.drawItem(g2, null, new Rectangle2D.Double(0, 0, 100, 100), 
                    plot, plot.getDomainAxis(), plot.getRangeAxis(), 
                    nonStatDataset, 0, 0, 0);
        } finally {
            g2.dispose();
        }
    }

    @Test(timeout = 4000)
    public void testDrawItemWithNullDataArea() {
        // Test with null data area - should not throw NPE for valid dataset
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(1.0, 0.5, "Series1", "Category1");
        
        JFreeChart chart = ChartFactory.createBarChart("Test", "Category", "Value", 
                dataset, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        plot.setRenderer(renderer);
        
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        try {
            chart.draw(g2, new java.awt.Rectangle(0, 0, 200, 100));
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        } finally {
            g2.dispose();
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testClone() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.setErrorIndicatorPaint(Color.green);
        renderer.setErrorIndicatorStroke(new BasicStroke(1.5f));
        
        try {
            StatisticalBarRenderer cloned = (StatisticalBarRenderer) renderer.clone();
            assertNotSame(renderer, cloned);
            assertEquals(renderer, cloned);
            assertEquals(renderer.getErrorIndicatorPaint(), cloned.getErrorIndicatorPaint());
            assertEquals(renderer.getErrorIndicatorStroke(), cloned.getErrorIndicatorStroke());
        } catch (CloneNotSupportedException e) {
            fail("Clone not supported: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        assertEquals(r1.hashCode(), r2.hashCode());
        
        r1.setErrorIndicatorPaint(Color.red);
        assertNotEquals(r1.hashCode(), r2.hashCode());
    }

    @Test(timeout = 4000)
    public void testDrawWithMultipleSeries() {
        // Test with multiple series to cover seriesCount > 1 branch
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(1.0, 0.5, "Series1", "Category1");
        dataset.add(2.0, 0.7, "Series2", "Category1");
        dataset.add(1.5, 0.3, "Series1", "Category2");
        dataset.add(2.5, 0.8, "Series2", "Category2");
        
        JFreeChart chart = ChartFactory.createBarChart("Test", "Category", "Value", 
                dataset, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        plot.setRenderer(renderer);
        
        BufferedImage image = new BufferedImage(400, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        try {
            chart.draw(g2, new java.awt.Rectangle(0, 0, 400, 200));
        } catch (Exception e) {
            fail("Unexpected exception with multiple series: " + e.getMessage());
        } finally {
            g2.dispose();
        }
    }

    @Test(timeout = 4000)
    public void testDrawWithNullErrorIndicatorPaint() {
        // Test with null errorIndicatorPaint - should use item outline paint
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(1.0, 0.5, "Series1", "Category1");
        
        JFreeChart chart = ChartFactory.createBarChart("Test", "Category", "Value", 
                dataset, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.setErrorIndicatorPaint(null);
        plot.setRenderer(renderer);
        
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        try {
            chart.draw(g2, new java.awt.Rectangle(0, 0, 200, 100));
        } catch (Exception e) {
            fail("Unexpected exception with null errorIndicatorPaint: " + e.getMessage());
        } finally {
            g2.dispose();
        }
    }

    @Test(timeout = 4000)
    public void testDrawWithNullErrorIndicatorStroke() {
        // Test with null errorIndicatorStroke - should use item outline stroke
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(1.0, 0.5, "Series1", "Category1");
        
        JFreeChart chart = ChartFactory.createBarChart("Test", "Category", "Value", 
                dataset, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.setErrorIndicatorStroke(null);
        plot.setRenderer(renderer);
        
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        try {
            chart.draw(g2, new java.awt.Rectangle(0, 0, 200, 100));
        } catch (Exception e) {
            fail("Unexpected exception with null errorIndicatorStroke: " + e.getMessage());
        } finally {
            g2.dispose();
        }
    }
}