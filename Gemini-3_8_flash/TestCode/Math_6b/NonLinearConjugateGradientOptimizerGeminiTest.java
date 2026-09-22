/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer
 *
 * Core Decision Branches & Conditions Analyzed:
 * 1. Constructor Overloads:
 *    - (Formula, Checker) -> uses default BrentSolver and IdentityPreconditioner
 *    - (Formula, Checker, UnivariateSolver) -> uses custom solver and IdentityPreconditioner
 *    - (Formula, Checker, UnivariateSolver, Preconditioner) -> full dependency injection
 * 2. Parameter Validation & OptimizationData:
 *    - parseOptimizationData with BracketingStep (initialStep configured)
 *    - checkParameters: getLowerBound() != null || getUpperBound() != null -> throws MathUnsupportedOperationException
 * 3. GoalType branches in doOptimize:
 *    - GoalType.MINIMIZE (reverses gradient r[i] = -r[i])
 *    - GoalType.MAXIMIZE (keeps gradient as is)
 * 4. Update Formula branches:
 *    - Formula.FLETCHER_REEVES (beta = delta / deltaOld)
 *    - Formula.POLAK_RIBIERE (computes deltaMid, beta = (delta - deltaMid) / deltaOld)
 * 5. Search Direction Reset Conditions:
 *    - iter % n == 0 (conjugation break periodically based on dimension)
 *    - beta < 0 (conjugation break on non-descent direction)
 *    - else: conjugate update direction: searchDirection[i] = steepestDescent[i] + beta * searchDirection[i]
 * 6. Line Search & Bracketing:
 *    - findUpperBound: loop doubling or multiplying step by max(2, yA/yB)
 *    - findUpperBound: unable to bracket -> MathIllegalStateException
 * 7. IdentityPreconditioner & Custom Preconditioner:
 *    - IdentityPreconditioner returns r.clone()
 *    - Custom Preconditioner transforms gradient correctly
 * 8. Defects4J Known Defect:
 *    - NonLinearConjugateGradientOptimizerTest::testTrivial
 *    - Root cause: doOptimize increments a local 'iter' variable instead of invoking
 *      incrementIterationCount() on BaseOptimizer; getIterations() remains 0 instead of > 0.
 */

