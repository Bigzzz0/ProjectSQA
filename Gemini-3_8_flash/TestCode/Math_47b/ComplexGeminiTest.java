/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math.complex;

import org.apache.commons.math.exception.NotPositiveException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Target Method    | Branch / Condition Covered                        | Targeted Defect / Edge Case
 * -------------------------------------------------------------------------------------------------------
 * divide(Complex)  | divisor == ZERO (0.0 + 0.0i)                      | Defect: returned INF instead of NaN
 * atan()           | z == I and z == -I (singularity in formula)       | Defect: returned non-NaN / threw error
 * divide(double)   | divisor == 0.0, isInfinite, NaN                   | Boundary: zero divisor returns NaN
 * multiply(Complex)| Infinite operands vs NaN operands                 | Priority: NaN overrides Inf
 * abs()            | abs(real) < abs(imag) vs abs(real) >= abs(imag)   | Algorithmic branch coverage
 * sqrt()           | real == 0 && imag == 0; real >= 0; real < 0       | Branch coverage for indicator/sign
 * nthRoot(n)       | n <= 0, isNaN, isInfinite, valid roots            | Exception & multi-root generation
 * equals/hashCode  | NaN equivalence rule vs IEEE-754 semantics        | Symmetric contract & hash stability
 * readResolve()    | Deserialization transient flag reconstruction     | isNaN and isInfinite state integrity
 * -------------------------------------------------------------------------------------------------------
 */
public class ComplexGeminiTest {

