package org.jsoup.select;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * <h3>Branch & Defect Analysis Matrix</h3>
 * <ul>
 *   <li><b>Combinator branches:</b> '>', ' ', '+', '~', ',' – needs test for each.</li>
 *   <li><b>findElements branches:</b> #id, .class, tag, [attr], *, :lt(), :gt(), :eq(), :has(), :contains, :matches, and the missing :not() handling.</li>
 *   <li><b>Attribute operator branches:</b> =, !=, ^=, $=, *=, ~=, and prefix-only (^attr without value).</li>
 *   <li><b>Pseudo-selector argument validation:</b> numeric index, non-numeric index, empty parentheses.</li>
 *   <li><b>Defect focus:</b> Missing implementation of :not(sel) – tests in testNotSelector will fail on buggy version (throws SelectorParseException).</li>
 *   <li><b>Edge cases:</b> Null/empty query, null root, unbalanced brackets.</li>
 * </ul>
 */
public class SelectorDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testTagSelector() {
        Document doc = Jsoup.parse("<div><p>hello</p><span>world</span></div>");
        Elements result = Selector.select("p", doc);
        assertEquals(1, result.size());
        assertEquals("p", result.get(0).tagName());
    }

    @Test(timeout = 4000)
    public void testIdSelector() {
        Document doc = Jsoup.parse("<div id='a'>x</div><p id='b'>y</p>");
        Elements result = Selector.select("#b", doc);
        assertEquals(1, result.size());
        assertEquals("b", result.get(0).id());
    }

    @Test(timeout = 4000)
    public void testClassSelector() {
        Document doc = Jsoup.parse("<div class='foo bar'>x</div><p class='foo'>y</p>");
        Elements result = Selector.select(".foo", doc);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testUniversalSelector() {
        Document doc = Jsoup.parse("<p>a</p><div>b</div>");
        Elements result = Selector.select("*", doc);
        // includes document itself? Actually root is the given element; *.body? We pass doc, so it selects all descendants.
        assertTrue(result.size() >= 2);
    }

    @Test(timeout = 4000)
    public void testChildCombinator() {
        Document doc = Jsoup.parse("<div><p>child</p><span><p>not direct</p></span></div>");
        Elements result = Selector.select("div > p", doc);
        assertEquals(1, result.size());
        assertEquals("child", result.get(0).text());
    }

    @Test(timeout = 4000)
    public void testDescendantCombinator() {
        Document doc = Jsoup.parse("<div><p>a</p><span><p>b</p></span></div>");
        Elements result = Selector.select("div p", doc);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testAdjacentSiblingCombinator() {
        Document doc = Jsoup.parse("<div></div><p>first</p><p>second</p>");
        Elements result = Selector.select("div + p", doc);
        assertEquals(1, result.size());
        assertEquals("first", result.get(0).text());
    }

    @Test(timeout = 4000)
    public void testGeneralSiblingCombinator() {
        Document doc = Jsoup.parse("<div></div><p>a</p><span></span><p>b</p>");
        Elements result = Selector.select("div ~ p", doc);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testGroupSelector() {
        Document doc = Jsoup.parse("<p>a</p><div>b</div><span>c</span>");
        Elements result = Selector.select("p, div", doc);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributePresenceSelector() {
        Document doc = Jsoup.parse("<p id='x'>a</p><p>no</p>");
        Elements result = Selector.select("p[id]", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributePrefixSelector() {
        Document doc = Jsoup.parse("<p data-abc='x'>a</p><p data-xyz='y'>b</p><p>no</p>");
        Elements result = Selector.select("[^data-]", doc);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributeExactValueSelector() {
        Document doc = Jsoup.parse("<a href='http://example.com'>link</a><a href='https://other'>other</a>");
        Elements result = Selector.select("a[href='http://example.com']", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributeNotEqualSelector() {
        Document doc = Jsoup.parse("<a href='val'>a</a><a href='other'>b</a><a nohref>c</a>");
        Elements result = Selector.select("a[href!='val']", doc);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributeStartsWithSelector() {
        Document doc = Jsoup.parse("<a href='https://a'>a</a><a href='http://b'>b</a>");
        Elements result = Selector.select("a[href^='http://']", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributeEndsWithSelector() {
        Document doc = Jsoup.parse("<img src='pic.png'><img src='pic.jpg'>");
        Elements result = Selector.select("img[src$='.png']", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributeContainsSelector() {
        Document doc = Jsoup.parse("<a href='/search/foo'>a</a><a href='/results/bar'>b</a>");
        Elements result = Selector.select("a[href*='/search/']", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testAttributeMatchesRegexSelector() {
        Document doc = Jsoup.parse("<img src='a.png'><img src='b.jpg'>");
        Elements result = Selector.select("img[src~=(?i)\\.(png|jpe?g)]", doc);
        assertEquals(2, result.size());
    }

    // ========== Partition B: Pseudo Selectors ==========

    @Test(timeout = 4000)
    public void testIndexLessThan() {
        Document doc = Jsoup.parse("<ul><li>A</li><li>B</li><li>C</li></ul>");
        Elements result = Selector.select("ul li:lt(2)", doc);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testIndexGreaterThan() {
        Document doc = Jsoup.parse("<ul><li>A</li><li>B</li><li>C</li></ul>");
        Elements result = Selector.select("ul li:gt(0)", doc);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testIndexEquals() {
        Document doc = Jsoup.parse("<ul><li>A</li><li>B</li><li>C</li></ul>");
        Elements result = Selector.select("ul li:eq(1)", doc);
        assertEquals(1, result.size());
        assertEquals("B", result.get(0).text());
    }

    @Test(timeout = 4000)
    public void testHas() {
        Document doc = Jsoup.parse("<div><p>hello</p></div><div><span>world</span></div>");
        Elements result = Selector.select("div:has(p)", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testContainsText() {
        Document doc = Jsoup.parse("<p>Hello World</p><p>Goodbye</p>");
        Elements result = Selector.select("p:contains(hello)", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testContainsOwnText() {
        Document doc = Jsoup.parse("<p>Hello <span>World</span></p><p>Goodbye</p>");
        Elements result = Selector.select("p:containsOwn(Hello)", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testMatchesRegex() {
        Document doc = Jsoup.parse("<p>123</p><p>abc</p>");
        Elements result = Selector.select("p:matches(\\d+)", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testMatchesOwnRegex() {
        Document doc = Jsoup.parse("<p>123 <b>456</b></p><p>abc</p>");
        Elements result = Selector.select("p:matchesOwn(\\d+)", doc);
        assertEquals(1, result.size());
    }

    // ========== Partition C: Defect-Targeted Branch Zone (:not) ==========

    @Test(timeout = 4000)
    public void testNotSelector() {
        Document doc = Jsoup.parse("<p>a</p><div>b</div><span>c</span>");
        // This should select all elements that are not <p>
        Elements result = Selector.select(":not(p)", doc);
        // On fixed version: <div> and <span> (plus maybe root node depending on implementation)
        // On buggy version: throws SelectorParseException -> test fails
        assertTrue(result.size() >= 2);
        for (Element e : result) {
            assertNotEquals("p", e.tagName());
        }
    }

    @Test(timeout = 4000)
    public void testNotClassSelector() {
        Document doc = Jsoup.parse("<div class='left'>a</div><div class='right'>b</div>");
        Elements result = Selector.select("div:not(.left)", doc);
        assertEquals(1, result.size());
        assertEquals("right", result.get(0).className());
    }

    @Test(timeout = 4000)
    public void testNotAttributeSelector() {
        Document doc = Jsoup.parse("<p id='1'>a</p><p id='2'>b</p><p>c</p>");
        Elements result = Selector.select("p:not([id=1])", doc);
        assertEquals(2, result.size());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullQuery() {
        Selector.select(null, new Element("div"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyQuery() {
        Selector.select("", new Element("div"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullRoot() {
        Document doc = Jsoup.parse("<p></p>");
        Selector.select("p", null);
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testMalformedAttributeBrackets() {
        Document doc = Jsoup.parse("<p>a</p>");
        Selector.select("p[href", doc);
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testMalformedPseudoMissingParenthesis() {
        Document doc = Jsoup.parse("<p>a</p>");
        Selector.select(":contains(abc", doc);
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testNonNumericIndex() {
        Document doc = Jsoup.parse("<ul><li></li></ul>");
        Selector.select("li:eq(abc)", doc);
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testEmptyPseudo() {
        Document doc = Jsoup.parse("<p></p>");
        Selector.select(":unknown()", doc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyId() {
        Document doc = Jsoup.parse("<p id=''>a</p>");
        Selector.select("#", doc);
    }

    // ========== Partition E: Edge Cases & Object Lifecycle ==========

    @Test(timeout = 4000)
    public void testEmptyResult() {
        Document doc = Jsoup.parse("<p>a</p>");
        Elements result = Selector.select("span", doc);
        assertEquals(0, result.size());
    }

    @Test(timeout = 4000)
    public void testSubQueryWithComma() {
        Document doc = Jsoup.parse("<p>a</p><div>b</div><span>c</span>");
        Elements result = Selector.select("p, span", doc);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testSubSelectOnElements() {
        // Test the overload that takes Iterable<Element>
        Document doc = Jsoup.parse("<div><p>a</p></div><div><p>b</p></div>");
        Elements divs = Selector.select("div", doc);
        Elements paras = Selector.select("p", (Iterable<Element>) divs);
        assertEquals(2, paras.size());
    }

    @Test(timeout = 4000)
    public void testComplexSelectorChain() {
        Document doc = Jsoup.parse("<div class='header'><a href='http://example.com' title='link'>link</a></div>");
        Elements result = Selector.select("div.header a[href^='http://'][title='link']", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testMultipleAttributeConditions() {
        Document doc = Jsoup.parse("<input type='text' name='user'>");
        Elements result = Selector.select("input[name][type='text']", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testPseudoHasWithComplexSubselect() {
        Document doc = Jsoup.parse("<div><p class='foo'>a</p></div><div><span>b</span></div>");
        Elements result = Selector.select("div:has(p.foo)", doc);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000)
    public void testPseudoContainsCaseInsensitive() {
        Document doc = Jsoup.parse("<p>Hello World</p><p>hello</p>");
        Elements result = Selector.select("p:contains(hello)", doc);
        assertEquals(2, result.size());
    }
}