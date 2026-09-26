package org.apache.commons.lang.math;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.lang.NumberUtils
 *
 * Decision / Condition Coverage Targets:
 * 1. stringToInt(str, defaultValue):
 *    - Valid numeric string -> returns parsed int
 *    - Null / invalid format -> throws NumberFormatException, returns defaultValue
 *    - 1-arg overload stringToInt(str) -> defaults to 0
 *
 * 2. createNumber(String):
 *    - val == null -> returns null
 *    - val.length() == 0 -> throws NumberFormatException ("" is not a valid number)
 *    - val.startsWith("--") -> returns null
 *    - val.startsWith("0x") || val.startsWith("-0x") -> createInteger(val)
 *    - decPos > -1:
 *        - expPos > -1:
 *            - expPos < decPos -> throws NumberFormatException
 *            - expPos >= decPos -> dec and exp substrings extracted
 *        - expPos == -1 -> dec extracted, no exp
 *    - decPos == -1:
 *        - expPos > -1 -> mant extracted up to expPos
 *        - expPos == -1 -> mant = val, dec = null
 *    - !Character.isDigit(lastChar):
 *        - Type specifiers:
 *            - 'l', 'L':
 *                - Valid long format (dec == null && exp == null && digits) -> createLong / createBigInteger
 *                - Invalid (contains decimal or exp or non-digits) -> NumberFormatException
 *                - Target Defect LANG-300: "1l", "1L", "-1l", "-1L" handling
 *            - 'f', 'F':
 *                - Float parsing, overflow check (!f.isInfinite() && !(f == 0.0F && !allZeros))
 *                - Falls through to 'd'/'D' on float overflow/underflow or NFE
 *            - 'd', 'D':
 *                - Double parsing, overflow check (!d.isInfinite() && !(d == 0.0D && !allZeros))
 *                - Falls through to BigDecimal
 *            - default -> throws NumberFormatException
 *    - Character.isDigit(lastChar):
 *        - dec == null && exp == null -> try Integer -> Long -> BigInteger
 *        - else -> try Float -> Double -> BigDecimal
 *
 * 3. isAllZeros(String):
 *    - null -> true
 *    - all '0' characters -> true (length > 0)
 *    - empty string -> false
 *    - contains non-'0' -> false
 *
 * 4. minimum / maximum (int, long):
 *    - a, b, c permutations where a < b < c, b < a < c, c < a < b, equal values, MIN_VALUE, MAX_VALUE
 *
 * 5. compare(double, double) and compare(float, float):
 *    - lhs < rhs (-1), lhs > rhs (+1), lhs == rhs (0)
 *    - NaN handling: NaN == NaN -> 0, NaN > all other values (+1 or -1)
 *    - Zero sign handling: -0.0 < +0.0 (-1)
 *    - Infinity boundaries: NEGATIVE_INFINITY, POSITIVE_INFINITY
 *
 * 6. isDigits(String):
 *    - null / empty -> false
 *    - all digits -> true
 *    - contains signs, letters, whitespace, symbols -> false
 *
 * 7. isNumber(String):
 *    - null / empty -> false
 *    - Hexadecimal: "0x...", "-0x...", "0x" (incomplete), valid/invalid hex chars
 *    - Leading sign (+/-), multiple signs
 *    - Decimals: multiple decimals, decimals in exponent
 *    - Exponents: 'e'/'E', multiple exponents, missing digits after exponent, signs after exponent
 *    - Trailing qualifiers: 'f', 'F', 'd', 'D', 'l', 'L'
 *    - Illegal characters, lonely signs, lonely "."
 */
