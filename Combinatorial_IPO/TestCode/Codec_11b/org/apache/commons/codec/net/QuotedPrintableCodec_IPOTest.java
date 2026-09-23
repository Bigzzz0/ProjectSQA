package org.apache.commons.codec.net;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for QuotedPrintableCodec.
 */
public class QuotedPrintableCodec_IPOTest {
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
    public void test_decode_pairwise_001() throws Exception {
        // Combination: pString="", charset=""
        try {
            (new QuotedPrintableCodec()).decode("", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_002() throws Exception {
        // Combination: pString=" ", charset=""
        try {
            (new QuotedPrintableCodec()).decode(" ", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_003() throws Exception {
        // Combination: pString="a", charset=""
        try {
            (new QuotedPrintableCodec()).decode("a", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_004() throws Exception {
        // Combination: pString="test123", charset=""
        try {
            (new QuotedPrintableCodec()).decode("test123", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_005() throws Exception {
        // Combination: pString="!@#", charset=""
        try {
            (new QuotedPrintableCodec()).decode("!@#", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_006() throws Exception {
        // Combination: pString="0", charset=""
        try {
            (new QuotedPrintableCodec()).decode("0", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_007() throws Exception {
        // Combination: pString="-1", charset=""
        try {
            (new QuotedPrintableCodec()).decode("-1", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_008() throws Exception {
        // Combination: pString="1.5", charset=""
        try {
            (new QuotedPrintableCodec()).decode("1.5", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_009() throws Exception {
        // Combination: pString="9223372036854775807", charset=""
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775807", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_010() throws Exception {
        // Combination: pString="9223372036854775808", charset=""
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775808", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_011() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset=""
        try {
            (new QuotedPrintableCodec()).decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_012() throws Exception {
        // Combination: pString="", charset=" "
        try {
            (new QuotedPrintableCodec()).decode("", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_013() throws Exception {
        // Combination: pString=" ", charset=" "
        try {
            (new QuotedPrintableCodec()).decode(" ", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_014() throws Exception {
        // Combination: pString="a", charset=" "
        try {
            (new QuotedPrintableCodec()).decode("a", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_015() throws Exception {
        // Combination: pString="test123", charset=" "
        try {
            (new QuotedPrintableCodec()).decode("test123", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_016() throws Exception {
        // Combination: pString="!@#", charset=" "
        try {
            (new QuotedPrintableCodec()).decode("!@#", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_017() throws Exception {
        // Combination: pString="0", charset=" "
        try {
            (new QuotedPrintableCodec()).decode("0", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_018() throws Exception {
        // Combination: pString="-1", charset=" "
        try {
            (new QuotedPrintableCodec()).decode("-1", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_019() throws Exception {
        // Combination: pString="1.5", charset=" "
        try {
            (new QuotedPrintableCodec()).decode("1.5", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_020() throws Exception {
        // Combination: pString="9223372036854775807", charset=" "
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775807", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_021() throws Exception {
        // Combination: pString="9223372036854775808", charset=" "
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775808", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_022() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset=" "
        try {
            (new QuotedPrintableCodec()).decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_023() throws Exception {
        // Combination: pString="", charset="a"
        try {
            (new QuotedPrintableCodec()).decode("", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_024() throws Exception {
        // Combination: pString=" ", charset="a"
        try {
            (new QuotedPrintableCodec()).decode(" ", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_025() throws Exception {
        // Combination: pString="a", charset="a"
        try {
            (new QuotedPrintableCodec()).decode("a", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_026() throws Exception {
        // Combination: pString="test123", charset="a"
        try {
            (new QuotedPrintableCodec()).decode("test123", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_027() throws Exception {
        // Combination: pString="!@#", charset="a"
        try {
            (new QuotedPrintableCodec()).decode("!@#", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_028() throws Exception {
        // Combination: pString="0", charset="a"
        try {
            (new QuotedPrintableCodec()).decode("0", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_029() throws Exception {
        // Combination: pString="-1", charset="a"
        try {
            (new QuotedPrintableCodec()).decode("-1", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_030() throws Exception {
        // Combination: pString="1.5", charset="a"
        try {
            (new QuotedPrintableCodec()).decode("1.5", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_031() throws Exception {
        // Combination: pString="9223372036854775807", charset="a"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775807", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_032() throws Exception {
        // Combination: pString="9223372036854775808", charset="a"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775808", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_033() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="a"
        try {
            (new QuotedPrintableCodec()).decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_034() throws Exception {
        // Combination: pString="", charset="test123"
        try {
            (new QuotedPrintableCodec()).decode("", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_035() throws Exception {
        // Combination: pString=" ", charset="test123"
        try {
            (new QuotedPrintableCodec()).decode(" ", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_036() throws Exception {
        // Combination: pString="a", charset="test123"
        try {
            (new QuotedPrintableCodec()).decode("a", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_037() throws Exception {
        // Combination: pString="test123", charset="test123"
        try {
            (new QuotedPrintableCodec()).decode("test123", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_038() throws Exception {
        // Combination: pString="!@#", charset="test123"
        try {
            (new QuotedPrintableCodec()).decode("!@#", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_039() throws Exception {
        // Combination: pString="0", charset="test123"
        try {
            (new QuotedPrintableCodec()).decode("0", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_040() throws Exception {
        // Combination: pString="-1", charset="test123"
        try {
            (new QuotedPrintableCodec()).decode("-1", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_041() throws Exception {
        // Combination: pString="1.5", charset="test123"
        try {
            (new QuotedPrintableCodec()).decode("1.5", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_042() throws Exception {
        // Combination: pString="9223372036854775807", charset="test123"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775807", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_043() throws Exception {
        // Combination: pString="9223372036854775808", charset="test123"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775808", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_044() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="test123"
        try {
            (new QuotedPrintableCodec()).decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_045() throws Exception {
        // Combination: pString="", charset="!@#"
        try {
            (new QuotedPrintableCodec()).decode("", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_046() throws Exception {
        // Combination: pString=" ", charset="!@#"
        try {
            (new QuotedPrintableCodec()).decode(" ", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_047() throws Exception {
        // Combination: pString="a", charset="!@#"
        try {
            (new QuotedPrintableCodec()).decode("a", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_048() throws Exception {
        // Combination: pString="test123", charset="!@#"
        try {
            (new QuotedPrintableCodec()).decode("test123", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_049() throws Exception {
        // Combination: pString="!@#", charset="!@#"
        try {
            (new QuotedPrintableCodec()).decode("!@#", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_050() throws Exception {
        // Combination: pString="0", charset="!@#"
        try {
            (new QuotedPrintableCodec()).decode("0", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_051() throws Exception {
        // Combination: pString="-1", charset="!@#"
        try {
            (new QuotedPrintableCodec()).decode("-1", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_052() throws Exception {
        // Combination: pString="1.5", charset="!@#"
        try {
            (new QuotedPrintableCodec()).decode("1.5", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_053() throws Exception {
        // Combination: pString="9223372036854775807", charset="!@#"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775807", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_054() throws Exception {
        // Combination: pString="9223372036854775808", charset="!@#"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775808", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_055() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="!@#"
        try {
            (new QuotedPrintableCodec()).decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_056() throws Exception {
        // Combination: pString="", charset="0"
        try {
            (new QuotedPrintableCodec()).decode("", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_057() throws Exception {
        // Combination: pString=" ", charset="0"
        try {
            (new QuotedPrintableCodec()).decode(" ", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_058() throws Exception {
        // Combination: pString="a", charset="0"
        try {
            (new QuotedPrintableCodec()).decode("a", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_059() throws Exception {
        // Combination: pString="test123", charset="0"
        try {
            (new QuotedPrintableCodec()).decode("test123", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_060() throws Exception {
        // Combination: pString="!@#", charset="0"
        try {
            (new QuotedPrintableCodec()).decode("!@#", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_061() throws Exception {
        // Combination: pString="0", charset="0"
        try {
            (new QuotedPrintableCodec()).decode("0", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_062() throws Exception {
        // Combination: pString="-1", charset="0"
        try {
            (new QuotedPrintableCodec()).decode("-1", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_063() throws Exception {
        // Combination: pString="1.5", charset="0"
        try {
            (new QuotedPrintableCodec()).decode("1.5", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_064() throws Exception {
        // Combination: pString="9223372036854775807", charset="0"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775807", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_065() throws Exception {
        // Combination: pString="9223372036854775808", charset="0"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775808", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_066() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="0"
        try {
            (new QuotedPrintableCodec()).decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_067() throws Exception {
        // Combination: pString="", charset="-1"
        try {
            (new QuotedPrintableCodec()).decode("", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_068() throws Exception {
        // Combination: pString=" ", charset="-1"
        try {
            (new QuotedPrintableCodec()).decode(" ", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_069() throws Exception {
        // Combination: pString="a", charset="-1"
        try {
            (new QuotedPrintableCodec()).decode("a", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_070() throws Exception {
        // Combination: pString="test123", charset="-1"
        try {
            (new QuotedPrintableCodec()).decode("test123", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_071() throws Exception {
        // Combination: pString="!@#", charset="-1"
        try {
            (new QuotedPrintableCodec()).decode("!@#", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_072() throws Exception {
        // Combination: pString="0", charset="-1"
        try {
            (new QuotedPrintableCodec()).decode("0", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_073() throws Exception {
        // Combination: pString="-1", charset="-1"
        try {
            (new QuotedPrintableCodec()).decode("-1", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_074() throws Exception {
        // Combination: pString="1.5", charset="-1"
        try {
            (new QuotedPrintableCodec()).decode("1.5", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_075() throws Exception {
        // Combination: pString="9223372036854775807", charset="-1"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775807", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_076() throws Exception {
        // Combination: pString="9223372036854775808", charset="-1"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775808", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_077() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="-1"
        try {
            (new QuotedPrintableCodec()).decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_078() throws Exception {
        // Combination: pString="", charset="1.5"
        try {
            (new QuotedPrintableCodec()).decode("", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_079() throws Exception {
        // Combination: pString=" ", charset="1.5"
        try {
            (new QuotedPrintableCodec()).decode(" ", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_080() throws Exception {
        // Combination: pString="a", charset="1.5"
        try {
            (new QuotedPrintableCodec()).decode("a", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_081() throws Exception {
        // Combination: pString="test123", charset="1.5"
        try {
            (new QuotedPrintableCodec()).decode("test123", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_082() throws Exception {
        // Combination: pString="!@#", charset="1.5"
        try {
            (new QuotedPrintableCodec()).decode("!@#", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_083() throws Exception {
        // Combination: pString="0", charset="1.5"
        try {
            (new QuotedPrintableCodec()).decode("0", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_084() throws Exception {
        // Combination: pString="-1", charset="1.5"
        try {
            (new QuotedPrintableCodec()).decode("-1", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_085() throws Exception {
        // Combination: pString="1.5", charset="1.5"
        try {
            (new QuotedPrintableCodec()).decode("1.5", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_086() throws Exception {
        // Combination: pString="9223372036854775807", charset="1.5"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775807", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_087() throws Exception {
        // Combination: pString="9223372036854775808", charset="1.5"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775808", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_088() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="1.5"
        try {
            (new QuotedPrintableCodec()).decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_089() throws Exception {
        // Combination: pString="", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).decode("", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_090() throws Exception {
        // Combination: pString=" ", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).decode(" ", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_091() throws Exception {
        // Combination: pString="a", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).decode("a", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_092() throws Exception {
        // Combination: pString="test123", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).decode("test123", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_093() throws Exception {
        // Combination: pString="!@#", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).decode("!@#", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_094() throws Exception {
        // Combination: pString="0", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).decode("0", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_095() throws Exception {
        // Combination: pString="-1", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).decode("-1", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_096() throws Exception {
        // Combination: pString="1.5", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).decode("1.5", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_097() throws Exception {
        // Combination: pString="9223372036854775807", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775807", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_098() throws Exception {
        // Combination: pString="9223372036854775808", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775808", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_099() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_100() throws Exception {
        // Combination: pString="", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).decode("", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_101() throws Exception {
        // Combination: pString=" ", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).decode(" ", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_102() throws Exception {
        // Combination: pString="a", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).decode("a", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_103() throws Exception {
        // Combination: pString="test123", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).decode("test123", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_104() throws Exception {
        // Combination: pString="!@#", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).decode("!@#", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_105() throws Exception {
        // Combination: pString="0", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).decode("0", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_106() throws Exception {
        // Combination: pString="-1", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).decode("-1", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_107() throws Exception {
        // Combination: pString="1.5", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).decode("1.5", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_108() throws Exception {
        // Combination: pString="9223372036854775807", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775807", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_109() throws Exception {
        // Combination: pString="9223372036854775808", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775808", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_110() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_111() throws Exception {
        // Combination: pString="", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).decode("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_112() throws Exception {
        // Combination: pString=" ", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).decode(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_113() throws Exception {
        // Combination: pString="a", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).decode("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_114() throws Exception {
        // Combination: pString="test123", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).decode("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_115() throws Exception {
        // Combination: pString="!@#", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).decode("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_116() throws Exception {
        // Combination: pString="0", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).decode("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_117() throws Exception {
        // Combination: pString="-1", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).decode("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_118() throws Exception {
        // Combination: pString="1.5", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).decode("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_119() throws Exception {
        // Combination: pString="9223372036854775807", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_120() throws Exception {
        // Combination: pString="9223372036854775808", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).decode("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_decode_pairwise_121() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_122() throws Exception {
        // Combination: pString="", charset=""
        try {
            (new QuotedPrintableCodec()).encode("", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_123() throws Exception {
        // Combination: pString=" ", charset=""
        try {
            (new QuotedPrintableCodec()).encode(" ", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_124() throws Exception {
        // Combination: pString="a", charset=""
        try {
            (new QuotedPrintableCodec()).encode("a", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_125() throws Exception {
        // Combination: pString="test123", charset=""
        try {
            (new QuotedPrintableCodec()).encode("test123", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_126() throws Exception {
        // Combination: pString="!@#", charset=""
        try {
            (new QuotedPrintableCodec()).encode("!@#", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_127() throws Exception {
        // Combination: pString="0", charset=""
        try {
            (new QuotedPrintableCodec()).encode("0", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_128() throws Exception {
        // Combination: pString="-1", charset=""
        try {
            (new QuotedPrintableCodec()).encode("-1", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_129() throws Exception {
        // Combination: pString="1.5", charset=""
        try {
            (new QuotedPrintableCodec()).encode("1.5", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_130() throws Exception {
        // Combination: pString="9223372036854775807", charset=""
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775807", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_131() throws Exception {
        // Combination: pString="9223372036854775808", charset=""
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775808", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_132() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset=""
        try {
            (new QuotedPrintableCodec()).encode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_133() throws Exception {
        // Combination: pString="", charset=" "
        try {
            (new QuotedPrintableCodec()).encode("", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_134() throws Exception {
        // Combination: pString=" ", charset=" "
        try {
            (new QuotedPrintableCodec()).encode(" ", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_135() throws Exception {
        // Combination: pString="a", charset=" "
        try {
            (new QuotedPrintableCodec()).encode("a", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_136() throws Exception {
        // Combination: pString="test123", charset=" "
        try {
            (new QuotedPrintableCodec()).encode("test123", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_137() throws Exception {
        // Combination: pString="!@#", charset=" "
        try {
            (new QuotedPrintableCodec()).encode("!@#", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_138() throws Exception {
        // Combination: pString="0", charset=" "
        try {
            (new QuotedPrintableCodec()).encode("0", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_139() throws Exception {
        // Combination: pString="-1", charset=" "
        try {
            (new QuotedPrintableCodec()).encode("-1", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_140() throws Exception {
        // Combination: pString="1.5", charset=" "
        try {
            (new QuotedPrintableCodec()).encode("1.5", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_141() throws Exception {
        // Combination: pString="9223372036854775807", charset=" "
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775807", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_142() throws Exception {
        // Combination: pString="9223372036854775808", charset=" "
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775808", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_143() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset=" "
        try {
            (new QuotedPrintableCodec()).encode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_144() throws Exception {
        // Combination: pString="", charset="a"
        try {
            (new QuotedPrintableCodec()).encode("", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_145() throws Exception {
        // Combination: pString=" ", charset="a"
        try {
            (new QuotedPrintableCodec()).encode(" ", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_146() throws Exception {
        // Combination: pString="a", charset="a"
        try {
            (new QuotedPrintableCodec()).encode("a", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_147() throws Exception {
        // Combination: pString="test123", charset="a"
        try {
            (new QuotedPrintableCodec()).encode("test123", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_148() throws Exception {
        // Combination: pString="!@#", charset="a"
        try {
            (new QuotedPrintableCodec()).encode("!@#", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_149() throws Exception {
        // Combination: pString="0", charset="a"
        try {
            (new QuotedPrintableCodec()).encode("0", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_150() throws Exception {
        // Combination: pString="-1", charset="a"
        try {
            (new QuotedPrintableCodec()).encode("-1", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_151() throws Exception {
        // Combination: pString="1.5", charset="a"
        try {
            (new QuotedPrintableCodec()).encode("1.5", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_152() throws Exception {
        // Combination: pString="9223372036854775807", charset="a"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775807", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_153() throws Exception {
        // Combination: pString="9223372036854775808", charset="a"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775808", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_154() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="a"
        try {
            (new QuotedPrintableCodec()).encode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_155() throws Exception {
        // Combination: pString="", charset="test123"
        try {
            (new QuotedPrintableCodec()).encode("", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_156() throws Exception {
        // Combination: pString=" ", charset="test123"
        try {
            (new QuotedPrintableCodec()).encode(" ", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_157() throws Exception {
        // Combination: pString="a", charset="test123"
        try {
            (new QuotedPrintableCodec()).encode("a", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_158() throws Exception {
        // Combination: pString="test123", charset="test123"
        try {
            (new QuotedPrintableCodec()).encode("test123", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_159() throws Exception {
        // Combination: pString="!@#", charset="test123"
        try {
            (new QuotedPrintableCodec()).encode("!@#", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_160() throws Exception {
        // Combination: pString="0", charset="test123"
        try {
            (new QuotedPrintableCodec()).encode("0", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_161() throws Exception {
        // Combination: pString="-1", charset="test123"
        try {
            (new QuotedPrintableCodec()).encode("-1", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_162() throws Exception {
        // Combination: pString="1.5", charset="test123"
        try {
            (new QuotedPrintableCodec()).encode("1.5", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_163() throws Exception {
        // Combination: pString="9223372036854775807", charset="test123"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775807", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_164() throws Exception {
        // Combination: pString="9223372036854775808", charset="test123"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775808", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_165() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="test123"
        try {
            (new QuotedPrintableCodec()).encode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_166() throws Exception {
        // Combination: pString="", charset="!@#"
        try {
            (new QuotedPrintableCodec()).encode("", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_167() throws Exception {
        // Combination: pString=" ", charset="!@#"
        try {
            (new QuotedPrintableCodec()).encode(" ", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_168() throws Exception {
        // Combination: pString="a", charset="!@#"
        try {
            (new QuotedPrintableCodec()).encode("a", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_169() throws Exception {
        // Combination: pString="test123", charset="!@#"
        try {
            (new QuotedPrintableCodec()).encode("test123", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_170() throws Exception {
        // Combination: pString="!@#", charset="!@#"
        try {
            (new QuotedPrintableCodec()).encode("!@#", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_171() throws Exception {
        // Combination: pString="0", charset="!@#"
        try {
            (new QuotedPrintableCodec()).encode("0", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_172() throws Exception {
        // Combination: pString="-1", charset="!@#"
        try {
            (new QuotedPrintableCodec()).encode("-1", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_173() throws Exception {
        // Combination: pString="1.5", charset="!@#"
        try {
            (new QuotedPrintableCodec()).encode("1.5", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_174() throws Exception {
        // Combination: pString="9223372036854775807", charset="!@#"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775807", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_175() throws Exception {
        // Combination: pString="9223372036854775808", charset="!@#"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775808", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_176() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="!@#"
        try {
            (new QuotedPrintableCodec()).encode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_177() throws Exception {
        // Combination: pString="", charset="0"
        try {
            (new QuotedPrintableCodec()).encode("", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_178() throws Exception {
        // Combination: pString=" ", charset="0"
        try {
            (new QuotedPrintableCodec()).encode(" ", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_179() throws Exception {
        // Combination: pString="a", charset="0"
        try {
            (new QuotedPrintableCodec()).encode("a", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_180() throws Exception {
        // Combination: pString="test123", charset="0"
        try {
            (new QuotedPrintableCodec()).encode("test123", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_181() throws Exception {
        // Combination: pString="!@#", charset="0"
        try {
            (new QuotedPrintableCodec()).encode("!@#", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_182() throws Exception {
        // Combination: pString="0", charset="0"
        try {
            (new QuotedPrintableCodec()).encode("0", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_183() throws Exception {
        // Combination: pString="-1", charset="0"
        try {
            (new QuotedPrintableCodec()).encode("-1", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_184() throws Exception {
        // Combination: pString="1.5", charset="0"
        try {
            (new QuotedPrintableCodec()).encode("1.5", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_185() throws Exception {
        // Combination: pString="9223372036854775807", charset="0"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775807", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_186() throws Exception {
        // Combination: pString="9223372036854775808", charset="0"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775808", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_187() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="0"
        try {
            (new QuotedPrintableCodec()).encode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_188() throws Exception {
        // Combination: pString="", charset="-1"
        try {
            (new QuotedPrintableCodec()).encode("", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_189() throws Exception {
        // Combination: pString=" ", charset="-1"
        try {
            (new QuotedPrintableCodec()).encode(" ", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_190() throws Exception {
        // Combination: pString="a", charset="-1"
        try {
            (new QuotedPrintableCodec()).encode("a", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_191() throws Exception {
        // Combination: pString="test123", charset="-1"
        try {
            (new QuotedPrintableCodec()).encode("test123", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_192() throws Exception {
        // Combination: pString="!@#", charset="-1"
        try {
            (new QuotedPrintableCodec()).encode("!@#", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_193() throws Exception {
        // Combination: pString="0", charset="-1"
        try {
            (new QuotedPrintableCodec()).encode("0", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_194() throws Exception {
        // Combination: pString="-1", charset="-1"
        try {
            (new QuotedPrintableCodec()).encode("-1", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_195() throws Exception {
        // Combination: pString="1.5", charset="-1"
        try {
            (new QuotedPrintableCodec()).encode("1.5", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_196() throws Exception {
        // Combination: pString="9223372036854775807", charset="-1"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775807", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_197() throws Exception {
        // Combination: pString="9223372036854775808", charset="-1"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775808", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_198() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="-1"
        try {
            (new QuotedPrintableCodec()).encode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_199() throws Exception {
        // Combination: pString="", charset="1.5"
        try {
            (new QuotedPrintableCodec()).encode("", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_200() throws Exception {
        // Combination: pString=" ", charset="1.5"
        try {
            (new QuotedPrintableCodec()).encode(" ", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_201() throws Exception {
        // Combination: pString="a", charset="1.5"
        try {
            (new QuotedPrintableCodec()).encode("a", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_202() throws Exception {
        // Combination: pString="test123", charset="1.5"
        try {
            (new QuotedPrintableCodec()).encode("test123", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_203() throws Exception {
        // Combination: pString="!@#", charset="1.5"
        try {
            (new QuotedPrintableCodec()).encode("!@#", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_204() throws Exception {
        // Combination: pString="0", charset="1.5"
        try {
            (new QuotedPrintableCodec()).encode("0", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_205() throws Exception {
        // Combination: pString="-1", charset="1.5"
        try {
            (new QuotedPrintableCodec()).encode("-1", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_206() throws Exception {
        // Combination: pString="1.5", charset="1.5"
        try {
            (new QuotedPrintableCodec()).encode("1.5", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_207() throws Exception {
        // Combination: pString="9223372036854775807", charset="1.5"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775807", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_208() throws Exception {
        // Combination: pString="9223372036854775808", charset="1.5"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775808", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_209() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="1.5"
        try {
            (new QuotedPrintableCodec()).encode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_210() throws Exception {
        // Combination: pString="", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).encode("", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_211() throws Exception {
        // Combination: pString=" ", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).encode(" ", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_212() throws Exception {
        // Combination: pString="a", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).encode("a", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_213() throws Exception {
        // Combination: pString="test123", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).encode("test123", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_214() throws Exception {
        // Combination: pString="!@#", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).encode("!@#", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_215() throws Exception {
        // Combination: pString="0", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).encode("0", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_216() throws Exception {
        // Combination: pString="-1", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).encode("-1", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_217() throws Exception {
        // Combination: pString="1.5", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).encode("1.5", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_218() throws Exception {
        // Combination: pString="9223372036854775807", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775807", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_219() throws Exception {
        // Combination: pString="9223372036854775808", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775808", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_220() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="9223372036854775807"
        try {
            (new QuotedPrintableCodec()).encode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_221() throws Exception {
        // Combination: pString="", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).encode("", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_222() throws Exception {
        // Combination: pString=" ", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).encode(" ", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_223() throws Exception {
        // Combination: pString="a", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).encode("a", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_224() throws Exception {
        // Combination: pString="test123", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).encode("test123", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_225() throws Exception {
        // Combination: pString="!@#", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).encode("!@#", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_226() throws Exception {
        // Combination: pString="0", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).encode("0", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_227() throws Exception {
        // Combination: pString="-1", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).encode("-1", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_228() throws Exception {
        // Combination: pString="1.5", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).encode("1.5", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_229() throws Exception {
        // Combination: pString="9223372036854775807", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775807", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_230() throws Exception {
        // Combination: pString="9223372036854775808", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775808", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_231() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="9223372036854775808"
        try {
            (new QuotedPrintableCodec()).encode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_232() throws Exception {
        // Combination: pString="", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).encode("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_233() throws Exception {
        // Combination: pString=" ", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).encode(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_234() throws Exception {
        // Combination: pString="a", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).encode("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_235() throws Exception {
        // Combination: pString="test123", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).encode("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_236() throws Exception {
        // Combination: pString="!@#", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).encode("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_237() throws Exception {
        // Combination: pString="0", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).encode("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_238() throws Exception {
        // Combination: pString="-1", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).encode("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_239() throws Exception {
        // Combination: pString="1.5", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).encode("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_240() throws Exception {
        // Combination: pString="9223372036854775807", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_241() throws Exception {
        // Combination: pString="9223372036854775808", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).encode("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encode_pairwise_242() throws Exception {
        // Combination: pString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charset="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new QuotedPrintableCodec()).encode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
