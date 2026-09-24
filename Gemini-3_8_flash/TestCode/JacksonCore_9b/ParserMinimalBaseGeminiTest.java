package com.fasterxml.jackson.core.base;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: ParserMinimalBase
 * Defect Target (Defects4J):
 *  - getValueAsString() / getValueAsString(defaultValue) when pointing to JsonToken.FIELD_NAME.
 *    In the defective version, FIELD_NAME is not recognized as a string-convertible value because
 *    !_currToken.isScalarValue() evaluates to true, resulting in returning defaultValue (null),
 *    whereas it should return the field name (e.g. "a"), matching getText() / getCurrentName().
 *
 * Decision / Branch Coverage Targets:
 *  - getCurrentToken(), getCurrentTokenId(), hasCurrentToken(), hasTokenId(), hasToken()
 *  - isExpectedStartArrayToken(), isExpectedStartObjectToken()
 *  - nextValue() across FIELD_NAME and scalar/struct tokens
 *  - skipChildren() on START_OBJECT, START_ARRAY, nested objects/arrays, scalar tokens, and EOF
 *  - clearCurrentToken(), getLastClearedToken()
 *  - getValueAsBoolean(default) for ID_STRING ("true", "false", "null", non-boolean),
 *    ID_NUMBER_INT (zero vs non-zero), ID_TRUE, ID_FALSE, ID_NULL, ID_EMBEDDED_OBJECT (Boolean vs others)
 *  - getValueAsInt() / getValueAsInt(default) for VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT,
 *    ID_STRING ("null", valid int, malformed int), ID_TRUE, ID_FALSE, ID_NULL, ID_EMBEDDED_OBJECT (Number vs others)
 *  - getValueAsLong() / getValueAsLong(default) for VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT,
 *    ID_STRING ("null", valid long, malformed long), ID_TRUE, ID_FALSE, ID_NULL, ID_EMBEDDED_OBJECT
 *  - getValueAsDouble(default) for ID_STRING ("null", valid double, malformed),
 *    ID_NUMBER_INT, ID_NUMBER_FLOAT, ID_TRUE, ID_FALSE, ID_NULL, ID_EMBEDDED_OBJECT
 *  - getValueAsString() / getValueAsString(default) for VALUE_STRING, FIELD_NAME, VALUE_NULL, null, scalar/non-scalar
 *  - _decodeBase64, _reportInvalidBase64, _reportBase64EOF
 *  - _hasTextualNull
 *  - _reportUnexpectedChar, _reportInvalidEOF, _reportMissingRootWS, _throwInvalidSpace, _throwUnquotedSpace
 *  - _handleUnrecognizedCharacterEscape with/without ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER & ALLOW_SINGLE_QUOTES
 *  - _getCharDesc (control chars, ASCII, > 255 chars)
 *  - _asciiBytes, _ascii
 */
public class ParserMinimalBaseGeminiTest {

    /**
     * Concrete testable mock implementation of ParserMinimalBase for white-box testing.
     */
    private static class StubParserMinimalBase extends ParserMinimalBase {
        private final List<JsonToken> _tokens;
        private int _index = -1;
        private String _text;
        private String _currentName;
        private Object _embeddedObject;
        private int _intValue;
        private long _longValue;
        private double _doubleValue;
        private boolean _closed;

        public StubParserMinimalBase() {
            super();
            _tokens = new ArrayList<JsonToken>();
        }

        public StubParserMinimalBase(int features, JsonToken... tokens) {
            super(features);
            _tokens = new ArrayList<JsonToken>(Arrays.asList(tokens));
        }

        public void setTokens(JsonToken... tokens) {
            _tokens.clear();
            _tokens.addAll(Arrays.asList(tokens));
            _index = -1;
            _currToken = null;
        }

        public void setCurrToken(JsonToken t) {
            _currToken = t;
        }

        public void setText(String text) {
            _text = text;
        }

        public void setCurrentName(String name) {
            _currentName = name;
        }

        public void setEmbeddedObject(Object obj) {
            _embeddedObject = obj;
        }

