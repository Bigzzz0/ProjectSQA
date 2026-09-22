package org.apache.commons.math.estimation;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target: org.apache.commons.math.estimation.AbstractEstimator
 *
 * Decision Branches & Flow Conditions:
 * 1. updateResidualsAndCost():
 *    - (++costEvaluations > maxCostEval) -> True: throws EstimationException("maximal number of evaluations exceeded")
 *    - (++costEvaluations > maxCostEval) -> False: computes residuals and cost accumulation
 * 2. getCovariances(problem):
 *    - Jacobian setup and J^T * J accumulation:
 *      * Evaluated with unbound parameters vs. all parameters.
 *    - RealMatrixImpl.inverse() -> Success: returns inverse data matrix.
 *    - RealMatrixImpl.inverse() -> InvalidMatrixException: catches and wraps in EstimationException("singular problem")
 * 3. guessParametersErrors(problem):
 *    - (m <= p) -> True: throws EstimationException("no degrees of freedom")
 *    - (m <= p) -> False: computes chi-square scale factor c and scales covariance diagonal elements
 * 4. updateJacobian():
 *    - Nested loop over rows (measurements) and cols (parameters):
 *      jacobian[index++] = -sqrt(weight) * partial
 * 5. initializeEstimate(problem):
 *    - Resets costEvaluations = 0, jacobianEvaluations = 0, cost = Double.POSITIVE_INFINITY
 *    - Initializes jacobian and residual arrays based on problem dimensions
 *
 * Defects4J Defect Analysis (Math-100 / testBoundParameters):
 * - Ground Truth Failure: GaussNewtonEstimatorTest::testBoundParameters -> ArrayIndexOutOfBoundsException
 * - Root Cause: getCovariances() and guessParametersErrors() query problem.getAllParameters().length instead
 *   of problem.getUnboundParameters().length. When bound parameters exist, cols > unbound parameters, causing
 *   out-of-bounds indexing in the jacobian array which was allocated sized to unbound parameters only.
 * ---------------------------------------------------------------------------------------------------------
 */
public class AbstractEstimatorGeminiTest {

    // =========================================================================
    // Test Doubles & Helper Classes
    // =========================================================================

    private static class ConcreteEstimator extends AbstractEstimator {
        @Override
        public void estimate(EstimationProblem problem) throws EstimationException {
            initializeEstimate(problem);
        }

        @Override
        public void updateJacobian() {
            super.updateJacobian();
        }

        @Override
        public void updateResidualsAndCost() throws EstimationException {
            super.updateResidualsAndCost();
        }

        @Override
        public void initializeEstimate(EstimationProblem problem) {
            super.initializeEstimate(problem);
        }

        @Override
        public void incrementJacobianEvaluationsCounter() {
            super.incrementJacobianEvaluationsCounter();
        }

        public double[] getJacobianArray() {
            return jacobian;
        }

        public double[] getResidualsArray() {
            return residuals;
        }

        public double getCostValue() {
            return cost;
        }

        public int getRows() {
            return rows;
        }

        public int getCols() {
            return cols;
        }
    }

    private static class MockMeasurement extends WeightedMeasurement {
        private static final long serialVersionUID = 1L;
        private final double theoretical;
        private final Map<EstimatedParameter, Double> partials = new HashMap<>();

        public MockMeasurement(double weight, double measuredValue, double theoretical) {
            super(weight, measuredValue);
            this.theoretical = theoretical;
        }

        public void setPartial(EstimatedParameter param, double partial) {
            partials.put(param, partial);
        }

        @Override
        public double getTheoreticalValue() {
            return theoretical;
        }

        @Override
        public double getPartial(EstimatedParameter parameter) {
            Double p = partials.get(parameter);
            return (p != null) ? p : 0.0;
        }
    }

    private static class MockProblem implements EstimationProblem {
        private final EstimatedParameter[] allParameters;
        private final EstimatedParameter[] unboundParameters;
        private final WeightedMeasurement[] measurements;

        public MockProblem(EstimatedParameter[] allParameters,
                           EstimatedParameter[] unboundParameters,
                           WeightedMeasurement[] measurements) {
            this.allParameters = allParameters;
            this.unboundParameters = unboundParameters;
            this.measurements = measurements;
        }

