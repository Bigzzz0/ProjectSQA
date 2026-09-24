package org.jsoup.parser;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.HtmlTreeBuilder
 *
 * 1. Defect-Targeted Zone:
 *    - testTemplateInsideTable: HTML5 template inside table elements (e.g. <table><template><tr><td>foo</td></tr></template></table>).
 *      In defective versions, clearStackToTableContext() or clearStackToTableBodyContext() incorrectly pops the <template>
 *      element because it only stops at "table" and "html", causing template children to be fostered or misparented into tbody.
 *
 * 2. Partition A: Core Functional Logic & State Transitions:
 *    - defaultSettings(), initialiseParse(), state getters & setters (state, originalState, markInsertionMode).
 *    - framesetOk flags, baseUri, baseUriSetFromDoc logic via maybeSetBaseUri().
 *    - resetInsertionMode() exercising branches: select, td/th (last & not last), tr, tbody/thead/tfoot, caption,
 *      colgroup, table, head, body, frameset, html, and contextElement fallback.
 *    - Token processing delegates and transitions.
 *
 * 3. Partition B: Boundary Value Analysis (BVA) & Extremes:
 *    - parseFragment() with null context vs various context tag types ("title", "textarea", "iframe", "script",
 *      "noscript", "plaintext", "custom").
 *    - parseFragment() inheriting quirks mode from context ownerDocument.
 *    - parseFragment() finding ancestor FormElement and binding form controls.
 *    - Stack manipulation boundaries: pop(), push(), onStack(), getFromStack(), removeFromStack(),
 *      popStackToClose() single & varargs, popStackToBefore(), clearStackToContext() variants.
 *    - Scope checks: inScope(), inListItemScope(), inButtonScope(), inTableScope(), inSelectScope().
 *
 * 4. Partition C: Formatting Elements & Foster Parenting:
 *    - pushActiveFormattingElements() duplicate capping rule (max 3 identical elements, removes earlier).
 *    - reconstructFormattingElements() walking backwards across stack/null markers and forwards reconstructing tags.
 *    - clearFormattingElementsToLastMarker(), removeFromActiveFormattingElements(), replaceActiveFormattingElement().
 *    - insertInFosterParent() branches: table with parent (inserts before table), table without parent (above on stack),
 *      and no table on stack (stack root).
 *
 * 5. Partition D: Defensive Guards & Tag Characteristics:
 *    - isSpecial() for special vs standard tags.
 *    - generateImpliedEndTags() with and without excludeTag matching candidates (dd, dt, li, p, etc.).
 *    - insertStartTag(), insertEmpty() self-closing known vs unknown, insertForm().
 *    - Character insertion inside script/style (DataNode) vs general tags (TextNode).
 */
public class HtmlTreeBuilderGeminiTest {

    // =========================================================================
    // Partition C (Defect-Targeted Zone)
    // =========================================================================

    /**
     * Defects4J Target: org.jsoup.parser.HtmlParserTest::testTemplateInsideTable
     * Ensures that when <template> is inside a <table>, clearStackToTableContext()
     * and table insertion modes properly retain the template element on stack,
     * allowing <tr>/<td> to be nested inside <template> rather than fostered or moved outside.
     */
    @Test(timeout = 4000)
    public void testTemplateInsideTable() {
        String html = "<table><template><tr><td>foo</td></tr></template></table>";
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput(html, "");

        Element template = doc.select("template").first();
        assertNotNull("Template element must be parsed inside document", template);
        assertEquals("<tr><td>foo</td></tr>", template.html());
        assertEquals(0, doc.select("table > tr").size());
        assertEquals(0, doc.select("table > tbody").size());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialiseParseAndDefaultSettings() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        ParseSettings settings = tb.defaultSettings();
        assertNotNull(settings);
        assertTrue(settings.preserveTagCase() == ParseSettings.htmlDefault.preserveTagCase());

        ParseErrorList errors = ParseErrorList.tracking(10);
        tb.initialiseParse(new StringReader("<div></div>"), "http://example.com", errors, settings);

        assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        assertNull(tb.originalState());
        assertEquals("http://example.com", tb.getBaseUri());
        assertNotNull(tb.getDocument());
        assertTrue(tb.framesetOk());
        assertFalse(tb.isFosterInserts());
        assertFalse(tb.isFragmentParsing());
        assertNull(tb.getHeadElement());
        assertNull(tb.getFormElement());
    }

