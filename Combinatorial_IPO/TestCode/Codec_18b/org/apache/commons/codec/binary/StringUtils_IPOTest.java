package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for StringUtils.
 */
public class StringUtils_IPOTest {
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
    public void test_equals_pairwise_001() throws Exception {
        // Combination: cs1="", cs2=""
        Object actual = StringUtils.equals("", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_002() throws Exception {
        // Combination: cs1="", cs2="a"
        Object actual = StringUtils.equals("", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_003() throws Exception {
        // Combination: cs1="", cs2="test"
        Object actual = StringUtils.equals("", "test");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_004() throws Exception {
        // Combination: cs1="a", cs2=""
        Object actual = StringUtils.equals("a", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_005() throws Exception {
        // Combination: cs1="a", cs2="a"
        Object actual = StringUtils.equals("a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_006() throws Exception {
        // Combination: cs1="a", cs2="test"
        Object actual = StringUtils.equals("a", "test");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_007() throws Exception {
        // Combination: cs1="test", cs2=""
        Object actual = StringUtils.equals("test", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_008() throws Exception {
        // Combination: cs1="test", cs2="a"
        Object actual = StringUtils.equals("test", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_009() throws Exception {
        // Combination: cs1="test", cs2="test"
        Object actual = StringUtils.equals("test", "test");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_010() throws Exception {
        // Combination: string="", charsetName=""
        try {
            StringUtils.getBytesUnchecked("", "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_011() throws Exception {
        // Combination: string=" ", charsetName=""
        try {
            StringUtils.getBytesUnchecked(" ", "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_012() throws Exception {
        // Combination: string="a", charsetName=""
        try {
            StringUtils.getBytesUnchecked("a", "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_013() throws Exception {
        // Combination: string="test123", charsetName=""
        try {
            StringUtils.getBytesUnchecked("test123", "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_014() throws Exception {
        // Combination: string="!@#", charsetName=""
        try {
            StringUtils.getBytesUnchecked("!@#", "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_015() throws Exception {
        // Combination: string="0", charsetName=""
        try {
            StringUtils.getBytesUnchecked("0", "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_016() throws Exception {
        // Combination: string="-1", charsetName=""
        try {
            StringUtils.getBytesUnchecked("-1", "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_017() throws Exception {
        // Combination: string="1.5", charsetName=""
        try {
            StringUtils.getBytesUnchecked("1.5", "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_018() throws Exception {
        // Combination: string="9223372036854775807", charsetName=""
        try {
            StringUtils.getBytesUnchecked("9223372036854775807", "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_019() throws Exception {
        // Combination: string="9223372036854775808", charsetName=""
        try {
            StringUtils.getBytesUnchecked("9223372036854775808", "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_020() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charsetName=""
        try {
            StringUtils.getBytesUnchecked("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_021() throws Exception {
        // Combination: string="", charsetName=" "
        try {
            StringUtils.getBytesUnchecked("", " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_022() throws Exception {
        // Combination: string=" ", charsetName=" "
        try {
            StringUtils.getBytesUnchecked(" ", " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_023() throws Exception {
        // Combination: string="a", charsetName=" "
        try {
            StringUtils.getBytesUnchecked("a", " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_024() throws Exception {
        // Combination: string="test123", charsetName=" "
        try {
            StringUtils.getBytesUnchecked("test123", " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_025() throws Exception {
        // Combination: string="!@#", charsetName=" "
        try {
            StringUtils.getBytesUnchecked("!@#", " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_026() throws Exception {
        // Combination: string="0", charsetName=" "
        try {
            StringUtils.getBytesUnchecked("0", " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_027() throws Exception {
        // Combination: string="-1", charsetName=" "
        try {
            StringUtils.getBytesUnchecked("-1", " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_028() throws Exception {
        // Combination: string="1.5", charsetName=" "
        try {
            StringUtils.getBytesUnchecked("1.5", " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_029() throws Exception {
        // Combination: string="9223372036854775807", charsetName=" "
        try {
            StringUtils.getBytesUnchecked("9223372036854775807", " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_030() throws Exception {
        // Combination: string="9223372036854775808", charsetName=" "
        try {
            StringUtils.getBytesUnchecked("9223372036854775808", " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_031() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charsetName=" "
        try {
            StringUtils.getBytesUnchecked("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_032() throws Exception {
        // Combination: string="", charsetName="a"
        try {
            StringUtils.getBytesUnchecked("", "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_033() throws Exception {
        // Combination: string=" ", charsetName="a"
        try {
            StringUtils.getBytesUnchecked(" ", "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_034() throws Exception {
        // Combination: string="a", charsetName="a"
        try {
            StringUtils.getBytesUnchecked("a", "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_035() throws Exception {
        // Combination: string="test123", charsetName="a"
        try {
            StringUtils.getBytesUnchecked("test123", "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_036() throws Exception {
        // Combination: string="!@#", charsetName="a"
        try {
            StringUtils.getBytesUnchecked("!@#", "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_037() throws Exception {
        // Combination: string="0", charsetName="a"
        try {
            StringUtils.getBytesUnchecked("0", "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_038() throws Exception {
        // Combination: string="-1", charsetName="a"
        try {
            StringUtils.getBytesUnchecked("-1", "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_039() throws Exception {
        // Combination: string="1.5", charsetName="a"
        try {
            StringUtils.getBytesUnchecked("1.5", "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_040() throws Exception {
        // Combination: string="9223372036854775807", charsetName="a"
        try {
            StringUtils.getBytesUnchecked("9223372036854775807", "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_041() throws Exception {
        // Combination: string="9223372036854775808", charsetName="a"
        try {
            StringUtils.getBytesUnchecked("9223372036854775808", "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_042() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charsetName="a"
        try {
            StringUtils.getBytesUnchecked("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_043() throws Exception {
        // Combination: string="", charsetName="test123"
        try {
            StringUtils.getBytesUnchecked("", "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_044() throws Exception {
        // Combination: string=" ", charsetName="test123"
        try {
            StringUtils.getBytesUnchecked(" ", "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_045() throws Exception {
        // Combination: string="a", charsetName="test123"
        try {
            StringUtils.getBytesUnchecked("a", "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_046() throws Exception {
        // Combination: string="test123", charsetName="test123"
        try {
            StringUtils.getBytesUnchecked("test123", "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_047() throws Exception {
        // Combination: string="!@#", charsetName="test123"
        try {
            StringUtils.getBytesUnchecked("!@#", "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_048() throws Exception {
        // Combination: string="0", charsetName="test123"
        try {
            StringUtils.getBytesUnchecked("0", "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_049() throws Exception {
        // Combination: string="-1", charsetName="test123"
        try {
            StringUtils.getBytesUnchecked("-1", "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_050() throws Exception {
        // Combination: string="1.5", charsetName="test123"
        try {
            StringUtils.getBytesUnchecked("1.5", "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_051() throws Exception {
        // Combination: string="9223372036854775807", charsetName="test123"
        try {
            StringUtils.getBytesUnchecked("9223372036854775807", "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_052() throws Exception {
        // Combination: string="9223372036854775808", charsetName="test123"
        try {
            StringUtils.getBytesUnchecked("9223372036854775808", "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_053() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charsetName="test123"
        try {
            StringUtils.getBytesUnchecked("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_054() throws Exception {
        // Combination: string="", charsetName="!@#"
        try {
            StringUtils.getBytesUnchecked("", "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_055() throws Exception {
        // Combination: string=" ", charsetName="!@#"
        try {
            StringUtils.getBytesUnchecked(" ", "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_056() throws Exception {
        // Combination: string="a", charsetName="!@#"
        try {
            StringUtils.getBytesUnchecked("a", "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_057() throws Exception {
        // Combination: string="test123", charsetName="!@#"
        try {
            StringUtils.getBytesUnchecked("test123", "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_058() throws Exception {
        // Combination: string="!@#", charsetName="!@#"
        try {
            StringUtils.getBytesUnchecked("!@#", "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_059() throws Exception {
        // Combination: string="0", charsetName="!@#"
        try {
            StringUtils.getBytesUnchecked("0", "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_060() throws Exception {
        // Combination: string="-1", charsetName="!@#"
        try {
            StringUtils.getBytesUnchecked("-1", "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_061() throws Exception {
        // Combination: string="1.5", charsetName="!@#"
        try {
            StringUtils.getBytesUnchecked("1.5", "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_062() throws Exception {
        // Combination: string="9223372036854775807", charsetName="!@#"
        try {
            StringUtils.getBytesUnchecked("9223372036854775807", "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_063() throws Exception {
        // Combination: string="9223372036854775808", charsetName="!@#"
        try {
            StringUtils.getBytesUnchecked("9223372036854775808", "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_064() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charsetName="!@#"
        try {
            StringUtils.getBytesUnchecked("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_065() throws Exception {
        // Combination: string="", charsetName="0"
        try {
            StringUtils.getBytesUnchecked("", "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_066() throws Exception {
        // Combination: string=" ", charsetName="0"
        try {
            StringUtils.getBytesUnchecked(" ", "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_067() throws Exception {
        // Combination: string="a", charsetName="0"
        try {
            StringUtils.getBytesUnchecked("a", "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_068() throws Exception {
        // Combination: string="test123", charsetName="0"
        try {
            StringUtils.getBytesUnchecked("test123", "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_069() throws Exception {
        // Combination: string="!@#", charsetName="0"
        try {
            StringUtils.getBytesUnchecked("!@#", "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_070() throws Exception {
        // Combination: string="0", charsetName="0"
        try {
            StringUtils.getBytesUnchecked("0", "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_071() throws Exception {
        // Combination: string="-1", charsetName="0"
        try {
            StringUtils.getBytesUnchecked("-1", "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_072() throws Exception {
        // Combination: string="1.5", charsetName="0"
        try {
            StringUtils.getBytesUnchecked("1.5", "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_073() throws Exception {
        // Combination: string="9223372036854775807", charsetName="0"
        try {
            StringUtils.getBytesUnchecked("9223372036854775807", "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_074() throws Exception {
        // Combination: string="9223372036854775808", charsetName="0"
        try {
            StringUtils.getBytesUnchecked("9223372036854775808", "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_075() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charsetName="0"
        try {
            StringUtils.getBytesUnchecked("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_076() throws Exception {
        // Combination: string="", charsetName="-1"
        try {
            StringUtils.getBytesUnchecked("", "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_077() throws Exception {
        // Combination: string=" ", charsetName="-1"
        try {
            StringUtils.getBytesUnchecked(" ", "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_078() throws Exception {
        // Combination: string="a", charsetName="-1"
        try {
            StringUtils.getBytesUnchecked("a", "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_079() throws Exception {
        // Combination: string="test123", charsetName="-1"
        try {
            StringUtils.getBytesUnchecked("test123", "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_080() throws Exception {
        // Combination: string="!@#", charsetName="-1"
        try {
            StringUtils.getBytesUnchecked("!@#", "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_081() throws Exception {
        // Combination: string="0", charsetName="-1"
        try {
            StringUtils.getBytesUnchecked("0", "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_082() throws Exception {
        // Combination: string="-1", charsetName="-1"
        try {
            StringUtils.getBytesUnchecked("-1", "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_083() throws Exception {
        // Combination: string="1.5", charsetName="-1"
        try {
            StringUtils.getBytesUnchecked("1.5", "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_084() throws Exception {
        // Combination: string="9223372036854775807", charsetName="-1"
        try {
            StringUtils.getBytesUnchecked("9223372036854775807", "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_085() throws Exception {
        // Combination: string="9223372036854775808", charsetName="-1"
        try {
            StringUtils.getBytesUnchecked("9223372036854775808", "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_086() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charsetName="-1"
        try {
            StringUtils.getBytesUnchecked("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_087() throws Exception {
        // Combination: string="", charsetName="1.5"
        try {
            StringUtils.getBytesUnchecked("", "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_088() throws Exception {
        // Combination: string=" ", charsetName="1.5"
        try {
            StringUtils.getBytesUnchecked(" ", "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_089() throws Exception {
        // Combination: string="a", charsetName="1.5"
        try {
            StringUtils.getBytesUnchecked("a", "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_090() throws Exception {
        // Combination: string="test123", charsetName="1.5"
        try {
            StringUtils.getBytesUnchecked("test123", "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_091() throws Exception {
        // Combination: string="!@#", charsetName="1.5"
        try {
            StringUtils.getBytesUnchecked("!@#", "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_092() throws Exception {
        // Combination: string="0", charsetName="1.5"
        try {
            StringUtils.getBytesUnchecked("0", "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_093() throws Exception {
        // Combination: string="-1", charsetName="1.5"
        try {
            StringUtils.getBytesUnchecked("-1", "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_094() throws Exception {
        // Combination: string="1.5", charsetName="1.5"
        try {
            StringUtils.getBytesUnchecked("1.5", "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_095() throws Exception {
        // Combination: string="9223372036854775807", charsetName="1.5"
        try {
            StringUtils.getBytesUnchecked("9223372036854775807", "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_096() throws Exception {
        // Combination: string="9223372036854775808", charsetName="1.5"
        try {
            StringUtils.getBytesUnchecked("9223372036854775808", "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_097() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charsetName="1.5"
        try {
            StringUtils.getBytesUnchecked("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_098() throws Exception {
        // Combination: string="", charsetName="9223372036854775807"
        try {
            StringUtils.getBytesUnchecked("", "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_099() throws Exception {
        // Combination: string=" ", charsetName="9223372036854775807"
        try {
            StringUtils.getBytesUnchecked(" ", "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_100() throws Exception {
        // Combination: string="a", charsetName="9223372036854775807"
        try {
            StringUtils.getBytesUnchecked("a", "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_101() throws Exception {
        // Combination: string="test123", charsetName="9223372036854775807"
        try {
            StringUtils.getBytesUnchecked("test123", "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_102() throws Exception {
        // Combination: string="!@#", charsetName="9223372036854775807"
        try {
            StringUtils.getBytesUnchecked("!@#", "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_103() throws Exception {
        // Combination: string="0", charsetName="9223372036854775807"
        try {
            StringUtils.getBytesUnchecked("0", "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_104() throws Exception {
        // Combination: string="-1", charsetName="9223372036854775807"
        try {
            StringUtils.getBytesUnchecked("-1", "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_105() throws Exception {
        // Combination: string="1.5", charsetName="9223372036854775807"
        try {
            StringUtils.getBytesUnchecked("1.5", "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_106() throws Exception {
        // Combination: string="9223372036854775807", charsetName="9223372036854775807"
        try {
            StringUtils.getBytesUnchecked("9223372036854775807", "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_107() throws Exception {
        // Combination: string="9223372036854775808", charsetName="9223372036854775807"
        try {
            StringUtils.getBytesUnchecked("9223372036854775808", "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_108() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charsetName="9223372036854775807"
        try {
            StringUtils.getBytesUnchecked("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_109() throws Exception {
        // Combination: string="", charsetName="9223372036854775808"
        try {
            StringUtils.getBytesUnchecked("", "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_110() throws Exception {
        // Combination: string=" ", charsetName="9223372036854775808"
        try {
            StringUtils.getBytesUnchecked(" ", "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_111() throws Exception {
        // Combination: string="a", charsetName="9223372036854775808"
        try {
            StringUtils.getBytesUnchecked("a", "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_112() throws Exception {
        // Combination: string="test123", charsetName="9223372036854775808"
        try {
            StringUtils.getBytesUnchecked("test123", "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_113() throws Exception {
        // Combination: string="!@#", charsetName="9223372036854775808"
        try {
            StringUtils.getBytesUnchecked("!@#", "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_114() throws Exception {
        // Combination: string="0", charsetName="9223372036854775808"
        try {
            StringUtils.getBytesUnchecked("0", "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_115() throws Exception {
        // Combination: string="-1", charsetName="9223372036854775808"
        try {
            StringUtils.getBytesUnchecked("-1", "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_116() throws Exception {
        // Combination: string="1.5", charsetName="9223372036854775808"
        try {
            StringUtils.getBytesUnchecked("1.5", "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_117() throws Exception {
        // Combination: string="9223372036854775807", charsetName="9223372036854775808"
        try {
            StringUtils.getBytesUnchecked("9223372036854775807", "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_118() throws Exception {
        // Combination: string="9223372036854775808", charsetName="9223372036854775808"
        try {
            StringUtils.getBytesUnchecked("9223372036854775808", "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_119() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charsetName="9223372036854775808"
        try {
            StringUtils.getBytesUnchecked("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_120() throws Exception {
        // Combination: string="", charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.getBytesUnchecked("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_121() throws Exception {
        // Combination: string=" ", charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.getBytesUnchecked(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_122() throws Exception {
        // Combination: string="a", charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.getBytesUnchecked("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_123() throws Exception {
        // Combination: string="test123", charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.getBytesUnchecked("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_124() throws Exception {
        // Combination: string="!@#", charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.getBytesUnchecked("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_125() throws Exception {
        // Combination: string="0", charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.getBytesUnchecked("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_126() throws Exception {
        // Combination: string="-1", charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.getBytesUnchecked("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_127() throws Exception {
        // Combination: string="1.5", charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.getBytesUnchecked("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_128() throws Exception {
        // Combination: string="9223372036854775807", charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.getBytesUnchecked("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_129() throws Exception {
        // Combination: string="9223372036854775808", charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.getBytesUnchecked("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBytesUnchecked_pairwise_130() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.getBytesUnchecked("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_131() throws Exception {
        // Combination: bytes=new byte[] {}, charsetName=""
        try {
            StringUtils.newString(new byte[] {}, "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_132() throws Exception {
        // Combination: bytes=new byte[] {}, charsetName=" "
        try {
            StringUtils.newString(new byte[] {}, " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_133() throws Exception {
        // Combination: bytes=new byte[] {}, charsetName="a"
        try {
            StringUtils.newString(new byte[] {}, "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_134() throws Exception {
        // Combination: bytes=new byte[] {}, charsetName="test123"
        try {
            StringUtils.newString(new byte[] {}, "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_135() throws Exception {
        // Combination: bytes=new byte[] {}, charsetName="!@#"
        try {
            StringUtils.newString(new byte[] {}, "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_136() throws Exception {
        // Combination: bytes=new byte[] {}, charsetName="0"
        try {
            StringUtils.newString(new byte[] {}, "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_137() throws Exception {
        // Combination: bytes=new byte[] {}, charsetName="-1"
        try {
            StringUtils.newString(new byte[] {}, "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_138() throws Exception {
        // Combination: bytes=new byte[] {}, charsetName="1.5"
        try {
            StringUtils.newString(new byte[] {}, "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_139() throws Exception {
        // Combination: bytes=new byte[] {}, charsetName="9223372036854775807"
        try {
            StringUtils.newString(new byte[] {}, "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_140() throws Exception {
        // Combination: bytes=new byte[] {}, charsetName="9223372036854775808"
        try {
            StringUtils.newString(new byte[] {}, "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_141() throws Exception {
        // Combination: bytes=new byte[] {}, charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.newString(new byte[] {}, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_142() throws Exception {
        // Combination: bytes=new byte[] {1}, charsetName=""
        try {
            StringUtils.newString(new byte[] {1}, "");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_143() throws Exception {
        // Combination: bytes=new byte[] {1}, charsetName=" "
        try {
            StringUtils.newString(new byte[] {1}, " ");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_144() throws Exception {
        // Combination: bytes=new byte[] {1}, charsetName="a"
        try {
            StringUtils.newString(new byte[] {1}, "a");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_145() throws Exception {
        // Combination: bytes=new byte[] {1}, charsetName="test123"
        try {
            StringUtils.newString(new byte[] {1}, "test123");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_146() throws Exception {
        // Combination: bytes=new byte[] {1}, charsetName="!@#"
        try {
            StringUtils.newString(new byte[] {1}, "!@#");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_147() throws Exception {
        // Combination: bytes=new byte[] {1}, charsetName="0"
        try {
            StringUtils.newString(new byte[] {1}, "0");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_148() throws Exception {
        // Combination: bytes=new byte[] {1}, charsetName="-1"
        try {
            StringUtils.newString(new byte[] {1}, "-1");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_149() throws Exception {
        // Combination: bytes=new byte[] {1}, charsetName="1.5"
        try {
            StringUtils.newString(new byte[] {1}, "1.5");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_150() throws Exception {
        // Combination: bytes=new byte[] {1}, charsetName="9223372036854775807"
        try {
            StringUtils.newString(new byte[] {1}, "9223372036854775807");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_151() throws Exception {
        // Combination: bytes=new byte[] {1}, charsetName="9223372036854775808"
        try {
            StringUtils.newString(new byte[] {1}, "9223372036854775808");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_newString_pairwise_152() throws Exception {
        // Combination: bytes=new byte[] {1}, charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            StringUtils.newString(new byte[] {1}, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalStateException");
        } catch (java.lang.IllegalStateException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
