package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

/*
 [Branch & Defect Analysis Matrix]
 ------------------------------------------------------------------------------------------------------------------
 Target Method                  | Decision / Condition Branches Covered               | Boundary / Fault Notes
 ------------------------------------------------------------------------------------------------------------------
 Element(Tag, baseUri, Attr)    | tag == null, tag != null                            | Validate.notNull check
 id()                           | attr("id") == null vs present                       | Returns "" when absent
 parents(), accumulateParents() | parent != null, parent == null, parent is #root     | Loop termination at #root
 child(), children()            | Element child vs non-Element (Text/Data), empty     | Node filtering branch
 select()                       | selector match vs empty                             | Delegation to Selector
 appendChild(), prependChild()  | null child guard, index 0 vs childNodes.size()      | Parent linkage assertion
 append(), prepend()            | Parse HTML fragment into Element, reverse order     | DEFECT: table context handling
 wrap()                         | null wrap, deep single wrap, multi-child remainder  | deepest child descent
 siblingElements()              | parent null vs present, sibling elements            | Sibling list retrieval
 next/prev/first/lastSibling    | index bounds (first, last, single child, multi)     | null checks when at boundaries
 elementSiblingIndex()          | parent == null (0), parent != null                  | Relative index calculation
 getElementsBy* family          | Id, Tag, Class, Attr, AttrPrefix/Suffix/Regex, Idx  | Evaluator branching
 text(), preserveWhitespace()   | whitespace normalisation, block element spacing     | <pre> / parent whitespace
 hasText()                      | Blank TextNode vs non-blank, nested elements        | Recursive text detection
 data()                         | DataNode, child Elements, mixed child nodes         | String accumulator
 className(), classNames()      | No class attr, single class, multiple whitespace    | LinkedHashSet backing
 hasClass(), toggleClass()      | Add existing, remove existing, toggle presence      | Attribute mutation
 val()                          | "textarea" tag vs other input tags                  | Text vs "value" attribute
 outerHtml(), html()            | Tag.isEmpty() self-closing (/>), block indent       | Empty void tags vs paired
 equals(), hashCode()           | Identity (==), non-Element, super diff, tag diff    | Contract integrity
 ------------------------------------------------------------------------------------------------------------------
 DEFECT UNDER TEST:
 - Parser body fragment ignores parent context when appending/prepending table rows (<tr>) directly to <table>,
   causing rows to be erroneously wrapped in redundant nested <table> elements.
*/

public class ElementGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesAndAccessors() {
        Tag divTag = Tag.valueOf("div");
        Element el = new Element(divTag, "http://example.com/");

        assertEquals("div", el.nodeName());
        assertEquals("div", el.tagName());
        assertSame(divTag, el.tag());
        assertTrue(el.isBlock());
        assertEquals("http://example.com/", el.baseUri());
        assertEquals("", el.id());

        el.attr("id", "main-content");
        assertEquals("main-content", el.id());

