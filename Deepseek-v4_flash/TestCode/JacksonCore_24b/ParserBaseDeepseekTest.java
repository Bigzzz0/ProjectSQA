package com.fasterxml.jackson.core.base;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.TextBuffer;

/**
 * White-box test suite for ParserBase, targeting numeric overflow defects
 * and comprehensive branch coverage.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (reset, resetInt, resetFloat, resetAsNaN, isNaN)
 * - Partition B: Boundary value analysis (int min/max, long min/max, zero, negative)
 * - Partition C: Defect-targeted branch zone (overflow detection in _parseSlowInt, _reportTooLongIntegral)
 * - Partition D: Exception paths (invalid token, malformed number, base64 errors)
 * - Partition E: Object lifecycle (close, isClosed, getCurrentName, overrideCurrentName, feature toggling)
 */
public class ParserBaseDeepseekTest {

    // ---------- Helper: concrete stub for ParserBase ----------
    private static class TestParser extends ParserBase {
        public TestParser(IOContext ctxt, int features) {
            super(ctxt, features);
        }

        @Override
        protected void _closeInput() throws IOException {
            // no-op
        }

        @Override
        protected void _handleEOF() throws JsonParseException {
            throw new JsonParseException(null, "EOF");
        }

        @Override
        protected char _decodeEscaped() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JsonToken getCurrentToken() {
            return _currToken;
        }

        @Override
        public String getText() throws IOException {
            return _textBuffer.contentsAsString();
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            return _textBuffer.getTextBuffer();
        }

        @Override
        public int getTextLength() throws IOException {
            return _textBuffer.size();
        }

        @Override
        public int getTextOffset() throws IOException {
            return _textBuffer.getTextOffset();
        }

        @Override
        public Object getEmbeddedObject() throws IOException {
            return null;
        }

        @Override
        public JsonToken nextToken() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JsonToken nextValue() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JsonParser skipChildren() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isClosed() {
            return _closed;
        }
    }

