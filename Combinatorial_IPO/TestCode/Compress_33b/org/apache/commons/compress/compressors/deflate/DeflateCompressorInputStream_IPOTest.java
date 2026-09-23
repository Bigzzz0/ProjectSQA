package org.apache.commons.compress.compressors.deflate;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for DeflateCompressorInputStream.
 */
public class DeflateCompressorInputStream_IPOTest {
    @Test(timeout = 4000)
    public void test_read_pairwise_001() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=0, len=0
        Object actual = (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_002() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {}, off=1, len=1
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {}, 1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_003() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=-1, len=-1
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_004() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MAX_VALUE, len=Integer.MAX_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_005() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MIN_VALUE, len=Integer.MIN_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_006() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {1}, off=1, len=0
        Object actual = (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {1}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_007() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {1}, off=0, len=1
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, 0, 1);
            fail("Expected java.io.EOFException");
        } catch (java.io.EOFException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_008() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {1}, off=Integer.MAX_VALUE, len=-1
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_009() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {1}, off=-1, len=Integer.MAX_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_010() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {1}, off=0, len=Integer.MIN_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_011() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=0, len=-1
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_012() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=0, len=Integer.MAX_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_013() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=1, len=-1
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_014() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=1, len=Integer.MAX_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_015() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=1, len=Integer.MIN_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_016() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {}, off=-1, len=0
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {}, -1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_017() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=-1, len=1
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_018() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=-1, len=Integer.MIN_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_019() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MAX_VALUE, len=0
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_020() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MAX_VALUE, len=1
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_021() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MAX_VALUE, len=Integer.MIN_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_022() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {1}, off=Integer.MIN_VALUE, len=0
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_023() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MIN_VALUE, len=1
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_024() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MIN_VALUE, len=-1
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_025() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MIN_VALUE, len=Integer.MAX_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_026() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), n=0L
        Object actual = (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_027() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), n=0L
        Object actual = (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_028() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), n=1L
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(1L);
            fail("Expected java.io.EOFException");
        } catch (java.io.EOFException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_029() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), n=1L
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(1L);
            fail("Expected java.io.EOFException");
        } catch (java.io.EOFException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_030() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), n=-1L
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(-1L);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_031() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), n=-1L
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(-1L);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_032() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), n=Long.MAX_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(Long.MAX_VALUE);
            fail("Expected java.io.EOFException");
        } catch (java.io.EOFException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_033() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), n=Long.MAX_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(Long.MAX_VALUE);
            fail("Expected java.io.EOFException");
        } catch (java.io.EOFException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_034() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), n=Long.MIN_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(Long.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_035() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), n=Long.MIN_VALUE
        try {
            (new DeflateCompressorInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(Long.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_036() throws Exception {
        // Combination: signature=new byte[] {}, length=0
        Object actual = DeflateCompressorInputStream.matches(new byte[] {}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_037() throws Exception {
        // Combination: signature=new byte[] {1}, length=0
        Object actual = DeflateCompressorInputStream.matches(new byte[] {1}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_038() throws Exception {
        // Combination: signature=new byte[] {}, length=1
        Object actual = DeflateCompressorInputStream.matches(new byte[] {}, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_039() throws Exception {
        // Combination: signature=new byte[] {1}, length=1
        Object actual = DeflateCompressorInputStream.matches(new byte[] {1}, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_040() throws Exception {
        // Combination: signature=new byte[] {}, length=-1
        Object actual = DeflateCompressorInputStream.matches(new byte[] {}, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_041() throws Exception {
        // Combination: signature=new byte[] {1}, length=-1
        Object actual = DeflateCompressorInputStream.matches(new byte[] {1}, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_042() throws Exception {
        // Combination: signature=new byte[] {}, length=Integer.MAX_VALUE
        try {
            DeflateCompressorInputStream.matches(new byte[] {}, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_043() throws Exception {
        // Combination: signature=new byte[] {1}, length=Integer.MAX_VALUE
        Object actual = DeflateCompressorInputStream.matches(new byte[] {1}, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_044() throws Exception {
        // Combination: signature=new byte[] {}, length=Integer.MIN_VALUE
        Object actual = DeflateCompressorInputStream.matches(new byte[] {}, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_045() throws Exception {
        // Combination: signature=new byte[] {1}, length=Integer.MIN_VALUE
        Object actual = DeflateCompressorInputStream.matches(new byte[] {1}, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

}
