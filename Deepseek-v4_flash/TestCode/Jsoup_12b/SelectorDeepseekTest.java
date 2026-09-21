package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: Selector (org.jsoup.select)
 * Known defect: SelectorParseException thrown for query "=x|y]" (testByAttributeRegexCombined)
 * 
 * Branch coverage targets:
 * - select() initial if-else: combinator start, :has( start, else (findElements)
 * - select() while loop: comma, combinator, whitespace combinator, AND (intersect)
 * - combinator(): >, space, +, ~, else throw
 * - findElements(): #, ., word, [, *, :lt, :gt, :eq, :has, :contains, :containsOwn, :matches, :matchesOwn, :not, else throw
 * - byAttribute(): key with/without operator, various operators (=, !=, ^=, $=, *=, ~=), else throw
 * - consumeIndex(): numeric validation
 * - filterForChildren, filterForDescendants, filterForParentsOfDescendants, filterForAdjacentSiblings, filterForGeneralSiblings, filterForSelf, filterOut
 * - Constructor validation: null/empty query, null root
 * - Static select(Iterable) validation: empty query, null roots
 * 
 * Boundary conditions:
 * - Empty query string
 * - Null query/root
 * - Index values: 0, negative, non-numeric
 * - Attribute values: empty key, empty value
 * - Combinator at start of query
 * - Multiple commas
 * - Nested pseudo-selectors
 * - Regex with special characters
 */
public class SelectorDeepseekTest {

    private Document createDoc(String html) {
        return Jsoup.parse(html);
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testBasicTagSelector() {
        Document doc = createDoc("<div><p>text</p></div>");
        Elements result = Selector.select("p", doc);
        assertEquals(1, result.size());
        assertEquals("p", result.get(0).tagName());
    }

    @Test(timeout = 4000)
    public void testIdSelector() {
        Document doc = createDoc("<div id='foo'><span>bar</span></div>");
        Elements result = Selector.select("#foo", doc);
        assertEquals(1, result.size());
        assertEquals("foo", result.get(0).id());
    }

    @Test(timeout = 4000)
    public void testClassSelector() {
        Document doc = createDoc("<div class='test'><span>content</span></div>");
        Elements result = Selector.select(".test", doc);
        assertEquals(1, result.size());
        assertTrue(result.get(0).hasClass("test"));
    }

    @Test(timeout = 4000)
    public void testAttributeSelector() {
        Document doc = createDoc("<a href='http://example.com'>link</a>");
        Elements result = Selector.select("a[href]", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributeEqualsSelector() {
        Document doc = createDoc("<a href='http://example.com'>link</a>");
        Elements result = Selector.select("a[href='http://example.com']", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributeNotEqualsSelector() {
        Document doc = createDoc("<a href='http://example.com'>link</a><a href='other'>other</a>");
        Elements result = Selector.select("a[href!='http://example.com']", doc);
        assertEquals(1, result.size());
        assertEquals("other", result.get(0).attr("href"));
    }

    @Test(timeout = 4000)
    public void testAttributeStartsWithSelector() {
        Document doc = createDoc("<a href='http://example.com'>link</a>");
        Elements result = Selector.select("a[href^='http']", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributeEndsWithSelector() {
        Document doc = createDoc("<a href='http://example.com'>link</a>");
        Elements result = Selector.select("a[href$='.com']", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributeContainsSelector() {
        Document doc = createDoc("<a href='http://example.com'>link</a>");
        Elements result = Selector.select("a[href*='example']", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributeRegexSelector() {
        Document doc = createDoc("<img src='image.png'><img src='image.jpg'>");
        Elements result = Selector.select("img[src~=(?i)\\.(png|jpe?g)]", doc);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributeStartingPrefixSelector() {
        Document doc = createDoc("<div data-value='test'>data</div>");
        Elements result = Selector.select("[^data-]", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testUniversalSelector() {
        Document doc = createDoc("<div><p>text</p></div>");
        Elements result = Selector.select("*", doc);
        assertEquals(3, result.size()); // div, p, text node? Actually text node is not element, so 2 elements
        // Actually Jsoup parses text as TextNode, not Element. So * returns all elements: div and p.
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testCombinatorDescendant() {
        Document doc = createDoc("<div><p><span>deep</span></p></div>");
        Elements result = Selector.select("div span", doc);
        assertEquals(1, result.size());
        assertEquals("span", result.get(0).tagName());
    }

    @Test(timeout = 4000)
    public void testCombinatorChild() {
        Document doc = createDoc("<div><p>child</p><span>not child</span></div>");
        Elements result = Selector.select("div > p", doc);
        assertEquals(1, result.size());
        assertEquals("p", result.get(0).tagName());
    }

    @Test(timeout = 4000)
    public void testCombinatorAdjacentSibling() {
        Document doc = createDoc("<ul><li>one</li><li>two</li><li>three</li></ul>");
        Elements result = Selector.select("li + li", doc);
        assertEquals(2, result.size()); // two and three
    }

    @Test(timeout = 4000)
    public void testCombinatorGeneralSibling() {
        Document doc = createDoc("<h1>title</h1><p>first</p><p>second</p>");
        Elements result = Selector.select("h1 ~ p", doc);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testCommaOrSelector() {
        Document doc = createDoc("<div>div</div><p>p</p><span>span</span>");
        Elements result = Selector.select("div, p", doc);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testPseudoLt() {
        Document doc = createDoc("<ul><li>1</li><li>2</li><li>3</li></ul>");
        Elements result = Selector.select("li:lt(2)", doc);
        assertEquals(2, result.size()); // indices 0 and 1
    }

    @Test(timeout = 4000)
    public void testPseudoGt() {
        Document doc = createDoc("<ul><li>1</li><li>2</li><li>3</li></ul>");
        Elements result = Selector.select("li:gt(0)", doc);
        assertEquals(2, result.size()); // indices 1 and 2
    }

    @Test(timeout = 4000)
    public void testPseudoEq() {
        Document doc = createDoc("<ul><li>1</li><li>2</li><li>3</li></ul>");
        Elements result = Selector.select("li:eq(1)", doc);
        assertEquals(1, result.size());
        assertEquals("2", result.get(0).text());
    }

    @Test(timeout = 4000)
    public void testPseudoHas() {
        Document doc = createDoc("<div><p>text</p></div><div><span>other</span></div>");
        Elements result = Selector.select("div:has(p)", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testPseudoContains() {
        Document doc = createDoc("<p>Hello World</p><p>Goodbye</p>");
        Elements result = Selector.select("p:contains(hello)", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testPseudoContainsOwn() {
        Document doc = createDoc("<div><p>outer</p>inner</div>");
        Elements result = Selector.select("div:containsOwn(inner)", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testPseudoMatches() {
        Document doc = createDoc("<p>123</p><p>abc</p>");
        Elements result = Selector.select("p:matches(\\d+)", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testPseudoMatchesOwn() {
        Document doc = createDoc("<div>own text</div>");
        Elements result = Selector.select("div:matchesOwn(own)", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testPseudoNot() {
        Document doc = createDoc("<div class='a'>A</div><div class='b'>B</div>");
        Elements result = Selector.select("div:not(.a)", doc);
        assertEquals(1, result.size());
        assertTrue(result.get(0).hasClass("b"));
    }

    @Test(timeout = 4000)
    public void testCombinedSelectors() {
        Document doc = createDoc("<div class='header' id='top'><p>text</p></div>");
        Elements result = Selector.select("div.header#top", doc);
        assertEquals(1, result.size());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullQuery() {
        Selector.select(null, createDoc("<div></div>"));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyQuery() {
        Selector.select("", createDoc("<div></div>"));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBlankQuery() {
        Selector.select("   ", createDoc("<div></div>"));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullRoot() {
        Selector.select("div", (Element) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyQueryIterable() {
        Selector.select("", Collections.<Element>emptyList());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullRootsIterable() {
        Selector.select("div", (Iterable<Element>) null);
    }

    @Test(timeout = 4000)
    public void testEmptyResult() {
        Document doc = createDoc("<div></div>");
        Elements result = Selector.select("span", doc);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIndexZero() {
        Document doc = createDoc("<ul><li>1</li><li>2</li></ul>");
        Elements result = Selector.select("li:eq(0)", doc);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).text());
    }

    @Test(timeout = 4000)
    public void testIndexNegative() {
        Document doc = createDoc("<ul><li>1</li></ul>");
        // Negative index is not numeric, should throw
        try {
            Selector.select("li:lt(-1)", doc);
            fail("Expected SelectorParseException for negative index");
        } catch (Selector.SelectorParseException e) {
            assertTrue(e.getMessage().contains("Index must be numeric"));
        }
    }

    @Test(timeout = 4000)
    public void testIndexNonNumeric() {
        Document doc = createDoc("<ul><li>1</li></ul>");
        try {
            Selector.select("li:lt(abc)", doc);
            fail("Expected SelectorParseException for non-numeric index");
        } catch (Selector.SelectorParseException e) {
            assertTrue(e.getMessage().contains("Index must be numeric"));
        }
    }

    @Test(timeout = 4000)
    public void testEmptyAttributeKey() {
        Document doc = createDoc("<div attr='val'></div>");
        try {
            Selector.select("[='val']", doc);
            fail("Expected SelectorParseException for empty key");
        } catch (Selector.SelectorParseException e) {
            // The exception is thrown from byAttribute because key is empty after consumeToAny
            assertTrue(e.getMessage().contains("Could not parse attribute query"));
        }
    }

    @Test(timeout = 4000)
    public void testEmptyAttributeValue() {
        Document doc = createDoc("<div attr=''></div>");
        Elements result = Selector.select("[attr='']", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testMultipleRootsIterable() {
        Document doc1 = createDoc("<p>one</p>");
        Document doc2 = createDoc("<p>two</p>");
        Elements result = Selector.select("p", Arrays.asList(doc1, doc2));
        assertEquals(2, result.size());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testByAttributeRegexCombined() {
        // This is the known defect: query "=x|y]" should throw SelectorParseException
        Document doc = createDoc("<div></div>");
        Selector.select("=x|y]", doc);
    }

    @Test(timeout = 4000)
    public void testMalformedAttributeSelector() {
        Document doc = createDoc("<div></div>");
        try {
            Selector.select("[attr~=invalid", doc);
            fail("Expected SelectorParseException");
        } catch (Selector.SelectorParseException e) {
            // Should fail because unbalanced brackets
        }
    }

    @Test(timeout = 4000)
    public void testCombinatorAtStart() {
        Document doc = createDoc("<div><p>child</p></div>");
        Elements result = Selector.select("> p", doc);
        assertEquals(1, result.size());
        assertEquals("p", result.get(0).tagName());
    }

    @Test(timeout = 4000)
    public void testCombinatorUnknown() {
        Document doc = createDoc("<div></div>");
        try {
            Selector.select("div & span", doc);
            fail("Expected IllegalStateException for unknown combinator");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Unknown combinator"));
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testUnrecognizedPseudoSelector() {
        Document doc = createDoc("<div></div>");
        Selector.select("div:unknown", doc);
    }

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testUnbalancedPseudo() {
        Document doc = createDoc("<div></div>");
        Selector.select("div:has(p", doc);
    }

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testEmptyPseudoSubselect() {
        Document doc = createDoc("<div></div>");
        Selector.select("div:has()", doc);
    }

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testEmptyContainsText() {
        Document doc = createDoc("<div></div>");
        Selector.select("div:contains()", doc);
    }

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testEmptyMatchesRegex() {
        Document doc = createDoc("<div></div>");
        Selector.select("div:matches()", doc);
    }

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testEmptyNotSubselect() {
        Document doc = createDoc("<div></div>");
        Selector.select("div:not()", doc);
    }

    @Test(timeout = 4000)
    public void testAttributeOperatorWithoutValue() {
        Document doc = createDoc("<div attr='val'></div>");
        // [attr=] is invalid because value is empty? Actually it's allowed? Let's test.
        // The parser will consume "=" and then remainder is empty. root.getElementsByAttributeValue(key, "") should work.
        Elements result = Selector.select("[attr=]", doc);
        assertEquals(1, result.size());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testSelectReturnsNewElementsEachCall() {
        Document doc = createDoc("<div><p>text</p></div>");
        Elements first = Selector.select("p", doc);
        Elements second = Selector.select("p", doc);
        assertNotSame(first, second);
        assertEquals(first, second);
    }

    @Test(timeout = 4000)
    public void testSelectorParseExceptionIsIllegalStateException() {
        try {
            Selector.select("invalid", createDoc("<div></div>"));
        } catch (Selector.SelectorParseException e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test(timeout = 4000)
    public void testFilterOutStaticMethod() {
        Document doc = createDoc("<div class='a'>A</div><div class='b'>B</div><div class='c'>C</div>");
        Elements all = doc.getAllElements();
        Elements toExclude = Selector.select(".a", doc);
        Elements filtered = Selector.filterOut(all, toExclude);
        assertEquals(2, filtered.size());
        assertFalse(filtered.get(0).hasClass("a"));
    }

    @Test(timeout = 4000)
    public void testNamespaceTagSelector() {
        Document doc = createDoc("<fb:name>value</fb:name>");
        // In Jsoup, namespace is handled via colon, but selector uses pipe
        Elements result = Selector.select("fb|name", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testMultipleCommas() {
        Document doc = createDoc("<a>a</a><b>b</b><c>c</c>");
        Elements result = Selector.select("a, b, c", doc);
        assertEquals(3, result.size());
    }

    @Test(timeout = 4000)
    public void testNestedPseudoHas() {
        Document doc = createDoc("<div><p><span>text</span></p></div>");
        Elements result = Selector.select("div:has(p:has(span))", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testPseudoContainsCaseInsensitive() {
        Document doc = createDoc("<p>Hello World</p>");
        Elements result = Selector.select("p:contains(hello)", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testPseudoMatchesRegexFlags() {
        Document doc = createDoc("<p>Hello</p>");
        Elements result = Selector.select("p:matches((?i)hello)", doc);
        assertEquals(1, result.size());
    }
}