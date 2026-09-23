package org.apache.commons.lang.math;

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
    public void test_toInt_pairwise_056() throws Exception {
        // Combination: str="", defaultValue=0
        Object actual = NumberUtils.toInt("", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_057() throws Exception {
        // Combination: str=" ", defaultValue=0
        Object actual = NumberUtils.toInt(" ", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_058() throws Exception {
        // Combination: str="a", defaultValue=0
        Object actual = NumberUtils.toInt("a", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_059() throws Exception {
        // Combination: str="test123", defaultValue=0
        Object actual = NumberUtils.toInt("test123", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_060() throws Exception {
        // Combination: str="!@#", defaultValue=0
        Object actual = NumberUtils.toInt("!@#", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_061() throws Exception {
        // Combination: str="0", defaultValue=0
        Object actual = NumberUtils.toInt("0", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_062() throws Exception {
        // Combination: str="-1", defaultValue=0
        Object actual = NumberUtils.toInt("-1", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_063() throws Exception {
        // Combination: str="1.5", defaultValue=0
        Object actual = NumberUtils.toInt("1.5", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_064() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=0
        Object actual = NumberUtils.toInt("9223372036854775807", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_065() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=0
        Object actual = NumberUtils.toInt("9223372036854775808", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_066() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0
        Object actual = NumberUtils.toInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_067() throws Exception {
        // Combination: str="", defaultValue=1
        Object actual = NumberUtils.toInt("", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_068() throws Exception {
        // Combination: str=" ", defaultValue=1
        Object actual = NumberUtils.toInt(" ", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_069() throws Exception {
        // Combination: str="a", defaultValue=1
        Object actual = NumberUtils.toInt("a", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_070() throws Exception {
        // Combination: str="test123", defaultValue=1
        Object actual = NumberUtils.toInt("test123", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_071() throws Exception {
        // Combination: str="!@#", defaultValue=1
        Object actual = NumberUtils.toInt("!@#", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_072() throws Exception {
        // Combination: str="0", defaultValue=1
        Object actual = NumberUtils.toInt("0", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_073() throws Exception {
        // Combination: str="-1", defaultValue=1
        Object actual = NumberUtils.toInt("-1", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_074() throws Exception {
        // Combination: str="1.5", defaultValue=1
        Object actual = NumberUtils.toInt("1.5", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_075() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=1
        Object actual = NumberUtils.toInt("9223372036854775807", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_076() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=1
        Object actual = NumberUtils.toInt("9223372036854775808", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_077() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1
        Object actual = NumberUtils.toInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_078() throws Exception {
        // Combination: str="", defaultValue=-1
        Object actual = NumberUtils.toInt("", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_079() throws Exception {
        // Combination: str=" ", defaultValue=-1
        Object actual = NumberUtils.toInt(" ", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_080() throws Exception {
        // Combination: str="a", defaultValue=-1
        Object actual = NumberUtils.toInt("a", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_081() throws Exception {
        // Combination: str="test123", defaultValue=-1
        Object actual = NumberUtils.toInt("test123", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_082() throws Exception {
        // Combination: str="!@#", defaultValue=-1
        Object actual = NumberUtils.toInt("!@#", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_083() throws Exception {
        // Combination: str="0", defaultValue=-1
        Object actual = NumberUtils.toInt("0", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_084() throws Exception {
        // Combination: str="-1", defaultValue=-1
        Object actual = NumberUtils.toInt("-1", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_085() throws Exception {
        // Combination: str="1.5", defaultValue=-1
        Object actual = NumberUtils.toInt("1.5", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_086() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=-1
        Object actual = NumberUtils.toInt("9223372036854775807", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_087() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=-1
        Object actual = NumberUtils.toInt("9223372036854775808", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_088() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1
        Object actual = NumberUtils.toInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_089() throws Exception {
        // Combination: str="", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.toInt("", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_090() throws Exception {
        // Combination: str=" ", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.toInt(" ", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_091() throws Exception {
        // Combination: str="a", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.toInt("a", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_092() throws Exception {
        // Combination: str="test123", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.toInt("test123", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_093() throws Exception {
        // Combination: str="!@#", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.toInt("!@#", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_094() throws Exception {
        // Combination: str="0", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.toInt("0", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_095() throws Exception {
        // Combination: str="-1", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.toInt("-1", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_096() throws Exception {
        // Combination: str="1.5", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.toInt("1.5", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_097() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.toInt("9223372036854775807", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_098() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.toInt("9223372036854775808", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_099() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Integer.MAX_VALUE
        Object actual = NumberUtils.toInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_100() throws Exception {
        // Combination: str="", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.toInt("", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_101() throws Exception {
        // Combination: str=" ", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.toInt(" ", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_102() throws Exception {
        // Combination: str="a", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.toInt("a", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_103() throws Exception {
        // Combination: str="test123", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.toInt("test123", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_104() throws Exception {
        // Combination: str="!@#", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.toInt("!@#", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_105() throws Exception {
        // Combination: str="0", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.toInt("0", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_106() throws Exception {
        // Combination: str="-1", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.toInt("-1", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_107() throws Exception {
        // Combination: str="1.5", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.toInt("1.5", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_108() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.toInt("9223372036854775807", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_109() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.toInt("9223372036854775808", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInt_pairwise_110() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Integer.MIN_VALUE
        Object actual = NumberUtils.toInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_111() throws Exception {
        // Combination: str="", defaultValue=0L
        Object actual = NumberUtils.toLong("", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_112() throws Exception {
        // Combination: str=" ", defaultValue=0L
        Object actual = NumberUtils.toLong(" ", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_113() throws Exception {
        // Combination: str="a", defaultValue=0L
        Object actual = NumberUtils.toLong("a", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_114() throws Exception {
        // Combination: str="test123", defaultValue=0L
        Object actual = NumberUtils.toLong("test123", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_115() throws Exception {
        // Combination: str="!@#", defaultValue=0L
        Object actual = NumberUtils.toLong("!@#", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_116() throws Exception {
        // Combination: str="0", defaultValue=0L
        Object actual = NumberUtils.toLong("0", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_117() throws Exception {
        // Combination: str="-1", defaultValue=0L
        Object actual = NumberUtils.toLong("-1", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_118() throws Exception {
        // Combination: str="1.5", defaultValue=0L
        Object actual = NumberUtils.toLong("1.5", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_119() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=0L
        Object actual = NumberUtils.toLong("9223372036854775807", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_120() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=0L
        Object actual = NumberUtils.toLong("9223372036854775808", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_121() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0L
        Object actual = NumberUtils.toLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_122() throws Exception {
        // Combination: str="", defaultValue=1L
        Object actual = NumberUtils.toLong("", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_123() throws Exception {
        // Combination: str=" ", defaultValue=1L
        Object actual = NumberUtils.toLong(" ", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_124() throws Exception {
        // Combination: str="a", defaultValue=1L
        Object actual = NumberUtils.toLong("a", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_125() throws Exception {
        // Combination: str="test123", defaultValue=1L
        Object actual = NumberUtils.toLong("test123", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_126() throws Exception {
        // Combination: str="!@#", defaultValue=1L
        Object actual = NumberUtils.toLong("!@#", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_127() throws Exception {
        // Combination: str="0", defaultValue=1L
        Object actual = NumberUtils.toLong("0", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_128() throws Exception {
        // Combination: str="-1", defaultValue=1L
        Object actual = NumberUtils.toLong("-1", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_129() throws Exception {
        // Combination: str="1.5", defaultValue=1L
        Object actual = NumberUtils.toLong("1.5", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_130() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=1L
        Object actual = NumberUtils.toLong("9223372036854775807", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_131() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=1L
        Object actual = NumberUtils.toLong("9223372036854775808", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_132() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1L
        Object actual = NumberUtils.toLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_133() throws Exception {
        // Combination: str="", defaultValue=-1L
        Object actual = NumberUtils.toLong("", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_134() throws Exception {
        // Combination: str=" ", defaultValue=-1L
        Object actual = NumberUtils.toLong(" ", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_135() throws Exception {
        // Combination: str="a", defaultValue=-1L
        Object actual = NumberUtils.toLong("a", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_136() throws Exception {
        // Combination: str="test123", defaultValue=-1L
        Object actual = NumberUtils.toLong("test123", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_137() throws Exception {
        // Combination: str="!@#", defaultValue=-1L
        Object actual = NumberUtils.toLong("!@#", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_138() throws Exception {
        // Combination: str="0", defaultValue=-1L
        Object actual = NumberUtils.toLong("0", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_139() throws Exception {
        // Combination: str="-1", defaultValue=-1L
        Object actual = NumberUtils.toLong("-1", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_140() throws Exception {
        // Combination: str="1.5", defaultValue=-1L
        Object actual = NumberUtils.toLong("1.5", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_141() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=-1L
        Object actual = NumberUtils.toLong("9223372036854775807", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_142() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=-1L
        Object actual = NumberUtils.toLong("9223372036854775808", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_143() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1L
        Object actual = NumberUtils.toLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_144() throws Exception {
        // Combination: str="", defaultValue=Long.MAX_VALUE
        Object actual = NumberUtils.toLong("", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_145() throws Exception {
        // Combination: str=" ", defaultValue=Long.MAX_VALUE
        Object actual = NumberUtils.toLong(" ", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_146() throws Exception {
        // Combination: str="a", defaultValue=Long.MAX_VALUE
        Object actual = NumberUtils.toLong("a", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_147() throws Exception {
        // Combination: str="test123", defaultValue=Long.MAX_VALUE
        Object actual = NumberUtils.toLong("test123", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_148() throws Exception {
        // Combination: str="!@#", defaultValue=Long.MAX_VALUE
        Object actual = NumberUtils.toLong("!@#", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_149() throws Exception {
        // Combination: str="0", defaultValue=Long.MAX_VALUE
        Object actual = NumberUtils.toLong("0", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_150() throws Exception {
        // Combination: str="-1", defaultValue=Long.MAX_VALUE
        Object actual = NumberUtils.toLong("-1", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_151() throws Exception {
        // Combination: str="1.5", defaultValue=Long.MAX_VALUE
        Object actual = NumberUtils.toLong("1.5", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_152() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=Long.MAX_VALUE
        Object actual = NumberUtils.toLong("9223372036854775807", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_153() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=Long.MAX_VALUE
        Object actual = NumberUtils.toLong("9223372036854775808", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_154() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Long.MAX_VALUE
        Object actual = NumberUtils.toLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_155() throws Exception {
        // Combination: str="", defaultValue=Long.MIN_VALUE
        Object actual = NumberUtils.toLong("", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_156() throws Exception {
        // Combination: str=" ", defaultValue=Long.MIN_VALUE
        Object actual = NumberUtils.toLong(" ", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_157() throws Exception {
        // Combination: str="a", defaultValue=Long.MIN_VALUE
        Object actual = NumberUtils.toLong("a", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_158() throws Exception {
        // Combination: str="test123", defaultValue=Long.MIN_VALUE
        Object actual = NumberUtils.toLong("test123", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_159() throws Exception {
        // Combination: str="!@#", defaultValue=Long.MIN_VALUE
        Object actual = NumberUtils.toLong("!@#", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_160() throws Exception {
        // Combination: str="0", defaultValue=Long.MIN_VALUE
        Object actual = NumberUtils.toLong("0", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_161() throws Exception {
        // Combination: str="-1", defaultValue=Long.MIN_VALUE
        Object actual = NumberUtils.toLong("-1", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_162() throws Exception {
        // Combination: str="1.5", defaultValue=Long.MIN_VALUE
        Object actual = NumberUtils.toLong("1.5", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_163() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=Long.MIN_VALUE
        Object actual = NumberUtils.toLong("9223372036854775807", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_164() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=Long.MIN_VALUE
        Object actual = NumberUtils.toLong("9223372036854775808", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toLong_pairwise_165() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Long.MIN_VALUE
        Object actual = NumberUtils.toLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_166() throws Exception {
        // Combination: str="", defaultValue=0.0f
        Object actual = NumberUtils.toFloat("", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_167() throws Exception {
        // Combination: str=" ", defaultValue=0.0f
        Object actual = NumberUtils.toFloat(" ", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_168() throws Exception {
        // Combination: str="a", defaultValue=0.0f
        Object actual = NumberUtils.toFloat("a", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_169() throws Exception {
        // Combination: str="test123", defaultValue=0.0f
        Object actual = NumberUtils.toFloat("test123", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_170() throws Exception {
        // Combination: str="!@#", defaultValue=0.0f
        Object actual = NumberUtils.toFloat("!@#", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_171() throws Exception {
        // Combination: str="0", defaultValue=0.0f
        Object actual = NumberUtils.toFloat("0", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_172() throws Exception {
        // Combination: str="-1", defaultValue=0.0f
        Object actual = NumberUtils.toFloat("-1", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_173() throws Exception {
        // Combination: str="1.5", defaultValue=0.0f
        Object actual = NumberUtils.toFloat("1.5", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_174() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=0.0f
        Object actual = NumberUtils.toFloat("9223372036854775807", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("9.223372E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_175() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=0.0f
        Object actual = NumberUtils.toFloat("9223372036854775808", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("9.223372E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_176() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0.0f
        Object actual = NumberUtils.toFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_177() throws Exception {
        // Combination: str="", defaultValue=1.0f
        Object actual = NumberUtils.toFloat("", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_178() throws Exception {
        // Combination: str=" ", defaultValue=1.0f
        Object actual = NumberUtils.toFloat(" ", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_179() throws Exception {
        // Combination: str="a", defaultValue=1.0f
        Object actual = NumberUtils.toFloat("a", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_180() throws Exception {
        // Combination: str="test123", defaultValue=1.0f
        Object actual = NumberUtils.toFloat("test123", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_181() throws Exception {
        // Combination: str="!@#", defaultValue=1.0f
        Object actual = NumberUtils.toFloat("!@#", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_182() throws Exception {
        // Combination: str="0", defaultValue=1.0f
        Object actual = NumberUtils.toFloat("0", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_183() throws Exception {
        // Combination: str="-1", defaultValue=1.0f
        Object actual = NumberUtils.toFloat("-1", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_184() throws Exception {
        // Combination: str="1.5", defaultValue=1.0f
        Object actual = NumberUtils.toFloat("1.5", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_185() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=1.0f
        Object actual = NumberUtils.toFloat("9223372036854775807", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("9.223372E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_186() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=1.0f
        Object actual = NumberUtils.toFloat("9223372036854775808", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("9.223372E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_187() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1.0f
        Object actual = NumberUtils.toFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_188() throws Exception {
        // Combination: str="", defaultValue=-1.0f
        Object actual = NumberUtils.toFloat("", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_189() throws Exception {
        // Combination: str=" ", defaultValue=-1.0f
        Object actual = NumberUtils.toFloat(" ", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_190() throws Exception {
        // Combination: str="a", defaultValue=-1.0f
        Object actual = NumberUtils.toFloat("a", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_191() throws Exception {
        // Combination: str="test123", defaultValue=-1.0f
        Object actual = NumberUtils.toFloat("test123", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_192() throws Exception {
        // Combination: str="!@#", defaultValue=-1.0f
        Object actual = NumberUtils.toFloat("!@#", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_193() throws Exception {
        // Combination: str="0", defaultValue=-1.0f
        Object actual = NumberUtils.toFloat("0", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_194() throws Exception {
        // Combination: str="-1", defaultValue=-1.0f
        Object actual = NumberUtils.toFloat("-1", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_195() throws Exception {
        // Combination: str="1.5", defaultValue=-1.0f
        Object actual = NumberUtils.toFloat("1.5", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_196() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=-1.0f
        Object actual = NumberUtils.toFloat("9223372036854775807", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("9.223372E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_197() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=-1.0f
        Object actual = NumberUtils.toFloat("9223372036854775808", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("9.223372E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_198() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1.0f
        Object actual = NumberUtils.toFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_199() throws Exception {
        // Combination: str="", defaultValue=Float.NaN
        Object actual = NumberUtils.toFloat("", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_200() throws Exception {
        // Combination: str=" ", defaultValue=Float.NaN
        Object actual = NumberUtils.toFloat(" ", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_201() throws Exception {
        // Combination: str="a", defaultValue=Float.NaN
        Object actual = NumberUtils.toFloat("a", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_202() throws Exception {
        // Combination: str="test123", defaultValue=Float.NaN
        Object actual = NumberUtils.toFloat("test123", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_203() throws Exception {
        // Combination: str="!@#", defaultValue=Float.NaN
        Object actual = NumberUtils.toFloat("!@#", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_204() throws Exception {
        // Combination: str="0", defaultValue=Float.NaN
        Object actual = NumberUtils.toFloat("0", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_205() throws Exception {
        // Combination: str="-1", defaultValue=Float.NaN
        Object actual = NumberUtils.toFloat("-1", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_206() throws Exception {
        // Combination: str="1.5", defaultValue=Float.NaN
        Object actual = NumberUtils.toFloat("1.5", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_207() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=Float.NaN
        Object actual = NumberUtils.toFloat("9223372036854775807", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("9.223372E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_208() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=Float.NaN
        Object actual = NumberUtils.toFloat("9223372036854775808", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("9.223372E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_209() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Float.NaN
        Object actual = NumberUtils.toFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_210() throws Exception {
        // Combination: str="", defaultValue=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.toFloat("", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_211() throws Exception {
        // Combination: str=" ", defaultValue=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.toFloat(" ", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_212() throws Exception {
        // Combination: str="a", defaultValue=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.toFloat("a", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_213() throws Exception {
        // Combination: str="test123", defaultValue=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.toFloat("test123", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_214() throws Exception {
        // Combination: str="!@#", defaultValue=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.toFloat("!@#", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_215() throws Exception {
        // Combination: str="0", defaultValue=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.toFloat("0", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_216() throws Exception {
        // Combination: str="-1", defaultValue=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.toFloat("-1", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_217() throws Exception {
        // Combination: str="1.5", defaultValue=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.toFloat("1.5", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_218() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.toFloat("9223372036854775807", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("9.223372E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_219() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.toFloat("9223372036854775808", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("9.223372E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toFloat_pairwise_220() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.toFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_221() throws Exception {
        // Combination: str="", defaultValue=0.0d
        Object actual = NumberUtils.toDouble("", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_222() throws Exception {
        // Combination: str=" ", defaultValue=0.0d
        Object actual = NumberUtils.toDouble(" ", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_223() throws Exception {
        // Combination: str="a", defaultValue=0.0d
        Object actual = NumberUtils.toDouble("a", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_224() throws Exception {
        // Combination: str="test123", defaultValue=0.0d
        Object actual = NumberUtils.toDouble("test123", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_225() throws Exception {
        // Combination: str="!@#", defaultValue=0.0d
        Object actual = NumberUtils.toDouble("!@#", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_226() throws Exception {
        // Combination: str="0", defaultValue=0.0d
        Object actual = NumberUtils.toDouble("0", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_227() throws Exception {
        // Combination: str="-1", defaultValue=0.0d
        Object actual = NumberUtils.toDouble("-1", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_228() throws Exception {
        // Combination: str="1.5", defaultValue=0.0d
        Object actual = NumberUtils.toDouble("1.5", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_229() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=0.0d
        Object actual = NumberUtils.toDouble("9223372036854775807", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_230() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=0.0d
        Object actual = NumberUtils.toDouble("9223372036854775808", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_231() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0.0d
        Object actual = NumberUtils.toDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_232() throws Exception {
        // Combination: str="", defaultValue=1.0d
        Object actual = NumberUtils.toDouble("", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_233() throws Exception {
        // Combination: str=" ", defaultValue=1.0d
        Object actual = NumberUtils.toDouble(" ", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_234() throws Exception {
        // Combination: str="a", defaultValue=1.0d
        Object actual = NumberUtils.toDouble("a", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_235() throws Exception {
        // Combination: str="test123", defaultValue=1.0d
        Object actual = NumberUtils.toDouble("test123", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_236() throws Exception {
        // Combination: str="!@#", defaultValue=1.0d
        Object actual = NumberUtils.toDouble("!@#", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_237() throws Exception {
        // Combination: str="0", defaultValue=1.0d
        Object actual = NumberUtils.toDouble("0", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_238() throws Exception {
        // Combination: str="-1", defaultValue=1.0d
        Object actual = NumberUtils.toDouble("-1", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_239() throws Exception {
        // Combination: str="1.5", defaultValue=1.0d
        Object actual = NumberUtils.toDouble("1.5", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_240() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=1.0d
        Object actual = NumberUtils.toDouble("9223372036854775807", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_241() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=1.0d
        Object actual = NumberUtils.toDouble("9223372036854775808", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_242() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1.0d
        Object actual = NumberUtils.toDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_243() throws Exception {
        // Combination: str="", defaultValue=-1.0d
        Object actual = NumberUtils.toDouble("", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_244() throws Exception {
        // Combination: str=" ", defaultValue=-1.0d
        Object actual = NumberUtils.toDouble(" ", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_245() throws Exception {
        // Combination: str="a", defaultValue=-1.0d
        Object actual = NumberUtils.toDouble("a", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_246() throws Exception {
        // Combination: str="test123", defaultValue=-1.0d
        Object actual = NumberUtils.toDouble("test123", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_247() throws Exception {
        // Combination: str="!@#", defaultValue=-1.0d
        Object actual = NumberUtils.toDouble("!@#", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_248() throws Exception {
        // Combination: str="0", defaultValue=-1.0d
        Object actual = NumberUtils.toDouble("0", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_249() throws Exception {
        // Combination: str="-1", defaultValue=-1.0d
        Object actual = NumberUtils.toDouble("-1", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_250() throws Exception {
        // Combination: str="1.5", defaultValue=-1.0d
        Object actual = NumberUtils.toDouble("1.5", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_251() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=-1.0d
        Object actual = NumberUtils.toDouble("9223372036854775807", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_252() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=-1.0d
        Object actual = NumberUtils.toDouble("9223372036854775808", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_253() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1.0d
        Object actual = NumberUtils.toDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_254() throws Exception {
        // Combination: str="", defaultValue=Double.NaN
        Object actual = NumberUtils.toDouble("", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_255() throws Exception {
        // Combination: str=" ", defaultValue=Double.NaN
        Object actual = NumberUtils.toDouble(" ", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_256() throws Exception {
        // Combination: str="a", defaultValue=Double.NaN
        Object actual = NumberUtils.toDouble("a", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_257() throws Exception {
        // Combination: str="test123", defaultValue=Double.NaN
        Object actual = NumberUtils.toDouble("test123", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_258() throws Exception {
        // Combination: str="!@#", defaultValue=Double.NaN
        Object actual = NumberUtils.toDouble("!@#", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_259() throws Exception {
        // Combination: str="0", defaultValue=Double.NaN
        Object actual = NumberUtils.toDouble("0", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_260() throws Exception {
        // Combination: str="-1", defaultValue=Double.NaN
        Object actual = NumberUtils.toDouble("-1", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_261() throws Exception {
        // Combination: str="1.5", defaultValue=Double.NaN
        Object actual = NumberUtils.toDouble("1.5", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_262() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=Double.NaN
        Object actual = NumberUtils.toDouble("9223372036854775807", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_263() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=Double.NaN
        Object actual = NumberUtils.toDouble("9223372036854775808", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_264() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Double.NaN
        Object actual = NumberUtils.toDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_265() throws Exception {
        // Combination: str="", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.toDouble("", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_266() throws Exception {
        // Combination: str=" ", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.toDouble(" ", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_267() throws Exception {
        // Combination: str="a", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.toDouble("a", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_268() throws Exception {
        // Combination: str="test123", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.toDouble("test123", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_269() throws Exception {
        // Combination: str="!@#", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.toDouble("!@#", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_270() throws Exception {
        // Combination: str="0", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.toDouble("0", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_271() throws Exception {
        // Combination: str="-1", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.toDouble("-1", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_272() throws Exception {
        // Combination: str="1.5", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.toDouble("1.5", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_273() throws Exception {
        // Combination: str="9223372036854775807", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.toDouble("9223372036854775807", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_274() throws Exception {
        // Combination: str="9223372036854775808", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.toDouble("9223372036854775808", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toDouble_pairwise_275() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.toDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_276() throws Exception {
        // Combination: array1=new byte[] {}, array2=new byte[] {}
        Object actual = NumberUtils.equals(new byte[] {}, new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_277() throws Exception {
        // Combination: array1=new byte[] {}, array2=new byte[] {1}
        Object actual = NumberUtils.equals(new byte[] {}, new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_278() throws Exception {
        // Combination: array1=new byte[] {1}, array2=new byte[] {}
        Object actual = NumberUtils.equals(new byte[] {1}, new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_279() throws Exception {
        // Combination: array1=new byte[] {1}, array2=new byte[] {1}
        Object actual = NumberUtils.equals(new byte[] {1}, new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_280() throws Exception {
        // Combination: array1=new short[] {}, array2=new short[] {}
        Object actual = NumberUtils.equals(new short[] {}, new short[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_281() throws Exception {
        // Combination: array1=new short[] {}, array2=new short[] {1}
        Object actual = NumberUtils.equals(new short[] {}, new short[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_282() throws Exception {
        // Combination: array1=new short[] {1}, array2=new short[] {}
        Object actual = NumberUtils.equals(new short[] {1}, new short[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_283() throws Exception {
        // Combination: array1=new short[] {1}, array2=new short[] {1}
        Object actual = NumberUtils.equals(new short[] {1}, new short[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_284() throws Exception {
        // Combination: array1=new int[] {}, array2=new int[] {}
        Object actual = NumberUtils.equals(new int[] {}, new int[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_285() throws Exception {
        // Combination: array1=new int[] {}, array2=new int[] {1}
        Object actual = NumberUtils.equals(new int[] {}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_286() throws Exception {
        // Combination: array1=new int[] {1}, array2=new int[] {}
        Object actual = NumberUtils.equals(new int[] {1}, new int[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_287() throws Exception {
        // Combination: array1=new int[] {1}, array2=new int[] {1}
        Object actual = NumberUtils.equals(new int[] {1}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_288() throws Exception {
        // Combination: array1=new long[] {}, array2=new long[] {}
        Object actual = NumberUtils.equals(new long[] {}, new long[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_289() throws Exception {
        // Combination: array1=new long[] {}, array2=new long[] {1}
        Object actual = NumberUtils.equals(new long[] {}, new long[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_290() throws Exception {
        // Combination: array1=new long[] {1}, array2=new long[] {}
        Object actual = NumberUtils.equals(new long[] {1}, new long[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_291() throws Exception {
        // Combination: array1=new long[] {1}, array2=new long[] {1}
        Object actual = NumberUtils.equals(new long[] {1}, new long[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_292() throws Exception {
        // Combination: array1=new float[] {}, array2=new float[] {}
        Object actual = NumberUtils.equals(new float[] {}, new float[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_293() throws Exception {
        // Combination: array1=new float[] {}, array2=new float[] {1}
        Object actual = NumberUtils.equals(new float[] {}, new float[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_294() throws Exception {
        // Combination: array1=new float[] {1}, array2=new float[] {}
        Object actual = NumberUtils.equals(new float[] {1}, new float[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_295() throws Exception {
        // Combination: array1=new float[] {1}, array2=new float[] {1}
        Object actual = NumberUtils.equals(new float[] {1}, new float[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_296() throws Exception {
        // Combination: array1=new double[] {}, array2=new double[] {}
        Object actual = NumberUtils.equals(new double[] {}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_297() throws Exception {
        // Combination: array1=new double[] {}, array2=new double[] {1}
        Object actual = NumberUtils.equals(new double[] {}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_298() throws Exception {
        // Combination: array1=new double[] {1}, array2=new double[] {}
        Object actual = NumberUtils.equals(new double[] {1}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_299() throws Exception {
        // Combination: array1=new double[] {1}, array2=new double[] {1}
        Object actual = NumberUtils.equals(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_300() throws Exception {
        // Combination: a=0L, b=0L, c=0L
        Object actual = NumberUtils.min(0L, 0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_301() throws Exception {
        // Combination: a=0L, b=1L, c=1L
        Object actual = NumberUtils.min(0L, 1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_302() throws Exception {
        // Combination: a=0L, b=-1L, c=-1L
        Object actual = NumberUtils.min(0L, -1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_303() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE, c=Long.MAX_VALUE
        Object actual = NumberUtils.min(0L, Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_304() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE, c=Long.MIN_VALUE
        Object actual = NumberUtils.min(0L, Long.MIN_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_305() throws Exception {
        // Combination: a=1L, b=0L, c=1L
        Object actual = NumberUtils.min(1L, 0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_306() throws Exception {
        // Combination: a=1L, b=1L, c=0L
        Object actual = NumberUtils.min(1L, 1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_307() throws Exception {
        // Combination: a=1L, b=-1L, c=Long.MAX_VALUE
        Object actual = NumberUtils.min(1L, -1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_308() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE, c=-1L
        Object actual = NumberUtils.min(1L, Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_309() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE, c=0L
        Object actual = NumberUtils.min(1L, Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_310() throws Exception {
        // Combination: a=-1L, b=0L, c=-1L
        Object actual = NumberUtils.min(-1L, 0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_311() throws Exception {
        // Combination: a=-1L, b=1L, c=Long.MAX_VALUE
        Object actual = NumberUtils.min(-1L, 1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_312() throws Exception {
        // Combination: a=-1L, b=-1L, c=0L
        Object actual = NumberUtils.min(-1L, -1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_313() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE, c=1L
        Object actual = NumberUtils.min(-1L, Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_314() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE, c=1L
        Object actual = NumberUtils.min(-1L, Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_315() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L, c=Long.MAX_VALUE
        Object actual = NumberUtils.min(Long.MAX_VALUE, 0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_316() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L, c=-1L
        Object actual = NumberUtils.min(Long.MAX_VALUE, 1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_317() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L, c=1L
        Object actual = NumberUtils.min(Long.MAX_VALUE, -1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_318() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE, c=0L
        Object actual = NumberUtils.min(Long.MAX_VALUE, Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_319() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE, c=-1L
        Object actual = NumberUtils.min(Long.MAX_VALUE, Long.MIN_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_320() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L, c=Long.MIN_VALUE
        Object actual = NumberUtils.min(Long.MIN_VALUE, 0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_321() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L, c=0L
        Object actual = NumberUtils.min(Long.MIN_VALUE, 1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_322() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L, c=1L
        Object actual = NumberUtils.min(Long.MIN_VALUE, -1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_323() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE, c=-1L
        Object actual = NumberUtils.min(Long.MIN_VALUE, Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_324() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE, c=Long.MAX_VALUE
        Object actual = NumberUtils.min(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_325() throws Exception {
        // Combination: a=1L, b=1L, c=Long.MIN_VALUE
        Object actual = NumberUtils.min(1L, 1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_326() throws Exception {
        // Combination: a=-1L, b=-1L, c=Long.MIN_VALUE
        Object actual = NumberUtils.min(-1L, -1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_327() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE, c=Long.MIN_VALUE
        Object actual = NumberUtils.min(Long.MAX_VALUE, Long.MAX_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_328() throws Exception {
        // Combination: a=0, b=0, c=0
        Object actual = NumberUtils.min(0, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_329() throws Exception {
        // Combination: a=0, b=1, c=1
        Object actual = NumberUtils.min(0, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_330() throws Exception {
        // Combination: a=0, b=-1, c=-1
        Object actual = NumberUtils.min(0, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_331() throws Exception {
        // Combination: a=0, b=Integer.MAX_VALUE, c=Integer.MAX_VALUE
        Object actual = NumberUtils.min(0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_332() throws Exception {
        // Combination: a=0, b=Integer.MIN_VALUE, c=Integer.MIN_VALUE
        Object actual = NumberUtils.min(0, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_333() throws Exception {
        // Combination: a=1, b=0, c=1
        Object actual = NumberUtils.min(1, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_334() throws Exception {
        // Combination: a=1, b=1, c=0
        Object actual = NumberUtils.min(1, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_335() throws Exception {
        // Combination: a=1, b=-1, c=Integer.MAX_VALUE
        Object actual = NumberUtils.min(1, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_336() throws Exception {
        // Combination: a=1, b=Integer.MAX_VALUE, c=-1
        Object actual = NumberUtils.min(1, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_337() throws Exception {
        // Combination: a=1, b=Integer.MIN_VALUE, c=0
        Object actual = NumberUtils.min(1, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_338() throws Exception {
        // Combination: a=-1, b=0, c=-1
        Object actual = NumberUtils.min(-1, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_339() throws Exception {
        // Combination: a=-1, b=1, c=Integer.MAX_VALUE
        Object actual = NumberUtils.min(-1, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_340() throws Exception {
        // Combination: a=-1, b=-1, c=0
        Object actual = NumberUtils.min(-1, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_341() throws Exception {
        // Combination: a=-1, b=Integer.MAX_VALUE, c=1
        Object actual = NumberUtils.min(-1, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_342() throws Exception {
        // Combination: a=-1, b=Integer.MIN_VALUE, c=1
        Object actual = NumberUtils.min(-1, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_343() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=0, c=Integer.MAX_VALUE
        Object actual = NumberUtils.min(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_344() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=1, c=-1
        Object actual = NumberUtils.min(Integer.MAX_VALUE, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_345() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=-1, c=1
        Object actual = NumberUtils.min(Integer.MAX_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_346() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE, c=0
        Object actual = NumberUtils.min(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_347() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MIN_VALUE, c=-1
        Object actual = NumberUtils.min(Integer.MAX_VALUE, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_348() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=0, c=Integer.MIN_VALUE
        Object actual = NumberUtils.min(Integer.MIN_VALUE, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_349() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=1, c=0
        Object actual = NumberUtils.min(Integer.MIN_VALUE, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_350() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=-1, c=1
        Object actual = NumberUtils.min(Integer.MIN_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_351() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MAX_VALUE, c=-1
        Object actual = NumberUtils.min(Integer.MIN_VALUE, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_352() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MIN_VALUE, c=Integer.MAX_VALUE
        Object actual = NumberUtils.min(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_353() throws Exception {
        // Combination: a=1, b=1, c=Integer.MIN_VALUE
        Object actual = NumberUtils.min(1, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_354() throws Exception {
        // Combination: a=-1, b=-1, c=Integer.MIN_VALUE
        Object actual = NumberUtils.min(-1, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_355() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE, c=Integer.MIN_VALUE
        Object actual = NumberUtils.min(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_356() throws Exception {
        // Combination: a=(short) 0, b=(short) 0, c=(short) 0
        Object actual = NumberUtils.min((short) 0, (short) 0, (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_357() throws Exception {
        // Combination: a=(short) 0, b=(short) 1, c=(short) 1
        Object actual = NumberUtils.min((short) 0, (short) 1, (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_358() throws Exception {
        // Combination: a=(short) 0, b=(short) -1, c=(short) -1
        Object actual = NumberUtils.min((short) 0, (short) -1, (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_359() throws Exception {
        // Combination: a=(short) 0, b=Short.MAX_VALUE, c=Short.MAX_VALUE
        Object actual = NumberUtils.min((short) 0, Short.MAX_VALUE, Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_360() throws Exception {
        // Combination: a=(short) 0, b=Short.MIN_VALUE, c=Short.MIN_VALUE
        Object actual = NumberUtils.min((short) 0, Short.MIN_VALUE, Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_361() throws Exception {
        // Combination: a=(short) 1, b=(short) 0, c=(short) 1
        Object actual = NumberUtils.min((short) 1, (short) 0, (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_362() throws Exception {
        // Combination: a=(short) 1, b=(short) 1, c=(short) 0
        Object actual = NumberUtils.min((short) 1, (short) 1, (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_363() throws Exception {
        // Combination: a=(short) 1, b=(short) -1, c=Short.MAX_VALUE
        Object actual = NumberUtils.min((short) 1, (short) -1, Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_364() throws Exception {
        // Combination: a=(short) 1, b=Short.MAX_VALUE, c=(short) -1
        Object actual = NumberUtils.min((short) 1, Short.MAX_VALUE, (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_365() throws Exception {
        // Combination: a=(short) 1, b=Short.MIN_VALUE, c=(short) 0
        Object actual = NumberUtils.min((short) 1, Short.MIN_VALUE, (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_366() throws Exception {
        // Combination: a=(short) -1, b=(short) 0, c=(short) -1
        Object actual = NumberUtils.min((short) -1, (short) 0, (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_367() throws Exception {
        // Combination: a=(short) -1, b=(short) 1, c=Short.MAX_VALUE
        Object actual = NumberUtils.min((short) -1, (short) 1, Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_368() throws Exception {
        // Combination: a=(short) -1, b=(short) -1, c=(short) 0
        Object actual = NumberUtils.min((short) -1, (short) -1, (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_369() throws Exception {
        // Combination: a=(short) -1, b=Short.MAX_VALUE, c=(short) 1
        Object actual = NumberUtils.min((short) -1, Short.MAX_VALUE, (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_370() throws Exception {
        // Combination: a=(short) -1, b=Short.MIN_VALUE, c=(short) 1
        Object actual = NumberUtils.min((short) -1, Short.MIN_VALUE, (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_371() throws Exception {
        // Combination: a=Short.MAX_VALUE, b=(short) 0, c=Short.MAX_VALUE
        Object actual = NumberUtils.min(Short.MAX_VALUE, (short) 0, Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_372() throws Exception {
        // Combination: a=Short.MAX_VALUE, b=(short) 1, c=(short) -1
        Object actual = NumberUtils.min(Short.MAX_VALUE, (short) 1, (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_373() throws Exception {
        // Combination: a=Short.MAX_VALUE, b=(short) -1, c=(short) 1
        Object actual = NumberUtils.min(Short.MAX_VALUE, (short) -1, (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_374() throws Exception {
        // Combination: a=Short.MAX_VALUE, b=Short.MAX_VALUE, c=(short) 0
        Object actual = NumberUtils.min(Short.MAX_VALUE, Short.MAX_VALUE, (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_375() throws Exception {
        // Combination: a=Short.MAX_VALUE, b=Short.MIN_VALUE, c=(short) -1
        Object actual = NumberUtils.min(Short.MAX_VALUE, Short.MIN_VALUE, (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_376() throws Exception {
        // Combination: a=Short.MIN_VALUE, b=(short) 0, c=Short.MIN_VALUE
        Object actual = NumberUtils.min(Short.MIN_VALUE, (short) 0, Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_377() throws Exception {
        // Combination: a=Short.MIN_VALUE, b=(short) 1, c=(short) 0
        Object actual = NumberUtils.min(Short.MIN_VALUE, (short) 1, (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_378() throws Exception {
        // Combination: a=Short.MIN_VALUE, b=(short) -1, c=(short) 1
        Object actual = NumberUtils.min(Short.MIN_VALUE, (short) -1, (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_379() throws Exception {
        // Combination: a=Short.MIN_VALUE, b=Short.MAX_VALUE, c=(short) -1
        Object actual = NumberUtils.min(Short.MIN_VALUE, Short.MAX_VALUE, (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_380() throws Exception {
        // Combination: a=Short.MIN_VALUE, b=Short.MIN_VALUE, c=Short.MAX_VALUE
        Object actual = NumberUtils.min(Short.MIN_VALUE, Short.MIN_VALUE, Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_381() throws Exception {
        // Combination: a=(short) 1, b=(short) 1, c=Short.MIN_VALUE
        Object actual = NumberUtils.min((short) 1, (short) 1, Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_382() throws Exception {
        // Combination: a=(short) -1, b=(short) -1, c=Short.MIN_VALUE
        Object actual = NumberUtils.min((short) -1, (short) -1, Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_383() throws Exception {
        // Combination: a=Short.MAX_VALUE, b=Short.MAX_VALUE, c=Short.MIN_VALUE
        Object actual = NumberUtils.min(Short.MAX_VALUE, Short.MAX_VALUE, Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_384() throws Exception {
        // Combination: a=(byte) 0, b=(byte) 0, c=(byte) 0
        Object actual = NumberUtils.min((byte) 0, (byte) 0, (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_385() throws Exception {
        // Combination: a=(byte) 0, b=(byte) 1, c=(byte) 1
        Object actual = NumberUtils.min((byte) 0, (byte) 1, (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_386() throws Exception {
        // Combination: a=(byte) 0, b=(byte) -1, c=(byte) -1
        Object actual = NumberUtils.min((byte) 0, (byte) -1, (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_387() throws Exception {
        // Combination: a=(byte) 0, b=Byte.MAX_VALUE, c=Byte.MAX_VALUE
        Object actual = NumberUtils.min((byte) 0, Byte.MAX_VALUE, Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_388() throws Exception {
        // Combination: a=(byte) 0, b=Byte.MIN_VALUE, c=Byte.MIN_VALUE
        Object actual = NumberUtils.min((byte) 0, Byte.MIN_VALUE, Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_389() throws Exception {
        // Combination: a=(byte) 1, b=(byte) 0, c=(byte) 1
        Object actual = NumberUtils.min((byte) 1, (byte) 0, (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_390() throws Exception {
        // Combination: a=(byte) 1, b=(byte) 1, c=(byte) 0
        Object actual = NumberUtils.min((byte) 1, (byte) 1, (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_391() throws Exception {
        // Combination: a=(byte) 1, b=(byte) -1, c=Byte.MAX_VALUE
        Object actual = NumberUtils.min((byte) 1, (byte) -1, Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_392() throws Exception {
        // Combination: a=(byte) 1, b=Byte.MAX_VALUE, c=(byte) -1
        Object actual = NumberUtils.min((byte) 1, Byte.MAX_VALUE, (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_393() throws Exception {
        // Combination: a=(byte) 1, b=Byte.MIN_VALUE, c=(byte) 0
        Object actual = NumberUtils.min((byte) 1, Byte.MIN_VALUE, (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_394() throws Exception {
        // Combination: a=(byte) -1, b=(byte) 0, c=(byte) -1
        Object actual = NumberUtils.min((byte) -1, (byte) 0, (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_395() throws Exception {
        // Combination: a=(byte) -1, b=(byte) 1, c=Byte.MAX_VALUE
        Object actual = NumberUtils.min((byte) -1, (byte) 1, Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_396() throws Exception {
        // Combination: a=(byte) -1, b=(byte) -1, c=(byte) 0
        Object actual = NumberUtils.min((byte) -1, (byte) -1, (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_397() throws Exception {
        // Combination: a=(byte) -1, b=Byte.MAX_VALUE, c=(byte) 1
        Object actual = NumberUtils.min((byte) -1, Byte.MAX_VALUE, (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_398() throws Exception {
        // Combination: a=(byte) -1, b=Byte.MIN_VALUE, c=(byte) 1
        Object actual = NumberUtils.min((byte) -1, Byte.MIN_VALUE, (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_399() throws Exception {
        // Combination: a=Byte.MAX_VALUE, b=(byte) 0, c=Byte.MAX_VALUE
        Object actual = NumberUtils.min(Byte.MAX_VALUE, (byte) 0, Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_400() throws Exception {
        // Combination: a=Byte.MAX_VALUE, b=(byte) 1, c=(byte) -1
        Object actual = NumberUtils.min(Byte.MAX_VALUE, (byte) 1, (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_401() throws Exception {
        // Combination: a=Byte.MAX_VALUE, b=(byte) -1, c=(byte) 1
        Object actual = NumberUtils.min(Byte.MAX_VALUE, (byte) -1, (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_402() throws Exception {
        // Combination: a=Byte.MAX_VALUE, b=Byte.MAX_VALUE, c=(byte) 0
        Object actual = NumberUtils.min(Byte.MAX_VALUE, Byte.MAX_VALUE, (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_403() throws Exception {
        // Combination: a=Byte.MAX_VALUE, b=Byte.MIN_VALUE, c=(byte) -1
        Object actual = NumberUtils.min(Byte.MAX_VALUE, Byte.MIN_VALUE, (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_404() throws Exception {
        // Combination: a=Byte.MIN_VALUE, b=(byte) 0, c=Byte.MIN_VALUE
        Object actual = NumberUtils.min(Byte.MIN_VALUE, (byte) 0, Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_405() throws Exception {
        // Combination: a=Byte.MIN_VALUE, b=(byte) 1, c=(byte) 0
        Object actual = NumberUtils.min(Byte.MIN_VALUE, (byte) 1, (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_406() throws Exception {
        // Combination: a=Byte.MIN_VALUE, b=(byte) -1, c=(byte) 1
        Object actual = NumberUtils.min(Byte.MIN_VALUE, (byte) -1, (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_407() throws Exception {
        // Combination: a=Byte.MIN_VALUE, b=Byte.MAX_VALUE, c=(byte) -1
        Object actual = NumberUtils.min(Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_408() throws Exception {
        // Combination: a=Byte.MIN_VALUE, b=Byte.MIN_VALUE, c=Byte.MAX_VALUE
        Object actual = NumberUtils.min(Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_409() throws Exception {
        // Combination: a=(byte) 1, b=(byte) 1, c=Byte.MIN_VALUE
        Object actual = NumberUtils.min((byte) 1, (byte) 1, Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_410() throws Exception {
        // Combination: a=(byte) -1, b=(byte) -1, c=Byte.MIN_VALUE
        Object actual = NumberUtils.min((byte) -1, (byte) -1, Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_411() throws Exception {
        // Combination: a=Byte.MAX_VALUE, b=Byte.MAX_VALUE, c=Byte.MIN_VALUE
        Object actual = NumberUtils.min(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_412() throws Exception {
        // Combination: a=0.0d, b=0.0d, c=0.0d
        Object actual = NumberUtils.min(0.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_413() throws Exception {
        // Combination: a=0.0d, b=1.0d, c=1.0d
        Object actual = NumberUtils.min(0.0d, 1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_414() throws Exception {
        // Combination: a=0.0d, b=-1.0d, c=-1.0d
        Object actual = NumberUtils.min(0.0d, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_415() throws Exception {
        // Combination: a=0.0d, b=Double.NaN, c=Double.NaN
        Object actual = NumberUtils.min(0.0d, Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_416() throws Exception {
        // Combination: a=0.0d, b=Double.POSITIVE_INFINITY, c=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.min(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_417() throws Exception {
        // Combination: a=1.0d, b=0.0d, c=1.0d
        Object actual = NumberUtils.min(1.0d, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_418() throws Exception {
        // Combination: a=1.0d, b=1.0d, c=0.0d
        Object actual = NumberUtils.min(1.0d, 1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_419() throws Exception {
        // Combination: a=1.0d, b=-1.0d, c=Double.NaN
        Object actual = NumberUtils.min(1.0d, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_420() throws Exception {
        // Combination: a=1.0d, b=Double.NaN, c=-1.0d
        Object actual = NumberUtils.min(1.0d, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_421() throws Exception {
        // Combination: a=1.0d, b=Double.POSITIVE_INFINITY, c=0.0d
        Object actual = NumberUtils.min(1.0d, Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_422() throws Exception {
        // Combination: a=-1.0d, b=0.0d, c=-1.0d
        Object actual = NumberUtils.min(-1.0d, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_423() throws Exception {
        // Combination: a=-1.0d, b=1.0d, c=Double.NaN
        Object actual = NumberUtils.min(-1.0d, 1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_424() throws Exception {
        // Combination: a=-1.0d, b=-1.0d, c=0.0d
        Object actual = NumberUtils.min(-1.0d, -1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_425() throws Exception {
        // Combination: a=-1.0d, b=Double.NaN, c=1.0d
        Object actual = NumberUtils.min(-1.0d, Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_426() throws Exception {
        // Combination: a=-1.0d, b=Double.POSITIVE_INFINITY, c=1.0d
        Object actual = NumberUtils.min(-1.0d, Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_427() throws Exception {
        // Combination: a=Double.NaN, b=0.0d, c=Double.NaN
        Object actual = NumberUtils.min(Double.NaN, 0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_428() throws Exception {
        // Combination: a=Double.NaN, b=1.0d, c=-1.0d
        Object actual = NumberUtils.min(Double.NaN, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_429() throws Exception {
        // Combination: a=Double.NaN, b=-1.0d, c=1.0d
        Object actual = NumberUtils.min(Double.NaN, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_430() throws Exception {
        // Combination: a=Double.NaN, b=Double.NaN, c=0.0d
        Object actual = NumberUtils.min(Double.NaN, Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_431() throws Exception {
        // Combination: a=Double.NaN, b=Double.POSITIVE_INFINITY, c=-1.0d
        Object actual = NumberUtils.min(Double.NaN, Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_432() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=0.0d, c=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.min(Double.POSITIVE_INFINITY, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_433() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=1.0d, c=0.0d
        Object actual = NumberUtils.min(Double.POSITIVE_INFINITY, 1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_434() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=-1.0d, c=1.0d
        Object actual = NumberUtils.min(Double.POSITIVE_INFINITY, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_435() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.NaN, c=-1.0d
        Object actual = NumberUtils.min(Double.POSITIVE_INFINITY, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_436() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.POSITIVE_INFINITY, c=Double.NaN
        Object actual = NumberUtils.min(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_437() throws Exception {
        // Combination: a=1.0d, b=1.0d, c=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.min(1.0d, 1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_438() throws Exception {
        // Combination: a=-1.0d, b=-1.0d, c=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.min(-1.0d, -1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_439() throws Exception {
        // Combination: a=Double.NaN, b=Double.NaN, c=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.min(Double.NaN, Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_440() throws Exception {
        // Combination: a=0.0f, b=0.0f, c=0.0f
        Object actual = NumberUtils.min(0.0f, 0.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_441() throws Exception {
        // Combination: a=0.0f, b=1.0f, c=1.0f
        Object actual = NumberUtils.min(0.0f, 1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_442() throws Exception {
        // Combination: a=0.0f, b=-1.0f, c=-1.0f
        Object actual = NumberUtils.min(0.0f, -1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_443() throws Exception {
        // Combination: a=0.0f, b=Float.NaN, c=Float.NaN
        Object actual = NumberUtils.min(0.0f, Float.NaN, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_444() throws Exception {
        // Combination: a=0.0f, b=Float.POSITIVE_INFINITY, c=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.min(0.0f, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_445() throws Exception {
        // Combination: a=1.0f, b=0.0f, c=1.0f
        Object actual = NumberUtils.min(1.0f, 0.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_446() throws Exception {
        // Combination: a=1.0f, b=1.0f, c=0.0f
        Object actual = NumberUtils.min(1.0f, 1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_447() throws Exception {
        // Combination: a=1.0f, b=-1.0f, c=Float.NaN
        Object actual = NumberUtils.min(1.0f, -1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_448() throws Exception {
        // Combination: a=1.0f, b=Float.NaN, c=-1.0f
        Object actual = NumberUtils.min(1.0f, Float.NaN, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_449() throws Exception {
        // Combination: a=1.0f, b=Float.POSITIVE_INFINITY, c=0.0f
        Object actual = NumberUtils.min(1.0f, Float.POSITIVE_INFINITY, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_450() throws Exception {
        // Combination: a=-1.0f, b=0.0f, c=-1.0f
        Object actual = NumberUtils.min(-1.0f, 0.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_451() throws Exception {
        // Combination: a=-1.0f, b=1.0f, c=Float.NaN
        Object actual = NumberUtils.min(-1.0f, 1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_452() throws Exception {
        // Combination: a=-1.0f, b=-1.0f, c=0.0f
        Object actual = NumberUtils.min(-1.0f, -1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_453() throws Exception {
        // Combination: a=-1.0f, b=Float.NaN, c=1.0f
        Object actual = NumberUtils.min(-1.0f, Float.NaN, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_454() throws Exception {
        // Combination: a=-1.0f, b=Float.POSITIVE_INFINITY, c=1.0f
        Object actual = NumberUtils.min(-1.0f, Float.POSITIVE_INFINITY, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_455() throws Exception {
        // Combination: a=Float.NaN, b=0.0f, c=Float.NaN
        Object actual = NumberUtils.min(Float.NaN, 0.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_456() throws Exception {
        // Combination: a=Float.NaN, b=1.0f, c=-1.0f
        Object actual = NumberUtils.min(Float.NaN, 1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_457() throws Exception {
        // Combination: a=Float.NaN, b=-1.0f, c=1.0f
        Object actual = NumberUtils.min(Float.NaN, -1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_458() throws Exception {
        // Combination: a=Float.NaN, b=Float.NaN, c=0.0f
        Object actual = NumberUtils.min(Float.NaN, Float.NaN, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_459() throws Exception {
        // Combination: a=Float.NaN, b=Float.POSITIVE_INFINITY, c=-1.0f
        Object actual = NumberUtils.min(Float.NaN, Float.POSITIVE_INFINITY, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_460() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=0.0f, c=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.min(Float.POSITIVE_INFINITY, 0.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_461() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=1.0f, c=0.0f
        Object actual = NumberUtils.min(Float.POSITIVE_INFINITY, 1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_462() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=-1.0f, c=1.0f
        Object actual = NumberUtils.min(Float.POSITIVE_INFINITY, -1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_463() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=Float.NaN, c=-1.0f
        Object actual = NumberUtils.min(Float.POSITIVE_INFINITY, Float.NaN, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_464() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=Float.POSITIVE_INFINITY, c=Float.NaN
        Object actual = NumberUtils.min(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_465() throws Exception {
        // Combination: a=1.0f, b=1.0f, c=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.min(1.0f, 1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_466() throws Exception {
        // Combination: a=-1.0f, b=-1.0f, c=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.min(-1.0f, -1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_467() throws Exception {
        // Combination: a=Float.NaN, b=Float.NaN, c=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.min(Float.NaN, Float.NaN, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_468() throws Exception {
        // Combination: a=0L, b=0L, c=0L
        Object actual = NumberUtils.max(0L, 0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_469() throws Exception {
        // Combination: a=0L, b=1L, c=1L
        Object actual = NumberUtils.max(0L, 1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_470() throws Exception {
        // Combination: a=0L, b=-1L, c=-1L
        Object actual = NumberUtils.max(0L, -1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_471() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE, c=Long.MAX_VALUE
        Object actual = NumberUtils.max(0L, Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_472() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE, c=Long.MIN_VALUE
        Object actual = NumberUtils.max(0L, Long.MIN_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_473() throws Exception {
        // Combination: a=1L, b=0L, c=1L
        Object actual = NumberUtils.max(1L, 0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_474() throws Exception {
        // Combination: a=1L, b=1L, c=0L
        Object actual = NumberUtils.max(1L, 1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_475() throws Exception {
        // Combination: a=1L, b=-1L, c=Long.MAX_VALUE
        Object actual = NumberUtils.max(1L, -1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_476() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE, c=-1L
        Object actual = NumberUtils.max(1L, Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_477() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE, c=0L
        Object actual = NumberUtils.max(1L, Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_478() throws Exception {
        // Combination: a=-1L, b=0L, c=-1L
        Object actual = NumberUtils.max(-1L, 0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_479() throws Exception {
        // Combination: a=-1L, b=1L, c=Long.MAX_VALUE
        Object actual = NumberUtils.max(-1L, 1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_480() throws Exception {
        // Combination: a=-1L, b=-1L, c=0L
        Object actual = NumberUtils.max(-1L, -1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_481() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE, c=1L
        Object actual = NumberUtils.max(-1L, Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_482() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE, c=1L
        Object actual = NumberUtils.max(-1L, Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_483() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L, c=Long.MAX_VALUE
        Object actual = NumberUtils.max(Long.MAX_VALUE, 0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_484() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L, c=-1L
        Object actual = NumberUtils.max(Long.MAX_VALUE, 1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_485() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L, c=1L
        Object actual = NumberUtils.max(Long.MAX_VALUE, -1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_486() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE, c=0L
        Object actual = NumberUtils.max(Long.MAX_VALUE, Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_487() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE, c=-1L
        Object actual = NumberUtils.max(Long.MAX_VALUE, Long.MIN_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_488() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L, c=Long.MIN_VALUE
        Object actual = NumberUtils.max(Long.MIN_VALUE, 0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_489() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L, c=0L
        Object actual = NumberUtils.max(Long.MIN_VALUE, 1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_490() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L, c=1L
        Object actual = NumberUtils.max(Long.MIN_VALUE, -1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_491() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE, c=-1L
        Object actual = NumberUtils.max(Long.MIN_VALUE, Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_492() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE, c=Long.MAX_VALUE
        Object actual = NumberUtils.max(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_493() throws Exception {
        // Combination: a=1L, b=1L, c=Long.MIN_VALUE
        Object actual = NumberUtils.max(1L, 1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_494() throws Exception {
        // Combination: a=-1L, b=-1L, c=Long.MIN_VALUE
        Object actual = NumberUtils.max(-1L, -1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_495() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE, c=Long.MIN_VALUE
        Object actual = NumberUtils.max(Long.MAX_VALUE, Long.MAX_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_496() throws Exception {
        // Combination: a=0, b=0, c=0
        Object actual = NumberUtils.max(0, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_497() throws Exception {
        // Combination: a=0, b=1, c=1
        Object actual = NumberUtils.max(0, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_498() throws Exception {
        // Combination: a=0, b=-1, c=-1
        Object actual = NumberUtils.max(0, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_499() throws Exception {
        // Combination: a=0, b=Integer.MAX_VALUE, c=Integer.MAX_VALUE
        Object actual = NumberUtils.max(0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_500() throws Exception {
        // Combination: a=0, b=Integer.MIN_VALUE, c=Integer.MIN_VALUE
        Object actual = NumberUtils.max(0, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_501() throws Exception {
        // Combination: a=1, b=0, c=1
        Object actual = NumberUtils.max(1, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_502() throws Exception {
        // Combination: a=1, b=1, c=0
        Object actual = NumberUtils.max(1, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_503() throws Exception {
        // Combination: a=1, b=-1, c=Integer.MAX_VALUE
        Object actual = NumberUtils.max(1, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_504() throws Exception {
        // Combination: a=1, b=Integer.MAX_VALUE, c=-1
        Object actual = NumberUtils.max(1, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_505() throws Exception {
        // Combination: a=1, b=Integer.MIN_VALUE, c=0
        Object actual = NumberUtils.max(1, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_506() throws Exception {
        // Combination: a=-1, b=0, c=-1
        Object actual = NumberUtils.max(-1, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_507() throws Exception {
        // Combination: a=-1, b=1, c=Integer.MAX_VALUE
        Object actual = NumberUtils.max(-1, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_508() throws Exception {
        // Combination: a=-1, b=-1, c=0
        Object actual = NumberUtils.max(-1, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_509() throws Exception {
        // Combination: a=-1, b=Integer.MAX_VALUE, c=1
        Object actual = NumberUtils.max(-1, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_510() throws Exception {
        // Combination: a=-1, b=Integer.MIN_VALUE, c=1
        Object actual = NumberUtils.max(-1, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_511() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=0, c=Integer.MAX_VALUE
        Object actual = NumberUtils.max(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_512() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=1, c=-1
        Object actual = NumberUtils.max(Integer.MAX_VALUE, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_513() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=-1, c=1
        Object actual = NumberUtils.max(Integer.MAX_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_514() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE, c=0
        Object actual = NumberUtils.max(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_515() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MIN_VALUE, c=-1
        Object actual = NumberUtils.max(Integer.MAX_VALUE, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_516() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=0, c=Integer.MIN_VALUE
        Object actual = NumberUtils.max(Integer.MIN_VALUE, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_517() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=1, c=0
        Object actual = NumberUtils.max(Integer.MIN_VALUE, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_518() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=-1, c=1
        Object actual = NumberUtils.max(Integer.MIN_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_519() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MAX_VALUE, c=-1
        Object actual = NumberUtils.max(Integer.MIN_VALUE, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_520() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MIN_VALUE, c=Integer.MAX_VALUE
        Object actual = NumberUtils.max(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_521() throws Exception {
        // Combination: a=1, b=1, c=Integer.MIN_VALUE
        Object actual = NumberUtils.max(1, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_522() throws Exception {
        // Combination: a=-1, b=-1, c=Integer.MIN_VALUE
        Object actual = NumberUtils.max(-1, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_523() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE, c=Integer.MIN_VALUE
        Object actual = NumberUtils.max(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_524() throws Exception {
        // Combination: a=(short) 0, b=(short) 0, c=(short) 0
        Object actual = NumberUtils.max((short) 0, (short) 0, (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_525() throws Exception {
        // Combination: a=(short) 0, b=(short) 1, c=(short) 1
        Object actual = NumberUtils.max((short) 0, (short) 1, (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_526() throws Exception {
        // Combination: a=(short) 0, b=(short) -1, c=(short) -1
        Object actual = NumberUtils.max((short) 0, (short) -1, (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_527() throws Exception {
        // Combination: a=(short) 0, b=Short.MAX_VALUE, c=Short.MAX_VALUE
        Object actual = NumberUtils.max((short) 0, Short.MAX_VALUE, Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_528() throws Exception {
        // Combination: a=(short) 0, b=Short.MIN_VALUE, c=Short.MIN_VALUE
        Object actual = NumberUtils.max((short) 0, Short.MIN_VALUE, Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_529() throws Exception {
        // Combination: a=(short) 1, b=(short) 0, c=(short) 1
        Object actual = NumberUtils.max((short) 1, (short) 0, (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_530() throws Exception {
        // Combination: a=(short) 1, b=(short) 1, c=(short) 0
        Object actual = NumberUtils.max((short) 1, (short) 1, (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_531() throws Exception {
        // Combination: a=(short) 1, b=(short) -1, c=Short.MAX_VALUE
        Object actual = NumberUtils.max((short) 1, (short) -1, Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_532() throws Exception {
        // Combination: a=(short) 1, b=Short.MAX_VALUE, c=(short) -1
        Object actual = NumberUtils.max((short) 1, Short.MAX_VALUE, (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_533() throws Exception {
        // Combination: a=(short) 1, b=Short.MIN_VALUE, c=(short) 0
        Object actual = NumberUtils.max((short) 1, Short.MIN_VALUE, (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_534() throws Exception {
        // Combination: a=(short) -1, b=(short) 0, c=(short) -1
        Object actual = NumberUtils.max((short) -1, (short) 0, (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_535() throws Exception {
        // Combination: a=(short) -1, b=(short) 1, c=Short.MAX_VALUE
        Object actual = NumberUtils.max((short) -1, (short) 1, Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_536() throws Exception {
        // Combination: a=(short) -1, b=(short) -1, c=(short) 0
        Object actual = NumberUtils.max((short) -1, (short) -1, (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_537() throws Exception {
        // Combination: a=(short) -1, b=Short.MAX_VALUE, c=(short) 1
        Object actual = NumberUtils.max((short) -1, Short.MAX_VALUE, (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_538() throws Exception {
        // Combination: a=(short) -1, b=Short.MIN_VALUE, c=(short) 1
        Object actual = NumberUtils.max((short) -1, Short.MIN_VALUE, (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_539() throws Exception {
        // Combination: a=Short.MAX_VALUE, b=(short) 0, c=Short.MAX_VALUE
        Object actual = NumberUtils.max(Short.MAX_VALUE, (short) 0, Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_540() throws Exception {
        // Combination: a=Short.MAX_VALUE, b=(short) 1, c=(short) -1
        Object actual = NumberUtils.max(Short.MAX_VALUE, (short) 1, (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_541() throws Exception {
        // Combination: a=Short.MAX_VALUE, b=(short) -1, c=(short) 1
        Object actual = NumberUtils.max(Short.MAX_VALUE, (short) -1, (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_542() throws Exception {
        // Combination: a=Short.MAX_VALUE, b=Short.MAX_VALUE, c=(short) 0
        Object actual = NumberUtils.max(Short.MAX_VALUE, Short.MAX_VALUE, (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_543() throws Exception {
        // Combination: a=Short.MAX_VALUE, b=Short.MIN_VALUE, c=(short) -1
        Object actual = NumberUtils.max(Short.MAX_VALUE, Short.MIN_VALUE, (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_544() throws Exception {
        // Combination: a=Short.MIN_VALUE, b=(short) 0, c=Short.MIN_VALUE
        Object actual = NumberUtils.max(Short.MIN_VALUE, (short) 0, Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_545() throws Exception {
        // Combination: a=Short.MIN_VALUE, b=(short) 1, c=(short) 0
        Object actual = NumberUtils.max(Short.MIN_VALUE, (short) 1, (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_546() throws Exception {
        // Combination: a=Short.MIN_VALUE, b=(short) -1, c=(short) 1
        Object actual = NumberUtils.max(Short.MIN_VALUE, (short) -1, (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_547() throws Exception {
        // Combination: a=Short.MIN_VALUE, b=Short.MAX_VALUE, c=(short) -1
        Object actual = NumberUtils.max(Short.MIN_VALUE, Short.MAX_VALUE, (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_548() throws Exception {
        // Combination: a=Short.MIN_VALUE, b=Short.MIN_VALUE, c=Short.MAX_VALUE
        Object actual = NumberUtils.max(Short.MIN_VALUE, Short.MIN_VALUE, Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_549() throws Exception {
        // Combination: a=(short) 1, b=(short) 1, c=Short.MIN_VALUE
        Object actual = NumberUtils.max((short) 1, (short) 1, Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_550() throws Exception {
        // Combination: a=(short) -1, b=(short) -1, c=Short.MIN_VALUE
        Object actual = NumberUtils.max((short) -1, (short) -1, Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_551() throws Exception {
        // Combination: a=Short.MAX_VALUE, b=Short.MAX_VALUE, c=Short.MIN_VALUE
        Object actual = NumberUtils.max(Short.MAX_VALUE, Short.MAX_VALUE, Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_552() throws Exception {
        // Combination: a=(byte) 0, b=(byte) 0, c=(byte) 0
        Object actual = NumberUtils.max((byte) 0, (byte) 0, (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_553() throws Exception {
        // Combination: a=(byte) 0, b=(byte) 1, c=(byte) 1
        Object actual = NumberUtils.max((byte) 0, (byte) 1, (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_554() throws Exception {
        // Combination: a=(byte) 0, b=(byte) -1, c=(byte) -1
        Object actual = NumberUtils.max((byte) 0, (byte) -1, (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_555() throws Exception {
        // Combination: a=(byte) 0, b=Byte.MAX_VALUE, c=Byte.MAX_VALUE
        Object actual = NumberUtils.max((byte) 0, Byte.MAX_VALUE, Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_556() throws Exception {
        // Combination: a=(byte) 0, b=Byte.MIN_VALUE, c=Byte.MIN_VALUE
        Object actual = NumberUtils.max((byte) 0, Byte.MIN_VALUE, Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_557() throws Exception {
        // Combination: a=(byte) 1, b=(byte) 0, c=(byte) 1
        Object actual = NumberUtils.max((byte) 1, (byte) 0, (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_558() throws Exception {
        // Combination: a=(byte) 1, b=(byte) 1, c=(byte) 0
        Object actual = NumberUtils.max((byte) 1, (byte) 1, (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_559() throws Exception {
        // Combination: a=(byte) 1, b=(byte) -1, c=Byte.MAX_VALUE
        Object actual = NumberUtils.max((byte) 1, (byte) -1, Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_560() throws Exception {
        // Combination: a=(byte) 1, b=Byte.MAX_VALUE, c=(byte) -1
        Object actual = NumberUtils.max((byte) 1, Byte.MAX_VALUE, (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_561() throws Exception {
        // Combination: a=(byte) 1, b=Byte.MIN_VALUE, c=(byte) 0
        Object actual = NumberUtils.max((byte) 1, Byte.MIN_VALUE, (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_562() throws Exception {
        // Combination: a=(byte) -1, b=(byte) 0, c=(byte) -1
        Object actual = NumberUtils.max((byte) -1, (byte) 0, (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_563() throws Exception {
        // Combination: a=(byte) -1, b=(byte) 1, c=Byte.MAX_VALUE
        Object actual = NumberUtils.max((byte) -1, (byte) 1, Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_564() throws Exception {
        // Combination: a=(byte) -1, b=(byte) -1, c=(byte) 0
        Object actual = NumberUtils.max((byte) -1, (byte) -1, (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_565() throws Exception {
        // Combination: a=(byte) -1, b=Byte.MAX_VALUE, c=(byte) 1
        Object actual = NumberUtils.max((byte) -1, Byte.MAX_VALUE, (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_566() throws Exception {
        // Combination: a=(byte) -1, b=Byte.MIN_VALUE, c=(byte) 1
        Object actual = NumberUtils.max((byte) -1, Byte.MIN_VALUE, (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_567() throws Exception {
        // Combination: a=Byte.MAX_VALUE, b=(byte) 0, c=Byte.MAX_VALUE
        Object actual = NumberUtils.max(Byte.MAX_VALUE, (byte) 0, Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_568() throws Exception {
        // Combination: a=Byte.MAX_VALUE, b=(byte) 1, c=(byte) -1
        Object actual = NumberUtils.max(Byte.MAX_VALUE, (byte) 1, (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_569() throws Exception {
        // Combination: a=Byte.MAX_VALUE, b=(byte) -1, c=(byte) 1
        Object actual = NumberUtils.max(Byte.MAX_VALUE, (byte) -1, (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_570() throws Exception {
        // Combination: a=Byte.MAX_VALUE, b=Byte.MAX_VALUE, c=(byte) 0
        Object actual = NumberUtils.max(Byte.MAX_VALUE, Byte.MAX_VALUE, (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_571() throws Exception {
        // Combination: a=Byte.MAX_VALUE, b=Byte.MIN_VALUE, c=(byte) -1
        Object actual = NumberUtils.max(Byte.MAX_VALUE, Byte.MIN_VALUE, (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_572() throws Exception {
        // Combination: a=Byte.MIN_VALUE, b=(byte) 0, c=Byte.MIN_VALUE
        Object actual = NumberUtils.max(Byte.MIN_VALUE, (byte) 0, Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_573() throws Exception {
        // Combination: a=Byte.MIN_VALUE, b=(byte) 1, c=(byte) 0
        Object actual = NumberUtils.max(Byte.MIN_VALUE, (byte) 1, (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_574() throws Exception {
        // Combination: a=Byte.MIN_VALUE, b=(byte) -1, c=(byte) 1
        Object actual = NumberUtils.max(Byte.MIN_VALUE, (byte) -1, (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_575() throws Exception {
        // Combination: a=Byte.MIN_VALUE, b=Byte.MAX_VALUE, c=(byte) -1
        Object actual = NumberUtils.max(Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_576() throws Exception {
        // Combination: a=Byte.MIN_VALUE, b=Byte.MIN_VALUE, c=Byte.MAX_VALUE
        Object actual = NumberUtils.max(Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_577() throws Exception {
        // Combination: a=(byte) 1, b=(byte) 1, c=Byte.MIN_VALUE
        Object actual = NumberUtils.max((byte) 1, (byte) 1, Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_578() throws Exception {
        // Combination: a=(byte) -1, b=(byte) -1, c=Byte.MIN_VALUE
        Object actual = NumberUtils.max((byte) -1, (byte) -1, Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_579() throws Exception {
        // Combination: a=Byte.MAX_VALUE, b=Byte.MAX_VALUE, c=Byte.MIN_VALUE
        Object actual = NumberUtils.max(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_580() throws Exception {
        // Combination: a=0.0d, b=0.0d, c=0.0d
        Object actual = NumberUtils.max(0.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_581() throws Exception {
        // Combination: a=0.0d, b=1.0d, c=1.0d
        Object actual = NumberUtils.max(0.0d, 1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_582() throws Exception {
        // Combination: a=0.0d, b=-1.0d, c=-1.0d
        Object actual = NumberUtils.max(0.0d, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_583() throws Exception {
        // Combination: a=0.0d, b=Double.NaN, c=Double.NaN
        Object actual = NumberUtils.max(0.0d, Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_584() throws Exception {
        // Combination: a=0.0d, b=Double.POSITIVE_INFINITY, c=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.max(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_585() throws Exception {
        // Combination: a=1.0d, b=0.0d, c=1.0d
        Object actual = NumberUtils.max(1.0d, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_586() throws Exception {
        // Combination: a=1.0d, b=1.0d, c=0.0d
        Object actual = NumberUtils.max(1.0d, 1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_587() throws Exception {
        // Combination: a=1.0d, b=-1.0d, c=Double.NaN
        Object actual = NumberUtils.max(1.0d, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_588() throws Exception {
        // Combination: a=1.0d, b=Double.NaN, c=-1.0d
        Object actual = NumberUtils.max(1.0d, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_589() throws Exception {
        // Combination: a=1.0d, b=Double.POSITIVE_INFINITY, c=0.0d
        Object actual = NumberUtils.max(1.0d, Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_590() throws Exception {
        // Combination: a=-1.0d, b=0.0d, c=-1.0d
        Object actual = NumberUtils.max(-1.0d, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_591() throws Exception {
        // Combination: a=-1.0d, b=1.0d, c=Double.NaN
        Object actual = NumberUtils.max(-1.0d, 1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_592() throws Exception {
        // Combination: a=-1.0d, b=-1.0d, c=0.0d
        Object actual = NumberUtils.max(-1.0d, -1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_593() throws Exception {
        // Combination: a=-1.0d, b=Double.NaN, c=1.0d
        Object actual = NumberUtils.max(-1.0d, Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_594() throws Exception {
        // Combination: a=-1.0d, b=Double.POSITIVE_INFINITY, c=1.0d
        Object actual = NumberUtils.max(-1.0d, Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_595() throws Exception {
        // Combination: a=Double.NaN, b=0.0d, c=Double.NaN
        Object actual = NumberUtils.max(Double.NaN, 0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_596() throws Exception {
        // Combination: a=Double.NaN, b=1.0d, c=-1.0d
        Object actual = NumberUtils.max(Double.NaN, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_597() throws Exception {
        // Combination: a=Double.NaN, b=-1.0d, c=1.0d
        Object actual = NumberUtils.max(Double.NaN, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_598() throws Exception {
        // Combination: a=Double.NaN, b=Double.NaN, c=0.0d
        Object actual = NumberUtils.max(Double.NaN, Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_599() throws Exception {
        // Combination: a=Double.NaN, b=Double.POSITIVE_INFINITY, c=-1.0d
        Object actual = NumberUtils.max(Double.NaN, Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_600() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=0.0d, c=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.max(Double.POSITIVE_INFINITY, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_601() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=1.0d, c=0.0d
        Object actual = NumberUtils.max(Double.POSITIVE_INFINITY, 1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_602() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=-1.0d, c=1.0d
        Object actual = NumberUtils.max(Double.POSITIVE_INFINITY, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_603() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.NaN, c=-1.0d
        Object actual = NumberUtils.max(Double.POSITIVE_INFINITY, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_604() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.POSITIVE_INFINITY, c=Double.NaN
        Object actual = NumberUtils.max(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_605() throws Exception {
        // Combination: a=1.0d, b=1.0d, c=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.max(1.0d, 1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_606() throws Exception {
        // Combination: a=-1.0d, b=-1.0d, c=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.max(-1.0d, -1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_607() throws Exception {
        // Combination: a=Double.NaN, b=Double.NaN, c=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.max(Double.NaN, Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_608() throws Exception {
        // Combination: a=0.0f, b=0.0f, c=0.0f
        Object actual = NumberUtils.max(0.0f, 0.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_609() throws Exception {
        // Combination: a=0.0f, b=1.0f, c=1.0f
        Object actual = NumberUtils.max(0.0f, 1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_610() throws Exception {
        // Combination: a=0.0f, b=-1.0f, c=-1.0f
        Object actual = NumberUtils.max(0.0f, -1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_611() throws Exception {
        // Combination: a=0.0f, b=Float.NaN, c=Float.NaN
        Object actual = NumberUtils.max(0.0f, Float.NaN, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_612() throws Exception {
        // Combination: a=0.0f, b=Float.POSITIVE_INFINITY, c=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.max(0.0f, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_613() throws Exception {
        // Combination: a=1.0f, b=0.0f, c=1.0f
        Object actual = NumberUtils.max(1.0f, 0.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_614() throws Exception {
        // Combination: a=1.0f, b=1.0f, c=0.0f
        Object actual = NumberUtils.max(1.0f, 1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_615() throws Exception {
        // Combination: a=1.0f, b=-1.0f, c=Float.NaN
        Object actual = NumberUtils.max(1.0f, -1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_616() throws Exception {
        // Combination: a=1.0f, b=Float.NaN, c=-1.0f
        Object actual = NumberUtils.max(1.0f, Float.NaN, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_617() throws Exception {
        // Combination: a=1.0f, b=Float.POSITIVE_INFINITY, c=0.0f
        Object actual = NumberUtils.max(1.0f, Float.POSITIVE_INFINITY, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_618() throws Exception {
        // Combination: a=-1.0f, b=0.0f, c=-1.0f
        Object actual = NumberUtils.max(-1.0f, 0.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_619() throws Exception {
        // Combination: a=-1.0f, b=1.0f, c=Float.NaN
        Object actual = NumberUtils.max(-1.0f, 1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_620() throws Exception {
        // Combination: a=-1.0f, b=-1.0f, c=0.0f
        Object actual = NumberUtils.max(-1.0f, -1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_621() throws Exception {
        // Combination: a=-1.0f, b=Float.NaN, c=1.0f
        Object actual = NumberUtils.max(-1.0f, Float.NaN, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_622() throws Exception {
        // Combination: a=-1.0f, b=Float.POSITIVE_INFINITY, c=1.0f
        Object actual = NumberUtils.max(-1.0f, Float.POSITIVE_INFINITY, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_623() throws Exception {
        // Combination: a=Float.NaN, b=0.0f, c=Float.NaN
        Object actual = NumberUtils.max(Float.NaN, 0.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_624() throws Exception {
        // Combination: a=Float.NaN, b=1.0f, c=-1.0f
        Object actual = NumberUtils.max(Float.NaN, 1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_625() throws Exception {
        // Combination: a=Float.NaN, b=-1.0f, c=1.0f
        Object actual = NumberUtils.max(Float.NaN, -1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_626() throws Exception {
        // Combination: a=Float.NaN, b=Float.NaN, c=0.0f
        Object actual = NumberUtils.max(Float.NaN, Float.NaN, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_627() throws Exception {
        // Combination: a=Float.NaN, b=Float.POSITIVE_INFINITY, c=-1.0f
        Object actual = NumberUtils.max(Float.NaN, Float.POSITIVE_INFINITY, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_628() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=0.0f, c=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.max(Float.POSITIVE_INFINITY, 0.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_629() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=1.0f, c=0.0f
        Object actual = NumberUtils.max(Float.POSITIVE_INFINITY, 1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_630() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=-1.0f, c=1.0f
        Object actual = NumberUtils.max(Float.POSITIVE_INFINITY, -1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_631() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=Float.NaN, c=-1.0f
        Object actual = NumberUtils.max(Float.POSITIVE_INFINITY, Float.NaN, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_632() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=Float.POSITIVE_INFINITY, c=Float.NaN
        Object actual = NumberUtils.max(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_633() throws Exception {
        // Combination: a=1.0f, b=1.0f, c=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.max(1.0f, 1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_634() throws Exception {
        // Combination: a=-1.0f, b=-1.0f, c=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.max(-1.0f, -1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_635() throws Exception {
        // Combination: a=Float.NaN, b=Float.NaN, c=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.max(Float.NaN, Float.NaN, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_636() throws Exception {
        // Combination: lhs=0.0d, rhs=0.0d
        Object actual = NumberUtils.compare(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_637() throws Exception {
        // Combination: lhs=0.0d, rhs=1.0d
        Object actual = NumberUtils.compare(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_638() throws Exception {
        // Combination: lhs=0.0d, rhs=-1.0d
        Object actual = NumberUtils.compare(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_639() throws Exception {
        // Combination: lhs=0.0d, rhs=Double.NaN
        Object actual = NumberUtils.compare(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_640() throws Exception {
        // Combination: lhs=0.0d, rhs=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_641() throws Exception {
        // Combination: lhs=1.0d, rhs=0.0d
        Object actual = NumberUtils.compare(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_642() throws Exception {
        // Combination: lhs=1.0d, rhs=1.0d
        Object actual = NumberUtils.compare(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_643() throws Exception {
        // Combination: lhs=1.0d, rhs=-1.0d
        Object actual = NumberUtils.compare(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_644() throws Exception {
        // Combination: lhs=1.0d, rhs=Double.NaN
        Object actual = NumberUtils.compare(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_645() throws Exception {
        // Combination: lhs=1.0d, rhs=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_646() throws Exception {
        // Combination: lhs=-1.0d, rhs=0.0d
        Object actual = NumberUtils.compare(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_647() throws Exception {
        // Combination: lhs=-1.0d, rhs=1.0d
        Object actual = NumberUtils.compare(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_648() throws Exception {
        // Combination: lhs=-1.0d, rhs=-1.0d
        Object actual = NumberUtils.compare(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_649() throws Exception {
        // Combination: lhs=-1.0d, rhs=Double.NaN
        Object actual = NumberUtils.compare(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_650() throws Exception {
        // Combination: lhs=-1.0d, rhs=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_651() throws Exception {
        // Combination: lhs=Double.NaN, rhs=0.0d
        Object actual = NumberUtils.compare(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_652() throws Exception {
        // Combination: lhs=Double.NaN, rhs=1.0d
        Object actual = NumberUtils.compare(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_653() throws Exception {
        // Combination: lhs=Double.NaN, rhs=-1.0d
        Object actual = NumberUtils.compare(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_654() throws Exception {
        // Combination: lhs=Double.NaN, rhs=Double.NaN
        Object actual = NumberUtils.compare(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_655() throws Exception {
        // Combination: lhs=Double.NaN, rhs=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_656() throws Exception {
        // Combination: lhs=Double.POSITIVE_INFINITY, rhs=0.0d
        Object actual = NumberUtils.compare(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_657() throws Exception {
        // Combination: lhs=Double.POSITIVE_INFINITY, rhs=1.0d
        Object actual = NumberUtils.compare(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_658() throws Exception {
        // Combination: lhs=Double.POSITIVE_INFINITY, rhs=-1.0d
        Object actual = NumberUtils.compare(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_659() throws Exception {
        // Combination: lhs=Double.POSITIVE_INFINITY, rhs=Double.NaN
        Object actual = NumberUtils.compare(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_660() throws Exception {
        // Combination: lhs=Double.POSITIVE_INFINITY, rhs=Double.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_661() throws Exception {
        // Combination: lhs=0.0f, rhs=0.0f
        Object actual = NumberUtils.compare(0.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_662() throws Exception {
        // Combination: lhs=0.0f, rhs=1.0f
        Object actual = NumberUtils.compare(0.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_663() throws Exception {
        // Combination: lhs=0.0f, rhs=-1.0f
        Object actual = NumberUtils.compare(0.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_664() throws Exception {
        // Combination: lhs=0.0f, rhs=Float.NaN
        Object actual = NumberUtils.compare(0.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_665() throws Exception {
        // Combination: lhs=0.0f, rhs=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(0.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_666() throws Exception {
        // Combination: lhs=1.0f, rhs=0.0f
        Object actual = NumberUtils.compare(1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_667() throws Exception {
        // Combination: lhs=1.0f, rhs=1.0f
        Object actual = NumberUtils.compare(1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_668() throws Exception {
        // Combination: lhs=1.0f, rhs=-1.0f
        Object actual = NumberUtils.compare(1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_669() throws Exception {
        // Combination: lhs=1.0f, rhs=Float.NaN
        Object actual = NumberUtils.compare(1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_670() throws Exception {
        // Combination: lhs=1.0f, rhs=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_671() throws Exception {
        // Combination: lhs=-1.0f, rhs=0.0f
        Object actual = NumberUtils.compare(-1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_672() throws Exception {
        // Combination: lhs=-1.0f, rhs=1.0f
        Object actual = NumberUtils.compare(-1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_673() throws Exception {
        // Combination: lhs=-1.0f, rhs=-1.0f
        Object actual = NumberUtils.compare(-1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_674() throws Exception {
        // Combination: lhs=-1.0f, rhs=Float.NaN
        Object actual = NumberUtils.compare(-1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_675() throws Exception {
        // Combination: lhs=-1.0f, rhs=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(-1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_676() throws Exception {
        // Combination: lhs=Float.NaN, rhs=0.0f
        Object actual = NumberUtils.compare(Float.NaN, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_677() throws Exception {
        // Combination: lhs=Float.NaN, rhs=1.0f
        Object actual = NumberUtils.compare(Float.NaN, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_678() throws Exception {
        // Combination: lhs=Float.NaN, rhs=-1.0f
        Object actual = NumberUtils.compare(Float.NaN, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_679() throws Exception {
        // Combination: lhs=Float.NaN, rhs=Float.NaN
        Object actual = NumberUtils.compare(Float.NaN, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_680() throws Exception {
        // Combination: lhs=Float.NaN, rhs=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(Float.NaN, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_681() throws Exception {
        // Combination: lhs=Float.POSITIVE_INFINITY, rhs=0.0f
        Object actual = NumberUtils.compare(Float.POSITIVE_INFINITY, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_682() throws Exception {
        // Combination: lhs=Float.POSITIVE_INFINITY, rhs=1.0f
        Object actual = NumberUtils.compare(Float.POSITIVE_INFINITY, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_683() throws Exception {
        // Combination: lhs=Float.POSITIVE_INFINITY, rhs=-1.0f
        Object actual = NumberUtils.compare(Float.POSITIVE_INFINITY, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_684() throws Exception {
        // Combination: lhs=Float.POSITIVE_INFINITY, rhs=Float.NaN
        Object actual = NumberUtils.compare(Float.POSITIVE_INFINITY, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compare_pairwise_685() throws Exception {
        // Combination: lhs=Float.POSITIVE_INFINITY, rhs=Float.POSITIVE_INFINITY
        Object actual = NumberUtils.compare(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

}
