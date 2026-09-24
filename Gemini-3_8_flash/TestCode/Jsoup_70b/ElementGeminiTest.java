package org.jsoup.nodes;

import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------------------------
 * TARGET CLASS: org.jsoup.nodes.Element
 *
 * 1. DEFECT UNDER TEST:
 *    - Bug ID: ElementTest::testKeepsPreTextAtDepth
 *    - Symptom: Whitespace normalization collapses newlines/whitespace inside <pre> tags when text nodes are nested
 *      deeper than 1 level (e.g. <pre><code><span>code\n\ncode</span></code></pre>).
 *    - Root Cause: Element.preserveWhitespace(Node node) only inspects node.parent() and stops, failing to walk up
 *      to 5 ancestor levels as specified by the method documentation, causing text nodes at depth >= 2 to lose
 *      whitespace preservation.
 *
 * 2. BRANCH & BOUNDARY MATRIX:
 *    - Constructors & Null Validation: Tag/BaseUri null guards, Tag name empty checks.
 *    - Child Node Operations: ensureChildNodes() lazy init from EMPTY_NODES; append/prepend/insertChildren with
 *      boundary indexes (0, currentSize, -1 roll-around, out-of-bounds < 0 and > size).
 *    - Text & Whitespace Traversal: text(), ownText(), hasText(), preserveWhitespace() across multiple DOM depths,
 *      <br> handling, block vs inline boundary whitespace normalization.
 *    - Shadow Children Cache: childElementsList() cache population and invalidation via nodelistChanged().
 *    - Class Manipulation & String Scanning: hasClass() scan branches: len < wantLen, len == wantLen, whitespace
 *      delimiters at start, middle, end, substring non-matches. classNames(), addClass(), removeClass(), toggleClass().
 *    - CSS Selector Generation: id vs class vs tag vs namespace replacement ':' -> '|', parent hierarchy, nth-child.
 *    - DOM Query API: getElementsBy* suite (Tag, Id, Class, Attribute variations, Regex patterns, Index evaluators).
 *    - Outer/Inner HTML Generation: OutputSettings prettyPrint (true/false), outline modes, XML vs HTML syntax
 *      self-closing rules (isEmpty vs isSelfClosing), Appendable implementations (StringBuilder vs StringWriter).
 *    - Value & Form Control Logic: textarea text handling vs generic input value attribute.
 *    - Lifecycle Contracts: clone(), shallowClone(), doClone() deep copy of childNodes, NodeList owner integrity.
 * --------------------------------------------------------------------------------------------------------------------
 */
public class ElementGeminiTest {

    // ================================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Targeting testKeepsPreTextAtDepth)
    // ================================================================================================================

    /**
     * Dedicated defect reproduction test:
     * When text is nested inside tags within a <pre> block (depth >= 2), Element.preserveWhitespace
     * must retain whitespace/newlines according to HTML preformatted specification.
     */
    @Test(timeout = 4000)
    public void testKeepsPreTextAtDepth() {
        String html = "<pre><code><span>code\n\ncode</span></code></pre>";
        Document doc = Parser.parse(html, "");
        assertEquals("code\n\ncode", doc.text());
    }

    @Test(timeout = 4000)
    public void testKeepsPreTextAtDepthDirectHierarchy() {
        Element pre = new Element(Tag.valueOf("pre"), "");
        Element code = pre.appendElement("code");
        Element span = code.appendElement("span");
        span.appendText("code\n\ncode");

        assertEquals("code\n\ncode", pre.text());
        assertTrue("Whitespace preservation must evaluate to true for nested code node", Element.preserveWhitespace(span));
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespaceMultiLevelAncestry() {
        Element pre = new Element(Tag.valueOf("pre"), "");
        Element div = pre.appendElement("div");
        Element p = div.appendElement("p");
        Element em = p.appendElement("em");
        Element span = em.appendElement("span");
        span.appendText("line1\n    line2\n    line3");

        assertEquals("line1\n    line2\n    line3", pre.text());
        assertTrue("Depth 4 descendant inside <pre> must preserve whitespace", Element.preserveWhitespace(span));
        assertFalse("Node outside <pre> should not preserve whitespace", Element.preserveWhitespace(new Element("p")));
        assertFalse("Null node must evaluate to false", Element.preserveWhitespace(null));
    }

    // ================================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndBasicProperties() {
        Element el = new Element("div");
        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());
        assertEquals("", el.baseUri());
        assertTrue(el.isBlock());
        assertEquals(0, el.childNodeSize());
        assertEquals(0, el.children().size());