        @Override
        public EstimatedParameter[] getAllParameters() {
            return allParameters;
        }

        @Override
        public EstimatedParameter[] getUnboundParameters() {
            return unboundParameters;
        }

        @Override
        public WeightedMeasurement[] getMeasurements() {
            return measurements;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitializationResetsCountersAndArrays() {
        ConcreteEstimator estimator = new ConcreteEstimator();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        MockMeasurement m1 = new MockMeasurement(1.0, 2.0, 1.0);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { p1 },
            new EstimatedParameter[] { p1 },
            new WeightedMeasurement[] { m1 }
        );

        estimator.setMaxCostEval(10);
        estimator.incrementJacobianEvaluationsCounter();
        assertEquals(1, estimator.getJacobianEvaluations());

        estimator.initializeEstimate(problem);

        assertEquals(0, estimator.getCostEvaluations());
        assertEquals(0, estimator.getJacobianEvaluations());
        assertEquals(Double.POSITIVE_INFINITY, estimator.getCostValue(), 1e-12);
        assertEquals(1, estimator.getRows());
        assertEquals(1, estimator.getCols());
        assertEquals(1, estimator.getJacobianArray().length);
        assertEquals(1, estimator.getResidualsArray().length);
    }

    @Test(timeout = 4000)
    public void testUpdateJacobianComputation() {
        ConcreteEstimator estimator = new ConcreteEstimator();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        EstimatedParameter p2 = new EstimatedParameter("p2", 2.0);

        MockMeasurement m1 = new MockMeasurement(4.0, 5.0, 1.0);
        m1.setPartial(p1, 2.0);
        m1.setPartial(p2, -3.0);

        MockMeasurement m2 = new MockMeasurement(9.0, 10.0, 1.0);
        m2.setPartial(p1, 0.5);
        m2.setPartial(p2, 1.5);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { p1, p2 },
            new EstimatedParameter[] { p1, p2 },
            new WeightedMeasurement[] { m1, m2 }
        );

        estimator.initializeEstimate(problem);
        estimator.updateJacobian();

        assertEquals(1, estimator.getJacobianEvaluations());
        double[] j = estimator.getJacobianArray();
        assertEquals(4, j.length);

        // Row 1: factor = -sqrt(4) = -2
        assertEquals(-2.0 * 2.0, j[0], 1e-12);
        assertEquals(-2.0 * (-3.0), j[1], 1e-12);

        // Row 2: factor = -sqrt(9) = -3
        assertEquals(-3.0 * 0.5, j[2], 1e-12);
        assertEquals(-3.0 * 1.5, j[3], 1e-12);
    }

    @Test(timeout = 4000)
    public void testUpdateResidualsAndCostComputation() throws EstimationException {
        ConcreteEstimator estimator = new ConcreteEstimator();
        estimator.setMaxCostEval(5);

        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        // Residual 1: measured(5) - theoretical(3) = 2; weight = 4 -> sqrt(4)*2 = 4; w*res^2 = 16
        MockMeasurement m1 = new MockMeasurement(4.0, 5.0, 3.0);
        // Residual 2: measured(10) - theoretical(7) = 3; weight = 1 -> sqrt(1)*3 = 3; w*res^2 = 9
        MockMeasurement m2 = new MockMeasurement(1.0, 10.0, 7.0);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { p1 },
            new EstimatedParameter[] { p1 },
            new WeightedMeasurement[] { m1, m2 }
        );

        estimator.initializeEstimate(problem);
        estimator.updateResidualsAndCost();

