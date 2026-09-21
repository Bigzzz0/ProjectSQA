package org.jsoup.select;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.jsoup.select.QueryParser (Defects4J bug)
 * 
 * Known Defects:
 * 1. testParsesSingleQuoteInContains - Expected SelectorParseException for malformed :contains() with single quote
 * 2. exceptionOnUncloseAttribute - Expected SelectorParseException but got IllegalArgumentException for unclosed attribute
 * 
 * Branch Coverage Targets:
 * - parse() entry: empty query, leading combinator, whitespace handling, multiple combinators
 * - findElements(): all selector types (#id, .class, tag, [attr], *, :lt, :gt, :eq, :has, :contains, :containsOwn, 
 *   :containsData, :matches, :matchesOwn, :not, :nth-child, :nth-last-child, :nth-of-type, :nth-last-of-type,
 *   :first-child, :last-child, :first-of-type, :last-of-type, :only-child, :only-of-type, :empty, :root)
 * - byAttribute(): all operators (=, !=, ^=, $=, *=, ~=), empty key, ^ prefix, invalid operator
 * - cssNthChild(): odd/even, An+B format, B format, invalid format
 * - consumeIndex(): numeric validation
 * - has(): empty subselect validation
 * - contains(): empty text validation
 * - matches(): empty regex validation
 * - not(): empty subselect validation
 * 
 * Boundary Values:
 * - Empty query string
 * - Single character selectors
 * - Complex combinators (>, +, ~, space, ,)
 * - Attribute with special characters
 * - Pseudo-classes with edge case arguments
 * - Null/empty subselects
 * - Invalid numeric indices
 * - Unclosed brackets
 * - Single quotes in :contains()
 */
public class QueryParserDeepseekTest {
    
    /* ==================== PART A: Core Functional Logic ==================== */
    
