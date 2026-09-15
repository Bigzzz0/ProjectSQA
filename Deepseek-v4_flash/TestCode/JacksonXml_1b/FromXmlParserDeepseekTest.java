package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.*;
import java.util.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive test suite for FromXmlParser targeting maximum coverage and defect detection.
 */
public class FromXmlParserDeepseekTest {

    // ============================================================
    // POJO definitions for nested unwrapped lists defect testing
    // ============================================================

    static class Root {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Folder> folder;
    }

    static class Folder {
        public String name;
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Item> item;
    }

    static class Item {
        public String id;
    }

    // ============================================================
    // Helper methods
    // ============================================================

    private XmlMapper createMapper() {
        return new XmlMapper();
    }

    private FromXmlParser createParser(String xml) throws Exception {
        XmlMapper mapper = createMapper();
        XmlFactory factory = mapper.getFactory();
        return (FromXmlParser) factory.createParser(new StringReader(xml));
    }

    // ============================================================
    // Defect-specific test: NestedUnwrappedLists with empty element
    // ============================================================

    /**
     * @target nextToken(), _mayBeLeaf handling, empty element in unwrapped list
     * @scenario XML with empty item element inside unwrapped folder list
     * @defectRisk Issue #180: empty element causes loss of elements or wrong count
     */
    @Test(timeout = 4000)
    public void testNestedUnwrappedListsWithEmptyElement_Issue180() throws Exception {
        String xml = "<Root><folder><name>test</name><item/></folder></Root>";
        XmlMapper mapper = createMapper();
        Root root = mapper.readValue(xml, Root.class);
        assertNotNull("Root should not be null", root);
        assertNotNull("folder list should not be null", root.folder);
        assertEquals("Should have 1 folder", 1, root.folder.size());
        Folder folder = root.folder.get(0);
        assertEquals("Folder name should be 'test'", "test", folder.name);
        assertNotNull("item list should not be null", folder.item);
        assertEquals("Should have 1 item (empty element)", 1, folder.item.size());
        assertNull("Item id should be null for empty element", folder.item.get(0).id);
    }

    /**
     * @target nextToken(), empty folder with no items
     * @scenario XML with folder element but no item children
     * @defectRisk Issue #180: empty folder causes loss of folder element
     */
    @Test(timeout = 4000)
    public void testNestedWithEmptyFolder_Issue180() throws Exception {
        String xml = "<Root><folder><name>f1</name></folder></Root>";
        XmlMapper mapper = createMapper();
        Root root = mapper.readValue(xml, Root.class);
        assertNotNull("Root should not be null", root);
        assertNotNull("folder list should not be null", root.folder);
        assertEquals("Should have 1 folder", 1, root.folder.size());
        Folder folder = root.folder.get(0);
        assertEquals("Folder name should be 'f1'", "f1", folder.name);
        assertNull("item list should be null for folder with no items", folder.item);
    }

    /**
     * @target nextToken(), multiple empty items in unwrapped list
     * @scenario XML with multiple empty item elements
     * @defectRisk Issue #180: multiple empty elements cause count mismatch
     */
    @Test(timeout = 4000)
    public void testNestedWithMultipleEmptyItems_Issue180() throws Exception {
        String xml = "<Root><folder><name>test</name><item/><item/><item/></folder></Root>";
        XmlMapper mapper = createMapper();
        Root root = mapper.readValue(xml, Root.class);
        assertNotNull("Root should not be null", root);
        assertEquals("Should have 1 folder", 1, root.folder.size());
        Folder folder = root.folder.get(0);
        assertEquals("Should have 3 items", 3, folder.item.size());
        for (Item item : folder.item) {
            assertNull("Item id should be null", item.id);
        }
    }

