package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.jsoup.nodes.Element
 *
 * Defects4J Known Faults Targeted:
 * 1. siblingElements() including itself:
 *    - In Element.java: siblingElements() returns parent().children() which contains `this`.
 *    - Specification: "An element is not a sibling of itself, so will not be included in the returned list."
 *    - Defect Manifestation: siblingElements().size() is 3 instead of 2 for a parent with 3 children.
 * 2. siblingElements() on an orphan element:
 *    - Calling siblingElements() when parent() == null leads to NullPointerException instead of returning
 *      an empty Elements list (as specified: "If the element has no sibling elements, returns an empty list.").
 *
 * Logical Partitions & Branches Covered:
 * - Partition A: Core Functional Logic & State Transitions (getters, setters, traversal, tag change, node modifications)
 * - Partition B: Boundary Value Analysis (empty strings, single child, first/last elements, index boundaries)
 * - Partition C: Defect-Targeted Branch Zone (siblingElements self-exclusion and null-parent handling)
 * - Partition D: Exception & Defensive Guard Paths (null tag, null child, empty tag name, invalid regex pattern)
 * - Partition E: Object Lifecycle & Contract Integrity (equals, hashCode, clone independence)
 */
public class ElementGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testElementIsNotASiblingOfItself() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element c1 = parent.appendElement("p");
        Element c2 = parent.appendElement("p");
        Element c3 = parent.appendElement("p");

        Elements siblings = c1.siblingElements();
        // Ground truth defect: siblingElements returned parent().children(), yielding 3 instead of 2
        assertEquals(2, siblings.size());
        assertFalse(siblings.contains(c1));
        assertTrue(siblings.contains(c2));
        assertTrue(siblings.contains(c3));
    }

    @Test(timeout = 4000)
    public void testOrphanNodeReturnsEmptyListForSiblingElements() {
        Element orphan = new Element(Tag.valueOf("span"), "");
        // Ground truth defect: Calling orphan.siblingElements() throws NullPointerException on parent().children()
        Elements siblings = orphan.siblingElements();
        assertNotNull(siblings);
        assertEquals(0, siblings.size());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & DOM Navigation
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndBasicGetters() {
        Tag tag = Tag.valueOf("div");
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        attrs.put("data-test", "true");

        Element el = new Element(tag, "http://example.com/", attrs);
        assertEquals("div", el.nodeName());
        assertEquals("div", el.tagName());
        assertSame(tag, el.tag());
        assertTrue(el.isBlock());
        assertEquals("main", el.id());
        assertEquals("http://example.com/", el.baseUri());

        Map<String, String> dataset = el.dataset();
        assertEquals("true", dataset.get("test"));

        Element simpleEl = new Element(Tag.valueOf("span"), "");
        assertEquals("span", simpleEl.nodeName());
        assertFalse(simpleEl.isBlock());
        assertEquals("", simpleEl.id());
    }

    @Test(timeout = 4000)
    public void testChangeTagName() {
        Element el = new Element(Tag.valueOf("span"), "");
        assertFalse(el.isBlock());

        Element chained = el.tagName("div");
        assertSame(el, chained);
        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());
        assertTrue(el.isBlock());
    }

    @Test(timeout = 4000)
    public void testChildrenTextNodesAndDataNodesFiltering() {
        Element parent = new Element(Tag.valueOf("p"), "");
        TextNode t1 = new TextNode("One ", "");
        Element childEl = new Element(Tag.valueOf("span"), "");
        childEl.text("Two");
        TextNode t2 = new TextNode(" Three", "");
        DataNode d1 = new DataNode("var x = 1;", "");

        parent.appendChild(t1);
        parent.appendChild(childEl);
        parent.appendChild(t2);
        parent.appendChild(d1);

        Elements children = parent.children();
        assertEquals(1, children.size());
        assertSame(childEl, children.get(0));
        assertSame(childEl, parent.child(0));

        List<TextNode> textNodes = parent.textNodes();
        assertEquals(2, textNodes.size());
        assertSame(t1, textNodes.get(0));
        assertSame(t2, textNodes.get(1));

        List<DataNode> dataNodes = parent.dataNodes();
        assertEquals(1, dataNodes.size());
        assertSame(d1, dataNodes.get(0));
    }

    @Test(timeout = 4000)
    public void testParentsAndAncestors() {
        Element root = new Element(Tag.valueOf("html"), "");
        Element body = root.appendElement("body");
        Element div = body.appendElement("div");
        Element p = div.appendElement("p");

        Elements parents = p.parents();
        assertEquals(3, parents.size());
        assertSame(div, parents.get(0));
        assertSame(body, parents.get(1));
        assertSame(root, parents.get(2));

        Element syntheticRoot = new Element(Tag.valueOf("#root"), "");
        Element directChild = syntheticRoot.appendElement("div");
        assertEquals(0, directChild.parents().size());
    }

    @Test(timeout = 4000)
    public void testSiblingNavigationBranches() {
        Element parent = new Element(Tag.valueOf("ul"), "");
        Element li1 = parent.appendElement("li").text("1");
        Element li2 = parent.appendElement("li").text("2");
        Element li3 = parent.appendElement("li").text("3");

        assertEquals(Integer.valueOf(0), li1.elementSiblingIndex());
        assertEquals(Integer.valueOf(1), li2.elementSiblingIndex());
        assertEquals(Integer.valueOf(2), li3.elementSiblingIndex());

        assertSame(li2, li1.nextElementSibling());
        assertSame(li3, li2.nextElementSibling());
        assertNull(li3.nextElementSibling());

        assertNull(li1.previousElementSibling());
        assertSame(li1, li2.previousElementSibling());
        assertSame(li2, li3.previousElementSibling());

        assertSame(li1, li2.firstElementSibling());
        assertSame(li3, li2.lastElementSibling());

        Element solitaryParent = new Element(Tag.valueOf("div"), "");
        Element singleChild = solitaryParent.appendElement("span");
        assertNull(singleChild.firstElementSibling());
        assertNull(singleChild.lastElementSibling());

        Element orphan = new Element(Tag.valueOf("p"), "");
        assertEquals(Integer.valueOf(0), orphan.elementSiblingIndex());
    }

    @Test(timeout = 4000)
    public void testDomInsertionAndMutationMethods() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("p").text("Middle");

        div.prependElement("header").text("Top");
        div.appendText(" EndText");
        div.prependText("StartText ");

        assertTrue(div.text().contains("Top"));
        assertTrue(div.text().contains("Middle"));
        assertTrue(div.text().contains("StartText"));
        assertTrue(div.text().contains("EndText"));

        Element container = new Element(Tag.valueOf("div"), "");
        Element target = container.appendElement("span").text("Target");

        target.before("<b>BeforeHtml</b>");
        target.after("<i>AfterHtml</i>");
        target.before(new TextNode("BeforeNode ", ""));
        target.after(new TextNode(" AfterNode", ""));

        String html = container.html();
        assertTrue(html.contains("BeforeHtml"));
        assertTrue(html.contains("AfterHtml"));
        assertTrue(html.contains("BeforeNode"));
        assertTrue(html.contains("AfterNode"));

        target.wrap("<div class='wrapper'></div>");
        assertTrue(container.html().contains("class=\"wrapper\""));
    }

    @Test(timeout = 4000)
    public void testTextAndOwnTextExtraction() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendText("One ");
        Element span = p.appendElement("span").text("Two");
        p.appendText(" Three");
        p.appendElement("br");
        p.appendText("Four");

        assertEquals("One Two Three Four", p.text());
        assertEquals("One Three Four", p.ownText());
        assertTrue(p.hasText());

        Element emptyEl = new Element(Tag.valueOf("div"), "");
        assertEquals("", emptyEl.text());
        assertEquals("", emptyEl.ownText());
        assertFalse(emptyEl.hasText());

        Element blankTextEl = new Element(Tag.valueOf("div"), "");
        blankTextEl.appendText("   ");
        assertFalse(blankTextEl.hasText());
    }

    @Test(timeout = 4000)
    public void testDataExtraction() {
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("var a = 1;", ""));
        Element nested = script.appendElement("nested");
        nested.appendChild(new DataNode("var b = 2;", ""));

        assertEquals("var a = 1;var b = 2;", script.data());
    }

    @Test(timeout = 4000)
    public void testClassManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.className());
        assertFalse(el.hasClass("active"));

        el.addClass("active");
        assertTrue(el.hasClass("active"));
        assertTrue(el.hasClass("ACTIVE")); // case-insensitive check
        assertEquals("active", el.className());

        el.addClass("second");
        Set<String> classNames = el.classNames();
        assertEquals(2, classNames.size());
        assertTrue(classNames.contains("active"));
        assertTrue(classNames.contains("second"));

        el.toggleClass("active");
        assertFalse(el.hasClass("active"));
        assertTrue(el.hasClass("second"));

        el.toggleClass("active");
        assertTrue(el.hasClass("active"));

        el.removeClass("active");
        assertFalse(el.hasClass("active"));
        assertTrue(el.hasClass("second"));

        Set<String> newClasses = new HashSet<String>(Arrays.asList("c1", "c2"));
        el.classNames(newClasses);
        assertTrue(el.hasClass("c1"));
        assertTrue(el.hasClass("c2"));
        assertFalse(el.hasClass("second"));
    }

    @Test(timeout = 4000)
    public void testValMethodFormElements() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("value", "foo");
        assertEquals("foo", input.val());
        input.val("bar");
        assertEquals("bar", input.attr("value"));
        assertEquals("bar", input.val());

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("comment text");
        assertEquals("comment text", textarea.val());
        textarea.val("updated comment");
        assertEquals("updated comment", textarea.text());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlAndFormatting() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.attr("class", "lead");
        p.text("Hello World");
        assertEquals("<p class=\"lead\">Hello World</p>", p.outerHtml());
        assertEquals("Hello World", p.html());

        Element img = new Element(Tag.valueOf("img"), "");
        img.attr("src", "image.png");
        assertEquals("<img src=\"image.png\" />", img.outerHtml());

        p.html("<span>Replaced</span>");
        assertEquals("<span>Replaced</span>", p.html());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespaceLogic() {
        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.appendText("  line 1\n  line 2  ");
        assertTrue(pre.preserveWhitespace());
        assertEquals("  line 1\n  line 2  ", pre.text());

        Element child = pre.appendElement("code").text("  code block  ");
        assertTrue(child.preserveWhitespace());
    }

    // =========================================================================
    // Partition B: BVA & DOM Collector Selectors
    // =========================================================================

    @Test(timeout = 4000)
    public void testSelectorAndElementCollectorMethods() {
        Element root = new Element(Tag.valueOf("div"), "");
        root.attr("id", "root-id");

        Element child1 = root.appendElement("p").attr("class", "my-class test").text("First paragraph content");
        Element child2 = root.appendElement("p").attr("class", "other").attr("data-role", "admin").text("Second item");
        Element child3 = root.appendElement("span").attr("data-role", "user").text("Own span");

        assertEquals(1, root.getElementsByTag("span").size());
        assertSame(child3, root.getElementsByTag("SPAN").get(0));

        assertSame(root, root.getElementById("root-id"));
        assertNull(root.getElementById("non-existent"));

        assertEquals(1, root.getElementsByClass("my-class").size());
        assertSame(child1, root.getElementsByClass("MY-CLASS").get(0));

        assertEquals(2, root.getElementsByAttribute("data-role").size());
        assertEquals(2, root.getElementsByAttributeStarting("data-").size());
        assertEquals(1, root.getElementsByAttributeValue("data-role", "admin").size());
        assertEquals(1, root.getElementsByAttributeValueStarting("data-role", "ad").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("data-role", "min").size());
        assertEquals(1, root.getElementsByAttributeValueContaining("data-role", "dmi").size());
        assertEquals(1, root.getElementsByAttributeValueMatching("data-role", Pattern.compile("^adm.*$")).size());
        assertEquals(1, root.getElementsByAttributeValueMatching("data-role", "^adm.*$").size());

        assertEquals(1, root.getElementsByAttributeValueNot("data-role", "admin").size());

        assertEquals(1, root.getElementsByIndexLessThan(1).size());
        assertEquals(1, root.getElementsByIndexGreaterThan(1).size());
        assertEquals(1, root.getElementsByIndexEquals(1).size());

        assertEquals(1, root.getElementsContainingText("paragraph").size());
        assertEquals(1, root.getElementsContainingOwnText("First").size());
        assertEquals(1, root.getElementsMatchingText(Pattern.compile(".*content.*")).size());
        assertEquals(1, root.getElementsMatchingText(".*content.*").size());
        assertEquals(1, root.getElementsMatchingOwnText(Pattern.compile(".*paragraph.*")).size());
        assertEquals(1, root.getElementsMatchingOwnText(".*paragraph.*").size());

        assertEquals(4, root.getAllElements().size()); // root, child1, child2, child3
        assertEquals(1, root.select("p.my-class").size());
    }

    // =========================================================================
    // Partition D: Defensive Programming & Exception Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorRejectsNullTag() {
        new Element(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetEmptyTagNameThrowsException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullChildThrowsException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrependNullChildThrowsException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.prependChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullHtmlThrowsException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.append(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrependNullHtmlThrowsException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.prepend(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNullTextThrowsException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.text(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidRegexThrowsIllegalArgumentException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttributeValueMatching("key", "[unclosed");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidTextRegexThrowsIllegalArgumentException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingText("[unclosed");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidOwnTextRegexThrowsIllegalArgumentException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingOwnText("[unclosed");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (equals, hashCode, clone)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeIdentityContract() {
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        Element el2 = new Element(Tag.valueOf("div"), "http://example.com");

        assertTrue(el1.equals(el1));
        assertFalse(el1.equals(el2));
        assertFalse(el1.equals(null));
        assertFalse(el1.equals("notAnElement"));

        assertEquals(el1.hashCode(), el1.hashCode());
    }

    @Test(timeout = 4000)
    public void testDeepCloneIndependence() {
        Element original = new Element(Tag.valueOf("div"), "http://example.com");
        original.attr("class", "my-class");
        original.appendElement("span").text("Child Text");

        Element cloned = original.clone();
        assertNotSame(original, cloned);
        assertEquals(original.outerHtml(), cloned.outerHtml());

        cloned.addClass("modified");
        assertFalse(original.hasClass("modified"));
        assertTrue(cloned.hasClass("modified"));

        cloned.child(0).text("Modified Child Text");
        assertEquals("Child Text", original.child(0).text());
        assertEquals("Modified Child Text", cloned.child(0).text());
    }
}