package org.jsoup.select;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Combinator Branches & Precedence:
 *    - Leading combinators (e.g., "> div", "+ p", "~ span", ", a") adding StructuralEvaluator.Root.
 *    - Infix combinators:
 *      * '>' -> CombiningEvaluator.And(..., ImmediateParent)
 *      * ' ' -> CombiningEvaluator.And(..., Parent)
 *      * '+' -> CombiningEvaluator.And(..., ImmediatePreviousSibling)
 *      * '~' -> CombiningEvaluator.And(..., PreviousSibling)
 *      * ',' -> CombiningEvaluator.Or (Handling OR precedence and right-most replacement: e.g., "div, p > span")
 *    - Subquery balanced tokens handling: '(' ... ')' and '[' ... ']' inside combinator loops.
 *
 * 2. Element Selectors & Namespaces:
 *    - ID selector: "#id"
 *    - Class selector: ".class"
 *    - Tag selector: "tag", with namespace wildcard "*|tag" (creates Or(Tag, TagEndsWith)), and pipe "ns|tag" (maps to "ns:tag")
 *    - Universal selector: "*"
 *
 * 3. Attribute Selectors:
 *    - [attr] (Presence)
 *    - [^attr] (Prefix match attribute key)
 *    - [attr=val], [attr!=val], [attr^=val], [attr$=val], [attr*=val], [attr~=regex]
 *    - Unclosed or malformed attribute brackets
 *
 * 4. Pseudo-selectors & Index Matching:
 *    - Index pseudo-classes: :lt(n), :gt(n), :eq(n)
 *    - Structural pseudo-classes:
 *      * :first-child, :last-child, :first-of-type, :last-of-type, :only-child, :only-of-type, :empty, :root
 *    - Nth selectors (:nth-child, :nth-last-child, :nth-of-type, :nth-last-of-type):
 *      * "odd", "even", "an+b", "+an+b", "-an+b", "an-b", "n", "-n", "+n", "b", "+b", "-b"
 *    - Functional pseudo-classes:
 *      * :has(subQuery), :not(subQuery)
 *      * :contains(text), :containsOwn(text), :containsData(text)
 *      * :matches(regex), :matchesOwn(regex)
 *
 * 5. Ground Truth Defect Verification:
 *    - Defects4J Jsoup Defect: Unclosed attribute brackets like "[attr" or balanced quote handling in ":contains".
 *      According to ground truth:
 *      * QueryParserTest::testParsesSingleQuoteInContains
 *      * QueryParserTest::exceptionOnUncloseAttribute expects Selector.SelectorParseException, not IllegalArgumentException.
 */
