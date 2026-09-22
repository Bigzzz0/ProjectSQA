package org.apache.commons.math3.optim;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target: org.apache.commons.math3.optim.BaseOptimizer
 *
 * Decision / Branch Points Targeted:
 * 1. Constructor BaseOptimizer(ConvergenceChecker<PAIR> checker):
 *    - Initialization of evaluations and iterations counters with their respective callbacks.
 *    - Null vs non-null ConvergenceChecker reference handling.
 * 2. optimize(OptimizationData... optData):
 *    - Invocation of parseOptimizationData, counter resets, and execution of doOptimize.
 *    - Verification of counter zeroing across consecutive optimize calls.
 * 3. parseOptimizationData(OptimizationData... optData):
 *    - Branch: data instanceof MaxEval -> updates evaluations maximal count.
 *    - Branch: data instanceof MaxIter -> updates iterations maximal count.
 *    - Branch: data is null or unknown OptimizationData -> skipped without exception.
 *    - State Retention Contract: Data set in prior invocation is preserved if omitted in subsequent calls.
 * 4. incrementEvaluationCount():
 *    - Normal count increments.
 *    - Exceeding maximal evaluation count -> triggers MaxEvalCallback throwing TooManyEvaluationsException.
 * 5. incrementIterationCount():
 *    - Normal count increments.
 *    - Exceeding maximal iteration count -> triggers MaxIterCallback throwing TooManyIterationsException.
 *
 * Known Defect Target (Defects4J Math-6):
 * - Multiple optimization algorithms failed with AssertionFailedError asserting optimizer.getIterations() > 0.
 * - MaxIter is specified as optional optimization data. In the defective design, iterations maximal count
 *   initialized to 0 rather than Integer.MAX_VALUE. Algorithms attempting iteration tracking without an
 *   explicit MaxIter parameter either failed to count iterations or encountered premature termination.
 * -----------------------------------------------------------------------------------------
 */
public class BaseOptimizerGeminiTest {

    /**
     * Concrete test implementation of BaseOptimizer for white-box verification.
     */
    private static class ConcreteOptimizer extends BaseOptimizer<String> {
        private String returnVal = "OPTIMAL_SOLUTION";
        private int evalSteps = 0;
        private int iterSteps = 0;
        private CustomData customData;

        ConcreteOptimizer(ConvergenceChecker<String> checker) {
            super(checker);
        }

        void setSteps(int evalSteps, int iterSteps) {
            this.evalSteps = evalSteps;
            this.iterSteps = iterSteps;
        }

        void setReturnVal(String returnVal) {
            this.returnVal = returnVal;
        }

        CustomData getCustomData() {
            return customData;
        }

        @Override
        protected String doOptimize() {
            for (int i = 0; i < evalSteps; i++) {
                incrementEvaluationCount();
            }
            for (int i = 0; i < iterSteps; i++) {
                incrementIterationCount();
            }
            return returnVal;
        }

        @Override
        protected void parseOptimizationData(OptimizationData... optData) {
            super.parseOptimizationData(optData);
            for (OptimizationData data : optData) {
                if (data instanceof CustomData) {
                    this.customData = (CustomData) data;
                }
            }
        }
    }

    /**
     * Dummy OptimizationData implementation for verifying polymorphism and isolation.
     */
    private static class CustomData implements OptimizationData {
        private final String payload;

        CustomData(String payload) {
            this.payload = payload;
        }

        String getPayload() {
            return payload;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateAndGetters() {
        ConvergenceChecker<String> checker = new ConvergenceChecker<String>() {
            public boolean converged(int iteration, String previous, String current) {
                return false;
            }
        };

        ConcreteOptimizer optimizer = new ConcreteOptimizer(checker);

        assertSame("Checker must match the passed instance", checker, optimizer.getConvergenceChecker());
        assertEquals("Evaluations must start at 0 before optimize()", 0, optimizer.getEvaluations());
        assertEquals("Iterations must start at 0 before optimize()", 0, optimizer.getIterations());
        assertEquals("Default max evaluations must be 0 before config", 0, optimizer.getMaxEvaluations());
    }

    @Test(timeout = 4000)
    public void testOptimizeNormalExecutionAndCounting() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        optimizer.setSteps(7, 4);
        optimizer.setReturnVal("CONVERGED");

        String result = optimizer.optimize(new MaxEval(20), new MaxIter(10));

        assertEquals("CONVERGED", result);
        assertEquals("Exact evaluations executed must be 7", 7, optimizer.getEvaluations());
        assertEquals("Exact iterations executed must be 4", 4, optimizer.getIterations());
        assertEquals(20, optimizer.getMaxEvaluations());
        assertEquals(10, optimizer.getMaxIterations());
    }

