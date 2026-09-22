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
package org.apache.commons.math3.analysis.differentiation;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects:
 * - Defects4J DerivativeStructureTest::testAtan2SpecialCases:
 *   DSCompiler.atan2(y, x) fails to handle special edge cases (+0/+0, -0/+0, etc.) properly,
 *   evaluating 0 / (r + x) -> 0 / 0 -> NaN instead of returning FastMath.atan2(y, x) (0.0).
 *
 * Targeted Branches & Edge Conditions:
 * 1. Factory & Caching:
 *    - getCompiler(p, o) with cache hit, cache miss requiring resize, diagonal traversal.
 *    - Corner compilers: (0, 0), (0, 1), (1, 0), (3, 4).
 * 2. Indirection Compilation:
 *    - compileDerivativesIndirection: parameters == 0 || order == 0 branch vs general recursion.
 *    - compileLowerIndirection: parameters == 0 || order <= 1 branch vs general case.
 *    - compileMultiplicationIndirection: parameters == 0 || order == 0 branch, term merging loop.
 *    - compileCompositionIndirection: parameters == 0 || order == 0 branch, factor and term derivations.
 * 3. Index & Orders Lookups:
 *    - getPartialDerivativeIndex: mismatch of dimensions (throws DimensionMismatchException).
 *    - getPartialDerivativeIndex: sum of derivation orders > max order (throws NumberIsTooLargeException).
 *    - getPartialDerivativeOrders round-trip verification.
 * 4. Linear Combinations:
 *    - 2-terms, 3-terms, and 4-terms linearCombination variations.
 * 5. Arithmetic Operations:
 *    - add, subtract, multiply, divide, remainder (positive, negative, modulo steps).
 * 6. Exponentiation & Powers:
 *    - pow(double): arbitrary double exponent.
 *    - pow(int): n == 0, n > 0 (order > n vs order <= n), n < 0.
 *    - pow(DS, DS): base and exponent as derivative structures.
 *    - rootN: n == 2 (FastMath.sqrt branch), n == 3 (FastMath.cbrt branch), n > 3 (FastMath.pow branch).
 * 7. Elementary & Transcendental Functions:
 *    - exp, expm1, log, log1p, log10 with order == 0 and order > 0.
 *    - cos, sin with order == 0, 1, and > 1 (alternating parity).
 *    - tan with order up to 4 (exercising polynomial recurrence: k > 2, k == 2, even/odd n).
 *    - acos, asin, atan with order up to 4 (k > 2, k == 2, even/odd degree branches).
 *    - atan2 with x >= 0 and x < 0 (exercising sign branches and defect zone).
 *    - cosh, sinh, tanh (recurrence branches with order >= 4).
 *    - acosh, asinh, atanh (order >= 4 polynomial recurrences).
 * 8. Taylor Expansion & Utility:
 *    - taylor expansion with non-zero and zero deltas.
 *    - checkCompatibility: matching vs parameters mismatch vs order mismatch.
 */
public class DSCompilerGeminiTest {

    private static final double EPSILON = 1e-12;

    // =========================================================================
    // Partition A: Core Functional Logic & Factory Caching
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryCachingAndProperties() {
        DSCompiler c1 = DSCompiler.getCompiler(2, 2);
        DSCompiler c2 = DSCompiler.getCompiler(2, 2);
        assertSame("Compiler instance must be cached and reused", c1, c2);

        assertEquals(2, c1.getFreeParameters());
        assertEquals(2, c1.getOrder());
        // For (p=2, o=2), size is (2+2)!/(2!*2!) = 6
        assertEquals(6, c1.getSize());

        // Request compiler with larger dimensions to force cache expansion
        DSCompiler c3 = DSCompiler.getCompiler(3, 3);
        assertEquals(3, c3.getFreeParameters());
        assertEquals(3, c3.getOrder());
        // For (p=3, o=3), size is (3+3)!/(3!*3!) = 20
        assertEquals(20, c3.getSize());

        // Boundary cases (0 parameters or 0 order)
        DSCompiler c00 = DSCompiler.getCompiler(0, 0);
        assertEquals(0, c00.getFreeParameters());
        assertEquals(0, c00.getOrder());
        assertEquals(1, c00.getSize());

        DSCompiler c10 = DSCompiler.getCompiler(1, 0);
        assertEquals(1, c10.getSize());

        DSCompiler c01 = DSCompiler.getCompiler(0, 1);
        assertEquals(1, c01.getSize());
    }

