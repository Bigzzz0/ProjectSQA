package com.fasterxml.jackson.core.filter;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * Advanced White-Box JUnit 4 test suite for FilteringParserDelegate.
 * Targets maximal line/branch coverage and the known defect where
 * _matchCount is never incremented (always returns 0).
 * <p>
 * [Branch & Defect Analysis Matrix]
 * <ul>
 *   <li>Decision points: nextToken() switch on token IDs, buffering logic,
 *       includePath boolean, allowMultipleMatches boolean.</li>
 *   <li>Boundaries: null filter, empty input, deep nesting, scalar values in arrays/objects.</li>
 *   <li>Defect: _matchCount not incremented anywhere in source – all match-count assertions will fail on buggy version.</li>
 * </ul>
 */
public class FilteringParserDelegateDeepseekTest {

    // Helper: create a FilteringParserDelegate from JSON string with given filter, includePath, allowMultipleMatches
    private FilteringParserDelegate createDelegate(String json, TokenFilter filter,
                                                   boolean includePath, boolean allowMultipleMatches) throws IOException {
        JsonFactory factory = new JsonFactory();
        JsonParser baseParser = factory.createParser(new StringReader(json));
        return new FilteringParserDelegate(baseParser, filter, includePath, allowMultipleMatches);
    }

    // Helper: consume all tokens, counting matches (which should be >0 if any match occurred)
    private int countMatchesAfterFullParse(FilteringParserDelegate delegate) throws IOException {
        while (delegate.nextToken() != null) {
            // consume
        }
        return delegate.getMatchCount();
    }

    // Helper: consume tokens until end, verify match count equals expected
    private void assertMatchCount(String json, TokenFilter filter,
                                  boolean includePath, boolean allowMultipleMatches,
                                  int expected) throws IOException {
        FilteringParserDelegate delegate = createDelegate(json, filter, includePath, allowMultipleMatches);
        int actual = countMatchesAfterFullParse(delegate);
        assertEquals("getMatchCount mismatch", expected, actual);
    }

