/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.jsoup.parser.HtmlTreeBuilder
 * Primary Focus: State transitions, stack manipulation, scope checking, formatting element reconstruction,
 *                foster parenting, fragment parsing modes, and case preservation defect handling.
 *
 * 1. DEFECT-TARGETED ZONE:
 *    - Defects4J / Jsoup preservedCaseLinksCantNest:
 *      Ensures start/end tag handling under ParseSettings.preserveCase properly associates and closes
 *      active formatting elements regardless of case (e.g., <A> ONE <A> Two </A> -> <A> ONE </A> <A> Two </A>).
 *
 * 2. FRAGMENT PARSING & CONTEXT MATRICES:
 *    - Context types triggering TokeniserState transitions: title, textarea (Rcdata); iframe, noembed,
 *      noframes, style, xmp (Rawtext); script (ScriptData); noscript, plaintext, and defaults (Data).
 *    - Form element association across ancestor chains in fragment parsing.
 *    - Context with quirks mode inheritance.
 *    - Context = null handling (falling back to document root parsing).
 *
 * 3. STACK & INSERTION MODE MATRICES:
 *    - resetInsertionMode across all branches: select, td/th (last vs non-last), tr, tbody/thead/tfoot,
 *      caption, colgroup, table, head, body, frameset, html, and fallback.
 *    - clearStackToTableContext, clearStackToTableBodyContext, clearStackToTableRowContext.
 *    - popStackToClose(String), popStackToClose(String...), popStackToBefore(String).
 *    - aboveOnStack assertion and boundary verification.
 *    - insertOnStackAfter and replaceOnStack validation.
 *
 * 4. SCOPE SEARCHES:
 *    - inScope, inListItemScope, inButtonScope, inTableScope, inSelectScope (including MaxScopeSearchDepth boundary).
 *
 * 5. ACTIVE FORMATTING ELEMENTS:
 *    - Noah's Ark limit: pushActiveFormattingElements drops the earliest when 3 identical elements appear.
 *    - reconstructFormattingElements: traversal with markers (null), elements already on stack,
 *      entry at pos 0, and non-zero pos skip branches.
 *    - clearFormattingElementsToLastMarker, removeFromActiveFormattingElements.
 *
 * 6. FOSTER PARENTING & NODE INSERTION:
 *    - insertNode with stack == 0 (doctype, comment to doc).
 *    - insertInFosterParent: table with parent (insert before), table without parent (above on stack),
 *      and fragment fallback (no table on stack -> stack.get(0)).
 *    - Character node insertion: CData, script/style DataNode, and regular TextNode.
 *    - Self-closing tag handling (known void tag, unknown self-closing tag, non-void error).
 * ====================================================================================================
 */

