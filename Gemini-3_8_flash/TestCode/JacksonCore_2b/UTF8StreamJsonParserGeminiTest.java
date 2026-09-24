package com.fasterxml.jackson.core.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.BytesToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------------------
 * Target Class: UTF8StreamJsonParser
 * Known Defect (Defects4J):
 *   - TestParserErrorHandling::testMangledNumbersBytes -> Number parsing fails to enforce separating
 *     space between root values and trailing characters, erroneously returning VALUE_NUMBER_INT
 *     instead of throwing JsonParseException (e.g., input "123a").
 *
 * Branch & Equivalence Partitions Targeted:
 * - Partition A: Core Functional Logic & State Transitions
 *     * Standard token stream: objects, arrays, integers, decimals, booleans, null.
 *     * Fast-path name matching (nextFieldName(SerializableString)), nextIntValue, nextLongValue,
 *       nextTextValue, nextBooleanValue.
 *     * Base64 decoding: standard MIME, chunked buffer reading, padding variants.
 * - Partition B: Boundary Value Analysis (BVA) & Buffer Extremes
 *     * Short/empty inputs, single character boundaries.
 *     * 1-byte, 2-byte, 3-byte, and 4-byte UTF-8 sequences in strings and field names.
 *     * Small input buffers forcing split tokens across loadMore() and _parserNumber2().
 *     * Long and medium field names triggering parseMediumName, parseLongName, and growArrayBy.
 * - Partition C: Defect-Targeted Branch Zone
 *     * Mangled numbers at root level without whitespace: "123a", "123.456foo", "2e+foo".
 *     * Negative sign without subsequent digits: "-".
 * - Partition D: Exception & Defensive Guard Paths
 *     * Missing colons, unmatched array/object end markers (']', '}').
 *     * Premature EOF in string, comment, escape sequence, and field name.
 *     * Comments (C-style, C++ line, YAML #) with and without feature enablement.
 *     * Non-standard features: single-quotes, unquoted field names, leading zeros, NaN/Infinity.
 *     * Invalid UTF-8 start and continuation bytes.
 * - Partition E: Object Lifecycle & Contract Integrity
 *     * releaseBuffered(OutputStream), getInputSource(), getCodec()/setCodec(), close() cleanup.
 */
public class UTF8StreamJsonParserGeminiTest {

    private UTF8StreamJsonParser createParser(String doc) throws IOException {
        return createParser(doc.getBytes("UTF-8"));
    }

    private UTF8StreamJsonParser createParser(byte[] bytes) throws IOException {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        BytesToNameCanonicalizer sym = BytesToNameCanonicalizer.createRoot(1);
        return new UTF8StreamJsonParser(ctxt, 0, new ByteArrayInputStream(bytes), null, sym, bytes, 0, bytes.length, false);
    }

    private JsonParser createFactoryParser(String doc, JsonParser.Feature... features) throws IOException {
        JsonFactory jf = new JsonFactory();
        for (JsonParser.Feature f : features) {
            jf.enable(f);
        }
        return jf.createParser(new ByteArrayInputStream(doc.getBytes("UTF-8")));
    }

    // =================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =================================================================================================

    @Test(timeout = 4000)
    public void testStandardJsonStructureAndDataRetrieval() throws IOException {
        String json = "{\"name\":\"Alice\",\"age\":30,\"weight\":65.5,\"active\":true,\"history\":null,\"scores\":[10,20]}";
        UTF8StreamJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.nextFieldName(new SerializedString("name")));
        assertEquals("name", parser.getCurrentName());
        assertEquals("Alice", parser.nextTextValue());
        assertEquals("Alice", parser.getText());
        assertNotNull(parser.getTextCharacters());
        assertEquals(5, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());

        assertTrue(parser.nextFieldName(new SerializedString("age")));
        assertEquals(30, parser.nextIntValue(0));
        assertEquals("30", parser.getValueAsString());

        assertFalse(parser.nextFieldName(new SerializedString("wrong")));
        assertEquals("weight", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(65.5, parser.getDoubleValue(), 0.001);

        assertEquals("active", parser.nextFieldName());
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());

        assertEquals("history", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextTextValue());

        assertEquals("scores", parser.nextFieldName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(10L, parser.nextLongValue(0L));
        assertEquals(20, parser.nextIntValue(0));
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testBase64BinaryValueDecoding() throws IOException {
        String base64Data = "SGVsbG8gV29ybGQh"; // "Hello World!"
        String json = "[\"" + base64Data + "\"]";
        UTF8StreamJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        assertNotNull(binary);
        assertEquals("Hello World!", new String(binary, "UTF-8"));

        // Incremental streaming read
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = parser.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(12, bytesRead);
        assertEquals("Hello World!", out.toString("UTF-8"));

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testBase64WithWhitespaceAndPaddingVariants() throws IOException {
        String json = "[\"  S  GVs bG8 = \"]"; // "Hello" (5 bytes, 1 padding char)
        UTF8StreamJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        assertEquals("Hello", new String(binary, "UTF-8"));
        parser.close();
    }

    // =================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =================================================================================================

    @Test(timeout = 4000)
    public void testEmptyDocumentAndWhitespace() throws IOException {
        UTF8StreamJsonParser parser = createParser("   \n\r\t   ");
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testEmptyCollectionsAndStrings() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"\":\"\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testLongAndMediumFieldNames() throws IOException {
        // Triggers parseMediumName (5-8 chars) and parseLongName (> 8 chars)
        String json = "{\"fiveB\":\"v1\",\"eightByt\":\"v2\",\"veryLongFieldNameExceedingEightBytes\":\"v3\"}";
        UTF8StreamJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("fiveB", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("eightByt", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("veryLongFieldNameExceedingEightBytes", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("v3", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testMultiByteUtf8InFieldsAndValues() throws IOException {
        // 2-byte (\u00A9), 3-byte (\u4E16\u754C), 4-byte surrogate pair (\uD83D\uDE00)
        String json = "{\"\\u00A9_\\u4E16\\u754C\":\"\\uD83D\\uDE00\"}";
        UTF8StreamJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("\u00A9_\u4E16\u754C", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\uD83D\uDE00", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNumbersSplitAcrossInputBufferBoundaries() throws IOException {
        byte[] data = "12345678901234567890".getBytes("UTF-8");
        // Use a tiny 4-byte buffer to force loadMore() and _parserNumber2 execution
        InputStream in = new ByteArrayInputStream(data);
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        BytesToNameCanonicalizer sym = BytesToNameCanonicalizer.createRoot(1);
        UTF8StreamJsonParser parser = new UTF8StreamJsonParser(ctxt, 0, in, null, sym, new byte[4], 0, 0, false);

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("12345678901234567890", parser.getText());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testEscapedCharacters() throws IOException {
        String json = "[\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t \\u0041\"]";
        UTF8StreamJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\" \\ / \b \f \n \r \t A", parser.getText());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGrowArrayByUtility() {
        int[] original = null;
        int[] grown = UTF8StreamJsonParser.growArrayBy(original, 4);
        assertNotNull(grown);
        assertEquals(4, grown.length);

        int[] furtherGrown = UTF8StreamJsonParser.growArrayBy(grown, 8);
        assertEquals(12, furtherGrown.length);
    }

    // =================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J: testMangledNumbersBytes)
    // =================================================================================================

    @Test(timeout = 4000)
    public void testMangledNumbersBytes() throws IOException {
        // Direct replication of Defects4J failure: root level number followed immediately by letters.
        // The defective implementation erroneously accepts "123" and ignores trailing "a",
        // returning VALUE_NUMBER_INT instead of throwing JsonParseException.
        JsonParser parser = createFactoryParser("123a");
        try {
            JsonToken t = parser.nextToken();
            fail("Should have gotten an exception; instead got token: " + t);
        } catch (JsonParseException expected) {
            assertNotNull(expected.getMessage());
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testMangledFloatExponentBytes() throws IOException {
        JsonParser parser = createFactoryParser("1.0e+X");
        try {
            JsonToken t = parser.nextToken();
            fail("Should have gotten an exception; instead got token: " + t);
        } catch (JsonParseException expected) {
            assertNotNull(expected.getMessage());
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testMangledLeadingMinusWithoutDigits() throws IOException {
        JsonParser parser = createFactoryParser("-");
        try {
            JsonToken t = parser.nextToken();
            fail("Expected JsonParseException for lone minus, but got: " + t);
        } catch (JsonParseException expected) {
            assertNotNull(expected.getMessage());
        } finally {
            parser.close();
        }
    }

    // =================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =================================================================================================

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMismatchedClosingArrayMarker() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"key\": 123]");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // Mismatched ]
        parser.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMismatchedClosingObjectMarker() throws IOException {
        UTF8StreamJsonParser parser = createParser("[123}");
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // Mismatched }
        parser.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMissingColonBetweenFieldAndValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"key\" \"value\"}");
        parser.nextToken();
        parser.nextToken();
        parser.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnterminatedString() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"unterminated");
        parser.nextToken();
        parser.getText();
        parser.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnterminatedComment() throws IOException {
        JsonParser parser = createFactoryParser("/* unclosed comment ", JsonParser.Feature.ALLOW_COMMENTS);
        parser.nextToken();
        parser.close();
    }

    @Test(timeout = 4000)
    public void testCommentsEnabled() throws IOException {
        String json = "/* header comment */ { // line comment\n \"key\": # yaml comment\n 123 /* inline */ }";
        JsonParser parser = createFactoryParser(json,
                JsonParser.Feature.ALLOW_COMMENTS,
                JsonParser.Feature.ALLOW_YAML_COMMENTS);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testCommentsDisabledThrowsException() throws IOException {
        UTF8StreamJsonParser parser = createParser("// comment\n 123");
        parser.nextToken();
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSingleQuotesEnabled() throws IOException {
        JsonParser parser = createFactoryParser("{'name': 'Bob'}", JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Bob", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testSingleQuotesDisabledThrowsException() throws IOException {
        UTF8StreamJsonParser parser = createParser("{'name': 'Bob'}");
        parser.nextToken();
        parser.nextToken();
        parser.close();
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNamesEnabled() throws IOException {
        JsonParser parser = createFactoryParser("{foo: 100}", JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("foo", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testLeadingZerosDisabledThrowsException() throws IOException {
        UTF8StreamJsonParser parser = createParser("007");
        parser.nextToken();
        parser.close();
    }

    @Test(timeout = 4000)
    public void testLeadingZerosEnabled() throws IOException {
        JsonParser parser = createFactoryParser("007", JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(7, parser.getIntValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNonNumericNumbersEnabled() throws IOException {
        JsonParser parser = createFactoryParser("[NaN, Infinity, -Infinity]", JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidUtf8InitialByte() throws IOException {
        byte[] malformed = new byte[] { (byte) 0xFF, 0x20 };
        UTF8StreamJsonParser parser = createParser(malformed);
        parser.nextToken();
        parser.close();
    }

    // =================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =================================================================================================

    @Test(timeout = 4000)
    public void testReleaseBuffered() throws IOException {
        byte[] data = "{\"msg\":\"hello\"}".getBytes("UTF-8");
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        BytesToNameCanonicalizer sym = BytesToNameCanonicalizer.createRoot(1);
        UTF8StreamJsonParser parser = new UTF8StreamJsonParser(ctxt, 0, new ByteArrayInputStream(data), null, sym, data, 0, data.length, false);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int released = parser.releaseBuffered(out);
        assertEquals(data.length, released);
        assertArrayEquals(data, out.toByteArray());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testInputSourceAndCodecAccessors() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("123".getBytes("UTF-8"));
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        BytesToNameCanonicalizer sym = BytesToNameCanonicalizer.createRoot(1);
        UTF8StreamJsonParser parser = new UTF8StreamJsonParser(ctxt, 0, in, null, sym, new byte[10], 0, 0, false);

        assertSame(in, parser.getInputSource());
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
            @Override public <T> com.fasterxml.jackson.core.type.ResolvedType constructType(java.lang.reflect.Type type) { return null; }
            @Override public <T> java.util.Iterator<T> readValues(JsonParser p, Class<T> valueType) { return null; }
            @Override public <T> java.util.Iterator<T> readValues(JsonParser p, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) { return null; }
            @Override public <T> java.util.Iterator<T> readValues(JsonParser p, com.fasterxml.jackson.core.type.ResolvedType valueType) { return null; }
        };

        parser.setCodec(dummyCodec);
        assertSame(dummyCodec, parser.getCodec());

        parser.close();
        assertTrue(parser.isClosed());
    }
}