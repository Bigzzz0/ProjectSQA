package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Compiler.
 */
public class Compiler_IPOTest {
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
    public void test_getSourceLine_pairwise_001() throws Exception {
        // Combination: sourceName="", lineNumber=0
        assertNull((new Compiler()).getSourceLine("", 0));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_002() throws Exception {
        // Combination: sourceName=" ", lineNumber=0
        assertNull((new Compiler()).getSourceLine(" ", 0));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_003() throws Exception {
        // Combination: sourceName="a", lineNumber=0
        assertNull((new Compiler()).getSourceLine("a", 0));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_004() throws Exception {
        // Combination: sourceName="test123", lineNumber=0
        assertNull((new Compiler()).getSourceLine("test123", 0));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_005() throws Exception {
        // Combination: sourceName="!@#", lineNumber=0
        assertNull((new Compiler()).getSourceLine("!@#", 0));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_006() throws Exception {
        // Combination: sourceName="0", lineNumber=0
        assertNull((new Compiler()).getSourceLine("0", 0));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_007() throws Exception {
        // Combination: sourceName="-1", lineNumber=0
        assertNull((new Compiler()).getSourceLine("-1", 0));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_008() throws Exception {
        // Combination: sourceName="1.5", lineNumber=0
        assertNull((new Compiler()).getSourceLine("1.5", 0));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_009() throws Exception {
        // Combination: sourceName="9223372036854775807", lineNumber=0
        assertNull((new Compiler()).getSourceLine("9223372036854775807", 0));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_010() throws Exception {
        // Combination: sourceName="9223372036854775808", lineNumber=0
        assertNull((new Compiler()).getSourceLine("9223372036854775808", 0));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_011() throws Exception {
        // Combination: sourceName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=0
        assertNull((new Compiler()).getSourceLine("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_012() throws Exception {
        // Combination: sourceName="", lineNumber=1
        try {
            (new Compiler()).getSourceLine("", 1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_013() throws Exception {
        // Combination: sourceName=" ", lineNumber=1
        try {
            (new Compiler()).getSourceLine(" ", 1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_014() throws Exception {
        // Combination: sourceName="a", lineNumber=1
        try {
            (new Compiler()).getSourceLine("a", 1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_015() throws Exception {
        // Combination: sourceName="test123", lineNumber=1
        try {
            (new Compiler()).getSourceLine("test123", 1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_016() throws Exception {
        // Combination: sourceName="!@#", lineNumber=1
        try {
            (new Compiler()).getSourceLine("!@#", 1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_017() throws Exception {
        // Combination: sourceName="0", lineNumber=1
        try {
            (new Compiler()).getSourceLine("0", 1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_018() throws Exception {
        // Combination: sourceName="-1", lineNumber=1
        try {
            (new Compiler()).getSourceLine("-1", 1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_019() throws Exception {
        // Combination: sourceName="1.5", lineNumber=1
        try {
            (new Compiler()).getSourceLine("1.5", 1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_020() throws Exception {
        // Combination: sourceName="9223372036854775807", lineNumber=1
        try {
            (new Compiler()).getSourceLine("9223372036854775807", 1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_021() throws Exception {
        // Combination: sourceName="9223372036854775808", lineNumber=1
        try {
            (new Compiler()).getSourceLine("9223372036854775808", 1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_022() throws Exception {
        // Combination: sourceName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=1
        try {
            (new Compiler()).getSourceLine("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_023() throws Exception {
        // Combination: sourceName="", lineNumber=-1
        assertNull((new Compiler()).getSourceLine("", -1));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_024() throws Exception {
        // Combination: sourceName=" ", lineNumber=-1
        assertNull((new Compiler()).getSourceLine(" ", -1));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_025() throws Exception {
        // Combination: sourceName="a", lineNumber=-1
        assertNull((new Compiler()).getSourceLine("a", -1));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_026() throws Exception {
        // Combination: sourceName="test123", lineNumber=-1
        assertNull((new Compiler()).getSourceLine("test123", -1));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_027() throws Exception {
        // Combination: sourceName="!@#", lineNumber=-1
        assertNull((new Compiler()).getSourceLine("!@#", -1));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_028() throws Exception {
        // Combination: sourceName="0", lineNumber=-1
        assertNull((new Compiler()).getSourceLine("0", -1));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_029() throws Exception {
        // Combination: sourceName="-1", lineNumber=-1
        assertNull((new Compiler()).getSourceLine("-1", -1));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_030() throws Exception {
        // Combination: sourceName="1.5", lineNumber=-1
        assertNull((new Compiler()).getSourceLine("1.5", -1));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_031() throws Exception {
        // Combination: sourceName="9223372036854775807", lineNumber=-1
        assertNull((new Compiler()).getSourceLine("9223372036854775807", -1));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_032() throws Exception {
        // Combination: sourceName="9223372036854775808", lineNumber=-1
        assertNull((new Compiler()).getSourceLine("9223372036854775808", -1));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_033() throws Exception {
        // Combination: sourceName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=-1
        assertNull((new Compiler()).getSourceLine("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_034() throws Exception {
        // Combination: sourceName="", lineNumber=Integer.MAX_VALUE
        try {
            (new Compiler()).getSourceLine("", Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_035() throws Exception {
        // Combination: sourceName=" ", lineNumber=Integer.MAX_VALUE
        try {
            (new Compiler()).getSourceLine(" ", Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_036() throws Exception {
        // Combination: sourceName="a", lineNumber=Integer.MAX_VALUE
        try {
            (new Compiler()).getSourceLine("a", Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_037() throws Exception {
        // Combination: sourceName="test123", lineNumber=Integer.MAX_VALUE
        try {
            (new Compiler()).getSourceLine("test123", Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_038() throws Exception {
        // Combination: sourceName="!@#", lineNumber=Integer.MAX_VALUE
        try {
            (new Compiler()).getSourceLine("!@#", Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_039() throws Exception {
        // Combination: sourceName="0", lineNumber=Integer.MAX_VALUE
        try {
            (new Compiler()).getSourceLine("0", Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_040() throws Exception {
        // Combination: sourceName="-1", lineNumber=Integer.MAX_VALUE
        try {
            (new Compiler()).getSourceLine("-1", Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_041() throws Exception {
        // Combination: sourceName="1.5", lineNumber=Integer.MAX_VALUE
        try {
            (new Compiler()).getSourceLine("1.5", Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_042() throws Exception {
        // Combination: sourceName="9223372036854775807", lineNumber=Integer.MAX_VALUE
        try {
            (new Compiler()).getSourceLine("9223372036854775807", Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_043() throws Exception {
        // Combination: sourceName="9223372036854775808", lineNumber=Integer.MAX_VALUE
        try {
            (new Compiler()).getSourceLine("9223372036854775808", Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_044() throws Exception {
        // Combination: sourceName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=Integer.MAX_VALUE
        try {
            (new Compiler()).getSourceLine("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_045() throws Exception {
        // Combination: sourceName="", lineNumber=Integer.MIN_VALUE
        assertNull((new Compiler()).getSourceLine("", Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_046() throws Exception {
        // Combination: sourceName=" ", lineNumber=Integer.MIN_VALUE
        assertNull((new Compiler()).getSourceLine(" ", Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_047() throws Exception {
        // Combination: sourceName="a", lineNumber=Integer.MIN_VALUE
        assertNull((new Compiler()).getSourceLine("a", Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_048() throws Exception {
        // Combination: sourceName="test123", lineNumber=Integer.MIN_VALUE
        assertNull((new Compiler()).getSourceLine("test123", Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_049() throws Exception {
        // Combination: sourceName="!@#", lineNumber=Integer.MIN_VALUE
        assertNull((new Compiler()).getSourceLine("!@#", Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_050() throws Exception {
        // Combination: sourceName="0", lineNumber=Integer.MIN_VALUE
        assertNull((new Compiler()).getSourceLine("0", Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_051() throws Exception {
        // Combination: sourceName="-1", lineNumber=Integer.MIN_VALUE
        assertNull((new Compiler()).getSourceLine("-1", Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_052() throws Exception {
        // Combination: sourceName="1.5", lineNumber=Integer.MIN_VALUE
        assertNull((new Compiler()).getSourceLine("1.5", Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_053() throws Exception {
        // Combination: sourceName="9223372036854775807", lineNumber=Integer.MIN_VALUE
        assertNull((new Compiler()).getSourceLine("9223372036854775807", Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_054() throws Exception {
        // Combination: sourceName="9223372036854775808", lineNumber=Integer.MIN_VALUE
        assertNull((new Compiler()).getSourceLine("9223372036854775808", Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getSourceLine_pairwise_055() throws Exception {
        // Combination: sourceName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=Integer.MIN_VALUE
        assertNull((new Compiler()).getSourceLine("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE));
    }

}
