package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: org.jsoup.parser.HtmlTreeBuilder
 *
 * PARTITION A: Core Functional Logic & State Transitions
 * - resetInsertionMode: All branch decisions (select, td, th, tr, tbody, thead, tfoot, caption,
 *   colgroup, table, head, body, frameset, html, and last element / context fallback).
 * - Stack manipulation: push, pop, onStack, getFromStack, removeFromStack, popStackToClose (single & varargs),
 *   popStackToBefore, aboveOnStack, insertOnStackAfter, replaceOnStack.
 * - Active formatting elements: pushActiveFormattingElements (duplicate detection >= 3),
 *   reconstructFormattingElements, clearFormattingElementsToLastMarker, removeFromActiveFormattingElements,
 *   isInActiveFormattingElements, getActiveFormattingElement, replaceActiveFormattingElement,
 *   insertMarkerToFormattingElements.
 * - Pending table characters and Foster insertion: insertInFosterParent (table with parent, table without
 *   parent, no table on stack).
 *
 * PARTITION B: Boundary Value Analysis (BVA) & Extremes
 * - maybeSetBaseUri: href present vs missing vs empty; immutable baseUriSetFromDoc guard.
 * - generateImpliedEndTags: default null exclusion vs explicit excluded tag; TagSearchEndTags boundary.
 * - isSpecial: Tags in TagSearchSpecial list vs ordinary inline tags.
 * - Scope checking: inScope, inListItemScope, inButtonScope, inTableScope, inSelectScope.
 * - Character insertion: script/style (DataNode) vs normal elements (TextNode).
 *
 * PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 * - Known Defect: Tokeniser error handling on self-closing non-void vs void tags.
 *   Non-void self-closing tags (e.g. <div/>) must record "Tag cannot be self closing; not a void tag",
 *   while valid self-closing void tags (e.g. <img/>) must acknowledge self-closing flag without error.
 *
 * PARTITION D: Exception & Defensive Guard Paths
 * - stack insertion/replacement with non-existent targets (Validate.isTrue failure).
 * - inSelectScope reaching bottom without hitting select scope boundaries (Validate.fail).
 *
 * PARTITION E: Fragment Parsing & Context Handling
 * - parseFragment: context null vs non-null (title/textarea -> Rcdata; style/iframe -> Rawtext;
 *   script -> ScriptData; plaintext/noscript -> Data; ancestor form element resolution; quirks mode).
 * ----------------------------------------------------------------------------------------------------
 */
public class HtmlTreeBuilderGeminiTest {

    private HtmlTreeBuilder createInitializedTreeBuilder(String baseUri) {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), baseUri, ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        return tb;
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStateAndOriginalStateTransitions() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        assertNull(tb.originalState());

        tb.transition(HtmlTreeBuilderState.BeforeHtml);
        assertEquals(HtmlTreeBuilderState.BeforeHtml, tb.state());

