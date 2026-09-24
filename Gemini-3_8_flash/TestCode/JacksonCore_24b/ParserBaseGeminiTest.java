package com.fasterxml.jackson.core.base;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.core.base.ParserBase
 *
 * 1. Life-cycle & State:
 *    - Constructor: IOContext, DupDetector initialization, parsing context hierarchy.
 *    - Features: enable/disable STRICT_DUPLICATE_DETECTION, setFeatureMask, overrideStdFeatures.
 *    - Parsing Context: getCurrentValue/setCurrentValue, getCurrentName/overrideCurrentName.
 *    - Close & Buffers: close() idempotency, _releaseBuffers, _ioContext release.
 *    - Location tracking: getTokenLocation(), getCurrentLocation(), getTokenCharacterOffset/LineNr/ColNr.
 *
 * 2. Numeric Parsing & Representation Branches:
 *    - reset, resetInt, resetFloat, resetAsNaN, isNaN.
 *    - _parseNumericValue for:
 *      * INT: len <= 9 (contentsAsInt), len == 10 (boundary check with MIN_INT_L / MAX_INT_L),
 *             10 < len <= 18 (contentsAsLong), len > 18 (_parseSlowInt).
 *      * FLOAT: NR_BIGDECIMAL vs NR_DOUBLE via _parseSlowFloat.
 *      * InLongRange checking and out-of-range integral reporting (_reportTooLongIntegral).
 *    - Conversions:
 *      * convertNumberToInt: from LONG (overflow check), BIGINT, DOUBLE, BIGDECIMAL.
 *      * convertNumberToLong: from INT, BIGINT, DOUBLE, BIGDECIMAL.
 *      * convertNumberToBigInteger: from BIGDECIMAL, LONG, INT, DOUBLE.
 *      * convertNumberToDouble: from BIGDECIMAL, BIGINT, LONG, INT.
 *      * convertNumberToBigDecimal: from DOUBLE, BIGINT, LONG, INT.
 *
 * 3. Base64 & Text Buffer:
 *    - getBinaryValue(Base64Variant) caching and validation against non-VALUE_STRING.
 *    - _decodeBase64Escape with whitespace skip (index 0 vs index > 0), padding char checks.
 *    - reportInvalidBase64Char branch conditions (whitespace, padding, ISO control, generic char).
 *
 * 4. Ground Truth Defects4J Regression Targeting:
 *    - Numeric overflow coercions: int overflow (2147483648), long overflow (9223372036854775817, -9223372036854775809),
 *      and malicious extreme digit lengths (>18 chars / 199999 digits).
 *    - Verifies _reportTooLongIntegral and out of range errors throw JsonParseException.
 */

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.ContentReference;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class ParserBaseGeminiTest {

    /**
     * Concrete testable harness extending ParserBase.
     */
    private static class TestableParserBase extends ParserBase {
        private String _text;
        private boolean _closeInputCalled = false;
        private char _escapedChar = 'A';

        public TestableParserBase(IOContext ctxt, int features) {
            super(ctxt, features);
        }

        public void setCurrToken(JsonToken t) {
            this._currToken = t;
        }

        public void setText(String text) {
            this._text = text;
            this._textBuffer.resetWithString(text);
        }

        public void setNumberNegative(boolean neg) {
            this._numberNegative = neg;
        }

        public void setIntLength(int len) {
            this._intLength = len;
        }

        public void setEscapedChar(char c) {
            this._escapedChar = c;
        }

        public void setInputPointers(int ptr, int end) {
            this._inputPtr = ptr;
            this._inputEnd = end;
        }

        public void setLocationInfo(long total, int row, int col, int rowStart, long processed) {
            this._tokenInputTotal = total;
            this._tokenInputRow = row;
            this._tokenInputCol = col;
            this._currInputRow = row;
            this._currInputRowStart = rowStart;
            this._currInputProcessed = processed;
        }

        @Override
        public String getText() throws IOException {
            if (_text != null) {
                return _text;
            }
            return _textBuffer.contentsAsString();
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            return getText().toCharArray();
        }

        @Override
        public int getTextLength() throws IOException {
            return getText().length();
        }

        @Override
        public int getTextOffset() throws IOException {
            return 0;
        }

        @Override
        protected char _decodeEscaped() throws IOException {
            return _escapedChar;
        }

        @Override
        protected void _closeInput() throws IOException {
            _closeInputCalled = true;
        }

        @Override
        public JsonToken nextToken() throws IOException {
            return null;
        }
    }

    private TestableParserBase createParser(int features) {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, ContentReference.rawReference("test-source"), false);
        return new TestableParserBase(ctxt, features);
    }

    private TestableParserBase createParser() {
        return createParser(0);
    }

    /*
     * ----------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testVersionAndInitialState() {
        TestableParserBase parser = createParser();
        assertNotNull(parser.version());
        assertFalse(parser.isClosed());
        assertNotNull(parser.getParsingContext());
        assertTrue(parser.getParsingContext().inRoot());
        assertNull(parser.getCurrentValue());

        Object dummy = new Object();
        parser.setCurrentValue(dummy);
        assertSame(dummy, parser.getCurrentValue());
    }

    @Test(timeout = 4000)
    public void testFeatureManipulation() {
        TestableParserBase parser = createParser(0);
        assertFalse(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));

        parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        assertTrue(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        assertNotNull(parser.getParsingContext().getDupDetector());

        parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION); // idempotency check
        assertTrue(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));

        parser.disable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        assertFalse(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        assertNull(parser.getParsingContext().getDupDetector());

        int mask = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        parser.setFeatureMask(mask);
        assertTrue(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        assertNotNull(parser.getParsingContext().getDupDetector());

        parser.overrideStdFeatures(0, mask);
        assertFalse(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        assertNull(parser.getParsingContext().getDupDetector());
    }

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws IOException {
        TestableParserBase parser = createParser();
        parser.setInputPointers(5, 10);
        assertFalse(parser.isClosed());

        parser.close();
        assertTrue(parser.isClosed());
        assertTrue(parser._closeInputCalled);
        assertEquals(10, parser._inputPtr);

        parser._closeInputCalled = false;
        parser.close(); // subsequent call should be a no-op
        assertFalse(parser._closeInputCalled);
    }

    @Test(timeout = 4000)
    public void testLocationReporting() {
        TestableParserBase parser = createParser(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION.getMask());
        parser.setLocationInfo(100L, 5, 2, 80, 200L);
        parser.setInputPointers(95, 120);

        JsonLocation tokenLoc = parser.getTokenLocation();
        assertEquals(100L, tokenLoc.getCharOffset());
        assertEquals(5, tokenLoc.getLineNr());
        assertEquals(3, tokenLoc.getColumnNr()); // 0-based + 1 => 3
        assertEquals(100L, parser.getTokenCharacterOffset());
        assertEquals(5, parser.getTokenLineNr());
        assertEquals(3, parser.getTokenColumnNr());

        JsonLocation currLoc = parser.getCurrentLocation();
        assertEquals(200L + 95L, currLoc.getCharOffset());
        assertEquals(5, currLoc.getLineNr());
        assertEquals(95 - 80 + 1, currLoc.getColumnNr()); // 16
    }

    @Test(timeout = 4000)
    public void testTokenLocationWithNegativeCol() {
        TestableParserBase parser = createParser(0);
        parser.setLocationInfo(50L, 1, -1, 0, 0L);
        assertEquals(-1, parser.getTokenColumnNr());
    }

    @Test(timeout = 4000)
    public void testNumberResetsAndIsNaN() {
        TestableParserBase parser = createParser();

        JsonToken tInt = parser.reset(false, 5, 0, 0);
        assertEquals(JsonToken.VALUE_NUMBER_INT, tInt);
        assertFalse(parser._numberNegative);
        assertEquals(5, parser._intLength);

        JsonToken tFloat = parser.reset(true, 3, 2, 1);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, tFloat);
        assertTrue(parser._numberNegative);
        assertEquals(3, parser._intLength);
        assertEquals(2, parser._fractLength);
        assertEquals(1, parser._expLength);
        assertFalse(parser.isNaN());

        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.resetAsNaN("NaN", Double.NaN);
        assertTrue(parser.isNaN());

        parser.resetAsNaN("Infinity", Double.POSITIVE_INFINITY);
        assertTrue(parser.isNaN());

        parser.resetAsNaN("-Infinity", Double.NEGATIVE_INFINITY);
        assertTrue(parser.isNaN());

        parser.resetAsNaN("12.5", 12.5);
        assertFalse(parser.isNaN());

        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        assertFalse(parser.isNaN());
    }

    /*
     * ----------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Numeric Conversions
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testIntegerOptimizedParsings() throws IOException {
        TestableParserBase parser = createParser();

        // 1. len <= 9 (fits in standard int)
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("123456789");
        parser.setIntLength(9);
        parser.setNumberNegative(false);

        assertEquals(123456789, parser.getIntValue());
        assertEquals(NumberType.INT, parser.getNumberType());
        assertEquals(123456789, parser.getNumberValue());
        assertEquals(123456789L, parser.getLongValue());
        assertEquals(BigInteger.valueOf(123456789), parser.getBigIntegerValue());
        assertEquals(123456789.0, parser.getDoubleValue(), 0.0001);
        assertEquals(new BigDecimal("123456789"), parser.getDecimalValue());
        assertEquals(123456789.0f, parser.getFloatValue(), 0.0001f);

        // 2. len == 10, positive boundary check (MAX_INT = 2147483647)
        parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("2147483647");
        parser.setIntLength(10);
        parser.setNumberNegative(false);

        assertEquals(Integer.MAX_VALUE, parser.getIntValue());
        assertEquals(NumberType.INT, parser.getNumberType());

        // 3. len == 10, negative boundary check (MIN_INT = -2147483648)
        parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("-2147483648");
        parser.setIntLength(10);
        parser.setNumberNegative(true);

        assertEquals(Integer.MIN_VALUE, parser.getIntValue());
        assertEquals(NumberType.INT, parser.getNumberType());

        // 4. len == 10, positive overflow to long (2147483648)
        parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("2147483648");
        parser.setIntLength(10);
        parser.setNumberNegative(false);

        assertEquals(2147483648L, parser.getLongValue());
        assertEquals(NumberType.LONG, parser.getNumberType());
        assertEquals(2147483648L, parser.getNumberValue());
    }

    @Test(timeout = 4000)
    public void testLongRangeParsing() throws IOException {
        TestableParserBase parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("9223372036854775807"); // Long.MAX_VALUE (len 19)
        parser.setIntLength(19);
        parser.setNumberNegative(false);

        assertEquals(Long.MAX_VALUE, parser.getLongValue());
        assertEquals(NumberType.LONG, parser.getNumberType());

        // Long.MIN_VALUE
        parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("-9223372036854775808");
        parser.setIntLength(19);
        parser.setNumberNegative(true);

        assertEquals(Long.MIN_VALUE, parser.getLongValue());
        assertEquals(NumberType.LONG, parser.getNumberType());
    }

    @Test(timeout = 4000)
    public void testFloatAndDoubleParsing() throws IOException {
        TestableParserBase parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setText("123.456");

        assertEquals(123.456, parser.getDoubleValue(), 0.0001);
        assertEquals(NumberType.DOUBLE, parser.getNumberType());
        assertEquals(123.456, (Double) parser.getNumberValue(), 0.0001);
        assertEquals(123, parser.getIntValue());
        assertEquals(123L, parser.getLongValue());
        assertEquals(BigInteger.valueOf(123), parser.getBigIntegerValue());

        // Float with explicit BigDecimal request
        parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setText("999999999999999999.8888888888");

        assertEquals(new BigDecimal("999999999999999999.8888888888"), parser.getDecimalValue());
        assertEquals(NumberType.BIG_DECIMAL, parser.getNumberType());
        assertEquals(new BigDecimal("999999999999999999.8888888888"), parser.getNumberValue());
    }

    /*
     * ----------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testDefectIntOverflowFailing() {
        TestableParserBase parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("2147483648");
        parser.setIntLength(10);
        parser.setNumberNegative(false);

        try {
            parser.getIntValue();
            fail("Expected JsonParseException on int overflow");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("out of range of int"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testDefectLongOverflowFailing() {
        TestableParserBase parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        // Exceeds Long.MAX_VALUE (9223372036854775807)
        parser.setText("9223372036854775817");
        parser.setIntLength(19);
        parser.setNumberNegative(false);

        try {
            parser.getLongValue();
            fail("Expected JsonParseException on long overflow");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("out of range of long"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testDefectNegativeLongOverflowFailing() {
        TestableParserBase parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        // Exceeds Long.MIN_VALUE (-9223372036854775808)
        parser.setText("-9223372036854775809");
        parser.setIntLength(19);
        parser.setNumberNegative(true);

        try {
            parser.getLongValue();
            fail("Expected JsonParseException on negative long overflow");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("out of range of long"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testDefectMaliciousIntegralOverflow() {
        TestableParserBase parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append('9');
        }
        String hugeNumber = sb.toString();
        parser.setText(hugeNumber);
        parser.setIntLength(200);
        parser.setNumberNegative(false);

        try {
            parser.getIntValue();
            fail("Expected JsonParseException on massive int overflow");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("out of range of int"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }

        try {
            parser.getLongValue();
            fail("Expected JsonParseException on massive long overflow");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("out of range of long"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testCoercionFromDoubleOverflow() throws IOException {
        TestableParserBase parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setText("1e30");

        try {
            parser.getIntValue();
            fail("Expected overflow when converting large double to int");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Overflow"));
        }

        try {
            parser.getLongValue();
            fail("Expected overflow when converting large double to long");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testCoercionFromBigDecimalOverflow() throws IOException {
        TestableParserBase parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setText("123456789012345678901234567890.5");
        parser.getDecimalValue(); // force BigDecimal parsing

        try {
            parser.getIntValue();
            fail("Expected overflow converting massive BigDecimal to int");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Overflow"));
        }

        try {
            parser.getLongValue();
            fail("Expected overflow converting massive BigDecimal to long");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Overflow"));
        }
    }

    /*
     * ----------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * ----------------------------------------------------------------------
     */

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testNonNumericTokenThrowsOnNumberAccessors() throws IOException {
        TestableParserBase parser = createParser();
        parser.setCurrToken(JsonToken.START_OBJECT);
        parser.getIntValue();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testGetBinaryValueThrowsOnNonStringToken() throws IOException {
        TestableParserBase parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.getBinaryValue(Base64Variants.MIME);
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueSuccess() throws IOException {
        TestableParserBase parser = createParser();
        parser.setCurrToken(JsonToken.VALUE_STRING);
        // Base64 for "Hello World" => "SGVsbG8gV29ybGQ="
        parser.setText("SGVsbG8gV29ybGQ=");

        byte[] bytes = parser.getBinaryValue(Base64Variants.MIME);
        assertNotNull(bytes);
        assertEquals("Hello World", new String(bytes, "UTF-8"));

        // Subsequent call returns cached array
        assertSame(bytes, parser.getBinaryValue(Base64Variants.MIME));
    }

    @Test(timeout = 4000)
    public void testBase64EscapeDecoding() throws IOException {
        TestableParserBase parser = createParser();
        Base64Variant variant = Base64Variants.MIME;

        // Escaped character 'B' has value 1 in Base64
        parser.setEscapedChar('B');
        int bits = parser._decodeBase64Escape(variant, '\\', 0);
        assertEquals(1, bits);

        // Escaped whitespace skipped if index == 0
        parser.setEscapedChar(' ');
        assertEquals(-1, parser._decodeBase64Escape(variant, '\\', 0));

        // Invalid padding at index 1 throws
        parser.setEscapedChar('=');
        try {
            parser._decodeBase64Escape(variant, '\\', 1);
            fail("Expected IllegalArgumentException for padding at index 1");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unexpected padding"));
        }

        // Invalid base64 character throws
        parser.setEscapedChar('@');
        try {
            parser._decodeBase64Escape(variant, '\\', 0);
            fail("Expected IllegalArgumentException for '@'");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Illegal character '@'"));
        }

        // Non-backslash passed directly to escape decode
        try {
            parser._decodeBase64Escape(variant, 'Z', 0);
            fail("Expected exception for unescaped char");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReportInvalidBase64CharVarieties() {
        TestableParserBase parser = createParser();
        Base64Variant variant = Base64Variants.MIME;

        // 1. Whitespace error
        IllegalArgumentException ex1 = parser.reportInvalidBase64Char(variant, ' ', 1);
        assertTrue(ex1.getMessage().contains("Illegal white space character"));

        // 2. Padding error
        IllegalArgumentException ex2 = parser.reportInvalidBase64Char(variant, '=', 0);
        assertTrue(ex2.getMessage().contains("Unexpected padding character"));

        // 3. Control character error
        IllegalArgumentException ex3 = parser.reportInvalidBase64Char(variant, 0x05, 0);
        assertTrue(ex3.getMessage().contains("Illegal character (code 0x5)"));

        // 4. Custom message suffix
        IllegalArgumentException ex4 = parser.reportInvalidBase64Char(variant, '?', 2, "custom detail");
        assertTrue(ex4.getMessage().contains("custom detail"));
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testReportBase64MissingPadding() throws IOException {
        TestableParserBase parser = createParser();
        parser._handleBase64MissingPadding(Base64Variants.MIME);
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testHandleEOFInNonRootContext() throws JsonParseException {
        TestableParserBase parser = createParser();
        // Create child context (Array)
        parser._parsingContext = parser._parsingContext.createChildArrayContext(1, 1);
        parser._handleEOF();
    }

    @Test(timeout = 4000)
    public void testHandleEOFInRootContext() throws JsonParseException {
        TestableParserBase parser = createParser();
        assertTrue(parser.getParsingContext().inRoot());
        // Should not throw in root context
        parser._handleEOF();
        assertEquals(-1, parser._eofAsNextChar());
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testReportMismatchedEndMarker() throws JsonParseException {
        TestableParserBase parser = createParser();
        parser._reportMismatchedEndMarker('}', ']');
    }

    @Test(timeout = 4000)
    public void testHandleUnrecognizedCharacterEscape() throws Exception {
        TestableParserBase parser = createParser();

        // Disabled backslash escaping
        try {
            parser._handleUnrecognizedCharacterEscape('q');
            fail("Expected exception for unrecognized character escape");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("Unrecognized character escape 'q'"));
        }

        // Enabled backslash escaping of any char
        parser.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        assertEquals('q', parser._handleUnrecognizedCharacterEscape('q'));

        // Single quote allowed
        parser.disable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        parser.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        assertEquals('\'', parser._handleUnrecognizedCharacterEscape('\''));
    }

    @Test(timeout = 4000)
    public void testThrowUnquotedSpace() throws JsonParseException {
        TestableParserBase parser = createParser();

        // Control char below space
        try {
            parser._throwUnquotedSpace(0x0A, "string value");
            fail("Expected error for unquoted control character");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Illegal unquoted character"));
        }

        // Feature enabled suppresses exception for chars <= 0x20
        parser.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        parser._throwUnquotedSpace(0x0A, "string value"); // should pass smoothly
    }

    /*
     * ----------------------------------------------------------------------
     * Partition E: Object Lifecycle, Text Buffers & Helper Utilities
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testGrowArrayBy() {
        int[] original = new int[]{1, 2, 3};
        int[] grown = ParserBase.growArrayBy(original, 2);
        assertNotNull(grown);
        assertEquals(5, grown.length);
        assertEquals(1, grown[0]);
        assertEquals(2, grown[1]);
        assertEquals(3, grown[2]);
        assertEquals(0, grown[3]);

        int[] fromNull = ParserBase.growArrayBy(null, 4);
        assertNotNull(fromNull);
        assertEquals(4, fromNull.length);
    }

    @Test(timeout = 4000)
    public void testHasTextCharacters() {
        TestableParserBase parser = createParser();

        parser.setCurrToken(JsonToken.VALUE_STRING);
        assertTrue(parser.hasTextCharacters());

        parser.setCurrToken(JsonToken.FIELD_NAME);
        parser._nameCopied = false;
        assertFalse(parser.hasTextCharacters());
        parser._nameCopied = true;
        assertTrue(parser.hasTextCharacters());

        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        assertFalse(parser.hasTextCharacters());
    }

    @Test(timeout = 4000)
    public void testGetCurrentNameAndOverrideName() throws IOException {
        TestableParserBase parser = createParser();

        // Root context
        assertNull(parser.getCurrentName());
        parser.overrideCurrentName("rootField");
        assertEquals("rootField", parser.getCurrentName());

        // Object context
        parser._parsingContext = parser._parsingContext.createChildObjectContext(1, 1);
        parser._parsingContext.setCurrentName("childField");
        parser.setCurrToken(JsonToken.START_OBJECT);

        // When START_OBJECT/ARRAY, looks up parent's current name
        assertEquals("rootField", parser.getCurrentName());

        parser.setCurrToken(JsonToken.FIELD_NAME);
        assertEquals("childField", parser.getCurrentName());

        parser.overrideCurrentName("renamedField");
        assertEquals("renamedField", parser.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testByteArrayBuilderReuse() {
        TestableParserBase parser = createParser();
        assertNotNull(parser._getByteArrayBuilder());
        parser._getByteArrayBuilder().append(42);
        assertEquals(1, parser._getByteArrayBuilder().size());

        // Reset and reuse
        assertNotNull(parser._getByteArrayBuilder());
        assertEquals(0, parser._getByteArrayBuilder().size());
    }

    @Test(timeout = 4000)
    public void testReleaseBuffers() throws IOException {
        TestableParserBase parser = createParser();
        parser._nameCopyBuffer = new char[100];
        parser._releaseBuffers();
        assertNull(parser._nameCopyBuffer);

        // Deprecated lifecycle methods
        assertFalse(parser.loadMore());
        parser._finishString();
    }
}