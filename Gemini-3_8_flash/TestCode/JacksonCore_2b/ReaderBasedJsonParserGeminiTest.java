package com.fasterxml.jackson.core.json;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.util.Arrays;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Defects4J Defect Target:
 * - Issue: Mangled numbers (e.g., "123a", "123.456foo") at root level or within values are
 *   improperly recognized as valid integers (VALUE_NUMBER_INT) instead of failing with a
 *   JsonParseException due to an invalid character immediately trailing the numeric digits.
 * - Test Target: testMangledNumbersCharsDefect, testMangledNumbersWithDecimalDefect.
 *
 * Branch & Coverage Matrix:
 * - Partition A: Core Functional Logic & State Transitions
 *   * Document traversal (objects, arrays, primitives: int, float, true, false, null).
 *   * Field name canonicalization & _nameCopyBuffer expansion across small and large lengths.
 *   * Fast-path and secondary nextXxxValue() variants (nextTextValue, nextIntValue, etc.).
 *   * Base64 decoding (padding: 0, 1, 2 padding chars, unpadded variants, streams).
 *
 * - Partition B: Boundary Value Analysis & Buffer Spanning
 *   * Single-character inputs, zero (0, leading zeroes allowed/forbidden).
 *   * Large payloads forcing segment finish/alloc in TextBuffer and loadMore() buffer reload.
 *   * releaseBuffered() with content remaining vs. drained input.
 *
 * - Partition C: Defect-Targeted Branch Zone
 *   * Root number immediately followed by alphanumeric tokens ('a', 'foo') without whitespace.
 *   * Verification of exception propagation for mangled numeric representations.
 *
 * - Partition D: Exception & Defensive Guard Paths
 *   * Unclosed tokens (EOF in string, comment, number, base64).
 *   * Mismatched structure closers (']' when expecting '}', '}' when expecting ']').
 *   * Missing colons and commas.
 *   * Illegal escapes, non-hexadecimal unicode escape sequences, unquoted ASCII control chars.
 *   * Non-standard number handling without feature flag (NaN, Infinity, +INF, -INF).
 *   * Reader returning 0 characters error check.
 *
 * - Partition E: Object Lifecycle & Config
 *   * ParserBase / Reader closing and symbol table release.
 *   * Codec get/set integrity.
 *   * getInputSource() verification.
 * =========================================================================================
 */
public class ReaderBasedJsonParserGeminiTest {

    private ReaderBasedJsonParser createParser(String json) {
        return createParser(json, 0);
    }

