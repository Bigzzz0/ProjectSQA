package org.apache.commons.math3.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for FastMath.
 */
public class FastMath_IPOTest {
    @Test(timeout = 4000)
    public void test_pow_pairwise_001() throws Exception {
        // Combination: x=0.0d, y=0.0d
        Object actual = FastMath.pow(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_002() throws Exception {
        // Combination: x=0.0d, y=1.0d
        Object actual = FastMath.pow(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_003() throws Exception {
        // Combination: x=0.0d, y=-1.0d
        Object actual = FastMath.pow(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_004() throws Exception {
        // Combination: x=0.0d, y=Double.NaN
        Object actual = FastMath.pow(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_005() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_006() throws Exception {
        // Combination: x=1.0d, y=0.0d
        Object actual = FastMath.pow(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_007() throws Exception {
        // Combination: x=1.0d, y=1.0d
        Object actual = FastMath.pow(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_008() throws Exception {
        // Combination: x=1.0d, y=-1.0d
        Object actual = FastMath.pow(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_009() throws Exception {
        // Combination: x=1.0d, y=Double.NaN
        Object actual = FastMath.pow(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_010() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_011() throws Exception {
        // Combination: x=-1.0d, y=0.0d
        Object actual = FastMath.pow(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_012() throws Exception {
        // Combination: x=-1.0d, y=1.0d
        Object actual = FastMath.pow(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_013() throws Exception {
        // Combination: x=-1.0d, y=-1.0d
        Object actual = FastMath.pow(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_014() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN
        Object actual = FastMath.pow(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_015() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_016() throws Exception {
        // Combination: x=Double.NaN, y=0.0d
        Object actual = FastMath.pow(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_017() throws Exception {
        // Combination: x=Double.NaN, y=1.0d
        Object actual = FastMath.pow(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_018() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d
        Object actual = FastMath.pow(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_019() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN
        Object actual = FastMath.pow(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_020() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_021() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_022() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_023() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_024() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_025() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

}
