package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.TreeBuilder
 *
 * 1. Decision & Condition Coverage:
 *    - initialiseParse(input, baseUri, errors):
 *      * input == null -> IllegalArgumentException ("String input must not be null")
 *      * baseUri == null -> IllegalArgumentException ("BaseURI must not be null")
 *      * valid inputs -> doc, reader, tokeniser, stack, baseUri correctly initialized.
 *    - parse(input, baseUri):
 *      * delegates to parse(input, baseUri, ParseErrorList.noTracking())
 *    - parse(input, baseUri, errors):
 *      * runs initialiseParse() and runParser(), returns doc instance.
 *    - runParser():
 *      * tokeniser.read() loop until EOF token is received.
 *      * calls process(token) and token.reset() on every cycle.
 *      * breaks when token.type == TokenType.EOF.
 *    - processStartTag(name):
 *      * resets start token, sets name, invokes process(start), propagates boolean return.
 *    - processStartTag(name, attrs):
 *      * resets start token, sets name and attributes, invokes process(start), propagates boolean return.
 *    - processEndTag(name):
 *      * resets end token, sets name, invokes process(end), propagates boolean return.
 *    - currentElement():
 *      * stack.size() == 0 -> returns null
 *      * stack.size() > 0 -> returns stack.get(size - 1) (top of stack)
 *
 * 2. Defects4J Defect Target:
 *    - Failure: org.jsoup.parser.HtmlParserTest::testInvalidTableContents
 *      AssertionFailedError: "Search text did not come after comment"
 *    - Condition: Foster parenting of invalid table elements must place adopted nodes
 *      before the table, but strictly after any pre-existing siblings (e.g. comments).
 */
public class TreeBuilderGeminiTest {

    // Concrete test implementation of abstract TreeBuilder to inspect internal lifecycle
    private static class RecordingTreeBuilder extends TreeBuilder {
        final List<Token.TokenType> tokenTypesSeen = new ArrayList<Token.TokenType>();
        final List<String> tagNamesSeen = new ArrayList<String>();
        boolean processReturnValue = true;

        @Override
        protected boolean process(Token token) {
            tokenTypesSeen.add(token.type);
            if (token instanceof Token.Tag) {
                tagNamesSeen.add(((Token.Tag) token).name());
            }
            return processReturnValue;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseDefaultNoTracking() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        Document doc = builder.parse("<p>Hello</p>", "http://example.com");

        assertNotNull("Doc should not be null", doc);
        assertEquals("http://example.com", doc.baseUri());
        assertFalse("Errors should not track by default", builder.errors.canAddError());
        assertTrue("Tokens should have been processed", builder.tokenTypesSeen.size() > 0);
        assertEquals("Last token processed must be EOF",
                Token.TokenType.EOF,
                builder.tokenTypesSeen.get(builder.tokenTypesSeen.size() - 1));
    }

    @Test(timeout = 4000)
    public void testParseWithTrackingErrors() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        ParseErrorList errorList = ParseErrorList.tracking(10);
        Document doc = builder.parse("<div>Text", "http://example.com/dir/", errorList);

        assertNotNull("Doc should not be null", doc);
        assertEquals("http://example.com/dir/", doc.baseUri());
        assertSame("Assigned error list must match instance", errorList, builder.errors);
        assertEquals(errorList, builder.tokeniser.getErrors());
    }

    @Test(timeout = 4000)
    public void testRunParserLoopExecutesUntilEof() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        builder.initialiseParse("<span>test</span>", "http://example.com", ParseErrorList.noTracking());
        builder.runParser();

