package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: org.apache.commons.math.complex.Complex
 * 
 * Key Branches and Boundary Conditions:
 * 1. tan() method:
 *    - isNaN branch (returns NaN)
 *    - Normal computation path with finite values
 *    - Infinite real/imaginary parts handling
 *    - Zero real/imaginary parts
 * 
 * 2. divide(Complex) method:
 *    - Null divisor (NullArgumentException)
 *    - NaN in either operand
 *    - Zero divisor (returns NaN)
 *    - Infinite divisor with finite this (returns ZERO)
 *    - Infinite this with finite divisor
 *    - Both infinite (returns NaN)
 *    - Normal division with various magnitudes
 *    - Prescaling branches (|c| < |d| vs |c| >= |d|)
 * 
 * 3. sqrt1z() method:
 *    - NaN propagation
 *    - Normal computation
 *    - Infinite handling
 * 
 * 4. log() method:
 *    - NaN propagation
 *    - Zero real/imaginary parts
 *    - Negative real parts
 * 
 * 5. atan() method:
 *    - NaN propagation
 *    - Normal computation
 *    - Infinite handling
 * 
 * 6. cosh() method:
 *    - NaN propagation
 *    - Infinite handling
 *    - Normal computation
 * 
 * 7. sinh() method:
 *    - NaN propagation
 *    - Infinite handling
 *    - Normal computation
 * 
 * 8. tanh() method:
 *    - NaN propagation
 *    - Infinite handling
 *    - Normal computation
 * 
 * 9. exp() method:
 *    - NaN propagation
 *    - Infinite handling
 *    - Normal computation
 * 
 * 10. multiply(Complex) method:
 *     - Null factor (NullArgumentException)
 *     - NaN in either operand
 *     - Infinite in either operand
 *     - Normal multiplication
 * 
 * 11. multiply(double) method:
 *     - NaN factor
 *     - Infinite factor
 *     - Normal multiplication
 * 
 * 12. add(Complex) method:
 *     - Null addend (NullArgumentException)
 *     - NaN in either operand
 *     - Normal addition
 * 
 * 13. add(double) method:
 *     - NaN addend
 *     - Normal addition
 * 
 * 14. subtract(Complex) method:
 *     - Null subtrahend (NullArgumentException)
 *     - NaN in either operand
 *     - Normal subtraction
 * 
 * 15. subtract(double) method:
 *     - NaN subtrahend
 *     - Normal subtraction
 * 
 * 16. negate() method:
 *     - NaN propagation
 *     - Normal negation
 * 
 * 17. abs() method:
 *     - NaN propagation
 *     - Infinite propagation
 *     - Zero real/imaginary parts
 *     - |real| < |imaginary| branch
 *     - |real| >= |imaginary| branch
 * 
 * 18. getArgument() method:
 *     - NaN propagation
 *     - Zero real/imaginary parts
 *     - Quadrant boundaries
 * 
 * 19. nthRoot(int) method:
 *     - n <= 0 (NotPositiveException)
 *     - NaN propagation
 *     - Infinite propagation
 *     - Normal computation with various n values
 * 
 * 20. valueOf(double, double) method:
 *     - NaN in either part
 *     - Normal creation
 * 
 * 21. valueOf(double) method:
 *     - NaN real part
 *     - Normal creation
 * 
 * 22. createComplex(double, double) method:
 *     - Normal creation
 * 
 * 23. equals(Object) method:
 *     - Same object reference
 *     - null comparison
 *     - Non-Complex object
 *     - NaN comparison
 *     - Equal real/imaginary parts
 *     - Different real/imaginary parts
 * 
 * 24. hashCode() method:
 *     - NaN hash code (returns 7)
 *     - Normal hash code
 * 
 * 25. getReal()/getImaginary() methods:
 *     - Normal access
 * 
 * 26. isNaN()/isInfinite() methods:
 *     - Normal access
 * 
 * 27. createComplex() method:
 *     - Normal creation
 * 
 * 28. getField() method:
 *     - Returns ComplexField instance
 * 
 * 29. toString() method:
 *     - Normal string representation
 * 
 * 30. readResolve() method:
 *     - Deserialization handling
 * 
 * Known Defect Targets:
 * - testAtanI: atan() method with imaginary unit I
 * - testDivideZero: divide(Complex) with zero divisor
 * 
 * The defect in divide(Complex) is that when divisor is zero,
 * it returns INF instead of NaN. The correct behavior is to
 * return NaN when dividing by zero.
 * 
 * The defect in atan() is related to the computation of
 * atan(I) which should return a specific value but may
 * return incorrect results due to floating point issues.
 */
