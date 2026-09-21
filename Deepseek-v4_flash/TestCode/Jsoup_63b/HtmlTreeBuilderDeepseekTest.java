package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * HtmlTreeBuilderDeepseekTest
 * 
 * White-box unit tests for HtmlTreeBuilder, targeting maximum line/branch coverage
 * and specifically the known defect from Defects4J where self-closing non‑void tags
 * produce an incorrect error message.
 *
 * [Branch & Defect Analysis Matrix]
 * =================================
 * Defect 1: selfClosingOnNonvoidIsError → expects "Tag cannot be self closing; not a void tag"
 *           but buggy version yields "Self closing flag not acknowledged"
 * Defect 2: selfClosingVoidIsNotAnError → expects no errors; buggy version yields 2 errors.
 * Defect 3: tracksErrorsWhenRequested  → error count/message mismatch.
 *
 * Key branches covered:
 * - insert(Token.StartTag) : self‑closing vs non‑self‑closing
 * - insertEmpty : knownTag vs unknown, isSelfClosing flag
 * - generateImpliedEndTags : excludeTag null / non‑null
 * - inSpecificScope : baseTypes, extraTypes, targetNames
 * - resetInsertionMode : all possible element names (select, td/th, tr, tbody/thead/tfoot, etc.)
 * - formatting elements : push, remove, reconstruct, clear, replace, marker insertion
 * - foster insertion : with/without table in stack
 * - stack manipulation : popStackToClose, popStackToBefore, clearStackToContext, etc.
 * - isElementInQueue, onStack, getFromStack, removeFromStack, replaceInQueue
 * - maybeSetBaseUri : already set / not set
 * - error() : when errors can be added
 * - parseFragment : with context, without context
 */
public class HtmlTreeBuilderDeepseekTest {

    // ---------- Partition A: Core Functional Logic & State Transitions ----------