public class NumberUtilsGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-300)
    // =========================================================================

    @Test(timeout = 4000)
    public void testLang300CreateNumberLongQualifiers() {
        // Direct regression coverage for LANG-300: lowercase 'l' and uppercase 'L'
        assertEquals(Long.valueOf(1L), NumberUtils.createNumber("1l"));
        assertEquals(Long.valueOf(1L), NumberUtils.createNumber("1L"));
        assertEquals(Long.valueOf(-1L), NumberUtils.createNumber("-1l"));
        assertEquals(Long.valueOf(-1L), NumberUtils.createNumber("-1L"));
        assertEquals(Long.valueOf(0L), NumberUtils.createNumber("0l"));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123l"));
        assertEquals(Long.valueOf(-1234567890123L), NumberUtils.createNumber("-1234567890123L"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        NumberUtils instance = new NumberUtils();
        assertNotNull(instance);
    }

    @Test(timeout = 4000)
    public void testStringToInt() {
        assertEquals(123, NumberUtils.stringToInt("123"));
        assertEquals(-456, NumberUtils.stringToInt("-456"));
        assertEquals(0, NumberUtils.stringToInt("0"));
        assertEquals(0, NumberUtils.stringToInt("invalid"));
        assertEquals(0, NumberUtils.stringToInt(null));

        // Overloaded variant with custom defaultValue
        assertEquals(10, NumberUtils.stringToInt("10", 99));
        assertEquals(99, NumberUtils.stringToInt("not_a_number", 99));
        assertEquals(42, NumberUtils.stringToInt(null, 42));
    }

    @Test(timeout = 4000)
    public void testCreateNumberBasicTypes() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));

        // Hexadecimal
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xff"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));

        // Integer, Long, BigInteger without qualifiers
        assertEquals(Integer.valueOf(12345), NumberUtils.createNumber("12345"));
        assertEquals(Integer.valueOf(-12345), NumberUtils.createNumber("-12345"));
        assertEquals(Long.valueOf(21474836480L), NumberUtils.createNumber("21474836480"));
        assertEquals(new BigInteger("123456789012345678901234567890"),
                NumberUtils.createNumber("123456789012345678901234567890"));

        // Float, Double, BigDecimal without qualifiers
        assertEquals(Float.valueOf("1.23"), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf("1.234567890123456"), NumberUtils.createNumber("1.234567890123456"));
        assertEquals(new BigDecimal("1.234567890123456789012345678901234567890"),
                NumberUtils.createNumber("1.234567890123456789012345678901234567890"));

        // Scientific notation without qualifiers
        assertEquals(Float.valueOf("1.23e2"), NumberUtils.createNumber("1.23e2"));
        assertEquals(Double.valueOf("1.23456789012345e20"), NumberUtils.createNumber("1.23456789012345e20"));
        assertEquals(Float.valueOf("1.0e-3"), NumberUtils.createNumber("1.0e-3"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithQualifiers() {
        // Float qualifiers
        assertEquals(Float.valueOf("1.23f"), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf("1.23F"), NumberUtils.createNumber("1.23F"));
        assertEquals(Float.valueOf("0.0f"), NumberUtils.createNumber("0.0f"));
        assertEquals(Float.valueOf("00.00F"), NumberUtils.createNumber("00.00F"));

        // Double qualifiers
        assertEquals(Double.valueOf("1.23d"), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf("1.23D"), NumberUtils.createNumber("1.23D"));
        assertEquals(Double.valueOf("0.0d"), NumberUtils.createNumber("0.0d"));

        // Fallback from float to double/BigDecimal when float overflows or loses precision
        // Extremely small non-zero float falls back to double/BigDecimal
        Number smallFloat = NumberUtils.createNumber("1.0e-50f");
        assertTrue(smallFloat instanceof Double || smallFloat instanceof BigDecimal);

        // Fallback from Long to BigInteger for large qualifier 'l'
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808l"));
        assertEquals(new BigInteger("-9223372036854775809"), NumberUtils.createNumber("-9223372036854775809L"));
    }

    @Test(timeout = 4000)
    public void testIndividualFactoryMethods() {
        assertEquals(Float.valueOf("3.14"), NumberUtils.createFloat("3.14"));
        assertEquals(Double.valueOf("3.1415926535"), NumberUtils.createDouble("3.1415926535"));
        assertEquals(Integer.valueOf(100), NumberUtils.createInteger("100"));
        assertEquals(Integer.valueOf(255), NumberUtils.createInteger("0xFF"));
        assertEquals(Integer.valueOf(8), NumberUtils.createInteger("010"));
        assertEquals(Long.valueOf(10000000000L), NumberUtils.createLong("10000000000"));
        assertEquals(new BigInteger("99999999999999999999"), NumberUtils.createBigInteger("99999999999999999999"));
        assertEquals(new BigDecimal("123.4567890123456789"), NumberUtils.createBigDecimal("123.4567890123456789"));
    }

    @Test(timeout = 4000)
    public void testMinimumAndMaximum() {
        // Int minimum permutations
        assertEquals(1, NumberUtils.minimum(1, 2, 3));
        assertEquals(1, NumberUtils.minimum(2, 1, 3));
        assertEquals(1, NumberUtils.minimum(3, 2, 1));
        assertEquals(1, NumberUtils.minimum(1, 1, 1));
        assertEquals(-5, NumberUtils.minimum(-1, -5, -3));
        assertEquals(Integer.MIN_VALUE, NumberUtils.minimum(0, Integer.MIN_VALUE, 10));

        // Long minimum permutations
        assertEquals(1L, NumberUtils.minimum(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.minimum(2L, 1L, 3L));
        assertEquals(1L, NumberUtils.minimum(3L, 2L, 1L));
        assertEquals(1L, NumberUtils.minimum(1L, 1L, 1L));
        assertEquals(-5L, NumberUtils.minimum(-1L, -5L, -3L));
        assertEquals(Long.MIN_VALUE, NumberUtils.minimum(0L, Long.MIN_VALUE, 10L));

        // Int maximum permutations
        assertEquals(3, NumberUtils.maximum(1, 2, 3));
        assertEquals(3, NumberUtils.maximum(2, 3, 1));
        assertEquals(3, NumberUtils.maximum(3, 2, 1));
        assertEquals(5, NumberUtils.maximum(5, 5, 5));
        assertEquals(-1, NumberUtils.maximum(-1, -5, -3));
        assertEquals(Integer.MAX_VALUE, NumberUtils.maximum(0, Integer.MAX_VALUE, 10));

        // Long maximum permutations
        assertEquals(3L, NumberUtils.maximum(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.maximum(2L, 3L, 1L));
        assertEquals(3L, NumberUtils.maximum(3L, 2L, 1L));
        assertEquals(5L, NumberUtils.maximum(5L, 5L, 5L));
        assertEquals(-1L, NumberUtils.maximum(-1L, -5L, -3L));
        assertEquals(Long.MAX_VALUE, NumberUtils.maximum(0L, Long.MAX_VALUE, 10L));
    }

    @Test(timeout = 4000)
    public void testCompareDouble() {
        assertEquals(-1, NumberUtils.compare(1.0, 2.0));
        assertEquals(+1, NumberUtils.compare(2.0, 1.0));
        assertEquals(0, NumberUtils.compare(1.5, 1.5));

        // -0.0 vs +0.0
        assertEquals(-1, NumberUtils.compare(-0.0, 0.0));
        assertEquals(+1, NumberUtils.compare(0.0, -0.0));
        assertEquals(0, NumberUtils.compare(0.0, 0.0));
        assertEquals(0, NumberUtils.compare(-0.0, -0.0));

        // NaN comparisons
        assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));
        assertEquals(+1, NumberUtils.compare(Double.NaN, 100.0));
        assertEquals(+1, NumberUtils.compare(Double.NaN, Double.POSITIVE_INFINITY));
        assertEquals(-1, NumberUtils.compare(100.0, Double.NaN));
        assertEquals(-1, NumberUtils.compare(Double.POSITIVE_INFINITY, Double.NaN));

        // Infinities
        assertEquals(-1, NumberUtils.compare(Double.NEGATIVE_INFINITY, 0.0));
        assertEquals(+1, NumberUtils.compare(Double.POSITIVE_INFINITY, Double.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testCompareFloat() {
        assertEquals(-1, NumberUtils.compare(1.0f, 2.0f));
        assertEquals(+1, NumberUtils.compare(2.0f, 1.0f));
        assertEquals(0, NumberUtils.compare(1.5f, 1.5f));

        // -0.0f vs +0.0f
        assertEquals(-1, NumberUtils.compare(-0.0f, 0.0f));
        assertEquals(+1, NumberUtils.compare(0.0f, -0.0f));
        assertEquals(0, NumberUtils.compare(0.0f, 0.0f));
        assertEquals(0, NumberUtils.compare(-0.0f, -0.0f));

        // NaN comparisons
        assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
        assertEquals(+1, NumberUtils.compare(Float.NaN, 100.0f));
        assertEquals(+1, NumberUtils.compare(Float.NaN, Float.POSITIVE_INFINITY));
        assertEquals(-1, NumberUtils.compare(100.0f, Float.NaN));
        assertEquals(-1, NumberUtils.compare(Float.POSITIVE_INFINITY, Float.NaN));

        // Infinities
        assertEquals(-1, NumberUtils.compare(Float.NEGATIVE_INFINITY, 0.0f));
        assertEquals(+1, NumberUtils.compare(Float.POSITIVE_INFINITY, Float.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("0"));
        assertTrue(NumberUtils.isDigits("1234567890"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("12a3"));
        assertFalse(NumberUtils.isDigits(" "));
    }

    @Test(timeout = 4000)
    public void testIsNumber() {
        // Null / empty
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber(" "));

        // Hexadecimal
        assertTrue(NumberUtils.isNumber("0x123"));
        assertTrue(NumberUtils.isNumber("0xABCDEF"));
        assertTrue(NumberUtils.isNumber("0xabcdef"));
        assertTrue(NumberUtils.isNumber("-0x123"));
        assertTrue(NumberUtils.isNumber("-0xABCDEF"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0xGHI"));
        assertFalse(NumberUtils.isNumber("-0x12G"));

        // Integers and signs
        assertTrue(NumberUtils.isNumber("0"));
        assertTrue(NumberUtils.isNumber("-0"));
        assertTrue(NumberUtils.isNumber("+0"));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertFalse(NumberUtils.isNumber("-"));
        assertFalse(NumberUtils.isNumber("+"));
        assertFalse(NumberUtils.isNumber("--123"));
        assertFalse(NumberUtils.isNumber("1-2"));

        // Floating point numbers
        assertTrue(NumberUtils.isNumber("1.23"));
        assertTrue(NumberUtils.isNumber(".23"));
        assertTrue(NumberUtils.isNumber("12."));
        assertTrue(NumberUtils.isNumber("-1.23"));
        assertTrue(NumberUtils.isNumber("+.23"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber(".."));
        assertFalse(NumberUtils.isNumber("1.2.3"));

        // Scientific notations
        assertTrue(NumberUtils.isNumber("1e3"));
        assertTrue(NumberUtils.isNumber("1.2E+3"));
        assertTrue(NumberUtils.isNumber("1.2e-3"));
        assertTrue(NumberUtils.isNumber("-1.2E3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1E+"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("e1"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1.2e3.4"));

        // Type qualifiers
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("-123L"));
        assertFalse(NumberUtils.isNumber("12.3L"));
        assertFalse(NumberUtils.isNumber("1e3L"));
        assertFalse(NumberUtils.isNumber("L"));

        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123F"));
        assertTrue(NumberUtils.isNumber("12.3f"));
        assertTrue(NumberUtils.isNumber(".3f"));
        assertTrue(NumberUtils.isNumber("1e3f"));
        assertFalse(NumberUtils.isNumber("f"));

        assertTrue(NumberUtils.isNumber("123d"));
        assertTrue(NumberUtils.isNumber("123D"));
        assertTrue(NumberUtils.isNumber("12.3d"));
        assertTrue(NumberUtils.isNumber(".3D"));
        assertTrue(NumberUtils.isNumber("1e3d"));
        assertFalse(NumberUtils.isNumber("d"));

        // Invalid ending characters
        assertFalse(NumberUtils.isNumber("123a"));
        assertFalse(NumberUtils.isNumber("123z"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberExtremeDecimals() {
        // Zero representations with decimal and exp
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("00.00"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0e0"));

        // Number ending with decimal point
        assertEquals(Float.valueOf(42.0f), NumberUtils.createNumber("42."));

        // Number starting with decimal point (no qualifier)
        assertEquals(Float.valueOf(0.42f), NumberUtils.createNumber(".42"));
    }

    @Test(timeout = 4000)
    public void testMinMaxBoundaries() {
        assertEquals(Long.MAX_VALUE, NumberUtils.maximum(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, NumberUtils.minimum(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, NumberUtils.maximum(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, NumberUtils.minimum(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberEmptyString() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberBlankString() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidExponentPlacement() {
        // expPos < decPos
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidLongQualifierDecimal() {
        // Decimals are illegal with 'l' / 'L'
        NumberUtils.createNumber("1.23l");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidLongQualifierExp() {
        // Exponents are illegal with 'l' / 'L'
        NumberUtils.createNumber("1e3l");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidLongQualifierNonDigits() {
        NumberUtils.createNumber("abcl");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidQualifier() {
        NumberUtils.createNumber("123q");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidDoubleOnly() {
        NumberUtils.createNumber("foo.bar");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidFormatDecimalOnly() {
        NumberUtils.createNumber(".");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateIntegerFailure() {
        NumberUtils.createInteger("invalid");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateLongFailure() {
        NumberUtils.createLong("invalid");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigIntegerFailure() {
        NumberUtils.createBigInteger("invalid");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateFloatFailure() {
        NumberUtils.createFloat("invalid");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateDoubleFailure() {
        NumberUtils.createDouble("invalid");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimalFailure() {
        NumberUtils.createBigDecimal("invalid");
    }
}
