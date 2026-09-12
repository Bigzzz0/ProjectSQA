package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class FromXmlParserClaudeTest
{
    // ------------------------------------------------------------------
    // POJOs used for defect-targeted nested/unwrapped list tests
    // ------------------------------------------------------------------

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

    static class ListHolder {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> tag;
    }

    static class WrappedListHolder {
        @JacksonXmlElementWrapper(localName = "tags")
        @JacksonXmlProperty(localName = "tag")
        public List<String> tags;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private FromXmlParser createParser(String xml) throws IOException {
        XmlFactory f = new XmlFactory();
        return (FromXmlParser) f.createParser(xml);
    }

    // ------------------------------------------------------------------
    // DEFECT-TARGETED TESTS (Issue #180 / NestedUnwrappedLists)
    // ------------------------------------------------------------------

    /**
     * @target FromXmlParser#nextToken() handling of empty leaf elements within nested unwrapped lists
     * @scenario XML with a folder containing a name and an empty (self-closing) item element,
     *           deserialized into a POJO graph using unwrapped collections
     * @defectRisk Known defect #180: empty leaf element inside nested unwrapped list context causes
     *             the outer folder list to be lost (size 0 instead of 1)
     */
    @Test(timeout = 4000)
    public void testNestedUnwrappedListsWithEmptyElement_Issue180() throws Exception {
        XmlMapper mapper = new XmlMapper();
        String xml = "<Root><folder><name>test</name><item/></folder></Root>";
        Root root = mapper.readValue(xml, Root.class);
        assertNotNull(root.folder);
        assertEquals(1, root.folder.size());
        assertEquals("test", root.folder.get(0).name);
    }

    /**
     * @target FromXmlParser#nextToken() nested unwrapped list traversal across multiple folder elements
     * @scenario Two folder elements where the first has no item children at all (empty list)
     * @defectRisk Known defect: folder list count silently drops to 0 due to XML_TEXT / END_OBJECT handling
     *             within nested unwrapped list parsing contexts
     */
    @Test(timeout = 4000)
    public void testNestedWithEmptyElementInMiddle() throws Exception {
        XmlMapper mapper = new XmlMapper();
        String xml = "<Root>"
                + "<folder><name>empty-folder</name></folder>"
                + "<folder><name>real-folder</name><item><id>x1</id></item></folder>"
                + "</Root>";
        Root root = mapper.readValue(xml, Root.class);
        assertNotNull(root.folder);
        assertEquals(2, root.folder.size());
        assertEquals("empty-folder", root.folder.get(0).name);
        assertEquals("real-folder", root.folder.get(1).name);
    }

    /**
     * @target FromXmlParser#nextToken() handling of empty self-closing child inside nested unwrapped list
     * @scenario Folder containing name plus an empty (but present, non-self-closing) item element
     * @defectRisk Known defect: item list ends up empty/lost or the whole folder record disappears
     *             (expected:<1> but was:<0>)
     */
    @Test(timeout = 4000)
    public void testNestedWithEmptyItemElement() throws Exception {
        XmlMapper mapper = new XmlMapper();
        String xml = "<Root><folder><name>f</name><item></item></folder></Root>";
        Root root = mapper.readValue(xml, Root.class);
        assertNotNull(root.folder);
        assertEquals(1, root.folder.size());
        assertNotNull(root.folder.get(0).item);
    }

    /**
     * @target FromXmlParser#nextToken() nested unwrapped list with multiple fully populated folders
     * @scenario Two folder elements each with a nested unwrapped item list containing one item
     * @defectRisk Regression guard: ensures normal (non-empty) nested unwrapped list case still works
     */
    @Test(timeout = 4000)
    public void testNestedUnwrappedListsMultipleFolders() throws Exception {
        XmlMapper mapper = new XmlMapper();
        String xml = "<Root><folder><name>f1</name><item><id>a</id></item></folder>"
                + "<folder><name>f2</name><item><id>b</id></item></folder></Root>";
        Root root = mapper.readValue(xml, Root.class);
        assertNotNull(root.folder);
        assertEquals(2, root.folder.size());
        assertEquals(1, root.folder.get(0).item.size());
        assertEquals("a", root.folder.get(0).item.get(0).id);
        assertEquals("b", root.folder.get(1).item.get(0).id);
    }

    // ------------------------------------------------------------------
    // Low-level token stream tests
    // ------------------------------------------------------------------

    /**
     * @target FromXmlParser#nextToken() basic object field traversal
     * @scenario Simple root element with two leaf child elements
     * @defectRisk Regression guard for basic FIELD_NAME/VALUE_STRING/END_OBJECT sequencing
     */
    @Test(timeout = 4000)
    public void testBasicObjectParsing() throws Exception {
        String xml = "<root><a>1</a><b>2</b></root>";
        FromXmlParser p = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("1", p.getText());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("2", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    /**
     * @target FromXmlParser#nextToken() attribute + text mixed-content handling with default text element name
     * @scenario Root element with an attribute and direct text content (no child elements)
     * @defectRisk Verifies XML_ATTRIBUTE_NAME/VALUE and XML_TEXT->FIELD_NAME("") transformation
     */
    @Test(timeout = 4000)
    public void testMixedContentWithAttributeAndText() throws Exception {
        String xml = "<root attr=\"a\">text</root>";
        FromXmlParser p = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("attr", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("a", p.getText());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("text", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    /**
     * @target FromXmlParser#nextToken() empty (self-closing) element handling
     * @scenario Root element containing a self-closing child element
     * @defectRisk Verifies [dataformat-xml#180] fix: empty leaf exposed as VALUE_NULL, not swallowed
     */
    @Test(timeout = 4000)
    public void testEmptyElementYieldsNull() throws Exception {
        String xml = "<root><child/></root>";
        FromXmlParser p = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("child", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    /**
     * @target FromXmlParser#nextToken() XML_ATTRIBUTE_NAME branch when _mayBeLeaf is true
     * @scenario Child element that could be a leaf but carries an attribute, forcing conversion to START_OBJECT
     * @defectRisk Verifies correct promotion of leaf candidate to object when attributes are present
     */
    @Test(timeout = 4000)
    public void testLeafElementWithAttributeBecomesObject() throws Exception {
        String xml = "<root><a attr=\"x\">text</a></root>";
        FromXmlParser p = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("attr", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("x", p.getText());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("text", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    /**
     * @target FromXmlParser#setXMLTextElementName(String)
     * @scenario Custom text-element pseudo-property name configured before parsing mixed content
     * @defectRisk Ensures _cfgNameForTextElement override is honored instead of default ""
     */
    @Test(timeout = 4000)
    public void testCustomTextElementName() throws Exception {
        String xml = "<root attr=\"a\">text</root>";
        FromXmlParser p = createParser(xml);
        p.setXMLTextElementName("value");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("attr", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("value", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("text", p.getText());
        p.close();
    }

    /**
     * @target FromXmlParser#nextTextValue() leaf text shortcut
     * @scenario Simple leaf child element with text content
     * @defectRisk Ensures nextTextValue() returns text directly without extra FIELD_NAME roundtrip
     */
    @Test(timeout = 4000)
    public void testNextTextValueLeaf() throws Exception {
        String xml = "<root><a>hello</a></root>";
        FromXmlParser p = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        String text = p.nextTextValue();
        assertEquals("hello", text);
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    /**
     * @target FromXmlParser#nextTextValue() empty element special-case behavior
     * @scenario Leaf child element with no text content (empty)
     * @defectRisk nextTextValue() must return "" (not null) for empty leaf, distinct from nextToken()
     */
    @Test(timeout = 4000)
    public void testNextTextValueEmptyElement() throws Exception {
        String xml = "<root><a></a></root>";
        FromXmlParser p = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        String text = p.nextTextValue();
        assertEquals("", text);
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    /**
     * @target FromXmlParser#isExpectedStartArrayToken() conversion branch
     * @scenario Current token is START_OBJECT, method should convert to START_ARRAY and update context
     * @defectRisk Ensures array-vs-object ambiguity is resolved correctly for unwrapped array bindings
     */
    @Test(timeout = 4000)
    public void testIsExpectedStartArrayTokenTrue() throws Exception {
        String xml = "<root><item>1</item></root>";
        FromXmlParser p = createParser(xml);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        boolean isArray = p.isExpectedStartArrayToken();
        assertTrue(isArray);
        assertEquals(JsonToken.START_ARRAY, p.getCurrentToken());
        p.close();
    }

    /**
     * @target FromXmlParser#isExpectedStartArrayToken() negative branch
     * @scenario Current token is FIELD_NAME (not START_OBJECT/START_ARRAY)
     * @defectRisk Ensures method correctly returns false without mutating context when not applicable
     */
    @Test(timeout = 4000)
    public void testIsExpectedStartArrayTokenFalseForFieldName() throws Exception {
        String xml = "<root><item>1</item></root>";
        FromXmlParser p = createParser(xml);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME "item"
        assertFalse(p.isExpectedStartArrayToken());
        p.close();
    }

    /**
     * @target FromXmlParser numeric accessor stub methods (getIntValue, getLongValue, getDoubleValue, etc.)
     * @scenario Parser positioned at a VALUE_STRING token containing digits
     * @defectRisk These methods are unimplemented stubs (return 0/null); verify no exception & stable stub values
     */
    @Test(timeout = 4000)
    public void testNumericStubMethods() throws Exception {
        String xml = "<root><a>123</a></root>";
        FromXmlParser p = createParser(xml);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME a
        p.nextToken(); // VALUE_STRING 123
        assertEquals(0, p.getIntValue());
        assertEquals(0L, p.getLongValue());
        assertEquals(0.0, p.getDoubleValue(), 0.0001);
        assertEquals(0.0f, p.getFloatValue(), 0.0001f);
        assertNull(p.getBigIntegerValue());
        assertNull(p.getDecimalValue());
        assertNull(p.getNumberType());
        assertNull(p.getNumberValue());
        p.close();
    }

    /**
     * @target FromXmlParser#getValueAsString(String) FIELD_NAME and VALUE_STRING branches
     * @scenario Parser positioned first at FIELD_NAME then at VALUE_STRING
     * @defectRisk Ensures correct text extraction for both token kinds without defaulting
     */
    @Test(timeout = 4000)
    public void testGetValueAsStringForFieldNameAndValueString() throws Exception {
        String xml = "<root><a>hello</a></root>";
        FromXmlParser p = createParser(xml);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME a
        assertEquals("a", p.getValueAsString("default"));
        p.nextToken(); // VALUE_STRING hello
        assertEquals("hello", p.getValueAsString("default"));
        p.close();
    }

    /**
     * @target FromXmlParser#getValueAsString(String) null-token branch
     * @scenario Parser has not yet produced any current token
     * @defectRisk Ensures null is returned (not defValue) when currToken is null, per implementation contract
     */
    @Test(timeout = 4000)
    public void testGetValueAsStringDefaultForNullToken() throws Exception {
        String xml = "<root/>";
        FromXmlParser p = createParser(xml);
        assertNull(p.getValueAsString("default"));
        p.close();
    }

    /**
     * @target FromXmlParser#getTextCharacters(), getTextLength(), getTextOffset(), hasTextCharacters()
     * @scenario Parser positioned at a VALUE_STRING token with known text
     * @defectRisk Ensures character array conversion, length, offset(always 0) and hasTextCharacters(always false)
     */
    @Test(timeout = 4000)
    public void testTextCharactersLengthOffset() throws Exception {
        String xml = "<root><a>hello</a></root>";
        FromXmlParser p = createParser(xml);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME a
        p.nextToken(); // VALUE_STRING hello
        assertEquals("hello", p.getText());
        char[] chars = p.getTextCharacters();
        assertArrayEquals("hello".toCharArray(), chars);
        assertEquals(5, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        assertFalse(p.hasTextCharacters());
        p.close();
    }

    /**
     * @target FromXmlParser#getTextCharacters()/getTextLength() when current token is null
     * @scenario Parser fully exhausted (past END_OBJECT, next() returns null)
     * @defectRisk Ensures null-safe behavior: null char array and length 0 rather than NPE
     */
    @Test(timeout = 4000)
    public void testTextCharactersNullAtEnd() throws Exception {
        String xml = "<root/>";
        FromXmlParser p = createParser(xml);
        while (p.nextToken() != null) {
            // drain
        }
        assertNull(p.getTextCharacters());
        assertEquals(0, p.getTextLength());
        p.close();
    }

    /**
     * @target FromXmlParser#getBinaryValue(Base64Variant)
     * @scenario VALUE_STRING token containing base64-encoded bytes
     * @defectRisk Ensures correct base64 decoding and caching of decoded byte[] value
     */
    @Test(timeout = 4000)
    public void testGetBinaryValue() throws Exception {
        byte[] data = "Hello".getBytes("UTF-8");
        String encoded = Base64Variants.getDefaultVariant().encode(data);
        String xml = "<root><a>" + encoded + "</a></root>";
        FromXmlParser p = createParser(xml);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME a
        p.nextToken(); // VALUE_STRING encoded
        byte[] decoded = p.getBinaryValue(Base64Variants.getDefaultVariant());
        assertArrayEquals(data, decoded);
        p.close();
    }

    /**
     * @target FromXmlParser#getBinaryValue(Base64Variant) error branch
     * @scenario Current token is START_OBJECT (not VALUE_STRING/VALUE_EMBEDDED_OBJECT)
     * @defectRisk Ensures proper JsonParseException is thrown for invalid token state
     */
    @Test(timeout = 4000)
    public void testGetBinaryValueThrowsOnWrongToken() throws Exception {
        String xml = "<root><a>1</a></root>";
        FromXmlParser p = createParser(xml);
        p.nextToken(); // START_OBJECT
        try {
            p.getBinaryValue(Base64Variants.getDefaultVariant());
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            // expected
        }
        p.close();
    }

    /**
     * @target FromXmlParser#close() and isClosed()
     * @scenario Parser closed once, then closed again (idempotent no-op)
     * @defectRisk Ensures resource release logic runs exactly once and isClosed() reflects state correctly
     */
    @Test(timeout = 4000)
    public void testCloseAndIsClosed() throws Exception {
        String xml = "<root><a>1</a></root>";
        FromXmlParser p = createParser(xml);
        assertFalse(p.isClosed());
        p.close();
        assertTrue(p.isClosed());
        p.close(); // no-op second call
        assertTrue(p.isClosed());
    }

    /**
     * @target FromXmlParser#getEmbeddedObject()
     * @scenario Any parser state (POJO embedding not supported by XML backend)
     * @defectRisk Ensures method always returns null, never throws
     */
    @Test(timeout = 4000)
    public void testGetEmbeddedObjectReturnsNull() throws Exception {
        String xml = "<root/>";
        FromXmlParser p = createParser(xml);
        p.nextToken();
        assertNull(p.getEmbeddedObject());
        p.close();
    }

    /**
     * @target FromXmlParser#overrideCurrentName(String) with START_OBJECT "off-by-one" handling
     * @scenario Current token is START_OBJECT; override should apply to the parent context
     * @defectRisk Ensures name override lands on correct context level, not the newly pushed child
     */
    @Test(timeout = 4000)
    public void testOverrideCurrentNameEffect() throws Exception {
        String xml = "<root><a>1</a></root>";
        FromXmlParser p = createParser(xml);
        p.nextToken(); // START_OBJECT (root's content object)
        p.overrideCurrentName("overridden");
        assertEquals("overridden", p.getParsingContext().getParent().getCurrentName());
        p.close();
    }

    /**
     * @target FromXmlParser#getCurrentName() sanity-check branch
     * @scenario No token has been read yet; parsing context has no name set
     * @defectRisk Ensures IllegalStateException is thrown rather than returning null silently
     */
    @Test(timeout = 4000)
    public void testGetCurrentNameThrowsWhenNull() throws Exception {
        String xml = "<root/>";
        FromXmlParser p = createParser(xml);
        try {
            p.getCurrentName();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        p.close();
    }

    /**
     * @target FromXmlParser#getTokenLocation() and getCurrentLocation()
     * @scenario Parser positioned at first token after a nextToken() call
     * @defectRisk Ensures location objects are always non-null and delegate to underlying XmlTokenStream
     */
    @Test(timeout = 4000)
    public void testTokenAndCurrentLocation() throws Exception {
        String xml = "<root><a>1</a></root>";
        FromXmlParser p = createParser(xml);
        p.nextToken();
        assertNotNull(p.getTokenLocation());
        assertNotNull(p.getCurrentLocation());
        p.close();
    }

    /**
     * @target FromXmlParser#getStaxReader()
     * @scenario Freshly created parser instance
     * @defectRisk Ensures direct access to underlying XMLStreamReader is exposed correctly
     */
    @Test(timeout = 4000)
    public void testGetStaxReader() throws Exception {
        String xml = "<root/>";
        FromXmlParser p = createParser(xml);
        assertNotNull(p.getStaxReader());
        p.close();
    }

    /**
     * @target FromXmlParser#requiresCustomCodec(), version(), getCodec()/setCodec()
     * @scenario Basic accessor round-trip with an ObjectMapper as codec
     * @defectRisk Ensures codec assignment/retrieval works and version() never returns null
     */
    @Test(timeout = 4000)
    public void testCodecAndVersion() throws Exception {
        String xml = "<root/>";
        FromXmlParser p = createParser(xml);
        assertTrue(p.requiresCustomCodec());
        assertNotNull(p.version());
        ObjectMapper mapper = new ObjectMapper();
        p.setCodec(mapper);
        assertSame(mapper, p.getCodec());
        p.close();
    }

    /**
     * @target FromXmlParser.Feature#collectDefaults()
     * @scenario Feature enum currently declares no constants
     * @defectRisk Ensures collectDefaults() safely returns 0 for an empty enum rather than throwing
     */
    @Test(timeout = 4000)
    public void testFeatureCollectDefaultsIsZero() {
        assertEquals(0, FromXmlParser.Feature.collectDefaults());
    }

    /**
     * @target FromXmlParser#getFormatFeatures()
     * @scenario Freshly created parser with default feature flags
     * @defectRisk Ensures default format feature bitmask is 0 (no features enabled by default)
     */
    @Test(timeout = 4000)
    public void testGetFormatFeaturesDefaultZero() throws Exception {
        String xml = "<root/>";
        FromXmlParser p = createParser(xml);
        assertEquals(0, p.getFormatFeatures());
        p.close();
    }

    /**
     * @target FromXmlParser#overrideFormatFeatures(int, int)
     * @scenario Overriding format features bitmask with mask=1, value=1
     * @defectRisk Ensures bitmask arithmetic correctly applies values under mask
     */
    @Test(timeout = 4000)
    public void testOverrideFormatFeatures() throws Exception {
        String xml = "<root/>";
        FromXmlParser p = createParser(xml);
        p.overrideFormatFeatures(1, 1);
        assertEquals(1, p.getFormatFeatures());
        p.close();
    }

    /**
     * @target FromXmlParser#nextToken() combined with inherited skipChildren()
     * @scenario Skipping a nested object's children then continuing to read sibling fields
     * @defectRisk Ensures parsing context correctly unwinds to sibling level after skipChildren()
     */
    @Test(timeout = 4000)
    public void testSkipChildren() throws Exception {
        String xml = "<root><child><a>1</a><b>2</b></child><after>done</after></root>";
        FromXmlParser p = createParser(xml);
        p.nextToken(); // START_OBJECT root content
        p.nextToken(); // FIELD_NAME child
        p.nextToken(); // START_OBJECT child content
        p.skipChildren();
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());
        p.nextToken(); // FIELD_NAME after
        assertEquals("after", p.getCurrentName());
        p.nextToken(); // VALUE_STRING done
        assertEquals("done", p.getText());
        p.close();
    }

    /**
     * @target FromXmlParser#addVirtualWrapping(Set)
     * @scenario Registering a virtual-wrap name set for unwrapped array emulation
     * @defectRisk Ensures the wrap-name registration does not break normal FIELD_NAME emission
     */
    @Test(timeout = 4000)
    public void testAddVirtualWrapping() throws Exception {
        String xml = "<root><item>1</item><item>2</item></root>";
        FromXmlParser p = createParser(xml);
        p.nextToken(); // START_OBJECT
        Set<String> namesToWrap = new HashSet<String>();
        namesToWrap.add("item");
        p.addVirtualWrapping(namesToWrap);
        p.nextToken(); // FIELD_NAME item
        assertEquals("item", p.getCurrentName());
        p.close();
    }

    /**
     * @target FromXmlParser#getText() default branch using JsonToken.asString()
     * @scenario Current token is START_OBJECT
     * @defectRisk Ensures getText() falls back correctly to token's literal string representation
     */
    @Test(timeout = 4000)
    public void testGetTextForStartObjectToken() throws Exception {
        String xml = "<root><a>1</a></root>";
        FromXmlParser p = createParser(xml);
        p.nextToken(); // START_OBJECT
        String text = p.getText();
        assertEquals("{", text);
        p.close();
    }

    /**
     * @target FromXmlParser#getText() default branch for VALUE_NULL literal
     * @scenario Empty child element produces a VALUE_NULL token
     * @defectRisk Ensures getText() returns the literal "null" string for VALUE_NULL via asString()
     */
    @Test(timeout = 4000)
    public void testGetTextForValueNullToken() throws Exception {
        String xml = "<root><child/></root>";
        FromXmlParser p = createParser(xml);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME child
        JsonToken t = p.nextToken(); // VALUE_NULL
        assertEquals(JsonToken.VALUE_NULL, t);
        assertEquals("null", p.getText());
        p.close();
    }

    /**
     * @target FromXmlParser#getText() null-token branch
     * @scenario No token has been consumed yet (currToken is null)
     * @defectRisk Ensures getText() returns null rather than throwing NullPointerException
     */
    @Test(timeout = 4000)
    public void testGetTextNullWhenNoCurrentToken() throws Exception {
        String xml = "<root/>";
        FromXmlParser p = createParser(xml);
        assertNull(p.getText());
        p.close();
    }

    /**
     * @target FromXmlParser unwrapped collection deserialization (via XmlMapper) for List<String>
     * @scenario Multiple repeated sibling elements bound to an unwrapped List<String> field
     * @defectRisk Ensures FIELD_NAME/VALUE_STRING repetition inside array context yields all items
     */
    @Test(timeout = 4000)
    public void testUnwrappedListOfStrings() throws Exception {
        XmlMapper mapper = new XmlMapper();
        String xml = "<ListHolder><tag>a</tag><tag>b</tag><tag>c</tag></ListHolder>";
        ListHolder h = mapper.readValue(xml, ListHolder.class);
        assertNotNull(h.tag);
        assertEquals(3, h.tag.size());
        assertEquals("a", h.tag.get(0));
        assertEquals("c", h.tag.get(2));
    }

    /**
     * @target FromXmlParser unwrapped collection deserialization edge case with single element
     * @scenario Single repeated element still bound to a List (not collapsed to scalar)
     * @defectRisk Ensures single-occurrence unwrapped list still produces a List of size 1
     */
    @Test(timeout = 4000)
    public void testUnwrappedListSingleItem() throws Exception {
        XmlMapper mapper = new XmlMapper();
        String xml = "<ListHolder><tag>only</tag></ListHolder>";
        ListHolder h = mapper.readValue(xml, ListHolder.class);
        assertNotNull(h.tag);
        assertEquals(1, h.tag.size());
        assertEquals("only", h.tag.get(0));
    }

    /**
     * @target FromXmlParser wrapped collection deserialization (via XmlMapper) for List<String>
     * @scenario Explicit wrapper element containing repeated item elements
     * @defectRisk Ensures wrapped-array START_ARRAY/END_ARRAY handling functions correctly (contrast to unwrapped)
     */
    @Test(timeout = 4000)
    public void testWrappedList() throws Exception {
        XmlMapper mapper = new XmlMapper();
        String xml = "<WrappedListHolder><tags><tag>x</tag><tag>y</tag></tags></WrappedListHolder>";
        WrappedListHolder h = mapper.readValue(xml, WrappedListHolder.class);
        assertNotNull(h.tags);
        assertEquals(2, h.tags.size());
        assertEquals("x", h.tags.get(0));
        assertEquals("y", h.tags.get(1));
    }
}