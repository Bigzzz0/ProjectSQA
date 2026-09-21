package org.apache.commons.math3.optim;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for BaseOptimizer.
 * Targets all branches, boundary conditions, and the known defect
 * (incorrect counter reset/state management leading to assertion failures in subclasses).
 *
 * [Branch & Defect Analysis Matrix]
 * - Constructor: checker null/non-null, evaluations/iterations initial max=0.
 * - getMaxEvaluations/getEvaluations/getMaxIterations/getIterations: before/after optimize, after reset.
 * - getConvergenceChecker: returns stored checker.
 * - optimize: parseOptimizationData, resetCount, doOptimize.
 * - parseOptimizationData: MaxEval, MaxIter, other data, null array, retention of previous values.
 * - incrementEvaluationCount/incrementIterationCount: triggers callback when max reached.
 * - MaxEvalCallback/MaxIterCallback: throw TooManyEvaluationsException/TooManyIterationsException.
 * - Defect: counters not reset properly between optimize calls, or max not updated correctly.
 */
public class BaseOptimizerDeepseekTest {

    // Concrete stub for testing BaseOptimizer
    private static class TestOptimizer extends BaseOptimizer<Object> {
        private int evalIncrements = 0;
        private int iterIncrements = 0;

        protected TestOptimizer(ConvergenceChecker<Object> checker) {
            super(checker);
        }

        // Allow direct calls to protected methods for testing
        public void callIncrementEvaluationCount() {
            incrementEvaluationCount();
        }

        public void callIncrementIterationCount() {
            incrementIterationCount();
        }

        public void callParseOptimizationData(OptimizationData... optData) {
            parseOptimizationData(optData);
        }

        // Set how many increments doOptimize should perform
        public void setEvalIncrements(int n) { this.evalIncrements = n; }
        public void setIterIncrements(int n) { this.iterIncrements = n; }

        @Override
        protected Object doOptimize() {
            for (int i = 0; i < evalIncrements; i++) {
                incrementEvaluationCount();
            }
            for (int i = 0; i < iterIncrements; i++) {
                incrementIterationCount();
            }
            return new Object();
        }
    }