    @Test(timeout = 4000)
    public void testStateTransitionsAndMark() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        tb.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());

        tb.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, tb.originalState());

        tb.transition(HtmlTreeBuilderState.AfterBody);
        assertEquals(HtmlTreeBuilderState.AfterBody, tb.state());
        assertEquals(HtmlTreeBuilderState.InBody, tb.originalState());

        tb.framesetOk(false);
        assertFalse(tb.framesetOk());
        tb.framesetOk(true);
        assertTrue(tb.framesetOk());
    }

    @Test(timeout = 4000)
    public void testMaybeSetBaseUri() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "http://initial.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        // 1. Element with no href
        Element baseNoHref = new Element(Tag.valueOf("base"), "http://initial.com/");
        tb.maybeSetBaseUri(baseNoHref);
        assertEquals("http://initial.com/", tb.getBaseUri());

        // 2. Element with valid href
        Attributes attrs = new Attributes();
        attrs.put("href", "http://updated.com/path/");
        Element baseWithHref = new Element(Tag.valueOf("base"), "http://initial.com/", attrs);
        tb.maybeSetBaseUri(baseWithHref);
        assertEquals("http://updated.com/path/", tb.getBaseUri());
        assertEquals("http://updated.com/path/", tb.getDocument().baseUri());

        // 3. Second call should be ignored because baseUriSetFromDoc is true
        attrs.put("href", "http://third.com/");
        Element baseSecond = new Element(Tag.valueOf("base"), "http://updated.com/path/", attrs);
        tb.maybeSetBaseUri(baseSecond);
        assertEquals("http://updated.com/path/", tb.getBaseUri());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeBranches() {
        String[] tagNames = new String[]{
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

        for (int i = 0; i < tagNames.length; i++) {
            HtmlTreeBuilder tb = new HtmlTreeBuilder();
            tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
            Element el = new Element(Tag.valueOf(tagNames[i]), "");
            tb.getStack().add(el);
            tb.resetInsertionMode();
            assertEquals("Failed for tag: " + tagNames[i], expectedStates[i], tb.state());
        }

        // Test fallback when last is true and tag is generic (e.g., div)
        HtmlTreeBuilder tbFallback = new HtmlTreeBuilder();
        tbFallback.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element div = new Element(Tag.valueOf("div"), "");
        tbFallback.getStack().add(div);
        tbFallback.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, tbFallback.state());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Fragment Parsing
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseFragmentNullContext() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        List<Node> nodes = tb.parseFragment("<p>Fragment text</p>", null, "http://example.com",
                ParseErrorList.tracking(10), ParseSettings.htmlDefault);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
        // Null context attaches to Document and returns doc.childNodes()
        assertEquals(1, nodes.size());
        assertEquals("html", nodes.get(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testParseFragmentContextSpecialTags() {
        String[] contextTags = new String[]{
                "title", "textarea", "iframe", "noembed", "noframes", "style", "xmp",
                "script", "noscript", "plaintext", "div"
        };

        for (String tagName : contextTags) {
            HtmlTreeBuilder tb = new HtmlTreeBuilder();
            Element context = new Element(Tag.valueOf(tagName), "http://example.com");
            List<Node> nodes = tb.parseFragment("Hello <b>world</b>", context, "http://example.com",
                    ParseErrorList.noTracking(), ParseSettings.htmlDefault);
            assertNotNull("Nodes should not be null for context: " + tagName, nodes);
            assertTrue(tb.isFragmentParsing());
        }
    }

    @Test(timeout = 4000)
    public void testParseFragmentInheritsQuirksModeAndForm() {
        Document ownerDoc = new Document("http://example.com");
        ownerDoc.quirksMode(Document.OutputSettings.Syntax.html.equals(Document.OutputSettings.Syntax.html)
                ? Document.QuirksMode.quirks : Document.QuirksMode.noQuirks);

        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element divInsideForm = new Element(Tag.valueOf("div"), "http://example.com");
        form.appendChild(divInsideForm);
        ownerDoc.appendChild(form);

        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        List<Node> nodes = tb.parseFragment("<input name='foo' value='bar'>", divInsideForm, "http://example.com",
                ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        assertEquals(Document.QuirksMode.quirks, tb.getDocument().quirksMode());
        assertSame(form, tb.getFormElement());
        assertFalse(nodes.isEmpty());
    }

    // =========================================================================
    // Partition C: Stack Operations & Scopes
    // =========================================================================

    @Test(timeout = 4000)
    public void testStackPushPopAndQueries() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        tb.push(html);
        tb.push(body);
        tb.push(p);

        assertEquals(3, tb.getStack().size());
        assertTrue(tb.onStack(body));
        assertSame(p, tb.getFromStack("p"));
        assertNull(tb.getFromStack("span"));

        assertSame(body, tb.aboveOnStack(p));
        assertSame(html, tb.aboveOnStack(body));

        assertSame(p, tb.pop());
        assertFalse(tb.onStack(p));
        assertEquals(2, tb.getStack().size());

        Element span = new Element(Tag.valueOf("span"), "");
        tb.insertOnStackAfter(html, span);
        assertSame(span, tb.getStack().get(1));

        Element div = new Element(Tag.valueOf("div"), "");
        tb.replaceOnStack(span, div);
        assertSame(div, tb.getStack().get(1));
        assertFalse(tb.onStack(span));

        assertTrue(tb.removeFromStack(div));
        assertFalse(tb.removeFromStack(span));
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseAndBefore() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        tb.push(html);
        tb.push(body);
        tb.push(div);
        tb.push(span);

        tb.popStackToClose("div");
        assertFalse(tb.onStack(span));
        assertFalse(tb.onStack(div));
        assertTrue(tb.onStack(body));

        tb.push(div);
        tb.push(span);
        tb.popStackToClose("div", "body");
        assertFalse(tb.onStack(span));
        assertFalse(tb.onStack(div));

        tb.push(div);
        tb.push(span);
        tb.popStackToBefore("div");
        assertFalse(tb.onStack(span));
        assertTrue(tb.onStack(div));
    }

    @Test(timeout = 4000)
    public void testClearStackToContexts() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tbody = new Element(Tag.valueOf("tbody"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");

        tb.push(html);
        tb.push(body);
        tb.push(table);
        tb.push(tbody);
        tb.push(tr);
        tb.push(td);

        tb.clearStackToTableRowContext();
        assertSame(tr, tb.currentElement());

        tb.push(td);
        tb.clearStackToTableBodyContext();
        assertSame(tbody, tb.currentElement());

        tb.push(tr);
        tb.clearStackToTableContext();
        assertSame(table, tb.currentElement());
    }

    @Test(timeout = 4000)
    public void testScopeMethods() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        tb.push(html);
        tb.push(body);
        tb.push(p);

        assertTrue(tb.inScope("p"));
        assertFalse(tb.inScope("span"));
        assertTrue(tb.inScope(new String[]{"p", "span"}));

        assertTrue(tb.inListItemScope("p"));
        assertTrue(tb.inButtonScope("p"));
        assertTrue(tb.inTableScope("body"));

        Element select = new Element(Tag.valueOf("select"), "");
        Element option = new Element(Tag.valueOf("option"), "");
        tb.push(select);
        tb.push(option);

        assertTrue(tb.inSelectScope("option"));
        assertFalse(tb.inSelectScope("p"));
    }

    // =========================================================================
    // Partition D: Active Formatting Elements & Foster Parenting
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormattingElementsDuplicateCap() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Attributes attrs = new Attributes();
        attrs.put("class", "c1");

        Element b1 = new Element(Tag.valueOf("b"), "", attrs.clone());
        Element b2 = new Element(Tag.valueOf("b"), "", attrs.clone());
        Element b3 = new Element(Tag.valueOf("b"), "", attrs.clone());
        Element b4 = new Element(Tag.valueOf("b"), "", attrs.clone());

        tb.pushActiveFormattingElements(b1);
        tb.pushActiveFormattingElements(b2);
        tb.pushActiveFormattingElements(b3);
        assertTrue(tb.isInActiveFormattingElements(b1));

        // Pushing 4th duplicate should remove the earliest seen (b1)
        tb.pushActiveFormattingElements(b4);
        assertFalse(tb.isInActiveFormattingElements(b1));
        assertTrue(tb.isInActiveFormattingElements(b4));
    }

    @Test(timeout = 4000)
    public void testFormattingElementsMarkersAndReconstruct() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        Element i = new Element(Tag.valueOf("i"), "");
        tb.pushActiveFormattingElements(i);

        tb.insertMarkerToFormattingElements();
        assertNull(tb.lastFormattingElement());

        Element span = new Element(Tag.valueOf("span"), "");
        tb.pushActiveFormattingElements(span);
        assertSame(span, tb.lastFormattingElement());
        assertSame(span, tb.getActiveFormattingElement("span"));

        tb.clearFormattingElementsToLastMarker();
        assertSame(i, tb.lastFormattingElement());

        Element replaced = new Element(Tag.valueOf("em"), "");
        tb.replaceActiveFormattingElement(i, replaced);
        assertSame(replaced, tb.lastFormattingElement());

        tb.removeFromActiveFormattingElements(replaced);
        assertNull(tb.lastFormattingElement());
        assertNull(tb.removeLastFormattingElement());
    }

    @Test(timeout = 4000)
    public void testReconstructFormattingElements() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        Element b = new Element(Tag.valueOf("b"), "");
        tb.pushActiveFormattingElements(b);
        assertFalse(tb.onStack(b));

        tb.reconstructFormattingElements();
        assertEquals("b", tb.currentElement().nodeName());
        assertTrue(tb.onStack(tb.currentElement()));

        // Second call when already on stack should be a no-op
        int stackSize = tb.getStack().size();
        tb.reconstructFormattingElements();
        assertEquals(stackSize, tb.getStack().size());
    }

    @Test(timeout = 4000)
    public void testInsertInFosterParentBranches() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        // Branch 1: No table on stack -> fosters into stack.get(0)
        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);
        TextNode text1 = new TextNode("text1");
        tb.insertInFosterParent(text1);
        assertEquals(1, html.childNodeSize());
        assertSame(text1, html.childNode(0));

        // Branch 2: Table has parent -> inserts before table
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        body.appendChild(table);
        tb.push(body);
        tb.push(table);

        TextNode text2 = new TextNode("text2");
        tb.insertInFosterParent(text2);
        // text2 should be inserted before table inside body
        assertEquals(0, text2.siblingIndex());
        assertEquals(1, table.siblingIndex());

        // Branch 3: Table is on stack but has null parent -> aboveOnStack.appendChild()
        Element tableNoParent = new Element(Tag.valueOf("table"), "");
        tb.getStack().clear();
        tb.push(html);
        tb.push(body);
        tb.push(tableNoParent);
        assertNull(tableNoParent.parent());

        TextNode text3 = new TextNode("text3");
        tb.insertInFosterParent(text3);
        assertSame(body, text3.parent());
    }

    // =========================================================================
    // Partition E: DOM Node Insertions & Token Processing
    // =========================================================================

    @Test(timeout = 4000)
    public void testInsertStartTagAndSelfClosing() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.tracking(10), ParseSettings.htmlDefault);

        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);

        Token.StartTag knownVoid = new Token.StartTag();
        knownVoid.nameAttr("img", new Attributes());
        knownVoid.selfClosing = true;
        Element img = tb.insertEmpty(knownVoid);
        assertEquals("img", img.tagName());
        assertFalse(tb.onStack(img));

        Token.StartTag knownNonVoid = new Token.StartTag();
        knownNonVoid.nameAttr("div", new Attributes());
        knownNonVoid.selfClosing = true;
        Element div = tb.insertEmpty(knownNonVoid);
        assertEquals("div", div.tagName());

        Token.StartTag unknownTag = new Token.StartTag();
        unknownTag.nameAttr("custom-element", new Attributes());
        unknownTag.selfClosing = true;
        Element custom = tb.insertEmpty(unknownTag);
        assertTrue(custom.tag().isSelfClosing());

        Token.StartTag regularStart = new Token.StartTag();
        regularStart.nameAttr("span", new Attributes());
        Element span = tb.insert(regularStart);
        assertSame(span, tb.currentElement());
        assertTrue(tb.onStack(span));

        Element h1 = tb.insertStartTag("h1");
        assertSame(h1, tb.currentElement());
    }

    @Test(timeout = 4000)
    public void testInsertCharacterNodes() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        Token.Character charToken = new Token.Character();
        charToken.data("Hello normal text");
        tb.insert(charToken);
        assertTrue(body.childNode(0) instanceof TextNode);

        Element script = new Element(Tag.valueOf("script"), "");
        tb.push(script);
        Token.Character scriptToken = new Token.Character();
        scriptToken.data("var x = 1;");
        tb.insert(scriptToken);
        assertTrue(script.childNode(0) instanceof DataNode);

        Element style = new Element(Tag.valueOf("style"), "");
        tb.push(style);
        Token.Character styleToken = new Token.Character();
        styleToken.data("body { color: red; }");
        tb.insert(styleToken);
        assertTrue(style.childNode(0) instanceof DataNode);
    }

    @Test(timeout = 4000)
    public void testInsertCommentAndForm() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        // Stack size 0 -> comment directly to doc
        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("Doc comment");
        tb.insert(commentToken);
        assertTrue(tb.getDocument().childNode(0) instanceof Comment);

        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);

        Token.StartTag formTag = new Token.StartTag();
        formTag.nameAttr("form", new Attributes());
        FormElement form1 = tb.insertForm(formTag, true);
        assertSame(form1, tb.getFormElement());
        assertTrue(tb.onStack(form1));

        FormElement form2 = tb.insertForm(formTag, false);
        assertSame(form2, tb.getFormElement());
        assertFalse(tb.onStack(form2));

        // Insert form-listed element to associate with form
        Element input = new Element(Tag.valueOf("input"), "");
        tb.insert(input);
        assertEquals(1, form2.elements().size());
        assertSame(input, form2.elements().get(0));
    }

    @Test(timeout = 4000)
    public void testGenerateImpliedEndTags() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element li = new Element(Tag.valueOf("li"), "");

        tb.push(html);
        tb.push(body);
        tb.push(p);
        tb.push(li);

        tb.generateImpliedEndTags("p");
        // li should be popped, p was excluded so remains on stack
        assertFalse(tb.onStack(li));
        assertTrue(tb.onStack(p));

        tb.generateImpliedEndTags();
        assertFalse(tb.onStack(p));
        assertSame(body, tb.currentElement());
    }

    @Test(timeout = 4000)
    public void testMiscellaneousStateAndToString() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader("<div></div>"), "", ParseErrorList.tracking(5), ParseSettings.htmlDefault);

        Element head = new Element(Tag.valueOf("head"), "");
        tb.setHeadElement(head);
        assertSame(head, tb.getHeadElement());

        tb.setFosterInserts(true);
        assertTrue(tb.isFosterInserts());

        List<String> pending = new ArrayList<>();
        pending.add("foo");
        tb.setPendingTableCharacters(pending);
        assertEquals(1, tb.getPendingTableCharacters().size());
        tb.newPendingTableCharacters();
        assertEquals(0, tb.getPendingTableCharacters().size());

        assertTrue(tb.isSpecial(new Element(Tag.valueOf("table"), "")));
        assertFalse(tb.isSpecial(new Element(Tag.valueOf("custom-foo"), "")));

        tb.error(HtmlTreeBuilderState.InBody);
        tb.transition(HtmlTreeBuilderState.InBody);

        Token.Comment comment = new Token.Comment();
        comment.getData().append("test");
        assertTrue(tb.process(comment));
        assertTrue(tb.process(comment, HtmlTreeBuilderState.InBody));

        String str = tb.toString();
        assertNotNull(str);
        assertTrue(str.contains("TreeBuilder"));
    }
}