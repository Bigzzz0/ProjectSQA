package com.fasterxml.jackson.dataformat.xml.deser;

import org.junit.Test;
import static org.junit.Assert.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: XmlTokenStream - a STAX wrapper that flattens XML events into a simple token stream.
 * 
 * Key branches and states:
 * - Constructor: validates START_ELEMENT state, initializes _currentState, _localName, _namespaceURI, _attributeCount
 * - next(): handles _repeatElement (REPLAY_START_DUP, REPLAY_END, REPLAY_START_DELAYED) and delegates to _next()
 * - _next(): 
 *   - XML_ATTRIBUTE_NAME state: increments _nextAttributeIndex, falls through to XML_START_ELEMENT
 *   - XML_START_ELEMENT: returns attributes if _nextAttributeIndex < _attributeCount, else handles text/end
 *   - XML_ATTRIBUTE_VALUE: returns XML_ATTRIBUTE_VALUE
 *   - XML_TEXT: collects until tag, handles END_ELEMENT
 *   - XML_END: returns XML_END
 * - _initStartElement(): handles wrapper matching, REPLAY_START_DELAYED for virtual END_ELEMENT
 * - _handleRepeatElement(): REPLAY_START_DUP returns XML_START_ELEMENT, REPLAY_END returns XML_END_ELEMENT, REPLAY_START_DELAYED restores names
 * - _handleEndElement(): checks wrapper matching, sets REPLAY_END if wrapper matches
 * - convertToString(): only for XML_ATTRIBUTE_NAME with _nextAttributeIndex == 0, collects text until tag
 * - skipAttributes(): handles XML_ATTRIBUTE_NAME and XML_START_ELEMENT states
 * - hasAttributes(): checks _currentState == XML_START_ELEMENT && _attributeCount > 0
 * 
 * Boundary conditions:
 * - _attributeCount == 0 (no attributes)
 * - _nextAttributeIndex == _attributeCount (all attributes consumed)
 * - Empty text (null vs empty string)
 * - Wrapper matching with null namespace
 * - REPLAY_START_DELAYED with null _nextLocalName
 * 
 * Known defect (from Defects4J): XmlTextTest::testMixedContent expects 27 but got 0.
 * This indicates that mixed content (text + elements) is not being handled correctly.
 * The defect likely involves _collectUntilTag() or the handling of text followed by START_ELEMENT.
 * 
 * Test strategy:
 * - Partition A: Core functional logic - basic start/end elements, attributes, text
 * - Partition B: Boundary values - empty text, no attributes, null namespace
 * - Partition C: Defect-targeted - mixed content with text and nested elements
 * - Partition D: Exception paths - invalid constructor state, skipEndElement on wrong token
 * - Partition E: Lifecycle - close, getCurrentLocation, toString
 */
public class XmlTokenStreamDeepseekTest {

