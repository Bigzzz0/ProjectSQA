package com.fasterxml.jackson.core.json;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for UTF8StreamJsonParser.
 */
public class UTF8StreamJsonParser_IPOTest {
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
    public void test_growArrayBy_pairwise_001() throws Exception {
        // Combination: arr=new int[] {}, more=0
        Object actual = UTF8StreamJsonParser.growArrayBy(new int[] {}, 0);
        assertNotNull(actual);
        assertEquals("[I", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_growArrayBy_pairwise_002() throws Exception {
        // Combination: arr=new int[] {}, more=1
        Object actual = UTF8StreamJsonParser.growArrayBy(new int[] {}, 1);
        assertNotNull(actual);
        assertEquals("[I", actual.getClass().getName());
        assertEquals("[0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_growArrayBy_pairwise_003() throws Exception {
        // Combination: arr=new int[] {}, more=-1
        try {
            UTF8StreamJsonParser.growArrayBy(new int[] {}, -1);
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_growArrayBy_pairwise_004() throws Exception {
        // Combination: arr=new int[] {}, more=Integer.MAX_VALUE
        try {
            UTF8StreamJsonParser.growArrayBy(new int[] {}, Integer.MAX_VALUE);
            fail("Expected java.lang.OutOfMemoryError");
        } catch (java.lang.OutOfMemoryError expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_growArrayBy_pairwise_005() throws Exception {
        // Combination: arr=new int[] {}, more=Integer.MIN_VALUE
        try {
            UTF8StreamJsonParser.growArrayBy(new int[] {}, Integer.MIN_VALUE);
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_growArrayBy_pairwise_006() throws Exception {
        // Combination: arr=new int[] {1}, more=0
        Object actual = UTF8StreamJsonParser.growArrayBy(new int[] {1}, 0);
        assertNotNull(actual);
        assertEquals("[I", actual.getClass().getName());
        assertEquals("[1]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_growArrayBy_pairwise_007() throws Exception {
        // Combination: arr=new int[] {1}, more=1
        Object actual = UTF8StreamJsonParser.growArrayBy(new int[] {1}, 1);
        assertNotNull(actual);
        assertEquals("[I", actual.getClass().getName());
        assertEquals("[1, 0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_growArrayBy_pairwise_008() throws Exception {
        // Combination: arr=new int[] {1}, more=-1
        Object actual = UTF8StreamJsonParser.growArrayBy(new int[] {1}, -1);
        assertNotNull(actual);
        assertEquals("[I", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_growArrayBy_pairwise_009() throws Exception {
        // Combination: arr=new int[] {1}, more=Integer.MAX_VALUE
        try {
            UTF8StreamJsonParser.growArrayBy(new int[] {1}, Integer.MAX_VALUE);
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_growArrayBy_pairwise_010() throws Exception {
        // Combination: arr=new int[] {1}, more=Integer.MIN_VALUE
        try {
            UTF8StreamJsonParser.growArrayBy(new int[] {1}, Integer.MIN_VALUE);
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
