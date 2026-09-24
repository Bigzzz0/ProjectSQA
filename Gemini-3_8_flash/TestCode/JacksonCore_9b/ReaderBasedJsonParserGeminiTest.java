package com.fasterxml.jackson.core.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------------------
 * Target Class: com.fasterxml.jackson.core.json.ReaderBasedJsonParser
 *
 * Targeted Defects & Regressions:
 * 1. Defects4J Known Defect: getValueAsString() / getValueAsString(def) on JsonToken.FIELD_NAME returns
 *    null instead of the field name because it delegated to super.getValueAsString(null) instead of
 *    returning _parsingContext.getCurrentName().
 *    Target Test: testGetValueAsStringOnFieldName_DefectTarget()
 *
 * Targeted Decision / Condition Branches:
 * - Parsing Lifecycle: Constructors with recyclable buffer vs custom buffer; releaseBuffered, close.
 * - String Parsing: _finishString fast path vs slow path (_finishString2), escape characters (\b,\t,\n,
 *   \f,\r,\",\/,\\,\uXXXX), unquoted control characters, _skipString partial skipping.
 * - Single-Quote Support (ALLOW_SINGLE_QUOTES): _parseAposName, _handleApos with escapes.
 * - Unquoted Field Names (ALLOW_UNQUOTED_FIELD_NAMES): Latin-1 and Java identifier chars, odd name parsing.
 * - Numeric Values: Pos/Neg ints, decimals, exponents (e, E, +/-), boundary buffer reloading (_parseNumber2),
 *   leading zero handling (ALLOW_NUMERIC_LEADING_ZEROS), NaN / Infinity (ALLOW_NON_NUMERIC_NUMBERS).
 * - Comments: C-style multi-line comments, C++ single-line comments (ALLOW_COMMENTS), YAML comments (#).
 * - Base64 Decoding: Incomplete tokens, streaming to OutputStream, with and without padding, whitespace.
 * - Next* Value Lookaheads: nextTextValue(), nextIntValue(), nextLongValue(), nextBooleanValue()
 *   under FIELD_NAME context vs root/array context.
 * - Structural & Error Guards: Mismatched brackets (], }), missing colons, missing commas, invalid tokens.
 * -----------------------------------------------------------------------------------------------------
 */
public class ReaderBasedJsonParserGeminiTest {

    private final JsonFactory _jsonFactory = new JsonFactory();

    private ReaderBasedJsonParser createParser(String doc) throws IOException {
        return (ReaderBasedJsonParser) _jsonFactory.createParser(new StringReader(doc));
    }

    private ReaderBasedJsonParser createParser(String doc, JsonFactory factory) throws IOException {
        return (ReaderBasedJsonParser) factory.createParser(new StringReader(doc));
    }

    /*
     ***************************************************************************************************
     * Partition A: Core Functional Logic & State Transitions
     ***************************************************************************************************
     */

