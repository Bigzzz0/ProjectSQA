package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for TarUtils.
 */
public class TarUtils_IPOTest {
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
    public void test_parseOctal_pairwise_001() throws Exception {
        // Combination: buffer=new byte[] {}, offset=0, length=0
        try {
            TarUtils.parseOctal(new byte[] {}, 0, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_002() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1, length=1
        try {
            TarUtils.parseOctal(new byte[] {}, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_003() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.parseOctal(new byte[] {}, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_004() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.parseOctal(new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_005() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.parseOctal(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_006() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=1, length=0
        try {
            TarUtils.parseOctal(new byte[] {1}, 1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_007() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=0, length=1
        try {
            TarUtils.parseOctal(new byte[] {1}, 0, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_008() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=Integer.MAX_VALUE, length=-1
        try {
            TarUtils.parseOctal(new byte[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_009() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.parseOctal(new byte[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_010() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=0, length=Integer.MIN_VALUE
        try {
            TarUtils.parseOctal(new byte[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_011() throws Exception {
        // Combination: buffer=new byte[] {}, offset=0, length=-1
        try {
            TarUtils.parseOctal(new byte[] {}, 0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_012() throws Exception {
        // Combination: buffer=new byte[] {}, offset=0, length=Integer.MAX_VALUE
        try {
            TarUtils.parseOctal(new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_013() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1, length=-1
        try {
            TarUtils.parseOctal(new byte[] {}, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_014() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1, length=Integer.MAX_VALUE
        try {
            TarUtils.parseOctal(new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_015() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1, length=Integer.MIN_VALUE
        try {
            TarUtils.parseOctal(new byte[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_016() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1, length=0
        try {
            TarUtils.parseOctal(new byte[] {}, -1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_017() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1, length=1
        try {
            TarUtils.parseOctal(new byte[] {}, -1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_018() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        try {
            TarUtils.parseOctal(new byte[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_019() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.parseOctal(new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_020() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=1
        try {
            TarUtils.parseOctal(new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_021() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.parseOctal(new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_022() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.parseOctal(new byte[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_023() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=1
        try {
            TarUtils.parseOctal(new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_024() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=-1
        try {
            TarUtils.parseOctal(new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctal_pairwise_025() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.parseOctal(new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_026() throws Exception {
        // Combination: buffer=new byte[] {}, offset=0, length=0
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, 0, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_027() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1, length=1
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_028() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, -1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_029() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_030() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_031() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=1, length=0
        try {
            TarUtils.parseOctalOrBinary(new byte[] {1}, 1, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_032() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=0, length=1
        try {
            TarUtils.parseOctalOrBinary(new byte[] {1}, 0, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_033() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=Integer.MAX_VALUE, length=-1
        try {
            TarUtils.parseOctalOrBinary(new byte[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_034() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.parseOctalOrBinary(new byte[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_035() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=0, length=Integer.MIN_VALUE
        try {
            TarUtils.parseOctalOrBinary(new byte[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_036() throws Exception {
        // Combination: buffer=new byte[] {}, offset=0, length=-1
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, 0, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_037() throws Exception {
        // Combination: buffer=new byte[] {}, offset=0, length=Integer.MAX_VALUE
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_038() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1, length=-1
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, 1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_039() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1, length=Integer.MAX_VALUE
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_040() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1, length=Integer.MIN_VALUE
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_041() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1, length=0
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, -1, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_042() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1, length=1
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_043() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_044() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_045() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=1
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_046() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_047() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.parseOctalOrBinary(new byte[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_048() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=1
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_049() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=-1
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseOctalOrBinary_pairwise_050() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.parseOctalOrBinary(new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBoolean_pairwise_051() throws Exception {
        // Combination: buffer=new byte[] {}, offset=0
        try {
            TarUtils.parseBoolean(new byte[] {}, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBoolean_pairwise_052() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1
        try {
            TarUtils.parseBoolean(new byte[] {}, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBoolean_pairwise_053() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1
        try {
            TarUtils.parseBoolean(new byte[] {}, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBoolean_pairwise_054() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE
        try {
            TarUtils.parseBoolean(new byte[] {}, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBoolean_pairwise_055() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE
        try {
            TarUtils.parseBoolean(new byte[] {}, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBoolean_pairwise_056() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=0
        Object actual = TarUtils.parseBoolean(new byte[] {1}, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBoolean_pairwise_057() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=1
        try {
            TarUtils.parseBoolean(new byte[] {1}, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBoolean_pairwise_058() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=-1
        try {
            TarUtils.parseBoolean(new byte[] {1}, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBoolean_pairwise_059() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=Integer.MAX_VALUE
        try {
            TarUtils.parseBoolean(new byte[] {1}, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBoolean_pairwise_060() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=Integer.MIN_VALUE
        try {
            TarUtils.parseBoolean(new byte[] {1}, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_061() throws Exception {
        // Combination: buffer=new byte[] {}, offset=0, length=0
        Object actual = TarUtils.parseName(new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_062() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1, length=1
        try {
            TarUtils.parseName(new byte[] {}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_063() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1, length=-1
        Object actual = TarUtils.parseName(new byte[] {}, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_064() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.parseName(new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_065() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        Object actual = TarUtils.parseName(new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_066() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=1, length=0
        Object actual = TarUtils.parseName(new byte[] {1}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_067() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=0, length=1
        Object actual = TarUtils.parseName(new byte[] {1}, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\u0001", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_068() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=Integer.MAX_VALUE, length=-1
        Object actual = TarUtils.parseName(new byte[] {1}, Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_069() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.parseName(new byte[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_070() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=0, length=Integer.MIN_VALUE
        Object actual = TarUtils.parseName(new byte[] {1}, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_071() throws Exception {
        // Combination: buffer=new byte[] {}, offset=0, length=-1
        Object actual = TarUtils.parseName(new byte[] {}, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_072() throws Exception {
        // Combination: buffer=new byte[] {}, offset=0, length=Integer.MAX_VALUE
        try {
            TarUtils.parseName(new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_073() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1, length=-1
        Object actual = TarUtils.parseName(new byte[] {}, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_074() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1, length=Integer.MAX_VALUE
        try {
            TarUtils.parseName(new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_075() throws Exception {
        // Combination: buffer=new byte[] {}, offset=1, length=Integer.MIN_VALUE
        Object actual = TarUtils.parseName(new byte[] {}, 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_076() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1, length=0
        Object actual = TarUtils.parseName(new byte[] {}, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_077() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1, length=1
        try {
            TarUtils.parseName(new byte[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_078() throws Exception {
        // Combination: buffer=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        Object actual = TarUtils.parseName(new byte[] {}, -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_079() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        Object actual = TarUtils.parseName(new byte[] {}, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_080() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=1
        try {
            TarUtils.parseName(new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_081() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        Object actual = TarUtils.parseName(new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_082() throws Exception {
        // Combination: buffer=new byte[] {1}, offset=Integer.MIN_VALUE, length=0
        Object actual = TarUtils.parseName(new byte[] {1}, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_083() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=1
        try {
            TarUtils.parseName(new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_084() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=-1
        Object actual = TarUtils.parseName(new byte[] {}, Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseName_pairwise_085() throws Exception {
        // Combination: buffer=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.parseName(new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_086() throws Exception {
        // Combination: name="", buf=new byte[] {}, offset=0, length=0
        Object actual = TarUtils.formatNameBytes("", new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_087() throws Exception {
        // Combination: name=" ", buf=new byte[] {}, offset=1, length=1
        try {
            TarUtils.formatNameBytes(" ", new byte[] {}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_088() throws Exception {
        // Combination: name="a", buf=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.formatNameBytes("a", new byte[] {}, -1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_089() throws Exception {
        // Combination: name="test123", buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatNameBytes("test123", new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_090() throws Exception {
        // Combination: name="!@#", buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatNameBytes("!@#", new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_091() throws Exception {
        // Combination: name=" ", buf=new byte[] {1}, offset=-1, length=0
        try {
            TarUtils.formatNameBytes(" ", new byte[] {1}, -1, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_092() throws Exception {
        // Combination: name="", buf=new byte[] {1}, offset=Integer.MAX_VALUE, length=1
        try {
            TarUtils.formatNameBytes("", new byte[] {1}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_093() throws Exception {
        // Combination: name="test123", buf=new byte[] {1}, offset=0, length=-1
        Object actual = TarUtils.formatNameBytes("test123", new byte[] {1}, 0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_094() throws Exception {
        // Combination: name="a", buf=new byte[] {1}, offset=1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatNameBytes("a", new byte[] {1}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_095() throws Exception {
        // Combination: name="0", buf=new byte[] {1}, offset=0, length=Integer.MIN_VALUE
        Object actual = TarUtils.formatNameBytes("0", new byte[] {1}, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_096() throws Exception {
        // Combination: name="", buf=new byte[] {}, offset=1, length=-1
        try {
            TarUtils.formatNameBytes("", new byte[] {}, 1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_097() throws Exception {
        // Combination: name="", buf=new byte[] {}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatNameBytes("", new byte[] {}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_098() throws Exception {
        // Combination: name="", buf=new byte[] {}, offset=1, length=Integer.MIN_VALUE
        try {
            TarUtils.formatNameBytes("", new byte[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_099() throws Exception {
        // Combination: name=" ", buf=new byte[] {}, offset=Integer.MAX_VALUE, length=-1
        try {
            TarUtils.formatNameBytes(" ", new byte[] {}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_100() throws Exception {
        // Combination: name=" ", buf=new byte[] {}, offset=0, length=Integer.MAX_VALUE
        try {
            TarUtils.formatNameBytes(" ", new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_101() throws Exception {
        // Combination: name=" ", buf=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        try {
            TarUtils.formatNameBytes(" ", new byte[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_102() throws Exception {
        // Combination: name="a", buf=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.formatNameBytes("a", new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_103() throws Exception {
        // Combination: name="a", buf=new byte[] {}, offset=0, length=1
        try {
            TarUtils.formatNameBytes("a", new byte[] {}, 0, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_104() throws Exception {
        // Combination: name="a", buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatNameBytes("a", new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_105() throws Exception {
        // Combination: name="test123", buf=new byte[] {}, offset=1, length=0
        try {
            TarUtils.formatNameBytes("test123", new byte[] {}, 1, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_106() throws Exception {
        // Combination: name="test123", buf=new byte[] {}, offset=-1, length=1
        try {
            TarUtils.formatNameBytes("test123", new byte[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_107() throws Exception {
        // Combination: name="test123", buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatNameBytes("test123", new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_108() throws Exception {
        // Combination: name="!@#", buf=new byte[] {1}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatNameBytes("!@#", new byte[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_109() throws Exception {
        // Combination: name="!@#", buf=new byte[] {}, offset=0, length=1
        try {
            TarUtils.formatNameBytes("!@#", new byte[] {}, 0, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_110() throws Exception {
        // Combination: name="!@#", buf=new byte[] {}, offset=1, length=-1
        try {
            TarUtils.formatNameBytes("!@#", new byte[] {}, 1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_111() throws Exception {
        // Combination: name="!@#", buf=new byte[] {}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatNameBytes("!@#", new byte[] {}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_112() throws Exception {
        // Combination: name="0", buf=new byte[] {}, offset=1, length=0
        try {
            TarUtils.formatNameBytes("0", new byte[] {}, 1, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_113() throws Exception {
        // Combination: name="0", buf=new byte[] {}, offset=Integer.MIN_VALUE, length=1
        try {
            TarUtils.formatNameBytes("0", new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_114() throws Exception {
        // Combination: name="0", buf=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.formatNameBytes("0", new byte[] {}, -1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_115() throws Exception {
        // Combination: name="0", buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatNameBytes("0", new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_116() throws Exception {
        // Combination: name="-1", buf=new byte[] {}, offset=0, length=0
        Object actual = TarUtils.formatNameBytes("-1", new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_117() throws Exception {
        // Combination: name="-1", buf=new byte[] {1}, offset=1, length=1
        try {
            TarUtils.formatNameBytes("-1", new byte[] {1}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_118() throws Exception {
        // Combination: name="-1", buf=new byte[] {}, offset=Integer.MIN_VALUE, length=-1
        try {
            TarUtils.formatNameBytes("-1", new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_119() throws Exception {
        // Combination: name="-1", buf=new byte[] {}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatNameBytes("-1", new byte[] {}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_120() throws Exception {
        // Combination: name="-1", buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatNameBytes("-1", new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_121() throws Exception {
        // Combination: name="1.5", buf=new byte[] {}, offset=0, length=0
        Object actual = TarUtils.formatNameBytes("1.5", new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_122() throws Exception {
        // Combination: name="1.5", buf=new byte[] {1}, offset=1, length=1
        try {
            TarUtils.formatNameBytes("1.5", new byte[] {1}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_123() throws Exception {
        // Combination: name="1.5", buf=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.formatNameBytes("1.5", new byte[] {}, -1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_124() throws Exception {
        // Combination: name="1.5", buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatNameBytes("1.5", new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_125() throws Exception {
        // Combination: name="1.5", buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatNameBytes("1.5", new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_126() throws Exception {
        // Combination: name="9223372036854775807", buf=new byte[] {}, offset=0, length=0
        Object actual = TarUtils.formatNameBytes("9223372036854775807", new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_127() throws Exception {
        // Combination: name="9223372036854775807", buf=new byte[] {1}, offset=1, length=1
        try {
            TarUtils.formatNameBytes("9223372036854775807", new byte[] {1}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_128() throws Exception {
        // Combination: name="9223372036854775807", buf=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.formatNameBytes("9223372036854775807", new byte[] {}, -1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_129() throws Exception {
        // Combination: name="9223372036854775807", buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatNameBytes("9223372036854775807", new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_130() throws Exception {
        // Combination: name="9223372036854775807", buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatNameBytes("9223372036854775807", new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_131() throws Exception {
        // Combination: name="9223372036854775808", buf=new byte[] {}, offset=0, length=0
        Object actual = TarUtils.formatNameBytes("9223372036854775808", new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_132() throws Exception {
        // Combination: name="9223372036854775808", buf=new byte[] {1}, offset=1, length=1
        try {
            TarUtils.formatNameBytes("9223372036854775808", new byte[] {1}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_133() throws Exception {
        // Combination: name="9223372036854775808", buf=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.formatNameBytes("9223372036854775808", new byte[] {}, -1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_134() throws Exception {
        // Combination: name="9223372036854775808", buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatNameBytes("9223372036854775808", new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_135() throws Exception {
        // Combination: name="9223372036854775808", buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatNameBytes("9223372036854775808", new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_136() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", buf=new byte[] {}, offset=0, length=0
        Object actual = TarUtils.formatNameBytes("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new byte[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_137() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", buf=new byte[] {1}, offset=1, length=1
        try {
            TarUtils.formatNameBytes("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new byte[] {1}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_138() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", buf=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.formatNameBytes("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new byte[] {}, -1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_139() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatNameBytes("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_140() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatNameBytes("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_141() throws Exception {
        // Combination: name="!@#", buf=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.formatNameBytes("!@#", new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_142() throws Exception {
        // Combination: name="", buf=new byte[] {}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatNameBytes("", new byte[] {}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_143() throws Exception {
        // Combination: name=" ", buf=new byte[] {}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatNameBytes(" ", new byte[] {}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatNameBytes_pairwise_144() throws Exception {
        // Combination: name="a", buf=new byte[] {}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatNameBytes("a", new byte[] {}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_145() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=0, length=0
        try {
            TarUtils.formatOctalBytes(0L, new byte[] {}, 0, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_146() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=1, length=1
        try {
            TarUtils.formatOctalBytes(1L, new byte[] {}, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_147() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.formatOctalBytes(-1L, new byte[] {}, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_148() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatOctalBytes(Long.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_149() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatOctalBytes(Long.MIN_VALUE, new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_150() throws Exception {
        // Combination: value=-1L, buf=new byte[] {1}, offset=1, length=0
        try {
            TarUtils.formatOctalBytes(-1L, new byte[] {1}, 1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_151() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {1}, offset=0, length=1
        try {
            TarUtils.formatOctalBytes(Long.MAX_VALUE, new byte[] {1}, 0, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_152() throws Exception {
        // Combination: value=0L, buf=new byte[] {1}, offset=Integer.MAX_VALUE, length=-1
        try {
            TarUtils.formatOctalBytes(0L, new byte[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_153() throws Exception {
        // Combination: value=1L, buf=new byte[] {1}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatOctalBytes(1L, new byte[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_154() throws Exception {
        // Combination: value=1L, buf=new byte[] {1}, offset=0, length=Integer.MIN_VALUE
        try {
            TarUtils.formatOctalBytes(1L, new byte[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_155() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=0, length=-1
        try {
            TarUtils.formatOctalBytes(Long.MIN_VALUE, new byte[] {}, 0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_156() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=0, length=Integer.MAX_VALUE
        try {
            TarUtils.formatOctalBytes(-1L, new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_157() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=1, length=-1
        try {
            TarUtils.formatOctalBytes(Long.MAX_VALUE, new byte[] {}, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_158() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatOctalBytes(0L, new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_159() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=1, length=Integer.MIN_VALUE
        try {
            TarUtils.formatOctalBytes(0L, new byte[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_160() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=-1, length=0
        try {
            TarUtils.formatOctalBytes(Long.MAX_VALUE, new byte[] {}, -1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_161() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=-1, length=1
        try {
            TarUtils.formatOctalBytes(0L, new byte[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_162() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        try {
            TarUtils.formatOctalBytes(-1L, new byte[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_163() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.formatOctalBytes(1L, new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_164() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=1
        try {
            TarUtils.formatOctalBytes(-1L, new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_165() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatOctalBytes(Long.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_166() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {1}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatOctalBytes(Long.MIN_VALUE, new byte[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_167() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=1
        try {
            TarUtils.formatOctalBytes(0L, new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_168() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=-1
        try {
            TarUtils.formatOctalBytes(1L, new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_169() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatOctalBytes(-1L, new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_170() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatOctalBytes(Long.MAX_VALUE, new byte[] {}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_171() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=1, length=1
        try {
            TarUtils.formatOctalBytes(Long.MIN_VALUE, new byte[] {}, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_172() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatOctalBytes(Long.MIN_VALUE, new byte[] {}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatOctalBytes_pairwise_173() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.formatOctalBytes(Long.MIN_VALUE, new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_174() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=0, length=0
        try {
            TarUtils.formatLongOctalBytes(0L, new byte[] {}, 0, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_175() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=1, length=1
        try {
            TarUtils.formatLongOctalBytes(1L, new byte[] {}, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_176() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.formatLongOctalBytes(-1L, new byte[] {}, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_177() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalBytes(Long.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_178() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalBytes(Long.MIN_VALUE, new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_179() throws Exception {
        // Combination: value=-1L, buf=new byte[] {1}, offset=1, length=0
        try {
            TarUtils.formatLongOctalBytes(-1L, new byte[] {1}, 1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_180() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {1}, offset=0, length=1
        try {
            TarUtils.formatLongOctalBytes(Long.MAX_VALUE, new byte[] {1}, 0, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_181() throws Exception {
        // Combination: value=0L, buf=new byte[] {1}, offset=Integer.MAX_VALUE, length=-1
        try {
            TarUtils.formatLongOctalBytes(0L, new byte[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_182() throws Exception {
        // Combination: value=1L, buf=new byte[] {1}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalBytes(1L, new byte[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_183() throws Exception {
        // Combination: value=1L, buf=new byte[] {1}, offset=0, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalBytes(1L, new byte[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_184() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=0, length=-1
        try {
            TarUtils.formatLongOctalBytes(Long.MIN_VALUE, new byte[] {}, 0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_185() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=0, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalBytes(-1L, new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_186() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=1, length=-1
        try {
            TarUtils.formatLongOctalBytes(Long.MAX_VALUE, new byte[] {}, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_187() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalBytes(0L, new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_188() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=1, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalBytes(0L, new byte[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_189() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=-1, length=0
        try {
            TarUtils.formatLongOctalBytes(Long.MAX_VALUE, new byte[] {}, -1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_190() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=-1, length=1
        try {
            TarUtils.formatLongOctalBytes(0L, new byte[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_191() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalBytes(-1L, new byte[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_192() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.formatLongOctalBytes(1L, new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_193() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=1
        try {
            TarUtils.formatLongOctalBytes(-1L, new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_194() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalBytes(Long.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_195() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {1}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatLongOctalBytes(Long.MIN_VALUE, new byte[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_196() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=1
        try {
            TarUtils.formatLongOctalBytes(0L, new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_197() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=-1
        try {
            TarUtils.formatLongOctalBytes(1L, new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_198() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalBytes(-1L, new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_199() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatLongOctalBytes(Long.MAX_VALUE, new byte[] {}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_200() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=1, length=1
        try {
            TarUtils.formatLongOctalBytes(Long.MIN_VALUE, new byte[] {}, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_201() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalBytes(Long.MIN_VALUE, new byte[] {}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalBytes_pairwise_202() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.formatLongOctalBytes(Long.MIN_VALUE, new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_203() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=0, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(0L, new byte[] {}, 0, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_204() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=1, length=1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(1L, new byte[] {}, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_205() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(-1L, new byte[] {}, -1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_206() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_207() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_208() throws Exception {
        // Combination: value=-1L, buf=new byte[] {1}, offset=1, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(-1L, new byte[] {1}, 1, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_209() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {1}, offset=0, length=1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, new byte[] {1}, 0, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_210() throws Exception {
        // Combination: value=0L, buf=new byte[] {1}, offset=Integer.MAX_VALUE, length=-1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(0L, new byte[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_211() throws Exception {
        // Combination: value=1L, buf=new byte[] {1}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(1L, new byte[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_212() throws Exception {
        // Combination: value=1L, buf=new byte[] {1}, offset=0, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(1L, new byte[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_213() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=0, length=-1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, new byte[] {}, 0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_214() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=0, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(-1L, new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_215() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=1, length=-1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, new byte[] {}, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_216() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(0L, new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_217() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=1, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(0L, new byte[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_218() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=-1, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, new byte[] {}, -1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_219() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=-1, length=1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(0L, new byte[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_220() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(-1L, new byte[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_221() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(1L, new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_222() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(-1L, new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_223() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_224() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {1}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, new byte[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_225() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(0L, new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_226() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=-1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(1L, new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_227() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(-1L, new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_228() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, new byte[] {}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_229() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=1, length=1
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, new byte[] {}, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_230() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, new byte[] {}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatLongOctalOrBinaryBytes_pairwise_231() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_232() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=0, length=0
        try {
            TarUtils.formatCheckSumOctalBytes(0L, new byte[] {}, 0, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_233() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=1, length=1
        try {
            TarUtils.formatCheckSumOctalBytes(1L, new byte[] {}, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_234() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=-1, length=-1
        try {
            TarUtils.formatCheckSumOctalBytes(-1L, new byte[] {}, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_235() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatCheckSumOctalBytes(Long.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_236() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatCheckSumOctalBytes(Long.MIN_VALUE, new byte[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_237() throws Exception {
        // Combination: value=-1L, buf=new byte[] {1}, offset=1, length=0
        try {
            TarUtils.formatCheckSumOctalBytes(-1L, new byte[] {1}, 1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_238() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {1}, offset=0, length=1
        try {
            TarUtils.formatCheckSumOctalBytes(Long.MAX_VALUE, new byte[] {1}, 0, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_239() throws Exception {
        // Combination: value=0L, buf=new byte[] {1}, offset=Integer.MAX_VALUE, length=-1
        try {
            TarUtils.formatCheckSumOctalBytes(0L, new byte[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_240() throws Exception {
        // Combination: value=1L, buf=new byte[] {1}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatCheckSumOctalBytes(1L, new byte[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_241() throws Exception {
        // Combination: value=1L, buf=new byte[] {1}, offset=0, length=Integer.MIN_VALUE
        try {
            TarUtils.formatCheckSumOctalBytes(1L, new byte[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_242() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=0, length=-1
        try {
            TarUtils.formatCheckSumOctalBytes(Long.MIN_VALUE, new byte[] {}, 0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_243() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=0, length=Integer.MAX_VALUE
        try {
            TarUtils.formatCheckSumOctalBytes(-1L, new byte[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_244() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=1, length=-1
        try {
            TarUtils.formatCheckSumOctalBytes(Long.MAX_VALUE, new byte[] {}, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_245() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatCheckSumOctalBytes(0L, new byte[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_246() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=1, length=Integer.MIN_VALUE
        try {
            TarUtils.formatCheckSumOctalBytes(0L, new byte[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_247() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=-1, length=0
        try {
            TarUtils.formatCheckSumOctalBytes(Long.MAX_VALUE, new byte[] {}, -1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_248() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=-1, length=1
        try {
            TarUtils.formatCheckSumOctalBytes(0L, new byte[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_249() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=-1, length=Integer.MIN_VALUE
        try {
            TarUtils.formatCheckSumOctalBytes(-1L, new byte[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_250() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.formatCheckSumOctalBytes(1L, new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_251() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=1
        try {
            TarUtils.formatCheckSumOctalBytes(-1L, new byte[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_252() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            TarUtils.formatCheckSumOctalBytes(Long.MAX_VALUE, new byte[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_253() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {1}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatCheckSumOctalBytes(Long.MIN_VALUE, new byte[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_254() throws Exception {
        // Combination: value=0L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=1
        try {
            TarUtils.formatCheckSumOctalBytes(0L, new byte[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_255() throws Exception {
        // Combination: value=1L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=-1
        try {
            TarUtils.formatCheckSumOctalBytes(1L, new byte[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_256() throws Exception {
        // Combination: value=-1L, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            TarUtils.formatCheckSumOctalBytes(-1L, new byte[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_257() throws Exception {
        // Combination: value=Long.MAX_VALUE, buf=new byte[] {}, offset=Integer.MIN_VALUE, length=0
        try {
            TarUtils.formatCheckSumOctalBytes(Long.MAX_VALUE, new byte[] {}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_258() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=1, length=1
        try {
            TarUtils.formatCheckSumOctalBytes(Long.MIN_VALUE, new byte[] {}, 1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_259() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=-1, length=Integer.MAX_VALUE
        try {
            TarUtils.formatCheckSumOctalBytes(Long.MIN_VALUE, new byte[] {}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_formatCheckSumOctalBytes_pairwise_260() throws Exception {
        // Combination: value=Long.MIN_VALUE, buf=new byte[] {}, offset=Integer.MAX_VALUE, length=0
        try {
            TarUtils.formatCheckSumOctalBytes(Long.MIN_VALUE, new byte[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
