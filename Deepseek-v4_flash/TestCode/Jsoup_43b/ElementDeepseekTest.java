package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import java.util.*;

/**
 * ElementDeepseekTest: White-box test suite for Element.java targeting the known Defects4J defect
 * where elementSiblingIndex and sibling navigation fail when sibling elements have identical content.
 * 
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (constructors, tag, id, dataset, parent, children, text, etc.)
 * - Partition B: Boundary values (null, empty, negative indices, large values)
 * - Partition C: Defect-targeted branch zone (duplicate sibling content, indexInList identity vs equality)
 * - Partition D: Exception & defensive guard paths (null arguments, invalid indices, pattern syntax)
 * - Partition E: Object lifecycle (equals, hashCode, clone)
 * 
 * Known defect: indexInList uses equals() instead of identity (==), causing wrong sibling index
 * when multiple elements have the same tag and attributes.
 */
public class ElementDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorAndTag() {
        Tag tag = Tag.valueOf("div");
        Element el = new Element(tag, "http://example.com");
        assertEquals("div", el.tagName());
        assertEquals(tag, el.tag());
        assertTrue(el.isBlock()); // div is block
    }

    @Test(timeout = 4000)
    public void testConstructorWithAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("class", "test");
        Element el = new Element(Tag.valueOf("span"), "http://example.com", attrs);
        assertEquals("span", el.tagName());
        assertEquals("test", el.attr("class"));
    }

    @Test(timeout = 4000)
    public void testTagNameSetter() {
        Element el = new Element(Tag.valueOf("span"), "http://example.com");
        el.tagName("div");
        assertEquals("div", el.tagName());
        assertTrue(el.isBlock());
    }

    @Test(timeout = 4000)
    public void testId() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("", el.id());
        el.attr("id", "myId");
        assertEquals("myId", el.id());
    }

    @Test(timeout = 4000)
    public void testDataset() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("data-name", "jsoup");
        Map<String, String> ds = el.dataset();
        assertEquals("jsoup", ds.get("name"));
        ds.put("version", "1.0");
        assertEquals("1.0", el.attr("data-version"));
    }

    @Test(timeout = 4000)
    public void testParent() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = new Element(Tag.valueOf("p"), "http://example.com");
        parent.appendChild(child);
        assertSame(parent, child.parent());
    }

    @Test(timeout = 4000)
    public void testParents() {
        Element grandparent = new Element(Tag.valueOf("div"), "http://example.com");
        Element parent = new Element(Tag.valueOf("p"), "http://example.com");
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        grandparent.appendChild(parent);
        parent.appendChild(child);
        Elements parents = child.parents();
        assertEquals(2, parents.size());
        assertSame(parent, parents.get(0));
        assertSame(grandparent, parents.get(1));
    }

    @Test(timeout = 4000)
    public void testChildByIndex() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child1 = new Element(Tag.valueOf("p"), "http://example.com");
        Element child2 = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(child1);
        parent.appendChild(child2);
        assertSame(child1, parent.child(0));
        assertSame(child2, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testChildren() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        parent.appendChild(new TextNode("text", "http://example.com"));
        parent.appendChild(new Element(Tag.valueOf("span"), "http://example.com"));
        Elements children = parent.children();
        assertEquals(2, children.size());
    }

    @Test(timeout = 4000)
    public void testTextNodes() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendChild(new TextNode("Hello ", "http://example.com"));
        parent.appendChild(new Element(Tag.valueOf("b"), "http://example.com"));
        parent.appendChild(new TextNode(" World", "http://example.com"));
        List<TextNode> textNodes = parent.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Hello ", textNodes.get(0).getWholeText());
        assertEquals(" World", textNodes.get(1).getWholeText());
    }

    @Test(timeout = 4000)
    public void testDataNodes() {
        Element parent = new Element(Tag.valueOf("script"), "http://example.com");
        parent.appendChild(new DataNode("alert('hi');", "http://example.com"));
        List<DataNode> dataNodes = parent.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("alert('hi');", dataNodes.get(0).getWholeData());
    }

    @Test(timeout = 4000)
    public void testSelect() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("id", "test");
        Elements selected = el.select("#test");
        assertEquals(1, selected.size());
        assertSame(el, selected.get(0));
    }

    @Test(timeout = 4000)
    public void testAppendChild() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = new Element(Tag.valueOf("p"), "http://example.com");
        parent.appendChild(child);
        assertSame(child, parent.child(0));
        assertEquals(0, child.siblingIndex());
    }

    @Test(timeout = 4000)
    public void testPrependChild() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child1 = new Element(Tag.valueOf("p"), "http://example.com");
        Element child2 = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(child1);
        parent.prependChild(child2);
        assertSame(child2, parent.child(0));
        assertSame(child1, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testInsertChildren() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child1 = new Element(Tag.valueOf("p"), "http://example.com");
        Element child2 = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(child1);
        parent.insertChildren(0, Collections.singletonList(child2));
        assertSame(child2, parent.child(0));
        assertSame(child1, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testInsertChildrenNegativeIndex() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child1 = new Element(Tag.valueOf("p"), "http://example.com");
        Element child2 = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(child1);
        parent.insertChildren(-1, Collections.singletonList(child2)); // -1 means end
        assertSame(child1, parent.child(0));
        assertSame(child2, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testAppendElement() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = parent.appendElement("p");
        assertEquals("p", child.tagName());
        assertSame(child, parent.child(0));
    }

    @Test(timeout = 4000)
    public void testPrependElement() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.prependElement("span");
        assertSame(child2, parent.child(0));
        assertSame(child1, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testAppendText() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendText("Hello");
        assertEquals("Hello", parent.text());
    }

    @Test(timeout = 4000)
    public void testPrependText() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendText("World");
        parent.prependText("Hello ");
        assertEquals("Hello World", parent.text());
    }

    @Test(timeout = 4000)
    public void testAppendHtml() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.append("<p>Hello</p>");
        assertEquals("Hello", parent.text());
        assertEquals("p", parent.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testPrependHtml() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.append("<span>World</span>");
        parent.prepend("<p>Hello </p>");
        assertEquals("Hello World", parent.text());
        assertEquals("p", parent.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testBeforeAfter() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = parent.appendElement("p");
        child.before("<span>before</span>");
        child.after("<span>after</span>");
        assertEquals(3, parent.children().size());
        assertEquals("span", parent.child(0).tagName());
        assertEquals("p", parent.child(1).tagName());
        assertEquals("span", parent.child(2).tagName());
    }

    @Test(timeout = 4000)
    public void testEmpty() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        parent.empty();
        assertEquals(0, parent.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testWrap() {
        Element el = new Element(Tag.valueOf("span"), "http://example.com");
        el.wrap("<div></div>");
        assertEquals("div", el.parent().tagName());
    }

    @Test(timeout = 4000)
    public void testCssSelectorWithId() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("id", "myId");
        assertEquals("#myId", el.cssSelector());
    }

    @Test(timeout = 4000)
    public void testCssSelectorWithoutId() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = parent.appendElement("p");
        child.addClass("foo");
        String selector = child.cssSelector();
        assertTrue(selector.contains("p.foo"));
    }

    @Test(timeout = 4000)
    public void testSiblingElements() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = parent.appendElement("p");
        Element b = parent.appendElement("span");
        Element c = parent.appendElement("div");
        Elements siblings = b.siblingElements();
        assertEquals(2, siblings.size());
        assertSame(a, siblings.get(0));
        assertSame(c, siblings.get(1));
    }

    @Test(timeout = 4000)
    public void testNextPreviousElementSibling() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = parent.appendElement("p");
        Element b = parent.appendElement("span");
        Element c = parent.appendElement("div");
        assertSame(b, a.nextElementSibling());
        assertSame(a, b.previousElementSibling());
        assertNull(c.nextElementSibling());
        assertNull(a.previousElementSibling());
    }

    @Test(timeout = 4000)
    public void testFirstLastElementSibling() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = parent.appendElement("p");
        Element b = parent.appendElement("span");
        assertSame(a, a.firstElementSibling());
        assertSame(b, b.lastElementSibling());
    }

    @Test(timeout = 4000)
    public void testElementSiblingIndex() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = parent.appendElement("p");
        Element b = parent.appendElement("span");
        assertEquals(0, a.elementSiblingIndex().intValue());
        assertEquals(1, b.elementSiblingIndex().intValue());
    }

    @Test(timeout = 4000)
    public void testGetElementsByTag() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        el.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        Elements ps = el.getElementsByTag("p");
        assertEquals(2, ps.size());
    }

    @Test(timeout = 4000)
    public void testGetElementById() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = el.appendElement("p");
        child.attr("id", "myId");
        assertSame(child, el.getElementById("myId"));
        assertNull(el.getElementById("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetElementsByClass() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = el.appendElement("p");
        child.addClass("foo");
        Elements found = el.getElementsByClass("foo");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttribute() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("href", "http://example.com");
        Elements found = el.getElementsByAttribute("href");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeStarting() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("data-test", "value");
        Elements found = el.getElementsByAttributeStarting("data-");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValue() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "test");
        Elements found = el.getElementsByAttributeValue("class", "test");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueNot() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "test");
        Elements found = el.getElementsByAttributeValueNot("class", "other");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueStarting() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("href", "http://example.com");
        Elements found = el.getElementsByAttributeValueStarting("href", "http");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueEnding() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("href", "http://example.com");
        Elements found = el.getElementsByAttributeValueEnding("href", ".com");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueContaining() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("href", "http://example.com");
        Elements found = el.getElementsByAttributeValueContaining("href", "example");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatchingPattern() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("href", "http://example.com");
        Elements found = el.getElementsByAttributeValueMatching("href", Pattern.compile("http.*"));
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatchingString() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("href", "http://example.com");
        Elements found = el.getElementsByAttributeValueMatching("href", "http.*");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexLessThan() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendElement("p");
        parent.appendElement("span");
        Elements less = parent.getElementsByIndexLessThan(1);
        assertEquals(1, less.size());
        assertEquals("p", less.get(0).tagName());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexGreaterThan() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendElement("p");
        parent.appendElement("span");
        Elements greater = parent.getElementsByIndexGreaterThan(0);
        assertEquals(1, greater.size());
        assertEquals("span", greater.get(0).tagName());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexEquals() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendElement("p");
        parent.appendElement("span");
        Elements equals = parent.getElementsByIndexEquals(1);
        assertEquals(1, equals.size());
        assertEquals("span", equals.get(0).tagName());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingText() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(new TextNode("Hello World", "http://example.com"));
        Elements found = el.getElementsContainingText("world");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingOwnText() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(new TextNode("Hello", "http://example.com"));
        Elements found = el.getElementsContainingOwnText("Hello");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingTextPattern() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(new TextNode("Hello", "http://example.com"));
        Elements found = el.getElementsMatchingText(Pattern.compile("Hello"));
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingTextString() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(new TextNode("Hello", "http://example.com"));
        Elements found = el.getElementsMatchingText("Hello");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingOwnTextPattern() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(new TextNode("Hello", "http://example.com"));
        Elements found = el.getElementsMatchingOwnText(Pattern.compile("Hello"));
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingOwnTextString() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(new TextNode("Hello", "http://example.com"));
        Elements found = el.getElementsMatchingOwnText("Hello");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetAllElements() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        Elements all = el.getAllElements();
        assertEquals(2, all.size()); // includes self
    }

    @Test(timeout = 4000)
    public void testText() {
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        el.appendChild(new TextNode("Hello ", "http://example.com"));
        el.appendChild(new Element(Tag.valueOf("b"), "http://example.com").appendText("there"));
        el.appendChild(new TextNode(" now!", "http://example.com"));
        assertEquals("Hello there now!", el.text());
    }

    @Test(timeout = 4000)
    public void testOwnText() {
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        el.appendChild(new TextNode("Hello ", "http://example.com"));
        el.appendChild(new Element(Tag.valueOf("b"), "http://example.com").appendText("there"));
        el.appendChild(new TextNode(" now!", "http://example.com"));
        assertEquals("Hello  now!", el.ownText());
    }

    @Test(timeout = 4000)
    public void testHasText() {
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        assertFalse(el.hasText());
        el.appendChild(new TextNode(" ", "http://example.com"));
        assertFalse(el.hasText()); // whitespace only
        el.appendChild(new TextNode("a", "http://example.com"));
        assertTrue(el.hasText());
    }

    @Test(timeout = 4000)
    public void testData() {
        Element el = new Element(Tag.valueOf("script"), "http://example.com");
        el.appendChild(new DataNode("alert(1);", "http://example.com"));
        assertEquals("alert(1);", el.data());
    }

    @Test(timeout = 4000)
    public void testClassName() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("", el.className());
        el.attr("class", "foo bar");
        assertEquals("foo bar", el.className());
    }

    @Test(timeout = 4000)
    public void testClassNames() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertTrue(el.classNames().isEmpty());
        el.attr("class", "foo bar");
        Set<String> names = el.classNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("foo"));
        assertTrue(names.contains("bar"));
    }

    @Test(timeout = 4000)
    public void testClassNamesSet() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        Set<String> newClasses = new LinkedHashSet<>(Arrays.asList("a", "b"));
        el.classNames(newClasses);
        assertEquals("a b", el.attr("class"));
    }

    @Test(timeout = 4000)
    public void testHasClass() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertFalse(el.hasClass("foo"));
        el.attr("class", "foo bar");
        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));
        assertFalse(el.hasClass("baz"));
    }

    @Test(timeout = 4000)
    public void testAddClass() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.addClass("foo");
        assertEquals("foo", el.attr("class"));
        el.addClass("bar");
        assertEquals("foo bar", el.attr("class"));
    }

    @Test(timeout = 4000)
    public void testRemoveClass() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "foo bar");
        el.removeClass("foo");
        assertEquals("bar", el.attr("class"));
    }

    @Test(timeout = 4000)
    public void testToggleClass() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.toggleClass("foo");
        assertEquals("foo", el.attr("class"));
        el.toggleClass("foo");
        assertEquals("", el.attr("class"));
    }

    @Test(timeout = 4000)
    public void testVal() {
        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        assertEquals("", input.val());
        input.attr("value", "test");
        assertEquals("test", input.val());

        Element textarea = new Element(Tag.valueOf("textarea"), "http://example.com");
        assertEquals("", textarea.val());
        textarea.text("content");
        assertEquals("content", textarea.val());
    }

    @Test(timeout = 4000)
    public void testValSetter() {
        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        input.val("newValue");
        assertEquals("newValue", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "http://example.com");
        textarea.val("newContent");
        assertEquals("newContent", textarea.text());
    }

    @Test(timeout = 4000)
    public void testHtml() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(new Element(Tag.valueOf("p"), "http://example.com").appendText("Hello"));
        String html = el.html();
        assertTrue(html.contains("<p>Hello</p>"));
    }

    @Test(timeout = 4000)
    public void testHtmlSetter() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.html("<p>New</p>");
        assertEquals("New", el.text());
        assertEquals("p", el.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testOuterHtml() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "test");
        String outer = el.outerHtml();
        assertTrue(outer.contains("<div class=\"test\">"));
        assertTrue(outer.contains("</div>"));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTagNameEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.tagName("");
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAppendChildNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testPrependChildNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.prependChild(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testInsertChildrenNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.insertChildren(0, null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInsertChildrenOutOfBounds() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.insertChildren(5, Collections.singletonList(new Element(Tag.valueOf("p"), "http://example.com")));
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAppendNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.append(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testPrependNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.prepend(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testTextNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.text(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testClassNamesNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.classNames(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAddClassNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.addClass(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testRemoveClassNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.removeClass(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testToggleClassNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.toggleClass(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByTagEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsByTag("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementByIdEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementById("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByClassEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsByClass("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsByAttribute("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeStartingEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsByAttributeStarting("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeValueMatchingInvalidRegex() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsByAttributeValueMatching("href", "[invalid");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsMatchingTextInvalidRegex() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsMatchingText("[invalid");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsMatchingOwnTextInvalidRegex() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsMatchingOwnText("[invalid");
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Directly targets the known defect: indexInList uses equals() instead of identity.
     * When two sibling elements have identical tag and attributes (and thus are equal),
     * elementSiblingIndex() and sibling navigation methods return wrong results.
     */
    @Test(timeout = 4000)
    public void testElementSiblingIndexWithDuplicateContent() {
        // Create a parent with two identical child elements (same tag, same attributes, same text)
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child1 = new Element(Tag.valueOf("p"), "http://example.com");
        child1.attr("class", "dup");
        child1.appendChild(new TextNode("same", "http://example.com"));
        Element child2 = new Element(Tag.valueOf("p"), "http://example.com");
        child2.attr("class", "dup");
        child2.appendChild(new TextNode("same", "http://example.com"));
        parent.appendChild(child1);
        parent.appendChild(child2);

        // Verify sibling indices
        assertEquals("First child should have index 0", Integer.valueOf(0), child1.elementSiblingIndex());
        assertEquals("Second child should have index 1", Integer.valueOf(1), child2.elementSiblingIndex());

        // Verify sibling navigation
        assertSame("nextElementSibling of first should be second", child2, child1.nextElementSibling());
        assertSame("previousElementSibling of second should be first", child1, child2.previousElementSibling());

        // Verify first/last element sibling
        assertSame("firstElementSibling should be child1", child1, child1.firstElementSibling());
        assertSame("lastElementSibling should be child2", child2, child2.lastElementSibling());

        // Verify siblingElements
        Elements siblings = child1.siblingElements();
        assertEquals(1, siblings.size());
        assertSame(child2, siblings.get(0));
    }

    @Test(timeout = 4000)
    public void testElementSiblingIndexWithTripleDuplicate() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = new Element(Tag.valueOf("span"), "http://example.com");
        Element b = new Element(Tag.valueOf("span"), "http://example.com");
        Element c = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);

        assertEquals(Integer.valueOf(0), a.elementSiblingIndex());
        assertEquals(Integer.valueOf(1), b.elementSiblingIndex());
        assertEquals(Integer.valueOf(2), c.elementSiblingIndex());

        assertSame(b, a.nextElementSibling());
        assertSame(c, b.nextElementSibling());
        assertNull(c.nextElementSibling());
        assertNull(a.previousElementSibling());
        assertSame(a, b.previousElementSibling());
        assertSame(b, c.previousElementSibling());
    }

    @Test(timeout = 4000)
    public void testGetSiblingsWithDuplicateContent() {
        // Reproduce the exact scenario from the defect report: expected:<[]is> but was:<[th]is>
        // This test checks that previousElementSibling returns the correct element when siblings have same content.
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element first = parent.appendElement("p").text("this");
        Element second = parent.appendElement("p").text("is");
        Element third = parent.appendElement("p").text("a");
        Element fourth = parent.appendElement("p").text("test");

        // Check previous sibling of "is" should be "this"
        Element prev = second.previousElementSibling();
        assertNotNull(prev);
        assertEquals("this", prev.text());
        assertSame(first, prev);

        // Check next sibling of "this" should be "is"
        Element next = first.nextElementSibling();
        assertNotNull(next);
        assertEquals("is", next.text());
        assertSame(second, next);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullTag() {
        new Element(null, "http://example.com");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullTagWithAttributes() {
        new Element(null, "http://example.com", new Attributes());
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testChildNegativeIndex() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.child(-1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testChildOutOfBounds() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.child(0);
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespace() {
        Element pre = new Element(Tag.valueOf("pre"), "http://example.com");
        pre.appendChild(new TextNode("  spaced  ", "http://example.com"));
        assertEquals("  spaced  ", pre.text());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespaceInherited() {
        Element parent = new Element(Tag.valueOf("pre"), "http://example.com");
        Element child = parent.appendElement("code");
        child.appendChild(new TextNode("  code  ", "http://example.com"));
        assertEquals("  code  ", child.text());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEquals() {
        Element a = new Element(Tag.valueOf("div"), "http://example.com");
        a.attr("class", "foo");
        Element b = new Element(Tag.valueOf("div"), "http://example.com");
        b.attr("class", "foo");
        assertEquals(a, b);
        b.attr("id", "x");
        assertNotEquals(a, b);
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        Element a = new Element(Tag.valueOf("div"), "http://example.com");
        Element b = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testClone() {
        Element original = new Element(Tag.valueOf("div"), "http://example.com");
        original.attr("class", "test");
        original.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        Element clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.tagName(), clone.tagName());
        assertEquals(original.attr("class"), clone.attr("class"));
        assertEquals(original.childNodeSize(), clone.childNodeSize());
        assertNotSame(original.child(0), clone.child(0));
    }

    @Test(timeout = 4000)
    public void testToString() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("id", "test");
        String str = el.toString();
        assertTrue(str.contains("<div id=\"test\">"));
        assertTrue(str.contains("</div>"));
    }
}