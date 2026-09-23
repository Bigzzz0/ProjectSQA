package com.google.gson.internal;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for $Gson$Types.
 */
public class $Gson$Types_IPOTest {
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
        // Combination: a=String.class, b=String.class
        Object actual = $Gson$Types.equals(String.class, String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_002() throws Exception {
        // Combination: a=String.class, b=Object.class
        Object actual = $Gson$Types.equals(String.class, Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_003() throws Exception {
        // Combination: a=Object.class, b=String.class
        Object actual = $Gson$Types.equals(Object.class, String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_004() throws Exception {
        // Combination: a=Object.class, b=Object.class
        Object actual = $Gson$Types.equals(Object.class, Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getMapKeyAndValueTypes_pairwise_005() throws Exception {
        // Combination: context=String.class, contextRawType=String.class
        try {
            $Gson$Types.getMapKeyAndValueTypes(String.class, String.class);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMapKeyAndValueTypes_pairwise_006() throws Exception {
        // Combination: context=String.class, contextRawType=Object.class
        try {
            $Gson$Types.getMapKeyAndValueTypes(String.class, Object.class);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMapKeyAndValueTypes_pairwise_007() throws Exception {
        // Combination: context=String.class, contextRawType=Integer.class
        try {
            $Gson$Types.getMapKeyAndValueTypes(String.class, Integer.class);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMapKeyAndValueTypes_pairwise_008() throws Exception {
        // Combination: context=Object.class, contextRawType=String.class
        try {
            $Gson$Types.getMapKeyAndValueTypes(Object.class, String.class);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMapKeyAndValueTypes_pairwise_009() throws Exception {
        // Combination: context=Object.class, contextRawType=Object.class
        try {
            $Gson$Types.getMapKeyAndValueTypes(Object.class, Object.class);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMapKeyAndValueTypes_pairwise_010() throws Exception {
        // Combination: context=Object.class, contextRawType=Integer.class
        try {
            $Gson$Types.getMapKeyAndValueTypes(Object.class, Integer.class);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
