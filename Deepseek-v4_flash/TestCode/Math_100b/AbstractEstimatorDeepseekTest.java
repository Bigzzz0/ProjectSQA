package org.apache.commons.math.estimation;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: AbstractEstimator.java
 * 
 * Decision branches and boundary conditions covered:
 * 1. setMaxCostEval: normal positive value, zero, negative value
 * 2. getCostEvaluations/getJacobianEvaluations: initial state (0), after increments
 * 3. updateJacobian: 
 *    - normal case with multiple measurements/parameters
 *    - zero measurements (empty array)
 *    - zero parameters (empty array)
 *    - verifies jacobian array is zeroed before filling
 *    - verifies correct sign (negative factor) and weight scaling
 * 4. incrementJacobianEvaluationsCounter: multiple calls
 * 5. updateResidualsAndCost:
 *    - normal case with valid measurements
 *    - boundary: exactly at maxCostEval (should pass)
 *    - boundary: exceeding maxCostEval (should throw EstimationException)
 *    - verifies cost calculation (sqrt of weighted sum of squared residuals)
 *    - verifies residuals array is correctly populated
 * 6. getRMS:
 *    - normal case with multiple measurements
 *    - single measurement
 *    - empty measurements array (division by zero - potential issue)
 *    - zero weights
 * 7. getChiSquare:
 *    - normal case
 *    - single measurement
 *    - empty measurements array
 *    - zero weights (division by zero - potential issue)
 * 8. getCovariances:
 *    - normal case with valid jacobian
 *    - singular matrix (should throw EstimationException)
 *    - verifies symmetry of jTj matrix
 * 9. guessParametersErrors:
 *    - normal case (m > p)
 *    - boundary: m == p (should throw EstimationException)
 *    - boundary: m < p (should throw EstimationException)
 *    - verifies error calculation
 * 10. initializeEstimate:
 *    - resets counters to zero
 *    - sets measurements, parameters, rows, cols
 *    - allocates jacobian and residuals arrays
 *    - sets cost to POSITIVE_INFINITY
 * 
 * Defect targeting:
 * - ArrayIndexOutOfBoundsException in testBoundParameters scenario
 * - This occurs when getCovariances or guessParametersErrors is called
 *   with a problem where the number of measurements/parameters causes
 *   an index out of bounds in the jacobian array access
 * - The defect is likely in the jacobian indexing when rows*cols
 *   doesn't match the actual jacobian array size
 */
public class AbstractEstimatorDeepseekTest {

    // Test implementation of AbstractEstimator for testing purposes
    private static class TestEstimator extends AbstractEstimator {
        @Override
        public void estimate(EstimationProblem problem) throws EstimationException {
            // No-op implementation for testing
        }
    }

    // Simple test problem implementation
    private static class TestProblem implements EstimationProblem {
        private WeightedMeasurement[] measurements;
        private EstimatedParameter[] parameters;
        private EstimatedParameter[] unboundParameters;

        public TestProblem(WeightedMeasurement[] measurements, EstimatedParameter[] parameters) {
            this.measurements = measurements;
            this.parameters = parameters;
            this.unboundParameters = parameters;
        }

        @Override
        public WeightedMeasurement[] getMeasurements() {
            return measurements;
        }

        @Override
        public EstimatedParameter[] getAllParameters() {
            return parameters;
        }

        @Override
        public EstimatedParameter[] getUnboundParameters() {
            return unboundParameters;
        }
    }

    // Simple WeightedMeasurement implementation
    private static class TestMeasurement extends WeightedMeasurement {
        private double partial;
        private double residual;

        public TestMeasurement(double weight, double partial, double residual) {
            super(weight, residual);
            this.partial = partial;
            this.residual = residual;
        }

        @Override
        public double getPartial(EstimatedParameter parameter) {
            return partial;
        }

        @Override
        public double getResidual() {
            return residual;
        }
    }

