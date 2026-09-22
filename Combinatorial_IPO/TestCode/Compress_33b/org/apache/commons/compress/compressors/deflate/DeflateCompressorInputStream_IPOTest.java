package org.apache.commons.compress.compressors.deflate;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for DeflateCompressorInputStream.
 */
public class DeflateCompressorInputStream_IPOTest {
    @Test(timeout = 4000)
    public void test_matches_pairwise_001() throws Exception {
        // Combination: signature=new byte[] {}, length=0
        Object actual = DeflateCompressorInputStream.matches(new byte[] {}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_002() throws Exception {
        // Combination: signature=new byte[] {1}, length=0
        Object actual = DeflateCompressorInputStream.matches(new byte[] {1}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_003() throws Exception {
        // Combination: signature=new byte[] {}, length=1
        Object actual = DeflateCompressorInputStream.matches(new byte[] {}, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_004() throws Exception {
        // Combination: signature=new byte[] {1}, length=1
        Object actual = DeflateCompressorInputStream.matches(new byte[] {1}, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_005() throws Exception {
        // Combination: signature=new byte[] {}, length=-1
        Object actual = DeflateCompressorInputStream.matches(new byte[] {}, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_006() throws Exception {
        // Combination: signature=new byte[] {1}, length=-1
        Object actual = DeflateCompressorInputStream.matches(new byte[] {1}, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_007() throws Exception {
        // Combination: signature=new byte[] {}, length=Integer.MAX_VALUE
        try {
            DeflateCompressorInputStream.matches(new byte[] {}, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_008() throws Exception {
        // Combination: signature=new byte[] {1}, length=Integer.MAX_VALUE
        Object actual = DeflateCompressorInputStream.matches(new byte[] {1}, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_009() throws Exception {
        // Combination: signature=new byte[] {}, length=Integer.MIN_VALUE
        Object actual = DeflateCompressorInputStream.matches(new byte[] {}, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_010() throws Exception {
        // Combination: signature=new byte[] {1}, length=Integer.MIN_VALUE
        Object actual = DeflateCompressorInputStream.matches(new byte[] {1}, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

}
