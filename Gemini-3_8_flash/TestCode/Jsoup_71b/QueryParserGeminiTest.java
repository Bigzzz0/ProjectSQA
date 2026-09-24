package org.jsoup.select;

import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.select.QueryParser
 *
 * Decision / Condition Matrix:
 * 1. Combinator at start:
 *    - startsWith combinator (>, +, ~, ,, ' ') -> wraps in StructuralEvaluator.Root and processes combinator.
 * 2. Combinator hierarchy:
 *    - combinator == '>' -> CombiningEvaluator.And with ImmediateParent
 *    - combinator == ' ' -> CombiningEvaluator.And with Parent
 *    - combinator == '+' -> CombiningEvaluator.And with ImmediatePreviousSibling
 *    - combinator == '~' -> CombiningEvaluator.And with PreviousSibling
 *    - combinator == ',' -> CombiningEvaluator.Or (new or add to existing Or)
 *    - Combinator precedence with OR: (rootEval instanceof CombiningEvaluator.Or && combinator != ',') -> replaceRightMostEvaluator
 *    - evals.size() == 1 vs evals.size() > 1 -> wrapping in CombiningEvaluator.And
 * 3. Element selector branches (findElements):
 *    - '#' -> byId
 *    - '.' -> byClass
 *    - Tag / namespace: word or '*|' -> Tag, TagEndsWith (*|tag), ns|tag (replaced with ns:tag)
 *    - '[' -> byAttribute: starting (^key), key-only, =, !=, ^=, $=, *=, ~=
 *    - '*' -> AllElements
 *    - Pseudo-selectors: :lt(n), :gt(n), :eq(n) with integer validation
 *    - :has(el), :contains(text), :containsOwn(text), :containsData(data)
 *    - :matches(regex), :matchesOwn(regex), :not(selector)
 *    - Pseudo-child selectors: :first-child, :last-child, :first-of-type, :last-of-type, :only-child, :only-of-type, :empty, :root
 *    - :nth-child, :nth-last-child, :nth-of-type, :nth-last-of-type with odd, even, an+b, b forms
 * 4. Known Ground-Truth Defect (Defects4J):
 *    - SelectorTest::splitOnBr and SelectorTest::textAsElements fail due to missing pseudo-selector ':matchText'.
 *      QueryParser throws SelectorParseException on 'p:matchText' and 'p:matchText:first-child'.
 */