        public void setNumericValues(int i, long l, double d) {
            _intValue = i;
            _longValue = l;
            _doubleValue = d;
        }

        @Override
        public JsonToken nextToken() throws IOException {
            _index++;
            if (_index < _tokens.size()) {
                _currToken = _tokens.get(_index);
            } else {
                _currToken = null;
            }
            return _currToken;
        }

        @Override
        protected void _handleEOF() throws JsonParseException {
            _reportInvalidEOF(" in stub");
        }

        @Override
        public String getCurrentName() throws IOException {
            return _currentName != null ? _currentName : _text;
        }

        @Override
        public void overrideCurrentName(String name) {
            _currentName = name;
        }

        @Override
        public void close() throws IOException {
            _closed = true;
        }

        @Override
        public boolean isClosed() {
            return _closed;
        }

        @Override
        public JsonStreamContext getParsingContext() {
            return null;
        }

        @Override
        public JsonLocation getCurrentLocation() {
            return JsonLocation.NA;
        }

        @Override
        public JsonLocation getTokenLocation() {
            return JsonLocation.NA;
        }

        @Override
        public String getText() throws IOException {
            return _text;
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            return _text != null ? _text.toCharArray() : null;
        }

        @Override
        public boolean hasTextCharacters() {
            return _text != null;
        }

        @Override
        public int getTextLength() throws IOException {
            return _text != null ? _text.length() : 0;
        }

        @Override
        public int getTextOffset() throws IOException {
            return 0;
        }

        @Override
        public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
            return null;
        }

        @Override
        public Codec getCodec() {
            return null;
        }

        @Override
        public void setCodec(Codec c) {
        }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public Object getEmbeddedObject() throws IOException {
            return _embeddedObject;
        }

        @Override
        public Number getNumberValue() throws IOException {
            return _intValue;
        }

        @Override
        public NumberType getNumberType() throws IOException {
            return NumberType.INT;
        }

        @Override
        public int getIntValue() throws IOException {
            return _intValue;
        }

        @Override
        public long getLongValue() throws IOException {
            return _longValue;
        }

        @Override
        public BigInteger getBigIntegerValue() throws IOException {
            return BigInteger.valueOf(_longValue);
        }

        @Override
        public float getFloatValue() throws IOException {
            return (float) _doubleValue;
        }

        @Override
        public double getDoubleValue() throws IOException {
            return _doubleValue;
        }

