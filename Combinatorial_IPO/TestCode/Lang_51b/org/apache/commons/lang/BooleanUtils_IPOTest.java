package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for BooleanUtils.
 */
public class BooleanUtils_IPOTest {
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
    public void test_toBooleanDefaultIfNull_pairwise_001() throws Exception {
        // Combination: bool=true, valueIfNull=true
        Object actual = BooleanUtils.toBooleanDefaultIfNull(true, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanDefaultIfNull_pairwise_002() throws Exception {
        // Combination: bool=true, valueIfNull=false
        Object actual = BooleanUtils.toBooleanDefaultIfNull(true, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanDefaultIfNull_pairwise_003() throws Exception {
        // Combination: bool=false, valueIfNull=true
        Object actual = BooleanUtils.toBooleanDefaultIfNull(false, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanDefaultIfNull_pairwise_004() throws Exception {
        // Combination: bool=false, valueIfNull=false
        Object actual = BooleanUtils.toBooleanDefaultIfNull(false, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_005() throws Exception {
        // Combination: value=0, trueValue=0, falseValue=0
        Object actual = BooleanUtils.toBoolean(0, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_006() throws Exception {
        // Combination: value=1, trueValue=1, falseValue=0
        Object actual = BooleanUtils.toBoolean(1, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_007() throws Exception {
        // Combination: value=-1, trueValue=-1, falseValue=0
        Object actual = BooleanUtils.toBoolean(-1, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_008() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=Integer.MAX_VALUE, falseValue=0
        Object actual = BooleanUtils.toBoolean(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_009() throws Exception {
        // Combination: value=Integer.MIN_VALUE, trueValue=Integer.MIN_VALUE, falseValue=0
        Object actual = BooleanUtils.toBoolean(Integer.MIN_VALUE, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_010() throws Exception {
        // Combination: value=1, trueValue=0, falseValue=1
        Object actual = BooleanUtils.toBoolean(1, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_011() throws Exception {
        // Combination: value=0, trueValue=1, falseValue=1
        try {
            BooleanUtils.toBoolean(0, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_012() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=-1, falseValue=1
        try {
            BooleanUtils.toBoolean(Integer.MAX_VALUE, -1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_013() throws Exception {
        // Combination: value=-1, trueValue=Integer.MAX_VALUE, falseValue=1
        try {
            BooleanUtils.toBoolean(-1, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_014() throws Exception {
        // Combination: value=0, trueValue=Integer.MIN_VALUE, falseValue=1
        try {
            BooleanUtils.toBoolean(0, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_015() throws Exception {
        // Combination: value=-1, trueValue=0, falseValue=-1
        Object actual = BooleanUtils.toBoolean(-1, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_016() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=1, falseValue=-1
        try {
            BooleanUtils.toBoolean(Integer.MAX_VALUE, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_017() throws Exception {
        // Combination: value=0, trueValue=-1, falseValue=-1
        try {
            BooleanUtils.toBoolean(0, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_018() throws Exception {
        // Combination: value=1, trueValue=Integer.MAX_VALUE, falseValue=-1
        try {
            BooleanUtils.toBoolean(1, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_019() throws Exception {
        // Combination: value=1, trueValue=Integer.MIN_VALUE, falseValue=-1
        try {
            BooleanUtils.toBoolean(1, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_020() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=0, falseValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toBoolean(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_021() throws Exception {
        // Combination: value=-1, trueValue=1, falseValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBoolean(-1, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_022() throws Exception {
        // Combination: value=1, trueValue=-1, falseValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBoolean(1, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_023() throws Exception {
        // Combination: value=0, trueValue=Integer.MAX_VALUE, falseValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBoolean(0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_024() throws Exception {
        // Combination: value=-1, trueValue=Integer.MIN_VALUE, falseValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBoolean(-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_025() throws Exception {
        // Combination: value=Integer.MIN_VALUE, trueValue=0, falseValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toBoolean(Integer.MIN_VALUE, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_026() throws Exception {
        // Combination: value=0, trueValue=1, falseValue=Integer.MIN_VALUE
        try {
            BooleanUtils.toBoolean(0, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_027() throws Exception {
        // Combination: value=1, trueValue=-1, falseValue=Integer.MIN_VALUE
        try {
            BooleanUtils.toBoolean(1, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_028() throws Exception {
        // Combination: value=-1, trueValue=Integer.MAX_VALUE, falseValue=Integer.MIN_VALUE
        try {
            BooleanUtils.toBoolean(-1, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_029() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=Integer.MIN_VALUE, falseValue=Integer.MIN_VALUE
        try {
            BooleanUtils.toBoolean(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_030() throws Exception {
        // Combination: value=Integer.MIN_VALUE, trueValue=1, falseValue=1
        try {
            BooleanUtils.toBoolean(Integer.MIN_VALUE, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_031() throws Exception {
        // Combination: value=Integer.MIN_VALUE, trueValue=-1, falseValue=-1
        try {
            BooleanUtils.toBoolean(Integer.MIN_VALUE, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_032() throws Exception {
        // Combination: value=Integer.MIN_VALUE, trueValue=Integer.MAX_VALUE, falseValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBoolean(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_033() throws Exception {
        // Combination: value=0, trueValue=0, falseValue=0
        Object actual = BooleanUtils.toBoolean(0, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_034() throws Exception {
        // Combination: value=1, trueValue=1, falseValue=0
        Object actual = BooleanUtils.toBoolean(1, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_035() throws Exception {
        // Combination: value=-1, trueValue=-1, falseValue=0
        Object actual = BooleanUtils.toBoolean(-1, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_036() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=Integer.MAX_VALUE, falseValue=0
        Object actual = BooleanUtils.toBoolean(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_037() throws Exception {
        // Combination: value=1, trueValue=0, falseValue=1
        Object actual = BooleanUtils.toBoolean(1, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_038() throws Exception {
        // Combination: value=0, trueValue=1, falseValue=1
        try {
            BooleanUtils.toBoolean(0, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_039() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=-1, falseValue=1
        try {
            BooleanUtils.toBoolean(Integer.MAX_VALUE, -1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_040() throws Exception {
        // Combination: value=-1, trueValue=Integer.MAX_VALUE, falseValue=1
        try {
            BooleanUtils.toBoolean(-1, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_041() throws Exception {
        // Combination: value=-1, trueValue=0, falseValue=-1
        Object actual = BooleanUtils.toBoolean(-1, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_042() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=1, falseValue=-1
        try {
            BooleanUtils.toBoolean(Integer.MAX_VALUE, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_043() throws Exception {
        // Combination: value=0, trueValue=-1, falseValue=-1
        try {
            BooleanUtils.toBoolean(0, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_044() throws Exception {
        // Combination: value=1, trueValue=Integer.MAX_VALUE, falseValue=-1
        try {
            BooleanUtils.toBoolean(1, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_045() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=0, falseValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toBoolean(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_046() throws Exception {
        // Combination: value=-1, trueValue=1, falseValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBoolean(-1, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_047() throws Exception {
        // Combination: value=1, trueValue=-1, falseValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBoolean(1, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_048() throws Exception {
        // Combination: value=0, trueValue=Integer.MAX_VALUE, falseValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBoolean(0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_049() throws Exception {
        // Combination: value=0, trueValue=0, falseValue=0, nullValue=0
        Object actual = BooleanUtils.toBooleanObject(0, 0, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_050() throws Exception {
        // Combination: value=1, trueValue=1, falseValue=0, nullValue=1
        Object actual = BooleanUtils.toBooleanObject(1, 1, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_051() throws Exception {
        // Combination: value=-1, trueValue=-1, falseValue=0, nullValue=-1
        Object actual = BooleanUtils.toBooleanObject(-1, -1, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_052() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=Integer.MAX_VALUE, falseValue=0, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toBooleanObject(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_053() throws Exception {
        // Combination: value=Integer.MIN_VALUE, trueValue=Integer.MIN_VALUE, falseValue=0, nullValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toBooleanObject(Integer.MIN_VALUE, Integer.MIN_VALUE, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_054() throws Exception {
        // Combination: value=-1, trueValue=1, falseValue=1, nullValue=0
        try {
            BooleanUtils.toBooleanObject(-1, 1, 1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_055() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=0, falseValue=1, nullValue=1
        try {
            BooleanUtils.toBooleanObject(Integer.MAX_VALUE, 0, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_056() throws Exception {
        // Combination: value=0, trueValue=Integer.MAX_VALUE, falseValue=1, nullValue=-1
        try {
            BooleanUtils.toBooleanObject(0, Integer.MAX_VALUE, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_057() throws Exception {
        // Combination: value=1, trueValue=-1, falseValue=1, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toBooleanObject(1, -1, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_058() throws Exception {
        // Combination: value=1, trueValue=0, falseValue=1, nullValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toBooleanObject(1, 0, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_059() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=-1, falseValue=-1, nullValue=0
        try {
            BooleanUtils.toBooleanObject(Integer.MAX_VALUE, -1, -1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_060() throws Exception {
        // Combination: value=-1, trueValue=Integer.MAX_VALUE, falseValue=-1, nullValue=1
        Object actual = BooleanUtils.toBooleanObject(-1, Integer.MAX_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_061() throws Exception {
        // Combination: value=Integer.MIN_VALUE, trueValue=0, falseValue=-1, nullValue=-1
        try {
            BooleanUtils.toBooleanObject(Integer.MIN_VALUE, 0, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_062() throws Exception {
        // Combination: value=0, trueValue=1, falseValue=-1, nullValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBooleanObject(0, 1, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_063() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=1, falseValue=-1, nullValue=Integer.MIN_VALUE
        try {
            BooleanUtils.toBooleanObject(Integer.MAX_VALUE, 1, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_064() throws Exception {
        // Combination: value=1, trueValue=Integer.MAX_VALUE, falseValue=Integer.MAX_VALUE, nullValue=0
        try {
            BooleanUtils.toBooleanObject(1, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_065() throws Exception {
        // Combination: value=0, trueValue=-1, falseValue=Integer.MAX_VALUE, nullValue=1
        try {
            BooleanUtils.toBooleanObject(0, -1, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_066() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=1, falseValue=Integer.MAX_VALUE, nullValue=-1
        Object actual = BooleanUtils.toBooleanObject(Integer.MAX_VALUE, 1, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_067() throws Exception {
        // Combination: value=-1, trueValue=0, falseValue=Integer.MAX_VALUE, nullValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBooleanObject(-1, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_068() throws Exception {
        // Combination: value=Integer.MIN_VALUE, trueValue=-1, falseValue=Integer.MAX_VALUE, nullValue=Integer.MIN_VALUE
        assertNull(BooleanUtils.toBooleanObject(Integer.MIN_VALUE, -1, Integer.MAX_VALUE, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_069() throws Exception {
        // Combination: value=0, trueValue=Integer.MIN_VALUE, falseValue=Integer.MIN_VALUE, nullValue=0
        assertNull(BooleanUtils.toBooleanObject(0, Integer.MIN_VALUE, Integer.MIN_VALUE, 0));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_070() throws Exception {
        // Combination: value=Integer.MIN_VALUE, trueValue=0, falseValue=Integer.MIN_VALUE, nullValue=1
        Object actual = BooleanUtils.toBooleanObject(Integer.MIN_VALUE, 0, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_071() throws Exception {
        // Combination: value=1, trueValue=1, falseValue=Integer.MIN_VALUE, nullValue=-1
        Object actual = BooleanUtils.toBooleanObject(1, 1, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_072() throws Exception {
        // Combination: value=-1, trueValue=-1, falseValue=Integer.MIN_VALUE, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toBooleanObject(-1, -1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_073() throws Exception {
        // Combination: value=0, trueValue=Integer.MAX_VALUE, falseValue=Integer.MIN_VALUE, nullValue=Integer.MIN_VALUE
        try {
            BooleanUtils.toBooleanObject(0, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_074() throws Exception {
        // Combination: value=1, trueValue=Integer.MIN_VALUE, falseValue=1, nullValue=1
        Object actual = BooleanUtils.toBooleanObject(1, Integer.MIN_VALUE, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_075() throws Exception {
        // Combination: value=1, trueValue=Integer.MIN_VALUE, falseValue=-1, nullValue=-1
        try {
            BooleanUtils.toBooleanObject(1, Integer.MIN_VALUE, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_076() throws Exception {
        // Combination: value=-1, trueValue=Integer.MIN_VALUE, falseValue=Integer.MAX_VALUE, nullValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBooleanObject(-1, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_077() throws Exception {
        // Combination: value=-1, trueValue=0, falseValue=0, nullValue=Integer.MIN_VALUE
        try {
            BooleanUtils.toBooleanObject(-1, 0, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_078() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=Integer.MIN_VALUE, falseValue=Integer.MIN_VALUE, nullValue=0
        try {
            BooleanUtils.toBooleanObject(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_079() throws Exception {
        // Combination: value=Integer.MIN_VALUE, trueValue=1, falseValue=1, nullValue=0
        try {
            BooleanUtils.toBooleanObject(Integer.MIN_VALUE, 1, 1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_080() throws Exception {
        // Combination: value=Integer.MIN_VALUE, trueValue=Integer.MAX_VALUE, falseValue=0, nullValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBooleanObject(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_081() throws Exception {
        // Combination: value=0, trueValue=0, falseValue=0, nullValue=0
        Object actual = BooleanUtils.toBooleanObject(0, 0, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_082() throws Exception {
        // Combination: value=1, trueValue=1, falseValue=0, nullValue=1
        Object actual = BooleanUtils.toBooleanObject(1, 1, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_083() throws Exception {
        // Combination: value=-1, trueValue=-1, falseValue=0, nullValue=-1
        Object actual = BooleanUtils.toBooleanObject(-1, -1, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_084() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=Integer.MAX_VALUE, falseValue=0, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toBooleanObject(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_085() throws Exception {
        // Combination: value=-1, trueValue=1, falseValue=1, nullValue=0
        try {
            BooleanUtils.toBooleanObject(-1, 1, 1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_086() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=0, falseValue=1, nullValue=1
        try {
            BooleanUtils.toBooleanObject(Integer.MAX_VALUE, 0, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_087() throws Exception {
        // Combination: value=0, trueValue=Integer.MAX_VALUE, falseValue=1, nullValue=-1
        try {
            BooleanUtils.toBooleanObject(0, Integer.MAX_VALUE, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_088() throws Exception {
        // Combination: value=1, trueValue=-1, falseValue=1, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toBooleanObject(1, -1, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_089() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=-1, falseValue=-1, nullValue=0
        try {
            BooleanUtils.toBooleanObject(Integer.MAX_VALUE, -1, -1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_090() throws Exception {
        // Combination: value=-1, trueValue=Integer.MAX_VALUE, falseValue=-1, nullValue=1
        Object actual = BooleanUtils.toBooleanObject(-1, Integer.MAX_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_091() throws Exception {
        // Combination: value=1, trueValue=0, falseValue=-1, nullValue=-1
        try {
            BooleanUtils.toBooleanObject(1, 0, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_092() throws Exception {
        // Combination: value=0, trueValue=1, falseValue=-1, nullValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBooleanObject(0, 1, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_093() throws Exception {
        // Combination: value=1, trueValue=Integer.MAX_VALUE, falseValue=Integer.MAX_VALUE, nullValue=0
        try {
            BooleanUtils.toBooleanObject(1, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_094() throws Exception {
        // Combination: value=0, trueValue=-1, falseValue=Integer.MAX_VALUE, nullValue=1
        try {
            BooleanUtils.toBooleanObject(0, -1, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_095() throws Exception {
        // Combination: value=Integer.MAX_VALUE, trueValue=1, falseValue=Integer.MAX_VALUE, nullValue=-1
        Object actual = BooleanUtils.toBooleanObject(Integer.MAX_VALUE, 1, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_096() throws Exception {
        // Combination: value=-1, trueValue=0, falseValue=Integer.MAX_VALUE, nullValue=Integer.MAX_VALUE
        try {
            BooleanUtils.toBooleanObject(-1, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_097() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=0
        Object actual = BooleanUtils.toInteger(true, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_098() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=1
        Object actual = BooleanUtils.toInteger(true, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_099() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=-1
        Object actual = BooleanUtils.toInteger(true, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_100() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toInteger(true, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_101() throws Exception {
        // Combination: bool=true, trueValue=Integer.MIN_VALUE, falseValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toInteger(true, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_102() throws Exception {
        // Combination: bool=false, trueValue=1, falseValue=0
        Object actual = BooleanUtils.toInteger(false, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_103() throws Exception {
        // Combination: bool=false, trueValue=0, falseValue=1
        Object actual = BooleanUtils.toInteger(false, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_104() throws Exception {
        // Combination: bool=false, trueValue=Integer.MAX_VALUE, falseValue=-1
        Object actual = BooleanUtils.toInteger(false, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_105() throws Exception {
        // Combination: bool=false, trueValue=-1, falseValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toInteger(false, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_106() throws Exception {
        // Combination: bool=false, trueValue=0, falseValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toInteger(false, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_107() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=-1
        Object actual = BooleanUtils.toInteger(true, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_108() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toInteger(true, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_109() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=-1
        Object actual = BooleanUtils.toInteger(true, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_110() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toInteger(true, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_111() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toInteger(true, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_112() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=0
        Object actual = BooleanUtils.toInteger(true, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_113() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=1
        Object actual = BooleanUtils.toInteger(true, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_114() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toInteger(true, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_115() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=0
        Object actual = BooleanUtils.toInteger(true, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_116() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=1
        Object actual = BooleanUtils.toInteger(true, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_117() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toInteger(true, Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_118() throws Exception {
        // Combination: bool=false, trueValue=Integer.MIN_VALUE, falseValue=0
        Object actual = BooleanUtils.toInteger(false, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_119() throws Exception {
        // Combination: bool=true, trueValue=Integer.MIN_VALUE, falseValue=1
        Object actual = BooleanUtils.toInteger(true, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_120() throws Exception {
        // Combination: bool=true, trueValue=Integer.MIN_VALUE, falseValue=-1
        Object actual = BooleanUtils.toInteger(true, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_121() throws Exception {
        // Combination: bool=true, trueValue=Integer.MIN_VALUE, falseValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toInteger(true, Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_122() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=0, nullValue=0
        Object actual = BooleanUtils.toInteger(true, 0, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_123() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=1, nullValue=1
        Object actual = BooleanUtils.toInteger(true, 1, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_124() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=-1, nullValue=-1
        Object actual = BooleanUtils.toInteger(true, -1, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_125() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=Integer.MAX_VALUE, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toInteger(true, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_126() throws Exception {
        // Combination: bool=true, trueValue=Integer.MIN_VALUE, falseValue=Integer.MIN_VALUE, nullValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toInteger(true, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_127() throws Exception {
        // Combination: bool=false, trueValue=-1, falseValue=0, nullValue=1
        Object actual = BooleanUtils.toInteger(false, -1, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_128() throws Exception {
        // Combination: bool=false, trueValue=Integer.MAX_VALUE, falseValue=1, nullValue=0
        Object actual = BooleanUtils.toInteger(false, Integer.MAX_VALUE, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_129() throws Exception {
        // Combination: bool=false, trueValue=0, falseValue=-1, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toInteger(false, 0, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_130() throws Exception {
        // Combination: bool=false, trueValue=1, falseValue=Integer.MAX_VALUE, nullValue=-1
        Object actual = BooleanUtils.toInteger(false, 1, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_131() throws Exception {
        // Combination: bool=false, trueValue=1, falseValue=Integer.MIN_VALUE, nullValue=0
        Object actual = BooleanUtils.toInteger(false, 1, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_132() throws Exception {
        // Combination: bool=true, trueValue=Integer.MIN_VALUE, falseValue=-1, nullValue=0
        Object actual = BooleanUtils.toInteger(true, Integer.MIN_VALUE, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_133() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=Integer.MAX_VALUE, nullValue=0
        Object actual = BooleanUtils.toInteger(true, -1, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_134() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=-1, nullValue=1
        Object actual = BooleanUtils.toInteger(true, Integer.MAX_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_135() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=Integer.MAX_VALUE, nullValue=1
        Object actual = BooleanUtils.toInteger(true, 0, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_136() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=Integer.MIN_VALUE, nullValue=1
        Object actual = BooleanUtils.toInteger(true, 0, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_137() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=0, nullValue=-1
        Object actual = BooleanUtils.toInteger(true, Integer.MAX_VALUE, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_138() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=1, nullValue=-1
        Object actual = BooleanUtils.toInteger(true, 0, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_139() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=Integer.MIN_VALUE, nullValue=-1
        Object actual = BooleanUtils.toInteger(true, -1, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_140() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=0, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toInteger(true, 1, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_141() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=1, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toInteger(true, -1, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_142() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=Integer.MIN_VALUE, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toInteger(true, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_143() throws Exception {
        // Combination: bool=false, trueValue=Integer.MIN_VALUE, falseValue=0, nullValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toInteger(false, Integer.MIN_VALUE, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_144() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=1, nullValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toInteger(true, 0, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_145() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=-1, nullValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toInteger(true, 1, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_146() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=Integer.MAX_VALUE, nullValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toInteger(true, -1, Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_147() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=0, nullValue=Integer.MIN_VALUE
        Object actual = BooleanUtils.toInteger(true, Integer.MAX_VALUE, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_148() throws Exception {
        // Combination: bool=true, trueValue=Integer.MIN_VALUE, falseValue=1, nullValue=1
        Object actual = BooleanUtils.toInteger(true, Integer.MIN_VALUE, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_149() throws Exception {
        // Combination: bool=true, trueValue=Integer.MIN_VALUE, falseValue=Integer.MAX_VALUE, nullValue=-1
        Object actual = BooleanUtils.toInteger(true, Integer.MIN_VALUE, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toInteger_pairwise_150() throws Exception {
        // Combination: bool=true, trueValue=Integer.MIN_VALUE, falseValue=0, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toInteger(true, Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_151() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=0
        Object actual = BooleanUtils.toIntegerObject(true, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_152() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=1
        Object actual = BooleanUtils.toIntegerObject(true, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_153() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=-1
        Object actual = BooleanUtils.toIntegerObject(true, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_154() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toIntegerObject(true, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_155() throws Exception {
        // Combination: bool=false, trueValue=1, falseValue=0
        Object actual = BooleanUtils.toIntegerObject(false, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_156() throws Exception {
        // Combination: bool=false, trueValue=0, falseValue=1
        Object actual = BooleanUtils.toIntegerObject(false, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_157() throws Exception {
        // Combination: bool=false, trueValue=Integer.MAX_VALUE, falseValue=-1
        Object actual = BooleanUtils.toIntegerObject(false, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_158() throws Exception {
        // Combination: bool=false, trueValue=-1, falseValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toIntegerObject(false, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_159() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=-1
        Object actual = BooleanUtils.toIntegerObject(true, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_160() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toIntegerObject(true, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_161() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=-1
        Object actual = BooleanUtils.toIntegerObject(true, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_162() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toIntegerObject(true, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_163() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=0
        Object actual = BooleanUtils.toIntegerObject(true, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_164() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=1
        Object actual = BooleanUtils.toIntegerObject(true, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_165() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=0
        Object actual = BooleanUtils.toIntegerObject(true, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_166() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=1
        Object actual = BooleanUtils.toIntegerObject(true, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_167() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=0, nullValue=0
        Object actual = BooleanUtils.toIntegerObject(true, 0, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_168() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=1, nullValue=1
        Object actual = BooleanUtils.toIntegerObject(true, 1, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_169() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=-1, nullValue=-1
        Object actual = BooleanUtils.toIntegerObject(true, -1, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_170() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=Integer.MAX_VALUE, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toIntegerObject(true, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_171() throws Exception {
        // Combination: bool=false, trueValue=-1, falseValue=0, nullValue=1
        Object actual = BooleanUtils.toIntegerObject(false, -1, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_172() throws Exception {
        // Combination: bool=false, trueValue=Integer.MAX_VALUE, falseValue=1, nullValue=0
        Object actual = BooleanUtils.toIntegerObject(false, Integer.MAX_VALUE, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_173() throws Exception {
        // Combination: bool=false, trueValue=0, falseValue=-1, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toIntegerObject(false, 0, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_174() throws Exception {
        // Combination: bool=false, trueValue=1, falseValue=Integer.MAX_VALUE, nullValue=-1
        Object actual = BooleanUtils.toIntegerObject(false, 1, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_175() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=-1, nullValue=0
        Object actual = BooleanUtils.toIntegerObject(true, 1, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_176() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=Integer.MAX_VALUE, nullValue=0
        Object actual = BooleanUtils.toIntegerObject(true, -1, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_177() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=-1, nullValue=1
        Object actual = BooleanUtils.toIntegerObject(true, Integer.MAX_VALUE, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_178() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=Integer.MAX_VALUE, nullValue=1
        Object actual = BooleanUtils.toIntegerObject(true, 0, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_179() throws Exception {
        // Combination: bool=true, trueValue=Integer.MAX_VALUE, falseValue=0, nullValue=-1
        Object actual = BooleanUtils.toIntegerObject(true, Integer.MAX_VALUE, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_180() throws Exception {
        // Combination: bool=true, trueValue=0, falseValue=1, nullValue=-1
        Object actual = BooleanUtils.toIntegerObject(true, 0, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_181() throws Exception {
        // Combination: bool=true, trueValue=1, falseValue=0, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toIntegerObject(true, 1, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toIntegerObject_pairwise_182() throws Exception {
        // Combination: bool=true, trueValue=-1, falseValue=1, nullValue=Integer.MAX_VALUE
        Object actual = BooleanUtils.toIntegerObject(true, -1, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_183() throws Exception {
        // Combination: str="", trueString="", falseString="", nullString=""
        Object actual = BooleanUtils.toBooleanObject("", "", "", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_184() throws Exception {
        // Combination: str=" ", trueString=" ", falseString="", nullString=" "
        Object actual = BooleanUtils.toBooleanObject(" ", " ", "", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_185() throws Exception {
        // Combination: str="a", trueString="a", falseString="", nullString="a"
        Object actual = BooleanUtils.toBooleanObject("a", "a", "", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_186() throws Exception {
        // Combination: str="test123", trueString="test123", falseString="", nullString="test123"
        Object actual = BooleanUtils.toBooleanObject("test123", "test123", "", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_187() throws Exception {
        // Combination: str="!@#", trueString="!@#", falseString="", nullString="!@#"
        Object actual = BooleanUtils.toBooleanObject("!@#", "!@#", "", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_188() throws Exception {
        // Combination: str="0", trueString="0", falseString="", nullString="0"
        Object actual = BooleanUtils.toBooleanObject("0", "0", "", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_189() throws Exception {
        // Combination: str="-1", trueString="-1", falseString="", nullString="-1"
        Object actual = BooleanUtils.toBooleanObject("-1", "-1", "", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_190() throws Exception {
        // Combination: str="1.5", trueString="1.5", falseString="", nullString="1.5"
        Object actual = BooleanUtils.toBooleanObject("1.5", "1.5", "", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_191() throws Exception {
        // Combination: str="9223372036854775807", trueString="9223372036854775807", falseString="", nullString="9223372036854775807"
        Object actual = BooleanUtils.toBooleanObject("9223372036854775807", "9223372036854775807", "", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_192() throws Exception {
        // Combination: str="9223372036854775808", trueString="9223372036854775808", falseString="", nullString="9223372036854775808"
        Object actual = BooleanUtils.toBooleanObject("9223372036854775808", "9223372036854775808", "", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_193() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toBooleanObject("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_194() throws Exception {
        // Combination: str=" ", trueString="a", falseString=" ", nullString=""
        Object actual = BooleanUtils.toBooleanObject(" ", "a", " ", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_195() throws Exception {
        // Combination: str="", trueString="test123", falseString=" ", nullString=" "
        try {
            BooleanUtils.toBooleanObject("", "test123", " ", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_196() throws Exception {
        // Combination: str="test123", trueString="", falseString=" ", nullString="a"
        try {
            BooleanUtils.toBooleanObject("test123", "", " ", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_197() throws Exception {
        // Combination: str="a", trueString=" ", falseString=" ", nullString="test123"
        try {
            BooleanUtils.toBooleanObject("a", " ", " ", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_198() throws Exception {
        // Combination: str="0", trueString="-1", falseString=" ", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject("0", "-1", " ", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_199() throws Exception {
        // Combination: str="!@#", trueString="1.5", falseString=" ", nullString="0"
        try {
            BooleanUtils.toBooleanObject("!@#", "1.5", " ", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_200() throws Exception {
        // Combination: str="1.5", trueString="!@#", falseString=" ", nullString="-1"
        try {
            BooleanUtils.toBooleanObject("1.5", "!@#", " ", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_201() throws Exception {
        // Combination: str="-1", trueString="0", falseString=" ", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("-1", "0", " ", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_202() throws Exception {
        // Combination: str="9223372036854775808", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString=" ", nullString="9223372036854775807"
        try {
            BooleanUtils.toBooleanObject("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_203() throws Exception {
        // Combination: str="9223372036854775807", trueString="", falseString=" ", nullString="9223372036854775808"
        try {
            BooleanUtils.toBooleanObject("9223372036854775807", "", " ", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_204() throws Exception {
        // Combination: str="", trueString="9223372036854775807", falseString=" ", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBooleanObject("", "9223372036854775807", " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_205() throws Exception {
        // Combination: str="a", trueString="test123", falseString="a", nullString=""
        Object actual = BooleanUtils.toBooleanObject("a", "test123", "a", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_206() throws Exception {
        // Combination: str="test123", trueString="a", falseString="a", nullString=" "
        try {
            BooleanUtils.toBooleanObject("test123", "a", "a", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_207() throws Exception {
        // Combination: str="", trueString=" ", falseString="a", nullString="a"
        try {
            BooleanUtils.toBooleanObject("", " ", "a", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_208() throws Exception {
        // Combination: str=" ", trueString="", falseString="a", nullString="test123"
        try {
            BooleanUtils.toBooleanObject(" ", "", "a", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_209() throws Exception {
        // Combination: str="-1", trueString="1.5", falseString="a", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject("-1", "1.5", "a", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_210() throws Exception {
        // Combination: str="1.5", trueString="-1", falseString="a", nullString="0"
        try {
            BooleanUtils.toBooleanObject("1.5", "-1", "a", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_211() throws Exception {
        // Combination: str="!@#", trueString="0", falseString="a", nullString="-1"
        try {
            BooleanUtils.toBooleanObject("!@#", "0", "a", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_212() throws Exception {
        // Combination: str="0", trueString="!@#", falseString="a", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("0", "!@#", "a", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_213() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="9223372036854775808", falseString="a", nullString="9223372036854775807"
        try {
            BooleanUtils.toBooleanObject("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", "a", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_214() throws Exception {
        // Combination: str="", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="a", nullString="9223372036854775808"
        try {
            BooleanUtils.toBooleanObject("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_215() throws Exception {
        // Combination: str="9223372036854775807", trueString=" ", falseString="a", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBooleanObject("9223372036854775807", " ", "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_216() throws Exception {
        // Combination: str="test123", trueString=" ", falseString="test123", nullString=""
        Object actual = BooleanUtils.toBooleanObject("test123", " ", "test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_217() throws Exception {
        // Combination: str="a", trueString="", falseString="test123", nullString=" "
        try {
            BooleanUtils.toBooleanObject("a", "", "test123", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_218() throws Exception {
        // Combination: str=" ", trueString="test123", falseString="test123", nullString="a"
        try {
            BooleanUtils.toBooleanObject(" ", "test123", "test123", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_219() throws Exception {
        // Combination: str="", trueString="a", falseString="test123", nullString="test123"
        try {
            BooleanUtils.toBooleanObject("", "a", "test123", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_220() throws Exception {
        // Combination: str="1.5", trueString="0", falseString="test123", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject("1.5", "0", "test123", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_221() throws Exception {
        // Combination: str="-1", trueString="!@#", falseString="test123", nullString="0"
        try {
            BooleanUtils.toBooleanObject("-1", "!@#", "test123", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_222() throws Exception {
        // Combination: str="0", trueString="1.5", falseString="test123", nullString="-1"
        try {
            BooleanUtils.toBooleanObject("0", "1.5", "test123", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_223() throws Exception {
        // Combination: str="!@#", trueString="-1", falseString="test123", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("!@#", "-1", "test123", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_224() throws Exception {
        // Combination: str="", trueString="!@#", falseString="test123", nullString="9223372036854775807"
        try {
            BooleanUtils.toBooleanObject("", "!@#", "test123", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_225() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="9223372036854775807", falseString="test123", nullString="9223372036854775808"
        try {
            BooleanUtils.toBooleanObject("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", "test123", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_226() throws Exception {
        // Combination: str="9223372036854775808", trueString="", falseString="test123", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBooleanObject("9223372036854775808", "", "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_227() throws Exception {
        // Combination: str="!@#", trueString="9223372036854775807", falseString="!@#", nullString=""
        Object actual = BooleanUtils.toBooleanObject("!@#", "9223372036854775807", "!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_228() throws Exception {
        // Combination: str="0", trueString="9223372036854775808", falseString="!@#", nullString=" "
        try {
            BooleanUtils.toBooleanObject("0", "9223372036854775808", "!@#", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_229() throws Exception {
        // Combination: str="-1", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="!@#", nullString="a"
        try {
            BooleanUtils.toBooleanObject("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_230() throws Exception {
        // Combination: str="1.5", trueString="", falseString="!@#", nullString="test123"
        try {
            BooleanUtils.toBooleanObject("1.5", "", "!@#", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_231() throws Exception {
        // Combination: str="", trueString=" ", falseString="!@#", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject("", " ", "!@#", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_232() throws Exception {
        // Combination: str=" ", trueString="a", falseString="!@#", nullString="0"
        try {
            BooleanUtils.toBooleanObject(" ", "a", "!@#", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_233() throws Exception {
        // Combination: str="a", trueString="test123", falseString="!@#", nullString="-1"
        try {
            BooleanUtils.toBooleanObject("a", "test123", "!@#", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_234() throws Exception {
        // Combination: str="test123", trueString="!@#", falseString="!@#", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("test123", "!@#", "!@#", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_235() throws Exception {
        // Combination: str=" ", trueString="0", falseString="!@#", nullString="9223372036854775807"
        try {
            BooleanUtils.toBooleanObject(" ", "0", "!@#", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_236() throws Exception {
        // Combination: str=" ", trueString="-1", falseString="!@#", nullString="9223372036854775808"
        try {
            BooleanUtils.toBooleanObject(" ", "-1", "!@#", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_237() throws Exception {
        // Combination: str=" ", trueString="1.5", falseString="!@#", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBooleanObject(" ", "1.5", "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_238() throws Exception {
        // Combination: str="0", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="0", nullString=""
        Object actual = BooleanUtils.toBooleanObject("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_239() throws Exception {
        // Combination: str="!@#", trueString="", falseString="0", nullString=" "
        try {
            BooleanUtils.toBooleanObject("!@#", "", "0", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_240() throws Exception {
        // Combination: str="1.5", trueString="9223372036854775807", falseString="0", nullString="a"
        try {
            BooleanUtils.toBooleanObject("1.5", "9223372036854775807", "0", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_241() throws Exception {
        // Combination: str="-1", trueString="9223372036854775808", falseString="0", nullString="test123"
        try {
            BooleanUtils.toBooleanObject("-1", "9223372036854775808", "0", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_242() throws Exception {
        // Combination: str=" ", trueString="a", falseString="0", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject(" ", "a", "0", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_243() throws Exception {
        // Combination: str="", trueString=" ", falseString="0", nullString="0"
        try {
            BooleanUtils.toBooleanObject("", " ", "0", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_244() throws Exception {
        // Combination: str="test123", trueString="0", falseString="0", nullString="-1"
        try {
            BooleanUtils.toBooleanObject("test123", "0", "0", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_245() throws Exception {
        // Combination: str="a", trueString="test123", falseString="0", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("a", "test123", "0", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_246() throws Exception {
        // Combination: str="a", trueString="-1", falseString="0", nullString="9223372036854775807"
        try {
            BooleanUtils.toBooleanObject("a", "-1", "0", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_247() throws Exception {
        // Combination: str="a", trueString="!@#", falseString="0", nullString="9223372036854775808"
        try {
            BooleanUtils.toBooleanObject("a", "!@#", "0", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_248() throws Exception {
        // Combination: str="a", trueString="0", falseString="0", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBooleanObject("a", "0", "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_249() throws Exception {
        // Combination: str="-1", trueString="", falseString="-1", nullString=""
        Object actual = BooleanUtils.toBooleanObject("-1", "", "-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_250() throws Exception {
        // Combination: str="1.5", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="-1", nullString=" "
        try {
            BooleanUtils.toBooleanObject("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_251() throws Exception {
        // Combination: str="!@#", trueString="9223372036854775808", falseString="-1", nullString="a"
        try {
            BooleanUtils.toBooleanObject("!@#", "9223372036854775808", "-1", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_252() throws Exception {
        // Combination: str="0", trueString="9223372036854775807", falseString="-1", nullString="test123"
        try {
            BooleanUtils.toBooleanObject("0", "9223372036854775807", "-1", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_253() throws Exception {
        // Combination: str="a", trueString="test123", falseString="-1", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject("a", "test123", "-1", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_254() throws Exception {
        // Combination: str="test123", trueString="-1", falseString="-1", nullString="0"
        try {
            BooleanUtils.toBooleanObject("test123", "-1", "-1", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_255() throws Exception {
        // Combination: str="", trueString=" ", falseString="-1", nullString="-1"
        try {
            BooleanUtils.toBooleanObject("", " ", "-1", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_256() throws Exception {
        // Combination: str=" ", trueString="a", falseString="-1", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject(" ", "a", "-1", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_257() throws Exception {
        // Combination: str="test123", trueString="1.5", falseString="-1", nullString="9223372036854775807"
        try {
            BooleanUtils.toBooleanObject("test123", "1.5", "-1", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_258() throws Exception {
        // Combination: str="test123", trueString="0", falseString="-1", nullString="9223372036854775808"
        try {
            BooleanUtils.toBooleanObject("test123", "0", "-1", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_259() throws Exception {
        // Combination: str="test123", trueString="!@#", falseString="-1", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBooleanObject("test123", "!@#", "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_260() throws Exception {
        // Combination: str="1.5", trueString="9223372036854775808", falseString="1.5", nullString=""
        Object actual = BooleanUtils.toBooleanObject("1.5", "9223372036854775808", "1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_261() throws Exception {
        // Combination: str="-1", trueString="9223372036854775807", falseString="1.5", nullString=" "
        try {
            BooleanUtils.toBooleanObject("-1", "9223372036854775807", "1.5", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_262() throws Exception {
        // Combination: str="0", trueString="", falseString="1.5", nullString="a"
        try {
            BooleanUtils.toBooleanObject("0", "", "1.5", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_263() throws Exception {
        // Combination: str="!@#", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="1.5", nullString="test123"
        try {
            BooleanUtils.toBooleanObject("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_264() throws Exception {
        // Combination: str="test123", trueString="9223372036854775807", falseString="1.5", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject("test123", "9223372036854775807", "1.5", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_265() throws Exception {
        // Combination: str="a", trueString="test123", falseString="1.5", nullString="0"
        try {
            BooleanUtils.toBooleanObject("a", "test123", "1.5", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_266() throws Exception {
        // Combination: str=" ", trueString="a", falseString="1.5", nullString="-1"
        try {
            BooleanUtils.toBooleanObject(" ", "a", "1.5", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_267() throws Exception {
        // Combination: str="", trueString=" ", falseString="1.5", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("", " ", "1.5", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_268() throws Exception {
        // Combination: str="!@#", trueString=" ", falseString="1.5", nullString="9223372036854775807"
        try {
            BooleanUtils.toBooleanObject("!@#", " ", "1.5", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_269() throws Exception {
        // Combination: str="!@#", trueString="a", falseString="1.5", nullString="9223372036854775808"
        try {
            BooleanUtils.toBooleanObject("!@#", "a", "1.5", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_270() throws Exception {
        // Combination: str="!@#", trueString="test123", falseString="1.5", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBooleanObject("!@#", "test123", "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_271() throws Exception {
        // Combination: str="9223372036854775807", trueString="!@#", falseString="9223372036854775807", nullString=""
        Object actual = BooleanUtils.toBooleanObject("9223372036854775807", "!@#", "9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_272() throws Exception {
        // Combination: str="9223372036854775808", trueString="0", falseString="9223372036854775807", nullString=" "
        try {
            BooleanUtils.toBooleanObject("9223372036854775808", "0", "9223372036854775807", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_273() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="-1", falseString="9223372036854775807", nullString="a"
        try {
            BooleanUtils.toBooleanObject("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", "9223372036854775807", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_274() throws Exception {
        // Combination: str="", trueString="1.5", falseString="9223372036854775807", nullString="test123"
        try {
            BooleanUtils.toBooleanObject("", "1.5", "9223372036854775807", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_275() throws Exception {
        // Combination: str=" ", trueString="9223372036854775808", falseString="9223372036854775807", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject(" ", "9223372036854775808", "9223372036854775807", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_276() throws Exception {
        // Combination: str="a", trueString="9223372036854775807", falseString="9223372036854775807", nullString="0"
        try {
            BooleanUtils.toBooleanObject("a", "9223372036854775807", "9223372036854775807", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_277() throws Exception {
        // Combination: str="test123", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="9223372036854775807", nullString="-1"
        try {
            BooleanUtils.toBooleanObject("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_278() throws Exception {
        // Combination: str="!@#", trueString="", falseString="9223372036854775807", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("!@#", "", "9223372036854775807", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_279() throws Exception {
        // Combination: str="0", trueString="a", falseString="9223372036854775807", nullString="9223372036854775807"
        try {
            BooleanUtils.toBooleanObject("0", "a", "9223372036854775807", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_280() throws Exception {
        // Combination: str="-1", trueString=" ", falseString="9223372036854775807", nullString="9223372036854775808"
        try {
            BooleanUtils.toBooleanObject("-1", " ", "9223372036854775807", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_281() throws Exception {
        // Combination: str="1.5", trueString="a", falseString="9223372036854775807", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBooleanObject("1.5", "a", "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_282() throws Exception {
        // Combination: str="9223372036854775808", trueString="-1", falseString="9223372036854775808", nullString=""
        Object actual = BooleanUtils.toBooleanObject("9223372036854775808", "-1", "9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_283() throws Exception {
        // Combination: str="9223372036854775807", trueString="1.5", falseString="9223372036854775808", nullString=" "
        try {
            BooleanUtils.toBooleanObject("9223372036854775807", "1.5", "9223372036854775808", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_284() throws Exception {
        // Combination: str="", trueString="0", falseString="9223372036854775808", nullString="a"
        try {
            BooleanUtils.toBooleanObject("", "0", "9223372036854775808", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_285() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="!@#", falseString="9223372036854775808", nullString="test123"
        try {
            BooleanUtils.toBooleanObject("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", "9223372036854775808", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_286() throws Exception {
        // Combination: str=" ", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="9223372036854775808", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_287() throws Exception {
        // Combination: str="a", trueString="9223372036854775808", falseString="9223372036854775808", nullString="0"
        try {
            BooleanUtils.toBooleanObject("a", "9223372036854775808", "9223372036854775808", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_288() throws Exception {
        // Combination: str="test123", trueString="", falseString="9223372036854775808", nullString="-1"
        try {
            BooleanUtils.toBooleanObject("test123", "", "9223372036854775808", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_289() throws Exception {
        // Combination: str="!@#", trueString="9223372036854775807", falseString="9223372036854775808", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("!@#", "9223372036854775807", "9223372036854775808", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_290() throws Exception {
        // Combination: str="-1", trueString="test123", falseString="9223372036854775808", nullString="9223372036854775807"
        try {
            BooleanUtils.toBooleanObject("-1", "test123", "9223372036854775808", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_291() throws Exception {
        // Combination: str="0", trueString=" ", falseString="9223372036854775808", nullString="9223372036854775808"
        try {
            BooleanUtils.toBooleanObject("0", " ", "9223372036854775808", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_292() throws Exception {
        // Combination: str="0", trueString="a", falseString="9223372036854775808", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBooleanObject("0", "a", "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_293() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="0", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString=""
        Object actual = BooleanUtils.toBooleanObject("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_294() throws Exception {
        // Combination: str="", trueString="-1", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString=" "
        try {
            BooleanUtils.toBooleanObject("", "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_295() throws Exception {
        // Combination: str="9223372036854775807", trueString="a", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="a"
        try {
            BooleanUtils.toBooleanObject("9223372036854775807", "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_296() throws Exception {
        // Combination: str="9223372036854775808", trueString=" ", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="test123"
        try {
            BooleanUtils.toBooleanObject("9223372036854775808", " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_297() throws Exception {
        // Combination: str=" ", trueString="", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject(" ", "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_298() throws Exception {
        // Combination: str="a", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="0"
        try {
            BooleanUtils.toBooleanObject("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_299() throws Exception {
        // Combination: str="test123", trueString="9223372036854775808", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="-1"
        try {
            BooleanUtils.toBooleanObject("test123", "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_300() throws Exception {
        // Combination: str="!@#", trueString="test123", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("!@#", "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_301() throws Exception {
        // Combination: str="1.5", trueString="", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="9223372036854775807"
        try {
            BooleanUtils.toBooleanObject("1.5", "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_302() throws Exception {
        // Combination: str="0", trueString="test123", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="9223372036854775808"
        try {
            BooleanUtils.toBooleanObject("0", "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_303() throws Exception {
        // Combination: str="-1", trueString="a", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBooleanObject("-1", "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_304() throws Exception {
        // Combination: str="1.5", trueString=" ", falseString="9223372036854775808", nullString="9223372036854775808"
        try {
            BooleanUtils.toBooleanObject("1.5", " ", "9223372036854775808", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_305() throws Exception {
        // Combination: str="9223372036854775807", trueString="0", falseString="test123", nullString="test123"
        try {
            BooleanUtils.toBooleanObject("9223372036854775807", "0", "test123", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_306() throws Exception {
        // Combination: str="9223372036854775807", trueString="test123", falseString="!@#", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject("9223372036854775807", "test123", "!@#", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_307() throws Exception {
        // Combination: str="9223372036854775807", trueString="", falseString="0", nullString="0"
        try {
            BooleanUtils.toBooleanObject("9223372036854775807", "", "0", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_308() throws Exception {
        // Combination: str="9223372036854775807", trueString="-1", falseString="-1", nullString="-1"
        try {
            BooleanUtils.toBooleanObject("9223372036854775807", "-1", "-1", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_309() throws Exception {
        // Combination: str="9223372036854775807", trueString="9223372036854775808", falseString="1.5", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("9223372036854775807", "9223372036854775808", "1.5", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_310() throws Exception {
        // Combination: str="9223372036854775808", trueString="!@#", falseString="a", nullString="a"
        try {
            BooleanUtils.toBooleanObject("9223372036854775808", "!@#", "a", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_311() throws Exception {
        // Combination: str="9223372036854775808", trueString="a", falseString="!@#", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject("9223372036854775808", "a", "!@#", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_312() throws Exception {
        // Combination: str="9223372036854775808", trueString="1.5", falseString="0", nullString="0"
        try {
            BooleanUtils.toBooleanObject("9223372036854775808", "1.5", "0", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_313() throws Exception {
        // Combination: str="9223372036854775808", trueString="9223372036854775807", falseString="-1", nullString="-1"
        try {
            BooleanUtils.toBooleanObject("9223372036854775808", "9223372036854775807", "-1", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_314() throws Exception {
        // Combination: str="9223372036854775808", trueString="test123", falseString="1.5", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("9223372036854775808", "test123", "1.5", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_315() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="", falseString=" ", nullString=" "
        try {
            BooleanUtils.toBooleanObject("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", " ", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_316() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString=" ", falseString="!@#", nullString="!@#"
        try {
            BooleanUtils.toBooleanObject("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", "!@#", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_317() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="a", falseString="0", nullString="0"
        try {
            BooleanUtils.toBooleanObject("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", "0", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_318() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="test123", falseString="-1", nullString="-1"
        try {
            BooleanUtils.toBooleanObject("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", "-1", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_319() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="1.5", falseString="1.5", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", "1.5", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_320() throws Exception {
        // Combination: str="1.5", trueString="test123", falseString="9223372036854775807", nullString=""
        try {
            BooleanUtils.toBooleanObject("1.5", "test123", "9223372036854775807", "");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_321() throws Exception {
        // Combination: str=" ", trueString="!@#", falseString="1.5", nullString=" "
        assertNull(BooleanUtils.toBooleanObject(" ", "!@#", "1.5", " "));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_322() throws Exception {
        // Combination: str="", trueString="!@#", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString=""
        assertNull(BooleanUtils.toBooleanObject("", "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", ""));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_323() throws Exception {
        // Combination: str="", trueString="0", falseString="1.5", nullString=""
        assertNull(BooleanUtils.toBooleanObject("", "0", "1.5", ""));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_324() throws Exception {
        // Combination: str="", trueString="-1", falseString="1.5", nullString="test123"
        try {
            BooleanUtils.toBooleanObject("", "-1", "1.5", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_325() throws Exception {
        // Combination: str="", trueString="-1", falseString="", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toBooleanObject("", "-1", "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_326() throws Exception {
        // Combination: str="a", trueString="1.5", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString=""
        try {
            BooleanUtils.toBooleanObject("a", "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_327() throws Exception {
        // Combination: str="", trueString="1.5", falseString="", nullString="a"
        Object actual = BooleanUtils.toBooleanObject("", "1.5", "", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_328() throws Exception {
        // Combination: str="", trueString="1.5", falseString="", nullString="9223372036854775808"
        Object actual = BooleanUtils.toBooleanObject("", "1.5", "", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_329() throws Exception {
        // Combination: str=" ", trueString="9223372036854775807", falseString="a", nullString=""
        try {
            BooleanUtils.toBooleanObject(" ", "9223372036854775807", "a", "");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_330() throws Exception {
        // Combination: str="", trueString="9223372036854775807", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString=""
        assertNull(BooleanUtils.toBooleanObject("", "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", ""));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_331() throws Exception {
        // Combination: str="", trueString="9223372036854775808", falseString=" ", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBooleanObject("", "9223372036854775808", " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_332() throws Exception {
        // Combination: str="", trueString="9223372036854775808", falseString="test123", nullString=""
        assertNull(BooleanUtils.toBooleanObject("", "9223372036854775808", "test123", ""));
    }

    @Test(timeout = 4000)
    public void test_toBooleanObject_pairwise_333() throws Exception {
        // Combination: str="9223372036854775807", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="test123", nullString="1.5"
        try {
            BooleanUtils.toBooleanObject("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_334() throws Exception {
        // Combination: str="", trueString="", falseString=""
        Object actual = BooleanUtils.toBoolean("", "", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_335() throws Exception {
        // Combination: str=" ", trueString=" ", falseString=""
        Object actual = BooleanUtils.toBoolean(" ", " ", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_336() throws Exception {
        // Combination: str="a", trueString="a", falseString=""
        Object actual = BooleanUtils.toBoolean("a", "a", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_337() throws Exception {
        // Combination: str="test123", trueString="test123", falseString=""
        Object actual = BooleanUtils.toBoolean("test123", "test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_338() throws Exception {
        // Combination: str="!@#", trueString="!@#", falseString=""
        Object actual = BooleanUtils.toBoolean("!@#", "!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_339() throws Exception {
        // Combination: str="0", trueString="0", falseString=""
        Object actual = BooleanUtils.toBoolean("0", "0", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_340() throws Exception {
        // Combination: str="-1", trueString="-1", falseString=""
        Object actual = BooleanUtils.toBoolean("-1", "-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_341() throws Exception {
        // Combination: str="1.5", trueString="1.5", falseString=""
        Object actual = BooleanUtils.toBoolean("1.5", "1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_342() throws Exception {
        // Combination: str="9223372036854775807", trueString="9223372036854775807", falseString=""
        Object actual = BooleanUtils.toBoolean("9223372036854775807", "9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_343() throws Exception {
        // Combination: str="9223372036854775808", trueString="9223372036854775808", falseString=""
        Object actual = BooleanUtils.toBoolean("9223372036854775808", "9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_344() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString=""
        Object actual = BooleanUtils.toBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_345() throws Exception {
        // Combination: str="", trueString=" ", falseString=" "
        try {
            BooleanUtils.toBoolean("", " ", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_346() throws Exception {
        // Combination: str=" ", trueString="", falseString=" "
        Object actual = BooleanUtils.toBoolean(" ", "", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_347() throws Exception {
        // Combination: str="a", trueString="test123", falseString=" "
        try {
            BooleanUtils.toBoolean("a", "test123", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_348() throws Exception {
        // Combination: str="test123", trueString="a", falseString=" "
        try {
            BooleanUtils.toBoolean("test123", "a", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_349() throws Exception {
        // Combination: str="!@#", trueString="0", falseString=" "
        try {
            BooleanUtils.toBoolean("!@#", "0", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_350() throws Exception {
        // Combination: str="0", trueString="!@#", falseString=" "
        try {
            BooleanUtils.toBoolean("0", "!@#", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_351() throws Exception {
        // Combination: str="-1", trueString="1.5", falseString=" "
        try {
            BooleanUtils.toBoolean("-1", "1.5", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_352() throws Exception {
        // Combination: str="1.5", trueString="-1", falseString=" "
        try {
            BooleanUtils.toBoolean("1.5", "-1", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_353() throws Exception {
        // Combination: str="9223372036854775807", trueString="9223372036854775808", falseString=" "
        try {
            BooleanUtils.toBoolean("9223372036854775807", "9223372036854775808", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_354() throws Exception {
        // Combination: str="9223372036854775808", trueString="9223372036854775807", falseString=" "
        try {
            BooleanUtils.toBoolean("9223372036854775808", "9223372036854775807", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_355() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="", falseString=" "
        try {
            BooleanUtils.toBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_356() throws Exception {
        // Combination: str="", trueString="a", falseString="a"
        try {
            BooleanUtils.toBoolean("", "a", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_357() throws Exception {
        // Combination: str=" ", trueString="test123", falseString="a"
        try {
            BooleanUtils.toBoolean(" ", "test123", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_358() throws Exception {
        // Combination: str="a", trueString="", falseString="a"
        Object actual = BooleanUtils.toBoolean("a", "", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_359() throws Exception {
        // Combination: str="test123", trueString=" ", falseString="a"
        try {
            BooleanUtils.toBoolean("test123", " ", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_360() throws Exception {
        // Combination: str="!@#", trueString="-1", falseString="a"
        try {
            BooleanUtils.toBoolean("!@#", "-1", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_361() throws Exception {
        // Combination: str="0", trueString="1.5", falseString="a"
        try {
            BooleanUtils.toBoolean("0", "1.5", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_362() throws Exception {
        // Combination: str="-1", trueString="!@#", falseString="a"
        try {
            BooleanUtils.toBoolean("-1", "!@#", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_363() throws Exception {
        // Combination: str="1.5", trueString="0", falseString="a"
        try {
            BooleanUtils.toBoolean("1.5", "0", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_364() throws Exception {
        // Combination: str="9223372036854775807", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="a"
        try {
            BooleanUtils.toBoolean("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_365() throws Exception {
        // Combination: str="9223372036854775808", trueString="", falseString="a"
        try {
            BooleanUtils.toBoolean("9223372036854775808", "", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_366() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="9223372036854775807", falseString="a"
        try {
            BooleanUtils.toBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_367() throws Exception {
        // Combination: str="", trueString="test123", falseString="test123"
        try {
            BooleanUtils.toBoolean("", "test123", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_368() throws Exception {
        // Combination: str=" ", trueString="a", falseString="test123"
        try {
            BooleanUtils.toBoolean(" ", "a", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_369() throws Exception {
        // Combination: str="a", trueString=" ", falseString="test123"
        try {
            BooleanUtils.toBoolean("a", " ", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_370() throws Exception {
        // Combination: str="test123", trueString="", falseString="test123"
        Object actual = BooleanUtils.toBoolean("test123", "", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_371() throws Exception {
        // Combination: str="!@#", trueString="1.5", falseString="test123"
        try {
            BooleanUtils.toBoolean("!@#", "1.5", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_372() throws Exception {
        // Combination: str="0", trueString="-1", falseString="test123"
        try {
            BooleanUtils.toBoolean("0", "-1", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_373() throws Exception {
        // Combination: str="-1", trueString="0", falseString="test123"
        try {
            BooleanUtils.toBoolean("-1", "0", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_374() throws Exception {
        // Combination: str="1.5", trueString="!@#", falseString="test123"
        try {
            BooleanUtils.toBoolean("1.5", "!@#", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_375() throws Exception {
        // Combination: str="9223372036854775807", trueString="", falseString="test123"
        try {
            BooleanUtils.toBoolean("9223372036854775807", "", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_376() throws Exception {
        // Combination: str="9223372036854775808", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="test123"
        try {
            BooleanUtils.toBoolean("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_377() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="9223372036854775808", falseString="test123"
        try {
            BooleanUtils.toBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_378() throws Exception {
        // Combination: str="", trueString="!@#", falseString="!@#"
        try {
            BooleanUtils.toBoolean("", "!@#", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_379() throws Exception {
        // Combination: str=" ", trueString="0", falseString="!@#"
        try {
            BooleanUtils.toBoolean(" ", "0", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_380() throws Exception {
        // Combination: str="a", trueString="-1", falseString="!@#"
        try {
            BooleanUtils.toBoolean("a", "-1", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_381() throws Exception {
        // Combination: str="test123", trueString="1.5", falseString="!@#"
        try {
            BooleanUtils.toBoolean("test123", "1.5", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_382() throws Exception {
        // Combination: str="!@#", trueString="", falseString="!@#"
        Object actual = BooleanUtils.toBoolean("!@#", "", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_383() throws Exception {
        // Combination: str="0", trueString=" ", falseString="!@#"
        try {
            BooleanUtils.toBoolean("0", " ", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_384() throws Exception {
        // Combination: str="-1", trueString="a", falseString="!@#"
        try {
            BooleanUtils.toBoolean("-1", "a", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_385() throws Exception {
        // Combination: str="1.5", trueString="test123", falseString="!@#"
        try {
            BooleanUtils.toBoolean("1.5", "test123", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_386() throws Exception {
        // Combination: str="9223372036854775807", trueString=" ", falseString="!@#"
        try {
            BooleanUtils.toBoolean("9223372036854775807", " ", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_387() throws Exception {
        // Combination: str="9223372036854775808", trueString=" ", falseString="!@#"
        try {
            BooleanUtils.toBoolean("9223372036854775808", " ", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_388() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString=" ", falseString="!@#"
        try {
            BooleanUtils.toBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_389() throws Exception {
        // Combination: str="", trueString="0", falseString="0"
        try {
            BooleanUtils.toBoolean("", "0", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_390() throws Exception {
        // Combination: str=" ", trueString="!@#", falseString="0"
        try {
            BooleanUtils.toBoolean(" ", "!@#", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_391() throws Exception {
        // Combination: str="a", trueString="1.5", falseString="0"
        try {
            BooleanUtils.toBoolean("a", "1.5", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_392() throws Exception {
        // Combination: str="test123", trueString="-1", falseString="0"
        try {
            BooleanUtils.toBoolean("test123", "-1", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_393() throws Exception {
        // Combination: str="!@#", trueString=" ", falseString="0"
        try {
            BooleanUtils.toBoolean("!@#", " ", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_394() throws Exception {
        // Combination: str="0", trueString="", falseString="0"
        Object actual = BooleanUtils.toBoolean("0", "", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_395() throws Exception {
        // Combination: str="-1", trueString="test123", falseString="0"
        try {
            BooleanUtils.toBoolean("-1", "test123", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_396() throws Exception {
        // Combination: str="1.5", trueString="a", falseString="0"
        try {
            BooleanUtils.toBoolean("1.5", "a", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_397() throws Exception {
        // Combination: str="9223372036854775807", trueString="a", falseString="0"
        try {
            BooleanUtils.toBoolean("9223372036854775807", "a", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_398() throws Exception {
        // Combination: str="9223372036854775808", trueString="a", falseString="0"
        try {
            BooleanUtils.toBoolean("9223372036854775808", "a", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_399() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="a", falseString="0"
        try {
            BooleanUtils.toBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_400() throws Exception {
        // Combination: str="", trueString="-1", falseString="-1"
        try {
            BooleanUtils.toBoolean("", "-1", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_401() throws Exception {
        // Combination: str=" ", trueString="1.5", falseString="-1"
        try {
            BooleanUtils.toBoolean(" ", "1.5", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_402() throws Exception {
        // Combination: str="a", trueString="!@#", falseString="-1"
        try {
            BooleanUtils.toBoolean("a", "!@#", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_403() throws Exception {
        // Combination: str="test123", trueString="0", falseString="-1"
        try {
            BooleanUtils.toBoolean("test123", "0", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_404() throws Exception {
        // Combination: str="!@#", trueString="a", falseString="-1"
        try {
            BooleanUtils.toBoolean("!@#", "a", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_405() throws Exception {
        // Combination: str="0", trueString="test123", falseString="-1"
        try {
            BooleanUtils.toBoolean("0", "test123", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_406() throws Exception {
        // Combination: str="-1", trueString="", falseString="-1"
        Object actual = BooleanUtils.toBoolean("-1", "", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_407() throws Exception {
        // Combination: str="1.5", trueString=" ", falseString="-1"
        try {
            BooleanUtils.toBoolean("1.5", " ", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_408() throws Exception {
        // Combination: str="9223372036854775807", trueString="test123", falseString="-1"
        try {
            BooleanUtils.toBoolean("9223372036854775807", "test123", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_409() throws Exception {
        // Combination: str="9223372036854775808", trueString="test123", falseString="-1"
        try {
            BooleanUtils.toBoolean("9223372036854775808", "test123", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_410() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="test123", falseString="-1"
        try {
            BooleanUtils.toBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_411() throws Exception {
        // Combination: str="", trueString="1.5", falseString="1.5"
        try {
            BooleanUtils.toBoolean("", "1.5", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_412() throws Exception {
        // Combination: str=" ", trueString="-1", falseString="1.5"
        try {
            BooleanUtils.toBoolean(" ", "-1", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_413() throws Exception {
        // Combination: str="a", trueString="0", falseString="1.5"
        try {
            BooleanUtils.toBoolean("a", "0", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_414() throws Exception {
        // Combination: str="test123", trueString="!@#", falseString="1.5"
        try {
            BooleanUtils.toBoolean("test123", "!@#", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_415() throws Exception {
        // Combination: str="!@#", trueString="test123", falseString="1.5"
        try {
            BooleanUtils.toBoolean("!@#", "test123", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_416() throws Exception {
        // Combination: str="0", trueString="a", falseString="1.5"
        try {
            BooleanUtils.toBoolean("0", "a", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_417() throws Exception {
        // Combination: str="-1", trueString=" ", falseString="1.5"
        try {
            BooleanUtils.toBoolean("-1", " ", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_418() throws Exception {
        // Combination: str="1.5", trueString="", falseString="1.5"
        Object actual = BooleanUtils.toBoolean("1.5", "", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_419() throws Exception {
        // Combination: str="9223372036854775807", trueString="!@#", falseString="1.5"
        try {
            BooleanUtils.toBoolean("9223372036854775807", "!@#", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_420() throws Exception {
        // Combination: str="9223372036854775808", trueString="!@#", falseString="1.5"
        try {
            BooleanUtils.toBoolean("9223372036854775808", "!@#", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_421() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="!@#", falseString="1.5"
        try {
            BooleanUtils.toBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_422() throws Exception {
        // Combination: str="", trueString="9223372036854775807", falseString="9223372036854775807"
        try {
            BooleanUtils.toBoolean("", "9223372036854775807", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_423() throws Exception {
        // Combination: str=" ", trueString="9223372036854775808", falseString="9223372036854775807"
        try {
            BooleanUtils.toBoolean(" ", "9223372036854775808", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_424() throws Exception {
        // Combination: str="a", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="9223372036854775807"
        try {
            BooleanUtils.toBoolean("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_425() throws Exception {
        // Combination: str="test123", trueString="", falseString="9223372036854775807"
        try {
            BooleanUtils.toBoolean("test123", "", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_426() throws Exception {
        // Combination: str="!@#", trueString=" ", falseString="9223372036854775807"
        try {
            BooleanUtils.toBoolean("!@#", " ", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_427() throws Exception {
        // Combination: str="0", trueString="a", falseString="9223372036854775807"
        try {
            BooleanUtils.toBoolean("0", "a", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_428() throws Exception {
        // Combination: str="-1", trueString="test123", falseString="9223372036854775807"
        try {
            BooleanUtils.toBoolean("-1", "test123", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_429() throws Exception {
        // Combination: str="1.5", trueString="!@#", falseString="9223372036854775807"
        try {
            BooleanUtils.toBoolean("1.5", "!@#", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_430() throws Exception {
        // Combination: str="9223372036854775807", trueString="0", falseString="9223372036854775807"
        Object actual = BooleanUtils.toBoolean("9223372036854775807", "0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_431() throws Exception {
        // Combination: str="9223372036854775808", trueString="-1", falseString="9223372036854775807"
        try {
            BooleanUtils.toBoolean("9223372036854775808", "-1", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_432() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="1.5", falseString="9223372036854775807"
        try {
            BooleanUtils.toBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_433() throws Exception {
        // Combination: str="", trueString="9223372036854775808", falseString="9223372036854775808"
        try {
            BooleanUtils.toBoolean("", "9223372036854775808", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_434() throws Exception {
        // Combination: str=" ", trueString="9223372036854775807", falseString="9223372036854775808"
        try {
            BooleanUtils.toBoolean(" ", "9223372036854775807", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_435() throws Exception {
        // Combination: str="a", trueString="", falseString="9223372036854775808"
        try {
            BooleanUtils.toBoolean("a", "", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_436() throws Exception {
        // Combination: str="test123", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="9223372036854775808"
        try {
            BooleanUtils.toBoolean("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_437() throws Exception {
        // Combination: str="!@#", trueString=" ", falseString="9223372036854775808"
        try {
            BooleanUtils.toBoolean("!@#", " ", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_438() throws Exception {
        // Combination: str="0", trueString="a", falseString="9223372036854775808"
        try {
            BooleanUtils.toBoolean("0", "a", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_439() throws Exception {
        // Combination: str="-1", trueString="test123", falseString="9223372036854775808"
        try {
            BooleanUtils.toBoolean("-1", "test123", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_440() throws Exception {
        // Combination: str="1.5", trueString="!@#", falseString="9223372036854775808"
        try {
            BooleanUtils.toBoolean("1.5", "!@#", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_441() throws Exception {
        // Combination: str="9223372036854775807", trueString="-1", falseString="9223372036854775808"
        try {
            BooleanUtils.toBoolean("9223372036854775807", "-1", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_442() throws Exception {
        // Combination: str="9223372036854775808", trueString="0", falseString="9223372036854775808"
        Object actual = BooleanUtils.toBoolean("9223372036854775808", "0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_443() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="0", falseString="9223372036854775808"
        try {
            BooleanUtils.toBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_444() throws Exception {
        // Combination: str="", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBoolean("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_445() throws Exception {
        // Combination: str=" ", trueString="", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBoolean(" ", "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_446() throws Exception {
        // Combination: str="a", trueString="9223372036854775807", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBoolean("a", "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_447() throws Exception {
        // Combination: str="test123", trueString="9223372036854775808", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBoolean("test123", "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_448() throws Exception {
        // Combination: str="!@#", trueString=" ", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBoolean("!@#", " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_449() throws Exception {
        // Combination: str="0", trueString="a", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBoolean("0", "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_450() throws Exception {
        // Combination: str="-1", trueString="test123", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBoolean("-1", "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_451() throws Exception {
        // Combination: str="1.5", trueString="!@#", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBoolean("1.5", "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_452() throws Exception {
        // Combination: str="9223372036854775807", trueString="1.5", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBoolean("9223372036854775807", "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_453() throws Exception {
        // Combination: str="9223372036854775808", trueString="0", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            BooleanUtils.toBoolean("9223372036854775808", "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_454() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", trueString="-1", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_455() throws Exception {
        // Combination: str="9223372036854775808", trueString="1.5", falseString="9223372036854775808"
        Object actual = BooleanUtils.toBoolean("9223372036854775808", "1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_456() throws Exception {
        // Combination: str="test123", trueString="9223372036854775807", falseString="test123"
        Object actual = BooleanUtils.toBoolean("test123", "9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_457() throws Exception {
        // Combination: str="!@#", trueString="9223372036854775807", falseString="!@#"
        Object actual = BooleanUtils.toBoolean("!@#", "9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_458() throws Exception {
        // Combination: str="0", trueString="9223372036854775807", falseString="0"
        Object actual = BooleanUtils.toBoolean("0", "9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_459() throws Exception {
        // Combination: str="-1", trueString="9223372036854775807", falseString="-1"
        Object actual = BooleanUtils.toBoolean("-1", "9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_460() throws Exception {
        // Combination: str="1.5", trueString="9223372036854775807", falseString="1.5"
        Object actual = BooleanUtils.toBoolean("1.5", "9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_461() throws Exception {
        // Combination: str="a", trueString="9223372036854775808", falseString="a"
        Object actual = BooleanUtils.toBoolean("a", "9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_462() throws Exception {
        // Combination: str="!@#", trueString="9223372036854775808", falseString="!@#"
        Object actual = BooleanUtils.toBoolean("!@#", "9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_463() throws Exception {
        // Combination: str="0", trueString="9223372036854775808", falseString="0"
        Object actual = BooleanUtils.toBoolean("0", "9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_464() throws Exception {
        // Combination: str="-1", trueString="9223372036854775808", falseString="-1"
        Object actual = BooleanUtils.toBoolean("-1", "9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_465() throws Exception {
        // Combination: str="1.5", trueString="9223372036854775808", falseString="1.5"
        Object actual = BooleanUtils.toBoolean("1.5", "9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_466() throws Exception {
        // Combination: str=" ", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString=" "
        Object actual = BooleanUtils.toBoolean(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_467() throws Exception {
        // Combination: str="!@#", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="!@#"
        Object actual = BooleanUtils.toBoolean("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_468() throws Exception {
        // Combination: str="0", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="0"
        Object actual = BooleanUtils.toBoolean("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_469() throws Exception {
        // Combination: str="-1", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="-1"
        Object actual = BooleanUtils.toBoolean("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toBoolean_pairwise_470() throws Exception {
        // Combination: str="1.5", trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="1.5"
        Object actual = BooleanUtils.toBoolean("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_471() throws Exception {
        // Combination: bool=true, trueString="", falseString="", nullString=""
        Object actual = BooleanUtils.toString(true, "", "", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_472() throws Exception {
        // Combination: bool=true, trueString=" ", falseString=" ", nullString=" "
        Object actual = BooleanUtils.toString(true, " ", " ", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_473() throws Exception {
        // Combination: bool=true, trueString="a", falseString="a", nullString="a"
        Object actual = BooleanUtils.toString(true, "a", "a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_474() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="test123", nullString="test123"
        Object actual = BooleanUtils.toString(true, "test123", "test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_475() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="!@#", nullString="!@#"
        Object actual = BooleanUtils.toString(true, "!@#", "!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_476() throws Exception {
        // Combination: bool=true, trueString="0", falseString="0", nullString="0"
        Object actual = BooleanUtils.toString(true, "0", "0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_477() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="-1", nullString="-1"
        Object actual = BooleanUtils.toString(true, "-1", "-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_478() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="1.5", nullString="1.5"
        Object actual = BooleanUtils.toString(true, "1.5", "1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_479() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="9223372036854775807", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_480() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="9223372036854775808", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_481() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_482() throws Exception {
        // Combination: bool=false, trueString="a", falseString="", nullString=" "
        Object actual = BooleanUtils.toString(false, "a", "", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_483() throws Exception {
        // Combination: bool=false, trueString="test123", falseString=" ", nullString=""
        Object actual = BooleanUtils.toString(false, "test123", " ", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_484() throws Exception {
        // Combination: bool=false, trueString="", falseString="a", nullString="test123"
        Object actual = BooleanUtils.toString(false, "", "a", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_485() throws Exception {
        // Combination: bool=false, trueString=" ", falseString="test123", nullString="a"
        Object actual = BooleanUtils.toString(false, " ", "test123", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_486() throws Exception {
        // Combination: bool=false, trueString="-1", falseString="!@#", nullString="0"
        Object actual = BooleanUtils.toString(false, "-1", "!@#", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_487() throws Exception {
        // Combination: bool=false, trueString="1.5", falseString="0", nullString="!@#"
        Object actual = BooleanUtils.toString(false, "1.5", "0", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_488() throws Exception {
        // Combination: bool=false, trueString="!@#", falseString="-1", nullString="1.5"
        Object actual = BooleanUtils.toString(false, "!@#", "-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_489() throws Exception {
        // Combination: bool=false, trueString="0", falseString="1.5", nullString="-1"
        Object actual = BooleanUtils.toString(false, "0", "1.5", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_490() throws Exception {
        // Combination: bool=false, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="9223372036854775807", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(false, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_491() throws Exception {
        // Combination: bool=false, trueString="", falseString="9223372036854775808", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(false, "", "9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_492() throws Exception {
        // Combination: bool=false, trueString="9223372036854775807", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString=""
        Object actual = BooleanUtils.toString(false, "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_493() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="a", nullString=""
        Object actual = BooleanUtils.toString(true, " ", "a", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_494() throws Exception {
        // Combination: bool=true, trueString="a", falseString="test123", nullString=""
        Object actual = BooleanUtils.toString(true, "a", "test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_495() throws Exception {
        // Combination: bool=true, trueString="0", falseString="!@#", nullString=""
        Object actual = BooleanUtils.toString(true, "0", "!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_496() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="0", nullString=""
        Object actual = BooleanUtils.toString(true, "!@#", "0", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_497() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="-1", nullString=""
        Object actual = BooleanUtils.toString(true, "1.5", "-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_498() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="1.5", nullString=""
        Object actual = BooleanUtils.toString(true, "-1", "1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_499() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="9223372036854775807", nullString=""
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_500() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="9223372036854775808", nullString=""
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_501() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="a", nullString=" "
        Object actual = BooleanUtils.toString(true, "test123", "a", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_502() throws Exception {
        // Combination: bool=true, trueString="", falseString="test123", nullString=" "
        Object actual = BooleanUtils.toString(true, "", "test123", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_503() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="!@#", nullString=" "
        Object actual = BooleanUtils.toString(true, "1.5", "!@#", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_504() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="0", nullString=" "
        Object actual = BooleanUtils.toString(true, "-1", "0", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_505() throws Exception {
        // Combination: bool=true, trueString="0", falseString="-1", nullString=" "
        Object actual = BooleanUtils.toString(true, "0", "-1", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_506() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="1.5", nullString=" "
        Object actual = BooleanUtils.toString(true, "!@#", "1.5", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_507() throws Exception {
        // Combination: bool=true, trueString="", falseString="9223372036854775807", nullString=" "
        Object actual = BooleanUtils.toString(true, "", "9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_508() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="9223372036854775808", nullString=" "
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_509() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString=" "
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_510() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="", nullString="a"
        Object actual = BooleanUtils.toString(true, "test123", "", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_511() throws Exception {
        // Combination: bool=true, trueString="", falseString=" ", nullString="a"
        Object actual = BooleanUtils.toString(true, "", " ", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_512() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="!@#", nullString="a"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "!@#", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_513() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="0", nullString="a"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "0", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_514() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="-1", nullString="a"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_515() throws Exception {
        // Combination: bool=true, trueString="", falseString="1.5", nullString="a"
        Object actual = BooleanUtils.toString(true, "", "1.5", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_516() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="9223372036854775807", nullString="a"
        Object actual = BooleanUtils.toString(true, "!@#", "9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_517() throws Exception {
        // Combination: bool=true, trueString="0", falseString="9223372036854775808", nullString="a"
        Object actual = BooleanUtils.toString(true, "0", "9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_518() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="a"
        Object actual = BooleanUtils.toString(true, "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_519() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="", nullString="test123"
        Object actual = BooleanUtils.toString(true, " ", "", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_520() throws Exception {
        // Combination: bool=true, trueString="a", falseString=" ", nullString="test123"
        Object actual = BooleanUtils.toString(true, "a", " ", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_521() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="!@#", nullString="test123"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "!@#", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_522() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="0", nullString="test123"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "0", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_523() throws Exception {
        // Combination: bool=true, trueString="", falseString="-1", nullString="test123"
        Object actual = BooleanUtils.toString(true, "", "-1", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_524() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="1.5", nullString="test123"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_525() throws Exception {
        // Combination: bool=true, trueString="0", falseString="9223372036854775807", nullString="test123"
        Object actual = BooleanUtils.toString(true, "0", "9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_526() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="9223372036854775808", nullString="test123"
        Object actual = BooleanUtils.toString(true, "!@#", "9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_527() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="test123"
        Object actual = BooleanUtils.toString(true, "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_528() throws Exception {
        // Combination: bool=true, trueString="0", falseString="", nullString="!@#"
        Object actual = BooleanUtils.toString(true, "0", "", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_529() throws Exception {
        // Combination: bool=true, trueString="-1", falseString=" ", nullString="!@#"
        Object actual = BooleanUtils.toString(true, "-1", " ", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_530() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="a", nullString="!@#"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "a", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_531() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="test123", nullString="!@#"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "test123", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_532() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="-1", nullString="!@#"
        Object actual = BooleanUtils.toString(true, " ", "-1", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_533() throws Exception {
        // Combination: bool=true, trueString="a", falseString="1.5", nullString="!@#"
        Object actual = BooleanUtils.toString(true, "a", "1.5", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_534() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="9223372036854775807", nullString="!@#"
        Object actual = BooleanUtils.toString(true, "test123", "9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_535() throws Exception {
        // Combination: bool=true, trueString="", falseString="9223372036854775808", nullString="!@#"
        Object actual = BooleanUtils.toString(true, "", "9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_536() throws Exception {
        // Combination: bool=true, trueString="", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="!@#"
        Object actual = BooleanUtils.toString(true, "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_537() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="", nullString="0"
        Object actual = BooleanUtils.toString(true, "!@#", "", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_538() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString=" ", nullString="0"
        Object actual = BooleanUtils.toString(true, "1.5", " ", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_539() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="a", nullString="0"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "a", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_540() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="test123", nullString="0"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "test123", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_541() throws Exception {
        // Combination: bool=true, trueString="a", falseString="-1", nullString="0"
        Object actual = BooleanUtils.toString(true, "a", "-1", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_542() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="1.5", nullString="0"
        Object actual = BooleanUtils.toString(true, " ", "1.5", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_543() throws Exception {
        // Combination: bool=true, trueString="", falseString="9223372036854775807", nullString="0"
        Object actual = BooleanUtils.toString(true, "", "9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_544() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="9223372036854775808", nullString="0"
        Object actual = BooleanUtils.toString(true, "test123", "9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_545() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="0"
        Object actual = BooleanUtils.toString(true, " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_546() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="", nullString="-1"
        Object actual = BooleanUtils.toString(true, "1.5", "", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_547() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString=" ", nullString="-1"
        Object actual = BooleanUtils.toString(true, "!@#", " ", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_548() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="a", nullString="-1"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_549() throws Exception {
        // Combination: bool=true, trueString="", falseString="test123", nullString="-1"
        Object actual = BooleanUtils.toString(true, "", "test123", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_550() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="!@#", nullString="-1"
        Object actual = BooleanUtils.toString(true, " ", "!@#", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_551() throws Exception {
        // Combination: bool=true, trueString="a", falseString="0", nullString="-1"
        Object actual = BooleanUtils.toString(true, "a", "0", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_552() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="9223372036854775807", nullString="-1"
        Object actual = BooleanUtils.toString(true, " ", "9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_553() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="9223372036854775808", nullString="-1"
        Object actual = BooleanUtils.toString(true, " ", "9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_554() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="-1"
        Object actual = BooleanUtils.toString(true, "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_555() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="", nullString="1.5"
        Object actual = BooleanUtils.toString(true, "-1", "", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_556() throws Exception {
        // Combination: bool=true, trueString="0", falseString=" ", nullString="1.5"
        Object actual = BooleanUtils.toString(true, "0", " ", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_557() throws Exception {
        // Combination: bool=true, trueString="", falseString="a", nullString="1.5"
        Object actual = BooleanUtils.toString(true, "", "a", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_558() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="test123", nullString="1.5"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_559() throws Exception {
        // Combination: bool=true, trueString="a", falseString="!@#", nullString="1.5"
        Object actual = BooleanUtils.toString(true, "a", "!@#", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_560() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="0", nullString="1.5"
        Object actual = BooleanUtils.toString(true, " ", "0", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_561() throws Exception {
        // Combination: bool=true, trueString="a", falseString="9223372036854775807", nullString="1.5"
        Object actual = BooleanUtils.toString(true, "a", "9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_562() throws Exception {
        // Combination: bool=true, trueString="a", falseString="9223372036854775808", nullString="1.5"
        Object actual = BooleanUtils.toString(true, "a", "9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_563() throws Exception {
        // Combination: bool=true, trueString="a", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="1.5"
        Object actual = BooleanUtils.toString(true, "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_564() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_565() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString=" ", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_566() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="a", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "!@#", "a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_567() throws Exception {
        // Combination: bool=true, trueString="0", falseString="test123", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "0", "test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_568() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="!@#", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "test123", "!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_569() throws Exception {
        // Combination: bool=true, trueString="", falseString="0", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "", "0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_570() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="-1", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, " ", "-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_571() throws Exception {
        // Combination: bool=true, trueString="a", falseString="1.5", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "a", "1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_572() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_573() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_574() throws Exception {
        // Combination: bool=true, trueString="", falseString=" ", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "", " ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_575() throws Exception {
        // Combination: bool=true, trueString="0", falseString="a", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "0", "a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_576() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="test123", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "!@#", "test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_577() throws Exception {
        // Combination: bool=true, trueString="", falseString="!@#", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "", "!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_578() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="0", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "test123", "0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_579() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="-1", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, " ", "-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_580() throws Exception {
        // Combination: bool=true, trueString="a", falseString="1.5", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "a", "1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_581() throws Exception {
        // Combination: bool=true, trueString="0", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_582() throws Exception {
        // Combination: bool=false, trueString="9223372036854775808", falseString="", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(false, "9223372036854775808", "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_583() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString=" ", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_584() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="a", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "-1", "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_585() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="test123", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "1.5", "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_586() throws Exception {
        // Combination: bool=true, trueString="", falseString="!@#", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "", "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_587() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="0", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, " ", "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_588() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="-1", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "test123", "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_589() throws Exception {
        // Combination: bool=true, trueString="a", falseString="1.5", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "a", "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_590() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="9223372036854775807", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "!@#", "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_591() throws Exception {
        // Combination: bool=true, trueString="0", falseString="9223372036854775808", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "0", "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_592() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="1.5", nullString="1.5"
        Object actual = BooleanUtils.toString(true, "test123", "1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_593() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="test123", nullString="test123"
        Object actual = BooleanUtils.toString(true, "-1", "test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_594() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="9223372036854775807", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "-1", "9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_595() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="9223372036854775808", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "-1", "9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_596() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="a", nullString="a"
        Object actual = BooleanUtils.toString(true, "1.5", "a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_597() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="9223372036854775807", nullString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "1.5", "9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_598() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="9223372036854775808", nullString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "1.5", "9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_599() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="-1", nullString="-1"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_600() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="1.5", nullString="1.5"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_601() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString=" ", nullString="-1"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", " ", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_602() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="-1", nullString="1.5"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_603() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="1.5", nullString=""
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_604() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="", nullString=" "
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_605() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="!@#", nullString="!@#"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_606() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="0", nullString="0"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_607() throws Exception {
        // Combination: bool=true, trueString="", falseString=""
        Object actual = BooleanUtils.toString(true, "", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_608() throws Exception {
        // Combination: bool=true, trueString=" ", falseString=" "
        Object actual = BooleanUtils.toString(true, " ", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_609() throws Exception {
        // Combination: bool=true, trueString="a", falseString="a"
        Object actual = BooleanUtils.toString(true, "a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_610() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="test123"
        Object actual = BooleanUtils.toString(true, "test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_611() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="!@#"
        Object actual = BooleanUtils.toString(true, "!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_612() throws Exception {
        // Combination: bool=true, trueString="0", falseString="0"
        Object actual = BooleanUtils.toString(true, "0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_613() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="-1"
        Object actual = BooleanUtils.toString(true, "-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_614() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="1.5"
        Object actual = BooleanUtils.toString(true, "1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_615() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_616() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_617() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_618() throws Exception {
        // Combination: bool=false, trueString=" ", falseString=""
        Object actual = BooleanUtils.toString(false, " ", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_619() throws Exception {
        // Combination: bool=false, trueString="", falseString=" "
        Object actual = BooleanUtils.toString(false, "", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_620() throws Exception {
        // Combination: bool=false, trueString="test123", falseString="a"
        Object actual = BooleanUtils.toString(false, "test123", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_621() throws Exception {
        // Combination: bool=false, trueString="a", falseString="test123"
        Object actual = BooleanUtils.toString(false, "a", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_622() throws Exception {
        // Combination: bool=false, trueString="0", falseString="!@#"
        Object actual = BooleanUtils.toString(false, "0", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_623() throws Exception {
        // Combination: bool=false, trueString="!@#", falseString="0"
        Object actual = BooleanUtils.toString(false, "!@#", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_624() throws Exception {
        // Combination: bool=false, trueString="1.5", falseString="-1"
        Object actual = BooleanUtils.toString(false, "1.5", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_625() throws Exception {
        // Combination: bool=false, trueString="-1", falseString="1.5"
        Object actual = BooleanUtils.toString(false, "-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_626() throws Exception {
        // Combination: bool=false, trueString="9223372036854775808", falseString="9223372036854775807"
        Object actual = BooleanUtils.toString(false, "9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_627() throws Exception {
        // Combination: bool=false, trueString="9223372036854775807", falseString="9223372036854775808"
        Object actual = BooleanUtils.toString(false, "9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_628() throws Exception {
        // Combination: bool=false, trueString="", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(false, "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_629() throws Exception {
        // Combination: bool=true, trueString="", falseString="a"
        Object actual = BooleanUtils.toString(true, "", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_630() throws Exception {
        // Combination: bool=true, trueString="", falseString="test123"
        Object actual = BooleanUtils.toString(true, "", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_631() throws Exception {
        // Combination: bool=true, trueString="", falseString="!@#"
        Object actual = BooleanUtils.toString(true, "", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_632() throws Exception {
        // Combination: bool=true, trueString="", falseString="0"
        Object actual = BooleanUtils.toString(true, "", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_633() throws Exception {
        // Combination: bool=true, trueString="", falseString="-1"
        Object actual = BooleanUtils.toString(true, "", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_634() throws Exception {
        // Combination: bool=true, trueString="", falseString="1.5"
        Object actual = BooleanUtils.toString(true, "", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_635() throws Exception {
        // Combination: bool=true, trueString="", falseString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_636() throws Exception {
        // Combination: bool=true, trueString="", falseString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_637() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="a"
        Object actual = BooleanUtils.toString(true, " ", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_638() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="test123"
        Object actual = BooleanUtils.toString(true, " ", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_639() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="!@#"
        Object actual = BooleanUtils.toString(true, " ", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_640() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="0"
        Object actual = BooleanUtils.toString(true, " ", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_641() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="-1"
        Object actual = BooleanUtils.toString(true, " ", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_642() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="1.5"
        Object actual = BooleanUtils.toString(true, " ", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_643() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, " ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_644() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, " ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_645() throws Exception {
        // Combination: bool=true, trueString=" ", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_646() throws Exception {
        // Combination: bool=true, trueString="a", falseString=""
        Object actual = BooleanUtils.toString(true, "a", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_647() throws Exception {
        // Combination: bool=true, trueString="a", falseString=" "
        Object actual = BooleanUtils.toString(true, "a", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_648() throws Exception {
        // Combination: bool=true, trueString="a", falseString="!@#"
        Object actual = BooleanUtils.toString(true, "a", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_649() throws Exception {
        // Combination: bool=true, trueString="a", falseString="0"
        Object actual = BooleanUtils.toString(true, "a", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_650() throws Exception {
        // Combination: bool=true, trueString="a", falseString="-1"
        Object actual = BooleanUtils.toString(true, "a", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_651() throws Exception {
        // Combination: bool=true, trueString="a", falseString="1.5"
        Object actual = BooleanUtils.toString(true, "a", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_652() throws Exception {
        // Combination: bool=true, trueString="a", falseString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_653() throws Exception {
        // Combination: bool=true, trueString="a", falseString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_654() throws Exception {
        // Combination: bool=true, trueString="a", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_655() throws Exception {
        // Combination: bool=true, trueString="test123", falseString=""
        Object actual = BooleanUtils.toString(true, "test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_656() throws Exception {
        // Combination: bool=true, trueString="test123", falseString=" "
        Object actual = BooleanUtils.toString(true, "test123", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_657() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="!@#"
        Object actual = BooleanUtils.toString(true, "test123", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_658() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="0"
        Object actual = BooleanUtils.toString(true, "test123", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_659() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="-1"
        Object actual = BooleanUtils.toString(true, "test123", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_660() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="1.5"
        Object actual = BooleanUtils.toString(true, "test123", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_661() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_662() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_663() throws Exception {
        // Combination: bool=true, trueString="test123", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_664() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString=""
        Object actual = BooleanUtils.toString(true, "!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_665() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString=" "
        Object actual = BooleanUtils.toString(true, "!@#", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_666() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="a"
        Object actual = BooleanUtils.toString(true, "!@#", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_667() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="test123"
        Object actual = BooleanUtils.toString(true, "!@#", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_668() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="-1"
        Object actual = BooleanUtils.toString(true, "!@#", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_669() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="1.5"
        Object actual = BooleanUtils.toString(true, "!@#", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_670() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_671() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_672() throws Exception {
        // Combination: bool=true, trueString="!@#", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_673() throws Exception {
        // Combination: bool=true, trueString="0", falseString=""
        Object actual = BooleanUtils.toString(true, "0", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_674() throws Exception {
        // Combination: bool=true, trueString="0", falseString=" "
        Object actual = BooleanUtils.toString(true, "0", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_675() throws Exception {
        // Combination: bool=true, trueString="0", falseString="a"
        Object actual = BooleanUtils.toString(true, "0", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_676() throws Exception {
        // Combination: bool=true, trueString="0", falseString="test123"
        Object actual = BooleanUtils.toString(true, "0", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_677() throws Exception {
        // Combination: bool=true, trueString="0", falseString="-1"
        Object actual = BooleanUtils.toString(true, "0", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_678() throws Exception {
        // Combination: bool=true, trueString="0", falseString="1.5"
        Object actual = BooleanUtils.toString(true, "0", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_679() throws Exception {
        // Combination: bool=true, trueString="0", falseString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_680() throws Exception {
        // Combination: bool=true, trueString="0", falseString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_681() throws Exception {
        // Combination: bool=true, trueString="0", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_682() throws Exception {
        // Combination: bool=true, trueString="-1", falseString=""
        Object actual = BooleanUtils.toString(true, "-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_683() throws Exception {
        // Combination: bool=true, trueString="-1", falseString=" "
        Object actual = BooleanUtils.toString(true, "-1", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_684() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="a"
        Object actual = BooleanUtils.toString(true, "-1", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_685() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="test123"
        Object actual = BooleanUtils.toString(true, "-1", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_686() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="!@#"
        Object actual = BooleanUtils.toString(true, "-1", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_687() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="0"
        Object actual = BooleanUtils.toString(true, "-1", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_688() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_689() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_690() throws Exception {
        // Combination: bool=true, trueString="-1", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_691() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString=""
        Object actual = BooleanUtils.toString(true, "1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_692() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString=" "
        Object actual = BooleanUtils.toString(true, "1.5", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_693() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="a"
        Object actual = BooleanUtils.toString(true, "1.5", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_694() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="test123"
        Object actual = BooleanUtils.toString(true, "1.5", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_695() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="!@#"
        Object actual = BooleanUtils.toString(true, "1.5", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_696() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="0"
        Object actual = BooleanUtils.toString(true, "1.5", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_697() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_698() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_699() throws Exception {
        // Combination: bool=true, trueString="1.5", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_700() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString=""
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_701() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString=" "
        Object actual = BooleanUtils.toString(true, "9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_702() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="a"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_703() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="test123"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_704() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="!@#"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_705() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="0"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_706() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="-1"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_707() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="1.5"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_708() throws Exception {
        // Combination: bool=true, trueString="9223372036854775807", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_709() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString=""
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_710() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString=" "
        Object actual = BooleanUtils.toString(true, "9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_711() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="a"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_712() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="test123"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_713() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="!@#"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_714() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="0"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_715() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="-1"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_716() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="1.5"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_717() throws Exception {
        // Combination: bool=true, trueString="9223372036854775808", falseString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = BooleanUtils.toString(true, "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_718() throws Exception {
        // Combination: bool=false, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString=""
        Object actual = BooleanUtils.toString(false, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_719() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString=" "
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_720() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="a"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_721() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="test123"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_722() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="!@#"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_723() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="0"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_724() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="-1"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_725() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="1.5"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_726() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="9223372036854775807"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_727() throws Exception {
        // Combination: bool=true, trueString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", falseString="9223372036854775808"
        Object actual = BooleanUtils.toString(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

}
