/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: MultiStartUnivariateRealOptimizer
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructor:
 *    - Valid optimizer, starts count, and random generator state initialization.
 * 2. getOptima():
 *    - Branch [optima == null]: Throws MathIllegalStateException(NO_OPTIMUM_COMPUTED_YET).
 *    - Branch [optima != null]: Returns defensive clone of optima array; mutation must not corrupt internal state.
 * 3. optimize(f, goal, min, max):
 *    - Delegates to optimize(f, goal, min, max, 0).
 * 4. optimize(f, goal, min, max, startValue):
 *    - Start loop iteration branch [i == 0]: Uses bounds exactly [min, max].
 *    - Start loop iteration branch [i > 0]: Uses generator.nextDouble() for bound1 & bound2.
 *    - Ordering branch: bound1 < bound2 vs bound1 > bound2 (FastMath.min / max guarantees sorted bounds).
 *    - Exception catch branches:
 *      * Catches FunctionEvaluationException -> optima[i] = null.
 *      * Catches ConvergenceException -> optima[i] = null.
 *    - Evaluation accounting: optimizer.getEvaluations() subtracted from optimizer.getMaxEvaluations(),
 *      and accumulated into totalEvaluations.
 *    - Post-sort check [optima[0] == null]: Throws ConvergenceException(NO_CONVERGENCE_WITH_ANY_START_POINT).
 *    - Post-sort check [optima[0] != null]: Returns optima[0] (best point found).
 * 5. sortPairs(goal) Comparator:
 *    - Branch [o1 == null, o2 == null]: Returns 0.
 *    - Branch [o1 == null, o2 != null]: Returns 1 (nulls sent to end).
 *    - Branch [o1 != null, o2 == null]: Returns -1 (non-nulls placed first).
 *    - Branch [goal == GoalType.MINIMIZE]: Double.compare(v1, v2) (ascending order).
 *    - Branch [goal == GoalType.MAXIMIZE]: Double.compare(v2, v1) (descending order).
 *    - Identical values (Double.compare returns 0).
 * 6. Delegate getters/setters:
 *    - setConvergenceChecker / getConvergenceChecker delegation.
 *    - setMaxEvaluations / getMaxEvaluations / getEvaluations lifecycle.
 *
 * Ground Truth Defect Targeted:
 * - MultiStartUnivariateRealOptimizerTest::testQuinticMin
 *   Failure: expected:<-0.2719561293> but was:<-0.2719561278056452>
 *   Underlying cause: Sub-interval optimization boundary shrinking and ignoring startValue in the loop
 *   leads to imprecise convergence or failure to pinpoint the analytical minimum within tolerance 1e-9.
 * ----------------------------------------------------------------------------------------------------
 */

package org.apache.commons.math.optimization.univariate;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.MathIllegalStateException;
import org.apache.commons.math.exception.ConvergenceException;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.ConvergenceChecker;
import org.apache.commons.math.random.RandomGenerator;
import org.apache.commons.math.random.JDKRandomGenerator;

public class MultiStartUnivariateRealOptimizerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testOptimizeMinimizeSorting() throws Exception {
        UnivariateRealPointValuePair p1 = new UnivariateRealPointValuePair(1.0, 50.0);
        UnivariateRealPointValuePair p2 = new UnivariateRealPointValuePair(2.0, 10.0);
        UnivariateRealPointValuePair p3 = new UnivariateRealPointValuePair(3.0, 30.0);

        StubOptimizer stub = new StubOptimizer(p1, p2, p3);
        ConstantGenerator gen = new ConstantGenerator(0.5);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> multi =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(stub, 3, gen);

        DummyFunction f = new DummyFunction();
        UnivariateRealPointValuePair best = multi.optimize(f, GoalType.MINIMIZE, -10.0, 10.0);

        assertNotNull("Best optimum must not be null", best);
        assertEquals(2.0, best.getPoint(), 1e-12);
        assertEquals(10.0, best.getValue(), 1e-12);