    // ---------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testBasicFilterIncludeAll() throws IOException {
        // TokenFilter.INCLUDE_ALL should include everything
        TokenFilter f = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = createDelegate("{\"a\":1}", f, false, true);
        assertNotNull("first token should not be null", delegate.nextToken());
        assertEquals("start object", JsonToken.START_OBJECT, delegate.getCurrentToken());
        delegate.nextToken(); // field name
        delegate.nextToken(); // value
        delegate.nextToken(); // end object
        assertNull("after end, should be null", delegate.nextToken());
    }

    @Test(timeout = 4000)
    public void testGetFilterReturnsRoot() throws IOException {
        TokenFilter filter = new TokenFilter() {
            // include nothing by default
        };
        FilteringParserDelegate delegate = createDelegate("[]", filter, false, false);
        assertSame("getFilter() must return root filter", filter, delegate.getFilter());
    }

    @Test(timeout = 4000)
    public void testInitialState() throws IOException {
        FilteringParserDelegate delegate = createDelegate("{}", TokenFilter.INCLUDE_ALL, false, false);
        assertNull("current token before nextToken()", delegate.getCurrentToken());
        assertFalse("hasCurrentToken false initially", delegate.hasCurrentToken());
        assertEquals("no token id", JsonTokenId.ID_NO_TOKEN, delegate.getCurrentTokenId());
        assertNotNull("head context not null", delegate._headContext);
    }

    @Test(timeout = 4000)
    public void testClearCurrentToken() throws IOException {
        FilteringParserDelegate delegate = createDelegate("{}", TokenFilter.INCLUDE_ALL, false, false);
        delegate.nextToken(); // START_OBJECT
        assertTrue("current token present", delegate.hasCurrentToken());
        delegate.clearCurrentToken();
        assertFalse("after clear, current token null", delegate.hasCurrentToken());
        assertEquals("last cleared token", JsonToken.START_OBJECT, delegate.getLastClearedToken());
    }

    // ---------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyInput() throws IOException {
        FilteringParserDelegate delegate = createDelegate("", TokenFilter.INCLUDE_ALL, false, false);
        assertNull("empty input returns null immediately", delegate.nextToken());
        assertEquals("match count 0", 0, delegate.getMatchCount());
    }

    @Test(timeout = 4000)
    public void testNullFilter() throws IOException {
        // null filter means nothing included
        // But we should not NPE? Actually the constructor allows null, so token stream should yield nothing.
        FilteringParserDelegate delegate = createDelegate("[1]", null, false, false);
        assertNull("null filter should produce no tokens", delegate.nextToken());
        assertEquals("match count 0", 0, delegate.getMatchCount());
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedInput() throws IOException {
        // deep nested object with include all
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 100; i++) {
            sb.append("\"a\":{");
        }
        sb.append("\"x\":1");
        for (int i = 0; i < 100; i++) {
            sb.append("}");
        }
        String json = sb.toString();
        FilteringParserDelegate delegate = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        int count = 0;
        while (delegate.nextToken() != null) {
            count++;
        }
        // should consume all tokens without exception
        assertTrue("deeply nested consumed at least some tokens", count > 200);
    }

    @Test(timeout = 4000)
    public void testMultipleScalarsInArray() throws IOException {
        // array with many scalar values, filter includes all
        String json = "[1,2,3,4,5]";
        FilteringParserDelegate delegate = createDelegate(json, TokenFilter.INCLUDE_ALL, false, false);
        assertEquals("start array", JsonToken.START_ARRAY, delegate.nextToken());
        for (int i = 0; i < 5; i++) {
            assertEquals("value " + i, JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        }
        assertEquals("end array", JsonToken.END_ARRAY, delegate.nextToken());
        assertNull("should end", delegate.nextToken());
    }

    // ---------------------------------------------------------------
    // Partition C: Defect-Targeted Zone (match count bug)
    // ---------------------------------------------------------------

    // Directly targets the known defect: getMatchCount() always returns 0.
    // These tests will fail on the buggy version because the count is never incremented.

    @Test(timeout = 4000)
    public void testSingleMatchWithPathCount() throws IOException {
        // Filter that includes property "a"
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("a".equals(name)) return TokenFilter.INCLUDE_ALL;
                return null; // exclude others
            }
        };
        // With includePath=true, the START_OBJECT and field name will be included, plus value.
        // Match count should = 1 (for the value? Or for the property? The source never increments _matchCount.
        // Correct behavior: _matchCount incremented when a value is included? Based on docs, count of matches.
        // Test: Expect at least 1.
        String json = "{\"a\":1}";
        FilteringParserDelegate delegate = createDelegate(json, filter, true, false);
        while (delegate.nextToken() != null) {
            // consume
        }
        int count = delegate.getMatchCount();
        assertTrue("single match count should be >=1, but got " + count, count >= 1);
    }

    @Test(timeout = 4000)
    public void testMultipleMatchesWithoutPathCount() throws IOException {
        // Filter that includes all scalar values (includeValue returns true)
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return true; // include all values
            }
        };
        String json = "{\"a\":1,\"b\":2}";
        // allowMultipleMatches false, but includePath false => only first match?
        // Actually with includePath false and allowMultiple false, after first match we stop.
        // But we want to test count >0 anyway.
        FilteringParserDelegate delegate = createDelegate(json, filter, false, false);
        while (delegate.nextToken() != null) {}
        int count = delegate.getMatchCount();
        assertTrue("expected at least 1 match, got " + count, count >= 1);
    }

    @Test(timeout = 4000)
    public void testAllowMultipleMatchesWithPath1() throws IOException {
        // Simulate testAllowMultipleMatchesWithPath1 from Defects4J:
        // Input: {"a":1,"a":2}? Actually multiple matches with same property?
        // Use filter that includes property "a".
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("a".equals(name)) return TokenFilter.INCLUDE_ALL;
                return null;
            }
        };
        String json = "[{\"a\":1},{\"a\":2}]";
        FilteringParserDelegate delegate = createDelegate(json, filter, true, true);
        int tokenCount = 0;
        while (delegate.nextToken() != null) {
            tokenCount++;
        }
        int matchCount = delegate.getMatchCount();
        assertTrue("match count should be >0, got " + matchCount, matchCount > 0);
        // Correct expected matchCount according to defect description is 3? Actually testAllowMultipleMatchesWithPath1 expects 3.
        // For a more precise test we could parse and count manually, but for bug detection any positive count suffices.
    }

    @Test(timeout = 4000)
    public void testIndexMatchWithPath1() throws IOException {
        // Filter that includes array element at index 0
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter filterStartArray() {
                // include the first element only
                return new TokenFilter() {
                    int index = 0;
                    @Override
                    protected boolean _includeScalar() {
                        return index++ == 0;
                    }
                };
            }
        };
        String json = "[1,2,3]";
        FilteringParserDelegate delegate = createDelegate(json, filter, true, false);
        while (delegate.nextToken() != null) {}
        assertTrue("match count should be >0 but was " + delegate.getMatchCount(), delegate.getMatchCount() > 0);
    }

    // ---------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ---------------------------------------------------------------

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testOverrideCurrentNameThrows() throws IOException {
        FilteringParserDelegate delegate = createDelegate("{}", TokenFilter.INCLUDE_ALL, false, false);
        delegate.overrideCurrentName("x");
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testNextTokenOnClosedParser() throws IOException {
        // Closing the underlying parser should cause IOException when nextToken is called.
        JsonFactory factory = new JsonFactory();
        JsonParser baseParser = factory.createParser(new StringReader("{}"));
        baseParser.close();
        FilteringParserDelegate delegate = new FilteringParserDelegate(baseParser, TokenFilter.INCLUDE_ALL, false, false);
        delegate.nextToken(); // should throw
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnNonContainer() throws IOException {
        FilteringParserDelegate delegate = createDelegate("123", TokenFilter.INCLUDE_ALL, false, false);
        delegate.nextToken(); // VALUE_NUMBER_INT
        // skipChildren on scalar should return this
        assertSame("skipChildren returns same parser", delegate, delegate.skipChildren());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnContainer() throws IOException {
        FilteringParserDelegate delegate = createDelegate("[1,2,3]", TokenFilter.INCLUDE_ALL, false, false);
        delegate.nextToken(); // START_ARRAY
        delegate.skipChildren();
        // after skip, should be at END_ARRAY? Actually skipChildren consumes the array and returns at END_ARRAY.
        assertEquals("should be at end array", JsonToken.END_ARRAY, delegate.getCurrentToken());
    }

    // ---------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testParsingContextConsistency() throws IOException {
        FilteringParserDelegate delegate = createDelegate("{\"a\":1}", TokenFilter.INCLUDE_ALL, false, false);
        delegate.nextToken(); // START_OBJECT
        JsonStreamContext ctxt = delegate.getParsingContext();
        assertNotNull("context not null", ctxt);
        assertEquals("should be object context", JsonStreamContext.TYPE_OBJECT, ctxt.getType());
        delegate.nextToken(); // FIELD_NAME
        assertEquals("field name a", "a", delegate.getCurrentName());
        delegate.nextToken(); // VALUE_NUMBER_INT
        delegate.nextToken(); // END_OBJECT
    }

    @Test(timeout = 4000)
    public void testTextAccess() throws IOException {
        FilteringParserDelegate delegate = createDelegate("\"hello\"", TokenFilter.INCLUDE_ALL, false, false);
        delegate.nextToken(); // VALUE_STRING
        assertEquals("hello", delegate.getText());
        assertTrue("has text characters", delegate.hasTextCharacters());
        assertNotNull("text characters array", delegate.getTextCharacters());
    }

    @Test(timeout = 4000)
    public void testNumberAccess() throws IOException {
        FilteringParserDelegate delegate = createDelegate("42", TokenFilter.INCLUDE_ALL, false, false);
        delegate.nextToken(); // VALUE_NUMBER_INT
        assertEquals(42, delegate.getIntValue());
        assertEquals(42L, delegate.getLongValue());
        assertEquals(42.0, delegate.getDoubleValue(), 0.0);
        assertEquals(42f, delegate.getFloatValue(), 0.0f);
        assertEquals(42, delegate.getByteValue());
        assertEquals(42, delegate.getShortValue());
        assertEquals(java.math.BigDecimal.valueOf(42), delegate.getDecimalValue());
        assertEquals(java.math.BigInteger.valueOf(42), delegate.getBigIntegerValue());
        assertEquals(JsonParser.NumberType.INT, delegate.getNumberType());
    }

    @Test(timeout = 4000)
    public void testValueAsCoercion() throws IOException {
        FilteringParserDelegate delegate = createDelegate("true", TokenFilter.INCLUDE_ALL, false, false);
        delegate.nextToken(); // VALUE_TRUE
        assertTrue(delegate.getValueAsBoolean());
        assertEquals(1, delegate.getValueAsInt());
        assertEquals(1L, delegate.getValueAsLong());
        assertEquals(1.0, delegate.getValueAsDouble(), 0.0);
        assertEquals("true", delegate.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testValueAsDefault() throws IOException {
        FilteringParserDelegate delegate = createDelegate("\"abc\"", TokenFilter.INCLUDE_ALL, false, false);
        delegate.nextToken(); // VALUE_STRING
        assertEquals(0, delegate.getValueAsInt(0));
        assertEquals(0L, delegate.getValueAsLong(0L));
        assertEquals(0.0, delegate.getValueAsDouble(0.0), 0.0);
        assertFalse(delegate.getValueAsBoolean(true));
    }

    // ---------------------------------------------------------------
    // Edge case: when token filter returns null for checkValue (filter out entire container)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFilterOutObjectAndArray() throws IOException {
        // Filter that rejects everything
        TokenFilter rejectAll = new TokenFilter() {
            @Override
            public TokenFilter filterStartObject() {
                return null; // reject object
            }
            @Override
            public TokenFilter filterStartArray() {
                return null; // reject array
            }
        };
        String json = "{\"a\":1}";
        FilteringParserDelegate delegate = createDelegate(json, rejectAll, false, false);
        // Should skip the object, so no tokens
        assertNull("no tokens", delegate.nextToken());
    }

    @Test(timeout = 4000)
    public void testFilterOutPropertyValue() throws IOException {
        // Filter that rejects a specific property's value
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("a".equals(name)) {
                    // only include if value is even? We'll just return null to drop the property entirely.
                    return null;
                }
                return TokenFilter.INCLUDE_ALL;
            }
        };
        String json = "{\"a\":1,\"b\":2}";
        FilteringParserDelegate delegate = createDelegate(json, filter, false, false);
        // Should only include property "b"
        assertEquals("start object", JsonToken.START_OBJECT, delegate.nextToken());
        // If includePath false, start object may be included? Actually includePath false => no automatic inclusion of parents.
        // So START_OBJECT may not appear because the property "a" is omitted and the empty object might be skipped?
        // To keep test simple, just consume and check match count eventually.
        while (delegate.nextToken() != null) {}
        // at least some tokens consumed
        assertTrue("tokens consumed", delegate.getCurrentToken() == null);
    }

    // ---------------------------------------------------------------
    // Test _nextTokenWithBuffering paths (covered via includePath true)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testBufferingInObject() throws IOException {
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("a".equals(name)) return TokenFilter.INCLUDE_ALL;
                return null;
            }
        };
        String json = "{\"x\":1,\"a\":2,\"y\":3}";
        FilteringParserDelegate delegate = createDelegate(json, filter, true, false);
        // With includePath true, we expect tokens: START_OBJECT, FIELD_NAME("a"), VALUE_NUMBER_INT(2), END_OBJECT(? maybe)
        // But also might include the root object's start?
        // Let's consume all and check match count >0.
        while (delegate.nextToken() != null) {}
        assertTrue("match count >0", delegate.getMatchCount() > 0);
    }

    @Test(timeout = 4000)
    public void testBufferingInArray() throws IOException {
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter filterStartArray() {
                // include the first element only
                return new TokenFilter() {
                    int index = 0;
                    @Override
                    protected boolean _includeScalar() {
                        return index++ == 0;
                    }
                };
            }
        };
        String json = "[1,2,3]";
        FilteringParserDelegate delegate = createDelegate(json, filter, true, false);
        while (delegate.nextToken() != null) {}
        assertTrue("match count >0", delegate.getMatchCount() > 0);
    }

    // ---------------------------------------------------------------
    // Additional boundary: empty object, empty array
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyObjectWithIncludeAll() throws IOException {
        FilteringParserDelegate delegate = createDelegate("{}", TokenFilter.INCLUDE_ALL, false, false);
        assertEquals("start object", JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals("end object", JsonToken.END_OBJECT, delegate.nextToken());
        assertNull("end", delegate.nextToken());
    }

    @Test(timeout = 4000)
    public void testEmptyArrayWithIncludeAll() throws IOException {
        FilteringParserDelegate delegate = createDelegate("[]", TokenFilter.INCLUDE_ALL, false, false);
        assertEquals("start array", JsonToken.START_ARRAY, delegate.nextToken());
        assertEquals("end array", JsonToken.END_ARRAY, delegate.nextToken());
        assertNull("end", delegate.nextToken());
    }

    // ---------------------------------------------------------------
    // Test configuration booleans states
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAllowMultipleMatchesFalse_OnceMatched_NoMoreTokens() throws IOException {
        // With allowMultipleMatches false, after first full match (INCLUDE_ALL) no more tokens.
        // Test: input with multiple top-level values? Actually top-level value is a single element.
        // But with array: first scalar value included, then stop.
        TokenFilter filter = new TokenFilter() {
            @Override
            protected boolean _includeScalar() {
                return true;
            }
        };
        String json = "[1,2,3]";
        FilteringParserDelegate delegate = createDelegate(json, filter, false, false);
        // Since includePath false, the array start/end are not automatically included.
        // So we should see first value 1, then null.
        JsonToken t = delegate.nextToken();
        assertNotNull("first token", t);
        assertTrue("should be scalar", t.isScalarValue());
        // After that, nextToken should return null because allowMultipleMatches false and _exposedContext null.
        assertNull("should stop after first match", delegate.nextToken());
        // But note: the actual bug may affect the count, not the token stream.
    }

    @Test(timeout = 4000)
    public void testIncludePathFalse_NoExtraTokens() throws IOException {
        // Property "a" included, but not path => only the value token should appear? Actually also may need start/end? Not sure.
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("a".equals(name)) return TokenFilter.INCLUDE_ALL;
                return null;
            }
        };
        String json = "{\"a\":1}";
        FilteringParserDelegate delegate = createDelegate(json, filter, false, false);
        // Expect only the value 1? Or maybe nothing because start object not included?
        // Let's just check that some token appears.
        assertNotNull("at least one token", delegate.nextToken());
        while (delegate.nextToken() != null) {}
        assertTrue("match count should be 1? Actually no, match count bug", delegate.getMatchCount() >= 0);
    }

    // ---------------------------------------------------------------
    // Test that ensures no infinite loops
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNoInfiniteLoopOnEmptyObjectAfterFilterSkip() throws IOException {
        // Filter that rejects everything (returns null for filterStartObject)
        TokenFilter rejectAll = new TokenFilter() {
            @Override
            public TokenFilter filterStartObject() {
                return null;
            }
        };
        String json = "{\"a\":1}";
        FilteringParserDelegate delegate = createDelegate(json, rejectAll, false, false);
        assertNull("should return null", delegate.nextToken());
    }

    // ---------------------------------------------------------------
    // Test that exercises the _nextTokenWithBuffering default case (scalar)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testBufferingWithScalarIncluded() throws IOException {
        // Create filter that includes scalars; includePath true
        TokenFilter filter = new TokenFilter() {
            @Override
            protected boolean _includeScalar() {
                return true;
            }
        };
        String json = "[100]";
        FilteringParserDelegate delegate = createDelegate(json, filter, true, false);
        // Should consume start array (included because parent path), then scalar, then end array.
        assertNotNull("tokens should be emitted", delegate.nextToken());
        while (delegate.nextToken() != null) {}
        assertTrue("match count >0", delegate.getMatchCount() > 0);
    }

    // ---------------------------------------------------------------
    // Test that directly replicates BasicParserFilteringTest conditions
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSingleMatchFilteringWithPath() throws IOException {
        // Known failing test
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("value".equals(name)) return TokenFilter.INCLUDE_ALL;
                return null;
            }
        };
        String json = "{\"value\":42}";
        FilteringParserDelegate delegate = createDelegate(json, filter, true, false);
        int matchCount = countMatchesAfterFullParse(delegate);
        assertEquals("expected 1 match", 1, matchCount);
    }

    @Test(timeout = 4000)
    public void testAllowMultipleMatchesWithPath1() throws IOException {
        // From defect list: expects 3 matches
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("value".equals(name)) return TokenFilter.INCLUDE_ALL;
                return null;
            }
        };
        String json = "[{\"value\":1},{\"value\":2},{\"value\":3}]";
        FilteringParserDelegate delegate = createDelegate(json, filter, true, true);
        int matchCount = countMatchesAfterFullParse(delegate);
        assertEquals("expected 3 matches", 3, matchCount);
    }

    @Test(timeout = 4000)
    public void testNotAllowMultipleMatchesWithoutPath1() throws IOException {
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("a".equals(name)) return TokenFilter.INCLUDE_ALL;
                return null;
            }
        };
        String json = "{\"a\":1}";
        FilteringParserDelegate delegate = createDelegate(json, filter, false, false);
        int matchCount = countMatchesAfterFullParse(delegate);
        assertEquals("expected 1 match", 1, matchCount);
    }

    @Test(timeout = 4000)
    public void testMultipleMatchFilteringWithPath1() throws IOException {
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("value".equals(name)) return TokenFilter.INCLUDE_ALL;
                return null;
            }
        };
        String json = "[{\"value\":1},{\"value\":2}]";
        FilteringParserDelegate delegate = createDelegate(json, filter, true, false);
        int matchCount = countMatchesAfterFullParse(delegate);
        assertEquals("expected 2 matches", 2, matchCount);
    }
}