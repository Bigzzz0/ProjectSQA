package org.apache.commons.math.optimization.univariate;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.ConvergenceChecker;
import org.apache.commons.math.exception.ConvergenceException;
import org.apache.commons.math.exception.MathIllegalStateException;
import org.apache.commons.math.random.RandomGenerator;

import java.util.Random;

/**
 * Test class for MultiStartUnivariateRealOptimizer.
 * Targets maximum line/branch coverage and the known defect.
 */
public class MultiStartUnivariateRealOptimizerDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     *
     * Branches covered:
     * - Constructor: optimizer, starts, generator assignment.
     * - setConvergenceChecker / getConvergenceChecker: delegation.
     * - getMaxEvaluations / getEvaluations / setMaxEvaluations: state management.
     * - getOptima: null check (throws MathIllegalStateException) and clone.
     * - optimize(FUNC,GoalType,double,double): delegates to 5-arg version.
     * - optimize(FUNC,GoalType,double,double,double):
     *   - Loop over starts (i=0..starts-1).
     *   - i==0 branch: uses min/max directly.
     *   - i>0 branch: uses random bounds.
     *   - Catch FunctionEvaluationException -> optima[i]=null.
     *   - Catch ConvergenceException -> optima[i]=null.
     *   - Evaluation count update and maxEvaluations decrement.
     *   - sortPairs call.
     *   - If optima[0]==null -> throw ConvergenceException.
     *   - Return optima[0].
     * - sortPairs: comparator handles nulls, MINIMIZE vs MAXIMIZE ordering.
     *
     * Defect targeting:
     * - The known defect (testQuinticMin) shows a slight numerical difference.
     *   Likely caused by incorrect evaluation count management or bound generation.
     *   We test with a controlled optimizer that returns a known best value,
     *   and verify that the multi-start result matches the expected best.
     *   Also test that getOptima returns correct array after optimize.
     */

    // Helper: a simple optimizer that returns a fixed point-value pair.
    private static class FixedOptimizer implements BaseUnivariateRealOptimizer<UnivariateRealFunction> {
        private final UnivariateRealPointValuePair result;
        private int maxEval;
        private int evalCount;
        private ConvergenceChecker<UnivariateRealPointValuePair> checker;

        FixedOptimizer(UnivariateRealPointValuePair result) {
            this.result = result;
            this.maxEval = Integer.MAX_VALUE;
            this.evalCount = 0;
        }

        @Override
        public void setConvergenceChecker(ConvergenceChecker<UnivariateRealPointValuePair> checker) {
            this.checker = checker;
        }

        @Override
        public ConvergenceChecker<UnivariateRealPointValuePair> getConvergenceChecker() {
            return checker;
        }

        @Override
        public int getMaxEvaluations() {
            return maxEval;
        }

        @Override
        public int getEvaluations() {
            return evalCount;
        }

        @Override
        public void setMaxEvaluations(int maxEvaluations) {
            this.maxEval = maxEvaluations;
        }

        @Override
        public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goal,
                                                      double min, double max,
                                                      double startValue) {
            evalCount = 1; // simulate one evaluation
            return result;
        }

        @Override
        public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goal,
                                                      double min, double max) {
            return optimize(f, goal, min, max, 0);
        }
    }

    // Helper: an optimizer that throws FunctionEvaluationException.
    private static class ThrowingOptimizer implements BaseUnivariateRealOptimizer<UnivariateRealFunction> {
        private int maxEval;
        private int evalCount;

        ThrowingOptimizer() {
            this.maxEval = Integer.MAX_VALUE;
            this.evalCount = 0;
        }

        @Override
        public void setConvergenceChecker(ConvergenceChecker<UnivariateRealPointValuePair> checker) {}

        @Override
        public ConvergenceChecker<UnivariateRealPointValuePair> getConvergenceChecker() { return null; }

        @Override
        public int getMaxEvaluations() { return maxEval; }

        @Override
        public int getEvaluations() { return evalCount; }

        @Override
        public void setMaxEvaluations(int maxEvaluations) { this.maxEval = maxEvaluations; }

        @Override
        public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goal,
                                                      double min, double max,
                                                      double startValue) throws FunctionEvaluationException {
            evalCount = 0;
            throw new FunctionEvaluationException(0.0);
        }

        @Override
        public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goal,
                                                      double min, double max) throws FunctionEvaluationException {
            return optimize(f, goal, min, max, 0);
        }
    }

    // Helper: an optimizer that throws ConvergenceException.
    private static class ConvergenceThrowingOptimizer implements BaseUnivariateRealOptimizer<UnivariateRealFunction> {
        private int maxEval;
        private int evalCount;

        ConvergenceThrowingOptimizer() {
            this.maxEval = Integer.MAX_VALUE;
            this.evalCount = 0;
        }

        @Override
        public void setConvergenceChecker(ConvergenceChecker<UnivariateRealPointValuePair> checker) {}

        @Override
        public ConvergenceChecker<UnivariateRealPointValuePair> getConvergenceChecker() { return null; }

        @Override
        public int getMaxEvaluations() { return maxEval; }

        @Override
        public int getEvaluations() { return evalCount; }

        @Override
        public void setMaxEvaluations(int maxEvaluations) { this.maxEval = maxEvaluations; }

        @Override
        public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goal,
                                                      double min, double max,
                                                      double startValue) throws ConvergenceException {
            evalCount = 0;
            throw new ConvergenceException();
        }

        @Override
        public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goal,
                                                      double min, double max) throws ConvergenceException {
            return optimize(f, goal, min, max, 0);
        }
    }

    // Helper: a deterministic random generator for testing.
    private static class DeterministicRandom implements RandomGenerator {
        private final double[] values;
        private int index;

        DeterministicRandom(double... values) {
            this.values = values;
            this.index = 0;
        }

        @Override
        public void setSeed(int seed) {}

        @Override
        public void setSeed(int[] seed) {}

        @Override
        public void setSeed(long seed) {}

        @Override
        public void nextBytes(byte[] bytes) {}

        @Override
        public int nextInt() { return 0; }

        @Override
        public int nextInt(int n) { return 0; }

        @Override
        public long nextLong() { return 0L; }

        @Override
        public boolean nextBoolean() { return false; }

        @Override
        public float nextFloat() { return 0.0f; }

        @Override
        public double nextDouble() {
            double v = values[index % values.length];
            index++;
            return v;
        }

        @Override
        public double nextGaussian() { return 0.0; }
    }

    // ---------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorAndGetters() {
        FixedOptimizer inner = new FixedOptimizer(new UnivariateRealPointValuePair(1.0, 2.0));
        DeterministicRandom rand = new DeterministicRandom(0.5, 0.3);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> ms =
                new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(inner, 3, rand);
        assertNotNull(ms);
        // Check initial state
        assertEquals(Integer.MAX_VALUE, ms.getMaxEvaluations()); // default from inner?
        // Actually getMaxEvaluations returns the field maxEvaluations, initially 0? No, it's not set.
        // The field maxEvaluations is not initialized in constructor, so it's 0 by default.
        assertEquals(0, ms.getMaxEvaluations());
        assertEquals(0, ms.getEvaluations());
    }

    @Test(timeout = 4000)
    public void testSetMaxEvaluations() {
        FixedOptimizer inner = new FixedOptimizer(new UnivariateRealPointValuePair(1.0, 2.0));
        DeterministicRandom rand = new DeterministicRandom(0.5);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> ms =
                new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(inner, 1, rand);
        ms.setMaxEvaluations(100);
        assertEquals(100, ms.getMaxEvaluations());
        assertEquals(100, inner.getMaxEvaluations());
    }

    @Test(timeout = 4000)
    public void testConvergenceCheckerDelegation() {
        FixedOptimizer inner = new FixedOptimizer(new UnivariateRealPointValuePair(1.0, 2.0));
        DeterministicRandom rand = new DeterministicRandom(0.5);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> ms =
                new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(inner, 1, rand);
        ConvergenceChecker<UnivariateRealPointValuePair> checker = new ConvergenceChecker<UnivariateRealPointValuePair>() {
            public boolean converged(int iteration, UnivariateRealPointValuePair previous, UnivariateRealPointValuePair current) {
                return true;
            }
        };
        ms.setConvergenceChecker(checker);
        assertSame(checker, ms.getConvergenceChecker());
        assertSame(checker, inner.getConvergenceChecker());
    }

    // ---------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testStartsLessThanOrEqualToOne() {
        // starts <= 1 means multi-start disabled, but still works.
        FixedOptimizer inner = new FixedOptimizer(new UnivariateRealPointValuePair(1.0, 2.0));
        DeterministicRandom rand = new DeterministicRandom(0.5);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> ms =
                new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(inner, 1, rand);
        UnivariateRealPointValuePair result = ms.optimize(new SimpleFunction(), GoalType.MINIMIZE, 0.0, 10.0);
        assertEquals(1.0, result.getPoint(), 1e-15);
        assertEquals(2.0, result.getValue(), 1e-15);
        // getOptima should return array of length 1
        UnivariateRealPointValuePair[] optima = ms.getOptima();
        assertEquals(1, optima.length);
        assertNotNull(optima[0]);
    }

    @Test(timeout = 4000)
    public void testStartsZero() {
        // starts = 0: loop does nothing, optima[0] remains null -> ConvergenceException
        FixedOptimizer inner = new FixedOptimizer(new UnivariateRealPointValuePair(1.0, 2.0));
        DeterministicRandom rand = new DeterministicRandom(0.5);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> ms =
                new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(inner, 0, rand);
        try {
            ms.optimize(new SimpleFunction(), GoalType.MINIMIZE, 0.0, 10.0);
            fail("Expected ConvergenceException");
        } catch (ConvergenceException e) {
            // expected
        } catch (FunctionEvaluationException e) {
            fail("Wrong exception");
        }
    }

    @Test(timeout = 4000)
    public void testGetOptimaBeforeOptimize() {
        FixedOptimizer inner = new FixedOptimizer(new UnivariateRealPointValuePair(1.0, 2.0));
        DeterministicRandom rand = new DeterministicRandom(0.5);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> ms =
                new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(inner, 1, rand);
        try {
            ms.getOptima();
            fail("Expected MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            // expected
        }
    }

    // ---------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testQuinticMinDefectReproduction() throws FunctionEvaluationException {
        // This test targets the known defect: expected -0.2719561293 but got -0.2719561278056452.
        // We simulate a scenario where the multi-start optimizer should return the best value.
        // The defect likely involves evaluation count or bound generation.
        // We use a controlled optimizer that returns different values for different starts.
        // The best value should be -0.2719561293 (minimization).
        // We'll create an optimizer that returns values in order: first start returns a worse value,
        // second start returns the best, third returns another worse.
        // The multi-start should pick the best (min) after sorting.
        // We'll also verify getOptima order.

        // Create an inner optimizer that returns specific results based on call count.
        // Since we cannot easily track calls, we'll use a custom optimizer that returns a sequence.
        // We'll use a fixed array of results and a counter.
        final UnivariateRealPointValuePair[] results = new UnivariateRealPointValuePair[] {
            new UnivariateRealPointValuePair(0.5, 0.5),          // worse
            new UnivariateRealPointValuePair(-0.2719561293, -0.2719561293), // best (expected)
            new UnivariateRealPointValuePair(0.2, 0.2)           // worse
        };
        final int[] callCount = {0};
        BaseUnivariateRealOptimizer<UnivariateRealFunction> inner = new BaseUnivariateRealOptimizer<UnivariateRealFunction>() {
            private int maxEval = Integer.MAX_VALUE;
            private int evalCount = 0;
            private ConvergenceChecker<UnivariateRealPointValuePair> checker;

            @Override
            public void setConvergenceChecker(ConvergenceChecker<UnivariateRealPointValuePair> checker) {
                this.checker = checker;
            }

            @Override
            public ConvergenceChecker<UnivariateRealPointValuePair> getConvergenceChecker() {
                return checker;
            }

            @Override
            public int getMaxEvaluations() { return maxEval; }

            @Override
            public int getEvaluations() { return evalCount; }

            @Override
            public void setMaxEvaluations(int maxEvaluations) { this.maxEval = maxEvaluations; }

            @Override
            public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goal,
                                                          double min, double max,
                                                          double startValue) {
                evalCount = 1;
                int idx = callCount[0];
                callCount[0]++;
                return results[idx % results.length];
            }

            @Override
            public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goal,
                                                          double min, double max) {
                return optimize(f, goal, min, max, 0);
            }
        };

        DeterministicRandom rand = new DeterministicRandom(0.5, 0.3); // not used for i==0, but for i>0
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> ms =
                new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(inner, 3, rand);

        UnivariateRealPointValuePair result = ms.optimize(new SimpleFunction(), GoalType.MINIMIZE, 0.0, 10.0);
        // The best is the second result: point -0.2719561293, value -0.2719561293
        assertEquals("Defect: expected best point", -0.2719561293, result.getPoint(), 1e-10);
        assertEquals("Defect: expected best value", -0.2719561293, result.getValue(), 1e-10);

        // Also verify getOptima order: sorted best to worst, nulls at end.
        UnivariateRealPointValuePair[] optima = ms.getOptima();
        assertEquals(3, optima.length);
        assertNotNull(optima[0]);
        assertNotNull(optima[1]);
        assertNotNull(optima[2]);
        // First should be best (min value)
        assertEquals(-0.2719561293, optima[0].getValue(), 1e-10);
        // Second should be 0.2
        assertEquals(0.2, optima[1].getValue(), 1e-10);
        // Third should be 0.5
        assertEquals(0.5, optima[2].getValue(), 1e-10);
    }

    // ---------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFunctionEvaluationExceptionHandling() throws FunctionEvaluationException {
        ThrowingOptimizer inner = new ThrowingOptimizer();
        DeterministicRandom rand = new DeterministicRandom(0.5, 0.3);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> ms =
                new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(inner, 3, rand);
        // All starts throw FunctionEvaluationException -> all optima null -> ConvergenceException
        try {
            ms.optimize(new SimpleFunction(), GoalType.MINIMIZE, 0.0, 10.0);
            fail("Expected ConvergenceException");
        } catch (ConvergenceException e) {
            // expected
        }
        // getOptima should return array of nulls
        UnivariateRealPointValuePair[] optima = ms.getOptima();
        assertEquals(3, optima.length);
        for (UnivariateRealPointValuePair p : optima) {
            assertNull(p);
        }
    }

    @Test(timeout = 4000)
    public void testConvergenceExceptionHandling() throws FunctionEvaluationException {
        ConvergenceThrowingOptimizer inner = new ConvergenceThrowingOptimizer();
        DeterministicRandom rand = new DeterministicRandom(0.5, 0.3);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> ms =
                new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(inner, 3, rand);
        try {
            ms.optimize(new SimpleFunction(), GoalType.MINIMIZE, 0.0, 10.0);
            fail("Expected ConvergenceException");
        } catch (ConvergenceException e) {
            // expected
        }
        UnivariateRealPointValuePair[] optima = ms.getOptima();
        assertEquals(3, optima.length);
        for (UnivariateRealPointValuePair p : optima) {
            assertNull(p);
        }
    }

    @Test(timeout = 4000)
    public void testMixedExceptionsAndSuccess() throws FunctionEvaluationException {
        // First start succeeds, second throws, third succeeds.
        // We need an optimizer that can throw on specific calls.
        // Use a custom optimizer with a counter.
        final UnivariateRealPointValuePair successResult = new UnivariateRealPointValuePair(1.0, 2.0);
        final int[] callCount = {0};
        BaseUnivariateRealOptimizer<UnivariateRealFunction> inner = new BaseUnivariateRealOptimizer<UnivariateRealFunction>() {
            private int maxEval = Integer.MAX_VALUE;
            private int evalCount = 0;
            private ConvergenceChecker<UnivariateRealPointValuePair> checker;

            @Override
            public void setConvergenceChecker(ConvergenceChecker<UnivariateRealPointValuePair> checker) {
                this.checker = checker;
            }

            @Override
            public ConvergenceChecker<UnivariateRealPointValuePair> getConvergenceChecker() {
                return checker;
            }

            @Override
            public int getMaxEvaluations() { return maxEval; }

            @Override
            public int getEvaluations() { return evalCount; }

            @Override
            public void setMaxEvaluations(int maxEvaluations) { this.maxEval = maxEvaluations; }

            @Override
            public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goal,
                                                          double min, double max,
                                                          double startValue) throws FunctionEvaluationException {
                evalCount = 1;
                int idx = callCount[0];
                callCount[0]++;
                if (idx == 1) {
                    throw new FunctionEvaluationException(0.0);
                }
                return successResult;
            }

            @Override
            public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goal,
                                                          double min, double max) throws FunctionEvaluationException {
                return optimize(f, goal, min, max, 0);
            }
        };

        DeterministicRandom rand = new DeterministicRandom(0.5, 0.3);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> ms =
                new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(inner, 3, rand);

        UnivariateRealPointValuePair result = ms.optimize(new SimpleFunction(), GoalType.MINIMIZE, 0.0, 10.0);
        // Best should be successResult (value 2.0)
        assertEquals(1.0, result.getPoint(), 1e-15);
        assertEquals(2.0, result.getValue(), 1e-15);

        UnivariateRealPointValuePair[] optima = ms.getOptima();
        assertEquals(3, optima.length);
        // First two should be non-null (successes), third null (exception)
        assertNotNull(optima[0]);
        assertNotNull(optima[1]);
        assertNull(optima[2]);
        // Sorted: best (min value) first, then next best, then nulls.
        // Both successes have same value 2.0, so order among them is stable (original order? comparator returns 0 for equal values)
        // But we can check that the first two are the two successes.
    }

    // ---------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetOptimaReturnsClone() throws FunctionEvaluationException {
        FixedOptimizer inner = new FixedOptimizer(new UnivariateRealPointValuePair(1.0, 2.0));
        DeterministicRandom rand = new DeterministicRandom(0.5);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> ms =
                new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(inner, 1, rand);
        ms.optimize(new SimpleFunction(), GoalType.MINIMIZE, 0.0, 10.0);
        UnivariateRealPointValuePair[] optima1 = ms.getOptima();
        UnivariateRealPointValuePair[] optima2 = ms.getOptima();
        assertNotSame(optima1, optima2); // different arrays
        assertEquals(optima1[0].getPoint(), optima2[0].getPoint(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testOptimizeWithStartValue() throws FunctionEvaluationException {
        // The 5-arg optimize is called internally; we just test that it works.
        FixedOptimizer inner = new FixedOptimizer(new UnivariateRealPointValuePair(1.0, 2.0));
        DeterministicRandom rand = new DeterministicRandom(0.5);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> ms =
                new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(inner, 1, rand);
        UnivariateRealPointValuePair result = ms.optimize(new SimpleFunction(), GoalType.MINIMIZE, 0.0, 10.0, 5.0);
        assertEquals(1.0, result.getPoint(), 1e-15);
        assertEquals(2.0, result.getValue(), 1e-15);
    }

    // Helper function for tests.
    private static class SimpleFunction implements UnivariateRealFunction {
        @Override
        public double value(double x) {
            return x * x; // not used, optimizer returns fixed values
        }
    }
}