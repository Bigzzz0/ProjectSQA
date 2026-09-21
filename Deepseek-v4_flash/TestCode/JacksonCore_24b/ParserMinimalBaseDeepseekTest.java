package com.fasterxml.jackson.core.base;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for {@link ParserMinimalBase}.
 * Targets all concrete methods, branches, and the known overflow defect.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core state transitions (currentToken, clearCurrentToken, hasToken, etc.)
 * - Partition B: Boundary values (null token, empty string, MAX/MIN boundaries)
 * - Partition C: Defect-targeted overflow paths (reportOverflowInt/Long, _longIntegerDesc)
 * - Partition D: Exception/defensive paths (skipChildren NOT_AVAILABLE, _reportInvalidEOF, etc.)
 * - Partition E: Contract integrity (getValueAsXxx with all token types)
 *
 * Known defect: Number overflow detection must throw JsonParseException with precise message.
 */
public class ParserMinimalBaseDeepseekTest {

    // ----------------------------------------------------------
    // Concrete test stub that extends ParserMinimalBase
    // ----------------------------------------------------------
    static class TestParser extends ParserMinimalBase {
        private String _text;
        private int _intValue;
        private long _longValue;
        private double _doubleValue;
        private Object _embeddedObject;
        private boolean _closed;
        private JsonStreamContext _parsingContext;

        public TestParser() {
            super(0);
        }

        public TestParser(int features) {
            super(features);
        }

        // Setters for test control
        public void setCurrentToken(JsonToken t) { _currToken = t; }
        public void setText(String s) { _text = s; }
        public void setIntValue(int v) { _intValue = v; }
        public void setLongValue(long v) { _longValue = v; }
        public void setDoubleValue(double v) { _doubleValue = v; }
        public void setEmbeddedObject(Object o) { _embeddedObject = o; }
        public void setParsingContext(JsonStreamContext c) { _parsingContext = c; }

        // Expose protected methods for testing
        public void exposeReportOverflowInt() throws IOException { reportOverflowInt(); }
        public void exposeReportOverflowInt(String numDesc) throws IOException { reportOverflowInt(numDesc); }
        public void exposeReportOverflowLong() throws IOException { reportOverflowLong(); }
        public void exposeReportOverflowLong(String numDesc) throws IOException { reportOverflowLong(numDesc); }
        public void exposeReportUnexpectedNumberChar(int ch, String comment) throws JsonParseException { reportUnexpectedNumberChar(ch, comment); }
        public void exposeReportInvalidNumber(String msg) throws JsonParseException { reportInvalidNumber(msg); }
        public void exposeReportUnexpectedChar(int ch, String comment) throws JsonParseException { _reportUnexpectedChar(ch, comment); }
        public void exposeReportInvalidEOF() throws JsonParseException { _reportInvalidEOF(); }
        public void exposeReportInvalidEOFInValue(JsonToken type) throws JsonParseException { _reportInvalidEOFInValue(type); }
        public void exposeReportInvalidEOF(String msg, JsonToken currToken) throws JsonParseException { _reportInvalidEOF(msg, currToken); }
        public void exposeReportMissingRootWS(int ch) throws JsonParseException { _reportMissingRootWS(ch); }
        public void exposeThrowInvalidSpace(int i) throws JsonParseException { _throwInvalidSpace(i); }
        public String exposeGetCharDesc(int ch) { return _getCharDesc(ch); }
        public void exposeReportError(String msg) throws JsonParseException { _reportError(msg); }
        public void exposeReportError(String msg, Object arg) throws JsonParseException { _reportError(msg, arg); }
        public void exposeReportError(String msg, Object arg1, Object arg2) throws JsonParseException { _reportError(msg, arg1, arg2); }
        public void exposeWrapError(String msg, Throwable t) throws JsonParseException { _wrapError(msg, t); }
        public void exposeThrowInternal() { _throwInternal(); }
        public JsonParseException exposeConstructError(String msg, Throwable t) { return _constructError(msg, t); }
        public String exposeLongIntegerDesc(String rawNum) { return _longIntegerDesc(rawNum); }
        public String exposeLongNumberDesc(String rawNum) { return _longNumberDesc(rawNum); }
        public boolean exposeHasTextualNull(String value) { return _hasTextualNull(value); }
        public void exposeDecodeBase64(String str, ByteArrayBuilder builder, Base64Variant b64variant) throws IOException {
            _decodeBase64(str, builder, b64variant);
        }