    @Test(timeout = 4000)
    public void testPartialDerivativeIndexAndOrdersMapping() {
        DSCompiler compiler = DSCompiler.getCompiler(2, 2);

        // 0th order derivative (value) is always at index 0
        int idx00 = compiler.getPartialDerivativeIndex(0, 0);
        assertEquals(0, idx00);
        assertArrayEquals(new int[]{0, 0}, compiler.getPartialDerivativeOrders(0));

        // First parameter derivative: d/dx
        int idx10 = compiler.getPartialDerivativeIndex(1, 0);
        assertEquals(1, idx10);
        assertArrayEquals(new int[]{1, 0}, compiler.getPartialDerivativeOrders(1));

        // Second order: d2/dx2
        int idx20 = compiler.getPartialDerivativeIndex(2, 0);
        assertEquals(2, idx20);
        assertArrayEquals(new int[]{2, 0}, compiler.getPartialDerivativeOrders(2));

        // Second parameter: d/dy
        int idx01 = compiler.getPartialDerivativeIndex(0, 1);
        assertEquals(3, idx01);
        assertArrayEquals(new int[]{0, 1}, compiler.getPartialDerivativeOrders(3));

        // Cross derivative: d2/dxdy
        int idx11 = compiler.getPartialDerivativeIndex(1, 1);
        assertEquals(4, idx11);
        assertArrayEquals(new int[]{1, 1}, compiler.getPartialDerivativeOrders(4));

        // Second order: d2/dy2
        int idx02 = compiler.getPartialDerivativeIndex(0, 2);
        assertEquals(5, idx02);
        assertArrayEquals(new int[]{0, 2}, compiler.getPartialDerivativeOrders(5));
    }

    @Test(timeout = 4000)
    public void testLinearCombinations() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        int sz = compiler.getSize();

        double[] c1 = new double[]{2.0, 1.0};
        double[] c2 = new double[]{3.0, 2.0};
        double[] c3 = new double[]{4.0, -1.0};
        double[] c4 = new double[]{-1.0, 3.0};
        double[] res = new double[sz];

        // 2 terms: a1*c1 + a2*c2 -> 2.0 * [2, 1] + (-1.0) * [3, 2] = [1, 0]
        compiler.linearCombination(2.0, c1, 0, -1.0, c2, 0, res, 0);
        assertEquals(1.0, res[0], EPSILON);
        assertEquals(0.0, res[1], EPSILON);

        // 3 terms: 1*c1 + 2*c2 + 3*c3 -> [2, 1] + [6, 4] + [12, -3] = [20, 2]
        compiler.linearCombination(1.0, c1, 0, 2.0, c2, 0, 3.0, c3, 0, res, 0);
        assertEquals(20.0, res[0], EPSILON);
        assertEquals(2.0, res[1], EPSILON);

