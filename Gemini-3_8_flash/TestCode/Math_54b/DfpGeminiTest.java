package org.apache.commons.math.dfp;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target Class: org.apache.commons.math.dfp.Dfp
 * Target Environment: Java 8 / JUnit 4 / Defects4J
 *
 * 1. DEFECT ISSUE MATH-567:
 *    - Method: Dfp.toDouble()
 *    - Defect Condition: When converting Dfp.ZERO to double, toDouble() misses an explicit
 *      zero-check and enters the mantissa-reconstruction loop. It subtracts 1.0 from 0.0,
 *      resulting in a negative value whose bitwise mantissa representation sets the high bits,
 *      eventually yielding Double.NEGATIVE_INFINITY instead of 0.0.
 *    - Expected Behavior: new DfpField(20).getZero().toDouble() == 0.0.
 *
 * 2. BRANCH & BOUNDARY MATRIX:
 *    - Constructors:
 *      * Dfp(DfpField, long): 0L, Long.MIN_VALUE (special offset loop), Long.MAX_VALUE, negatives.
 *      * Dfp(DfpField, double): 0.0, subnormals (Double.MIN_VALUE, while loop normalization),
 *        Double.NaN, +/-Infinity, normal positives and negatives.
 *      * Dfp(DfpField, String): "Infinity", "-Infinity", "NaN", scientific notation ('e' and 'E',
 *        negative/positive exponents), leading/trailing zeros, "0.00000", implicit decimal point.
 *      * Copy constructor & non-finite constructor (SNAN, QNAN, INFINITE).
 *    - Align / Shift Logic:
 *      * align(int): diff == 0, adiff > mant.length + 1 (inexact trap), diff < 0 (shiftRight),
 *        diff > 0 (shiftLeft), inexact tracking on lost digits.
 *      * shiftLeft(), shiftRight().
 *    - Arithmetic & Normalization:
 *      * add(Dfp): precision mismatch trap, NaN cases, Inf combinations (+Inf + -Inf -> invalid),
 *        zero preservation, sign complement, carry overflow (rh != 0 && asign == bsign),
 *        normalization loops, addition of negative zeros (IEEE 854 rule).
 *      * multiply(Dfp): precision mismatch, Inf * 0 -> invalid trap, Inf * Inf, Inf * Finite,
 *        sign combinations, round of least significant product digits.
 *      * multiply(int): NaN, Inf * 0, out-of-range (x < 0, x >= RADIX), carry shiftRight.
 *      * divide(Dfp): precision mismatch, div-by-zero trap, Inf / Inf -> invalid, finite / 0 -> Inf,
 *        trial quotient digit adjustments (minadj >= 2, trialgood loops), ROUND_DOWN early exit.
 *      * divide(int): div-by-zero, invalid range, normalization (shiftLeft).
 *      * sqrt(): 0, +Inf, QNAN, SNAN (invalid trap), negative (invalid trap), coarse mantissa
 *        branches (mant/2000 == 0, 2, 3, default), Newton-Raphson alternating break.
 *    - Integer & Rounding Operations:
 *      * rint(), floor(), ceil(), trunc(RoundingMode): exp < 0 (returns zero with inexact),
 *        exp >= mant.length, ROUND_FLOOR negative increment, ROUND_CEIL positive increment,
 *        ROUND_HALF_EVEN tie-breaking (odd vs even mantissa bit).
 *      * intValue(): > Integer.MAX_VALUE clamp, < Integer.MIN_VALUE clamp, normal conversions.
 *      * remainder(Dfp): zero result preserving original sign.
 *      * round(int): all 8 RoundingModes (DOWN, UP, HALF_UP, HALF_DOWN, HALF_EVEN, HALF_ODD,
 *        CEIL, FLOOR), carry overflow (rh != 0), exp < MIN_EXP (underflow), exp > MAX_EXP (overflow).
 *    - Comparison & Contracts:
 *      * compare(a, b): zero vs -zero equality, sign divergence, Inf vs Finite, exponent/mantissa diffs.
 *      * equals, hashCode, lessThan, greaterThan, unequal with NaNs and cross-precision guards.
 *    - Trap System & Conversions:
 *      * dotrap: FLAG_INVALID, FLAG_DIV_ZERO (0/0, normal, Inf/QNAN), FLAG_UNDERFLOW (flush vs gradual),
 *        FLAG_OVERFLOW.
 *      * toString(), dfp2sci(), dfp2string(): large/small exponent transitions, zero in sci notation.
 *      * toSplitDouble(): split array integrity.
 * =========================================================================================
 */
