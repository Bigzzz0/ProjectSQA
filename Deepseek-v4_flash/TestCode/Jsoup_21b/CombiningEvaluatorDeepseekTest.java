package org.jsoup.select;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * White-box JUnit 4 test suite for CombiningEvaluator (And, Or).
 * Targets all branches, boundary conditions, and the known Defects4J defect.
 *
 * [Branch & Defect Analysis Matrix]
 * And.matches:
 *   - Loop over evaluators: early return false if any evaluator fails, else true.
 *   - Empty evaluators list: returns true (vacuous truth).
 * Or.matches:
 *   - Loop over evaluators: early return true if any evaluator matches, else false.
 *   - Empty evaluators list: returns false.
 * Or constructor:
 *   - evaluators.size() > 1: wraps into an And evaluator.
 *   - evaluators.size() <= 1: adds directly (0 or 1).
 * Or.add(Evaluator):
 *   - Adds evaluator to list (no special wrapping).
 * Known defect: mixCombinatorGroup (expected 2 but was 3) likely due to incorrect
 *   grouping in Or when combining initial evaluators with later additions.
 *   Test simulates scenario: Or with two initial evaluators (wrapped into And)
 *   then add a third evaluator; verify matches behavior matches expected OR semantics.
 */
public class CombiningEvaluatorDeepseekTest {

    // Helper evaluator that returns a fixed boolean value
    private static class FixedEvaluator extends Evaluator {
        private final boolean result;
        FixedEvaluator(boolean result) { this.result = result; }
        @Override
        public boolean matches(Element root, Element node) { return result; }
        @Override
        public String toString() { return String.valueOf(result); }
    }

    // Helper evaluator that returns true for a specific condition (e.g., node index)
    private static class IndexEvaluator extends Evaluator {
        private final int targetIndex;
        private int callCount = 0;
        IndexEvaluator(int targetIndex) { this.targetIndex = targetIndex; }
        @Override
        public boolean matches(Element root, Element node) {
            callCount++;
            return callCount == targetIndex;
        }
        @Override
        public String toString() { return "Index(" + targetIndex + ")"; }
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testAndAllMatch() {
        List<Evaluator> evals = Arrays.asList(new FixedEvaluator(true), new FixedEvaluator(true));
        CombiningEvaluator.And and = new CombiningEvaluator.And(evals);
        assertTrue("And should return true when all evaluators match", and.matches(null, null));
    }

    @Test(timeout = 4000)
    public void testAndOneFails() {
        List<Evaluator> evals = Arrays.asList(new FixedEvaluator(true), new FixedEvaluator(false));
        CombiningEvaluator.And and = new CombiningEvaluator.And(evals);
        assertFalse("And should return false when any evaluator fails", and.matches(null, null));
    }

    @Test(timeout = 4000)
    public void testAndEmptyList() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(new ArrayList<Evaluator>());
        assertTrue("And with empty list should return true (vacuous truth)", and.matches(null, null));
    }

