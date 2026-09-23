package org.apache.commons.collections4.keyvalue;

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
    public void test_getKeys_pairwise_001() throws Exception {
        // Combination: receiver__key1="sample", receiver__key2="sample"
        Object actual = (new MultiKey("sample", "sample")).getKeys();
        assertNotNull(actual);
        assertEquals("[Ljava.lang.Object;", actual.getClass().getName());
        assertEquals("[sample, sample]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKeys_pairwise_002() throws Exception {
        // Combination: receiver__key1="sample", receiver__key2=Integer.valueOf(1)
        Object actual = (new MultiKey("sample", Integer.valueOf(1))).getKeys();
        assertNotNull(actual);
        assertEquals("[Ljava.lang.Object;", actual.getClass().getName());
        assertEquals("[sample, 1]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKeys_pairwise_003() throws Exception {
        // Combination: receiver__key1=Integer.valueOf(1), receiver__key2="sample"
        Object actual = (new MultiKey(Integer.valueOf(1), "sample")).getKeys();
        assertNotNull(actual);
        assertEquals("[Ljava.lang.Object;", actual.getClass().getName());
        assertEquals("[1, sample]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getKeys_pairwise_004() throws Exception {
        // Combination: receiver__key1=Integer.valueOf(1), receiver__key2=Integer.valueOf(1)
        Object actual = (new MultiKey(Integer.valueOf(1), Integer.valueOf(1))).getKeys();
        assertNotNull(actual);
        assertEquals("[Ljava.lang.Object;", actual.getClass().getName());
        assertEquals("[1, 1]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_size_pairwise_005() throws Exception {
        // Combination: receiver__key1="sample", receiver__key2="sample"
        Object actual = (new MultiKey("sample", "sample")).size();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_size_pairwise_006() throws Exception {
        // Combination: receiver__key1="sample", receiver__key2=Integer.valueOf(1)
        Object actual = (new MultiKey("sample", Integer.valueOf(1))).size();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_size_pairwise_007() throws Exception {
        // Combination: receiver__key1=Integer.valueOf(1), receiver__key2="sample"
        Object actual = (new MultiKey(Integer.valueOf(1), "sample")).size();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_size_pairwise_008() throws Exception {
        // Combination: receiver__key1=Integer.valueOf(1), receiver__key2=Integer.valueOf(1)
        Object actual = (new MultiKey(Integer.valueOf(1), Integer.valueOf(1))).size();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_009() throws Exception {
        // Combination: receiver__key1="sample", receiver__key2="sample", other=new Object()
        Object actual = (new MultiKey("sample", "sample")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_010() throws Exception {
        // Combination: receiver__key1=Integer.valueOf(1), receiver__key2=Integer.valueOf(1), other=new Object()
        Object actual = (new MultiKey(Integer.valueOf(1), Integer.valueOf(1))).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_011() throws Exception {
        // Combination: receiver__key1="sample", receiver__key2=Integer.valueOf(1), other="sample_str"
        Object actual = (new MultiKey("sample", Integer.valueOf(1))).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_012() throws Exception {
        // Combination: receiver__key1=Integer.valueOf(1), receiver__key2="sample", other="sample_str"
        Object actual = (new MultiKey(Integer.valueOf(1), "sample")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_013() throws Exception {
        // Combination: receiver__key1="sample", receiver__key2="sample", other=Integer.valueOf(1)
        Object actual = (new MultiKey("sample", "sample")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_014() throws Exception {
        // Combination: receiver__key1=Integer.valueOf(1), receiver__key2=Integer.valueOf(1), other=Integer.valueOf(1)
        Object actual = (new MultiKey(Integer.valueOf(1), Integer.valueOf(1))).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_015() throws Exception {
        // Combination: receiver__key1="sample", receiver__key2="sample"
        Object actual = (new MultiKey("sample", "sample")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_016() throws Exception {
        // Combination: receiver__key1="sample", receiver__key2=Integer.valueOf(1)
        Object actual = (new MultiKey("sample", Integer.valueOf(1))).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-909675093", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_017() throws Exception {
        // Combination: receiver__key1=Integer.valueOf(1), receiver__key2="sample"
        Object actual = (new MultiKey(Integer.valueOf(1), "sample")).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-909675093", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_018() throws Exception {
        // Combination: receiver__key1=Integer.valueOf(1), receiver__key2=Integer.valueOf(1)
        Object actual = (new MultiKey(Integer.valueOf(1), Integer.valueOf(1))).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_019() throws Exception {
        // Combination: receiver__key1="sample", receiver__key2="sample"
        Object actual = (new MultiKey("sample", "sample")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("MultiKey[sample, sample]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_020() throws Exception {
        // Combination: receiver__key1="sample", receiver__key2=Integer.valueOf(1)
        Object actual = (new MultiKey("sample", Integer.valueOf(1))).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("MultiKey[sample, 1]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_021() throws Exception {
        // Combination: receiver__key1=Integer.valueOf(1), receiver__key2="sample"
        Object actual = (new MultiKey(Integer.valueOf(1), "sample")).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("MultiKey[1, sample]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_022() throws Exception {
        // Combination: receiver__key1=Integer.valueOf(1), receiver__key2=Integer.valueOf(1)
        Object actual = (new MultiKey(Integer.valueOf(1), Integer.valueOf(1))).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("MultiKey[1, 1]", formatValue(actual));
    }

}
