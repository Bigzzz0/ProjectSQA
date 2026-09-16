package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.dataformat.xml.deser.XmlTokenStream
 *
 * ---------------------------------------------------------------------------------------------------------
 * Branch / Condition                                        | Targeted By Test Method
 * ---------------------------------------------------------------------------------------------------------
 * Defect: Mixed content discarded before child tag          | testDefectMixedContentTextBeforeChildTag
 *   (XmlTextTest::testMixedContent - expected:<27> but 0)   |
 * Constructor: xmlReader not at START_ELEMENT               | testConstructorThrowsOnNonStartElement
 * Constructor: standard initialization                      | testSimpleDocumentTraversal
 * next(): XML_START_ELEMENT -> attributes traversal         | testAttributeTokensTraversal
 * next(): XML_ATTRIBUTE_VALUE -> XML_ATTRIBUTE_NAME / TEXT  | testAttributeTokensTraversal
 * next(): XML_TEXT -> XML_END_ELEMENT                       | testSimpleDocumentTraversal
 * next(): XML_END -> XML_END terminal idempotency           | testXmlEndTokenIdempotency
 * next(): XMLStreamConstants.END_DOCUMENT -> XML_END        | testSimpleDocumentTraversal
 * next(): CDATA and Characters combination                  | testCollectUntilTagWithCDataAndComments
 * skipAttributes(): from XML_ATTRIBUTE_NAME                 | testSkipAttributesFromAttributeName
 * skipAttributes(): from XML_START_ELEMENT                  | testSkipAttributesFromStartElement
 * skipAttributes(): from XML_TEXT                           | testSkipAttributesFromText
 * skipAttributes(): invalid state exception                 | testSkipAttributesThrowsOnInvalidState
 * convertToString(): valid with content                     | testConvertToStringWithAttributeAndText
 * convertToString(): valid empty tag                        | testConvertToStringEmptyTag
 * convertToString(): invalid state / index != 0             | testConvertToStringInvalidConditions
 * convertToString(): tag has child elements                 | testConvertToStringWithChildElementFails
 * skipEndElement(): success                                 | testSkipEndElementSuccess
 * skipEndElement(): failure (expected END_ELEMENT)          | testSkipEndElementFailure
 * repeatStartElement(): valid REPLAY_START_DUP / DELAYED    | testRepeatStartElementAndDelayedReplay
 * repeatStartElement(): REPLAY_END on matching wrapper      | testRepeatStartElementReplayEnd
 * repeatStartElement(): invalid state exception             | testRepeatStartElementThrowsOnInvalidState
 * Locations, Close, and Diagnostics                         | testLocationsAndCloseAndToString
 * ---------------------------------------------------------------------------------------------------------
 */
public class XmlTokenStreamGeminiTest {

