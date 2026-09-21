package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.apache.commons.math.complex.Complex
 * 
 * Known Defect: testAddNaN - AssertionFailedError in add() method
 * The defect is in the add() method which is missing from the provided source
 * but referenced in the defect specification. The add() method should return
 * Complex.NaN when either operand is NaN, but the defective version likely
 * fails to do so.
 * 
 * Branch Coverage Targets:
 * - add(): null check, isNaN branches, infinite handling, finite arithmetic
 * - multiply(): NaN check, infinite check, finite multiplication
 * - divide(): null check, NaN check, zero divisor, infinite handling, prescaling branches
 * - sqrt(): NaN check, zero check, real >= 0 branch, real < 0 branch
 * - log(): NaN check, normal computation
 * - exp(): NaN check, normal computation
 * - sin()/cos()/tan()/sinh()/cosh()/tanh(): NaN checks, formula branches
 * - equals(): identity, NaN handling, value comparison, non-Complex
 * - hashCode(): NaN hash, normal hash
 * - getArgument(): atan2 behavior
 * - nthRoot(): n<=0 exception, NaN, infinite, normal roots
 * - createComplex(): factory method
 * - getReal()/getImaginary(): accessors
 * - isNaN()/isInfinite(): state flags
 * - negate(): NaN check, negation
 * - subtract(): null check, NaN check, subtraction
 * - conjugate(): NaN check, conjugation
 * - sqrt1z(): computation
 * - asin()/acos()/atan(): NaN checks, formula execution
 * - cosh()/sinh(): NaN checks, formula execution
 * - tanh(): NaN check, formula execution
 * - pow(): null check, computation
 * - getField(): field instance
 * - toString(): string representation
 * - readResolve(): deserialization
 * 
 * Boundary Values:
 * - Zero: (0,0), (0,±INF), (±INF,0)
 * - NaN: (NaN, x), (x, NaN), (NaN, NaN)
 * - Infinity: (±INF, ±INF), (±INF, y), (x, ±INF)
 * - Negative: (-1, -1), (-0.0, -0.0)
 * - Large/Small: Double.MAX_VALUE, Double.MIN_VALUE
 * - n values: 0, 1, 2, 3, negative, large
 * 
 * Defect-Specific Test:
 * testAddNaN - Verifies that adding a NaN complex number returns Complex.NaN
 * This directly targets the known defect where add() fails to handle NaN
 * correctly.
 */
public class ComplexDeepseekTest {

    /* ==================== PART A: CORE FUNCTIONAL LOGIC & STATE ==================== */

    @Test(timeout = 4000)
    public void testConstructorAndAccessors() {
        Complex c = new Complex(3.5, -2.0);
        assertEquals(3.5, c.getReal(), 0.0);
        assertEquals(-2.0, c.getImaginary(), 0.0);
        assertFalse(c.isNaN());
        assertFalse(c.isInfinite());
    }