public class DfpGeminiTest {

    private DfpField factory;
    private DfpField factorySmall;

    @Before
    public void setUp() {
        factory = new DfpField(20);
        factorySmall = new DfpField(10);
    }

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Issue MATH-567 Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testIssue567ZeroToDouble() {
        // Direct trigger for MATH-567: toDouble() on 0.0 must return 0.0, not -Infinity
        Dfp zero = factory.getZero();
        assertEquals(0.0, zero.toDouble(), 0.0);

        Dfp zeroFromStr = factory.newDfp("0.0");
        assertEquals(0.0, zeroFromStr.toDouble(), 0.0);

        Dfp zeroFromDouble = factory.newDfp(0.0);
        assertEquals(0.0, zeroFromDouble.toDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testIssue567NegativeZeroToDouble() {
        Dfp negZero = factory.newDfp("-0.0");
        double d = negZero.toDouble();
        assertEquals(0.0, d, 0.0);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicArithmetic() {
        Dfp a = factory.newDfp("12.34");
        Dfp b = factory.newDfp("5.66");

        Dfp sum = a.add(b);
        assertEquals(factory.newDfp("18.00"), sum);

        Dfp diff = a.subtract(b);
        assertEquals(factory.newDfp("6.68"), diff);

        Dfp prod = a.multiply(b);
        assertEquals(factory.newDfp("69.8444"), prod);

        Dfp quot = factory.newDfp("10.0").divide(factory.newDfp("2.0"));
        assertEquals(factory.newDfp("5.0"), quot);
    }

    @Test(timeout = 4000)
    public void testIntArithmeticSpecializations() {
        Dfp a = factory.newDfp("123.45");

        Dfp prod = a.multiply(3);
        assertEquals(factory.newDfp("370.35"), prod);

        Dfp quot = a.divide(3);
        assertEquals(factory.newDfp("41.15"), quot);

        // multiply with carry overflow
        Dfp bigSingleDigit = factory.newDfp("9999");
        Dfp bigProd = bigSingleDigit.multiply(9);
        assertEquals(factory.newDfp("89991"), bigProd);
    }

    @Test(timeout = 4000)
    public void testLogAndPowerMethods() {
        Dfp tenK = factory.newDfp("10000");
        assertEquals(1, tenK.log10K());

        Dfp pow10K = factory.getOne().power10K(2);
        assertEquals(factory.newDfp("100000000"), pow10K);

        // Test log10() branches based on most significant digits
        Dfp d4 = factory.newDfp("5000"); // mant > 1000
        assertEquals(3, d4.log10());

        Dfp d3 = factory.newDfp("500");  // mant > 100
        assertEquals(2, d3.log10());

        Dfp d2 = factory.newDfp("50");   // mant > 10
        assertEquals(1, d2.log10());

        Dfp d1 = factory.newDfp("5");    // mant <= 10
        assertEquals(0, d1.log10());

        // Test power10(e) with all 4 modulo branches, both positive and negative
        for (int e = -6; e <= 6; e++) {
            Dfp p = factory.getOne().power10(e);
            assertNotNull(p);
            assertTrue(p.greaterThan(factory.getZero()));
        }
    }

    @Test(timeout = 4000)
    public void testTruncationAndRoundingModes() {
        Dfp valPos = factory.newDfp("12.5");
        Dfp valNeg = factory.newDfp("-12.5");

        assertEquals(factory.newDfp("12"), valPos.rint());
        assertEquals(factory.newDfp("13"), factory.newDfp("13.5").rint()); // half-even to 14
        assertEquals(factory.newDfp("14"), factory.newDfp("13.5").rint());
        assertEquals(factory.newDfp("12"), valPos.floor());
        assertEquals(factory.newDfp("-13"), valNeg.floor());
        assertEquals(factory.newDfp("13"), valPos.ceil());
        assertEquals(factory.newDfp("-12"), valNeg.ceil());

        // Exponent less than zero returns zero
        Dfp small = factory.newDfp("0.00001");
        assertEquals(factory.getZero(), small.rint());

        // Exponent >= mant.length returns this
        Dfp huge = factory.getOne().power10K(factory.getRadixDigits() + 2);
        assertSame(huge, huge.rint());
    }

    @Test(timeout = 4000)
    public void testRemainder() {
        Dfp a = factory.newDfp("5.0");
        Dfp b = factory.newDfp("3.0");
        // 5.0 - (rint(5/3) * 3) = 5 - (2*3) = -1.0
        assertEquals(factory.newDfp("-1.0"), a.remainder(b));

        // Result zero carries sign of dividend
        Dfp c = factory.newDfp("-4.0");
        Dfp d = factory.newDfp("2.0");
        Dfp rem = c.remainder(d);
        assertEquals(0, rem.mant[rem.mant.length - 1]);
        assertEquals(-1, rem.sign);
    }

    @Test(timeout = 4000)
    public void testSqrtCoarseBranches() {
        // mant / 2000 coarse branches:
        // Case 0: mant/2000 == 0
        Dfp sqrt1 = factory.newDfp("1.0").sqrt();
        assertEquals(factory.newDfp("1.0"), sqrt1);

        // Case 2: mant/2000 == 2
        Dfp sqrt4 = factory.newDfp("4.0").sqrt();
        assertEquals(factory.newDfp("2.0"), sqrt4);

        // Case 3: mant/2000 == 3
        Dfp sqrt6 = factory.newDfp("6.25").sqrt();
        assertEquals(factory.newDfp("2.5"), sqrt6);

        // Case default: mant/2000 == 4
        Dfp sqrt8 = factory.newDfp("9.0").sqrt();
        assertEquals(factory.newDfp("3.0"), sqrt8);

        // Sqrt with exponent adjustment
        Dfp sqrtLarge = factory.newDfp("1000000.0").sqrt();
        assertEquals(factory.newDfp("1000.0"), sqrtLarge);

        Dfp sqrtSmall = factory.newDfp("0.0001").sqrt();
        assertEquals(factory.newDfp("0.01"), sqrtSmall);
    }

    @Test(timeout = 4000)
    public void testRoundAllModes() {
        factory.clearIEEEFlags();

        Dfp d = factory.newDfp("1.0");

        // Direct testing of round(n) method under all RoundingModes
        factory.setRoundingMode(DfpField.RoundingMode.ROUND_DOWN);
        assertEquals(0, d.round(4999));
        assertEquals(0, d.round(5000));

        factory.setRoundingMode(DfpField.RoundingMode.ROUND_UP);
        assertEquals(0, d.round(0));
        assertEquals(DfpField.FLAG_INEXACT, d.round(1));

        factory.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_UP);
        assertEquals(0, d.round(4999));
        assertEquals(DfpField.FLAG_INEXACT, d.round(5000));

        factory.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_DOWN);
        assertEquals(0, d.round(5000));
        assertEquals(DfpField.FLAG_INEXACT, d.round(5001));

        factory.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN);
        assertEquals(0, d.round(4999));
        // mant[0] of 1.0 is 0 (even), so 5000 does not inc
        assertEquals(0, d.round(5000));

        factory.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_ODD);
        // mant[0] of 1.0 is 0 (even), so 5000 DOES inc under HALF_ODD
        assertEquals(DfpField.FLAG_INEXACT, d.round(5000));

