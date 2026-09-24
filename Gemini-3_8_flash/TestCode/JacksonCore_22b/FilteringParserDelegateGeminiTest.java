/* [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: com.fasterxml.jackson.core.filter.FilteringParserDelegate
 *
 * Core Decision Logic & Branches Targeted:
 * 1. Construction & Initialization:
 *    - rootFilter, _itemFilter, _headContext initialization, _includePath, _allowMultipleMatches
 * 2. Token Accessors & Introspection:
 *    - getCurrentToken(), currentToken(), getCurrentTokenId(), currentTokenId(), hasCurrentToken()
 *    - hasTokenId(int), hasToken(JsonToken), isExpectedStartArrayToken(), isExpectedStartObjectToken()
 *    - getCurrentLocation(), getTokenLocation(), getParsingContext(), getCurrentName()
 *    - Parent name traversal when _currToken is START_OBJECT or START_ARRAY vs other tokens
 * 3. State Overrides:
 *    - clearCurrentToken() and getLastClearedToken()
 *    - overrideCurrentName() throwing UnsupportedOperationException
 * 4. Traversal Logic (nextToken, _nextToken2, _nextTokenWithBuffering, _nextBuffered, nextValue, skipChildren):
 *    - _allowMultipleMatches = false stopping conditions (scalar vs non-scalar)
 *    - Buffering through _exposedContext when _includePath = true
 *    - ID_START_ARRAY / ID_START_OBJECT / ID_FIELD_NAME / ID_END_ARRAY / ID_END_OBJECT / scalar tokens
 *    - Filtering outcomes: INCLUDE_ALL, partial include (filterStartArray/Object), property skipping (includeProperty returning null)
 * 5. Delegated Primitive & Numeric Accessors:
 *    - getText, getTextCharacters, getTextLength, getTextOffset, hasTextCharacters
 *    - getBigIntegerValue, getBigDecimalValue, getBooleanValue, getByteValue, getShortValue
 *    - getIntValue, getLongValue, getFloatValue, getDoubleValue, getNumberType, getNumberValue
 *    - getValueAsInt, getValueAsLong, getValueAsDouble, getValueAsBoolean, getValueAsString
 * 6. Defect-Targeted Ground Truth Assertions:
 *    - Defects4J JacksonCore issue: `_matchCount` is never updated during token filtering
 *      (e.g., testSingleMatchFilteringWithPath, testMultipleMatchFilteringWithPath expecting match count >= 1 but returning 0).
 *    - Disallow multiple matches flag with scalar values in arrays/objects.
 */

package com.fasterxml.jackson.core.filter;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.Base64Variants;

public class FilteringParserDelegateGeminiTest {

    private final JsonFactory JSON_F = new JsonFactory();

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIncludeAllPassThroughObject() throws IOException {
        String json = "{\"a\":1,\"b\":[true,false],\"c\":{\"nested\":\"text\"}}";
        JsonParser rawParser = JSON_F.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                rawParser, TokenFilter.INCLUDE_ALL, true, true);

        assertSame(TokenFilter.INCLUDE_ALL, filterParser.getFilter());
        assertNull(filterParser.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, filterParser.getCurrentTokenId());
        assertEquals(JsonTokenId.ID_NO_TOKEN, filterParser.currentTokenId());
        assertFalse(filterParser.hasCurrentToken());
        assertFalse(filterParser.isExpectedStartArrayToken());
        assertFalse(filterParser.isExpectedStartObjectToken());

        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        assertTrue(filterParser.hasCurrentToken());
        assertTrue(filterParser.hasToken(JsonToken.START_OBJECT));
        assertTrue(filterParser.hasTokenId(JsonTokenId.ID_START_OBJECT));
        assertTrue(filterParser.isExpectedStartObjectToken());
        assertFalse(filterParser.isExpectedStartArrayToken());

        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("a", filterParser.getCurrentName());
        assertEquals("a", filterParser.getText());

        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        assertEquals(1, filterParser.getIntValue());
        assertEquals(1L, filterParser.getLongValue());
        assertEquals((short) 1, filterParser.getShortValue());
        assertEquals((byte) 1, filterParser.getByteValue());
        assertEquals(1.0, filterParser.getDoubleValue(), 0.0001);
        assertEquals(1.0f, filterParser.getFloatValue(), 0.0001f);
        assertEquals(BigInteger.valueOf(1), filterParser.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(1), filterParser.getDecimalValue());
        assertEquals(JsonParser.NumberType.INT, filterParser.getNumberType());
        assertEquals(Integer.valueOf(1), filterParser.getNumberValue());

        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("b", filterParser.getCurrentName());

