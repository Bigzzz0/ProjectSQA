package org.apache.commons.math3.complex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.math3.complex.Complex
 *
 * Targeted Defects & Decision Branches:
 * 1. DEFECT testReciprocalZero:
 *    - Verifies reciprocal() of Complex.ZERO (0.0 + 0.0i) returns Complex.NaN.
 * 2. abs():
 *    - isNaN branch -> NaN
 *    - isInfinite() branch -> POSITIVE_INFINITY
 *    - |real| < |imaginary| vs |real| >= |imaginary| branches
 *    - Zero imaginary and zero real sub-branches
 * 3. add() & subtract():
 *    - Complex and double scalar overloads
 *    - Null check guards throwing NullArgumentException
 *    - NaN propagation and regular addition/subtraction
 * 4. divide():
 *    - Complex divisor: null check, NaN operand, divisor == 0.0,
 *      divisor is infinite & dividend is finite, |c| < |d| vs |c| >= |d| prescaling
 *    - double divisor: isNaN, divisor == 0, infinite divisor (!isInfinite() -> ZERO, else NaN)
 * 5. reciprocal():
 *    - isNaN, real == 0 && imag == 0 (Defect-targeted branch), isInfinite -> ZERO,
 *      |real| < |imaginary| prescaling, |real| >= |imaginary| prescaling
 * 6. multiply():
 *    - Complex factor: null check, NaN operands, infinite parts -> INF, standard multiplication
 *    - int factor: isNaN, infinite -> INF, normal product
 *    - double factor: isNaN / Double.isNaN, infinite -> INF, normal product
 * 7. Elementary & Transcendental Functions:
 *    - conjugate(): isNaN, normal
 *    - negate(): isNaN, normal
 *    - acos(), asin(), atan(): isNaN, normal
 *    - cos(), cosh(): isNaN, finite, infinite handling
 *    - exp(), log(): isNaN, zero modulus, infinite
 *    - pow(): null guard, Complex and double exponents, NaN/infinite propagation
 *    - sin(), sinh(): isNaN, normal
 *    - sqrt(): isNaN, 0.0+0.0i, real >= 0, real < 0 (with positive/negative imaginary for copySign)
 *    - sqrt1z(): normal and boundary evaluation
 *    - tan(): isNaN or Double.isInfinite(real), imag > 20.0, imag < -20.0, normal
 *    - tanh(): isNaN or Double.isInfinite(imag), real > 20.0, real < -20.0, normal
 *    - getArgument(): angle evaluation via atan2
 *    - nthRoot(): n <= 0 (NotPositiveException), isNaN, isInfinite, normal nth roots
 * 8. Factory Methods & Object Contract Integrity:
 *    - valueOf(double), valueOf(double, double): NaN checks
 *    - equals(): identity, null, non-Complex, isNaN symmetric equality, real/imag mismatch
 *    - hashCode(): isNaN -> 7, standard hash code consistency
 *    - Serialization & readResolve(): transient isNaN and isInfinite restoration
 *    - getField() & toString() verification
 */
public class ComplexGeminiTest {

    private static final double EPSILON = 1.0e-12;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicGettersAndConstants() {
        Complex z = new Complex(3.0, -4.0);
        assertEquals(3.0, z.getReal(), EPSILON);
        assertEquals(-4.0, z.getImaginary(), EPSILON);
        assertFalse(z.isNaN());
        assertFalse(z.isInfinite());

        assertEquals(0.0, Complex.I.getReal(), EPSILON);
        assertEquals(1.0, Complex.I.getImaginary(), EPSILON);

        assertEquals(1.0, Complex.ONE.getReal(), EPSILON);
        assertEquals(0.0, Complex.ONE.getImaginary(), EPSILON);

        assertEquals(0.0, Complex.ZERO.getReal(), EPSILON);
        assertEquals(0.0, Complex.ZERO.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.isNaN());
        assertTrue(Complex.INF.isInfinite());
    }

