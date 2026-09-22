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

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.math.complex.Complex
 *
 * 1. Defect MATH-221 (Partition C):
 *    - testMath221: Complex(0, 1).multiply(Complex(-1, 0)) producing -0.0 real part vs +0.0
 *      in Complex(0, -1), exposing strict bitwise equals failure (doubleToRawLongBits vs MathUtils.equals).
 *
 * 2. abs():
 *    - isNaN() branch -> NaN
 *    - isInfinite() branch -> POSITIVE_INFINITY
 *    - |real| < |imaginary| with imaginary == 0.0 branch vs != 0.0
 *    - |real| >= |imaginary| with real == 0.0 branch vs != 0.0
 *
 * 3. add() / subtract() / negate() / conjugate():
 *    - Standard operational values
 *    - Handling of NaN, Infinite, and sign flips
 *
 * 4. divide():
 *    - isNaN() branches (lhs or rhs)
 *    - rhs == ZERO (c == 0.0 && d == 0.0) -> NaN
 *    - rhs.isInfinite() && !isInfinite() -> ZERO
 *    - |c| < |d| (with d == 0.0 safeguard branch)
 *    - |c| >= |d| (with c == 0.0 safeguard branch)
 *    - NPE on null rhs
 *
 * 5. multiply():
 *    - lhs.isNaN() || rhs.isNaN() -> NaN
 *    - lhs.isInfinite() || rhs.isInfinite() -> INF
 *    - Standard multiplication
 *    - NPE on null rhs
 *
 * 6. equals() & hashCode():
 *    - this == other -> true
 *    - other == null -> false
 *    - other not Complex instance -> false
 *    - rhs.isNaN() && this.isNaN() -> true
 *    - rhs.isNaN() != this.isNaN() -> false
 *    - bit equality checks on real and imaginary
 *    - hashCode consistency, including NaN hashCode contract (returns 7)
 *
 * 7. Transcendental and Trigonometric Functions:
 *    - acos, asin, atan: NaN/Infinite handling, normal computation
 *    - cos, cosh, sin, sinh, exp, log, pow, sqrt, sqrt1z, tan, tanh:
 *      * NaN checks
 *      * Zero boundaries (e.g. sqrt(0, 0), log(0, 0))
 *      * Sign branches in sqrt (real >= 0 vs real < 0)
 *      * pow(null) -> NPE
 *
 * 8. Subclassing hook:
 *    - createComplex(double, double) protected method invocation
 */
public class ComplexGeminiTest {