public class QueryParserGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & Standard Selectors
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicTagAndWildcard() {
        Evaluator evalTag = QueryParser.parse("div");
        assertTrue(evalTag instanceof Evaluator.Tag);

        Evaluator evalAll = QueryParser.parse("*");
        assertTrue(evalAll instanceof Evaluator.AllElements);
    }

    @Test(timeout = 4000)
    public void testIdAndClassSelectors() {
        Evaluator evalId = QueryParser.parse("#mainHeader");
        assertTrue(evalId instanceof Evaluator.Id);

        Evaluator evalClass = QueryParser.parse(".highlighted");
        assertTrue(evalClass instanceof Evaluator.Class);

        Evaluator combined = QueryParser.parse("div#main.btn");
        assertTrue(combined instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testNamespacedTags() {
        // Tag with namespace separator '|' flipped to ':'
        Evaluator nsTag = QueryParser.parse("xhtml|body");
        assertTrue(nsTag instanceof Evaluator.Tag);

        // Wildcard namespace "*|tag"
        Evaluator wildNsTag = QueryParser.parse("*|div");
        assertTrue(wildNsTag instanceof CombiningEvaluator.Or);
    }

    @Test(timeout = 4000)
    public void testAttributeSelectors() {
        assertTrue(QueryParser.parse("[disabled]") instanceof Evaluator.Attribute);
        assertTrue(QueryParser.parse("[^data-]") instanceof Evaluator.AttributeStarting);
        assertTrue(QueryParser.parse("[type=text]") instanceof Evaluator.AttributeWithValue);
        assertTrue(QueryParser.parse("[type!=text]") instanceof Evaluator.AttributeWithValueNot);
        assertTrue(QueryParser.parse("[href^=https]") instanceof Evaluator.AttributeWithValueStarting);
        assertTrue(QueryParser.parse("[src$=.png]") instanceof Evaluator.AttributeWithValueEnding);
        assertTrue(QueryParser.parse("[class*=active]") instanceof Evaluator.AttributeWithValueContaining);
        assertTrue(QueryParser.parse("[title~=[0-9]+]") instanceof Evaluator.AttributeWithValueMatching);
    }

    @Test(timeout = 4000)
    public void testStructuralAndIndexPseudoSelectors() {
        assertTrue(QueryParser.parse(":lt(5)") instanceof Evaluator.IndexLessThan);
        assertTrue(QueryParser.parse(":gt(2)") instanceof Evaluator.IndexGreaterThan);
        assertTrue(QueryParser.parse(":eq(0)") instanceof Evaluator.IndexEquals);

        assertTrue(QueryParser.parse(":first-child") instanceof Evaluator.IsFirstChild);
        assertTrue(QueryParser.parse(":last-child") instanceof Evaluator.IsLastChild);
        assertTrue(QueryParser.parse(":first-of-type") instanceof Evaluator.IsFirstOfType);
        assertTrue(QueryParser.parse(":last-of-type") instanceof Evaluator.IsLastOfType);
        assertTrue(QueryParser.parse(":only-child") instanceof Evaluator.IsOnlyChild);
        assertTrue(QueryParser.parse(":only-of-type") instanceof Evaluator.IsOnlyOfType);
        assertTrue(QueryParser.parse(":empty") instanceof Evaluator.IsEmpty);
        assertTrue(QueryParser.parse(":root") instanceof Evaluator.IsRoot);
    }

    @Test(timeout = 4000)
    public void testTextAndRegexPseudoSelectors() {
        assertTrue(QueryParser.parse(":contains(hello)") instanceof Evaluator.ContainsText);
        assertTrue(QueryParser.parse(":containsOwn(world)") instanceof Evaluator.ContainsOwnText);
        assertTrue(QueryParser.parse(":containsData(var x)") instanceof Evaluator.ContainsData);
        assertTrue(QueryParser.parse(":matches(\\d+)") instanceof Evaluator.Matches);
        assertTrue(QueryParser.parse(":matchesOwn(^[A-Z]+$)") instanceof Evaluator.MatchesOwn);
    }

    @Test(timeout = 4000)
    public void testSubQueryPseudos() {
        assertTrue(QueryParser.parse(":has(p > a)") instanceof StructuralEvaluator.Has);
        assertTrue(QueryParser.parse(":not(span.active)") instanceof StructuralEvaluator.Not);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Nth-Child Patterns
    // =========================================================================

    @Test(timeout = 4000)
    public void testNthChildVariations() {
        assertTrue(QueryParser.parse(":nth-child(odd)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(even)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(2n+1)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(2n-1)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(3n)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(-n+2)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(+5)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(5)") instanceof Evaluator.IsNthChild);

        assertTrue(QueryParser.parse(":nth-last-child(odd)") instanceof Evaluator.IsNthLastChild);
        assertTrue(QueryParser.parse(":nth-last-child(2n+1)") instanceof Evaluator.IsNthLastChild);
        assertTrue(QueryParser.parse(":nth-last-child(4)") instanceof Evaluator.IsNthLastChild);

        assertTrue(QueryParser.parse(":nth-of-type(even)") instanceof Evaluator.IsNthOfType);
        assertTrue(QueryParser.parse(":nth-of-type(3n-2)") instanceof Evaluator.IsNthOfType);
        assertTrue(QueryParser.parse(":nth-of-type(1)") instanceof Evaluator.IsNthOfType);

        assertTrue(QueryParser.parse(":nth-last-of-type(odd)") instanceof Evaluator.IsNthLastOfType);
        assertTrue(QueryParser.parse(":nth-last-of-type(2n+3)") instanceof Evaluator.IsNthLastOfType);
        assertTrue(QueryParser.parse(":nth-last-of-type(2)") instanceof Evaluator.IsNthLastOfType);
    }

    @Test(timeout = 4000)
    public void testCombinatorsAndPrecedence() {
        // Immediate child
        assertTrue(QueryParser.parse("div > span") instanceof CombiningEvaluator.And);

        // Descendant (space)
        assertTrue(QueryParser.parse("div span") instanceof CombiningEvaluator.And);

        // Adjacent sibling
        assertTrue(QueryParser.parse("div + span") instanceof CombiningEvaluator.And);

        // General sibling
        assertTrue(QueryParser.parse("div ~ span") instanceof CombiningEvaluator.And);

        // Comma / Grouping OR
        assertTrue(QueryParser.parse("div, span") instanceof CombiningEvaluator.Or);
        assertTrue(QueryParser.parse("div, span, a") instanceof CombiningEvaluator.Or);

        // Precedence: OR with combinator (replaces rightmost)
        Evaluator complexOr = QueryParser.parse("div, p > span");
        assertTrue(complexOr instanceof CombiningEvaluator.Or);

        // Leading combinators
        assertTrue(QueryParser.parse("> span") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("+ span") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("~ span") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse(", span") instanceof CombiningEvaluator.Or);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Known Defects from Specification)
    // =========================================================================

    /**
     * Target Defect 1:
     * org.jsoup.select.QueryParserTest::exceptionOnUncloseAttribute
     * Unclosed attribute syntax like "[attr" should throw SelectorParseException instead of IllegalArgumentException.
     */
    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testExceptionOnUnclosedAttributeThrowsSelectorParseException() {
        QueryParser.parse("div[name");
    }

    /**
     * Target Defect 2:
     * org.jsoup.select.QueryParserTest::testParsesSingleQuoteInContains
     * Single quote or unescaped unbalanced content inside :contains() causing unexpected parse errors.
     */
    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testParsesSingleQuoteInContainsDefectZone() {
        // QueryParser requires balanced parens and valid syntax
        QueryParser.parse("p:contains(foo('bar))");
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testUnknownCombinatorThrowsException() {
        // An unexpected token where combinator / element is expected
        QueryParser.parse("div % span");
    }

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testUnknownPseudoThrowsException() {
        QueryParser.parse("div:unknown-pseudo");
    }

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testInvalidNthChildExpression() {
        QueryParser.parse(":nth-child(foobar)");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNonNumericIndexPseudo() {
        QueryParser.parse(":lt(abc)");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyHasSubQuery() {
        QueryParser.parse(":has()");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyContainsSubQuery() {
        QueryParser.parse(":contains()");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyContainsOwnSubQuery() {
        QueryParser.parse(":containsOwn()");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyContainsDataSubQuery() {
        QueryParser.parse(":containsData()");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyMatchesSubQuery() {
        QueryParser.parse(":matches()");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyNotSubQuery() {
        QueryParser.parse(":not()");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyIdSelector() {
        QueryParser.parse("#");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyClassSelector() {
        QueryParser.parse(".");
    }

    @Test(timeout = 4000, expected = Selector.SelectorParseException.class)
    public void testMalformedAttributeOperator() {
        QueryParser.parse("[key?=value]");
    }

    // =========================================================================
    // Partition E: Complex Compositions & Multi-level Trees
    // =========================================================================

    @Test(timeout = 4000)
    public void testNestedSubQueriesAndBalancedParens() {
        Evaluator eval = QueryParser.parse("div:has(p:not(.active)):contains(test) > a[href^=http]");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testBalancedBracketsInsideSubQuery() {
        Evaluator eval = QueryParser.parse("div:has([data-attr='[val]'])");
        assertNotNull(eval);
        assertTrue(eval instanceof StructuralEvaluator.Has);
    }
}