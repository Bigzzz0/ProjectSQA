package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive white-box test suite for Complex class.
 * Targets line/branch coverage and the known Defects4J defect (testMath221).
 *
 * [Branch & Defect Analysis Matrix]
 * - abs(): NaN guard, infinite guard, scaling branches (|real|<|imag| vs >=), zero checks.
 * - add/subtract/multiply/divide: NaN/infinite guards, zero divisor, prescaling branches.
 * - equals/hashCode: NaN handling, -0.0 vs 0.0, raw bits comparison, hash consistency.
 * - conjugate/negate: NaN guard.
 * - trigonometric/hyperbolic: NaN guard, formula correctness.
 * - sqrt: zero guard, real>=0 branch, indicator usage.
 * - log/pow: NaN guard, atan2 usage.
 * - Known defect: testMath221 failure due to incorrect result in divide or equals.
 *   We target divide with zero imaginary part and negative real part, and also
 *   verify equals/hashCode contract for edge cases.
 */
public class ComplexDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State ====================

    @Test(timeout = 4000)
    public void testAbsNormal() {
        Complex z = new Complex(3.0, 4.0);
        assertEquals(5.0, z.abs(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAbsRealZero() {
        Complex z = new Complex(0.0, 5.0);
        assertEquals(5.0, z.abs(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAbsImagZero() {
        Complex z = new Complex(-4.0, 0.0);
        assertEquals(4.0, z.abs(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAbsBothZero() {
        Complex z = new Complex(0.0, 0.0);
        assertEquals(0.0, z.abs(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAddNormal() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.add(b);
        assertEquals(4.0, result.getReal(), 1e-12);
        assertEquals(6.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSubtractNormal() {
        Complex a = new Complex(5.0, 6.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.subtract(b);
        assertEquals(2.0, result.getReal(), 1e-12);
        assertEquals(2.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testMultiplyNormal() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.multiply(b);
        assertEquals(-5.0, result.getReal(), 1e-12);
        assertEquals(10.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDivideNormal() {
        Complex a = new Complex(1.0, 0.0);
        Complex b = new Complex(1.0, 1.0);
        Complex result = a.divide(b);
        assertEquals(0.5, result.getReal(), 1e-12);
        assertEquals(-0.5, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testConjugateNormal() {
        Complex z = new Complex(2.0, 3.0);
        Complex conj = z.conjugate();
        assertEquals(2.0, conj.getReal(), 1e-12);
        assertEquals(-3.0, conj.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testNegateNormal() {
        Complex z = new Complex(2.0, -3.0);
        Complex neg = z.negate();
        assertEquals(-2.0, neg.getReal(), 1e-12);
        assertEquals(3.0, neg.getImaginary(), 1e-12);
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testAbsNaN() {
        Complex z = new Complex(Double.NaN, 0.0);
        assertTrue(Double.isNaN(z.abs()));
    }

    @Test(timeout = 4000)
    public void testAbsInfinite() {
        Complex z = new Complex(Double.POSITIVE_INFINITY, 0.0);
        assertEquals(Double.POSITIVE_INFINITY, z.abs(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAbsInfiniteBoth() {
        Complex z = new Complex(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, z.abs(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddNaN() {
        Complex a = new Complex(1.0, Double.NaN);
        Complex b = new Complex(2.0, 3.0);
        Complex result = a.add(b);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSubtractNaN() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(Double.NaN, 3.0);
        Complex result = a.subtract(b);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testMultiplyNaN() {
        Complex a = new Complex(1.0, Double.NaN);
        Complex b = new Complex(2.0, 3.0);
        Complex result = a.multiply(b);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testMultiplyInfinite() {
        Complex a = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex b = new Complex(1.0, 2.0);
        Complex result = a.multiply(b);
        assertSame(Complex.INF, result);
    }

    @Test(timeout = 4000)
    public void testDivideByZero() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = Complex.ZERO;
        Complex result = a.divide(b);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideInfiniteDivisor() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex result = a.divide(b);
        assertSame(Complex.ZERO, result);
    }

    @Test(timeout = 4000)
    public void testDivideBothInfinite() {
        Complex a = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex b = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex result = a.divide(b);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSqrtZero() {
        Complex z = Complex.ZERO;
        Complex sqrt = z.sqrt();
        assertEquals(0.0, sqrt.getReal(), 1e-12);
        assertEquals(0.0, sqrt.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSqrtPositiveReal() {
        Complex z = new Complex(4.0, 0.0);
        Complex sqrt = z.sqrt();
        assertEquals(2.0, sqrt.getReal(), 1e-12);
        assertEquals(0.0, sqrt.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSqrtNegativeReal() {
        Complex z = new Complex(-4.0, 0.0);
        Complex sqrt = z.sqrt();
        assertEquals(0.0, sqrt.getReal(), 1e-12);
        assertEquals(2.0, sqrt.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSqrtNegativeRealPositiveImag() {
        Complex z = new Complex(-4.0, 3.0);
        Complex sqrt = z.sqrt();
        // Expected: sqrt = sqrt((|z|+real)/2) + i*sign(imag)*sqrt((|z|-real)/2)
        double abs = z.abs();
        double t = Math.sqrt((abs + Math.abs(z.getReal())) / 2.0);
        double expectedReal = Math.abs(z.getImaginary()) / (2.0 * t);
        double expectedImag = Math.signum(z.getImaginary()) * t;
        assertEquals(expectedReal, sqrt.getReal(), 1e-12);
        assertEquals(expectedImag, sqrt.getImaginary(), 1e-12);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Targets the known Defects4J defect (testMath221).
     * The defect likely involves divide with a divisor having zero imaginary part
     * and negative real part, or equals/hashCode inconsistency.
     * We test divide by a negative real number and verify the result.
     */
    @Test(timeout = 4000)
    public void testMath221() {
        // Case: divide by a negative real number (c < 0, d = 0)
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(-3.0, 0.0);
        Complex result = a.divide(b);
        // Expected: (1+2i)/(-3) = -1/3 - (2/3)i
        assertEquals(-1.0/3.0, result.getReal(), 1e-12);
        assertEquals(-2.0/3.0, result.getImaginary(), 1e-12);
        // Also verify equals/hashCode consistency for this result
        Complex expected = new Complex(-1.0/3.0, -2.0/3.0);
        assertEquals(expected, result);
        assertEquals(expected.hashCode(), result.hashCode());
    }

    @Test(timeout = 4000)
    public void testDivideWithZeroImagAndPositiveReal() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 0.0);
        Complex result = a.divide(b);
        assertEquals(1.0/3.0, result.getReal(), 1e-12);
        assertEquals(2.0/3.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDivideWithZeroRealAndNonZeroImag() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(0.0, 4.0);
        Complex result = a.divide(b);
        // (1+2i)/(0+4i) = (1+2i)/(4i) = (1/(4i) + 2i/(4i)) = -i/4 + 1/2 = 0.5 - 0.25i
        assertEquals(0.5, result.getReal(), 1e-12);
        assertEquals(-0.25, result.getImaginary(), 1e-12);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAddNull() {
        new Complex(1.0, 2.0).add(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testSubtractNull() {
        new Complex(1.0, 2.0).subtract(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testMultiplyNull() {
        new Complex(1.0, 2.0).multiply(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testDivideNull() {
        new Complex(1.0, 2.0).divide(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPowNull() {
        new Complex(1.0, 2.0).pow(null);
    }

    @Test(timeout = 4000)
    public void testPowZeroBase() {
        Complex result = Complex.ZERO.pow(new Complex(2.0, 0.0));
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testLogZero() {
        Complex result = Complex.ZERO.log();
        assertEquals(Double.NEGATIVE_INFINITY, result.getReal(), 0.0);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testLogNegativeReal() {
        Complex z = new Complex(-1.0, 0.0);
        Complex result = z.log();
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(Math.PI, result.getImaginary(), 1e-12);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        Complex z = new Complex(1.0, 2.0);
        assertTrue(z.equals(z));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Complex z = new Complex(1.0, 2.0);
        assertFalse(z.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        Complex z = new Complex(1.0, 2.0);
        assertFalse(z.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsExactMatch() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(1.0, 2.0);
        assertTrue(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentReal() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(1.5, 2.0);
        assertFalse(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentImag() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(1.0, 2.5);
        assertFalse(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testEqualsNegativeZero() {
        Complex a = new Complex(0.0, 0.0);
        Complex b = new Complex(-0.0, 0.0);
        // According to spec, -0.0 and 0.0 are not exactly the same (bitwise different)
        assertFalse(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testEqualsNaN() {
        Complex a = new Complex(Double.NaN, 0.0);
        Complex b = new Complex(Double.NaN, Double.NaN);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }

    @Test(timeout = 4000)
    public void testEqualsNaNvsNonNaN() {
        Complex a = new Complex(Double.NaN, 0.0);
        Complex b = new Complex(1.0, 2.0);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Complex a = new Complex(3.0, 4.0);
        Complex b = new Complex(3.0, 4.0);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeNaN() {
        Complex a = new Complex(Double.NaN, 0.0);
        Complex b = new Complex(Double.NaN, Double.NaN);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(7, a.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeDifferentValues() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(2.0, 1.0);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testGetters() {
        Complex z = new Complex(1.5, -2.5);
        assertEquals(1.5, z.getReal(), 1e-12);
        assertEquals(-2.5, z.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testIsNaN() {
        assertTrue(new Complex(Double.NaN, 0.0).isNaN());
        assertTrue(new Complex(0.0, Double.NaN).isNaN());
        assertFalse(new Complex(1.0, 2.0).isNaN());
    }

    @Test(timeout = 4000)
    public void testIsInfinite() {
        assertTrue(new Complex(Double.POSITIVE_INFINITY, 0.0).isInfinite());
        assertTrue(new Complex(0.0, Double.NEGATIVE_INFINITY).isInfinite());
        assertFalse(new Complex(1.0, 2.0).isInfinite());
        assertFalse(new Complex(Double.NaN, Double.POSITIVE_INFINITY).isInfinite());
    }

    @Test(timeout = 4000)
    public void testConstants() {
        assertEquals(0.0, Complex.I.getReal(), 1e-12);
        assertEquals(1.0, Complex.I.getImaginary(), 1e-12);
        assertTrue(Complex.NaN.isNaN());
        assertTrue(Complex.INF.isInfinite());
        assertEquals(1.0, Complex.ONE.getReal(), 1e-12);
        assertEquals(0.0, Complex.ONE.getImaginary(), 1e-12);
        assertEquals(0.0, Complex.ZERO.getReal(), 1e-12);
        assertEquals(0.0, Complex.ZERO.getImaginary(), 1e-12);
    }

    // Additional tests for trigonometric functions to ensure coverage

    @Test(timeout = 4000)
    public void testSinNormal() {
        Complex z = new Complex(1.0, 0.5);
        Complex result = z.sin();
        double expectedReal = Math.sin(1.0) * MathUtils.cosh(0.5);
        double expectedImag = Math.cos(1.0) * MathUtils.sinh(0.5);
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCosNormal() {
        Complex z = new Complex(0.5, 1.0);
        Complex result = z.cos();
        double expectedReal = Math.cos(0.5) * MathUtils.cosh(1.0);
        double expectedImag = -Math.sin(0.5) * MathUtils.sinh(1.0);
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTanNormal() {
        Complex z = new Complex(0.5, 0.5);
        Complex result = z.tan();
        double real2 = 2.0 * z.getReal();
        double imag2 = 2.0 * z.getImaginary();
        double d = Math.cos(real2) + MathUtils.cosh(imag2);
        double expectedReal = Math.sin(real2) / d;
        double expectedImag = MathUtils.sinh(imag2) / d;
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSinhNormal() {
        Complex z = new Complex(0.3, 0.4);
        Complex result = z.sinh();
        double expectedReal = MathUtils.sinh(0.3) * Math.cos(0.4);
        double expectedImag = MathUtils.cosh(0.3) * Math.sin(0.4);
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCoshNormal() {
        Complex z = new Complex(0.3, 0.4);
        Complex result = z.cosh();
        double expectedReal = MathUtils.cosh(0.3) * Math.cos(0.4);
        double expectedImag = MathUtils.sinh(0.3) * Math.sin(0.4);
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTanhNormal() {
        Complex z = new Complex(0.5, 0.5);
        Complex result = z.tanh();
        double real2 = 2.0 * z.getReal();
        double imag2 = 2.0 * z.getImaginary();
        double d = MathUtils.cosh(real2) + Math.cos(imag2);
        double expectedReal = MathUtils.sinh(real2) / d;
        double expectedImag = Math.sin(imag2) / d;
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testExpNormal() {
        Complex z = new Complex(1.0, 0.5);
        Complex result = z.exp();
        double expReal = Math.exp(1.0);
        double expectedReal = expReal * Math.cos(0.5);
        double expectedImag = expReal * Math.sin(0.5);
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testLogNormal() {
        Complex z = new Complex(3.0, 4.0);
        Complex result = z.log();
        double expectedReal = Math.log(z.abs());
        double expectedImag = Math.atan2(4.0, 3.0);
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testPowNormal() {
        Complex base = new Complex(2.0, 0.0);
        Complex exp = new Complex(3.0, 0.0);
        Complex result = base.pow(exp);
        assertEquals(8.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAcosNormal() {
        Complex z = new Complex(0.5, 0.0);
        Complex result = z.acos();
        // acos(0.5) = 1.0471975511965976 (real), imag 0
        assertEquals(1.0471975511965976, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAsinNormal() {
        Complex z = new Complex(0.5, 0.0);
        Complex result = z.asin();
        // asin(0.5) = 0.5235987755982989 (real), imag 0
        assertEquals(0.5235987755982989, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAtanNormal() {
        Complex z = new Complex(1.0, 0.0);
        Complex result = z.atan();
        // atan(1) = pi/4 = 0.7853981633974483
        assertEquals(Math.PI / 4.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSqrt1zNormal() {
        Complex z = new Complex(0.0, 0.0);
        Complex result = z.sqrt1z();
        assertEquals(1.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTrigonometricNaN() {
        Complex z = new Complex(Double.NaN, 0.0);
        assertTrue(z.sin().isNaN());
        assertTrue(z.cos().isNaN());
        assertTrue(z.tan().isNaN());
        assertTrue(z.sinh().isNaN());
        assertTrue(z.cosh().isNaN());
        assertTrue(z.tanh().isNaN());
        assertTrue(z.exp().isNaN());
        assertTrue(z.log().isNaN());
        assertTrue(z.acos().isNaN());
        assertTrue(z.asin().isNaN());
        assertTrue(z.atan().isNaN());
        assertTrue(z.sqrt().isNaN());
        assertTrue(z.sqrt1z().isNaN());
    }
}