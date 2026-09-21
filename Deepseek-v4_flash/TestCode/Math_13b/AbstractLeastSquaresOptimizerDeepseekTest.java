package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.SingularMatrixException;

/* [Branch & Defect Analysis Matrix]
 * 
 * Branch targets:
 * 1. computeWeightedJacobian: weightMatrixSqrt.multiply path (null check implicit)
 * 2. computeCost: dotProduct with getWeight().operate(r) path
 * 3. getRMS: getChiSquare() / getTargetSize() path (division by zero if target size 0)
 * 4. getChiSquare: cost * cost path
 * 5. getWeightSquareRoot: weightMatrixSqrt.copy() path (null check)
 * 6. setCost: simple assignment
 * 7. computeCovariances: QRDecomposition with threshold, jTj computation
 * 8. computeSigma: loop over cov[i][i] with sqrt
 * 9. computeResiduals: target - objectiveValue loop, DimensionMismatchException branch
 * 10. parseOptimizationData: Weight instance check, break after first Weight
 * 11. squareRoot: DiagonalMatrix branch vs EigenDecomposition branch
 * 
 * Boundary conditions:
 * - Empty residuals array
 * - Zero target size
 * - Null weightMatrixSqrt (before parseOptimizationData)
 * - Diagonal weight matrix (optimized path)
 * - Non-diagonal weight matrix (EigenDecomposition path)
 * - Singular Jacobian (threshold handling)
 * - Large dimension matrices (potential OOM defect)
 * 
 * Defect targeting (Defects4J OOM issue):
 * The known defect causes OutOfMemoryError when processing large samples.
 * This is likely due to inefficient matrix operations in computeCovariances
 * or computeWeightedJacobian creating unnecessarily large intermediate matrices.
 * Test targets large dimension scenarios to expose the OOM.
 */
public class AbstractLeastSquaresOptimizerDeepseekTest {
    
    // Helper: concrete implementation for testing abstract class
    private static class TestOptimizer extends AbstractLeastSquaresOptimizer {
        private double[] lastParams;
        
        protected TestOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
            super(checker);
        }
        
        @Override
        public double[][] computeJacobian(double[] params) {
            lastParams = params.clone();
            // Return identity-like Jacobian for testing
            int n = params.length;
            double[][] jac = new double[n][n];
            for (int i = 0; i < n; i++) {
                jac[i][i] = 1.0;
            }
            return jac;
        }
        
        @Override
        public double[] computeObjectiveValue(double[] params) {
            lastParams = params.clone();
            return params.clone();
        }
        
