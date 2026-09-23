package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Fraction.
 */
public class Fraction_IPOTest {
    @Test(timeout = 4000)
    public void test_getFraction_pairwise_001() throws Exception {
        // Combination: numerator=0, denominator=0
        try {
            Fraction.getFraction(0, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_002() throws Exception {
        // Combination: numerator=1, denominator=0
        try {
            Fraction.getFraction(1, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_003() throws Exception {
        // Combination: numerator=-1, denominator=0
        try {
            Fraction.getFraction(-1, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_004() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=0
        try {
            Fraction.getFraction(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_005() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=0
        try {
            Fraction.getFraction(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_006() throws Exception {
        // Combination: numerator=0, denominator=1
        Object actual = Fraction.getFraction(0, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("0/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_007() throws Exception {
        // Combination: numerator=1, denominator=1
        Object actual = Fraction.getFraction(1, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("1/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_008() throws Exception {
        // Combination: numerator=-1, denominator=1
        Object actual = Fraction.getFraction(-1, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-1/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_009() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=1
        Object actual = Fraction.getFraction(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("2147483647/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_010() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=1
        Object actual = Fraction.getFraction(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-2147483648/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_011() throws Exception {
        // Combination: numerator=0, denominator=-1
        Object actual = Fraction.getFraction(0, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("0/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_012() throws Exception {
        // Combination: numerator=1, denominator=-1
        Object actual = Fraction.getFraction(1, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-1/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_013() throws Exception {
        // Combination: numerator=-1, denominator=-1
        Object actual = Fraction.getFraction(-1, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("1/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_014() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=-1
        Object actual = Fraction.getFraction(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-2147483647/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_015() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=-1
        try {
            Fraction.getFraction(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_016() throws Exception {
        // Combination: numerator=0, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getFraction(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("0/2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_017() throws Exception {
        // Combination: numerator=1, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getFraction(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("1/2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_018() throws Exception {
        // Combination: numerator=-1, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getFraction(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-1/2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_019() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getFraction(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("2147483647/2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_020() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-2147483648/2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_021() throws Exception {
        // Combination: numerator=0, denominator=Integer.MIN_VALUE
        try {
            Fraction.getFraction(0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_022() throws Exception {
        // Combination: numerator=1, denominator=Integer.MIN_VALUE
        try {
            Fraction.getFraction(1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_023() throws Exception {
        // Combination: numerator=-1, denominator=Integer.MIN_VALUE
        try {
            Fraction.getFraction(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_024() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=Integer.MIN_VALUE
        try {
            Fraction.getFraction(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_025() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=Integer.MIN_VALUE
        try {
            Fraction.getFraction(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_026() throws Exception {
        // Combination: whole=0, numerator=0, denominator=0
        try {
            Fraction.getFraction(0, 0, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_027() throws Exception {
        // Combination: whole=1, numerator=1, denominator=0
        try {
            Fraction.getFraction(1, 1, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_028() throws Exception {
        // Combination: whole=-1, numerator=-1, denominator=0
        try {
            Fraction.getFraction(-1, -1, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_029() throws Exception {
        // Combination: whole=Integer.MAX_VALUE, numerator=Integer.MAX_VALUE, denominator=0
        try {
            Fraction.getFraction(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_030() throws Exception {
        // Combination: whole=Integer.MIN_VALUE, numerator=Integer.MIN_VALUE, denominator=0
        try {
            Fraction.getFraction(Integer.MIN_VALUE, Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_031() throws Exception {
        // Combination: whole=1, numerator=0, denominator=1
        Object actual = Fraction.getFraction(1, 0, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("1/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_032() throws Exception {
        // Combination: whole=0, numerator=1, denominator=1
        Object actual = Fraction.getFraction(0, 1, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("1/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_033() throws Exception {
        // Combination: whole=Integer.MAX_VALUE, numerator=-1, denominator=1
        try {
            Fraction.getFraction(Integer.MAX_VALUE, -1, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_034() throws Exception {
        // Combination: whole=-1, numerator=Integer.MAX_VALUE, denominator=1
        Object actual = Fraction.getFraction(-1, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-2147483648/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_035() throws Exception {
        // Combination: whole=0, numerator=Integer.MIN_VALUE, denominator=1
        try {
            Fraction.getFraction(0, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_036() throws Exception {
        // Combination: whole=-1, numerator=0, denominator=-1
        try {
            Fraction.getFraction(-1, 0, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_037() throws Exception {
        // Combination: whole=Integer.MAX_VALUE, numerator=1, denominator=-1
        try {
            Fraction.getFraction(Integer.MAX_VALUE, 1, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_038() throws Exception {
        // Combination: whole=0, numerator=-1, denominator=-1
        try {
            Fraction.getFraction(0, -1, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_039() throws Exception {
        // Combination: whole=1, numerator=Integer.MAX_VALUE, denominator=-1
        try {
            Fraction.getFraction(1, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_040() throws Exception {
        // Combination: whole=1, numerator=Integer.MIN_VALUE, denominator=-1
        try {
            Fraction.getFraction(1, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_041() throws Exception {
        // Combination: whole=Integer.MAX_VALUE, numerator=0, denominator=Integer.MAX_VALUE
        try {
            Fraction.getFraction(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_042() throws Exception {
        // Combination: whole=-1, numerator=1, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getFraction(-1, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-2147483648/2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_043() throws Exception {
        // Combination: whole=1, numerator=-1, denominator=Integer.MAX_VALUE
        try {
            Fraction.getFraction(1, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_044() throws Exception {
        // Combination: whole=0, numerator=Integer.MAX_VALUE, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getFraction(0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("2147483647/2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_045() throws Exception {
        // Combination: whole=-1, numerator=Integer.MIN_VALUE, denominator=Integer.MAX_VALUE
        try {
            Fraction.getFraction(-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_046() throws Exception {
        // Combination: whole=Integer.MIN_VALUE, numerator=0, denominator=Integer.MIN_VALUE
        try {
            Fraction.getFraction(Integer.MIN_VALUE, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_047() throws Exception {
        // Combination: whole=0, numerator=1, denominator=Integer.MIN_VALUE
        try {
            Fraction.getFraction(0, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_048() throws Exception {
        // Combination: whole=1, numerator=-1, denominator=Integer.MIN_VALUE
        try {
            Fraction.getFraction(1, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_049() throws Exception {
        // Combination: whole=-1, numerator=Integer.MAX_VALUE, denominator=Integer.MIN_VALUE
        try {
            Fraction.getFraction(-1, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_050() throws Exception {
        // Combination: whole=Integer.MAX_VALUE, numerator=Integer.MIN_VALUE, denominator=Integer.MIN_VALUE
        try {
            Fraction.getFraction(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_051() throws Exception {
        // Combination: whole=Integer.MIN_VALUE, numerator=1, denominator=1
        try {
            Fraction.getFraction(Integer.MIN_VALUE, 1, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_052() throws Exception {
        // Combination: whole=Integer.MIN_VALUE, numerator=-1, denominator=-1
        try {
            Fraction.getFraction(Integer.MIN_VALUE, -1, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getFraction_pairwise_053() throws Exception {
        // Combination: whole=Integer.MIN_VALUE, numerator=Integer.MAX_VALUE, denominator=Integer.MAX_VALUE
        try {
            Fraction.getFraction(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_054() throws Exception {
        // Combination: numerator=0, denominator=0
        try {
            Fraction.getReducedFraction(0, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_055() throws Exception {
        // Combination: numerator=1, denominator=0
        try {
            Fraction.getReducedFraction(1, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_056() throws Exception {
        // Combination: numerator=-1, denominator=0
        try {
            Fraction.getReducedFraction(-1, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_057() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=0
        try {
            Fraction.getReducedFraction(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_058() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=0
        try {
            Fraction.getReducedFraction(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_059() throws Exception {
        // Combination: numerator=0, denominator=1
        Object actual = Fraction.getReducedFraction(0, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("0/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_060() throws Exception {
        // Combination: numerator=1, denominator=1
        Object actual = Fraction.getReducedFraction(1, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("1/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_061() throws Exception {
        // Combination: numerator=-1, denominator=1
        Object actual = Fraction.getReducedFraction(-1, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-1/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_062() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=1
        Object actual = Fraction.getReducedFraction(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("2147483647/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_063() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=1
        Object actual = Fraction.getReducedFraction(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-2147483648/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_064() throws Exception {
        // Combination: numerator=0, denominator=-1
        Object actual = Fraction.getReducedFraction(0, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("0/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_065() throws Exception {
        // Combination: numerator=1, denominator=-1
        Object actual = Fraction.getReducedFraction(1, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-1/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_066() throws Exception {
        // Combination: numerator=-1, denominator=-1
        Object actual = Fraction.getReducedFraction(-1, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("1/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_067() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=-1
        Object actual = Fraction.getReducedFraction(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-2147483647/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_068() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=-1
        try {
            Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_069() throws Exception {
        // Combination: numerator=0, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("0/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_070() throws Exception {
        // Combination: numerator=1, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("1/2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_071() throws Exception {
        // Combination: numerator=-1, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-1/2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_072() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("1/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_073() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("-2147483648/2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_074() throws Exception {
        // Combination: numerator=0, denominator=Integer.MIN_VALUE
        Object actual = Fraction.getReducedFraction(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("0/1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_075() throws Exception {
        // Combination: numerator=1, denominator=Integer.MIN_VALUE
        try {
            Fraction.getReducedFraction(1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_076() throws Exception {
        // Combination: numerator=-1, denominator=Integer.MIN_VALUE
        try {
            Fraction.getReducedFraction(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_077() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=Integer.MIN_VALUE
        try {
            Fraction.getReducedFraction(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_078() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=Integer.MIN_VALUE
        Object actual = Fraction.getReducedFraction(Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.lang3.math.Fraction", actual.getClass().getName());
        assertEquals("1/1", String.valueOf(actual));
    }

}
