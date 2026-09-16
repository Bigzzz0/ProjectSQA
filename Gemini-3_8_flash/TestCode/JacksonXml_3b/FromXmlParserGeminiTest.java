/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser
 *
 * Identified Defect (Defects4J):
 * - Method: nextTextValue()
 * - Fault: When processing XmlTokenStream.XML_ATTRIBUTE_VALUE, nextTextValue() sets _currText and _currToken
 *   to VALUE_STRING, but executes 'break' instead of returning _currText. Consequently, it drops through the
 *   switch statement and returns null instead of the attribute string value ("expected:<7> but was:<null>").
 * - Target Test: testXmlAttributesWithNextTextValue
 *
 * Coverage Partitions & Branch Analysis:
 * Partition A: Core Functional Logic & State Transitions
 *   - nextToken() sequence through START_OBJECT, FIELD_NAME, VALUE_STRING, END_OBJECT, null (EOF).
 *   - nextToken() with nested arrays / elements: isExpectedStartArrayToken() OBJ->Array conversion.
 *   - nextTextValue() for buffered next token (_nextToken != null) vs stream next token.
 *   - nextTextValue() on leaf elements returning empty string ("") on empty end element.
 *   - getCurrentName() and overrideCurrentName() across root, object, and array contexts.
 *   - Token text accessors: getText(), getTextCharacters(), getTextLength(), getTextOffset(), hasTextCharacters().
 *   - getValueAsString() / getValueAsString(defVal) for scalars, field names, and convertible START_OBJECT.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Empty and whitespace-only text elements (_isEmpty evaluation in object / array context).
 *   - addVirtualWrapping() with repeated START_ELEMENT triggers.
 *   - Binary decoding with Base64Variants (MIME) via getBinaryValue().
 *   - Base64 caching validation (second getBinaryValue call reuses _binaryValue).
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - Calling nextTextValue() when an XML attribute value is next in stream. Verifies that the string value
 *     is returned rather than null.
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - Calling getBinaryValue() on non-string token (throws JsonParseException / error).
 *   - Calling getBinaryValue() on corrupted Base64 string (throws JsonParseException).
 *   - getCurrentName() when name is null (IllegalStateException).
 *   - _handleEOF() called with unclosed context (throws JsonParseException).
 *
 * Partition E: Object Lifecycle & Configuration Integrity
 *   - Feature enum: collectDefaults, enabledByDefault, getMask, enabledIn.
 *   - Parser features: enable, disable, isEnabled, configure, getFormatFeatures, overrideFormatFeatures.
 *   - Parser lifecycle: close(), isClosed(), resource managed / AUTO_CLOSE_SOURCE variations.
 *   - Numeric stub accessors (getIntValue, getLongValue, getDoubleValue, etc.).
 *   - Metadata getters: version(), getCodec(), setCodec(), requiresCustomCodec(), getStaxReader().
 */

