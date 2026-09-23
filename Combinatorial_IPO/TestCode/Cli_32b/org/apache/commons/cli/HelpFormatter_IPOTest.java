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
    public void test_findWrapPos_pairwise_001() throws Exception {
        // Combination: text="", width=0, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("", 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_002() throws Exception {
        // Combination: text=" ", width=1, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos(" ", 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_003() throws Exception {
        // Combination: text="a", width=-1, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("a", -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_004() throws Exception {
        // Combination: text="test123", width=Integer.MAX_VALUE, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("test123", Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_005() throws Exception {
        // Combination: text="!@#", width=Integer.MIN_VALUE, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("!@#", Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_006() throws Exception {
        // Combination: text="0", width=0, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("0", 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_007() throws Exception {
        // Combination: text="-1", width=0, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("-1", 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_008() throws Exception {
        // Combination: text="1.5", width=0, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("1.5", 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_009() throws Exception {
        // Combination: text="9223372036854775807", width=0, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("9223372036854775807", 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_010() throws Exception {
        // Combination: text="9223372036854775808", width=0, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("9223372036854775808", 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_011() throws Exception {
        // Combination: text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", width=0, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_012() throws Exception {
        // Combination: text="", width=1, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos("", 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_013() throws Exception {
        // Combination: text=" ", width=0, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos(" ", 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_014() throws Exception {
        // Combination: text="a", width=Integer.MAX_VALUE, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos("a", Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_015() throws Exception {
        // Combination: text="test123", width=-1, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos("test123", -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_016() throws Exception {
        // Combination: text="!@#", width=0, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos("!@#", 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_017() throws Exception {
        // Combination: text="0", width=Integer.MIN_VALUE, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos("0", Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_018() throws Exception {
        // Combination: text="-1", width=1, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos("-1", 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_019() throws Exception {
        // Combination: text="1.5", width=1, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos("1.5", 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_020() throws Exception {
        // Combination: text="9223372036854775807", width=1, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos("9223372036854775807", 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_021() throws Exception {
        // Combination: text="9223372036854775808", width=1, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos("9223372036854775808", 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_022() throws Exception {
        // Combination: text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", width=1, startPos=1
        Object actual = (new HelpFormatter()).findWrapPos("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_023() throws Exception {
        // Combination: text="", width=-1, startPos=-1
        Object actual = (new HelpFormatter()).findWrapPos("", -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_024() throws Exception {
        // Combination: text=" ", width=Integer.MAX_VALUE, startPos=-1
        Object actual = (new HelpFormatter()).findWrapPos(" ", Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_025() throws Exception {
        // Combination: text="a", width=0, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("a", 0, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_026() throws Exception {
        // Combination: text="test123", width=1, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("test123", 1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_027() throws Exception {
        // Combination: text="!@#", width=1, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("!@#", 1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_028() throws Exception {
        // Combination: text="0", width=1, startPos=-1
        try {
            (new HelpFormatter()).findWrapPos("0", 1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_029() throws Exception {
        // Combination: text="-1", width=Integer.MIN_VALUE, startPos=-1
        Object actual = (new HelpFormatter()).findWrapPos("-1", Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_030() throws Exception {
        // Combination: text="1.5", width=-1, startPos=-1
        Object actual = (new HelpFormatter()).findWrapPos("1.5", -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_031() throws Exception {
        // Combination: text="9223372036854775807", width=-1, startPos=-1
        Object actual = (new HelpFormatter()).findWrapPos("9223372036854775807", -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_032() throws Exception {
        // Combination: text="9223372036854775808", width=-1, startPos=-1
        Object actual = (new HelpFormatter()).findWrapPos("9223372036854775808", -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_033() throws Exception {
        // Combination: text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", width=-1, startPos=-1
        Object actual = (new HelpFormatter()).findWrapPos("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_034() throws Exception {
        // Combination: text="", width=Integer.MAX_VALUE, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_035() throws Exception {
        // Combination: text=" ", width=-1, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos(" ", -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_036() throws Exception {
        // Combination: text="a", width=1, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("a", 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_037() throws Exception {
        // Combination: text="test123", width=0, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("test123", 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_038() throws Exception {
        // Combination: text="!@#", width=-1, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("!@#", -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_039() throws Exception {
        // Combination: text="0", width=-1, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("0", -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_040() throws Exception {
        // Combination: text="-1", width=-1, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("-1", -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_041() throws Exception {
        // Combination: text="1.5", width=Integer.MIN_VALUE, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("1.5", Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_042() throws Exception {
        // Combination: text="9223372036854775807", width=Integer.MAX_VALUE, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_043() throws Exception {
        // Combination: text="9223372036854775808", width=Integer.MAX_VALUE, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_044() throws Exception {
        // Combination: text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", width=Integer.MAX_VALUE, startPos=Integer.MAX_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_045() throws Exception {
        // Combination: text="", width=Integer.MIN_VALUE, startPos=Integer.MIN_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("", Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_046() throws Exception {
        // Combination: text=" ", width=0, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos(" ", 0, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_047() throws Exception {
        // Combination: text="a", width=1, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("a", 1, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_048() throws Exception {
        // Combination: text="test123", width=-1, startPos=Integer.MIN_VALUE
        Object actual = (new HelpFormatter()).findWrapPos("test123", -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_049() throws Exception {
        // Combination: text="!@#", width=Integer.MAX_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("!@#", Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_050() throws Exception {
        // Combination: text="0", width=Integer.MAX_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("0", Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_051() throws Exception {
        // Combination: text="-1", width=Integer.MAX_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("-1", Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_052() throws Exception {
        // Combination: text="1.5", width=Integer.MAX_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("1.5", Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_053() throws Exception {
        // Combination: text="9223372036854775807", width=Integer.MIN_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_054() throws Exception {
        // Combination: text="9223372036854775808", width=Integer.MIN_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_055() throws Exception {
        // Combination: text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", width=Integer.MIN_VALUE, startPos=Integer.MIN_VALUE
        try {
            (new HelpFormatter()).findWrapPos("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_056() throws Exception {
        // Combination: text=" ", width=Integer.MIN_VALUE, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos(" ", Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_057() throws Exception {
        // Combination: text="a", width=Integer.MIN_VALUE, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("a", Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_findWrapPos_pairwise_058() throws Exception {
        // Combination: text="test123", width=Integer.MIN_VALUE, startPos=0
        Object actual = (new HelpFormatter()).findWrapPos("test123", Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

}
