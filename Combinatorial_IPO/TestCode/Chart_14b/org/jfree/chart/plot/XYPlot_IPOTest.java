package org.jfree.chart.plot;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for XYPlot.
 */
public class XYPlot_IPOTest {
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
    public void test_removeDomainMarker_pairwise_001() throws Exception {
        // Combination: marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeDomainMarker(new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_002() throws Exception {
        // Combination: marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeDomainMarker(new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_003() throws Exception {
        // Combination: marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeDomainMarker(new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_004() throws Exception {
        // Combination: marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeDomainMarker(new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_005() throws Exception {
        // Combination: index=0, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeDomainMarker(0, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_006() throws Exception {
        // Combination: index=0, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeDomainMarker(0, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_007() throws Exception {
        // Combination: index=1, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeDomainMarker(1, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_008() throws Exception {
        // Combination: index=1, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeDomainMarker(1, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_009() throws Exception {
        // Combination: index=-1, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeDomainMarker(-1, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_010() throws Exception {
        // Combination: index=-1, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeDomainMarker(-1, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_011() throws Exception {
        // Combination: index=Integer.MAX_VALUE, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeDomainMarker(Integer.MAX_VALUE, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_012() throws Exception {
        // Combination: index=Integer.MAX_VALUE, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeDomainMarker(Integer.MAX_VALUE, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_013() throws Exception {
        // Combination: index=Integer.MIN_VALUE, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeDomainMarker(Integer.MIN_VALUE, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_014() throws Exception {
        // Combination: index=Integer.MIN_VALUE, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeDomainMarker(Integer.MIN_VALUE, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_015() throws Exception {
        // Combination: index=0, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND, notify=true
        Object actual = (new XYPlot()).removeDomainMarker(0, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_016() throws Exception {
        // Combination: index=0, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND, notify=false
        Object actual = (new XYPlot()).removeDomainMarker(0, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_017() throws Exception {
        // Combination: index=1, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.FOREGROUND, notify=true
        Object actual = (new XYPlot()).removeDomainMarker(1, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.FOREGROUND, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_018() throws Exception {
        // Combination: index=1, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.BACKGROUND, notify=false
        Object actual = (new XYPlot()).removeDomainMarker(1, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.BACKGROUND, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_019() throws Exception {
        // Combination: index=-1, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND, notify=false
        Object actual = (new XYPlot()).removeDomainMarker(-1, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_020() throws Exception {
        // Combination: index=-1, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND, notify=true
        Object actual = (new XYPlot()).removeDomainMarker(-1, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_021() throws Exception {
        // Combination: index=Integer.MAX_VALUE, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND, notify=true
        Object actual = (new XYPlot()).removeDomainMarker(Integer.MAX_VALUE, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_022() throws Exception {
        // Combination: index=Integer.MAX_VALUE, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND, notify=false
        Object actual = (new XYPlot()).removeDomainMarker(Integer.MAX_VALUE, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_023() throws Exception {
        // Combination: index=Integer.MIN_VALUE, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND, notify=true
        Object actual = (new XYPlot()).removeDomainMarker(Integer.MIN_VALUE, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeDomainMarker_pairwise_024() throws Exception {
        // Combination: index=Integer.MIN_VALUE, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND, notify=false
        Object actual = (new XYPlot()).removeDomainMarker(Integer.MIN_VALUE, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_025() throws Exception {
        // Combination: marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeRangeMarker(new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_026() throws Exception {
        // Combination: marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeRangeMarker(new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_027() throws Exception {
        // Combination: marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeRangeMarker(new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_028() throws Exception {
        // Combination: marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeRangeMarker(new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_029() throws Exception {
        // Combination: index=0, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeRangeMarker(0, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_030() throws Exception {
        // Combination: index=0, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeRangeMarker(0, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_031() throws Exception {
        // Combination: index=1, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeRangeMarker(1, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_032() throws Exception {
        // Combination: index=1, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeRangeMarker(1, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_033() throws Exception {
        // Combination: index=-1, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeRangeMarker(-1, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_034() throws Exception {
        // Combination: index=-1, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeRangeMarker(-1, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_035() throws Exception {
        // Combination: index=Integer.MAX_VALUE, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeRangeMarker(Integer.MAX_VALUE, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_036() throws Exception {
        // Combination: index=Integer.MAX_VALUE, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeRangeMarker(Integer.MAX_VALUE, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_037() throws Exception {
        // Combination: index=Integer.MIN_VALUE, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND
        Object actual = (new XYPlot()).removeRangeMarker(Integer.MIN_VALUE, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_038() throws Exception {
        // Combination: index=Integer.MIN_VALUE, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND
        Object actual = (new XYPlot()).removeRangeMarker(Integer.MIN_VALUE, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_039() throws Exception {
        // Combination: index=0, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND, notify=true
        Object actual = (new XYPlot()).removeRangeMarker(0, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_040() throws Exception {
        // Combination: index=0, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND, notify=false
        Object actual = (new XYPlot()).removeRangeMarker(0, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_041() throws Exception {
        // Combination: index=1, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.FOREGROUND, notify=true
        Object actual = (new XYPlot()).removeRangeMarker(1, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.FOREGROUND, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_042() throws Exception {
        // Combination: index=1, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.BACKGROUND, notify=false
        Object actual = (new XYPlot()).removeRangeMarker(1, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.BACKGROUND, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_043() throws Exception {
        // Combination: index=-1, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND, notify=false
        Object actual = (new XYPlot()).removeRangeMarker(-1, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_044() throws Exception {
        // Combination: index=-1, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND, notify=true
        Object actual = (new XYPlot()).removeRangeMarker(-1, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_045() throws Exception {
        // Combination: index=Integer.MAX_VALUE, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND, notify=true
        Object actual = (new XYPlot()).removeRangeMarker(Integer.MAX_VALUE, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_046() throws Exception {
        // Combination: index=Integer.MAX_VALUE, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND, notify=false
        Object actual = (new XYPlot()).removeRangeMarker(Integer.MAX_VALUE, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_047() throws Exception {
        // Combination: index=Integer.MIN_VALUE, marker=new org.jfree.chart.plot.ValueMarker(0.0), layer=org.jfree.chart.util.Layer.FOREGROUND, notify=true
        Object actual = (new XYPlot()).removeRangeMarker(Integer.MIN_VALUE, new org.jfree.chart.plot.ValueMarker(0.0), org.jfree.chart.util.Layer.FOREGROUND, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeRangeMarker_pairwise_048() throws Exception {
        // Combination: index=Integer.MIN_VALUE, marker=new org.jfree.chart.plot.ValueMarker(1.0), layer=org.jfree.chart.util.Layer.BACKGROUND, notify=false
        Object actual = (new XYPlot()).removeRangeMarker(Integer.MIN_VALUE, new org.jfree.chart.plot.ValueMarker(1.0), org.jfree.chart.util.Layer.BACKGROUND, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainMarkers_pairwise_049() throws Exception {
        // Combination: index=0, layer=org.jfree.chart.util.Layer.FOREGROUND
        assertNull((new XYPlot()).getDomainMarkers(0, org.jfree.chart.util.Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void test_getDomainMarkers_pairwise_050() throws Exception {
        // Combination: index=0, layer=org.jfree.chart.util.Layer.BACKGROUND
        assertNull((new XYPlot()).getDomainMarkers(0, org.jfree.chart.util.Layer.BACKGROUND));
    }

    @Test(timeout = 4000)
    public void test_getDomainMarkers_pairwise_051() throws Exception {
        // Combination: index=1, layer=org.jfree.chart.util.Layer.FOREGROUND
        assertNull((new XYPlot()).getDomainMarkers(1, org.jfree.chart.util.Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void test_getDomainMarkers_pairwise_052() throws Exception {
        // Combination: index=1, layer=org.jfree.chart.util.Layer.BACKGROUND
        assertNull((new XYPlot()).getDomainMarkers(1, org.jfree.chart.util.Layer.BACKGROUND));
    }

    @Test(timeout = 4000)
    public void test_getDomainMarkers_pairwise_053() throws Exception {
        // Combination: index=-1, layer=org.jfree.chart.util.Layer.FOREGROUND
        assertNull((new XYPlot()).getDomainMarkers(-1, org.jfree.chart.util.Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void test_getDomainMarkers_pairwise_054() throws Exception {
        // Combination: index=-1, layer=org.jfree.chart.util.Layer.BACKGROUND
        assertNull((new XYPlot()).getDomainMarkers(-1, org.jfree.chart.util.Layer.BACKGROUND));
    }

    @Test(timeout = 4000)
    public void test_getDomainMarkers_pairwise_055() throws Exception {
        // Combination: index=Integer.MAX_VALUE, layer=org.jfree.chart.util.Layer.FOREGROUND
        assertNull((new XYPlot()).getDomainMarkers(Integer.MAX_VALUE, org.jfree.chart.util.Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void test_getDomainMarkers_pairwise_056() throws Exception {
        // Combination: index=Integer.MAX_VALUE, layer=org.jfree.chart.util.Layer.BACKGROUND
        assertNull((new XYPlot()).getDomainMarkers(Integer.MAX_VALUE, org.jfree.chart.util.Layer.BACKGROUND));
    }

    @Test(timeout = 4000)
    public void test_getDomainMarkers_pairwise_057() throws Exception {
        // Combination: index=Integer.MIN_VALUE, layer=org.jfree.chart.util.Layer.FOREGROUND
        assertNull((new XYPlot()).getDomainMarkers(Integer.MIN_VALUE, org.jfree.chart.util.Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void test_getDomainMarkers_pairwise_058() throws Exception {
        // Combination: index=Integer.MIN_VALUE, layer=org.jfree.chart.util.Layer.BACKGROUND
        assertNull((new XYPlot()).getDomainMarkers(Integer.MIN_VALUE, org.jfree.chart.util.Layer.BACKGROUND));
    }

    @Test(timeout = 4000)
    public void test_getRangeMarkers_pairwise_059() throws Exception {
        // Combination: index=0, layer=org.jfree.chart.util.Layer.FOREGROUND
        assertNull((new XYPlot()).getRangeMarkers(0, org.jfree.chart.util.Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void test_getRangeMarkers_pairwise_060() throws Exception {
        // Combination: index=0, layer=org.jfree.chart.util.Layer.BACKGROUND
        assertNull((new XYPlot()).getRangeMarkers(0, org.jfree.chart.util.Layer.BACKGROUND));
    }

    @Test(timeout = 4000)
    public void test_getRangeMarkers_pairwise_061() throws Exception {
        // Combination: index=1, layer=org.jfree.chart.util.Layer.FOREGROUND
        assertNull((new XYPlot()).getRangeMarkers(1, org.jfree.chart.util.Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void test_getRangeMarkers_pairwise_062() throws Exception {
        // Combination: index=1, layer=org.jfree.chart.util.Layer.BACKGROUND
        assertNull((new XYPlot()).getRangeMarkers(1, org.jfree.chart.util.Layer.BACKGROUND));
    }

    @Test(timeout = 4000)
    public void test_getRangeMarkers_pairwise_063() throws Exception {
        // Combination: index=-1, layer=org.jfree.chart.util.Layer.FOREGROUND
        assertNull((new XYPlot()).getRangeMarkers(-1, org.jfree.chart.util.Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void test_getRangeMarkers_pairwise_064() throws Exception {
        // Combination: index=-1, layer=org.jfree.chart.util.Layer.BACKGROUND
        assertNull((new XYPlot()).getRangeMarkers(-1, org.jfree.chart.util.Layer.BACKGROUND));
    }

    @Test(timeout = 4000)
    public void test_getRangeMarkers_pairwise_065() throws Exception {
        // Combination: index=Integer.MAX_VALUE, layer=org.jfree.chart.util.Layer.FOREGROUND
        assertNull((new XYPlot()).getRangeMarkers(Integer.MAX_VALUE, org.jfree.chart.util.Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void test_getRangeMarkers_pairwise_066() throws Exception {
        // Combination: index=Integer.MAX_VALUE, layer=org.jfree.chart.util.Layer.BACKGROUND
        assertNull((new XYPlot()).getRangeMarkers(Integer.MAX_VALUE, org.jfree.chart.util.Layer.BACKGROUND));
    }

    @Test(timeout = 4000)
    public void test_getRangeMarkers_pairwise_067() throws Exception {
        // Combination: index=Integer.MIN_VALUE, layer=org.jfree.chart.util.Layer.FOREGROUND
        assertNull((new XYPlot()).getRangeMarkers(Integer.MIN_VALUE, org.jfree.chart.util.Layer.FOREGROUND));
    }

    @Test(timeout = 4000)
    public void test_getRangeMarkers_pairwise_068() throws Exception {
        // Combination: index=Integer.MIN_VALUE, layer=org.jfree.chart.util.Layer.BACKGROUND
        assertNull((new XYPlot()).getRangeMarkers(Integer.MIN_VALUE, org.jfree.chart.util.Layer.BACKGROUND));
    }

}
