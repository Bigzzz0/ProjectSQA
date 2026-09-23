package org.apache.commons.compress.archivers.dump;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for DumpArchiveInputStream.
 */
public class DumpArchiveInputStream_IPOTest {
    @Test(timeout = 4000)
    public void test_read_pairwise_001() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=0, len=0
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 0, 0);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_002() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {}, off=1, len=1
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {}, 1, 1);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_003() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=-1, len=-1
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, -1);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_004() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MAX_VALUE, len=Integer.MAX_VALUE
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_005() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MIN_VALUE, len=Integer.MIN_VALUE
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_006() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {1}, off=1, len=0
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {1}, 1, 0);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_007() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {1}, off=0, len=1
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, 0, 1);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_008() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {1}, off=Integer.MAX_VALUE, len=-1
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_009() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {1}, off=-1, len=Integer.MAX_VALUE
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_010() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {1}, off=0, len=Integer.MIN_VALUE
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_011() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=0, len=-1
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 0, -1);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_012() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=0, len=Integer.MAX_VALUE
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_013() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=1, len=-1
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 1, -1);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_014() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=1, len=Integer.MAX_VALUE
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_015() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=1, len=Integer.MIN_VALUE
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, 1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_016() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {}, off=-1, len=0
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {}, -1, 0);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_017() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=-1, len=1
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, 1);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_018() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=-1, len=Integer.MIN_VALUE
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, -1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_019() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MAX_VALUE, len=0
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_020() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MAX_VALUE, len=1
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_021() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MAX_VALUE, len=Integer.MIN_VALUE
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_022() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {1}), buf=new byte[] {1}, off=Integer.MIN_VALUE, len=0
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {1}))).read(new byte[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_023() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MIN_VALUE, len=1
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_024() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MIN_VALUE, len=-1
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_read_pairwise_025() throws Exception {
        // Combination: receiver__is=new java.io.ByteArrayInputStream(new byte[] {}), buf=new byte[] {}, off=Integer.MIN_VALUE, len=Integer.MAX_VALUE
        try {
            (new DumpArchiveInputStream(new java.io.ByteArrayInputStream(new byte[] {}))).read(new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.compress.archivers.ArchiveException");
        } catch (org.apache.commons.compress.archivers.ArchiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_026() throws Exception {
        // Combination: buffer=new byte[] {}, length=0
        Object actual = DumpArchiveInputStream.matches(new byte[] {}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_027() throws Exception {
        // Combination: buffer=new byte[] {}, length=1
        Object actual = DumpArchiveInputStream.matches(new byte[] {}, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_028() throws Exception {
        // Combination: buffer=new byte[] {}, length=-1
        Object actual = DumpArchiveInputStream.matches(new byte[] {}, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_029() throws Exception {
        // Combination: buffer=new byte[] {}, length=Integer.MAX_VALUE
        try {
            DumpArchiveInputStream.matches(new byte[] {}, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_030() throws Exception {
        // Combination: buffer=new byte[] {}, length=Integer.MIN_VALUE
        Object actual = DumpArchiveInputStream.matches(new byte[] {}, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_031() throws Exception {
        // Combination: buffer=new byte[] {1}, length=0
        Object actual = DumpArchiveInputStream.matches(new byte[] {1}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_032() throws Exception {
        // Combination: buffer=new byte[] {1}, length=1
        Object actual = DumpArchiveInputStream.matches(new byte[] {1}, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_033() throws Exception {
        // Combination: buffer=new byte[] {1}, length=-1
        Object actual = DumpArchiveInputStream.matches(new byte[] {1}, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_034() throws Exception {
        // Combination: buffer=new byte[] {1}, length=Integer.MAX_VALUE
        try {
            DumpArchiveInputStream.matches(new byte[] {1}, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matches_pairwise_035() throws Exception {
        // Combination: buffer=new byte[] {1}, length=Integer.MIN_VALUE
        Object actual = DumpArchiveInputStream.matches(new byte[] {1}, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

}
