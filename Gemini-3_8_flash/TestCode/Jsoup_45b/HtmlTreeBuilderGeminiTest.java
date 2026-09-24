/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.HtmlTreeBuilder
 * Defects4J Defect: testReinsertionModeForThCelss (expected:<1> but was:<5>)
 *
 * Key Branches & Targeted Logic:
 * 1. resetInsertionMode():
 *    - Branch: "select", "td", "th" (defect zone: "td".equals(name) || "td".equals(name) && !last missing "th")
 *    - Branch: "tr", "tbody"/"thead"/"tfoot", "caption", "colgroup", "table", "head", "body", "frameset", "html"
 *    - ContextElement / Fragment parsing with contextElement as last item.
 * 2. parseFragment():
 *    - Null context vs non-null context (various context tags: title, textarea, iframe, noembed, script, noscript, plaintext)
 *    - OwnerDocument quirksMode inheritance
 *    - FormElement ancestor resolution in context chain
 * 3. Scope Checks (inScope, inListItemScope, inButtonScope, inTableScope, inSelectScope):
 *    - Target hit, BaseTypes stop, ExtraTypes stop, Unreachable fail guard.
 * 4. Active Formatting Elements & Foster Parenting:
 *    - pushActiveFormattingElements (limit of 3 identical elements, marker handling)
 *    - reconstructFormattingElements (null/onStack, traversal to pos 0 or marker, rewind & reinsert)
 *    - clearFormattingElementsToLastMarker
 *    - insertInFosterParent (table with parent, table without parent on stack, no table frag)
 * 5. Stack Operations & Context Clearing:
 *    - popStackToClose, popStackToBefore, clearStackToTableContext, clearStackToTableBodyContext, clearStackToTableRowContext
 *    - insertOnStackAfter, replaceOnStack, removeFromStack
 * 6. Base URI & Document setup:
 *    - maybeSetBaseUri (only once, ignore without href)
 */

