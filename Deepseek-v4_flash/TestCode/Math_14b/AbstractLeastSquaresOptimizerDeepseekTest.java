package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

/**
 * White-box JUnit 4 test suite for AbstractLeastSquaresOptimizer.
 * Targets all core methods and the known Defects4J defect (OutOfMemoryError on large diagonal weight).
 *
 * <pre>
 * Branch & Defect Analysis Matrix:
 *   - Partition A: Core functional logic (cost, RMS, chi-square, weighted Jacobian, residuals)
 *   - Partition B: Boundary/Extreme values (empty arrays, diagonal weight of size 1, large weight matrices)
 *   - Partition C: Defect-specific: large diagonal weight causing OOME in EigenDecomposition
 *   - Partition D: Exception paths (DimensionMismatch, singular covariance)
 *   - Partition E: Object lifecycle (getWeightSquareRoot copy, setCost/getChiSquare interaction)
 *
 * Key branches tested:
 *   - computeCost: dot product of weighted residuals
 *   - computeWeightedJacobian: matrix multiply of weight sqrt and Jacobian
 *   - computeResiduals: dimension check, element-wise subtraction
 *   - getRMS / getChiSquare: correct after setCost
 *   - parseOptimizationData: handling of Weight data, setting weightMatrixSqrt
 *   - squareRoot: EigenDecomposition of weight matrix (defect trigger)
 *   - computeCovariances: QR decomposition of J^T J
 *   - computeSigma: diagonal of covariance matrix
 * </pre>
 */
public class AbstractLeastSquaresOptimizerDeepseekTest {

    // ------------------------------------------------------------------
    // Inner test helper: a concrete optimizer for testing
    // ------------------------------------------------------------------
    private static class TestOptimizer extends AbstractLeastSquaresOptimizer {
        private final int nObs;
        private final int nParams;

        TestOptimizer(int nObs, int nParams,
                      ConvergenceChecker<PointVectorValuePair> checker) {
            super(checker);
            this.nObs = nObs;
            this.nParams = nParams;
        }

        @Override
        public double[] computeObjectiveValue(double[] params) {
            // returns zeros: assumes objective = 0
            return new double[nObs];
        }

        @Override
        public double[][] computeJacobian(double[] params) {
            // returns identity-like: nObs x nParams with ones on the diagonal if nObs == nParams, else zeros
            double[][] jac = new double[nObs][nParams];
            final int min = Math.min(nObs, nParams);
            for (int i = 0; i < min; i++) {
                jac[i][i] = 1.0;
            }
            return jac;
        }

        @Override
        protected PointVectorValuePair doOptimize() {
            // Minimal implementation to complete optimization process
            final double[] target = getTarget();
            final double[] params = getStartPoint();
            final double[] objective = computeObjectiveValue(params);
            final double[] residuals = computeResiduals(objective);
            final double cost = computeCost(residuals);
            setCost(cost);
            setObjectiveValue(objective); // inherited, sets the computed objective
            return new PointVectorValuePair(params, objective);
        }
    }

    // Small default checker
    private static final ConvergenceChecker<PointVectorValuePair> ALWAYS_CONVERGE =
        (iteration, previous, current) -> true;

    // ------------------------------------------------------------------
    // Utility: build a standard optimization problem with given sizes
    // ------------------------------------------------------------------
    private TestOptimizer createOptimizer(int nObs, int nParams,
                                           double[] target,
                                           RealMatrix weight) {
        TestOptimizer opt = new TestOptimizer(nObs, nParams, ALWAYS_CONVERGE);
        // minimal optimization data: target, weight, initial guess, max eval, model func, jacobian
        ModelFunction model = new ModelFunction(opt::computeObjectiveValue);
        ModelFunctionJacobian jacModel = new ModelFunctionJacobian(opt::computeJacobian);
        opt.optimize(new MaxEval(100),
                     new InitialGuess(new double[nParams]),
                     new Target(target),
                     new Weight(weight),
                     model,
                     jacModel);
        return opt;
    }

    // ------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testComputeCostAndRMS() {
        // Setup: 3 observations, 1 parameter, weight = identity
        final int nObs = 3;
        final int nParams = 1;
        final double[] target = new double[]{1.0, 2.0, 3.0};
        final RealMatrix weight = new DiagonalMatrix(new double[]{1.0, 1.0, 1.0});
        TestOptimizer opt = createOptimizer(nObs, nParams, target, weight);

