package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for MultiValueMap.
 */
public class MultiValueMap_IPOTest {
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
    public void test_containsValue_pairwise_001() throws Exception {
        // Combination: key=new Object(), value=new Object()
        Object actual = (new MultiValueMap()).containsValue(new Object(), new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_002() throws Exception {
        // Combination: key=new Object(), value="sample_str"
        Object actual = (new MultiValueMap()).containsValue(new Object(), "sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_003() throws Exception {
        // Combination: key=new Object(), value=Integer.valueOf(1)
        Object actual = (new MultiValueMap()).containsValue(new Object(), Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_004() throws Exception {
        // Combination: key="sample_str", value=new Object()
        Object actual = (new MultiValueMap()).containsValue("sample_str", new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_005() throws Exception {
        // Combination: key="sample_str", value="sample_str"
        Object actual = (new MultiValueMap()).containsValue("sample_str", "sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_006() throws Exception {
        // Combination: key="sample_str", value=Integer.valueOf(1)
        Object actual = (new MultiValueMap()).containsValue("sample_str", Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_007() throws Exception {
        // Combination: key=Integer.valueOf(1), value=new Object()
        Object actual = (new MultiValueMap()).containsValue(Integer.valueOf(1), new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_008() throws Exception {
        // Combination: key=Integer.valueOf(1), value="sample_str"
        Object actual = (new MultiValueMap()).containsValue(Integer.valueOf(1), "sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_009() throws Exception {
        // Combination: key=Integer.valueOf(1), value=Integer.valueOf(1)
        Object actual = (new MultiValueMap()).containsValue(Integer.valueOf(1), Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_putAll_pairwise_010() throws Exception {
        // Combination: key=new Object(), values=java.util.Collections.emptyList()
        Object actual = (new MultiValueMap()).putAll(new Object(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_putAll_pairwise_011() throws Exception {
        // Combination: key=new Object(), values=java.util.Arrays.asList("a", "b")
        Object actual = (new MultiValueMap()).putAll(new Object(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_putAll_pairwise_012() throws Exception {
        // Combination: key="sample_str", values=java.util.Collections.emptyList()
        Object actual = (new MultiValueMap()).putAll("sample_str", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_putAll_pairwise_013() throws Exception {
        // Combination: key="sample_str", values=java.util.Arrays.asList("a", "b")
        Object actual = (new MultiValueMap()).putAll("sample_str", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_putAll_pairwise_014() throws Exception {
        // Combination: key=Integer.valueOf(1), values=java.util.Collections.emptyList()
        Object actual = (new MultiValueMap()).putAll(Integer.valueOf(1), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_putAll_pairwise_015() throws Exception {
        // Combination: key=Integer.valueOf(1), values=java.util.Arrays.asList("a", "b")
        Object actual = (new MultiValueMap()).putAll(Integer.valueOf(1), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

}
