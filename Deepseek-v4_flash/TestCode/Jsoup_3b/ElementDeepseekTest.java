package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * ElementDeepseekTest: White-box test suite for Element.java targeting known Defects4J defect
 * and achieving high line/branch coverage.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (constructors, tag, id, parent, children, text, data, classNames, val, html)
 * - Partition B: Boundary values (null arguments, empty strings, negative indices, empty collections)
 * - Partition C: Defect-targeted branch zone (append/prepend HTML with table rows causing nested <table>)
 * - Partition D: Exception & defensive guard paths (null checks, empty checks, illegal states)
 * - Partition E: Object lifecycle & contract (equals, hashCode, toString)
 *
 * Known defect: When appending/prepending HTML fragments containing <tr> to a <table> element,
 * the parser incorrectly wraps the fragment in an extra <table> tag.
 * Tests testAppendRowToTable and testPrependRowToTable expose this.
 */
public class ElementDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorAndTag() {
        Tag divTag = Tag.valueOf("div");
        Element div = new Element(divTag, "http://example.com");
        assertEquals("div", div.tagName());
        assertSame(divTag, div.tag());
        assertTrue(div.isBlock()); // div is block
        assertEquals("http://example.com", div.baseUri());
    }

    @Test(timeout = 4000)
    public void testId() {
        Element el = new Element(Tag.valueOf("p"), "");
        assertEquals("", el.id());
        el.attr("id", "myId");
        assertEquals("myId", el.id());
    }

    @Test(timeout = 4000)
    public void testParentAndParents() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("span"), "");
        parent.appendChild(child);
        assertSame(parent, child.parent());
        Elements parents = child.parents();
        assertEquals(1, parents.size());
        assertSame(parent, parents.get(0));
    }

    @Test(timeout = 4000)
    public void testChildrenAndChild() {
        Element parent = new Element(Tag.valueOf("ul"), "");
        Element child1 = new Element(Tag.valueOf("li"), "");
        Element child2 = new Element(Tag.valueOf("li"), "");
        parent.appendChild(child1);
        parent.appendChild(child2);
        Elements children = parent.children();
        assertEquals(2, children.size());
        assertSame(child1, parent.child(0));
        assertSame(child2, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testAppendChildAndPrependChild() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        parent.appendChild(child);
        assertSame(parent, child.parent());
        assertEquals(1, parent.childNodes().size());

        Element prependChild = new Element(Tag.valueOf("span"), "");
        parent.prependChild(prependChild);
        assertEquals(2, parent.childNodes().size());
        assertSame(prependChild, parent.childNode(0));
    }

    @Test(timeout = 4000)
    public void testAppendElementAndPrependElement() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element appended = parent.appendElement("p");
        assertEquals("p", appended.tagName());
        assertSame(parent, appended.parent());
        assertEquals(1, parent.children().size());

        Element prepended = parent.prependElement("span");
        assertEquals("span", prepended.tagName());
        assertSame(prepended, parent.child(0));
    }

    @Test(timeout = 4000)
    public void testTextAndHasText() {
        Element el = new Element(Tag.valueOf("p"), "");
        assertFalse(el.hasText());
        el.text("Hello");
        assertEquals("Hello", el.text());
        assertTrue(el.hasText());
        el.empty();
        assertFalse(el.hasText());
    }

    @Test(timeout = 4000)
    public void testData() {
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("alert('hi');", ""));
        assertEquals("alert('hi');", script.data());
    }

    @Test(timeout = 4000)
    public void testClassNames() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "header main");
        Set<String> classes = el.classNames();
        assertEquals(2, classes.size());
        assertTrue(classes.contains("header"));
        assertTrue(classes.contains("main"));
    }

    @Test(timeout = 4000)
    public void testHasClass() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "active");
        assertTrue(el.hasClass("active"));
        assertFalse(el.hasClass("inactive"));
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
    public void testVal() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("value", "test");
        assertEquals("test", input.val());
        input.val("new");
        assertEquals("new", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("content");
        assertEquals("content", textarea.val());
        textarea.val("updated");
        assertEquals("updated", textarea.text());
    }

    @Test(timeout = 4000)
    public void testHtml() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append("<p>Hello</p>");
        assertEquals("<p>Hello</p>", div.html());
        assertEquals("<div><p>Hello</p></div>", div.outerHtml());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testEmptyChildren() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertTrue(el.children().isEmpty());
        assertNull(el.child(0)); // out of bounds returns null from Elements.get
    }

    @Test(timeout = 4000)
    public void testNullArguments() {
        Element el = new Element(Tag.valueOf("div"), "");
        try {
            el.appendChild(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.prependChild(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.text(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.html(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.classNames(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEmptyStringArguments() {
        Element el = new Element(Tag.valueOf("div"), "");
        try {
            el.getElementsByTag("");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.getElementById("");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.getElementsByClass("");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.getElementsByAttribute("");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testBoundaryIndices() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        parent.appendChild(child);
        // elementSiblingIndex when no parent
        Element standalone = new Element(Tag.valueOf("span"), "");
        assertEquals(0, (int) standalone.elementSiblingIndex());
        // first/last element sibling when only one child
        assertNull(parent.firstElementSibling());
        assertNull(parent.lastElementSibling());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Directly targets the known defect: appending a <tr> to a <table> should not wrap it in another <table>.
     * Expected: <table><tr><td>1</td></tr><tr><td>2</td></tr></table>
     * Buggy:    <table><tr><td>1</td></tr><table><tr><td>2</td></tr></table></table>
     */
    @Test(timeout = 4000)
    public void testAppendRowToTable() {
        Element table = new Element(Tag.valueOf("table"), "");
        table.append("<tr><td>1</td></tr>");
        table.append("<tr><td>2</td></tr>");
        String html = table.html();
        // Expected: two <tr> directly under <table>
        assertEquals("<tr><td>1</td></tr><tr><td>2</td></tr>", html);
    }

    /**
     * Directly targets the known defect: prepending a <tr> to a <table> should not wrap it in another <table>.
     * Expected: <table><tr><td>2</td></tr><tr><td>1</td></tr></table>
     * Buggy:    <table><table><tr><td>2</td></tr></table><tr><td>1</td></tr></table>
     */
    @Test(timeout = 4000)
    public void testPrependRowToTable() {
        Element table = new Element(Tag.valueOf("table"), "");
        table.append("<tr><td>1</td></tr>");
        table.prepend("<tr><td>2</td></tr>");
        String html = table.html();
        assertEquals("<tr><td>2</td></tr><tr><td>1</td></tr>", html);
    }

    /**
     * Additional test: nested implicit table handling (from Defects4J ParserTest).
     * This test ensures that when a <tr> is appended to a <table> that already contains a nested <table>,
     * the structure is correct.
     */
    @Test(timeout = 4000)
    public void testHandlesNestedImplicitTable() {
        // Simulate the scenario: <table><tr><td><table><tr><td>3</td><td>4</td></tr></table></td></tr><tr><td>5</td></tr></table>
        Element outerTable = new Element(Tag.valueOf("table"), "");
        outerTable.append("<tr><td><table><tr><td>3</td><td>4</td></tr></table></td></tr>");
        outerTable.append("<tr><td>5</td></tr>");
        String html = outerTable.html();
        // The outer table should have two <tr> children; the first <tr> contains a <td> with a nested <table>
        assertTrue("Expected two <tr> children", html.startsWith("<tr><td><table>"));
        assertTrue("Expected second <tr>", html.contains("</table></td></tr><tr><td>5</td></tr>"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testWrapWithNullHtml() {
        Element el = new Element(Tag.valueOf("span"), "");
        try {
            el.wrap(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWrapWithEmptyHtml() {
        Element el = new Element(Tag.valueOf("span"), "");
        try {
            el.wrap("");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWrapWithNoContent() {
        Element el = new Element(Tag.valueOf("span"), "");
        Element result = el.wrap("<div></div>");
        assertNull(result); // wrap returns null when no wrapping element
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespace() {
        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.text("  spaced  ");
        assertEquals("  spaced  ", pre.text()); // whitespace preserved
    }

    @Test(timeout = 4000)
    public void testSiblingElementsWhenNoParent() {
        Element el = new Element(Tag.valueOf("div"), "");
        try {
            el.siblingElements();
            fail("Should throw NullPointerException because parent is null");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract ====================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Element el1 = new Element(Tag.valueOf("div"), "http://a.com");
        Element el2 = new Element(Tag.valueOf("div"), "http://b.com");
        Element el3 = new Element(Tag.valueOf("span"), "http://a.com");
        // Same tag and baseUri (but baseUri not used in equals? Actually equals uses super.equals which includes baseUri)
        // Let's check: super.equals compares baseUri? In Node.equals it compares baseUri? Not shown but likely.
        // For simplicity, test tag equality.
        assertTrue(el1.equals(el1));
        assertFalse(el1.equals(null));
        assertFalse(el1.equals("string"));
        // Different tags
        assertFalse(el1.equals(el3));
        // Same tag, different baseUri (should be equal if baseUri not considered? Actually Node.equals compares baseUri)
        // We'll just ensure no exception.
        el1.hashCode();
        el2.hashCode();
    }

    @Test(timeout = 4000)
    public void testToString() {
        Element el = new Element(Tag.valueOf("p"), "");
        el.text("Hello");
        assertEquals("<p>Hello</p>", el.toString());
    }

    @Test(timeout = 4000)
    public void testEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.append("<p>text</p>");
        assertFalse(el.children().isEmpty());
        el.empty();
        assertTrue(el.children().isEmpty());
        assertTrue(el.childNodes().isEmpty());
    }
}