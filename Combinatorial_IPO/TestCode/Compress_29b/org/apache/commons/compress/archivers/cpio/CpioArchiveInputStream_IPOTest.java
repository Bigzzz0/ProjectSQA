package org.apache.commons.compress.archivers.cpio;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for CpioArchiveInputStream.
 */
public class CpioArchiveInputStream_IPOTest {
    @Test(timeout = 4000)
    public void test_skip_pairwise_001() throws Exception {
        // Combination: receiver__in=new java.io.ByteArrayInputStream(new byte[] {}), n=0L
        Object actual = (new CpioArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_002() throws Exception {
        // Combination: receiver__in=new java.io.ByteArrayInputStream(new byte[] {1}), n=0L
        Object actual = (new CpioArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_003() throws Exception {
        // Combination: receiver__in=new java.io.ByteArrayInputStream(new byte[] {}), n=1L
        Object actual = (new CpioArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_004() throws Exception {
        // Combination: receiver__in=new java.io.ByteArrayInputStream(new byte[] {1}), n=1L
        Object actual = (new CpioArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_005() throws Exception {
        // Combination: receiver__in=new java.io.ByteArrayInputStream(new byte[] {}), n=-1L
        try {
            (new CpioArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(-1L);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_006() throws Exception {
        // Combination: receiver__in=new java.io.ByteArrayInputStream(new byte[] {1}), n=-1L
        try {
            (new CpioArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(-1L);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_007() throws Exception {
        // Combination: receiver__in=new java.io.ByteArrayInputStream(new byte[] {}), n=Long.MAX_VALUE
        Object actual = (new CpioArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_008() throws Exception {
        // Combination: receiver__in=new java.io.ByteArrayInputStream(new byte[] {1}), n=Long.MAX_VALUE
        Object actual = (new CpioArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_009() throws Exception {
        // Combination: receiver__in=new java.io.ByteArrayInputStream(new byte[] {}), n=Long.MIN_VALUE
        try {
            (new CpioArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).skip(Long.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_skip_pairwise_010() throws Exception {
        // Combination: receiver__in=new java.io.ByteArrayInputStream(new byte[] {1}), n=Long.MIN_VALUE
        try {
            (new CpioArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).skip(Long.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_011() throws Exception {
        // Combination: signature=new byte[] {}, length=0
        Object actual = CpioArchiveInputStream.matches(new byte[] {}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_012() throws Exception {
        // Combination: signature=new byte[] {1}, length=0
        Object actual = CpioArchiveInputStream.matches(new byte[] {1}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_013() throws Exception {
        // Combination: signature=new byte[] {}, length=1
        Object actual = CpioArchiveInputStream.matches(new byte[] {}, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_014() throws Exception {
        // Combination: signature=new byte[] {1}, length=1
        Object actual = CpioArchiveInputStream.matches(new byte[] {1}, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_015() throws Exception {
        // Combination: signature=new byte[] {}, length=-1
        Object actual = CpioArchiveInputStream.matches(new byte[] {}, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_016() throws Exception {
        // Combination: signature=new byte[] {1}, length=-1
        Object actual = CpioArchiveInputStream.matches(new byte[] {1}, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_017() throws Exception {
        // Combination: signature=new byte[] {}, length=Integer.MAX_VALUE
        try {
            CpioArchiveInputStream.matches(new byte[] {}, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_018() throws Exception {
        // Combination: signature=new byte[] {1}, length=Integer.MAX_VALUE
        try {
            CpioArchiveInputStream.matches(new byte[] {1}, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_019() throws Exception {
        // Combination: signature=new byte[] {}, length=Integer.MIN_VALUE
        Object actual = CpioArchiveInputStream.matches(new byte[] {}, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_020() throws Exception {
        // Combination: signature=new byte[] {1}, length=Integer.MIN_VALUE
        Object actual = CpioArchiveInputStream.matches(new byte[] {1}, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

}
