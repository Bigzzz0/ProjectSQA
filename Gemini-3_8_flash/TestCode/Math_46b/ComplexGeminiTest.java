package org.apache.commons.math.complex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.math.exception.NotPositiveException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Methods & Branches Targeted:
 * 1. atan():
 *    - Formula: (i/2) * log((i + z)/(i - z)).
 *    - DEFECT (Defects4J testAtanI): When z = I, (i - z) == ZERO, causing log(INF) -> non-NaN result.
 *      Mathematical expectation is NaN because tan(pi/2) is undefined.
 * 2. divide(Complex) / divide(double):
 *    - DEFECT (Defects4J testDivideZero): Dividing Complex.ZERO by Complex.ZERO / 0.0 evaluated to NaN
 *      instead of INF according to legacy specification tests expecting Complex.INF.
 *    - Branches: divisor.isNaN, this.isNaN, divisor.isZero (this.isZero vs !this.isZero),
 *      divisor.isInfinite() vs this.isInfinite(), |c| < |d| vs |c| >= |d|.
 * 3. abs():
 *    - Branches: isNaN, isInfinite, |real| < |imaginary| (imaginary == 0.0 vs != 0.0),
 *      |real| >= |imaginary| (real == 0.0 vs != 0.0).
 * 4. multiply(Complex) / multiply(double):
 *    - Branches: isNaN, factor.isNaN, isInfinite branches (real, imaginary, factor.real, factor.imaginary).
 * 5. sqrt() & sqrt1z():
 *    - Branches: isNaN, real == 0 && imaginary == 0, real >= 0 vs real < 0, indicator(imaginary).
 * 6. nthRoot(int):
 *    - Defensive checks: n <= 0 (NotPositiveException), isNaN, isInfinite, normal k-loop generation.
 * 7. equals() & hashCode():
 *    - Branches: this == other, other not instanceof Complex, other.isNaN (this.isNaN vs !this.isNaN),
 *      real == c.real && imaginary == c.imaginary. Contract symmetry and hashCode(NaN) == 7.
 * 8. Trigs & Hyperbolics:
 *    - acos, asin, atan, cos, cosh, exp, log, sin, sinh, tan, tanh with NaN, Inf, and finite inputs.
 * -------------------------------------------------------------------------------------------------------
 */
public class ComplexGeminiTest {

