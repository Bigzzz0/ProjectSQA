package org.apache.commons.math3.complex;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Comprehensive JUnit 4 test suite for Complex class.
 * Targets maximum line/branch coverage and the known defect in reciprocal().
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (add, subtract, multiply, divide, conjugate, negate, reciprocal)
 * - Partition B: Boundary values (NaN, Infinity, zero, large/small magnitudes)
 * - Partition C: Defect-targeted: reciprocal(0) must return NaN, not Infinity
 * - Partition D: Exception paths (null arguments, NotPositiveException)
 * - Partition E: Object contract (equals, hashCode, toString, valueOf, serialization via readResolve)
 *
 * Known defect: Complex.ZERO.reciprocal() returns (Infinity, Infinity) instead of (NaN, NaN).
 */
public class ComplexDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testAddComplex() {
        Complex a = new Complex(3, 4);
        Complex b = new Complex(1, -2);
        Complex result = a.add(b);
        assertEquals(4.0, result.getReal(), 1e-12);
        assertEquals(2.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAddDouble() {
        Complex a = new Complex(3, 4);
        Complex result = a.add(2.0);
        assertEquals(5.0, result.getReal(), 1e-12);
        assertEquals(4.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSubtractComplex() {
        Complex a = new Complex(5, 6);
        Complex b = new Complex(2, 3);
        Complex result = a.subtract(b);
        assertEquals(3.0, result.getReal(), 1e-12);
        assertEquals(3.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSubtractDouble() {
        Complex a = new Complex(5, 6);
        Complex result = a.subtract(1.0);
        assertEquals(4.0, result.getReal(), 1e-12);
        assertEquals(6.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testMultiplyComplex() {
        Complex a = new Complex(1, 2);
        Complex b = new Complex(3, 4);
        Complex result = a.multiply(b);
        assertEquals(-5.0, result.getReal(), 1e-12);
        assertEquals(10.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testMultiplyDouble() {
        Complex a = new Complex(2, -3);
        Complex result = a.multiply(4.0);
        assertEquals(8.0, result.getReal(), 1e-12);
        assertEquals(-12.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testMultiplyInt() {
        Complex a = new Complex(2, -3);
        Complex result = a.multiply(3);
        assertEquals(6.0, result.getReal(), 1e-12);
        assertEquals(-9.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDivideComplex() {
        Complex a = new Complex(1, 2);
        Complex b = new Complex(3, 4);
        Complex result = a.divide(b);
        assertEquals(0.44, result.getReal(), 1e-12);
        assertEquals(0.08, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDivideDouble() {
        Complex a = new Complex(6, 8);
        Complex result = a.divide(2.0);
        assertEquals(3.0, result.getReal(), 1e-12);
        assertEquals(4.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testConjugate() {
        Complex a = new Complex(3, -4);
        Complex result = a.conjugate();
        assertEquals(3.0, result.getReal(), 1e-12);
        assertEquals(4.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testNegate() {
        Complex a = new Complex(3, -4);
        Complex result = a.negate();
        assertEquals(-3.0, result.getReal(), 1e-12);
        assertEquals(4.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testReciprocalNormal() {
        Complex a = new Complex(2, 0);
        Complex result = a.reciprocal();
        assertEquals(0.5, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testNaNState() {
        Complex nan = new Complex(Double.NaN, 0);
        assertTrue(nan.isNaN());
        assertFalse(nan.isInfinite());
        Complex nan2 = new Complex(0, Double.NaN);
        assertTrue(nan2.isNaN());
    }

    @Test(timeout = 4000)
    public void testInfiniteState() {
        Complex inf = new Complex(Double.POSITIVE_INFINITY, 0);
        assertTrue(inf.isInfinite());
        assertFalse(inf.isNaN());
        Complex inf2 = new Complex(0, Double.NEGATIVE_INFINITY);
        assertTrue(inf2.isInfinite());
    }

    @Test(timeout = 4000)
    public void testAbsNaN() {
        assertEquals(Double.NaN, Complex.NaN.abs(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAbsInfinite() {
        assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAbsNormal() {
        Complex a = new Complex(3, 4);
        assertEquals(5.0, a.abs(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAbsLargeImag() {
        Complex a = new Complex(1, 1e10);
        double expected = Math.sqrt(1 + 1e20);
        assertEquals(expected, a.abs(), 1e-5);
    }

    @Test(timeout = 4000)
    public void testAbsLargeReal() {
        Complex a = new Complex(1e10, 1);
        double expected = Math.sqrt(1e20 + 1);
        assertEquals(expected, a.abs(), 1e-5);
    }

    @Test(timeout = 4000)
    public void testAbsZero() {
        assertEquals(0.0, Complex.ZERO.abs(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddNaN() {
        assertSame(Complex.NaN, Complex.NaN.add(Complex.ONE));
        assertSame(Complex.NaN, Complex.ONE.add(Complex.NaN));
    }

    @Test(timeout = 4000)
    public void testAddDoubleNaN() {
        assertSame(Complex.NaN, Complex.NaN.add(1.0));
        assertSame(Complex.NaN, Complex.ONE.add(Double.NaN));
    }

    @Test(timeout = 4000)
    public void testSubtractNaN() {
        assertSame(Complex.NaN, Complex.NaN.subtract(Complex.ONE));
        assertSame(Complex.NaN, Complex.ONE.subtract(Complex.NaN));
    }

    @Test(timeout = 4000)
    public void testSubtractDoubleNaN() {
        assertSame(Complex.NaN, Complex.NaN.subtract(1.0));
        assertSame(Complex.NaN, Complex.ONE.subtract(Double.NaN));
    }

    @Test(timeout = 4000)
    public void testMultiplyNaN() {
        assertSame(Complex.NaN, Complex.NaN.multiply(Complex.ONE));
        assertSame(Complex.NaN, Complex.ONE.multiply(Complex.NaN));
    }

    @Test(timeout = 4000)
    public void testMultiplyDoubleNaN() {
        assertSame(Complex.NaN, Complex.NaN.multiply(1.0));
        assertSame(Complex.NaN, Complex.ONE.multiply(Double.NaN));
    }

    @Test(timeout = 4000)
    public void testMultiplyIntNaN() {
        assertSame(Complex.NaN, Complex.NaN.multiply(2));
    }

    @Test(timeout = 4000)
    public void testDivideNaN() {
        assertSame(Complex.NaN, Complex.NaN.divide(Complex.ONE));
        assertSame(Complex.NaN, Complex.ONE.divide(Complex.NaN));
    }

    @Test(timeout = 4000)
    public void testDivideDoubleNaN() {
        assertSame(Complex.NaN, Complex.NaN.divide(1.0));
        assertSame(Complex.NaN, Complex.ONE.divide(Double.NaN));
    }

    @Test(timeout = 4000)
    public void testDivideByZero() {
        Complex result = Complex.ONE.divide(Complex.ZERO);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideDoubleByZero() {
        Complex result = Complex.ONE.divide(0.0);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideInfiniteDivisor() {
        Complex result = Complex.ONE.divide(Complex.INF);
        assertEquals(Complex.ZERO, result);
    }

    @Test(timeout = 4000)
    public void testDivideDoubleInfinite() {
        Complex result = Complex.ONE.divide(Double.POSITIVE_INFINITY);
        assertEquals(Complex.ZERO, result);
    }

    @Test(timeout = 4000)
    public void testMultiplyInfinite() {
        Complex result = Complex.INF.multiply(Complex.ONE);
        assertSame(Complex.INF, result);
    }

    @Test(timeout = 4000)
    public void testMultiplyDoubleInfinite() {
        Complex result = Complex.INF.multiply(2.0);
        assertSame(Complex.INF, result);
    }

    @Test(timeout = 4000)
    public void testMultiplyIntInfinite() {
        Complex result = Complex.INF.multiply(2);
        assertSame(Complex.INF, result);
    }

    @Test(timeout = 4000)
    public void testConjugateNaN() {
        assertSame(Complex.NaN, Complex.NaN.conjugate());
    }

    @Test(timeout = 4000)
    public void testNegateNaN() {
        assertSame(Complex.NaN, Complex.NaN.negate());
    }

    @Test(timeout = 4000)
    public void testReciprocalNaN() {
        assertSame(Complex.NaN, Complex.NaN.reciprocal());
    }

    @Test(timeout = 4000)
    public void testReciprocalInfinite() {
        assertSame(Complex.ZERO, Complex.INF.reciprocal());
    }

    @Test(timeout = 4000)
    public void testGetArgument() {
        assertEquals(Math.PI / 4, new Complex(1, 1).getArgument(), 1e-12);
        assertEquals(-Math.PI / 4, new Complex(1, -1).getArgument(), 1e-12);
        assertEquals(Math.PI, new Complex(-1, 0).getArgument(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetArgumentNaN() {
        assertTrue(Double.isNaN(Complex.NaN.getArgument()));
    }

    @Test(timeout = 4000)
    public void testNthRootNormal() {
        Complex z = new Complex(1, 0);
        List<Complex> roots = z.nthRoot(4);
        assertEquals(4, roots.size());
        for (Complex root : roots) {
            assertEquals(1.0, root.abs(), 1e-12);
        }
    }

    @Test(timeout = 4000)
    public void testNthRootNaN() {
        List<Complex> roots = Complex.NaN.nthRoot(3);
        assertEquals(1, roots.size());
        assertSame(Complex.NaN, roots.get(0));
    }

    @Test(timeout = 4000)
    public void testNthRootInfinite() {
        List<Complex> roots = Complex.INF.nthRoot(2);
        assertEquals(1, roots.size());
        assertSame(Complex.INF, roots.get(0));
    }

    @Test(expected = org.apache.commons.math3.exception.NotPositiveException.class, timeout = 4000)
    public void testNthRootNonPositive() {
        Complex.ONE.nthRoot(0);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testReciprocalZero() {
        // Known defect: reciprocal of zero should be NaN, not Infinity
        Complex result = Complex.ZERO.reciprocal();
        assertTrue("reciprocal(0) must be NaN", result.isNaN());
        // Also verify both parts are NaN
        assertTrue(Double.isNaN(result.getReal()));
        assertTrue(Double.isNaN(result.getImaginary()));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = org.apache.commons.math3.exception.NullArgumentException.class, timeout = 4000)
    public void testAddNull() {
        Complex.ONE.add(null);
    }

    @Test(expected = org.apache.commons.math3.exception.NullArgumentException.class, timeout = 4000)
    public void testSubtractNull() {
        Complex.ONE.subtract(null);
    }

    @Test(expected = org.apache.commons.math3.exception.NullArgumentException.class, timeout = 4000)
    public void testMultiplyNull() {
        Complex.ONE.multiply(null);
    }

    @Test(expected = org.apache.commons.math3.exception.NullArgumentException.class, timeout = 4000)
    public void testDivideNull() {
        Complex.ONE.divide(null);
    }

    @Test(expected = org.apache.commons.math3.exception.NullArgumentException.class, timeout = 4000)
    public void testPowNull() {
        Complex.ONE.pow((Complex) null);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        Complex a = new Complex(2, 3);
        assertEquals(a, a);
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetric() {
        Complex a = new Complex(2, 3);
        Complex b = new Complex(2, 3);
        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test(timeout = 4000)
    public void testEqualsNaN() {
        Complex nan1 = new Complex(Double.NaN, 0);
        Complex nan2 = new Complex(0, Double.NaN);
        assertEquals(nan1, nan2);
        assertEquals(Complex.NaN, nan1);
    }

    @Test(timeout = 4000)
    public void testEqualsNotInstance() {
        assertFalse(Complex.ONE.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        assertFalse(Complex.ONE.equals(null));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistent() {
        Complex a = new Complex(2, 3);
        Complex b = new Complex(2, 3);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeNaN() {
        assertEquals(7, Complex.NaN.hashCode());
        assertEquals(7, new Complex(Double.NaN, 1).hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Complex a = new Complex(1.5, -2.5);
        assertEquals("(1.5, -2.5)", a.toString());
    }

    @Test(timeout = 4000)
    public void testValueOfDouble() {
        Complex a = Complex.valueOf(3.0);
        assertEquals(3.0, a.getReal(), 1e-12);
        assertEquals(0.0, a.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testValueOfDoubleDouble() {
        Complex a = Complex.valueOf(1.0, 2.0);
        assertEquals(1.0, a.getReal(), 1e-12);
        assertEquals(2.0, a.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testValueOfNaN() {
        assertSame(Complex.NaN, Complex.valueOf(Double.NaN));
        assertSame(Complex.NaN, Complex.valueOf(1.0, Double.NaN));
    }

    @Test(timeout = 4000)
    public void testReadResolve() {
        Complex original = new Complex(3, 4);
        Complex resolved = original.createComplex(3, 4);
        assertEquals(original, resolved);
    }

    @Test(timeout = 4000)
    public void testGetField() {
        assertSame(ComplexField.getInstance(), Complex.ONE.getField());
    }

    // ========== Trigonometric and Transcendental Functions ==========

    @Test(timeout = 4000)
    public void testSin() {
        Complex z = new Complex(1, 2);
        Complex result = z.sin();
        double expectedReal = Math.sin(1) * Math.cosh(2);
        double expectedImag = Math.cos(1) * Math.sinh(2);
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCos() {
        Complex z = new Complex(1, 2);
        Complex result = z.cos();
        double expectedReal = Math.cos(1) * Math.cosh(2);
        double expectedImag = -Math.sin(1) * Math.sinh(2);
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTan() {
        Complex z = new Complex(1, 0.5);
        Complex result = z.tan();
        double real2 = 2.0 * 1;
        double imag2 = 2.0 * 0.5;
        double d = Math.cos(real2) + Math.cosh(imag2);
        double expectedReal = Math.sin(real2) / d;
        double expectedImag = Math.sinh(imag2) / d;
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTanLargeImag() {
        Complex z = new Complex(1, 30);
        Complex result = z.tan();
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(1.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTanLargeNegImag() {
        Complex z = new Complex(1, -30);
        Complex result = z.tan();
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(-1.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTanInfiniteReal() {
        Complex z = new Complex(Double.POSITIVE_INFINITY, 1);
        assertTrue(z.tan().isNaN());
    }

    @Test(timeout = 4000)
    public void testSinh() {
        Complex z = new Complex(1, 2);
        Complex result = z.sinh();
        double expectedReal = Math.sinh(1) * Math.cos(2);
        double expectedImag = Math.cosh(1) * Math.sin(2);
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCosh() {
        Complex z = new Complex(1, 2);
        Complex result = z.cosh();
        double expectedReal = Math.cosh(1) * Math.cos(2);
        double expectedImag = Math.sinh(1) * Math.sin(2);
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTanh() {
        Complex z = new Complex(1, 0.5);
        Complex result = z.tanh();
        double real2 = 2.0 * 1;
        double imag2 = 2.0 * 0.5;
        double d = Math.cosh(real2) + Math.cos(imag2);
        double expectedReal = Math.sinh(real2) / d;
        double expectedImag = Math.sin(imag2) / d;
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTanhLargeReal() {
        Complex z = new Complex(30, 1);
        Complex result = z.tanh();
        assertEquals(1.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTanhLargeNegReal() {
        Complex z = new Complex(-30, 1);
        Complex result = z.tanh();
        assertEquals(-1.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTanhInfiniteImag() {
        Complex z = new Complex(1, Double.POSITIVE_INFINITY);
        assertTrue(z.tanh().isNaN());
    }

    @Test(timeout = 4000)
    public void testExp() {
        Complex z = new Complex(1, Math.PI / 2);
        Complex result = z.exp();
        double expReal = Math.exp(1);
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(expReal, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testLog() {
        Complex z = new Complex(1, 1);
        Complex result = z.log();
        double expectedReal = Math.log(Math.sqrt(2));
        double expectedImag = Math.PI / 4;
        assertEquals(expectedReal, result.getReal(), 1e-12);
        assertEquals(expectedImag, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSqrt() {
        Complex z = new Complex(3, 4);
        Complex result = z.sqrt();
        assertEquals(2.0, result.getReal(), 1e-12);
        assertEquals(1.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSqrtZero() {
        Complex result = Complex.ZERO.sqrt();
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSqrtNegativeReal() {
        Complex z = new Complex(-1, 0);
        Complex result = z.sqrt();
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(1.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSqrt1z() {
        Complex z = new Complex(0, 0);
        Complex result = z.sqrt1z();
        assertEquals(1.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testPowComplex() {
        Complex base = new Complex(2, 0);
        Complex exponent = new Complex(3, 0);
        Complex result = base.pow(exponent);
        assertEquals(8.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testPowDouble() {
        Complex base = new Complex(2, 0);
        Complex result = base.pow(3.0);
        assertEquals(8.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAcos() {
        Complex z = new Complex(0.5, 0);
        Complex result = z.acos();
        assertEquals(Math.acos(0.5), result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAsin() {
        Complex z = new Complex(0.5, 0);
        Complex result = z.asin();
        assertEquals(Math.asin(0.5), result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAtan() {
        Complex z = new Complex(1, 0);
        Complex result = z.atan();
        assertEquals(Math.PI / 4, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTrigNaN() {
        assertTrue(Complex.NaN.sin().isNaN());
        assertTrue(Complex.NaN.cos().isNaN());
        assertTrue(Complex.NaN.tan().isNaN());
        assertTrue(Complex.NaN.asin().isNaN());
        assertTrue(Complex.NaN.acos().isNaN());
        assertTrue(Complex.NaN.atan().isNaN());
        assertTrue(Complex.NaN.sinh().isNaN());
        assertTrue(Complex.NaN.cosh().isNaN());
        assertTrue(Complex.NaN.tanh().isNaN());
        assertTrue(Complex.NaN.exp().isNaN());
        assertTrue(Complex.NaN.log().isNaN());
        assertTrue(Complex.NaN.sqrt().isNaN());
        assertTrue(Complex.NaN.sqrt1z().isNaN());
    }
}