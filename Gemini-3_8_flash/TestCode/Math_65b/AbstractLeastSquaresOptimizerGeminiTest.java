/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math.optimization.general;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxEvaluationsExceededException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.SimpleVectorialValueChecker;
import org.apache.commons.math.optimization.VectorialConvergenceChecker;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: AbstractLeastSquaresOptimizer
 *
 * Branch & Condition Coverage:
 * 1. incrementIterationsCounter():
 *    - Branch (++iterations > maxIterations) [TRUE -> OptimizationException(MaxIterationsExceededException)]
 *    - Branch (++iterations <= maxIterations) [FALSE -> normal return]
 * 2. updateResidualsAndCost():
 *    - Branch (++objectiveEvaluations > maxEvaluations) [TRUE -> FunctionEvaluationException(MaxEvaluationsExceededException)]
 *    - Branch (objective.length != rows) [TRUE -> FunctionEvaluationException(DIMENSIONS_MISMATCH_SIMPLE)]
 *    - Residual and weighted cost computation loop across all rows
 * 3. updateJacobian():
 *    - Branch (jacobian.length != rows) [TRUE -> FunctionEvaluationException(DIMENSIONS_MISMATCH_SIMPLE)]
 *    - Weighted jacobian scaling: ji[j] *= -Math.sqrt(residualsWeights[i])
 * 4. getCovariances():
 *    - Normal symmetric J^T * J inversion via LUDecomposition
 *    - Singular matrix inversion failure [InvalidMatrixException -> OptimizationException]
 * 5. guessParametersErrors():
 *    - Branch (rows <= cols) [TRUE -> OptimizationException(NO_DEGREES_OF_FREEDOM)]
 *    - Calculation of c = sqrt(getChiSquare() / (rows - cols)) and error = sqrt(covar[i][i]) * c
 * 6. getRMS() and getChiSquare():
 *    - Weighted sum of residuals squared and ChiSquare calculation
 * 7. optimize():
 *    - Branch (target.length != weights.length) [TRUE -> OptimizationException(DIMENSIONS_MISMATCH_SIMPLE)]
 *    - State initialization (rows, cols, jacobian, targetValues, residuals, cost)
 *
 * Known Defect (Defects4J - LevenbergMarquardtOptimizerTest::testCircleFitting):
 * - Discrepancy in getChiSquare() vs weight scaling calculation affecting parameter error estimations
 *   in circle fitting: chiSquare dividing vs multiplying weights or residual-scaling inconsistencies
 *   leading to parameter errors evaluating to 0.0019737... instead of expected 0.004.
 */
public class AbstractLeastSquaresOptimizerGeminiTest {

    /**
     * Concrete testable subclass to expose protected fields and methods
     * of AbstractLeastSquaresOptimizer.
     */
    private static class ConcreteLeastSquaresOptimizer extends AbstractLeastSquaresOptimizer {
        private boolean triggerDoOptimize = true;
        private VectorialPointValuePair customResult = null;

        public ConcreteLeastSquaresOptimizer() {
            super();
        }

        @Override
        protected VectorialPointValuePair doOptimize()
                throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {
            if (!triggerDoOptimize) {
                return customResult;
            }
            updateResidualsAndCost();
            updateJacobian();
            incrementIterationsCounter();
            return new VectorialPointValuePair(point, objective);
        }

        // Pass-through helpers to invoke protected methods
        public void callIncrementIterationsCounter() throws OptimizationException {
            super.incrementIterationsCounter();
        }

        public void callUpdateJacobian() throws FunctionEvaluationException {
            super.updateJacobian();
        }

        public void callUpdateResidualsAndCost() throws FunctionEvaluationException {
            super.updateResidualsAndCost();
        }

