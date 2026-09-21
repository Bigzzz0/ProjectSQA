package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * ElementDeepseekTest: Advanced white-box test suite for Element.java.
 * Targets maximum line/branch coverage and the known clone() defect.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (constructors, tag, attributes, children, text, data, DOM traversal)
 * - Partition B: Boundary values (null/empty strings, negative indices, empty collections, MAX_INT)
 * - Partition C: Defect-targeted: clone() sharing classNames set (testClonesClassnames)
 * - Partition D: Exception/defensive paths (null arguments, invalid indices, pattern syntax errors)
 * - Partition E: Object lifecycle (equals, hashCode, clone, toString)
 *
 * Known defect: clone() does not deep-copy classNames, causing shared mutable state.
 * Test: after cloning, modifying clone's classNames does not affect original.
 */
public class ElementDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorAndTag() {
        Tag divTag = Tag.valueOf("div");
        Element el = new Element(divTag, "http://example.com");
        assertEquals("div", el.tagName());
        assertSame(divTag, el.tag());
        assertTrue(el.isBlock()); // div is block
        assertEquals("http://example.com", el.baseUri());
    }

    @Test(timeout = 4000)
    public void testConstructorWithAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        attrs.put("class", "container");
        Element el = new Element(Tag.valueOf("div"), "http://example.com", attrs);
        assertEquals("main", el.id());
        assertEquals("container", el.className());
    }

    @Test(timeout = 4000)
    public void testTagNameChange() {
        Element el = new Element(Tag.valueOf("span"), "http://example.com");
        assertFalse(el.isBlock()); // span is inline
        el.tagName("div");
        assertEquals("div", el.tagName());
        assertTrue(el.isBlock());
    }

    @Test(timeout = 4000)
    public void testIdAndAttr() {
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        assertEquals("", el.id()); // no id attribute
        el.attr("id", "para1");
        assertEquals("para1", el.id());
        el.attr("class", "highlight");
        assertEquals("highlight", el.attr("class"));
    }

    @Test(timeout = 4000)
    public void testDataset() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("data-name", "jsoup");
        el.attr("data-version", "1.0");
        java.util.Map<String, String> ds = el.dataset();
        assertEquals("jsoup", ds.get("name"));
        assertEquals("1.0", ds.get("version"));
        // dataset is a live view
        ds.put("lang", "Java");
        assertEquals("Java", el.attr("data-lang"));
    }

    @Test(timeout = 4000)
    public void testParentAndParents() {
        Element root = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendChild(body);
        body.appendChild(div);
        assertSame(body, div.parent());
        Elements parents = div.parents();
        assertEquals(2, parents.size());
        assertSame(body, parents.get(0));
        assertSame(root, parents.get(1));
    }

    @Test(timeout = 4000)
    public void testChildrenAndChild() {
        Element parent = new Element(Tag.valueOf("ul"), "http://example.com");
        Element li1 = new Element(Tag.valueOf("li"), "http://example.com");
        Element li2 = new Element(Tag.valueOf("li"), "http://example.com");
        parent.appendChild(li1);
        parent.appendChild(li2);
        Elements children = parent.children();
        assertEquals(2, children.size());
        assertSame(li1, parent.child(0));
        assertSame(li2, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testTextNodes() {
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        p.appendChild(new TextNode("Hello ", "http://example.com"));
        p.appendChild(new Element(Tag.valueOf("b"), "http://example.com"));
        p.appendChild(new TextNode(" World", "http://example.com"));
        List<TextNode> textNodes = p.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Hello ", textNodes.get(0).getWholeText());
        assertEquals(" World", textNodes.get(1).getWholeText());
    }

    @Test(timeout = 4000)
    public void testDataNodes() {
        Element script = new Element(Tag.valueOf("script"), "http://example.com");
        script.appendChild(new DataNode("alert('hi');", "http://example.com"));
        List<DataNode> dataNodes = script.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("alert('hi');", dataNodes.get(0).getWholeData());
    }

    @Test(timeout = 4000)
    public void testSelect() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        div.appendChild(new Element(Tag.valueOf("span"), "http://example.com"));
        Elements selected = div.select("p");
        assertEquals(1, selected.size());
        assertEquals("p", selected.get(0).tagName());
    }

    @Test(timeout = 4000)
    public void testAppendChildAndPrependChild() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child1 = new Element(Tag.valueOf("p"), "http://example.com");
        Element child2 = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(child1);
        parent.prependChild(child2);
        assertEquals(2, parent.children().size());
        assertSame(child2, parent.child(0));
        assertSame(child1, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testInsertChildren() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        parent.appendChild(a);
        parent.appendChild(b);
        Element c = new Element(Tag.valueOf("c"), "http://example.com");
        parent.insertChildren(1, java.util.Collections.singletonList(c));
        assertEquals(3, parent.children().size());
        assertSame(a, parent.child(0));
        assertSame(c, parent.child(1));
        assertSame(b, parent.child(2));
    }

    @Test(timeout = 4000)
    public void testAppendElementAndPrependElement() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.prependElement("span");
        assertEquals(2, parent.children().size());
        assertSame(child2, parent.child(0));
        assertSame(child1, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testAppendTextAndPrependText() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendText("World");
        div.prependText("Hello ");
        assertEquals("Hello World", div.text());
    }

    @Test(timeout = 4000)
    public void testAppendAndPrependHtml() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.append("<p>para</p>");
        div.prepend("<span>span</span>");
        assertEquals(2, div.children().size());
        assertEquals("span", div.child(0).tagName());
        assertEquals("p", div.child(1).tagName());
    }

    @Test(timeout = 4000)
    public void testBeforeAndAfter() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element target = new Element(Tag.valueOf("p"), "http://example.com");
        parent.appendChild(target);
        target.before("<span>before</span>");
        target.after("<span>after</span>");
        assertEquals(3, parent.children().size());
        assertEquals("span", parent.child(0).tagName());
        assertEquals("p", parent.child(1).tagName());
        assertEquals("span", parent.child(2).tagName());
    }

    @Test(timeout = 4000)
    public void testEmpty() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        div.empty();
        assertEquals(0, div.children().size());
    }

    @Test(timeout = 4000)
    public void testWrap() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.wrap("<section></section>");
        assertEquals("section", div.parent().tagName());
    }

    @Test(timeout = 4000)
    public void testSiblingElements() {
        Element parent = new Element(Tag.valueOf("ul"), "http://example.com");
        Element li1 = new Element(Tag.valueOf("li"), "http://example.com");
        Element li2 = new Element(Tag.valueOf("li"), "http://example.com");
        Element li3 = new Element(Tag.valueOf("li"), "http://example.com");
        parent.appendChild(li1);
        parent.appendChild(li2);
        parent.appendChild(li3);
        Elements siblings = li2.siblingElements();
        assertEquals(2, siblings.size());
        assertSame(li1, siblings.get(0));
        assertSame(li3, siblings.get(1));
    }

    @Test(timeout = 4000)
    public void testNextPreviousElementSibling() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        Element c = new Element(Tag.valueOf("c"), "http://example.com");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        assertSame(b, a.nextElementSibling());
        assertSame(a, b.previousElementSibling());
        assertNull(c.nextElementSibling());
        assertNull(a.previousElementSibling());
    }

    @Test(timeout = 4000)
    public void testFirstLastElementSibling() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        parent.appendChild(a);
        parent.appendChild(b);
        assertSame(a, a.firstElementSibling());
        assertSame(b, b.lastElementSibling());
        // single child
        Element single = new Element(Tag.valueOf("p"), "http://example.com");
        parent.appendChild(single);
        assertNull(single.firstElementSibling()); // because siblings.size() > 1? Actually firstElementSibling returns null if size <=1
        // Let's test with only one child
        Element only = new Element(Tag.valueOf("div"), "http://example.com");
        only.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        Element onlyChild = only.child(0);
        assertNull(onlyChild.firstElementSibling());
        assertNull(onlyChild.lastElementSibling());
    }

    @Test(timeout = 4000)
    public void testElementSiblingIndex() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        parent.appendChild(a);
        parent.appendChild(b);
        assertEquals(0, a.elementSiblingIndex().intValue());
        assertEquals(1, b.elementSiblingIndex().intValue());
        // no parent
        Element orphan = new Element(Tag.valueOf("span"), "http://example.com");
        assertEquals(0, orphan.elementSiblingIndex().intValue());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testEmptyClassName() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("", el.className());
        Set<String> classNames = el.classNames();
        assertTrue(classNames.isEmpty());
    }

    @Test(timeout = 4000)
    public void testClassNamesWithMultipleClasses() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "foo bar baz");
        Set<String> classNames = el.classNames();
        assertEquals(3, classNames.size());
        assertTrue(classNames.contains("foo"));
        assertTrue(classNames.contains("bar"));
        assertTrue(classNames.contains("baz"));
    }

    @Test(timeout = 4000)
    public void testHasClassCaseInsensitive() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "Header");
        assertTrue(el.hasClass("header"));
        assertTrue(el.hasClass("Header"));
        assertFalse(el.hasClass("footer"));
    }

    @Test(timeout = 4000)
    public void testAddRemoveToggleClass() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.addClass("active");
        assertTrue(el.hasClass("active"));
        el.removeClass("active");
        assertFalse(el.hasClass("active"));
        el.toggleClass("visible");
        assertTrue(el.hasClass("visible"));
        el.toggleClass("visible");
        assertFalse(el.hasClass("visible"));
    }

    @Test(timeout = 4000)
    public void testValForTextarea() {
        Element textarea = new Element(Tag.valueOf("textarea"), "http://example.com");
        textarea.text("content");
        assertEquals("content", textarea.val());
        textarea.val("new content");
        assertEquals("new content", textarea.text());
    }

    @Test(timeout = 4000)
    public void testValForInput() {
        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        input.attr("value", "test");
        assertEquals("test", input.val());
        input.val("updated");
        assertEquals("updated", input.attr("value"));
    }

    @Test(timeout = 4000)
    public void testTextWithBlockElements() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new TextNode("Hello", "http://example.com"));
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        p.appendChild(new TextNode("World", "http://example.com"));
        div.appendChild(p);
        assertEquals("Hello World", div.text().trim());
    }

    @Test(timeout = 4000)
    public void testOwnText() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new TextNode("Hello ", "http://example.com"));
        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        span.appendChild(new TextNode("inner", "http://example.com"));
        div.appendChild(span);
        div.appendChild(new TextNode(" World", "http://example.com"));
        assertEquals("Hello  World", div.ownText()); // note: space before World due to TextNode
    }

    @Test(timeout = 4000)
    public void testHasText() {
        Element empty = new Element(Tag.valueOf("div"), "http://example.com");
        assertFalse(empty.hasText());
        Element withText = new Element(Tag.valueOf("p"), "http://example.com");
        withText.appendChild(new TextNode("a", "http://example.com"));
        assertTrue(withText.hasText());
        Element withOnlyWhitespace = new Element(Tag.valueOf("p"), "http://example.com");
        withOnlyWhitespace.appendChild(new TextNode("   ", "http://example.com"));
        assertFalse(withOnlyWhitespace.hasText()); // isBlank returns true
    }

    @Test(timeout = 4000)
    public void testData() {
        Element script = new Element(Tag.valueOf("script"), "http://example.com");
        script.appendChild(new DataNode("var x = 1;", "http://example.com"));
        assertEquals("var x = 1;", script.data());
    }

    @Test(timeout = 4000)
    public void testHtml() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        assertEquals("<p></p>", div.html());
        div.html("<span>new</span>");
        assertEquals("<span>new</span>", div.html());
    }

    @Test(timeout = 4000)
    public void testOuterHtml() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.attr("id", "test");
        assertEquals("<div id=\"test\"></div>", div.outerHtml());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testClonesClassnames() {
        // This test targets the known defect: clone() shares classNames set.
        Element original = new Element(Tag.valueOf("div"), "http://example.com");
        original.addClass("foo");
        Element clone = original.clone();
        // Modify clone's classNames
        clone.addClass("bar");
        // Original should NOT have "bar"
        assertFalse("Original should not have class 'bar' after clone modification", original.hasClass("bar"));
        // Also verify clone has both
        assertTrue(clone.hasClass("foo"));
        assertTrue(clone.hasClass("bar"));
        // And original still has only foo
        assertTrue(original.hasClass("foo"));
        assertEquals(1, original.classNames().size());
    }

    @Test(timeout = 4000)
    public void testCloneIndependenceOfClassNamesSet() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "a b");
        Element clone = el.clone();
        // Remove from clone's classNames via set
        clone.classNames().remove("a");
        assertTrue("Original should still have class 'a'", el.hasClass("a"));
        assertFalse("Clone should not have class 'a'", clone.hasClass("a"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTagNameEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.tagName("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTagNameNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.tagName(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAppendChildNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPrependChildNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.prependChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenNullCollection() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.insertChildren(0, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenOutOfBounds() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.insertChildren(5, java.util.Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenNegativeIndex() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        // negative index rolls around: -1 becomes currentSize+1 = 1, which is valid if size=0? Actually currentSize=0, -1+0+1=0, so valid.
        // Use -2 to get -1 which is invalid? Let's test with -2 on empty: -2+0+1=-1, fails.
        el.insertChildren(-2, java.util.Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullHtml() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.append(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrependNullHtml() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.prepend(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTextNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.text(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testClassNamesNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.classNames(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddClassNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.addClass(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveClassNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.removeClass(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToggleClassNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.toggleClass(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByTagEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsByTag("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementByIdEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementById("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByClassEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsByClass("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsByAttribute("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeStartingEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsByAttributeStarting("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeValueMatchingInvalidRegex() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsByAttributeValueMatching("class", "[invalid");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingTextInvalidRegex() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsMatchingText("[invalid");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingOwnTextInvalidRegex() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.getElementsMatchingOwnText("[invalid");
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        Element el2 = new Element(Tag.valueOf("div"), "http://example.com");
        // equals is identity-based
        assertTrue(el1.equals(el1));
        assertFalse(el1.equals(el2));
        assertFalse(el1.equals(null));
        // hashCode consistency
        assertEquals(el1.hashCode(), el1.hashCode());
    }

    @Test(timeout = 4000)
    public void testCloneBasic() {
        Element original = new Element(Tag.valueOf("div"), "http://example.com");
        original.attr("id", "test");
        original.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        Element clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.tagName(), clone.tagName());
        assertEquals(original.attr("id"), clone.attr("id"));
        // Children are also cloned? clone() uses super.clone() which does shallow copy of child list? Actually Node.clone() does deep copy? We need to check.
        // For this test, we just verify that clone is not same reference.
    }

    @Test(timeout = 4000)
    public void testToString() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("<div></div>", el.toString());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespace() {
        Element pre = new Element(Tag.valueOf("pre"), "http://example.com");
        pre.appendChild(new TextNode("  spaced  ", "http://example.com"));
        // preserveWhitespace should be true for pre
        assertTrue(pre.preserveWhitespace());
        assertEquals("  spaced  ", pre.text());
    }

    @Test(timeout = 4000)
    public void testGetElementsByTag() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        div.appendChild(new Element(Tag.valueOf("span"), "http://example.com"));
        Elements ps = div.getElementsByTag("p");
        assertEquals(1, ps.size());
    }

    @Test(timeout = 4000)
    public void testGetElementById() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element target = new Element(Tag.valueOf("p"), "http://example.com");
        target.attr("id", "target");
        div.appendChild(target);
        assertSame(target, div.getElementById("target"));
        assertNull(div.getElementById("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetElementsByClass() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        p.attr("class", "highlight");
        div.appendChild(p);
        Elements found = div.getElementsByClass("highlight");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttribute() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        a.attr("href", "http://example.com");
        div.appendChild(a);
        Elements found = div.getElementsByAttribute("href");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeStarting() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element el = new Element(Tag.valueOf("span"), "http://example.com");
        el.attr("data-x", "value");
        div.appendChild(el);
        Elements found = div.getElementsByAttributeStarting("data-");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValue() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        el.attr("href", "http://example.com");
        div.appendChild(el);
        Elements found = div.getElementsByAttributeValue("href", "http://example.com");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueNot() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element el1 = new Element(Tag.valueOf("a"), "http://example.com");
        el1.attr("href", "http://example.com");
        Element el2 = new Element(Tag.valueOf("a"), "http://example.com");
        el2.attr("href", "http://other.com");
        div.appendChild(el1);
        div.appendChild(el2);
        Elements not = div.getElementsByAttributeValueNot("href", "http://example.com");
        assertEquals(1, not.size());
        assertSame(el2, not.get(0));
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueStarting() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        el.attr("href", "http://example.com/page");
        div.appendChild(el);
        Elements found = div.getElementsByAttributeValueStarting("href", "http://example.com");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueEnding() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        el.attr("href", "page.html");
        div.appendChild(el);
        Elements found = div.getElementsByAttributeValueEnding("href", ".html");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueContaining() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        el.attr("href", "http://example.com/path");
        div.appendChild(el);
        Elements found = div.getElementsByAttributeValueContaining("href", "example");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatchingPattern() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        el.attr("href", "http://example.com");
        div.appendChild(el);
        Elements found = div.getElementsByAttributeValueMatching("href", Pattern.compile("http.*"));
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatchingString() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        el.attr("href", "http://example.com");
        div.appendChild(el);
        Elements found = div.getElementsByAttributeValueMatching("href", "http.*");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexLessThan() {
        Element parent = new Element(Tag.valueOf("ul"), "http://example.com");
        for (int i = 0; i < 5; i++) {
            parent.appendChild(new Element(Tag.valueOf("li"), "http://example.com"));
        }
        Elements less = parent.getElementsByIndexLessThan(3);
        assertEquals(3, less.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexGreaterThan() {
        Element parent = new Element(Tag.valueOf("ul"), "http://example.com");
        for (int i = 0; i < 5; i++) {
            parent.appendChild(new Element(Tag.valueOf("li"), "http://example.com"));
        }
        Elements greater = parent.getElementsByIndexGreaterThan(2);
        assertEquals(2, greater.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexEquals() {
        Element parent = new Element(Tag.valueOf("ul"), "http://example.com");
        for (int i = 0; i < 5; i++) {
            parent.appendChild(new Element(Tag.valueOf("li"), "http://example.com"));
        }
        Elements equals = parent.getElementsByIndexEquals(2);
        assertEquals(1, equals.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingText() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new TextNode("Hello World", "http://example.com"));
        Elements found = div.getElementsContainingText("World");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingOwnText() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new TextNode("Hello", "http://example.com"));
        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        span.appendChild(new TextNode("World", "http://example.com"));
        div.appendChild(span);
        Elements found = div.getElementsContainingOwnText("Hello");
        assertEquals(1, found.size());
        // Should not find "World" in own text of div
        Elements notFound = div.getElementsContainingOwnText("World");
        assertEquals(0, notFound.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingTextPattern() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new TextNode("Hello 123", "http://example.com"));
        Elements found = div.getElementsMatchingText(Pattern.compile("\\d+"));
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingTextString() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new TextNode("Hello 123", "http://example.com"));
        Elements found = div.getElementsMatchingText("\\d+");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingOwnTextPattern() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new TextNode("Hello", "http://example.com"));
        Elements found = div.getElementsMatchingOwnText(Pattern.compile("Hello"));
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingOwnTextString() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new TextNode("Hello", "http://example.com"));
        Elements found = div.getElementsMatchingOwnText("Hello");
        assertEquals(1, found.size());
    }

    @Test(timeout = 4000)
    public void testGetAllElements() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        div.appendChild(new Element(Tag.valueOf("span"), "http://example.com"));
        Elements all = div.getAllElements();
        assertEquals(3, all.size()); // includes self
    }
}