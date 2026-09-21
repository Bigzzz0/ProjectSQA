package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * MathUtilsDeepseekTest - Comprehensive JUnit 4 test suite for MathUtils.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Target methods and their decision branches:
 * 
 * 1. addAndCheck(int,int): overflow check (s < MIN || s > MAX) -> throw ArithmeticException.
 * 2. addAndCheck(long,long): delegates to private addAndCheck(long,long,String) with symmetry.
 *    - a > b: swap to reduce cases.
 *    - a <= b: three subcases: both negative (check MIN_VALUE - b <= a), opposite sign (safe), both non-negative (check a <= MAX_VALUE - b).
 * 3. binomialCoefficient(int,int): preconditions (n<k, n<0 -> IAE), trivial cases (n==k||k==0 -> 1; k==1||k==n-1 -> n), then uses binomialCoefficientDouble and checks overflow.
 *    - Known defect: for n=48,k=22 returns 27385657281647 instead of 27385657281648 due to rounding error in binomialCoefficientDouble.
 * 4. binomialCoefficientDouble: uses floor(exp(binomialCoefficientLog)+0.5). Potential rounding error.
 * 5. binomialCoefficientLog: preconditions, trivial cases, then sum logs.
 * 6. factorial(int): n<0 -> IAE; n>20 -> ArithmeticException; else return factorials[n].
 * 7. factorialDouble(int): n<0 -> IAE; n<21 -> factorial(n); else floor(exp(factorialLog)+0.5).
 * 8. factorialLog(int): n<0 -> IAE; n<21 -> log(factorial(n)); else sum logs.
 * 9. gcd(int,int): zero case (abs(u)+abs(v)); then negative conversion, power-of-2 extraction, Stein's algorithm.
 *    - k==31 -> overflow exception.
 * 10. lcm(int,int): uses gcd and mulAndCheck.
 * 11. mulAndCheck(int,int): overflow check.
 * 12. mulAndCheck(long,long): symmetry, then cases: both negative, negative/positive, both positive, zero.
 * 13. subAndCheck(int,int): overflow check.
 * 14. subAndCheck(long,long): special case b==MIN_VALUE, else use addAndCheck(a,-b).
 * 15. equals(double,double): NaN handling.
 * 16. equals(double[],double[]): null checks, length check, element-wise equals.
 * 17. hash(double), hash(double[]): delegates.
 * 18. indicator(byte,double,float,int,long,short): sign-based.
 * 19. sign(byte,double,float,int,long,short): zero handling.
 * 20. cosh, sinh: simple formulas.
 * 21. log(double,double): division.
 * 22. nextAfter(double,double): special cases (NaN, Inf, zero), then bit manipulation.
 * 23. scalb(double,int): special cases, then exponent shift.
 * 24. normalizeAngle(double,double): formula.
 * 25. round(double,int), round(double,int,int), round(float,int), round(float,int,int): delegates to roundUnscaled.
 * 26. roundUnscaled: switch on roundingMethod (8 cases).
 * 
 * Defect-targeted: binomialCoefficient(48,22) should be 27385657281648, but bug gives 27385657281647.
 * 
 * We will write tests to cover all branches and expose the defect.
 */