    @Test(timeout = 4000)
    public void testAddAndSubtractNormal() {
        Complex z1 = new Complex(2.0, 3.0);
        Complex z2 = new Complex(-1.0, 4.0);

        Complex sum = z1.add(z2);
        assertEquals(1.0, sum.getReal(), EPSILON);
        assertEquals(7.0, sum.getImaginary(), EPSILON);

        Complex sumDouble = z1.add(5.0);
        assertEquals(7.0, sumDouble.getReal(), EPSILON);
        assertEquals(3.0, sumDouble.getImaginary(), EPSILON);

        Complex diff = z1.subtract(z2);
        assertEquals(3.0, diff.getReal(), EPSILON);
        assertEquals(-1.0, diff.getImaginary(), EPSILON);

        Complex diffDouble = z1.subtract(1.5);
        assertEquals(0.5, diffDouble.getReal(), EPSILON);
        assertEquals(3.0, diffDouble.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testMultiplyAndDivideNormal() {
        Complex z1 = new Complex(2.0, 3.0);
        Complex z2 = new Complex(4.0, -5.0);

        Complex prod = z1.multiply(z2);
        assertEquals(23.0, prod.getReal(), EPSILON);
        assertEquals(2.0, prod.getImaginary(), EPSILON);

        Complex prodInt = z1.multiply(3);
        assertEquals(6.0, prodInt.getReal(), EPSILON);
        assertEquals(9.0, prodInt.getImaginary(), EPSILON);

        Complex prodDouble = z1.multiply(2.5);
        assertEquals(5.0, prodDouble.getReal(), EPSILON);
        assertEquals(7.5, prodDouble.getImaginary(), EPSILON);

        // Division: |c| < |d|
        Complex div1 = new Complex(1.0, 2.0).divide(new Complex(3.0, 4.0));
        assertEquals(11.0 / 25.0, div1.getReal(), EPSILON);
        assertEquals(2.0 / 25.0, div1.getImaginary(), EPSILON);

        // Division: |c| >= |d|
        Complex div2 = new Complex(1.0, 2.0).divide(new Complex(4.0, 3.0));
        assertEquals(10.0 / 25.0, div2.getReal(), EPSILON);
        assertEquals(5.0 / 25.0, div2.getImaginary(), EPSILON);

        Complex divDouble = new Complex(6.0, -9.0).divide(3.0);
        assertEquals(2.0, divDouble.getReal(), EPSILON);
        assertEquals(-3.0, divDouble.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testConjugateAndNegate() {
        Complex z = new Complex(2.5, -4.5);
        Complex conj = z.conjugate();
        assertEquals(2.5, conj.getReal(), EPSILON);
        assertEquals(4.5, conj.getImaginary(), EPSILON);

        Complex neg = z.negate();
        assertEquals(-2.5, neg.getReal(), EPSILON);
        assertEquals(4.5, neg.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.conjugate().isNaN());
        assertTrue(Complex.NaN.negate().isNaN());
    }

    @Test(timeout = 4000)
    public void testAbsBranches() {
        assertTrue(Double.isNaN(Complex.NaN.abs()));
        assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, new Complex(1.0, Double.NEGATIVE_INFINITY).abs(), EPSILON);

        // |real| < |imaginary| branch
        Complex z1 = new Complex(3.0, 4.0);
        assertEquals(5.0, z1.abs(), EPSILON);

        // |real| >= |imaginary| branch
        Complex z2 = new Complex(4.0, 3.0);
        assertEquals(5.0, z2.abs(), EPSILON);

        // real == 0.0 inside |real| >= |imaginary| (0, 0)
        assertEquals(0.0, Complex.ZERO.abs(), EPSILON);

        // imaginary == 0.0 branch in |real| >= |imaginary|
        Complex zRealOnly = new Complex(-7.0, 0.0);
        assertEquals(7.0, zRealOnly.abs(), EPSILON);

        // real == 0.0 inside |real| < |imaginary|
        Complex zImagOnly = new Complex(0.0, -6.0);
        assertEquals(6.0, zImagOnly.abs(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testTrigonometricAndHyperbolicFunctions() {
        Complex z = new Complex(0.5, 0.5);

        Complex sin = z.sin();
        Complex cos = z.cos();
        Complex tan = z.tan();
        Complex sinh = z.sinh();
        Complex cosh = z.cosh();
        Complex tanh = z.tanh();

        assertFalse(sin.isNaN());
        assertFalse(cos.isNaN());
        assertFalse(tan.isNaN());
        assertFalse(sinh.isNaN());
        assertFalse(cosh.isNaN());
        assertFalse(tanh.isNaN());

        // Test tan asymptotic branches
        Complex tanLargePosImag = new Complex(1.0, 25.0).tan();
        assertEquals(0.0, tanLargePosImag.getReal(), EPSILON);
        assertEquals(1.0, tanLargePosImag.getImaginary(), EPSILON);

        Complex tanLargeNegImag = new Complex(1.0, -25.0).tan();
        assertEquals(0.0, tanLargeNegImag.getReal(), EPSILON);
        assertEquals(-1.0, tanLargeNegImag.getImaginary(), EPSILON);

        // Test tanh asymptotic branches
        Complex tanhLargePosReal = new Complex(25.0, 1.0).tanh();
        assertEquals(1.0, tanhLargePosReal.getReal(), EPSILON);
        assertEquals(0.0, tanhLargePosReal.getImaginary(), EPSILON);

        Complex tanhLargeNegReal = new Complex(-25.0, 1.0).tanh();
        assertEquals(-1.0, tanhLargeNegReal.getReal(), EPSILON);
        assertEquals(0.0, tanhLargeNegReal.getImaginary(), EPSILON);

        // Infinite branches for tan / tanh
        assertTrue(new Complex(Double.POSITIVE_INFINITY, 0.0).tan().isNaN());
        assertTrue(new Complex(0.0, Double.POSITIVE_INFINITY).tanh().isNaN());
    }

    @Test(timeout = 4000)
    public void testInverseTrigonometricFunctions() {
        Complex z = new Complex(0.3, 0.4);

        Complex acos = z.acos();
        Complex asin = z.asin();
        Complex atan = z.atan();

        assertFalse(acos.isNaN());
        assertFalse(asin.isNaN());
        assertFalse(atan.isNaN());

        assertTrue(Complex.NaN.acos().isNaN());
        assertTrue(Complex.NaN.asin().isNaN());
        assertTrue(Complex.NaN.atan().isNaN());
    }

    @Test(timeout = 4000)
    public void testExponentialLogarithmAndPower() {
        Complex z = new Complex(1.0, FastMath.PI / 2.0);
        Complex exp = z.exp();
        assertEquals(0.0, exp.getReal(), EPSILON);
        assertEquals(FastMath.E, exp.getImaginary(), EPSILON);

        Complex log = exp.log();
        assertEquals(1.0, log.getReal(), EPSILON);
        assertEquals(FastMath.PI / 2.0, log.getImaginary(), EPSILON);

        Complex base = new Complex(2.0, 0.0);
        Complex powResult = base.pow(3.0);
        assertEquals(8.0, powResult.getReal(), EPSILON);
        assertEquals(0.0, powResult.getImaginary(), EPSILON);

        Complex powComplex = base.pow(new Complex(2.0, 0.0));
        assertEquals(4.0, powComplex.getReal(), EPSILON);
        assertEquals(0.0, powComplex.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.exp().isNaN());
        assertTrue(Complex.NaN.log().isNaN());
    }

    @Test(timeout = 4000)
    public void testSqrtBranches() {
        // (0, 0) branch
        Complex zeroSqrt = Complex.ZERO.sqrt();
        assertEquals(0.0, zeroSqrt.getReal(), EPSILON);
        assertEquals(0.0, zeroSqrt.getImaginary(), EPSILON);

        // real >= 0 branch
        Complex zPos = new Complex(3.0, 4.0).sqrt();
        assertEquals(2.0, zPos.getReal(), EPSILON);
        assertEquals(1.0, zPos.getImaginary(), EPSILON);

        // real < 0, imaginary >= 0
        Complex zNegRealPosImag = new Complex(-3.0, 4.0).sqrt();
        assertEquals(1.0, zNegRealPosImag.getReal(), EPSILON);
        assertEquals(2.0, zNegRealPosImag.getImaginary(), EPSILON);

        // real < 0, imaginary < 0 (exercising copySign)
        Complex zNegRealNegImag = new Complex(-3.0, -4.0).sqrt();
        assertEquals(1.0, zNegRealNegImag.getReal(), EPSILON);
        assertEquals(-2.0, zNegRealNegImag.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.sqrt().isNaN());

        Complex sqrt1z = new Complex(0.6, 0.8).sqrt1z();
        assertFalse(sqrt1z.isNaN());
    }

    @Test(timeout = 4000)
    public void testGetArgument() {
        Complex zPos = new Complex(1.0, 1.0);
        assertEquals(FastMath.PI / 4.0, zPos.getArgument(), EPSILON);

        Complex zNeg = new Complex(-1.0, -1.0);
        assertEquals(-3.0 * FastMath.PI / 4.0, zNeg.getArgument(), EPSILON);

        assertTrue(Double.isNaN(Complex.NaN.getArgument()));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testInfiniteMultiplication() {
        Complex infReal = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Complex normal = new Complex(2.0, 3.0);

        Complex result1 = infReal.multiply(normal);
        assertTrue(result1.isInfinite());
        assertEquals(Complex.INF, result1);

        Complex result2 = normal.multiply(infReal);
        assertTrue(result2.isInfinite());
        assertEquals(Complex.INF, result2);

        Complex resultInt = infReal.multiply(2);
        assertTrue(resultInt.isInfinite());
        assertEquals(Complex.INF, resultInt);

        Complex resultDouble = infReal.multiply(2.0);
        assertTrue(resultDouble.isInfinite());
        assertEquals(Complex.INF, resultDouble);

        Complex normalDoubleInf = normal.multiply(Double.POSITIVE_INFINITY);
        assertTrue(normalDoubleInf.isInfinite());
        assertEquals(Complex.INF, normalDoubleInf);
    }

    @Test(timeout = 4000)
    public void testDivideBoundaries() {
        // Division by ZERO yields NaN
        Complex divZero = Complex.ONE.divide(Complex.ZERO);
        assertTrue(divZero.isNaN());

        // Division by 0.0 scalar yields NaN
        Complex divDoubleZero = Complex.ONE.divide(0.0);
        assertTrue(divDoubleZero.isNaN());

        // Finite / Infinite yields ZERO
        Complex finiteDividedByInf = Complex.ONE.divide(Complex.INF);
        assertEquals(Complex.ZERO, finiteDividedByInf);

        Complex finiteDividedByDoubleInf = Complex.ONE.divide(Double.POSITIVE_INFINITY);
        assertEquals(Complex.ZERO, finiteDividedByDoubleInf);

        // Infinite / Infinite yields NaN
        Complex infDividedByDoubleInf = Complex.INF.divide(Double.POSITIVE_INFINITY);
        assertTrue(infDividedByDoubleInf.isNaN());

        // Scalar division with NaN
        assertTrue(Complex.ONE.divide(Double.NaN).isNaN());
        assertTrue(Complex.NaN.divide(2.0).isNaN());
    }

    @Test(timeout = 4000)
    public void testNthRootBoundaries() {
        // isNaN branch
        List<Complex> nanRoots = Complex.NaN.nthRoot(2);
        assertEquals(1, nanRoots.size());
        assertTrue(nanRoots.get(0).isNaN());

        // isInfinite branch
        List<Complex> infRoots = Complex.INF.nthRoot(3);
        assertEquals(1, infRoots.size());
        assertTrue(infRoots.get(0).isInfinite());

        // Normal roots
        Complex z = new Complex(0.0, 8.0);
        List<Complex> roots = z.nthRoot(3);
        assertEquals(3, roots.size());
        for (Complex root : roots) {
            assertEquals(2.0, root.abs(), EPSILON);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets Defects4J known failure condition:
     * Complex.ZERO.reciprocal() must return Complex.NaN rather than (Infinity, Infinity).
     */
    @Test(timeout = 4000)
    public void testReciprocalZero() {
        Complex recZero = Complex.ZERO.reciprocal();
        assertTrue("Reciprocal of ZERO must be NaN", recZero.isNaN());
        assertEquals(Complex.NaN, recZero);

        Complex recExplicitZero = new Complex(0.0, 0.0).reciprocal();
        assertTrue("Reciprocal of 0.0 + 0.0i must be NaN", recExplicitZero.isNaN());
        assertEquals(Complex.NaN, recExplicitZero);

        Complex recNegativeZero = new Complex(-0.0, 0.0).reciprocal();
        assertTrue("Reciprocal of -0.0 + 0.0i must be NaN", recNegativeZero.isNaN());
        assertEquals(Complex.NaN, recNegativeZero);
    }

    @Test(timeout = 4000)
    public void testReciprocalPrescalingAndBranches() {
        // isNaN
        assertTrue(Complex.NaN.reciprocal().isNaN());

        // isInfinite
        assertEquals(Complex.ZERO, Complex.INF.reciprocal());
        assertEquals(Complex.ZERO, new Complex(Double.POSITIVE_INFINITY, 0.0).reciprocal());

        // |real| < |imaginary| prescaling
        Complex z1 = new Complex(3.0, 4.0);
        Complex rec1 = z1.reciprocal();
        assertEquals(3.0 / 25.0, rec1.getReal(), EPSILON);
        assertEquals(-4.0 / 25.0, rec1.getImaginary(), EPSILON);

        // |real| >= |imaginary| prescaling
        Complex z2 = new Complex(4.0, 3.0);
        Complex rec2 = z2.reciprocal();
        assertEquals(4.0 / 25.0, rec2.getReal(), EPSILON);
        assertEquals(-3.0 / 25.0, rec2.getImaginary(), EPSILON);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testAddNullThrows() {
        Complex.ONE.add(null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testSubtractNullThrows() {
        Complex.ONE.subtract(null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testMultiplyNullThrows() {
        Complex.ONE.multiply(null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testDivideNullThrows() {
        Complex.ONE.divide(null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testPowNullThrows() {
        Complex.ONE.pow(null);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testNthRootZeroDegreeThrows() {
        Complex.ONE.nthRoot(0);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testNthRootNegativeDegreeThrows() {
        Complex.ONE.nthRoot(-2);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Complex z1 = new Complex(1.23, 4.56);
        Complex z2 = new Complex(1.23, 4.56);
        Complex zDiffReal = new Complex(9.99, 4.56);
        Complex zDiffImag = new Complex(1.23, 9.99);

        // Reflexivity
        assertEquals(z1, z1);
        // Symmetry
        assertEquals(z1, z2);
        assertEquals(z2, z1);
        assertEquals(z1.hashCode(), z2.hashCode());

        // Inequality
        assertFalse(z1.equals(null));
        assertFalse(z1.equals("A String"));
        assertFalse(z1.equals(zDiffReal));
        assertFalse(z1.equals(zDiffImag));

        // NaN Equivalence Contract
        Complex nan1 = Complex.NaN;
        Complex nan2 = new Complex(Double.NaN, 0.0);
        Complex nan3 = new Complex(0.0, Double.NaN);
        Complex nan4 = new Complex(Double.NaN, Double.NaN);

        assertEquals(nan1, nan2);
        assertEquals(nan2, nan3);
        assertEquals(nan3, nan4);
        assertEquals(7, nan1.hashCode());
        assertEquals(7, nan2.hashCode());
        assertEquals(7, nan3.hashCode());
        assertEquals(7, nan4.hashCode());

        assertFalse(z1.equals(nan1));
        assertFalse(nan1.equals(z1));
    }

    @Test(timeout = 4000)
    public void testFactoryValueOf() {
        Complex z1 = Complex.valueOf(3.5, -2.5);
        assertEquals(3.5, z1.getReal(), EPSILON);
        assertEquals(-2.5, z1.getImaginary(), EPSILON);

        Complex z2 = Complex.valueOf(4.0);
        assertEquals(4.0, z2.getReal(), EPSILON);
        assertEquals(0.0, z2.getImaginary(), EPSILON);

        assertTrue(Complex.valueOf(Double.NaN, 1.0).isNaN());
        assertTrue(Complex.valueOf(1.0, Double.NaN).isNaN());
        assertTrue(Complex.valueOf(Double.NaN).isNaN());
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        Complex original = new Complex(5.5, -6.5);
        Complex deserialized = serializeAndDeserialize(original);

        assertEquals(original, deserialized);
        assertFalse(deserialized.isNaN());
        assertFalse(deserialized.isInfinite());

        Complex nanOriginal = Complex.NaN;
        Complex nanDeserialized = serializeAndDeserialize(nanOriginal);
        assertTrue(nanDeserialized.isNaN());
        assertFalse(nanDeserialized.isInfinite());

        Complex infOriginal = Complex.INF;
        Complex infDeserialized = serializeAndDeserialize(infOriginal);
        assertTrue(infDeserialized.isInfinite());
        assertFalse(infDeserialized.isNaN());
    }

    @Test(timeout = 4000)
    public void testToStringAndField() {
        Complex z = new Complex(1.5, -2.5);
        assertEquals("(1.5, -2.5)", z.toString());

        ComplexField field = z.getField();
        assertNotNull(field);
        assertEquals(Complex.ZERO, field.getZero());
        assertEquals(Complex.ONE, field.getOne());
    }

    // Helper for Partition E serialization round-trip
    private Complex serializeAndDeserialize(Complex target) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(target);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Complex result = (Complex) ois.readObject();
        ois.close();
        return result;
    }
}