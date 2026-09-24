package org.jsoup.parser;

import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.HtmlTreeBuilder
 *
 * 1. Core State & Parsing Transitions:
 *    - parse(): Normal HTML document parse, baseUri propagation, Document return.
 *    - parseFragment(): Context null vs context non-null; quirksMode propagation from context's ownerDocument;
 *      tokeniser transitions for context tags ("title", "textarea" -> Rcdata; "iframe", "noembed", "noframes",
 *      "style", "xmp" -> Rawtext; "script" -> ScriptData; "noscript", "plaintext", default -> Data);
 *      FormElement ancestor detection from context parent chain.
 *    - resetInsertionMode(): Decisions on stack elements ("select" -> InSelect, "td" -> InCell, "tr" -> InRow,
 *      "tbody"/"thead"/"tfoot" -> InTableBody, "caption" -> InCaption, "colgroup" -> InColumnGroup,
 *      "table" -> InTable, "head"/"body" -> InBody, "frameset" -> InFrameset, "html" -> BeforeHead,
 *      fallback last node -> InBody).
 *
 * 2. Stack Operations & Boundary Conditions:
 *    - pop(): Normal pop; guard assertion against popping "html"; guard assertion against popping "td" outside "InCell".
 *    - onStack(), getFromStack(), removeFromStack(): Existing and missing element queries.
 *    - popStackToClose(String) & popStackToClose(String...): Target element removal and intermediate element popping.
 *    - popStackToBefore(String): Target preserved while descendants popped.
 *    - clearStackToContext() variants: Table, TableBody, TableRow contexts stopping at boundaries or "html".
 *    - aboveOnStack(), insertOnStackAfter(), replaceOnStack(): Index validations and queue modifications.
 *
 * 3. Scope Checks:
 *    - inScope(String), inScope(String, String[]), inListItemScope(), inButtonScope(), inTableScope(), inSelectScope().
 *    - Boundary failures triggering "Should not be reachable" assertion errors when target and delimiters are absent.
 *
 * 4. Active Formatting Elements:
 *    - pushActiveFormattingElements(): Deduplication of 3 identical formatting elements (same tag & attributes)
 *      across scope markers.
 *    - reconstructFormattingElements(): Empty list, marker at top, top on stack, rewind past un-stacked elements,
 *      element re-insertion, stack sync.
 *    - clearFormattingElementsToLastMarker(), removeFromActiveFormattingElements(), getActiveFormattingElement(),
 *      replaceActiveFormattingElement(), insertMarkerToFormattingElements().
 *
 * 5. Foster Parenting & Node Insertion:
 *    - insertInFosterParent(): Last table with parent -> insert before; last table without parent -> append to above;
 *      no table -> append to stack.get(0).
 *    - insert(Token.Character): In "script"/"style" -> DataNode; normal -> TextNode.
 *    - insertNode(): Empty stack -> doc append; foster inserts true; form listed element association with FormElement.
 *    - maybeSetBaseUri(): First valid base href sets base; second call ignored; empty href ignored.
 *    - generateImpliedEndTags(): Popping dd, dt, li, option, optgroup, p, rp, rt; with/without excludeTag.
 *    - isSpecial(): Known special elements return true, custom/formatting return false.
 *
 * 6. Defect-Targeted Zone (Defects4J ground truth: handlesKnownEmptyBlocks):
 *    - Self-closing known block tags (e.g., <script src='/foo' />). In defective state, self-closing script
 *      remained un-popped or forced tokenizer into ScriptData state, causing subsequent DOM nodes to be eaten
 *      as script text or entity-escaped. Correct behavior expects script to close cleanly and subsequent
 *      elements (<div id=2>, etc.) to exist as top-level body nodes.
 */
public class HtmlTreeBuilderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseStandardDocument() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Document doc = tb.parse("<!DOCTYPE html><html><head><title>Test</title></head><body><p>Hello</p></body></html>",
                "http://example.com/", ParseErrorList.noTracking());

        assertNotNull(doc);
        assertEquals("http://example.com/", tb.getBaseUri());
        assertEquals(doc, tb.getDocument());
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.select("p").first().text());
        assertFalse(tb.isFragmentParsing());
    }

    @Test(timeout = 4000)
    public void testStateAndTransition() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<p>A</p>", "http://example.com/", ParseErrorList.noTracking());

        tb.transition(HtmlTreeBuilderState.InTable);
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());

        tb.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTable, tb.originalState());

        tb.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
        assertEquals(HtmlTreeBuilderState.InTable, tb.originalState());
    }

    @Test(timeout = 4000)
    public void testFramesetOkFlag() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        tb.framesetOk(true);
        assertTrue(tb.framesetOk());
        tb.framesetOk(false);
        assertFalse(tb.framesetOk());
    }

    @Test(timeout = 4000)
    public void testHeadAndFormElementAccessors() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        Element head = new Element(Tag.valueOf("head"), "http://example.com/");
        tb.setHeadElement(head);
        assertSame(head, tb.getHeadElement());

        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com/", new Attributes());
        tb.setFormElement(form);
        assertSame(form, tb.getFormElement());
    }

    @Test(timeout = 4000)
    public void testPendingTableCharacters() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<table></table>", "http://example.com/", ParseErrorList.noTracking());

        assertNotNull(tb.getPendingTableCharacters());
        assertTrue(tb.getPendingTableCharacters().isEmpty());

        List<Token.Character> list = new ArrayList<Token.Character>();
        tb.setPendingTableCharacters(list);
        assertSame(list, tb.getPendingTableCharacters());

        tb.newPendingTableCharacters();
        assertNotSame(list, tb.getPendingTableCharacters());
        assertTrue(tb.getPendingTableCharacters().isEmpty());
    }

    @Test(timeout = 4000)
    public void testToStringFormat() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<p>Text</p>", "http://example.com/", ParseErrorList.noTracking());

        String str = tb.