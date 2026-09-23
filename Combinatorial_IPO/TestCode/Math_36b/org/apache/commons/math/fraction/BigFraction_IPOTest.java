package org.apache.commons.math.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for BigFraction.
 */
public class BigFraction_IPOTest {
    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_001() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, roundingMode=0
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(0);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_002() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, roundingMode=1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(1);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_003() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_004() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_005() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_006() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, roundingMode=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(0);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_007() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, roundingMode=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(1);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_008() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_009() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_010() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_011() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), roundingMode=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(0);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_012() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), roundingMode=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(1);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_013() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_014() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_015() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_016() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=0, roundingMode=0
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(0, 0);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_017() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=1, roundingMode=1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(1, 1);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_018() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=-1, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(-1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_019() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=Integer.MAX_VALUE, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_020() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=Integer.MIN_VALUE, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_021() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, scale=1, roundingMode=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(1, 0);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_022() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, scale=0, roundingMode=1
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(0, 1);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_023() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, scale=Integer.MAX_VALUE, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_024() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, scale=-1, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_025() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, scale=0, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_026() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), scale=-1, roundingMode=0
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(-1, 0);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("-1E+1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_027() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), scale=Integer.MAX_VALUE, roundingMode=1
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_028() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), scale=0, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_029() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), scale=1, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_030() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), scale=1, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_031() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=0, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_032() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=1, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_033() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=-1, roundingMode=1
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(-1, 1);
        assertNotNull(actual);
        assertEquals("java.math.BigDecimal", actual.getClass().getName());
        assertEquals("0E+1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_034() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=-1, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_035() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=Integer.MAX_VALUE, roundingMode=0
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_036() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=Integer.MAX_VALUE, roundingMode=Integer.MIN_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_037() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, scale=Integer.MIN_VALUE, roundingMode=0
        try {
            (new BigFraction(java.math.BigInteger.ONE)).bigDecimalValue(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_038() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), scale=Integer.MIN_VALUE, roundingMode=1
        try {
            (new BigFraction(java.math.BigInteger.ONE.negate())).bigDecimalValue(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_039() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=Integer.MIN_VALUE, roundingMode=-1
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_bigDecimalValue_pairwise_040() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, scale=Integer.MIN_VALUE, roundingMode=Integer.MAX_VALUE
        try {
            (new BigFraction(java.math.BigInteger.ZERO)).bigDecimalValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_041() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, other=new Object()
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_042() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, other=new Object()
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_043() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), other=new Object()
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_044() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, other="sample_str"
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_045() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, other="sample_str"
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_046() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), other="sample_str"
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_047() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, other=Integer.valueOf(1)
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_048() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, other=Integer.valueOf(1)
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_049() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), other=Integer.valueOf(1)
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_050() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=0.0d
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_051() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=0.0d
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_052() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=0.0d
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_053() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=1.0d
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_054() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=1.0d
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_055() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=1.0d
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_056() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=-1.0d
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_057() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=-1.0d
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_058() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=-1.0d
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_059() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=Double.NaN
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_060() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=Double.NaN
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_061() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=Double.NaN
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_062() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ZERO, exponent=Double.POSITIVE_INFINITY
        Object actual = (new BigFraction(java.math.BigInteger.ZERO)).pow(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_063() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE, exponent=Double.POSITIVE_INFINITY
        Object actual = (new BigFraction(java.math.BigInteger.ONE)).pow(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_064() throws Exception {
        // Combination: receiver__num=java.math.BigInteger.ONE.negate(), exponent=Double.POSITIVE_INFINITY
        Object actual = (new BigFraction(java.math.BigInteger.ONE.negate())).pow(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

}
