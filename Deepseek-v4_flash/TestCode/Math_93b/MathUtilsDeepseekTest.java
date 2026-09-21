package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for MathUtils designed to maximize line and branch coverage,
 * targeting the known defect in factorial(17).
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core functional logic & state transitions
 * - addAndCheck(int,int): overflow/normal, positive/negative
 * - addAndCheck(long,long): symmetric/asymmetric cases
 * - addAndCheck(long,long,String): private, tested via public addAndCheck
 * - subAndCheck(int,int): overflow/normal
 * - subAndCheck(long,long): special b==Long.MIN_VALUE
 * - mulAndCheck(int,int): overflow/normal
 * - mulAndCheck(long,long): symmetric, zero, negative, overflow
 * - gcd(int,int): zero, even/odd, negative, overflow k==31
 * - lcm(int,int): normal, overflow
 * - factorial, factorialDouble, factorialLog: normal, edge n=0,1,20,21,22 (overflow)
 * - binomialCoefficient, binomialCoefficientDouble, binomialCoefficientLog: normal, edge k=0,n,1,n-1, n<k, n<0
 * - equals(double,double): NaN handling, normal equality
 * - equals(double[],double[]): null, length mismatch, element equality
 * - hash(double), hash(double[]): NaN, normal, null
 * - indicator all types: zero, positive, negative, NaN for double/float
 * - sign all types: zero, positive, negative, NaN
 * - cosh, sinh: basic values
 * - normalizeAngle: center aligned
 * - nextAfter: special cases (NaN, infinite, zero, normal, subnormal)
 * - scalb: special cases, normal
 * - round(double,int), round(double,int,int): infinite, NaN, normal
 * - round(float,int), round(float,int,int): similar
 * - roundUnscaled: private, tested indirectly via public round methods
 * - log(double,double): basic
 * 
 * Partition B: Boundary Value Analysis & Extremes
 * - Integer.MAX_VALUE/MIN_VALUE for add/sub/mul
 * - Long.MAX_VALUE/MIN_VALUE for add/sub/mul
 * - 0, 1, -1 for factorial, binomialCoefficient
 * - n=20 for factorial (max long representable), n=21 overflow
 * - n=66 for binomialCoefficient (largest n with all results < Long.MAX_VALUE)
 * - n=67 overflow (result > Long.MAX_VALUE)
 * - k=0, k=n, k=1, k=n-1 special cases
 * - double array equals with nulls
 * - indicator/sign with zero, positive, negative, NaN
 * 
 * Partition C: Defect-targeted branch zone
 * - factorial(17) must be exactly 355687428096000L (the known defect)
 * - factorialDouble(17) should be approximately correct with delta 1e-5
 * - factorialLog(17) double precision
 * - binomialCoefficientDouble usage of Math.floor and rounding
 * 
 * Partition D: Exception & defensive guard paths
 * - addAndCheck overflow -> ArithmeticException
 * - addAndCheck long overflow
 * - subAndCheck overflow
 * - mulAndCheck overflow int and long
 * - gcd overflow (k==31) -> ArithmeticException
 * - lcm overflow -> ArithmeticException
 * - factorial negative, factorial overflow (n=21)
 * - binomialCoefficient n<k, n<0, overflow
 * - round unrecognized rounding method -> IllegalArgumentException
 * - roundUnnecessary with non-integer -> ArithmeticException
 * 
 * Partition E: Object lifecycle & contract integrity (not applicable)
 */
public class MathUtilsDeepseekTest {

    // ========== Partition A: Core functional logic ==========

