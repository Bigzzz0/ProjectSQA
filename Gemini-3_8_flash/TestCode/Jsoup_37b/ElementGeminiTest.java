package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Pattern;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Target Defect:
 *    - ElementTest::testNotPretty -> Element.html() trims the accumulated inner HTML output
 *      unconditionally even when pretty-printing is disabled on OutputSettings. When
 *      prettyPrint == false, leading or trailing whitespace inside an element must be
 *      faithfully preserved without trimming.
 *
 * 2. Decision & Branch Coverage Points:
 *    - Node hierarchy: parent != null, parent == null, accumulateParents root (#root) boundary.
 *    - Child filtering: Element vs TextNode vs DataNode in children(), textNodes(), dataNodes().
 *    - Sibling navigation: index == 0, index > 0, index == size - 1, size <= 1, detached element.
 *    - Child insertion: index == 0, index == -1 (roll-around to end), index < -1, index > size.
 *    - Form element values: textarea (delegates to text()) vs input/other (delegates to attr("value")).
 *    - Class name manipulation: split on whitespace, add, remove, toggle (present/absent), case-insensitive hasClass.
 *    - Whitespace preservation in text(): pre/textarea elements vs standard elements; sibling <br> whitespace.
 *    - Selector evaluators: attribute matching, regex compilation (valid vs syntax error), index comparisons.
 *    - HTML output formatting: pretty-print enabled/disabled, block formatting, self-closing vs container tags.
 *    - Object lifecycle: identity equals contract, hashCode consistency, deep clone classNames detachment.
 */
public class ElementGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTagAndBasicProperties() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("div", el.nodeName());
        assertEquals("div", el.tagName());
        assertTrue(el.isBlock());

        el.tagName("span");
        assertEquals("span", el.tagName());
        assertFalse(el.isBlock());
        assertSame(Tag.valueOf("span"), el.tag());
    }

    @Test(timeout = 4000)
    public void testIdAndAttributes() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.id());

        el.attr("id", "main-header");
        assertEquals("main-header", el.id());

        el.attr("title", "tooltip");
        assertEquals("tooltip", el.attr("title"));
    }

    @Test(timeout = 4000)
    public void testDatasetView() {
        Attributes attrs = new Attributes();
        attrs.put("data-user-id", "12345");
        attrs.put("data-action", "save");
        attrs.put("class", "btn");
        Element el = new Element(Tag.valueOf("button"), "", attrs);

        Map<String, String> data = el.dataset();
        assertEquals(2, data.size());
        assertEquals("12345", data.get("user-id"));
        assertEquals("save", data.get("action"));

        data.put("role", "admin");
        assertEquals("admin", el.attr("data-role"));
    }

    @Test(timeout = 4000)
    public void testParentAndParentsTraversal() {
        Document doc = Jsoup.parse("<html><body><div id='outer'><p id='inner'><span>Text</span></p></div></body></html>");
        Element span = doc.select("span").first();
        assertNotNull(span);

        assertEquals("inner", span.parent().id());

        Elements parents = span.parents();
        assertEquals(4, parents.size());
        assertEquals("p", parents.get(0).tagName());
        assertEquals("div", parents.get(1).tagName());
        assertEquals("body", parents.get(2).tagName());
        assertEquals("html", parents.get(3).tagName());

        // Document/root is excluded from parents()
        for (Element parent : parents) {
            assertNotEquals("#root", parent.tagName());
        }
    }

    @Test(timeout = 4000)
    public void testChildrenTextNodesAndDataNodesFiltering() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(new TextNode("Hello ", ""));
        Element span = div.appendElement("span");
        span.text("World");
        div.appendChild(new DataNode("var code = 1;", ""));
        div.appendChild(new TextNode(" End", ""));

        Elements children = div.children();
        assertEquals(1, children.size());
        assertSame(span, children.get(0));
        assertSame(span, div.child(0));

        List<TextNode> textNodes = div.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Hello ", textNodes.get(0).getWholeText());
        assertEquals(" End", textNodes.get(1).getWholeText());

        List<DataNode> dataNodes = div.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("var code = 1;", dataNodes.get(0).getWholeData());

        assertEquals("var code = 1;", div.data());
    }

    @Test(timeout = 4000)
    public void testAddAndPrependElementsAndText() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element c1 = root.appendElement("p").text("Middle");
        Element c0 = root.prependElement("header").text("Start");
        root.appendText(" AppendedText");
        root.prependText("PrependedText ");

        assertEquals(2, root.children().size());
        assertSame(c0, root.child(0));
        assertSame(c1, root.child(1));
        assertTrue(root.text().startsWith("PrependedText Start"));
        assertTrue(root.text().endsWith("Middle AppendedText"));
    }

    @Test(timeout = 4000)
    public void testAppendAndPrependHtml() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append("<span>One</span>");
        div.prepend("<b>Zero</b>");
        assertEquals("<b>Zero</b><span>One</span>", div.html());
    }

    @Test(timeout = 4000)
    public void testSiblingNavigation() {
        Document doc = Jsoup.parse("<ul><li id='i1'>1</li><li id='i2'>2</li><li id='i3'>3</li></ul>");
        Element i1 = doc.getElementById("i1");
        Element i2 = doc.getElementById("i2");
        Element i3 = doc.getElementById("i3");

        assertEquals(Integer.valueOf(0), i1.elementSiblingIndex());
        assertEquals(Integer.valueOf(1), i2.elementSiblingIndex());
        assertEquals(Integer.valueOf(2), i3.elementSiblingIndex());

        assertNull(i1.previousElementSibling());
        assertSame(i2, i1.nextElementSibling());

        assertSame(i1, i2.previousElementSibling());
        assertSame(i3, i2.nextElementSibling());

        assertSame(i2, i3.previousElementSibling());
        assertNull(i3.nextElementSibling());

        assertSame(i1, i2.firstElementSibling());
        assertSame(i3, i2.lastElementSibling());

        Elements i2Siblings = i2.siblingElements();
        assertEquals(2, i2Siblings.size());
        assertTrue(i2Siblings.contains(i1));
        assertTrue(i2Siblings.contains(i3));
        assertFalse(i2Siblings.contains(i2));
    }

    @Test(timeout = 4000)
    public void testFormValBehavior() {
        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("initial content");
        assertEquals("initial content", textarea.val());
        textarea.val("updated content");
        assertEquals("updated content", textarea.text());

        Element input = new Element(Tag.valueOf("input"), "");
        input.val("submit_btn");
        assertEquals("submit_btn", input.val());
        assertEquals("submit_btn", input.attr("value"));
    }

    @Test(timeout = 4000)
    public void testClassNamesManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.className());
        assertFalse(el.hasClass("active"));

        el.addClass("btn");
        el.addClass("btn-primary");
        assertTrue(el.hasClass("btn"));
        assertTrue(el.hasClass("BTN")); // case-insensitive check
        assertTrue(el.hasClass("btn-primary"));

        el.removeClass("btn");
        assertFalse(el.hasClass("btn"));
        assertTrue(el.hasClass("btn-primary"));

        el.toggleClass("active");
        assertTrue(el.hasClass("active"));
        el.toggleClass("active");
        assertFalse(el.hasClass("active"));

        Set<String> customClasses = new LinkedHashSet<String>();
        customClasses.add("foo");
        customClasses.add("bar");
        el.classNames(customClasses);
        assertEquals("foo bar", el.className());
    }

    @Test(timeout = 4000)
    public void testTextAndOwnTextExtraction() {
        Document doc = Jsoup.parse("<div>Hello <b>World</b>!<br>New <span>Line</span></div>");
        Element div = doc.select("div").first();

        assertEquals("Hello World! New Line", div.text());
        assertEquals("Hello !", div.ownText());
        assertTrue(div.hasText());

        Element emptyDiv = new Element(Tag.valueOf("div"), "");
        assertFalse(emptyDiv.hasText());
        emptyDiv.text("   ");
        assertFalse(emptyDiv.hasText());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespaceInText() {
        Document doc = Jsoup.parse("<pre>  line 1  \n  line 2  </pre>");
        Element pre = doc.select("pre").first();
        assertEquals("  line 1  \n  line 2  ", pre.text());

        Document docNested = Jsoup.parse("<pre><code>  code block  </code></pre>");
        Element code = docNested.select("code").first();
        assertEquals("  code block  ", code.text());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandaloneElementSiblingBoundaries() {
        Element standalone = new Element(Tag.valueOf("p"), "");
        assertEquals(Integer.valueOf(0), standalone.elementSiblingIndex());
        assertNull(standalone.nextElementSibling());
        assertNull(standalone.previousElementSibling());
        assertEquals(0, standalone.siblingElements().size());
    }

    @Test(timeout = 4000)
    public void testSingleChildSiblingBoundaries() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element onlyChild = parent.appendElement("span");

        assertNull(onlyChild.firstElementSibling());
        assertNull(onlyChild.lastElementSibling());
        assertNull(onlyChild.nextElementSibling());
        assertNull(onlyChild.previousElementSibling());
        assertEquals(0, onlyChild.siblingElements().size());
    }

    @Test(timeout = 4000)
    public void testInsertChildrenBoundaryIndices() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element c1 = new Element(Tag.valueOf("p"), "").text("1");
        Element c2 = new Element(Tag.valueOf("p"), "").text("2");
        parent.appendChild(c1);
        parent.appendChild(c2);

        // Insert at start (index 0)
        Element start = new Element(Tag.valueOf("header"), "");
        parent.insertChildren(0, Collections.singletonList(start));
        assertSame(start, parent.child(0));

        // Insert with roll-around -1 (inserts at the end)
        Element end = new Element(Tag.valueOf("footer"), "");
        parent.insertChildren(-1, Collections.singletonList(end));
        assertSame(end, parent.child(parent.children().size() - 1));

        // Empty children collection insert
        parent.insertChildren(1, Collections.<Node>emptyList());
        assertEquals(4, parent.children().size());
    }

    @Test(timeout = 4000)
    public void testEmptyElementAndClear() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("p").text("Content");
        assertEquals(1, div.childNodeSize());

        div.empty();
        assertEquals(0, div.childNodeSize());
        assertEquals(0, div.children().size());
        assertEquals("", div.html());
    }

    @Test(timeout = 4000)
    public void testSelfClosingOuterHtml() {
        Element img = new Element(Tag.valueOf("img"), "");
        img.attr("src", "image.png");
        assertEquals("<img src=\"image.png\" />", img.outerHtml());

        Element div = new Element(Tag.valueOf("div"), "");
        assertEquals("<div></div>", div.outerHtml());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect)
    // =========================================================================

    /**
     * Targets Defects4J known failure: ElementTest::testNotPretty
     * When pretty-printing is disabled on Document.OutputSettings, inner html()
     * must preserve leading/trailing whitespace without calling trim().
     */
    @Test(timeout = 4000)
    public void testNotPretty() {
        Document doc = Jsoup.parse("<div>   \n<p>Hello\n there\n</p></div>");
        doc.outputSettings().prettyPrint(false);
        assertEquals("<html><head></head><body><div>   \n<p>Hello\n there\n</p></div></body></html>", doc.html());

        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals("   \n<p>Hello\n there\n</p>", div.html());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullTagFails() {
        new Element(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyTagNameFails() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullChildFails() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrependNullChildFails() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.prependChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenNullCollectionFails() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.insertChildren(0, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenOutOfBoundsNegativeFails() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.insertChildren(-3, Collections.singletonList(new TextNode("a", "")));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenOutOfBoundsPositiveFails() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.insertChildren(5, Collections.singletonList(new TextNode("a", "")));
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testChildIndexOutOfBoundsFails() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.child(0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeValueMatchingInvalidRegexFails() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttributeValueMatching("key", "[unclosed-regex");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingTextInvalidRegexFails() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingText("[unclosed-regex");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingOwnTextInvalidRegexFails() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingOwnText("[unclosed-regex");
    }

    // =========================================================================
    // Partition E: DOM Search & Evaluator Method Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testElementSelectionMethods() {
        Document doc = Jsoup.parse(
                "<div id='root' class='container'>" +
                "  <p id='p1' class='text active' data-item='first'>Paragraph 1</p>" +
                "  <p id='p2' class='text' data-item='second'>Paragraph 2</p>" +
                "  <span class='label'>Note</span>" +
                "</div>"
        );

        Element root = doc.getElementById("root");
        assertNotNull(root);
        assertEquals("p1", doc.getElementById("p1").id());
        assertNull(doc.getElementById("non-existent"));

        assertEquals(2, root.getElementsByTag("p").size());
        assertEquals(2, root.getElementsByClass("text").size());
        assertEquals(1, root.getElementsByClass("active").size());
        assertEquals(2, root.getElementsByAttribute("data-item").size());
        assertEquals(2, root.getElementsByAttributeStarting("data-").size());

        assertEquals(1, root.getElementsByAttributeValue("data-item", "first").size());
        assertEquals(1, root.getElementsByAttributeValueNot("data-item", "first").size());
        assertEquals(2, root.getElementsByAttributeValueStarting("data-item", "fir").size()
                + root.getElementsByAttributeValueStarting("data-item", "sec").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("data-item", "ond").size());
        assertEquals(2, root.getElementsByAttributeValueContaining("data-item", "ir").size()
                + root.getElementsByAttributeValueContaining("data-item", "eco").size());

        assertEquals(1, root.getElementsByAttributeValueMatching("data-item", Pattern.compile("^fir.*")).size());
        assertEquals(1, root.getElementsByAttributeValueMatching("data-item", "^sec.*").size());

        assertEquals(1, root.getElementsByIndexLessThan(1).size());
        assertEquals(1, root.getElementsByIndexGreaterThan(1).size());
        assertEquals(1, root.getElementsByIndexEquals(1).size());

        assertEquals(1, root.getElementsContainingText("Paragraph 1").size());
        assertEquals(1, root.getElementsContainingOwnText("Paragraph 2").size());

        assertEquals(2, root.getElementsMatchingText(Pattern.compile("Paragraph \\d")).size());
        assertEquals(2, root.getElementsMatchingText("Paragraph \\d").size());

        assertEquals(1, root.getElementsMatchingOwnText(Pattern.compile("^Note$")).size());
        assertEquals(1, root.getElementsMatchingOwnText("^Note$").size());

        assertTrue(root.getAllElements().size() >= 4);
    }

    // =========================================================================
    // Partition F: Object Lifecycle & DOM Mutation Contracts
    // =========================================================================

    @Test(timeout = 4000)
    public void testWrapBeforeAfterManipulations() {
        Document doc = Jsoup.parse("<div id='content'><p>Target</p></div>");
        Element p = doc.select("p").first();

        p.before("<span id='before-str'>BeforeStr</span>");
        p.before(new Element(Tag.valueOf("span"), "").attr("id", "before-node"));

        p.after("<span id='after-str'>AfterStr</span>");
        p.after(new Element(Tag.valueOf("span"), "").attr("id", "after-node"));

        Elements children = doc.getElementById("content").children();
        assertEquals(5, children.size());
        assertEquals("before-str", children.get(0).id());
        assertEquals("before-node", children.get(1).id());
        assertEquals("p", children.get(2).tagName());
        assertEquals("after-node", children.get(3).id());
        assertEquals("after-str", children.get(4).id());

        p.wrap("<div class='wrapper'></div>");
        assertEquals("wrapper", p.parent().className());
    }

    @Test(timeout = 4000)
    public void testEqualsHashCodeAndCloneContracts() {
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        el1.addClass("sample");

        // Identity equals contract
        assertEquals(el1, el1);
        Element el2 = new Element(Tag.valueOf("div"), "http://example.com");
        assertNotEquals(el1, el2);
        assertNotEquals(el1, null);
        assertNotEquals(el1, "not-an-element");

        // Hash code consistency
        int hc1 = el1.hashCode();
        assertEquals(hc1, el1.hashCode());

        // Clone contract
        Element cloned = el1.clone();
        assertNotSame(el1, cloned);
        assertNotEquals(el1, cloned); // Identity equals means clone is not equal
        assertEquals(el1.tagName(), cloned.tagName());
        assertEquals(el1.className(), cloned.className());

        // Cloned classNames set is independent
        cloned.addClass("cloned-only");
        assertTrue(cloned.hasClass("cloned-only"));
        assertFalse(el1.hasClass("cloned-only"));
    }

    @Test(timeout = 4000)
    public void testOuterHtmlWithPrettyPrintAndOutline() {
        Document doc = Jsoup.parse("<div><p>Paragraph</p></div>");
        doc.outputSettings().prettyPrint(true).outline(true);
        String html = doc.body().outerHtml();
        assertTrue(html.contains("<p>"));
        assertTrue(html.contains("</p>"));

        assertEquals(doc.body().outerHtml(), doc.body().toString());
    }
}