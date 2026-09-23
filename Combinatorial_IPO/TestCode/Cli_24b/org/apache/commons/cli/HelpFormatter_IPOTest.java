package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for HelpFormatter.
 */
public class HelpFormatter_IPOTest {
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
    public void test_renderWrappedText_pairwise_001() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=0, nextLineTabStop=0, text=""
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 0, 0, "");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_002() throws Exception {
        // Combination: sb=new java.lang.StringBuffer("test"), width=1, nextLineTabStop=0, text=" "
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer("test"), 1, 0, " ");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("test", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_003() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=-1, nextLineTabStop=1, text=" "
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), -1, 1, " ");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_004() throws Exception {
        // Combination: sb=new java.lang.StringBuffer("test"), width=Integer.MAX_VALUE, nextLineTabStop=1, text=""
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer("test"), Integer.MAX_VALUE, 1, "");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("test", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_005() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=1, nextLineTabStop=-1, text="a"
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 1, -1, "a");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_006() throws Exception {
        // Combination: sb=new java.lang.StringBuffer("test"), width=0, nextLineTabStop=-1, text="test123"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer("test"), 0, -1, "test123");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_007() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MAX_VALUE, nextLineTabStop=Integer.MAX_VALUE, text="test123"
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MAX_VALUE, Integer.MAX_VALUE, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_008() throws Exception {
        // Combination: sb=new java.lang.StringBuffer("test"), width=-1, nextLineTabStop=Integer.MAX_VALUE, text="a"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer("test"), -1, Integer.MAX_VALUE, "a");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_009() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MIN_VALUE, nextLineTabStop=Integer.MIN_VALUE, text="!@#"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MIN_VALUE, Integer.MIN_VALUE, "!@#");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_010() throws Exception {
        // Combination: sb=new java.lang.StringBuffer("test"), width=0, nextLineTabStop=Integer.MIN_VALUE, text="0"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer("test"), 0, Integer.MIN_VALUE, "0");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_011() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=-1, nextLineTabStop=-1, text=""
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), -1, -1, "");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_012() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=1, nextLineTabStop=Integer.MAX_VALUE, text=""
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 1, Integer.MAX_VALUE, "");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_013() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=1, nextLineTabStop=Integer.MIN_VALUE, text=""
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 1, Integer.MIN_VALUE, "");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_014() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MAX_VALUE, nextLineTabStop=-1, text=" "
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MAX_VALUE, -1, " ");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_015() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=0, nextLineTabStop=Integer.MAX_VALUE, text=" "
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 0, Integer.MAX_VALUE, " ");
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_016() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=-1, nextLineTabStop=Integer.MIN_VALUE, text=" "
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), -1, Integer.MIN_VALUE, " ");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_017() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MAX_VALUE, nextLineTabStop=0, text="a"
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MAX_VALUE, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_018() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=0, nextLineTabStop=1, text="a"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 0, 1, "a");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_019() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MAX_VALUE, nextLineTabStop=Integer.MIN_VALUE, text="a"
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MAX_VALUE, Integer.MIN_VALUE, "a");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_020() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=-1, nextLineTabStop=0, text="test123"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), -1, 0, "test123");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_021() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=1, nextLineTabStop=1, text="test123"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 1, 1, "test123");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_022() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MIN_VALUE, nextLineTabStop=Integer.MIN_VALUE, text="test123"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MIN_VALUE, Integer.MIN_VALUE, "test123");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_023() throws Exception {
        // Combination: sb=new java.lang.StringBuffer("test"), width=Integer.MIN_VALUE, nextLineTabStop=0, text="!@#"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer("test"), Integer.MIN_VALUE, 0, "!@#");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_024() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=0, nextLineTabStop=1, text="!@#"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 0, 1, "!@#");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_025() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=1, nextLineTabStop=-1, text="!@#"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 1, -1, "!@#");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_026() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=-1, nextLineTabStop=Integer.MAX_VALUE, text="!@#"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), -1, Integer.MAX_VALUE, "!@#");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_027() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=1, nextLineTabStop=0, text="0"
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 1, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_028() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MIN_VALUE, nextLineTabStop=1, text="0"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MIN_VALUE, 1, "0");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_029() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=-1, nextLineTabStop=-1, text="0"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), -1, -1, "0");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_030() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MAX_VALUE, nextLineTabStop=Integer.MAX_VALUE, text="0"
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MAX_VALUE, Integer.MAX_VALUE, "0");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_031() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=0, nextLineTabStop=0, text="-1"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 0, 0, "-1");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_032() throws Exception {
        // Combination: sb=new java.lang.StringBuffer("test"), width=1, nextLineTabStop=1, text="-1"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer("test"), 1, 1, "-1");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_033() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MIN_VALUE, nextLineTabStop=-1, text="-1"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MIN_VALUE, -1, "-1");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_034() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=-1, nextLineTabStop=Integer.MAX_VALUE, text="-1"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), -1, Integer.MAX_VALUE, "-1");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_035() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MAX_VALUE, nextLineTabStop=Integer.MIN_VALUE, text="-1"
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MAX_VALUE, Integer.MIN_VALUE, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_036() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=0, nextLineTabStop=0, text="1.5"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 0, 0, "1.5");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_037() throws Exception {
        // Combination: sb=new java.lang.StringBuffer("test"), width=1, nextLineTabStop=1, text="1.5"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer("test"), 1, 1, "1.5");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_038() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=-1, nextLineTabStop=-1, text="1.5"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), -1, -1, "1.5");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_039() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MIN_VALUE, nextLineTabStop=Integer.MAX_VALUE, text="1.5"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MIN_VALUE, Integer.MAX_VALUE, "1.5");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_040() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MAX_VALUE, nextLineTabStop=Integer.MIN_VALUE, text="1.5"
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MAX_VALUE, Integer.MIN_VALUE, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_041() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=0, nextLineTabStop=0, text="9223372036854775807"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 0, 0, "9223372036854775807");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_042() throws Exception {
        // Combination: sb=new java.lang.StringBuffer("test"), width=1, nextLineTabStop=1, text="9223372036854775807"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer("test"), 1, 1, "9223372036854775807");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_043() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=-1, nextLineTabStop=-1, text="9223372036854775807"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), -1, -1, "9223372036854775807");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_044() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MAX_VALUE, nextLineTabStop=Integer.MAX_VALUE, text="9223372036854775807"
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MAX_VALUE, Integer.MAX_VALUE, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_045() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MIN_VALUE, nextLineTabStop=Integer.MIN_VALUE, text="9223372036854775807"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MIN_VALUE, Integer.MIN_VALUE, "9223372036854775807");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_046() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=0, nextLineTabStop=0, text="9223372036854775808"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 0, 0, "9223372036854775808");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_047() throws Exception {
        // Combination: sb=new java.lang.StringBuffer("test"), width=1, nextLineTabStop=1, text="9223372036854775808"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer("test"), 1, 1, "9223372036854775808");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_048() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=-1, nextLineTabStop=-1, text="9223372036854775808"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), -1, -1, "9223372036854775808");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_049() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MAX_VALUE, nextLineTabStop=Integer.MAX_VALUE, text="9223372036854775808"
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MAX_VALUE, Integer.MAX_VALUE, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_050() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MIN_VALUE, nextLineTabStop=Integer.MIN_VALUE, text="9223372036854775808"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MIN_VALUE, Integer.MIN_VALUE, "9223372036854775808");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_051() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=0, nextLineTabStop=0, text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_052() throws Exception {
        // Combination: sb=new java.lang.StringBuffer("test"), width=1, nextLineTabStop=1, text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer("test"), 1, 1, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_053() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=-1, nextLineTabStop=-1, text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), -1, -1, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_054() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MAX_VALUE, nextLineTabStop=Integer.MAX_VALUE, text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MAX_VALUE, Integer.MAX_VALUE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_055() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MIN_VALUE, nextLineTabStop=Integer.MIN_VALUE, text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MIN_VALUE, Integer.MIN_VALUE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_056() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MAX_VALUE, nextLineTabStop=0, text="!@#"
        Object actual = (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MAX_VALUE, 0, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.StringBuffer", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_057() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MIN_VALUE, nextLineTabStop=0, text=""
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MIN_VALUE, 0, "");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_058() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MIN_VALUE, nextLineTabStop=0, text=" "
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MIN_VALUE, 0, " ");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_renderWrappedText_pairwise_059() throws Exception {
        // Combination: sb=new java.lang.StringBuffer(""), width=Integer.MIN_VALUE, nextLineTabStop=0, text="a"
        try {
            (new HelpFormatter()).renderWrappedText(new java.lang.StringBuffer(""), Integer.MIN_VALUE, 0, "a");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_060() throws Exception {
        // Combination: text="", width=0, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("", 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_061() throws Exception {
        // Combination: text=" ", width=1, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos(" ", 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_062() throws Exception {
        // Combination: text="a", width=-1, startPos=0
        try {
            (new HelpFormatter()).findWrapPos("a", -1, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_063() throws Exception {
        // Combination: text="test123", width=Integer.MAX_VALUE, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("test123", Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_064() throws Exception {
        // Combination: text="!@#", width=Integer.MIN_VALUE, startPos=0
        try {
            (new HelpFormatter()).findWrapPos("!@#", Integer.MIN_VALUE, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_065() throws Exception {
        // Combination: text="0", width=0, startPos=0
        try {
            (new HelpFormatter()).findWrapPos("0", 0, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_066() throws Exception {
        // Combination: text="-1", width=0, startPos=0
        try {
            (new HelpFormatter()).findWrapPos("-1", 0, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_067() throws Exception {
        // Combination: text="1.5", width=0, startPos=0
        try {
            (new HelpFormatter()).findWrapPos("1.5", 0, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_068() throws Exception {
        // Combination: text="9223372036854775807", width=0, startPos=0
        try {
            (new HelpFormatter()).findWrapPos("9223372036854775807", 0, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_069() throws Exception {
        // Combination: text="9223372036854775808", width=0, startPos=0
        try {
            (new HelpFormatter()).findWrapPos("9223372036854775808", 0, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_070() throws Exception {
        // Combination: text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", width=0, startPos=0
        try {
            (new HelpFormatter()).findWrapPos("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_071() throws Exception {
        // Combination: text="", width=1, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos("", 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_072() throws Exception {
        // Combination: text=" ", width=0, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos(" ", 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_073() throws Exception {
        // Combination: text="a", width=Integer.MAX_VALUE, startPos=1
        try {
            (new HelpFormatter()).findWrapPos("a", Integer.MAX_VALUE, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_074() throws Exception {
        // Combination: text="test123", width=-1, startPos=1
        try {
            (new HelpFormatter()).findWrapPos("test123", -1, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_075() throws Exception {
        // Combination: text="!@#", width=0, startPos=1
        try {
            (new HelpFormatter()).findWrapPos("!@#", 0, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_076() throws Exception {
        // Combination: text="0", width=Integer.MIN_VALUE, startPos=1
        try {
            (new HelpFormatter()).findWrapPos("0", Integer.MIN_VALUE, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_077() throws Exception {
        // Combination: text="-1", width=1, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos("-1", 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_078() throws Exception {
        // Combination: text="1.5", width=1, startPos=1
        try {
            (new HelpFormatter()).findWrapPos("1.5", 1, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_079() throws Exception {
        // Combination: text="9223372036854775807", width=1, startPos=1
        try {
            (new HelpFormatter()).findWrapPos("9223372036854775807", 1, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_080() throws Exception {
        // Combination: text="9223372036854775808", width=1, startPos=1
        try {
            (new HelpFormatter()).findWrapPos("9223372036854775808", 1, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_081() throws Exception {
        // Combination: text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", width=1, startPos=1
        try {
            (new HelpFormatter()).findWrapPos("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_082() throws Exception {
        // Combination: text="", width=-1, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("", -1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_083() throws Exception {
        // Combination: text=" ", width=Integer.MAX_VALUE, startPos=-1
        Object actual = (new HelpFormatter()).findWrapPos(" ", Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_084() throws Exception {
        // Combination: text="a", width=0, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("a", 0, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_085() throws Exception {
        // Combination: text="test123", width=1, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("test123", 1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_086() throws Exception {
        // Combination: text="!@#", width=1, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("!@#", 1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_087() throws Exception {
        // Combination: text="0", width=1, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("0", 1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_088() throws Exception {
        // Combination: text="-1", width=Integer.MIN_VALUE, startPos=-1
        Object actual = (new HelpFormatter()).findWrapPos("-1", Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_089() throws Exception {
        // Combination: text="1.5", width=-1, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("1.5", -1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_090() throws Exception {
        // Combination: text="9223372036854775807", width=-1, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("9223372036854775807", -1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_091() throws Exception {
        // Combination: text="9223372036854775808", width=-1, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("9223372036854775808", -1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_092() throws Exception {
        // Combination: text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", width=-1, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_093() throws Exception {
        // Combination: text="", width=Integer.MAX_VALUE, startPos=Integer.MAX_VALUE
        try {
            (new HelpFormatter()).findWrapPos("", Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_094() throws Exception {
        // Combination: text=" ", width=-1, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos(" ", -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_095() throws Exception {
        // Combination: text="a", width=1, startPos=Integer.MAX_VALUE
        try {
            (new HelpFormatter()).findWrapPos("a", 1, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_096() throws Exception {
        // Combination: text="test123", width=0, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("test123", 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_097() throws Exception {
        // Combination: text="!@#", width=-1, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("!@#", -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_098() throws Exception {
        // Combination: text="0", width=-1, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("0", -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_099() throws Exception {
        // Combination: text="-1", width=-1, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("-1", -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_100() throws Exception {
        // Combination: text="1.5", width=Integer.MIN_VALUE, startPos=Integer.MAX_VALUE
        try {
            (new HelpFormatter()).findWrapPos("1.5", Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_101() throws Exception {
        // Combination: text="9223372036854775807", width=Integer.MAX_VALUE, startPos=Integer.MAX_VALUE
        try {
            (new HelpFormatter()).findWrapPos("9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_102() throws Exception {
        // Combination: text="9223372036854775808", width=Integer.MAX_VALUE, startPos=Integer.MAX_VALUE
        try {
            (new HelpFormatter()).findWrapPos("9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_103() throws Exception {
        // Combination: text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", width=Integer.MAX_VALUE, startPos=Integer.MAX_VALUE
        try {
            (new HelpFormatter()).findWrapPos("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_104() throws Exception {
        // Combination: text="", width=Integer.MIN_VALUE, startPos=Integer.MIN_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("", Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_105() throws Exception {
        // Combination: text=" ", width=0, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos(" ", 0, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_106() throws Exception {
        // Combination: text="a", width=1, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("a", 1, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_107() throws Exception {
        // Combination: text="test123", width=-1, startPos=Integer.MIN_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("test123", -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_108() throws Exception {
        // Combination: text="!@#", width=Integer.MAX_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("!@#", Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_109() throws Exception {
        // Combination: text="0", width=Integer.MAX_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("0", Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_110() throws Exception {
        // Combination: text="-1", width=Integer.MAX_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("-1", Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_111() throws Exception {
        // Combination: text="1.5", width=Integer.MAX_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("1.5", Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_112() throws Exception {
        // Combination: text="9223372036854775807", width=Integer.MIN_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_113() throws Exception {
        // Combination: text="9223372036854775808", width=Integer.MIN_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_114() throws Exception {
        // Combination: text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", width=Integer.MIN_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_115() throws Exception {
        // Combination: text=" ", width=Integer.MIN_VALUE, startPos=0
        try {
            (new HelpFormatter()).findWrapPos(" ", Integer.MIN_VALUE, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_116() throws Exception {
        // Combination: text="a", width=Integer.MIN_VALUE, startPos=0
        try {
            (new HelpFormatter()).findWrapPos("a", Integer.MIN_VALUE, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_117() throws Exception {
        // Combination: text="test123", width=Integer.MIN_VALUE, startPos=0
        try {
            (new HelpFormatter()).findWrapPos("test123", Integer.MIN_VALUE, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
