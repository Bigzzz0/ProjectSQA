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

/*
 [Branch & Defect Analysis Matrix]
 -------------------------------------------------------------------------------------------------------------------
 Decision / Branch Target                     | Condition / Boundary               | Test Method
 ---------------------------------------------+------------------------------------+--------------------------------
 Defects4J Defect #330: single match + path   | !_allowMultipleMatches &&          | testDefect330SingleMatchWithPath
 struct end premature null return             | includePath && structEnd           | testDefect330NestedObjectWithPath
 Construction & State Getters                 | Initial state, null tokens, IDs    | testInitialStateAndTokenAccessors
 Token matching: Include all filter           | rootFilter == INCLUDE_ALL          | testIncludeAllRootFilter
 Scalar filtering (int, double, bool, null)   | includeValue true/false            | testScalarFiltering
 Array traversal & element inclusion          | Array start, elements, array end   | testArrayFilteringWithElements
 Object traversal & property inclusion        | Property matched/skipped, nested   | testObjectFilteringIncludePath
 Offlined traversal: _nextToken2              | Non-immediate inclusion, buffering | testDeepBufferingTraversal
 Immediate parent inclusion feature          | _includeImmediateParent == true    | testIncludeImmediateParent
 Skip children (Object & Array)               | isStructStart(), nesting level     | testSkipChildrenOnStructures
 Clear token and last cleared token state     | clearCurrentToken(), token null    | testClearCurrentToken
 getCurrentName() on Struct Start vs Property | START_OBJECT/ARRAY vs FIELD/scalar | testGetCurrentNameVariations
 Delegation: text, numbers, conversions       | All delegate getter wrappers       | testDelegatedValueAccessors
 Defensive guard: overrideCurrentName()       | UnsupportedOperationException      | testOverrideCurrentNameThrows
 -------------------------------------------------------------------------------------------------------------------
*/
public class FilteringParserDelegateGeminiTest {

    private static final JsonFactory JSON_F = new JsonFactory();

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Defect #330)
    // =========================================================================

    /**
     * Targets Defects4J bug: When _allowMultipleMatches is false and _includePath is true,
     * closing an inner structure prematurely set _currToken = null and returned null,
     * dropping the enclosing END_OBJECT tokens.
     */
    @Test(timeout = 4000)
    public void testDefect330SingleMatchWithPath() throws IOException {
        String json = "{\"ob\":{\"value\":3}}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("ob".equals(name)) {
                    return this;
                }
                if ("value".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        // includePath = true, allowMultipleMatches = false
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, false);

        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("ob", fp.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("value", fp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        assertEquals(3, fp.getIntValue());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());

        // Defects4J bug manifests here: returns null instead of outer END_OBJECT
        JsonToken outerEnd = fp.nextToken();
        assertNotNull("Expected outer END_OBJECT token but received null due to bug #330", outerEnd);
        assertEquals(JsonToken.END_OBJECT, outerEnd);

        assertNull(fp.nextToken());
        fp.close();
    }

