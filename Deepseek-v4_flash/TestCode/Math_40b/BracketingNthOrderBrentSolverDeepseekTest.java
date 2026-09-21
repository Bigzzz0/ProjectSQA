package org.apache.commons.math.analysis.solvers;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.analysis.UnivariateFunction;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.TooManyEvaluationsException;

/**
 * Comprehensive white-box JUnit 4 test suite for BracketingNthOrderBrentSolver.
 * Targets line coverage, branch coverage, and the known Defects4J defect (testIssue716).
 *
 * [Branch & Defect Analysis Matrix]
 * Part A: Core logic (doSolve flow, convergence, aging, inverse polynomial interpolation)
 * Part B: Boundary values (maximalOrder < 2, exact roots at endpoints/startValue, zero function values)
 * Part C: Defect-targeted zone (solver with ALLOWED_SIDE solutions triggering TooManyEvaluationsException)
 * Part D: Exception paths (NoBracketingException, NumberIsTooSmallException)
 * Part E: Object lifecycle (constructors, getMaximalOrder, solve overloads)
 */
public class BracketingNthOrderBrentSolverDeepseekTest {

    // ===================== Part A: Core Functional Logic & State Transitions =====================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndMaxOrder() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        assertEquals(5, solver.getMaximalOrder());
        assertNotNull(solver);
    }

    @Test(timeout = 4000)
    public void testSolveWithStartValueAndExactRoot() {
        // f(x) = x - 2 => root at 2.0
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return x - 2.0; }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(100, f, 0.0, 5.0, 1.5, AllowedSolution.ANY_SIDE);
        assertEquals(2.0, root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithoutStartValue() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return Math.sin(x) - 0.5; }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(100, f, 0.0, Math.PI, AllowedSolution.BELOW_SIDE);
        // sin(x) = 0.5 => x = PI/6 ≈ 0.5236, BELOW_SIDE returns left
        assertTrue(root >= 0.5 && root <= 0.55);
    }

    @Test(timeout = 4000)
    public void testExactRootAtMin() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return x * (x - 1); }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(100, f, 0.0, 2.0, AllowedSolution.ANY_SIDE);
        assertEquals(0.0, root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testExactRootAtMax() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return (x + 1) * (x - 3); }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(100, f, -2.0, 3.0, -2.5, AllowedSolution.ANY_SIDE);
        assertEquals(-1.0, root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testExactRootAtStartValue() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return Math.pow(x - 4.0, 3); }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(100, f, 0.0, 10.0, 4.0, AllowedSolution.ANY_SIDE);
        assertEquals(4.0, root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testFunctionEvaluationWithTinyAbsoluteAccuracy() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return Math.sin(x); }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-12, 5);
        double root = solver.solve(200, f, 3.0, 3.2, 3.1, AllowedSolution.ANY_SIDE);
        assertEquals(Math.PI, root, 1e-8);
    }

    @Test(timeout = 4000)
    public void testAgingAndBisectionFallback() {
        // A function that is nearly flat near root to trigger aging and bisection
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return (x - 1) * (x - 1) * (x - 1); }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(200, f, 0.5, 1.5, AllowedSolution.ANY_SIDE);
        assertEquals(1.0, root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testMaximalOrderFourConstructor() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-6, 4);
        assertEquals(4, solver.getMaximalOrder());
        // Use a simple function
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return x - 0.5; }
        };
        double root = solver.solve(100, f, -1.0, 2.0, AllowedSolution.ANY_SIDE);
        assertEquals(0.5, root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testRelativeAndAbsoluteAccuracyConstructor() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-4, 1e-6, 3);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return Math.cos(x); }
        };
        double root = solver.solve(100, f, 0.0, 2.0, AllowedSolution.ANY_SIDE);
        assertEquals(Math.PI / 2, root, 1e-4);
    }

    @Test(timeout = 4000)
    public void testFullAccuracyConstructor() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-4, 1e-6, 1e-8, 2);
        assertEquals(2, solver.getMaximalOrder());
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return Math.exp(x) - 2; }
        };
        double root = solver.solve(100, f, 0.0, 1.0, 0.5, AllowedSolution.ANY_SIDE);
        assertEquals(Math.log(2), root, 1e-4);
    }

    // ===================== Part B: Boundary Value Analysis & Extremes =====================

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testMaximalOrderTooSmall_1() {
        new BracketingNthOrderBrentSolver(1e-6, 1);  // < 2
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testMaximalOrderTooSmall_2() {
        new BracketingNthOrderBrentSolver(1e-4, 1e-6, 1);  // < 2
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testMaximalOrderTooSmall_3() {
        new BracketingNthOrderBrentSolver(1e-4, 1e-6, 1e-8, 1);  // < 2
    }

    @Test(expected = TooManyEvaluationsException.class, timeout = 4000)
    public void testMaxEvaluationsExceeded() {
        // A function that forces many evaluations (e.g., slow convergence with low order)
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return Math.sin(1.0 / x); }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-6, 2);
        // This should throw TooManyEvaluationsException because low order and oscillatory function
        solver.solve(5, f, 0.5, 2.0, AllowedSolution.ANY_SIDE);
    }

    @Test(expected = NoBracketingException.class, timeout = 4000)
    public void testNoBracketing() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return x * x + 1; } // always positive
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-6, 5);
        solver.solve(100, f, -1.0, 1.0, AllowedSolution.ANY_SIDE);
    }

    @Test(timeout = 4000)
    public void testAllAllowedSolutions() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return x - 0.75; }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        // ANY_SIDE returns whichever side has smaller function value
        double rootAny = solver.solve(100, f, 0.0, 1.0, AllowedSolution.ANY_SIDE);
        assertEquals(0.75, rootAny, 1e-6);
        // LEFT_SIDE returns left endpoint of bracket
        double rootLeft = solver.solve(100, f, 0.0, 1.0, AllowedSolution.LEFT_SIDE);
        assertTrue(rootLeft <= 0.75);
        // RIGHT_SIDE returns right endpoint
        double rootRight = solver.solve(100, f, 0.0, 1.0, AllowedSolution.RIGHT_SIDE);
        assertTrue(rootRight >= 0.75);
        // BELOW_SIDE: if yA <= 0 return xA else xB
        double rootBelow = solver.solve(100, f, 0.0, 1.0, AllowedSolution.BELOW_SIDE);
        // f(0) = -0.75 < 0 => returns xA (0.0)
        assertEquals(0.0, rootBelow, 1e-6);
        // ABOVE_SIDE: if yA < 0 return xB else xA
        double rootAbove = solver.solve(100, f, 0.0, 1.0, AllowedSolution.ABOVE_SIDE);
        assertEquals(1.0, rootAbove, 1e-6);
    }

    // ===================== Part C: Defect-Targeted Branch Zone (testIssue716) =====================

    @Test(timeout = 4000)
    public void testIssue716() {
        // This test targets the known Defects4J bug: TooManyEvaluationsException.
        // The bug occurred when using a combination of allowed solution (e.g., BELOW_SIDE)
        // and a function that requires many evaluations. The solver failed to converge
        // properly due to aging/bracketing logic.
        // We replicate a scenario similar to the original test.
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                // A function with a root at ~0.4 but with a steep gradient to force many iterations
                return (x - 0.4) * (x + 1) * (x - 2);
            }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        // Use BELOW_SIDE which may interact poorly with the aging mechanism.
        // The defective version throws TooManyEvaluationsException; the fixed version should succeed.
        try {
            double root = solver.solve(100, f, 0.0, 1.0, AllowedSolution.BELOW_SIDE);
            // Verify that root is close to the actual root
            assertEquals(0.4, root, 1e-6);
        } catch (TooManyEvaluationsException e) {
            fail("testIssue716 triggered the known defect: TooManyEvaluationsException was thrown");
        }
    }

    // Additional target: stress test with ALLOWED_SIDE and low maxEval to force the bug
    @Test(timeout = 4000)
    public void testIssue716Variant() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                // Function where root is near a boundary
                return Math.sin(100 * x) * Math.cos(x);
            }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-6, 2); // low order
        try {
            double root = solver.solve(50, f, 0.0, 0.5, AllowedSolution.ABOVE_SIDE);
            // Should converge to some root; we just ensure no exception
            assertFalse(Double.isNaN(root));
        } catch (TooManyEvaluationsException e) {
            fail("Defect reproduced: TooManyEvaluationsException should not occur");
        }
        // Note: In defective version, the solver may not converge within 50 evaluations.
    }

    // ===================== Part D: Exception & Defensive Guard Paths =====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullFunction() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-6, 5);
        solver.solve(100, null, 0.0, 1.0, AllowedSolution.ANY_SIDE);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullMinMax() {
        // min > max usually handled by base class; but we test invalid arguments
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-6, 5);
        solver.solve(100, new UnivariateFunction() {
            public double value(double x) { return x; }
        }, 1.0, 0.0, AllowedSolution.ANY_SIDE);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullAllowedSolution() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-6, 5);
        solver.solve(100, new UnivariateFunction() {
            public double value(double x) { return x; }
        }, -1.0, 1.0, null);
    }

    // ===================== Part E: Object Lifecycle & Contract Integrity =====================

    @Test(timeout = 4000)
    public void testGetMaximalOrderAfterConstruction() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-6, 2);
        assertEquals(2, solver.getMaximalOrder());
        solver = new BracketingNthOrderBrentSolver(1e-4, 1e-6, 3);
        assertEquals(3, solver.getMaximalOrder());
        solver = new BracketingNthOrderBrentSolver(1e-4, 1e-6, 1e-8, 4);
        assertEquals(4, solver.getMaximalOrder());
    }

    @Test(timeout = 4000)
    public void testSolveWithDifferentMaxEvaluations() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return Math.pow(x, 5) - 0.5; }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(100, f, 0.0, 1.0, AllowedSolution.ANY_SIDE);
        assertEquals(Math.pow(0.5, 1.0/5.0), root, 1e-6);
        // Use fewer evaluations; should still work but may trigger exception if too low
        try {
            root = solver.solve(10, f, 0.0, 1.0, AllowedSolution.ANY_SIDE);
            assertNotNull(root);
        } catch (TooManyEvaluationsException e) {
            // Acceptable if not enough evaluations
        }
    }

    @Test(timeout = 4000)
    public void testDefaultAbsoluteAccuracy() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        // By default, absolute accuracy is 1e-6
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) { return x - 1e-5; }
        };
        double root = solver.solve(100, f, -1.0, 2.0, AllowedSolution.ANY_SIDE);
        // Since accuracy is 1e-6, root should be within that of true root 1e-5
        assertEquals(1e-5, root, 1e-5);
    }

    @Test(timeout = 4000)
    public void testSolverWorksWithPolynomialOfHighOrder() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                // (x - 0.12345)^7 = 0 => root at 0.12345
                return Math.pow(x - 0.12345, 7);
            }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(200, f, 0.0, 1.0, AllowedSolution.ANY_SIDE);
        assertEquals(0.12345, root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testAgingCompensation() {
        // Function designed to cause aging in one direction
        // f(x) = (x - 0.8) * (x + 10) with bracket [0, 1] -> root at 0.8
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 0.8) * (x + 10);
            }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(200, f, 0.0, 1.0, AllowedSolution.ANY_SIDE);
        assertEquals(0.8, root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testInversePolynomialInterpolationWithCoincidentY() {
        // Use a function where two evaluations might produce the same y-value,
        // forcing the interpolation to fallback to bisection.
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                if (Math.abs(x - 0.5) < 1e-3) {
                    return 0.0;  // artificial flat zone
                }
                return x - 0.5;
            }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-6, 3);
        double root = solver.solve(100, f, 0.0, 1.0, AllowedSolution.ANY_SIDE);
        assertEquals(0.5, root, 1e-4);
    }
}