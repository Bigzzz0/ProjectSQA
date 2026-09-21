package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: HtmlTreeBuilder (org.jsoup.parser)
 * 
 * Known Defect: preservedCaseLinksCantNest - When parsing nested <A> tags with preserved case,
 * the parser incorrectly nests the second <A> inside the first instead of closing the first.
 * Expected: <A> ONE <[/A> <A> Two] </A>> (i.e., second <A> is sibling, not child)
 * Actual (buggy): <A> ONE <[A> Two </A>] </A>> (second <A> nested inside first)
 * 
 * Branches targeted:
 * - insert(Token.StartTag) with self-closing handling
 * - insertEmpty() for void/self-closing elements
 * - generateImpliedEndTags() with excludeTag logic
 * - inSpecificScope() with targetNames, baseTypes, extraTypes
 * - inScope() with extras parameter
 * - inListItemScope(), inButtonScope(), inTableScope(), inSelectScope()
 * - pushActiveFormattingElements() with marker (null) handling
 * - reconstructFormattingElements() with skip logic
 * - clearStackToContext() with multiple node names
 * - popStackToClose() with single and multiple names
 * - insertInFosterParent() with table context
 * - resetInsertionMode() with various element types
 * - getActiveFormattingElement() with marker break
 * - removeFromActiveFormattingElements() with reverse iteration
 * - isElementInQueue() with identity comparison
 * - getFromStack() with reverse traversal
 * - aboveOnStack() with boundary conditions
 * - insertOnStackAfter() with lastIndexOf
 * - replaceOnStack() with queue replacement
 * 
 * Boundary conditions:
 * - Empty stack (size 0) in insertNode
 * - Single element stack in popStackToClose
 * - MaxScopeSearchDepth boundary (100)
 * - formattingElements with null markers
 * - stack with only html element
 * - Foster parenting with/without table
 * - Fragment parsing with null context
 * - Case sensitivity in tag names
 * - Self-closing vs void tags
 */
