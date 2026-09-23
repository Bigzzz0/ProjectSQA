package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for TreeBuilder.
 */
public class TreeBuilder_IPOTest {
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
    public void test_processStartTag_pairwise_001() throws Exception {
        // Combination: name="", attrs=new org.jsoup.nodes.Attributes()
        try {
            (new org.jsoup.parser.HtmlTreeBuilder()).processStartTag("", new org.jsoup.nodes.Attributes());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_processStartTag_pairwise_002() throws Exception {
        // Combination: name=" ", attrs=new org.jsoup.nodes.Attributes()
        try {
            (new org.jsoup.parser.HtmlTreeBuilder()).processStartTag(" ", new org.jsoup.nodes.Attributes());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_processStartTag_pairwise_003() throws Exception {
        // Combination: name="a", attrs=new org.jsoup.nodes.Attributes()
        try {
            (new org.jsoup.parser.HtmlTreeBuilder()).processStartTag("a", new org.jsoup.nodes.Attributes());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_processStartTag_pairwise_004() throws Exception {
        // Combination: name="test123", attrs=new org.jsoup.nodes.Attributes()
        try {
            (new org.jsoup.parser.HtmlTreeBuilder()).processStartTag("test123", new org.jsoup.nodes.Attributes());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_processStartTag_pairwise_005() throws Exception {
        // Combination: name="!@#", attrs=new org.jsoup.nodes.Attributes()
        try {
            (new org.jsoup.parser.HtmlTreeBuilder()).processStartTag("!@#", new org.jsoup.nodes.Attributes());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_processStartTag_pairwise_006() throws Exception {
        // Combination: name="0", attrs=new org.jsoup.nodes.Attributes()
        try {
            (new org.jsoup.parser.HtmlTreeBuilder()).processStartTag("0", new org.jsoup.nodes.Attributes());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_processStartTag_pairwise_007() throws Exception {
        // Combination: name="-1", attrs=new org.jsoup.nodes.Attributes()
        try {
            (new org.jsoup.parser.HtmlTreeBuilder()).processStartTag("-1", new org.jsoup.nodes.Attributes());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_processStartTag_pairwise_008() throws Exception {
        // Combination: name="1.5", attrs=new org.jsoup.nodes.Attributes()
        try {
            (new org.jsoup.parser.HtmlTreeBuilder()).processStartTag("1.5", new org.jsoup.nodes.Attributes());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_processStartTag_pairwise_009() throws Exception {
        // Combination: name="9223372036854775807", attrs=new org.jsoup.nodes.Attributes()
        try {
            (new org.jsoup.parser.HtmlTreeBuilder()).processStartTag("9223372036854775807", new org.jsoup.nodes.Attributes());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_processStartTag_pairwise_010() throws Exception {
        // Combination: name="9223372036854775808", attrs=new org.jsoup.nodes.Attributes()
        try {
            (new org.jsoup.parser.HtmlTreeBuilder()).processStartTag("9223372036854775808", new org.jsoup.nodes.Attributes());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_processStartTag_pairwise_011() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", attrs=new org.jsoup.nodes.Attributes()
        try {
            (new org.jsoup.parser.HtmlTreeBuilder()).processStartTag("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new org.jsoup.nodes.Attributes());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
