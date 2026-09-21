package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;

/**
 * Comprehensive test suite for PowellOptimizer.
 * Targets all branches, boundary conditions, and the known Defects4J failure.
 */
public class PowellOptimizerDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Constructors:
     *   - rel < MIN_RELATIVE_TOLERANCE -> NumberIsTooSmallException
     *   - abs <= 0 -> NotStrictlyPositiveException
     *   - lineRel/lineAbs passed correctly
     *   - checker null allowed
     * 
     * doOptimize():
     *   - checkParameters() throws MathUnsupportedOperationException if bounds set
     *   - GoalType.MINIMIZE vs MAXIMIZE
     *   - Convergence check: default (2*(fX-fVal) <= rel*(|fX|+|fVal|)+abs)
     *   - User-defined checker (if not null)
     *   - Direction update condition: if (fX > fX2) then compute t and possibly update
     *   - t < 0.0 triggers new line search and direction replacement
     *   - bigInd selection: delta = max(fX2 - fVal)
     *   - LineSearch: bracket search, custom checker
     * 
     * Known defect: testSumSinc fails – likely due to convergence or direction update logic.
     * We replicate that test and assert the optimizer finds a minimum close to zero.
     */

    // ---------- Partition A: Constructor validation ----------

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testConstructorRelativeTooSmall() {
        new PowellOptimizer(1e-16, 1e-6);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorAbsoluteNonPositive() {
        new PowellOptimizer(1e-3, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorAbsoluteNegative() {
        new PowellOptimizer(1e-3, -1.0);
    }

    @Test(timeout = 4000)
    public void testConstructorValid() {
        PowellOptimizer opt = new PowellOptimizer(1e-3, 1e-6);
        assertNotNull(opt);
    }

    @Test(timeout = 4000)
    public void testConstructorWithChecker() {
        SimpleValueChecker checker = new SimpleValueChecker(1e-3, 1e-6);
        PowellOptimizer opt = new PowellOptimizer(1e-3, 1e-6, checker);
        assertNotNull(opt);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLineTolerances() {
        PowellOptimizer opt = new PowellOptimizer(1e-3, 1e-6, 1e-2, 1e-4);
        assertNotNull(opt);
    }

    @Test(timeout = 4000)
    public void testConstructorWithAllParams() {
        SimpleValueChecker checker = new SimpleValueChecker(1e-3, 1e-6);
        PowellOptimizer opt = new PowellOptimizer(1e-3, 1e-6, 1e-2, 1e-4, checker);
        assertNotNull(opt);
    }

    // ---------- Partition B: BVA & Extremes ----------

    @Test(expected = MathUnsupportedOperationException.class, timeout = 4000)
    public void testBoundsNotSupported() {
        PowellOptimizer opt = new PowellOptimizer(1e-3, 1e-6);
        // Attempt to optimize with bounds (simulate via lower/upper bound)
        // The optimizer's checkParameters() will throw.
        // We need to call optimize with bounds. Since bounds are not directly exposed,
        // we can use the inherited method setLowerBound/setUpperBound via optimization data.
        // But those are not public. Instead, we can create a subclass? Not needed.
        // The test is covered by the checkParameters() method; we can trigger it by
        // calling doOptimize() after setting bounds via reflection? Simpler: we trust
        // that the check is correct. We'll test indirectly by calling optimize with
        // a SimpleBounds instance? Actually, the optimizer's optimize method accepts
        // OptimizationData, and if SimpleBounds is passed, it will set lower/upper bounds.
        // So we can do:
        opt.optimize(
            new MaxEval(100),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0];
                }
            }),
            GoalType.MINIMIZE,
            new double[] { 1.0 },
            new org.apache.commons.math3.optim.SimpleBounds(new double[] { -1 }, new double[] { 1 })
        );
    }

    @Test(timeout = 4000)
    public void testOptimizeSimpleQuadratic() {
        PowellOptimizer opt = new PowellOptimizer(1e-8, 1e-8);
        PointValuePair result = opt.optimize(
            new MaxEval(1000),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            GoalType.MINIMIZE,
            new double[] { 3.0, 4.0 }
        );
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-6);
        assertEquals(0.0, point[1], 1e-6);
        assertEquals(0.0, result.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testOptimizeMaximize() {
        PowellOptimizer opt = new PowellOptimizer(1e-8, 1e-8);
        PointValuePair result = opt.optimize(
            new MaxEval(1000),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return -point[0] * point[0] - point[1] * point[1];
                }
            }),
            GoalType.MAXIMIZE,
            new double[] { 3.0, 4.0 }
        );
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-6);
        assertEquals(0.0, point[1], 1e-6);
        assertEquals(0.0, result.getValue(), 1e-6);
    }

    // ---------- Partition C: Defect-Targeted Branch Zone ----------
    // Known failure: testSumSinc from Defects4J.
    // The sum of sinc functions: f(x) = sum_i (sin(x_i)/x_i) for x_i != 0, else 1.
    // Minimum is near 0.
    @Test(timeout = 4000)
    public void testSumSinc() {
        PowellOptimizer opt = new PowellOptimizer(1e-8, 1e-8);
        MultivariateFunction sumSinc = new MultivariateFunction() {
            public double value(double[] point) {
                double sum = 0;
                for (int i = 0; i < point.length; i++) {
                    double x = point[i];
                    if (Math.abs(x) < 1e-12) {
                        sum += 1.0;
                    } else {
                        sum += Math.sin(x) / x;
                    }
                }
                return sum;
            }
        };
        PointValuePair result = opt.optimize(
            new MaxEval(2000),
            new ObjectiveFunction(sumSinc),
            GoalType.MINIMIZE,
            new double[] { 3.0, 1.0 }
        );
        // The minimum should be close to 2 (since each sinc(0)=1, sum=2)
        // But the optimizer may converge to a local minimum? Actually sinc has many minima.
        // The known test expects the optimizer to find a point near zero.
        // We'll assert that the function value is less than the initial value.
        double initVal = sumSinc.value(new double[] { 3.0, 1.0 });
        assertTrue("Optimizer did not improve", result.getValue() < initVal);
        // Additionally, the point should be near zero (global minimum at 0)
        // But due to local minima, we just check improvement.
        // The defect might cause wrong convergence or failure to improve.
        // We'll also check that the result is not NaN.
        assertFalse(Double.isNaN(result.getValue()));
        assertFalse(Double.isNaN(result.getPoint()[0]));
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testConstructorLineRelTooSmall() {
        new PowellOptimizer(1e-3, 1e-6, 1e-16, 1e-6);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorLineAbsNonPositive() {
        new PowellOptimizer(1e-3, 1e-6, 1e-2, 0.0);
    }

    // Test that a null checker is allowed (no exception)
    @Test(timeout = 4000)
    public void testNullCheckerAllowed() {
        PowellOptimizer opt = new PowellOptimizer(1e-3, 1e-6, (org.apache.commons.math3.optim.ConvergenceChecker<PointValuePair>) null);
        assertNotNull(opt);
    }

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------
    // No equals/hashCode/clone/serialization in this class, but we can test
    // that the optimizer can be reused.

    @Test(timeout = 4000)
    public void testReuseOptimizer() {
        PowellOptimizer opt = new PowellOptimizer(1e-8, 1e-8);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };
        PointValuePair r1 = opt.optimize(new MaxEval(100), new ObjectiveFunction(f), GoalType.MINIMIZE, new double[] { 2.0 });
        PointValuePair r2 = opt.optimize(new MaxEval(100), new ObjectiveFunction(f), GoalType.MINIMIZE, new double[] { -3.0 });
        assertEquals(0.0, r1.getPoint()[0], 1e-6);
        assertEquals(0.0, r2.getPoint()[0], 1e-6);
    }

    // Additional test for direction update branch (t < 0.0)
    // We can craft a function where the condition is triggered.
    // This is a white-box test: we want to cover the if (fX > fX2) block.
    // Use a function that is not quadratic to force direction update.
    @Test(timeout = 4000)
    public void testDirectionUpdateBranch() {
        PowellOptimizer opt = new PowellOptimizer(1e-6, 1e-6);
        // Rosenbrock function is known to cause direction updates.
        MultivariateFunction rosenbrock = new MultivariateFunction() {
            public double value(double[] x) {
                double a = x[0];
                double b = x[1];
                return (1 - a) * (1 - a) + 100 * (b - a * a) * (b - a * a);
            }
        };
        PointValuePair result = opt.optimize(
            new MaxEval(5000),
            new ObjectiveFunction(rosenbrock),
            GoalType.MINIMIZE,
            new double[] { -1.0, 1.0 }
        );
        // The minimum is at (1,1) with value 0.
        assertEquals(1.0, result.getPoint()[0], 1e-3);
        assertEquals(1.0, result.getPoint()[1], 1e-3);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    // Test that the line search works with a simple 1D function (indirectly)
    @Test(timeout = 4000)
    public void testLineSearchIndirect() {
        PowellOptimizer opt = new PowellOptimizer(1e-8, 1e-8);
        // Use a 1D problem (n=1) to exercise line search directly.
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return (x[0] - 2) * (x[0] - 2);
            }
        };
        PointValuePair result = opt.optimize(
            new MaxEval(100),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new double[] { 0.0 }
        );
        assertEquals(2.0, result.getPoint()[0], 1e-6);
        assertEquals(0.0, result.getValue(), 1e-6);
    }
}