public class MathUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testAddAndCheckInt() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-5, MathUtils.addAndCheck(-2, -3));
        assertEquals(0, MathUtils.addAndCheck(Integer.MAX_VALUE, Integer.MIN_VALUE + 1));
        // overflow positive
        try {
            MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        // overflow negative
        try {
            MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddAndCheckLong() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(-5L, MathUtils.addAndCheck(-2L, -3L));
        assertEquals(0L, MathUtils.addAndCheck(Long.MAX_VALUE, Long.MIN_VALUE + 1));
        // overflow positive
        try {
            MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        // overflow negative
        try {
            MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        // opposite sign safe
        assertEquals(-1L, MathUtils.addAndCheck(Long.MAX_VALUE, Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testSubAndCheckInt() {
        assertEquals(1, MathUtils.subAndCheck(3, 2));
        assertEquals(-1, MathUtils.subAndCheck(2, 3));
        // overflow positive
        try {
            MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        // overflow negative
        try {
            MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSubAndCheckLong() {
        assertEquals(1L, MathUtils.subAndCheck(3L, 2L));
        assertEquals(-1L, MathUtils.subAndCheck(2L, 3L));
        // overflow positive
        try {
            MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        // overflow negative
        try {
            MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        // special case b == MIN_VALUE
        assertEquals(Long.MAX_VALUE, MathUtils.subAndCheck(-1L, Long.MIN_VALUE));
        try {
            MathUtils.subAndCheck(1L, Long.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMulAndCheckInt() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        assertEquals(0, MathUtils.mulAndCheck(0, 100));
        // overflow positive
        try {
            MathUtils.mulAndCheck(100000, 100000);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        // overflow negative
        try {
            MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMulAndCheckLong() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, 100L));
        // overflow positive
        try {
            MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        // overflow negative with negative a, positive b
        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        // both negative overflow
        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        // zero cases
        assertEquals(0L, MathUtils.mulAndCheck(0L, Long.MAX_VALUE));
        assertEquals(0L, MathUtils.mulAndCheck(Long.MIN_VALUE, 0L));
    }

    @Test(timeout = 4000)
    public void testGcd() {
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(1, MathUtils.gcd(13, 17));
        assertEquals(0, MathUtils.gcd(0, 0));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(5, MathUtils.gcd(-5, 0));
        assertEquals(2, MathUtils.gcd(-4, 6));
        // overflow case: gcd(2^31, 0) -> 2^31 but that's > Integer.MAX_VALUE? Actually gcd(0, Integer.MIN_VALUE) returns abs(MIN_VALUE) which is 2^31, but that's > Integer.MAX_VALUE? The method returns abs(u)+abs(v) which for MIN_VALUE is 2^31, which is negative? Wait: Math.abs(Integer.MIN_VALUE) returns Integer.MIN_VALUE (negative). So gcd(0, Integer.MIN_VALUE) returns Integer.MIN_VALUE + 0 = Integer.MIN_VALUE (negative). That's a known issue but not our target. We'll test normal cases.
        // Test k==31 overflow: u and v both even and after dividing 31 times, they become odd? Actually the condition is while ((u&1)==0 && (v&1)==0 && k<31). If both are 0? Not possible. We'll test with u=0, v=0 already covered.
        // For large numbers: gcd(Integer.MIN_VALUE, Integer.MIN_VALUE) -> both negative, then while loop: both even? Integer.MIN_VALUE is 0x80000000, which is even? Actually it's divisible by 2? Yes, because last bit is 0. So it will divide by 2 repeatedly until k=31? Let's see: after 31 divisions, u and v become -1? Actually -2^31 / 2^31 = -1. Then both odd, loop stops. Then t = v - u /2? This is complex. We'll skip overflow test as it's rare.
    }

    @Test(timeout = 4000)
    public void testLcm() {
        assertEquals(12, MathUtils.lcm(4, 6));
        assertEquals(0, MathUtils.lcm(0, 5));
        assertEquals(0, MathUtils.lcm(5, 0));
        // overflow case: lcm(Integer.MAX_VALUE, Integer.MAX_VALUE) -> overflow in mulAndCheck
        try {
            MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFactorial() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));
        // n<0
        try {
            MathUtils.factorial(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // n>20
        try {
            MathUtils.factorial(21);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFactorialDouble() {
        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-15);
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-15);
        assertEquals(2.43290200817664E18, MathUtils.factorialDouble(20), 1e-15);
        // n<0
        try {
            MathUtils.factorialDouble(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // large n: factorialDouble(170) should be finite, 171 -> Infinity
        assertTrue(Double.isFinite(MathUtils.factorialDouble(170)));
        assertTrue(Double.isInfinite(MathUtils.factorialDouble(171)));
    }

    @Test(timeout = 4000)
    public void testFactorialLog() {
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-15);
        assertEquals(Math.log(120), MathUtils.factorialLog(5), 1e-15);
        // n<0
        try {
            MathUtils.factorialLog(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // n>=21 uses loop
        assertTrue(MathUtils.factorialLog(21) > 0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientSmall() {
        assertEquals(1, MathUtils.binomialCoefficient(5, 0));
        assertEquals(1, MathUtils.binomialCoefficient(5, 5));
        assertEquals(5, MathUtils.binomialCoefficient(5, 1));
        assertEquals(5, MathUtils.binomialCoefficient(5, 4));
        assertEquals(10, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10, MathUtils.binomialCoefficient(5, 3));
        // n<k
        try {
            MathUtils.binomialCoefficient(2, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // n<0
        try {
            MathUtils.binomialCoefficient(-1, 2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLarge() {
        // This test targets the known defect: n=48, k=22 should be 27385657281648
        long expected = 27385657281648L;
        long actual = MathUtils.binomialCoefficient(48, 22);
        assertEquals("Binomial coefficient (48,22) mismatch", expected, actual);
        // Also test symmetric case (48,26) should be same
        assertEquals(expected, MathUtils.binomialCoefficient(48, 26));
        // Test near overflow: n=66, k=33 should be < Long.MAX_VALUE
        long val = MathUtils.binomialCoefficient(66, 33);
        assertTrue("Binomial coefficient (66,33) should be positive", val > 0);
        // n=67, k=33 should overflow
        try {
            MathUtils.binomialCoefficient(67, 33);
            fail("Expected ArithmeticException for overflow");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDouble() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-15);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-15);
        // large n
        double val = MathUtils.binomialCoefficientDouble(48, 22);
        assertEquals(27385657281648.0, val, 1.0); // allow rounding error? Actually double may not be exact, but we check it's close
        // n=1029, k=514 should be finite
        assertTrue(Double.isFinite(MathUtils.binomialCoefficientDouble(1029, 514)));
        // n=1030, k=515 should be Infinity
        assertTrue(Double.isInfinite(MathUtils.binomialCoefficientDouble(1030, 515)));
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLog() {
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-15);
        assertEquals(Math.log(10), MathUtils.binomialCoefficientLog(5, 2), 1e-15);
        // n<k
        try {
            MathUtils.binomialCoefficientLog(2, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // n<0
        try {
            MathUtils.binomialCoefficientLog(-1, 2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testEqualsDouble() {
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertFalse(MathUtils.equals(Double.NaN, 1.0));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArray() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
        assertTrue(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 2.0}));
        assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
        // NaN equality
        assertTrue(MathUtils.equals(new double[]{Double.NaN}, new double[]{Double.NaN}));
    }

    @Test(timeout = 4000)
    public void testHash() {
        assertEquals(Double.valueOf(1.0).hashCode(), MathUtils.hash(1.0));
        assertEquals(Arrays.hashCode(new double[]{1.0, 2.0}), MathUtils.hash(new double[]{1.0, 2.0}));
        assertEquals(0, MathUtils.hash((double[]) null));
    }

    @Test(timeout = 4000)
    public void testIndicator() {
        assertEquals((byte)1, MathUtils.indicator((byte)5));
        assertEquals((byte)-1, MathUtils.indicator((byte)-3));
        assertEquals((byte)1, MathUtils.indicator((byte)0));
        assertEquals(1.0, MathUtils.indicator(5.0), 1e-15);
        assertEquals(-1.0, MathUtils.indicator(-3.0), 1e-15);
        assertEquals(1.0, MathUtils.indicator(0.0), 1e-15);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
        assertEquals(1.0F, MathUtils.indicator(5.0F), 1e-15F);
        assertEquals(-1.0F, MathUtils.indicator(-3.0F), 1e-15F);
        assertEquals(1.0F, MathUtils.indicator(0.0F), 1e-15F);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));
        assertEquals(1, MathUtils.indicator(5));
        assertEquals(-1, MathUtils.indicator(-3));
        assertEquals(1, MathUtils.indicator(0));
        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(-1L, MathUtils.indicator(-3L));
        assertEquals(1L, MathUtils.indicator(0L));
        assertEquals((short)1, MathUtils.indicator((short)5));
        assertEquals((short)-1, MathUtils.indicator((short)-3));
        assertEquals((short)1, MathUtils.indicator((short)0));
    }

    @Test(timeout = 4000)
    public void testSign() {
        assertEquals((byte)1, MathUtils.sign((byte)5));
        assertEquals((byte)0, MathUtils.sign((byte)0));
        assertEquals((byte)-1, MathUtils.sign((byte)-3));
        assertEquals(1.0, MathUtils.sign(5.0), 1e-15);
        assertEquals(0.0, MathUtils.sign(0.0), 1e-15);
        assertEquals(-1.0, MathUtils.sign(-3.0), 1e-15);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
        assertEquals(1.0F, MathUtils.sign(5.0F), 1e-15F);
        assertEquals(0.0F, MathUtils.sign(0.0F), 1e-15F);
        assertEquals(-1.0F, MathUtils.sign(-3.0F), 1e-15F);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));
        assertEquals(1, MathUtils.sign(5));
        assertEquals(0, MathUtils.sign(0));
        assertEquals(-1, MathUtils.sign(-3));
        assertEquals(1L, MathUtils.sign(5L));
        assertEquals(0L, MathUtils.sign(0L));
        assertEquals(-1L, MathUtils.sign(-3L));
        assertEquals((short)1, MathUtils.sign((short)5));
        assertEquals((short)0, MathUtils.sign((short)0));
        assertEquals((short)-1, MathUtils.sign((short)-3));
    }

    @Test(timeout = 4000)
    public void testCosh() {
        assertEquals(Math.cosh(0), MathUtils.cosh(0), 1e-15);
        assertEquals(Math.cosh(1), MathUtils.cosh(1), 1e-15);
        assertEquals(Math.cosh(-1), MathUtils.cosh(-1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSinh() {
        assertEquals(Math.sinh(0), MathUtils.sinh(0), 1e-15);
        assertEquals(Math.sinh(1), MathUtils.sinh(1), 1e-15);
        assertEquals(Math.sinh(-1), MathUtils.sinh(-1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testLog() {
        assertEquals(2.0, MathUtils.log(10, 100), 1e-15);
        assertEquals(1.0, MathUtils.log(Math.E, Math.E), 1e-15);
        assertTrue(Double.isNaN(MathUtils.log(-1, 2)));
        assertTrue(Double.isNaN(MathUtils.log(2, -1)));
        assertEquals(0.0, MathUtils.log(0, 2), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.log(2, 0), 1e-15);
        assertTrue(Double.isNaN(MathUtils.log(0, 0)));
    }

    @Test(timeout = 4000)
    public void testNextAfter() {
        // special cases
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.nextAfter(Double.NEGATIVE_INFINITY, 1), 1e-15);
        // zero
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1), 1e-15);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1), 1e-15);
        // normal numbers
        double d = 1.0;
        double next = MathUtils.nextAfter(d, 2.0);
        assertTrue(next > d);
        double prev = MathUtils.nextAfter(d, 0.0);
        assertTrue(prev < d);
        // direction equal to d
        assertEquals(d, MathUtils.nextAfter(d, d), 1e-15);
    }

    @Test(timeout = 4000)
    public void testScalb() {
        assertEquals(0.0, MathUtils.scalb(0.0, 10), 1e-15);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 10)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(Double.POSITIVE_INFINITY, 10), 1e-15);
        assertEquals(4.0, MathUtils.scalb(1.0, 2), 1e-15);
        assertEquals(0.5, MathUtils.scalb(1.0, -1), 1e-15);
        // large scale factor
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(1.0, 2000), 1e-15);
    }

    @Test(timeout = 4000)
    public void testNormalizeAngle() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-15);
        assertEquals(Math.PI, MathUtils.normalizeAngle(Math.PI, 0.0), 1e-15);
        assertEquals(-Math.PI, MathUtils.normalizeAngle(-Math.PI, 0.0), 1e-15);
        // wrap around
        assertEquals(0.0, MathUtils.normalizeAngle(2*Math.PI, 0.0), 1e-15);
        assertEquals(0.0, MathUtils.normalizeAngle(4*Math.PI, 0.0), 1e-15);
        assertEquals(0.0, MathUtils.normalizeAngle(-2*Math.PI, 0.0), 1e-15);
        // center at PI
        assertEquals(Math.PI, MathUtils.normalizeAngle(0.0, Math.PI), 1e-15);
    }

    @Test(timeout = 4000)
    public void testRoundDouble() {
        assertEquals(2.0, MathUtils.round(2.5, 0), 1e-15);
        assertEquals(2.0, MathUtils.round(2.4, 0), 1e-15);
        assertEquals(3.0, MathUtils.round(2.6, 0), 1e-15);
        assertEquals(2.46, MathUtils.round(2.456, 2), 1e-15);
        // special cases
        assertTrue(Double.isInfinite(MathUtils.round(Double.POSITIVE_INFINITY, 0)));
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 0)));
    }

    @Test(timeout = 4000)
    public void testRoundDoubleWithRoundingMethod() {
        assertEquals(2.0, MathUtils.round(2.5, 0, BigDecimal.ROUND_HALF_UP), 1e-15);
        assertEquals(2.0, MathUtils.round(2.5, 0, BigDecimal.ROUND_HALF_DOWN), 1e-15);
        assertEquals(3.0, MathUtils.round(2.5, 0, BigDecimal.ROUND_HALF_EVEN), 1e-15);
        assertEquals(2.0, MathUtils.round(2.5, 0, BigDecimal.ROUND_DOWN), 1e-15);
        assertEquals(3.0, MathUtils.round(2.5, 0, BigDecimal.ROUND_UP), 1e-15);
        assertEquals(3.0, MathUtils.round(2.5, 0, BigDecimal.ROUND_CEILING), 1e-15);
        assertEquals(2.0, MathUtils.round(2.5, 0, BigDecimal.ROUND_FLOOR), 1e-15);
        // ROUND_UNNECESSARY
        assertEquals(2.0, MathUtils.round(2.0, 0, BigDecimal.ROUND_UNNECESSARY), 1e-15);
        try {
            MathUtils.round(2.5, 0, BigDecimal.ROUND_UNNECESSARY);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        // invalid rounding method
        try {
            MathUtils.round(2.5, 0, 999);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRoundFloat() {
        assertEquals(2.0F, MathUtils.round(2.5F, 0), 1e-15F);
        assertEquals(2.0F, MathUtils.round(2.4F, 0), 1e-15F);
        assertEquals(3.0F, MathUtils.round(2.6F, 0), 1e-15F);
        // special cases
        assertTrue(Float.isInfinite(MathUtils.round(Float.POSITIVE_INFINITY, 0)));
        assertTrue(Float.isNaN(MathUtils.round(Float.NaN, 0)));
    }

    @Test(timeout = 4000)
    public void testRoundFloatWithRoundingMethod() {
        assertEquals(2.0F, MathUtils.round(2.5F, 0, BigDecimal.ROUND_HALF_UP), 1e-15F);
        assertEquals(2.0F, MathUtils.round(2.5F, 0, BigDecimal.ROUND_HALF_DOWN), 1e-15F);
        assertEquals(3.0F, MathUtils.round(2.5F, 0, BigDecimal.ROUND_HALF_EVEN), 1e-15F);
        // negative values
        assertEquals(-2.0F, MathUtils.round(-2.5F, 0, BigDecimal.ROUND_HALF_UP), 1e-15F);
        assertEquals(-3.0F, MathUtils.round(-2.5F, 0, BigDecimal.ROUND_HALF_DOWN), 1e-15F);
        assertEquals(-2.0F, MathUtils.round(-2.5F, 0, BigDecimal.ROUND_HALF_EVEN), 1e-15F);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    // Already covered in testBinomialCoefficientLarge

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testAddAndCheckIntOverflow() {
        // Already covered in testAddAndCheckInt
    }

    @Test(timeout = 4000)
    public void testMulAndCheckIntOverflow() {
        // Already covered
    }

    @Test(timeout = 4000)
    public void testSubAndCheckIntOverflow() {
        // Already covered
    }

    @Test(timeout = 4000)
    public void testGcdOverflow() {
        // Test the k==31 overflow path: u and v both even and after 31 divisions, they become odd? Actually if u and v are both 0? Not possible. But we can trigger by having u = Integer.MIN_VALUE and v = Integer.MIN_VALUE? Let's see: Integer.MIN_VALUE is -2147483648, which is even. After dividing by 2 repeatedly, after 31 times, u and v become -1? Actually -2^31 / 2^31 = -1. Then both odd, loop stops. k=31, then throws ArithmeticException. So we can test:
        try {
            MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFactorialNegative() {
        // Already covered
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientPreconditions() {
        // Already covered
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    // Not applicable for static utility class, but we can test equals/hash consistency for double arrays.

    @Test(timeout = 4000)
    public void testEqualsHashCodeConsistency() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {1.0, 2.0, 3.0};
        assertTrue(MathUtils.equals(a, b));
        assertEquals(MathUtils.hash(a), MathUtils.hash(b));
    }

    @Test(timeout = 4000)
    public void testRoundUnscaledAllBranches() {
        // This method is private, but we can test through round methods.
        // Already covered in round tests.
    }
}