        public void setCols(int cols) {
            this.cols = cols;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public void setPoint(double[] point) {
            this.point = point;
        }

        public void setResiduals(double[] residuals) {
            this.residuals = residuals;
        }

        public void setResidualsWeights(double[] weights) {
            this.residualsWeights = weights;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public void setJacobian(double[][] jacobian) {
            this.jacobian = jacobian;
        }

        public double[][] getInternalJacobian() {
            return this.jacobian;
        }

        public double getCost() {
            return this.cost;
        }

        public double[] getResiduals() {
            return this.residuals;
        }

        public void setTriggerDoOptimize(boolean trigger) {
            this.triggerDoOptimize = trigger;
        }

        public void setCustomResult(VectorialPointValuePair result) {
            this.customResult = result;
        }
    }

    /**
     * Reusable mock vector function with analytical Jacobian.
     */
    private static class MockVectorialFunction implements DifferentiableMultivariateVectorialFunction {
        private final int outputDim;
        private final int inputDim;
        private int evaluationCount = 0;
        private int jacobianCount = 0;
        private boolean mismatchObjectiveDim = false;
        private boolean mismatchJacobianDim = false;

        public MockVectorialFunction(int inputDim, int outputDim) {
            this.inputDim = inputDim;
            this.outputDim = outputDim;
        }

        public void setMismatchObjectiveDim(boolean mismatch) {
            this.mismatchObjectiveDim = mismatch;
        }

        public void setMismatchJacobianDim(boolean mismatch) {
            this.mismatchJacobianDim = mismatch;
        }

        @Override
        public double[] value(double[] point) {
            ++evaluationCount;
            int dim = mismatchObjectiveDim ? outputDim + 1 : outputDim;
            double[] res = new double[dim];
            for (int i = 0; i < dim; ++i) {
                double val = 0.0;
                for (int j = 0; j < inputDim; ++j) {
                    val += (j + 1) * point[j];
                }
                res[i] = val + (i + 1);
            }
            return res;
        }

        @Override
        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                @Override
                public double[][] value(double[] point) {
                    ++jacobianCount;
                    int rowDim = mismatchJacobianDim ? outputDim + 1 : outputDim;
                    double[][] jac = new double[rowDim][inputDim];
                    for (int i = 0; i < rowDim; ++i) {
                        for (int j = 0; j < inputDim; ++j) {
                            jac[i][j] = (j + 1.0);
                        }
                    }
                    return jac;
                }
            };
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultInitializationAndGettersSetters() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();

        assertEquals(AbstractLeastSquaresOptimizer.DEFAULT_MAX_ITERATIONS, optimizer.getMaxIterations());
        assertEquals(Integer.MAX_VALUE, optimizer.getMaxEvaluations());
        assertEquals(0, optimizer.getIterations());
        assertEquals(0, optimizer.getEvaluations());
        assertEquals(0, optimizer.getJacobianEvaluations());
        assertNotNull(optimizer.getConvergenceChecker());
        assertTrue(optimizer.getConvergenceChecker() instanceof SimpleVectorialValueChecker);

        optimizer.setMaxIterations(250);
        assertEquals(250, optimizer.getMaxIterations());

        optimizer.setMaxEvaluations(500);
        assertEquals(500, optimizer.getMaxEvaluations());

        VectorialConvergenceChecker customChecker = new SimpleVectorialValueChecker(1e-4, 1e-4);
        optimizer.setConvergenceChecker(customChecker);
        assertSame(customChecker, optimizer.getConvergenceChecker());
    }

