package org.jfree.data.xy;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for XYSeries.
 */
public class XYSeries_IPOTest {
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
    public void test_getX_pairwise_001() throws Exception {
        // Combination: receiver__key="sample_str", index=0
        try {
            (new XYSeries("sample_str")).getX(0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_002() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), index=0
        try {
            (new XYSeries(Integer.valueOf(1))).getX(0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_003() throws Exception {
        // Combination: receiver__key="sample_str", index=1
        try {
            (new XYSeries("sample_str")).getX(1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_004() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), index=1
        try {
            (new XYSeries(Integer.valueOf(1))).getX(1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_005() throws Exception {
        // Combination: receiver__key="sample_str", index=-1
        try {
            (new XYSeries("sample_str")).getX(-1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_006() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), index=-1
        try {
            (new XYSeries(Integer.valueOf(1))).getX(-1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_007() throws Exception {
        // Combination: receiver__key="sample_str", index=Integer.MAX_VALUE
        try {
            (new XYSeries("sample_str")).getX(Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_008() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), index=Integer.MAX_VALUE
        try {
            (new XYSeries(Integer.valueOf(1))).getX(Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_009() throws Exception {
        // Combination: receiver__key="sample_str", index=Integer.MIN_VALUE
        try {
            (new XYSeries("sample_str")).getX(Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_010() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), index=Integer.MIN_VALUE
        try {
            (new XYSeries(Integer.valueOf(1))).getX(Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_011() throws Exception {
        // Combination: receiver__key="sample_str", index=0
        try {
            (new XYSeries("sample_str")).getY(0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_012() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), index=0
        try {
            (new XYSeries(Integer.valueOf(1))).getY(0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_013() throws Exception {
        // Combination: receiver__key="sample_str", index=1
        try {
            (new XYSeries("sample_str")).getY(1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_014() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), index=1
        try {
            (new XYSeries(Integer.valueOf(1))).getY(1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_015() throws Exception {
        // Combination: receiver__key="sample_str", index=-1
        try {
            (new XYSeries("sample_str")).getY(-1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_016() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), index=-1
        try {
            (new XYSeries(Integer.valueOf(1))).getY(-1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_017() throws Exception {
        // Combination: receiver__key="sample_str", index=Integer.MAX_VALUE
        try {
            (new XYSeries("sample_str")).getY(Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_018() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), index=Integer.MAX_VALUE
        try {
            (new XYSeries(Integer.valueOf(1))).getY(Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_019() throws Exception {
        // Combination: receiver__key="sample_str", index=Integer.MIN_VALUE
        try {
            (new XYSeries("sample_str")).getY(Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_020() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), index=Integer.MIN_VALUE
        try {
            (new XYSeries(Integer.valueOf(1))).getY(Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_indexOf_pairwise_021() throws Exception {
        // Combination: receiver__key="sample_str", x=Integer.valueOf(1)
        Object actual = (new XYSeries("sample_str")).indexOf(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_indexOf_pairwise_022() throws Exception {
        // Combination: receiver__key="sample_str", x=Double.valueOf(2.5)
        Object actual = (new XYSeries("sample_str")).indexOf(Double.valueOf(2.5));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_indexOf_pairwise_023() throws Exception {
        // Combination: receiver__key="sample_str", x=Long.valueOf(100L)
        Object actual = (new XYSeries("sample_str")).indexOf(Long.valueOf(100L));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_indexOf_pairwise_024() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), x=Integer.valueOf(1)
        Object actual = (new XYSeries(Integer.valueOf(1))).indexOf(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_indexOf_pairwise_025() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), x=Double.valueOf(2.5)
        Object actual = (new XYSeries(Integer.valueOf(1))).indexOf(Double.valueOf(2.5));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_indexOf_pairwise_026() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), x=Long.valueOf(100L)
        Object actual = (new XYSeries(Integer.valueOf(1))).indexOf(Long.valueOf(100L));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_027() throws Exception {
        // Combination: receiver__key="sample_str", obj=new Object()
        Object actual = (new XYSeries("sample_str")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_028() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), obj=new Object()
        Object actual = (new XYSeries(Integer.valueOf(1))).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_029() throws Exception {
        // Combination: receiver__key="sample_str", obj="sample_str"
        Object actual = (new XYSeries("sample_str")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_030() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), obj="sample_str"
        Object actual = (new XYSeries(Integer.valueOf(1))).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_031() throws Exception {
        // Combination: receiver__key="sample_str", obj=Integer.valueOf(1)
        Object actual = (new XYSeries("sample_str")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_032() throws Exception {
        // Combination: receiver__key=Integer.valueOf(1), obj=Integer.valueOf(1)
        Object actual = (new XYSeries(Integer.valueOf(1))).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

}
