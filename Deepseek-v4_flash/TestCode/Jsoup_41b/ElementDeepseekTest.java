package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * White-box test suite for Element.java targeting line/branch coverage and the known equals/hashCode defect.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic & state transitions (constructors, tagName, id, children, text, etc.)
 * - Partition B: Boundary value analysis (null, empty strings, negative indices, MAX_VALUE)
 * - Partition C: Defect-targeted branch zone (equals/hashCode contract violation)
 * - Partition D: Exception & defensive guard paths (null arguments, invalid indices, pattern syntax)
 * - Partition E: Object lifecycle & contract integrity (clone, toString, cssSelector)
 *
 * Known defect: Element.equals() only returns true for same reference (this == o) instead of comparing tag and attributes.
 * Test method testEqualsAndHashCode() directly targets this failure.
 */
public class ElementDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorAndTagName() {
        Tag tag = Tag.valueOf("div");
        Element el = new Element(tag, "http://example.com");
        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());
        assertSame(tag, el.tag());
    }

    @Test(timeout = 4000)
    public void testConstructorWithAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("id", "myId");
        attrs.put("class", "myClass");
        Element el = new Element(Tag.valueOf("p"), "http://example.com", attrs);
        assertEquals("myId", el.id());
        assertEquals("myClass", el.className());
    }

    @Test(timeout = 4000)
    public void testTagNameChange() {
        Element el = new Element(Tag.valueOf("span"), "http://example.com");
        el.tagName("div");
        assertEquals("div", el.tagName());
        // Ensure tag object is updated
        assertEquals("div", el.tag().getName());
    }

    @Test(timeout = 4000)
    public void testIsBlock() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        assertTrue(div.isBlock());
        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        assertFalse(span.isBlock());
    }

    @Test(timeout = 4000)
    public void testId() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("", el.id()); // no id attribute
        el.attr("id", "testId");
        assertEquals("testId", el.id());
    }

    @Test(timeout = 4000)
    public void testAttrReturnsThis() {
        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        Element returned = el.attr("href", "http://jsoup.org");
        assertSame(el, returned);
    }

    @Test(timeout = 4000)
    public void testDataset() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("data-name", "jsoup");
        el.attr("data-version", "1.0");
        Map<String, String> dataset = el.dataset();
        assertEquals("jsoup", dataset.get("name"));
        assertEquals("1.0", dataset.get("version"));
        // Changes to dataset reflect on element
        dataset.put("lang", "Java");
        assertEquals("Java", el.attr("data-lang"));
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
        Element root = new Element(Tag.valueOf("html"), "http://example.com");
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        root.appendChild(div);
        div.appendChild(p);
        Elements parents = p.parents();
        assertEquals(2, parents.size());
        assertSame(div, parents.get(0));
        assertSame(root, parents.get(1));
    }

    @Test(timeout = 4000)
    public void testChildByIndex() {
        Element parent = new Element(Tag.valueOf("ul"), "http://example.com");
        Element li1 = parent.appendElement("li");
        Element li2 = parent.appendElement("li");
        assertSame(li1, parent.child(0));
        assertSame(li2, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testChildren() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendChild(new TextNode("text", "http://example.com"));
        Element child = parent.appendElement("span");
        Elements children = parent.children();
        assertEquals(1, children.size());
        assertSame(child, children.get(0));
    }

    @Test(timeout = 4000)
    public void testTextNodes() {
        Element parent = new Element(Tag.valueOf("p"), "http://example.com");
        parent.appendChild(new TextNode("Hello ", "http://example.com"));
        parent.appendChild(new TextNode("World", "http://example.com"));
        List<TextNode> textNodes = parent.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Hello ", textNodes.get(0).getWholeText());
        assertEquals("World", textNodes.get(1).getWholeText());
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
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        Element p = root.appendElement("p");
        p.attr("class", "test");
        Elements selected = root.select("p.test");
        assertEquals(1, selected.size());
        assertSame(p, selected.get(0));
    }

    @Test(timeout = 4000)
    public void testAppendChild() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(child);
        assertEquals(1, parent.childNodeSize());
        assertSame(child, parent.child(0));
    }

    @Test(timeout = 4000)
    public void testPrependChild() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element first = parent.appendElement("span");
        Element second = new Element(Tag.valueOf("p"), "http://example.com");
        parent.prependChild(second);
        assertSame(second, parent.child(0));
        assertSame(first, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testInsertChildren() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = parent.appendElement("a");
        Element b = parent.appendElement("b");
        Element c = new Element(Tag.valueOf("c"), "http://example.com");
        Element d = new Element(Tag.valueOf("d"), "http://example.com");
        List<Element> toInsert = Arrays.asList(c, d);
        parent.insertChildren(1, toInsert);
        assertEquals(4, parent.children().size());
        assertSame(a, parent.child(0));
        assertSame(c, parent.child(1));
        assertSame(d, parent.child(2));
        assertSame(b, parent.child(3));
    }

    @Test(timeout = 4000)
    public void testAppendElement() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = parent.appendElement("p");
        assertNotNull(child);
        assertEquals("p", child.tagName());
        assertSame(parent, child.parent());
    }

    @Test(timeout = 4000)
    public void testPrependElement() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element first = parent.appendElement("span");
        Element second = parent.prependElement("p");
        assertSame(second, parent.child(0));
        assertSame(first, parent.child(1));
    }

    @Test(timeout = 4000)
    public void testAppendText() {
        Element parent = new Element(Tag.valueOf("p"), "http://example.com");
        parent.appendText("Hello");
        assertEquals(1, parent.textNodes().size());
        assertEquals("Hello", parent.text());
    }

    @Test(timeout = 4000)
    public void testPrependText() {
        Element parent = new Element(Tag.valueOf("p"), "http://example.com");
        parent.appendText("World");
        parent.prependText("Hello ");
        assertEquals("Hello World", parent.text());
    }

    @Test(timeout = 4000)
    public void testAppendHtml() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.append("<p>Hello</p>");
        assertEquals(1, parent.children().size());
        assertEquals("p", parent.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testPrependHtml() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.append("<span>World</span>");
        parent.prepend("<p>Hello</p>");
        assertEquals(2, parent.children().size());
        assertEquals("p", parent.child(0).tagName());
        assertEquals("span", parent.child(1).tagName());
    }

    @Test(timeout = 4000)
    public void testBeforeAfterHtml() {
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
    public void testBeforeAfterNode() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = parent.appendElement("p");
        Element beforeNode = new Element(Tag.valueOf("span"), "http://example.com");
        Element afterNode = new Element(Tag.valueOf("span"), "http://example.com");
        child.before(beforeNode);
        child.after(afterNode);
        assertSame(beforeNode, parent.child(0));
        assertSame(child, parent.child(1));
        assertSame(afterNode, parent.child(2));
    }

    @Test(timeout = 4000)
    public void testEmpty() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendElement("p");
        parent.appendText("text");
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
        child.attr("class", "foo");
        String selector = child.cssSelector();
        assertTrue(selector.contains("p.foo"));
        assertTrue(selector.contains(":nth-child"));
    }

    @Test(timeout = 4000)
    public void testSiblingElements() {
        Element parent = new Element(Tag.valueOf("ul"), "http://example.com");
        Element li1 = parent.appendElement("li");
        Element li2 = parent.appendElement("li");
        Element li3 = parent.appendElement("li");
        Elements siblings = li2.siblingElements();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(li1));
        assertTrue(siblings.contains(li3));
    }

    @Test(timeout = 4000)
    public void testNextPreviousElementSibling() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
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
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = parent.appendElement("a");
        Element b = parent.appendElement("b");
        assertSame(a, a.firstElementSibling());
        assertSame(b, b.lastElementSibling());
        // Single child
        Element single = new Element(Tag.valueOf("div"), "http://example.com");
        Element only = single.appendElement("p");
        assertNull(only.firstElementSibling());
        assertNull(only.lastElementSibling());
    }

    @Test(timeout = 4000)
    public void testElementSiblingIndex() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element a = parent.appendElement("a");
        Element b = parent.appendElement("b");
        assertEquals(0, a.elementSiblingIndex().intValue());
        assertEquals(1, b.elementSiblingIndex().intValue());
    }

    @Test(timeout = 4000)
    public void testGetElementsByTag() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("p").attr("class", "a");
        root.appendElement("p").attr("class", "b");
        Elements ps = root.getElementsByTag("p");
        assertEquals(2, ps.size());
    }

    @Test(timeout = 4000)
    public void testGetElementById() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        Element target = root.appendElement("p");
        target.attr("id", "target");
        assertSame(target, root.getElementById("target"));
        assertNull(root.getElementById("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetElementsByClass() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("p").attr("class", "foo");
        root.appendElement("p").attr("class", "bar");
        Elements foos = root.getElementsByClass("foo");
        assertEquals(1, foos.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttribute() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("a").attr("href", "http://example.com");
        root.appendElement("span");
        Elements withHref = root.getElementsByAttribute("href");
        assertEquals(1, withHref.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeStarting() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("div").attr("data-name", "test");
        Elements data = root.getElementsByAttributeStarting("data-");
        assertEquals(1, data.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValue() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("a").attr("href", "http://jsoup.org");
        Elements matches = root.getElementsByAttributeValue("href", "http://jsoup.org");
        assertEquals(1, matches.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueNot() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("a").attr("href", "http://jsoup.org");
        root.appendElement("a").attr("href", "http://other.com");
        Elements not = root.getElementsByAttributeValueNot("href", "http://jsoup.org");
        assertEquals(1, not.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueStarting() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("a").attr("href", "https://secure.com");
        Elements matches = root.getElementsByAttributeValueStarting("href", "https");
        assertEquals(1, matches.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueEnding() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("a").attr("href", "file.pdf");
        Elements matches = root.getElementsByAttributeValueEnding("href", ".pdf");
        assertEquals(1, matches.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueContaining() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("a").attr("href", "http://example.com/page");
        Elements matches = root.getElementsByAttributeValueContaining("href", "example");
        assertEquals(1, matches.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatchingPattern() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("a").attr("href", "http://jsoup.org");
        Pattern pattern = Pattern.compile("http://.*\\.org");
        Elements matches = root.getElementsByAttributeValueMatching("href", pattern);
        assertEquals(1, matches.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatchingString() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("a").attr("href", "http://jsoup.org");
        Elements matches = root.getElementsByAttributeValueMatching("href", "http://.*\\.org");
        assertEquals(1, matches.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexLessThan() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendElement("p");
        parent.appendElement("p");
        parent.appendElement("p");
        Elements less = parent.getElementsByIndexLessThan(2);
        assertEquals(2, less.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexGreaterThan() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendElement("p");
        parent.appendElement("p");
        parent.appendElement("p");
        Elements greater = parent.getElementsByIndexGreaterThan(0);
        assertEquals(2, greater.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexEquals() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendElement("p");
        parent.appendElement("p");
        Elements equals = parent.getElementsByIndexEquals(1);
        assertEquals(1, equals.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingText() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        Element p = root.appendElement("p");
        p.text("Hello World");
        Elements matches = root.getElementsContainingText("hello");
        assertEquals(1, matches.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingOwnText() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        Element p = root.appendElement("p");
        p.text("Hello");
        Element span = p.appendElement("span");
        span.text("World");
        Elements matches = root.getElementsContainingOwnText("Hello");
        assertEquals(1, matches.size());
        // "World" is not own text of p
        matches = root.getElementsContainingOwnText("World");
        assertEquals(0, matches.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingTextPattern() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("p").text("Hello 123");
        Pattern pattern = Pattern.compile("\\d+");
        Elements matches = root.getElementsMatchingText(pattern);
        assertEquals(1, matches.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingTextString() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("p").text("Hello 123");
        Elements matches = root.getElementsMatchingText("\\d+");
        assertEquals(1, matches.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingOwnTextPattern() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        Element p = root.appendElement("p");
        p.text("Hello");
        p.appendElement("span").text("World");
        Pattern pattern = Pattern.compile("Hello");
        Elements matches = root.getElementsMatchingOwnText(pattern);
        assertEquals(1, matches.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingOwnTextString() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        Element p = root.appendElement("p");
        p.text("Hello");
        Elements matches = root.getElementsMatchingOwnText("Hello");
        assertEquals(1, matches.size());
    }

    @Test(timeout = 4000)
    public void testGetAllElements() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.appendElement("p");
        root.appendElement("span");
        Elements all = root.getAllElements();
        assertEquals(3, all.size()); // includes root
    }

    @Test(timeout = 4000)
    public void testText() {
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        el.appendChild(new TextNode("Hello ", "http://example.com"));
        el.appendChild(new Element(Tag.valueOf("b"), "http://example.com").appendText("World"));
        assertEquals("Hello World", el.text());
    }

    @Test(timeout = 4000)
    public void testOwnText() {
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        el.appendChild(new TextNode("Hello ", "http://example.com"));
        el.appendChild(new Element(Tag.valueOf("b"), "http://example.com").appendText("World"));
        assertEquals("Hello", el.ownText());
    }

    @Test(timeout = 4000)
    public void testTextSetter() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.text("New text");
        assertEquals("New text", el.text());
        assertEquals(1, el.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testHasText() {
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        assertFalse(el.hasText());
        el.text("Hello");
        assertTrue(el.hasText());
        // Whitespace only
        Element ws = new Element(Tag.valueOf("p"), "http://example.com");
        ws.appendChild(new TextNode("   ", "http://example.com"));
        assertFalse(ws.hasText());
    }

    @Test(timeout = 4000)
    public void testData() {
        Element script = new Element(Tag.valueOf("script"), "http://example.com");
        script.appendChild(new DataNode("var x = 1;", "http://example.com"));
        assertEquals("var x = 1;", script.data());
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
        el.attr("class", "foo bar");
        Set<String> classNames = el.classNames();
        assertEquals(2, classNames.size());
        assertTrue(classNames.contains("foo"));
        assertTrue(classNames.contains("bar"));
    }

    @Test(timeout = 4000)
    public void testClassNamesSetter() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        Set<String> newClasses = new LinkedHashSet<>(Arrays.asList("a", "b"));
        el.classNames(newClasses);
        assertEquals("a b", el.className());
    }

    @Test(timeout = 4000)
    public void testHasClass() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "foo bar");
        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));
        assertFalse(el.hasClass("baz"));
        // Case insensitive
        assertTrue(el.hasClass("FOO"));
    }

    @Test(timeout = 4000)
    public void testAddClass() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.addClass("foo");
        assertEquals("foo", el.className());
        el.addClass("bar");
        assertEquals("foo bar", el.className());
    }

    @Test(timeout = 4000)
    public void testRemoveClass() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "foo bar");
        el.removeClass("foo");
        assertEquals("bar", el.className());
    }

    @Test(timeout = 4000)
    public void testToggleClass() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.toggleClass("foo");
        assertTrue(el.hasClass("foo"));
        el.toggleClass("foo");
        assertFalse(el.hasClass("foo"));
    }

    @Test(timeout = 4000)
    public void testVal() {
        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        assertEquals("", input.val());
        input.attr("value", "test");
        assertEquals("test", input.val());

        Element textarea = new Element(Tag.valueOf("textarea"), "http://example.com");
        textarea.text("content");
        assertEquals("content", textarea.val());
    }

    @Test(timeout = 4000)
    public void testValSetter() {
        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        input.val("newValue");
        assertEquals("newValue", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "http://example.com");
        textarea.val("newText");
        assertEquals("newText", textarea.text());
    }

    @Test(timeout = 4000)
    public void testHtml() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.append("<p>Hello</p>");
        String html = el.html();
        assertTrue(html.contains("<p>Hello</p>"));
    }

    @Test(timeout = 4000)
    public void testHtmlSetter() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.html("<span>New</span>");
        assertEquals(1, el.children().size());
        assertEquals("span", el.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testOuterHtml() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("id", "test");
        String outer = el.outerHtml();
        assertTrue(outer.startsWith("<div"));
        assertTrue(outer.endsWith("</div>"));
    }

    @Test(timeout = 4000)
    public void testToString() {
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        assertEquals(el.outerHtml(), el.toString());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testConstructorWithEmptyBaseUri() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.baseUri());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTagNameEmpty() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.tagName("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAppendChildNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrependChildNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.prependChild(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInsertChildrenNullCollection() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.insertChildren(0, null);
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

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAppendNullHtml() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.append((String) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrependNullHtml() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.prepend((String) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTextNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.text(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testClassNamesNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.classNames(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddClassNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.addClass(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRemoveClassNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.removeClass(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
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

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInsertChildrenNegativeIndex() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.insertChildren(-1, Collections.emptyList());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInsertChildrenIndexTooLarge() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.insertChildren(5, Collections.emptyList());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Directly targets the known equals/hashCode defect.
     * Two elements with identical tag and attributes should be equal and have same hashCode.
     * The buggy equals() only returns true for same reference.
     */
    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Element el1 = new Element(Tag.valueOf("p"), "http://example.com");
        el1.attr("class", "one");
        el1.text("One");

        Element el2 = new Element(Tag.valueOf("p"), "http://example.com");
        el2.attr("class", "one");
        el2.text("One");

        // These should be equal according to value equality
        assertEquals("Elements with same tag and attributes should be equal", el1, el2);
        assertEquals("Hash codes should be equal for equal elements", el1.hashCode(), el2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentTag() {
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        Element el2 = new Element(Tag.valueOf("span"), "http://example.com");
        assertNotEquals(el1, el2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentAttributes() {
        Element el1 = new Element(Tag.valueOf("p"), "http://example.com");
        el1.attr("class", "one");
        Element el2 = new Element(Tag.valueOf("p"), "http://example.com");
        el2.attr("class", "two");
        assertNotEquals(el1, el2);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertFalse(el.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertFalse(el.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsSameReference() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertTrue(el.equals(el));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        int hash1 = el.hashCode();
        int hash2 = el.hashCode();
        assertEquals(hash1, hash2);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullTag() {
        new Element(null, "http://example.com");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullBaseUri() {
        // baseUri can be empty but not null; Node constructor will throw
        new Element(Tag.valueOf("div"), null);
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespace() {
        Element pre = new Element(Tag.valueOf("pre"), "http://example.com");
        pre.appendChild(new TextNode("  spaced  ", "http://example.com"));
        assertEquals("  spaced  ", pre.text());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespaceInherited() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element pre = div.appendElement("pre");
        pre.appendChild(new TextNode("  text  ", "http://example.com"));
        assertEquals("  text  ", pre.text());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlSelfClosing() {
        Element br = new Element(Tag.valueOf("br"), "http://example.com");
        String html = br.outerHtml();
        assertTrue(html.equals("<br>") || html.equals("<br />"));
    }

    @Test(timeout = 4000)
    public void testOuterHtmlIndent() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendChild(new Element(Tag.valueOf("p"), "http://example.com"));
        String html = div.outerHtml();
        assertTrue(html.contains("\n"));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testClone() {
        Element original = new Element(Tag.valueOf("div"), "http://example.com");
        original.attr("id", "original");
        original.appendElement("p").text("child");
        Element clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.tagName(), clone.tagName());
        assertEquals(original.attr("id"), clone.attr("id"));
        assertEquals(original.children().size(), clone.children().size());
        // Ensure deep clone: child is also cloned
        assertNotSame(original.child(0), clone.child(0));
    }

    @Test(timeout = 4000)
    public void testCloneIndependence() {
        Element original = new Element(Tag.valueOf("div"), "http://example.com");
        original.text("Hello");
        Element clone = original.clone();
        clone.text("World");
        assertEquals("Hello", original.text());
        assertEquals("World", clone.text());
    }

    @Test(timeout = 4000)
    public void testParentAfterClone() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = parent.appendElement("p");
        Element clonedChild = child.clone();
        assertNull(clonedChild.parent());
    }

    @Test(timeout = 4000)
    public void testCssSelectorWithDocumentParent() {
        // Simulate a Document as parent (Document extends Element)
        // We can't easily create a Document here, but we can test the branch where parent is null or Document
        Element el = new Element(Tag.valueOf("html"), "http://example.com");
        // No parent, so cssSelector should just return tagName
        assertEquals("html", el.cssSelector());
    }
}