package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for MathUtils.
 */
public class MathUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_log_pairwise_001() throws Exception {
        // Combination: base=0.0d, x=0.0d
        Object actual = MathUtils.log(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_002() throws Exception {
        // Combination: base=0.0d, x=1.0d
        Object actual = MathUtils.log(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_003() throws Exception {
        // Combination: base=0.0d, x=-1.0d
        Object actual = MathUtils.log(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_004() throws Exception {
        // Combination: base=0.0d, x=Double.NaN
        Object actual = MathUtils.log(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_005() throws Exception {
        // Combination: base=0.0d, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_006() throws Exception {
        // Combination: base=1.0d, x=0.0d
        Object actual = MathUtils.log(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_007() throws Exception {
        // Combination: base=1.0d, x=1.0d
        Object actual = MathUtils.log(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_008() throws Exception {
        // Combination: base=1.0d, x=-1.0d
        Object actual = MathUtils.log(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_009() throws Exception {
        // Combination: base=1.0d, x=Double.NaN
        Object actual = MathUtils.log(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_010() throws Exception {
        // Combination: base=1.0d, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_011() throws Exception {
        // Combination: base=-1.0d, x=0.0d
        Object actual = MathUtils.log(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_012() throws Exception {
        // Combination: base=-1.0d, x=1.0d
        Object actual = MathUtils.log(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_013() throws Exception {
        // Combination: base=-1.0d, x=-1.0d
        Object actual = MathUtils.log(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_014() throws Exception {
        // Combination: base=-1.0d, x=Double.NaN
        Object actual = MathUtils.log(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_015() throws Exception {
        // Combination: base=-1.0d, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_016() throws Exception {
        // Combination: base=Double.NaN, x=0.0d
        Object actual = MathUtils.log(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_017() throws Exception {
        // Combination: base=Double.NaN, x=1.0d
        Object actual = MathUtils.log(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_018() throws Exception {
        // Combination: base=Double.NaN, x=-1.0d
        Object actual = MathUtils.log(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_019() throws Exception {
        // Combination: base=Double.NaN, x=Double.NaN
        Object actual = MathUtils.log(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_020() throws Exception {
        // Combination: base=Double.NaN, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_021() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=0.0d
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_022() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=1.0d
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_023() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=-1.0d
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_024() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=Double.NaN
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_025() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

}
