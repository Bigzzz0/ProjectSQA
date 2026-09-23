package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Tag.
 */
public class Tag_IPOTest {
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
        // Combination: receiver__tagName="", o=new Object()
        try {
            (Tag.valueOf("")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_002() throws Exception {
        // Combination: receiver__tagName=" ", o=new Object()
        try {
            (Tag.valueOf(" ")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_003() throws Exception {
        // Combination: receiver__tagName="a", o=new Object()
        Object actual = (Tag.valueOf("a")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_004() throws Exception {
        // Combination: receiver__tagName="test123", o=new Object()
        Object actual = (Tag.valueOf("test123")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_005() throws Exception {
        // Combination: receiver__tagName="!@#", o=new Object()
        Object actual = (Tag.valueOf("!@#")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_006() throws Exception {
        // Combination: receiver__tagName="0", o=new Object()
        Object actual = (Tag.valueOf("0")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_007() throws Exception {
        // Combination: receiver__tagName="-1", o=new Object()
        Object actual = (Tag.valueOf("-1")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_008() throws Exception {
        // Combination: receiver__tagName="1.5", o=new Object()
        Object actual = (Tag.valueOf("1.5")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_009() throws Exception {
        // Combination: receiver__tagName="9223372036854775807", o=new Object()
        Object actual = (Tag.valueOf("9223372036854775807")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_010() throws Exception {
        // Combination: receiver__tagName="9223372036854775808", o=new Object()
        Object actual = (Tag.valueOf("9223372036854775808")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_011() throws Exception {
        // Combination: receiver__tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o=new Object()
        Object actual = (Tag.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_012() throws Exception {
        // Combination: receiver__tagName="", o="sample_str"
        try {
            (Tag.valueOf("")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_013() throws Exception {
        // Combination: receiver__tagName=" ", o="sample_str"
        try {
            (Tag.valueOf(" ")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_014() throws Exception {
        // Combination: receiver__tagName="a", o="sample_str"
        Object actual = (Tag.valueOf("a")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_015() throws Exception {
        // Combination: receiver__tagName="test123", o="sample_str"
        Object actual = (Tag.valueOf("test123")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_016() throws Exception {
        // Combination: receiver__tagName="!@#", o="sample_str"
        Object actual = (Tag.valueOf("!@#")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_017() throws Exception {
        // Combination: receiver__tagName="0", o="sample_str"
        Object actual = (Tag.valueOf("0")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_018() throws Exception {
        // Combination: receiver__tagName="-1", o="sample_str"
        Object actual = (Tag.valueOf("-1")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_019() throws Exception {
        // Combination: receiver__tagName="1.5", o="sample_str"
        Object actual = (Tag.valueOf("1.5")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_020() throws Exception {
        // Combination: receiver__tagName="9223372036854775807", o="sample_str"
        Object actual = (Tag.valueOf("9223372036854775807")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_021() throws Exception {
        // Combination: receiver__tagName="9223372036854775808", o="sample_str"
        Object actual = (Tag.valueOf("9223372036854775808")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_022() throws Exception {
        // Combination: receiver__tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o="sample_str"
        Object actual = (Tag.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_023() throws Exception {
        // Combination: receiver__tagName="", o=Integer.valueOf(1)
        try {
            (Tag.valueOf("")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_024() throws Exception {
        // Combination: receiver__tagName=" ", o=Integer.valueOf(1)
        try {
            (Tag.valueOf(" ")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_025() throws Exception {
        // Combination: receiver__tagName="a", o=Integer.valueOf(1)
        Object actual = (Tag.valueOf("a")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_026() throws Exception {
        // Combination: receiver__tagName="test123", o=Integer.valueOf(1)
        Object actual = (Tag.valueOf("test123")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_027() throws Exception {
        // Combination: receiver__tagName="!@#", o=Integer.valueOf(1)
        Object actual = (Tag.valueOf("!@#")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_028() throws Exception {
        // Combination: receiver__tagName="0", o=Integer.valueOf(1)
        Object actual = (Tag.valueOf("0")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_029() throws Exception {
        // Combination: receiver__tagName="-1", o=Integer.valueOf(1)
        Object actual = (Tag.valueOf("-1")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_030() throws Exception {
        // Combination: receiver__tagName="1.5", o=Integer.valueOf(1)
        Object actual = (Tag.valueOf("1.5")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_031() throws Exception {
        // Combination: receiver__tagName="9223372036854775807", o=Integer.valueOf(1)
        Object actual = (Tag.valueOf("9223372036854775807")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_032() throws Exception {
        // Combination: receiver__tagName="9223372036854775808", o=Integer.valueOf(1)
        Object actual = (Tag.valueOf("9223372036854775808")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_033() throws Exception {
        // Combination: receiver__tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", o=Integer.valueOf(1)
        Object actual = (Tag.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

}
