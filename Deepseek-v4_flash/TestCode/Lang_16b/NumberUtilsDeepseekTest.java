package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Comprehensive JUnit 4 test suite for NumberUtils.
 * Targets maximum line/branch coverage and the known defect
 * where createNumber("0Xfade") throws NumberFormatException.
 *
 * [Branch & Defect Analysis Matrix]
 * Partition A: Core conversions (toInt/toLong/toFloat/toDouble/toByte/toShort)
 * Partition B: Array min/max (long/int/short/byte/double/float)
 * Partition C: Three-arg min/max (all numeric types)
 * Partition D: isDigits/isNumber edge cases
 * Partition E: createNumber - hex, octal, type qualifiers, scientific notation,
 *              blank/null, "0X" defect, "--" prefix, decimal points, exponents,
 *              fallback to BigInteger/BigDecimal, Float/Double infinite/zero checks.
 * Partition F: createInteger/createLong/createFloat/createDouble/createBigInteger/createBigDecimal
 * Partition G: Constants (LONG_ZERO, INTEGER_ONE, etc.)
 */
public class NumberUtilsDeepseekTest {

    // --- Partition A: toInt / toLong / toFloat / toDouble / toByte / toShort ---

    @Test(timeout = 4000)
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(1, NumberUtils.toInt("1"));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(5, NumberUtils.toInt("5", 10));
        assertEquals(10, NumberUtils.toInt(null, 10));
        assertEquals(10, NumberUtils.toInt("", 10));
        assertEquals(Integer.MAX_VALUE, NumberUtils.toInt(String.valueOf(Integer.MAX_VALUE)));
        assertEquals(Integer.MIN_VALUE, NumberUtils.toInt(String.valueOf(Integer.MIN_VALUE)));
    }

    @Test(timeout = 4000)
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(1L, NumberUtils.toLong("1"));
        assertEquals(0L, NumberUtils.toLong("notanumber"));
        assertEquals(10L, NumberUtils.toLong(null, 10L));
        assertEquals(10L, NumberUtils.toLong("", 10L));
        assertEquals(Long.MAX_VALUE, NumberUtils.toLong(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, NumberUtils.toLong(String.valueOf(Long.MIN_VALUE)));
    }

    @Test(timeout = 4000)
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat("", 1.1f), 0.0f);
        assertEquals(Float.NaN, NumberUtils.toFloat("NaN"), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, NumberUtils.toFloat("Infinity"), 0.0f);
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0d);
        assertEquals(1.1d, NumberUtils.toDouble(null, 1.1d), 0.0d);
        assertEquals(1.1d, NumberUtils.toDouble("", 1.1d), 0.0d);
        assertEquals(Double.NaN, NumberUtils.toDouble("NaN"), 0.0d);
        assertEquals(Double.POSITIVE_INFINITY, NumberUtils.toDouble("Infinity"), 0.0d);
    }

    @Test(timeout = 4000)
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 1, NumberUtils.toByte("1"));
        assertEquals((byte) 0, NumberUtils.toByte("abc"));
        assertEquals((byte) 10, NumberUtils.toByte(null, (byte) 10));
        assertEquals((byte) 10, NumberUtils.toByte("", (byte) 10));
        assertEquals(Byte.MAX_VALUE, NumberUtils.toByte(String.valueOf(Byte.MAX_VALUE)));
        assertEquals(Byte.MIN_VALUE, NumberUtils.toByte(String.valueOf(Byte.MIN_VALUE)));
    }

    @Test(timeout = 4000)
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 1, NumberUtils.toShort("1"));
        assertEquals((short) 0, NumberUtils.toShort("abc"));
        assertEquals((short) 10, NumberUtils.toShort(null, (short) 10));
        assertEquals((short) 10, NumberUtils.toShort("", (short) 10));
        assertEquals(Short.MAX_VALUE, NumberUtils.toShort(String.valueOf(Short.MAX_VALUE)));
        assertEquals(Short.MIN_VALUE, NumberUtils.toShort(String.valueOf(Short.MIN_VALUE)));
    }

    // --- Partition B: Array min/max ---

    @Test(timeout = 4000)
    public void testMinArrayLong() {
        assertEquals(1L, NumberUtils.min(new long[]{5L, 1L, 3L}));
        assertEquals(Long.MIN_VALUE, NumberUtils.min(new long[]{Long.MIN_VALUE, Long.MAX_VALUE}));
        assertEquals(-1L, NumberUtils.min(new long[]{-1L}));
        try {
            NumberUtils.min((long[]) null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            NumberUtils.min(new long[0]);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMinArrayInt() {
        assertEquals(1, NumberUtils.min(new int[]{5, 1, 3}));
        assertEquals(Integer.MIN_VALUE, NumberUtils.min(new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE}));
        try {
            NumberUtils.min((int[]) null);
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            NumberUtils.min(new int[0]);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testMinArrayShort() {
        assertEquals((short) 1, NumberUtils.min(new short[]{(short) 5, (short) 1, (short) 3}));
        try {
            NumberUtils.min((short[]) null);
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            NumberUtils.min(new short[0]);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testMinArrayByte() {
        assertEquals((byte) 1, NumberUtils.min(new byte[]{(byte) 5, (byte) 1, (byte) 3}));
        try {
            NumberUtils.min((byte[]) null);
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            NumberUtils.min(new byte[0]);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testMinArrayDouble() {
        assertEquals(1.0d, NumberUtils.min(new double[]{5.0d, 1.0d, 3.0d}), 0.0d);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0d, Double.NaN, 3.0d})));
        try {
            NumberUtils.min((double[]) null);
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            NumberUtils.min(new double[0]);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testMinArrayFloat() {
        assertEquals(1.0f, NumberUtils.min(new float[]{5.0f, 1.0f, 3.0f}), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 3.0f})));
        try {
            NumberUtils.min((float[]) null);
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            NumberUtils.min(new float[0]);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testMaxArrayLong() {
        assertEquals(5L, NumberUtils.max(new long[]{5L, 1L, 3L}));
        assertEquals(Long.MAX_VALUE, NumberUtils.max(new long[]{Long.MIN_VALUE, Long.MAX_VALUE}));
        try {
            NumberUtils.max((long[]) null);
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            NumberUtils.max(new long[0]);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testMaxArrayInt() {
        assertEquals(5, NumberUtils.max(new int[]{5, 1, 3}));
        assertEquals(Integer.MAX_VALUE, NumberUtils.max(new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE}));
        try {
            NumberUtils.max((int[]) null);
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            NumberUtils.max(new int[0]);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testMaxArrayShort() {
        assertEquals((short) 5, NumberUtils.max(new short[]{(short) 5, (short) 1, (short) 3}));
        try {
            NumberUtils.max((short[]) null);
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            NumberUtils.max(new short[0]);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testMaxArrayByte() {
        assertEquals((byte) 5, NumberUtils.max(new byte[]{(byte) 5, (byte) 1, (byte) 3}));
        try {
            NumberUtils.max((byte[]) null);
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            NumberUtils.max(new byte[0]);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testMaxArrayDouble() {
        assertEquals(5.0d, NumberUtils.max(new double[]{5.0d, 1.0d, 3.0d}), 0.0d);
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN, 3.0d})));
        try {
            NumberUtils.max((double[]) null);
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            NumberUtils.max(new double[0]);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testMaxArrayFloat() {
        assertEquals(5.0f, NumberUtils.max(new float[]{5.0f, 1.0f, 3.0f}), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 3.0f})));
        try {
            NumberUtils.max((float[]) null);
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            NumberUtils.max(new float[0]);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    // --- Partition C: three-arg min/max ---

    @Test(timeout = 4000)
    public void testMinThreeLong() {
        assertEquals(1L, NumberUtils.min(5L, 1L, 3L));
        assertEquals(Long.MIN_VALUE, NumberUtils.min(Long.MIN_VALUE, Long.MAX_VALUE, 0L));
    }

    @Test(timeout = 4000)
    public void testMinThreeInt() {
        assertEquals(1, NumberUtils.min(5, 1, 3));
        assertEquals(Integer.MIN_VALUE, NumberUtils.min(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
    }

    @Test(timeout = 4000)
    public void testMinThreeShort() {
        assertEquals((short) 1, NumberUtils.min((short) 5, (short) 1, (short) 3));
    }

    @Test(timeout = 4000)
    public void testMinThreeByte() {
        assertEquals((byte) 1, NumberUtils.min((byte) 5, (byte) 1, (byte) 3));
    }

    @Test(timeout = 4000)
    public void testMinThreeDouble() {
        assertEquals(1.0d, NumberUtils.min(5.0d, 1.0d, 3.0d), 0.0d);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 1.0d, 3.0d)));
    }

    @Test(timeout = 4000)
    public void testMinThreeFloat() {
        assertEquals(1.0f, NumberUtils.min(5.0f, 1.0f, 3.0f), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 1.0f, 3.0f)));
    }

    @Test(timeout = 4000)
    public void testMaxThreeLong() {
        assertEquals(5L, NumberUtils.max(5L, 1L, 3L));
        assertEquals(Long.MAX_VALUE, NumberUtils.max(Long.MIN_VALUE, Long.MAX_VALUE, 0L));
    }

    @Test(timeout = 4000)
    public void testMaxThreeInt() {
        assertEquals(5, NumberUtils.max(5, 1, 3));
        assertEquals(Integer.MAX_VALUE, NumberUtils.max(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
    }

    @Test(timeout = 4000)
    public void testMaxThreeShort() {
        assertEquals((short) 5, NumberUtils.max((short) 5, (short) 1, (short) 3));
    }

    @Test(timeout = 4000)
    public void testMaxThreeByte() {
        assertEquals((byte) 5, NumberUtils.max((byte) 5, (byte) 1, (byte) 3));
    }

    @Test(timeout = 4000)
    public void testMaxThreeDouble() {
        assertEquals(5.0d, NumberUtils.max(5.0d, 1.0d, 3.0d), 0.0d);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, 1.0d, 3.0d)));
    }

    @Test(timeout = 4000)
    public void testMaxThreeFloat() {
        assertEquals(5.0f, NumberUtils.max(5.0f, 1.0f, 3.0f), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, 1.0f, 3.0f)));
    }

    // --- Partition D: isDigits / isNumber ---

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("123"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("123a"));
        assertFalse(NumberUtils.isDigits(" "));
    }

    @Test(timeout = 4000)
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber(" "));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123D"));
        assertTrue(NumberUtils.isNumber("1.23"));
        assertTrue(NumberUtils.isNumber("1.23e2"));
        assertTrue(NumberUtils.isNumber("1.23E2"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertTrue(NumberUtils.isNumber("0X1A"));
        assertTrue(NumberUtils.isNumber("-0x1A"));
        assertTrue(NumberUtils.isNumber("0.1"));
        assertTrue(NumberUtils.isNumber(".1"));
        assertTrue(NumberUtils.isNumber("1."));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("123Lf"));
        assertFalse(NumberUtils.isNumber("123e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("--123"));
        assertFalse(NumberUtils.isNumber("+"));
        assertFalse(NumberUtils.isNumber("-"));
        assertTrue(NumberUtils.isNumber("1e-2"));
        assertTrue(NumberUtils.isNumber("1E+2"));
        assertFalse(NumberUtils.isNumber("0xGG"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("123L."));
        assertFalse(NumberUtils.isNumber("123.L"));
        assertTrue(NumberUtils.isNumber("9876543210"));
        assertTrue(NumberUtils.isNumber("123.")); // trailing dot allowed
        assertTrue(NumberUtils.isNumber(".123"));
        assertFalse(NumberUtils.isNumber(".e1"));
        assertFalse(NumberUtils.isNumber("1.e1"));
        assertTrue(NumberUtils.isNumber("1.e1".substring(0,3))); // "1.e" not number
        assertFalse(NumberUtils.isNumber("1e."));
        assertFalse(NumberUtils.isNumber("1e1."));
        assertTrue(NumberUtils.isNumber("1e1")); // valid exponent
        // Additional edge cases
        assertFalse(NumberUtils.isNumber("1E")); // trailing E
        assertFalse(NumberUtils.isNumber("1e+1")); // OK but "+" after E is allowed, need digit later
        // Let's add a few more
        assertTrue(NumberUtils.isNumber("1e+1")); // valid
        assertTrue(NumberUtils.isNumber("1E-1"));
        assertFalse(NumberUtils.isNumber("1e1.2"));
        assertTrue(NumberUtils.isNumber("0.0"));
        assertTrue(NumberUtils.isNumber("0."));
        assertTrue(NumberUtils.isNumber(".0"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("+."));
        assertFalse(NumberUtils.isNumber("-."));
    }

    // --- Partition E: createNumber (including defect targeting) ---

    @Test(timeout = 4000)
    public void testCreateNumberNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("  ");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumberEmpty() {
        NumberUtils.createNumber("");
    }

    @Test(timeout = 4000)
    public void testCreateNumberDoubleDash() {
        assertNull(NumberUtils.createNumber("--123"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexLowerX() {
        assertEquals(Integer.valueOf(0xabcdef), NumberUtils.createNumber("0xabcdef"));
        assertEquals(Integer.valueOf(0x10), NumberUtils.createNumber("0x10"));
        assertEquals(Integer.valueOf(-0x1A), NumberUtils.createNumber("-0x1A"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexUpperX() {
        // Defect: "0Xfade" should return 0xfade = 64222
        // The bug: NumberUtils.createNumber("0Xfade") throws NumberFormatException because
        // the prefix check is case-sensitive: str.startsWith("0x") or "-0x", but not "0X".
        // Expected: createNumber("0Xfade") -> Integer.valueOf(0xfade) = 64222
        Object result = NumberUtils.createNumber("0Xfade");
        assertTrue("Result should be an Integer", result instanceof Integer);
        assertEquals(Integer.valueOf(0xfade), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexUpperXNegative() {
        assertEquals(Integer.valueOf(-0x1A), NumberUtils.createNumber("-0X1A"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberIntegerNoQualifier() {
        assertEquals(Integer.valueOf("123"), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf("-123"), NumberUtils.createNumber("-123"));
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), NumberUtils.createNumber(String.valueOf(Integer.MAX_VALUE)));
        // Number larger than int but fits in long
        assertEquals(Long.valueOf(3000000000L), NumberUtils.createNumber("3000000000"));
        // Very large to BigInteger
        assertEquals(new BigInteger("99999999999999999999999999999"), NumberUtils.createNumber("99999999999999999999999999999"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberLongQualifier() {
        assertEquals(Long.valueOf("123"), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf("1"), NumberUtils.createNumber("1L"));
        assertEquals(Long.valueOf(Long.MAX_VALUE), NumberUtils.createNumber(Long.MAX_VALUE + "L"));
        try {
            NumberUtils.createNumber("123.0L");
            fail();
        } catch (NumberFormatException e) {
            // expected
        }
        try {
            NumberUtils.createNumber("123e1L");
            fail();
        } catch (NumberFormatException e) {
            // expected
        }
        // Too big for long -> BigInteger
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890L"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloatQualifier() {
        Float f = (Float) NumberUtils.createNumber("1.5f");
        assertEquals(1.5f, f.floatValue(), 0.0f);
        f = (Float) NumberUtils.createNumber("1.5F");
        assertEquals(1.5f, f.floatValue(), 0.0f);
        // Infinity -> fallback to Double then BigDecimal? Actually Float parse of "1e50" returns Infinity => fallback to Double, then BigDecimal.
        Number n = NumberUtils.createNumber("1e50f");
        assertTrue(n instanceof Double || n instanceof BigDecimal);
        // Zero with non-zero mantissa -> fallback
        n = NumberUtils.createNumber("0.00001f");
        assertTrue(n instanceof Float);
        // "0.0f" -> zero, all zeros => Float should be returned
        n = NumberUtils.createNumber("0.0f");
        assertTrue(n instanceof Float);
        assertEquals(0.0f, ((Float) n).floatValue(), 0.0f);
        // non-zero mantissa with zero exp? Not relevant.
    }

    @Test(timeout = 4000)
    public void testCreateNumberDoubleQualifier() {
        Double d = (Double) NumberUtils.createNumber("1.5d");
        assertEquals(1.5d, d.doubleValue(), 0.0d);
        d = (Double) NumberUtils.createNumber("1.5D");
        assertEquals(1.5d, d.doubleValue(), 0.0d);
        // Infinity -> fallback to BigDecimal
        Number n = NumberUtils.createNumber("1e500d");
        assertTrue(n instanceof BigDecimal);
        // Zero all zeros -> Double
        n = NumberUtils.createNumber("0.0d");
        assertTrue(n instanceof Double);
        assertEquals(0.0d, ((Double) n).doubleValue(), 0.0d);
    }

    @Test(timeout = 4000)
    public void testCreateNumberDecimalNoQualifier() {
        Number n = NumberUtils.createNumber("1.5");
        assertTrue(n instanceof Float || n instanceof Double || n instanceof BigDecimal);
        // Actually should return Float if not too big
        assertTrue(n instanceof Float);
        n = NumberUtils.createNumber("1.5e10");
        assertTrue(n instanceof Double);
        n = NumberUtils.createNumber("1.5e100");
        assertTrue(n instanceof BigDecimal);
        // All zeros mant/exp -> Float
        n = NumberUtils.createNumber("0.0");
        assertTrue(n instanceof Float);
        assertEquals(0.0f, ((Float) n).floatValue(), 0.0f);
        // Non-zero mantissa, zero exp -> Float
        n = NumberUtils.createNumber("0.1");
        assertTrue(n instanceof Float);
    }

    @Test(timeout = 4000)
    public void testCreateNumberExpOnly() {
        assertEquals(Integer.valueOf(100), NumberUtils.createNumber("1e2"));
        assertEquals(Long.valueOf(100000000000L), NumberUtils.createNumber("1e11"));
        assertEquals(new BigInteger("1".repeat(20)), NumberUtils.createNumber("1" + "0".repeat(19)));
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumberInvalidExpPos() {
        // The code has a check: if (expPos > -1 && (expPos < decPos || expPos > str.length()))
        // but the expPos calculation is buggy: expPos = str.indexOf('e') + str.indexOf('E') + 1;
        // This can produce incorrect expPos. Let's test a case where it may fail.
        // For instance, "1e2E3" -> expPos = indexOf('e')=1 + indexOf('E')=3 +1=5, but length=5, then it's inside.
        // Actually that string has two 'e's, but the code may not handle it correctly? We'll just test a valid number.
        // To trigger NumberFormatException from that path, we need expPos > str.length()? but the code sets expPos = indexOf('e')+indexOf('E')+1 which is impossible > length if both found? Actually if 'e' and 'E' both present, indexOf returns indices, sum+1 could be > length if they are at adjacent positions? e.g., "1eE" has indices 1 and 2, sum=3, string length=3, not >. But if the string is "1eE2", indices 1 and 2, sum=3, length=4, not >. The condition expPos > str.length() is almost never true. We'll skip this branch.
        // Instead, test invalid number: "1e2.3" -> decimal inside exponent, should throw?
        NumberUtils.createNumber("1e2.3");
    }

    @Test(timeout = 4000)
    public void testCreateNumberLeadingZeros() {
        // Leading zeros are not interpreted as octal in createNumber
        assertEquals(Integer.valueOf(10), NumberUtils.createNumber("010"));
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumberInvalidChar() {
        NumberUtils.createNumber("12a34");
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeFloat() {
        Number n = NumberUtils.createNumber("-1.5f");
        assertTrue(n instanceof Float);
        assertEquals(-1.5f, ((Float) n).floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeHex() {
        assertEquals(Integer.valueOf(-0x1A), NumberUtils.createNumber("-0x1A"));
        assertEquals(Integer.valueOf(-0x1A), NumberUtils.createNumber("-0X1A"));
    }

    // --- Partition F: createInteger / createLong / createFloat / createDouble / createBigInteger / createBigDecimal ---

    @Test(timeout = 4000)
    public void testCreateIntegerNull() {
        assertNull(NumberUtils.createInteger(null));
    }

    @Test(timeout = 4000)
    public void testCreateInteger() {
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.decode("0x1A"), NumberUtils.createInteger("0x1A"));
        assertEquals(Integer.decode("-077"), NumberUtils.createInteger("-077"));
    }

    @Test(timeout = 4000)
    public void testCreateLongNull() {
        assertNull(NumberUtils.createLong(null));
    }

    @Test(timeout = 4000)
    public void testCreateLong() {
        assertEquals(Long.valueOf(123), NumberUtils.createLong("123"));
        assertEquals(Long.valueOf(-123), NumberUtils.createLong("-123"));
    }

    @Test(timeout = 4000)
    public void testCreateFloatNull() {
        assertNull(NumberUtils.createFloat(null));
    }

    @Test(timeout = 4000)
    public void testCreateFloat() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));
    }

    @Test(timeout = 4000)
    public void testCreateDoubleNull() {
        assertNull(NumberUtils.createDouble(null));
    }

    @Test(timeout = 4000)
    public void testCreateDouble() {
        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));
    }

    @Test(timeout = 4000)
    public void testCreateBigIntegerNull() {
        assertNull(NumberUtils.createBigInteger(null));
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger() {
        assertEquals(new BigInteger("123"), NumberUtils.createBigInteger("123"));
        assertEquals(new BigInteger("-123"), NumberUtils.createBigInteger("-123"));
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimalNull() {
        assertNull(NumberUtils.createBigDecimal(null));
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("  ");
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal() {
        assertEquals(new BigDecimal("123.456"), NumberUtils.createBigDecimal("123.456"));
    }

    // --- Partition G: Constants ---

    @Test(timeout = 4000)
    public void testConstants() {
        assertEquals(Long.valueOf(0L), NumberUtils.LONG_ZERO);
        assertEquals(Long.valueOf(1L), NumberUtils.LONG_ONE);
        assertEquals(Long.valueOf(-1L), NumberUtils.LONG_MINUS_ONE);
        assertEquals(Integer.valueOf(0), NumberUtils.INTEGER_ZERO);
        assertEquals(Integer.valueOf(1), NumberUtils.INTEGER_ONE);
        assertEquals(Integer.valueOf(-1), NumberUtils.INTEGER_MINUS_ONE);
        assertEquals(Short.valueOf((short) 0), NumberUtils.SHORT_ZERO);
        assertEquals(Short.valueOf((short) 1), NumberUtils.SHORT_ONE);
        assertEquals(Short.valueOf((short) -1), NumberUtils.SHORT_MINUS_ONE);
        assertEquals(Byte.valueOf((byte) 0), NumberUtils.BYTE_ZERO);
        assertEquals(Byte.valueOf((byte) 1), NumberUtils.BYTE_ONE);
        assertEquals(Byte.valueOf((byte) -1), NumberUtils.BYTE_MINUS_ONE);
        assertEquals(Double.valueOf(0.0d), NumberUtils.DOUBLE_ZERO);
        assertEquals(Double.valueOf(1.0d), NumberUtils.DOUBLE_ONE);
        assertEquals(Double.valueOf(-1.0d), NumberUtils.DOUBLE_MINUS_ONE);
        assertEquals(Float.valueOf(0.0f), NumberUtils.FLOAT_ZERO);
        assertEquals(Float.valueOf(1.0f), NumberUtils.FLOAT_ONE);
        assertEquals(Float.valueOf(-1.0f), NumberUtils.FLOAT_MINUS_ONE);
    }
}