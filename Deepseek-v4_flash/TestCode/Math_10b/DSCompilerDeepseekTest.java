package org.apache.commons.math3.analysis.differentiation;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: DSCompiler – advanced white-box test suite.
 * 
 * Known defect: atan2 returns NaN for special cases (e.g., (0,0)).
 * The atan2 method has two main branches based on x >= 0.
 * Special cases: (+0,+0), (+0,-0), (-0,+0), (-0,-0), (+inf, ...), (-inf, ...).
 * 
 * Partitions:
 * A: Core functional logic (add, subtract, multiply, divide, remainder, pow, rootN, exp, log, sin, cos, tan, etc.)
 * B: Boundary Value Analysis (zero, negative, large, NaN, infinity)
 * C: Defect-targeted (atan2 special cases)
 * D: Exception & defensive guard paths (DimensionMismatchException, NumberIsTooLargeException)
 * E: Object lifecycle & contract integrity (getCompiler, getSize, getPartialDerivativeIndex, getPartialDerivativeOrders, checkCompatibility)
 * 
 * Branch coverage targets:
 * - atan2: if (x[xOffset] >= 0) { ... } else { ... }
 * - getPartialDerivativeIndex: loop over parameters, while derivativeOrder-- > 0
 * - compileSizes: if (parameters == 0) ... else ...
 * - compileLowerIndirection: if (parameters == 0 || order <= 1)
 * - compileMultiplicationIndirection: if ((parameters == 0) || (order == 0))
 * - compileCompositionIndirection: if ((parameters == 0) || (order == 0))
 * - pow(int n): if (n == 0), if (n > 0) else
 * - rootN: if (n == 2), else if (n == 3), else
 * - tan, tanh, acos, asin, atan, acosh, asinh, atanh: order > 0 branches
 * - compose: loop over compIndirection
 * - taylor: loop over getSize()
 * - checkCompatibility: if parameters !=, if order !=
 */
public class DSCompilerDeepseekTest {

