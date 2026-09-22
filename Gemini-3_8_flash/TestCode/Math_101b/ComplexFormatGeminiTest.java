package org.apache.commons.math.complex;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Branch / Condition                                | Targeted Method / Test Case
 * -------------------------------------------------------------------------------------------------------
 * [DEFECT] Missing imaginary character (OOB bounds) | testForgottenImaginaryCharacter, testForgottenImaginaryCharacterParsePosition
 * Format: Real only (im == 0.0)                     | testFormatRealOnly
 * Format: Negative imaginary (im < 0.0)             | testFormatNegativeImaginary
 * Format: Positive imaginary (im > 0.0)             | testFormatPositiveImaginary
 * Format: NaN imaginary (Double.isNaN(im))          | testFormatNanImaginary
 * Format: Infinite real/imaginary (Double.isInfinite)| testFormatInfinity
 * Format: Object types (Complex, Number, Invalid)   | testFormatObjectTypes, testFormatInvalidObjectType
 * Format: Static helper formatComplex               | testStaticFormatComplex
 * Parse: Valid standard complex (positive / negative)| testParseStandard
 * Parse: Real-only input ("1.23", c == 0)           | testParseRealOnly
 * Parse: Special numbers (NaN, Infinity, -Infinity) | testParseSpecialDoubleValues
 * Parse: Whitespace handling                        | testParseWithVariousWhitespaces
 * Parse: Invalid real part                          | testParseInvalidRealPart
 * Parse: Invalid sign ('?' instead of '+' / '-')    | testParseInvalidSign
 * Parse: Invalid imaginary part                     | testParseInvalidImaginaryPart
 * Parse: Custom imaginary character ('j')           | testCustomImaginaryCharacter
 * Parse: parseObject invocation                     | testParseObject
 * Constructors & Getters/Setters validation         | testConstructorsAndPropertyGuards
 * Validation: null/empty imaginary character        | testInvalidImaginaryCharacter
 * Validation: null formats (real/imaginary)         | testInvalidNumberFormats
 * Static Locale checks                              | testLocales
 * Serialization & Deserialization                   | testSerialization
 * -------------------------------------------------------------------------------------------------------
 */