    private XmlTokenStream createStream(String xml) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
        while (reader.getEventType() != XMLStreamConstants.START_ELEMENT) {
            reader.next();
        }
        return new XmlTokenStream(reader, xml);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Defect)
    // =========================================================================

    /**
     * Targets defect in XmlTokenStream._next():
     * When mixed text appears before a child element (e.g. "<root>27<child>foo</child></root>"),
     * _collectUntilTag() collects "27", but seeing START_ELEMENT for <child>, the defective code
     * unconditionally returns _initStartElement() instead of returning the collected mixed text.
     * This causes deserialization of mixed content (@XmlText) to receive 0 instead of 27.
     */
    @Test(timeout = 4000)
    public void testDefectMixedContentTextBeforeChildTag() throws Exception {
        String xml = "<root>27<child>foo</child></root>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        assertEquals("root", stream.getLocalName());

        // In correct behavior, the stream must yield XML_TEXT ("27") before the child element
        int nextToken = stream.next();
        assertEquals("Mixed content preceding child tag must be returned as XML_TEXT",
                XmlTokenStream.XML_TEXT, nextToken);
        assertEquals("27", stream.getText());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSimpleDocumentTraversal() throws Exception {
        String xml = "<root>Hello World</root>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        assertEquals("root", stream.getLocalName());
        assertFalse(stream.hasAttributes());

        int token = stream.next();
        assertEquals(XmlTokenStream.XML_TEXT, token);
        assertEquals("Hello World", stream.getText());

        token = stream.next();
        assertEquals(XmlTokenStream.XML_END_ELEMENT, token);
        assertEquals("root", stream.getLocalName());

        token = stream.next();
        assertEquals(XmlTokenStream.XML_END, token);
    }

    @Test(timeout = 4000)
    public void testAttributeTokensTraversal() throws Exception {
        String xml = "<item id=\"101\" code=\"XYZ\">content</item>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        assertTrue(stream.hasAttributes());

        // First attribute name
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());
        assertEquals("id", stream.getLocalName());

        // First attribute value
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, stream.next());
        assertEquals("101", stream.getText());

        // Second attribute name
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());
        assertEquals("code", stream.getLocalName());

        // Second attribute value
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, stream.next());
        assertEquals("XYZ", stream.getText());

        // Following text
        assertEquals(XmlTokenStream.XML_TEXT, stream.next());
        assertEquals("content", stream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next());
    }

    @Test(timeout = 4000)
    public void testXmlEndTokenIdempotency() throws Exception {
        String xml = "<root/>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next());
        assertEquals(XmlTokenStream.XML_END, stream.next());
        // Subsequent calls to next() after XML_END should continue returning XML_END
        assertEquals(XmlTokenStream.XML_END, stream.next());
    }

    @Test(timeout = 4000)
    public void testCollectUntilTagWithCDataAndComments() throws Exception {
        String xml = "<root><![CDATA[Chunk1]]><!-- Ignored Comment --><![CDATA[Chunk2]]></root>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        int token = stream.next();
        assertEquals(XmlTokenStream.XML_TEXT, token);
        assertEquals("Chunk1Chunk2", stream.getText());
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next());
    }

    @Test(timeout = 4000)
    public void testSkipEndElementSuccess() throws Exception {
        String xml = "<root></root>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        stream.skipEndElement();
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testSkipEndElementFailure() throws Exception {
        String xml = "<root><child/></root>";
        XmlTokenStream stream = createStream(xml);

        try {
            stream.skipEndElement();
            fail("Expected skipEndElement to throw IOException when next token is not END_ELEMENT");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Expected END_ELEMENT"));
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Virtual Wrapping
    // =========================================================================

    @Test(timeout = 4000)
    public void testSkipAttributesFromAttributeName() throws Exception {
        String xml = "<root attr=\"val\">text</root>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());
        assertEquals("attr", stream.getLocalName());

        stream.skipAttributes();
        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        assertFalse(stream.hasAttributes());

        // Should directly proceed to text
        assertEquals(XmlTokenStream.XML_TEXT, stream.next());
        assertEquals("text", stream.getText());
    }

    @Test(timeout = 4000)
    public void testSkipAttributesFromStartElement() throws Exception {
        String xml = "<root>text</root>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        // skipAttributes when already at START_ELEMENT should be a safe no-op
        stream.skipAttributes();
        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testSkipAttributesFromText() throws Exception {
        String xml = "<root>text</root>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_TEXT, stream.next());
        // skipAttributes when at XML_TEXT should be a safe no-op
        stream.skipAttributes();
        assertEquals(XmlTokenStream.XML_TEXT, stream.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testConvertToStringWithAttributeAndText() throws Exception {
        String xml = "<elem id=\"1\">Sample Text</elem>";
        XmlTokenStream stream = createStream(xml);

        // Move to XML_ATTRIBUTE_NAME where index == 0
        int token = stream.next();
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, token);

        String text = stream.convertToString();
        assertEquals("Sample Text", text);
        assertEquals(XmlTokenStream.XML_TEXT, stream.getCurrentToken());
        assertEquals("Sample Text", stream.getText());
        assertEquals("elem", stream.getLocalName());
    }

    @Test(timeout = 4000)
    public void testConvertToStringEmptyTag() throws Exception {
        String xml = "<elem id=\"1\"></elem>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());
        String text = stream.convertToString();
        assertEquals("", text);
        assertEquals(XmlTokenStream.XML_TEXT, stream.getCurrentToken());
        assertEquals("", stream.getText());
    }

    @Test(timeout = 4000)
    public void testConvertToStringInvalidConditions() throws Exception {
        String xml = "<elem id=\"1\" num=\"2\">Text</elem>";
        XmlTokenStream stream = createStream(xml);

        // State is XML_START_ELEMENT, not XML_ATTRIBUTE_NAME
        assertNull(stream.convertToString());

        // Move to first attribute
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());
        // Move to first attribute value
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, stream.next());
        // Move to second attribute (nextAttributeIndex > 0)
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());

        assertNull(stream.convertToString());
    }

    @Test(timeout = 4000)
    public void testConvertToStringWithChildElementFails() throws Exception {
        String xml = "<elem id=\"1\"><child/></elem>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());
        // Tags with child elements cannot be converted to simple string
        assertNull(stream.convertToString());
    }

    @Test(timeout = 4000)
    public void testRepeatStartElementAndDelayedReplay() throws Exception {
        String xml = "<wrapper><child>data</child></wrapper>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        assertEquals("wrapper", stream.getLocalName());

        // Inject virtual repeat
        stream.repeatStartElement();

        // 1. Replays START_ELEMENT for wrapper (REPLAY_START_DUP)
        int token = stream.next();
        assertEquals(XmlTokenStream.XML_START_ELEMENT, token);
        assertEquals("wrapper", stream.getLocalName());

        // 2. Encounters <child>, does not match wrapper -> generates virtual END_ELEMENT for wrapper
        token = stream.next();
        assertEquals(XmlTokenStream.XML_END_ELEMENT, token);
        assertEquals("wrapper", stream.getLocalName());

        // 3. REPLAY_START_DELAYED -> restores delayed child START_ELEMENT
        token = stream.next();
        assertEquals(XmlTokenStream.XML_START_ELEMENT, token);
        assertEquals("child", stream.getLocalName());
    }

    @Test(timeout = 4000)
    public void testRepeatStartElementReplayEnd() throws Exception {
        String xml = "<wrapper></wrapper>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        stream.repeatStartElement();

        // REPLAY_START_DUP
        assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.next());

        // Reaches END_ELEMENT of wrapper; matching wrapper schedules REPLAY_END
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next());

        // REPLAY_END triggers duplicate virtual closing
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next());
    }

    // =========================================================================
    // Partition D: Defensive Guards & Exception Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorThrowsOnNonStartElement() throws Exception {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader("<root/>"));
        // Reader is initially at START_DOCUMENT (event type 7), not START_ELEMENT (1)
        assertEquals(XMLStreamConstants.START_DOCUMENT, reader.getEventType());
        new XmlTokenStream(reader, "source");
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testRepeatStartElementThrowsOnInvalidState() throws Exception {
        String xml = "<root>text</root>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_TEXT, stream.next());
        // Can only repeat START_ELEMENT when current state is XML_START_ELEMENT
        stream.repeatStartElement();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSkipAttributesThrowsOnInvalidState() throws Exception {
        String xml = "<root></root>";
        XmlTokenStream stream = createStream(xml);

        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next());
        // Calling skipAttributes on END_ELEMENT must throw IllegalStateException
        stream.skipAttributes();
    }

    // =========================================================================
    // Partition E: Lifecycle, Locations, and Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testLocationsAndCloseAndToString() throws Exception {
        String xml = "<root xmlns:ns=\"http://example.com\" ns:attr=\"val\">content</root>";
        XmlTokenStream stream = createStream(xml);

        assertNotNull(stream.getXmlReader());
        assertNotNull(stream.getCurrentLocation());
        assertNotNull(stream.getTokenLocation());

        assertNull(stream.getNamespaceURI());
        assertEquals("root", stream.getLocalName());

        String repr = stream.toString();
        assertNotNull(repr);
        assertTrue(repr.contains("state=1"));
        assertTrue(repr.contains("root"));

        stream.close();
        stream.closeCompletely();
    }
}