        tb.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.BeforeHtml, tb.originalState());

        tb.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
        assertEquals(HtmlTreeBuilderState.BeforeHtml, tb.originalState());
    }

    @Test(timeout = 4000)
    public void testFramesetOkFlag() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        assertTrue(tb.framesetOk());

        tb.framesetOk(false);
        assertFalse(tb.framesetOk());

        tb.framesetOk(true);
        assertTrue(tb.framesetOk());
    }

    @Test(timeout = 4000)
    public void testStackPushPopAndQueries() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");

        tb.push(html);
        tb.push(body);
        tb.push(p);

        assertEquals(3, tb.getStack().size());
        assertTrue(tb.onStack(body));
        assertSame(p, tb.getFromStack("p"));
        assertNull(tb.getFromStack("div"));

        assertSame(body, tb.aboveOnStack(p));
        assertSame(html, tb.aboveOnStack(body));

        Element popped = tb.pop();
        assertSame(p, popped);
        assertFalse(tb.onStack(p));

        boolean removed = tb.removeFromStack(body);
        assertTrue(removed);
        assertFalse(tb.onStack(body));

        assertFalse(tb.removeFromStack(body));
    }

    @Test(timeout = 4000)
    public void testInsertOnStackAfterAndReplaceOnStack() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element span = new Element(Tag.valueOf("span"), "http://example.com");

        tb.push(html);
        tb.push(body);

        tb.insertOnStackAfter(html, div);
        assertEquals(3, tb.getStack().size());
        assertSame(div, tb.getStack().get(1));

        tb.replaceOnStack(div, span);
        assertEquals(3, tb.getStack().size());
        assertSame(span, tb.getStack().get(1));
        assertFalse(tb.onStack(div));
        assertTrue(tb.onStack(span));
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseSingleAndVarargs() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");

        tb.push(html);
        tb.push(body);
        tb.push(div);
        tb.push(p);

        tb.popStackToClose("div");
        assertEquals(2, tb.getStack().size());
        assertFalse(tb.onStack(div));
        assertFalse(tb.onStack(p));

        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        tb.push(span);
        tb.popStackToClose("body", "table");
        assertEquals(1, tb.getStack().size());
        assertSame(html, tb.getStack().get(0));
    }

    @Test(timeout = 4000)
    public void testPopStackToBefore() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");

        tb.push(html);
        tb.push(body);
        tb.push(div);
        tb.push(p);

        tb.popStackToBefore("body");
        assertEquals(2, tb.getStack().size());
        assertSame(body, tb.getStack().get(1));
    }

    @Test(timeout = 4000)
    public void testClearStackToContexts() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element table = new Element(Tag.valueOf("table"), "http://example.com");
        Element tbody = new Element(Tag.valueOf("tbody"), "http://example.com");
        Element tr = new Element(Tag.valueOf("tr"), "http://example.com");
        Element td = new Element(Tag.valueOf("td"), "http://example.com");

        tb.push(html);
        tb.push(table);
        tb.push(tbody);
        tb.push(tr);
        tb.push(td);

        tb.clearStackToTableRowContext();
        assertEquals(4, tb.getStack().size());
        assertSame(tr, tb.getStack().get(3));

        tb.clearStackToTableBodyContext();
        assertEquals(3, tb.getStack().size());
        assertSame(tbody, tb.getStack().get(2));

        tb.clearStackToTableContext();
        assertEquals(2, tb.getStack().size());
        assertSame(table, tb.getStack().get(1));
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeComprehensiveBranches() {
        String[] tags = new String[]{
            "select", "td", "th", "tr", "tbody", "thead", "tfoot",
            "caption", "colgroup", "table", "head", "body", "frameset", "html"
        };
        HtmlTreeBuilderState[] expectedStates = new HtmlTreeBuilderState[]{
            HtmlTreeBuilderState.InSelect, HtmlTreeBuilderState.InCell, HtmlTreeBuilderState.InCell,
            HtmlTreeBuilderState.InRow, HtmlTreeBuilderState.InTableBody, HtmlTreeBuilderState.InTableBody,
            HtmlTreeBuilderState.InTableBody, HtmlTreeBuilderState.InCaption, HtmlTreeBuilderState.InColumnGroup,
            HtmlTreeBuilderState.InTable, HtmlTreeBuilderState.InBody, HtmlTreeBuilderState.InBody,
            HtmlTreeBuilderState.InFrameset, HtmlTreeBuilderState.BeforeHead
        };

        for (int i = 0; i < tags.length; i++) {
            HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
            Element html = new Element(Tag.valueOf("html"), "http://example.com");
            Element target = new Element(Tag.valueOf(tags[i]), "http://example.com");
            Element child = new Element(Tag.valueOf("span"), "http://example.com");

            tb.push(html);
            tb.push(target);
            tb.push(child);

            tb.resetInsertionMode();
            assertEquals("Failed on tag: " + tags[i], expectedStates[i], tb.state());
        }
    }

    @Test(timeout = 4000)
    public void testActiveFormattingElementsAndReconstruction() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        tb.push(html);
        tb.push(body);

        Element a1 = new Element(Tag.valueOf("a"), "http://example.com");
        a1.attr("href", "http://jsoup.org");
        Element a2 = new Element(Tag.valueOf("a"), "http://example.com");
        a2.attr("href", "http://jsoup.org");
        Element a3 = new Element(Tag.valueOf("a"), "http://example.com");
        a3.attr("href", "http://jsoup.org");
        Element a4 = new Element(Tag.valueOf("a"), "http://example.com");
        a4.attr("href", "http://jsoup.org");

        tb.pushActiveFormattingElements(a1);
        tb.pushActiveFormattingElements(a2);
        tb.pushActiveFormattingElements(a3);
        assertTrue(tb.isInActiveFormattingElements(a1));

        // Pushing 4th identical element should drop a1 due to 3-duplicate limit
        tb.pushActiveFormattingElements(a4);
        assertFalse(tb.isInActiveFormattingElements(a1));
        assertTrue(tb.isInActiveFormattingElements(a4));

        tb.insertMarkerToFormattingElements();
        assertNull(tb.lastFormattingElement());

        Element bold = new Element(Tag.valueOf("b"), "http://example.com");
        tb.pushActiveFormattingElements(bold);
        assertSame(bold, tb.lastFormattingElement());
        assertSame(bold, tb.getActiveFormattingElement("b"));

        tb.clearFormattingElementsToLastMarker();
        assertNotNull(tb.lastFormattingElement());
        assertFalse(tb.isInActiveFormattingElements(bold));

        // Reconstruct formatting elements when not on stack
        Element italic = new Element(Tag.valueOf("i"), "http://example.com");
        tb.pushActiveFormattingElements(italic);
        tb.reconstructFormattingElements();
        assertTrue(tb.onStack(tb.lastFormattingElement()));
    }

    @Test(timeout = 4000)
    public void testPendingTableCharacters() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        assertTrue(tb.getPendingTableCharacters().isEmpty());

        List<String> chars = new ArrayList<>();
        chars.add("a");
        chars.add("b");
        tb.setPendingTableCharacters(chars);
        assertEquals(2, tb.getPendingTableCharacters().size());

        tb.newPendingTableCharacters();
        assertTrue(tb.getPendingTableCharacters().isEmpty());
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testMaybeSetBaseUriBoundary() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://initial.com");

        // Element without href should not set baseUri
        Element baseWithoutHref = new Element(Tag.valueOf("base"), "http://initial.com");
        tb.maybeSetBaseUri(baseWithoutHref);
        assertEquals("http://initial.com", tb.getBaseUri());

        // First valid base with href should set baseUri
        Element baseWithHref = new Element(Tag.valueOf("base"), "http://initial.com");
        baseWithHref.attr("href", "http://updated.com/path/");
        tb.maybeSetBaseUri(baseWithHref);
        assertEquals("http://updated.com/path/", tb.getBaseUri());

        // Subsequent valid base must be ignored
        Element baseSecond = new Element(Tag.valueOf("base"), "http://updated.com/path/");
        baseSecond.attr("href", "http://ignored.com/");
        tb.maybeSetBaseUri(baseSecond);
        assertEquals("http://updated.com/path/", tb.getBaseUri());
    }

    @Test(timeout = 4000)
    public void testGenerateImpliedEndTagsWithAndWithoutExclusion() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        Element li = new Element(Tag.valueOf("li"), "http://example.com");

        tb.push(html);
        tb.push(body);
        tb.push(p);
        tb.push(li);

        tb.generateImpliedEndTags("p");
        // li popped, p kept because it is excluded
        assertEquals(3, tb.getStack().size());
        assertSame(p, tb.currentElement());

        tb.generateImpliedEndTags();
        // p popped
        assertEquals(2, tb.getStack().size());
        assertSame(body, tb.currentElement());
    }

    @Test(timeout = 4000)
    public void testIsSpecialTagCheck() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        Element img = new Element(Tag.valueOf("img"), "http://example.com");
        Element custom = new Element(Tag.valueOf("custom-tag"), "http://example.com");

        assertTrue(tb.isSpecial(div));
        assertTrue(tb.isSpecial(img));
        assertFalse(tb.isSpecial(span));
        assertFalse(tb.isSpecial(custom));
    }

    @Test(timeout = 4000)
    public void testScopeMethods() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element table = new Element(Tag.valueOf("table"), "http://example.com");
        Element tr = new Element(Tag.valueOf("tr"), "http://example.com");
        Element td = new Element(Tag.valueOf("td"), "http://example.com");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");

        tb.push(html);
        tb.push(table);
        tb.push(tr);
        tb.push(td);
        tb.push(p);

        assertTrue(tb.inScope("p"));
        assertTrue(tb.inTableScope("table"));
        assertFalse(tb.inTableScope("tr"));
        assertTrue(tb.inListItemScope("td"));
        assertTrue(tb.inButtonScope("td"));

        tb.pop(); // pop p
        tb.pop(); // pop td
        tb.pop(); // pop tr
        assertFalse(tb.inScope("td"));
    }

    @Test(timeout = 4000)
    public void testCharacterInsertionScriptStyleVsNormal() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element script = new Element(Tag.valueOf("script"), "http://example.com");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");

        tb.push(html);
        tb.push(script);

        Token.Character charToken = new Token.Character();
        charToken.data("var x = 1;");
        tb.insert(charToken);

        assertEquals(1, script.childNodeSize());
        assertTrue(script.childNode(0) instanceof DataNode);

        tb.pop();
        tb.push(p);
        charToken.data("Normal text");
        tb.insert(charToken);

        assertEquals(1, p.childNodeSize());
        assertTrue(p.childNode(0) instanceof TextNode);
    }

    @Test(timeout = 4000)
    public void testInsertInFosterParentVariants() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element table = new Element(Tag.valueOf("table"), "http://example.com");
        Element divWrapper = new Element(Tag.valueOf("div"), "http://example.com");

        // Variant 1: Table has a parent -> foster parent is table.parent(), inserted before table
        divWrapper.appendChild(table);
        tb.push(html);
        tb.push(divWrapper);
        tb.push(table);

        Element fosterChild1 = new Element(Tag.valueOf("span"), "http://example.com");
        tb.setFosterInserts(true);
        tb.insertInFosterParent(fosterChild1);

        assertEquals(2, divWrapper.childNodeSize());
        assertSame(fosterChild1, divWrapper.childNode(0));
        assertSame(table, divWrapper.childNode(1));

        // Variant 2: Table has no DOM parent -> foster parent is aboveOnStack(table)
        HtmlTreeBuilder tb2 = createInitializedTreeBuilder("http://example.com");
        Element html2 = new Element(Tag.valueOf("html"), "http://example.com");
        Element body2 = new Element(Tag.valueOf("body"), "http://example.com");
        Element unparentedTable = new Element(Tag.valueOf("table"), "http://example.com");

        tb2.push(html2);
        tb2.push(body2);
        tb2.push(unparentedTable);

        Element fosterChild2 = new Element(Tag.valueOf("b"), "http://example.com");
        tb2.insertInFosterParent(fosterChild2);
        assertSame(body2, fosterChild2.parent());

        // Variant 3: No table on stack -> foster parent is stack.get(0)
        HtmlTreeBuilder tb3 = createInitializedTreeBuilder("http://example.com");
        Element html3 = new Element(Tag.valueOf("html"), "http://example.com");
        tb3.push(html3);

        Element fosterChild3 = new Element(Tag.valueOf("i"), "http://example.com");
        tb3.insertInFosterParent(fosterChild3);
        assertSame(html3, fosterChild3.parent());
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testSelfClosingOnNonvoidIsErrorDefect() {
        // Defects4J bug: <div/> should register error "Tag cannot be self closing; not a void tag"
        String html = "<p>Check</p><div/><div>Hello</div>";
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput(html, "");
        assertEquals(1, parser.getErrors().size());
        assertEquals("18: Tag cannot be self closing; not a void tag", parser.getErrors().get(0).toString());
    }

    @Test(timeout = 4000)
    public void testSelfClosingVoidIsNotAnErrorDefect() {
        // Defects4J bug: <img/> is a void tag and self-closing is fully permitted, so 0 errors expected
        String html = "<p>嵌<img src='foo'/><span>Bar</span></p>";
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput(html, "");
        assertEquals(0, parser.getErrors().size());
    }

    @Test(timeout = 4000)
    public void testTracksErrorsWhenRequestedDefect() {
        // Defect: Self-closing flag must be acknowledged correctly or flagged as not a void tag
        String html = "<div/>";
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput(html, "");
        assertEquals(1, parser.getErrors().size());
        assertTrue(parser.getErrors().get(0).toString().contains("Tag cannot be self closing; not a void tag"));
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertOnStackAfterThrowsWhenElementNotFound() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element notOnStack = new Element(Tag.valueOf("p"), "http://example.com");
        Element toInsert = new Element(Tag.valueOf("span"), "http://example.com");

        tb.push(html);
        tb.insertOnStackAfter(notOnStack, toInsert);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceOnStackThrowsWhenElementNotFound() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element notOnStack = new Element(Tag.valueOf("p"), "http://example.com");
        Element replacement = new Element(Tag.valueOf("span"), "http://example.com");

        tb.push(html);
        tb.replaceOnStack(notOnStack, replacement);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInSelectScopeThrowsWhenBottomReached() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element optgroup = new Element(Tag.valueOf("optgroup"), "http://example.com");

        // Both are in TagSearchSelectScope ("optgroup", "option"), so iteration never returns false and hits Validate.fail
        tb.push(html);
        tb.push(optgroup);
        tb.inSelectScope("nonexistent");
    }

    @Test(timeout = 4000)
    public void testErrorRecordingRespectsErrorMaxLimit() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        ParseErrorList errorList = new ParseErrorList(1, 1);
        tb.initialiseParse(new StringReader(""), "http://example.com", errorList, ParseSettings.htmlDefault);

        Token.Character token = new Token.Character();
        token.data("a");
        tb.process(token, HtmlTreeBuilderState.Initial);
        assertEquals(1, errorList.size());

        tb.error(HtmlTreeBuilderState.Initial);
        // Should not add beyond max limit of 1
        assertEquals(1, errorList.size());
    }

    // =========================================================================
    // PARTITION E: Fragment Parsing & Complex Integration
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseFragmentWithNullContext() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        List<Node> nodes = tb.parseFragment("<div><p>Hello</p></div>", null, "http://example.com",
                ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithSpecialContextTags() {
        String[] contextTags = new String[]{
            "title", "textarea", "iframe", "style", "script", "noscript", "plaintext", "div"
        };
        for (String tag : contextTags) {
            HtmlTreeBuilder tb = new HtmlTreeBuilder();
            Element context = new Element(Tag.valueOf(tag), "http://example.com");
            List<Node> nodes = tb.parseFragment("Content", context, "http://example.com",
                    ParseErrorList.noTracking(), ParseSettings.htmlDefault);
            assertNotNull("Failed for tag: " + tag, nodes);
            assertTrue(tb.isFragmentParsing());
        }
    }

    @Test(timeout = 4000)
    public void testParseFragmentInheritsAncestorFormElement() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        form.appendChild(div);

        List<Node> nodes = tb.parseFragment("<input name='q' />", div, "http://example.com",
                ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        assertNotNull(nodes);
        assertSame(form, tb.getFormElement());
    }

    @Test(timeout = 4000)
    public void testInsertFormAndListingAssociation() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        tb.push(html);
        tb.push(body);

        Token.StartTag formStart = new Token.StartTag();
        formStart.nameAttr("form", new Attributes());
        FormElement form = tb.insertForm(formStart, true);

        assertSame(form, tb.getFormElement());
        assertTrue(tb.onStack(form));

        Token.StartTag inputStart = new Token.StartTag();
        inputStart.nameAttr("input", new Attributes());
        Element input = tb.insertEmpty(inputStart);

        assertTrue(form.elements().contains(input));
    }

    @Test(timeout = 4000)
    public void testInsertCommentAndToStringContract() {
        HtmlTreeBuilder tb = createInitializedTreeBuilder("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        tb.push(html);

        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("Test Comment");
        tb.insert(commentToken);

        assertEquals(1, html.childNodeSize());
        assertTrue(html.childNode(0) instanceof Comment);
        assertEquals("Test Comment", ((Comment) html.childNode(0)).getData());

        String str = tb.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("TreeBuilder{"));
    }
}