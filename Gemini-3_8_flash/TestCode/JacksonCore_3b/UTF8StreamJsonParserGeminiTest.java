package com.fasterxml.jackson.core.json;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.BytesToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Defects4J Bug Focus:
 * - TestLocation::testOffsetWithInputOffset:
 *   When UTF8StreamJsonParser is constructed with a non-zero byte array offset (e.g. inputBuffer, start > 0),
 *   the token location reporting failed to offset _currInputProcessed / _inputPtr appropriately,
 *   causing getTokenLocation().getByteOffset() to report an absolute array index instead of logical stream offset 0.
 *
 * Branch & Coverage Matrix:
 * 1. Construction & Lifecycle:
 *    - releaseBuffered(OutputStream), getInputSource(), _closeInput() with managed/unmanaged resource.
 *    - _releaseBuffers() recycling buffers and releasing canonicalizer symbols.
 * 2. NextToken & State Transitions:
 *    - Object context: field name parsing (short, medium 5-8 bytes, long > 8 bytes, escaped, apos, unquoted).
 *    - Array context vs Object context vs Root context.
 *    - Next after name transitions for string, object, array, number, boolean, null.
 * 3. nextFieldName() Fast & Slow Paths:
 *    - Match with SerializableString on fast colon path, mismatch, and non-object fallback.
 * 4. nextXxxValue Traversal:
 *    - nextTextValue(), nextIntValue(), nextLongValue(), nextBooleanValue() for both field and standalone values.
 * 5. Text & Binary Extraction:
 *    - getText(), getValueAsString(), getTextCharacters(), getTextLength(), getTextOffset().
 *    - Base64 binary decoding (_decodeBase64, readBinaryValue) with whitespace, padding, and missing padding.
 * 6. Numeric & Float Parsing:
 *    - Leading minus, zero validation (_verifyNoLeadingZeroes), buffer boundaries (_parserNumber2),
 *    - Fractions and exponents (_parseFloat), non-numeric tokens (NaN, Infinity, -INF).
 * 7. Comments & Whitespace:
 *    - C-style block comments, C++ line comments, YAML comments (#), CRLF handling.
 * 8. Character & UTF-8 Decoding:
 *    - 2-byte, 3-byte (fast & boundary), 4-byte (surrogate) UTF-8 characters and escapes (\uXXXX, \n, \t, etc.).
 * 9. Exception & Malformed Syntax:
 *    - Unterminated strings, invalid numbers, unexpected end markers, mismatched brackets/braces.
 */
public class UTF8StreamJsonParserGeminiTest {

    private UTF8StreamJsonParser createParser(byte[] input) {
        return createParser(input, 0, input.length, null);
    }

    private UTF8StreamJsonParser createParser(byte[] input, int offset, int len, InputStream in) {
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, "testSource", false);
        BytesToNameCanonicalizer sym = BytesToNameCanonicalizer.createRoot(0);
        int features = JsonFactory.Feature.collectDefaults();
        return new UTF8StreamJsonParser(ctxt, features, in, null,
                sym.makeChild(features), input, offset, offset + len, true);
    }

    private UTF8StreamJsonParser createParserWithFeatures(byte[] input, int features) {
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, "testSource", false);
        BytesToNameCanonicalizer sym = BytesToNameCanonicalizer.createRoot(0);
        return new UTF8StreamJsonParser(ctxt, features, null, null,
                sym.makeChild(features), input, 0, input.length, true);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Defect)
    // =========================================================================

    /**
     * Targets Defects4J bug where inputBuffer offset > 0 yields an incorrect
     * token byte offset in getTokenLocation() (e.g. expected: 0, actual: 3).
     */
    @Test(timeout = 4000)
    public void testDefectOffsetWithInputOffsetViaFactory() throws Exception {
        byte[] raw = "123".getBytes(StandardCharsets.UTF_8);
        byte[] padded = new byte[raw.length + 3];
        System.arraycopy(raw, 0, padded, 3, raw.length);

        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(padded, 3, raw.length);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("Offset relative to parsed segment must be 0", 0L, p.getTokenLocation().getByteOffset());
        assertEquals("Char offset relative to parsed segment must be 0", 0L, p.getTokenLocation().getCharOffset());
        p.close();
    }

    @Test(timeout = 4000)
    public void testDefectDirectParserLocationWithStartOffset() throws Exception {
        byte[] raw = new byte[] { 'x', 'x', 'x', '[', '4', '2', ']' };
        UTF8StreamJsonParser parser = createParser(raw, 3, 4, null);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getTokenLocation().getByteOffset());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1L, parser.getTokenLocation().getByteOffset());
        assertEquals(42, parser.getIntValue());
        parser.close();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testObjectParsingAndNextFieldMatching() throws Exception {
        String json = "{\"name\":\"Alice\",\"age\":30,\"valid\":true,\"score\":null}";
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8));

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        SerializedString nameStr = new SerializedString("name");
        assertTrue(p.nextFieldName(nameStr));
        assertEquals("Alice", p.nextTextValue());

        SerializedString ageStr = new SerializedString("age");
        assertTrue(p.nextFieldName(ageStr));
        assertEquals(30, p.nextIntValue(-1));

        SerializedString validStr = new SerializedString("valid");
        assertTrue(p.nextFieldName(validStr));
        assertEquals(Boolean.TRUE, p.nextBooleanValue());

        SerializedString scoreStr = new SerializedString("score");
        assertTrue(p.nextFieldName(scoreStr));
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextFieldNameMismatchAndFallback() throws Exception {
        String json = "{\"first\":10,\"second\":20}";
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8));
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        SerializedString wrongKey = new SerializedString("mismatch");
        assertFalse(p.nextFieldName(wrongKey));
        assertEquals("first", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(10, p.getIntValue());

        SerializedString secondKey = new SerializedString("second");
        assertTrue(p.nextFieldName(secondKey));
        assertEquals(20L, p.nextLongValue(-1L));

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testArrayTraversalAndPrimitiveLookaheads() throws Exception {
        String json = "[100, 20000000000, true, false, \"hello\"]";
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8));

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(100, p.nextIntValue(0));
        assertEquals(20000000000L, p.nextLongValue(0L));
        assertEquals(Boolean.TRUE, p.nextBooleanValue());
        assertEquals(Boolean.FALSE, p.nextBooleanValue());
        assertEquals("hello", p.nextTextValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testTextExtractionMethods() throws Exception {
        String json = "{\"key\":\"hello\\u0020world\"}";
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8));

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertArrayEquals("key".toCharArray(), p.getTextCharacters());
        assertEquals(3, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        assertEquals("key", p.getText());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello world", p.getText());
        assertEquals("hello world", p.getValueAsString());
        assertEquals("hello world", p.getValueAsString("default"));
        assertEquals(11, p.getTextLength());

        p.close();
    }

    @Test(timeout = 4000)
    public void testNameParsingLengths() throws Exception {
        // Names of length 1..4 (short), 5..8 (medium), and >8 (long)
        String json = "{\"a\":1, \"abcde\":2, \"abcdefgh\":3, \"abcdefghijklmnopqrstuvwxyz\":4}";
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8));
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("abcde", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("abcdefgh", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("abcdefghijklmnopqrstuvwxyz", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testEscapedAndUtf8Names() throws Exception {
        String json = "{\"foo\\\\bar\":1, \"\\\"quoted\\\"\":2, \"\\u0041\\u0042\":3, \"\\u00E9l\\u00E8ve\":4}";
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8));
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("foo\\bar", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("\"quoted\"", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("AB", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("\u00E9l\u00E8ve", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyDocumentAndWhitespace() throws Exception {
        byte[] input = "   \n\r\t   ".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParser(input);
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testEmptyStringAndEmptyName() throws Exception {
        String json = "{\"\":\"\"}";
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8));
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNumberBoundariesAndFormats() throws Exception {
        String json = "[0, -0, 123456789, -987654321, 0.125, -12.5e-3, 1.0E+2]";
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8));
        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123456789, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-987654321, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.125, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(-0.0125, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(100.0, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNumberSplitAcrossStreamChunks() throws Exception {
        byte[] input = "1234567890".getBytes(StandardCharsets.UTF_8);
        InputStream chunkedIn = new InputStream() {
            private int idx = 0;
            @Override
            public int read() {
                if (idx >= input.length) return -1;
                return input[idx++];
            }
            @Override
            public int read(byte[] b, int off, int len) {
                if (idx >= input.length) return -1;
                b[off] = input[idx++];
                return 1; // read 1 byte at a time
            }
        };

        UTF8StreamJsonParser p = createParser(new byte[2], 0, 0, chunkedIn);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("1234567890", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testBase64BinaryParsingVariations() throws Exception {
        // Base64 variants: "foob" -> "Zm9vYg=="
        String json = "{\"b1\":\"Zm9vYg==\", \"b2\":\"Zm9v\", \"b3\":\"\"}";
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8));
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] b1 = p.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals("foob".getBytes(StandardCharsets.UTF_8), b1);

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesWritten = p.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(3, bytesWritten);
        assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), out.toByteArray());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] b3 = p.getBinaryValue(Base64Variants.MIME);
        assertEquals(0, b3.length);

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUtf8MultiByteSequencesInStrings() throws Exception {
        // 2-byte (\u00a2), 3-byte (\u20ac), 4-byte (\uD83D\uDE00)
        String unicodeStr = "\u00A2 \u20AC \uD83D\uDE00";
        String json = "[\"" + unicodeStr + "\"]";
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8));

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(unicodeStr, p.getText());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testExceptionOnUnclosedString() throws Exception {
        byte[] input = "\"unclosed string".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParser(input);
        p.nextToken();
        p.getText(); // finishes string, triggers EOF
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testExceptionOnLeadingZeroNotAllowed() throws Exception {
        byte[] input = "0123".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParser(input);
        p.nextToken();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testExceptionOnMismatchedCloseObject() throws Exception {
        byte[] input = "[ } ]".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParser(input);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        p.nextToken();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testExceptionOnMismatchedCloseArray() throws Exception {
        byte[] input = "{ ] }".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParser(input);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        p.nextToken();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testExceptionOnMissingColon() throws Exception {
        byte[] input = "{\"key\" \"value\"}".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParser(input);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        p.nextToken();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testExceptionOnInvalidUtf8Escape() throws Exception {
        byte[] input = "\"\\u12G4\"".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParser(input);
        p.nextToken();
        p.getText();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testExceptionOnInvalidEscapeChar() throws Exception {
        byte[] input = "\"\\z\"".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParser(input);
        p.nextToken();
        p.getText();
    }

    @Test(timeout = 4000)
    public void testBinaryValueInvalidTokenException() throws Exception {
        byte[] input = "123".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParser(input);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        try {
            p.getBinaryValue(Base64Variants.MIME);
            fail("Expected JsonParseException for binary on number token");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("can not access as binary"));
        }
        p.close();
    }

    // =========================================================================
    // Partition E: Non-Standard Features, Comments & Object Lifecycle
    // =========================================================================

    @Test(timeout = 4000)
    public void testCommentsJavaAndYaml() throws Exception {
        String json = "/* block comment */\n"
                    + "// line comment\n"
                    + "{\n"
                    + "  # yaml comment\n"
                    + "  \"val\": 1 /* inline block */ // trailing\n"
                    + "}";
        int features = JsonFactory.Feature.collectDefaults()
                | JsonParser.Feature.ALLOW_COMMENTS.getMask()
                | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        UTF8StreamJsonParser p = createParserWithFeatures(json.getBytes(StandardCharsets.UTF_8), features);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("val", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSingleQuotesAndUnquotedFieldNames() throws Exception {
        String json = "{ unquoted: 'single quoted' }";
        int features = JsonFactory.Feature.collectDefaults()
                | JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask()
                | JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        UTF8StreamJsonParser p = createParserWithFeatures(json.getBytes(StandardCharsets.UTF_8), features);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("unquoted", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("single quoted", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNonNumericNumbersAllowFeature() throws Exception {
        String json = "[ NaN, Infinity, -Infinity ]";
        int features = JsonFactory.Feature.collectDefaults()
                | JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        UTF8StreamJsonParser p = createParserWithFeatures(json.getBytes(StandardCharsets.UTF_8), features);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testReleaseBufferedAndLifecycle() throws Exception {
        byte[] data = "{\"a\":1}   tail".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        UTF8StreamJsonParser p = createParser(new byte[data.length], 0, 0, in);

        assertNotNull(p.getInputSource());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int released = p.releaseBuffered(out);
        assertTrue(released >= 0);

        p.close();
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testLocationAccuracy() throws Exception {
        String json = "{\n  \"line2\": true\n}";
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8));
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        JsonLocation loc = p.getTokenLocation();
        assertEquals(2, loc.getLineNr());
        assertTrue(loc.getColumnNr() > 1);

        p.close();
    }
}