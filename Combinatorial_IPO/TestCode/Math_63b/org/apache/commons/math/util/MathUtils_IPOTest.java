package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for MathUtils.
 */
public class MathUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_equals_pairwise_001() throws Exception {
        // Combination: x=0.0d, y=0.0d
        Object actual = MathUtils.equals(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_002() throws Exception {
        // Combination: x=0.0d, y=1.0d
        Object actual = MathUtils.equals(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_003() throws Exception {
        // Combination: x=0.0d, y=-1.0d
        Object actual = MathUtils.equals(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_004() throws Exception {
        // Combination: x=0.0d, y=Double.NaN
        Object actual = MathUtils.equals(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_005() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_006() throws Exception {
        // Combination: x=1.0d, y=0.0d
        Object actual = MathUtils.equals(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_007() throws Exception {
        // Combination: x=1.0d, y=1.0d
        Object actual = MathUtils.equals(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_008() throws Exception {
        // Combination: x=1.0d, y=-1.0d
        Object actual = MathUtils.equals(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_009() throws Exception {
        // Combination: x=1.0d, y=Double.NaN
        Object actual = MathUtils.equals(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_010() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_011() throws Exception {
        // Combination: x=-1.0d, y=0.0d
        Object actual = MathUtils.equals(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_012() throws Exception {
        // Combination: x=-1.0d, y=1.0d
        Object actual = MathUtils.equals(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_013() throws Exception {
        // Combination: x=-1.0d, y=-1.0d
        Object actual = MathUtils.equals(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_014() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN
        Object actual = MathUtils.equals(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_015() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_016() throws Exception {
        // Combination: x=Double.NaN, y=0.0d
        Object actual = MathUtils.equals(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_017() throws Exception {
        // Combination: x=Double.NaN, y=1.0d
        Object actual = MathUtils.equals(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_018() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d
        Object actual = MathUtils.equals(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_019() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN
        Object actual = MathUtils.equals(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_020() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_021() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_022() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_023() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_024() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_025() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

}
