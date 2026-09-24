package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.CombiningEvaluator;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;
import org.jsoup.select.Selector;
import org.jsoup.select.StructuralEvaluator;
import org.junit.Test;

import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.jsoup.select.QueryParser
 *
 * Branch / Condition Coverage Map:
 * - parse():
 *     * Combinator at start: matchesAny(combinators) -> evals.add(Root), combinator(...)
 *     * Leading whitespace: tq.consumeWhitespace()
 *     * Top-level comma grouping: tq.matchChomp(",") -> CombiningEvaluator.Or
 *     * Combinator token in queue: matchesAny(combinators) -> combinator(tq.consume())
 *     * Whitespace combinator: seenWhite == true -> combinator(' ')
 *     * Multi-combinator chains: E.class, E#id, E[attr] (evals.size() > 1 AND grouping)
 *     * evals.size() == 1 return vs CombiningEvaluator.And return
 * - combinator(char combinator):
 *     * evals.size() == 1 vs CombiningEvaluator.And(evals) when evals.size() > 1
 *     * combinator == '>' (ImmediateParent)
 *     * combinator == ' ' (Parent)
 *     * combinator == '+' (ImmediatePreviousSibling)
 *     * combinator == '~' (PreviousSibling)
 *     * Unknown combinator fallback -> SelectorParseException
 * - consumeSubQuery():
 *     * Bracket and parenthesis balancing: chompBalanced('(', ')'), chompBalanced('[', ']')
 *     * Break on combinators in subquery
 * - findElements():
 *     * '#' -> byId()
 *     * '.' -> byClass()
 *     * tag word -> byTag() (including namespace pipe '|' conversion to ':')
 *     * '[' -> byAttribute() (=, !=, ^=, $=, *=, ~=, ^prefix, bare attribute)
 *     * '*' -> allElements()
 *     * ':lt(' -> indexLessThan()
 *     * ':gt(' -> indexGreaterThan()
 *     * ':eq(' -> indexEquals()
 *     * ':has(' -> has()
 *     * ':contains(' -> contains(false)
 *     * ':containsOwn(' -> contains(true)
 *     * ':matches(' -> matches(false)
 *     * ':matchesOwn(' -> matches(true)
 *     * ':not(' -> not()
 *     * Unhandled token fallback -> SelectorParseException
 *
 * Defects4J Ground Truth Defect Targets:
 * - SelectorTest::handlesCommasInSelector:
 *     * Defect: tq.chompTo(",") within comma-grouped selector splits inside nested subqueries
 *       (e.g., regex character classes like [0-9,a-z] or attribute values containing commas),
 *       leaving unclosed regex/brackets: causes PatternSyntaxException: Unclosed character class.
 * - SelectorTest::mixCombinatorGroup:
 *     * Defect: Evaluator tree corruption when mixing combinator chains with comma grouping,
 *       yielding incorrect match count (e.g. expected:<2> but was:<3>).
 * ====================================================================================================
 */
