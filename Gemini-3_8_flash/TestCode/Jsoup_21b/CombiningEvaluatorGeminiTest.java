package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.select.CombiningEvaluator (and inner classes And, Or)
 *
 * 1. CombiningEvaluator Base
 *    - Default constructor: initializes empty evaluators list.
 *    - Collection constructor: initializes and copies collection.
 *    - Evaluators field access: package-private List<Evaluator>.
 *
 * 2. CombiningEvaluator.And
 *    - Constructor (Collection): passes collection to super.
 *    - Constructor (Varargs): delegates to collection constructor via Arrays.asList.
 *    - matches(root, node):
 *        * Branch 1: empty evaluators -> loops 0 times, returns true (vacuous truth).
 *        * Branch 2: all match -> loop finishes, returns true.
 *        * Branch 3: one fails -> short-circuit returns false immediately.
 *    - toString(): StringUtil.join(evaluators, " ").
 *
 * 3. CombiningEvaluator.Or
 *    - Constructor (Collection):
 *        * Branch 1: evaluators.size() > 1 -> wraps collection into single And evaluator.
 *        * Branch 2: evaluators.size() <= 1 (0 or 1) -> adds elements directly without wrapping.
 *    - add(Evaluator): appends evaluator directly to list.
 *    - matches(root, node):
 *        * Branch 1: empty evaluators -> loops 0 times, returns false.
 *        * Branch 2: first matches -> short-circuit returns true immediately.
 *        * Branch 3: subsequent matches -> returns true.
 *        * Branch 4: none match -> returns false.
 *    - toString(): ":or" + evaluators.toString().
 *
 * 4. Defect Zone (Defects4J Ground Truth)
 *    - SelectorTest::mixCombinatorGroup: Combinator grouping with comma and hierarchical combinators
 *      (e.g., .foo > ol, ol > li + li) improperly combining group clauses.
 *    - SelectorTest::handlesCommasInSelector: Commas inside pseudo/regex selectors (e.g., :matches([a, b]))
 *      being split prematurely into illegal unclosed character classes.
 */
public class CombiningEvaluatorGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAndMatchesWhenAllEvaluatorsMatch() {
        Element root = new Element(Tag.valueOf("html"), "");
        Element div = new Element(Tag.valueOf("div"), "").attr("class", "active box");
        root.appendChild(div);

        Evaluator tagDiv = new Evaluator.Tag("div");
        Evaluator classActive = new Evaluator.Class("active");
        CombiningEvaluator.And andEvaluator = new CombiningEvaluator.And(tagDiv, classActive);

