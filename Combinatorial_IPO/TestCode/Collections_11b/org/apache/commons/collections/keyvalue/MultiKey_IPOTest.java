package org.apache.commons.collections.keyvalue;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for MultiKey.
 */
public class MultiKey_IPOTest {
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
        // Combination: receiver__keys=new Object[] {}, other=new Object()
        Object actual = (new MultiKey(new Object[] {})).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_002() throws Exception {
        // Combination: receiver__keys=new Object[] {"a", Integer.valueOf(1)}, other=new Object()
        Object actual = (new MultiKey(new Object[] {"a", Integer.valueOf(1)})).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_003() throws Exception {
        // Combination: receiver__keys=new Object[] {}, other="sample_str"
        Object actual = (new MultiKey(new Object[] {})).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_004() throws Exception {
        // Combination: receiver__keys=new Object[] {"a", Integer.valueOf(1)}, other="sample_str"
        Object actual = (new MultiKey(new Object[] {"a", Integer.valueOf(1)})).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_005() throws Exception {
        // Combination: receiver__keys=new Object[] {}, other=Integer.valueOf(1)
        Object actual = (new MultiKey(new Object[] {})).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_006() throws Exception {
        // Combination: receiver__keys=new Object[] {"a", Integer.valueOf(1)}, other=Integer.valueOf(1)
        Object actual = (new MultiKey(new Object[] {"a", Integer.valueOf(1)})).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

}
