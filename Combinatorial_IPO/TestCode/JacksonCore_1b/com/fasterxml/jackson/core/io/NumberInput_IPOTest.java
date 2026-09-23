package com.fasterxml.jackson.core.io;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for NumberInput.
 */
public class NumberInput_IPOTest {
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
    public void test_parseInt_pairwise_001() throws Exception {
        // Combination: digitChars=new char[] {}, offset=0, len=0
        try {
            NumberInput.parseInt(new char[] {}, 0, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_002() throws Exception {
        // Combination: digitChars=new char[] {}, offset=1, len=1
        try {
            NumberInput.parseInt(new char[] {}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_003() throws Exception {
        // Combination: digitChars=new char[] {}, offset=-1, len=-1
        try {
            NumberInput.parseInt(new char[] {}, -1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_004() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MAX_VALUE, len=Integer.MAX_VALUE
        try {
            NumberInput.parseInt(new char[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_005() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MIN_VALUE, len=Integer.MIN_VALUE
        try {
            NumberInput.parseInt(new char[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_006() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=1, len=0
        try {
            NumberInput.parseInt(new char[] {1}, 1, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_007() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=0, len=1
        Object actual = NumberInput.parseInt(new char[] {1}, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-47", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_008() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=Integer.MAX_VALUE, len=-1
        try {
            NumberInput.parseInt(new char[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_009() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=-1, len=Integer.MAX_VALUE
        try {
            NumberInput.parseInt(new char[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_010() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=0, len=Integer.MIN_VALUE
        Object actual = NumberInput.parseInt(new char[] {1}, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-47", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_011() throws Exception {
        // Combination: digitChars=new char[] {}, offset=0, len=-1
        try {
            NumberInput.parseInt(new char[] {}, 0, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_012() throws Exception {
        // Combination: digitChars=new char[] {}, offset=0, len=Integer.MAX_VALUE
        try {
            NumberInput.parseInt(new char[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_013() throws Exception {
        // Combination: digitChars=new char[] {}, offset=1, len=-1
        try {
            NumberInput.parseInt(new char[] {}, 1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_014() throws Exception {
        // Combination: digitChars=new char[] {}, offset=1, len=Integer.MAX_VALUE
        try {
            NumberInput.parseInt(new char[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_015() throws Exception {
        // Combination: digitChars=new char[] {}, offset=1, len=Integer.MIN_VALUE
        try {
            NumberInput.parseInt(new char[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_016() throws Exception {
        // Combination: digitChars=new char[] {}, offset=-1, len=0
        try {
            NumberInput.parseInt(new char[] {}, -1, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_017() throws Exception {
        // Combination: digitChars=new char[] {}, offset=-1, len=1
        try {
            NumberInput.parseInt(new char[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_018() throws Exception {
        // Combination: digitChars=new char[] {}, offset=-1, len=Integer.MIN_VALUE
        try {
            NumberInput.parseInt(new char[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_019() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MAX_VALUE, len=0
        try {
            NumberInput.parseInt(new char[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_020() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MAX_VALUE, len=1
        try {
            NumberInput.parseInt(new char[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_021() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MAX_VALUE, len=Integer.MIN_VALUE
        try {
            NumberInput.parseInt(new char[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_022() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=Integer.MIN_VALUE, len=0
        try {
            NumberInput.parseInt(new char[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_023() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MIN_VALUE, len=1
        try {
            NumberInput.parseInt(new char[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_024() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MIN_VALUE, len=-1
        try {
            NumberInput.parseInt(new char[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseInt_pairwise_025() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MIN_VALUE, len=Integer.MAX_VALUE
        try {
            NumberInput.parseInt(new char[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_026() throws Exception {
        // Combination: digitChars=new char[] {}, offset=0, len=0
        try {
            NumberInput.parseLong(new char[] {}, 0, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_027() throws Exception {
        // Combination: digitChars=new char[] {}, offset=1, len=1
        try {
            NumberInput.parseLong(new char[] {}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_028() throws Exception {
        // Combination: digitChars=new char[] {}, offset=-1, len=-1
        try {
            NumberInput.parseLong(new char[] {}, -1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_029() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MAX_VALUE, len=Integer.MAX_VALUE
        try {
            NumberInput.parseLong(new char[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_030() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MIN_VALUE, len=Integer.MIN_VALUE
        try {
            NumberInput.parseLong(new char[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_031() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=1, len=0
        try {
            NumberInput.parseLong(new char[] {1}, 1, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_032() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=0, len=1
        try {
            NumberInput.parseLong(new char[] {1}, 0, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_033() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=Integer.MAX_VALUE, len=-1
        try {
            NumberInput.parseLong(new char[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_034() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=-1, len=Integer.MAX_VALUE
        try {
            NumberInput.parseLong(new char[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_035() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=0, len=Integer.MIN_VALUE
        try {
            NumberInput.parseLong(new char[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_036() throws Exception {
        // Combination: digitChars=new char[] {}, offset=0, len=-1
        try {
            NumberInput.parseLong(new char[] {}, 0, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_037() throws Exception {
        // Combination: digitChars=new char[] {}, offset=0, len=Integer.MAX_VALUE
        try {
            NumberInput.parseLong(new char[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_038() throws Exception {
        // Combination: digitChars=new char[] {}, offset=1, len=-1
        try {
            NumberInput.parseLong(new char[] {}, 1, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_039() throws Exception {
        // Combination: digitChars=new char[] {}, offset=1, len=Integer.MAX_VALUE
        try {
            NumberInput.parseLong(new char[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_040() throws Exception {
        // Combination: digitChars=new char[] {}, offset=1, len=Integer.MIN_VALUE
        try {
            NumberInput.parseLong(new char[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_041() throws Exception {
        // Combination: digitChars=new char[] {}, offset=-1, len=0
        try {
            NumberInput.parseLong(new char[] {}, -1, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_042() throws Exception {
        // Combination: digitChars=new char[] {}, offset=-1, len=1
        try {
            NumberInput.parseLong(new char[] {}, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_043() throws Exception {
        // Combination: digitChars=new char[] {}, offset=-1, len=Integer.MIN_VALUE
        try {
            NumberInput.parseLong(new char[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_044() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MAX_VALUE, len=0
        try {
            NumberInput.parseLong(new char[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_045() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MAX_VALUE, len=1
        try {
            NumberInput.parseLong(new char[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_046() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MAX_VALUE, len=Integer.MIN_VALUE
        try {
            NumberInput.parseLong(new char[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_047() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=Integer.MIN_VALUE, len=0
        try {
            NumberInput.parseLong(new char[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_048() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MIN_VALUE, len=1
        try {
            NumberInput.parseLong(new char[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_049() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MIN_VALUE, len=-1
        try {
            NumberInput.parseLong(new char[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseLong_pairwise_050() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MIN_VALUE, len=Integer.MAX_VALUE
        try {
            NumberInput.parseLong(new char[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_051() throws Exception {
        // Combination: digitChars=new char[] {}, offset=0, len=0, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, 0, 0, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_052() throws Exception {
        // Combination: digitChars=new char[] {}, offset=1, len=1, negative=false
        Object actual = NumberInput.inLongRange(new char[] {}, 1, 1, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_053() throws Exception {
        // Combination: digitChars=new char[] {}, offset=-1, len=-1, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, -1, -1, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_054() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MAX_VALUE, len=Integer.MAX_VALUE, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_055() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MIN_VALUE, len=Integer.MIN_VALUE, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_056() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=-1, len=0, negative=false
        Object actual = NumberInput.inLongRange(new char[] {1}, -1, 0, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_057() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=0, len=1, negative=true
        Object actual = NumberInput.inLongRange(new char[] {1}, 0, 1, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_058() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=Integer.MAX_VALUE, len=-1, negative=false
        Object actual = NumberInput.inLongRange(new char[] {1}, Integer.MAX_VALUE, -1, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_059() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=Integer.MIN_VALUE, len=Integer.MAX_VALUE, negative=false
        Object actual = NumberInput.inLongRange(new char[] {1}, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_060() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=0, len=Integer.MIN_VALUE, negative=false
        Object actual = NumberInput.inLongRange(new char[] {1}, 0, Integer.MIN_VALUE, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_061() throws Exception {
        // Combination: digitChars=new char[] {}, offset=0, len=-1, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, 0, -1, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_062() throws Exception {
        // Combination: digitChars=new char[] {}, offset=0, len=Integer.MAX_VALUE, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, 0, Integer.MAX_VALUE, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_063() throws Exception {
        // Combination: digitChars=new char[] {1}, offset=1, len=0, negative=true
        Object actual = NumberInput.inLongRange(new char[] {1}, 1, 0, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_064() throws Exception {
        // Combination: digitChars=new char[] {}, offset=1, len=-1, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, 1, -1, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_065() throws Exception {
        // Combination: digitChars=new char[] {}, offset=1, len=Integer.MAX_VALUE, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, 1, Integer.MAX_VALUE, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_066() throws Exception {
        // Combination: digitChars=new char[] {}, offset=1, len=Integer.MIN_VALUE, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, 1, Integer.MIN_VALUE, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_067() throws Exception {
        // Combination: digitChars=new char[] {}, offset=-1, len=1, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, -1, 1, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_068() throws Exception {
        // Combination: digitChars=new char[] {}, offset=-1, len=Integer.MAX_VALUE, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, -1, Integer.MAX_VALUE, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_069() throws Exception {
        // Combination: digitChars=new char[] {}, offset=-1, len=Integer.MIN_VALUE, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, -1, Integer.MIN_VALUE, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_070() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MAX_VALUE, len=0, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, Integer.MAX_VALUE, 0, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_071() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MAX_VALUE, len=1, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, Integer.MAX_VALUE, 1, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_072() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MAX_VALUE, len=Integer.MIN_VALUE, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_073() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MIN_VALUE, len=0, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, Integer.MIN_VALUE, 0, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_074() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MIN_VALUE, len=1, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, Integer.MIN_VALUE, 1, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_075() throws Exception {
        // Combination: digitChars=new char[] {}, offset=Integer.MIN_VALUE, len=-1, negative=true
        Object actual = NumberInput.inLongRange(new char[] {}, Integer.MIN_VALUE, -1, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_076() throws Exception {
        // Combination: numberStr="", negative=true
        Object actual = NumberInput.inLongRange("", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_077() throws Exception {
        // Combination: numberStr=" ", negative=true
        Object actual = NumberInput.inLongRange(" ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_078() throws Exception {
        // Combination: numberStr="a", negative=true
        Object actual = NumberInput.inLongRange("a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_079() throws Exception {
        // Combination: numberStr="test123", negative=true
        Object actual = NumberInput.inLongRange("test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_080() throws Exception {
        // Combination: numberStr="!@#", negative=true
        Object actual = NumberInput.inLongRange("!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_081() throws Exception {
        // Combination: numberStr="0", negative=true
        Object actual = NumberInput.inLongRange("0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_082() throws Exception {
        // Combination: numberStr="-1", negative=true
        Object actual = NumberInput.inLongRange("-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_083() throws Exception {
        // Combination: numberStr="1.5", negative=true
        Object actual = NumberInput.inLongRange("1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_084() throws Exception {
        // Combination: numberStr="9223372036854775807", negative=true
        Object actual = NumberInput.inLongRange("9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_085() throws Exception {
        // Combination: numberStr="9223372036854775808", negative=true
        Object actual = NumberInput.inLongRange("9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_086() throws Exception {
        // Combination: numberStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", negative=true
        Object actual = NumberInput.inLongRange("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_087() throws Exception {
        // Combination: numberStr="", negative=false
        Object actual = NumberInput.inLongRange("", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_088() throws Exception {
        // Combination: numberStr=" ", negative=false
        Object actual = NumberInput.inLongRange(" ", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_089() throws Exception {
        // Combination: numberStr="a", negative=false
        Object actual = NumberInput.inLongRange("a", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_090() throws Exception {
        // Combination: numberStr="test123", negative=false
        Object actual = NumberInput.inLongRange("test123", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_091() throws Exception {
        // Combination: numberStr="!@#", negative=false
        Object actual = NumberInput.inLongRange("!@#", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_092() throws Exception {
        // Combination: numberStr="0", negative=false
        Object actual = NumberInput.inLongRange("0", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_093() throws Exception {
        // Combination: numberStr="-1", negative=false
        Object actual = NumberInput.inLongRange("-1", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_094() throws Exception {
        // Combination: numberStr="1.5", negative=false
        Object actual = NumberInput.inLongRange("1.5", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_095() throws Exception {
        // Combination: numberStr="9223372036854775807", negative=false
        Object actual = NumberInput.inLongRange("9223372036854775807", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_096() throws Exception {
        // Combination: numberStr="9223372036854775808", negative=false
        Object actual = NumberInput.inLongRange("9223372036854775808", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_097() throws Exception {
        // Combination: numberStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", negative=false
        Object actual = NumberInput.inLongRange("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_098() throws Exception {
        // Combination: input="", defaultValue=0
        Object actual = NumberInput.parseAsInt("", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_099() throws Exception {
        // Combination: input=" ", defaultValue=0
        Object actual = NumberInput.parseAsInt(" ", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_100() throws Exception {
        // Combination: input="a", defaultValue=0
        Object actual = NumberInput.parseAsInt("a", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_101() throws Exception {
        // Combination: input="test123", defaultValue=0
        Object actual = NumberInput.parseAsInt("test123", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_102() throws Exception {
        // Combination: input="!@#", defaultValue=0
        Object actual = NumberInput.parseAsInt("!@#", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_103() throws Exception {
        // Combination: input="0", defaultValue=0
        Object actual = NumberInput.parseAsInt("0", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_104() throws Exception {
        // Combination: input="-1", defaultValue=0
        Object actual = NumberInput.parseAsInt("-1", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_105() throws Exception {
        // Combination: input="1.5", defaultValue=0
        Object actual = NumberInput.parseAsInt("1.5", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_106() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=0
        Object actual = NumberInput.parseAsInt("9223372036854775807", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_107() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=0
        Object actual = NumberInput.parseAsInt("9223372036854775808", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_108() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0
        Object actual = NumberInput.parseAsInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_109() throws Exception {
        // Combination: input="", defaultValue=1
        Object actual = NumberInput.parseAsInt("", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_110() throws Exception {
        // Combination: input=" ", defaultValue=1
        Object actual = NumberInput.parseAsInt(" ", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_111() throws Exception {
        // Combination: input="a", defaultValue=1
        Object actual = NumberInput.parseAsInt("a", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_112() throws Exception {
        // Combination: input="test123", defaultValue=1
        Object actual = NumberInput.parseAsInt("test123", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_113() throws Exception {
        // Combination: input="!@#", defaultValue=1
        Object actual = NumberInput.parseAsInt("!@#", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_114() throws Exception {
        // Combination: input="0", defaultValue=1
        Object actual = NumberInput.parseAsInt("0", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_115() throws Exception {
        // Combination: input="-1", defaultValue=1
        Object actual = NumberInput.parseAsInt("-1", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_116() throws Exception {
        // Combination: input="1.5", defaultValue=1
        Object actual = NumberInput.parseAsInt("1.5", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_117() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=1
        Object actual = NumberInput.parseAsInt("9223372036854775807", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_118() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=1
        Object actual = NumberInput.parseAsInt("9223372036854775808", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_119() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1
        Object actual = NumberInput.parseAsInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_120() throws Exception {
        // Combination: input="", defaultValue=-1
        Object actual = NumberInput.parseAsInt("", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_121() throws Exception {
        // Combination: input=" ", defaultValue=-1
        Object actual = NumberInput.parseAsInt(" ", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_122() throws Exception {
        // Combination: input="a", defaultValue=-1
        Object actual = NumberInput.parseAsInt("a", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_123() throws Exception {
        // Combination: input="test123", defaultValue=-1
        Object actual = NumberInput.parseAsInt("test123", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_124() throws Exception {
        // Combination: input="!@#", defaultValue=-1
        Object actual = NumberInput.parseAsInt("!@#", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_125() throws Exception {
        // Combination: input="0", defaultValue=-1
        Object actual = NumberInput.parseAsInt("0", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_126() throws Exception {
        // Combination: input="-1", defaultValue=-1
        Object actual = NumberInput.parseAsInt("-1", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_127() throws Exception {
        // Combination: input="1.5", defaultValue=-1
        Object actual = NumberInput.parseAsInt("1.5", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_128() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=-1
        Object actual = NumberInput.parseAsInt("9223372036854775807", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_129() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=-1
        Object actual = NumberInput.parseAsInt("9223372036854775808", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_130() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1
        Object actual = NumberInput.parseAsInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_131() throws Exception {
        // Combination: input="", defaultValue=Integer.MAX_VALUE
        Object actual = NumberInput.parseAsInt("", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_132() throws Exception {
        // Combination: input=" ", defaultValue=Integer.MAX_VALUE
        Object actual = NumberInput.parseAsInt(" ", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_133() throws Exception {
        // Combination: input="a", defaultValue=Integer.MAX_VALUE
        Object actual = NumberInput.parseAsInt("a", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_134() throws Exception {
        // Combination: input="test123", defaultValue=Integer.MAX_VALUE
        Object actual = NumberInput.parseAsInt("test123", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_135() throws Exception {
        // Combination: input="!@#", defaultValue=Integer.MAX_VALUE
        Object actual = NumberInput.parseAsInt("!@#", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_136() throws Exception {
        // Combination: input="0", defaultValue=Integer.MAX_VALUE
        Object actual = NumberInput.parseAsInt("0", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_137() throws Exception {
        // Combination: input="-1", defaultValue=Integer.MAX_VALUE
        Object actual = NumberInput.parseAsInt("-1", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_138() throws Exception {
        // Combination: input="1.5", defaultValue=Integer.MAX_VALUE
        Object actual = NumberInput.parseAsInt("1.5", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_139() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=Integer.MAX_VALUE
        Object actual = NumberInput.parseAsInt("9223372036854775807", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_140() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=Integer.MAX_VALUE
        Object actual = NumberInput.parseAsInt("9223372036854775808", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_141() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Integer.MAX_VALUE
        Object actual = NumberInput.parseAsInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_142() throws Exception {
        // Combination: input="", defaultValue=Integer.MIN_VALUE
        Object actual = NumberInput.parseAsInt("", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_143() throws Exception {
        // Combination: input=" ", defaultValue=Integer.MIN_VALUE
        Object actual = NumberInput.parseAsInt(" ", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_144() throws Exception {
        // Combination: input="a", defaultValue=Integer.MIN_VALUE
        Object actual = NumberInput.parseAsInt("a", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_145() throws Exception {
        // Combination: input="test123", defaultValue=Integer.MIN_VALUE
        Object actual = NumberInput.parseAsInt("test123", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_146() throws Exception {
        // Combination: input="!@#", defaultValue=Integer.MIN_VALUE
        Object actual = NumberInput.parseAsInt("!@#", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_147() throws Exception {
        // Combination: input="0", defaultValue=Integer.MIN_VALUE
        Object actual = NumberInput.parseAsInt("0", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_148() throws Exception {
        // Combination: input="-1", defaultValue=Integer.MIN_VALUE
        Object actual = NumberInput.parseAsInt("-1", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_149() throws Exception {
        // Combination: input="1.5", defaultValue=Integer.MIN_VALUE
        Object actual = NumberInput.parseAsInt("1.5", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_150() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=Integer.MIN_VALUE
        Object actual = NumberInput.parseAsInt("9223372036854775807", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_151() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=Integer.MIN_VALUE
        Object actual = NumberInput.parseAsInt("9223372036854775808", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsInt_pairwise_152() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Integer.MIN_VALUE
        Object actual = NumberInput.parseAsInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_153() throws Exception {
        // Combination: input="", defaultValue=0L
        Object actual = NumberInput.parseAsLong("", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_154() throws Exception {
        // Combination: input=" ", defaultValue=0L
        Object actual = NumberInput.parseAsLong(" ", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_155() throws Exception {
        // Combination: input="a", defaultValue=0L
        Object actual = NumberInput.parseAsLong("a", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_156() throws Exception {
        // Combination: input="test123", defaultValue=0L
        Object actual = NumberInput.parseAsLong("test123", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_157() throws Exception {
        // Combination: input="!@#", defaultValue=0L
        Object actual = NumberInput.parseAsLong("!@#", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_158() throws Exception {
        // Combination: input="0", defaultValue=0L
        Object actual = NumberInput.parseAsLong("0", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_159() throws Exception {
        // Combination: input="-1", defaultValue=0L
        Object actual = NumberInput.parseAsLong("-1", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_160() throws Exception {
        // Combination: input="1.5", defaultValue=0L
        Object actual = NumberInput.parseAsLong("1.5", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_161() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=0L
        Object actual = NumberInput.parseAsLong("9223372036854775807", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_162() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=0L
        Object actual = NumberInput.parseAsLong("9223372036854775808", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_163() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0L
        Object actual = NumberInput.parseAsLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_164() throws Exception {
        // Combination: input="", defaultValue=1L
        Object actual = NumberInput.parseAsLong("", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_165() throws Exception {
        // Combination: input=" ", defaultValue=1L
        Object actual = NumberInput.parseAsLong(" ", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_166() throws Exception {
        // Combination: input="a", defaultValue=1L
        Object actual = NumberInput.parseAsLong("a", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_167() throws Exception {
        // Combination: input="test123", defaultValue=1L
        Object actual = NumberInput.parseAsLong("test123", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_168() throws Exception {
        // Combination: input="!@#", defaultValue=1L
        Object actual = NumberInput.parseAsLong("!@#", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_169() throws Exception {
        // Combination: input="0", defaultValue=1L
        Object actual = NumberInput.parseAsLong("0", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_170() throws Exception {
        // Combination: input="-1", defaultValue=1L
        Object actual = NumberInput.parseAsLong("-1", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_171() throws Exception {
        // Combination: input="1.5", defaultValue=1L
        Object actual = NumberInput.parseAsLong("1.5", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_172() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=1L
        Object actual = NumberInput.parseAsLong("9223372036854775807", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_173() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=1L
        Object actual = NumberInput.parseAsLong("9223372036854775808", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_174() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1L
        Object actual = NumberInput.parseAsLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_175() throws Exception {
        // Combination: input="", defaultValue=-1L
        Object actual = NumberInput.parseAsLong("", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_176() throws Exception {
        // Combination: input=" ", defaultValue=-1L
        Object actual = NumberInput.parseAsLong(" ", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_177() throws Exception {
        // Combination: input="a", defaultValue=-1L
        Object actual = NumberInput.parseAsLong("a", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_178() throws Exception {
        // Combination: input="test123", defaultValue=-1L
        Object actual = NumberInput.parseAsLong("test123", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_179() throws Exception {
        // Combination: input="!@#", defaultValue=-1L
        Object actual = NumberInput.parseAsLong("!@#", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_180() throws Exception {
        // Combination: input="0", defaultValue=-1L
        Object actual = NumberInput.parseAsLong("0", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_181() throws Exception {
        // Combination: input="-1", defaultValue=-1L
        Object actual = NumberInput.parseAsLong("-1", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_182() throws Exception {
        // Combination: input="1.5", defaultValue=-1L
        Object actual = NumberInput.parseAsLong("1.5", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_183() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=-1L
        Object actual = NumberInput.parseAsLong("9223372036854775807", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_184() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=-1L
        Object actual = NumberInput.parseAsLong("9223372036854775808", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_185() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1L
        Object actual = NumberInput.parseAsLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_186() throws Exception {
        // Combination: input="", defaultValue=Long.MAX_VALUE
        Object actual = NumberInput.parseAsLong("", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_187() throws Exception {
        // Combination: input=" ", defaultValue=Long.MAX_VALUE
        Object actual = NumberInput.parseAsLong(" ", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_188() throws Exception {
        // Combination: input="a", defaultValue=Long.MAX_VALUE
        Object actual = NumberInput.parseAsLong("a", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_189() throws Exception {
        // Combination: input="test123", defaultValue=Long.MAX_VALUE
        Object actual = NumberInput.parseAsLong("test123", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_190() throws Exception {
        // Combination: input="!@#", defaultValue=Long.MAX_VALUE
        Object actual = NumberInput.parseAsLong("!@#", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_191() throws Exception {
        // Combination: input="0", defaultValue=Long.MAX_VALUE
        Object actual = NumberInput.parseAsLong("0", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_192() throws Exception {
        // Combination: input="-1", defaultValue=Long.MAX_VALUE
        Object actual = NumberInput.parseAsLong("-1", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_193() throws Exception {
        // Combination: input="1.5", defaultValue=Long.MAX_VALUE
        Object actual = NumberInput.parseAsLong("1.5", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_194() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=Long.MAX_VALUE
        Object actual = NumberInput.parseAsLong("9223372036854775807", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_195() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=Long.MAX_VALUE
        Object actual = NumberInput.parseAsLong("9223372036854775808", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_196() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Long.MAX_VALUE
        Object actual = NumberInput.parseAsLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_197() throws Exception {
        // Combination: input="", defaultValue=Long.MIN_VALUE
        Object actual = NumberInput.parseAsLong("", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_198() throws Exception {
        // Combination: input=" ", defaultValue=Long.MIN_VALUE
        Object actual = NumberInput.parseAsLong(" ", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_199() throws Exception {
        // Combination: input="a", defaultValue=Long.MIN_VALUE
        Object actual = NumberInput.parseAsLong("a", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_200() throws Exception {
        // Combination: input="test123", defaultValue=Long.MIN_VALUE
        Object actual = NumberInput.parseAsLong("test123", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_201() throws Exception {
        // Combination: input="!@#", defaultValue=Long.MIN_VALUE
        Object actual = NumberInput.parseAsLong("!@#", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_202() throws Exception {
        // Combination: input="0", defaultValue=Long.MIN_VALUE
        Object actual = NumberInput.parseAsLong("0", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_203() throws Exception {
        // Combination: input="-1", defaultValue=Long.MIN_VALUE
        Object actual = NumberInput.parseAsLong("-1", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_204() throws Exception {
        // Combination: input="1.5", defaultValue=Long.MIN_VALUE
        Object actual = NumberInput.parseAsLong("1.5", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_205() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=Long.MIN_VALUE
        Object actual = NumberInput.parseAsLong("9223372036854775807", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_206() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=Long.MIN_VALUE
        Object actual = NumberInput.parseAsLong("9223372036854775808", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsLong_pairwise_207() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Long.MIN_VALUE
        Object actual = NumberInput.parseAsLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_208() throws Exception {
        // Combination: input="", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_209() throws Exception {
        // Combination: input=" ", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble(" ", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_210() throws Exception {
        // Combination: input="a", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("a", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_211() throws Exception {
        // Combination: input="test123", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("test123", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_212() throws Exception {
        // Combination: input="!@#", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("!@#", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_213() throws Exception {
        // Combination: input="0", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("0", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_214() throws Exception {
        // Combination: input="-1", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("-1", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_215() throws Exception {
        // Combination: input="1.5", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("1.5", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_216() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("9223372036854775807", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_217() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("9223372036854775808", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_218() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_219() throws Exception {
        // Combination: input="", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_220() throws Exception {
        // Combination: input=" ", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble(" ", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_221() throws Exception {
        // Combination: input="a", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("a", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_222() throws Exception {
        // Combination: input="test123", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("test123", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_223() throws Exception {
        // Combination: input="!@#", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("!@#", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_224() throws Exception {
        // Combination: input="0", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("0", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_225() throws Exception {
        // Combination: input="-1", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("-1", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_226() throws Exception {
        // Combination: input="1.5", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("1.5", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_227() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("9223372036854775807", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_228() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("9223372036854775808", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_229() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_230() throws Exception {
        // Combination: input="", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_231() throws Exception {
        // Combination: input=" ", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble(" ", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_232() throws Exception {
        // Combination: input="a", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("a", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_233() throws Exception {
        // Combination: input="test123", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("test123", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_234() throws Exception {
        // Combination: input="!@#", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("!@#", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_235() throws Exception {
        // Combination: input="0", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("0", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_236() throws Exception {
        // Combination: input="-1", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("-1", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_237() throws Exception {
        // Combination: input="1.5", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("1.5", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_238() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("9223372036854775807", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_239() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("9223372036854775808", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_240() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_241() throws Exception {
        // Combination: input="", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_242() throws Exception {
        // Combination: input=" ", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble(" ", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_243() throws Exception {
        // Combination: input="a", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("a", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_244() throws Exception {
        // Combination: input="test123", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("test123", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_245() throws Exception {
        // Combination: input="!@#", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("!@#", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_246() throws Exception {
        // Combination: input="0", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("0", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_247() throws Exception {
        // Combination: input="-1", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("-1", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_248() throws Exception {
        // Combination: input="1.5", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("1.5", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_249() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("9223372036854775807", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_250() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("9223372036854775808", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_251() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_252() throws Exception {
        // Combination: input="", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_253() throws Exception {
        // Combination: input=" ", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble(" ", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_254() throws Exception {
        // Combination: input="a", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("a", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_255() throws Exception {
        // Combination: input="test123", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("test123", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_256() throws Exception {
        // Combination: input="!@#", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("!@#", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_257() throws Exception {
        // Combination: input="0", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("0", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_258() throws Exception {
        // Combination: input="-1", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("-1", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_259() throws Exception {
        // Combination: input="1.5", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("1.5", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_260() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("9223372036854775807", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_261() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("9223372036854775808", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_262() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_263() throws Exception {
        // Combination: buffer=new char[] {}, offset=0, len=0
        try {
            NumberInput.parseBigDecimal(new char[] {}, 0, 0);
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_264() throws Exception {
        // Combination: buffer=new char[] {}, offset=1, len=1
        try {
            NumberInput.parseBigDecimal(new char[] {}, 1, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_265() throws Exception {
        // Combination: buffer=new char[] {}, offset=-1, len=-1
        try {
            NumberInput.parseBigDecimal(new char[] {}, -1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_266() throws Exception {
        // Combination: buffer=new char[] {}, offset=Integer.MAX_VALUE, len=Integer.MAX_VALUE
        try {
            NumberInput.parseBigDecimal(new char[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_267() throws Exception {
        // Combination: buffer=new char[] {}, offset=Integer.MIN_VALUE, len=Integer.MIN_VALUE
        try {
            NumberInput.parseBigDecimal(new char[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_268() throws Exception {
        // Combination: buffer=new char[] {1}, offset=1, len=0
        try {
            NumberInput.parseBigDecimal(new char[] {1}, 1, 0);
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_269() throws Exception {
        // Combination: buffer=new char[] {1}, offset=0, len=1
        try {
            NumberInput.parseBigDecimal(new char[] {1}, 0, 1);
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_270() throws Exception {
        // Combination: buffer=new char[] {1}, offset=Integer.MAX_VALUE, len=-1
        try {
            NumberInput.parseBigDecimal(new char[] {1}, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_271() throws Exception {
        // Combination: buffer=new char[] {1}, offset=-1, len=Integer.MAX_VALUE
        try {
            NumberInput.parseBigDecimal(new char[] {1}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_272() throws Exception {
        // Combination: buffer=new char[] {1}, offset=0, len=Integer.MIN_VALUE
        try {
            NumberInput.parseBigDecimal(new char[] {1}, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_273() throws Exception {
        // Combination: buffer=new char[] {}, offset=0, len=-1
        try {
            NumberInput.parseBigDecimal(new char[] {}, 0, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_274() throws Exception {
        // Combination: buffer=new char[] {}, offset=0, len=Integer.MAX_VALUE
        try {
            NumberInput.parseBigDecimal(new char[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_275() throws Exception {
        // Combination: buffer=new char[] {}, offset=1, len=-1
        try {
            NumberInput.parseBigDecimal(new char[] {}, 1, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_276() throws Exception {
        // Combination: buffer=new char[] {}, offset=1, len=Integer.MAX_VALUE
        try {
            NumberInput.parseBigDecimal(new char[] {}, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_277() throws Exception {
        // Combination: buffer=new char[] {}, offset=1, len=Integer.MIN_VALUE
        try {
            NumberInput.parseBigDecimal(new char[] {}, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_278() throws Exception {
        // Combination: buffer=new char[] {}, offset=-1, len=0
        try {
            NumberInput.parseBigDecimal(new char[] {}, -1, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_279() throws Exception {
        // Combination: buffer=new char[] {}, offset=-1, len=1
        try {
            NumberInput.parseBigDecimal(new char[] {}, -1, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_280() throws Exception {
        // Combination: buffer=new char[] {}, offset=-1, len=Integer.MIN_VALUE
        try {
            NumberInput.parseBigDecimal(new char[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_281() throws Exception {
        // Combination: buffer=new char[] {}, offset=Integer.MAX_VALUE, len=0
        try {
            NumberInput.parseBigDecimal(new char[] {}, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_282() throws Exception {
        // Combination: buffer=new char[] {}, offset=Integer.MAX_VALUE, len=1
        try {
            NumberInput.parseBigDecimal(new char[] {}, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_283() throws Exception {
        // Combination: buffer=new char[] {}, offset=Integer.MAX_VALUE, len=Integer.MIN_VALUE
        try {
            NumberInput.parseBigDecimal(new char[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_284() throws Exception {
        // Combination: buffer=new char[] {1}, offset=Integer.MIN_VALUE, len=0
        try {
            NumberInput.parseBigDecimal(new char[] {1}, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_285() throws Exception {
        // Combination: buffer=new char[] {}, offset=Integer.MIN_VALUE, len=1
        try {
            NumberInput.parseBigDecimal(new char[] {}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_286() throws Exception {
        // Combination: buffer=new char[] {}, offset=Integer.MIN_VALUE, len=-1
        try {
            NumberInput.parseBigDecimal(new char[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBigDecimal_pairwise_287() throws Exception {
        // Combination: buffer=new char[] {}, offset=Integer.MIN_VALUE, len=Integer.MAX_VALUE
        try {
            NumberInput.parseBigDecimal(new char[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
