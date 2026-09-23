package org.apache.commons.math.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for BigFraction.
 */
public class BigFraction_IPOTest {
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
    public void test_getReducedFraction_pairwise_001() throws Exception {
        // Combination: numerator=0, denominator=0
        Object actual = BigFraction.getReducedFraction(0, 0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_002() throws Exception {
        // Combination: numerator=1, denominator=0
        try {
            BigFraction.getReducedFraction(1, 0);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_003() throws Exception {
        // Combination: numerator=-1, denominator=0
        try {
            BigFraction.getReducedFraction(-1, 0);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_004() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=0
        try {
            BigFraction.getReducedFraction(Integer.MAX_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_005() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=0
        try {
            BigFraction.getReducedFraction(Integer.MIN_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_006() throws Exception {
        // Combination: numerator=0, denominator=1
        Object actual = BigFraction.getReducedFraction(0, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_007() throws Exception {
        // Combination: numerator=1, denominator=1
        Object actual = BigFraction.getReducedFraction(1, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_008() throws Exception {
        // Combination: numerator=-1, denominator=1
        Object actual = BigFraction.getReducedFraction(-1, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_009() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=1
        Object actual = BigFraction.getReducedFraction(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_010() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=1
        Object actual = BigFraction.getReducedFraction(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_011() throws Exception {
        // Combination: numerator=0, denominator=-1
        Object actual = BigFraction.getReducedFraction(0, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_012() throws Exception {
        // Combination: numerator=1, denominator=-1
        Object actual = BigFraction.getReducedFraction(1, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_013() throws Exception {
        // Combination: numerator=-1, denominator=-1
        Object actual = BigFraction.getReducedFraction(-1, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_014() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=-1
        Object actual = BigFraction.getReducedFraction(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_015() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=-1
        Object actual = BigFraction.getReducedFraction(Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_016() throws Exception {
        // Combination: numerator=0, denominator=Integer.MAX_VALUE
        Object actual = BigFraction.getReducedFraction(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_017() throws Exception {
        // Combination: numerator=1, denominator=Integer.MAX_VALUE
        Object actual = BigFraction.getReducedFraction(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1 / 2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_018() throws Exception {
        // Combination: numerator=-1, denominator=Integer.MAX_VALUE
        Object actual = BigFraction.getReducedFraction(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1 / 2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_019() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=Integer.MAX_VALUE
        Object actual = BigFraction.getReducedFraction(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_020() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=Integer.MAX_VALUE
        Object actual = BigFraction.getReducedFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2147483648 / 2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_021() throws Exception {
        // Combination: numerator=0, denominator=Integer.MIN_VALUE
        Object actual = BigFraction.getReducedFraction(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_022() throws Exception {
        // Combination: numerator=1, denominator=Integer.MIN_VALUE
        Object actual = BigFraction.getReducedFraction(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1 / 2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_023() throws Exception {
        // Combination: numerator=-1, denominator=Integer.MIN_VALUE
        Object actual = BigFraction.getReducedFraction(-1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1 / 2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_024() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=Integer.MIN_VALUE
        Object actual = BigFraction.getReducedFraction(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2147483647 / 2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_025() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=Integer.MIN_VALUE
        Object actual = BigFraction.getReducedFraction(Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_026() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, bg=java.math.BigInteger.ZERO
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(java.math.BigInteger.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_027() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, bg=java.math.BigInteger.ZERO
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(java.math.BigInteger.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_028() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), bg=java.math.BigInteger.ZERO
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(java.math.BigInteger.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_029() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, bg=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_030() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, bg=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_031() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), bg=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_032() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, bg=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_033() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, bg=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_034() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), bg=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_035() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=0
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_036() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_037() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_038() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_039() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_040() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_041() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=-1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_042() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=-1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_043() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=-1
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_044() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_045() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_046() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2147483646", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_047() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=Integer.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_048() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=Integer.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_049() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=Integer.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2147483649", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_050() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=0L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(0L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_051() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=0L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(0L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_052() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=0L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(0L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_053() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=1L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_054() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_055() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_056() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_057() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_058() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_059() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_060() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_061() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("9223372036854775806", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_062() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=Long.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_063() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=Long.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_064() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=Long.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-9223372036854775809", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_065() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, fraction=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1 / 2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_066() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, fraction=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("3 / 2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_067() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), fraction=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1 / 2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_068() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, fraction=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).add(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_069() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, fraction=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).add(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_070() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), fraction=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).add(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_071() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, roundingMode=0
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(0);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_072() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, roundingMode=1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(1);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_073() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_074() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_075() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_076() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, roundingMode=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(0);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_077() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, roundingMode=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(1);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_078() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_079() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_080() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_081() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), roundingMode=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(0);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_082() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), roundingMode=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(1);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_083() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_084() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_085() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_086() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=0, roundingMode=0
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(0, 0);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_087() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=1, roundingMode=1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(1, 1);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_088() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=-1, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(-1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_089() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=Integer.MAX_VALUE, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_090() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=Integer.MIN_VALUE, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_091() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, scale=1, roundingMode=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(1, 0);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_092() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, scale=0, roundingMode=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(0, 1);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_093() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, scale=Integer.MAX_VALUE, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_094() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, scale=-1, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_095() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, scale=0, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_096() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), scale=-1, roundingMode=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(-1, 0);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("-1E+1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_097() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), scale=Integer.MAX_VALUE, roundingMode=1
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_098() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), scale=0, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_099() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), scale=1, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_100() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), scale=1, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_101() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=0, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_102() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=1, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_103() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=-1, roundingMode=1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(-1, 1);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("0E+1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_104() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=-1, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_105() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=Integer.MAX_VALUE, roundingMode=0
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_106() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=Integer.MAX_VALUE, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_107() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, scale=Integer.MIN_VALUE, roundingMode=0
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_108() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), scale=Integer.MIN_VALUE, roundingMode=1
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_109() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=Integer.MIN_VALUE, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_110() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=Integer.MIN_VALUE, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_111() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, object=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).compareTo(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_112() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, object=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).compareTo(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_113() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), object=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).compareTo(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_114() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, object=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).compareTo(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_115() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, object=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).compareTo(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_116() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), object=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).compareTo(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_117() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, bg=java.math.BigInteger.ZERO
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).divide(java.math.BigInteger.ZERO);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_118() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, bg=java.math.BigInteger.ZERO
        try {
            (new BigFraction(java.math.BigInteger.ONE)).divide(java.math.BigInteger.ZERO);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_119() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), bg=java.math.BigInteger.ZERO
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).divide(java.math.BigInteger.ZERO);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_120() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, bg=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).divide(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_121() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, bg=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).divide(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_122() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), bg=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).divide(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_123() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, bg=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).divide(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_124() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, bg=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).divide(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_125() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), bg=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).divide(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_126() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=0
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).divide(0);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_127() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=0
        try {
            (new BigFraction(java.math.BigInteger.ONE)).divide(0);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_128() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=0
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).divide(0);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_129() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).divide(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_130() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).divide(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_131() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).divide(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_132() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=-1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).divide(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_133() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=-1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).divide(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_134() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=-1
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).divide(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_135() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).divide(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_136() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).divide(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1 / 2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_137() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).divide(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1 / 2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_138() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=Integer.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).divide(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_139() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=Integer.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).divide(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1 / 2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_140() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=Integer.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).divide(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1 / 2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_141() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=0L
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).divide(0L);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_142() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=0L
        try {
            (new BigFraction(java.math.BigInteger.ONE)).divide(0L);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_143() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=0L
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).divide(0L);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_144() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=1L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).divide(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_145() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).divide(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_146() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).divide(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_147() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).divide(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_148() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).divide(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_149() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).divide(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_150() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).divide(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_151() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).divide(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1 / 9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_152() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).divide(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1 / 9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_153() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=Long.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).divide(Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_154() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=Long.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).divide(Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1 / 9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_155() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=Long.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).divide(Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1 / 9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_156() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, fraction=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).divide(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_157() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, fraction=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).divide(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_158() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), fraction=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).divide(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_159() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, fraction=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).divide(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_160() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, fraction=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).divide(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_161() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), fraction=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).divide(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_162() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, other=new Object()
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_163() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, other=new Object()
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_164() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), other=new Object()
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_165() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, other="sample_str"
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_166() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, other="sample_str"
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_167() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), other="sample_str"
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_168() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, other=Integer.valueOf(1)
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_169() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, other=Integer.valueOf(1)
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_170() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), other=Integer.valueOf(1)
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_171() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, bg=java.math.BigInteger.ZERO
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(java.math.BigInteger.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_172() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, bg=java.math.BigInteger.ZERO
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(java.math.BigInteger.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_173() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), bg=java.math.BigInteger.ZERO
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(java.math.BigInteger.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_174() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, bg=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_175() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, bg=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_176() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), bg=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_177() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, bg=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_178() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, bg=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_179() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), bg=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_180() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=0
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_181() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_182() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_183() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_184() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_185() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_186() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=-1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_187() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=-1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_188() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=-1
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_189() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_190() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_191() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_192() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=Integer.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_193() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=Integer.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_194() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=Integer.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_195() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=0L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(0L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_196() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=0L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(0L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_197() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=0L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(0L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_198() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=1L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_199() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_200() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_201() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_202() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_203() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_204() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_205() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_206() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_207() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=Long.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_208() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=Long.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_209() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=Long.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_210() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, fraction=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_211() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, fraction=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1 / 2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_212() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), fraction=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1 / 2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_213() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, fraction=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).multiply(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_214() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, fraction=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).multiply(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_215() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), fraction=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).multiply(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_216() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=0
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_217() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_218() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_219() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_220() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_221() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_222() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=-1
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).pow(-1);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_223() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=-1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_224() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=-1
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_225() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_226() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_227() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_228() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).pow(Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_229() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE)).pow(Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_230() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).pow(Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_231() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=0L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(0L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_232() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=0L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(0L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_233() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=0L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(0L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_234() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=1L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_235() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_236() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_237() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=-1L
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).pow(-1L);
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_238() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_239() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_240() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_241() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_242() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_243() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=Long.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).pow(Long.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_244() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=Long.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE)).pow(Long.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_245() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=Long.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).pow(Long.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_246() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=java.math.BigInteger.ZERO
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(java.math.BigInteger.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_247() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=java.math.BigInteger.ZERO
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(java.math.BigInteger.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_248() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=java.math.BigInteger.ZERO
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(java.math.BigInteger.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_249() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_250() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_251() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_252() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=java.math.BigInteger.ONE.negate()
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).pow(java.math.BigInteger.ONE.negate());
            fail("Expected org.apache.commons.math.exception.ZeroException");
        } catch (org.apache.commons.math.exception.ZeroException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_253() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_254() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_255() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=0.0d
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_256() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=0.0d
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_257() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=0.0d
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_258() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=1.0d
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_259() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=1.0d
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_260() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=1.0d
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_261() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=-1.0d
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_262() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=-1.0d
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_263() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=-1.0d
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_264() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=Double.NaN
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_265() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=Double.NaN
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_266() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=Double.NaN
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_267() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=Double.POSITIVE_INFINITY
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_268() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=Double.POSITIVE_INFINITY
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_269() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=Double.POSITIVE_INFINITY
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_270() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, bg=java.math.BigInteger.ZERO
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(java.math.BigInteger.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_271() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, bg=java.math.BigInteger.ZERO
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(java.math.BigInteger.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_272() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), bg=java.math.BigInteger.ZERO
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(java.math.BigInteger.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_273() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, bg=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_274() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, bg=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_275() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), bg=java.math.BigInteger.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(java.math.BigInteger.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_276() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, bg=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_277() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, bg=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_278() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), bg=java.math.BigInteger.ONE.negate()
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(java.math.BigInteger.ONE.negate());
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_279() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=0
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_280() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_281() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_282() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_283() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_284() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_285() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=-1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_286() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=-1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_287() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=-1
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_288() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_289() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2147483646", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_290() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=Integer.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_291() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, i=Integer.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_292() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, i=Integer.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2147483649", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_293() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), i=Integer.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_294() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=0L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(0L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_295() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=0L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(0L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_296() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=0L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(0L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_297() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=1L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_298() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_299() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_300() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_301() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_302() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=-1L
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(-1L);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_303() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_304() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-9223372036854775806", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_305() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=Long.MAX_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_306() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, l=Long.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_307() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, l=Long.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("9223372036854775809", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_308() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), l=Long.MIN_VALUE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_309() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, fraction=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1 / 2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_310() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, fraction=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("1 / 2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_311() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), fraction=new org.apache.commons.math.fraction.BigFraction(1, 2)
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(new org.apache.commons.math.fraction.BigFraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-3 / 2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_312() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, fraction=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).subtract(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_313() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, fraction=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).subtract(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_314() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), fraction=org.apache.commons.math.fraction.BigFraction.ONE
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).subtract(org.apache.commons.math.fraction.BigFraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.BigFraction", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

}
