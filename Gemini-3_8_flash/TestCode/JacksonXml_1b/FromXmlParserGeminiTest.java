package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.*;
import java.util.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Partition A: Core Token Stream Navigation
 * - nextToken() / nextTextValue():
 *   - Branch: _nextToken != null (handling buffered START_OBJECT, START_ARRAY, END_OBJECT, END_ARRAY, FIELD_NAME)
 *   - Branch: XML_START_ELEMENT (with _mayBeLeaf true/false, inArray true/false, virtual wrapping)
 *   - Branch: XML_END_ELEMENT (with _mayBeLeaf true/false, inArray true/false)
 *   - Branch: XML_ATTRIBUTE_NAME / XML_ATTRIBUTE_VALUE
 *   - Branch: XML_TEXT (leaf vs non-leaf text, empty string handling in array vs object context)
 *   - Branch: XML_END (EOF handling and null return)
 *
 * Partition B: Text & Numerical Extraction & Binary Data
 * - getText(), getValueAsString(), getValueAsString(defVal), getTextCharacters(), getTextLength(), getTextOffset(), hasTextCharacters()
 * - getBinaryValue(Base64Variant): Base64 decoding, caching in _binaryValue, exception on non-string / invalid format
 * - Numeric accessors: getIntValue(), getLongValue(), getDoubleValue(), getFloatValue(), getBigIntegerValue(), getDecimalValue(), getNumberType(), getNumberValue()
 *
 * Partition C: Defect-Targeted Branch Zone (JacksonXml-1 / Issue 180)
 * - Handling of nested unwrapped lists containing empty elements or whitespace (<item/>, <item></item>).
 * - Premature END_ARRAY firing in nextToken() causing missing array items / zero-sized collections.
 * - Verification that empty child elements in nested lists preserve parent list size (expected:<1> but was:<0>).
 *
 * Partition D: Attributes, XML Namespaces, Pseudo-Properties
 * - Element containing both attributes and character text (using configured _cfgNameForTextElement).
 * - Skipping attributes when converting START_OBJECT to START_ARRAY in isExpectedStartArrayToken().
 *
 * Partition E: Parser Lifecycle, Configuration & ParsingContext
 * - Feature mask toggling: enable, disable, configure, isEnabled, getFormatFeatures, overrideFormatFeatures.
 * - Lifecycle: close(), isClosed(), AUTO_CLOSE_SOURCE feature, double close idempotency.
 * - ParsingContext: overrideCurrentName, getCurrentName off-by-one behavior for START_OBJECT/START_ARRAY.
 */
public class FromXmlParserGeminiTest {

    public static class Root180 {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Folder180> folder;
    }

    public static class Folder180 {
        public String name;
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Item180> item;
    }

    public static class Item180 {
        public String id;
    }

    private XmlMapper createXmlMapper() {
        return new XmlMapper();
    }

