package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for XmlDeclaration.
 */
public class XmlDeclaration_IPOTest {
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
    public void test_nodeName_pairwise_001() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_002() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration(" ", "", false)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_003() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", " ", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_004() throws Exception {
        // Combination: receiver__name="", receiver__baseUri=" ", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("", " ", false)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_005() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "a", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_006() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="a", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("test123", "a", false)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_007() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "test123", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_008() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="test123", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("a", "test123", false)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_009() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "!@#", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_010() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="!@#", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("0", "!@#", false)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_011() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "0", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_012() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="0", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("!@#", "0", false)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_013() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "-1", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_014() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="-1", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("1.5", "-1", false)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_015() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "1.5", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_016() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="1.5", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("-1", "1.5", false)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_017() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "9223372036854775807", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_018() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("9223372036854775808", "9223372036854775807", false)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_019() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "9223372036854775808", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_020() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("9223372036854775807", "9223372036854775808", false)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_021() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_022() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_023() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "a", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_024() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "test123", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_025() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "!@#", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_026() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "0", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_027() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "-1", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_028() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "1.5", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_029() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "9223372036854775807", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_030() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "9223372036854775808", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_031() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "a", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_032() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "test123", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_033() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "!@#", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_034() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "0", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_035() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "-1", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_036() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "1.5", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_037() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "9223372036854775807", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_038() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "9223372036854775808", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_039() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_040() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_041() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", " ", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_042() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "!@#", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_043() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "0", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_044() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "-1", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_045() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "1.5", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_046() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "9223372036854775807", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_047() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "9223372036854775808", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_048() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_049() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_050() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", " ", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_051() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "!@#", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_052() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "0", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_053() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "-1", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_054() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "1.5", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_055() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "9223372036854775807", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_056() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "9223372036854775808", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_057() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_058() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_059() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", " ", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_060() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "a", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_061() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "test123", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_062() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "-1", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_063() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "1.5", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_064() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "9223372036854775807", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_065() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "9223372036854775808", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_066() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_067() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_068() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", " ", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_069() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "a", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_070() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "test123", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_071() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "-1", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_072() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "1.5", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_073() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "9223372036854775807", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_074() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "9223372036854775808", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_075() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_076() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_077() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", " ", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_078() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "a", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_079() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "test123", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_080() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "!@#", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_081() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "0", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_082() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "9223372036854775807", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_083() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "9223372036854775808", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_084() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_085() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_086() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", " ", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_087() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "a", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_088() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "test123", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_089() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "!@#", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_090() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "0", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_091() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "9223372036854775807", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_092() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "9223372036854775808", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_093() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_094() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_095() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", " ", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_096() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "a", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_097() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "test123", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_098() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "!@#", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_099() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "0", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_100() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "-1", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_101() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "1.5", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_102() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_103() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_104() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", " ", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_105() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "a", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_106() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "test123", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_107() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "!@#", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_108() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "0", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_109() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "-1", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_110() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "1.5", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_111() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_112() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", false)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_113() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_114() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_115() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_116() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_117() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_118() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_119() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_120() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_121() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", true)).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#declaration", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_122() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_123() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration(" ", "", false)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_124() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", " ", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_125() throws Exception {
        // Combination: receiver__name="", receiver__baseUri=" ", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("", " ", false)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_126() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "a", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_127() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="a", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("test123", "a", false)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_128() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "test123", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_129() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="test123", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("a", "test123", false)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_130() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "!@#", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_131() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="!@#", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("0", "!@#", false)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_132() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "0", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_133() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="0", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("!@#", "0", false)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_134() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "-1", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_135() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="-1", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("1.5", "-1", false)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_136() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "1.5", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_137() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="1.5", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("-1", "1.5", false)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_138() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "9223372036854775807", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_139() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("9223372036854775808", "9223372036854775807", false)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_140() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "9223372036854775808", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_141() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("9223372036854775807", "9223372036854775808", false)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_142() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_143() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_144() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "a", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_145() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "test123", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_146() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "!@#", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_147() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "0", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_148() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "-1", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_149() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "1.5", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_150() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "9223372036854775807", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_151() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "9223372036854775808", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_152() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "a", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_153() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "test123", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_154() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "!@#", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_155() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "0", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_156() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "-1", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_157() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "1.5", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_158() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "9223372036854775807", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_159() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "9223372036854775808", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_160() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_161() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_162() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", " ", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_163() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "!@#", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_164() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "0", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_165() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "-1", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_166() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "1.5", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_167() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "9223372036854775807", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_168() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "9223372036854775808", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_169() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_170() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_171() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", " ", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_172() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "!@#", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_173() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "0", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_174() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "-1", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_175() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "1.5", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_176() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "9223372036854775807", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_177() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "9223372036854775808", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_178() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_179() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_180() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", " ", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_181() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "a", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_182() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "test123", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_183() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "-1", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_184() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "1.5", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_185() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "9223372036854775807", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_186() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "9223372036854775808", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_187() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_188() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_189() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", " ", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_190() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "a", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_191() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "test123", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_192() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "-1", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_193() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "1.5", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_194() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "9223372036854775807", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_195() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "9223372036854775808", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_196() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_197() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_198() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", " ", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_199() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "a", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_200() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "test123", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_201() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "!@#", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_202() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "0", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_203() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "9223372036854775807", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_204() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "9223372036854775808", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_205() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_206() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_207() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", " ", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_208() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "a", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_209() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "test123", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_210() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "!@#", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_211() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "0", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_212() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "9223372036854775807", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_213() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "9223372036854775808", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_214() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_215() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_216() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", " ", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_217() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "a", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_218() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "test123", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_219() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "!@#", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_220() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "0", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_221() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "-1", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_222() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "1.5", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_223() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_224() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_225() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", " ", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_226() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "a", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_227() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "test123", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_228() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "!@#", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_229() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "0", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_230() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "-1", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_231() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "1.5", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_232() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_233() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", false)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_234() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_235() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_236() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_237() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_238() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_239() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_240() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_241() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_name_pairwise_242() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", true)).name();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_243() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_244() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration(" ", "", false)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_245() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", " ", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_246() throws Exception {
        // Combination: receiver__name="", receiver__baseUri=" ", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("", " ", false)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_247() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "a", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_248() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="a", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("test123", "a", false)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_249() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "test123", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_250() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="test123", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("a", "test123", false)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_251() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "!@#", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_252() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="!@#", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("0", "!@#", false)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_253() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "0", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_254() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="0", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("!@#", "0", false)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_255() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "-1", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_256() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="-1", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("1.5", "-1", false)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_257() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "1.5", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_258() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="1.5", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("-1", "1.5", false)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_259() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "9223372036854775807", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_260() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("9223372036854775808", "9223372036854775807", false)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_261() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "9223372036854775808", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_262() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("9223372036854775807", "9223372036854775808", false)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_263() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_264() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_265() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "a", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_266() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "test123", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_267() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "!@#", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_268() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "0", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_269() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "-1", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_270() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "1.5", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_271() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "9223372036854775807", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_272() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "9223372036854775808", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_273() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "a", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_274() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "test123", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_275() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "!@#", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_276() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "0", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_277() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "-1", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_278() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "1.5", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_279() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "9223372036854775807", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_280() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "9223372036854775808", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_281() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_282() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_283() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", " ", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_284() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "!@#", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_285() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "0", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_286() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "-1", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_287() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "1.5", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_288() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "9223372036854775807", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_289() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "9223372036854775808", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_290() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_291() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_292() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", " ", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_293() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "!@#", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_294() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "0", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_295() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "-1", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_296() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "1.5", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_297() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "9223372036854775807", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_298() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "9223372036854775808", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_299() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_300() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_301() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", " ", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_302() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "a", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_303() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "test123", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_304() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "-1", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_305() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "1.5", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_306() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "9223372036854775807", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_307() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "9223372036854775808", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_308() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_309() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_310() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", " ", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_311() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "a", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_312() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "test123", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_313() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "-1", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_314() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "1.5", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_315() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "9223372036854775807", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_316() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "9223372036854775808", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_317() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_318() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_319() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", " ", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_320() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "a", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_321() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "test123", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_322() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "!@#", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_323() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "0", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_324() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "9223372036854775807", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_325() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "9223372036854775808", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_326() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_327() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_328() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", " ", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_329() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "a", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_330() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "test123", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_331() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "!@#", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_332() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "0", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_333() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "9223372036854775807", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_334() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "9223372036854775808", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_335() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_336() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_337() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", " ", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_338() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "a", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_339() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "test123", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_340() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "!@#", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_341() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "0", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_342() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "-1", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_343() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "1.5", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_344() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_345() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_346() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", " ", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_347() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "a", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_348() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "test123", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_349() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "!@#", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_350() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "0", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_351() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "-1", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_352() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "1.5", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_353() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_354() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", false)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_355() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_356() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_357() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_358() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_359() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_360() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_361() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_362() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getWholeDeclaration_pairwise_363() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", true)).getWholeDeclaration();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_364() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_365() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration(" ", "", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<? ?>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_366() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", " ", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<! !>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_367() throws Exception {
        // Combination: receiver__name="", receiver__baseUri=" ", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("", " ", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<??>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_368() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "a", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!a!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_369() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="a", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("test123", "a", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<?test123?>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_370() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "test123", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!test123!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_371() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="test123", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("a", "test123", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<?a?>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_372() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "!@#", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!@#!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_373() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="!@#", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("0", "!@#", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<?0?>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_374() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "0", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!0!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_375() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="0", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("!@#", "0", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<?!@#?>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_376() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "-1", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!-1!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_377() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="-1", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("1.5", "-1", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<?1.5?>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_378() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "1.5", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!1.5!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_379() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="1.5", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("-1", "1.5", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<?-1?>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_380() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "9223372036854775807", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775807!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_381() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("9223372036854775808", "9223372036854775807", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<?9223372036854775808?>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_382() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "9223372036854775808", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775808!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_383() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("9223372036854775807", "9223372036854775808", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<?9223372036854775807?>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_384() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_385() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<??>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_386() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "a", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_387() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "test123", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_388() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "!@#", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_389() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "0", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_390() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "-1", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_391() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "1.5", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_392() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "9223372036854775807", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_393() throws Exception {
        // Combination: receiver__name="", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("", "9223372036854775808", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_394() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "a", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<! !>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_395() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "test123", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<! !>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_396() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "!@#", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<! !>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_397() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "0", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<! !>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_398() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "-1", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<! !>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_399() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "1.5", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<! !>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_400() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "9223372036854775807", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<! !>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_401() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "9223372036854775808", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<! !>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_402() throws Exception {
        // Combination: receiver__name=" ", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<! !>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_403() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!a!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_404() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", " ", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!a!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_405() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "!@#", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!a!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_406() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "0", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!a!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_407() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "-1", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!a!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_408() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "1.5", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!a!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_409() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "9223372036854775807", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!a!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_410() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "9223372036854775808", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!a!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_411() throws Exception {
        // Combination: receiver__name="a", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!a!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_412() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!test123!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_413() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", " ", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!test123!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_414() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "!@#", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!test123!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_415() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "0", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!test123!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_416() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "-1", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!test123!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_417() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "1.5", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!test123!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_418() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "9223372036854775807", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!test123!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_419() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "9223372036854775808", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!test123!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_420() throws Exception {
        // Combination: receiver__name="test123", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!test123!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_421() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!@#!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_422() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", " ", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!@#!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_423() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "a", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!@#!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_424() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "test123", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!@#!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_425() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "-1", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!@#!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_426() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "1.5", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!@#!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_427() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "9223372036854775807", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!@#!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_428() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "9223372036854775808", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!@#!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_429() throws Exception {
        // Combination: receiver__name="!@#", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!!@#!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_430() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!0!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_431() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", " ", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!0!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_432() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "a", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!0!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_433() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "test123", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!0!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_434() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "-1", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!0!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_435() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "1.5", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!0!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_436() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "9223372036854775807", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!0!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_437() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "9223372036854775808", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!0!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_438() throws Exception {
        // Combination: receiver__name="0", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!0!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_439() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!-1!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_440() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", " ", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!-1!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_441() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "a", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!-1!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_442() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "test123", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!-1!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_443() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "!@#", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!-1!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_444() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "0", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!-1!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_445() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "9223372036854775807", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!-1!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_446() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "9223372036854775808", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!-1!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_447() throws Exception {
        // Combination: receiver__name="-1", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!-1!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_448() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!1.5!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_449() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", " ", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!1.5!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_450() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "a", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!1.5!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_451() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "test123", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!1.5!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_452() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "!@#", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!1.5!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_453() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "0", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!1.5!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_454() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "9223372036854775807", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!1.5!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_455() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "9223372036854775808", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!1.5!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_456() throws Exception {
        // Combination: receiver__name="1.5", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!1.5!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_457() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775807!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_458() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", " ", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775807!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_459() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "a", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775807!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_460() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "test123", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775807!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_461() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "!@#", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775807!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_462() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "0", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775807!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_463() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "-1", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775807!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_464() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "1.5", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775807!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_465() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775807!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_466() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775808!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_467() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", " ", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775808!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_468() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "a", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775808!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_469() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "test123", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775808!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_470() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "!@#", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775808!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_471() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "0", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775808!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_472() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "-1", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775808!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_473() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "1.5", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775808!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_474() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!9223372036854775808!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_475() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="", receiver__isProcessingInstruction=false
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<?aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa?>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_476() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri=" ", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_477() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="a", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_478() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="test123", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_479() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="!@#", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_480() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="0", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_481() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="-1", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_482() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="1.5", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_483() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="9223372036854775807", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_484() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="9223372036854775808", receiver__isProcessingInstruction=true
        Object actual = (new XmlDeclaration("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("<!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!>", formatValue(actual));
    }

}
