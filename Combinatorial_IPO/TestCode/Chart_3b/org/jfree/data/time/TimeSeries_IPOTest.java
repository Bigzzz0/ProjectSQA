package org.jfree.data.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for TimeSeries.
 */
public class TimeSeries_IPOTest {
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
    public void test_getValue_pairwise_001() throws Exception {
        // Combination: receiver__name="sample_str", index=0
        try {
            (new TimeSeries("sample_str")).getValue(0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_002() throws Exception {
        // Combination: receiver__name=Integer.valueOf(1), index=0
        try {
            (new TimeSeries(Integer.valueOf(1))).getValue(0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_003() throws Exception {
        // Combination: receiver__name="sample_str", index=1
        try {
            (new TimeSeries("sample_str")).getValue(1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_004() throws Exception {
        // Combination: receiver__name=Integer.valueOf(1), index=1
        try {
            (new TimeSeries(Integer.valueOf(1))).getValue(1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_005() throws Exception {
        // Combination: receiver__name="sample_str", index=-1
        try {
            (new TimeSeries("sample_str")).getValue(-1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_006() throws Exception {
        // Combination: receiver__name=Integer.valueOf(1), index=-1
        try {
            (new TimeSeries(Integer.valueOf(1))).getValue(-1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_007() throws Exception {
        // Combination: receiver__name="sample_str", index=Integer.MAX_VALUE
        try {
            (new TimeSeries("sample_str")).getValue(Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_008() throws Exception {
        // Combination: receiver__name=Integer.valueOf(1), index=Integer.MAX_VALUE
        try {
            (new TimeSeries(Integer.valueOf(1))).getValue(Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_009() throws Exception {
        // Combination: receiver__name="sample_str", index=Integer.MIN_VALUE
        try {
            (new TimeSeries("sample_str")).getValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_010() throws Exception {
        // Combination: receiver__name=Integer.valueOf(1), index=Integer.MIN_VALUE
        try {
            (new TimeSeries(Integer.valueOf(1))).getValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_011() throws Exception {
        // Combination: receiver__name="sample_str", obj=new Object()
        Object actual = (new TimeSeries("sample_str")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_012() throws Exception {
        // Combination: receiver__name=Integer.valueOf(1), obj=new Object()
        Object actual = (new TimeSeries(Integer.valueOf(1))).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_013() throws Exception {
        // Combination: receiver__name="sample_str", obj="sample_str"
        Object actual = (new TimeSeries("sample_str")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_014() throws Exception {
        // Combination: receiver__name=Integer.valueOf(1), obj="sample_str"
        Object actual = (new TimeSeries(Integer.valueOf(1))).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_015() throws Exception {
        // Combination: receiver__name="sample_str", obj=Integer.valueOf(1)
        Object actual = (new TimeSeries("sample_str")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_016() throws Exception {
        // Combination: receiver__name=Integer.valueOf(1), obj=Integer.valueOf(1)
        Object actual = (new TimeSeries(Integer.valueOf(1))).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

}