    private static final double EPSILON = 1e-15;

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-221)
    // =========================================================================

    /**
     * Defect MATH-221: Multiplying (0 + 1i) by (-1 + 0i) yields (-0.0 - 1.0i).
     * In the buggy implementation, equals() compares raw bits so (0.0 - 1.0i) != (-0.0 - 1.0i).
     */
    @Test(timeout = 4000)
    public void testMath221() {
        Complex z = new Complex(0.0, 1.0);
        Complex expected = new Complex(0.0, -1.0);
        Complex actual = z.multiply(new Complex(-1.0, 0.0));
        assertEquals(expected, actual);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGettersAndConstants() {
        Complex c = new Complex(3.0, -4.0);
        assertEquals(3.0, c.getReal(), EPSILON);
        assertEquals(-4.0, c.getImaginary(), EPSILON);

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
    public void testAbsNormal() {
        Complex c1 = new Complex(3.0, 4.0);
        assertEquals(5.0, c1.abs(), EPSILON);

        // |real| < |imaginary|
        Complex c2 = new Complex(3.0, 5.0);
        assertEquals(Math.sqrt(34.0), c2.abs(), EPSILON);

        // |real| >= |imaginary|
        Complex c3 = new Complex(5.0, 3.0);
        assertEquals(Math.sqrt(34.0), c3.abs(), EPSILON);

        // real == 0.0 (|real| >= |imaginary| boundary with imaginary == 0.0)
        Complex c4 = new Complex(0.0, 0.0);
        assertEquals(0.0, c4.abs(), EPSILON);

        // imaginary == 0.0 with real != 0.0
        Complex c5 = new Complex(7.0, 0.0);
        assertEquals(7.0, c5.abs(), EPSILON);

        // real == 0.0 with imaginary != 0.0
        Complex c6 = new Complex(0.0, 7.0);
        assertEquals(7.0, c6.abs(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testBasicArithmetic() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);

        Complex sum = x.add(y);
        assertEquals(8.0, sum.getReal(), EPSILON);
        assertEquals(10.0, sum.getImaginary(), EPSILON);

        Complex diff = x.subtract(y);
        assertEquals(-2.0, diff.getReal(), EPSILON);
        assertEquals(-2.0, diff.getImaginary(), EPSILON);

        Complex prod = x.multiply(y);
        assertEquals(-9.0, prod.getReal(), EPSILON); // 3*5 - 4*6 = 15 - 24 = -9
        assertEquals(38.0, prod.getImaginary(), EPSILON); // 3*6 + 4*5 = 18 + 20 = 38

        Complex neg = x.negate();
        assertEquals(-3.0, neg.getReal(), EPSILON);
        assertEquals(-4.0, neg.getImaginary(), EPSILON);

        Complex conj = x.conjugate();
        assertEquals(3.0, conj.getReal(), EPSILON);
        assertEquals(-4.0, conj.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDivideBranches() {
        Complex x = new Complex(1.0, 2.0);
        // |c| < |d| branch
        Complex y1 = new Complex(3.0, 4.0);
        Complex q1 = x.divide(y1);
        // (1*3 + 2*4)/25 = 11/25 = 0.44; (2*3 - 1*4)/25 = 2/25 = 0.08
        assertEquals(0.44, q1.getReal(), EPSILON);
        assertEquals(0.08, q1.getImaginary(), EPSILON);

        // |c| >= |d| branch
        Complex y2 = new Complex(4.0, 3.0);
        Complex q2 = x.divide(y2);
        // (1*4 + 2*3)/25 = 10/25 = 0.4; (2*4 - 1*3)/25 = 5/25 = 0.2
        assertEquals(0.4, q2.getReal(), EPSILON);
        assertEquals(0.2, q2.getImaginary(), EPSILON);

        // d == 0.0 in |c| < |d| (only reachable if c == 0.0, but c == 0 && d == 0 handled first)
        // c == 0.0 in |c| >= |d| (reachable when c == 0.0 and d == 0.0 handled)
        // Divide by zero
        Complex divZero = x.divide(Complex.ZERO);
        assertTrue(divZero.isNaN());
    }

    @Test(timeout = 4000)
    public void testTrigonometricFunctions() {
        Complex z = new Complex(1.0, 1.0);

        Complex sinZ = z.sin();
        assertEquals(Math.sin(1.0) * Math.cosh(1.0), sinZ.getReal(), EPSILON);
        assertEquals(Math.cos(1.0) * Math.sinh(1.0), sinZ.getImaginary(), EPSILON);

        Complex cosZ = z.cos();
        assertEquals(Math.cos(1.0) * Math.cosh(1.0), cosZ.getReal(), EPSILON);
        assertEquals(-Math.sin(1.0) * Math.sinh(1.0), cosZ.getImaginary(), EPSILON);

        Complex tanZ = z.tan();
        assertFalse(tanZ.isNaN());

        Complex sinhZ = z.sinh();
        assertEquals(Math.sinh(1.0) * Math.cos(1.0), sinhZ.getReal(), EPSILON);
        assertEquals(Math.cosh(1.0) * Math.sin(1.0), sinhZ.getImaginary(), EPSILON);

        Complex coshZ = z.cosh();
        assertEquals(Math.cosh(1.0) * Math.cos(1.0), coshZ.getReal(), EPSILON);
        assertEquals(Math.sinh(1.0) * Math.sin(1.0), coshZ.getImaginary(), EPSILON);

        Complex tanhZ = z.tanh();
        assertFalse(tanhZ.isNaN());
    }

    @Test(timeout = 4000)
    public void testInverseTrigonometricFunctions() {
        Complex z = new Complex(0.5, 0.5);

        Complex asinZ = z.asin();
        assertFalse(asinZ.isNaN());

        Complex acosZ = z.acos();
        assertFalse(acosZ.isNaN());

        Complex atanZ = z.atan();
        assertFalse(atanZ.isNaN());
    }

    @Test(timeout = 4000)
    public void testExponentialAndLogarithm() {
        Complex z = new Complex(2.0, Math.PI / 3.0);

        Complex expZ = z.exp();
        assertEquals(Math.exp(2.0) * Math.cos(Math.PI / 3.0), expZ.getReal(), EPSILON);
        assertEquals(Math.exp(2.0) * Math.sin(Math.PI / 3.0), expZ.getImaginary(), EPSILON);

        Complex logZ = z.log();
        assertEquals(Math.log(z.abs()), logZ.getReal(), EPSILON);
        assertEquals(Math.atan2(Math.PI / 3.0, 2.0), logZ.getImaginary(), EPSILON);

        Complex powZ = z.pow(new Complex(2.0, 0.0));
        Complex expectedPow = z.multiply(z);
        assertEquals(expectedPow.getReal(), powZ.getReal(), 1e-14);
        assertEquals(expectedPow.getImaginary(), powZ.getImaginary(), 1e-14);
    }

    @Test(timeout = 4000)
    public void testSquareRootBranches() {
        // sqrt(0, 0)
        Complex zeroSqrt = Complex.ZERO.sqrt();
        assertEquals(0.0, zeroSqrt.getReal(), EPSILON);
        assertEquals(0.0, zeroSqrt.getImaginary(), EPSILON);

        // real >= 0 branch
        Complex z1 = new Complex(3.0, 4.0);
        Complex sqrt1 = z1.sqrt();
        assertEquals(2.0, sqrt1.getReal(), EPSILON);
        assertEquals(1.0, sqrt1.getImaginary(), EPSILON);

        // real < 0 branch, positive imaginary
        Complex z2 = new Complex(-3.0, 4.0);
        Complex sqrt2 = z2.sqrt();
        assertEquals(1.0, sqrt2.getReal(), EPSILON);
        assertEquals(2.0, sqrt2.getImaginary(), EPSILON);

        // real < 0 branch, negative imaginary
        Complex z3 = new Complex(-3.0, -4.0);
        Complex sqrt3 = z3.sqrt();
        assertEquals(1.0, sqrt3.getReal(), EPSILON);
        assertEquals(-2.0, sqrt3.getImaginary(), EPSILON);

        // sqrt1z
        Complex z4 = new Complex(0.5, 0.5);
        Complex sqrt1zVal = z4.sqrt1z();
        assertFalse(sqrt1zVal.isNaN());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes (NaN and Infs)
    // =========================================================================

    @Test(timeout = 4000)
    public void testAbsNaNAndInfinite() {
        assertTrue(Double.isNaN(new Complex(Double.NaN, 0.0).abs()));
        assertTrue(Double.isNaN(new Complex(0.0, Double.NaN).abs()));
        assertTrue(Double.isNaN(Complex.NaN.abs()));

        assertEquals(Double.POSITIVE_INFINITY, new Complex(Double.POSITIVE_INFINITY, 0.0).abs(), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, new Complex(0.0, Double.NEGATIVE_INFINITY).abs(), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), EPSILON);

        // NaN takes precedence over Infinite according to javadoc
        assertTrue(Double.isNaN(new Complex(Double.POSITIVE_INFINITY, Double.NaN).abs()));
    }

    @Test(timeout = 4000)
    public void testConjugateNaNAndInfinite() {
        assertTrue(Complex.NaN.conjugate().isNaN());
        assertTrue(new Complex(Double.NaN, 1.0).conjugate().isNaN());
        assertTrue(new Complex(1.0, Double.NaN).conjugate().isNaN());

        Complex infConj = new Complex(1.0, Double.POSITIVE_INFINITY).conjugate();
        assertEquals(1.0, infConj.getReal(), EPSILON);
        assertEquals(Double.NEGATIVE_INFINITY, infConj.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAddSubtractNegateNaN() {
        Complex nanReal = new Complex(Double.NaN, 1.0);
        Complex valid = new Complex(2.0, 3.0);

        assertTrue(nanReal.negate().isNaN());
        assertTrue(valid.add(Complex.NaN).isNaN());
        assertTrue(Complex.NaN.add(valid).isNaN());
        assertTrue(valid.subtract(Complex.NaN).isNaN());
        assertTrue(Complex.NaN.subtract(valid).isNaN());
    }

    @Test(timeout = 4000)
    public void testMultiplyNaNAndInfinite() {
        Complex valid = new Complex(1.0, 1.0);

        assertTrue(valid.multiply(Complex.NaN).isNaN());
        assertTrue(Complex.NaN.multiply(valid).isNaN());

        assertEquals(Complex.INF, valid.multiply(Complex.INF));
        assertEquals(Complex.INF, Complex.INF.multiply(valid));
        assertEquals(Complex.INF, new Complex(Double.NEGATIVE_INFINITY, 1.0).multiply(valid));
        assertEquals(Complex.INF, valid.multiply(new Complex(1.0, Double.NEGATIVE_INFINITY)));
    }

    @Test(timeout = 4000)
    public void testDivideNaNAndInfinite() {
        Complex valid = new Complex(1.0, 1.0);

        assertTrue(valid.divide(Complex.NaN).isNaN());
        assertTrue(Complex.NaN.divide(valid).isNaN());

        // finite / infinite -> ZERO
        assertEquals(Complex.ZERO, valid.divide(Complex.INF));
        assertEquals(Complex.ZERO, valid.divide(new Complex(Double.POSITIVE_INFINITY, 0.0)));
        assertEquals(Complex.ZERO, valid.divide(new Complex(0.0, Double.NEGATIVE_INFINITY)));

        // infinite / infinite -> NaN
        assertTrue(Complex.INF.divide(Complex.INF).isNaN());

        // infinite / finite -> parts follow definitional rules
        Complex infDivided = Complex.INF.divide(valid);
        assertTrue(infDivided.isNaN() || infDivided.isInfinite());
    }

    @Test(timeout = 4000)
    public void testTrigonometricAndTranscendentalNaN() {
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
        assertTrue(Complex.NaN.sqrt1z().isNaN());
        assertTrue(Complex.NaN.tan().isNaN());
        assertTrue(Complex.NaN.tanh().isNaN());
    }

    @Test(timeout = 4000)
    public void testSpecialFunctionInfinities() {
        Complex infReal = new Complex(Double.POSITIVE_INFINITY, 1.0);

        assertTrue(Complex.INF.exp().isNaN() || Complex.INF.exp().isInfinite());
        assertTrue(infReal.cos().isNaN());
        assertTrue(infReal.sin().isNaN());
        assertTrue(Complex.INF.log().isInfinite());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAddNull() {
        Complex.ONE.add(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testSubtractNull() {
        Complex.ONE.subtract(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testMultiplyNull() {
        Complex.ONE.multiply(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testDivideNull() {
        Complex.ONE.divide(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPowNull() {
        Complex.ONE.pow(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (equals, hashCode, etc.)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Complex c1 = new Complex(2.5, 3.5);
        Complex c2 = new Complex(2.5, 3.5);
        Complex c3 = new Complex(2.5, -3.5);
        Complex c4 = new Complex(-2.5, 3.5);

        // Reflexive
        assertTrue(c1.equals(c1));

        // Symmetric
        assertTrue(c1.equals(c2));
        assertTrue(c2.equals(c1));
        assertEquals(c1.hashCode(), c2.hashCode());

        // Different imaginary
        assertFalse(c1.equals(c3));
        // Different real
        assertFalse(c1.equals(c4));

        // Null check
        assertFalse(c1.equals(null));

        // Non-Complex type
        assertFalse(c1.equals("A String"));
        assertFalse(c1.equals(new Double(2.5)));

        // NaN equality contract: all NaNs equal each other and have hashCode == 7
        Complex nan1 = new Complex(Double.NaN, 0.0);
        Complex nan2 = new Complex(0.0, Double.NaN);
        Complex nan3 = new Complex(Double.NaN, Double.NaN);

        assertTrue(nan1.equals(nan2));
        assertTrue(nan2.equals(nan3));
        assertTrue(nan1.equals(Complex.NaN));
        assertFalse(c1.equals(nan1));
        assertFalse(nan1.equals(c1));

        assertEquals(7, Complex.NaN.hashCode());
        assertEquals(7, nan1.hashCode());
        assertEquals(7, nan2.hashCode());
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        Complex c = new Complex(1.23, 4.56);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(c);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Complex deserialized = (Complex) ois.readObject();

        assertEquals(c, deserialized);
        assertEquals(c.getReal(), deserialized.getReal(), EPSILON);
        assertEquals(c.getImaginary(), deserialized.getImaginary(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSubclassCreateComplex() {
        class CustomComplex extends Complex {
            private static final long serialVersionUID = 1L;
            public CustomComplex(double real, double imaginary) {
                super(real, imaginary);
            }
            @Override
            public Complex createComplex(double real, double imaginary) {
                return new CustomComplex(real, imaginary);
            }
        }

        CustomComplex custom = new CustomComplex(1.0, 2.0);
        Complex result = custom.add(new Complex(2.0, 3.0));
        assertTrue(result instanceof CustomComplex);
        assertEquals(3.0, result.getReal(), EPSILON);
        assertEquals(5.0, result.getImaginary(), EPSILON);
    }
}