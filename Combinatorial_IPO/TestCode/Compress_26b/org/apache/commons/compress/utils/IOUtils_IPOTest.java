package org.apache.commons.compress.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for IOUtils.
 */
public class IOUtils_IPOTest {
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
    public void test_copy_pairwise_001() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {}), output=new java.io.ByteArrayOutputStream()
        Object actual = IOUtils.copy(new java.io.ByteArrayInputStream(new byte[] {}), new java.io.ByteArrayOutputStream());
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copy_pairwise_002() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {}), output=new java.io.ByteArrayOutputStream(32)
        Object actual = IOUtils.copy(new java.io.ByteArrayInputStream(new byte[] {}), new java.io.ByteArrayOutputStream(32));
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copy_pairwise_003() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {1}), output=new java.io.ByteArrayOutputStream()
        Object actual = IOUtils.copy(new java.io.ByteArrayInputStream(new byte[] {1}), new java.io.ByteArrayOutputStream());
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copy_pairwise_004() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {1}), output=new java.io.ByteArrayOutputStream(32)
        Object actual = IOUtils.copy(new java.io.ByteArrayInputStream(new byte[] {1}), new java.io.ByteArrayOutputStream(32));
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_005() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {}), numToSkip=0L
        Object actual = IOUtils.skip(new java.io.ByteArrayInputStream(new byte[] {}), 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_006() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {}), numToSkip=1L
        Object actual = IOUtils.skip(new java.io.ByteArrayInputStream(new byte[] {}), 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_007() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {}), numToSkip=-1L
        Object actual = IOUtils.skip(new java.io.ByteArrayInputStream(new byte[] {}), -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_008() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {}), numToSkip=Long.MAX_VALUE
        Object actual = IOUtils.skip(new java.io.ByteArrayInputStream(new byte[] {}), Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_009() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {}), numToSkip=Long.MIN_VALUE
        Object actual = IOUtils.skip(new java.io.ByteArrayInputStream(new byte[] {}), Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_010() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {1}), numToSkip=0L
        Object actual = IOUtils.skip(new java.io.ByteArrayInputStream(new byte[] {1}), 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_011() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {1}), numToSkip=1L
        Object actual = IOUtils.skip(new java.io.ByteArrayInputStream(new byte[] {1}), 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_012() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {1}), numToSkip=-1L
        Object actual = IOUtils.skip(new java.io.ByteArrayInputStream(new byte[] {1}), -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_013() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {1}), numToSkip=Long.MAX_VALUE
        Object actual = IOUtils.skip(new java.io.ByteArrayInputStream(new byte[] {1}), Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_014() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {1}), numToSkip=Long.MIN_VALUE
        Object actual = IOUtils.skip(new java.io.ByteArrayInputStream(new byte[] {1}), Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_readFully_pairwise_015() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {}), b=new byte[] {}
        Object actual = IOUtils.readFully(new java.io.ByteArrayInputStream(new byte[] {}), new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_readFully_pairwise_016() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {1}), b=new byte[] {}
        Object actual = IOUtils.readFully(new java.io.ByteArrayInputStream(new byte[] {1}), new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_readFully_pairwise_017() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {}), b=new byte[] {1}
        Object actual = IOUtils.readFully(new java.io.ByteArrayInputStream(new byte[] {}), new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_readFully_pairwise_018() throws Exception {
        // Combination: input=new java.io.ByteArrayInputStream(new byte[] {1}), b=new byte[] {1}
        Object actual = IOUtils.readFully(new java.io.ByteArrayInputStream(new byte[] {1}), new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

}