public class QueryParserGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSimpleTagSelector() {
        Evaluator eval = QueryParser.parse("div");
        assertTrue(eval instanceof Evaluator.Tag);
        assertEquals("div", eval.toString());
    }

    @Test(timeout = 4000)
    public void testIdSelector() {
        Evaluator eval = QueryParser.parse("#mainHeader");
        assertTrue(eval instanceof Evaluator.Id);
        assertEquals("#mainHeader", eval.toString());
    }

    @Test(timeout = 4000)
    public void testClassSelector() {
        Evaluator eval = QueryParser.parse(".highlighted");
        assertTrue(eval instanceof Evaluator.Class);
        assertEquals(".highlighted", eval.toString());
    }

    @Test(timeout = 4000)
    public void testAllElementsSelector() {
        Evaluator eval = QueryParser.parse("*");
        assertTrue(eval instanceof Evaluator.AllElements);
        assertEquals("*", eval.toString());
    }

    @Test(timeout = 4000)
    public void testCompoundTagIdClassSelector() {
        Evaluator eval = QueryParser.parse("div#content.active");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testCombinatorImmediateParent() {
        Evaluator eval = QueryParser.parse("div > span");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testCombinatorDescendant() {
        Evaluator eval = QueryParser.parse("div span");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testCombinatorImmediatePreviousSibling() {
        Evaluator eval = QueryParser.parse("h1 + p");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testCombinatorPreviousSibling() {
        Evaluator eval = QueryParser.parse("h1 ~ p");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testCombinatorOrGrouping() {
        Evaluator eval = QueryParser.parse("div, p, span");
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    @Test(timeout = 4000)
    public void testOrPrecedenceWithCombinator() {
        // Triggers replaceRightMost in combinator method: Or(a, b) followed by > c
        Evaluator eval = QueryParser.parse("div, p > span");
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    @Test(timeout = 4000)
    public void testMultipleElementsBeforeCombinator() {
        // Multiple evals before combinator: evals.size() > 1 branch
        Evaluator eval = QueryParser.parse("div.btn > span");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testStartingWithCombinatorRoot() {
        // Selector starts with combinator
        Evaluator evalGreater = QueryParser.parse("> span");
        assertTrue(evalGreater instanceof CombiningEvaluator.And);

        Evaluator evalTilde = QueryParser.parse("~ p");
        assertTrue(evalTilde instanceof CombiningEvaluator.And);

        Evaluator evalPlus = QueryParser.parse("+ p");
        assertTrue(evalPlus instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testNamespaceHandling() {
        // Tag with specific namespace
        Evaluator evalNs = QueryParser.parse("svg|circle");
        assertTrue(evalNs instanceof Evaluator.Tag);
        assertEquals("svg:circle", evalNs.toString());

        // Tag with wildcard namespace
        Evaluator evalWildcard = QueryParser.parse("*|circle");
        assertTrue(evalWildcard instanceof CombiningEvaluator.Or);
    }

    @Test(timeout = 4000)
    public void testAttributeSelectors() {
        // Attribute exists
        Evaluator eval1 = QueryParser.parse("[disabled]");
        assertTrue(eval1 instanceof Evaluator.Attribute);

        // Attribute starts with prefix
        Evaluator eval2 = QueryParser.parse("[^data-]");
        assertTrue(eval2 instanceof Evaluator.AttributeStarting);

        // Attribute value equals
        Evaluator eval3 = QueryParser.parse("[type=text]");
        assertTrue(eval3 instanceof Evaluator.AttributeWithValue);

        // Attribute value not equals
        Evaluator eval4 = QueryParser.parse("[type!=text]");
        assertTrue(eval4 instanceof Evaluator.AttributeWithValueNot);

        // Attribute value starts with
        Evaluator eval5 = QueryParser.parse("[href^=https]");
        assertTrue(eval5 instanceof Evaluator.AttributeWithValueStarting);

        // Attribute value ends with
        Evaluator eval6 = QueryParser.parse("[src$=.png]");
        assertTrue(eval6 instanceof Evaluator.AttributeWithValueEnding);

        // Attribute value contains
        Evaluator eval7 = QueryParser.parse("[class*=active]");
        assertTrue(eval7 instanceof Evaluator.AttributeWithValueContaining);

        // Attribute value regex match
        Evaluator eval8 = QueryParser.parse("[title~=[a-z]+]");
        assertTrue(eval8 instanceof Evaluator.AttributeWithValueMatching);
    }

    @Test(timeout = 4000)
    public void testIndexBasedPseudoSelectors() {
        Evaluator lt = QueryParser.parse(":lt(3)");
        assertTrue(lt instanceof Evaluator.IndexLessThan);

        Evaluator gt = QueryParser.parse(":gt(5)");
        assertTrue(gt instanceof Evaluator.IndexGreaterThan);

        Evaluator eq = QueryParser.parse(":eq(1)");
        assertTrue(eq instanceof Evaluator.IndexEquals);
    }

    @Test(timeout = 4000)
    public void testStructuralPseudoSelectors() {
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
    public void testNthChildVariations() {
        // odd and even
        assertTrue(QueryParser.parse(":nth-child(odd)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(even)") instanceof Evaluator.IsNthChild);

        // an+b formats
        assertTrue(QueryParser.parse(":nth-child(2n+1)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(+2n+1)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(-2n+1)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(n)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(2n)") instanceof Evaluator.IsNthChild);

        // pure b formats
        assertTrue(QueryParser.parse(":nth-child(3)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(+3)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(-3)") instanceof Evaluator.IsNthChild);

        // Backwards and OfType combinations
        assertTrue(QueryParser.parse(":nth-last-child(2n+1)") instanceof Evaluator.IsNthLastChild);
        assertTrue(QueryParser.parse(":nth-of-type(2n+1)") instanceof Evaluator.IsNthOfType);
        assertTrue(QueryParser.parse(":nth-last-of-type(2n+1)") instanceof Evaluator.IsNthLastOfType);
    }

    @Test(timeout = 4000)
    public void testContentAndRegexPseudoSelectors() {
        assertTrue(QueryParser.parse(":has(p > a)") instanceof StructuralEvaluator.Has);
        assertTrue(QueryParser.parse(":contains(sample text)") instanceof Evaluator.ContainsText);
        assertTrue(QueryParser.parse(":containsOwn(sample text)") instanceof Evaluator.ContainsOwnText);
        assertTrue(QueryParser.parse(":containsData(scriptData)") instanceof Evaluator.ContainsData);
        assertTrue(QueryParser.parse(":matches(\\d+)") instanceof Evaluator.Matches);
        assertTrue(QueryParser.parse(":matchesOwn(\\d+)") instanceof Evaluator.MatchesOwn);
        assertTrue(QueryParser.parse(":not(.hidden)") instanceof StructuralEvaluator.Not);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNestedParenthesesAndBracketsInSubQuery() {
        // Nested sub-queries with brackets and parens inside
        Evaluator eval = QueryParser.parse("div:not([data-attr='value'])");
        assertTrue(eval instanceof CombiningEvaluator.And);

        Evaluator evalNested = QueryParser.parse("div:has(span:not(.active))");
        assertTrue(evalNested instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testLeadingAndTrailingWhitespace() {
        Evaluator eval = QueryParser.parse("   div.test   ");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testEscapedTextInContains() {
        Evaluator eval = QueryParser.parse(":contains(foo\\(bar\\))");
        assertTrue(eval instanceof Evaluator.ContainsText);
    }

    @Test(timeout = 4000)
    public void testNumericIndexBoundaryZero() {
        Evaluator eval = QueryParser.parse(":eq(0)");
        assertTrue(eval instanceof Evaluator.IndexEquals);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Known Defect)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMatchTextPseudoSelector() {
        // Direct target for Defect: Could not parse query 'p:matchText': unexpected token at ':matchText'
        Evaluator eval = QueryParser.parse("p:matchText");
        assertNotNull("Evaluator should not be null for p:matchText", eval);
    }

    @Test(timeout = 4000)
    public void testMatchTextWithCompoundPseudoSelector() {
        // Direct target for Defect: Could not parse query 'p:matchText:first-child'
        Evaluator eval = QueryParser.parse("p:matchText:first-child");
        assertNotNull("Evaluator should not be null for p:matchText:first-child", eval);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testEmptyQueryThrowsException() {
        QueryParser.parse("");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testWhitespaceOnlyQueryThrowsException() {
        QueryParser.parse("    ");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testUnknownPseudoSelectorThrowsException() {
        QueryParser.parse("div:unknownPseudo");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testInvalidIndexNotNumericThrowsException() {
        QueryParser.parse(":lt(notANumber)");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testInvalidNthChildFormatThrowsException() {
        QueryParser.parse(":nth-child(fooBar)");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testEmptyAttributeSelectorThrowsException() {
        QueryParser.parse("[]");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testUnsupportedAttributeOperatorThrowsException() {
        QueryParser.parse("[name?=value]");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testEmptyHasSubselectThrowsException() {
        QueryParser.parse(":has()");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testEmptyContainsThrowsException() {
        QueryParser.parse(":contains()");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testEmptyContainsOwnThrowsException() {
        QueryParser.parse(":containsOwn()");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testEmptyContainsDataThrowsException() {
        QueryParser.parse(":containsData()");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testEmptyMatchesThrowsException() {
        QueryParser.parse(":matches()");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testEmptyMatchesOwnThrowsException() {
        QueryParser.parse(":matchesOwn()");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testEmptyNotThrowsException() {
        QueryParser.parse(":not()");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testDanglingCombinatorThrowsException() {
        QueryParser.parse("div > ");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void testUnclosedBracketThrowsException() {
        QueryParser.parse("[href=val");
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Complex Combination Guard
    // =========================================================================

    @Test(timeout = 4000)
    public void testComplexChainedSelectorTree() {
        Evaluator eval = QueryParser.parse("div.card > h2.title + p:matches(^Hello) ~ a[href*='example.com']:not(.disabled)");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void testMultipleOrChains() {
        Evaluator eval = QueryParser.parse("h1, h2, h3, h4, h5, h6");
        assertTrue(eval instanceof CombiningEvaluator.Or);
        CombiningEvaluator.Or or = (CombiningEvaluator.Or) eval;
        assertEquals(6, or.evaluators.size());
    }
}