package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class FromXmlParserGeminiTest {

    private final XmlFactory _xmlFactory = new XmlFactory();

    private FromXmlParser _createParser(String xml) throws IOException {
        return (FromXmlParser) _xmlFactory.createParser(new StringReader(xml));
    }

    /*
     **********************************************************
     * Partition C: Defect-Targeted Branch Zone
     **********************************************************
     */

    /**
     * Targets the known defect in FromXmlParser.nextTextValue():
     * When reading an attribute value (XML_ATTRIBUTE_VALUE), nextTextValue() failed to return
     * the text and instead fell through to return null.
     */
    @Test(timeout = 4000)
    public void testXmlAttributesWithNextTextValue() throws IOException {
        String xml = "<data id=\"7\"><name>Bob</name></data>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("id", parser.getCurrentName());

        // Under defective code, this returns null instead of "7"
        String attrVal = parser.nextTextValue();
        assertEquals("7", attrVal);

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals("Bob", parser.nextTextValue());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    /*
     **********************************************************
     * Partition A: Core Functional Logic & State Transitions
     **********************************************************
     */

    @Test(timeout = 4000)
    public void testBasicTokenStreamTraversal() throws IOException {
        String xml = "<root><child>value</child></root>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("child", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());

        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testNextTextValueForLeafAndEmptyLeaf() throws IOException {
        String xml = "<root><empty></empty><leaf>test</leaf></root>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("empty", parser.getCurrentName());
        // For empty leaf, nextTextValue returns ""
        String emptyVal = parser.nextTextValue();
        assertEquals("", emptyVal);

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("leaf", parser.getCurrentName());
        String leafVal = parser.nextTextValue();
        assertEquals("test", leafVal);

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testIsExpectedStartArrayTokenConversion() throws IOException {
        String xml = "<root><item>A</item><item>B</item></root>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // At START_OBJECT, convert to START_ARRAY
        assertTrue(parser.isExpectedStartArrayToken());
        assertEquals(JsonToken.START_ARRAY, parser.getCurrentToken());
        assertTrue(parser.getParsingContext().inArray());

        // Calling isExpectedStartArrayToken when already in START_ARRAY returns true
        assertTrue(parser.isExpectedStartArrayToken());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("A", parser.getText());
        // In array context, not START_OBJECT anymore
        assertFalse(parser.isExpectedStartArrayToken());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("B", parser.getText());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testIsExpectedStartArrayTokenWithEmptyBufferedObject() throws IOException {
        String xml = "<root/>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken());
        assertEquals(JsonToken.START_ARRAY, parser.getCurrentToken());

        // Buffer was converted from END_OBJECT to END_ARRAY
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testCurrentNameAndOverrideCurrentName() throws IOException {
        String xml = "<root><a attr=\"val\">hello</a></root>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // For START_OBJECT, getCurrentName checks parent name (which is root's name)
        assertEquals("root", parser.getCurrentName());

        parser.overrideCurrentName("newRoot");
        assertEquals("newRoot", parser.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        parser.overrideCurrentName("overriddenA");
        assertEquals("overriddenA", parser.getCurrentName());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("overriddenA", parser.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("attr", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("val", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(FromXmlParser.DEFAULT_UNNAMED_TEXT_PROPERTY, parser.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testTextAccessors() throws IOException {
        String xml = "<root><child>sample</child></root>";
        FromXmlParser parser = _createParser(xml);

        assertNull(parser.getText());
        assertNull(parser.getTextCharacters());
        assertEquals(0, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertFalse(parser.hasTextCharacters());

        parser.nextToken(); // START_OBJECT
        assertEquals("{", parser.getText());

        parser.nextToken(); // FIELD_NAME
        assertEquals("child", parser.getText());
        assertArrayEquals("child".toCharArray(), parser.getTextCharacters());
        assertEquals(5, parser.getTextLength());

        parser.nextToken(); // VALUE_STRING
        assertEquals("sample", parser.getText());
        assertArrayEquals("sample".toCharArray(), parser.getTextCharacters());
        assertEquals(6, parser.getTextLength());

        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetValueAsString() throws IOException {
        String xml = "<root><child attr=\"x\">data</child><plain>simple</plain></root>";
        FromXmlParser parser = _createParser(xml);

        assertNull(parser.getValueAsString());
        assertEquals("default", parser.getValueAsString("default"));

        parser.nextToken(); // START_OBJECT
        assertNull(parser.getValueAsString());

        parser.nextToken(); // FIELD_NAME "child"
        assertEquals("child", parser.getValueAsString());

        parser.nextToken(); // START_OBJECT for child with attr
        // convertToString logic in getValueAsString
        String converted = parser.getValueAsString();
        assertEquals("data", converted);
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken()); // "plain"
        assertEquals("plain", parser.getValueAsString());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken()); // "simple"
        assertEquals("simple", parser.getValueAsString());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // END_OBJECT
        assertEquals("fallback", parser.getValueAsString("fallback"));

        parser.close();
    }

    /*
     **********************************************************
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     **********************************************************
     */

    @Test(timeout = 4000)
    public void testAddVirtualWrapping() throws IOException {
        String xml = "<root><list><item>1</item></list></root>";
        FromXmlParser parser = _createParser(xml);

        Set<String> wrap = new HashSet<String>();
        wrap.add("item");
        parser.addVirtualWrapping(wrap);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken()); // "list"
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken()); // "item"
        // repeated start element simulates unwrapped array wrap
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken()); // "item"
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("1", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testBase64DecodingAndCaching() throws IOException {
        byte[] original = "JacksonXmlBase64".getBytes("UTF-8");
        String encoded = Base64Variants.MIME.encode(original);

        String xml = "<root><data>" + encoded + "</data></root>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] decoded1 = parser.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(original, decoded1);

        // Second call should return cached _binaryValue
        byte[] decoded2 = parser.getBinaryValue(Base64Variants.MIME);
        assertSame(decoded1, decoded2);

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testEmptyAndWhitespaceInArrayContext() throws IOException {
        String xml = "<root><item>   </item></root>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken()); // convert root to array

        // Whitespace in array leaf converts to empty object
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testCustomXMLTextElementName() throws IOException {
        String xml = "<root id=\"123\">MixedText</root>";
        FromXmlParser parser = _createParser(xml);
        parser.setXMLTextElementName("value");

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("id", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("123", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("value", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("MixedText", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    /*
     **********************************************************
     * Partition D: Exception & Defensive Guard Paths
     **********************************************************
     */

    @Test(timeout = 4000)
    public void testGetBinaryValueInvalidTokenThrowsException() throws IOException {
        String xml = "<root><child>value</child></root>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        try {
            parser.getBinaryValue(Base64Variants.MIME);
            fail("Expected JsonParseException when calling getBinaryValue on START_OBJECT");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("not VALUE_STRING"));
        }
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueCorruptBase64ThrowsException() throws IOException {
        String xml = "<root><data>!NotValidBase64!</data></root>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        try {
            parser.getBinaryValue(Base64Variants.MIME);
            fail("Expected JsonParseException on invalid base64 content");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("Failed to decode VALUE_STRING as base64"));
        }
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetCurrentNameWhenNullThrowsException() throws IOException {
        String xml = "<root/>";
        FromXmlParser parser = _createParser(xml);
        // parser before nextToken() is in root context with null name and null token
        try {
            parser.getCurrentName();
            fail("Expected IllegalStateException for missing name");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Missing name"));
        }
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHandleEOFThrowsExceptionWhenUnclosed() throws IOException {
        String xml = "<root><unclosed>";
        FromXmlParser parser = _createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        try {
            // Next token attempts to read beyond unexpected EOF while context is not in root
            parser._handleEOF();
            fail("Expected JsonParseException when EOF encountered in open context");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("expected close marker"));
        }
        parser.close();
    }

    /*
     **********************************************************
     * Partition E: Object Lifecycle, Features & Contract Integrity
     **********************************************************
     */

    @Test(timeout = 4000)
    public void testFormatFeaturesAndConfig() throws IOException {
        String xml = "<root/>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(0, FromXmlParser.Feature.collectDefaults());
        assertEquals(0, parser.getFormatFeatures());

        // overrideFormatFeatures
        parser.overrideFormatFeatures(0xFF, 0x0F);
        assertEquals(0x0F, parser.getFormatFeatures());

        parser.overrideFormatFeatures(0x00, 0x0F);
        assertEquals(0, parser.getFormatFeatures());

        parser.close();
    }

    @Test(timeout = 4000)
    public void testMetadataAndCodecSupport() throws IOException {
        String xml = "<root/>";
        FromXmlParser parser = _createParser(xml);

        Version v = parser.version();
        assertNotNull(v);
        assertFalse(v.isUnknownVersion());

        assertTrue(parser.requiresCustomCodec());
        assertNull(parser.getEmbeddedObject());
        assertNotNull(parser.getStaxReader());
        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());

        ObjectCodec mockCodec = new XmlMapper();
        parser.setCodec(mockCodec);
        assertSame(mockCodec, parser.getCodec());

        parser.close();
        assertTrue(parser.isClosed());

        // Repeated close should be no-op
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testNumericStubMethodsReturnDefaults() throws IOException {
        String xml = "<root/>";
        FromXmlParser parser = _createParser(xml);

        assertNull(parser.getBigIntegerValue());
        assertNull(parser.getDecimalValue());
        assertEquals(0.0, parser.getDoubleValue(), 0.00001);
        assertEquals(0.0f, parser.getFloatValue(), 0.00001f);
        assertEquals(0, parser.getIntValue());
        assertEquals(0L, parser.getLongValue());
        assertNull(parser.getNumberType());
        assertNull(parser.getNumberValue());

        parser.close();
    }

    @Test(timeout = 4000)
    public void testAutoCloseSourceFeature() throws IOException {
        String xml = "<root><a/></root>";
        FromXmlParser parser = _createParser(xml);

        parser.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        assertTrue(parser.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));

        parser.nextToken();
        parser.close();
        assertTrue(parser.isClosed());
    }
}