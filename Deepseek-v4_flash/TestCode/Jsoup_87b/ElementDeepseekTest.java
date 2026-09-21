package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Tag;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.List;

/**
 * White-box test suite for org.jsoup.nodes.Element.
 * Targets maximum line/branch coverage and the known Defects4J defect
 * in preserved case link nesting.
 *
 * [Branch & Defect Analysis Matrix]
 * Partition A: Core functional paths (constructor, tag, attributes, children, text, html, data, class methods)
 * Partition B: Boundary Value Analysis (null/empty arguments, negative indices, edge cases)
 * Partition C: Defect-targeted branch – preservedCaseLinksCantNest (reproduces the exact failing scenario)
 * Partition D: Exception/guard paths (invalid arguments, out-of-bounds, null checks)
 * Partition E: Object lifecycle (clone, shallowClone, doClone, nodelistChanged)
 *
 * All tests are deterministic and run under a 4-second timeout.
 */
public class ElementDeepseekTest {

    // ============================================================
    // Partition A: Core Functional Logic & State Transitions
    // ============================================================

    @Test(timeout = 4000)
    public void testConstructorAndTagName() {
        Element div = new Element("div");
        assertEquals("div", div.tagName());
        assertTrue(div.isBlock());
        assertEquals(0, div.childNodeSize());
        assertNotNull(div.attributes());
        assertEquals(0, div.attributes().size());
    }

    @Test(timeout = 4000)
    public void testTagNameChange() {
        Element span = new Element("span");
        span.tagName("div");
        assertEquals("div", span.tagName());
        // Case preservation default is false, so tag name becomes lowercased
    }

    @Test(timeout = 4000)
    public void testTagNameWithSettings() {
        // Uses the parser's settings; with default settings tag name is lowercased
        Element el = new Element(Tag.valueOf("DIV"), "http://example.com");
        assertEquals("div", el.tagName());
    }

    @Test(timeout = 4000)
    public void testId() {
        Element el = new Element("div");
        assertEquals("", el.id());
        el.attr("id", "testId");
        assertEquals("testId", el.id());
    }

    @Test(timeout = 4000)
    public void testAttributes() {
        Element el = new Element("div");
        assertFalse(el.hasAttributes()); // attributes is null initially
        el.attr("class", "foo");
        assertTrue(el.hasAttributes());
        assertEquals("foo", el.attr("class"));
    }

    @Test(timeout = 4000)
    public void testBaseUri() {
        Element el = new Element(Tag.valueOf("div"), "http://base.com");
        assertEquals("http://base.com", el.baseUri());
        el.doSetBaseUri("http://newbase.com");
        assertEquals("http://newbase.com", el.baseUri());
    }

    @Test(timeout = 4000)
    public void testAppendChildAndChildren() {
        Element parent = new Element("div");
        Element child = new Element("p");
        parent.appendChild(child);
        assertEquals(1, parent.childNodeSize());
        assertEquals(1, parent.children().size());
        assertSame(child, parent.child(0));
        assertSame(child, parent.children().get(0));
    }