    // Simple EstimatedParameter implementation
    private static class TestParameter extends EstimatedParameter {
        public TestParameter(String name, double estimate) {
            super(name, estimate);
        }
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testSetMaxCostEvalAndGetCostEvaluations() {
        TestEstimator estimator = new TestEstimator();
        
        // Initial state
        assertEquals(0, estimator.getCostEvaluations());
        assertEquals(0, estimator.getJacobianEvaluations());
        
        // Set max cost eval
        estimator.setMaxCostEval(100);
        
        // Verify counters still zero
        assertEquals(0, estimator.getCostEvaluations());
        assertEquals(0, estimator.getJacobianEvaluations());
    }

    @Test(timeout = 4000)
    public void testIncrementJacobianEvaluationsCounter() {
        TestEstimator estimator = new TestEstimator();
        
        assertEquals(0, estimator.getJacobianEvaluations());
        
        // Call protected method via reflection or subclass
        estimator.incrementJacobianEvaluationsCounter();
        assertEquals(1, estimator.getJacobianEvaluations());
        
        estimator.incrementJacobianEvaluationsCounter();
        estimator.incrementJacobianEvaluationsCounter();
        assertEquals(3, estimator.getJacobianEvaluations());
    }

    @Test(timeout = 4000)
    public void testInitializeEstimate() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 2.0, 0.5),
            new TestMeasurement(2.0, 3.0, -0.5)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        // Verify counters reset
        assertEquals(0, estimator.getCostEvaluations());
        assertEquals(0, estimator.getJacobianEvaluations());
        
        // Verify arrays allocated
        assertEquals(2, estimator.rows);
        assertEquals(2, estimator.cols);
        assertEquals(4, estimator.jacobian.length);
        assertEquals(2, estimator.residuals.length);
        
        // Verify cost initialized to infinity
        assertEquals(Double.POSITIVE_INFINITY, estimator.cost, 0.0);
        
