package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Attribute.
 */
public class Attribute_IPOTest {
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
    public void test_getKey_pairwise_001() throws Exception {
        // Combination: receiver__key="", receiver__value=""
        try {
            (new Attribute("", "")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_002() throws Exception {
        // Combination: receiver__key="", receiver__value=" "
        try {
            (new Attribute("", " ")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_003() throws Exception {
        // Combination: receiver__key="", receiver__value="a"
        try {
            (new Attribute("", "a")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_004() throws Exception {
        // Combination: receiver__key="", receiver__value="test123"
        try {
            (new Attribute("", "test123")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_005() throws Exception {
        // Combination: receiver__key="", receiver__value="!@#"
        try {
            (new Attribute("", "!@#")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_006() throws Exception {
        // Combination: receiver__key="", receiver__value="0"
        try {
            (new Attribute("", "0")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_007() throws Exception {
        // Combination: receiver__key="", receiver__value="-1"
        try {
            (new Attribute("", "-1")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_008() throws Exception {
        // Combination: receiver__key="", receiver__value="1.5"
        try {
            (new Attribute("", "1.5")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_009() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775807"
        try {
            (new Attribute("", "9223372036854775807")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_010() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775808"
        try {
            (new Attribute("", "9223372036854775808")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_011() throws Exception {
        // Combination: receiver__key="", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_012() throws Exception {
        // Combination: receiver__key=" ", receiver__value=""
        try {
            (new Attribute(" ", "")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_013() throws Exception {
        // Combination: receiver__key=" ", receiver__value=" "
        try {
            (new Attribute(" ", " ")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_014() throws Exception {
        // Combination: receiver__key=" ", receiver__value="a"
        try {
            (new Attribute(" ", "a")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_015() throws Exception {
        // Combination: receiver__key=" ", receiver__value="test123"
        try {
            (new Attribute(" ", "test123")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_016() throws Exception {
        // Combination: receiver__key=" ", receiver__value="!@#"
        try {
            (new Attribute(" ", "!@#")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_017() throws Exception {
        // Combination: receiver__key=" ", receiver__value="0"
        try {
            (new Attribute(" ", "0")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_018() throws Exception {
        // Combination: receiver__key=" ", receiver__value="-1"
        try {
            (new Attribute(" ", "-1")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_019() throws Exception {
        // Combination: receiver__key=" ", receiver__value="1.5"
        try {
            (new Attribute(" ", "1.5")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_020() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775807"
        try {
            (new Attribute(" ", "9223372036854775807")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_021() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775808"
        try {
            (new Attribute(" ", "9223372036854775808")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_022() throws Exception {
        // Combination: receiver__key=" ", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getKey();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_023() throws Exception {
        // Combination: receiver__key="a", receiver__value=""
        Object actual = (new Attribute("a", "")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_024() throws Exception {
        // Combination: receiver__key="a", receiver__value=" "
        Object actual = (new Attribute("a", " ")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_025() throws Exception {
        // Combination: receiver__key="a", receiver__value="a"
        Object actual = (new Attribute("a", "a")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_026() throws Exception {
        // Combination: receiver__key="a", receiver__value="test123"
        Object actual = (new Attribute("a", "test123")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_027() throws Exception {
        // Combination: receiver__key="a", receiver__value="!@#"
        Object actual = (new Attribute("a", "!@#")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_028() throws Exception {
        // Combination: receiver__key="a", receiver__value="0"
        Object actual = (new Attribute("a", "0")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_029() throws Exception {
        // Combination: receiver__key="a", receiver__value="-1"
        Object actual = (new Attribute("a", "-1")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_030() throws Exception {
        // Combination: receiver__key="a", receiver__value="1.5"
        Object actual = (new Attribute("a", "1.5")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_031() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775807"
        Object actual = (new Attribute("a", "9223372036854775807")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_032() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775808"
        Object actual = (new Attribute("a", "9223372036854775808")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_033() throws Exception {
        // Combination: receiver__key="a", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_034() throws Exception {
        // Combination: receiver__key="test123", receiver__value=""
        Object actual = (new Attribute("test123", "")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_035() throws Exception {
        // Combination: receiver__key="test123", receiver__value=" "
        Object actual = (new Attribute("test123", " ")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_036() throws Exception {
        // Combination: receiver__key="test123", receiver__value="a"
        Object actual = (new Attribute("test123", "a")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_037() throws Exception {
        // Combination: receiver__key="test123", receiver__value="test123"
        Object actual = (new Attribute("test123", "test123")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_038() throws Exception {
        // Combination: receiver__key="test123", receiver__value="!@#"
        Object actual = (new Attribute("test123", "!@#")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_039() throws Exception {
        // Combination: receiver__key="test123", receiver__value="0"
        Object actual = (new Attribute("test123", "0")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_040() throws Exception {
        // Combination: receiver__key="test123", receiver__value="-1"
        Object actual = (new Attribute("test123", "-1")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_041() throws Exception {
        // Combination: receiver__key="test123", receiver__value="1.5"
        Object actual = (new Attribute("test123", "1.5")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_042() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775807"
        Object actual = (new Attribute("test123", "9223372036854775807")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_043() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775808"
        Object actual = (new Attribute("test123", "9223372036854775808")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_044() throws Exception {
        // Combination: receiver__key="test123", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_045() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=""
        Object actual = (new Attribute("!@#", "")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_046() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=" "
        Object actual = (new Attribute("!@#", " ")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_047() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="a"
        Object actual = (new Attribute("!@#", "a")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_048() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="test123"
        Object actual = (new Attribute("!@#", "test123")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_049() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="!@#"
        Object actual = (new Attribute("!@#", "!@#")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_050() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="0"
        Object actual = (new Attribute("!@#", "0")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_051() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="-1"
        Object actual = (new Attribute("!@#", "-1")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_052() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="1.5"
        Object actual = (new Attribute("!@#", "1.5")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_053() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775807"
        Object actual = (new Attribute("!@#", "9223372036854775807")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_054() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775808"
        Object actual = (new Attribute("!@#", "9223372036854775808")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_055() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_056() throws Exception {
        // Combination: receiver__key="0", receiver__value=""
        Object actual = (new Attribute("0", "")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_057() throws Exception {
        // Combination: receiver__key="0", receiver__value=" "
        Object actual = (new Attribute("0", " ")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_058() throws Exception {
        // Combination: receiver__key="0", receiver__value="a"
        Object actual = (new Attribute("0", "a")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_059() throws Exception {
        // Combination: receiver__key="0", receiver__value="test123"
        Object actual = (new Attribute("0", "test123")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_060() throws Exception {
        // Combination: receiver__key="0", receiver__value="!@#"
        Object actual = (new Attribute("0", "!@#")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_061() throws Exception {
        // Combination: receiver__key="0", receiver__value="0"
        Object actual = (new Attribute("0", "0")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_062() throws Exception {
        // Combination: receiver__key="0", receiver__value="-1"
        Object actual = (new Attribute("0", "-1")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_063() throws Exception {
        // Combination: receiver__key="0", receiver__value="1.5"
        Object actual = (new Attribute("0", "1.5")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_064() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775807"
        Object actual = (new Attribute("0", "9223372036854775807")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_065() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775808"
        Object actual = (new Attribute("0", "9223372036854775808")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_066() throws Exception {
        // Combination: receiver__key="0", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_067() throws Exception {
        // Combination: receiver__key="-1", receiver__value=""
        Object actual = (new Attribute("-1", "")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_068() throws Exception {
        // Combination: receiver__key="-1", receiver__value=" "
        Object actual = (new Attribute("-1", " ")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_069() throws Exception {
        // Combination: receiver__key="-1", receiver__value="a"
        Object actual = (new Attribute("-1", "a")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_070() throws Exception {
        // Combination: receiver__key="-1", receiver__value="test123"
        Object actual = (new Attribute("-1", "test123")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_071() throws Exception {
        // Combination: receiver__key="-1", receiver__value="!@#"
        Object actual = (new Attribute("-1", "!@#")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_072() throws Exception {
        // Combination: receiver__key="-1", receiver__value="0"
        Object actual = (new Attribute("-1", "0")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_073() throws Exception {
        // Combination: receiver__key="-1", receiver__value="-1"
        Object actual = (new Attribute("-1", "-1")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_074() throws Exception {
        // Combination: receiver__key="-1", receiver__value="1.5"
        Object actual = (new Attribute("-1", "1.5")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_075() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775807"
        Object actual = (new Attribute("-1", "9223372036854775807")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_076() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775808"
        Object actual = (new Attribute("-1", "9223372036854775808")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_077() throws Exception {
        // Combination: receiver__key="-1", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_078() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=""
        Object actual = (new Attribute("1.5", "")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_079() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=" "
        Object actual = (new Attribute("1.5", " ")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_080() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="a"
        Object actual = (new Attribute("1.5", "a")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_081() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="test123"
        Object actual = (new Attribute("1.5", "test123")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_082() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="!@#"
        Object actual = (new Attribute("1.5", "!@#")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_083() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="0"
        Object actual = (new Attribute("1.5", "0")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_084() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="-1"
        Object actual = (new Attribute("1.5", "-1")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_085() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="1.5"
        Object actual = (new Attribute("1.5", "1.5")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_086() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775807"
        Object actual = (new Attribute("1.5", "9223372036854775807")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_087() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775808"
        Object actual = (new Attribute("1.5", "9223372036854775808")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_088() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_089() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=""
        Object actual = (new Attribute("9223372036854775807", "")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_090() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=" "
        Object actual = (new Attribute("9223372036854775807", " ")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_091() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="a"
        Object actual = (new Attribute("9223372036854775807", "a")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_092() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775807", "test123")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_093() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775807", "!@#")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_094() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="0"
        Object actual = (new Attribute("9223372036854775807", "0")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_095() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775807", "-1")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_096() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775807", "1.5")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_097() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775807")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_098() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775808")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_099() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_100() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=""
        Object actual = (new Attribute("9223372036854775808", "")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_101() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=" "
        Object actual = (new Attribute("9223372036854775808", " ")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_102() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="a"
        Object actual = (new Attribute("9223372036854775808", "a")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_103() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775808", "test123")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_104() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775808", "!@#")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_105() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="0"
        Object actual = (new Attribute("9223372036854775808", "0")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_106() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775808", "-1")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_107() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775808", "1.5")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_108() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775807")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_109() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775808")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_110() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_111() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=""
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_112() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=" "
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_113() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="a"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_114() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="test123"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_115() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="!@#"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_116() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="0"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_117() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="-1"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_118() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="1.5"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_119() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775807"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_120() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775808"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKey_pairwise_121() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getKey();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_122() throws Exception {
        // Combination: receiver__key="", receiver__value=""
        try {
            (new Attribute("", "")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_123() throws Exception {
        // Combination: receiver__key="", receiver__value=" "
        try {
            (new Attribute("", " ")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_124() throws Exception {
        // Combination: receiver__key="", receiver__value="a"
        try {
            (new Attribute("", "a")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_125() throws Exception {
        // Combination: receiver__key="", receiver__value="test123"
        try {
            (new Attribute("", "test123")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_126() throws Exception {
        // Combination: receiver__key="", receiver__value="!@#"
        try {
            (new Attribute("", "!@#")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_127() throws Exception {
        // Combination: receiver__key="", receiver__value="0"
        try {
            (new Attribute("", "0")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_128() throws Exception {
        // Combination: receiver__key="", receiver__value="-1"
        try {
            (new Attribute("", "-1")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_129() throws Exception {
        // Combination: receiver__key="", receiver__value="1.5"
        try {
            (new Attribute("", "1.5")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_130() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775807"
        try {
            (new Attribute("", "9223372036854775807")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_131() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775808"
        try {
            (new Attribute("", "9223372036854775808")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_132() throws Exception {
        // Combination: receiver__key="", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_133() throws Exception {
        // Combination: receiver__key=" ", receiver__value=""
        try {
            (new Attribute(" ", "")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_134() throws Exception {
        // Combination: receiver__key=" ", receiver__value=" "
        try {
            (new Attribute(" ", " ")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_135() throws Exception {
        // Combination: receiver__key=" ", receiver__value="a"
        try {
            (new Attribute(" ", "a")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_136() throws Exception {
        // Combination: receiver__key=" ", receiver__value="test123"
        try {
            (new Attribute(" ", "test123")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_137() throws Exception {
        // Combination: receiver__key=" ", receiver__value="!@#"
        try {
            (new Attribute(" ", "!@#")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_138() throws Exception {
        // Combination: receiver__key=" ", receiver__value="0"
        try {
            (new Attribute(" ", "0")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_139() throws Exception {
        // Combination: receiver__key=" ", receiver__value="-1"
        try {
            (new Attribute(" ", "-1")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_140() throws Exception {
        // Combination: receiver__key=" ", receiver__value="1.5"
        try {
            (new Attribute(" ", "1.5")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_141() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775807"
        try {
            (new Attribute(" ", "9223372036854775807")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_142() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775808"
        try {
            (new Attribute(" ", "9223372036854775808")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_143() throws Exception {
        // Combination: receiver__key=" ", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_144() throws Exception {
        // Combination: receiver__key="a", receiver__value=""
        Object actual = (new Attribute("a", "")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_145() throws Exception {
        // Combination: receiver__key="a", receiver__value=" "
        Object actual = (new Attribute("a", " ")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_146() throws Exception {
        // Combination: receiver__key="a", receiver__value="a"
        Object actual = (new Attribute("a", "a")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_147() throws Exception {
        // Combination: receiver__key="a", receiver__value="test123"
        Object actual = (new Attribute("a", "test123")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_148() throws Exception {
        // Combination: receiver__key="a", receiver__value="!@#"
        Object actual = (new Attribute("a", "!@#")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_149() throws Exception {
        // Combination: receiver__key="a", receiver__value="0"
        Object actual = (new Attribute("a", "0")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_150() throws Exception {
        // Combination: receiver__key="a", receiver__value="-1"
        Object actual = (new Attribute("a", "-1")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_151() throws Exception {
        // Combination: receiver__key="a", receiver__value="1.5"
        Object actual = (new Attribute("a", "1.5")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_152() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775807"
        Object actual = (new Attribute("a", "9223372036854775807")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_153() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775808"
        Object actual = (new Attribute("a", "9223372036854775808")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_154() throws Exception {
        // Combination: receiver__key="a", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_155() throws Exception {
        // Combination: receiver__key="test123", receiver__value=""
        Object actual = (new Attribute("test123", "")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_156() throws Exception {
        // Combination: receiver__key="test123", receiver__value=" "
        Object actual = (new Attribute("test123", " ")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_157() throws Exception {
        // Combination: receiver__key="test123", receiver__value="a"
        Object actual = (new Attribute("test123", "a")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_158() throws Exception {
        // Combination: receiver__key="test123", receiver__value="test123"
        Object actual = (new Attribute("test123", "test123")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_159() throws Exception {
        // Combination: receiver__key="test123", receiver__value="!@#"
        Object actual = (new Attribute("test123", "!@#")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_160() throws Exception {
        // Combination: receiver__key="test123", receiver__value="0"
        Object actual = (new Attribute("test123", "0")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_161() throws Exception {
        // Combination: receiver__key="test123", receiver__value="-1"
        Object actual = (new Attribute("test123", "-1")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_162() throws Exception {
        // Combination: receiver__key="test123", receiver__value="1.5"
        Object actual = (new Attribute("test123", "1.5")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_163() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775807"
        Object actual = (new Attribute("test123", "9223372036854775807")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_164() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775808"
        Object actual = (new Attribute("test123", "9223372036854775808")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_165() throws Exception {
        // Combination: receiver__key="test123", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_166() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=""
        Object actual = (new Attribute("!@#", "")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_167() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=" "
        Object actual = (new Attribute("!@#", " ")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_168() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="a"
        Object actual = (new Attribute("!@#", "a")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_169() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="test123"
        Object actual = (new Attribute("!@#", "test123")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_170() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="!@#"
        Object actual = (new Attribute("!@#", "!@#")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_171() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="0"
        Object actual = (new Attribute("!@#", "0")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_172() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="-1"
        Object actual = (new Attribute("!@#", "-1")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_173() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="1.5"
        Object actual = (new Attribute("!@#", "1.5")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_174() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775807"
        Object actual = (new Attribute("!@#", "9223372036854775807")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_175() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775808"
        Object actual = (new Attribute("!@#", "9223372036854775808")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_176() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_177() throws Exception {
        // Combination: receiver__key="0", receiver__value=""
        Object actual = (new Attribute("0", "")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_178() throws Exception {
        // Combination: receiver__key="0", receiver__value=" "
        Object actual = (new Attribute("0", " ")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_179() throws Exception {
        // Combination: receiver__key="0", receiver__value="a"
        Object actual = (new Attribute("0", "a")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_180() throws Exception {
        // Combination: receiver__key="0", receiver__value="test123"
        Object actual = (new Attribute("0", "test123")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_181() throws Exception {
        // Combination: receiver__key="0", receiver__value="!@#"
        Object actual = (new Attribute("0", "!@#")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_182() throws Exception {
        // Combination: receiver__key="0", receiver__value="0"
        Object actual = (new Attribute("0", "0")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_183() throws Exception {
        // Combination: receiver__key="0", receiver__value="-1"
        Object actual = (new Attribute("0", "-1")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_184() throws Exception {
        // Combination: receiver__key="0", receiver__value="1.5"
        Object actual = (new Attribute("0", "1.5")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_185() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775807"
        Object actual = (new Attribute("0", "9223372036854775807")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_186() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775808"
        Object actual = (new Attribute("0", "9223372036854775808")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_187() throws Exception {
        // Combination: receiver__key="0", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_188() throws Exception {
        // Combination: receiver__key="-1", receiver__value=""
        Object actual = (new Attribute("-1", "")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_189() throws Exception {
        // Combination: receiver__key="-1", receiver__value=" "
        Object actual = (new Attribute("-1", " ")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_190() throws Exception {
        // Combination: receiver__key="-1", receiver__value="a"
        Object actual = (new Attribute("-1", "a")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_191() throws Exception {
        // Combination: receiver__key="-1", receiver__value="test123"
        Object actual = (new Attribute("-1", "test123")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_192() throws Exception {
        // Combination: receiver__key="-1", receiver__value="!@#"
        Object actual = (new Attribute("-1", "!@#")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_193() throws Exception {
        // Combination: receiver__key="-1", receiver__value="0"
        Object actual = (new Attribute("-1", "0")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_194() throws Exception {
        // Combination: receiver__key="-1", receiver__value="-1"
        Object actual = (new Attribute("-1", "-1")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_195() throws Exception {
        // Combination: receiver__key="-1", receiver__value="1.5"
        Object actual = (new Attribute("-1", "1.5")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_196() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775807"
        Object actual = (new Attribute("-1", "9223372036854775807")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_197() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775808"
        Object actual = (new Attribute("-1", "9223372036854775808")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_198() throws Exception {
        // Combination: receiver__key="-1", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_199() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=""
        Object actual = (new Attribute("1.5", "")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_200() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=" "
        Object actual = (new Attribute("1.5", " ")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_201() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="a"
        Object actual = (new Attribute("1.5", "a")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_202() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="test123"
        Object actual = (new Attribute("1.5", "test123")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_203() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="!@#"
        Object actual = (new Attribute("1.5", "!@#")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_204() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="0"
        Object actual = (new Attribute("1.5", "0")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_205() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="-1"
        Object actual = (new Attribute("1.5", "-1")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_206() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="1.5"
        Object actual = (new Attribute("1.5", "1.5")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_207() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775807"
        Object actual = (new Attribute("1.5", "9223372036854775807")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_208() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775808"
        Object actual = (new Attribute("1.5", "9223372036854775808")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_209() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_210() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=""
        Object actual = (new Attribute("9223372036854775807", "")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_211() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=" "
        Object actual = (new Attribute("9223372036854775807", " ")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_212() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="a"
        Object actual = (new Attribute("9223372036854775807", "a")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_213() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775807", "test123")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_214() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775807", "!@#")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_215() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="0"
        Object actual = (new Attribute("9223372036854775807", "0")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_216() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775807", "-1")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_217() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775807", "1.5")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_218() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775807")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_219() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775808")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_220() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_221() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=""
        Object actual = (new Attribute("9223372036854775808", "")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_222() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=" "
        Object actual = (new Attribute("9223372036854775808", " ")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_223() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="a"
        Object actual = (new Attribute("9223372036854775808", "a")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_224() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775808", "test123")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_225() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775808", "!@#")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_226() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="0"
        Object actual = (new Attribute("9223372036854775808", "0")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_227() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775808", "-1")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_228() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775808", "1.5")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_229() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775807")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_230() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775808")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_231() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_232() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=""
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_233() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=" "
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_234() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="a"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_235() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="test123"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_236() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="!@#"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_237() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="0"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_238() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="-1"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_239() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="1.5"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_240() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775807"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_241() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775808"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_242() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_243() throws Exception {
        // Combination: receiver__key="", receiver__value="", val=""
        try {
            (new Attribute("", "")).setValue("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_244() throws Exception {
        // Combination: receiver__key="", receiver__value=" ", val=" "
        try {
            (new Attribute("", " ")).setValue(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_245() throws Exception {
        // Combination: receiver__key="", receiver__value="a", val="a"
        try {
            (new Attribute("", "a")).setValue("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_246() throws Exception {
        // Combination: receiver__key="", receiver__value="test123", val="test123"
        try {
            (new Attribute("", "test123")).setValue("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_247() throws Exception {
        // Combination: receiver__key="", receiver__value="!@#", val="!@#"
        try {
            (new Attribute("", "!@#")).setValue("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_248() throws Exception {
        // Combination: receiver__key="", receiver__value="0", val="0"
        try {
            (new Attribute("", "0")).setValue("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_249() throws Exception {
        // Combination: receiver__key="", receiver__value="-1", val="-1"
        try {
            (new Attribute("", "-1")).setValue("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_250() throws Exception {
        // Combination: receiver__key="", receiver__value="1.5", val="1.5"
        try {
            (new Attribute("", "1.5")).setValue("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_251() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775807", val="9223372036854775807"
        try {
            (new Attribute("", "9223372036854775807")).setValue("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_252() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775808", val="9223372036854775808"
        try {
            (new Attribute("", "9223372036854775808")).setValue("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_253() throws Exception {
        // Combination: receiver__key="", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", val="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).setValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_254() throws Exception {
        // Combination: receiver__key=" ", receiver__value="", val=" "
        try {
            (new Attribute(" ", "")).setValue(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_255() throws Exception {
        // Combination: receiver__key=" ", receiver__value=" ", val=""
        try {
            (new Attribute(" ", " ")).setValue("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_256() throws Exception {
        // Combination: receiver__key=" ", receiver__value="a", val="test123"
        try {
            (new Attribute(" ", "a")).setValue("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_257() throws Exception {
        // Combination: receiver__key=" ", receiver__value="test123", val="a"
        try {
            (new Attribute(" ", "test123")).setValue("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_258() throws Exception {
        // Combination: receiver__key=" ", receiver__value="!@#", val="0"
        try {
            (new Attribute(" ", "!@#")).setValue("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_259() throws Exception {
        // Combination: receiver__key=" ", receiver__value="0", val="!@#"
        try {
            (new Attribute(" ", "0")).setValue("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_260() throws Exception {
        // Combination: receiver__key=" ", receiver__value="-1", val="1.5"
        try {
            (new Attribute(" ", "-1")).setValue("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_261() throws Exception {
        // Combination: receiver__key=" ", receiver__value="1.5", val="-1"
        try {
            (new Attribute(" ", "1.5")).setValue("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_262() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775807", val="9223372036854775808"
        try {
            (new Attribute(" ", "9223372036854775807")).setValue("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_263() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775808", val="9223372036854775807"
        try {
            (new Attribute(" ", "9223372036854775808")).setValue("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_264() throws Exception {
        // Combination: receiver__key=" ", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", val=""
        try {
            (new Attribute(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).setValue("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_265() throws Exception {
        // Combination: receiver__key="a", receiver__value="", val="a"
        try {
            (new Attribute("a", "")).setValue("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_266() throws Exception {
        // Combination: receiver__key="a", receiver__value=" ", val="test123"
        try {
            (new Attribute("a", " ")).setValue("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_267() throws Exception {
        // Combination: receiver__key="a", receiver__value="a", val=""
        try {
            (new Attribute("a", "a")).setValue("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_268() throws Exception {
        // Combination: receiver__key="a", receiver__value="test123", val=" "
        try {
            (new Attribute("a", "test123")).setValue(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_269() throws Exception {
        // Combination: receiver__key="a", receiver__value="!@#", val="-1"
        try {
            (new Attribute("a", "!@#")).setValue("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_270() throws Exception {
        // Combination: receiver__key="a", receiver__value="0", val="1.5"
        try {
            (new Attribute("a", "0")).setValue("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_271() throws Exception {
        // Combination: receiver__key="a", receiver__value="-1", val="!@#"
        try {
            (new Attribute("a", "-1")).setValue("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_272() throws Exception {
        // Combination: receiver__key="a", receiver__value="1.5", val="0"
        try {
            (new Attribute("a", "1.5")).setValue("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_273() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775807", val="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("a", "9223372036854775807")).setValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_274() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775808", val=""
        try {
            (new Attribute("a", "9223372036854775808")).setValue("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_275() throws Exception {
        // Combination: receiver__key="a", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", val="9223372036854775807"
        try {
            (new Attribute("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).setValue("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_276() throws Exception {
        // Combination: receiver__key="test123", receiver__value="", val="test123"
        try {
            (new Attribute("test123", "")).setValue("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_277() throws Exception {
        // Combination: receiver__key="test123", receiver__value=" ", val="a"
        try {
            (new Attribute("test123", " ")).setValue("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_278() throws Exception {
        // Combination: receiver__key="test123", receiver__value="a", val=" "
        try {
            (new Attribute("test123", "a")).setValue(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_279() throws Exception {
        // Combination: receiver__key="test123", receiver__value="test123", val=""
        try {
            (new Attribute("test123", "test123")).setValue("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_280() throws Exception {
        // Combination: receiver__key="test123", receiver__value="!@#", val="1.5"
        try {
            (new Attribute("test123", "!@#")).setValue("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_281() throws Exception {
        // Combination: receiver__key="test123", receiver__value="0", val="-1"
        try {
            (new Attribute("test123", "0")).setValue("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_282() throws Exception {
        // Combination: receiver__key="test123", receiver__value="-1", val="0"
        try {
            (new Attribute("test123", "-1")).setValue("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_283() throws Exception {
        // Combination: receiver__key="test123", receiver__value="1.5", val="!@#"
        try {
            (new Attribute("test123", "1.5")).setValue("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_284() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775807", val=""
        try {
            (new Attribute("test123", "9223372036854775807")).setValue("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_285() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775808", val="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("test123", "9223372036854775808")).setValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_286() throws Exception {
        // Combination: receiver__key="test123", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", val="9223372036854775808"
        try {
            (new Attribute("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).setValue("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_287() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="", val="!@#"
        try {
            (new Attribute("!@#", "")).setValue("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_288() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=" ", val="0"
        try {
            (new Attribute("!@#", " ")).setValue("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_289() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="a", val="-1"
        try {
            (new Attribute("!@#", "a")).setValue("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_290() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="test123", val="1.5"
        try {
            (new Attribute("!@#", "test123")).setValue("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_291() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="!@#", val=""
        try {
            (new Attribute("!@#", "!@#")).setValue("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_292() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="0", val=" "
        try {
            (new Attribute("!@#", "0")).setValue(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_293() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="-1", val="a"
        try {
            (new Attribute("!@#", "-1")).setValue("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_294() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="1.5", val="test123"
        try {
            (new Attribute("!@#", "1.5")).setValue("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_295() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775807", val=" "
        try {
            (new Attribute("!@#", "9223372036854775807")).setValue(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_296() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775808", val=" "
        try {
            (new Attribute("!@#", "9223372036854775808")).setValue(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_297() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", val=" "
        try {
            (new Attribute("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).setValue(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_298() throws Exception {
        // Combination: receiver__key="0", receiver__value="", val="0"
        try {
            (new Attribute("0", "")).setValue("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_299() throws Exception {
        // Combination: receiver__key="0", receiver__value=" ", val="!@#"
        try {
            (new Attribute("0", " ")).setValue("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_300() throws Exception {
        // Combination: receiver__key="0", receiver__value="a", val="1.5"
        try {
            (new Attribute("0", "a")).setValue("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_301() throws Exception {
        // Combination: receiver__key="0", receiver__value="test123", val="-1"
        try {
            (new Attribute("0", "test123")).setValue("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_302() throws Exception {
        // Combination: receiver__key="0", receiver__value="!@#", val=" "
        try {
            (new Attribute("0", "!@#")).setValue(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_303() throws Exception {
        // Combination: receiver__key="0", receiver__value="0", val=""
        try {
            (new Attribute("0", "0")).setValue("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_304() throws Exception {
        // Combination: receiver__key="0", receiver__value="-1", val="test123"
        try {
            (new Attribute("0", "-1")).setValue("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_305() throws Exception {
        // Combination: receiver__key="0", receiver__value="1.5", val="a"
        try {
            (new Attribute("0", "1.5")).setValue("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_306() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775807", val="a"
        try {
            (new Attribute("0", "9223372036854775807")).setValue("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_307() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775808", val="a"
        try {
            (new Attribute("0", "9223372036854775808")).setValue("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_308() throws Exception {
        // Combination: receiver__key="0", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", val="a"
        try {
            (new Attribute("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).setValue("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_309() throws Exception {
        // Combination: receiver__key="-1", receiver__value="", val="-1"
        try {
            (new Attribute("-1", "")).setValue("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_310() throws Exception {
        // Combination: receiver__key="-1", receiver__value=" ", val="1.5"
        try {
            (new Attribute("-1", " ")).setValue("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_311() throws Exception {
        // Combination: receiver__key="-1", receiver__value="a", val="!@#"
        try {
            (new Attribute("-1", "a")).setValue("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_312() throws Exception {
        // Combination: receiver__key="-1", receiver__value="test123", val="0"
        try {
            (new Attribute("-1", "test123")).setValue("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_313() throws Exception {
        // Combination: receiver__key="-1", receiver__value="!@#", val="a"
        try {
            (new Attribute("-1", "!@#")).setValue("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_314() throws Exception {
        // Combination: receiver__key="-1", receiver__value="0", val="test123"
        try {
            (new Attribute("-1", "0")).setValue("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_315() throws Exception {
        // Combination: receiver__key="-1", receiver__value="-1", val=""
        try {
            (new Attribute("-1", "-1")).setValue("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_316() throws Exception {
        // Combination: receiver__key="-1", receiver__value="1.5", val=" "
        try {
            (new Attribute("-1", "1.5")).setValue(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_317() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775807", val="test123"
        try {
            (new Attribute("-1", "9223372036854775807")).setValue("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_318() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775808", val="test123"
        try {
            (new Attribute("-1", "9223372036854775808")).setValue("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_319() throws Exception {
        // Combination: receiver__key="-1", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", val="test123"
        try {
            (new Attribute("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).setValue("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_320() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="", val="1.5"
        try {
            (new Attribute("1.5", "")).setValue("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_321() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=" ", val="-1"
        try {
            (new Attribute("1.5", " ")).setValue("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_322() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="a", val="0"
        try {
            (new Attribute("1.5", "a")).setValue("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_323() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="test123", val="!@#"
        try {
            (new Attribute("1.5", "test123")).setValue("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_324() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="!@#", val="test123"
        try {
            (new Attribute("1.5", "!@#")).setValue("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_325() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="0", val="a"
        try {
            (new Attribute("1.5", "0")).setValue("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_326() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="-1", val=" "
        try {
            (new Attribute("1.5", "-1")).setValue(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_327() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="1.5", val=""
        try {
            (new Attribute("1.5", "1.5")).setValue("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_328() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775807", val="!@#"
        try {
            (new Attribute("1.5", "9223372036854775807")).setValue("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_329() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775808", val="!@#"
        try {
            (new Attribute("1.5", "9223372036854775808")).setValue("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_330() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", val="!@#"
        try {
            (new Attribute("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).setValue("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_331() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="", val="9223372036854775807"
        try {
            (new Attribute("9223372036854775807", "")).setValue("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_332() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=" ", val="9223372036854775808"
        try {
            (new Attribute("9223372036854775807", " ")).setValue("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_333() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="a", val="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("9223372036854775807", "a")).setValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_334() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="test123", val=""
        try {
            (new Attribute("9223372036854775807", "test123")).setValue("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_335() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="!@#", val=" "
        try {
            (new Attribute("9223372036854775807", "!@#")).setValue(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_336() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="0", val="a"
        try {
            (new Attribute("9223372036854775807", "0")).setValue("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_337() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="-1", val="test123"
        try {
            (new Attribute("9223372036854775807", "-1")).setValue("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_338() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="1.5", val="!@#"
        try {
            (new Attribute("9223372036854775807", "1.5")).setValue("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_339() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775807", val="0"
        try {
            (new Attribute("9223372036854775807", "9223372036854775807")).setValue("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_340() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775808", val="-1"
        try {
            (new Attribute("9223372036854775807", "9223372036854775808")).setValue("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_341() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", val="1.5"
        try {
            (new Attribute("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).setValue("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_342() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="", val="9223372036854775808"
        try {
            (new Attribute("9223372036854775808", "")).setValue("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_343() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=" ", val="9223372036854775807"
        try {
            (new Attribute("9223372036854775808", " ")).setValue("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_344() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="a", val=""
        try {
            (new Attribute("9223372036854775808", "a")).setValue("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_345() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="test123", val="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("9223372036854775808", "test123")).setValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_346() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="!@#", val=" "
        try {
            (new Attribute("9223372036854775808", "!@#")).setValue(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_347() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="0", val="a"
        try {
            (new Attribute("9223372036854775808", "0")).setValue("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_348() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="-1", val="test123"
        try {
            (new Attribute("9223372036854775808", "-1")).setValue("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_349() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="1.5", val="!@#"
        try {
            (new Attribute("9223372036854775808", "1.5")).setValue("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_350() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775807", val="-1"
        try {
            (new Attribute("9223372036854775808", "9223372036854775807")).setValue("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_351() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775808", val="0"
        try {
            (new Attribute("9223372036854775808", "9223372036854775808")).setValue("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_352() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", val="0"
        try {
            (new Attribute("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).setValue("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_353() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="", val="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "")).setValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_354() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=" ", val=""
        try {
            (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ")).setValue("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_355() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="a", val="9223372036854775807"
        try {
            (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a")).setValue("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_356() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="test123", val="9223372036854775808"
        try {
            (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123")).setValue("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_357() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="!@#", val=" "
        try {
            (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#")).setValue(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_358() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="0", val="a"
        try {
            (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0")).setValue("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_359() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="-1", val="test123"
        try {
            (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1")).setValue("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_360() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="1.5", val="!@#"
        try {
            (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5")).setValue("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_361() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775807", val="1.5"
        try {
            (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807")).setValue("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_362() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775808", val="0"
        try {
            (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808")).setValue("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_363() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", val="-1"
        try {
            (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).setValue("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_364() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775808", val="1.5"
        try {
            (new Attribute("9223372036854775808", "9223372036854775808")).setValue("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_365() throws Exception {
        // Combination: receiver__key="test123", receiver__value="test123", val="9223372036854775807"
        try {
            (new Attribute("test123", "test123")).setValue("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_366() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="!@#", val="9223372036854775807"
        try {
            (new Attribute("!@#", "!@#")).setValue("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_367() throws Exception {
        // Combination: receiver__key="0", receiver__value="0", val="9223372036854775807"
        try {
            (new Attribute("0", "0")).setValue("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_368() throws Exception {
        // Combination: receiver__key="-1", receiver__value="-1", val="9223372036854775807"
        try {
            (new Attribute("-1", "-1")).setValue("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_369() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="1.5", val="9223372036854775807"
        try {
            (new Attribute("1.5", "1.5")).setValue("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_370() throws Exception {
        // Combination: receiver__key="a", receiver__value="a", val="9223372036854775808"
        try {
            (new Attribute("a", "a")).setValue("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_371() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="!@#", val="9223372036854775808"
        try {
            (new Attribute("!@#", "!@#")).setValue("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_372() throws Exception {
        // Combination: receiver__key="0", receiver__value="0", val="9223372036854775808"
        try {
            (new Attribute("0", "0")).setValue("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_373() throws Exception {
        // Combination: receiver__key="-1", receiver__value="-1", val="9223372036854775808"
        try {
            (new Attribute("-1", "-1")).setValue("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_374() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="1.5", val="9223372036854775808"
        try {
            (new Attribute("1.5", "1.5")).setValue("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_375() throws Exception {
        // Combination: receiver__key=" ", receiver__value=" ", val="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute(" ", " ")).setValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_376() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="!@#", val="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("!@#", "!@#")).setValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_377() throws Exception {
        // Combination: receiver__key="0", receiver__value="0", val="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("0", "0")).setValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_378() throws Exception {
        // Combination: receiver__key="-1", receiver__value="-1", val="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("-1", "-1")).setValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_setValue_pairwise_379() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="1.5", val="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("1.5", "1.5")).setValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_380() throws Exception {
        // Combination: receiver__key="", receiver__value=""
        try {
            (new Attribute("", "")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_381() throws Exception {
        // Combination: receiver__key="", receiver__value=" "
        try {
            (new Attribute("", " ")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_382() throws Exception {
        // Combination: receiver__key="", receiver__value="a"
        try {
            (new Attribute("", "a")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_383() throws Exception {
        // Combination: receiver__key="", receiver__value="test123"
        try {
            (new Attribute("", "test123")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_384() throws Exception {
        // Combination: receiver__key="", receiver__value="!@#"
        try {
            (new Attribute("", "!@#")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_385() throws Exception {
        // Combination: receiver__key="", receiver__value="0"
        try {
            (new Attribute("", "0")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_386() throws Exception {
        // Combination: receiver__key="", receiver__value="-1"
        try {
            (new Attribute("", "-1")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_387() throws Exception {
        // Combination: receiver__key="", receiver__value="1.5"
        try {
            (new Attribute("", "1.5")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_388() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775807"
        try {
            (new Attribute("", "9223372036854775807")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_389() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775808"
        try {
            (new Attribute("", "9223372036854775808")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_390() throws Exception {
        // Combination: receiver__key="", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_391() throws Exception {
        // Combination: receiver__key=" ", receiver__value=""
        try {
            (new Attribute(" ", "")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_392() throws Exception {
        // Combination: receiver__key=" ", receiver__value=" "
        try {
            (new Attribute(" ", " ")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_393() throws Exception {
        // Combination: receiver__key=" ", receiver__value="a"
        try {
            (new Attribute(" ", "a")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_394() throws Exception {
        // Combination: receiver__key=" ", receiver__value="test123"
        try {
            (new Attribute(" ", "test123")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_395() throws Exception {
        // Combination: receiver__key=" ", receiver__value="!@#"
        try {
            (new Attribute(" ", "!@#")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_396() throws Exception {
        // Combination: receiver__key=" ", receiver__value="0"
        try {
            (new Attribute(" ", "0")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_397() throws Exception {
        // Combination: receiver__key=" ", receiver__value="-1"
        try {
            (new Attribute(" ", "-1")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_398() throws Exception {
        // Combination: receiver__key=" ", receiver__value="1.5"
        try {
            (new Attribute(" ", "1.5")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_399() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775807"
        try {
            (new Attribute(" ", "9223372036854775807")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_400() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775808"
        try {
            (new Attribute(" ", "9223372036854775808")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_401() throws Exception {
        // Combination: receiver__key=" ", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).html();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_402() throws Exception {
        // Combination: receiver__key="a", receiver__value=""
        Object actual = (new Attribute("a", "")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_403() throws Exception {
        // Combination: receiver__key="a", receiver__value=" "
        Object actual = (new Attribute("a", " ")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_404() throws Exception {
        // Combination: receiver__key="a", receiver__value="a"
        Object actual = (new Attribute("a", "a")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_405() throws Exception {
        // Combination: receiver__key="a", receiver__value="test123"
        Object actual = (new Attribute("a", "test123")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_406() throws Exception {
        // Combination: receiver__key="a", receiver__value="!@#"
        Object actual = (new Attribute("a", "!@#")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_407() throws Exception {
        // Combination: receiver__key="a", receiver__value="0"
        Object actual = (new Attribute("a", "0")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_408() throws Exception {
        // Combination: receiver__key="a", receiver__value="-1"
        Object actual = (new Attribute("a", "-1")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_409() throws Exception {
        // Combination: receiver__key="a", receiver__value="1.5"
        Object actual = (new Attribute("a", "1.5")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_410() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775807"
        Object actual = (new Attribute("a", "9223372036854775807")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_411() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775808"
        Object actual = (new Attribute("a", "9223372036854775808")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_412() throws Exception {
        // Combination: receiver__key="a", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_413() throws Exception {
        // Combination: receiver__key="test123", receiver__value=""
        Object actual = (new Attribute("test123", "")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_414() throws Exception {
        // Combination: receiver__key="test123", receiver__value=" "
        Object actual = (new Attribute("test123", " ")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_415() throws Exception {
        // Combination: receiver__key="test123", receiver__value="a"
        Object actual = (new Attribute("test123", "a")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_416() throws Exception {
        // Combination: receiver__key="test123", receiver__value="test123"
        Object actual = (new Attribute("test123", "test123")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_417() throws Exception {
        // Combination: receiver__key="test123", receiver__value="!@#"
        Object actual = (new Attribute("test123", "!@#")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_418() throws Exception {
        // Combination: receiver__key="test123", receiver__value="0"
        Object actual = (new Attribute("test123", "0")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_419() throws Exception {
        // Combination: receiver__key="test123", receiver__value="-1"
        Object actual = (new Attribute("test123", "-1")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_420() throws Exception {
        // Combination: receiver__key="test123", receiver__value="1.5"
        Object actual = (new Attribute("test123", "1.5")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_421() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775807"
        Object actual = (new Attribute("test123", "9223372036854775807")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_422() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775808"
        Object actual = (new Attribute("test123", "9223372036854775808")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_423() throws Exception {
        // Combination: receiver__key="test123", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_424() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=""
        Object actual = (new Attribute("!@#", "")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_425() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=" "
        Object actual = (new Attribute("!@#", " ")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_426() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="a"
        Object actual = (new Attribute("!@#", "a")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_427() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="test123"
        Object actual = (new Attribute("!@#", "test123")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_428() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="!@#"
        Object actual = (new Attribute("!@#", "!@#")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_429() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="0"
        Object actual = (new Attribute("!@#", "0")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_430() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="-1"
        Object actual = (new Attribute("!@#", "-1")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_431() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="1.5"
        Object actual = (new Attribute("!@#", "1.5")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_432() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775807"
        Object actual = (new Attribute("!@#", "9223372036854775807")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_433() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775808"
        Object actual = (new Attribute("!@#", "9223372036854775808")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_434() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_435() throws Exception {
        // Combination: receiver__key="0", receiver__value=""
        Object actual = (new Attribute("0", "")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_436() throws Exception {
        // Combination: receiver__key="0", receiver__value=" "
        Object actual = (new Attribute("0", " ")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_437() throws Exception {
        // Combination: receiver__key="0", receiver__value="a"
        Object actual = (new Attribute("0", "a")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_438() throws Exception {
        // Combination: receiver__key="0", receiver__value="test123"
        Object actual = (new Attribute("0", "test123")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_439() throws Exception {
        // Combination: receiver__key="0", receiver__value="!@#"
        Object actual = (new Attribute("0", "!@#")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_440() throws Exception {
        // Combination: receiver__key="0", receiver__value="0"
        Object actual = (new Attribute("0", "0")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_441() throws Exception {
        // Combination: receiver__key="0", receiver__value="-1"
        Object actual = (new Attribute("0", "-1")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_442() throws Exception {
        // Combination: receiver__key="0", receiver__value="1.5"
        Object actual = (new Attribute("0", "1.5")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_443() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775807"
        Object actual = (new Attribute("0", "9223372036854775807")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_444() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775808"
        Object actual = (new Attribute("0", "9223372036854775808")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_445() throws Exception {
        // Combination: receiver__key="0", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_446() throws Exception {
        // Combination: receiver__key="-1", receiver__value=""
        Object actual = (new Attribute("-1", "")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_447() throws Exception {
        // Combination: receiver__key="-1", receiver__value=" "
        Object actual = (new Attribute("-1", " ")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_448() throws Exception {
        // Combination: receiver__key="-1", receiver__value="a"
        Object actual = (new Attribute("-1", "a")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_449() throws Exception {
        // Combination: receiver__key="-1", receiver__value="test123"
        Object actual = (new Attribute("-1", "test123")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_450() throws Exception {
        // Combination: receiver__key="-1", receiver__value="!@#"
        Object actual = (new Attribute("-1", "!@#")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_451() throws Exception {
        // Combination: receiver__key="-1", receiver__value="0"
        Object actual = (new Attribute("-1", "0")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_452() throws Exception {
        // Combination: receiver__key="-1", receiver__value="-1"
        Object actual = (new Attribute("-1", "-1")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_453() throws Exception {
        // Combination: receiver__key="-1", receiver__value="1.5"
        Object actual = (new Attribute("-1", "1.5")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_454() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775807"
        Object actual = (new Attribute("-1", "9223372036854775807")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_455() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775808"
        Object actual = (new Attribute("-1", "9223372036854775808")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_456() throws Exception {
        // Combination: receiver__key="-1", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_457() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=""
        Object actual = (new Attribute("1.5", "")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_458() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=" "
        Object actual = (new Attribute("1.5", " ")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_459() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="a"
        Object actual = (new Attribute("1.5", "a")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_460() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="test123"
        Object actual = (new Attribute("1.5", "test123")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_461() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="!@#"
        Object actual = (new Attribute("1.5", "!@#")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_462() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="0"
        Object actual = (new Attribute("1.5", "0")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_463() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="-1"
        Object actual = (new Attribute("1.5", "-1")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_464() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="1.5"
        Object actual = (new Attribute("1.5", "1.5")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_465() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775807"
        Object actual = (new Attribute("1.5", "9223372036854775807")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_466() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775808"
        Object actual = (new Attribute("1.5", "9223372036854775808")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_467() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_468() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=""
        Object actual = (new Attribute("9223372036854775807", "")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_469() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=" "
        Object actual = (new Attribute("9223372036854775807", " ")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_470() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="a"
        Object actual = (new Attribute("9223372036854775807", "a")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_471() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775807", "test123")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_472() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775807", "!@#")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_473() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="0"
        Object actual = (new Attribute("9223372036854775807", "0")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_474() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775807", "-1")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_475() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775807", "1.5")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_476() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775807")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_477() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775808")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_478() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_479() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=""
        Object actual = (new Attribute("9223372036854775808", "")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_480() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=" "
        Object actual = (new Attribute("9223372036854775808", " ")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_481() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="a"
        Object actual = (new Attribute("9223372036854775808", "a")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_482() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775808", "test123")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_483() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775808", "!@#")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_484() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="0"
        Object actual = (new Attribute("9223372036854775808", "0")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_485() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775808", "-1")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_486() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775808", "1.5")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_487() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775807")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_488() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775808")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_489() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_490() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=""
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_491() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=" "
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_492() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="a"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_493() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="test123"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_494() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="!@#"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_495() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="0"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_496() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="-1"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_497() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="1.5"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_498() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775807"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_499() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775808"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_html_pairwise_500() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).html();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_501() throws Exception {
        // Combination: receiver__key="", receiver__value=""
        try {
            (new Attribute("", "")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_502() throws Exception {
        // Combination: receiver__key="", receiver__value=" "
        try {
            (new Attribute("", " ")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_503() throws Exception {
        // Combination: receiver__key="", receiver__value="a"
        try {
            (new Attribute("", "a")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_504() throws Exception {
        // Combination: receiver__key="", receiver__value="test123"
        try {
            (new Attribute("", "test123")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_505() throws Exception {
        // Combination: receiver__key="", receiver__value="!@#"
        try {
            (new Attribute("", "!@#")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_506() throws Exception {
        // Combination: receiver__key="", receiver__value="0"
        try {
            (new Attribute("", "0")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_507() throws Exception {
        // Combination: receiver__key="", receiver__value="-1"
        try {
            (new Attribute("", "-1")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_508() throws Exception {
        // Combination: receiver__key="", receiver__value="1.5"
        try {
            (new Attribute("", "1.5")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_509() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775807"
        try {
            (new Attribute("", "9223372036854775807")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_510() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775808"
        try {
            (new Attribute("", "9223372036854775808")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_511() throws Exception {
        // Combination: receiver__key="", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_512() throws Exception {
        // Combination: receiver__key=" ", receiver__value=""
        try {
            (new Attribute(" ", "")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_513() throws Exception {
        // Combination: receiver__key=" ", receiver__value=" "
        try {
            (new Attribute(" ", " ")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_514() throws Exception {
        // Combination: receiver__key=" ", receiver__value="a"
        try {
            (new Attribute(" ", "a")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_515() throws Exception {
        // Combination: receiver__key=" ", receiver__value="test123"
        try {
            (new Attribute(" ", "test123")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_516() throws Exception {
        // Combination: receiver__key=" ", receiver__value="!@#"
        try {
            (new Attribute(" ", "!@#")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_517() throws Exception {
        // Combination: receiver__key=" ", receiver__value="0"
        try {
            (new Attribute(" ", "0")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_518() throws Exception {
        // Combination: receiver__key=" ", receiver__value="-1"
        try {
            (new Attribute(" ", "-1")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_519() throws Exception {
        // Combination: receiver__key=" ", receiver__value="1.5"
        try {
            (new Attribute(" ", "1.5")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_520() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775807"
        try {
            (new Attribute(" ", "9223372036854775807")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_521() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775808"
        try {
            (new Attribute(" ", "9223372036854775808")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_522() throws Exception {
        // Combination: receiver__key=" ", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_523() throws Exception {
        // Combination: receiver__key="a", receiver__value=""
        Object actual = (new Attribute("a", "")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_524() throws Exception {
        // Combination: receiver__key="a", receiver__value=" "
        Object actual = (new Attribute("a", " ")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_525() throws Exception {
        // Combination: receiver__key="a", receiver__value="a"
        Object actual = (new Attribute("a", "a")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_526() throws Exception {
        // Combination: receiver__key="a", receiver__value="test123"
        Object actual = (new Attribute("a", "test123")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_527() throws Exception {
        // Combination: receiver__key="a", receiver__value="!@#"
        Object actual = (new Attribute("a", "!@#")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_528() throws Exception {
        // Combination: receiver__key="a", receiver__value="0"
        Object actual = (new Attribute("a", "0")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_529() throws Exception {
        // Combination: receiver__key="a", receiver__value="-1"
        Object actual = (new Attribute("a", "-1")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_530() throws Exception {
        // Combination: receiver__key="a", receiver__value="1.5"
        Object actual = (new Attribute("a", "1.5")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_531() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775807"
        Object actual = (new Attribute("a", "9223372036854775807")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_532() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775808"
        Object actual = (new Attribute("a", "9223372036854775808")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_533() throws Exception {
        // Combination: receiver__key="a", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_534() throws Exception {
        // Combination: receiver__key="test123", receiver__value=""
        Object actual = (new Attribute("test123", "")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_535() throws Exception {
        // Combination: receiver__key="test123", receiver__value=" "
        Object actual = (new Attribute("test123", " ")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_536() throws Exception {
        // Combination: receiver__key="test123", receiver__value="a"
        Object actual = (new Attribute("test123", "a")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_537() throws Exception {
        // Combination: receiver__key="test123", receiver__value="test123"
        Object actual = (new Attribute("test123", "test123")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_538() throws Exception {
        // Combination: receiver__key="test123", receiver__value="!@#"
        Object actual = (new Attribute("test123", "!@#")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_539() throws Exception {
        // Combination: receiver__key="test123", receiver__value="0"
        Object actual = (new Attribute("test123", "0")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_540() throws Exception {
        // Combination: receiver__key="test123", receiver__value="-1"
        Object actual = (new Attribute("test123", "-1")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_541() throws Exception {
        // Combination: receiver__key="test123", receiver__value="1.5"
        Object actual = (new Attribute("test123", "1.5")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_542() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775807"
        Object actual = (new Attribute("test123", "9223372036854775807")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_543() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775808"
        Object actual = (new Attribute("test123", "9223372036854775808")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_544() throws Exception {
        // Combination: receiver__key="test123", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_545() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=""
        Object actual = (new Attribute("!@#", "")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_546() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=" "
        Object actual = (new Attribute("!@#", " ")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_547() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="a"
        Object actual = (new Attribute("!@#", "a")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_548() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="test123"
        Object actual = (new Attribute("!@#", "test123")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_549() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="!@#"
        Object actual = (new Attribute("!@#", "!@#")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_550() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="0"
        Object actual = (new Attribute("!@#", "0")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_551() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="-1"
        Object actual = (new Attribute("!@#", "-1")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_552() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="1.5"
        Object actual = (new Attribute("!@#", "1.5")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_553() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775807"
        Object actual = (new Attribute("!@#", "9223372036854775807")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_554() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775808"
        Object actual = (new Attribute("!@#", "9223372036854775808")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_555() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_556() throws Exception {
        // Combination: receiver__key="0", receiver__value=""
        Object actual = (new Attribute("0", "")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_557() throws Exception {
        // Combination: receiver__key="0", receiver__value=" "
        Object actual = (new Attribute("0", " ")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_558() throws Exception {
        // Combination: receiver__key="0", receiver__value="a"
        Object actual = (new Attribute("0", "a")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_559() throws Exception {
        // Combination: receiver__key="0", receiver__value="test123"
        Object actual = (new Attribute("0", "test123")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_560() throws Exception {
        // Combination: receiver__key="0", receiver__value="!@#"
        Object actual = (new Attribute("0", "!@#")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_561() throws Exception {
        // Combination: receiver__key="0", receiver__value="0"
        Object actual = (new Attribute("0", "0")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_562() throws Exception {
        // Combination: receiver__key="0", receiver__value="-1"
        Object actual = (new Attribute("0", "-1")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_563() throws Exception {
        // Combination: receiver__key="0", receiver__value="1.5"
        Object actual = (new Attribute("0", "1.5")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_564() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775807"
        Object actual = (new Attribute("0", "9223372036854775807")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_565() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775808"
        Object actual = (new Attribute("0", "9223372036854775808")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_566() throws Exception {
        // Combination: receiver__key="0", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_567() throws Exception {
        // Combination: receiver__key="-1", receiver__value=""
        Object actual = (new Attribute("-1", "")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_568() throws Exception {
        // Combination: receiver__key="-1", receiver__value=" "
        Object actual = (new Attribute("-1", " ")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_569() throws Exception {
        // Combination: receiver__key="-1", receiver__value="a"
        Object actual = (new Attribute("-1", "a")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_570() throws Exception {
        // Combination: receiver__key="-1", receiver__value="test123"
        Object actual = (new Attribute("-1", "test123")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_571() throws Exception {
        // Combination: receiver__key="-1", receiver__value="!@#"
        Object actual = (new Attribute("-1", "!@#")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_572() throws Exception {
        // Combination: receiver__key="-1", receiver__value="0"
        Object actual = (new Attribute("-1", "0")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_573() throws Exception {
        // Combination: receiver__key="-1", receiver__value="-1"
        Object actual = (new Attribute("-1", "-1")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_574() throws Exception {
        // Combination: receiver__key="-1", receiver__value="1.5"
        Object actual = (new Attribute("-1", "1.5")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_575() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775807"
        Object actual = (new Attribute("-1", "9223372036854775807")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_576() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775808"
        Object actual = (new Attribute("-1", "9223372036854775808")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_577() throws Exception {
        // Combination: receiver__key="-1", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_578() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=""
        Object actual = (new Attribute("1.5", "")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_579() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=" "
        Object actual = (new Attribute("1.5", " ")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_580() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="a"
        Object actual = (new Attribute("1.5", "a")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_581() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="test123"
        Object actual = (new Attribute("1.5", "test123")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_582() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="!@#"
        Object actual = (new Attribute("1.5", "!@#")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_583() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="0"
        Object actual = (new Attribute("1.5", "0")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_584() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="-1"
        Object actual = (new Attribute("1.5", "-1")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_585() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="1.5"
        Object actual = (new Attribute("1.5", "1.5")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_586() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775807"
        Object actual = (new Attribute("1.5", "9223372036854775807")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_587() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775808"
        Object actual = (new Attribute("1.5", "9223372036854775808")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_588() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_589() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=""
        Object actual = (new Attribute("9223372036854775807", "")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_590() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=" "
        Object actual = (new Attribute("9223372036854775807", " ")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_591() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="a"
        Object actual = (new Attribute("9223372036854775807", "a")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_592() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775807", "test123")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_593() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775807", "!@#")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_594() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="0"
        Object actual = (new Attribute("9223372036854775807", "0")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_595() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775807", "-1")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_596() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775807", "1.5")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_597() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775807")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_598() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775808")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_599() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_600() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=""
        Object actual = (new Attribute("9223372036854775808", "")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_601() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=" "
        Object actual = (new Attribute("9223372036854775808", " ")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_602() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="a"
        Object actual = (new Attribute("9223372036854775808", "a")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_603() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775808", "test123")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_604() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775808", "!@#")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_605() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="0"
        Object actual = (new Attribute("9223372036854775808", "0")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_606() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775808", "-1")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_607() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775808", "1.5")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_608() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775807")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_609() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775808")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_610() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_611() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=""
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_612() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=" "
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\" \"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_613() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="a"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"a\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_614() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="test123"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"test123\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_615() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="!@#"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"!@#\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_616() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="0"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"0\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_617() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="-1"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"-1\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_618() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="1.5"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"1.5\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_619() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775807"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"9223372036854775807\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_620() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775808"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"9223372036854775808\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_621() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_622() throws Exception {
        // Combination: receiver__key="", receiver__value=""
        try {
            (new Attribute("", "")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_623() throws Exception {
        // Combination: receiver__key="", receiver__value=" "
        try {
            (new Attribute("", " ")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_624() throws Exception {
        // Combination: receiver__key="", receiver__value="a"
        try {
            (new Attribute("", "a")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_625() throws Exception {
        // Combination: receiver__key="", receiver__value="test123"
        try {
            (new Attribute("", "test123")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_626() throws Exception {
        // Combination: receiver__key="", receiver__value="!@#"
        try {
            (new Attribute("", "!@#")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_627() throws Exception {
        // Combination: receiver__key="", receiver__value="0"
        try {
            (new Attribute("", "0")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_628() throws Exception {
        // Combination: receiver__key="", receiver__value="-1"
        try {
            (new Attribute("", "-1")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_629() throws Exception {
        // Combination: receiver__key="", receiver__value="1.5"
        try {
            (new Attribute("", "1.5")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_630() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775807"
        try {
            (new Attribute("", "9223372036854775807")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_631() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775808"
        try {
            (new Attribute("", "9223372036854775808")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_632() throws Exception {
        // Combination: receiver__key="", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_633() throws Exception {
        // Combination: receiver__key=" ", receiver__value=""
        try {
            (new Attribute(" ", "")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_634() throws Exception {
        // Combination: receiver__key=" ", receiver__value=" "
        try {
            (new Attribute(" ", " ")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_635() throws Exception {
        // Combination: receiver__key=" ", receiver__value="a"
        try {
            (new Attribute(" ", "a")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_636() throws Exception {
        // Combination: receiver__key=" ", receiver__value="test123"
        try {
            (new Attribute(" ", "test123")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_637() throws Exception {
        // Combination: receiver__key=" ", receiver__value="!@#"
        try {
            (new Attribute(" ", "!@#")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_638() throws Exception {
        // Combination: receiver__key=" ", receiver__value="0"
        try {
            (new Attribute(" ", "0")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_639() throws Exception {
        // Combination: receiver__key=" ", receiver__value="-1"
        try {
            (new Attribute(" ", "-1")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_640() throws Exception {
        // Combination: receiver__key=" ", receiver__value="1.5"
        try {
            (new Attribute(" ", "1.5")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_641() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775807"
        try {
            (new Attribute(" ", "9223372036854775807")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_642() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775808"
        try {
            (new Attribute(" ", "9223372036854775808")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_643() throws Exception {
        // Combination: receiver__key=" ", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isDataAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_644() throws Exception {
        // Combination: receiver__key="a", receiver__value=""
        Object actual = (new Attribute("a", "")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_645() throws Exception {
        // Combination: receiver__key="a", receiver__value=" "
        Object actual = (new Attribute("a", " ")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_646() throws Exception {
        // Combination: receiver__key="a", receiver__value="a"
        Object actual = (new Attribute("a", "a")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_647() throws Exception {
        // Combination: receiver__key="a", receiver__value="test123"
        Object actual = (new Attribute("a", "test123")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_648() throws Exception {
        // Combination: receiver__key="a", receiver__value="!@#"
        Object actual = (new Attribute("a", "!@#")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_649() throws Exception {
        // Combination: receiver__key="a", receiver__value="0"
        Object actual = (new Attribute("a", "0")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_650() throws Exception {
        // Combination: receiver__key="a", receiver__value="-1"
        Object actual = (new Attribute("a", "-1")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_651() throws Exception {
        // Combination: receiver__key="a", receiver__value="1.5"
        Object actual = (new Attribute("a", "1.5")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_652() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775807"
        Object actual = (new Attribute("a", "9223372036854775807")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_653() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775808"
        Object actual = (new Attribute("a", "9223372036854775808")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_654() throws Exception {
        // Combination: receiver__key="a", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_655() throws Exception {
        // Combination: receiver__key="test123", receiver__value=""
        Object actual = (new Attribute("test123", "")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_656() throws Exception {
        // Combination: receiver__key="test123", receiver__value=" "
        Object actual = (new Attribute("test123", " ")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_657() throws Exception {
        // Combination: receiver__key="test123", receiver__value="a"
        Object actual = (new Attribute("test123", "a")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_658() throws Exception {
        // Combination: receiver__key="test123", receiver__value="test123"
        Object actual = (new Attribute("test123", "test123")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_659() throws Exception {
        // Combination: receiver__key="test123", receiver__value="!@#"
        Object actual = (new Attribute("test123", "!@#")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_660() throws Exception {
        // Combination: receiver__key="test123", receiver__value="0"
        Object actual = (new Attribute("test123", "0")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_661() throws Exception {
        // Combination: receiver__key="test123", receiver__value="-1"
        Object actual = (new Attribute("test123", "-1")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_662() throws Exception {
        // Combination: receiver__key="test123", receiver__value="1.5"
        Object actual = (new Attribute("test123", "1.5")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_663() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775807"
        Object actual = (new Attribute("test123", "9223372036854775807")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_664() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775808"
        Object actual = (new Attribute("test123", "9223372036854775808")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_665() throws Exception {
        // Combination: receiver__key="test123", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_666() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=""
        Object actual = (new Attribute("!@#", "")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_667() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=" "
        Object actual = (new Attribute("!@#", " ")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_668() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="a"
        Object actual = (new Attribute("!@#", "a")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_669() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="test123"
        Object actual = (new Attribute("!@#", "test123")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_670() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="!@#"
        Object actual = (new Attribute("!@#", "!@#")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_671() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="0"
        Object actual = (new Attribute("!@#", "0")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_672() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="-1"
        Object actual = (new Attribute("!@#", "-1")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_673() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="1.5"
        Object actual = (new Attribute("!@#", "1.5")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_674() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775807"
        Object actual = (new Attribute("!@#", "9223372036854775807")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_675() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775808"
        Object actual = (new Attribute("!@#", "9223372036854775808")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_676() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_677() throws Exception {
        // Combination: receiver__key="0", receiver__value=""
        Object actual = (new Attribute("0", "")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_678() throws Exception {
        // Combination: receiver__key="0", receiver__value=" "
        Object actual = (new Attribute("0", " ")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_679() throws Exception {
        // Combination: receiver__key="0", receiver__value="a"
        Object actual = (new Attribute("0", "a")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_680() throws Exception {
        // Combination: receiver__key="0", receiver__value="test123"
        Object actual = (new Attribute("0", "test123")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_681() throws Exception {
        // Combination: receiver__key="0", receiver__value="!@#"
        Object actual = (new Attribute("0", "!@#")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_682() throws Exception {
        // Combination: receiver__key="0", receiver__value="0"
        Object actual = (new Attribute("0", "0")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_683() throws Exception {
        // Combination: receiver__key="0", receiver__value="-1"
        Object actual = (new Attribute("0", "-1")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_684() throws Exception {
        // Combination: receiver__key="0", receiver__value="1.5"
        Object actual = (new Attribute("0", "1.5")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_685() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775807"
        Object actual = (new Attribute("0", "9223372036854775807")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_686() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775808"
        Object actual = (new Attribute("0", "9223372036854775808")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_687() throws Exception {
        // Combination: receiver__key="0", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_688() throws Exception {
        // Combination: receiver__key="-1", receiver__value=""
        Object actual = (new Attribute("-1", "")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_689() throws Exception {
        // Combination: receiver__key="-1", receiver__value=" "
        Object actual = (new Attribute("-1", " ")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_690() throws Exception {
        // Combination: receiver__key="-1", receiver__value="a"
        Object actual = (new Attribute("-1", "a")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_691() throws Exception {
        // Combination: receiver__key="-1", receiver__value="test123"
        Object actual = (new Attribute("-1", "test123")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_692() throws Exception {
        // Combination: receiver__key="-1", receiver__value="!@#"
        Object actual = (new Attribute("-1", "!@#")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_693() throws Exception {
        // Combination: receiver__key="-1", receiver__value="0"
        Object actual = (new Attribute("-1", "0")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_694() throws Exception {
        // Combination: receiver__key="-1", receiver__value="-1"
        Object actual = (new Attribute("-1", "-1")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_695() throws Exception {
        // Combination: receiver__key="-1", receiver__value="1.5"
        Object actual = (new Attribute("-1", "1.5")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_696() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775807"
        Object actual = (new Attribute("-1", "9223372036854775807")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_697() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775808"
        Object actual = (new Attribute("-1", "9223372036854775808")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_698() throws Exception {
        // Combination: receiver__key="-1", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_699() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=""
        Object actual = (new Attribute("1.5", "")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_700() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=" "
        Object actual = (new Attribute("1.5", " ")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_701() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="a"
        Object actual = (new Attribute("1.5", "a")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_702() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="test123"
        Object actual = (new Attribute("1.5", "test123")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_703() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="!@#"
        Object actual = (new Attribute("1.5", "!@#")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_704() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="0"
        Object actual = (new Attribute("1.5", "0")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_705() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="-1"
        Object actual = (new Attribute("1.5", "-1")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_706() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="1.5"
        Object actual = (new Attribute("1.5", "1.5")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_707() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775807"
        Object actual = (new Attribute("1.5", "9223372036854775807")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_708() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775808"
        Object actual = (new Attribute("1.5", "9223372036854775808")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_709() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_710() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=""
        Object actual = (new Attribute("9223372036854775807", "")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_711() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=" "
        Object actual = (new Attribute("9223372036854775807", " ")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_712() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="a"
        Object actual = (new Attribute("9223372036854775807", "a")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_713() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775807", "test123")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_714() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775807", "!@#")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_715() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="0"
        Object actual = (new Attribute("9223372036854775807", "0")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_716() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775807", "-1")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_717() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775807", "1.5")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_718() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775807")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_719() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775808")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_720() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_721() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=""
        Object actual = (new Attribute("9223372036854775808", "")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_722() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=" "
        Object actual = (new Attribute("9223372036854775808", " ")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_723() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="a"
        Object actual = (new Attribute("9223372036854775808", "a")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_724() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775808", "test123")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_725() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775808", "!@#")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_726() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="0"
        Object actual = (new Attribute("9223372036854775808", "0")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_727() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775808", "-1")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_728() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775808", "1.5")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_729() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775807")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_730() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775808")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_731() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_732() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=""
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_733() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=" "
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_734() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="a"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_735() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="test123"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_736() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="!@#"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_737() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="0"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_738() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="-1"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_739() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="1.5"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_740() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775807"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_741() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775808"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isDataAttribute_pairwise_742() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isDataAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_743() throws Exception {
        // Combination: receiver__key="", receiver__value=""
        try {
            (new Attribute("", "")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_744() throws Exception {
        // Combination: receiver__key="", receiver__value=" "
        try {
            (new Attribute("", " ")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_745() throws Exception {
        // Combination: receiver__key="", receiver__value="a"
        try {
            (new Attribute("", "a")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_746() throws Exception {
        // Combination: receiver__key="", receiver__value="test123"
        try {
            (new Attribute("", "test123")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_747() throws Exception {
        // Combination: receiver__key="", receiver__value="!@#"
        try {
            (new Attribute("", "!@#")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_748() throws Exception {
        // Combination: receiver__key="", receiver__value="0"
        try {
            (new Attribute("", "0")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_749() throws Exception {
        // Combination: receiver__key="", receiver__value="-1"
        try {
            (new Attribute("", "-1")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_750() throws Exception {
        // Combination: receiver__key="", receiver__value="1.5"
        try {
            (new Attribute("", "1.5")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_751() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775807"
        try {
            (new Attribute("", "9223372036854775807")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_752() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775808"
        try {
            (new Attribute("", "9223372036854775808")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_753() throws Exception {
        // Combination: receiver__key="", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_754() throws Exception {
        // Combination: receiver__key=" ", receiver__value=""
        try {
            (new Attribute(" ", "")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_755() throws Exception {
        // Combination: receiver__key=" ", receiver__value=" "
        try {
            (new Attribute(" ", " ")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_756() throws Exception {
        // Combination: receiver__key=" ", receiver__value="a"
        try {
            (new Attribute(" ", "a")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_757() throws Exception {
        // Combination: receiver__key=" ", receiver__value="test123"
        try {
            (new Attribute(" ", "test123")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_758() throws Exception {
        // Combination: receiver__key=" ", receiver__value="!@#"
        try {
            (new Attribute(" ", "!@#")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_759() throws Exception {
        // Combination: receiver__key=" ", receiver__value="0"
        try {
            (new Attribute(" ", "0")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_760() throws Exception {
        // Combination: receiver__key=" ", receiver__value="-1"
        try {
            (new Attribute(" ", "-1")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_761() throws Exception {
        // Combination: receiver__key=" ", receiver__value="1.5"
        try {
            (new Attribute(" ", "1.5")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_762() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775807"
        try {
            (new Attribute(" ", "9223372036854775807")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_763() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775808"
        try {
            (new Attribute(" ", "9223372036854775808")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_764() throws Exception {
        // Combination: receiver__key=" ", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isBooleanAttribute();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_765() throws Exception {
        // Combination: receiver__key="a", receiver__value=""
        Object actual = (new Attribute("a", "")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_766() throws Exception {
        // Combination: receiver__key="a", receiver__value=" "
        Object actual = (new Attribute("a", " ")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_767() throws Exception {
        // Combination: receiver__key="a", receiver__value="a"
        Object actual = (new Attribute("a", "a")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_768() throws Exception {
        // Combination: receiver__key="a", receiver__value="test123"
        Object actual = (new Attribute("a", "test123")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_769() throws Exception {
        // Combination: receiver__key="a", receiver__value="!@#"
        Object actual = (new Attribute("a", "!@#")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_770() throws Exception {
        // Combination: receiver__key="a", receiver__value="0"
        Object actual = (new Attribute("a", "0")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_771() throws Exception {
        // Combination: receiver__key="a", receiver__value="-1"
        Object actual = (new Attribute("a", "-1")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_772() throws Exception {
        // Combination: receiver__key="a", receiver__value="1.5"
        Object actual = (new Attribute("a", "1.5")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_773() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775807"
        Object actual = (new Attribute("a", "9223372036854775807")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_774() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775808"
        Object actual = (new Attribute("a", "9223372036854775808")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_775() throws Exception {
        // Combination: receiver__key="a", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_776() throws Exception {
        // Combination: receiver__key="test123", receiver__value=""
        Object actual = (new Attribute("test123", "")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_777() throws Exception {
        // Combination: receiver__key="test123", receiver__value=" "
        Object actual = (new Attribute("test123", " ")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_778() throws Exception {
        // Combination: receiver__key="test123", receiver__value="a"
        Object actual = (new Attribute("test123", "a")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_779() throws Exception {
        // Combination: receiver__key="test123", receiver__value="test123"
        Object actual = (new Attribute("test123", "test123")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_780() throws Exception {
        // Combination: receiver__key="test123", receiver__value="!@#"
        Object actual = (new Attribute("test123", "!@#")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_781() throws Exception {
        // Combination: receiver__key="test123", receiver__value="0"
        Object actual = (new Attribute("test123", "0")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_782() throws Exception {
        // Combination: receiver__key="test123", receiver__value="-1"
        Object actual = (new Attribute("test123", "-1")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_783() throws Exception {
        // Combination: receiver__key="test123", receiver__value="1.5"
        Object actual = (new Attribute("test123", "1.5")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_784() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775807"
        Object actual = (new Attribute("test123", "9223372036854775807")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_785() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775808"
        Object actual = (new Attribute("test123", "9223372036854775808")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_786() throws Exception {
        // Combination: receiver__key="test123", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_787() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=""
        Object actual = (new Attribute("!@#", "")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_788() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=" "
        Object actual = (new Attribute("!@#", " ")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_789() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="a"
        Object actual = (new Attribute("!@#", "a")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_790() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="test123"
        Object actual = (new Attribute("!@#", "test123")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_791() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="!@#"
        Object actual = (new Attribute("!@#", "!@#")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_792() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="0"
        Object actual = (new Attribute("!@#", "0")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_793() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="-1"
        Object actual = (new Attribute("!@#", "-1")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_794() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="1.5"
        Object actual = (new Attribute("!@#", "1.5")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_795() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775807"
        Object actual = (new Attribute("!@#", "9223372036854775807")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_796() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775808"
        Object actual = (new Attribute("!@#", "9223372036854775808")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_797() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_798() throws Exception {
        // Combination: receiver__key="0", receiver__value=""
        Object actual = (new Attribute("0", "")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_799() throws Exception {
        // Combination: receiver__key="0", receiver__value=" "
        Object actual = (new Attribute("0", " ")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_800() throws Exception {
        // Combination: receiver__key="0", receiver__value="a"
        Object actual = (new Attribute("0", "a")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_801() throws Exception {
        // Combination: receiver__key="0", receiver__value="test123"
        Object actual = (new Attribute("0", "test123")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_802() throws Exception {
        // Combination: receiver__key="0", receiver__value="!@#"
        Object actual = (new Attribute("0", "!@#")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_803() throws Exception {
        // Combination: receiver__key="0", receiver__value="0"
        Object actual = (new Attribute("0", "0")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_804() throws Exception {
        // Combination: receiver__key="0", receiver__value="-1"
        Object actual = (new Attribute("0", "-1")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_805() throws Exception {
        // Combination: receiver__key="0", receiver__value="1.5"
        Object actual = (new Attribute("0", "1.5")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_806() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775807"
        Object actual = (new Attribute("0", "9223372036854775807")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_807() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775808"
        Object actual = (new Attribute("0", "9223372036854775808")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_808() throws Exception {
        // Combination: receiver__key="0", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_809() throws Exception {
        // Combination: receiver__key="-1", receiver__value=""
        Object actual = (new Attribute("-1", "")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_810() throws Exception {
        // Combination: receiver__key="-1", receiver__value=" "
        Object actual = (new Attribute("-1", " ")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_811() throws Exception {
        // Combination: receiver__key="-1", receiver__value="a"
        Object actual = (new Attribute("-1", "a")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_812() throws Exception {
        // Combination: receiver__key="-1", receiver__value="test123"
        Object actual = (new Attribute("-1", "test123")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_813() throws Exception {
        // Combination: receiver__key="-1", receiver__value="!@#"
        Object actual = (new Attribute("-1", "!@#")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_814() throws Exception {
        // Combination: receiver__key="-1", receiver__value="0"
        Object actual = (new Attribute("-1", "0")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_815() throws Exception {
        // Combination: receiver__key="-1", receiver__value="-1"
        Object actual = (new Attribute("-1", "-1")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_816() throws Exception {
        // Combination: receiver__key="-1", receiver__value="1.5"
        Object actual = (new Attribute("-1", "1.5")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_817() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775807"
        Object actual = (new Attribute("-1", "9223372036854775807")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_818() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775808"
        Object actual = (new Attribute("-1", "9223372036854775808")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_819() throws Exception {
        // Combination: receiver__key="-1", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_820() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=""
        Object actual = (new Attribute("1.5", "")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_821() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=" "
        Object actual = (new Attribute("1.5", " ")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_822() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="a"
        Object actual = (new Attribute("1.5", "a")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_823() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="test123"
        Object actual = (new Attribute("1.5", "test123")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_824() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="!@#"
        Object actual = (new Attribute("1.5", "!@#")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_825() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="0"
        Object actual = (new Attribute("1.5", "0")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_826() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="-1"
        Object actual = (new Attribute("1.5", "-1")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_827() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="1.5"
        Object actual = (new Attribute("1.5", "1.5")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_828() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775807"
        Object actual = (new Attribute("1.5", "9223372036854775807")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_829() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775808"
        Object actual = (new Attribute("1.5", "9223372036854775808")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_830() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_831() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=""
        Object actual = (new Attribute("9223372036854775807", "")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_832() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=" "
        Object actual = (new Attribute("9223372036854775807", " ")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_833() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="a"
        Object actual = (new Attribute("9223372036854775807", "a")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_834() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775807", "test123")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_835() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775807", "!@#")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_836() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="0"
        Object actual = (new Attribute("9223372036854775807", "0")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_837() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775807", "-1")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_838() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775807", "1.5")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_839() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775807")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_840() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775808")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_841() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_842() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=""
        Object actual = (new Attribute("9223372036854775808", "")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_843() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=" "
        Object actual = (new Attribute("9223372036854775808", " ")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_844() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="a"
        Object actual = (new Attribute("9223372036854775808", "a")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_845() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775808", "test123")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_846() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775808", "!@#")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_847() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="0"
        Object actual = (new Attribute("9223372036854775808", "0")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_848() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775808", "-1")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_849() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775808", "1.5")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_850() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775807")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_851() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775808")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_852() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_853() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=""
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_854() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=" "
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_855() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="a"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_856() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="test123"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_857() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="!@#"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_858() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="0"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_859() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="-1"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_860() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="1.5"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_861() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775807"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_862() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775808"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isBooleanAttribute_pairwise_863() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).isBooleanAttribute();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_864() throws Exception {
        // Combination: receiver__key="", receiver__value="", o=new Object()
        try {
            (new Attribute("", "")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_865() throws Exception {
        // Combination: receiver__key=" ", receiver__value=" ", o=new Object()
        try {
            (new Attribute(" ", " ")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_866() throws Exception {
        // Combination: receiver__key="a", receiver__value="a", o=new Object()
        Object actual = (new Attribute("a", "a")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_867() throws Exception {
        // Combination: receiver__key="test123", receiver__value="test123", o=new Object()
        Object actual = (new Attribute("test123", "test123")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_868() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="!@#", o=new Object()
        Object actual = (new Attribute("!@#", "!@#")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_869() throws Exception {
        // Combination: receiver__key="0", receiver__value="0", o=new Object()
        Object actual = (new Attribute("0", "0")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_870() throws Exception {
        // Combination: receiver__key="-1", receiver__value="-1", o=new Object()
        Object actual = (new Attribute("-1", "-1")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_871() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="1.5", o=new Object()
        Object actual = (new Attribute("1.5", "1.5")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_872() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775807", o=new Object()
        Object actual = (new Attribute("9223372036854775807", "9223372036854775807")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_873() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775808", o=new Object()
        Object actual = (new Attribute("9223372036854775808", "9223372036854775808")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_874() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o=new Object()
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_875() throws Exception {
        // Combination: receiver__key="", receiver__value=" ", o="sample_str"
        try {
            (new Attribute("", " ")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_876() throws Exception {
        // Combination: receiver__key=" ", receiver__value="", o="sample_str"
        try {
            (new Attribute(" ", "")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_877() throws Exception {
        // Combination: receiver__key="a", receiver__value="test123", o="sample_str"
        Object actual = (new Attribute("a", "test123")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_878() throws Exception {
        // Combination: receiver__key="test123", receiver__value="a", o="sample_str"
        Object actual = (new Attribute("test123", "a")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_879() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="0", o="sample_str"
        Object actual = (new Attribute("!@#", "0")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_880() throws Exception {
        // Combination: receiver__key="0", receiver__value="!@#", o="sample_str"
        Object actual = (new Attribute("0", "!@#")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_881() throws Exception {
        // Combination: receiver__key="-1", receiver__value="1.5", o="sample_str"
        Object actual = (new Attribute("-1", "1.5")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_882() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="-1", o="sample_str"
        Object actual = (new Attribute("1.5", "-1")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_883() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775808", o="sample_str"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775808")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_884() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775807", o="sample_str"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775807")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_885() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="", o="sample_str"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_886() throws Exception {
        // Combination: receiver__key="", receiver__value="a", o=Integer.valueOf(1)
        try {
            (new Attribute("", "a")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_887() throws Exception {
        // Combination: receiver__key=" ", receiver__value="test123", o=Integer.valueOf(1)
        try {
            (new Attribute(" ", "test123")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_888() throws Exception {
        // Combination: receiver__key="a", receiver__value="", o=Integer.valueOf(1)
        Object actual = (new Attribute("a", "")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_889() throws Exception {
        // Combination: receiver__key="test123", receiver__value=" ", o=Integer.valueOf(1)
        Object actual = (new Attribute("test123", " ")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_890() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="-1", o=Integer.valueOf(1)
        Object actual = (new Attribute("!@#", "-1")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_891() throws Exception {
        // Combination: receiver__key="0", receiver__value="1.5", o=Integer.valueOf(1)
        Object actual = (new Attribute("0", "1.5")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_892() throws Exception {
        // Combination: receiver__key="-1", receiver__value="!@#", o=Integer.valueOf(1)
        Object actual = (new Attribute("-1", "!@#")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_893() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="0", o=Integer.valueOf(1)
        Object actual = (new Attribute("1.5", "0")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_894() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o=Integer.valueOf(1)
        Object actual = (new Attribute("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_895() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="", o=Integer.valueOf(1)
        Object actual = (new Attribute("9223372036854775808", "")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_896() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775807", o=Integer.valueOf(1)
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_897() throws Exception {
        // Combination: receiver__key="test123", receiver__value="", o=new Object()
        Object actual = (new Attribute("test123", "")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_898() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="", o=new Object()
        Object actual = (new Attribute("!@#", "")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_899() throws Exception {
        // Combination: receiver__key="0", receiver__value="", o=new Object()
        Object actual = (new Attribute("0", "")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_900() throws Exception {
        // Combination: receiver__key="-1", receiver__value="", o=new Object()
        Object actual = (new Attribute("-1", "")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_901() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="", o=new Object()
        Object actual = (new Attribute("1.5", "")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_902() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="", o=new Object()
        Object actual = (new Attribute("9223372036854775807", "")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_903() throws Exception {
        // Combination: receiver__key="a", receiver__value=" ", o=new Object()
        Object actual = (new Attribute("a", " ")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_904() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=" ", o=new Object()
        Object actual = (new Attribute("!@#", " ")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_905() throws Exception {
        // Combination: receiver__key="0", receiver__value=" ", o=new Object()
        Object actual = (new Attribute("0", " ")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_906() throws Exception {
        // Combination: receiver__key="-1", receiver__value=" ", o=new Object()
        Object actual = (new Attribute("-1", " ")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_907() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=" ", o=new Object()
        Object actual = (new Attribute("1.5", " ")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_908() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=" ", o=new Object()
        Object actual = (new Attribute("9223372036854775807", " ")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_909() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=" ", o=new Object()
        Object actual = (new Attribute("9223372036854775808", " ")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_910() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=" ", o=new Object()
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_911() throws Exception {
        // Combination: receiver__key=" ", receiver__value="a", o=new Object()
        try {
            (new Attribute(" ", "a")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_912() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="a", o=new Object()
        Object actual = (new Attribute("!@#", "a")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_913() throws Exception {
        // Combination: receiver__key="0", receiver__value="a", o=new Object()
        Object actual = (new Attribute("0", "a")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_914() throws Exception {
        // Combination: receiver__key="-1", receiver__value="a", o=new Object()
        Object actual = (new Attribute("-1", "a")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_915() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="a", o=new Object()
        Object actual = (new Attribute("1.5", "a")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_916() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="a", o=new Object()
        Object actual = (new Attribute("9223372036854775807", "a")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_917() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="a", o=new Object()
        Object actual = (new Attribute("9223372036854775808", "a")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_918() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="a", o=new Object()
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_919() throws Exception {
        // Combination: receiver__key="", receiver__value="test123", o=new Object()
        try {
            (new Attribute("", "test123")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_920() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="test123", o=new Object()
        Object actual = (new Attribute("!@#", "test123")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_921() throws Exception {
        // Combination: receiver__key="0", receiver__value="test123", o=new Object()
        Object actual = (new Attribute("0", "test123")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_922() throws Exception {
        // Combination: receiver__key="-1", receiver__value="test123", o=new Object()
        Object actual = (new Attribute("-1", "test123")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_923() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="test123", o=new Object()
        Object actual = (new Attribute("1.5", "test123")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_924() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="test123", o=new Object()
        Object actual = (new Attribute("9223372036854775807", "test123")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_925() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="test123", o=new Object()
        Object actual = (new Attribute("9223372036854775808", "test123")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_926() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="test123", o=new Object()
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_927() throws Exception {
        // Combination: receiver__key="", receiver__value="!@#", o=new Object()
        try {
            (new Attribute("", "!@#")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_928() throws Exception {
        // Combination: receiver__key=" ", receiver__value="!@#", o=new Object()
        try {
            (new Attribute(" ", "!@#")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_929() throws Exception {
        // Combination: receiver__key="a", receiver__value="!@#", o=new Object()
        Object actual = (new Attribute("a", "!@#")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_930() throws Exception {
        // Combination: receiver__key="test123", receiver__value="!@#", o=new Object()
        Object actual = (new Attribute("test123", "!@#")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_931() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="!@#", o=new Object()
        Object actual = (new Attribute("1.5", "!@#")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_932() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="!@#", o=new Object()
        Object actual = (new Attribute("9223372036854775807", "!@#")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_933() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="!@#", o=new Object()
        Object actual = (new Attribute("9223372036854775808", "!@#")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_934() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="!@#", o=new Object()
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_935() throws Exception {
        // Combination: receiver__key="", receiver__value="0", o=new Object()
        try {
            (new Attribute("", "0")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_936() throws Exception {
        // Combination: receiver__key=" ", receiver__value="0", o=new Object()
        try {
            (new Attribute(" ", "0")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_937() throws Exception {
        // Combination: receiver__key="a", receiver__value="0", o=new Object()
        Object actual = (new Attribute("a", "0")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_938() throws Exception {
        // Combination: receiver__key="test123", receiver__value="0", o=new Object()
        Object actual = (new Attribute("test123", "0")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_939() throws Exception {
        // Combination: receiver__key="-1", receiver__value="0", o=new Object()
        Object actual = (new Attribute("-1", "0")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_940() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="0", o=new Object()
        Object actual = (new Attribute("9223372036854775807", "0")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_941() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="0", o=new Object()
        Object actual = (new Attribute("9223372036854775808", "0")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_942() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="0", o=new Object()
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_943() throws Exception {
        // Combination: receiver__key="", receiver__value="-1", o=new Object()
        try {
            (new Attribute("", "-1")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_944() throws Exception {
        // Combination: receiver__key=" ", receiver__value="-1", o=new Object()
        try {
            (new Attribute(" ", "-1")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_945() throws Exception {
        // Combination: receiver__key="a", receiver__value="-1", o=new Object()
        Object actual = (new Attribute("a", "-1")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_946() throws Exception {
        // Combination: receiver__key="test123", receiver__value="-1", o=new Object()
        Object actual = (new Attribute("test123", "-1")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_947() throws Exception {
        // Combination: receiver__key="0", receiver__value="-1", o=new Object()
        Object actual = (new Attribute("0", "-1")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_948() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="-1", o=new Object()
        Object actual = (new Attribute("9223372036854775807", "-1")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_949() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="-1", o=new Object()
        Object actual = (new Attribute("9223372036854775808", "-1")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_950() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="-1", o=new Object()
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_951() throws Exception {
        // Combination: receiver__key="", receiver__value="1.5", o=new Object()
        try {
            (new Attribute("", "1.5")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_952() throws Exception {
        // Combination: receiver__key=" ", receiver__value="1.5", o=new Object()
        try {
            (new Attribute(" ", "1.5")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_953() throws Exception {
        // Combination: receiver__key="a", receiver__value="1.5", o=new Object()
        Object actual = (new Attribute("a", "1.5")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_954() throws Exception {
        // Combination: receiver__key="test123", receiver__value="1.5", o=new Object()
        Object actual = (new Attribute("test123", "1.5")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_955() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="1.5", o=new Object()
        Object actual = (new Attribute("!@#", "1.5")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_956() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="1.5", o=new Object()
        Object actual = (new Attribute("9223372036854775807", "1.5")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_957() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="1.5", o=new Object()
        Object actual = (new Attribute("9223372036854775808", "1.5")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_958() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="1.5", o=new Object()
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_959() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775807", o=new Object()
        try {
            (new Attribute("", "9223372036854775807")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_960() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775807", o=new Object()
        try {
            (new Attribute(" ", "9223372036854775807")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_961() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775807", o=new Object()
        Object actual = (new Attribute("a", "9223372036854775807")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_962() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775807", o=new Object()
        Object actual = (new Attribute("test123", "9223372036854775807")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_963() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775807", o=new Object()
        Object actual = (new Attribute("!@#", "9223372036854775807")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_964() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775807", o=new Object()
        Object actual = (new Attribute("0", "9223372036854775807")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_965() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775807", o=new Object()
        Object actual = (new Attribute("-1", "9223372036854775807")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_966() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775807", o=new Object()
        Object actual = (new Attribute("1.5", "9223372036854775807")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_967() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775808", o=Integer.valueOf(1)
        try {
            (new Attribute("", "9223372036854775808")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_968() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775808", o=new Object()
        try {
            (new Attribute(" ", "9223372036854775808")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_969() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775808", o=new Object()
        Object actual = (new Attribute("a", "9223372036854775808")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_970() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775808", o=new Object()
        Object actual = (new Attribute("test123", "9223372036854775808")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_971() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775808", o=new Object()
        Object actual = (new Attribute("!@#", "9223372036854775808")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_972() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775808", o=new Object()
        Object actual = (new Attribute("0", "9223372036854775808")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_973() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775808", o=new Object()
        Object actual = (new Attribute("-1", "9223372036854775808")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_974() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775808", o=new Object()
        Object actual = (new Attribute("1.5", "9223372036854775808")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_975() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775808", o=new Object()
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_976() throws Exception {
        // Combination: receiver__key="", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o="sample_str"
        try {
            (new Attribute("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_977() throws Exception {
        // Combination: receiver__key=" ", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o=new Object()
        try {
            (new Attribute(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_978() throws Exception {
        // Combination: receiver__key="a", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o=new Object()
        Object actual = (new Attribute("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_979() throws Exception {
        // Combination: receiver__key="test123", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o=new Object()
        Object actual = (new Attribute("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_980() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o=new Object()
        Object actual = (new Attribute("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_981() throws Exception {
        // Combination: receiver__key="0", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o=new Object()
        Object actual = (new Attribute("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_982() throws Exception {
        // Combination: receiver__key="-1", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o=new Object()
        Object actual = (new Attribute("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_983() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o=new Object()
        Object actual = (new Attribute("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_984() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o=new Object()
        Object actual = (new Attribute("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_985() throws Exception {
        // Combination: receiver__key="", receiver__value=""
        try {
            (new Attribute("", "")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_986() throws Exception {
        // Combination: receiver__key="", receiver__value=" "
        try {
            (new Attribute("", " ")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_987() throws Exception {
        // Combination: receiver__key="", receiver__value="a"
        try {
            (new Attribute("", "a")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_988() throws Exception {
        // Combination: receiver__key="", receiver__value="test123"
        try {
            (new Attribute("", "test123")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_989() throws Exception {
        // Combination: receiver__key="", receiver__value="!@#"
        try {
            (new Attribute("", "!@#")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_990() throws Exception {
        // Combination: receiver__key="", receiver__value="0"
        try {
            (new Attribute("", "0")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_991() throws Exception {
        // Combination: receiver__key="", receiver__value="-1"
        try {
            (new Attribute("", "-1")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_992() throws Exception {
        // Combination: receiver__key="", receiver__value="1.5"
        try {
            (new Attribute("", "1.5")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_993() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775807"
        try {
            (new Attribute("", "9223372036854775807")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_994() throws Exception {
        // Combination: receiver__key="", receiver__value="9223372036854775808"
        try {
            (new Attribute("", "9223372036854775808")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_995() throws Exception {
        // Combination: receiver__key="", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_996() throws Exception {
        // Combination: receiver__key=" ", receiver__value=""
        try {
            (new Attribute(" ", "")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_997() throws Exception {
        // Combination: receiver__key=" ", receiver__value=" "
        try {
            (new Attribute(" ", " ")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_998() throws Exception {
        // Combination: receiver__key=" ", receiver__value="a"
        try {
            (new Attribute(" ", "a")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_999() throws Exception {
        // Combination: receiver__key=" ", receiver__value="test123"
        try {
            (new Attribute(" ", "test123")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1000() throws Exception {
        // Combination: receiver__key=" ", receiver__value="!@#"
        try {
            (new Attribute(" ", "!@#")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1001() throws Exception {
        // Combination: receiver__key=" ", receiver__value="0"
        try {
            (new Attribute(" ", "0")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1002() throws Exception {
        // Combination: receiver__key=" ", receiver__value="-1"
        try {
            (new Attribute(" ", "-1")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1003() throws Exception {
        // Combination: receiver__key=" ", receiver__value="1.5"
        try {
            (new Attribute(" ", "1.5")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1004() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775807"
        try {
            (new Attribute(" ", "9223372036854775807")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1005() throws Exception {
        // Combination: receiver__key=" ", receiver__value="9223372036854775808"
        try {
            (new Attribute(" ", "9223372036854775808")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1006() throws Exception {
        // Combination: receiver__key=" ", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Attribute(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1007() throws Exception {
        // Combination: receiver__key="a", receiver__value=""
        Object actual = (new Attribute("a", "")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("3007", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1008() throws Exception {
        // Combination: receiver__key="a", receiver__value=" "
        Object actual = (new Attribute("a", " ")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("3039", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1009() throws Exception {
        // Combination: receiver__key="a", receiver__value="a"
        Object actual = (new Attribute("a", "a")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("3104", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1010() throws Exception {
        // Combination: receiver__key="a", receiver__value="test123"
        Object actual = (new Attribute("a", "test123")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1422498785", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1011() throws Exception {
        // Combination: receiver__key="a", receiver__value="!@#"
        Object actual = (new Attribute("a", "!@#")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("36739", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1012() throws Exception {
        // Combination: receiver__key="a", receiver__value="0"
        Object actual = (new Attribute("a", "0")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("3055", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1013() throws Exception {
        // Combination: receiver__key="a", receiver__value="-1"
        Object actual = (new Attribute("a", "-1")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("4451", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1014() throws Exception {
        // Combination: receiver__key="a", receiver__value="1.5"
        Object actual = (new Attribute("a", "1.5")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("51575", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1015() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775807"
        Object actual = (new Attribute("a", "9223372036854775807")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1773148191", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1016() throws Exception {
        // Combination: receiver__key="a", receiver__value="9223372036854775808"
        Object actual = (new Attribute("a", "9223372036854775808")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1773148190", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1017() throws Exception {
        // Combination: receiver__key="a", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-474935873", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1018() throws Exception {
        // Combination: receiver__key="test123", receiver__value=""
        Object actual = (new Attribute("test123", "")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1147882592", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1019() throws Exception {
        // Combination: receiver__key="test123", receiver__value=" "
        Object actual = (new Attribute("test123", " ")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1147882560", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1020() throws Exception {
        // Combination: receiver__key="test123", receiver__value="a"
        Object actual = (new Attribute("test123", "a")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1147882495", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1021() throws Exception {
        // Combination: receiver__key="test123", receiver__value="test123"
        Object actual = (new Attribute("test123", "test123")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1724582912", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1022() throws Exception {
        // Combination: receiver__key="test123", receiver__value="!@#"
        Object actual = (new Attribute("test123", "!@#")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1147848860", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1023() throws Exception {
        // Combination: receiver__key="test123", receiver__value="0"
        Object actual = (new Attribute("test123", "0")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1147882544", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1024() throws Exception {
        // Combination: receiver__key="test123", receiver__value="-1"
        Object actual = (new Attribute("test123", "-1")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1147881148", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1025() throws Exception {
        // Combination: receiver__key="test123", receiver__value="1.5"
        Object actual = (new Attribute("test123", "1.5")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1147834024", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1026() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775807"
        Object actual = (new Attribute("test123", "9223372036854775807")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1373933506", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1027() throws Exception {
        // Combination: receiver__key="test123", receiver__value="9223372036854775808"
        Object actual = (new Attribute("test123", "9223372036854775808")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1373933507", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1028() throws Exception {
        // Combination: receiver__key="test123", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1622821472", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1029() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=""
        Object actual = (new Attribute("!@#", "")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1045692", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1030() throws Exception {
        // Combination: receiver__key="!@#", receiver__value=" "
        Object actual = (new Attribute("!@#", " ")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1045724", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1031() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="a"
        Object actual = (new Attribute("!@#", "a")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1045789", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1032() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="test123"
        Object actual = (new Attribute("!@#", "test123")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1421456100", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1033() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="!@#"
        Object actual = (new Attribute("!@#", "!@#")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1079424", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1034() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="0"
        Object actual = (new Attribute("!@#", "0")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1045740", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1035() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="-1"
        Object actual = (new Attribute("!@#", "-1")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1047136", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1036() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="1.5"
        Object actual = (new Attribute("!@#", "1.5")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1094260", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1037() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775807"
        Object actual = (new Attribute("!@#", "9223372036854775807")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1772105506", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1038() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="9223372036854775808"
        Object actual = (new Attribute("!@#", "9223372036854775808")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1772105505", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1039() throws Exception {
        // Combination: receiver__key="!@#", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-473893188", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1040() throws Exception {
        // Combination: receiver__key="0", receiver__value=""
        Object actual = (new Attribute("0", "")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1488", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1041() throws Exception {
        // Combination: receiver__key="0", receiver__value=" "
        Object actual = (new Attribute("0", " ")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1520", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1042() throws Exception {
        // Combination: receiver__key="0", receiver__value="a"
        Object actual = (new Attribute("0", "a")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1585", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1043() throws Exception {
        // Combination: receiver__key="0", receiver__value="test123"
        Object actual = (new Attribute("0", "test123")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1422500304", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1044() throws Exception {
        // Combination: receiver__key="0", receiver__value="!@#"
        Object actual = (new Attribute("0", "!@#")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("35220", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1045() throws Exception {
        // Combination: receiver__key="0", receiver__value="0"
        Object actual = (new Attribute("0", "0")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1536", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1046() throws Exception {
        // Combination: receiver__key="0", receiver__value="-1"
        Object actual = (new Attribute("0", "-1")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2932", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1047() throws Exception {
        // Combination: receiver__key="0", receiver__value="1.5"
        Object actual = (new Attribute("0", "1.5")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("50056", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1048() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775807"
        Object actual = (new Attribute("0", "9223372036854775807")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1773149710", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1049() throws Exception {
        // Combination: receiver__key="0", receiver__value="9223372036854775808"
        Object actual = (new Attribute("0", "9223372036854775808")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1773149709", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1050() throws Exception {
        // Combination: receiver__key="0", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-474937392", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1051() throws Exception {
        // Combination: receiver__key="-1", receiver__value=""
        Object actual = (new Attribute("-1", "")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("44764", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1052() throws Exception {
        // Combination: receiver__key="-1", receiver__value=" "
        Object actual = (new Attribute("-1", " ")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("44796", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1053() throws Exception {
        // Combination: receiver__key="-1", receiver__value="a"
        Object actual = (new Attribute("-1", "a")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("44861", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1054() throws Exception {
        // Combination: receiver__key="-1", receiver__value="test123"
        Object actual = (new Attribute("-1", "test123")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1422457028", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1055() throws Exception {
        // Combination: receiver__key="-1", receiver__value="!@#"
        Object actual = (new Attribute("-1", "!@#")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("78496", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1056() throws Exception {
        // Combination: receiver__key="-1", receiver__value="0"
        Object actual = (new Attribute("-1", "0")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("44812", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1057() throws Exception {
        // Combination: receiver__key="-1", receiver__value="-1"
        Object actual = (new Attribute("-1", "-1")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("46208", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1058() throws Exception {
        // Combination: receiver__key="-1", receiver__value="1.5"
        Object actual = (new Attribute("-1", "1.5")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("93332", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1059() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775807"
        Object actual = (new Attribute("-1", "9223372036854775807")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1773106434", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1060() throws Exception {
        // Combination: receiver__key="-1", receiver__value="9223372036854775808"
        Object actual = (new Attribute("-1", "9223372036854775808")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1773106433", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1061() throws Exception {
        // Combination: receiver__key="-1", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-474894116", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1062() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=""
        Object actual = (new Attribute("1.5", "")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1505608", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1063() throws Exception {
        // Combination: receiver__key="1.5", receiver__value=" "
        Object actual = (new Attribute("1.5", " ")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1505640", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1064() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="a"
        Object actual = (new Attribute("1.5", "a")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1505705", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1065() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="test123"
        Object actual = (new Attribute("1.5", "test123")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1420996184", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1066() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="!@#"
        Object actual = (new Attribute("1.5", "!@#")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1539340", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1067() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="0"
        Object actual = (new Attribute("1.5", "0")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1505656", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1068() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="-1"
        Object actual = (new Attribute("1.5", "-1")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1507052", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1069() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="1.5"
        Object actual = (new Attribute("1.5", "1.5")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1554176", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1070() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775807"
        Object actual = (new Attribute("1.5", "9223372036854775807")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1771645590", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1071() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="9223372036854775808"
        Object actual = (new Attribute("1.5", "9223372036854775808")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1771645589", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1072() throws Exception {
        // Combination: receiver__key="1.5", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-473433272", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1073() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=""
        Object actual = (new Attribute("9223372036854775807", "")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866887710", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1074() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value=" "
        Object actual = (new Attribute("9223372036854775807", " ")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866887742", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1075() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="a"
        Object actual = (new Attribute("9223372036854775807", "a")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866887807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1076() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775807", "test123")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-555614082", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1077() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775807", "!@#")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866921442", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1078() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="0"
        Object actual = (new Attribute("9223372036854775807", "0")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866887758", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1079() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775807", "-1")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866889154", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1080() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775807", "1.5")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866936278", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1081() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775807")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-906263488", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1082() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775807", "9223372036854775808")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-906263487", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1083() throws Exception {
        // Combination: receiver__key="9223372036854775807", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("391948830", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1084() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=""
        Object actual = (new Attribute("9223372036854775808", "")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866887741", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1085() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value=" "
        Object actual = (new Attribute("9223372036854775808", " ")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866887773", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1086() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="a"
        Object actual = (new Attribute("9223372036854775808", "a")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866887838", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1087() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="test123"
        Object actual = (new Attribute("9223372036854775808", "test123")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-555614051", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1088() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="!@#"
        Object actual = (new Attribute("9223372036854775808", "!@#")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866921473", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1089() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="0"
        Object actual = (new Attribute("9223372036854775808", "0")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866887789", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1090() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="-1"
        Object actual = (new Attribute("9223372036854775808", "-1")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866889185", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1091() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="1.5"
        Object actual = (new Attribute("9223372036854775808", "1.5")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("866936309", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1092() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775807"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775807")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-906263457", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1093() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="9223372036854775808"
        Object actual = (new Attribute("9223372036854775808", "9223372036854775808")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-906263456", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1094() throws Exception {
        // Combination: receiver__key="9223372036854775808", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("391948861", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1095() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=""
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1838203392", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1096() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value=" "
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1838203360", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1097() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="a"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1838203295", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1098() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="test123"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1034262112", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1099() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="!@#"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1838169660", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1100() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="0"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1838203344", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1101() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="-1"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1838201948", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1102() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="1.5"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1838154824", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1103() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775807"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("683612706", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1104() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="9223372036854775808"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("683612707", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_1105() throws Exception {
        // Combination: receiver__key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__value="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Attribute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1981825024", formatValue(actual));
    }

}
