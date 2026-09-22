package org.apache.commons.math.optimization;

/* [Branch & Defect Analysis Matrix]
 * Target Class: MultiStartUnivariateRealOptimizer
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructor:
 *    - Initialization of underlying optimizer, starts, generator, iteration/eval limits.
 * 2. Delegate Methods & State Inspections:
 *    - getFunctionValue(), getResult(), getAbsoluteAccuracy(), getIterationCount(),
 *      getMaximalIterationCount(), getMaxEvaluations(), getEvaluations(), getRelativeAccuracy()
 *    - Mutators & Resets: resetAbsoluteAccuracy(), resetMaximalIterationCount(), resetRelativeAccuracy(),
 *      setAbsoluteAccuracy(), setMaximalIterationCount(), setMaxEvaluations(), setRelativeAccuracy()
 * 3. getOptima() & getOptimaValues():
 *    - Branch: optima == null -> throws IllegalStateException (LocalizedFormats.NO_OPTIMUM_COMPUTED_YET)
 *    - Branch: optima != null -> returns cloned array (ensuring immutability of internal state)
 * 4. optimize(UnivariateRealFunction, GoalType, double, double, double):
 *    - 5-arg overload delegates to 4-arg optimize()
 * 5. optimize(UnivariateRealFunction, GoalType, double, double):
 *    - Loop i from 0 to starts - 1:
 *      - Branch i == 0 (bound1 = min, bound2 = max)
 *      - Branch i > 0 (random bounds via generator)
 *      - Exception catch: FunctionEvaluationException -> store Double.NaN
 *      - Exception catch: ConvergenceException -> store Double.NaN
 *      - Normal convergence -> store result and value
 *      - Accumulation of totalIterations and totalEvaluations
 *    - NaN Filtering & Swapping:
 *      - Branch: Double.isNaN(optima[i]) inside lastNaN loop
 *    - Sorting (Insertion Sort):
 *      - Branch: GoalType.MAXIMIZE vs GoalType.MINIMIZE with XOR logic
 *      - Branch: Inner while loop conditions (i >= 0, condition check, i decrement)
 *    - Convergence Check:
 *      - Branch: Double.isNaN(optima[0]) -> throws OptimizationException
 *      - Branch: !Double.isNaN(optima[0]) -> returns optima[0]
 *
 * Defects4J Ground Truth Target:
 * - MultiStartUnivariateRealOptimizerTest::testQuinticMin:
 *   Underlying optimizer's getResult() does not match the multi-start best result when the
 *   last start converged to an inferior point. The test asserts that getResult() matches the
 *   best optimum found across starts.
 */

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.QuinticFunction;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomGenerator;
import org.junit.Test;

import static org.junit.Assert.*;

public class MultiStartUnivariateRealOptimizerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDelegationAndStateAccessors() {
        BrentOptimizer underlying = new BrentOptimizer();
        JDKRandomGenerator generator = new JDKRandomGenerator();
        generator.setSeed(42L);

        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 3, generator);

        optimizer.setAbsoluteAccuracy(1e-8);
        assertEquals(1e-8, optimizer.getAbsoluteAccuracy(), 1e-15);

        optimizer.setRelativeAccuracy(1e-7);
        assertEquals(1e-7, optimizer.getRelativeAccuracy(), 1e-15);

        optimizer.setMaximalIterationCount(500);
        assertEquals(500, optimizer.getMaximalIterationCount());

        optimizer.setMaxEvaluations(1000);
        assertEquals(1000, optimizer.getMaxEvaluations());

        optimizer.resetAbsoluteAccuracy();
        assertEquals(1e-11, optimizer.getAbsoluteAccuracy(), 1e-15);

        optimizer.resetRelativeAccuracy();
        assertEquals(1e-9, optimizer.getRelativeAccuracy(), 1e-15);

        optimizer.resetMaximalIterationCount();
        assertEquals(100, optimizer.getMaximalIterationCount());

        assertEquals(0, optimizer.getIterationCount());
        assertEquals(0, optimizer.getEvaluations());
    }

    @Test(timeout = 4000)
    public void testSuccessfulOptimizationMinimization() throws Exception {
        BrentOptimizer underlying = new BrentOptimizer();
        JDKRandomGenerator generator = new JDKRandomGenerator();
        generator.setSeed(12345L);

        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 5, generator);

        UnivariateRealFunction f = new SinFunction();
        double min = optimizer.optimize(f, GoalType.MINIMIZE, 3.0, 6.0);

        assertEquals(3.0 * Math.PI / 2.0, min, 1e-5);
        assertEquals(-1.0, f.value(min), 1e-5);

        double[] optima = optimizer.getOptima();
        double[] values = optimizer.getOptimaValues();
        assertEquals(5, optima.length);
        assertEquals(5, values.length);

        // Verify sorted order (ascending for minimization)
        for (int i = 1; i < values.length; ++i) {
            if (!Double.isNaN(values[i]) && !Double.isNaN(values[i - 1])) {
                assertTrue(values[i - 1] <= values[i]);
            }
        }

        assertTrue(optimizer.getIterationCount() > 0);
        assertTrue(optimizer.getEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testSuccessfulOptimizationMaximization() throws Exception {
        BrentOptimizer underlying = new BrentOptimizer();
        JDKRandomGenerator generator = new JDKRandomGenerator();
        generator.setSeed(54321L);

        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 5, generator);

        UnivariateRealFunction f = new SinFunction();
        double max = optimizer.optimize(f, GoalType.MAXIMIZE, 0.0, 3.0);

        assertEquals(Math.PI / 2.0, max, 1e-5);
        assertEquals(1.0, f.value(max), 1e-5);

        double[] optima = optimizer.getOptima();
        double[] values = optimizer.getOptimaValues();
        assertEquals(5, optima.length);
        assertEquals(5, values.length);

        // Verify sorted order (descending for maximization)
        for (int i = 1; i < values.length; ++i) {
            if (!Double.isNaN(values[i]) && !Double.isNaN(values[i - 1])) {
                assertTrue(values[i - 1] >= values[i]);
            }
        }
    }

    @Test(timeout = 4000)
    public void testFiveArgOptimizeDelegation() throws Exception {
        BrentOptimizer underlying = new BrentOptimizer();
        JDKRandomGenerator generator = new JDKRandomGenerator();
        generator.setSeed(999L);

        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 3, generator);

        UnivariateRealFunction f = new SinFunction();
        double res = optimizer.optimize(f, GoalType.MINIMIZE, 4.0, 5.0, 4.5);
        assertEquals(3.0 * Math.PI / 2.0, res, 1e-5);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetOptimaBeforeOptimizeThrowsIllegalStateException() {
        BrentOptimizer underlying = new BrentOptimizer();
        JDKRandomGenerator generator = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 3, generator);

        try {
            optimizer.getOptima();
            fail("Expected IllegalStateException before optimize() is called");
        } catch (IllegalStateException ise) {
            assertNotNull(ise.getMessage());
        }

        try {
            optimizer.getOptimaValues();
            fail("Expected IllegalStateException before optimize() is called");
        } catch (IllegalStateException ise) {
            assertNotNull(ise.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testOptimaArrayImmutability() throws Exception {
        BrentOptimizer underlying = new BrentOptimizer();
        JDKRandomGenerator generator = new JDKRandomGenerator();
        generator.setSeed(7L);

        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 3, generator);

        optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, 3.0, 6.0);

        double[] optima = optimizer.getOptima();
        double original = optima[0];
        optima[0] = original + 100.0;
        assertEquals(original, optimizer.getOptima()[0], 1e-15);

        double[] values = optimizer.getOptimaValues();
        double originalVal = values[0];
        values[0] = originalVal + 100.0;
        assertEquals(originalVal, optimizer.getOptimaValues()[0], 1e-15);
    }

    @Test(timeout = 4000)
    public void testSingleStartBoundary() throws Exception {
        BrentOptimizer underlying = new BrentOptimizer();
        JDKRandomGenerator generator = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 1, generator);

        double res = optimizer.optimize(new SinFunction(), GoalType.MAXIMIZE, 0.0, 3.0);
        assertEquals(Math.PI / 2.0, res, 1e-5);
        assertEquals(1, optimizer.getOptima().length);
        assertEquals(1, optimizer.getOptimaValues().length);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where getResult() on MultiStartUnivariateRealOptimizer returns
     * the result of the LAST restart from the underlying optimizer instead of the global
     * best optimum found.
     */
    @Test(timeout = 4000)
    public void testQuinticMinDefectDetection() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealOptimizer underlying = new BrentOptimizer();
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(4312001526L);

        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 5, g);
        optimizer.setAbsoluteAccuracy(10e-12);
        optimizer.setRelativeAccuracy(10e-10);
        optimizer.setMaximalIterationCount(100);
        optimizer.setMaxEvaluations(100);

        double result = optimizer.optimize(f, GoalType.MINIMIZE, -0.3, -0.2);
        assertEquals(-0.27195612846834, result, 1.0e-13);
        // On defective version, getResult() returns the last start (-0.27194301946870036)
        // instead of the best result (-0.27195612846834).
        assertEquals(-0.27195612846834, optimizer.getResult(), 1.0e-13);
        assertEquals(-0.04433426954946, optimizer.getFunctionValue(), 1.0e-13);

        double[] optima = optimizer.getOptima();
        double[] optimaValues = optimizer.getOptimaValues();
        for (int i = 0; i < optima.length; ++i) {
            assertEquals(f.value(optima[i]), optimaValues[i], 1.0e-10);
        }
        assertTrue(optimizer.getEvaluations() >= 50);
        assertTrue(optimizer.getEvaluations() <= 100);
        assertTrue(optimizer.getIterationCount() >= 50);
        assertTrue(optimizer.getIterationCount() <= 100);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = OptimizationException.class, timeout = 4000)
    public void testAllStartsThrowConvergenceException() throws Exception {
        UnivariateRealOptimizer alwaysFailOptimizer = new UnivariateRealOptimizer() {
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max)
                    throws ConvergenceException {
                throw new ConvergenceException();
            }

            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue)
                    throws ConvergenceException {
                throw new ConvergenceException();
            }

            public double getResult() { return Double.NaN; }
            public double getFunctionValue() { return Double.NaN; }
            public int getIterationCount() { return 1; }
            public int getEvaluations() { return 1; }
            public void setMaximalIterationCount(int count) {}
            public int getMaximalIterationCount() { return 0; }
            public void resetMaximalIterationCount() {}
            public void setMaxEvaluations(int maxEvaluations) {}
            public int getMaxEvaluations() { return 0; }
            public void resetMaxEvaluations() {}
            public void setAbsoluteAccuracy(double accuracy) {}
            public double getAbsoluteAccuracy() { return 0; }
            public void resetAbsoluteAccuracy() {}
            public void setRelativeAccuracy(double accuracy) {}
            public double getRelativeAccuracy() { return 0; }
            public void resetRelativeAccuracy() {}
        };

        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(alwaysFailOptimizer, 3, g);

        optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, -1.0, 1.0);
    }

    @Test(expected = OptimizationException.class, timeout = 4000)
    public void testAllStartsThrowFunctionEvaluationException() throws Exception {
        UnivariateRealOptimizer alwaysFailOptimizer = new UnivariateRealOptimizer() {
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max)
                    throws FunctionEvaluationException {
                throw new FunctionEvaluationException(min);
            }

            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue)
                    throws FunctionEvaluationException {
                throw new FunctionEvaluationException(startValue);
            }

            public double getResult() { return Double.NaN; }
            public double getFunctionValue() { return Double.NaN; }
            public int getIterationCount() { return 0; }
            public int getEvaluations() { return 1; }
            public void setMaximalIterationCount(int count) {}
            public int getMaximalIterationCount() { return 0; }
            public void resetMaximalIterationCount() {}
            public void setMaxEvaluations(int maxEvaluations) {}
            public int getMaxEvaluations() { return 0; }
            public void resetMaxEvaluations() {}
            public void setAbsoluteAccuracy(double accuracy) {}
            public double getAbsoluteAccuracy() { return 0; }
            public void resetAbsoluteAccuracy() {}
            public void setRelativeAccuracy(double accuracy) {}
            public double getRelativeAccuracy() { return 0; }
            public void resetRelativeAccuracy() {}
        };

        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(alwaysFailOptimizer, 2, g);

        optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, -1.0, 1.0);
    }

    @Test(timeout = 4000)
    public void testPartialConvergencePushesNaNToEnd() throws Exception {
        // Mock optimizer: converges on run 0 and run 2, fails on run 1
        UnivariateRealOptimizer flakyOptimizer = new UnivariateRealOptimizer() {
            private int runCount = 0;
            private double lastResult = 0;
            private double lastValue = 0;

            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max)
                    throws ConvergenceException {
                int currentRun = runCount++;
                if (currentRun == 1) {
                    throw new ConvergenceException();
                }
                lastResult = (currentRun == 0) ? 2.0 : 1.0;
                lastValue = (currentRun == 0) ? 10.0 : 5.0; // run 2 is better than run 0
                return lastResult;
            }

            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue)
                    throws ConvergenceException {
                return optimize(f, goalType, min, max);
            }

            public double getResult() { return lastResult; }
            public double getFunctionValue() { return lastValue; }
            public int getIterationCount() { return 5; }
            public int getEvaluations() { return 5; }
            public void setMaximalIterationCount(int count) {}
            public int getMaximalIterationCount() { return 100; }
            public void resetMaximalIterationCount() {}
            public void setMaxEvaluations(int maxEvaluations) {}
            public int getMaxEvaluations() { return 100; }
            public void resetMaxEvaluations() {}
            public void setAbsoluteAccuracy(double accuracy) {}
            public double getAbsoluteAccuracy() { return 1e-10; }
            public void resetAbsoluteAccuracy() {}
            public void setRelativeAccuracy(double accuracy) {}
            public double getRelativeAccuracy() { return 1e-10; }
            public void resetRelativeAccuracy() {}
        };

        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(flakyOptimizer, 3, g);

        double best = optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, 0.0, 5.0);
        assertEquals(1.0, best, 1e-15);

        double[] optima = optimizer.getOptima();
        double[] values = optimizer.getOptimaValues();

        assertEquals(3, optima.length);
        // Best should be index 0
        assertEquals(1.0, optima[0], 1e-15);
        assertEquals(5.0, values[0], 1e-15);

        // Second best should be index 1
        assertEquals(2.0, optima[1], 1e-15);
        assertEquals(10.0, values[1], 1e-15);

        // Third should be NaN
        assertTrue(Double.isNaN(optima[2]));
        assertTrue(Double.isNaN(values[2]));

        assertEquals(15, optimizer.getIterationCount());
        assertEquals(15, optimizer.getEvaluations());
    }

    @Test(timeout = 4000)
    public void testBoundReversalHandling() throws Exception {
        // Tests min > max in bound computation
        BrentOptimizer underlying = new BrentOptimizer();
        // A custom generator that produces numbers to test bound ordering
        RandomGenerator fixedGen = new RandomGenerator() {
            private int count = 0;
            public void setSeed(int seed) {}
            public void setSeed(int[] seed) {}
            public void setSeed(long seed) {}
            public void nextBytes(byte[] bytes) {}
            public int nextInt() { return 0; }
            public int nextInt(int n) { return 0; }
            public long nextLong() { return 0; }
            public boolean nextBoolean() { return false; }
            public float nextFloat() { return 0.5f; }
            public double nextDouble() {
                // Return 0.9 then 0.1 so bound1 > bound2
                return (count++ % 2 == 0) ? 0.9 : 0.1;
            }
            public double nextGaussian() { return 0; }
        };

        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 2, fixedGen);

        double res = optimizer.optimize(new SinFunction(), GoalType.MINIMIZE, 3.0, 6.0);
        assertEquals(3.0 * Math.PI / 2.0, res, 1e-5);
    }
}