        // Verify measurements and parameters set
        assertSame(measurements, estimator.measurements);
        assertSame(parameters, estimator.parameters);
    }

    @Test(timeout = 4000)
    public void testUpdateJacobianNormalCase() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(4.0, 2.0, 0.5),  // weight=4, partial=2
            new TestMeasurement(9.0, 3.0, -0.5)  // weight=9, partial=3
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        estimator.updateJacobian();
        
        // Verify jacobian evaluations counter incremented
        assertEquals(1, estimator.getJacobianEvaluations());
        
        // Expected jacobian values:
        // factor = -sqrt(weight)
        // jacobian[i*cols + j] = factor * partial
        // For measurement 0: factor = -sqrt(4) = -2
        //   jacobian[0] = -2 * 2 = -4
        //   jacobian[1] = -2 * 2 = -4 (same partial for both params)
        // For measurement 1: factor = -sqrt(9) = -3
        //   jacobian[2] = -3 * 3 = -9
        //   jacobian[3] = -3 * 3 = -9
        
        assertEquals(-4.0, estimator.jacobian[0], 1e-10);
        assertEquals(-4.0, estimator.jacobian[1], 1e-10);
        assertEquals(-9.0, estimator.jacobian[2], 1e-10);
        assertEquals(-9.0, estimator.jacobian[3], 1e-10);
    }

    @Test(timeout = 4000)
    public void testUpdateJacobianZeroesArrayFirst() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 2.0, 0.5)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        // Pre-fill jacobian with garbage
        estimator.jacobian[0] = 99.0;
        
        estimator.updateJacobian();
        
        // Verify jacobian was zeroed and then filled
        double expected = -Math.sqrt(1.0) * 2.0;
        assertEquals(expected, estimator.jacobian[0], 1e-10);
    }

    @Test(timeout = 4000)
    public void testUpdateResidualsAndCostNormalCase() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        estimator.setMaxCostEval(10);
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(4.0, 2.0, 0.5),   // weight=4, residual=0.5
            new TestMeasurement(9.0, 3.0, -0.5)   // weight=9, residual=-0.5
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        estimator.updateResidualsAndCost();
        
        // Verify cost evaluations counter incremented
        assertEquals(1, estimator.getCostEvaluations());
        
        // Expected residuals: sqrt(weight) * residual
        // residual[0] = sqrt(4) * 0.5 = 2 * 0.5 = 1.0
        // residual[1] = sqrt(9) * (-0.5) = 3 * (-0.5) = -1.5
        assertEquals(1.0, estimator.residuals[0], 1e-10);
        assertEquals(-1.5, estimator.residuals[1], 1e-10);
        
        // Expected cost: sqrt(sum(weight * residual^2))
        // = sqrt(4 * 0.25 + 9 * 0.25) = sqrt(1 + 2.25) = sqrt(3.25)
        assertEquals(Math.sqrt(3.25), estimator.cost, 1e-10);
    }

    @Test(timeout = 4000)
    public void testUpdateResidualsAndCostAtMaxBoundary() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        estimator.setMaxCostEval(2);
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.1)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        // First call - should succeed
        estimator.updateResidualsAndCost();
        assertEquals(1, estimator.getCostEvaluations());
        
        // Second call - should succeed (at boundary)
        estimator.updateResidualsAndCost();
        assertEquals(2, estimator.getCostEvaluations());
    }

    @Test(timeout = 4000)
    public void testUpdateResidualsAndCostExceedsMax() {
        TestEstimator estimator = new TestEstimator();
        estimator.setMaxCostEval(1);
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.1)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        // First call - should succeed
        try {
            estimator.updateResidualsAndCost();
        } catch (EstimationException e) {
            fail("First call should not throw");
        }
        
        // Second call - should throw EstimationException
        try {
            estimator.updateResidualsAndCost();
            fail("Expected EstimationException");
        } catch (EstimationException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetRMSNormalCase() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(4.0, 2.0, 0.5),   // weight=4, residual=0.5
            new TestMeasurement(9.0, 3.0, -0.5)   // weight=9, residual=-0.5
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        // criterion = sum(weight * residual^2) = 4*0.25 + 9*0.25 = 3.25
        // RMS = sqrt(3.25 / 2) = sqrt(1.625)
        double expected = Math.sqrt(3.25 / 2.0);
        assertEquals(expected, estimator.getRMS(problem), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetRMSSingleMeasurement() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(4.0, 2.0, 0.5)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        // criterion = 4 * 0.25 = 1.0
        // RMS = sqrt(1.0 / 1) = 1.0
        assertEquals(1.0, estimator.getRMS(problem), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetChiSquareNormalCase() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(4.0, 2.0, 0.5),   // weight=4, residual=0.5
            new TestMeasurement(9.0, 3.0, -0.5)   // weight=9, residual=-0.5
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        // chiSquare = sum(residual^2 / weight) = 0.25/4 + 0.25/9 = 0.0625 + 0.02778
        double expected = 0.25/4.0 + 0.25/9.0;
        assertEquals(expected, estimator.getChiSquare(problem), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetChiSquareSingleMeasurement() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(4.0, 2.0, 0.5)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        // chiSquare = 0.25 / 4 = 0.0625
        assertEquals(0.0625, estimator.getChiSquare(problem), 1e-10);
    }

    // ========== Partition B: Boundary Value Analysis (BVA) & Extremes ==========

    @Test(timeout = 4000)
    public void testSetMaxCostEvalZero() {
        TestEstimator estimator = new TestEstimator();
        estimator.setMaxCostEval(0);
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.1)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        // First call should throw because 1 > 0
        try {
            estimator.updateResidualsAndCost();
            fail("Expected EstimationException");
        } catch (EstimationException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testSetMaxCostEvalNegative() {
        TestEstimator estimator = new TestEstimator();
        estimator.setMaxCostEval(-5);
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.1)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        // First call should throw because 1 > -5
        try {
            estimator.updateResidualsAndCost();
            fail("Expected EstimationException");
        } catch (EstimationException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetRMSWithZeroWeights() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(0.0, 2.0, 0.5),   // weight=0
            new TestMeasurement(0.0, 3.0, -0.5)   // weight=0
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        // criterion = 0 + 0 = 0
        // RMS = sqrt(0/2) = 0
        assertEquals(0.0, estimator.getRMS(problem), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetChiSquareWithZeroWeights() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(0.0, 2.0, 0.5)   // weight=0
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        // This will cause division by zero - should return Infinity or NaN
        double result = estimator.getChiSquare(problem);
        assertTrue(Double.isInfinite(result) || Double.isNaN(result));
    }

    @Test(timeout = 4000)
    public void testGetRMSWithEmptyMeasurements() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[0];
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        // Division by zero - should return NaN or Infinity
        double result = estimator.getRMS(problem);
        assertTrue(Double.isNaN(result) || Double.isInfinite(result));
    }

    @Test(timeout = 4000)
    public void testGetChiSquareWithEmptyMeasurements() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[0];
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        // Empty sum = 0
        assertEquals(0.0, estimator.getChiSquare(problem), 1e-10);
    }

    @Test(timeout = 4000)
    public void testInitializeEstimateWithEmptyArrays() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[0];
        EstimatedParameter[] parameters = new EstimatedParameter[0];
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        assertEquals(0, estimator.rows);
        assertEquals(0, estimator.cols);
        assertEquals(0, estimator.jacobian.length);
        assertEquals(0, estimator.residuals.length);
        assertEquals(Double.POSITIVE_INFINITY, estimator.cost, 0.0);
    }

    @Test(timeout = 4000)
    public void testUpdateJacobianWithEmptyArrays() {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[0];
        EstimatedParameter[] parameters = new EstimatedParameter[0];
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        // Should not throw
        estimator.updateJacobian();
        assertEquals(1, estimator.getJacobianEvaluations());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testGetCovariancesNormalCase() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        
        // Create a problem with 2 measurements and 2 parameters
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.5),
            new TestMeasurement(1.0, 2.0, -0.5)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        double[][] covariances = estimator.getCovariances(problem);
        
        // Verify dimensions
        assertEquals(2, covariances.length);
        assertEquals(2, covariances[0].length);
        
        // Verify symmetry
        assertEquals(covariances[0][1], covariances[1][0], 1e-10);
        
        // Verify jacobian evaluations incremented
        assertEquals(1, estimator.getJacobianEvaluations());
    }

    @Test(timeout = 4000)
    public void testGetCovariancesSingularMatrix() {
        TestEstimator estimator = new TestEstimator();
        
        // Create a problem with singular jacobian (all partials equal)
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.5),
            new TestMeasurement(1.0, 1.0, -0.5)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        try {
            estimator.getCovariances(problem);
            fail("Expected EstimationException for singular matrix");
        } catch (EstimationException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGuessParametersErrorsNormalCase() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        
        // Create a problem with 3 measurements and 2 parameters (m > p)
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.5),
            new TestMeasurement(1.0, 2.0, -0.5),
            new TestMeasurement(1.0, 3.0, 0.2)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        double[] errors = estimator.guessParametersErrors(problem);
        
        // Verify dimensions
        assertEquals(2, errors.length);
        
        // Verify errors are positive
        assertTrue(errors[0] >= 0);
        assertTrue(errors[1] >= 0);
    }

    @Test(timeout = 4000)
    public void testGuessParametersErrorsEqualMeasurementsAndParams() {
        TestEstimator estimator = new TestEstimator();
        
        // Create a problem with 2 measurements and 2 parameters (m == p)
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.5),
            new TestMeasurement(1.0, 2.0, -0.5)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        try {
            estimator.guessParametersErrors(problem);
            fail("Expected EstimationException when m == p");
        } catch (EstimationException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGuessParametersErrorsFewerMeasurementsThanParams() {
        TestEstimator estimator = new TestEstimator();
        
        // Create a problem with 1 measurement and 2 parameters (m < p)
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.5)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        try {
            estimator.guessParametersErrors(problem);
            fail("Expected EstimationException when m < p");
        } catch (EstimationException e) {
            // Expected
        }
    }

    /**
     * DEFECT-TARGETED TEST:
     * This test targets the ArrayIndexOutOfBoundsException that occurs
     * in the testBoundParameters scenario. The defect occurs when
     * getCovariances is called with a problem where the jacobian
     * array indexing causes an out-of-bounds access.
     * 
     * The specific scenario involves a problem where the number of
     * measurements and parameters causes the jacobian array to be
     * accessed with an index that exceeds its bounds.
     */
    @Test(timeout = 4000)
    public void testBoundParametersDefectScenario() {
        TestEstimator estimator = new TestEstimator();
        
        // This scenario mimics the testBoundParameters test case
        // with a configuration that triggers the ArrayIndexOutOfBoundsException
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.5),
            new TestMeasurement(1.0, 2.0, -0.5),
            new TestMeasurement(1.0, 3.0, 0.2),
            new TestMeasurement(1.0, 4.0, -0.1)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0),
            new TestParameter("p3", 3.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        // This should not throw ArrayIndexOutOfBoundsException
        // The defect causes an ArrayIndexOutOfBoundsException: 6
        try {
            double[][] covariances = estimator.getCovariances(problem);
            
            // If we get here, the defect is not triggered
            // Verify the result is valid
            assertEquals(3, covariances.length);
            assertEquals(3, covariances[0].length);
        } catch (ArrayIndexOutOfBoundsException e) {
            // This is the defect - fail the test
            fail("ArrayIndexOutOfBoundsException thrown: " + e.getMessage());
        } catch (EstimationException e) {
            // This could be acceptable if the matrix is singular
            // But we should verify it's not the ArrayIndexOutOfBoundsException
            assertFalse("Should not throw EstimationException for this case", 
                       e.getMessage().contains("singular"));
        }
    }

    @Test(timeout = 4000)
    public void testBoundParametersDefectScenarioWithGuessErrors() {
        TestEstimator estimator = new TestEstimator();
        
        // Similar scenario but with more measurements to avoid singular matrix
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.5),
            new TestMeasurement(1.0, 2.0, -0.5),
            new TestMeasurement(1.0, 3.0, 0.2),
            new TestMeasurement(1.0, 4.0, -0.1),
            new TestMeasurement(1.0, 5.0, 0.3)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0),
            new TestParameter("p3", 3.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        try {
            double[] errors = estimator.guessParametersErrors(problem);
            
            // If we get here, the defect is not triggered
            assertEquals(3, errors.length);
        } catch (ArrayIndexOutOfBoundsException e) {
            // This is the defect - fail the test
            fail("ArrayIndexOutOfBoundsException thrown: " + e.getMessage());
        } catch (EstimationException e) {
            // Could be singular matrix, but should not be ArrayIndexOutOfBounds
            assertFalse("Should not throw EstimationException for this case", 
                       e.getMessage().contains("singular"));
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testUpdateResidualsAndCostWithNullMeasurements() {
        TestEstimator estimator = new TestEstimator();
        estimator.setMaxCostEval(10);
        
        // Create problem with null measurements
        TestProblem problem = new TestProblem(null, new EstimatedParameter[0]);
        
        estimator.initializeEstimate(problem);
        
        // This will throw NullPointerException
        try {
            estimator.updateResidualsAndCost();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        } catch (EstimationException e) {
            fail("Expected NullPointerException, got EstimationException");
        }
    }

    @Test(timeout = 4000)
    public void testGetRMSWithNullProblem() {
        TestEstimator estimator = new TestEstimator();
        
        try {
            estimator.getRMS(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetChiSquareWithNullProblem() {
        TestEstimator estimator = new TestEstimator();
        
        try {
            estimator.getChiSquare(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetCovariancesWithNullProblem() {
        TestEstimator estimator = new TestEstimator();
        
        try {
            estimator.getCovariances(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        } catch (EstimationException e) {
            fail("Expected NullPointerException, got EstimationException");
        }
    }

    @Test(timeout = 4000)
    public void testGuessParametersErrorsWithNullProblem() {
        TestEstimator estimator = new TestEstimator();
        
        try {
            estimator.guessParametersErrors(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        } catch (EstimationException e) {
            fail("Expected NullPointerException, got EstimationException");
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testMultipleInitializeEstimateCalls() {
        TestEstimator estimator = new TestEstimator();
        
        // First initialization
        WeightedMeasurement[] measurements1 = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.5)
        };
        EstimatedParameter[] parameters1 = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem1 = new TestProblem(measurements1, parameters1);
        
        estimator.initializeEstimate(problem1);
        assertEquals(1, estimator.rows);
        assertEquals(1, estimator.cols);
        
        // Second initialization with different sizes
        WeightedMeasurement[] measurements2 = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.5),
            new TestMeasurement(1.0, 2.0, -0.5)
        };
        EstimatedParameter[] parameters2 = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0)
        };
        TestProblem problem2 = new TestProblem(measurements2, parameters2);
        
        estimator.initializeEstimate(problem2);
        assertEquals(2, estimator.rows);
        assertEquals(2, estimator.cols);
        assertEquals(4, estimator.jacobian.length);
        assertEquals(2, estimator.residuals.length);
        
        // Verify counters reset
        assertEquals(0, estimator.getCostEvaluations());
        assertEquals(0, estimator.getJacobianEvaluations());
    }

    @Test(timeout = 4000)
    public void testStateAfterUpdateJacobianAndResiduals() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        estimator.setMaxCostEval(10);
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(4.0, 2.0, 0.5),
            new TestMeasurement(9.0, 3.0, -0.5)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        // Update jacobian
        estimator.updateJacobian();
        assertEquals(1, estimator.getJacobianEvaluations());
        assertEquals(0, estimator.getCostEvaluations());
        
        // Update residuals and cost
        estimator.updateResidualsAndCost();
        assertEquals(1, estimator.getJacobianEvaluations());
        assertEquals(1, estimator.getCostEvaluations());
        
        // Verify cost is finite
        assertTrue(Double.isFinite(estimator.cost));
        
        // Update again
        estimator.updateResidualsAndCost();
        assertEquals(2, estimator.getCostEvaluations());
    }

    @Test(timeout = 4000)
    public void testCostValueAfterMultipleUpdates() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        estimator.setMaxCostEval(10);
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.5)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        // First update
        estimator.updateResidualsAndCost();
        double firstCost = estimator.cost;
        assertEquals(Math.sqrt(0.25), firstCost, 1e-10);
        
        // Second update (same measurements, same cost)
        estimator.updateResidualsAndCost();
        assertEquals(firstCost, estimator.cost, 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetCovariancesMultipleCalls() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.5),
            new TestMeasurement(1.0, 2.0, -0.5),
            new TestMeasurement(1.0, 3.0, 0.2)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        // First call
        double[][] cov1 = estimator.getCovariances(problem);
        assertEquals(1, estimator.getJacobianEvaluations());
        
        // Second call - jacobian evaluations should increment
        double[][] cov2 = estimator.getCovariances(problem);
        assertEquals(2, estimator.getJacobianEvaluations());
        
        // Results should be the same
        assertEquals(cov1[0][0], cov2[0][0], 1e-10);
        assertEquals(cov1[0][1], cov2[0][1], 1e-10);
        assertEquals(cov1[1][0], cov2[1][0], 1e-10);
        assertEquals(cov1[1][1], cov2[1][1], 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetCovariancesSymmetry() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        
        WeightedMeasurement[] measurements = new WeightedMeasurement[] {
            new TestMeasurement(1.0, 1.0, 0.5),
            new TestMeasurement(1.0, 2.0, -0.5),
            new TestMeasurement(1.0, 3.0, 0.2)
        };
        EstimatedParameter[] parameters = new EstimatedParameter[] {
            new TestParameter("p1", 1.0),
            new TestParameter("p2", 2.0),
            new TestParameter("p3", 3.0)
        };
        TestProblem problem = new TestProblem(measurements, parameters);
        
        estimator.initializeEstimate(problem);
        
        double[][] covariances = estimator.getCovariances(problem);
        
        // Verify symmetry
        for (int i = 0; i < covariances.length; i++) {
            for (int j = 0; j < covariances.length; j++) {
                assertEquals(covariances[i][j], covariances[j][i], 1e-10);
            }
        }
    }
}