    @Test(timeout = 4000)
    public void testOptimizeSuccessfulWorkflow() throws Exception {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        MockVectorialFunction function = new MockVectorialFunction(2, 3);

        double[] target = new double[]{10.0, 20.0, 30.0};
        double[] weights = new double[]{1.0, 1.0, 1.0};
        double[] startPoint = new double[]{1.0, 2.0};

        VectorialPointValuePair result = optimizer.optimize(function, target, weights, startPoint);
        assertNotNull(result);

        assertEquals(1, optimizer.getIterations());
        assertEquals(1, optimizer.getEvaluations());
        assertEquals(1, optimizer.getJacobianEvaluations());
        assertArrayEquals(startPoint, result.getPointRef(), 1e-12);

        // Function value: [1*1 + 2*2 + 1, 1*1 + 2*2 + 2, 1*1 + 2*2 + 3] = [6, 7, 8]
        // Target: [10, 20, 30] -> Residuals: [4, 13, 22]
        double[] expectedResiduals = new double[]{4.0, 13.0, 22.0};
        assertArrayEquals(expectedResiduals, optimizer.getResiduals(), 1e-12);

        // Cost = sqrt(4^2 + 13^2 + 22^2) = sqrt(16 + 169 + 484) = sqrt(669)
        assertEquals(Math.sqrt(669.0), optimizer.getCost(), 1e-9);

        // RMS = sqrt(669.0 / 3)
        assertEquals(Math.sqrt(669.0 / 3.0), optimizer.getRMS(), 1e-9);

        // ChiSquare = 4^2/1 + 13^2/1 + 22^2/1 = 669.0
        assertEquals(669.0, optimizer.getChiSquare(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testWeightedResidualsAndJacobianScaling() throws Exception {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        MockVectorialFunction function = new MockVectorialFunction(2, 2);

        double[] target = new double[]{5.0, 10.0};
        double[] weights = new double[]{4.0, 9.0}; // sqrt weights = 2.0 and 3.0
        double[] startPoint = new double[]{1.0, 1.0};

        optimizer.optimize(function, target, weights, startPoint);

        // Mock jacobian unweighted rows were: [1.0, 2.0]
        // Scaled by -sqrt(weight):
        // Row 0 scaled by -2.0 -> [-2.0, -4.0]
        // Row 1 scaled by -3.0 -> [-3.0, -6.0]
        double[][] jac = optimizer.getInternalJacobian();
        assertEquals(-2.0, jac[0][0], 1e-12);
        assertEquals(-4.0, jac[0][1], 1e-12);
        assertEquals(-3.0, jac[1][0], 1e-12);
        assertEquals(-6.0, jac[1][1], 1e-12);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Degenerate Scenarios
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyProblemDimensions() throws Exception {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.setTriggerDoOptimize(false);
        optimizer.setCustomResult(new VectorialPointValuePair(new double[0], new double[0]));

        MockVectorialFunction function = new MockVectorialFunction(0, 0);
        VectorialPointValuePair pair = optimizer.optimize(function, new double[0], new double[0], new double[0]);
        assertNotNull(pair);
        assertEquals(0, pair.getPointRef().length);
    }

    @Test(timeout = 4000)
    public void testIterationsLimitBoundary() throws Exception {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.setMaxIterations(2);

        optimizer.callIncrementIterationsCounter();
        assertEquals(1, optimizer.getIterations());
        optimizer.callIncrementIterationsCounter();
        assertEquals(2, optimizer.getIterations());

        try {
            optimizer.callIncrementIterationsCounter();
            fail("Expected OptimizationException on exceeding iterations");
        } catch (OptimizationException e) {
            assertTrue(e.getCause() instanceof MaxIterationsExceededException);
        }
    }

    @Test(timeout = 4000)
    public void testEvaluationsLimitBoundary() throws Exception {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        MockVectorialFunction function = new MockVectorialFunction(1, 1);
        optimizer.setTriggerDoOptimize(false);

        optimizer.optimize(function, new double[]{1.0}, new double[]{1.0}, new double[]{0.0});
        optimizer.setMaxEvaluations(1);

        // First call should succeed (objectiveEvaluations becomes 1 <= 1)
        optimizer.callUpdateResidualsAndCost();
        assertEquals(1, optimizer.getEvaluations());

        // Second call should exceed max evaluations (2 > 1)
        try {
            optimizer.callUpdateResidualsAndCost();
            fail("Expected FunctionEvaluationException on exceeding evaluations");
        } catch (FunctionEvaluationException e) {
            assertTrue(e.getCause() instanceof MaxEvaluationsExceededException);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Circle Fitting Error / Covariance)
    // =========================================================================

    /**
     * Targeted test for the known defect:
     * LevenbergMarquardtOptimizerTest::testCircleFitting where parameter errors
     * evaluation produced wrong order of magnitude (0.00197... instead of 0.004).
     *
     * In AbstractLeastSquaresOptimizer.guessParametersErrors():
     *   final double c = Math.sqrt(getChiSquare() / (rows - cols));
     *   double[][] covar = getCovariances();
     *   errors[i] = Math.sqrt(covar[i][i]) * c;
     *
     * If weights are not standard variance reciprocals or getChiSquare() diverges,
     * the estimated standard errors deviate significantly from expected statistical error.
     */
    @Test(timeout = 4000)
    public void testDefectCircleFittingParameterErrorsEstimation() throws Exception {
        // Construct a circular fit problem with known noisy points
        final List<double[]> points = new ArrayList<>();
        points.add(new double[]{30.0, 68.0});
        points.add(new double[]{50.0, -6.0});
        points.add(new double[]{110.0, -20.0});
        points.add(new double[]{35.0, 15.0});
        points.add(new double[]{45.0, 97.0});

        final int numPoints = points.size(); // rows = 5, cols = 3 (cx, cy, r)
        DifferentiableMultivariateVectorialFunction circleFunction = new DifferentiableMultivariateVectorialFunction() {
            @Override
            public double[] value(double[] point) {
                double cx = point[0];
                double cy = point[1];
                double r = point[2];
                double[] residuals = new double[numPoints];
                for (int i = 0; i < numPoints; ++i) {
                    double[] p = points.get(i);
                    double d = Math.hypot(p[0] - cx, p[1] - cy);
                    residuals[i] = d - r;
                }
                return residuals;
            }

            @Override
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    @Override
                    public double[][] value(double[] point) {
                        double cx = point[0];
                        double cy = point[1];
                        double[][] jac = new double[numPoints][3];
                        for (int i = 0; i < numPoints; ++i) {
                            double[] p = points.get(i);
                            double d = Math.hypot(p[0] - cx, p[1] - cy);
                            jac[i][0] = (cx - p[0]) / d;
                            jac[i][1] = (cy - p[1]) / d;
                            jac[i][2] = -1.0;
                        }
                        return jac;
                    }
                };
            }
        };

        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.setTriggerDoOptimize(false);

        double[] target = new double[numPoints];
        double[] weights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0};
        // Simulated optimal point for the circle: center=(50, 40), radius=50
        double[] optimalPoint = new double[]{50.0, 40.0, 50.0};

        optimizer.optimize(circleFunction, target, weights, optimalPoint);
        optimizer.callUpdateResidualsAndCost();

        double[] errors = optimizer.guessParametersErrors();
        assertNotNull(errors);
        assertEquals(3, errors.length);

        // Verify that degrees of freedom factor and covariances give deterministic positive values
        for (double error : errors) {
            assertFalse(Double.isNaN(error));
            assertFalse(Double.isInfinite(error));
            assertTrue(error > 0.0);
        }

        // Test Chi-Square relationship with cost:
        // When all weights are 1.0, getChiSquare() must strictly equal cost^2
        double cost = optimizer.getCost();
        assertEquals(cost * cost, optimizer.getChiSquare(), 1e-10);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = OptimizationException.class, timeout = 4000)
    public void testDimensionMismatchBetweenTargetAndWeightsThrows() throws Exception {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        MockVectorialFunction function = new MockVectorialFunction(2, 2);

        double[] target = new double[]{1.0, 2.0};
        double[] weights = new double[]{1.0}; // Length mismatch
        double[] startPoint = new double[]{0.0, 0.0};

        optimizer.optimize(function, target, weights, startPoint);
    }

    @Test(expected = FunctionEvaluationException.class, timeout = 4000)
    public void testObjectiveFunctionDimensionMismatchThrows() throws Exception {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        MockVectorialFunction function = new MockVectorialFunction(2, 2);
        function.setMismatchObjectiveDim(true); // Returns 3 outputs instead of 2

        double[] target = new double[]{1.0, 2.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] startPoint = new double[]{0.0, 0.0};

        optimizer.optimize(function, target, weights, startPoint);
    }

    @Test(expected = FunctionEvaluationException.class, timeout = 4000)
    public void testJacobianDimensionMismatchThrows() throws Exception {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        MockVectorialFunction function = new MockVectorialFunction(2, 2);
        function.setMismatchJacobianDim(true); // Returns 3 rows instead of 2

        double[] target = new double[]{1.0, 2.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] startPoint = new double[]{0.0, 0.0};

        optimizer.optimize(function, target, weights, startPoint);
    }

    @Test(expected = OptimizationException.class, timeout = 4000)
    public void testGuessParametersErrorsThrowsWhenNoDegreesOfFreedom() throws Exception {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        MockVectorialFunction function = new MockVectorialFunction(3, 2); // rows (2) <= cols (3)

        optimizer.setTriggerDoOptimize(false);
        double[] target = new double[]{1.0, 2.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] startPoint = new double[]{0.0, 0.0, 0.0};

        optimizer.optimize(function, target, weights, startPoint);
        optimizer.guessParametersErrors();
    }

    @Test(expected = OptimizationException.class, timeout = 4000)
    public void testSingularCovarianceThrowsOptimizationException() throws Exception {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();

        // Create a function whose Jacobian has linearly dependent columns (all zeros)
        DifferentiableMultivariateVectorialFunction zeroJacFunction = new DifferentiableMultivariateVectorialFunction() {
            @Override
            public double[] value(double[] point) {
                return new double[]{point[0], point[1], point[0] + point[1]};
            }

            @Override
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    @Override
                    public double[][] value(double[] point) {
                        return new double[][]{
                                {0.0, 0.0},
                                {0.0, 0.0},
                                {0.0, 0.0}
                        };
                    }
                };
            }
        };

        optimizer.setTriggerDoOptimize(false);
        optimizer.optimize(zeroJacFunction, new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 1.0, 1.0}, new double[]{0.0, 0.0});
        optimizer.getCovariances();
    }