        // Abstract method implementations (minimal stubs)
        @Override public JsonToken nextToken() throws IOException {
            // Not used in these tests; return null to avoid infinite loops
            return null;
        }
        @Override public String getCurrentName() throws IOException { return _text; }
        @Override public void close() throws IOException { _closed = true; }
        @Override public boolean isClosed() { return _closed; }
        @Override public JsonStreamContext getParsingContext() { return _parsingContext; }
        @Override public String getText() throws IOException { return _text; }
        @Override public char[] getTextCharacters() throws IOException { return _text.toCharArray(); }
        @Override public boolean hasTextCharacters() { return true; }
        @Override public int getTextLength() throws IOException { return _text.length(); }
        @Override public int getTextOffset() throws IOException { return 0; }
        @Override public byte[] getBinaryValue(Base64Variant b64variant) throws IOException { return new byte[0]; }
        @Override public Object getEmbeddedObject() throws IOException { return _embeddedObject; }
        @Override public int getIntValue() throws IOException { return _intValue; }
        @Override public long getLongValue() throws IOException { return _longValue; }
        @Override public BigInteger getBigIntegerValue() throws IOException { return BigInteger.valueOf(_longValue); }
        @Override public float getFloatValue() throws IOException { return (float)_doubleValue; }
        @Override public double getDoubleValue() throws IOException { return _doubleValue; }
        @Override public BigDecimal getDecimalValue() throws IOException { return BigDecimal.valueOf(_doubleValue); }
        @Override public void overrideCurrentName(String name) { _text = name; }
        @Override protected void _handleEOF() throws JsonParseException {
            throw new JsonEOFException(this, null, "Unexpected end-of-input");
        }
    }