    @Test(timeout = 4000)
    public void testPrependChild() {
        Element parent = new Element("div");
        Element first = new Element("p");
        Element second = new Element("span");
        parent.appendChild(first);
        parent.prependChild(second);
        assertEquals(2, parent.children().size());
        assertSame(second, parent.child(0));
        assertSame(first, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testInsertChildrenAtEnd() {
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        parent.appendChild(a);
        parent.insertChildren(1, b); // index 1 = end
        assertEquals(2, parent.children().size());
        assertSame(b, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testInsertChildrenAtStart() {
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        parent.appendChild(a);
        parent.insertChildren(0, b);
        assertEquals(2, parent.children().size());
        assertSame(b, parent.child(0));
    }

    @Test(timeout = 4000)
    public void testAppendElement() {
        Element parent = new Element("div");
        Element child = parent.appendElement("span");
        assertNotNull(child);
        assertEquals("span", child.tagName());
        assertEquals(1, parent.children().size());
    }

    @Test(timeout = 4000)
    public void testPrependElement() {
        Element parent = new Element("div");
        Element child = parent.prependElement("span");
        assertNotNull(child);
        assertEquals("span", child.tagName());
        assertEquals(1, parent.children().size());
        assertSame(child, parent.child(0));
    }

    @Test(timeout = 4000)
    public void testAppendText() {
        Element el = new Element("div");
        el.appendText("Hello");
        List<TextNode> textNodes = el.textNodes();
        assertEquals(1, textNodes.size());
        assertEquals("Hello", textNodes.get(0).text());
    }

    @Test(timeout = 4000)
    public void testPrependText() {
        Element el = new Element("div");
        el.appendText(" World");
        el.prependText("Hello");
        assertEquals("Hello World", el.text());
    }

    @Test(timeout = 4000)
    public void testTextGetter() {
        Element el = new Element("p");
        el.appendText("Hello  ");
        el.appendElement("b").text("there");
        el.appendText(" now! ");
        assertEquals("Hello there now!", el.text());
    }

    @Test(timeout = 4000)
    public void testWholeText() {
        Element el = new Element("p");
        el.appendText("Hello  ");
        el.appendText("there");
        assertEquals("Hello  there", el.wholeText());
    }

    @Test(timeout = 4000)
    public void testOwnText() {
        Element parent = new Element("p");
        parent.appendText("Hello ");
        Element bold = new Element("b");
        bold.appendText("there");
        parent.appendChild(bold);
        parent.appendText(" now!");
        assertEquals("Hello  now!", parent.ownText());
    }

    @Test(timeout = 4000)
    public void testTextSetter() {
        Element el = new Element("div");
        el.text("new text");
        assertEquals("new text", el.text());
        assertEquals(1, el.childNodeSize());
        assertTrue(el.childNode(0) instanceof TextNode);
    }

    @Test(timeout = 4000)
    public void testHasText() {
        Element empty = new Element("div");
        assertFalse(empty.hasText());
        Element withText = new Element("div");
        withText.appendText("x");
        assertTrue(withText.hasText());
        Element withChild = new Element("div");
        withChild.appendChild(new Element("span").text("x"));
        assertTrue(withChild.hasText());
    }

    @Test(timeout = 4000)
    public void testHtmlGetter() {
        Element div = new Element("div");
        div.appendChild(new Element("p").text("Hello"));
        String html = div.html();
        assertEquals("<p>Hello</p>", html);
    }

    @Test(timeout = 4000)
    public void testHtmlSetter() {
        Element div = new Element("div");
        div.html("<p>Hello</p>");
        assertEquals(1, div.children().size());
        assertEquals("p", div.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testData() {
        Element script = new Element("script");
        script.appendChild(new DataNode("alert('x')"));
        assertEquals("alert('x')", script.data());
    }

    @Test(timeout = 4000)
    public void testClassNameAndClassNames() {
        Element el = new Element("div");
        assertEquals("", el.className());
        assertTrue(el.classNames().isEmpty());

        el.attr("class", "foo bar");
        assertEquals("foo bar", el.className());
        Set<String> classes = el.classNames();
        assertEquals(2, classes.size());
        assertTrue(classes.contains("foo"));
        assertTrue(classes.contains("bar"));
    }

    @Test(timeout = 4000)
    public void testClassNamesSet() {
        Element el = new Element("div");
        Set<String> classes = new LinkedHashSet<>(Arrays.asList("a", "b"));
        el.classNames(classes);
        assertEquals("a b", el.attr("class"));
    }

    @Test(timeout = 4000)
    public void testClassNamesSetEmpty() {
        Element el = new Element("div");
        el.attr("class", "foo");
        el.classNames(new LinkedHashSet<String>());
        assertFalse(el.hasAttr("class"));
    }

    @Test(timeout = 4000)
    public void testHasClass() {
        Element el = new Element("div");
        el.attr("class", "foo bar");
        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));
        assertFalse(el.hasClass("baz"));
        // case insensitive
        assertTrue(el.hasClass("FOO"));
    }

    @Test(timeout = 4000)
    public void testAddClass() {
        Element el = new Element("div");
        el.addClass("foo");
        assertTrue(el.hasClass("foo"));
        el.addClass("bar");
        assertTrue(el.hasClass("bar"));
        assertEquals("foo bar", el.attr("class"));
    }

    @Test(timeout = 4000)
    public void testRemoveClass() {
        Element el = new Element("div");
        el.attr("class", "foo bar");
        el.removeClass("foo");
        assertFalse(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));
        assertEquals("bar", el.attr("class"));
    }

    @Test(timeout = 4000)
    public void testToggleClass() {
        Element el = new Element("div");
        el.attr("class", "foo");
        el.toggleClass("foo");
        assertFalse(el.hasClass("foo"));
        el.toggleClass("foo");
        assertTrue(el.hasClass("foo"));
    }

    @Test(timeout = 4000)
    public void testEmpty() {
        Element el = new Element("div");
        el.appendChild(new Element("p"));
        el.appendChild(new Element("span"));
        assertEquals(2, el.childNodeSize());
        el.empty();
        assertEquals(0, el.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testValOnTextarea() {
        Element textarea = new Element("textarea");
        textarea.text("content");
        assertEquals("content", textarea.val());
        textarea.val("newval");
        assertEquals("newval", textarea.text());
    }

    @Test(timeout = 4000)
    public void testValOnInput() {
        Element input = new Element("input");
        input.attr("value", "initial");
        assertEquals("initial", input.val());
        input.val("updated");
        assertEquals("updated", input.attr("value"));
    }

    @Test(timeout = 4000)
    public void testCssSelectorWithId() {
        Element el = new Element("div");
        el.attr("id", "myid");
        assertEquals("#myid", el.cssSelector());
    }

    @Test(timeout = 4000)
    public void testCssSelectorWithoutId() {
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        child.attr("class", "a b");
        // The selector will be something like "div > span.a.b"
        String selector = child.cssSelector();
        assertTrue(selector.startsWith("div > span"));
        assertTrue(selector.contains(".a.b"));
    }

    @Test(timeout = 4000)
    public void testSiblingElements() {
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        Element c = new Element("c");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        Elements siblings = b.siblingElements();
        assertEquals(2, siblings.size());
        assertSame(a, siblings.get(0));
        assertSame(c, siblings.get(1));
        // own element not included
        assertFalse(siblings.contains(b));
    }

    @Test(timeout = 4000)
    public void testNextElementSibling() {
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        parent.appendChild(a);
        parent.appendChild(b);
        assertSame(b, a.nextElementSibling());
        assertNull(b.nextElementSibling());
    }

    @Test(timeout = 4000)
    public void testPreviousElementSibling() {
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        parent.appendChild(a);
        parent.appendChild(b);
        assertSame(a, b.previousElementSibling());
        assertNull(a.previousElementSibling());
    }

    @Test(timeout = 4000)
    public void testFirstElementSibling() {
        Element parent = new Element("div");
        parent.appendChild(new Element("a"));
        parent.appendChild(new Element("b"));
        Element first = parent.child(0);
        assertSame(first, first.firstElementSibling());
        // when only one child, it returns null
        Element only = new Element("div");
        only.appendChild(new Element("p"));
        assertNull(only.child(0).firstElementSibling());
    }

    @Test(timeout = 4000)
    public void testLastElementSibling() {
        Element parent = new Element("div");
        parent.appendChild(new Element("a"));
        parent.appendChild(new Element("b"));
        Element last = parent.child(1);
        assertSame(last, last.lastElementSibling());
    }

    @Test(timeout = 4000)
    public void testElementSiblingIndex() {
        Element parent = new Element("div");
        Element first = new Element("a");
        Element second = new Element("b");
        parent.appendChild(first);
        parent.appendChild(second);
        assertEquals(0, first.elementSiblingIndex());
        assertEquals(1, second.elementSiblingIndex());
        // element without parent returns 0
        Element orphan = new Element("p");
        assertEquals(0, orphan.elementSiblingIndex());
    }

    @Test(timeout = 4000)
    public void testParents() {
        Element root = new Element("html");
        Element body = root.appendElement("body");
        Element div = body.appendElement("div");
        Elements parents = div.parents();
        assertEquals(2, parents.size());
        assertSame(body, parents.get(0));
        assertSame(root, parents.get(1));
    }

    @Test(timeout = 4000)
    public void testParent() {
        Element parent = new Element("div");
        Element child = parent.appendElement("p");
        assertSame(parent, child.parent());
    }

    // ============================================================
    // Partition B: Boundary Value Analysis & Edge Cases
    // ============================================================

    @Test(timeout = 4000)
    public void testEmptyTagName() {
        try {
            new Element("").tagName("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not empty"));
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullTextArgument() {
        new Element("div").text(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullAppendChild() {
        new Element("div").appendChild(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullPrependChild() {
        new Element("div").prependChild(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullAppendText() {
        new Element("div").appendText(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullAppendHtml() {
        new Element("div").append(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullPrependHtml() {
        new Element("div").prepend(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullClassNames() {
        new Element("div").classNames(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullAddClass() {
        new Element("div").addClass(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullRemoveClass() {
        new Element("div").removeClass(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullToggleClass() {
        new Element("div").toggleClass(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullInsertChildrenCollection() {
        new Element("div").insertChildren(0, (Collection<? extends Node>) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullInsertChildrenVarargs() {
        new Element("div").insertChildren(0, (Node[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullAppendTo() {
        new Element("div").appendTo(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullCssQueryForSelect() {
        new Element("div").select(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyGetElementsByTag() {
        new Element("div").getElementsByTag("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyGetElementById() {
        new Element("div").getElementById("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyGetElementsByClass() {
        new Element("div").getElementsByClass("");
    }

    @Test(timeout = 4000)
    public void testInsertChildrenNegativeIndex() {
        Element parent = new Element("div");
        parent.appendChild(new Element("a"));
        Element b = new Element("b");
        parent.insertChildren(-1, b); // roll around to end
        assertEquals(2, parent.children().size());
        assertSame(b, parent.child(1));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInsertChildrenOutOfBounds() {
        Element parent = new Element("div");
        parent.insertChildren(5, new Element("a"));
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexLessThan() {
        Element parent = new Element("div");
        parent.appendChild(new Element("a"));
        parent.appendChild(new Element("b"));
        parent.appendChild(new Element("c"));
        Elements res = parent.getElementsByIndexLessThan(2);
        assertEquals(2, res.size());
        assertEquals("a", res.get(0).tagName());
        assertEquals("b", res.get(1).tagName());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexGreaterThan() {
        Element parent = new Element("div");
        parent.appendChild(new Element("a"));
        parent.appendChild(new Element("b"));
        parent.appendChild(new Element("c"));
        Elements res = parent.getElementsByIndexGreaterThan(1);
        assertEquals(1, res.size());
        assertEquals("c", res.get(0).tagName());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexEquals() {
        Element parent = new Element("div");
        parent.appendChild(new Element("a"));
        parent.appendChild(new Element("b"));
        parent.appendChild(new Element("c"));
        Elements res = parent.getElementsByIndexEquals(1);
        assertEquals(1, res.size());
        assertEquals("b", res.get(0).tagName());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingText() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p").text("hello world"));
        Elements res = parent.getElementsContainingText("hello");
        assertEquals(1, res.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingOwnText() {
        Element parent = new Element("div");
        parent.text("parent text");
        Element child = parent.appendElement("span").text("child text");
        Elements res = parent.getElementsContainingOwnText("parent");
        assertEquals(1, res.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingTextValidPattern() {
        Element parent = new Element("div");
        parent.text("abc");
        Elements res = parent.getElementsMatchingText("a.c");
        assertEquals(1, res.size());
    }

    // ============================================================
    // Partition C: Defect-Targeted Branch Zone
    // ============================================================

    @Test(timeout = 4000)
    public void preservedCaseLinksCantNest() {
        // This test directly reproduces the known Defects4J failure for preserved case anchor nesting.
        // On the defective version, the parser incorrectly nests the second <A> inside the first.
        String html = "<A> ONE <A> Two </A> </A>";
        Document doc = Jsoup.parse(html);
        String bodyHtml = doc.body().html();
        String expected = "<A> ONE </A> <A> Two </A>";
        // Use a whitespace-normalized comparison to ignore formatting differences
        String normalizedActual = bodyHtml.replaceAll("\\s+", " ").trim();
        assertEquals(expected, normalizedActual);
    }

    // ============================================================
    // Partition D: Exception & Defensive Guard Paths
    // ============================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidRegexGetElementsByAttributeValueMatching() {
        new Element("div").getElementsByAttributeValueMatching("key", "[invalid");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidRegexGetElementsMatchingText() {
        new Element("div").getElementsMatchingText("[invalid");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidRegexGetElementsMatchingOwnText() {
        new Element("div").getElementsMatchingOwnText("[invalid");
    }

    // ============================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ============================================================

    @Test(timeout = 4000)
    public void testClone() {
        Element original = new Element("div");
        original.attr("id", "test");
        original.text("Hello");
        Element cloned = original.clone();
        assertNotSame(original, cloned);
        assertEquals(original.tagName(), cloned.tagName());
        assertEquals(original.attr("id"), cloned.attr("id"));
        assertEquals(original.text(), cloned.text());
        assertEquals(original.childNodeSize(), cloned.childNodeSize());
        // Ensure shallow copy of attributes?
        assertNotSame(original.attributes(), cloned.attributes());
    }

    @Test(timeout = 4000)
    public void testShallowClone() {
        Element original = new Element("div");
        original.attr("id", "x");
        Element shallow = original.shallowClone();
        assertEquals(original.tagName(), shallow.tagName());
        assertEquals(original.attr("id"), shallow.attr("id"));
        assertEquals(0, shallow.childNodeSize()); // no children in shallow copy
        assertNotSame(original.attributes(), shallow.attributes());
    }

    @Test(timeout = 4000)
    public void testDoCloneKeepsBaseUri() {
        Element parent = new Element("div");
        Element child = new Element("p", "http://example.com");
        parent.appendChild(child);
        Element clonedChild = child.doClone(parent);
        assertEquals("http://example.com", clonedChild.baseUri());
        // child node list should be a copy
        assertNotSame(child.childNodes, clonedChild.childNodes);
    }

    @Test(timeout = 4000)
    public void testNodelistChangedInvalidatesShadowChildren() {
        Element parent = new Element("div");
        parent.appendChild(new Element("a"));
        // Access children to create shadow list
        parent.children();
        // Now change nodelist via appendChild
        parent.appendChild(new Element("b"));
        // shadow list should be refreshed on next access
        List<Element> children = parent.children();
        assertEquals(2, children.size());
        assertEquals("b", children.get(1).tagName());
    }

    @Test(timeout = 4000)
    public void testNodeListEmptyInitial() {
        Element el = new Element("div");
        // childNodes is EMPTY_NODES initially
        assertEquals(0, el.childNodeSize());
        // ensureChildNodes creates a mutable list
        el.ensureChildNodes();
        assertNotSame(Element.EMPTY_NODES, el.childNodes);
    }
}