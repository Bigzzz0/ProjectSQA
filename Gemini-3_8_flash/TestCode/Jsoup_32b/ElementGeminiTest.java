package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/*
 [Branch & Defect Analysis Matrix]
 ---------------------------------------------------------------------------------------------------------
 Targeted Decisions & Branches:
 1. Element.clone() Isolation Defect (Defects4J: ElementTest::testClonesClassnames)
    - Target: In defective versions, clone() fails to decouple classNames state from the prototype.
    - Path: el1.addClass("c1") -> clone to el2 -> modifying el1's classNames must NOT modify el2, and
      assertNotSame(el1.classNames(), el2.classNames()).
 2. Hierarchy & Siblings:
    - parent == null vs parent != null; parent == "#root" (root boundary in accumulateParents)
    - siblingElements, nextElementSibling, previousElementSibling, firstElementSibling, lastElementSibling
    - elementSiblingIndex with/without parent
 3. Child Manipulation & BVA Index:
    - insertChildren with index == 0, index == -1 (roll around to currentSize), out-of-bounds index (< 0 or > size)
    - empty(), append(), prepend(), before(), after(), wrap()
 4. DOM Selectors & Regex Evaluators:
    - PatternSyntaxException handling in getElementsByAttributeValueMatching, getElementsMatchingText,
      getElementsMatchingOwnText
    - Case sensitivity and trimming in getElementsByTag, getElementsByClass, getElementsByAttribute
 5. Text & Whitespace Preservation:
    - text() and ownText() with normal vs preserveWhitespace tags (<pre>, <textarea>)
    - <br> whitespace appending branch (appendWhitespaceIfBr)
    - Block vs inline elements whitespace separation in text accumulator
 6. Form Values & Dataset:
    - val() and val(String) on "textarea" (text backed) vs "input" (attribute backed)
    - dataset() live map modifications
 7. Class Name Operations:
    - className(), classNames(), hasClass(), addClass(), removeClass(), toggleClass()
 ---------------------------------------------------------------------------------------------------------
*/
public class ElementGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndBasicTagProperties() {
        Tag divTag = Tag.valueOf("div");
        Element el = new Element(divTag, "http://example.com");

        assertEquals("div", el.tagName());
        assertEquals("div", el.nodeName());
        assertSame(divTag, el.tag());
        assertTrue(el.isBlock());
        assertEquals("http://example.com", el.baseUri());
        assertEquals(0, el.childNodeSize());
        assertEquals("", el.id());

