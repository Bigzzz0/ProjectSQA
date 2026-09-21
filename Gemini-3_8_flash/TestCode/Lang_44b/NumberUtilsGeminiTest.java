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
package org.apache.commons.lang;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------
 * Targeted Class: org.apache.commons.lang.NumberUtils
 * Target Defect: LANG-457 (StringIndexOutOfBoundsException in createNumber when string is "l" or "L")
 * 
 * Branch Coverage Focus:
 * 1. createNumber(String):
 *    - null and empty ("") input handling.
 *    - Leading "--" edge-case handling.
 *    - Hexadecimal ("0x", "-0x") parsing branch.
 *    - Exponent ('e', 'E') and decimal ('.') position comparisons (expPos < decPos error condition).
 *    - Non-digit trailing character qualifiers ('l', 'L', 'f', 'F', 'd', 'D', default illegal char).
 *    - Fallback logic for qualifiers: Float -> Double -> BigDecimal, Long -> BigInteger.
 *    - Untyped numbers: Integer -> Long -> BigInteger, Float -> Double -> BigDecimal.
 *    - Zero detection (isAllZeros) with positive/negative zero mantissas and exponents.
 *    - DEFECT LANG-457: Single-character strings with type qualifiers like "l" and "L" triggering
 *      numeric.charAt(0) on empty string -> StringIndexOutOfBoundsException instead of NumberFormatException.
 * 2. stringToInt(String, int):
 *    - Normal parsing vs NumberFormatException handling returning default value.
 * 3. Specific Creators:
 *    - createFloat, createDouble, createInteger (decoding dec/hex/octal), createLong,
 *      createBigInteger, createBigDecimal.
 * 4. min / max algorithms (long and int):
 *    - All condition branches (b < a, c < a, b > a, c > a) covering all permutations.
 * 5. compare(double, double) and compare(float, float):
 *    - Standard comparisons (lhs < rhs, lhs > rhs).
 *    - Bit-level comparisons: -0.0 vs +0.0, NaN vs NaN, NaN vs Infinite, NaN vs normal numbers.
 * 6. isDigits(String):
 *    - null, empty, all digits, mixed alphanumeric, negative numbers.
 * 7. isNumber(String):
 *    - Null and empty check via StringUtils.
 *    - Leading signs ('-').
 *    - Hexadecimal representations ("0x...", "-0x...", boundary "0x", invalid chars).
 *    - Inner loop transitions: digits, decimal points (multiple points guard), exponents ('e', 'E'),
 *      exponent signs ('+', '-'), invalid chars.
 *    - Trailing qualifiers ('d', 'D', 'f', 'F', 'l', 'L') and rejection of illegal suffixes.
 * --------------------------------------------------------------------------------------------------
 */
public class NumberUtilsGeminiTest {