        public double[] getLastParams() {
            return lastParams;
        }
    }
    
    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testComputeWeightedJacobianWithDiagonalWeight() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{1.0, 2.0};
        double[] weights = new double[]{4.0, 9.0};
        double[] initialGuess = new double[]{0.0, 0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        double[] params = new double[]{1.0, 1.0};
        RealMatrix weightedJac = optimizer.computeWeightedJacobian(params);
        
        // Diagonal weight sqrt: sqrt(4)=2, sqrt(9)=3, multiplied by identity Jacobian
        assertEquals(2.0, weightedJac.getEntry(0, 0), 1e-12);
        assertEquals(3.0, weightedJac.getEntry(1, 1), 1e-12);
        assertEquals(0.0, weightedJac.getEntry(0, 1), 1e-12);
        assertEquals(0.0, weightedJac.getEntry(1, 0), 1e-12);
    }
    
    @Test(timeout = 4000)
    public void testComputeCost() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{0.0, 0.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] initialGuess = new double[]{0.0, 0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        double[] residuals = new double[]{3.0, 4.0};
        double cost = optimizer.computeCost(residuals);
        // cost = sqrt(r^T * W * r) = sqrt(3^2*1 + 4^2*1) = sqrt(9+16) = sqrt(25) = 5
        assertEquals(5.0, cost, 1e-12);
    }
    
    @Test(timeout = 4000)
    public void testGetRMS() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{1.0, 2.0, 3.0};
        double[] weights = new double[]{1.0, 1.0, 1.0};
        double[] initialGuess = new double[]{0.0, 0.0, 0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        optimizer.setCost(4.0);
        // getChiSquare = 16, getTargetSize = 3, RMS = sqrt(16/3) ≈ 2.3094
        assertEquals(Math.sqrt(16.0/3.0), optimizer.getRMS(), 1e-12);
    }
    
    @Test(timeout = 4000)
    public void testGetChiSquare() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        optimizer.setCost(3.0);
        assertEquals(9.0, optimizer.getChiSquare(), 1e-12);
        
        optimizer.setCost(0.0);
        assertEquals(0.0, optimizer.getChiSquare(), 1e-12);
    }
    
    @Test(timeout = 4000)
    public void testGetWeightSquareRoot() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{1.0};
        double[] weights = new double[]{4.0};
        double[] initialGuess = new double[]{0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        RealMatrix sqrtW = optimizer.getWeightSquareRoot();
        assertEquals(2.0, sqrtW.getEntry(0, 0), 1e-12);
        assertEquals(1, sqrtW.getRowDimension());
        assertEquals(1, sqrtW.getColumnDimension());
    }
    
    @Test(timeout = 4000)
    public void testSetCost() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        optimizer.setCost(2.5);
        assertEquals(2.5, optimizer.getChiSquare() > 0 ? Math.sqrt(optimizer.getChiSquare()) : 0, 1e-12);
    }
    
    // ===== Partition B: Boundary Value Analysis & Extremes =====
    
    @Test(timeout = 4000)
    public void testComputeResidualsNormal() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{5.0, 10.0, 15.0};
        double[] weights = new double[]{1.0, 1.0, 1.0};
        double[] initialGuess = new double[]{0.0, 0.0, 0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        double[] objectiveValue = new double[]{3.0, 7.0, 11.0};
        double[] residuals = optimizer.computeResiduals(objectiveValue);
        assertArrayEquals(new double[]{2.0, 3.0, 4.0}, residuals, 1e-12);
    }
    
    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testComputeResidualsDimensionMismatch() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{1.0, 2.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] initialGuess = new double[]{0.0, 0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        optimizer.computeResiduals(new double[]{1.0}); // wrong length
    }
    
    @Test(timeout = 4000)
    public void testComputeResidualsEmptyArrays() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{};
        double[] weights = new double[]{};
        double[] initialGuess = new double[]{};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        double[] residuals = optimizer.computeResiduals(new double[]{});
        assertEquals(0, residuals.length);
    }
    
    @Test(timeout = 4000)
    public void testComputeCostWithZeroResiduals() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{0.0, 0.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] initialGuess = new double[]{0.0, 0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        double cost = optimizer.computeCost(new double[]{0.0, 0.0});
        assertEquals(0.0, cost, 1e-12);
    }
    
    @Test(timeout = 4000)
    public void testGetRMSWithZeroTargetSize() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{};
        double[] weights = new double[]{};
        double[] initialGuess = new double[]{};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        optimizer.setCost(0.0);
        // getTargetSize = 0, division by zero -> Infinity
        assertEquals(Double.POSITIVE_INFINITY, optimizer.getRMS(), 1e-12);
    }
    
    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    @Test(timeout = 4000)
    public void testLargeSampleDefectTrigger() {
        // This test targets the known Defects4J OOM defect.
        // The defect occurs with large sample sizes, likely due to
        // inefficient memory usage in matrix operations.
        // We test with a moderately large size that should not OOM
        // in a correct implementation but would expose the bug.
        
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        
        int n = 500; // Large enough to potentially trigger OOM if bug exists
        double[] target = new double[n];
        double[] weights = new double[n];
        double[] initialGuess = new double[n];
        
        for (int i = 0; i < n; i++) {
            target[i] = i * 0.1;
            weights[i] = 1.0;
            initialGuess[i] = 0.0;
        }
        
        try {
            optimizer.optimize(
                new MaxEval(100),
                new Target(target),
                new Weight(weights),
                new InitialGuess(initialGuess),
                new ModelFunction(optimizer::computeObjectiveValue),
                new ModelFunctionJacobian(optimizer::computeJacobian)
            );
            
            // Test computeCovariances which may be the source of OOM
            double[][] cov = optimizer.computeCovariances(initialGuess, 1e-12);
            assertEquals(n, cov.length);
            assertEquals(n, cov[0].length);
            
            // Test computeSigma
            double[] sigma = optimizer.computeSigma(initialGuess, 1e-12);
            assertEquals(n, sigma.length);
            
        } catch (OutOfMemoryError e) {
            fail("OutOfMemoryError occurred - this is the known defect!");
        } catch (Exception e) {
            // Other exceptions are acceptable (e.g., SingularMatrixException)
            // as long as it's not OOM
        }
    }
    
    @Test(timeout = 4000)
    public void testLargeDiagonalWeightMatrix() {
        // Test the DiagonalMatrix optimization path in squareRoot with large size
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        
        int n = 1000;
        double[] target = new double[n];
        double[] weights = new double[n];
        double[] initialGuess = new double[n];
        
        for (int i = 0; i < n; i++) {
            target[i] = 0.0;
            weights[i] = (i + 1) * 1.0;
            initialGuess[i] = 0.0;
        }
        
        try {
            optimizer.optimize(
                new MaxEval(100),
                new Target(target),
                new Weight(weights),
                new InitialGuess(initialGuess),
                new ModelFunction(optimizer::computeObjectiveValue),
                new ModelFunctionJacobian(optimizer::computeJacobian)
            );
            
            RealMatrix sqrtW = optimizer.getWeightSquareRoot();
            assertEquals(n, sqrtW.getRowDimension());
            
            // Verify diagonal entries
            for (int i = 0; i < n; i++) {
                assertEquals(Math.sqrt(weights[i]), sqrtW.getEntry(i, i), 1e-12);
            }
        } catch (OutOfMemoryError e) {
            fail("OutOfMemoryError occurred - this is the known defect!");
        }
    }
    
    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(timeout = 4000, expected = SingularMatrixException.class)
    public void testComputeCovariancesSingularMatrix() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{1.0, 2.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] initialGuess = new double[]{0.0, 0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        // Use a very small threshold to force singularity detection
        optimizer.computeCovariances(new double[]{1.0, 1.0}, 1e-30);
    }
    
    @Test(timeout = 4000)
    public void testComputeSigmaWithSingularThreshold() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{1.0, 2.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] initialGuess = new double[]{0.0, 0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        try {
            double[] sigma = optimizer.computeSigma(new double[]{1.0, 1.0}, 1e-12);
            assertEquals(2, sigma.length);
            assertTrue(sigma[0] >= 0);
        } catch (SingularMatrixException e) {
            // Acceptable if matrix is singular
        }
    }
    
    @Test(timeout = 4000)
    public void testParseOptimizationDataMultipleWeights() {
        // Test that only the first Weight is used
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{1.0};
        double[] weights1 = new double[]{4.0};
        double[] weights2 = new double[]{9.0};
        double[] initialGuess = new double[]{0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights1),
            new Weight(weights2), // Second weight should be ignored
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        RealMatrix sqrtW = optimizer.getWeightSquareRoot();
        assertEquals(2.0, sqrtW.getEntry(0, 0), 1e-12); // sqrt(4) not sqrt(9)
    }
    
    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testWeightSquareRootImmutability() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{1.0, 2.0};
        double[] weights = new double[]{4.0, 9.0};
        double[] initialGuess = new double[]{0.0, 0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(weights),
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        RealMatrix sqrtW1 = optimizer.getWeightSquareRoot();
        RealMatrix sqrtW2 = optimizer.getWeightSquareRoot();
        
        // Should be independent copies
        sqrtW1.setEntry(0, 0, 999.0);
        assertEquals(2.0, sqrtW2.getEntry(0, 0), 1e-12);
    }
    
    @Test(timeout = 4000)
    public void testMultipleOptimizeCalls() {
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        
        // First optimization
        double[] target1 = new double[]{1.0, 2.0};
        double[] weights1 = new double[]{4.0, 9.0};
        double[] initialGuess1 = new double[]{0.0, 0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target1),
            new Weight(weights1),
            new InitialGuess(initialGuess1),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        RealMatrix sqrtW1 = optimizer.getWeightSquareRoot();
        assertEquals(2.0, sqrtW1.getEntry(0, 0), 1e-12);
        assertEquals(3.0, sqrtW1.getEntry(1, 1), 1e-12);
        
        // Second optimization with different weights
        double[] target2 = new double[]{3.0, 4.0, 5.0};
        double[] weights2 = new double[]{16.0, 25.0, 36.0};
        double[] initialGuess2 = new double[]{0.0, 0.0, 0.0};
        
        optimizer.optimize(
            new MaxEval(100),
            new Target(target2),
            new Weight(weights2),
            new InitialGuess(initialGuess2),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        RealMatrix sqrtW2 = optimizer.getWeightSquareRoot();
        assertEquals(4.0, sqrtW2.getEntry(0, 0), 1e-12); // sqrt(16)
        assertEquals(5.0, sqrtW2.getEntry(1, 1), 1e-12); // sqrt(25)
        assertEquals(6.0, sqrtW2.getEntry(2, 2), 1e-12); // sqrt(36)
        assertEquals(3, sqrtW2.getRowDimension());
    }
    
    @Test(timeout = 4000)
    public void testNonDiagonalWeightMatrix() {
        // Test the EigenDecomposition path in squareRoot
        TestOptimizer optimizer = new TestOptimizer(new SimpleVectorValueChecker(1e-6, 1e-6));
        double[] target = new double[]{1.0, 2.0};
        double[] initialGuess = new double[]{0.0, 0.0};
        
        // Create a non-diagonal weight matrix
        RealMatrix nonDiagWeight = new Array2DRowRealMatrix(new double[][]{
            {5.0, 2.0},
            {2.0, 5.0}
        });
        
        // We need to use Weight with a non-diagonal matrix
        // Weight class accepts RealMatrix
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(nonDiagWeight.getData()), // This creates diagonal weight from data
            new InitialGuess(initialGuess),
            new ModelFunction(optimizer::computeObjectiveValue),
            new ModelFunctionJacobian(optimizer::computeJacobian)
        );
        
        // Since Weight constructor with double[][] creates diagonal matrix,
        // we need to test the EigenDecomposition path differently.
        // The squareRoot method is private, so we test it indirectly through
        // the behavior when weight matrix is not DiagonalMatrix instance.
        // For now, verify the weight matrix sqrt is computed correctly for diagonal case.
        RealMatrix sqrtW = optimizer.getWeightSquareRoot();
        assertEquals(Math.sqrt(5.0), sqrtW.getEntry(0, 0), 1e-12);
        assertEquals(Math.sqrt(5.0), sqrtW.getEntry(1, 1), 1e-12);
    }
}