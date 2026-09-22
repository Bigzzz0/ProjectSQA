package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectCodec;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TokenBuffer (and inner classes Segment, Parser)
 * 
 * Known Defect (Defects4J): When parsing a buffer that starts with a field name,
 * then START_OBJECT, the parser's getCurrentName() returns the wrong name.
 * Specifically, after writing field "b" then start object, the parser should
 * return "b" when current token is START_OBJECT, but it returns "a" (the previous field name).
 * 
 * Branches targeted:
 * - Parser.getCurrentName(): if _currToken == START_OBJECT or START_ARRAY, use parent context
 * - Parser.nextToken(): update _parsingContext on START_OBJECT/START_ARRAY/END_OBJECT/END_ARRAY
 * - Segment.append/set: handling of token types, values, native ids
 * - _append methods: with/without native ids, with/without value
 * - serialize: switch on token types, handling of native ids
 * - copyCurrentEvent: switch on token types, _forceBigDecimal branch
 * - copyCurrentStructure: field name handling, recursive copy
 * - writeObject/writeTree: null handling, codec vs embedded
 * - writeNumber: null handling, various number types
 * - writeString: null handling
 * - writeFieldName: String vs SerializableString
 * - _checkNativeIds: setting _hasNativeId
 * - toString: iteration, native ids, truncation
 * - append(TokenBuffer): merging native ids
 * - deserialize: starting from FIELD_NAME
 * 
 * Boundary conditions:
 * - null values for strings, numbers, objects
 * - empty strings
 * - MAX/MIN integer, long, double, float
 * - BigDecimal, BigInteger extremes
 * - Segment full (16 tokens) causing new segment creation
 * - Native ids present/absent
 * - _forceBigDecimal true/false
 * - Unbalanced start/end markers
 * - Empty buffer
 */
public class TokenBufferDeepseekTest {

    /* ============================================================
     * Partition A: Core Functional Logic & State Transitions
     * ============================================================ */