    // ---------- Helper: create a TestParser with minimal IOContext ----------
    private TestParser createParser(int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), new Object(), false);
        return new TestParser(ctxt, features);
    }

    // ---------- Helper: set up numeric state via reflection ----------
    private void setupNumber(TestParser parser, String numStr, boolean negative,
                             int intLen, int fractLen, int expLen, JsonToken token) throws Exception {
        // Set _currToken
        Field tokenField = ParserBase.class.getSuperclass().getDeclaredField("_currToken");
        tokenField.setAccessible(true);
        tokenField.set(parser, token);

        // Set _textBuffer content
        Field textBufferField = ParserBase.class.getDeclaredField("_textBuffer");
        textBufferField.setAccessible(true);
        TextBuffer tb = (TextBuffer) textBufferField.get(parser);
        tb.resetWithString(numStr);

        // Set numeric fields
        setField(parser, "_numberNegative", negative);
        setField(parser, "_intLength", intLen);
        setField(parser, "_fractLength", fractLen);
        setField(parser, "_expLength", expLen);
        setField(parser, "_numTypesValid", 0); // NR_UNKNOWN = 0
    }

    private void setField(Object obj, String name, Object value) throws Exception {
        Field f = ParserBase.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    // ---------- Partition A: Core functional logic ----------
    @Test(timeout = 4000)
    public void testResetInt() throws Exception {
        TestParser p = createParser(0);
        JsonToken t = p.resetInt(false, 3);
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        assertEquals(false, p._numberNegative);
        assertEquals(3, p._intLength);
        assertEquals(0, p._fractLength);
        assertEquals(0, p._expLength);
        assertEquals(0, p._numTypesValid);
    }

    @Test(timeout = 4000)
    public void testResetFloat() throws Exception {
        TestParser p = createParser(0);
        JsonToken t = p.resetFloat(true, 2, 3, 1);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, t);
        assertEquals(true, p._numberNegative);
        assertEquals(2, p._intLength);
        assertEquals(3, p._fractLength);
        assertEquals(1, p._expLength);
        assertEquals(0, p._numTypesValid);
    }

    @Test(timeout = 4000)
    public void testResetAsNaN() throws Exception {
        TestParser p = createParser(0);
        JsonToken t = p.resetAsNaN("NaN", Double.NaN);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, t);
        assertTrue(Double.isNaN(p._numberDouble));
        assertEquals(1, p._numTypesValid & 4); // NR_DOUBLE = 4
    }

    @Test(timeout = 4000)
    public void testIsNaN() throws Exception {
        TestParser p = createParser(0);
        // Set up NaN state
        p.resetAsNaN("NaN", Double.NaN);
        assertTrue(p.isNaN());
        // Set up infinite
        p.resetAsNaN("Infinity", Double.POSITIVE_INFINITY);
        assertTrue(p.isNaN());
        // Set up normal float
        p.resetFloat(false, 1, 1, 0);
        // Need to set _numTypesValid to NR_DOUBLE and _numberDouble
        setField(p, "_numTypesValid", 4); // NR_DOUBLE
        setField(p, "_numberDouble", 3.14);
        assertFalse(p.isNaN());
    }

    // ---------- Partition B: Boundary value analysis ----------
    @Test(timeout = 4000)
    public void testGetIntValueValid() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "123", false, 3, 0, 0, JsonToken.VALUE_NUMBER_INT);
        assertEquals(123, p.getIntValue());
    }

    @Test(timeout = 4000)
    public void testGetIntValueNegative() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "-456", true, 3, 0, 0, JsonToken.VALUE_NUMBER_INT);
        assertEquals(-456, p.getIntValue());
    }

    @Test(timeout = 4000)
    public void testGetIntValueMaxInt() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "2147483647", false, 10, 0, 0, JsonToken.VALUE_NUMBER_INT);
        assertEquals(Integer.MAX_VALUE, p.getIntValue());
    }

    @Test(timeout = 4000)
    public void testGetIntValueMinInt() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "-2147483648", true, 10, 0, 0, JsonToken.VALUE_NUMBER_INT);
        assertEquals(Integer.MIN_VALUE, p.getIntValue());
    }

    @Test(timeout = 4000)
    public void testGetLongValueValid() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "1234567890123", false, 13, 0, 0, JsonToken.VALUE_NUMBER_INT);
        assertEquals(1234567890123L, p.getLongValue());
    }

    @Test(timeout = 4000)
    public void testGetLongValueMaxLong() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "9223372036854775807", false, 19, 0, 0, JsonToken.VALUE_NUMBER_INT);
        assertEquals(Long.MAX_VALUE, p.getLongValue());
    }

    @Test(timeout = 4000)
    public void testGetLongValueMinLong() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "-9223372036854775808", true, 19, 0, 0, JsonToken.VALUE_NUMBER_INT);
        assertEquals(Long.MIN_VALUE, p.getLongValue());
    }

    // ---------- Partition C: Defect-targeted overflow detection ----------
    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testIntOverflow() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "2147483648", false, 10, 0, 0, JsonToken.VALUE_NUMBER_INT);
        p.getIntValue(); // Should throw JsonParseException
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testIntOverflowNegative() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "-2147483649", true, 10, 0, 0, JsonToken.VALUE_NUMBER_INT);
        p.getIntValue();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testLongOverflow() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "9223372036854775817", false, 19, 0, 0, JsonToken.VALUE_NUMBER_INT);
        p.getLongValue();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testLongOverflowNegative() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "-9223372036854775809", true, 19, 0, 0, JsonToken.VALUE_NUMBER_INT);
        p.getLongValue();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMaliciousIntOverflow() throws Exception {
        TestParser p = createParser(0);
        // 199999-digit number (too large for any integer type)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 199999; i++) sb.append('9');
        String huge = sb.toString();
        setupNumber(p, huge, false, 199999, 0, 0, JsonToken.VALUE_NUMBER_INT);
        p.getIntValue();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMaliciousLongOverflow() throws Exception {
        TestParser p = createParser(0);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 199999; i++) sb.append('9');
        String huge = sb.toString();
        setupNumber(p, huge, false, 199999, 0, 0, JsonToken.VALUE_NUMBER_INT);
        p.getLongValue();
    }

    // ---------- Partition D: Exception & defensive guard paths ----------
    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testGetIntValueOnFloatToken() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "3.14", false, 1, 2, 0, JsonToken.VALUE_NUMBER_FLOAT);
        p.getIntValue(); // Should fail because token is float, not int
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testGetNumberValueOnNonNumericToken() throws Exception {
        TestParser p = createParser(0);
        // Set token to something non-numeric, e.g., VALUE_STRING
        Field tokenField = ParserBase.class.getSuperclass().getDeclaredField("_currToken");
        tokenField.setAccessible(true);
        tokenField.set(p, JsonToken.VALUE_STRING);
        p.getNumberValue();
    }

    @Test(timeout = 4000)
    public void testGetNumberValueInt() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "42", false, 2, 0, 0, JsonToken.VALUE_NUMBER_INT);
        Number n = p.getNumberValue();
        assertTrue(n instanceof Integer);
        assertEquals(42, n.intValue());
    }

    @Test(timeout = 4000)
    public void testGetNumberValueLong() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "1234567890123", false, 13, 0, 0, JsonToken.VALUE_NUMBER_INT);
        Number n = p.getNumberValue();
        assertTrue(n instanceof Long);
        assertEquals(1234567890123L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testGetNumberValueBigInteger() throws Exception {
        TestParser p = createParser(0);
        // Number too large for long, should become BigInteger
        setupNumber(p, "123456789012345678901234567890", false, 30, 0, 0, JsonToken.VALUE_NUMBER_INT);
        Number n = p.getNumberValue();
        assertTrue(n instanceof BigInteger);
        assertEquals(new BigInteger("123456789012345678901234567890"), n);
    }

    @Test(timeout = 4000)
    public void testGetNumberValueFloat() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "3.14", false, 1, 2, 0, JsonToken.VALUE_NUMBER_FLOAT);
        Number n = p.getNumberValue();
        assertTrue(n instanceof Double);
        assertEquals(3.14, n.doubleValue(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetNumberValueBigDecimal() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "3.14159265358979323846", false, 1, 20, 0, JsonToken.VALUE_NUMBER_FLOAT);
        // Force BigDecimal by calling getDecimalValue first
        p.getDecimalValue();
        Number n = p.getNumberValue();
        assertTrue(n instanceof BigDecimal);
        assertEquals(new BigDecimal("3.14159265358979323846"), n);
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeInt() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "42", false, 2, 0, 0, JsonToken.VALUE_NUMBER_INT);
        assertEquals(NumberType.INT, p.getNumberType());
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeLong() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "1234567890123", false, 13, 0, 0, JsonToken.VALUE_NUMBER_INT);
        assertEquals(NumberType.LONG, p.getNumberType());
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeBigInteger() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "123456789012345678901234567890", false, 30, 0, 0, JsonToken.VALUE_NUMBER_INT);
        assertEquals(NumberType.BIG_INTEGER, p.getNumberType());
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeDouble() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "3.14", false, 1, 2, 0, JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(NumberType.DOUBLE, p.getNumberType());
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeBigDecimal() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "3.14159265358979323846", false, 1, 20, 0, JsonToken.VALUE_NUMBER_FLOAT);
        p.getDecimalValue(); // force BigDecimal
        assertEquals(NumberType.BIG_DECIMAL, p.getNumberType());
    }

    @Test(timeout = 4000)
    public void testGetFloatValue() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "3.14", false, 1, 2, 0, JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(3.14f, p.getFloatValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testGetDoubleValue() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "3.14", false, 1, 2, 0, JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(3.14, p.getDoubleValue(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetDecimalValue() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "3.14", false, 1, 2, 0, JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(new BigDecimal("3.14"), p.getDecimalValue());
    }

    @Test(timeout = 4000)
    public void testGetBigIntegerValue() throws Exception {
        TestParser p = createParser(0);
        setupNumber(p, "12345678901234567890", false, 20, 0, 0, JsonToken.VALUE_NUMBER_INT);
        assertEquals(new BigInteger("12345678901234567890"), p.getBigIntegerValue());
    }

    // ---------- Partition E: Object lifecycle & contract integrity ----------
    @Test(timeout = 4000)
    public void testCloseAndIsClosed() throws Exception {
        TestParser p = createParser(0);
        assertFalse(p.isClosed());
        p.close();
        assertTrue(p.isClosed());
        // Closing again should be safe
        p.close();
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testGetCurrentName() throws Exception {
        TestParser p = createParser(0);
        // In root context, current name should be null
        assertNull(p.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentName() throws Exception {
        TestParser p = createParser(0);
        p.overrideCurrentName("testName");
        // After override, getCurrentName should return the new name
        assertEquals("testName", p.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testEnableDisableStrictDuplicateDetection() throws Exception {
        TestParser p = createParser(0);
        assertNull(p._parsingContext.getDupDetector());
        p.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        assertNotNull(p._parsingContext.getDupDetector());
        p.disable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        assertNull(p._parsingContext.getDupDetector());
    }

    @Test(timeout = 4000)
    public void testOverrideStdFeatures() throws Exception {
        TestParser p = createParser(0);
        int mask = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        p.overrideStdFeatures(mask, mask);
        assertNotNull(p._parsingContext.getDupDetector());
        p.overrideStdFeatures(0, mask);
        assertNull(p._parsingContext.getDupDetector());
    }

    @Test(timeout = 4000)
    public void testGetTokenLocation() throws Exception {
        TestParser p = createParser(0);
        JsonLocation loc = p.getTokenLocation();
        assertNotNull(loc);
        // Default values: -1 for byte offset, 0 for char offset? Actually getTokenCharacterOffset returns _tokenInputTotal which is 0 initially
        assertEquals(0L, loc.getCharOffset());
        assertEquals(1, loc.getLineNr());
        // Column is 0-based internally, getTokenColumnNr returns col+1 if col>=0, else col
        assertEquals(1, loc.getColumnNr());
    }

    @Test(timeout = 4000)
    public void testGetCurrentLocation() throws Exception {
        TestParser p = createParser(0);
        JsonLocation loc = p.getCurrentLocation();
        assertNotNull(loc);
        // _inputPtr and _currInputRowStart are 0, so column = 1
        assertEquals(1, loc.getColumnNr());
        assertEquals(1, loc.getLineNr());
    }

    @Test(timeout = 4000)
    public void testHasTextCharacters() throws Exception {
        TestParser p = createParser(0);
        // For VALUE_STRING, should return true
        Field tokenField = ParserBase.class.getSuperclass().getDeclaredField("_currToken");
        tokenField.setAccessible(true);
        tokenField.set(p, JsonToken.VALUE_STRING);
        assertTrue(p.hasTextCharacters());
        // For FIELD_NAME with _nameCopied false, should return false
        tokenField.set(p, JsonToken.FIELD_NAME);
        setField(p, "_nameCopied", false);
        assertFalse(p.hasTextCharacters());
        // For FIELD_NAME with _nameCopied true, should return true
        setField(p, "_nameCopied", true);
        assertTrue(p.hasTextCharacters());
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueOnNonStringToken() throws Exception {
        TestParser p = createParser(0);
        Field tokenField = ParserBase.class.getSuperclass().getDeclaredField("_currToken");
        tokenField.setAccessible(true);
        tokenField.set(p, JsonToken.VALUE_NUMBER_INT);
        try {
            p.getBinaryValue(Base64Variants.MIME);
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetBinaryValue() throws Exception {
        TestParser p = createParser(0);
        // Set up a string token with base64 content
        Field tokenField = ParserBase.class.getSuperclass().getDeclaredField("_currToken");
        tokenField.setAccessible(true);
        tokenField.set(p, JsonToken.VALUE_STRING);
        Field textBufferField = ParserBase.class.getDeclaredField("_textBuffer");
        textBufferField.setAccessible(true);
        TextBuffer tb = (TextBuffer) textBufferField.get(p);
        tb.resetWithString("dGVzdA=="); // "test" in base64
        byte[] result = p.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(new byte[]{'t', 'e', 's', 't'}, result);
    }

    @Test(timeout = 4000)
    public void testGetCurrentValue() throws Exception {
        TestParser p = createParser(0);
        assertNull(p.getCurrentValue());
        Object val = new Object();
        p.setCurrentValue(val);
        assertSame(val, p.getCurrentValue());
    }

    @Test(timeout = 4000)
    public void testVersion() throws Exception {
        TestParser p = createParser(0);
        assertNotNull(p.version());
    }

    @Test(timeout = 4000)
    public void testGetParsingContext() throws Exception {
        TestParser p = createParser(0);
        assertNotNull(p.getParsingContext());
        assertTrue(p.getParsingContext().inRoot());
    }

    @Test(timeout = 4000)
    public void testReleaseBuffers() throws Exception {
        TestParser p = createParser(0);
        // Should not throw
        p._releaseBuffers();
    }

    @Test(timeout = 4000)
    public void testGetByteArrayBuilder() throws Exception {
        TestParser p = createParser(0);
        assertNotNull(p._getByteArrayBuilder());
        // Second call should return same builder (reset)
        assertSame(p._getByteArrayBuilder(), p._getByteArrayBuilder());
    }

    @Test(timeout = 4000)
    public void testGrowArrayBy() throws Exception {
        int[] arr = new int[]{1,2,3};
        int[] grown = ParserBase.growArrayBy(arr, 2);
        assertEquals(5, grown.length);
        assertArrayEquals(new int[]{1,2,3,0,0}, grown);
        // null input
        int[] nullGrown = ParserBase.growArrayBy(null, 5);
        assertEquals(5, nullGrown.length);
    }

    @Test(timeout = 4000)
    public void testHandleEOFInRoot() throws Exception {
        TestParser p = createParser(0);
        // Should not throw because in root
        p._handleEOF();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testHandleEOFInArray() throws Exception {
        TestParser p = createParser(0);
        // Simulate being inside an array
        Field contextField = ParserBase.class.getDeclaredField("_parsingContext");
        contextField.setAccessible(true);
        JsonReadContext arrayCtx = JsonReadContext.createRootContext(null).createChildArrayContext(null);
        contextField.set(p, arrayCtx);
        p._handleEOF();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testHandleEOFInObject() throws Exception {
        TestParser p = createParser(0);
        Field contextField = ParserBase.class.getDeclaredField("_parsingContext");
        contextField.setAccessible(true);
        JsonReadContext objCtx = JsonReadContext.createRootContext(null).createChildObjectContext(null);
        contextField.set(p, objCtx);
        p._handleEOF();
    }

    @Test(timeout = 4000)
    public void testEofAsNextChar() throws Exception {
        TestParser p = createParser(0);
        // Should call _handleEOF and return -1
        assertEquals(-1, p._eofAsNextChar());
    }

    @Test(timeout = 4000)
    public void testReportMismatchedEndMarker() throws Exception {
        TestParser p = createParser(0);
        try {
            p._reportMismatchedEndMarker(']', '}');
            fail("Expected exception");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Unexpected close marker"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleUnrecognizedCharacterEscape() throws Exception {
        TestParser p = createParser(0);
        // Without ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, should throw
        try {
            p._handleUnrecognizedCharacterEscape('x');
            fail("Expected exception");
        } catch (JsonProcessingException e) {
            assertTrue(e.getMessage().contains("Unrecognized character escape"));
        }
        // With feature enabled, should return the character
        p.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        assertEquals('x', p._handleUnrecognizedCharacterEscape('x'));
    }

    @Test(timeout = 4000)
    public void testThrowUnquotedSpace() throws Exception {
        TestParser p = createParser(0);
        // Without ALLOW_UNQUOTED_CONTROL_CHARS, should throw for space
        try {
            p._throwUnquotedSpace(0x20, "test");
            fail("Expected exception");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Illegal unquoted character"));
        }
        // With feature enabled, should not throw for space
        p.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        p._throwUnquotedSpace(0x20, "test"); // no exception
        // But should still throw for control char below space
        try {
            p._throwUnquotedSpace(0x00, "test");
            fail("Expected exception");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDecodeBase64Escape() throws Exception {
        TestParser p = createParser(0);
        // Test with backslash escape
        // We need to mock _decodeEscaped to return a valid base64 char
        // Since we can't easily mock, we'll test the error path
        try {
            p._decodeBase64Escape(Base64Variants.MIME, '\\', 0);
            fail("Expected exception from _decodeEscaped");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReportInvalidBase64Char() throws Exception {
        TestParser p = createParser(0);
        IllegalArgumentException iae = p.reportInvalidBase64Char(Base64Variants.MIME, 0x00, 0);
        assertTrue(iae.getMessage().contains("Illegal white space character"));
    }

    @Test(timeout = 4000)
    public void testHandleBase64MissingPadding() throws Exception {
        TestParser p = createParser(0);
        try {
            p._handleBase64MissingPadding(Base64Variants.MIME);
            fail("Expected exception");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Base64"));
        }
    }

    @Test(timeout = 4000)
    public void testGetSourceReference() throws Exception {
        TestParser p = createParser(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION.getMask());
        Object ref = p._getSourceReference();
        assertNotNull(ref);
        // Without feature
        TestParser p2 = createParser(0);
        assertNull(p2._getSourceReference());
    }
}