        UnivariateRealPointValuePair[] all = multi.getOptima();
        assertEquals(3, all.length);
        assertEquals(10.0, all[0].getValue(), 1e-12);
        assertEquals(30.0, all[1].getValue(), 1e-12);
        assertEquals(50.0, all[2].getValue(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testOptimizeMaximizeSorting() throws Exception {
        UnivariateRealPointValuePair p1 = new UnivariateRealPointValuePair(1.0, 50.0);
        UnivariateRealPointValuePair p2 = new UnivariateRealPointValuePair(2.0, 100.0);
        UnivariateRealPointValuePair p3 = new UnivariateRealPointValuePair(3.0, 30.0);

        StubOptimizer stub = new StubOptimizer(p1, p2, p3);
        ConstantGenerator gen = new ConstantGenerator(0.5);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> multi =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(stub, 3, gen);

        DummyFunction f = new DummyFunction();
        UnivariateRealPointValuePair best = multi.optimize(f, GoalType.MAXIMIZE, -10.0, 10.0);

        assertNotNull("Best optimum must not be null", best);
        assertEquals(2.0, best.getPoint(), 1e-12);
        assertEquals(100.0, best.getValue(), 1e-12);

        UnivariateRealPointValuePair[] all = multi.getOptima();
        assertEquals(3, all.length);
        assertEquals(100.0, all[0].getValue(), 1e-12);
        assertEquals(50.0, all[1].getValue(), 1e-12);
        assertEquals(30.0, all[2].getValue(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testEvaluationsAndMaxEvaluationsAccounting() throws Exception {
        UnivariateRealPointValuePair p = new UnivariateRealPointValuePair(0.0, 0.0);
        StubOptimizer stub = new StubOptimizer(p, p, p);
        stub.setEvaluationsPerRun(25);

        ConstantGenerator gen = new ConstantGenerator(0.3);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> multi =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(stub, 3, gen);

        multi.setMaxEvaluations(1000);
        assertEquals(1000, multi.getMaxEvaluations());
        assertEquals(1000, stub.getMaxEvaluations());

        multi.optimize(new DummyFunction(), GoalType.MINIMIZE, 0.0, 5.0);

        assertEquals(75, multi.getEvaluations());
        assertEquals(1000 - 75, stub.getMaxEvaluations());
    }

    @Test(timeout = 4000)
    public void testConvergenceCheckerDelegation() {
        StubOptimizer stub = new StubOptimizer();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> multi =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(stub, 1, new ConstantGenerator(0.0));

        assertNull(multi.getConvergenceChecker());

        ConvergenceChecker<UnivariateRealPointValuePair> checker =
            new ConvergenceChecker<UnivariateRealPointValuePair>() {
                public boolean converged(int iteration, UnivariateRealPointValuePair previous, UnivariateRealPointValuePair current) {
                    return true;
                }
            };

        multi.setConvergenceChecker(checker);
        assertSame(checker, stub.getConvergenceChecker());
        assertSame(checker, multi.getConvergenceChecker());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingleStartOnlyUsesMinMaxDirectly() throws Exception {
        StubOptimizer stub = new StubOptimizer(new UnivariateRealPointValuePair(1.5, 2.5));
        ConstantGenerator gen = new ConstantGenerator(0.999);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> multi =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(stub, 1, gen);

        multi.optimize(new DummyFunction(), GoalType.MINIMIZE, 1.0, 3.0);

        assertEquals(1, stub.capturedMins.size());
        assertEquals(1.0, stub.capturedMins.get(0), 1e-12);
        assertEquals(3.0, stub.capturedMaxs.get(0), 1e-12);
        assertEquals(1, multi.getOptima().length);
    }

    @Test(timeout = 4000)
    public void testGeneratorProducesReversedBoundsOrdersCorrectly() throws Exception {
        // Sequence alternates so that bound1 > bound2 in the second start
        SequenceGenerator seqGen = new SequenceGenerator(0.8, 0.2);
        StubOptimizer stub = new StubOptimizer(
            new UnivariateRealPointValuePair(1.0, 10.0),
            new UnivariateRealPointValuePair(1.5, 5.0)
        );

        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> multi =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(stub, 2, seqGen);

        multi.optimize(new DummyFunction(), GoalType.MINIMIZE, 0.0, 10.0);

        assertEquals(2, stub.capturedMins.size());
        // Start 0: exactly [0.0, 10.0]
        assertEquals(0.0, stub.capturedMins.get(0), 1e-12);
        assertEquals(10.0, stub.capturedMaxs.get(0), 1e-12);

        // Start 1: bound1 = 8.0, bound2 = 2.0 -> min = 2.0, max = 8.0
        assertEquals(2.0, stub.capturedMins.get(1), 1e-12);
        assertEquals(8.0, stub.capturedMaxs.get(1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetOptimaDefensiveCloning() throws Exception {
        UnivariateRealPointValuePair p = new UnivariateRealPointValuePair(2.0, 4.0);
        StubOptimizer stub = new StubOptimizer(p);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> multi =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(stub, 1, new ConstantGenerator(0.0));

        multi.optimize(new DummyFunction(), GoalType.MINIMIZE, 0.0, 5.0);

        UnivariateRealPointValuePair[] clone1 = multi.getOptima();
        clone1[0] = null; // Attempt external corruption

        UnivariateRealPointValuePair[] clone2 = multi.getOptima();
        assertNotNull("Internal optima array must not be mutated via returned clone", clone2[0]);
        assertEquals(4.0, clone2[0].getValue(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testEqualValuesSortingStability() throws Exception {
        UnivariateRealPointValuePair p1 = new UnivariateRealPointValuePair(1.0, 15.0);
        UnivariateRealPointValuePair p2 = new UnivariateRealPointValuePair(2.0, 15.0);
        StubOptimizer stub = new StubOptimizer(p1, p2);

        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> multi =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(stub, 2, new ConstantGenerator(0.5));

        UnivariateRealPointValuePair best = multi.optimize(new DummyFunction(), GoalType.MINIMIZE, 0.0, 10.0);
        assertNotNull(best);
        assertEquals(15.0, best.getValue(), 1e-12);
        assertEquals(2, multi.getOptima().length);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Exact replication of the ground-truth defect condition in Defects4J:
     * MultiStartUnivariateRealOptimizerTest::testQuinticMin.
     * On the defective version, interval shrinking and startValue omission in MultiStart
     * prevents finding the expected minimum point with 1e-9 precision,
     * yielding: expected:<-0.2719561293> but was:<-0.2719561278056452>.
     */
    @Test(timeout = 4000)
    public void testQuinticMin() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 0.5) * x * (x + 0.5) * (x + 1.0);
            }
        };

        BrentOptimizer underlying = new BrentOptimizer(1e-9, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(43120011L);

        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 5, g);

        UnivariateRealPointValuePair optimum =
            optimizer.optimize(f, GoalType.MINIMIZE, -0.3, -0.2);

        assertEquals(-0.2719561293, optimum.getPoint(), 1e-9);
        assertEquals(-0.0443342695, optimum.getValue(), 1e-9);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = MathIllegalStateException.class, timeout = 4000)
    public void testGetOptimaBeforeOptimizeThrowsMathIllegalStateException() {
        StubOptimizer stub = new StubOptimizer();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> multi =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(stub, 3, new ConstantGenerator(0.1));

        multi.getOptima();
    }

    @Test(timeout = 4000)
    public void testPartialFailureCatchesFunctionEvaluationExceptionAndConvergenceException() throws Exception {
        UnivariateRealPointValuePair successPoint = new UnivariateRealPointValuePair(3.0, 7.0);

        StubOptimizer stub = new StubOptimizer(
            null, // start 0: throws FunctionEvaluationException
            null, // start 1: throws ConvergenceException
            successPoint // start 2: succeeds
        );
        stub.setException(0, new FunctionEvaluationException(1.0));
        stub.setException(1, new ConvergenceException());

        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> multi =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(stub, 3, new ConstantGenerator(0.2));

        UnivariateRealPointValuePair best = multi.optimize(new DummyFunction(), GoalType.MINIMIZE, 0.0, 10.0);

        assertSame(successPoint, best);
        UnivariateRealPointValuePair[] optima = multi.getOptima();
        assertEquals(3, optima.length);
        assertSame(successPoint, optima[0]);
        assertNull("Failed start must be sorted to null at end", optima[1]);
        assertNull("Failed start must be sorted to null at end", optima[2]);
    }

    @