    private static final double EPSILON = 1e-12;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicArithmeticOperations() {
        Complex z1 = new Complex(3.0, 4.0);
        Complex z2 = new Complex(1.0, -2.0);

        // Addition
        Complex sum = z1.add(z2);
        assertEquals(4.0, sum.getReal(), EPSILON);
        assertEquals(2.0, sum.getImaginary(), EPSILON);

        // Subtraction
        Complex diff = z1.subtract(z2);
        assertEquals(2.0, diff.getReal(), EPSILON);
        assertEquals(6.0, diff.getImaginary(), EPSILON);

        // Multiplication: (3 + 4i)(1 - 2i) = 3 - 6i + 4i - 8i^2 = 11 - 2i
        Complex prod = z1.multiply(z2);
        assertEquals(11.0, prod.getReal(), EPSILON);
        assertEquals(-2.0, prod.getImaginary(), EPSILON);

        // Division: (3 + 4i)/(1 - 2i) = ((3+4i)(1+2i)) / 5 = (-5 + 10i) / 5 = -1 + 2i
        Complex quot = z1.divide(z2);
        assertEquals(-1.0, quot.getReal(), EPSILON);
        assertEquals(2.0, quot.getImaginary(), EPSILON);

        // Scalar addition / subtraction
        Complex scalarAdd = z1.add(2.0);
        assertEquals(5.0, scalarAdd.getReal(), EPSILON);
        assertEquals(4.0, scalarAdd.getImaginary(), EPSILON);

        Complex scalarSub = z1.subtract(1.5);
        assertEquals(1.5, scalarSub.getReal(), EPSILON);
        assertEquals(4.0, scalarSub.getImaginary(), EPSILON);

        // Conjugate & Negate
        Complex conj = z1.conjugate();
        assertEquals(3.0, conj.getReal(), EPSILON);
        assertEquals(-4.0, conj.getImaginary(), EPSILON);

        Complex neg = z1.negate();
        assertEquals(-3.0, neg.getReal(), EPSILON);
        assertEquals(-4.0, neg.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAbsBranches() {
        // |real| < |imaginary| branch
        Complex c1 = new Complex(3.0, 4.0);
        assertEquals(5.0, c1.abs(), EPSILON);

        // |real| >= |imaginary| branch
        Complex c2 = new Complex(4.0, 3.0);
        assertEquals(5.0, c2.abs(), EPSILON);

        // real == 0.0 and imaginary == 0.0 branch
        assertEquals(0.0, Complex.ZERO.abs(), EPSILON);

        // isNaN branch
        assertTrue(Double.isNaN(Complex.NaN.abs()));

        // isInfinite branch
        assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, new Complex(Double.NEGATIVE_INFINITY, 0.0).abs(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSqrtBranches() {
        // Zero branch
        Complex sqrtZero = Complex.ZERO.sqrt();
        assertEquals(0.0, sqrtZero.getReal(), EPSILON);
        assertEquals(0.0, sqrtZero.getImaginary(), EPSILON);

        // real >= 0 branch (e.g. 3 + 4i -> 2 + i)
        Complex sqrtPosReal = new Complex(3.0, 4.0).sqrt();
        assertEquals(2.0, sqrtPosReal.getReal(), EPSILON);
        assertEquals(1.0, sqrtPosReal.getImaginary(), EPSILON);

        // real < 0 branch (e.g. -3 + 4i -> 1 + 2i)
        Complex sqrtNegReal = new Complex(-3.0, 4.0).sqrt();
        assertEquals(1.0, sqrtNegReal.getReal(), EPSILON);
        assertEquals(2.0, sqrtNegReal.getImaginary(), EPSILON);

        // real < 0 and imaginary < 0 branch (testing indicator / sign)
        Complex sqrtNegBoth = new Complex(-3.0, -4.0).sqrt();
        assertEquals(1.0, sqrtNegBoth.getReal(), EPSILON);
        assertEquals(-2.0, sqrtNegBoth.getImaginary(), EPSILON);

        // NaN branch
        assertTrue(Complex.NaN.sqrt().isNaN());
    }

    @Test(timeout = 4000)
    public void testTrigonometricAndHyperbolicFunctions() {
        Complex z = new Complex(1.0, 1.0);

        // sin and cos: sin^2(z) + cos^2(z) = 1
        Complex sin = z.sin();
        Complex cos = z.cos();
        Complex sumSq = sin.multiply(sin).add(cos.multiply(cos));
        assertEquals(1.0, sumSq.getReal(), EPSILON);
        assertEquals(0.0, sumSq.getImaginary(), EPSILON);

        // tan(z) = sin(z) / cos(z)
        Complex tan = z.tan();
        Complex tanExpected = sin.divide(cos);
        assertEquals(tanExpected.getReal(), tan.getReal(), EPSILON);
        assertEquals(tanExpected.getImaginary(), tan.getImaginary(), EPSILON);

        // sinh, cosh: cosh^2(z) - sinh^2(z) = 1
        Complex sinh = z.sinh();
        Complex cosh = z.cosh();
        Complex hypDiff = cosh.multiply(cosh).subtract(sinh.multiply(sinh));
        assertEquals(1.0, hypDiff.getReal(), EPSILON);
        assertEquals(0.0, hypDiff.getImaginary(), EPSILON);

        // tanh(z) = sinh(z) / cosh(z)
        Complex tanh = z.tanh();
        Complex tanhExpected = sinh.divide(cosh);
        assertEquals(tanhExpected.getReal(), tanh.getReal(), EPSILON);
        assertEquals(tanhExpected.getImaginary(), tanh.getImaginary(), EPSILON);

        // Exponential and Logarithm: exp(log(z)) = z
        Complex expLog = z.log().exp();
        assertEquals(z.getReal(), expLog.getReal(), EPSILON);
        assertEquals(z.getImaginary(), expLog.getImaginary(), EPSILON);

        // Inverse functions: acos, asin
        Complex acosZ = z.acos();
        Complex cosAcos = acosZ.cos();
        assertEquals(z.getReal(), cosAcos.getReal(), EPSILON);
        assertEquals(z.getImaginary(), cosAcos.getImaginary(), EPSILON);

        Complex asinZ = z.asin();
        Complex sinAsin = asinZ.sin();
        assertEquals(z.getReal(), sinAsin.getReal(), EPSILON);
        assertEquals(z.getImaginary(), sinAsin.getImaginary(), EPSILON);

        // sqrt1z: sqrt(1 - z^2)
        Complex sqrt1z = z.sqrt1z();
        Complex expectedSqrt1z = Complex.ONE.subtract(z.multiply(z)).sqrt();
        assertEquals(expectedSqrt1z.getReal(), sqrt1z.getReal(), EPSILON);
        assertEquals(expectedSqrt1z.getImaginary(), sqrt1z.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testPowAndArgument() {
        Complex base = new Complex(2.0, 3.0);
        Complex exponent = new Complex(1.5, -0.5);

        // Complex.pow(Complex)
        Complex powResult = base.pow(exponent);
        Complex expectedPow = base.log().multiply(exponent).exp();
        assertEquals(expectedPow.getReal(), powResult.getReal(), EPSILON);
        assertEquals(expectedPow.getImaginary(), powResult.getImaginary(), EPSILON);

        // Complex.pow(double)
        Complex powScalar = base.pow(2.0);
        Complex expectedScalarPow = base.multiply(base);
        assertEquals(expectedScalarPow.getReal(), powScalar.getReal(), EPSILON);
        assertEquals(expectedScalarPow.getImaginary(), powScalar.getImaginary(), EPSILON);

        // Argument
        Complex posReal = new Complex(2.0, 0.0);
        assertEquals(0.0, posReal.getArgument(), EPSILON);
        Complex posImag = new Complex(0.0, 2.0);
        assertEquals(FastMath.PI / 2.0, posImag.getArgument(), EPSILON);
        Complex negReal = new Complex(-2.0, 0.0);
        assertEquals(FastMath.PI, negReal.getArgument(), EPSILON);
        Complex negImag = new Complex(0.0, -2.0);
        assertEquals(-FastMath.PI / 2.0, negImag.getArgument(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testNthRoots() {
        Complex z = new Complex(0.0, 8.0);
        List<Complex> roots3 = z.nthRoot(3);
        assertEquals(3, roots3.size());
        for (Complex root : roots3) {
            Complex cubed = root.multiply(root).multiply(root);
            assertEquals(z.getReal(), cubed.getReal(), 1e-10);
            assertEquals(z.getImaginary(), cubed.getImaginary(), 1e-10);
        }

        // nthRoot for NaN
        List<Complex> nanRoots = Complex.NaN.nthRoot(2);
        assertEquals(1, nanRoots.size());
        assertTrue(nanRoots.get(0).isNaN());

        // nthRoot for INF
        List<Complex> infRoots = Complex.INF.nthRoot(2);
        assertEquals(1, infRoots.size());
        assertTrue(infRoots.get(0).isInfinite());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDivideBranchCoverage() {
        Complex finite = new Complex(2.0, 3.0);

        // abs(c) < abs(d) branch where divisor is (1.0 + 2.0i)
        Complex divSmallC = finite.divide(new Complex(1.0, 2.0));
        assertFalse(divSmallC.isNaN());

        // abs(c) >= abs(d) branch where divisor is (2.0 + 1.0i)
        Complex divLargeC = finite.divide(new Complex(2.0, 1.0));
        assertFalse(divLargeC.isNaN());

        // Finite divided by Infinite -> ZERO
        Complex divByInf = finite.divide(Complex.INF);
        assertEquals(Complex.ZERO, divByInf);

        // Scalar divide branches
        assertEquals(Complex.NaN, finite.divide(Double.NaN));
        assertEquals(Complex.NaN, Complex.NaN.divide(2.0));
        assertEquals(Complex.ZERO, finite.divide(Double.POSITIVE_INFINITY));
        assertEquals(Complex.ZERO, finite.divide(Double.NEGATIVE_INFINITY));
        assertEquals(Complex.NaN, Complex.INF.divide(Double.POSITIVE_INFINITY));

        Complex scalarDiv = finite.divide(2.0);
        assertEquals(1.0, scalarDiv.getReal(), EPSILON);
        assertEquals(1.5, scalarDiv.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testMultiplyInfiniteAndNaNHandling() {
        Complex finite = new Complex(2.0, 3.0);

        // Infinite * finite -> INF
        Complex infProd1 = finite.multiply(Complex.INF);
        assertTrue(infProd1.isInfinite());

        // NaN takes precedence over infinite: (NaN + 1i) * INF -> NaN
        Complex nanWithInf = new Complex(Double.NaN, 1.0);
        Complex nanInfProd = nanWithInf.multiply(Complex.INF);
        assertTrue(nanInfProd.isNaN());
        assertFalse(nanInfProd.isInfinite());

        // Scalar multiply with infinity
        Complex scalarInf = finite.multiply(Double.POSITIVE_INFINITY);
        assertTrue(scalarInf.isInfinite());

        // Scalar multiply with NaN
        Complex scalarNaN = finite.multiply(Double.NaN);
        assertTrue(scalarNaN.isNaN());
    }

    @Test(timeout = 4000)
    public void testMathematicalFunctionsWithNaNInputs() {
        assertTrue(Complex.NaN.acos().isNaN());
        assertTrue(Complex.NaN.asin().isNaN());
        assertTrue(Complex.NaN.atan().isNaN());
        assertTrue(Complex.NaN.cos().isNaN());
        assertTrue(Complex.NaN.cosh().isNaN());
        assertTrue(Complex.NaN.exp().isNaN());
        assertTrue(Complex.NaN.log().isNaN());
        assertTrue(Complex.NaN.sin().isNaN());
        assertTrue(Complex.NaN.sinh().isNaN());
        assertTrue(Complex.NaN.sqrt().isNaN());
        assertTrue(Complex.NaN.tan().isNaN());
        assertTrue(Complex.NaN.tanh().isNaN());
        assertTrue(Complex.NaN.conjugate().isNaN());
        assertTrue(Complex.NaN.negate().isNaN());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGET DEFECT 1: testDivideZero
     * Defect condition: Dividing any non-zero Complex number by Complex.ZERO (0.0, 0.0)
     * must return Complex.NaN per specification and IEEE-754 semantics.
     * The defective implementation returned Complex.INF (Infinity, Infinity).
     */
    @Test(timeout = 4000)
    public void testDivideZeroDefect() {
        Complex nonZero = new Complex(3.0, 4.0);
        Complex result = nonZero.divide(Complex.ZERO);

        // Correct behavior: should equal Complex.NaN, NOT Complex.INF
        assertEquals(Complex.NaN, result);
        assertTrue("Result of division by Complex.ZERO must be NaN", result.isNaN());
        assertFalse("Result of division by Complex.ZERO must not be Infinite", result.isInfinite());

        // Also verify (0.0 + 0.0i) / (0.0 + 0.0i) -> NaN
        Complex zeroDivZero = Complex.ZERO.divide(Complex.ZERO);
        assertEquals(Complex.NaN, zeroDivZero);
        assertTrue(zeroDivZero.isNaN());

        // Scalar division by 0.0 -> NaN
        Complex scalarZeroDiv = nonZero.divide(0.0);
        assertEquals(Complex.NaN, scalarZeroDiv);
        assertTrue(scalarZeroDiv.isNaN());
    }

    /**
     * TARGET DEFECT 2: testAtanI
     * Defect condition: atan(I) and atan(-I) are singular points because (i - z)
     * or (i + z) becomes zero, resulting in division by zero.
     * With defective division returning INF instead of NaN, atan(I) returned
     * non-NaN numbers instead of Complex.NaN.
     */
    @Test(timeout = 4000)
    public void testAtanIDefect() {
        // Singular point atan(i) -> NaN
        Complex atanI = Complex.I.atan();
        assertTrue("atan(i) must evaluate to NaN due to singularity", atanI.isNaN());

        // Singular point atan(-i) -> NaN
        Complex atanNegI = Complex.I.negate().atan();
        assertTrue("atan(-i) must evaluate to NaN due to singularity", atanNegI.isNaN());

        // Non-singular points around the lattice must compute valid Complex numbers
        for (int r = -1; r <= 1; r++) {
            for (int i = -1; i <= 1; i++) {
                Complex c = new Complex(r, i);
                if (c.equals(Complex.I) || c.equals(Complex.I.negate())) {
                    assertTrue(c.atan().isNaN());
                } else {
                    assertFalse("atan(" + c + ") should not be NaN", c.atan().isNaN());
                }
            }
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testAddNullThrowsException() {
        Complex.ONE.add((Complex) null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testSubtractNullThrowsException() {
        Complex.ONE.subtract((Complex) null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testMultiplyNullThrowsException() {
        Complex.ONE.multiply((Complex) null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testDivideNullThrowsException() {
        Complex.ONE.divide((Complex) null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testPowNullThrowsException() {
        Complex.ONE.pow((Complex) null);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testNthRootZeroDegreeThrowsException() {
        Complex.ONE.nthRoot(0);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testNthRootNegativeDegreeThrowsException() {
        Complex.ONE.nthRoot(-2);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Complex a1 = new Complex(1.23, 4.56);
        Complex a2 = new Complex(1.23, 4.56);
        Complex b = new Complex(1.23, 7.89);
        Complex c = new Complex(7.89, 4.56);

        // Reflexive
        assertTrue(a1.equals(a1));

        // Symmetric
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
        assertEquals(a1.hashCode(), a2.hashCode());

        // Non-equality
        assertFalse(a1.equals(b));
        assertFalse(a1.equals(c));
        assertFalse(a1.equals(null));
        assertFalse(a1.equals("Not a complex number"));

        // NaN Equivalence rule (All NaN complexes are considered equal)
        Complex nan1 = new Complex(Double.NaN, 0.0);
        Complex nan2 = new Complex(0.0, Double.NaN);
        Complex nan3 = new Complex(Double.NaN, Double.NaN);
        assertTrue(nan1.equals(Complex.NaN));
        assertTrue(nan2.equals(Complex.NaN));
        assertTrue(nan3.equals(Complex.NaN));
        assertTrue(nan1.equals(nan2));
        assertEquals(7, Complex.NaN.hashCode());
        assertEquals(7, nan1.hashCode());
        assertEquals(7, nan2.hashCode());
    }

    @Test(timeout = 4000)
    public void testValueOfFactories() {
        Complex v1 = Complex.valueOf(3.0, 4.0);
        assertEquals(3.0, v1.getReal(), EPSILON);
        assertEquals(4.0, v1.getImaginary(), EPSILON);

        Complex v2 = Complex.valueOf(5.0);
        assertEquals(5.0, v2.getReal(), EPSILON);
        assertEquals(0.0, v2.getImaginary(), EPSILON);

        // NaN factories
        assertTrue(Complex.valueOf(Double.NaN, 1.0).isNaN());
        assertTrue(Complex.valueOf(1.0, Double.NaN).isNaN());
        assertTrue(Complex.valueOf(Double.NaN).isNaN());
    }

    @Test(timeout = 4000)
    public void testSingleArgConstructor() {
        Complex c = new Complex(2.5);
        assertEquals(2.5, c.getReal(), EPSILON);
        assertEquals(0.0, c.getImaginary(), EPSILON);
        assertFalse(c.isNaN());
        assertFalse(c.isInfinite());
    }

    @Test(timeout = 4000)
    public void testToStringAndGetField() {
        Complex z = new Complex(1.5, -2.5);
        assertEquals("(1.5, -2.5)", z.toString());

        assertNotNull(z.getField());
        assertSame(ComplexField.getInstance(), z.getField());
    }

    @Test(timeout = 4000)
    public void testSerializationReadResolve() throws Exception {
        Complex original = new Complex(3.14, 2.71);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Complex deserialized = (Complex) ois.readObject();

        assertEquals(original, deserialized);
        assertEquals(original.getReal(), deserialized.getReal(), EPSILON);
        assertEquals(original.getImaginary(), deserialized.getImaginary(), EPSILON);
        assertFalse(deserialized.isNaN());
        assertFalse(deserialized.isInfinite());

        // Test deserialization with NaN to ensure transient flags resolve correctly
        Complex nanOriginal = Complex.NaN;
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        oos.writeObject(nanOriginal);
        oos.flush();

        bis = new ByteArrayInputStream(bos.toByteArray());
        ois = new ObjectInputStream(bis);
        Complex deserializedNaN = (Complex) ois.readObject();

        assertTrue(deserializedNaN.isNaN());
        assertEquals(Complex.NaN, deserializedNaN);
    }
}