public class ComplexFormatGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
    // =========================================================================

    /**
     * Target Defects4J known issue: StringIndexOutOfBoundsException when parsing
     * an input string that is missing the trailing imaginary character (e.g., "1 + 1").
     * The implementation attempts source.substring(startIndex, startIndex + n) without
     * verifying if startIndex + n <= source.length().
     */
    @Test(expected = ParseException.class, timeout = 4000)
    public void testForgottenImaginaryCharacter() throws ParseException {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        // "1 + 1" has no trailing 'i'; should cleanly fail parsing with ParseException
        format.parse("1 + 1");
    }

    /**
     * Target Defects4J known issue using ParsePosition directly to verify that
     * null is returned and error index is assigned rather than an unchecked exception.
     */
    @Test(timeout = 4000)
    public void testForgottenImaginaryCharacterParsePosition() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        ParsePosition pos = new ParsePosition(0);
        Complex result = format.parse("1 + 2", pos);
        assertNull("Parsing without imaginary character must yield null", result);
        assertTrue("Error index must be set when imaginary character is omitted", pos.getErrorIndex() >= 0);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatPositiveImaginary() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        Complex c = new Complex(1.234, 5.678);
        String formatted = format.format(c);
        assertEquals("1.23 + 5.68i", formatted);
    }

    @Test(timeout = 4000)
    public void testFormatNegativeImaginary() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        Complex c = new Complex(2.5, -3.75);
        String formatted = format.format(c);
        assertEquals("2.5 - 3.75i", formatted);
    }

    @Test(timeout = 4000)
    public void testFormatRealOnly() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        Complex c = new Complex(4.0, 0.0);
        String formatted = format.format(c);
        assertEquals("4", formatted);
    }

    @Test(timeout = 4000)
    public void testFormatNanImaginary() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        Complex c = new Complex(1.0, Double.NaN);
        String formatted = format.format(c);
        assertEquals("1 + (NaN)i", formatted);
    }

    @Test(timeout = 4000)
    public void testFormatInfinity() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        Complex posInf = new Complex(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertEquals("(Infinity) - (Infinity)i", format.format(posInf));

        Complex negInfReal = new Complex(Double.NEGATIVE_INFINITY, 2.0);
        assertEquals("(-Infinity) + 2i", format.format(negInfReal));
    }

    @Test(timeout = 4000)
    public void testFormatObjectTypes() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        StringBuffer buffer = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);

        // Test with java.lang.Number (e.g. Double)
        format.format(Double.valueOf(3.14), buffer, pos);
        assertEquals("3.14", buffer.toString());

        // Test with Complex
        buffer = new StringBuffer();
        format.format(new Complex(1.0, 2.0), buffer, pos);
        assertEquals("1 + 2i", buffer.toString());
    }

    @Test(timeout = 4000)
    public void testStaticFormatComplex() {
        Complex c = new Complex(1.0, 1.0);
        String res = ComplexFormat.formatComplex(c);
        assertNotNull(res);
        assertTrue(res.contains("1") && res.contains("i"));
    }

    @Test(timeout = 4000)
    public void testParseStandard() throws ParseException {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);

        Complex parsed1 = format.parse("1.5 + 2.5i");
        assertEquals(1.5, parsed1.getReal(), 1e-9);
        assertEquals(2.5, parsed1.getImaginary(), 1e-9);

        Complex parsed2 = format.parse("1.5 - 2.5i");
        assertEquals(1.5, parsed2.getReal(), 1e-9);
        assertEquals(-2.5, parsed2.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testParseRealOnly() throws ParseException {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        Complex parsed = format.parse("42");
        assertEquals(42.0, parsed.getReal(), 1e-9);
        assertEquals(0.0, parsed.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testParseWithVariousWhitespaces() throws ParseException {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        Complex parsed = format.parse("   3   +   4i   ");
        assertEquals(3.0, parsed.getReal(), 1e-9);
        assertEquals(4.0, parsed.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testCustomImaginaryCharacter() throws ParseException {
        ComplexFormat format = new ComplexFormat("j");
        assertEquals("j", format.getImaginaryCharacter());

        String formatted = format.format(new Complex(1.0, 2.0));
        assertTrue(formatted.endsWith("2j"));

        Complex parsed = format.parse("1 + 2j");
        assertEquals(1.0, parsed.getReal(), 1e-9);
        assertEquals(2.0, parsed.getImaginary(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testParseObject() throws ParseException {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        ParsePosition pos = new ParsePosition(0);
        Object obj = format.parseObject("5 - 6i", pos);
        assertTrue(obj instanceof Complex);
        Complex c = (Complex) obj;
        assertEquals(5.0, c.getReal(), 1e-9);
        assertEquals(-6.0, c.getImaginary(), 1e-9);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseSpecialDoubleValues() throws ParseException {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);

        Complex cNan = format.parse("(NaN) + (NaN)i");
        assertTrue(Double.isNaN(cNan.getReal()));
        assertTrue(Double.isNaN(cNan.getImaginary()));

        Complex cPosInf = format.parse("(Infinity) + (Infinity)i");
        assertEquals(Double.POSITIVE_INFINITY, cPosInf.getReal(), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, cPosInf.getImaginary(), 0.0);

        Complex cNegInf = format.parse("(-Infinity) - (Infinity)i");
        assertEquals(Double.NEGATIVE_INFINITY, cNegInf.getReal(), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, cNegInf.getImaginary(), 0.0);
    }

    @Test(timeout = 4000)
    public void testLocales() {
        Locale[] locales = ComplexFormat.getAvailableLocales();
        assertNotNull(locales);
        assertTrue(locales.length > 0);

        ComplexFormat frenchFormat = ComplexFormat.getInstance(Locale.FRENCH);
        assertNotNull(frenchFormat);
        assertEquals("i", frenchFormat.getImaginaryCharacter());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatInvalidObjectType() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        format.format("InvalidObjectPayload", new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidImaginaryCharacterNull() {
        new ComplexFormat((String) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidImaginaryCharacterEmpty() {
        new ComplexFormat("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetInvalidImaginaryCharacterNull() {
        ComplexFormat format = new ComplexFormat();
        format.setImaginaryCharacter(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetInvalidImaginaryCharacterEmpty() {
        ComplexFormat format = new ComplexFormat();
        format.setImaginaryCharacter("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetInvalidImaginaryFormatNull() {
        ComplexFormat format = new ComplexFormat();
        format.setImaginaryFormat(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetInvalidRealFormatNull() {
        ComplexFormat format = new ComplexFormat();
        format.setRealFormat(null);
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseInvalidRealPart() throws ParseException {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        format.parse("INVALID + 2i");
    }

    @Test(timeout = 4000)
    public void testParseInvalidSign() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        ParsePosition pos = new ParsePosition(0);
        Complex result = format.parse("1.0 * 2.0i", pos);
        assertNull("Invalid sign must result in null parse", result);
        assertEquals(0, pos.getIndex());
        assertEquals(4, pos.getErrorIndex());
    }

    @Test(timeout = 4000)
    public void testParseInvalidImaginaryPart() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        ParsePosition pos = new ParsePosition(0);
        Complex result = format.parse("1.0 + INVALIDi", pos);
        assertNull("Invalid imaginary number must return null", result);
        assertEquals(0, pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseWrongImaginaryCharacter() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        ParsePosition pos = new ParsePosition(0);
        Complex result = format.parse("1.0 + 2.0j", pos);
        assertNull("Mismatched imaginary character must return null", result);
        assertEquals(0, pos.getIndex());
        assertEquals(10, pos.getErrorIndex());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndPropertyGuards() {
        NumberFormat nfReal = NumberFormat.getInstance(Locale.US);
        NumberFormat nfImag = NumberFormat.getInstance(Locale.US);

        ComplexFormat cf1 = new ComplexFormat();
        assertEquals("i", cf1.getImaginaryCharacter());
        assertNotNull(cf1.getRealFormat());
        assertNotNull(cf1.getImaginaryFormat());

        ComplexFormat cf2 = new ComplexFormat(nfReal);
        assertSame(nfReal, cf2.getRealFormat());

        ComplexFormat cf3 = new ComplexFormat(nfReal, nfImag);
        assertSame(nfReal, cf3.getRealFormat());
        assertSame(nfImag, cf3.getImaginaryFormat());

        ComplexFormat cf4 = new ComplexFormat("k");
        assertEquals("k", cf4.getImaginaryCharacter());

        ComplexFormat cf5 = new ComplexFormat("m", nfReal);
        assertEquals("m", cf5.getImaginaryCharacter());
        assertSame(nfReal, cf5.getRealFormat());
        assertNotSame(nfReal, cf5.getImaginaryFormat()); // cloned instance

        ComplexFormat cf6 = new ComplexFormat("z", nfReal, nfImag);
        assertEquals("z", cf6.getImaginaryCharacter());
        assertSame(nfReal, cf6.getRealFormat());
        assertSame(nfImag, cf6.getImaginaryFormat());
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        format.setImaginaryCharacter("j");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(format);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            ComplexFormat deserialized = (ComplexFormat) ois.readObject();
            assertEquals(format.getImaginaryCharacter(), deserialized.getImaginaryCharacter());
            assertEquals(format.format(new Complex(2.0, 3.0)), deserialized.format(new Complex(2.0, 3.0)));
        }
    }
}