package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderGeminiTest {

    // ====================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Preserved Case Links & Formatting Scope)
    // ====================================================================================================

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestDefect() {
        Parser parser = Parser.htmlParser().settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput("<A> ONE <A> Two </A>", "");
        assertEquals("<A> ONE </A> <A> Two </A>", StringUtil.normaliseWhitespace(doc.body().html()));
    }

    @Test(timeout = 4000)
    public void testPreservedCaseNestedFormatting() {
        Parser parser = Parser.htmlParser().settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput("<B>Bold <I>Bold-Italic</B> Italic</I>", "");
        assertEquals("<B>Bold <I>Bold-Italic</I></B><I> Italic</I>", StringUtil.normaliseWhitespace(doc.body().html()));
    }

    // ====================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ====================================================================================================

    @Test(timeout = 4000)
    public void testInitialiseParseAndStateGetters() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader("<div></div>"), "http://example.com/", parser);

        assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        assertNull(tb.originalState());
        assertTrue(tb.framesetOk());
        assertFalse(tb.isFosterInserts());
        assertFalse(tb.isFragmentParsing());
        assertNotNull(tb.getDocument());
        assertEquals("http://example.com/", tb.getBaseUri());

        tb.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.Initial, tb.originalState());

        tb.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());

        tb.framesetOk(false);
        assertFalse(tb.framesetOk());

        tb.setFosterInserts(true);
        assertTrue(tb.isFosterInserts());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeAllBranches() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        // Branch 1: select
        tb.getStack().clear();
        Element select = new Element(Tag.valueOf("select"), "");
        tb.push(select);
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InSelect, tb.state());

        // Branch 2: td / th (not last, i.e. stack.size() > 1)
        tb.getStack().clear();
        tb.push(new Element(Tag.valueOf("html"), ""));
        Element td = new Element(Tag.valueOf("td"), "");
        tb.push(td);
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, tb.state());

        tb.getStack().clear();
        tb.push(new Element(Tag.valueOf("html"), ""));
        Element th = new Element(Tag.valueOf("th"), "");
        tb.push(th);
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, tb.state());

        // Branch 3: tr
        tb.getStack().clear();
        tb.push(new Element(Tag.valueOf("tr"), ""));
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InRow, tb.state());

        // Branch 4: tbody / thead / tfoot
        for (String tag : new String[]{"tbody", "thead", "tfoot"}) {
            tb.getStack().clear();
            tb.push(new Element(Tag.valueOf(tag), ""));
            tb.resetInsertionMode();
            assertEquals(HtmlTreeBuilderState.InTableBody, tb.state());
        }

        // Branch 5: caption
        tb.getStack().clear();
        tb.push(new Element(Tag.valueOf("caption"), ""));
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCaption, tb.state());

        // Branch 6: colgroup
        tb.getStack().clear();
        tb.push(new Element(Tag.valueOf("colgroup"), ""));
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InColumnGroup, tb.state());

        // Branch 7: table
        tb.getStack().clear();
        tb.push(new Element(Tag.valueOf("table"), ""));
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());

        // Branch 8: head
        tb.getStack().clear();
        tb.push(new Element(Tag.valueOf("head"), ""));
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());

        // Branch 9: body
        tb.getStack().clear();
        tb.push(new Element(Tag.valueOf("body"), ""));
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());

        // Branch 10: frameset
        tb.getStack().clear();
        tb.push(new Element(Tag.valueOf("frameset"), ""));
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InFrameset, tb.state());

        // Branch 11: html
        tb.getStack().clear();
        tb.push(new Element(Tag.valueOf("html"), ""));
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.BeforeHead, tb.state());

        // Branch 12: last element fallback (not matching above)
        tb.getStack().clear();
        tb.push(new Element(Tag.valueOf("custom"), ""));
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
    }

    @Test(timeout = 4000)
    public void testClearStackToContextMethods() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tbody = new Element(Tag.valueOf("tbody"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element div = new Element(Tag.valueOf("div"), "");

        // Test clearStackToTableRowContext
        tb.getStack().clear();
        tb.push(html);
        tb.push(table);
        tb.push(tbody);
        tb.push(tr);
        tb.push(td);
        tb.push(div);
        tb.clearStackToTableRowContext();
        assertSame(tr, tb.currentElement());

        // Test clearStackToTableBodyContext
        tb.push(td);
        tb.push(div);
        tb.clearStackToTableBodyContext();
        assertSame(tbody, tb.currentElement());

        // Test clearStackToTableContext
        tb.push(tr);
        tb.push(td);
        tb.clearStackToTableContext();
        assertSame(table, tb.currentElement());

        // Test clearStack stops at html if context tag is not present
        tb.getStack().clear();
        tb.push(html);
        tb.push(div);
        tb.clearStackToTableContext();
        assertSame(html, tb.currentElement());
    }

    @Test(timeout = 4000)
    public void testStackOperations() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        Element el1 = new Element(Tag.valueOf("p"), "");
        Element el2 = new Element(Tag.valueOf("div"), "");
        Element el3 = new Element(Tag.valueOf("span"), "");

        tb.push(el1);
        tb.push(el2);
        assertTrue(tb.onStack(el1));
        assertTrue(tb.onStack(el2));
        assertFalse(tb.onStack(el3));

        assertSame(el2, tb.aboveOnStack(el3) == null ? el2 : null);
        assertSame(el1, tb.aboveOnStack(el2));

        assertSame(el2, tb.getFromStack("div"));
        assertSame(el1, tb.getFromStack("p"));
        assertNull(tb.getFromStack("span"));

        tb.insertOnStackAfter(el1, el3);
        assertEquals(3, tb.getStack().size());
        assertSame(el3, tb.getStack().get(1));

        Element el4 = new Element(Tag.valueOf("section"), "");
        tb.replaceOnStack(el3, el4);
        assertFalse(tb.onStack(el3));
        assertSame(el4, tb.getStack().get(1));

        assertTrue(tb.removeFromStack(el4));
        assertFalse(tb.removeFromStack(el3));

        assertSame(el2, tb.pop());
        assertSame(el1, tb.pop());
        assertEquals(0, tb.getStack().size());
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseAndBefore() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        Element a = new Element(Tag.valueOf("div"), "");
        Element b = new Element(Tag.valueOf("p"), "");
        Element c = new Element(Tag.valueOf("span"), "");

        tb.push(a);
        tb.push(b);
        tb.push(c);

        tb.popStackToBefore("p");
        assertSame(b, tb.currentElement());
        assertEquals(2, tb.getStack().size());

        tb.push(c);
        tb.popStackToClose("p");
        assertSame(a, tb.currentElement());
        assertEquals(1, tb.getStack().size());

        tb.push(b);
        tb.push(c);
        // popStackToClose(String... sorted)
        tb.popStackToClose("div", "p");
        assertSame(a, tb.currentElement());
    }

    // ====================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Scopes
    // ====================================================================================================

    @Test(timeout = 4000)
    public void testScopeMethods() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element li = new Element(Tag.valueOf("li"), "");
        Element button = new Element(Tag.valueOf("button"), "");
        Element select = new Element(Tag.valueOf("select"), "");
        Element opt = new Element(Tag.valueOf("option"), "");

        tb.push(html);
        tb.push(table);
        tb.push(td);
        tb.push(p);

        assertTrue(tb.inScope("p"));
        assertTrue(tb.inScope(new String[]{"p", "span"}));
        assertTrue(tb.inScope("td"));
        assertFalse(tb.inScope("table")); // td is in TagsSearchInScope, so search stops before table

        assertTrue(tb.inTableScope("table"));
        assertFalse(tb.inTableScope("div"));

        tb.push(li);
        assertTrue(tb.inListItemScope("li"));

        tb.push(button);
        assertTrue(tb.inButtonScope("button"));

        // Select scope test
        tb.getStack().clear();
        tb.push(select);
        tb.push(opt);
        assertTrue(tb.inSelectScope("option"));
        assertFalse(tb.inSelectScope("div"));
    }

    @Test(timeout = 4000)
    public void testMaxScopeSearchDepthBoundary() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        Element target = new Element(Tag.valueOf("div"), "");
        tb.push(target);

        for (int i = 0; i < HtmlTreeBuilder.MaxScopeSearchDepth + 5; i++) {
            tb.push(new Element(Tag.valueOf("span"), ""));
        }

        // Beyond MaxScopeSearchDepth of 100, target shouldn't be found in scope
        assertFalse(tb.inScope("div"));
    }

    @Test(timeout = 4000)
    public void testActiveFormattingElementsAndNoahsArkLimit() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        Element b1 = new Element(Tag.valueOf("b"), "");
        b1.attr("class", "bold");
        Element b2 = new Element(Tag.valueOf("b"), "");
        b2.attr("class", "bold");
        Element b3 = new Element(Tag.valueOf("b"), "");
        b3.attr("class", "bold");
        Element b4 = new Element(Tag.valueOf("b"), "");
        b4.attr("class", "bold");

        tb.pushActiveFormattingElements(b1);
        tb.pushActiveFormattingElements(b2);
        tb.pushActiveFormattingElements(b3);
        assertTrue(tb.isInActiveFormattingElements(b1));

        // 4th identical element triggers removal of b1 (Noah's Ark rule of 3)
        tb.pushActiveFormattingElements(b4);
        assertFalse(tb.isInActiveFormattingElements(b1));
        assertTrue(tb.isInActiveFormattingElements(b2));
        assertTrue(tb.isInActiveFormattingElements(b3));
        assertTrue(tb.isInActiveFormattingElements(b4));

        tb.insertMarkerToFormattingElements();
        assertNull(tb.lastFormattingElement());

        tb.pushActiveFormattingElements(new Element(Tag.valueOf("i"), ""));
        assertNotNull(tb.getActiveFormattingElement("i"));
        assertNull(tb.getActiveFormattingElement("b")); // stopped at marker

        tb.clearFormattingElementsToLastMarker();
        assertNotNull(tb.lastFormattingElement()); // marker cleared, b4 is visible again
        assertSame(b4, tb.lastFormattingElement());

        Element replacement = new Element(Tag.valueOf("strong"), "");
        tb.replaceActiveFormattingElement(b4, replacement);
        assertFalse(tb.isInActiveFormattingElements(b4));
        assertTrue(tb.isInActiveFormattingElements(replacement));

        tb.removeFromActiveFormattingElements(replacement);
        assertFalse(tb.isInActiveFormattingElements(replacement));
    }

    @Test(timeout = 4000)
    public void testReconstructFormattingElements() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        Element b = new Element(Tag.valueOf("b"), "");
        tb.pushActiveFormattingElements(b);

        // b is in active formatting elements and NOT on stack
        assertFalse(tb.onStack(b));
        tb.reconstructFormattingElements();
        // After reconstruction, a new 'b' element should be pushed onto stack
        assertTrue(tb.onStack(tb.getFromStack("b")));
        assertEquals("b", tb.currentElement().nodeName());

        // Calling it again when already on stack does nothing
        tb.reconstructFormattingElements();
        assertEquals("b", tb.currentElement().nodeName());
    }

    // ====================================================================================================
    // Partition D: Fragment Parsing & Context Matrix
    // ====================================================================================================

    @Test(timeout = 4000)
    public void testParseFragmentWithDifferentContexts() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);

        // Null context
        List<Node> nodesNull = tb.parseFragment("<div>Hello</div>", null, "http://example.com/", parser);
        assertFalse(nodesNull.isEmpty());

        // Title context (Rcdata)
        Element titleCtx = new Element(Tag.valueOf("title"), "");
        List<Node> nodesTitle = tb.parseFragment("Some &amp; text <b>tag</b>", titleCtx, "", parser);
        assertEquals(1, nodesTitle.size());

        // Textarea context (Rcdata)
        Element textareaCtx = new Element(Tag.valueOf("textarea"), "");
        List<Node> nodesTextarea = tb.parseFragment("Inside textarea", textareaCtx, "", parser);
        assertEquals(1, nodesTextarea.size());

        // Style / Rawtext context
        Element styleCtx = new Element(Tag.valueOf("style"), "");
        List<Node> nodesStyle = tb.parseFragment("body { color: red; }", styleCtx, "", parser);
        assertEquals(1, nodesStyle.size());

        // Script context (ScriptData)
        Element scriptCtx = new Element(Tag.valueOf("script"), "");
        List<Node> nodesScript = tb.parseFragment("var x = 1;", scriptCtx, "", parser);
        assertEquals(1, nodesScript.size());

        // Plaintext context
        Element plainCtx = new Element(Tag.valueOf("plaintext"), "");
        List<Node> nodesPlain = tb.parseFragment("raw data", plainCtx, "", parser);
        assertEquals(1, nodesPlain.size());

        // Form context traversal
        Document doc = Jsoup.parse("<html><body><form id='f'><div id='inner'></div></form></body></html>");
        Element inner = doc.getElementById("inner");
        List<Node> nodesForm = tb.parseFragment("<input name='foo'>", inner, "", parser);
        assertNotNull(tb.getFormElement());
        assertEquals("f", tb.getFormElement().id());
    }

    // ====================================================================================================
    // Partition E: Foster Parenting & Specific Insertions
    // ====================================================================================================

    @Test(timeout = 4000)
    public void testInsertInFosterParent() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        tb.getDocument().appendChild(html);
        html.appendChild(body);
        body.appendChild(table);

        tb.push(html);
        tb.push(body);
        tb.push(table);

        tb.setFosterInserts(true);
        Element fostered = new Element(Tag.valueOf("p"), "");
        tb.insertInFosterParent(fostered);

        // Should be inserted before table in table's parent (body)
        assertSame(body, fostered.parent());
        assertEquals(0, body.children().indexOf(fostered));
        assertEquals(1, body.children().indexOf(table));

        // Foster insert when table has no parent (stack-based aboveOnStack)
        Element orphanTable = new Element(Tag.valueOf("table"), "");
        tb.push(orphanTable);
        Element fostered2 = new Element(Tag.valueOf("span"), "");
        tb.insertInFosterParent(fostered2);
        assertSame(table, fostered2.parent());

        // Foster insert when no table on stack (falls back to stack.get(0))
        tb.getStack().clear();
        tb.push(html);
        Element fostered3 = new Element(Tag.valueOf("div"), "");
        tb.insertInFosterParent(fostered3);
        assertSame(html, fostered3.parent());
    }

    @Test(timeout = 4000)
    public void testInsertNodeCharacterAndComment() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        // Normal text character
        Token.Character charToken = new Token.Character();
        charToken.data("Regular text");
        tb.insert(charToken);
        assertEquals(1, body.childNodes().size());
        assertTrue(body.childNode(0) instanceof TextNode);

        // CData character
        Token.Character cdataToken = new Token.CData("cdata content");
        tb.insert(cdataToken);
        assertEquals(2, body.childNodes().size());

        // Script character (DataNode)
        Element script = new Element(Tag.valueOf("script"), "");
        tb.push(script);
        Token.Character scriptToken = new Token.Character();
        scriptToken.data("alert(1);");
        tb.insert(scriptToken);
        assertEquals(1, script.childNodes().size());
        tb.pop();

        // Comment insertion
        Token.Comment commentToken = new Token.Comment();
        commentToken.data.append("a comment");
        tb.insert(commentToken);
        assertTrue(body.childNode(body.childNodes().size() - 1) instanceof Comment);
    }

    @Test(timeout = 4000)
    public void testInsertEmptyAndStartTag() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);

        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("img", new Attributes());
        startTag.selfClosing = true;

        Element img = tb.insertEmpty(startTag);
        assertEquals("img", img.tagName());
        assertTrue(tb.onStack(html));

        // Self-closing unknown tag
        Token.StartTag customTag = new Token.StartTag();
        customTag.nameAttr("custom-element", new Attributes());
        customTag.selfClosing = true;

        Element custom = tb.insert(customTag);
        assertEquals("custom-element", custom.tagName());
    }

    @Test(timeout = 4000)
    public void testFormElementTrackingAndInsertForm() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        Token.StartTag formStart = new Token.StartTag();
        formStart.nameAttr("form", new Attributes());
        FormElement form = tb.insertForm(formStart, true);

        assertSame(form, tb.getFormElement());
        assertTrue(tb.onStack(form));

        // Listed form controls added to formElement automatically
        Token.StartTag inputStart = new Token.StartTag();
        inputStart.nameAttr("input", new Attributes());
        Element input = tb.insertEmpty(inputStart);
        assertTrue(form.elements().contains(input));

        tb.setFormElement(null);
        assertNull(tb.getFormElement());
    }

    @Test(timeout = 4000)
    public void testMaybeSetBaseUri() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com/", parser);

        Element baseWithoutHref = new Element(Tag.valueOf("base"), "");
        tb.maybeSetBaseUri(baseWithoutHref);
        assertEquals("http://example.com/", tb.getBaseUri());

        Element baseWithHref = new Element(Tag.valueOf("base"), "");
        baseWithHref.attr("href", "http://example.com/sub/");
        tb.maybeSetBaseUri(baseWithHref);
        assertEquals("http://example.com/sub/", tb.getBaseUri());

        // Subsequent <base> should be ignored
        Element base2 = new Element(Tag.valueOf("base"), "");
        base2.attr("href", "http://example.com/other/");
        tb.maybeSetBaseUri(base2);
        assertEquals("http://example.com/sub/", tb.getBaseUri());
    }

    @Test(timeout = 4000)
    public void testGenerateImpliedEndTags() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element li = new Element(Tag.valueOf("li"), "");

        tb.push(html);
        tb.push(body);
        tb.push(p);
        tb.push(li);

        // Exclude 'li', so only 'p' could be popped, but 'li' is at top
        tb.generateImpliedEndTags("li");
        assertSame(li, tb.currentElement());

        // No exclusion: pops 'li', then pops 'p'
        tb.generateImpliedEndTags();
        assertSame(body, tb.currentElement());
    }

    @Test(timeout = 4000)
    public void testPendingTableCharactersAndSpecialElements() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        assertNotNull(tb.getPendingTableCharacters());
        tb.getPendingTableCharacters().add("test");
        assertEquals(1, tb.getPendingTableCharacters().size());
        tb.newPendingTableCharacters();
        assertEquals(0, tb.getPendingTableCharacters().size());

        Element div = new Element(Tag.valueOf("div"), "");
        Element custom = new Element(Tag.valueOf("custom-tag"), "");
        assertTrue(tb.isSpecial(div));
        assertFalse(tb.isSpecial(custom));

        Element head = new Element(Tag.valueOf("head"), "");
        tb.setHeadElement(head);
        assertSame(head, tb.getHeadElement());
    }

    @Test(timeout = 4000)
    public void testToStringAndDefaultSettings() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "", parser);

        assertNotNull(tb.defaultSettings());
        assertNotNull(tb.toString());
        assertTrue(tb.toString().contains("TreeBuilder{"));
    }

    @Test(timeout = 4000)
    public void testParseErrorsCollected() {
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput("<html><head></head><body><p>Test</invalid></body></html>", "");
        assertFalse(parser.getErrors().isEmpty());
    }
}