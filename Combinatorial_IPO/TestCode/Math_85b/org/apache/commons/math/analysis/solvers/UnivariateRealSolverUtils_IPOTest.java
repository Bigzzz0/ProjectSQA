package org.apache.commons.math.analysis.solvers;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for UnivariateRealSolverUtils.
 */
public class UnivariateRealSolverUtils_IPOTest {
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
    public void test_midpoint_pairwise_001() throws Exception {
        // Combination: a=0.0d, b=0.0d
        Object actual = UnivariateRealSolverUtils.midpoint(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_002() throws Exception {
        // Combination: a=0.0d, b=1.0d
        Object actual = UnivariateRealSolverUtils.midpoint(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_003() throws Exception {
        // Combination: a=0.0d, b=-1.0d
        Object actual = UnivariateRealSolverUtils.midpoint(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_004() throws Exception {
        // Combination: a=0.0d, b=Double.NaN
        Object actual = UnivariateRealSolverUtils.midpoint(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_005() throws Exception {
        // Combination: a=0.0d, b=Double.POSITIVE_INFINITY
        Object actual = UnivariateRealSolverUtils.midpoint(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_006() throws Exception {
        // Combination: a=1.0d, b=0.0d
        Object actual = UnivariateRealSolverUtils.midpoint(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_007() throws Exception {
        // Combination: a=1.0d, b=1.0d
        Object actual = UnivariateRealSolverUtils.midpoint(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_008() throws Exception {
        // Combination: a=1.0d, b=-1.0d
        Object actual = UnivariateRealSolverUtils.midpoint(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_009() throws Exception {
        // Combination: a=1.0d, b=Double.NaN
        Object actual = UnivariateRealSolverUtils.midpoint(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_010() throws Exception {
        // Combination: a=1.0d, b=Double.POSITIVE_INFINITY
        Object actual = UnivariateRealSolverUtils.midpoint(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_011() throws Exception {
        // Combination: a=-1.0d, b=0.0d
        Object actual = UnivariateRealSolverUtils.midpoint(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_012() throws Exception {
        // Combination: a=-1.0d, b=1.0d
        Object actual = UnivariateRealSolverUtils.midpoint(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_013() throws Exception {
        // Combination: a=-1.0d, b=-1.0d
        Object actual = UnivariateRealSolverUtils.midpoint(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_014() throws Exception {
        // Combination: a=-1.0d, b=Double.NaN
        Object actual = UnivariateRealSolverUtils.midpoint(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_015() throws Exception {
        // Combination: a=-1.0d, b=Double.POSITIVE_INFINITY
        Object actual = UnivariateRealSolverUtils.midpoint(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_016() throws Exception {
        // Combination: a=Double.NaN, b=0.0d
        Object actual = UnivariateRealSolverUtils.midpoint(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_017() throws Exception {
        // Combination: a=Double.NaN, b=1.0d
        Object actual = UnivariateRealSolverUtils.midpoint(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_018() throws Exception {
        // Combination: a=Double.NaN, b=-1.0d
        Object actual = UnivariateRealSolverUtils.midpoint(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_019() throws Exception {
        // Combination: a=Double.NaN, b=Double.NaN
        Object actual = UnivariateRealSolverUtils.midpoint(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_020() throws Exception {
        // Combination: a=Double.NaN, b=Double.POSITIVE_INFINITY
        Object actual = UnivariateRealSolverUtils.midpoint(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_021() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=0.0d
        Object actual = UnivariateRealSolverUtils.midpoint(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_022() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=1.0d
        Object actual = UnivariateRealSolverUtils.midpoint(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_023() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=-1.0d
        Object actual = UnivariateRealSolverUtils.midpoint(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_024() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.NaN
        Object actual = UnivariateRealSolverUtils.midpoint(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_midpoint_pairwise_025() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.POSITIVE_INFINITY
        Object actual = UnivariateRealSolverUtils.midpoint(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

}