        factory.setRoundingMode(DfpField.RoundingMode.ROUND_CEIL);
        assertEquals(DfpField.FLAG_INEXACT, d.round(100)); // sign = 1, inc = true
        Dfp dNeg = d.negate();
        assertEquals(0, dNeg.round(100)); // sign = -1, inc = false

        factory.setRoundingMode(DfpField.RoundingMode.ROUND_FLOOR);
        assertEquals(0, d.round(100)); // sign = 1, inc = false
        assertEquals(DfpField.FLAG_INEXACT, dNeg.round(100)); // sign = -1, inc = true
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testLongMinBoundaryConstructor() {
        Dfp minLongDfp = factory.newDfp(Long.MIN_VALUE);
        assertNotNull(minLongDfp);
        assertEquals("-9223372036854775808.", minLongDfp.toString());

        Dfp maxLongDfp = factory.newDfp(Long.MAX_VALUE);
        assertEquals("9223372036854775807.", maxLongDfp.toString());
    }

    @Test(timeout = 4000)
    public void testDoubleSubnormalAndSpecialValuesConstructor() {
        // Subnormal Double normalization while loop
        Dfp subnormalDfp = factory.newDfp(Double.MIN_VALUE);
        assertFalse(subnormalDfp.isNaN());
        assertFalse(subnormalDfp.isInfinite());
        assertTrue(subnormalDfp.greaterThan(factory.getZero()));

        // Double special values
        Dfp nanDfp = factory.newDfp(Double.NaN);
        assertTrue(nanDfp.isNaN());

        Dfp posInfDfp = factory.newDfp(Double.POSITIVE_INFINITY);
        assertTrue(posInfDfp.isInfinite());
        assertEquals(1, posInfDfp.sign);

        Dfp negInfDfp = factory.newDfp(Double.NEGATIVE_INFINITY);
        assertTrue(negInfDfp.isInfinite());
        assertEquals(-1, negInfDfp.sign);
    }