    // Helper to get compiler
    private DSCompiler getCompiler(int parameters, int order) {
        return DSCompiler.getCompiler(parameters, order);
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testAdd() {
        DSCompiler compiler = getCompiler(2, 2);
        int size = compiler.getSize();
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        // Set some values: f=1, df/dx=2, df/dy=3, etc.
        lhs[0] = 1.0;
        lhs[1] = 2.0; // df/dx
        lhs[2] = 3.0; // df/dy
        rhs[0] = 4.0;
        rhs[1] = 5.0;
        rhs[2] = 6.0;
        compiler.add(lhs, 0, rhs, 0, result, 0);
        assertEquals("Value", 5.0, result[0], 1e-15);
        assertEquals("df/dx", 7.0, result[1], 1e-15);
        assertEquals("df/dy", 9.0, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testSubtract() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 10.0;
        lhs[1] = 3.0; // df/dx
        lhs[2] = 1.0; // d2f/dx2
        rhs[0] = 4.0;
        rhs[1] = 1.0;
        rhs[2] = 0.5;
        compiler.subtract(lhs, 0, rhs, 0, result, 0);
        assertEquals("Value", 6.0, result[0], 1e-15);
        assertEquals("df/dx", 2.0, result[1], 1e-15);
        assertEquals("d2f/dx2", 0.5, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testMultiply() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 2.0;
        lhs[1] = 3.0;
        lhs[2] = 1.0;
        rhs[0] = 5.0;
        rhs[1] = 0.0;
        rhs[2] = 0.0;
        compiler.multiply(lhs, 0, rhs, 0, result, 0);
        assertEquals("Value", 10.0, result[0], 1e-15);
        assertEquals("df/dx", 15.0, result[1], 1e-15);
        assertEquals("d2f/dx2", 10.0, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testDivide() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 6.0;
        lhs[1] = 2.0;
        rhs[0] = 3.0;
        rhs[1] = 1.0;
        compiler.divide(lhs, 0, rhs, 0, result, 0);
        assertEquals("Value", 2.0, result[0], 1e-15);
        // derivative: (f'*g - f*g')/g^2 = (2*3 - 6*1)/9 = 0/9 = 0
        assertEquals("df/dx", 0.0, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testRemainder() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 7.0;
        lhs[1] = 2.0;
        rhs[0] = 3.0;
        rhs[1] = 1.0;
        compiler.remainder(lhs, 0, rhs, 0, result, 0);
        assertEquals("Value", 1.0, result[0], 1e-15);
        // derivative: lhs' - k*rhs' where k = floor((lhs - rem)/rhs) = floor((7-1)/3)=2
        assertEquals("df/dx", 2.0 - 2*1.0, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testPowDouble() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 2.0;
        operand[1] = 1.0; // df/dx
        operand[2] = 0.0; // d2f/dx2
        compiler.pow(operand, 0, 3.0, result, 0);
        // f(x)=x^3, f(2)=8, f'=3x^2=12, f''=6x=12
        assertEquals("Value", 8.0, result[0], 1e-15);
        assertEquals("df/dx", 12.0, result[1], 1e-15);
        assertEquals("d2f/dx2", 12.0, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testPowIntZero() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 5.0;
        operand[1] = 2.0;
        compiler.pow(operand, 0, 0, result, 0);
        assertEquals("Value", 1.0, result[0], 1e-15);
        for (int i = 1; i < size; i++) {
            assertEquals("Derivative " + i, 0.0, result[i], 1e-15);
        }
    }

    @Test(timeout = 4000)
    public void testPowIntPositive() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 2.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.pow(operand, 0, 3, result, 0);
        assertEquals("Value", 8.0, result[0], 1e-15);
        assertEquals("df/dx", 12.0, result[1], 1e-15);
        assertEquals("d2f/dx2", 12.0, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testPowIntNegative() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 2.0;
        operand[1] = 1.0;
        compiler.pow(operand, 0, -2, result, 0);
        // f(x)=x^-2, f(2)=0.25, f'=-2x^-3 = -0.25
        assertEquals("Value", 0.25, result[0], 1e-15);
        assertEquals("df/dx", -0.25, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testPowXY() {
        DSCompiler compiler = getCompiler(2, 1);
        int size = compiler.getSize();
        double[] x = new double[size];
        double[] y = new double[size];
        double[] result = new double[size];
        x[0] = 2.0; x[1] = 1.0; x[2] = 0.0; // df/dx, df/dy
        y[0] = 3.0; y[1] = 0.0; y[2] = 1.0;
        compiler.pow(x, 0, y, 0, result, 0);
        // x^y = 2^3 = 8, derivatives via log/exp
        assertEquals("Value", 8.0, result[0], 1e-12);
    }

    @Test(timeout = 4000)
    public void testRootN() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 8.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.rootN(operand, 0, 3, result, 0);
        // cbrt(8)=2, derivative = 1/(3*4)=1/12 ≈ 0.08333
        assertEquals("Value", 2.0, result[0], 1e-15);
        assertEquals("df/dx", 1.0 / 12.0, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testExp() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 1.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.exp(operand, 0, result, 0);
        double e = FastMath.E;
        assertEquals("Value", e, result[0], 1e-15);
        assertEquals("df/dx", e, result[1], 1e-15);
        assertEquals("d2f/dx2", e, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testExpm1() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.0;
        operand[1] = 1.0;
        compiler.expm1(operand, 0, result, 0);
        assertEquals("Value", 0.0, result[0], 1e-15);
        assertEquals("df/dx", 1.0, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testLog() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 2.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.log(operand, 0, result, 0);
        assertEquals("Value", FastMath.log(2.0), result[0], 1e-15);
        assertEquals("df/dx", 0.5, result[1], 1e-15);
        assertEquals("d2f/dx2", -0.25, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testLog1p() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 1.0;
        operand[1] = 1.0;
        compiler.log1p(operand, 0, result, 0);
        assertEquals("Value", FastMath.log1p(1.0), result[0], 1e-15);
        assertEquals("df/dx", 0.5, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testLog10() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 100.0;
        operand[1] = 1.0;
        compiler.log10(operand, 0, result, 0);
        assertEquals("Value", 2.0, result[0], 1e-15);
        assertEquals("df/dx", 1.0 / (100.0 * FastMath.log(10.0)), result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testSin() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.sin(operand, 0, result, 0);
        assertEquals("Value", 0.0, result[0], 1e-15);
        assertEquals("df/dx", 1.0, result[1], 1e-15);
        assertEquals("d2f/dx2", 0.0, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testCos() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.cos(operand, 0, result, 0);
        assertEquals("Value", 1.0, result[0], 1e-15);
        assertEquals("df/dx", 0.0, result[1], 1e-15);
        assertEquals("d2f/dx2", -1.0, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testTan() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.tan(operand, 0, result, 0);
        assertEquals("Value", 0.0, result[0], 1e-15);
        assertEquals("df/dx", 1.0, result[1], 1e-15);
        assertEquals("d2f/dx2", 0.0, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAcos() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.5;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.acos(operand, 0, result, 0);
        assertEquals("Value", FastMath.acos(0.5), result[0], 1e-15);
        // derivative = -1/sqrt(1-x^2) = -1/sqrt(0.75) ≈ -1.1547
        assertEquals("df/dx", -1.0 / FastMath.sqrt(0.75), result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAsin() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.5;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.asin(operand, 0, result, 0);
        assertEquals("Value", FastMath.asin(0.5), result[0], 1e-15);
        assertEquals("df/dx", 1.0 / FastMath.sqrt(0.75), result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAtan() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 1.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.atan(operand, 0, result, 0);
        assertEquals("Value", FastMath.PI / 4, result[0], 1e-15);
        assertEquals("df/dx", 0.5, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testCosh() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.cosh(operand, 0, result, 0);
        assertEquals("Value", 1.0, result[0], 1e-15);
        assertEquals("df/dx", 0.0, result[1], 1e-15);
        assertEquals("d2f/dx2", 1.0, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testSinh() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.sinh(operand, 0, result, 0);
        assertEquals("Value", 0.0, result[0], 1e-15);
        assertEquals("df/dx", 1.0, result[1], 1e-15);
        assertEquals("d2f/dx2", 0.0, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testTanh() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.tanh(operand, 0, result, 0);
        assertEquals("Value", 0.0, result[0], 1e-15);
        assertEquals("df/dx", 1.0, result[1], 1e-15);
        assertEquals("d2f/dx2", 0.0, result[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAcosh() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 2.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.acosh(operand, 0, result, 0);
        assertEquals("Value", FastMath.acosh(2.0), result[0], 1e-15);
        // derivative = 1/sqrt(x^2-1) = 1/sqrt(3) ≈ 0.57735
        assertEquals("df/dx", 1.0 / FastMath.sqrt(3.0), result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAsinh() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 1.0;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.asinh(operand, 0, result, 0);
        assertEquals("Value", FastMath.asinh(1.0), result[0], 1e-15);
        // derivative = 1/sqrt(x^2+1) = 1/sqrt(2) ≈ 0.7071
        assertEquals("df/dx", 1.0 / FastMath.sqrt(2.0), result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAtanh() {
        DSCompiler compiler = getCompiler(1, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.5;
        operand[1] = 1.0;
        operand[2] = 0.0;
        compiler.atanh(operand, 0, result, 0);
        assertEquals("Value", FastMath.atanh(0.5), result[0], 1e-15);
        // derivative = 1/(1-x^2) = 1/(0.75) ≈ 1.3333
        assertEquals("df/dx", 1.0 / 0.75, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testLinearCombination2() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] c1 = new double[size];
        double[] c2 = new double[size];
        double[] result = new double[size];
        c1[0] = 2.0; c1[1] = 3.0;
        c2[0] = 4.0; c2[1] = 5.0;
        compiler.linearCombination(1.0, c1, 0, 2.0, c2, 0, result, 0);
        assertEquals("Value", 2.0 + 2*4.0, result[0], 1e-15);
        assertEquals("df/dx", 3.0 + 2*5.0, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testLinearCombination3() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] c1 = new double[size];
        double[] c2 = new double[size];
        double[] c3 = new double[size];
        double[] result = new double[size];
        c1[0] = 1; c1[1] = 2;
        c2[0] = 3; c2[1] = 4;
        c3[0] = 5; c3[1] = 6;
        compiler.linearCombination(1, c1, 0, 2, c2, 0, 3, c3, 0, result, 0);
        assertEquals("Value", 1+2*3+3*5, result[0], 1e-15);
        assertEquals("df/dx", 2+2*4+3*6, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testLinearCombination4() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] c1 = new double[size];
        double[] c2 = new double[size];
        double[] c3 = new double[size];
        double[] c4 = new double[size];
        double[] result = new double[size];
        c1[0] = 1; c1[1] = 2;
        c2[0] = 3; c2[1] = 4;
        c3[0] = 5; c3[1] = 6;
        c4[0] = 7; c4[1] = 8;
        compiler.linearCombination(1, c1, 0, 2, c2, 0, 3, c3, 0, 4, c4, 0, result, 0);
        assertEquals("Value", 1+2*3+3*5+4*7, result[0], 1e-15);
        assertEquals("df/dx", 2+2*4+3*6+4*8, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testTaylor() {
        DSCompiler compiler = getCompiler(2, 2);
        int size = compiler.getSize();
        double[] ds = new double[size];
        // f = 1, df/dx=2, df/dy=3, d2f/dx2=4, d2f/dxdy=5, d2f/dy2=6
        ds[0] = 1.0;
        ds[1] = 2.0; // df/dx
        ds[2] = 3.0; // df/dy
        ds[3] = 4.0; // d2f/dx2
        ds[4] = 5.0; // d2f/dxdy
        ds[5] = 6.0; // d2f/dy2
        double deltaX = 0.1;
        double deltaY = 0.2;
        double expected = 1.0 + 2*0.1 + 3*0.2 + 0.5*4*0.01 + 5*0.1*0.2 + 0.5*6*0.04;
        double actual = compiler.taylor(ds, 0, deltaX, deltaY);
        assertEquals("Taylor", expected, actual, 1e-15);
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testAddWithZero() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 0.0; lhs[1] = 0.0;
        rhs[0] = 0.0; rhs[1] = 0.0;
        compiler.add(lhs, 0, rhs, 0, result, 0);
        assertEquals(0.0, result[0], 1e-15);
        assertEquals(0.0, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testMultiplyByZero() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 5.0; lhs[1] = 2.0;
        rhs[0] = 0.0; rhs[1] = 0.0;
        compiler.multiply(lhs, 0, rhs, 0, result, 0);
        assertEquals(0.0, result[0], 1e-15);
        assertEquals(0.0, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testDivideByZero() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 1.0; lhs[1] = 0.0;
        rhs[0] = 0.0; rhs[1] = 1.0;
        compiler.divide(lhs, 0, rhs, 0, result, 0);
        // division by zero leads to infinity or NaN; we just check no exception
        assertTrue(Double.isInfinite(result[0]) || Double.isNaN(result[0]));
    }

    @Test(timeout = 4000)
    public void testPowNegativeBase() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = -2.0;
        operand[1] = 1.0;
        compiler.pow(operand, 0, 3.0, result, 0);
        assertEquals(-8.0, result[0], 1e-15);
        // derivative: 3*x^2 = 12
        assertEquals(12.0, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testLogNegative() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = -1.0;
        operand[1] = 1.0;
        compiler.log(operand, 0, result, 0);
        assertTrue(Double.isNaN(result[0]));
    }

    @Test(timeout = 4000)
    public void testSqrtOfNegative() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = -4.0;
        operand[1] = 1.0;
        compiler.rootN(operand, 0, 2, result, 0);
        assertTrue(Double.isNaN(result[0]));
    }

    // ========== Partition C: Defect-Targeted (atan2 special cases) ==========

    @Test(timeout = 4000)
    public void testAtan2SpecialCase_PosZero_PosZero() {
        // atan2(0,0) should be 0.0
        DSCompiler compiler = getCompiler(2, 1);
        int size = compiler.getSize();
        double[] y = new double[size];
        double[] x = new double[size];
        double[] result = new double[size];
        // all zeros
        y[0] = 0.0; y[1] = 0.0; y[2] = 0.0;
        x[0] = 0.0; x[1] = 0.0; x[2] = 0.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals("atan2(0,0) should be 0.0", 0.0, result[0], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAtan2SpecialCase_PosZero_NegZero() {
        // atan2(0, -0) should be pi
        DSCompiler compiler = getCompiler(2, 1);
        int size = compiler.getSize();
        double[] y = new double[size];
        double[] x = new double[size];
        double[] result = new double[size];
        y[0] = 0.0; y[1] = 0.0; y[2] = 0.0;
        x[0] = -0.0; x[1] = 0.0; x[2] = 0.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals("atan2(0, -0) should be pi", FastMath.PI, result[0], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAtan2SpecialCase_NegZero_PosZero() {
        // atan2(-0, 0) should be -0.0
        DSCompiler compiler = getCompiler(2, 1);
        int size = compiler.getSize();
        double[] y = new double[size];
        double[] x = new double[size];
        double[] result = new double[size];
        y[0] = -0.0; y[1] = 0.0; y[2] = 0.0;
        x[0] = 0.0; x[1] = 0.0; x[2] = 0.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals("atan2(-0, 0) should be -0.0", -0.0, result[0], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAtan2SpecialCase_NegZero_NegZero() {
        // atan2(-0, -0) should be -pi
        DSCompiler compiler = getCompiler(2, 1);
        int size = compiler.getSize();
        double[] y = new double[size];
        double[] x = new double[size];
        double[] result = new double[size];
        y[0] = -0.0; y[1] = 0.0; y[2] = 0.0;
        x[0] = -0.0; x[1] = 0.0; x[2] = 0.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals("atan2(-0, -0) should be -pi", -FastMath.PI, result[0], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAtan2SpecialCase_PosInfinity() {
        // atan2(inf, 1) should be pi/2
        DSCompiler compiler = getCompiler(2, 1);
        int size = compiler.getSize();
        double[] y = new double[size];
        double[] x = new double[size];
        double[] result = new double[size];
        y[0] = Double.POSITIVE_INFINITY; y[1] = 0.0; y[2] = 0.0;
        x[0] = 1.0; x[1] = 0.0; x[2] = 0.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals("atan2(inf, 1) should be pi/2", FastMath.PI / 2, result[0], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAtan2SpecialCase_NegInfinity() {
        // atan2(-inf, 1) should be -pi/2
        DSCompiler compiler = getCompiler(2, 1);
        int size = compiler.getSize();
        double[] y = new double[size];
        double[] x = new double[size];
        double[] result = new double[size];
        y[0] = Double.NEGATIVE_INFINITY; y[1] = 0.0; y[2] = 0.0;
        x[0] = 1.0; x[1] = 0.0; x[2] = 0.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals("atan2(-inf, 1) should be -pi/2", -FastMath.PI / 2, result[0], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAtan2BranchXNegative() {
        // Test the else branch: x < 0
        DSCompiler compiler = getCompiler(2, 1);
        int size = compiler.getSize();
        double[] y = new double[size];
        double[] x = new double[size];
        double[] result = new double[size];
        y[0] = 1.0; y[1] = 0.0; y[2] = 0.0;
        x[0] = -1.0; x[1] = 0.0; x[2] = 0.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        // atan2(1, -1) = 3pi/4
        assertEquals("atan2(1, -1) should be 3pi/4", 3.0 * FastMath.PI / 4.0, result[0], 1e-15);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testGetPartialDerivativeIndexWrongLength() {
        DSCompiler compiler = getCompiler(2, 2);
        compiler.getPartialDerivativeIndex(1); // only 1 order, but need 2
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testGetPartialDerivativeIndexSumTooLarge() {
        DSCompiler compiler = getCompiler(2, 2);
        compiler.getPartialDerivativeIndex(2, 1); // sum=3 > order=2
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testCheckCompatibilityParametersMismatch() {
        DSCompiler compiler1 = getCompiler(2, 2);
        DSCompiler compiler2 = getCompiler(3, 2);
        compiler1.checkCompatibility(compiler2);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testCheckCompatibilityOrderMismatch() {
        DSCompiler compiler1 = getCompiler(2, 2);
        DSCompiler compiler2 = getCompiler(2, 3);
        compiler1.checkCompatibility(compiler2);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testGetCompilerCaching() {
        DSCompiler c1 = getCompiler(3, 4);
        DSCompiler c2 = getCompiler(3, 4);
        assertSame("Same compiler should be returned", c1, c2);
    }

    @Test(timeout = 4000)
    public void testGetSize() {
        DSCompiler compiler = getCompiler(2, 2);
        // size = C(2+2,2) = C(4,2)=6
        assertEquals(6, compiler.getSize());
    }

    @Test(timeout = 4000)
    public void testGetFreeParameters() {
        DSCompiler compiler = getCompiler(3, 1);
        assertEquals(3, compiler.getFreeParameters());
    }

    @Test(timeout = 4000)
    public void testGetOrder() {
        DSCompiler compiler = getCompiler(1, 5);
        assertEquals(5, compiler.getOrder());
    }

    @Test(timeout = 4000)
    public void testGetPartialDerivativeIndexAndOrdersRoundTrip() {
        DSCompiler compiler = getCompiler(2, 2);
        int[] orders = {1, 0};
        int index = compiler.getPartialDerivativeIndex(orders);
        int[] retrieved = compiler.getPartialDerivativeOrders(index);
        assertArrayEquals(orders, retrieved);
    }

    @Test(timeout = 4000)
    public void testGetPartialDerivativeIndexZero() {
        DSCompiler compiler = getCompiler(2, 2);
        assertEquals(0, compiler.getPartialDerivativeIndex(0, 0));
    }

    @Test(timeout = 4000)
    public void testGetPartialDerivativeOrdersZero() {
        DSCompiler compiler = getCompiler(2, 2);
        int[] orders = compiler.getPartialDerivativeOrders(0);
        assertArrayEquals(new int[]{0, 0}, orders);
    }

    @Test(timeout = 4000)
    public void testComposeSimple() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] f = new double[2]; // f and f'
        double[] result = new double[size];
        operand[0] = 1.0;
        operand[1] = 2.0;
        f[0] = 3.0; // f(1)
        f[1] = 4.0; // f'(1)
        compiler.compose(operand, 0, f, result, 0);
        assertEquals("Value", 3.0, result[0], 1e-15);
        assertEquals("df/dx", 4.0 * 2.0, result[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testPowIntZeroOrderZero() {
        DSCompiler compiler = getCompiler(1, 0);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 10.0;
        compiler.pow(operand, 0, 0, result, 0);
        assertEquals(1.0, result[0], 1e-15);
    }

    @Test(timeout = 4000)
    public void testRootN2() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 4.0;
        operand[1] = 1.0;
        compiler.rootN(operand, 0, 2, result, 0);
        assertEquals(2.0, result[0], 1e-15);
        assertEquals(0.25, result[1], 1e-15); // 1/(2*sqrt(4)) = 0.25
    }

    @Test(timeout = 4000)
    public void testRootN3() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 27.0;
        operand[1] = 1.0;
        compiler.rootN(operand, 0, 3, result, 0);
        assertEquals(3.0, result[0], 1e-15);
        assertEquals(1.0 / 27.0, result[1], 1e-15); // 1/(3*3^2)=1/27
    }

    @Test(timeout = 4000)
    public void testRootNGeneral() {
        DSCompiler compiler = getCompiler(1, 1);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 16.0;
        operand[1] = 1.0;
        compiler.rootN(operand, 0, 4, result, 0);
        assertEquals(2.0, result[0], 1e-15);
        // derivative: 1/(4 * 2^3) = 1/32 = 0.03125
        assertEquals(1.0 / 32.0, result[1], 1e-15);
    }
}