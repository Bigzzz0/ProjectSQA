package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target: org.jsoup.nodes.Element
 *
 * Key Decision Branches & Logic Boundaries:
 * 1. Defect Zone (Defects4J):
 *    - `indexInList(Element search, List<E> elements)`: In buggy versions, `element.equals(search)`
 *      is used instead of reference equality (`==`). When siblings have identical tags and content,
 *      `indexInList` prematurely matches the first duplicate sibling, corrupting `elementSiblingIndex()`,
 *      `nextElementSibling()`, `previousElementSibling()`, etc.
 * 2. Tree Traversal & Relational Navigation:
 *    - `parent()`, `parents()` (checks `#root` stopping boundary and root null conditions)
 *    - `child(i)`, `children()`, `textNodes()`, `dataNodes()`
 *    - `firstElementSibling()`, `lastElementSibling()`, `nextElementSibling()`, `previousElementSibling()`,
 *      `siblingElements()`, `elementSiblingIndex()` with 0, 1, and multiple siblings, null parent.
 * 3. Manipulation & Hierarchy Insertion:
 *    - `appendChild`, `prependChild`, `insertChildren` (with negative index wrap-around and boundary checks)
 *    - `appendElement`, `prependElement`, `appendText`, `prependText`
 *    - `append(html)`, `prepend(html)`, `before(html/node)`, `after(html/node)`, `empty()`, `wrap(html)`
 * 4. CSS Selector Generator:
 *    - `cssSelector()`: element with id, without id, with classes, root parent, document parent, multiple same-tag siblings triggering :nth-child(n).
 * 5. Class & Attribute Manipulation:
 *    - `className()`, `classNames()`, `classNames(set)`, `hasClass()`, `addClass()`, `removeClass()`, `toggleClass()`
 *    - Case insensitivity, empty strings, multiple classes, class whitespace splitting.
 * 6. Content, Text, and Data Extraction:
 *    - `text()`, `ownText()`, `hasText()`, `text(val)`
 *    - `preserveWhitespace` handling for `<pre>` / `<textarea>` or ancestors.
 *    - `data()`, `val()`, `val(str)` (textarea vs regular input/element)
 * 7. Collector & Search Methods:
 *    - `getElementById`, `getElementsByTag`, `getElementsByClass`
 *    - `getElementsByAttribute`, `getElementsByAttributeStarting`, `getElementsByAttributeValue`,
 *      `getElementsByAttributeValueNot`, `getElementsByAttributeValueStarting`, `getElementsByAttributeValueEnding`,
 *      `getElementsByAttributeValueContaining`, `getElementsByAttributeValueMatching(Pattern/regex)`
 *    - `getElementsByIndexLessThan`, `getElementsByIndexGreaterThan`, `getElementsByIndexEquals`
 *    - `getElementsContainingText`, `getElementsContainingOwnText`, `getElementsMatchingText`, `getElementsMatchingOwnText`
 * 8. HTML Output & Formatting:
 *    - `html()`, `html(str)`, `outerHtmlHead`, `outerHtmlTail`, block vs inline, self-closing, XML vs HTML syntax.
 * 9. Object Contract & Lifecycle:
 *    - `equals`, `hashCode`, `clone`, `nodeName`, `tagName`, `isBlock`, `dataset`.
 */
