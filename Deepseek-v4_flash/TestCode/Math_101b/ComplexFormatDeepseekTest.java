package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * Comprehensive JUnit 4 test suite for ComplexFormat.
 * Targets line and branch coverage, including the known defect
 * (StringIndexOutOfBoundsException when imaginary character is missing).
 */
public class ComplexFormatDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     *
     * Partitions & Decision Points:
     * 1. Constructors: default, with NumberFormat, with two NumberFormat, with String, with String+NumberFormat, with String+two NumberFormat
     * 2. Get/Set imaginaryCharacter, imaginaryFormat, realFormat (incl. null/empty validation)
     * 3. format(Complex, StringBuffer, FieldPosition):
     *    - real/imaginary sign branching: im < 0, im > 0, im == 0, im is NaN
     *    - formatDouble wrapper for NaN/infinity cases
     * 4. format(Object, StringBuffer, FieldPosition): Complex vs Number vs invalid type
     * 5. parse(String) and parse(String, ParsePosition):
     *    - valid complex: "Re + Imi", "Re - Imi", "Re" (no imaginary)
     *    - whitespace handling, sign parsing, imaginary character matching
     *    - special number parsing: NaN, Infinity
     *    - error paths: invalid real, invalid sign, invalid imaginary, missing imaginary character
     * 6. Static methods: formatComplex, getInstance, getInstance(Locale), getAvailableLocales
     * 7. Edge: imaginaryCharacter as "j", empty string (should throw), null (should throw)
     * 8. Boundary: zero imaginary, negative imaginary, very large/small values, Double.MIN_VALUE, Double.MAX_VALUE
     * 
     * Defect trigger (Defects4J):
     *   parse("1 + 2") missing imaginary character -> expects ParseException,
     *   but defective version throws StringIndexOutOfBoundsException.
     */

    // ================ Partition A: Core Functional Logic & State Transitions ================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        ComplexFormat f = new ComplexFormat();
        assertEquals("i", f.getImaginaryCharacter());
        assertNotNull(f.getRealFormat());
        assertNotNull(f.getImaginaryFormat());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNumberFormat() {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        ComplexFormat f = new ComplexFormat(nf);
        assertEquals("i", f.getImaginaryCharacter());
        assertSame(nf, f.getRealFormat());
        // Imaginary format is a clone of the given format
        assertNotSame(nf, f.getImaginaryFormat());
        assertEquals(nf.format(1.23), f.getImaginaryFormat().format(1.23));
    }

    @Test(timeout = 4000)
    public void testConstructorWithTwoNumberFormats() {
        NumberFormat real = NumberFormat.getInstance(Locale.US);
        NumberFormat imag = NumberFormat.getInstance(Locale.FRANCE);
        ComplexFormat f = new ComplexFormat(real, imag);
        assertEquals("i", f.getImaginaryCharacter());
        assertSame(real, f.getRealFormat());
        assertSame(imag, f.getImaginaryFormat());
    }

    @Test(timeout = 4000)
    public void testConstructorWithImaginaryCharacter() {
        ComplexFormat f = new ComplexFormat("j");
        assertEquals("j", f.getImaginaryCharacter());
    }

    @Test(timeout = 4000)
    public void testConstructorWithImaginaryCharacterAndFormat() {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        ComplexFormat f = new ComplexFormat("j", nf);
        assertEquals("j", f.getImaginaryCharacter());
        assertSame(nf, f.getRealFormat());
        assertNotSame(nf, f.getImaginaryFormat());
    }

    @Test(timeout = 4000)
    public void testConstructorWithAllParams() {
        NumberFormat real = NumberFormat.getInstance(Locale.US);
        NumberFormat imag = NumberFormat.getInstance(Locale.UK);
        ComplexFormat f = new ComplexFormat("j", real, imag);
        assertEquals("j", f.getImaginaryCharacter());
        assertSame(real, f.getRealFormat());
        assertSame(imag, f.getImaginaryFormat());
    }

    @Test(timeout = 4000)
    public void testSettersAndGetters() {
        ComplexFormat f = new ComplexFormat();
        f.setImaginaryCharacter("k");
        assertEquals("k", f.getImaginaryCharacter());

        NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
        f.setImaginaryFormat(nf);
        assertSame(nf, f.getImaginaryFormat());

        NumberFormat nf2 = NumberFormat.getInstance(Locale.CHINA);
        f.setRealFormat(nf2);
        assertSame(nf2, f.getRealFormat());
    }

    // ================ Partition B: Boundary Value Analysis & Extremes ================

    @Test(timeout = 4000)
    public void testFormatComplexPositiveImaginary() {
        ComplexFormat f = new ComplexFormat();
        Complex c = new Complex(3.5, 2.1);
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = f.format(c, sb, pos);
        assertEquals("3.5 + 2.1i", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatComplexNegativeImaginary() {
        ComplexFormat f = new ComplexFormat();
        Complex c = new Complex(1.0, -4.5);
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = f.format(c, sb, pos);
        assertEquals("1 - 4.5i", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatComplexZeroImaginary() {
        ComplexFormat f = new ComplexFormat();
        Complex c = new Complex(2.0, 0.0);
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = f.format(c, sb, pos);
        assertEquals("2", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatComplexNaNImaginary() {
        ComplexFormat f = new ComplexFormat();
        Complex c = new Complex(1.0, Double.NaN);
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = f.format(c, sb, pos);
        // NaN imaginary should be formatted as " + NaN i" (since >0 false, isNaN true)
        assertEquals("1 + (NaN)i", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatComplexRealNaN() {
        ComplexFormat f = new ComplexFormat();
        Complex c = new Complex(Double.NaN, 2.0);
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = f.format(c, sb, pos);
        assertEquals("(NaN) + 2i", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatComplexInfinity() {
        ComplexFormat f = new ComplexFormat();
        Complex c = new Complex(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = f.format(c, sb, pos);
        assertEquals("(Infinity) - (Infinity)i", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatObjectComplex() {
        ComplexFormat f = new ComplexFormat();
        Complex c = new Complex(2, 3);
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = f.format((Object) c, sb, pos);
        assertEquals("2 + 3i", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatObjectNumber() {
        ComplexFormat f = new ComplexFormat();
        Number num = 5.5;
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = f.format((Object) num, sb, pos);
        assertEquals("5.5", result.toString());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFormatObjectInvalidType() {
        ComplexFormat f = new ComplexFormat();
        f.format("not a complex", new StringBuffer(), new FieldPosition(0));
    }

    // ================ Partition C: Defect-Targeted Branch Zone ================

    /**
     * Directly targets the known Defects4J defect:
     * Parsing a string like "1 + 2" (missing imaginary character) should throw ParseException,
     * but the defective version throws StringIndexOutOfBoundsException.
     * This test expects ParseException, so on the defective version it will fail,
     * thus revealing the bug.
     */
    @Test(expected = ParseException.class, timeout = 4000)
    public void testForgottenImaginaryCharacter() throws Exception {
        ComplexFormat format = new ComplexFormat();
        format.parse("1 + 2");
    }

    // Additional edge cases for parse defect
    @Test(timeout = 4000)
    public void testParseMissingImaginaryCharacterWithMinus() throws Exception {
        ComplexFormat format = new ComplexFormat();
        try {
            format.parse("1 - 2");
            fail("Expected ParseException for missing imaginary character");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseMissingImaginaryCharacterWithNoSign() throws Exception {
        ComplexFormat format = new ComplexFormat();
        // "1 2i" should not parse normally but missing sign? Actually it's invalid.
        try {
            format.parse("1 2i");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // expected
        }
    }

    // ================ Partition D: Exception & Defensive Guard Paths ================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetImaginaryCharacterNull() {
        ComplexFormat f = new ComplexFormat();
        f.setImaginaryCharacter(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetImaginaryCharacterEmpty() {
        ComplexFormat f = new ComplexFormat();
        f.setImaginaryCharacter("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetImaginaryFormatNull() {
        ComplexFormat f = new ComplexFormat();
        f.setImaginaryFormat(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetRealFormatNull() {
        ComplexFormat f = new ComplexFormat();
        f.setRealFormat(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorImaginaryCharacterNull() {
        new ComplexFormat((String) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorImaginaryCharacterEmpty() {
        new ComplexFormat("");
    }

    @Test(timeout = 4000)
    public void testParseInvalidReal() throws Exception {
        ComplexFormat f = new ComplexFormat();
        try {
            f.parse("abc + 2i");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseInvalidSign() throws Exception {
        ComplexFormat f = new ComplexFormat();
        try {
            f.parse("1 * 2i");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseInvalidImaginaryNumber() throws Exception {
        ComplexFormat f = new ComplexFormat();
        try {
            f.parse("1 + abi");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseSpecialNumbers() throws Exception {
        ComplexFormat f = new ComplexFormat();
        Complex c = f.parse("(NaN) + (Infinity)i");
        assertTrue(Double.isNaN(c.getReal()));
        assertTrue(Double.isInfinite(c.getImaginary()));
        assertEquals(Double.POSITIVE_INFINITY, c.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testParseNegInfinity() throws Exception {
        ComplexFormat f = new ComplexFormat();
        Complex c = f.parse("(-Infinity) - (NaN)i");
        assertTrue(Double.isInfinite(c.getReal()));
        assertTrue(c.getReal() < 0);
        assertTrue(Double.isNaN(c.getImaginary()));
    }

    @Test(timeout = 4000)
    public void testParseOnlyReal() throws Exception {
        ComplexFormat f = new ComplexFormat();
        Complex c = f.parse("42.5");
        assertEquals(42.5, c.getReal(), 0.0);
        assertEquals(0.0, c.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testParseWithWhitespace() throws Exception {
        ComplexFormat f = new ComplexFormat();
        Complex c = f.parse("  3.0  -  4.0i  ");
        assertEquals(3.0, c.getReal(), 0.0);
        assertEquals(-4.0, c.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testParseWithCustomImaginaryCharacter() throws Exception {
        ComplexFormat f = new ComplexFormat("j");
        Complex c = f.parse("1.5 + 2.3j");
        assertEquals(1.5, c.getReal(), 0.0);
        assertEquals(2.3, c.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testParsePositions() {
        ComplexFormat f = new ComplexFormat();
        ParsePosition pos = new ParsePosition(0);
        Complex c = f.parse("1 + 2i", pos);
        assertNotNull(c);
        assertEquals(1.0, c.getReal(), 0.0);
        assertEquals(2.0, c.getImaginary(), 0.0);
        assertEquals(6, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    @Test(timeout = 4000)
    public void testParsePositionReturnsNullOnError() {
        ComplexFormat f = new ComplexFormat();
        ParsePosition pos = new ParsePosition(0);
        Complex c = f.parse("xyz", pos);
        assertNull(c);
        assertTrue(pos.getIndex() == 0);
        assertTrue(pos.getErrorIndex() > 0);
    }

    @Test(timeout = 4000)
    public void testObjectContractParseObject() {
        ComplexFormat f = new ComplexFormat();
        ParsePosition pos = new ParsePosition(0);
        Object obj = f.parseObject("2 - 3i", pos);
        assertTrue(obj instanceof Complex);
        Complex c = (Complex) obj;
        assertEquals(2.0, c.getReal(), 0.0);
        assertEquals(-3.0, c.getImaginary(), 0.0);
    }

    // ================ Partition E: Object Lifecycle & Contract Integrity ================

    @Test(timeout = 4000)
    public void testStaticFormatComplex() {
        Complex c = new Complex(1.0, 2.0);
        String result = ComplexFormat.formatComplex(c);
        assertEquals("1 + 2i", result);
    }

    @Test(timeout = 4000)
    public void testGetInstance() {
        ComplexFormat f = ComplexFormat.getInstance();
        assertNotNull(f);
        assertEquals("i", f.getImaginaryCharacter());
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithLocale() {
        ComplexFormat f = ComplexFormat.getInstance(Locale.FRANCE);
        assertNotNull(f);
        // Verify decimal separator is comma for French locale
        assertTrue(f.getRealFormat().format(1.5).contains(","));
    }

    @Test(timeout = 4000)
    public void testGetAvailableLocales() {
        Locale[] locales = ComplexFormat.getAvailableLocales();
        assertNotNull(locales);
        assertTrue(locales.length > 0);
    }

    @Test(timeout = 4000)
    public void testFormatDoubleNaN() throws Exception {
        ComplexFormat f = new ComplexFormat();
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        // via formatDouble private method tested indirectly through format
        Complex c = new Complex(Double.NaN, 0);
        f.format(c, sb, pos);
        assertEquals("(NaN)", sb.toString());
    }

    @Test(timeout = 4000)
    public void testFormatDoubleInfinity() throws Exception {
        ComplexFormat f = new ComplexFormat();
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        Complex c = new Complex(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        f.format(c, sb, pos);
        assertEquals("(Infinity) - (Infinity)i", sb.toString());
    }

    @Test(timeout = 4000)
    public void testParseOnlyRealWithSign() throws Exception {
        // Case where after real there is a sign but no imaginary number - should fail
        ComplexFormat f = new ComplexFormat();
        try {
            f.parse("1 +");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseNumberWithSpecialFallback() throws Exception {
        // Indirectly test parseNumber when format.parse fails and special numbers are tried
        ComplexFormat f = new ComplexFormat();
        Complex c = f.parse("(Infinity)");
        assertTrue(Double.isInfinite(c.getReal()));
        assertTrue(c.getReal() > 0);
    }

    @Test(timeout = 4000)
    public void testParseEmptyString() throws Exception {
        ComplexFormat f = new ComplexFormat();
        try {
            f.parse("");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseNegativeZeroImaginary() throws Exception {
        ComplexFormat f = new ComplexFormat();
        Complex c = f.parse("0 - 0i");
        assertEquals(0.0, c.getReal(), 0.0);
        // Negative zero? We'll just check value
        assertEquals(0.0, c.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testParseWithMultipleSpaces() throws Exception {
        ComplexFormat f = new ComplexFormat();
        Complex c = f.parse("  1.5   +   2.5i   ");
        assertEquals(1.5, c.getReal(), 0.0);
        assertEquals(2.5, c.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFormatNegativeImaginaryWithCustomChar() {
        ComplexFormat f = new ComplexFormat("j");
        Complex c = new Complex(1.0, -2.0);
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        f.format(c, sb, pos);
        assertEquals("1 - 2j", sb.toString());
    }
}