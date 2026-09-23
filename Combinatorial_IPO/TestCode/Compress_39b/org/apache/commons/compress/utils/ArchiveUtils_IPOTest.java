package org.apache.commons.compress.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ArchiveUtils.
 */
public class ArchiveUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_001() throws Exception {
        // Combination: expected="", buffer=new byte[] {}, offset=0, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("", new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_002() throws Exception {
        // Combination: expected=" ", buffer=new byte[] {}, offset=1, length=1
        try {
            ArchiveUtils.matchAsciiBuffer(" ", new byte[] {}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_003() throws Exception {
        // Combination: expected="a", buffer=new byte[] {}, offset=-1, length=-1
        Object actual = ArchiveUtils.matchAsciiBuffer("a", new byte[] {}, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_004() throws Exception {
        // Combination: expected="test123", buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.matchAsciiBuffer("test123", new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_005() throws Exception {
        // Combination: expected="!@#", buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        Object actual = ArchiveUtils.matchAsciiBuffer("!@#", new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_006() throws Exception {
        // Combination: expected="0", buffer=new byte[] {}, offset=1, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("0", new byte[] {}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_007() throws Exception {
        // Combination: expected="-1", buffer=new byte[] {}, offset=-1, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("-1", new byte[] {}, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_008() throws Exception {
        // Combination: expected="1.5", buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("1.5", new byte[] {}, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_009() throws Exception {
        // Combination: expected="9223372036854775807", buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("9223372036854775807", new byte[] {}, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_010() throws Exception {
        // Combination: expected="9223372036854775808", buffer=new byte[] {}, offset=0, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("9223372036854775808", new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_011() throws Exception {
        // Combination: expected="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", buffer=new byte[] {}, offset=0, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_012() throws Exception {
        // Combination: expected="", buffer=new byte[] {1}, offset=-1, length=1
        Object actual = ArchiveUtils.matchAsciiBuffer("", new byte[] {1}, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_013() throws Exception {
        // Combination: expected=" ", buffer=new byte[] {1}, offset=0, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer(" ", new byte[] {1}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_014() throws Exception {
        // Combination: expected="a", buffer=new byte[] {1}, offset=1, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.matchAsciiBuffer("a", new byte[] {1}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_015() throws Exception {
        // Combination: expected="test123", buffer=new byte[] {1}, offset=Integer.MIN_VALUE, length=-1
        Object actual = ArchiveUtils.matchAsciiBuffer("test123", new byte[] {1}, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_016() throws Exception {
        // Combination: expected="!@#", buffer=new byte[] {1}, offset=Integer.MAX_VALUE, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("!@#", new byte[] {1}, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_017() throws Exception {
        // Combination: expected="0", buffer=new byte[] {1}, offset=0, length=Integer.MIN_VALUE
        Object actual = ArchiveUtils.matchAsciiBuffer("0", new byte[] {1}, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_018() throws Exception {
        // Combination: expected="-1", buffer=new byte[] {1}, offset=0, length=1
        Object actual = ArchiveUtils.matchAsciiBuffer("-1", new byte[] {1}, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_019() throws Exception {
        // Combination: expected="1.5", buffer=new byte[] {1}, offset=Integer.MIN_VALUE, length=1
        try {
            ArchiveUtils.matchAsciiBuffer("1.5", new byte[] {1}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_020() throws Exception {
        // Combination: expected="9223372036854775807", buffer=new byte[] {1}, offset=Integer.MAX_VALUE, length=1
        try {
            ArchiveUtils.matchAsciiBuffer("9223372036854775807", new byte[] {1}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_021() throws Exception {
        // Combination: expected="9223372036854775808", buffer=new byte[] {1}, offset=1, length=1
        try {
            ArchiveUtils.matchAsciiBuffer("9223372036854775808", new byte[] {1}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_022() throws Exception {
        // Combination: expected="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", buffer=new byte[] {1}, offset=1, length=1
        try {
            ArchiveUtils.matchAsciiBuffer("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new byte[] {1}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_023() throws Exception {
        // Combination: expected="a", buffer=new byte[] {}, offset=0, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("a", new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_024() throws Exception {
        // Combination: expected="test123", buffer=new byte[] {}, offset=0, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("test123", new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_025() throws Exception {
        // Combination: expected="a", buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=1
        try {
            ArchiveUtils.matchAsciiBuffer("a", new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_026() throws Exception {
        // Combination: expected="test123", buffer=new byte[] {}, offset=1, length=1
        try {
            ArchiveUtils.matchAsciiBuffer("test123", new byte[] {}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_027() throws Exception {
        // Combination: expected="!@#", buffer=new byte[] {}, offset=0, length=1
        try {
            ArchiveUtils.matchAsciiBuffer("!@#", new byte[] {}, 0, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_028() throws Exception {
        // Combination: expected="0", buffer=new byte[] {}, offset=-1, length=1
        try {
            ArchiveUtils.matchAsciiBuffer("0", new byte[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_029() throws Exception {
        // Combination: expected="", buffer=new byte[] {}, offset=1, length=-1
        Object actual = ArchiveUtils.matchAsciiBuffer("", new byte[] {}, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_030() throws Exception {
        // Combination: expected=" ", buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=-1
        Object actual = ArchiveUtils.matchAsciiBuffer(" ", new byte[] {}, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_031() throws Exception {
        // Combination: expected="!@#", buffer=new byte[] {}, offset=0, length=-1
        Object actual = ArchiveUtils.matchAsciiBuffer("!@#", new byte[] {}, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_032() throws Exception {
        // Combination: expected="0", buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=-1
        Object actual = ArchiveUtils.matchAsciiBuffer("0", new byte[] {}, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_033() throws Exception {
        // Combination: expected="-1", buffer=new byte[] {}, offset=1, length=-1
        Object actual = ArchiveUtils.matchAsciiBuffer("-1", new byte[] {}, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_034() throws Exception {
        // Combination: expected="1.5", buffer=new byte[] {}, offset=0, length=-1
        Object actual = ArchiveUtils.matchAsciiBuffer("1.5", new byte[] {}, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_035() throws Exception {
        // Combination: expected="9223372036854775807", buffer=new byte[] {}, offset=0, length=-1
        Object actual = ArchiveUtils.matchAsciiBuffer("9223372036854775807", new byte[] {}, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_036() throws Exception {
        // Combination: expected="9223372036854775808", buffer=new byte[] {}, offset=-1, length=-1
        Object actual = ArchiveUtils.matchAsciiBuffer("9223372036854775808", new byte[] {}, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_037() throws Exception {
        // Combination: expected="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", buffer=new byte[] {}, offset=-1, length=-1
        Object actual = ArchiveUtils.matchAsciiBuffer("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new byte[] {}, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_038() throws Exception {
        // Combination: expected="", buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        Object actual = ArchiveUtils.matchAsciiBuffer("", new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_039() throws Exception {
        // Combination: expected=" ", buffer=new byte[] {}, offset=-1, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.matchAsciiBuffer(" ", new byte[] {}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_040() throws Exception {
        // Combination: expected="!@#", buffer=new byte[] {}, offset=0, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.matchAsciiBuffer("!@#", new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_041() throws Exception {
        // Combination: expected="0", buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.matchAsciiBuffer("0", new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_042() throws Exception {
        // Combination: expected="-1", buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.matchAsciiBuffer("-1", new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_043() throws Exception {
        // Combination: expected="1.5", buffer=new byte[] {}, offset=1, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.matchAsciiBuffer("1.5", new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_044() throws Exception {
        // Combination: expected="9223372036854775807", buffer=new byte[] {}, offset=1, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.matchAsciiBuffer("9223372036854775807", new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_045() throws Exception {
        // Combination: expected="9223372036854775808", buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.matchAsciiBuffer("9223372036854775808", new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_046() throws Exception {
        // Combination: expected="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.matchAsciiBuffer("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_047() throws Exception {
        // Combination: expected="", buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        Object actual = ArchiveUtils.matchAsciiBuffer("", new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_048() throws Exception {
        // Combination: expected=" ", buffer=new byte[] {}, offset=1, length=Integer.MIN_VALUE
        Object actual = ArchiveUtils.matchAsciiBuffer(" ", new byte[] {}, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_049() throws Exception {
        // Combination: expected="a", buffer=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        Object actual = ArchiveUtils.matchAsciiBuffer("a", new byte[] {}, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_050() throws Exception {
        // Combination: expected="test123", buffer=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        Object actual = ArchiveUtils.matchAsciiBuffer("test123", new byte[] {}, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_051() throws Exception {
        // Combination: expected="-1", buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        Object actual = ArchiveUtils.matchAsciiBuffer("-1", new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_052() throws Exception {
        // Combination: expected="1.5", buffer=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        Object actual = ArchiveUtils.matchAsciiBuffer("1.5", new byte[] {}, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_053() throws Exception {
        // Combination: expected="9223372036854775807", buffer=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        Object actual = ArchiveUtils.matchAsciiBuffer("9223372036854775807", new byte[] {}, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_054() throws Exception {
        // Combination: expected="9223372036854775808", buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        Object actual = ArchiveUtils.matchAsciiBuffer("9223372036854775808", new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_055() throws Exception {
        // Combination: expected="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        Object actual = ArchiveUtils.matchAsciiBuffer("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_056() throws Exception {
        // Combination: expected="!@#", buffer=new byte[] {}, offset=1, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("!@#", new byte[] {}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_057() throws Exception {
        // Combination: expected="!@#", buffer=new byte[] {}, offset=-1, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("!@#", new byte[] {}, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_058() throws Exception {
        // Combination: expected=" ", buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer(" ", new byte[] {}, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_059() throws Exception {
        // Combination: expected="a", buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=0
        Object actual = ArchiveUtils.matchAsciiBuffer("a", new byte[] {}, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_060() throws Exception {
        // Combination: expected="", buffer=new byte[] {}
        Object actual = ArchiveUtils.matchAsciiBuffer("", new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_061() throws Exception {
        // Combination: expected=" ", buffer=new byte[] {}
        Object actual = ArchiveUtils.matchAsciiBuffer(" ", new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_062() throws Exception {
        // Combination: expected="a", buffer=new byte[] {}
        Object actual = ArchiveUtils.matchAsciiBuffer("a", new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_063() throws Exception {
        // Combination: expected="test123", buffer=new byte[] {}
        Object actual = ArchiveUtils.matchAsciiBuffer("test123", new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_064() throws Exception {
        // Combination: expected="!@#", buffer=new byte[] {}
        Object actual = ArchiveUtils.matchAsciiBuffer("!@#", new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_065() throws Exception {
        // Combination: expected="0", buffer=new byte[] {}
        Object actual = ArchiveUtils.matchAsciiBuffer("0", new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_066() throws Exception {
        // Combination: expected="-1", buffer=new byte[] {}
        Object actual = ArchiveUtils.matchAsciiBuffer("-1", new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_067() throws Exception {
        // Combination: expected="1.5", buffer=new byte[] {}
        Object actual = ArchiveUtils.matchAsciiBuffer("1.5", new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_068() throws Exception {
        // Combination: expected="9223372036854775807", buffer=new byte[] {}
        Object actual = ArchiveUtils.matchAsciiBuffer("9223372036854775807", new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_069() throws Exception {
        // Combination: expected="9223372036854775808", buffer=new byte[] {}
        Object actual = ArchiveUtils.matchAsciiBuffer("9223372036854775808", new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_070() throws Exception {
        // Combination: expected="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", buffer=new byte[] {}
        Object actual = ArchiveUtils.matchAsciiBuffer("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_071() throws Exception {
        // Combination: expected="", buffer=new byte[] {1}
        Object actual = ArchiveUtils.matchAsciiBuffer("", new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_072() throws Exception {
        // Combination: expected=" ", buffer=new byte[] {1}
        Object actual = ArchiveUtils.matchAsciiBuffer(" ", new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_073() throws Exception {
        // Combination: expected="a", buffer=new byte[] {1}
        Object actual = ArchiveUtils.matchAsciiBuffer("a", new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_074() throws Exception {
        // Combination: expected="test123", buffer=new byte[] {1}
        Object actual = ArchiveUtils.matchAsciiBuffer("test123", new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_075() throws Exception {
        // Combination: expected="!@#", buffer=new byte[] {1}
        Object actual = ArchiveUtils.matchAsciiBuffer("!@#", new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_076() throws Exception {
        // Combination: expected="0", buffer=new byte[] {1}
        Object actual = ArchiveUtils.matchAsciiBuffer("0", new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_077() throws Exception {
        // Combination: expected="-1", buffer=new byte[] {1}
        Object actual = ArchiveUtils.matchAsciiBuffer("-1", new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_078() throws Exception {
        // Combination: expected="1.5", buffer=new byte[] {1}
        Object actual = ArchiveUtils.matchAsciiBuffer("1.5", new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_079() throws Exception {
        // Combination: expected="9223372036854775807", buffer=new byte[] {1}
        Object actual = ArchiveUtils.matchAsciiBuffer("9223372036854775807", new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_080() throws Exception {
        // Combination: expected="9223372036854775808", buffer=new byte[] {1}
        Object actual = ArchiveUtils.matchAsciiBuffer("9223372036854775808", new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_matchAsciiBuffer_pairwise_081() throws Exception {
        // Combination: expected="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", buffer=new byte[] {1}
        Object actual = ArchiveUtils.matchAsciiBuffer("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_082() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=0, length=0
        Object actual = ArchiveUtils.toAsciiString(new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_083() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=1, length=1
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, 1, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_084() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=-1, length=-1
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, -1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_085() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_086() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_087() throws Exception {
        // Combination: inputBytes=new byte[] {1}, offset=1, length=0
        Object actual = ArchiveUtils.toAsciiString(new byte[] {1}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_088() throws Exception {
        // Combination: inputBytes=new byte[] {1}, offset=0, length=1
        Object actual = ArchiveUtils.toAsciiString(new byte[] {1}, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\u0001", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_089() throws Exception {
        // Combination: inputBytes=new byte[] {1}, offset=Integer.MAX_VALUE, length=-1
        try {
            ArchiveUtils.toAsciiString(new byte[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_090() throws Exception {
        // Combination: inputBytes=new byte[] {1}, offset=-1, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.toAsciiString(new byte[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_091() throws Exception {
        // Combination: inputBytes=new byte[] {1}, offset=0, length=Integer.MIN_VALUE
        try {
            ArchiveUtils.toAsciiString(new byte[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_092() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=0, length=-1
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, 0, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_093() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=0, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_094() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=1, length=-1
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, 1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_095() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=1, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_096() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=1, length=Integer.MIN_VALUE
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_097() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=-1, length=0
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, -1, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_098() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=-1, length=1
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, -1, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_099() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_100() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_101() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=Integer.MAX_VALUE, length=1
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_102() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_103() throws Exception {
        // Combination: inputBytes=new byte[] {1}, offset=Integer.MIN_VALUE, length=0
        try {
            ArchiveUtils.toAsciiString(new byte[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_104() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=Integer.MIN_VALUE, length=1
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_105() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=Integer.MIN_VALUE, length=-1
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toAsciiString_pairwise_106() throws Exception {
        // Combination: inputBytes=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            ArchiveUtils.toAsciiString(new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_107() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=0, buffer2=new byte[] {}, offset2=0, length2=0, ignoreTrailingNulls=true
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 0, 0, new byte[] {}, 0, 0, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_108() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=1, buffer2=new byte[] {1}, offset2=1, length2=1, ignoreTrailingNulls=false
        try {
            ArchiveUtils.isEqual(new byte[] {}, 1, 1, new byte[] {1}, 1, 1, false);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_109() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=-1, length1=-1, buffer2=new byte[] {}, offset2=-1, length2=-1, ignoreTrailingNulls=false
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, -1, -1, new byte[] {}, -1, -1, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_110() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=Integer.MAX_VALUE, length1=Integer.MAX_VALUE, buffer2=new byte[] {1}, offset2=Integer.MAX_VALUE, length2=Integer.MAX_VALUE, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {1}, Integer.MAX_VALUE, Integer.MAX_VALUE, new byte[] {1}, Integer.MAX_VALUE, Integer.MAX_VALUE, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_111() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=Integer.MIN_VALUE, length1=0, buffer2=new byte[] {1}, offset2=Integer.MIN_VALUE, length2=Integer.MIN_VALUE, ignoreTrailingNulls=false
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, Integer.MIN_VALUE, 0, new byte[] {1}, Integer.MIN_VALUE, Integer.MIN_VALUE, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_112() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=Integer.MIN_VALUE, length1=1, buffer2=new byte[] {}, offset2=0, length2=1, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {1}, Integer.MIN_VALUE, 1, new byte[] {}, 0, 1, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_113() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=-1, buffer2=new byte[] {1}, offset2=Integer.MIN_VALUE, length2=-1, ignoreTrailingNulls=true
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 0, -1, new byte[] {1}, Integer.MIN_VALUE, -1, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_114() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=1, length2=Integer.MAX_VALUE, ignoreTrailingNulls=false
        try {
            ArchiveUtils.isEqual(new byte[] {}, 0, Integer.MAX_VALUE, new byte[] {}, 1, Integer.MAX_VALUE, false);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_115() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=-1, length2=Integer.MIN_VALUE, ignoreTrailingNulls=true
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 1, Integer.MIN_VALUE, new byte[] {}, -1, Integer.MIN_VALUE, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_116() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=-1, length1=Integer.MIN_VALUE, buffer2=new byte[] {1}, offset2=0, length2=0, ignoreTrailingNulls=false
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, -1, Integer.MIN_VALUE, new byte[] {1}, 0, 0, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_117() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=1, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=0, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, Integer.MAX_VALUE, 1, new byte[] {}, Integer.MAX_VALUE, 0, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_118() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=-1, buffer2=new byte[] {}, offset2=1, length2=0, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, Integer.MIN_VALUE, -1, new byte[] {}, 1, 0, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_119() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=-1, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=0, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, -1, Integer.MAX_VALUE, new byte[] {}, Integer.MIN_VALUE, 0, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_120() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=-1, length1=0, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=1, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, -1, 0, new byte[] {}, Integer.MAX_VALUE, 1, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_121() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=-1, buffer2=new byte[] {}, offset2=0, length2=1, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, Integer.MAX_VALUE, -1, new byte[] {}, 0, 1, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_122() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=-1, length2=1, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, 0, Integer.MAX_VALUE, new byte[] {}, -1, 1, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_123() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=1, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, 0, Integer.MIN_VALUE, new byte[] {}, Integer.MAX_VALUE, 1, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_124() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=0, buffer2=new byte[] {}, offset2=0, length2=-1, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, 1, 0, new byte[] {}, 0, -1, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_125() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=1, buffer2=new byte[] {}, offset2=1, length2=-1, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, 0, 1, new byte[] {}, 1, -1, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_126() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=-1, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, -1, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_127() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=1, length2=-1, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE, new byte[] {}, 1, -1, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_128() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=0, buffer2=new byte[] {}, offset2=-1, length2=Integer.MAX_VALUE, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, 1, 0, new byte[] {}, -1, Integer.MAX_VALUE, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_129() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=-1, length1=1, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=Integer.MAX_VALUE, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, -1, 1, new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_130() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=-1, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=Integer.MAX_VALUE, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, 1, -1, new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_131() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=0, length2=Integer.MAX_VALUE, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE, new byte[] {}, 0, Integer.MAX_VALUE, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_132() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=1, buffer2=new byte[] {}, offset2=0, length2=Integer.MIN_VALUE, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, 0, 1, new byte[] {}, 0, Integer.MIN_VALUE, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_133() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=-1, length1=-1, buffer2=new byte[] {}, offset2=1, length2=Integer.MIN_VALUE, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, -1, -1, new byte[] {}, 1, Integer.MIN_VALUE, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_134() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=0, length2=Integer.MIN_VALUE, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, 1, Integer.MAX_VALUE, new byte[] {}, 0, Integer.MIN_VALUE, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_135() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=0, length1=0, buffer2=new byte[] {}, offset2=1, length2=0, ignoreTrailingNulls=true
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, 0, 0, new byte[] {}, 1, 0, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_136() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=1, length1=0, buffer2=new byte[] {}, offset2=-1, length2=0, ignoreTrailingNulls=true
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, 1, 0, new byte[] {}, -1, 0, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_137() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=0, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=Integer.MIN_VALUE, ignoreTrailingNulls=false
        Object actual = ArchiveUtils.isEqual(new byte[] {}, Integer.MAX_VALUE, 0, new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_138() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=1, buffer2=new byte[] {1}, offset2=-1, length2=0, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, Integer.MAX_VALUE, 1, new byte[] {1}, -1, 0, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_139() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=0, buffer2=new byte[] {}, offset2=-1, length2=0, ignoreTrailingNulls=true
        Object actual = ArchiveUtils.isEqual(new byte[] {}, Integer.MIN_VALUE, 0, new byte[] {}, -1, 0, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_140() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=1, ignoreTrailingNulls=true
        try {
            ArchiveUtils.isEqual(new byte[] {}, 1, Integer.MIN_VALUE, new byte[] {}, Integer.MIN_VALUE, 1, true);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_141() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=0, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=0, ignoreTrailingNulls=true
        Object actual = ArchiveUtils.isEqual(new byte[] {}, Integer.MAX_VALUE, 0, new byte[] {}, Integer.MIN_VALUE, 0, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_142() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=0, buffer2=new byte[] {}, offset2=0, length2=0
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 0, 0, new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_143() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=1, buffer2=new byte[] {1}, offset2=1, length2=1
        try {
            ArchiveUtils.isEqual(new byte[] {}, 1, 1, new byte[] {1}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_144() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=-1, length1=1, buffer2=new byte[] {}, offset2=-1, length2=-1
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, -1, 1, new byte[] {}, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_145() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=Integer.MAX_VALUE, length1=0, buffer2=new byte[] {1}, offset2=Integer.MAX_VALUE, length2=Integer.MAX_VALUE
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, Integer.MAX_VALUE, 0, new byte[] {1}, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_146() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=-1, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=Integer.MAX_VALUE
        Object actual = ArchiveUtils.isEqual(new byte[] {}, Integer.MIN_VALUE, -1, new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_147() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=0, length1=-1, buffer2=new byte[] {1}, offset2=1, length2=0
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, 0, -1, new byte[] {1}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_148() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=1, length2=Integer.MIN_VALUE
        Object actual = ArchiveUtils.isEqual(new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE, new byte[] {}, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_149() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=Integer.MIN_VALUE, length1=Integer.MAX_VALUE, buffer2=new byte[] {1}, offset2=0, length2=1
        try {
            ArchiveUtils.isEqual(new byte[] {1}, Integer.MIN_VALUE, Integer.MAX_VALUE, new byte[] {1}, 0, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_150() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=-1, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=1
        Object actual = ArchiveUtils.isEqual(new byte[] {}, -1, Integer.MIN_VALUE, new byte[] {}, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_151() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=1, length1=Integer.MIN_VALUE, buffer2=new byte[] {1}, offset2=Integer.MIN_VALUE, length2=Integer.MIN_VALUE
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, 1, Integer.MIN_VALUE, new byte[] {1}, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_152() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=1, buffer2=new byte[] {}, offset2=-1, length2=0
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 1, 1, new byte[] {}, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_153() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=-1, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=0
        Object actual = ArchiveUtils.isEqual(new byte[] {}, -1, Integer.MAX_VALUE, new byte[] {}, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_154() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=0, length2=0
        Object actual = ArchiveUtils.isEqual(new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE, new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_155() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=0, buffer2=new byte[] {}, offset2=-1, length2=1
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 0, 0, new byte[] {}, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_156() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=-1, buffer2=new byte[] {}, offset2=-1, length2=1
        Object actual = ArchiveUtils.isEqual(new byte[] {}, Integer.MAX_VALUE, -1, new byte[] {}, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_157() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=0, buffer2=new byte[] {1}, offset2=0, length2=-1
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 1, 0, new byte[] {1}, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_158() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=-1, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=-1
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 0, -1, new byte[] {}, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_159() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=-1
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 0, Integer.MAX_VALUE, new byte[] {}, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_160() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=1, length2=-1
        Object actual = ArchiveUtils.isEqual(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE, new byte[] {}, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_161() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=1, buffer2=new byte[] {}, offset2=0, length2=Integer.MAX_VALUE
        try {
            ArchiveUtils.isEqual(new byte[] {}, 0, 1, new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_162() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=-1, length2=Integer.MAX_VALUE
        try {
            ArchiveUtils.isEqual(new byte[] {}, 1, Integer.MAX_VALUE, new byte[] {}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_163() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=1, length2=Integer.MAX_VALUE
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 0, Integer.MIN_VALUE, new byte[] {}, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_164() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=-1, length1=0, buffer2=new byte[] {}, offset2=0, length2=Integer.MIN_VALUE
        Object actual = ArchiveUtils.isEqual(new byte[] {}, -1, 0, new byte[] {}, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_165() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=1, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=Integer.MIN_VALUE
        Object actual = ArchiveUtils.isEqual(new byte[] {}, Integer.MIN_VALUE, 1, new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_166() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=-1, buffer2=new byte[] {}, offset2=0, length2=Integer.MIN_VALUE
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 0, -1, new byte[] {}, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_167() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=-1, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=0
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 1, -1, new byte[] {}, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_168() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=-1, length1=-1, buffer2=new byte[] {1}, offset2=1, length2=Integer.MAX_VALUE
        Object actual = ArchiveUtils.isEqual(new byte[] {}, -1, -1, new byte[] {1}, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_169() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=1, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=-1
        Object actual = ArchiveUtils.isEqual(new byte[] {}, Integer.MAX_VALUE, 1, new byte[] {}, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_170() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=0, buffer2=new byte[] {}, offset2=1, length2=0
        Object actual = ArchiveUtils.isEqual(new byte[] {}, Integer.MIN_VALUE, 0, new byte[] {}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_171() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=Integer.MIN_VALUE, buffer2=new byte[] {1}, offset2=-1, length2=Integer.MIN_VALUE
        Object actual = ArchiveUtils.isEqual(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE, new byte[] {1}, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_172() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=0
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 0, Integer.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_173() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=0, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=1
        Object actual = ArchiveUtils.isEqual(new byte[] {}, 0, 0, new byte[] {}, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_174() throws Exception {
        // Combination: buffer1=new byte[] {}, buffer2=new byte[] {}
        Object actual = ArchiveUtils.isEqual(new byte[] {}, new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_175() throws Exception {
        // Combination: buffer1=new byte[] {}, buffer2=new byte[] {1}
        Object actual = ArchiveUtils.isEqual(new byte[] {}, new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_176() throws Exception {
        // Combination: buffer1=new byte[] {1}, buffer2=new byte[] {}
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, new byte[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_177() throws Exception {
        // Combination: buffer1=new byte[] {1}, buffer2=new byte[] {1}
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, new byte[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_178() throws Exception {
        // Combination: buffer1=new byte[] {}, buffer2=new byte[] {}, ignoreTrailingNulls=true
        Object actual = ArchiveUtils.isEqual(new byte[] {}, new byte[] {}, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_179() throws Exception {
        // Combination: buffer1=new byte[] {}, buffer2=new byte[] {1}, ignoreTrailingNulls=false
        Object actual = ArchiveUtils.isEqual(new byte[] {}, new byte[] {1}, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_180() throws Exception {
        // Combination: buffer1=new byte[] {1}, buffer2=new byte[] {}, ignoreTrailingNulls=false
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, new byte[] {}, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqual_pairwise_181() throws Exception {
        // Combination: buffer1=new byte[] {1}, buffer2=new byte[] {1}, ignoreTrailingNulls=true
        Object actual = ArchiveUtils.isEqual(new byte[] {1}, new byte[] {1}, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_182() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=0, buffer2=new byte[] {}, offset2=0, length2=0
        Object actual = ArchiveUtils.isEqualWithNull(new byte[] {}, 0, 0, new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_183() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=1, buffer2=new byte[] {1}, offset2=1, length2=1
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, 1, 1, new byte[] {1}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_184() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=-1, length1=1, buffer2=new byte[] {}, offset2=-1, length2=-1
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {1}, -1, 1, new byte[] {}, -1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_185() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=Integer.MAX_VALUE, length1=0, buffer2=new byte[] {1}, offset2=Integer.MAX_VALUE, length2=Integer.MAX_VALUE
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {1}, Integer.MAX_VALUE, 0, new byte[] {1}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_186() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=-1, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=Integer.MAX_VALUE
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, Integer.MIN_VALUE, -1, new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_187() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=0, length1=-1, buffer2=new byte[] {1}, offset2=1, length2=0
        Object actual = ArchiveUtils.isEqualWithNull(new byte[] {1}, 0, -1, new byte[] {1}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_188() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=1, length2=Integer.MIN_VALUE
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE, new byte[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_189() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=Integer.MIN_VALUE, length1=Integer.MAX_VALUE, buffer2=new byte[] {1}, offset2=0, length2=1
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {1}, Integer.MIN_VALUE, Integer.MAX_VALUE, new byte[] {1}, 0, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_190() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=-1, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=1
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, -1, Integer.MIN_VALUE, new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_191() throws Exception {
        // Combination: buffer1=new byte[] {1}, offset1=1, length1=Integer.MIN_VALUE, buffer2=new byte[] {1}, offset2=Integer.MIN_VALUE, length2=Integer.MIN_VALUE
        Object actual = ArchiveUtils.isEqualWithNull(new byte[] {1}, 1, Integer.MIN_VALUE, new byte[] {1}, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_192() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=1, buffer2=new byte[] {}, offset2=-1, length2=0
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, 1, 1, new byte[] {}, -1, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_193() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=-1, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=0
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, -1, Integer.MAX_VALUE, new byte[] {}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_194() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=0, length2=0
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE, new byte[] {}, 0, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_195() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=0, buffer2=new byte[] {}, offset2=-1, length2=1
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, 0, 0, new byte[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_196() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=-1, buffer2=new byte[] {}, offset2=-1, length2=1
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, Integer.MAX_VALUE, -1, new byte[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_197() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=0, buffer2=new byte[] {1}, offset2=0, length2=-1
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, 1, 0, new byte[] {1}, 0, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_198() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=-1, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=-1
        Object actual = ArchiveUtils.isEqualWithNull(new byte[] {}, 0, -1, new byte[] {}, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_199() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=-1
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, 0, Integer.MAX_VALUE, new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_200() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=1, length2=-1
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE, new byte[] {}, 1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_201() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=1, buffer2=new byte[] {}, offset2=0, length2=Integer.MAX_VALUE
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, 0, 1, new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_202() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=-1, length2=Integer.MAX_VALUE
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, 1, Integer.MAX_VALUE, new byte[] {}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_203() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=Integer.MIN_VALUE, buffer2=new byte[] {}, offset2=1, length2=Integer.MAX_VALUE
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, 0, Integer.MIN_VALUE, new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_204() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=-1, length1=0, buffer2=new byte[] {}, offset2=0, length2=Integer.MIN_VALUE
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, -1, 0, new byte[] {}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_205() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=1, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=Integer.MIN_VALUE
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, Integer.MIN_VALUE, 1, new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_206() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=-1, buffer2=new byte[] {}, offset2=0, length2=Integer.MIN_VALUE
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, 0, -1, new byte[] {}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_207() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=1, length1=-1, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=0
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, 1, -1, new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_208() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=-1, length1=-1, buffer2=new byte[] {1}, offset2=1, length2=Integer.MAX_VALUE
        Object actual = ArchiveUtils.isEqualWithNull(new byte[] {}, -1, -1, new byte[] {1}, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_209() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MAX_VALUE, length1=1, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=-1
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, Integer.MAX_VALUE, 1, new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_210() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=0, buffer2=new byte[] {}, offset2=1, length2=0
        Object actual = ArchiveUtils.isEqualWithNull(new byte[] {}, Integer.MIN_VALUE, 0, new byte[] {}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_211() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=Integer.MIN_VALUE, length1=Integer.MIN_VALUE, buffer2=new byte[] {1}, offset2=-1, length2=Integer.MIN_VALUE
        Object actual = ArchiveUtils.isEqualWithNull(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE, new byte[] {1}, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_212() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=Integer.MAX_VALUE, buffer2=new byte[] {}, offset2=Integer.MAX_VALUE, length2=0
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, 0, Integer.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEqualWithNull_pairwise_213() throws Exception {
        // Combination: buffer1=new byte[] {}, offset1=0, length1=0, buffer2=new byte[] {}, offset2=Integer.MIN_VALUE, length2=1
        try {
            ArchiveUtils.isEqualWithNull(new byte[] {}, 0, 0, new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isArrayZero_pairwise_214() throws Exception {
        // Combination: a=new byte[] {}, size=0
        Object actual = ArchiveUtils.isArrayZero(new byte[] {}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isArrayZero_pairwise_215() throws Exception {
        // Combination: a=new byte[] {}, size=1
        try {
            ArchiveUtils.isArrayZero(new byte[] {}, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isArrayZero_pairwise_216() throws Exception {
        // Combination: a=new byte[] {}, size=-1
        Object actual = ArchiveUtils.isArrayZero(new byte[] {}, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isArrayZero_pairwise_217() throws Exception {
        // Combination: a=new byte[] {}, size=Integer.MAX_VALUE
        try {
            ArchiveUtils.isArrayZero(new byte[] {}, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isArrayZero_pairwise_218() throws Exception {
        // Combination: a=new byte[] {}, size=Integer.MIN_VALUE
        Object actual = ArchiveUtils.isArrayZero(new byte[] {}, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isArrayZero_pairwise_219() throws Exception {
        // Combination: a=new byte[] {1}, size=0
        Object actual = ArchiveUtils.isArrayZero(new byte[] {1}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isArrayZero_pairwise_220() throws Exception {
        // Combination: a=new byte[] {1}, size=1
        Object actual = ArchiveUtils.isArrayZero(new byte[] {1}, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isArrayZero_pairwise_221() throws Exception {
        // Combination: a=new byte[] {1}, size=-1
        Object actual = ArchiveUtils.isArrayZero(new byte[] {1}, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isArrayZero_pairwise_222() throws Exception {
        // Combination: a=new byte[] {1}, size=Integer.MAX_VALUE
        Object actual = ArchiveUtils.isArrayZero(new byte[] {1}, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isArrayZero_pairwise_223() throws Exception {
        // Combination: a=new byte[] {1}, size=Integer.MIN_VALUE
        Object actual = ArchiveUtils.isArrayZero(new byte[] {1}, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

}