public class ComplexDeepseekTest {

    /* ==================== Partition A: Core Functional Logic & State Transitions ==================== */

    @Test(timeout = 4000)
    public void testConstructorAndGetters() {
        Complex c = new Complex(3.0, 4.0);
        assertEquals(3.0, c.getReal(), 0.0);
        assertEquals(4.0, c.getImaginary(), 0.0);
        assertFalse(c.isNaN());
        assertFalse(c.isInfinite());
    }

    @Test(timeout = 4000)
    public void testConstructorWithRealOnly() {
        Complex c = new Complex(5.0);
        assertEquals(5.0, c.getReal(), 0.0);
        assertEquals(0.0, c.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateComplex() {
        Complex c = new Complex(1.0, 2.0);
        Complex created = c.createComplex(3.0, 4.0);
        assertEquals(3.0, created.getReal(), 0.0);
        assertEquals(4.0, created.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testValueOfDoubleDouble() {
        Complex c = Complex.valueOf(1.0, 2.0);
        assertEquals(1.0, c.getReal(), 0.0);
        assertEquals(2.0, c.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testValueOfDoubleDoubleNaN() {
        Complex c = Complex.valueOf(Double.NaN, 2.0);
        assertTrue(c.isNaN());
        assertSame(Complex.NaN, c);
    }

    @Test(timeout = 4000)
    public void testValueOfDouble() {
        Complex c = Complex.valueOf(3.0);
        assertEquals(3.0, c.getReal(), 0.0);
        assertEquals(0.0, c.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testValueOfDoubleNaN() {
        Complex c = Complex.valueOf(Double.NaN);
        assertTrue(c.isNaN());
        assertSame(Complex.NaN, c);
    }

    @Test(timeout = 4000)
    public void testGetField() {
        Complex c = new Complex(1.0, 2.0);
        assertNotNull(c.getField());
        assertSame(ComplexField.getInstance(), c.getField());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Complex c = new Complex(1.0, 2.0);
        assertEquals("(1.0, 2.0)", c.toString());
    }

    @Test(timeout = 4000)
    public void testReadResolve() {
        Complex c = new Complex(1.0, 2.0);
        Complex resolved = (Complex) c.readResolve();
        assertEquals(c, resolved);
    }

    /* ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ==================== */

    @Test(timeout = 4000)
    public void testAbsZero() {
        Complex c = new Complex(0.0, 0.0);
        assertEquals(0.0, c.abs(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAbsRealOnly() {
        Complex c = new Complex(3.0, 0.0);
        assertEquals(3.0, c.abs(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAbsImaginaryOnly() {
        Complex c = new Complex(0.0, 4.0);
        assertEquals(4.0, c.abs(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAbsBothParts() {
        Complex c = new Complex(3.0, 4.0);
        assertEquals(5.0, c.abs(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAbsNegativeReal() {
        Complex c = new Complex(-3.0, 4.0);
        assertEquals(5.0, c.abs(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAbsNegativeImaginary() {
        Complex c = new Complex(3.0, -4.0);
        assertEquals(5.0, c.abs(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAbsNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        assertTrue(Double.isNaN(c.abs()));
    }

    @Test(timeout = 4000)
    public void testAbsInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        assertEquals(Double.POSITIVE_INFINITY, c.abs(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAbsLargeValues() {
        Complex c = new Complex(1e300, 1e300);
        assertEquals(Math.sqrt(2) * 1e300, c.abs(), 1e285);
    }

    @Test(timeout = 4000)
    public void testAbsSmallValues() {
        Complex c = new Complex(1e-300, 1e-300);
        assertEquals(Math.sqrt(2) * 1e-300, c.abs(), 1e-315);
    }

    @Test(timeout = 4000)
    public void testGetArgumentZero() {
        Complex c = new Complex(0.0, 0.0);
        assertEquals(0.0, c.getArgument(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetArgumentPositiveReal() {
        Complex c = new Complex(1.0, 0.0);
        assertEquals(0.0, c.getArgument(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetArgumentNegativeReal() {
        Complex c = new Complex(-1.0, 0.0);
        assertEquals(Math.PI, c.getArgument(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetArgumentPositiveImaginary() {
        Complex c = new Complex(0.0, 1.0);
        assertEquals(Math.PI / 2, c.getArgument(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetArgumentNegativeImaginary() {
        Complex c = new Complex(0.0, -1.0);
        assertEquals(-Math.PI / 2, c.getArgument(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetArgumentFirstQuadrant() {
        Complex c = new Complex(1.0, 1.0);
        assertEquals(Math.PI / 4, c.getArgument(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetArgumentSecondQuadrant() {
        Complex c = new Complex(-1.0, 1.0);
        assertEquals(3 * Math.PI / 4, c.getArgument(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetArgumentThirdQuadrant() {
        Complex c = new Complex(-1.0, -1.0);
        assertEquals(-3 * Math.PI / 4, c.getArgument(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetArgumentFourthQuadrant() {
        Complex c = new Complex(1.0, -1.0);
        assertEquals(-Math.PI / 4, c.getArgument(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetArgumentNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        assertTrue(Double.isNaN(c.getArgument()));
    }

    @Test(timeout = 4000)
    public void testGetArgumentInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        assertEquals(0.0, c.getArgument(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testIsNaN() {
        assertTrue(new Complex(Double.NaN, 1.0).isNaN());
        assertTrue(new Complex(1.0, Double.NaN).isNaN());
        assertTrue(new Complex(Double.NaN, Double.NaN).isNaN());
        assertFalse(new Complex(1.0, 2.0).isNaN());
    }

    @Test(timeout = 4000)
    public void testIsInfinite() {
        assertTrue(new Complex(Double.POSITIVE_INFINITY, 1.0).isInfinite());
        assertTrue(new Complex(1.0, Double.NEGATIVE_INFINITY).isInfinite());
        assertTrue(new Complex(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isInfinite());
        assertFalse(new Complex(1.0, 2.0).isInfinite());
        assertFalse(new Complex(Double.NaN, Double.POSITIVE_INFINITY).isInfinite());
    }

    /* ==================== Partition C: Defect-Targeted Branch Zone ==================== */

    /**
     * Test for the known defect in divide(Complex) where dividing by zero
     * returns INF instead of NaN.
     */
    @Test(timeout = 4000)
    public void testDivideZero() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.divide(Complex.ZERO);
        assertTrue("Expected NaN when dividing by zero, but got: " + result, result.isNaN());
        assertSame(Complex.NaN, result);
    }

    /**
     * Test for the known defect in atan() with imaginary unit I.
     */
    @Test(timeout = 4000)
    public void testAtanI() {
        Complex result = Complex.I.atan();
        // atan(i) should be a specific value, but the defect may cause incorrect results
        // The expected value is approximately -i * log((1+i)/(1-i)) / 2
        // which equals -i * log(i) / 2 = -i * (i*pi/2) / 2 = pi/4
        // So atan(i) = pi/4 + 0i
        assertFalse("atan(i) should not be NaN", result.isNaN());
        assertEquals(Math.PI / 4, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDivideZeroWithInfiniteThis() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Complex result = c.divide(Complex.ZERO);
        assertTrue("Expected NaN when dividing infinite by zero", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideZeroWithNaNThis() {
        Complex c = new Complex(Double.NaN, 1.0);
        Complex result = c.divide(Complex.ZERO);
        assertTrue("Expected NaN when dividing NaN by zero", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideZeroWithZeroThis() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.divide(Complex.ZERO);
        assertTrue("Expected NaN when dividing zero by zero", result.isNaN());
    }

    /* ==================== Partition D: Exception & Defensive Guard Paths ==================== */

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testDivideNull() {
        Complex c = new Complex(1.0, 2.0);
        c.divide(null);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testMultiplyNull() {
        Complex c = new Complex(1.0, 2.0);
        c.multiply((Complex) null);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testAddNull() {
        Complex c = new Complex(1.0, 2.0);
        c.add(null);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testSubtractNull() {
        Complex c = new Complex(1.0, 2.0);
        c.subtract(null);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testPowNull() {
        Complex c = new Complex(1.0, 2.0);
        c.pow((Complex) null);
    }

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testNthRootNonPositive() {
        Complex c = new Complex(1.0, 2.0);
        c.nthRoot(0);
    }

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testNthRootNegative() {
        Complex c = new Complex(1.0, 2.0);
        c.nthRoot(-1);
    }

    @Test(timeout = 4000)
    public void testDivideByZeroDouble() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.divide(0.0);
        assertTrue("Expected NaN when dividing by zero double", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideByInfiniteDouble() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.divide(Double.POSITIVE_INFINITY);
        assertEquals(0.0, result.getReal(), 0.0);
        assertEquals(0.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testDivideByNaNDouble() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.divide(Double.NaN);
        assertTrue(result.isNaN());
    }

    /* ==================== Partition E: Object Lifecycle & Contract Integrity ==================== */

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        Complex c = new Complex(1.0, 2.0);
        assertTrue(c.equals(c));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Complex c = new Complex(1.0, 2.0);
        assertFalse(c.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsNonComplex() {
        Complex c = new Complex(1.0, 2.0);
        assertFalse(c.equals("not a complex"));
    }

    @Test(timeout = 4000)
    public void testEqualsEqualComplex() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        assertTrue(c1.equals(c2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentReal() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(2.0, 2.0);
        assertFalse(c1.equals(c2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentImaginary() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 3.0);
        assertFalse(c1.equals(c2));
    }

    @Test(timeout = 4000)
    public void testEqualsNaN() {
        Complex c1 = new Complex(Double.NaN, 2.0);
        Complex c2 = new Complex(Double.NaN, 3.0);
        assertTrue(c1.equals(c2));
    }

    @Test(timeout = 4000)
    public void testEqualsNaNWithNonNaN() {
        Complex c1 = new Complex(Double.NaN, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        assertFalse(c1.equals(c2));
    }

    @Test(timeout = 4000)
    public void testHashCodeNaN() {
        Complex c1 = new Complex(Double.NaN, 2.0);
        Complex c2 = new Complex(1.0, Double.NaN);
        assertEquals(7, c1.hashCode());
        assertEquals(7, c2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeNormal() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeDifferent() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(2.0, 1.0);
        assertNotEquals(c1.hashCode(), c2.hashCode());
    }

    /* ==================== Additional Complex Operations ==================== */

    @Test(timeout = 4000)
    public void testAddComplex() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.add(c2);
        assertEquals(4.0, result.getReal(), 0.0);
        assertEquals(6.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddDouble() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.add(3.0);
        assertEquals(4.0, result.getReal(), 0.0);
        assertEquals(2.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddNaN() {
        Complex c1 = new Complex(Double.NaN, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex result = c1.add(c2);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testAddDoubleNaN() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.add(Double.NaN);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSubtractComplex() {
        Complex c1 = new Complex(5.0, 7.0);
        Complex c2 = new Complex(2.0, 3.0);
        Complex result = c1.subtract(c2);
        assertEquals(3.0, result.getReal(), 0.0);
        assertEquals(4.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtractDouble() {
        Complex c = new Complex(5.0, 7.0);
        Complex result = c.subtract(2.0);
        assertEquals(3.0, result.getReal(), 0.0);
        assertEquals(7.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtractNaN() {
        Complex c1 = new Complex(Double.NaN, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex result = c1.subtract(c2);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSubtractDoubleNaN() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.subtract(Double.NaN);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testMultiplyComplex() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.multiply(c2);
        assertEquals(-5.0, result.getReal(), 0.0);
        assertEquals(10.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testMultiplyDouble() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.multiply(3.0);
        assertEquals(3.0, result.getReal(), 0.0);
        assertEquals(6.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testMultiplyNaN() {
        Complex c1 = new Complex(Double.NaN, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex result = c1.multiply(c2);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testMultiplyDoubleNaN() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.multiply(Double.NaN);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testMultiplyInfinite() {
        Complex c1 = new Complex(Double.POSITIVE_INFINITY, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex result = c1.multiply(c2);
        assertTrue(result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testMultiplyDoubleInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 2.0);
        Complex result = c.multiply(3.0);
        assertTrue(result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testDivideComplex() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.divide(c2);
        assertEquals(0.44, result.getReal(), 1e-12);
        assertEquals(0.08, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDivideDouble() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.divide(2.0);
        assertEquals(0.5, result.getReal(), 0.0);
        assertEquals(1.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testDivideNaN() {
        Complex c1 = new Complex(Double.NaN, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex result = c1.divide(c2);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideInfiniteDivisor() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(Double.POSITIVE_INFINITY, 2.0);
        Complex result = c1.divide(c2);
        assertEquals(0.0, result.getReal(), 0.0);
        assertEquals(0.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testDivideInfiniteThis() {
        Complex c1 = new Complex(Double.POSITIVE_INFINITY, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex result = c1.divide(c2);
        assertTrue(result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testDivideBothInfinite() {
        Complex c1 = new Complex(Double.POSITIVE_INFINITY, 2.0);
        Complex c2 = new Complex(Double.POSITIVE_INFINITY, 2.0);
        Complex result = c1.divide(c2);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testNegate() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.negate();
        assertEquals(-1.0, result.getReal(), 0.0);
        assertEquals(-2.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testNegateNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.negate();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testConjugate() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.conjugate();
        assertEquals(1.0, result.getReal(), 0.0);
        assertEquals(-2.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testConjugateNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.conjugate();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSin() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.sin();
        assertEquals(Math.sin(1.0) * MathUtils.cosh(2.0), result.getReal(), 1e-12);
        assertEquals(Math.cos(1.0) * MathUtils.sinh(2.0), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSinNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.sin();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testCos() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.cos();
        assertEquals(Math.cos(1.0) * MathUtils.cosh(2.0), result.getReal(), 1e-12);
        assertEquals(-Math.sin(1.0) * MathUtils.sinh(2.0), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCosNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.cos();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testTan() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.tan();
        // Verify using the formula: tan(a+bi) = sin(2a)/(cos(2a)+cosh(2b)) + [sinh(2b)/(cos(2a)+cosh(2b))]i
        double real2 = 2.0 * 1.0;
        double imag2 = 2.0 * 2.0;
        double d = Math.cos(real2) + MathUtils.cosh(imag2);
        double expectedReal = Math.sin(real2) / d;
        double expectedImag = MathUtils.sinh(imag2) / d;
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTanNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.tan();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSinh() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.sinh();
        assertEquals(MathUtils.sinh(1.0) * Math.cos(2.0), result.getReal(), 1e-12);
        assertEquals(MathUtils.cosh(1.0) * Math.sin(2.0), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSinhNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.sinh();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testCosh() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.cosh();
        assertEquals(MathUtils.cosh(1.0) * Math.cos(2.0), result.getReal(), 1e-12);
        assertEquals(MathUtils.sinh(1.0) * Math.sin(2.0), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCoshNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.cosh();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testTanh() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.tanh();
        // Verify using the formula: tanh(a+bi) = sinh(2a)/(cosh(2a)+cos(2b)) + [sin(2b)/(cosh(2a)+cos(2b))]i
        double real2 = 2.0 * 1.0;
        double imag2 = 2.0 * 2.0;
        double d = MathUtils.cosh(real2) + Math.cos(imag2);
        double expectedReal = MathUtils.sinh(real2) / d;
        double expectedImag = Math.sin(imag2) / d;
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTanhNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.tanh();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testExp() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.exp();
        double expReal = Math.exp(1.0);
        assertEquals(expReal * Math.cos(2.0), result.getReal(), 1e-12);
        assertEquals(expReal * Math.sin(2.0), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testExpNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.exp();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testLog() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.log();
        assertEquals(Math.log(Math.sqrt(5)), result.getReal(), 1e-12);
        assertEquals(Math.atan2(2.0, 1.0), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testLogNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.log();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSqrt() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.sqrt();
        Complex expected = c.pow(0.5);
        assertEquals(expected.getReal(), result.getReal(), 1e-12);
        assertEquals(expected.getImaginary(), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSqrtNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.sqrt();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSqrtZero() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.sqrt();
        assertEquals(0.0, result.getReal(), 0.0);
        assertEquals(0.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSqrt1z() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.sqrt1z();
        Complex expected = new Complex(1.0, 0.0).subtract(c.multiply(c)).sqrt();
        assertEquals(expected.getReal(), result.getReal(), 1e-12);
        assertEquals(expected.getImaginary(), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSqrt1zNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.sqrt1z();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testPowComplex() {
        Complex c = new Complex(1.0, 2.0);
        Complex exponent = new Complex(2.0, 3.0);
        Complex result = c.pow(exponent);
        Complex expected = c.log().multiply(exponent).exp();
        assertEquals(expected.getReal(), result.getReal(), 1e-12);
        assertEquals(expected.getImaginary(), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testPowDouble() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.pow(2.0);
        Complex expected = c.log().multiply(2.0).exp();
        assertEquals(expected.getReal(), result.getReal(), 1e-12);
        assertEquals(expected.getImaginary(), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testPowNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.pow(2.0);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testAtan() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.atan();
        // atan(z) = (i/2) * log((i+z)/(i-z))
        Complex expected = Complex.I.multiply(0.5).multiply(
            Complex.I.add(c).divide(Complex.I.subtract(c)).log()
        );
        assertEquals(expected.getReal(), result.getReal(), 1e-12);
        assertEquals(expected.getImaginary(), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAtanNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.atan();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testAcos() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.acos();
        // acos(z) = -i * log(z + i * sqrt(1-z^2))
        Complex expected = Complex.I.negate().multiply(
            c.add(Complex.I.multiply(c.sqrt1z())).log()
        );
        assertEquals(expected.getReal(), result.getReal(), 1e-12);
        assertEquals(expected.getImaginary(), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAcosNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.acos();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testAsin() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.asin();
        // asin(z) = -i * log(sqrt(1-z^2) + iz)
        Complex expected = Complex.I.negate().multiply(
            c.sqrt1z().add(Complex.I.multiply(c)).log()
        );
        assertEquals(expected.getReal(), result.getReal(), 1e-12);
        assertEquals(expected.getImaginary(), result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAsinNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        Complex result = c.asin();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testNthRoot() {
        Complex c = new Complex(1.0, 2.0);
        List<Complex> roots = c.nthRoot(3);
        assertEquals(3, roots.size());
        for (Complex root : roots) {
            Complex powered = root.pow(3);
            assertEquals(c.getReal(), powered.getReal(), 1e-12);
            assertEquals(c.getImaginary(), powered.getImaginary(), 1e-12);
        }
    }

    @Test(timeout = 4000)
    public void testNthRootNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        List<Complex> roots = c.nthRoot(3);
        assertEquals(1, roots.size());
        assertTrue(roots.get(0).isNaN());
    }

    @Test(timeout = 4000)
    public void testNthRootInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 2.0);
        List<Complex> roots = c.nthRoot(3);
        assertEquals(1, roots.size());
        assertTrue(roots.get(0).isInfinite());
    }

    @Test(timeout = 4000)
    public void testNthRootOne() {
        Complex c = new Complex(1.0, 2.0);
        List<Complex> roots = c.nthRoot(1);
        assertEquals(1, roots.size());
        assertEquals(c.getReal(), roots.get(0).getReal(), 1e-12);
        assertEquals(c.getImaginary(), roots.get(0).getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testNthRootLargeN() {
        Complex c = new Complex(1.0, 2.0);
        List<Complex> roots = c.nthRoot(10);
        assertEquals(10, roots.size());
    }
}