public class QueryParserGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & Structural Evaluator Trees
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseIdSelector() {
        Evaluator eval = QueryParser.parse("#header");
        assertTrue(eval instanceof Evaluator.Id);
        assertEquals("header", eval.toString());
    }

    @Test(timeout = 4000)
    public void testParseClassSelector() {
        Evaluator eval = QueryParser.parse(".btn-primary");
        assertTrue(eval instanceof Evaluator.Class);
        assertEquals(".btn-primary", eval.toString());
    }

    @Test(timeout = 4000)
    public void testParseTagSelectorNormalAndNamespace() {
        Evaluator evalNormal = QueryParser.parse("div");
        assertTrue(evalNormal instanceof Evaluator.Tag);
        assertEquals("div", evalNormal.toString());

        Evaluator evalNs = QueryParser.parse("svg|circle");
        assertTrue(evalNs instanceof Evaluator.Tag);
        assertEquals("svg:circle", evalNs.toString());
    }

    @Test(timeout = 4000)
    public void testParseAllElementsSelector() {
        Evaluator eval = QueryParser.parse("*");
        assertTrue(eval instanceof Evaluator.AllElements);
        assertEquals("*", eval.toString());
    }

    @Test(timeout = 4000)
    public void testParseAttributeSelectors() {
        Evaluator bareAttr = QueryParser.parse("[disabled]");
        assertTrue(bareAttr instanceof Evaluator.Attribute);

        Evaluator attrPrefix = QueryParser.parse("[^data-]");
        assertTrue(attrPrefix instanceof Evaluator.AttributeStarting);

        Evaluator attrEq = QueryParser.parse("[type=text]");
        assertTrue(attrEq instanceof Evaluator.AttributeWithValue);

        Evaluator attrNotEq = QueryParser.parse("[type!=hidden]");
        assertTrue(attrNotEq instanceof Evaluator.AttributeWithValueNot);

        Evaluator attrStarts = QueryParser.parse("[href^=https]");
        assertTrue(attrStarts instanceof Evaluator.AttributeWithValueStarting);

        Evaluator attrEnds = QueryParser.parse("[src$=.png]");
        assertTrue(attrEnds instanceof Evaluator.AttributeWithValueEnding);

        Evaluator attrContains = QueryParser.parse("[title*=info]");
        assertTrue(attrContains instanceof Evaluator.AttributeWithValueContaining);

        Evaluator attrRegex = QueryParser.parse("[class~=^[a-z]+$]");
        assertTrue(attrRegex instanceof Evaluator.AttributeWithValueMatching);
    }

    @Test(timeout = 4000)
    public void testParseIndexSelectors() {
        Evaluator lt = QueryParser.parse(":lt(3)");
        assertTrue(lt instanceof Evaluator.IndexLessThan);

        Evaluator gt = QueryParser.parse(":gt(1)");
        assertTrue(gt instanceof Evaluator.IndexGreaterThan);

        Evaluator eq = QueryParser.parse(":eq(0)");
        assertTrue(eq instanceof Evaluator.IndexEquals);
    }

    @Test(timeout = 4000)
    public void testParsePseudoSelectors() {
        Evaluator has = QueryParser.parse(":has(p > a)");
        assertTrue(has instanceof StructuralEvaluator.Has);

        Evaluator contains = QueryParser.parse(":contains(Click Here)");
        assertTrue(contains instanceof Evaluator.ContainsText);

        Evaluator containsOwn = QueryParser.parse(":containsOwn(Direct Text)");
        assertTrue(containsOwn instanceof Evaluator.ContainsOwnText);

        Evaluator matches = QueryParser.parse(":matches(\\d{3}-\\d{4})");
        assertTrue(matches instanceof Evaluator.Matches);

        Evaluator matchesOwn = QueryParser.parse(":matchesOwn(^[A-Z]+$)");
        assertTrue(matchesOwn instanceof Evaluator.MatchesOwn);

        Evaluator not = QueryParser.parse(":not(div.alert)");
        assertTrue(not instanceof StructuralEvaluator.Not);
    }

    @Test(timeout = 4000)
    public void testCombinatorVariations() {
        // Child combinator (>)
        Evaluator child = QueryParser.parse("div > span");
        assertTrue(child instanceof CombiningEvaluator.And);

        // Descendant combinator (space)
        Evaluator descendant = QueryParser.parse("div span");
        assertTrue(descendant instanceof CombiningEvaluator.And);

        // Adjacent sibling combinator (+)
        Evaluator adjacent = QueryParser.parse("h1 + p");
        assertTrue(adjacent instanceof CombiningEvaluator.And);

        // General sibling combinator (~)
        Evaluator sibling = QueryParser.parse("h1 ~ p");
        assertTrue(sibling instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testMultiCombinatorAndEvaluatorChain() {
        // evals.size() > 1 branch in combinator()
        Evaluator eval = QueryParser.parse("div.content#main > p");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testLeadingCombinatorUsesRoot() {
        Evaluator eval = QueryParser.parse("> span");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Formatting Edge Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testSurroundingWhitespaceAndMultipleSpaces() {
        Evaluator eval = QueryParser.parse("   div    >    span   ");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testBalancedParenthesesAndBracketsInSubQuery() {
        Evaluator eval = QueryParser.parse("div:not([class=foo]) > p:has(span[data-id=bar])");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testZeroAndLargeIndexValues() {
        Evaluator eqZero = QueryParser.parse(":eq(0)");
        assertNotNull(eqZero);

        Evaluator gtLarge = QueryParser.parse(":gt(999999)");
        assertNotNull(gtLarge);
    }

    @Test(timeout = 4000)
    public void testUnescapeInContains() {
        Evaluator eval = QueryParser.parse(":contains(foo\\(bar\\))");
        assertTrue(eval instanceof Evaluator.ContainsText);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Defects4J Ground Truth Regressions)
    // =========================================================================

    /**
     * Targets Defect: SelectorTest::handlesCommasInSelector
     * tq.chompTo(",") within top-level comma loop cuts through nested selectors containing commas,
     * such as regex character classes or commas within attribute selectors.
     */
    @Test(timeout = 4000)
    public void testHandlesCommasInSelectorRegexPattern() {
        // When Chomping to "," without balancing brackets/parentheses,
        // the subQuery breaks at [0-9, and pattern compilation throws PatternSyntaxException
        Evaluator eval = QueryParser.parse("p:matches([0-9]), div:matches([0-9,a-z])");
        assertNotNull("Evaluator tree should parse comma-delimited regex subqueries safely", eval);
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    /**
     * Targets Defect: SelectorTest::handlesCommasInSelector on Document level
     */
    @Test(timeout = 4000)
    public void testHandlesCommasInSelectorDocumentEvaluation() {
        Document doc = Jsoup.parse("<p><a href='abc'>1</a><a href='def'>2</a></p>");
        assertEquals(2, doc.select("a[href=abc], a[href=def]").size());

        Document docRegex = Jsoup.parse("<div>123</div><p>456,abc</p>");
        assertEquals(2, docRegex.select("div:matches([0-9]), p:matches([0-9,a-z])").size());
    }

    /**
     * Targets Defect: SelectorTest::mixCombinatorGroup
     * Ensures combinator groups mixing structural steps and multiple commas parse into
     * non-interfering sub-evaluators and match exact document counts.
     */
    @Test(timeout = 4000)
    public void testMixCombinatorGroup() {
        String html = "<small><span><b>1</b></span><span><em>2</em></span></small>";
        Document doc = Jsoup.parse(html);
        org.jsoup.select.Elements els = doc.select("small > * > b, small > * > em");
        assertEquals(2, els.size());
        assertEquals("1", els.get(0).text());
        assertEquals("2", els.get(1).text());
    }

    @Test(timeout = 4000)
    public void testCommasWithinAttributeSelectorValue() {
        Evaluator eval = QueryParser.parse("a, [data-query='foo,bar']");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testUnknownCombinatorThrows() {
        QueryParser.parse("div % span");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testUnexpectedTokenThrows() {
        QueryParser.parse("@unexpected");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testMalformedAttributeOperatorThrows() {
        QueryParser.parse("[key?val]");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyIdThrows() {
        QueryParser.parse("#");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyClassThrows() {
        QueryParser.parse(".");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNonNumericIndexLessThanThrows() {
        QueryParser.parse(":lt(not-numeric)");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNonNumericIndexGreaterThanThrows() {
        QueryParser.parse(":gt(abc)");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNonNumericIndexEqualsThrows() {
        QueryParser.parse(":eq(xyz)");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyHasSubselectThrows() {
        QueryParser.parse(":has()");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyContainsThrows() {
        QueryParser.parse(":contains()");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyContainsOwnThrows() {
        QueryParser.parse(":containsOwn()");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyMatchesThrows() {
        QueryParser.parse(":matches()");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyMatchesOwnThrows() {
        QueryParser.parse(":matchesOwn()");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyNotThrows() {
        QueryParser.parse(":not()");
    }

    @Test(expected = PatternSyntaxException.class, timeout = 4000)
    public void testMalformedRegexInMatchesThrows() {
        QueryParser.parse(":matches([unclosed-pattern)");
    }

    // =========================================================================
    // Partition E: Multi-Combinator & Structural Hierarchy State Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testMultipleSequentialCommas() {
        Evaluator eval = QueryParser.parse("h1, h2, h3, h4");
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    @Test(timeout = 4000)
    public void testComplexSelectorComposition() {
        Evaluator eval = QueryParser.parse("div.card > h2.title + p:not(.hidden)");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }
}