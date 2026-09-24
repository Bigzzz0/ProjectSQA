package com.fasterxml.jackson.core.json;

import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: ReaderBasedJsonParser (Jackson-core)
 *
 * Key Areas & Branches Targeted:
 * 1. Defects4J Known Defect (LocationInObjectTest::testOffsetWithObjectFieldsUsingReader):
 *    - After reading a FIELD_NAME, _nextAfterName() did not properly update token location
 *      offsets, causing subsequent value token locations to inherit outdated start offsets.
 * 2. Token Lifecycle & State Transitions:
 *    - nextToken(), _nextAfterName(), nextFieldName(), nextTextValue(), nextIntValue(),
 *      nextLongValue(), nextBooleanValue().
 * 3. Fast and Slow Paths for Field Name Matching:
 *    - _isNextTokenNameYes vs _isNextTokenNameMaybe, nextFieldName(SerializableString).
 * 4. Number Parsing:
 *    - Fast path (_parsePosNumber, _parseNegNumber) vs Split-buffer boundary (_parseNumber2).
 *    - Zero prefixes, leading zeroes validation (_verifyNoLeadingZeroes, _verifyNLZ2).
 *    - Float, scientific exponent ('e', 'E', '+', '-'), root space verification.
 *    - Non-numeric numbers (NaN, Infinity, -Infinity) under Feature.ALLOW_NON_NUMERIC_NUMBERS.
 * 5. String and Escape Processing:
 *    - Fast unescaped buffer scan (_finishString) vs slow escape decode (_finishString2, _skipString).
 *    - Base64 incremental decoding (_readBinary, _decodeBase64, missing padding JACKSON-631).
 *    - Odd values & single quotes (_handleApos, _parseAposName, _handleOddName).
 * 6. Comments & Whitespace:
 *    - Standard C-style comments, line comments, YAML comments (#), CRLF counting.
 * 7. Buffer and Resource Lifecycle:
 *    - Buffer recycling, releaseBuffered(Writer), AUTO_CLOSE_SOURCE toggle, non-recyclable buffer.
 */
public class ReaderBasedJsonParserGeminiTest {

    // Helper: standard factory method for ReaderBasedJsonParser
    private ReaderBasedJsonParser createParser(String doc) {
        return createParser(doc, 0);
    }

    private ReaderBasedJsonParser createParser(String doc, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot().makeChild(JsonFactory.Feature.collectDefaults());
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(doc), null, sym);
    }

    private ReaderBasedJsonParser createParserWithBuffer(String doc, int bufSize, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot().makeChild(JsonFactory.Feature.collectDefaults());
        char[] buf = new char[bufSize];
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(doc), null, sym, buf, 0, 0, true);
    }

    private static class CloseTrackerReader extends StringReader {
        boolean closed = false;
        CloseTrackerReader(String s) { super(s); }
        @Override public void close() { closed = true; super.close(); }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defects4J Regression)
    // =========================================================================

    /**
     * Exact replication of Defects4J bug:
     * com.fasterxml.jackson.core.json.LocationInObjectTest::testOffsetWithObjectFieldsUsingReader
     * Verifies that after parsing a field name, the following value token's character offset
     * is correctly pointing to the value itself (e.g. 6) rather than retaining the start offset (1).
     */
    @Test(timeout = 4000)
    public void testOffsetWithObjectFieldsUsingReaderDefect() throws IOException {
        final String doc = "{\"f1\":\"v1\",\"f2\":true}";
        JsonParser p = createParser(doc);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(1L, p.getTokenLocation().getCharOffset());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("f1", p.getCurrentName());
        assertEquals(2L, p.getTokenLocation().getCharOffset());

        // Under defective versions, _nextAfterName fails to update token location,
        // yielding offset 1 instead of 6.
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("v1", p.getText());
        assertEquals(6L, p.getTokenLocation().getCharOffset());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("f2", p.getCurrentName());

        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals(18L, p.getTokenLocation().getCharOffset());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicDocumentTraversal() throws IOException {
        String json = "{\"str\":\"hello\",\"num\":42,\"arr\":[true,false,null]}";
        ReaderBasedJsonParser p = createParser(json);

        assertNull(p.currentToken());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("{", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("str", p.getCurrentName());
        assertEquals("str", p.getText());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        assertEquals("hello", p.getValueAsString());
        assertEquals("hello", p.getValueAsString("default"));

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("num", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertEquals(42L, p.getLongValue());
        assertEquals("42", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("arr", p.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());

        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertFalse(p.getBooleanValue());

        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.getText());

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextFieldNameWithSerializableString() throws IOException {
        SerializedString f1 = new SerializedString("f1");
        SerializedString f2 = new SerializedString("f2");
        SerializedString missing = new SerializedString("f3");

        String json = "{\"f1\":\"val1\", \"f2\":123}";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        // Direct fast match
        assertTrue(p.nextFieldName(f1));
        assertEquals("val1", p.nextTextValue());

        // Mismatched match attempt
        assertFalse(p.nextFieldName(missing));
        assertEquals("f2", p.getCurrentName());
        assertEquals(123, p.nextIntValue(-1));

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertFalse(p.nextFieldName(f1)); // after object
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextFieldNameFastAndFallback() throws IOException {
        String json = "{\"alpha\": \"str\", \"beta\": 100, \"gamma\": true, \"delta\": [1]}";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("alpha", p.nextFieldName());
        assertEquals("str", p.nextTextValue());

        assertEquals("beta", p.nextFieldName());
        assertEquals(100, p.nextIntValue(-1));

        assertEquals("gamma", p.nextFieldName());
        assertEquals(Boolean.TRUE, p.nextBooleanValue());

        assertEquals("delta", p.nextFieldName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(1, p.nextIntValue(-1));
        assertEquals(JsonToken.END_ARRAY, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextFieldName());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextOptimizedValues() throws IOException {
        String json = "{\"n\": 500, \"l\": 999999999999, \"t\": true, \"f\": false, \"s\": \"foo\"}";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals("n", p.nextFieldName());
        assertEquals(500, p.nextIntValue(0));

        assertEquals("l", p.nextFieldName());
        assertEquals(999999999999L, p.nextLongValue(0L));

        assertEquals("t", p.nextFieldName());
        assertEquals(Boolean.TRUE, p.nextBooleanValue());

        assertEquals("f", p.nextFieldName());
        assertEquals(Boolean.FALSE, p.nextBooleanValue());

        assertEquals("s", p.nextFieldName());
        assertEquals("foo", p.nextTextValue());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testTextCharactersOffsetAndLength() throws IOException {
        String json = "{\"testName\": 123.45}";
        ReaderBasedJsonParser p = createParser(json);

        assertNull(p.getTextCharacters());
        assertEquals(0, p.getTextLength());
        assertEquals(0, p.getTextOffset());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertNotNull(p.getTextCharacters());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        char[] nameChars = p.getTextCharacters();
        assertNotNull(nameChars);
        int nameLen = p.getTextLength();
        assertEquals(8, nameLen);
        assertEquals("testName", new String(nameChars, p.getTextOffset(), nameLen));

        // Call again to test cached _nameCopied path
        char[] nameChars2 = p.getTextCharacters();
        assertSame(nameChars, nameChars2);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        char[] numChars = p.getTextCharacters();
        int numLen = p.getTextLength();
        int numOffset = p.getTextOffset();
        assertEquals("123.45", new String(numChars, numOffset, numLen));

        p.close();
    }

    @Test(timeout = 4000)
    public void testReleaseBuffered() throws IOException {
        String json = "{\"a\":1} tail content";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        StringWriter sw = new StringWriter();
        int count = p.releaseBuffered(sw);
        assertTrue(count > 0);
        assertTrue(sw.toString().contains("tail content"));

        int count2 = p.releaseBuffered(sw);
        assertEquals(0, count2);
        p.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Numeric Edge Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndWhitespaceInput() throws IOException {
        ReaderBasedJsonParser p = createParser("   \t  \r \n  ");
        assertNull(p.nextToken());
        assertNull(p.currentToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNumberRepresentations() throws IOException {
        String json = "[ 0, -0, 100, -100, 0.125, -0.5, 1e3, 2.5e-2, -4.2E+3 ]";
        ReaderBasedJsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(100, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-100, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.125, p.getDoubleValue(), 0.00001);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(-0.5, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1000.0, p.getDoubleValue(), 0.001);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.025, p.getDoubleValue(), 0.00001);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(-4200.0, p.getDoubleValue(), 0.001);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNumberParsingOverSmallBuffer() throws IOException {
        // Small buffer size of 4 forces _parseNumber2 and