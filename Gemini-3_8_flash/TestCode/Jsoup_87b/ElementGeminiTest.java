package org.jsoup.nodes;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: org.jsoup.nodes.Element
 *
 * DEFECT TARGET (from Defects4J):
 * - Defect: Preserved case tags (e.g. <A>) failed to obey HTML element nesting rules such that links
 *   could illegally nest (<A> ONE <A> Two </A> was not closing the first anchor).
 *   Root Cause / Manifestation: Preserving case in tag settings affected tag-based formatting/nesting
 *   decisions when parsing fragments and documents.
 *   Target Test: testPreservedCaseLinksCantNest() asserting doc.body().html() on preserved-case parse.
 *
 * BRANCH & EQUIVALENCE PARTITIONS:
 * - Partition A: Core DOM & Hierarchy Operations
 *   * appendChild, prependChild, appendTo, insertChildren (index >= 0, negative roll-around index, out of bounds).
 *   * child(i), children(), textNodes(), dataNodes(), ensureChildNodes(), shadowChildrenRef cache & invalidation.
 *   * parents() accumulation terminating at #root or null.
 *   * Sibling navigation: siblingElements, nextElementSibling, previousElementSibling,
 *     nextElementSiblings, previousElementSiblings, firstElementSibling, lastElementSibling,
 *     elementSiblingIndex when element is orphan, lone child, first, middle, or last.
 * - Partition B: Selector & Attribute Search Methods
 *   * select, selectFirst, is(Evaluator), is(String query).
 *   * getElementsByTag, getElementById, getElementsByClass, getElementsByAttribute,
 *     getElementsByAttributeStarting, getElementsByAttributeValue, getElementsByAttributeValueNot,
 *     getElementsByAttributeValueStarting, getElementsByAttributeValueEnding,
 *     getElementsByAttributeValueContaining, getElementsByAttributeValueMatching (Pattern & String).
 *   * getElementsByIndexLessThan, getElementsByIndexGreaterThan, getElementsByIndexEquals.
 *   * getElementsContainingText, getElementsContainingOwnText, getElementsMatchingText,
 *     getElementsMatchingOwnText, getAllElements.
 * - Partition C: Text & Data Extraction
 *   * text() vs ownText() vs wholeText() with block elements, inline elements, <br>, whitespace collapse.
 *   * preserveWhitespace() testing <pre> tag up to 6 levels deep vs > 6 levels deep.
 *   * data() with DataNode, Comment, child Element, CDataNode.
 *   * hasText() with empty, whitespace-only, TextNode, nested Element with text.
 * - Partition D: Class Manipulation & Performance Paths
 *   * hasClass: len == 0, len < wantLen, len == wantLen (exact/case-insensitive),
 *     inClass transitions, token start/end matching, intermediate whitespace, trailing match, no match.
 *   * addClass, removeClass, toggleClass, classNames(Set).
 * - Partition E: Form Value, Output Formatting & Cloning
 *   * val() and val(String) on <input> vs <textarea>.
 *   * cssSelector() with ID, classes, tag namespace ':', nth-child with multiple matching siblings,
 *     orphan element, parent as Document.
 *   * outerHtmlHead / outerHtmlTail: prettyPrint on/off, block tags, outline mode,
 *     self-closing empty tags in HTML vs XML syntax.
 *   * clone(), shallowClone(), doClone().
 * ----------------------------------------------------------------------------------------------------
 */

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNest() {
        // Direct replication of the defect: links cannot nest even when case is preserved
        String html = "<A> ONE <A> Two </A>";
        Document doc = Jsoup.parse(html, "", Parser.htmlParser().settings(ParseSettings.preserveCase));
        Element body = doc.body();
        assertEquals("<A> ONE </A> <A> Two </A>", StringUtil.normaliseWhitespace(body.html()));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Hierarchy / Sibling Navigation
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndBaseUri() {
        Element el1 = new Element("div");
        assertEquals("div", el1.tagName());
        assertEquals("", el1.baseUri());
        assertTrue(el1.attributes().isEmpty());

        Attributes attrs = new Attributes();
        attrs.put("key", "val");
        Element el2 = new Element(Tag.valueOf("span"), "https://example.com/", attrs);
        assertEquals("span", el2.tagName());
        assertEquals("https://example.com/", el2.baseUri());
        assertEquals("val", el2.attr("key"));

        Element el3 = new Element(Tag.valueOf("p"), "https://example.com/");
        assertEquals("p", el3.tagName());
        assertEquals("https://example.com/", el3.baseUri());
        assertFalse(el3.hasAttributes());

        el3.setBaseUri("https://updated.com/");
        assertEquals("https://updated.com/", el3.baseUri());
    }

    @Test(timeout = 4000)
    public void testChildNodesAndCacheInvalidation() {
        Element parent = new Element("div");
        assertEquals(0, parent.childNodeSize());
        assertEquals(0, parent.children().size());

        Element child1 = parent.appendElement("span");
        TextNode textNode = new TextNode("hello");
        parent.appendChild(textNode);
        Element child2 = parent.appendElement("p");

        assertEquals(3, parent.childNodeSize());
        List<Element> children = parent.children();
        assertEquals(2, children.size());
        assertSame(child1, children.get(0));
        assertSame(child2, children.get(1));
        assertSame(child1, parent.child(0));
        assertSame(child2, parent.child(1));

        List<TextNode> textNodes = parent.textNodes();
        assertEquals(1, textNodes.size());
        assertSame(textNode, textNodes.get(0));

        // Invalidate cache via empty()
        parent.empty();
        assertEquals(0, parent.childNodeSize());
        assertEquals(0, parent.children().size());
        assertEquals(0, parent.textNodes().size());
    }

    @Test(timeout = 4000)
    public void testParentsAccumulation() {
        Document doc = Jsoup.parse("<html><body><div id='grand'><div id='parent'><p id='child'>Text</p></div></div></body></html>");
        Element child = doc.getElementById("child");
        Elements parents = child.parents();

        assertEquals(4, parents.size());
        assertEquals("parent", parents.get(0).id());
        assertEquals("grand", parents.get(1).id());
        assertEquals("body", parents.get(2).tagName());
        assertEquals("html", parents.get(3).tagName());

        // Root/Document has no parent in parents()
        assertEquals(0, doc.parents().size());
    }

    @Test(timeout = 4000)
    public void testInsertChildrenVariations() {
        Element parent = new Element("div");
        Element c1 = new Element("span");
        Element c2 = new Element("em");
        parent.appendChild(c1);

        // Insert at 0 (prepend)
        parent.insertChildren(0, Collections.singletonList(c2));
        assertEquals(2, parent.childNodeSize());
        assertSame(c2, parent.child(0));
        assertSame(c1, parent.child(1));

        // Insert using negative roll-around (-1 inserts at currentSize + 1 - 1 = currentSize)
        Element c3 = new Element("b");
        parent.insertChildren(-1, c3);
        assertEquals(3, parent.childNodeSize());
        assertSame(c3, parent.child(2));

        // Insert array at specific index
        Element c4 = new Element("i");
        Element c5 = new Element("u");
        parent.insertChildren(1, c4, c5);
        assertEquals(5, parent.childNodeSize());
        assertSame(c4, parent.child(1));
        assertSame(c5, parent.child(2));
    }

    @Test(timeout = 4000)
    public void testPrependAndAppendMethods() {
        Element root = new Element("div");
        Element first = root.appendElement("p").text("first");
        Element prepended = root.prependElement("span").text("prepended");
        assertSame(prepended, root.child(0));
        assertSame(first, root.child(1));

        root.appendText(" appendedText ");
        root.prependText(" prependedText ");
        assertTrue(root.childNode(0) instanceof TextNode);
        assertTrue(root.childNode(root.childNodeSize() - 1) instanceof TextNode);

        Element appendedFromHtml = root.append("<b>bold</b>");
        assertSame(root, appendedFromHtml);
        assertEquals("b", ((Element) root.childNode(root.childNodeSize() - 1)).tagName());

        Element prependedFromHtml = root.prepend("<i>italic</i>");
        assertSame(root, prependedFromHtml);
        assertEquals("i", ((Element) root.childNode(0)).tagName());
    }

    @Test(timeout = 4000)
    public void testSiblingsNavigation() {
        Document doc = Jsoup.parse("<div id='root'><p id='p1'>1</p><p id='p2'>2</p><p id='p3'>3</p></div>");
        Element p1 = doc.getElementById("p1");
        Element p2 = doc.getElementById("p2");
        Element p3 = doc.getElementById("p3");

        assertEquals(0, p1.elementSiblingIndex());
        assertEquals(1, p2.elementSiblingIndex());
        assertEquals(2, p3.elementSiblingIndex());

        assertSame(p2, p1.nextElementSibling());
        assertNull(p3.nextElementSibling());

        assertSame(p2, p3.previousElementSibling());
        assertNull(p1.previousElementSibling());

        assertSame(p1, p2.firstElementSibling());
        assertSame(p3, p2.lastElementSibling());

        Elements p2NextSiblings = p2.nextElementSiblings();
        assertEquals(1, p2NextSiblings.size());
        assertSame(p3, p2NextSiblings.get(0));

        Elements p2PrevSiblings = p2.previousElementSiblings();
        assertEquals(1, p2PrevSiblings.size());
        assertSame(p1, p2PrevSiblings.get(0));

        Elements p2AllSiblings = p2.siblingElements();
        assertEquals(2, p2AllSiblings.size());
        assertTrue(p2AllSiblings.contains(p1));
        assertTrue(p2AllSiblings.contains(p3));
        assertFalse(p2AllSiblings.contains(p2));

        // Standalone element without parent
        Element orphan = new Element("div");
        assertEquals(0, orphan.elementSiblingIndex());
        assertNull(orphan.nextElementSibling());
        assertNull(orphan.previousElementSibling());
        assertEquals(0, orphan.siblingElements().size());
        assertEquals(0, orphan.nextElementSiblings().size());
        assertEquals(0, orphan.previousElementSiblings().size());
    }

    @Test(timeout = 4000)
    public void testBeforeAndAfterInsertions() {
        Document doc = Jsoup.parse("<div><p id='mid'>mid</p></div>");
        Element mid = doc.getElementById("mid");

        mid.before("<span id='beforeStr'>bStr</span>");
        mid.before(new Element("span").attr("id", "beforeNode"));
        mid.after("<span id='afterStr'>aStr</span>");
        mid.after(new Element("span").attr("id", "afterNode"));

        assertNotNull(doc.getElementById("beforeStr"));
        assertNotNull(doc.getElementById("beforeNode"));
        assertNotNull(doc.getElementById("afterStr"));
        assertNotNull(doc.getElementById("afterNode"));

        mid.wrap("<div class='wrapper'></div>");
        Element wrapper = mid.parent();
        assertEquals("wrapper", wrapper.className());
    }

    // =========================================================================
    // Partition B: Selectors, Queries and Evaluations
    // =========================================================================

    @Test(timeout = 4000)
    public void testSelectAndIs() {
        Document doc = Jsoup.parse("<div id='main' class='content'><p class='sub' data-kind='item'>Item 1</p><p class='sub'>Item 2</p></div>");
        Element main = doc.getElementById("main");

        assertTrue(main.is("#main"));
        assertTrue(main.is(".content"));
        assertTrue(main.is(new Evaluator.Tag("div")));
        assertFalse(main.is("span"));

        Elements subs = main.select(".sub");
        assertEquals(2, subs.size());

        Element firstSub = main.selectFirst(".sub");
        assertNotNull(firstSub);
        assertEquals("Item 1", firstSub.text());

        assertNull(main.selectFirst(".nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetElementsByMethods() {
        Document doc = Jsoup.parse("<div id='d1' class='A b C' data-type='special' title='hello world'>" +
                "<p class='b' title='hello'>P1</p>" +
                "<p class='C' title='world'>P2</p>" +
                "<span class='last' val='123abc456'>S1</span>" +
                "</div>");

        assertEquals(2, doc.getElementsByTag("p").size());
        assertNotNull(doc.getElementById("d1"));
        assertEquals(2, doc.getElementsByClass("b").size());
        assertEquals(1, doc.getElementsByAttribute("data-type").size());
        assertEquals(1, doc.getElementsByAttributeStarting("data-").size());

        assertEquals(1, doc.getElementsByAttributeValue("title", "hello").size());
        assertEquals(3, doc.getElementsByAttributeValueNot("title", "hello").size()); // d1, P2, S1
        assertEquals(2, doc.getElementsByAttributeValueStarting("title", "hel").size());
        assertEquals(2, doc.getElementsByAttributeValueEnding("title", "rld").size());
        assertEquals(2, doc.getElementsByAttributeValueContaining("title", "ell").size());

        assertEquals(1, doc.getElementsByAttributeValueMatching("val", Pattern.compile("\\d+abc\\d+")).size());
        assertEquals(1, doc.getElementsByAttributeValueMatching("val", "^123.*456$").size());

        Element d1 = doc.getElementById("d1");
        assertEquals(1, d1.getElementsByIndexLessThan(1).size());
        assertEquals(1, d1.getElementsByIndexGreaterThan(1).size());
        assertEquals(1, d1.getElementsByIndexEquals(1).size());

        assertEquals(1, doc.getElementsContainingText("P1").size());
        assertEquals(1, doc.getElementsContainingOwnText("P1").size());

        assertEquals(1, doc.getElementsMatchingText(Pattern.compile("P[0-9]")).size());
        assertEquals(1, doc.getElementsMatchingText("P[0-9]").size());
        assertEquals(1, doc.getElementsMatchingOwnText(Pattern.compile("^P2$")).size());
        assertEquals(1, doc.getElementsMatchingOwnText("^P2$").size());

        assertTrue(doc.getAllElements().size() >= 5);
    }

    // =========================================================================
    // Partition C: Text, Data, Whitespace & Normalization
    // =========================================================================

    @Test(timeout = 4000)
    public void testTextAndOwnTextNormalization() {
        Document doc = Jsoup.parse("<div>Hello <b>world</b>! <br>New line <div>Block text</div>After block</div>");
        Element div = doc.selectFirst("div");

        assertEquals("Hello world! New line Block text After block", div.text());
        assertEquals("Hello ! New line After block", div.ownText());
        assertTrue(div.hasText());

        Element emptyDiv = new Element("div");
        assertFalse(emptyDiv.hasText());
        assertEquals("", emptyDiv.text());
        assertEquals("", emptyDiv.ownText());

        emptyDiv.text("Just text");
        assertEquals("Just text", emptyDiv.text());
        assertTrue(emptyDiv.hasText());
    }

    @Test(timeout = 4000)
    public void testWholeTextAndPreserveWhitespace() {
        Document doc = Jsoup.parse("<pre>  Line 1\n  Line 2  </pre><div>  Line 1\n  Line 2  </div>");
        Element pre = doc.selectFirst("pre");
        Element div = doc.selectFirst("div");

        assertEquals("  Line 1\n  Line 2  ", pre.text());
        assertEquals("  Line 1\n  Line 2  ", pre.wholeText());
        assertEquals("Line 1 Line 2", div.text());

        // Test whitespace preservation beyond 6 levels
        Document deepDoc = Jsoup.parse("<pre><a><b><c><d><e><f><g>   Deep   </g></f></e></d></c></b></a></pre>");
        Element deepG = deepDoc.selectFirst("g");
        // Deep tag > 6 levels from <pre> does not preserve whitespace
        assertFalse(Element.preserveWhitespace(deepG));

        Element shallowD = deepDoc.selectFirst("d");
        assertTrue(Element.preserveWhitespace(shallowD));
    }

    @Test(timeout = 4000)
    public void testDataAndDataNodes() {
        Document doc = Jsoup.parse("<script type='text/javascript'>var a = 1; /* comment */</script>" +
                "<style><!-- body { color: red; } --></style>" +
                "<div><![CDATA[cdata section]]></div>");

        Element script = doc.selectFirst("script");
        assertEquals("var a = 1; /* comment */", script.data());
        assertEquals(1, script.dataNodes().size());

        Element style = doc.selectFirst("style");
        assertEquals(" body { color: red; } ", style.data());

        Element divWithCdata = doc.selectFirst("div");
        assertEquals("cdata section", divWithCdata.data());

        // Element containing mixed comments and data nodes
        Element container = new Element("div");
        container.appendChild(new DataNode("data1"));
        container.appendChild(new Comment("comment1"));
        container.appendChild(new CDataNode("cdata1"));
        assertEquals("data1comment1cdata1", container.data());
    }

    // =========================================================================
    // Partition D: Class Attributes, Dataset & Form Values
    // =========================================================================

    @Test(timeout = 4000)
    public void testHasClassAndClassNamesVariations() {
        Element el = new Element("div");
        assertFalse(el.hasClass("active"));

        el.attr("class", "active");
        assertTrue(el.hasClass("active"));
        assertTrue(el.hasClass("ACTIVE"));
        assertFalse(el.hasClass("act"));
        assertFalse(el.hasClass("activeLonger"));

        el.attr("class", "  first   active   last  ");
        assertTrue(el.hasClass("first"));
        assertTrue(el.hasClass("active"));
        assertTrue(el.hasClass("last"));
        assertFalse(el.hasClass("middle"));

        el.addClass("newClass");
        assertTrue(el.hasClass("newClass"));

        el.removeClass("active");
        assertFalse(el.hasClass("active"));

        el.toggleClass("toggled");
        assertTrue(el.hasClass("toggled"));
        el.toggleClass("toggled");
        assertFalse(el.hasClass("toggled"));

        Set<String> customClasses = new LinkedHashSet<>(Arrays.asList("c1", "c2"));
        el.classNames(customClasses);
        assertEquals("c1 c2", el.className());

        el.classNames(Collections.emptySet());
        assertEquals("", el.className());
        assertFalse(el.hasAttributes());
    }

    @Test(timeout = 4000)
    public void testDatasetAndAttributes() {
        Element el = new Element("div");
        el.attr("data-id", "123");
        el.attr("data-user-name", "john");
        el.attr("class", "normal");
        el.attr("disabled", true);

        Map<String, String> dataset = el.dataset();
        assertEquals(2, dataset.size());
        assertEquals("123", dataset.get("id"));
        assertEquals("john", dataset.get("user-name"));

        dataset.put("id", "456");
        assertEquals("456", el.attr("data-id"));

        el.attr("disabled", false);
        assertFalse(el.hasAttr("disabled"));
    }

    @Test(timeout = 4000)
    public void testValMethod() {
        Element input = new Element("input");
        input.val("testVal");
        assertEquals("testVal", input.val());
        assertEquals("testVal", input.attr("value"));

        Element textarea = new Element("textarea");
        textarea.val("areaContent");
        assertEquals("areaContent", textarea.val());
        assertEquals("areaContent", textarea.text());

        Element div = new Element("div");
        div.val("divVal");
        assertEquals("divVal", div.attr("value"));
    }

    // =========================================================================
    // Partition E: Output Formatting, CssSelector & Lifecycle Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCssSelector() {
        Document doc = Jsoup.parse("<html><body><div id='one'><div class='two three'><p>First</p><p>Second</p></div></div></body></html>");
        Element pFirst = doc.select("p").first();
        Element pSecond = doc.select("p").last();

        assertEquals("#one > div.two.three > p:nth-child(1)", pFirst.cssSelector());
        assertEquals("#one > div.two.three > p:nth-child(2)", pSecond.cssSelector());

        Element withId = doc.getElementById("one");
        assertEquals("#one", withId.cssSelector());

        // Namespaced tag
        Element xmlEl = new Element("fb:like");
        assertEquals("fb|like", xmlEl.cssSelector());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlAndSyntaxOptions() throws IOException {
        Element img = new Element("img");
        assertEquals("<img>", img.outerHtml());

        Document.OutputSettings xmlSettings = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);
        StringBuilder sb = new StringBuilder();
        img.outerHtmlHead(sb, 0, xmlSettings);
        img.outerHtmlTail(sb, 0, xmlSettings);
        assertEquals("<img />", sb.toString());

        Element block = new Element("div");
        block.appendChild(new Element("p").text("content"));
        Document.OutputSettings prettyOff = new Document.OutputSettings().prettyPrint(false);
        assertEquals("<div><p>content</p></div>", block.outerHtml().replaceAll("\n", ""));

        block.html("<span>new HTML</span>");
        assertEquals("<span>new HTML</span>", block.html());
    }

    @Test(timeout = 4000)
    public void testCloneAndShallowClone() {
        Element parent = new Element("div");
        parent.attr("key", "val");
        Element child = parent.appendElement("span").text("text");

        Element deepClone = parent.clone();
        assertEquals(parent.outerHtml(), deepClone.outerHtml());
        assertNotSame(parent, deepClone);
        assertNotSame(child, deepClone.child(0));

        Element shallow = parent.shallowClone();
        assertEquals(0, shallow.childNodeSize());
        assertEquals("val", shallow.attr("key"));
        assertEquals("div", shallow.tagName());
    }

    @Test(timeout = 4000)
    public void testTagNameUpdates() {
        Element el = new Element("div");
        el.tagName("span");
        assertEquals("span", el.tagName());
        assertEquals("span", el.nodeName());
        assertEquals(Tag.valueOf("span"), el.tag());
        assertFalse(el.isBlock());
    }

    // =========================================================================
    // Partition F: Boundary Value Analysis & Defensive Guards
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyTagNameThrows() {
        Element el = new Element("div");
        el.tagName("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullTagInConstructorThrows() {
        new Element((Tag) null, "");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullBaseUriInConstructorThrows() {
        new Element(Tag.valueOf("p"), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullAppendChildThrows() {
        Element el = new Element("div");
        el.appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenOutOfBoundsThrows() {
        Element el = new Element("div");
        el.insertChildren(5, new Element("p"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementByEmptyIdThrows() {
        Element el = new Element("div");
        el.getElementById("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidRegexThrows() {
        Element el = new Element("div");
        el.getElementsByAttributeValueMatching("key", "[invalid regex");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testChildIndexOutOfBoundsThrows() {
        Element el = new Element("div");
        el.child(0);
    }
}