    private static final double EPSILON = 1.0e-12;

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicAccessorsAndConstants() {
        Complex z = new Complex(3.0, -4.0);
        assertEquals(3.0, z.getReal(), EPSILON);
        assertEquals(-4.0, z.getImaginary(), EPSILON);
        assertFalse(z.isNaN());
        assertFalse(z.isInfinite());

        Complex realOnly = new Complex(5.0);
        assertEquals(5.0, realOnly.getReal(), EPSILON);
        assertEquals(0.0, realOnly.getImaginary(), EPSILON);

        assertEquals(0.0, Complex.I.getReal(), EPSILON);
        assertEquals(1.0, Complex.I.getImaginary(), EPSILON);
        assertEquals(1.0, Complex.ONE.getReal(), EPSILON);
        assertEquals(0.0, Complex.ONE.getImaginary(), EPSILON);
        assertEquals(0.0, Complex.ZERO.getReal(), EPSILON);
        assertEquals(0.0, Complex.ZERO.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAbsNormalAndZero() {
        Complex z1 = new Complex(3.0, 4.0); // |real| < |imaginary|
        assertEquals(5.0, z1.abs(), EPSILON);

        Complex z2 = new Complex(4.0, 3.0); // |real| >= |imaginary|
        assertEquals(5.0, z2.abs(), EPSILON);

        Complex z3 = new Complex(0.0, 0.0); // real == 0.0 branch in else
        assertEquals(0.0, z3.abs(), EPSILON);

        Complex z4 = new Complex(0.0, 6.0); // real == 0, imaginary != 0
        assertEquals(6.0, z4.abs(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAddAndSubtractNormal() {
        Complex a = new Complex(1.5, 2.5);
        Complex b = new Complex(3.5, -1.0);

        Complex sum = a.add(b);
        assertEquals(5.0, sum.getReal(), EPSILON);
        assertEquals(1.5, sum.getImaginary(), EPSILON);

        Complex sumDouble = a.add(2.5);
        assertEquals(4.0, sumDouble.getReal(), EPSILON);
        assertEquals(2.5, sumDouble.getImaginary(), EPSILON);

        Complex diff = a.subtract(b);
        assertEquals(-2.0, diff.getReal(), EPSILON);
        assertEquals(3.5, diff.getImaginary(), EPSILON);

        Complex diffDouble = a.subtract(1.5);
        assertEquals(0.0, diffDouble.getReal(), EPSILON);
        assertEquals(2.5, diffDouble.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testConjugateAndNegate() {
        Complex z = new Complex(2.0, -5.0);
        Complex conj = z.conjugate();
        assertEquals(2.0, conj.getReal(), EPSILON);
        assertEquals(5.0, conj.getImaginary(), EPSILON);

        Complex neg = z.negate();
        assertEquals(-2.0, neg.getReal(), EPSILON);
        assertEquals(5.0, neg.getImaginary(), EPSILON);

        Complex infImag = new Complex(1.0, Double.POSITIVE_INFINITY);
        Complex conjInf = infImag.conjugate();
        assertEquals(1.0, conjInf.getReal(), EPSILON);
        assertEquals(Double.NEGATIVE_INFINITY, conjInf.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testMultiplyNormal() {
        Complex a = new Complex(2.0, 3.0);
        Complex b = new Complex(4.0, -5.0);
        Complex product = a.multiply(b);
        // (2*4 - 3*(-5)) + (2*(-5) + 3*4)i = 8 + 15 + (-10 + 12)i = 23 + 2i
        assertEquals(23.0, product.getReal(), EPSILON);
        assertEquals(2.0, product.getImaginary(), EPSILON);

        Complex productDouble = a.multiply(2.5);
        assertEquals(5.0, productDouble.getReal(), EPSILON);
        assertEquals(7.5, productDouble.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDividePrescalingBranches() {
        // |c| < |d|
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result1 = a.divide(b);
        // (1*3 + 2*4)/25 + (2*3 - 1*4)/25 i = 11/25 + 2/25 i = 0.44 + 0.08i
        assertEquals(0.44, result1.getReal(), EPSILON);
        assertEquals(0.08, result1.getImaginary(), EPSILON);

        // |c| >= |d|
        Complex c = new Complex(4.0, 3.0);
        Complex result2 = a.divide(c);
        // (1*4 + 2*3)/25 + (2*4 - 1*3)/25 i = 10/25 + 5/25 i = 0.4 + 0.2i
        assertEquals(0.4, result2.getReal(), EPSILON);
        assertEquals(0.2, result2.getImaginary(), EPSILON);

        // divide(double)
        Complex resultDouble = a.divide(2.0);
        assertEquals(0.5, resultDouble.getReal(), EPSILON);
        assertEquals(1.0, resultDouble.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSqrtBranches() {
        // real == 0 && imaginary == 0
        Complex zeroSqrt = Complex.ZERO.sqrt();
        assertEquals(0.0, zeroSqrt.getReal(), EPSILON);
        assertEquals(0.0, zeroSqrt.getImaginary(), EPSILON);

        // real >= 0
        Complex z1 = new Complex(3.0, 4.0);
        Complex s1 = z1.sqrt();
        assertEquals(2.0, s1.getReal(), EPSILON);
        assertEquals(1.0, s1.getImaginary(), EPSILON);

        // real < 0, positive imaginary
        Complex z2 = new Complex(-3.0, 4.0);
        Complex s2 = z2.sqrt();
        assertEquals(1.0, s2.getReal(), EPSILON);
        assertEquals(2.0, s2.getImaginary(), EPSILON);

        // real < 0, negative imaginary
        Complex z3 = new Complex(-3.0, -4.0);
        Complex s3 = z3.sqrt();
        assertEquals(1.0, s3.getReal(), EPSILON);
        assertEquals(-2.0, s3.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testElementaryFunctions() {
        Complex z = new Complex(1.0, 1.0);

        // exp
        Complex expZ = z.exp();
        assertEquals(FastMath.exp(1.0) * FastMath.cos(1.0), expZ.getReal(), EPSILON);
        assertEquals(FastMath.exp(1.0) * FastMath.sin(1.0), expZ.getImaginary(), EPSILON);

        // log
        Complex logZ = z.log();
        assertEquals(FastMath.log(z.abs()), logZ.getReal(), EPSILON);
        assertEquals(FastMath.atan2(1.0, 1.0), logZ.getImaginary(), EPSILON);

        // pow
        Complex powZ = z.pow(2.0);
        Complex expectedPow = z.multiply(z);
        assertEquals(expectedPow.getReal(), powZ.getReal(), EPSILON);
        assertEquals(expectedPow.getImaginary(), powZ.getImaginary(), EPSILON);

        Complex powComplex = z.pow(Complex.ONE);
        assertEquals(z.getReal(), powComplex.getReal(), EPSILON);
        assertEquals(z.getImaginary(), powComplex.getImaginary(), EPSILON);

        // sin & cos
        Complex sinZ = z.sin();
        Complex cosZ = z.cos();
        assertFalse(sinZ.isNaN());
        assertFalse(cosZ.isNaN());

        // sinh & cosh
        Complex sinhZ = z.sinh();
        Complex coshZ = z.cosh();
        assertFalse(sinhZ.isNaN());
        assertFalse(coshZ.isNaN());

        // tan & tanh
        Complex tanZ = z.tan();
        Complex tanhZ = z.tanh();
        assertFalse(tanZ.isNaN());
        assertFalse(tanhZ.isNaN());

        // asin, acos, atan
        Complex asinZ = z.asin();
        Complex acosZ = z.acos();
        Complex atanZ = z.atan();
        assertFalse(asinZ.isNaN());
        assertFalse(acosZ.isNaN());
        assertFalse(atanZ.isNaN());

        // sqrt1z: sqrt(1 - z^2)
        Complex sqrt1zVal = z.sqrt1z();
        Complex expectedSqrt1z = Complex.ONE.subtract(z.multiply(z)).sqrt();
        assertEquals(expectedSqrt1z.getReal(), sqrt1zVal.getReal(), EPSILON);
        assertEquals(expectedSqrt1z.getImaginary(), sqrt1zVal.getImaginary(), EPSILON);

        // getArgument
        assertEquals(FastMath.atan2(1.0, 1.0), z.getArgument(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testNthRootNormal() {
        Complex z = new Complex(0.0, 8.0);
        List<Complex> roots = z.nthRoot(3);
        assertEquals(3, roots.size());
        for (Complex root : roots) {
            Complex cubed = root.multiply(root).multiply(root);
            assertEquals(0.0, cubed.getReal(), 1.0e-5);
            assertEquals(8.0, cubed.getImaginary(), 1.0e-5);
        }
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAbsBoundaryConditions() {
        assertTrue(Double.isNaN(Complex.NaN.abs()));
        assertTrue(Double.isNaN(new Complex(Double.NaN, 1.0).abs()));
        assertTrue(Double.isNaN(new Complex(1.0, Double.NaN).abs()));

        assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, new Complex(Double.POSITIVE_INFINITY, 0.0).abs(), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, new Complex(0.0, Double.NEGATIVE_INFINITY).abs(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testInfiniteMultiplicationBranches() {
        Complex finite = new Complex(2.0, 3.0);
        Complex infReal = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Complex infImag = new Complex(1.0, Double.NEGATIVE_INFINITY);

        assertTrue(infReal.isInfinite());
        assertTrue(infImag.isInfinite());

        // Infinity times finite -> INF
        assertEquals(Complex.INF, finite.multiply(infReal));
        assertEquals(Complex.INF, infImag.multiply(finite));

        // multiply(double) with infinity
        assertEquals(Complex.INF, finite.multiply(Double.POSITIVE_INFINITY));
        assertEquals(Complex.INF, finite.multiply(Double.NEGATIVE_INFINITY));
        assertEquals(Complex.INF, infReal.multiply(2.0));
        assertEquals(Complex.INF, infImag.multiply(2.0));
    }

    @Test(timeout = 4000)
    public void testInfiniteDivisionBranches() {
        Complex finite = new Complex(3.0, 4.0);

        // finite / infinite -> ZERO
        assertEquals(Complex.ZERO, finite.divide(Complex.INF));
        assertEquals(Complex.ZERO, finite.divide(new Complex(Double.POSITIVE_INFINITY, 0.0)));
        assertEquals(Complex.ZERO, finite.divide(Double.POSITIVE_INFINITY));
        assertEquals(Complex.ZERO, finite.divide(Double.NEGATIVE_INFINITY));

        // infinite / infinite -> NaN
        assertTrue(Complex.INF.divide(Complex.INF).isNaN());
        assertTrue(Complex.INF.divide(Double.POSITIVE_INFINITY).isNaN());
    }

    @Test(timeout = 4000)
    public void testNaNPropagationAcrossAllOperations() {
        Complex nanReal = new Complex(Double.NaN, 0.0);
        Complex nanImag = new Complex(0.0, Double.NaN);
        Complex normal = new Complex(1.0, 1.0);

        assertTrue(nanReal.isNaN());
        assertTrue(nanImag.isNaN());
        assertTrue(Complex.NaN.isNaN());

        // add / subtract
        assertTrue(normal.add(Complex.NaN).isNaN());
        assertTrue(Complex.NaN.add(normal).isNaN());
        assertTrue(normal.add(Double.NaN).isNaN());
        assertTrue(Complex.NaN.add(1.0).isNaN());

        assertTrue(normal.subtract(Complex.NaN).isNaN());
        assertTrue(Complex.NaN.subtract(normal).isNaN());
        assertTrue(normal.subtract(Double.NaN).isNaN());
        assertTrue(Complex.NaN.subtract(1.0).isNaN());

        // multiply / divide
        assertTrue(normal.multiply(Complex.NaN).isNaN());
        assertTrue(Complex.NaN.multiply(normal).isNaN());
        assertTrue(normal.multiply(Double.NaN).isNaN());
        assertTrue(Complex.NaN.multiply(1.0).isNaN());

        assertTrue(normal.divide(Complex.NaN).isNaN());
        assertTrue(Complex.NaN.divide(normal).isNaN());
        assertTrue(normal.divide(Double.NaN).isNaN());
        assertTrue(Complex.NaN.divide(1.0).isNaN());

        // conjugate / negate
        assertTrue(Complex.NaN.conjugate().isNaN());
        assertTrue(Complex.NaN.negate().isNaN());

        // mathematical functions
        assertTrue(Complex.NaN.sqrt().isNaN());
        assertTrue(Complex.NaN.sqrt1z().isNaN());
        assertTrue(Complex.NaN.exp().isNaN());
        assertTrue(Complex.NaN.log().isNaN());
        assertTrue(Complex.NaN.sin().isNaN());
        assertTrue(Complex.NaN.cos().isNaN());
        assertTrue(Complex.NaN.tan().isNaN());
        assertTrue(Complex.NaN.asin().isNaN());
        assertTrue(Complex.NaN.acos().isNaN());
        assertTrue(Complex.NaN.atan().isNaN());
        assertTrue(Complex.NaN.sinh().isNaN());
        assertTrue(Complex.NaN.cosh().isNaN());
        assertTrue(Complex.NaN.tanh().isNaN());

        // nthRoot of NaN
        List<Complex> nanRoots = Complex.NaN.nthRoot(2);
        assertEquals(1, nanRoots.size());
        assertTrue(nanRoots.get(0).isNaN());

        // nthRoot of INF
        List<Complex> infRoots = Complex.INF.nthRoot(2);
        assertEquals(1, infRoots.size());
        assertEquals(Complex.INF, infRoots.get(0));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Defects4J Target: org.apache.commons.math.complex.ComplexTest::testAtanI
     * Failure Reason: atan(I) implements formula: (i/2) log((i + z)/(i - z)).
     * At z = i, denominator is zero, triggering divide-by-zero path leading to INF
     * and log(INF) -> non-NaN result. Math standards specify atan(i) must be NaN.
     */
    @Test(timeout = 4000)
    public void testAtanI() {
        assertTrue("atan(I) must be NaN", Complex.I.atan().isNaN());
    }

    /**
     * Complementary defect guard: atan(-I) must also evaluate to NaN.
     */
    @Test(timeout = 4000)
    public void testAtanNegativeI() {
        assertTrue("atan(-I) must be NaN", new Complex(0.0, -1.0).atan().isNaN());
    }

    /**
     * Defects4J Target: org.apache.commons.math.complex.ComplexTest::testDivideZero
     * Failure Reason: Dividing ZERO by ZERO returned NaN instead of INF as expected by the legacy test.
     */
    @Test(timeout = 4000)
    public void testDivideZero() {
        Complex x = new Complex(3.0, 4.0);
        assertEquals(Complex.INF, x.divide(Complex.ZERO));
        assertEquals(Complex.INF, Complex.ZERO.divide(Complex.ZERO));
    }

    /**
     * Targeting divide(double) when divisor == 0d.
     */
    @Test(timeout = 4000)
    public void testDivideDoubleZero() {
        Complex x = new Complex(3.0, 4.0);
        assertEquals(Complex.INF, x.divide(0.0));
        assertEquals(Complex.INF, Complex.ZERO.divide(0.0));
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testAddNullThrows() {
        Complex.ONE.add((Complex) null);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testSubtractNullThrows() {
        Complex.ONE.subtract((Complex) null);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testMultiplyNullThrows() {
        Complex.ONE.multiply((Complex) null);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testDivideNullThrows() {
        Complex.ONE.divide((Complex) null);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testPowNullThrows() {
        Complex.ONE.pow((Complex) null);
    }

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testNthRootZeroThrows() {
        Complex.ONE.nthRoot(0);
    }

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testNthRootNegativeThrows() {
        Complex.ONE.nthRoot(-3);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle, Factory & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsContract() {
        Complex z1 = new Complex(1.0, 2.0);
        Complex z2 = new Complex(1.0, 2.0);
        Complex zDiffReal = new Complex(2.0, 2.0);
        Complex zDiffImag = new Complex(1.0, 3.0);

        // Reflexive
        assertEquals(z1, z1);

        // Symmetric & Consistent
        assertEquals(z1, z2);
        assertEquals(z2, z1);

        // Unequal values
        assertNotEquals(z1, zDiffReal);
        assertNotEquals(z1, zDiffImag);

        // Null and non-Complex type check
        assertFalse(z1.equals(null));
        assertFalse(z1.equals("NotAComplex"));

        // NaN equality specification: All NaNs are equal
        Complex nan1 = new Complex(Double.NaN, 0.0);
        Complex nan2 = new Complex(0.0, Double.NaN);
        Complex nan3 = Complex.NaN;

        assertEquals(nan1, nan2);
        assertEquals(nan1, nan3);
        assertEquals(nan3, nan1);
        assertNotEquals(z1, nan1);
        assertNotEquals(nan1, z1);
    }

    @Test(timeout = 4000)
    public void testHashCodeContract() {
        Complex z1 = new Complex(1.0, 2.0);
        Complex z2 = new Complex(1.0, 2.0);
        assertEquals(z1.hashCode(), z2.hashCode());

        // NaN hashcode must be 7
        assertEquals(7, Complex.NaN.hashCode());
        assertEquals(7, new Complex(Double.NaN, 1.0).hashCode());
        assertEquals(7, new Complex(1.0, Double.NaN).hashCode());
    }

    @Test(timeout = 4000)
    public void testValueOfFactories() {
        Complex val1 = Complex.valueOf(3.0, 4.0);
        assertEquals(3.0, val1.getReal(), EPSILON);
        assertEquals(4.0, val1.getImaginary(), EPSILON);

        Complex valRealOnly = Complex.valueOf(5.0);
        assertEquals(5.0, valRealOnly.getReal(), EPSILON);
        assertEquals(0.0, valRealOnly.getImaginary(), EPSILON);

        // NaN checks
        assertTrue(Complex.valueOf(Double.NaN, 1.0).isNaN());
        assertTrue(Complex.valueOf(1.0, Double.NaN).isNaN());
        assertTrue(Complex.valueOf(Double.NaN).isNaN());
    }

    @Test(timeout = 4000)
    public void testToStringAndField() {
        Complex z = new Complex(2.5, -3.5);
        assertEquals("(2.5, -3.5)", z.toString());

        ComplexField field = z.getField();
        assertNotNull(field);
        assertEquals(ComplexField.getInstance(), field);
    }

    @Test(timeout = 4000)
    public void testCreateComplexAndSerialization() throws Exception {
        Complex z = new Complex(1.5, 2.5);
        Complex created = z.createComplex(3.0, 4.0);
        assertEquals(3.0, created.getReal(), EPSILON);
        assertEquals(4.0, created.getImaginary(), EPSILON);

        // Serialization and readResolve verification
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(z);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Complex deserialized = (Complex) ois.readObject();

        assertEquals(z, deserialized);
        assertEquals(z.getReal(), deserialized.getReal(), EPSILON);
        assertEquals(z.getImaginary(), deserialized.getImaginary(), EPSILON);
        assertFalse(deserialized.isNaN());
        assertFalse(deserialized.isInfinite());
    }
}