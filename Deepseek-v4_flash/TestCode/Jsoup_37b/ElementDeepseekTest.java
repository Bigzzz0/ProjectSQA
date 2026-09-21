package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Element.java from Defects4J (org.jsoup.nodes.Element)
 * Known defect: testNotPretty – expected output differs when prettyPrint is false.
 * 
 * Key branches to cover:
 * - outerHtmlHead: condition on accum.length() > 0 && out.prettyPrint() && (tag.formatAsBlock() || parent().tag().formatAsBlock() || out.outline())
 * - outerHtmlTail: condition on !(childNodes.isEmpty() && tag.isSelfClosing()) and inner prettyPrint logic
 * - text(): block vs inline handling, br tag, whitespace normalization
 * - ownText(): similar
 * - preserveWhitespace(): null check, element check, parent check
 * - classNames(): lazy initialization, split regex
 * - hasClass(): case-insensitive comparison
 * - insertChildren(): index roll-around, bounds check
 * - siblingElements(), nextElementSibling(), previousElementSibling(): null parent, index logic
 * - getElementsByAttributeValueMatching(String): PatternSyntaxException handling
 * - equals() and hashCode(): identity-based
 * - clone(): classNames set to null
 * 
 * Defect-targeted: When prettyPrint is false, outerHtml should not contain extra whitespace/newlines.
 * The bug likely causes a newline or space to appear even when prettyPrint is false.
 * We will test that a simple div with a child p produces exactly "<div><p></p></div>" when prettyPrint is false.
 */
