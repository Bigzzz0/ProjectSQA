package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * The target class QueryParser contains two known defects:
 * 1. PatternSyntaxException when parsing selectors with commas inside :matches() or :matchesOwn() due to improper handling of balanced parentheses.
 * 2. Incorrect element count when mixing comma-separated selectors with combinators (e.g., "div > p, span" yields 3 instead of 2).
 * 
 * This test suite targets:
 * - All major functional paths: element types (id, class, tag, attribute, all), pseudo-selectors (:lt, :gt, :eq, :has, :contains, :containsOwn, :matches, :matchesOwn, :not)
 * - Combinators: space, >, +, ~
 * - Comma (OR) grouping
 * - Edge cases: empty query, invalid indices, missing values, malformed regex, nested parentheses
 * - Boundary values: empty strings, zero/negative indices, null-like values (empty queries)
 * - Exception paths: missing subselects, invalid combinators, unbalanced brackets
 * 
 * The defect-revealing tests directly trigger the known failures.
 */
public class QueryParserDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void parseById() {
        Evaluator eval = QueryParser.parse("#myId");
        assertTrue("Expected Evaluator.Id", eval instanceof Evaluator.Id);
        assertEquals("myId", ((Evaluator.Id) eval).getId());
    }

    @Test(timeout = 4000)
    public void parseByClass() {
        Evaluator eval = QueryParser.parse(".myClass");
        assertTrue("Expected Evaluator.Class", eval instanceof Evaluator.Class);
        assertEquals("myclass", ((Evaluator.Class) eval).getClassName());
    }

    @Test(timeout = 4000)
    public void parseByTagWithNamespace() {
        Evaluator eval = QueryParser.parse("abc|def");
        assertTrue(eval instanceof Evaluator.Tag);
        assertEquals("abc:def", ((Evaluator.Tag) eval).getTagName());
    }

    @Test(timeout = 4000)
    public void parseByTagSimple() {
        Evaluator eval = QueryParser.parse("div");
        assertTrue(eval instanceof Evaluator.Tag);
        assertEquals("div", ((Evaluator.Tag) eval).getTagName());
    }

    @Test(timeout = 4000)
    public void parseAllElements() {
        Evaluator eval = QueryParser.parse("*");
        assertTrue(eval instanceof Evaluator.AllElements);
    }

    @Test(timeout = 4000)
    public void parseByAttributeNoValue() {
        Evaluator eval = QueryParser.parse("[data]");
        assertTrue(eval instanceof Evaluator.Attribute);
        assertEquals("data", ((Evaluator.Attribute) eval).getKey());
    }

    @Test(timeout = 4000)
    public void parseByAttributeWithValue() {
        Evaluator eval = QueryParser.parse("[attr=val]");
        assertTrue(eval instanceof Evaluator.AttributeWithValue);
        assertEquals("attr", ((Evaluator.AttributeWithValue) eval).getKey());
        assertEquals("val", ((Evaluator.AttributeWithValue) eval).getValue());
    }

    @Test(timeout = 4000)
    public void parseByAttributeNotEqual() {
        Evaluator eval = QueryParser.parse("[attr!=val]");
        assertTrue(eval instanceof Evaluator.AttributeWithValueNot);
    }

    @Test(timeout = 4000)
    public void parseByAttributeStartsWith() {
        Evaluator eval = QueryParser.parse("[attr^=val]");
        assertTrue(eval instanceof Evaluator.AttributeWithValueStarting);
    }

    @Test(timeout = 4000)
    public void parseByAttributeEndsWith() {
        Evaluator eval = QueryParser.parse("[attr$=val]");
        assertTrue(eval instanceof Evaluator.AttributeWithValueEnding);
    }

    @Test(timeout = 4000)
    public void parseByAttributeContains() {
        Evaluator eval = QueryParser.parse("[attr*=val]");
        assertTrue(eval instanceof Evaluator.AttributeWithValueContaining);
    }

    @Test(timeout = 4000)
    public void parseByAttributeRegex() {
        Evaluator eval = QueryParser.parse("[attr~=val]");
        assertTrue(eval instanceof Evaluator.AttributeWithValueMatching);
    }

    @Test(timeout = 4000)
    public void parseByAttributeStartingKey() {
        Evaluator eval = QueryParser.parse("[^data]");
        assertTrue(eval instanceof Evaluator.AttributeStarting);
        assertEquals("data", ((Evaluator.AttributeStarting) eval).getKey());
    }

    @Test(timeout = 4000)
    public void parsePseudoLessThan() {
        Evaluator eval = QueryParser.parse(":lt(5)");
        assertTrue(eval instanceof Evaluator.IndexLessThan);
        assertEquals(5, ((Evaluator.IndexLessThan) eval).getIndex());
    }

    @Test(timeout = 4000)
    public void parsePseudoGreaterThan() {
        Evaluator eval = QueryParser.parse(":gt(3)");
        assertTrue(eval instanceof Evaluator.IndexGreaterThan);
        assertEquals(3, ((Evaluator.IndexGreaterThan) eval).getIndex());
    }

    @Test(timeout = 4000)
    public void parsePseudoEquals() {
        Evaluator eval = QueryParser.parse(":eq(0)");
        assertTrue(eval instanceof Evaluator.IndexEquals);
        assertEquals(0, ((Evaluator.IndexEquals) eval).getIndex());
    }

    @Test(timeout = 4000)
    public void parsePseudoHas() {
        Evaluator eval = QueryParser.parse(":has(p)");
        assertTrue(eval instanceof StructuralEvaluator.Has);
    }

    @Test(timeout = 4000)
    public void parsePseudoContains() {
        Evaluator eval = QueryParser.parse(":contains(text)");
        assertTrue(eval instanceof Evaluator.ContainsText);
    }

    @Test(timeout = 4000)
    public void parsePseudoContainsOwn() {
        Evaluator eval = QueryParser.parse(":containsOwn(own)");
        assertTrue(eval instanceof Evaluator.ContainsOwnText);
    }

    @Test(timeout = 4000)
    public void parsePseudoMatches() {
        Evaluator eval = QueryParser.parse(":matches(\\d+)");
        assertTrue(eval instanceof Evaluator.Matches);
    }

    @Test(timeout = 4000)
    public void parsePseudoMatchesOwn() {
        Evaluator eval = QueryParser.parse(":matchesOwn(\\w+)");
        assertTrue(eval instanceof Evaluator.MatchesOwn);
    }

    @Test(timeout = 4000)
    public void parsePseudoNot() {
        Evaluator eval = QueryParser.parse(":not(div)");
        assertTrue(eval instanceof StructuralEvaluator.Not);
    }

    // -------------------- Combinators --------------------

    @Test(timeout = 4000)
    public void parseDescendantCombinator() {
        Evaluator eval = QueryParser.parse("div p");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void parseChildCombinator() {
        Evaluator eval = QueryParser.parse("div > p");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void parseAdjacentSiblingCombinator() {
        Evaluator eval = QueryParser.parse("div + p");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void parseGeneralSiblingCombinator() {
        Evaluator eval = QueryParser.parse("div ~ p");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(timeout = 4000)
    public void parseMultipleCombinators() {
        Evaluator eval = QueryParser.parse("div > p + span");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // -------------------- Comma (OR) grouping --------------------

    @Test(timeout = 4000)
    public void parseOrGroup() {
        Evaluator eval = QueryParser.parse("div, p");
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    @Test(timeout = 4000)
    public void parseOrWithCombinators() {
        Evaluator eval = QueryParser.parse("div > p, span > a");
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    @Test(timeout = 4000)
    public void parseOrWithMultipleParts() {
        Evaluator eval = QueryParser.parse("a, b, c");
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void parseEmptyQuery() {
        QueryParser.parse("");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void parseOnlyWhitespace() {
        QueryParser.parse("   ");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void parseEmptyId() {
        QueryParser.parse("#");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void parseEmptyClass() {
        QueryParser.parse(".");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void parseEmptyTag() {
        QueryParser.parse(" ");
    }

    // Actually tag can't be empty; parse throws SelectorParseException? Let's handle.
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void parseNonNumericIndex() {
        QueryParser.parse(":lt(abc)");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void parseInvalidIndexNegative() {
        QueryParser.parse(":lt(-1)");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void parseEmptyHasSubselect() {
        QueryParser.parse(":has()");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void parseEmptyContainsText() {
        QueryParser.parse(":contains()");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void parseEmptyMatchesRegex() {
        QueryParser.parse(":matches()");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void parseEmptyNotSubselect() {
        QueryParser.parse(":not()");
    }

    // Test that commas inside balanced parentheses don't break parsing
    @Test(timeout = 4000)
    public void parseCommaInsidePseudo() {
        // This should parse without error; in buggy version it throws PatternSyntaxException
        Evaluator eval = null;
        try {
            eval = QueryParser.parse("div:matches(\\d+,\\w+)");
        } catch (Exception e) {
            fail("Parsing selector with comma inside :matches() should not throw exception. Got: " + e.getClass().getName() + ": " + e.getMessage());
        }
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And); // because div tag + :matches
    }

    @Test(timeout = 4000)
    public void parseCommaInsideAttributeValue() {
        // Commas in attribute values inside brackets should be preserved
        Evaluator eval = QueryParser.parse("[attr=val,ue]");
        assertTrue(eval instanceof Evaluator.AttributeWithValue);
    }

    @Test(timeout = 4000)
    public void parseNestedParentheses() {
        // Nested parentheses in :has() with comma inside
        Evaluator eval = QueryParser.parse(":has(div > p, span)");
        assertTrue(eval instanceof StructuralEvaluator.Has);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect 1: PatternSyntaxException when commas appear inside :matches() or :matchesOwn().
     * This test verifies that the parser correctly handles comma-separated regex inside balanced parentheses.
     */
    @Test(timeout = 4000)
    public void testHandlesCommasInSelector() {
        // The original failing test from Defects4J: "div:matches(\\d+,\\w+)" would cause PatternSyntaxException.
        // Ensure parsing succeeds and the evaluator is built correctly.
        Evaluator eval = null;
        try {
            eval = QueryParser.parse("div:matches(\\d+,\\w+)");
        } catch (Exception e) {
            fail("Failed to parse selector with comma in regex: " + e.getMessage());
        }
        assertNotNull(eval);
        // Verify it is a combination of Tag and Matches
        assertTrue(eval + " should be CombiningEvaluator.And", eval instanceof CombiningEvaluator.And);
    }

    /**
     * Defect 2: mixCombinatorGroup - "div > p, span" selects 3 elements instead of 2.
     * This test replicates the scenario by creating a document and selecting with that query.
     */
    @Test(timeout = 4000)
    public void testMixCombinatorGroup() {
        String html = "<div><p>1</p></div><span>2</span>";
        Document doc = Jsoup.parse(html);
        Elements result = doc.select("div > p, span");
        assertEquals("Expected 2 elements but got " + result.size(), 2, result.size());
        assertEquals("p", result.get(0).tagName());
        assertEquals("span", result.get(1).tagName());
    }

    // Additional defect-targeted test: multiple commas with combinators
    @Test(timeout = 4000)
    public void testComplexOrGroup() {
        String html = "<div><p>1</p></div><span>2</span><a>3</a>";
        Document doc = Jsoup.parse(html);
        Elements result = doc.select("div > p, span, a");
        assertEquals(3, result.size());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void parseInvalidCombinator() {
        // The combinator method throws for unknown combinators like '%'
        QueryParser.parse("div % p");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void parseEmptyAttributeKey() {
        QueryParser.parse("[=val]");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void parseUnclosedBracket() {
        QueryParser.parse("div[attr");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void parseInvalidAttributeOperator() {
        // byAttribute will throw because operator not recognized
        QueryParser.parse("[attr%=val]");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void parseUnclosedPseudo() {
        QueryParser.parse(":has(div");
    }

    @Test(expected = Selector.SelectorParseException.class, timeout = 4000)
    public void parseUnexpectedToken() {
        // findElements throws when no pattern matches, e.g. starting with '?'
        QueryParser.parse("?div");
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    // No serialization or cloning; we test state consistency

    @Test(timeout = 4000)
    public void parseMultipleCalls() {
        // Vertify that sequential calls to parse() produce independent results
        Evaluator e1 = QueryParser.parse("div");
        Evaluator e2 = QueryParser.parse("p");
        assertNotSame(e1, e2);
    }

    // Verify that parsing of a simple tag returns a single Evaluator (not wrapped in And)
    @Test(timeout = 4000)
    public void parseSingleEvaluator() {
        Evaluator e = QueryParser.parse("span");
        assertTrue(e instanceof Evaluator.Tag);
    }
}