        assertEquals(1, estimator.getCostEvaluations());
        assertEquals(4.0, estimator.getResidualsArray()[0], 1e-12);
        assertEquals(3.0, estimator.getResidualsArray()[1], 1e-12);
        // Total cost = sqrt(16 + 9) = 5.0
        assertEquals(5.0, estimator.getCostValue(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetRMS() {
        ConcreteEstimator estimator = new ConcreteEstimator();
        // m1: weight = 1, residual = 2 -> criterion component = 1 * 4 = 4
        MockMeasurement m1 = new MockMeasurement(1.0, 3.0, 1.0);
        // m2: weight = 2, residual = 3 -> criterion component = 2 * 9 = 18
        MockMeasurement m2 = new MockMeasurement(2.0, 5.0, 2.0);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[0],
            new EstimatedParameter[0],
            new WeightedMeasurement[] { m1, m2 }
        );

        // RMS = sqrt((4 + 18) / 2) = sqrt(11)
        double rms = estimator.getRMS(problem);
        assertEquals(Math.sqrt(11.0), rms, 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetChiSquare() {
        ConcreteEstimator estimator = new ConcreteEstimator();
        // m1: weight = 2.0, residual = 4.0 -> res^2 / weight = 16 / 2 = 8.0
        MockMeasurement m1 = new MockMeasurement(2.0, 5.0, 1.0);
        // m2: weight = 0.5, residual = 1.0 -> res^2 / weight = 1.0 / 0.5 = 2.0
        MockMeasurement m2 = new MockMeasurement(0.5, 3.0, 2.0);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[0],
            new EstimatedParameter[0],
            new WeightedMeasurement[] { m1, m2 }
        );

        double chiSquare = estimator.getChiSquare(problem);
        assertEquals(10.0, chiSquare, 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetCovariancesNormal() throws EstimationException {
        ConcreteEstimator estimator = new ConcreteEstimator();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);

        MockMeasurement m1 = new MockMeasurement(1.0, 2.0, 1.0);
        m1.setPartial(p1, 2.0);
        MockMeasurement m2 = new MockMeasurement(1.0, 2.0, 1.0);
        m2.setPartial(p1, 1.0);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { p1 },
            new EstimatedParameter[] { p1 },
            new WeightedMeasurement[] { m1, m2 }
        );

        estimator.initializeEstimate(problem);

        // j[0] = -1 * 2 = -2; j[1] = -1 * 1 = -1
        // jTj = (-2)*(-2) + (-1)*(-1) = 4 + 1 = 5
        // inverse = 1 / 5 = 0.2
        double[][] cov = estimator.getCovariances(problem);
        assertNotNull(cov);
        assertEquals(1, cov.length);
        assertEquals(1, cov[0].length);
        assertEquals(0.2, cov[0][0], 1e-12);
    }

    @Test(timeout = 4000)
    public void testGuessParametersErrorsNormal() throws EstimationException {
        ConcreteEstimator estimator = new ConcreteEstimator();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);

        // 2 measurements, 1 parameter -> m(2) > p(1)
        MockMeasurement m1 = new MockMeasurement(1.0, 3.0, 1.0); // residual = 2, chi^2 comp = 4/1 = 4
        m1.setPartial(p1, 2.0);
        MockMeasurement m2 = new MockMeasurement(1.0, 2.0, 1.0); // residual = 1, chi^2 comp = 1/1 = 1
        m2.setPartial(p1, 2.0);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { p1 },
            new EstimatedParameter[] { p1 },
            new WeightedMeasurement[] { m1, m2 }
        );

        estimator.initializeEstimate(problem);

        // ChiSquare = 4 + 1 = 5.0
        // m - p = 2 - 1 = 1 -> c = sqrt(5.0 / 1) = sqrt(5.0)
        // j[0] = -2, j[1] = -2 -> jTj = 4 + 4 = 8 -> cov[0][0] = 1/8 = 0.125
        // error = sqrt(0.125) * sqrt(5.0) = sqrt(0.625)
        double[] errors = estimator.guessParametersErrors(problem);
        assertNotNull(errors);
        assertEquals(1, errors.length);
        assertEquals(Math.sqrt(0.625), errors[0], 1e-12);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testMaxCostEvalBoundaryZero() {
        ConcreteEstimator estimator = new ConcreteEstimator();
        estimator.setMaxCostEval(0);

        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        MockMeasurement m1 = new MockMeasurement(1.0, 1.0, 1.0);
        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { p1 },
            new EstimatedParameter[] { p1 },
            new WeightedMeasurement[] { m1 }
        );

        estimator.initializeEstimate(problem);
        try {
            estimator.updateResidualsAndCost();
            fail("Expected EstimationException when maxCostEval is 0 and evaluation occurs");
        } catch (EstimationException e) {
            assertTrue(e.getMessage().contains("maximal number of evaluations exceeded"));
        }
    }

