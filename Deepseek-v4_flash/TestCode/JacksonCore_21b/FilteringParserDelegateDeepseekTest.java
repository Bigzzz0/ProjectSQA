package com.fasterxml.jackson.core.filter;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.json.JsonFactory;

public class FilteringParserDelegateDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: FilteringParserDelegate.nextToken() and related state machine
     * 
     * Key branches:
     * - _allowMultipleMatches == false && _currToken != null && _exposedContext == null
     *   - _currToken.isStructEnd() -> return null if start handled
     *   - _currToken.isScalarValue() -> return null if !startHandled && itemFilter == INCLUDE_ALL
     * - ID_START_ARRAY / ID_START_OBJECT handling with various filter states
     * - ID_END_ARRAY / ID_END_OBJECT with returnEnd logic
     * - ID_FIELD_NAME with includeProperty and path buffering
     * - Scalar value handling with checkValue and includeValue
     * - _nextTokenWithBuffering for path inclusion
     * 
     * Defect targeted (from Defects4J):
     * - When _allowMultipleMatches=false, _includePath=true, and a single match is found
     *   inside an object, the END_OBJECT token is incorrectly suppressed or null returned.
     *   Specifically, after returning the matching scalar value, the subsequent END_OBJECT
     *   is not returned, causing assertion failures like:
     *   - "Expected token END_OBJECT, current token null"
     *   - "expected:<{\"ob\":{\"value\":3}[}]> but was:<{\"ob\":{\"value\":3}[]>" (missing END_OBJECT)
     *   - "expected:<END_OBJECT> but was:<null>"
     * 
     * The bug is in the main_loop handling: after a scalar value is returned with buffering,
     * the END_OBJECT token is consumed but not returned because _headContext.isStartHandled()
     * returns false or the returnEnd logic fails.
     * 
     * Test strategy:
     * - Partition A: Core functional - basic filtering with includePath, single match
     * - Partition B: Boundary - empty objects, null filters, INCLUDE_ALL
     * - Partition C: Defect-targeted - exact reproduction of the failing scenario
     * - Partition D: Exception paths - unsupported overrideCurrentName
     * - Partition E: Contract - getFilter, getMatchCount, token state
     */

    private final JsonFactory factory = new JsonFactory();

    // ========== Helper methods ==========

    private JsonParser createParser(String json) throws IOException {
        return factory.createParser(new StringReader(json));
    }

    private FilteringParserDelegate createDelegate(String json, TokenFilter filter,
            boolean includePath, boolean allowMultipleMatches) throws IOException {
        JsonParser delegate = createParser(json);
        return new FilteringParserDelegate(delegate, filter, includePath, allowMultipleMatches);
    }

    private String collectTokens(FilteringParserDelegate parser) throws IOException {
        StringBuilder sb = new StringBuilder();
        JsonToken t;
        while ((t = parser.nextToken()) != null) {
            sb.append(t.name());
            if (t == JsonToken.FIELD_NAME || t == JsonToken.VALUE_STRING) {
                sb.append("(").append(parser.getCurrentName()).append(")");
            } else if (t.isScalarValue()) {
                sb.append("(").append(parser.getText()).append(")");
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testBasicSingleMatchWithPath() throws IOException {
        String json = "{\"ob\":{\"value\":3}}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return "value".equals(p.getCurrentName());
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("ob", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("value", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        assertEquals(3, parser.getIntValue());
        t = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testBasicSingleMatchNoPath() throws IOException {
        String json = "{\"ob\":{\"value\":3}}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return "value".equals(p.getCurrentName());
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, false, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("ob", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("value", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        assertEquals(3, parser.getIntValue());
        t = parser.nextToken();
        assertNull(t); // No buffered END_OBJECT when !includePath
    }

    @Test(timeout = 4000)
    public void testMultipleMatchesAllowed() throws IOException {
        String json = "{\"a\":1,\"b\":2}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return true;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, true);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("a", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("b", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testIncludeAllFilter() throws IOException {
        String json = "{\"a\":1}";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("a", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testNullFilter() throws IOException {
        String json = "{\"a\":1}";
        FilteringParserDelegate parser = createDelegate(json, null, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertNull(t); // All filtered out
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testEmptyObjectWithPath() throws IOException {
        String json = "{}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return false;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testEmptyArrayWithPath() throws IOException {
        String json = "[]";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return false;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_ARRAY, t);
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testNestedArrayWithPath() throws IOException {
        String json = "[[1,2],[3,4]]";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return true;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_ARRAY, t);
        t = parser.nextToken();
        assertEquals(JsonToken.START_ARRAY, t);
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_ARRAY, t);
        t = parser.nextToken();
        assertEquals(JsonToken.START_ARRAY, t);
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_ARRAY, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_ARRAY, t);
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testScalarRoot() throws IOException {
        String json = "42";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return true;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        assertEquals(42, parser.getIntValue());
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testStringRoot() throws IOException {
        String json = "\"hello\"";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return true;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.VALUE_STRING, t);
        assertEquals("hello", parser.getText());
        t = parser.nextToken();
        assertNull(t);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Directly reproduces the defect from Defects4J:
     * TestTokensSingleMatchWithPath - expected END_OBJECT but got null
     */
    @Test(timeout = 4000)
    public void testSingleMatchWithPathEndObject() throws IOException {
        String json = "{\"ob\":{\"value\":3}}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return "value".equals(p.getCurrentName());
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        // This is where the bug occurs - END_OBJECT should be returned
        t = parser.nextToken();
        assertEquals("Expected END_OBJECT after value", JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertEquals("Expected END_OBJECT for outer object", JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    /**
     * Reproduces the exact assertion from TestBasicSingleMatchFilteringWithPath
     * where the output was missing the closing bracket.
     */
    @Test(timeout = 4000)
    public void testBasicSingleMatchFilteringWithPathOutput() throws IOException {
        String json = "{\"ob\":{\"value\":3}}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return "value".equals(p.getCurrentName());
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        StringBuilder sb = new StringBuilder();
        JsonToken t;
        while ((t = parser.nextToken()) != null) {
            if (t == JsonToken.FIELD_NAME) {
                sb.append("\"").append(parser.getCurrentName()).append("\":");
            } else if (t == JsonToken.START_OBJECT) {
                sb.append("{");
            } else if (t == JsonToken.END_OBJECT) {
                sb.append("}");
            } else if (t == JsonToken.VALUE_NUMBER_INT) {
                sb.append(parser.getIntValue());
            }
        }
        assertEquals("{\"ob\":{\"value\":3}}", sb.toString());
    }

    /**
     * Reproduces TestSkippingForSingleWithPath - expected END_OBJECT but got null
     */
    @Test(timeout = 4000)
    public void testSkippingForSingleWithPath() throws IOException {
        String json = "{\"a\":1,\"b\":{\"c\":2},\"d\":3}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return "c".equals(p.getCurrentName());
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("a", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("b", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("c", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        t = parser.nextToken();
        assertEquals("Expected END_OBJECT for inner object", JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertEquals("Expected END_OBJECT for outer object", JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testSingleMatchInArrayWithPath() throws IOException {
        String json = "{\"arr\":[1,2,3]}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return p.getIntValue() == 2;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("arr", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.START_ARRAY, t);
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        assertEquals(2, parser.getIntValue());
        t = parser.nextToken();
        assertEquals("Expected END_ARRAY", JsonToken.END_ARRAY, t);
        t = parser.nextToken();
        assertEquals("Expected END_OBJECT", JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testOverrideCurrentNameThrows() throws IOException {
        String json = "{\"a\":1}";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        parser.overrideCurrentName("test");
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnObject() throws IOException {
        String json = "{\"a\":{\"b\":1},\"c\":2}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return "c".equals(p.getCurrentName());
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("a", parser.getCurrentName());
        parser.skipChildren();
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("c", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testNextValue() throws IOException {
        String json = "{\"a\":1,\"b\":2}";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextValue();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        assertEquals(1, parser.getIntValue());
        t = parser.nextValue();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        assertEquals(2, parser.getIntValue());
        t = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testGetFilter() throws IOException {
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return true;
            }
        };
        FilteringParserDelegate parser = createDelegate("{}", filter, true, false);
        assertSame(filter, parser.getFilter());
    }

    @Test(timeout = 4000)
    public void testGetMatchCount() throws IOException {
        String json = "{\"a\":1,\"b\":2}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return true;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        while (parser.nextToken() != null) {
            // consume all
        }
        assertTrue(parser.getMatchCount() > 0);
    }

    @Test(timeout = 4000)
    public void testGetCurrentTokenInitial() throws IOException {
        FilteringParserDelegate parser = createDelegate("{}", TokenFilter.INCLUDE_ALL, true, false);
        assertNull(parser.getCurrentToken());
        assertNull(parser.currentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.getCurrentTokenId());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.currentTokenId());
        assertFalse(parser.hasCurrentToken());
        assertFalse(parser.hasTokenId(JsonTokenId.ID_START_OBJECT));
        assertFalse(parser.hasToken(JsonToken.START_OBJECT));
    }

    @Test(timeout = 4000)
    public void testClearCurrentToken() throws IOException {
        FilteringParserDelegate parser = createDelegate("{}", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        assertNotNull(parser.getCurrentToken());
        parser.clearCurrentToken();
        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, parser.getLastClearedToken());
    }

    @Test(timeout = 4000)
    public void testGetParsingContext() throws IOException {
        FilteringParserDelegate parser = createDelegate("{\"a\":1}", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        JsonStreamContext ctxt = parser.getParsingContext();
        assertNotNull(ctxt);
        assertEquals(JsonStreamContext.TYPE_OBJECT, ctxt.getType());
    }

    @Test(timeout = 4000)
    public void testGetCurrentName() throws IOException {
        FilteringParserDelegate parser = createDelegate("{\"a\":1}", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        parser.nextToken();
        assertEquals("a", parser.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testGetTextDelegation() throws IOException {
        FilteringParserDelegate parser = createDelegate("\"hello\"", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        assertEquals("hello", parser.getText());
        assertTrue(parser.hasTextCharacters());
        assertNotNull(parser.getTextCharacters());
        assertEquals(5, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
    }

    @Test(timeout = 4000)
    public void testNumericDelegation() throws IOException {
        FilteringParserDelegate parser = createDelegate("42", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        assertEquals(42, parser.getIntValue());
        assertEquals(42L, parser.getLongValue());
        assertEquals(42.0, parser.getDoubleValue(), 0.001);
        assertEquals(42.0f, parser.getFloatValue(), 0.001);
        assertEquals(BigInteger.valueOf(42), parser.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(42), parser.getDecimalValue());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
        assertEquals(42, parser.getNumberValue());
    }

    @Test(timeout = 4000)
    public void testBooleanDelegation() throws IOException {
        FilteringParserDelegate parser = createDelegate("true", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        assertTrue(parser.getBooleanValue());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(false));
    }

    @Test(timeout = 4000)
    public void testValueAsMethods() throws IOException {
        FilteringParserDelegate parser = createDelegate("42", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(0));
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(0L));
        assertEquals(42.0, parser.getValueAsDouble(), 0.001);
        assertEquals(42.0, parser.getValueAsDouble(0.0), 0.001);
        assertEquals("42", parser.getValueAsString());
        assertEquals("42", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObject() throws IOException {
        FilteringParserDelegate parser = createDelegate("null", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        assertNull(parser.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetBinaryValue() throws IOException {
        FilteringParserDelegate parser = createDelegate("\"AQID\"", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        byte[] expected = new byte[] {1, 2, 3};
        assertArrayEquals(expected, parser.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testGetTokenLocation() throws IOException {
        FilteringParserDelegate parser = createDelegate("{}", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        assertNotNull(parser.getTokenLocation());
    }

    @Test(timeout = 4000)
    public void testIsExpectedStartArrayToken() throws IOException {
        FilteringParserDelegate parser = createDelegate("[]", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        assertTrue(parser.isExpectedStartArrayToken());
        assertFalse(parser.isExpectedStartObjectToken());
    }

    @Test(timeout = 4000)
    public void testIsExpectedStartObjectToken() throws IOException {
        FilteringParserDelegate parser = createDelegate("{}", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        assertTrue(parser.isExpectedStartObjectToken());
        assertFalse(parser.isExpectedStartArrayToken());
    }

    @Test(timeout = 4000)
    public void testGetCurrentLocation() throws IOException {
        FilteringParserDelegate parser = createDelegate("{}", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        assertNotNull(parser.getCurrentLocation());
    }

    @Test(timeout = 4000)
    public void testReadBinaryValue() throws IOException {
        FilteringParserDelegate parser = createDelegate("\"AQID\"", TokenFilter.INCLUDE_ALL, true, false);
        parser.nextToken();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int count = parser.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(3, count);
        assertArrayEquals(new byte[] {1, 2, 3}, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testFilterFinishArray() throws IOException {
        String json = "{\"a\":[1,2,3]}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return p.getIntValue() == 2;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        t = parser.nextToken();
        assertEquals(JsonToken.START_ARRAY, t);
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        assertEquals(2, parser.getIntValue());
        t = parser.nextToken();
        assertEquals(JsonToken.END_ARRAY, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testIncludePropertyWithNull() throws IOException {
        String json = "{\"a\":1}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return null;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testIncludePropertyWithIncludeAll() throws IOException {
        String json = "{\"a\":1}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return TokenFilter.INCLUDE_ALL;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("a", parser.getCurrentName());
        t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testFilterStartObject() throws IOException {
        String json = "{\"a\":{}}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public JsonToken filterStartObject() {
                return JsonToken.START_OBJECT;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }

    @Test(timeout = 4000)
    public void testFilterStartArray() throws IOException {
        String json = "{\"a\":[]}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public JsonToken filterStartArray() {
                return JsonToken.START_ARRAY;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, false);
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        t = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        t = parser.nextToken();
        assertEquals(JsonToken.START_ARRAY, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_ARRAY, t);
        t = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        t = parser.nextToken();
        assertNull(t);
    }
}