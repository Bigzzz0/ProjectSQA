/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.node.TreeTraversingParser
 *
 * Branch & Path Coverage:
 * 1. Constructor:
 *    - JsonNode.isArray() -> ArrayCursor, _nextToken = START_ARRAY
 *    - JsonNode.isObject() -> ObjectCursor, _nextToken = START_OBJECT
 *    - Else (value node) -> RootCursor, _nextToken = null
 * 2. nextToken():
 *    - _nextToken != null (initial token consumption)
 *    - _startContainer == true:
 *      * !currentHasChildren() -> skips container, returns END_OBJECT / END_ARRAY
 *      * currentHasChildren() -> iterates children, sets _startContainer if child is container
 *    - _nodeCursor == null -> marks _closed, returns null
 *    - _nodeCursor.nextToken() != null -> checks if START_OBJECT / START_ARRAY to set _startContainer
 *    - _nodeCursor.nextToken() == null -> returns endToken(), updates cursor to parent
 * 3. skipChildren():
 *    - _currToken == START_OBJECT -> reset _startContainer, sets END_OBJECT
 *    - _currToken == START_ARRAY -> reset _startContainer, sets END_ARRAY
 *    - Other tokens -> no-op
 * 4. getText() & Textual Accessors:
 *    - _closed == true -> returns null
 *    - _currToken switch: FIELD_NAME, VALUE_STRING, VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT,
 *      VALUE_EMBEDDED_OBJECT (BinaryNode asText() base64 vs POJONode), default asString()
 *    - getTextCharacters(), getTextLength(), getTextOffset(), hasTextCharacters()
 * 5. Numeric Accessors & currentNumericNode():
 *    - getNumberType, getBigIntegerValue, getDecimalValue, getDoubleValue, getFloatValue,
 *      getLongValue, getIntValue, getNumberValue
 *    - Non-numeric token -> JsonParseException ("Current token (...) not numeric...")
 * 6. Binary & Embedded Access:
 *    - getEmbeddedObject(): POJONode, BinaryNode, closed parser, non-embedded nodes
 *    - getBinaryValue(b64variant): TextNode with variant vs BinaryNode vs POJONode(byte[]) vs other
 *    - readBinaryValue(b64variant, OutputStream)
 *    - isNaN(): NumericNode with NaN / non-NaN, non-numeric node, closed parser
 * 7. Name & Context Tracking:
 *    - getCurrentName(), overrideCurrentName(), getParsingContext(), getTokenLocation(), getCurrentLocation()
 * 8. Defect Focus (databind #2096 / TestConversions#testBase64Text):
 *    - getBinaryValue(Base64Variant) fails on TextNode encoded with Base64Variants.MODIFIED_FOR_URL
 *      because defective parser delegates to n.binaryValue() which hardcodes the default Base64Variant
 *      instead of passing the custom Base64Variant.
 */

package com.fasterxml.jackson.databind.node;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;

public class TreeTraversingParserGeminiTest {

    private final JsonNodeFactory nf = JsonNodeFactory.instance;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testArrayTraversalWithMixedElements() throws IOException {
        ArrayNode array = nf.arrayNode();
        array.add(100);
        array.add("hello");
        array.add(true);
        array.addNull();

        TreeTraversingParser parser = new TreeTraversingParser(array);
        assertFalse(parser.isClosed());
        assertNull(parser.getCurrentToken());

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.START_ARRAY, parser.getCurrentToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(100, parser.getIntValue());
        assertEquals(100L, parser.getLongValue());
        assertEquals(100.0, parser.getDoubleValue(), 0.0001);
        assertEquals(100.0f, parser.getFloatValue(), 0.0001f);
        assertEquals(BigInteger.valueOf(100), parser.getBigIntegerValue());
        assertEquals(new BigDecimal(100), parser.getDecimalValue());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
        assertEquals(Integer.valueOf(100), parser.getNumberValue());
        assertEquals("100", parser.getText());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        assertArrayEquals("hello".toCharArray(), parser.getTextCharacters());
        assertEquals(5, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertFalse(parser.hasTextCharacters());

        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        assertEquals("true", parser.getText());

        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("null", parser.getText());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testObjectTraversalWithNestedStructures() throws IOException {
        ObjectNode root = nf.objectNode();
        root.put("name", "Alice");
        ObjectNode child = root.putObject("child");
        child.put("age", 12);
        ArrayNode items = root.putArray("items");
        items.add(1);

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals("name", parser.getText());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Alice", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("child", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("age", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(12, parser.getIntValue());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("items", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testRootValueNodesDirectTraversal() throws IOException {
        TextNode textNode = nf.textNode("standalone");
        TreeTraversingParser parser = new TreeTraversingParser(textNode);

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("standalone", parser.getText());
        assertNull(parser.nextToken());
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenForObjectAndArray() throws IOException {
        ObjectNode root = nf.objectNode();
        ObjectNode subObj = root.putObject("subObj");
        subObj.put("k1", "v1");
        ArrayNode subArr = root.putArray("subArr");
        subArr.add("v2");
        root.put("tail", "end");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("subObj", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertSame(parser, parser.skipChildren());
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("subArr", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertSame(parser, parser.skipChildren());
        assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("tail", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("end", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnNonContainerTokenIsNoOp() throws IOException {
        TextNode node = nf.textNode("value");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertSame(parser, parser.skipChildren());
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testEmptyContainerOptimization() throws IOException {
        ObjectNode root = nf.objectNode();
        root.putArray("emptyArray");
        root.putObject("emptyObject");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("emptyArray", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("emptyObject", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyRootContainers() throws IOException {
        // Empty Array
        TreeTraversingParser arrParser = new TreeTraversingParser(nf.arrayNode());
        assertEquals(JsonToken.START_ARRAY, arrParser.nextToken());
        assertEquals(JsonToken.END_ARRAY, arrParser.nextToken());
        assertNull(arrParser.nextToken());
        assertTrue(arrParser.isClosed());

        // Empty Object
        TreeTraversingParser objParser = new TreeTraversingParser(nf.objectNode());
        assertEquals(JsonToken.START_OBJECT, objParser.nextToken());
        assertEquals(JsonToken.END_OBJECT, objParser.nextToken());
        assertNull(objParser.nextToken());
        assertTrue(objParser.isClosed());
    }

    @Test(timeout = 4000)
    public void testNumericBoundariesAndSpecialValues() throws IOException {
        ObjectNode root = nf.objectNode();
        root.put("maxLong", Long.MAX_VALUE);
        root.put("minLong", Long.MIN_VALUE);
        root.put("bigInt", new BigInteger("123456789012345678901234567890"));
        root.put("bigDec", new BigDecimal("12345678901234567890.123456789"));
        root.put("doubleNan", Double.NaN);
        root.put("doubleNormal", 3.14159);

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        // maxLong
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Long.MAX_VALUE, parser.getLongValue());
        assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());
        assertFalse(parser.isNaN());

        // minLong
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Long.MIN_VALUE, parser.getLongValue());

        // bigInt
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(new BigInteger("123456789012345678901234567890"), parser.getBigIntegerValue());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, parser.getNumberType());

        // bigDec
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(new BigDecimal("12345678901234567890.123456789"), parser.getDecimalValue());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, parser.getNumberType());

        // doubleNan
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(parser.isNaN());
        assertTrue(Double.isNaN(parser.getDoubleValue()));

        // doubleNormal
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertFalse(parser.isNaN());
        assertEquals(3.14159, parser.getDoubleValue(), 0.000001);
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testBinaryAndPojoNodesHandling() throws IOException {
        byte[] rawBytes = new byte[] { 1, 2, 3, 4, 5 };
        BinaryNode binaryNode = nf.binaryNode(rawBytes);
        TreeTraversingParser binParser = new TreeTraversingParser(binaryNode);

        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, binParser.nextToken());
        assertArrayEquals(rawBytes, (byte[]) binParser.getEmbeddedObject());
        assertArrayEquals(rawBytes, binParser.getBinaryValue(Base64Variants.getDefaultVariant()));
        assertNotNull(binParser.getText()); // converts to Base64 text

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int written = binParser.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(rawBytes.length, written);
        assertArrayEquals(rawBytes, out.toByteArray());

        // POJO with byte[]
        POJONode pojoBytes = nf.pojoNode(rawBytes);
        TreeTraversingParser pojoParser = new TreeTraversingParser(pojoBytes);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, pojoParser.nextToken());
        assertArrayEquals(rawBytes, (byte[]) pojoParser.getEmbeddedObject());
        assertArrayEquals(rawBytes, pojoParser.getBinaryValue(Base64Variants.getDefaultVariant()));

        // POJO with non-byte[]
        POJONode pojoOther = nf.pojoNode("customObject");
        TreeTraversingParser pojoOtherParser = new TreeTraversingParser(pojoOther);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, pojoOtherParser.nextToken());
        assertEquals("customObject", pojoOtherParser.getEmbeddedObject());
        assertNull(pojoOtherParser.getBinaryValue(Base64Variants.getDefaultVariant()));
        assertEquals(0, pojoOtherParser.readBinaryValue(Base64Variants.getDefaultVariant(), new ByteArrayOutputStream()));
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentNameAndParsingContext() throws IOException {
        ObjectNode root = nf.objectNode();
        root.put("origField", "value");

        TreeTraversingParser parser = new TreeTraversingParser(root);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("origField", parser.getCurrentName());

        parser.overrideCurrentName("renamedField");
        assertEquals("renamedField", parser.getCurrentName());

        assertNotNull(parser.getParsingContext());
        assertEquals(JsonLocation.NA, parser.getTokenLocation());
        assertEquals(JsonLocation.NA, parser.getCurrentLocation());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind #2096 / testBase64Text)
    // =========================================================================

    /**
     * Target Defect Ground Truth:
     * com.fasterxml.jackson.databind.node.TestConversions::testBase64Text
     * junit.framework.AssertionFailedError: Failed (variant MODIFIED-FOR-URL, data length 1):
     * Cannot access contents of TextNode as binary due to broken Base64 encoding: Unexpected end-of-String in base64 content
     *
     * In TreeTraversingParser.getBinaryValue(Base64Variant b64variant):
     * Defective code delegates directly to n.binaryValue() which hardcodes the default Base64Variant (MIME)
     * instead of passing the supplied b64variant (such as MODIFIED_FOR_URL).
     */
    @Test(timeout = 4000)
    public void testGetBinaryValueWithModifiedForUrlVariantOnTextNode() throws IOException {
        Base64Variant variant = Base64Variants.MODIFIED_FOR_URL;
        byte[] originalData = new byte[] { (byte) 0xFA };
        String encoded = variant.encode(originalData); // 2 chars without padding, e.g. "-g"

        TextNode textNode = nf.textNode(encoded);
        TreeTraversingParser parser = new TreeTraversingParser(textNode);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        // This triggers the defect in TreeTraversingParser.getBinaryValue()
        byte[] decoded = parser.getBinaryValue(variant);
        assertNotNull("Decoded binary data should not be null", decoded);
        assertArrayEquals("Decoded data must match original input", originalData, decoded);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesWritten = parser.readBinaryValue(variant, out);
        assertEquals(originalData.length, bytesWritten);
        assertArrayEquals(originalData, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueMultipleDataLengthsWithCustomVariant() throws IOException {
        Base64Variant variant = Base64Variants.MODIFIED_FOR_URL;
        for (int length = 1; length <= 5; ++length) {
            byte[] data = new byte[length];
            for (int i = 0; i < length; ++i) {
                data[i] = (byte) (i * 37 + 13);
            }
            String encoded = variant.encode(data);
            TextNode textNode = nf.textNode(encoded);
            TreeTraversingParser parser = new TreeTraversingParser(textNode);
            parser.nextToken();

            byte[] actual = parser.getBinaryValue(variant);
            assertNotNull("Length " + length + " decoded null with variant " + variant, actual);
            assertArrayEquals("Failed on length " + length, data, actual);
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testNumericAccessorsOnNonNumericNodeThrowsJsonParseException() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(nf.textNode("notANumber"));
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        try {
            parser.getIntValue();
            fail("Expected JsonParseException on getIntValue()");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("not numeric"));
        }

        try {
            parser.getLongValue();
            fail("Expected JsonParseException on getLongValue()");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("not numeric"));
        }

        try {
            parser.getDoubleValue();
            fail("Expected JsonParseException on getDoubleValue()");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("not numeric"));
        }

        try {
            parser.getFloatValue();
            fail("Expected JsonParseException on getFloatValue()");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("not numeric"));
        }

        try {
            parser.getBigIntegerValue();
            fail("Expected JsonParseException on getBigIntegerValue()");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("not numeric"));
        }

        try {
            parser.getDecimalValue();
            fail("Expected JsonParseException on getDecimalValue()");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("not numeric"));
        }

        try {
            parser.getNumberType();
            fail("Expected JsonParseException on getNumberType()");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("not numeric"));
        }

        try {
            parser.getNumberValue();
            fail("Expected JsonParseException on getNumberValue()");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("not numeric"));
        }
    }

    @Test(timeout = 4000)
    public void testClosedParserBehavior() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(nf.textNode("test"));
        parser.nextToken();
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());

        // Repeated close should be idempotent
        parser.close();
        assertTrue(parser.isClosed());

        assertNull(parser.getText());
        assertNull(parser.getEmbeddedObject());
        assertFalse(parser.isNaN());
        assertNull(parser.getCurrentName());
        assertNull(parser.nextToken());

        // overrideCurrentName should do nothing when closed / nodeCursor is null
        parser.overrideCurrentName("newName");
        assertNull(parser.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testHandleEOFProtectedMethod() {
        TreeTraversingParser parser = new TreeTraversingParser(nf.nullNode());
        try {
            parser._handleEOF();
            fail("Expected JsonParseException / RuntimeException from _handleEOF()");
        } catch (JsonParseException expected) {
            // Expected internal error
        } catch (RuntimeException expected) {
            // ParserMinimalBase may throw internal error
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCodecAndVersionContract() {
        ObjectCodec mockCodec = null; // ObjectCodec is abstract
        TreeTraversingParser parser = new TreeTraversingParser(nf.nullNode(), mockCodec);
        assertNull(parser.getCodec());

        parser.setCodec(null);
        assertNull(parser.getCodec());

        Version v = parser.version();
        assertNotNull(v);
        assertFalse(v.isUnknownVersion());
    }

    @Test(timeout = 4000)
    public void testBinaryValueOnNonBinaryNodeReturnsNull() throws IOException {
        TreeTraversingParser parser = new TreeTraversingParser(nf.booleanNode(true));
        parser.nextToken();
        assertNull(parser.getBinaryValue(Base64Variants.getDefaultVariant()));
        assertEquals(0, parser.readBinaryValue(Base64Variants.getDefaultVariant(), new ByteArrayOutputStream()));
    }
}