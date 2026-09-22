/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.select.Selector
 *
 * 1. Combinators Decision Branches:
 *    - Starting combinators: ">", " ", "+", "~", "," (IllegalStateException trigger)
 *    - In-query combinators: direct child ">", descendant " ", adjacent sibling "+", general sibling "~"
 *    - Whitespace handling: seenWhite implicitly translating to descendant " "
 *    - Multi-combinators & chaining (e.g., "div > p > span")
 *    - Sibling matching: previousElementSibling matching vs non-matching, elementSiblingIndex comparison
 *
 * 2. Element Matchers & Selectors Branches:
 *    - Tag selectors: simple "tag", namespaced "ns|tag" -> "ns:tag"
 *    - ID selectors: "#id" (found vs not found)
 *    - Class selectors: ".class"
 *    - Universal selector: "*"
 *    - Attribute selectors:
 *      * [attr] exists, [^attrPrefix] prefix
 *      * [attr=val] equality
 *      * [attr!=val] inequality
 *      * [attr^=valPrefix] prefix match
 *      * [attr$=valSuffix] suffix match
 *      * [attr*=valContaining] substring match
 *      * [attr~=regex] pattern match
 *      * Invalid attribute syntax -> SelectorParseException
 *    - Index pseudo-selectors:
 *      * :lt(n), :gt(n), :eq(n)
 *      * Non-numeric index validation failure -> IllegalArgumentException
 *    - Text pseudo-selectors:
 *      * :contains(text), :containsOwn(text)
 *      * :matches(regex), :matchesOwn(regex)
 *    - Structural pseudo-selectors:
 *      * :has(selector) with descendants and hierarchical queries
 *    - Composite / Intersecting selectors:
 *      * Tag + Class + Attribute ("div.class[attr]") -> filterForSelf & intersectElements
 *      * Grouping / Or query ("div, p, span")
 *
 * 3. Defects4J Ground Truth Targets:
 *    - Defect 1: :not(selector) pseudo-selector missing / unhandled in findElements() -> throws SelectorParseException
 *      Targets: div:not(.left), p:not([id=1]), :not(p)
 *    - Defect 2: :has(...) standalone or root descendant resolution failing to match parent elements (expected 3, got 0)
 *
 * 4. Boundaries & Exceptions:
 *    - Null query, empty query, whitespace query
 *    - Null root element, null roots collection
 *    - Unrecognized token parse failure -> SelectorParseException
 */

package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SelectorGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSelectByTagAndNamespace() {
        String html = "<div><p>Para 1</p><p>Para 2</p><fb:name>Facebook</fb:name></div>";
        Document doc = Jsoup.parse(html);

        Elements pElements = Selector.select("p", doc);
        assertEquals(2, pElements.size());
        assertEquals("Para 1", pElements.get(0).text());
        assertEquals("Para 2", pElements.get(1).text());

        Elements nsElements = Selector.select("fb|name", doc);
        assertEquals(1, nsElements.size());
        assertEquals("Facebook", nsElements.first().text());
    }

    @Test(timeout = 4000)
    public void testSelectByIdAndClass() {
        String html = "<div id='main' class='content active'><span id='s1' class='active'>Span</span></div>";
        Document doc = Jsoup.parse(html);

        Elements byId = Selector.select("#main", doc);
        assertEquals(1, byId.size());
        assertEquals("div", byId.first().tagName());

        Elements nonExistentId = Selector.select("#absent", doc);
        assertEquals(0, nonExistentId.size());

        Elements byClass = Selector.select(".active", doc);
        assertEquals(2, byClass.size());
        assertEquals("div", byClass.get(0).tagName());
        assertEquals("span", byClass.get(1).tagName());
    }

    @Test(timeout = 4000)
    public void testSelectAllAndChainedAndGrouped() {
        String html = "<div class='container'><p class='highlight' id='p1'>One</p><p class='highlight' id='p2'>Two</p><span>Three</span></div>";
        Document doc = Jsoup.parse(html);

        Elements all = Selector.select("*", doc);
        assertTrue(all.size() >= 5); // html, head, body, div, p, p, span

        // Intersecting AND logic: p.highlight#p1
        Elements chained = Selector.select("p.highlight#p1", doc);
        assertEquals(1, chained.size());
        assertEquals("p1", chained.first().id());

        // Grouping OR logic: #p1, span
        Elements grouped = Selector.select("#p1, span", doc);
        assertEquals(2, grouped.size());
        assertEquals("p1", grouped.get(0).id());
        assertEquals("span", grouped.get(1).tagName());
    }

    @Test(timeout = 4000)
    public void testCombinatorDescendantAndChild() {
        String html = "<div id='outer'><div id='inner'><p>Direct Child</p></div><p>Descendant</p></div>";
        Document doc = Jsoup.parse(html);

        // Child combinator '>'
        Elements children = Selector.select("div#inner > p", doc);
        assertEquals(1, children.size());
        assertEquals("Direct Child", children.first().text());

        // Descendant combinator (whitespace)
        Elements descendants = Selector.select("#outer p", doc);
        assertEquals(2, descendants.size());
    }

    @Test(timeout = 4000)
    public void testCombinatorAdjacentAndGeneralSiblings() {
        String html = "<div><h1>Title</h1><p id='p1'>First</p><p id='p2'>Second</p><span>Other</span><p id='p3'>Third</p></div>";
        Document doc = Jsoup.parse(html);

        // Adjacent sibling '+'
        Elements adjacent = Selector.select("h1 + p", doc);
        assertEquals(1, adjacent.size());
        assertEquals("p1", adjacent.first().id());

        Elements noAdjacent = Selector.select("h1 + span", doc);
        assertEquals(0, noAdjacent.size());

        // General sibling '~'
        Elements general = Selector.select("h1 ~ p", doc);
        assertEquals(3, general.size());
        assertEquals("p1", general.get(0).id());
        assertEquals("p2", general.get(1).id());
        assertEquals("p3", general.get(2).id());
    }

    @Test(timeout = 4000)
    public void testAttributeOperators() {
        String html = "<div id='root'>"
                + "<a href='http://example.com/test' title='sample title' data-src='foo' data-cat='bar' rel='nofollow'>Link 1</a>"
                + "<a href='https://example.org' title='sample' data-src='test-foo' rel='dofollow'>Link 2</a>"
                + "<a href='ftp://example.com' title='other sample' class='external'>Link 3</a>"
                + "</div>";
        Document doc = Jsoup.parse(html);

        // Has attribute [attr]
        assertEquals(3, Selector.select("a[href]", doc).size());

        // Attribute starting with prefix [^attrPrefix]
        assertEquals(2, Selector.select("a[^data-]", doc).size());

        // Attribute value equals [attr=val]
        assertEquals(1, Selector.select("a[rel=nofollow]", doc).size());

        // Attribute value not equals [attr!=val]
        Elements notEquals = Selector.select("a[rel!=nofollow]", doc);
        assertEquals(2, notEquals.size());

        // Attribute value starts with [attr^=valPrefix]
        assertEquals(1, Selector.select("a[href^=http:]", doc).size());

        // Attribute value ends with [attr$=valSuffix]
        assertEquals(1, Selector.select("a[href$=org]", doc).size());

        // Attribute value contains [attr*=valContaining]
        assertEquals(2, Selector.select("a[href*=/example.]", doc).size());

        // Attribute value matches regex [attr~=regex]
        assertEquals(2, Selector.select("a[href~=https?://.*]", doc).size());
    }

    @Test(timeout = 4000)
    public void testIndexPseudoSelectors() {
        String html = "<ol><li>0</li><li>1</li><li>2</li><li>3</li><li>4</li></ol>";
        Document doc = Jsoup.parse(html);

        Elements lt = Selector.select("li:lt(2)", doc);
        assertEquals(2, lt.size());
        assertEquals("0", lt.get(0).text());
        assertEquals("1", lt.get(1).text());

        Elements gt = Selector.select("li:gt(2)", doc);
        assertEquals(2, gt.size());
        assertEquals("3", gt.get(0).text());
        assertEquals("4", gt.get(1).text());

        Elements eq = Selector.select("li:eq(2)", doc);
        assertEquals(1, eq.size());
        assertEquals("2", eq.first().text());
    }

    @Test(timeout = 4000)
    public void testTextAndRegexPseudoSelectors() {
        String html = "<div id='p'>Parent <span>Child with (parentheses)</span></div>"
                + "<div id='alone'>Standalone 123</div>";
        Document doc = Jsoup.parse(html);

        // :contains(text)
        assertEquals(2, Selector.select(":contains(Child)", doc).size()); // div#p and span
        assertEquals(1, Selector.select(":contains(Standalone)", doc).size());

        // :containsOwn(text)
        assertEquals(0, Selector.select("div:containsOwn(Child)", doc).size());
        assertEquals(1, Selector.select("span:containsOwn(Child)", doc).size());
        assertEquals(1, Selector.select("div:containsOwn(Parent)", doc).size());

        // :matches(regex)
        assertEquals(2, Selector.select(":matches(\\d+)", doc).size()); // body/root and div#alone
        assertEquals(1, Selector.select("div:matches(\\d+)", doc).size());

        // :matchesOwn(regex)
        assertEquals(1, Selector.select(":matchesOwn(\\d+)", doc).size());
        assertEquals("alone", Selector.select(":matchesOwn(\\d+)", doc).first().id());
    }

    @Test(timeout = 4000)
    public void testSelectWithRootsCollection() {
        Document doc = Jsoup.parse("<div id='d1'><p>P1</p></div><div id='d2'><p>P2</p></div>");
        List<Element> roots = new ArrayList<Element>();
        roots.add(doc.getElementById("d1"));
        roots.add(doc.getElementById("d2"));

        Elements selected = Selector.select("p", roots);
        assertEquals(2, selected.size());
        assertEquals("P1", selected.get(0).text());
        assertEquals("P2", selected.get(1).text());
    }

    @Test(timeout = 4000)
    public void testQueryStartingWithCombinator() {
        Document doc = Jsoup.parse("<div id='root'><p>First</p><p>Second</p></div>");
        Element root = doc.getElementById("root");

        // Starting with ">" executes elements.add(root); combinator(">")
        Elements children = Selector.select("> p", root);
        assertEquals(2, children.size());
        assertEquals("First", children.get(0).text());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullQueryThrowsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(null, doc);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyQueryThrowsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("", doc);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWhitespaceOnlyQueryThrowsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("   ", doc);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullRootThrowsException() {
        Selector.select("div", (Element) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullRootsCollectionThrowsException() {
        Selector.select("div", (Iterable<Element>) null);
    }

    @Test(timeout = 4000)
    public void testEmptyRootsCollectionReturnsEmpty() {
        Elements result = Selector.select("div", Collections.<Element>emptyList());
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: :not(selector) pseudo-selector.
     * The defective parser throws SelectorParseException: Could not parse query ':not(...)'.
     */
    @Test(timeout = 4000)
    public void testDefectNotAllSelector() {
        Document doc = Jsoup.parse("<p>One</p><span>Two</span><p>Three</p>");
        // Expected behavior: select all non-p elements
        Elements result = Selector.select(":not(p)", doc.body());
        assertEquals(1, result.size());
        assertEquals("span", result.first().tagName());
    }

    /**
     * Target Defect: :not(.class) pseudo-selector.
     * The defective parser fails to parse 'div:not(.left)'.
     */
    @Test(timeout = 4000)
    public void testDefectNotClassSelector() {
        Document doc = Jsoup.parse("<div class='left'>1</div><div class='right'>2</div>");
        Elements result = Selector.select("div:not(.left)", doc);
        assertEquals(1, result.size());
        assertEquals("right", result.first().className());
    }

    /**
     * Target Defect: :not([attr=val]) pseudo-selector.
     * The defective parser fails to parse 'p:not([id=1])'.
     */
    @Test(timeout = 4000)
    public void testDefectNotAttributeSelector() {
        Document doc = Jsoup.parse("<p id='1'>1</p><p id='2'>2</p>");
        Elements result = Selector.select("p:not([id=1])", doc);
        assertEquals(1, result.size());
        assertEquals("2", result.first().id());
    }

    /**
     * Target Defect: :has(selector) element matching.
     * In Defects4J, SelectorTest::testPseudoHas failed with expected:<3> but was:<0>.
     */
    @Test(timeout = 4000)
    public void testDefectPseudoHasDescendants() {
        Document doc = Jsoup.parse("<div><p><span>Foo</span></p></div><div><p>Bar</p></div><div><a>Baz</a></div>");
        // Target divs that contain a p element: expected 2 divs
        Elements divsWithP = Selector.select("div:has(p)", doc);
        assertEquals(2, divsWithP.size());

        // Target elements that contain a span
        Elements hasSpan = Selector.select(":has(span)", doc.body());
        assertTrue("Should match parent elements containing span", hasSpan.size() >= 2);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testInvalidQueryTokenThrowsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("div?", doc);
    }

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testInvalidAttributeSelectorThrowsException() {
        Document doc = Jsoup.parse("<div></div>");
        // Attribute with unknown comparator '%='
        Selector.select("div[name%=val]", doc);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNonNumericIndexThrowsException() {
        Document doc = Jsoup.parse("<div><p>One</p></div>");
        Selector.select("p:eq(abc)", doc);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyHasSubQueryThrowsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(":has()", doc);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyContainsQueryThrowsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(":contains()", doc);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyMatchesQueryThrowsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(":matches()", doc);
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testUnknownCombinatorThrowsException() {
        Document doc = Jsoup.parse("<div><p>A</p></div>");
        // A query starting with ',' matches combinators list but isn't handled by >, ' ', +, ~
        Selector.select(", p", doc);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSelectorParseExceptionMessageFormatting() {
        Selector.SelectorParseException ex = new Selector.SelectorParseException("Error at %s in %s", "idx", "query");
        assertNotNull(ex.getMessage());
        assertEquals("Error at idx in query", ex.getMessage());
        assertTrue(ex instanceof IllegalStateException);
    }
}