public class ElementDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testTagNameGetterSetter() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("div", div.tagName());
        div.tagName("span");
        assertEquals("span", div.tagName());
    }

    @Test(timeout = 4000)
    public void testIsBlock() {
        Element div = new Element(Tag.valueOf("div"), "");
        assertTrue(div.isBlock());
        Element span = new Element(Tag.valueOf("span"), "");
        assertFalse(span.isBlock());
    }

    @Test(timeout = 4000)
    public void testId() {
        Element el = new Element(Tag.valueOf("p"), "");
        assertEquals("", el.id());
        el.attr("id", "main");
        assertEquals("main", el.id());
    }

    @Test(timeout = 4000)
    public void testAttrReturnsThis() {
        Element el = new Element(Tag.valueOf("a"), "");
        Element same = el.attr("href", "http://example.com");
        assertSame(el, same);
        assertEquals("http://example.com", el.attr("href"));
    }

    @Test(timeout = 4000)
    public void testDataset() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("data-name", "jsoup");
        Map<String, String> ds = el.dataset();
        assertEquals("jsoup", ds.get("name"));
        ds.put("version", "1.8");
        assertEquals("1.8", el.attr("data-version"));
    }

    @Test(timeout = 4000)
    public void testParent() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        parent.appendChild(child);
        assertSame(parent, child.parent());
    }

    @Test(timeout = 4000)
    public void testParents() {
        Element root = new Element(Tag.valueOf("html"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        root.appendChild(div);
        div.appendChild(p);
        Elements parents = p.parents();
        assertEquals(2, parents.size());
        assertSame(div, parents.get(0));
        assertSame(root, parents.get(1));
    }

    @Test(timeout = 4000)
    public void testChildByIndex() {
        Element parent = new Element(Tag.valueOf("ul"), "");
        Element li1 = new Element(Tag.valueOf("li"), "");
        Element li2 = new Element(Tag.valueOf("li"), "");
        parent.appendChild(li1);
        parent.appendChild(li2);
        assertSame(li1, parent.child(0));
        assertSame(li2, parent.child(1));
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testChildOutOfBounds() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.child(0);
    }

    @Test(timeout = 4000)
    public void testChildren() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("span"), "");
        parent.appendChild(child1);
        parent.appendChild(new TextNode("text", ""));
        parent.appendChild(child2);
        Elements children = parent.children();
        assertEquals(2, children.size());
        assertSame(child1, children.get(0));
        assertSame(child2, children.get(1));
    }

    @Test(timeout = 4000)
    public void testTextNodes() {
        Element parent = new Element(Tag.valueOf("p"), "");
        parent.appendChild(new TextNode("Hello ", ""));
        parent.appendChild(new Element(Tag.valueOf("b"), ""));
        parent.appendChild(new TextNode(" World", ""));
        List<TextNode> textNodes = parent.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Hello ", textNodes.get(0).getWholeText());
        assertEquals(" World", textNodes.get(1).getWholeText());
    }

    @Test(timeout = 4000)
    public void testDataNodes() {
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("alert('hi');", ""));
        List<DataNode> dataNodes = script.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("alert('hi');", dataNodes.get(0).getWholeData());
    }

    @Test(timeout = 4000)
    public void testSelect() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        div.appendChild(p);
        Elements result = div.select("p");
        assertEquals(1, result.size());
        assertSame(p, result.get(0));
    }

    @Test(timeout = 4000)
    public void testAppendChild() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("span"), "");
        parent.appendChild(child);
        assertSame(child, parent.child(0));
    }

    @Test(timeout = 4000)
    public void testPrependChild() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element first = new Element(Tag.valueOf("span"), "");
        Element second = new Element(Tag.valueOf("p"), "");
        parent.appendChild(second);
        parent.prependChild(first);
        assertSame(first, parent.child(0));
        assertSame(second, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testInsertChildren() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element a = new Element(Tag.valueOf("a"), "");
        Element b = new Element(Tag.valueOf("b"), "");
        parent.appendChild(a);
        parent.appendChild(b);
        Element c = new Element(Tag.valueOf("c"), "");
        parent.insertChildren(1, Collections.singletonList(c));
        assertSame(a, parent.child(0));
        assertSame(c, parent.child(1));
        assertSame(b, parent.child(2));
    }

    @Test(timeout = 4000)
    public void testInsertChildrenNegativeIndex() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element a = new Element(Tag.valueOf("a"), "");
        Element b = new Element(Tag.valueOf("b"), "");
        parent.appendChild(a);
        parent.appendChild(b);
        Element c = new Element(Tag.valueOf("c"), "");
        parent.insertChildren(-1, Collections.singletonList(c)); // -1 means end
        assertSame(a, parent.child(0));
        assertSame(b, parent.child(1));
        assertSame(c, parent.child(2));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInsertChildrenOutOfBounds() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.insertChildren(1, Collections.singletonList(new Element(Tag.valueOf("x"), "")));
    }

    @Test(timeout = 4000)
    public void testAppendElement() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = parent.appendElement("span");
        assertEquals("span", child.tagName());
        assertSame(child, parent.child(0));
    }

    @Test(timeout = 4000)
    public void testPrependElement() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element later = parent.appendElement("p");
        Element first = parent.prependElement("span");
        assertSame(first, parent.child(0));
        assertSame(later, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testAppendText() {
        Element parent = new Element(Tag.valueOf("p"), "");
        parent.appendText("Hello");
        assertEquals("Hello", parent.text());
    }

    @Test(timeout = 4000)
    public void testPrependText() {
        Element parent = new Element(Tag.valueOf("p"), "");
        parent.appendText("World");
        parent.prependText("Hello ");
        assertEquals("Hello World", parent.text());
    }

    @Test(timeout = 4000)
    public void testAppendHtml() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.append("<p>One</p>");
        assertEquals(1, parent.children().size());
        assertEquals("One", parent.text());
    }

    @Test(timeout = 4000)
    public void testPrependHtml() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.append("<p>Two</p>");
        parent.prepend("<p>One</p>");
        assertEquals("One Two", parent.text());
    }

    @Test(timeout = 4000)
    public void testBeforeAfter() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element target = parent.appendElement("p");
        target.before("<span>before</span>");
        target.after("<span>after</span>");
        assertEquals(3, parent.children().size());
        assertEquals("before", parent.child(0).text());
        assertEquals("after", parent.child(2).text());
    }

    @Test(timeout = 4000)
    public void testEmpty() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendChild(new Element(Tag.valueOf("p"), ""));
        parent.empty();
        assertEquals(0, parent.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testWrap() {
        Element inner = new Element(Tag.valueOf("span"), "");
        inner.wrap("<div></div>");
        assertEquals("div", inner.parent().tagName());
    }

    @Test(timeout = 4000)
    public void testSiblingElements() {
        Element parent = new Element(Tag.valueOf("ul"), "");
        Element li1 = parent.appendElement("li");
        Element li2 = parent.appendElement("li");
        Element li3 = parent.appendElement("li");
        Elements sibs = li2.siblingElements();
        assertEquals(2, sibs.size());
        assertTrue(sibs.contains(li1));
        assertTrue(sibs.contains(li3));
    }

    @Test(timeout = 4000)
    public void testNextPreviousElementSibling() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element a = parent.appendElement("a");
        Element b = parent.appendElement("b");
        Element c = parent.appendElement("c");
        assertSame(b, a.nextElementSibling());
        assertSame(a, b.previousElementSibling());
        assertNull(c.nextElementSibling());
        assertNull(a.previousElementSibling());
    }

    @Test(timeout = 4000)
    public void testFirstLastElementSibling() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element a = parent.appendElement("a");
        Element b = parent.appendElement("b");
        assertSame(a, a.firstElementSibling());
        assertSame(a, b.firstElementSibling());
        assertSame(b, a.lastElementSibling());
        assertSame(b, b.lastElementSibling());
    }

    @Test(timeout = 4000)
    public void testElementSiblingIndex() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element a = parent.appendElement("a");
        Element b = parent.appendElement("b");
        assertEquals(0, a.elementSiblingIndex().intValue());
        assertEquals(1, b.elementSiblingIndex().intValue());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTagNameEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAppendChildNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendChild(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrependChildNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.prependChild(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInsertChildrenNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.insertChildren(0, null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAppendNullHtml() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.append(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrependNullHtml() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.prepend(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTextNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.text(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testClassNamesNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.classNames(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddClassNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRemoveClassNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.removeClass(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToggleClassNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.toggleClass(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByTagEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByTag("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementByIdEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementById("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByClassEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByClass("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttribute("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeStartingEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttributeStarting("");
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatchingInvalidRegex() {
        Element el = new Element(Tag.valueOf("div"), "");
        try {
            el.getElementsByAttributeValueMatching("href", "[invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Pattern syntax error"));
        }
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexLessThan() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p");
        parent.appendElement("p");
        Elements els = parent.getElementsByIndexLessThan(1);
        assertEquals(1, els.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexGreaterThan() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p");
        parent.appendElement("p");
        parent.appendElement("p");
        Elements els = parent.getElementsByIndexGreaterThan(1);
        assertEquals(1, els.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexEquals() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p");
        parent.appendElement("p");
        Elements els = parent.getElementsByIndexEquals(0);
        assertEquals(1, els.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingText() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(new TextNode("Hello World", ""));
        Elements els = div.getElementsContainingText("world");
        assertEquals(1, els.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingOwnText() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(new TextNode("Hello", ""));
        Element span = div.appendElement("span");
        span.appendChild(new TextNode("World", ""));
        Elements els = div.getElementsContainingOwnText("Hello");
        assertEquals(1, els.size());
        assertSame(div, els.get(0));
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingText() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(new TextNode("Hello World", ""));
        Elements els = div.getElementsMatchingText("Hello.*");
        assertEquals(1, els.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingOwnText() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(new TextNode("Hello", ""));
        Elements els = div.getElementsMatchingOwnText("Hello");
        assertEquals(1, els.size());
    }

    @Test(timeout = 4000)
    public void testGetAllElements() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("p").appendElement("span");
        Elements all = div.getAllElements();
        assertEquals(3, all.size()); // div, p, span
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testNotPretty() {
        // This test targets the known defect: when prettyPrint is false, outerHtml should not contain extra whitespace.
        Element div = new Element(Tag.valueOf("div"), "");
        Element p = div.appendElement("p");
        p.appendChild(new TextNode("Hello", ""));

        // Simulate Document with prettyPrint false
        Document doc = new Document("");
        doc.appendChild(div);
        doc.outputSettings().prettyPrint(false);

        String outer = div.outerHtml();
        // Expected: no newlines or extra spaces
        assertEquals("<div><p>Hello</p></div>", outer);
    }

    @Test(timeout = 4000)
    public void testPrettyPrintDefault() {
        // Verify that with prettyPrint true (default), output is formatted
        Element div = new Element(Tag.valueOf("div"), "");
        Element p = div.appendElement("p");
        p.appendChild(new TextNode("Hello", ""));

        Document doc = new Document("");
        doc.appendChild(div);
        // prettyPrint true by default
        String outer = div.outerHtml();
        // Should contain newline and indentation
        assertTrue(outer.contains("\n"));
        assertTrue(outer.contains("  "));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullTag() {
        new Element(null, "http://example.com");
    }

    @Test(timeout = 4000)
    public void testHasClassCaseInsensitive() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "Header");
        assertTrue(el.hasClass("header"));
        assertTrue(el.hasClass("Header"));
        assertFalse(el.hasClass("footer"));
    }

    @Test(timeout = 4000)
    public void testAddRemoveToggleClass() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass("foo");
        assertTrue(el.hasClass("foo"));
        el.removeClass("foo");
        assertFalse(el.hasClass("foo"));
        el.toggleClass("bar");
        assertTrue(el.hasClass("bar"));
        el.toggleClass("bar");
        assertFalse(el.hasClass("bar"));
    }

    @Test(timeout = 4000)
    public void testValTextarea() {
        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("content");
        assertEquals("content", textarea.val());
        textarea.val("new content");
        assertEquals("new content", textarea.text());
    }

    @Test(timeout = 4000)
    public void testValInput() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("value", "initial");
        assertEquals("initial", input.val());
        input.val("updated");
        assertEquals("updated", input.attr("value"));
    }

    @Test(timeout = 4000)
    public void testHasText() {
        Element el = new Element(Tag.valueOf("p"), "");
        assertFalse(el.hasText());
        el.appendChild(new TextNode("   ", ""));
        assertFalse(el.hasText());
        el.appendChild(new TextNode("hello", ""));
        assertTrue(el.hasText());
    }

    @Test(timeout = 4000)
    public void testData() {
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("var x = 1;", ""));
        assertEquals("var x = 1;", script.data());
    }

    @Test(timeout = 4000)
    public void testClassName() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.className());
        el.attr("class", "foo bar");
        assertEquals("foo bar", el.className());
    }

    @Test(timeout = 4000)
    public void testClassNamesLazyInit() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "a b");
        Set<String> names = el.classNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));
        // Second call should return same set
        assertSame(names, el.classNames());
    }

    @Test(timeout = 4000)
    public void testClassNamesSet() {
        Element el = new Element(Tag.valueOf("div"), "");
        Set<String> newClasses = new LinkedHashSet<>(Arrays.asList("x", "y"));
        el.classNames(newClasses);
        assertEquals("x y", el.attr("class"));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEquals() {
        Element a = new Element(Tag.valueOf("div"), "");
        Element b = new Element(Tag.valueOf("div"), "");
        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
        assertFalse(a.equals(null));
        assertFalse(a.equals("string"));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        Element a = new Element(Tag.valueOf("div"), "");
        Element b = new Element(Tag.valueOf("div"), "");
        // Not equal objects should have different hash codes (not guaranteed but likely)
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testClone() {
        Element original = new Element(Tag.valueOf("div"), "");
        original.attr("class", "foo");
        original.appendChild(new Element(Tag.valueOf("p"), ""));
        Element clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.tagName(), clone.tagName());
        assertEquals(original.attr("class"), clone.attr("class"));
        assertEquals(original.childNodeSize(), clone.childNodeSize());
        // classNames should be independent
        assertNotSame(original.classNames(), clone.classNames());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadTail() {
        // Test self-closing tag
        Element br = new Element(Tag.valueOf("br"), "");
        Document doc = new Document("");
        doc.appendChild(br);
        doc.outputSettings().prettyPrint(false);
        assertEquals("<br />", br.outerHtml());

        // Test non-self-closing with children
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(new TextNode("text", ""));
        doc = new Document("");
        doc.appendChild(div);
        doc.outputSettings().prettyPrint(false);
        assertEquals("<div>text</div>", div.outerHtml());
    }

    @Test(timeout = 4000)
    public void testHtmlSetter() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.html("<p>Hello</p>");
        assertEquals("<p>Hello</p>", div.html());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespace() {
        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.appendChild(new TextNode("  spaced  ", ""));
        assertEquals("  spaced  ", pre.text());
    }

    @Test(timeout = 4000)
    public void testOwnText() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(new TextNode("Hello ", ""));
        Element span = div.appendElement("span");
        span.appendChild(new TextNode("World", ""));
        div.appendChild(new TextNode("!", ""));
        assertEquals("Hello !", div.ownText());
    }

    @Test(timeout = 4000)
    public void testSiblingElementsNoParent() {
        Element orphan = new Element(Tag.valueOf("div"), "");
        Elements sibs = orphan.siblingElements();
        assertEquals(0, sibs.size());
    }

    @Test(timeout = 4000)
    public void testNextPreviousSiblingNoParent() {
        Element orphan = new Element(Tag.valueOf("div"), "");
        assertNull(orphan.nextElementSibling());
        assertNull(orphan.previousElementSibling());
    }

    @Test(timeout = 4000)
    public void testFirstLastElementSiblingSingleChild() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element only = parent.appendElement("p");
        assertNull(only.firstElementSibling());
        assertNull(only.lastElementSibling());
    }

    @Test(timeout = 4000)
    public void testElementSiblingIndexNoParent() {
        Element orphan = new Element(Tag.valueOf("div"), "");
        assertEquals(0, orphan.elementSiblingIndex().intValue());
    }

    @Test(timeout = 4000)
    public void testGetElementByIdNotFound() {
        Element div = new Element(Tag.valueOf("div"), "");
        assertNull(div.getElementById("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatchingWithPattern() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("data-key", "value123");
        Pattern p = Pattern.compile("value\\d+");
        Elements els = div.getElementsByAttributeValueMatching("data-key", p);
        assertEquals(1, els.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueNot() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("class", "foo");
        Element other = div.appendElement("p");
        other.attr("class", "bar");
        Elements notFoo = div.getElementsByAttributeValueNot("class", "foo");
        assertEquals(1, notFoo.size());
        assertSame(other, notFoo.get(0));
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueStarting() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("data-name", "jsoup");
        Elements els = div.getElementsByAttributeValueStarting("data-name", "js");
        assertEquals(1, els.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueEnding() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("href", "http://example.com");
        Elements els = div.getElementsByAttributeValueEnding("href", ".com");
        assertEquals(1, els.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueContaining() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("class", "header main");
        Elements els = div.getElementsByAttributeValueContaining("class", "ader");
        assertEquals(1, els.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingTextInvalidRegex() {
        Element div = new Element(Tag.valueOf("div"), "");
        try {
            div.getElementsMatchingText("[invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Pattern syntax error"));
        }
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingOwnTextInvalidRegex() {
        Element div = new Element(Tag.valueOf("div"), "");
        try {
            div.getElementsMatchingOwnText("[invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Pattern syntax error"));
        }
    }
}