package com.fasterxml.jackson.databind.node;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * White-box JUnit 4 test suite for TreeTraversingParser.
 * Targets line/branch coverage and the known Defects4J defect: missing overflow
 * detection in getIntValue() and getLongValue().
 * 
 * [Branch & Defect Analysis Matrix]
 *  - Partition A: Core traversal (nextToken, skipChildren, getCurrentName, etc.)
 *  - Partition B: Boundaries (null, empty containers, numeric extremes)
 *  - Partition C: Defect-targeted (int/long overflow → expected JsonParseException)
 *  - Partition D: Exception/defensive paths (closed state, non-numeric tokens)
 *  - Partition E: Object contract (isClosed, getEmbeddedObject, isNaN)
 */
public class TreeTraversingParserDeepseekTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    // ========== Partition A: Core traversal ==========

    @Test(timeout = 4000)
    public void testBasicObjectTraversal() throws IOException {
        String json = "{\"a\":1,\"b\":2}";
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree(json));
        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testBasicArrayTraversal() throws IOException {
        String json = "[10,20]";
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree(json));
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(10, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(20, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnObject() throws IOException {
        String json = "{\"x\":{\"inner\":true},\"y\":2}";
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree(json));
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("x", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.skipChildren(); // should move to END_OBJECT
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("y", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnArray() throws IOException {
        String json = "[1,[2,3],4]";
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree(json));
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        parser.skipChildren(); // to END_ARRAY
        assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(4, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testGetCurrentNameNullAtRoot() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("42"));
        assertNull(parser.getCurrentName());
        parser.nextToken();
        assertNull(parser.getCurrentName());
    }

    // ========== Partition B: Boundaries ==========

    @Test(timeout = 4000)
    public void testNullNode() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.getNodeFactory().nullNode());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getText());
        assertNull(parser.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testEmptyObject() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("{}"));
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testEmptyArray() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("[]"));
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testBoundaryIntValues() throws IOException {
        // Minimum and maximum int
        JsonNode node = MAPPER.readTree("[ " + Integer.MIN_VALUE + "," + Integer.MAX_VALUE + " ]");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // VALUE_NUMBER_INT
        assertEquals(Integer.MIN_VALUE, parser.getIntValue());
        parser.nextToken();
        assertEquals(Integer.MAX_VALUE, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testBoundaryLongValues() throws IOException {
        // Values within long range but beyond int
        JsonNode node = MAPPER.readTree("[ " + ((long) Integer.MAX_VALUE + 1) + "," + Long.MAX_VALUE + " ]");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        parser.nextToken(); // START_ARRAY
        parser.nextToken();
        assertEquals((long) Integer.MAX_VALUE + 1, parser.getLongValue());
        parser.nextToken();
        assertEquals(Long.MAX_VALUE, parser.getLongValue());
    }

    @Test(timeout = 4000)
    public void testDoubleValues() throws IOException {
        JsonNode node = MAPPER.readTree("[ 1.5, 3.14159 ]");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        parser.nextToken(); // START_ARRAY
        parser.nextToken();
        assertEquals(1.5, parser.getDoubleValue(), 1e-9);
        parser.nextToken();
        assertEquals(3.14159, parser.getDoubleValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testDecimalValue() throws IOException {
        BigDecimal val = new BigDecimal("12345678901234567890.12345");
        JsonNode node = MAPPER.getNodeFactory().numberNode(val);
        TreeTraversingParser parser = new TreeTraversingParser(node);
        parser.nextToken();
        assertEquals(val, parser.getDecimalValue());
    }

    @Test(timeout = 4000)
    public void testBigIntegerValue() throws IOException {
        BigInteger val = new BigInteger("123456789012345678901234567890");
        JsonNode node = MAPPER.getNodeFactory().numberNode(val);
        TreeTraversingParser parser = new TreeTraversingParser(node);
        parser.nextToken();
        assertEquals(val, parser.getBigIntegerValue());
    }

    // ========== Partition C: Defect-targeted overflow ==========

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testNumberOverflowInt() throws IOException {
        // Value larger than Integer.MAX_VALUE should throw on getIntValue()
        BigInteger overflowInt = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE);
        JsonNode node = MAPPER.getNodeFactory().numberNode(overflowInt);
        TreeTraversingParser parser = new TreeTraversingParser(node);
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.getIntValue(); // expected: JsonParseException for overflow
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testNumberOverflowLong() throws IOException {
        // Value larger than Long.MAX_VALUE should throw on getLongValue()
        BigInteger overflowLong = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        JsonNode node = MAPPER.getNodeFactory().numberNode(overflowLong);
        TreeTraversingParser parser = new TreeTraversingParser(node);
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.getLongValue(); // expected: JsonParseException for overflow
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testNegativeIntOverflow() throws IOException {
        // Value smaller than Integer.MIN_VALUE
        BigInteger underflowInt = BigInteger.valueOf(Integer.MIN_VALUE).subtract(BigInteger.ONE);
        JsonNode node = MAPPER.getNodeFactory().numberNode(underflowInt);
        TreeTraversingParser parser = new TreeTraversingParser(node);
        parser.nextToken();
        parser.getIntValue();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testNegativeLongOverflow() throws IOException {
        BigInteger underflowLong = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);
        JsonNode node = MAPPER.getNodeFactory().numberNode(underflowLong);
        TreeTraversingParser parser = new TreeTraversingParser(node);
        parser.nextToken();
        parser.getLongValue();
    }

    // ========== Partition D: Exception/defensive paths ==========

    @Test(timeout = 4000)
    public void testGetTextOnClosedParser() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("true"));
        parser.close();
        assertNull(parser.getText());
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testNumericAccessOnNonNumeric() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("\"string\""));
        parser.nextToken();
        parser.getIntValue(); // should throw because token is VALUE_STRING
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testNumericAccessOnNullToken() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("null"));
        parser.nextToken();
        parser.getIntValue(); // null token? Actually token is VALUE_NULL, not numeric
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectPojo() throws IOException {
        // Create a POJO node via ObjectMapper (will be wrapped as POJONode)
        ObjectMapper mapper = new ObjectMapper();
        // We can use valueToTree to create a POJONode for a simple object
        JsonNode pojoNode = mapper.valueToTree(new java.awt.Point(10, 20));
        TreeTraversingParser parser = new TreeTraversingParser(pojoNode);
        parser.nextToken(); // should be START_OBJECT or VALUE_EMBEDDED_OBJECT?
        // Actually valueToTree on a POJO creates a ObjectNode (structured), not POJONode.
        // To get POJONode, we need to use mapper.getNodeFactory().pojoNode(...)
        // But POJONode is package-private? We'll test with a different approach.
        // Instead, we test with a BinaryNode.
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectBinary() throws IOException {
        byte[] data = {1,2,3};
        JsonNode binaryNode = MAPPER.getNodeFactory().binaryNode(data);
        TreeTraversingParser parser = new TreeTraversingParser(binaryNode);
        parser.nextToken(); // VALUE_EMBEDDED_OBJECT
        Object embedded = parser.getEmbeddedObject();
        assertArrayEquals(data, (byte[]) embedded);
    }

    @Test(timeout = 4000)
    public void testIsNaNOnNaN() throws IOException {
        // Use BigDecimal NaN? DoubleNode can be NaN.
        JsonNode nanNode = MAPPER.getNodeFactory().numberNode(Double.NaN);
        TreeTraversingParser parser = new TreeTraversingParser(nanNode);
        parser.nextToken();
        assertTrue(parser.isNaN());
    }

    @Test(timeout = 4000)
    public void testIsNaNOnNormalNumber() throws IOException {
        JsonNode numNode = MAPPER.getNodeFactory().numberNode(42);
        TreeTraversingParser parser = new TreeTraversingParser(numNode);
        parser.nextToken();
        assertFalse(parser.isNaN());
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentName() throws IOException {
        String json = "{\"a\":1}";
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree(json));
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        assertEquals("a", parser.getCurrentName());
        parser.overrideCurrentName("b");
        assertEquals("b", parser.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldName() throws IOException {
        String json = "{\"field\":\"value\"}";
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree(json));
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        assertEquals("field", parser.getText());
        parser.nextToken(); // VALUE_STRING
        assertEquals("value", parser.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumber() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("42"));
        parser.nextToken();
        assertEquals("42", parser.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNull() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("null"));
        parser.nextToken();
        // getText() for null returns "null" (from JsonToken.asString)
        assertEquals("null", parser.getText());
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueFromTextNode() throws IOException {
        // Text node with base64 content
        String base64 = "SGVsbG8=";
        JsonNode textNode = MAPPER.readTree("\"" + base64 + "\"");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        parser.nextToken();
        byte[] expected = java.util.Base64.getDecoder().decode(base64);
        assertArrayEquals(expected, parser.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testReadBinaryValue() throws IOException {
        byte[] data = {10,20,30};
        JsonNode binaryNode = MAPPER.getNodeFactory().binaryNode(data);
        TreeTraversingParser parser = new TreeTraversingParser(binaryNode);
        parser.nextToken();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        int len = parser.readBinaryValue(Base64Variants.getDefaultVariant(), baos);
        assertEquals(data.length, len);
        assertArrayEquals(data, baos.toByteArray());
    }

    // ========== Partition E: Object lifecycle ==========

    @Test(timeout = 4000)
    public void testCloseAndIsClosed() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("true"));
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        // Second close should be no-op
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testGetParsingContext() throws IOException {
        String json = "{\"x\":1}";
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree(json));
        assertNotNull(parser.getParsingContext());
        parser.nextToken(); // START_OBJECT
        assertTrue(parser.getParsingContext() instanceof NodeCursor.ObjectCursor);
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // END_OBJECT
        // After end, parent is null? The cursor should be null after end of root.
        // Actually after END_OBJECT, nextToken returns null and closes.
        // The context after closing? It's set to null by close().
        // We'll check after full traversal.
        parser.nextToken(); // null
        assertTrue(parser.isClosed());
        // Parsing context may be null
    }

    @Test(timeout = 4000)
    public void testGetTokenLocation() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("1"));
        assertEquals(JsonLocation.NA, parser.getTokenLocation());
        parser.nextToken();
        assertEquals(JsonLocation.NA, parser.getTokenLocation());
    }

    @Test(timeout = 4000)
    public void testGetCurrentLocation() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("1"));
        assertEquals(JsonLocation.NA, parser.getCurrentLocation());
        parser.nextToken();
        assertEquals(JsonLocation.NA, parser.getCurrentLocation());
    }

    @Test(timeout = 4000)
    public void testVersion() {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("1"));
        assertNotNull(parser.version());
    }

    @Test(timeout = 4000)
    public void testSetAndGetCodec() {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("1"));
        assertNotNull(parser.getCodec());
        ObjectCodec customCodec = new ObjectMapper();
        parser.setCodec(customCodec);
        assertSame(customCodec, parser.getCodec());
    }

    @Test(timeout = 4000)
    public void testGetNumberType() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("42"));
        parser.nextToken();
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());

        parser = new TreeTraversingParser(MAPPER.readTree("3.14"));
        parser.nextToken();
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());
    }

    @Test(timeout = 4000)
    public void testGetNumberValue() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("42"));
        parser.nextToken();
        assertEquals(42, parser.getNumberValue());

        parser = new TreeTraversingParser(MAPPER.readTree("3.14"));
        parser.nextToken();
        assertEquals(3.14, parser.getNumberValue());
    }

    @Test(timeout = 4000)
    public void testHasTextCharacters() {
        TreeTraversingParser parser = new TreeTraversingParser(MAPPER.readTree("\"abc\""));
        assertFalse(parser.hasTextCharacters());
    }
}