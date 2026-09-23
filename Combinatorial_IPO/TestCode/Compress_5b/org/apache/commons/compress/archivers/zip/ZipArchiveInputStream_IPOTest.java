package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ZipArchiveInputStream.
 */
public class ZipArchiveInputStream_IPOTest {
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
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=0, length=0
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_002() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buffer=new byte[] {}, start=1, length=1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {}, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_003() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=-1, length=-1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_004() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_005() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_006() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buffer=new byte[] {1}, start=-1, length=0
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_007() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {1}, start=0, length=1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {1}, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_008() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buffer=new byte[] {1}, start=Integer.MAX_VALUE, length=-1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_009() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buffer=new byte[] {1}, start=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_010() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buffer=new byte[] {1}, start=0, length=Integer.MIN_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_011() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=0, length=-1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_012() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=0, length=Integer.MAX_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_013() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {1}, start=1, length=0
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {1}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_014() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=1, length=-1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_015() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=1, length=Integer.MAX_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_016() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=1, length=Integer.MIN_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_017() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=-1, length=1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_018() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=-1, length=Integer.MAX_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_019() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=-1, length=Integer.MIN_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_020() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MAX_VALUE, length=0
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_021() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MAX_VALUE, length=1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_022() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_023() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MIN_VALUE, length=0
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_024() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MIN_VALUE, length=1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_025() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MIN_VALUE, length=-1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_026() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), value=0L
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_027() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), value=1L
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_028() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), value=-1L
        try {
            (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(-1L);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_029() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), value=Long.MAX_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_030() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), value=Long.MIN_VALUE
        try {
            (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(Long.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_031() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), value=0L
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_032() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), value=1L
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_033() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), value=-1L
        try {
            (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(-1L);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_034() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), value=Long.MAX_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_035() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), value=Long.MIN_VALUE
        try {
            (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(Long.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_036() throws Exception {
        // Combination: signature=new byte[] {}, length=0
        Object actual = ZipArchiveInputStream.matches(new byte[] {}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_037() throws Exception {
        // Combination: signature=new byte[] {1}, length=0
        Object actual = ZipArchiveInputStream.matches(new byte[] {1}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_038() throws Exception {
        // Combination: signature=new byte[] {}, length=1
        Object actual = ZipArchiveInputStream.matches(new byte[] {}, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_039() throws Exception {
        // Combination: signature=new byte[] {1}, length=1
        Object actual = ZipArchiveInputStream.matches(new byte[] {1}, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_040() throws Exception {
        // Combination: signature=new byte[] {}, length=-1
        Object actual = ZipArchiveInputStream.matches(new byte[] {}, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_041() throws Exception {
        // Combination: signature=new byte[] {1}, length=-1
        Object actual = ZipArchiveInputStream.matches(new byte[] {1}, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_042() throws Exception {
        // Combination: signature=new byte[] {}, length=Integer.MAX_VALUE
        try {
            ZipArchiveInputStream.matches(new byte[] {}, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_043() throws Exception {
        // Combination: signature=new byte[] {1}, length=Integer.MAX_VALUE
        Object actual = ZipArchiveInputStream.matches(new byte[] {1}, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_044() throws Exception {
        // Combination: signature=new byte[] {}, length=Integer.MIN_VALUE
        Object actual = ZipArchiveInputStream.matches(new byte[] {}, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_045() throws Exception {
        // Combination: signature=new byte[] {1}, length=Integer.MIN_VALUE
        Object actual = ZipArchiveInputStream.matches(new byte[] {1}, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

}
