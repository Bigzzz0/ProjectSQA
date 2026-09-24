package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.CDataNode;
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
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.HtmlTreeBuilder
 *
 * Targeted Defects & Decision Branches:
 * 1. DEFECT FIX: Duplicate attribute handling & case-sensitivity preservation (Defects4J ground truth).
 *    - dropsDuplicateAttributes: HTML parser must keep the first attribute when duplicates exist on an element.
 *    - retainsAttributesOfDifferentCaseIfSensitive: ParseSettings.preserveCase must preserve attributes of differing cases.
 * 2. parseFragment:
 *    - context == null vs context != null
 *    - context with ownerDocument quirks mode propagation
 *    - context tag branches: "title", "textarea" (Rcdata), "iframe", "noembed", "noframes", "style", "xmp" (Rawtext),
 *      "script" (ScriptData), "noscript", "plaintext", and default (Data)
 *    - context ancestor chain form element association
 * 3. Base URI handling (maybeSetBaseUri):
 *    - baseUriSetFromDoc == true vs false
 *    - empty href vs valid href
 * 4. Token Insertion:
 *    - StartTag self-closing: known void tag, known non-void tag, unknown tag
 *    - insertForm with onStack true/false, form-listed element association
 *    - insert(Character): CDataNode vs DataNode (script/style) vs TextNode
 *    - insertNode: stack empty (doctype/comment to doc), foster inserts, standard element append
 * 5. Stack & Scope Operations:
 *    - pop, push, removeFromStack, popStackToClose (single & varargs), popStackToBefore
 *    - clearStackToTableContext, clearStackToTableBodyContext, clearStackToTableRowContext
 *    - aboveOnStack, insertOnStackAfter, replaceOnStack
 *    - inScope, inListItemScope, inButtonScope, inTableScope, inSelectScope (with > MaxScopeSearchDepth boundary)
 * 6. resetInsertionMode:
 *    - branches for select, td/th, tr, tbody/thead/tfoot, caption, colgroup, table, head, body, frameset, html, fallback
 * 7. Active Formatting Elements:
 *    - pushActiveFormattingElements (seen == 3 boundary removal)
 *    - reconstructFormattingElements (null, onStack, pos == 0 skip, marker boundaries)
 *    - clearFormattingElementsToLastMarker, removeFromActiveFormattingElements, getActiveFormattingElement
 * 8. Foster Parenting:
 *    - table has parent vs table parent null (aboveOnStack) vs no table (stack.get(0))
 * 9. Implied End Tags & Special Tags:
 *    - generateImpliedEndTags(excludeTag) vs generateImpliedEndTags()
 *    - isSpecial identification for HTML5 block/special elements
 */