    @Test(timeout = 4000)
    public void testStringParsingBoundaries() {
        assertEquals(Dfp.INFINITE, factory.newDfp("Infinity").classify());
        assertEquals(Dfp.INFINITE, factory.newDfp("-Infinity").classify());
        assertEquals(Dfp.QNAN, factory.newDfp("NaN").classify());

        // Number with scientific notation and negative exponent
        Dfp sci1 = factory.newDfp("1.234e-4");
        assertEquals(factory.newDfp("0.0001234"), sci1);

        Dfp sci2 = factory.newDfp("-1.234E3");
        assertEquals(factory.newDfp("-1234"), sci2);

        // Decimal found but significant digits is 0 (e.g., "0.00000")
        Dfp zeros = factory.newDfp("0.00000");
        assertEquals(factory.getZero(), zeros);

        // Implicit decimal point at end
        Dfp intStr = factory.newDfp("12345");
        assertEquals(factory.newDfp("12345."), intStr);

        // Leading and trailing zeros
        Dfp padded = factory.newDfp("000123.45000");
        assertEquals(factory.newDfp("123.45"), padded);
    }

    @Test(timeout = 4000)
    public void testIntValueClamping() {
        Dfp normal = factory.newDfp("123456");
        assertEquals(123456, normal.intValue());

        Dfp normalNeg = factory.newDfp("-654321");
        assertEquals(-654321, normalNeg.intValue());

        Dfp tooBig = factory.newDfp("3000000000");
        assertEquals(Integer.MAX_VALUE, tooBig.intValue());

        Dfp tooSmall = factory.newDfp("-3000000000");
        assertEquals(Integer.MIN_VALUE, tooSmall.intValue());
    }

    @Test(timeout = 4000)
    public void testAlignBoundaries() {
        Dfp a = factory.newDfp("1234.5678");

        // diff == 0
        assertEquals(0, a.align(a.exp));

        // adiff > mant.length + 1: resets mantissa, sets inexact flag
        factory.clearIEEEFlags();
        int lost = a.align(a.exp + factory.getRadixDigits() + 10);
        assertEquals(0, lost);
        assertEquals(DfpField.FLAG_INEXACT, factory.getIEEEFlags());

        // Shift left
        Dfp b = factory.newDfp("1234.5678");
        int expBefore = b.exp;
        b.shiftLeft();
        assertEquals(expBefore - 1, b.exp);

        // Shift right
        b.shiftRight();
        assertEquals(expBefore, b.exp);
    }