    @Test(timeout = 4000)
    public void testCounterResetAcrossSuccessiveRuns() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);

        // Run 1
        optimizer.setSteps(5, 3);
        optimizer.optimize(new MaxEval(50), new MaxIter(50));
        assertEquals(5, optimizer.getEvaluations());
        assertEquals(3, optimizer.getIterations());

        // Run 2: Counters must reset rather than accumulate
        optimizer.setSteps(2, 1);
        optimizer.optimize();
        assertEquals("Evaluations must be reset to 0 before the run and end at 2", 2, optimizer.getEvaluations());
        assertEquals("Iterations must be reset to 0 before the run and end at 1", 1, optimizer.getIterations());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testOptimizeWithEmptyArguments() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        optimizer.setSteps(0, 0);

        String result = optimizer.optimize();

        assertNotNull(result);
        assertEquals(0, optimizer.getEvaluations());
        assertEquals(0, optimizer.getIterations());
    }

    @Test(timeout = 4000)
    public void testOptimizeWithNullElementsInArguments() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        optimizer.setSteps(1, 1);

        // Should not throw NullPointerException on null elements
        optimizer.optimize((OptimizationData) null, new MaxEval(10), null, new MaxIter(10), null);

        assertEquals(10, optimizer.getMaxEvaluations());
        assertEquals(10, optimizer.getMaxIterations());
        assertEquals(1, optimizer.getEvaluations());
        assertEquals(1, optimizer.getIterations());
    }

    @Test(timeout = 4000)
    public void testExactMaxEvalBoundaryAcceptance() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        optimizer.setSteps(5, 0);

        // Reaching exact limit should succeed without exception
        optimizer.optimize(new MaxEval(5), new MaxIter(10));

        assertEquals(5, optimizer.getEvaluations());
        assertEquals(5, optimizer.getMaxEvaluations());
    }

    @Test(timeout = 4000)
    public void testExactMaxIterBoundaryAcceptance() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        optimizer.setSteps(0, 5);

        // Reaching exact limit should succeed without exception
        optimizer.optimize(new MaxEval(10), new MaxIter(5));

        assertEquals(5, optimizer.getIterations());
        assertEquals(5, optimizer.getMaxIterations());
    }

    @Test(timeout = 4000)
    public void testZeroAllowedEvaluationsBoundary() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        optimizer.setSteps(0, 0);

        optimizer.optimize(new MaxEval(0), new MaxIter(10));

        assertEquals(0, optimizer.getEvaluations());
        assertEquals(0, optimizer.getMaxEvaluations());
    }

    @Test(timeout = 4000)
    public void testMaxIntegerBoundaries() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        optimizer.setSteps(0, 0);

        optimizer.optimize(new MaxEval(Integer.MAX_VALUE), new MaxIter(Integer.MAX_VALUE));

        assertEquals(Integer.MAX_VALUE, optimizer.getMaxEvaluations());
        assertEquals(Integer.MAX_VALUE, optimizer.getMaxIterations());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Math-6)
    // =========================================================================

    /**
     * Targets Defects4J Math-6:
     * MaxIter is documented as optional optimization data. If omitted,
     * getMaxIterations() must default to Integer.MAX_VALUE so that algorithms
     * calling incrementIterationCount() do not fail prematurely with 0 iterations allowed.
     */
    @Test(timeout = 4000)
    public void testDefaultMaxIterationsIsMaxInteger() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        assertEquals("MaxIter is optional; default maximal iterations must be Integer.MAX_VALUE",
                     Integer.MAX_VALUE, optimizer.getMaxIterations());
    }

    /**
     * Targets Defects4J Math-6:
     * Optimization executed without explicit MaxIter parameter must successfully
     * permit iterations and track iteration counts greater than zero.
     */
    @Test(timeout = 4000)
    public void testOptimizeWithoutExplicitMaxIterTracksIterations() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        optimizer.setSteps(2, 3);

        optimizer.optimize(new MaxEval(100));

        assertTrue("Iterations performed must be greater than 0", optimizer.getIterations() > 0);
        assertEquals("Exact iteration count must be tracked", 3, optimizer.getIterations());
    }

    /**
     * Verifies the data retention contract across multiple optimize calls:
     * When an option is not specified in a subsequent call, previously set data is retained.
     */
    @Test(timeout = 4000)
    public void testOptimizationDataRetentionAcrossCalls() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);

        // Initial specification
        optimizer.optimize(new MaxEval(120), new MaxIter(60));
        assertEquals(120, optimizer.getMaxEvaluations());
        assertEquals(60, optimizer.getMaxIterations());

        // Subsequent call omitting MaxIter -> MaxIter must be retained
        optimizer.optimize(new MaxEval(240));
        assertEquals(240, optimizer.getMaxEvaluations());
        assertEquals("Previous MaxIter must be preserved when omitted", 60, optimizer.getMaxIterations());

        // Subsequent call omitting MaxEval -> MaxEval must be retained
        optimizer.optimize(new MaxIter(90));
        assertEquals("Previous MaxEval must be preserved when omitted", 240, optimizer.getMaxEvaluations());
        assertEquals(90, optimizer.getMaxIterations());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testTooManyEvaluationsExceptionTriggered() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        optimizer.setSteps(4, 0); // Exceeds MaxEval(3)

        try {
            optimizer.optimize(new MaxEval(3), new MaxIter(10));
            fail("Expected TooManyEvaluationsException when steps exceed MaxEval");
        } catch (TooManyEvaluationsException e) {
            assertEquals("Reported limit in exception must match MaxEval",
                         Integer.valueOf(3), e.getMax());
        }
    }

    @Test(timeout = 4000)
    public void testTooManyIterationsExceptionTriggered() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        optimizer.setSteps(0, 3); // Exceeds MaxIter(2)

        try {
            optimizer.optimize(new MaxEval(10), new MaxIter(2));
            fail("Expected TooManyIterationsException when steps exceed MaxIter");
        } catch (TooManyIterationsException e) {
            assertEquals("Reported limit in exception must match MaxIter",
                         Integer.valueOf(2), e.getMax());
        }
    }

    @Test(timeout = 4000)
    public void testZeroMaxEvalThrowsOnFirstIncrement() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        optimizer.setSteps(1, 0);

        try {
            optimizer.optimize(new MaxEval(0), new MaxIter(10));
            fail("Expected TooManyEvaluationsException for MaxEval(0)");
        } catch (TooManyEvaluationsException e) {
            assertEquals(Integer.valueOf(0), e.getMax());
        }
    }

    @Test(timeout = 4000)
    public void testZeroMaxIterThrowsOnFirstIncrement() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        optimizer.setSteps(0, 1);

        try {
            optimizer.optimize(new MaxEval(10), new MaxIter(0));
            fail("Expected TooManyIterationsException for MaxIter(0)");
        } catch (TooManyIterationsException e) {
            assertEquals(Integer.valueOf(0), e.getMax());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Subclass Extensibility
    // =========================================================================

    @Test(timeout = 4000)
    public void testSubclassCustomOptimizationDataParsing() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        CustomData custom = new CustomData("CUSTOM_CONFIG");

        optimizer.optimize(new MaxEval(25), custom, new MaxIter(15));

        assertNotNull(optimizer.getCustomData());
        assertEquals("CUSTOM_CONFIG", optimizer.getCustomData().getPayload());
        assertEquals(25, optimizer.getMaxEvaluations());
        assertEquals(15, optimizer.getMaxIterations());
    }

    @Test(timeout = 4000)
    public void testNullConvergenceCheckerAllowed() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        assertNull("ConvergenceChecker may be legitimately null", optimizer.getConvergenceChecker());

        String res = optimizer.optimize(new MaxEval(10), new MaxIter(10));
        assertNotNull(res);
    }
}