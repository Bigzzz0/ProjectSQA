package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ZipArchiveInputStream.
 */
public class ZipArchiveInputStream_IPOTest {
    @Test(timeout = 4000)
    public void test_read_pairwise_001() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=0, length=0
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_002() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buffer=new byte[] {}, start=1, length=1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {}, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_003() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=-1, length=-1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_004() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_005() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_006() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buffer=new byte[] {1}, start=-1, length=0
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_007() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {1}, start=0, length=1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {1}, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_008() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buffer=new byte[] {1}, start=Integer.MAX_VALUE, length=-1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_009() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buffer=new byte[] {1}, start=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_010() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {1}), buffer=new byte[] {1}, start=0, length=Integer.MIN_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_011() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=0, length=-1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_012() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=0, length=Integer.MAX_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_013() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {1}, start=1, length=0
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {1}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_014() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=1, length=-1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_015() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=1, length=Integer.MAX_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_016() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=1, length=Integer.MIN_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_017() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=-1, length=1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_018() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=-1, length=Integer.MAX_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_019() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=-1, length=Integer.MIN_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_020() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MAX_VALUE, length=0
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_021() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MAX_VALUE, length=1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_022() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_023() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MIN_VALUE, length=0
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_024() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MIN_VALUE, length=1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_025() throws Exception {
        // Combination: receiver__inputStream=new java.io.ByteArrayInputStream(new byte[] {}), buffer=new byte[] {}, start=Integer.MIN_VALUE, length=-1
        Object actual = (new ZipArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

}
