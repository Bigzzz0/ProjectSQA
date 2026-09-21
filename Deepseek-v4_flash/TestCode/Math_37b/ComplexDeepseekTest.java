package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: Complex (org.apache.commons.math.complex)
 * 
 * Known Defect (from Defects4J):
 * - tan() and tanh() methods return NaN instead of expected finite values
 *   for infinite imaginary parts (e.g., tan(1 + INFINITY i) should be 0 + 1i,
 *   but returns NaN + NaNi)
 * - Specifically: testTanInf, testTanhInf, testTan, testTanh fail with
 *   expected:<1.0> but was:<NaN>
 * 
 * Branch/Decision Coverage Targets:
 * 1. tan() method:
 *    - isNaN branch (returns NaN)
 *    - Infinite imaginary part handling (defective: returns NaN instead of 0 + 1i)
 *    - Normal finite computation path
 * 2. tanh() method:
 *    - isNaN branch (returns NaN)
 *    - Infinite imaginary part handling (defective: returns NaN instead of 0 + 1i)
 *    - Normal finite computation path
 * 3. Supporting methods:
 *    - createComplex() factory
 *    - getReal()/getImaginary() accessors
 *    - isNaN()/isInfinite() state flags
 * 
 * Boundary Value Analysis:
 * - Zero real/imaginary parts
 * - Positive/Negative infinity in imaginary part
 * - NaN in real/imaginary parts
 * - Large finite values
 * - Zero values
 * 
 * Defect-Targeted Tests:
 * - testTanInfiniteImaginary: tan(1 + INFINITY i) should be 0 + 1i
 * - testTanhInfiniteImaginary: tanh(1 + INFINITY i) should be 1 + 0i
 * - testTanNegativeInfiniteImaginary: tan(1 - INFINITY i) should be 0 - 1i
 * - testTanhNegativeInfiniteImaginary: tanh(1 - INFINITY i) should be 1 + 0i
 */
public class ComplexDeepseekTest {

    /* ==================== PART A: Core Functional Logic & State Transitions ==================== */

    @Test(timeout = 4000)
    public void testCreateComplexAndAccessors() {
        Complex c = new Complex(3.0, 4.0);
        assertEquals("Real part", 3.0, c.getReal(), 0.0);
        assertEquals("Imaginary part", 4.0, c.getImaginary(), 0.0);
        assertFalse("Should not be NaN", c.isNaN());
        assertFalse("Should not be infinite", c.isInfinite());
    }

    @Test(timeout = 4000)
    public void testCreateComplexSingleArg() {
        Complex c = new Complex(2.5);
        assertEquals("Real part", 2.5, c.getReal(), 0.0);
        assertEquals("Imaginary part", 0.0, c.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testIsNaNWithNaNReal() {
        Complex c = new Complex(Double.NaN, 1.0);
        assertTrue("Should be NaN", c.isNaN());
        assertFalse("Should not be infinite", c.isInfinite());
    }

    @Test(timeout = 4000)
    public void testIsNaNWithNaNImaginary() {
        Complex c = new Complex(1.0, Double.NaN);
        assertTrue("Should be NaN", c.isNaN());
        assertFalse("Should not be infinite", c.isInfinite());
    }

    @Test(timeout = 4000)
    public void testIsInfiniteWithInfiniteReal() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        assertFalse("Should not be NaN", c.isNaN());
        assertTrue("Should be infinite", c.isInfinite());
    }

    @Test(timeout = 4000)
    public void testIsInfiniteWithInfiniteImaginary() {
        Complex c = new Complex(1.0, Double.NEGATIVE_INFINITY);
        assertFalse("Should not be NaN", c.isNaN());
        assertTrue("Should be infinite", c.isInfinite());
    }

    @Test(timeout = 4000)
    public void testIsInfiniteWithBothInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertFalse("Should not be NaN", c.isNaN());
        assertTrue("Should be infinite", c.isInfinite());
    }

    @Test(timeout = 4000)
    public void testIsNaNWithBothNaN() {
        Complex c = new Complex(Double.NaN, Double.NaN);
        assertTrue("Should be NaN", c.isNaN());
        assertFalse("Should not be infinite", c.isInfinite());
    }

