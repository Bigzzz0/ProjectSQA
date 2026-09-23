package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for WordUtils.
 */
public class WordUtils_IPOTest {
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
    public void test_wrap_pairwise_001() throws Exception {
        // Combination: str="", wrapLength=0
        Object actual = WordUtils.wrap("", 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_002() throws Exception {
        // Combination: str="", wrapLength=1
        Object actual = WordUtils.wrap("", 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_003() throws Exception {
        // Combination: str="", wrapLength=-1
        Object actual = WordUtils.wrap("", -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_004() throws Exception {
        // Combination: str="", wrapLength=Integer.MAX_VALUE
        Object actual = WordUtils.wrap("", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_005() throws Exception {
        // Combination: str="", wrapLength=Integer.MIN_VALUE
        Object actual = WordUtils.wrap("", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_006() throws Exception {
        // Combination: str=" ", wrapLength=0
        Object actual = WordUtils.wrap(" ", 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_007() throws Exception {
        // Combination: str=" ", wrapLength=1
        Object actual = WordUtils.wrap(" ", 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_008() throws Exception {
        // Combination: str=" ", wrapLength=-1
        Object actual = WordUtils.wrap(" ", -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_009() throws Exception {
        // Combination: str=" ", wrapLength=Integer.MAX_VALUE
        Object actual = WordUtils.wrap(" ", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_010() throws Exception {
        // Combination: str=" ", wrapLength=Integer.MIN_VALUE
        Object actual = WordUtils.wrap(" ", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_011() throws Exception {
        // Combination: str="a", wrapLength=0
        Object actual = WordUtils.wrap("a", 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_012() throws Exception {
        // Combination: str="a", wrapLength=1
        Object actual = WordUtils.wrap("a", 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_013() throws Exception {
        // Combination: str="a", wrapLength=-1
        Object actual = WordUtils.wrap("a", -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_014() throws Exception {
        // Combination: str="a", wrapLength=Integer.MAX_VALUE
        Object actual = WordUtils.wrap("a", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_015() throws Exception {
        // Combination: str="a", wrapLength=Integer.MIN_VALUE
        Object actual = WordUtils.wrap("a", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_016() throws Exception {
        // Combination: str="test123", wrapLength=0
        Object actual = WordUtils.wrap("test123", 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_017() throws Exception {
        // Combination: str="test123", wrapLength=1
        Object actual = WordUtils.wrap("test123", 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_018() throws Exception {
        // Combination: str="test123", wrapLength=-1
        Object actual = WordUtils.wrap("test123", -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_019() throws Exception {
        // Combination: str="test123", wrapLength=Integer.MAX_VALUE
        Object actual = WordUtils.wrap("test123", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_020() throws Exception {
        // Combination: str="test123", wrapLength=Integer.MIN_VALUE
        Object actual = WordUtils.wrap("test123", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_021() throws Exception {
        // Combination: str="!@#", wrapLength=0
        Object actual = WordUtils.wrap("!@#", 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_022() throws Exception {
        // Combination: str="!@#", wrapLength=1
        Object actual = WordUtils.wrap("!@#", 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_023() throws Exception {
        // Combination: str="!@#", wrapLength=-1
        Object actual = WordUtils.wrap("!@#", -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_024() throws Exception {
        // Combination: str="!@#", wrapLength=Integer.MAX_VALUE
        Object actual = WordUtils.wrap("!@#", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_025() throws Exception {
        // Combination: str="!@#", wrapLength=Integer.MIN_VALUE
        Object actual = WordUtils.wrap("!@#", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_026() throws Exception {
        // Combination: str="0", wrapLength=0
        Object actual = WordUtils.wrap("0", 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_027() throws Exception {
        // Combination: str="0", wrapLength=1
        Object actual = WordUtils.wrap("0", 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_028() throws Exception {
        // Combination: str="0", wrapLength=-1
        Object actual = WordUtils.wrap("0", -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_029() throws Exception {
        // Combination: str="0", wrapLength=Integer.MAX_VALUE
        Object actual = WordUtils.wrap("0", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_030() throws Exception {
        // Combination: str="0", wrapLength=Integer.MIN_VALUE
        Object actual = WordUtils.wrap("0", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_031() throws Exception {
        // Combination: str="-1", wrapLength=0
        Object actual = WordUtils.wrap("-1", 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_032() throws Exception {
        // Combination: str="-1", wrapLength=1
        Object actual = WordUtils.wrap("-1", 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_033() throws Exception {
        // Combination: str="-1", wrapLength=-1
        Object actual = WordUtils.wrap("-1", -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_034() throws Exception {
        // Combination: str="-1", wrapLength=Integer.MAX_VALUE
        Object actual = WordUtils.wrap("-1", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_035() throws Exception {
        // Combination: str="-1", wrapLength=Integer.MIN_VALUE
        Object actual = WordUtils.wrap("-1", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_036() throws Exception {
        // Combination: str="1.5", wrapLength=0
        Object actual = WordUtils.wrap("1.5", 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_037() throws Exception {
        // Combination: str="1.5", wrapLength=1
        Object actual = WordUtils.wrap("1.5", 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_038() throws Exception {
        // Combination: str="1.5", wrapLength=-1
        Object actual = WordUtils.wrap("1.5", -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_039() throws Exception {
        // Combination: str="1.5", wrapLength=Integer.MAX_VALUE
        Object actual = WordUtils.wrap("1.5", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_040() throws Exception {
        // Combination: str="1.5", wrapLength=Integer.MIN_VALUE
        Object actual = WordUtils.wrap("1.5", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_041() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=0
        Object actual = WordUtils.wrap("9223372036854775807", 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_042() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=1
        Object actual = WordUtils.wrap("9223372036854775807", 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_043() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=-1
        Object actual = WordUtils.wrap("9223372036854775807", -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_044() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=Integer.MAX_VALUE
        Object actual = WordUtils.wrap("9223372036854775807", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_045() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=Integer.MIN_VALUE
        Object actual = WordUtils.wrap("9223372036854775807", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_046() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=0
        Object actual = WordUtils.wrap("9223372036854775808", 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_047() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=1
        Object actual = WordUtils.wrap("9223372036854775808", 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_048() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=-1
        Object actual = WordUtils.wrap("9223372036854775808", -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_049() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=Integer.MAX_VALUE
        Object actual = WordUtils.wrap("9223372036854775808", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_050() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=Integer.MIN_VALUE
        Object actual = WordUtils.wrap("9223372036854775808", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_051() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=0
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_052() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=1
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_053() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=-1
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_054() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=Integer.MAX_VALUE
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_055() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=Integer.MIN_VALUE
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_056() throws Exception {
        // Combination: str="", wrapLength=0, newLineStr="", wrapLongWords=true
        Object actual = WordUtils.wrap("", 0, "", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_057() throws Exception {
        // Combination: str=" ", wrapLength=1, newLineStr="", wrapLongWords=false
        Object actual = WordUtils.wrap(" ", 1, "", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_058() throws Exception {
        // Combination: str="a", wrapLength=-1, newLineStr="", wrapLongWords=true
        Object actual = WordUtils.wrap("a", -1, "", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_059() throws Exception {
        // Combination: str="test123", wrapLength=Integer.MAX_VALUE, newLineStr="", wrapLongWords=true
        Object actual = WordUtils.wrap("test123", Integer.MAX_VALUE, "", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_060() throws Exception {
        // Combination: str="!@#", wrapLength=Integer.MIN_VALUE, newLineStr="", wrapLongWords=true
        Object actual = WordUtils.wrap("!@#", Integer.MIN_VALUE, "", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_061() throws Exception {
        // Combination: str="0", wrapLength=0, newLineStr="", wrapLongWords=false
        Object actual = WordUtils.wrap("0", 0, "", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_062() throws Exception {
        // Combination: str="-1", wrapLength=0, newLineStr="", wrapLongWords=true
        Object actual = WordUtils.wrap("-1", 0, "", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_063() throws Exception {
        // Combination: str="1.5", wrapLength=0, newLineStr="", wrapLongWords=true
        Object actual = WordUtils.wrap("1.5", 0, "", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_064() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=0, newLineStr="", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775807", 0, "", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_065() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=0, newLineStr="", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775808", 0, "", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_066() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=0, newLineStr="", wrapLongWords=true
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, "", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_067() throws Exception {
        // Combination: str="", wrapLength=1, newLineStr=" ", wrapLongWords=true
        Object actual = WordUtils.wrap("", 1, " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_068() throws Exception {
        // Combination: str=" ", wrapLength=0, newLineStr=" ", wrapLongWords=true
        Object actual = WordUtils.wrap(" ", 0, " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_069() throws Exception {
        // Combination: str="a", wrapLength=Integer.MAX_VALUE, newLineStr=" ", wrapLongWords=false
        Object actual = WordUtils.wrap("a", Integer.MAX_VALUE, " ", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_070() throws Exception {
        // Combination: str="test123", wrapLength=-1, newLineStr=" ", wrapLongWords=false
        Object actual = WordUtils.wrap("test123", -1, " ", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_071() throws Exception {
        // Combination: str="!@#", wrapLength=0, newLineStr=" ", wrapLongWords=false
        Object actual = WordUtils.wrap("!@#", 0, " ", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_072() throws Exception {
        // Combination: str="0", wrapLength=Integer.MIN_VALUE, newLineStr=" ", wrapLongWords=true
        Object actual = WordUtils.wrap("0", Integer.MIN_VALUE, " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_073() throws Exception {
        // Combination: str="-1", wrapLength=1, newLineStr=" ", wrapLongWords=false
        Object actual = WordUtils.wrap("-1", 1, " ", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_074() throws Exception {
        // Combination: str="1.5", wrapLength=1, newLineStr=" ", wrapLongWords=false
        Object actual = WordUtils.wrap("1.5", 1, " ", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_075() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=1, newLineStr=" ", wrapLongWords=false
        Object actual = WordUtils.wrap("9223372036854775807", 1, " ", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_076() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=1, newLineStr=" ", wrapLongWords=false
        Object actual = WordUtils.wrap("9223372036854775808", 1, " ", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_077() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=1, newLineStr=" ", wrapLongWords=false
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, " ", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_078() throws Exception {
        // Combination: str="", wrapLength=-1, newLineStr="a", wrapLongWords=false
        Object actual = WordUtils.wrap("", -1, "a", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_079() throws Exception {
        // Combination: str=" ", wrapLength=Integer.MAX_VALUE, newLineStr="a", wrapLongWords=true
        Object actual = WordUtils.wrap(" ", Integer.MAX_VALUE, "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_080() throws Exception {
        // Combination: str="a", wrapLength=0, newLineStr="a", wrapLongWords=true
        Object actual = WordUtils.wrap("a", 0, "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_081() throws Exception {
        // Combination: str="test123", wrapLength=1, newLineStr="a", wrapLongWords=true
        Object actual = WordUtils.wrap("test123", 1, "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("taeasata1a2a3", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_082() throws Exception {
        // Combination: str="!@#", wrapLength=1, newLineStr="a", wrapLongWords=true
        Object actual = WordUtils.wrap("!@#", 1, "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!a@a#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_083() throws Exception {
        // Combination: str="0", wrapLength=1, newLineStr="a", wrapLongWords=true
        Object actual = WordUtils.wrap("0", 1, "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_084() throws Exception {
        // Combination: str="-1", wrapLength=Integer.MIN_VALUE, newLineStr="a", wrapLongWords=false
        Object actual = WordUtils.wrap("-1", Integer.MIN_VALUE, "a", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_085() throws Exception {
        // Combination: str="1.5", wrapLength=-1, newLineStr="a", wrapLongWords=true
        Object actual = WordUtils.wrap("1.5", -1, "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1a.a5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_086() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=-1, newLineStr="a", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775807", -1, "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9a2a2a3a3a7a2a0a3a6a8a5a4a7a7a5a8a0a7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_087() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=-1, newLineStr="a", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775808", -1, "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9a2a2a3a3a7a2a0a3a6a8a5a4a7a7a5a8a0a8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_088() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=-1, newLineStr="a", wrapLongWords=true
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_089() throws Exception {
        // Combination: str="", wrapLength=Integer.MAX_VALUE, newLineStr="test123", wrapLongWords=true
        Object actual = WordUtils.wrap("", Integer.MAX_VALUE, "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_090() throws Exception {
        // Combination: str=" ", wrapLength=-1, newLineStr="test123", wrapLongWords=false
        Object actual = WordUtils.wrap(" ", -1, "test123", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_091() throws Exception {
        // Combination: str="a", wrapLength=1, newLineStr="test123", wrapLongWords=true
        Object actual = WordUtils.wrap("a", 1, "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_092() throws Exception {
        // Combination: str="test123", wrapLength=0, newLineStr="test123", wrapLongWords=true
        Object actual = WordUtils.wrap("test123", 0, "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("ttest123etest123stest123ttest1231test1232test1233", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_093() throws Exception {
        // Combination: str="!@#", wrapLength=-1, newLineStr="test123", wrapLongWords=true
        Object actual = WordUtils.wrap("!@#", -1, "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!test123@test123#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_094() throws Exception {
        // Combination: str="0", wrapLength=-1, newLineStr="test123", wrapLongWords=true
        Object actual = WordUtils.wrap("0", -1, "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_095() throws Exception {
        // Combination: str="-1", wrapLength=-1, newLineStr="test123", wrapLongWords=true
        Object actual = WordUtils.wrap("-1", -1, "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-test1231", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_096() throws Exception {
        // Combination: str="1.5", wrapLength=Integer.MIN_VALUE, newLineStr="test123", wrapLongWords=true
        Object actual = WordUtils.wrap("1.5", Integer.MIN_VALUE, "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1test123.test1235", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_097() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=Integer.MAX_VALUE, newLineStr="test123", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775807", Integer.MAX_VALUE, "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_098() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=Integer.MAX_VALUE, newLineStr="test123", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775808", Integer.MAX_VALUE, "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_099() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=Integer.MAX_VALUE, newLineStr="test123", wrapLongWords=true
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_100() throws Exception {
        // Combination: str="", wrapLength=Integer.MIN_VALUE, newLineStr="!@#", wrapLongWords=true
        Object actual = WordUtils.wrap("", Integer.MIN_VALUE, "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_101() throws Exception {
        // Combination: str=" ", wrapLength=0, newLineStr="!@#", wrapLongWords=false
        Object actual = WordUtils.wrap(" ", 0, "!@#", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_102() throws Exception {
        // Combination: str="a", wrapLength=1, newLineStr="!@#", wrapLongWords=true
        Object actual = WordUtils.wrap("a", 1, "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_103() throws Exception {
        // Combination: str="test123", wrapLength=-1, newLineStr="!@#", wrapLongWords=true
        Object actual = WordUtils.wrap("test123", -1, "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("t!@#e!@#s!@#t!@#1!@#2!@#3", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_104() throws Exception {
        // Combination: str="!@#", wrapLength=Integer.MAX_VALUE, newLineStr="!@#", wrapLongWords=true
        Object actual = WordUtils.wrap("!@#", Integer.MAX_VALUE, "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_105() throws Exception {
        // Combination: str="0", wrapLength=Integer.MAX_VALUE, newLineStr="!@#", wrapLongWords=true
        Object actual = WordUtils.wrap("0", Integer.MAX_VALUE, "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_106() throws Exception {
        // Combination: str="-1", wrapLength=Integer.MAX_VALUE, newLineStr="!@#", wrapLongWords=true
        Object actual = WordUtils.wrap("-1", Integer.MAX_VALUE, "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_107() throws Exception {
        // Combination: str="1.5", wrapLength=Integer.MAX_VALUE, newLineStr="!@#", wrapLongWords=true
        Object actual = WordUtils.wrap("1.5", Integer.MAX_VALUE, "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_108() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=Integer.MIN_VALUE, newLineStr="!@#", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775807", Integer.MIN_VALUE, "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9!@#2!@#2!@#3!@#3!@#7!@#2!@#0!@#3!@#6!@#8!@#5!@#4!@#7!@#7!@#5!@#8!@#0!@#7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_109() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=Integer.MIN_VALUE, newLineStr="!@#", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775808", Integer.MIN_VALUE, "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9!@#2!@#2!@#3!@#3!@#7!@#2!@#0!@#3!@#6!@#8!@#5!@#4!@#7!@#7!@#5!@#8!@#0!@#8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_110() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=Integer.MIN_VALUE, newLineStr="!@#", wrapLongWords=true
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a!@#a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_111() throws Exception {
        // Combination: str="", wrapLength=0, newLineStr="0", wrapLongWords=true
        Object actual = WordUtils.wrap("", 0, "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_112() throws Exception {
        // Combination: str=" ", wrapLength=Integer.MIN_VALUE, newLineStr="0", wrapLongWords=false
        Object actual = WordUtils.wrap(" ", Integer.MIN_VALUE, "0", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_113() throws Exception {
        // Combination: str="a", wrapLength=1, newLineStr="0", wrapLongWords=true
        Object actual = WordUtils.wrap("a", 1, "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_114() throws Exception {
        // Combination: str="test123", wrapLength=-1, newLineStr="0", wrapLongWords=true
        Object actual = WordUtils.wrap("test123", -1, "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("t0e0s0t010203", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_115() throws Exception {
        // Combination: str="!@#", wrapLength=Integer.MAX_VALUE, newLineStr="0", wrapLongWords=true
        Object actual = WordUtils.wrap("!@#", Integer.MAX_VALUE, "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_116() throws Exception {
        // Combination: str="0", wrapLength=0, newLineStr="0", wrapLongWords=true
        Object actual = WordUtils.wrap("0", 0, "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_117() throws Exception {
        // Combination: str="-1", wrapLength=0, newLineStr="0", wrapLongWords=true
        Object actual = WordUtils.wrap("-1", 0, "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-01", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_118() throws Exception {
        // Combination: str="1.5", wrapLength=0, newLineStr="0", wrapLongWords=true
        Object actual = WordUtils.wrap("1.5", 0, "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("10.05", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_119() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=0, newLineStr="0", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775807", 0, "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9020203030702000306080504070705080007", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_120() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=0, newLineStr="0", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775808", 0, "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9020203030702000306080504070705080008", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_121() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=0, newLineStr="0", wrapLongWords=true
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_122() throws Exception {
        // Combination: str="", wrapLength=0, newLineStr="-1", wrapLongWords=true
        Object actual = WordUtils.wrap("", 0, "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_123() throws Exception {
        // Combination: str=" ", wrapLength=1, newLineStr="-1", wrapLongWords=false
        Object actual = WordUtils.wrap(" ", 1, "-1", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_124() throws Exception {
        // Combination: str="a", wrapLength=Integer.MIN_VALUE, newLineStr="-1", wrapLongWords=true
        Object actual = WordUtils.wrap("a", Integer.MIN_VALUE, "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_125() throws Exception {
        // Combination: str="test123", wrapLength=-1, newLineStr="-1", wrapLongWords=true
        Object actual = WordUtils.wrap("test123", -1, "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("t-1e-1s-1t-11-12-13", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_126() throws Exception {
        // Combination: str="!@#", wrapLength=Integer.MAX_VALUE, newLineStr="-1", wrapLongWords=true
        Object actual = WordUtils.wrap("!@#", Integer.MAX_VALUE, "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_127() throws Exception {
        // Combination: str="0", wrapLength=0, newLineStr="-1", wrapLongWords=true
        Object actual = WordUtils.wrap("0", 0, "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_128() throws Exception {
        // Combination: str="-1", wrapLength=0, newLineStr="-1", wrapLongWords=true
        Object actual = WordUtils.wrap("-1", 0, "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("--11", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_129() throws Exception {
        // Combination: str="1.5", wrapLength=0, newLineStr="-1", wrapLongWords=true
        Object actual = WordUtils.wrap("1.5", 0, "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1-1.-15", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_130() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=0, newLineStr="-1", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775807", 0, "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9-12-12-13-13-17-12-10-13-16-18-15-14-17-17-15-18-10-17", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_131() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=0, newLineStr="-1", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775808", 0, "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9-12-12-13-13-17-12-10-13-16-18-15-14-17-17-15-18-10-18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_132() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=0, newLineStr="-1", wrapLongWords=true
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a-1a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_133() throws Exception {
        // Combination: str="", wrapLength=0, newLineStr="1.5", wrapLongWords=true
        Object actual = WordUtils.wrap("", 0, "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_134() throws Exception {
        // Combination: str=" ", wrapLength=1, newLineStr="1.5", wrapLongWords=false
        Object actual = WordUtils.wrap(" ", 1, "1.5", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_135() throws Exception {
        // Combination: str="a", wrapLength=-1, newLineStr="1.5", wrapLongWords=true
        Object actual = WordUtils.wrap("a", -1, "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_136() throws Exception {
        // Combination: str="test123", wrapLength=Integer.MIN_VALUE, newLineStr="1.5", wrapLongWords=true
        Object actual = WordUtils.wrap("test123", Integer.MIN_VALUE, "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("t1.5e1.5s1.5t1.511.521.53", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_137() throws Exception {
        // Combination: str="!@#", wrapLength=Integer.MAX_VALUE, newLineStr="1.5", wrapLongWords=true
        Object actual = WordUtils.wrap("!@#", Integer.MAX_VALUE, "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_138() throws Exception {
        // Combination: str="0", wrapLength=0, newLineStr="1.5", wrapLongWords=true
        Object actual = WordUtils.wrap("0", 0, "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_139() throws Exception {
        // Combination: str="-1", wrapLength=0, newLineStr="1.5", wrapLongWords=true
        Object actual = WordUtils.wrap("-1", 0, "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1.51", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_140() throws Exception {
        // Combination: str="1.5", wrapLength=0, newLineStr="1.5", wrapLongWords=true
        Object actual = WordUtils.wrap("1.5", 0, "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("11.5.1.55", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_141() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=0, newLineStr="1.5", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775807", 0, "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("91.521.521.531.531.571.521.501.531.561.581.551.541.571.571.551.581.501.57", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_142() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=0, newLineStr="1.5", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775808", 0, "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("91.521.521.531.531.571.521.501.531.561.581.551.541.571.571.551.581.501.58", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_143() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=0, newLineStr="1.5", wrapLongWords=true
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a1.5a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_144() throws Exception {
        // Combination: str="", wrapLength=0, newLineStr="9223372036854775807", wrapLongWords=true
        Object actual = WordUtils.wrap("", 0, "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_145() throws Exception {
        // Combination: str=" ", wrapLength=1, newLineStr="9223372036854775807", wrapLongWords=false
        Object actual = WordUtils.wrap(" ", 1, "9223372036854775807", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_146() throws Exception {
        // Combination: str="a", wrapLength=-1, newLineStr="9223372036854775807", wrapLongWords=true
        Object actual = WordUtils.wrap("a", -1, "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_147() throws Exception {
        // Combination: str="test123", wrapLength=Integer.MAX_VALUE, newLineStr="9223372036854775807", wrapLongWords=true
        Object actual = WordUtils.wrap("test123", Integer.MAX_VALUE, "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_148() throws Exception {
        // Combination: str="!@#", wrapLength=Integer.MIN_VALUE, newLineStr="9223372036854775807", wrapLongWords=true
        Object actual = WordUtils.wrap("!@#", Integer.MIN_VALUE, "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!9223372036854775807@9223372036854775807#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_149() throws Exception {
        // Combination: str="0", wrapLength=0, newLineStr="9223372036854775807", wrapLongWords=true
        Object actual = WordUtils.wrap("0", 0, "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_150() throws Exception {
        // Combination: str="-1", wrapLength=0, newLineStr="9223372036854775807", wrapLongWords=true
        Object actual = WordUtils.wrap("-1", 0, "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-92233720368547758071", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_151() throws Exception {
        // Combination: str="1.5", wrapLength=0, newLineStr="9223372036854775807", wrapLongWords=true
        Object actual = WordUtils.wrap("1.5", 0, "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("19223372036854775807.92233720368547758075", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_152() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=0, newLineStr="9223372036854775807", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775807", 0, "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9922337203685477580729223372036854775807292233720368547758073922337203685477580739223372036854775807792233720368547758072922337203685477580709223372036854775807392233720368547758076922337203685477580789223372036854775807592233720368547758074922337203685477580779223372036854775807792233720368547758075922337203685477580789223372036854775807092233720368547758077", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_153() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=0, newLineStr="9223372036854775807", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775808", 0, "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9922337203685477580729223372036854775807292233720368547758073922337203685477580739223372036854775807792233720368547758072922337203685477580709223372036854775807392233720368547758076922337203685477580789223372036854775807592233720368547758074922337203685477580779223372036854775807792233720368547758075922337203685477580789223372036854775807092233720368547758078", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_154() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=0, newLineStr="9223372036854775807", wrapLongWords=true
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a9223372036854775807a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_155() throws Exception {
        // Combination: str="", wrapLength=0, newLineStr="9223372036854775808", wrapLongWords=true
        Object actual = WordUtils.wrap("", 0, "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_156() throws Exception {
        // Combination: str=" ", wrapLength=1, newLineStr="9223372036854775808", wrapLongWords=false
        Object actual = WordUtils.wrap(" ", 1, "9223372036854775808", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_157() throws Exception {
        // Combination: str="a", wrapLength=-1, newLineStr="9223372036854775808", wrapLongWords=true
        Object actual = WordUtils.wrap("a", -1, "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_158() throws Exception {
        // Combination: str="test123", wrapLength=Integer.MAX_VALUE, newLineStr="9223372036854775808", wrapLongWords=true
        Object actual = WordUtils.wrap("test123", Integer.MAX_VALUE, "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_159() throws Exception {
        // Combination: str="!@#", wrapLength=Integer.MIN_VALUE, newLineStr="9223372036854775808", wrapLongWords=true
        Object actual = WordUtils.wrap("!@#", Integer.MIN_VALUE, "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!9223372036854775808@9223372036854775808#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_160() throws Exception {
        // Combination: str="0", wrapLength=0, newLineStr="9223372036854775808", wrapLongWords=true
        Object actual = WordUtils.wrap("0", 0, "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_161() throws Exception {
        // Combination: str="-1", wrapLength=0, newLineStr="9223372036854775808", wrapLongWords=true
        Object actual = WordUtils.wrap("-1", 0, "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-92233720368547758081", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_162() throws Exception {
        // Combination: str="1.5", wrapLength=0, newLineStr="9223372036854775808", wrapLongWords=true
        Object actual = WordUtils.wrap("1.5", 0, "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("19223372036854775808.92233720368547758085", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_163() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=0, newLineStr="9223372036854775808", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775807", 0, "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9922337203685477580829223372036854775808292233720368547758083922337203685477580839223372036854775808792233720368547758082922337203685477580809223372036854775808392233720368547758086922337203685477580889223372036854775808592233720368547758084922337203685477580879223372036854775808792233720368547758085922337203685477580889223372036854775808092233720368547758087", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_164() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=0, newLineStr="9223372036854775808", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775808", 0, "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9922337203685477580829223372036854775808292233720368547758083922337203685477580839223372036854775808792233720368547758082922337203685477580809223372036854775808392233720368547758086922337203685477580889223372036854775808592233720368547758084922337203685477580879223372036854775808792233720368547758085922337203685477580889223372036854775808092233720368547758088", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_165() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=0, newLineStr="9223372036854775808", wrapLongWords=true
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a9223372036854775808a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_166() throws Exception {
        // Combination: str="", wrapLength=0, newLineStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLongWords=true
        Object actual = WordUtils.wrap("", 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_167() throws Exception {
        // Combination: str=" ", wrapLength=1, newLineStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLongWords=false
        Object actual = WordUtils.wrap(" ", 1, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_168() throws Exception {
        // Combination: str="a", wrapLength=-1, newLineStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLongWords=true
        Object actual = WordUtils.wrap("a", -1, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_169() throws Exception {
        // Combination: str="test123", wrapLength=Integer.MAX_VALUE, newLineStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLongWords=true
        Object actual = WordUtils.wrap("test123", Integer.MAX_VALUE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_170() throws Exception {
        // Combination: str="!@#", wrapLength=Integer.MIN_VALUE, newLineStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLongWords=true
        Object actual = WordUtils.wrap("!@#", Integer.MIN_VALUE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_171() throws Exception {
        // Combination: str="0", wrapLength=0, newLineStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLongWords=true
        Object actual = WordUtils.wrap("0", 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_172() throws Exception {
        // Combination: str="-1", wrapLength=0, newLineStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLongWords=true
        Object actual = WordUtils.wrap("-1", 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_173() throws Exception {
        // Combination: str="1.5", wrapLength=0, newLineStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLongWords=true
        Object actual = WordUtils.wrap("1.5", 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_174() throws Exception {
        // Combination: str="9223372036854775807", wrapLength=0, newLineStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775807", 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa7aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa6aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa8aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa7aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa7aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa8aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_175() throws Exception {
        // Combination: str="9223372036854775808", wrapLength=0, newLineStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLongWords=true
        Object actual = WordUtils.wrap("9223372036854775808", 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa7aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa6aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa8aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa7aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa7aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa8aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_wrap_pairwise_176() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLength=0, newLineStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", wrapLongWords=true
        Object actual = WordUtils.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_177() throws Exception {
        // Combination: str="", delimiters=new char[] {}
        Object actual = WordUtils.capitalize("", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_178() throws Exception {
        // Combination: str=" ", delimiters=new char[] {}
        Object actual = WordUtils.capitalize(" ", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_179() throws Exception {
        // Combination: str="a", delimiters=new char[] {}
        Object actual = WordUtils.capitalize("a", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_180() throws Exception {
        // Combination: str="test123", delimiters=new char[] {}
        Object actual = WordUtils.capitalize("test123", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_181() throws Exception {
        // Combination: str="!@#", delimiters=new char[] {}
        Object actual = WordUtils.capitalize("!@#", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_182() throws Exception {
        // Combination: str="0", delimiters=new char[] {}
        Object actual = WordUtils.capitalize("0", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_183() throws Exception {
        // Combination: str="-1", delimiters=new char[] {}
        Object actual = WordUtils.capitalize("-1", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_184() throws Exception {
        // Combination: str="1.5", delimiters=new char[] {}
        Object actual = WordUtils.capitalize("1.5", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_185() throws Exception {
        // Combination: str="9223372036854775807", delimiters=new char[] {}
        Object actual = WordUtils.capitalize("9223372036854775807", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_186() throws Exception {
        // Combination: str="9223372036854775808", delimiters=new char[] {}
        Object actual = WordUtils.capitalize("9223372036854775808", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_187() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiters=new char[] {}
        Object actual = WordUtils.capitalize("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_188() throws Exception {
        // Combination: str="", delimiters=new char[] {1}
        Object actual = WordUtils.capitalize("", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_189() throws Exception {
        // Combination: str=" ", delimiters=new char[] {1}
        Object actual = WordUtils.capitalize(" ", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_190() throws Exception {
        // Combination: str="a", delimiters=new char[] {1}
        Object actual = WordUtils.capitalize("a", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("A", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_191() throws Exception {
        // Combination: str="test123", delimiters=new char[] {1}
        Object actual = WordUtils.capitalize("test123", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_192() throws Exception {
        // Combination: str="!@#", delimiters=new char[] {1}
        Object actual = WordUtils.capitalize("!@#", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_193() throws Exception {
        // Combination: str="0", delimiters=new char[] {1}
        Object actual = WordUtils.capitalize("0", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_194() throws Exception {
        // Combination: str="-1", delimiters=new char[] {1}
        Object actual = WordUtils.capitalize("-1", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_195() throws Exception {
        // Combination: str="1.5", delimiters=new char[] {1}
        Object actual = WordUtils.capitalize("1.5", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_196() throws Exception {
        // Combination: str="9223372036854775807", delimiters=new char[] {1}
        Object actual = WordUtils.capitalize("9223372036854775807", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_197() throws Exception {
        // Combination: str="9223372036854775808", delimiters=new char[] {1}
        Object actual = WordUtils.capitalize("9223372036854775808", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalize_pairwise_198() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiters=new char[] {1}
        Object actual = WordUtils.capitalize("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_199() throws Exception {
        // Combination: str="", delimiters=new char[] {}
        Object actual = WordUtils.capitalizeFully("", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_200() throws Exception {
        // Combination: str=" ", delimiters=new char[] {}
        Object actual = WordUtils.capitalizeFully(" ", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_201() throws Exception {
        // Combination: str="a", delimiters=new char[] {}
        Object actual = WordUtils.capitalizeFully("a", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_202() throws Exception {
        // Combination: str="test123", delimiters=new char[] {}
        Object actual = WordUtils.capitalizeFully("test123", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_203() throws Exception {
        // Combination: str="!@#", delimiters=new char[] {}
        Object actual = WordUtils.capitalizeFully("!@#", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_204() throws Exception {
        // Combination: str="0", delimiters=new char[] {}
        Object actual = WordUtils.capitalizeFully("0", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_205() throws Exception {
        // Combination: str="-1", delimiters=new char[] {}
        Object actual = WordUtils.capitalizeFully("-1", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_206() throws Exception {
        // Combination: str="1.5", delimiters=new char[] {}
        Object actual = WordUtils.capitalizeFully("1.5", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_207() throws Exception {
        // Combination: str="9223372036854775807", delimiters=new char[] {}
        Object actual = WordUtils.capitalizeFully("9223372036854775807", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_208() throws Exception {
        // Combination: str="9223372036854775808", delimiters=new char[] {}
        Object actual = WordUtils.capitalizeFully("9223372036854775808", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_209() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiters=new char[] {}
        Object actual = WordUtils.capitalizeFully("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_210() throws Exception {
        // Combination: str="", delimiters=new char[] {1}
        Object actual = WordUtils.capitalizeFully("", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_211() throws Exception {
        // Combination: str=" ", delimiters=new char[] {1}
        Object actual = WordUtils.capitalizeFully(" ", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_212() throws Exception {
        // Combination: str="a", delimiters=new char[] {1}
        Object actual = WordUtils.capitalizeFully("a", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("A", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_213() throws Exception {
        // Combination: str="test123", delimiters=new char[] {1}
        Object actual = WordUtils.capitalizeFully("test123", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_214() throws Exception {
        // Combination: str="!@#", delimiters=new char[] {1}
        Object actual = WordUtils.capitalizeFully("!@#", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_215() throws Exception {
        // Combination: str="0", delimiters=new char[] {1}
        Object actual = WordUtils.capitalizeFully("0", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_216() throws Exception {
        // Combination: str="-1", delimiters=new char[] {1}
        Object actual = WordUtils.capitalizeFully("-1", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_217() throws Exception {
        // Combination: str="1.5", delimiters=new char[] {1}
        Object actual = WordUtils.capitalizeFully("1.5", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_218() throws Exception {
        // Combination: str="9223372036854775807", delimiters=new char[] {1}
        Object actual = WordUtils.capitalizeFully("9223372036854775807", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_219() throws Exception {
        // Combination: str="9223372036854775808", delimiters=new char[] {1}
        Object actual = WordUtils.capitalizeFully("9223372036854775808", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_capitalizeFully_pairwise_220() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiters=new char[] {1}
        Object actual = WordUtils.capitalizeFully("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_221() throws Exception {
        // Combination: str="", delimiters=new char[] {}
        Object actual = WordUtils.uncapitalize("", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_222() throws Exception {
        // Combination: str=" ", delimiters=new char[] {}
        Object actual = WordUtils.uncapitalize(" ", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_223() throws Exception {
        // Combination: str="a", delimiters=new char[] {}
        Object actual = WordUtils.uncapitalize("a", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_224() throws Exception {
        // Combination: str="test123", delimiters=new char[] {}
        Object actual = WordUtils.uncapitalize("test123", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_225() throws Exception {
        // Combination: str="!@#", delimiters=new char[] {}
        Object actual = WordUtils.uncapitalize("!@#", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_226() throws Exception {
        // Combination: str="0", delimiters=new char[] {}
        Object actual = WordUtils.uncapitalize("0", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_227() throws Exception {
        // Combination: str="-1", delimiters=new char[] {}
        Object actual = WordUtils.uncapitalize("-1", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_228() throws Exception {
        // Combination: str="1.5", delimiters=new char[] {}
        Object actual = WordUtils.uncapitalize("1.5", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_229() throws Exception {
        // Combination: str="9223372036854775807", delimiters=new char[] {}
        Object actual = WordUtils.uncapitalize("9223372036854775807", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_230() throws Exception {
        // Combination: str="9223372036854775808", delimiters=new char[] {}
        Object actual = WordUtils.uncapitalize("9223372036854775808", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_231() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiters=new char[] {}
        Object actual = WordUtils.uncapitalize("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_232() throws Exception {
        // Combination: str="", delimiters=new char[] {1}
        Object actual = WordUtils.uncapitalize("", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_233() throws Exception {
        // Combination: str=" ", delimiters=new char[] {1}
        Object actual = WordUtils.uncapitalize(" ", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_234() throws Exception {
        // Combination: str="a", delimiters=new char[] {1}
        Object actual = WordUtils.uncapitalize("a", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_235() throws Exception {
        // Combination: str="test123", delimiters=new char[] {1}
        Object actual = WordUtils.uncapitalize("test123", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_236() throws Exception {
        // Combination: str="!@#", delimiters=new char[] {1}
        Object actual = WordUtils.uncapitalize("!@#", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_237() throws Exception {
        // Combination: str="0", delimiters=new char[] {1}
        Object actual = WordUtils.uncapitalize("0", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_238() throws Exception {
        // Combination: str="-1", delimiters=new char[] {1}
        Object actual = WordUtils.uncapitalize("-1", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_239() throws Exception {
        // Combination: str="1.5", delimiters=new char[] {1}
        Object actual = WordUtils.uncapitalize("1.5", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_240() throws Exception {
        // Combination: str="9223372036854775807", delimiters=new char[] {1}
        Object actual = WordUtils.uncapitalize("9223372036854775807", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_241() throws Exception {
        // Combination: str="9223372036854775808", delimiters=new char[] {1}
        Object actual = WordUtils.uncapitalize("9223372036854775808", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_uncapitalize_pairwise_242() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiters=new char[] {1}
        Object actual = WordUtils.uncapitalize("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_243() throws Exception {
        // Combination: str="", delimiters=new char[] {}
        Object actual = WordUtils.initials("", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_244() throws Exception {
        // Combination: str=" ", delimiters=new char[] {}
        Object actual = WordUtils.initials(" ", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_245() throws Exception {
        // Combination: str="a", delimiters=new char[] {}
        Object actual = WordUtils.initials("a", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_246() throws Exception {
        // Combination: str="test123", delimiters=new char[] {}
        Object actual = WordUtils.initials("test123", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_247() throws Exception {
        // Combination: str="!@#", delimiters=new char[] {}
        Object actual = WordUtils.initials("!@#", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_248() throws Exception {
        // Combination: str="0", delimiters=new char[] {}
        Object actual = WordUtils.initials("0", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_249() throws Exception {
        // Combination: str="-1", delimiters=new char[] {}
        Object actual = WordUtils.initials("-1", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_250() throws Exception {
        // Combination: str="1.5", delimiters=new char[] {}
        Object actual = WordUtils.initials("1.5", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_251() throws Exception {
        // Combination: str="9223372036854775807", delimiters=new char[] {}
        Object actual = WordUtils.initials("9223372036854775807", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_252() throws Exception {
        // Combination: str="9223372036854775808", delimiters=new char[] {}
        Object actual = WordUtils.initials("9223372036854775808", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_253() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiters=new char[] {}
        Object actual = WordUtils.initials("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_254() throws Exception {
        // Combination: str="", delimiters=new char[] {1}
        Object actual = WordUtils.initials("", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_255() throws Exception {
        // Combination: str=" ", delimiters=new char[] {1}
        Object actual = WordUtils.initials(" ", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_256() throws Exception {
        // Combination: str="a", delimiters=new char[] {1}
        Object actual = WordUtils.initials("a", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_257() throws Exception {
        // Combination: str="test123", delimiters=new char[] {1}
        Object actual = WordUtils.initials("test123", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("t", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_258() throws Exception {
        // Combination: str="!@#", delimiters=new char[] {1}
        Object actual = WordUtils.initials("!@#", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_259() throws Exception {
        // Combination: str="0", delimiters=new char[] {1}
        Object actual = WordUtils.initials("0", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_260() throws Exception {
        // Combination: str="-1", delimiters=new char[] {1}
        Object actual = WordUtils.initials("-1", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_261() throws Exception {
        // Combination: str="1.5", delimiters=new char[] {1}
        Object actual = WordUtils.initials("1.5", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_262() throws Exception {
        // Combination: str="9223372036854775807", delimiters=new char[] {1}
        Object actual = WordUtils.initials("9223372036854775807", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_263() throws Exception {
        // Combination: str="9223372036854775808", delimiters=new char[] {1}
        Object actual = WordUtils.initials("9223372036854775808", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_initials_pairwise_264() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiters=new char[] {1}
        Object actual = WordUtils.initials("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_265() throws Exception {
        // Combination: str="", lower=0, upper=0, appendToEnd=""
        Object actual = WordUtils.abbreviate("", 0, 0, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_266() throws Exception {
        // Combination: str=" ", lower=1, upper=1, appendToEnd=""
        Object actual = WordUtils.abbreviate(" ", 1, 1, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_267() throws Exception {
        // Combination: str="a", lower=-1, upper=-1, appendToEnd=""
        Object actual = WordUtils.abbreviate("a", -1, -1, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_268() throws Exception {
        // Combination: str="test123", lower=Integer.MAX_VALUE, upper=Integer.MAX_VALUE, appendToEnd=""
        Object actual = WordUtils.abbreviate("test123", Integer.MAX_VALUE, Integer.MAX_VALUE, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_269() throws Exception {
        // Combination: str="!@#", lower=Integer.MIN_VALUE, upper=Integer.MIN_VALUE, appendToEnd=""
        try {
            WordUtils.abbreviate("!@#", Integer.MIN_VALUE, Integer.MIN_VALUE, "");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_270() throws Exception {
        // Combination: str=" ", lower=0, upper=-1, appendToEnd=" "
        Object actual = WordUtils.abbreviate(" ", 0, -1, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_271() throws Exception {
        // Combination: str="", lower=1, upper=Integer.MAX_VALUE, appendToEnd=" "
        Object actual = WordUtils.abbreviate("", 1, Integer.MAX_VALUE, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_272() throws Exception {
        // Combination: str="test123", lower=-1, upper=0, appendToEnd=" "
        Object actual = WordUtils.abbreviate("test123", -1, 0, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_273() throws Exception {
        // Combination: str="a", lower=Integer.MAX_VALUE, upper=1, appendToEnd=" "
        Object actual = WordUtils.abbreviate("a", Integer.MAX_VALUE, 1, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_274() throws Exception {
        // Combination: str="0", lower=Integer.MIN_VALUE, upper=0, appendToEnd=" "
        Object actual = WordUtils.abbreviate("0", Integer.MIN_VALUE, 0, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_275() throws Exception {
        // Combination: str="a", lower=0, upper=Integer.MAX_VALUE, appendToEnd="a"
        Object actual = WordUtils.abbreviate("a", 0, Integer.MAX_VALUE, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_276() throws Exception {
        // Combination: str="test123", lower=1, upper=-1, appendToEnd="a"
        Object actual = WordUtils.abbreviate("test123", 1, -1, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_277() throws Exception {
        // Combination: str="", lower=-1, upper=1, appendToEnd="a"
        Object actual = WordUtils.abbreviate("", -1, 1, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_278() throws Exception {
        // Combination: str=" ", lower=Integer.MAX_VALUE, upper=0, appendToEnd="a"
        Object actual = WordUtils.abbreviate(" ", Integer.MAX_VALUE, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_279() throws Exception {
        // Combination: str="-1", lower=Integer.MIN_VALUE, upper=1, appendToEnd="a"
        Object actual = WordUtils.abbreviate("-1", Integer.MIN_VALUE, 1, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_280() throws Exception {
        // Combination: str="test123", lower=0, upper=1, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("test123", 0, 1, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("ttest123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_281() throws Exception {
        // Combination: str="a", lower=1, upper=0, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("a", 1, 0, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_282() throws Exception {
        // Combination: str=" ", lower=-1, upper=Integer.MAX_VALUE, appendToEnd="test123"
        Object actual = WordUtils.abbreviate(" ", -1, Integer.MAX_VALUE, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_283() throws Exception {
        // Combination: str="", lower=Integer.MAX_VALUE, upper=-1, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("", Integer.MAX_VALUE, -1, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_284() throws Exception {
        // Combination: str="1.5", lower=Integer.MIN_VALUE, upper=-1, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("1.5", Integer.MIN_VALUE, -1, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_285() throws Exception {
        // Combination: str="!@#", lower=0, upper=0, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("!@#", 0, 0, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_286() throws Exception {
        // Combination: str="0", lower=1, upper=Integer.MIN_VALUE, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("0", 1, Integer.MIN_VALUE, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_287() throws Exception {
        // Combination: str="-1", lower=-1, upper=-1, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("-1", -1, -1, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_288() throws Exception {
        // Combination: str="1.5", lower=Integer.MAX_VALUE, upper=1, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("1.5", Integer.MAX_VALUE, 1, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_289() throws Exception {
        // Combination: str="", lower=Integer.MIN_VALUE, upper=Integer.MAX_VALUE, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("", Integer.MIN_VALUE, Integer.MAX_VALUE, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_290() throws Exception {
        // Combination: str="0", lower=0, upper=1, appendToEnd="0"
        Object actual = WordUtils.abbreviate("0", 0, 1, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_291() throws Exception {
        // Combination: str="!@#", lower=1, upper=-1, appendToEnd="0"
        Object actual = WordUtils.abbreviate("!@#", 1, -1, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_292() throws Exception {
        // Combination: str="1.5", lower=-1, upper=Integer.MIN_VALUE, appendToEnd="0"
        try {
            WordUtils.abbreviate("1.5", -1, Integer.MIN_VALUE, "0");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_293() throws Exception {
        // Combination: str="-1", lower=Integer.MAX_VALUE, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("-1", Integer.MAX_VALUE, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_294() throws Exception {
        // Combination: str=" ", lower=Integer.MIN_VALUE, upper=Integer.MAX_VALUE, appendToEnd="0"
        Object actual = WordUtils.abbreviate(" ", Integer.MIN_VALUE, Integer.MAX_VALUE, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_295() throws Exception {
        // Combination: str="-1", lower=0, upper=Integer.MIN_VALUE, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("-1", 0, Integer.MIN_VALUE, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_296() throws Exception {
        // Combination: str="1.5", lower=1, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("1.5", 1, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_297() throws Exception {
        // Combination: str="!@#", lower=-1, upper=1, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("!@#", -1, 1, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_298() throws Exception {
        // Combination: str="0", lower=Integer.MAX_VALUE, upper=-1, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("0", Integer.MAX_VALUE, -1, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_299() throws Exception {
        // Combination: str="a", lower=Integer.MIN_VALUE, upper=Integer.MAX_VALUE, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("a", Integer.MIN_VALUE, Integer.MAX_VALUE, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_300() throws Exception {
        // Combination: str="1.5", lower=0, upper=Integer.MAX_VALUE, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("1.5", 0, Integer.MAX_VALUE, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_301() throws Exception {
        // Combination: str="-1", lower=1, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("-1", 1, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_302() throws Exception {
        // Combination: str="0", lower=-1, upper=1, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("0", -1, 1, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_303() throws Exception {
        // Combination: str="!@#", lower=Integer.MAX_VALUE, upper=Integer.MIN_VALUE, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("!@#", Integer.MAX_VALUE, Integer.MIN_VALUE, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_304() throws Exception {
        // Combination: str="test123", lower=Integer.MIN_VALUE, upper=-1, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("test123", Integer.MIN_VALUE, -1, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_305() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_306() throws Exception {
        // Combination: str="9223372036854775808", lower=1, upper=1, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("9223372036854775808", 1, 1, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("99223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_307() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=-1, upper=-1, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_308() throws Exception {
        // Combination: str="", lower=Integer.MAX_VALUE, upper=Integer.MIN_VALUE, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("", Integer.MAX_VALUE, Integer.MIN_VALUE, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_309() throws Exception {
        // Combination: str=" ", lower=Integer.MIN_VALUE, upper=Integer.MAX_VALUE, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate(" ", Integer.MIN_VALUE, Integer.MAX_VALUE, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_310() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_311() throws Exception {
        // Combination: str="9223372036854775807", lower=1, upper=1, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("9223372036854775807", 1, 1, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("99223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_312() throws Exception {
        // Combination: str="", lower=-1, upper=-1, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("", -1, -1, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_313() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=Integer.MAX_VALUE, upper=Integer.MAX_VALUE, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_314() throws Exception {
        // Combination: str=" ", lower=Integer.MIN_VALUE, upper=Integer.MIN_VALUE, appendToEnd="9223372036854775808"
        try {
            WordUtils.abbreviate(" ", Integer.MIN_VALUE, Integer.MIN_VALUE, "9223372036854775808");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_315() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_316() throws Exception {
        // Combination: str="", lower=1, upper=1, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("", 1, 1, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_317() throws Exception {
        // Combination: str="9223372036854775807", lower=-1, upper=-1, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("9223372036854775807", -1, -1, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_318() throws Exception {
        // Combination: str="9223372036854775808", lower=Integer.MAX_VALUE, upper=Integer.MAX_VALUE, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_319() throws Exception {
        // Combination: str=" ", lower=Integer.MIN_VALUE, upper=Integer.MIN_VALUE, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            WordUtils.abbreviate(" ", Integer.MIN_VALUE, Integer.MIN_VALUE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_320() throws Exception {
        // Combination: str="", lower=0, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("", 0, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_321() throws Exception {
        // Combination: str="", lower=0, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("", 0, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_322() throws Exception {
        // Combination: str="", lower=0, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("", 0, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_323() throws Exception {
        // Combination: str=" ", lower=0, upper=0, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate(" ", 0, 0, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_324() throws Exception {
        // Combination: str=" ", lower=0, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate(" ", 0, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_325() throws Exception {
        // Combination: str=" ", lower=0, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate(" ", 0, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_326() throws Exception {
        // Combination: str="a", lower=0, upper=Integer.MIN_VALUE, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("a", 0, Integer.MIN_VALUE, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_327() throws Exception {
        // Combination: str="a", lower=0, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("a", 0, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_328() throws Exception {
        // Combination: str="a", lower=0, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("a", 0, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_329() throws Exception {
        // Combination: str="a", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("a", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_330() throws Exception {
        // Combination: str="a", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("a", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_331() throws Exception {
        // Combination: str="a", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("a", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_332() throws Exception {
        // Combination: str="test123", lower=0, upper=Integer.MIN_VALUE, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("test123", 0, Integer.MIN_VALUE, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_333() throws Exception {
        // Combination: str="test123", lower=0, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("test123", 0, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_334() throws Exception {
        // Combination: str="test123", lower=0, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("test123", 0, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_335() throws Exception {
        // Combination: str="test123", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("test123", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_336() throws Exception {
        // Combination: str="test123", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("test123", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_337() throws Exception {
        // Combination: str="test123", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("test123", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_338() throws Exception {
        // Combination: str="!@#", lower=0, upper=Integer.MAX_VALUE, appendToEnd=" "
        Object actual = WordUtils.abbreviate("!@#", 0, Integer.MAX_VALUE, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_339() throws Exception {
        // Combination: str="!@#", lower=0, upper=Integer.MIN_VALUE, appendToEnd="a"
        Object actual = WordUtils.abbreviate("!@#", 0, Integer.MIN_VALUE, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_340() throws Exception {
        // Combination: str="!@#", lower=0, upper=Integer.MIN_VALUE, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("!@#", 0, Integer.MIN_VALUE, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_341() throws Exception {
        // Combination: str="!@#", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("!@#", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_342() throws Exception {
        // Combination: str="!@#", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("!@#", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_343() throws Exception {
        // Combination: str="!@#", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("!@#", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_344() throws Exception {
        // Combination: str="0", lower=0, upper=Integer.MAX_VALUE, appendToEnd=""
        Object actual = WordUtils.abbreviate("0", 0, Integer.MAX_VALUE, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_345() throws Exception {
        // Combination: str="0", lower=0, upper=0, appendToEnd="a"
        Object actual = WordUtils.abbreviate("0", 0, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_346() throws Exception {
        // Combination: str="0", lower=0, upper=0, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("0", 0, 0, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_347() throws Exception {
        // Combination: str="0", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("0", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_348() throws Exception {
        // Combination: str="0", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("0", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_349() throws Exception {
        // Combination: str="0", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("0", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_350() throws Exception {
        // Combination: str="-1", lower=0, upper=Integer.MAX_VALUE, appendToEnd=""
        Object actual = WordUtils.abbreviate("-1", 0, Integer.MAX_VALUE, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_351() throws Exception {
        // Combination: str="-1", lower=0, upper=Integer.MIN_VALUE, appendToEnd=" "
        Object actual = WordUtils.abbreviate("-1", 0, Integer.MIN_VALUE, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_352() throws Exception {
        // Combination: str="-1", lower=0, upper=0, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("-1", 0, 0, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_353() throws Exception {
        // Combination: str="-1", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("-1", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_354() throws Exception {
        // Combination: str="-1", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("-1", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_355() throws Exception {
        // Combination: str="-1", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("-1", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_356() throws Exception {
        // Combination: str="1.5", lower=0, upper=0, appendToEnd=""
        Object actual = WordUtils.abbreviate("1.5", 0, 0, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_357() throws Exception {
        // Combination: str="1.5", lower=0, upper=0, appendToEnd=" "
        Object actual = WordUtils.abbreviate("1.5", 0, 0, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_358() throws Exception {
        // Combination: str="1.5", lower=0, upper=0, appendToEnd="a"
        Object actual = WordUtils.abbreviate("1.5", 0, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_359() throws Exception {
        // Combination: str="1.5", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("1.5", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_360() throws Exception {
        // Combination: str="1.5", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("1.5", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_361() throws Exception {
        // Combination: str="1.5", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("1.5", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_362() throws Exception {
        // Combination: str="9223372036854775807", lower=Integer.MAX_VALUE, upper=Integer.MAX_VALUE, appendToEnd=""
        Object actual = WordUtils.abbreviate("9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_363() throws Exception {
        // Combination: str="9223372036854775807", lower=Integer.MIN_VALUE, upper=Integer.MIN_VALUE, appendToEnd=" "
        try {
            WordUtils.abbreviate("9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE, " ");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_364() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="a"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_365() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_366() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_367() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_368() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_369() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_370() throws Exception {
        // Combination: str="9223372036854775808", lower=-1, upper=-1, appendToEnd=""
        Object actual = WordUtils.abbreviate("9223372036854775808", -1, -1, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_371() throws Exception {
        // Combination: str="9223372036854775808", lower=Integer.MIN_VALUE, upper=Integer.MIN_VALUE, appendToEnd=" "
        try {
            WordUtils.abbreviate("9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE, " ");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_372() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="a"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_373() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_374() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_375() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_376() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_377() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_378() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=1, upper=1, appendToEnd=""
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_379() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=Integer.MIN_VALUE, upper=Integer.MIN_VALUE, appendToEnd=" "
        try {
            WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE, " ");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_380() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="a"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_381() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_382() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_383() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_384() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_385() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

}
