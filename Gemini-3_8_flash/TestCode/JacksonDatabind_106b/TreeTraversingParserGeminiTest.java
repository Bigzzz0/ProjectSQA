package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.node.TreeTraversingParser
 *
 * Decision / Branch Matrix:
 * 1. Constructor Branching:
 *    - ArrayNode input -> sets _nextToken = START_ARRAY, NodeCursor.ArrayCursor
 *    - ObjectNode input -> sets _nextToken = START_OBJECT, NodeCursor.ObjectCursor
 *    - ValueNode / other input -> sets NodeCursor.RootCursor
 * 2. nextToken() & Container Traversal:
 *    - _nextToken != null buffer consumption
 *    - _startContainer == true with empty container (skipped: returns END_OBJECT / END_ARRAY)
 *    - _startContainer == true with non-empty container (calls iterateChildren())
 *    - nested container triggers recursive _startContainer flag
 *    - _nodeCursor == null -> marks _closed = true and returns null
 *    - _nodeCursor.nextToken() == null -> returns endToken() and ascends to getParent()
 * 3. skipChildren() Branching:
 *    - _currToken == START_OBJECT -> sets _startContainer = false, _currToken = END_OBJECT
 *    - _currToken == START_ARRAY -> sets _startContainer = false, _currToken = END_ARRAY
 *    - Other token types -> no-op
 * 4. getText() & Text Accessors:
 *    - _closed == true -> returns null
 *    - _currToken == FIELD_NAME -> returns _nodeCursor.getCurrentName()
 *    - _currToken == VALUE_STRING -> returns currentNode().textValue()
 *    - _currToken == VALUE_NUMBER_INT / VALUE_NUMBER_FLOAT -> returns String.valueOf(numberValue)
 *    - _currToken == VALUE_EMBEDDED_OBJECT -> if binary, returns base64 asText(); else default asString()
 *    - default -> _currToken.asString() or null if _currToken is null
 *    - getTextCharacters(), getTextLength(), getTextOffset() (=0), hasTextCharacters() (=false)
 * 5. Numeric Accessors & currentNumericNode():
 *    - Non-numeric or null current node -> throws JsonParseException
 *    - getNumberType(), getBigIntegerValue(), getDecimalValue(), getDoubleValue(), getFloatValue(), getNumberValue()
 *    - isNaN() for DoubleNode / FloatNode with NaN vs other nodes
 * 6. Defects4J Known Defect Zone:
 *    - getIntValue() on BigInteger/Long values exceeding Integer.MAX_VALUE / Integer.MIN_VALUE
 *      must fail with JsonParseException on long/int overflow instead of silently truncating.
 *    - getLongValue() on BigInteger/BigDecimal values exceeding Long.MAX_VALUE / Long.MIN_VALUE
 *      must fail with JsonParseException on long overflow instead of silently truncating.
 * 7. Embedded & Binary Handling:
 *    - getEmbeddedObject() for POJONode, BinaryNode, and other types
 *    - getBinaryValue(b64variant) on TextNode (decoded via Base64Variant) vs BinaryNode vs null
 *    - readBinaryValue(b64variant, out) writing decoded bytes to stream
 * 8. Context, Location & Metadata:
 *    - getCurrentName(), overrideCurrentName(), getParsingContext(), getTokenLocation(), getCurrentLocation(), version()
 *    - setCodec() / getCodec()
 *    - close() idempotent behavior
 */
public class TreeTraversingParserGeminiTest {