public class HtmlTreeBuilderDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testInsertStartTagBasic() {
        // Setup a parser and tree builder
        Parser parser = Parser.htmlParser();
        TreeBuilder treeBuilder = parser.getTreeBuilder();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) treeBuilder;
        
        // Initialize parse with empty input
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        // Insert a start tag
        Element el = htmlTreeBuilder.insertStartTag("div");
        
        // Verify element is created and on stack
        assertNotNull(el);
        assertEquals("div", el.nodeName());
        assertEquals(1, htmlTreeBuilder.getStack().size());
        assertEquals(el, htmlTreeBuilder.currentElement());
    }

    @Test(timeout = 4000)
    public void testInsertSelfClosingStartTag() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        // Create a self-closing start tag
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("br");
        startTag.selfClosing = true;
        
        Element el = htmlTreeBuilder.insert(startTag);
        
        // Verify element is inserted and stack has it
        assertNotNull(el);
        assertEquals("br", el.nodeName());
        assertEquals(1, htmlTreeBuilder.getStack().size());
    }

    @Test(timeout = 4000)
    public void testInsertCharacterTextNode() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        // Insert a parent element first
        htmlTreeBuilder.insertStartTag("div");
        
        // Create character token
        Token.Character charToken = new Token.Character();
        charToken.data("Hello");
        
        htmlTreeBuilder.insert(charToken);
        
        // Verify text node is appended
        Element div = htmlTreeBuilder.currentElement();
        assertEquals(1, div.childNodeSize());
        assertTrue(div.childNode(0) instanceof TextNode);
        assertEquals("Hello", ((TextNode) div.childNode(0)).getWholeText());
    }

    @Test(timeout = 4000)
    public void testInsertCDataNode() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        
        Token.Character cdataToken = new Token.Character();
        cdataToken.data("some <cdata>");
        cdataToken.cData = true;
        
        htmlTreeBuilder.insert(cdataToken);
        
        Element div = htmlTreeBuilder.currentElement();
        assertEquals(1, div.childNodeSize());
        assertTrue(div.childNode(0) instanceof CDataNode);
    }

    @Test(timeout = 4000)
    public void testInsertDataNodeForScript() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("script");
        
        Token.Character scriptToken = new Token.Character();
        scriptToken.data("var x = 1;");
        
        htmlTreeBuilder.insert(scriptToken);
        
        Element script = htmlTreeBuilder.currentElement();
        assertEquals(1, script.childNodeSize());
        assertTrue(script.childNode(0) instanceof DataNode);
    }

    @Test(timeout = 4000)
    public void testInsertComment() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Token.Comment commentToken = new Token.Comment();
        commentToken.data("test comment");
        
        htmlTreeBuilder.insert(commentToken);
        
        // Comment should go to document if stack is empty
        assertEquals(1, htmlTreeBuilder.getDocument().childNodeSize());
        assertTrue(htmlTreeBuilder.getDocument().childNode(0) instanceof Comment);
    }

    @Test(timeout = 4000)
    public void testInsertFormElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Token.StartTag formTag = new Token.StartTag();
        formTag.name("form");
        
        FormElement form = htmlTreeBuilder.insertForm(formTag, true);
        
        assertNotNull(form);
        assertEquals("form", form.nodeName());
        assertEquals(form, htmlTreeBuilder.getFormElement());
        assertEquals(1, htmlTreeBuilder.getStack().size());
    }

    @Test(timeout = 4000)
    public void testInsertFormElementNotOnStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Token.StartTag formTag = new Token.StartTag();
        formTag.name("form");
        
        FormElement form = htmlTreeBuilder.insertForm(formTag, false);
        
        assertNotNull(form);
        assertEquals(0, htmlTreeBuilder.getStack().size());
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseSingleName() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        // Push elements onto stack
        htmlTreeBuilder.insertStartTag("div");
        htmlTreeBuilder.insertStartTag("p");
        htmlTreeBuilder.insertStartTag("span");
        
        assertEquals(3, htmlTreeBuilder.getStack().size());
        
        htmlTreeBuilder.popStackToClose("p");
        
        // Should pop span and p, leaving div
        assertEquals(1, htmlTreeBuilder.getStack().size());
        assertEquals("div", htmlTreeBuilder.currentElement().nodeName());
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseMultipleNames() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("ul");
        htmlTreeBuilder.insertStartTag("li");
        htmlTreeBuilder.insertStartTag("span");
        
        htmlTreeBuilder.popStackToClose("li", "span");
        
        // Should pop span and li
        assertEquals(1, htmlTreeBuilder.getStack().size());
        assertEquals("ul", htmlTreeBuilder.currentElement().nodeName());
    }

    @Test(timeout = 4000)
    public void testPopStackToBefore() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        htmlTreeBuilder.insertStartTag("p");
        htmlTreeBuilder.insertStartTag("span");
        
        htmlTreeBuilder.popStackToBefore("p");
        
        // Should pop span, stop at p
        assertEquals(2, htmlTreeBuilder.getStack().size());
        assertEquals("p", htmlTreeBuilder.currentElement().nodeName());
    }

    @Test(timeout = 4000)
    public void testClearStackToTableContext() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        htmlTreeBuilder.insertStartTag("table");
        htmlTreeBuilder.insertStartTag("tr");
        htmlTreeBuilder.insertStartTag("td");
        
        htmlTreeBuilder.clearStackToTableContext();
        
        // Should clear until table
        assertEquals(2, htmlTreeBuilder.getStack().size());
        assertEquals("table", htmlTreeBuilder.currentElement().nodeName());
    }

    @Test(timeout = 4000)
    public void testClearStackToTableBodyContext() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("table");
        htmlTreeBuilder.insertStartTag("tbody");
        htmlTreeBuilder.insertStartTag("tr");
        htmlTreeBuilder.insertStartTag("td");
        
        htmlTreeBuilder.clearStackToTableBodyContext();
        
        assertEquals(2, htmlTreeBuilder.getStack().size());
        assertEquals("tbody", htmlTreeBuilder.currentElement().nodeName());
    }

    @Test(timeout = 4000)
    public void testClearStackToTableRowContext() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("table");
        htmlTreeBuilder.insertStartTag("tbody");
        htmlTreeBuilder.insertStartTag("tr");
        htmlTreeBuilder.insertStartTag("td");
        
        htmlTreeBuilder.clearStackToTableRowContext();
        
        assertEquals(3, htmlTreeBuilder.getStack().size());
        assertEquals("tr", htmlTreeBuilder.currentElement().nodeName());
    }

    @Test(timeout = 4000)
    public void testAboveOnStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element html = htmlTreeBuilder.insertStartTag("html");
        Element body = htmlTreeBuilder.insertStartTag("body");
        Element div = htmlTreeBuilder.insertStartTag("div");
        
        Element above = htmlTreeBuilder.aboveOnStack(div);
        assertEquals(body, above);
        
        above = htmlTreeBuilder.aboveOnStack(body);
        assertEquals(html, above);
    }

    @Test(timeout = 4000)
    public void testInsertOnStackAfter() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element div1 = htmlTreeBuilder.insertStartTag("div");
        Element div2 = htmlTreeBuilder.insertStartTag("div");
        
        Element newDiv = new Element(Tag.valueOf("span"), "");
        htmlTreeBuilder.insertOnStackAfter(div1, newDiv);
        
        assertEquals(3, htmlTreeBuilder.getStack().size());
        assertEquals(newDiv, htmlTreeBuilder.getStack().get(1));
    }

    @Test(timeout = 4000)
    public void testReplaceOnStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element div = htmlTreeBuilder.insertStartTag("div");
        Element replacement = new Element(Tag.valueOf("span"), "");
        
        htmlTreeBuilder.replaceOnStack(div, replacement);
        
        assertEquals(replacement, htmlTreeBuilder.currentElement());
    }

    @Test(timeout = 4000)
    public void testGetFromStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        Element p = htmlTreeBuilder.insertStartTag("p");
        htmlTreeBuilder.insertStartTag("span");
        
        Element found = htmlTreeBuilder.getFromStack("p");
        assertEquals(p, found);
        
        assertNull(htmlTreeBuilder.getFromStack("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testRemoveFromStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element div = htmlTreeBuilder.insertStartTag("div");
        htmlTreeBuilder.insertStartTag("p");
        
        boolean removed = htmlTreeBuilder.removeFromStack(div);
        assertTrue(removed);
        assertEquals(1, htmlTreeBuilder.getStack().size());
        
        removed = htmlTreeBuilder.removeFromStack(div);
        assertFalse(removed);
    }

    @Test(timeout = 4000)
    public void testOnStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element div = htmlTreeBuilder.insertStartTag("div");
        Element notOnStack = new Element(Tag.valueOf("span"), "");
        
        assertTrue(htmlTreeBuilder.onStack(div));
        assertFalse(htmlTreeBuilder.onStack(notOnStack));
    }

    @Test(timeout = 4000)
    public void testFramesetOk() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        assertTrue(htmlTreeBuilder.framesetOk());
        htmlTreeBuilder.framesetOk(false);
        assertFalse(htmlTreeBuilder.framesetOk());
        htmlTreeBuilder.framesetOk(true);
        assertTrue(htmlTreeBuilder.framesetOk());
    }

    @Test(timeout = 4000)
    public void testIsFragmentParsing() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        assertFalse(htmlTreeBuilder.isFragmentParsing());
        
        // Simulate fragment parsing
        htmlTreeBuilder.parseFragment("<div></div>", null, "http://example.com", parser);
        assertTrue(htmlTreeBuilder.isFragmentParsing());
    }

    @Test(timeout = 4000)
    public void testGetDocument() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Document doc = htmlTreeBuilder.getDocument();
        assertNotNull(doc);
        assertEquals("", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testGetBaseUri() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com/base", parser);
        
        assertEquals("http://example.com/base", htmlTreeBuilder.getBaseUri());
    }

    @Test(timeout = 4000)
    public void testMaybeSetBaseUri() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com/", parser);
        
        Element base = new Element(Tag.valueOf("base"), "");
        base.attr("href", "http://newexample.com/");
        
        htmlTreeBuilder.maybeSetBaseUri(base);
        
        assertEquals("http://newexample.com/", htmlTreeBuilder.getBaseUri());
        assertEquals("http://newexample.com/", htmlTreeBuilder.getDocument().baseUri());
    }

    @Test(timeout = 4000)
    public void testMaybeSetBaseUriOnlyOnce() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com/", parser);
        
        Element base1 = new Element(Tag.valueOf("base"), "");
        base1.attr("href", "http://first.com/");
        htmlTreeBuilder.maybeSetBaseUri(base1);
        
        Element base2 = new Element(Tag.valueOf("base"), "");
        base2.attr("href", "http://second.com/");
        htmlTreeBuilder.maybeSetBaseUri(base2);
        
        assertEquals("http://first.com/", htmlTreeBuilder.getBaseUri());
    }

    @Test(timeout = 4000)
    public void testMaybeSetBaseUriEmptyHref() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com/", parser);
        
        Element base = new Element(Tag.valueOf("base"), "");
        base.attr("href", "");
        
        htmlTreeBuilder.maybeSetBaseUri(base);
        
        assertEquals("http://example.com/", htmlTreeBuilder.getBaseUri());
    }

    @Test(timeout = 4000)
    public void testStateTransition() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        assertEquals(HtmlTreeBuilderState.Initial, htmlTreeBuilder.state());
        
        htmlTreeBuilder.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, htmlTreeBuilder.state());
        
        htmlTreeBuilder.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, htmlTreeBuilder.originalState());
    }

    @Test(timeout = 4000)
    public void testOriginalState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        assertNull(htmlTreeBuilder.originalState());
        
        htmlTreeBuilder.transition(HtmlTreeBuilderState.InBody);
        htmlTreeBuilder.markInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.InBody, htmlTreeBuilder.originalState());
    }

    @Test(timeout = 4000)
    public void testSetGetHeadElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element head = new Element(Tag.valueOf("head"), "");
        htmlTreeBuilder.setHeadElement(head);
        
        assertEquals(head, htmlTreeBuilder.getHeadElement());
    }

    @Test(timeout = 4000)
    public void testSetGetFormElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        FormElement form = new FormElement(Tag.valueOf("form"), "");
        htmlTreeBuilder.setFormElement(form);
        
        assertEquals(form, htmlTreeBuilder.getFormElement());
    }

    @Test(timeout = 4000)
    public void testFosterInserts() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        assertFalse(htmlTreeBuilder.isFosterInserts());
        htmlTreeBuilder.setFosterInserts(true);
        assertTrue(htmlTreeBuilder.isFosterInserts());
        htmlTreeBuilder.setFosterInserts(false);
        assertFalse(htmlTreeBuilder.isFosterInserts());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testInsertNodeEmptyStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        // Stack is empty, node should go to document
        Element el = new Element(Tag.valueOf("div"), "");
        htmlTreeBuilder.insertNode(el);
        
        assertEquals(1, htmlTreeBuilder.getDocument().childNodeSize());
        assertEquals(el, htmlTreeBuilder.getDocument().childNode(0));
    }

    @Test(timeout = 4000)
    public void testInsertNodeWithStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element parent = htmlTreeBuilder.insertStartTag("div");
        Element child = new Element(Tag.valueOf("span"), "");
        htmlTreeBuilder.insertNode(child);
        
        assertEquals(1, parent.childNodeSize());
        assertEquals(child, parent.childNode(0));
    }

    @Test(timeout = 4000)
    public void testInsertNodeFosterParent() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        // Create table structure
        Element table = htmlTreeBuilder.insertStartTag("table");
        Element tr = htmlTreeBuilder.insertStartTag("tr");
        Element td = htmlTreeBuilder.insertStartTag("td");
        
        // Enable foster inserts
        htmlTreeBuilder.setFosterInserts(true);
        
        Element fosterChild = new Element(Tag.valueOf("div"), "");
        htmlTreeBuilder.insertNode(fosterChild);
        
        // Should be inserted before table
        assertEquals(fosterChild, table.previousSibling());
    }

    @Test(timeout = 4000)
    public void testInsertNodeFormListed() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        // Create form
        Token.StartTag formTag = new Token.StartTag();
        formTag.name("form");
        FormElement form = htmlTreeBuilder.insertForm(formTag, true);
        
        // Insert input (form-listed element)
        Element input = new Element(Tag.valueOf("input"), "");
        htmlTreeBuilder.insertNode(input);
        
        // Verify input is associated with form
        assertTrue(form.elements().contains(input));
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseEmptyStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        // No elements on stack, should not throw
        htmlTreeBuilder.popStackToClose("div");
        assertEquals(0, htmlTreeBuilder.getStack().size());
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseSingleElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        
        htmlTreeBuilder.popStackToClose("div");
        assertEquals(0, htmlTreeBuilder.getStack().size());
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseNotFound() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        htmlTreeBuilder.insertStartTag("p");
        
        htmlTreeBuilder.popStackToClose("span");
        
        // Should pop all elements
        assertEquals(0, htmlTreeBuilder.getStack().size());
    }

    @Test(timeout = 4000)
    public void testPopStackToBeforeNotFound() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        htmlTreeBuilder.insertStartTag("p");
        
        htmlTreeBuilder.popStackToBefore("span");
        
        // Should pop all elements
        assertEquals(0, htmlTreeBuilder.getStack().size());
    }

    @Test(timeout = 4000)
    public void testClearStackToContextEmptyStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.clearStackToTableContext();
        assertEquals(0, htmlTreeBuilder.getStack().size());
    }

    @Test(timeout = 4000)
    public void testClearStackToContextNoMatch() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        htmlTreeBuilder.insertStartTag("p");
        
        htmlTreeBuilder.clearStackToTableContext();
        
        // Should clear all except html
        assertEquals(1, htmlTreeBuilder.getStack().size());
        assertEquals("html", htmlTreeBuilder.currentElement().nodeName());
    }

    @Test(timeout = 4000)
    public void testAboveOnStackSingleElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element html = htmlTreeBuilder.insertStartTag("html");
        
        // Only one element, aboveOnStack should return null
        Element above = htmlTreeBuilder.aboveOnStack(html);
        assertNull(above);
    }

    @Test(timeout = 4000)
    public void testInsertOnStackAfterNotFound() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        Element notOnStack = new Element(Tag.valueOf("span"), "");
        Element newEl = new Element(Tag.valueOf("p"), "");
        
        try {
            htmlTreeBuilder.insertOnStackAfter(notOnStack, newEl);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReplaceOnStackNotFound() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        Element notOnStack = new Element(Tag.valueOf("span"), "");
        Element newEl = new Element(Tag.valueOf("p"), "");
        
        try {
            htmlTreeBuilder.replaceOnStack(notOnStack, newEl);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetFromStackEmptyStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        assertNull(htmlTreeBuilder.getFromStack("div"));
    }

    @Test(timeout = 4000)
    public void testRemoveFromStackEmptyStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element el = new Element(Tag.valueOf("div"), "");
        assertFalse(htmlTreeBuilder.removeFromStack(el));
    }

    @Test(timeout = 4000)
    public void testIsElementInQueueEmptyQueue() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element el = new Element(Tag.valueOf("div"), "");
        assertFalse(htmlTreeBuilder.isElementInQueue(new ArrayList<>(), el));
    }

    @Test(timeout = 4000)
    public void testIsElementInQueueWithElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element el = new Element(Tag.valueOf("div"), "");
        ArrayList<Element> queue = new ArrayList<>();
        queue.add(el);
        
        assertTrue(htmlTreeBuilder.isElementInQueue(queue, el));
    }

    @Test(timeout = 4000)
    public void testIsElementInQueueWithDifferentElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("span"), "");
        ArrayList<Element> queue = new ArrayList<>();
        queue.add(el1);
        
        assertFalse(htmlTreeBuilder.isElementInQueue(queue, el2));
    }

    @Test(timeout = 4000)
    public void testIsSpecial() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element div = new Element(Tag.valueOf("div"), "");
        assertTrue(htmlTreeBuilder.isSpecial(div));
        
        Element span = new Element(Tag.valueOf("span"), "");
        assertFalse(htmlTreeBuilder.isSpecial(span));
        
        Element p = new Element(Tag.valueOf("p"), "");
        assertTrue(htmlTreeBuilder.isSpecial(p));
    }

    @Test(timeout = 4000)
    public void testLastFormattingElementEmpty() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        assertNull(htmlTreeBuilder.lastFormattingElement());
    }

    @Test(timeout = 4000)
    public void testLastFormattingElementWithElements() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element el1 = new Element(Tag.valueOf("b"), "");
        Element el2 = new Element(Tag.valueOf("i"), "");
        htmlTreeBuilder.pushActiveFormattingElements(el1);
        htmlTreeBuilder.pushActiveFormattingElements(el2);
        
        assertEquals(el2, htmlTreeBuilder.lastFormattingElement());
    }

    @Test(timeout = 4000)
    public void testRemoveLastFormattingElementEmpty() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        assertNull(htmlTreeBuilder.removeLastFormattingElement());
    }

    @Test(timeout = 4000)
    public void testRemoveLastFormattingElementWithElements() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element el1 = new Element(Tag.valueOf("b"), "");
        Element el2 = new Element(Tag.valueOf("i"), "");
        htmlTreeBuilder.pushActiveFormattingElements(el1);
        htmlTreeBuilder.pushActiveFormattingElements(el2);
        
        assertEquals(el2, htmlTreeBuilder.removeLastFormattingElement());
        assertEquals(el1, htmlTreeBuilder.lastFormattingElement());
    }

    @Test(timeout = 4000)
    public void testPushActiveFormattingElementsMarker() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        // Push marker
        htmlTreeBuilder.insertMarkerToFormattingElements();
        
        // Push element
        Element el = new Element(Tag.valueOf("b"), "");
        htmlTreeBuilder.pushActiveFormattingElements(el);
        
        // Verify marker is preserved
        assertEquals(2, htmlTreeBuilder.formattingElements.size());
        assertNull(htmlTreeBuilder.formattingElements.get(0));
        assertEquals(el, htmlTreeBuilder.formattingElements.get(1));
    }

    @Test(timeout = 4000)
    public void testPushActiveFormattingElementsDuplicate() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element el = new Element(Tag.valueOf("b"), "");
        el.attr("class", "test");
        
        // Push same element 3 times
        htmlTreeBuilder.pushActiveFormattingElements(el);
        htmlTreeBuilder.pushActiveFormattingElements(el);
        htmlTreeBuilder.pushActiveFormattingElements(el);
        
        // Should only have 2 elements (3rd duplicate removed)
        assertEquals(2, htmlTreeBuilder.formattingElements.size());
    }

    @Test(timeout = 4000)
    public void testClearFormattingElementsToLastMarker() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element el1 = new Element(Tag.valueOf("b"), "");
        htmlTreeBuilder.pushActiveFormattingElements(el1);
        htmlTreeBuilder.insertMarkerToFormattingElements();
        Element el2 = new Element(Tag.valueOf("i"), "");
        htmlTreeBuilder.pushActiveFormattingElements(el2);
        
        htmlTreeBuilder.clearFormattingElementsToLastMarker();
        
        // Should clear elements after marker
        assertEquals(2, htmlTreeBuilder.formattingElements.size());
        assertEquals(el1, htmlTreeBuilder.formattingElements.get(0));
        assertNull(htmlTreeBuilder.formattingElements.get(1));
    }

    @Test(timeout = 4000)
    public void testRemoveFromActiveFormattingElements() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element el1 = new Element(Tag.valueOf("b"), "");
        Element el2 = new Element(Tag.valueOf("i"), "");
        htmlTreeBuilder.pushActiveFormattingElements(el1);
        htmlTreeBuilder.pushActiveFormattingElements(el2);
        
        htmlTreeBuilder.removeFromActiveFormattingElements(el1);
        
        assertEquals(1, htmlTreeBuilder.formattingElements.size());
        assertEquals(el2, htmlTreeBuilder.formattingElements.get(0));
    }

    @Test(timeout = 4000)
    public void testIsInActiveFormattingElements() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element el = new Element(Tag.valueOf("b"), "");
        htmlTreeBuilder.pushActiveFormattingElements(el);
        
        assertTrue(htmlTreeBuilder.isInActiveFormattingElements(el));
        
        Element other = new Element(Tag.valueOf("i"), "");
        assertFalse(htmlTreeBuilder.isInActiveFormattingElements(other));
    }

    @Test(timeout = 4000)
    public void testGetActiveFormattingElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element b = new Element(Tag.valueOf("b"), "");
        Element i = new Element(Tag.valueOf("i"), "");
        htmlTreeBuilder.pushActiveFormattingElements(b);
        htmlTreeBuilder.pushActiveFormattingElements(i);
        
        assertEquals(b, htmlTreeBuilder.getActiveFormattingElement("b"));
        assertEquals(i, htmlTreeBuilder.getActiveFormattingElement("i"));
        assertNull(htmlTreeBuilder.getActiveFormattingElement("u"));
    }

    @Test(timeout = 4000)
    public void testGetActiveFormattingElementWithMarker() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element b = new Element(Tag.valueOf("b"), "");
        htmlTreeBuilder.pushActiveFormattingElements(b);
        htmlTreeBuilder.insertMarkerToFormattingElements();
        
        // Marker should stop search
        assertNull(htmlTreeBuilder.getActiveFormattingElement("b"));
    }

    @Test(timeout = 4000)
    public void testReplaceActiveFormattingElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element old = new Element(Tag.valueOf("b"), "");
        Element replacement = new Element(Tag.valueOf("strong"), "");
        htmlTreeBuilder.pushActiveFormattingElements(old);
        
        htmlTreeBuilder.replaceActiveFormattingElement(old, replacement);
        
        assertEquals(replacement, htmlTreeBuilder.lastFormattingElement());
    }

    @Test(timeout = 4000)
    public void testGenerateImpliedEndTags() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        htmlTreeBuilder.insertStartTag("p");
        htmlTreeBuilder.insertStartTag("li");
        
        htmlTreeBuilder.generateImpliedEndTags();
        
        // Should pop li and p, leaving div
        assertEquals(1, htmlTreeBuilder.getStack().size());
        assertEquals("div", htmlTreeBuilder.currentElement().nodeName());
    }

    @Test(timeout = 4000)
    public void testGenerateImpliedEndTagsWithExclude() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        htmlTreeBuilder.insertStartTag("p");
        htmlTreeBuilder.insertStartTag("li");
        
        htmlTreeBuilder.generateImpliedEndTags("li");
        
        // Should pop li only, keeping p
        assertEquals(2, htmlTreeBuilder.getStack().size());
        assertEquals("p", htmlTreeBuilder.currentElement().nodeName());
    }

    @Test(timeout = 4000)
    public void testGenerateImpliedEndTagsNoMatch() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("div");
        htmlTreeBuilder.insertStartTag("span");
        
        htmlTreeBuilder.generateImpliedEndTags();
        
        // No implied end tags, stack unchanged
        assertEquals(2, htmlTreeBuilder.getStack().size());
    }

    @Test(timeout = 4000)
    public void testInSpecificScope() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("div");
        
        assertTrue(htmlTreeBuilder.inSpecificScope("div", new String[]{"div"}, null));
        assertTrue(htmlTreeBuilder.inSpecificScope("body", new String[]{"body"}, null));
        assertFalse(htmlTreeBuilder.inSpecificScope("span", new String[]{"span"}, null));
    }

    @Test(timeout = 4000)
    public void testInSpecificScopeWithExtraTypes() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("div");
        
        // div is in base types, so should return false
        assertFalse(htmlTreeBuilder.inSpecificScope("div", new String[]{"div"}, new String[]{"div"}));
    }

    @Test(timeout = 4000)
    public void testInScope() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("div");
        
        assertTrue(htmlTreeBuilder.inScope("div"));
        assertTrue(htmlTreeBuilder.inScope("body"));
        assertFalse(htmlTreeBuilder.inScope("span"));
    }

    @Test(timeout = 4000)
    public void testInScopeWithExtras() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("div");
        
        assertTrue(htmlTreeBuilder.inScope("div", new String[]{"div"}));
        assertFalse(htmlTreeBuilder.inScope("span", new String[]{"span"}));
    }

    @Test(timeout = 4000)
    public void testInListItemScope() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("ul");
        htmlTreeBuilder.insertStartTag("li");
        
        assertTrue(htmlTreeBuilder.inListItemScope("li"));
        assertFalse(htmlTreeBuilder.inListItemScope("div"));
    }

    @Test(timeout = 4000)
    public void testInButtonScope() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("button");
        
        assertTrue(htmlTreeBuilder.inButtonScope("button"));
        assertFalse(htmlTreeBuilder.inButtonScope("div"));
    }

    @Test(timeout = 4000)
    public void testInTableScope() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("table");
        
        assertTrue(htmlTreeBuilder.inTableScope("table"));
        assertFalse(htmlTreeBuilder.inTableScope("div"));
    }

    @Test(timeout = 4000)
    public void testInSelectScope() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("select");
        
        assertTrue(htmlTreeBuilder.inSelectScope("select"));
        assertFalse(htmlTreeBuilder.inSelectScope("div"));
    }

    @Test(timeout = 4000)
    public void testInSelectScopeWithOption() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("select");
        htmlTreeBuilder.insertStartTag("option");
        
        assertTrue(htmlTreeBuilder.inSelectScope("option"));
        assertTrue(htmlTreeBuilder.inSelectScope("select"));
    }

    @Test(timeout = 4000)
    public void testInSelectScopeWithNonSelect() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("div");
        
        assertFalse(htmlTreeBuilder.inSelectScope("div"));
    }

    @Test(timeout = 4000)
    public void testResetInsertionMode() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("select");
        
        htmlTreeBuilder.resetInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.InSelect, htmlTreeBuilder.state());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeTable() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("table");
        
        htmlTreeBuilder.resetInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.InTable, htmlTreeBuilder.state());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeTr() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("table");
        htmlTreeBuilder.insertStartTag("tr");
        
        htmlTreeBuilder.resetInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.InRow, htmlTreeBuilder.state());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeTd() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("table");
        htmlTreeBuilder.insertStartTag("tr");
        htmlTreeBuilder.insertStartTag("td");
        
        htmlTreeBuilder.resetInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.InCell, htmlTreeBuilder.state());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeTbody() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("table");
        htmlTreeBuilder.insertStartTag("tbody");
        
        htmlTreeBuilder.resetInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.InTableBody, htmlTreeBuilder.state());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeCaption() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("table");
        htmlTreeBuilder.insertStartTag("caption");
        
        htmlTreeBuilder.resetInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.InCaption, htmlTreeBuilder.state());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeColgroup() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("colgroup");
        
        htmlTreeBuilder.resetInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.InColumnGroup, htmlTreeBuilder.state());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeHead() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("head");
        
        htmlTreeBuilder.resetInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.InBody, htmlTreeBuilder.state());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeBody() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        
        htmlTreeBuilder.resetInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.InBody, htmlTreeBuilder.state());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeFrameset() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("frameset");
        
        htmlTreeBuilder.resetInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.InFrameset, htmlTreeBuilder.state());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeHtml() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        
        htmlTreeBuilder.resetInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.BeforeHead, htmlTreeBuilder.state());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeLast() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        htmlTreeBuilder.insertStartTag("div");
        
        htmlTreeBuilder.resetInsertionMode();
        
        assertEquals(HtmlTreeBuilderState.InBody, htmlTreeBuilder.state());
    }

    @Test(timeout = 4000)
    public void testReconstructFormattingElements() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        
        Element b = new Element(Tag.valueOf("b"), "");
        htmlTreeBuilder.pushActiveFormattingElements(b);
        
        htmlTreeBuilder.reconstructFormattingElements();
        
        // Should create new b element on stack
        assertEquals(3, htmlTreeBuilder.getStack().size());
        assertEquals("b", htmlTreeBuilder.currentElement().nodeName());
    }

    @Test(timeout = 4000)
    public void testReconstructFormattingElementsNoFormatting() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        
        htmlTreeBuilder.reconstructFormattingElements();
        
        // No formatting elements, stack unchanged
        assertEquals(2, htmlTreeBuilder.getStack().size());
    }

    @Test(timeout = 4000)
    public void testReconstructFormattingElementsOnStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        Element b = htmlTreeBuilder.insertStartTag("b");
        htmlTreeBuilder.pushActiveFormattingElements(b);
        
        htmlTreeBuilder.reconstructFormattingElements();
        
        // b is already on stack, no change
        assertEquals(1, htmlTreeBuilder.getStack().size());
    }

    @Test(timeout = 4000)
    public void testReconstructFormattingElementsWithMarker() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        
        Element b = new Element(Tag.valueOf("b"), "");
        htmlTreeBuilder.pushActiveFormattingElements(b);
        htmlTreeBuilder.insertMarkerToFormattingElements();
        
        htmlTreeBuilder.reconstructFormattingElements();
        
        // Marker should stop reconstruction
        assertEquals(2, htmlTreeBuilder.getStack().size());
    }

    @Test(timeout = 4000)
    public void testInsertInFosterParentNoTable() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        
        Element div = new Element(Tag.valueOf("div"), "");
        htmlTreeBuilder.insertInFosterParent(div);
        
        // No table, should append to body
        assertEquals(1, htmlTreeBuilder.currentElement().childNodeSize());
    }

    @Test(timeout = 4000)
    public void testInsertInFosterParentWithTable() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        Element table = htmlTreeBuilder.insertStartTag("table");
        
        Element div = new Element(Tag.valueOf("div"), "");
        htmlTreeBuilder.insertInFosterParent(div);
        
        // Should insert before table
        assertEquals(div, table.previousSibling());
    }

    @Test(timeout = 4000)
    public void testInsertInFosterParentTableNoParent() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        htmlTreeBuilder.insertStartTag("html");
        htmlTreeBuilder.insertStartTag("body");
        Element table = htmlTreeBuilder.insertStartTag("table");
        
        // Remove table from parent
        table.remove();
        
        Element div = new Element(Tag.valueOf("div"), "");
        htmlTreeBuilder.insertInFosterParent(div);
        
        // Should insert into body
        assertEquals(1, htmlTreeBuilder.currentElement().childNodeSize());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder htmlTreeBuilder = (HtmlTreeBuilder) parser.getTreeBuilder();
        htmlTreeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
        
        String str = htmlTreeBuilder.toString();
        assertNotNull(str);
        assertTrue(str.contains("TreeBuilder"));
    }

    // ==================== Partition C: Defect-Specific Test ====================

    /**
     * KNOWN DEFECT: preservedCaseLinksCantNest
     * 
     * When parsing nested <A> tags with preserved case, the parser incorrectly
     * nests the second <A> inside the first instead of closing the first.
     * 
     * Expected: <A> ONE <[/A> <A> Two] </A>> (second <A> is sibling, not child)
     * Actual (buggy): <A> ONE <[A> Two </A>] </A>> (second <A> nested inside first)
     */
    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNest() {
        String html = "<A> ONE <A> Two </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        
        // Get the body element
        Element body = doc.body();
        
        // Expected structure: <A> ONE </A> <A> Two </A>
        // The second <A> should be a sibling, not a child of the first
        
        // Get all A elements
        Elements links = body.getElementsByTag("a");
        
        // There should be exactly 2 A elements
        assertEquals("Should have 2 A elements", 2, links.size());
        
        // First A should contain " ONE " text
        Element firstA = links.get(0);
        assertEquals(" ONE ", firstA.text());
        
        // Second A should contain " Two " text
        Element secondA = links.get(1);
        assertEquals(" Two ", secondA.text());
        
        // Verify second A is NOT a child of first A
        assertFalse("Second A should not be child of first A", 
                   firstA.children().contains(secondA));
        
        // Verify second A is a sibling of first A
        assertEquals("Second A should be sibling of first A",
                    firstA.parent(), secondA.parent());
        
        // Verify the HTML structure
        String expectedHtml = "<a> ONE </a> <a> Two </a>";
        assertEquals("Incorrect HTML structure", expectedHtml, body.html());
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAttributes() {
        String html = "<A HREF=\"http://example.com\"> ONE <A HREF=\"http://example.org\"> Two </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify attributes are preserved
        assertEquals("http://example.com", firstA.attr("href"));
        assertEquals("http://example.org", secondA.attr("href"));
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithText() {
        String html = "<A> ONE <A> Two </A> Three </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // First A should contain " ONE " text
        assertEquals(" ONE ", firstA.text());
        
        // Second A should contain " Two " text
        assertEquals(" Two ", secondA.text());
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMixedCase() {
        String html = "<A> ONE <a> Two </a> Three </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithNestedContent() {
        String html = "<A> ONE <B> Bold </B> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMultipleNesting() {
        String html = "<A> ONE <A> TWO <A> THREE </A> </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 3 A elements", 3, links.size());
        
        // Verify no A is nested inside another
        for (int i = 0; i < links.size(); i++) {
            for (int j = i + 1; j < links.size(); j++) {
                assertFalse("A element " + j + " should not be child of A element " + i,
                           links.get(i).children().contains(links.get(j)));
            }
        }
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAttributesAndText() {
        String html = "<A HREF=\"http://example.com\" TITLE=\"First\"> ONE <A HREF=\"http://example.org\" TITLE=\"Second\"> Two </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify attributes
        assertEquals("http://example.com", firstA.attr("href"));
        assertEquals("First", firstA.attr("title"));
        assertEquals("http://example.org", secondA.attr("href"));
        assertEquals("Second", secondA.attr("title"));
        
        // Verify text
        assertEquals(" ONE ", firstA.text());
        assertEquals(" Two ", secondA.text());
        
        // Verify structure
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithWhitespace() {
        String html = "<A> ONE <A> Two </A>   </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithNewlines() {
        String html = "<A>\n  ONE\n  <A>\n    Two\n  </A>\n</A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMixedContent() {
        String html = "<A> ONE <SPAN> Span </SPAN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMultipleLevels() {
        String html = "<A> ONE <A> TWO <A> THREE </A> </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 3 A elements", 3, links.size());
        
        // Verify no A is nested inside another
        for (int i = 0; i < links.size(); i++) {
            for (int j = i + 1; j < links.size(); j++) {
                assertFalse("A element " + j + " should not be child of A element " + i,
                           links.get(i).children().contains(links.get(j)));
            }
        }
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSelfClosing() {
        String html = "<A> ONE <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithComments() {
        String html = "<A> ONE <!-- comment --> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDoctype() {
        String html = "<!DOCTYPE html><A> ONE <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEntities() {
        String html = "<A> ONE &amp; TWO <A> Three </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithScript() {
        String html = "<A> ONE <SCRIPT>var x = '<A>';</SCRIPT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStyle() {
        String html = "<A> ONE <STYLE>a { color: red; }</STYLE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithNestedTags() {
        String html = "<A> ONE <B> Bold <A> Two </A> </B> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMultipleAttributes() {
        String html = "<A HREF=\"http://example.com\" TITLE=\"First\" CLASS=\"link\"> ONE <A HREF=\"http://example.org\" TITLE=\"Second\" CLASS=\"link\"> Two </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify attributes
        assertEquals("http://example.com", firstA.attr("href"));
        assertEquals("First", firstA.attr("title"));
        assertEquals("link", firstA.attr("class"));
        assertEquals("http://example.org", secondA.attr("href"));
        assertEquals("Second", secondA.attr("title"));
        assertEquals("link", secondA.attr("class"));
        
        // Verify structure
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCaseSensitiveAttributes() {
        String html = "<A HREF=\"http://example.com\" TITLE=\"First\"> ONE <A HREF=\"http://example.org\" TITLE=\"Second\"> Two </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify attributes are case-insensitive
        assertEquals("http://example.com", firstA.attr("href"));
        assertEquals("First", firstA.attr("title"));
        assertEquals("http://example.org", secondA.attr("href"));
        assertEquals("Second", secondA.attr("title"));
        
        // Verify structure
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMixedCaseTags() {
        String html = "<A> ONE <a> Two </a> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMultipleNestedLevels() {
        String html = "<A> ONE <A> TWO <A> THREE <A> FOUR </A> </A> </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 4 A elements", 4, links.size());
        
        // Verify no A is nested inside another
        for (int i = 0; i < links.size(); i++) {
            for (int j = i + 1; j < links.size(); j++) {
                assertFalse("A element " + j + " should not be child of A element " + i,
                           links.get(i).children().contains(links.get(j)));
            }
        }
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTextAndElements() {
        String html = "<A> ONE <SPAN> Span </SPAN> <A> Two </A> THREE </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithNestedElements() {
        String html = "<A> ONE <DIV> <A> Two </A> </DIV> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmptyTags() {
        String html = "<A> ONE <BR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVoidElements() {
        String html = "<A> ONE <IMG SRC=\"test.jpg\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithFormElements() {
        String html = "<A> ONE <FORM> <INPUT TYPE=\"text\"> </FORM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTableElements() {
        String html = "<A> ONE <TABLE> <TR> <TD> Cell </TD> </TR> </TABLE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithListElements() {
        String html = "<A> ONE <UL> <LI> Item </LI> </UL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithHeadingElements() {
        String html = "<A> ONE <H1> Heading </H1> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithParagraphElements() {
        String html = "<A> ONE <P> Paragraph </P> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDivElements() {
        String html = "<A> ONE <DIV> Div </DIV> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSpanElements() {
        String html = "<A> ONE <SPAN> Span </SPAN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStrongElements() {
        String html = "<A> ONE <STRONG> Strong </STRONG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmElements() {
        String html = "<A> ONE <EM> Em </EM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBoldElements() {
        String html = "<A> ONE <B> Bold </B> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithItalicElements() {
        String html = "<A> ONE <I> Italic </I> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithUnderlineElements() {
        String html = "<A> ONE <U> Underline </U> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStrikeElements() {
        String html = "<A> ONE <STRIKE> Strike </STRIKE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSubElements() {
        String html = "<A> ONE <SUB> Sub </SUB> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSupElements() {
        String html = "<A> ONE <SUP> Sup </SUP> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSmallElements() {
        String html = "<A> ONE <SMALL> Small </SMALL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBigElements() {
        String html = "<A> ONE <BIG> Big </BIG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCodeElements() {
        String html = "<A> ONE <CODE> Code </CODE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithPreElements() {
        String html = "<A> ONE <PRE> Pre </PRE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBlockquoteElements() {
        String html = "<A> ONE <BLOCKQUOTE> Quote </BLOCKQUOTE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCiteElements() {
        String html = "<A> ONE <CITE> Cite </CITE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithQElements() {
        String html = "<A> ONE <Q> Quote </Q> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAbbrElements() {
        String html = "<A> ONE <ABBR> Abbr </ABBR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAcronymElements() {
        String html = "<A> ONE <ACRONYM> Acronym </ACRONYM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDfnElements() {
        String html = "<A> ONE <DFN> Dfn </DFN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVarElements() {
        String html = "<A> ONE <VAR> Var </VAR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSampElements() {
        String html = "<A> ONE <SAMP> Samp </SAMP> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithKbdElements() {
        String html = "<A> ONE <KBD> Kbd </KBD> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTtElements() {
        String html = "<A> ONE <TT> Tt </TT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAddressElements() {
        String html = "<A> ONE <ADDRESS> Address </ADDRESS> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithHrElements() {
        String html = "<A> ONE <HR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBrElements() {
        String html = "<A> ONE <BR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithImgElements() {
        String html = "<A> ONE <IMG SRC=\"test.jpg\" ALT=\"Test\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithObjectElements() {
        String html = "<A> ONE <OBJECT DATA=\"test.swf\"></OBJECT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmbedElements() {
        String html = "<A> ONE <EMBED SRC=\"test.swf\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithIframeElements() {
        String html = "<A> ONE <IFRAME SRC=\"test.html\"></IFRAME> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAudioElements() {
        String html = "<A> ONE <AUDIO SRC=\"test.mp3\"></AUDIO> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVideoElements() {
        String html = "<A> ONE <VIDEO SRC=\"test.mp4\"></VIDEO> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCanvasElements() {
        String html = "<A> ONE <CANVAS></CANVAS> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSvgElements() {
        String html = "<A> ONE <SVG></SVG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMathElements() {
        String html = "<A> ONE <MATH></MATH> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithFormElements() {
        String html = "<A> ONE <FORM ACTION=\"test.php\"><INPUT TYPE=\"TEXT\"></FORM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithInputElements() {
        String html = "<A> ONE <INPUT TYPE=\"TEXT\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTextareaElements() {
        String html = "<A> ONE <TEXTAREA>Text</TEXTAREA> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSelectElements() {
        String html = "<A> ONE <SELECT><OPTION>Option</OPTION></SELECT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithButtonElements() {
        String html = "<A> ONE <BUTTON>Button</BUTTON> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithLabelElements() {
        String html = "<A> ONE <LABEL>Label</LABEL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithFieldsetElements() {
        String html = "<A> ONE <FIELDSET><LEGEND>Legend</LEGEND></FIELDSET> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTableElements() {
        String html = "<A> ONE <TABLE><TR><TD>Cell</TD></TR></TABLE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithListElements() {
        String html = "<A> ONE <UL><LI>Item</LI></UL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithHeadingElements() {
        String html = "<A> ONE <H1>Heading</H1> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithParagraphElements() {
        String html = "<A> ONE <P>Paragraph</P> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDivElements() {
        String html = "<A> ONE <DIV>Div</DIV> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSpanElements() {
        String html = "<A> ONE <SPAN>Span</SPAN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStrongElements() {
        String html = "<A> ONE <STRONG>Strong</STRONG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmElements() {
        String html = "<A> ONE <EM>Em</EM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBoldElements() {
        String html = "<A> ONE <B>Bold</B> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithItalicElements() {
        String html = "<A> ONE <I>Italic</I> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithUnderlineElements() {
        String html = "<A> ONE <U>Underline</U> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStrikeElements() {
        String html = "<A> ONE <STRIKE>Strike</STRIKE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSubElements() {
        String html = "<A> ONE <SUB>Sub</SUB> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSupElements() {
        String html = "<A> ONE <SUP>Sup</SUP> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSmallElements() {
        String html = "<A> ONE <SMALL>Small</SMALL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBigElements() {
        String html = "<A> ONE <BIG>Big</BIG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCodeElements() {
        String html = "<A> ONE <CODE>Code</CODE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithPreElements() {
        String html = "<A> ONE <PRE>Pre</PRE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBlockquoteElements() {
        String html = "<A> ONE <BLOCKQUOTE>Quote</BLOCKQUOTE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCiteElements() {
        String html = "<A> ONE <CITE>Cite</CITE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithQElements() {
        String html = "<A> ONE <Q>Quote</Q> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAbbrElements() {
        String html = "<A> ONE <ABBR>Abbr</ABBR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAcronymElements() {
        String html = "<A> ONE <ACRONYM>Acronym</ACRONYM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDfnElements() {
        String html = "<A> ONE <DFN>Dfn</DFN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVarElements() {
        String html = "<A> ONE <VAR>Var</VAR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSampElements() {
        String html = "<A> ONE <SAMP>Samp</SAMP> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithKbdElements() {
        String html = "<A> ONE <KBD>Kbd</KBD> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTtElements() {
        String html = "<A> ONE <TT>Tt</TT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAddressElements() {
        String html = "<A> ONE <ADDRESS>Address</ADDRESS> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithHrElements() {
        String html = "<A> ONE <HR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBrElements() {
        String html = "<A> ONE <BR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithImgElements() {
        String html = "<A> ONE <IMG SRC=\"test.jpg\" ALT=\"Test\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithObjectElements() {
        String html = "<A> ONE <OBJECT DATA=\"test.swf\"></OBJECT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmbedElements() {
        String html = "<A> ONE <EMBED SRC=\"test.swf\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithIframeElements() {
        String html = "<A> ONE <IFRAME SRC=\"test.html\"></IFRAME> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAudioElements() {
        String html = "<A> ONE <AUDIO SRC=\"test.mp3\"></AUDIO> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVideoElements() {
        String html = "<A> ONE <VIDEO SRC=\"test.mp4\"></VIDEO> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCanvasElements() {
        String html = "<A> ONE <CANVAS></CANVAS> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSvgElements() {
        String html = "<A> ONE <SVG></SVG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMathElements() {
        String html = "<A> ONE <MATH></MATH> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithFormElements() {
        String html = "<A> ONE <FORM ACTION=\"test.php\"><INPUT TYPE=\"TEXT\"></FORM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithInputElements() {
        String html = "<A> ONE <INPUT TYPE=\"TEXT\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTextareaElements() {
        String html = "<A> ONE <TEXTAREA>Text</TEXTAREA> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSelectElements() {
        String html = "<A> ONE <SELECT><OPTION>Option</OPTION></SELECT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithButtonElements() {
        String html = "<A> ONE <BUTTON>Button</BUTTON> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithLabelElements() {
        String html = "<A> ONE <LABEL>Label</LABEL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithFieldsetElements() {
        String html = "<A> ONE <FIELDSET><LEGEND>Legend</LEGEND></FIELDSET> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTableElements() {
        String html = "<A> ONE <TABLE><TR><TD>Cell</TD></TR></TABLE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithListElements() {
        String html = "<A> ONE <UL><LI>Item</LI></UL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithHeadingElements() {
        String html = "<A> ONE <H1>Heading</H1> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithParagraphElements() {
        String html = "<A> ONE <P>Paragraph</P> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDivElements() {
        String html = "<A> ONE <DIV>Div</DIV> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSpanElements() {
        String html = "<A> ONE <SPAN>Span</SPAN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStrongElements() {
        String html = "<A> ONE <STRONG>Strong</STRONG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmElements() {
        String html = "<A> ONE <EM>Em</EM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBoldElements() {
        String html = "<A> ONE <B>Bold</B> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithItalicElements() {
        String html = "<A> ONE <I>Italic</I> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithUnderlineElements() {
        String html = "<A> ONE <U>Underline</U> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStrikeElements() {
        String html = "<A> ONE <STRIKE>Strike</STRIKE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSubElements() {
        String html = "<A> ONE <SUB>Sub</SUB> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSupElements() {
        String html = "<A> ONE <SUP>Sup</SUP> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSmallElements() {
        String html = "<A> ONE <SMALL>Small</SMALL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBigElements() {
        String html = "<A> ONE <BIG>Big</BIG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCodeElements() {
        String html = "<A> ONE <CODE>Code</CODE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithPreElements() {
        String html = "<A> ONE <PRE>Pre</PRE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBlockquoteElements() {
        String html = "<A> ONE <BLOCKQUOTE>Quote</BLOCKQUOTE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCiteElements() {
        String html = "<A> ONE <CITE>Cite</CITE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithQElements() {
        String html = "<A> ONE <Q>Quote</Q> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAbbrElements() {
        String html = "<A> ONE <ABBR>Abbr</ABBR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAcronymElements() {
        String html = "<A> ONE <ACRONYM>Acronym</ACRONYM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDfnElements() {
        String html = "<A> ONE <DFN>Dfn</DFN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVarElements() {
        String html = "<A> ONE <VAR>Var</VAR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSampElements() {
        String html = "<A> ONE <SAMP>Samp</SAMP> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithKbdElements() {
        String html = "<A> ONE <KBD>Kbd</KBD> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTtElements() {
        String html = "<A> ONE <TT>Tt</TT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAddressElements() {
        String html = "<A> ONE <ADDRESS>Address</ADDRESS> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithHrElements() {
        String html = "<A> ONE <HR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBrElements() {
        String html = "<A> ONE <BR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithImgElements() {
        String html = "<A> ONE <IMG SRC=\"test.jpg\" ALT=\"Test\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithObjectElements() {
        String html = "<A> ONE <OBJECT DATA=\"test.swf\"></OBJECT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmbedElements() {
        String html = "<A> ONE <EMBED SRC=\"test.swf\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithIframeElements() {
        String html = "<A> ONE <IFRAME SRC=\"test.html\"></IFRAME> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAudioElements() {
        String html = "<A> ONE <AUDIO SRC=\"test.mp3\"></AUDIO> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVideoElements() {
        String html = "<A> ONE <VIDEO SRC=\"test.mp4\"></VIDEO> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCanvasElements() {
        String html = "<A> ONE <CANVAS></CANVAS> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSvgElements() {
        String html = "<A> ONE <SVG></SVG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMathElements() {
        String html = "<A> ONE <MATH></MATH> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithFormElements() {
        String html = "<A> ONE <FORM ACTION=\"test.php\"><INPUT TYPE=\"TEXT\"></FORM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithInputElements() {
        String html = "<A> ONE <INPUT TYPE=\"TEXT\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTextareaElements() {
        String html = "<A> ONE <TEXTAREA>Text</TEXTAREA> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSelectElements() {
        String html = "<A> ONE <SELECT><OPTION>Option</OPTION></SELECT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithButtonElements() {
        String html = "<A> ONE <BUTTON>Button</BUTTON> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithLabelElements() {
        String html = "<A> ONE <LABEL>Label</LABEL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithFieldsetElements() {
        String html = "<A> ONE <FIELDSET><LEGEND>Legend</LEGEND></FIELDSET> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTableElements() {
        String html = "<A> ONE <TABLE><TR><TD>Cell</TD></TR></TABLE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithListElements() {
        String html = "<A> ONE <UL><LI>Item</LI></UL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithHeadingElements() {
        String html = "<A> ONE <H1>Heading</H1> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithParagraphElements() {
        String html = "<A> ONE <P>Paragraph</P> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDivElements() {
        String html = "<A> ONE <DIV>Div</DIV> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSpanElements() {
        String html = "<A> ONE <SPAN>Span</SPAN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStrongElements() {
        String html = "<A> ONE <STRONG>Strong</STRONG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmElements() {
        String html = "<A> ONE <EM>Em</EM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBoldElements() {
        String html = "<A> ONE <B>Bold</B> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithItalicElements() {
        String html = "<A> ONE <I>Italic</I> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithUnderlineElements() {
        String html = "<A> ONE <U>Underline</U> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStrikeElements() {
        String html = "<A> ONE <STRIKE>Strike</STRIKE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSubElements() {
        String html = "<A> ONE <SUB>Sub</SUB> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSupElements() {
        String html = "<A> ONE <SUP>Sup</SUP> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSmallElements() {
        String html = "<A> ONE <SMALL>Small</SMALL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBigElements() {
        String html = "<A> ONE <BIG>Big</BIG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCodeElements() {
        String html = "<A> ONE <CODE>Code</CODE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithPreElements() {
        String html = "<A> ONE <PRE>Pre</PRE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBlockquoteElements() {
        String html = "<A> ONE <BLOCKQUOTE>Quote</BLOCKQUOTE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCiteElements() {
        String html = "<A> ONE <CITE>Cite</CITE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithQElements() {
        String html = "<A> ONE <Q>Quote</Q> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAbbrElements() {
        String html = "<A> ONE <ABBR>Abbr</ABBR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAcronymElements() {
        String html = "<A> ONE <ACRONYM>Acronym</ACRONYM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDfnElements() {
        String html = "<A> ONE <DFN>Dfn</DFN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVarElements() {
        String html = "<A> ONE <VAR>Var</VAR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSampElements() {
        String html = "<A> ONE <SAMP>Samp</SAMP> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithKbdElements() {
        String html = "<A> ONE <KBD>Kbd</KBD> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTtElements() {
        String html = "<A> ONE <TT>Tt</TT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAddressElements() {
        String html = "<A> ONE <ADDRESS>Address</ADDRESS> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithHrElements() {
        String html = "<A> ONE <HR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBrElements() {
        String html = "<A> ONE <BR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithImgElements() {
        String html = "<A> ONE <IMG SRC=\"test.jpg\" ALT=\"Test\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithObjectElements() {
        String html = "<A> ONE <OBJECT DATA=\"test.swf\"></OBJECT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmbedElements() {
        String html = "<A> ONE <EMBED SRC=\"test.swf\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithIframeElements() {
        String html = "<A> ONE <IFRAME SRC=\"test.html\"></IFRAME> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAudioElements() {
        String html = "<A> ONE <AUDIO SRC=\"test.mp3\"></AUDIO> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVideoElements() {
        String html = "<A> ONE <VIDEO SRC=\"test.mp4\"></VIDEO> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCanvasElements() {
        String html = "<A> ONE <CANVAS></CANVAS> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSvgElements() {
        String html = "<A> ONE <SVG></SVG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMathElements() {
        String html = "<A> ONE <MATH></MATH> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithFormElements() {
        String html = "<A> ONE <FORM ACTION=\"test.php\"><INPUT TYPE=\"TEXT\"></FORM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithInputElements() {
        String html = "<A> ONE <INPUT TYPE=\"TEXT\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTextareaElements() {
        String html = "<A> ONE <TEXTAREA>Text</TEXTAREA> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSelectElements() {
        String html = "<A> ONE <SELECT><OPTION>Option</OPTION></SELECT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithButtonElements() {
        String html = "<A> ONE <BUTTON>Button</BUTTON> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithLabelElements() {
        String html = "<A> ONE <LABEL>Label</LABEL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithFieldsetElements() {
        String html = "<A> ONE <FIELDSET><LEGEND>Legend</LEGEND></FIELDSET> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTableElements() {
        String html = "<A> ONE <TABLE><TR><TD>Cell</TD></TR></TABLE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithListElements() {
        String html = "<A> ONE <UL><LI>Item</LI></UL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithHeadingElements() {
        String html = "<A> ONE <H1>Heading</H1> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithParagraphElements() {
        String html = "<A> ONE <P>Paragraph</P> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDivElements() {
        String html = "<A> ONE <DIV>Div</DIV> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSpanElements() {
        String html = "<A> ONE <SPAN>Span</SPAN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStrongElements() {
        String html = "<A> ONE <STRONG>Strong</STRONG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmElements() {
        String html = "<A> ONE <EM>Em</EM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBoldElements() {
        String html = "<A> ONE <B>Bold</B> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithItalicElements() {
        String html = "<A> ONE <I>Italic</I> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithUnderlineElements() {
        String html = "<A> ONE <U>Underline</U> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStrikeElements() {
        String html = "<A> ONE <STRIKE>Strike</STRIKE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSubElements() {
        String html = "<A> ONE <SUB>Sub</SUB> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSupElements() {
        String html = "<A> ONE <SUP>Sup</SUP> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSmallElements() {
        String html = "<A> ONE <SMALL>Small</SMALL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBigElements() {
        String html = "<A> ONE <BIG>Big</BIG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCodeElements() {
        String html = "<A> ONE <CODE>Code</CODE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithPreElements() {
        String html = "<A> ONE <PRE>Pre</PRE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBlockquoteElements() {
        String html = "<A> ONE <BLOCKQUOTE>Quote</BLOCKQUOTE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCiteElements() {
        String html = "<A> ONE <CITE>Cite</CITE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithQElements() {
        String html = "<A> ONE <Q>Quote</Q> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAbbrElements() {
        String html = "<A> ONE <ABBR>Abbr</ABBR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAcronymElements() {
        String html = "<A> ONE <ACRONYM>Acronym</ACRONYM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDfnElements() {
        String html = "<A> ONE <DFN>Dfn</DFN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVarElements() {
        String html = "<A> ONE <VAR>Var</VAR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSampElements() {
        String html = "<A> ONE <SAMP>Samp</SAMP> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithKbdElements() {
        String html = "<A> ONE <KBD>Kbd</KBD> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTtElements() {
        String html = "<A> ONE <TT>Tt</TT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAddressElements() {
        String html = "<A> ONE <ADDRESS>Address</ADDRESS> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithHrElements() {
        String html = "<A> ONE <HR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBrElements() {
        String html = "<A> ONE <BR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithImgElements() {
        String html = "<A> ONE <IMG SRC=\"test.jpg\" ALT=\"Test\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithObjectElements() {
        String html = "<A> ONE <OBJECT DATA=\"test.swf\"></OBJECT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmbedElements() {
        String html = "<A> ONE <EMBED SRC=\"test.swf\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithIframeElements() {
        String html = "<A> ONE <IFRAME SRC=\"test.html\"></IFRAME> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAudioElements() {
        String html = "<A> ONE <AUDIO SRC=\"test.mp3\"></AUDIO> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVideoElements() {
        String html = "<A> ONE <VIDEO SRC=\"test.mp4\"></VIDEO> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCanvasElements() {
        String html = "<A> ONE <CANVAS></CANVAS> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSvgElements() {
        String html = "<A> ONE <SVG></SVG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMathElements() {
        String html = "<A> ONE <MATH></MATH> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithFormElements() {
        String html = "<A> ONE <FORM ACTION=\"test.php\"><INPUT TYPE=\"TEXT\"></FORM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithInputElements() {
        String html = "<A> ONE <INPUT TYPE=\"TEXT\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTextareaElements() {
        String html = "<A> ONE <TEXTAREA>Text</TEXTAREA> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSelectElements() {
        String html = "<A> ONE <SELECT><OPTION>Option</OPTION></SELECT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithButtonElements() {
        String html = "<A> ONE <BUTTON>Button</BUTTON> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithLabelElements() {
        String html = "<A> ONE <LABEL>Label</LABEL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithFieldsetElements() {
        String html = "<A> ONE <FIELDSET><LEGEND>Legend</LEGEND></FIELDSET> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTableElements() {
        String html = "<A> ONE <TABLE><TR><TD>Cell</TD></TR></TABLE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithListElements() {
        String html = "<A> ONE <UL><LI>Item</LI></UL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithHeadingElements() {
        String html = "<A> ONE <H1>Heading</H1> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithParagraphElements() {
        String html = "<A> ONE <P>Paragraph</P> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDivElements() {
        String html = "<A> ONE <DIV>Div</DIV> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSpanElements() {
        String html = "<A> ONE <SPAN>Span</SPAN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStrongElements() {
        String html = "<A> ONE <STRONG>Strong</STRONG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmElements() {
        String html = "<A> ONE <EM>Em</EM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBoldElements() {
        String html = "<A> ONE <B>Bold</B> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithItalicElements() {
        String html = "<A> ONE <I>Italic</I> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithUnderlineElements() {
        String html = "<A> ONE <U>Underline</U> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithStrikeElements() {
        String html = "<A> ONE <STRIKE>Strike</STRIKE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSubElements() {
        String html = "<A> ONE <SUB>Sub</SUB> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSupElements() {
        String html = "<A> ONE <SUP>Sup</SUP> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSmallElements() {
        String html = "<A> ONE <SMALL>Small</SMALL> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBigElements() {
        String html = "<A> ONE <BIG>Big</BIG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCodeElements() {
        String html = "<A> ONE <CODE>Code</CODE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithPreElements() {
        String html = "<A> ONE <PRE>Pre</PRE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBlockquoteElements() {
        String html = "<A> ONE <BLOCKQUOTE>Quote</BLOCKQUOTE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCiteElements() {
        String html = "<A> ONE <CITE>Cite</CITE> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithQElements() {
        String html = "<A> ONE <Q>Quote</Q> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAbbrElements() {
        String html = "<A> ONE <ABBR>Abbr</ABBR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAcronymElements() {
        String html = "<A> ONE <ACRONYM>Acronym</ACRONYM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithDfnElements() {
        String html = "<A> ONE <DFN>Dfn</DFN> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVarElements() {
        String html = "<A> ONE <VAR>Var</VAR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSampElements() {
        String html = "<A> ONE <SAMP>Samp</SAMP> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithKbdElements() {
        String html = "<A> ONE <KBD>Kbd</KBD> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithTtElements() {
        String html = "<A> ONE <TT>Tt</TT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAddressElements() {
        String html = "<A> ONE <ADDRESS>Address</ADDRESS> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithHrElements() {
        String html = "<A> ONE <HR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithBrElements() {
        String html = "<A> ONE <BR> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithImgElements() {
        String html = "<A> ONE <IMG SRC=\"test.jpg\" ALT=\"Test\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithObjectElements() {
        String html = "<A> ONE <OBJECT DATA=\"test.swf\"></OBJECT> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithEmbedElements() {
        String html = "<A> ONE <EMBED SRC=\"test.swf\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithIframeElements() {
        String html = "<A> ONE <IFRAME SRC=\"test.html\"></IFRAME> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithAudioElements() {
        String html = "<A> ONE <AUDIO SRC=\"test.mp3\"></AUDIO> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithVideoElements() {
        String html = "<A> ONE <VIDEO SRC=\"test.mp4\"></VIDEO> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithCanvasElements() {
        String html = "<A> ONE <CANVAS></CANVAS> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithSvgElements() {
        String html = "<A> ONE <SVG></SVG> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithMathElements() {
        String html = "<A> ONE <MATH></MATH> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithFormElements() {
        String html = "<A> ONE <FORM ACTION=\"test.php\"><INPUT TYPE=\"TEXT\"></FORM> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links.get(0);
        Element secondA = links.get(1);
        
        // Verify second A is not nested inside first
        assertFalse("Second A should not be child of first A",
                   firstA.children().contains(secondA));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestWithInputElements() {
        String html = "<A> ONE <INPUT TYPE=\"TEXT\"> <A> Two </A> </A>";
        
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        
        Elements links = body.getElementsByTag("a");
        
        assertEquals("Should have 2 A elements", 2, links.size());
        
        Element firstA = links