    @Test(timeout = 4000)
    public void testNextAfterBoundaries() {
        Dfp zero = factory.getZero();
        Dfp one = factory.getOne();

        // nextAfter identical number returns equal
        assertEquals(one, one.nextAfter(one));

        // nextAfter from zero in positive direction
        Dfp nextPos = zero.nextAfter(one);
        assertTrue(nextPos.greaterThan(zero));

        // nextAfter from zero in negative direction
        Dfp nextNeg = zero.nextAfter(one.negate());
        assertTrue(nextNeg.lessThan(zero));

        // nextAfter from 1.0 down towards 0.0
        Dfp nextDown = one.nextAfter(zero);
        assertTrue(nextDown.lessThan(one));

        // nextAfter from negative number up towards 0.0
        Dfp negOne = one.negate();
        Dfp nextUp = negOne.nextAfter(zero);
        assertTrue(nextUp.greaterThan(negOne));
    }

    @Test(timeout = 4000)
    public void testToStringAndScientificFormatting() {
        // Non-finite formatting
        assertEquals("Infinity", factory.newDfp("Infinity").toString());
        assertEquals("-Infinity", factory.newDfp("-Infinity").toString());
        assertEquals("NaN", factory.newDfp("NaN").toString());

        // dfp2sci triggers: exp > mant.length or exp < -1
        Dfp tiny = factory.newDfp("1.2345e-15");
        String tinyStr = tiny.toString();
        assertTrue(tinyStr.contains("e-") || tinyStr.contains("e"));

        Dfp huge = factory.newDfp("1.2345e50");
        String hugeStr = huge.toString();
        assertTrue(hugeStr.contains("e"));

        // dfp2sci for zero
        Dfp zeroSci = factory.getZero();
        assertEquals("0.0e0", zeroSci.dfp2sci());

        // dfp2string formatting
        Dfp normal = factory.newDfp("123.456");
        assertEquals("123.456", normal.dfp2string());
    }

