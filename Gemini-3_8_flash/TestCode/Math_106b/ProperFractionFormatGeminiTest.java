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
package org.apache.commons.math.fraction;

import static org.junit.Assert.*;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math.fraction.ProperFractionFormat
 *
 * Decision / Condition Matrix Covered:
 * 1. Constructor Variants:
 *    - ProperFractionFormat() -> Default locale formats
 *    - ProperFractionFormat(NumberFormat) -> Cloned across whole/num/den
 *    - ProperFractionFormat(NumberFormat, NumberFormat, NumberFormat) -> Distinct format objects
 * 2. setWholeFormat(NumberFormat):
 *    - format == null -> throws IllegalArgumentException
 *    - format != null -> mutates wholeFormat
 * 3. format(Fraction, StringBuffer, FieldPosition):
 *    - whole != 0 (positive) -> wholeFormat.format(...) + ' ' + Math.abs(num) + " / " + den
 *    - whole != 0 (negative) -> wholeFormat.format(...) + ' ' + Math.abs(num) + " / " + den
 *    - whole == 0 (num >= 0) -> numeratorFormat.format(...) + " / " + den (no whole prefix)
 *    - whole == 0 (num < 0)  -> numeratorFormat.format(...) + " / " + den
 * 4. parse(String, ParsePosition):
 *    - super.parse succeeds (e.g. improper fraction without whole part like "1 / 2") -> early return
 *    - super.parse returns null:
 *      * whole parse fails (whole == null) -> reset index to initial, return null
 *      * num parse fails (num == null) -> reset index to initial, return null
 *      * switch(c):
 *        - case 0: no '/' character found -> return new Fraction(num.intValue(), 1)
 *        - case '/': proceed to denominator
 *        - default: invalid separator -> reset index, setErrorIndex, return null
 *      * den parse fails (den == null) -> reset index to initial, return null
 *      * valid whole, num, den parsed -> calculate ((|w| * d) + n) * sign(w) / d
 * 5. Defect Targeted (Defects4J Ground Truth: FractionFormatTest::testParseProperInvalidMinus):
 *    - "2 -2/3": Negative numerator in proper fraction must be rejected as invalid.
 *    - "2 2/-3": Negative denominator in proper fraction must be rejected as invalid.
 *    - "-2 -2/3" and "-2 2/-3": Minus signs only permitted in the leading whole part.
 */
