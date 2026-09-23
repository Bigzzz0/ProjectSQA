package org.apache.commons.math.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Fraction.
 */
public class Fraction_IPOTest {
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
    public void test_compareTo_pairwise_001() throws Exception {
        // Combination: receiver__value=0.0d, object=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(0.0d)).compareTo(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_002() throws Exception {
        // Combination: receiver__value=1.0d, object=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(1.0d)).compareTo(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_003() throws Exception {
        // Combination: receiver__value=-1.0d, object=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(-1.0d)).compareTo(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_004() throws Exception {
        // Combination: receiver__value=Double.NaN, object=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(Double.NaN)).compareTo(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_005() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, object=new org.apache.commons.math.fraction.Fraction(1, 2)
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).compareTo(new org.apache.commons.math.fraction.Fraction(1, 2));
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_006() throws Exception {
        // Combination: receiver__value=0.0d, object=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(0.0d)).compareTo(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_007() throws Exception {
        // Combination: receiver__value=1.0d, object=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(1.0d)).compareTo(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_008() throws Exception {
        // Combination: receiver__value=-1.0d, object=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(-1.0d)).compareTo(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_009() throws Exception {
        // Combination: receiver__value=Double.NaN, object=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(Double.NaN)).compareTo(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_010() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, object=org.apache.commons.math.fraction.Fraction.ONE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).compareTo(org.apache.commons.math.fraction.Fraction.ONE);
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_011() throws Exception {
        // Combination: receiver__value=0.0d, other=new Object()
        Object actual = (new Fraction(0.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_012() throws Exception {
        // Combination: receiver__value=1.0d, other=new Object()
        Object actual = (new Fraction(1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_013() throws Exception {
        // Combination: receiver__value=-1.0d, other=new Object()
        Object actual = (new Fraction(-1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_014() throws Exception {
        // Combination: receiver__value=Double.NaN, other=new Object()
        Object actual = (new Fraction(Double.NaN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_015() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, other=new Object()
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).equals(new Object());
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_016() throws Exception {
        // Combination: receiver__value=0.0d, other="sample_str"
        Object actual = (new Fraction(0.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_017() throws Exception {
        // Combination: receiver__value=1.0d, other="sample_str"
        Object actual = (new Fraction(1.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_018() throws Exception {
        // Combination: receiver__value=-1.0d, other="sample_str"
        Object actual = (new Fraction(-1.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_019() throws Exception {
        // Combination: receiver__value=Double.NaN, other="sample_str"
        Object actual = (new Fraction(Double.NaN)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_020() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, other="sample_str"
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).equals("sample_str");
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_021() throws Exception {
        // Combination: receiver__value=0.0d, other=Integer.valueOf(1)
        Object actual = (new Fraction(0.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_022() throws Exception {
        // Combination: receiver__value=1.0d, other=Integer.valueOf(1)
        Object actual = (new Fraction(1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_023() throws Exception {
        // Combination: receiver__value=-1.0d, other=Integer.valueOf(1)
        Object actual = (new Fraction(-1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_024() throws Exception {
        // Combination: receiver__value=Double.NaN, other=Integer.valueOf(1)
        Object actual = (new Fraction(Double.NaN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_025() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, other=Integer.valueOf(1)
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).equals(Integer.valueOf(1));
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_026() throws Exception {
        // Combination: receiver__value=0.0d, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(0.0d)).add(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b10", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_027() throws Exception {
        // Combination: receiver__value=1.0d, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(1.0d)).add(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b5a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_028() throws Exception {
        // Combination: receiver__value=-1.0d, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(-1.0d)).add(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5ac6", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_029() throws Exception {
        // Combination: receiver__value=Double.NaN, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        try {
            (new Fraction(Double.NaN)).add(new org.apache.commons.math.fraction.Fraction(1, 2));
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_030() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).add(new org.apache.commons.math.fraction.Fraction(1, 2));
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_031() throws Exception {
        // Combination: receiver__value=0.0d, fraction=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(0.0d)).add(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b0f", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_032() throws Exception {
        // Combination: receiver__value=1.0d, fraction=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(1.0d)).add(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b34", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_033() throws Exception {
        // Combination: receiver__value=-1.0d, fraction=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(-1.0d)).add(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5aea", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_034() throws Exception {
        // Combination: receiver__value=Double.NaN, fraction=org.apache.commons.math.fraction.Fraction.ONE
        try {
            (new Fraction(Double.NaN)).add(org.apache.commons.math.fraction.Fraction.ONE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_035() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, fraction=org.apache.commons.math.fraction.Fraction.ONE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).add(org.apache.commons.math.fraction.Fraction.ONE);
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_036() throws Exception {
        // Combination: receiver__value=0.0d, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(0.0d)).subtract(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5ac6", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_037() throws Exception {
        // Combination: receiver__value=1.0d, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(1.0d)).subtract(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b10", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_038() throws Exception {
        // Combination: receiver__value=-1.0d, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(-1.0d)).subtract(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5a7c", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_039() throws Exception {
        // Combination: receiver__value=Double.NaN, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        try {
            (new Fraction(Double.NaN)).subtract(new org.apache.commons.math.fraction.Fraction(1, 2));
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_040() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).subtract(new org.apache.commons.math.fraction.Fraction(1, 2));
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_041() throws Exception {
        // Combination: receiver__value=0.0d, fraction=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(0.0d)).subtract(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5ac5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_042() throws Exception {
        // Combination: receiver__value=1.0d, fraction=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(1.0d)).subtract(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5aea", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_043() throws Exception {
        // Combination: receiver__value=-1.0d, fraction=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(-1.0d)).subtract(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5aa0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_044() throws Exception {
        // Combination: receiver__value=Double.NaN, fraction=org.apache.commons.math.fraction.Fraction.ONE
        try {
            (new Fraction(Double.NaN)).subtract(org.apache.commons.math.fraction.Fraction.ONE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_045() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, fraction=org.apache.commons.math.fraction.Fraction.ONE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).subtract(org.apache.commons.math.fraction.Fraction.ONE);
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_046() throws Exception {
        // Combination: receiver__value=0.0d, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(0.0d)).multiply(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5aea", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_047() throws Exception {
        // Combination: receiver__value=1.0d, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(1.0d)).multiply(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b10", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_048() throws Exception {
        // Combination: receiver__value=-1.0d, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(-1.0d)).multiply(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5ac6", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_049() throws Exception {
        // Combination: receiver__value=Double.NaN, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        try {
            (new Fraction(Double.NaN)).multiply(new org.apache.commons.math.fraction.Fraction(1, 2));
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_050() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).multiply(new org.apache.commons.math.fraction.Fraction(1, 2));
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_051() throws Exception {
        // Combination: receiver__value=0.0d, fraction=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(0.0d)).multiply(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5aea", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_052() throws Exception {
        // Combination: receiver__value=1.0d, fraction=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(1.0d)).multiply(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b0f", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_053() throws Exception {
        // Combination: receiver__value=-1.0d, fraction=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(-1.0d)).multiply(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5ac5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_054() throws Exception {
        // Combination: receiver__value=Double.NaN, fraction=org.apache.commons.math.fraction.Fraction.ONE
        try {
            (new Fraction(Double.NaN)).multiply(org.apache.commons.math.fraction.Fraction.ONE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_055() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, fraction=org.apache.commons.math.fraction.Fraction.ONE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).multiply(org.apache.commons.math.fraction.Fraction.ONE);
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_056() throws Exception {
        // Combination: receiver__value=0.0d, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(0.0d)).divide(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5aea", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_057() throws Exception {
        // Combination: receiver__value=1.0d, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(1.0d)).divide(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b34", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_058() throws Exception {
        // Combination: receiver__value=-1.0d, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        Object actual = (new Fraction(-1.0d)).divide(new org.apache.commons.math.fraction.Fraction(1, 2));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5aa0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_059() throws Exception {
        // Combination: receiver__value=Double.NaN, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        try {
            (new Fraction(Double.NaN)).divide(new org.apache.commons.math.fraction.Fraction(1, 2));
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_060() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, fraction=new org.apache.commons.math.fraction.Fraction(1, 2)
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).divide(new org.apache.commons.math.fraction.Fraction(1, 2));
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_061() throws Exception {
        // Combination: receiver__value=0.0d, fraction=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(0.0d)).divide(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5aea", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_062() throws Exception {
        // Combination: receiver__value=1.0d, fraction=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(1.0d)).divide(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b0f", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_063() throws Exception {
        // Combination: receiver__value=-1.0d, fraction=org.apache.commons.math.fraction.Fraction.ONE
        Object actual = (new Fraction(-1.0d)).divide(org.apache.commons.math.fraction.Fraction.ONE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5ac5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_064() throws Exception {
        // Combination: receiver__value=Double.NaN, fraction=org.apache.commons.math.fraction.Fraction.ONE
        try {
            (new Fraction(Double.NaN)).divide(org.apache.commons.math.fraction.Fraction.ONE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_065() throws Exception {
        // Combination: receiver__value=Double.POSITIVE_INFINITY, fraction=org.apache.commons.math.fraction.Fraction.ONE
        try {
            (new Fraction(Double.POSITIVE_INFINITY)).divide(org.apache.commons.math.fraction.Fraction.ONE);
            fail("Expected org.apache.commons.math.fraction.FractionConversionException");
        } catch (org.apache.commons.math.fraction.FractionConversionException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_066() throws Exception {
        // Combination: numerator=0, denominator=0
        try {
            Fraction.getReducedFraction(0, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_067() throws Exception {
        // Combination: numerator=1, denominator=0
        try {
            Fraction.getReducedFraction(1, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_068() throws Exception {
        // Combination: numerator=-1, denominator=0
        try {
            Fraction.getReducedFraction(-1, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_069() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=0
        try {
            Fraction.getReducedFraction(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_070() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=0
        try {
            Fraction.getReducedFraction(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_071() throws Exception {
        // Combination: numerator=0, denominator=1
        Object actual = Fraction.getReducedFraction(0, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5aea", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_072() throws Exception {
        // Combination: numerator=1, denominator=1
        Object actual = Fraction.getReducedFraction(1, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b0f", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_073() throws Exception {
        // Combination: numerator=-1, denominator=1
        Object actual = Fraction.getReducedFraction(-1, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5ac5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_074() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=1
        Object actual = Fraction.getReducedFraction(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@80005ac5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_075() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=1
        Object actual = Fraction.getReducedFraction(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@80005aea", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_076() throws Exception {
        // Combination: numerator=0, denominator=-1
        Object actual = Fraction.getReducedFraction(0, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5aea", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_077() throws Exception {
        // Combination: numerator=1, denominator=-1
        Object actual = Fraction.getReducedFraction(1, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5ac5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_078() throws Exception {
        // Combination: numerator=-1, denominator=-1
        Object actual = Fraction.getReducedFraction(-1, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b0f", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_079() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=-1
        Object actual = Fraction.getReducedFraction(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@80005b0f", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_080() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=-1
        try {
            Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_081() throws Exception {
        // Combination: numerator=0, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5aea", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_082() throws Exception {
        // Combination: numerator=1, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@80005b0d", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_083() throws Exception {
        // Combination: numerator=-1, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@80005ac3", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_084() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b0f", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_085() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=Integer.MAX_VALUE
        Object actual = Fraction.getReducedFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5ae8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_086() throws Exception {
        // Combination: numerator=0, denominator=Integer.MIN_VALUE
        Object actual = Fraction.getReducedFraction(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5aea", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_087() throws Exception {
        // Combination: numerator=1, denominator=Integer.MIN_VALUE
        try {
            Fraction.getReducedFraction(1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_088() throws Exception {
        // Combination: numerator=-1, denominator=Integer.MIN_VALUE
        try {
            Fraction.getReducedFraction(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_089() throws Exception {
        // Combination: numerator=Integer.MAX_VALUE, denominator=Integer.MIN_VALUE
        try {
            Fraction.getReducedFraction(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getReducedFraction_pairwise_090() throws Exception {
        // Combination: numerator=Integer.MIN_VALUE, denominator=Integer.MIN_VALUE
        Object actual = Fraction.getReducedFraction(Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.fraction.Fraction", actual.getClass().getName());
        assertEquals("org.apache.commons.math.fraction.Fraction@5b0f", formatValue(actual));
    }

}