        assertEquals(JsonToken.START_ARRAY, filterParser.nextToken());
        assertTrue(filterParser.isExpectedStartArrayToken());

        assertEquals(JsonToken.VALUE_TRUE, filterParser.nextToken());
        assertTrue(filterParser.getBooleanValue());
        assertTrue(filterParser.getValueAsBoolean());
        assertEquals(1, filterParser.getValueAsInt());
        assertEquals(1L, filterParser.getValueAsLong());
        assertEquals(1.0, filterParser.getValueAsDouble(), 0.0001);
        assertEquals("true", filterParser.getValueAsString());

        assertEquals(JsonToken.VALUE_FALSE, filterParser.nextToken());
        assertFalse(filterParser.getBooleanValue());

        assertEquals(JsonToken.END_ARRAY, filterParser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("c", filterParser.getCurrentName());

        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("nested", filterParser.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, filterParser.nextToken());
        assertEquals("text", filterParser.getText());
        assertTrue(filterParser.hasTextCharacters());
        assertNotNull(filterParser.getTextCharacters());
        assertEquals(4, filterParser.getTextLength());
        assertEquals(0, filterParser.getTextOffset());

        assertEquals(JsonToken.END_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.END_OBJECT, filterParser.nextToken());
        assertNull(filterParser.nextToken());
        filterParser.close();
    }

    @Test(timeout = 4000)
    public void testTokenClearingAndStateOverrides() throws IOException {
        String json = "{\"x\":42}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertNull(parser.getLastClearedToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.currentToken());
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());

        parser.clearCurrentToken();
        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, parser.getLastClearedToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.getCurrentTokenId());
        assertTrue(parser.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(parser.hasToken(JsonToken.START_OBJECT));

        // Multiple clears in succession
        parser.clearCurrentToken();
        assertEquals(JsonToken.START_OBJECT, parser.getLastClearedToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("x", parser.getCurrentName());

        parser.close();
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnObjectAndArray() throws IOException {
        String json = "{\"obj\":{\"k1\":\"v1\",\"k2\":2},\"arr\":[1,2,3],\"final\":\"val\"}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("obj", parser.getCurrentName());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertSame(parser, parser.skipChildren());
        // After skipping "obj", current token must be END_OBJECT of "obj"
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("arr", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertSame(parser, parser.skipChildren());
        assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("final", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("val", parser.getText());

        // Calling skipChildren on a scalar does nothing and returns this
        assertSame(parser, parser.skipChildren());
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNextValueMethod() throws IOException {
        String json = "{\"a\":10,\"b\":true}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, false, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextValue());
        // Next value after START_OBJECT should advance past FIELD_NAME "a" to VALUE_NUMBER_INT
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextValue());
        assertEquals(10, parser.getIntValue());
        // Next value should advance past FIELD_NAME "b" to VALUE_TRUE
        assertEquals(JsonToken.VALUE_TRUE, parser.nextValue());
        assertTrue(parser.getBooleanValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextValue());
        assertNull(parser.nextValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testContextAndNameNavigation() throws IOException {
        String json = "{\"outer\":{\"inner\":123}}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertNotNull(parser.getParsingContext());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.getCurrentName()); // Root object parent is null

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("outer", parser.getCurrentName());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("outer", parser.getCurrentName()); // START_OBJECT returns parent context name

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("inner", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("inner", parser.getCurrentName());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFilteringEmptyObjectAndArray() throws IOException {
        String json = "{\"emptyObj\":{},\"emptyArr\":[]}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("emptyObj", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("emptyArr", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSelectiveFieldFilteringWithPath() throws IOException {
        String json = "{\"skip1\":1,\"keep\":{\"subKeep\":99},\"skip2\":[1,2]}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("keep".equals(name)) {
                    return this;
                }
                if ("subKeep".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, true, true);

        // Path inclusion must construct START_OBJECT -> keep -> START_OBJECT -> subKeep -> 99 -> END_OBJECT -> END_OBJECT
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("keep", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("subKeep", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(99, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSelectiveFieldFilteringWithoutPath() throws IOException {
        String json = "{\"skip\":100,\"target\":\"winner\"}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return "target".equals(name) ? TokenFilter.INCLUDE_ALL : null;
            }
        };

        // includePath = false
        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, false, true);

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("target", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("winner", parser.getText());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testFilteringExcludesEverything() throws IOException {
        String json = "{\"a\":1,\"b\":[1,2,3],\"c\":{\"d\":4}}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return null;
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, true, true);
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDelegatedValueConversionsWithDefaults() throws IOException {
        String json = "{\"val\":null}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        assertEquals(42, parser.getValueAsInt(42));
        assertEquals(42000000000L, parser.getValueAsLong(42000000000L));
        assertEquals(3.1415, parser.getValueAsDouble(3.1415), 0.00001);
        assertTrue(parser.getValueAsBoolean(true));
        assertEquals("defVal", parser.getValueAsString("defVal"));

        assertNull(parser.getEmbeddedObject());
        assertNotNull(parser.getCurrentLocation());
        assertNotNull(parser.getTokenLocation());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertEquals(0, parser.readBinaryValue(Base64Variants.MIME, baos));
        assertNull(parser.getBinaryValue(Base64Variants.MIME));

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // Ground truth: Defects4J JacksonCore FilteringParserDelegate match count
    // and multiple match tracking failures.
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingleMatchFilteringCountWithPath() throws IOException {
        // Matches Defects4J failure:
        // BasicParserFilteringTest::testSingleMatchFilteringWithPath
        // -> expected:<1> but was:<0>
        String json = "{\"a\":123,\"b\":456}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return "b".equals(name) ? TokenFilter.INCLUDE_ALL : null;
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, true, false);

        while (parser.nextToken() != null) {
            // consume all filtered tokens
        }

        // Defect check: getMatchCount() must report exactly 1 matched sub-structure/token
        assertEquals(1, parser.getMatchCount());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testMultipleMatchCountFiltering() throws IOException {
        // Matches Defects4J failure:
        // BasicParserFilteringTest::testAllowMultipleMatchesWithPath1
        // -> expected:<3> but was:<0>
        String json = "{\"item1\":1,\"item2\":2,\"skip\":3,\"item3\":4}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return name.startsWith("item") ? TokenFilter.INCLUDE_ALL : null;
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, true, true);

        while (parser.nextToken() != null) {
            // consume
        }

        // Defect check: 3 items matched
        assertEquals(3, parser.getMatchCount());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNotAllowMultipleMatchesWithoutPathScalar() throws IOException {
        // Matches Defects4J failure:
        // BasicParserFilteringTest::testNotAllowMultipleMatchesWithoutPath2
        // -> expected:<2[]> but was:<2[ 4]>
        String json = "[2, 4, 6]";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return (p.getIntValue() % 2) == 0;
            }
        };

        // includePath = false, allowMultipleMatches = false
        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, false, false);

        JsonToken t = parser.nextToken();
        assertNotNull("Should read first matched scalar", t);
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
        assertEquals(2, parser.getIntValue());

        // Because allowMultipleMatches = false and includePath = false, no subsequent tokens should be emitted
        JsonToken t2 = parser.nextToken();
        assertNull("Subsequent matches should not be emitted when allowMultipleMatches=false", t2);
        parser.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testOverrideCurrentNameThrowsUnsupportedOperationException() throws IOException {
        String json = "{\"field\":1}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME

        try {
            parser.overrideCurrentName("newName");
            fail("Expected UnsupportedOperationException on overrideCurrentName");
        } catch (UnsupportedOperationException expected) {
            assertEquals("Can not currently override name during filtering read", expected.getMessage());
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testArrayFilteringAndSkipArrayChildren() throws IOException {
        String json = "[[1, 2], [3, 4], [5, 6]]";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            private int arrayIndex = 0;
            @Override
            public TokenFilter filterStartArray() {
                arrayIndex++;
                if (arrayIndex == 2) { // only include second child array: [3, 4]
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, false, true);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(4, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDelegateWrappingAndContextIntegrity() throws IOException {
        String json = "{\"status\":\"ok\"}";
        JsonParser rawParser = JSON_F.createParser(json);
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate parser = new FilteringParserDelegate(rawParser, filter, false, false);

        assertSame(rawParser, parser.getDelegate());
        assertSame(filter, parser.getFilter());

        // Context check before token consumption
        JsonStreamContext ctxt = parser.getParsingContext();
        assertNotNull(ctxt);
        assertTrue(ctxt.inRoot());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("status", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("ok", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());

        parser.close();
        assertTrue(rawParser.isClosed());
    }
}