public class ElementGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testElementSiblingIndexSameContent() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p").text("same");
        Element child2 = parent.appendElement("p").text("same");
        Element child3 = parent.appendElement("p").text("same");

        // When elements have identical content, indexInList must match the exact reference,
        // otherwise index 1 or 2 will resolve to 0.
        assertEquals(0, (int) child1.elementSiblingIndex());
        assertEquals(1, (int) child2.elementSiblingIndex());
        assertEquals(2, (int) child3.elementSiblingIndex());
    }

    @Test(timeout = 4000)
    public void testGetSiblingsWithDuplicateContent() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element p1 = parent.appendElement("p").text("dup");
        Element p2 = parent.appendElement("p").text("dup");
        Element p3 = parent.appendElement("p").text("different");

        assertSame(p2, p1.nextElementSibling());
        assertSame(p3, p2.nextElementSibling());
        assertNull(p3.nextElementSibling());

        assertSame(p2, p3.previousElementSibling());
        assertSame(p1, p2.previousElementSibling());
        assertNull(p1.previousElementSibling());

        Elements p2Siblings = p2.siblingElements();
        assertEquals(2, p2Siblings.size());
        assertTrue(p2Siblings.contains(p1));
        assertTrue(p2Siblings.contains(p3));
        assertFalse(p2Siblings.contains(p2));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTagAndNodeNameMutations() {
        Element el = new Element(Tag.valueOf("span"), "");
        assertEquals("span", el.tagName());
        assertEquals("span", el.nodeName());
        assertFalse(el.isBlock());

        el.tagName("div");
        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());
        assertTrue(el.isBlock());
        assertEquals(Tag.valueOf("div"), el.tag());
    }

    @Test(timeout = 4000)
    public void testAttributesAndDataset() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("id", "main-div");
        el.attr("data-test", "val1");
        el.attr("data-other", "val2");
        el.attr("title", "tooltip");

        assertEquals("main-div", el.id());
        Map<String, String> dataset = el.dataset();
        assertEquals(2, dataset.size());
        assertEquals("val1", dataset.get("test"));
        assertEquals("val2", dataset.get("other"));

        dataset.put("dynamic", "dynVal");
        assertEquals("dynVal", el.attr("data-dynamic"));
    }

    @Test(timeout = 4000)
    public void testParentAndParentsTraversal() {
        Element docRoot = new Element(Tag.valueOf("#root"), "");
        Element html = docRoot.appendElement("html");
        Element body = html.appendElement("body");
        Element div = body.appendElement("div");
        Element p = div.appendElement("p");

        assertSame(div, p.parent());
        Elements parents = p.parents();
        assertEquals(3, parents.size());
        assertSame(div, parents.get(0));
        assertSame(body, parents.get(1));
        assertSame(html, parents.get(2));
        assertFalse(parents.contains(docRoot));
    }

    @Test(timeout = 4000)
    public void testChildrenAndFiltering() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendText("Text 1 ");
        Element child1 = parent.appendElement("span");
        parent.appendText("Text 2 ");
        Element child2 = parent.appendElement("a");
        DataNode data = new DataNode("var x = 1;", "");
        parent.appendChild(data);

        assertEquals(2, parent.children().size());
        assertSame(child1, parent.child(0));
        assertSame(child2, parent.child(1));

        List<TextNode> textNodes = parent.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Text 1 ", textNodes.get(0).getWholeText());
        assertEquals("Text 2 ", textNodes.get(1).getWholeText());

        List<DataNode> dataNodes = parent.dataNodes();
        assertEquals(1, dataNodes.size());
        assertSame(data, dataNodes.get(0));
    }

    @Test(timeout = 4000)
    public void testInsertionAndManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendElement("p").text("Middle");
        Element first = el.prependElement("header").text("Header");
        Element last = el.appendElement("footer").text("Footer");

        assertEquals(3, el.children().size());
        assertSame(first, el.child(0));
        assertSame(last, el.child(2));

        el.prependText("Start-Text: ");
        el.appendText(" :End-Text");
        assertTrue(el.text().startsWith("Start-Text:"));
        assertTrue(el.text().endsWith(":End-Text"));

        Element span = new Element(Tag.valueOf("span"), "");
        span.text("inserted");
        el.insertChildren(1, Collections.singletonList(span));
        assertSame(span, el.child(1));

        Element endSpan = new Element(Tag.valueOf("b"), "");
        el.insertChildren(-1, Collections.singletonList(endSpan));
        assertSame(endSpan, el.childNodes().get(el.childNodes().size() - 1));
    }

    @Test(timeout = 4000)
    public void testAppendPrependHtml() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.append("<p>Paragraph</p>");
        assertEquals(1, div.children().size());
        assertEquals("p", div.child(0).tagName());

        div.prepend("<h1>Header</h1>");
        assertEquals(2, div.children().size());
        assertEquals("h1", div.child(0).tagName());
        assertEquals("p", div.child(1).tagName());
    }

    @Test(timeout = 4000)
    public void testBeforeAfterWrap() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child = root.appendElement("span");

        child.before("<header>Top</header>");
        child.after("<footer>Bottom</footer>");

        assertEquals(3, root.children().size());
        assertEquals("header", root.child(0).tagName());
        assertEquals("span", root.child(1).tagName());
        assertEquals("footer", root.child(2).tagName());

        Element newBeforeNode = new Element(Tag.valueOf("i"), "");
        child.before(newBeforeNode);
        assertSame(newBeforeNode, root.child(1));

        Element newAfterNode = new Element(Tag.valueOf("b"), "");
        child.after(newAfterNode);
        assertSame(newAfterNode, root.child(3));

        child.wrap("<div class='wrapper'></div>");
        assertEquals("wrapper", child.parent().className());
    }

    @Test(timeout = 4000)
    public void testClassNamesAndManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.className());
        assertFalse(el.hasClass("active"));

        el.addClass("active");
        assertTrue(el.hasClass("active"));
        assertTrue(el.hasClass("ACTIVE")); // case-insensitive
        assertEquals("active", el.className());

        el.addClass("selected");
        Set<String> classNames = el.classNames();
        assertEquals(2, classNames.size());
        assertTrue(classNames.contains("active"));
        assertTrue(classNames.contains("selected"));

        el.removeClass("active");
        assertFalse(el.hasClass("active"));
        assertTrue(el.hasClass("selected"));

        el.toggleClass("selected");
        assertFalse(el.hasClass("selected"));
        el.toggleClass("new-class");
        assertTrue(el.hasClass("new-class"));

        Set<String> custom = new LinkedHashSet<String>();
        custom.add("c1");
        custom.add("c2");
        el.classNames(custom);
        assertEquals("c1 c2", el.className());

        // Coverage of hasClass length optimization
        el.attr("class", "a");
        assertFalse(el.hasClass("longerName"));
        el.attr("class", "");
        assertFalse(el.hasClass("any"));
    }

    @Test(timeout = 4000)
    public void testTextAndOwnText() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.append("Hello <b>there</b> world! <br> NewLine");

        assertEquals("Hello there world! NewLine", p.text());
        assertEquals("Hello world!", p.ownText());
        assertTrue(p.hasText());

        Element emptyP = new Element(Tag.valueOf("p"), "");
        assertFalse(emptyP.hasText());
        assertEquals("", emptyP.text());
        assertEquals("", emptyP.ownText());

        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.appendText("  spaced   text  \n  here ");
        assertEquals("  spaced   text  \n  here ", pre.text()); // whitespace preserved

        p.text("Replaced text");
        assertEquals("Replaced text", p.text());
        assertEquals(1, p.textNodes().size());
    }

    @Test(timeout = 4000)
    public void testDataAndVal() {
        Element script = new Element(Tag.valueOf("script"), "");
        DataNode data = new DataNode("var a = 10;\nvar b = 20;", "");
        script.appendChild(data);
        assertEquals("var a = 10;\nvar b = 20;", script.data());

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("Default content");
        assertEquals("Default content", textarea.val());
        textarea.val("Updated content");
        assertEquals("Updated content", textarea.val());

        Element input = new Element(Tag.valueOf("input"), "");
        input.val("admin");
        assertEquals("admin", input.val());
        assertEquals("admin", input.attr("value"));
    }

    @Test(timeout = 4000)
    public void testCssSelector() {
        Element root = new Element(Tag.valueOf("div"), "");
        root.attr("id", "root-id");
        assertEquals("#root-id", root.cssSelector());

        Element subDiv = root.appendElement("div").addClass("header").addClass("main");
        assertEquals("#root-id > div.header.main", subDiv.cssSelector());

        Element p1 = subDiv.appendElement("p");
        Element p2 = subDiv.appendElement("p");
        assertEquals("#root-id > div.header.main > p:nth-child(1)", p1.cssSelector());
        assertEquals("#root-id > div.header.main > p:nth-child(2)", p2.cssSelector());

        Element standalone = new Element(Tag.valueOf("span"), "");
        assertEquals("span", standalone.cssSelector());
    }

    @Test(timeout = 4000)
    public void testSiblingNavigationEdgeCases() {
        Element single = new Element(Tag.valueOf("div"), "");
        assertEquals(0, (int) single.elementSiblingIndex());
        assertNull(single.nextElementSibling());
        assertNull(single.previousElementSibling());
        assertNull(single.firstElementSibling());
        assertNull(single.lastElementSibling());
        assertEquals(0, single.siblingElements().size());

        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("span");
        Element child2 = parent.appendElement("a");

        assertSame(child1, child1.firstElementSibling());
        assertSame(child2, child1.lastElementSibling());
        assertSame(child1, child2.firstElementSibling());
        assertSame(child2, child2.lastElementSibling());

        assertSame(child2, child1.nextElementSibling());
        assertNull(child1.previousElementSibling());
        assertSame(child1, child2.previousElementSibling());
        assertNull(child2.nextElementSibling());
    }

    // =========================================================================
    // Partition B: Collector, Evaluator & DOM Search Methods
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetElementsQueries() {
        Element doc = new Element(Tag.valueOf("div"), "");
        Element header = doc.appendElement("header").attr("id", "head").addClass("bar");
        Element p1 = doc.appendElement("p").attr("data-role", "summary").text("Alpha Bravo Charlie");
        Element p2 = doc.appendElement("p").attr("data-role", "details").attr("data-hidden", "true").text("Bravo Delta");
        Element footer = doc.appendElement("footer").addClass("bar").text("Footer note");

        assertSame(header, doc.getElementById("head"));
        assertNull(doc.getElementById("non-existent"));

        assertEquals(2, doc.getElementsByTag("p").size());
        assertEquals(2, doc.getElementsByClass("bar").size());

        assertEquals(1, doc.getElementsByAttribute("data-hidden").size());
        assertEquals(2, doc.getElementsByAttributeStarting("data-ro").size());

        assertEquals(1, doc.getElementsByAttributeValue("data-role", "summary").size());
        assertEquals(3, doc.getElementsByAttributeValueNot("data-role", "summary").size());
        assertEquals(2, doc.getElementsByAttributeValueStarting("data-role", "d").size());
        assertEquals(1, doc.getElementsByAttributeValueEnding("data-role", "ils").size());
        assertEquals(2, doc.getElementsByAttributeValueContaining("data-role", "et").size());

        assertEquals(2, doc.getElementsByAttributeValueMatching("data-role", Pattern.compile("^d.*")).size());
        assertEquals(2, doc.getElementsByAttributeValueMatching("data-role", "^d.*").size());

        assertEquals(2, doc.getElementsByIndexLessThan(2).size());
        assertEquals(2, doc.getElementsByIndexGreaterThan(1).size());
        assertEquals(1, doc.getElementsByIndexEquals(0).size());

        assertEquals(2, doc.getElementsContainingText("Bravo").size());
        assertEquals(1, doc.getElementsContainingOwnText("Alpha").size());
        assertEquals(2, doc.getElementsMatchingText(Pattern.compile(".*Bravo.*")).size());
        assertEquals(2, doc.getElementsMatchingText(".*Bravo.*").size());
        assertEquals(1, doc.getElementsMatchingOwnText(Pattern.compile("^Alpha.*")).size());
        assertEquals(1, doc.getElementsMatchingOwnText("^Alpha.*").size());

        Elements all = doc.getAllElements();
        assertEquals(5, all.size());
    }

    @Test(timeout = 4000)
    public void testSelectQuery() {
        Element doc = new Element(Tag.valueOf("div"), "");
        doc.append("<p class='intro'>First</p><p>Second</p>");
        Elements selected = doc.select("p.intro");
        assertEquals(1, selected.size());
        assertEquals("First", selected.get(0).text());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullTagConstructorThrows() {
        new Element(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyTagNameThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullChildThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenNullThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.insertChildren(0, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenOutOfBoundsThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.insertChildren(5, Collections.<Node>emptyList());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidRegexPatternThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttributeValueMatching("key", "[invalid(");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidMatchingTextRegexThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingText("[unclosed");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidMatchingOwnTextRegexThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingOwnText("*invalid");
    }

    // =========================================================================
    // Partition E: Output Formatting & HTML Syntax
    // =========================================================================

    @Test(timeout = 4000)
    public void testHtmlFormatting() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.html("<span>Hello</span>");
        assertEquals("<span>Hello</span>", div.html());

        div.empty();
        assertEquals(0, div.childNodes().size());
        assertEquals("", div.html());

        Element img = new Element(Tag.valueOf("img"), "");
        assertEquals("<img>", img.outerHtml());

        Document doc = new Document("");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        Element xmlImg = doc.appendElement("img");
        assertEquals("<img />", xmlImg.outerHtml());
    }

    // =========================================================================
    // Partition F: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        Element el2 = new Element(Tag.valueOf("div"), "http://example.com");
        Element el3 = new Element(Tag.valueOf("span"), "http://example.com");

        assertTrue(el1.equals(el2));
        assertTrue(el2.equals(el1));
        assertEquals(el1.hashCode(), el2.hashCode());

        assertFalse(el1.equals(el3));
        assertFalse(el1.equals(null));
        assertFalse(el1.equals("String Object"));
        assertTrue(el1.equals(el1));
    }

    @Test(timeout = 4000)
    public void testCloneIntegrity() {
        Element el = new Element(Tag.valueOf("div"), "http://base.org");
        el.attr("class", "sample");
        el.appendElement("p").text("Paragraph text");

        Element clone = el.clone();
        assertNotSame(el, clone);
        assertEquals(el.outerHtml(), clone.outerHtml());
        assertEquals(el.tag(), clone.tag());
        assertEquals(el.className(), clone.className());
        assertEquals(1, clone.children().size());

        // Mutating clone does not mutate original
        clone.attr("class", "mutated");
        assertFalse(el.className().equals(clone.className()));
    }
}