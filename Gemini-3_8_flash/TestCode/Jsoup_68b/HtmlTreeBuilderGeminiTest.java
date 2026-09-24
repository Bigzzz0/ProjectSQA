/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.HtmlTreeBuilder
 *
 * Key Areas & Branches Targeted:
 * 1. Scope Search Logic (inSpecificScope, inScope, inButtonScope, inTableScope, inSelectScope, inListItemScope)
 *    - Stack size <= MaxScopeSearchDepth (100) vs > MaxScopeSearchDepth (100).
 *    - Boundary where target tag is at top of stack (pos > 100) vs within search range (0..100).
 *    - Known Defect (Defects4J - testHandlesDeepSpans / StackOverflowError):
 *      When stack depth exceeds MaxScopeSearchDepth (e.g. 200 <span> tags), calling inButtonScope("p")
 *      upon processing </p> improperly limits search start index to bottom = MaxScopeSearchDepth (100) instead
 *      of stack.size() - 1, missing the newly opened <p> tag at index > 100, causing endless recursive
 *      processStartTag("p") -> process(</p>) -> StackOverflowError.
 * 2. Tree Construction & Formatting Elements:
 *    - pushActiveFormattingElements (limit of 3 identical elements, marker handling).
 *    - reconstructFormattingElements (skip/increment steps, onStack vs unstacked, marker boundary).
 *    - clearFormattingElementsToLastMarker, removeFromActiveFormattingElements, isInActiveFormattingElements.
 * 3. Stack Manipulation & Context Clearing:
 *    - popStackToClose(String), popStackToClose(String...), popStackToBefore.
 *    - clearStackToTableContext, clearStackToTableBodyContext, clearStackToTableRowContext.
 *    - insertOnStackAfter, replaceOnStack, removeFromStack, aboveOnStack, getFromStack, onStack.
 * 4. Fragment Parsing & Insertion Modes:
 *    - parseFragment with null context, various context tags ("title", "textarea", "iframe", "script", "noscript", etc.).
 *    - resetInsertionMode across all states (InSelect, InCell, InRow, InTableBody, InCaption, InColumnGroup, InTable, etc.).
 *    - Context element hierarchy with FormElement parent linkage.
 * 5. Node Insertion Paths:
 *    - insert(StartTag), insert(Comment), insert(Character) - script/style DataNode vs TextNode.
 *    - insertEmpty with self-closing known tags (non-void error) vs unknown tags.
 *    - insertForm (onStack = true/false), setFormElement association with form-listed controls.
 *    - insertInFosterParent (table with parent, table without parent, frag without table).
 * 6. Base URI & Metadata Handling:
 *    - maybeSetBaseUri (first <base href> set, subsequent ignored, base without href ignored).
 *    - framesetOk toggling, error generation guard.
 */

