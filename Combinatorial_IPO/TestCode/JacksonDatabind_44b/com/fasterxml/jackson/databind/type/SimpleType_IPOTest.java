package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for SimpleType.
 */
public class SimpleType_IPOTest {
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
    public void test_getErasedSignature_pairwise_001() throws Exception {
        // Combination: receiver__cls=String.class, sb=new java.lang.StringBuilder("")
        Object actual = (new SimpleType(String.class)).getErasedSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/String;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_002() throws Exception {
        // Combination: receiver__cls=String.class, sb=new java.lang.StringBuilder("test")
        Object actual = (new SimpleType(String.class)).getErasedSignature(new java.lang.StringBuilder("test"));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("testLjava/lang/String;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_003() throws Exception {
        // Combination: receiver__cls=Object.class, sb=new java.lang.StringBuilder("")
        Object actual = (new SimpleType(Object.class)).getErasedSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/Object;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_004() throws Exception {
        // Combination: receiver__cls=Object.class, sb=new java.lang.StringBuilder("test")
        Object actual = (new SimpleType(Object.class)).getErasedSignature(new java.lang.StringBuilder("test"));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("testLjava/lang/Object;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_005() throws Exception {
        // Combination: receiver__cls=Integer.class, sb=new java.lang.StringBuilder("")
        Object actual = (new SimpleType(Integer.class)).getErasedSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/Integer;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_006() throws Exception {
        // Combination: receiver__cls=Integer.class, sb=new java.lang.StringBuilder("test")
        Object actual = (new SimpleType(Integer.class)).getErasedSignature(new java.lang.StringBuilder("test"));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("testLjava/lang/Integer;", formatValue(actual));
    }

}
