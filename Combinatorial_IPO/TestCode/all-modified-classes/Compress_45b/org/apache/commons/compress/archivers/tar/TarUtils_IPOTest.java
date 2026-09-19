package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for TarUtils.
 */
public class TarUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_001() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=0, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(0L, new byte[] {}, 0, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_002() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=1, length=1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(1L, new byte[] {}, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_003() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(-1L, new byte[] {}, -1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_004() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_005() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_006() throws Exception {
        // Combination: value=-1L, buf=new byte[] {1}, offset=1, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(-1L, new byte[] {1}, 1, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_007() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {1}, offset=0, length=1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, new byte[] {1}, 0, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_008() throws Exception {
        // Combination: value=0L, buf=new byte[] {1}, offset=Integer.MAX_VALUE, length=-1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(0L, new byte[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_009() throws Exception {
        // Combination: value=1L, buf=new byte[] {1}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(1L, new byte[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_010() throws Exception {
        // Combination: value=1L, buf=new byte[] {1}, offset=0, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(1L, new byte[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_011() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=0, length=-1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, new byte[] {}, 0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_012() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=0, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(-1L, new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_013() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=1, length=-1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, new byte[] {}, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_014() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(0L, new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_015() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=1, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(0L, new byte[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_016() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=-1, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, new byte[] {}, -1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_017() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=-1, length=1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(0L, new byte[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_018() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(-1L, new byte[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_019() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(1L, new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_020() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(-1L, new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_021() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_022() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {1}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, new byte[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_023() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(0L, new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_024() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=-1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(1L, new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_025() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(-1L, new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_026() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, new byte[] {}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_027() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=1, length=1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, new byte[] {}, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_028() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, new byte[] {}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_029() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
