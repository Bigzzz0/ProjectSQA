/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.nodes.Element
 * Defects4J Defect Focus:
 *   - hasClassCaseInsensitive / testByClassCaseInsensitive
 *   - Defect Root: Element#hasClass(String) performs attributes.get("class") which is case-sensitive
 *     with respect to the attribute key. If an element's class attribute key is "Class" or "CLASS",
 *     attributes.get("class") returns an empty string or misses it, causing hasClass() to return false.
 *
 * Test Partitions & Branch Coverage:
 * Partition A: Core Functional Logic & State Transitions
 *   - Tag names, preservation of tag casing, block status.
 *   - Attribute manipulation (string values, boolean attributes, dataset).
 *   - Hierarchy navigation: parent, parents, children, textNodes, dataNodes.
 *   - Tree mutations: appendChild, prependChild, insertChildren (index 0, -1, mid), wrap, empty.
 *   - Sibling queries: siblingElements, nextElementSibling, previousElementSibling,
 *     firstElementSibling, lastElementSibling, elementSiblingIndex.
 *   - Value queries (val, val(String)) on standard elements and textarea.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - insertChildren with boundary indices (0, size(), -1, negative wrapping).
 *   - Empty and single-child elements, root without parent, elements with no siblings.
 *   - Text extraction with nested blocks, <br> tags, and <pre> whitespace preservation.
 *   - Class attribute boundary: multiple classes, leading/trailing whitespace, exact length matches.
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 *   - hasClass() with mixed-case attribute names ("Class", "CLASS") and mixed-case class values.
 *   - getElementsByClass() case insensitivity.
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - Null tags, null children, invalid regex strings, out-of-bounds insert positions.
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Deep clone validation (isolation of attributes and children).
 *   - Serialization/HTML rendering in HTML vs XML syntax, pretty-print on/off, self-closing tags.
 * ---------------------------------------------------------------------------------------------------
 */

package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: Element.hasClass() must be case-insensitive both in class name matching
     * AND when the attribute key itself is mixed-case (e.g. "Class" or "CLASS").
     */
    @Test(timeout = 4000)
    public void testHasClassCaseInsensitiveAttributeKeyAndValue() {
        Element el1 = new Element(Tag.valueOf("div"), "");
        el1.attr("class", "foo BAR Baz");
        assertTrue("Matches exact case", el1.hasClass("foo"));
        assertTrue("Matches uppercase target", el1.hasClass("FOO"));
        assertTrue("Matches lowercase target for uppercase class", el1.hasClass("bar"));
        assertTrue("Matches mixed-case middle class", el1.hasClass("BAR"));
        assertTrue("Matches mixed-case tail class", el1.hasClass("baz"));
        assertTrue("Matches uppercase tail class", el1.hasClass("BAZ"));
        assertFalse("Should not match missing class", el1.hasClass("qux"));

        // Case-insensitive attribute key ("Class" instead of "class")
        Element el2 = new Element(Tag.valueOf("span"), "");
        el2.attr("Class", "ActiveItem");
        assertTrue("Must detect class even if attribute key is 'Class'", el2.hasClass("ActiveItem"));
        assertTrue("Must detect class case-insensitively if key is 'Class'", el2.hasClass("activeitem"));

        Element el3 = new Element(Tag.valueOf("p"), "");
        el3.attr("CLASS", "Selected");
        assertTrue("Must detect class even if attribute key is 'CLASS'", el3.hasClass("selected"));
    }

    /**
     * Direct test for getElementsByClass with mixed-case attributes.
     */
    @Test(timeout = 4000)
    public void testGetElementsByClassCaseInsensitivity() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = root.appendElement("p").attr("Class", "Foo");
        Element child2 = root.appendElement("p").attr("class", "FOO");
        Element child3 = root.appendElement("p").attr("CLASS", "foo");

        Elements result = root.getElementsByClass("foo");
        assertEquals("Should find all 3 elements regardless of attribute key/value casing", 3, result.size());
        assertTrue(result.contains(child1));
        assertTrue(result.contains(child2));
        assertTrue(result.contains(child3));
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndTagManagement() {
        Element el1 = new Element("div");
        assertEquals("div", el1.tagName());
        assertEquals("div", el1.nodeName());
        assertTrue(el1.isBlock());
        assertEquals("", el1.baseUri());

        Element el2 = new Element(Tag.valueOf("SPAN"), "http://example.com");
        assertEquals("span", el2.tagName()); // Tag normalizes to lowercase by default
        assertFalse(el2.isBlock());
        assertEquals("http://example.com", el2.baseUri());

        el2.tagName("p");
        assertEquals("p", el2.tagName());
        assertTrue(el2.isBlock());
    }

    @Test(timeout = 4000)
    public void testAttributesAndDataset() {
        Element el = new Element("div");
        el.attr("id", "mainId");
        assertEquals("mainId", el.id());

        el.attr("title", "heading");
        assertEquals("heading", el.attr("title"));

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
    }

    @Test(timeout = 4000)
    public void testHierarchyAndParents() {
        Element doc = new Element("#root");
        Element body = doc.appendElement("body");
        Element div = body.appendElement("div");
        Element span = div.appendElement("span");

        assertEquals(div, span.parent());
        Elements parents = span.parents();
        assertEquals(2, parents.size());
        assertEquals(div, parents.get(0));
        assertEquals(body, parents.get(1));
        // #root should be excluded from parents()
        assertFalse(parents.contains(doc));

        Element standalone = new Element("p");
        assertNull(standalone.parent());
        assertEquals(0, standalone.parents().size());
    }

    @Test(timeout = 4000)
    public void testChildrenAndFilters() {
        Element el = new Element("div");
        el.appendText("Text 1 ");
        Element child1 = el.appendElement("p");
        el.append("<!-- Comment -->");
        el.appendText("Text 2");
        Element child2 = el.appendElement("span");

        assertEquals(2, el.children().size());
        assertEquals(child1, el.child(0));
        assertEquals(child2, el.child(1));

        List<TextNode> textNodes = el.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Text 1 ", textNodes.get(0).getWholeText());
        assertEquals("Text 2", textNodes.get(1).getWholeText());

        Element script = new Element("script");
        script.appendChild(new DataNode("var x = 1;", ""));
        List<DataNode> dataNodes = script.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("var x = 1;", dataNodes.get(0).getWholeData());
    }

    @Test(timeout = 4000)
    public void testSelectAndIs() {
        Element div = new Element("div");
        div.attr("id", "container");
        Element p = div.appendElement("p").addClass("lead");
        p.appendText("Content");

        Elements selected = div.select("p.lead");
        assertEquals(1, selected.size());
        assertEquals(p, selected.first());

        assertTrue(p.is("p"));
        assertTrue(p.is(".lead"));
        assertTrue(p.is(new Evaluator.Tag("p")));
        assertFalse(p.is("span"));
    }

    @Test(timeout = 4000)
    public void testTreeMutationAppendPrependInsert() {
        Element div = new Element("div");
        Element span = new Element("span");
        div.appendChild(span);
        assertEquals(1, div.childNodeSize());
        assertEquals(span, div.child(0));

        Element b = new Element("b");
        div.prependChild(b);
        assertEquals(2, div.childNodeSize());
        assertEquals(b, div.child(0));
        assertEquals(span, div.child(1));

        Element i = new Element("i");
        Element u = new Element("u");
        div.insertChildren(1, Arrays.asList(i, u));
        assertEquals(4, div.children().size());
        assertEquals(b, div.child(0));
        assertEquals(i, div.child(1));
        assertEquals(u, div.child(2));
        assertEquals(span, div.child(3));

        // Insert at negative index (-1 wraps to end)
        Element em = new Element("em");
        div.insertChildren(-1, Collections.singletonList(em));
        assertEquals(em, div.childNodes.get(div.childNodes.size() - 1));
    }

    @Test(timeout = 4000)
    public void testSiblingNavigation() {
        Element div = new Element("div");
        Element c1 = div.appendElement("p").attr("id", "c1");
        Element c2 = div.appendElement("span").attr("id", "c2");
        Element c3 = div.appendElement("b").attr("id", "c3");

        assertEquals(2, c1.siblingElements().size());
        assertEquals(c2, c1.nextElementSibling());
        assertNull(c1.previousElementSibling());
        assertEquals(c1, c2.previousElementSibling());
        assertEquals(c3, c2.nextElementSibling());
        assertNull(c3.nextElementSibling());

        assertEquals(c1, c2.firstElementSibling());
        assertEquals(c3, c2.lastElementSibling());

        assertEquals(Integer.valueOf(0), c1.elementSiblingIndex());
        assertEquals(Integer.valueOf(1), c2.elementSiblingIndex());
        assertEquals(Integer.valueOf(2), c3.elementSiblingIndex());
    }

    @Test(timeout = 4000)
    public void testFormValMethod() {
        Element input = new Element("input");
        input.val("test-value");
        assertEquals("test-value", input.val());
        assertEquals("test-value", input.attr("value"));

        Element textarea = new Element("textarea");
        textarea.val("multiline\ntext");
        assertEquals("multiline text", textarea.val()); // textarea.text() normalizes whitespace
        assertEquals("multiline text", textarea.text());
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testCssSelectorGenerators() {
        // Element with ID
        Element div = new Element("div").attr("id", "rootDiv");
        assertEquals("#rootDiv", div.cssSelector());

        // Element without ID in hierarchy
        Document doc = new Document("http://example.com");
        Element body = doc.appendElement("body");
        Element p1 = body.appendElement("p");
        Element p2 = body.appendElement("p");

        assertEquals("body > p:nth-child(1)", p1.cssSelector());
        assertEquals("body > p:nth-child(2)", p2.cssSelector());

        // Namespace replacement ':' to '|'
        Element xmlTag = new Element("fb:like");
        assertEquals("fb|like", xmlTag.cssSelector());

        // Classes formatting in cssSelector
        Element classed = new Element("div").addClass("foo").addClass("bar");
        assertEquals("div.foo.bar", classed.cssSelector());
    }

    @Test(timeout = 4000)
    public void testSiblingBoundaryCases() {
        Element standalone = new Element("div");
        assertEquals(0, standalone.siblingElements().size());
        assertNull(standalone.nextElementSibling());
        assertNull(standalone.previousElementSibling());
        assertEquals(Integer.valueOf(0), standalone.elementSiblingIndex());

        Element parent = new Element("div");
        Element single = parent.appendElement("p");
        assertEquals(0, single.siblingElements().size());
        assertNull(single.firstElementSibling());
        assertNull(single.lastElementSibling());
    }

    @Test(timeout = 4000)
    public void testTextExtractionAndWhitespaceHandling() {
        Element p = new Element("p");
        p.appendText("  Hello  ");
        Element span = p.appendElement("span");
        span.appendText(" World ");
        assertEquals("Hello World", p.text());
        assertEquals("Hello", p.ownText());
        assertTrue(p.hasText());

        // Test <br> inserts whitespace
        Element brDiv = new Element("div");
        brDiv.appendText("First");
        brDiv.appendElement("br");
        brDiv.appendText("Second");
        assertEquals("First Second", brDiv.text());
        assertEquals("First Second", brDiv.ownText());

        // Test block elements insert whitespace
        Element blockDiv = new Element("div");
        blockDiv.appendText("First");
        blockDiv.appendElement("p").appendText("Inside Block");
        assertEquals("First Inside Block", blockDiv.text());

        // Whitespace preservation in <pre>
        Element pre = new Element("pre");
        pre.appendText("  line1\n  line2  ");
        assertEquals("  line1\n  line2  ", pre.text());

        Element emptyEl = new Element("div");
        assertFalse(emptyEl.hasText());
        assertEquals("", emptyEl.text());
        assertEquals("", emptyEl.ownText());
    }

    @Test(timeout = 4000)
    public void testDataExtraction() {
        Element script = new Element("script");
        script.appendChild(new DataNode("/* data1 */", ""));
        script.appendChild(new Comment(" comment "));
        Element child = script.appendElement("nested");
        child.appendChild(new DataNode("/* data2 */", ""));

        assertEquals("/* data1 */ comment /* data2 */", script.data());
    }

    @Test(timeout = 4000)
    public void testClassNamesManipulations() {
        Element el = new Element("div");
        assertEquals("", el.className());
        assertTrue(el.classNames().isEmpty());

        el.addClass("c1");
        assertTrue(el.hasClass("c1"));
        assertEquals("c1", el.className());

        el.addClass("c2").addClass("c3");
        assertEquals(3, el.classNames().size());

        el.removeClass("c2");
        assertFalse(el.hasClass("c2"));
        assertTrue(el.hasClass("c1"));
        assertTrue(el.hasClass("c3"));

        el.toggleClass("c3"); // removes
        assertFalse(el.hasClass("c3"));
        el.toggleClass("c3"); // adds
        assertTrue(el.hasClass("c3"));

        Set<String> custom = new HashSet<String>(Arrays.asList("alpha", "beta"));
        el.classNames(custom);
        assertEquals(2, el.classNames().size());
        assertTrue(el.hasClass("alpha"));
        assertTrue(el.hasClass("beta"));
        assertFalse(el.hasClass("c1"));
    }

    @Test(timeout = 4000)
    public void testClassMatchingBoundaryConditions() {
        Element el = new Element("div");
        el.attr("class", "one two three");

        assertFalse(el.hasClass(""));
        assertFalse(el.hasClass("on"));
        assertFalse(el.hasClass("twoo"));
        assertFalse(el.hasClass("longerthanallclassescombined"));

        assertTrue(el.hasClass("one"));
        assertTrue(el.hasClass("two"));
        assertTrue(el.hasClass("three"));

        // Single class exact length
        Element elSingle = new Element("div").attr("class", "exact");
        assertTrue(elSingle.hasClass("exact"));
        assertTrue(elSingle.hasClass("EXACT"));
        assertFalse(elSingle.hasClass("other"));
    }

    @Test(timeout = 4000)
    public void testDomQueryMethods() {
        Element root = new Element("div");
        Element p1 = root.appendElement("p").attr("id", "p1").attr("title", "start-val");
        Element p2 = root.appendElement("p").attr("id", "p2").attr("title", "mid-val-middle");
        Element p3 = root.appendElement("p").attr("id", "p3").attr("title", "end-val");
        p1.text("Alpha target");
        p2.text("Beta");
        p3.text("Gamma target");

        assertEquals(p1, root.getElementById("p1"));
        assertNull(root.getElementById("non-existent"));

        assertEquals(3, root.getElementsByTag("P").size());
        assertEquals(3, root.getElementsByAttribute("title").size());
        assertEquals(3, root.getElementsByAttributeStarting("ti").size());

        assertEquals(1, root.getElementsByAttributeValue("title", "start-val").size());
        assertEquals(2, root.getElementsByAttributeValueNot("title", "start-val").size());
        assertEquals(1, root.getElementsByAttributeValueStarting("title", "start").size());
        assertEquals(1, root.getElementsByAttributeValueEnding("title", "-val").size());
        assertEquals(1, root.getElementsByAttributeValueContaining("title", "middle").size());

        assertEquals(3, root.getElementsByAttributeValueMatching("title", Pattern.compile(".*val.*")).size());
        assertEquals(3, root.getElementsByAttributeValueMatching("title", ".*val.*").size());

        assertEquals(1, root.getElementsByIndexLessThan(1).size());
        assertEquals(1, root.getElementsByIndexGreaterThan(1).size());
        assertEquals(1, root.getElementsByIndexEquals(1).size());

        assertEquals(2, root.getElementsContainingText("target").size());
        assertEquals(2, root.getElementsContainingOwnText("target").size());
        assertEquals(2, root.getElementsMatchingText(Pattern.compile("(?i)TARGET")).size());
        assertEquals(2, root.getElementsMatchingText("(?i)TARGET").size());
        assertEquals(2, root.getElementsMatchingOwnText(Pattern.compile("(?i)TARGET")).size());
        assertEquals(2, root.getElementsMatchingOwnText("(?i)TARGET").size());

        assertEquals(4, root.getAllElements().size()); // root + 3 children
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorWithNullTagThrowsException() {
        new Element((Tag) null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetEmptyTagNameThrowsException() {
        Element el = new Element("div");
        el.tagName("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullChildThrowsException() {
        Element el = new Element("div");
        el.appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrependNullChildThrowsException() {
        Element el = new Element("div");
        el.prependChild(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenOutOfBoundsNegativeThrowsException() {
        Element el = new Element("div");
        // size = 0; index -2 -> 0 + 1 - 2 = -1 (invalid)
        el.insertChildren(-2, Collections.singletonList(new Element("p")));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsertChildrenOutOfBoundsPositiveThrowsException() {
        Element el = new Element("div");
        el.insertChildren(2, Collections.singletonList(new Element("p")));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementByEmptyIdThrowsException() {
        Element el = new Element("div");
        el.getElementById("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsByAttributeValueMatchingInvalidRegex() {
        Element el = new Element("div");
        el.getElementsByAttributeValueMatching("title", "[unclosed-bracket");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingTextInvalidRegex() {
        Element el = new Element("div");
        el.getElementsMatchingText("[unclosed-bracket");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetElementsMatchingOwnTextInvalidRegex() {
        Element el = new Element("div");
        el.getElementsMatchingOwnText("[unclosed-bracket");
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testOuterHtmlAndFormatting() {
        Element div = new Element("div");
        Element p = div.appendElement("p").text("Hello");
        assertEquals("<div>\n <p>Hello</p>\n</div>", div.outerHtml());

        // Self-closing void tags in HTML
        Element img = new Element("img").attr("src", "foo.jpg");
        assertEquals("<img src=\"foo.jpg\">", img.outerHtml());

        // Self-closing void tags in XML syntax
        Document xmlDoc = new Document("");
        xmlDoc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        Element xmlImg = xmlDoc.appendElement("img").attr("src", "foo.jpg");
        assertEquals("<img src=\"foo.jpg\" />", xmlImg.outerHtml());

        // Empty non-void element
        Element emptySpan = new Element("span");
        assertEquals("<span></span>", emptySpan.outerHtml());

        // Pretty print turned off
        div.getOutputSettings().prettyPrint(false);
        assertEquals("<div><p>Hello</p></div>", div.outerHtml());
    }

    @Test(timeout = 4000)
    public void testInnerHtmlAndAppendPrependHtml() {
        Element div = new Element("div");
        div.html("<span>Text</span>");
        assertEquals("<span>Text</span>", div.html());
        assertEquals("span", div.child(0).tagName());

        div.prepend("<p>Before</p>");
        assertEquals(2, div.children().size());
        assertEquals("p", div.child(0).tagName());

        div.append("<b>After</b>");
        assertEquals(3, div.children().size());
        assertEquals("b", div.child(2).tagName());
    }

    @Test(timeout = 4000)
    public void testWrapAndEmpty() {
        Element parent = new Element("div");
        Element child = parent.appendElement("p").text("Inside");

        child.wrap("<div class='wrapper'></div>");
        assertEquals("wrapper", parent.child(0).className());
        assertEquals(child, parent.child(0).child(0));

        parent.empty();
        assertEquals(0, parent.childNodeSize());
        assertEquals("", parent.html());
    }

    @Test(timeout = 4000)
    public void testSiblingMutationsBeforeAfter() {
        Element parent = new Element("div");
        Element mid = parent.appendElement("span").text("Mid");

        mid.before("<p>First</p>");
        mid.after("<b>Last</b>");

        assertEquals(3, parent.children().size());
        assertEquals("p", parent.child(0).tagName());
        assertEquals("span", parent.child(1).tagName());
        assertEquals("b", parent.child(2).tagName());

        Element newBeforeNode = new Element("i");
        mid.before(newBeforeNode);
        assertEquals(newBeforeNode, parent.child(1));

        Element newAfterNode = new Element("u");
        mid.after(newAfterNode);
        assertEquals(newAfterNode, parent.child(3));
    }

    @Test(timeout = 4000)
    public void testDeepCloneIntegrity() {
        Element parent = new Element("div").attr("id", "root").addClass("container");
        Element child = parent.appendElement("p").text("Original");

        Element clone = parent.clone();

        assertNotSame("Cloned object should be distinct", parent, clone);
        assertEquals("Clone should mirror outerHtml", parent.outerHtml(), clone.outerHtml());

        // Ensure deep clone modifies independently
        clone.attr("id", "clonedRoot");
        clone.removeClass("container");
        clone.child(0).text("Modified");

        assertEquals("root", parent.id());
        assertTrue(parent.hasClass("container"));
        assertEquals("Original", child.text());

        assertEquals("clonedRoot", clone.id());
        assertFalse(clone.hasClass("container"));
        assertEquals("Modified", clone.child(0).text());
    }
}