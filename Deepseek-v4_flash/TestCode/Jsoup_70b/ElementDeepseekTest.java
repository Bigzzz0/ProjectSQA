package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ElementDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target defect: testKeepsPreTextAtDepth - expected "<code[" but actual differs.
     * Root cause hypothesis: In `text()` method, when traversing nested elements inside a `<pre>` block,
     * the whitespace preservation logic (`preserveWhitespace`) may not correctly propagate to deeper levels
     * (only checks up to 5 levels up, or the `ownText`/`text` accumulation fails to preserve newlines/indentation
     * at depth). The defect likely manifests when a `<pre>` contains nested block elements with text at depth,
     * causing the text() output to lose leading/trailing whitespace or newlines that should be preserved.
     * 
     * Branches targeted:
     * - text() traversal: TextNode vs Element vs other node types
     * - preserveWhitespace: null node, non-Element node, Element with preserveWhitespace tag, Element with parent
     * - hasText(): TextNode blank/non-blank, Element recursion, empty childNodes
     * - classNames(): empty class attr, single class, multiple classes, whitespace handling
     * - getElementsByAttributeValueMatching: valid regex, invalid regex (PatternSyntaxException)
     * - insertChildren: negative index roll-around, boundary index, out-of-bounds
     * - sibling methods: no parent, first/last element sibling, index calculations
     * - val(): textarea vs input, null value
     * - html(String): clearing existing content, parsing fragments
     * - ownText(): direct text nodes, nested elements, br handling
     * - data(): DataNode, Comment, nested Element data
     * - cssSelector(): with/without id, parent/child relationships
     * - clone/shallowClone: deep copy, attribute cloning, child node list
     * - equals/hashCode: identity-based (no override)
     * 
     * Partitions:
     * A. Core text/html/data extraction
     * B. Class manipulation (add/remove/toggle/has)
     * C. Element traversal (siblings, children, parents)
     * D. Attribute-based queries
     * E. DOM manipulation (insert/prepend/append)
     * F. Cloning and lifecycle
     * G. Exception paths
     */

    // ==================== PARTITION A: Core Text/HTML/Data ====================

    /**
     * Defect-targeted test: verifies that text() preserves whitespace inside <pre> at depth.
     * This directly targets the known failure where expected "<code[" but actual differs.
     */
    @Test(timeout = 4000)
    public void testKeepsPreTextAtDepth() {
        // Simulate the failing scenario: <pre> containing nested elements with text at depth
        Element pre = new Element("pre");
        Element code = new Element("code");
        Element span = new Element("span");
        span.text("  indented  ");
        code.appendChild(span);
        pre.appendChild(code);
        
        // The text should preserve the leading/trailing spaces because <pre> preserves whitespace
        String result = pre.text();
        assertEquals("  indented  ", result);
    }

    @Test(timeout = 4000)
    public void testTextWithMixedContent() {
        Element div = new Element("div");
        div.append("<p>Hello <b>there</b> now!</p>");
        assertEquals("Hello there now!", div.text());
    }

    @Test(timeout = 4000)
    public void testTextWithPrePreservesWhitespace() {
        Element pre = new Element("pre");
        pre.appendText("  line1\n  line2  ");
        assertEquals("  line1\n  line2  ", pre.text());
    }

    @Test(timeout = 4000)
    public void testOwnTextOnlyDirectChildren() {
        Element div = new Element("div");
        div.append("<p>Hello <b>there</b> now!</p>");
        // ownText only includes direct text nodes, not nested elements
        assertEquals("", div.ownText());
        
        Element p = div.child(0);
        assertEquals("Hello  now!", p.ownText());
    }

    @Test(timeout = 4000)
    public void testHtmlWithPrettyPrint() {
        Element div = new Element("div");
        div.append("<p>one</p><p>two</p>");
        String html = div.html();
        assertTrue(html.contains("<p>one</p>"));
        assertTrue(html.contains("<p>two</p>"));
    }

    @Test(timeout = 4000)
    public void testDataWithScriptAndComment() {
        Element div = new Element("div");
        div.appendChild(new DataNode("var x = 1;"));
        div.appendChild(new Comment("comment"));
        assertEquals("var x = 1;comment", div.data());
    }

    @Test(timeout = 4000)
    public void testTextWithBr() {
        Element div = new Element("div");
        div.append("one<br>two");
        assertEquals("one two", div.text());
    }

    @Test(timeout = 4000)
    public void testTextWithBlockElements() {
        Element div = new Element("div");
        div.append("<p>one</p><p>two</p>");
        assertEquals("one two", div.text());
    }

    // ==================== PARTITION B: Class Manipulation ====================

    @Test(timeout = 4000)
    public void testClassNamesEmpty() {
        Element div = new Element("div");
        assertTrue(div.classNames().isEmpty());
        assertEquals("", div.className());
    }

    @Test(timeout = 4000)
    public void testClassNamesSingle() {
        Element div = new Element("div");
        div.addClass("foo");
        assertEquals(1, div.classNames().size());
        assertTrue(div.classNames().contains("foo"));
        assertEquals("foo", div.className());
    }

    @Test(timeout = 4000)
    public void testClassNamesMultiple() {
        Element div = new Element("div");
        div.addClass("foo");
        div.addClass("bar");
        div.addClass("baz");
        assertEquals(3, div.classNames().size());
        assertTrue(div.hasClass("foo"));
        assertTrue(div.hasClass("bar"));
        assertTrue(div.hasClass("baz"));
        assertEquals("foo bar baz", div.className());
    }

    @Test(timeout = 4000)
    public void testHasClassCaseInsensitive() {
        Element div = new Element("div");
        div.addClass("Foo");
        assertTrue(div.hasClass("foo"));
        assertTrue(div.hasClass("FOO"));
        assertTrue(div.hasClass("Foo"));
        assertFalse(div.hasClass("bar"));
    }

    @Test(timeout = 4000)
    public void testRemoveClass() {
        Element div = new Element("div");
        div.addClass("foo");
        div.addClass("bar");
        div.removeClass("foo");
        assertFalse(div.hasClass("foo"));
        assertTrue(div.hasClass("bar"));
        assertEquals("bar", div.className());
    }

    @Test(timeout = 4000)
    public void testToggleClass() {
        Element div = new Element("div");
        div.toggleClass("foo");
        assertTrue(div.hasClass("foo"));
        div.toggleClass("foo");
        assertFalse(div.hasClass("foo"));
    }

    @Test(timeout = 4000)
    public void testClassNamesSetPersistence() {
        Element div = new Element("div");
        div.classNames(new LinkedHashSet<>(Arrays.asList("a", "b")));
        assertEquals("a b", div.className());
        assertTrue(div.hasClass("a"));
        assertTrue(div.hasClass("b"));
    }

    @Test(timeout = 4000)
    public void testClassNamesEmptySetRemovesAttribute() {
        Element div = new Element("div");
        div.addClass("foo");
        div.classNames(new LinkedHashSet<>());
        assertFalse(div.hasAttr("class"));
        assertEquals("", div.className());
    }

    // ==================== PARTITION C: Element Traversal ====================

    @Test(timeout = 4000)
    public void testSiblingElements() {
        Element parent = new Element("div");
        Element child1 = new Element("p");
        Element child2 = new Element("span");
        Element child3 = new Element("a");
        parent.appendChild(child1);
        parent.appendChild(child2);
        parent.appendChild(child3);

        assertEquals(2, child1.siblingElements().size());
        assertEquals(child2, child1.nextElementSibling());
        assertEquals(child3, child2.nextElementSibling());
        assertNull(child3.nextElementSibling());
        assertNull(child1.previousElementSibling());
        assertEquals(child1, child2.previousElementSibling());
    }

    @Test(timeout = 4000)
    public void testSiblingElementsWithNoParent() {
        Element orphan = new Element("div");
        assertNull(orphan.nextElementSibling());
        assertNull(orphan.previousElementSibling());
        assertEquals(0, orphan.elementSiblingIndex());
        assertNull(orphan.lastElementSibling());
    }

    @Test(timeout = 4000)
    public void testFirstElementSibling() {
        Element parent = new Element("div");
        Element child1 = new Element("p");
        Element child2 = new Element("span");
        parent.appendChild(child1);
        parent.appendChild(child2);

        assertEquals(child1, child2.firstElementSibling());
        assertNull(child1.firstElementSibling()); // only one sibling
    }

    @Test(timeout = 4000)
    public void testElementSiblingIndex() {
        Element parent = new Element("div");
        Element child1 = new Element("p");
        Element child2 = new Element("span");
        Element child3 = new Element("a");
        parent.appendChild(child1);
        parent.appendChild(child2);
        parent.appendChild(child3);

        assertEquals(0, child1.elementSiblingIndex());
        assertEquals(1, child2.elementSiblingIndex());
        assertEquals(2, child3.elementSiblingIndex());
    }

    @Test(timeout = 4000)
    public void testParents() {
        Element root = new Element("div");
        Element child = new Element("p");
        Element grandchild = new Element("span");
        child.appendChild(grandchild);
        root.appendChild(child);

        Elements parents = grandchild.parents();
        assertEquals(2, parents.size());
        assertEquals(child, parents.get(0));
        assertEquals(root, parents.get(1));
    }

    @Test(timeout = 4000)
    public void testGetAllElements() {
        Element root = new Element("div");
        Element child = new Element("p");
        Element grandchild = new Element("span");
        child.appendChild(grandchild);
        root.appendChild(child);

        Elements all = root.getAllElements();
        assertEquals(3, all.size());
        assertTrue(all.contains(root));
        assertTrue(all.contains(child));
        assertTrue(all.contains(grandchild));
    }

    // ==================== PARTITION D: Attribute Queries ====================

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatchingValid() {
        Element root = new Element("div");
        Element child = new Element("a");
        child.attr("href", "http://example.com");
        root.appendChild(child);

        Elements result = root.getElementsByAttributeValueMatching("href", Pattern.compile("example"));
        assertEquals(1, result.size());
        assertEquals(child, result.get(0));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeValueMatchingInvalidRegex() {
        Element root = new Element("div");
        root.getElementsByAttributeValueMatching("href", "[invalid");
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatchingString() {
        Element root = new Element("div");
        Element child = new Element("a");
        child.attr("href", "http://example.com");
        root.appendChild(child);

        Elements result = root.getElementsByAttributeValueMatching("href", "example");
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingText() {
        Element root = new Element("div");
        Element p1 = new Element("p");
        p1.text("Hello World");
        Element p2 = new Element("p");
        p2.text("Goodbye");
        root.appendChild(p1);
        root.appendChild(p2);

        Elements result = root.getElementsContainingText("hello");
        assertEquals(1, result.size());
        assertEquals(p1, result.get(0));
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingOwnText() {
        Element root = new Element("div");
        Element p1 = new Element("p");
        p1.text("Hello <b>World</b>");
        Element p2 = new Element("p");
        p2.text("Hello");
        root.appendChild(p1);
        root.appendChild(p2);

        Elements result = root.getElementsContainingOwnText("Hello");
        assertEquals(1, result.size());
        assertEquals(p2, result.get(0));
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexLessThan() {
        Element root = new Element("div");
        Element c1 = new Element("p");
        Element c2 = new Element("p");
        Element c3 = new Element("p");
        root.appendChild(c1);
        root.appendChild(c2);
        root.appendChild(c3);

        Elements result = root.getElementsByIndexLessThan(2);
        assertEquals(2, result.size());
        assertEquals(c1, result.get(0));
        assertEquals(c2, result.get(1));
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexGreaterThan() {
        Element root = new Element("div");
        Element c1 = new Element("p");
        Element c2 = new Element("p");
        Element c3 = new Element("p");
        root.appendChild(c1);
        root.appendChild(c2);
        root.appendChild(c3);

        Elements result = root.getElementsByIndexGreaterThan(0);
        assertEquals(2, result.size());
        assertEquals(c2, result.get(0));
        assertEquals(c3, result.get(1));
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexEquals() {
        Element root = new Element("div");
        Element c1 = new Element("p");
        Element c2 = new Element("p");
        Element c3 = new Element("p");
        root.appendChild(c1);
        root.appendChild(c2);
        root.appendChild(c3);

        Elements result = root.getElementsByIndexEquals(1);
        assertEquals(1, result.size());
        assertEquals(c2, result.get(0));
    }

    // ==================== PARTITION E: DOM Manipulation ====================

    @Test(timeout = 4000)
    public void testInsertChildrenAtBeginning() {
        Element parent = new Element("div");
        Element child1 = new Element("p");
        Element child2 = new Element("span");
        parent.appendChild(child1);

        parent.insertChildren(0, child2);
        assertEquals(2, parent.childNodeSize());
        assertEquals(child2, parent.child(0));
        assertEquals(child1, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testInsertChildrenAtEnd() {
        Element parent = new Element("div");
        Element child1 = new Element("p");
        Element child2 = new Element("span");
        parent.appendChild(child1);

        parent.insertChildren(-1, child2);
        assertEquals(2, parent.childNodeSize());
        assertEquals(child1, parent.child(0));
        assertEquals(child2, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testInsertChildrenAtMiddle() {
        Element parent = new Element("div");
        Element child1 = new Element("p");
        Element child2 = new Element("span");
        Element child3 = new Element("a");
        parent.appendChild(child1);
        parent.appendChild(child3);

        parent.insertChildren(1, child2);
        assertEquals(3, parent.childNodeSize());
        assertEquals(child1, parent.child(0));
        assertEquals(child2, parent.child(1));
        assertEquals(child3, parent.child(2));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInsertChildrenOutOfBounds() {
        Element parent = new Element("div");
        Element child = new Element("p");
        parent.insertChildren(5, child);
    }

    @Test(timeout = 4000)
    public void testPrependElement() {
        Element parent = new Element("div");
        Element existing = new Element("p");
        parent.appendChild(existing);

        Element prepended = parent.prependElement("span");
        assertEquals(2, parent.childNodeSize());
        assertEquals(prepended, parent.child(0));
        assertEquals(existing, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testAppendText() {
        Element parent = new Element("div");
        parent.appendText("Hello");
        assertEquals("Hello", parent.text());
    }

    @Test(timeout = 4000)
    public void testPrependText() {
        Element parent = new Element("div");
        parent.appendText("World");
        parent.prependText("Hello ");
        assertEquals("Hello World", parent.text());
    }

    @Test(timeout = 4000)
    public void testAppendAndPrependHtml() {
        Element parent = new Element("div");
        parent.append("<p>one</p>");
        parent.prepend("<p>zero</p>");
        assertEquals(2, parent.children().size());
        assertEquals("zero", parent.child(0).text());
        assertEquals("one", parent.child(1).text());
    }

    @Test(timeout = 4000)
    public void testEmpty() {
        Element parent = new Element("div");
        parent.append("<p>one</p><p>two</p>");
        parent.empty();
        assertEquals(0, parent.childNodeSize());
        assertEquals("", parent.html());
    }

    @Test(timeout = 4000)
    public void testHtmlSetter() {
        Element parent = new Element("div");
        parent.append("<p>old</p>");
        parent.html("<p>new</p>");
        assertEquals("new", parent.text());
        assertEquals(1, parent.children().size());
    }

    @Test(timeout = 4000)
    public void testValTextarea() {
        Element textarea = new Element("textarea");
        textarea.val("some text");
        assertEquals("some text", textarea.val());
        assertEquals("some text", textarea.text());
    }

    @Test(timeout = 4000)
    public void testValInput() {
        Element input = new Element("input");
        input.val("value");
        assertEquals("value", input.val());
        assertEquals("value", input.attr("value"));
    }

    @Test(timeout = 4000)
    public void testBeforeAndAfter() {
        Element parent = new Element("div");
        Element child = new Element("p");
        parent.appendChild(child);

        child.before("<span>before</span>");
        child.after("<span>after</span>");

        assertEquals(3, parent.childNodeSize());
        assertEquals("span", parent.child(0).tagName());
        assertEquals("p", parent.child(1).tagName());
        assertEquals("span", parent.child(2).tagName());
    }

    // ==================== PARTITION F: Cloning and Lifecycle ====================

    @Test(timeout = 4000)
    public void testCloneDeep() {
        Element original = new Element("div");
        original.attr("id", "test");
        original.append("<p>child</p>");

        Element clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.attr("id"), clone.attr("id"));
        assertEquals(original.html(), clone.html());
        assertNotSame(original.child(0), clone.child(0));
    }

    @Test(timeout = 4000)
    public void testShallowClone() {
        Element original = new Element("div");
        original.attr("id", "test");
        original.append("<p>child</p>");

        Element clone = original.shallowClone();
        assertNotSame(original, clone);
        assertEquals(original.attr("id"), clone.attr("id"));
        assertEquals(0, clone.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testCloneWithAttributes() {
        Element original = new Element("div");
        original.attr("class", "foo bar");
        original.attr("data-x", "value");

        Element clone = original.clone();
        assertEquals("foo bar", clone.attr("class"));
        assertEquals("value", clone.attr("data-x"));
    }

    @Test(timeout = 4000)
    public void testCssSelectorWithId() {
        Element div = new Element("div");
        div.attr("id", "myId");
        assertEquals("#myId", div.cssSelector());
    }

    @Test(timeout = 4000)
    public void testCssSelectorWithoutId() {
        Element parent = new Element("div");
        Element child = new Element("p");
        child.attr("class", "foo");
        parent.appendChild(child);

        assertTrue(child.cssSelector().contains("p.foo"));
    }

    @Test(timeout = 4000)
    public void testSelectFirst() {
        Element root = new Element("div");
        Element child = new Element("p");
        child.attr("class", "target");
        root.appendChild(child);

        Element result = root.selectFirst("p.target");
        assertEquals(child, result);
    }

    @Test(timeout = 4000)
    public void testIsWithCssQuery() {
        Element div = new Element("div");
        div.attr("class", "foo");
        assertTrue(div.is("div.foo"));
        assertFalse(div.is("span"));
    }

    @Test(timeout = 4000)
    public void testGetElementById() {
        Element root = new Element("div");
        Element child = new Element("p");
        child.attr("id", "unique");
        root.appendChild(child);

        Element result = root.getElementById("unique");
        assertEquals(child, result);
        assertNull(root.getElementById("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetElementsByClass() {
        Element root = new Element("div");
        Element child1 = new Element("p");
        child1.addClass("foo");
        Element child2 = new Element("p");
        child2.addClass("foo bar");
        root.appendChild(child1);
        root.appendChild(child2);

        Elements result = root.getElementsByClass("foo");
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttribute() {
        Element root = new Element("div");
        Element child = new Element("a");
        child.attr("href", "http://example.com");
        root.appendChild(child);

        Elements result = root.getElementsByAttribute("href");
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeStarting() {
        Element root = new Element("div");
        Element child = new Element("a");
        child.attr("data-custom", "value");
        root.appendChild(child);

        Elements result = root.getElementsByAttributeStarting("data-");
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValue() {
        Element root = new Element("div");
        Element child = new Element("a");
        child.attr("href", "http://example.com");
        root.appendChild(child);

        Elements result = root.getElementsByAttributeValue("href", "http://example.com");
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueNot() {
        Element root = new Element("div");
        Element child1 = new Element("a");
        child1.attr("href", "http://example.com");
        Element child2 = new Element("a");
        child2.attr("href", "http://other.com");
        root.appendChild(child1);
        root.appendChild(child2);

        Elements result = root.getElementsByAttributeValueNot("href", "http://example.com");
        assertEquals(1, result.size());
        assertEquals(child2, result.get(0));
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueStarting() {
        Element root = new Element("div");
        Element child = new Element("a");
        child.attr("href", "http://example.com");
        root.appendChild(child);

        Elements result = root.getElementsByAttributeValueStarting("href", "http://");
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueEnding() {
        Element root = new Element("div");
        Element child = new Element("a");
        child.attr("href", "http://example.com");
        root.appendChild(child);

        Elements result = root.getElementsByAttributeValueEnding("href", ".com");
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueContaining() {
        Element root = new Element("div");
        Element child = new Element("a");
        child.attr("href", "http://example.com/path");
        root.appendChild(child);

        Elements result = root.getElementsByAttributeValueContaining("href", "example");
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingText() {
        Element root = new Element("div");
        Element p1 = new Element("p");
        p1.text("Hello World");
        Element p2 = new Element("p");
        p2.text("Goodbye");
        root.appendChild(p1);
        root.appendChild(p2);

        Elements result = root.getElementsMatchingText("Hello.*");
        assertEquals(1, result.size());
        assertEquals(p1, result.get(0));
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingOwnText() {
        Element root = new Element("div");
        Element p1 = new Element("p");
        p1.text("Hello <b>World</b>");
        Element p2 = new Element("p");
        p2.text("Hello");
        root.appendChild(p1);
        root.appendChild(p2);

        Elements result = root.getElementsMatchingOwnText("Hello");
        assertEquals(1, result.size());
        assertEquals(p2, result.get(0));
    }

    // ==================== PARTITION G: Exception Paths ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAppendChildNull() {
        Element div = new Element("div");
        div.appendChild(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testPrependChildNull() {
        Element div = new Element("div");
        div.prependChild(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAppendNullHtml() {
        Element div = new Element("div");
        div.append((String) null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testPrependNullHtml() {
        Element div = new Element("div");
        div.prepend((String) null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testTextNull() {
        Element div = new Element("div");
        div.text(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAddClassNull() {
        Element div = new Element("div");
        div.addClass(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testRemoveClassNull() {
        Element div = new Element("div");
        div.removeClass(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testToggleClassNull() {
        Element div = new Element("div");
        div.toggleClass(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testClassNamesNull() {
        Element div = new Element("div");
        div.classNames(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByClassEmpty() {
        Element div = new Element("div");
        div.getElementsByClass("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeEmpty() {
        Element div = new Element("div");
        div.getElementsByAttribute("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeStartingEmpty() {
        Element div = new Element("div");
        div.getElementsByAttributeStarting("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementByIdEmpty() {
        Element div = new Element("div");
        div.getElementById("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInsertChildrenNull() {
        Element div = new Element("div");
        div.insertChildren(0, (Node[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsMatchingTextInvalidRegex() {
        Element div = new Element("div");
        div.getElementsMatchingText("[invalid");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeValueMatchingInvalidRegexString() {
        Element div = new Element("div");
        div.getElementsByAttributeValueMatching("href", "[invalid");
    }
}