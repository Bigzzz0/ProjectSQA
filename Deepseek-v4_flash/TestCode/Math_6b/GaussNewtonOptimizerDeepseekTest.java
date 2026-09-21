package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.optim.Target;
import org.apache.commons.math3.optim.Weight;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.TargetValues;
import org.apache.commons.math3.optim.nonlinear.vector.WeightMatrix;

/**
 * Test class for GaussNewtonOptimizer targeting the known defect in iteration counting.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core functional logic & state transitions
 *   - doOptimize() with valid model, convergence, iteration counting
 *   - Constructor with useLU = true/false
 *   - getIterations() after optimization
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Null convergence checker -> NullArgumentException
 *   - Bounds set -> MathUnsupportedOperationException
 *   - Singular normal equations -> ConvergenceException
 *   - Zero iterations? (converged immediately)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - The known defect: testGetIterations fails because iteration count is not updated correctly.
 *     We test that after a successful optimization, getIterations() returns a positive number.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Null checker
 *   - Bounds
 *   - Singular matrix
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Not applicable (no equals/hashcode/clone)
 */
public class GaussNewtonOptimizerDeepseekTest {

    // Helper: create a simple linear model y = a*x + b
    // Parameters: [a, b]
    // Observations: (x, y) pairs
    private static class LinearModel implements MultivariateVectorFunction {
        public double[] value(double[] params) {
            double a = params[0];
            double b = params[1];
            // x values: 0, 1, 2
            return new double[] { b, a + b, 2 * a + b };
        }
    }

    private static class LinearJacobian implements MultivariateMatrixFunction {
        public double[][] value(double[] params) {
            // Jacobian: df/da = x, df/db = 1
            return new double[][] {
                { 0, 1 },
                { 1, 1 },
                { 2, 1 }
            };
        }
    }

    // Target values for y = 2*x + 1 (a=2, b=1)
    private static final double[] TARGET = { 1.0, 3.0, 5.0 };
    private static final double[] WEIGHTS = { 1.0, 1.0, 1.0 };
    private static final double[] START = { 0.0, 0.0 };

