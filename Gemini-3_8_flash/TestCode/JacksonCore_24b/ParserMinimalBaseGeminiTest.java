/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.core.base.ParserMinimalBase
 * Defect Reference: Defects4J JacksonCore NumberCoercion / NumberOverflow issues
 *
 * Specific Defect Targeted:
 * - reportOverflowInt() and reportOverflowLong() were improperly delegating to _reportError(),
 *   which instantiates and throws a generic JsonParseException instead of the specialized
 *   InputCoercionException mandated by the 2.10+ specification and method Javadoc contract.
 * - Calling reportOverflowInt() / reportOverflowLong() should result in an InputCoercionException.
 *
 * Branch Coverage Targets:
 * - nextValue(): field name skip vs non-field name
 * - skipChildren(): non-struct token early return, nesting loop (START_OBJECT/START_ARRAY increment,
 *   struct end decrement until open == 0, null EOF handling, NOT_AVAILABLE non-blocking check)
 * - clearCurrentToken() & getLastClearedToken(): _currToken null vs non-null transitions
 * - hasTokenId(), hasToken(), hasCurrentToken(), isExpectedStartArrayToken(), isExpectedStartObjectToken()
 * - getValueAsBoolean(): ID_STRING ("true", "false", "null", unknown), ID_NUMBER_INT (!=0, ==0),
 *   ID_TRUE, ID_FALSE, ID_NULL, ID_EMBEDDED_OBJECT (Boolean vs non-Boolean), default fallback
 * - getValueAsInt() & getValueAsLong(): numeric tokens shortcut vs ID_STRING (textual null vs number parse),
 *   ID_TRUE (1), ID_FALSE (0), ID_NULL (0), ID_EMBEDDED_OBJECT (Number vs non-Number), fallback
 * - getValueAsDouble(): ID_STRING, ID_NUMBER_INT / FLOAT, ID_TRUE, ID_FALSE, ID_NULL,
 *   ID_EMBEDDED_OBJECT, fallback
 * - getValueAsString(): VALUE_STRING, FIELD_NAME, null/VALUE_NULL/non-scalar fallback vs scalar getText()
 * - _decodeBase64(): valid Base64 decode vs caught IllegalArgumentException delegating to _reportError()
 * - reportUnexpectedNumberChar(), reportInvalidNumber(), _reportUnexpectedChar() (ch < 0 vs >= 0)
 * - _reportInvalidEOF(), _reportInvalidEOFInValue() for String, Number, other
 * - _longIntegerDesc() and _longNumberDesc(): length < 1000 vs >= 1000 (with/without negative sign)
 * - _getCharDesc(): ISO control chars, chars > 255 (hex repr), regular chars
 * - _asciiBytes() & _ascii() roundtrip
 */

package com.fasterxml.jackson.core.base;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

public class ParserMinimalBaseGeminiTest {

    /**
     * Concrete test harness subclass exposing protected methods and state for white-box inspection.
     */
    private static class MinimalParserStub extends ParserMinimalBase {
        private final Queue<JsonToken> tokenQueue = new LinkedList<>();
        private String textValue;
        private String currentNameValue;
        private Object embeddedObjectValue;
        private int intVal;
        private long longVal;
        private double doubleVal;
        private boolean closed = false;

        public MinimalParserStub() {
            super();
        }

        public MinimalParserStub(int features) {
            super(features);
        }

        public void setTokens(JsonToken... tokens) {
            tokenQueue.clear();
            tokenQueue.addAll(Arrays.asList(tokens));
        }

        public void setCurrentTokenDirectly(JsonToken t) {
            this._currToken = t;
        }

        public void setTextValue(String s) {
            this.textValue = s;
        }

        public void setEmbeddedObjectValue(Object obj) {
            this.embeddedObjectValue = obj;
        }

        public void setNumericValues(int i, long l, double d) {
            this.intVal = i;
            this.longVal = l;
            this.doubleVal = d;
        }

        @Override
        public JsonToken nextToken() throws IOException {
            _currToken = tokenQueue.poll();
            return _currToken;
        }

        @Override
        protected void _handleEOF() throws JsonParseException {
            _reportInvalidEOF(" in stub");
        }

        @Override
        public String getCurrentName() throws IOException {
            return currentNameValue;
        }

