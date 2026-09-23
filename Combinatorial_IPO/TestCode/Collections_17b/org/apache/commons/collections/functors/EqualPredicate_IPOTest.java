package org.apache.commons.collections.functors;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for EqualPredicate.
 */
public class EqualPredicate_IPOTest {
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
    public void test_evaluate_pairwise_001() throws Exception {
        // Combination: receiver__object="sample", object="sample"
        Object actual = (new EqualPredicate("sample")).evaluate("sample");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_002() throws Exception {
        // Combination: receiver__object=Integer.valueOf(1), object="sample"
        Object actual = (new EqualPredicate(Integer.valueOf(1))).evaluate("sample");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_003() throws Exception {
        // Combination: receiver__object="sample", object=Integer.valueOf(1)
        Object actual = (new EqualPredicate("sample")).evaluate(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_004() throws Exception {
        // Combination: receiver__object=Integer.valueOf(1), object=Integer.valueOf(1)
        Object actual = (new EqualPredicate(Integer.valueOf(1))).evaluate(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

}
