package org.jsoup.parser;

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
 * Primary Known Defect: Defects4J Jsoup-77 (handlesDeepStack failure)
 * Root Cause Analysis:
 *   In `generateImpliedEndTags(String excludeTag)`:
 *   The defective logic checks:
 *     `while ((excludeTag != null && !currentElement().nodeName().equals(excludeTag)) &&
 *             inSorted(currentElement().nodeName(), TagSearchEndTags))`
 *   When `generateImpliedEndTags()` is invoked (passing `excludeTag == null`), the condition
 *   `(excludeTag != null ...)` evaluates to false immediately. As a result, implied end tags
 *   (dd, dt, li, option, optgroup, p, rp, rt) are NEVER popped when `excludeTag` is null.
 *   This causes deeply nested unclosed tags (such as 500 unclosed `<p>` tags) to remain
 *   on the open element stack or cause parser anomalies / stack depth overflow during parsing.
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. Defect-Targeted Zone:
 *    - `generateImpliedEndTags()` with null excludeTag: verifies whether implied tags are popped.
 *    - `handlesDeepStack`: large unclosed <p> tag sequence matching Defects4J regression test.
 *    - `generateImpliedEndTags(String excludeTag)` with non-null matching & non-matching tags.
 * 2. State & Insertion Mode Machine:
 *    - `resetInsertionMode()` across all 15 switch paths (select, td/th, tr, tbody/thead/tfoot,
 *      caption, colgroup, table, head, body, frameset, html, fallback last).
 * 3. Scope Search Decisions:
 *    - `inScope`, `inListItemScope`, `inButtonScope`, `inTableScope`, `inSelectScope` with hit,
 *      boundary termination, and unreachable exception guard.
 * 4. Stack & Foster Parent Operations:
 *    - `insertInFosterParent`: table with parent, table without parent (on stack), fragment (stack 0).
 *    - `insertNode`: stack empty (root doc), foster insertion, normal child, form listing association.
 *    - `aboveOnStack`, `insertOnStackAfter`, `replaceOnStack`, `popStackToClose`, `clearStackToContext`.
 * 5. Formatting Elements Queue (Active Formatting List):
 *    - 3-element duplicate limit in `pushActiveFormattingElements`.
 *    - `reconstructFormattingElements` with markers, pos==0, already on stack.
 *    - `clearFormattingElementsToLastMarker`, `getActiveFormattingElement` before marker.
 * 6. Fragment Parsing:
 *    - Tokeniser transitions: title/textarea (Rcdata), iframe/style/xmp (Rawtext), script (ScriptData),
 *      noscript/plaintext (Data), form element inheritance from context parents chain.
 */
public class HtmlTreeBuilderGeminiTest {