    @Test(timeout = 4000)
    public void testBasicAppendAndFirstToken() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertNull(buffer.firstToken());
        buffer.writeStartArray();
        assertEquals(JsonToken.START_ARRAY, buffer.firstToken());
        buffer.writeEndArray();
        assertEquals(JsonToken.START_ARRAY, buffer.firstToken());
    }

    @Test(timeout = 4000)
    public void testWriteAndReadSimpleTokens() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("key");
        buffer.writeString("value");
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testWriteAndReadNestedStructures() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartArray();
        buffer.writeStartObject();
        buffer.writeFieldName("a");
        buffer.writeNumber(1);
        buffer.writeEndObject();
        buffer.writeEndArray();

        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testWriteNumberVariants() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeNumber((short) 42);
        buffer.writeNumber(123);
        buffer.writeNumber(456L);
        buffer.writeNumber(3.14);
        buffer.writeNumber(2.71f);
        buffer.writeNumber(new BigDecimal("123.456"));
        buffer.writeNumber(new BigInteger("9999999999999999999"));
        buffer.writeNumber("1.23e4");

        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getShortValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(456L, parser.getLongValue());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 1e-9);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(2.71f, parser.getFloatValue(), 1e-9);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(new BigDecimal("123.456"), parser.getDecimalValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(new BigInteger("9999999999999999999"), parser.getBigIntegerValue());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.23e4, parser.getDoubleValue(), 1e-9);
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testWriteStringNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeString((String) null);
        buffer.writeString((SerializableString) null);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testWriteBooleanAndNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeBoolean(true);
        buffer.writeBoolean(false);
        buffer.writeNull();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        assertToken(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getBooleanValue());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testWriteEmbeddedObject() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new int[]{1,2,3});
        buffer.writeObject(new TextNode("hello"));
        buffer.writeObject((Object) null);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(new int[]{1,2,3}, (int[]) parser.getEmbeddedObject());
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertEquals("hello", ((TextNode) parser.getEmbeddedObject()).asText());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testWriteRawValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeRawValue("raw");
        buffer.writeRawValue("partial", 1, 3);
        buffer.writeRawValue(new char[]{'a','b','c'}, 0, 2);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertEquals("raw", ((RawValue) parser.getEmbeddedObject()).toString());
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertEquals("art", ((RawValue) parser.getEmbeddedObject()).toString());
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertEquals("ab", ((RawValue) parser.getEmbeddedObject()).toString());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testWriteBinary() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1,2,3,4};
        buffer.writeBinary(Base64Variants.MIME, data, 0, data.length);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testWriteFieldNameSerializableString() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName(new SerializableString() {
            public String getValue() { return "ser"; }
            public int charLength() { return 3; }
            public char[] asQuotedChars() { return new char[]{'s','e','r'}; }
            public byte[] asUnquotedUTF8() { return new byte[]{'s','e','r'}; }
            public byte[] asQuotedUTF8() { return new byte[]{'s','e','r'}; }
        });
        buffer.writeString("val");
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("ser", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("val", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithCodec() throws IOException {
        ObjectCodec codec = new ObjectCodec() {
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            public TreeNode readTree(JsonParser p) { return null; }
            public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            public <T> T readValue(JsonParser p, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) { return null; }
            public <T> T readValue(JsonParser p, com.fasterxml.jackson.databind.JavaType valueType) { return null; }
            public void writeValue(JsonGenerator gen, Object value) throws IOException {
                gen.writeString("codec:" + value);
            }
            public <T> T readValue(com.fasterxml.jackson.databind.DeserializationConfig cfg, JsonParser p, com.fasterxml.jackson.databind.JavaType valueType) { return null; }
            public <T> T readValue(com.fasterxml.jackson.databind.DeserializationConfig cfg, JsonParser p, Class<T> valueType) { return null; }
            public <T> T readValue(com.fasterxml.jackson.databind.DeserializationConfig cfg, JsonParser p, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) { return null; }
            public com.fasterxml.jackson.databind.JsonNode getFactory() { return null; }
        };
        TokenBuffer buffer = new TokenBuffer(codec, false);
        buffer.writeObject("test");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("codec:test", parser.getText());
    }

    @Test(timeout = 4000)
    public void testWriteTree() throws IOException {
        ObjectCodec codec = new ObjectCodec() {
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            public TreeNode readTree(JsonParser p) { return null; }
            public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            public <T> T readValue(JsonParser p, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) { return null; }
            public <T> T readValue(JsonParser p, com.fasterxml.jackson.databind.JavaType valueType) { return null; }
            public void writeValue(JsonGenerator gen, Object value) throws IOException {
                gen.writeNumber(42);
            }
            public <T> T readValue(com.fasterxml.jackson.databind.DeserializationConfig cfg, JsonParser p, com.fasterxml.jackson.databind.JavaType valueType) { return null; }
            public <T> T readValue(com.fasterxml.jackson.databind.DeserializationConfig cfg, JsonParser p, Class<T> valueType) { return null; }
            public <T> T readValue(com.fasterxml.jackson.databind.DeserializationConfig cfg, JsonParser p, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) { return null; }
            public com.fasterxml.jackson.databind.JsonNode getFactory() { return null; }
        };
        TokenBuffer buffer = new TokenBuffer(codec, false);
        buffer.writeTree(new TextNode("tree"));
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testWriteTreeNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeTree(null);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
    }

    /* ============================================================
     * Partition B: Boundary Value Analysis & Extremes
     * ============================================================ */

    @Test(timeout = 4000)
    public void testBoundaryNumbers() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeNumber(Integer.MAX_VALUE);
        buffer.writeNumber(Integer.MIN_VALUE);
        buffer.writeNumber(Long.MAX_VALUE);
        buffer.writeNumber(Long.MIN_VALUE);
        buffer.writeNumber(Double.MAX_VALUE);
        buffer.writeNumber(Double.MIN_NORMAL);
        buffer.writeNumber(Float.MAX_VALUE);
        buffer.writeNumber(Float.MIN_NORMAL);
        buffer.writeNumber(new BigDecimal("1e-1000"));
        buffer.writeNumber(new BigInteger("-99999999999999999999999999999999999999"));

        JsonParser parser = buffer.asParser();
        assertEquals(Integer.MAX_VALUE, parser.nextIntValue(0));
        assertEquals(Integer.MIN_VALUE, parser.nextIntValue(0));
        assertEquals(Long.MAX_VALUE, parser.nextLongValue(0));
        assertEquals(Long.MIN_VALUE, parser.nextLongValue(0));
        assertEquals(Double.MAX_VALUE, parser.nextDoubleValue(), 0);
        assertEquals(Double.MIN_NORMAL, parser.nextDoubleValue(), 0);
        assertEquals(Float.MAX_VALUE, parser.nextFloatValue(), 0);
        assertEquals(Float.MIN_NORMAL, parser.nextFloatValue(), 0);
        assertEquals(new BigDecimal("1e-1000"), parser.nextDecimalValue());
        assertEquals(new BigInteger("-99999999999999999999999999999999999999"), parser.nextBigIntegerValue());
    }

    @Test(timeout = 4000)
    public void testEmptyStringFieldName() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("");
        buffer.writeString("empty");
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("empty", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testSegmentFullCreatesNewSegment() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        // Write 16 tokens to fill first segment
        for (int i = 0; i < 16; i++) {
            buffer.writeNumber(i);
        }
        // This should create a new segment
        buffer.writeNumber(16);
        JsonParser parser = buffer.asParser();
        for (int i = 0; i < 17; i++) {
            assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(i, parser.getIntValue());
        }
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNativeIdsBoundary() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, true);
        buffer.writeStartObject();
        buffer.writeObjectId("obj1");
        buffer.writeTypeId("type1");
        buffer.writeFieldName("x");
        buffer.writeString("y");
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        assertTrue(parser.canReadObjectId());
        assertTrue(parser.canReadTypeId());
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        // Native ids are attached to the following token (field name)
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("obj1", parser.getObjectId());
        assertEquals("type1", parser.getTypeId());
        assertEquals("x", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("y", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testForceBigDecimal() throws IOException {
        // Create a context that enables USE_BIG_DECIMAL_FOR_FLOATS
        DeserializationContext ctxt = new DeserializationContext(null, null, null, null) {
            public boolean isEnabled(DeserializationFeature f) {
                return f == DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS;
            }
        };
        TokenBuffer buffer = new TokenBuffer(null, ctxt);
        assertTrue(buffer._forceBigDecimal);
        buffer.writeNumber(3.14);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(new BigDecimal("3.14"), parser.getDecimalValue());
    }

    /* ============================================================
     * Partition C: Defect-Targeted Branch Zone
     * ============================================================ */

    /**
     * Targets the known defect: after writing field name "b" then start object,
     * the parser's getCurrentName() should return "b" when current token is START_OBJECT.
     * The defective version returns "a" (previous field name).
     */
    @Test(timeout = 4000)
    public void testGetCurrentNameAfterStartObject() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("a");
        buffer.writeNumber(1);
        buffer.writeFieldName("b");
        buffer.writeStartObject();
        buffer.writeFieldName("c");
        buffer.writeString("d");
        buffer.writeEndObject();
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        // Now we are at START_OBJECT; getCurrentName() should return "b"
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("b", parser.getCurrentName());  // <-- This is the defect check
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("c", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("d", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    /**
     * Similar test for START_ARRAY: after field name, start array should also return field name.
     */
    @Test(timeout = 4000)
    public void testGetCurrentNameAfterStartArray() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("arr");
        buffer.writeStartArray();
        buffer.writeNumber(1);
        buffer.writeEndArray();
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("arr", parser.getCurrentName());
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals("arr", parser.getCurrentName());  // Should be "arr"
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    /* ============================================================
     * Partition D: Exception & Defensive Guard Paths
     * ============================================================ */

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUnsupported() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeRaw("unsupported");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUTF8StringUnsupported() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeRawUTF8String(new byte[]{}, 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteUTF8StringUnsupported() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeUTF8String(new byte[]{}, 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteBinaryStreamUnsupported() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeBinary(Base64Variants.MIME, new java.io.ByteArrayInputStream(new byte[]{}), 0);
    }

    @Test(timeout = 4000)
    public void testWriteNumberNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeNumber((BigDecimal) null);
        buffer.writeNumber((BigInteger) null);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testWriteObjectNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testDeserializeStartingFromFieldName() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        // Simulate a parser that starts at FIELD_NAME
        JsonParser p = new JsonParser() {
            private int state = 0;
            public JsonToken nextToken() {
                switch (state++) {
                    case 0: return JsonToken.FIELD_NAME;
                    case 1: return JsonToken.VALUE_STRING;
                    case 2: return JsonToken.END_OBJECT;
                    default: return null;
                }
            }
            public String getCurrentName() { return "x"; }
            public String getText() { return "y"; }
            // other methods not needed
        };
        buffer.deserialize(p, null);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("x", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("y", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testAppendTokenBuffer() throws IOException {
        TokenBuffer buffer1 = new TokenBuffer(null, false);
        buffer1.writeNumber(1);
        TokenBuffer buffer2 = new TokenBuffer(null, false);
        buffer2.writeNumber(2);
        buffer1.append(buffer2);
        JsonParser parser = buffer1.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testSerialize() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("a");
        buffer.writeString("b");
        buffer.writeEndObject();

        TokenBuffer out = new TokenBuffer(null, false);
        buffer.serialize(out);
        JsonParser parser = out.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("b", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testSerializeWithNativeIds() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, true);
        buffer.writeStartObject();
        buffer.writeObjectId("oid");
        buffer.writeTypeId("tid");
        buffer.writeFieldName("x");
        buffer.writeNumber(1);
        buffer.writeEndObject();

        TokenBuffer out = new TokenBuffer(null, true);
        buffer.serialize(out);
        JsonParser parser = out.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("oid", parser.getObjectId());
        assertEquals("tid", parser.getTypeId());
        assertEquals("x", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testCopyCurrentEvent() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("f");
        buffer.writeString("v");
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        TokenBuffer copy = new TokenBuffer(null, false);
        parser.nextToken(); // START_OBJECT
        copy.copyCurrentEvent(parser);
        parser.nextToken(); // FIELD_NAME
        copy.copyCurrentEvent(parser);
        parser.nextToken(); // VALUE_STRING
        copy.copyCurrentEvent(parser);
        parser.nextToken(); // END_OBJECT
        copy.copyCurrentEvent(parser);

        JsonParser copyParser = copy.asParser();
        assertToken(JsonToken.START_OBJECT, copyParser.nextToken());
        assertToken(JsonToken.FIELD_NAME, copyParser.nextToken());
        assertEquals("f", copyParser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, copyParser.nextToken());
        assertEquals("v", copyParser.getText());
        assertToken(JsonToken.END_OBJECT, copyParser.nextToken());
        assertNull(copyParser.nextToken());
    }

    @Test(timeout = 4000)
    public void testCopyCurrentStructure() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartArray();
        buffer.writeStartObject();
        buffer.writeFieldName("k");
        buffer.writeNumber(42);
        buffer.writeEndObject();
        buffer.writeEndArray();

        JsonParser parser = buffer.asParser();
        TokenBuffer copy = new TokenBuffer(null, false);
        parser.nextToken(); // START_ARRAY
        copy.copyCurrentStructure(parser); // should copy entire array

        JsonParser copyParser = copy.asParser();
        assertToken(JsonToken.START_ARRAY, copyParser.nextToken());
        assertToken(JsonToken.START_OBJECT, copyParser.nextToken());
        assertToken(JsonToken.FIELD_NAME, copyParser.nextToken());
        assertEquals("k", copyParser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, copyParser.nextToken());
        assertEquals(42, copyParser.getIntValue());
        assertToken(JsonToken.END_OBJECT, copyParser.nextToken());
        assertToken(JsonToken.END_ARRAY, copyParser.nextToken());
        assertNull(copyParser.nextToken());
    }

    @Test(timeout = 4000)
    public void testCopyCurrentStructureFieldNameFirst() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("x");
        buffer.writeString("y");
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        TokenBuffer copy = new TokenBuffer(null, false);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        copy.copyCurrentStructure(parser); // should copy field name and value

        JsonParser copyParser = copy.asParser();
        assertToken(JsonToken.FIELD_NAME, copyParser.nextToken());
        assertEquals("x", copyParser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, copyParser.nextToken());
        assertEquals("y", copyParser.getText());
        assertNull(copyParser.nextToken());
    }

    /* ============================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ============================================================ */

    @Test(timeout = 4000)
    public void testClosedState() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertFalse(buffer.isClosed());
        buffer.close();
        assertTrue(buffer.isClosed());
        // After close, asParser should still work? Actually close just sets flag.
        // Parser from closed buffer should still work.
        JsonParser parser = buffer.asParser();
        assertNull(parser.nextToken()); // empty buffer
    }

    @Test(timeout = 4000)
    public void testToStringEmpty() {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertEquals("[TokenBuffer: ]", buffer.toString());
    }

    @Test(timeout = 4000)
    public void testToStringWithTokens() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("a");
        buffer.writeNumber(1);
        buffer.writeEndObject();
        String str = buffer.toString();
        assertTrue(str.startsWith("[TokenBuffer: "));
        assertTrue(str.contains("START_OBJECT"));
        assertTrue(str.contains("FIELD_NAME(a)"));
        assertTrue(str.contains("VALUE_NUMBER_INT"));
        assertTrue(str.contains("END_OBJECT"));
        assertTrue(str.endsWith("]"));
    }

    @Test(timeout = 4000)
    public void testToStringTruncated() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        for (int i = 0; i < 150; i++) {
            buffer.writeNumber(i);
        }
        String str = buffer.toString();
        assertTrue(str.contains("... (truncated 50 entries)"));
    }

    @Test(timeout = 4000)
    public void testToStringWithNativeIds() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, true);
        buffer.writeStartObject();
        buffer.writeObjectId("oid");
        buffer.writeTypeId("tid");
        buffer.writeFieldName("x");
        buffer.writeNumber(1);
        buffer.writeEndObject();
        String str = buffer.toString();
        assertTrue(str.contains("[objectId=oid]"));
        assertTrue(str.contains("[typeId=tid]"));
    }

    @Test(timeout = 4000)
    public void testGetOutputContext() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertNotNull(buffer.getOutputContext());
        assertEquals(JsonWriteContext.createRootContext(null).toString(), buffer.getOutputContext().toString());
        buffer.writeStartObject();
        assertEquals("OBJECT", buffer.getOutputContext().getTypeDesc());
        buffer.writeEndObject();
        assertEquals("ROOT", buffer.getOutputContext().getTypeDesc());
    }

    @Test(timeout = 4000)
    public void testFeatureMask() {
        TokenBuffer buffer = new TokenBuffer(null, false);
        int defaultMask = JsonGenerator.Feature.collectDefaults();
        assertEquals(defaultMask, buffer.getFeatureMask());
        buffer.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertTrue(buffer.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        buffer.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(buffer.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        buffer.setFeatureMask(0);
        assertEquals(0, buffer.getFeatureMask());
    }

    @Test(timeout = 4000)
    public void testUseDefaultPrettyPrinter() {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertSame(buffer, buffer.useDefaultPrettyPrinter());
    }

    @Test(timeout = 4000)
    public void testSetCodec() {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertNull(buffer.getCodec());
        ObjectCodec codec = new ObjectCodec() {
            // minimal stub
        };
        buffer.setCodec(codec);
        assertSame(codec, buffer.getCodec());
    }

    @Test(timeout = 4000)
    public void testCanWriteBinaryNatively() {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertTrue(buffer.canWriteBinaryNatively());
    }

    @Test(timeout = 4000)
    public void testFlushDoesNothing() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.flush(); // should not throw
    }

    @Test(timeout = 4000)
    public void testVersion() {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertNotNull(buffer.version());
    }

    @Test(timeout = 4000)
    public void testForceUseOfBigDecimal() {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertFalse(buffer._forceBigDecimal);
        buffer.forceUseOfBigDecimal(true);
        assertTrue(buffer._forceBigDecimal);
    }

    @Test(timeout = 4000)
    public void testAsParserWithCodec() {
        TokenBuffer buffer = new TokenBuffer(null, false);
        JsonParser parser = buffer.asParser(null);
        assertNotNull(parser);
        assertNull(parser.getCodec());
    }

    @Test(timeout = 4000)
    public void testAsParserWithSource() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeNumber(1);
        JsonParser src = buffer.asParser();
        src.nextToken(); // move to VALUE_NUMBER_INT
        JsonParser parser2 = buffer.asParser(src);
        assertNotNull(parser2);
        assertEquals(src.getTokenLocation(), parser2.getTokenLocation());
    }

    @Test(timeout = 4000)
    public void testPeekNextToken() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartArray();
        buffer.writeNumber(1);
        buffer.writeEndArray();
        JsonParser parser = buffer.asParser();
        assertNull(parser.peekNextToken()); // before first token
        parser.nextToken(); // START_ARRAY
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.peekNextToken());
        parser.nextToken();
        assertEquals(JsonToken.END_ARRAY, parser.peekNextToken());
        parser.nextToken();
        assertNull(parser.peekNextToken());
    }

    @Test(timeout = 4000)
    public void testNextFieldName() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("x");
        buffer.writeNumber(1);
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        parser.nextToken(); // START_OBJECT
        assertEquals("x", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentName() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("old");
        buffer.writeString("val");
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.overrideCurrentName("new");
        assertEquals("new", parser.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testGetTextForNonString() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeNumber(42);
        JsonParser parser = buffer.asParser();
        parser.nextToken();
        assertEquals("42", parser.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextCharacters() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeString("hello");
        JsonParser parser = buffer.asParser();
        parser.nextToken();
        assertArrayEquals("hello".toCharArray(), parser.getTextCharacters());
        assertEquals(0, parser.getTextOffset());
        assertEquals(5, parser.getTextLength());
        assertFalse(parser.hasTextCharacters());
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueFromString() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeString("dGVzdA=="); // base64 for "test"
        JsonParser parser = buffer.asParser();
        parser.nextToken();
        byte[] expected = "test".getBytes("UTF-8");
        assertArrayEquals(expected, parser.getBinaryValue(Base64Variants.MIME));
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueFromEmbeddedBytes() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new byte[]{1,2,3});
        JsonParser parser = buffer.asParser();
        parser.nextToken();
        assertArrayEquals(new byte[]{1,2,3}, parser.getBinaryValue(Base64Variants.MIME));
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testGetBinaryValueFromNonString() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeNumber(1);
        JsonParser parser = buffer.asParser();
        parser.nextToken();
        parser.getBinaryValue(Base64Variants.MIME);
    }

    @Test(timeout = 4000)
    public void testReadBinaryValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeString("dGVzdA==");
        JsonParser parser = buffer.asParser();
        parser.nextToken();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int len = parser.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(4, len);
        assertArrayEquals("test".getBytes("UTF-8"), out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testGetNumberValueFromString() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeNumber("123.45");
        JsonParser parser = buffer.asParser();
        parser.nextToken();
        assertEquals(123.45, parser.getDoubleValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testGetNumberValueFromNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeNull();
        JsonParser parser = buffer.asParser();
        parser.nextToken();
        assertNull(parser.getNumberValue());
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testGetNumberValueFromNonNumeric() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeString("not a number");
        JsonParser parser = buffer.asParser();
        parser.nextToken();
        parser.getNumberValue();
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectReturnsNullForNonEmbedded() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeString("x");
        JsonParser parser = buffer.asParser();
        parser.nextToken();
        assertNull(parser.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testUnbalancedEndObject() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeEndObject(); // unbalanced
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        // After unbalanced end, context should be root
        assertEquals("ROOT", parser.getParsingContext().getTypeDesc());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testUnbalancedEndArray() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeEndArray();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals("ROOT", parser.getParsingContext().getTypeDesc());
    }

    @Test(timeout = 4000)
    public void testWriteStringCharArray() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeString(new char[]{'a','b','c'}, 0, 2);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("ab", parser.getText());
    }

    @Test(timeout = 4000)
    public void testWriteStringSerializableString() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeString(new SerializableString() {
            public String getValue() { return "ser"; }
            public int charLength() { return 3; }
            public char[] asQuotedChars() { return new char[]{'s','e','r'}; }
            public byte[] asUnquotedUTF8() { return new byte[]{'s','e','r'}; }
            public byte[] asQuotedUTF8() { return new byte[]{'s','e','r'}; }
        });
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("ser", parser.getText());
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithRawValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new RawValue("raw"));
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertEquals("raw", ((RawValue) parser.getEmbeddedObject()).toString());
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArray() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new byte[]{1,2,3});
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(new byte[]{1,2,3}, (byte[]) parser.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithCodecNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("someString");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertEquals("someString", parser.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testWriteTreeWithCodecNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeTree(new TextNode("tree"));
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertEquals("tree", ((TextNode) parser.getEmbeddedObject()).asText());
    }

    @Test(timeout = 4000)
    public void testAppendWithNativeIds() throws IOException {
        TokenBuffer buffer1 = new TokenBuffer(null, true);
        buffer1.writeStartObject();
        buffer1.writeObjectId("oid1");
        buffer1.writeFieldName("a");
        buffer1.writeNumber(1);
        buffer1.writeEndObject();

        TokenBuffer buffer2 = new TokenBuffer(null, false);
        buffer2.writeStartObject();
        buffer2.writeFieldName("b");
        buffer2.writeNumber(2);
        buffer2.writeEndObject();

        buffer1.append(buffer2);
        assertTrue(buffer1.canWriteObjectId());
        assertTrue(buffer1.canWriteTypeId());
        JsonParser parser = buffer1.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("oid1", parser.getObjectId());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithInvalidEndToken() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        JsonParser p = new JsonParser() {
            private int state = 0;
            public JsonToken nextToken() {
                switch (state++) {
                    case 0: return JsonToken.FIELD_NAME;
                    case 1: return JsonToken.VALUE_STRING;
                    case 2: return JsonToken.END_ARRAY; // unexpected
                    default: return null;
                }
            }
            public String getCurrentName() { return "x"; }
            public String getText() { return "y"; }
        };
        try {
            buffer.deserialize(p, null);
            fail("Expected exception");
        } catch (Exception e) {
            // expected
        }
    }

    // Helper method to assert token type
    private void assertToken(JsonToken expected, JsonToken actual) {
        assertEquals("Expected token " + expected, expected, actual);
    }
}