    // =========================================================================
    // Partition E: Mathematical Invariants & Covariance Structure
    // =========================================================================

    @Test(timeout = 4000)
    public void testCovarianceMatrixSymmetryAndInversion() throws Exception {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();

        // J has independent columns:
        // row 0: [2, 0]
        // row 1: [0, 3]
        // row 2: [1, 1]
        DifferentiableMultivariateVectorialFunction linearFunction = new DifferentiableMultivariateVectorialFunction() {
            @Override
            public double[] value(double[] point) {
                return new double[]{
                        2.0 * point[0],
                        3.0 * point[1],
                        point[0] + point[1]
                };
            }

            @Override
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    @Override
                    public double[][] value(double[] point) {
                        return new double[][]{
                                {2.0, 0.0},
                                {0.0, 3.0},
                                {1.0, 1.0}
                        };
                    }
                };
            }
        };

        optimizer.setTriggerDoOptimize(false);
        optimizer.optimize(linearFunction, new double[]{1.0, 1.0, 1.0}, new double[]{1.0, 1.0, 1.0}, new double[]{0.5, 0.5});

        double[][] covar = optimizer.getCovariances();
        assertNotNull(covar);
        assertEquals(2, covar.length);
        assertEquals(2, covar[0].length);

        // Covariance matrix must be symmetric: covar[0][1] == covar[1][0]
        assertEquals(covar[0][1], covar[1][0], 1e-12);

        // Diagonal elements (variances) must be positive
        assertTrue(covar[0][0] > 0.0);
        assertTrue(covar[1][1] > 0.0);
    }
}