public class ProperFractionFormatGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatPositiveProperFraction() {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction fraction = new Fraction(7, 2); // 3 1/2
        String result = format.format(fraction);
        assertEquals("3 1 / 2", result);
    }

    @Test(timeout = 4000)
    public void testFormatNegativeProperFraction() {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction fraction = new Fraction(-7, 2); // -3 1/2
        String result = format.format(fraction);
        assertEquals("-3 1 / 2", result);
    }

    @Test(timeout = 4000)
    public void testFormatZeroWholeFraction() {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction fraction = new Fraction(1, 3); // whole = 0
        String result = format.format(fraction);
        assertEquals("1 / 3", result);

        Fraction negFraction = new Fraction(-1, 3); // whole = 0
        String negResult = format.format(negFraction);
        assertEquals("-1 / 3", negResult);
    }

    @Test(timeout = 4000)
    public void testFormatCustomBufferAndFieldPosition() {
        ProperFractionFormat format = new ProperFractionFormat();
        StringBuffer sb = new StringBuffer("Prefix: ");
        FieldPosition pos = new FieldPosition(0);
        StringBuffer returned = format.format(new Fraction(5, 4), sb, pos);

        assertSame(sb, returned);
        assertEquals("Prefix: 1 1 / 4", returned.toString());
        assertEquals(0, pos.getBeginIndex());
        assertEquals(0, pos.getEndIndex());
    }

    @Test(timeout = 4000)
    public void testParseStandardProperFraction() throws ParseException {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction fraction = format.parse("1 1/2");
        assertNotNull(fraction);
        assertEquals(3, fraction.getNumerator());
        assertEquals(2, fraction.getDenominator());
    }

    @Test(timeout = 4000)
    public void testParseNegativeWholeProperFraction() throws ParseException {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction fraction = format.parse("-1 1/2");
        assertNotNull(fraction);
        assertEquals(-3, fraction.getNumerator());
        assertEquals(2, fraction.getDenominator());
    }

    @Test(timeout = 4000)
    public void testParseFallbackToImproperFraction() throws ParseException {
        ProperFractionFormat format = new ProperFractionFormat();
        // Superclass improper parsing handles single fraction without whole part
        Fraction fraction = format.parse("3 / 4");
        assertNotNull(fraction);
        assertEquals(3, fraction.getNumerator());
        assertEquals(4, fraction.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetAndSetWholeFormat() {
        NumberFormat nf1 = NumberFormat.getIntegerInstance(Locale.US);
        NumberFormat nf2 = NumberFormat.getIntegerInstance(Locale.GERMANY);
        ProperFractionFormat format = new ProperFractionFormat(nf1);

        assertSame(nf1, format.getWholeFormat());
        format.setWholeFormat(nf2);
        assertSame(nf2, format.getWholeFormat());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatIntegerValues() {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction fraction = new Fraction(4, 1);
        String result = format.format(fraction);
        assertEquals("4 0 / 1", result);
    }

    @Test(timeout = 4000)
    public void testFormatZero() {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction fraction = new Fraction(0, 1);
        String result = format.format(fraction);
        assertEquals("0 / 1", result);
    }

    @Test(timeout = 4000)
    public void testParseWithExtensiveWhitespace() throws ParseException {
        ProperFractionFormat format = new ProperFractionFormat();
        Fraction fraction = format.parse("   2    3   /   4   ");
        assertNotNull(fraction);
        assertEquals(11, fraction.getNumerator());
        assertEquals(4, fraction.getDenominator());
    }

    @Test(timeout = 4000)
    public void testParseTerminatingWithoutSlash() {
        ProperFractionFormat format = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        // Switch case 0: when char is 0 (end of string without delimiter)
        // Whole = 1, num = 2, returns Fraction(2, 1)
        Fraction fraction = format.parse("1 2", pos);
        assertNotNull(fraction);
        assertEquals(2, fraction.getNumerator());
        assertEquals(1, fraction.getDenominator());
        assertEquals(3, pos.getIndex());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseProperInvalidMinusNumerator() {
        ProperFractionFormat format = new ProperFractionFormat();
        String source = "2 -2/3";
        try {
            format.parse(source);
            fail("Expected ParseException when parsing negative numerator in proper fraction: " + source);
        } catch (ParseException expected) {
            // Success: minus sign in numerator part must be rejected
        }
    }

    @Test(timeout = 4000)
    public void testParseProperInvalidMinusDenominator() {
        ProperFractionFormat format = new ProperFractionFormat();
        String source = "2 2/-3";
        try {
            format.parse(source);
            fail("Expected ParseException when parsing negative denominator in proper fraction: " + source);
        } catch (ParseException expected) {
            // Success: minus sign in denominator part must be rejected
        }
    }

    @Test(timeout = 4000)
    public void testParseProperInvalidMinusBothNumeratorAndDenominator() {
        ProperFractionFormat format = new ProperFractionFormat();
        String source = "2 -2/-3";
        try {
            format.parse(source);
            fail("Expected ParseException for proper fraction with negative numerator and denominator");
        } catch (ParseException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseProperInvalidMinusWithNegativeWhole() {
        ProperFractionFormat format = new ProperFractionFormat();
        String source1 = "-2 -2/3";
        try {
            format.parse(source1);
            fail("Expected ParseException for proper fraction with negative whole and negative numerator: " + source1);
        } catch (ParseException expected) {
            // Expected
        }

        String source2 = "-2 2/-3";
        try {
            format.parse(source2);
            fail("Expected ParseException for proper fraction with negative whole and negative denominator: " + source2);
        } catch (ParseException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseProperInvalidMinusWithParsePosition() {
        ProperFractionFormat format = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f1 = format.parse("2 -2/3", pos);
        assertNull("Parsing '2 -2/3' should return null for invalid minus in numerator", f1);
        assertEquals(0, pos.getIndex());

        pos = new ParsePosition(0);
        Fraction f2 = format.parse("2 2/-3", pos);
        assertNull("Parsing '2 2/-3' should return null for invalid minus in denominator", f2);
        assertEquals(0, pos.getIndex());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetWholeFormatNull() {
        ProperFractionFormat format = new ProperFractionFormat();
        format.setWholeFormat(null);
    }

    @Test(timeout = 4000)
    public void testParseInvalidWholeNumber() {
        ProperFractionFormat format = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction fraction = format.parse("invalid 1/2", pos);
        assertNull(fraction);
        assertEquals(0, pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseInvalidNumeratorNumber() {
        ProperFractionFormat format = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction fraction = format.parse("1 invalid/2", pos);
        assertNull(fraction);
        assertEquals(0, pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseInvalidDenominatorNumber() {
        ProperFractionFormat format = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction fraction = format.parse("1 1/invalid", pos);
        assertNull(fraction);
        assertEquals(0, pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseInvalidSeparatorCharacter() {
        ProperFractionFormat format = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        // Switch default branch: neither 0 nor '/'
        Fraction fraction = format.parse("1 2 : 3", pos);
        assertNull(fraction);
        assertEquals(0, pos.getIndex());
        assertTrue(pos.getErrorIndex() > 0);
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseEmptyStringThrowsParseException() throws ParseException {
        ProperFractionFormat format = new ProperFractionFormat();
        format.parse("");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseUnparseableStringThrowsParseException() throws ParseException {
        ProperFractionFormat format = new ProperFractionFormat();
        format.parse("not a fraction at all");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testThreeArgumentConstructor() {
        NumberFormat wholeFormat = NumberFormat.getIntegerInstance(Locale.US);
        NumberFormat numFormat = NumberFormat.getIntegerInstance(Locale.FRENCH);
        NumberFormat denFormat = NumberFormat.getIntegerInstance(Locale.GERMAN);

        ProperFractionFormat format = new ProperFractionFormat(wholeFormat, numFormat, denFormat);
        assertSame(wholeFormat, format.getWholeFormat());
        assertSame(numFormat, format.getNumeratorFormat());
        assertSame(denFormat, format.getDenominatorFormat());
    }

    @Test(timeout = 4000)
    public void testSingleArgumentConstructorCloning() {
        NumberFormat baseFormat = NumberFormat.getIntegerInstance(Locale.US);
        ProperFractionFormat format = new ProperFractionFormat(baseFormat);

        assertSame(baseFormat, format.getWholeFormat());
        assertNotSame(baseFormat, format.getNumeratorFormat());
        assertNotSame(baseFormat, format.getDenominatorFormat());
        assertNotSame(format.getNumeratorFormat(), format.getDenominatorFormat());
    }

    @Test(timeout = 4000)
    public void testDefaultConstructorNotNull() {
        ProperFractionFormat format = new ProperFractionFormat();
        assertNotNull(format.getWholeFormat());
        assertNotNull(format.getNumeratorFormat());
        assertNotNull(format.getDenominatorFormat());
    }
}