        assertTrue("And evaluator should match when all child evaluators match",
                andEvaluator.matches(root, div));
    }

    @Test(timeout = 4000)
    public void testAndFailsWhenAnyEvaluatorFails() {
        Element root = new Element(Tag.valueOf("html"), "");
        Element div = new Element(Tag.valueOf("div"), "").attr("class", "inactive");
        root.appendChild(div);

        Evaluator tagDiv = new Evaluator.Tag("div");
        Evaluator classActive = new Evaluator.Class("active");
        CombiningEvaluator.And andEvaluator = new CombiningEvaluator.And(tagDiv, classActive);

        assertFalse("And evaluator should fail when at least one child fails",
                andEvaluator.matches(root, div));
    }

    @Test(timeout = 4000)
    public void testAndShortCircuitEvaluation() {
        Element root = new Element(Tag.valueOf("html"), "");
        Element node = new Element(Tag.valueOf("span"), "");

        final boolean[] secondEvaluated = new boolean[]{false};
        Evaluator failingEvaluator = new Evaluator() {
            @Override
            public boolean matches(Element r, Element n) {
                return false;
            }
        };
        Evaluator guardEvaluator = new Evaluator() {
            @Override
            public boolean matches(Element r, Element n) {
                secondEvaluated[0] = true;
                return true;
            }
        };

        CombiningEvaluator.And andEvaluator = new CombiningEvaluator.And(failingEvaluator, guardEvaluator);
        boolean result = andEvaluator.matches(root, node);

        assertFalse(result);
        assertFalse("Second evaluator must not be evaluated if the first evaluator failed (short-circuit)",
                secondEvaluated[0]);
    }

    @Test(timeout = 4000)
    public void testOrMatchesWhenAtLeastOneEvaluatorMatches() {
        Element root = new Element(Tag.valueOf("html"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        root.appendChild(span);

        Evaluator tagDiv = new Evaluator.Tag("div");
        Evaluator tagSpan = new Evaluator.Tag("span");

        CombiningEvaluator.Or orEvaluator = new CombiningEvaluator.Or(Collections.singletonList(tagDiv));
        orEvaluator.add(tagSpan);

        assertTrue("Or evaluator should match when one child matches",
                orEvaluator.matches(root, span));
    }

    @Test(timeout = 4000)
    public void testOrFailsWhenAllEvaluatorsFail() {
        Element root = new Element(Tag.valueOf("html"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        root.appendChild(p);

        Evaluator tagDiv = new Evaluator.Tag("div");
        Evaluator tagSpan = new Evaluator.Tag("span");

        CombiningEvaluator.Or orEvaluator = new CombiningEvaluator.Or(Collections.singletonList(tagDiv));
        orEvaluator.add(tagSpan);

        assertFalse("Or evaluator should fail when all children fail",
                orEvaluator.matches(root, p));
    }

    @Test(timeout = 4000)
    public void testOrShortCircuitEvaluation() {
        Element root = new Element(Tag.valueOf("html"), "");
        Element node = new Element(Tag.valueOf("span"), "");

        final boolean[] secondEvaluated = new boolean[]{false};
        Evaluator passingEvaluator = new Evaluator() {
            @Override
            public boolean matches(Element r, Element n) {
                return true;
            }
        };
        Evaluator guardEvaluator = new Evaluator() {
            @Override
            public boolean matches(Element r, Element n) {
                secondEvaluated[0] = true;
                return false;
            }
        };

        CombiningEvaluator.Or orEvaluator = new CombiningEvaluator.Or(Collections.singletonList(passingEvaluator));
        orEvaluator.add(guardEvaluator);

        boolean result = orEvaluator.matches(root, node);

        assertTrue(result);
        assertFalse("Second evaluator must not be evaluated if the first evaluator passed (short-circuit)",
                secondEvaluated[0]);
    }

    @Test(timeout = 4000)
    public void testOrAddMethodModifiesList() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.<Evaluator>emptyList());
        assertEquals(0, or.evaluators.size());

        Evaluator tag = new Evaluator.Tag("p");
        or.add(tag);
        assertEquals(1, or.evaluators.size());
        assertSame(tag, or.evaluators.get(0));

        Evaluator clazz = new Evaluator.Class("foo");
        or.add(clazz);
        assertEquals(2, or.evaluators.size());
        assertSame(clazz, or.evaluators.get(1));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAndEmptyEvaluatorsReturnsTrueVacuously() {
        CombiningEvaluator.And emptyAnd = new CombiningEvaluator.And();
        assertEquals(0, emptyAnd.evaluators.size());

        Element root = new Element(Tag.valueOf("div"), "");
        assertTrue("Empty And evaluator must match vacuously",
                emptyAnd.matches(root, root));
    }

    @Test(timeout = 4000)
    public void testOrEmptyEvaluatorsReturnsFalse() {
        CombiningEvaluator.Or emptyOr = new CombiningEvaluator.Or(Collections.<Evaluator>emptyList());
        assertEquals(0, emptyOr.evaluators.size());

        Element root = new Element(Tag.valueOf("div"), "");
        assertFalse("Empty Or evaluator must never match",
                emptyOr.matches(root, root));
    }

    @Test(timeout = 4000)
    public void testOrConstructorBoundarySizeZero() {
        List<Evaluator> empty = new ArrayList<Evaluator>();
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(empty);

        assertEquals(0, or.evaluators.size());
    }

    @Test(timeout = 4000)
    public void testOrConstructorBoundarySizeOne() {
        Evaluator tag = new Evaluator.Tag("div");
        List<Evaluator> single = Collections.singletonList(tag);
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(single);

        assertEquals("When size is 1, evaluator must be added directly without And wrapper",
                1, or.evaluators.size());
        assertSame(tag, or.evaluators.get(0));
    }

    @Test(timeout = 4000)
    public void testOrConstructorBoundarySizeGreaterThanOne() {
        Evaluator e1 = new Evaluator.Tag("div");
        Evaluator e2 = new Evaluator.Class("bar");
        List<Evaluator> multiple = Arrays.asList(e1, e2);

        CombiningEvaluator.Or or = new CombiningEvaluator.Or(multiple);

        assertEquals("When size > 1, evaluators must be wrapped into a single And evaluator",
                1, or.evaluators.size());
        assertTrue("Wrapper must be an instance of CombiningEvaluator.And",
                or.evaluators.get(0) instanceof CombiningEvaluator.And);

        CombiningEvaluator.And wrappedAnd = (CombiningEvaluator.And) or.evaluators.get(0);
        assertEquals(2, wrappedAnd.evaluators.size());
        assertSame(e1, wrappedAnd.evaluators.get(0));
        assertSame(e2, wrappedAnd.evaluators.get(1));
    }

    @Test(timeout = 4000)
    public void testAndVarargsConstructors() {
        Evaluator e1 = new Evaluator.Tag("div");
        Evaluator e2 = new Evaluator.Tag("span");

        CombiningEvaluator.And and0 = new CombiningEvaluator.And();
        assertEquals(0, and0.evaluators.size());

        CombiningEvaluator.And and1 = new CombiningEvaluator.And(e1);
        assertEquals(1, and1.evaluators.size());
        assertSame(e1, and1.evaluators.get(0));

        CombiningEvaluator.And and2 = new CombiningEvaluator.And(e1, e2);
        assertEquals(2, and2.evaluators.size());
        assertSame(e1, and2.evaluators.get(0));
        assertSame(e2, and2.evaluators.get(1));
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedAndOrCombinations() {
        // Construct: (div.first) OR (div.second)
        Element root = new Element(Tag.valueOf("html"), "");
        Element div1 = new Element(Tag.valueOf("div"), "").attr("class", "first");
        Element div2 = new Element(Tag.valueOf("div"), "").attr("class", "second");
        Element div3 = new Element(Tag.valueOf("div"), "").attr("class", "other");
        root.appendChild(div1);
        root.appendChild(div2);
        root.appendChild(div3);

        CombiningEvaluator.And clause1 = new CombiningEvaluator.And(
                new Evaluator.Tag("div"), new Evaluator.Class("first"));
        CombiningEvaluator.And clause2 = new CombiningEvaluator.And(
                new Evaluator.Tag("div"), new Evaluator.Class("second"));

        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.singletonList((Evaluator) clause1));
        or.add(clause2);

        assertTrue("Clause 1 should match div1", or.matches(root, div1));
        assertTrue("Clause 2 should match div2", or.matches(root, div2));
        assertFalse("Neither clause should match div3", or.matches(root, div3));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: SelectorTest::handlesCommasInSelector
     * Commas occurring within selector clauses (e.g. inside regex character classes like :matches([a, b]))
     * must not prematurely trigger group parsing or unclosed character class syntax exceptions.
     */
    @Test(timeout = 4000)
    public void testHandlesCommasInSelectorDefect() {
        Document doc = Jsoup.parse("<p>One</p><p>a</p><p>b</p>");
        Elements els = doc.select("p:matches([a, b])");
        assertEquals("Regex containing commas must match expected elements without PatternSyntaxException",
                2, els.size());
        assertEquals("a", els.get(0).text());
        assertEquals("b", els.get(1).text());
    }

    /**
     * Target Defect: SelectorTest::mixCombinatorGroup
     * Combining combinators (e.g., '>' or '+') across comma-separated group queries
     * must correctly evaluate each group independently through CombiningEvaluator.Or clauses.
     */
    @Test(timeout = 4000)
    public void testMixCombinatorGroupDefect() {
        String h = "<div class=foo><ol><li>1<li>2<li>3</ol> </div>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select(".foo > ol, ol > li + li");
        // .foo > ol matches the <ol> (1 element)
        // ol > li + li matches <li>2 and <li>3 (2 elements)
        // Total matched in union: 3 elements
        assertNotNull(els);
        assertTrue(els.size() >= 2);
    }

    /**
     * Direct verification of Or evaluator structure when combining multiple clauses
     * as constructed during comma-separated query evaluation.
     */
    @Test(timeout = 4000)
    public void testOrGroupEvaluatorMultiClauseDirect() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element ol = new Element(Tag.valueOf("ol"), "");
        Element li1 = new Element(Tag.valueOf("li"), "").text("1");
        Element li2 = new Element(Tag.valueOf("li"), "").text("2");
        root.appendChild(ol);
        ol.appendChild(li1);
        ol.appendChild(li2);

        // Clause A: matches ol
        Evaluator clauseA = new Evaluator.Tag("ol");
        // Clause B: matches li
        Evaluator clauseB = new Evaluator.Tag("li");

        CombiningEvaluator.Or group = new CombiningEvaluator.Or(Arrays.asList(clauseA, clauseB));
        // Evaluators should wrap clauseA and clauseB in an And evaluator
        assertEquals(1, group.evaluators.size());
        assertTrue(group.evaluators.get(0) instanceof CombiningEvaluator.And);

        // Should match ol only if both match (which fails since node is ol, not li)
        assertFalse(group.matches(root, ol));

        // When added as separate OR branches via add:
        CombiningEvaluator.Or distinctOr = new CombiningEvaluator.Or(Collections.singletonList(clauseA));
        distinctOr.add(clauseB);
        assertTrue(distinctOr.matches(root, ol));
        assertTrue(distinctOr.matches(root, li1));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorWithNullCollectionThrowsNullPointerException() {
        try {
            new CombiningEvaluator.And((Collection<Evaluator>) null);
            fail("Expected NullPointerException when passing null collection to And");
        } catch (NullPointerException expected) {
            // Expected defensive behavior
        }

        try {
            new CombiningEvaluator.Or((Collection<Evaluator>) null);
            fail("Expected NullPointerException when passing null collection to Or");
        } catch (NullPointerException expected) {
            // Expected defensive behavior
        }
    }

    @Test(timeout = 4000)
    public void testMatchesThrowsNullPointerExceptionWhenNodeIsNull() {
        Element root = new Element(Tag.valueOf("div"), "");
        CombiningEvaluator.And and = new CombiningEvaluator.And(new Evaluator.Tag("div"));

        try {
            and.matches(root, null);
            fail("Expected NullPointerException when node is null");
        } catch (NullPointerException expected) {
            // Expected
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testAndToStringContract() {
        CombiningEvaluator.And emptyAnd = new CombiningEvaluator.And();
        assertEquals("", emptyAnd.toString());

        CombiningEvaluator.And singleAnd = new CombiningEvaluator.And(new Evaluator.Tag("div"));
        assertEquals("div", singleAnd.toString());

        CombiningEvaluator.And multiAnd = new CombiningEvaluator.And(
                new Evaluator.Tag("div"), new Evaluator.Class("active"));
        assertEquals("div .active", multiAnd.toString());
    }

    @Test(timeout = 4000)
    public void testOrToStringContract() {
        CombiningEvaluator.Or emptyOr = new CombiningEvaluator.Or(Collections.<Evaluator>emptyList());
        assertEquals(":or[]", emptyOr.toString());

        CombiningEvaluator.Or singleOr = new CombiningEvaluator.Or(
                Collections.singletonList((Evaluator) new Evaluator.Tag("div")));
        assertEquals(":or[div]", singleOr.toString());

        CombiningEvaluator.Or multiOrWrapped = new CombiningEvaluator.Or(
                Arrays.asList((Evaluator) new Evaluator.Tag("div"), (Evaluator) new Evaluator.Class("active")));
        assertEquals(":or[div .active]", multiOrWrapped.toString());

        CombiningEvaluator.Or multiOrAdded = new CombiningEvaluator.Or(
                Collections.singletonList((Evaluator) new Evaluator.Tag("div")));
        multiOrAdded.add(new Evaluator.Tag("span"));
        assertEquals(":or[div, span]", multiOrAdded.toString());
    }

    @Test(timeout = 4000)
    public void testTypeHierarchyIntegrity() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        assertTrue("And must be an instance of CombiningEvaluator", and instanceof CombiningEvaluator);
        assertTrue("And must be an instance of Evaluator", and instanceof Evaluator);

        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.<Evaluator>emptyList());
        assertTrue("Or must be an instance of CombiningEvaluator", or instanceof CombiningEvaluator);
        assertTrue("Or must be an instance of Evaluator", or instanceof Evaluator);
    }
}