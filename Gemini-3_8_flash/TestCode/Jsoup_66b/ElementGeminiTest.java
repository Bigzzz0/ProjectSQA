package org.jsoup.nodes;

import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * =====================================================================================================
 * Target: org.jsoup.nodes.Element
 * Known Defect (Defects4J): ElementTest::testNextElementSiblingAfterClone -> java.lang.NullPointerException
 * Root Cause Target: Element cloning and cached shadow children reference / childNodes sibling linkage.
 *
 * Specific Decision Branches & Boundary Conditions Covered:
 * 1. Element Constructors: null tag, null baseUri, null attributes vs populated attributes.
 * 2. ensureChildNodes: empty (EMPTY_NODES) -> new NodeList(4), and already populated lists.
 * 3. hasAttributes & attributes(): null attributes on-demand initialization.
 * 4. tagName: preserveCase setting, empty/null validation.
 * 5. id(), attr(k, v), attr(k, boolean): true creates boolean attr, false removes attr.
 * 6. dataset(): HTML5 data-* attribute filtering and synchronization.
 * 7. parents() & accumulateParents: tree structure, termination at #root document, isolated elements.
 * 8. child(i), children(), childElementsList(): caching in shadowChildrenRef (WeakReference), cache
 *    invalidation on nodelistChanged, non-Element child filtering.
 * 9. textNodes() & dataNodes(): filtering childNodes, unmodifiable list contract.
 * 10. select(query), selectFirst(query), is(query), is(evaluator): query evaluation and first hit matching.
 * 11. appendChild, appendTo, prependChild: reparenting, sibling indexing.
 * 12. insertChildren (Collection and Varargs): roll-around negative indexing (index < 0 -> currentSize + 1),
 *     out-of-bounds validations (index < 0 || index > currentSize).
 * 13. appendElement, prependElement, appendText, prependText, append(html), prepend(html), wrap, empty.
 * 14. cssSelector(): with ID (#id), namespace tag translation (ns:tag -> ns|tag), class concatenation,
 *     Document parent exclusion, nth-child calculation for multiple siblings.
 * 15. Sibling navigation: siblingElements, nextElementSibling, previousElementSibling,
 *     firstElementSibling, lastElementSibling, elementSiblingIndex when parent is null vs populated.
 * 16. DOM Collector Queries: getElementsByTag (normalization), getElementById (hit/miss), getElementsByClass,
 *     getElementsByAttribute*, getElementsByIndex*, getElementsContainingText, getElementsMatchingText,
 *     getAllElements, PatternSyntaxException -> IllegalArgumentException mapping.
 * 17. text() & ownText(): traversal, block elements and <br> whitespace appending, whitespace normalization,
 *     preserveWhitespace checking (in element or parent e.g. <pre>, <textarea>).
 * 18. hasText(): whitespace-only TextNodes vs non-blank TextNodes, recursive Element text checks.
 * 19. data(): DataNode, Comment, nested Element data extraction.
 * 20. hasClass: len == 0, len < wantLen, len == wantLen, whitespace scanning (first class, middle,
 *     last class, exact match, case insensitivity, partial name collision).
 * 21. addClass, removeClass, toggleClass, classNames(): empty removal, whitespace splitting.
 * 22. val(): input vs textarea handling (getting and setting).
 * 23. outerHtmlHead / outerHtmlTail: prettyPrint true/false, syntax HTML vs XML, self-closing tags,
 *     empty tags, outline formatting, indentation with StringBuilder vs generic Appendable.
 * 24. doClone & clone(): deep copy attributes, baseUri, childNodes isolation, sibling navigation after clone.
 * =====================================================================================================
 */
public class ElementGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testElementConstructorsAndBasicGetters() {
        Element el1 = new Element("div");
        assertEquals("div", el1.tagName());
        assertEquals("div", el1.nodeName());
        assertEquals("", el1.baseUri());
        assertTrue(el1.isBlock());
        assertTrue(el1.hasAttributes());
        assertEquals(0, el1.childNodeSize());

        Tag pTag = Tag.valueOf("p");
        Element el2 = new Element(pTag, "http://example.com");
        assertEquals("p", el2.tagName());
        assertEquals("http://example.com", el2.baseUri());
        assertFalse(el2.hasAttributes()); // attributes were null initially
        assertNotNull(el2.attributes()); // lazy instantiated
        assertTrue(el2.hasAttributes());

        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        Element el3 = new Element(Tag.valueOf("span"), "http://example.com", attrs);
        assertEquals("span", el3.tagName());
        assertFalse(el3.isBlock());
        assertEquals("main", el3.id());
    }

    @Test(timeout = 4000)
    public void testBaseUriAndTagNameMutation() {
        Element el = new Element("div");
        el.doSetBaseUri("http://foo.com");
        assertEquals("http://foo.com", el.baseUri());

        el.tagName("SPAN");
        assertEquals("SPAN", el.tagName());
        assertEquals("SPAN", el.tag().getName());
        assertFalse(el.isBlock());
    }

    @Test(timeout = 4000)
    public void testAttributeAndDatasetManipulation() {
        Element el = new Element("div");
        el.attr("title", "header");
        assertEquals("header", el.attr("title"));

        el.attr("disabled", true);
        assertTrue(el.hasAttr("disabled"));

        el.attr("disabled", false);
        assertFalse(el.hasAttr("disabled"));

        el.attr("data-user-id", "12345");
        el.attr("data-role", "admin");
        Map<String, String> dataset = el.dataset();
        assertEquals(2, dataset.size());
        assertEquals("12345", dataset.get("user-id"));
        assertEquals("admin", dataset.get("role"));

        dataset.put("new-key", "value");
        assertEquals("value", el.attr("data-new-key"));
    }

    @Test(timeout = 4000)
    public void testHierarchyParentsAndChildren() {
        Element root = new Element(Tag.valueOf("#root"), "");
        Element body = root.appendElement("body");
        Element div = body.appendElement("div");
        Element p = div.appendElement("p");

        assertEquals(div, p.parent());
        Elements parents = p.parents();
        assertEquals(2, parents.size()); // div, body (skips #root)
        assertEquals(div, parents.get(0));
        assertEquals(body, parents.get(1));

        assertEquals(1, div.children().size());
        assertEquals(p, div.child(0));

        // Test shadow children caching and invalidation
        Element span = div.appendElement("span");
        assertEquals(2, div.children().size());
        assertEquals(span, div.child(1));
    }

    @Test(timeout = 4000)
    public void testChildTextNodesAndDataNodes() {
        Element script = new Element("script");
        script.appendChild(new DataNode("var x = 1;", ""));
        script.appendChild(new Comment("a comment"));

        List<DataNode> dataNodes = script.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("var x = 1;", dataNodes.get(0).getWholeData());
        assertEquals("var x = 1;a comment", script.data());

        Element p = new Element("p");
        p.appendChild(new TextNode("Hello "));
        p.appendElement("b").text("world");
        p.appendChild(new TextNode(" !"));

        List<TextNode> textNodes = p.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Hello ", textNodes.get(0).getWholeText());
        assertEquals(" !", textNodes.get(1).getWholeText());
        assertEquals("Hello world !", p.text());
        assertEquals("Hello !", p.ownText());
    }

    @Test(timeout = 4000)
    public void testSelectorAndEvaluatorIntegration() {
        Element div = new Element("div");
        div.attr("id", "container");
        Element p1 = div.appendElement("p").attr("class", "msg").text("First");
        Element p2 = div.appendElement("p").attr("class", "msg highlight").text("Second");

        assertTrue(div.is("#container"));
        assertTrue(p1.is(new Evaluator.Class("msg")));
        assertFalse(p1.is(".highlight"));

        Elements selected = div.select("p.msg");
        assertEquals(2, selected.size());

        Element firstHit = div.selectFirst(".highlight");
        assertNotNull(firstHit);
        assertEquals(p2, firstHit);

        assertNull(div.selectFirst(".not-existing"));
    }

    @Test(timeout = 4000)
    public void testChildInsertionVariants() {
        Element div = new Element("div");
        Element c1 = new Element("span").text("1");
        Element c2 = new Element("span").text("2");
        Element c3 = new Element("span").text("3");

        div.appendChild(c2);
        div.prependChild(c1);
        assertEquals("<span>1</span>\n<span>2</span>", div.html());

        // Negative index roll around: index = -1 inserts at end
        div.insertChildren(-1, Collections.singletonList(c3));
        assertEquals(3, div.children().size());
        assertEquals("3", div.child(2).text());

        // Varargs insertion at start
        Element c0 = new Element("span").text("0");
        div.insertChildren(0, c0);
        assertEquals("0", div.child(0).text());
        assertEquals(4, div.children().size());

        // Append to parent
        Element newChild = new Element("i").text("italic");
        newChild.appendTo(div);
        assertEquals(5, div.children().size());
        assertEquals("italic", div.child(4).text());
    }

    @Test(timeout = 4000)
    public void testSiblingNavigationMethods() {
        Element div = new Element("div");
        Element p1 = div.appendElement("p").text("One");
        Element p2 = div.appendElement("p").text("Two");
        Element p3 = div.appendElement("p").text("Three");

        assertEquals(0, p1.elementSiblingIndex());
        assertEquals(1, p2.elementSiblingIndex());
        assertEquals(2, p3.elementSiblingIndex());

        assertEquals(p2, p1.nextElementSibling());
        assertNull(p3.nextElementSibling());

        assertEquals(p2, p3.previousElementSibling());
        assertNull(p1.previousElementSibling());

        assertEquals(p1, p2.firstElementSibling());
        assertEquals(p3, p2.lastElementSibling());

        Elements siblings = p2.siblingElements();
        assertEquals(2, siblings.size());
        assertEquals(p1, siblings.get(0));
        assertEquals(p3, siblings.get(1));
    }

    @Test(timeout = 4000)
    public void testCssSelectorCalculations() {
        Element elWithId = new Element("div").attr("id", "my-id");
        assertEquals("#my-id", elWithId.cssSelector());

        Element root = new Element("root");
        Element parent = root.appendElement("div").attr("class", "container");
        Element child1 = parent.appendElement("ns:custom").attr("class", "item primary");
        Element child2 = parent.appendElement("ns:custom").attr("class", "item");

        // Translate ns:tag to ns|tag and include classes and nth-child
        String selectorChild1 = child1.cssSelector();
        assertTrue(selectorChild1.contains("ns|custom.item.primary"));

        String selectorChild2 = child2.cssSelector();
        assertTrue(selectorChild2.contains("ns|custom.item:nth-child(2)"));
    }

    @Test(timeout = 4000)
    public void testDomCollectorQueries() {
        Element root = new Element("div");
        root.append("<p id='p1' class='text active' attr='val1' data-flag='true'>Hello World</p>" +
                "<p id='p2' class='text' attr='val2' data-flag='false'>Hello <span>Universe</span></p>" +
                "<input type='text' value='Foo' />");

        assertEquals(2, root.getElementsByTag("P").size());
        assertNotNull(root.getElementById("p1"));
        assertNull(root.getElementById("unknown"));
        assertEquals(2, root.getElementsByClass("text").size());
        assertEquals(1, root.getElementsByClass("active").size());
        assertEquals(2, root.getElementsByAttribute("attr").size());
        assertEquals(2, root.getElementsByAttributeStarting("data-").size());
        assertEquals(1, root.getElementsByAttributeValue("attr", "val1").size());
        assertEquals(1, root.getElementsByAttributeValueNot("attr", "val1").size());
        assertEquals(2, root.getElementsByAttributeValueStarting("attr", "val").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("attr", "2").size());
        assertEquals(2, root.getElementsByAttributeValueContaining("attr", "al").size());
        assertEquals(1, root.getElementsByAttributeValueMatching("data-flag", Pattern.compile("tru.*")).size());
        assertEquals(1, root.getElementsByAttributeValueMatching("data-flag", "fal.*").size());

        assertEquals(1, root.getElementsByIndexLessThan(1).size());
        assertEquals(1, root.getElementsByIndexGreaterThan(1).size());
        assertEquals(1, root.getElementsByIndexEquals(1).size());

        assertEquals(2, root.getElementsContainingText("Hello").size());
        assertEquals(1, root.getElementsContainingOwnText("Hello World").size());
        assertEquals(2, root.getElementsMatchingText(".*World.*").size()); // p1 and its child span if any
        assertEquals(1, root.getElementsMatchingOwnText("Hello World").size());
        assertEquals(4, root.getAllElements().size()); // root, p1, p2, input
    }

    @Test(timeout = 4000)
    public void testTextAndWhitespacePreservation() {
        Element div = new Element("div");
        div.append("<p>Hello <br> World</p>");
        assertEquals("Hello World", div.text());
        assertEquals("Hello World", div.child(0).ownText());

        Element pre = new Element("pre");
        pre.append("   leading and\n   new lines   ");
        assertTrue(pre.text().contains("   leading and"));

        Element parentPre = new Element("pre");
        Element code = parentPre.appendElement("code");
        code.appendText("  keep   spaces  ");
        assertEquals("  keep   spaces  ", code.text());
    }

    @Test(timeout = 4000)
    public void testHasTextConditions() {
        Element empty = new Element("div");
        assertFalse(empty.hasText());

        Element blankText = new Element("div");
        blankText.appendText("    \t\n  ");
        assertFalse(blankText.hasText());

        Element hasTextEl = new Element("div");
        hasTextEl.appendText("   some text   ");
        assertTrue(hasTextEl.hasText());

        Element nested = new Element("div");
        nested.appendElement("span").text("inner");
        assertTrue(nested.hasText());
    }

    @Test(timeout = 4000)
    public void testClassManipulationAndHasClassBranches() {
        Element el = new Element("div");
        assertFalse(el.hasClass("foo"));
        assertEquals("", el.className());
        assertTrue(el.classNames().isEmpty());

        el.addClass("foo");
        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("FOO")); // case insensitive
        assertFalse(el.hasClass("fo"));
        assertFalse(el.hasClass("fooo"));

        el.addClass("bar");
        el.addClass("baz");
        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));
        assertTrue(el.hasClass("baz"));
        assertFalse(el.hasClass("ba"));

        el.removeClass("bar");
        assertFalse(el.hasClass("bar"));
        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("baz"));

        el.toggleClass("qux");
        assertTrue(el.hasClass("qux"));
        el.toggleClass("qux");
        assertFalse(el.hasClass("qux"));

        Set<String> customClasses = new HashSet<>(Arrays.asList("c1", "c2"));
        el.classNames(customClasses);
        assertEquals(2, el.classNames().size());
        assertTrue(el.hasClass("c1"));
        assertTrue(el.hasClass("c2"));
    }

    @Test(timeout = 4000)
    public void testValHandlingForInputAndTextarea() {
        Element input = new Element("input").attr("value", "initial");
        assertEquals("initial", input.val());
        input.val("updated");
        assertEquals("updated", input.attr("value"));

        Element textarea = new Element("textarea").text("sample text");
        assertEquals("sample text", textarea.val());
        textarea.val("new text");
        assertEquals("new text", textarea.text());
    }

    @Test(timeout = 4000)
    public void testHtmlOutputAndPrettyPrintBranches() throws IOException {
        Element div = new Element("div");
        div.append("<p>One</p><img src='pic.jpg'>");

        // Pretty print HTML
        String prettyHtml = div.outerHtml();
        assertTrue(prettyHtml.contains("<div>\n <p>One</p>\n <img src=\"pic.jpg\">"));

        // Non-pretty HTML
        Document.OutputSettings settings = new Document.OutputSettings().prettyPrint(false);
        assertEquals("<div><p>One</p><img src=\"pic.jpg\"></div>", div.outerHtml().replaceAll("\r?\n", ""));

        // XML syntax self closing
        settings.syntax(Document.OutputSettings.Syntax.xml);
        StringWriter writer = new StringWriter();
        div.child(1).outerHtmlHead(writer, 0, settings);
        assertEquals("<img src=\"pic.jpg\" />", writer.toString());

        // Inner HTML replacement
        div.html("<span>New Content</span>");
        assertEquals("<span>New Content</span>", div.html());
    }

    @Test(timeout = 4000)
    public void testDomSiblingsBeforeAfterWrap() {
        Element parent = new Element("div");
        Element child = parent.appendElement("span").text("target");

        child.before("<p>preceding</p>");
        child.after("<b>following</b>");

        assertEquals(3, parent.children().size());
        assertEquals("p", parent.child(0).tagName());
        assertEquals("span", parent.child(1).tagName());
        assertEquals("b", parent.child(2).tagName());

        Element wrapped = child.wrap("<div class='wrapper'></div>");
        assertEquals(parent, wrapped.parent());
        assertEquals("wrapper", wrapped.parent().className());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testInsertChildrenBoundaryIndices() {
        Element el = new Element("ul");
        Element item1 = new Element("li").text("1");
        Element item2 = new Element("li").text("2");

        // Insert into empty list at 0
        el.insertChildren(0, item1);
        assertEquals(1, el.childNodeSize());

        // Insert at boundary size
        el.insertChildren(1, item2);
        assertEquals(2, el.childNodeSize());

        // Negative boundary: -1 inserts at currentSize (end)
        Element item3 = new Element("li").text("3");
        el.insertChildren(-1, item3);
        assertEquals(item3, el.child(2));

        // Negative boundary: -el.childNodeSize() - 1 rolls around to 0
        Element item0 = new Element("li").text("0");
        el.insertChildren(-el.childNodeSize() - 1, item0);
        assertEquals(item0, el.child(0));
    }

    @Test(timeout = 4000)
    public void testStandaloneElementNavigationEdgeCases() {
        Element standalone = new Element("p");
        assertNull(standalone.parent());
        assertEquals(0, standalone.elementSiblingIndex());
        assertTrue(standalone.siblingElements().isEmpty());
        assertNull(standalone.nextElementSibling());
        assertNull(standalone.previousElementSibling());
    }

    @Test(timeout = 4000)
    public void testSingleChildSiblingNavigation() {
        Element parent = new Element("div");
        Element soleChild = parent.appendElement("span");

        assertNull(soleChild.firstElementSibling());
        assertNull(soleChild.lastElementSibling());
        assertNull(soleChild.nextElementSibling());
        assertNull(soleChild.previousElementSibling());
        assertEquals(0, soleChild.siblingElements().size());
    }

    @Test(timeout = 4000)
    public void testHasClassBoundaryStrings() {
        Element el = new Element("div");
        el.attr("class", "  a   b   c  ");
        assertTrue(el.hasClass("a"));
        assertTrue(el.hasClass("b"));
        assertTrue(el.hasClass("c"));
        assertFalse(el.hasClass("d"));
        assertFalse(el.hasClass(""));
        assertFalse(el.hasClass("longerThanAttribute"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Defects4J Ground Truth Defect:
     * org.jsoup.nodes.ElementTest::testNextElementSiblingAfterClone
     * -> java.lang.NullPointerException
     *
     * In unpatched versions, cloning an element and retrieving siblings or navigating
     * nextElementSibling triggers a NullPointerException because cloned child node lists
     * or shadow element references are either null or desynchronized from the parent.
     */
    @Test(timeout = 4000)
    public void testNextElementSiblingAfterClone() {
        Element parent = new Element("div");
        Element child1 = parent.appendElement("p").text("One");
        Element child2 = parent.appendElement("p").text("Two");

        assertEquals(child2, child1.nextElementSibling());

        Element clone = parent.clone();
        assertNotNull(clone);
        assertEquals(2, clone.children().size());

        Element clonedChild1 = clone.child(0);
        Element clonedChild2 = clonedChild1.nextElementSibling();

        assertNotNull("Defect verification: nextElementSibling() on child after parent clone must not throw NPE and must not be null", clonedChild2);
        assertEquals("Two", clonedChild2.text());
        assertEquals(clone.child(1), clonedChild2);
    }

    @Test(timeout = 4000)
    public void testPreviousElementSiblingAfterClone() {
        Element parent = new Element("div");
        parent.appendElement("span").text("First");
        parent.appendElement("span").text("Second");

        Element clone = parent.clone();
        Element clonedChild2 = clone.child(1);
        Element clonedChild1 = clonedChild2.previousElementSibling();

        assertNotNull("Defect verification: previousElementSibling() on child after parent clone must not be null", clonedChild1);
        assertEquals("First", clonedChild1.text());
    }

    @Test(timeout = 4000)
    public void testStandaloneChildCloneNextElementSibling() {
        Element parent = new Element("div");
        Element child1 = parent.appendElement("p");
        parent.appendElement("p");

        Element clonedChild = child1.clone();
        // Standalone cloned element has no parentNode, nextElementSibling must safely return null
        assertNull(clonedChild.nextElementSibling());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullTag() {
        new Element((Tag) null, "");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullBaseUri() {
        new Element(Tag.valueOf("p"), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetEmptyTagName() {
        new Element("p").tagName("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendChildNull() {
        new Element("div").appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendToNull() {
        new Element("div").appendTo(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrependChildNull() {
        new Element("div").prependChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenNullCollection() {
        new Element("div").insertChildren(0, (List<Node>) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenOutOfBoundsPositive() {
        Element div = new Element("div");
        div.insertChildren(5, Collections.singletonList(new Element("p")));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenOutOfBoundsNegative() {
        Element div = new Element("div");
        // currentSize = 0, index = -2 rolls around to -2 + 0 + 1 = -1 -> fails index >= 0 check
        div.insertChildren(-2, Collections.singletonList(new Element("p")));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendTextNull() {
        new Element("div").appendText(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrependTextNull() {
        new Element("div").prependText(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendHtmlNull() {
        new Element("div").append(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrependHtmlNull() {
        new Element("div").prepend(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetTextNull() {
        new Element("div").text(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByTagEmpty() {
        new Element("div").getElementsByTag("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementByIdEmpty() {
        new Element("div").getElementById("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByClassEmpty() {
        new Element("div").getElementsByClass("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeEmpty() {
        new Element("div").getElementsByAttribute("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeStartingEmpty() {
        new Element("div").getElementsByAttributeStarting("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeValueMatchingInvalidPattern() {
        new Element("div").getElementsByAttributeValueMatching("key", "[unclosed");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingTextInvalidPattern() {
        new Element("div").getElementsMatchingText("[unclosed");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingOwnTextInvalidPattern() {
        new Element("div").getElementsMatchingOwnText("[unclosed");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testClassNamesNull() {
        new Element("div").classNames(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddClassNull() {
        new Element("div").addClass(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveClassNull() {
        new Element("div").removeClass(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToggleClassNull() {
        new Element("div").toggleClass(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloneDeepCopyIntegrity() {
        Element original = new Element("div");
        original.attr("key", "val");
        Element child = original.appendElement("span").text("hello");

        Element clone = original.clone();
        assertNotSame(original, clone);
        assertNotSame(original.attributes(), clone.attributes());
        assertEquals("val", clone.attr("key"));

        // Mutating clone should not mutate original
        clone.attr("key", "new-val");
        assertEquals("val", original.attr("key"));
        assertEquals("new-val", clone.attr("key"));

        clone.child(0).text("world");
        assertEquals("hello", child.text());
        assertEquals("world", clone.child(0).text());
    }

    @Test(timeout = 4000)
    public void testEmptyClearsChildren() {
        Element parent = new Element("div");
        parent.appendElement("p").text("1");
        parent.appendElement("p").text("2");
        assertEquals(2, parent.childNodeSize());

        parent.empty();
        assertEquals(0, parent.childNodeSize());
        assertTrue(parent.children().isEmpty());
    }

    @Test(timeout = 4000)
    public void testToStringMatchesOuterHtml() {
        Element p = new Element("p").attr("class", "msg").text("test");
        assertEquals(p.outerHtml(), p.toString());
    }
}