    private FromXmlParser createParser(String xml) throws IOException {
        XmlMapper mapper = createXmlMapper();
        return (FromXmlParser) mapper.getFactory().createParser(xml);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (JacksonXml-1 / Issue 180)
    // =========================================================================

    @Test(timeout = 4000)
    public void testNestedUnwrappedListsWithEmptyElement_Issue180() throws Exception {
        String xml = "<Root><folder><name>test</name><item/></folder></Root>";
        XmlMapper mapper = createXmlMapper();
        Root180 root = mapper.readValue(xml, Root180.class);

        assertNotNull("Root object should not be null", root);
        assertNotNull("Folder list should not be null", root.folder);
        assertEquals("Expected exactly 1 folder element", 1, root.folder.size());
        Folder180 folder = root.folder.get(0);
        assertEquals("test", folder.name);
        assertNotNull("Item list should not be null", folder.item);
        assertEquals("Expected 1 empty item element inside folder", 1, folder.item.size());
    }

    @Test(timeout = 4000)
    public void testNestedUnwrappedListsWithEmptyElement2_Issue180() throws Exception {
        String xml = "<Root><folder><name>test</name><item></item></folder></Root>";
        XmlMapper mapper = createXmlMapper();
        Root180 root = mapper.readValue(xml, Root180.class);

        assertNotNull(root);
        assertNotNull(root.folder);
        assertEquals(1, root.folder.size());
        Folder180 folder = root.folder.get(0);
        assertEquals("test", folder.name);
        assertNotNull(folder.item);
        assertEquals(1, folder.item.size());
    }

    @Test(timeout = 4000)
    public void testNestedUnwrappedListsWithMultipleEmptyElements_Issue180() throws Exception {
        String xml = "<Root><folder><name>f1</name><item/><item></item></folder></Root>";
        XmlMapper mapper = createXmlMapper();
        Root180 root = mapper.readValue(xml, Root180.class);

        assertNotNull(root);
        assertNotNull(root.folder);
        assertEquals(1, root.folder.size());
        assertEquals(2, root.folder.get(0).item.size());
    }

    // =========================================================================
    // Partition A: Core Token Stream Navigation
    // =========================================================================

    @Test(timeout = 4000)
    public void testTokenStreamSimpleObject() throws Exception {
        FromXmlParser parser = createParser("<root><name>Jackson</name><age>10</age></root>");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Jackson", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("age", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("10", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNextTextValueDirectly() throws Exception {
        FromXmlParser parser = createParser("<root><name>Gemini</name><empty/></root>");

        assertNull(parser.nextTextValue()); // consumes START_OBJECT -> null
        assertNull(parser.nextTextValue()); // consumes FIELD_NAME ('name') -> null
        assertEquals("Gemini", parser.nextTextValue()); // consumes VALUE_STRING -> returns string

        assertNull(parser.nextTextValue()); // consumes FIELD_NAME ('empty') -> null
        assertEquals("", parser.nextTextValue()); // empty element produced "" in nextTextValue

        assertNull(parser.nextTextValue()); // consumes END_OBJECT
        assertNull(parser.nextTextValue()); // EOF
        parser.close();
    }

    @Test(timeout = 4000)
    public void testEmptyElementYieldsNullInNextToken() throws Exception {
        FromXmlParser parser = createParser("<root><empty/></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("empty", parser.getCurrentName());
        // For nextToken(), an empty leaf element emits VALUE_NULL
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testIsExpectedStartArrayTokenConversion() throws Exception {
        FromXmlParser parser = createParser("<root><item>1</item><item>2</item></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        // Convert START_OBJECT to START_ARRAY
        assertTrue(parser.isExpectedStartArrayToken());
        assertEquals(JsonToken.START_ARRAY, parser.getCurrentToken());
        // Subsequent call when already START_ARRAY returns true
        assertTrue(parser.isExpectedStartArrayToken());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // Subsequent call on scalar token returns false
        assertFalse(parser.isExpectedStartArrayToken());

        parser.close();
    }

    // =========================================================================
    // Partition B: Text & Numerical Extraction & Binary Data
    // =========================================================================

    @Test(timeout = 4000)
    public void testTextExtractionAndCharacterArrays() throws Exception {
        FromXmlParser parser = createParser("<root><data>HelloWorld</data></root>");
        assertNull(parser.getText());
        assertEquals(0, parser.getTextLength());
        assertNull(parser.getTextCharacters());
        assertEquals(0, parser.getTextOffset());
        assertFalse(parser.hasTextCharacters());

        parser.nextToken(); // START_OBJECT
        assertEquals("{", parser.getText());

        parser.nextToken(); // FIELD_NAME
        assertEquals("data", parser.getText());
        assertArrayEquals("data".toCharArray(), parser.getTextCharacters());
        assertEquals(4, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());

        parser.nextToken(); // VALUE_STRING
        assertEquals("HelloWorld", parser.getText());
        assertArrayEquals("HelloWorld".toCharArray(), parser.getTextCharacters());
        assertEquals(10, parser.getTextLength());

        assertEquals("HelloWorld", parser.getValueAsString());
        assertEquals("HelloWorld", parser.getValueAsString("default"));

        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefault() throws Exception {
        FromXmlParser parser = createParser("<root></root>");
        assertNull(parser.getValueAsString("fallback"));
        parser.nextToken(); // START_OBJECT
        assertNull(parser.getValueAsString(null));
        parser.nextToken(); // END_OBJECT
        assertEquals("fallback", parser.getValueAsString("fallback"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNumericAccessorsDefaults() throws Exception {
        FromXmlParser parser = createParser("<root><num>42</num></root>");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING

        assertEquals(0, parser.getIntValue());
        assertEquals(0L, parser.getLongValue());
        assertEquals(0.0, parser.getDoubleValue(), 0.0001);
        assertEquals(0.0f, parser.getFloatValue(), 0.0001f);
        assertNull(parser.getBigIntegerValue());
        assertNull(parser.getDecimalValue());
        assertNull(parser.getNumberType());
        assertNull(parser.getNumberValue());
        assertNull(parser.getEmbeddedObject());

        parser.close();
    }

    @Test(timeout = 4000)
    public void testBinaryValueDecoding() throws Exception {
        // "Hello World" in Base64 is "SGVsbG8gV29ybGQ="
        FromXmlParser parser = createParser("<root><bin>SGVsbG8gV29ybGQ=</bin></root>");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING

        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        assertNotNull(binary);
        assertEquals("Hello World", new String(binary, "UTF-8"));

        // Call again to verify cached _binaryValue path
        byte[] binaryCached = parser.getBinaryValue(Base64Variants.MIME);
        assertSame(binary, binaryCached);

        parser.close();
    }

    @Test(timeout = 4000)
    public void testBinaryValueInvalidTokenThrows() throws Exception {
        FromXmlParser parser = createParser("<root></root>");
        parser.nextToken(); // START_OBJECT
        try {
            parser.getBinaryValue(Base64Variants.MIME);
            fail("Expected JsonParseException for binary access on START_OBJECT");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("can not access as binary"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testBinaryValueInvalidBase64ContentThrows() throws Exception {
        FromXmlParser parser = createParser("<root><bin>NotValidBase64!@#$</bin></root>");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING
        try {
            parser.getBinaryValue(Base64Variants.MIME);
            fail("Expected JsonParseException for corrupted base64 data");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("Failed to decode VALUE_STRING as base64"));
        } finally {
            parser.close();
        }
    }

    // =========================================================================
    // Partition D: Attribute & XML Namespace Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testAttributesAndMixedTextProperty() throws Exception {
        FromXmlParser parser = createParser("<root id=\"123\">Content</root>");
        parser.setXMLTextElementName("unnamedText");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("id", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("123", parser.getText());

        // Pseudo property for element text when attributes are present
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("unnamedText", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Content", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testStaxReaderAccess() throws Exception {
        FromXmlParser parser = createParser("<root/>");
        assertNotNull("StaxReader accessor should return underlying XMLStreamReader", parser.getStaxReader());
        parser.close();
    }

    // =========================================================================
    // Partition E: Parsing Context State Machine & Parser Features
    // =========================================================================

    @Test(timeout = 4000)
    public void testParserConfigurationAndFeatures() throws Exception {
        FromXmlParser parser = createParser("<root/>");

        assertTrue("XML parser requires custom codec", parser.requiresCustomCodec());
        assertNotNull("Package version should not be null", parser.version());

        ObjectCodec codec = parser.getCodec();
        assertNotNull(codec);
        parser.setCodec(null);
        assertNull(parser.getCodec());
        parser.setCodec(codec);

        int initialFeatures = parser.getFormatFeatures();
        parser.overrideFormatFeatures(0, -1);
        assertEquals(0, parser.getFormatFeatures());
        parser.overrideFormatFeatures(initialFeatures, initialFeatures);
        assertEquals(initialFeatures, parser.getFormatFeatures());

        assertEquals(0, FromXmlParser.Feature.collectDefaults());

        parser.close();
    }

    @Test(timeout = 4000)
    public void testCurrentNameOverrides() throws Exception {
        FromXmlParser parser = createParser("<root><child>value</child></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        parser.overrideCurrentName("customRoot");
        // Check off-by-one parent handling
        assertEquals("customRoot", parser.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("child", parser.getCurrentName());
        parser.overrideCurrentName("renamedChild");
        assertEquals("renamedChild", parser.getCurrentName());

        parser.close();
    }

    @Test(timeout = 4000)
    public void testMissingNameThrowsException() throws Exception {
        FromXmlParser parser = createParser("<root/>");
        try {
            parser.getCurrentName();
            fail("Expected IllegalStateException when calling getCurrentName before starting");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Missing name"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testCloseIdempotencyAndLocations() throws Exception {
        FromXmlParser parser = createParser("<root>data</root>");
        assertFalse(parser.isClosed());
        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());
        assertNotNull(parser.getParsingContext());

        parser.close();
        assertTrue(parser.isClosed());
        // Second close should be safe and idempotent
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testVirtualWrappingAssignment() throws Exception {
        FromXmlParser parser = createParser("<root><item>1</item></root>");
        Set<String> wrapNames = new HashSet<String>(Arrays.asList("item"));
        parser.addVirtualWrapping(wrapNames);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.close();
    }
}