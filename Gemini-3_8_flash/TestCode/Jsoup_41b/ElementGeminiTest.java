/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.jsoup.nodes.Element
 * Target Framework: JUnit 4 (Defects4J / Java 8 compatible)
 *
 * Decision / Condition Coverage Matrix:
 * 1. equals(Object) & hashCode():
 *    - Branch: this == o (identical reference) -> true
 *    - Branch: o == null -> false
 *    - Branch: getClass() != o.getClass() -> false
 *    - Branch: !super.equals(o) -> false
 *    - Defect Point: 'return this == o;' in Element#equals causes distinct instances with equal state
 *      to incorrectly return false! (Defects4J ElementTest::testHashAndEquals)
 * 2. cssSelector():
 *    - Branch: id().length() > 0 -> returns #id
 *    - Branch: classes.length() > 0 -> appends .class1.class2
 *    - Branch: parent() == null || parent() instanceof Document -> returns tagName + classes
 *    - Branch: parent().select(selector).size() > 1 -> appends :nth-child(%d)
 * 3. insertChildren(int, Collection):
 *    - Branch: index < 0 (negative roll-around: index += currentSize + 1)
 *    - Branch: Validate bounds (index >= 0 && index <= currentSize)
 * 4. siblingElements(), nextElementSibling(), previousElementSibling(), firstElementSibling(), lastElementSibling():
 *    - Branches for parentNode == null, siblings.size() <= 1 vs > 1, index boundaries (0, last).
 * 5. outerHtmlHead() & outerHtmlTail():
 *    - Branch: prettyPrint() && (formatAsBlock || parent.formatAsBlock || outline) -> indent
 *    - Branch: childNodes.isEmpty() && tag.isSelfClosing()
 *      -> Syntax.html && tag.isEmpty() -> '>' vs ' />'
 *    - Branch: out.outline() with single non-text child or multiple children.
 * 6. text(), ownText(), hasText(), data():
 *    - Branch: TextNode vs Element (<br> whitespace insertion, block whitespace insertion)
 *    - Branch: preserveWhitespace() on self or parent (e.g. <pre>, <textarea>)
 *    - Branch: DataNode vs Element in data()
 * 7. Class manipulation (className, classNames, hasClass, addClass, removeClass, toggleClass):
 *    - Split by whitespace, removal of empty strings, case-insensitive check, toggle presence.
 * 8. Form element values val() & val(String):
 *    - Branch: tagName().equals("textarea") vs input/other.
 * ====================================================================================================
 */

