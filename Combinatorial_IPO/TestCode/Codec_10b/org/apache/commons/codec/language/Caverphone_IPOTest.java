package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Caverphone.
 */
public class Caverphone_IPOTest {
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
    public void test_isCaverphoneEqual_pairwise_001() throws Exception {
        // Combination: str1="", str2=""
        Object actual = (new Caverphone()).isCaverphoneEqual("", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_002() throws Exception {
        // Combination: str1="", str2=" "
        Object actual = (new Caverphone()).isCaverphoneEqual("", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_003() throws Exception {
        // Combination: str1="", str2="a"
        Object actual = (new Caverphone()).isCaverphoneEqual("", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_004() throws Exception {
        // Combination: str1="", str2="test123"
        Object actual = (new Caverphone()).isCaverphoneEqual("", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_005() throws Exception {
        // Combination: str1="", str2="!@#"
        Object actual = (new Caverphone()).isCaverphoneEqual("", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_006() throws Exception {
        // Combination: str1="", str2="0"
        Object actual = (new Caverphone()).isCaverphoneEqual("", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_007() throws Exception {
        // Combination: str1="", str2="-1"
        Object actual = (new Caverphone()).isCaverphoneEqual("", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_008() throws Exception {
        // Combination: str1="", str2="1.5"
        Object actual = (new Caverphone()).isCaverphoneEqual("", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_009() throws Exception {
        // Combination: str1="", str2="9223372036854775807"
        Object actual = (new Caverphone()).isCaverphoneEqual("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_010() throws Exception {
        // Combination: str1="", str2="9223372036854775808"
        Object actual = (new Caverphone()).isCaverphoneEqual("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_011() throws Exception {
        // Combination: str1="", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Caverphone()).isCaverphoneEqual("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_012() throws Exception {
        // Combination: str1=" ", str2=""
        Object actual = (new Caverphone()).isCaverphoneEqual(" ", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_013() throws Exception {
        // Combination: str1=" ", str2=" "
        Object actual = (new Caverphone()).isCaverphoneEqual(" ", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_014() throws Exception {
        // Combination: str1=" ", str2="a"
        Object actual = (new Caverphone()).isCaverphoneEqual(" ", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_015() throws Exception {
        // Combination: str1=" ", str2="test123"
        Object actual = (new Caverphone()).isCaverphoneEqual(" ", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_016() throws Exception {
        // Combination: str1=" ", str2="!@#"
        Object actual = (new Caverphone()).isCaverphoneEqual(" ", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_017() throws Exception {
        // Combination: str1=" ", str2="0"
        Object actual = (new Caverphone()).isCaverphoneEqual(" ", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_018() throws Exception {
        // Combination: str1=" ", str2="-1"
        Object actual = (new Caverphone()).isCaverphoneEqual(" ", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_019() throws Exception {
        // Combination: str1=" ", str2="1.5"
        Object actual = (new Caverphone()).isCaverphoneEqual(" ", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_020() throws Exception {
        // Combination: str1=" ", str2="9223372036854775807"
        Object actual = (new Caverphone()).isCaverphoneEqual(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_021() throws Exception {
        // Combination: str1=" ", str2="9223372036854775808"
        Object actual = (new Caverphone()).isCaverphoneEqual(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_022() throws Exception {
        // Combination: str1=" ", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Caverphone()).isCaverphoneEqual(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_023() throws Exception {
        // Combination: str1="a", str2=""
        Object actual = (new Caverphone()).isCaverphoneEqual("a", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_024() throws Exception {
        // Combination: str1="a", str2=" "
        Object actual = (new Caverphone()).isCaverphoneEqual("a", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_025() throws Exception {
        // Combination: str1="a", str2="a"
        Object actual = (new Caverphone()).isCaverphoneEqual("a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_026() throws Exception {
        // Combination: str1="a", str2="test123"
        Object actual = (new Caverphone()).isCaverphoneEqual("a", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_027() throws Exception {
        // Combination: str1="a", str2="!@#"
        Object actual = (new Caverphone()).isCaverphoneEqual("a", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_028() throws Exception {
        // Combination: str1="a", str2="0"
        Object actual = (new Caverphone()).isCaverphoneEqual("a", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_029() throws Exception {
        // Combination: str1="a", str2="-1"
        Object actual = (new Caverphone()).isCaverphoneEqual("a", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_030() throws Exception {
        // Combination: str1="a", str2="1.5"
        Object actual = (new Caverphone()).isCaverphoneEqual("a", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_031() throws Exception {
        // Combination: str1="a", str2="9223372036854775807"
        Object actual = (new Caverphone()).isCaverphoneEqual("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_032() throws Exception {
        // Combination: str1="a", str2="9223372036854775808"
        Object actual = (new Caverphone()).isCaverphoneEqual("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_033() throws Exception {
        // Combination: str1="a", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Caverphone()).isCaverphoneEqual("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_034() throws Exception {
        // Combination: str1="test123", str2=""
        Object actual = (new Caverphone()).isCaverphoneEqual("test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_035() throws Exception {
        // Combination: str1="test123", str2=" "
        Object actual = (new Caverphone()).isCaverphoneEqual("test123", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_036() throws Exception {
        // Combination: str1="test123", str2="a"
        Object actual = (new Caverphone()).isCaverphoneEqual("test123", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_037() throws Exception {
        // Combination: str1="test123", str2="test123"
        Object actual = (new Caverphone()).isCaverphoneEqual("test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_038() throws Exception {
        // Combination: str1="test123", str2="!@#"
        Object actual = (new Caverphone()).isCaverphoneEqual("test123", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_039() throws Exception {
        // Combination: str1="test123", str2="0"
        Object actual = (new Caverphone()).isCaverphoneEqual("test123", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_040() throws Exception {
        // Combination: str1="test123", str2="-1"
        Object actual = (new Caverphone()).isCaverphoneEqual("test123", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_041() throws Exception {
        // Combination: str1="test123", str2="1.5"
        Object actual = (new Caverphone()).isCaverphoneEqual("test123", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_042() throws Exception {
        // Combination: str1="test123", str2="9223372036854775807"
        Object actual = (new Caverphone()).isCaverphoneEqual("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_043() throws Exception {
        // Combination: str1="test123", str2="9223372036854775808"
        Object actual = (new Caverphone()).isCaverphoneEqual("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_044() throws Exception {
        // Combination: str1="test123", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Caverphone()).isCaverphoneEqual("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_045() throws Exception {
        // Combination: str1="!@#", str2=""
        Object actual = (new Caverphone()).isCaverphoneEqual("!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_046() throws Exception {
        // Combination: str1="!@#", str2=" "
        Object actual = (new Caverphone()).isCaverphoneEqual("!@#", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_047() throws Exception {
        // Combination: str1="!@#", str2="a"
        Object actual = (new Caverphone()).isCaverphoneEqual("!@#", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_048() throws Exception {
        // Combination: str1="!@#", str2="test123"
        Object actual = (new Caverphone()).isCaverphoneEqual("!@#", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_049() throws Exception {
        // Combination: str1="!@#", str2="!@#"
        Object actual = (new Caverphone()).isCaverphoneEqual("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_050() throws Exception {
        // Combination: str1="!@#", str2="0"
        Object actual = (new Caverphone()).isCaverphoneEqual("!@#", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_051() throws Exception {
        // Combination: str1="!@#", str2="-1"
        Object actual = (new Caverphone()).isCaverphoneEqual("!@#", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_052() throws Exception {
        // Combination: str1="!@#", str2="1.5"
        Object actual = (new Caverphone()).isCaverphoneEqual("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_053() throws Exception {
        // Combination: str1="!@#", str2="9223372036854775807"
        Object actual = (new Caverphone()).isCaverphoneEqual("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_054() throws Exception {
        // Combination: str1="!@#", str2="9223372036854775808"
        Object actual = (new Caverphone()).isCaverphoneEqual("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_055() throws Exception {
        // Combination: str1="!@#", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Caverphone()).isCaverphoneEqual("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_056() throws Exception {
        // Combination: str1="0", str2=""
        Object actual = (new Caverphone()).isCaverphoneEqual("0", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_057() throws Exception {
        // Combination: str1="0", str2=" "
        Object actual = (new Caverphone()).isCaverphoneEqual("0", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_058() throws Exception {
        // Combination: str1="0", str2="a"
        Object actual = (new Caverphone()).isCaverphoneEqual("0", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_059() throws Exception {
        // Combination: str1="0", str2="test123"
        Object actual = (new Caverphone()).isCaverphoneEqual("0", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_060() throws Exception {
        // Combination: str1="0", str2="!@#"
        Object actual = (new Caverphone()).isCaverphoneEqual("0", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_061() throws Exception {
        // Combination: str1="0", str2="0"
        Object actual = (new Caverphone()).isCaverphoneEqual("0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_062() throws Exception {
        // Combination: str1="0", str2="-1"
        Object actual = (new Caverphone()).isCaverphoneEqual("0", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_063() throws Exception {
        // Combination: str1="0", str2="1.5"
        Object actual = (new Caverphone()).isCaverphoneEqual("0", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_064() throws Exception {
        // Combination: str1="0", str2="9223372036854775807"
        Object actual = (new Caverphone()).isCaverphoneEqual("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_065() throws Exception {
        // Combination: str1="0", str2="9223372036854775808"
        Object actual = (new Caverphone()).isCaverphoneEqual("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_066() throws Exception {
        // Combination: str1="0", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Caverphone()).isCaverphoneEqual("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_067() throws Exception {
        // Combination: str1="-1", str2=""
        Object actual = (new Caverphone()).isCaverphoneEqual("-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_068() throws Exception {
        // Combination: str1="-1", str2=" "
        Object actual = (new Caverphone()).isCaverphoneEqual("-1", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_069() throws Exception {
        // Combination: str1="-1", str2="a"
        Object actual = (new Caverphone()).isCaverphoneEqual("-1", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_070() throws Exception {
        // Combination: str1="-1", str2="test123"
        Object actual = (new Caverphone()).isCaverphoneEqual("-1", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_071() throws Exception {
        // Combination: str1="-1", str2="!@#"
        Object actual = (new Caverphone()).isCaverphoneEqual("-1", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_072() throws Exception {
        // Combination: str1="-1", str2="0"
        Object actual = (new Caverphone()).isCaverphoneEqual("-1", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_073() throws Exception {
        // Combination: str1="-1", str2="-1"
        Object actual = (new Caverphone()).isCaverphoneEqual("-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_074() throws Exception {
        // Combination: str1="-1", str2="1.5"
        Object actual = (new Caverphone()).isCaverphoneEqual("-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_075() throws Exception {
        // Combination: str1="-1", str2="9223372036854775807"
        Object actual = (new Caverphone()).isCaverphoneEqual("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_076() throws Exception {
        // Combination: str1="-1", str2="9223372036854775808"
        Object actual = (new Caverphone()).isCaverphoneEqual("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_077() throws Exception {
        // Combination: str1="-1", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Caverphone()).isCaverphoneEqual("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_078() throws Exception {
        // Combination: str1="1.5", str2=""
        Object actual = (new Caverphone()).isCaverphoneEqual("1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_079() throws Exception {
        // Combination: str1="1.5", str2=" "
        Object actual = (new Caverphone()).isCaverphoneEqual("1.5", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_080() throws Exception {
        // Combination: str1="1.5", str2="a"
        Object actual = (new Caverphone()).isCaverphoneEqual("1.5", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_081() throws Exception {
        // Combination: str1="1.5", str2="test123"
        Object actual = (new Caverphone()).isCaverphoneEqual("1.5", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_082() throws Exception {
        // Combination: str1="1.5", str2="!@#"
        Object actual = (new Caverphone()).isCaverphoneEqual("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_083() throws Exception {
        // Combination: str1="1.5", str2="0"
        Object actual = (new Caverphone()).isCaverphoneEqual("1.5", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_084() throws Exception {
        // Combination: str1="1.5", str2="-1"
        Object actual = (new Caverphone()).isCaverphoneEqual("1.5", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_085() throws Exception {
        // Combination: str1="1.5", str2="1.5"
        Object actual = (new Caverphone()).isCaverphoneEqual("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_086() throws Exception {
        // Combination: str1="1.5", str2="9223372036854775807"
        Object actual = (new Caverphone()).isCaverphoneEqual("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_087() throws Exception {
        // Combination: str1="1.5", str2="9223372036854775808"
        Object actual = (new Caverphone()).isCaverphoneEqual("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_088() throws Exception {
        // Combination: str1="1.5", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Caverphone()).isCaverphoneEqual("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_089() throws Exception {
        // Combination: str1="9223372036854775807", str2=""
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_090() throws Exception {
        // Combination: str1="9223372036854775807", str2=" "
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_091() throws Exception {
        // Combination: str1="9223372036854775807", str2="a"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_092() throws Exception {
        // Combination: str1="9223372036854775807", str2="test123"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_093() throws Exception {
        // Combination: str1="9223372036854775807", str2="!@#"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_094() throws Exception {
        // Combination: str1="9223372036854775807", str2="0"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_095() throws Exception {
        // Combination: str1="9223372036854775807", str2="-1"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_096() throws Exception {
        // Combination: str1="9223372036854775807", str2="1.5"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_097() throws Exception {
        // Combination: str1="9223372036854775807", str2="9223372036854775807"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_098() throws Exception {
        // Combination: str1="9223372036854775807", str2="9223372036854775808"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_099() throws Exception {
        // Combination: str1="9223372036854775807", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_100() throws Exception {
        // Combination: str1="9223372036854775808", str2=""
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_101() throws Exception {
        // Combination: str1="9223372036854775808", str2=" "
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_102() throws Exception {
        // Combination: str1="9223372036854775808", str2="a"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_103() throws Exception {
        // Combination: str1="9223372036854775808", str2="test123"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_104() throws Exception {
        // Combination: str1="9223372036854775808", str2="!@#"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_105() throws Exception {
        // Combination: str1="9223372036854775808", str2="0"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_106() throws Exception {
        // Combination: str1="9223372036854775808", str2="-1"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_107() throws Exception {
        // Combination: str1="9223372036854775808", str2="1.5"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_108() throws Exception {
        // Combination: str1="9223372036854775808", str2="9223372036854775807"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_109() throws Exception {
        // Combination: str1="9223372036854775808", str2="9223372036854775808"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_110() throws Exception {
        // Combination: str1="9223372036854775808", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Caverphone()).isCaverphoneEqual("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_111() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2=""
        Object actual = (new Caverphone()).isCaverphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_112() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2=" "
        Object actual = (new Caverphone()).isCaverphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_113() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="a"
        Object actual = (new Caverphone()).isCaverphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_114() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="test123"
        Object actual = (new Caverphone()).isCaverphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_115() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="!@#"
        Object actual = (new Caverphone()).isCaverphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_116() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="0"
        Object actual = (new Caverphone()).isCaverphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_117() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="-1"
        Object actual = (new Caverphone()).isCaverphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_118() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="1.5"
        Object actual = (new Caverphone()).isCaverphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_119() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="9223372036854775807"
        Object actual = (new Caverphone()).isCaverphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_120() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="9223372036854775808"
        Object actual = (new Caverphone()).isCaverphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCaverphoneEqual_pairwise_121() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Caverphone()).isCaverphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

}
