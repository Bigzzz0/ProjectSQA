package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for MathUtils.
 */
public class MathUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_gcd_pairwise_001() throws Exception {
        // Combination: u=0, v=0
        Object actual = MathUtils.gcd(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_002() throws Exception {
        // Combination: u=0, v=1
        Object actual = MathUtils.gcd(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_003() throws Exception {
        // Combination: u=0, v=-1
        Object actual = MathUtils.gcd(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_004() throws Exception {
        // Combination: u=0, v=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_005() throws Exception {
        // Combination: u=0, v=Integer.MIN_VALUE
        Object actual = MathUtils.gcd(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_006() throws Exception {
        // Combination: u=1, v=0
        Object actual = MathUtils.gcd(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_007() throws Exception {
        // Combination: u=1, v=1
        Object actual = MathUtils.gcd(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_008() throws Exception {
        // Combination: u=1, v=-1
        Object actual = MathUtils.gcd(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_009() throws Exception {
        // Combination: u=1, v=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_010() throws Exception {
        // Combination: u=1, v=Integer.MIN_VALUE
        Object actual = MathUtils.gcd(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_011() throws Exception {
        // Combination: u=-1, v=0
        Object actual = MathUtils.gcd(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_012() throws Exception {
        // Combination: u=-1, v=1
        Object actual = MathUtils.gcd(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_013() throws Exception {
        // Combination: u=-1, v=-1
        Object actual = MathUtils.gcd(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_014() throws Exception {
        // Combination: u=-1, v=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_015() throws Exception {
        // Combination: u=-1, v=Integer.MIN_VALUE
        Object actual = MathUtils.gcd(-1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_016() throws Exception {
        // Combination: u=Integer.MAX_VALUE, v=0
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_017() throws Exception {
        // Combination: u=Integer.MAX_VALUE, v=1
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_018() throws Exception {
        // Combination: u=Integer.MAX_VALUE, v=-1
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_019() throws Exception {
        // Combination: u=Integer.MAX_VALUE, v=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_020() throws Exception {
        // Combination: u=Integer.MAX_VALUE, v=Integer.MIN_VALUE
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_021() throws Exception {
        // Combination: u=Integer.MIN_VALUE, v=0
        Object actual = MathUtils.gcd(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_022() throws Exception {
        // Combination: u=Integer.MIN_VALUE, v=1
        Object actual = MathUtils.gcd(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_023() throws Exception {
        // Combination: u=Integer.MIN_VALUE, v=-1
        Object actual = MathUtils.gcd(Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_024() throws Exception {
        // Combination: u=Integer.MIN_VALUE, v=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_025() throws Exception {
        // Combination: u=Integer.MIN_VALUE, v=Integer.MIN_VALUE
        try {
            MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