        assertTrue("Expected multiple tokens processed", builder.tokenTypesSeen.size() >= 3);
        assertEquals("Expected EOF as final token",
                Token.TokenType.EOF,
                builder.tokenTypesSeen.get(builder.tokenTypesSeen.size() - 1));
    }

    @Test(timeout = 4000)
    public void testProcessStartTagSimple() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        builder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());

        builder.processReturnValue = true;
        boolean accepted = builder.processStartTag("div");
        assertTrue("processStartTag should propagate true return", accepted);
        assertTrue("Tag name should be recorded as div", builder.tagNamesSeen.contains("div"));

        builder.processReturnValue = false;
        boolean rejected = builder.processStartTag("span");
        assertFalse("processStartTag should propagate false return", rejected);
        assertTrue("Tag name should be recorded as span", builder.tagNamesSeen.contains("span"));
    }

    @Test(timeout = 4000)
    public void testProcessStartTagWithAttributes() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        builder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());

        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        attrs.put("class", "content");

        builder.processReturnValue = true;
        boolean accepted = builder.processStartTag("section", attrs);
        assertTrue("processStartTag with attrs should return true", accepted);
        assertTrue("Tag name should be recorded as section", builder.tagNamesSeen.contains("section"));

        builder.processReturnValue = false;
        boolean rejected = builder.processStartTag("article", attrs);
        assertFalse("processStartTag with attrs should return false", rejected);
        assertTrue("Tag name should be recorded as article", builder.tagNamesSeen.contains("article"));
    }

    @Test(timeout = 4000)
    public void testProcessEndTag() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        builder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());

        builder.processReturnValue = true;
        boolean accepted = builder.processEndTag("p");
        assertTrue("processEndTag should return true", accepted);
        assertTrue("End tag name should be recorded as p", builder.tagNamesSeen.contains("p"));

        builder.processReturnValue = false;
        boolean rejected = builder.processEndTag("div");
        assertFalse("processEndTag should return false", rejected);
        assertTrue("End tag name should be recorded as div", builder.tagNamesSeen.contains("div"));
    }

    @Test(timeout = 4000)
    public void testCurrentElementOnEmptyAndPopulatedStack() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        builder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());

        // Branch 1: stack size == 0 -> returns null
        assertNull("currentElement() on empty stack must return null", builder.currentElement());

        // Branch 2: stack size == 1 -> returns element
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        builder.stack.add(el1);
        assertSame("currentElement() must return top element", el1, builder.currentElement());

        // Branch 3: stack size > 1 -> returns the newest element
        Element el2 = new Element(Tag.valueOf("p"), "http://example.com");
        builder.stack.add(el2);
        assertSame("currentElement() must return top element of stack", el2, builder.currentElement());

        // Stack pop transition
        builder.stack.remove(builder.stack.size() - 1);
        assertSame("currentElement() must return el1 after popping el2", el1, builder.currentElement());

        builder.stack.remove(builder.stack.size() - 1);
        assertNull("currentElement() must return null after empty", builder.currentElement());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyHtmlStringParsing() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        Document doc = builder.parse("", "http://example.com");

        assertNotNull("Document should be created even for empty input", doc);
        assertEquals("http://example.com", doc.baseUri());
        assertEquals("Empty input produces single EOF token", 1, builder.tokenTypesSeen.size());
        assertEquals(Token.TokenType.EOF, builder.tokenTypesSeen.get(0));
    }

    @Test(timeout = 4000)
    public void testEmptyBaseUriParsing() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        Document doc = builder.parse("<span>text</span>", "");

        assertNotNull("Document should be created with empty baseUri", doc);
        assertEquals("", doc.baseUri());
        assertEquals("", builder.baseUri);
    }

    @Test(timeout = 4000)
    public void testStackPreAllocationSize() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        builder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());
        assertNotNull("Stack must be initialized", builder.stack);
        assertEquals("Initial stack size must be 0", 0, builder.stack.size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets known defect: org.jsoup.parser.HtmlParserTest::testInvalidTableContents
     * Failure symptom: junit.framework.AssertionFailedError: Search text did not come after comment
     *
     * Cause: Foster parenting incorrectly prepended adopted nodes ahead of pre-existing
     * comment nodes in body instead of inserting them immediately prior to the table element.
     */
    @Test(timeout = 4000)
    public void testInvalidTableContentsFosterParentingOrder() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        String invalidHtml = "<html><head>text1<p>text2</head><body><!-- comment --><table><meta><p><b>hello<p>there!</b></td></tr></table> <p>ghost";
        Document doc = parser.parseInput(invalidHtml, "http://example.com");

        Element body = doc.body();
        assertNotNull("Body element must exist", body);

        // Ground-truth assertion targeting the defect
        int commentIdx = body.html().indexOf("<!-- comment -->");
        int textIdx = body.html().indexOf("hello");

        assertTrue("Search text did not come after comment", textIdx > commentIdx);

        // Verify that the comment remains the very first child of body
        Node firstChild = body.childNode(0);
        assertTrue("First child node of body must be a Comment", firstChild instanceof Comment);
        assertEquals(" comment ", ((Comment) firstChild).getData());
    }

    @Test(timeout = 4000)
    public void testInvalidTableContentsFosterParentingStructure() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        String invalidHtml = "<body><!-- comment --><table><tr><td>cell</td></tr>outside text</table></body>";
        Document doc = parser.parseInput(invalidHtml, "");

        Element body = doc.body();
        int commentPos = body.html().indexOf("<!-- comment -->");
        int textPos = body.html().indexOf("outside text");

        assertTrue("Foster-parented 'outside text' must follow '<!-- comment -->'", textPos > commentPos);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInitialiseParseNullInputThrowsException() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        builder.initialiseParse(null, "http://example.com", ParseErrorList.noTracking());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInitialiseParseNullBaseUriThrowsException() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        builder.initialiseParse("<p>test</p>", null, ParseErrorList.noTracking());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseNullInputTwoArgThrowsException() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        builder.parse(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseNullBaseUriTwoArgThrowsException() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        builder.parse("<p>test</p>", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseNullInputThreeArgThrowsException() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        builder.parse(null, "http://example.com", ParseErrorList.noTracking());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseNullBaseUriThreeArgThrowsException() {
        RecordingTreeBuilder builder = new RecordingTreeBuilder();
        builder.parse("<div>test</div>", null, ParseErrorList.noTracking());
    }

    // =========================================================================
    // Partition E: Subclass Specialization & Integration Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testHtmlTreeBuilderSubclassIntegration() {
        HtmlTreeBuilder htmlBuilder = new HtmlTreeBuilder();
        Document doc = htmlBuilder.parse("<div id='content'>Content</div>", "http://example.com");

        assertNotNull("HtmlTreeBuilder should produce non-null Document", doc);
        assertEquals("Content", doc.getElementById("content").text());
        assertSame(doc, htmlBuilder.doc);
        assertNotNull("Stack must be cleared or managed appropriately", htmlBuilder.stack);
    }

    @Test(timeout = 4000)
    public void testXmlTreeBuilderSubclassIntegration() {
        XmlTreeBuilder xmlBuilder = new XmlTreeBuilder();
        Document doc = xmlBuilder.parse("<root><child attr='val'>text</child></root>", "http://example.com");

        assertNotNull("XmlTreeBuilder should produce non-null Document", doc);
        assertEquals("val", doc.select("child").attr("attr"));
        assertSame(doc, xmlBuilder.doc);
    }
}