package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementGeminiTest {

    // ================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================

    @Test(timeout = 4000)
    public void testBasicCreationAndTagNameChange() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());
        assertTrue(el.isBlock());

        el.tagName("span");
        assertEquals("span", el.tagName());
        assertEquals("span", el.nodeName());
        assertFalse(el.isBlock());
    }

    @Test(timeout = 4000)
    public void testAttributeManipulationAndDataset() {
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        attrs.put("data-item-id", "123");
        attrs.put("data-category", "books");
        Element el = new Element(Tag.valueOf("section"), "http://example.com", attrs);

        assertEquals("main", el.id());
        el.attr("title", "Header Section");
        assertEquals("Header Section", el.attr("title"));

        Map<String, String> dataset = el.dataset();
        assertEquals(2, dataset.size());
        assertEquals("123", dataset.get("item-id"));
        assertEquals("books", dataset.get("category"));
    }

    @Test(timeout = 4000)
    public void testChildAppendsAndPrepends() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("span");
        child1.text("Middle");

        Element child0 = parent.prependElement("header");
        child0.text("Start");

        Element child2 = parent.appendElement("footer");
        child2.text("End");

        assertEquals(3, parent.children().size());
        assertEquals("header", parent.child(0).tagName());
        assertEquals("span", parent.child(1).tagName());
        assertEquals("footer", parent.child(2).tagName());

        parent.appendText(" appendedText");
        parent.prependText("prependedText ");
        assertEquals("prependedText Start Middle End appendedText", parent.text());
    }

    @Test(timeout = 4000)
    public void testInnerHtmlAndAppendPrependFragments() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.html("<p>Original</p>");
        assertEquals(1, parent.children().size());
        assertEquals("Original", parent.child(0).text());

        parent.append("<span>Appended</span>");
        parent.prepend("<b>Prepended</b>");

        assertEquals(3, parent.children().size());
        assertEquals("b", parent.child(0).tagName());
        assertEquals("p", parent.child(1).tagName());
        assertEquals("span", parent.child(2).tagName());
    }

    @Test(timeout = 4000)
    public void testSiblingNavigation() {
        Element parent = new Element(Tag.valueOf("ul"), "");
        Element li1 = parent.appendElement("li").attr("id", "item1");
        Element li2 = parent.appendElement("li").attr("id", "item2");
        Element li3 = parent.appendElement("li").attr("id", "item3");

        assertEquals(0, (int) li1.elementSiblingIndex());
        assertEquals(1, (int) li2.elementSiblingIndex());
        assertEquals(2, (int) li3.elementSiblingIndex());

        assertNull(li1.previousElementSibling());
        assertEquals(li2, li1.nextElementSibling());

        assertEquals(li1, li2.previousElementSibling());
        assertEquals(li3, li2.nextElementSibling());

        assertEquals(li2, li3.previousElementSibling());
        assertNull(li3.nextElementSibling());

        assertEquals(li1, li2.firstElementSibling());
        assertEquals(li3, li2.lastElementSibling());

        Elements siblingsOfLi2 = li2.siblingElements();
        assertEquals(2, siblingsOfLi2.size());
        assertTrue(siblingsOfLi2.contains(li1));
        assertTrue(siblingsOfLi2.contains(li3));
        assertFalse(siblingsOfLi2.contains(li2));
    }

    @Test(timeout = 4000)
    public void testNodeRelativeInsertionBeforeAfterWrap() {
        Element container = new Element(Tag.valueOf("div"), "");
        Element middle = container.appendElement("p").text("Middle");

        middle.before("<span>BeforeHtml</span>");
        middle.after("<span>AfterHtml</span>");
        middle.before(new Element(Tag.valueOf("b"), "").text("BeforeNode"));
        middle.after(new Element(Tag.valueOf("i"), "").text("AfterNode"));

        assertEquals(5, container.children().size());
        assertEquals("span", container.child(0).tagName());
        assertEquals("b", container.child(1).tagName());
        assertEquals("p", container.child(2).tagName());
        assertEquals("i", container.child(3).tagName());
        assertEquals("span", container.child(4).tagName());

        middle.wrap("<div class='wrapper'></div>");
        assertEquals("wrapper", middle.parent().className());
        assertEquals(container, middle.parent().parent());
    }

    @Test(timeout = 4000)
    public void testTextAndWhitespaceHandlingWithBrAndPre() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.append("Hello<br>World\n <span>Next</span>");
        assertEquals("Hello World Next", p.text());
        assertEquals("Hello World", p.ownText());

        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.appendText("  preformatted   lines\n   retained  ");
        assertEquals("  preformatted   lines\n   retained  ", pre.text());
    }

    @Test(timeout = 4000)
    public void testClassManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "  header   active   bold  ");

        assertEquals("header active bold", el.className());
        assertTrue(el.hasClass("header"));
        assertTrue(el.hasClass("ACTIVE"));
        assertFalse(el.hasClass("missing"));

        el.addClass("highlight");
        assertTrue(el.hasClass("highlight"));

        el.removeClass("active");
        assertFalse(el.hasClass("active"));

        el.toggleClass("visible");
        assertTrue(el.hasClass("visible"));
        el.toggleClass("visible");
        assertFalse(el.hasClass("visible"));

        Set<String> newClasses = new LinkedHashSet<String>();
        newClasses.add("one");
        newClasses.add("two");
        el.classNames(newClasses);
        assertEquals("one two", el.className());
        assertEquals(2, el.classNames().size());
    }

    @Test(timeout = 4000)
    public void testValHandlingForInputAndTextarea() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("user123");
        assertEquals("user123", input.val());
        assertEquals("user123", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("Detailed message here");
        assertEquals("Detailed message here", textarea.val());
        assertEquals("Detailed message here", textarea.text());
    }

    @Test(timeout = 4000)
    public void testDomSearchMethods() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = root.appendElement("p").attr("id", "p1").attr("class", "msg info").text("Hello World");
        Element child2 = root.appendElement("p").attr("id", "p2").attr("class", "msg warn").text("Goodbye Moon");
        Element child3 = root.appendElement("span").attr("data-custom", "true").text("Standalone");

        assertEquals(child1, root.getElementById("p1"));
        assertNull(root.getElementById("non-existent"));

        assertEquals(2, root.getElementsByTag("p").size());
        assertEquals(2, root.getElementsByClass("msg").size());
        assertEquals(1, root.getElementsByClass("info").size());
        assertEquals(1, root.getElementsByAttribute("data-custom").size());
        assertEquals(1, root.getElementsByAttributeStarting("data-").size());
        assertEquals(1, root.getElementsByAttributeValue("id", "p1").size());
        assertEquals(2, root.getElementsByAttributeValueNot("id", "p1").size());
        assertEquals(1, root.getElementsByAttributeValueStarting("class", "msg i").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("class", "warn").size());
        assertEquals(2, root.getElementsByAttributeValueContaining("class", "msg").size());
        assertEquals(2, root.getElementsByAttributeValueMatching("id", Pattern.compile("p\\d")).size());
        assertEquals(2, root.getElementsByAttributeValueMatching("id", "p\\d").size());

        assertEquals(1, root.getElementsByIndexLessThan(1).size());
        assertEquals(1, root.getElementsByIndexGreaterThan(1).size());
        assertEquals(1, root.getElementsByIndexEquals(1).size());

        assertEquals(1, root.getElementsContainingText("Hello").size());
        assertEquals(1, root.getElementsContainingOwnText("Goodbye").size());
        assertEquals(1, root.getElementsMatchingText(Pattern.compile("Hello.*")).size());
        assertEquals(1, root.getElementsMatchingText("Goodbye.*").size());
        assertEquals(1, root.getElementsMatchingOwnText(Pattern.compile("Hello.*")).size());
        assertEquals(1, root.getElementsMatchingOwnText("Goodbye.*").size());

        assertEquals(4, root.getAllElements().size());
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================

    @Test(timeout = 4000)
    public void testEmptyElementAndEmptyStateQueries() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.id());
        assertEquals("", el.className());
        assertTrue(el.classNames().isEmpty());
        assertFalse(el.hasText());
        assertEquals("", el.text());
        assertEquals("", el.ownText());
        assertEquals("", el.data());
        assertTrue(el.children().isEmpty());
        assertTrue(el.textNodes().isEmpty());
        assertTrue(el.dataNodes().isEmpty());
        assertEquals(0, el.siblingElements().size());
        assertNull(el.nextElementSibling());
        assertNull(el.previousElementSibling());
        assertNull(el.firstElementSibling());
        assertNull(el.lastElementSibling());
        assertEquals(0, (int) el.elementSiblingIndex());
    }

    @Test(timeout = 4000)
    public void testCssSelectorBoundaries() {
        Element standalone = new Element(Tag.valueOf("div"), "");
        assertEquals("div", standalone.cssSelector());

        standalone.attr("id", "uniqueId");
        assertEquals("#uniqueId", standalone.cssSelector());

        Element doc = new Document("http://example.com");
        Element body = doc.appendElement("body");
        Element div1 = body.appendElement("div").attr("class", "box main");
        Element div2 = body.appendElement("div").attr("class", "box alt");

        assertEquals("div.box.main:nth-child(1)", div1.cssSelector());
        assertEquals("div.box.alt:nth-child(2)", div2.cssSelector());
    }

    @Test(timeout = 4000)
    public void testInsertChildrenIndexBoundaries() {
        Element el = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("span"), "");
        Element child2 = new Element(Tag.valueOf("b"), "");
        Element child3 = new Element(Tag.valueOf("i"), "");

        el.insertChildren(0, Collections.singletonList(child2));
        assertEquals(1, el.children().size());
        assertEquals("b", el.child(0).tagName());

        el.insertChildren(0, Collections.singletonList(child1));
        assertEquals(2, el.children().size());
        assertEquals("span", el.child(0).tagName());
        assertEquals("b", el.child(1).tagName());

        // Negative index: -1 maps to insert at end (currentSize + 1 roll around)
        el.insertChildren(-1, Collections.singletonList(child3));
        assertEquals(3, el.children().size());
        assertEquals("i", el.child(2).tagName());
    }

    @Test(timeout = 4000)
    public void testHtmlOutputFormattingAndSyntaxModes() {
        Document doc = new Document("");
        Element img = doc.body().appendElement("img").attr("src", "foo.jpg");

        doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
        assertTrue(img.outerHtml().endsWith(">"));
        assertFalse(img.outerHtml().endsWith("/>"));

        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        assertTrue(img.outerHtml().endsWith("/>"));

        doc.outputSettings().prettyPrint(false);
        Element div = doc.body().appendElement("div");
        div.html("<span>Text</span>");
        assertEquals("<span>Text</span>", div.html());
    }

    @Test(timeout = 4000)
    public void testHasTextWithOnlyWhitespaceTextNodes() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendChild(new TextNode("    \n\t  ", ""));
        assertFalse(el.hasText());

        el.appendElement("span").text("actual content");
        assertTrue(el.hasText());
    }

    @Test(timeout = 4000)
    public void testDataNodesExtraction() {
        Element script = new Element(Tag.valueOf("script"), "");
        DataNode data = new DataNode("function test() { return 1; }", "");
        script.appendChild(data);

        List<DataNode> nodes = script.dataNodes();
        assertEquals(1, nodes.size());
        assertEquals("function test() { return 1; }", script.data());
    }

    // ================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // ================================================================================================

    /**
     * Targets known defect: org.jsoup.nodes.ElementTest::testHashAndEquals
     * In the defective version, Element#equals performs `return this == o;` instead of delegating
     * state evaluation correctly, causing distinct instances with identical tags, attributes, and
     * children to return false.
     */
    @Test(timeout = 4000)
    public void testHashAndEqualsDefect() {
        Element el1 = new Element(Tag.valueOf("p"), "").attr("class", "one").text("One");
        Element el2 = new Element(Tag.valueOf("p"), "").attr("class", "one").text("One");

        assertTrue("Reflexive equality must pass", el1.equals(el1));
        assertEquals("HashCodes must be identical for identical content", el1.hashCode(), el2.hashCode());
        assertEquals("Equal instances with identical content must be equal", el1, el2);
    }

    @Test(timeout = 4000)
    public void testHashAndEqualsWithParsedDocumentsDefect() {
        Element p1 = Jsoup.parse("<p class=\"one\">One</p>").select("p").first();
        Element p2 = Jsoup.parse("<p class=\"one\">One</p>").select("p").first();

        assertNotNull(p1);
        assertNotNull(p2);
        assertNotSame(p1, p2);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    // ================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullTag() {
        new Element(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTagNameEmptyThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTagNameNullThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendChildNullThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrependChildNullThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.prependChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenOutOfBoundsThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.insertChildren(5, Collections.singletonList(new TextNode("a", "")));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenNegativeOutOfBoundsThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.insertChildren(-5, Collections.singletonList(new TextNode("a", "")));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByTagEmptyThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByTag("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementByIdEmptyThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementById("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByClassEmptyThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByClass("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeEmptyThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttribute("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeStartingEmptyThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttributeStarting("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeValueMatchingInvalidRegexThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttributeValueMatching("title", "[unclosed");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingTextInvalidRegexThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingText("[unclosed");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingOwnTextInvalidRegexThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingOwnText("[unclosed");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTextNullThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.text(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testClassNamesNullThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.classNames(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddClassNullThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveClassNullThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.removeClass(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToggleClassNullThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.toggleClass(null);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testTextNodesListIsUnmodifiable() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendText("Text");
        List<TextNode> textNodes = el.textNodes();
        textNodes.add(new TextNode("More", ""));
    }

    // ================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ================================================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("id", "main");

        assertFalse(el.equals(null));
        assertFalse(el.equals("NotAnElement"));
        assertTrue(el.equals(el));

        Element differentTag = new Element(Tag.valueOf("span"), "http://example.com");
        differentTag.attr("id", "main");
        assertFalse(el.equals(differentTag));

        Element differentAttr = new Element(Tag.valueOf("div"), "http://example.com");
        differentAttr.attr("id", "other");
        assertFalse(el.equals(differentAttr));
    }

    @Test(timeout = 4000)
    public void testCloneContract() {
        Element original = new Element(Tag.valueOf("div"), "http://example.com");
        original.attr("id", "orig");
        original.appendElement("p").text("Paragraph");

        Element clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.tagName(), clone.tagName());
        assertEquals(original.attr("id"), clone.attr("id"));
        assertEquals(original.text(), clone.text());
        assertEquals(original.children().size(), clone.children().size());

        // Ensure deep clone independence
        clone.child(0).text("Modified");
        assertEquals("Paragraph", original.child(0).text());
        assertEquals("Modified", clone.child(0).text());
    }

    @Test(timeout = 4000)
    public void testToStringMatchesOuterHtml() {
        Element el = new Element(Tag.valueOf("p"), "");
        el.attr("class", "msg").text("Content");
        assertEquals(el.outerHtml(), el.toString());
    }

    @Test(timeout = 4000)
    public void testParentsAccumulationUntilRoot() {
        Document doc = Jsoup.parse("<html><body><div id='d1'><p id='p1'><span>Target</span></p></div></body></html>");
        Element span = doc.select("span").first();
        assertNotNull(span);

        Elements parents = span.parents();
        assertEquals(4, parents.size());
        assertEquals("p", parents.get(0).tagName());
        assertEquals("div", parents.get(1).tagName());
        assertEquals("body", parents.get(2).tagName());
        assertEquals("html", parents.get(3).tagName());
    }
}