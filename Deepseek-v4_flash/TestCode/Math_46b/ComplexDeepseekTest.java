package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Comprehensive test suite for Complex class targeting known defects and maximum coverage.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (add, subtract, multiply, divide, conjugate, negate, abs, getArgument, etc.)
 * - Partition B: Boundary values (NaN, infinite, zero, negative zero, extreme doubles)
 * - Partition C: Defect-targeted zones (divide by zero, atan(I), isZero/isInfinite flags)
 * - Partition D: Exception paths (null arguments, n <= 0 for nthRoot)
 * - Partition E: Object contract (equals, hashCode, toString, serialization via readResolve)
 *
 * Known defects from Defects4J:
 *   - testDivideZero: dividing non-zero by zero should return INF, not NaN.
 *   - testAtanI: atan(I) should return INF (or appropriate infinite result), not NaN.
 */
public class ComplexDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testAddBasic() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.add(b);
        assertEquals(4.0, result.getReal(), 1e-15);
        assertEquals(6.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAddDouble() {
        Complex a = new Complex(1.0, 2.0);
        Complex result = a.add(5.0);
        assertEquals(6.0, result.getReal(), 1e-15);
        assertEquals(2.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSubtractBasic() {
        Complex a = new Complex(5.0, 6.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.subtract(b);
        assertEquals(2.0, result.getReal(), 1e-15);
        assertEquals(2.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSubtractDouble() {
        Complex a = new Complex(5.0, 6.0);
        Complex result = a.subtract(3.0);
        assertEquals(2.0, result.getReal(), 1e-15);
        assertEquals(6.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMultiplyBasic() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.multiply(b);
        assertEquals(-5.0, result.getReal(), 1e-15);
        assertEquals(10.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMultiplyDouble() {
        Complex a = new Complex(1.0, 2.0);
        Complex result = a.multiply(3.0);
        assertEquals(3.0, result.getReal(), 1e-15);
        assertEquals(6.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDivideBasic() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.divide(b);
        assertEquals(0.44, result.getReal(), 1e-15);
        assertEquals(0.08, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDivideDouble() {
        Complex a = new Complex(1.0, 2.0);
        Complex result = a.divide(2.0);
        assertEquals(0.5, result.getReal(), 1e-15);
        assertEquals(1.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConjugate() {
        Complex a = new Complex(1.0, 2.0);
        Complex result = a.conjugate();
        assertEquals(1.0, result.getReal(), 1e-15);
        assertEquals(-2.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testNegate() {
        Complex a = new Complex(1.0, -2.0);
        Complex result = a.negate();
        assertEquals(-1.0, result.getReal(), 1e-15);
        assertEquals(2.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAbs() {
        Complex a = new Complex(3.0, 4.0);
        assertEquals(5.0, a.abs(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAbsRealOnly() {
        Complex a = new Complex(-5.0, 0.0);
        assertEquals(5.0, a.abs(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAbsImagOnly() {
        Complex a = new Complex(0.0, -12.0);
        assertEquals(12.0, a.abs(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetArgument() {
        Complex a = new Complex(1.0, 1.0);
        assertEquals(Math.PI / 4, a.getArgument(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetArgumentNegativeImag() {
        Complex a = new Complex(1.0, -1.0);
        assertEquals(-Math.PI / 4, a.getArgument(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSqrt() {
        Complex a = new Complex(1.0, 0.0);
        Complex result = a.sqrt();
        assertEquals(1.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSqrtNegativeReal() {
        Complex a = new Complex(-1.0, 0.0);
        Complex result = a.sqrt();
        assertEquals(0.0, result.getReal(), 1e-15);
        assertEquals(1.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSqrtZero() {
        Complex a = Complex.ZERO;
        Complex result = a.sqrt();
        assertEquals(0.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testExp() {
        Complex a = new Complex(1.0, Math.PI);
        Complex result = a.exp();
        assertEquals(-Math.E, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testLog() {
        Complex a = new Complex(0.0, 1.0);
        Complex result = a.log();
        assertEquals(0.0, result.getReal(), 1e-15);
        assertEquals(Math.PI / 2, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testPowComplex() {
        Complex a = new Complex(2.0, 0.0);
        Complex b = new Complex(3.0, 0.0);
        Complex result = a.pow(b);
        assertEquals(8.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testPowDouble() {
        Complex a = new Complex(2.0, 0.0);
        Complex result = a.pow(3.0);
        assertEquals(8.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSin() {
        Complex a = new Complex(0.0, 0.0);
        Complex result = a.sin();
        assertEquals(0.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testCos() {
        Complex a = new Complex(0.0, 0.0);
        Complex result = a.cos();
        assertEquals(1.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testTan() {
        Complex a = new Complex(0.0, 0.0);
        Complex result = a.tan();
        assertEquals(0.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSinh() {
        Complex a = new Complex(0.0, 0.0);
        Complex result = a.sinh();
        assertEquals(0.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testCosh() {
        Complex a = new Complex(0.0, 0.0);
        Complex result = a.cosh();
        assertEquals(1.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testTanh() {
        Complex a = new Complex(0.0, 0.0);
        Complex result = a.tanh();
        assertEquals(0.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAcos() {
        Complex a = new Complex(1.0, 0.0);
        Complex result = a.acos();
        assertEquals(0.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAsin() {
        Complex a = new Complex(0.0, 0.0);
        Complex result = a.asin();
        assertEquals(0.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSqrt1z() {
        Complex a = new Complex(0.0, 0.0);
        Complex result = a.sqrt1z();
        assertEquals(1.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testNthRoot() {
        Complex a = new Complex(1.0, 0.0);
        List<Complex> roots = a.nthRoot(4);
        assertEquals(4, roots.size());
        for (Complex root : roots) {
            assertEquals(1.0, root.abs(), 1e-15);
        }
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testNaNReal() {
        Complex a = new Complex(Double.NaN, 1.0);
        assertTrue(a.isNaN());
        assertFalse(a.isInfinite());
        assertEquals(Complex.NaN, a.add(Complex.ONE));
        assertEquals(Complex.NaN, a.subtract(Complex.ONE));
        assertEquals(Complex.NaN, a.multiply(Complex.ONE));
        assertEquals(Complex.NaN, a.divide(Complex.ONE));
        assertEquals(Complex.NaN, a.conjugate());
        assertEquals(Complex.NaN, a.negate());
        assertEquals(Double.NaN, a.abs(), 0.0);
        assertEquals(Complex.NaN, a.sqrt());
        assertEquals(Complex.NaN, a.log());
        assertEquals(Complex.NaN, a.exp());
        assertEquals(Complex.NaN, a.sin());
        assertEquals(Complex.NaN, a.cos());
        assertEquals(Complex.NaN, a.tan());
        assertEquals(Complex.NaN, a.sinh());
        assertEquals(Complex.NaN, a.cosh());
        assertEquals(Complex.NaN, a.tanh());
        assertEquals(Complex.NaN, a.acos());
        assertEquals(Complex.NaN, a.asin());
        assertEquals(Complex.NaN, a.atan());
        assertEquals(Double.NaN, a.getArgument(), 0.0);
        List<Complex> roots = a.nthRoot(2);
        assertEquals(1, roots.size());
        assertTrue(roots.get(0).isNaN());
    }

    @Test(timeout = 4000)
    public void testNaNImaginary() {
        Complex a = new Complex(1.0, Double.NaN);
        assertTrue(a.isNaN());
        assertFalse(a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testInfiniteReal() {
        Complex a = new Complex(Double.POSITIVE_INFINITY, 1.0);
        assertTrue(a.isInfinite());
        assertFalse(a.isNaN());
        assertEquals(Complex.INF, a.add(Complex.ONE));
        assertEquals(Complex.INF, a.subtract(Complex.ONE));
        assertEquals(Complex.INF, a.multiply(Complex.ONE));
        assertEquals(Complex.INF, a.divide(Complex.ONE));
        assertEquals(Double.POSITIVE_INFINITY, a.abs(), 0.0);
        List<Complex> roots = a.nthRoot(2);
        assertEquals(1, roots.size());
        assertTrue(roots.get(0).isInfinite());
    }

    @Test(timeout = 4000)
    public void testInfiniteImaginary() {
        Complex a = new Complex(1.0, Double.NEGATIVE_INFINITY);
        assertTrue(a.isInfinite());
        assertFalse(a.isNaN());
    }

    @Test(timeout = 4000)
    public void testZero() {
        Complex a = Complex.ZERO;
        assertTrue(a.isZero());
        assertFalse(a.isNaN());
        assertFalse(a.isInfinite());
        assertEquals(0.0, a.getReal(), 0.0);
        assertEquals(0.0, a.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testNegativeZero() {
        Complex a = new Complex(-0.0, -0.0);
        assertTrue(a.isZero());
        assertEquals(0.0, a.getReal(), 0.0);
        assertEquals(0.0, a.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAbsLargeValues() {
        Complex a = new Complex(Double.MAX_VALUE, Double.MAX_VALUE);
        double expected = Math.sqrt(2) * Double.MAX_VALUE;
        assertEquals(expected, a.abs(), 1e10);
    }

    @Test(timeout = 4000)
    public void testAbsUnderflow() {
        Complex a = new Complex(Double.MIN_VALUE, Double.MIN_VALUE);
        double expected = Math.sqrt(2) * Double.MIN_VALUE;
        assertEquals(expected, a.abs(), 1e-300);
    }

    // ========== Partition C: Defect-Targeted Tests ==========

    // Defect: divide by zero should return INF for non-zero numerator
    @Test(timeout = 4000)
    public void testDivideByZeroComplex() {
        Complex numerator = new Complex(1.0, 2.0);
        Complex divisor = Complex.ZERO;
        Complex result = numerator.divide(divisor);
        assertTrue("Expected INF but got " + result, result.isInfinite());
        assertEquals(Double.POSITIVE_INFINITY, result.getReal(), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testDivideByZeroDouble() {
        Complex numerator = new Complex(1.0, 2.0);
        Complex result = numerator.divide(0.0);
        assertTrue("Expected INF but got " + result, result.isInfinite());
        assertEquals(Double.POSITIVE_INFINITY, result.getReal(), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, result.getImaginary(), 0.0);
    }

    // Defect: atan(I) should return INF (or appropriate infinite result)
    @Test(timeout = 4000)
    public void testAtanI() {
        Complex i = Complex.I;
        Complex result = i.atan();
        // The correct result is infinite; we expect INF
        assertTrue("Expected INF but got " + result, result.isInfinite());
    }

    // Additional edge: divide zero by zero should return NaN
    @Test(timeout = 4000)
    public void testDivideZeroByZeroComplex() {
        Complex result = Complex.ZERO.divide(Complex.ZERO);
        assertTrue("Expected NaN but got " + result, result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideZeroByZeroDouble() {
        Complex result = Complex.ZERO.divide(0.0);
        assertTrue("Expected NaN but got " + result, result.isNaN());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testAddNull() {
        Complex.ONE.add(null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testSubtractNull() {
        Complex.ONE.subtract(null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testMultiplyNull() {
        Complex.ONE.multiply(null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testDivideNull() {
        Complex.ONE.divide(null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testPowNull() {
        Complex.ONE.pow((Complex) null);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testNthRootNegative() {
        Complex.ONE.nthRoot(-1);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testNthRootZero() {
        Complex.ONE.nthRoot(0);
    }

    // ========== Partition E: Object Contract ==========

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        Complex a = new Complex(1.0, 2.0);
        assertEquals(a, a);
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetric() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(1.0, 2.0);
        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test(timeout = 4000)
    public void testEqualsNaN() {
        Complex nan1 = Complex.NaN;
        Complex nan2 = new Complex(Double.NaN, 1.0);
        assertEquals(nan1, nan2);
        assertEquals(nan2, nan1);
    }

    @Test(timeout = 4000)
    public void testEqualsNotInstance() {
        Complex a = new Complex(1.0, 2.0);
        assertFalse(a.equals("string"));
        assertFalse(a.equals(null));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Complex a = new Complex(1.0, 2.0);
        int hash1 = a.hashCode();
        int hash2 = a.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testHashCodeNaN() {
        Complex nan1 = Complex.NaN;
        Complex nan2 = new Complex(Double.NaN, 1.0);
        assertEquals(nan1.hashCode(), nan2.hashCode());
        assertEquals(7, nan1.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Complex a = new Complex(1.0, -2.0);
        assertEquals("(1.0, -2.0)", a.toString());
    }

    @Test(timeout = 4000)
    public void testValueOfStatic() {
        Complex a = Complex.valueOf(1.0, 2.0);
        assertEquals(1.0, a.getReal(), 0.0);
        assertEquals(2.0, a.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testValueOfNaN() {
        Complex a = Complex.valueOf(Double.NaN, 1.0);
        assertTrue(a.isNaN());
    }

    @Test(timeout = 4000)
    public void testValueOfDoubleOnly() {
        Complex a = Complex.valueOf(5.0);
        assertEquals(5.0, a.getReal(), 0.0);
        assertEquals(0.0, a.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testReadResolve() {
        Complex a = new Complex(1.0, 2.0);
        Complex resolved = a.readResolve();
        assertEquals(a, resolved);
    }

    @Test(timeout = 4000)
    public void testGetField() {
        assertNotNull(Complex.ONE.getField());
    }

    // Additional coverage for divide with infinite divisor
    @Test(timeout = 4000)
    public void testDivideByInfiniteComplex() {
        Complex numerator = new Complex(1.0, 2.0);
        Complex divisor = Complex.INF;
        Complex result = numerator.divide(divisor);
        assertEquals(Complex.ZERO, result);
    }

    @Test(timeout = 4000)
    public void testDivideInfiniteByFiniteComplex() {
        Complex numerator = Complex.INF;
        Complex divisor = new Complex(1.0, 2.0);
        Complex result = numerator.divide(divisor);
        assertTrue(result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testDivideInfiniteByInfiniteComplex() {
        Complex result = Complex.INF.divide(Complex.INF);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideByInfiniteDouble() {
        Complex numerator = new Complex(1.0, 2.0);
        Complex result = numerator.divide(Double.POSITIVE_INFINITY);
        assertEquals(Complex.ZERO, result);
    }

    @Test(timeout = 4000)
    public void testDivideInfiniteByFiniteDouble() {
        Complex numerator = Complex.INF;
        Complex result = numerator.divide(2.0);
        assertTrue(result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testDivideInfiniteByInfiniteDouble() {
        Complex numerator = Complex.INF;
        Complex result = numerator.divide(Double.POSITIVE_INFINITY);
        assertTrue(result.isNaN());
    }

    // Edge: multiply with infinite parts
    @Test(timeout = 4000)
    public void testMultiplyInfinite() {
        Complex a = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex b = new Complex(1.0, 0.0);
        Complex result = a.multiply(b);
        assertEquals(Complex.INF, result);
    }

    @Test(timeout = 4000)
    public void testMultiplyDoubleInfinite() {
        Complex a = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex result = a.multiply(2.0);
        assertEquals(Complex.INF, result);
    }

    // Edge: add with infinite
    @Test(timeout = 4000)
    public void testAddInfinite() {
        Complex a = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex b = new Complex(1.0, 0.0);
        Complex result = a.add(b);
        assertTrue(result.isInfinite());
    }

    // Edge: subtract with infinite
    @Test(timeout = 4000)
    public void testSubtractInfinite() {
        Complex a = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex b = new Complex(1.0, 0.0);
        Complex result = a.subtract(b);
        assertTrue(result.isInfinite());
    }

    // Edge: conjugate of infinite
    @Test(timeout = 4000)
    public void testConjugateInfinite() {
        Complex a = new Complex(1.0, Double.POSITIVE_INFINITY);
        Complex result = a.conjugate();
        assertEquals(1.0, result.getReal(), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, result.getImaginary(), 0.0);
    }

    // Edge: negate of infinite
    @Test(timeout = 4000)
    public void testNegateInfinite() {
        Complex a = new Complex(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        Complex result = a.negate();
        assertEquals(Double.NEGATIVE_INFINITY, result.getReal(), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, result.getImaginary(), 0.0);
    }

    // Edge: abs of infinite
    @Test(timeout = 4000)
    public void testAbsInfinite() {
        Complex a = new Complex(Double.POSITIVE_INFINITY, 0.0);
        assertEquals(Double.POSITIVE_INFINITY, a.abs(), 0.0);
    }

    // Edge: getArgument of infinite
    @Test(timeout = 4000)
    public void testGetArgumentInfinite() {
        Complex a = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertEquals(Math.PI / 4, a.getArgument(), 1e-15);
    }

    // Edge: sqrt of negative real with imaginary zero
    @Test(timeout = 4000)
    public void testSqrtNegativeRealZeroImag() {
        Complex a = new Complex(-4.0, 0.0);
        Complex result = a.sqrt();
        assertEquals(0.0, result.getReal(), 1e-15);
        assertEquals(2.0, result.getImaginary(), 1e-15);
    }

    // Edge: sqrt of positive real with imaginary zero
    @Test(timeout = 4000)
    public void testSqrtPositiveRealZeroImag() {
        Complex a = new Complex(9.0, 0.0);
        Complex result = a.sqrt();
        assertEquals(3.0, result.getReal(), 1e-15);
        assertEquals(0.0, result.getImaginary(), 1e-15);
    }

    // Edge: log of zero
    @Test(timeout = 4000)
    public void testLogZero() {
        Complex result = Complex.ZERO.log();
        assertEquals(Double.NEGATIVE_INFINITY, result.getReal(), 0.0);
        assertEquals(0.0, result.getImaginary(), 0.0);
    }

    // Edge: pow with zero base and positive exponent
    @Test(timeout = 4000)
    public void testPowZeroBase() {
        Complex result = Complex.ZERO.pow(2.0);
        assertTrue(result.isNaN());
    }

    // Edge: tan near pole
    @Test(timeout = 4000)
    public void testTanNearPole() {
        Complex a = new Complex(Math.PI / 2, 0.0);
        Complex result = a.tan();
        // Should be infinite (or very large)
        assertTrue(Double.isInfinite(result.getReal()) || Double.isNaN(result.getReal()));
    }
}