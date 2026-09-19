package org.joda.time.field;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for FieldUtils.
 */
public class FieldUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_001() throws Exception {
        // Combination: val1=0L, val2=0
        Object actual = FieldUtils.safeMultiply(0L, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_002() throws Exception {
        // Combination: val1=0L, val2=1
        Object actual = FieldUtils.safeMultiply(0L, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_003() throws Exception {
        // Combination: val1=0L, val2=-1
        Object actual = FieldUtils.safeMultiply(0L, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_004() throws Exception {
        // Combination: val1=0L, val2=Integer.MAX_VALUE
        Object actual = FieldUtils.safeMultiply(0L, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_005() throws Exception {
        // Combination: val1=0L, val2=Integer.MIN_VALUE
        Object actual = FieldUtils.safeMultiply(0L, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_006() throws Exception {
        // Combination: val1=1L, val2=0
        Object actual = FieldUtils.safeMultiply(1L, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_007() throws Exception {
        // Combination: val1=1L, val2=1
        Object actual = FieldUtils.safeMultiply(1L, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_008() throws Exception {
        // Combination: val1=1L, val2=-1
        Object actual = FieldUtils.safeMultiply(1L, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_009() throws Exception {
        // Combination: val1=1L, val2=Integer.MAX_VALUE
        Object actual = FieldUtils.safeMultiply(1L, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_010() throws Exception {
        // Combination: val1=1L, val2=Integer.MIN_VALUE
        Object actual = FieldUtils.safeMultiply(1L, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_011() throws Exception {
        // Combination: val1=-1L, val2=0
        Object actual = FieldUtils.safeMultiply(-1L, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_012() throws Exception {
        // Combination: val1=-1L, val2=1
        Object actual = FieldUtils.safeMultiply(-1L, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_013() throws Exception {
        // Combination: val1=-1L, val2=-1
        Object actual = FieldUtils.safeMultiply(-1L, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_014() throws Exception {
        // Combination: val1=-1L, val2=Integer.MAX_VALUE
        Object actual = FieldUtils.safeMultiply(-1L, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_015() throws Exception {
        // Combination: val1=-1L, val2=Integer.MIN_VALUE
        Object actual = FieldUtils.safeMultiply(-1L, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_016() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=0
        Object actual = FieldUtils.safeMultiply(Long.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_017() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=1
        Object actual = FieldUtils.safeMultiply(Long.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_018() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=-1
        Object actual = FieldUtils.safeMultiply(Long.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_019() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=Integer.MAX_VALUE
        try {
            FieldUtils.safeMultiply(Long.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_020() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=Integer.MIN_VALUE
        try {
            FieldUtils.safeMultiply(Long.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_021() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=0
        Object actual = FieldUtils.safeMultiply(Long.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_022() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=1
        Object actual = FieldUtils.safeMultiply(Long.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_023() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=-1
        try {
            FieldUtils.safeMultiply(Long.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_024() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=Integer.MAX_VALUE
        try {
            FieldUtils.safeMultiply(Long.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_025() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=Integer.MIN_VALUE
        try {
            FieldUtils.safeMultiply(Long.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