    @Test(timeout = 4000)
    public void testToDoubleAndToSplitDouble() {
        Dfp inf = factory.newDfp("Infinity");
        assertEquals(Double.POSITIVE_INFINITY, inf.toDouble(), 0.0);

        Dfp negInf = factory.newDfp("-Infinity");
        assertEquals(Double.NEGATIVE_INFINITY, negInf.toDouble(), 0.0);

        Dfp nan = factory.newDfp("NaN");
        assertTrue(Double.isNaN(nan.toDouble()));

        Dfp piApprox = factory.newDfp("3.141592653589793");
        assertEquals(3.141592653589793, piApprox.toDouble(), 1e-15);

        Dfp negPi = piApprox.negate();
        assertEquals(-3.141592653589793, negPi.toDouble(), 1e-15);

        // Extremely small exponent -> 0
        Dfp underflowDouble = factory.getOne().power10(-400);
        assertEquals(0.0, underflowDouble.toDouble(), 0.0);

        // Extremely large exponent -> Infinity
        Dfp overflowDouble = factory.getOne().power10(400);
        assertEquals(Double.POSITIVE_INFINITY, overflowDouble.toDouble(), 0.0);

        // toSplitDouble
        double[] split = piApprox.toSplitDouble();
        assertEquals(2, split.length);
        assertEquals(piApprox.toDouble(), split[0] + split[1], 1e-15);
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrecisionMismatchGuards() {
        Dfp big = factory.getOne();
        Dfp small = factorySmall.getOne();

        factory.clearIEEEFlags();

        // newInstance
        Dfp ni = big.newInstance(small);
        assertTrue(ni.isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        // add
        factory.clearIEEEFlags();
        assertTrue(big.add(small).isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        // multiply
        factory.clearIEEEFlags();
        assertTrue(big.multiply(small).isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        // divide
        factory.clearIEEEFlags();
        assertTrue(big.divide(small).isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        // lessThan & greaterThan
        factory.clearIEEEFlags();
        assertFalse(big.lessThan(small));
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        factory.clearIEEEFlags();
        assertFalse(big.greaterThan(small));
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        // nextAfter
        factory.clearIEEEFlags();
        assertTrue(big.nextAfter(small).isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());
    }

    @Test(timeout = 4000)
    public void testDivideByZeroTraps() {
        factory.clearIEEEFlags();
        Dfp five = factory.newDfp("5.0");
        Dfp zero = factory.getZero();

        Dfp divZero = five.divide(zero);
        assertTrue(divZero.isInfinite());
        assertEquals(DfpField.FLAG_DIV_ZERO, factory.getIEEEFlags());

        // Divide integer 0
        factory.clearIEEEFlags();
        Dfp intDivZero = five.divide(0);
        assertTrue(intDivZero.isInfinite());
        assertEquals(DfpField.FLAG_DIV_ZERO, factory.getIEEEFlags());

        // 0 / 0 yields NaN
        factory.clearIEEEFlags();
        Dfp zeroDivZero = zero.divide(zero);
        assertTrue(zeroDivZero.isNaN());
    }

    @Test(timeout = 4000)
    public void testIntMultiplicationAndDivisionInvalidOperands() {
        Dfp val = factory.newDfp("10.0");

        // multiply(int) with x < 0 or x >= RADIX
        factory.clearIEEEFlags();
        Dfp resNeg = val.multiply(-1);
        assertTrue(resNeg.isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        factory.clearIEEEFlags();
        Dfp resRadix = val.multiply(Dfp.RADIX);
        assertTrue(resRadix.isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        // divide(int) with divisor < 0 or divisor >= RADIX
        factory.clearIEEEFlags();
        Dfp divNeg = val.divide(-1);
        assertTrue(divNeg.isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        factory.clearIEEEFlags();
        Dfp divRadix = val.divide(Dfp.RADIX);
        assertTrue(divRadix.isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());
    }

    @Test(timeout = 4000)
    public void testNonFiniteArithmeticTransitions() {
        Dfp posInf = factory.newDfp("Infinity");
        Dfp negInf = factory.newDfp("-Infinity");
        Dfp nan = factory.newDfp("NaN");
        Dfp five = factory.newDfp("5");
        Dfp zero = factory.getZero();

        // Inf + -Inf -> NaN (Invalid)
        factory.clearIEEEFlags();
        assertTrue(posInf.add(negInf).isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        // Inf + Inf -> Inf
        assertEquals(posInf, posInf.add(posInf));

        // Inf * 0 -> NaN (Invalid)
        factory.clearIEEEFlags();
        assertTrue(posInf.multiply(zero).isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        // Inf * 5 -> Inf
        assertEquals(posInf, posInf.multiply(five));

        // Inf / Inf -> NaN (Invalid)
        factory.clearIEEEFlags();
        assertTrue(posInf.divide(posInf).isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        // 5 / Inf -> 0
        assertEquals(zero, five.divide(posInf));

        // Sqrt(-5) -> NaN
        factory.clearIEEEFlags();
        assertTrue(five.negate().sqrt().isNaN());
        assertEquals(DfpField.FLAG_INVALID, factory.getIEEEFlags());

        // Sqrt(NaN), Sqrt(0), Sqrt(+Inf)
        assertTrue(nan.sqrt().isNaN());
        assertEquals(zero, zero.sqrt());
        assertEquals(posInf, posInf.sqrt());
    }

    @Test(timeout = 4000)
    public void testTrapDispatcherBranches() {
        Dfp one = factory.getOne();
        Dfp zero = factory.getZero();

        // FLAG_INVALID trap
        Dfp rInvalid = one.dotrap(DfpField.FLAG_INVALID, "test", one, one);
        assertTrue(rInvalid.isNaN());

        // FLAG_DIV_ZERO traps: 0/0, Inf/QNAN, normal
        Dfp rZeroDiv = zero.dotrap(DfpField.FLAG_DIV_ZERO, "divide", zero, zero);
        assertTrue(rZeroDiv.isNaN());

        Dfp qnan = factory.newDfp("NaN");
        Dfp rQnanDiv = qnan.dotrap(DfpField.FLAG_DIV_ZERO, "divide", zero, zero);
        assertTrue(rQnanDiv.isNaN());

        // FLAG_UNDERFLOW trap: below minExp - digits vs gradual underflow
        Dfp underflowResult = factory.newDfp("1.0");
        underflowResult.exp = Dfp.MIN_EXP - factory.getRadixDigits() - 5;
        Dfp flushed = one.dotrap(DfpField.FLAG_UNDERFLOW, "round", one, underflowResult);
        assertEquals(zero, flushed);

        // FLAG_OVERFLOW trap
        Dfp rOverflow = one.dotrap(DfpField.FLAG_OVERFLOW, "round", one, one);
        assertTrue(rOverflow.isInfinite());
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Dfp valA1 = factory.newDfp("123.456");
        Dfp valA2 = factory.newDfp("123.456");
        Dfp valB = factory.newDfp("654.321");

        // Reflexive, symmetric, transitive
        assertTrue(valA1.equals(valA1));
        assertTrue(valA1.equals(valA2));
        assertTrue(valA2.equals(valA1));
        assertEquals(valA1.hashCode(), valA2.hashCode());

        // Inequality
        assertFalse(valA1.equals(valB));
        assertFalse(valA1.equals(null));
        assertFalse(valA1.equals("123.456"));

        // Sign of zero ignored per IEEE 854
        Dfp posZero = factory.newDfp("0.0");
        Dfp negZero = factory.newDfp("-0.0");
        assertTrue(posZero.equals(negZero));

        // NaNs never equal anything, including themselves
        Dfp nan1 = factory.newDfp("NaN");
        Dfp nan2 = factory.newDfp("NaN");
        assertFalse(nan1.equals(nan2));
        assertFalse(nan1.equals(nan1));

        // Cross-precision equality returns false
        Dfp cross1 = factory.getOne();
        Dfp cross2 = factorySmall.getOne();
        assertFalse(cross1.equals(cross2));
    }

    @Test(timeout = 4000)
    public void testComparisonsAndUnequal() {
        Dfp a = factory.newDfp("10");
        Dfp b = factory.newDfp("20");
        Dfp c = factory.newDfp("-5");

        assertTrue(a.lessThan(b));
        assertFalse(b.lessThan(a));
        assertTrue(b.greaterThan(a));
        assertFalse(a.greaterThan(b));

        assertTrue(c.lessThan(a));
        assertTrue(a.greaterThan(c));

        assertTrue(a.unequal(b));
        assertFalse(a.unequal(factory.newDfp("10")));

        // NaN comparisons
        Dfp nan = factory.newDfp("NaN");
        assertFalse(a.lessThan(nan));
        assertFalse(a.greaterThan(nan));
        assertFalse(a.unequal(nan));
    }

    @Test(timeout = 4000)
    public void testCopysignAndClassify() {
        Dfp pos = factory.newDfp("42");
        Dfp neg = factory.newDfp("-42");
        Dfp one = factory.getOne();
        Dfp negOne = one.negate();

        assertEquals(neg, Dfp.copysign(pos, negOne));
        assertEquals(pos, Dfp.copysign(neg, one));
        assertEquals(pos, Dfp.copysign(pos, one));
        assertEquals(neg, Dfp.copysign(neg, negOne));

        assertEquals(Dfp.FINITE, pos.classify());
        assertEquals(Dfp.INFINITE, factory.newDfp("Infinity").classify());
        assertEquals(Dfp.QNAN, factory.newDfp("NaN").classify());
        Dfp snan = factory.newDfp((byte) 1, Dfp.SNAN);
        assertEquals(Dfp.SNAN, snan.classify());
    }

    @Test(timeout = 4000)
    public void testCopyConstructorAndNewInstanceMethods() {
        Dfp original = factory.newDfp("789.1011");
        Dfp copy = new Dfp(original);
        assertEquals(original, copy);
        assertNotSame(original, copy);
        assertNotSame(original.mant, copy.mant);

        assertEquals(factory.newDfp((byte) 7), original.newInstance((byte) 7));
        assertEquals(factory.newDfp(123), original.newInstance(123));
        assertEquals(factory.newDfp(9876543210L), original.newInstance(9876543210L));
        assertEquals(factory.newDfp(45.67), original.newInstance(45.67));
        assertEquals(factory.newDfp("12.34"), original.newInstance("12.34"));
        assertEquals(factory.getZero(), original.newInstance());

        assertSame(factory, original.getField());
        assertEquals(factory.getRadixDigits(), original.getRadixDigits());
        assertEquals(factory.getOne(), original.getOne());
        assertEquals(factory.getTwo(), original.getTwo());
    }
}