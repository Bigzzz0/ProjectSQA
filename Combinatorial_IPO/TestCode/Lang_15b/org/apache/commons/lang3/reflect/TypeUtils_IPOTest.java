package org.apache.commons.lang3.reflect;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for TypeUtils.
 */
public class TypeUtils_IPOTest {
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
    public void test_isAssignable_pairwise_001() throws Exception {
        // Combination: type=String.class, toType=String.class
        Object actual = TypeUtils.isAssignable(String.class, String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_002() throws Exception {
        // Combination: type=Object.class, toType=String.class
        Object actual = TypeUtils.isAssignable(Object.class, String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_003() throws Exception {
        // Combination: type=String.class, toType=Object.class
        Object actual = TypeUtils.isAssignable(String.class, Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_004() throws Exception {
        // Combination: type=Object.class, toType=Object.class
        Object actual = TypeUtils.isAssignable(Object.class, Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInstance_pairwise_005() throws Exception {
        // Combination: value=new Object(), type=String.class
        Object actual = TypeUtils.isInstance(new Object(), String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInstance_pairwise_006() throws Exception {
        // Combination: value="sample_str", type=String.class
        Object actual = TypeUtils.isInstance("sample_str", String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInstance_pairwise_007() throws Exception {
        // Combination: value=Integer.valueOf(1), type=String.class
        Object actual = TypeUtils.isInstance(Integer.valueOf(1), String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInstance_pairwise_008() throws Exception {
        // Combination: value=new Object(), type=Object.class
        Object actual = TypeUtils.isInstance(new Object(), Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInstance_pairwise_009() throws Exception {
        // Combination: value="sample_str", type=Object.class
        Object actual = TypeUtils.isInstance("sample_str", Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInstance_pairwise_010() throws Exception {
        // Combination: value=Integer.valueOf(1), type=Object.class
        Object actual = TypeUtils.isInstance(Integer.valueOf(1), Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

}