        // 4 terms: 1*c1 + 1*c2 + 1*c3 + 1*c4 -> [2+3+4-1, 1+2-1+3] = [8, 5]
        compiler.linearCombination(1.0, c1, 0, 1.0, c2, 0, 1.0, c3, 0, 1.0, c4, 0, res, 0);
        assertEquals(8.0, res[0], EPSILON);
        assertEquals(5.0, res[1], EPSILON);
    }

    @Test(timeout = 4000)
    public void testBasicArithmeticAddSubtractMultiplyDivideRemainder() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 2); // 1 param, order 2 (size 3)
        // u = 5 + 2x, v = 2 + 3x
        double[] u = new double[]{5.0, 2.0, 0.0};
        double[] v = new double[]{2.0, 3.0, 0.0};
        double[] res = new double[compiler.getSize()];

        // Add
        compiler.add(u, 0, v, 0, res, 0);
        assertArrayEquals(new double[]{7.0, 5.0, 0.0}, res, EPSILON);

        // Subtract
        compiler.subtract(u, 0, v, 0, res, 0);
        assertArrayEquals(new double[]{3.0, -1.0, 0.0}, res, EPSILON);

        // Multiply: (5 + 2x)*(2 + 3x) = 10 + 19x + 6x^2; order 2 derivative is 12 (since coeff of x^2 is 6)
        compiler.multiply(u, 0, v, 0, res, 0);
        assertEquals(10.0, res[0], EPSILON);
        assertEquals(19.0, res[1], EPSILON);
        assertEquals(12.0, res[2], EPSILON);

        // Divide: (10 + 19x + 6x^2) / (2 + 3x) = (5 + 2x)
        double[] prod = res.clone();
        compiler.divide(prod, 0, v, 0, res, 0);
        assertEquals(5.0, res[0], EPSILON);
        assertEquals(2.0, res[1], EPSILON);
        assertEquals(0.0, res[2], EPSILON);

        // Remainder: u % v -> 5.0 % 2.0 = 1.0, k = round((5-1)/2) = 2.
        // der: u' - k * v' = 2 - 2 * 3 = -4
        compiler.remainder(u, 0, v, 0, res, 0);
        assertEquals(1.0, res[0], EPSILON);
        assertEquals(-4.0, res[1], EPSILON);
    }

    // =========================================================================
    // Partition B: Powers, Roots, Exponentials & Logarithms
    // =========================================================================

    @Test(timeout = 4000)
    public void testPowDoubleAndIntegerBranches() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 2);
        double[] x = new double[]{2.0, 1.0, 0.0}; // x at 2.0
        double[] res = new double[compiler.getSize()];

        // pow(double)
        compiler.pow(x, 0, 2.5, res, 0);
        // f(2) = 2^2.5 = 4*sqrt(2) approx 5.656854249492381
        assertEquals(FastMath.pow(2.0, 2.5), res[0], EPSILON);
        // f'(2) = 2.5 * 2^1.5 = 5 * sqrt(2) approx 7.071067811865475
        assertEquals(2.5 * FastMath.pow(2.0, 1.5), res[1], EPSILON);

        // pow(int n == 0) branch
        compiler.pow(x, 0, 0, res, 0);
        assertEquals(1.0, res[0], EPSILON);
        assertEquals(0.0, res[1], EPSILON);
        assertEquals(0.0, res[2], EPSILON);

        // pow(int n > 0 with n > order)
        compiler.pow(x, 0, 3, res, 0); // x^3 at 2.0 -> value: 8, f': 3*4=12, f'': 6*2=12
        assertEquals(8.0, res[0], EPSILON);
        assertEquals(12.0, res[1], EPSILON);
        assertEquals(12.0, res[2], EPSILON);

        // pow(int n > 0 with n <= order)
        DSCompiler compOrder3 = DSCompiler.getCompiler(1, 3);
        double[] x3 = new double[]{2.0, 1.0, 0.0, 0.0};
        double[] res3 = new double[compOrder3.getSize()];
        compOrder3.pow(x3, 0, 2, res3, 0); // x^2: value=4, f'=4, f''=2, f'''=0
        assertEquals(4.0, res3[0], EPSILON);
        assertEquals(4.0, res3[1], EPSILON);
        assertEquals(2.0, res3[2], EPSILON);
        assertEquals(0.0, res3[3], EPSILON);

        // pow(int n < 0)
        compiler.pow(x, 0, -1, res, 0); // 1/x: value: 0.5, f': -1/4 = -0.25, f'': 2/8 = 0.25
        assertEquals(0.5, res[0], EPSILON);
        assertEquals(-0.25, res[1], EPSILON);
        assertEquals(0.25, res[2], EPSILON);

        // pow(DS, DS)
        double[] y = new double[]{3.0, 0.0, 0.0}; // constant 3.0
        compiler.pow(x, 0, y, 0, res, 0); // 2^3 = 8
        assertEquals(8.0, res[0], EPSILON);
        assertEquals(12.0, res[1], EPSILON);
    }

    @Test(timeout = 4000)
    public void testRootsBranches() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        double[] x = new double[]{4.0, 1.0};
        double[] res = new double[compiler.getSize()];

        // rootN n == 2 branch (sqrt)
        compiler.rootN(x, 0, 2, res, 0);
        assertEquals(2.0, res[0], EPSILON);
        assertEquals(0.25, res[1], EPSILON); // 1 / (2*sqrt(4)) = 0.25

        // rootN n == 3 branch (cbrt)
        double[] x8 = new double[]{8.0, 1.0};
        compiler.rootN(x8, 0, 3, res, 0);
        assertEquals(2.0, res[0], EPSILON);
        assertEquals(1.0 / (3.0 * 4.0), res[1], EPSILON);

        // rootN n > 3 branch
        double[] x16 = new double[]{16.0, 1.0};
        compiler.rootN(x16, 0, 4, res, 0);
        assertEquals(2.0, res[0], EPSILON);
        assertEquals(1.0 / (4.0 * 8.0), res[1], EPSILON);
    }

    @Test(timeout = 4000)
    public void testExponentialsAndLogarithms() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 2);
        double[] x = new double[]{0.5, 1.0, 0.0};
        double[] res = new double[compiler.getSize()];

        // exp & expm1
        compiler.exp(x, 0, res, 0);
        assertEquals(FastMath.exp(0.5), res[0], EPSILON);
        assertEquals(FastMath.exp(0.5), res[1], EPSILON);

        compiler.expm1(x, 0, res, 0);
        assertEquals(FastMath.expm1(0.5), res[0], EPSILON);
        assertEquals(FastMath.exp(0.5), res[1], EPSILON);

        // log
        compiler.log(x, 0, res, 0);
        assertEquals(FastMath.log(0.5), res[0], EPSILON);
        assertEquals(1.0 / 0.5, res[1], EPSILON);
        assertEquals(-1.0 / (0.5 * 0.5), res[2], EPSILON);

        // log1p
        compiler.log1p(x, 0, res, 0);
        assertEquals(FastMath.log1p(0.5), res[0], EPSILON);
        assertEquals(1.0 / 1.5, res[1], EPSILON);

        // log10
        compiler.log10(x, 0, res, 0);
        assertEquals(FastMath.log10(0.5), res[0], EPSILON);
        assertEquals(1.0 / (0.5 * FastMath.log(10.0)), res[1], EPSILON);

        // 0-order checks to cover (order == 0) branches
        DSCompiler comp0 = DSCompiler.getCompiler(1, 0);
        double[] x0 = new double[]{0.5};
        double[] res0 = new double[1];
        comp0.log(x0, 0, res0, 0);
        assertEquals(FastMath.log(0.5), res0[0], EPSILON);
        comp0.log1p(x0, 0, res0, 0);
        assertEquals(FastMath.log1p(0.5), res0[0], EPSILON);
        comp0.log10(x0, 0, res0, 0);
        assertEquals(FastMath.log10(0.5), res0[0], EPSILON);
    }

    // =========================================================================
    // Partition C: Trigonometric, Inverse & Hyperbolic Recurrences
    // =========================================================================

    @Test(timeout = 4000)
    public void testTrigonometricHigherOrders() {
        // High order (4) to trigger inner loop branches: k > 2, k == 2, even and odd parities
        DSCompiler compiler = DSCompiler.getCompiler(1, 4);
        double[] x = new double[]{0.25, 1.0, 0.0, 0.0, 0.0};
        double[] res = new double[compiler.getSize()];

        // cos & sin
        compiler.cos(x, 0, res, 0);
        assertEquals(FastMath.cos(0.25), res[0], EPSILON);
        assertEquals(-FastMath.sin(0.25), res[1], EPSILON);
        assertEquals(-FastMath.cos(0.25), res[2], EPSILON);
        assertEquals(FastMath.sin(0.25), res[3], EPSILON);
        assertEquals(FastMath.cos(0.25), res[4], EPSILON);

        compiler.sin(x, 0, res, 0);
        assertEquals(FastMath.sin(0.25), res[0], EPSILON);
        assertEquals(FastMath.cos(0.25), res[1], EPSILON);
        assertEquals(-FastMath.sin(0.25), res[2], EPSILON);
        assertEquals(-FastMath.cos(0.25), res[3], EPSILON);
        assertEquals(FastMath.sin(0.25), res[4], EPSILON);

        // tan
        compiler.tan(x, 0, res, 0);
        double t = FastMath.tan(0.25);
        assertEquals(t, res[0], EPSILON);
        assertEquals(1.0 + t * t, res[1], EPSILON);
        assertEquals(2.0 * t * (1.0 + t * t), res[2], EPSILON);
    }

    @Test(timeout = 4000)
    public void testInverseTrigonometricsHigherOrders() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 4);
        double[] x = new double[]{0.3, 1.0, 0.0, 0.0, 0.0};
        double[] res = new double[compiler.getSize()];

        // acos
        compiler.acos(x, 0, res, 0);
        assertEquals(FastMath.acos(0.3), res[0], EPSILON);
        assertEquals(-1.0 / FastMath.sqrt(1.0 - 0.3 * 0.3), res[1], EPSILON);

        // asin
        compiler.asin(x, 0, res, 0);
        assertEquals(FastMath.asin(0.3), res[0], EPSILON);
        assertEquals(1.0 / FastMath.sqrt(1.0 - 0.3 * 0.3), res[1], EPSILON);

        // atan
        compiler.atan(x, 0, res, 0);
        assertEquals(FastMath.atan(0.3), res[0], EPSILON);
        assertEquals(1.0 / (1.0 + 0.3 * 0.3), res[1], EPSILON);
    }

    @Test(timeout = 4000)
    public void testHyperbolicsAndInversesHigherOrders() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 4);
        double[] x = new double[]{0.4, 1.0, 0.0, 0.0, 0.0};
        double[] res = new double[compiler.getSize()];

        // cosh & sinh
        compiler.cosh(x, 0, res, 0);
        assertEquals(FastMath.cosh(0.4), res[0], EPSILON);
        assertEquals(FastMath.sinh(0.4), res[1], EPSILON);

        compiler.sinh(x, 0, res, 0);
        assertEquals(FastMath.sinh(0.4), res[0], EPSILON);
        assertEquals(FastMath.cosh(0.4), res[1], EPSILON);

        // tanh
        compiler.tanh(x, 0, res, 0);
        double th = FastMath.tanh(0.4);
        assertEquals(th, res[0], EPSILON);
        assertEquals(1.0 - th * th, res[1], EPSILON);

        // asinh & atanh
        compiler.asinh(x, 0, res, 0);
        assertEquals(FastMath.asinh(0.4), res[0], EPSILON);
        assertEquals(1.0 / FastMath.sqrt(1.0 + 0.4 * 0.4), res[1], EPSILON);

        compiler.atanh(x, 0, res, 0);
        assertEquals(FastMath.atanh(0.4), res[0], EPSILON);
        assertEquals(1.0 / (1.0 - 0.4 * 0.4), res[1], EPSILON);

        // acosh (requires x > 1)
        double[] xAcosh = new double[]{1.5, 1.0, 0.0, 0.0, 0.0};
        compiler.acosh(xAcosh, 0, res, 0);
        assertEquals(FastMath.acosh(1.5), res[0], EPSILON);
        assertEquals(1.0 / FastMath.sqrt(1.5 * 1.5 - 1.0), res[1], EPSILON);
    }

    @Test(timeout = 4000)
    public void testAtan2PositiveAndNegativeX() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        double[] res = new double[compiler.getSize()];

        // x >= 0 branch
        double[] yPos = new double[]{1.0, 0.0};
        double[] xPos = new double[]{1.0, 1.0};
        compiler.atan2(yPos, 0, xPos, 0, res, 0);
        assertEquals(FastMath.PI / 4.0, res[0], EPSILON);
        // d/dx atan2(y, x) = -y / (x^2 + y^2) = -1 / 2 = -0.5
        assertEquals(-0.5, res[1], EPSILON);

        // x < 0 branch, y > 0 -> tmp2[0] > 0
        double[] xNeg = new double[]{-1.0, 1.0};
        compiler.atan2(yPos, 0, xNeg, 0, res, 0);
        assertEquals(3.0 * FastMath.PI / 4.0, res[0], EPSILON);
        assertEquals(-0.5, res[1], EPSILON);

        // x < 0 branch, y < 0 -> tmp2[0] <= 0
        double[] yNeg = new double[]{-1.0, 0.0};
        compiler.atan2(yNeg, 0, xNeg, 0, res, 0);
        assertEquals(-3.0 * FastMath.PI / 4.0, res[0], EPSILON);
    }

    // =========================================================================
    // Partition D: Defect Zone - Defects4J Atan2 Special Cases
    // =========================================================================

    /**
     * Directly targets known Defects4J failure:
     * DerivativeStructureTest::testAtan2SpecialCases
     * When x = +0.0 and y = +0.0, atan2(y, x) should be +0.0, but without
     * the special cases handling, DSCompiler computes 0 / (r + x) = 0 / 0 = NaN.
     */
    @Test(timeout = 4000)
    public void testAtan2SpecialCasesZeroZero() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        double[] y = new double[]{+0.0, 0.0};
        double[] x = new double[]{+0.0, 1.0};
        double[] result = new double[compiler.getSize()];

        compiler.atan2(y, 0, x, 0, result, 0);

        // This assertion triggers the defect in buggy versions where result[0] is NaN
        assertEquals(0.0, result[0], EPSILON);
    }

    // =========================================================================
    // Partition E: Taylor Series & Compatibility Checks
    // =========================================================================

    @Test(timeout = 4000)
    public void testTaylorExpansion() {
        DSCompiler compiler = DSCompiler.getCompiler(2, 2);
        // f(x, y) = 1 + 2*dx + 3*dy + 4*dx^2/2 + 5*dx*dy + 6*dy^2/2
        // ds array indices: 0: val, 1: dx, 2: dx^2, 3: dy, 4: dxdy, 5: dy^2
        double[] ds = new double[compiler.getSize()];
        ds[compiler.getPartialDerivativeIndex(0, 0)] = 1.0;
        ds[compiler.getPartialDerivativeIndex(1, 0)] = 2.0;
        ds[compiler.getPartialDerivativeIndex(2, 0)] = 4.0;
        ds[compiler.getPartialDerivativeIndex(0, 1)] = 3.0;
        ds[compiler.getPartialDerivativeIndex(1, 1)] = 5.0;
        ds[compiler.getPartialDerivativeIndex(0, 2)] = 6.0;

        // Taylor evaluation at delta = (0.1, 0.2)
        // val = 1 + 2*(0.1) + 3*(0.2) + 4*(0.01)/2 + 5*(0.1*0.2) + 6*(0.04)/2
        //     = 1 + 0.2 + 0.6 + 0.02 + 0.10 + 0.12 = 2.04
        double eval = compiler.taylor(ds, 0, 0.1, 0.2);
        assertEquals(2.04, eval, EPSILON);
    }

    @Test(timeout = 4000)
    public void testCheckCompatibility() {
        DSCompiler c1 = DSCompiler.getCompiler(2, 3);
        DSCompiler c2 = DSCompiler.getCompiler(2, 3);

        // Should succeed without exception
        c1.checkCompatibility(c2);

        // Parameter mismatch
        DSCompiler cDiffParam = DSCompiler.getCompiler(1, 3);
        try {
            c1.checkCompatibility(cDiffParam);
            fail("Expected DimensionMismatchException for parameter count mismatch");
        } catch (DimensionMismatchException expected) {
            assertEquals(2, expected.getArgument());
            assertEquals(1, (int) expected.getDimension());
        }

        // Order mismatch
        DSCompiler cDiffOrder = DSCompiler.getCompiler(2, 2);
        try {
            c1.checkCompatibility(cDiffOrder);
            fail("Expected DimensionMismatchException for order mismatch");
        } catch (DimensionMismatchException expected) {
            assertEquals(3, expected.getArgument());
            assertEquals(2, (int) expected.getDimension());
        }
    }

    // =========================================================================
    // Partition F: Defensive Checks & Exceptions
    // =========================================================================

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testGetPartialDerivativeIndexDimensionMismatch() {
        DSCompiler compiler = DSCompiler.getCompiler(2, 2);
        // Requires 2 arguments, passing 3
        compiler.getPartialDerivativeIndex(1, 0, 0);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testGetPartialDerivativeIndexOrderTooLarge() {
        DSCompiler compiler = DSCompiler.getCompiler(2, 2);
        // Sum of orders is 2 + 1 = 3 > max order 2
        compiler.getPartialDerivativeIndex(2, 1);
    }
}