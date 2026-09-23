package org.jsoup.select;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Selector.
 */
public class Selector_IPOTest {
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
    public void test_select_pairwise_001() throws Exception {
        // Combination: query="", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), "")
        try {
            Selector.select("", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), ""));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_002() throws Exception {
        // Combination: query="", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), "")
        try {
            Selector.select("", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), ""));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_003() throws Exception {
        // Combination: query=" ", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), "")
        try {
            Selector.select(" ", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), ""));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_004() throws Exception {
        // Combination: query=" ", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), "")
        try {
            Selector.select(" ", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), ""));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_005() throws Exception {
        // Combination: query="a", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), "")
        Object actual = Selector.select("a", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_006() throws Exception {
        // Combination: query="a", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), "")
        Object actual = Selector.select("a", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_007() throws Exception {
        // Combination: query="test123", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), "")
        Object actual = Selector.select("test123", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_008() throws Exception {
        // Combination: query="test123", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), "")
        Object actual = Selector.select("test123", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_009() throws Exception {
        // Combination: query="!@#", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), "")
        try {
            Selector.select("!@#", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), ""));
            fail("Expected org.jsoup.select.Selector.SelectorParseException");
        } catch (org.jsoup.select.Selector.SelectorParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_010() throws Exception {
        // Combination: query="!@#", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), "")
        try {
            Selector.select("!@#", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), ""));
            fail("Expected org.jsoup.select.Selector.SelectorParseException");
        } catch (org.jsoup.select.Selector.SelectorParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_011() throws Exception {
        // Combination: query="0", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), "")
        Object actual = Selector.select("0", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_012() throws Exception {
        // Combination: query="0", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), "")
        Object actual = Selector.select("0", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_013() throws Exception {
        // Combination: query="-1", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), "")
        try {
            Selector.select("-1", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), ""));
            fail("Expected org.jsoup.select.Selector.SelectorParseException");
        } catch (org.jsoup.select.Selector.SelectorParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_014() throws Exception {
        // Combination: query="-1", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), "")
        try {
            Selector.select("-1", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), ""));
            fail("Expected org.jsoup.select.Selector.SelectorParseException");
        } catch (org.jsoup.select.Selector.SelectorParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_015() throws Exception {
        // Combination: query="1.5", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), "")
        Object actual = Selector.select("1.5", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_016() throws Exception {
        // Combination: query="1.5", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), "")
        Object actual = Selector.select("1.5", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_017() throws Exception {
        // Combination: query="9223372036854775807", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), "")
        Object actual = Selector.select("9223372036854775807", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_018() throws Exception {
        // Combination: query="9223372036854775807", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), "")
        Object actual = Selector.select("9223372036854775807", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_019() throws Exception {
        // Combination: query="9223372036854775808", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), "")
        Object actual = Selector.select("9223372036854775808", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_020() throws Exception {
        // Combination: query="9223372036854775808", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), "")
        Object actual = Selector.select("9223372036854775808", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_021() throws Exception {
        // Combination: query="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), "")
        Object actual = Selector.select("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_select_pairwise_022() throws Exception {
        // Combination: query="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", root=new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), "")
        Object actual = Selector.select("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), ""));
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

}