    @Test(timeout = 4000)
    public void testBasicDocumentTraversalAndState() throws Exception {
        String json = "{\"id\": 101, \"active\": true, \"nullVal\": null, \"tags\": [\"jvm\", \"qa\"]}";
        ReaderBasedJsonParser p = createParser(json);

        assertNull(p.getCurrentToken());
        assertNull(p.getText());
        assertEquals(0, p.getTextOffset());
        assertEquals(0, p.getTextLength());
        assertNull(p.getTextCharacters());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("{", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("id", p.getCurrentName());
        assertEquals("id", p.getText());
        assertEquals(2, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        assertNotNull(p.getTextCharacters());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(101, p.getIntValue());
        assertEquals("101", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("active", p.getCurrentName());

        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());
        assertEquals("true", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("nullVal", p.getCurrentName());

        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals("null", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("tags", p.getCurrentName());

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals("[", p.getText());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("jvm", p.getText());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("qa", p.getText());

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals("]", p.getText());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals("}", p.getText());

        assertNull(p.nextToken());
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testOptimizedNextValueLookaheads() throws Exception {
        String json = "{\"text\": \"hello\", \"int\": 42, \"long\": 9876543210, \"bool\": true, \"none\": false}";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("hello", p.nextTextValue());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(42, p.nextIntValue(-1));

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(9876543210L, p.nextLongValue(-1L));

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(Boolean.TRUE, p.nextBooleanValue());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(Boolean.FALSE, p.nextBooleanValue());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testOptimizedNextValueLookaheadsWithDefaults() throws Exception {
        String json = "{\"txtObj\": {}, \"intObj\": {}, \"longObj\": {}, \"boolObj\": {}}";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertNull(p.nextTextValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(99, p.nextIntValue(99));
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(888L, p.nextLongValue(888L));
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertNull(p.nextBooleanValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSkipStringIncompleteToken() throws Exception {
        String json = "[\"firstSkip\", \"secondKeep\"]";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        // Do not read text, advancing directly triggers _skipString()
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("secondKeep", p.getText());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCodecAndInputSourceAccess() throws Exception {
        Reader sr = new StringReader("{}");
        ReaderBasedJsonParser p = (ReaderBasedJsonParser) _jsonFactory.createParser(sr);

        assertSame(sr, p.getInputSource());
        assertNull(p.getCodec());
        p.setCodec(null);
        assertNull(p.getCodec());

        p.close();
    }

    @Test(timeout = 4000)
    public void testReleaseBuffered() throws Exception {
        ReaderBasedJsonParser p = createParser("{\"k\":1}   trail");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        StringWriter sw = new StringWriter();
        int released = p.releaseBuffered(sw);
        assertTrue(released >= 0);

        // After releasing, further release returns 0
        assertEquals(0, p.releaseBuffered(new StringWriter()));
        p.close();
    }

    /*
     ***************************************************************************************************
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     ***************************************************************************************************
     */

    @Test(timeout = 4000)
    public void testNumberParsingVariants() throws Exception {
        String json = "[0, -0, 123456789, -987654321, 0.125, -0.875, 1.2e3, -3.4E-2, 5e+4, 1234567890123456]";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("0", p.getText());
        assertEquals(0, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("-0", p.getText());
        assertEquals(0, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("123456789", p.getText());
        assertEquals(123456789, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("-987654321", p.getText());
        assertEquals(-987654321, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("0.125", p.getText());
        assertEquals(0.125, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("-0.875", p.getText());
        assertEquals(-0.875, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("1.2e3", p.getText());
        assertEquals(1200.0, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("-3.4E-2", p.getText());
        assertEquals(-0.034, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("5e+4", p.getText());
        assertEquals(50000.0, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1234567890123456L, p.getLongValue());

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNumberSplitOverSmallBuffer() throws Exception {
        // Construct parser with small 4-char buffer to exercise _parseNumber2 boundary logic
        IOContext ctxt = new IOContext(new BufferRecycler(), "testSource", false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot().makeChild(JsonFactory.Feature.collectDefaults());
        char[] customBuffer = new char[4];
        StringReader reader = new StringReader("[-123456.78e2, 987654]");

        ReaderBasedJsonParser p = new ReaderBasedJsonParser(ctxt, 0, reader, null, sym, customBuffer, 0, 0, false);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("-123456.78e2", p.getText());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("987654", p.getText());

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLeadingZerosFeature() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        ReaderBasedJsonParser p = createParser("[007, 000, 0123]", f);

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
    public void testNonStandardNumbers() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        ReaderBasedJsonParser p = createParser("[NaN, Infinity, +Infinity, -Infinity, +INF, -INF]", f);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));
        assertEquals("NaN", p.getText());

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
    public void testRootValuesWithWhitespaceSeparators() throws Exception {
        String json = "10 \t 20 \r\n 30 \r 40 \n 50";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(10, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(20, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(30, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(40, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(50, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
    }

    /*
     ***************************************************************************************************
     * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
     ***************************************************************************************************
     */

    /**
     * TARGETED DEFECT:
     * Defects4J ComparisonFailure: expected:<a> but was:<null>
     * When current token is FIELD_NAME, getValueAsString() must return the field name itself.
     * In the defective implementation, getValueAsString() delegated to super.getValueAsString(null),
     * which returned null because FIELD_NAME is not considered a scalar value.
     */
    @Test(timeout = 4000)
    public void testGetValueAsStringOnFieldName_DefectTarget() throws Exception {
        String json = "{\"a\": 123}";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getText());

        // CRITICAL DEFECT ASSERTION:
        // Expected "a", but defective code yields null
        assertEquals("a", p.getValueAsString());
        assertEquals("a", p.getValueAsString("default"));

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("123", p.getValueAsString());
        assertEquals("123", p.getValueAsString("default"));

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    /*
     ***************************************************************************************************
     * Partition D: Non-Standard Syntax, Comments, and Escapes
     ***************************************************************************************************
     */

    @Test(timeout = 4000)
    public void testSingleQuotesParsing() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        String json = "{'name': 'Jackson\\'s Parser', 'quote': 'Hello \\\"World\\\"'}";
        ReaderBasedJsonParser p = createParser(json, f);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getText());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("Jackson's Parser", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("quote", p.getText());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("Hello \"World\"", p.getText());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNames() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        String json = "{foo: 1, _bar$: 2, nonAscii\u00e9: 3}";
        ReaderBasedJsonParser p = createParser(json, f);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("foo", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("_bar$", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("nonAscii\u00e9", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCommentsSkipping() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        f.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);
        String json = "/* header comment */\n" +
                      "{\n" +
                      "  // single line comment\n" +
                      "  \"key\": /* inline */ \"val\",\n" +
                      "  # yaml style comment\n" +
                      "  \"arr\": [ 1 /* comm */ , 2 // end\n" +
                      "  ]\n" +
                      "}";
        ReaderBasedJsonParser p = createParser(json, f);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("key", p.getText());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("val", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("arr", p.getText());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testStringEscapeSequences() throws Exception {
        String json = "[\"\\b\\t\\n\\f\\r\\\"\\/\\\\\\u0041\\u007a\"]";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\b\t\n\f\r\"/\\Az", p.getText());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    /*
     ***************************************************************************************************
     * Partition E: Base64 Decoding & Binary Streaming
     ***************************************************************************************************
     */

    @Test(timeout = 4000)
    public void testBase64DecodingVariants() throws Exception {
        // "Defects4J" in base64 is "RGVmZWN0czRK"
        String json = "[\"RGVmZWN0czRK\", \"\", \"QQ==\", \"QUI=\"]";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] b1 = p.getBinaryValue(Base64Variants.MIME);
        assertEquals("Defects4J", new String(b1, "UTF-8"));

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] b2 = p.getBinaryValue(Base64Variants.MIME);
        assertEquals(0, b2.length);

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] b3 = p.getBinaryValue(Base64Variants.MIME);
        assertEquals("A", new String(b3, "UTF-8"));

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = p.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(2, count);
        assertEquals("AB", new String(out.toByteArray(), "UTF-8"));

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testBase64IncrementalStreaming() throws Exception {
        // Longer payload requiring incremental buffer writing
        String text = "Quick brown fox jumps over the lazy dog. Comprehensive Jackson White-box testing.";
        byte[] orig = text.getBytes("UTF-8");
        String b64 = Base64Variants.MIME.encode(orig);
        String json = "[\"" + b64 + "\"]";

        ReaderBasedJsonParser p = createParser(json);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = p.readBinaryValue(Base64Variants.MIME, baos);
        assertEquals(orig.length, bytesRead);
        assertArrayEquals(orig, baos.toByteArray());

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    /*
     ***************************************************************************************************
     * Partition F: Exception Paths & Defensive Guard Paths
     ***************************************************************************************************
     */

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMismatchedBracketThrowsException() throws Exception {
        ReaderBasedJsonParser p = createParser("[1, 2}");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMismatchedCurlyThrowsException() throws Exception {
        ReaderBasedJsonParser p = createParser("{\"k\": 1]");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testLeadingZeroWithoutFeatureThrows() throws Exception {
        ReaderBasedJsonParser p = createParser("0123");
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnrecognizedEscapeThrows() throws Exception {
        ReaderBasedJsonParser p = createParser("[\"\\z\"]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        p.nextToken();
        p.getText();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidHexEscapeThrows() throws Exception {
        ReaderBasedJsonParser p = createParser("[\"\\u00G0\"]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        p.nextToken();
        p.getText();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnexpectedCharInValueThrows() throws Exception {
        ReaderBasedJsonParser p = createParser("{\"key\": @invalid}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMissingColonThrows() throws Exception {
        ReaderBasedJsonParser p = createParser("{\"key\" \"val\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnclosedCommentThrows() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser p = createParser("/* unclosed comment", f);
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testDecimalWithoutDigitsThrows() throws Exception {
        ReaderBasedJsonParser p = createParser("123.");
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testExponentWithoutDigitsThrows() throws Exception {
        ReaderBasedJsonParser p = createParser("123e");
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidMinusCharThrows() throws Exception {
        ReaderBasedJsonParser p = createParser("-a");
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testBinaryAccessOnNonStringThrows() throws Exception {
        ReaderBasedJsonParser p = createParser("12345");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        p.getBinaryValue(Base64Variants.MIME);
    }
}