package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderGeminiTest {

    private HtmlTreeBuilder builder;

    @Before
    public void setUp() {
        builder = new HtmlTreeBuilder();
    }

    private void initBuilder(String html) {
        builder.initialiseParse(new StringReader(html), "http://example.com/", new ParseErrorList(10, 10), ParseSettings.htmlDefault);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the StackOverflowError when parsing deep nested spans followed by a paragraph.
     * When stack size > MaxScopeSearchDepth (100), inSpecificScope should still accurately find
     * elements near the top of the stack (such as the <p> tag) without infinite recursion.
     */
    @Test(timeout = 4000)
    public void testHandlesDeepSpans() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("<span>");
        }
        sb.append("<p>One</p>");
        for (int i = 0; i < 200; i++) {
            sb.append("</span>");
        }

        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput(sb.toString(), "http://example.com/");
        assertNotNull("Document should be parsed without StackOverflowError", doc);
        assertEquals(200, doc.select("span").size());
        assertEquals(1, doc.select("p").size());
        assertEquals("One", doc.select("p").first().text());
    }

    /**
     * Direct unit test targeting inSpecificScope with deep stack (> 100 elements)
     * verifying that target element placed above depth 100 is correctly located.
     */
    @Test(timeout = 4000)
    public void testInSpecificScopeDeepStackDetection() {
        initBuilder("<html><body></body></html>");
        Element root = new Element(Tag.valueOf("html"), "");
        builder.getStack().add(root);

        // Fill stack with 120 div elements
        for (int i = 0; i < 120; i++) {
            Element div = new Element(Tag.valueOf("div"), "");
            builder.getStack().add(div);
        }

        // Add target element at stack top (depth 121)
        Element targetP = new Element(Tag.valueOf("p"), "");
        builder.getStack().add(targetP);

        // Check if inButtonScope or inScope identifies "p" at stack top
        boolean found = builder.inButtonScope("p");
        assertTrue("Target tag 'p' at top of deep stack must be found in scope", found);

        // Check target tag not present in scope
        assertFalse("Tag 'article' should not be found in scope", builder.inButtonScope("article"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStateTransitionsAndMarkers() {
        initBuilder("<html><head></head><body></body></html>");
        assertEquals(HtmlTreeBuilderState.Initial, builder.state());

        builder.transition(HtmlTreeBuilderState.BeforeHtml);
        assertEquals(HtmlTreeBuilderState.BeforeHtml, builder.state());

        builder.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.BeforeHtml, builder.originalState());

        builder.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, builder.state());
        assertEquals(HtmlTreeBuilderState.BeforeHtml, builder.originalState());
    }

    @Test(timeout = 4000)
    public void testFramesetOkToggle() {
        initBuilder("<html></html>");
        assertTrue(builder.framesetOk());
        builder.framesetOk(false);
        assertFalse(builder.framesetOk());
        builder.framesetOk(true);
        assertTrue(builder.framesetOk());
    }

    @Test(timeout = 4000)
    public void testStackPushPopAndQueries() {
        initBuilder("<html></html>");
        Element elHtml = new Element(Tag.valueOf("html"), "");
        Element elBody = new Element(Tag.valueOf("body"), "");
        Element elDiv = new Element(Tag.valueOf("div"), "");

        builder.push(elHtml);
        builder.push(elBody);
        builder.push(elDiv);

        assertTrue(builder.onStack(elHtml));
        assertTrue(builder.onStack(elDiv));
        assertSame(elDiv, builder.currentElement());

        Element above = builder.aboveOnStack(elDiv);
        assertSame(elBody, above);

        Element popped = builder.pop();
        assertSame(elDiv, popped);
        assertFalse(builder.onStack(elDiv));
        assertSame(elBody, builder.currentElement());

        assertSame(elBody, builder.getFromStack("body"));
        assertNull(builder.getFromStack("div"));

        boolean removed = builder.removeFromStack(elBody);
        assertTrue(removed);
        assertFalse(builder.onStack(elBody));
        assertFalse(builder.removeFromStack(elBody));
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseSingle() {
        initBuilder("<html></html>");
        Element el1 = new Element(Tag.valueOf("html"), "");
        Element el2 = new Element(Tag.valueOf("body"), "");
        Element el3 = new Element(Tag.valueOf("div"), "");
        Element el4 = new Element(Tag.valueOf("p"), "");

        builder.push(el1);
        builder.push(el2);
        builder.push(el3);
        builder.push(el4);

        builder.popStackToClose("div");
        assertEquals(2, builder.getStack().size());
        assertSame(el2, builder.currentElement());
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseMultiSorted() {
        initBuilder("<html></html>");
        Element el1 = new Element(Tag.valueOf("html"), "");
        Element el2 = new Element(Tag.valueOf("body"), "");
        Element el3 = new Element(Tag.valueOf("ul"), "");
        Element el4 = new Element(Tag.valueOf("li"), "");

        builder.push(el1);
        builder.push(el2);
        builder.push(el3);
        builder.push(el4);

        // TagSearchList is ["ol", "ul"]
        builder.popStackToClose(HtmlTreeBuilder.TagSearchList);
        assertEquals(2, builder.getStack().size());
        assertSame(el2, builder.currentElement());
    }

    @Test(timeout = 4000)
    public void testPopStackToBefore() {
        initBuilder("<html></html>");
        Element el1 = new Element(Tag.valueOf("html"), "");
        Element el2 = new Element(Tag.valueOf("body"), "");
        Element el3 = new Element(Tag.valueOf("div"), "");
        Element el4 = new Element(Tag.valueOf("span"), "");

        builder.push(el1);
        builder.push(el2);
        builder.push(el3);
        builder.push(el4);

        builder.popStackToBefore("div");
        assertEquals(3, builder.getStack().size());
        assertSame(el3, builder.currentElement());
    }

    @Test(timeout = 4000)
    public void testClearStackToContexts() {
        initBuilder("<html></html>");
        Element elHtml = new Element(Tag.valueOf("html"), "");
        Element elBody = new Element(Tag.valueOf("body"), "");
        Element elTable = new Element(Tag.valueOf("table"), "");
        Element elTbody = new Element(Tag.valueOf("tbody"), "");
        Element elTr = new Element(Tag.valueOf("tr"), "");
        Element elDiv = new Element(Tag.valueOf("div"), "");

        builder.push(elHtml);
        builder.push(elBody);
        builder.push(elTable);
        builder.push(elTbody);
        builder.push(elTr);
        builder.push(elDiv);

        builder.clearStackToTableRowContext();
        assertSame(elTr, builder.currentElement());

        builder.push(elDiv);
        builder.clearStackToTableBodyContext();
        assertSame(elTbody, builder.currentElement());

        builder.push(elTr);
        builder.push(elDiv);
        builder.clearStackToTableContext();
        assertSame(elTable, builder.currentElement());
    }

    @Test(timeout = 4000)
    public void testStackInsertAfterAndReplace() {
        initBuilder("<html></html>");
        Element el1 = new Element(Tag.valueOf("html"), "");
        Element el2 = new Element(Tag.valueOf("body"), "");
        Element el3 = new Element(Tag.valueOf("p"), "");

        builder.push(el1);
        builder.push(el2);

        builder.insertOnStackAfter(el1, el3);
        assertEquals(3, builder.getStack().size());
        assertSame(el1, builder.getStack().get(0));
        assertSame(el3, builder.getStack().get(1));
        assertSame(el2, builder.getStack().get(2));

        Element el4 = new Element(Tag.valueOf("div"), "");
        builder.replaceOnStack(el3, el4);
        assertEquals(3, builder.getStack().size());
        assertSame(el4, builder.getStack().get(1));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Scope Rules
    // =========================================================================

    @Test(timeout = 4000)
    public void testInScopeVariations() {
        initBuilder("<html></html>");
        Element elHtml = new Element(Tag.valueOf("html"), "");
        Element elBody = new Element(Tag.valueOf("body"), "");
        Element elTable = new Element(Tag.valueOf("table"), "");
        Element elTd = new Element(Tag.valueOf("td"), "");
        Element elP = new Element(Tag.valueOf("p"), "");

        builder.push(elHtml);
        builder.push(elBody);
        builder.push(elTable);
        builder.push(elTd);
        builder.push(elP);

        assertTrue(builder.inScope("p"));
        assertTrue(builder.inScope("td"));
        // "table" is a scope boundary for inScope (TagsSearchInScope contains table),
        // but within td scope, table is below td.
        assertTrue(builder.inTableScope("table"));
        assertFalse(builder.inTableScope("div"));

        Element elOl = new Element(Tag.valueOf("ol"), "");
        Element elLi = new Element(Tag.valueOf("li"), "");
        builder.push(elOl);
        builder.push(elLi);

        assertTrue(builder.inListItemScope("li"));
        assertFalse(builder.inListItemScope("body"));
    }

    @Test(timeout = 4000)
    public void testInSelectScope() {
        initBuilder("<html></html>");
        Element elHtml = new Element(Tag.valueOf("html"), "");
        Element elSelect = new Element(Tag.valueOf("select"), "");
        Element elOpt = new Element(Tag.valueOf("option"), "");

        builder.push(elHtml);
        builder.push(elSelect);
        builder.push(elOpt);

        assertTrue(builder.inSelectScope("option"));
        assertTrue(builder.inSelectScope("select"));
        assertFalse(builder.inSelectScope("html"));
    }

    @Test(timeout = 4000)
    public void testGenerateImpliedEndTags() {
        initBuilder("<html></html>");
        Element elHtml = new Element(Tag.valueOf("html"), "");
        Element elBody = new Element(Tag.valueOf("body"), "");
        Element elP = new Element(Tag.valueOf("p"), "");
        Element elDt = new Element(Tag.valueOf("dt"), "");

        builder.push(elHtml);
        builder.push(elBody);
        builder.push(elP);
        builder.push(elDt);

        // TagSearchEndTags contains dd, dt, li, optgroup, option, p, rp, rt
        builder.generateImpliedEndTags("p");
        // dt popped, p was excluded so remains top
        assertSame(elP, builder.currentElement());

        builder.generateImpliedEndTags();
        // p popped
        assertSame(elBody, builder.currentElement());
    }

    @Test(timeout = 4000)
    public void testActiveFormattingElementsTriplicateRule() {
        initBuilder("<html></html>");
        Attributes attr = new Attributes();
        attr.put("class", "bold");

        Element b1 = new Element(Tag.valueOf("b"), "", attr.clone());
        Element b2 = new Element(Tag.valueOf("b"), "", attr.clone());
        Element b3 = new Element(Tag.valueOf("b"), "", attr.clone());
        Element b4 = new Element(Tag.valueOf("b"), "", attr.clone());

        builder.pushActiveFormattingElements(b1);
        builder.pushActiveFormattingElements(b2);
        builder.pushActiveFormattingElements(b3);

        assertTrue(builder.isInActiveFormattingElements(b1));
        assertTrue(builder.isInActiveFormattingElements(b2));
        assertTrue(builder.isInActiveFormattingElements(b3));

        // 4th identical element causes the oldest (b1) to be evicted
        builder.pushActiveFormattingElements(b4);
        assertFalse(builder.isInActiveFormattingElements(b1));
        assertTrue(builder.isInActiveFormattingElements(b2));
        assertTrue(builder.isInActiveFormattingElements(b3));
        assertTrue(builder.isInActiveFormattingElements(b4));
    }

    @Test(timeout = 4000)
    public void testActiveFormattingElementsMarkers() {
        initBuilder("<html></html>");
        Element b1 = new Element(Tag.valueOf("b"), "");
        Element b2 = new Element(Tag.valueOf("b"), "");

        builder.pushActiveFormattingElements(b1);
        builder.insertMarkerToFormattingElements();
        builder.pushActiveFormattingElements(b2);

        assertSame(b2, builder.lastFormattingElement());
        assertSame(b2, builder.getActiveFormattingElement("b"));

        builder.clearFormattingElementsToLastMarker();
        // b2 and the marker removed; b1 remains
        assertSame(b1, builder.lastFormattingElement());

        builder.removeFromActiveFormattingElements(b1);
        assertNull(builder.lastFormattingElement());
        assertNull(builder.removeLastFormattingElement());
    }

    @Test(timeout = 4000)
    public void testReconstructFormattingElements() {
        initBuilder("<html></html>");
        Element elHtml = new Element(Tag.valueOf("html"), "");
        Element elBody = new Element(Tag.valueOf("body"), "");
        builder.push(elHtml);
        builder.push(elBody);

        Attributes attr = new Attributes();
        attr.put("id", "f1");
        Element formatB = new Element(Tag.valueOf("b"), "", attr);
        builder.pushActiveFormattingElements(formatB);

        // formatB is in formatting elements but not on stack
        assertFalse(builder.onStack(formatB));
        builder.reconstructFormattingElements();

        // formatB should be reconstructed and pushed onto stack
        assertTrue(builder.onStack(builder.currentElement()));
        assertEquals("b", builder.currentElement().nodeName());
        assertEquals("f1", builder.currentElement().id());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testInsertOnStackAfterInvalidThrowsValidationException() {
        initBuilder("<html></html>");
        Element el1 = new Element(Tag.valueOf("html"), "");
        Element elNotOnStack = new Element(Tag.valueOf("div"), "");
        Element elNew = new Element(Tag.valueOf("span"), "");
        builder.push(el1);

        try {
            builder.insertOnStackAfter(elNotOnStack, elNew);
            fail("Expected IllegalArgumentException / ValidationException for missing element");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must be true"));
        }
    }

    @Test(timeout = 4000)
    public void testReplaceOnStackInvalidThrowsValidationException() {
        initBuilder("<html></html>");
        Element el1 = new Element(Tag.valueOf("html"), "");
        Element elNotOnStack = new Element(Tag.valueOf("div"), "");
        Element elNew = new Element(Tag.valueOf("span"), "");
        builder.push(el1);

        try {
            builder.replaceOnStack(elNotOnStack, elNew);
            fail("Expected IllegalArgumentException / ValidationException for missing element");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must be true"));
        }
    }

    @Test(timeout = 4000)
    public void testBaseUriHandling() {
        initBuilder("<html><head></head><body></body></html>");
        assertEquals("http://example.com/", builder.getBaseUri());

        Element baseNoHref = new Element(Tag.valueOf("base"), "");
        builder.maybeSetBaseUri(baseNoHref);
        assertEquals("http://example.com/", builder.getBaseUri());

        Element baseValid = new Element(Tag.valueOf("base"), "");
        baseValid.attr("href", "http://updated.example.com/");
        baseValid.setBaseUri("http://example.com/");
        builder.maybeSetBaseUri(baseValid);
        assertEquals("http://updated.example.com/", builder.getBaseUri());
        assertEquals("http://updated.example.com/", builder.getDocument().baseUri());

        // Subsequent base tag should be ignored
        Element baseSecond = new Element(Tag.valueOf("base"), "");
        baseSecond.attr("href", "http://ignored.example.com/");
        builder.maybeSetBaseUri(baseSecond);
        assertEquals("http://updated.example.com/", builder.getBaseUri());
    }

    @Test(timeout = 4000)
    public void testInsertNodesAndCharacters() {
        initBuilder("<html><body></body></html>");
        Element elHtml = new Element(Tag.valueOf("html"), "");
        Element elBody = new Element(Tag.valueOf("body"), "");
        builder.push(elHtml);
        builder.push(elBody);

        // Insert standard character -> TextNode
        Token.Character charToken = new Token.Character();
        charToken.data("Hello text");
        builder.insert(charToken);
        assertEquals(1, elBody.childNodeSize());
        assertTrue(elBody.childNode(0) instanceof TextNode);

        // Insert in script context -> DataNode
        Element scriptEl = new Element(Tag.valueOf("script"), "");
        builder.push(scriptEl);
        Token.Character scriptChar = new Token.Character();
        scriptChar.data("var x = 10;");
        builder.insert(scriptChar);
        assertEquals(1, scriptEl.childNodeSize());
        assertTrue(scriptEl.childNode(0) instanceof DataNode);
        builder.pop();

        // Insert comment
        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("Test Comment");
        builder.insert(commentToken);
        assertEquals(2, elBody.childNodeSize());
        assertTrue(elBody.childNode(1) instanceof Comment);
    }

    @Test(timeout = 4000)
    public void testInsertEmptyAndForms() {
        initBuilder("<html><body></body></html>");
        Element elHtml = new Element(Tag.valueOf("html"), "");
        Element elBody = new Element(Tag.valueOf("body"), "");
        builder.push(elHtml);
        builder.push(elBody);

        Token.StartTag startTagEmpty = new Token.StartTag();
        startTagEmpty.nameAttr("img", new Attributes());
        startTagEmpty.selfClosing = true;
        Element img = builder.insertEmpty(startTagEmpty);
        assertNotNull(img);
        assertEquals("img", img.nodeName());

        Token.StartTag formTag = new Token.StartTag();
        formTag.nameAttr("form", new Attributes());
        FormElement form = builder.insertForm(formTag, true);
        assertNotNull(form);
        assertSame(form, builder.getFormElement());
        assertSame(form, builder.currentElement());

        // Form listing association
        Token.StartTag inputTag = new Token.StartTag();
        inputTag.nameAttr("input", new Attributes());
        Element input = builder.insertEmpty(inputTag);
        assertEquals(1, form.elements().size());
        assertSame(input, form.elements().get(0));
    }

    @Test(timeout = 4000)
    public void testFosterInsertWithAndWithoutTableParent() {
        initBuilder("<html><body></body></html>");
        Element elHtml = new Element(Tag.valueOf("html"), "");
        Element elBody = new Element(Tag.valueOf("body"), "");
        Element elTable = new Element(Tag.valueOf("table"), "");
        elBody.appendChild(elTable);

        builder.push(elHtml);
        builder.push(elBody);
        builder.push(elTable);

        builder.setFosterInserts(true);
        assertTrue(builder.isFosterInserts());

        TextNode fosterNode = new TextNode("Fostered Text");
        builder.insertInFosterParent(fosterNode);

        // Since elTable has a parent (elBody), fosterNode should be inserted before elTable
        assertSame(fosterNode, elBody.childNode(0));
        assertSame(elTable, elBody.childNode(1));

        // Foster insert when no table is in stack
        builder.getStack().clear();
        builder.push(elHtml);
        TextNode fosterNode2 = new TextNode("Fostered Root");
        builder.insertInFosterParent(fosterNode2);
        assertSame(fosterNode2, elHtml.childNode(elHtml.childNodeSize() - 1));
    }

    @Test(timeout = 4000)
    public void testFragmentParsingVariousContexts() {
        ParseErrorList errorList = new ParseErrorList(10, 10);
        ParseSettings settings = ParseSettings.htmlDefault;

        // Context = textarea -> transitions to Rcdata
        Element contextTextarea = new Element(Tag.valueOf("textarea"), "");
        List<Node> nodes = builder.parseFragment("<b>bold</b> &amp; more", contextTextarea, "http://example.com/", errorList, settings);
        assertFalse(nodes.isEmpty());
        assertTrue(builder.isFragmentParsing());

        // Context = style -> transitions to Rawtext
        Element contextStyle = new Element(Tag.valueOf("style"), "");
        List<Node> styleNodes = builder.parseFragment("body { color: red; }", contextStyle, "http://example.com/", errorList, settings);
        assertFalse(styleNodes.isEmpty());

        // Context with form parent chain
        FormElement parentForm = new FormElement(Tag.valueOf("form"), "", new Attributes());
        Element divInForm = new Element(Tag.valueOf("div"), "");
        parentForm.appendChild(divInForm);
        List<Node> formControlNodes = builder.parseFragment("<input type='text' name='q' />", divInForm, "http://example.com/", errorList, settings);
        assertFalse(formControlNodes.isEmpty());
        assertSame(parentForm, builder.getFormElement());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeVariousTags() {
        initBuilder("<html></html>");
        Element elHtml = new Element(Tag.valueOf("html"), "");
        builder.push(elHtml);

        Element elSelect = new Element(Tag.valueOf("select"), "");
        builder.push(elSelect);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InSelect, builder.state());
        builder.pop();

        Element elTable = new Element(Tag.valueOf("table"), "");
        builder.push(elTable);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTable, builder.state());

        Element elCaption = new Element(Tag.valueOf("caption"), "");
        builder.push(elCaption);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCaption, builder.state());
        builder.pop();

        Element elTbody = new Element(Tag.valueOf("tbody"), "");
        builder.push(elTbody);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTableBody, builder.state());

        Element elTr = new Element(Tag.valueOf("tr"), "");
        builder.push(elTr);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InRow, builder.state());

        Element elTd = new Element(Tag.valueOf("td"), "");
        builder.push(elTd);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, builder.state());
        builder.pop();
        builder.pop();
        builder.pop();
        builder.pop();

        Element elFrameset = new Element(Tag.valueOf("frameset"), "");
        builder.push(elFrameset);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InFrameset, builder.state());
    }

    @Test(timeout = 4000)
    public void testSpecialTagsAndPendingTableCharacters() {
        initBuilder("<html></html>");
        assertTrue(builder.isSpecial(new Element(Tag.valueOf("p"), "")));
        assertTrue(builder.isSpecial(new Element(Tag.valueOf("table"), "")));
        assertTrue(builder.isSpecial(new Element(Tag.valueOf("script"), "")));
        assertFalse(builder.isSpecial(new Element(Tag.valueOf("span"), "")));
        assertFalse(builder.isSpecial(new Element(Tag.valueOf("custom-tag"), "")));

        List<String> pending = new ArrayList<>();
        pending.add("foo");
        builder.setPendingTableCharacters(pending);
        assertSame(pending, builder.getPendingTableCharacters());

        builder.newPendingTableCharacters();
        assertNotSame(pending, builder.getPendingTableCharacters());
        assertTrue(builder.getPendingTableCharacters().isEmpty());
    }

    @Test(timeout = 4000)
    public void testToStringContract() {
        initBuilder("<html></html>");
        String repr = builder.toString();
        assertNotNull(repr);
        assertTrue(repr.startsWith("TreeBuilder{"));
        assertTrue(repr.contains("state="));
    }
}