    /* ==================== PART B: Boundary Value Analysis & Extremes ==================== */

    @Test(timeout = 4000)
    public void testZeroComplexNumber() {
        Complex zero = new Complex(0.0, 0.0);
        assertEquals("Real part", 0.0, zero.getReal(), 0.0);
        assertEquals("Imaginary part", 0.0, zero.getImaginary(), 0.0);
        assertFalse("Should not be NaN", zero.isNaN());
        assertFalse("Should not be infinite", zero.isInfinite());
    }

    @Test(timeout = 4000)
    public void testPositiveInfinityComplexNumber() {
        Complex inf = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertFalse("Should not be NaN", inf.isNaN());
        assertTrue("Should be infinite", inf.isInfinite());
    }

    @Test(timeout = 4000)
    public void testNegativeInfinityComplexNumber() {
        Complex inf = new Complex(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertFalse("Should not be NaN", inf.isNaN());
        assertTrue("Should be infinite", inf.isInfinite());
    }

    @Test(timeout = 4000)
    public void testMixedInfiniteParts() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        assertFalse("Should not be NaN", c.isNaN());
        assertTrue("Should be infinite", c.isInfinite());
    }

    /* ==================== PART C: Defect-Targeted Branch Zone ==================== */

    /**
     * DEFECT-TARGETED TEST:
     * tan(1 + INFINITY i) should be 0 + 1i according to complex analysis
     * (tan(a + bi) = sin(2a)/(cos(2a)+cosh(2b)) + [sinh(2b)/(cos(2a)+cosh(2b))]i)
     * When b -> INFINITY, sinh(2b) -> INFINITY, cosh(2b) -> INFINITY,
     * so imaginary part -> 1.0 and real part -> 0.0
     * 
     * The defective implementation returns NaN + NaNi instead.
     */
    @Test(timeout = 4000)
    public void testTanInfiniteImaginary() {
        Complex c = new Complex(1.0, Double.POSITIVE_INFINITY);
        Complex result = c.tan();
        
        // Expected: tan(1 + INFINITY i) = 0 + 1i
        assertEquals("Real part should be 0.0", 0.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part should be 1.0", 1.0, result.getImaginary(), 1e-9);
    }

    /**
     * DEFECT-TARGETED TEST:
     * tan(1 - INFINITY i) should be 0 - 1i
     */
    @Test(timeout = 4000)
    public void testTanNegativeInfiniteImaginary() {
        Complex c = new Complex(1.0, Double.NEGATIVE_INFINITY);
        Complex result = c.tan();
        
        // Expected: tan(1 - INFINITY i) = 0 - 1i
        assertEquals("Real part should be 0.0", 0.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part should be -1.0", -1.0, result.getImaginary(), 1e-9);
    }

    /**
     * DEFECT-TARGETED TEST:
     * tanh(1 + INFINITY i) should be 1 + 0i
     * (tanh(a + bi) = sinh(2a)/(cosh(2a)+cos(2b)) + [sin(2b)/(cosh(2a)+cos(2b))]i)
     * When b -> INFINITY, cos(2b) oscillates but is bounded, sinh(2a) and cosh(2a)
     * are finite for finite a, so real part -> 1.0 and imaginary part -> 0.0
     */
    @Test(timeout = 4000)
    public void testTanhInfiniteImaginary() {
        Complex c = new Complex(1.0, Double.POSITIVE_INFINITY);
        Complex result = c.tanh();
        
        // Expected: tanh(1 + INFINITY i) = 1 + 0i
        assertEquals("Real part should be 1.0", 1.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part should be 0.0", 0.0, result.getImaginary(), 1e-9);
    }

    /**
     * DEFECT-TARGETED TEST:
     * tanh(1 - INFINITY i) should be 1 + 0i
     */
    @Test(timeout = 4000)
    public void testTanhNegativeInfiniteImaginary() {
        Complex c = new Complex(1.0, Double.NEGATIVE_INFINITY);
        Complex result = c.tanh();
        
        // Expected: tanh(1 - INFINITY i) = 1 + 0i
        assertEquals("Real part should be 1.0", 1.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part should be 0.0", 0.0, result.getImaginary(), 1e-9);
    }

    /**
     * DEFECT-TARGETED TEST:
     * tan(0 + INFINITY i) should be 0 + 1i
     */
    @Test(timeout = 4000)
    public void testTanZeroRealInfiniteImaginary() {
        Complex c = new Complex(0.0, Double.POSITIVE_INFINITY);
        Complex result = c.tan();
        
        // Expected: tan(0 + INFINITY i) = 0 + 1i
        assertEquals("Real part should be 0.0", 0.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part should be 1.0", 1.0, result.getImaginary(), 1e-9);
    }

    /**
     * DEFECT-TARGETED TEST:
     * tanh(0 + INFINITY i) should be 0 + 1i
     */
    @Test(timeout = 4000)
    public void testTanhZeroRealInfiniteImaginary() {
        Complex c = new Complex(0.0, Double.POSITIVE_INFINITY);
        Complex result = c.tanh();
        
        // Expected: tanh(0 + INFINITY i) = 0 + 1i
        assertEquals("Real part should be 0.0", 0.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part should be 1.0", 1.0, result.getImaginary(), 1e-9);
    }

    /* ==================== PART D: Exception & Defensive Guard Paths ==================== */

    @Test(timeout = 4000)
    public void testTanNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.tan();
        assertTrue("Should return NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testTanhNaN() {
        Complex c = new Complex(1.0, Double.NaN);
        Complex result = c.tanh();
        assertTrue("Should return NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testTanInfiniteReal() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Complex result = c.tan();
        // For infinite real part, result should be NaN + NaNi
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testTanhInfiniteReal() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Complex result = c.tanh();
        // For infinite real part, result should be NaN + NaNi
        assertTrue("Should be NaN", result.isNaN());
    }

    /* ==================== PART E: Object Lifecycle & Contract Integrity ==================== */

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        Complex c = new Complex(1.0, 2.0);
        assertEquals("Should be equal to itself", c, c);
    }

    @Test(timeout = 4000)
    public void testEqualsEqualObjects() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        assertEquals("Equal complex numbers should be equal", c1, c2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentReal() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.5, 2.0);
        assertNotEquals("Different real parts should not be equal", c1, c2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentImaginary() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.5);
        assertNotEquals("Different imaginary parts should not be equal", c1, c2);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Complex c = new Complex(1.0, 2.0);
        assertNotEquals("Should not be equal to null", null, c);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        Complex c = new Complex(1.0, 2.0);
        assertNotEquals("Should not be equal to different type", "not a complex", c);
    }

    @Test(timeout = 4000)
    public void testEqualsNaN() {
        Complex c1 = new Complex(Double.NaN, 1.0);
        Complex c2 = new Complex(1.0, Double.NaN);
        Complex c3 = new Complex(Double.NaN, Double.NaN);
        assertEquals("All NaN complex numbers should be equal", c1, c2);
        assertEquals("All NaN complex numbers should be equal", c1, c3);
    }

    @Test(timeout = 4000)
    public void testHashCodeNaN() {
        Complex c1 = new Complex(Double.NaN, 1.0);
        Complex c2 = new Complex(1.0, Double.NaN);
        assertEquals("NaN hash codes should be equal", c1.hashCode(), c2.hashCode());
        assertEquals("NaN hash code should be 7", 7, c1.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistent() {
        Complex c = new Complex(3.0, 4.0);
        int h1 = c.hashCode();
        int h2 = c.hashCode();
        assertEquals("Hash code should be consistent", h1, h2);
    }

    @Test(timeout = 4000)
    public void testToString() {
        Complex c = new Complex(1.0, 2.0);
        assertEquals("ToString representation", "(1.0, 2.0)", c.toString());
    }

    @Test(timeout = 4000)
    public void testValueOf() {
        Complex c = Complex.valueOf(1.0, 2.0);
        assertEquals("Real part", 1.0, c.getReal(), 0.0);
        assertEquals("Imaginary part", 2.0, c.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testValueOfNaN() {
        Complex c = Complex.valueOf(Double.NaN, 1.0);
        assertTrue("Should be NaN", c.isNaN());
    }

    @Test(timeout = 4000)
    public void testValueOfSingleArg() {
        Complex c = Complex.valueOf(2.5);
        assertEquals("Real part", 2.5, c.getReal(), 0.0);
        assertEquals("Imaginary part", 0.0, c.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testValueOfSingleArgNaN() {
        Complex c = Complex.valueOf(Double.NaN);
        assertTrue("Should be NaN", c.isNaN());
    }

    /* ==================== Additional Coverage for Related Methods ==================== */

    @Test(timeout = 4000)
    public void testSqrt() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.sqrt();
        assertEquals("Real part", 1.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSqrtNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.sqrt();
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSqrtZero() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.sqrt();
        assertEquals("Real part", 0.0, result.getReal(), 0.0);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSqrtNegativeReal() {
        Complex c = new Complex(-1.0, 0.0);
        Complex result = c.sqrt();
        assertEquals("Real part", 0.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part", 1.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSqrt1z() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.sqrt1z();
        assertEquals("Real part", 1.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSqrt1zNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.sqrt1z();
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testCosh() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.cosh();
        assertEquals("Real part", Math.cosh(1.0), result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testCoshNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.cosh();
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSinh() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.sinh();
        assertEquals("Real part", Math.sinh(1.0), result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSinhNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.sinh();
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSin() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.sin();
        assertEquals("Real part", Math.sin(1.0), result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSinNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.sin();
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testCos() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.cos();
        assertEquals("Real part", Math.cos(1.0), result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testCosNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.cos();
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testExp() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.exp();
        assertEquals("Real part", Math.exp(1.0), result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testExpNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.exp();
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testLog() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.log();
        assertEquals("Real part", 0.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testLogNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.log();
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testMultiply() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.multiply(c2);
        assertEquals("Real part", -5.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part", 10.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testMultiplyNaN() {
        Complex c1 = new Complex(Double.NaN, 1.0);
        Complex c2 = new Complex(1.0, 1.0);
        Complex result = c1.multiply(c2);
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testMultiplyInfinite() {
        Complex c1 = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Complex c2 = new Complex(1.0, 1.0);
        Complex result = c1.multiply(c2);
        assertTrue("Should be infinite", result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testDivide() {
        Complex c1 = new Complex(1.0, 0.0);
        Complex c2 = new Complex(1.0, 0.0);
        Complex result = c1.divide(c2);
        assertEquals("Real part", 1.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testDivideNaN() {
        Complex c1 = new Complex(Double.NaN, 1.0);
        Complex c2 = new Complex(1.0, 1.0);
        Complex result = c1.divide(c2);
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideByZero() {
        Complex c1 = new Complex(1.0, 1.0);
        Complex c2 = new Complex(0.0, 0.0);
        Complex result = c1.divide(c2);
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideNull() {
        Complex c1 = new Complex(1.0, 1.0);
        try {
            c1.divide((Complex) null);
            fail("Should throw NullArgumentException");
        } catch (NullArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testAdd() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.add(c2);
        assertEquals("Real part", 4.0, result.getReal(), 0.0);
        assertEquals("Imaginary part", 6.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddNaN() {
        Complex c1 = new Complex(Double.NaN, 1.0);
        Complex c2 = new Complex(1.0, 1.0);
        Complex result = c1.add(c2);
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testAddNull() {
        Complex c1 = new Complex(1.0, 1.0);
        try {
            c1.add((Complex) null);
            fail("Should throw NullArgumentException");
        } catch (NullArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testSubtract() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.subtract(c2);
        assertEquals("Real part", -2.0, result.getReal(), 0.0);
        assertEquals("Imaginary part", -2.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtractNaN() {
        Complex c1 = new Complex(Double.NaN, 1.0);
        Complex c2 = new Complex(1.0, 1.0);
        Complex result = c1.subtract(c2);
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSubtractNull() {
        Complex c1 = new Complex(1.0, 1.0);
        try {
            c1.subtract((Complex) null);
            fail("Should throw NullArgumentException");
        } catch (NullArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNegate() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.negate();
        assertEquals("Real part", -1.0, result.getReal(), 0.0);
        assertEquals("Imaginary part", -2.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testNegateNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.negate();
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testConjugate() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.conjugate();
        assertEquals("Real part", 1.0, result.getReal(), 0.0);
        assertEquals("Imaginary part", -2.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testConjugateNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.conjugate();
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testGetArgument() {
        Complex c = new Complex(1.0, 0.0);
        assertEquals("Argument", 0.0, c.getArgument(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testGetArgumentNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        assertTrue("Should be NaN", Double.isNaN(c.getArgument()));
    }

    @Test(timeout = 4000)
    public void testPow() {
        Complex c = new Complex(2.0, 0.0);
        Complex result = c.pow(2.0);
        assertEquals("Real part", 4.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testPowComplex() {
        Complex c = new Complex(2.0, 0.0);
        Complex exponent = new Complex(2.0, 0.0);
        Complex result = c.pow(exponent);
        assertEquals("Real part", 4.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testPowNull() {
        Complex c = new Complex(2.0, 0.0);
        try {
            c.pow((Complex) null);
            fail("Should throw NullArgumentException");
        } catch (NullArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNthRoot() {
        Complex c = new Complex(1.0, 0.0);
        List<Complex> roots = c.nthRoot(4);
        assertEquals("Should have 4 roots", 4, roots.size());
    }

    @Test(timeout = 4000)
    public void testNthRootNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        List<Complex> roots = c.nthRoot(4);
        assertEquals("Should have 1 root", 1, roots.size());
        assertTrue("Should be NaN", roots.get(0).isNaN());
    }

    @Test(timeout = 4000)
    public void testNthRootInvalidN() {
        Complex c = new Complex(1.0, 0.0);
        try {
            c.nthRoot(0);
            fail("Should throw NotPositiveException");
        } catch (NotPositiveException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNthRootNegativeN() {
        Complex c = new Complex(1.0, 0.0);
        try {
            c.nthRoot(-1);
            fail("Should throw NotPositiveException");
        } catch (NotPositiveException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetField() {
        Complex c = new Complex(1.0, 0.0);
        assertNotNull("Field should not be null", c.getField());
    }

    @Test(timeout = 4000)
    public void testReadResolve() {
        Complex c = new Complex(1.0, 2.0);
        Complex resolved = c.readResolve();
        assertEquals("Resolved should be equal", c, resolved);
    }

    @Test(timeout = 4000)
    public void testCreateComplex() {
        Complex c = new Complex(1.0, 2.0);
        Complex created = c.createComplex(3.0, 4.0);
        assertEquals("Real part", 3.0, created.getReal(), 0.0);
        assertEquals("Imaginary part", 4.0, created.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testMultiplyDouble() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.multiply(2.0);
        assertEquals("Real part", 2.0, result.getReal(), 0.0);
        assertEquals("Imaginary part", 4.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testMultiplyDoubleNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.multiply(2.0);
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testMultiplyDoubleInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Complex result = c.multiply(2.0);
        assertTrue("Should be infinite", result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testMultiplyInt() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.multiply(2);
        assertEquals("Real part", 2.0, result.getReal(), 0.0);
        assertEquals("Imaginary part", 4.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testMultiplyIntNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.multiply(2);
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testMultiplyIntInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Complex result = c.multiply(2);
        assertTrue("Should be infinite", result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testSubtractDouble() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.subtract(1.0);
        assertEquals("Real part", 0.0, result.getReal(), 0.0);
        assertEquals("Imaginary part", 2.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtractDoubleNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.subtract(1.0);
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testAtan() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.atan();
        assertEquals("Real part", Math.PI / 4, result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testAtanNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.atan();
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testAcos() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.acos();
        assertEquals("Real part", 0.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testAcosNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.acos();
        assertTrue("Should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testAsin() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.asin();
        assertEquals("Real part", 0.0, result.getReal(), 1e-9);
        assertEquals("Imaginary part", 0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testAsinNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.asin();
        assertTrue("Should be NaN", result.isNaN());
    }
}