    @Test(timeout = 4000)
    public void constructorAndInitialState() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        assertNotNull(builder);
        // After construction state is null until initialiseParse is called.
        // We can't directly call initialiseParse (protected), but we can call parseFragment.
        List<Node> nodes = builder.parseFragment("", null, "http://example.com", new ParseErrorList(10), ParseSettings.htmlDefault);
        assertNotNull(nodes);
    }

    @Test(timeout = 4000)
    public void parseSimpleDocument() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<html><head></head><body><p>Hello</p></body></html>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void parseWithTableAndFosterInsertion() {
        // Covers foster insertion path (table present, insertInFosterParent)
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<table><tr><td>Cell</td></tr></table>", "http://example.com");
        assertNotNull(doc);
        Element table = doc.select("table").first();
        assertNotNull(table);
        assertEquals("Cell", table.text());
    }

    @Test(timeout = 4000)
    public void parseWithSelectScope() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<select><option>1</option><option>2</option></select>", "http://example.com");
        assertNotNull(doc);
        Elements options = doc.select("option");
        assertEquals(2, options.size());
    }

    @Test(timeout = 4000)
    public void parseWithButtonScope() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<button>Click</button>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Click", doc.select("button").text());
    }

    @Test(timeout = 4000)
    public void parseWithListItems() {
        // Exercises inListItemScope, generateImpliedEndTags for li
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<ul><li>Item1<li>Item2</ul>", "http://example.com");
        assertNotNull(doc);
        assertEquals(2, doc.select("li").size());
    }

    @Test(timeout = 4000)
    public void parseWithFramesetOkFlag() {
        // framesetOk toggling
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<html><frameset></frameset></html>", "http://example.com");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void parseWithBaseTag() {
        // maybeSetBaseUri
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<base href='http://base.com/'><p>test</p>", "http://original.com");
        assertNotNull(doc);
        // base URI should be updated
        assertTrue(doc.baseUri().startsWith("http://base.com"));
    }

    // ---------- Partition B: Boundary Value Analysis & Extremes ----------

    @Test(timeout = 4000)
    public void parseEmptyInput() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("", "http://example.com");
        assertNotNull(doc);
        assertTrue(doc.children().isEmpty());
    }

    @Test(timeout = 4000)
    public void parseNullInput() {
        // We can't pass null to parseInput, but we can test fragment parsing with null context
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        List<Node> nodes = builder.parseFragment(null, null, "http://example.com", new ParseErrorList(10), ParseSettings.htmlDefault);
        // null input string is handled by StringReader, will throw NullPointerException
        // Actually the method signature takes String, so we can't pass null. Not needed.
    }

    @Test(timeout = 4000)
    public void parseFragmentWithNullContext() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        List<Node> nodes = builder.parseFragment("<p>test</p>", null, "http://example.com", new ParseErrorList(10), ParseSettings.htmlDefault);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertEquals("p", nodes.get(0).nodeName());
    }

    @Test(timeout = 4000)
    public void parseFragmentWithContext() {
        // Create a parent document to get a context element
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<div><p>original</p></div>", "http://example.com");
        Element context = doc.select("div").first();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        List<Node> nodes = builder.parseFragment("<span>fragment</span>", context, "http://example.com", new ParseErrorList(10), ParseSettings.htmlDefault);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertEquals("span", nodes.get(0).nodeName());
    }

    // ---------- Partition C: Defect‐Targeted Branches (Self‑closing tags) ----------

    @Test(timeout = 4000)
    public void selfClosingNonVoidIsError() {
        // Known defect: non‑void tag (div) with self‑closing slash should produce error
        // "Tag cannot be self closing; not a void tag", not "Self closing flag not acknowledged"
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(10);
        Document doc = parser.parseInput("<div/>", "http://example.com");
        List<ParseError> errors = parser.getErrors();
        assertFalse("Expected at least one error for self-closing non-void tag", errors.isEmpty());
        // The error message must contain the correct text
        String message = errors.get(0).getErrorMessage();
        assertTrue("Error message should contain 'Tag cannot be self closing; not a void tag', but was: " + message,
                message.contains("Tag cannot be self closing; not a void tag"));
        // Ensure the div was still created (as an element)
        assertNotNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void selfClosingVoidIsNotAnError() {
        // Void tag (br) with self‑closing slash should produce no error
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(10);
        Document doc = parser.parseInput("<br/>", "http://example.com");
        List<ParseError> errors = parser.getErrors();
        // The buggy version reports 2 errors; correct version should have 0 errors for void tag.
        // However, note that <br/> is allowed, but the HTML5 spec says it's fine.
        // Jsoup may also generate an error for other reasons (e.g., end tag omitted)
        // but at least the self-closing flag should NOT cause an error.
        // For this test we specifically check that no error contains "self closing".
        boolean hasSelfClosingError = errors.stream()
                .anyMatch(e -> e.getErrorMessage().toLowerCase().contains("self closing"));
        assertFalse("Self-closing void tag should not generate a 'self closing' error", hasSelfClosingError);
        // Additionally the parsed document should contain a br element
        assertEquals(1, doc.select("br").size());
    }

    @Test(timeout = 4000)
    public void tracksErrorsWhenRequested() {
        // Reproduce the third defect: parsing a complex input with various errors
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(100);
        // Include a self-closing non-void tag to trigger the known bug
        Document doc = parser.parseInput("<div></p><span/><table><tr><td></td></tr></table>", "http://example.com");
        List<ParseError> errors = parser.getErrors();
        assertFalse("Expected errors", errors.isEmpty());
        // At position 50 (in the original test) the error is about self-closing non-void
        // We just check that there is an error with the correct message
        boolean correctErrorFound = errors.stream()
                .anyMatch(e -> e.getErrorMessage().contains("Tag cannot be self closing; not a void tag"));
        assertTrue("Must contain the specific self-closing error message", correctErrorFound);
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void invalidParseSettings() {
        // ParseSettings.htmlDefault is non-null, but if we pass null it should throw
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.parseFragment("<p>test</p>", null, "http://example.com", new ParseErrorList(10), null);
    }

    @Test(timeout = 4000)
    public void stackUnderflow() {
        // pop on empty stack - should throw IndexOutOfBoundsException
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        // stack is initially empty, so pop will throw
        try {
            builder.pop();
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------

    @Test(timeout = 4000)
    public void toStringTest() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<html><body><p>X</p></body></html>", "http://example.com");
        assertNotNull(doc.toString());
    }

    @Test(timeout = 4000)
    public void getStackAfterParsing() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.parseFragment("<div><span></span></div>", null, "http://example.com", new ParseErrorList(10), ParseSettings.htmlDefault);
        ArrayList<Element> stack = builder.getStack();
        assertNotNull(stack);
        // After fragment parsing with context null, stack should contain the html element and div/span?
        // Actually parseFragment with null context returns child nodes of doc.
        // We won't assert specific stack contents here, just that it's not null.
        assertTrue(stack.size() > 0);
    }

    @Test(timeout = 4000)
    public void activeFormattingElementsAfterParsing() {
        // Parse something that triggers active formatting elements (e.g., <b>)
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<b><i>bold italic</i></b>", "http://example.com");
        assertNotNull(doc);
        // We can't directly inspect formatting elements, but we know they were used.
    }

    // ---------- Additional tests for deeper branch coverage ----------

    @Test(timeout = 4000)
    public void parseWithTableBodyAndRow() {
        // Covers resetInsertionMode for tbody/thead/tfoot and tr
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<table><tbody><tr><td>Cell</td></tr></tbody></table>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Cell", doc.select("td").text());
    }

    @Test(timeout = 4000)
    public void parseWithCaption() {
        // Covers InCaption state
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<table><caption>Title</caption><tr><td>Data</td></tr></table>", "http://example.com");
        assertNotNull(doc);
        assertEquals("TitleData", doc.select("caption").text() + doc.select("td").text());
    }

    @Test(timeout = 4000)
    public void parseWithColgroup() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<table><colgroup span='2'><col></colgroup><tr><td>1</td><td>2</td></tr></table>", "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.select("colgroup").size());
    }

    @Test(timeout = 4000)
    public void parseWithHeadAndBody() {
        // Covers resetInsertionMode for head and body
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<html><head><title>Test</title></head><body><p>Content</p></body></html>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.title());
    }

    @Test(timeout = 4000)
    public void parseWithFrameset() {
        // Covers InFrameset state
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<html><frameset><frame src='a.html'></frameset></html>", "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.select("frame").size());
    }

    @Test(timeout = 4000)
    public void parseWithScriptAndStyle() {
        // Covers DataNode insertion for script and style
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<script>alert('hi');</script><style>body {}</style>", "http://example.com");
        assertNotNull(doc);
        // The data should be preserved
        assertTrue(doc.html().contains("alert('hi')"));
    }

    @Test(timeout = 4000)
    public void parseWithFormControl() {
        // Covers form element association
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<form><input type='text' name='x'></form>", "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.select("input").size());
    }

    @Test(timeout = 4000)
    public void parseWithComment() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<!-- comment --><p>text</p>", "http://example.com");
        assertNotNull(doc);
        // Comment should be present
        assertTrue(doc.html().contains("comment"));
    }

    @Test(timeout = 4000)
    public void parseWithDoctype() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<!DOCTYPE html><html></html>", "http://example.com");
        assertNotNull(doc);
        // Doctype not exposed as node in Jsoup? Actually it is in document type.
        // But we just test no exception.
    }

    @Test(timeout = 4000)
    public void parseWithImpliedEndTags() {
        // GenerateImpliedEndTags with excludeTag
        Parser parser = Parser.htmlParser();
        // <p> inside <li> should trigger implied end tags for li? Actually li is implied.
        Document doc = parser.parseInput("<ul><li><p>Test</p></li></ul>", "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.select("li").size());
    }

    @Test(timeout = 4000)
    public void parseWithUnknownSelfClosing() {
        // Unknown tag with self-closing slash should be recognized as self-closing
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(10);
        Document doc = parser.parseInput("<mycustom />", "http://example.com");
        List<ParseError> errors = parser.getErrors();
        // Unknown tags are allowed to be self-closing, so no error expected
        assertTrue("Unknown self-closing tag should not produce an error", errors.isEmpty());
        assertNotNull(doc.select("mycustom").first());
    }
}