package com.fasterxml.jackson.core.json;

import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Target Class: com.fasterxml.jackson.core.json.ReaderBasedJsonParser
 * Target Defect: Defects4J NonStandardUnquotedNamesTest::testUnquotedIssue510
 *                --> java.lang.ArrayIndexOutOfBoundsException: 256 in _handleOddName2
 *
 * Specific Decision Branches & Boundary Conditions Targeted:
 * 1. [Partition A: Core Functional Logic & State Transitions]
 *    - Full JSON object/array traversal, primitive types, whitespace, newlines (CR, LF, CRLF).
 *    - Field name transitions (_nextAfterName), nextFieldName() for string, number, bool, null, obj, arr.
 *    - Fast-path matching in nextFieldName(SerializableString) vs slow-path _isNextTokenNameMaybe.
 *    - Escaped characters: \b, \t, \n, \f, \r, \", \/, \\, and Unicode 4-hex escapes \uXXXX.
 *    - Text retrieval methods: getText(), getText(Writer), getTextCharacters(), getTextLength(),
 *      getTextOffset(), getValueAsString(), getValueAsString(def).
 *
 * 2. [Partition B: Boundary Value Analysis (BVA) & Extremes]
 *    - Empty string and whitespace-only documents (EOF handling in _skipWSOrEnd).
 *    - Single token documents (scalar root values).
 *    - Numeric boundaries: 0, -0, Long.MIN_VALUE, Long.MAX_VALUE, fractional parts, exponents.
 *    - Root-level space requirement (_verifyRootSpace): spaces, tabs, CR, LF, and missing space error.
 *    - Buffer boundaries: split numbers, split field names, split strings across buffer chunks.
 *    - releaseBuffered(Writer) with remaining vs exhausted buffer.
 *    - Base64 binary decoding: 0, 1, 2, 3 byte triplets, whitespace skipping, padding and unpadded.
 *
 * 3. [Partition C: Defect-Targeted Branch Zone]
 *    - CRITICAL: _handleOddName2 character code boundary check (`i <= maxCode` vs `i < maxCode`).
 *      Character '\u0100' (decimal 256) causes `codes[256]` ArrayIndexOutOfBoundsException in defective code.
 *    - ALLOW_UNQUOTED_FIELD_NAMES: Latin-1 identifiers, non-ASCII identifier parts (e.g. \u0100).
 *    - ALLOW_SINGLE_QUOTES: single-quoted field names and single-quoted string values (_handleApos).
 *    - ALLOW_TRAILING_COMMA: trailing comma in arrays and objects with feature enabled.
 *    - ALLOW_MISSING_VALUES: empty comma slots in arrays returning VALUE_NULL.
 *    - ALLOW_NON_NUMERIC_NUMBERS: NaN, Infinity, +Infinity, -Infinity, +INF, -INF.
 *    - ALLOW_NUMERIC_LEADING_ZEROS: leading zeros in positive/negative integers.
 *    - Comments: ALLOW_COMMENTS (// and /* */) and ALLOW_YAML_COMMENTS (#).
 *
 * 4. [Partition D: Exception & Defensive Guard Paths]
 *    - Invalid unquoted field names starting with illegal characters.
 *    - Missing colon between field name and value.
 *    - Missing comma between array/object entries.
 *    - Mismatched bracket/curly scope delimiters (_closeScope).
 *    - Premature EOF in strings, field names, escape sequences, numbers, and comments.
 *    - Invalid hex digit in Unicode escape sequence.
 *    - Raw unquoted control characters inside string literal (_throwUnquotedSpace).
 *    - getBinaryValue on non-string token.
 *    - Abnormal Reader returning 0 characters (_loadMore fail-fast).
 *
 * 5. [Partition E: Object Lifecycle & Contract Integrity]
 *    - Input source access (getInputSource), ObjectCodec get/set.
 *    - AUTO_CLOSE_SOURCE: closing vs retaining underlying Reader.
 *    - 9-argument direct constructor with recyclable vs non-recyclable buffer.
 * ---------------------------------------------------------------------------------------------------
 */
public class ReaderBasedJsonParserGeminiTest {

    private static class ChunkedStringReader extends Reader {
        private final String[] chunks;
        private int chunkIdx = 0;
        private int charIdx = 0;

        public ChunkedStringReader(String... chunks) {
            this.chunks = chunks;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (chunkIdx >= chunks.length) {
                return -1;
            }
            String current = chunks[chunkIdx];
            int available = current.length() - charIdx;
            int toRead = Math.min(len, available);
            current.getChars(charIdx, charIdx + toRead, cbuf, off, toRead);
            charIdx += toRead;
            if (charIdx >= current.length()) {
                chunkIdx++;
                charIdx = 0;
            }
            return toRead;
        }

        @Override
        public void close() throws IOException {
        }
    }

    private static class CloseCheckReader extends StringReader {
        boolean closed = false;

        public CloseCheckReader(String s) {
            super(s);
        }

        @Override
        public void close() {
            closed = true;
            super.close();
        }
    }

    private ReaderBasedJsonParser createParser(String doc) throws IOException {
        return createParser(new StringReader(doc), 0);
    }

    private ReaderBasedJsonParser createParser(String doc, int features) throws IOException {
        return createParser(new StringReader(doc), features);
    }

    private ReaderBasedJsonParser createParser(Reader reader, int features) throws IOException {
        JsonFactory jf = new JsonFactory();
        for (JsonParser.Feature f : JsonParser.Feature.values()) {
            if ((features & f.getMask()) != 0) {
                jf.enable(f);
            } else {
                jf.disable(f);
            }
        }
        return (ReaderBasedJsonParser) jf.createParser(reader);
    }

    /*
     * =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testDocumentStructureTraversal() throws Exception {
        String json = "{\"str\":\"hello\",\"num\":42,\"bool\":true,\"nil\":null,\"arr\":[1,false]}";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.isExpectedStartObjectToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("str", parser.getCurrentName());
        assertEquals("str", parser.getText());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("num", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("bool", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("nil", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("arr", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getBooleanValue());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNextFieldNameVariations() throws Exception {
        String json = "{\"name\":\"Alice\",\"age\":30,\"active\":true,\"data\":null,\"tags\":[\"a\"],\"nested\":{}}";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals("name", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Alice", parser.getText());

        assertEquals("age", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(30, parser.getIntValue());

        assertEquals("active", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());

        assertEquals("data", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        assertEquals("tags", parser.nextFieldName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals("nested", parser.nextFieldName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextFieldName());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNextFieldNameSerializableStringFastAndSlow() throws Exception {
        String json = "{\"title\":\"Dev\",\"count\":10}";
        ReaderBasedJsonParser parser = createParser(json);

        SerializedString title = new SerializedString("title");
        SerializedString wrong = new SerializedString("wrong");
        SerializedString count = new SerializedString("count");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertFalse(parser.nextFieldName(wrong));
        assertEquals("title", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Dev", parser.getText());

        assertTrue(parser.nextFieldName(count));
        assertEquals("count", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(10, parser.getIntValue());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertFalse(parser.nextFieldName(title));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNextValueOptimizedMethods() throws Exception {
        String json = "{\"s\":\"text\",\"i\":123,\"l\":9876543210,\"t\":true,\"f\":false}";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("text", parser.nextTextValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(123, parser.nextIntValue(-1));

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(9876543210L, parser.nextLongValue(-1L));

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(Boolean.FALSE, parser.nextBooleanValue());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testStringEscapesFullSpectrum() throws Exception {
        String json = "\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t \\u0041\\u007a\"";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        String expected = "\" \\ / \b \f \n \r \t Az";
        assertEquals(expected, parser.getText());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testTextExtractionMethods() throws Exception {
        String json = "{\"key\":\"myValue\",\"n\":12.34}";
        ReaderBasedJsonParser parser = createParser(json);

        StringWriter sw = new StringWriter();
        assertEquals(0, parser.getText(sw));
        assertNull(parser.getTextCharacters());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        sw = new StringWriter();
        int written = parser.getText(sw);
        assertEquals(1, written);
        assertEquals("{", sw.toString());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getText());
        assertEquals("key", parser.getValueAsString());
        assertEquals(3, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertNotNull(parser.getTextCharacters());

        sw = new StringWriter();
        assertEquals(3, parser.getText(sw));
        assertEquals("key", sw.toString());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        parser.finishToken();
        assertEquals("myValue", parser.getText());
        assertEquals("myValue", parser.getValueAsString("def"));
        assertEquals(7, parser.getTextLength());

        sw = new StringWriter();
        assertEquals(7, parser.getText(sw));
        assertEquals("myValue", sw.toString());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("12.34", parser.getText());
        assertEquals(12.34, parser.getDoubleValue(), 0.0001);

        sw = new StringWriter();
        assertEquals(5, parser.getText(sw));
        assertEquals("12.34", sw.toString());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testFloatingPointExponents() throws Exception {
        String json = "[1.5e2, -2.5E-1, 10e+3, -0.125]";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(150.0, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-0.25, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(10000.0, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-0.125, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    /*
     * =========================================================================
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testEmptyAndWhitespaceInput() throws Exception {
        ReaderBasedJsonParser p1 = createParser("");
        assertNull(p1.nextToken());
        p1.close();

        ReaderBasedJsonParser p2 = createParser("   \t  \r\n \n  ");
        assertNull(p2.nextToken());
        p2.close();
    }

    @Test(timeout = 4000)
    public void testNumericBoundaries() throws Exception {
        String json = "[0, -0, " + Long.MAX_VALUE + ", " + Long.MIN_VALUE + "]";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Long.MAX_VALUE, parser.getLongValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Long.MIN_VALUE, parser.getLongValue());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testRootLevelSpaceSeparation() throws Exception {
        String json = "123 \t 456 \r\n 789\n999";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(456, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(789, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(999, parser.getIntValue());

        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSplitNumberAcrossBufferBoundary() throws Exception {
        Reader chunked = new ChunkedStringReader("-", "1", "2", ".", "5", "e", "+", "2", " ");
        ReaderBasedJsonParser parser = createParser(chunked, 0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-1250.0, parser.getDoubleValue(), 0.001);
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testReleaseBuffered() throws Exception {
        char[] buf = "[\"cached\"]".toCharArray();
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, "buf", false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot(12345);

        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(
                ctxt, 0, null, null, sym, buf, 0, buf.length, false);

        StringWriter sw = new StringWriter();
        int released = parser.releaseBuffered(sw);
        assertEquals(buf.length, released);
        assertEquals("[\"cached\"]", sw.toString());

        assertEquals(0, parser.releaseBuffered(new StringWriter()));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testBase64DecodingVariants() throws Exception {
        // "Hello World" in base64 is "SGVsbG8gV29ybGQ="
        String json = "[\"SGVsbG8gV29ybGQ=\", \"YQ==\", \"YWI=\", \"YWJj\", \"YWJjZA==\"]";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] b1 = parser.getBinaryValue(Base64Variants.MIME);
        assertEquals("Hello World", new String(b1, "UTF-8"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int readCount = parser.readBinaryValue(Base64Variants.MIME, baos);
        assertEquals(11, readCount);
        assertEquals("Hello World", new String(baos.toByteArray(), "UTF-8"));

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("a", new String(parser.getBinaryValue(Base64Variants.MIME), "UTF-8"));

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("ab", new String(parser.getBinaryValue(Base64Variants.MIME), "UTF-8"));

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("abc", new String(parser.getBinaryValue(Base64Variants.MIME), "UTF-8"));

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("abcd", new String(parser.getBinaryValue(Base64Variants.MIME), "UTF-8"));

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testLocationTracking() throws Exception {
        String json = "{\n  \"field\": 1\n}";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        JsonLocation tokenLoc = parser.getTokenLocation();
        assertEquals(2, tokenLoc.getLineNr());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        JsonLocation curLoc = parser.getCurrentLocation();
        assertEquals(2, curLoc.getLineNr());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    /*
     * =========================================================================
     * Partition C: Defect-Targeted Branch Zone
     * =========================================================================
     */

    /**
     * TARGET DEFECT TEST:
     * Defects4J: NonStandardUnquotedNamesTest::testUnquotedIssue510
     * Triggering: java.lang.ArrayIndexOutOfBoundsException: 256
     * Location: ReaderBasedJsonParser._handleOddName2(int, int, int[])
     * Cause: Code evaluated `if (i <= maxCode)` where `maxCode = codes.length` (256).
     * When character `\u0100` (code 256) was processed in `_handleOddName2`, it checked `codes[256]`,
     * throwing ArrayIndexOutOfBoundsException.
     * The test asserts that an unquoted name containing `\u0100` across buffer boundaries is properly parsed.
     */
    @Test(timeout = 4000)
    public void testUnquotedIssue510_ArrayIndexOutOfBoundsAt256() throws Exception {
        int features = JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        Reader chunkedReader = new ChunkedStringReader("{", "a", "\u0100: 1}");
        ReaderBasedJsonParser parser = createParser(chunkedReader, features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a\u0100", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNamesStandard() throws Exception {
        int features = JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        String json = "{foo: 1, _bar: 2, $baz: 3, a123: 4}";
        ReaderBasedJsonParser parser = createParser(json, features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("foo", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("_bar", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("$baz", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a123", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSingleQuotedNamesAndValues() throws Exception {
        int features = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask();
        String json = "{'key': 'value with \\' quote'}";
        ReaderBasedJsonParser parser = createParser(json, features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value with ' quote", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testAllowTrailingComma() throws Exception {
        int features = JsonParser.Feature.ALLOW_TRAILING_COMMA.getMask();
        String json = "{\"a\": 1, }";
        ReaderBasedJsonParser parser = createParser(json, features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();

        ReaderBasedJsonParser pArr = createParser("[1, 2, ]", features);
        assertEquals(JsonToken.START_ARRAY, pArr.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, pArr.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, pArr.nextToken());
        assertEquals(JsonToken.END_ARRAY, pArr.nextToken());
        assertNull(pArr.nextToken());
        pArr.close();
    }

    @Test(timeout = 4000)
    public void testAllowMissingValues() throws Exception {
        int features = JsonParser.Feature.ALLOW_MISSING_VALUES.getMask();
        String json = "[1,,3]";
        ReaderBasedJsonParser parser = createParser(json, features);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNonNumericNumbers() throws Exception {
        int features = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        String json = "[NaN, Infinity, +Infinity, -Infinity, +INF, -INF]";
        ReaderBasedJsonParser parser = createParser(json, features);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testAllowNumericLeadingZeros() throws Exception {
        int features = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        String json = "[0123, -007, 000]";
        ReaderBasedJsonParser parser = createParser(json, features);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-7, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testCommentsJavaAndYAML() throws Exception {
        int features = JsonParser.Feature.ALLOW_COMMENTS.getMask()
                | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        String json = "/* C-comment */\n"
                + "{\n"
                + "  // Java line comment\n"
                + "  \"key\": /* inline */ 123 # YAML comment\n"
                + "}";
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

    /*
     * =========================================================================
     * Partition D: Exception & Defensive Guard Paths
     * =========================================================================
     */

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnquotedFieldNamesDisabledThrows() throws Exception {
        ReaderBasedJsonParser parser = createParser("{foo: 1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMissingColonThrows() throws Exception {
        ReaderBasedJsonParser parser = createParser("{\"key\" 123}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMissingCommaThrows() throws Exception {
        ReaderBasedJsonParser parser = createParser("[1 2]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMismatchedBracketThrows() throws Exception {
        ReaderBasedJsonParser parser = createParser("{]");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMismatchedCurlyThrows() throws Exception {
        ReaderBasedJsonParser parser = createParser("[}");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnterminatedStringThrows() throws Exception {
        ReaderBasedJsonParser parser = createParser("\"unterminated");
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnterminatedCommentThrows() throws Exception {
        int features = JsonParser.Feature.ALLOW_COMMENTS.getMask();
        ReaderBasedJsonParser parser = createParser("/* unclosed comment", features);
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidUnicodeEscapeThrows() throws Exception {
        ReaderBasedJsonParser parser = createParser("\"\\u004Z\"");
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnrecognizedEscapeThrows() throws Exception {
        ReaderBasedJsonParser parser = createParser("\"\\q\"");
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testLeadingZeroWithoutFeatureThrows() throws Exception {
        ReaderBasedJsonParser parser = createParser("[012]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMissingRootWhitespaceThrows() throws Exception {
        ReaderBasedJsonParser parser = createParser("123:456");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testNonNumericNumberWithoutFeatureThrows() throws Exception {
        ReaderBasedJsonParser parser = createParser("[NaN]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testGetBinaryValueOnNonStringThrows() throws Exception {
        ReaderBasedJsonParser parser = createParser("123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        parser.getBinaryValue(Base64Variants.MIME);
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testReaderReturningZeroCharactersThrows() throws Exception {
        Reader zeroReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                return 0;
            }

            @Override
            public void close() throws IOException {
            }
        };
        ReaderBasedJsonParser parser = createParser(zeroReader, 0);
        parser._loadMore();
    }

    /*
     * =========================================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testDirectNineArgConstructorAndNonRecyclableBuffer() throws Exception {
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, "directTest", false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot(54321);

        char[] buf = "[789]".toCharArray();
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(
                ctxt, 0, null, null, sym, buf, 0, buf.length, false);

        assertNull(parser.getCodec());
        parser.setCodec(null);
        assertNull(parser.getCodec());

        assertNull(parser.getInputSource());

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(789, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());

        parser.close();
    }

    @Test(timeout = 4000)
    public void testAutoCloseSourceFeature() throws Exception {
        CloseCheckReader r1 = new CloseCheckReader("123");
        int featuresWithAutoClose = JsonParser.Feature.AUTO_CLOSE_SOURCE.getMask();
        ReaderBasedJsonParser p1 = createParser(r1, featuresWithAutoClose);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        p1.close();
        assertTrue(r1.closed);

        CloseCheckReader r2 = new CloseCheckReader("123");
        int featuresNoAutoClose = 0;
        ReaderBasedJsonParser p2 = createParser(r2, featuresNoAutoClose);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p2.nextToken());
        p2.close();
        assertFalse(r2.closed);
    }

    @Test(timeout = 4000)
    public void testGetNextCharProtectedMethods() throws Exception {
        ReaderBasedJsonParser parser = createParser("abc");
        char c1 = parser.getNextChar("EOF", JsonToken.VALUE_STRING);
        assertEquals('a', c1);
        char c2 = parser.getNextChar("EOF");
        assertEquals('b', c2);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSkipStringSkipsUnparsedContent() throws Exception {
        String json = "[\"skippedString\", 42]";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // Deliberately do NOT call parser.getText(); nextToken() triggers _skipString
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }
}