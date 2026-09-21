package com.fasterxml.jackson.core.filter;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.JsonParserDelegate;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: FilteringParserDelegate - White-box testing with focus on:
 * 
 * Branch Coverage Targets:
 * 1. nextToken() - _exposedContext != null path (buffered tokens)
 * 2. nextToken() - _exposedContext == null path (reading new tokens)
 * 3. nextToken() - _allowMultipleMatches == false with scalar values
 * 4. nextToken() - ID_START_ARRAY with various filter states
 * 5. nextToken() - ID_START_OBJECT with various filter states
 * 6. nextToken() - ID_END_ARRAY/ID_END_OBJECT with isStartHandled()
 * 7. nextToken() - ID_FIELD_NAME with includePath and includeImmediateParent
 * 8. nextToken() - scalar value handling with includeValue
 * 9. _nextToken2() - all token types in secondary loop
 * 10. _nextTokenWithBuffering() - buffering logic
 * 11. _nextBuffered() - context chain traversal
 * 12. skipChildren() - nesting level counting
 * 13. getCurrentName() - START_OBJECT/START_ARRAY parent context
 * 14. clearCurrentToken() - state management
 * 
 * Defect-Specific Target:
 * - Bug: When _allowMultipleMatches=false and a scalar value is matched,
 *   the nextToken() should return null after the first match, but instead
 *   continues to return additional tokens.
 * - The defect manifests as: expected:<3[]> but was:<3[ 4]>
 * - This occurs in the early return logic at the top of nextToken()
 *   where _allowMultipleMatches check is incomplete for scalar values
 */
public class FilteringParserDelegateDeepseekTest {
    
    // ===========================================================
    // Part A: Core Functional Logic & State Transitions
    // ===========================================================
    
    @Test(timeout = 4000)
    public void testConstructorAndBasicState() throws Exception {
        // Create a simple JSON parser
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":1}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        assertNotNull("Filter should not be null", delegate.getFilter());
        assertEquals("Initial match count should be 0", 0, delegate.getMatchCount());
        assertNull("Current token should be null initially", delegate.getCurrentToken());
        assertFalse("hasCurrentToken should be false initially", delegate.hasCurrentToken());
        assertEquals("getCurrentTokenId should return ID_NO_TOKEN", 
            JsonTokenId.ID_NO_TOKEN, delegate.getCurrentTokenId());
    }
    
    @Test(timeout = 4000)
    public void testNextTokenWithIncludeAll() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":1,\"b\":2}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        assertNotNull("First token should be START_OBJECT", delegate.nextToken());
        assertEquals(JsonToken.START_OBJECT, delegate.getCurrentToken());
        assertTrue("isExpectedStartArrayToken should be false", 
            !delegate.isExpectedStartArrayToken());
        assertTrue("isExpectedStartObjectToken should be true", 
            delegate.isExpectedStartObjectToken());
        
        assertNotNull("Second token should be FIELD_NAME", delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.getCurrentToken());
        
        assertNotNull("Third token should be VALUE_NUMBER_INT", delegate.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.getCurrentToken());
        
        assertNotNull("Fourth token should be FIELD_NAME", delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.getCurrentToken());
        
        assertNotNull("Fifth token should be VALUE_NUMBER_INT", delegate.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.getCurrentToken());
        
        assertNotNull("Sixth token should be END_OBJECT", delegate.nextToken());
        assertEquals(JsonToken.END_OBJECT, delegate.getCurrentToken());
        
        assertNull("Seventh token should be null", delegate.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testGetCurrentName() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":1}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        delegate.nextToken(); // START_OBJECT
        assertNull("Current name should be null for START_OBJECT", 
            delegate.getCurrentName());
        
        delegate.nextToken(); // FIELD_NAME
        assertEquals("Current name should be 'a'", "a", delegate.getCurrentName());
    }
    
