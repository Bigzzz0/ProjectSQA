package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for NumberUtils.
 */
public class NumberUtils_min__int_int_int_IPOTest {
    @Test(timeout = 4000)
    public void test_min_pairwise_001() throws Exception {
        // Combination: a=0, b=0, c=0
        Object actual = NumberUtils.min(0, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_002() throws Exception {
        // Combination: a=0, b=1, c=1
        Object actual = NumberUtils.min(0, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_003() throws Exception {
        // Combination: a=0, b=-1, c=-1
        Object actual = NumberUtils.min(0, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_004() throws Exception {
        // Combination: a=0, b=Integer.MAX_VALUE, c=Integer.MAX_VALUE
        Object actual = NumberUtils.min(0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_005() throws Exception {
        // Combination: a=0, b=Integer.MIN_VALUE, c=Integer.MIN_VALUE
        Object actual = NumberUtils.min(0, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_006() throws Exception {
        // Combination: a=1, b=0, c=1
        Object actual = NumberUtils.min(1, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_007() throws Exception {
        // Combination: a=1, b=1, c=0
        Object actual = NumberUtils.min(1, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_008() throws Exception {
        // Combination: a=1, b=-1, c=Integer.MAX_VALUE
        Object actual = NumberUtils.min(1, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_009() throws Exception {
        // Combination: a=1, b=Integer.MAX_VALUE, c=-1
        Object actual = NumberUtils.min(1, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_010() throws Exception {
        // Combination: a=1, b=Integer.MIN_VALUE, c=0
        Object actual = NumberUtils.min(1, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_011() throws Exception {
        // Combination: a=-1, b=0, c=-1
        Object actual = NumberUtils.min(-1, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_012() throws Exception {
        // Combination: a=-1, b=1, c=Integer.MAX_VALUE
        Object actual = NumberUtils.min(-1, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_013() throws Exception {
        // Combination: a=-1, b=-1, c=0
        Object actual = NumberUtils.min(-1, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_014() throws Exception {
        // Combination: a=-1, b=Integer.MAX_VALUE, c=1
        Object actual = NumberUtils.min(-1, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_015() throws Exception {
        // Combination: a=-1, b=Integer.MIN_VALUE, c=1
        Object actual = NumberUtils.min(-1, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_016() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=0, c=Integer.MAX_VALUE
        Object actual = NumberUtils.min(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_017() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=1, c=-1
        Object actual = NumberUtils.min(Integer.MAX_VALUE, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_018() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=-1, c=1
        Object actual = NumberUtils.min(Integer.MAX_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_019() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE, c=0
        Object actual = NumberUtils.min(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_020() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MIN_VALUE, c=-1
        Object actual = NumberUtils.min(Integer.MAX_VALUE, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_021() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=0, c=Integer.MIN_VALUE
        Object actual = NumberUtils.min(Integer.MIN_VALUE, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_022() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=1, c=0
        Object actual = NumberUtils.min(Integer.MIN_VALUE, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_023() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=-1, c=1
        Object actual = NumberUtils.min(Integer.MIN_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_024() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MAX_VALUE, c=-1
        Object actual = NumberUtils.min(Integer.MIN_VALUE, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_025() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MIN_VALUE, c=Integer.MAX_VALUE
        Object actual = NumberUtils.min(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_026() throws Exception {
        // Combination: a=1, b=1, c=Integer.MIN_VALUE
        Object actual = NumberUtils.min(1, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_027() throws Exception {
        // Combination: a=-1, b=-1, c=Integer.MIN_VALUE
        Object actual = NumberUtils.min(-1, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_028() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE, c=Integer.MIN_VALUE
        Object actual = NumberUtils.min(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

}