    /**
     * Partition A: Core functional logic – normal optimization with LU decomposition.
     * Also tests iteration count (defect target).
     */
    @Test(timeout = 4000)
    public void testOptimizeWithLU() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true,
                new SimpleVectorValueChecker(1e-6, 1e-6));
        PointVectorValuePair result = optimizer.optimize(
                new MaxEval(1000),
                new MaxIter(100),
                new ModelFunction(new LinearModel()),
                new ModelFunctionJacobian(new LinearJacobian()),
                new Target(TARGET),
                new Weight(WEIGHTS),
                new InitialGuess(START));
        double[] point = result.getPoint();
        assertEquals(2.0, point[0], 1e-6);
        assertEquals(1.0, point[1], 1e-6);
        // Defect target: iteration count must be positive
        assertTrue("Iterations should be > 0", optimizer.getIterations() > 0);
    }

    /**
     * Partition A: Core functional logic – normal optimization with QR decomposition.
     */
    @Test(timeout = 4000)
    public void testOptimizeWithQR() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(false,
                new SimpleVectorValueChecker(1e-6, 1e-6));
        PointVectorValuePair result = optimizer.optimize(
                new MaxEval(1000),
                new MaxIter(100),
                new ModelFunction(new LinearModel()),
                new ModelFunctionJacobian(new LinearJacobian()),
                new Target(TARGET),
                new Weight(WEIGHTS),
                new InitialGuess(START));
        double[] point = result.getPoint();
        assertEquals(2.0, point[0], 1e-6);
        assertEquals(1.0, point[1], 1e-6);
        assertTrue("Iterations should be > 0", optimizer.getIterations() > 0);
    }

    /**
     * Partition B: Null convergence checker -> NullArgumentException.
     */
    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testNullChecker() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true, null);
        optimizer.optimize(
                new MaxEval(100),
                new MaxIter(10),
                new ModelFunction(new LinearModel()),
                new ModelFunctionJacobian(new LinearJacobian()),
                new Target(TARGET),
                new Weight(WEIGHTS),
                new InitialGuess(START));
    }

    /**
     * Partition B: Bounds set -> MathUnsupportedOperationException.
     */
    @Test(expected = MathUnsupportedOperationException.class, timeout = 4000)
    public void testBoundsNotSupported() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true,
                new SimpleVectorValueChecker(1e-6, 1e-6));
        // We need to pass bounds; but the API doesn't have a direct way.
        // The checkParameters() method checks getLowerBound() and getUpperBound().
        // We can simulate by using a subclass that sets bounds.
        // Alternatively, we can use reflection to set the bounds field.
        // For simplicity, we'll create an anonymous subclass that overrides getLowerBound().
        GaussNewtonOptimizer optimizerWithBounds = new GaussNewtonOptimizer(true,
                new SimpleVectorValueChecker(1e-6, 1e-6)) {
            @Override
            public double[] getLowerBound() {
                return new double[] { -1.0, -1.0 };
            }
        };
        optimizerWithBounds.optimize(
                new MaxEval(100),
                new MaxIter(10),
                new ModelFunction(new LinearModel()),
                new ModelFunctionJacobian(new LinearJacobian()),
                new Target(TARGET),
                new Weight(WEIGHTS),
                new InitialGuess(START));
    }

    /**
     * Partition B: Singular normal equations -> ConvergenceException.
     * We create a model where the Jacobian is rank-deficient.
     */
    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testSingularMatrix() {
        // Model: y = c (constant), Jacobian = [1, 0]? Actually we need a singular J^T W J.
        // Use a model with two parameters but only one independent observation.
        MultivariateVectorFunction singularModel = new MultivariateVectorFunction() {
            public double[] value(double[] params) {
                return new double[] { params[0] + params[1] }; // only one equation
            }
        };
        MultivariateMatrixFunction singularJacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] params) {
                return new double[][] { { 1.0, 1.0 } };
            }
        };
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true,
                new SimpleVectorValueChecker(1e-6, 1e-6));
        optimizer.optimize(
                new MaxEval(100),
                new MaxIter(10),
                new ModelFunction(singularModel),
                new ModelFunctionJacobian(singularJacobian),
                new Target(new double[] { 1.0 }),
                new Weight(new double[] { 1.0 }),
                new InitialGuess(new double[] { 0.0, 0.0 }));
    }

    /**
     * Partition C: Defect-targeted – verify iteration count after immediate convergence.
     * If the checker converges on first iteration, getIterations() should be 1.
     */
    @Test(timeout = 4000)
    public void testIterationsAfterImmediateConvergence() {
        // Use a very loose checker that converges immediately.
        ConvergenceChecker<PointVectorValuePair> immediateChecker =
                new ConvergenceChecker<PointVectorValuePair>() {
                    public boolean converged(int iteration, PointVectorValuePair previous,
                                             PointVectorValuePair current) {
                        return true; // always converged
                    }
                };
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(true, immediateChecker);
        optimizer.optimize(
                new MaxEval(100),
                new MaxIter(10),
                new ModelFunction(new LinearModel()),
                new ModelFunctionJacobian(new LinearJacobian()),
                new Target(TARGET),
                new Weight(WEIGHTS),
                new InitialGuess(START));
        // After one iteration, convergence is true, so iterations should be 1.
        assertEquals("Iterations should be 1 after immediate convergence", 1, optimizer.getIterations());
    }

    /**
     * Partition D: Exception path – MathInternalError (should never happen, but we can test
     * that the loop terminates). Not directly testable without mocking, but we can ensure
     * that the code does not throw it in normal cases.
     */

    /**
     * Partition E: Not applicable.
     */

    // Additional test to cover the constructor with default (useLU=true)
    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
                new SimpleVectorValueChecker(1e-6, 1e-6));
        assertTrue("Default should use LU", optimizer.useLU); // useLU is private, but we can test behavior
        // We can test that it works (same as testOptimizeWithLU)
        PointVectorValuePair result = optimizer.optimize(
                new MaxEval(1000),
                new MaxIter(100),
                new ModelFunction(new LinearModel()),
                new ModelFunctionJacobian(new LinearJacobian()),
                new Target(TARGET),
                new Weight(WEIGHTS),
                new InitialGuess(START));
        double[] point = result.getPoint();
        assertEquals(2.0, point[0], 1e-6);
        assertEquals(1.0, point[1], 1e-6);
    }
}