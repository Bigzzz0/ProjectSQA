package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Soundex.
 */
public class Soundex_IPOTest {
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
    public void test_difference_pairwise_001() throws Exception {
        // Combination: s1="", s2=""
        Object actual = (new Soundex()).difference("", "");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_002() throws Exception {
        // Combination: s1="", s2=" "
        Object actual = (new Soundex()).difference("", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_003() throws Exception {
        // Combination: s1="", s2="a"
        Object actual = (new Soundex()).difference("", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_004() throws Exception {
        // Combination: s1="", s2="test123"
        Object actual = (new Soundex()).difference("", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_005() throws Exception {
        // Combination: s1="", s2="!@#"
        Object actual = (new Soundex()).difference("", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_006() throws Exception {
        // Combination: s1="", s2="0"
        Object actual = (new Soundex()).difference("", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_007() throws Exception {
        // Combination: s1="", s2="-1"
        Object actual = (new Soundex()).difference("", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_008() throws Exception {
        // Combination: s1="", s2="1.5"
        Object actual = (new Soundex()).difference("", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_009() throws Exception {
        // Combination: s1="", s2="9223372036854775807"
        Object actual = (new Soundex()).difference("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_010() throws Exception {
        // Combination: s1="", s2="9223372036854775808"
        Object actual = (new Soundex()).difference("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_011() throws Exception {
        // Combination: s1="", s2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Soundex()).difference("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_012() throws Exception {
        // Combination: s1=" ", s2=""
        Object actual = (new Soundex()).difference(" ", "");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_013() throws Exception {
        // Combination: s1=" ", s2=" "
        Object actual = (new Soundex()).difference(" ", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_014() throws Exception {
        // Combination: s1=" ", s2="a"
        Object actual = (new Soundex()).difference(" ", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_015() throws Exception {
        // Combination: s1=" ", s2="test123"
        Object actual = (new Soundex()).difference(" ", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_016() throws Exception {
        // Combination: s1=" ", s2="!@#"
        Object actual = (new Soundex()).difference(" ", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_017() throws Exception {
        // Combination: s1=" ", s2="0"
        Object actual = (new Soundex()).difference(" ", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_018() throws Exception {
        // Combination: s1=" ", s2="-1"
        Object actual = (new Soundex()).difference(" ", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_019() throws Exception {
        // Combination: s1=" ", s2="1.5"
        Object actual = (new Soundex()).difference(" ", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_020() throws Exception {
        // Combination: s1=" ", s2="9223372036854775807"
        Object actual = (new Soundex()).difference(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_021() throws Exception {
        // Combination: s1=" ", s2="9223372036854775808"
        Object actual = (new Soundex()).difference(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_022() throws Exception {
        // Combination: s1=" ", s2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Soundex()).difference(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_023() throws Exception {
        // Combination: s1="a", s2=""
        Object actual = (new Soundex()).difference("a", "");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_024() throws Exception {
        // Combination: s1="a", s2=" "
        Object actual = (new Soundex()).difference("a", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_025() throws Exception {
        // Combination: s1="a", s2="a"
        Object actual = (new Soundex()).difference("a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_026() throws Exception {
        // Combination: s1="a", s2="test123"
        Object actual = (new Soundex()).difference("a", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_027() throws Exception {
        // Combination: s1="a", s2="!@#"
        Object actual = (new Soundex()).difference("a", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_028() throws Exception {
        // Combination: s1="a", s2="0"
        Object actual = (new Soundex()).difference("a", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_029() throws Exception {
        // Combination: s1="a", s2="-1"
        Object actual = (new Soundex()).difference("a", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_030() throws Exception {
        // Combination: s1="a", s2="1.5"
        Object actual = (new Soundex()).difference("a", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_031() throws Exception {
        // Combination: s1="a", s2="9223372036854775807"
        Object actual = (new Soundex()).difference("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_032() throws Exception {
        // Combination: s1="a", s2="9223372036854775808"
        Object actual = (new Soundex()).difference("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_033() throws Exception {
        // Combination: s1="a", s2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Soundex()).difference("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_034() throws Exception {
        // Combination: s1="test123", s2=""
        Object actual = (new Soundex()).difference("test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_035() throws Exception {
        // Combination: s1="test123", s2=" "
        Object actual = (new Soundex()).difference("test123", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_036() throws Exception {
        // Combination: s1="test123", s2="a"
        Object actual = (new Soundex()).difference("test123", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_037() throws Exception {
        // Combination: s1="test123", s2="test123"
        Object actual = (new Soundex()).difference("test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_038() throws Exception {
        // Combination: s1="test123", s2="!@#"
        Object actual = (new Soundex()).difference("test123", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_039() throws Exception {
        // Combination: s1="test123", s2="0"
        Object actual = (new Soundex()).difference("test123", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_040() throws Exception {
        // Combination: s1="test123", s2="-1"
        Object actual = (new Soundex()).difference("test123", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_041() throws Exception {
        // Combination: s1="test123", s2="1.5"
        Object actual = (new Soundex()).difference("test123", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_042() throws Exception {
        // Combination: s1="test123", s2="9223372036854775807"
        Object actual = (new Soundex()).difference("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_043() throws Exception {
        // Combination: s1="test123", s2="9223372036854775808"
        Object actual = (new Soundex()).difference("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_044() throws Exception {
        // Combination: s1="test123", s2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Soundex()).difference("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_045() throws Exception {
        // Combination: s1="!@#", s2=""
        Object actual = (new Soundex()).difference("!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_046() throws Exception {
        // Combination: s1="!@#", s2=" "
        Object actual = (new Soundex()).difference("!@#", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_047() throws Exception {
        // Combination: s1="!@#", s2="a"
        Object actual = (new Soundex()).difference("!@#", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_048() throws Exception {
        // Combination: s1="!@#", s2="test123"
        Object actual = (new Soundex()).difference("!@#", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_049() throws Exception {
        // Combination: s1="!@#", s2="!@#"
        Object actual = (new Soundex()).difference("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_050() throws Exception {
        // Combination: s1="!@#", s2="0"
        Object actual = (new Soundex()).difference("!@#", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_051() throws Exception {
        // Combination: s1="!@#", s2="-1"
        Object actual = (new Soundex()).difference("!@#", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_052() throws Exception {
        // Combination: s1="!@#", s2="1.5"
        Object actual = (new Soundex()).difference("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_053() throws Exception {
        // Combination: s1="!@#", s2="9223372036854775807"
        Object actual = (new Soundex()).difference("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_054() throws Exception {
        // Combination: s1="!@#", s2="9223372036854775808"
        Object actual = (new Soundex()).difference("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_055() throws Exception {
        // Combination: s1="!@#", s2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Soundex()).difference("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_056() throws Exception {
        // Combination: s1="0", s2=""
        Object actual = (new Soundex()).difference("0", "");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_057() throws Exception {
        // Combination: s1="0", s2=" "
        Object actual = (new Soundex()).difference("0", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_058() throws Exception {
        // Combination: s1="0", s2="a"
        Object actual = (new Soundex()).difference("0", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_059() throws Exception {
        // Combination: s1="0", s2="test123"
        Object actual = (new Soundex()).difference("0", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_060() throws Exception {
        // Combination: s1="0", s2="!@#"
        Object actual = (new Soundex()).difference("0", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_061() throws Exception {
        // Combination: s1="0", s2="0"
        Object actual = (new Soundex()).difference("0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_062() throws Exception {
        // Combination: s1="0", s2="-1"
        Object actual = (new Soundex()).difference("0", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_063() throws Exception {
        // Combination: s1="0", s2="1.5"
        Object actual = (new Soundex()).difference("0", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_064() throws Exception {
        // Combination: s1="0", s2="9223372036854775807"
        Object actual = (new Soundex()).difference("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_065() throws Exception {
        // Combination: s1="0", s2="9223372036854775808"
        Object actual = (new Soundex()).difference("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_066() throws Exception {
        // Combination: s1="0", s2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Soundex()).difference("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_067() throws Exception {
        // Combination: s1="-1", s2=""
        Object actual = (new Soundex()).difference("-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_068() throws Exception {
        // Combination: s1="-1", s2=" "
        Object actual = (new Soundex()).difference("-1", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_069() throws Exception {
        // Combination: s1="-1", s2="a"
        Object actual = (new Soundex()).difference("-1", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_070() throws Exception {
        // Combination: s1="-1", s2="test123"
        Object actual = (new Soundex()).difference("-1", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_071() throws Exception {
        // Combination: s1="-1", s2="!@#"
        Object actual = (new Soundex()).difference("-1", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_072() throws Exception {
        // Combination: s1="-1", s2="0"
        Object actual = (new Soundex()).difference("-1", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_073() throws Exception {
        // Combination: s1="-1", s2="-1"
        Object actual = (new Soundex()).difference("-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_074() throws Exception {
        // Combination: s1="-1", s2="1.5"
        Object actual = (new Soundex()).difference("-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_075() throws Exception {
        // Combination: s1="-1", s2="9223372036854775807"
        Object actual = (new Soundex()).difference("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_076() throws Exception {
        // Combination: s1="-1", s2="9223372036854775808"
        Object actual = (new Soundex()).difference("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_077() throws Exception {
        // Combination: s1="-1", s2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Soundex()).difference("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_078() throws Exception {
        // Combination: s1="1.5", s2=""
        Object actual = (new Soundex()).difference("1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_079() throws Exception {
        // Combination: s1="1.5", s2=" "
        Object actual = (new Soundex()).difference("1.5", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_080() throws Exception {
        // Combination: s1="1.5", s2="a"
        Object actual = (new Soundex()).difference("1.5", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_081() throws Exception {
        // Combination: s1="1.5", s2="test123"
        Object actual = (new Soundex()).difference("1.5", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_082() throws Exception {
        // Combination: s1="1.5", s2="!@#"
        Object actual = (new Soundex()).difference("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_083() throws Exception {
        // Combination: s1="1.5", s2="0"
        Object actual = (new Soundex()).difference("1.5", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_084() throws Exception {
        // Combination: s1="1.5", s2="-1"
        Object actual = (new Soundex()).difference("1.5", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_085() throws Exception {
        // Combination: s1="1.5", s2="1.5"
        Object actual = (new Soundex()).difference("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_086() throws Exception {
        // Combination: s1="1.5", s2="9223372036854775807"
        Object actual = (new Soundex()).difference("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_087() throws Exception {
        // Combination: s1="1.5", s2="9223372036854775808"
        Object actual = (new Soundex()).difference("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_088() throws Exception {
        // Combination: s1="1.5", s2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Soundex()).difference("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_089() throws Exception {
        // Combination: s1="9223372036854775807", s2=""
        Object actual = (new Soundex()).difference("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_090() throws Exception {
        // Combination: s1="9223372036854775807", s2=" "
        Object actual = (new Soundex()).difference("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_091() throws Exception {
        // Combination: s1="9223372036854775807", s2="a"
        Object actual = (new Soundex()).difference("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_092() throws Exception {
        // Combination: s1="9223372036854775807", s2="test123"
        Object actual = (new Soundex()).difference("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_093() throws Exception {
        // Combination: s1="9223372036854775807", s2="!@#"
        Object actual = (new Soundex()).difference("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_094() throws Exception {
        // Combination: s1="9223372036854775807", s2="0"
        Object actual = (new Soundex()).difference("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_095() throws Exception {
        // Combination: s1="9223372036854775807", s2="-1"
        Object actual = (new Soundex()).difference("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_096() throws Exception {
        // Combination: s1="9223372036854775807", s2="1.5"
        Object actual = (new Soundex()).difference("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_097() throws Exception {
        // Combination: s1="9223372036854775807", s2="9223372036854775807"
        Object actual = (new Soundex()).difference("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_098() throws Exception {
        // Combination: s1="9223372036854775807", s2="9223372036854775808"
        Object actual = (new Soundex()).difference("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_099() throws Exception {
        // Combination: s1="9223372036854775807", s2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Soundex()).difference("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_100() throws Exception {
        // Combination: s1="9223372036854775808", s2=""
        Object actual = (new Soundex()).difference("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_101() throws Exception {
        // Combination: s1="9223372036854775808", s2=" "
        Object actual = (new Soundex()).difference("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_102() throws Exception {
        // Combination: s1="9223372036854775808", s2="a"
        Object actual = (new Soundex()).difference("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_103() throws Exception {
        // Combination: s1="9223372036854775808", s2="test123"
        Object actual = (new Soundex()).difference("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_104() throws Exception {
        // Combination: s1="9223372036854775808", s2="!@#"
        Object actual = (new Soundex()).difference("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_105() throws Exception {
        // Combination: s1="9223372036854775808", s2="0"
        Object actual = (new Soundex()).difference("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_106() throws Exception {
        // Combination: s1="9223372036854775808", s2="-1"
        Object actual = (new Soundex()).difference("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_107() throws Exception {
        // Combination: s1="9223372036854775808", s2="1.5"
        Object actual = (new Soundex()).difference("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_108() throws Exception {
        // Combination: s1="9223372036854775808", s2="9223372036854775807"
        Object actual = (new Soundex()).difference("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_109() throws Exception {
        // Combination: s1="9223372036854775808", s2="9223372036854775808"
        Object actual = (new Soundex()).difference("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_110() throws Exception {
        // Combination: s1="9223372036854775808", s2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Soundex()).difference("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_111() throws Exception {
        // Combination: s1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s2=""
        Object actual = (new Soundex()).difference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_112() throws Exception {
        // Combination: s1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s2=" "
        Object actual = (new Soundex()).difference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_113() throws Exception {
        // Combination: s1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s2="a"
        Object actual = (new Soundex()).difference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_114() throws Exception {
        // Combination: s1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s2="test123"
        Object actual = (new Soundex()).difference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_115() throws Exception {
        // Combination: s1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s2="!@#"
        Object actual = (new Soundex()).difference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_116() throws Exception {
        // Combination: s1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s2="0"
        Object actual = (new Soundex()).difference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_117() throws Exception {
        // Combination: s1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s2="-1"
        Object actual = (new Soundex()).difference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_118() throws Exception {
        // Combination: s1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s2="1.5"
        Object actual = (new Soundex()).difference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_119() throws Exception {
        // Combination: s1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s2="9223372036854775807"
        Object actual = (new Soundex()).difference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_120() throws Exception {
        // Combination: s1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s2="9223372036854775808"
        Object actual = (new Soundex()).difference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_difference_pairwise_121() throws Exception {
        // Combination: s1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", s2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Soundex()).difference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("4", formatValue(actual));
    }

}
