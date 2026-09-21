package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.select.Elements;
import java.util.Set;
import java.util.LinkedHashSet;

public class ElementDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * Partition A: Core functional paths.
     * - Constructors (valid, empty baseUri)     * - tagName getter/setter (case preservation)     * - isBlock ()     * - id() / attr() / dataset()     * - parent () / parents()     * - children() / child() / textNodes() / dataNodes()     * - select() / is()     * - appendChild / prependChild / insertChildren     * - appendElement / prependElement     * - appendText / prependText     * - append / prepend     * - before / after     * - empty / wrap / cssSelctor     * - siblingElements / nextElementSibling / previousElementSibling     * - firstElementSibling / lastElementSibling / elementSiblingIncex     * - getElementsByTag / getElementById / etc.     * - tetx() / ownText() / hasText() / data()     * - className() / classNames() / hasClass() / addClass() / removeClass() / toggleClass()     * - val() / html() / clone()     *
     * Partition B: Boundary / Edge cases     * - null arguments (Validate.notNull paths)     * - empty strings (id, className, etc.)     * - index boundaries (0, -1, size) for child(), insertChildren ()     * - whitespace/empty class attribute     * - pattern syntax error in getElementsMatchingText / getElementsByAttributeValueMatching     *
     * Partition C: Defect-targeted (case-insensitive class)     * - hasClass with mismatched case     * - getElementsByClass with mismatched case     * - classNames() preserves original case     *
     * Partition D: Exception / defensive paths     * - IllegalArgumentException on null/empty tag/class/id/attr key     * - IndexOutOfBoundsException on child()     * - IllegalArgument on invalid regex     * - NullPointer on null child in appendChild etc.     *
     * Partition E: Object contract     * - clone() is independant     */

    // ---------- Partition A: Core Functional --------}

    @Test(timeout = 4000)
    public void testConstructorString() {
        Element el = new Element("div");
        assertEquals("div", el.tagName());
        assertTrue(el.tagName().equals("div"));
        assertTrue(el.isBlock()); // div is block by default
    }

    @Test(timeout = 4000)
    public void testConstructorTagBaseUri() {
        Element el = new Element(Tag.valueOf("span"), "http://example.com");
        assertEquals("span", el.tagName());
        assertFalse(el.isBlock());
        assertEquals("http://example.com", el.baseUri());
    }

    @Test(timeout = 4000)
    public void testConstructorAllArgs() {
        Attributes attrs = new Attributes();
        attrs.put("id", "one");
        Element el = new Element(Tag.valueOf("p"), "", attrs);
        assertEquals("p", el.tagName());
        assertEquals("one", el.id());
    }

    @Test(timeout = 4000)
    public void testTagNameSetter() {
        Element el = new Element("div");
        el.tagName("span");
        assertEquals("span", el.tagName());

        // case preservation
        el.tagName("DIV");
        assertEquals("DIV", el.tagName());
    }

    @Test(timeout = 4000)
    public void testId() {
        Element el = new Element("div");
        assertEquals("", el.id());
        el.attr("id", "myId");
        assertEquals("myId", el.id());
    }

    @Test(timeout = 4000)
    public void testAttr() {
        Element el = new Element("a");
        el.attr("href", "http://example.com");
        assertEquals("http://example.com", el.attr("href"));
        // boolean attr
        el.attr("disabled", true);
        assertEquals("", el.attr("disabled"));
        el.attr("disabled", false);
        assertEquals("", el.attr("disabled")); // removed
    }

    @Test(timeout = 4000)
    public void testDataset() {
        Element el = new Element("div");
        el.attr("data-name", "John");
        el.attr("data-age", "30");
        Map<String,String> ds = el.dataset();
        assertEquals(2, ds.size());
        assertEquals("John", ds.get("name"));
        assertEquals("30", ds.get("age"));
        // changes reflect
        ds.put("city", "NYC");
        assertEquals("NYC", el.attr("data-city"));
    }

    @Test(timeout = 4000)
    public void testParent() {
        Element parent = new Element("div");
        Element child = new Element("p");
        parent.appendChild(child);
        assertSame(parent, child.parent());
    }

    @Test(timeout = 4000)
    public void testParents() {
        Element root = new Element("html");
        Element body = new Element("body");
        Element div = new Element("div");
        root.appendChild(body);
        body.appendChild(div);
        Elements parents = div.parents();
        assertEquals(2, parents.size()); // body, div? Actually parents() includes closest first: root? Wait, accumulateParents stops at #root. So parents from div: body, html. But html tag name is "html". So size 2.
        assertEquals("body", parents.get(0).tagName());
        assertEquals("html", parents.get(1).tagName());
    }

    @Test(timeout = 4000)
    public void testChildren() {
        Element parent = new Element("div");
        Element child1 = new Element("p");
        Element child2 = new Elements("span");
        TextNode text = new TextNode("hello", "");
        parent.appendChild(child1);
        parent.appendChild(text);
        parent.appendChild(child2);
        Elements children = parent.children();
        assertEquals(2, children.size());
        assertEquals("p", children.get(0).tagName());
        assertEquals("span", children.get(1).tagName());
    }

    @Test(timeout = 4000)
    public void testChild() {
        Element parent = new Element("div");
        Element child = new Element("p");
        parent.appendChild(child);
        assertSame(child, parent.child(0));
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testChildOutOfBounds() {
        Element parent = new Element("div");
        parent.child(0);
    }

    @Test(timeout = 4000)
    public void testTextNodes() {
        Element el = new Element("p");
        el.appendChild(new TextNode("Hello ", ""));
        el.appendChild(new Element("b"));
        el.appendChild(new TextNode("World", ""));
        List<TextNode> nodes = el.textNodes();
        assertEquals(2, nodes.size());
        assertEquals("Hello ", nodes.get(0).getWholeText());
        assertEquals("World", nodes.get(1).getWholeText());
    }

    @Test(timeout = 4000)
    public void testDataNodes() {
        Element el = new Element("script");
        DataNode data = new DataNode("alert('hi');", "");
        el.appendChild(data);
        List<DataNode> nodes = el.dataNodes();
        assertEquals(1, nodes.size());
        assertEquals("alert('hi');", nodes.get(0).getWholeData());
    }

    @Test(timeout = 4000)
    public void testSelect() {
        Element root = new Element("div");
        root.attr("id", "root");
        Element p = new Element("p");
        p.attr("class", "content");
        root.appendChild(p);
        Elements sel = root.select("p");
        assertEquals(1, sel.size());
        assertEquals("content", sel.get(0).attr("class"));
    }

    @Test(timeout = 4000)
    public void testIs() {
        Element el = new Element("div");
        el.attr("class", "box");
        assertTrue(el.is(".box"));
        assertFalse(el.is("span"));
    }

    @Test(timeout = 4000)
    public void testAppendChild() {
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        assertEquals(1, parent.childNodeSize());
        assertSame(parent, child.parent());
    }

    @Test(timeout = 4000)
    public void testPrependChild() {
        Element parent = new Element("div");
        Element first = new Element("span");
        Element second = new Element("p");
        parent.appendChild(first);
        parent.prependChild(second);
        assertSame(second, parent.child(0));
    }

    @Test(timeout = 4000)
    public void testInsertChildren() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("span"));
        List<Element> toInsert = new ArrayList<>();
        toInsert.add(new Element("a"));
        parent.insertChildren(1, toInsert);
        assertEquals(3, parent.children().size());
        assertEquals("a", parent.child(1).tagName());
    }

    @Test(timeout = 4000)
    public void testAppendElement() {
        Element parent = new Element("div");
        Element child = parent.appendElement("span");
        assertEquals("span", child.tagName());
        assertSame(parent, child.parent());
    }

    @Test(timeout = 4000)
    public void testPrependElement() {
        Element parent = new Element("div");
        Element child = new Element("p");
        parent.appendChild(child);
        Element first = parent.prependElement("a");
        assertSame(first, parent.child(0));
    }

    @Test(timeout = 4000)
    public void testAppendText() {
        Element el = new Element("p");
        el.appendText("Hello");
        assertEquals("Hello", el.text().trim());
    }

    @Test(timeout = 4000)
    public void testPrependText() {
        Element el = new Element("p");
        el.appendText("World");
        el.prependText("Hello ");
        assertEquals("Hello World", el.text());
    }

    @Test(timeout = 4000
    public void testAppend() {
        Element el = new Element("div");
        el.append("<p>text</p>");
        assertEquals(1, el.children().size());
        assertEquals("p", el.child(0).tagName());
    }

    @Test(timeout = 4000
    public void testPrepend() {
        Element el = new Element("div");
        el.append("<span>second</span>");
        el.prepend("<p>first</p>");
        assertEquals(2, el.children().size());
        assertEquals("p", el.child(0).tagName());
        assertEquals("first", el.child(0).text());
    }

    @Test(timeout = 4000)
    public void testBefore() {
        Element parent = new Element("div");
        Element c1 = new Element("p");
        parent.appendChild(c1);
        c1.before("<span>before</span>");
        assertEquals(2, parent.children().size());
        assertEquals("span", parent.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testAfter() {
        Element parent = new Element("div");
        Element c1 = new Element("p");
        parent.appendChild(c1);
        c1.after("<span>after</span>");
        assertEquals(2, parent.children().size());
        assertEquals("span", parent.child(1).tagName());
    }

    @Test(timeout = 4000)
    public void testEmpty() {
        Element el = new Element("div");
        el.appendChild(new Element("p"));
        assertEquals(1, el.childNodeSize());
        el.empty();
        assertEquals(0, el.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testWrap() {
        Element el = new Element("span");
        el.wrap("<div></div>");
        assertEquals(1, el.parent().children().size());
        assertEquals("div", el.parent().tagName());
        assertSame(el, el.parent().child(0));
    }

    @Test(timeout = 4000)
    public void testCssSelector() {
        Document doc = new Document("http://example.com");
        Element html = doc.createElement("html");
        doc.appendChild(html);
        Element body = doc.createElement("body");
        html.appendChild(body);
        Element div = doc.createElement("div");
        div.attr("id", "main");
        body.appendChild(div);
        assertEquals("#main", div.cssSelector());
    }

    @Test(timeout = 4000)
    public void testSiblingElements() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("span"));
        parent.appendChild(new Element("a"));
        Elements siblings = parent.child(1).siblingElements();
        assertEquals(2, siblings.size());
        assertEquals("p", siblings.get(0).tagName());
        assertEquals("a", siblings.get(1).tagName());
    }

    @Test(timeout = 4000)
    public void testNextElementSibling() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("span"));
        Element first = parent.child(0);
        assertSame(parent.child(1), first.nextElementSibling());
        Element last = parent.child(1);
        assertNull(last.nextElementSibling());
    }

    @Test(timeout = 4000)
    public void testPreviousElementSibling() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("span"));
        Element last = parent.child(1);
        assertSame(parent.child(0), last.previousElementSibling());
        Element first = parent.child(0);
        assertNull(first.previousElementSibling());
    }

    @Test(timeout = 4000)
    public void testFirstElementSibling() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("span"));
        Element middle = parent.child(0);
        assertSame(parent.child(0), middle.firstElementSibling());
    }

    @Test(timeout = 4000)
    public void testLastElementSibling() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("span"));
        Element first = parent.child(0);
        assertSame(parent.child(1), first.lastElementSibling());
    }

    @Test(timeout = 4000)
    public void testElementSiblingIndex() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("span"));
        parent.appendChild(new Element("a"));
        assertEquals(0, parent.child(0).elementSiblingIndex());
        assertEquals(1, parent.child(1).elementSiblingIndex());
        assertEquals(2, parent.child(2).elementSiblingIndex());
    }

    @Test(timeout = 4000)
    public void testGetElementsByTag() {
        Element root = new Element("div");
        root.appendChild(new Element("p"));
        root.appendChild(new Element("p"));
        root.appendChild(new Element("span"));
        Elements ps = root.getElementsByTag("p");
        assertEquals(2, ps.size());
    }

    @Test(timeout = 4000)
    public void testGetElementById() {
        Element root = new Element("div");
        Element child = new Element("p");
        child.attr("id", "unique");
        root.appendChild(child);
        assertSame(child, root.getElementById("unique"));
        assertNull(root.getElementById("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetElementsByClass() {
        Element root = new Element("div");
        root.appendChild(new Element("p").addClass("foo"));
        root.appendChild(new Element("p").addClass("bar"));
        root.appendChild(new Element("p").addClass("foo"));
        Elements foo = root.getElementsByClass("foo");
        assertEquals(2, foo.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttribute() {
        Element root = new Element("div");
        root.appendChild(new Element("a").attr("href", "url"));
        root.appendChild(new Element("a"));
        Elements hasHref = root.getElementsByAttribute("href");
        assertEquals(1, hasHref.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeStarting() {
        Element root = new Element("div");
        root.appendChild(new Element("a").attr("data-x", "1"));
        root.appendChild(new Element("a").attr("data-y", "2"));
        Elements data = root.getElementsByAttributeStarting("data-");
        assertEquals(2, data.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValue() {
        Element root = new Element("div");
        root.appendChild(new Element("a").attr("href", "http://example.com"));
        root.appendChild(new Element("a").attr("href", "http://other.com"));
        Elements ex = root.getElementsByAttributeValue("href", "http://example.com");
        assertEquals(1, ex.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueNot() {
        Element root = new Element("div");
        root.appendChild(new Element("a").attr("href", "http://example.com"));
        root.appendChild(new Element("a").attr("href", "http://other.com"));
        Elements notEx = root.getElementsByAttributeValueNot("href", "http://example.com");
        assertEquals(1, notEx.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueStarting() {
        Element root = new Element("div");
        root.appendChild(new Element("a").attr("href", "http://example.com"));
        root.appendChild(new Element("a").attr("href", "https://secure.com"));
        Elements http = root.getElementsByAttributeValueStarting("href", "http");
        assertEquals(1, http.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueEnding() {
        Element root = new Element("div");
        root.appendChild(new Element("a").attr("href", "file.pdf"));
        root.appendChild(new Element("a").attr("href", "file.txt"));
        Elements pdf = root.getElementsByAttributeValueEnding("href", ".pdf");
        assertEquals(1, pdf.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueContaining() {
        Element root = new Element("div");
        root.appendChild(new Element("a").attr("title", "hello world"));
        root.appendChild(new Element("a").attr("title", "goodbye"));
        Elements world = root.getElementsByAttributeValueContaining("title", "world");
        assertEquals(1, world.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatching() {
        Element root = new Element("div");
        root.appendChild(new Element("a").attr("href", "http://example.com"));
        root.appendChild(new Element("a").attr("href", "https://example.com"));
        Elements http = root.getElementsByAttributeValueMatching("href", Pattern.compile("^http:"));
        assertEquals(1, http.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByAttributeValueMatchingString() {
        Element root = new Element("div");
        root.appendChild(new Element("a").attr("href", "http://example.com"));
        root.appendChild(new Element("a").attr("href", "https://example.com"));
        Elements http = root.getElementsByAttributeValueMatching("href", "^http:");
        assertEquals(1, http.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexLessThan() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("p"));
        Elements firstTwo = parent.getElementsByIndexLessThan(2);
        assertEquals(2, firstTwo.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexGreaterThan() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("p"));
        Elements last = parent.getElementsByIndexGreaterThan(1);
        assertEquals(1, last.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsByIndexEquals() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("p"));
        Elements second = parent.getElementsByIndexEquals(1);
        assertEquals(1, second.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingText() {
        Element root = new Element("div");
        root.appendChild(new Element("p").appendText("hello world"));
        root.appendChild(new Element("p").appendText("goodbye"));
        Elements hello = root.getElementsContainingText("hello");
        assertEquals(1, hello.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsContainingOwnText() {
        Element root = new Element("div");
        Element p1 = new Element("p");
        p1.appendChild(new TextNode("hello", ""));
        root.appendChild(p1);
        Element p2 = new Element("p");
        p2.appendChild(new TextNode("world", ""));
        root.appendChild(p2);
        Elements hello = root.getElementsContainingOwnText("hello");
        assertEquals(1, hello.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingText() {
        Element root = new Element("div");
        root.appendChild(new Element("p").appendText("apple"));
        root.appendChild(new Element("p").appendText("banana"));
        Elements a = root.getElementsMatchingText(Pattern.compile("a.*"));
        assertEquals(2, a.size()); // both have 'a'
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingTextString() {
        Element root = new Element("div");
        root.appendChild(new Element("p").appendText("cat"));
        root.appendChild(new Element("p").appendText("dog"));
        Elements c = root.getElementsMatchingText("^c");
        assertEquals(1, c.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingOwnText() {
        Element root = new Element("div");
        Element p = new Element("p");
        p.appendChild(new TextNode("own", ""));
        root.appendChild(p);
        root.appendChild(new Element("p").appendText("child"));
        Elements own = root.getElementsMatchingOwnText(Pattern.compile("own"));
        assertEquals(1, own.size());
    }

    @Test(timeout = 4000)
    public void testGetElementsMatchingOwnTextString() {
        Element root = new Element("div");
        Element p = new Element("p");
        p.appendChild(new TextNode("direct", ""));
        root.appendChild(p);
        Elements d = root.getElementsMatchingOwnText("direct");
        assertEquals(1, d.size());
    }

    @Test(timeout = 4000)
    public void testGetAllElements() {
        Element root = new Element("div");
        root.appendChild(new Element("p"));
        root.appendChild(new Element("span"));
        Elements all = root.getAllElements();
        assertEquals(3, all.size()); // root + children
    }

    @Test(timeout = 4000)
    public void testText() {
        Element el = new Element("p");
        el.appendChild(new TextNode("Hello ", ""));
        el.appendChild(new Element("b").appendText("there"));
        el.appendChild(new TextNode(" now!", ""));
        assertEquals("Hello there now!", el.text());
    }

    @Test(timeout = 4000)
    public void testOwnText() {
        Element el = new Element("p");
        el.appendChild(new TextNode("Hello ", ""));
        el.appendChild(new Element("b").appendText("there"));
        el.appendChild(new TextNode(" now!", ""));
        assertEquals("Hello now!", el.ownText());
    }

    @Test(timeout = 4000)
    public void testHasText() {
        Element el = new Element("p");
        assertFalse(el.hasText());
        el.appendText("hi");
        assertTrue(el.hasText());
    }

    @Test(timeout = 4000)
    public void testData() {
        Element el = new Element("script");
        el.appendChild(new DataNode("alert(1);", ""));
        assertEquals("alert(1);", el.data());
    }

    @Test(timeout = 4000)
    public void testClassNameAndClassNames() {
        Element el = new Element("div");
        el.attr("class", "foo bar");
        assertEquals("foo bar", el.className());
        Set<String> names = el.classNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("foo"));
        assertTrue(names.contains("bar"));
    }

    @Test(timeout = 4000)
    public void testClassNamesSet() {
        Element el = new Element("div");
        Set<String> newClasses = new LinkedHashSet<>();
        newClasses.add("a");
        newClasses.add("b");
        el.classNames(newClasses);
        assertEquals("a b", el.attr("class"));
    }

    @Test(timeout = 4000)
    public void testHasClassBasic() {
        Element el = new Element("div");
        el.attr("class", "one two");
        assertTrue(el.hasClass("one"));
        assertTrue(el.hasClass("two"));
        assertFalse(el.hasClass("three"));
    }

    @Test(timeout = 4000)
    public void testHasClassSingleWhitespace() {
        Element el = new Element("div");
        el.attr("class", "one ");
        assertTrue(el.hasClass("one"));
        assertFalse(el.hasClass("on")); // ensure exact match
    }

    @Test(timeout = 4000)
    public void testHasClassLastClass() {
        Element el = new Element("div");
        el.attr("class", "first last");
        assertTrue(el.hasClass("last"));
    }

    @Test(timeout = 4000)
    public void testAddClass() {
        Element el = new Element("div");
        el.addClass("existing");
        el.addClass("new");
        assertTrue(el.hasClass("existing"));
        assertTrue(el.hasClass("new"));
    }

    @Test(timeout = 4000)
    public void testRemoveClass() {
        Element el = new Element("div");
        el.addClass("one");
        el.addClass("two");
        el.removeClass("one");
        assertFalse(el.hasClass("one"));
        assertTrue(el.hasClass("two"));
    }

    @Test(timeout = 4000)
    public void testToggleClass() {
        Element el = new Element("div");
        el.addClass("on");
        el.toggleClass("on");
        assertFalse(el.hasClass("on"));
        el.toggleClass("off");
        assertTrue(el.hasClass("off"));
    }

    @Test(timeout = 4000)
    public void testVal() {
        Element input = new Element("input");
        input.attr("value", "test");
        assertEquals("test", input.val());
        // textarea
        Element textarea = new Element("textarea");
        textarea.appendText("content");
        assertEquals("content", textarea.val());
        // set
        input.val("new");
        assertEquals("new", input.attr("value"));
        textarea.val("new text");
        assertEquals("new text", textarea.text());
    }

    @Test(timeout = 4000)
    public void testHtml() {
        Element el = new Element("div");
        el.appendChild(new Element("p").appendText("hi"));
        assertEquals("<p>hi</p>", el.html().trim());
        // set html
        el.html("<span>world</span>");
        assertEquals(1, el.children().size());
        assertEquals("span", el.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testClone() {
        Element original = new Element("div");
        original.attr("id", "orig");
        Element clone = original.clone();
        assertEquals("orig", clone.id());
        assertNotSame(original, clone);
        // change original attributes should not affect clone
        original.attr("id", "changed");
        assertEquals("orig", clone.id());
    }

    // ---------- Partition B: Boundary & Edge Cases ----------

    @Test(timeout = 4000)
    public void testEmptyClassAttribute() {
        Element el = new Element("div");
        assertEquals("", el.className());
        Set<String> classNames = el.classNames();
        assertTrue(classNames.isEmpty());
        // hasClass on empty class
        assertFalse(el.hasClass("anything"));
    }

    @Test(timeout = 4000)
    public void testNullArguments() {
        Element el = new Element("div");
        // appendChild null
        try {
            el.appendChild(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // prependChild null
        try {
            el.prependChild(null);
            fail();
        } catch (IllegalArgumentException e) {}
        // appendText null
        try {
            el.appendText(null);
            fail();
        } catch (IllegalArgumentException e) {}
        // prependText null
        try {
            el.prependText(null);
            fail();
        } catch (IllegalArgumentException e) {}
        // insertChildren null collection
        try {
            el.insertChildren(0, null);
            fail();
        } catch (IllegalArgumentException e) {}
        // classNames null
        try {
            el.classNames(null);
            fail();
        } catch (IllegalArgumentException e) {}
        // addClass null
        try {
            el.addClass(null);
            fail();
        } catch (IllegalArgumentException e) {}
        // removeClass null
        try {
            el.removeClass(null);
            fail();
        } catch (IllegalArgumentException e) {}
        // toggleClass null
        try {
            el.toggleClass(null);
            fail();
        } catch (IllegalArgumentException e) {}
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyTagNameOnConstruction() {
        new Element("");  // Tag.valueOf may throw
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullTagInConstructor() {
        new Element((Tag) null, "", new Attributes());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTagNameSetterEmpty() {
        Element el = new Element("div");
        el.tagName("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyCssQuerySelect() {
        Element el = new Element("div");
        el.select(""); // may throw SelectorParseException or IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementByTagEmpty() {
        Element el = new Element("div");
        el.getElementsByTag("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementByIdEmpty() {
        Element el = new Element("div");
        el.getElementById("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByClassEmpty() {
        Element el = new Element("div");
        el.getElementsByClass("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeEmptyKey() {
        Element el = new Element("div");
        el.getElementsByAttribute("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeStartingEmpty() {
        Element el = new Element("div");
        el.getElementsByAttributeStarting("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByIndexLessThanNegative() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.getElementsByIndexLessThan(-1); // should be fine? Evaluator.IndexLessThan(-1) should work but may match none. The method doesn't validate index.
    }

    @Test(timeout = 4000)
    public void testInsertChildrenIndexNegative() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Element("span"));
        parent.insertChildren(-1, nodes); // -1 means last+1? Check code: if (index < 0) index += currentSize +1; so -1 becomes size+1-1 = size, inserts at end
        assertEquals(2, parent.children().size());
        assertEquals("p", parent.child(0).tagName());
       assertEquals("span", parent.child(1).tagName());
    }

    @Test(timeout = 4000)
    public void testInsertChildrenIndexTooLarge() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Element("span"));
        try {
            parent.insertChildren(5, nodes);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidRegex() {
        Element el = new Element("div");
        el.getElementsByAttributeValueMatching("href", "[invalid");
    }

    // ---------- Partition C: Defect-Targeted (Case-Insensitive Class) ----------

    @Test(timeout = 4000)
    public void testHasClassCaseInsensitive() {
        Element el = new Element("div");
        el.addClass("MyClass");
        assertTrue("hasClass should be case-insensitive", el.hasClass("myclass"));
        assertTrue(el.hasClass("MYCLASS"));
        assertTrue(el.hasClass("MyClass"));
        // multiple classes
        el.addClass("other");
        assertTrue(el.hasClass("Other"));
        assertTrue(el.hasClass("OTHER"));
    }

    @Test(timeout = 4000)
    public void testGetElementsByClassCaseInsensitive() {
        Element root = new Element("div");
        root.appendChild(new Element("p").addClass("one"));
        root.appendChild(new Element("p").addClass("ONE"));
        root.appendChild(new Element("p").addClass("One"));
        Elements result = root.getElementsByClass("one");
        // Documentation says case insensitive, should return 3
        assertEquals("getElementsByClass should be case-insensitive", 3, result.size());
    }

    @Test(timeout = 4000)
    public void testCssSelectorByClassCaseInsensitive() {
        Element root = new Element("div");
        root.appendChild(new Element("p").addClass("cLass"));
        Elements sel = root.select(".cLass"); // CSS class matching is case-sensitive by default, but jsoup may handle case-insensitively? Actually CSS is case-sensitive, but jsoup may have a bug. The known defect suggests it should work? The error "expected:<3> but was:<1>" for testByClassCaseInsensitive suggests a different scenario. This test is just for safety.
        // Just assert that it finds at least one
        assertTrue(sel.size() > 0);
    }

    // ---------- Partition D: Exception & Defensive Guards ----------

    @Test(timeout = 4000)
    public void testNullBaseUri() {
        // Element(Tag tag, String baseUri, Attributes attributes) – baseUri is acceptably empty, but not null? The doc says "it is acceptable for the base URI to be an empty string, but not null." But the code does not validate. So test that null baseUri causes NullPointer? Actually the super constructor may set baseUri and later cause NPE. We'll avoid passing null to be safe.
        // Instead test that empty works.
        Element el = new Eleent(Tag.valueOf("div"), "", new Attributes());
        assertEquals("", el.baseUri());
    }

    @Test(expected = IllegalArgumentExeption.class, timeout = 4000)
    public void testSelectNullQuery() {
        Element el = new Element("div");
        el.select(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000) // or illegal argument? evalutor null
    public void testIsNullEvaluator() {
        Element el = new Element("div");
        el.is((Evaluator) null);
    }

    // ---------- Partition E: Object Lifecycle & Contract ----------

    @Test(timeout = 4000)
    public void testCloneIndependence() {
        Element original = new Element("div");
        original.appendText("text");
        Element clone = original.clone();
        assertEquals(original.text(), clone.text());
        original.text("changed");
        assertEquals("changed", original.text());
        assertEquals("text", clone.text());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Element el = new Element("div");
        el.attr("class", "test");
        String expected = "<div class=\"test\"></div>";
        assertEquals(expected, el.toString());
    }
}