    @Test(timeout = 4000)
    public void testClearCurrentToken() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":1}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        delegate.nextToken(); // START_OBJECT
        assertNotNull("Current token should not be null", delegate.getCurrentToken());
        
        delegate.clearCurrentToken();
        assertNull("Current token should be null after clear", delegate.getCurrentToken());
        assertEquals("Last cleared token should be START_OBJECT", 
            JsonToken.START_OBJECT, delegate.getLastClearedToken());
        
        // Clear again when already null
        delegate.clearCurrentToken();
        assertNull("Current token should still be null", delegate.getCurrentToken());
        assertEquals("Last cleared token should still be START_OBJECT", 
            JsonToken.START_OBJECT, delegate.getLastClearedToken());
    }
    
    @Test(timeout = 4000)
    public void testOverrideCurrentNameThrows() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{}";
        JsonParser parser = factory.createParser(json);
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        try {
            delegate.overrideCurrentName("test");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
    
    // ===========================================================
    // Part B: Boundary Value Analysis & Extremes
    // ===========================================================
    
    @Test(timeout = 4000)
    public void testEmptyObject() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testEmptyArray() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "[]";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_ARRAY
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        assertEquals(JsonToken.START_ARRAY, delegate.nextToken());
        assertEquals(JsonToken.END_ARRAY, delegate.nextToken());
        assertNull(delegate.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testNullFilter() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":1}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        // Using null filter should still work but filter everything out
        TokenFilter filter = null;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        // Should return null since nothing is included
        assertNull(delegate.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testNestedStructures() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":{\"b\":[1,2]}}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("a", delegate.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("b", delegate.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, delegate.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(1, delegate.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(2, delegate.getIntValue());
        assertEquals(JsonToken.END_ARRAY, delegate.nextToken());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
    }
    
    // ===========================================================
    // Part C: Defect-Targeted Branch Zone
    // ===========================================================
    
    /**
     * This test directly targets the known defect:
     * When _allowMultipleMatches=false and a scalar value is matched,
     * nextToken() should return null after the first match, but instead
     * continues to return additional tokens.
     * 
     * The defect manifests as: expected:<3[]> but was:<3[ 4]>
     */
    @Test(timeout = 4000)
    public void testNotAllowMultipleMatchesWithScalar() throws Exception {
        JsonFactory factory = new JsonFactory();
        // Array with multiple scalar values
        String json = "[3, 4]";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_ARRAY
        
        // Create a filter that includes all tokens
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, false, false); // _allowMultipleMatches = false
        
        // Should get START_ARRAY
        JsonToken token = delegate.nextToken();
        assertNotNull("Should get START_ARRAY", token);
        assertEquals(JsonToken.START_ARRAY, token);
        
        // Should get first value (3)
        token = delegate.nextToken();
        assertNotNull("Should get first value", token);
        assertEquals(JsonToken.VALUE_NUMBER_INT, token);
        assertEquals(3, delegate.getIntValue());
        
        // BUG: With _allowMultipleMatches=false, after matching the first scalar,
        // nextToken() should return null. But the bug causes it to return 4 as well.
        // The correct behavior is to return null here.
        token = delegate.nextToken();
        
        // According to the defect, the buggy version returns VALUE_NUMBER_INT (4)
        // The correct version should return null
        // We assert the CORRECT behavior (null) to reveal the bug
        assertNull("With _allowMultipleMatches=false, after first scalar match, " +
            "nextToken() should return null. Bug causes it to return 4 instead.",
            token);
    }
    
    @Test(timeout = 4000)
    public void testNotAllowMultipleMatchesWithObject() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":1,\"b\":2}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, false, false); // _allowMultipleMatches = false
        
        // Should get START_OBJECT
        JsonToken token = delegate.nextToken();
        assertNotNull(token);
        assertEquals(JsonToken.START_OBJECT, token);
        
        // Should get FIELD_NAME "a"
        token = delegate.nextToken();
        assertNotNull(token);
        assertEquals(JsonToken.FIELD_NAME, token);
        assertEquals("a", delegate.getCurrentName());
        
        // Should get VALUE_NUMBER_INT 1
        token = delegate.nextToken();
        assertNotNull(token);
        assertEquals(JsonToken.VALUE_NUMBER_INT, token);
        assertEquals(1, delegate.getIntValue());
        
        // With _allowMultipleMatches=false, should return null after first match
        token = delegate.nextToken();
        assertNull("With _allowMultipleMatches=false, should return null after first match", token);
    }
    
    @Test(timeout = 4000)
    public void testAllowMultipleMatchesWithScalar() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "[3, 4]";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_ARRAY
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, false, true); // _allowMultipleMatches = true
        
        assertEquals(JsonToken.START_ARRAY, delegate.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(3, delegate.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(4, delegate.getIntValue());
        assertEquals(JsonToken.END_ARRAY, delegate.nextToken());
        assertNull(delegate.nextToken());
    }
    
    // ===========================================================
    // Part D: Exception & Defensive Guard Paths
    // ===========================================================
    
    @Test(timeout = 4000)
    public void testSkipChildrenOnNonContainer() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "123";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // VALUE_NUMBER_INT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        delegate.nextToken(); // VALUE_NUMBER_INT
        JsonParser result = delegate.skipChildren();
        assertSame("skipChildren should return this for non-container", delegate, result);
    }
    
    @Test(timeout = 4000)
    public void testSkipChildrenOnObject() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":1,\"b\":2}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        delegate.nextToken(); // START_OBJECT
        JsonParser result = delegate.skipChildren();
        assertSame("skipChildren should return this", delegate, result);
        assertEquals("After skipChildren, should be at END_OBJECT", 
            JsonToken.END_OBJECT, delegate.getCurrentToken());
    }
    
    @Test(timeout = 4000)
    public void testSkipChildrenOnArray() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "[1,2,3]";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_ARRAY
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        delegate.nextToken(); // START_ARRAY
        JsonParser result = delegate.skipChildren();
        assertSame("skipChildren should return this", delegate, result);
        assertEquals("After skipChildren, should be at END_ARRAY", 
            JsonToken.END_ARRAY, delegate.getCurrentToken());
    }
    
    @Test(timeout = 4000)
    public void testNextValue() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":1}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        delegate.nextToken(); // START_OBJECT
        JsonToken token = delegate.nextValue();
        assertEquals("nextValue should skip FIELD_NAME and return value", 
            JsonToken.VALUE_NUMBER_INT, token);
        assertEquals(1, delegate.getIntValue());
    }
    
    // ===========================================================
    // Part E: Object Lifecycle & Contract Integrity
    // ===========================================================
    
    @Test(timeout = 4000)
    public void testHasTokenId() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "123";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // VALUE_NUMBER_INT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        // Before any token
        assertTrue("hasTokenId should return true for ID_NO_TOKEN when no token",
            delegate.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse("hasTokenId should return false for other IDs when no token",
            delegate.hasTokenId(JsonTokenId.ID_FIELD_NAME));
        
        delegate.nextToken(); // VALUE_NUMBER_INT
        
        assertTrue("hasTokenId should return true for matching ID",
            delegate.hasTokenId(JsonTokenId.ID_NUMBER_INT));
        assertFalse("hasTokenId should return false for non-matching ID",
            delegate.hasTokenId(JsonTokenId.ID_FIELD_NAME));
    }
    
    @Test(timeout = 4000)
    public void testHasToken() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "123";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // VALUE_NUMBER_INT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        assertFalse("hasToken should return false when no token",
            delegate.hasToken(JsonToken.VALUE_NUMBER_INT));
        
        delegate.nextToken();
        
        assertTrue("hasToken should return true for matching token",
            delegate.hasToken(JsonToken.VALUE_NUMBER_INT));
        assertFalse("hasToken should return false for non-matching token",
            delegate.hasToken(JsonToken.FIELD_NAME));
    }
    
    @Test(timeout = 4000)
    public void testGetParsingContext() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":1}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        JsonStreamContext context = delegate.getParsingContext();
        assertNotNull("Parsing context should not be null", context);
        assertEquals("Should be root context initially", 
            JsonStreamContext.TYPE_ROOT, context.getType());
    }
    
    @Test(timeout = 4000)
    public void testDelegatedMethods() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":\"hello\",\"b\":true,\"c\":null}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        delegate.nextToken(); // START_OBJECT
        delegate.nextToken(); // FIELD_NAME "a"
        delegate.nextToken(); // VALUE_STRING "hello"
        
        assertEquals("getText should return 'hello'", "hello", delegate.getText());
        assertTrue("hasTextCharacters should return true", delegate.hasTextCharacters());
        assertNotNull("getTextCharacters should not be null", delegate.getTextCharacters());
        assertTrue("getTextLength should be > 0", delegate.getTextLength() > 0);
        
        delegate.nextToken(); // FIELD_NAME "b"
        delegate.nextToken(); // VALUE_TRUE
        
        assertTrue("getBooleanValue should return true", delegate.getBooleanValue());
        
        delegate.nextToken(); // FIELD_NAME "c"
        delegate.nextToken(); // VALUE_NULL
        
        assertNull("getEmbeddedObject should return null for null value", 
            delegate.getEmbeddedObject());
    }
    
    @Test(timeout = 4000)
    public void testNumericMethods() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"int\":42,\"long\":1234567890123,\"double\":3.14,\"neg\":-5}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        delegate.nextToken(); // START_OBJECT
        
        // Test int value
        delegate.nextToken(); // FIELD_NAME "int"
        delegate.nextToken(); // VALUE_NUMBER_INT 42
        assertEquals(42, delegate.getIntValue());
        assertEquals(42L, delegate.getLongValue());
        assertEquals(42.0, delegate.getDoubleValue(), 0.001);
        assertEquals(42f, delegate.getFloatValue(), 0.001f);
        assertEquals((byte)42, delegate.getByteValue());
        assertEquals((short)42, delegate.getShortValue());
        assertEquals(BigInteger.valueOf(42), delegate.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(42), delegate.getDecimalValue());
        assertEquals(JsonParser.NumberType.INT, delegate.getNumberType());
        assertEquals(42, delegate.getNumberValue());
        
        // Test getValueAsXxx methods
        assertEquals(42, delegate.getValueAsInt());
        assertEquals(42L, delegate.getValueAsLong());
        assertEquals(42.0, delegate.getValueAsDouble(), 0.001);
        assertTrue(delegate.getValueAsBoolean());
        assertEquals("42", delegate.getValueAsString());
    }
    
    @Test(timeout = 4000)
    public void testGetValueAsXxxWithDefaults() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "null";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // VALUE_NULL
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        delegate.nextToken(); // VALUE_NULL
        
        assertEquals(99, delegate.getValueAsInt(99));
        assertEquals(99L, delegate.getValueAsLong(99));
        assertEquals(99.0, delegate.getValueAsDouble(99.0), 0.001);
        assertFalse(delegate.getValueAsBoolean(true));
        assertEquals("default", delegate.getValueAsString("default"));
    }
    
    @Test(timeout = 4000)
    public void testGetCurrentLocation() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "123";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // VALUE_NUMBER_INT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        JsonLocation location = delegate.getCurrentLocation();
        assertNotNull("Current location should not be null", location);
    }
    
    @Test(timeout = 4000)
    public void testGetTokenLocation() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "123";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // VALUE_NUMBER_INT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        JsonLocation location = delegate.getTokenLocation();
        assertNotNull("Token location should not be null", location);
    }
    
    @Test(timeout = 4000)
    public void testFilteringWithCustomFilter() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":1,\"b\":2,\"c\":3}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        // Create a filter that only includes property "b"
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("b".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };
        
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("b", delegate.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(2, delegate.getIntValue());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testFilteringWithArrayFilter() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "[1,2,3,4,5]";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_ARRAY
        
        // Create a filter that only includes even numbers
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return p.getIntValue() % 2 == 0;
            }
        };
        
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        assertEquals(JsonToken.START_ARRAY, delegate.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(2, delegate.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(4, delegate.getIntValue());
        assertEquals(JsonToken.END_ARRAY, delegate.nextToken());
        assertNull(delegate.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testMatchCount() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":1,\"b\":2}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        assertEquals(0, delegate.getMatchCount());
        
        delegate.nextToken(); // START_OBJECT
        delegate.nextToken(); // FIELD_NAME "a"
        delegate.nextToken(); // VALUE_NUMBER_INT 1
        
        // Match count should be incremented
        assertTrue("Match count should be > 0", delegate.getMatchCount() > 0);
    }
    
    @Test(timeout = 4000)
    public void testComplexNestedFiltering() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"level1\":{\"level2\":{\"target\":42}}}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        // Filter that only includes the "target" property
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("target".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                // Include path to nested objects
                return this;
            }
        };
        
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("level1", delegate.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("level2", delegate.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("target", delegate.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(42, delegate.getIntValue());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testFilteringWithIncludePathFalse() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":{\"b\":1}}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        // Filter that only includes property "b"
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("b".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };
        
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, false, true); // _includePath = false
        
        // With includePath=false, we should only get the matching value
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(1, delegate.getIntValue());
        assertNull(delegate.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testFilteringWithIncludePathTrue() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":{\"b\":1}}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        // Filter that only includes property "b"
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("b".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };
        
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true); // _includePath = true
        
        // With includePath=true, we should get the path to the match
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("a", delegate.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("b", delegate.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(1, delegate.getIntValue());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testBinaryValue() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "\"dGVzdA==\""; // base64 for "test"
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // VALUE_STRING
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        delegate.nextToken(); // VALUE_STRING
        
        byte[] binary = delegate.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull("Binary value should not be null", binary);
        assertTrue("Binary value length should be > 0", binary.length > 0);
    }
    
    @Test(timeout = 4000)
    public void testMultipleMatchesWithArray() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "[1,2,3]";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_ARRAY
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, false, true); // allowMultipleMatches = true
        
        assertEquals(JsonToken.START_ARRAY, delegate.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(1, delegate.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(2, delegate.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(3, delegate.getIntValue());
        assertEquals(JsonToken.END_ARRAY, delegate.nextToken());
        assertNull(delegate.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testFilterFinishArray() throws Exception {
        JsonFactory factory = new JsonFactory();
        String json = "{\"a\":[1,2]}";
        JsonParser parser = factory.createParser(json);
        parser.nextToken(); // START_OBJECT
        
        // Custom filter that tracks filterFinishArray calls
        final boolean[] finishArrayCalled = {false};
        TokenFilter filter = new TokenFilter() {
            @Override
            public void filterFinishArray() {
                finishArrayCalled[0] = true;
            }
            
            @Override
            public TokenFilter filterStartArray() {
                return this;
            }
        };
        
        FilteringParserDelegate delegate = new FilteringParserDelegate(
            parser, filter, true, true);
        
        delegate.nextToken(); // START_OBJECT
        delegate.nextToken(); // FIELD_NAME "a"
        delegate.nextToken(); // START_ARRAY
        delegate.nextToken(); // VALUE_NUMBER_INT 1
        delegate.nextToken(); // VALUE_NUMBER_INT 2
        delegate.nextToken(); // END_ARRAY
        
        assertTrue("filterFinishArray should have been called", finishArrayCalled[0]);
    }
}