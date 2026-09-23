package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for FormElement.
 */
public class FormElement_IPOTest {
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
    public void test_elements_pairwise_001() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("p"), receiver__baseUri="", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("p"), "", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_002() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("div"), receiver__baseUri=" ", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("div"), " ", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_003() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("p"), receiver__baseUri="a", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("p"), "a", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_004() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("p"), receiver__baseUri="test123", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("p"), "test123", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_005() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("p"), receiver__baseUri="!@#", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("p"), "!@#", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_006() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("p"), receiver__baseUri="0", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("p"), "0", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_007() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("p"), receiver__baseUri="-1", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("p"), "-1", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_008() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("p"), receiver__baseUri="1.5", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("p"), "1.5", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_009() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("p"), receiver__baseUri="9223372036854775807", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("p"), "9223372036854775807", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_010() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("p"), receiver__baseUri="9223372036854775808", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("p"), "9223372036854775808", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_011() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("p"), receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("p"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_012() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("p"), receiver__baseUri=" ", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("p"), " ", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_013() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("div"), receiver__baseUri="", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("div"), "", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_014() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("div"), receiver__baseUri="a", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("div"), "a", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_015() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("div"), receiver__baseUri="test123", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("div"), "test123", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_016() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("div"), receiver__baseUri="!@#", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("div"), "!@#", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_017() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("div"), receiver__baseUri="0", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("div"), "0", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_018() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("div"), receiver__baseUri="-1", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("div"), "-1", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_019() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("div"), receiver__baseUri="1.5", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("div"), "1.5", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_020() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("div"), receiver__baseUri="9223372036854775807", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("div"), "9223372036854775807", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_021() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("div"), receiver__baseUri="9223372036854775808", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("div"), "9223372036854775808", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_elements_pairwise_022() throws Exception {
        // Combination: receiver__tag=org.jsoup.parser.Tag.valueOf("div"), receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__attributes=new org.jsoup.nodes.Attributes()
        Object actual = (new FormElement(org.jsoup.parser.Tag.valueOf("div"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new org.jsoup.nodes.Attributes())).elements();
        assertNotNull(actual);
        assertEquals("org.jsoup.select.Elements", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

}