    @Test(timeout = 4000)
    public void testParseBasicTag() {
        Evaluator eval = QueryParser.parse("div");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Tag);
        Evaluator.Tag tag = (Evaluator.Tag) eval;
        assertEquals("div", tag.getTagName());
    }
    
    @Test(timeout = 4000)
    public void testParseIdSelector() {
        Evaluator eval = QueryParser.parse("#myId");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Id);
        Evaluator.Id id = (Evaluator.Id) eval;
        assertEquals("myId", id.getId());
    }
    
    @Test(timeout = 4000)
    public void testParseClassSelector() {
        Evaluator eval = QueryParser.parse(".myClass");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Class);
        Evaluator.Class cls = (Evaluator.Class) eval;
        assertEquals("myClass", cls.getClassName());
    }
    
    @Test(timeout = 4000)
    public void testParseAllElements() {
        Evaluator eval = QueryParser.parse("*");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AllElements);
    }
    
    @Test(timeout = 4000)
    public void testParseMultipleSelectors() {
        Evaluator eval = QueryParser.parse("div p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
        CombiningEvaluator.And and = (CombiningEvaluator.And) eval;
        assertEquals(2, and.getEvaluators().size());
    }
    
    @Test(timeout = 4000)
    public void testParseCommaSeparated() {
        Evaluator eval = QueryParser.parse("div, span");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.Or);
        CombiningEvaluator.Or or = (CombiningEvaluator.Or) eval;
        assertEquals(2, or.getEvaluators().size());
    }
    
    @Test(timeout = 4000)
    public void testParseChildCombinator() {
        Evaluator eval = QueryParser.parse("div > p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
        CombiningEvaluator.And and = (CombiningEvaluator.And) eval;
        assertEquals(2, and.getEvaluators().size());
        assertTrue(and.getEvaluators().get(1) instanceof StructuralEvaluator.ImmediateParent);
    }
    
    @Test(timeout = 4000)
    public void testParseSiblingCombinator() {
        Evaluator eval = QueryParser.parse("div + p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
        CombiningEvaluator.And and = (CombiningEvaluator.And) eval;
        assertEquals(2, and.getEvaluators().size());
        assertTrue(and.getEvaluators().get(1) instanceof StructuralEvaluator.ImmediatePreviousSibling);
    }
    
    @Test(timeout = 4000)
    public void testParseGeneralSiblingCombinator() {
        Evaluator eval = QueryParser.parse("div ~ p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
        CombiningEvaluator.And and = (CombiningEvaluator.And) eval;
        assertEquals(2, and.getEvaluators().size());
        assertTrue(and.getEvaluators().get(1) instanceof StructuralEvaluator.PreviousSibling);
    }
    
    @Test(timeout = 4000)
    public void testParseAttributeEquals() {
        Evaluator eval = QueryParser.parse("[href=test]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValue);
        Evaluator.AttributeWithValue attr = (Evaluator.AttributeWithValue) eval;
        assertEquals("href", attr.getKey());
        assertEquals("test", attr.getValue());
    }
    
    @Test(timeout = 4000)
    public void testParseAttributeNotEquals() {
        Evaluator eval = QueryParser.parse("[href!=test]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValueNot);
    }
    
    @Test(timeout = 4000)
    public void testParseAttributeStartsWith() {
        Evaluator eval = QueryParser.parse("[href^=http]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValueStarting);
    }
    
    @Test(timeout = 4000)
    public void testParseAttributeEndsWith() {
        Evaluator eval = QueryParser.parse("[href$=.pdf]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValueEnding);
    }
    
    @Test(timeout = 4000)
    public void testParseAttributeContains() {
        Evaluator eval = QueryParser.parse("[href*=example]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValueContaining);
    }
    
    @Test(timeout = 4000)
    public void testParseAttributeMatching() {
        Evaluator eval = QueryParser.parse("[href~=regex]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValueMatching);
    }
    
    @Test(timeout = 4000)
    public void testParseAttributeNoValue() {
        Evaluator eval = QueryParser.parse("[disabled]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Attribute);
    }
    
    @Test(timeout = 4000)
    public void testParseAttributeStarting() {
        Evaluator eval = QueryParser.parse("[^data-]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeStarting);
    }
    
    /* ==================== PART B: Boundary Value Analysis ==================== */
    
    @Test(timeout = 4000)
    public void testParseEmptyQuery() {
        try {
            QueryParser.parse("");
            fail("Should throw exception for empty query");
        } catch (Selector.SelectorParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testParseNullQuery() {
        try {
            QueryParser.parse(null);
            fail("Should throw exception for null query");
        } catch (Exception e) {
            // expected - NullPointerException or SelectorParseException
        }
    }
    
    @Test(timeout = 4000)
    public void testParseWhitespaceOnly() {
        try {
            QueryParser.parse("   ");
            fail("Should throw exception for whitespace-only query");
        } catch (Selector.SelectorParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testParseSingleCharacterTag() {
        Evaluator eval = QueryParser.parse("a");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Tag);
    }
    
    @Test(timeout = 4000)
    public void testParseNumericTag() {
        Evaluator eval = QueryParser.parse("h1");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Tag);
    }
    
    @Test(timeout = 4000)
    public void testParseTagWithNamespace() {
        Evaluator eval = QueryParser.parse("svg|circle");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }
    
    @Test(timeout = 4000)
    public void testParseWildcardNamespace() {
        Evaluator eval = QueryParser.parse("*|div");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }
    
    @Test(timeout = 4000)
    public void testParseIndexLessThan() {
        Evaluator eval = QueryParser.parse(":lt(3)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IndexLessThan);
    }
    
    @Test(timeout = 4000)
    public void testParseIndexGreaterThan() {
        Evaluator eval = QueryParser.parse(":gt(3)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IndexGreaterThan);
    }
    
    @Test(timeout = 4000)
    public void testParseIndexEquals() {
        Evaluator eval = QueryParser.parse(":eq(3)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IndexEquals);
    }
    
    @Test(timeout = 4000)
    public void testParseIndexZero() {
        Evaluator eval = QueryParser.parse(":eq(0)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IndexEquals);
    }
    
    @Test(timeout = 4000)
    public void testParseIndexNegative() {
        Evaluator eval = QueryParser.parse(":lt(-1)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IndexLessThan);
    }
    
    @Test(timeout = 4000)
    public void testParseIndexNonNumeric() {
        try {
            QueryParser.parse(":lt(abc)");
            fail("Should throw exception for non-numeric index");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    /* ==================== PART C: Defect-Targeted Tests ==================== */
    
    /**
     * Defect 1: testParsesSingleQuoteInContains
     * Expected: SelectorParseException when :contains() has single quote
     * Bug: The parser doesn't handle single quotes in :contains() properly
     */
    @Test(timeout = 4000)
    public void testParsesSingleQuoteInContains() {
        try {
            QueryParser.parse(":contains(It's a test)");
            fail("Should throw SelectorParseException for single quote in :contains()");
        } catch (Selector.SelectorParseException e) {
            // expected - this is the correct behavior
        }
    }
    
    /**
     * Defect 2: exceptionOnUncloseAttribute
     * Expected: SelectorParseException for unclosed attribute
     * Bug: Throws IllegalArgumentException instead of SelectorParseException
     */
    @Test(timeout = 4000)
    public void testExceptionOnUnclosedAttribute() {
        try {
            QueryParser.parse("[href=test");
            fail("Should throw SelectorParseException for unclosed attribute");
        } catch (Selector.SelectorParseException e) {
            // expected - this is the correct behavior
        } catch (IllegalArgumentException e) {
            fail("Should throw SelectorParseException, not IllegalArgumentException");
        }
    }
    
    @Test(timeout = 4000)
    public void testUnclosedPseudoClass() {
        try {
            QueryParser.parse(":contains(test");
            fail("Should throw exception for unclosed pseudo-class");
        } catch (Selector.SelectorParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testInvalidPseudoClass() {
        try {
            QueryParser.parse(":invalidPseudo");
            fail("Should throw exception for invalid pseudo-class");
        } catch (Selector.SelectorParseException e) {
            // expected
        }
    }
    
    /* ==================== PART D: Exception & Defensive Guard Paths ==================== */
    
    @Test(timeout = 4000)
    public void testEmptyHasSubselect() {
        try {
            QueryParser.parse(":has()");
            fail("Should throw exception for empty :has() subselect");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testEmptyContainsText() {
        try {
            QueryParser.parse(":contains()");
            fail("Should throw exception for empty :contains() text");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testEmptyContainsOwnText() {
        try {
            QueryParser.parse(":containsOwn()");
            fail("Should throw exception for empty :containsOwn() text");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testEmptyContainsDataText() {
        try {
            QueryParser.parse(":containsData()");
            fail("Should throw exception for empty :containsData() text");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testEmptyMatchesRegex() {
        try {
            QueryParser.parse(":matches()");
            fail("Should throw exception for empty :matches() regex");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testEmptyNotSubselect() {
        try {
            QueryParser.parse(":not()");
            fail("Should throw exception for empty :not() subselect");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testInvalidAttributeOperator() {
        try {
            QueryParser.parse("[href|=test]");
            fail("Should throw exception for invalid attribute operator");
        } catch (Selector.SelectorParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testEmptyAttributeKey() {
        try {
            QueryParser.parse("[]");
            fail("Should throw exception for empty attribute key");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    /* ==================== PART E: Pseudo-classes ==================== */
    
    @Test(timeout = 4000)
    public void testParseHas() {
        Evaluator eval = QueryParser.parse(":has(div)");
        assertNotNull(eval);
        assertTrue(eval instanceof StructuralEvaluator.Has);
    }
    
    @Test(timeout = 4000)
    public void testParseContains() {
        Evaluator eval = QueryParser.parse(":contains(hello)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.ContainsText);
    }
    
    @Test(timeout = 4000)
    public void testParseContainsOwn() {
        Evaluator eval = QueryParser.parse(":containsOwn(hello)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.ContainsOwnText);
    }
    
    @Test(timeout = 4000)
    public void testParseContainsData() {
        Evaluator eval = QueryParser.parse(":containsData(data)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.ContainsData);
    }
    
    @Test(timeout = 4000)
    public void testParseMatches() {
        Evaluator eval = QueryParser.parse(":matches(regex)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Matches);
    }
    
    @Test(timeout = 4000)
    public void testParseMatchesOwn() {
        Evaluator eval = QueryParser.parse(":matchesOwn(regex)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.MatchesOwn);
    }
    
    @Test(timeout = 4000)
    public void testParseNot() {
        Evaluator eval = QueryParser.parse(":not(div)");
        assertNotNull(eval);
        assertTrue(eval instanceof StructuralEvaluator.Not);
    }
    
    @Test(timeout = 4000)
    public void testParseFirstChild() {
        Evaluator eval = QueryParser.parse(":first-child");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsFirstChild);
    }
    
    @Test(timeout = 4000)
    public void testParseLastChild() {
        Evaluator eval = QueryParser.parse(":last-child");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsLastChild);
    }
    
    @Test(timeout = 4000)
    public void testParseFirstOfType() {
        Evaluator eval = QueryParser.parse(":first-of-type");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsFirstOfType);
    }
    
    @Test(timeout = 4000)
    public void testParseLastOfType() {
        Evaluator eval = QueryParser.parse(":last-of-type");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsLastOfType);
    }
    
    @Test(timeout = 4000)
    public void testParseOnlyChild() {
        Evaluator eval = QueryParser.parse(":only-child");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsOnlyChild);
    }
    
    @Test(timeout = 4000)
    public void testParseOnlyOfType() {
        Evaluator eval = QueryParser.parse(":only-of-type");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsOnlyOfType);
    }
    
    @Test(timeout = 4000)
    public void testParseEmpty() {
        Evaluator eval = QueryParser.parse(":empty");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsEmpty);
    }
    
    @Test(timeout = 4000)
    public void testParseRoot() {
        Evaluator eval = QueryParser.parse(":root");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsRoot);
    }
    
    @Test(timeout = 4000)
    public void testParseNthChild() {
        Evaluator eval = QueryParser.parse(":nth-child(2)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthChild);
    }
    
    @Test(timeout = 4000)
    public void testParseNthLastChild() {
        Evaluator eval = QueryParser.parse(":nth-last-child(2)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthLastChild);
    }
    
    @Test(timeout = 4000)
    public void testParseNthOfType() {
        Evaluator eval = QueryParser.parse(":nth-of-type(2)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthOfType);
    }
    
    @Test(timeout = 4000)
    public void testParseNthLastOfType() {
        Evaluator eval = QueryParser.parse(":nth-last-of-type(2)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthLastOfType);
    }
    
    @Test(timeout = 4000)
    public void testParseNthChildOdd() {
        Evaluator eval = QueryParser.parse(":nth-child(odd)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthChild);
    }
    
    @Test(timeout = 4000)
    public void testParseNthChildEven() {
        Evaluator eval = QueryParser.parse(":nth-child(even)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthChild);
    }
    
    @Test(timeout = 4000)
    public void testParseNthChildAnB() {
        Evaluator eval = QueryParser.parse(":nth-child(2n+1)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthChild);
    }
    
    @Test(timeout = 4000)
    public void testParseNthChildInvalid() {
        try {
            QueryParser.parse(":nth-child(invalid)");
            fail("Should throw exception for invalid nth-child");
        } catch (Selector.SelectorParseException e) {
            // expected
        }
    }
    
    /* ==================== Complex Combinations ==================== */
    
    @Test(timeout = 4000)
    public void testParseComplexSelector() {
        Evaluator eval = QueryParser.parse("div#id.class[attr=val]:first-child");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }
    
    @Test(timeout = 4000)
    public void testParseNestedPseudoClasses() {
        Evaluator eval = QueryParser.parse(":not(:has(div))");
        assertNotNull(eval);
        assertTrue(eval instanceof StructuralEvaluator.Not);
    }
    
    @Test(timeout = 4000)
    public void testParseMultipleCombinators() {
        Evaluator eval = QueryParser.parse("div > p + span ~ a");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }
    
    @Test(timeout = 4000)
    public void testParseLeadingCombinator() {
        Evaluator eval = QueryParser.parse("> div");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }
    
    @Test(timeout = 4000)
    public void testParseEscapedCharacters() {
        Evaluator eval = QueryParser.parse("#my\\:id");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Id);
    }
    
    @Test(timeout = 4000)
    public void testParseCaseInsensitivePseudo() {
        Evaluator eval = QueryParser.parse(":FIRST-CHILD");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsFirstChild);
    }
    
    @Test(timeout = 4000)
    public void testParseMultipleClasses() {
        Evaluator eval = QueryParser.parse(".class1.class2");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }
    
    @Test(timeout = 4000)
    public void testParseIdWithUnderscore() {
        Evaluator eval = QueryParser.parse("#my_id");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Id);
    }
    
    @Test(timeout = 4000)
    public void testParseClassWithHyphen() {
        Evaluator eval = QueryParser.parse(".my-class");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Class);
    }
    
    @Test(timeout = 4000)
    public void testParseAttributeWithHyphen() {
        Evaluator eval = QueryParser.parse("[data-test=value]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValue);
    }
}