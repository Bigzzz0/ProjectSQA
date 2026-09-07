package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigInteger;
import java.math.BigDecimal;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.lang3.math.NumberUtils
 * Primary Target: createNumber(String str) & auxiliary parsing/math methods
 *
 * Decision / Condition Matrix:
 * 1. String validation:
 *    - null -> return null
 *    - empty / whitespace -> throw NumberFormatException
 * 2. Hexadecimal Branching (starts with 0x, 0X, -0x, -0X, #, -#):
 *    - hexDigits <= 8 -> createInteger
 *    - hexDigits > 8 && <= 16 -> createLong
 *    - hexDigits > 16 -> createBigInteger
 *    - Boundary Hex values (e.g. 0x80000000, 0xFFFFFFFF, negative hex representations)
 * 3. Decimal and Exponent Analysis (decPos, expPos):
 *    - Decimal without exponent: "1.23"
 *    - Decimal with exponent: "1.23e4", "1.23E-4"
 *    - Exponent without decimal: "1e4", "1E-4"
 *    - Invalid combinations: expPos < decPos ("1e2.3"), double exponent ("1.2e3E4")
 * 4. Explicit Type Qualifiers (l, L, f, F, d, D):
 *    - 'l', 'L': only allowed if dec == null && exp == null and valid integral digits
 *      - fits Long -> Long
 *      - overflow Long -> BigInteger
 *    - 'f', 'F': createFloat; if infinite or (zero and !allZeros) -> fall-through to Double/BigDecimal
 *    - 'd', 'D': createDouble; if infinite or (zero and !allZeros) -> createBigDecimal
 *    - Other non-digit lastChar -> throw NumberFormatException
 * 5. Implicit Type Promotion (no qualifier):
 *    - No dec, no exp -> Integer -> Long -> BigInteger
 *    - Has dec or exp:
 *      - numDecimals <= 7: try Float (check infinite/precision loss), then Double, then BigDecimal
 *      - numDecimals <= 16: try Double (check infinite/precision loss), then BigDecimal
 *      - numDecimals > 16: BigDecimal
 * 6. Edge & Boundary Handling:
 *    - Leading zeros / octal notation
 *    - All-zero mantissas/exponents ("00E0", "0.0")
 *    - Negative zero ("-0.0") and signs in exponent ("1.2e+3")
 *    - createBigInteger / createBigDecimal specific prefixes ("--", "0", "-0")
 * 7. Auxiliary Functions (min, max, isDigits, isNumber, conversions to primitives):
 *    - Full condition testing across all array and 3-param overloads
 *    - Float/Double NaN and Infinity branches in min/max
 */
public class NumberUtilsGeminiTest {

    // =========================================================================
    // PARTITION A: Standard Numeric Primitives & createNumber Implicit Parsing
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_IntegerBoundary() {
        Number minInt = NumberUtils.createNumber("-2147483648");
        assertTrue("Expected Integer", minInt instanceof Integer);
        assertEquals(Integer.MIN_VALUE, minInt.intValue());

        Number maxInt = NumberUtils.createNumber("2147483647");
        assertTrue("Expected Integer", maxInt instanceof Integer);
        assertEquals(Integer.MAX_VALUE, maxInt.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_LongBoundary() {
        Number overflowIntPos = NumberUtils.createNumber("2147483648");
        assertTrue("Expected Long", overflowIntPos instanceof Long);
        assertEquals(2147483648L, overflowIntPos.longValue());

        Number overflowIntNeg = NumberUtils.createNumber("-2147483649");
        assertTrue("Expected Long", overflowIntNeg instanceof Long);
        assertEquals(-2147483649L, overflowIntNeg.longValue());

        Number maxLong = NumberUtils.createNumber("9223372036854775807");
        assertTrue("Expected Long", maxLong instanceof Long);
        assertEquals(Long.MAX_VALUE, maxLong.longValue());

        Number minLong = NumberUtils.createNumber("-9223372036854775808");
        assertTrue("Expected Long", minLong instanceof Long);
        assertEquals(Long.MIN_VALUE, minLong.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_BigIntegerBoundary() {
        Number overflowLongPos = NumberUtils.createNumber("9223372036854775808");
        assertTrue("Expected BigInteger", overflowLongPos instanceof BigInteger);
        assertEquals(new BigInteger("9223372036854775808"), overflowLongPos);

        Number overflowLongNeg = NumberUtils.createNumber("-9223372036854775809");
        assertTrue("Expected BigInteger", overflowLongNeg instanceof BigInteger);
        assertEquals(new BigInteger("-9223372036854775809"), overflowLongNeg);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_ImplicitFloat() {
        Number num = NumberUtils.createNumber("12.34");
        assertTrue("Expected Float", num instanceof Float);
        assertEquals(12.34f, num.floatValue(), 1e-5f);

        Number trailingDot = NumberUtils.createNumber("12.");
        assertTrue("Expected Float", trailingDot instanceof Float);
        assertEquals(12.0f, trailingDot.floatValue(), 1e-5f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_ImplicitDouble() {
        // 8 decimals past decimal point triggers Double branch (> 7 and <= 16)
        Number num = NumberUtils.createNumber("1.12345678");
        assertTrue("Expected Double", num instanceof Double);
        assertEquals(1.12345678d, num.doubleValue(), 1e-9d);

        Number sixteenDecimals = NumberUtils.createNumber("1.1234567890123456");
        assertTrue("Expected Double", sixteenDecimals instanceof Double);
        assertEquals(1.1234567890123456d, sixteenDecimals.doubleValue(), 1e-16d);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_ImplicitBigDecimal() {
        // 17 decimals triggers BigDecimal branch (> 16)
        String val = "1.12345678901234567";
        Number num = NumberUtils.createNumber(val);
        assertTrue("Expected BigDecimal", num instanceof BigDecimal);
        assertEquals(new BigDecimal(val), num);
    }

    // =========================================================================
    // PARTITION B: Hexadecimal Representations & Sign Handling (Lang-1 Target)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_HexPrefixes_Integer() {
        Number hexLower = NumberUtils.createNumber("0x10");
        assertTrue("Expected Integer", hexLower instanceof Integer);
        assertEquals(16, hexLower.intValue());

        Number hexUpper = NumberUtils.createNumber("0X10");
        assertTrue("Expected Integer", hexUpper instanceof Integer);
        assertEquals(16, hexUpper.intValue());

        Number hexHash = NumberUtils.createNumber("#10");
        assertTrue("Expected Integer", hexHash instanceof Integer);
        assertEquals(16, hexHash.intValue());

        Number hexNegLower = NumberUtils.createNumber("-0x10");
        assertTrue("Expected Integer", hexNegLower instanceof Integer);
        assertEquals(-16, hexNegLower.intValue());

        Number hexNegUpper = NumberUtils.createNumber("-0X10");
        assertTrue("Expected Integer", hexNegUpper instanceof Integer);
        assertEquals(-16, hexNegUpper.intValue());

        Number hexNegHash = NumberUtils.createNumber("-#10");
        assertTrue("Expected Integer", hexNegHash instanceof Integer);
        assertEquals(-16, hexNegHash.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_HexPrefixes_Long() {
        // 9 hex digits exceeds int (hexDigits > 8 && <= 16)
        Number hexLongPos = NumberUtils.createNumber("0x100000000");
        assertTrue("Expected Long", hexLongPos instanceof Long);
        assertEquals(0x100000000L, hexLongPos.longValue());

        Number hexLongNeg = NumberUtils.createNumber("-0x100000000");
        assertTrue("Expected Long", hexLongNeg instanceof Long);
        assertEquals(-0x100000000L, hexLongNeg.longValue());

        Number hexHashLong = NumberUtils.createNumber("#100000000");
        assertTrue("Expected Long", hexHashLong instanceof Long);
        assertEquals(0x100000000L, hexHashLong.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_HexPrefixes_BigInteger() {
        // 17 hex digits exceeds long (hexDigits > 16)
        String hex17 = "0x10000000000000000";
        Number hexBIPos = NumberUtils.createNumber(hex17);
        assertTrue("Expected BigInteger", hexBIPos instanceof BigInteger);
        assertEquals(new BigInteger("10000000000000000", 16), hexBIPos);

        String hexNeg17 = "-0x10000000000000000";
        Number hexBINeg = NumberUtils.createNumber(hexNeg17);
        assertTrue("Expected BigInteger", hexBINeg instanceof BigInteger);
        assertEquals(new BigInteger("-10000000000000000", 16), hexBINeg);

        String hexHash17 = "#10000000000000000";
        Number hexHashBI = NumberUtils.createNumber(hexHash17);
        assertTrue("Expected BigInteger", hexHashBI instanceof BigInteger);
        assertEquals(new BigInteger("10000000000000000", 16), hexHashBI);
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger_HexAndOctalPaths() {
        assertEquals(new BigInteger("26"), NumberUtils.createBigInteger("0x1a"));
        assertEquals(new BigInteger("-26"), NumberUtils.createBigInteger("-0x1a"));
        assertEquals(new BigInteger("26"), NumberUtils.createBigInteger("#1a"));
        assertEquals(new BigInteger("-26"), NumberUtils.createBigInteger("-#1a"));
        assertEquals(new BigInteger("8"), NumberUtils.createBigInteger("010"));
        assertEquals(new BigInteger("-8"), NumberUtils.createBigInteger("-010"));
        assertEquals(new BigInteger("0"), NumberUtils.createBigInteger("0"));
        assertEquals(new BigInteger("0"), NumberUtils.createBigInteger("-0"));
        assertNull(NumberUtils.createBigInteger(null));
    }

    // =========================================================================
    // PARTITION C: Scientific / Exponent Notations
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_Scientific_Valid() {
        Number f1 = NumberUtils.createNumber("1.2e3");
        assertTrue("Expected Float", f1 instanceof Float);
        assertEquals(1200.0f, f1.floatValue(), 1e-5f);

        Number f2 = NumberUtils.createNumber("1.2E3");
        assertTrue("Expected Float", f2 instanceof Float);
        assertEquals(1200.0f, f2.floatValue(), 1e-5f);

        Number f3 = NumberUtils.createNumber("1.2e+3");
        assertTrue("Expected Float", f3 instanceof Float);
        assertEquals(1200.0f, f3.floatValue(), 1e-5f);

        Number f4 = NumberUtils.createNumber("-2.5E-4");
        assertTrue("Expected Float", f4 instanceof Float);
        assertEquals(-0.00025f, f4.floatValue(), 1e-7f);

        Number zeroExp = NumberUtils.createNumber("00E0");
        assertTrue("Expected Float", zeroExp instanceof Float);
        assertEquals(0.0f, zeroExp.floatValue(), 1e-5f);

        Number mantissaOnlyExp = NumberUtils.createNumber("1e3");
        assertTrue("Expected Float", mantissaOnlyExp instanceof Float);
        assertEquals(1000.0f, mantissaOnlyExp.floatValue(), 1e-5f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Scientific_FallthroughPrecision() {
        // Float underflow to 0.0 with !allZeros falls through to Double
        Number smallExp = NumberUtils.createNumber("1e-50");
        assertTrue("Expected Double", smallExp instanceof Double);
        assertEquals(1e-50, smallExp.doubleValue(), 1e-60);

        // Float overflow to infinity falls through to Double
        Number largeExp = NumberUtils.createNumber("1e50");
        assertTrue("Expected Double", largeExp instanceof Double);
        assertEquals(1e50, largeExp.doubleValue(), 1e40);

        // Exceeding Double limits falls through to BigDecimal
        Number hugeExp = NumberUtils.createNumber("1e500");
        assertTrue("Expected BigDecimal", hugeExp instanceof BigDecimal);
        assertEquals(new BigDecimal("1e500"), hugeExp);

        Number tinyExp = NumberUtils.createNumber("1e-500");
        assertTrue("Expected BigDecimal", tinyExp instanceof BigDecimal);
        assertEquals(new BigDecimal("1e-500"), tinyExp);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_InvalidExponentPosition_DecimalAfterExp() {
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_InvalidExponent_DoubleExponentWithDec() {
        NumberUtils.createNumber("1.2e3E4");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_InvalidExponent_DoubleExponentNoDec() {
        NumberUtils.createNumber("12e3E4");
    }

    // =========================================================================
    // PARTITION D: Type Qualifiers & Case Sensitivity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_TypeQualifier_Long() {
        Number lLower = NumberUtils.createNumber("12345l");
        assertTrue("Expected Long", lLower instanceof Long);
        assertEquals(12345L, lLower.longValue());

        Number lUpper = NumberUtils.createNumber("12345L");
        assertTrue("Expected Long", lUpper instanceof Long);
        assertEquals(12345L, lUpper.longValue());

        Number lNeg = NumberUtils.createNumber("-12345L");
        assertTrue("Expected Long", lNeg instanceof Long);
        assertEquals(-12345L, lNeg.longValue());

        // Long overflow with qualifier promotes to BigInteger
        Number lOverflow = NumberUtils.createNumber("9223372036854775808L");
        assertTrue("Expected BigInteger", lOverflow instanceof BigInteger);
        assertEquals(new BigInteger("9223372036854775808"), lOverflow);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_TypeQualifier_Long_InvalidDecimal() {
        NumberUtils.createNumber("1.2L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_TypeQualifier_Long_InvalidExp() {
        NumberUtils.createNumber("1e2L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_TypeQualifier_Long_BareSign() {
        NumberUtils.createNumber("-L");
    }

    @Test(timeout = 4000)
    public void testCreateNumber_TypeQualifier_Float() {
        Number fLower = NumberUtils.createNumber("12.34f");
        assertTrue("Expected Float", fLower instanceof Float);
        assertEquals(12.34f, fLower.floatValue(), 1e-5f);

        Number fUpper = NumberUtils.createNumber("12.34F");
        assertTrue("Expected Float", fUpper instanceof Float);
        assertEquals(12.34f, fUpper.floatValue(), 1e-5f);

        Number fZero = NumberUtils.createNumber("0.0f");
        assertTrue("Expected Float", fZero instanceof Float);
        assertEquals(0.0f, fZero.floatValue(), 1e-5f);

        // Underflow with 'f' qualifier falls through to Double
        Number fUnderflow = NumberUtils.createNumber("1.0e-50f");
        assertTrue("Expected Double", fUnderflow instanceof Double);
        assertEquals(1.0e-50, fUnderflow.doubleValue(), 1e-60);

        // Overflow with 'f' qualifier falls through to Double
        Number fOverflow = NumberUtils.createNumber("1.0e50f");
        assertTrue("Expected Double", fOverflow instanceof Double);
        assertEquals(1.0e50, fOverflow.doubleValue(), 1e40);

        // Overflow with 'f' exceeding Double falls through to BigDecimal
        Number fHuge = NumberUtils.createNumber("1.0e500f");
        assertTrue("Expected BigDecimal", fHuge instanceof BigDecimal);
        assertEquals(new BigDecimal("1.0e500"), fHuge);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_TypeQualifier_Double() {
        Number dLower = NumberUtils.createNumber("12.34d");
        assertTrue("Expected Double", dLower instanceof Double);
        assertEquals(12.34d, dLower.doubleValue(), 1e-9d);

        Number dUpper = NumberUtils.createNumber("12.34D");
        assertTrue("Expected Double", dUpper instanceof Double);
        assertEquals(12.34d, dUpper.doubleValue(), 1e-9d);

        Number dZero = NumberUtils.createNumber("0.0d");
        assertTrue("Expected Double", dZero instanceof Double);
        assertEquals(0.0d, dZero.doubleValue(), 1e-9d);

        // Underflow with 'd' qualifier promotes to BigDecimal
        Number dUnderflow = NumberUtils.createNumber("1.0e-500d");
        assertTrue("Expected BigDecimal", dUnderflow instanceof BigDecimal);
        assertEquals(new BigDecimal("1.0e-500"), dUnderflow);

        // Overflow with 'd' qualifier promotes to BigDecimal
        Number dOverflow = NumberUtils.createNumber("1.0e500d");
        assertTrue("Expected BigDecimal", dOverflow instanceof BigDecimal);
        assertEquals(new BigDecimal("1.0e500"), dOverflow);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_TypeQualifier_InvalidQualifier() {
        NumberUtils.createNumber("1234z");
    }

    // =========================================================================
    // PARTITION E: Edge, Boundary & Degenerate Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_Null() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_EmptyString() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_BlankString() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_BareDot() {
        NumberUtils.createNumber(".");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_MultipleDots() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_BareHexPrefix() {
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_BareHexPrefixNegative() {
        NumberUtils.createNumber("-0x");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_BareHashHex() {
        NumberUtils.createNumber("#");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_BareHashHexNegative() {
        NumberUtils.createNumber("-#");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_InvalidCharacters() {
        NumberUtils.createNumber("abc");
    }

    // =========================================================================
    // PARTITION F: Isolated Parsers (Float, Double, Int, Long, BigInt, BigDec)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(3.14f), NumberUtils.createFloat("3.14"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateFloat_Invalid() {
        NumberUtils.createFloat("invalid");
    }

    @Test(timeout = 4000)
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(3.14159d), NumberUtils.createDouble("3.14159"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateDouble_Invalid() {
        NumberUtils.createDouble("invalid");
    }

    @Test(timeout = 4000)
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(16), NumberUtils.createInteger("0x10"));
        assertEquals(Integer.valueOf(8), NumberUtils.createInteger("010"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateInteger_Invalid() {
        NumberUtils.createInteger("invalid");
    }

    @Test(timeout = 4000)
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(123456789L), NumberUtils.createLong("123456789"));
        assertEquals(Long.valueOf(16L), NumberUtils.createLong("0x10"));
        assertEquals(Long.valueOf(8L), NumberUtils.createLong("010"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateLong_Invalid() {
        NumberUtils.createLong("invalid");
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("123.456"), NumberUtils.createBigDecimal("123.456"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimal_Empty() {
        NumberUtils.createBigDecimal("");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimal_Blank() {
        NumberUtils.createBigDecimal("   ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimal_DoubleMinus() {
        NumberUtils.createBigDecimal("--123");
    }

    // =========================================================================
    // PARTITION G: Primitive Conversion Overloads with Defaults
    // =========================================================================

    @Test(timeout = 4000)
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(42, NumberUtils.toInt("42"));
        assertEquals(99, NumberUtils.toInt(null, 99));
        assertEquals(99, NumberUtils.toInt("abc", 99));
        assertEquals(42, NumberUtils.toInt("42", 99));
    }

    @Test(timeout = 4000)
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(42L, NumberUtils.toLong("42"));
        assertEquals(99L, NumberUtils.toLong(null, 99L));
        assertEquals(99L, NumberUtils.toLong("abc", 99L));
        assertEquals(42L, NumberUtils.toLong("42", 99L));
    }

    @Test(timeout = 4000)
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 1e-5f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 1e-5f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 1e-5f);
        assertEquals(4.2f, NumberUtils.toFloat("4.2"), 1e-5f);
        assertEquals(9.9f, NumberUtils.toFloat(null, 9.9f), 1e-5f);
        assertEquals(9.9f, NumberUtils.toFloat("abc", 9.9f), 1e-5f);
        assertEquals(4.2f, NumberUtils.toFloat("4.2", 9.9f), 1e-5f);
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 1e-9d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 1e-9d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 1e-9d);
        assertEquals(4.2d, NumberUtils.toDouble("4.2"), 1e-9d);
        assertEquals(9.9d, NumberUtils.toDouble(null, 9.9d), 1e-9d);
        assertEquals(9.9d, NumberUtils.toDouble("abc", 9.9d), 1e-9d);
        assertEquals(4.2d, NumberUtils.toDouble("4.2", 9.9d), 1e-9d);
    }

    @Test(timeout = 4000)
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 0, NumberUtils.toByte("abc"));
        assertEquals((byte) 42, NumberUtils.toByte("42"));
        assertEquals((byte) 99, NumberUtils.toByte(null, (byte) 99));
        assertEquals((byte) 99, NumberUtils.toByte("abc", (byte) 99));
        assertEquals((byte) 42, NumberUtils.toByte("42", (byte) 99));
    }

    @Test(timeout = 4000)
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 0, NumberUtils.toShort("abc"));
        assertEquals((short) 42, NumberUtils.toShort("42"));
        assertEquals((short) 99, NumberUtils.toShort(null, (short) 99));
        assertEquals((short) 99, NumberUtils.toShort("abc", (short) 99));
        assertEquals((short) 42, NumberUtils.toShort("42", (short) 99));
    }

    // =========================================================================
    // PARTITION H: Validation Methods (isDigits, isNumber)
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("  "));
        assertFalse(NumberUtils.isDigits("123a"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertTrue(NumberUtils.isDigits("0"));
        assertTrue(NumberUtils.isDigits("123456789"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_NullEmpty() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
    }

    @Test(timeout = 4000)
    public void testIsNumber_Hex() {
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("0xABCDEF"));
        assertTrue(NumberUtils.isNumber("0xabcdef"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0x12G"));
        assertFalse(NumberUtils.isNumber("-0x12G"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_DecimalsAndExponents() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("12.34"));
        assertTrue(NumberUtils.isNumber("-12.34"));
        assertTrue(NumberUtils.isNumber(".34"));
        assertTrue(NumberUtils.isNumber("12."));
        assertTrue(NumberUtils.isNumber("1e3"));
        assertTrue(NumberUtils.isNumber("1.2e3"));
        assertTrue(NumberUtils.isNumber("-1.2E-3"));
        assertTrue(NumberUtils.isNumber("1.2e+3"));

        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("e1"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1.2e3.4"));
        assertFalse(NumberUtils.isNumber("+123"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_TypeQualifiers() {
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("12.3f"));
        assertTrue(NumberUtils.isNumber("12.3F"));
        assertTrue(NumberUtils.isNumber("12.3d"));
        assertTrue(NumberUtils.isNumber("12.3D"));
        assertTrue(NumberUtils.isNumber("12e3f"));

        assertFalse(NumberUtils.isNumber("12.3L"));
        assertFalse(NumberUtils.isNumber("12e3L"));
        assertFalse(NumberUtils.isNumber("L"));
        assertFalse(NumberUtils.isNumber("f"));
        assertFalse(NumberUtils.isNumber("d"));
        assertFalse(NumberUtils.isNumber("123a"));
    }

    // =========================================================================
    // PARTITION I: Array min/max Operations & Exception Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinMax_LongArray() {
        long[] arr = { 5L, 2L, 9L, -3L, 7L };
        assertEquals(-3L, NumberUtils.min(arr));
        assertEquals(9L, NumberUtils.max(arr));

        long[] single = { 42L };
        assertEquals(42L, NumberUtils.min(single));
        assertEquals(42L, NumberUtils.max(single));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_LongArrayNull() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_LongArrayEmpty() {
        NumberUtils.min(new long[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_LongArrayNull() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_LongArrayEmpty() {
        NumberUtils.max(new long[0]);
    }

    @Test(timeout = 4000)
    public void testMinMax_IntArray() {
        int[] arr = { 5, 2, 9, -3, 7 };
        assertEquals(-3, NumberUtils.min(arr));
        assertEquals(9, NumberUtils.max(arr));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_IntArrayNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_IntArrayEmpty() {
        NumberUtils.min(new int[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_IntArrayNull() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_IntArrayEmpty() {
        NumberUtils.max(new int[0]);
    }

    @Test(timeout = 4000)
    public void testMinMax_ShortArray() {
        short[] arr = { (short) 5, (short) 2, (short) 9, (short) -3, (short) 7 };
        assertEquals((short) -3, NumberUtils.min(arr));
        assertEquals((short) 9, NumberUtils.max(arr));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_ShortArrayNull() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_ShortArrayEmpty() {
        NumberUtils.min(new short[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_ShortArrayNull() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_ShortArrayEmpty() {
        NumberUtils.max(new short[0]);
    }

    @Test(timeout = 4000)
    public void testMinMax_ByteArray() {
        byte[] arr = { (byte) 5, (byte) 2, (byte) 9, (byte) -3, (byte) 7 };
        assertEquals((byte) -3, NumberUtils.min(arr));
        assertEquals((byte) 9, NumberUtils.max(arr));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_ByteArrayNull() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_ByteArrayEmpty() {
        NumberUtils.min(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_ByteArrayNull() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_ByteArrayEmpty() {
        NumberUtils.max(new byte[0]);
    }

    @Test(timeout = 4000)
    public void testMinMax_DoubleArray() {
        double[] arr = { 5.2d, 2.1d, 9.8d, -3.4d, 7.0d };
        assertEquals(-3.4d, NumberUtils.min(arr), 1e-9d);
        assertEquals(9.8d, NumberUtils.max(arr), 1e-9d);

        double[] nanArr = { 1.0d, Double.NaN, 2.0d };
        assertTrue(Double.isNaN(NumberUtils.min(nanArr)));
        assertTrue(Double.isNaN(NumberUtils.max(nanArr)));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_DoubleArrayNull() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_DoubleArrayEmpty() {
        NumberUtils.min(new double[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_DoubleArrayNull() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_DoubleArrayEmpty() {
        NumberUtils.max(new double[0]);
    }

    @Test(timeout = 4000)
    public void testMinMax_FloatArray() {
        float[] arr = { 5.2f, 2.1f, 9.8f, -3.4f, 7.0f };
        assertEquals(-3.4f, NumberUtils.min(arr), 1e-5f);
        assertEquals(9.8f, NumberUtils.max(arr), 1e-5f);

        float[] nanArr = { 1.0f, Float.NaN, 2.0f };
        assertTrue(Float.isNaN(NumberUtils.min(nanArr)));
        assertTrue(Float.isNaN(NumberUtils.max(nanArr)));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_FloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_FloatArrayEmpty() {
        NumberUtils.min(new float[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_FloatArrayNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_FloatArrayEmpty() {
        NumberUtils.max(new float[0]);
    }

    // =========================================================================
    // PARTITION J: Three-Parameter min/max Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinMax_ThreeParams_Long() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));

        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
    }

    @Test(timeout = 4000)
    public void testMinMax_ThreeParams_Int() {
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(1, NumberUtils.min(3, 2, 1));

        assertEquals(3, NumberUtils.max(3, 2, 1));
        assertEquals(3, NumberUtils.max(1, 3, 2));
        assertEquals(3, NumberUtils.max(1, 2, 3));
    }

    @Test(timeout = 4000)
    public void testMinMax_ThreeParams_Short() {
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));

        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 2, (short) 1));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
    }

    @Test(timeout = 4000)
    public void testMinMax_ThreeParams_Byte() {
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));

        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
    }

    @Test(timeout = 4000)
    public void testMinMax_ThreeParams_Double() {
        assertEquals(1.1d, NumberUtils.min(1.1d, 2.2d, 3.3d), 1e-9d);
        assertEquals(1.1d, NumberUtils.min(3.3d, 1.1d, 2.2d), 1e-9d);
        assertEquals(1.1d, NumberUtils.min(3.3d, 2.2d, 1.1d), 1e-9d);

        assertEquals(3.3d, NumberUtils.max(3.3d, 2.2d, 1.1d), 1e-9d);
        assertEquals(3.3d, NumberUtils.max(1.1d, 3.3d, 2.2d), 1e-9d);
        assertEquals(3.3d, NumberUtils.max(1.1d, 2.2d, 3.3d), 1e-9d);

        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 2.0d, 3.0d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.0d, Double.NaN, 3.0d)));
    }

    @Test(timeout = 4000)
    public void testMinMax_ThreeParams_Float() {
        assertEquals(1.1f, NumberUtils.min(1.1f, 2.2f, 3.3f), 1e-5f);
        assertEquals(1.1f, NumberUtils.min(3.3f, 1.1f, 2.2f), 1e-5f);
        assertEquals(1.1f, NumberUtils.min(3.3f, 2.2f, 1.1f), 1e-5f);

        assertEquals(3.3f, NumberUtils.max(3.3f, 2.2f, 1.1f), 1e-5f);
        assertEquals(3.3f, NumberUtils.max(1.1f, 3.3f, 2.2f), 1e-5f);
        assertEquals(3.3f, NumberUtils.max(1.1f, 2.2f, 3.3f), 1e-5f);

        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 2.0f, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, Float.NaN, 3.0f)));
    }

    // =========================================================================
    // PARTITION K: Static Constants & Constructor Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndConstants() {
        assertNotNull(new NumberUtils());

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