    // Helper to create a simple convergence checker (always returns false)
    private static ConvergenceChecker<Object> dummyChecker() {
        return new ConvergenceChecker<Object>() {
            public boolean converged(int iteration, Object previous, Object current) {
                return false;
            }
        };
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructorAndGetters() {
        ConvergenceChecker<Object> checker = dummyChecker();
        TestOptimizer opt = new TestOptimizer(checker);
        assertSame("ConvergenceChecker should be the one passed", checker, opt.getConvergenceChecker());
        assertEquals("Initial max evaluations should be 0", 0, opt.getMaxEvaluations());
        assertEquals("Initial evaluations count should be 0", 0, opt.getEvaluations());
        assertEquals("Initial max iterations should be 0", 0, opt.getMaxIterations());
        assertEquals("Initial iterations count should be 0", 0, opt.getIterations());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullChecker() {
        TestOptimizer opt = new TestOptimizer(null);
        assertNull("ConvergenceChecker should be null", opt.getConvergenceChecker());
    }

    @Test(timeout = 4000)
    public void testOptimizeResetsCounters() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        // Set max to allow increments
        opt.callParseOptimizationData(new MaxEval(100), new MaxIter(100));
        opt.setEvalIncrements(5);
        opt.setIterIncrements(3);
        opt.optimize();
        assertEquals("Evaluations after optimize should be 5", 5, opt.getEvaluations());
        assertEquals("Iterations after optimize should be 3", 3, opt.getIterations());

        // Second call should reset counters
        opt.setEvalIncrements(2);
        opt.setIterIncrements(1);
        opt.optimize();
        assertEquals("Evaluations after second optimize should be 2", 2, opt.getEvaluations());
        assertEquals("Iterations after second optimize should be 1", 1, opt.getIterations());
    }

    @Test(timeout = 4000)
    public void testGetMaxEvaluationsAndIterationsAfterParse() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        opt.callParseOptimizationData(new MaxEval(50), new MaxIter(30));
        assertEquals("Max evaluations should be 50", 50, opt.getMaxEvaluations());
        assertEquals("Max iterations should be 30", 30, opt.getMaxIterations());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testOptimizeWithZeroMaxEval() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        opt.callParseOptimizationData(new MaxEval(0), new MaxIter(10));
        opt.setEvalIncrements(1);
        try {
            opt.optimize();
            fail("Should have thrown TooManyEvaluationsException");
        } catch (TooManyEvaluationsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testOptimizeWithZeroMaxIter() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        opt.callParseOptimizationData(new MaxEval(10), new MaxIter(0));
        opt.setIterIncrements(1);
        try {
            opt.optimize();
            fail("Should have thrown TooManyIterationsException");
        } catch (TooManyIterationsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testOptimizeWithNoMaxEvalProvided() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        // Only set MaxIter, no MaxEval -> maxEval remains 0
        opt.callParseOptimizationData(new MaxIter(10));
        opt.setEvalIncrements(1);
        try {
            opt.optimize();
            fail("Should have thrown TooManyEvaluationsException because maxEval=0");
        } catch (TooManyEvaluationsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testOptimizeWithNoMaxIterProvided() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        opt.callParseOptimizationData(new MaxEval(10));
        opt.setIterIncrements(1);
        try {
            opt.optimize();
            fail("Should have thrown TooManyIterationsException because maxIter=0");
        } catch (TooManyIterationsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseOptimizationDataRetainsPreviousValues() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        opt.callParseOptimizationData(new MaxEval(100), new MaxIter(200));
        assertEquals(100, opt.getMaxEvaluations());
        assertEquals(200, opt.getMaxIterations());

        // Second call with only MaxEval -> MaxIter should retain previous value
        opt.callParseOptimizationData(new MaxEval(50));
        assertEquals(50, opt.getMaxEvaluations());
        assertEquals(200, opt.getMaxIterations());

        // Third call with only MaxIter -> MaxEval should retain previous value
        opt.callParseOptimizationData(new MaxIter(300));
        assertEquals(50, opt.getMaxEvaluations());
        assertEquals(300, opt.getMaxIterations());
    }

    @Test(timeout = 4000)
    public void testParseOptimizationDataWithEmptyArray() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        opt.callParseOptimizationData(new MaxEval(10));
        opt.callParseOptimizationData(); // empty
        assertEquals("Max evaluations should remain 10", 10, opt.getMaxEvaluations());
    }

    @Test(timeout = 4000)
    public void testParseOptimizationDataWithNullArray() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        opt.callParseOptimizationData(new MaxEval(10));
        // null array is not allowed by varargs, but we can test with null reference
        // Actually varargs can be null, but the method iterates over it, so it will throw NPE.
        // We'll test that it throws NullPointerException (defensive guard)
        try {
            opt.callParseOptimizationData((OptimizationData[]) null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========
    // The known defect manifests as incorrect iteration/evaluation counts after multiple optimize calls.
    // This test verifies that counters are properly reset and that the max counts are correctly applied.

    @Test(timeout = 4000)
    public void testDefectCountersResetBetweenOptimizeCalls() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        // First optimization
        opt.callParseOptimizationData(new MaxEval(10), new MaxIter(10));
        opt.setEvalIncrements(3);
        opt.setIterIncrements(2);
        opt.optimize();
        assertEquals(3, opt.getEvaluations());
        assertEquals(2, opt.getIterations());

        // Second optimization with different increments
        opt.setEvalIncrements(5);
        opt.setIterIncrements(4);
        opt.optimize();
        assertEquals("Evaluations should be 5 after second call", 5, opt.getEvaluations());
        assertEquals("Iterations should be 4 after second call", 4, opt.getIterations());

        // Third optimization with no increments (should reset to 0)
        opt.setEvalIncrements(0);
        opt.setIterIncrements(0);
        opt.optimize();
        assertEquals("Evaluations should be 0 after no increments", 0, opt.getEvaluations());
        assertEquals("Iterations should be 0 after no increments", 0, opt.getIterations());
    }

    @Test(timeout = 4000)
    public void testDefectMaxCountsAppliedBeforeOptimize() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        // Set max to 1, then try to increment twice
        opt.callParseOptimizationData(new MaxEval(1), new MaxIter(1));
        opt.setEvalIncrements(2);
        opt.setIterIncrements(0);
        try {
            opt.optimize();
            fail("Should have thrown TooManyEvaluationsException on second increment");
        } catch (TooManyEvaluationsException e) {
            // expected
        }
        // Verify that the count stopped at 1 (the first increment succeeded)
        assertEquals("Evaluations should be 1 before exception", 1, opt.getEvaluations());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = TooManyEvaluationsException.class)
    public void testIncrementEvaluationCountExceedsMax() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        opt.callParseOptimizationData(new MaxEval(0));
        opt.callIncrementEvaluationCount(); // should throw
    }

    @Test(timeout = 4000, expected = TooManyIterationsException.class)
    public void testIncrementIterationCountExceedsMax() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        opt.callParseOptimizationData(new MaxIter(0));
        opt.callIncrementIterationCount(); // should throw
    }

    @Test(timeout = 4000)
    public void testIncrementEvaluationCountWithinLimit() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        opt.callParseOptimizationData(new MaxEval(5));
        opt.callIncrementEvaluationCount();
        assertEquals(1, opt.getEvaluations());
    }

    @Test(timeout = 4000)
    public void testIncrementIterationCountWithinLimit() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        opt.callParseOptimizationData(new MaxIter(5));
        opt.callIncrementIterationCount();
        assertEquals(1, opt.getIterations());
    }

    @Test(timeout = 4000)
    public void testParseOptimizationDataIgnoresOtherData() {
        TestOptimizer opt = new TestOptimizer(dummyChecker());
        // Pass an unknown OptimizationData (we'll create a dummy)
        OptimizationData dummy = new OptimizationData() {};
        opt.callParseOptimizationData(new MaxEval(10), dummy);
        assertEquals(10, opt.getMaxEvaluations());
        // MaxIter should remain 0 (unchanged)
        assertEquals(0, opt.getMaxIterations());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    // No equals/hashCode/clone in BaseOptimizer, but we can test that the convergence checker is correctly stored.

    @Test(timeout = 4000)
    public void testConvergenceCheckerIdentity() {
        ConvergenceChecker<Object> checker = dummyChecker();
        TestOptimizer opt = new TestOptimizer(checker);
        assertSame(checker, opt.getConvergenceChecker());
    }

    @Test(timeout = 4000)
    public void testConvergenceCheckerNull() {
        TestOptimizer opt = new TestOptimizer(null);
        assertNull(opt.getConvergenceChecker());
    }
}