        @Override
        public void overrideCurrentName(String name) {
            this.currentNameValue = name;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public JsonStreamContext getParsingContext() {
            return null;
        }

        @Override
        public String getText() throws IOException {
            return textValue;
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            return textValue != null ? textValue.toCharArray() : null;
        }

        @Override
        public boolean hasTextCharacters() {
            return textValue != null;
        }

        @Override
        public int getTextLength() throws IOException {
            return textValue != null ? textValue.length() : 0;
        }

        @Override
        public int getTextOffset() throws IOException {
            return 0;
        }

        @Override
        public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
            ByteArrayBuilder builder = new ByteArrayBuilder();
            _decodeBase64(textValue, builder, b64variant);
            return builder.toByteArray();
        }

        @Override
        public Object getEmbeddedObject() throws IOException {
            return embeddedObjectValue;
        }

        @Override
        public int getIntValue() throws IOException {
            return intVal;
        }

        @Override
        public long getLongValue() throws IOException {
            return longVal;
        }

        @Override
        public double getDoubleValue() throws IOException {
            return doubleVal;
        }

        @Override
        public Number getNumberValue() throws IOException {
            return intVal;
        }

        @Override
        public NumberType getNumberType() throws IOException {
            return NumberType.INT;
        }

        @Override
        public BigInteger getBigIntegerValue() throws IOException {
            return BigInteger.valueOf(longVal);
        }

        @Override
        public float getFloatValue() throws IOException {
            return (float) doubleVal;
        }

        @Override
        public BigDecimal getDecimalValue() throws IOException {
            return BigDecimal.valueOf(doubleVal);
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
        public Codec getCodec() {
            return null;
        }

        @Override
        public void setCodec(Codec c) {
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (InputCoercionException Contract)
    // =========================================================================

    @Test(timeout = 4000)
    public void testReportOverflowIntThrowsInputCoercionException() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setCurrentTokenDirectly(JsonToken.VALUE_NUMBER_INT);
        parser.setTextValue("2147483648");

        try {
            parser.reportOverflowInt("2147483648");
            fail("Expected InputCoercionException when int overflows");
        } catch (InputCoercionException ice) {
            // Defects4J fix verification: Must throw InputCoercionException directly
            assertNotNull(ice.getMessage());
            assertTrue("Message should mention out of range of int", ice.getMessage().contains("out of range of int"));
        } catch (JsonParseException jpe) {
            fail("Expected InputCoercionException but received generic JsonParseException: " + jpe.getClass().getName());
        }
    }

    @Test(timeout = 4000)
    public void testReportOverflowIntDefaultParameterThrowsInputCoercionException() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setCurrentTokenDirectly(JsonToken.VALUE_NUMBER_INT);
        parser.setTextValue("99999999999");

        try {
            parser.reportOverflowInt();
            fail("Expected InputCoercionException on reportOverflowInt()");
        } catch (InputCoercionException ice) {
            assertTrue(ice.getMessage().contains("out of range of int"));
        } catch (JsonParseException jpe) {
            fail("Expected InputCoercionException but caught: " + jpe.getClass().getName());
        }
    }

    @Test(timeout = 4000)
    public void testReportOverflowLongThrowsInputCoercionException() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setCurrentTokenDirectly(JsonToken.VALUE_NUMBER_INT);
        parser.setTextValue("9223372036854775817");

        try {
            parser.reportOverflowLong("9223372036854775817");
            fail("Expected InputCoercionException when long overflows");
        } catch (InputCoercionException ice) {
            assertNotNull(ice.getMessage());
            assertTrue("Message should mention out of range of long", ice.getMessage().contains("out of range of long"));
        } catch (JsonParseException jpe) {
            fail("Expected InputCoercionException but received generic JsonParseException: " + jpe.getClass().getName());
        }
    }

    @Test(timeout = 4000)
    public void testReportOverflowLongDefaultParameterThrowsInputCoercionException() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setCurrentTokenDirectly(JsonToken.VALUE_NUMBER_INT);
        parser.setTextValue("-9223372036854775809");

        try {
            parser.reportOverflowLong();
            fail("Expected InputCoercionException on reportOverflowLong()");
        } catch (InputCoercionException ice) {
            assertTrue(ice.getMessage().contains("out of range of long"));
        } catch (JsonParseException jpe) {
            fail("Expected InputCoercionException but caught: " + jpe.getClass().getName());
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTokenInspectionAndClearCurrentToken() {
        MinimalParserStub parser = new MinimalParserStub();
        assertNull(parser.currentToken());
        assertNull(parser.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.currentTokenId());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.getCurrentTokenId());
        assertFalse(parser.hasCurrentToken());
        assertTrue(parser.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(parser.hasTokenId(JsonTokenId.ID_START_OBJECT));
        assertFalse(parser.hasToken(JsonToken.START_OBJECT));

        // Set token
        parser.setCurrentTokenDirectly(JsonToken.START_OBJECT);