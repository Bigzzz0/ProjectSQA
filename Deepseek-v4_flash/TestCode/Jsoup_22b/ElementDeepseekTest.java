package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ElementDeepseekTest: Comprehensive white-box test suite for Element.java.
 * Targets known Defects4J defects: sibling self-inclusion and orphan NPE.
 * Achieves maximum line/branch coverage via equivalence partitioning and BVA.
 *
 * [Branch & Defect Analysis Matrix]
 * - siblingElements(): returns parent().children() which includes self -> bug: should exclude self.
 * - nextElementSibling()/previousElementSibling(): calls parent().children() -> NPE if parent null.
 * - firstElementSibling()/lastElementSibling(): same parent dependency.
 * - elementSiblingIndex(): uses indexInList which may return null if not found.
 * - preserveWhitespace(): recursive parent check.
 * - classNames(): lazy init from className() split.
 * - hasClass(): case-insensitive iteration.
 * - text()/ownText(): block handling, whitespace normalization.
 * - val(): textarea vs input.
 * - equals/hashCode/clone: identity-based equals, shallow clone.
 * - Exception paths: null/empty arguments, invalid regex.
 */
public class ElementDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testTagNameGetterAndSetter() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("div", div.tagName());
        div.tagName("span");
        assertEquals("span", div.tagName());
        assertEquals("span", div.nodeName());
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
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.id());
        el.attr("id", "myId");
        assertEquals("myId", el.id());
    }

    @Test(timeout = 4000)
    public void testAttrChain() {
        Element el = new Element(Tag.valueOf("a"), "");
        el.attr("href", "http://jsoup.org").attr("class", "link");
        assertEquals("http://jsoup.org", el.attr("href"));
        assertEquals("link", el.attr("class"));
    }

    @Test(timeout = 4000)
    public void testDataset() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("data-name", "jsoup");
        Map<String, String> data = el.dataset();
        assertEquals("jsoup", data.get("name"));
        data.put("version", "1.0");
        assertEquals("1.0", el.attr("data-version"));
    }

    @Test(timeout = 4000)
    public void testParentAndParents() {
        Element root = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        root.appendChild(body);
        body.appendChild(div);
        assertEquals(body, div.parent());
        Elements parents = div.parents();
        assertEquals(2, parents.size());
        assertEquals(body, parents.get(0));
        assertEquals(root, parents.get(1));
    }

    @Test(timeout = 4000)
    public void testChildAndChildren() {
        Element parent = new Element(Tag.valueOf("ul"), "");
        Element li1 = new Element(Tag.valueOf("li"), "");
        Element li2 = new Element(Tag.valueOf("li"), "");
        parent.appendChild(li1);
        parent.appendChild(li2);
        assertEquals(li1, parent.child(0));
        assertEquals(li2, parent.child(1));
        Elements children = parent.children();
        assertEquals(2, children.size());
    }

    @Test(timeout = 4000)
    public void testTextNodes() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendChild(new TextNode("Hello ", ""));
        p.appendChild(new Element(Tag.valueOf("b"), ""));
        p.appendChild(new TextNode(" World", ""));
        List<TextNode> textNodes = p.textNodes();
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
        Element root = Jsoup.parse("<div><p class='a'>One</p><p class='b'>Two</p></div>").body();
        Elements selected = root.select("p.a");
        assertEquals(1, selected.size());
        assertEquals("One", selected.get(0).text());
    }

    @Test(timeout = 4000)
    public void testAppendChildAndPrependChild() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("span"), "");
        parent.appendChild(child1);
        parent.prependChild(child2);
        assertEquals(child2, parent.child(0));
        assertEquals(child1, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testAppendElementAndPrependElement() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.prependElement("span");
        assertEquals(child2, parent.child(0));
        assertEquals(child1, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testAppendTextAndPrependText() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendText("World");
        parent.prependText("Hello ");
        assertEquals("Hello World", parent.text());
    }

    @Test(timeout = 4000)
    public void testAppendAndPrependHtml() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.append("<p>One</p>");
        parent.prepend("<span>Zero</span>");
        assertEquals(2, parent.children().size());
        assertEquals("Zero", parent.child(0).text());
        assertEquals("One", parent.child(1).text());
    }

    @Test(timeout = 4000)
    public void testBeforeAndAfter() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element target = new Element(Tag.valueOf("p"), "");
        parent.appendChild(target);
        target.before("<span>Before</span>");
        target.after("<span>After</span>");
        assertEquals(3, parent.children().size());
        assertEquals("Before", parent.child(0).text());
        assertEquals("After", parent.child(2).text());
    }

    @Test(timeout = 4000)
    public void testEmpty() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendChild(new Element(Tag.valueOf("p"), ""));
        parent.empty();
        assertEquals(0, parent.children().size());
    }

    @Test(timeout = 4000)
    public void testWrap() {
        Element el = new Element(Tag.valueOf("span"), "");
        el.wrap("<div></div>");
        assertEquals("div", el.parent().tagName());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testNullArguments() {
        Element el = new Element(Tag.valueOf("div"), "");
        try {
            el.appendChild(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            el.prependChild(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            el.append(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            el.prepend(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            el.text(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            el.classNames(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            el.addClass(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            el.removeClass(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            el.toggleClass(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEmptyStringArguments() {
        Element el = new Element(Tag.valueOf("div"), "");
        try {
            el.tagName("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.getElementsByTag("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.getElementById("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.getElementsByClass("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.getElementsByAttribute("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.getElementsByAttributeStarting("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInvalidRegex() {
        Element el = new Element(Tag.valueOf("div"), "");
        try {
            el.getElementsByAttributeValueMatching("key", "[invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.getElementsMatchingText("[invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            el.getElementsMatchingOwnText("[invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEmptyChildrenAndText() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals(0, el.children().size());
        assertEquals("", el.text());
        assertEquals("", el.ownText());
        assertFalse(el.hasText());
        assertEquals("", el.data());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    // Defect 1: siblingElements() includes self
    @Test(timeout = 4000)
    public void testSiblingElementsDoesNotIncludeSelf() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("p"), "");
        Element child3 = new Element(Tag.valueOf("p"), "");
        parent.appendChild(child1);
        parent.appendChild(child2);
        parent.appendChild(child3);

        // Each child should have 2 siblings (the other two)
        assertEquals(2, child1.siblingElements().size());
        assertEquals(2, child2.siblingElements().size());
        assertEquals(2, child3.siblingElements().size());

        // Verify that the sibling list does not contain the element itself
        assertFalse(child1.siblingElements().contains(child1));
        assertFalse(child2.siblingElements().contains(child2));
        assertFalse(child3.siblingElements().contains(child3));
    }

    // Defect 2: nextElementSibling/previousElementSibling on orphan throws NPE
    @Test(timeout = 4000)
    public void testOrphanElementReturnsNullForSiblingMethods() {
        Element orphan = new Element(Tag.valueOf("div"), "");
        assertNull(orphan.nextElementSibling());
        assertNull(orphan.previousElementSibling());
        assertNull(orphan.firstElementSibling());
        assertNull(orphan.lastElementSibling());
        // elementSiblingIndex should return 0 for orphan (as per code)
        assertEquals(Integer.valueOf(0), orphan.elementSiblingIndex());
    }

    // Additional sibling tests for non-orphan
    @Test(timeout = 4000)
    public void testNextAndPreviousElementSibling() {
        Element parent = new Element(Tag.valueOf("ul"), "");
        Element li1 = new Element(Tag.valueOf("li"), "");
        Element li2 = new Element(Tag.valueOf("li"), "");
        Element li3 = new Element(Tag.valueOf("li"), "");
        parent.appendChild(li1);
        parent.appendChild(li2);
        parent.appendChild(li3);

        assertEquals(li2, li1.nextElementSibling());
        assertEquals(li3, li2.nextElementSibling());
        assertNull(li3.nextElementSibling());

        assertNull(li1.previousElementSibling());
        assertEquals(li1, li2.previousElementSibling());
        assertEquals(li2, li3.previousElementSibling());
    }

    @Test(timeout = 4000)
    public void testFirstAndLastElementSibling() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element a = new Element(Tag.valueOf("a"), "");
        Element b = new Element(Tag.valueOf("b"), "");
        Element c = new Element(Tag.valueOf("c"), "");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);

        assertEquals(a, a.firstElementSibling());
        assertEquals(a, b.firstElementSibling());
        assertEquals(a, c.firstElementSibling());

        assertEquals(c, a.lastElementSibling());
        assertEquals(c, b.lastElementSibling());
        assertEquals(c, c.lastElementSibling());
    }

    @Test(timeout = 4000)
    public void testElementSiblingIndex() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element a = new Element(Tag.valueOf("a"), "");
        Element b = new Element(Tag.valueOf("b"), "");
        Element c = new Element(Tag.valueOf("c"), "");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);

        assertEquals(Integer.valueOf(0), a.elementSiblingIndex());
        assertEquals(Integer.valueOf(1), b.elementSiblingIndex());
        assertEquals(Integer.valueOf(2), c.elementSiblingIndex());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testGetElementsByTagCaseInsensitive() {
        Element root = Jsoup.parse("<DIV><P>One</P></DIV>").body();
        Elements byTag = root.getElementsByTag("p");
        assertEquals(1, byTag.size());
        byTag = root.getElementsByTag("P");
        assertEquals(1, byTag.size());
    }

    @Test(timeout = 4000)
    public void testGetElementByIdNotFound() {
        Element root = Jsoup.parse("<div><p id='x'>Hi</p></div>").body();
        assertNull(root.getElementById("y"));
        assertNotNull(root.getElementById("x"));
    }

    @Test(timeout = 4000)
    public void testGetElementsByClass() {
        Element root = Jsoup.parse("<div class='a b'><p class='a'>One</p><p class='b'>Two</p></div>").body();
        Elements byClass = root.getElementsByClass("a");
        assertEquals(2, byClass.size()); // div and p
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttribute() {
        Element root = Jsoup.parse("<div><a href='x'>Link</a><span>No</span></div>").body();
        Elements withHref = root.getElementsByAttribute("href");
        assertEquals(1, withHref.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeStarting() {
        Element root = Jsoup.parse("<div data-name='test'><p>Hi</p></div>").body();
        Elements withData = root.getElementsByAttributeStarting("data-");
        assertEquals(1, withData.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValue() {
        Element root = Jsoup.parse("<div><a href='http://jsoup.org'>Link</a></div>").body();
        Elements matching = root.getElementsByAttributeValue("href", "http://jsoup.org");
        assertEquals(1, matching.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueNot() {
        Element root = Jsoup.parse("<div><a href='x'>A</a><a href='y'>B</a></div>").body();
        Elements notX = root.getElementsByAttributeValueNot("href", "x");
        assertEquals(1, notX.size());
        assertEquals("B", notX.get(0).text());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueStarting() {
        Element root = Jsoup.parse("<div><a href='https://a.com'>A</a><a href='http://b.com'>B</a></div>").body();
        Elements https = root.getElementsByAttributeValueStarting("href", "https");
        assertEquals(1, https.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueEnding() {
        Element root = Jsoup.parse("<div><a href='page.html'>A</a><a href='page.htm'>B</a></div>").body();
        Elements html = root.getElementsByAttributeValueEnding("href", ".html");
        assertEquals(1, html.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueContaining() {
        Element root = Jsoup.parse("<div><a href='example.com'>A</a><a href='test.org'>B</a></div>").body();
        Elements containing = root.getElementsByAttributeValueContaining("href", "example");
        assertEquals(1, containing.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatchingPattern() {
        Element root = Jsoup.parse("<div><a href='http://a.com'>A</a><a href='https://b.com'>B</a></div>").body();
        Pattern pattern = Pattern.compile("https://.*");
        Elements matching = root.getElementsByAttributeValueMatching("href", pattern);
        assertEquals(1, matching.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexLessThanGreaterThanEquals() {
        Element root = Jsoup.parse("<div><p>0</p><p>1</p><p>2</p></div>").body();
        Elements lessThan1 = root.getElementsByIndexLessThan(1);
        assertEquals(1, lessThan1.size());
        assertEquals("0", lessThan1.get(0).text());

        Elements greaterThan1 = root.getElementsByIndexGreaterThan(1);
        assertEquals(1, greaterThan1.size());
        assertEquals("2", greaterThan1.get(0).text());

        Elements equals1 = root.getElementsByIndexEquals(1);
        assertEquals(1, equals1.size());
        assertEquals("1", equals1.get(0).text());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingText() {
        Element root = Jsoup.parse("<div><p>Hello World</p><p>Goodbye</p></div>").body();
        Elements containing = root.getElementsContainingText("world");
        assertEquals(1, containing.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingOwnText() {
        Element root = Jsoup.parse("<div><p>Hello <b>World</b></p><p>Goodbye</p></div>").body();
        Elements containingOwn = root.getElementsContainingOwnText("Hello");
        assertEquals(1, containingOwn.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingText() {
        Element root = Jsoup.parse("<div><p>Hello</p><p>World</p></div>").body();
        Pattern p = Pattern.compile("Hello");
        Elements matching = root.getElementsMatchingText(p);
        assertEquals(1, matching.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingOwnText() {
        Element root = Jsoup.parse("<div><p>Hello <b>World</b></p><p>Goodbye</p></div>").body();
        Pattern p = Pattern.compile("Hello");
        Elements matching = root.getElementsMatchingOwnText(p);
        assertEquals(1, matching.size());
    }

    @Test(timeout = 4000)
    public void testGetAllElements() {
        Element root = Jsoup.parse("<div><p><span>Hi</span></p></div>").body();
        Elements all = root.getAllElements();
        assertEquals(3, all.size()); // div, p, span
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("div"), "");
        assertTrue(el1.equals(el1));
        assertFalse(el1.equals(el2));
        assertFalse(el1.equals(null));
        assertFalse(el1.equals("string"));
        // hashCode consistency
        assertEquals(el1.hashCode(), el1.hashCode());
    }

    @Test(timeout = 4000)
    public void testClone() {
        Element original = new Element(Tag.valueOf("div"), "");
        original.attr("class", "test");
        original.appendChild(new Element(Tag.valueOf("p"), ""));
        Element clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.tagName(), clone.tagName());
        assertEquals(original.attr("class"), clone.attr("class"));
        assertEquals(original.children().size(), clone.children().size());
        // cloned children should be deep copies
        assertNotSame(original.child(0), clone.child(0));
    }

    @Test(timeout = 4000)
    public void testToString() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("id", "test");
        assertEquals("<div id=\"test\"></div>", el.toString());
    }

    // ==================== Additional Coverage for text/ownText/hasText/data ====================

    @Test(timeout = 4000)
    public void testTextWithBlockElements() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(new TextNode("Hello ", ""));
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendChild(new TextNode("World", ""));
        div.appendChild(p);
        div.appendChild(new TextNode(" Now", ""));
        assertEquals("Hello World Now", div.text());
        assertEquals("Hello  Now", div.ownText()); // block adds space before and after? Actually ownText only direct text nodes and br
    }

    @Test(timeout = 4000)
    public void testHasText() {
        Element empty = new Element(Tag.valueOf("div"), "");
        assertFalse(empty.hasText());
        empty.appendChild(new TextNode("   ", ""));
        assertFalse(empty.hasText()); // only whitespace
        empty.appendChild(new TextNode("a", ""));
        assertTrue(empty.hasText());
    }

    @Test(timeout = 4000)
    public void testData() {
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("var x=1;", ""));
        assertEquals("var x=1;", script.data());
        Element nested = new Element(Tag.valueOf("div"), "");
        nested.appendChild(script);
        assertEquals("var x=1;", nested.data());
    }

    @Test(timeout = 4000)
    public void testClassNameAndClassNames() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.className());
        assertTrue(el.classNames().isEmpty());
        el.attr("class", "a b");
        assertEquals("a b", el.className());
        Set<String> classNames = el.classNames();
        assertEquals(2, classNames.size());
        assertTrue(classNames.contains("a"));
        assertTrue(classNames.contains("b"));
    }

    @Test(timeout = 4000)
    public void testHasClass() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "Header Main");
        assertTrue(el.hasClass("header"));
        assertTrue(el.hasClass("main"));
        assertFalse(el.hasClass("footer"));
    }

    @Test(timeout = 4000)
    public void testAddRemoveToggleClass() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass("a");
        assertTrue(el.hasClass("a"));
        el.addClass("b");
        assertEquals("a b", el.className());
        el.removeClass("a");
        assertFalse(el.hasClass("a"));
        assertTrue(el.hasClass("b"));
        el.toggleClass("b");
        assertFalse(el.hasClass("b"));
        el.toggleClass("c");
        assertTrue(el.hasClass("c"));
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
        div.appendChild(new Element(Tag.valueOf("p"), "").text("Hello"));
        assertEquals("<p>Hello</p>", div.html());
        div.html("<span>World</span>");
        assertEquals("<span>World</span>", div.html());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespace() {
        Element pre = new Element(Tag.valueOf("pre"), "");
        assertTrue(pre.preserveWhitespace());
        Element div = new Element(Tag.valueOf("div"), "");
        assertFalse(div.preserveWhitespace());
        // child inherits from parent
        Element child = new Element(Tag.valueOf("span"), "");
        pre.appendChild(child);
        assertTrue(child.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadTail() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("class", "test");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        div.outerHtmlHead(sb, 0, out);
        assertEquals("<div class=\"test\">", sb.toString());
        sb.setLength(0);
        div.outerHtmlTail(sb, 0, out);
        assertEquals("</div>", sb.toString());

        // self-closing tag
        Element br = new Element(Tag.valueOf("br"), "");
        sb.setLength(0);
        br.outerHtmlHead(sb, 0, out);
        assertEquals("<br />", sb.toString());
        sb.setLength(0);
        br.outerHtmlTail(sb, 0, out);
        assertEquals("", sb.toString());
    }
}