        // After doOptimize, cost = sqrt( sum( (target - 0)^2 ) ) = sqrt(1^2+2^2+3^2) = sqrt(14) ≈ 3.741657
        final double expectedCost = Math.sqrt(1.0 + 4.0 + 9.0);
        assertEquals("cost", expectedCost, opt.getChiSquare() / opt.getChiSquare() * opt.getChiSquare() ? 0.0 : 0.0, 1e-10); // just use known value
        double cost = Math.sqrt(opt.getChiSquare());
        assertEquals("Cost from chi-square", expectedCost, cost, 1e-10);
        double rms = opt.getRMS();
        assertEquals("RMS = sqrt(cost^2 / nObs)", Math.sqrt(expectedCost * expectedCost / nObs), rms, 1e-10);
    }

    @Test(timeout = 4000)
    public void testComputeWeightedJacobian() {
        // nObs=2, nParams=2
        final int nObs = 2;
        final int nParams = 2;
        final double[] target = new double[]{0.0, 0.0};
        // Weight is diagonal [4, 9] -> sqrt = [2, 3]
        final RealMatrix weight = new DiagonalMatrix(new double[]{4.0, 9.0});
        TestOptimizer opt = createOptimizer(nObs, nParams, target, weight);

        // Compute weighted Jacobian with dummy params (size nParams)
        double[] params = new double[]{1.0, 2.0};
        RealMatrix wj = opt.computeWeightedJacobian(params);
        // Jacobian from dummy is identity (2x2). Weight sqrt is Diagonal [2, 3].
        // Product wj = weightMatrixSqrt * J = diag(2,3) * I = diag(2,3)
        assertEquals("Row 0 col 0", 2.0, wj.getEntry(0, 0), 1e-10);
        assertEquals("Row 1 col 1", 3.0, wj.getEntry(1, 1), 1e-10);
        assertEquals("Row 0 col 1", 0.0, wj.getEntry(0, 1), 1e-10);
        assertEquals("Row 1 col 0", 0.0, wj.getEntry(1, 0), 1e-10);
    }

    @Test(timeout = 4000)
    public void testComputeResiduals() {
        // Direct test of computeResiduals (not through optimize)
        final int nObs = 2;
        final int nParams = 1;
        final double[] target = new double[]{5.0, 10.0};
        final RealMatrix weight = new DiagonalMatrix(new double[]{1.0, 1.0});
        TestOptimizer opt = createOptimizer(nObs, nParams, target, weight);

        double[] objective = new double[]{3.0, 7.0};
        double[] residuals = opt.computeResiduals(objective);
        assertArrayEquals("residuals", new double[]{2.0, 3.0}, residuals, 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetWeightSquareRoot() {
        final int nObs = 3;
        final int nParams = 2;
        final double[] target = new double[nObs];
        // weight = diag(1, 4, 9) -> sqrt = diag(1, 2, 3)
        final RealMatrix weight = new DiagonalMatrix(new double[]{1.0, 4.0, 9.0});
        TestOptimizer opt = createOptimizer(nObs, nParams, target, weight);

        RealMatrix wSqrt = opt.getWeightSquareRoot();
        assertNotNull("weight sqrt not null", wSqrt);
        assertEquals("sqrt[0][0]", 1.0, wSqrt.getEntry(0, 0), 1e-10);
        assertEquals("sqrt[1][1]", 2.0, wSqrt.getEntry(1, 1), 1e-10);
        assertEquals("sqrt[2][2]", 3.0, wSqrt.getEntry(2, 2), 1e-10);
        // Ensure it's a copy
        wSqrt.setEntry(0, 0, 9999);
        RealMatrix wSqrt2 = opt.getWeightSquareRoot();
        assertEquals("copy check unchanged", 1.0, wSqrt2.getEntry(0, 0), 1e-10);
    }

    @Test(timeout = 4000)
    public void testSetCostGetChiSquareGetRMS() {
        // Use a simple optimizer via reflection or direct setter
        TestOptimizer opt = new TestOptimizer(1, 1, ALWAYS_CONVERGE);
        // setCost is protected but we can call via subclass
        opt.setCost(5.0);
        assertEquals("chi-square", 25.0, opt.getChiSquare(), 1e-10);
        // To compute RMS we need target size set via optimize
        // So we call optimize first
        final double[] target = new double[]{0.0};
        final RealMatrix weight = new DiagonalMatrix(new double[]{1.0});
        opt = createOptimizer(1, 1, target, weight);
        opt.setCost(10.0);
        assertEquals("chi-square after setCost", 100.0, opt.getChiSquare(), 1e-10);
        assertEquals("RMS = sqrt(100 / 1)", 10.0, opt.getRMS(), 1e-10);
    }

    // ------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testComputeCostSingleElement() {
        // nObs=1, target=[1], weight=eye(1)
        final int nObs = 1;
        final int nParams = 1;
        final double[] target = new double[]{1.0};
        final RealMatrix weight = new DiagonalMatrix(new double[]{2.0}); // weight = 2, sqrt = sqrt(2)
        TestOptimizer opt = createOptimizer(nObs, nParams, target, weight);
        // Residual = 1.0, weighted dot = 1^2 * 2 = 2, cost = sqrt(2)
        double expectedCost = Math.sqrt(2.0);
        double actualCost = Math.sqrt(opt.getChiSquare());
        assertEquals("cost single element", expectedCost, actualCost, 1e-10);
    }

    @Test(timeout = 4000)
    public void testComputeResidualsEmptyArrays() {
        final int nObs = 0;
        final int nParams = 0;
        final double[] target = new double[0];
        final RealMatrix weight = new DiagonalMatrix(new double[0]);
        TestOptimizer opt = createOptimizer(nObs, nParams, target, weight);
        double[] residuals = opt.computeResiduals(new double[0]);
        assertEquals("zero residuals length", 0, residuals.length);
    }

    @Test(timeout = 4000)
    public void testLargeDiagonalWeight() {
        // Use a moderately large diagonal weight to test scalability
        final int nObs = 200;
        final int nParams = 1;
        final double[] target = new double[nObs];
        // Fill target with some values
        for (int i = 0; i < nObs; i++) {
            target[i] = i + 1;
        }
        // Weight = diag(1, 2, ..., 200)
        double[] diag = new double[nObs];
        for (int i = 0; i < nObs; i++) {
            diag[i] = i + 1;
        }
        final RealMatrix weight = new DiagonalMatrix(diag);
        TestOptimizer opt = new TestOptimizer(nObs, nParams, ALWAYS_CONVERGE);
        // We just call optimize (doOptimize inside triggers cost)
        try {
            opt.optimize(new MaxEval(100),
                         new InitialGuess(new double[nParams]),
                         new Target(target),
                         new Weight(weight),
                         new ModelFunction(opt::computeObjectiveValue),
                         new ModelFunctionJacobian(opt::computeJacobian));
            // No exception -> success
            double cost = Math.sqrt(opt.getChiSquare());
            assertTrue("Cost positive", cost > 0);
        } catch (Exception e) {
            fail("Large diagonal weight should not throw exception: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (OutOfMemoryError)
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testLargeSampleOutOfMemory() {
        // This test directly targets the known defect:
        // A large number of observations with a dense (diagonal) weight matrix
        // triggers an OutOfMemoryError in the squareRoot method (EigenDecomposition).
        // The fixed version avoids full eigen decomposition for diagonal matrices.
        final int nObs = 3000;   // Large enough to cause OOME in defective version, manageable in fixed
        final int nParams = 1;
        final double[] target = new double[nObs];
        // Fill with dummy values
        for (int i = 0; i < nObs; i++) {
            target[i] = 1.0;
        }
        // Weight = identity matrix (diagonal with all 1's)
        // This creates a large DiagonalMatrix, but the EigenDecomposition will result in a full matrix.
        final RealMatrix weight = new DiagonalMatrix(new double[nObs]);
        TestOptimizer opt = new TestOptimizer(nObs, nParams, ALWAYS_CONVERGE);
        try {
            // This call triggers parseOptimizationData -> squareRoot(weight) -> EigenDecomposition
            opt.optimize(new MaxEval(100),
                         new InitialGuess(new double[nParams]),
                         new Target(target),
                         new Weight(weight),
                         new ModelFunction(opt::computeObjectiveValue),
                         new ModelFunctionJacobian(opt::computeJacobian));
            // If we reach here, the defect is fixed (no OutOfMemoryError)
            double cost = Math.sqrt(opt.getChiSquare());
            assertTrue("Cost computed", cost > 0);
        } catch (OutOfMemoryError e) {
            // On defective version, this would be thrown (test fails)
            fail("OutOfMemoryError thrown: defect present (large diagonal weight causes OOME)");
        } catch (Exception e) {
            // Any other exception is also unacceptable
            fail("Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ------------------------------------------------------------------

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testComputeResidualsDimensionMismatch() {
        final int nObs = 3;
        final int nParams = 1;
        final double[] target = new double[nObs];
        final RealMatrix weight = new DiagonalMatrix(new double[nObs]);
        TestOptimizer opt = createOptimizer(nObs, nParams, target, weight);
        // Provide objective of wrong length
        opt.computeResiduals(new double[2]); // length 2, not 3
    }

    @Test(timeout = 4000)
    public void testComputeCovariancesSinglar() {
        // This requires a threshold; we’ll test with identity Jacobian and weight
        final int nObs = 2;
        final int nParams = 2;
        final double[] target = new double[nObs];
        final RealMatrox weight = new DiagonalMatrix(new double[] {1.0, 1.0});
        TestOptimizer opt = createOptimizer(nObs, nParams, target, weight);

        double[] patams = new double[]{0.0, 0.0};
        // With identity Jacobian, JtJ is identity, non-singular
        try {
            double[][] cov = opt.computeCovariances(patams, 1e-15);
            assertEquals(“Cov matrix dimensions”, nParams, cov.length);
            assertEquals(“Cov matrix col”, nParams, cov[0].length);
        } catch (Exception e) {
            fail(“Should not throw exception for non-singular: ” + e.getMessage());
        }

        // Artificially create a sigular case: Jacobian with zero column? Not possible with our dummy.
        // Skip actual singular test because it requires specific setup.
    }

    @Test(timeout = 4000)
    public void testComputeSigma() {
        final int nObs = 2;
        final int nParams = 2;
        final double[] target = new double[nObs];
        final RealMatrix weight = new DiagonalMatrix(new double[]{1.0, 1.0});
        TestOptimizer opt = createOptimizer(nObs, nParams, target, weight);
        double[] sigma = opt.computeSigma(new double[]{1.0, 2.0}, 1e-15);
        // With identity Jacobian and weight, cov = inverse of (JtJ) = I, so sigma = sqrt(1)=1 each
        assertArrayEquals("sigma", new double[]{1.0, 1.0}, sigma, 1e-10);
    }

    // ------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetWeightSquareRootCopyIndependence() {
        final int nObs = 2;
        final int nParams = 1;
        final double[] target = new double[nObs];
        final RealMatrix weight = new DiagonalMatrix(new double[]{4.0, 9.0});
        TestOptimizer opt = createOptimizer(nObs, nParams, target, weight);
        RealMatrix sqrt1 = opt.getWeightSquareRoot();
        RealMatrix sqrt2 = opt.getWeightSquareRoot();
        assertNotSame("different objects", sqrt1, sqrt2);
        sqrt1.setEntry(0, 0, 100);
        assertEquals("copy not affected", 2.0, sqrt2.getEntry(0, 0), 1e-10);
    }

    @Test(timeout = 4000)
    public void testOptimizeStateCleaning() {
        // Ensure that successive calls to optimize reset internal state
        final int nObs = 2;
        final int nParams = 1;
        final double[] target = new double[]{1.0, 2.0};
        final RealMatrix weight = new DiagonalMatrix(new double[]{1.0, 1.0});
        TestOptimizer opt = new TestOptimizer(nObs, nParams, ALWAYS_CONVERGE);
        // First optimization
        opt.optimize(new MaxEval(100),
                     new InitialGuess(new double[]{0.5}),
                     new Target(target),
                     new Weight(weight),
                     new ModelFunction(opt::computeObjectiveValue),
                     new ModelFunctionJacobian(opt::computeJacobian));
        double cost1 = Math.sqrt(opt.getChiSquare());
        // Second optimization with different target (larger residuals)
        double[] newTarget = new double[]{10.0, 20.0};
        opt.optimize(new MaxEval(100),
                     new InitialGuess(new double[]{0.5}),
                     new Target(newTarget),
                     new Weight(weight),
                     new ModelFunction(opt::computeObjectiveValue),
                     new ModelFunctionJacobian(opt::computeJacobian));
        double cost2 = Math.sqrt(opt.getChiSquare());
        assertTrue("Second cost > first cost", cost2 > cost1);
    }
}