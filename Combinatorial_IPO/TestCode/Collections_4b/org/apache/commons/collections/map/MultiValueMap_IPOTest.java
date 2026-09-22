package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for MultiValueMap.
 */
public class MultiValueMap_IPOTest {
    @Test(timeout = 4000)
    public void test_containsValue_pairwise_001() throws Exception {
        // Combination: key=new Object(), value=new Object()
        Object actual = (new MultiValueMap()).containsValue(new Object(), new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_002() throws Exception {
        // Combination: key=new Object(), value="sample_str"
        Object actual = (new MultiValueMap()).containsValue(new Object(), "sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_003() throws Exception {
        // Combination: key=new Object(), value=Integer.valueOf(1)
        Object actual = (new MultiValueMap()).containsValue(new Object(), Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_004() throws Exception {
        // Combination: key="sample_str", value=new Object()
        Object actual = (new MultiValueMap()).containsValue("sample_str", new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_005() throws Exception {
        // Combination: key="sample_str", value="sample_str"
        Object actual = (new MultiValueMap()).containsValue("sample_str", "sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_006() throws Exception {
        // Combination: key="sample_str", value=Integer.valueOf(1)
        Object actual = (new MultiValueMap()).containsValue("sample_str", Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_007() throws Exception {
        // Combination: key=Integer.valueOf(1), value=new Object()
        Object actual = (new MultiValueMap()).containsValue(Integer.valueOf(1), new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_008() throws Exception {
        // Combination: key=Integer.valueOf(1), value="sample_str"
        Object actual = (new MultiValueMap()).containsValue(Integer.valueOf(1), "sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsValue_pairwise_009() throws Exception {
        // Combination: key=Integer.valueOf(1), value=Integer.valueOf(1)
        Object actual = (new MultiValueMap()).containsValue(Integer.valueOf(1), Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_putAll_pairwise_010() throws Exception {
        // Combination: key=new Object(), values=java.util.Collections.emptyList()
        Object actual = (new MultiValueMap()).putAll(new Object(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_putAll_pairwise_011() throws Exception {
        // Combination: key=new Object(), values=java.util.Arrays.asList("a", "b")
        Object actual = (new MultiValueMap()).putAll(new Object(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_putAll_pairwise_012() throws Exception {
        // Combination: key="sample_str", values=java.util.Collections.emptyList()
        Object actual = (new MultiValueMap()).putAll("sample_str", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_putAll_pairwise_013() throws Exception {
        // Combination: key="sample_str", values=java.util.Arrays.asList("a", "b")
        Object actual = (new MultiValueMap()).putAll("sample_str", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_putAll_pairwise_014() throws Exception {
        // Combination: key=Integer.valueOf(1), values=java.util.Collections.emptyList()
        Object actual = (new MultiValueMap()).putAll(Integer.valueOf(1), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_putAll_pairwise_015() throws Exception {
        // Combination: key=Integer.valueOf(1), values=java.util.Arrays.asList("a", "b")
        Object actual = (new MultiValueMap()).putAll(Integer.valueOf(1), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

}