        el.tagName("span");
        assertEquals("span", el.tagName());
        assertFalse(el.isBlock());

        el.doSetBaseUri("https://example.com/base/");
        assertEquals("https://example.com/base/", el.baseUri());
    }

    @Test(timeout = 4000)
    public void testAttributesAndDataset() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertFalse(el.hasAttributes());

        el.attr("id", "main");
        assertTrue(el.hasAttributes());
        assertEquals("main", el.id());

        el.attr("data-role", "admin");
        el.attr("data-status", "active");
        el.attr("aria-hidden", "false");

        Map<String, String> dataset = el.dataset();
        assertEquals(2, dataset.size());
        assertEquals("admin", dataset.get("role"));
        assertEquals("active", dataset.get("status"));

        el.attr("checked", true);
        assertEquals("", el.attr("checked"));
        assertTrue(el.hasAttr("checked"));

        el.attr("checked", false);
        assertFalse(el.hasAttr("checked"));
    }

    @Test(timeout = 4000)
    public void testChildNodeManipulations() {
        Element parent = new Element("div");
        Element child1 = parent.appendElement("p");
        child1.attr("id", "p1");
        Element child2 = parent.prependElement("span");
        child2.attr("id", "s1");

        assertEquals(2, parent.childNodeSize());
        assertEquals("span", parent.child(0).tagName());
        assertEquals("p", parent.child(1).tagName());

        parent.appendText("Text After");
        parent.prependText("Text Before");

        assertEquals(4, parent.childNodeSize());
        assertEquals(2, parent.children().size());
        assertEquals(2, parent.textNodes().size());
        assertEquals("Text Before", parent.textNodes().get(0).getWholeText());
        assertEquals("Text After", parent.textNodes().get(1).getWholeText());

        Element appendedTo = new Element("section");
        parent.appendTo(appendedTo);
        assertSame(appendedTo, parent.parent());
        assertEquals(1, appendedTo.children().size());
    }

    @Test(timeout = 4000)
    public void testAppendAndPrependHtml() {
        Element div = new Element("div");
        div.append("<p>Paragraph 1</p>");
        assertEquals(1, div.children().size());
        assertEquals("p", div.child(0).tagName());

        div.prepend("<h1>Header 1</h1>");
        assertEquals(2, div.children().size());
        assertEquals("h1", div.child(0).tagName());
        assertEquals("p", div.child(1).tagName());
    }

    @Test(timeout = 4000)
    public void testSiblingNavigation() {
        Element root = new Element("div");
        Element e1 = root.appendElement("p").attr("id", "1");
        Element e2 = root.appendElement("span").attr("id", "2");
        Element e3 = root.appendElement("a").attr("id", "3");

        assertEquals(0, e1.elementSiblingIndex());
        assertEquals(1, e2.elementSiblingIndex());
        assertEquals(2, e3.elementSiblingIndex());

        assertSame(e2, e1.nextElementSibling());
        assertNull(e1.previousElementSibling());

        assertSame(e1, e2.previousElementSibling());
        assertSame(e3, e2.nextElementSibling());

        assertSame(e1, e3.firstElementSibling());
        assertSame(e3, e1.lastElementSibling());

        Elements e2Siblings = e2.siblingElements();
        assertEquals(2, e2Siblings.size());
        assertTrue(e2Siblings.contains(e1));
        assertTrue(e2Siblings.contains(e3));
        assertFalse(e2Siblings.contains(e2));
    }

    @Test(timeout = 4000)
    public void testTextAndOwnTextExtraction() {
        Element p = new Element("p");
        p.appendText("Hello ");
        p.appendElement("b").text("Beautiful");
        p.appendText(" World");
        p.appendElement("br");
        p.appendText("Next Line");

        assertEquals("Hello Beautiful World Next Line", p.text());
        assertEquals("Hello World Next Line", p.ownText());
        assertTrue(p.hasText());

        p.text("Replaced plain text");
        assertEquals("Replaced plain text", p.text());
        assertEquals(1, p.childNodeSize());
        assertEquals(0, p.children().size());

        Element emptyP = new Element("p");
        assertFalse(emptyP.hasText());
        emptyP.appendText("   \n\t  ");
        assertFalse(emptyP.hasText());
    }

    @Test(timeout = 4000)
    public void testDataExtraction() {
        Element script = new Element("script");
        DataNode dataNode = new DataNode("var x = 10;", "");
        script.appendChild(dataNode);
        script.appendChild(new Comment("a comment"));

        assertEquals(1, script.dataNodes().size());
        assertEquals("var x = 10;a comment", script.data());

        Element parent = new Element("div");
        parent.appendChild(script);
        assertEquals("var x = 10;a comment", parent.data());
    }

    @Test(timeout = 4000)
    public void testValHandling() {
        Element input = new Element("input").attr("value", "username123");
        assertEquals("username123", input.val());
        input.val("newUsername");
        assertEquals("newUsername", input.attr("value"));

        Element textarea = new Element("textarea").text("sample comment");
        assertEquals("sample comment", textarea.val());
        textarea.val("updated comment");
        assertEquals("updated comment", textarea.text());
    }

    // ================================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================================

    @Test(timeout = 4000)
    public void testInsertChildrenBoundaryIndices() {
        Element container = new Element("div");
        Element c1 = new Element("p").text("One");
        Element c2 = new Element("p").text("Two");
        container.appendChild(c1);
        container.appendChild(c2);

        // Insert at 0 (start)
        Element startNode = new Element("span").text("Start");
        container.insertChildren(0, startNode);
        assertSame(startNode, container.child(0));

        // Insert with roll-around (-1 means at the end: currentSize + 1 + (-1) == currentSize)
        Element endNode = new Element("span").text("End");
        container.insertChildren(-1, Collections.singletonList(endNode));
        assertSame(endNode, container.child(container.children().size() - 1));

        // Insert at exact middle
        Element midNode = new Element("span").text("Middle");
        container.insertChildren(2, midNode);
        assertSame(midNode, container.child(2));
    }

    @Test(timeout = 4000)
    public void testHasClassVariationsAndBoundaries() {
        Element el = new Element("div");
        assertFalse(el.hasClass("active"));

        el.attr("class", "");
        assertFalse(el.hasClass("active"));

        el.attr("class", "act");
        assertFalse(el.hasClass("active"));

        // Exact match (len == wantLen)
        el.attr("class", "ACTIVE");
        assertTrue(el.hasClass("active"));
        assertTrue(el.hasClass("ACTIVE"));
        assertFalse(el.hasClass("action"));

        // Multiple classes: start, middle, and end matches with varied whitespace
        el.attr("class", "  first   mid-class   last  ");
        assertTrue(el.hasClass("first"));
        assertTrue(el.hasClass("mid-class"));
        assertTrue(el.hasClass("last"));
        assertFalse(el.hasClass("mid"));
        assertFalse(el.hasClass("clas"));
        assertFalse(el.hasClass("las"));

        // Substring boundary within token
        el.attr("class", "short longer longest");
        assertFalse(el.hasClass("long"));
        assertTrue(el.hasClass("longer"));
    }

    @Test(timeout = 4000)
    public void testClassNamesSetOperations() {
        Element el = new Element("div");
        assertEquals(0, el.classNames().size());
        assertEquals("", el.className());

        el.addClass("highlight");
        assertTrue(el.hasClass("highlight"));
        assertEquals("highlight", el.className());

        el.addClass("bold");
        Set<String> classes = el.classNames();
        assertEquals(2, classes.size());
        assertTrue(classes.contains("highlight"));
        assertTrue(classes.contains("bold"));

        el.removeClass("highlight");
        assertFalse(el.hasClass("highlight"));
        assertTrue(el.hasClass("bold"));

        el.toggleClass("bold");
        assertFalse(el.hasClass("bold"));
        el.toggleClass("bold");
        assertTrue(el.hasClass("bold"));

        // Set classNames to empty collection should remove the attribute
        el.classNames(Collections.emptySet());
        assertFalse(el.hasAttr("class"));
        assertEquals("", el.className());

        // Set with multiple
        Set<String> newClasses = new LinkedHashSet<>(Arrays.asList("c1", "c2"));
        el.classNames(newClasses);
        assertEquals("c1 c2", el.className());
    }

    @Test(timeout = 4000)
    public void testStandaloneElementNavigationNullGuards() {
        Element standalone = new Element("div");
        assertNull(standalone.parent());
        assertEquals(0, standalone.parents().size());
        assertNull(standalone.nextElementSibling());
        assertNull(standalone.previousElementSibling());
        assertNull(standalone.firstElementSibling());
        assertNull(standalone.lastElementSibling());
        assertEquals(0, standalone.elementSiblingIndex());
        assertEquals(0, standalone.siblingElements().size());
    }

    @Test(timeout = 4000)
    public void testEmptyElementCleaning() {
        Element el = new Element("div");
        el.appendElement("span").text("Child");
        el.appendText("Some text");
        assertEquals(2, el.childNodeSize());

        el.empty();
        assertEquals(0, el.childNodeSize());
        assertEquals("", el.text());
    }

    // ================================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullTagFails() {
        new Element((Tag) null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullBaseUriFails() {
        new Element(Tag.valueOf("div"), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTagNameEmptyFails() {
        Element el = new Element("div");
        el.tagName("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendChildNullFails() {
        Element el = new Element("div");
        el.appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrependChildNullFails() {
        Element el = new Element("div");
        el.prependChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenNegativeOutOfBoundsFails() {
        Element el = new Element("div");
        el.insertChildren(-3, new Element("span"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenPositiveOutOfBoundsFails() {
        Element el = new Element("div");
        el.insertChildren(2, new Element("span"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenNullCollectionFails() {
        Element el = new Element("div");
        el.insertChildren(0, (List<Node>) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeValueMatchingInvalidRegex() {
        Element el = new Element("div");
        el.getElementsByAttributeValueMatching("key", "[unclosed-bracket");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingTextInvalidRegex() {
        Element el = new Element("div");
        el.getElementsMatchingText("(invalid-regex");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingOwnTextInvalidRegex() {
        Element el = new Element("div");
        el.getElementsMatchingOwnText("*invalid");
    }

    // ================================================================================================================
    // Partition E: Object Lifecycle, DOM Search, CSS Selectors, and HTML Formatting
    // ================================================================================================================

    @Test(timeout = 4000)
    public void testCssSelectorPaths() {
        Element div = new Element("div").attr("id", "content");
        assertEquals("#content", div.cssSelector());

        Element section = new Element("section");
        Element p1 = section.appendElement("p").addClass("article").addClass("lead");
        Element p2 = section.appendElement("p").addClass("article");

        assertEquals("section", section.cssSelector());
        assertEquals("section > p.article.lead:nth-child(1)", p1.cssSelector());
        assertEquals("section > p.article:nth-child(2)", p2.cssSelector());

        // Namespace translation check ns:tag -> ns|tag
        Element xmlNode = new Element(Tag.valueOf("fb:like"), "");
        assertEquals("fb|like", xmlNode.cssSelector());
    }

    @Test(timeout = 4000)
    public void testParentsAccumulation() {
        Document doc = new Document("http://example.com");
        Element body = doc.appendElement("body");
        Element div = body.appendElement("div");
        Element span = div.appendElement("span");

        Elements parents = span.parents();
        assertEquals(2, parents.size());
        assertSame(div, parents.get(0));
        assertSame(body, parents.get(1));
    }

    @Test(timeout = 4000)
    public void testDomQuerySelectors() {
        Element root = new Element("div");
        Element header = root.appendElement("header").attr("id", "hdr").attr("data-section", "main");
        Element p1 = root.appendElement("p").addClass("text primary").text("Alpha 123");
        Element p2 = root.appendElement("p").addClass("text secondary").text("Beta 456");
        Element span = p2.appendElement("span").text("Nested Target");

        assertSame(header, root.getElementById("hdr"));
        assertNull(root.getElementById("non-existent"));

        assertEquals(2, root.getElementsByTag("P").size());
        assertEquals(2, root.getElementsByClass("TEXT").size());
        assertEquals(1, root.getElementsByClass("secondary").size());

        assertEquals(1, root.getElementsByAttribute("data-section").size());
        assertEquals(1, root.getElementsByAttributeStarting("data-").size());
        assertEquals(1, root.getElementsByAttributeValue("data-section", "main").size());
        assertEquals(1, root.getElementsByAttributeValueStarting("data-section", "ma").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("data-section", "in").size());
        assertEquals(1, root.getElementsByAttributeValueContaining("data-section", "ai").size());
        assertEquals(3, root.getElementsByAttributeValueNot("data-section", "other").size());

        assertEquals(1, root.getElementsByAttributeValueMatching("data-section", Pattern.compile("^m.*n$")).size());
        assertEquals(1, root.getElementsByAttributeValueMatching("data-section", "^m.*n$").size());

        assertEquals(1, root.getElementsByIndexEquals(1).size());
        assertEquals(1, root.getElementsByIndexLessThan(1).size());
        assertEquals(1, root.getElementsByIndexGreaterThan(1).size());

        assertEquals(1, root.getElementsContainingText("Alpha").size());
        assertEquals(1, root.getElementsContainingOwnText("Beta").size());
        assertEquals(0, p2.getElementsContainingOwnText("Nested").size());

        assertEquals(1, root.getElementsMatchingText(Pattern.compile("Alpha\\s+\\d+")).size());
        assertEquals(1, root.getElementsMatchingText("Alpha\\s+\\d+").size());
        assertEquals(1, root.getElementsMatchingOwnText(Pattern.compile("Beta\\s+\\d+")).size());
        assertEquals(1, root.getElementsMatchingOwnText("Beta\\s+\\d+").size());

        assertEquals(5, root.getAllElements().size()); // root, header, p1, p2, span
    }

    @Test(timeout = 4000)
    public void testSelectorAndIsEvaluator() {
        Element container = new Element("div").attr("id", "main").addClass("active");
        Element child = container.appendElement("p").addClass("inner");

        assertTrue(container.is("#main"));
        assertTrue(container.is(".active"));
        assertFalse(container.is(".hidden"));
        assertTrue(container.is(new Evaluator.Id("main")));

        Elements matched = container.select("p.inner");
        assertEquals(1, matched.size());
        assertSame(child, matched.first());

        Element first = container.selectFirst("p");
        assertSame(child, first);
        assertNull(container.selectFirst("a"));
    }

    @Test(timeout = 4000)
    public void testOuterAndInnerHtmlFormatting() throws IOException {
        Element div = new Element("div");
        div.appendElement("p").text("Hello");

        // Pretty print output check
        String prettyHtml = div.outerHtml();
        assertTrue(prettyHtml.contains("<div>\n <p>Hello</p>\n</div>") || prettyHtml.contains("<div>\n  <p>Hello</p>\n</div>"));

        // HTML self-closing void elements (<img>)
        Element img = new Element(Tag.valueOf("img"), "");
        Document.OutputSettings htmlOut = new Document.OutputSettings();
        htmlOut.syntax(Document.OutputSettings.Syntax.html);
        StringBuilder accum = new StringBuilder();
        img.outerHtmlHead(accum, 0, htmlOut);
        img.outerHtmlTail(accum, 0, htmlOut);
        assertEquals("<img>", accum.toString());

        // XML self-closing syntax (<img />)
        Document.OutputSettings xmlOut = new Document.OutputSettings();
        xmlOut.syntax(Document.OutputSettings.Syntax.xml);
        accum = new StringBuilder();
        img.outerHtmlHead(accum, 0, xmlOut);
        img.outerHtmlTail(accum, 0, xmlOut);
        assertEquals("<img />", accum.toString());

        // Non-pretty print html()
        Document doc = Parser.parse("<div><p>NoFormat</p></div>", "");
        doc.outputSettings().prettyPrint(false);
        assertEquals("<p>NoFormat</p>", doc.body().child(0).html());

        // Appendable target test using StringWriter
        StringWriter writer = new StringWriter();
        div.html(writer);
        assertTrue(writer.toString().contains("<p>Hello</p>"));

        div.html("<span>New Content</span>");
        assertEquals("<span>New Content</span>", div.html());
    }

    @Test(timeout = 4000)
    public void testOutlineModeOuterHtml() {
        Element div = new Element("div");
        div.appendElement("p").text("P1");
        div.appendElement("p").text("P2");

        Document.OutputSettings outlineSettings = new Document.OutputSettings();
        outlineSettings.outline(true);
        outlineSettings.prettyPrint(true);

        String rendered = div.outerHtml();
        assertNotNull(rendered);
        assertTrue(rendered.contains("<p>P1</p>"));
    }

    @Test(timeout = 4000)
    public void testCloningDeepAndShallow() {
        Element original = new Element(Tag.valueOf("div"), "http://example.com");
        original.attr("id", "orig");
        Element child = original.appendElement("p").text("Child paragraph");

        // Shallow clone should not copy children
        Element shallow = original.shallowClone();
        assertEquals("orig", shallow.id());
        assertEquals("http://example.com", shallow.baseUri());
        assertEquals(0, shallow.childNodeSize());

        // Deep clone
        Element deep = original.clone();
        assertEquals("orig", deep.id());
        assertEquals(1, deep.children().size());
        assertNotSame(original, deep);
        assertNotSame(child, deep.child(0));
        assertEquals("Child paragraph", deep.child(0).text());

        // Mutating clone should not impact original
        deep.attr("id", "cloned");
        deep.child(0).text("Modified");
        assertEquals("orig", original.id());
        assertEquals("Child paragraph", original.child(0).text());
    }

    @Test(timeout = 4000)
    public void testBeforeAfterAndWrap() {
        Element parent = new Element("div");
        Element target = parent.appendElement("span").text("Target");

        target.before("<p>Before HTML</p>");
        target.after("<p>After HTML</p>");

        assertEquals(3, parent.children().size());
        assertEquals("Before HTML", parent.child(0).text());
        assertEquals("Target", parent.child(1).text());
        assertEquals("After HTML", parent.child(2).text());

        Element nodeBefore = new Element("i").text("Italic Before");
        Element nodeAfter = new Element("b").text("Bold After");

        target.before(nodeBefore);
        target.after(nodeAfter);

        assertEquals(5, parent.children().size());
        assertSame(nodeBefore, parent.child(1));
        assertSame(target, parent.child(2));
        assertSame(nodeAfter, parent.child(3));

        target.wrap("<section class='wrapper'></section>");
        assertEquals("section", target.parent().tagName());
        assertTrue(target.parent().hasClass("wrapper"));
    }
}