    @Test(timeout = 4000)
    public void testCostEvaluationExactlyAtLimitThenExceeded() throws EstimationException {
        ConcreteEstimator estimator = new ConcreteEstimator();
        estimator.setMaxCostEval(2);

        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        MockMeasurement m1 = new MockMeasurement(1.0, 1.0, 1.0);
        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { p1 },
            new EstimatedParameter[] { p1 },
            new WeightedMeasurement[] { m1 }
        );

        estimator.initializeEstimate(problem);
        estimator.updateResidualsAndCost(); // eval 1
        assertEquals(1, estimator.getCostEvaluations());

        estimator.updateResidualsAndCost(); // eval 2 (exactly at limit)
        assertEquals(2, estimator.getCostEvaluations());

        try {
            estimator.updateResidualsAndCost(); // eval 3 (exceeds limit)
            fail("Expected EstimationException on 3rd evaluation when maxCostEval is 2");
        } catch (EstimationException e) {
            assertEquals(3, estimator.getCostEvaluations());
        }
    }

    @Test(timeout = 4000)
    public void testZeroResidualProducesZeroCost() throws EstimationException {
        ConcreteEstimator estimator = new ConcreteEstimator();
        estimator.setMaxCostEval(10);

        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        MockMeasurement m1 = new MockMeasurement(10.0, 5.0, 5.0); // residual = 0
        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { p1 },
            new EstimatedParameter[] { p1 },
            new WeightedMeasurement[] { m1 }
        );

        estimator.initializeEstimate(problem);
        estimator.updateResidualsAndCost();

        assertEquals(0.0, estimator.getCostValue(), 1e-12);
        assertEquals(0.0, estimator.getResidualsArray()[0], 1e-12);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where problem.getAllParameters().length is used instead of
     * problem.getUnboundParameters().length in getCovariances().
     * On defective versions, this throws ArrayIndexOutOfBoundsException.
     */
    @Test(timeout = 4000)
    public void testGetCovariancesWithBoundParametersTargetingDefect() throws EstimationException {
        EstimatedParameter unboundParam = new EstimatedParameter("unbound", 1.0, false);
        EstimatedParameter boundParam = new EstimatedParameter("bound", 2.0, true);

        MockMeasurement m1 = new MockMeasurement(1.0, 3.0, 1.0);
        m1.setPartial(unboundParam, 2.0);
        m1.setPartial(boundParam, 5.0);

        MockMeasurement m2 = new MockMeasurement(1.0, 4.0, 1.0);
        m2.setPartial(unboundParam, 1.0);
        m2.setPartial(boundParam, 7.0);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { unboundParam, boundParam },
            new EstimatedParameter[] { unboundParam },
            new WeightedMeasurement[] { m1, m2 }
        );

        ConcreteEstimator estimator = new ConcreteEstimator();
        estimator.initializeEstimate(problem);

        double[][] cov = estimator.getCovariances(problem);
        assertNotNull(cov);
        assertEquals(1, cov.length);
        assertEquals(1, cov[0].length);
        // j[0] = -2, j[1] = -1 -> jTj = 4 + 1 = 5 -> cov = 0.2
        assertEquals(0.2, cov[0][0], 1e-12);
    }

    /**
     * Targets degrees of freedom calculation and covariance sizing in guessParametersErrors()
     * when bound parameters are present.
     */
    @Test(timeout = 4000)
    public void testGuessParametersErrorsWithBoundParametersTargetingDefect() throws EstimationException {
        EstimatedParameter unboundParam = new EstimatedParameter("unbound", 1.0, false);
        EstimatedParameter boundParam = new EstimatedParameter("bound", 2.0, true);

        // 2 measurements, 1 unbound param, 1 bound param.
        // True degrees of freedom = m (2) - unbound (1) = 1 > 0.
        // Defective version checks m (2) <= allParams (2) and incorrectly throws "no degrees of freedom".
        MockMeasurement m1 = new MockMeasurement(1.0, 3.0, 1.0);
        m1.setPartial(unboundParam, 2.0);
        m1.setPartial(boundParam, 10.0);

        MockMeasurement m2 = new MockMeasurement(1.0, 2.0, 1.0);
        m2.setPartial(unboundParam, 1.0);
        m2.setPartial(boundParam, 20.0);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { unboundParam, boundParam },
            new EstimatedParameter[] { unboundParam },
            new WeightedMeasurement[] { m1, m2 }
        );

        ConcreteEstimator estimator = new ConcreteEstimator();
        estimator.initializeEstimate(problem);

        double[] errors = estimator.guessParametersErrors(problem);
        assertNotNull(errors);
        assertEquals(1, errors.length);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetCovariancesThrowsOnSingularMatrix() {
        ConcreteEstimator estimator = new ConcreteEstimator();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        EstimatedParameter p2 = new EstimatedParameter("p2", 2.0);

        // Identical partials make J^T * J singular (linearly dependent columns)
        MockMeasurement m1 = new MockMeasurement(1.0, 2.0, 1.0);
        m1.setPartial(p1, 1.0);
        m1.setPartial(p2, 1.0);

        MockMeasurement m2 = new MockMeasurement(1.0, 3.0, 1.0);
        m2.setPartial(p1, 2.0);
        m2.setPartial(p2, 2.0);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { p1, p2 },
            new EstimatedParameter[] { p1, p2 },
            new WeightedMeasurement[] { m1, m2 }
        );

        estimator.initializeEstimate(problem);

        try {
            estimator.getCovariances(problem);
            fail("Expected EstimationException due to singular matrix");
        } catch (EstimationException e) {
            assertTrue(e.getMessage().contains("singular problem"));
        }
    }

    @Test(timeout = 4000)
    public void testGuessParametersErrorsThrowsWhenMeasurementsEqualParameters() {
        ConcreteEstimator estimator = new ConcreteEstimator();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        EstimatedParameter p2 = new EstimatedParameter("p2", 2.0);

        // m = 2, p = 2 -> m <= p branch triggers
        MockMeasurement m1 = new MockMeasurement(1.0, 2.0, 1.0);
        MockMeasurement m2 = new MockMeasurement(1.0, 3.0, 1.0);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { p1, p2 },
            new EstimatedParameter[] { p1, p2 },
            new WeightedMeasurement[] { m1, m2 }
        );

        estimator.initializeEstimate(problem);

        try {
            estimator.guessParametersErrors(problem);
            fail("Expected EstimationException due to zero degrees of freedom (m == p)");
        } catch (EstimationException e) {
            assertTrue(e.getMessage().contains("no degrees of freedom"));
        }
    }

    @Test(timeout = 4000)
    public void testGuessParametersErrorsThrowsWhenMeasurementsLesserThanParameters() {
        ConcreteEstimator estimator = new ConcreteEstimator();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        EstimatedParameter p2 = new EstimatedParameter("p2", 2.0);

        // m = 1, p = 2 -> m < p branch triggers
        MockMeasurement m1 = new MockMeasurement(1.0, 2.0, 1.0);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { p1, p2 },
            new EstimatedParameter[] { p1, p2 },
            new WeightedMeasurement[] { m1 }
        );

        estimator.initializeEstimate(problem);

        try {
            estimator.guessParametersErrors(problem);
            fail("Expected EstimationException due to negative degrees of freedom (m < p)");
        } catch (EstimationException e) {
            assertTrue(e.getMessage().contains("no degrees of freedom"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEstimateInvocationLifecycle() throws EstimationException {
        ConcreteEstimator estimator = new ConcreteEstimator();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        MockMeasurement m1 = new MockMeasurement(1.0, 2.0, 1.0);

        MockProblem problem = new MockProblem(
            new EstimatedParameter[] { p1 },
            new EstimatedParameter[] { p1 },
            new WeightedMeasurement[] { m1 }
        );

        estimator.estimate(problem);

        assertEquals(0, estimator.getCostEvaluations());
        assertEquals(0, estimator.getJacobianEvaluations());
        assertEquals(1, estimator.getRows());
        assertEquals(1, estimator.getCols());
    }
}