        Tag spanTag = Tag.valueOf("span");
        Element span = new Element(spanTag, "");
        assertFalse(span.isBlock());
    }

    @Test(timeout = 4000)
    public void testChildAndChildrenFiltering() {
        Element root = new Element(Tag.valueOf("div"), "");
        root.appendText("Leading text");
        Element child1 = root.appendElement("p");
        root.appendText("Middle text");
        Element child2 = root.appendElement("span");

        assertEquals(4, root.childNodes.size());
        Elements elementChildren = root.children();
        assertEquals(2, elementChildren.size());
        assertSame(child1, elementChildren.get(0));
        assertSame(child2, elementChildren.get(1));
        assertSame(child1, root.child(0));
        assertSame(child2, root.child(1));
    }

    @Test(timeout = 4000)
    public void testAppendAndPrependElementAndText() {
        Element container = new Element(Tag.valueOf("div"), "http://example.com");
        Element p = container.appendElement("p");
        p.text("Middle");

        Element h1 = container.prependElement("h1");
        h1.text("Header");

        container.prependText("Start - ");
        container.appendText(" - End");

        assertEquals(4, container.childNodes.size());
        assertTrue(container.childNodes.get(0) instanceof TextNode);
        assertSame(h1, container.childNodes.get(1));
        assertSame(p, container.childNodes.get(2));
        assertTrue(container.childNodes.get(3) instanceof TextNode);
        assertEquals("Start - Header Middle - End", container.text());
    }

    @Test(timeout = 4000)
    public void testParentAndParentsAccumulation() {
        Element root = new Element(Tag.valueOf("#root"), "");
        Element html = root.appendElement("html");
        Element body = html.appendElement("body");
        Element div = body.appendElement("div");

        assertSame(body, div.parent());
        Elements parents = div.parents();

        assertEquals(2, parents.size());
        assertSame(body, parents.get(0));
        assertSame(html, parents.get(1));

        Element standalone = new Element(Tag.valueOf("div"), "");
        assertEquals(0, standalone.parents().size());
    }

    @Test(timeout = 4000)
    public void testSiblingNavigation() {
        Element parent = new Element(Tag.valueOf("ul"), "");
        Element li1 = parent.appendElement("li");
        Element li2 = parent.appendElement("li");
        Element li3 = parent.appendElement("li");

        assertEquals(0, (int) li1.elementSiblingIndex());
        assertEquals(1, (int) li2.elementSiblingIndex());
        assertEquals(2, (int) li3.elementSiblingIndex());

        assertNull(li1.previousElementSibling());
        assertSame(li2, li1.nextElementSibling());

        assertSame(li1, li2.previousElementSibling());
        assertSame(li3, li2.nextElementSibling());

        assertSame(li2, li3.previousElementSibling());
        assertNull(li3.nextElementSibling());

        assertSame(li1, li1.firstElementSibling());
        assertSame(li3, li1.lastElementSibling());

        Elements siblings = li1.siblingElements();
        assertEquals(3, siblings.size());
    }

    @Test(timeout = 4000)
    public void testSingleChildSiblingBoundaries() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element solo = parent.appendElement("span");

        assertEquals(0, (int) solo.elementSiblingIndex());
        assertNull(solo.previousElementSibling());
        assertNull(solo.nextElementSibling());
        assertNull(solo.firstElementSibling());
        assertNull(solo.lastElementSibling());
    }

    @Test(timeout = 4000)
    public void testStandaloneElementSiblingIndex() {
        Element standalone = new Element(Tag.valueOf("p"), "");
        assertEquals(0, (int) standalone.elementSiblingIndex());
    }

    @Test(timeout = 4000)
    public void testDomQueryMethods() {
        Element doc = new Element(Tag.valueOf("div"), "");
        Element p1 = doc.appendElement("p").attr("id", "p1").attr("class", "text highlight").text("First paragraph");
        Element p2 = doc.appendElement("p").attr("id", "p2").attr("class", "text").text("Second paragraph");
        Element a = doc.appendElement("a").attr("href", "http://example.com/test").attr("target", "_blank");

        assertSame(p1, doc.getElementById("p1"));
        assertNull(doc.getElementById("non-existent"));

        Elements pElements = doc.getElementsByTag("P");
        assertEquals(2, pElements.size());

        Elements textClass = doc.getElementsByClass("TEXT");
        assertEquals(2, textClass.size());

        Elements highlightClass = doc.getElementsByClass("highlight");
        assertEquals(1, highlightClass.size());
        assertSame(p1, highlightClass.get(0));

        assertEquals(1, doc.getElementsByAttribute("target").size());
        assertEquals(1, doc.getElementsByAttributeValue("target", "_blank").size());
        assertEquals(3, doc.getElementsByAttributeValueNot("target", "_blank").size());
        assertEquals(1, doc.getElementsByAttributeValueStarting("href", "http://example").size());
        assertEquals(1, doc.getElementsByAttributeValueEnding("href", "/test").size());
        assertEquals(1, doc.getElementsByAttributeValueContaining("href", "example").size());

        assertEquals(1, doc.getElementsByIndexLessThan(1).size());
        assertEquals(1, doc.getElementsByIndexGreaterThan(1).size());
        assertEquals(1, doc.getElementsByIndexEquals(1).size());

        assertEquals(4, doc.getAllElements().size());
    }

    @Test(timeout = 4000)
    public void testSelectorIntegration() {
        Element container = new Element(Tag.valueOf("div"), "");
        Element span = container.appendElement("span").attr("class", "badge").text("42");
        Elements selected = container.select("span.badge");
        assertEquals(1, selected.size());
        assertSame(span, selected.first());
    }

    @Test(timeout = 4000)
    public void testWhitespaceAndFormattingInText() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendText("   Hello   ");
        Element span = p.appendElement("span");
        span.appendText("  World  ");
        assertEquals("Hello World", p.text());

        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.appendText("   Line 1\n   Line 2   ");
        assertEquals("Line 1\n   Line 2", pre.text());
        assertTrue(pre.preserveWhitespace());

        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("p").text("One");
        div.appendElement("p").text("Two");
        assertEquals("One Two", div.text());
    }

    @Test(timeout = 4000)
    public void testHasText() {
        Element empty = new Element(Tag.valueOf("div"), "");
        assertFalse(empty.hasText());

        Element blankText = new Element(Tag.valueOf("div"), "");
        blankText.appendText("    ");
        assertFalse(blankText.hasText());

        Element withText = new Element(Tag.valueOf("div"), "");
        withText.appendText("Content");
        assertTrue(withText.hasText());

        Element nestedWithText = new Element(Tag.valueOf("div"), "");
        nestedWithText.appendElement("p").text("Deep text");
        assertTrue(nestedWithText.hasText());
    }

    @Test(timeout = 4000)
    public void testDataRetrieval() {
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("var foo = 'bar';", ""));
        assertEquals("var foo = 'bar';", script.data());

        Element outer = new Element(Tag.valueOf("div"), "");
        outer.appendChild(script);
        assertEquals("var foo = 'bar';", outer.data());
    }

    @Test(timeout = 4000)
    public void testClassManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.className());
        assertTrue(el.classNames().isEmpty());

        el.attr("class", "btn btn-primary large");
        assertEquals("btn btn-primary large", el.className());
        Set<String> classes = el.classNames();
        assertEquals(3, classes.size());
        assertTrue(el.hasClass("btn-primary"));
        assertFalse(el.hasClass("small"));

        el.removeClass("btn-primary");
        assertFalse(el.hasClass("btn-primary"));
        assertTrue(el.hasClass("btn"));

        el.addClass("disabled");
        assertTrue(el.hasClass("disabled"));

        el.toggleClass("active");
        assertTrue(el.hasClass("active"));
        el.toggleClass("active");
        assertFalse(el.hasClass("active"));

        Set<String> customClasses = new LinkedHashSet<String>(Arrays.asList("custom1", "custom2"));
        el.classNames(customClasses);
        assertEquals("custom1 custom2", el.className());
    }

    @Test(timeout = 4000)
    public void testValMethodOnInputsAndTextarea() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("test value");
        assertEquals("test value", input.val());
        assertEquals("test value", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("multiline\ntext");
        assertEquals("multiline\ntext", textarea.val());
        assertEquals("multiline\ntext", textarea.text());
    }

    @Test(timeout = 4000)
    public void testHtmlAndEmpty() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.html("<span>Hello</span><b>World</b>");
        assertEquals(2, div.children().size());
        assertEquals("<span>Hello</span><b>World</b>", div.html().replaceAll("\\s+", ""));

        div.empty();
        assertEquals(0, div.childNodes.size());
        assertEquals("", div.html());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlVoidElements() {
        Element img = new Element(Tag.valueOf("img"), "");
        img.attr("src", "image.png");
        assertEquals("<img src=\"image.png\" />", img.outerHtml().trim());

        Element div = new Element(Tag.valueOf("div"), "");
        assertEquals("<div></div>", div.outerHtml().trim());
        assertEquals(div.outerHtml(), div.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Wrap Structure
    // =========================================================================

    @Test(timeout = 4000)
    public void testWrapSingleAndDeep() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = parent.appendElement("span").attr("id", "target");

        child.wrap("<div class=\"wrapper\"><div class=\"inner\"></div></div>");

        assertEquals(1, parent.children().size());
        Element wrapper = parent.child(0);
        assertEquals("wrapper", wrapper.className());
        Element inner = wrapper.child(0);
        assertEquals("inner", inner.className());
        assertSame(child, inner.child(0));
    }

    @Test(timeout = 4000)
    public void testWrapWithRemainderElements() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element target = parent.appendElement("span");

        target.wrap("<div class=\"first\"></div><div class=\"second\"></div>");

        Element first = parent.child(0);
        assertEquals("first", first.className());
        assertSame(target, first.child(0));
        assertEquals(2, first.children().size());
        assertEquals("second", first.child(1).className());
    }

    @Test(timeout = 4000)
    public void testWrapReturnsNullOnEmptyStructure() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element target = parent.appendElement("span");
        Element result = target.wrap("plain text without tags");
        assertNull(result);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testAppendRowToTableDefect() {
        Element table = new Element(Tag.valueOf("table"), "");
        table.append("<tr><td>1</td></tr>");
        table.append("<tr><td>2</td></tr>");

        String expected = "<table><tr><td>1</td></tr><tr><td>2</td></tr></table>";
        String actual = table.outerHtml().replaceAll("\\s+", "");
        assertEquals("Append row directly to table must not insert intermediate <table> wrapper", expected, actual);
    }

    @Test(timeout = 4000)
    public void testPrependRowToTableDefect() {
        Element table = new Element(Tag.valueOf("table"), "");
        table.append("<tr><td>1</td></tr>");
        table.prepend("<tr><td>2</td></tr>");

        String expected = "<table><tr><td>2</td></tr><tr><td>1</td></tr></table>";
        String actual = table.outerHtml().replaceAll("\\s+", "");
        assertEquals("Prepend row directly to table must not insert intermediate <table> wrapper", expected, actual);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullTag() {
        new Element(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendChildNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrependChildNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.prependChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullHtml() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.append(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrependNullHtml() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.prepend(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWrapEmptyHtml() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.wrap("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByTagEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByTag("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementByIdEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementById("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByClassEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByClass("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttribute("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTextNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.text(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testClassNamesNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.classNames(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        el1.attr("id", "test");

        Element el2 = new Element(Tag.valueOf("div"), "http://example.com");
        el2.attr("id", "test");

        Element el3 = new Element(Tag.valueOf("span"), "http://example.com");
        el3.attr("id", "test");

        Element el4 = new Element(Tag.valueOf("div"), "http://different.com");
        el4.attr("id", "test");

        assertEquals(el1, el1);
        assertEquals(el1, el2);
        assertEquals(el1.hashCode(), el2.hashCode());

        assertFalse(el1.equals("non-element"));
        assertFalse(el1.equals(null));
        assertFalse(el1.equals(el3));
        assertFalse(el1.equals(el4));
    }
}