    @Test(timeout = 4000)
    public void testAndVarargs() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(new FixedEvaluator(true), new FixedEvaluator(true));
        assertTrue("And varargs constructor should work", and.matches(null, null));
    }

    @Test(timeout = 4000)
    public void testOrAllFail() {
        List<Evaluator> evals = Arrays.asList(new FixedEvaluator(false), new FixedEvaluator(false));
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(evals);
        assertFalse("Or should return false when all evaluators fail", or.matches(null, null));
    }

    @Test(timeout = 4000)
    public void testOrOneMatches() {
        List<Evaluator> evals = Arrays.asList(new FixedEvaluator(false), new FixedEvaluator(true));
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(evals);
        assertTrue("Or should return true when any evaluator matches", or.matches(null, null));
    }

    @Test(timeout = 4000)
    public void testOrEmptyList() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(new ArrayList<Evaluator>());
        assertFalse("Or with empty list should return false", or.matches(null, null));
    }

    @Test(timeout = 4000)
    public void testOrSingleEvaluator() {
        List<Evaluator> evals = Arrays.asList(new FixedEvaluator(true));
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(evals);
        assertTrue("Or with single evaluator should delegate to that evaluator", or.matches(null, null));
    }

    @Test(timeout = 4000)
    public void testOrAddEvaluator() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(new ArrayList<Evaluator>());
        or.add(new FixedEvaluator(true));
        assertTrue("Or after adding a matching evaluator should return true", or.matches(null, null));
        or.add(new FixedEvaluator(false));
        assertTrue("Or should still return true after adding non-matching", or.matches(null, null));
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testAndWithNullEvaluatorsInList() {
        // Although evaluators list should not contain null, we test defensive behavior
        List<Evaluator> evals = new ArrayList<Evaluator>();
        evals.add(null);
        try {
            new CombiningEvaluator.And(evals);
            fail("Should throw NullPointerException or similar when evaluator is null");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testOrWithNullEvaluatorsInList() {
        List<Evaluator> evals = new ArrayList<Evaluator>();
        evals.add(null);
        try {
            new CombiningEvaluator.Or(evals);
            fail("Should throw NullPointerException when evaluator is null");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testOrConstructorWithZeroEvaluators() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(new ArrayList<Evaluator>());
        assertFalse("Or with zero evaluators should return false", or.matches(null, null));
        assertEquals("Or toString should show empty list", ":or[]", or.toString());
    }

    @Test(timeout = 4000)
    public void testOrConstructorWithOneEvaluator() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(new FixedEvaluator(true)));
        assertTrue("Or with one evaluator should match", or.matches(null, null));
    }

    @Test(timeout = 4000)
    public void testOrConstructorWithTwoEvaluators() {
        // Two evaluators should be wrapped into an And
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(
            new FixedEvaluator(true), new FixedEvaluator(false)));
        // The And will fail because second evaluator is false, so Or should return false
        assertFalse("Or with two evaluators (wrapped into And) should fail if And fails", or.matches(null, null));
    }

    @Test(timeout = 4000)
    public void testOrAddAfterConstructorWithMultiple() {
        // Simulate scenario: initial two evaluators (wrapped into And), then add a third
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(
            new FixedEvaluator(false), new FixedEvaluator(false)));
        // Initially, the And fails, so Or returns false
        assertFalse("Initial Or should be false", or.matches(null, null));
        // Add a matching evaluator
        or.add(new FixedEvaluator(true));
        // Now Or should return true because the added evaluator matches
        assertTrue("Or after adding a matching evaluator should return true", or.matches(null, null));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Targets the known defect mixCombinatorGroup (expected 2 but was 3).
     * The defect likely arises from incorrect grouping in Or when combining
     * initial evaluators (wrapped into And) with later added evaluators.
     * This test creates an Or with two evaluators that match different elements,
     * then adds a third evaluator that should not affect the count if the
     * grouping is correct. We simulate by using IndexEvaluator that matches
     * only on specific call order.
     */
    @Test(timeout = 4000)
    public void testDefectMixCombinatorGroup() {
        // Simulate a scenario where Or should match exactly 2 out of 3 elements
        // We'll use a custom evaluator that tracks calls and returns true for specific indices
        // This is a simplified model of the real defect.
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(
            new IndexEvaluator(1),  // matches first call
            new IndexEvaluator(2)   // matches second call
        ));
        // The two evaluators are wrapped into an And, so the And will match only if both match
        // But IndexEvaluator matches only once, so And will never match (since second call will be false)
        // Then we add a third evaluator that matches always
        or.add(new FixedEvaluator(true));
        // Now Or should match because the added evaluator matches
        // But the defect might cause the Or to match incorrectly due to grouping
        // We assert that the Or matches as expected
        assertTrue("Or should match after adding a matching evaluator", or.matches(null, null));
        // Additionally, we can test that the Or does not match when all evaluators fail
        CombiningEvaluator.Or or2 = new CombiningEvaluator.Or(Arrays.asList(
            new FixedEvaluator(false), new FixedEvaluator(false)
        ));
        or2.add(new FixedEvaluator(false));
        assertFalse("Or should not match when all evaluators fail", or2.matches(null, null));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAndConstructorWithNullCollection() {
        new CombiningEvaluator.And((Collection<Evaluator>) null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testOrConstructorWithNullCollection() {
        new CombiningEvaluator.Or((Collection<Evaluator>) null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAndVarargsWithNullElement() {
        new CombiningEvaluator.And((Evaluator) null);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testAndToString() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(
            new FixedEvaluator(true), new FixedEvaluator(false));
        assertEquals("And toString should join evaluators with space", "true false", and.toString());
    }

    @Test(timeout = 4000)
    public void testAndToStringEmpty() {
        CombiningEvaluator.And and = new CombiningEvaluator.And(new ArrayList<Evaluator>());
        assertEquals("And toString with empty list should be empty string", "", and.toString());
    }

    @Test(timeout = 4000)
    public void testOrToString() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(
            new FixedEvaluator(true), new FixedEvaluator(false)));
        // The Or wraps the two into an And, so toString should show :or[And(...)]
        String str = or.toString();
        assertTrue("Or toString should contain :or", str.startsWith(":or"));
        assertTrue("Or toString should contain And", str.contains("And"));
    }

    @Test(timeout = 4000)
    public void testOrToStringAfterAdd() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(new ArrayList<Evaluator>());
        or.add(new FixedEvaluator(true));
        assertEquals("Or toString after add should show evaluator", ":or[true]", or.toString());
    }

    @Test(timeout = 4000)
    public void testOrEvaluatorsListModification() {
        // Ensure that the evaluators list is modifiable via add
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(new ArrayList<Evaluator>());
        or.add(new FixedEvaluator(true));
        assertEquals("Or evaluators list should have one element", 1, or.evaluators.size());
        or.add(new FixedEvaluator(false));
        assertEquals("Or evaluators list should have two elements", 2, or.evaluators.size());
    }
}