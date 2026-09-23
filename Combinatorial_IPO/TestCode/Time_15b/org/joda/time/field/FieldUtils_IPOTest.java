package org.joda.time.field;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for FieldUtils.
 */
public class FieldUtils_IPOTest {
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
    public void test_safeAdd_pairwise_001() throws Exception {
        // Combination: val1=0, val2=0
        Object actual = FieldUtils.safeAdd(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_002() throws Exception {
        // Combination: val1=0, val2=1
        Object actual = FieldUtils.safeAdd(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_003() throws Exception {
        // Combination: val1=0, val2=-1
        Object actual = FieldUtils.safeAdd(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_004() throws Exception {
        // Combination: val1=0, val2=Integer.MAX_VALUE
        Object actual = FieldUtils.safeAdd(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_005() throws Exception {
        // Combination: val1=0, val2=Integer.MIN_VALUE
        Object actual = FieldUtils.safeAdd(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_006() throws Exception {
        // Combination: val1=1, val2=0
        Object actual = FieldUtils.safeAdd(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_007() throws Exception {
        // Combination: val1=1, val2=1
        Object actual = FieldUtils.safeAdd(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_008() throws Exception {
        // Combination: val1=1, val2=-1
        Object actual = FieldUtils.safeAdd(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_009() throws Exception {
        // Combination: val1=1, val2=Integer.MAX_VALUE
        try {
            FieldUtils.safeAdd(1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_010() throws Exception {
        // Combination: val1=1, val2=Integer.MIN_VALUE
        Object actual = FieldUtils.safeAdd(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_011() throws Exception {
        // Combination: val1=-1, val2=0
        Object actual = FieldUtils.safeAdd(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_012() throws Exception {
        // Combination: val1=-1, val2=1
        Object actual = FieldUtils.safeAdd(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_013() throws Exception {
        // Combination: val1=-1, val2=-1
        Object actual = FieldUtils.safeAdd(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_014() throws Exception {
        // Combination: val1=-1, val2=Integer.MAX_VALUE
        Object actual = FieldUtils.safeAdd(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483646", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_015() throws Exception {
        // Combination: val1=-1, val2=Integer.MIN_VALUE
        try {
            FieldUtils.safeAdd(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_016() throws Exception {
        // Combination: val1=Integer.MAX_VALUE, val2=0
        Object actual = FieldUtils.safeAdd(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_017() throws Exception {
        // Combination: val1=Integer.MAX_VALUE, val2=1
        try {
            FieldUtils.safeAdd(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_018() throws Exception {
        // Combination: val1=Integer.MAX_VALUE, val2=-1
        Object actual = FieldUtils.safeAdd(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483646", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_019() throws Exception {
        // Combination: val1=Integer.MAX_VALUE, val2=Integer.MAX_VALUE
        try {
            FieldUtils.safeAdd(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_020() throws Exception {
        // Combination: val1=Integer.MAX_VALUE, val2=Integer.MIN_VALUE
        Object actual = FieldUtils.safeAdd(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_021() throws Exception {
        // Combination: val1=Integer.MIN_VALUE, val2=0
        Object actual = FieldUtils.safeAdd(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_022() throws Exception {
        // Combination: val1=Integer.MIN_VALUE, val2=1
        Object actual = FieldUtils.safeAdd(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_023() throws Exception {
        // Combination: val1=Integer.MIN_VALUE, val2=-1
        try {
            FieldUtils.safeAdd(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_024() throws Exception {
        // Combination: val1=Integer.MIN_VALUE, val2=Integer.MAX_VALUE
        Object actual = FieldUtils.safeAdd(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_025() throws Exception {
        // Combination: val1=Integer.MIN_VALUE, val2=Integer.MIN_VALUE
        try {
            FieldUtils.safeAdd(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_026() throws Exception {
        // Combination: val1=0L, val2=0L
        Object actual = FieldUtils.safeAdd(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_027() throws Exception {
        // Combination: val1=0L, val2=1L
        Object actual = FieldUtils.safeAdd(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_028() throws Exception {
        // Combination: val1=0L, val2=-1L
        Object actual = FieldUtils.safeAdd(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_029() throws Exception {
        // Combination: val1=0L, val2=Long.MAX_VALUE
        Object actual = FieldUtils.safeAdd(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_030() throws Exception {
        // Combination: val1=0L, val2=Long.MIN_VALUE
        Object actual = FieldUtils.safeAdd(0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_031() throws Exception {
        // Combination: val1=1L, val2=0L
        Object actual = FieldUtils.safeAdd(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_032() throws Exception {
        // Combination: val1=1L, val2=1L
        Object actual = FieldUtils.safeAdd(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_033() throws Exception {
        // Combination: val1=1L, val2=-1L
        Object actual = FieldUtils.safeAdd(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_034() throws Exception {
        // Combination: val1=1L, val2=Long.MAX_VALUE
        try {
            FieldUtils.safeAdd(1L, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_035() throws Exception {
        // Combination: val1=1L, val2=Long.MIN_VALUE
        Object actual = FieldUtils.safeAdd(1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_036() throws Exception {
        // Combination: val1=-1L, val2=0L
        Object actual = FieldUtils.safeAdd(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_037() throws Exception {
        // Combination: val1=-1L, val2=1L
        Object actual = FieldUtils.safeAdd(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_038() throws Exception {
        // Combination: val1=-1L, val2=-1L
        Object actual = FieldUtils.safeAdd(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_039() throws Exception {
        // Combination: val1=-1L, val2=Long.MAX_VALUE
        Object actual = FieldUtils.safeAdd(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775806", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_040() throws Exception {
        // Combination: val1=-1L, val2=Long.MIN_VALUE
        try {
            FieldUtils.safeAdd(-1L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_041() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=0L
        Object actual = FieldUtils.safeAdd(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_042() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=1L
        try {
            FieldUtils.safeAdd(Long.MAX_VALUE, 1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_043() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=-1L
        Object actual = FieldUtils.safeAdd(Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775806", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_044() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=Long.MAX_VALUE
        try {
            FieldUtils.safeAdd(Long.MAX_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_045() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=Long.MIN_VALUE
        Object actual = FieldUtils.safeAdd(Long.MAX_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_046() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=0L
        Object actual = FieldUtils.safeAdd(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_047() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=1L
        Object actual = FieldUtils.safeAdd(Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_048() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=-1L
        try {
            FieldUtils.safeAdd(Long.MIN_VALUE, -1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_049() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=Long.MAX_VALUE
        Object actual = FieldUtils.safeAdd(Long.MIN_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeAdd_pairwise_050() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=Long.MIN_VALUE
        try {
            FieldUtils.safeAdd(Long.MIN_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_051() throws Exception {
        // Combination: val1=0L, val2=0L
        Object actual = FieldUtils.safeSubtract(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_052() throws Exception {
        // Combination: val1=0L, val2=1L
        Object actual = FieldUtils.safeSubtract(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_053() throws Exception {
        // Combination: val1=0L, val2=-1L
        Object actual = FieldUtils.safeSubtract(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_054() throws Exception {
        // Combination: val1=0L, val2=Long.MAX_VALUE
        Object actual = FieldUtils.safeSubtract(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_055() throws Exception {
        // Combination: val1=0L, val2=Long.MIN_VALUE
        try {
            FieldUtils.safeSubtract(0L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_056() throws Exception {
        // Combination: val1=1L, val2=0L
        Object actual = FieldUtils.safeSubtract(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_057() throws Exception {
        // Combination: val1=1L, val2=1L
        Object actual = FieldUtils.safeSubtract(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_058() throws Exception {
        // Combination: val1=1L, val2=-1L
        Object actual = FieldUtils.safeSubtract(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_059() throws Exception {
        // Combination: val1=1L, val2=Long.MAX_VALUE
        Object actual = FieldUtils.safeSubtract(1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775806", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_060() throws Exception {
        // Combination: val1=1L, val2=Long.MIN_VALUE
        try {
            FieldUtils.safeSubtract(1L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_061() throws Exception {
        // Combination: val1=-1L, val2=0L
        Object actual = FieldUtils.safeSubtract(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_062() throws Exception {
        // Combination: val1=-1L, val2=1L
        Object actual = FieldUtils.safeSubtract(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_063() throws Exception {
        // Combination: val1=-1L, val2=-1L
        Object actual = FieldUtils.safeSubtract(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_064() throws Exception {
        // Combination: val1=-1L, val2=Long.MAX_VALUE
        Object actual = FieldUtils.safeSubtract(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_065() throws Exception {
        // Combination: val1=-1L, val2=Long.MIN_VALUE
        Object actual = FieldUtils.safeSubtract(-1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_066() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=0L
        Object actual = FieldUtils.safeSubtract(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_067() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=1L
        Object actual = FieldUtils.safeSubtract(Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775806", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_068() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=-1L
        try {
            FieldUtils.safeSubtract(Long.MAX_VALUE, -1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_069() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=Long.MAX_VALUE
        Object actual = FieldUtils.safeSubtract(Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_070() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=Long.MIN_VALUE
        try {
            FieldUtils.safeSubtract(Long.MAX_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_071() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=0L
        Object actual = FieldUtils.safeSubtract(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_072() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=1L
        try {
            FieldUtils.safeSubtract(Long.MIN_VALUE, 1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_073() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=-1L
        Object actual = FieldUtils.safeSubtract(Long.MIN_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_074() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=Long.MAX_VALUE
        try {
            FieldUtils.safeSubtract(Long.MIN_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeSubtract_pairwise_075() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=Long.MIN_VALUE
        Object actual = FieldUtils.safeSubtract(Long.MIN_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_076() throws Exception {
        // Combination: val1=0, val2=0
        Object actual = FieldUtils.safeMultiply(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_077() throws Exception {
        // Combination: val1=0, val2=1
        Object actual = FieldUtils.safeMultiply(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_078() throws Exception {
        // Combination: val1=0, val2=-1
        Object actual = FieldUtils.safeMultiply(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_079() throws Exception {
        // Combination: val1=0, val2=Integer.MAX_VALUE
        Object actual = FieldUtils.safeMultiply(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_080() throws Exception {
        // Combination: val1=0, val2=Integer.MIN_VALUE
        Object actual = FieldUtils.safeMultiply(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_081() throws Exception {
        // Combination: val1=1, val2=0
        Object actual = FieldUtils.safeMultiply(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_082() throws Exception {
        // Combination: val1=1, val2=1
        Object actual = FieldUtils.safeMultiply(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_083() throws Exception {
        // Combination: val1=1, val2=-1
        Object actual = FieldUtils.safeMultiply(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_084() throws Exception {
        // Combination: val1=1, val2=Integer.MAX_VALUE
        Object actual = FieldUtils.safeMultiply(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_085() throws Exception {
        // Combination: val1=1, val2=Integer.MIN_VALUE
        Object actual = FieldUtils.safeMultiply(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_086() throws Exception {
        // Combination: val1=-1, val2=0
        Object actual = FieldUtils.safeMultiply(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_087() throws Exception {
        // Combination: val1=-1, val2=1
        Object actual = FieldUtils.safeMultiply(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_088() throws Exception {
        // Combination: val1=-1, val2=-1
        Object actual = FieldUtils.safeMultiply(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_089() throws Exception {
        // Combination: val1=-1, val2=Integer.MAX_VALUE
        Object actual = FieldUtils.safeMultiply(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_090() throws Exception {
        // Combination: val1=-1, val2=Integer.MIN_VALUE
        try {
            FieldUtils.safeMultiply(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_091() throws Exception {
        // Combination: val1=Integer.MAX_VALUE, val2=0
        Object actual = FieldUtils.safeMultiply(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_092() throws Exception {
        // Combination: val1=Integer.MAX_VALUE, val2=1
        Object actual = FieldUtils.safeMultiply(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_093() throws Exception {
        // Combination: val1=Integer.MAX_VALUE, val2=-1
        Object actual = FieldUtils.safeMultiply(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_094() throws Exception {
        // Combination: val1=Integer.MAX_VALUE, val2=Integer.MAX_VALUE
        try {
            FieldUtils.safeMultiply(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_095() throws Exception {
        // Combination: val1=Integer.MAX_VALUE, val2=Integer.MIN_VALUE
        try {
            FieldUtils.safeMultiply(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_096() throws Exception {
        // Combination: val1=Integer.MIN_VALUE, val2=0
        Object actual = FieldUtils.safeMultiply(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_097() throws Exception {
        // Combination: val1=Integer.MIN_VALUE, val2=1
        Object actual = FieldUtils.safeMultiply(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_098() throws Exception {
        // Combination: val1=Integer.MIN_VALUE, val2=-1
        try {
            FieldUtils.safeMultiply(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_099() throws Exception {
        // Combination: val1=Integer.MIN_VALUE, val2=Integer.MAX_VALUE
        try {
            FieldUtils.safeMultiply(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_100() throws Exception {
        // Combination: val1=Integer.MIN_VALUE, val2=Integer.MIN_VALUE
        try {
            FieldUtils.safeMultiply(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_101() throws Exception {
        // Combination: val1=0L, val2=0
        Object actual = FieldUtils.safeMultiply(0L, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_102() throws Exception {
        // Combination: val1=0L, val2=1
        Object actual = FieldUtils.safeMultiply(0L, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_103() throws Exception {
        // Combination: val1=0L, val2=-1
        Object actual = FieldUtils.safeMultiply(0L, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_104() throws Exception {
        // Combination: val1=0L, val2=Integer.MAX_VALUE
        Object actual = FieldUtils.safeMultiply(0L, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_105() throws Exception {
        // Combination: val1=0L, val2=Integer.MIN_VALUE
        Object actual = FieldUtils.safeMultiply(0L, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_106() throws Exception {
        // Combination: val1=1L, val2=0
        Object actual = FieldUtils.safeMultiply(1L, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_107() throws Exception {
        // Combination: val1=1L, val2=1
        Object actual = FieldUtils.safeMultiply(1L, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_108() throws Exception {
        // Combination: val1=1L, val2=-1
        Object actual = FieldUtils.safeMultiply(1L, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_109() throws Exception {
        // Combination: val1=1L, val2=Integer.MAX_VALUE
        Object actual = FieldUtils.safeMultiply(1L, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_110() throws Exception {
        // Combination: val1=1L, val2=Integer.MIN_VALUE
        Object actual = FieldUtils.safeMultiply(1L, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_111() throws Exception {
        // Combination: val1=-1L, val2=0
        Object actual = FieldUtils.safeMultiply(-1L, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_112() throws Exception {
        // Combination: val1=-1L, val2=1
        Object actual = FieldUtils.safeMultiply(-1L, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_113() throws Exception {
        // Combination: val1=-1L, val2=-1
        Object actual = FieldUtils.safeMultiply(-1L, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_114() throws Exception {
        // Combination: val1=-1L, val2=Integer.MAX_VALUE
        Object actual = FieldUtils.safeMultiply(-1L, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_115() throws Exception {
        // Combination: val1=-1L, val2=Integer.MIN_VALUE
        Object actual = FieldUtils.safeMultiply(-1L, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_116() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=0
        Object actual = FieldUtils.safeMultiply(Long.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_117() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=1
        Object actual = FieldUtils.safeMultiply(Long.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_118() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=-1
        Object actual = FieldUtils.safeMultiply(Long.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_119() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=Integer.MAX_VALUE
        try {
            FieldUtils.safeMultiply(Long.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_120() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=Integer.MIN_VALUE
        try {
            FieldUtils.safeMultiply(Long.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_121() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=0
        Object actual = FieldUtils.safeMultiply(Long.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_122() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=1
        Object actual = FieldUtils.safeMultiply(Long.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_123() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=-1
        try {
            FieldUtils.safeMultiply(Long.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_124() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=Integer.MAX_VALUE
        try {
            FieldUtils.safeMultiply(Long.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_125() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=Integer.MIN_VALUE
        try {
            FieldUtils.safeMultiply(Long.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_126() throws Exception {
        // Combination: val1=0L, val2=0L
        Object actual = FieldUtils.safeMultiply(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_127() throws Exception {
        // Combination: val1=0L, val2=1L
        Object actual = FieldUtils.safeMultiply(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_128() throws Exception {
        // Combination: val1=0L, val2=-1L
        Object actual = FieldUtils.safeMultiply(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_129() throws Exception {
        // Combination: val1=0L, val2=Long.MAX_VALUE
        Object actual = FieldUtils.safeMultiply(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_130() throws Exception {
        // Combination: val1=0L, val2=Long.MIN_VALUE
        Object actual = FieldUtils.safeMultiply(0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_131() throws Exception {
        // Combination: val1=1L, val2=0L
        Object actual = FieldUtils.safeMultiply(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_132() throws Exception {
        // Combination: val1=1L, val2=1L
        Object actual = FieldUtils.safeMultiply(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_133() throws Exception {
        // Combination: val1=1L, val2=-1L
        Object actual = FieldUtils.safeMultiply(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_134() throws Exception {
        // Combination: val1=1L, val2=Long.MAX_VALUE
        Object actual = FieldUtils.safeMultiply(1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_135() throws Exception {
        // Combination: val1=1L, val2=Long.MIN_VALUE
        Object actual = FieldUtils.safeMultiply(1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_136() throws Exception {
        // Combination: val1=-1L, val2=0L
        Object actual = FieldUtils.safeMultiply(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_137() throws Exception {
        // Combination: val1=-1L, val2=1L
        Object actual = FieldUtils.safeMultiply(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_138() throws Exception {
        // Combination: val1=-1L, val2=-1L
        Object actual = FieldUtils.safeMultiply(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_139() throws Exception {
        // Combination: val1=-1L, val2=Long.MAX_VALUE
        Object actual = FieldUtils.safeMultiply(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_140() throws Exception {
        // Combination: val1=-1L, val2=Long.MIN_VALUE
        try {
            FieldUtils.safeMultiply(-1L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_141() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=0L
        Object actual = FieldUtils.safeMultiply(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_142() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=1L
        Object actual = FieldUtils.safeMultiply(Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_143() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=-1L
        Object actual = FieldUtils.safeMultiply(Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_144() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=Long.MAX_VALUE
        try {
            FieldUtils.safeMultiply(Long.MAX_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_145() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=Long.MIN_VALUE
        try {
            FieldUtils.safeMultiply(Long.MAX_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_146() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=0L
        Object actual = FieldUtils.safeMultiply(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_147() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=1L
        Object actual = FieldUtils.safeMultiply(Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_148() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=-1L
        try {
            FieldUtils.safeMultiply(Long.MIN_VALUE, -1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_149() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=Long.MAX_VALUE
        try {
            FieldUtils.safeMultiply(Long.MIN_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiply_pairwise_150() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=Long.MIN_VALUE
        try {
            FieldUtils.safeMultiply(Long.MIN_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_151() throws Exception {
        // Combination: val1=0L, val2=0L
        Object actual = FieldUtils.safeMultiplyToInt(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_152() throws Exception {
        // Combination: val1=0L, val2=1L
        Object actual = FieldUtils.safeMultiplyToInt(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_153() throws Exception {
        // Combination: val1=0L, val2=-1L
        Object actual = FieldUtils.safeMultiplyToInt(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_154() throws Exception {
        // Combination: val1=0L, val2=Long.MAX_VALUE
        Object actual = FieldUtils.safeMultiplyToInt(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_155() throws Exception {
        // Combination: val1=0L, val2=Long.MIN_VALUE
        Object actual = FieldUtils.safeMultiplyToInt(0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_156() throws Exception {
        // Combination: val1=1L, val2=0L
        Object actual = FieldUtils.safeMultiplyToInt(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_157() throws Exception {
        // Combination: val1=1L, val2=1L
        Object actual = FieldUtils.safeMultiplyToInt(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_158() throws Exception {
        // Combination: val1=1L, val2=-1L
        Object actual = FieldUtils.safeMultiplyToInt(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_159() throws Exception {
        // Combination: val1=1L, val2=Long.MAX_VALUE
        try {
            FieldUtils.safeMultiplyToInt(1L, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_160() throws Exception {
        // Combination: val1=1L, val2=Long.MIN_VALUE
        try {
            FieldUtils.safeMultiplyToInt(1L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_161() throws Exception {
        // Combination: val1=-1L, val2=0L
        Object actual = FieldUtils.safeMultiplyToInt(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_162() throws Exception {
        // Combination: val1=-1L, val2=1L
        Object actual = FieldUtils.safeMultiplyToInt(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_163() throws Exception {
        // Combination: val1=-1L, val2=-1L
        Object actual = FieldUtils.safeMultiplyToInt(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_164() throws Exception {
        // Combination: val1=-1L, val2=Long.MAX_VALUE
        try {
            FieldUtils.safeMultiplyToInt(-1L, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_165() throws Exception {
        // Combination: val1=-1L, val2=Long.MIN_VALUE
        try {
            FieldUtils.safeMultiplyToInt(-1L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_166() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=0L
        Object actual = FieldUtils.safeMultiplyToInt(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_167() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=1L
        try {
            FieldUtils.safeMultiplyToInt(Long.MAX_VALUE, 1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_168() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=-1L
        try {
            FieldUtils.safeMultiplyToInt(Long.MAX_VALUE, -1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_169() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=Long.MAX_VALUE
        try {
            FieldUtils.safeMultiplyToInt(Long.MAX_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_170() throws Exception {
        // Combination: val1=Long.MAX_VALUE, val2=Long.MIN_VALUE
        try {
            FieldUtils.safeMultiplyToInt(Long.MAX_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_171() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=0L
        Object actual = FieldUtils.safeMultiplyToInt(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_172() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=1L
        try {
            FieldUtils.safeMultiplyToInt(Long.MIN_VALUE, 1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_173() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=-1L
        try {
            FieldUtils.safeMultiplyToInt(Long.MIN_VALUE, -1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_174() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=Long.MAX_VALUE
        try {
            FieldUtils.safeMultiplyToInt(Long.MIN_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_safeMultiplyToInt_pairwise_175() throws Exception {
        // Combination: val1=Long.MIN_VALUE, val2=Long.MIN_VALUE
        try {
            FieldUtils.safeMultiplyToInt(Long.MIN_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_176() throws Exception {
        // Combination: currentValue=0, wrapValue=0, minValue=0, maxValue=0
        try {
            FieldUtils.getWrappedValue(0, 0, 0, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_177() throws Exception {
        // Combination: currentValue=0, wrapValue=1, minValue=1, maxValue=1
        try {
            FieldUtils.getWrappedValue(0, 1, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_178() throws Exception {
        // Combination: currentValue=0, wrapValue=-1, minValue=-1, maxValue=-1
        try {
            FieldUtils.getWrappedValue(0, -1, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_179() throws Exception {
        // Combination: currentValue=0, wrapValue=Integer.MAX_VALUE, minValue=Integer.MAX_VALUE, maxValue=Integer.MAX_VALUE
        try {
            FieldUtils.getWrappedValue(0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_180() throws Exception {
        // Combination: currentValue=0, wrapValue=Integer.MIN_VALUE, minValue=Integer.MIN_VALUE, maxValue=Integer.MIN_VALUE
        try {
            FieldUtils.getWrappedValue(0, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_181() throws Exception {
        // Combination: currentValue=1, wrapValue=-1, minValue=1, maxValue=0
        try {
            FieldUtils.getWrappedValue(1, -1, 1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_182() throws Exception {
        // Combination: currentValue=1, wrapValue=Integer.MAX_VALUE, minValue=0, maxValue=1
        Object actual = FieldUtils.getWrappedValue(1, Integer.MAX_VALUE, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_183() throws Exception {
        // Combination: currentValue=1, wrapValue=0, minValue=Integer.MAX_VALUE, maxValue=-1
        try {
            FieldUtils.getWrappedValue(1, 0, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_184() throws Exception {
        // Combination: currentValue=1, wrapValue=1, minValue=-1, maxValue=Integer.MAX_VALUE
        Object actual = FieldUtils.getWrappedValue(1, 1, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_185() throws Exception {
        // Combination: currentValue=1, wrapValue=1, minValue=0, maxValue=Integer.MIN_VALUE
        try {
            FieldUtils.getWrappedValue(1, 1, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_186() throws Exception {
        // Combination: currentValue=-1, wrapValue=Integer.MAX_VALUE, minValue=-1, maxValue=0
        Object actual = FieldUtils.getWrappedValue(-1, Integer.MAX_VALUE, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_187() throws Exception {
        // Combination: currentValue=-1, wrapValue=-1, minValue=Integer.MAX_VALUE, maxValue=1
        try {
            FieldUtils.getWrappedValue(-1, -1, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_188() throws Exception {
        // Combination: currentValue=-1, wrapValue=Integer.MIN_VALUE, minValue=0, maxValue=-1
        try {
            FieldUtils.getWrappedValue(-1, Integer.MIN_VALUE, 0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_189() throws Exception {
        // Combination: currentValue=-1, wrapValue=0, minValue=1, maxValue=Integer.MAX_VALUE
        Object actual = FieldUtils.getWrappedValue(-1, 0, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483646", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_190() throws Exception {
        // Combination: currentValue=-1, wrapValue=Integer.MAX_VALUE, minValue=1, maxValue=Integer.MIN_VALUE
        try {
            FieldUtils.getWrappedValue(-1, Integer.MAX_VALUE, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_191() throws Exception {
        // Combination: currentValue=Integer.MAX_VALUE, wrapValue=1, minValue=Integer.MAX_VALUE, maxValue=0
        try {
            FieldUtils.getWrappedValue(Integer.MAX_VALUE, 1, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_192() throws Exception {
        // Combination: currentValue=Integer.MAX_VALUE, wrapValue=0, minValue=-1, maxValue=1
        Object actual = FieldUtils.getWrappedValue(Integer.MAX_VALUE, 0, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_193() throws Exception {
        // Combination: currentValue=Integer.MAX_VALUE, wrapValue=Integer.MAX_VALUE, minValue=1, maxValue=-1
        try {
            FieldUtils.getWrappedValue(Integer.MAX_VALUE, Integer.MAX_VALUE, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_194() throws Exception {
        // Combination: currentValue=Integer.MAX_VALUE, wrapValue=-1, minValue=0, maxValue=Integer.MAX_VALUE
        Object actual = FieldUtils.getWrappedValue(Integer.MAX_VALUE, -1, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483646", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_195() throws Exception {
        // Combination: currentValue=Integer.MAX_VALUE, wrapValue=Integer.MIN_VALUE, minValue=-1, maxValue=Integer.MIN_VALUE
        try {
            FieldUtils.getWrappedValue(Integer.MAX_VALUE, Integer.MIN_VALUE, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_196() throws Exception {
        // Combination: currentValue=Integer.MIN_VALUE, wrapValue=0, minValue=Integer.MIN_VALUE, maxValue=0
        Object actual = FieldUtils.getWrappedValue(Integer.MIN_VALUE, 0, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_197() throws Exception {
        // Combination: currentValue=Integer.MIN_VALUE, wrapValue=Integer.MIN_VALUE, minValue=0, maxValue=1
        Object actual = FieldUtils.getWrappedValue(Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_198() throws Exception {
        // Combination: currentValue=Integer.MIN_VALUE, wrapValue=1, minValue=1, maxValue=-1
        try {
            FieldUtils.getWrappedValue(Integer.MIN_VALUE, 1, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_199() throws Exception {
        // Combination: currentValue=Integer.MIN_VALUE, wrapValue=-1, minValue=-1, maxValue=Integer.MAX_VALUE
        Object actual = FieldUtils.getWrappedValue(Integer.MIN_VALUE, -1, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_200() throws Exception {
        // Combination: currentValue=Integer.MIN_VALUE, wrapValue=0, minValue=Integer.MAX_VALUE, maxValue=Integer.MIN_VALUE
        try {
            FieldUtils.getWrappedValue(Integer.MIN_VALUE, 0, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_201() throws Exception {
        // Combination: currentValue=1, wrapValue=1, minValue=Integer.MIN_VALUE, maxValue=1
        Object actual = FieldUtils.getWrappedValue(1, 1, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_202() throws Exception {
        // Combination: currentValue=-1, wrapValue=1, minValue=Integer.MIN_VALUE, maxValue=-1
        Object actual = FieldUtils.getWrappedValue(-1, 1, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_203() throws Exception {
        // Combination: currentValue=Integer.MAX_VALUE, wrapValue=-1, minValue=Integer.MIN_VALUE, maxValue=Integer.MAX_VALUE
        try {
            FieldUtils.getWrappedValue(Integer.MAX_VALUE, -1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_204() throws Exception {
        // Combination: currentValue=0, wrapValue=-1, minValue=0, maxValue=Integer.MIN_VALUE
        try {
            FieldUtils.getWrappedValue(0, -1, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_205() throws Exception {
        // Combination: currentValue=Integer.MIN_VALUE, wrapValue=Integer.MAX_VALUE, minValue=Integer.MIN_VALUE, maxValue=0
        Object actual = FieldUtils.getWrappedValue(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_206() throws Exception {
        // Combination: currentValue=1, wrapValue=Integer.MIN_VALUE, minValue=1, maxValue=0
        try {
            FieldUtils.getWrappedValue(1, Integer.MIN_VALUE, 1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_207() throws Exception {
        // Combination: currentValue=0, wrapValue=Integer.MIN_VALUE, minValue=Integer.MAX_VALUE, maxValue=Integer.MAX_VALUE
        try {
            FieldUtils.getWrappedValue(0, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_208() throws Exception {
        // Combination: value=0, minValue=0, maxValue=0
        try {
            FieldUtils.getWrappedValue(0, 0, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_209() throws Exception {
        // Combination: value=1, minValue=1, maxValue=0
        try {
            FieldUtils.getWrappedValue(1, 1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_210() throws Exception {
        // Combination: value=-1, minValue=-1, maxValue=0
        Object actual = FieldUtils.getWrappedValue(-1, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_211() throws Exception {
        // Combination: value=Integer.MAX_VALUE, minValue=Integer.MAX_VALUE, maxValue=0
        try {
            FieldUtils.getWrappedValue(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_212() throws Exception {
        // Combination: value=Integer.MIN_VALUE, minValue=Integer.MIN_VALUE, maxValue=0
        Object actual = FieldUtils.getWrappedValue(Integer.MIN_VALUE, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_213() throws Exception {
        // Combination: value=1, minValue=0, maxValue=1
        Object actual = FieldUtils.getWrappedValue(1, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_214() throws Exception {
        // Combination: value=0, minValue=1, maxValue=1
        try {
            FieldUtils.getWrappedValue(0, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_215() throws Exception {
        // Combination: value=Integer.MAX_VALUE, minValue=-1, maxValue=1
        Object actual = FieldUtils.getWrappedValue(Integer.MAX_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_216() throws Exception {
        // Combination: value=-1, minValue=Integer.MAX_VALUE, maxValue=1
        try {
            FieldUtils.getWrappedValue(-1, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_217() throws Exception {
        // Combination: value=0, minValue=Integer.MIN_VALUE, maxValue=1
        Object actual = FieldUtils.getWrappedValue(0, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_218() throws Exception {
        // Combination: value=-1, minValue=0, maxValue=-1
        try {
            FieldUtils.getWrappedValue(-1, 0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_219() throws Exception {
        // Combination: value=Integer.MAX_VALUE, minValue=1, maxValue=-1
        try {
            FieldUtils.getWrappedValue(Integer.MAX_VALUE, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_220() throws Exception {
        // Combination: value=0, minValue=-1, maxValue=-1
        try {
            FieldUtils.getWrappedValue(0, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_221() throws Exception {
        // Combination: value=1, minValue=Integer.MAX_VALUE, maxValue=-1
        try {
            FieldUtils.getWrappedValue(1, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_222() throws Exception {
        // Combination: value=1, minValue=Integer.MIN_VALUE, maxValue=-1
        Object actual = FieldUtils.getWrappedValue(1, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_223() throws Exception {
        // Combination: value=Integer.MAX_VALUE, minValue=0, maxValue=Integer.MAX_VALUE
        Object actual = FieldUtils.getWrappedValue(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_224() throws Exception {
        // Combination: value=-1, minValue=1, maxValue=Integer.MAX_VALUE
        Object actual = FieldUtils.getWrappedValue(-1, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483646", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_225() throws Exception {
        // Combination: value=1, minValue=-1, maxValue=Integer.MAX_VALUE
        Object actual = FieldUtils.getWrappedValue(1, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_226() throws Exception {
        // Combination: value=0, minValue=Integer.MAX_VALUE, maxValue=Integer.MAX_VALUE
        try {
            FieldUtils.getWrappedValue(0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_227() throws Exception {
        // Combination: value=-1, minValue=Integer.MIN_VALUE, maxValue=Integer.MAX_VALUE
        try {
            FieldUtils.getWrappedValue(-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_228() throws Exception {
        // Combination: value=Integer.MIN_VALUE, minValue=0, maxValue=Integer.MIN_VALUE
        try {
            FieldUtils.getWrappedValue(Integer.MIN_VALUE, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_229() throws Exception {
        // Combination: value=0, minValue=1, maxValue=Integer.MIN_VALUE
        try {
            FieldUtils.getWrappedValue(0, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_230() throws Exception {
        // Combination: value=1, minValue=-1, maxValue=Integer.MIN_VALUE
        try {
            FieldUtils.getWrappedValue(1, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_231() throws Exception {
        // Combination: value=-1, minValue=Integer.MAX_VALUE, maxValue=Integer.MIN_VALUE
        try {
            FieldUtils.getWrappedValue(-1, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_232() throws Exception {
        // Combination: value=Integer.MAX_VALUE, minValue=Integer.MIN_VALUE, maxValue=Integer.MIN_VALUE
        try {
            FieldUtils.getWrappedValue(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_233() throws Exception {
        // Combination: value=Integer.MIN_VALUE, minValue=1, maxValue=1
        try {
            FieldUtils.getWrappedValue(Integer.MIN_VALUE, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_234() throws Exception {
        // Combination: value=Integer.MIN_VALUE, minValue=-1, maxValue=-1
        try {
            FieldUtils.getWrappedValue(Integer.MIN_VALUE, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getWrappedValue_pairwise_235() throws Exception {
        // Combination: value=Integer.MIN_VALUE, minValue=Integer.MAX_VALUE, maxValue=Integer.MAX_VALUE
        try {
            FieldUtils.getWrappedValue(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_236() throws Exception {
        // Combination: object1=new Object(), object2=new Object()
        Object actual = FieldUtils.equals(new Object(), new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_237() throws Exception {
        // Combination: object1=new Object(), object2="sample_str"
        Object actual = FieldUtils.equals(new Object(), "sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_238() throws Exception {
        // Combination: object1=new Object(), object2=Integer.valueOf(1)
        Object actual = FieldUtils.equals(new Object(), Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_239() throws Exception {
        // Combination: object1="sample_str", object2=new Object()
        Object actual = FieldUtils.equals("sample_str", new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_240() throws Exception {
        // Combination: object1="sample_str", object2="sample_str"
        Object actual = FieldUtils.equals("sample_str", "sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_241() throws Exception {
        // Combination: object1="sample_str", object2=Integer.valueOf(1)
        Object actual = FieldUtils.equals("sample_str", Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_242() throws Exception {
        // Combination: object1=Integer.valueOf(1), object2=new Object()
        Object actual = FieldUtils.equals(Integer.valueOf(1), new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_243() throws Exception {
        // Combination: object1=Integer.valueOf(1), object2="sample_str"
        Object actual = FieldUtils.equals(Integer.valueOf(1), "sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_244() throws Exception {
        // Combination: object1=Integer.valueOf(1), object2=Integer.valueOf(1)
        Object actual = FieldUtils.equals(Integer.valueOf(1), Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

}
