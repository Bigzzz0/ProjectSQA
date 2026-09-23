package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ParseSettings.
 */
public class ParseSettings_IPOTest {
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
    public void test_preserveTagCase_pairwise_001() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true
        Object actual = (new ParseSettings(true, true)).preserveTagCase();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_preserveTagCase_pairwise_002() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=true
        Object actual = (new ParseSettings(false, true)).preserveTagCase();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_preserveTagCase_pairwise_003() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=false
        Object actual = (new ParseSettings(true, false)).preserveTagCase();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_preserveTagCase_pairwise_004() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false
        Object actual = (new ParseSettings(false, false)).preserveTagCase();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_preserveAttributeCase_pairwise_005() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true
        Object actual = (new ParseSettings(true, true)).preserveAttributeCase();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_preserveAttributeCase_pairwise_006() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=true
        Object actual = (new ParseSettings(false, true)).preserveAttributeCase();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_preserveAttributeCase_pairwise_007() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=false
        Object actual = (new ParseSettings(true, false)).preserveAttributeCase();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_preserveAttributeCase_pairwise_008() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false
        Object actual = (new ParseSettings(false, false)).preserveAttributeCase();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_009() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name=""
        Object actual = (new ParseSettings(true, true)).normalizeTag("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_010() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name=""
        Object actual = (new ParseSettings(false, false)).normalizeTag("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_011() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=true, name=" "
        Object actual = (new ParseSettings(false, true)).normalizeTag(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_012() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=false, name=" "
        Object actual = (new ParseSettings(true, false)).normalizeTag(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_013() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="a"
        Object actual = (new ParseSettings(true, true)).normalizeTag("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_014() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="a"
        Object actual = (new ParseSettings(false, false)).normalizeTag("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_015() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="test123"
        Object actual = (new ParseSettings(true, true)).normalizeTag("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_016() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="test123"
        Object actual = (new ParseSettings(false, false)).normalizeTag("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_017() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="!@#"
        Object actual = (new ParseSettings(true, true)).normalizeTag("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_018() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="!@#"
        Object actual = (new ParseSettings(false, false)).normalizeTag("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_019() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="0"
        Object actual = (new ParseSettings(true, true)).normalizeTag("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_020() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="0"
        Object actual = (new ParseSettings(false, false)).normalizeTag("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_021() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="-1"
        Object actual = (new ParseSettings(true, true)).normalizeTag("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_022() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="-1"
        Object actual = (new ParseSettings(false, false)).normalizeTag("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_023() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="1.5"
        Object actual = (new ParseSettings(true, true)).normalizeTag("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_024() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="1.5"
        Object actual = (new ParseSettings(false, false)).normalizeTag("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_025() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="9223372036854775807"
        Object actual = (new ParseSettings(true, true)).normalizeTag("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_026() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="9223372036854775807"
        Object actual = (new ParseSettings(false, false)).normalizeTag("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_027() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="9223372036854775808"
        Object actual = (new ParseSettings(true, true)).normalizeTag("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_028() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="9223372036854775808"
        Object actual = (new ParseSettings(false, false)).normalizeTag("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_029() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ParseSettings(true, true)).normalizeTag("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeTag_pairwise_030() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ParseSettings(false, false)).normalizeTag("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_031() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name=""
        Object actual = (new ParseSettings(true, true)).normalizeAttribute("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_032() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name=""
        Object actual = (new ParseSettings(false, false)).normalizeAttribute("");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_033() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=true, name=" "
        Object actual = (new ParseSettings(false, true)).normalizeAttribute(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_034() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=false, name=" "
        Object actual = (new ParseSettings(true, false)).normalizeAttribute(" ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_035() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="a"
        Object actual = (new ParseSettings(true, true)).normalizeAttribute("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_036() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="a"
        Object actual = (new ParseSettings(false, false)).normalizeAttribute("a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_037() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="test123"
        Object actual = (new ParseSettings(true, true)).normalizeAttribute("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_038() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="test123"
        Object actual = (new ParseSettings(false, false)).normalizeAttribute("test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_039() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="!@#"
        Object actual = (new ParseSettings(true, true)).normalizeAttribute("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_040() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="!@#"
        Object actual = (new ParseSettings(false, false)).normalizeAttribute("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_041() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="0"
        Object actual = (new ParseSettings(true, true)).normalizeAttribute("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_042() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="0"
        Object actual = (new ParseSettings(false, false)).normalizeAttribute("0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_043() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="-1"
        Object actual = (new ParseSettings(true, true)).normalizeAttribute("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_044() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="-1"
        Object actual = (new ParseSettings(false, false)).normalizeAttribute("-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_045() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="1.5"
        Object actual = (new ParseSettings(true, true)).normalizeAttribute("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_046() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="1.5"
        Object actual = (new ParseSettings(false, false)).normalizeAttribute("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_047() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="9223372036854775807"
        Object actual = (new ParseSettings(true, true)).normalizeAttribute("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_048() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="9223372036854775807"
        Object actual = (new ParseSettings(false, false)).normalizeAttribute("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_049() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="9223372036854775808"
        Object actual = (new ParseSettings(true, true)).normalizeAttribute("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_050() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="9223372036854775808"
        Object actual = (new ParseSettings(false, false)).normalizeAttribute("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_051() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true, name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ParseSettings(true, true)).normalizeAttribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAttribute_pairwise_052() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false, name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ParseSettings(false, false)).normalizeAttribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

}