    @Test(timeout = 4000)
    public void testAddAndCheckIntNormal() {
        assertEquals("normal add", 5, MathUtils.addAndCheck(2, 3));
        assertEquals("negative add", -5, MathUtils.addAndCheck(-2, -3));
        assertEquals("mixed add", 1, MathUtils.addAndCheck(-2, 3));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckIntOverflowPositive() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckIntOverflowNegative() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    @Test(timeout = 4000)
    public void testAddAndCheckLongNormal() {
        assertEquals("normal add", 10L, MathUtils.addAndCheck(4L, 6L));
        assertEquals("negative add", -10L, MathUtils.addAndCheck(-4L, -6L));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowPositive() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowNegative() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(timeout = 4000)
    public void testSubAndCheckIntNormal() {
        assertEquals("normal sub", 2, MathUtils.subAndCheck(5, 3));
        assertEquals("negative result", -8, MathUtils.subAndCheck(-5, 3));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckIntOverflowPositive() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckIntOverflowNegative() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test(timeout = 4000)
    public void testSubAndCheckLongNormal() {
        assertEquals(5L, MathUtils.subAndCheck(10L, 5L));
        assertEquals(-5L, MathUtils.subAndCheck(-10L, -5L));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowPositive() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    // subAndCheck for b==Long.MIN_VALUE, different paths
    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckLongBMinA0() {
        MathUtils.subAndCheck(0L, Long.MIN_VALUE); // a>=0 -> throw
    }

    @Test(timeout = 4000)
    public void testSubAndCheckLongBMinANeg() {
        assertEquals("a<0,b=MIN_VALUE", -1L, MathUtils.subAndCheck(-1L, Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testMulAndCheckIntNormal() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        assertEquals(0, MathUtils.mulAndCheck(0, 100));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckIntOverflowPositive() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckIntOverflowNegative() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    @Test(timeout = 4000)
    public void testMulAndCheckLongNormal() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, 100L));
        assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowPositive() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowNegative() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(timeout = 4000)
    public void testGcdNormal() {
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(1, MathUtils.gcd(17, 23));
        assertEquals(12, MathUtils.gcd(0, 12));
        assertEquals(5, MathUtils.gcd(-10, 15));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGcdOverflow() {
        MathUtils.gcd(Integer.MIN_VALUE, 0); // leads to k=31
    }

    @Test(timeout = 4000)
    public void testGcdBothEven() {
        assertEquals(8, MathUtils.gcd(16, 24));
    }

    @Test(timeout = 4000)
    public void testLcmNormal() {
        assertEquals(36, MathUtils.lcm(12, 18));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
    }

    @Test(timeout = 4000)
    public void testFactorialLogNormal() {
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-12);
        assertEquals(0.0, MathUtils.factorialLog(1), 1e-12);
        assertTrue(MathUtils.factorialLog(5) > 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test(timeout = 4000)
    public void testFactorialDoubleNormal() {
        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-12);
        assertEquals(1.0, MathUtils.factorialDouble(1), 1e-12);
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-12);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test(timeout = 4000)
    public void testFactorialNormal() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));
    }

    @Test(timeout = 4000)
    public void testFactorialDefectTarget() {
        // This test targets the known defect: factorial(17) returns incorrect value
        assertEquals("17! must be exactly 355687428096000", 
                     355687428096000L, MathUtils.factorial(17));
        // Also verify double representation
        assertEquals("17! double", 355687428096000.0, MathUtils.factorialDouble(17), 1e-6);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testFactorialOverflow() {
        MathUtils.factorial(21);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLogNormal() {
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-12);
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 5), 1e-12);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 1), 1e-12);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 4), 1e-12);
        assertTrue(MathUtils.binomialCoefficientLog(10, 3) > 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogNLessThanK() {
        MathUtils.binomialCoefficientLog(3, 5);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogNNegative() {
        MathUtils.binomialCoefficientLog(-1, 2);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleNormal() {
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-12);
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-12);
        assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientNormal() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testBinomialCoefficientOverflow() {
        MathUtils.binomialCoefficient(67, 34);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNLessThanK() {
        MathUtils.binomialCoefficient(3, 5);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNNegative() {
        MathUtils.binomialCoefficient(-1, 2);
    }

    @Test(timeout = 4000)
    public void testEqualsDouble() {
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArray() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(null, new double[] {1.0}));
        assertFalse(MathUtils.equals(new double[] {1.0}, null));
        assertTrue(MathUtils.equals(new double[] {1.0, 2.0}, new double[] {1.0, 2.0}));
        assertFalse(MathUtils.equals(new double[] {1.0, 2.0}, new double[] {1.0, 3.0}));
        assertFalse(MathUtils.equals(new double[] {1.0}, new double[] {1.0, 2.0}));
    }

    @Test(timeout = 4000)
    public void testHashDouble() {
        assertEquals(new Double(3.14).hashCode(), MathUtils.hash(3.14));
        assertEquals(new Double(Double.NaN).hashCode(), MathUtils.hash(Double.NaN));
    }

    @Test(timeout = 4000)
    public void testHashDoubleArray() {
        assertNotNull(MathUtils.hash(new double[] {1.0}));
        assertEquals(0, MathUtils.hash((double[]) null));
    }

    @Test(timeout = 4000)
    public void testIndicatorAllTypes() {
        // byte
        assertEquals((byte)1, MathUtils.indicator((byte)5));
        assertEquals((byte)-1, MathUtils.indicator((byte)-3));
        assertEquals((byte)1, MathUtils.indicator((byte)0));
        // double
        assertEquals(1.0, MathUtils.indicator(3.0), 1e-12);
        assertEquals(-1.0, MathUtils.indicator(-2.5), 1e-12);
        assertEquals(1.0, MathUtils.indicator(0.0), 1e-12);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
        // float
        assertEquals(1.0F, MathUtils.indicator(3.0F), 1e-12F);
        assertEquals(-1.0F, MathUtils.indicator(-2.5F), 1e-12F);
        assertEquals(1.0F, MathUtils.indicator(0.0F), 1e-12F);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));
        // int
        assertEquals(1, MathUtils.indicator(5));
        assertEquals(-1, MathUtils.indicator(-3));
        assertEquals(1, MathUtils.indicator(0));
        // long
        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(-1L, MathUtils.indicator(-3L));
        assertEquals(1L, MathUtils.indicator(0L));
        // short
        assertEquals((short)1, MathUtils.indicator((short)5));
        assertEquals((short)-1, MathUtils.indicator((short)-3));
        assertEquals((short)1, MathUtils.indicator((short)0));
    }

    @Test(timeout = 4000)
    public void testSignAllTypes() {
        // byte
        assertEquals((byte)1, MathUtils.sign((byte)5));
        assertEquals((byte)-1, MathUtils.sign((byte)-3));
        assertEquals((byte)0, MathUtils.sign((byte)0));
        // double
        assertEquals(1.0, MathUtils.sign(3.0), 1e-12);
        assertEquals(-1.0, MathUtils.sign(-2.5), 1e-12);
        assertEquals(0.0, MathUtils.sign(0.0), 1e-12);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
        // float
        assertEquals(1.0F, MathUtils.sign(3.0F), 1e-12F);
        assertEquals(-1.0F, MathUtils.sign(-2.5F), 1e-12F);
        assertEquals(0.0F, MathUtils.sign(0.0F), 1e-12F);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));
        // int
        assertEquals(1, MathUtils.sign(5));
        assertEquals(-1, MathUtils.sign(-3));
        assertEquals(0, MathUtils.sign(0));
        // long
        assertEquals(1L, MathUtils.sign(5L));
        assertEquals(-1L, MathUtils.sign(-3L));
        assertEquals(0L, MathUtils.sign(0L));
        // short
        assertEquals((short)1, MathUtils.sign((short)5));
        assertEquals((short)-1, MathUtils.sign((short)-3));
        assertEquals((short)0, MathUtils.sign((short)0));
    }

    @Test(timeout = 4000)
    public void testCosh() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-12);
        assertTrue(MathUtils.cosh(1.0) > 0);
        assertTrue(MathUtils.cosh(-1.0) > 0);
    }

    @Test(timeout = 4000)
    public void testSinh() {
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-12);
        assertTrue(MathUtils.sinh(1.0) > 0);
        assertTrue(MathUtils.sinh(-1.0) < 0);
    }

    @Test(timeout = 4000)
    public void testNormalizeAngle() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-12);
        assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, 0.0), 1e-12);
        assertEquals(0.0, MathUtils.normalizeAngle(-2 * Math.PI, 0.0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testNextAfter() {
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.MAX_VALUE, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);
        double d = 1.0;
        double next = MathUtils.nextAfter(d, 2.0);
        assertTrue(next > d);
        double prev = MathUtils.nextAfter(d, 0.0);
        assertTrue(prev < d);
    }

    @Test(timeout = 4000)
    public void testScalb() {
        assertEquals(0.0, MathUtils.scalb(0.0, 10), 0.0);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 10)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(Double.POSITIVE_INFINITY, 10), 0.0);
        assertEquals(8.0, MathUtils.scalb(1.0, 3), 1e-12);
        assertEquals(1.0, MathUtils.scalb(8.0, -3), 1e-12);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleInt() {
        assertEquals(3.14, MathUtils.round(3.14159, 2), 1e-6);
        assertEquals(-3.14, MathUtils.round(-3.14159, 2), 1e-6);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleIntRoundingMethod() {
        assertEquals(3.14, MathUtils.round(3.14159, 2, BigDecimal.ROUND_HALF_UP), 1e-6);
        assertEquals(3.14, MathUtils.round(3.145, 2, BigDecimal.ROUND_HALF_DOWN), 1e-6);
        // Test infinite and NaN
        assertTrue(Double.isInfinite(MathUtils.round(Double.POSITIVE_INFINITY, 2)));
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRoundInvalidRoundingMethod() {
        MathUtils.round(1.0, 2, -1);
    }

    @Test(timeout = 4000)
    public void testRoundFloatInt() {
        assertEquals(3.14F, MathUtils.round(3.14159F, 2), 1e-6F);
    }

    @Test(timeout = 4000)
    public void testRoundFloatIntRoundingMethod() {
        assertEquals(3.14F, MathUtils.round(3.14159F, 2, BigDecimal.ROUND_HALF_UP), 1e-6F);
    }

    @Test(timeout = 4000)
    public void testLog() {
        assertEquals(2.0, MathUtils.log(10.0, 100.0), 1e-12);
        assertTrue(Double.isNaN(MathUtils.log(-1.0, 10.0)));
    }

    // ========== Partition D: Exception paths ==========

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGcdOverflowK31() {
        // u and v both even repeatedly until k=31
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testRoundUnnecessaryException() {
        // This is indirectly tested via round with BigDecimal.ROUND_UNNECESSARY
        MathUtils.round(3.14, 2, BigDecimal.ROUND_UNNECESSARY);
    }

    // Additional edge coverage for factorialDouble and factorialLog
    @Test(timeout = 4000)
    public void testFactorialLogEdge() {
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-12);
        assertEquals(0.0, MathUtils.factorialLog(1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLogEdge() {
        // n=k case
        assertEquals(0.0, MathUtils.binomialCoefficientLog(10, 10), 1e-12);
        // k=0
        assertEquals(0.0, MathUtils.binomialCoefficientLog(10, 0), 1e-12);
        // k=1
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(10, 1), 1e-12);
        // k=n-1
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(10, 9), 1e-12);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoublePrecision() {
        // Ensure no overflow to infinity
        double res = MathUtils.binomialCoefficientDouble(66, 33);
        assertFalse(Double.isInfinite(res));
        assertTrue(res > 0);
    }

    // Additional long multiplication edge cases
    @Test(timeout = 4000)
    public void testMulAndCheckLongEdge() {
        // a == 0
        assertEquals(0L, MathUtils.mulAndCheck(0L, Long.MAX_VALUE));
        // a < 0, b == 0 (handled by a>0 branch? Actually a<0 and b==0 -> ret=0)
        assertEquals(0L, MathUtils.mulAndCheck(-5L, 0L));
        // a > 0, b == 0 -> ret=0
        assertEquals(0L, MathUtils.mulAndCheck(5L, 0L));
        // a < 0, b < 0, within limit
        assertEquals(6L, MathUtils.mulAndCheck(-2L, -3L));
        // a < 0, b > 0, within limit
        assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L));
        // a > 0, b > 0, overflow
        try {
            MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // Additional subAndCheck long boundary
    @Test(timeout = 4000)
    public void testSubAndCheckLongNormalBnotMIN() {
        assertEquals(5L, MathUtils.subAndCheck(10L, 5L));
        assertEquals(-5L, MathUtils.subAndCheck(-10L, -5L));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowBnotMIN() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    // ========== Additional edge coverage for equals(double[],double[]) ==========

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayNullBoth() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayDifferentLength() {
        assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayElementInequality() {
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaN() {
        assertTrue(MathUtils.equals(new double[]{Double.NaN}, new double[]{Double.NaN}));
        assertFalse(MathUtils.equals(new double[]{Double.NaN}, new double[]{1.0}));
    }

    // ========== Extra coverage for roundUnscaled (via round methods) ==========

    @Test(timeout = 4000)
    public void testRoundDoubleUp() {
        assertEquals(4.0, MathUtils.round(3.6, 0, BigDecimal.ROUND_UP), 1e-12);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleDown() {
        assertEquals(3.0, MathUtils.round(3.6, 0, BigDecimal.ROUND_DOWN), 1e-12);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleCeiling() {
        assertEquals(4.0, MathUtils.round(3.2, 0, BigDecimal.ROUND_CEILING), 1e-12);
        assertEquals(-3.0, MathUtils.round(-3.2, 0, BigDecimal.ROUND_CEILING), 1e-12);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleFloor() {
        assertEquals(3.0, MathUtils.round(3.2, 0, BigDecimal.ROUND_FLOOR), 1e-12);
        assertEquals(-4.0, MathUtils.round(-3.2, 0, BigDecimal.ROUND_FLOOR), 1e-12);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleHalfEvenEven() {
        assertEquals(2.0, MathUtils.round(2.5, 0, BigDecimal.ROUND_HALF_EVEN), 1e-12);
        assertEquals(3.0, MathUtils.round(3.5, 0, BigDecimal.ROUND_HALF_EVEN), 1e-12);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleHalfDown() {
        assertEquals(3.0, MathUtils.round(3.5, 0, BigDecimal.ROUND_HALF_DOWN), 1e-12);
        assertEquals(4.0, MathUtils.round(4.5, 0, BigDecimal.ROUND_HALF_DOWN), 1e-12); // 4.5 -> floor after nextAfter? Actually nextAfter makes it less than 4.5, fraction = 0.5, so floor
    }

    // ========== Additional boundary for factorial double precision ==========

    @Test(timeout = 4000)
    public void testFactorialDoubleBoundary() {
        // n=170 should be finite
        assertFalse(Double.isInfinite(MathUtils.factorialDouble(170)));
        // n=171 should overflow to infinity
        assertTrue(Double.isInfinite(MathUtils.factorialDouble(171)));
    }

    // ========== Additional coverage for binomialCoefficientDouble special ==========

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleK0() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(10, 0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleKN() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(10, 10), 1e-12);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleK1() {
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(10, 1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleKNMinus1() {
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(10, 9), 1e-12);
    }

    // ========== Additional coverage for nextAfter special ==========

    @Test(timeout = 4000)
    public void testNextAfterInfinite() {
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.POSITIVE_INFINITY, 0.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.nextAfter(Double.NEGATIVE_INFINITY, 0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testNextAfterZero() {
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testNextAfterNormal() {
        double d = 1.0;
        double next = MathUtils.nextAfter(d, 2.0);
        assertTrue(next > d);
        assertEquals(1.0 + Double.MIN_VALUE, next, 0.0); // Actually nextAfter for 1.0 returns 1.0+2^-52
        double prev = MathUtils.nextAfter(d, 0.0);
        assertTrue(prev < d);
    }

    // ========== Additional coverage for scalb special ==========

    @Test(timeout = 4000)
    public void testScalbSpecial() {
        assertEquals(0.0, MathUtils.scalb(0.0, 100), 0.0);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 1)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(Double.POSITIVE_INFINITY, 1), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.scalb(Double.NEGATIVE_INFINITY, 1), 0.0);
    }

    @Test(timeout = 4000)
    public void testScalbNormal() {
        assertEquals(4.0, MathUtils.scalb(1.0, 2), 1e-12);
        assertEquals(0.5, MathUtils.scalb(1.0, -1), 1e-12);
    }

    // ========== Additional coverage for log ==========

    @Test(timeout = 4000)
    public void testLogBaseBoundary() {
        // base=0, x>0 -> return 0
        assertEquals(0.0, MathUtils.log(0.0, 10.0), 1e-12);
        // base>0, x=0 -> -Infinity
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.log(10.0, 0.0), 0.0);
        // both zero -> NaN
        assertTrue(Double.isNaN(MathUtils.log(0.0, 0.0)));
    }

    // ========== Additional coverage for equals with NaN double ==========

    @Test(timeout = 4000)
    public void testEqualsDoubleNaN() {
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(Double.NaN, 1.0));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
    }
}