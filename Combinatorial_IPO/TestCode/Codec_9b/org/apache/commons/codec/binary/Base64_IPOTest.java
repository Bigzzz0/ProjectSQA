package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Base64.
 */
public class Base64_IPOTest {
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
    public void test_encodeBase64_pairwise_001() throws Exception {
        // Combination: binaryData=new byte[] {}, isChunked=true
        Object actual = Base64.encodeBase64(new byte[] {}, true);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_002() throws Exception {
        // Combination: binaryData=new byte[] {}, isChunked=false
        Object actual = Base64.encodeBase64(new byte[] {}, false);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_003() throws Exception {
        // Combination: binaryData=new byte[] {1}, isChunked=true
        Object actual = Base64.encodeBase64(new byte[] {1}, true);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[65, 81, 61, 61, 13, 10]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_004() throws Exception {
        // Combination: binaryData=new byte[] {1}, isChunked=false
        Object actual = Base64.encodeBase64(new byte[] {1}, false);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[65, 81, 61, 61]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_005() throws Exception {
        // Combination: binaryData=new byte[] {}, isChunked=true, urlSafe=true
        Object actual = Base64.encodeBase64(new byte[] {}, true, true);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_006() throws Exception {
        // Combination: binaryData=new byte[] {}, isChunked=false, urlSafe=false
        Object actual = Base64.encodeBase64(new byte[] {}, false, false);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_007() throws Exception {
        // Combination: binaryData=new byte[] {1}, isChunked=true, urlSafe=false
        Object actual = Base64.encodeBase64(new byte[] {1}, true, false);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[65, 81, 61, 61, 13, 10]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_008() throws Exception {
        // Combination: binaryData=new byte[] {1}, isChunked=false, urlSafe=true
        Object actual = Base64.encodeBase64(new byte[] {1}, false, true);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[65, 81]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_009() throws Exception {
        // Combination: binaryData=new byte[] {}, isChunked=true, urlSafe=true, maxResultSize=0
        Object actual = Base64.encodeBase64(new byte[] {}, true, true, 0);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_010() throws Exception {
        // Combination: binaryData=new byte[] {}, isChunked=false, urlSafe=false, maxResultSize=1
        Object actual = Base64.encodeBase64(new byte[] {}, false, false, 1);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_011() throws Exception {
        // Combination: binaryData=new byte[] {1}, isChunked=true, urlSafe=true, maxResultSize=1
        try {
            Base64.encodeBase64(new byte[] {1}, true, true, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_012() throws Exception {
        // Combination: binaryData=new byte[] {1}, isChunked=false, urlSafe=false, maxResultSize=0
        try {
            Base64.encodeBase64(new byte[] {1}, false, false, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_013() throws Exception {
        // Combination: binaryData=new byte[] {}, isChunked=true, urlSafe=false, maxResultSize=-1
        Object actual = Base64.encodeBase64(new byte[] {}, true, false, -1);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_014() throws Exception {
        // Combination: binaryData=new byte[] {1}, isChunked=false, urlSafe=true, maxResultSize=-1
        try {
            Base64.encodeBase64(new byte[] {1}, false, true, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_015() throws Exception {
        // Combination: binaryData=new byte[] {}, isChunked=true, urlSafe=true, maxResultSize=Integer.MAX_VALUE
        Object actual = Base64.encodeBase64(new byte[] {}, true, true, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_016() throws Exception {
        // Combination: binaryData=new byte[] {1}, isChunked=false, urlSafe=false, maxResultSize=Integer.MAX_VALUE
        Object actual = Base64.encodeBase64(new byte[] {1}, false, false, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[65, 81, 61, 61]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_017() throws Exception {
        // Combination: binaryData=new byte[] {}, isChunked=true, urlSafe=true, maxResultSize=Integer.MIN_VALUE
        Object actual = Base64.encodeBase64(new byte[] {}, true, true, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("[B", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_encodeBase64_pairwise_018() throws Exception {
        // Combination: binaryData=new byte[] {1}, isChunked=false, urlSafe=false, maxResultSize=Integer.MIN_VALUE
        try {
            Base64.encodeBase64(new byte[] {1}, false, false, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
