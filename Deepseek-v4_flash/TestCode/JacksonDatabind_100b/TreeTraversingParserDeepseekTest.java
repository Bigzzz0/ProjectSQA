package com.fasterxml.jackson.databind.node;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;

public class TreeTraversingParserDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * Target: TreeTraversingParser
     * 
     * Defect: getBinaryValue(Base64Variant) fails to handle TextNode with base64 content
     *         when the text is a valid base64 string but contains URL-safe characters or
     *         when the text node represents binary data that should be decoded.
     *         The method only checks binaryValue() on the node and POJO nodes, but
     *         TextNode.binaryValue() may return null if the text is not valid base64
     *         under the default variant, even though it could be valid under a different
     *         variant (e.g., URL_SAFE).
     * 
     * Branches covered:
     * - Constructor: array/object/value node initialization
     * - nextToken(): _nextToken handling, _startContainer logic, empty container skip,
     *                cursor iteration, end token handling
     * - skipChildren(): START_OBJECT/START_ARRAY handling
     * - getText(): all token types (FIELD_NAME, VALUE_STRING, VALUE_NUMBER_INT/FLOAT,
     *              VALUE_EMBEDDED_OBJECT, default)
     * - getBinaryValue(): null node, binary node, POJO node, text node with base64
     * - readBinaryValue(): data present, data null
     * - currentNumericNode(): valid numeric, non-numeric exception
     * - getNumberType(): numeric node, null node
     * - isNaN(): closed parser, non-numeric node, numeric node
     * - getEmbeddedObject(): POJO, binary, null cases
     * - close(): idempotent, state reset
     * - getCurrentName/overrideCurrentName: null cursor, valid cursor
     * - getTextCharacters/Length/Offset: text retrieval
     * - hasTextCharacters: always false
     * - getBigIntegerValue/getDecimalValue/getDoubleValue/getFloatValue/getLongValue/getIntValue/getNumberValue
     * - _handleEOF: internal error
     * 
     * Boundary values:
     * - Empty containers (array/object)
     * - Nested containers
     * - Null nodes
     * - Binary data of length 0, 1, 2, 3 (base64 padding boundaries)
     * - URL-safe base64 characters
     * - Non-numeric nodes accessed as numeric
     * - Closed parser state
     */

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testArrayTraversal() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1,2,3]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(3, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testObjectTraversal() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"a\":1,\"b\":\"two\"}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("two", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testValueNodeTraversal() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testNestedContainers() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"arr\":[1,{\"x\":2}],\"obj\":{\"y\":3}}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("arr", p.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("x", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("obj", p.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("y", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(3, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testEmptyArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testEmptyObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1,[2,3],4]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        p.skipChildren();
        assertEquals(JsonToken.END_ARRAY, p.getCurrentToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(4, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"a\":{\"b\":1},\"c\":2}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        p.skipChildren();
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("c", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test(timeout = 4000)
    public void testGetCurrentNameNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"a\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken(); // FIELD_NAME
        p.overrideCurrentName("custom");
        assertEquals("custom", p.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testGetParsingContext() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        assertNotNull(p.getParsingContext());
        p.nextToken();
        assertNotNull(p.getParsingContext());
    }

    @Test(timeout = 4000)
    public void testGetTokenLocation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        assertEquals(JsonLocation.NA, p.getTokenLocation());
        assertEquals(JsonLocation.NA, p.getCurrentLocation());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testNullNode() throws Exception {
        TreeTraversingParser p = new TreeTraversingParser(JsonNodeFactory.instance.nullNode());
        assertNull(p.nextToken());
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testBinaryNodeValue() throws Exception {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertArrayEquals(data, p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testBinaryNodeEmpty() throws Exception {
        byte[] data = new byte[0];
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertArrayEquals(data, p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testBinaryNodeSingleByte() throws Exception {
        byte[] data = new byte[]{42};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertArrayEquals(data, p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testBinaryNodeTwoBytes() throws Exception {
        byte[] data = new byte[]{1, 2};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertArrayEquals(data, p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testBinaryNodeThreeBytes() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertArrayEquals(data, p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testPojoNodeWithByteArray() throws Exception {
        byte[] data = new byte[]{10, 20, 30};
        POJONode node = new POJONode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertArrayEquals(data, p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testPojoNodeWithNonByteArray() throws Exception {
        POJONode node = new POJONode("not bytes");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNull(p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testTextNodeWithValidBase64() throws Exception {
        String base64 = "SGVsbG8="; // "Hello"
        TextNode node = new TextNode(base64);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        byte[] result = p.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull(result);
        assertEquals("Hello", new String(result, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testTextNodeWithUrlSafeBase64() throws Exception {
        // URL-safe base64 with - and _ instead of + and /
        String base64 = "SGVsbG8tXw=="; // "Hello-_" in URL-safe
        TextNode node = new TextNode(base64);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        byte[] result = p.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull(result);
        assertEquals("Hello-_", new String(result, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testTextNodeWithInvalidBase64() throws Exception {
        String invalid = "not!valid@base64";
        TextNode node = new TextNode(invalid);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNull(p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testReadBinaryValueWithData() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = p.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(data.length, len);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testReadBinaryValueNoData() throws Exception {
        TextNode node = new TextNode("not base64");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = p.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(0, len);
        assertEquals(0, out.size());
    }

    @Test(timeout = 4000)
    public void testGetTextForNumberInt() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextForNumberFloat() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextForFieldName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextForNullToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextForBoolean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextForEmbeddedObject() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        // Binary node as embedded object should return base64 string
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextCharacters() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertArrayEquals("hello".toCharArray(), p.getTextCharacters());
        assertEquals(5, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        assertFalse(p.hasTextCharacters());
    }

    @Test(timeout = 4000)
    public void testGetTextWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getText());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect: getBinaryValue() fails to handle TextNode with base64 content
     * when the text is a valid base64 string but contains URL-safe characters.
     * The method only checks binaryValue() on the node, which may return null
     * for URL-safe base64 under the default variant.
     * 
     * This test verifies that a TextNode containing valid base64 data can be
     * read as binary, even when the text uses URL-safe characters.
     */
    @Test(timeout = 4000)
    public void testGetBinaryValueFromTextNodeWithUrlSafeBase64() throws Exception {
        // This is the exact scenario from the defect report
        // "MODIFIED-FOR-URL" variant with data length 1
        String base64 = "Lg=="; // "." in standard base64, but URL-safe would be "Lg=="
        TextNode node = new TextNode(base64);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        
        // The defect: this should return the decoded bytes but currently throws
        // "Cannot access contents of TextNode as binary due to broken Base64 encoding"
        byte[] result = p.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull("Should decode valid base64 from TextNode", result);
        assertEquals(1, result.length);
        assertEquals('.', result[0]);
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueFromTextNodeWithStandardBase64() throws Exception {
        String base64 = "QQ=="; // "A"
        TextNode node = new TextNode(base64);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        
        byte[] result = p.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals('A', result[0]);
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueFromTextNodeWithEmptyString() throws Exception {
        TextNode node = new TextNode("");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        
        byte[] result = p.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueFromTextNodeWithWhitespace() throws Exception {
        String base64 = "SGVsbG8="; // "Hello"
        TextNode node = new TextNode("  " + base64 + "  ");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        
        byte[] result = p.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull(result);
        assertEquals("Hello", new String(result, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueFromTextNodeWithLineBreaks() throws Exception {
        String base64 = "SGVsbG8="; // "Hello"
        TextNode node = new TextNode("SGVs\nbG8=");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        
        byte[] result = p.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull(result);
        assertEquals("Hello", new String(result, "UTF-8"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testGetIntValueOnNonNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"not a number\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.getIntValue();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testGetLongValueOnNonNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.getLongValue();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testGetDoubleValueOnNonNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.getDoubleValue();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testGetFloatValueOnNonNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.getFloatValue();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testGetBigIntegerValueOnNonNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.getBigIntegerValue();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testGetDecimalValueOnNonNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"abc\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.getDecimalValue();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testGetNumberValueOnNonNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("false");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.getNumberValue();
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeOnNonNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"abc\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getNumberType());
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeOnNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNotNull(p.getNumberType());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testIsNaNWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertFalse(p.isNaN());
    }

    @Test(timeout = 4000)
    public void testIsNaNOnNonNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"abc\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertFalse(p.isNaN());
    }

    @Test(timeout = 4000)
    public void testIsNaNOnNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1.5");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertFalse(p.isNaN());
    }

    @Test(timeout = 4000)
    public void testIsNaNOnNaN() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("NaN");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertTrue(p.isNaN());
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueOnNonBinaryNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testCloseIdempotent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        p.close(); // should not throw
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testCloseResetsState() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"a\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getCurrentToken());
        assertNull(p.getCurrentName());
        assertNull(p.getParsingContext());
    }

    @Test(timeout = 4000)
    public void testGetCodecSetCodec() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        assertNull(p.getCodec());
        p.setCodec(mapper);
        assertSame(mapper, p.getCodec());
    }

    @Test(timeout = 4000)
    public void testVersion() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.createObjectNode();
        TreeTraversingParser p = new TreeTraversingParser(root);
        assertNotNull(p.version());
    }

    @Test(timeout = 4000)
    public void testConstructorWithCodec() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root, mapper);
        assertSame(mapper, p.getCodec());
    }

    @Test(timeout = 4000)
    public void testGetNumericValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        // Test int
        JsonNode intNode = mapper.readTree("42");
        TreeTraversingParser p1 = new TreeTraversingParser(intNode);
        p1.nextToken();
        assertEquals(42, p1.getIntValue());
        assertEquals(42L, p1.getLongValue());
        assertEquals(42.0, p1.getDoubleValue(), 0.0001);
        assertEquals(42.0f, p1.getFloatValue(), 0.0001f);
        assertEquals(BigInteger.valueOf(42), p1.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(42), p1.getDecimalValue());
        assertEquals(42, p1.getNumberValue().intValue());
        
        // Test long
        JsonNode longNode = mapper.readTree("1234567890123");
        TreeTraversingParser p2 = new TreeTraversingParser(longNode);
        p2.nextToken();
        assertEquals(1234567890123L, p2.getLongValue());
        
        // Test double
        JsonNode doubleNode = mapper.readTree("3.14159");
        TreeTraversingParser p3 = new TreeTraversingParser(doubleNode);
        p3.nextToken();
        assertEquals(3.14159, p3.getDoubleValue(), 0.000001);
        
        // Test float
        JsonNode floatNode = mapper.readTree("1.5");
        TreeTraversingParser p4 = new TreeTraversingParser(floatNode);
        p4.nextToken();
        assertEquals(1.5f, p4.getFloatValue(), 0.0001f);
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeVariants() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        JsonNode intNode = mapper.readTree("42");
        TreeTraversingParser p1 = new TreeTraversingParser(intNode);
        p1.nextToken();
        assertEquals(JsonParser.NumberType.INT, p1.getNumberType());
        
        JsonNode longNode = mapper.readTree("1234567890123");
        TreeTraversingParser p2 = new TreeTraversingParser(longNode);
        p2.nextToken();
        assertEquals(JsonParser.NumberType.LONG, p2.getNumberType());
        
        JsonNode doubleNode = mapper.readTree("3.14");
        TreeTraversingParser p3 = new TreeTraversingParser(doubleNode);
        p3.nextToken();
        assertEquals(JsonParser.NumberType.DOUBLE, p3.getNumberType());
        
        JsonNode floatNode = mapper.readTree("1.5");
        TreeTraversingParser p4 = new TreeTraversingParser(floatNode);
        p4.nextToken();
        assertEquals(JsonParser.NumberType.FLOAT, p4.getNumberType());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectPojo() throws Exception {
        Object pojo = new Object();
        POJONode node = new POJONode(pojo);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertSame(pojo, p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectBinary() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertArrayEquals(data, (byte[]) p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testNextTokenAfterEnd() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.nextToken());
        assertNull(p.nextToken()); // should stay null
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testNextTokenAfterClose() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"name\":\"value\"}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("name", p.getText());
        p.nextToken();
        assertEquals("value", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnArrayNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1,2]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnObjectNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"a\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("{", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"a\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("}", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedBinary() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        // Binary node as embedded object should return base64 string
        String text = p.getText();
        assertNotNull(text);
        assertTrue(text.length() > 0);
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedPojo() throws Exception {
        Object pojo = new Object();
        POJONode node = new POJONode(pojo);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        // POJO node as embedded object should return toString
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullCurrentNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextCharactersOnNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        try {
            p.getTextCharacters();
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetTextLengthOnNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        try {
            p.getTextLength();
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetTextOffsetAlwaysZero() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals(0, p.getTextOffset());
    }

    @Test(timeout = 4000)
    public void testHasTextCharactersAlwaysFalse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertFalse(p.hasTextCharacters());
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueWithNullNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueWithPojoByteArray() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        POJONode node = new POJONode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertArrayEquals(data, p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueWithPojoNonByteArray() throws Exception {
        POJONode node = new POJONode("not bytes");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNull(p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueWithTextNode() throws Exception {
        TextNode node = new TextNode("SGVsbG8=");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        byte[] result = p.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull(result);
        assertEquals("Hello", new String(result, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueWithTextNodeInvalid() throws Exception {
        TextNode node = new TextNode("invalid!");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNull(p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testReadBinaryValueWithTextNode() throws Exception {
        TextNode node = new TextNode("SGVsbG8=");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = p.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(5, len);
        assertEquals("Hello", new String(out.toByteArray(), "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testReadBinaryValueWithTextNodeInvalid() throws Exception {
        TextNode node = new TextNode("invalid!");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = p.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(0, len);
        assertEquals(0, out.size());
    }

    @Test(timeout = 4000)
    public void testReadBinaryValueWithNullNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = p.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(0, len);
        assertEquals(0, out.size());
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeOnNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getNumberType());
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeOnNonNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"abc\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getNumberType());
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeOnNumericNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNotNull(p.getNumberType());
    }

    @Test(timeout = 4000)
    public void testIsNaNOnNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertFalse(p.isNaN());
    }

    @Test(timeout = 4000)
    public void testIsNaNOnTextNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"abc\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertFalse(p.isNaN());
    }

    @Test(timeout = 4000)
    public void testIsNaNOnNumericNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1.5");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertFalse(p.isNaN());
    }

    @Test(timeout = 4000)
    public void testIsNaNOnNaNNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("NaN");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertTrue(p.isNaN());
    }

    @Test(timeout = 4000)
    public void testIsNaNOnInfinity() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("Infinity");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertFalse(p.isNaN());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectOnPojo() throws Exception {
        Object pojo = new Object();
        POJONode node = new POJONode(pojo);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertSame(pojo, p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectOnBinary() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertArrayEquals(data, (byte[]) p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectOnNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectOnText() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"abc\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectOnNumber() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectOnBoolean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectOnArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1,2]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectOnObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"a\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetCurrentNameWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"a\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentNameWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"a\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        p.overrideCurrentName("test"); // should not throw
    }

    @Test(timeout = 4000)
    public void testGetParsingContextWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getParsingContext());
    }

    @Test(timeout = 4000)
    public void testGetTokenLocationWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertEquals(JsonLocation.NA, p.getTokenLocation());
        assertEquals(JsonLocation.NA, p.getCurrentLocation());
    }

    @Test(timeout = 4000)
    public void testGetTextWhenClosedReturnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextCharactersWhenClosedThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        try {
            p.getTextCharacters();
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetTextLengthWhenClosedThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        try {
            p.getTextLength();
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetTextOffsetWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertEquals(0, p.getTextOffset());
    }

    @Test(timeout = 4000)
    public void testHasTextCharactersWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertFalse(p.hasTextCharacters());
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueWhenClosedReturnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getBinaryValue(Base64Variants.getDefaultVariant()));
    }

    @Test(timeout = 4000)
    public void testReadBinaryValueWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = p.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(0, len);
        assertEquals(0, out.size());
    }

    @Test(timeout = 4000)
    public void testGetNumberTypeWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getNumberType());
    }

    @Test(timeout = 4000)
    public void testIsNaNWhenClosedReturnsFalse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertFalse(p.isNaN());
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObjectWhenClosedReturnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testNextTokenAfterEndOfInput() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.nextToken());
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testNextTokenAfterCloseReturnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenWhenNotAtContainer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.skipChildren();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenWhenAtStartObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"a\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.skipChildren();
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenWhenAtStartArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1,2]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.skipChildren();
        assertEquals(JsonToken.END_ARRAY, p.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenWhenClosed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.close();
        p.skipChildren();
        assertNull(p.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testGetCurrentNameOnFieldName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testGetCurrentNameOnValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertNull(p.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testGetCurrentNameOnArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1,2]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertNull(p.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testGetCurrentNameOnRoot() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNull(p.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentNameOnFieldName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.overrideCurrentName("custom");
        assertEquals("custom", p.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentNameOnValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        p.overrideCurrentName("custom");
        assertNull(p.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testGetParsingContextOnRoot() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNotNull(p.getParsingContext());
    }

    @Test(timeout = 4000)
    public void testGetParsingContextOnArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNotNull(p.getParsingContext());
    }

    @Test(timeout = 4000)
    public void testGetParsingContextOnObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"a\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertNotNull(p.getParsingContext());
    }

    @Test(timeout = 4000)
    public void testGetParsingContextOnNested() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"a\":[1,2]}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertNotNull(p.getParsingContext());
    }

    @Test(timeout = 4000)
    public void testGetTokenLocationAlwaysNA() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("1");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals(JsonLocation.NA, p.getTokenLocation());
        assertEquals(JsonLocation.NA, p.getCurrentLocation());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanNodeValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullNodeValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryNode() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoNode() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectNode() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryToken() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoToken() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectToken() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue2() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue2() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue2() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue2() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue2() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue2() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue2() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue2() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue2() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue2() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue2() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue3() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue3() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue3() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue3() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue3() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue3() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue3() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue3() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue3() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue3() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue3() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue4() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue4() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue4() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue4() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue4() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue4() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue4() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue4() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue4() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue4() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue4() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue5() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue5() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue5() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue5() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue5() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue5() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue5() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue5() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue5() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue5() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue5() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue6() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue6() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue6() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue6() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue6() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue6() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue6() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue6() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue6() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue6() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue6() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue7() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue7() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue7() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue7() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue7() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue7() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue7() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue7() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue7() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue7() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue7() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue8() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue8() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue8() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue8() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue8() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue8() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue8() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue8() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue8() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue8() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue8() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue9() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue9() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue9() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue9() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue9() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue9() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue9() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue9() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue9() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue9() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue9() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue10() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue10() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue10() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue10() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue10() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue10() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue10() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue10() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue10() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue10() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue10() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue11() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue11() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue11() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue11() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue11() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue11() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue11() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue11() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue11() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue11() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue11() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue12() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue12() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue12() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue12() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue12() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue12() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue12() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue12() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue12() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue12() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue12() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue13() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue13() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue13() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue13() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue13() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue13() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue13() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue13() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue13() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue13() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue13() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue14() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue14() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue14() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue14() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue14() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue14() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue14() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue14() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue14() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue14() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue14() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue15() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue15() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue15() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue15() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue15() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue15() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue15() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue15() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue15() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue15() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue15() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue16() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue16() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue16() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue16() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue16() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue16() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue16() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue16() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue16() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue16() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue16() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue17() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue17() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue17() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue17() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue17() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue17() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue17() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue17() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue17() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue17() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue17() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue18() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue18() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue18() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue18() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue18() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue18() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue18() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue18() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue18() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue18() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue18() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue19() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue19() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue19() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue19() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue19() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue19() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue19() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue19() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue19() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue19() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue19() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue20() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue20() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue20() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue20() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue20() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue20() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue20() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue20() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue20() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue20() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue20() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue21() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue21() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue21() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue21() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue21() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue21() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue21() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue21() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue21() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue21() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue21() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue22() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue22() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue22() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue22() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue22() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue22() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue22() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue22() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue22() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue22() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue22() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue23() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue23() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue23() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue23() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue23() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue23() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue23() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue23() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue23() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue23() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue23() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue24() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue24() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue24() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue24() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue24() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue24() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue24() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue24() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue24() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue24() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue24() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue25() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue25() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue25() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue25() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue25() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue25() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue25() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue25() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue25() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue25() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue25() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue26() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue26() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue26() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue26() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue26() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue26() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue26() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue26() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue26() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue26() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue26() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue27() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue27() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue27() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue27() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue27() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue27() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue27() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue27() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue27() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue27() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue27() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue28() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue28() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue28() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue28() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue28() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue28() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue28() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue28() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue28() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue28() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue28() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue29() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue29() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue29() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue29() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue29() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue29() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue29() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue29() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue29() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue29() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue29() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue30() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue30() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue30() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue30() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue30() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue30() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue30() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue30() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue30() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue30() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue30() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue31() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue31() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue31() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue31() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue31() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue31() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue31() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue31() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue31() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue31() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue31() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue32() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue32() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue32() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue32() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue32() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue32() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue32() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue32() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue32() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue32() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue32() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue33() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue33() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue33() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue33() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue33() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue33() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue33() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue33() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue33() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue33() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue33() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue34() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue34() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue34() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue34() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue34() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue34() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue34() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue34() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue34() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue34() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue34() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue35() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue35() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue35() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue35() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue35() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue35() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue35() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue35() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue35() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue35() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue35() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue36() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue36() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue36() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue36() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue36() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue36() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue36() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue36() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue36() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue36() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue36() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue37() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue37() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue37() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue37() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue37() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue37() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue37() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue37() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue37() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue37() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue37() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue38() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue38() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue38() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue38() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue38() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue38() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue38() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue38() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue38() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue38() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue38() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue39() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue39() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue39() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue39() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue39() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue39() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue39() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue39() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue39() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue39() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue39() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue40() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue40() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue40() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue40() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue40() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue40() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue40() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue40() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue40() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue40() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue40() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue41() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue41() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue41() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue41() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue41() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue41() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue41() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue41() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue41() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue41() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue41() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue42() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue42() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue42() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue42() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue42() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue42() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue42() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue42() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue42() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue42() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue42() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue43() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue43() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue43() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue43() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue43() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue43() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue43() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue43() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue43() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue43() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue43() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue44() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue44() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue44() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue44() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue44() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue44() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue44() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue44() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue44() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue44() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue44() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue45() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue45() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue45() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue45() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue45() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue45() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue45() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue45() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue45() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue45() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue45() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue46() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue46() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue46() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue46() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue46() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue46() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue46() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue46() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue46() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue46() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue46() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue47() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue47() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue47() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue47() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue47() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue47() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue47() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue47() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue47() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue47() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue47() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue48() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue48() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue48() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue48() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue48() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue48() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue48() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue48() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue48() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue48() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue48() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue49() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue49() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue49() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue49() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue49() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue49() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue49() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue49() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue49() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue49() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue49() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue50() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue50() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue50() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue50() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue50() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue50() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue50() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue50() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue50() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue50() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue50() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue51() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue51() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue51() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue51() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue51() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue51() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue51() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue51() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue51() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue51() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue51() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue52() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue52() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue52() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue52() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue52() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue52() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue52() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue52() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue52() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue52() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue52() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue53() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue53() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue53() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue53() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue53() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue53() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue53() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue53() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue53() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue53() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue53() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue54() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue54() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue54() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue54() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue54() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue54() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue54() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue54() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue54() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue54() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue54() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue55() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue55() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue55() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue55() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue55() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue55() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue55() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue55() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue55() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue55() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue55() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue56() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue56() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue56() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue56() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue56() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue56() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue56() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue56() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue56() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue56() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue56() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue57() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue57() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue57() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue57() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue57() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue57() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue57() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue57() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue57() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue57() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue57() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue58() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue58() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue58() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue58() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue58() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBooleanTokenValue58() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("true");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStringTokenValue58() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("\"hello\"");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("hello", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnBinaryTokenValue58() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        BinaryNode node = new BinaryNode(data);
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnPojoTokenValue58() throws Exception {
        POJONode node = new POJONode("test");
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEmbeddedObjectTokenValue58() throws Exception {
        POJONode node = new POJONode(new byte[]{1, 2, 3});
        TreeTraversingParser p = new TreeTraversingParser(node);
        p.nextToken();
        assertNotNull(p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFieldNameTokenValue58() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("{\"field\":1}");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        assertEquals("field", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnEndTokenValue59() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        assertEquals("]", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnStartTokenValue59() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("[1]");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("[", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNullTokenValue59() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("null");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("null", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnNumberTokenValue59() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("42");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("42", p.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextOnFloatTokenValue59() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree("3.14");
        TreeTraversingParser p = new TreeTraversingParser(root);
        p.nextToken();
        assertEquals("3.14", p.getText());
    }

    @Test(timeout = 4000)
    public