    @Test(timeout = 4000)
    public void testConstructorNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        assertTrue(c.isNaN());
        assertFalse(c.isInfinite());
    }

    @Test(timeout = 4000)
    public void testConstructorInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        assertFalse(c.isNaN());
        assertTrue(c.isInfinite());
    }

    @Test(timeout = 4000)
    public void testCreateComplex() {
        Complex c = new Complex(1.0, 2.0);
        Complex created = c.createComplex(3.0, 4.0);
        assertEquals(3.0, created.getReal(), 0.0);
        assertEquals(4.0, created.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetField() {
        Complex c = new Complex(1.0, 2.0);
        assertNotNull(c.getField());
        assertEquals(ComplexField.getInstance(), c.getField());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Complex c = new Complex(1.5, -2.5);
        assertEquals("(1.5, -2.5)", c.toString());
    }

    @Test(timeout = 4000)
    public void testReadResolve() {
        Complex c = new Complex(1.0, 2.0);
        Complex resolved = c.readResolve();
        assertEquals(c, resolved);
        assertNotSame(c, resolved);
    }

    /* ==================== PART B: BOUNDARY VALUE ANALYSIS & EXTREMES ==================== */

    @Test(timeout = 4000)
    public void testConstants() {
        assertTrue(Complex.I.isNaN() == false);
        assertTrue(Complex.NaN.isNaN());
        assertTrue(Complex.INF.isInfinite());
        assertEquals(1.0, Complex.ONE.getReal(), 0.0);
        assertEquals(0.0, Complex.ZERO.getReal(), 0.0);
    }

    @Test(timeout = 4000)
    public void testZeroBoundary() {
        Complex zero = new Complex(0.0, 0.0);
        assertEquals(0.0, zero.abs(), 0.0);
        assertEquals(0.0, zero.getArgument(), 0.0);
    }

    @Test(timeout = 4000)
    public void testMaxValueBoundary() {
        Complex max = new Complex(Double.MAX_VALUE, Double.MAX_VALUE);
        assertTrue(Double.isInfinite(max.abs()));
    }

    @Test(timeout = 4000)
    public void testMinValueBoundary() {
        Complex min = new Complex(Double.MIN_VALUE, Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE * Math.sqrt(2), min.abs(), Double.MIN_VALUE * 2);
    }

    @Test(timeout = 4000)
    public void testNegativeZero() {
        Complex negZero = new Complex(-0.0, -0.0);
        assertEquals(0.0, negZero.abs(), 0.0);
        assertEquals(-0.0, negZero.getReal(), 0.0);
        assertEquals(-0.0, negZero.getImaginary(), 0.0);
    }

    /* ==================== PART C: DEFECT-TARGETED BRANCH ZONE ==================== */

    /**
     * CRITICAL DEFECT TEST:
     * Tests that add() correctly returns Complex.NaN when either operand is NaN.
     * This directly targets the known defect testAddNaN failure.
     */
    @Test(timeout = 4000)
    public void testAddNaN() {
        Complex c = new Complex(1.0, 2.0);
        Complex nan = Complex.NaN;
        
        // Adding NaN to a finite complex number should return NaN
        Complex result = c.add(nan);
        assertTrue("Adding NaN should return NaN", result.isNaN());
        
        // Adding a finite complex number to NaN should return NaN
        result = nan.add(c);
        assertTrue("Adding to NaN should return NaN", result.isNaN());
        
        // Adding two NaN values should return NaN
        result = nan.add(nan);
        assertTrue("Adding NaN to NaN should return NaN", result.isNaN());
        
        // Verify the result is the same NaN instance
        assertSame("Should return the NaN constant", Complex.NaN, result);
    }

    @Test(timeout = 4000)
    public void testAddNormal() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.add(b);
        assertEquals(4.0, result.getReal(), 0.0);
        assertEquals(6.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddNull() {
        Complex c = new Complex(1.0, 2.0);
        try {
            c.add(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // Expected
        }
    }

    /* ==================== PART D: EXCEPTION & DEFENSIVE GUARD PATHS ==================== */

    @Test(timeout = 4000)
    public void testNthRootInvalidN() {
        Complex c = new Complex(1.0, 0.0);
        try {
            c.nthRoot(0);
            fail("Expected IllegalArgumentException for n=0");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        try {
            c.nthRoot(-1);
            fail("Expected IllegalArgumentException for negative n");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNthRootNaN() {
        Complex nan = Complex.NaN;
        List<Complex> roots = nan.nthRoot(3);
        assertEquals(1, roots.size());
        assertTrue(roots.get(0).isNaN());
    }

    @Test(timeout = 4000)
    public void testNthRootInfinite() {
        Complex inf = Complex.INF;
        List<Complex> roots = inf.nthRoot(3);
        assertEquals(1, roots.size());
        assertTrue(roots.get(0).isInfinite());
    }

    @Test(timeout = 4000)
    public void testNthRootNormal() {
        Complex c = new Complex(1.0, 0.0);
        List<Complex> roots = c.nthRoot(4);
        assertEquals(4, roots.size());
        // Verify all roots raised to 4th power return original
        for (Complex root : roots) {
            Complex powered = root.multiply(root).multiply(root).multiply(root);
            assertEquals(1.0, powered.getReal(), 1e-9);
            assertEquals(0.0, powered.getImaginary(), 1e-9);
        }
    }

    @Test(timeout = 4000)
    public void testMultiplyNull() {
        Complex c = new Complex(1.0, 2.0);
        try {
            c.multiply((Complex) null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testDivideNull() {
        Complex c = new Complex(1.0, 2.0);
        try {
            c.divide(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testPowNull() {
        Complex c = new Complex(1.0, 2.0);
        try {
            c.pow(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // Expected
        }
    }

    /* ==================== PART E: OBJECT LIFECYCLE & CONTRACT INTEGRITY ==================== */

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        Complex c = new Complex(1.0, 2.0);
        assertEquals(c, c);
    }

    @Test(timeout = 4000)
    public void testEqualsEqualValues() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentValues() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 3.0);
        assertNotEquals(c1, c2);
    }

    @Test(timeout = 4000)
    public void testEqualsNaN() {
        Complex nan1 = new Complex(Double.NaN, 1.0);
        Complex nan2 = new Complex(1.0, Double.NaN);
        Complex nan3 = Complex.NaN;
        
        assertEquals(nan1, nan2);
        assertEquals(nan1, nan3);
        assertEquals(nan1.hashCode(), nan2.hashCode());
        assertEquals(nan1.hashCode(), nan3.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsNonComplex() {
        Complex c = new Complex(1.0, 2.0);
        assertNotEquals(c, "not a complex");
        assertNotEquals(c, null);
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Complex c = new Complex(3.0, 4.0);
        int hash1 = c.hashCode();
        int hash2 = c.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testHashCodeNaN() {
        Complex nan1 = new Complex(Double.NaN, 1.0);
        Complex nan2 = new Complex(2.0, Double.NaN);
        assertEquals(7, nan1.hashCode());
        assertEquals(7, nan2.hashCode());
    }

    /* ==================== ADDITIONAL COVERAGE TESTS ==================== */

    @Test(timeout = 4000)
    public void testSqrtNaN() {
        Complex result = Complex.NaN.sqrt();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSqrtZero() {
        Complex zero = new Complex(0.0, 0.0);
        Complex result = zero.sqrt();
        assertEquals(0.0, result.getReal(), 0.0);
        assertEquals(0.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSqrtPositiveReal() {
        Complex c = new Complex(4.0, 0.0);
        Complex result = c.sqrt();
        assertEquals(2.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSqrtNegativeReal() {
        Complex c = new Complex(-4.0, 0.0);
        Complex result = c.sqrt();
        assertEquals(0.0, result.getReal(), 1e-9);
        assertEquals(2.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSqrtImaginary() {
        Complex c = new Complex(0.0, 4.0);
        Complex result = c.sqrt();
        assertEquals(Math.sqrt(2), result.getReal(), 1e-9);
        assertEquals(Math.sqrt(2), result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testLogNaN() {
        Complex result = Complex.NaN.log();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testLogNormal() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.log();
        assertEquals(0.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testExpNaN() {
        Complex result = Complex.NaN.exp();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testExpNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.exp();
        assertEquals(1.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSinNaN() {
        Complex result = Complex.NaN.sin();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSinNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.sin();
        assertEquals(0.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testCosNaN() {
        Complex result = Complex.NaN.cos();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testCosNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.cos();
        assertEquals(1.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testTanNaN() {
        Complex result = Complex.NaN.tan();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testTanNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.tan();
        assertEquals(0.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSinhNaN() {
        Complex result = Complex.NaN.sinh();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSinhNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.sinh();
        assertEquals(0.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testCoshNaN() {
        Complex result = Complex.NaN.cosh();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testCoshNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.cosh();
        assertEquals(1.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testTanhNaN() {
        Complex result = Complex.NaN.tanh();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testTanhNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.tanh();
        assertEquals(0.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testAsinNaN() {
        Complex result = Complex.NaN.asin();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testAsinNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.asin();
        assertEquals(0.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testAcosNaN() {
        Complex result = Complex.NaN.acos();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testAcosNormal() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.acos();
        assertEquals(0.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testAtanNaN() {
        Complex result = Complex.NaN.atan();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testAtanNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.atan();
        assertEquals(0.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSqrt1z() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.sqrt1z();
        assertEquals(1.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testPowNormal() {
        Complex base = new Complex(2.0, 0.0);
        Complex exp = new Complex(2.0, 0.0);
        Complex result = base.pow(exp);
        assertEquals(4.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testMultiplyScalar() {
        Complex c = new Complex(2.0, 3.0);
        Complex result = c.multiply(2.0);
        assertEquals(4.0, result.getReal(), 0.0);
        assertEquals(6.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testMultiplyScalarNaN() {
        Complex c = new Complex(2.0, 3.0);
        Complex result = c.multiply(Double.NaN);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testMultiplyScalarInfinite() {
        Complex c = new Complex(2.0, 3.0);
        Complex result = c.multiply(Double.POSITIVE_INFINITY);
        assertTrue(result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testNegate() {
        Complex c = new Complex(2.0, -3.0);
        Complex result = c.negate();
        assertEquals(-2.0, result.getReal(), 0.0);
        assertEquals(3.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testNegateNaN() {
        Complex result = Complex.NaN.negate();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSubtract() {
        Complex a = new Complex(5.0, 7.0);
        Complex b = new Complex(2.0, 3.0);
        Complex result = a.subtract(b);
        assertEquals(3.0, result.getReal(), 0.0);
        assertEquals(4.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtractNaN() {
        Complex a = new Complex(5.0, 7.0);
        Complex result = a.subtract(Complex.NaN);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testConjugate() {
        Complex c = new Complex(2.0, 3.0);
        Complex result = c.conjugate();
        assertEquals(2.0, result.getReal(), 0.0);
        assertEquals(-3.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testConjugateNaN() {
        Complex result = Complex.NaN.conjugate();
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testGetArgumentNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        assertTrue(Double.isNaN(c.getArgument()));
    }

    @Test(timeout = 4000)
    public void testGetArgumentPositive() {
        Complex c = new Complex(1.0, 1.0);
        assertEquals(Math.PI / 4, c.getArgument(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testGetArgumentNegative() {
        Complex c = new Complex(1.0, -1.0);
        assertEquals(-Math.PI / 4, c.getArgument(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testGetArgumentInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        assertEquals(0.0, c.getArgument(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testIsInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        assertTrue(c.isInfinite());
        assertFalse(c.isNaN());
    }

    @Test(timeout = 4000)
    public void testIsNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        assertTrue(c.isNaN());
        assertFalse(c.isInfinite());
    }

    @Test(timeout = 4000)
    public void testMultiplyComplex() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.multiply(b);
        assertEquals(-5.0, result.getReal(), 1e-9);
        assertEquals(10.0, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testMultiplyComplexNaN() {
        Complex a = new Complex(1.0, 2.0);
        Complex result = a.multiply(Complex.NaN);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testMultiplyComplexInfinite() {
        Complex a = new Complex(1.0, 2.0);
        Complex result = a.multiply(Complex.INF);
        assertTrue(result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testDivide() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.divide(b);
        assertEquals(0.44, result.getReal(), 1e-9);
        assertEquals(0.08, result.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testDivideNaN() {
        Complex a = new Complex(1.0, 2.0);
        Complex result = a.divide(Complex.NaN);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideByZero() {
        Complex a = new Complex(1.0, 2.0);
        Complex result = a.divide(Complex.ZERO);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideInfiniteRHS() {
        Complex a = new Complex(1.0, 2.0);
        Complex result = a.divide(Complex.INF);
        assertEquals(0.0, result.getReal(), 0.0);
        assertEquals(0.0, result.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testDivideInfiniteLHS() {
        Complex a = Complex.INF;
        Complex b = new Complex(1.0, 2.0);
        Complex result = a.divide(b);
        assertTrue(result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testDivideBothInfinite() {
        Complex result = Complex.INF.divide(Complex.INF);
        assertTrue(result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDividePrescaling() {
        Complex a = new Complex(Double.MAX_VALUE, Double.MAX_VALUE);
        Complex b = new Complex(Double.MAX_VALUE, Double.MAX_VALUE);
        Complex result = a.divide(b);
        assertEquals(1.0, result.getReal(), 1e-9);
        assertEquals(0.0, result.getImaginary(), 1e-9);
    }
}