package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for TokenQueue.
 */
public class TokenQueue_IPOTest {
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
    public void test_matches_pairwise_001() throws Exception {
        // Combination: receiver__data="", seq=""
        Object actual = (new TokenQueue("")).matches("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_002() throws Exception {
        // Combination: receiver__data="", seq=" "
        Object actual = (new TokenQueue("")).matches(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_003() throws Exception {
        // Combination: receiver__data="", seq="a"
        Object actual = (new TokenQueue("")).matches("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_004() throws Exception {
        // Combination: receiver__data="", seq="test123"
        Object actual = (new TokenQueue("")).matches("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_005() throws Exception {
        // Combination: receiver__data="", seq="!@#"
        Object actual = (new TokenQueue("")).matches("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_006() throws Exception {
        // Combination: receiver__data="", seq="0"
        Object actual = (new TokenQueue("")).matches("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_007() throws Exception {
        // Combination: receiver__data="", seq="-1"
        Object actual = (new TokenQueue("")).matches("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_008() throws Exception {
        // Combination: receiver__data="", seq="1.5"
        Object actual = (new TokenQueue("")).matches("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_009() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775807"
        Object actual = (new TokenQueue("")).matches("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_010() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775808"
        Object actual = (new TokenQueue("")).matches("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_011() throws Exception {
        // Combination: receiver__data="", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("")).matches("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_012() throws Exception {
        // Combination: receiver__data=" ", seq=""
        Object actual = (new TokenQueue(" ")).matches("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_013() throws Exception {
        // Combination: receiver__data=" ", seq=" "
        Object actual = (new TokenQueue(" ")).matches(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_014() throws Exception {
        // Combination: receiver__data=" ", seq="a"
        Object actual = (new TokenQueue(" ")).matches("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_015() throws Exception {
        // Combination: receiver__data=" ", seq="test123"
        Object actual = (new TokenQueue(" ")).matches("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_016() throws Exception {
        // Combination: receiver__data=" ", seq="!@#"
        Object actual = (new TokenQueue(" ")).matches("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_017() throws Exception {
        // Combination: receiver__data=" ", seq="0"
        Object actual = (new TokenQueue(" ")).matches("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_018() throws Exception {
        // Combination: receiver__data=" ", seq="-1"
        Object actual = (new TokenQueue(" ")).matches("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_019() throws Exception {
        // Combination: receiver__data=" ", seq="1.5"
        Object actual = (new TokenQueue(" ")).matches("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_020() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775807"
        Object actual = (new TokenQueue(" ")).matches("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_021() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775808"
        Object actual = (new TokenQueue(" ")).matches("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_022() throws Exception {
        // Combination: receiver__data=" ", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue(" ")).matches("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_023() throws Exception {
        // Combination: receiver__data="a", seq=""
        Object actual = (new TokenQueue("a")).matches("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_024() throws Exception {
        // Combination: receiver__data="a", seq=" "
        Object actual = (new TokenQueue("a")).matches(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_025() throws Exception {
        // Combination: receiver__data="a", seq="a"
        Object actual = (new TokenQueue("a")).matches("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_026() throws Exception {
        // Combination: receiver__data="a", seq="test123"
        Object actual = (new TokenQueue("a")).matches("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_027() throws Exception {
        // Combination: receiver__data="a", seq="!@#"
        Object actual = (new TokenQueue("a")).matches("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_028() throws Exception {
        // Combination: receiver__data="a", seq="0"
        Object actual = (new TokenQueue("a")).matches("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_029() throws Exception {
        // Combination: receiver__data="a", seq="-1"
        Object actual = (new TokenQueue("a")).matches("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_030() throws Exception {
        // Combination: receiver__data="a", seq="1.5"
        Object actual = (new TokenQueue("a")).matches("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_031() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775807"
        Object actual = (new TokenQueue("a")).matches("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_032() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775808"
        Object actual = (new TokenQueue("a")).matches("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_033() throws Exception {
        // Combination: receiver__data="a", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("a")).matches("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_034() throws Exception {
        // Combination: receiver__data="test123", seq=""
        Object actual = (new TokenQueue("test123")).matches("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_035() throws Exception {
        // Combination: receiver__data="test123", seq=" "
        Object actual = (new TokenQueue("test123")).matches(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_036() throws Exception {
        // Combination: receiver__data="test123", seq="a"
        Object actual = (new TokenQueue("test123")).matches("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_037() throws Exception {
        // Combination: receiver__data="test123", seq="test123"
        Object actual = (new TokenQueue("test123")).matches("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_038() throws Exception {
        // Combination: receiver__data="test123", seq="!@#"
        Object actual = (new TokenQueue("test123")).matches("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_039() throws Exception {
        // Combination: receiver__data="test123", seq="0"
        Object actual = (new TokenQueue("test123")).matches("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_040() throws Exception {
        // Combination: receiver__data="test123", seq="-1"
        Object actual = (new TokenQueue("test123")).matches("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_041() throws Exception {
        // Combination: receiver__data="test123", seq="1.5"
        Object actual = (new TokenQueue("test123")).matches("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_042() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775807"
        Object actual = (new TokenQueue("test123")).matches("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_043() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775808"
        Object actual = (new TokenQueue("test123")).matches("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_044() throws Exception {
        // Combination: receiver__data="test123", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("test123")).matches("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_045() throws Exception {
        // Combination: receiver__data="!@#", seq=""
        Object actual = (new TokenQueue("!@#")).matches("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_046() throws Exception {
        // Combination: receiver__data="!@#", seq=" "
        Object actual = (new TokenQueue("!@#")).matches(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_047() throws Exception {
        // Combination: receiver__data="!@#", seq="a"
        Object actual = (new TokenQueue("!@#")).matches("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_048() throws Exception {
        // Combination: receiver__data="!@#", seq="test123"
        Object actual = (new TokenQueue("!@#")).matches("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_049() throws Exception {
        // Combination: receiver__data="!@#", seq="!@#"
        Object actual = (new TokenQueue("!@#")).matches("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_050() throws Exception {
        // Combination: receiver__data="!@#", seq="0"
        Object actual = (new TokenQueue("!@#")).matches("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_051() throws Exception {
        // Combination: receiver__data="!@#", seq="-1"
        Object actual = (new TokenQueue("!@#")).matches("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_052() throws Exception {
        // Combination: receiver__data="!@#", seq="1.5"
        Object actual = (new TokenQueue("!@#")).matches("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_053() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775807"
        Object actual = (new TokenQueue("!@#")).matches("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_054() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775808"
        Object actual = (new TokenQueue("!@#")).matches("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_055() throws Exception {
        // Combination: receiver__data="!@#", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("!@#")).matches("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_056() throws Exception {
        // Combination: receiver__data="0", seq=""
        Object actual = (new TokenQueue("0")).matches("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_057() throws Exception {
        // Combination: receiver__data="0", seq=" "
        Object actual = (new TokenQueue("0")).matches(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_058() throws Exception {
        // Combination: receiver__data="0", seq="a"
        Object actual = (new TokenQueue("0")).matches("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_059() throws Exception {
        // Combination: receiver__data="0", seq="test123"
        Object actual = (new TokenQueue("0")).matches("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_060() throws Exception {
        // Combination: receiver__data="0", seq="!@#"
        Object actual = (new TokenQueue("0")).matches("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_061() throws Exception {
        // Combination: receiver__data="0", seq="0"
        Object actual = (new TokenQueue("0")).matches("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_062() throws Exception {
        // Combination: receiver__data="0", seq="-1"
        Object actual = (new TokenQueue("0")).matches("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_063() throws Exception {
        // Combination: receiver__data="0", seq="1.5"
        Object actual = (new TokenQueue("0")).matches("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_064() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775807"
        Object actual = (new TokenQueue("0")).matches("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_065() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775808"
        Object actual = (new TokenQueue("0")).matches("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_066() throws Exception {
        // Combination: receiver__data="0", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("0")).matches("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_067() throws Exception {
        // Combination: receiver__data="-1", seq=""
        Object actual = (new TokenQueue("-1")).matches("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_068() throws Exception {
        // Combination: receiver__data="-1", seq=" "
        Object actual = (new TokenQueue("-1")).matches(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_069() throws Exception {
        // Combination: receiver__data="-1", seq="a"
        Object actual = (new TokenQueue("-1")).matches("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_070() throws Exception {
        // Combination: receiver__data="-1", seq="test123"
        Object actual = (new TokenQueue("-1")).matches("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_071() throws Exception {
        // Combination: receiver__data="-1", seq="!@#"
        Object actual = (new TokenQueue("-1")).matches("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_072() throws Exception {
        // Combination: receiver__data="-1", seq="0"
        Object actual = (new TokenQueue("-1")).matches("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_073() throws Exception {
        // Combination: receiver__data="-1", seq="-1"
        Object actual = (new TokenQueue("-1")).matches("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_074() throws Exception {
        // Combination: receiver__data="-1", seq="1.5"
        Object actual = (new TokenQueue("-1")).matches("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_075() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775807"
        Object actual = (new TokenQueue("-1")).matches("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_076() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775808"
        Object actual = (new TokenQueue("-1")).matches("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_077() throws Exception {
        // Combination: receiver__data="-1", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("-1")).matches("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_078() throws Exception {
        // Combination: receiver__data="1.5", seq=""
        Object actual = (new TokenQueue("1.5")).matches("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_079() throws Exception {
        // Combination: receiver__data="1.5", seq=" "
        Object actual = (new TokenQueue("1.5")).matches(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_080() throws Exception {
        // Combination: receiver__data="1.5", seq="a"
        Object actual = (new TokenQueue("1.5")).matches("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_081() throws Exception {
        // Combination: receiver__data="1.5", seq="test123"
        Object actual = (new TokenQueue("1.5")).matches("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_082() throws Exception {
        // Combination: receiver__data="1.5", seq="!@#"
        Object actual = (new TokenQueue("1.5")).matches("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_083() throws Exception {
        // Combination: receiver__data="1.5", seq="0"
        Object actual = (new TokenQueue("1.5")).matches("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_084() throws Exception {
        // Combination: receiver__data="1.5", seq="-1"
        Object actual = (new TokenQueue("1.5")).matches("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_085() throws Exception {
        // Combination: receiver__data="1.5", seq="1.5"
        Object actual = (new TokenQueue("1.5")).matches("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_086() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775807"
        Object actual = (new TokenQueue("1.5")).matches("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_087() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775808"
        Object actual = (new TokenQueue("1.5")).matches("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_088() throws Exception {
        // Combination: receiver__data="1.5", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("1.5")).matches("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_089() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=""
        Object actual = (new TokenQueue("9223372036854775807")).matches("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_090() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=" "
        Object actual = (new TokenQueue("9223372036854775807")).matches(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_091() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="a"
        Object actual = (new TokenQueue("9223372036854775807")).matches("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_092() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="test123"
        Object actual = (new TokenQueue("9223372036854775807")).matches("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_093() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775807")).matches("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_094() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="0"
        Object actual = (new TokenQueue("9223372036854775807")).matches("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_095() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="-1"
        Object actual = (new TokenQueue("9223372036854775807")).matches("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_096() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775807")).matches("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_097() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775807")).matches("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_098() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775807")).matches("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_099() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775807")).matches("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_100() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=""
        Object actual = (new TokenQueue("9223372036854775808")).matches("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_101() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=" "
        Object actual = (new TokenQueue("9223372036854775808")).matches(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_102() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="a"
        Object actual = (new TokenQueue("9223372036854775808")).matches("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_103() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="test123"
        Object actual = (new TokenQueue("9223372036854775808")).matches("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_104() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775808")).matches("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_105() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="0"
        Object actual = (new TokenQueue("9223372036854775808")).matches("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_106() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="-1"
        Object actual = (new TokenQueue("9223372036854775808")).matches("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_107() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775808")).matches("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_108() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775808")).matches("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_109() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775808")).matches("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_110() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775808")).matches("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_111() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=""
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matches("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_112() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=" "
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matches(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_113() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="a"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matches("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_114() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="test123"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matches("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_115() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="!@#"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matches("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_116() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="0"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matches("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_117() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="-1"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matches("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_118() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="1.5"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matches("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_119() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775807"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matches("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_120() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775808"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matches("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_121() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matches("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_122() throws Exception {
        // Combination: receiver__data="", seq=""
        Object actual = (new TokenQueue("")).matchesCS("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_123() throws Exception {
        // Combination: receiver__data="", seq=" "
        Object actual = (new TokenQueue("")).matchesCS(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_124() throws Exception {
        // Combination: receiver__data="", seq="a"
        Object actual = (new TokenQueue("")).matchesCS("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_125() throws Exception {
        // Combination: receiver__data="", seq="test123"
        Object actual = (new TokenQueue("")).matchesCS("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_126() throws Exception {
        // Combination: receiver__data="", seq="!@#"
        Object actual = (new TokenQueue("")).matchesCS("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_127() throws Exception {
        // Combination: receiver__data="", seq="0"
        Object actual = (new TokenQueue("")).matchesCS("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_128() throws Exception {
        // Combination: receiver__data="", seq="-1"
        Object actual = (new TokenQueue("")).matchesCS("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_129() throws Exception {
        // Combination: receiver__data="", seq="1.5"
        Object actual = (new TokenQueue("")).matchesCS("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_130() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775807"
        Object actual = (new TokenQueue("")).matchesCS("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_131() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775808"
        Object actual = (new TokenQueue("")).matchesCS("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_132() throws Exception {
        // Combination: receiver__data="", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("")).matchesCS("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_133() throws Exception {
        // Combination: receiver__data=" ", seq=""
        Object actual = (new TokenQueue(" ")).matchesCS("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_134() throws Exception {
        // Combination: receiver__data=" ", seq=" "
        Object actual = (new TokenQueue(" ")).matchesCS(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_135() throws Exception {
        // Combination: receiver__data=" ", seq="a"
        Object actual = (new TokenQueue(" ")).matchesCS("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_136() throws Exception {
        // Combination: receiver__data=" ", seq="test123"
        Object actual = (new TokenQueue(" ")).matchesCS("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_137() throws Exception {
        // Combination: receiver__data=" ", seq="!@#"
        Object actual = (new TokenQueue(" ")).matchesCS("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_138() throws Exception {
        // Combination: receiver__data=" ", seq="0"
        Object actual = (new TokenQueue(" ")).matchesCS("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_139() throws Exception {
        // Combination: receiver__data=" ", seq="-1"
        Object actual = (new TokenQueue(" ")).matchesCS("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_140() throws Exception {
        // Combination: receiver__data=" ", seq="1.5"
        Object actual = (new TokenQueue(" ")).matchesCS("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_141() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775807"
        Object actual = (new TokenQueue(" ")).matchesCS("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_142() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775808"
        Object actual = (new TokenQueue(" ")).matchesCS("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_143() throws Exception {
        // Combination: receiver__data=" ", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue(" ")).matchesCS("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_144() throws Exception {
        // Combination: receiver__data="a", seq=""
        Object actual = (new TokenQueue("a")).matchesCS("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_145() throws Exception {
        // Combination: receiver__data="a", seq=" "
        Object actual = (new TokenQueue("a")).matchesCS(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_146() throws Exception {
        // Combination: receiver__data="a", seq="a"
        Object actual = (new TokenQueue("a")).matchesCS("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_147() throws Exception {
        // Combination: receiver__data="a", seq="test123"
        Object actual = (new TokenQueue("a")).matchesCS("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_148() throws Exception {
        // Combination: receiver__data="a", seq="!@#"
        Object actual = (new TokenQueue("a")).matchesCS("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_149() throws Exception {
        // Combination: receiver__data="a", seq="0"
        Object actual = (new TokenQueue("a")).matchesCS("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_150() throws Exception {
        // Combination: receiver__data="a", seq="-1"
        Object actual = (new TokenQueue("a")).matchesCS("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_151() throws Exception {
        // Combination: receiver__data="a", seq="1.5"
        Object actual = (new TokenQueue("a")).matchesCS("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_152() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775807"
        Object actual = (new TokenQueue("a")).matchesCS("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_153() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775808"
        Object actual = (new TokenQueue("a")).matchesCS("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_154() throws Exception {
        // Combination: receiver__data="a", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("a")).matchesCS("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_155() throws Exception {
        // Combination: receiver__data="test123", seq=""
        Object actual = (new TokenQueue("test123")).matchesCS("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_156() throws Exception {
        // Combination: receiver__data="test123", seq=" "
        Object actual = (new TokenQueue("test123")).matchesCS(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_157() throws Exception {
        // Combination: receiver__data="test123", seq="a"
        Object actual = (new TokenQueue("test123")).matchesCS("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_158() throws Exception {
        // Combination: receiver__data="test123", seq="test123"
        Object actual = (new TokenQueue("test123")).matchesCS("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_159() throws Exception {
        // Combination: receiver__data="test123", seq="!@#"
        Object actual = (new TokenQueue("test123")).matchesCS("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_160() throws Exception {
        // Combination: receiver__data="test123", seq="0"
        Object actual = (new TokenQueue("test123")).matchesCS("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_161() throws Exception {
        // Combination: receiver__data="test123", seq="-1"
        Object actual = (new TokenQueue("test123")).matchesCS("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_162() throws Exception {
        // Combination: receiver__data="test123", seq="1.5"
        Object actual = (new TokenQueue("test123")).matchesCS("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_163() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775807"
        Object actual = (new TokenQueue("test123")).matchesCS("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_164() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775808"
        Object actual = (new TokenQueue("test123")).matchesCS("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_165() throws Exception {
        // Combination: receiver__data="test123", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("test123")).matchesCS("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_166() throws Exception {
        // Combination: receiver__data="!@#", seq=""
        Object actual = (new TokenQueue("!@#")).matchesCS("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_167() throws Exception {
        // Combination: receiver__data="!@#", seq=" "
        Object actual = (new TokenQueue("!@#")).matchesCS(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_168() throws Exception {
        // Combination: receiver__data="!@#", seq="a"
        Object actual = (new TokenQueue("!@#")).matchesCS("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_169() throws Exception {
        // Combination: receiver__data="!@#", seq="test123"
        Object actual = (new TokenQueue("!@#")).matchesCS("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_170() throws Exception {
        // Combination: receiver__data="!@#", seq="!@#"
        Object actual = (new TokenQueue("!@#")).matchesCS("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_171() throws Exception {
        // Combination: receiver__data="!@#", seq="0"
        Object actual = (new TokenQueue("!@#")).matchesCS("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_172() throws Exception {
        // Combination: receiver__data="!@#", seq="-1"
        Object actual = (new TokenQueue("!@#")).matchesCS("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_173() throws Exception {
        // Combination: receiver__data="!@#", seq="1.5"
        Object actual = (new TokenQueue("!@#")).matchesCS("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_174() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775807"
        Object actual = (new TokenQueue("!@#")).matchesCS("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_175() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775808"
        Object actual = (new TokenQueue("!@#")).matchesCS("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_176() throws Exception {
        // Combination: receiver__data="!@#", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("!@#")).matchesCS("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_177() throws Exception {
        // Combination: receiver__data="0", seq=""
        Object actual = (new TokenQueue("0")).matchesCS("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_178() throws Exception {
        // Combination: receiver__data="0", seq=" "
        Object actual = (new TokenQueue("0")).matchesCS(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_179() throws Exception {
        // Combination: receiver__data="0", seq="a"
        Object actual = (new TokenQueue("0")).matchesCS("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_180() throws Exception {
        // Combination: receiver__data="0", seq="test123"
        Object actual = (new TokenQueue("0")).matchesCS("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_181() throws Exception {
        // Combination: receiver__data="0", seq="!@#"
        Object actual = (new TokenQueue("0")).matchesCS("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_182() throws Exception {
        // Combination: receiver__data="0", seq="0"
        Object actual = (new TokenQueue("0")).matchesCS("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_183() throws Exception {
        // Combination: receiver__data="0", seq="-1"
        Object actual = (new TokenQueue("0")).matchesCS("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_184() throws Exception {
        // Combination: receiver__data="0", seq="1.5"
        Object actual = (new TokenQueue("0")).matchesCS("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_185() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775807"
        Object actual = (new TokenQueue("0")).matchesCS("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_186() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775808"
        Object actual = (new TokenQueue("0")).matchesCS("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_187() throws Exception {
        // Combination: receiver__data="0", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("0")).matchesCS("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_188() throws Exception {
        // Combination: receiver__data="-1", seq=""
        Object actual = (new TokenQueue("-1")).matchesCS("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_189() throws Exception {
        // Combination: receiver__data="-1", seq=" "
        Object actual = (new TokenQueue("-1")).matchesCS(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_190() throws Exception {
        // Combination: receiver__data="-1", seq="a"
        Object actual = (new TokenQueue("-1")).matchesCS("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_191() throws Exception {
        // Combination: receiver__data="-1", seq="test123"
        Object actual = (new TokenQueue("-1")).matchesCS("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_192() throws Exception {
        // Combination: receiver__data="-1", seq="!@#"
        Object actual = (new TokenQueue("-1")).matchesCS("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_193() throws Exception {
        // Combination: receiver__data="-1", seq="0"
        Object actual = (new TokenQueue("-1")).matchesCS("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_194() throws Exception {
        // Combination: receiver__data="-1", seq="-1"
        Object actual = (new TokenQueue("-1")).matchesCS("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_195() throws Exception {
        // Combination: receiver__data="-1", seq="1.5"
        Object actual = (new TokenQueue("-1")).matchesCS("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_196() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775807"
        Object actual = (new TokenQueue("-1")).matchesCS("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_197() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775808"
        Object actual = (new TokenQueue("-1")).matchesCS("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_198() throws Exception {
        // Combination: receiver__data="-1", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("-1")).matchesCS("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_199() throws Exception {
        // Combination: receiver__data="1.5", seq=""
        Object actual = (new TokenQueue("1.5")).matchesCS("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_200() throws Exception {
        // Combination: receiver__data="1.5", seq=" "
        Object actual = (new TokenQueue("1.5")).matchesCS(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_201() throws Exception {
        // Combination: receiver__data="1.5", seq="a"
        Object actual = (new TokenQueue("1.5")).matchesCS("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_202() throws Exception {
        // Combination: receiver__data="1.5", seq="test123"
        Object actual = (new TokenQueue("1.5")).matchesCS("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_203() throws Exception {
        // Combination: receiver__data="1.5", seq="!@#"
        Object actual = (new TokenQueue("1.5")).matchesCS("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_204() throws Exception {
        // Combination: receiver__data="1.5", seq="0"
        Object actual = (new TokenQueue("1.5")).matchesCS("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_205() throws Exception {
        // Combination: receiver__data="1.5", seq="-1"
        Object actual = (new TokenQueue("1.5")).matchesCS("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_206() throws Exception {
        // Combination: receiver__data="1.5", seq="1.5"
        Object actual = (new TokenQueue("1.5")).matchesCS("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_207() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775807"
        Object actual = (new TokenQueue("1.5")).matchesCS("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_208() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775808"
        Object actual = (new TokenQueue("1.5")).matchesCS("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_209() throws Exception {
        // Combination: receiver__data="1.5", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("1.5")).matchesCS("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_210() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=""
        Object actual = (new TokenQueue("9223372036854775807")).matchesCS("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_211() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=" "
        Object actual = (new TokenQueue("9223372036854775807")).matchesCS(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_212() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="a"
        Object actual = (new TokenQueue("9223372036854775807")).matchesCS("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_213() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="test123"
        Object actual = (new TokenQueue("9223372036854775807")).matchesCS("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_214() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775807")).matchesCS("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_215() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="0"
        Object actual = (new TokenQueue("9223372036854775807")).matchesCS("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_216() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="-1"
        Object actual = (new TokenQueue("9223372036854775807")).matchesCS("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_217() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775807")).matchesCS("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_218() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775807")).matchesCS("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_219() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775807")).matchesCS("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_220() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775807")).matchesCS("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_221() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=""
        Object actual = (new TokenQueue("9223372036854775808")).matchesCS("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_222() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=" "
        Object actual = (new TokenQueue("9223372036854775808")).matchesCS(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_223() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="a"
        Object actual = (new TokenQueue("9223372036854775808")).matchesCS("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_224() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="test123"
        Object actual = (new TokenQueue("9223372036854775808")).matchesCS("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_225() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775808")).matchesCS("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_226() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="0"
        Object actual = (new TokenQueue("9223372036854775808")).matchesCS("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_227() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="-1"
        Object actual = (new TokenQueue("9223372036854775808")).matchesCS("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_228() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775808")).matchesCS("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_229() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775808")).matchesCS("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_230() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775808")).matchesCS("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_231() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775808")).matchesCS("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_232() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=""
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesCS("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_233() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=" "
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesCS(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_234() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="a"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesCS("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_235() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="test123"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesCS("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_236() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="!@#"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesCS("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_237() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="0"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesCS("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_238() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="-1"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesCS("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_239() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="1.5"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesCS("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_240() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775807"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesCS("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_241() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775808"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesCS("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesCS_pairwise_242() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesCS("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_243() throws Exception {
        // Combination: receiver__data="", seq=new String[] {}
        Object actual = (new TokenQueue("")).matchesAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_244() throws Exception {
        // Combination: receiver__data="", seq=new String[] {"value"}
        Object actual = (new TokenQueue("")).matchesAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_245() throws Exception {
        // Combination: receiver__data=" ", seq=new String[] {}
        Object actual = (new TokenQueue(" ")).matchesAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_246() throws Exception {
        // Combination: receiver__data=" ", seq=new String[] {"value"}
        Object actual = (new TokenQueue(" ")).matchesAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_247() throws Exception {
        // Combination: receiver__data="a", seq=new String[] {}
        Object actual = (new TokenQueue("a")).matchesAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_248() throws Exception {
        // Combination: receiver__data="a", seq=new String[] {"value"}
        Object actual = (new TokenQueue("a")).matchesAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_249() throws Exception {
        // Combination: receiver__data="test123", seq=new String[] {}
        Object actual = (new TokenQueue("test123")).matchesAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_250() throws Exception {
        // Combination: receiver__data="test123", seq=new String[] {"value"}
        Object actual = (new TokenQueue("test123")).matchesAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_251() throws Exception {
        // Combination: receiver__data="!@#", seq=new String[] {}
        Object actual = (new TokenQueue("!@#")).matchesAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_252() throws Exception {
        // Combination: receiver__data="!@#", seq=new String[] {"value"}
        Object actual = (new TokenQueue("!@#")).matchesAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_253() throws Exception {
        // Combination: receiver__data="0", seq=new String[] {}
        Object actual = (new TokenQueue("0")).matchesAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_254() throws Exception {
        // Combination: receiver__data="0", seq=new String[] {"value"}
        Object actual = (new TokenQueue("0")).matchesAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_255() throws Exception {
        // Combination: receiver__data="-1", seq=new String[] {}
        Object actual = (new TokenQueue("-1")).matchesAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_256() throws Exception {
        // Combination: receiver__data="-1", seq=new String[] {"value"}
        Object actual = (new TokenQueue("-1")).matchesAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_257() throws Exception {
        // Combination: receiver__data="1.5", seq=new String[] {}
        Object actual = (new TokenQueue("1.5")).matchesAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_258() throws Exception {
        // Combination: receiver__data="1.5", seq=new String[] {"value"}
        Object actual = (new TokenQueue("1.5")).matchesAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_259() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=new String[] {}
        Object actual = (new TokenQueue("9223372036854775807")).matchesAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_260() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=new String[] {"value"}
        Object actual = (new TokenQueue("9223372036854775807")).matchesAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_261() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=new String[] {}
        Object actual = (new TokenQueue("9223372036854775808")).matchesAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_262() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=new String[] {"value"}
        Object actual = (new TokenQueue("9223372036854775808")).matchesAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_263() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=new String[] {}
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_264() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=new String[] {"value"}
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_265() throws Exception {
        // Combination: receiver__data="", seq=new char[] {}
        Object actual = (new TokenQueue("")).matchesAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_266() throws Exception {
        // Combination: receiver__data="", seq=new char[] {1}
        Object actual = (new TokenQueue("")).matchesAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_267() throws Exception {
        // Combination: receiver__data=" ", seq=new char[] {}
        Object actual = (new TokenQueue(" ")).matchesAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_268() throws Exception {
        // Combination: receiver__data=" ", seq=new char[] {1}
        Object actual = (new TokenQueue(" ")).matchesAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_269() throws Exception {
        // Combination: receiver__data="a", seq=new char[] {}
        Object actual = (new TokenQueue("a")).matchesAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_270() throws Exception {
        // Combination: receiver__data="a", seq=new char[] {1}
        Object actual = (new TokenQueue("a")).matchesAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_271() throws Exception {
        // Combination: receiver__data="test123", seq=new char[] {}
        Object actual = (new TokenQueue("test123")).matchesAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_272() throws Exception {
        // Combination: receiver__data="test123", seq=new char[] {1}
        Object actual = (new TokenQueue("test123")).matchesAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_273() throws Exception {
        // Combination: receiver__data="!@#", seq=new char[] {}
        Object actual = (new TokenQueue("!@#")).matchesAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_274() throws Exception {
        // Combination: receiver__data="!@#", seq=new char[] {1}
        Object actual = (new TokenQueue("!@#")).matchesAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_275() throws Exception {
        // Combination: receiver__data="0", seq=new char[] {}
        Object actual = (new TokenQueue("0")).matchesAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_276() throws Exception {
        // Combination: receiver__data="0", seq=new char[] {1}
        Object actual = (new TokenQueue("0")).matchesAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_277() throws Exception {
        // Combination: receiver__data="-1", seq=new char[] {}
        Object actual = (new TokenQueue("-1")).matchesAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_278() throws Exception {
        // Combination: receiver__data="-1", seq=new char[] {1}
        Object actual = (new TokenQueue("-1")).matchesAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_279() throws Exception {
        // Combination: receiver__data="1.5", seq=new char[] {}
        Object actual = (new TokenQueue("1.5")).matchesAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_280() throws Exception {
        // Combination: receiver__data="1.5", seq=new char[] {1}
        Object actual = (new TokenQueue("1.5")).matchesAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_281() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=new char[] {}
        Object actual = (new TokenQueue("9223372036854775807")).matchesAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_282() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=new char[] {1}
        Object actual = (new TokenQueue("9223372036854775807")).matchesAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_283() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=new char[] {}
        Object actual = (new TokenQueue("9223372036854775808")).matchesAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_284() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=new char[] {1}
        Object actual = (new TokenQueue("9223372036854775808")).matchesAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_285() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=new char[] {}
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchesAny_pairwise_286() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=new char[] {1}
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchesAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_287() throws Exception {
        // Combination: receiver__data="", seq=""
        Object actual = (new TokenQueue("")).matchChomp("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_288() throws Exception {
        // Combination: receiver__data="", seq=" "
        Object actual = (new TokenQueue("")).matchChomp(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_289() throws Exception {
        // Combination: receiver__data="", seq="a"
        Object actual = (new TokenQueue("")).matchChomp("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_290() throws Exception {
        // Combination: receiver__data="", seq="test123"
        Object actual = (new TokenQueue("")).matchChomp("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_291() throws Exception {
        // Combination: receiver__data="", seq="!@#"
        Object actual = (new TokenQueue("")).matchChomp("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_292() throws Exception {
        // Combination: receiver__data="", seq="0"
        Object actual = (new TokenQueue("")).matchChomp("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_293() throws Exception {
        // Combination: receiver__data="", seq="-1"
        Object actual = (new TokenQueue("")).matchChomp("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_294() throws Exception {
        // Combination: receiver__data="", seq="1.5"
        Object actual = (new TokenQueue("")).matchChomp("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_295() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775807"
        Object actual = (new TokenQueue("")).matchChomp("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_296() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775808"
        Object actual = (new TokenQueue("")).matchChomp("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_297() throws Exception {
        // Combination: receiver__data="", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("")).matchChomp("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_298() throws Exception {
        // Combination: receiver__data=" ", seq=""
        Object actual = (new TokenQueue(" ")).matchChomp("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_299() throws Exception {
        // Combination: receiver__data=" ", seq=" "
        Object actual = (new TokenQueue(" ")).matchChomp(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_300() throws Exception {
        // Combination: receiver__data=" ", seq="a"
        Object actual = (new TokenQueue(" ")).matchChomp("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_301() throws Exception {
        // Combination: receiver__data=" ", seq="test123"
        Object actual = (new TokenQueue(" ")).matchChomp("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_302() throws Exception {
        // Combination: receiver__data=" ", seq="!@#"
        Object actual = (new TokenQueue(" ")).matchChomp("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_303() throws Exception {
        // Combination: receiver__data=" ", seq="0"
        Object actual = (new TokenQueue(" ")).matchChomp("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_304() throws Exception {
        // Combination: receiver__data=" ", seq="-1"
        Object actual = (new TokenQueue(" ")).matchChomp("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_305() throws Exception {
        // Combination: receiver__data=" ", seq="1.5"
        Object actual = (new TokenQueue(" ")).matchChomp("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_306() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775807"
        Object actual = (new TokenQueue(" ")).matchChomp("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_307() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775808"
        Object actual = (new TokenQueue(" ")).matchChomp("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_308() throws Exception {
        // Combination: receiver__data=" ", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue(" ")).matchChomp("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_309() throws Exception {
        // Combination: receiver__data="a", seq=""
        Object actual = (new TokenQueue("a")).matchChomp("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_310() throws Exception {
        // Combination: receiver__data="a", seq=" "
        Object actual = (new TokenQueue("a")).matchChomp(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_311() throws Exception {
        // Combination: receiver__data="a", seq="a"
        Object actual = (new TokenQueue("a")).matchChomp("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_312() throws Exception {
        // Combination: receiver__data="a", seq="test123"
        Object actual = (new TokenQueue("a")).matchChomp("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_313() throws Exception {
        // Combination: receiver__data="a", seq="!@#"
        Object actual = (new TokenQueue("a")).matchChomp("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_314() throws Exception {
        // Combination: receiver__data="a", seq="0"
        Object actual = (new TokenQueue("a")).matchChomp("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_315() throws Exception {
        // Combination: receiver__data="a", seq="-1"
        Object actual = (new TokenQueue("a")).matchChomp("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_316() throws Exception {
        // Combination: receiver__data="a", seq="1.5"
        Object actual = (new TokenQueue("a")).matchChomp("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_317() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775807"
        Object actual = (new TokenQueue("a")).matchChomp("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_318() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775808"
        Object actual = (new TokenQueue("a")).matchChomp("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_319() throws Exception {
        // Combination: receiver__data="a", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("a")).matchChomp("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_320() throws Exception {
        // Combination: receiver__data="test123", seq=""
        Object actual = (new TokenQueue("test123")).matchChomp("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_321() throws Exception {
        // Combination: receiver__data="test123", seq=" "
        Object actual = (new TokenQueue("test123")).matchChomp(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_322() throws Exception {
        // Combination: receiver__data="test123", seq="a"
        Object actual = (new TokenQueue("test123")).matchChomp("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_323() throws Exception {
        // Combination: receiver__data="test123", seq="test123"
        Object actual = (new TokenQueue("test123")).matchChomp("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_324() throws Exception {
        // Combination: receiver__data="test123", seq="!@#"
        Object actual = (new TokenQueue("test123")).matchChomp("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_325() throws Exception {
        // Combination: receiver__data="test123", seq="0"
        Object actual = (new TokenQueue("test123")).matchChomp("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_326() throws Exception {
        // Combination: receiver__data="test123", seq="-1"
        Object actual = (new TokenQueue("test123")).matchChomp("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_327() throws Exception {
        // Combination: receiver__data="test123", seq="1.5"
        Object actual = (new TokenQueue("test123")).matchChomp("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_328() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775807"
        Object actual = (new TokenQueue("test123")).matchChomp("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_329() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775808"
        Object actual = (new TokenQueue("test123")).matchChomp("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_330() throws Exception {
        // Combination: receiver__data="test123", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("test123")).matchChomp("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_331() throws Exception {
        // Combination: receiver__data="!@#", seq=""
        Object actual = (new TokenQueue("!@#")).matchChomp("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_332() throws Exception {
        // Combination: receiver__data="!@#", seq=" "
        Object actual = (new TokenQueue("!@#")).matchChomp(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_333() throws Exception {
        // Combination: receiver__data="!@#", seq="a"
        Object actual = (new TokenQueue("!@#")).matchChomp("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_334() throws Exception {
        // Combination: receiver__data="!@#", seq="test123"
        Object actual = (new TokenQueue("!@#")).matchChomp("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_335() throws Exception {
        // Combination: receiver__data="!@#", seq="!@#"
        Object actual = (new TokenQueue("!@#")).matchChomp("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_336() throws Exception {
        // Combination: receiver__data="!@#", seq="0"
        Object actual = (new TokenQueue("!@#")).matchChomp("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_337() throws Exception {
        // Combination: receiver__data="!@#", seq="-1"
        Object actual = (new TokenQueue("!@#")).matchChomp("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_338() throws Exception {
        // Combination: receiver__data="!@#", seq="1.5"
        Object actual = (new TokenQueue("!@#")).matchChomp("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_339() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775807"
        Object actual = (new TokenQueue("!@#")).matchChomp("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_340() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775808"
        Object actual = (new TokenQueue("!@#")).matchChomp("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_341() throws Exception {
        // Combination: receiver__data="!@#", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("!@#")).matchChomp("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_342() throws Exception {
        // Combination: receiver__data="0", seq=""
        Object actual = (new TokenQueue("0")).matchChomp("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_343() throws Exception {
        // Combination: receiver__data="0", seq=" "
        Object actual = (new TokenQueue("0")).matchChomp(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_344() throws Exception {
        // Combination: receiver__data="0", seq="a"
        Object actual = (new TokenQueue("0")).matchChomp("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_345() throws Exception {
        // Combination: receiver__data="0", seq="test123"
        Object actual = (new TokenQueue("0")).matchChomp("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_346() throws Exception {
        // Combination: receiver__data="0", seq="!@#"
        Object actual = (new TokenQueue("0")).matchChomp("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_347() throws Exception {
        // Combination: receiver__data="0", seq="0"
        Object actual = (new TokenQueue("0")).matchChomp("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_348() throws Exception {
        // Combination: receiver__data="0", seq="-1"
        Object actual = (new TokenQueue("0")).matchChomp("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_349() throws Exception {
        // Combination: receiver__data="0", seq="1.5"
        Object actual = (new TokenQueue("0")).matchChomp("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_350() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775807"
        Object actual = (new TokenQueue("0")).matchChomp("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_351() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775808"
        Object actual = (new TokenQueue("0")).matchChomp("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_352() throws Exception {
        // Combination: receiver__data="0", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("0")).matchChomp("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_353() throws Exception {
        // Combination: receiver__data="-1", seq=""
        Object actual = (new TokenQueue("-1")).matchChomp("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_354() throws Exception {
        // Combination: receiver__data="-1", seq=" "
        Object actual = (new TokenQueue("-1")).matchChomp(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_355() throws Exception {
        // Combination: receiver__data="-1", seq="a"
        Object actual = (new TokenQueue("-1")).matchChomp("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_356() throws Exception {
        // Combination: receiver__data="-1", seq="test123"
        Object actual = (new TokenQueue("-1")).matchChomp("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_357() throws Exception {
        // Combination: receiver__data="-1", seq="!@#"
        Object actual = (new TokenQueue("-1")).matchChomp("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_358() throws Exception {
        // Combination: receiver__data="-1", seq="0"
        Object actual = (new TokenQueue("-1")).matchChomp("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_359() throws Exception {
        // Combination: receiver__data="-1", seq="-1"
        Object actual = (new TokenQueue("-1")).matchChomp("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_360() throws Exception {
        // Combination: receiver__data="-1", seq="1.5"
        Object actual = (new TokenQueue("-1")).matchChomp("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_361() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775807"
        Object actual = (new TokenQueue("-1")).matchChomp("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_362() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775808"
        Object actual = (new TokenQueue("-1")).matchChomp("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_363() throws Exception {
        // Combination: receiver__data="-1", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("-1")).matchChomp("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_364() throws Exception {
        // Combination: receiver__data="1.5", seq=""
        Object actual = (new TokenQueue("1.5")).matchChomp("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_365() throws Exception {
        // Combination: receiver__data="1.5", seq=" "
        Object actual = (new TokenQueue("1.5")).matchChomp(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_366() throws Exception {
        // Combination: receiver__data="1.5", seq="a"
        Object actual = (new TokenQueue("1.5")).matchChomp("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_367() throws Exception {
        // Combination: receiver__data="1.5", seq="test123"
        Object actual = (new TokenQueue("1.5")).matchChomp("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_368() throws Exception {
        // Combination: receiver__data="1.5", seq="!@#"
        Object actual = (new TokenQueue("1.5")).matchChomp("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_369() throws Exception {
        // Combination: receiver__data="1.5", seq="0"
        Object actual = (new TokenQueue("1.5")).matchChomp("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_370() throws Exception {
        // Combination: receiver__data="1.5", seq="-1"
        Object actual = (new TokenQueue("1.5")).matchChomp("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_371() throws Exception {
        // Combination: receiver__data="1.5", seq="1.5"
        Object actual = (new TokenQueue("1.5")).matchChomp("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_372() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775807"
        Object actual = (new TokenQueue("1.5")).matchChomp("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_373() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775808"
        Object actual = (new TokenQueue("1.5")).matchChomp("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_374() throws Exception {
        // Combination: receiver__data="1.5", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("1.5")).matchChomp("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_375() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=""
        Object actual = (new TokenQueue("9223372036854775807")).matchChomp("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_376() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=" "
        Object actual = (new TokenQueue("9223372036854775807")).matchChomp(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_377() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="a"
        Object actual = (new TokenQueue("9223372036854775807")).matchChomp("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_378() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="test123"
        Object actual = (new TokenQueue("9223372036854775807")).matchChomp("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_379() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775807")).matchChomp("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_380() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="0"
        Object actual = (new TokenQueue("9223372036854775807")).matchChomp("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_381() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="-1"
        Object actual = (new TokenQueue("9223372036854775807")).matchChomp("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_382() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775807")).matchChomp("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_383() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775807")).matchChomp("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_384() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775807")).matchChomp("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_385() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775807")).matchChomp("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_386() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=""
        Object actual = (new TokenQueue("9223372036854775808")).matchChomp("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_387() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=" "
        Object actual = (new TokenQueue("9223372036854775808")).matchChomp(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_388() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="a"
        Object actual = (new TokenQueue("9223372036854775808")).matchChomp("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_389() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="test123"
        Object actual = (new TokenQueue("9223372036854775808")).matchChomp("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_390() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775808")).matchChomp("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_391() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="0"
        Object actual = (new TokenQueue("9223372036854775808")).matchChomp("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_392() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="-1"
        Object actual = (new TokenQueue("9223372036854775808")).matchChomp("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_393() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775808")).matchChomp("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_394() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775808")).matchChomp("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_395() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775808")).matchChomp("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_396() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775808")).matchChomp("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_397() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=""
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchChomp("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_398() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=" "
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchChomp(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_399() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="a"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchChomp("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_400() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="test123"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchChomp("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_401() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="!@#"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchChomp("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_402() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="0"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchChomp("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_403() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="-1"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchChomp("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_404() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="1.5"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchChomp("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_405() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775807"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchChomp("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_406() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775808"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchChomp("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matchChomp_pairwise_407() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).matchChomp("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_408() throws Exception {
        // Combination: receiver__data="", seq=""
        Object actual = (new TokenQueue("")).consumeTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_409() throws Exception {
        // Combination: receiver__data="", seq=" "
        Object actual = (new TokenQueue("")).consumeTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_410() throws Exception {
        // Combination: receiver__data="", seq="a"
        Object actual = (new TokenQueue("")).consumeTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_411() throws Exception {
        // Combination: receiver__data="", seq="test123"
        Object actual = (new TokenQueue("")).consumeTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_412() throws Exception {
        // Combination: receiver__data="", seq="!@#"
        Object actual = (new TokenQueue("")).consumeTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_413() throws Exception {
        // Combination: receiver__data="", seq="0"
        Object actual = (new TokenQueue("")).consumeTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_414() throws Exception {
        // Combination: receiver__data="", seq="-1"
        Object actual = (new TokenQueue("")).consumeTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_415() throws Exception {
        // Combination: receiver__data="", seq="1.5"
        Object actual = (new TokenQueue("")).consumeTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_416() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775807"
        Object actual = (new TokenQueue("")).consumeTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_417() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775808"
        Object actual = (new TokenQueue("")).consumeTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_418() throws Exception {
        // Combination: receiver__data="", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("")).consumeTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_419() throws Exception {
        // Combination: receiver__data=" ", seq=""
        Object actual = (new TokenQueue(" ")).consumeTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_420() throws Exception {
        // Combination: receiver__data=" ", seq=" "
        Object actual = (new TokenQueue(" ")).consumeTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_421() throws Exception {
        // Combination: receiver__data=" ", seq="a"
        Object actual = (new TokenQueue(" ")).consumeTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_422() throws Exception {
        // Combination: receiver__data=" ", seq="test123"
        Object actual = (new TokenQueue(" ")).consumeTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_423() throws Exception {
        // Combination: receiver__data=" ", seq="!@#"
        Object actual = (new TokenQueue(" ")).consumeTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_424() throws Exception {
        // Combination: receiver__data=" ", seq="0"
        Object actual = (new TokenQueue(" ")).consumeTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_425() throws Exception {
        // Combination: receiver__data=" ", seq="-1"
        Object actual = (new TokenQueue(" ")).consumeTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_426() throws Exception {
        // Combination: receiver__data=" ", seq="1.5"
        Object actual = (new TokenQueue(" ")).consumeTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_427() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775807"
        Object actual = (new TokenQueue(" ")).consumeTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_428() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775808"
        Object actual = (new TokenQueue(" ")).consumeTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_429() throws Exception {
        // Combination: receiver__data=" ", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue(" ")).consumeTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_430() throws Exception {
        // Combination: receiver__data="a", seq=""
        Object actual = (new TokenQueue("a")).consumeTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_431() throws Exception {
        // Combination: receiver__data="a", seq=" "
        Object actual = (new TokenQueue("a")).consumeTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_432() throws Exception {
        // Combination: receiver__data="a", seq="a"
        Object actual = (new TokenQueue("a")).consumeTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_433() throws Exception {
        // Combination: receiver__data="a", seq="test123"
        Object actual = (new TokenQueue("a")).consumeTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_434() throws Exception {
        // Combination: receiver__data="a", seq="!@#"
        Object actual = (new TokenQueue("a")).consumeTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_435() throws Exception {
        // Combination: receiver__data="a", seq="0"
        Object actual = (new TokenQueue("a")).consumeTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_436() throws Exception {
        // Combination: receiver__data="a", seq="-1"
        Object actual = (new TokenQueue("a")).consumeTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_437() throws Exception {
        // Combination: receiver__data="a", seq="1.5"
        Object actual = (new TokenQueue("a")).consumeTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_438() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775807"
        Object actual = (new TokenQueue("a")).consumeTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_439() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775808"
        Object actual = (new TokenQueue("a")).consumeTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_440() throws Exception {
        // Combination: receiver__data="a", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("a")).consumeTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_441() throws Exception {
        // Combination: receiver__data="test123", seq=""
        Object actual = (new TokenQueue("test123")).consumeTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_442() throws Exception {
        // Combination: receiver__data="test123", seq=" "
        Object actual = (new TokenQueue("test123")).consumeTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_443() throws Exception {
        // Combination: receiver__data="test123", seq="a"
        Object actual = (new TokenQueue("test123")).consumeTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_444() throws Exception {
        // Combination: receiver__data="test123", seq="test123"
        Object actual = (new TokenQueue("test123")).consumeTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_445() throws Exception {
        // Combination: receiver__data="test123", seq="!@#"
        Object actual = (new TokenQueue("test123")).consumeTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_446() throws Exception {
        // Combination: receiver__data="test123", seq="0"
        Object actual = (new TokenQueue("test123")).consumeTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_447() throws Exception {
        // Combination: receiver__data="test123", seq="-1"
        Object actual = (new TokenQueue("test123")).consumeTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_448() throws Exception {
        // Combination: receiver__data="test123", seq="1.5"
        Object actual = (new TokenQueue("test123")).consumeTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_449() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775807"
        Object actual = (new TokenQueue("test123")).consumeTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_450() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775808"
        Object actual = (new TokenQueue("test123")).consumeTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_451() throws Exception {
        // Combination: receiver__data="test123", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("test123")).consumeTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_452() throws Exception {
        // Combination: receiver__data="!@#", seq=""
        Object actual = (new TokenQueue("!@#")).consumeTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_453() throws Exception {
        // Combination: receiver__data="!@#", seq=" "
        Object actual = (new TokenQueue("!@#")).consumeTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_454() throws Exception {
        // Combination: receiver__data="!@#", seq="a"
        Object actual = (new TokenQueue("!@#")).consumeTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_455() throws Exception {
        // Combination: receiver__data="!@#", seq="test123"
        Object actual = (new TokenQueue("!@#")).consumeTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_456() throws Exception {
        // Combination: receiver__data="!@#", seq="!@#"
        Object actual = (new TokenQueue("!@#")).consumeTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_457() throws Exception {
        // Combination: receiver__data="!@#", seq="0"
        Object actual = (new TokenQueue("!@#")).consumeTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_458() throws Exception {
        // Combination: receiver__data="!@#", seq="-1"
        Object actual = (new TokenQueue("!@#")).consumeTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_459() throws Exception {
        // Combination: receiver__data="!@#", seq="1.5"
        Object actual = (new TokenQueue("!@#")).consumeTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_460() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775807"
        Object actual = (new TokenQueue("!@#")).consumeTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_461() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775808"
        Object actual = (new TokenQueue("!@#")).consumeTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_462() throws Exception {
        // Combination: receiver__data="!@#", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("!@#")).consumeTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_463() throws Exception {
        // Combination: receiver__data="0", seq=""
        Object actual = (new TokenQueue("0")).consumeTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_464() throws Exception {
        // Combination: receiver__data="0", seq=" "
        Object actual = (new TokenQueue("0")).consumeTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_465() throws Exception {
        // Combination: receiver__data="0", seq="a"
        Object actual = (new TokenQueue("0")).consumeTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_466() throws Exception {
        // Combination: receiver__data="0", seq="test123"
        Object actual = (new TokenQueue("0")).consumeTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_467() throws Exception {
        // Combination: receiver__data="0", seq="!@#"
        Object actual = (new TokenQueue("0")).consumeTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_468() throws Exception {
        // Combination: receiver__data="0", seq="0"
        Object actual = (new TokenQueue("0")).consumeTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_469() throws Exception {
        // Combination: receiver__data="0", seq="-1"
        Object actual = (new TokenQueue("0")).consumeTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_470() throws Exception {
        // Combination: receiver__data="0", seq="1.5"
        Object actual = (new TokenQueue("0")).consumeTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_471() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775807"
        Object actual = (new TokenQueue("0")).consumeTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_472() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775808"
        Object actual = (new TokenQueue("0")).consumeTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_473() throws Exception {
        // Combination: receiver__data="0", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("0")).consumeTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_474() throws Exception {
        // Combination: receiver__data="-1", seq=""
        Object actual = (new TokenQueue("-1")).consumeTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_475() throws Exception {
        // Combination: receiver__data="-1", seq=" "
        Object actual = (new TokenQueue("-1")).consumeTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_476() throws Exception {
        // Combination: receiver__data="-1", seq="a"
        Object actual = (new TokenQueue("-1")).consumeTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_477() throws Exception {
        // Combination: receiver__data="-1", seq="test123"
        Object actual = (new TokenQueue("-1")).consumeTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_478() throws Exception {
        // Combination: receiver__data="-1", seq="!@#"
        Object actual = (new TokenQueue("-1")).consumeTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_479() throws Exception {
        // Combination: receiver__data="-1", seq="0"
        Object actual = (new TokenQueue("-1")).consumeTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_480() throws Exception {
        // Combination: receiver__data="-1", seq="-1"
        Object actual = (new TokenQueue("-1")).consumeTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_481() throws Exception {
        // Combination: receiver__data="-1", seq="1.5"
        Object actual = (new TokenQueue("-1")).consumeTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_482() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775807"
        Object actual = (new TokenQueue("-1")).consumeTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_483() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775808"
        Object actual = (new TokenQueue("-1")).consumeTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_484() throws Exception {
        // Combination: receiver__data="-1", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("-1")).consumeTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_485() throws Exception {
        // Combination: receiver__data="1.5", seq=""
        Object actual = (new TokenQueue("1.5")).consumeTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_486() throws Exception {
        // Combination: receiver__data="1.5", seq=" "
        Object actual = (new TokenQueue("1.5")).consumeTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_487() throws Exception {
        // Combination: receiver__data="1.5", seq="a"
        Object actual = (new TokenQueue("1.5")).consumeTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_488() throws Exception {
        // Combination: receiver__data="1.5", seq="test123"
        Object actual = (new TokenQueue("1.5")).consumeTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_489() throws Exception {
        // Combination: receiver__data="1.5", seq="!@#"
        Object actual = (new TokenQueue("1.5")).consumeTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_490() throws Exception {
        // Combination: receiver__data="1.5", seq="0"
        Object actual = (new TokenQueue("1.5")).consumeTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_491() throws Exception {
        // Combination: receiver__data="1.5", seq="-1"
        Object actual = (new TokenQueue("1.5")).consumeTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_492() throws Exception {
        // Combination: receiver__data="1.5", seq="1.5"
        Object actual = (new TokenQueue("1.5")).consumeTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_493() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775807"
        Object actual = (new TokenQueue("1.5")).consumeTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_494() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775808"
        Object actual = (new TokenQueue("1.5")).consumeTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_495() throws Exception {
        // Combination: receiver__data="1.5", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("1.5")).consumeTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_496() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=""
        Object actual = (new TokenQueue("9223372036854775807")).consumeTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_497() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=" "
        Object actual = (new TokenQueue("9223372036854775807")).consumeTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_498() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="a"
        Object actual = (new TokenQueue("9223372036854775807")).consumeTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_499() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="test123"
        Object actual = (new TokenQueue("9223372036854775807")).consumeTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_500() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775807")).consumeTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_501() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="0"
        Object actual = (new TokenQueue("9223372036854775807")).consumeTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_502() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="-1"
        Object actual = (new TokenQueue("9223372036854775807")).consumeTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_503() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775807")).consumeTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_504() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775807")).consumeTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_505() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775807")).consumeTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_506() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775807")).consumeTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_507() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=""
        Object actual = (new TokenQueue("9223372036854775808")).consumeTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_508() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=" "
        Object actual = (new TokenQueue("9223372036854775808")).consumeTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_509() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="a"
        Object actual = (new TokenQueue("9223372036854775808")).consumeTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_510() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="test123"
        Object actual = (new TokenQueue("9223372036854775808")).consumeTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_511() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775808")).consumeTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_512() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="0"
        Object actual = (new TokenQueue("9223372036854775808")).consumeTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_513() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="-1"
        Object actual = (new TokenQueue("9223372036854775808")).consumeTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_514() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775808")).consumeTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_515() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775808")).consumeTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_516() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775808")).consumeTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_517() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775808")).consumeTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_518() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=""
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_519() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=" "
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_520() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="a"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_521() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="test123"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_522() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="!@#"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_523() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="0"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_524() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="-1"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_525() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="1.5"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_526() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775807"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_527() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775808"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_528() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_529() throws Exception {
        // Combination: receiver__data="", seq=""
        try {
            (new TokenQueue("")).consumeToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_530() throws Exception {
        // Combination: receiver__data="", seq=" "
        Object actual = (new TokenQueue("")).consumeToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_531() throws Exception {
        // Combination: receiver__data="", seq="a"
        Object actual = (new TokenQueue("")).consumeToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_532() throws Exception {
        // Combination: receiver__data="", seq="test123"
        Object actual = (new TokenQueue("")).consumeToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_533() throws Exception {
        // Combination: receiver__data="", seq="!@#"
        Object actual = (new TokenQueue("")).consumeToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_534() throws Exception {
        // Combination: receiver__data="", seq="0"
        Object actual = (new TokenQueue("")).consumeToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_535() throws Exception {
        // Combination: receiver__data="", seq="-1"
        Object actual = (new TokenQueue("")).consumeToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_536() throws Exception {
        // Combination: receiver__data="", seq="1.5"
        Object actual = (new TokenQueue("")).consumeToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_537() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775807"
        Object actual = (new TokenQueue("")).consumeToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_538() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775808"
        Object actual = (new TokenQueue("")).consumeToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_539() throws Exception {
        // Combination: receiver__data="", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("")).consumeToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_540() throws Exception {
        // Combination: receiver__data=" ", seq=""
        try {
            (new TokenQueue(" ")).consumeToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_541() throws Exception {
        // Combination: receiver__data=" ", seq=" "
        Object actual = (new TokenQueue(" ")).consumeToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_542() throws Exception {
        // Combination: receiver__data=" ", seq="a"
        Object actual = (new TokenQueue(" ")).consumeToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_543() throws Exception {
        // Combination: receiver__data=" ", seq="test123"
        Object actual = (new TokenQueue(" ")).consumeToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_544() throws Exception {
        // Combination: receiver__data=" ", seq="!@#"
        Object actual = (new TokenQueue(" ")).consumeToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_545() throws Exception {
        // Combination: receiver__data=" ", seq="0"
        Object actual = (new TokenQueue(" ")).consumeToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_546() throws Exception {
        // Combination: receiver__data=" ", seq="-1"
        Object actual = (new TokenQueue(" ")).consumeToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_547() throws Exception {
        // Combination: receiver__data=" ", seq="1.5"
        Object actual = (new TokenQueue(" ")).consumeToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_548() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775807"
        Object actual = (new TokenQueue(" ")).consumeToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_549() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775808"
        Object actual = (new TokenQueue(" ")).consumeToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_550() throws Exception {
        // Combination: receiver__data=" ", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue(" ")).consumeToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_551() throws Exception {
        // Combination: receiver__data="a", seq=""
        try {
            (new TokenQueue("a")).consumeToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_552() throws Exception {
        // Combination: receiver__data="a", seq=" "
        Object actual = (new TokenQueue("a")).consumeToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_553() throws Exception {
        // Combination: receiver__data="a", seq="a"
        Object actual = (new TokenQueue("a")).consumeToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_554() throws Exception {
        // Combination: receiver__data="a", seq="test123"
        Object actual = (new TokenQueue("a")).consumeToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_555() throws Exception {
        // Combination: receiver__data="a", seq="!@#"
        Object actual = (new TokenQueue("a")).consumeToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_556() throws Exception {
        // Combination: receiver__data="a", seq="0"
        Object actual = (new TokenQueue("a")).consumeToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_557() throws Exception {
        // Combination: receiver__data="a", seq="-1"
        Object actual = (new TokenQueue("a")).consumeToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_558() throws Exception {
        // Combination: receiver__data="a", seq="1.5"
        Object actual = (new TokenQueue("a")).consumeToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_559() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775807"
        Object actual = (new TokenQueue("a")).consumeToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_560() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775808"
        Object actual = (new TokenQueue("a")).consumeToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_561() throws Exception {
        // Combination: receiver__data="a", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("a")).consumeToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_562() throws Exception {
        // Combination: receiver__data="test123", seq=""
        try {
            (new TokenQueue("test123")).consumeToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_563() throws Exception {
        // Combination: receiver__data="test123", seq=" "
        Object actual = (new TokenQueue("test123")).consumeToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_564() throws Exception {
        // Combination: receiver__data="test123", seq="a"
        Object actual = (new TokenQueue("test123")).consumeToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_565() throws Exception {
        // Combination: receiver__data="test123", seq="test123"
        Object actual = (new TokenQueue("test123")).consumeToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_566() throws Exception {
        // Combination: receiver__data="test123", seq="!@#"
        Object actual = (new TokenQueue("test123")).consumeToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_567() throws Exception {
        // Combination: receiver__data="test123", seq="0"
        Object actual = (new TokenQueue("test123")).consumeToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_568() throws Exception {
        // Combination: receiver__data="test123", seq="-1"
        Object actual = (new TokenQueue("test123")).consumeToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_569() throws Exception {
        // Combination: receiver__data="test123", seq="1.5"
        Object actual = (new TokenQueue("test123")).consumeToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_570() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775807"
        Object actual = (new TokenQueue("test123")).consumeToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_571() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775808"
        Object actual = (new TokenQueue("test123")).consumeToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_572() throws Exception {
        // Combination: receiver__data="test123", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("test123")).consumeToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_573() throws Exception {
        // Combination: receiver__data="!@#", seq=""
        try {
            (new TokenQueue("!@#")).consumeToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_574() throws Exception {
        // Combination: receiver__data="!@#", seq=" "
        Object actual = (new TokenQueue("!@#")).consumeToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_575() throws Exception {
        // Combination: receiver__data="!@#", seq="a"
        Object actual = (new TokenQueue("!@#")).consumeToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_576() throws Exception {
        // Combination: receiver__data="!@#", seq="test123"
        Object actual = (new TokenQueue("!@#")).consumeToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_577() throws Exception {
        // Combination: receiver__data="!@#", seq="!@#"
        Object actual = (new TokenQueue("!@#")).consumeToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_578() throws Exception {
        // Combination: receiver__data="!@#", seq="0"
        Object actual = (new TokenQueue("!@#")).consumeToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_579() throws Exception {
        // Combination: receiver__data="!@#", seq="-1"
        Object actual = (new TokenQueue("!@#")).consumeToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_580() throws Exception {
        // Combination: receiver__data="!@#", seq="1.5"
        Object actual = (new TokenQueue("!@#")).consumeToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_581() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775807"
        Object actual = (new TokenQueue("!@#")).consumeToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_582() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775808"
        Object actual = (new TokenQueue("!@#")).consumeToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_583() throws Exception {
        // Combination: receiver__data="!@#", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("!@#")).consumeToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_584() throws Exception {
        // Combination: receiver__data="0", seq=""
        try {
            (new TokenQueue("0")).consumeToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_585() throws Exception {
        // Combination: receiver__data="0", seq=" "
        Object actual = (new TokenQueue("0")).consumeToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_586() throws Exception {
        // Combination: receiver__data="0", seq="a"
        Object actual = (new TokenQueue("0")).consumeToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_587() throws Exception {
        // Combination: receiver__data="0", seq="test123"
        Object actual = (new TokenQueue("0")).consumeToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_588() throws Exception {
        // Combination: receiver__data="0", seq="!@#"
        Object actual = (new TokenQueue("0")).consumeToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_589() throws Exception {
        // Combination: receiver__data="0", seq="0"
        Object actual = (new TokenQueue("0")).consumeToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_590() throws Exception {
        // Combination: receiver__data="0", seq="-1"
        Object actual = (new TokenQueue("0")).consumeToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_591() throws Exception {
        // Combination: receiver__data="0", seq="1.5"
        Object actual = (new TokenQueue("0")).consumeToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_592() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775807"
        Object actual = (new TokenQueue("0")).consumeToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_593() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775808"
        Object actual = (new TokenQueue("0")).consumeToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_594() throws Exception {
        // Combination: receiver__data="0", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("0")).consumeToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_595() throws Exception {
        // Combination: receiver__data="-1", seq=""
        try {
            (new TokenQueue("-1")).consumeToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_596() throws Exception {
        // Combination: receiver__data="-1", seq=" "
        Object actual = (new TokenQueue("-1")).consumeToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_597() throws Exception {
        // Combination: receiver__data="-1", seq="a"
        Object actual = (new TokenQueue("-1")).consumeToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_598() throws Exception {
        // Combination: receiver__data="-1", seq="test123"
        Object actual = (new TokenQueue("-1")).consumeToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_599() throws Exception {
        // Combination: receiver__data="-1", seq="!@#"
        Object actual = (new TokenQueue("-1")).consumeToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_600() throws Exception {
        // Combination: receiver__data="-1", seq="0"
        Object actual = (new TokenQueue("-1")).consumeToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_601() throws Exception {
        // Combination: receiver__data="-1", seq="-1"
        Object actual = (new TokenQueue("-1")).consumeToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_602() throws Exception {
        // Combination: receiver__data="-1", seq="1.5"
        Object actual = (new TokenQueue("-1")).consumeToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_603() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775807"
        Object actual = (new TokenQueue("-1")).consumeToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_604() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775808"
        Object actual = (new TokenQueue("-1")).consumeToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_605() throws Exception {
        // Combination: receiver__data="-1", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("-1")).consumeToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_606() throws Exception {
        // Combination: receiver__data="1.5", seq=""
        try {
            (new TokenQueue("1.5")).consumeToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_607() throws Exception {
        // Combination: receiver__data="1.5", seq=" "
        Object actual = (new TokenQueue("1.5")).consumeToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_608() throws Exception {
        // Combination: receiver__data="1.5", seq="a"
        Object actual = (new TokenQueue("1.5")).consumeToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_609() throws Exception {
        // Combination: receiver__data="1.5", seq="test123"
        Object actual = (new TokenQueue("1.5")).consumeToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_610() throws Exception {
        // Combination: receiver__data="1.5", seq="!@#"
        Object actual = (new TokenQueue("1.5")).consumeToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_611() throws Exception {
        // Combination: receiver__data="1.5", seq="0"
        Object actual = (new TokenQueue("1.5")).consumeToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_612() throws Exception {
        // Combination: receiver__data="1.5", seq="-1"
        Object actual = (new TokenQueue("1.5")).consumeToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_613() throws Exception {
        // Combination: receiver__data="1.5", seq="1.5"
        Object actual = (new TokenQueue("1.5")).consumeToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_614() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775807"
        Object actual = (new TokenQueue("1.5")).consumeToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_615() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775808"
        Object actual = (new TokenQueue("1.5")).consumeToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_616() throws Exception {
        // Combination: receiver__data="1.5", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("1.5")).consumeToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_617() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=""
        try {
            (new TokenQueue("9223372036854775807")).consumeToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_618() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=" "
        Object actual = (new TokenQueue("9223372036854775807")).consumeToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_619() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="a"
        Object actual = (new TokenQueue("9223372036854775807")).consumeToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_620() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="test123"
        Object actual = (new TokenQueue("9223372036854775807")).consumeToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_621() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775807")).consumeToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_622() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="0"
        Object actual = (new TokenQueue("9223372036854775807")).consumeToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_623() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="-1"
        Object actual = (new TokenQueue("9223372036854775807")).consumeToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_624() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775807")).consumeToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_625() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775807")).consumeToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_626() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775807")).consumeToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_627() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775807")).consumeToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_628() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=""
        try {
            (new TokenQueue("9223372036854775808")).consumeToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_629() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=" "
        Object actual = (new TokenQueue("9223372036854775808")).consumeToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_630() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="a"
        Object actual = (new TokenQueue("9223372036854775808")).consumeToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_631() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="test123"
        Object actual = (new TokenQueue("9223372036854775808")).consumeToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_632() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775808")).consumeToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_633() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="0"
        Object actual = (new TokenQueue("9223372036854775808")).consumeToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_634() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="-1"
        Object actual = (new TokenQueue("9223372036854775808")).consumeToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_635() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775808")).consumeToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_636() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775808")).consumeToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_637() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775808")).consumeToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_638() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775808")).consumeToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_639() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=""
        try {
            (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_640() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=" "
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_641() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="a"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_642() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="test123"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_643() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="!@#"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_644() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="0"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_645() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="-1"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_646() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="1.5"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_647() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775807"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_648() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775808"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToIgnoreCase_pairwise_649() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_650() throws Exception {
        // Combination: receiver__data="", seq=new String[] {}
        Object actual = (new TokenQueue("")).consumeToAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_651() throws Exception {
        // Combination: receiver__data="", seq=new String[] {"value"}
        Object actual = (new TokenQueue("")).consumeToAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_652() throws Exception {
        // Combination: receiver__data=" ", seq=new String[] {}
        Object actual = (new TokenQueue(" ")).consumeToAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_653() throws Exception {
        // Combination: receiver__data=" ", seq=new String[] {"value"}
        Object actual = (new TokenQueue(" ")).consumeToAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_654() throws Exception {
        // Combination: receiver__data="a", seq=new String[] {}
        Object actual = (new TokenQueue("a")).consumeToAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_655() throws Exception {
        // Combination: receiver__data="a", seq=new String[] {"value"}
        Object actual = (new TokenQueue("a")).consumeToAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_656() throws Exception {
        // Combination: receiver__data="test123", seq=new String[] {}
        Object actual = (new TokenQueue("test123")).consumeToAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_657() throws Exception {
        // Combination: receiver__data="test123", seq=new String[] {"value"}
        Object actual = (new TokenQueue("test123")).consumeToAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_658() throws Exception {
        // Combination: receiver__data="!@#", seq=new String[] {}
        Object actual = (new TokenQueue("!@#")).consumeToAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_659() throws Exception {
        // Combination: receiver__data="!@#", seq=new String[] {"value"}
        Object actual = (new TokenQueue("!@#")).consumeToAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_660() throws Exception {
        // Combination: receiver__data="0", seq=new String[] {}
        Object actual = (new TokenQueue("0")).consumeToAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_661() throws Exception {
        // Combination: receiver__data="0", seq=new String[] {"value"}
        Object actual = (new TokenQueue("0")).consumeToAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_662() throws Exception {
        // Combination: receiver__data="-1", seq=new String[] {}
        Object actual = (new TokenQueue("-1")).consumeToAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_663() throws Exception {
        // Combination: receiver__data="-1", seq=new String[] {"value"}
        Object actual = (new TokenQueue("-1")).consumeToAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_664() throws Exception {
        // Combination: receiver__data="1.5", seq=new String[] {}
        Object actual = (new TokenQueue("1.5")).consumeToAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_665() throws Exception {
        // Combination: receiver__data="1.5", seq=new String[] {"value"}
        Object actual = (new TokenQueue("1.5")).consumeToAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_666() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=new String[] {}
        Object actual = (new TokenQueue("9223372036854775807")).consumeToAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_667() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=new String[] {"value"}
        Object actual = (new TokenQueue("9223372036854775807")).consumeToAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_668() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=new String[] {}
        Object actual = (new TokenQueue("9223372036854775808")).consumeToAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_669() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=new String[] {"value"}
        Object actual = (new TokenQueue("9223372036854775808")).consumeToAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_670() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=new String[] {}
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToAny(new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_671() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=new String[] {"value"}
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).consumeToAny(new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_672() throws Exception {
        // Combination: receiver__data="", seq=""
        Object actual = (new TokenQueue("")).chompTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_673() throws Exception {
        // Combination: receiver__data="", seq=" "
        Object actual = (new TokenQueue("")).chompTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_674() throws Exception {
        // Combination: receiver__data="", seq="a"
        Object actual = (new TokenQueue("")).chompTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_675() throws Exception {
        // Combination: receiver__data="", seq="test123"
        Object actual = (new TokenQueue("")).chompTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_676() throws Exception {
        // Combination: receiver__data="", seq="!@#"
        Object actual = (new TokenQueue("")).chompTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_677() throws Exception {
        // Combination: receiver__data="", seq="0"
        Object actual = (new TokenQueue("")).chompTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_678() throws Exception {
        // Combination: receiver__data="", seq="-1"
        Object actual = (new TokenQueue("")).chompTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_679() throws Exception {
        // Combination: receiver__data="", seq="1.5"
        Object actual = (new TokenQueue("")).chompTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_680() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775807"
        Object actual = (new TokenQueue("")).chompTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_681() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775808"
        Object actual = (new TokenQueue("")).chompTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_682() throws Exception {
        // Combination: receiver__data="", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("")).chompTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_683() throws Exception {
        // Combination: receiver__data=" ", seq=""
        Object actual = (new TokenQueue(" ")).chompTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_684() throws Exception {
        // Combination: receiver__data=" ", seq=" "
        Object actual = (new TokenQueue(" ")).chompTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_685() throws Exception {
        // Combination: receiver__data=" ", seq="a"
        Object actual = (new TokenQueue(" ")).chompTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_686() throws Exception {
        // Combination: receiver__data=" ", seq="test123"
        Object actual = (new TokenQueue(" ")).chompTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_687() throws Exception {
        // Combination: receiver__data=" ", seq="!@#"
        Object actual = (new TokenQueue(" ")).chompTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_688() throws Exception {
        // Combination: receiver__data=" ", seq="0"
        Object actual = (new TokenQueue(" ")).chompTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_689() throws Exception {
        // Combination: receiver__data=" ", seq="-1"
        Object actual = (new TokenQueue(" ")).chompTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_690() throws Exception {
        // Combination: receiver__data=" ", seq="1.5"
        Object actual = (new TokenQueue(" ")).chompTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_691() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775807"
        Object actual = (new TokenQueue(" ")).chompTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_692() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775808"
        Object actual = (new TokenQueue(" ")).chompTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_693() throws Exception {
        // Combination: receiver__data=" ", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue(" ")).chompTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_694() throws Exception {
        // Combination: receiver__data="a", seq=""
        Object actual = (new TokenQueue("a")).chompTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_695() throws Exception {
        // Combination: receiver__data="a", seq=" "
        Object actual = (new TokenQueue("a")).chompTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_696() throws Exception {
        // Combination: receiver__data="a", seq="a"
        Object actual = (new TokenQueue("a")).chompTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_697() throws Exception {
        // Combination: receiver__data="a", seq="test123"
        Object actual = (new TokenQueue("a")).chompTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_698() throws Exception {
        // Combination: receiver__data="a", seq="!@#"
        Object actual = (new TokenQueue("a")).chompTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_699() throws Exception {
        // Combination: receiver__data="a", seq="0"
        Object actual = (new TokenQueue("a")).chompTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_700() throws Exception {
        // Combination: receiver__data="a", seq="-1"
        Object actual = (new TokenQueue("a")).chompTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_701() throws Exception {
        // Combination: receiver__data="a", seq="1.5"
        Object actual = (new TokenQueue("a")).chompTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_702() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775807"
        Object actual = (new TokenQueue("a")).chompTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_703() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775808"
        Object actual = (new TokenQueue("a")).chompTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_704() throws Exception {
        // Combination: receiver__data="a", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("a")).chompTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_705() throws Exception {
        // Combination: receiver__data="test123", seq=""
        Object actual = (new TokenQueue("test123")).chompTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_706() throws Exception {
        // Combination: receiver__data="test123", seq=" "
        Object actual = (new TokenQueue("test123")).chompTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_707() throws Exception {
        // Combination: receiver__data="test123", seq="a"
        Object actual = (new TokenQueue("test123")).chompTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_708() throws Exception {
        // Combination: receiver__data="test123", seq="test123"
        Object actual = (new TokenQueue("test123")).chompTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_709() throws Exception {
        // Combination: receiver__data="test123", seq="!@#"
        Object actual = (new TokenQueue("test123")).chompTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_710() throws Exception {
        // Combination: receiver__data="test123", seq="0"
        Object actual = (new TokenQueue("test123")).chompTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_711() throws Exception {
        // Combination: receiver__data="test123", seq="-1"
        Object actual = (new TokenQueue("test123")).chompTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_712() throws Exception {
        // Combination: receiver__data="test123", seq="1.5"
        Object actual = (new TokenQueue("test123")).chompTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_713() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775807"
        Object actual = (new TokenQueue("test123")).chompTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_714() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775808"
        Object actual = (new TokenQueue("test123")).chompTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_715() throws Exception {
        // Combination: receiver__data="test123", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("test123")).chompTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_716() throws Exception {
        // Combination: receiver__data="!@#", seq=""
        Object actual = (new TokenQueue("!@#")).chompTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_717() throws Exception {
        // Combination: receiver__data="!@#", seq=" "
        Object actual = (new TokenQueue("!@#")).chompTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_718() throws Exception {
        // Combination: receiver__data="!@#", seq="a"
        Object actual = (new TokenQueue("!@#")).chompTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_719() throws Exception {
        // Combination: receiver__data="!@#", seq="test123"
        Object actual = (new TokenQueue("!@#")).chompTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_720() throws Exception {
        // Combination: receiver__data="!@#", seq="!@#"
        Object actual = (new TokenQueue("!@#")).chompTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_721() throws Exception {
        // Combination: receiver__data="!@#", seq="0"
        Object actual = (new TokenQueue("!@#")).chompTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_722() throws Exception {
        // Combination: receiver__data="!@#", seq="-1"
        Object actual = (new TokenQueue("!@#")).chompTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_723() throws Exception {
        // Combination: receiver__data="!@#", seq="1.5"
        Object actual = (new TokenQueue("!@#")).chompTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_724() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775807"
        Object actual = (new TokenQueue("!@#")).chompTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_725() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775808"
        Object actual = (new TokenQueue("!@#")).chompTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_726() throws Exception {
        // Combination: receiver__data="!@#", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("!@#")).chompTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_727() throws Exception {
        // Combination: receiver__data="0", seq=""
        Object actual = (new TokenQueue("0")).chompTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_728() throws Exception {
        // Combination: receiver__data="0", seq=" "
        Object actual = (new TokenQueue("0")).chompTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_729() throws Exception {
        // Combination: receiver__data="0", seq="a"
        Object actual = (new TokenQueue("0")).chompTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_730() throws Exception {
        // Combination: receiver__data="0", seq="test123"
        Object actual = (new TokenQueue("0")).chompTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_731() throws Exception {
        // Combination: receiver__data="0", seq="!@#"
        Object actual = (new TokenQueue("0")).chompTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_732() throws Exception {
        // Combination: receiver__data="0", seq="0"
        Object actual = (new TokenQueue("0")).chompTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_733() throws Exception {
        // Combination: receiver__data="0", seq="-1"
        Object actual = (new TokenQueue("0")).chompTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_734() throws Exception {
        // Combination: receiver__data="0", seq="1.5"
        Object actual = (new TokenQueue("0")).chompTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_735() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775807"
        Object actual = (new TokenQueue("0")).chompTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_736() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775808"
        Object actual = (new TokenQueue("0")).chompTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_737() throws Exception {
        // Combination: receiver__data="0", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("0")).chompTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_738() throws Exception {
        // Combination: receiver__data="-1", seq=""
        Object actual = (new TokenQueue("-1")).chompTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_739() throws Exception {
        // Combination: receiver__data="-1", seq=" "
        Object actual = (new TokenQueue("-1")).chompTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_740() throws Exception {
        // Combination: receiver__data="-1", seq="a"
        Object actual = (new TokenQueue("-1")).chompTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_741() throws Exception {
        // Combination: receiver__data="-1", seq="test123"
        Object actual = (new TokenQueue("-1")).chompTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_742() throws Exception {
        // Combination: receiver__data="-1", seq="!@#"
        Object actual = (new TokenQueue("-1")).chompTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_743() throws Exception {
        // Combination: receiver__data="-1", seq="0"
        Object actual = (new TokenQueue("-1")).chompTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_744() throws Exception {
        // Combination: receiver__data="-1", seq="-1"
        Object actual = (new TokenQueue("-1")).chompTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_745() throws Exception {
        // Combination: receiver__data="-1", seq="1.5"
        Object actual = (new TokenQueue("-1")).chompTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_746() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775807"
        Object actual = (new TokenQueue("-1")).chompTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_747() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775808"
        Object actual = (new TokenQueue("-1")).chompTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_748() throws Exception {
        // Combination: receiver__data="-1", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("-1")).chompTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_749() throws Exception {
        // Combination: receiver__data="1.5", seq=""
        Object actual = (new TokenQueue("1.5")).chompTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_750() throws Exception {
        // Combination: receiver__data="1.5", seq=" "
        Object actual = (new TokenQueue("1.5")).chompTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_751() throws Exception {
        // Combination: receiver__data="1.5", seq="a"
        Object actual = (new TokenQueue("1.5")).chompTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_752() throws Exception {
        // Combination: receiver__data="1.5", seq="test123"
        Object actual = (new TokenQueue("1.5")).chompTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_753() throws Exception {
        // Combination: receiver__data="1.5", seq="!@#"
        Object actual = (new TokenQueue("1.5")).chompTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_754() throws Exception {
        // Combination: receiver__data="1.5", seq="0"
        Object actual = (new TokenQueue("1.5")).chompTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_755() throws Exception {
        // Combination: receiver__data="1.5", seq="-1"
        Object actual = (new TokenQueue("1.5")).chompTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_756() throws Exception {
        // Combination: receiver__data="1.5", seq="1.5"
        Object actual = (new TokenQueue("1.5")).chompTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_757() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775807"
        Object actual = (new TokenQueue("1.5")).chompTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_758() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775808"
        Object actual = (new TokenQueue("1.5")).chompTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_759() throws Exception {
        // Combination: receiver__data="1.5", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("1.5")).chompTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_760() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=""
        Object actual = (new TokenQueue("9223372036854775807")).chompTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_761() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=" "
        Object actual = (new TokenQueue("9223372036854775807")).chompTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_762() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="a"
        Object actual = (new TokenQueue("9223372036854775807")).chompTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_763() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="test123"
        Object actual = (new TokenQueue("9223372036854775807")).chompTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_764() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775807")).chompTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_765() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="0"
        Object actual = (new TokenQueue("9223372036854775807")).chompTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_766() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="-1"
        Object actual = (new TokenQueue("9223372036854775807")).chompTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_767() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775807")).chompTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_768() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775807")).chompTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_769() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775807")).chompTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_770() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775807")).chompTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_771() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=""
        Object actual = (new TokenQueue("9223372036854775808")).chompTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_772() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=" "
        Object actual = (new TokenQueue("9223372036854775808")).chompTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_773() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="a"
        Object actual = (new TokenQueue("9223372036854775808")).chompTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_774() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="test123"
        Object actual = (new TokenQueue("9223372036854775808")).chompTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_775() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775808")).chompTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_776() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="0"
        Object actual = (new TokenQueue("9223372036854775808")).chompTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_777() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="-1"
        Object actual = (new TokenQueue("9223372036854775808")).chompTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_778() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775808")).chompTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_779() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775808")).chompTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_780() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775808")).chompTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_781() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775808")).chompTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_782() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=""
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompTo("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_783() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=" "
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompTo(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_784() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="a"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompTo("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_785() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="test123"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompTo("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_786() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="!@#"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompTo("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_787() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="0"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompTo("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_788() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="-1"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompTo("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_789() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="1.5"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompTo("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_790() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775807"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompTo("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_791() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775808"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompTo("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompTo_pairwise_792() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_793() throws Exception {
        // Combination: receiver__data="", seq=""
        try {
            (new TokenQueue("")).chompToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_794() throws Exception {
        // Combination: receiver__data="", seq=" "
        Object actual = (new TokenQueue("")).chompToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_795() throws Exception {
        // Combination: receiver__data="", seq="a"
        Object actual = (new TokenQueue("")).chompToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_796() throws Exception {
        // Combination: receiver__data="", seq="test123"
        Object actual = (new TokenQueue("")).chompToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_797() throws Exception {
        // Combination: receiver__data="", seq="!@#"
        Object actual = (new TokenQueue("")).chompToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_798() throws Exception {
        // Combination: receiver__data="", seq="0"
        Object actual = (new TokenQueue("")).chompToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_799() throws Exception {
        // Combination: receiver__data="", seq="-1"
        Object actual = (new TokenQueue("")).chompToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_800() throws Exception {
        // Combination: receiver__data="", seq="1.5"
        Object actual = (new TokenQueue("")).chompToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_801() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775807"
        Object actual = (new TokenQueue("")).chompToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_802() throws Exception {
        // Combination: receiver__data="", seq="9223372036854775808"
        Object actual = (new TokenQueue("")).chompToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_803() throws Exception {
        // Combination: receiver__data="", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("")).chompToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_804() throws Exception {
        // Combination: receiver__data=" ", seq=""
        try {
            (new TokenQueue(" ")).chompToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_805() throws Exception {
        // Combination: receiver__data=" ", seq=" "
        Object actual = (new TokenQueue(" ")).chompToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_806() throws Exception {
        // Combination: receiver__data=" ", seq="a"
        Object actual = (new TokenQueue(" ")).chompToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_807() throws Exception {
        // Combination: receiver__data=" ", seq="test123"
        Object actual = (new TokenQueue(" ")).chompToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_808() throws Exception {
        // Combination: receiver__data=" ", seq="!@#"
        Object actual = (new TokenQueue(" ")).chompToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_809() throws Exception {
        // Combination: receiver__data=" ", seq="0"
        Object actual = (new TokenQueue(" ")).chompToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_810() throws Exception {
        // Combination: receiver__data=" ", seq="-1"
        Object actual = (new TokenQueue(" ")).chompToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_811() throws Exception {
        // Combination: receiver__data=" ", seq="1.5"
        Object actual = (new TokenQueue(" ")).chompToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_812() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775807"
        Object actual = (new TokenQueue(" ")).chompToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_813() throws Exception {
        // Combination: receiver__data=" ", seq="9223372036854775808"
        Object actual = (new TokenQueue(" ")).chompToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_814() throws Exception {
        // Combination: receiver__data=" ", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue(" ")).chompToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_815() throws Exception {
        // Combination: receiver__data="a", seq=""
        try {
            (new TokenQueue("a")).chompToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_816() throws Exception {
        // Combination: receiver__data="a", seq=" "
        Object actual = (new TokenQueue("a")).chompToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_817() throws Exception {
        // Combination: receiver__data="a", seq="a"
        Object actual = (new TokenQueue("a")).chompToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_818() throws Exception {
        // Combination: receiver__data="a", seq="test123"
        Object actual = (new TokenQueue("a")).chompToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_819() throws Exception {
        // Combination: receiver__data="a", seq="!@#"
        Object actual = (new TokenQueue("a")).chompToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_820() throws Exception {
        // Combination: receiver__data="a", seq="0"
        Object actual = (new TokenQueue("a")).chompToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_821() throws Exception {
        // Combination: receiver__data="a", seq="-1"
        Object actual = (new TokenQueue("a")).chompToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_822() throws Exception {
        // Combination: receiver__data="a", seq="1.5"
        Object actual = (new TokenQueue("a")).chompToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_823() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775807"
        Object actual = (new TokenQueue("a")).chompToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_824() throws Exception {
        // Combination: receiver__data="a", seq="9223372036854775808"
        Object actual = (new TokenQueue("a")).chompToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_825() throws Exception {
        // Combination: receiver__data="a", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("a")).chompToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_826() throws Exception {
        // Combination: receiver__data="test123", seq=""
        try {
            (new TokenQueue("test123")).chompToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_827() throws Exception {
        // Combination: receiver__data="test123", seq=" "
        Object actual = (new TokenQueue("test123")).chompToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_828() throws Exception {
        // Combination: receiver__data="test123", seq="a"
        Object actual = (new TokenQueue("test123")).chompToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_829() throws Exception {
        // Combination: receiver__data="test123", seq="test123"
        Object actual = (new TokenQueue("test123")).chompToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_830() throws Exception {
        // Combination: receiver__data="test123", seq="!@#"
        Object actual = (new TokenQueue("test123")).chompToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_831() throws Exception {
        // Combination: receiver__data="test123", seq="0"
        Object actual = (new TokenQueue("test123")).chompToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_832() throws Exception {
        // Combination: receiver__data="test123", seq="-1"
        Object actual = (new TokenQueue("test123")).chompToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_833() throws Exception {
        // Combination: receiver__data="test123", seq="1.5"
        Object actual = (new TokenQueue("test123")).chompToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_834() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775807"
        Object actual = (new TokenQueue("test123")).chompToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_835() throws Exception {
        // Combination: receiver__data="test123", seq="9223372036854775808"
        Object actual = (new TokenQueue("test123")).chompToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_836() throws Exception {
        // Combination: receiver__data="test123", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("test123")).chompToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_837() throws Exception {
        // Combination: receiver__data="!@#", seq=""
        try {
            (new TokenQueue("!@#")).chompToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_838() throws Exception {
        // Combination: receiver__data="!@#", seq=" "
        Object actual = (new TokenQueue("!@#")).chompToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_839() throws Exception {
        // Combination: receiver__data="!@#", seq="a"
        Object actual = (new TokenQueue("!@#")).chompToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_840() throws Exception {
        // Combination: receiver__data="!@#", seq="test123"
        Object actual = (new TokenQueue("!@#")).chompToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_841() throws Exception {
        // Combination: receiver__data="!@#", seq="!@#"
        Object actual = (new TokenQueue("!@#")).chompToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_842() throws Exception {
        // Combination: receiver__data="!@#", seq="0"
        Object actual = (new TokenQueue("!@#")).chompToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_843() throws Exception {
        // Combination: receiver__data="!@#", seq="-1"
        Object actual = (new TokenQueue("!@#")).chompToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_844() throws Exception {
        // Combination: receiver__data="!@#", seq="1.5"
        Object actual = (new TokenQueue("!@#")).chompToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_845() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775807"
        Object actual = (new TokenQueue("!@#")).chompToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_846() throws Exception {
        // Combination: receiver__data="!@#", seq="9223372036854775808"
        Object actual = (new TokenQueue("!@#")).chompToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_847() throws Exception {
        // Combination: receiver__data="!@#", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("!@#")).chompToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_848() throws Exception {
        // Combination: receiver__data="0", seq=""
        try {
            (new TokenQueue("0")).chompToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_849() throws Exception {
        // Combination: receiver__data="0", seq=" "
        Object actual = (new TokenQueue("0")).chompToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_850() throws Exception {
        // Combination: receiver__data="0", seq="a"
        Object actual = (new TokenQueue("0")).chompToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_851() throws Exception {
        // Combination: receiver__data="0", seq="test123"
        Object actual = (new TokenQueue("0")).chompToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_852() throws Exception {
        // Combination: receiver__data="0", seq="!@#"
        Object actual = (new TokenQueue("0")).chompToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_853() throws Exception {
        // Combination: receiver__data="0", seq="0"
        Object actual = (new TokenQueue("0")).chompToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_854() throws Exception {
        // Combination: receiver__data="0", seq="-1"
        Object actual = (new TokenQueue("0")).chompToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_855() throws Exception {
        // Combination: receiver__data="0", seq="1.5"
        Object actual = (new TokenQueue("0")).chompToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_856() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775807"
        Object actual = (new TokenQueue("0")).chompToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_857() throws Exception {
        // Combination: receiver__data="0", seq="9223372036854775808"
        Object actual = (new TokenQueue("0")).chompToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_858() throws Exception {
        // Combination: receiver__data="0", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("0")).chompToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_859() throws Exception {
        // Combination: receiver__data="-1", seq=""
        try {
            (new TokenQueue("-1")).chompToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_860() throws Exception {
        // Combination: receiver__data="-1", seq=" "
        Object actual = (new TokenQueue("-1")).chompToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_861() throws Exception {
        // Combination: receiver__data="-1", seq="a"
        Object actual = (new TokenQueue("-1")).chompToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_862() throws Exception {
        // Combination: receiver__data="-1", seq="test123"
        Object actual = (new TokenQueue("-1")).chompToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_863() throws Exception {
        // Combination: receiver__data="-1", seq="!@#"
        Object actual = (new TokenQueue("-1")).chompToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_864() throws Exception {
        // Combination: receiver__data="-1", seq="0"
        Object actual = (new TokenQueue("-1")).chompToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_865() throws Exception {
        // Combination: receiver__data="-1", seq="-1"
        Object actual = (new TokenQueue("-1")).chompToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_866() throws Exception {
        // Combination: receiver__data="-1", seq="1.5"
        Object actual = (new TokenQueue("-1")).chompToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_867() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775807"
        Object actual = (new TokenQueue("-1")).chompToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_868() throws Exception {
        // Combination: receiver__data="-1", seq="9223372036854775808"
        Object actual = (new TokenQueue("-1")).chompToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_869() throws Exception {
        // Combination: receiver__data="-1", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("-1")).chompToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_870() throws Exception {
        // Combination: receiver__data="1.5", seq=""
        try {
            (new TokenQueue("1.5")).chompToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_871() throws Exception {
        // Combination: receiver__data="1.5", seq=" "
        Object actual = (new TokenQueue("1.5")).chompToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_872() throws Exception {
        // Combination: receiver__data="1.5", seq="a"
        Object actual = (new TokenQueue("1.5")).chompToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_873() throws Exception {
        // Combination: receiver__data="1.5", seq="test123"
        Object actual = (new TokenQueue("1.5")).chompToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_874() throws Exception {
        // Combination: receiver__data="1.5", seq="!@#"
        Object actual = (new TokenQueue("1.5")).chompToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_875() throws Exception {
        // Combination: receiver__data="1.5", seq="0"
        Object actual = (new TokenQueue("1.5")).chompToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_876() throws Exception {
        // Combination: receiver__data="1.5", seq="-1"
        Object actual = (new TokenQueue("1.5")).chompToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_877() throws Exception {
        // Combination: receiver__data="1.5", seq="1.5"
        Object actual = (new TokenQueue("1.5")).chompToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_878() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775807"
        Object actual = (new TokenQueue("1.5")).chompToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_879() throws Exception {
        // Combination: receiver__data="1.5", seq="9223372036854775808"
        Object actual = (new TokenQueue("1.5")).chompToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_880() throws Exception {
        // Combination: receiver__data="1.5", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("1.5")).chompToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_881() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=""
        try {
            (new TokenQueue("9223372036854775807")).chompToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_882() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq=" "
        Object actual = (new TokenQueue("9223372036854775807")).chompToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_883() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="a"
        Object actual = (new TokenQueue("9223372036854775807")).chompToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_884() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="test123"
        Object actual = (new TokenQueue("9223372036854775807")).chompToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_885() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775807")).chompToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_886() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="0"
        Object actual = (new TokenQueue("9223372036854775807")).chompToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_887() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="-1"
        Object actual = (new TokenQueue("9223372036854775807")).chompToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_888() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775807")).chompToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_889() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775807")).chompToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_890() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775807")).chompToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_891() throws Exception {
        // Combination: receiver__data="9223372036854775807", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775807")).chompToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_892() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=""
        try {
            (new TokenQueue("9223372036854775808")).chompToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_893() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq=" "
        Object actual = (new TokenQueue("9223372036854775808")).chompToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_894() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="a"
        Object actual = (new TokenQueue("9223372036854775808")).chompToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_895() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="test123"
        Object actual = (new TokenQueue("9223372036854775808")).chompToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_896() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="!@#"
        Object actual = (new TokenQueue("9223372036854775808")).chompToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_897() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="0"
        Object actual = (new TokenQueue("9223372036854775808")).chompToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_898() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="-1"
        Object actual = (new TokenQueue("9223372036854775808")).chompToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_899() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="1.5"
        Object actual = (new TokenQueue("9223372036854775808")).chompToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_900() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775807"
        Object actual = (new TokenQueue("9223372036854775808")).chompToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_901() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="9223372036854775808"
        Object actual = (new TokenQueue("9223372036854775808")).chompToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_902() throws Exception {
        // Combination: receiver__data="9223372036854775808", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("9223372036854775808")).chompToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_903() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=""
        try {
            (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompToIgnoreCase("");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_904() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq=" "
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompToIgnoreCase(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_905() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="a"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompToIgnoreCase("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_906() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="test123"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompToIgnoreCase("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_907() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="!@#"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompToIgnoreCase("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_908() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="0"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompToIgnoreCase("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_909() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="-1"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompToIgnoreCase("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_910() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="1.5"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompToIgnoreCase("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_911() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775807"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompToIgnoreCase("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_912() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="9223372036854775808"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompToIgnoreCase("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompToIgnoreCase_pairwise_913() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seq="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompToIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_914() throws Exception {
        // Combination: receiver__data="", open='\0', close='\0'
        Object actual = (new TokenQueue("")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_915() throws Exception {
        // Combination: receiver__data=" ", open='a', close='\0'
        Object actual = (new TokenQueue(" ")).chompBalanced('a', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_916() throws Exception {
        // Combination: receiver__data="a", open='0', close='\0'
        Object actual = (new TokenQueue("a")).chompBalanced('0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_917() throws Exception {
        // Combination: receiver__data="test123", open=Character.MIN_VALUE, close='\0'
        Object actual = (new TokenQueue("test123")).chompBalanced(Character.MIN_VALUE, '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_918() throws Exception {
        // Combination: receiver__data="!@#", open=Character.MAX_VALUE, close='\0'
        Object actual = (new TokenQueue("!@#")).chompBalanced(Character.MAX_VALUE, '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_919() throws Exception {
        // Combination: receiver__data=" ", open='\0', close='a'
        Object actual = (new TokenQueue(" ")).chompBalanced('\0', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_920() throws Exception {
        // Combination: receiver__data="", open='a', close='a'
        Object actual = (new TokenQueue("")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_921() throws Exception {
        // Combination: receiver__data="test123", open='0', close='a'
        Object actual = (new TokenQueue("test123")).chompBalanced('0', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_922() throws Exception {
        // Combination: receiver__data="a", open=Character.MIN_VALUE, close='a'
        Object actual = (new TokenQueue("a")).chompBalanced(Character.MIN_VALUE, 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_923() throws Exception {
        // Combination: receiver__data="0", open=Character.MAX_VALUE, close='a'
        Object actual = (new TokenQueue("0")).chompBalanced(Character.MAX_VALUE, 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_924() throws Exception {
        // Combination: receiver__data="a", open='\0', close='0'
        Object actual = (new TokenQueue("a")).chompBalanced('\0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_925() throws Exception {
        // Combination: receiver__data="test123", open='a', close='0'
        Object actual = (new TokenQueue("test123")).chompBalanced('a', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_926() throws Exception {
        // Combination: receiver__data="", open='0', close='0'
        Object actual = (new TokenQueue("")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_927() throws Exception {
        // Combination: receiver__data=" ", open=Character.MIN_VALUE, close='0'
        Object actual = (new TokenQueue(" ")).chompBalanced(Character.MIN_VALUE, '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_928() throws Exception {
        // Combination: receiver__data="-1", open=Character.MAX_VALUE, close='0'
        Object actual = (new TokenQueue("-1")).chompBalanced(Character.MAX_VALUE, '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_929() throws Exception {
        // Combination: receiver__data="test123", open='\0', close=Character.MIN_VALUE
        Object actual = (new TokenQueue("test123")).chompBalanced('\0', Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_930() throws Exception {
        // Combination: receiver__data="a", open='a', close=Character.MIN_VALUE
        try {
            (new TokenQueue("a")).chompBalanced('a', Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_931() throws Exception {
        // Combination: receiver__data=" ", open='0', close=Character.MIN_VALUE
        Object actual = (new TokenQueue(" ")).chompBalanced('0', Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_932() throws Exception {
        // Combination: receiver__data="", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_933() throws Exception {
        // Combination: receiver__data="1.5", open=Character.MAX_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("1.5")).chompBalanced(Character.MAX_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_934() throws Exception {
        // Combination: receiver__data="!@#", open='\0', close=Character.MAX_VALUE
        Object actual = (new TokenQueue("!@#")).chompBalanced('\0', Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_935() throws Exception {
        // Combination: receiver__data="0", open='a', close=Character.MAX_VALUE
        Object actual = (new TokenQueue("0")).chompBalanced('a', Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_936() throws Exception {
        // Combination: receiver__data="-1", open='0', close=Character.MAX_VALUE
        Object actual = (new TokenQueue("-1")).chompBalanced('0', Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_937() throws Exception {
        // Combination: receiver__data="1.5", open=Character.MIN_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("1.5")).chompBalanced(Character.MIN_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_938() throws Exception {
        // Combination: receiver__data="", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_939() throws Exception {
        // Combination: receiver__data=" ", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue(" ")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_940() throws Exception {
        // Combination: receiver__data="a", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("a")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_941() throws Exception {
        // Combination: receiver__data="test123", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("test123")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_942() throws Exception {
        // Combination: receiver__data="!@#", open='a', close='a'
        Object actual = (new TokenQueue("!@#")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_943() throws Exception {
        // Combination: receiver__data="!@#", open='0', close='0'
        Object actual = (new TokenQueue("!@#")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_944() throws Exception {
        // Combination: receiver__data="!@#", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("!@#")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_945() throws Exception {
        // Combination: receiver__data="0", open='\0', close='\0'
        Object actual = (new TokenQueue("0")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_946() throws Exception {
        // Combination: receiver__data="0", open='0', close='0'
        try {
            (new TokenQueue("0")).chompBalanced('0', '0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_947() throws Exception {
        // Combination: receiver__data="0", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("0")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_948() throws Exception {
        // Combination: receiver__data="-1", open='\0', close='\0'
        Object actual = (new TokenQueue("-1")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_949() throws Exception {
        // Combination: receiver__data="-1", open='a', close='a'
        Object actual = (new TokenQueue("-1")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_950() throws Exception {
        // Combination: receiver__data="-1", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("-1")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_951() throws Exception {
        // Combination: receiver__data="1.5", open='\0', close='\0'
        Object actual = (new TokenQueue("1.5")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_952() throws Exception {
        // Combination: receiver__data="1.5", open='a', close='a'
        Object actual = (new TokenQueue("1.5")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_953() throws Exception {
        // Combination: receiver__data="1.5", open='0', close='0'
        Object actual = (new TokenQueue("1.5")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_954() throws Exception {
        // Combination: receiver__data="9223372036854775807", open='\0', close='\0'
        Object actual = (new TokenQueue("9223372036854775807")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_955() throws Exception {
        // Combination: receiver__data="9223372036854775807", open='a', close='a'
        Object actual = (new TokenQueue("9223372036854775807")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_956() throws Exception {
        // Combination: receiver__data="9223372036854775807", open='0', close='0'
        Object actual = (new TokenQueue("9223372036854775807")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_957() throws Exception {
        // Combination: receiver__data="9223372036854775807", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("9223372036854775807")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_958() throws Exception {
        // Combination: receiver__data="9223372036854775807", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("9223372036854775807")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_959() throws Exception {
        // Combination: receiver__data="9223372036854775808", open='\0', close='\0'
        Object actual = (new TokenQueue("9223372036854775808")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_960() throws Exception {
        // Combination: receiver__data="9223372036854775808", open='a', close='a'
        Object actual = (new TokenQueue("9223372036854775808")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_961() throws Exception {
        // Combination: receiver__data="9223372036854775808", open='0', close='0'
        Object actual = (new TokenQueue("9223372036854775808")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_962() throws Exception {
        // Combination: receiver__data="9223372036854775808", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("9223372036854775808")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_963() throws Exception {
        // Combination: receiver__data="9223372036854775808", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("9223372036854775808")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_964() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", open='\0', close='\0'
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_965() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", open='a', close='a'
        try {
            (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompBalanced('a', 'a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_966() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", open='0', close='0'
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_967() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_968() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

}