    private XMLStreamReader createReader(String xml) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
        // Advance to first START_ELEMENT
        while (reader.hasNext() && reader.getEventType() != XMLStreamConstants.START_ELEMENT) {
            reader.next();
        }
        return reader;
    }

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testBasicStartEndElement() throws Exception {
        XMLStreamReader reader = createReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertEquals("root", tokenStream.getLocalName());
        assertNull(tokenStream.getNamespaceURI());
        assertFalse(tokenStream.hasAttributes());

        int token = tokenStream.next();
        assertEquals(XmlTokenStream.XML_END_ELEMENT, token);
        assertEquals("root", tokenStream.getLocalName());

        token = tokenStream.next();
        assertEquals(XmlTokenStream.XML_END, token);
    }

    @Test(timeout = 4000)
    public void testElementWithAttributes() throws Exception {
        XMLStreamReader reader = createReader("<root attr1=\"val1\" attr2=\"val2\"/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertTrue(tokenStream.hasAttributes());

        // First attribute name
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, tokenStream.next());
        assertEquals("attr1", tokenStream.getLocalName());
        assertEquals("val1", tokenStream.getText());

        // First attribute value
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, tokenStream.next());
        assertEquals("val1", tokenStream.getText());

        // Second attribute name
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, tokenStream.next());
        assertEquals("attr2", tokenStream.getLocalName());
        assertEquals("val2", tokenStream.getText());

        // Second attribute value
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, tokenStream.next());
        assertEquals("val2", tokenStream.getText());

        // End element
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("root", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_END, tokenStream.next());
    }

    @Test(timeout = 4000)
    public void testElementWithText() throws Exception {
        XMLStreamReader reader = createReader("<root>Hello World</root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("Hello World", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("root", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_END, tokenStream.next());
    }

    @Test(timeout = 4000)
    public void testNestedElements() throws Exception {
        XMLStreamReader reader = createReader("<root><child>text</child></root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertEquals("root", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.next());
        assertEquals("child", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("text", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("child", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("root", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_END, tokenStream.next());
    }

    @Test(timeout = 4000)
    public void testSkipEndElement() throws Exception {
        XMLStreamReader reader = createReader("<root><child/></root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());

        // Skip to child start
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.next());
        assertEquals("child", tokenStream.getLocalName());

        // Skip child end
        tokenStream.skipEndElement();
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.getCurrentToken());
        assertEquals("child", tokenStream.getLocalName());

        // Next should be root end
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("root", tokenStream.getLocalName());
    }

    @Test(timeout = 4000)
    public void testGetXmlReader() throws Exception {
        XMLStreamReader reader = createReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");
        assertNotNull(tokenStream.getXmlReader());
        assertSame(reader, tokenStream.getXmlReader());
    }

    @Test(timeout = 4000)
    public void testGetCurrentLocation() throws Exception {
        XMLStreamReader reader = createReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");
        JsonLocation location = tokenStream.getCurrentLocation();
        assertNotNull(location);
        assertEquals("testSource", location.getSourceRef());
    }

    @Test(timeout = 4000)
    public void testGetTokenLocation() throws Exception {
        XMLStreamReader reader = createReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");
        JsonLocation location = tokenStream.getTokenLocation();
        assertNotNull(location);
    }

    @Test(timeout = 4000)
    public void testCloseAndCloseCompletely() throws Exception {
        XMLStreamReader reader = createReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");
        tokenStream.close();
        tokenStream.closeCompletely();
    }

    @Test(timeout = 4000)
    public void testToString() throws Exception {
        XMLStreamReader reader = createReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");
        String str = tokenStream.toString();
        assertNotNull(str);
        assertTrue(str.contains("state="));
        assertTrue(str.contains("name=root"));
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====

    @Test(timeout = 4000)
    public void testEmptyElementNoAttributes() throws Exception {
        XMLStreamReader reader = createReader("<root></root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertFalse(tokenStream.hasAttributes());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("root", tokenStream.getLocalName());
    }

    @Test(timeout = 4000)
    public void testElementWithEmptyText() throws Exception {
        XMLStreamReader reader = createReader("<root></root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());

        // Empty text should be skipped or handled
        int token = tokenStream.next();
        // Either XML_END_ELEMENT directly or XML_TEXT with empty string
        if (token == XmlTokenStream.XML_TEXT) {
            assertEquals("", tokenStream.getText());
            token = tokenStream.next();
        }
        assertEquals(XmlTokenStream.XML_END_ELEMENT, token);
    }

    @Test(timeout = 4000)
    public void testElementWithWhitespaceText() throws Exception {
        XMLStreamReader reader = createReader("<root>   </root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());

        int token = tokenStream.next();
        // Whitespace-only text may be skipped
        if (token == XmlTokenStream.XML_TEXT) {
            assertEquals("   ", tokenStream.getText());
            token = tokenStream.next();
        }
        assertEquals(XmlTokenStream.XML_END_ELEMENT, token);
    }

    @Test(timeout = 4000)
    public void testElementWithNamespace() throws Exception {
        XMLStreamReader reader = createReader("<ns:root xmlns:ns=\"http://example.com\"/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertEquals("root", tokenStream.getLocalName());
        assertEquals("http://example.com", tokenStream.getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testManyAttributes() throws Exception {
        StringBuilder sb = new StringBuilder("<root");
        for (int i = 0; i < 100; i++) {
            sb.append(" attr").append(i).append("=\"val").append(i).append("\"");
        }
        sb.append("/>");
        XMLStreamReader reader = createReader(sb.toString());
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertTrue(tokenStream.hasAttributes());

        for (int i = 0; i < 100; i++) {
            assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, tokenStream.next());
            assertEquals("attr" + i, tokenStream.getLocalName());
            assertEquals("val" + i, tokenStream.getText());

            assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, tokenStream.next());
            assertEquals("val" + i, tokenStream.getText());
        }

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
    }

    @Test(timeout = 4000)
    public void testDeepNesting() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append("<level").append(i).append(">");
        }
        sb.append("text");
        for (int i = 49; i >= 0; i--) {
            sb.append("</level").append(i).append(">");
        }
        XMLStreamReader reader = createReader(sb.toString());
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        for (int i = 0; i < 50; i++) {
            assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.next());
            assertEquals("level" + i, tokenStream.getLocalName());
        }

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("text", tokenStream.getText());

        for (int i = 49; i >= 0; i--) {
            assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
            assertEquals("level" + i, tokenStream.getLocalName());
        }
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    /**
     * Targets the known defect from XmlTextTest::testMixedContent.
     * The test expects that mixed content (text + elements) is properly handled.
     * The defect causes the text content to be lost (expected 27 but was 0).
     */
    @Test(timeout = 4000)
    public void testMixedContentWithTextAndElements() throws Exception {
        // Mixed content: text, element, text, element, text
        String xml = "<root>abc<child1>def</child1>ghi<child2>jkl</child2>mno</root>";
        XMLStreamReader reader = createReader(xml);
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertEquals("root", tokenStream.getLocalName());

        // First text segment
        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("abc", tokenStream.getText());

        // First child element
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.next());
        assertEquals("child1", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("def", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("child1", tokenStream.getLocalName());

        // Second text segment
        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("ghi", tokenStream.getText());

        // Second child element
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.next());
        assertEquals("child2", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("jkl", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("child2", tokenStream.getLocalName());

        // Final text segment
        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("mno", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("root", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_END, tokenStream.next());
    }

    /**
     * Additional mixed content test with CDATA sections.
     */
    @Test(timeout = 4000)
    public void testMixedContentWithCDATA() throws Exception {
        String xml = "<root>before<![CDATA[cdata content]]>after</root>";
        XMLStreamReader reader = createReader(xml);
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());

        // Text before CDATA
        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("before", tokenStream.getText());

        // CDATA content (may be merged with text)
        int token = tokenStream.next();
        if (token == XmlTokenStream.XML_TEXT) {
            assertEquals("cdata content", tokenStream.getText());
            token = tokenStream.next();
        }

        // Text after CDATA
        if (token == XmlTokenStream.XML_TEXT) {
            assertEquals("after", tokenStream.getText());
            token = tokenStream.next();
        }

        assertEquals(XmlTokenStream.XML_END_ELEMENT, token);
    }

    /**
     * Test with text that has leading/trailing whitespace mixed with elements.
     */
    @Test(timeout = 4000)
    public void testMixedContentWithWhitespace() throws Exception {
        String xml = "<root>  <child>text</child>  </root>";
        XMLStreamReader reader = createReader(xml);
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());

        // Whitespace before child may be skipped
        int token = tokenStream.next();
        if (token == XmlTokenStream.XML_TEXT) {
            assertEquals("  ", tokenStream.getText());
            token = tokenStream.next();
        }

        assertEquals(XmlTokenStream.XML_START_ELEMENT, token);
        assertEquals("child", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("text", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("child", tokenStream.getLocalName());

        // Whitespace after child may be skipped
        token = tokenStream.next();
        if (token == XmlTokenStream.XML_TEXT) {
            assertEquals("  ", tokenStream.getText());
            token = tokenStream.next();
        }

        assertEquals(XmlTokenStream.XML_END_ELEMENT, token);
        assertEquals("root", tokenStream.getLocalName());
    }

    /**
     * Test with multiple text segments and nested elements.
     */
    @Test(timeout = 4000)
    public void testComplexMixedContent() throws Exception {
        String xml = "<root>text1<child1>inner1<grandchild>deep</grandchild>inner2</child1>text2</root>";
        XMLStreamReader reader = createReader(xml);
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertEquals("root", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("text1", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.next());
        assertEquals("child1", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("inner1", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.next());
        assertEquals("grandchild", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("deep", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("grandchild", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("inner2", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("child1", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("text2", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("root", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_END, tokenStream.next());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorWithNonStartElement() throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader("text"));
        // Reader is at CHARACTERS, not START_ELEMENT
        new XmlTokenStream(reader, "testSource");
    }

    @Test(timeout = 4000)
    public void testSkipEndElementWithWrongToken() throws Exception {
        XMLStreamReader reader = createReader("<root>text</root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        // Move to text
        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());

        // skipEndElement should fail because current is TEXT, not END_ELEMENT
        try {
            tokenStream.skipEndElement();
            fail("Expected IOException");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testRepeatStartElementInvalidState() throws Exception {
        XMLStreamReader reader = createReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        // Move to END_ELEMENT
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());

        // repeatStartElement should fail because state is not START_ELEMENT
        try {
            // Use reflection to access protected method
            java.lang.reflect.Method method = XmlTokenStream.class.getDeclaredMethod("repeatStartElement");
            method.setAccessible(true);
            method.invoke(tokenStream);
            fail("Expected IllegalStateException");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testSkipAttributesInvalidState() throws Exception {
        XMLStreamReader reader = createReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        // Move to END_ELEMENT
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());

        // skipAttributes should fail because state is not START_ELEMENT or ATTRIBUTE_NAME
        try {
            // Use reflection to access protected method
            java.lang.reflect.Method method = XmlTokenStream.class.getDeclaredMethod("skipAttributes");
            method.setAccessible(true);
            method.invoke(tokenStream);
            fail("Expected IllegalStateException");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testConvertToStringInvalidState() throws Exception {
        XMLStreamReader reader = createReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        // convertToString should return null when state is not ATTRIBUTE_NAME
        try {
            // Use reflection to access protected method
            java.lang.reflect.Method method = XmlTokenStream.class.getDeclaredMethod("convertToString");
            method.setAccessible(true);
            Object result = method.invoke(tokenStream);
            assertNull(result);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testStateTransitionsComplete() throws Exception {
        XMLStreamReader reader = createReader("<root attr=\"val\">text</root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        // START_ELEMENT
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertEquals("root", tokenStream.getLocalName());
        assertTrue(tokenStream.hasAttributes());

        // ATTRIBUTE_NAME
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, tokenStream.next());
        assertEquals("attr", tokenStream.getLocalName());
        assertEquals("val", tokenStream.getText());

        // ATTRIBUTE_VALUE
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, tokenStream.next());
        assertEquals("val", tokenStream.getText());

        // TEXT
        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("text", tokenStream.getText());

        // END_ELEMENT
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("root", tokenStream.getLocalName());

        // END
        assertEquals(XmlTokenStream.XML_END, tokenStream.next());
    }

    @Test(timeout = 4000)
    public void testMultipleSequentialReads() throws Exception {
        XMLStreamReader reader = createReader("<root><a>1</a><b>2</b></root>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertEquals("root", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.next());
        assertEquals("a", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("1", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("a", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.next());
        assertEquals("b", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("2", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("b", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("root", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_END, tokenStream.next());
    }

    @Test(timeout = 4000)
    public void testSelfClosingElement() throws Exception {
        XMLStreamReader reader = createReader("<root/>");
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertEquals("root", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("root", tokenStream.getLocalName());

        assertEquals(XmlTokenStream.XML_END, tokenStream.next());
    }

    @Test(timeout = 4000)
    public void testElementWithCommentsAndPI() throws Exception {
        String xml = "<root><!-- comment --><?pi test?>text</root>";
        XMLStreamReader reader = createReader(xml);
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());

        // Comments and PIs should be skipped, text should be returned
        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("text", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("root", tokenStream.getLocalName());
    }

    @Test(timeout = 4000)
    public void testAttributeWithNamespace() throws Exception {
        String xml = "<root xmlns:ns=\"http://example.com\" ns:attr=\"value\"/>";
        XMLStreamReader reader = createReader(xml);
        XmlTokenStream tokenStream = new XmlTokenStream(reader, "testSource");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());

        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, tokenStream.next());
        assertEquals("attr", tokenStream.getLocalName());
        assertEquals("http://example.com", tokenStream.getNamespaceURI());
        assertEquals("value", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, tokenStream.next());
        assertEquals("value", tokenStream.getText());

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
    }
}