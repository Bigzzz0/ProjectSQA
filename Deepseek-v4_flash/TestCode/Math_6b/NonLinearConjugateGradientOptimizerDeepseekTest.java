package org.apache.commons.math3.optim.nonlinear.scalar.gradient;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer.Formula;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer.BracketingStep;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target branches and conditions:
 * 1. doOptimize() - GoalType.MINIMIZE vs MAXIMIZE (negation of gradient)
 * 2. doOptimize() - ConvergenceChecker.converged() returning true/false
 * 3. doOptimize() - findUpperBound() bracket search loop and termination
 * 4. doOptimize() - LineSearchFunction.value() computation
 * 5. doOptimize() - Beta calculation: FLETCHER_REEVES vs POLAK_RIBIERE
 * 6. doOptimize() - Conjugation reset condition: iter % n == 0 || beta < 0
 * 7. parseOptimizationData() - BracketingStep parsing
 * 8. checkParameters() - Bounds validation (throws MathUnsupportedOperationException)
 * 9. IdentityPreconditioner.precondition() - returns clone
 * 10. Constructor variations (4 overloads)
 * 
 * Defect targeting: The known defect causes AssertionFailedError in multiple tests.
 * The root cause is likely in the line search or convergence logic. We target:
 * - Edge cases in findUpperBound (step overflow, sign change detection)
 * - Line search solver convergence with near-zero tolerance
 * - Beta calculation with deltaOld = 0 (division by zero)
 * - Conjugation reset logic
 */
public class NonLinearConjugateGradientOptimizerDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testConstructorWithDefaultSolverAndPreconditioner() {
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, new SimpleValueChecker(1e-6, 1e-6));
        assertNotNull(optimizer);
    }

    @Test(timeout = 4000)
    public void testConstructorWithCustomSolver() {
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.POLAK_RIBIERE, new SimpleValueChecker(1e-6, 1e-6), new BrentSolver());
        assertNotNull(optimizer);
    }

    @Test(timeout = 4000)
    public void testConstructorWithAllParameters() {
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, new SimpleValueChecker(1e-6, 1e-6), 
                new BrentSolver(), new NonLinearConjugateGradientOptimizer.IdentityPreconditioner());
        assertNotNull(optimizer);
    }

    @Test(timeout = 4000)
    public void testBracketingStepCreation() {
        BracketingStep step = new BracketingStep(2.5);
        assertEquals(2.5, step.getBracketingStep(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testIdentityPreconditioner() {
        NonLinearConjugateGradientOptimizer.IdentityPreconditioner precond = 
            new NonLinearConjugateGradientOptimizer.IdentityPreconditioner();
        double[] variables = {1.0, 2.0, 3.0};
        double[] r = {4.0, 5.0, 6.0};
        double[] result = precond.precondition(variables, r);
        assertArrayEquals(r, result, 1e-15);
        assertNotSame(r, result); // Verify it's a clone
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000, expected = MathUnsupportedOperationException.class)
    public void testOptimizeWithBoundsThrowsException() {
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, new SimpleValueChecker(1e-6, 1e-6));
        
        // Create a simple quadratic function for testing
        optimizer.optimize(
            new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0, 1.0}),
            new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(new org.apache.commons.math3.analysis.MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            new org.apache.commons.math3.optim.nonlinear.scalar.gradient.ObjectiveFunctionGradient(new org.apache.commons.math3.analysis.MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] {2 * point[0], 2 * point[1]};
                }
            }),
            GoalType.MINIMIZE,
            new org.apache.commons.math3.optim.SimpleBounds(new double[] {-1, -1}, new double[] {1, 1})
        );
    }

    @Test(timeout = 4000, expected = TooManyEvaluationsException.class)
    public void testOptimizeWithTooManyEvaluations() {
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, new SimpleValueChecker(1e-6, 1e-6));
        
        optimizer.optimize(
            new org.apache.commons.math3.optim.InitialGuess(new double[] {100.0, 100.0}),
            new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(new org.apache.commons.math3.analysis.MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            new org.apache.commons.math3.optim.nonlinear.scalar.gradient.ObjectiveFunctionGradient(new org.apache.commons.math3.analysis.MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] {2 * point[0], 2 * point[1]};
                }
            }),
            GoalType.MINIMIZE,
            new org.apache.commons.math3.optim.MaxEval(5)
        );
    }

    @Test(timeout = 4000)
    public void testOptimizeWithBracketingStep() {
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, new SimpleValueChecker(1e-6, 1e-6));
        
        PointValuePair result = optimizer.optimize(
            new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0, 1.0}),
            new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(new org.apache.commons.math3.analysis.MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            new org.apache.commons.math3.optim.nonlinear.scalar.gradient.ObjectiveFunctionGradient(new org.apache.commons.math3.analysis.MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] {2 * point[0], 2 * point[1]};
                }
            }),
            GoalType.MINIMIZE,
            new BracketingStep(0.5)
        );
        
        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 1e-6);
        assertArrayEquals(new double[] {0.0, 0.0}, result.getPoint(), 1e-6);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testTrivialOptimizationMinimize() {
        // This test targets the known defect that causes AssertionFailedError
        // in testTrivial. We test a simple quadratic function with known minimum.
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, new SimpleValueChecker(1e-6, 1e-6));
        
        PointValuePair result = optimizer.optimize(
            new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0, 1.0}),
            new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(new org.apache.commons.math3.analysis.MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            new org.apache.commons.math3.optim.nonlinear.scalar.gradient.ObjectiveFunctionGradient(new org.apache.commons.math3.analysis.MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] {2 * point[0], 2 * point[1]};
                }
            }),
            GoalType.MINIMIZE
        );
        
        assertNotNull("Result should not be null", result);
        assertTrue("Value should be close to 0", Math.abs(result.getValue()) < 1e-4);
        assertTrue("Point[0] should be close to 0", Math.abs(result.getPoint()[0]) < 1e-4);
        assertTrue("Point[1] should be close to 0", Math.abs(result.getPoint()[1]) < 1e-4);
    }

    @Test(timeout = 4000)
    public void testTrivialOptimizationMaximize() {
        // Test maximization of negative quadratic function
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.POLAK_RIBIERE, new SimpleValueChecker(1e-6, 1e-6));
        
        PointValuePair result = optimizer.optimize(
            new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0, 1.0}),
            new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(new org.apache.commons.math3.analysis.MultivariateFunction() {
                public double value(double[] point) {
                    return -point[0] * point[0] - point[1] * point[1];
                }
            }),
            new org.apache.commons.math3.optim.nonlinear.scalar.gradient.ObjectiveFunctionGradient(new org.apache.commons.math3.analysis.MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] {-2 * point[0], -2 * point[1]};
                }
            }),
            GoalType.MAXIMIZE
        );
        
        assertNotNull("Result should not be null", result);
        assertTrue("Value should be close to 0", Math.abs(result.getValue()) < 1e-4);
        assertTrue("Point[0] should be close to 0", Math.abs(result.getPoint()[0]) < 1e-4);
        assertTrue("Point[1] should be close to 0", Math.abs(result.getPoint()[1]) < 1e-4);
    }

    @Test(timeout = 4000)
    public void testOptimizationWithResetCondition() {
        // This test targets the conjugation reset condition (iter % n == 0 || beta < 0)
        // Use a function where the gradient direction changes significantly
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, new SimpleValueChecker(1e-6, 1e-6));
        
        PointValuePair result = optimizer.optimize(
            new org.apache.commons.math3.optim.InitialGuess(new double[] {2.0, -2.0}),
            new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(new org.apache.commons.math3.analysis.MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + 10 * point[1] * point[1];
                }
            }),
            new org.apache.commons.math3.optim.nonlinear.scalar.gradient.ObjectiveFunctionGradient(new org.apache.commons.math3.analysis.MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] {2 * point[0], 20 * point[1]};
                }
            }),
            GoalType.MINIMIZE
        );
        
        assertNotNull(result);
        assertTrue("Value should be close to 0", Math.abs(result.getValue()) < 1e-4);
    }

    @Test(timeout = 4000)
    public void testOptimizationWithPolakRibiere() {
        // Test Polak-Ribiere formula specifically
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.POLAK_RIBIERE, new SimpleValueChecker(1e-6, 1e-6));
        
        PointValuePair result = optimizer.optimize(
            new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0, 1.0}),
            new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(new org.apache.commons.math3.analysis.MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            new org.apache.commons.math3.optim.nonlinear.scalar.gradient.ObjectiveFunctionGradient(new org.apache.commons.math3.analysis.MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] {2 * point[0], 2 * point[1]};
                }
            }),
            GoalType.MINIMIZE
        );
        
        assertNotNull(result);
        assertTrue("Value should be close to 0", Math.abs(result.getValue()) < 1e-4);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = MathUnsupportedOperationException.class)
    public void testCheckParametersWithLowerBound() {
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, new SimpleValueChecker(1e-6, 1e-6));
        
        optimizer.optimize(
            new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0, 1.0}),
            new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(new org.apache.commons.math3.analysis.MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            new org.apache.commons.math3.optim.nonlinear.scalar.gradient.ObjectiveFunctionGradient(new org.apache.commons.math3.analysis.MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] {2 * point[0], 2 * point[1]};
                }
            }),
            GoalType.MINIMIZE,
            new org.apache.commons.math3.optim.SimpleBounds(new double[] {-1, -1}, null)
        );
    }

    @Test(timeout = 4000, expected = MathUnsupportedOperationException.class)
    public void testCheckParametersWithUpperBound() {
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, new SimpleValueChecker(1e-6, 1e-6));
        
        optimizer.optimize(
            new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0, 1.0}),
            new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(new org.apache.commons.math3.analysis.MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            new org.apache.commons.math3.optim.nonlinear.scalar.gradient.ObjectiveFunctionGradient(new org.apache.commons.math3.analysis.MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] {2 * point[0], 2 * point[1]};
                }
            }),
            GoalType.MINIMIZE,
            new org.apache.commons.math3.optim.SimpleBounds(null, new double[] {1, 1})
        );
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testFormulaEnumValues() {
        assertEquals(Formula.FLETCHER_REEVES, Formula.valueOf("FLETCHER_REEVES"));
        assertEquals(Formula.POLAK_RIBIERE, Formula.valueOf("POLAK_RIBIERE"));
        assertEquals(2, Formula.values().length);
    }

    @Test(timeout = 4000)
    public void testBracketingStepGetBracketingStep() {
        BracketingStep step = new BracketingStep(1.5);
        assertEquals(1.5, step.getBracketingStep(), 1e-15);
        
        BracketingStep step2 = new BracketingStep(0.0);
        assertEquals(0.0, step2.getBracketingStep(), 1e-15);
        
        BracketingStep step3 = new BracketingStep(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, step3.getBracketingStep(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testOptimizeWithCustomConvergenceChecker() {
        // Test with a convergence checker that converges immediately
        ConvergenceChecker<PointValuePair> immediateChecker = new ConvergenceChecker<PointValuePair>() {
            public boolean converged(int iteration, PointValuePair previous, PointValuePair current) {
                return true; // Always converge
            }
        };
        
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, immediateChecker);
        
        PointValuePair result = optimizer.optimize(
            new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0, 1.0}),
            new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(new org.apache.commons.math3.analysis.MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            new org.apache.commons.math3.optim.nonlinear.scalar.gradient.ObjectiveFunctionGradient(new org.apache.commons.math3.analysis.MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] {2 * point[0], 2 * point[1]};
                }
            }),
            GoalType.MINIMIZE
        );
        
        assertNotNull(result);
        // Should return the initial point since checker converges immediately
        assertEquals(1.0, result.getPoint()[0], 1e-15);
        assertEquals(1.0, result.getPoint()[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testLineSearchFunctionInternal() throws Exception {
        // Test the internal LineSearchFunction through optimization
        NonLinearConjugateGradientOptimizer optimizer = 
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, new SimpleValueChecker(1e-6, 1e-6));
        
        PointValuePair result = optimizer.optimize(
            new org.apache.commons.math3.optim.InitialGuess(new double[] {0.5, 0.5}),
            new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(new org.apache.commons.math3.analysis.MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            new org.apache.commons.math3.optim.nonlinear.scalar.gradient.ObjectiveFunctionGradient(new org.apache.commons.math3.analysis.MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] {2 * point[0], 2 * point[1]};
                }
            }),
            GoalType.MINIMIZE
        );
        
        assertNotNull(result);
        assertTrue("Value should be close to 0", Math.abs(result.getValue()) < 1e-4);
    }
}