    // =========================================================================
    // Partition A: Object Lifecycle & Basic Conversions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        NumberUtils utils = new NumberUtils();
        assertNotNull(utils);
    }

    @Test(timeout = 4000)
    public void testStringToInt() {
        assertEquals(0, NumberUtils.stringToInt(null));
        assertEquals(0, NumberUtils.stringToInt(""));
        assertEquals(0, NumberUtils.stringToInt("invalid"));
        assertEquals(42, NumberUtils.stringToInt("42"));
        assertEquals(-42, NumberUtils.stringToInt("-42"));

        assertEquals(10, NumberUtils.stringToInt(null, 10));
        assertEquals(10, NumberUtils.stringToInt("", 10));
        assertEquals(10, NumberUtils.stringToInt("not_a_number", 10));
        assertEquals(25, NumberUtils.stringToInt("25", 10));
        assertEquals(-25, NumberUtils.stringToInt("-25", 10));
    }

    @Test(timeout = 4000)
    public void testSpecificTypeCreators() {
        assertEquals(Float.valueOf(1.23f), NumberUtils.createFloat("1.23"));
        assertEquals(Double.valueOf(1.23456789d), NumberUtils.createDouble("1.23456789"));
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(16), NumberUtils.createInteger("0x10"));
        assertEquals(Integer.valueOf(8), NumberUtils.createInteger("010"));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createLong("1234567890123"));
        assertEquals(new BigInteger("123456789012345678901234567890"), 
                     NumberUtils.createBigInteger("123456789012345678901234567890"));
        assertEquals(new BigDecimal("1234567890.1234567890"), 
                     NumberUtils.createBigDecimal("1234567890.1234567890"));
    }

    // =========================================================================
    // Partition B: Defect-Targeted Zone (LANG-457)
    // =========================================================================

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testLang457_createNumber_l() {
        // Exposes defect: single qualifier char "l" causes StringIndexOutOfBoundsException: String index out of range: 0
        NumberUtils.createNumber("l");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testLang457_createNumber_L() {
        // Exposes defect: single qualifier char "L" causes StringIndexOutOfBoundsException: String index out of range: 0
        NumberUtils.createNumber("L");
    }

    // =========================================================================
    // Partition C: createNumber Method Comprehensive Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_NullAndEmptyAndSpecialPrefixes() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
        assertNull(NumberUtils.createNumber("--"));

        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0x12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0x12"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_EmptyStringThrowsException() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_InvalidExponentBeforeDecimal() {
        // expPos < decPos triggers exception
        NumberUtils.createNumber("12e3.4");
    }

    @Test(timeout = 4000)
    public void testCreateNumber_DefaultTypesWithoutSpecifier() {
        // Integer
        assertEquals(Integer.valueOf(12345), NumberUtils.createNumber("12345"));
        assertEquals(Integer.valueOf(-12345), NumberUtils.createNumber("-12345"));

        // Long (larger than Integer.MAX_VALUE)
        assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));

        // BigInteger (larger than Long.MAX_VALUE)
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808"));

        // Float (standard decimal fitting float)
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("00.00"));

        // Double (overflows float precision/range)
        assertEquals(Double.valueOf(1.7976931348623157e+308), NumberUtils.createNumber("1.7976931348623157e+308"));
        assertEquals(Double.valueOf(1.1e200), NumberUtils.createNumber("1.1e200"));

        // BigDecimal (overflows double range)
        assertEquals(new BigDecimal("1.1e400"), NumberUtils.createNumber("1.1e400"));
    }

    @Test(timeout = 4000)
    public void testCreateNumber_TypeQualifiers_Long() {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(Long.valueOf(-123L), NumberUtils.createNumber("-123L"));
        // BigInteger fallback for Long
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808l"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808L"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_LongQualifierWithDecimalThrows() {
        NumberUtils.createNumber("1.2L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_LongQualifierWithExponentThrows() {
        NumberUtils.createNumber("1e2L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_LongQualifierWithNonDigitsThrows() {
        NumberUtils.createNumber("12aL");
    }

    @Test(timeout = 4000)
    public void testCreateNumber_TypeQualifiers_Float() {
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("00.00f"));

        // Float overflow falls through to Double
        assertEquals(Double.valueOf(1.1e200), NumberUtils.createNumber("1.1e200f"));

        // Float and Double overflow falls through to BigDecimal
        assertEquals(new BigDecimal("1.1e400"), NumberUtils.createNumber("1.1e400f"));
    }

    @Test(timeout = 4000)
    public void testCreateNumber_TypeQualifiers_Double() {
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
        assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));

        // Double overflow falls through to BigDecimal
        assertEquals(new BigDecimal("1.1e400"), NumberUtils.createNumber("1.1e400d"));
        assertEquals(new BigDecimal("1.1e400"), NumberUtils.createNumber("1.1e400D"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_IllegalLastCharThrows() {
        NumberUtils.createNumber("123z");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_InvalidNumberFormatThrows() {
        NumberUtils.createNumber("abc");
    }

    // =========================================================================
    // Partition D: Min & Max Functions
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinimum_Long() {
        assertEquals(1L, NumberUtils.minimum(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.minimum(2L, 1L, 3L));
        assertEquals(1L, NumberUtils.minimum(3L, 2L, 1L));
        assertEquals(1L, NumberUtils.minimum(1L, 1L, 1L));
        assertEquals(-5L, NumberUtils.minimum(0L, -5L, 2L));
    }

    @Test(timeout = 4000)
    public void testMinimum_Int() {
        assertEquals(1, NumberUtils.minimum(1, 2, 3));
        assertEquals(1, NumberUtils.minimum(2, 1, 3));
        assertEquals(1, NumberUtils.minimum(3, 2, 1));
        assertEquals(1, NumberUtils.minimum(1, 1, 1));
        assertEquals(-5, NumberUtils.minimum(0, -5, 2));
    }

    @Test(timeout = 4000)
    public void testMaximum_Long() {
        assertEquals(3L, NumberUtils.maximum(3L, 2L, 1L));
        assertEquals(3L, NumberUtils.maximum(2L, 3L, 1L));
        assertEquals(3L, NumberUtils.maximum(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.maximum(3L, 3L, 3L));
        assertEquals(5L, NumberUtils.maximum(0L, 5L, -2L));
    }

    @Test(timeout = 4000)
    public void testMaximum_Int() {
        assertEquals(3, NumberUtils.maximum(3, 2, 1));
        assertEquals(3, NumberUtils.maximum(2, 3, 1));
        assertEquals(3, NumberUtils.maximum(1, 2, 3));
        assertEquals(3, NumberUtils.maximum(3, 3, 3));
        assertEquals(5, NumberUtils.maximum(0, 5, -2));
    }

    // =========================================================================
    // Partition E: Compare (Double and Float)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCompare_Double() {
        assertEquals(-1, NumberUtils.compare(1.0d, 2.0d));
        assertEquals(1, NumberUtils.compare(2.0d, 1.0d));
        assertEquals(0, NumberUtils.compare(1.5d, 1.5d));

        // Negative zero vs positive zero
        assertEquals(-1, NumberUtils.compare(-0.0d, 0.0d));
        assertEquals(1, NumberUtils.compare(0.0d, -0.0d));
        assertEquals(0, NumberUtils.compare(-0.0d, -0.0d));
        assertEquals(0, NumberUtils.compare(0.0d, 0.0d));

        // NaN handling
        assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));
        assertEquals(1, NumberUtils.compare(Double.NaN, Double.POSITIVE_INFINITY));
        assertEquals(-1, NumberUtils.compare(Double.POSITIVE_INFINITY, Double.NaN));
        assertEquals(1, NumberUtils.compare(Double.NaN, 0.0d));
        assertEquals(-1, NumberUtils.compare(0.0d, Double.NaN));
    }

    @Test(timeout = 4000)
    public void testCompare_Float() {
        assertEquals(-1, NumberUtils.compare(1.0f, 2.0f));
        assertEquals(1, NumberUtils.compare(2.0f, 1.0f));
        assertEquals(0, NumberUtils.compare(1.5f, 1.5f));

        // Negative zero vs positive zero
        assertEquals(-1, NumberUtils.compare(-0.0f, 0.0f));
        assertEquals(1, NumberUtils.compare(0.0f, -0.0f));
        assertEquals(0, NumberUtils.compare(-0.0f, -0.0f));
        assertEquals(0, NumberUtils.compare(0.0f, 0.0f));

        // NaN handling
        assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
        assertEquals(1, NumberUtils.compare(Float.NaN, Float.POSITIVE_INFINITY));
        assertEquals(-1, NumberUtils.compare(Float.POSITIVE_INFINITY, Float.NaN));
        assertEquals(1, NumberUtils.compare(Float.NaN, 0.0f));
        assertEquals(-1, NumberUtils.compare(0.0f, Float.NaN));
    }

    // =========================================================================
    // Partition F: isDigits Check
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("  "));
        assertFalse(NumberUtils.isDigits("-123"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("123a"));
        assertTrue(NumberUtils.isDigits("1234567890"));
    }

    // =========================================================================
    // Partition G: isNumber Validation
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsNumber_NullEmptyWhitespace() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
    }

    @Test(timeout = 4000)
    public void testIsNumber_Hexadecimal() {
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertTrue(NumberUtils.isNumber("0x1234abcd"));
        assertTrue(NumberUtils.isNumber("0x1234ABCD"));
        assertTrue(NumberUtils.isNumber("-0x1234abcd"));
        assertFalse(NumberUtils.isNumber("0x1234z"));
        assertFalse(NumberUtils.isNumber("-0x1234z"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_StandardIntegersAndDecimals() {
        assertTrue(NumberUtils.isNumber("0"));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("123.456"));
        assertTrue(NumberUtils.isNumber("-123.456"));
        assertTrue(NumberUtils.isNumber(".456"));
        assertTrue(NumberUtils.isNumber("-.456"));
        assertTrue(NumberUtils.isNumber("456."));
        assertTrue(NumberUtils.isNumber("-456."));

        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("-"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber(".."));
    }

    @Test(timeout = 4000)
    public void testIsNumber_ScientificNotation() {
        assertTrue(NumberUtils.isNumber("1e2"));
        assertTrue(NumberUtils.isNumber("1E2"));
        assertTrue(NumberUtils.isNumber("1.2e3"));
        assertTrue(NumberUtils.isNumber("-1.2e3"));
        assertTrue(NumberUtils.isNumber("1.2e+3"));
        assertTrue(NumberUtils.isNumber("1.2e-3"));

        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("e12"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1.2e3.4"));
        assertFalse(NumberUtils.isNumber("1+2"));
        assertFalse(NumberUtils.isNumber("1-2"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_TypeQualifiers() {
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("-123L"));
        assertTrue(NumberUtils.isNumber("1.2f"));
        assertTrue(NumberUtils.isNumber("1.2F"));
        assertTrue(NumberUtils.isNumber("1.2d"));
        assertTrue(NumberUtils.isNumber("1.2D"));
        assertTrue(NumberUtils.isNumber("1e2f"));
        assertTrue(NumberUtils.isNumber("1e2d"));

        // Qualifier errors
        assertFalse(NumberUtils.isNumber("1e2L")); // L with exponent not allowed
        assertFalse(NumberUtils.isNumber("L"));
        assertFalse(NumberUtils.isNumber("l"));
        assertFalse(NumberUtils.isNumber("f"));
        assertFalse(NumberUtils.isNumber("F"));
        assertFalse(NumberUtils.isNumber("d"));
        assertFalse(NumberUtils.isNumber("D"));
        assertFalse(NumberUtils.isNumber(".d"));
        assertFalse(NumberUtils.isNumber(".f"));
        assertFalse(NumberUtils.isNumber("123z"));
        assertFalse(NumberUtils.isNumber("123A"));
    }
}