        @Override
        public BigDecimal getDecimalValue() throws IOException {
            return BigDecimal.valueOf(_doubleValue);
        }
    }

    /*
     * =========================================================================
     * Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
     * =========================================================================
     */

    /**
     * Targets Defects4J bug where getValueAsString() on JsonToken.FIELD_NAME
     * returned null / defaultValue instead of the field name itself.
     */
    @Test(timeout = 4000)
    public void testGetValueAsStringOnFieldName_DefectTarget() throws IOException {
        StubParserMinimalBase parser = new StubParserMinimalBase();
        parser.setCurrToken(JsonToken.FIELD_NAME);
        parser.setText("a");
        parser.setCurrentName("a");

        assertEquals("a", parser.getText());
        assertEquals("a", parser.getCurrentName());
        // Defective version fails here: returns null instead of "a"
        assertEquals("a", parser.getValueAsString());
        assertEquals("a", parser.getValueAsString("default"));
    }

    /*
     * =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testTokenStateAndQueries() {
        StubParserMinimalBase parser = new StubParserMinimalBase();
        assertNull(parser.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.getCurrentTokenId());
        assertFalse(parser.hasCurrentToken());
        assertTrue(parser.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(parser.hasTokenId(JsonTokenId.ID_STRING));
        assertTrue(parser.hasToken(null));
        assertFalse(parser.hasToken(JsonToken.START_OBJECT));
        assertFalse(parser.isExpectedStartArrayToken());
        assertFalse(parser.isExpectedStartObjectToken());

        parser.setCurrToken(JsonToken.START_OBJECT);
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
        assertEquals(JsonTokenId.ID_START_OBJECT, parser.getCurrentTokenId());
        assertTrue(parser.hasCurrentToken());
        assertTrue(parser.hasTokenId(JsonTokenId.ID_START_OBJECT));
        assertFalse(parser.hasTokenId(JsonTokenId.ID_START_ARRAY));
        assertTrue(parser.hasToken(JsonToken.START_OBJECT));
        assertFalse(parser.isExpectedStartArrayToken());
        assertTrue(parser.isExpectedStartObjectToken());

        parser.setCurrToken(JsonToken.START_ARRAY);
        assertTrue(parser.isExpectedStartArrayToken());
        assertFalse(parser.isExpectedStartObjectToken());

        parser.clearCurrentToken();
        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.START_ARRAY, parser.getLastClearedToken());

        // Calling clearCurrentToken when already null should preserve last cleared token
        parser.clearCurrentToken();
        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.START_ARRAY, parser.getLastClearedToken());
    }

    @Test(timeout = 4000)
    public void testNextValue() throws IOException {
        StubParserMinimalBase parser = new StubParserMinimalBase(0,
                JsonToken.START_OBJECT,
                JsonToken.FIELD_NAME,
                JsonToken.VALUE_STRING,
                JsonToken.END_OBJECT
        );

        assertEquals(JsonToken.START_OBJECT, parser.nextValue());
        // FIELD_NAME is skipped by nextValue(), advancing directly to VALUE_STRING
        assertEquals(JsonToken.VALUE_STRING, parser.nextValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextValue());
        assertNull(parser.nextValue());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnNonStruct() throws IOException {
        StubParserMinimalBase parser = new StubParserMinimalBase(0, JsonToken.VALUE_STRING);
        parser.nextToken();
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
        assertSame(parser, parser.skipChildren());
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenNested() throws IOException {
        StubParserMinimalBase parser = new StubParserMinimalBase(0,
                JsonToken.START_OBJECT,
                JsonToken.FIELD_NAME,
                JsonToken.START_ARRAY,
                JsonToken.VALUE_NUMBER_INT,
                JsonToken.END_ARRAY,
                JsonToken.END_OBJECT,
                JsonToken.VALUE_TRUE
        );
        parser.nextToken(); // points to START_OBJECT
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());

        assertSame(parser, parser.skipChildren());
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());

        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenEOF() throws IOException {
        StubParserMinimalBase parser = new StubParserMinimalBase(0,
                JsonToken.START_ARRAY,
                JsonToken.VALUE_NUMBER_INT
        );
        parser.nextToken(); // points to START_ARRAY
        try {
            parser.skipChildren();
            fail("Expected JsonParseException on EOF during skipChildren");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Unexpected end-of-input"));
        }
    }

    /*
     * =========================================================================
     * Partition B: Boundary Value Analysis & Type Coercions
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testGetValueAsBoolean() throws IOException {
        StubParserMinimalBase parser = new StubParserMinimalBase();

        // null token returns default
        parser.setCurrToken(null);
        assertTrue(parser.getValueAsBoolean(true));
        assertFalse(parser.getValueAsBoolean(false));

        // String "true", "false", "null", other
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("true");
        assertTrue(parser.getValueAsBoolean(false));
        parser.setText("false");
        assertFalse(parser.getValueAsBoolean(true));
        parser.setText("null");
        assertFalse(parser.getValueAsBoolean(true));
        parser.setText("random");
        assertTrue(parser.getValueAsBoolean(true));
        assertFalse(parser.getValueAsBoolean(false));

        // Int 0 vs non-zero
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setNumericValues(0, 0L, 0.0);
        assertFalse(parser.getValueAsBoolean(true));
        parser.setNumericValues(1, 1L, 1.0);
        assertTrue(parser.getValueAsBoolean(false));
        parser.setNumericValues(-5, -5L, -5.0);
        assertTrue(parser.getValueAsBoolean(false));

        // Boolean tokens
        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertTrue(parser.getValueAsBoolean(false));
        parser.setCurrToken(JsonToken.VALUE_FALSE);
        assertFalse(parser.getValueAsBoolean(true));
        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertFalse(parser.getValueAsBoolean(true));

        // Embedded Object
        parser.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Boolean.TRUE);
        assertTrue(parser.getValueAsBoolean(false));
        parser.setEmbeddedObject(Boolean.FALSE);
        assertFalse(parser.getValueAsBoolean(true));
        parser.setEmbeddedObject("not a boolean");
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsInt() throws IOException {
        StubParserMinimalBase parser = new StubParserMinimalBase();

        // null token
        assertEquals(0, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(42));

        // VALUE_NUMBER_INT and VALUE_NUMBER_FLOAT
        parser.setNumericValues(123, 123L, 123.0);
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        assertEquals(123, parser.getValueAsInt());
        assertEquals(123, parser.getValueAsInt(99));

        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(123, parser.getValueAsInt());
        assertEquals(123, parser.getValueAsInt(99));

        // String values
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("null");
        assertEquals(0, parser.getValueAsInt(55));

        parser.setText("456");
        assertEquals(456, parser.getValueAsInt(0));

        parser.setText("not-a-number");
        assertEquals(77, parser.getValueAsInt(77));

        // Booleans & Null
        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertEquals(1, parser.getValueAsInt(0));
        parser.setCurrToken(JsonToken.VALUE_FALSE);
        assertEquals(0, parser.getValueAsInt(1));
        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertEquals(0, parser.getValueAsInt(1));

        // Embedded object
        parser.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Integer.valueOf(888));
        assertEquals(888, parser.getValueAsInt(0));
        parser.setEmbeddedObject("a string");
        assertEquals(99, parser.getValueAsInt(99));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLong() throws IOException {
        StubParserMinimalBase parser = new StubParserMinimalBase();

        assertEquals(0L, parser.getValueAsLong());
        assertEquals(1234567890123L, parser.getValueAsLong(1234567890123L));

        parser.setNumericValues(10, 9876543210L, 10.0);
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        assertEquals(9876543210L, parser.getValueAsLong());
        assertEquals(9876543210L, parser.getValueAsLong(5L));

        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(9876543210L, parser.getValueAsLong());

        // String values
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("null");
        assertEquals(0L, parser.getValueAsLong(999L));

        parser.setText("9876543210");
        assertEquals(9876543210L, parser.getValueAsLong(0L));

        parser.setText("invalid");
        assertEquals(88L, parser.getValueAsLong(88L));

        // Booleans & Null
        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertEquals(1L, parser.getValueAsLong(0L));
        parser.setCurrToken(JsonToken.VALUE_FALSE);
        assertEquals(0L, parser.getValueAsLong(1L));
        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertEquals(0L, parser.getValueAsLong(1L));

        // Embedded object
        parser.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Long.valueOf(5555555555L));
        assertEquals(5555555555L, parser.getValueAsLong(0L));
        parser.setEmbeddedObject("abc");
        assertEquals(11L, parser.getValueAsLong(11L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDouble() throws IOException {
        StubParserMinimalBase parser = new StubParserMinimalBase();

        assertEquals(0.0, parser.getValueAsDouble(0.0), 0.0001);
        assertEquals(3.1415, parser.getValueAsDouble(3.1415), 0.0001);

        parser.setNumericValues(2, 2L, 2.718);
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        assertEquals(2.718, parser.getValueAsDouble(0.0), 0.0001);
        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(2.718, parser.getValueAsDouble(0.0), 0.0001);

        // String values
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("null");
        assertEquals(0.0, parser.getValueAsDouble(5.5), 0.0001);

        parser.setText("1.25");
        assertEquals(1.25, parser.getValueAsDouble(0.0), 0.0001);

        parser.setText("bad-double");
        assertEquals(4.4, parser.getValueAsDouble(4.4), 0.0001);

        // Booleans & Null
        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertEquals(1.0, parser.getValueAsDouble(0.0), 0.0001);
        parser.setCurrToken(JsonToken.VALUE_FALSE);
        assertEquals(0.0, parser.getValueAsDouble(1.0), 0.0001);
        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertEquals(0.0, parser.getValueAsDouble(1.0), 0.0001);

        // Embedded object
        parser.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Double.valueOf(7.89));
        assertEquals(7.89, parser.getValueAsDouble(0.0), 0.0001);
        parser.setEmbeddedObject(new Object());
        assertEquals(9.9, parser.getValueAsDouble(9.9), 0.0001);
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringGeneral() throws IOException {
        StubParserMinimalBase parser = new StubParserMinimalBase();

        // Null token
        assertNull(parser.getValueAsString());
        assertEquals("default", parser.getValueAsString("default"));

        // VALUE_STRING
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("hello");
        assertEquals("hello", parser.getValueAsString());
        assertEquals("hello", parser.getValueAsString("default"));

        // VALUE_NULL
        parser.setCurrToken(JsonToken.VALUE_NULL);
        parser.setText("null");
        assertNull(parser.getValueAsString());
        assertEquals("default", parser.getValueAsString("default"));

        // Non-scalar tokens (e.g. START_OBJECT, START_ARRAY)
        parser.setCurrToken(JsonToken.START_OBJECT);
        parser.setText("{");
        assertNull(parser.getValueAsString());
        assertEquals("def", parser.getValueAsString("def"));

        parser.setCurrToken(JsonToken.START_ARRAY);
        parser.setText("[");
        assertNull(parser.getValueAsString());
        assertEquals("def", parser.getValueAsString("def"));

        // Scalar non-string (e.g. VALUE_NUMBER_INT)
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("123");
        assertEquals("123", parser.getValueAsString());
        assertEquals("123", parser.getValueAsString("def"));
    }

    /*
     * =========================================================================
     * Partition D: Exception & Defensive Guard Paths & Base64
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testDecodeBase64SuccessAndError() throws IOException {
        StubParserMinimalBase parser = new StubParserMinimalBase();
        ByteArrayBuilder builder = new ByteArrayBuilder();
        Base64Variant variant = Base64Variants.MIME;

        // Valid base64: "AQID" -> [1, 2, 3]
        parser._decodeBase64("AQID", builder, variant);
        byte[] result = builder.toByteArray();
        assertArrayEquals(new byte[]{1, 2, 3}, result);

        // Invalid base64 triggers JsonParseException via _reportError
        builder.reset();
        try {
            parser._decodeBase64("!@#$%", builder, variant);
            fail("Expected JsonParseException for malformed Base64");
        } catch (JsonParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testDeprecatedReportInvalidBase64() {
        StubParserMinimalBase parser = new StubParserMinimalBase();
        Base64Variant variant = Base64Variants.MIME;

        // 1. Whitespace
        try {
            parser._reportInvalidBase64(variant, ' ', 0, "space error");
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Illegal white space character"));
            assertTrue(e.getMessage().contains("space error"));
        }

        // 2. Padding char
        try {
            parser._reportInvalidBase64(variant, '=', 0, null);
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Unexpected padding character"));
        }

        // 3. Regular illegal char
        try {
            parser._reportInvalidBase64(variant, '?', 1, "custom msg");
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Illegal character '?'"));
            assertTrue(e.getMessage().contains("custom msg"));
        }
    }

    @SuppressWarnings("deprecation")
    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testDeprecatedReportBase64EOF() throws JsonParseException {
        StubParserMinimalBase parser = new StubParserMinimalBase();
        parser._reportBase64EOF();
    }

    @Test(timeout = 4000)
    public void testErrorReportingMethods() {
        StubParserMinimalBase parser = new StubParserMinimalBase();

        // _reportUnexpectedChar
        try {
            parser._reportUnexpectedChar('x', "extra info");
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Unexpected character ('x' (code 120))"));
            assertTrue(e.getMessage().contains("extra info"));
        }

        // _reportUnexpectedChar negative (triggers invalid EOF)
        try {
            parser._reportUnexpectedChar(-1, null);
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Unexpected end-of-input"));
        }

        // _reportInvalidEOFInValue
        try {
            parser._reportInvalidEOFInValue();
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Unexpected end-of-input in a value"));
        }

        // _reportMissingRootWS
        try {
            parser._reportMissingRootWS('A');
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Expected space separating root-level values"));
        }

        // _throwInvalidSpace
        try {
            parser._throwInvalidSpace('\u0000');
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("only regular white space"));
        }

        // _wrapError
        try {
            parser._wrapError("Wrapped issue", new RuntimeException("root cause"));
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Wrapped issue"));
            assertNotNull(e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testThrowUnquotedSpace() throws JsonParseException {
        // Without ALLOW_UNQUOTED_CONTROL_CHARS
        StubParserMinimalBase parserDisabled = new StubParserMinimalBase(0);
        try {
            parserDisabled._throwUnquotedSpace('\n', "string value");
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Illegal unquoted character"));
        }

        // With ALLOW_UNQUOTED_CONTROL_CHARS enabled, chars <= INT_SPACE are tolerated
        int flags = JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS.getMask();
        StubParserMinimalBase parserEnabled = new StubParserMinimalBase(flags);
        parserEnabled._throwUnquotedSpace('\n', "string value"); // Should not throw

        // Even with feature enabled, chars > INT_SPACE still throw
        try {
            parserEnabled._throwUnquotedSpace(0x0021, "string value");
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Illegal unquoted character"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleUnrecognizedCharacterEscape() throws JsonProcessingException {
        // Feature: backslash escaping any char
        int backslashFlag = JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.getMask();
        StubParserMinimalBase parserBackslash = new StubParserMinimalBase(backslashFlag);
        assertEquals('z', parserBackslash._handleUnrecognizedCharacterEscape('z'));

        // Feature: single quotes enabled
        int singleQuoteFlag = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask();
        StubParserMinimalBase parserSingleQuote = new StubParserMinimalBase(singleQuoteFlag);
        assertEquals('\'', parserSingleQuote._handleUnrecognizedCharacterEscape('\''));

        // Default: throws JsonParseException
        StubParserMinimalBase parserDefault = new StubParserMinimalBase(0);
        try {
            parserDefault._handleUnrecognizedCharacterEscape('z');
            fail("Expected JsonParseException");
        } catch (JsonProcessingException e) {
            assertTrue(e.getMessage().contains("Unrecognized character escape"));
        }
    }

    /*
     * =========================================================================
     * Partition E: Static Helpers & Character Formatting
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testGetCharDesc() {
        // Control character (< 32)
        assertEquals("(CTRL-CHAR, code 0)", ParserMinimalBase._getCharDesc(0));
        assertEquals("(CTRL-CHAR, code 9)", ParserMinimalBase._getCharDesc(9));

        // Normal ASCII (> 31 and <= 255)
        assertEquals("'A' (code 65)", ParserMinimalBase._getCharDesc(65));
        assertEquals("'~' (code 126)", ParserMinimalBase._getCharDesc(126));

        // Above 255
        assertEquals("'\u0100' (code 256 / 0x100)", ParserMinimalBase._getCharDesc(256));
        assertEquals("'\u2028' (code 8232 / 0x2028)", ParserMinimalBase._getCharDesc(0x2028));
    }

    @Test(timeout = 4000)
    public void testAsciiConversionHelpers() {
        String testStr = "JacksonParser123!";
        byte[] bytes = ParserMinimalBase._asciiBytes(testStr);
        assertEquals(testStr.length(), bytes.length);
        for (int i = 0; i < testStr.length(); i++) {
            assertEquals((byte) testStr.charAt(i), bytes[i]);
        }

        String roundtrip = ParserMinimalBase._ascii(bytes);
        assertEquals(testStr, roundtrip);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testThrowInternal() {
        StubParserMinimalBase parser = new StubParserMinimalBase();
        parser._throwInternal();
    }
}