package org.jfree.chart.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ShapeUtilities.
 */
public class ShapeUtilities_IPOTest {
    private static String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Object[]) return java.util.Arrays.deepToString((Object[]) value);
        if (value instanceof byte[]) return java.util.Arrays.toString((byte[]) value);
        if (value instanceof short[]) return java.util.Arrays.toString((short[]) value);
        if (value instanceof int[]) return java.util.Arrays.toString((int[]) value);
        if (value instanceof long[]) return java.util.Arrays.toString((long[]) value);
        if (value instanceof char[]) return java.util.Arrays.toString((char[]) value);
        if (value instanceof float[]) return java.util.Arrays.toString((float[]) value);
        if (value instanceof double[]) return java.util.Arrays.toString((double[]) value);
        if (value instanceof boolean[]) return java.util.Arrays.toString((boolean[]) value);
        return String.valueOf(value);
    }

    @Test(timeout = 4000)
    public void test_equal_pairwise_001() throws Exception {
        // Combination: s1=new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0), s2=new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0)
        Object actual = ShapeUtilities.equal(new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0), new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equal_pairwise_002() throws Exception {
        // Combination: s1=new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0), s2=new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0)
        Object actual = ShapeUtilities.equal(new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0), new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equal_pairwise_003() throws Exception {
        // Combination: s1=new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0), s2=new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0)
        Object actual = ShapeUtilities.equal(new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0), new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equal_pairwise_004() throws Exception {
        // Combination: s1=new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0), s2=new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0)
        Object actual = ShapeUtilities.equal(new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0), new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_005() throws Exception {
        // Combination: rect1=new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0), rect2=new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0)
        Object actual = ShapeUtilities.contains(new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0), new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_006() throws Exception {
        // Combination: rect1=new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0), rect2=new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0)
        Object actual = ShapeUtilities.contains(new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0), new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_007() throws Exception {
        // Combination: rect1=new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0), rect2=new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0)
        Object actual = ShapeUtilities.contains(new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0), new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_008() throws Exception {
        // Combination: rect1=new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0), rect2=new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0)
        Object actual = ShapeUtilities.contains(new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0), new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_intersects_pairwise_009() throws Exception {
        // Combination: rect1=new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0), rect2=new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0)
        Object actual = ShapeUtilities.intersects(new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0), new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_intersects_pairwise_010() throws Exception {
        // Combination: rect1=new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0), rect2=new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0)
        Object actual = ShapeUtilities.intersects(new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0), new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_intersects_pairwise_011() throws Exception {
        // Combination: rect1=new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0), rect2=new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0)
        Object actual = ShapeUtilities.intersects(new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0), new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_intersects_pairwise_012() throws Exception {
        // Combination: rect1=new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0), rect2=new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0)
        Object actual = ShapeUtilities.intersects(new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0), new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

}
