package com.fasterxml.jackson.core.json;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: com.fasterxml.jackson.core.json.ReaderBasedJsonParser
 * TARGET DEFECT: TestNumericValues::testLongerFloatingPoint -> ArrayIndexOutOfBoundsException: 200
 *
 * KEY BRANCHES & PATHWAYS TARGETED:
 * 1. DEFECT ZONE (Partition C):
 *    - Long floating-point numbers crossing buffer boundaries (_parsePosNumber, _parseFloat, _parseNumber2).
 *    - Boundary conditions when `outPtr >= outBuf.length` before appending '.' or fractional digits.
 *    - Parsing numbers with > 200 digits / fractions using both standard and constrained (200-char) buffers.
 *    - Negative long floats, floats with exponents ('e'/'E', signs '+/-'), and long integer parts.
 *
 * 2. CORE TOKENIZATION & LIFECYCLE (Partition A):
 *    - State transitions: nextToken(), _nextAfterName(), context nesting (OBJECT, ARRAY).
 *    - Custom accessors: nextFieldName(), nextFieldName(SerializableString), nextTextValue(),
 *      nextIntValue(), nextLongValue(), nextBooleanValue().
 *    - Data accessors: getText(), getTextCharacters(), getTextLength(), getTextOffset(), getValueAsString().
 *    - Life-cycle: releaseBuffered(Writer), getInputSource(), close(), _releaseBuffers().
 *
 * 3. BOUNDARY VALUE ANALYSIS (Partition B):
 *    - Empty content, pure whitespace (\t, \r, \n, spaces), mixed CR/LF normalization.
 *    - Root values without surrounding structures, root-level whitespace verification (_verifyRootSpace).
 *    - Numbers: zero ('0', '-0', '0.0'), leading zeroes (_verifyNoLeadingZeroes), scientific formats.
 *
 * 4. EXCEPTION & DEFENSIVE GUARD PATHS (Partition D):
 *    - Mismatched bracket/curly closers: ']' in Object, '}' in Array, root closers.
 *    - Missing separators: colons in objects, commas in arrays/objects.
 *    - Unexpected characters, unterminated strings, invalid/incomplete escapes (\uXXXX, \x).
 *    - Invalid number starts ('-', '+', non-digits after decimal/exponent).
 *    - Non-string binary decoding access rejection.
 *
 * 5. EXTENDED SYNTAX & COMPLIANCE (Partition E):
 *    - ALLOW_COMMENTS: C-style (/* ... * /), C++ style (// ...), unterminated comments.
 *    - ALLOW_YAML_COMMENTS: '#' to newline/EOF.
 *    - ALLOW_SINGLE_QUOTES: single-quoted string values, single-quoted field names, escape sequences.
 *    - ALLOW_UNQUOTED_FIELD_NAMES: valid JS identifiers, symbols, digits, odd name handling.
 *    - ALLOW_NUMERIC_LEADING_ZEROS: '007', '0000'.
 *    - ALLOW_NON_NUMERIC_NUMBERS: NaN, Infinity, -Infinity, +INF, -INF.
 *    - Base64 incremental streaming & padding variations (_decodeBase64, readBinaryValue).
 * ----------------------------------------------------------------------------------------------------
 */

import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

public class ReaderBasedJsonParserGeminiTest {

    // --------------------------------------------------------------------------------
    // Test Infrastructure & Helper Factories
    // --------------------------------------------------------------------------------

    private ReaderBasedJsonParser createParser(String input) {
        return createParser(input, 0);
    }

    private ReaderBasedJsonParser createParser(String input, int features) {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, input, false);
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot();
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(input), null, symbols);
    }

    private ReaderBasedJsonParser createParserWithBuffer(String input, int bufferSize) {
        return createParserWithBuffer(input, bufferSize, 0);
    }

    private ReaderBasedJsonParser createParserWithBuffer(String input, int bufferSize, int features) {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, input, false);
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot();
        char[] buf = new char[bufferSize];
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(input), null, symbols, buf, 0, 0, false);
    }

    private int enableFeatures(JsonParser.Feature... features) {
        int mask = 0;
        for (JsonParser.Feature f : features) {
            mask |= f.getMask();
        }
        return mask;
    }

    // ================================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Known Regressions)
    // ================================================================================

    /**
     * Targets Defects4J bug:
     * com.fasterxml.jackson.core.json.TestNumericValues::testLongerFloatingPoint
     * -> java.lang.ArrayIndexOutOfBoundsException: 200
     * Triggered when parsing long floating point representations where the fractional
     * expansion or buffer boundary causes outBuf/input indexing misalignment.
     */
    @Test(timeout = 4000)
    public void testLongerFloatingPoint() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("1234567890.1234567890");
        for (int i = 0; i < 200; ++i) {
            sb.append("0");
        }
        sb.append("1");
        String input = sb.toString();

        // 1. Verification on standard parser
        ReaderBasedJsonParser p1 = createParser(input);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p1.nextToken());
        assertEquals(input, p1.getText());
        assertNull(p1.nextToken());
        p1.close();

        // 2. Verification on parser constrained with a 200-char input buffer
        ReaderBasedJsonParser p2 = createParserWithBuffer(input, 200);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p2.nextToken());
        assertEquals(input, p2.getText());
        assertNull(p2.nextToken());
        p2.close();
    }

    @Test(timeout = 4000)
    public void testLongerFloatingPointNegative() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("-1234567890.1234567890");
        for (int i = 0; i < 200; ++i) {
            sb.append("0");
        }
        sb.append("1");
        String input = sb.toString();

        ReaderBasedJsonParser p = createParserWithBuffer(input, 200);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(input, p.getText());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLongerFloatingPointWithExponent() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("1234567890.1234567890");
        for (int i = 0; i < 200; ++i) {
            sb.append("0");
        }
        sb.append("e+12");
        String input = sb.toString();

        ReaderBasedJsonParser p = createParserWithBuffer(input, 200);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(input, p.getText());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLongIntegerPartFloatingPoint() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; ++i) {
            sb.append("1");
        }
        sb.append(".5");
        String input = sb.toString();

        ReaderBasedJsonParser p = createParserWithBuffer(input, 200);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(input, p.getText());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLongIntegerPartNegative() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("-");
        for (int i = 0; i < 200; ++i) {
            sb.append("9");
        }
        sb.append(".875");
        String input = sb.toString();

        ReaderBasedJsonParser p = createParserWithBuffer(input, 128);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(input, p.getText());
        assertNull(p.nextToken());
        p.close();
    }

    // ================================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // ================================================================================

    @Test(timeout = 4000)
    public void testComprehensiveDocumentTraversal() throws Exception {
        String json = "{\"name\":\"Alice\",\"age\":30,\"gpa\":3.85,\"verified\":true,\"retired\":false,"
                + "\"extra\":null,\"tags\":[\"dev\",\"qa\"],\"nested\":{\"subKey\":100}}";
        ReaderBasedJsonParser p = createParser(json);

        assertNull(p.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertTrue(p.getParsingContext().inObject());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals("name", p.getText());
        assertEquals("name", p.getValueAsString());
        assertArrayEquals("name".toCharArray(), p.getTextCharacters());
        assertEquals(4, p.getTextLength());
        assertEquals(0, p.getTextOffset());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("Alice", p.getText());
        assertEquals("Alice", p.getValueAsString());
        assertEquals("Alice", p.getValueAsString("fallback"));

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("age", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(30, p.getIntValue());
        assertEquals(30L, p.getLongValue());
        assertEquals("30", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.85, p.getDoubleValue(), 0.0001);
        assertEquals("3.85", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());
        assertEquals("true", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertFalse(p.getBooleanValue());
        assertEquals("false", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals("null", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertTrue(p.getParsingContext().inArray());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("dev", p.getText());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("qa", p.getText());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("subKey", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(100, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLookaheadAndNextMethods() throws Exception {
        String json = "{\"id\":101,\"big\":999999999999,\"flag\":true,\"flagFalse\":false,"
                + "\"title\":\"Architect\",\"arr\":[1],\"obj\":{\"a\":2}}";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        SerializedString idStr = new SerializedString("id");
        assertTrue(p.nextFieldName(idStr));
        assertEquals(101, p.nextIntValue(0));

        SerializedString bigStr = new SerializedString("big");
        assertTrue(p.nextFieldName(bigStr));
        assertEquals(999999999999L, p.nextLongValue(0L));

        assertEquals("flag", p.nextFieldName());
        assertEquals(Boolean.TRUE, p.nextBooleanValue());

        assertEquals("flagFalse", p.nextFieldName());
        assertEquals(Boolean.FALSE, p.nextBooleanValue());

        assertEquals("title", p.nextFieldName());
        assertEquals("Architect", p.nextTextValue());

        // Fast field name mismatch
        SerializedString missingStr = new SerializedString("missing");
        assertFalse(p.nextFieldName(missingStr));
        assertEquals(JsonToken.FIELD_NAME, p.getCurrentToken());
        assertEquals("arr", p.getCurrentName());

        p.nextToken(); // to START_ARRAY
        assertNull(p.nextFieldName()); // in array, should be null
        assertEquals(1, p.nextIntValue(0));
        assertEquals(JsonToken.END_ARRAY, p.nextToken());

        assertEquals("obj", p.nextFieldName());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("a", p.nextFieldName());
        assertEquals(2, p.nextIntValue(0));
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testReleaseBufferedAndLifecycle() throws Exception {
        String input = "{\"k\":1} trailing content";
        ReaderBasedJsonParser p = createParser(input);
        assertNotNull(p.getInputSource());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        StringWriter sw = new StringWriter();
        int released = p.releaseBuffered(sw);
        assertTrue(released > 0);
        assertTrue(sw.toString().contains("trailing content"));

        // Second release must return 0
        assertEquals(0, p.releaseBuffered(new StringWriter()));

        assertNull(p.getCodec());
        p.setCodec(null);
        p.close();
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testLocationsTracking() throws Exception {
        String json = "{\n  \"line2\": \"value\"\n}";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(1, p.getTokenLocation().getLineNr());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        JsonLocation nameLoc = p.getTokenLocation();
        assertEquals(2, nameLoc.getLineNr());
        assertTrue(nameLoc.getColumnNr() >= 3);

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        JsonLocation valLoc = p.getTokenLocation();
        assertEquals(2, valLoc.getLineNr());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // ================================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // ================================================================================

    @Test(timeout = 4000)
    public void testEmptyAndPureWhitespaceInput() throws Exception {
        ReaderBasedJsonParser p1 = createParser("");
        assertNull(p1.nextToken());
        assertNull(p1.getText());
        assertEquals(0, p1.getTextLength());
        assertNull(p1.getTextCharacters());
        p1.close();

        ReaderBasedJsonParser p2 = createParser("   \t  \r\n \n \r  ");
        assertNull(p2.nextToken());
        p2.close();
    }

    @Test(timeout = 4000)
    public void testRootLevelValuesSeparatedByWhitespace() throws Exception {
        String json = "100   \"rootString\"   true   false   null   -200.5";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(100, p.getIntValue());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("rootString", p.getText());

        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(-200.5, p.getDoubleValue(), 0.001);

        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testZeroBoundaries() throws Exception {
        String json = "[0, -0, 0.0, -0.0, 0.00001, -0.00001]";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
        assertEquals("0", p.getText());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
        assertEquals("-0", p.getText());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.0, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(-0.0, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testStringEscapesFullSpectrum() throws Exception {
        String json = "\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t \\u0041 \\u007a\"";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        String expected = "\" \\ / \b \f \n \r \t A z";
        assertEquals(expected, p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSkipStringFastForward() throws Exception {
        // Tests _skipString() when caller does not request getText()
        String json = "[\"skipped string 1\", \"skipped string 2\", 42]";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        // do not call getText(), call nextToken directly
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // ================================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // ================================================================================

    @Test(timeout = 4000)
    public void testMismatchedBracketCloser() throws Exception {
        ReaderBasedJsonParser p = createParser("[1, 2}");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        try {
            p.nextToken();
            fail("Expected JsonParseException for mismatched end marker");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("mismatched") || e.getMessage().contains("expected a value"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testMismatchedCurlyCloser() throws Exception {
        ReaderBasedJsonParser p = createParser("{\"a\": 1]");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        try {
            p.nextToken();
            fail("Expected JsonParseException for mismatched end marker");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("mismatched") || e.getMessage().contains("expected a value"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testUnterminatedStringThrowsEOF() throws Exception {
        ReaderBasedJsonParser p = createParser("\"unterminated string");
        try {
            p.nextToken();
            fail("Expected EOF in string");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("was expecting closing quote") || e.getMessage().contains("Unexpected end-of-input"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testInvalidEscapeSequence() throws Exception {
        ReaderBasedJsonParser p = createParser("\"invalid escape \\x here\"");
        try {
            p.nextToken();
            p.getText();
            fail("Expected error on invalid escape");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Unrecognized character escape"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testIncompleteHexEscape() throws Exception {
        ReaderBasedJsonParser p = createParser("\"bad hex \\u001\"");
        try {
            p.nextToken();
            p.getText();
            fail("Expected error on truncated hex escape");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Unexpected end-of-input") || e.getMessage().contains("character escape"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testMissingColonInObject() throws Exception {
        ReaderBasedJsonParser p = createParser("{\"key\" \"value\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        try {
            p.nextToken();
            fail("Expected colon error");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("was expecting a colon"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testMissingCommaInArray() throws Exception {
        ReaderBasedJsonParser p = createParser("[1 2]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        try {
            p.nextToken();
            fail("Expected comma error");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("was expecting comma"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testMalformedNumberStarts() throws Exception {
        ReaderBasedJsonParser p1 = createParser("-a");
        try {
            p1.nextToken();
            fail("Expected error on malformed negative number");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("expected digit"));
        }
        p1.close();

        ReaderBasedJsonParser p2 = createParser("1.");
        try {
            p2.nextToken();
            fail("Expected error on missing fraction digits");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Decimal point not followed by a digit"));
        }
        p2.close();

        ReaderBasedJsonParser p3 = createParser("1e");
        try {
            p3.nextToken();
            fail("Expected error on missing exponent digits");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Exponent indicator not followed by a digit"));
        }
        p3.close();
    }

    @Test(timeout = 4000)
    public void testLeadingZeroesDisallowedByDefault() throws Exception {
        ReaderBasedJsonParser p = createParser("007");
        try {
            p.nextToken();
            fail("Expected error for leading zeroes by default");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Leading zeroes not allowed"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testMissingRootWhitespaceThrows() throws Exception {
        ReaderBasedJsonParser p = createParser("123\"noSpace\"");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        try {
            p.nextToken();
            fail("Expected missing root whitespace error");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("missing whitespace") || e.getMessage().contains("expected a valid value"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueInvalidTokenType() throws Exception {
        ReaderBasedJsonParser p = createParser("123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        try {
            p.getBinaryValue();
            fail("Cannot access integer as binary");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("can not access as binary"));
        }
        p.close();
    }

    // ================================================================================
    // PARTITION E: EXTENDED SYNTAX, OPTIONS & CANONICALIZATION
    // ================================================================================

    @Test(timeout = 4000)
    public void testCommentsEnabled() throws Exception {
        String json = "/* block comment */\n"
                + "{\n"
                + "  // single line comment\n"
                + "  \"key\": /* inline */ 123 // trailing\n"
                + "}";
        int feat = enableFeatures(JsonParser.Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser p = createParser(json, feat);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("key", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testYamlCommentsEnabled() throws Exception {
        String json = "# YAML header comment\n"
                + "{\"id\": # internal comment\n"
                + "456}";
        int feat = enableFeatures(JsonParser.Feature.ALLOW_YAML_COMMENTS);
        ReaderBasedJsonParser p = createParser(json, feat);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("id", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(456, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSingleQuotesEnabled() throws Exception {
        String json = "{'key': 'value with \\'single\\' quotes', 'other': 'simple'}";
        int feat = enableFeatures(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        ReaderBasedJsonParser p = createParser(json, feat);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("key", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value with 'single' quotes", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("other", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("simple", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNamesEnabled() throws Exception {
        String json = "{foo: 1, _bar$: 2, $baz: 3, id_99: 4}";
        int feat = enableFeatures(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        ReaderBasedJsonParser p = createParser(json, feat);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("foo", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("_bar$", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("$baz", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("id_99", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLeadingZeroesEnabled() throws Exception {
        String json = "[007, 0000, 0123]";
        int feat = enableFeatures(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        ReaderBasedJsonParser p = createParser(json, feat);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(7, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNonNumericNumbersEnabled() throws Exception {
        String json = "[NaN, Infinity, +Infinity, -Infinity, +INF, -INF]";
        int feat = enableFeatures(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        ReaderBasedJsonParser p = createParser(json, feat);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testBase64DecodingAndStreaming() throws Exception {
        // "Hello World!" encoded in standard Base64 is "SGVsbG8gV29ybGQh"
        String raw = "Hello World!";
        String json = "[\"SGVsbG8gV29ybGQh\", \" YQ== \", \"YWI=\", \"YWJj\"]";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        // 1. Full decode via getBinaryValue()
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] decoded = p.getBinaryValue(Base64Variants.MIME);
        assertEquals(raw, new String(decoded, "UTF-8"));

        // 2. 1-byte decode ("a")
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        int bytes1 = p.readBinaryValue(Base64Variants.MIME, out1);
        assertEquals(1, bytes1);
        assertEquals("a", new String(out1.toByteArray(), "UTF-8"));

        // 3. 2-byte decode ("ab")
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        int bytes2 = p.readBinaryValue(Base64Variants.MIME, out2);
        assertEquals(2, bytes2);
        assertEquals("ab", new String(out2.toByteArray(), "UTF-8"));

        // 4. 3-byte decode ("abc")
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        int bytes3 = p.readBinaryValue(Base64Variants.MIME, out3);
        assertEquals(3, bytes3);
        assertEquals("abc", new String(out3.toByteArray(), "UTF-8"));

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testOddNameBufferBoundaryExpansion() throws Exception {
        // Forces _handleOddName to span multiple input reads if buffer is constrained
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 40; ++i) {
            sb.append("longFieldNamePart");
        }
        sb.append(": 999}");
        String json = sb.toString();

        int feat = enableFeatures(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        ReaderBasedJsonParser p = createParserWithBuffer(json, 32, feat);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertTrue(p.getCurrentName().startsWith("longFieldNamePart"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(999, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }
}