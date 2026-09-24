package com.fasterxml.jackson.core.filter;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Defect Zone:
 * - BasicParserFilteringTest::testNotAllowMultipleMatches:
 *   When allowMultipleMatches=false, FilteringParserDelegate fails to terminate after first full match,
 *   erroneously returning subsequent matches (e.g. expected:<3[]> but was:<3[ 4]>).
 *
 * Core Functional Branches & Logic:
 * - nextToken() & _nextToken2():
 *   - Buffered context traversal (_exposedContext != null, inArray() vs root/object, broken chain check)
 *   - delegate.nextToken() == null (EOF transition)
 *   - ID_START_ARRAY / ID_START_OBJECT:
 *     - f == TokenFilter.INCLUDE_ALL
 *     - f == null (skipChildren branch)
 *     - checkValue(f) returns null vs INCLUDE_ALL vs specialized filter
 *     - _includePath == true (enters _nextTokenWithBuffering) vs false
 *   - ID_END_ARRAY / ID_END_OBJECT:
 *     - returnEnd condition (isStartHandled)
 *     - filterFinishArray invocation when filter != INCLUDE_ALL
 *   - ID_FIELD_NAME:
 *     - setFieldName returning INCLUDE_ALL
 *     - includeProperty returning null (skips child) vs INCLUDE_ALL vs filter
 *     - _includeImmediateParent flag and non-handled start context
 *   - Default (scalars):
 *     - INCLUDE_ALL vs custom includeValue(delegate) returning true vs false
 * - _nextTokenWithBuffering():
 *   - Buffered resolution for deep path inclusion
 *   - Reaching end of buffer and unwinding context
 * - skipChildren():
 *   - Called on non-structural token (returns immediate this)
 *   - Nested start/end balance tracking
 * - nextValue():
 *   - Field name skipping to value
 * - Accessors & Delegations:
 *   - getCurrentToken(), getCurrentTokenId(), hasToken(), hasTokenId(), hasCurrentToken()
 *   - getCurrentName() with START_OBJECT/ARRAY (parent context) vs scalar/field
 *   - clearCurrentToken(), getLastClearedToken()
 *   - overrideCurrentName() throws UnsupportedOperationException
 *   - Scalar getters: int, long, double, float, boolean, byte, short, BigInteger, BigDecimal, NumberType
 *   - getValueAs* coercions, getText*, binary operations, locations
 * ---------------------------------------------------------------------------------------------------
 */
public class FilteringParserDelegateGeminiTest {

