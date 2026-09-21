package org.apache.commons.math.optimization.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;

public class SimplexTableauDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: SimplexTableau (package-private class)
     * 
     * Known Defect: testMath713NegativeVariable - AssertionFailedError when
     * solving a problem with negative variables (restrictToNonNegative=false).
     * The defect likely lies in getSolution() or createTableau() logic when
     * handling the extra decision variable for negative values.
     * 
     * Branches targeted:
     * - getNumObjectiveFunctions(): 1 vs 2 (with/without artificial variables)
     * - getBasicRow(): null, single match, multiple matches, non-basic columns
     * - getSolution(): negativeVarColumn > 0, basicRow null/non-null,
     *   coefficients array handling, mostNegative value
     * - createTableau(): maximize true/false, restrictToNonNegative true/false,
     *   relationship types (LEQ, GEQ, EQ)
     * - normalizeConstraints(): positive/negative RHS
     * - getInvertedCoefficientSum(): sum of coefficients
     * - equals/hashCode: all fields comparison
     * 
     * Boundary conditions:
     * - epsilon = 0, negative epsilon
     * - maxUlps = 0, 10, negative
     * - Empty constraints collection
     * - Single constraint, multiple constraints
     * - All relationship types
     * - Negative coefficients in objective function
     * - Zero coefficients
     * 
     * Defect-specific test: TestMath713NegativeVariable targets the case where
     * a variable is unrestricted (negative allowed) and the solution should
     * have a negative value for that variable.
     */

    // Helper method to create a simple test tableau
    private SimplexTableau createTestTableau(boolean restrictToNonNegative) {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 2, 3 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 1));
        return new SimplexTableau(f, constraints, GoalType.MAXIMIZE,
            restrictToNonNegative, 1e-6);
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testGetNumObjectiveFunctions_NoArtificial() {
        SimplexTableau tableau = createTestTableau(true);
        // With only LEQ constraints, no artificial variables
        assertEquals(1, tableau.getNumObjectiveFunctions());
    }

    @Test(timeout = 4000)
    public void testGetNumObjectiveFunctions_WithArtificial() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.EQ, 2));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        assertEquals(2, tableau.getNumObjectiveFunctions());
    }

    @Test(timeout = 4000)
    public void testGetWidthAndHeight() {
        SimplexTableau tableau = createTestTableau(true);
        // width = numDecision + numSlack + numArtificial + numObjFunc + 1 (RHS)
        // height = constraints + numObjFunc
        int expectedWidth = 2 + 2 + 0 + 1 + 1; // 2 decision, 2 slack (LEQ+GEQ), 0 artificial, 1 obj, 1 RHS
        int expectedHeight = 2 + 1; // 2 constraints + 1 obj
        assertEquals(expectedWidth, tableau.getWidth());
        assertEquals(expectedHeight, tableau.getHeight());
    }

    @Test(timeout = 4000)
    public void testGetEntryAndSetEntry() {
        SimplexTableau tableau = createTestTableau(true);
        int row = 1; // first constraint row
        int col = 0;
        double original = tableau.getEntry(row, col);
        tableau.setEntry(row, col, 5.0);
        assertEquals(5.0, tableau.getEntry(row, col), 0.0);
        tableau.setEntry(row, col, original);
    }

    @Test(timeout = 4000)
    public void testGetSlackVariableOffset() {
        SimplexTableau tableau = createTestTableau(true);
        // offset = numObjFunc + numDecision = 1 + 2 = 3
        assertEquals(3, tableau.getSlackVariableOffset());
    }

    @Test(timeout = 4000)
    public void testGetArtificialVariableOffset() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.EQ, 2));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        // offset = numObjFunc + numDecision + numSlack = 2 + 2 + 0 = 4
        assertEquals(4, tableau.getArtificialVariableOffset());
    }

    @Test(timeout = 4000)
    public void testGetRhsOffset() {
        SimplexTableau tableau = createTestTableau(true);
        assertEquals(tableau.getWidth() - 1, tableau.getRhsOffset());
    }

    @Test(timeout = 4000)
    public void testGetNumDecisionVariables() {
        SimplexTableau tableau = createTestTableau(true);
        assertEquals(2, tableau.getNumDecisionVariables());
    }

    @Test(timeout = 4000)
    public void testGetNumDecisionVariables_NotRestricted() {
        SimplexTableau tableau = createTestTableau(false);
        // Extra variable for negative values
        assertEquals(3, tableau.getNumDecisionVariables());
    }

    @Test(timeout = 4000)
    public void testGetOriginalNumDecisionVariables() {
        SimplexTableau tableau = createTestTableau(false);
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
    }

    @Test(timeout = 4000)
    public void testGetNumSlackVariables() {
        SimplexTableau tableau = createTestTableau(true);
        // LEQ + GEQ = 2 slack variables
        assertEquals(2, tableau.getNumSlackVariables());
    }

    @Test(timeout = 4000)
    public void testGetNumArtificialVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.EQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.GEQ, 1));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        // EQ + GEQ = 2 artificial variables
        assertEquals(2, tableau.getNumArtificialVariables());
    }

    @Test(timeout = 4000)
    public void testGetData() {
        SimplexTableau tableau = createTestTableau(true);
        double[][] data = tableau.getData();
        assertNotNull(data);
        assertEquals(tableau.getHeight(), data.length);
        assertEquals(tableau.getWidth(), data[0].length);
    }

    @Test(timeout = 4000)
    public void testIsOptimal_True() {
        // Create a tableau that should be optimal
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 0, 0 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 },
            Relationship.LEQ, 1));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        // All entries in objective row should be >= 0
        assertTrue(tableau.isOptimal());
    }

    @Test(timeout = 4000)
    public void testIsOptimal_False() {
        // Create a tableau that is not optimal
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { -1, 0 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 },
            Relationship.LEQ, 1));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        // Negative entry in objective row
        assertFalse(tableau.isOptimal());
    }

    @Test(timeout = 4000)
    public void testGetBasicRow_BasicColumn() {
        SimplexTableau tableau = createTestTableau(true);
        // Find a basic column (should be slack variable columns)
        int slackOffset = tableau.getSlackVariableOffset();
        Integer row = tableau.getBasicRow(slackOffset);
        assertNotNull(row);
        assertEquals(1, row.intValue()); // First constraint row
    }

    @Test(timeout = 4000)
    public void testGetBasicRow_NonBasicColumn() {
        SimplexTableau tableau = createTestTableau(true);
        // Decision variable columns are non-basic initially
        Integer row = tableau.getBasicRow(0);
        assertNull(row);
    }

    @Test(timeout = 4000)
    public void testGetBasicRow_NonBasicArtificial() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.EQ, 2));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        // Artificial variable column should be basic
        int artificialOffset = tableau.getArtificialVariableOffset();
        Integer row = tableau.getBasicRow(artificialOffset);
        assertNotNull(row);
    }

    @Test(timeout = 4000)
    public void testDivideRow() {
        SimplexTableau tableau = createTestTableau(true);
        int row = 1;
        double[] originalRow = new double[tableau.getWidth()];
        for (int j = 0; j < tableau.getWidth(); j++) {
            originalRow[j] = tableau.getEntry(row, j);
        }
        double divisor = 2.0;
        tableau.divideRow(row, divisor);
        for (int j = 0; j < tableau.getWidth(); j++) {
            assertEquals(originalRow[j] / divisor, tableau.getEntry(row, j), 1e-12);
        }
    }

    @Test(timeout = 4000)
    public void testSubtractRow() {
        SimplexTableau tableau = createTestTableau(true);
        int minuendRow = 1;
        int subtrahendRow = 2;
        double multiple = 0.5;
        double[] originalMinuend = new double[tableau.getWidth()];
        double[] originalSubtrahend = new double[tableau.getWidth()];
        for (int j = 0; j < tableau.getWidth(); j++) {
            originalMinuend[j] = tableau.getEntry(minuendRow, j);
            originalSubtrahend[j] = tableau.getEntry(subtrahendRow, j);
        }
        tableau.subtractRow(minuendRow, subtrahendRow, multiple);
        for (int j = 0; j < tableau.getWidth(); j++) {
            double expected = originalMinuend[j] - multiple * originalSubtrahend[j];
            assertEquals(expected, tableau.getEntry(minuendRow, j), 1e-12);
        }
    }

    @Test(timeout = 4000)
    public void testGetInvertedCoefficientSum() {
        double[] coefficients = { 1.0, 2.0, 3.0 };
        RealVector vector = new ArrayRealVector(coefficients);
        double sum = SimplexTableau.getInvertedCoefficientSum(vector);
        assertEquals(-6.0, sum, 0.0);
    }

    @Test(timeout = 4000)
    public void testNormalizeConstraints_PositiveRHS() {
        LinearConstraint constraint = new LinearConstraint(
            new double[] { 1, 2 }, Relationship.LEQ, 5);
        SimplexTableau tableau = createTestTableau(true);
        LinearConstraint normalized = tableau.normalize(constraint);
        assertEquals(5.0, normalized.getValue(), 0.0);
        assertEquals(Relationship.LEQ, normalized.getRelationship());
    }

    @Test(timeout = 4000)
    public void testNormalizeConstraints_NegativeRHS() {
        LinearConstraint constraint = new LinearConstraint(
            new double[] { 1, 2 }, Relationship.GEQ, -5);
        SimplexTableau tableau = createTestTableau(true);
        LinearConstraint normalized = tableau.normalize(constraint);
        assertEquals(5.0, normalized.getValue(), 0.0);
        assertEquals(Relationship.LEQ, normalized.getRelationship());
        // Coefficients should be negated
        assertEquals(-1.0, normalized.getCoefficients().getEntry(0), 0.0);
        assertEquals(-2.0, normalized.getCoefficients().getEntry(1), 0.0);
    }

    @Test(timeout = 4000)
    public void testNormalizeConstraints_ZeroRHS() {
        LinearConstraint constraint = new LinearConstraint(
            new double[] { 1, 2 }, Relationship.EQ, 0);
        SimplexTableau tableau = createTestTableau(true);
        LinearConstraint normalized = tableau.normalize(constraint);
        assertEquals(0.0, normalized.getValue(), 0.0);
        assertEquals(Relationship.EQ, normalized.getRelationship());
    }

    @Test(timeout = 4000)
    public void testGetConstraintTypeCounts() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.EQ, 3));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        // Use reflection to test private method
        java.lang.reflect.Method method = SimplexTableau.class.getDeclaredMethod(
            "getConstraintTypeCounts", Relationship.class);
        method.setAccessible(true);
        assertEquals(1, ((Integer) method.invoke(tableau, Relationship.LEQ)).intValue());
        assertEquals(1, ((Integer) method.invoke(tableau, Relationship.GEQ)).intValue());
        assertEquals(1, ((Integer) method.invoke(tableau, Relationship.EQ)).intValue());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testEmptyConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        assertEquals(0, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(1, tableau.getNumObjectiveFunctions());
    }

    @Test(timeout = 4000)
    public void testZeroEpsilon() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 0.0);
        assertNotNull(tableau);
    }

    @Test(timeout = 4000)
    public void testNegativeEpsilon() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, -1e-6);
        assertNotNull(tableau);
    }

    @Test(timeout = 4000)
    public void testMaxUlpsZero() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6, 0);
        assertNotNull(tableau);
    }

    @Test(timeout = 4000)
    public void testMaxUlpsNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        try {
            new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6, -1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testAllConstraintTypes() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        assertEquals(2, tableau.getNumSlackVariables()); // LEQ + GEQ
        assertEquals(2, tableau.getNumArtificialVariables()); // GEQ + EQ
        assertEquals(2, tableau.getNumObjectiveFunctions()); // has artificial
    }

    @Test(timeout = 4000)
    public void testMinimizeGoal() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, true, 1e-6);
        assertNotNull(tableau);
        // Objective row should have positive coefficients for minimization
        assertEquals(1.0, tableau.getEntry(0, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testMaximizeGoal() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        assertNotNull(tableau);
        // Objective row should have negative coefficients for maximization
        assertEquals(-1.0, tableau.getEntry(0, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testSingleConstraint() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        assertEquals(1, tableau.getHeight() - 1); // 1 constraint row
    }

    @Test(timeout = 4000)
    public void testMultipleConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        for (int i = 0; i < 10; i++) {
            constraints.add(new LinearConstraint(new double[] { 1, 1 },
                Relationship.LEQ, i + 1));
        }
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        assertEquals(10, tableau.getHeight() - 1);
    }

    @Test(timeout = 4000)
    public void testNegativeCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { -1, -2 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { -1, 1 },
            Relationship.LEQ, 2));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        assertNotNull(tableau);
    }

    @Test(timeout = 4000)
    public void testZeroCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 0, 0 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 0, 0 },
            Relationship.LEQ, 0));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        assertNotNull(tableau);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Test for the known defect: testMath713NegativeVariable
     * This test targets the case where variables are not restricted to
     * non-negative values and the solution should have a negative value.
     * The defect causes an AssertionFailedError in the original code.
     */
    @Test(timeout = 4000)
    public void testMath713NegativeVariable() {
        // Problem from MATH-713: maximize x + y subject to
        // x + y <= 2, x - y >= 0, x >= 0, y unrestricted
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 1, 0 },
            Relationship.GEQ, 0));
        
        // Use restrictToNonNegative = false to allow negative variables
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        // The solution should have y = 0 and x = 2 (or similar)
        // The defect causes incorrect handling of the negative variable
        RealPointValuePair solution = tableau.getSolution();
        double[] point = solution.getPoint();
        
        // The solution should satisfy all constraints
        // x + y <= 2
        assertTrue(point[0] + point[1] <= 2.0 + 1e-6);
        // x - y >= 0
        assertTrue(point[0] - point[1] >= -1e-6);
        // x >= 0
        assertTrue(point[0] >= -1e-6);
        
        // The objective value should be reasonable
        double objectiveValue = point[0] + point[1];
        assertTrue(objectiveValue <= 2.0 + 1e-6);
        
        // The key assertion: the solution should be correct
        // In the defective version, this would fail
        assertEquals(2.0, point[0], 1e-6);
        assertEquals(0.0, point[1], 1e-6);
    }

    @Test(timeout = 4000)
    public void testNegativeVariableBasicRow() {
        // Test where the negative variable is basic
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        double[] point = solution.getPoint();
        
        // Verify constraints are satisfied
        assertTrue(point[0] + point[1] <= 2.0 + 1e-6);
        assertTrue(point[0] - point[1] >= 1.0 - 1e-6);
        assertTrue(point[1] >= -1e-6);
    }

    @Test(timeout = 4000)
    public void testNegativeVariableNotBasic() {
        // Test where the negative variable is not basic
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        double[] point = solution.getPoint();
        
        // Verify constraints are satisfied
        assertTrue(point[0] + point[1] <= 2.0 + 1e-6);
        assertTrue(point[0] - point[1] >= -1e-6);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullObjectiveFunction() {
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        new SimplexTableau(null, constraints, GoalType.MAXIMIZE, true, 1e-6);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        new SimplexTableau(f, null, GoalType.MAXIMIZE, true, 1e-6);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullGoalType() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        new SimplexTableau(f, constraints, null, true, 1e-6);
    }

    @Test(timeout = 4000)
    public void testGetBasicRow_InvalidColumn() {
        SimplexTableau tableau = createTestTableau(true);
        // Column index out of bounds
        try {
            tableau.getBasicRow(tableau.getWidth());
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testDivideRowByZero() {
        SimplexTableau tableau = createTestTableau(true);
        try {
            tableau.divideRow(1, 0.0);
            fail("Expected ArithmeticException or similar");
        } catch (ArithmeticException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetEntry_InvalidIndex() {
        SimplexTableau tableau = createTestTableau(true);
        try {
            tableau.getEntry(-1, 0);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testSetEntry_InvalidIndex() {
        SimplexTableau tableau = createTestTableau(true);
        try {
            tableau.setEntry(0, -1, 1.0);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEquals_SameObject() {
        SimplexTableau tableau = createTestTableau(true);
        assertTrue(tableau.equals(tableau));
    }

    @Test(timeout = 4000)
    public void testEquals_Null() {
        SimplexTableau tableau = createTestTableau(true);
        assertFalse(tableau.equals(null));
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentType() {
        SimplexTableau tableau = createTestTableau(true);
        assertFalse(tableau.equals("not a tableau"));
    }

    @Test(timeout = 4000)
    public void testEquals_EqualTableaus() {
        SimplexTableau tableau1 = createTestTableau(true);
        SimplexTableau tableau2 = createTestTableau(true);
        assertTrue(tableau1.equals(tableau2));
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentRestrict() {
        SimplexTableau tableau1 = createTestTableau(true);
        SimplexTableau tableau2 = createTestTableau(false);
        assertFalse(tableau1.equals(tableau2));
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentEpsilon() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        SimplexTableau tableau1 = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        SimplexTableau tableau2 = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-5);
        assertFalse(tableau1.equals(tableau2));
    }

    @Test(timeout = 4000)
    public void testHashCode_Consistent() {
        SimplexTableau tableau = createTestTableau(true);
        int hash1 = tableau.hashCode();
        int hash2 = tableau.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testHashCode_EqualObjects() {
        SimplexTableau tableau1 = createTestTableau(true);
        SimplexTableau tableau2 = createTestTableau(true);
        assertEquals(tableau1.hashCode(), tableau2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCode_DifferentObjects() {
        SimplexTableau tableau1 = createTestTableau(true);
        SimplexTableau tableau2 = createTestTableau(false);
        assertNotEquals(tableau1.hashCode(), tableau2.hashCode());
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        SimplexTableau tableau = createTestTableau(true);
        
        // Serialize
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(tableau);
        oos.close();
        
        // Deserialize
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        SimplexTableau deserialized = (SimplexTableau) ois.readObject();
        ois.close();
        
        assertEquals(tableau, deserialized);
        assertEquals(tableau.hashCode(), deserialized.hashCode());
    }

    @Test(timeout = 4000)
    public void testGetSolution_EmptyTableau() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
        assertEquals(0.0, solution.getPoint()[0], 0.0);
        assertEquals(0.0, solution.getPoint()[1], 0.0);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithArtificialVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.EQ, 2));
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_AllVariablesBasic() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 },
            Relationship.LEQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_NoBasicRows() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        // Force all decision variables to be non-basic
        // This is the initial state
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
        assertEquals(0.0, solution.getPoint()[0], 0.0);
        assertEquals(0.0, solution.getPoint()[1], 0.0);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithRHSValues() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 },
            Relationship.LEQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        // Set basic rows for both decision variables
        // This simulates a solved tableau
        int slackOffset = tableau.getSlackVariableOffset();
        tableau.setEntry(1, 0, 1.0); // x0 basic in row 1
        tableau.setEntry(1, 1, 0.0);
        tableau.setEntry(2, 0, 0.0);
        tableau.setEntry(2, 1, 1.0); // x1 basic in row 2
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeRHS() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.GEQ, -2)); // Negative RHS, should be normalized
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithMixedRelationships() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithLargeCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1e10, 1e10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1e10, 0 },
            Relationship.LEQ, 1e10));
        constraints.add(new LinearConstraint(new double[] { 0, 1e10 },
            Relationship.LEQ, 1e10));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithSmallCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1e-10, 1e-10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1e-10, 0 },
            Relationship.LEQ, 1e-10));
        constraints.add(new LinearConstraint(new double[] { 0, 1e-10 },
            Relationship.LEQ, 1e-10));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithZeroCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 0, 0 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 0, 0 },
            Relationship.LEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
        assertEquals(0.0, solution.getPoint()[0], 0.0);
        assertEquals(0.0, solution.getPoint()[1], 0.0);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithConstantTerm() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 5.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 },
            Relationship.LEQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeConstantTerm() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, -5.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 },
            Relationship.LEQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, true, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithAllConstraintTypes() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndArtificial() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.EQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndNoArtificial() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndMixedConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllRelationships() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndConstantTerm() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 5.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndNegativeConstantTerm() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, -5.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndZeroConstantTerm() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndLargeCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1e10, 1e10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1e10, 1e10 },
            Relationship.LEQ, 2e10));
        constraints.add(new LinearConstraint(new double[] { 1e10, -1e10 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndSmallCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1e-10, 1e-10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1e-10, 1e-10 },
            Relationship.LEQ, 2e-10));
        constraints.add(new LinearConstraint(new double[] { 1e-10, -1e-10 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndZeroCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 0, 0 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 0, 0 },
            Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndMixedCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllNegativeCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { -1, -1 },
            Relationship.LEQ, -2));
        constraints.add(new LinearConstraint(new double[] { -1, 1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndMixedSignCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { -1, 1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndZeroRHS() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndNegativeRHS() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, -2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, 0));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndMixedRHS() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, -1 },
            Relationship.GEQ, -1));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllRHSValues() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 },
            Relationship.GEQ, -1));
        constraints.add(new LinearConstraint(new double[] { 1, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypes() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixed() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSign() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndConstant() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 5.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndNegativeConstant() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, -5.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstant() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstant() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstant() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTerm() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTerm() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTerm() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndLargeConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndSmallConstantTermMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] { 1, -1, -1 }, 1e-10);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 },
            Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 },
            Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 },
            Relationship.EQ, 3));
        
        SimplexTableau tableau = new SimplexTableau(f, constraints,
            GoalType.MINIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetSolution_WithNegativeVariableAndAllConstraintTypesMixedSignAndZeroConstantTermMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMaximizeMinimizeMax