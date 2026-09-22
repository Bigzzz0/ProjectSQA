package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.select.Selector
 * Targeted Defects4J Defect:
 *   - Issue: In Selector.combinator(char), subQuery is consumed using tq.consumeToAny(combinators).
 *     Because combinators contains '~', any attribute regex selector using '~=' (e.g. [attr~=regex] or
 *     combined with parent/ancestor combinators like "div [class~=x|y]") prematurely breaks on the '~'
 *     character, causing the remainder to be misparsed as a combinator followed by subQuery '=x|y]',
 *     triggering SelectorParseException: "Could not parse query '=x|y]': unexpected token at '=x|y]'".
 *
 * Branch & Decision Matrix:
 * 1. Selector() constructor:
 *    - query null / empty / whitespace check
 *    - root null check
 * 2. select(String, Iterable<Element>):
 *    - query empty check
 *    - roots null check
 *    - multi-root aggregation
 * 3. select():
 *    - starts with combinator ('>', '+', '~') -> adds root and processes
 *    - starts with ':has(' -> adds all root elements
 *    - normal element start -> findElements()
 *    - while loop: ',' group OR operator; combinators; whitespace combinator (' '); AND filter self
 * 4. combinator(char):
 *    - '>' (children)
 *    - ' ' (descendants)
 *    - '+' (adjacent sibling)
 *    - '~' (general sibling)
 *    - unknown combinator branch -> IllegalStateException
 * 5. findElements():
 *    - ID '#', Class '.', Tag word, Attribute '[', Universal '*',
 *    - :lt(n), :gt(n), :eq(n), :has(sel), :contains(text), :containsOwn(text),
 *      :matches(regex), :matchesOwn(regex), :not(sel)
 *    - unexpected token -> SelectorParseException
 * 6. byTag(): namespace syntax 'ns|tag' -> 'ns:tag'
 * 7. byAttribute():
 *    - key only, '^' prefix key
 *    - '=', '!=', '^=', '$=', '*=', '~='
 *    - invalid attribute query syntax -> SelectorParseException
 * 8. Filters:
 *    - filterForChildren, filterForDescendants, filterForAdjacentSiblings, filterForGeneralSiblings,
 *      filterForSelf, filterOut
 * ---------------------------------------------------------------------------------------------------------
 */
public class SelectorGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & Standard Combinators
    // =========================================================================

    @Test(timeout = 4000)
    public void testSelectByTagIdAndClass() {
        String html = "<div id='main' class='container'><p class='text highlight'>Hello</p><span>World</span></div>";
        Document doc = Jsoup.parse(html);

        Elements byTag = Selector.select("p", doc);
        assertEquals(1, byTag.size());
        assertEquals("p", byTag.first().tagName());

        Elements byId = Selector.select("#main", doc);
        assertEquals(1, byId.size());
        assertEquals("main", byId.first().id());

        Elements byClass = Selector.select(".container", doc);
        assertEquals(1, byClass.size());
        assertEquals("container", byClass.first().className());

        Elements combined = Selector.select("div#main.container", doc);
        assertEquals(1, combined.size());
        assertEquals("main", combined.first().id());
    }

    @Test(timeout = 4000)
    public void testSelectUniversalAndNamespaces() {
        String html = "<div><fb:like id='fblike'>Like</fb:like><p>Text</p></div>";
        Document doc = Jsoup.parse(html);

        Elements all = Selector.select("*", doc);
        assertTrue(all.size() >= 4); // html, head, body, div, fb:like, p

        Elements ns = Selector.select("fb|like", doc);
        assertEquals(1, ns.size());
        assertEquals("fb:like", ns.first().tagName());
        assertEquals("fblike", ns.first().id());
    }

    @Test(timeout = 4000)
    public void testCombinatorsDescendantAndDirectChild() {
        String html = "<div id='p1'><div id='p2'><p id='target1'>One</p></div><p id='target2'>Two</p></div>";
        Document doc = Jsoup.parse(html);

        Elements descendants = Selector.select("div#p1 p", doc);
        assertEquals(2, descendants.size());

        Elements directChildren = Selector.select("div#p1 > p", doc);
        assertEquals(1, directChildren.size());
        assertEquals("target2", directChildren.first().id());
    }

    @Test(timeout = 4000)
    public void testCombinatorsAdjacentAndGeneralSibling() {
        String html = "<div id='parent'><h1 id='h'>Title</h1><p id='p1'>First</p><p id='p2'>Second</p><span id='s1'>End</span></div>";
        Document doc = Jsoup.parse(html);

        Elements adjSibling = Selector.select("h1 + p", doc);
        assertEquals(1, adjSibling.size());
        assertEquals("p1", adjSibling.first().id());

        Elements genSiblings = Selector.select("h1 ~ p", doc);
        assertEquals(2, genSiblings.size());
        assertEquals("p1", genSiblings.get(0).id());
        assertEquals("p2", genSiblings.get(1).id());
    }

    @Test(timeout = 4000)
    public void testOrCombinatorGrouping() {
        String html = "<div><h1>Title</h1><p>Para</p><span>Span</span></div>";
        Document doc = Jsoup.parse(html);

        Elements multi = Selector.select("h1, span", doc);
        assertEquals(2, multi.size());
        assertEquals("h1", multi.get(0).tagName());
        assertEquals("span", multi.get(1).tagName());
    }

    @Test(timeout = 4000)
    public void testAttributeSelectors() {
        String html = "<div>" +
                "<a href='http://example.com/test.html' title='link1' data-ref='123'>Link</a>" +
                "<img src='image.png' width='500' alt='sample text' />" +
                "<input type='text' disabled />" +
                "</div>";
        Document doc = Jsoup.parse(html);

        assertEquals(1, Selector.select("[disabled]", doc).size());
        assertEquals(1, Selector.select("[^data-]", doc).size());
        assertEquals(1, Selector.select("[title=link1]", doc).size());
        assertTrue(Selector.select("[title!=link1]", doc).size() > 0);
        assertEquals(1, Selector.select("[href^=http://]", doc).size());
        assertEquals(1, Selector.select("[src$=.png]", doc).size());
        assertEquals(1, Selector.select("[href*=/test.]", doc).size());
        assertEquals(1, Selector.select("[alt~=sample]", doc).size());
    }

    @Test(timeout = 4000)
    public void testPseudoSelectorsIndexes() {
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
    public void testPseudoSelectorsTextAndRegex() {
        String html = "<div><p id='p1'>Hello <b>World</b></p><p id='p2'>Hello Alone</p></div>";
        Document doc = Jsoup.parse(html);

        assertEquals(2, Selector.select("p:contains(World)", doc).size() + 1); // doc has 1 p with 'World'
        assertEquals(1, Selector.select("p:contains(World)", doc).size());
        assertEquals(0, Selector.select("p:containsOwn(World)", doc).size());
        assertEquals(1, Selector.select("b:containsOwn(World)", doc).size());

        assertEquals(1, Selector.select("p:matches((?i)alone)", doc).size());
        assertEquals(1, Selector.select("p:matchesOwn((?i)alone)", doc).size());
    }

    @Test(timeout = 4000)
    public void testPseudoHasAndNot() {
        String html = "<div id='d1'><p class='special'>Special text</p></div>" +
                      "<div id='d2'><p>Ordinary text</p></div>";
        Document doc = Jsoup.parse(html);

        Elements hasSpecial = Selector.select("div:has(.special)", doc);
        assertEquals(1, hasSpecial.size());
        assertEquals("d1", hasSpecial.first().id());

        Elements notSpecial = Selector.select("div:not(#d1)", doc);
        boolean containsD2 = false;
        for (Element el : notSpecial) {
            if ("d2".equals(el.id())) {
                containsD2 = true;
            }
            assertNotEquals("d1", el.id());
        }
        assertTrue(containsD2);
    }

    @Test(timeout = 4000)
    public void testSelectWithIterableRoots() {
        Document doc = Jsoup.parse("<div><p class='a'>1</p></div><div><p class='a'>2</p></div>");
        Elements divs = doc.getElementsByTag("div");

        Elements ps = Selector.select("p.a", divs);
        assertEquals(2, ps.size());
        assertEquals("1", ps.get(0).text());
        assertEquals("2", ps.get(1).text());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Structural Edge Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testSelectorStartingWithCombinators() {
        Document doc = Jsoup.parse("<div id='root'><p id='c1'>Child 1</p><p id='c2'>Child 2</p></div>");
        Element div = doc.getElementById("root");

        Elements directChildren = Selector.select("> p", div);
        assertEquals(2, directChildren.size());

        Element firstP = doc.getElementById("c1");
        Elements nextSib = Selector.select("+ p", firstP);
        assertEquals(1, nextSib.size());
        assertEquals("c2", nextSib.first().id());

        Elements genSib = Selector.select("~ p", firstP);
        assertEquals(1, genSib.size());
        assertEquals("c2", genSib.first().id());
    }

    @Test(timeout = 4000)
    public void testSelectorStartingWithHas() {
        Document doc = Jsoup.parse("<div id='box'><p><span>Nested</span></p></div>");
        Elements matches = Selector.select(":has(span)", doc);
        assertFalse(matches.isEmpty());
        assertTrue(matches.contains(doc.getElementById("box")));
    }

    @Test(timeout = 4000)
    public void testExtraWhitespaceBetweenCombinators() {
        Document doc = Jsoup.parse("<div>  <ul>  <li>Item</li>  </ul>  </div>");
        Elements items = Selector.select("div   >   ul   >   li", doc);
        assertEquals(1, items.size());
        assertEquals("Item", items.first().text());
    }

    @Test(timeout = 4000)
    public void testEmptyResultWhenNotFound() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        Elements empty = Selector.select("span.non-existent", doc);
        assertNotNull(empty);
        assertEquals(0, empty.size());
    }

    @Test(timeout = 4000)
    public void testEmptyRootsCollection() {
        List<Element> emptyList = Collections.emptyList();
        Elements result = Selector.select("p", emptyList);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test(timeout = 4000)
    public void testAdjacentSiblingWithNoSiblingOrDifferentParent() {
        Document doc = Jsoup.parse("<div id='d1'><p id='p1'>A</p></div><div id='d2'><p id='p2'>B</p></div>");
        Element p1 = doc.getElementById("p1");
        // p2 is in a different parent, so p1 + p2 across parents shouldn't match
        Elements res = Selector.select("+ p", p1);
        assertEquals(0, res.size());

        Elements genRes = Selector.select("~ p", p1);
        assertEquals(0, genRes.size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where Selector breaks on '~' in attribute regexes combined with
     * hierarchy combinators, e.g. "div [class~=foo|bar]" or "div table[class~=foo|bar]".
     * On the defective code, Selector throws:
     * SelectorParseException: "Could not parse query '=x|y]': unexpected token at '=x|y]'"
     */
    @Test(timeout = 4000)
    public void testByAttributeRegexCombined() {
        String html = "<div>" +
                "<table class='foo'><tr><td>Hello</td></tr></table>" +
                "<table class='bar'><tr><td>World</td></tr></table>" +
                "<table class='baz'><tr><td>Other</td></tr></table>" +
                "</div>";
        Document doc = Jsoup.parse(html);

        Elements matchedTables = Selector.select("div table[class~=foo|bar]", doc);
        assertEquals(2, matchedTables.size());
        assertEquals("foo", matchedTables.get(0).className());
        assertEquals("bar", matchedTables.get(1).className());

        Elements matchedDescendant = Selector.select("div [class~=foo|bar]", doc);
        assertEquals(2, matchedDescendant.size());

        Elements matchedChild = Selector.select("div > [class~=foo|bar]", doc);
        assertEquals(2, matchedChild.size());
    }

    @Test(timeout = 4000)
    public void testByAttributeRegexWithMultipleAlternations() {
        String html = "<div>" +
                "<p class='x'>one</p>" +
                "<p class='y'>two</p>" +
                "<p class='z'>three</p>" +
                "</div>";
        Document doc = Jsoup.parse(html);

        Elements p = Selector.select("div p[class~=x|y]", doc);
        assertEquals(2, p.size());
        assertEquals("x", p.get(0).className());
        assertEquals("y", p.get(1).className());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullQueryThrows() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(null, doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorEmptyQueryThrows() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("", doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorWhitespaceQueryThrows() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("   ", doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullRootThrows() {
        Selector.select("div", (Element) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIterableSelectNullRootsThrows() {
        Selector.select("div", (Iterable<Element>) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIterableSelectEmptyQueryThrows() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("", Collections.singletonList((Element) doc));
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testUnexpectedTokenThrowsParseException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("div!@#$", doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNonNumericIndexLtThrows() {
        Document doc = Jsoup.parse("<ol><li>1</li></ol>");
        Selector.select("li:lt(abc)", doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNonNumericIndexGtThrows() {
        Document doc = Jsoup.parse("<ol><li>1</li></ol>");
        Selector.select("li:gt(xyz)", doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNonNumericIndexEqThrows() {
        Document doc = Jsoup.parse("<ol><li>1</li></ol>");
        Selector.select("li:eq(one)", doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyHasSubselectThrows() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        Selector.select("div:has()", doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyContainsThrows() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        Selector.select("p:contains()", doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyContainsOwnThrows() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        Selector.select("p:containsOwn()", doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyMatchesThrows() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        Selector.select("p:matches()", doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyMatchesOwnThrows() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        Selector.select("p:matchesOwn()", doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyNotThrows() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        Selector.select("p:not()", doc);
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testMalformedAttributeQueryThrows() {
        Document doc = Jsoup.parse("<div><a href='#'>link</a></div>");
        Selector.select("a[href?value]", doc);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Package-Level Methods & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSelectorParseExceptionMessageFormatting() {
        Selector.SelectorParseException ex = new Selector.SelectorParseException("Error at token '%s' in query '%s'", "foo", "bar");
        assertEquals("Error at token 'foo' in query 'bar'", ex.getMessage());
        assertTrue(ex instanceof IllegalStateException);
    }

    @Test(timeout = 4000)
    public void testFilterOutStaticMethod() {
        Document doc = Jsoup.parse("<div><p id='1'></p><p id='2'></p><p id='3'></p></div>");
        Element p1 = doc.getElementById("1");
        Element p2 = doc.getElementById("2");
        Element p3 = doc.getElementById("3");

        List<Element> original = Arrays.asList(p1, p2, p3);
        List<Element> outs = Collections.singletonList(p2);

        Elements remaining = Selector.filterOut(original, outs);
        assertEquals(2, remaining.size());
        assertTrue(remaining.contains(p1));
        assertFalse(remaining.contains(p2));
        assertTrue(remaining.contains(p3));
    }

    @Test(timeout = 4000)
    public void testFilterOutWithNoExclusions() {
        Document doc = Jsoup.parse("<div><p id='1'></p><p id='2'></p></div>");
        Element p1 = doc.getElementById("1");
        Element p2 = doc.getElementById("2");

        List<Element> original = Arrays.asList(p1, p2);
        List<Element> outs = Collections.emptyList();

        Elements remaining = Selector.filterOut(original, outs);
        assertEquals(2, remaining.size());
        assertTrue(remaining.contains(p1));
        assertTrue(remaining.contains(p2));
    }
}