    private final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testObjectTraversalSequence() throws IOException {
        ObjectNode root = nodeFactory.objectNode();
        root.put("name", "Alice");
        root.put("age", 30);

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertFalse(parser.isClosed());
        assertNull(parser.getCurrentToken());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals("name", parser.getText());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Alice", parser.getText());
        assertEquals("Alice", new String(parser.getTextCharacters()));
        assertEquals(5, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertFalse(parser.hasTextCharacters());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("age", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(30, parser.getIntValue());
        assertEquals(30L, parser.getLongValue());
        assertEquals(30.0, parser.getDoubleValue(), 0.0001);
        assertEquals(30.0f, parser.getFloatValue(), 0.0001f);
        assertEquals(BigInteger.valueOf(30), parser.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(30), parser.getDecimalValue());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
        assertEquals("30", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testArrayTraversalSequence() throws IOException {
        ArrayNode root = nodeFactory.arrayNode();
        root.add(true);
        root.add(false);
        root.addNull();

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getText());

        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getText());

        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("null", parser.getText());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testRootValueNodeTraversal() throws IOException {
        TextNode textNode = nodeFactory.textNode("SingleValue");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);

        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("SingleValue", parser.getText());

        assertNull(parser.nextToken());
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testNestedContainersWithIterateChildren() throws IOException {
        ObjectNode root = nodeFactory.objectNode();
        ArrayNode arr = root.putArray("items");
        ObjectNode childObj = arr.addObject();
        childObj.put("key", "val");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("items", parser.getCurrentName());

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("val", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & State Flags
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyObjectAndArrayOptimization() throws IOException {
        ObjectNode root = nodeFactory.objectNode();
        root.putObject("emptyObj");
        root.putArray("emptyArr");

        TreeTraversingParser parser = new TreeTraversingParser(root);
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
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnObjectAndArray() throws IOException {
        ObjectNode root = nodeFactory.objectNode();
        ObjectNode subObj = root.putObject("subObj");
        subObj.put("k1", "v1");
        subObj.put("k2", "v2");
        ArrayNode subArr = root.putArray("subArr");
        subArr.add(1);
        subArr.add(2);

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("subObj", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // skip children on Object
        assertSame(parser, parser.skipChildren());
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("subArr", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        // skip children on Array
        assertSame(parser, parser.skipChildren());
        assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnValueNode() throws IOException {
        TextNode textNode = nodeFactory.textNode("value");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        assertSame(parser, parser.skipChildren());
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testExplicitCloseAndIdempotence() throws IOException {
        ObjectNode root = nodeFactory.objectNode();
        root.put("a", 1);
        TreeTraversingParser parser = new TreeTraversingParser(root);

        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        assertNull(parser.nextToken());
        assertNull(parser.getText());
        assertNull(parser.getCurrentToken());
        assertNull(parser.getCurrentName());

        // Second close call must be completely safe
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentNameAndContext() throws IOException {
        ObjectNode root = nodeFactory.objectNode();
        root.put("original", "val");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("original", parser.getCurrentName());

        parser.overrideCurrentName("modified");
        assertEquals("modified", parser.getCurrentName());
        assertNotNull(parser.getParsingContext());
        assertEquals(JsonLocation.NA, parser.getTokenLocation());
        assertEquals(JsonLocation.NA, parser.getCurrentLocation());
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentNameWhenCursorNull() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.textNode("test"));
        parser.close();
        // Cursor is null now; overrideCurrentName should not fail
        parser.overrideCurrentName("ignored");
        assertNull(parser.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testCodecAndVersion() {
        ObjectCodec mapper = new ObjectMapper();
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.nullNode(), mapper);
        assertSame(mapper, parser.getCodec());

        parser.setCodec(null);
        assertNull(parser.getCodec());

        Version v = parser.version();
        assertNotNull(v);
        assertFalse(v.isUnknownVersion());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug: getIntValue() must throw JsonParseException on overflow
     * when value exceeds Integer.MAX_VALUE.
     */
    @Test(timeout = 4000)
    public void testNumberOverflowInt() throws IOException {
        BigInteger hugeValue = BigInteger.valueOf(Long.MAX_VALUE);
        BigIntegerNode node = new BigIntegerNode(hugeValue);
        TreeTraversingParser parser = new TreeTraversingParser(node);

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        try {
            parser.getIntValue();
            fail("Expected failure for `int` overflow");
        } catch (JsonParseException e) {
            String msg = e.getMessage();
            assertTrue("Exception message should indicate overflow/numeric issue, got: " + msg,
                    msg != null && (msg.contains("Numeric") || msg.contains("overflow") || msg.contains("out of range")));
        }
    }

    /**
     * Targets Defects4J bug: getLongValue() must throw JsonParseException on overflow
     * when value exceeds Long.MAX_VALUE.
     */
    @Test(timeout = 4000)
    public void testNumberOverflowLong() throws IOException {
        BigInteger hugeValue = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN);
        BigIntegerNode node = new BigIntegerNode(hugeValue);
        TreeTraversingParser parser = new TreeTraversingParser(node);

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        try {
            parser.getLongValue();
            fail("Expected failure for `long` overflow");
        } catch (JsonParseException e) {
            String msg = e.getMessage();
            assertTrue("Exception message should indicate overflow/numeric issue, got: " + msg,
                    msg != null && (msg.contains("Numeric") || msg.contains("overflow") || msg.contains("out of range")));
        }
    }

    // =========================================================================
    // Partition D: Numeric Edge Cases & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsNaNAndFloatingPointNumbers() throws IOException {
        DoubleNode nanNode = nodeFactory.numberNode(Double.NaN);
        TreeTraversingParser parser = new TreeTraversingParser(nanNode);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(parser.isNaN());
        assertEquals(Double.NaN, parser.getDoubleValue(), 0.0);
        assertTrue(Float.isNaN(parser.getFloatValue()));

        DoubleNode normalDouble = nodeFactory.numberNode(123.456);
        TreeTraversingParser normalParser = new TreeTraversingParser(normalDouble);
        normalParser.nextToken();
        assertFalse(normalParser.isNaN());
        assertEquals(123.456, normalParser.getDoubleValue(), 0.0001);
        assertEquals(123.456f, normalParser.getFloatValue(), 0.0001f);
        assertEquals(JsonParser.NumberType.DOUBLE, normalParser.getNumberType());
        assertEquals(123.456, normalParser.getNumberValue().doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testIsNaNWhenClosedOrNonNumeric() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.textNode("notNumber"));
        parser.nextToken();
        assertFalse(parser.isNaN());
        parser.close();
        assertFalse(parser.isNaN());
    }

    @Test(timeout = 4000)
    public void testCurrentNumericNodeThrowsOnNonNumericToken() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.textNode("hello"));
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        try {
            parser.getIntValue();
            fail("Expected JsonParseException because current token is not numeric");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("not numeric"));
        }
    }

    @Test(timeout = 4000)
    public void testCurrentNumericNodeThrowsWhenNodeIsNull() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.objectNode());
        // Before nextToken(), cursor has no current node yet
        try {
            parser.getIntValue();
            fail("Expected JsonParseException because currentNode() is null");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("not numeric"));
        }
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeWhenNoNumericNode() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.textNode("abc"));
        // before nextToken() -> currentNode() is null -> currentNumericNode throws
        try {
            parser.getNumberType();
            fail("Expected JsonParseException for non-numeric node");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("not numeric"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleEOFThrowsInternalError() {
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.nullNode());
        try {
            parser._handleEOF();
            fail("Expected RuntimeException/InternalError from _handleEOF");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException || e instanceof JsonParseException);
            assertTrue(e.getMessage().contains("Internal error"));
        }
    }

    // =========================================================================
    // Partition E: Embedded Objects & Binary Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testPOJONodeAndEmbeddedObject() throws IOException {
        Object customObject = new Object();
        POJONode pojoNode = new POJONode(customObject);

        TreeTraversingParser parser = new TreeTraversingParser(pojoNode);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(customObject, parser.getEmbeddedObject());

        parser.close();
        assertNull(parser.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testBinaryNodeAccess() throws IOException {
        byte[] original = new byte[]{1, 2, 3, 4, 5, 127};
        BinaryNode binaryNode = new BinaryNode(original);

        TreeTraversingParser parser = new TreeTraversingParser(binaryNode);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());

        // getEmbeddedObject on BinaryNode returns byte[]
        Object embedded = parser.getEmbeddedObject();
        assertNotNull(embedded);
        assertArrayEquals(original, (byte[]) embedded);

        // getText on binary node converts to base64
        String text = parser.getText();
        assertNotNull(text);
        assertFalse(text.isEmpty());

        // getBinaryValue directly
        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(original, binary);

        // readBinaryValue to OutputStream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = parser.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(original.length, bytesRead);
        assertArrayEquals(original, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testTextNodeToBinaryCoercion() throws IOException {
        byte[] raw = "Test binary string".getBytes("UTF-8");
        String base64 = Base64Variants.MIME.encode(raw);
        TextNode textNode = nodeFactory.textNode(base64);

        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] decoded = parser.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(raw, decoded);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = parser.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(raw.length, count);
        assertArrayEquals(raw, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testReadBinaryValueWhenNull() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.nullNode());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = parser.readBinaryValue(out);
        assertEquals(0, count);
        assertEquals(0, out.size());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectOnPlainNode() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(nodeFactory.textNode("regular"));
        parser.nextToken();
        assertNull(parser.getEmbeddedObject());
    }
}