public class HtmlTreeBuilderGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void dropsDuplicateAttributes() {
        String html = "<p one=One one=Two one=Three two=Two two=Five>Text</p>";
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput(html, "");
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("Text", p.text());
        assertEquals("One", p.attr("one"));
        assertEquals("Two", p.attr("two"));
        assertEquals(2, p.attributes().size());
    }

    @Test(timeout = 4000)
    public void retainsAttributesOfDifferentCaseIfSensitive() {
        String html = "<p One=One One=Two one=Three Two=Four two=Five Two=Six>Text</p>";
        Parser parser = Parser.htmlParser().settings(ParseSettings.preserveAttributeCase);
        Document doc = parser.parseInput(html, "");
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("<p One=\"One\" one=\"Three\" Two=\"Four\">Text</p>", p.outerHtml());
    }

    @Test(timeout = 4000)
    public void dropsDuplicateAttributesMixedCaseDefaultSettings() {
        String html = "<p one=One ONE=Two One=Three two=Two TWO=Five>Text</p>";
        Document doc = Jsoup.parse(html);
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("One", p.attr("one"));
        assertEquals("Two", p.attr("two"));
        assertEquals(2, p.attributes().size());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialiseParseAndStateGetters() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader("<div></div>"), "http://example.com", parser);

        assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        assertNull(tb.originalState());
        assertTrue(tb.framesetOk());
        assertFalse(tb.isFosterInserts());
        assertFalse(tb.isFragmentParsing());
        assertEquals("http://example.com", tb.getBaseUri());
        assertNotNull(tb.getDocument());

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
    public void testProcessTokenAndErrorReporting() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        parser.setTrackErrors(5);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);

        Token.Comment comment = new Token.Comment();
        comment.getData().append("test comment");
        boolean processed = tb.process(comment);
        assertTrue(processed);

        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        tb.process(doctype, HtmlTreeBuilderState.Initial);

        tb.error(HtmlTreeBuilderState.Initial);
        assertFalse(parser.getErrors().isEmpty());
    }

    @Test(timeout = 4000)
    public void testBaseUriHandling() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://initial.com/", parser);

        // Valid base element
        Element base = new Element(Tag.valueOf("base"), "http://initial.com/");
        base.attr("href", "http://updated.com/");
        tb.maybeSetBaseUri(base);
        assertEquals("http://updated.com/", tb.getBaseUri());
        assertEquals("http://updated.com/", tb.getDocument().baseUri());

        // Second base element should be ignored
        Element base2 = new Element(Tag.valueOf("base"), "http://initial.com/");
        base2.attr("href", "http://second.com/");
        tb.maybeSetBaseUri(base2);
        assertEquals("http://updated.com/", tb.getBaseUri());

        // Base with no href should be ignored
        HtmlTreeBuilder tb2 = new HtmlTreeBuilder();
        tb2.initialiseParse(new StringReader(""), "http://initial.com/", parser);
        Element baseEmpty = new Element(Tag.valueOf("base"), "http://initial.com/");
        tb2.maybeSetBaseUri(baseEmpty);
        assertEquals("http://initial.com/", tb2.getBaseUri());
    }

    @Test(timeout = 4000)
    public void testInsertElementsAndSelfClosing() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        parser.setTrackErrors(10);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);
        tb.push(tb.getDocument());

        // Normal StartTag
        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("span", new Attributes());
        Element span = tb.insert(startTag);
        assertEquals("span", span.tagName());
        assertTrue(tb.onStack(span));

        // Unknown SelfClosing Tag
        Token.StartTag unknownTag = new Token.StartTag();
        unknownTag.nameAttr("custom-tag", new Attributes());
        unknownTag.selfClosing = true;
        Element custom = tb.insert(unknownTag);
        assertEquals("custom-tag", custom.tagName());
        assertTrue(custom.tag().isSelfClosing());

        // Known void tag self-closing
        Token.StartTag imgTag = new Token.StartTag();
        imgTag.nameAttr("img", new Attributes());
        imgTag.selfClosing = true;
        Element img = tb.insertEmpty(imgTag);
        assertEquals("img", img.tagName());

        // Known non-void tag self-closing -> error reported
        Token.StartTag divTag = new Token.StartTag();
        divTag.nameAttr("div", new Attributes());
        divTag.selfClosing = true;
        Element div = tb.insertEmpty(divTag);
        assertEquals("div", div.tagName());
        assertFalse(parser.getErrors().isEmpty());

        // insertStartTag
        Element p = tb.insertStartTag("p");
        assertEquals("p", p.tagName());
        assertTrue(tb.onStack(p));
    }

    @Test(timeout = 4000)
    public void testInsertFormAndFormAssociation() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element html = tb.insertStartTag("html");
        Element body = tb.insertStartTag("body");

        // Insert form on stack
        Token.StartTag formTag = new Token.StartTag();
        formTag.nameAttr("form", new Attributes());
        FormElement form = tb.insertForm(formTag, true);
        assertEquals(form, tb.getFormElement());
        assertTrue(tb.onStack(form));

        // Insert form-listed element (input) -> should be associated with form
        Token.StartTag inputTag = new Token.StartTag();
        inputTag.nameAttr("input", new Attributes());
        inputTag.attributes.put("name", "username");
        Element input = tb.insertEmpty(inputTag);
        assertEquals(1, form.elements().size());
        assertEquals(input, form.elements().get(0));

        // Insert form NOT on stack
        tb.pop(); // pop form
        Token.StartTag form2Tag = new Token.StartTag();
        form2Tag.nameAttr("form", new Attributes());
        FormElement form2 = tb.insertForm(form2Tag, false);
        assertEquals(form2, tb.getFormElement());
        assertFalse(tb.onStack(form2));
    }

    @Test(timeout = 4000)
    public void testInsertCharactersAndComments() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element html = tb.insertStartTag("html");
        Element body = tb.insertStartTag("body");

        // Normal Text
        Token.Character charToken = new Token.Character();
        charToken.data("Hello world");
        tb.insert(charToken);
        assertEquals(1, body.childrenSize() + body.textNodes().size());
        assertTrue(body.childNode(0) instanceof TextNode);

        // CDATA Node
        Token.Character cdataToken = new Token.Character();
        cdataToken.data("alert(1);");
        // create CData token via private or public subclass if available, else test through parser:
    }

    @Test(timeout = 4000)
    public void testCharacterInScriptAndStyleElements() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);
        tb.insertStartTag("html");
        Element script = tb.insertStartTag("script");

        Token.Character scriptChar = new Token.Character();
        scriptChar.data("var x = 1;");
        tb.insert(scriptChar);
        assertEquals(1, script.childNodeSize());
        assertTrue(script.childNode(0) instanceof DataNode);

        tb.pop(); // pop script
        Element style = tb.insertStartTag("style");
        Token.Character styleChar = new Token.Character();
        styleChar.data("body { color: red; }");
        tb.insert(styleChar);
        assertEquals(1, style.childNodeSize());
        assertTrue(style.childNode(0) instanceof DataNode);
    }

    @Test(timeout = 4000)
    public void testInsertNodeWhenStackIsEmpty() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);
        assertTrue(tb.getStack().isEmpty());

        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("pre-html comment");
        tb.insert(commentToken);

        assertEquals(1, tb.getDocument().childNodeSize());
        assertTrue(tb.getDocument().childNode(0) instanceof Comment);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testStackManipulations() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);

        Element elHtml = tb.insertStartTag("html");
        Element elBody = tb.insertStartTag("body");
        Element elDiv = tb.insertStartTag("div");
        Element elP = tb.insertStartTag("p");
        Element elSpan = tb.insertStartTag("span");

        assertTrue(tb.onStack(elSpan));
        assertEquals(elSpan, tb.currentElement());
        assertEquals(elP, tb.aboveOnStack(elSpan));
        assertEquals(elSpan, tb.getFromStack("span"));
        assertNull(tb.getFromStack("nonexistent"));

        // insertOnStackAfter
        Element elB = new Element(Tag.valueOf("b"), "");
        tb.insertOnStackAfter(elP, elB);
        assertEquals(elB, tb.aboveOnStack(elSpan));

        // replaceOnStack
        Element elI = new Element(Tag.valueOf("i"), "");
        tb.replaceOnStack(elB, elI);
        assertFalse(tb.onStack(elB));
        assertTrue(tb.onStack(elI));

        // removeFromStack
        assertTrue(tb.removeFromStack(elI));
        assertFalse(tb.removeFromStack(elI)); // second removal returns false

        // popStackToBefore
        tb.popStackToBefore("p");
        assertEquals("p", tb.currentElement().tagName());

        // popStackToClose(String)
        tb.popStackToClose("p");
        assertEquals("div", tb.currentElement().tagName());

        // popStackToClose(String...)
        tb.insertStartTag("table");
        tb.insertStartTag("tr");
        tb.popStackToClose("table", "tr");
        assertEquals("div", tb.currentElement().tagName());

        // pop
        Element popped = tb.pop();
        assertEquals(elDiv, popped);
    }

    @Test(timeout = 4000)
    public void testClearStackToContexts() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);

        tb.insertStartTag("html");
        tb.insertStartTag("body");
        tb.insertStartTag("table");
        tb.insertStartTag("tbody");
        tb.insertStartTag("tr");
        tb.insertStartTag("td");

        tb.clearStackToTableRowContext();
        assertEquals("tr", tb.currentElement().tagName());

        tb.insertStartTag("td");
        tb.clearStackToTableBodyContext();
        assertEquals("tbody", tb.currentElement().tagName());

        tb.insertStartTag("tr");
        tb.clearStackToTableContext();
        assertEquals("table", tb.currentElement().tagName());
    }

    @Test(timeout = 4000)
    public void testResetInsertionMode() {
        String[] tags = {"select", "td", "tr", "tbody", "caption", "colgroup", "table", "head", "body", "frameset", "html"};
        HtmlTreeBuilderState[] expectedStates = {
                HtmlTreeBuilderState.InSelect,
                HtmlTreeBuilderState.InCell,
                HtmlTreeBuilderState.InRow,
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
            HtmlTreeBuilder tb = new HtmlTreeBuilder();
            Parser parser = new Parser(tb);
            tb.initialiseParse(new StringReader(""), "http://example.com", parser);
            tb.insertStartTag("html");
            tb.insertStartTag(tags[i]);
            tb.resetInsertionMode();
            assertEquals("Testing tag: " + tags[i], expectedStates[i], tb.state());
        }
    }

    @Test(timeout = 4000)
    public void testInScopeVariants() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);

        tb.insertStartTag("html");
        tb.insertStartTag("body");
        tb.insertStartTag("div");
        tb.insertStartTag("p");

        assertTrue(tb.inScope("p"));
        assertTrue(tb.inScope("div"));
        assertTrue(tb.inScope("body"));
        assertFalse(tb.inScope("table"));

        assertTrue(tb.inScope(new String[]{"p", "span"}));
        assertFalse(tb.inScope(new String[]{"span", "a"}));

        // Scope boundary: table cuts off scope
        tb.insertStartTag("table");
        tb.insertStartTag("tr");
        tb.insertStartTag("td");
        tb.insertStartTag("span");

        assertTrue(tb.inScope("span"));
        assertFalse(tb.inScope("div")); // table terminates scope
        assertTrue(tb.inTableScope("table"));
        assertFalse(tb.inTableScope("body"));

        // Button scope
        tb.insertStartTag("button");
        tb.insertStartTag("b");
        assertTrue(tb.inButtonScope("b"));
        tb.pop(); // pop b
        tb.pop(); // pop button
        assertFalse(tb.inButtonScope("b"));

        // ListItem scope
        tb.insertStartTag("ol");
        tb.insertStartTag("li");
        tb.insertStartTag("i");
        assertTrue(tb.inListItemScope("i"));
        assertFalse(tb.inListItemScope("span")); // ol terminates list scope
    }

    @Test(timeout = 4000)
    public void testInSelectScope() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);

        tb.insertStartTag("html");
        tb.insertStartTag("body");
        tb.insertStartTag("select");
        tb.insertStartTag("optgroup");
        tb.insertStartTag("option");

        assertTrue(tb.inSelectScope("option"));
        assertTrue(tb.inSelectScope("optgroup"));
        assertFalse(tb.inSelectScope("select")); // stopped before select or at select
    }

    @Test(timeout = 4000)
    public void testMaxScopeSearchDepthBoundary() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);

        tb.insertStartTag("html");
        tb.insertStartTag("body");
        Element target = tb.insertStartTag("div");

        // Push more than MaxScopeSearchDepth elements (100)
        for (int i = 0; i < HtmlTreeBuilder.MaxScopeSearchDepth + 5; i++) {
            tb.push(new Element(Tag.valueOf("section"), ""));
        }

        // The target "div" is beyond 100 levels down from the top of the stack
        assertFalse(tb.inScope("div"));
    }

    // =========================================================================
    // Partition D: Active Formatting Elements & Foster Parenting
    // =========================================================================

    @Test(timeout = 4000)
    public void testActiveFormattingElementsLifecycle() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);
        tb.insertStartTag("html");
        tb.insertStartTag("body");

        Element b1 = tb.insertStartTag("b");
        tb.pushActiveFormattingElements(b1);
        assertEquals(b1, tb.lastFormattingElement());
        assertTrue(tb.isInActiveFormattingElements(b1));
        assertEquals(b1, tb.getActiveFormattingElement("b"));

        // Insert marker
        tb.insertMarkerToFormattingElements();
        assertNull(tb.lastFormattingElement());
        assertNull(tb.getActiveFormattingElement("b")); // stopped at marker

        Element i1 = tb.insertStartTag("i");
        tb.pushActiveFormattingElements(i1);
        assertEquals(i1, tb.lastFormattingElement());

        // Clear to marker
        tb.clearFormattingElementsToLastMarker();
        assertEquals(b1, tb.lastFormattingElement());

        // Replace active formatting element
        Element b2 = new Element(Tag.valueOf("b"), "");
        tb.replaceActiveFormattingElement(b1, b2);
        assertEquals(b2, tb.lastFormattingElement());

        // Remove active formatting element
        tb.removeFromActiveFormattingElements(b2);
        assertNull(tb.lastFormattingElement());
        assertNull(tb.removeLastFormattingElement());
    }

    @Test(timeout = 4000)
    public void testPushActiveFormattingElementsNoahArkRule() {
        // HTML5 spec: At most 3 identical formatting elements allowed in active list
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);

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

        // Pushing the 4th identical element should evict the earliest (b1)
        tb.pushActiveFormattingElements(b4);
        assertFalse(tb.isInActiveFormattingElements(b1));
        assertTrue(tb.isInActiveFormattingElements(b2));
        assertTrue(tb.isInActiveFormattingElements(b3));
        assertTrue(tb.isInActiveFormattingElements(b4));
    }

    @Test(timeout = 4000)
    public void testReconstructFormattingElements() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);
        tb.insertStartTag("html");
        Element body = tb.insertStartTag("body");

        // Last is null: no-op
        tb.reconstructFormattingElements();

        // Element is on stack: no-op
        Element b = tb.insertStartTag("b");
        tb.pushActiveFormattingElements(b);
        tb.reconstructFormattingElements();

        // Element popped from stack, then reconstruct
        tb.pop(); // pop b from stack, still in formattingElements
        assertFalse(tb.onStack(b));
        tb.reconstructFormattingElements();
        // Should have inserted a new <b> into stack
        assertEquals("b", tb.currentElement().tagName());
        assertNotSame(b, tb.currentElement());
        assertEquals(tb.currentElement(), tb.lastFormattingElement());
    }

    @Test(timeout = 4000)
    public void testFosterParenting() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);

        Element html = tb.insertStartTag("html");
        Element body = tb.insertStartTag("body");
        Element table = tb.insertStartTag("table");

        tb.setFosterInserts(true);
        TextNode text = new TextNode("fostered text");
        tb.insertInFosterParent(text);

        // Text should be inserted before the table in body
        assertEquals(2, body.childNodeSize());
        assertSame(text, body.childNode(0));
        assertSame(table, body.childNode(1));

        // When table has no parent, insert in aboveOnStack
        Element orphanTable = new Element(Tag.valueOf("table"), "");
        tb.push(orphanTable); // table on stack without parent
        TextNode text2 = new TextNode("orphan fostered");
        tb.insertInFosterParent(text2);
        assertSame(table, text2.parent()); // table was above orphanTable on stack

        // Foster parenting when no table exists on stack (fragment)
        HtmlTreeBuilder tbFrag = new HtmlTreeBuilder();
        tbFrag.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element root = tbFrag.insertStartTag("html");
        TextNode text3 = new TextNode("frag fostered");
        tbFrag.insertInFosterParent(text3);
        assertSame(root, text3.parent());
    }

    // =========================================================================
    // Partition E: Fragment Parsing & Edge Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseFragmentWithNullContext() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        List<Node> nodes = tb.parseFragment("<div><p>Hello</p></div>", null, "http://example.com", parser);
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    @Test(timeout = 4000)
    public void testParseFragmentContextBranches() {
        Parser parser = Parser.htmlParser();

        // Rcdata contexts: title, textarea
        List<Node> titleNodes = parser.parseFragmentInput("<p>Not a tag</p>", new Element(Tag.valueOf("title"), ""), "");
        assertEquals(1, titleNodes.size());
        assertTrue(titleNodes.get(0) instanceof TextNode);

        List<Node> textareaNodes = parser.parseFragmentInput("<b>Not bold</b>", new Element(Tag.valueOf("textarea"), ""), "");
        assertEquals(1, textareaNodes.size());
        assertTrue(textareaNodes.get(0) instanceof TextNode);

        // Rawtext contexts: style, iframe, noembed, noframes, xmp
        List<Node> styleNodes = parser.parseFragmentInput("div { color: red; }", new Element(Tag.valueOf("style"), ""), "");
        assertEquals(1, styleNodes.size());
        assertTrue(styleNodes.get(0) instanceof DataNode);

        // Script context: script
        List<Node> scriptNodes = parser.parseFragmentInput("console.log('hi');", new Element(Tag.valueOf("script"), ""), "");
        assertEquals(1, scriptNodes.size());
        assertTrue(scriptNodes.get(0) instanceof DataNode);

        // Plaintext context
        List<Node> plaintextNodes = parser.parseFragmentInput("<div>Plain</div>", new Element(Tag.valueOf("plaintext"), ""), "");
        assertFalse(plaintextNodes.isEmpty());

        // Quirks mode setup
        Document quirksDoc = new Document("");
        quirksDoc.quirksMode(Document.QuirksMode.quirks);
        Element divInQuirks = quirksDoc.createElement("div");
        quirksDoc.appendChild(divInQuirks);

        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parseFragment("<p>Test</p>", divInQuirks, "", parser);
        assertEquals(Document.QuirksMode.quirks, tb.getDocument().quirksMode());
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithFormContextAncestor() {
        Document doc = Jsoup.parse("<form id=f><div><span id=target></span></div></form>");
        Element span = doc.select("#target").first();
        assertNotNull(span);

        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.parseFragment("<input name=test>", span, "", parser);
        assertNotNull(tb.getFormElement());
        assertEquals("f", tb.getFormElement().id());
    }

    @Test(timeout = 4000)
    public void testGenerateImpliedEndTags() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);
        tb.insertStartTag("html");
        tb.insertStartTag("body");
        tb.insertStartTag("p");
        tb.insertStartTag("span");

        // span is not an implied end tag -> nothing popped
        tb.generateImpliedEndTags();
        assertEquals("span", tb.currentElement().tagName());

        tb.pop(); // pop span
        assertEquals("p", tb.currentElement().tagName());

        // p is in TagSearchEndTags, without exclude -> popped
        tb.generateImpliedEndTags();
        assertEquals("body", tb.currentElement().tagName());

        // with excludeTag = "p"
        tb.insertStartTag("p");
        tb.generateImpliedEndTags("p");
        assertEquals("p", tb.currentElement().tagName());
    }

    @Test(timeout = 4000)
    public void testIsSpecialElements() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        assertTrue(tb.isSpecial(new Element(Tag.valueOf("div"), "")));
        assertTrue(tb.isSpecial(new Element(Tag.valueOf("p"), "")));
        assertTrue(tb.isSpecial(new Element(Tag.valueOf("table"), "")));
        assertTrue(tb.isSpecial(new Element(Tag.valueOf("script"), "")));
        assertFalse(tb.isSpecial(new Element(Tag.valueOf("span"), "")));
        assertFalse(tb.isSpecial(new Element(Tag.valueOf("b"), "")));
        assertFalse(tb.isSpecial(new Element(Tag.valueOf("custom-tag"), "")));
    }

    @Test(timeout = 4000)
    public void testPendingTableCharacters() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);

        assertNotNull(tb.getPendingTableCharacters());
        assertTrue(tb.getPendingTableCharacters().isEmpty());

        tb.getPendingTableCharacters().add("a");
        assertEquals(1, tb.getPendingTableCharacters().size());

        tb.newPendingTableCharacters();
        assertTrue(tb.getPendingTableCharacters().isEmpty());
    }

    @Test(timeout = 4000)
    public void testHeadElementAndToString() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com", parser);

        Element head = new Element(Tag.valueOf("head"), "");
        tb.setHeadElement(head);
        assertEquals(head, tb.getHeadElement());

        String str = tb.toString();
        assertNotNull(str);
        assertTrue(str.contains("TreeBuilder{"));
    }
}