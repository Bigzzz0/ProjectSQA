package org.apache.commons.math3.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Fraction.
 */
public class Fraction_IPOTest {
    @Test(timeout = 4000)
    public void test_equals_pairwise_001() throws Exception {
        // Combination: receiver__value=0.0d, other=new Object()
        Object actual = (new Fraction(0.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_002() throws Exception {
        // Combination: receiver__value=1.0d, other=new Object()
        Object actual = (new Fraction(1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_003() throws Exception {
        // Combination: receiver__value=-1.0d, other=new Object()
        Object actual = (new Fraction(-1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_004() throws Exception {
        // Combination: receiver__value=Double.NaN, other=new Object()
        Object actual = (new Fraction(Double.NaN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_005() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, other=new Object()
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).equals(new Object());
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_006() throws Exception {
        // Combination: receiver__value=0.0d, other="sample_str"
        Object actual = (new Fraction(0.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_007() throws Exception {
        // Combination: receiver__value=1.0d, other="sample_str"
        Object actual = (new Fraction(1.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_008() throws Exception {
        // Combination: receiver__value=-1.0d, other="sample_str"
        Object actual = (new Fraction(-1.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_009() throws Exception {
        // Combination: receiver__value=Double.NaN, other="sample_str"
        Object actual = (new Fraction(Double.NaN)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_010() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, other="sample_str"
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).equals("sample_str");
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_011() throws Exception {
        // Combination: receiver__value=0.0d, other=Integer.valueOf(1)
        Object actual = (new Fraction(0.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_012() throws Exception {
        // Combination: receiver__value=1.0d, other=Integer.valueOf(1)
        Object actual = (new Fraction(1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_013() throws Exception {
        // Combination: receiver__value=-1.0d, other=Integer.valueOf(1)
        Object actual = (new Fraction(-1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_014() throws Exception {
        // Combination: receiver__value=Double.NaN, other=Integer.valueOf(1)
        Object actual = (new Fraction(Double.NaN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_015() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, other=Integer.valueOf(1)
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).equals(Integer.valueOf(1));
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_016() throws Exception {
        // Combination: receiver__value=0.0d, i=0
        Object actual = (new Fraction(0.0d)).add(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_017() throws Exception {
        // Combination: receiver__value=1.0d, i=0
        Object actual = (new Fraction(1.0d)).add(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_018() throws Exception {
        // Combination: receiver__value=-1.0d, i=0
        Object actual = (new Fraction(-1.0d)).add(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_019() throws Exception {
        // Combination: receiver__value=Double.NaN, i=0
        try {
            (new Fraction(Double.NaN)).add(0);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_020() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=0
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).add(0);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_021() throws Exception {
        // Combination: receiver__value=0.0d, i=1
        Object actual = (new Fraction(0.0d)).add(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_022() throws Exception {
        // Combination: receiver__value=1.0d, i=1
        Object actual = (new Fraction(1.0d)).add(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_023() throws Exception {
        // Combination: receiver__value=-1.0d, i=1
        Object actual = (new Fraction(-1.0d)).add(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_024() throws Exception {
        // Combination: receiver__value=Double.NaN, i=1
        try {
            (new Fraction(Double.NaN)).add(1);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_025() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=1
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).add(1);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_026() throws Exception {
        // Combination: receiver__value=0.0d, i=-1
        Object actual = (new Fraction(0.0d)).add(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_027() throws Exception {
        // Combination: receiver__value=1.0d, i=-1
        Object actual = (new Fraction(1.0d)).add(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_028() throws Exception {
        // Combination: receiver__value=-1.0d, i=-1
        Object actual = (new Fraction(-1.0d)).add(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_029() throws Exception {
        // Combination: receiver__value=Double.NaN, i=-1
        try {
            (new Fraction(Double.NaN)).add(-1);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_030() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=-1
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).add(-1);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_031() throws Exception {
        // Combination: receiver__value=0.0d, i=Integer.MAX_VALUE
        Object actual = (new Fraction(0.0d)).add(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_032() throws Exception {
        // Combination: receiver__value=1.0d, i=Integer.MAX_VALUE
        Object actual = (new Fraction(1.0d)).add(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_033() throws Exception {
        // Combination: receiver__value=-1.0d, i=Integer.MAX_VALUE
        Object actual = (new Fraction(-1.0d)).add(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("2147483646", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_034() throws Exception {
        // Combination: receiver__value=Double.NaN, i=Integer.MAX_VALUE
        try {
            (new Fraction(Double.NaN)).add(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_035() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=Integer.MAX_VALUE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).add(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_036() throws Exception {
        // Combination: receiver__value=0.0d, i=Integer.MIN_VALUE
        Object actual = (new Fraction(0.0d)).add(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_037() throws Exception {
        // Combination: receiver__value=1.0d, i=Integer.MIN_VALUE
        Object actual = (new Fraction(1.0d)).add(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_038() throws Exception {
        // Combination: receiver__value=-1.0d, i=Integer.MIN_VALUE
        Object actual = (new Fraction(-1.0d)).add(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_039() throws Exception {
        // Combination: receiver__value=Double.NaN, i=Integer.MIN_VALUE
        try {
            (new Fraction(Double.NaN)).add(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_040() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=Integer.MIN_VALUE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).add(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_041() throws Exception {
        // Combination: receiver__value=0.0d, i=0
        Object actual = (new Fraction(0.0d)).subtract(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_042() throws Exception {
        // Combination: receiver__value=1.0d, i=0
        Object actual = (new Fraction(1.0d)).subtract(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_043() throws Exception {
        // Combination: receiver__value=-1.0d, i=0
        Object actual = (new Fraction(-1.0d)).subtract(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_044() throws Exception {
        // Combination: receiver__value=Double.NaN, i=0
        try {
            (new Fraction(Double.NaN)).subtract(0);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_045() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=0
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).subtract(0);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_046() throws Exception {
        // Combination: receiver__value=0.0d, i=1
        Object actual = (new Fraction(0.0d)).subtract(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_047() throws Exception {
        // Combination: receiver__value=1.0d, i=1
        Object actual = (new Fraction(1.0d)).subtract(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_048() throws Exception {
        // Combination: receiver__value=-1.0d, i=1
        Object actual = (new Fraction(-1.0d)).subtract(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_049() throws Exception {
        // Combination: receiver__value=Double.NaN, i=1
        try {
            (new Fraction(Double.NaN)).subtract(1);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_050() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=1
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).subtract(1);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_051() throws Exception {
        // Combination: receiver__value=0.0d, i=-1
        Object actual = (new Fraction(0.0d)).subtract(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_052() throws Exception {
        // Combination: receiver__value=1.0d, i=-1
        Object actual = (new Fraction(1.0d)).subtract(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_053() throws Exception {
        // Combination: receiver__value=-1.0d, i=-1
        Object actual = (new Fraction(-1.0d)).subtract(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_054() throws Exception {
        // Combination: receiver__value=Double.NaN, i=-1
        try {
            (new Fraction(Double.NaN)).subtract(-1);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_055() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=-1
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).subtract(-1);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_056() throws Exception {
        // Combination: receiver__value=0.0d, i=Integer.MAX_VALUE
        Object actual = (new Fraction(0.0d)).subtract(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_057() throws Exception {
        // Combination: receiver__value=1.0d, i=Integer.MAX_VALUE
        Object actual = (new Fraction(1.0d)).subtract(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483646", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_058() throws Exception {
        // Combination: receiver__value=-1.0d, i=Integer.MAX_VALUE
        Object actual = (new Fraction(-1.0d)).subtract(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_059() throws Exception {
        // Combination: receiver__value=Double.NaN, i=Integer.MAX_VALUE
        try {
            (new Fraction(Double.NaN)).subtract(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_060() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=Integer.MAX_VALUE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).subtract(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_061() throws Exception {
        // Combination: receiver__value=0.0d, i=Integer.MIN_VALUE
        Object actual = (new Fraction(0.0d)).subtract(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_062() throws Exception {
        // Combination: receiver__value=1.0d, i=Integer.MIN_VALUE
        Object actual = (new Fraction(1.0d)).subtract(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_063() throws Exception {
        // Combination: receiver__value=-1.0d, i=Integer.MIN_VALUE
        Object actual = (new Fraction(-1.0d)).subtract(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_064() throws Exception {
        // Combination: receiver__value=Double.NaN, i=Integer.MIN_VALUE
        try {
            (new Fraction(Double.NaN)).subtract(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_065() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=Integer.MIN_VALUE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).subtract(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_066() throws Exception {
        // Combination: receiver__value=0.0d, i=0
        Object actual = (new Fraction(0.0d)).multiply(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_067() throws Exception {
        // Combination: receiver__value=1.0d, i=0
        Object actual = (new Fraction(1.0d)).multiply(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_068() throws Exception {
        // Combination: receiver__value=-1.0d, i=0
        Object actual = (new Fraction(-1.0d)).multiply(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_069() throws Exception {
        // Combination: receiver__value=Double.NaN, i=0
        try {
            (new Fraction(Double.NaN)).multiply(0);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_070() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=0
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).multiply(0);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_071() throws Exception {
        // Combination: receiver__value=0.0d, i=1
        Object actual = (new Fraction(0.0d)).multiply(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_072() throws Exception {
        // Combination: receiver__value=1.0d, i=1
        Object actual = (new Fraction(1.0d)).multiply(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_073() throws Exception {
        // Combination: receiver__value=-1.0d, i=1
        Object actual = (new Fraction(-1.0d)).multiply(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_074() throws Exception {
        // Combination: receiver__value=Double.NaN, i=1
        try {
            (new Fraction(Double.NaN)).multiply(1);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_075() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=1
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).multiply(1);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_076() throws Exception {
        // Combination: receiver__value=0.0d, i=-1
        Object actual = (new Fraction(0.0d)).multiply(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_077() throws Exception {
        // Combination: receiver__value=1.0d, i=-1
        Object actual = (new Fraction(1.0d)).multiply(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_078() throws Exception {
        // Combination: receiver__value=-1.0d, i=-1
        Object actual = (new Fraction(-1.0d)).multiply(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_079() throws Exception {
        // Combination: receiver__value=Double.NaN, i=-1
        try {
            (new Fraction(Double.NaN)).multiply(-1);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_080() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=-1
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).multiply(-1);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_081() throws Exception {
        // Combination: receiver__value=0.0d, i=Integer.MAX_VALUE
        Object actual = (new Fraction(0.0d)).multiply(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_082() throws Exception {
        // Combination: receiver__value=1.0d, i=Integer.MAX_VALUE
        Object actual = (new Fraction(1.0d)).multiply(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_083() throws Exception {
        // Combination: receiver__value=-1.0d, i=Integer.MAX_VALUE
        Object actual = (new Fraction(-1.0d)).multiply(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_084() throws Exception {
        // Combination: receiver__value=Double.NaN, i=Integer.MAX_VALUE
        try {
            (new Fraction(Double.NaN)).multiply(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_085() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=Integer.MAX_VALUE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).multiply(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_086() throws Exception {
        // Combination: receiver__value=0.0d, i=Integer.MIN_VALUE
        Object actual = (new Fraction(0.0d)).multiply(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_087() throws Exception {
        // Combination: receiver__value=1.0d, i=Integer.MIN_VALUE
        Object actual = (new Fraction(1.0d)).multiply(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_088() throws Exception {
        // Combination: receiver__value=-1.0d, i=Integer.MIN_VALUE
        Object actual = (new Fraction(-1.0d)).multiply(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_089() throws Exception {
        // Combination: receiver__value=Double.NaN, i=Integer.MIN_VALUE
        try {
            (new Fraction(Double.NaN)).multiply(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_090() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=Integer.MIN_VALUE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).multiply(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_091() throws Exception {
        // Combination: receiver__value=0.0d, i=0
        try {
            (new Fraction(0.0d)).divide(0);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_092() throws Exception {
        // Combination: receiver__value=1.0d, i=0
        try {
            (new Fraction(1.0d)).divide(0);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_093() throws Exception {
        // Combination: receiver__value=-1.0d, i=0
        try {
            (new Fraction(-1.0d)).divide(0);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_094() throws Exception {
        // Combination: receiver__value=Double.NaN, i=0
        try {
            (new Fraction(Double.NaN)).divide(0);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_095() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=0
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).divide(0);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_096() throws Exception {
        // Combination: receiver__value=0.0d, i=1
        Object actual = (new Fraction(0.0d)).divide(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_097() throws Exception {
        // Combination: receiver__value=1.0d, i=1
        Object actual = (new Fraction(1.0d)).divide(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_098() throws Exception {
        // Combination: receiver__value=-1.0d, i=1
        Object actual = (new Fraction(-1.0d)).divide(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_099() throws Exception {
        // Combination: receiver__value=Double.NaN, i=1
        try {
            (new Fraction(Double.NaN)).divide(1);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_100() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=1
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).divide(1);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_101() throws Exception {
        // Combination: receiver__value=0.0d, i=-1
        Object actual = (new Fraction(0.0d)).divide(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_102() throws Exception {
        // Combination: receiver__value=1.0d, i=-1
        Object actual = (new Fraction(1.0d)).divide(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_103() throws Exception {
        // Combination: receiver__value=-1.0d, i=-1
        Object actual = (new Fraction(-1.0d)).divide(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_104() throws Exception {
        // Combination: receiver__value=Double.NaN, i=-1
        try {
            (new Fraction(Double.NaN)).divide(-1);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_105() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=-1
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).divide(-1);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_106() throws Exception {
        // Combination: receiver__value=0.0d, i=Integer.MAX_VALUE
        Object actual = (new Fraction(0.0d)).divide(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_107() throws Exception {
        // Combination: receiver__value=1.0d, i=Integer.MAX_VALUE
        Object actual = (new Fraction(1.0d)).divide(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1 / 2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_108() throws Exception {
        // Combination: receiver__value=-1.0d, i=Integer.MAX_VALUE
        Object actual = (new Fraction(-1.0d)).divide(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-1 / 2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_109() throws Exception {
        // Combination: receiver__value=Double.NaN, i=Integer.MAX_VALUE
        try {
            (new Fraction(Double.NaN)).divide(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_110() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=Integer.MAX_VALUE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).divide(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_111() throws Exception {
        // Combination: receiver__value=0.0d, i=Integer.MIN_VALUE
        try {
            (new Fraction(0.0d)).divide(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_112() throws Exception {
        // Combination: receiver__value=1.0d, i=Integer.MIN_VALUE
        try {
            (new Fraction(1.0d)).divide(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_113() throws Exception {
        // Combination: receiver__value=-1.0d, i=Integer.MIN_VALUE
        try {
            (new Fraction(-1.0d)).divide(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_114() throws Exception {
        // Combination: receiver__value=Double.NaN, i=Integer.MIN_VALUE
        try {
            (new Fraction(Double.NaN)).divide(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_115() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, i=Integer.MIN_VALUE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).divide(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.fraction.FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_116() throws Exception {
        // Combination: numerator=0, denominator=0
        try {
            Fraction.getReducedFraction(0, 0);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_117() throws Exception {
        // Combination: numerator=1, denominator=0
        try {
            Fraction.getReducedFraction(1, 0);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_118() throws Exception {
        // Combination: numerator=-1, denominator=0
        try {
            Fraction.getReducedFraction(-1, 0);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_119() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=0
        try {
            Fraction.getReducedFraction(Integer.MAX_VALUE, 0);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_120() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=0
        try {
            Fraction.getReducedFraction(Integer.MIN_VALUE, 0);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_121() throws Exception {
        // Combination: numerator=0, denominator=1
        Object actual = Fraction.getReducedFraction(0, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_122() throws Exception {
        // Combination: numerator=1, denominator=1
        Object actual = Fraction.getReducedFraction(1, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_123() throws Exception {
        // Combination: numerator=-1, denominator=1
        Object actual = Fraction.getReducedFraction(-1, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_124() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=1
        Object actual = Fraction.getReducedFraction(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_125() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=1
        Object actual = Fraction.getReducedFraction(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_126() throws Exception {
        // Combination: numerator=0, denominator=-1
        Object actual = Fraction.getReducedFraction(0, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_127() throws Exception {
        // Combination: numerator=1, denominator=-1
        Object actual = Fraction.getReducedFraction(1, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_128() throws Exception {
        // Combination: numerator=-1, denominator=-1
        Object actual = Fraction.getReducedFraction(-1, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_129() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=-1
        Object actual = Fraction.getReducedFraction(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_130() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=-1
        try {
            Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_131() throws Exception {
        // Combination: numerator=0, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_132() throws Exception {
        // Combination: numerator=1, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1 / 2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_133() throws Exception {
        // Combination: numerator=-1, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-1 / 2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_134() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_135() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("-2147483648 / 2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_136() throws Exception {
        // Combination: numerator=0, denominator=Integer.MIN_VALUE
        Object actual = Fraction.getReducedFraction(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_137() throws Exception {
        // Combination: numerator=1, denominator=Integer.MIN_VALUE
        try {
            Fraction.getReducedFraction(1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_138() throws Exception {
        // Combination: numerator=-1, denominator=Integer.MIN_VALUE
        try {
            Fraction.getReducedFraction(-1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_139() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=Integer.MIN_VALUE
        try {
            Fraction.getReducedFraction(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.MathArithmeticException");
        } catch (org.apache.commons.math3.exception.MathArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_140() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=Integer.MIN_VALUE
        Object actual = Fraction.getReducedFraction(Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math3.fraction.Fraction", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

}