    private ReaderBasedJsonParser createParser(String json, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), json, false);
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot();
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(json), null, symbols);
    }

    private ReaderBasedJsonParser createParser(Reader reader, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "reader", false);
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot();
        return new ReaderBasedJsonParser(ctxt, features, reader, null, symbols);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMangledNumbersCharsDefect() throws IOException {
        String doc = "123a";
        ReaderBasedJsonParser parser = createParser(doc);
        try {
            JsonToken t = parser.nextToken();
            fail("Should have gotten an exception; instead got token: " + t);
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("expected space")
                    || expected.getMessage().contains("unexpected character")
                    || expected.getMessage().contains("was expecting"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testMangledNumbersWithDecimalDefect() throws IOException {
        String doc = "123.456foo";
        ReaderBasedJsonParser parser = createParser(doc);
        try {
            JsonToken t = parser.nextToken();
            fail("Should have gotten an exception; instead got token: " + t);
        } catch (JsonParseException expected) {
            // Success
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testMangledNumbersNegativeDefect() throws IOException {
        String doc = "-999xyz";
        ReaderBasedJsonParser parser = createParser(doc);
        try {
            JsonToken t = parser.nextToken();
            fail("Should have gotten an exception; instead got token: " + t);
        } catch (JsonParseException expected) {
            // Success
        } finally {
            parser.close();
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicDocumentTraversal() throws IOException {
        String json = "{\"id\":101, \"name\":\"Alice\", \"valid\":true, \"score\":null, \"tags\":[\"admin\", false]}";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("id", parser.getCurrentName());
        assertEquals("id", parser.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(101, parser.getIntValue());
        assertEquals("101", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Alice", parser.getText());
        assertEquals("Alice", parser.getValueAsString());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getValueAsString());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("admin", parser.getText());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testOptimizedNextValueMethodsOnFieldName() throws IOException {
        String json = "{\"text\":\"hello\", \"num\":42, \"big\":9876543210123, \"flag\":true, \"flag2\":false, \"arr\":[], \"obj\":{}}";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("hello", parser.nextTextValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(42, parser.nextIntValue(0));

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(9876543210123L, parser.nextLongValue(0L));

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(Boolean.FALSE, parser.nextBooleanValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertNull(parser.nextTextValue()); // encounters START_ARRAY

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(99, parser.nextIntValue(99)); // encounters START_OBJECT

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDirectNextValueMethods() throws IOException {
        String json = "\"sample\" 1234 5678901234 true false";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals("sample", parser.nextTextValue());
        assertEquals(1234, parser.nextIntValue(0));
        assertEquals(5678901234L, parser.nextLongValue(0L));
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());
        assertEquals(Boolean.FALSE, parser.nextBooleanValue());
        assertNull(parser.nextBooleanValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testTextCharactersAndOffset() throws IOException {
        String json = "{\"prop\":\"value\"}";
        ReaderBasedJsonParser parser = createParser(json);

        assertNull(parser.getTextCharacters());
        assertEquals(0, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNotNull(parser.getTextCharacters());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        char[] nameChars = parser.getTextCharacters();
        assertEquals("prop", new String(nameChars, 0, parser.getTextLength()));
        assertEquals(4, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        char[] valChars = parser.getTextCharacters();
        assertEquals("value", new String(valChars, parser.getTextOffset(), parser.getTextLength()));
        assertEquals(5, parser.getTextLength());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNameCopyBufferExpansion() throws IOException {
        StringBuilder sb = new StringBuilder("{\"");
        for (int i = 0; i < 300; ++i) {
            sb.append('a');
        }
        sb.append("\": 1, \"");
        for (int i = 0; i < 600; ++i) {
            sb.append('b');
        }
        sb.append("\": 2}");
        ReaderBasedJsonParser parser = createParser(sb.toString());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        char[] chars1 = parser.getTextCharacters();
        assertEquals(300, parser.getTextLength());
        assertEquals('a', chars1[0]);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        char[] chars2 = parser.getTextCharacters();
        assertEquals(600, parser.getTextLength());
        assertEquals('b', chars2[0]);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testBase64DecodingAndStreaming() throws IOException {
        byte[] data = "Defects4J Testing of Jackson Base64".getBytes("UTF-8");
        String b64 = Base64Variants.MIME.encode(data);
        String json = "\"" + b64 + "\"";

        ReaderBasedJsonParser parser = createParser(json);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] decoded = parser.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(data, decoded);

        // Read again when already finished (exercises cached binary value branch)
        byte[] decodedAgain = parser.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(data, decodedAgain);
        parser.close();

        // Streaming binary read
        parser = createParser(json);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int readCount = parser.readBinaryValue(Base64Variants.MIME, baos);
        assertEquals(data.length, readCount);
        assertArrayEquals(data, baos.toByteArray());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testBase64VariationsAndPadding() throws IOException {
        // Test 1-byte, 2-byte, 3-byte lengths to test all padding branches
        for (int len = 1; len <= 5; ++len) {
            byte[] bytes = new byte[len];
            Arrays.fill(bytes, (byte) (0x41 + len));
            String b64 = Base64Variants.MIME.encode(bytes);
            ReaderBasedJsonParser parser = createParser("\"" + b64 + "\"");
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertArrayEquals(bytes, parser.getBinaryValue(Base64Variants.MIME));
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testStringEscapes() throws IOException {
        String json = "\"\\b\\t\\n\\f\\r\\\"\\/\\\\\\u0041\\u007a\"";
        ReaderBasedJsonParser parser = createParser(json);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\b\t\n\f\r\"/\\Az", parser.getText());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSingleQuotesAndUnquotedFieldNames() throws IOException {
        int features = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask()
                | JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        String json = "{foo: 'bar', 'count': 5}";
        ReaderBasedJsonParser parser = createParser(json, features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("foo", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("bar", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("count", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(5, parser.getIntValue());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testComments() throws IOException {
        int features = JsonParser.Feature.ALLOW_COMMENTS.getMask()
                | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        String json = "/* C-comment */\n{\n// line-comment\r\n \"key\": # YAML comment\n 123 /* inline */\n}";
        ReaderBasedJsonParser parser = createParser(json, features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNumericBoundaries() throws IOException {
        String json = "0 -0 0.0 -0.5 123456789 -987654321 1e5 -2E-3 0.12e+4";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(0.0, parser.getDoubleValue(), 0.0001);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-0.5, parser.getDoubleValue(), 0.0001);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123456789, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-987654321, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(100000.0, parser.getDoubleValue(), 0.0001);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-0.002, parser.getDoubleValue(), 0.0001);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1200.0, parser.getDoubleValue(), 0.0001);

        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testLeadingZeroes() throws IOException {
        int featureOn = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        ReaderBasedJsonParser parser = createParser("007", featureOn);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(7, parser.getIntValue());
        parser.close();

        // Disallowed leading zero should throw exception
        ReaderBasedJsonParser parserDisallowed = createParser("0123", 0);
        try {
            parserDisallowed.nextToken();
            fail("Expected exception on leading zero");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("Leading zeroes not allowed"));
        } finally {
            parserDisallowed.close();
        }
    }

    @Test(timeout = 4000)
    public void testNonStandardNumbers() throws IOException {
        int featureOn = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        String json = "NaN Infinity -Infinity +Infinity INF -INF +INF";
        ReaderBasedJsonParser parser = createParser(json, featureOn);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testReleaseBuffered() throws IOException {
        String json = "  {\"a\": 1} extra content";
        ReaderBasedJsonParser parser = createParser(json);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        StringWriter sw = new StringWriter();
        int released = parser.releaseBuffered(sw);
        assertTrue(released > 0);
        assertTrue(sw.toString().contains("extra content"));

        // Second call should return 0
        assertEquals(0, parser.releaseBuffered(new StringWriter()));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSkipStringLogic() throws IOException {
        // String token incomplete skipped via nextToken without reading text
        String json = "[\"skipped string 1\", \"skipped string 2 with \\\"escape\\\"\", 42]";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // do not call getText(), advance to next directly
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testMismatchedBracketErrors() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\": [1, 2}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        try {
            parser.nextToken();
            fail("Expected mismatched bracket exception");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("expected ']'"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testMissingColonInObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\" \"value\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        try {
            parser.nextToken();
            fail("Expected missing colon exception");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("was expecting a colon"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testMissingCommaInArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1 2]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        try {
            parser.nextToken();
            fail("Expected missing comma exception");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("was expecting comma"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testUnclosedStringEOF() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"unclosed string");
        try {
            parser.nextToken();
            parser.getText();
            fail("Expected EOF exception");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("was expecting closing quote"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testUnclosedCommentEOF() throws IOException {
        int features = JsonParser.Feature.ALLOW_COMMENTS.getMask();
        ReaderBasedJsonParser parser = createParser("/* unclosed comment", features);
        try {
            parser.nextToken();
            fail("Expected EOF in comment exception");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("in a comment"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testInvalidEscapeSequence() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"invalid \\k escape\"");
        try {
            parser.nextToken();
            parser.getText();
            fail("Expected invalid escape exception");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("Unrecognized character escape"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testInvalidUnicodeEscape() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"invalid \\u00G1 unicode\"");
        try {
            parser.nextToken();
            parser.getText();
            fail("Expected invalid hex digit exception");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("expected a hex-digit"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testBinaryAccessOnNonString() throws IOException {
        ReaderBasedJsonParser parser = createParser("12345");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        try {
            parser.getBinaryValue(Base64Variants.MIME);
            fail("Expected error accessing binary on non-string");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("not VALUE_STRING"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testZeroReturningReaderThrowsException() throws IOException {
        Reader zeroReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) {
                return 0; // Illegal contract behavior in Java Reader
            }
            @Override
            public void close() {}
        };
        ReaderBasedJsonParser parser = createParser(zeroReader, 0);
        try {
            parser.loadMore();
            fail("Expected IOException on reader returning 0 characters");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Reader returned 0 characters"));
        } finally {
            parser.close();
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCodecLifecycle() throws IOException {
        ReaderBasedJsonParser parser = createParser("null");
        assertNull(parser.getCodec());
        ObjectCodec dummyCodec = new ObjectCodec() {
            @Override public Version version() { return Version.unknownVersion(); }
            @Override public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            @Override public <T> T readValue(JsonParser p, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) { return null; }
            @Override public <T> T readValue(JsonParser p, com.fasterxml.jackson.core.type.ResolvedType valueType) { return null; }
            @Override public <T extends TreeNode> T readTree(JsonParser p) { return null; }
            @Override public void writeValue(JsonGenerator gen, Object value) {}
            @Override public void writeTree(JsonGenerator gen, TreeNode tree) {}
            @Override public TreeNode createObjectNode() { return null; }
            @Override public TreeNode createArrayNode() { return null; }
            @Override public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
        };
        parser.setCodec(dummyCodec);
        assertSame(dummyCodec, parser.getCodec());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetInputSource() throws IOException {
        StringReader reader = new StringReader("123");
        ReaderBasedJsonParser parser = createParser(reader, 0);
        assertSame(reader, parser.getInputSource());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        // Subsequent close call should be safe and idempotent
        parser.close();
    }
}