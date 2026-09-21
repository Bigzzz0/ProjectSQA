package org.apache.commons.math.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * Comprehensive JUnit 4 test suite for ProperFractionFormat.
 * Targets maximum line/branch coverage and the known defect related to
 * invalid minus signs in proper fraction parsing (Defects4J bug).
 *
 * [Branch & Defect Analysis Matrix]
 * - Format branches: whole != 0 vs whole == 0; Math.abs(num) used only when whole != 0.
 * - Parse branches: super.parse success/failure; whole parse null; numerator parse null;
 *   char after numerator: 0, '/', default; denominator parse null.
 * - Defect condition: minus sign in numerator or denominator (should be invalid).
 * - Boundary: zero whole, zero numerator, zero denominator, negative numbers, MAX_INT.
 * - Exception: setWholeFormat(null) throws IllegalArgumentException.
 */
public class ProperFractionFormatDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFormatPositiveProper() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        Fraction f = new Fraction(3, 2);  // 1 1/2
        StringBuffer sb = new StringBuffer();
        StringBuffer result = fmt.format(f, sb, new java.text.FieldPosition(0));
        assertEquals("1 1 / 2", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatNegativeProper() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        Fraction f = new Fraction(-3, 2); // -1 1/2
        StringBuffer sb = new StringBuffer();
        StringBuffer result = fmt.format(f, sb, new java.text.FieldPosition(0));
        assertEquals("-1 1 / 2", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatNoWholePart() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        Fraction f = new Fraction(1, 2); // whole=0
        StringBuffer sb = new StringBuffer();
        StringBuffer result = fmt.format(f, sb, new java.text.FieldPosition(0));
        assertEquals("1 / 2", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatWholeOnly() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        Fraction f = new Fraction(4, 2); // whole=2, num=0
        StringBuffer sb = new StringBuffer();
        StringBuffer result = fmt.format(f, sb, new java.text.FieldPosition(0));
        assertEquals("2 0 / 2", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatNegativeNoWhole() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        Fraction f = new Fraction(-1, 2); // whole=0, negative fraction
        StringBuffer sb = new StringBuffer();
        StringBuffer result = fmt.format(f, sb, new java.text.FieldPosition(0));
        assertEquals("-1 / 2", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatZero() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        Fraction f = new Fraction(0, 1);
        StringBuffer sb = new StringBuffer();
        StringBuffer result = fmt.format(f, sb, new java.text.FieldPosition(0));
        assertEquals("0 / 1", result.toString());
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFormatIntegerMax() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        Fraction f = new Fraction(Integer.MAX_VALUE, 1);
        StringBuffer sb = new StringBuffer();
        StringBuffer result = fmt.format(f, sb, new java.text.FieldPosition(0));
        assertTrue(result.toString().contains("2147483647"));
    }

    @Test(timeout = 4000)
    public void testParseProperSimple() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = fmt.parse("1 1/2", pos);
        assertNotNull(f);
        assertEquals(3, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testParseNegativeWhole() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = fmt.parse("-1 1/2", pos);
        assertNotNull(f);
        assertEquals(-3, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testParseNoDenominator() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = fmt.parse("3", pos);
        assertNotNull(f);
        assertEquals(3, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testParseImproperFraction() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = fmt.parse("3/2", pos);
        assertNotNull(f);
        assertEquals(3, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testParseWithWhitespace() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = fmt.parse("  1   1 / 2  ", pos);
        assertNotNull(f);
        assertEquals(3, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testParseEmptyString() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = fmt.parse("", pos);
        assertNull(f);
        assertTrue(pos.getErrorIndex() >= 0 || pos.getIndex() == 0);
    }

    @Test(timeout = 4000)
    public void testParseNull() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        try {
            fmt.parse(null, new ParsePosition(0));
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseInvalidCharsAfterWhole() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = fmt.parse("1abc", pos);
        assertNull(f);
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone
    // -----------------------------------------------------------------------

    /**
     * Known Defects4J defect: invalid minus signs in proper fraction.
     * "-1 -1/2" should be invalid (minus in numerator).
     */
    @Test(timeout = 4000)
    public void testParseInvalidMinusInNumerator() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = fmt.parse("-1 -1/2", pos);
        assertNull("Parsing should fail due to invalid minus in numerator", f);
        // error index should be at the minus sign location (after whole)
        assertTrue("Error index should be set", pos.getErrorIndex() >= 0);
        // index should be reset to initial
        assertEquals(0, pos.getIndex());
    }

    /**
     * Invalid minus in denominator: "1 1/-2" should fail.
     */
    @Test(timeout = 4000)
    public void testParseInvalidMinusInDenominator() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = fmt.parse("1 1/-2", pos);
        assertNull("Parsing should fail due to invalid minus in denominator", f);
        assertTrue("Error index should be set", pos.getErrorIndex() >= 0);
        assertEquals(0, pos.getIndex());
    }

    /**
     * Invalid minus only in numerator (without whole): "-1/2" is actually an
     * improper fraction; super.parse should handle it. But "-1 / -2" should fail.
     */
    @Test(timeout = 4000)
    public void testParseDoubleMinus() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        ParsePosition pos = new ParsePosition(0);
        Fraction f = fmt.parse("-1 / -2", pos);
        assertNull("Parsing should fail due to minus in denominator", f);
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetWholeFormatNull() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        fmt.setWholeFormat(null);
    }

    @Test(timeout = 4000)
    public void testConstructorNullFormat() {
        // single argument constructor with null should throw NPE from clone?
        try {
            new ProperFractionFormat(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetWholeFormat() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        assertNotNull(fmt.getWholeFormat());
    }

    @Test(timeout = 4000)
    public void testSetAndGetWholeFormat() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        NumberFormat nf = NumberFormat.getInstance();
        fmt.setWholeFormat(nf);
        assertSame(nf, fmt.getWholeFormat());
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        ProperFractionFormat fmt1 = new ProperFractionFormat();
        ProperFractionFormat fmt2 = new ProperFractionFormat();
        assertEquals(fmt1, fmt2);
        assertEquals(fmt1.hashCode(), fmt2.hashCode());

        // modify whole format
        NumberFormat custom = NumberFormat.getInstance();
        custom.setMinimumFractionDigits(2);
        fmt2.setWholeFormat(custom);
        assertFalse(fmt1.equals(fmt2));
    }

    @Test(timeout = 4000)
    public void testToString() {
        ProperFractionFormat fmt = new ProperFractionFormat();
        assertNotNull(fmt.toString());
    }
}