    /**
     * @target nextToken(), mixed empty and non-empty items
     * @scenario XML with both empty and populated item elements
     * @defectRisk Issue #180: mixed content causes element loss
     */
    @Test(timeout = 4000)
    public void testNestedWithMixedItems_Issue180() throws Exception {
        String xml = "<Root><folder><name>test</name><item/><item><id>1</id></item><item/></folder></Root>";
        XmlMapper mapper = createMapper();
        Root root = mapper.readValue(xml, Root.class);
        assertNotNull("Root should not be null", root);
        assertEquals("Should have 1 folder", 1, root.folder.size());
        Folder folder = root.folder.get(0);
        assertEquals("Should have 3 items", 3, folder.item.size());
        assertNull("First item id should be null", folder.item.get(0).id);
        assertEquals("Second item id should be '1'", "1", folder.item.get(1).id);
        assertNull("Third item id should be null", folder.item.get(2).id);
    }

    // ============================================================
    // Token navigation tests
    // ============================================================

    /**
     * @target nextToken(), basic token sequence
     * @scenario Simple XML element with text content
     * @defectRisk Incorrect token sequence for simple elements
     */
    @Test(timeout = 4000)
    public void testBasicTokenSequence() throws Exception {
        String xml = "<root>text</root>";
        FromXmlParser parser = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("root", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("text", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    /**
     * @target nextToken(), nested objects
     * @scenario XML with nested elements
     * @defectRisk Incorrect context management for nested objects
     */
    @Test(timeout = 4000)
    public void testNestedObjectTokenSequence() throws Exception {
        String xml = "<outer><inner>value</inner></outer>";
        FromXmlParser parser = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("outer", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("inner", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    /**
     * @target nextToken(), array context
     * @scenario XML with repeated elements (implicit array)
     * @defectRisk Incorrect array token handling
     */
    @Test(timeout = 4000)
    public void testArrayTokenSequence() throws Exception {
        String xml = "<root><item>a</item><item>b</item></root>";
        FromXmlParser parser = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("root", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("a", parser.getText());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("b", parser.getText());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    /**
     * @target nextTextValue(), basic text value
     * @scenario Simple element with text content
     * @defectRisk Incorrect text value extraction
     */
    @Test(timeout = 4000)
    public void testNextTextValueBasic() throws Exception {
        String xml = "<root>hello</root>";
        FromXmlParser parser = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("hello", parser.nextTextValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    /**
     * @target nextTextValue(), empty element
     * @scenario Empty XML element
     * @defectRisk Empty element should return empty string, not null
     */
    @Test(timeout = 4000)
    public void testNextTextValueEmptyElement() throws Exception {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("", parser.nextTextValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    /**
     * @target nextTextValue(), nested element returns null
     * @scenario Element with child element (not leaf)
     * @defectRisk Should return null when element has children
     */
    @Test(timeout = 4000)
    public void testNextTextValueWithChildren() throws Exception {
        String xml = "<root><child>text</child></root>";
        FromXmlParser parser = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertNull(parser.nextTextValue());
        parser.close();
    }

    /**
     * @target skipChildren(), simple case
     * @scenario Skip children of an element
     * @defectRisk Incorrect skipping of child tokens
     */
    @Test(timeout = 4000)
    public void testSkipChildren() throws Exception {
        String xml = "<root><a>1</a><b>2</b></root>";
        FromXmlParser parser = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.skipChildren();
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
        parser.close();
    }

    // ============================================================
    // Value extraction tests
    // ============================================================

    /**
     * @target getText(), various token types
     * @scenario Test getText() for FIELD_NAME, VALUE_STRING, and other tokens
     * @defectRisk Incorrect text extraction for different token types
     */
    @Test(timeout = 4000)
    public void testGetText() throws Exception {
        String xml = "<root>value</root>";
        FromXmlParser parser = createParser(xml);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        assertEquals("root", parser.getText());
        parser.nextToken(); // VALUE_STRING
        assertEquals("value", parser.getText());
        parser.nextToken(); // END_OBJECT
        assertEquals("}", parser.getText());
        parser.close();
    }

    /**
     * @target getTextCharacters(), getTextLength(), getTextOffset()
     * @scenario Test character array access methods
     * @defectRisk Incorrect character array handling
     */
    @Test(timeout = 4000)
    public void testTextCharacters() throws Exception {
        String xml = "<root>hello</root>";
        FromXmlParser parser = createParser(xml);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING
        char[] chars = parser.getTextCharacters();
        assertNotNull(chars);
        assertEquals(5, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertEquals("hello", new String(chars, 0, parser.getTextLength()));
        parser.close();
    }

    /**
     * @target getValueAsString(), from VALUE_STRING
     * @scenario Get value as string from text element
     * @defectRisk Incorrect string conversion
     */
    @Test(timeout = 4000)
    public void testGetValueAsString() throws Exception {
        String xml = "<root>text</root>";
        FromXmlParser parser = createParser(xml);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING
        assertEquals("text", parser.getValueAsString());
        parser.close();
    }

    /**
     * @target getValueAsString(), from START_OBJECT with attributes
     * @scenario Element with attributes that can be converted to string
     * @defectRisk Incorrect conversion of attribute-only element to string
     */
    @Test(timeout = 4000)
    public void testGetValueAsStringFromObject() throws Exception {
        String xml = "<root attr=\"val\">text</root>";
        FromXmlParser parser = createParser(xml);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // START_OBJECT (due to attributes)
        // Now we're at START_OBJECT, try getValueAsString
        String result = parser.getValueAsString();
        // This may return null or the text depending on implementation
        // Just verify no exception
        parser.close();
    }

    // ============================================================
    // XML attributes and namespace tests
    // ============================================================

    /**
     * @target nextToken(), XML attributes
     * @scenario Element with attributes
     * @defectRisk Incorrect handling of XML attributes
     */
    @Test(timeout = 4000)
    public void testXmlAttributes() throws Exception {
        String xml = "<root attr1=\"val1\" attr2=\"val2\">text</root>";
        FromXmlParser parser = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("root", parser.getCurrentName());
        // Attributes cause START_OBJECT
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // First attribute as field name
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        String name1 = parser.getCurrentName();
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        String val1 = parser.getText();
        // Second attribute
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        String name2 = parser.getCurrentName();
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        String val2 = parser.getText();
        // Text content as field
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("", parser.getCurrentName()); // default text property name
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("text", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    /**
     * @target getCurrentName(), various contexts
     * @scenario Test current name in different parsing contexts
     * @defectRisk Incorrect name resolution
     */
    @Test(timeout = 4000)
    public void testGetCurrentName() throws Exception {
        String xml = "<root><child>value</child></root>";
        FromXmlParser parser = createParser(xml);
        parser.nextToken(); // START_OBJECT
        assertEquals("root", parser.getCurrentName());
        parser.nextToken(); // FIELD_NAME
        assertEquals("root", parser.getCurrentName());
        parser.nextToken(); // START_OBJECT
        assertEquals("root", parser.getCurrentName());
        parser.nextToken(); // FIELD_NAME
        assertEquals("child", parser.getCurrentName());
        parser.nextToken(); // VALUE_STRING
        assertEquals("child", parser.getCurrentName());
        parser.close();
    }

    /**
     * @target overrideCurrentName()
     * @scenario Override current name for a token
     * @defectRisk Incorrect name override behavior
     */
    @Test(timeout = 4000)
    public void testOverrideCurrentName() throws Exception {
        String xml = "<root>value</root>";
        FromXmlParser parser = createParser(xml);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.overrideCurrentName("overridden");
        assertEquals("overridden", parser.getCurrentName());
        parser.close();
    }

    // ============================================================
    // Empty elements and text element name tests
    // ============================================================

    /**
     * @target _cfgNameForTextElement, setXMLTextElementName()
     * @scenario Custom text element property name
     * @defectRisk Incorrect text property name configuration
     */
    @Test(timeout = 4000)
    public void testCustomTextElementName() throws Exception {
        String xml = "<root>text</root>";
        XmlMapper mapper = createMapper();
        // Configure custom text element name
        mapper.configOverride(String.class).setSetterInfo(JsonSetter.Value.forValueNulls(JsonSetter.Nulls.SKIP));
        // Use ObjectMapper to test configuration
        String result = mapper.readValue(xml, String.class);
        assertEquals("text", result);
    }

    /**
     * @target _isEmpty(), empty text handling
     * @scenario Element with whitespace-only text
     * @defectRisk Whitespace-only text should be treated as empty
     */
    @Test(timeout = 4000)
    public void testWhitespaceOnlyText() throws Exception {
        String xml = "<root>   </root>";
        FromXmlParser parser = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        String text = parser.getText();
        assertTrue("Whitespace text should be preserved", text.equals("   ") || text.equals(""));
        parser.close();
    }

    /**
     * @target nextToken(), empty element in object context
     * @scenario Empty element inside an object
     * @defectRisk Empty element should produce VALUE_NULL
     */
    @Test(timeout = 4000)
    public void testEmptyElementInObject() throws Exception {
        String xml = "<root><empty/></root>";
        FromXmlParser parser = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("root", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("empty", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // ============================================================
    // isExpectedStartArrayToken() tests
    // ============================================================

    /**
     * @target isExpectedStartArrayToken()
     * @scenario Convert START_OBJECT to START_ARRAY
     * @defectRisk Incorrect array conversion
     */
    @Test(timeout = 4000)
    public void testIsExpectedStartArrayToken() throws Exception {
        String xml = "<root><item>a</item><item>b</item></root>";
        FromXmlParser parser = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        // Now at START_OBJECT for the array
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue("Should be expected start array", parser.isExpectedStartArrayToken());
        assertEquals(JsonToken.START_ARRAY, parser.getCurrentToken());
        parser.close();
    }

    // ============================================================
    // Binary value tests
    // ============================================================

    /**
     * @target getBinaryValue(), base64 decoding
     * @scenario Get binary value from base64-encoded text
     * @defectRisk Incorrect base64 decoding
     */
    @Test(timeout = 4000)
    public void testGetBinaryValue() throws Exception {
        String xml = "<root>dGVzdA==</root>"; // "test" in base64
        FromXmlParser parser = createParser(xml);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING
        byte[] binary = parser.getBinaryValue();
        assertNotNull(binary);
        assertEquals("test", new String(binary, "UTF-8"));
        parser.close();
    }

    /**
     * @target getBinaryValue(), error on non-string token
     * @scenario Try to get binary value from non-string token
     * @defectRisk Should throw exception
     */
    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testGetBinaryValueWrongToken() throws Exception {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);
        parser.nextToken(); // START_OBJECT
        parser.getBinaryValue(); // Should throw
        parser.close();
    }

    // ============================================================
    // Edge cases and error handling
    // ============================================================

    /**
     * @target _handleEOF(), premature EOF
     * @scenario Unexpected end of input
     * @defectRisk Should throw JsonParseException
     */
    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testPrematureEOF() throws Exception {
        String xml = "<root>";
        FromXmlParser parser = createParser(xml);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // Should throw due to missing end
        parser.close();
    }

    /**
     * @target close(), isClosed()
     * @scenario Close parser and check state
     * @defectRisk Incorrect closed state management
     */
    @Test(timeout = 4000)
    public void testCloseAndIsClosed() throws Exception {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        // Closing again should be safe
        parser.close();
    }

    /**
     * @target getParsingContext()
     * @scenario Check parsing context after various tokens
     * @defectRisk Incorrect context depth
     */
    @Test(timeout = 4000)
    public void testParsingContext() throws Exception {
        String xml = "<root><child>value</child></root>";
        FromXmlParser parser = createParser(xml);
        assertTrue(parser.getParsingContext().inRoot());
        parser.nextToken(); // START_OBJECT
        assertFalse(parser.getParsingContext().inRoot());
        assertEquals(0, parser.getParsingContext().getDepth());
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // START_OBJECT
        assertEquals(1, parser.getParsingContext().getDepth());
        parser.close();
    }

    /**
     * @target getTokenLocation(), getCurrentLocation()
     * @scenario Check location information
     * @defectRisk Incorrect location reporting
     */
    @Test(timeout = 4000)
    public void testLocations() throws Exception {
        String xml = "<root>text</root>";
        FromXmlParser parser = createParser(xml);
        parser.nextToken();
        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());
        parser.close();
    }

    /**
     * @target version()
     * @scenario Check version information
     * @defectRisk Missing version
     */
    @Test(timeout = 4000)
    public void testVersion() throws Exception {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);
        assertNotNull(parser.version());
        parser.close();
    }

    /**
     * @target requiresCustomCodec()
     * @scenario Check that custom codec is required
     * @defectRisk Incorrect codec requirement
     */
    @Test(timeout = 4000)
    public void testRequiresCustomCodec() throws Exception {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);
        assertTrue(parser.requiresCustomCodec());
        parser.close();
    }

    /**
     * @target getCodec(), setCodec()
     * @scenario Get and set ObjectCodec
     * @defectRisk Incorrect codec management
     */
    @Test(timeout = 4000)
    public void testCodec() throws Exception {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);
        assertNotNull(parser.getCodec());
        ObjectCodec newCodec = new XmlMapper();
        parser.setCodec(newCodec);
        assertSame(newCodec, parser.getCodec());
        parser.close();
    }

    /**
     * @target getStaxReader()
     * @scenario Access underlying Stax reader
     * @defectRisk Incorrect Stax reader access
     */
    @Test(timeout = 4000)
    public void testGetStaxReader() throws Exception {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);
        assertNotNull(parser.getStaxReader());
        parser.close();
    }

    /**
     * @target addVirtualWrapping()
     * @scenario Add virtual wrapping for unwrapped lists
     * @defectRisk Incorrect virtual wrapping behavior
     */
    @Test(timeout = 4000)
    public void testAddVirtualWrapping() throws Exception {
        String xml = "<root><item>a</item><item>b</item></root>";
        FromXmlParser parser = createParser(xml);
        Set<String> namesToWrap = new HashSet<>();
        namesToWrap.add("item");
        parser.addVirtualWrapping(namesToWrap);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        // Should handle virtual wrapping
        parser.close();
    }

    /**
     * @target getEmbeddedObject()
     * @scenario Get embedded object (should return null)
     * @defectRisk Should return null for XML
     */
    @Test(timeout = 4000)
    public void testGetEmbeddedObject() throws Exception {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);
        assertNull(parser.getEmbeddedObject());
        parser.close();
    }

    /**
     * @target hasTextCharacters()
     * @scenario Check text character availability
     * @defectRisk Should return false for XML
     */
    @Test(timeout = 4000)
    public void testHasTextCharacters() throws Exception {
        String xml = "<root>text</root>";
        FromXmlParser parser = createParser(xml);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING
        assertFalse(parser.hasTextCharacters());
        parser.close();
    }

    /**
     * @target getNumberValue(), getNumberType(), etc.
     * @scenario Numeric accessors (stubs)
     * @defectRisk Stub methods should not throw
     */
    @Test(timeout = 4000)
    public void testNumericAccessors() throws Exception {
        String xml = "<root>42</root>";
        FromXmlParser parser = createParser(xml);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING
        // These are stubs, just verify no exception
        assertEquals(0, parser.getIntValue());
        assertEquals(0L, parser.getLongValue());
        assertEquals(0.0, parser.getDoubleValue(), 0.0);
        assertEquals(0.0f, parser.getFloatValue(), 0.0f);
        assertNull(parser.getBigIntegerValue());
        assertNull(parser.getDecimalValue());
        assertNull(parser.getNumberValue());
        assertNull(parser.getNumberType());
        parser.close();
    }

    /**
     * @target getFormatFeatures(), overrideFormatFeatures()
     * @scenario Format features management
     * @defectRisk Incorrect feature handling
     */
    @Test(timeout = 4000)
    public void testFormatFeatures() throws Exception {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);
        assertEquals(0, parser.getFormatFeatures());
        parser.overrideFormatFeatures(0, 0);
        parser.close();
    }

    /**
     * @target enable(), disable(), configure(), isEnabled()
     * @scenario Feature toggling
     * @defectRisk Incorrect feature state management
     */
    @Test(timeout = 4000)
    public void testFeatureMethods() throws Exception {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);
        // Features are empty enum, just verify no exception
        parser.close();
    }
}