    // ----------------------------------------------------------
    // Partition A: Core state transitions
    // ----------------------------------------------------------
    @Test(timeout = 4000)
    public void testCurrentTokenAndId() {
        TestParser p = new TestParser();
        assertNull("initial token should be null", p.currentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, p.currentTokenId());
        assertNull(p.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, p.getCurrentTokenId());
        assertFalse(p.hasCurrentToken());
        assertFalse(p.hasTokenId(JsonTokenId.ID_START_OBJECT));
        assertTrue(p.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(p.hasToken(JsonToken.START_OBJECT));

        p.setCurrentToken(JsonToken.START_ARRAY);
        assertEquals(JsonToken.START_ARRAY, p.currentToken());
        assertEquals(JsonTokenId.ID_START_ARRAY, p.currentTokenId());
        assertTrue(p.hasCurrentToken());
        assertTrue(p.hasTokenId(JsonTokenId.ID_START_ARRAY));
        assertFalse(p.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertTrue(p.hasToken(JsonToken.START_ARRAY));
        assertFalse(p.hasToken(JsonToken.START_OBJECT));
        assertTrue(p.isExpectedStartArrayToken());
        assertFalse(p.isExpectedStartObjectToken());

        p.setCurrentToken(JsonToken.START_OBJECT);
        assertTrue(p.isExpectedStartObjectToken());
        assertFalse(p.isExpectedStartArrayToken());
    }

    @Test(timeout = 4000)
    public void testClearCurrentToken() {
        TestParser p = new TestParser();
        p.setCurrentToken(JsonToken.VALUE_STRING);
        p.clearCurrentToken();
        assertNull(p.currentToken());
        assertEquals(JsonToken.VALUE_STRING, p.getLastClearedToken());

        // clearing when already null should not change lastCleared
        p.clearCurrentToken();
        assertNull(p.currentToken());
        assertEquals(JsonToken.VALUE_STRING, p.getLastClearedToken());
    }

    @Test(timeout = 4000)
    public void testNextValue() throws IOException {
        TestParser p = new TestParser();
        // nextValue calls nextToken, which returns null in stub -> returns null
        assertNull(p.nextValue());

        // Simulate FIELD_NAME followed by another token
        p.setCurrentToken(JsonToken.FIELD_NAME);
        // nextToken will be called again; we need to override nextToken for this test
        // We'll use a custom subclass for this specific scenario
    }

    // Use a custom parser for nextValue test
    static class NextValueParser extends TestParser {
        private int callCount = 0;
        @Override
        public JsonToken nextToken() throws IOException {
            callCount++;
            if (callCount == 1) return JsonToken.FIELD_NAME;
            if (callCount == 2) return JsonToken.VALUE_NUMBER_INT;
            return null;
        }
    }

    @Test(timeout = 4000)
    public void testNextValueWithFieldName() throws IOException {
        NextValueParser p = new NextValueParser();
        JsonToken t = p.nextValue();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
    }

    @Test(timeout = 4000)
    public void testSkipChildren() throws IOException {
        TestParser p = new TestParser();
        // Not start token -> returns this
        p.setCurrentToken(JsonToken.VALUE_STRING);
        assertSame(p, p.skipChildren());

        // Start array with one level
        p.setCurrentToken(JsonToken.START_ARRAY);
        // We need to simulate nextToken returning END_ARRAY after one call
        // Use a custom parser
    }

    static class SkipChildrenParser extends TestParser {
        private int callCount = 0;
        @Override
        public JsonToken nextToken() throws IOException {
            callCount++;
            if (callCount == 1) return JsonToken.END_ARRAY;
            return null;
        }
    }

    @Test(timeout = 4000)
    public void testSkipChildrenSimple() throws IOException {
        SkipChildrenParser p = new SkipChildrenParser();
        p.setCurrentToken(JsonToken.START_ARRAY);
        assertSame(p, p.skipChildren());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenNested() throws IOException {
        // Nested: start object, then start array, then end array, then end object
        class NestedParser extends TestParser {
            private int callCount = 0;
            @Override
            public JsonToken nextToken() throws IOException {
                callCount++;
                switch (callCount) {
                    case 1: return JsonToken.START_ARRAY;
                    case 2: return JsonToken.END_ARRAY;
                    case 3: return JsonToken.END_OBJECT;
                    default: return null;
                }
            }
        }
        NestedParser p = new NestedParser();
        p.setCurrentToken(JsonToken.START_OBJECT);
        assertSame(p, p.skipChildren());
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testSkipChildrenNotAvailable() throws IOException {
        class NotAvailParser extends TestParser {
            @Override
            public JsonToken nextToken() throws IOException {
                return JsonToken.NOT_AVAILABLE;
            }
        }
        NotAvailParser p = new NotAvailParser();
        p.setCurrentToken(JsonToken.START_OBJECT);
        p.skipChildren();
    }

    // ----------------------------------------------------------
    // Partition B: Boundary values and null handling
    // ----------------------------------------------------------
    @Test(timeout = 4000)
    public void testHasTextualNull() {
        TestParser p = new TestParser();
        assertTrue(p.exposeHasTextualNull("null"));
        assertFalse(p.exposeHasTextualNull("NULL"));
        assertFalse(p.exposeHasTextualNull(""));
        assertFalse(p.exposeHasTextualNull(null));
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanBoundaries() throws IOException {
        TestParser p = new TestParser();
        // null token -> default
        assertFalse(p.getValueAsBoolean(false));
        assertTrue(p.getValueAsBoolean(true));

        // VALUE_NULL -> false
        p.setCurrentToken(JsonToken.VALUE_NULL);
        assertFalse(p.getValueAsBoolean(true));

        // VALUE_TRUE -> true
        p.setCurrentToken(JsonToken.VALUE_TRUE);
        assertTrue(p.getValueAsBoolean(false));

        // VALUE_FALSE -> false
        p.setCurrentToken(JsonToken.VALUE_FALSE);
        assertFalse(p.getValueAsBoolean(true));

        // VALUE_NUMBER_INT non-zero -> true
        p.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        p.setIntValue(42);
        assertTrue(p.getValueAsBoolean(false));

        // VALUE_NUMBER_INT zero -> false
        p.setIntValue(0);
        assertFalse(p.getValueAsBoolean(true));

        // VALUE_STRING "true" -> true
        p.setCurrentToken(JsonToken.VALUE_STRING);
        p.setText("true");
        assertTrue(p.getValueAsBoolean(false));

        // VALUE_STRING "false" -> false
        p.setText("false");
        assertFalse(p.getValueAsBoolean(true));

        // VALUE_STRING "null" -> false
        p.setText("null");
        assertFalse(p.getValueAsBoolean(true));

        // VALUE_STRING other -> default
        p.setText("other");
        assertTrue(p.getValueAsBoolean(true));

        // EMBEDDED_OBJECT Boolean
        p.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.setEmbeddedObject(Boolean.TRUE);
        assertTrue(p.getValueAsBoolean(false));
        p.setEmbeddedObject(Boolean.FALSE);
        assertFalse(p.getValueAsBoolean(true));
        p.setEmbeddedObject("notBoolean");
        assertTrue(p.getValueAsBoolean(true)); // default
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntBoundaries() throws IOException {
        TestParser p = new TestParser();
        // null token -> default
        assertEquals(10, p.getValueAsInt(10));

        // VALUE_NUMBER_INT -> getIntValue
        p.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        p.setIntValue(123);
        assertEquals(123, p.getValueAsInt(0));
        assertEquals(123, p.getValueAsInt());

        // VALUE_NUMBER_FLOAT -> getIntValue (truncation)
        p.setCurrentToken(JsonToken.VALUE_NUMBER_FLOAT);
        p.setIntValue(456);
        assertEquals(456, p.getValueAsInt(0));

        // VALUE_STRING "null" -> 0
        p.setCurrentToken(JsonToken.VALUE_STRING);
        p.setText("null");
        assertEquals(0, p.getValueAsInt(99));

        // VALUE_STRING numeric string -> parsed
        p.setText("789");
        assertEquals(789, p.getValueAsInt(0));

        // VALUE_STRING non-numeric -> default
        p.setText("abc");
        assertEquals(99, p.getValueAsInt(99));

        // VALUE_TRUE -> 1
        p.setCurrentToken(JsonToken.VALUE_TRUE);
        assertEquals(1, p.getValueAsInt(0));

        // VALUE_FALSE -> 0
        p.setCurrentToken(JsonToken.VALUE_FALSE);
        assertEquals(0, p.getValueAsInt(99));

        // VALUE_NULL -> 0
        p.setCurrentToken(JsonToken.VALUE_NULL);
        assertEquals(0, p.getValueAsInt(99));

        // EMBEDDED_OBJECT Number
        p.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.setEmbeddedObject(Integer.valueOf(1000));
        assertEquals(1000, p.getValueAsInt(0));
        p.setEmbeddedObject("notNumber");
        assertEquals(0, p.getValueAsInt(0)); // default
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongBoundaries() throws IOException {
        TestParser p = new TestParser();
        assertEquals(5L, p.getValueAsLong(5L));

        p.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        p.setLongValue(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, p.getValueAsLong(0L));
        assertEquals(Long.MAX_VALUE, p.getValueAsLong());

        p.setCurrentToken(JsonToken.VALUE_NUMBER_FLOAT);
        p.setLongValue(999L);
        assertEquals(999L, p.getValueAsLong(0L));

        p.setCurrentToken(JsonToken.VALUE_STRING);
        p.setText("null");
        assertEquals(0L, p.getValueAsLong(1L));
        p.setText("1234567890123");
        assertEquals(1234567890123L, p.getValueAsLong(0L));
        p.setText("abc");
        assertEquals(1L, p.getValueAsLong(1L));

        p.setCurrentToken(JsonToken.VALUE_TRUE);
        assertEquals(1L, p.getValueAsLong(0L));
        p.setCurrentToken(JsonToken.VALUE_FALSE);
        assertEquals(0L, p.getValueAsLong(1L));
        p.setCurrentToken(JsonToken.VALUE_NULL);
        assertEquals(0L, p.getValueAsLong(1L));

        p.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.setEmbeddedObject(Long.valueOf(42L));
        assertEquals(42L, p.getValueAsLong(0L));
        p.setEmbeddedObject("x");
        assertEquals(0L, p.getValueAsLong(0L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleBoundaries() throws IOException {
        TestParser p = new TestParser();
        assertEquals(3.14, p.getValueAsDouble(3.14), 1e-9);

        p.setCurrentToken(JsonToken.VALUE_STRING);
        p.setText("null");
        assertEquals(0.0, p.getValueAsDouble(1.0), 1e-9);
        p.setText("2.718");
        assertEquals(2.718, p.getValueAsDouble(0.0), 1e-9);
        p.setText("abc");
        assertEquals(1.0, p.getValueAsDouble(1.0), 1e-9);

        p.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        p.setDoubleValue(3.0);
        assertEquals(3.0, p.getValueAsDouble(0.0), 1e-9);

        p.setCurrentToken(JsonToken.VALUE_NUMBER_FLOAT);
        p.setDoubleValue(1.618);
        assertEquals(1.618, p.getValueAsDouble(0.0), 1e-9);

        p.setCurrentToken(JsonToken.VALUE_TRUE);
        assertEquals(1.0, p.getValueAsDouble(0.0), 1e-9);
        p.setCurrentToken(JsonToken.VALUE_FALSE);
        assertEquals(0.0, p.getValueAsDouble(1.0), 1e-9);
        p.setCurrentToken(JsonToken.VALUE_NULL);
        assertEquals(0.0, p.getValueAsDouble(1.0), 1e-9);

        p.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.setEmbeddedObject(Double.valueOf(2.0));
        assertEquals(2.0, p.getValueAsDouble(0.0), 1e-9);
        p.setEmbeddedObject("x");
        assertEquals(0.0, p.getValueAsDouble(0.0), 1e-9);
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringBoundaries() throws IOException {
        TestParser p = new TestParser();
        assertNull(p.getValueAsString());
        assertEquals("default", p.getValueAsString("default"));

        p.setCurrentToken(JsonToken.VALUE_STRING);
        p.setText("hello");
        assertEquals("hello", p.getValueAsString());
        assertEquals("hello", p.getValueAsString("default"));

        p.setCurrentToken(JsonToken.FIELD_NAME);
        p.setText("field");
        assertEquals("field", p.getValueAsString());
        assertEquals("field", p.getValueAsString("default"));

        p.setCurrentToken(JsonToken.VALUE_NULL);
        assertEquals("default", p.getValueAsString("default"));
        assertNull(p.getValueAsString());

        p.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        p.setText("42");
        assertEquals("42", p.getValueAsString("default"));

        p.setCurrentToken(JsonToken.VALUE_TRUE);
        p.setText("true");
        assertEquals("true", p.getValueAsString("default"));

        // non-scalar token (START_OBJECT) -> default
        p.setCurrentToken(JsonToken.START_OBJECT);
        assertEquals("default", p.getValueAsString("default"));
    }

    // ----------------------------------------------------------
    // Partition C: Defect-targeted overflow tests
    // ----------------------------------------------------------
    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testReportOverflowInt() throws IOException {
        TestParser p = new TestParser();
        p.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        p.setText("2147483648"); // > Integer.MAX_VALUE
        p.exposeReportOverflowInt();
    }

    @Test(timeout = 4000)
    public void testReportOverflowIntMessage() throws IOException {
        TestParser p = new TestParser();
        p.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        p.setText("2147483648");
        try {
            p.exposeReportOverflowInt();
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            String msg = e.getMessage();
            assertTrue("Message should contain 'out of range of int'", msg.contains("out of range of int"));
            assertTrue("Message should contain the number", msg.contains("2147483648"));
        }
    }

    @Test(timeout = 4000)
    public void testReportOverflowIntWithDesc() throws IOException {
        TestParser p = new TestParser();
        try {
            p.exposeReportOverflowInt("99999999999999999999");
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("out of range of int"));
            assertTrue(msg.contains("99999999999999999999"));
        }
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testReportOverflowLong() throws IOException {
        TestParser p = new TestParser();
        p.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        p.setText("9223372036854775817"); // > Long.MAX_VALUE
        p.exposeReportOverflowLong();
    }

    @Test(timeout = 4000)
    public void testReportOverflowLongMessage() throws IOException {
        TestParser p = new TestParser();
        p.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        p.setText("9223372036854775817");
        try {
            p.exposeReportOverflowLong();
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            String msg = e.getMessage();
            assertTrue("Message should contain 'out of range of long'", msg.contains("out of range of long"));
            assertTrue("Message should contain the number", msg.contains("9223372036854775817"));
            assertTrue("Message should contain range", msg.contains("-9223372036854775808"));
            assertTrue("Message should contain range", msg.contains("9223372036854775807"));
        }
    }

    @Test(timeout = 4000)
    public void testReportOverflowLongWithDesc() throws IOException {
        TestParser p = new TestParser();
        try {
            p.exposeReportOverflowLong("-9223372036854775809");
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("out of range of long"));
            assertTrue(msg.contains("-9223372036854775809"));
        }
    }

    @Test(timeout = 4000)
    public void testLongIntegerDesc() {
        TestParser p = new TestParser();
        assertEquals("123", p.exposeLongIntegerDesc("123"));
        // Short number (<1000) returns as-is
        String shortNum = "999";
        assertEquals(shortNum, p.exposeLongIntegerDesc(shortNum));
        // Long number (>=1000 digits) returns description
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1999; i++) sb.append('9');
        String longNum = sb.toString();
        String desc = p.exposeLongIntegerDesc(longNum);
        assertTrue(desc.startsWith("[Integer with "));
        assertTrue(desc.endsWith(" digits]"));
        // Negative sign reduces length
        String negLongNum = "-" + longNum;
        desc = p.exposeLongIntegerDesc(negLongNum);
        assertTrue(desc.startsWith("[Integer with "));
        assertTrue(desc.contains("1999")); // 1999 digits after minus
    }

    @Test(timeout = 4000)
    public void testLongNumberDesc() {
        TestParser p = new TestParser();
        assertEquals("123", p.exposeLongNumberDesc("123"));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) sb.append('x');
        String longStr = sb.toString();
        String desc = p.exposeLongNumberDesc(longStr);
        assertTrue(desc.startsWith("[number with "));
        assertTrue(desc.endsWith(" characters]"));
        // Negative sign reduces length
        String negLongStr = "-" + longStr;
        desc = p.exposeLongNumberDesc(negLongStr);
        assertTrue(desc.contains("2000")); // 2000 characters after minus
    }

    // ----------------------------------------------------------
    // Partition D: Exception and defensive paths
    // ----------------------------------------------------------
    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testReportUnexpectedNumberChar() throws JsonParseException {
        TestParser p = new TestParser();
        p.exposeReportUnexpectedNumberChar('x', "test comment");
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testReportInvalidNumber() throws JsonParseException {
        TestParser p = new TestParser();
        p.exposeReportInvalidNumber("bad number");
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testReportUnexpectedChar() throws JsonParseException {
        TestParser p = new TestParser();
        p.exposeReportUnexpectedChar(32, "space");
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testReportInvalidEOF() throws JsonParseException {
        TestParser p = new TestParser();
        p.setCurrentToken(JsonToken.VALUE_STRING);
        p.exposeReportInvalidEOF();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testReportInvalidEOFInValue() throws JsonParseException {
        TestParser p = new TestParser();
        p.exposeReportInvalidEOFInValue(JsonToken.VALUE_STRING);
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testReportInvalidEOFWithMsg() throws JsonParseException {
        TestParser p = new TestParser();
        p.exposeReportInvalidEOF(" in a value", JsonToken.VALUE_NUMBER_INT);
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testReportMissingRootWS() throws JsonParseException {
        TestParser p = new TestParser();
        p.exposeReportMissingRootWS('a');
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testThrowInvalidSpace() throws JsonParseException {
        TestParser p = new TestParser();
        p.exposeThrowInvalidSpace(0x0020); // space is not allowed? Actually only \r,\n,\t allowed
    }

    @Test(timeout = 4000)
    public void testGetCharDesc() {
        TestParser p = new TestParser();
        assertEquals("(CTRL-CHAR, code 0)", p.exposeGetCharDesc(0));
        assertEquals("'a' (code 97)", p.exposeGetCharDesc('a'));
        assertEquals("'\\u20AC' (code 8364 / 0x20ac)", p.exposeGetCharDesc(0x20AC));
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testReportError() throws JsonParseException {
        TestParser p = new TestParser();
        p.exposeReportError("error");
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testReportErrorWithArg() throws JsonParseException {
        TestParser p = new TestParser();
        p.exposeReportError("error %s", "arg");
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testReportErrorWithTwoArgs() throws JsonParseException {
        TestParser p = new TestParser();
        p.exposeReportError("error %s %s", "arg1", "arg2");
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testWrapError() throws JsonParseException {
        TestParser p = new TestParser();
        p.exposeWrapError("wrapped", new RuntimeException("cause"));
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testThrowInternal() {
        TestParser p = new TestParser();
        p.exposeThrowInternal();
    }

    @Test(timeout = 4000)
    public void testConstructError() {
        TestParser p = new TestParser();
        JsonParseException e = p.exposeConstructError("test", new IOException("cause"));
        assertEquals("test", e.getMessage());
        assertNotNull(e.getCause());
    }

    @Test(timeout = 4000)
    public void testDecodeBase64() throws IOException {
        TestParser p = new TestParser();
        ByteArrayBuilder builder = new ByteArrayBuilder();
        // Valid base64
        p.exposeDecodeBase64("dGVzdA==", builder, Base64Variants.MIME);
        assertArrayEquals("test".getBytes(), builder.toByteArray());
        builder.reset();
        // Invalid base64
        try {
            p.exposeDecodeBase64("!!!", builder, Base64Variants.MIME);
            fail("Expected exception");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAsciiBytes() {
        byte[] b = ParserMinimalBase._asciiBytes("ABC");
        assertArrayEquals(new byte[]{65,66,67}, b);
    }

    @Test(timeout = 4000)
    public void testAscii() {
        byte[] b = new byte[]{65,66,67};
        assertEquals("ABC", ParserMinimalBase._ascii(b));
    }

    // ----------------------------------------------------------
    // Partition E: Contract integrity (additional edge cases)
    // ----------------------------------------------------------
    @Test(timeout = 4000)
    public void testHasTokenIdNull() {
        TestParser p = new TestParser();
        assertTrue(p.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(p.hasTokenId(JsonTokenId.ID_START_OBJECT));
    }

    @Test(timeout = 4000)
    public void testHasTokenIdNonNull() {
        TestParser p = new TestParser();
        p.setCurrentToken(JsonToken.START_OBJECT);
        assertTrue(p.hasTokenId(JsonTokenId.ID_START_OBJECT));
        assertFalse(p.hasTokenId(JsonTokenId.ID_NO_TOKEN));
    }

    @Test(timeout = 4000)
    public void testIsExpectedStartArrayObject() {
        TestParser p = new TestParser();
        assertFalse(p.isExpectedStartArrayToken());
        assertFalse(p.isExpectedStartObjectToken());
        p.setCurrentToken(JsonToken.START_ARRAY);
        assertTrue(p.isExpectedStartArrayToken());
        assertFalse(p.isExpectedStartObjectToken());
        p.setCurrentToken(JsonToken.START_OBJECT);
        assertFalse(p.isExpectedStartArrayToken());
        assertTrue(p.isExpectedStartObjectToken());
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntNoToken() throws IOException {
        TestParser p = new TestParser();
        assertEquals(0, p.getValueAsInt());
        assertEquals(7, p.getValueAsInt(7));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongNoToken() throws IOException {
        TestParser p = new TestParser();
        assertEquals(0L, p.getValueAsLong());
        assertEquals(7L, p.getValueAsLong(7L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleNoToken() throws IOException {
        TestParser p = new TestParser();
        assertEquals(0.0, p.getValueAsDouble(0.0), 1e-9);
        assertEquals(3.14, p.getValueAsDouble(3.14), 1e-9);
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringNoToken() throws IOException {
        TestParser p = new TestParser();
        assertNull(p.getValueAsString());
        assertEquals("default", p.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringNullToken() throws IOException {
        TestParser p = new TestParser();
        p.setCurrentToken(JsonToken.VALUE_NULL);
        assertNull(p.getValueAsString());
        assertEquals("default", p.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringNonScalar() throws IOException {
        TestParser p = new TestParser();
        p.setCurrentToken(JsonToken.START_ARRAY);
        assertNull(p.getValueAsString());
        assertEquals("default", p.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringFieldName() throws IOException {
        TestParser p = new TestParser();
        p.setCurrentToken(JsonToken.FIELD_NAME);
        p.setText("myField");
        assertEquals("myField", p.getValueAsString());
        assertEquals("myField", p.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringValueString() throws IOException {
        TestParser p = new TestParser();
        p.setCurrentToken(JsonToken.VALUE_STRING);
        p.setText("value");
        assertEquals("value", p.getValueAsString());
        assertEquals("value", p.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringScalarOther() throws IOException {
        TestParser p = new TestParser();
        p.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        p.setText("42");
        assertEquals("42", p.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testReportInputCoercion() {
        TestParser p = new TestParser();
        try {
            p._reportInputCoercion("coercion error", JsonToken.VALUE_NUMBER_INT, Integer.class);
            fail("Expected InputCoercionException");
        } catch (InputCoercionException e) {
            assertEquals("coercion error", e.getMessage());
            assertEquals(JsonToken.VALUE_NUMBER_INT, e.getInputType());
            assertEquals(Integer.class, e.getTargetType());
        }
    }
}