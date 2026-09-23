package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ExtendedBufferedReader.
 */
public class ExtendedBufferedReader_IPOTest {
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
    public void test_read_pairwise_001() throws Exception {
        // Combination: receiver__r=new java.io.StringReader(""), buf=new char[] {}, offset=0, length=0
        Object actual = (new ExtendedBufferedReader(new java.io.StringReader(""))).read(new char[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_002() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a"), buf=new char[] {}, offset=1, length=1
        try {
            (new ExtendedBufferedReader(new java.io.StringReader("a"))).read(new char[] {}, 1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_003() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a\nb"), buf=new char[] {}, offset=-1, length=-1
        try {
            (new ExtendedBufferedReader(new java.io.StringReader("a\nb"))).read(new char[] {}, -1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_004() throws Exception {
        // Combination: receiver__r=new java.io.StringReader(""), buf=new char[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            (new ExtendedBufferedReader(new java.io.StringReader(""))).read(new char[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_005() throws Exception {
        // Combination: receiver__r=new java.io.StringReader(""), buf=new char[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            (new ExtendedBufferedReader(new java.io.StringReader(""))).read(new char[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_006() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a\nb"), buf=new char[] {1}, offset=1, length=0
        Object actual = (new ExtendedBufferedReader(new java.io.StringReader("a\nb"))).read(new char[] {1}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_007() throws Exception {
        // Combination: receiver__r=new java.io.StringReader(""), buf=new char[] {1}, offset=0, length=1
        Object actual = (new ExtendedBufferedReader(new java.io.StringReader(""))).read(new char[] {1}, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_008() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a"), buf=new char[] {1}, offset=Integer.MAX_VALUE, length=-1
        try {
            (new ExtendedBufferedReader(new java.io.StringReader("a"))).read(new char[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_009() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a"), buf=new char[] {1}, offset=-1, length=Integer.MAX_VALUE
        try {
            (new ExtendedBufferedReader(new java.io.StringReader("a"))).read(new char[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_010() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a"), buf=new char[] {1}, offset=0, length=Integer.MIN_VALUE
        try {
            (new ExtendedBufferedReader(new java.io.StringReader("a"))).read(new char[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_011() throws Exception {
        // Combination: receiver__r=new java.io.StringReader(""), buf=new char[] {}, offset=0, length=-1
        try {
            (new ExtendedBufferedReader(new java.io.StringReader(""))).read(new char[] {}, 0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_012() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a\nb"), buf=new char[] {}, offset=0, length=Integer.MAX_VALUE
        try {
            (new ExtendedBufferedReader(new java.io.StringReader("a\nb"))).read(new char[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_013() throws Exception {
        // Combination: receiver__r=new java.io.StringReader(""), buf=new char[] {}, offset=1, length=-1
        try {
            (new ExtendedBufferedReader(new java.io.StringReader(""))).read(new char[] {}, 1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_014() throws Exception {
        // Combination: receiver__r=new java.io.StringReader(""), buf=new char[] {}, offset=1, length=Integer.MAX_VALUE
        try {
            (new ExtendedBufferedReader(new java.io.StringReader(""))).read(new char[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_015() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a\nb"), buf=new char[] {}, offset=1, length=Integer.MIN_VALUE
        try {
            (new ExtendedBufferedReader(new java.io.StringReader("a\nb"))).read(new char[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_016() throws Exception {
        // Combination: receiver__r=new java.io.StringReader(""), buf=new char[] {}, offset=-1, length=0
        Object actual = (new ExtendedBufferedReader(new java.io.StringReader(""))).read(new char[] {}, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_017() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a\nb"), buf=new char[] {}, offset=-1, length=1
        try {
            (new ExtendedBufferedReader(new java.io.StringReader("a\nb"))).read(new char[] {}, -1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_018() throws Exception {
        // Combination: receiver__r=new java.io.StringReader(""), buf=new char[] {}, offset=-1, length=Integer.MIN_VALUE
        try {
            (new ExtendedBufferedReader(new java.io.StringReader(""))).read(new char[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_019() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a"), buf=new char[] {}, offset=Integer.MAX_VALUE, length=0
        Object actual = (new ExtendedBufferedReader(new java.io.StringReader("a"))).read(new char[] {}, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_020() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a\nb"), buf=new char[] {}, offset=Integer.MAX_VALUE, length=1
        try {
            (new ExtendedBufferedReader(new java.io.StringReader("a\nb"))).read(new char[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_021() throws Exception {
        // Combination: receiver__r=new java.io.StringReader(""), buf=new char[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            (new ExtendedBufferedReader(new java.io.StringReader(""))).read(new char[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_022() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a"), buf=new char[] {1}, offset=Integer.MIN_VALUE, length=0
        Object actual = (new ExtendedBufferedReader(new java.io.StringReader("a"))).read(new char[] {1}, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_023() throws Exception {
        // Combination: receiver__r=new java.io.StringReader("a\nb"), buf=new char[] {}, offset=Integer.MIN_VALUE, length=1
        try {
            (new ExtendedBufferedReader(new java.io.StringReader("a\nb"))).read(new char[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_024() throws Exception {
        // Combination: receiver__r=new java.io.StringReader(""), buf=new char[] {}, offset=Integer.MIN_VALUE, length=-1
        try {
            (new ExtendedBufferedReader(new java.io.StringReader(""))).read(new char[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_025() throws Exception {
        // Combination: receiver__r=new java.io.StringReader(""), buf=new char[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            (new ExtendedBufferedReader(new java.io.StringReader(""))).read(new char[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