    /**
     * Additional Defect #330 verification with nested arrays inside objects.
     */
    @Test(timeout = 4000)
    public void testDefect330NestedObjectWithPath() throws IOException {
        String json = "{\"wrapper\":{\"arr\":[42]}}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("wrapper".equals(name)) {
                    return this;
                }
                if ("arr".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, false);
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("wrapper", fp.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("arr", fp.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        assertEquals(42, fp.getIntValue());
        assertEquals(JsonToken.END_ARRAY, fp.nextToken());

        JsonToken innerEnd = fp.nextToken();
        assertNotNull("Inner struct END_OBJECT must not be null", innerEnd);
        assertEquals(JsonToken.END_OBJECT, innerEnd);

        JsonToken outerEnd = fp.nextToken();
        assertNotNull("Root struct END_OBJECT must not be null", outerEnd);
        assertEquals(JsonToken.END_OBJECT, outerEnd);

        assertNull(fp.nextToken());
        fp.close();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateAndTokenAccessors() throws IOException {
        String json = "{\"id\":100}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, true);

        assertSame(filter, fp.getFilter());
        assertEquals(0, fp.getMatchCount());

        // State before reading any tokens
        assertNull(fp.getCurrentToken());
        assertNull(fp.currentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, fp.getCurrentTokenId());
        assertEquals(JsonTokenId.ID_NO_TOKEN, fp.currentTokenId());
        assertFalse(fp.hasCurrentToken());
        assertTrue(fp.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(fp.hasToken(JsonToken.START_OBJECT));
        assertFalse(fp.isExpectedStartObjectToken());
        assertFalse(fp.isExpectedStartArrayToken());
        assertNull(fp.getLastClearedToken());

        // Read first token
        JsonToken t = fp.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        assertSame(t, fp.getCurrentToken());
        assertSame(t, fp.currentToken());
        assertEquals(JsonTokenId.ID_START_OBJECT, fp.getCurrentTokenId());
        assertEquals(JsonTokenId.ID_START_OBJECT, fp.currentTokenId());
        assertTrue(fp.hasCurrentToken());
        assertTrue(fp.hasTokenId(JsonTokenId.ID_START_OBJECT));
        assertTrue(fp.hasToken(JsonToken.START_OBJECT));
        assertTrue(fp.isExpectedStartObjectToken());
        assertFalse(fp.isExpectedStartArrayToken());

        JsonLocation loc = fp.getCurrentLocation();
        assertNotNull(loc);
        assertNotNull(fp.getTokenLocation());

        fp.close();
    }

    @Test(timeout = 4000)
    public void testIncludeAllRootFilter() throws IOException {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("name", fp.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, fp.nextToken());
        assertEquals("Alice", fp.getText());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("age", fp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        assertEquals(30, fp.getIntValue());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        assertNull(fp.nextToken());
        fp.close();
    }

    @Test(timeout = 4000)
    public void testScalarFiltering() throws IOException {
        String json = "[10, 20, 30]";
        JsonParser p = JSON_F.createParser(json);

        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return p.getIntValue() == 20;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, true);
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        assertEquals(20, fp.getIntValue());
        assertNull(fp.nextToken());
        fp.close();
    }

    @Test(timeout = 4000)
    public void testArrayFilteringWithElements() throws IOException {
        String json = "[1, [2, 3], 4]";
        JsonParser p = JSON_F.createParser(json);

        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter filterStartArray() {
                return this;
            }

            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return p.getIntValue() == 3;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);
        assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        assertEquals(3, fp.getIntValue());
        assertEquals(JsonToken.END_ARRAY, fp.nextToken());
        assertEquals(JsonToken.END_ARRAY, fp.nextToken());
        assertNull(fp.nextToken());
        fp.close();
    }

    @Test(timeout = 4000)
    public void testObjectFilteringIncludePath() throws IOException {
        String json = "{\"user\":{\"id\":123,\"secret\":\"hide\"},\"public\":true}";
        JsonParser p = JSON_F.createParser(json);

        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("user".equals(name)) {
                    return this;
                }
                if ("id".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("user", fp.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("id", fp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        assertEquals(123, fp.getIntValue());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        assertNull(fp.nextToken());
        fp.close();
    }

    @Test(timeout = 4000)
    public void testNextValueMethod() throws IOException {
        String json = "{\"a\":1,\"b\":2}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, fp.nextValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextValue());
        assertEquals(1, fp.getIntValue());
        assertEquals("a", fp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextValue());
        assertEquals(2, fp.getIntValue());
        assertEquals("b", fp.getCurrentName());
        assertEquals(JsonToken.END_OBJECT, fp.nextValue());
        assertNull(fp.nextValue());
        fp.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Traversal Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeepBufferingTraversal() throws IOException {
        String json = "{\"a\":{\"b\":{\"c\":{\"target\":999},\"d\":100}}}";
        JsonParser p = JSON_F.createParser(json);

        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("a".equals(name) || "b".equals(name) || "c".equals(name)) {
                    return this;
                }
                if ("target".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        // includePath = true activates _nextTokenWithBuffering and _nextBuffered
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("a", fp.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("b", fp.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("c", fp.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("target", fp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        assertEquals(999, fp.getIntValue());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        assertNull(fp.nextToken());
        fp.close();
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnStructures() throws IOException {
        String json = "{\"skipMe\":{\"k1\":\"v1\",\"k2\":[1,2]},\"keepMe\":true}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("skipMe", fp.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());

        // Skip children inside skipMe object
        assertSame(fp, fp.skipChildren());
        assertEquals(JsonToken.END_OBJECT, fp.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("keepMe", fp.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, fp.nextToken());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        assertNull(fp.nextToken());
        fp.close();
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnScalarHasNoEffect() throws IOException {
        String json = "[10, 20]";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        // Calling skipChildren on scalar should simply return this without consuming tokens
        assertSame(fp, fp.skipChildren());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.getCurrentToken());
        assertEquals(10, fp.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        assertEquals(20, fp.getIntValue());
        assertEquals(JsonToken.END_ARRAY, fp.nextToken());
        fp.close();
    }

    @Test(timeout = 4000)
    public void testClearCurrentToken() throws IOException {
        String json = "[42]";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());

        assertTrue(fp.hasCurrentToken());
        fp.clearCurrentToken();
        assertFalse(fp.hasCurrentToken());
        assertNull(fp.getCurrentToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.getLastClearedToken());

        // Calling clear again when already null
        fp.clearCurrentToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.getLastClearedToken());

        assertEquals(JsonToken.END_ARRAY, fp.nextToken());
        fp.close();
    }

    @Test(timeout = 4000)
    public void testGetCurrentNameVariations() throws IOException {
        String json = "{\"array\":[{\"prop\":\"val\"}]}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        // At root START_OBJECT, parent is null -> getCurrentName should be null
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertNull(fp.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("array", fp.getCurrentName());

        // At START_ARRAY under property "array", parent is Property context -> should return "array"
        assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        assertEquals("array", fp.getCurrentName());

        // At START_OBJECT in array, parent is Array context -> getCurrentName should be null
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertNull(fp.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("prop", fp.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, fp.nextToken());
        assertEquals("prop", fp.getCurrentName());

        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        assertEquals(JsonToken.END_ARRAY, fp.nextToken());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        fp.close();
    }

    @Test(timeout = 4000)
    public void testIncludeImmediateParent() throws IOException {
        String json = "{\"container\":{\"target\":123}}";
        JsonParser p = JSON_F.createParser(json);

        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("target".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return this;
            }
        };

        // Subclass to activate deprecated/protected _includeImmediateParent
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, true) {
            {
                this._includeImmediateParent = true;
            }
        };

        JsonToken t = fp.nextToken();
        assertNotNull(t);
        // Either START_OBJECT of immediate parent is induced, or target is emitted directly
        assertTrue(t == JsonToken.START_OBJECT || t == JsonToken.FIELD_NAME);
        while (fp.nextToken() != null) {
            // Drain remaining tokens
        }
        fp.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testOverrideCurrentNameThrows() throws IOException {
        String json = "{\"field\":\"value\"}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);
        try {
            fp.nextToken();
            fp.overrideCurrentName("newName");
        } finally {
            fp.close();
        }
    }

    @Test(timeout = 4000)
    public void testEmptyJsonReturnsNull() throws IOException {
        String json = "";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);
        assertNull(fp.nextToken());
        fp.close();
    }

    @Test(timeout = 4000)
    public void testFilterPruningEntireContent() throws IOException {
        String json = "{\"a\":1,\"b\":[2,3]}";
        JsonParser p = JSON_F.createParser(json);
        // Filter rejecting everything
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return null;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, true);
        assertNull(fp.nextToken());
        fp.close();
    }

    // =========================================================================
    // Partition E: Delegated Value Accessors & Parsing Context
    // =========================================================================

    @Test(timeout = 4000)
    public void testDelegatedValueAccessors() throws IOException {
        String json = "{\"num\":42,\"pi\":3.1415,\"flag\":true,\"text\":\"hello\",\"big\":12345678901234567890}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        // {"num":42}
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        JsonStreamContext sc = fp.getParsingContext();
        assertNotNull(sc);

        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("num", fp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());

        assertEquals(42, fp.getIntValue());
        assertEquals(42L, fp.getLongValue());
        assertEquals((short) 42, fp.getShortValue());
        assertEquals((byte) 42, fp.getByteValue());
        assertEquals(42.0f, fp.getFloatValue(), 0.001f);
        assertEquals(42.0, fp.getDoubleValue(), 0.001);
        assertEquals(BigInteger.valueOf(42), fp.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(42), fp.getDecimalValue());
        assertEquals(JsonParser.NumberType.INT, fp.getNumberType());
        assertEquals(Integer.valueOf(42), fp.getNumberValue());
        assertEquals(42, fp.getValueAsInt());
        assertEquals(42, fp.getValueAsInt(99));
        assertEquals(42L, fp.getValueAsLong());
        assertEquals(42L, fp.getValueAsLong(99L));
        assertEquals(42.0, fp.getValueAsDouble(), 0.001);
        assertEquals(42.0, fp.getValueAsDouble(99.0), 0.001);

        // "pi":3.1415
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, fp.nextToken());
        assertEquals(3.1415, fp.getDoubleValue(), 0.0001);

        // "flag":true
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, fp.nextToken());
        assertTrue(fp.getBooleanValue());
        assertTrue(fp.getValueAsBoolean());
        assertTrue(fp.getValueAsBoolean(false));

        // "text":"hello"
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals(JsonToken.VALUE_STRING, fp.nextToken());
        assertEquals("hello", fp.getText());
        assertTrue(fp.hasTextCharacters());
        assertNotNull(fp.getTextCharacters());
        assertTrue(fp.getTextLength() > 0);
        assertTrue(fp.getTextOffset() >= 0);
        assertEquals("hello", fp.getValueAsString());
        assertEquals("hello", fp.getValueAsString("fallback"));
        assertNull(fp.getEmbeddedObject());

        // "big":12345678901234567890
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        assertEquals(new BigInteger("12345678901234567890"), fp.getBigIntegerValue());

        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        fp.close();
    }

    @Test(timeout = 4000)
    public void testBinaryDelegation() throws IOException {
        String base64Content = "SGVsbG8gV29ybGQ="; // "Hello World"
        String json = "{\"data\":\"" + base64Content + "\"}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals(JsonToken.VALUE_STRING, fp.nextToken());

        byte[] binary = fp.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull(binary);
        assertEquals("Hello World", new String(binary));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = fp.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(11, count);
        assertEquals("Hello World", new String(out.toByteArray()));

        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        fp.close();
    }
}