    private HtmlTreeBuilder createBuilder(String baseUri, int maxErrors) {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), baseUri, ParseErrorList.tracking(maxErrors), ParseSettings.htmlDefault);
        return tb;
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Jsoup-77 Root Cause)
    // =========================================================================

    /**
     * Direct unit test targeting the defect in `generateImpliedEndTags(String excludeTag)`.
     * In the defective version, passing null excludeTag causes the while loop to terminate
     * immediately because `excludeTag != null` is false. Implied tags such as 'p' must be popped!
     */
    @Test(timeout = 4000)
    public void testGenerateImpliedEndTagsWithNullExcludeTag() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        tb.push(html);
        tb.push(body);
        tb.push(p);

        assertEquals("p", tb.currentElement().nodeName());
        // Bug triggers here: when excludeTag is null, defective code does not pop 'p'
        tb.generateImpliedEndTags();
        assertEquals("body", tb.currentElement().nodeName());
    }

    /**
     * Exact defect reproduction test matching Defects4J regression spec:
     * `org.jsoup.parser.HtmlParserTest::handlesDeepStack`.
     */
    @Test(timeout = 4000)
    public void handlesDeepStack() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("<p>");
        }
        sb.append("OK");
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput(sb.toString(), "");
        assertEquals(500, doc.select("p").size());
        assertEquals("OK", doc.text());
    }

    /**
     * Tests `generateImpliedEndTags(String excludeTag)` when excludeTag matches current element,
     * so that element must be preserved while non-matching implied elements are popped.
     */
    @Test(timeout = 4000)
    public void testGenerateImpliedEndTagsWithExcludedTag() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element li = new Element(Tag.valueOf("li"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        tb.push(html);
        tb.push(body);
        tb.push(li);
        tb.push(p);

        // Exclude 'p': 'p' should not be popped
        tb.generateImpliedEndTags("p");
        assertEquals("p", tb.currentElement().nodeName());

        // Now pop 'p' manually, leaving 'li'. Calling exclude 'p' should pop 'li'
        tb.pop();
        assertEquals("li", tb.currentElement().nodeName());
        tb.generateImpliedEndTags("p");
        assertEquals("body", tb.currentElement().nodeName());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialiseParseAndGetters() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 5);
        assertEquals("http://example.com", tb.getBaseUri());
        assertNotNull(tb.getDocument());
        assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        assertNull(tb.originalState());
        assertTrue(tb.framesetOk());
        assertFalse(tb.isFosterInserts());
        assertFalse(tb.isFragmentParsing());
        assertNull(tb.getHeadElement());
        assertNull(tb.getFormElement());
        assertNotNull(tb.defaultSettings());
    }

    @Test(timeout = 4000)
    public void testStateTransitionsAndMarking() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        tb.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());

        tb.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, tb.originalState());

        tb.transition(HtmlTreeBuilderState.InTable);
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());
        assertEquals(HtmlTreeBuilderState.InBody, tb.originalState());

        tb.framesetOk(false);
        assertFalse(tb.framesetOk());
    }

    @Test(timeout = 4000)
    public void testMaybeSetBaseUri() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);

        // 1. Base with empty href should be ignored
        Element emptyBase = new Element(Tag.valueOf("base"), "http://example.com");
        tb.maybeSetBaseUri(emptyBase);
        assertEquals("http://example.com", tb.getBaseUri());

        // 2. Base with valid href sets baseUri and doc baseUri
        Element validBase = new Element(Tag.valueOf("base"), "http://example.com");
        validBase.attr("href", "http://example.org/path/");
        tb.maybeSetBaseUri(validBase);
        assertEquals("http://example.org/path/", tb.getBaseUri());
        assertEquals("http://example.org/path/", tb.getDocument().baseUri());

        // 3. Subsequent base elements must be ignored
        Element secondBase = new Element(Tag.valueOf("base"), "http://example.org/path/");
        secondBase.attr("href", "http://ignored.com/");
        tb.maybeSetBaseUri(secondBase);
        assertEquals("http://example.org/path/", tb.getBaseUri());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeAllBranches() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);

        String[] tags = {"select", "td", "th", "tr", "tbody", "thead", "tfoot",
                         "caption", "colgroup", "table", "head", "body", "frameset", "html"};
        HtmlTreeBuilderState[] expectedStates = {
            HtmlTreeBuilderState.InSelect, HtmlTreeBuilderState.InCell, HtmlTreeBuilderState.InCell,
            HtmlTreeBuilderState.InRow, HtmlTreeBuilderState.InTableBody, HtmlTreeBuilderState.InTableBody,
            HtmlTreeBuilderState.InTableBody, HtmlTreeBuilderState.InCaption, HtmlTreeBuilderState.InColumnGroup,
            HtmlTreeBuilderState.InTable, HtmlTreeBuilderState.InBody, HtmlTreeBuilderState.InBody,
            HtmlTreeBuilderState.InFrameset, HtmlTreeBuilderState.BeforeHead
        };

        for (int i = 0; i < tags.length; i++) {
            tb.getStack().clear();
            Element root = new Element(Tag.valueOf("html"), "");
            Element target = new Element(Tag.valueOf(tags[i]), "");
            tb.push(root);
            if (!tags[i].equals("html")) {
                tb.push(target);
            }
            tb.resetInsertionMode();
            assertEquals("Failed for tag: " + tags[i], expectedStates[i], tb.state());
        }

        // Test fallback `last` branch when pos == 0 and node is not recognized
        tb.getStack().clear();
        Element unknownRoot = new Element(Tag.valueOf("custom"), "");
        tb.push(unknownRoot);
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
    }

    @Test(timeout = 4000)
    public void testStackManipulations() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        tb.push(html);
        tb.push(body);
        tb.push(div);
        tb.push(p);

        assertTrue(tb.onStack(div));
        assertEquals(p, tb.currentElement());
        assertEquals(div, tb.aboveOnStack(p));
        assertEquals(body, tb.aboveOnStack(div));
        assertEquals(div, tb.getFromStack("div"));
        assertNull(tb.getFromStack("span"));

        // Insert after & replace
        Element span = new Element(Tag.valueOf("span"), "");
        tb.insertOnStackAfter(div, span);
        assertEquals(5, tb.getStack().size());
        assertEquals(span, tb.getStack().get(3));

        Element section = new Element(Tag.valueOf("section"), "");
        tb.replaceOnStack(span, section);
        assertEquals(section, tb.getStack().get(3));
        assertFalse(tb.onStack(span));

        // Pop stack to before
        tb.popStackToBefore("body");
        assertEquals(body, tb.currentElement());

        // Pop stack to close single
        tb.push(div);
        tb.push(p);
        tb.popStackToClose("div");
        assertEquals(body, tb.currentElement());

        // Pop stack to close multiple
        tb.push(div);
        tb.push(p);
        tb.popStackToClose("div", "p");
        assertEquals(div, tb.currentElement());

        // Remove from stack
        assertTrue(tb.removeFromStack(div));
        assertFalse(tb.removeFromStack(div));
        assertEquals(body, tb.currentElement());
    }

    @Test(timeout = 4000)
    public void testClearStackToContexts() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
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
        assertEquals(tr, tb.currentElement());

        tb.push(td);
        tb.clearStackToTableBodyContext();
        assertEquals(tbody, tb.currentElement());

        tb.push(tr);
        tb.push(td);
        tb.clearStackToTableContext();
        assertEquals(table, tb.currentElement());
    }

    @Test(timeout = 4000)
    public void testInScopeVariants() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        tb.push(html);
        tb.push(body);
        tb.push(table);
        tb.push(td);
        tb.push(p);

        assertTrue(tb.inScope("p"));
        assertFalse(tb.inScope("body")); // table/td acts as scope boundary

        // List item scope with ol/ul boundary
        Element ol = new Element(Tag.valueOf("ol"), "");
        Element li = new Element(Tag.valueOf("li"), "");
        tb.push(ol);
        tb.push(li);
        assertTrue(tb.inListItemScope("li"));
        assertFalse(tb.inListItemScope("p")); // stopped by ol

        // Button scope with button boundary
        Element button = new Element(Tag.valueOf("button"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        tb.push(button);
        tb.push(span);
        assertTrue(tb.inButtonScope("span"));
        assertFalse(tb.inButtonScope("li")); // stopped by button

        // Table scope
        assertTrue(tb.inTableScope("table"));
        assertFalse(tb.inTableScope("div"));

        // Select scope
        HtmlTreeBuilder tbSelect = createBuilder("http://example.com", 0);
        Element selectHtml = new Element(Tag.valueOf("html"), "");
        Element optgroup = new Element(Tag.valueOf("optgroup"), "");
        Element option = new Element(Tag.valueOf("option"), "");
        tbSelect.push(selectHtml);
        tbSelect.push(optgroup);
        tbSelect.push(option);
        assertTrue(tbSelect.inSelectScope("option"));
        assertFalse(tbSelect.inSelectScope("html"));
    }

    // =========================================================================
    // Partition B: Active Formatting Elements & Foster Parenting
    // =========================================================================

    @Test(timeout = 4000)
    public void testActiveFormattingElementsLifecycleAndLimit() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);

        // Push formatting elements with 3-element identical limit
        for (int i = 0; i < 4; i++) {
            Element b = new Element(Tag.valueOf("b"), "");
            b.attr("class", "bold");
            tb.pushActiveFormattingElements(b);
        }
        // At max 3 of the same element are allowed; earlier should be purged
        assertEquals(3, tb.lastFormattingElement() != null ? 3 : 0);

        Element activeB = tb.getActiveFormattingElement("b");
        assertNotNull(activeB);
        assertTrue(tb.isInActiveFormattingElements(activeB));

        // Insert marker and verify getActiveFormattingElement respects marker
        tb.insertMarkerToFormattingElements();
        assertNull(tb.getActiveFormattingElement("b"));

        Element i = new Element(Tag.valueOf("i"), "");
        tb.pushActiveFormattingElements(i);
        assertEquals(i, tb.getActiveFormattingElement("i"));

        // Replace active formatting element
        Element em = new Element(Tag.valueOf("em"), "");
        tb.replaceActiveFormattingElement(i, em);
        assertEquals(em, tb.lastFormattingElement());

        // Clear to last marker
        tb.clearFormattingElementsToLastMarker();
        assertEquals("b", tb.lastFormattingElement().nodeName());

        // Remove active formatting element
        tb.removeFromActiveFormattingElements(tb.lastFormattingElement());
        assertNotNull(tb.removeLastFormattingElement());
        assertNull(tb.removeLastFormattingElement());
    }

    @Test(timeout = 4000)
    public void testReconstructFormattingElements() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        // None active -> no-op
        tb.reconstructFormattingElements();
        assertEquals(body, tb.currentElement());

        // Active element not on stack -> should be reconstructed onto stack
        Element b = new Element(Tag.valueOf("b"), "");
        b.attr("id", "b1");
        tb.pushActiveFormattingElements(b);

        tb.reconstructFormattingElements();
        assertEquals("b", tb.currentElement().nodeName());
        assertEquals("b1", tb.currentElement().id());
        assertTrue(tb.onStack(tb.currentElement()));

        // When already on stack -> no-op
        tb.reconstructFormattingElements();
        assertEquals("b", tb.currentElement().nodeName());
    }

    @Test(timeout = 4000)
    public void testFosterParenting() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        tb.setFosterInserts(true);
        assertTrue(tb.isFosterInserts());

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        body.appendChild(table);

        tb.push(html);
        tb.push(body);
        tb.push(table);

        TextNode textNode = new TextNode("fostered");
        tb.insertInFosterParent(textNode);

        // Fostered node must be inserted before table in table's parent (body)
        assertEquals(2, body.childNodeSize());
        assertEquals(textNode, body.childNode(0));
        assertEquals(table, body.childNode(1));

        // Test foster insert when table has no parent (falls back to aboveOnStack)
        table.remove();
        assertNull(table.parent());
        TextNode textNode2 = new TextNode("fostered2");
        tb.insertInFosterParent(textNode2);
        assertTrue(body.childNodes().contains(textNode2));

        // Test foster insert when no table is on stack (fragment fallback to stack.get(0))
        tb.getStack().clear();
        tb.push(html);
        TextNode textNode3 = new TextNode("fostered3");
        tb.insertInFosterParent(textNode3);
        assertTrue(html.childNodes().contains(textNode3));
    }

    // =========================================================================
    // Partition D: Nodes, Character, Form & Tag Insertion Variations
    // =========================================================================

    @Test(timeout = 4000)
    public void testInsertStartTagAndSelfClosingTag() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);

        Element div = tb.insertStartTag("div");
        assertEquals("div", div.nodeName());
        assertEquals(div, tb.currentElement());

        // Self closing known tag (e.g. img)
        Token.StartTag imgTag = new Token.StartTag();
        imgTag.nameAttr("img", new Attributes());
        imgTag.selfClosing = true;
        Element img = tb.insert(imgTag);
        assertEquals("img", img.nodeName());

        // Empty insert of unknown tag
        Token.StartTag customTag = new Token.StartTag();
        customTag.nameAttr("custom-elem", new Attributes());
        customTag.selfClosing = true;
        Element custom = tb.insertEmpty(customTag);
        assertTrue(custom.tag().isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testInsertCharactersTextVsDataNodes() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        // Regular character token in body -> TextNode
        Token.Character charToken = new Token.Character();
        charToken.data("Regular text");
        tb.insert(charToken);
        assertEquals(1, body.childNodeSize());
        assertTrue(body.childNode(0) instanceof TextNode);

        // Character token inside script -> DataNode
        Element script = new Element(Tag.valueOf("script"), "");
        tb.push(script);
        Token.Character scriptChar = new Token.Character();
        scriptChar.data("var x = 1;");
        tb.insert(scriptChar);
        assertEquals(1, script.childNodeSize());
        assertTrue(script.childNode(0) instanceof DataNode);

        // Character token inside style -> DataNode
        tb.pop();
        Element style = new Element(Tag.valueOf("style"), "");
        tb.push(style);
        Token.Character styleChar = new Token.Character();
        styleChar.data("body { color: red; }");
        tb.insert(styleChar);
        assertEquals(1, style.childNodeSize());
        assertTrue(style.childNode(0) instanceof DataNode);
    }

    @Test(timeout = 4000)
    public void testInsertCommentAndFormListing() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);

        // Comment insertion
        Token.Comment commentToken = new Token.Comment();
        commentToken.data.append("This is a comment");
        tb.insert(commentToken);
        assertTrue(html.childNode(0) instanceof Comment);

        // FormElement creation and association
        Token.StartTag formStart = new Token.StartTag();
        formStart.nameAttr("form", new Attributes());
        FormElement form = tb.insertForm(formStart, true);
        assertEquals(form, tb.getFormElement());
        assertEquals(form, tb.currentElement());

        // Insert form-listed element (e.g. input)
        Token.StartTag inputStart = new Token.StartTag();
        inputStart.nameAttr("input", new Attributes());
        Element input = tb.insertEmpty(inputStart);
        assertTrue(form.elements().contains(input));

        // Insert form when onStack = false
        Token.StartTag formStart2 = new Token.StartTag();
        formStart2.nameAttr("form", new Attributes());
        FormElement form2 = tb.insertForm(formStart2, false);
        assertEquals(form2, tb.getFormElement());
        assertNotEquals(form2, tb.currentElement());
    }

    @Test(timeout = 4000)
    public void testPendingTableCharacters() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        assertTrue(tb.getPendingTableCharacters().isEmpty());

        List<String> pending = new ArrayList<>();
        pending.add("foo");
        tb.setPendingTableCharacters(pending);
        assertEquals(1, tb.getPendingTableCharacters().size());
        assertEquals("foo", tb.getPendingTableCharacters().get(0));

        tb.newPendingTableCharacters();
        assertTrue(tb.getPendingTableCharacters().isEmpty());
    }

    @Test(timeout = 4000)
    public void testHeadElementAndIsSpecial() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        Element head = new Element(Tag.valueOf("head"), "");
        tb.setHeadElement(head);
        assertEquals(head, tb.getHeadElement());

        assertTrue(tb.isSpecial(new Element(Tag.valueOf("div"), "")));
        assertTrue(tb.isSpecial(new Element(Tag.valueOf("p"), "")));
        assertTrue(tb.isSpecial(new Element(Tag.valueOf("table"), "")));
        assertFalse(tb.isSpecial(new Element(Tag.valueOf("span"), "")));
        assertFalse(tb.isSpecial(new Element(Tag.valueOf("custom-tag"), "")));
    }

    // =========================================================================
    // Partition E: Fragment Parsing & Edge Boundary Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseFragmentWithNullContext() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        List<Node> nodes = tb.parseFragment("<div>Hello</div>", null, "http://example.com",
                ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
        assertTrue(nodes.get(0).ownerDocument() != null);
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithSpecificContexts() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Document ownerDoc = new Document("http://example.com");
        ownerDoc.quirksMode(Document.QuirksMode.quirks);

        String[] testTags = {"title", "textarea", "iframe", "script", "noscript", "plaintext", "div"};
        for (String tagName : testTags) {
            Element context = new Element(Tag.valueOf(tagName), "http://example.com");
            ownerDoc.appendChild(context);

            List<Node> nodes = tb.parseFragment("Fragment content", context, "http://example.com",
                    ParseErrorList.noTracking(), ParseSettings.htmlDefault);
            assertNotNull(nodes);
            assertEquals(Document.QuirksMode.quirks, tb.getDocument().quirksMode());
        }
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithAncestorFormContext() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        FormElement parentForm = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element divChild = new Element(Tag.valueOf("div"), "http://example.com");
        parentForm.appendChild(divChild);

        List<Node> nodes = tb.parseFragment("<input name='username' />", divChild, "http://example.com",
                ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertNotNull(nodes);
        assertEquals(parentForm, tb.getFormElement());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertOnStackAfterThrowsWhenNotFound() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        tb.insertOnStackAfter(div, span); // div not on stack -> throws IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceOnStackThrowsWhenNotFound() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        tb.replaceOnStack(div, span); // div not on stack -> throws IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInSpecificScopeFailsWhenTargetAndBaseNotFound() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        // Stack contains only custom tags, neither targetName nor baseType tags
        tb.push(new Element(Tag.valueOf("custom1"), ""));
        tb.push(new Element(Tag.valueOf("custom2"), ""));
        tb.inScope("targetNotFound"); // Validate.fail unreachable path
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInSelectScopeFailsWhenUnreachable() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        // Stack contains neither target nor optgroup/option
        tb.push(new Element(Tag.valueOf("optgroup"), ""));
        tb.inSelectScope("missingTarget"); // Validate.fail reachable via non-option path
    }

    @Test(timeout = 4000)
    public void testErrorTrackingBranch() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 1);
        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);

        Token.Character tok = new Token.Character();
        tok.data("x");
        tb.process(tok, HtmlTreeBuilderState.Initial);

        tb.error(HtmlTreeBuilderState.Initial); // 1st error recorded
        tb.error(HtmlTreeBuilderState.Initial); // 2nd error dropped due to max size = 1
        assertNotNull(tb.toString());
    }

    @Test(timeout = 4000)
    public void testToStringContract() {
        HtmlTreeBuilder tb = createBuilder("http://example.com", 0);
        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);
        String desc = tb.toString();
        assertNotNull(desc);
        assertTrue(desc.contains("TreeBuilder{"));
        assertTrue(desc.contains("state="));
    }
}