    private final JsonFactory JSON_F = new JsonFactory();

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the documented Defects4J failure:
     * BasicParserFilteringTest::testNotAllowMultipleMatches
     * ComparisonFailure: expected:<3[]> but was:<3[ 4]>
     *
     * When allowMultipleMatches is false and includePath is false, once the first
     * matching scalar is yielded, parsing should not return subsequent matches.
     */
    @Test(timeout = 4000)
    public void testNotAllowMultipleMatchesScalarDefect() throws IOException {
        String json = "{ \"a\": 1, \"b\": 2, \"c\": 3, \"d\": 4 }";
        JsonParser rawParser = JSON_F.createParser(json);

        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return p.getIntValue() >= 3;
            }
        };

        FilteringParserDelegate filteringParser = new FilteringParserDelegate(
                rawParser, filter, false, false
        );

        StringBuilder sb = new StringBuilder();
        while (filteringParser.nextToken() != null) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(filteringParser.getText());
        }

        assertEquals("3", sb.toString());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIncludeAllFilterPassThrough() throws IOException {
        String json = "{\"x\":[1,{\"y\":true}],\"z\":null}";
        JsonParser p = new FilteringParserDelegate(
                JSON_F.createParser(json),
                TokenFilter.INCLUDE_ALL,
                true,
                true
        );

        assertSame(TokenFilter.INCLUDE_ALL, ((FilteringParserDelegate) p).getFilter());
        assertEquals(0, ((FilteringParserDelegate) p).getMatchCount());

        assertNull(p.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, p.getCurrentTokenId());
        assertFalse(p.hasCurrentToken());
        assertTrue(p.hasTokenId(JsonTokenId.ID_NO_TOKEN));

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertTrue(p.isExpectedStartObjectToken());
        assertFalse(p.isExpectedStartArrayToken());
        assertEquals(JsonTokenId.ID_START_OBJECT, p.getCurrentTokenId());
        assertTrue(p.hasToken(JsonToken.START_OBJECT));

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("x", p.getCurrentName());

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertTrue(p.isExpectedStartArrayToken());
        // Parent of start array is the object with field "x"
        assertEquals("x", p.getCurrentName());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals("1", p.getText());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("y", p.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("z", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testFilteringWithIncludePathTrue() throws IOException {
        String json = "{\"a\":1,\"target\":{\"deep\":42},\"b\":2}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("target".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        JsonParser p = new FilteringParserDelegate(
                JSON_F.createParser(json),
                filter,
                true, // includePath
                true
        );

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("target", p.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("deep", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testFilteringWithIncludePathFalse() throws IOException {
        String json = "{\"a\":1,\"nested\":{\"leaf\":99}}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("nested".equals(name)) {
                    return this;
                }
                if ("leaf".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        JsonParser p = new FilteringParserDelegate(
                JSON_F.createParser(json),
                filter,
                false, // do not include path
                true
        );

        // Since path is omitted, leaf field and its value are emitted directly
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("leaf", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(99, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testArrayFilteringAndFinish() throws IOException {
        String json = "[10, 20, 30]";
        final int[] finishArrayCallCount = new int[1];

        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter filterStartArray() {
                return new TokenFilter() {
                    @Override
                    public boolean includeValue(JsonParser p) throws IOException {
                        return p.getIntValue() == 20;
                    }

                    @Override
                    public void filterFinishArray() {
                        finishArrayCallCount[0]++;
                    }
                };
            }
        };

        JsonParser p = new FilteringParserDelegate(
                JSON_F.createParser(json),
                filter,
                true,
                true
        );

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(20, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        assertEquals(1, finishArrayCallCount[0]);
    }

    @Test(timeout = 4000)
    public void testNextValueHandling() throws IOException {
        String json = "{\"k1\":100,\"k2\":200}";
        JsonParser p = new FilteringParserDelegate(
                JSON_F.createParser(json),
                TokenFilter.INCLUDE_ALL,
                true,
                true
        );

        assertEquals(JsonToken.START_OBJECT, p.nextValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextValue());
        assertEquals("k1", p.getCurrentName());
        assertEquals(100, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextValue());
        assertEquals("k2", p.getCurrentName());
        assertEquals(200, p.getIntValue());

        assertEquals(JsonToken.END_OBJECT, p.nextValue());
        assertNull(p.nextValue());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnStructure() throws IOException {
        String json = "{\"skipMe\":[1,2,{\"inner\":3}],\"keepMe\":999}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return TokenFilter.INCLUDE_ALL;
            }
        };

        JsonParser p = new FilteringParserDelegate(
                JSON_F.createParser(json),
                filter,
                true,
                true
        );

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("skipMe", p.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        // Skip the array structure entirely
        assertSame(p, p.skipChildren());
        assertEquals(JsonToken.END_ARRAY, p.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("keepMe", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(999, p.getIntValue());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnScalarNoOp() throws IOException {
        String json = "{\"a\": 1}";
        JsonParser p = new FilteringParserDelegate(
                JSON_F.createParser(json),
                TokenFilter.INCLUDE_ALL,
                true,
                true
        );

        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_NUMBER_INT
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.getCurrentToken());

        // Calling skipChildren on a non-structure token should immediately return this
        assertSame(p, p.skipChildren());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.getCurrentToken());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Delegation Assertions
    // =========================================================================

    @Test(timeout = 4000)
    public void testClearCurrentTokenAndLastClearedToken() throws IOException {
        String json = "[123]";
        FilteringParserDelegate p = new FilteringParserDelegate(
                JSON_F.createParser(json),
                TokenFilter.INCLUDE_ALL,
                true,
                true
        );

        assertNull(p.getLastClearedToken());
        p.clearCurrentToken(); // no-op when _currToken == null
        assertNull(p.getLastClearedToken());

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        p.clearCurrentToken();

        assertNull(p.getCurrentToken());
        assertFalse(p.hasCurrentToken());
        assertEquals(JsonToken.START_ARRAY, p.getLastClearedToken());
    }

    @Test(timeout = 4000)
    public void testAllScalarAndNumericDelegations() throws IOException {
        String json = "{\"b\":true,\"by\":12,\"s\":300,\"l\":9876543210,\"f\":1.5,\"d\":2.75,\"bi\":12345678901234567890,\"bd\":123.456}";
        FilteringParserDelegate p = new FilteringParserDelegate(
                JSON_F.createParser(json),
                TokenFilter.INCLUDE_ALL,
                true,
                true
        );

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());
        assertTrue(p.getValueAsBoolean());
        assertTrue(p.getValueAsBoolean(false));

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        p.nextToken();
        assertEquals((byte) 12, p.getByteValue());
        assertEquals((short) 12, p.getShortValue());
        assertEquals(12, p.getIntValue());
        assertEquals(12, p.getValueAsInt());
        assertEquals(12, p.getValueAsInt(0));

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        p.nextToken();
        assertEquals(300, p.getShortValue());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        p.nextToken();
        assertEquals(9876543210L, p.getLongValue());
        assertEquals(9876543210L, p.getValueAsLong());
        assertEquals(9876543210L, p.getValueAsLong(0L));

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        p.nextToken();
        assertEquals(1.5f, p.getFloatValue(), 0.0001f);

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        p.nextToken();
        assertEquals(2.75, p.getDoubleValue(), 0.0001);
        assertEquals(2.75, p.getValueAsDouble(), 0.0001);
        assertEquals(2.75, p.getValueAsDouble(0.0), 0.0001);

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        p.nextToken();
        assertEquals(new BigInteger("12345678901234567890"), p.getBigIntegerValue());
        assertNotNull(p.getNumberValue());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        p.nextToken();
        assertEquals(new BigDecimal("123.456"), p.getDecimalValue());
        assertEquals("123.456", p.getValueAsString());
        assertEquals("123.456", p.getValueAsString("def"));

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test(timeout = 4000)
    public void testTextAndBinaryDelegation() throws IOException {
        String json = "{\"data\":\"SGVsbG8gV29ybGQ=\"}"; // Base64 for "Hello World"
        FilteringParserDelegate p = new FilteringParserDelegate(
                JSON_F.createParser(json),
                TokenFilter.INCLUDE_ALL,
                true,
                true
        );

        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_STRING

        assertEquals("SGVsbG8gV29ybGQ=", p.getText());
        assertTrue(p.hasTextCharacters());
        assertNotNull(p.getTextCharacters());
        assertEquals(16, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        assertNull(p.getEmbeddedObject());

        byte[] b = p.getBinaryValue(Base64Variants.MIME);
        assertEquals("Hello World", new String(b, "UTF-8"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = p.readBinaryValue(Base64Variants.MIME, baos);
        assertEquals(11, bytesRead);
        assertEquals("Hello World", new String(baos.toByteArray(), "UTF-8"));

        JsonLocation loc = p.getCurrentLocation();
        assertNotNull(loc);
        JsonLocation tokenLoc = p.getTokenLocation();
        assertNotNull(tokenLoc);

        JsonStreamContext context = p.getParsingContext();
        assertNotNull(context);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testOverrideCurrentNameThrowsUnsupportedOperationException() {
        FilteringParserDelegate p = new FilteringParserDelegate(
                null,
                TokenFilter.INCLUDE_ALL,
                false,
                false
        );
        p.overrideCurrentName("anyName");
    }

    @Test(timeout = 4000)
    public void testEmptyJsonContentGivesNullImmediately() throws IOException {
        JsonParser p = new FilteringParserDelegate(
                JSON_F.createParser(""),
                TokenFilter.INCLUDE_ALL,
                true,
                true
        );
        assertNull(p.nextToken());
        assertNull(p.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testFilterPruningEntireTree() throws IOException {
        String json = "{\"a\":[1, 2], \"b\":{\"c\":3}}";
        TokenFilter rejectAllFilter = new TokenFilter() {
            @Override
            public TokenFilter filterStartObject() {
                return null;
            }

            @Override
            public TokenFilter filterStartArray() {
                return null;
            }
        };

        JsonParser p = new FilteringParserDelegate(
                JSON_F.createParser(json),
                rejectAllFilter,
                false,
                false
        );

        assertNull(p.nextToken());
    }
}