        el.tagName("span");
        assertEquals("span", el.tagName());
        assertEquals("span", el.nodeName());
        assertFalse(el.isBlock());
    }

    @Test(timeout = 4000)
    public void testIdAttributeOperations() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.id());

        el.attr("id", "main-content");
        assertEquals("main-content", el.id());

        el.attr("id", "");
        assertEquals("", el.id());
    }

    @Test(timeout = 4000)
    public void testDatasetOperations() {
        Attributes attrs = new Attributes();
        attrs.put("data-category", "books");
        attrs.put("title", "book list");
        Element el = new Element(Tag.valueOf("div"), "", attrs);

        Map<String, String> dataset = el.dataset();
        assertEquals(1, dataset.size());
        assertEquals("books", dataset.get("category"));

        dataset.put("author", "Arthur");
        assertEquals("Arthur", el.attr("data-author"));
    }

    @Test(timeout = 4000)
    public void testFormValueOperationsOnInputAndTextarea() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("user-val");
        assertEquals("user-val", input.val());
        assertEquals("user-val", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("initial text");
        assertEquals("initial text", textarea.val());
        assertEquals("initial text", textarea.text());

        textarea.val("updated text");
        assertEquals("updated text", textarea.val());
    }

    @Test(timeout = 4000)
    public void testTextAndOwnTextExtraction() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendText("Hello ");
        Element b = p.appendElement("b");
        b.text("Bold");
        p.appendText(" World");

        assertEquals("Hello Bold World", p.text());
        assertEquals("Hello World", p.ownText());
        assertEquals(2, p.textNodes().size());
        assertEquals(1, p.children().size());
    }

    @Test(timeout = 4000)
    public void testTextWithBrAndBlockElements() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendText("Line 1");
        div.appendElement("br");
        div.appendText("Line 2");

        Element childDiv = div.appendElement("div");
        childDiv.text("Block text");

        String result = div.text();
        assertTrue(result.contains("Line 1 Line 2"));
        assertTrue(result.contains("Block text"));
    }

    @Test(timeout = 4000)
    public void testDataNodesAndCombinedData() {
        Element script = new Element(Tag.valueOf("script"), "");
        DataNode dataNode1 = new DataNode("var a = 1;", "");
        DataNode dataNode2 = new DataNode("var b = 2;", "");
        script.appendChild(dataNode1);
        script.appendChild(dataNode2);

        List<DataNode> list = script.dataNodes();
        assertEquals(2, list.size());
        assertEquals("var a = 1;var b = 2;", script.data());
    }

    @Test(timeout = 4000)
    public void testClassAttributeManipulations() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.className());
        assertFalse(el.hasClass("active"));

        el.addClass("active");
        assertTrue(el.hasClass("active"));
        assertTrue(el.hasClass("ACTIVE")); // case-insensitive check
        assertEquals("active", el.className());

        el.addClass("selected");
        assertTrue(el.hasClass("active"));
        assertTrue(el.hasClass("selected"));
        assertEquals("active selected", el.className());

        el.removeClass("active");
        assertFalse(el.hasClass("active"));
        assertTrue(el.hasClass("selected"));

        el.toggleClass("selected");
        assertFalse(el.hasClass("selected"));

        el.toggleClass("visible");
        assertTrue(el.hasClass("visible"));

        Set<String> newClasses = new LinkedHashSet<String>();
        newClasses.add("one");
        newClasses.add("two");
        el.classNames(newClasses);
        assertEquals("one two", el.className());
        assertTrue(el.hasClass("one"));
        assertTrue(el.hasClass("two"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testInsertChildrenIndexBoundaries() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").text("first");
        parent.appendElement("p").text("second");

        // Boundary: index 0 (insert at beginning)
        List<Node> toPrepend = Collections.<Node>singletonList(new Element(Tag.valueOf("span"), "").text("zero"));
        parent.insertChildren(0, toPrepend);
        assertEquals(3, parent.childNodeSize());
        assertEquals("span", parent.child(0).tagName());

        // Boundary: index -1 (roll around to append to end: -1 + currentSize + 1 = currentSize)
        List<Node> toAppend = Collections.<Node>singletonList(new Element(Tag.valueOf("span"), "").text("last"));
        parent.insertChildren(-1, toAppend);
        assertEquals(4, parent.childNodeSize());
        assertEquals("last", parent.child(3).text());

        // Boundary: exact index equal to current size
        List<Node> toAppendExact = Collections.<Node>singletonList(new Element(Tag.valueOf("span"), "").text("end"));
        parent.insertChildren(parent.childNodeSize(), toAppendExact);
        assertEquals(5, parent.childNodeSize());
        assertEquals("end", parent.child(4).text());
    }

    @Test(timeout = 4000)
    public void testSiblingNavigationBoundariesWithNoParentOrSingleChild() {
        Element orphan = new Element(Tag.valueOf("div"), "");
        assertEquals(0, orphan.siblingElements().size());
        assertNull(orphan.nextElementSibling());
        assertNull(orphan.previousElementSibling());
        assertNull(orphan.firstElementSibling());
        assertNull(orphan.lastElementSibling());
        assertEquals(Integer.valueOf(0), orphan.elementSiblingIndex());

        // Single child of a parent
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = parent.appendElement("span");
        assertEquals(0, child.siblingElements().size());
        assertNull(child.nextElementSibling());
        assertNull(child.previousElementSibling());
        assertNull(child.firstElementSibling()); // siblings.size() not > 1
        assertNull(child.lastElementSibling());
        assertEquals(Integer.valueOf(0), child.elementSiblingIndex());
    }

    @Test(timeout = 4000)
    public void testSiblingNavigationMultiChildrenBoundaries() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element c0 = root.appendElement("c0");
        Element c1 = root.appendElement("c1");
        Element c2 = root.appendElement("c2");

        assertSame(c0, c1.previousElementSibling());
        assertNull(c0.previousElementSibling());

        assertSame(c2, c1.nextElementSibling());
        assertNull(c2.nextElementSibling());

        assertSame(c0, c1.firstElementSibling());
        assertSame(c2, c1.lastElementSibling());

        assertEquals(Integer.valueOf(0), c0.elementSiblingIndex());
        assertEquals(Integer.valueOf(1), c1.elementSiblingIndex());
        assertEquals(Integer.valueOf(2), c2.elementSiblingIndex());

        Elements siblingsOfC1 = c1.siblingElements();
        assertEquals(2, siblingsOfC1.size());
        assertFalse(siblingsOfC1.contains(c1));
        assertTrue(siblingsOfC1.contains(c0));
        assertTrue(siblingsOfC1.contains(c2));
    }

    @Test(timeout = 4000)
    public void testParentsAccumulationExcludesRoot() {
        Element root = new Element(Tag.valueOf("#root"), "");
        Element mid = root.appendElement("div");
        Element leaf = mid.appendElement("span");

        Elements parents = leaf.parents();
        assertEquals(1, parents.size());
        assertSame(mid, parents.get(0));
    }

    @Test(timeout = 4000)
    public void testHasTextBranches() {
        Element blankEl = new Element(Tag.valueOf("div"), "");
        assertFalse(blankEl.hasText());

        blankEl.appendText("   ");
        assertFalse(blankEl.hasText());

        Element nested = blankEl.appendElement("span");
        assertFalse(blankEl.hasText());

        nested.appendText("actual text");
        assertTrue(blankEl.hasText());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespaceInPre() {
        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.appendText("  line1\n  line2  ");
        assertEquals("  line1\n  line2  ", pre.text());

        Element div = new Element(Tag.valueOf("div"), "");
        div.appendText("  line1\n  line2  ");
        assertEquals("line1 line2", div.text());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Defect)
    // =========================================================================

    /**
     * Targets Defects4J fault in ElementTest::testClonesClassnames.
     * When an Element with classes is cloned, the clone must not share the Set<String> instance.
     */
    @Test(timeout = 4000)
    public void testClonesClassnamesDefectIsolation() {
        Element el1 = new Element(Tag.valueOf("div"), "").addClass("val1");
        Set<String> classes = el1.classNames();
        Element el2 = el1.clone();

        // Bug check 1: classes instance must not be identical
        assertNotSame("clone() must not share classNames reference", el1.classNames(), el2.classNames());

        // Bug check 2: modifying original set does not pollute clone
        classes.add("val2");
        assertFalse("Clone must not reflect additions to original classNames set", el2.hasClass("val2"));

        // Bug check 3: modifying clone set does not pollute original
        el2.addClass("val3");
        assertFalse("Original must not reflect additions to clone classes", el1.hasClass("val3"));
        assertTrue("Clone must reflect its own classes", el2.hasClass("val3"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullTag() {
        new Element(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTagNameEmptyGuard() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenNullGuard() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.insertChildren(0, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenNegativeOutOfBoundsGuard() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendElement("span");
        // size = 1. index = -3 -> index += 1 + 1 = -1 -> out of bounds!
        el.insertChildren(-3, Collections.<Node>singletonList(new TextNode("a", "")));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenPositiveOutOfBoundsGuard() {
        Element el = new Element(Tag.valueOf("div"), "");
        // size = 0. index = 2 -> out of bounds!
        el.insertChildren(2, Collections.<Node>singletonList(new TextNode("a", "")));
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testChildIndexOutOfBoundsGuard() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.child(0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeValueMatchingRegexSyntaxError() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttributeValueMatching("key", "[invalid regex");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingTextRegexSyntaxError() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingText("(?invalid");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingOwnTextRegexSyntaxError() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingOwnText("*invalid");
    }

    // =========================================================================
    // Partition E: DOM Manipulation, Selectors & Collector Integrations
    // =========================================================================

    @Test(timeout = 4000)
    public void testDomQueryAndFilterMethods() {
        Element root = new Element(Tag.valueOf("div"), "");
        root.attr("id", "root-id");
        root.addClass("box outer");

        Element child1 = root.appendElement("p").attr("title", "para1").text("Sample text here");
        child1.attr("data-role", "paragraph");
        Element child2 = root.appendElement("a").attr("href", "http://example.com/page").text("Link text");
        Element child3 = root.appendElement("span").text("Child span sample");

        // getElementById
        assertSame(root, root.getElementById("root-id"));
        assertNull(root.getElementById("non-existent"));

        // getElementsByTag
        assertEquals(1, root.getElementsByTag("p").size());
        assertEquals(4, root.getAllElements().size()); // root, p, a, span

        // getElementsByClass
        assertEquals(1, root.getElementsByClass("box").size());
        assertEquals(1, root.getElementsByClass("outer").size());

        // getElementsByAttribute
        assertEquals(1, root.getElementsByAttribute("title").size());
        assertEquals(1, root.getElementsByAttributeStarting("data-").size());
        assertEquals(1, root.getElementsByAttributeValue("title", "para1").size());
        assertEquals(3, root.getElementsByAttributeValueNot("title", "para1").size());
        assertEquals(1, root.getElementsByAttributeValueStarting("href", "http://").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("href", "/page").size());
        assertEquals(1, root.getElementsByAttributeValueContaining("href", "example").size());
        assertEquals(1, root.getElementsByAttributeValueMatching("href", Pattern.compile(".*example.*")).size());
        assertEquals(1, root.getElementsByAttributeValueMatching("href", ".*example.*").size());

        // Sibling index queries
        assertEquals(1, root.getElementsByIndexEquals(0).size()); // matches child1
        assertEquals(2, root.getElementsByIndexLessThan(2).size()); // child1(0), child2(1)
        assertEquals(1, root.getElementsByIndexGreaterThan(1).size()); // child3(2)

        // Text matching queries
        assertEquals(2, root.getElementsContainingText("Sample").size()); // root, child1, and child3
        assertEquals(1, root.getElementsContainingOwnText("Sample text").size()); // child1 only
        assertEquals(2, root.getElementsMatchingText(Pattern.compile("Sample.*")).size());
        assertEquals(2, root.getElementsMatchingText("Sample.*").size());
        assertEquals(1, root.getElementsMatchingOwnText(Pattern.compile(".*Link.*")).size());
        assertEquals(1, root.getElementsMatchingOwnText(".*Link.*").size());

        // select CSS query
        Elements selected = root.select("a[href]");
        assertEquals(1, selected.size());
        assertSame(child2, selected.get(0));
    }

    @Test(timeout = 4000)
    public void testDomMutationMethods() {
        Element container = new Element(Tag.valueOf("div"), "");
        container.html("<p>Existing</p>");
        assertEquals("<p>Existing</p>", container.html());

        container.prependChild(new Element(Tag.valueOf("span"), "").text("PrependedChild"));
        assertEquals("span", container.child(0).tagName());

        container.prependElement("header").text("Header");
        assertEquals("header", container.child(0).tagName());

        container.prependText("Prefix ");
        assertTrue(container.text().startsWith("Prefix"));

        container.appendElement("footer").text("Footer");
        assertEquals("footer", container.child(container.children().size() - 1).tagName());

        container.appendText(" Suffix");
        assertTrue(container.text().endsWith("Suffix"));

        container.prepend("<b>PrependedHtml</b>");
        assertTrue(container.html().contains("<b>PrependedHtml</b>"));

        container.append("<i>AppendedHtml</i>");
        assertTrue(container.html().contains("<i>AppendedHtml</i>"));

        Element elToEmpty = new Element(Tag.valueOf("div"), "");
        elToEmpty.appendElement("span");
        assertEquals(1, elToEmpty.childNodeSize());
        elToEmpty.empty();
        assertEquals(0, elToEmpty.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testSiblingInsertionBeforeAfterAndWrap() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element target = parent.appendElement("span").text("Target");

        target.before("<b>BeforeHtml</b>");
        assertEquals(2, parent.childNodeSize());
        assertEquals("b", parent.child(0).tagName());

        target.before(new Element(Tag.valueOf("i"), "").text("BeforeNode"));
        assertEquals(3, parent.childNodeSize());
        assertEquals("i", parent.child(1).tagName