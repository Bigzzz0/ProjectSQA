package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for NumberUtils.
 */
public class NumberUtils_IPOTest {
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
    public void test_stringToInt_pairwise_001() throws Exception {
        // Combination: str="", defaultValue=0
        Object actual = NumberUtils.stringToInt("", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_002() throws Exception {
        // Combination: str=" ", defaultValue=0
        Object actual = NumberUtils.stringToInt(" ", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_003() throws Exception {
        // Combination: str="a", defaultValue=0
        Object actual = NumberUtils.stringToInt("a", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_004() throws Exception {
        // Combination: str="test123", defaultValue=0
        Object actual = NumberUtils.stringToInt("test123", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_005() throws Exception {
        // Combination: str="!@#", defaultValue=0
        Object actual = NumberUtils.stringToInt("!@#", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_006() throws Exception {
        // Combination: str="0", defaultValue=0
        Object actual = NumberUtils.stringToInt("0", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_007() throws Exception {
        // Combination: str="-1", defaultValue=0
        Object actual = NumberUtils.stringToInt("-1", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_008() throws Exception {
        // Combination: str="1.5", defaultValue=0
        Object actual = NumberUtils.stringToInt("1.5", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_009() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=0
        Object actual = NumberUtils.stringToInt("9223372036854775807", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_010() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=0
        Object actual = NumberUtils.stringToInt("9223372036854775808", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_011() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0
        Object actual = NumberUtils.stringToInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_012() throws Exception {
        // Combination: str="", defaultValue=1
        Object actual = NumberUtils.stringToInt("", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_013() throws Exception {
        // Combination: str=" ", defaultValue=1
        Object actual = NumberUtils.stringToInt(" ", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_014() throws Exception {
        // Combination: str="a", defaultValue=1
        Object actual = NumberUtils.stringToInt("a", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_015() throws Exception {
        // Combination: str="test123", defaultValue=1
        Object actual = NumberUtils.stringToInt("test123", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_016() throws Exception {
        // Combination: str="!@#", defaultValue=1
        Object actual = NumberUtils.stringToInt("!@#", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_017() throws Exception {
        // Combination: str="0", defaultValue=1
        Object actual = NumberUtils.stringToInt("0", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_018() throws Exception {
        // Combination: str="-1", defaultValue=1
        Object actual = NumberUtils.stringToInt("-1", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_019() throws Exception {
        // Combination: str="1.5", defaultValue=1
        Object actual = NumberUtils.stringToInt("1.5", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_020() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=1
        Object actual = NumberUtils.stringToInt("9223372036854775807", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_021() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=1
        Object actual = NumberUtils.stringToInt("9223372036854775808", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_022() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1
        Object actual = NumberUtils.stringToInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_023() throws Exception {
        // Combination: str="", defaultValue=-1
        Object actual = NumberUtils.stringToInt("", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_024() throws Exception {
        // Combination: str=" ", defaultValue=-1
        Object actual = NumberUtils.stringToInt(" ", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_025() throws Exception {
        // Combination: str="a", defaultValue=-1
        Object actual = NumberUtils.stringToInt("a", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_026() throws Exception {
        // Combination: str="test123", defaultValue=-1
        Object actual = NumberUtils.stringToInt("test123", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_027() throws Exception {
        // Combination: str="!@#", defaultValue=-1
        Object actual = NumberUtils.stringToInt("!@#", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_028() throws Exception {
        // Combination: str="0", defaultValue=-1
        Object actual = NumberUtils.stringToInt("0", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_029() throws Exception {
        // Combination: str="-1", defaultValue=-1
        Object actual = NumberUtils.stringToInt("-1", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_030() throws Exception {
        // Combination: str="1.5", defaultValue=-1
        Object actual = NumberUtils.stringToInt("1.5", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_031() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=-1
        Object actual = NumberUtils.stringToInt("9223372036854775807", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_032() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=-1
        Object actual = NumberUtils.stringToInt("9223372036854775808", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_033() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1
        Object actual = NumberUtils.stringToInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_034() throws Exception {
        // Combination: str="", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.stringToInt("", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_035() throws Exception {
        // Combination: str=" ", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.stringToInt(" ", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_036() throws Exception {
        // Combination: str="a", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.stringToInt("a", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_037() throws Exception {
        // Combination: str="test123", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.stringToInt("test123", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_038() throws Exception {
        // Combination: str="!@#", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.stringToInt("!@#", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_039() throws Exception {
        // Combination: str="0", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.stringToInt("0", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_040() throws Exception {
        // Combination: str="-1", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.stringToInt("-1", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_041() throws Exception {
        // Combination: str="1.5", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.stringToInt("1.5", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_042() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.stringToInt("9223372036854775807", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_043() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.stringToInt("9223372036854775808", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_044() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.stringToInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_045() throws Exception {
        // Combination: str="", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.stringToInt("", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_046() throws Exception {
        // Combination: str=" ", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.stringToInt(" ", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_047() throws Exception {
        // Combination: str="a", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.stringToInt("a", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_048() throws Exception {
        // Combination: str="test123", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.stringToInt("test123", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_049() throws Exception {
        // Combination: str="!@#", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.stringToInt("!@#", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_050() throws Exception {
        // Combination: str="0", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.stringToInt("0", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_051() throws Exception {
        // Combination: str="-1", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.stringToInt("-1", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_052() throws Exception {
        // Combination: str="1.5", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.stringToInt("1.5", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_053() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.stringToInt("9223372036854775807", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_054() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.stringToInt("9223372036854775808", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_stringToInt_pairwise_055() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.stringToInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_056() throws Exception {
        // Combination: a=0L, b=0L, c=0L
        Object actual = NumberUtils.minimum(0L, 0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_057() throws Exception {
        // Combination: a=0L, b=1L, c=1L
        Object actual = NumberUtils.minimum(0L, 1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_058() throws Exception {
        // Combination: a=0L, b=-1L, c=-1L
        Object actual = NumberUtils.minimum(0L, -1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_059() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE, c=Long.MAX_VALUE
        Object actual = NumberUtils.minimum(0L, Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_060() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE, c=Long.MIN_VALUE
        Object actual = NumberUtils.minimum(0L, Long.MIN_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_061() throws Exception {
        // Combination: a=1L, b=0L, c=1L
        Object actual = NumberUtils.minimum(1L, 0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_062() throws Exception {
        // Combination: a=1L, b=1L, c=0L
        Object actual = NumberUtils.minimum(1L, 1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_063() throws Exception {
        // Combination: a=1L, b=-1L, c=Long.MAX_VALUE
        Object actual = NumberUtils.minimum(1L, -1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_064() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE, c=-1L
        Object actual = NumberUtils.minimum(1L, Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_065() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE, c=0L
        Object actual = NumberUtils.minimum(1L, Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_066() throws Exception {
        // Combination: a=-1L, b=0L, c=-1L
        Object actual = NumberUtils.minimum(-1L, 0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_067() throws Exception {
        // Combination: a=-1L, b=1L, c=Long.MAX_VALUE
        Object actual = NumberUtils.minimum(-1L, 1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_068() throws Exception {
        // Combination: a=-1L, b=-1L, c=0L
        Object actual = NumberUtils.minimum(-1L, -1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_069() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE, c=1L
        Object actual = NumberUtils.minimum(-1L, Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_070() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE, c=1L
        Object actual = NumberUtils.minimum(-1L, Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_071() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L, c=Long.MAX_VALUE
        Object actual = NumberUtils.minimum(Long.MAX_VALUE, 0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_072() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L, c=-1L
        Object actual = NumberUtils.minimum(Long.MAX_VALUE, 1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_073() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L, c=1L
        Object actual = NumberUtils.minimum(Long.MAX_VALUE, -1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_074() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE, c=0L
        Object actual = NumberUtils.minimum(Long.MAX_VALUE, Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_075() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE, c=-1L
        Object actual = NumberUtils.minimum(Long.MAX_VALUE, Long.MIN_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_076() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L, c=Long.MIN_VALUE
        Object actual = NumberUtils.minimum(Long.MIN_VALUE, 0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_077() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L, c=0L
        Object actual = NumberUtils.minimum(Long.MIN_VALUE, 1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_078() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L, c=1L
        Object actual = NumberUtils.minimum(Long.MIN_VALUE, -1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_079() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE, c=-1L
        Object actual = NumberUtils.minimum(Long.MIN_VALUE, Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_080() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE, c=Long.MAX_VALUE
        Object actual = NumberUtils.minimum(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_081() throws Exception {
        // Combination: a=1L, b=1L, c=Long.MIN_VALUE
        Object actual = NumberUtils.minimum(1L, 1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_082() throws Exception {
        // Combination: a=-1L, b=-1L, c=Long.MIN_VALUE
        Object actual = NumberUtils.minimum(-1L, -1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_083() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE, c=Long.MIN_VALUE
        Object actual = NumberUtils.minimum(Long.MAX_VALUE, Long.MAX_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_084() throws Exception {
        // Combination: a=0, b=0, c=0
        Object actual = NumberUtils.minimum(0, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_085() throws Exception {
        // Combination: a=0, b=1, c=1
        Object actual = NumberUtils.minimum(0, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_086() throws Exception {
        // Combination: a=0, b=-1, c=-1
        Object actual = NumberUtils.minimum(0, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_087() throws Exception {
        // Combination: a=0, b=Integer.MAX_VALUE, c=Integer.MAX_VALUE
        Object actual = NumberUtils.minimum(0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_088() throws Exception {
        // Combination: a=0, b=Integer.MIN_VALUE, c=Integer.MIN_VALUE
        Object actual = NumberUtils.minimum(0, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_089() throws Exception {
        // Combination: a=1, b=0, c=1
        Object actual = NumberUtils.minimum(1, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_090() throws Exception {
        // Combination: a=1, b=1, c=0
        Object actual = NumberUtils.minimum(1, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_091() throws Exception {
        // Combination: a=1, b=-1, c=Integer.MAX_VALUE
        Object actual = NumberUtils.minimum(1, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_092() throws Exception {
        // Combination: a=1, b=Integer.MAX_VALUE, c=-1
        Object actual = NumberUtils.minimum(1, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_093() throws Exception {
        // Combination: a=1, b=Integer.MIN_VALUE, c=0
        Object actual = NumberUtils.minimum(1, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_094() throws Exception {
        // Combination: a=-1, b=0, c=-1
        Object actual = NumberUtils.minimum(-1, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_095() throws Exception {
        // Combination: a=-1, b=1, c=Integer.MAX_VALUE
        Object actual = NumberUtils.minimum(-1, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_096() throws Exception {
        // Combination: a=-1, b=-1, c=0
        Object actual = NumberUtils.minimum(-1, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_097() throws Exception {
        // Combination: a=-1, b=Integer.MAX_VALUE, c=1
        Object actual = NumberUtils.minimum(-1, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_098() throws Exception {
        // Combination: a=-1, b=Integer.MIN_VALUE, c=1
        Object actual = NumberUtils.minimum(-1, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_099() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=0, c=Integer.MAX_VALUE
        Object actual = NumberUtils.minimum(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_100() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=1, c=-1
        Object actual = NumberUtils.minimum(Integer.MAX_VALUE, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_101() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=-1, c=1
        Object actual = NumberUtils.minimum(Integer.MAX_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_102() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE, c=0
        Object actual = NumberUtils.minimum(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_103() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MIN_VALUE, c=-1
        Object actual = NumberUtils.minimum(Integer.MAX_VALUE, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_104() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=0, c=Integer.MIN_VALUE
        Object actual = NumberUtils.minimum(Integer.MIN_VALUE, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_105() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=1, c=0
        Object actual = NumberUtils.minimum(Integer.MIN_VALUE, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_106() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=-1, c=1
        Object actual = NumberUtils.minimum(Integer.MIN_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_107() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MAX_VALUE, c=-1
        Object actual = NumberUtils.minimum(Integer.MIN_VALUE, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_108() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MIN_VALUE, c=Integer.MAX_VALUE
        Object actual = NumberUtils.minimum(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_109() throws Exception {
        // Combination: a=1, b=1, c=Integer.MIN_VALUE
        Object actual = NumberUtils.minimum(1, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_110() throws Exception {
        // Combination: a=-1, b=-1, c=Integer.MIN_VALUE
        Object actual = NumberUtils.minimum(-1, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_minimum_pairwise_111() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE, c=Integer.MIN_VALUE
        Object actual = NumberUtils.minimum(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_112() throws Exception {
        // Combination: a=0L, b=0L, c=0L
        Object actual = NumberUtils.maximum(0L, 0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_113() throws Exception {
        // Combination: a=0L, b=1L, c=1L
        Object actual = NumberUtils.maximum(0L, 1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_114() throws Exception {
        // Combination: a=0L, b=-1L, c=-1L
        Object actual = NumberUtils.maximum(0L, -1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_115() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE, c=Long.MAX_VALUE
        Object actual = NumberUtils.maximum(0L, Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_116() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE, c=Long.MIN_VALUE
        Object actual = NumberUtils.maximum(0L, Long.MIN_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_117() throws Exception {
        // Combination: a=1L, b=0L, c=1L
        Object actual = NumberUtils.maximum(1L, 0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_118() throws Exception {
        // Combination: a=1L, b=1L, c=0L
        Object actual = NumberUtils.maximum(1L, 1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_119() throws Exception {
        // Combination: a=1L, b=-1L, c=Long.MAX_VALUE
        Object actual = NumberUtils.maximum(1L, -1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_120() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE, c=-1L
        Object actual = NumberUtils.maximum(1L, Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_121() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE, c=0L
        Object actual = NumberUtils.maximum(1L, Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_122() throws Exception {
        // Combination: a=-1L, b=0L, c=-1L
        Object actual = NumberUtils.maximum(-1L, 0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_123() throws Exception {
        // Combination: a=-1L, b=1L, c=Long.MAX_VALUE
        Object actual = NumberUtils.maximum(-1L, 1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_124() throws Exception {
        // Combination: a=-1L, b=-1L, c=0L
        Object actual = NumberUtils.maximum(-1L, -1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_125() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE, c=1L
        Object actual = NumberUtils.maximum(-1L, Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_126() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE, c=1L
        Object actual = NumberUtils.maximum(-1L, Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_127() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L, c=Long.MAX_VALUE
        Object actual = NumberUtils.maximum(Long.MAX_VALUE, 0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_128() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L, c=-1L
        Object actual = NumberUtils.maximum(Long.MAX_VALUE, 1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_129() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L, c=1L
        Object actual = NumberUtils.maximum(Long.MAX_VALUE, -1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_130() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE, c=0L
        Object actual = NumberUtils.maximum(Long.MAX_VALUE, Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_131() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE, c=-1L
        Object actual = NumberUtils.maximum(Long.MAX_VALUE, Long.MIN_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_132() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L, c=Long.MIN_VALUE
        Object actual = NumberUtils.maximum(Long.MIN_VALUE, 0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_133() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L, c=0L
        Object actual = NumberUtils.maximum(Long.MIN_VALUE, 1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_134() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L, c=1L
        Object actual = NumberUtils.maximum(Long.MIN_VALUE, -1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_135() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE, c=-1L
        Object actual = NumberUtils.maximum(Long.MIN_VALUE, Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_136() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE, c=Long.MAX_VALUE
        Object actual = NumberUtils.maximum(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_137() throws Exception {
        // Combination: a=1L, b=1L, c=Long.MIN_VALUE
        Object actual = NumberUtils.maximum(1L, 1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_138() throws Exception {
        // Combination: a=-1L, b=-1L, c=Long.MIN_VALUE
        Object actual = NumberUtils.maximum(-1L, -1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_139() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE, c=Long.MIN_VALUE
        Object actual = NumberUtils.maximum(Long.MAX_VALUE, Long.MAX_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_140() throws Exception {
        // Combination: a=0, b=0, c=0
        Object actual = NumberUtils.maximum(0, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_141() throws Exception {
        // Combination: a=0, b=1, c=1
        Object actual = NumberUtils.maximum(0, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_142() throws Exception {
        // Combination: a=0, b=-1, c=-1
        Object actual = NumberUtils.maximum(0, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_143() throws Exception {
        // Combination: a=0, b=Integer.MAX_VALUE, c=Integer.MAX_VALUE
        Object actual = NumberUtils.maximum(0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_144() throws Exception {
        // Combination: a=0, b=Integer.MIN_VALUE, c=Integer.MIN_VALUE
        Object actual = NumberUtils.maximum(0, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_145() throws Exception {
        // Combination: a=1, b=0, c=1
        Object actual = NumberUtils.maximum(1, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_146() throws Exception {
        // Combination: a=1, b=1, c=0
        Object actual = NumberUtils.maximum(1, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_147() throws Exception {
        // Combination: a=1, b=-1, c=Integer.MAX_VALUE
        Object actual = NumberUtils.maximum(1, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_148() throws Exception {
        // Combination: a=1, b=Integer.MAX_VALUE, c=-1
        Object actual = NumberUtils.maximum(1, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_149() throws Exception {
        // Combination: a=1, b=Integer.MIN_VALUE, c=0
        Object actual = NumberUtils.maximum(1, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_150() throws Exception {
        // Combination: a=-1, b=0, c=-1
        Object actual = NumberUtils.maximum(-1, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_151() throws Exception {
        // Combination: a=-1, b=1, c=Integer.MAX_VALUE
        Object actual = NumberUtils.maximum(-1, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_152() throws Exception {
        // Combination: a=-1, b=-1, c=0
        Object actual = NumberUtils.maximum(-1, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_153() throws Exception {
        // Combination: a=-1, b=Integer.MAX_VALUE, c=1
        Object actual = NumberUtils.maximum(-1, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_154() throws Exception {
        // Combination: a=-1, b=Integer.MIN_VALUE, c=1
        Object actual = NumberUtils.maximum(-1, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_155() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=0, c=Integer.MAX_VALUE
        Object actual = NumberUtils.maximum(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_156() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=1, c=-1
        Object actual = NumberUtils.maximum(Integer.MAX_VALUE, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_157() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=-1, c=1
        Object actual = NumberUtils.maximum(Integer.MAX_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_158() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE, c=0
        Object actual = NumberUtils.maximum(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_159() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MIN_VALUE, c=-1
        Object actual = NumberUtils.maximum(Integer.MAX_VALUE, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_160() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=0, c=Integer.MIN_VALUE
        Object actual = NumberUtils.maximum(Integer.MIN_VALUE, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_161() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=1, c=0
        Object actual = NumberUtils.maximum(Integer.MIN_VALUE, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_162() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=-1, c=1
        Object actual = NumberUtils.maximum(Integer.MIN_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_163() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MAX_VALUE, c=-1
        Object actual = NumberUtils.maximum(Integer.MIN_VALUE, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_164() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MIN_VALUE, c=Integer.MAX_VALUE
        Object actual = NumberUtils.maximum(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_165() throws Exception {
        // Combination: a=1, b=1, c=Integer.MIN_VALUE
        Object actual = NumberUtils.maximum(1, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_166() throws Exception {
        // Combination: a=-1, b=-1, c=Integer.MIN_VALUE
        Object actual = NumberUtils.maximum(-1, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_maximum_pairwise_167() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE, c=Integer.MIN_VALUE
        Object actual = NumberUtils.maximum(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_168() throws Exception {
        // Combination: lhs=0.0d, rhs=0.0d
        Object actual = NumberUtils.compare(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_169() throws Exception {
        // Combination: lhs=0.0d, rhs=1.0d
        Object actual = NumberUtils.compare(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_170() throws Exception {
        // Combination: lhs=0.0d, rhs=-1.0d
        Object actual = NumberUtils.compare(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_171() throws Exception {
        // Combination: lhs=0.0d, rhs=Double.NaN
        Object actual = NumberUtils.compare(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_172() throws Exception {
        // Combination: lhs=0.0d, rhs=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_173() throws Exception {
        // Combination: lhs=1.0d, rhs=0.0d
        Object actual = NumberUtils.compare(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_174() throws Exception {
        // Combination: lhs=1.0d, rhs=1.0d
        Object actual = NumberUtils.compare(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_175() throws Exception {
        // Combination: lhs=1.0d, rhs=-1.0d
        Object actual = NumberUtils.compare(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_176() throws Exception {
        // Combination: lhs=1.0d, rhs=Double.NaN
        Object actual = NumberUtils.compare(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_177() throws Exception {
        // Combination: lhs=1.0d, rhs=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_178() throws Exception {
        // Combination: lhs=-1.0d, rhs=0.0d
        Object actual = NumberUtils.compare(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_179() throws Exception {
        // Combination: lhs=-1.0d, rhs=1.0d
        Object actual = NumberUtils.compare(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_180() throws Exception {
        // Combination: lhs=-1.0d, rhs=-1.0d
        Object actual = NumberUtils.compare(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_181() throws Exception {
        // Combination: lhs=-1.0d, rhs=Double.NaN
        Object actual = NumberUtils.compare(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_182() throws Exception {
        // Combination: lhs=-1.0d, rhs=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_183() throws Exception {
        // Combination: lhs=Double.NaN, rhs=0.0d
        Object actual = NumberUtils.compare(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_184() throws Exception {
        // Combination: lhs=Double.NaN, rhs=1.0d
        Object actual = NumberUtils.compare(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_185() throws Exception {
        // Combination: lhs=Double.NaN, rhs=-1.0d
        Object actual = NumberUtils.compare(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_186() throws Exception {
        // Combination: lhs=Double.NaN, rhs=Double.NaN
        Object actual = NumberUtils.compare(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_187() throws Exception {
        // Combination: lhs=Double.NaN, rhs=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_188() throws Exception {
        // Combination: lhs=Double.POSITIVE_INFINITY, rhs=0.0d
        Object actual = NumberUtils.compare(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_189() throws Exception {
        // Combination: lhs=Double.POSITIVE_INFINITY, rhs=1.0d
        Object actual = NumberUtils.compare(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_190() throws Exception {
        // Combination: lhs=Double.POSITIVE_INFINITY, rhs=-1.0d
        Object actual = NumberUtils.compare(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_191() throws Exception {
        // Combination: lhs=Double.POSITIVE_INFINITY, rhs=Double.NaN
        Object actual = NumberUtils.compare(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_192() throws Exception {
        // Combination: lhs=Double.POSITIVE_INFINITY, rhs=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_193() throws Exception {
        // Combination: lhs=0.0f, rhs=0.0f
        Object actual = NumberUtils.compare(0.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_194() throws Exception {
        // Combination: lhs=0.0f, rhs=1.0f
        Object actual = NumberUtils.compare(0.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_195() throws Exception {
        // Combination: lhs=0.0f, rhs=-1.0f
        Object actual = NumberUtils.compare(0.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_196() throws Exception {
        // Combination: lhs=0.0f, rhs=Float.NaN
        Object actual = NumberUtils.compare(0.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_197() throws Exception {
        // Combination: lhs=0.0f, rhs=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(0.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_198() throws Exception {
        // Combination: lhs=1.0f, rhs=0.0f
        Object actual = NumberUtils.compare(1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_199() throws Exception {
        // Combination: lhs=1.0f, rhs=1.0f
        Object actual = NumberUtils.compare(1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_200() throws Exception {
        // Combination: lhs=1.0f, rhs=-1.0f
        Object actual = NumberUtils.compare(1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_201() throws Exception {
        // Combination: lhs=1.0f, rhs=Float.NaN
        Object actual = NumberUtils.compare(1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_202() throws Exception {
        // Combination: lhs=1.0f, rhs=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_203() throws Exception {
        // Combination: lhs=-1.0f, rhs=0.0f
        Object actual = NumberUtils.compare(-1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_204() throws Exception {
        // Combination: lhs=-1.0f, rhs=1.0f
        Object actual = NumberUtils.compare(-1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_205() throws Exception {
        // Combination: lhs=-1.0f, rhs=-1.0f
        Object actual = NumberUtils.compare(-1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_206() throws Exception {
        // Combination: lhs=-1.0f, rhs=Float.NaN
        Object actual = NumberUtils.compare(-1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_207() throws Exception {
        // Combination: lhs=-1.0f, rhs=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(-1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_208() throws Exception {
        // Combination: lhs=Float.NaN, rhs=0.0f
        Object actual = NumberUtils.compare(Float.NaN, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_209() throws Exception {
        // Combination: lhs=Float.NaN, rhs=1.0f
        Object actual = NumberUtils.compare(Float.NaN, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_210() throws Exception {
        // Combination: lhs=Float.NaN, rhs=-1.0f
        Object actual = NumberUtils.compare(Float.NaN, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_211() throws Exception {
        // Combination: lhs=Float.NaN, rhs=Float.NaN
        Object actual = NumberUtils.compare(Float.NaN, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_212() throws Exception {
        // Combination: lhs=Float.NaN, rhs=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(Float.NaN, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_213() throws Exception {
        // Combination: lhs=Float.POSITIVE_INFINITY, rhs=0.0f
        Object actual = NumberUtils.compare(Float.POSITIVE_INFINITY, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_214() throws Exception {
        // Combination: lhs=Float.POSITIVE_INFINITY, rhs=1.0f
        Object actual = NumberUtils.compare(Float.POSITIVE_INFINITY, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_215() throws Exception {
        // Combination: lhs=Float.POSITIVE_INFINITY, rhs=-1.0f
        Object actual = NumberUtils.compare(Float.POSITIVE_INFINITY, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_216() throws Exception {
        // Combination: lhs=Float.POSITIVE_INFINITY, rhs=Float.NaN
        Object actual = NumberUtils.compare(Float.POSITIVE_INFINITY, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_217() throws Exception {
        // Combination: lhs=Float.POSITIVE_INFINITY, rhs=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

}
