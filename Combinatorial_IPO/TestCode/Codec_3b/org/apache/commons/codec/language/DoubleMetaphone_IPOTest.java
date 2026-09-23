package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for DoubleMetaphone.
 */
public class DoubleMetaphone_IPOTest {
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
    public void test_doubleMetaphone_pairwise_001() throws Exception {
        // Combination: value="", alternate=true
        assertNull((new DoubleMetaphone()).doubleMetaphone("", true));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_002() throws Exception {
        // Combination: value=" ", alternate=true
        assertNull((new DoubleMetaphone()).doubleMetaphone(" ", true));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_003() throws Exception {
        // Combination: value="a", alternate=true
        Object actual = (new DoubleMetaphone()).doubleMetaphone("a", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("A", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_004() throws Exception {
        // Combination: value="test123", alternate=true
        Object actual = (new DoubleMetaphone()).doubleMetaphone("test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("TST", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_005() throws Exception {
        // Combination: value="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).doubleMetaphone("!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_006() throws Exception {
        // Combination: value="0", alternate=true
        Object actual = (new DoubleMetaphone()).doubleMetaphone("0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_007() throws Exception {
        // Combination: value="-1", alternate=true
        Object actual = (new DoubleMetaphone()).doubleMetaphone("-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_008() throws Exception {
        // Combination: value="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).doubleMetaphone("1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_009() throws Exception {
        // Combination: value="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).doubleMetaphone("9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_010() throws Exception {
        // Combination: value="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).doubleMetaphone("9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_011() throws Exception {
        // Combination: value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).doubleMetaphone("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("A", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_012() throws Exception {
        // Combination: value="", alternate=false
        assertNull((new DoubleMetaphone()).doubleMetaphone("", false));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_013() throws Exception {
        // Combination: value=" ", alternate=false
        assertNull((new DoubleMetaphone()).doubleMetaphone(" ", false));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_014() throws Exception {
        // Combination: value="a", alternate=false
        Object actual = (new DoubleMetaphone()).doubleMetaphone("a", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("A", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_015() throws Exception {
        // Combination: value="test123", alternate=false
        Object actual = (new DoubleMetaphone()).doubleMetaphone("test123", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("TST", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_016() throws Exception {
        // Combination: value="!@#", alternate=false
        Object actual = (new DoubleMetaphone()).doubleMetaphone("!@#", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_017() throws Exception {
        // Combination: value="0", alternate=false
        Object actual = (new DoubleMetaphone()).doubleMetaphone("0", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_018() throws Exception {
        // Combination: value="-1", alternate=false
        Object actual = (new DoubleMetaphone()).doubleMetaphone("-1", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_019() throws Exception {
        // Combination: value="1.5", alternate=false
        Object actual = (new DoubleMetaphone()).doubleMetaphone("1.5", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_020() throws Exception {
        // Combination: value="9223372036854775807", alternate=false
        Object actual = (new DoubleMetaphone()).doubleMetaphone("9223372036854775807", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_021() throws Exception {
        // Combination: value="9223372036854775808", alternate=false
        Object actual = (new DoubleMetaphone()).doubleMetaphone("9223372036854775808", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_doubleMetaphone_pairwise_022() throws Exception {
        // Combination: value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=false
        Object actual = (new DoubleMetaphone()).doubleMetaphone("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("A", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_023() throws Exception {
        // Combination: value1="", value2=""
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_024() throws Exception {
        // Combination: value1="", value2=" "
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", " ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_025() throws Exception {
        // Combination: value1="", value2="a"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_026() throws Exception {
        // Combination: value1="", value2="test123"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_027() throws Exception {
        // Combination: value1="", value2="!@#"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_028() throws Exception {
        // Combination: value1="", value2="0"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_029() throws Exception {
        // Combination: value1="", value2="-1"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_030() throws Exception {
        // Combination: value1="", value2="1.5"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_031() throws Exception {
        // Combination: value1="", value2="9223372036854775807"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_032() throws Exception {
        // Combination: value1="", value2="9223372036854775808"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_033() throws Exception {
        // Combination: value1="", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_034() throws Exception {
        // Combination: value1=" ", value2=""
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_035() throws Exception {
        // Combination: value1=" ", value2=" "
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", " ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_036() throws Exception {
        // Combination: value1=" ", value2="a"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_037() throws Exception {
        // Combination: value1=" ", value2="test123"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_038() throws Exception {
        // Combination: value1=" ", value2="!@#"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_039() throws Exception {
        // Combination: value1=" ", value2="0"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_040() throws Exception {
        // Combination: value1=" ", value2="-1"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_041() throws Exception {
        // Combination: value1=" ", value2="1.5"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_042() throws Exception {
        // Combination: value1=" ", value2="9223372036854775807"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_043() throws Exception {
        // Combination: value1=" ", value2="9223372036854775808"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_044() throws Exception {
        // Combination: value1=" ", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_045() throws Exception {
        // Combination: value1="a", value2=""
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_046() throws Exception {
        // Combination: value1="a", value2=" "
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_047() throws Exception {
        // Combination: value1="a", value2="a"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_048() throws Exception {
        // Combination: value1="a", value2="test123"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_049() throws Exception {
        // Combination: value1="a", value2="!@#"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_050() throws Exception {
        // Combination: value1="a", value2="0"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_051() throws Exception {
        // Combination: value1="a", value2="-1"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_052() throws Exception {
        // Combination: value1="a", value2="1.5"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_053() throws Exception {
        // Combination: value1="a", value2="9223372036854775807"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_054() throws Exception {
        // Combination: value1="a", value2="9223372036854775808"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_055() throws Exception {
        // Combination: value1="a", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_056() throws Exception {
        // Combination: value1="test123", value2=""
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_057() throws Exception {
        // Combination: value1="test123", value2=" "
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_058() throws Exception {
        // Combination: value1="test123", value2="a"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_059() throws Exception {
        // Combination: value1="test123", value2="test123"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_060() throws Exception {
        // Combination: value1="test123", value2="!@#"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_061() throws Exception {
        // Combination: value1="test123", value2="0"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_062() throws Exception {
        // Combination: value1="test123", value2="-1"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_063() throws Exception {
        // Combination: value1="test123", value2="1.5"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_064() throws Exception {
        // Combination: value1="test123", value2="9223372036854775807"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_065() throws Exception {
        // Combination: value1="test123", value2="9223372036854775808"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_066() throws Exception {
        // Combination: value1="test123", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_067() throws Exception {
        // Combination: value1="!@#", value2=""
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_068() throws Exception {
        // Combination: value1="!@#", value2=" "
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_069() throws Exception {
        // Combination: value1="!@#", value2="a"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_070() throws Exception {
        // Combination: value1="!@#", value2="test123"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_071() throws Exception {
        // Combination: value1="!@#", value2="!@#"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_072() throws Exception {
        // Combination: value1="!@#", value2="0"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_073() throws Exception {
        // Combination: value1="!@#", value2="-1"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_074() throws Exception {
        // Combination: value1="!@#", value2="1.5"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_075() throws Exception {
        // Combination: value1="!@#", value2="9223372036854775807"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_076() throws Exception {
        // Combination: value1="!@#", value2="9223372036854775808"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_077() throws Exception {
        // Combination: value1="!@#", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_078() throws Exception {
        // Combination: value1="0", value2=""
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_079() throws Exception {
        // Combination: value1="0", value2=" "
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_080() throws Exception {
        // Combination: value1="0", value2="a"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_081() throws Exception {
        // Combination: value1="0", value2="test123"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_082() throws Exception {
        // Combination: value1="0", value2="!@#"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_083() throws Exception {
        // Combination: value1="0", value2="0"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_084() throws Exception {
        // Combination: value1="0", value2="-1"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_085() throws Exception {
        // Combination: value1="0", value2="1.5"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_086() throws Exception {
        // Combination: value1="0", value2="9223372036854775807"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_087() throws Exception {
        // Combination: value1="0", value2="9223372036854775808"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_088() throws Exception {
        // Combination: value1="0", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_089() throws Exception {
        // Combination: value1="-1", value2=""
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_090() throws Exception {
        // Combination: value1="-1", value2=" "
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_091() throws Exception {
        // Combination: value1="-1", value2="a"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_092() throws Exception {
        // Combination: value1="-1", value2="test123"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_093() throws Exception {
        // Combination: value1="-1", value2="!@#"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_094() throws Exception {
        // Combination: value1="-1", value2="0"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_095() throws Exception {
        // Combination: value1="-1", value2="-1"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_096() throws Exception {
        // Combination: value1="-1", value2="1.5"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_097() throws Exception {
        // Combination: value1="-1", value2="9223372036854775807"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_098() throws Exception {
        // Combination: value1="-1", value2="9223372036854775808"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_099() throws Exception {
        // Combination: value1="-1", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_100() throws Exception {
        // Combination: value1="1.5", value2=""
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_101() throws Exception {
        // Combination: value1="1.5", value2=" "
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_102() throws Exception {
        // Combination: value1="1.5", value2="a"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_103() throws Exception {
        // Combination: value1="1.5", value2="test123"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_104() throws Exception {
        // Combination: value1="1.5", value2="!@#"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_105() throws Exception {
        // Combination: value1="1.5", value2="0"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_106() throws Exception {
        // Combination: value1="1.5", value2="-1"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_107() throws Exception {
        // Combination: value1="1.5", value2="1.5"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_108() throws Exception {
        // Combination: value1="1.5", value2="9223372036854775807"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_109() throws Exception {
        // Combination: value1="1.5", value2="9223372036854775808"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_110() throws Exception {
        // Combination: value1="1.5", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_111() throws Exception {
        // Combination: value1="9223372036854775807", value2=""
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_112() throws Exception {
        // Combination: value1="9223372036854775807", value2=" "
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_113() throws Exception {
        // Combination: value1="9223372036854775807", value2="a"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_114() throws Exception {
        // Combination: value1="9223372036854775807", value2="test123"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_115() throws Exception {
        // Combination: value1="9223372036854775807", value2="!@#"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_116() throws Exception {
        // Combination: value1="9223372036854775807", value2="0"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_117() throws Exception {
        // Combination: value1="9223372036854775807", value2="-1"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_118() throws Exception {
        // Combination: value1="9223372036854775807", value2="1.5"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_119() throws Exception {
        // Combination: value1="9223372036854775807", value2="9223372036854775807"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_120() throws Exception {
        // Combination: value1="9223372036854775807", value2="9223372036854775808"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_121() throws Exception {
        // Combination: value1="9223372036854775807", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_122() throws Exception {
        // Combination: value1="9223372036854775808", value2=""
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_123() throws Exception {
        // Combination: value1="9223372036854775808", value2=" "
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_124() throws Exception {
        // Combination: value1="9223372036854775808", value2="a"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_125() throws Exception {
        // Combination: value1="9223372036854775808", value2="test123"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_126() throws Exception {
        // Combination: value1="9223372036854775808", value2="!@#"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_127() throws Exception {
        // Combination: value1="9223372036854775808", value2="0"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_128() throws Exception {
        // Combination: value1="9223372036854775808", value2="-1"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_129() throws Exception {
        // Combination: value1="9223372036854775808", value2="1.5"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_130() throws Exception {
        // Combination: value1="9223372036854775808", value2="9223372036854775807"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_131() throws Exception {
        // Combination: value1="9223372036854775808", value2="9223372036854775808"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_132() throws Exception {
        // Combination: value1="9223372036854775808", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_133() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2=""
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_134() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2=" "
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_135() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="a"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_136() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="test123"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_137() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="!@#"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_138() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="0"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_139() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="-1"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_140() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="1.5"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_141() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="9223372036854775807"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_142() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="9223372036854775808"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_143() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_144() throws Exception {
        // Combination: value1="", value2="", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_145() throws Exception {
        // Combination: value1=" ", value2=" ", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", " ", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_146() throws Exception {
        // Combination: value1="a", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_147() throws Exception {
        // Combination: value1="test123", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_148() throws Exception {
        // Combination: value1="!@#", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_149() throws Exception {
        // Combination: value1="0", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_150() throws Exception {
        // Combination: value1="-1", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_151() throws Exception {
        // Combination: value1="1.5", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_152() throws Exception {
        // Combination: value1="9223372036854775807", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_153() throws Exception {
        // Combination: value1="9223372036854775808", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_154() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_155() throws Exception {
        // Combination: value1="", value2=" ", alternate=false
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", " ", false);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_156() throws Exception {
        // Combination: value1=" ", value2="", alternate=false
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "", false);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_157() throws Exception {
        // Combination: value1="a", value2="test123", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "test123", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_158() throws Exception {
        // Combination: value1="test123", value2="a", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "a", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_159() throws Exception {
        // Combination: value1="!@#", value2="0", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "0", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_160() throws Exception {
        // Combination: value1="0", value2="!@#", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "!@#", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_161() throws Exception {
        // Combination: value1="-1", value2="1.5", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "1.5", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_162() throws Exception {
        // Combination: value1="1.5", value2="-1", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "-1", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_163() throws Exception {
        // Combination: value1="9223372036854775807", value2="9223372036854775808", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "9223372036854775808", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_164() throws Exception {
        // Combination: value1="9223372036854775808", value2="9223372036854775807", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "9223372036854775807", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_165() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_166() throws Exception {
        // Combination: value1="a", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_167() throws Exception {
        // Combination: value1="test123", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_168() throws Exception {
        // Combination: value1="!@#", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_169() throws Exception {
        // Combination: value1="0", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_170() throws Exception {
        // Combination: value1="-1", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_171() throws Exception {
        // Combination: value1="1.5", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_172() throws Exception {
        // Combination: value1="9223372036854775807", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_173() throws Exception {
        // Combination: value1="9223372036854775808", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_174() throws Exception {
        // Combination: value1="a", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_175() throws Exception {
        // Combination: value1="test123", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_176() throws Exception {
        // Combination: value1="!@#", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_177() throws Exception {
        // Combination: value1="0", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_178() throws Exception {
        // Combination: value1="-1", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_179() throws Exception {
        // Combination: value1="1.5", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_180() throws Exception {
        // Combination: value1="9223372036854775807", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_181() throws Exception {
        // Combination: value1="9223372036854775808", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_182() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_183() throws Exception {
        // Combination: value1="", value2="a", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "a", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_184() throws Exception {
        // Combination: value1=" ", value2="a", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "a", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_185() throws Exception {
        // Combination: value1="!@#", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_186() throws Exception {
        // Combination: value1="0", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_187() throws Exception {
        // Combination: value1="-1", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_188() throws Exception {
        // Combination: value1="1.5", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_189() throws Exception {
        // Combination: value1="9223372036854775807", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_190() throws Exception {
        // Combination: value1="9223372036854775808", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_191() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_192() throws Exception {
        // Combination: value1="", value2="test123", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "test123", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_193() throws Exception {
        // Combination: value1=" ", value2="test123", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "test123", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_194() throws Exception {
        // Combination: value1="!@#", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_195() throws Exception {
        // Combination: value1="0", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_196() throws Exception {
        // Combination: value1="-1", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_197() throws Exception {
        // Combination: value1="1.5", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_198() throws Exception {
        // Combination: value1="9223372036854775807", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_199() throws Exception {
        // Combination: value1="9223372036854775808", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_200() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_201() throws Exception {
        // Combination: value1="", value2="!@#", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "!@#", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_202() throws Exception {
        // Combination: value1=" ", value2="!@#", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "!@#", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_203() throws Exception {
        // Combination: value1="a", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_204() throws Exception {
        // Combination: value1="test123", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_205() throws Exception {
        // Combination: value1="-1", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_206() throws Exception {
        // Combination: value1="1.5", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_207() throws Exception {
        // Combination: value1="9223372036854775807", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_208() throws Exception {
        // Combination: value1="9223372036854775808", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_209() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_210() throws Exception {
        // Combination: value1="", value2="0", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "0", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_211() throws Exception {
        // Combination: value1=" ", value2="0", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "0", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_212() throws Exception {
        // Combination: value1="a", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_213() throws Exception {
        // Combination: value1="test123", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_214() throws Exception {
        // Combination: value1="-1", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_215() throws Exception {
        // Combination: value1="1.5", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_216() throws Exception {
        // Combination: value1="9223372036854775807", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_217() throws Exception {
        // Combination: value1="9223372036854775808", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_218() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_219() throws Exception {
        // Combination: value1="", value2="-1", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "-1", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_220() throws Exception {
        // Combination: value1=" ", value2="-1", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "-1", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_221() throws Exception {
        // Combination: value1="a", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_222() throws Exception {
        // Combination: value1="test123", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_223() throws Exception {
        // Combination: value1="!@#", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_224() throws Exception {
        // Combination: value1="0", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_225() throws Exception {
        // Combination: value1="9223372036854775807", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_226() throws Exception {
        // Combination: value1="9223372036854775808", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_227() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_228() throws Exception {
        // Combination: value1="", value2="1.5", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "1.5", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_229() throws Exception {
        // Combination: value1=" ", value2="1.5", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "1.5", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_230() throws Exception {
        // Combination: value1="a", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_231() throws Exception {
        // Combination: value1="test123", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_232() throws Exception {
        // Combination: value1="!@#", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_233() throws Exception {
        // Combination: value1="0", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_234() throws Exception {
        // Combination: value1="9223372036854775807", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_235() throws Exception {
        // Combination: value1="9223372036854775808", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_236() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_237() throws Exception {
        // Combination: value1="", value2="9223372036854775807", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "9223372036854775807", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_238() throws Exception {
        // Combination: value1=" ", value2="9223372036854775807", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "9223372036854775807", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_239() throws Exception {
        // Combination: value1="a", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_240() throws Exception {
        // Combination: value1="test123", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_241() throws Exception {
        // Combination: value1="!@#", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_242() throws Exception {
        // Combination: value1="0", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_243() throws Exception {
        // Combination: value1="-1", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_244() throws Exception {
        // Combination: value1="1.5", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_245() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_246() throws Exception {
        // Combination: value1="", value2="9223372036854775808", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "9223372036854775808", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_247() throws Exception {
        // Combination: value1=" ", value2="9223372036854775808", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "9223372036854775808", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_248() throws Exception {
        // Combination: value1="a", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_249() throws Exception {
        // Combination: value1="test123", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_250() throws Exception {
        // Combination: value1="!@#", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_251() throws Exception {
        // Combination: value1="0", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_252() throws Exception {
        // Combination: value1="-1", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_253() throws Exception {
        // Combination: value1="1.5", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_254() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_255() throws Exception {
        // Combination: value1="", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=false
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_256() throws Exception {
        // Combination: value1=" ", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        try {
            (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_257() throws Exception {
        // Combination: value1="a", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_258() throws Exception {
        // Combination: value1="test123", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_259() throws Exception {
        // Combination: value1="!@#", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_260() throws Exception {
        // Combination: value1="0", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_261() throws Exception {
        // Combination: value1="-1", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_262() throws Exception {
        // Combination: value1="1.5", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_263() throws Exception {
        // Combination: value1="9223372036854775807", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_264() throws Exception {
        // Combination: value1="9223372036854775808", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_265() throws Exception {
        // Combination: value="", index=0
        Object actual = (new DoubleMetaphone()).charAt("", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_266() throws Exception {
        // Combination: value=" ", index=0
        Object actual = (new DoubleMetaphone()).charAt(" ", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_267() throws Exception {
        // Combination: value="a", index=0
        Object actual = (new DoubleMetaphone()).charAt("a", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_268() throws Exception {
        // Combination: value="test123", index=0
        Object actual = (new DoubleMetaphone()).charAt("test123", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("t", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_269() throws Exception {
        // Combination: value="!@#", index=0
        Object actual = (new DoubleMetaphone()).charAt("!@#", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("!", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_270() throws Exception {
        // Combination: value="0", index=0
        Object actual = (new DoubleMetaphone()).charAt("0", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_271() throws Exception {
        // Combination: value="-1", index=0
        Object actual = (new DoubleMetaphone()).charAt("-1", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("-", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_272() throws Exception {
        // Combination: value="1.5", index=0
        Object actual = (new DoubleMetaphone()).charAt("1.5", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_273() throws Exception {
        // Combination: value="9223372036854775807", index=0
        Object actual = (new DoubleMetaphone()).charAt("9223372036854775807", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_274() throws Exception {
        // Combination: value="9223372036854775808", index=0
        Object actual = (new DoubleMetaphone()).charAt("9223372036854775808", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_275() throws Exception {
        // Combination: value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=0
        Object actual = (new DoubleMetaphone()).charAt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_276() throws Exception {
        // Combination: value="", index=1
        Object actual = (new DoubleMetaphone()).charAt("", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_277() throws Exception {
        // Combination: value=" ", index=1
        Object actual = (new DoubleMetaphone()).charAt(" ", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_278() throws Exception {
        // Combination: value="a", index=1
        Object actual = (new DoubleMetaphone()).charAt("a", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_279() throws Exception {
        // Combination: value="test123", index=1
        Object actual = (new DoubleMetaphone()).charAt("test123", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("e", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_280() throws Exception {
        // Combination: value="!@#", index=1
        Object actual = (new DoubleMetaphone()).charAt("!@#", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("@", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_281() throws Exception {
        // Combination: value="0", index=1
        Object actual = (new DoubleMetaphone()).charAt("0", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_282() throws Exception {
        // Combination: value="-1", index=1
        Object actual = (new DoubleMetaphone()).charAt("-1", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_283() throws Exception {
        // Combination: value="1.5", index=1
        Object actual = (new DoubleMetaphone()).charAt("1.5", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals(".", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_284() throws Exception {
        // Combination: value="9223372036854775807", index=1
        Object actual = (new DoubleMetaphone()).charAt("9223372036854775807", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_285() throws Exception {
        // Combination: value="9223372036854775808", index=1
        Object actual = (new DoubleMetaphone()).charAt("9223372036854775808", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_286() throws Exception {
        // Combination: value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=1
        Object actual = (new DoubleMetaphone()).charAt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_287() throws Exception {
        // Combination: value="", index=-1
        Object actual = (new DoubleMetaphone()).charAt("", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_288() throws Exception {
        // Combination: value=" ", index=-1
        Object actual = (new DoubleMetaphone()).charAt(" ", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_289() throws Exception {
        // Combination: value="a", index=-1
        Object actual = (new DoubleMetaphone()).charAt("a", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_290() throws Exception {
        // Combination: value="test123", index=-1
        Object actual = (new DoubleMetaphone()).charAt("test123", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_291() throws Exception {
        // Combination: value="!@#", index=-1
        Object actual = (new DoubleMetaphone()).charAt("!@#", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_292() throws Exception {
        // Combination: value="0", index=-1
        Object actual = (new DoubleMetaphone()).charAt("0", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_293() throws Exception {
        // Combination: value="-1", index=-1
        Object actual = (new DoubleMetaphone()).charAt("-1", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_294() throws Exception {
        // Combination: value="1.5", index=-1
        Object actual = (new DoubleMetaphone()).charAt("1.5", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_295() throws Exception {
        // Combination: value="9223372036854775807", index=-1
        Object actual = (new DoubleMetaphone()).charAt("9223372036854775807", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_296() throws Exception {
        // Combination: value="9223372036854775808", index=-1
        Object actual = (new DoubleMetaphone()).charAt("9223372036854775808", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_297() throws Exception {
        // Combination: value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=-1
        Object actual = (new DoubleMetaphone()).charAt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_298() throws Exception {
        // Combination: value="", index=Integer.MAX_VALUE
        Object actual = (new DoubleMetaphone()).charAt("", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_299() throws Exception {
        // Combination: value=" ", index=Integer.MAX_VALUE
        Object actual = (new DoubleMetaphone()).charAt(" ", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_300() throws Exception {
        // Combination: value="a", index=Integer.MAX_VALUE
        Object actual = (new DoubleMetaphone()).charAt("a", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_301() throws Exception {
        // Combination: value="test123", index=Integer.MAX_VALUE
        Object actual = (new DoubleMetaphone()).charAt("test123", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_302() throws Exception {
        // Combination: value="!@#", index=Integer.MAX_VALUE
        Object actual = (new DoubleMetaphone()).charAt("!@#", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_303() throws Exception {
        // Combination: value="0", index=Integer.MAX_VALUE
        Object actual = (new DoubleMetaphone()).charAt("0", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_304() throws Exception {
        // Combination: value="-1", index=Integer.MAX_VALUE
        Object actual = (new DoubleMetaphone()).charAt("-1", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_305() throws Exception {
        // Combination: value="1.5", index=Integer.MAX_VALUE
        Object actual = (new DoubleMetaphone()).charAt("1.5", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_306() throws Exception {
        // Combination: value="9223372036854775807", index=Integer.MAX_VALUE
        Object actual = (new DoubleMetaphone()).charAt("9223372036854775807", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_307() throws Exception {
        // Combination: value="9223372036854775808", index=Integer.MAX_VALUE
        Object actual = (new DoubleMetaphone()).charAt("9223372036854775808", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_308() throws Exception {
        // Combination: value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=Integer.MAX_VALUE
        Object actual = (new DoubleMetaphone()).charAt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_309() throws Exception {
        // Combination: value="", index=Integer.MIN_VALUE
        Object actual = (new DoubleMetaphone()).charAt("", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_310() throws Exception {
        // Combination: value=" ", index=Integer.MIN_VALUE
        Object actual = (new DoubleMetaphone()).charAt(" ", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_311() throws Exception {
        // Combination: value="a", index=Integer.MIN_VALUE
        Object actual = (new DoubleMetaphone()).charAt("a", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_312() throws Exception {
        // Combination: value="test123", index=Integer.MIN_VALUE
        Object actual = (new DoubleMetaphone()).charAt("test123", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_313() throws Exception {
        // Combination: value="!@#", index=Integer.MIN_VALUE
        Object actual = (new DoubleMetaphone()).charAt("!@#", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_314() throws Exception {
        // Combination: value="0", index=Integer.MIN_VALUE
        Object actual = (new DoubleMetaphone()).charAt("0", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_315() throws Exception {
        // Combination: value="-1", index=Integer.MIN_VALUE
        Object actual = (new DoubleMetaphone()).charAt("-1", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_316() throws Exception {
        // Combination: value="1.5", index=Integer.MIN_VALUE
        Object actual = (new DoubleMetaphone()).charAt("1.5", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_317() throws Exception {
        // Combination: value="9223372036854775807", index=Integer.MIN_VALUE
        Object actual = (new DoubleMetaphone()).charAt("9223372036854775807", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_318() throws Exception {
        // Combination: value="9223372036854775808", index=Integer.MIN_VALUE
        Object actual = (new DoubleMetaphone()).charAt("9223372036854775808", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_charAt_pairwise_319() throws Exception {
        // Combination: value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=Integer.MIN_VALUE
        Object actual = (new DoubleMetaphone()).charAt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Character", actual.getClass().getName());
        assertEquals("\u0000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_320() throws Exception {
        // Combination: value="", start=0, length=0, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("", 0, 0, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_321() throws Exception {
        // Combination: value=" ", start=1, length=1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains(" ", 1, 1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_322() throws Exception {
        // Combination: value="a", start=-1, length=-1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("a", -1, -1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_323() throws Exception {
        // Combination: value="test123", start=Integer.MAX_VALUE, length=Integer.MAX_VALUE, criteria=new String[] {}
        try {
            DoubleMetaphone.contains("test123", Integer.MAX_VALUE, Integer.MAX_VALUE, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_324() throws Exception {
        // Combination: value="!@#", start=Integer.MIN_VALUE, length=Integer.MIN_VALUE, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("!@#", Integer.MIN_VALUE, Integer.MIN_VALUE, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_325() throws Exception {
        // Combination: value="a", start=1, length=0, criteria=new String[] {"value"}
        Object actual = DoubleMetaphone.contains("a", 1, 0, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_326() throws Exception {
        // Combination: value="test123", start=0, length=1, criteria=new String[] {"value"}
        Object actual = DoubleMetaphone.contains("test123", 0, 1, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_327() throws Exception {
        // Combination: value="", start=Integer.MAX_VALUE, length=-1, criteria=new String[] {"value"}
        Object actual = DoubleMetaphone.contains("", Integer.MAX_VALUE, -1, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_328() throws Exception {
        // Combination: value=" ", start=-1, length=Integer.MAX_VALUE, criteria=new String[] {"value"}
        Object actual = DoubleMetaphone.contains(" ", -1, Integer.MAX_VALUE, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_329() throws Exception {
        // Combination: value="0", start=0, length=Integer.MIN_VALUE, criteria=new String[] {"value"}
        try {
            DoubleMetaphone.contains("0", 0, Integer.MIN_VALUE, new String[] {"value"});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_330() throws Exception {
        // Combination: value="-1", start=0, length=-1, criteria=new String[] {}
        try {
            DoubleMetaphone.contains("-1", 0, -1, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_331() throws Exception {
        // Combination: value="1.5", start=0, length=Integer.MAX_VALUE, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("1.5", 0, Integer.MAX_VALUE, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_332() throws Exception {
        // Combination: value="0", start=1, length=-1, criteria=new String[] {}
        try {
            DoubleMetaphone.contains("0", 1, -1, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_333() throws Exception {
        // Combination: value="9223372036854775807", start=1, length=Integer.MAX_VALUE, criteria=new String[] {}
        try {
            DoubleMetaphone.contains("9223372036854775807", 1, Integer.MAX_VALUE, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_334() throws Exception {
        // Combination: value="9223372036854775808", start=1, length=Integer.MIN_VALUE, criteria=new String[] {}
        try {
            DoubleMetaphone.contains("9223372036854775808", 1, Integer.MIN_VALUE, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_335() throws Exception {
        // Combination: value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", start=-1, length=0, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, 0, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_336() throws Exception {
        // Combination: value="", start=-1, length=1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("", -1, 1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_337() throws Exception {
        // Combination: value="test123", start=-1, length=Integer.MIN_VALUE, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("test123", -1, Integer.MIN_VALUE, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_338() throws Exception {
        // Combination: value=" ", start=Integer.MAX_VALUE, length=0, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains(" ", Integer.MAX_VALUE, 0, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_339() throws Exception {
        // Combination: value="a", start=Integer.MAX_VALUE, length=1, criteria=new String[] {}
        try {
            DoubleMetaphone.contains("a", Integer.MAX_VALUE, 1, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_340() throws Exception {
        // Combination: value="-1", start=Integer.MAX_VALUE, length=Integer.MIN_VALUE, criteria=new String[] {}
        try {
            DoubleMetaphone.contains("-1", Integer.MAX_VALUE, Integer.MIN_VALUE, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_341() throws Exception {
        // Combination: value="-1", start=Integer.MIN_VALUE, length=0, criteria=new String[] {"value"}
        Object actual = DoubleMetaphone.contains("-1", Integer.MIN_VALUE, 0, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_342() throws Exception {
        // Combination: value="0", start=Integer.MIN_VALUE, length=1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("0", Integer.MIN_VALUE, 1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_343() throws Exception {
        // Combination: value=" ", start=Integer.MIN_VALUE, length=-1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains(" ", Integer.MIN_VALUE, -1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_344() throws Exception {
        // Combination: value="", start=Integer.MIN_VALUE, length=Integer.MAX_VALUE, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("", Integer.MIN_VALUE, Integer.MAX_VALUE, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_345() throws Exception {
        // Combination: value="", start=1, length=Integer.MIN_VALUE, criteria=new String[] {}
        try {
            DoubleMetaphone.contains("", 1, Integer.MIN_VALUE, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_346() throws Exception {
        // Combination: value=" ", start=0, length=Integer.MIN_VALUE, criteria=new String[] {}
        try {
            DoubleMetaphone.contains(" ", 0, Integer.MIN_VALUE, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_347() throws Exception {
        // Combination: value="a", start=0, length=Integer.MAX_VALUE, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("a", 0, Integer.MAX_VALUE, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_348() throws Exception {
        // Combination: value="a", start=Integer.MIN_VALUE, length=Integer.MIN_VALUE, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("a", Integer.MIN_VALUE, Integer.MIN_VALUE, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_349() throws Exception {
        // Combination: value="test123", start=1, length=0, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("test123", 1, 0, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_350() throws Exception {
        // Combination: value="test123", start=Integer.MIN_VALUE, length=-1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("test123", Integer.MIN_VALUE, -1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_351() throws Exception {
        // Combination: value="!@#", start=0, length=0, criteria=new String[] {"value"}
        Object actual = DoubleMetaphone.contains("!@#", 0, 0, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_352() throws Exception {
        // Combination: value="!@#", start=1, length=1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("!@#", 1, 1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_353() throws Exception {
        // Combination: value="!@#", start=-1, length=-1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("!@#", -1, -1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_354() throws Exception {
        // Combination: value="!@#", start=Integer.MAX_VALUE, length=Integer.MAX_VALUE, criteria=new String[] {}
        try {
            DoubleMetaphone.contains("!@#", Integer.MAX_VALUE, Integer.MAX_VALUE, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_355() throws Exception {
        // Combination: value="0", start=-1, length=0, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("0", -1, 0, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_356() throws Exception {
        // Combination: value="0", start=Integer.MAX_VALUE, length=Integer.MAX_VALUE, criteria=new String[] {}
        try {
            DoubleMetaphone.contains("0", Integer.MAX_VALUE, Integer.MAX_VALUE, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_357() throws Exception {
        // Combination: value="-1", start=1, length=1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("-1", 1, 1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_358() throws Exception {
        // Combination: value="-1", start=-1, length=Integer.MAX_VALUE, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("-1", -1, Integer.MAX_VALUE, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_359() throws Exception {
        // Combination: value="1.5", start=1, length=0, criteria=new String[] {"value"}
        Object actual = DoubleMetaphone.contains("1.5", 1, 0, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_360() throws Exception {
        // Combination: value="1.5", start=-1, length=1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("1.5", -1, 1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_361() throws Exception {
        // Combination: value="1.5", start=Integer.MAX_VALUE, length=-1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("1.5", Integer.MAX_VALUE, -1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_362() throws Exception {
        // Combination: value="1.5", start=Integer.MIN_VALUE, length=Integer.MIN_VALUE, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("1.5", Integer.MIN_VALUE, Integer.MIN_VALUE, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_363() throws Exception {
        // Combination: value="9223372036854775807", start=0, length=0, criteria=new String[] {"value"}
        Object actual = DoubleMetaphone.contains("9223372036854775807", 0, 0, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_364() throws Exception {
        // Combination: value="9223372036854775807", start=-1, length=1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("9223372036854775807", -1, 1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_365() throws Exception {
        // Combination: value="9223372036854775807", start=Integer.MAX_VALUE, length=-1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("9223372036854775807", Integer.MAX_VALUE, -1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_366() throws Exception {
        // Combination: value="9223372036854775807", start=Integer.MIN_VALUE, length=Integer.MIN_VALUE, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_367() throws Exception {
        // Combination: value="9223372036854775808", start=0, length=0, criteria=new String[] {"value"}
        Object actual = DoubleMetaphone.contains("9223372036854775808", 0, 0, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_368() throws Exception {
        // Combination: value="9223372036854775808", start=-1, length=1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("9223372036854775808", -1, 1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_369() throws Exception {
        // Combination: value="9223372036854775808", start=Integer.MAX_VALUE, length=-1, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("9223372036854775808", Integer.MAX_VALUE, -1, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_370() throws Exception {
        // Combination: value="9223372036854775808", start=Integer.MIN_VALUE, length=Integer.MAX_VALUE, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("9223372036854775808", Integer.MIN_VALUE, Integer.MAX_VALUE, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_371() throws Exception {
        // Combination: value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", start=0, length=1, criteria=new String[] {"value"}
        Object actual = DoubleMetaphone.contains("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 1, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_372() throws Exception {
        // Combination: value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", start=1, length=-1, criteria=new String[] {}
        try {
            DoubleMetaphone.contains("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, -1, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_373() throws Exception {
        // Combination: value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", start=Integer.MAX_VALUE, length=Integer.MAX_VALUE, criteria=new String[] {}
        try {
            DoubleMetaphone.contains("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE, new String[] {});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_374() throws Exception {
        // Combination: value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", start=Integer.MIN_VALUE, length=Integer.MIN_VALUE, criteria=new String[] {}
        Object actual = DoubleMetaphone.contains("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

}