package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Fault)
    // =========================================================================

    /**
     * Targets the defect in resetInsertionMode where "th" cell fails to reset to InCell mode
     * due to the typo: ("td".equals(name) || "td".equals(name) && !last) instead of checking "th".
     * When a select inside a <th> is closed, resetInsertionMode must transition to InCell.
     */
    @Test(timeout = 4000)
    public void testReinsertionModeForThCells_DefectTarget() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<input>", "http://example.com", ParseErrorList.noTracking());

        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element th = new Element(Tag.valueOf("th"), "");

        tb.getStack().add(html);
        tb.getStack().add(table);
        tb.getStack().add(tr);
        tb.getStack().add(th);

        tb.resetInsertionMode();
        assertEquals("Should transition to InCell when current container is <th>",
                HtmlTreeBuilderState.InCell, tb.state());
    }

    /**
     * Full HTML parse triggering resetInsertionMode on closing a select element inside a <th>.
     * In the defective version, errors accumulate or nodes get foster-parented inappropriately.
     */
    @Test(timeout = 4000)
    public void testReinsertionModeForThCelss_FullParseIntegration() {
        ParseErrorList errors = ParseErrorList.tracking(100);
        String html = "<table><tr><th><select><option>A</option></select><span>Test</span></th></tr></table>";
        Document doc = Jsoup.parse(html, "http://example.com", new Parser(new HtmlTreeBuilder().setTrackErrors(100)));

        assertNotNull(doc);
        Element th = doc.select("th").first();
        assertNotNull(th);
        assertEquals(2, th.children().size());
        assertEquals("select", th.child(0).tagName());
        assertEquals("span", th.child(1).tagName());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testResetInsertionMode_AllBranches() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>", "", ParseErrorList.noTracking());

        String[] tags = new String[]{
                "select", "tr", "tbody", "thead", "tfoot", "caption",
                "colgroup", "table", "head", "body", "frameset", "html"
        };
        HtmlTreeBuilderState[] expectedStates = new HtmlTreeBuilderState[]{
                HtmlTreeBuilderState.InSelect,
                HtmlTreeBuilderState.InRow,
                HtmlTreeBuilderState.InTableBody,
                HtmlTreeBuilderState.InTableBody,
                HtmlTreeBuilderState.InTableBody,
                HtmlTreeBuilderState.InCaption,
                HtmlTreeBuilderState.InColumnGroup,
                HtmlTreeBuilderState.InTable,
                HtmlTreeBuilderState.InBody,
                HtmlTreeBuilderState.InBody,
                HtmlTreeBuilderState.InFrameset,
                HtmlTreeBuilderState.BeforeHead
        };

        for (int i = 0; i < tags.length; i++) {
            tb.getStack().clear();
            tb.getStack().add(new Element(Tag.valueOf("html"), ""));
            tb.getStack().add(new Element(Tag.valueOf(tags[i]), ""));
            tb.resetInsertionMode();
            assertEquals("Testing reset for tag: " + tags[i], expectedStates[i], tb.state());
        }

        // Test "td" cell
        tb.getStack().clear();
        tb.getStack().add(new Element(Tag.valueOf("html"), ""));
        tb.getStack().add(new Element(Tag.valueOf("td"), ""));
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, tb.state());
    }

    @Test(timeout = 4000)
    public void testStateTransitionsAndMarking() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("", "", ParseErrorList.noTracking());

        tb.transition(HtmlTreeBuilderState.InTable);
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());

        tb.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTable, tb.originalState());

        tb.transition(HtmlTreeBuilderState.InCell);
        assertEquals(HtmlTreeBuilderState.InCell, tb.state());
        assertEquals(HtmlTreeBuilderState.InTable, tb.originalState());

        tb.framesetOk(false);
        assertFalse(tb.framesetOk());
        tb.framesetOk(true);
        assertTrue(tb.framesetOk());
    }

    @Test(timeout = 4000)
    public void testBaseUriHandling() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html>", "http://original.com", ParseErrorList.noTracking());
        assertEquals("http://original.com", tb.getBaseUri());

        Element baseElWithHref = new Element(Tag.valueOf("base"), "http://original.com");
        baseElWithHref.attr("href", "http://newbase.com/path/");
        tb.maybeSetBaseUri(baseElWithHref);
        assertEquals("http://newbase.com/path/", tb.getBaseUri());
        assertEquals("http://newbase.com/path/", tb.getDocument().baseUri());

        // Second <base> tag should be ignored
        Element baseEl2 = new Element(Tag.valueOf("base"), "http://original.com");
        baseEl2.attr("href", "http://secondbase.com/");
        tb.maybeSetBaseUri(baseEl2);
        assertEquals("http://newbase.com/path/", tb.getBaseUri());

        // Base tag without href should be ignored if baseUriSetFromDoc was false
        HtmlTreeBuilder tb2 = new HtmlTreeBuilder();
        tb2.initialiseParse("<html>", "http://initial.com", ParseErrorList.noTracking());
        Element baseWithoutHref = new Element(Tag.valueOf("base"), "http://initial.com");
        tb2.maybeSetBaseUri(baseWithoutHref);
        assertEquals("http://initial.com", tb2.getBaseUri());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Fragment Parsing
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseFragmentWithDifferentContexts() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();

        // Null context
        List<Node> nodesNull = tb.parseFragment("<p>One</p>", null, "http://example.com", ParseErrorList.noTracking());
        assertTrue(tb.isFragmentParsing());
        assertFalse(nodesNull.isEmpty());

        // Specific contexts: title (Rcdata), textarea (Rcdata)
        Element titleContext = new Element(Tag.valueOf("title"), "");
        List<Node> titleNodes = tb.parseFragment("Sample &amp; Text", titleContext, "http://example.com", ParseErrorList.noTracking());
        assertNotNull(titleNodes);

        // Rawtext contexts: style, iframe, noembed, noframes, xmp
        String[] rawContexts = new String[]{"style", "iframe", "noembed", "noframes", "xmp"};
        for (String ctxTag : rawContexts) {
            Element ctx = new Element(Tag.valueOf(ctxTag), "");
            List<Node> rawNodes = tb.parseFragment("content", ctx, "http://example.com", ParseErrorList.noTracking());
            assertNotNull(rawNodes);
        }

        // Script context
        Element scriptContext = new Element(Tag.valueOf("script"), "");
        List<Node> scriptNodes = tb.parseFragment("var x = 1;", scriptContext, "http://example.com", ParseErrorList.noTracking());
        assertNotNull(scriptNodes);

        // Noscript and Plaintext contexts
        Element noscriptContext = new Element(Tag.valueOf("noscript"), "");
        assertNotNull(tb.parseFragment("text", noscriptContext, "http://example.com", ParseErrorList.noTracking()));

        Element plaintextContext = new Element(Tag.valueOf("plaintext"), "");
        assertNotNull(tb.parseFragment("text", plaintextContext, "http://example.com", ParseErrorList.noTracking()));
    }

    @Test(timeout = 4000)
    public void testParseFragmentFormAssociation() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        Element div = new Element(Tag.valueOf("div"), "");
        form.appendChild(div);

        // Context has an ancestor FormElement
        List<Node> nodes = tb.parseFragment("<input name='q'>", div, "http://example.com", ParseErrorList.noTracking());
        assertNotNull(nodes);
        assertNotNull(tb.getFormElement());
        assertEquals("form", tb.getFormElement().tagName());
    }

    // =========================================================================
    // Partition C: Scopes, Stack Manipulation & Foster Parenting
    // =========================================================================

    @Test(timeout = 4000)
    public void testScopeMethods() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>", "", ParseErrorList.noTracking());

        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        tb.push(html);
        tb.push(table);
        tb.push(tr);
        tb.push(p);

        assertTrue(tb.inScope("p"));
        assertTrue(tb.inScope(new String[]{"p"}));
        assertTrue(tb.inScope("tr"));
        assertTrue(tb.inScope("table"));

        assertFalse(tb.inTableScope("p"));
        assertFalse(tb.inTableScope("tr"));
        assertTrue(tb.inTableScope("table"));

        Element ol = new Element(Tag.valueOf("ol"), "");
        tb.push(ol);
        Element li = new Element(Tag.valueOf("li"), "");
        tb.push(li);

        assertTrue(tb.inListItemScope("li"));
        assertFalse(tb.inListItemScope("tr")); // blocked by ol

        Element button = new Element(Tag.valueOf("button"), "");
        tb.push(button);
        Element span = new Element(Tag.valueOf("span"), "");
        tb.push(span);

        assertTrue(tb.inButtonScope("span"));
        assertFalse(tb.inButtonScope("li")); // blocked by button
    }

    @Test(timeout = 4000)
    public void testInSelectScope() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>", "", ParseErrorList.noTracking());

        tb.push(new Element(Tag.valueOf("html"), ""));
        tb.push(new Element(Tag.valueOf("select"), ""));
        tb.push(new Element(Tag.valueOf("option"), ""));

        assertTrue(tb.inSelectScope("option"));
        assertFalse(tb.inSelectScope("div"));
    }

    @Test(timeout = 4000)
    public void testStackManipulation() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>", "", ParseErrorList.noTracking());

        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("span"), "");
        Element el3 = new Element(Tag.valueOf("p"), "");

        tb.push(el1);
        tb.push(el2);
        tb.push(el3);

        assertTrue(tb.onStack(el2));
        assertEquals(el3, tb.currentElement());
        assertEquals(el2, tb.aboveOnStack(el3));
        assertEquals(el3, tb.pop());
        assertFalse(tb.onStack(el3));

        Element el4 = new Element(Tag.valueOf("a"), "");
        tb.insertOnStackAfter(el1, el4);
        assertEquals(el4, tb.getStack().get(1));

        Element el5 = new Element(Tag.valueOf("b"), "");
        tb.replaceOnStack(el4, el5);
        assertEquals(el5, tb.getStack().get(1));

        assertTrue(tb.removeFromStack(el5));
        assertFalse(tb.removeFromStack(el5));

        assertEquals(el1, tb.getFromStack("div"));
        assertNull(tb.getFromStack("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseAndBefore() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>", "", ParseErrorList.noTracking());

        Element div = new Element(Tag.valueOf("div"), "");
        Element ul = new Element(Tag.valueOf("ul"), "");
        Element li = new Element(Tag.valueOf("li"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        tb.push(div);
        tb.push(ul);
        tb.push(li);
        tb.push(span);

        tb.popStackToClose("li");
        assertFalse(tb.onStack(li));
        assertFalse(tb.onStack(span));
        assertTrue(tb.onStack(ul));

        tb.push(li);
        tb.push(span);
        tb.popStackToClose("ul", "div");
        assertFalse(tb.onStack(ul));
        assertTrue(tb.onStack(div));

        tb.push(ul);
        tb.push(li);
        tb.popStackToBefore("ul");
        assertTrue(tb.onStack(ul));
        assertFalse(tb.onStack(li));
    }

    @Test(timeout = 4000)
    public void testClearStackToContexts() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>", "", ParseErrorList.noTracking());

        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tbody = new Element(Tag.valueOf("tbody"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        tb.push(html);
        tb.push(table);
        tb.push(tbody);
        tb.push(tr);
        tb.push(span);

        tb.clearStackToTableRowContext();
        assertEquals("tr", tb.currentElement().nodeName());

        tb.push(span);
        tb.clearStackToTableBodyContext();
        assertEquals("tbody", tb.currentElement().nodeName());

        tb.push(span);
        tb.clearStackToTableContext();
        assertEquals("table", tb.currentElement().nodeName());
    }

    @Test(timeout = 4000)
    public void testActiveFormattingElementsAndReconstruction() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>", "", ParseErrorList.noTracking());

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        Element b1 = new Element(Tag.valueOf("b"), "");
        Element b2 = new Element(Tag.valueOf("b"), "");
        Element b3 = new Element(Tag.valueOf("b"), "");
        Element b4 = new Element(Tag.valueOf("b"), "");

        tb.pushActiveFormattingElements(b1);
        tb.pushActiveFormattingElements(b2);
        tb.pushActiveFormattingElements(b3);
        assertEquals(3, tb.lastFormattingElement() != null ? 3 : 0);

        // 4th identical element causes the earliest one to be removed
        tb.pushActiveFormattingElements(b4);
        assertFalse(tb.isInActiveFormattingElements(b1));
        assertTrue(tb.isInActiveFormattingElements(b4));

        tb.insertMarkerToFormattingElements();
        assertNull(tb.lastFormattingElement());

        Element iTag = new Element(Tag.valueOf("i"), "");
        tb.pushActiveFormattingElements(iTag);
        assertEquals(iTag, tb.getActiveFormattingElement("i"));
        assertNull(tb.getActiveFormattingElement("b")); // stopped by marker

        tb.clearFormattingElementsToLastMarker();
        assertFalse(tb.isInActiveFormattingElements(iTag));

        // Test reconstruct formatting elements
        Element a = new Element(Tag.valueOf("a"), "");
        a.attr("href", "http://example.com");
        tb.pushActiveFormattingElements(a);
        // 'a' is not on stack, reconstruct should clone it into stack
        tb.reconstructFormattingElements();
        assertTrue(tb.onStack(tb.currentElement()));
        assertEquals("a", tb.currentElement().nodeName());
        assertEquals("http://example.com", tb.currentElement().attr("href"));
    }

    @Test(timeout = 4000)
    public void testFosterParenting() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<table>", "", ParseErrorList.noTracking());

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");

        html.appendChild(body);
        body.appendChild(table);

        tb.push(html);
        tb.push(body);
        tb.push(table);

        tb.setFosterInserts(true);
        assertTrue(tb.isFosterInserts());

        Element fostered = new Element(Tag.valueOf("span"), "");
        tb.insertInFosterParent(fostered);

        // Fostered element should be inserted before the table in table's parent
        assertEquals(body, fostered.parent());
        assertEquals(0, body.children().indexOf(fostered));
        assertEquals(1, body.children().indexOf(table));

        // Frag case: table has no parent
        Element orphanTable = new Element(Tag.valueOf("table"), "");
        HtmlTreeBuilder tbFrag = new HtmlTreeBuilder();
        tbFrag.initialiseParse("<table>", "", ParseErrorList.noTracking());
        tbFrag.push(html);
        tbFrag.push(orphanTable);

        Element fostered2 = new Element(Tag.valueOf("div"), "");
        tbFrag.insertInFosterParent(fostered2);
        assertEquals(html, fostered2.parent());
    }

    // =========================================================================
    // Partition D: Insertions, Tags, and Tokens
    // =========================================================================

    @Test(timeout = 4000)
    public void testInsertStartTagAndEmpty() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>", "", ParseErrorList.noTracking());
        tb.push(new Element(Tag.valueOf("html"), ""));

        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("img", new Attributes());
        Element img = tb.insertEmpty(startTag);
        assertEquals("img", img.tagName());
        assertFalse(tb.onStack(img)); // insertEmpty does not push to stack

        Token.StartTag divStart = new Token.StartTag();
        divStart.nameAttr("div", new Attributes());
        Element div = tb.insert(divStart);
        assertEquals("div", div.tagName());
        assertTrue(tb.onStack(div));

        Element custom = tb.insertStartTag("custom");
        assertEquals("custom", custom.tagName());
        assertTrue(tb.onStack(custom));
    }

    @Test(timeout = 4000)
    public void testInsertCharactersAndComments() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>", "", ParseErrorList.noTracking());
        Element div = new Element(Tag.valueOf("div"), "");
        tb.push(div);

        // Normal text node
        tb.insert(new Token.Character().data("Hello world"));
        assertEquals(1, div.childNodes().size());
        assertTrue(div.childNode(0) instanceof TextNode);

        // Script characters become DataNode
        Element script = new Element(Tag.valueOf("script"), "");
        tb.push(script);
        tb.insert(new Token.Character().data("alert(1);"));
        assertEquals(1, script.childNodes().size());
        assertTrue(script.childNode(0) instanceof DataNode);
        tb.pop();

        // Comments
        tb.insert(new Token.Comment().setData("my comment"));
        assertTrue(div.childNode(1) instanceof Comment);
    }

    @Test(timeout = 4000)
    public void testGenerateImpliedEndTags() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>", "", ParseErrorList.noTracking());

        tb.push(new Element(Tag.valueOf("html"), ""));
        tb.push(new Element(Tag.valueOf("body"), ""));
        tb.push(new Element(Tag.valueOf("p"), ""));

        tb.generateImpliedEndTags("other");
        assertEquals("body", tb.currentElement().nodeName()); // 'p' was popped

        tb.push(new Element(Tag.valueOf("p"), ""));
        tb.generateImpliedEndTags("p"); // excluded
        assertEquals("p", tb.currentElement().nodeName());

        tb.generateImpliedEndTags();
        assertEquals("body", tb.currentElement().nodeName());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Utility Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testPendingTableCharactersAndSpecialTagCheck() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        List<String> list = new ArrayList<String>();
        list.add("test");
        tb.setPendingTableCharacters(list);
        assertEquals(1, tb.getPendingTableCharacters().size());

        tb.newPendingTableCharacters();
        assertTrue(tb.getPendingTableCharacters().isEmpty());

        Element address = new Element(Tag.valueOf("address"), "");
        Element custom = new Element(Tag.valueOf("mycustomtag"), "");
        assertTrue(tb.isSpecial(address));
        assertFalse(tb.isSpecial(custom));

        tb.setHeadElement(address);
        assertEquals(address, tb.getHeadElement());

        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        tb.setFormElement(form);
        assertEquals(form, tb.getFormElement());
    }

    @Test(timeout = 4000)
    public void testToStringContract() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>", "", ParseErrorList.noTracking());
        tb.push(new Element(Tag.valueOf("html"), ""));

        String str = tb.toString();
        assertNotNull(str);
        assertTrue(str.contains("TreeBuilder{"));
        assertTrue(str.contains("state="));
        assertTrue(str.contains("currentElement="));
    }
}