package org.apache.commons.math3.optim.nonlinear.scalar.gradient;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.MullerSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer.BracketingStep;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer.Formula;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer.IdentityPreconditioner;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NonLinearConjugateGradientOptimizerGeminiTest {

    // -------------------------------------------------------------------------
    // Helper Test Functions
    // -------------------------------------------------------------------------

    /**
     * 2D Quadratic Function: f(x, y) = 2*(x - 3)^2 + 3*(y + 1)^2 + 5
     * Optimum: minimum at (3, -1) with value 5.
     */
    private static class Quadratic2DFunction {
        public ObjectiveFunction getObjectiveFunction() {
            return new ObjectiveFunction(point -> {
                double dx = point[0] - 3.0;
                double dy = point[1] + 1.0;
                return 2.0 * dx * dx + 3.0 * dy * dy + 5.0;
            });
        }

        public ObjectiveFunctionGradient getObjectiveFunctionGradient() {
            return new ObjectiveFunctionGradient(point -> {
                double gx = 4.0 * (point[0] - 3.0);
                double gy = 6.0 * (point[1] + 1.0);
                return new double[] { gx, gy };
            });
        }
    }

    /**
     * 1D Linear Problem: (2x - 3)^2 = 4x^2 - 12x + 9
     * Minimum at x = 1.5, value = 0.
     */
    private static class Linear1DProblem {
        public ObjectiveFunction getObjectiveFunction() {
            return new ObjectiveFunction(point -> {
                double diff = 2.0 * point[0] - 3.0;
                return diff * diff;
            });
        }

        public ObjectiveFunctionGradient getObjectiveFunctionGradient() {
            return new ObjectiveFunctionGradient(point -> {
                double diff = 2.0 * point[0] - 3.0;
                return new double[] { 4.0 * diff };
            });
        }
    }

    /**
     * 2D Concave Quadratic for Maximization:
     * f(x, y) = -(x - 1)^2 - 2*(y - 2)^2 + 10
     * Maximum at (1, 2) with value 10.
     */
    private static class Concave2DFunction {
        public ObjectiveFunction getObjectiveFunction() {
            return new ObjectiveFunction(point -> {
                double dx = point[0] - 1.0;
                double dy = point[1] - 2.0;
                return -dx * dx - 2.0 * dy * dy + 10.0;
            });
        }

        public ObjectiveFunctionGradient getObjectiveFunctionGradient() {
            return new ObjectiveFunctionGradient(point -> {
                double gx = -2.0 * (point[0] - 1.0);
                double gy = -4.0 * (point[1] - 2.0);
                return new double[] { gx, gy };
            });
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinimizeFletcherReevesDefaultConstructor() {
        Quadratic2DFunction problem = new Quadratic2DFunction();
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                Formula.FLETCHER_REEVES,
                new SimplePointChecker<PointValuePair>(1e-8, 1e-8)
            );

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            problem.getObjectiveFunction(),
            problem.getObjectiveFunctionGradient(),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 10.0, -10.0 })
        );

        assertNotNull("Optimum must not be null", optimum);
        assertEquals(3.0, optimum.getPoint()[0], 1e-4);
        assertEquals(-1.0, optimum.getPoint()[1], 1e-4);
        assertEquals(5.0, optimum.getValue(), 1e-4);
    }

    @Test(timeout = 4000)
    public void testMinimizePolakRibiereWithCustomSolver() {
        Quadratic2DFunction problem = new Quadratic2DFunction();
        UnivariateSolver customSolver = new BrentSolver(1e-10, 1e-10);
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                Formula.POLAK_RIBIERE,
                new SimpleValueChecker(1e-8, 1e-8),
                customSolver
            );

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new BracketingStep(0.5),
            problem.getObjectiveFunction(),
            problem.getObjectiveFunctionGradient(),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        assertNotNull("Optimum must not be null", optimum);
        assertEquals(3.0, optimum.getPoint()[0], 1e-4);
        assertEquals(-1.0, optimum.getPoint()[1], 1e-4);
        assertEquals(5.0, optimum.getValue(), 1e-4);
    }

    @Test(timeout = 4000)
    public void testMaximizeGoal() {
        Concave2DFunction problem = new Concave2DFunction();
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                Formula.POLAK_RIBIERE,
                new SimpleValueChecker(1e-7, 1e-7)
            );

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            problem.getObjectiveFunction(),
            problem.getObjectiveFunctionGradient(),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] { -5.0, 5.0 })
        );

        assertNotNull("Optimum must not be null", optimum);
        assertEquals(1.0, optimum.getPoint()[0], 1e-4);
        assertEquals(2.0, optimum.getPoint()[1], 1e-4);
        assertEquals(10.0, optimum.getValue(), 1e-4);
    }

    @Test(timeout = 4000)
    public void testCustomDiagonalPreconditioner() {
        Quadratic2DFunction problem = new Quadratic2DFunction();
        // Diagonal preconditioner approximating the inverse Hessian [1/4, 1/6]
        Preconditioner diagPreconditioner = (variables, r) -> {
            double[] res = new double[r.length];
            res[0] = r[0] * 0.25;
            res[1] = r[1] * (1.0 / 6.0);
            return res;
        };

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                Formula.POLAK_RIBIERE,
                new SimpleValueChecker(1e-8, 1e-8),
                new BrentSolver(),
                diagPreconditioner
            );

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            problem.getObjectiveFunction(),
            problem.getObjectiveFunctionGradient(),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { -20.0, 15.0 })
        );

        assertNotNull(optimum);
        assertEquals(3.0, optimum.getPoint()[0], 1e-5);
        assertEquals(-1.0, optimum.getPoint()[1], 1e-5);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBracketingStep