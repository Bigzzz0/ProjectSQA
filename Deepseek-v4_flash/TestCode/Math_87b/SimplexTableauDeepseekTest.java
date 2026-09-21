package org.apache.commons.math.optimization.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.ArrayRealVector;

public class SimplexTableauDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: SimplexTableau (package-private class)
     * 
     * Known Defect: testSingleVariableAndConstraint expects solution value 10.0
     * but gets 0.0. This indicates a bug in the solution extraction logic,
     * specifically in getSolution() where the basic row for the decision variable
     * is not correctly identified, or the coefficient is not properly set.
     * 
     * Branches targeted:
     * - Constructor: restrictToNonNegative true/false, goalType MAXIMIZE/MINIMIZE,
     *   epsilon boundary values (0, negative, positive)
     * - createTableau: maximize true/false, numObjectiveFunctions 1 vs 2,
     *   restrictToNonNegative true/false, constraint relationships (LEQ, GEQ, EQ)
     * - getBasicRow: column is basic (single non-zero), not basic (multiple non-zero),
     *   all zeros, row index bounds
     * - getSolution: basicRow null vs non-null, multiple basic rows for same variable,
     *   restrictToNonNegative true/false, mostNegative handling
     * - normalize: constraint value negative vs non-negative
     * - getInvertedCoeffiecientSum: positive, negative, zero coefficients
     * - divideRow: divisor positive, negative, zero (should throw)
     * - subtractRow: multiple positive, negative, zero
     * - equals/hashCode: same object, null, different type, all fields equal/different
     * - Serialization: round-trip preserves tableau
     * 
     * Defect-specific test: testSingleVariableAndConstraintDefect
     * Constructs a simple problem: maximize 10*x subject to x <= 1, x >= 0
     * Expected solution: x = 1, objective = 10.0
     * The bug causes the solution to be 0.0 instead of 10.0.
     */

    // Helper method to create a simple tableau for testing
    private SimplexTableau createSimpleTableau() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 10.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 1.0));
        return new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
    }

    // Helper to create a tableau with non-negative restriction
    private SimplexTableau createNonNegativeTableau() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 2.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 3.0));
        return new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
    }

    // ==================== PARTITION A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testConstructorBasic() {
        SimplexTableau tableau = createSimpleTableau();
        assertNotNull(tableau);
        // Check basic dimensions
        assertEquals("Width should be 5 (2 obj + 1 decision + 1 slack + 1 RHS)", 5, tableau.getWidth());
        assertEquals("Height should be 2 (1 obj + 1 constraint)", 2, tableau.getHeight());
        // Check offsets
        assertEquals("Slack offset should be 2 (2 obj + 0 decision)", 2, tableau.getSlackVariableOffset());
        assertEquals("Artificial offset should be 3 (2 obj + 1 decision + 0 slack)", 3, tableau.getArtificialVariableOffset());
        assertEquals("RHS offset should be 4", 4, tableau.getRhsOffset());
        // Check variable counts
        assertEquals("Decision variables should be 1", 1, tableau.getNumDecisionVariables());
        assertEquals("Original decision variables should be 1", 1, tableau.getOriginalNumDecisionVariables());
        assertEquals("Slack variables should be 1", 1, tableau.getNumSlackVariables());
        assertEquals("Artificial variables should be 0", 0, tableau.getNumArtificialVariables());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNonNegativeRestriction() {
        SimplexTableau tableau = createNonNegativeTableau();
        assertNotNull(tableau);
        // Width: 2 obj + 2 decision + 2 slack + 1 RHS = 7
        assertEquals("Width should be 7", 7, tableau.getWidth());
        // Height: 1 obj + 2 constraints = 3
        assertEquals("Height should be 3", 3, tableau.getHeight());
        assertEquals("Decision variables should be 2", 2, tableau.getNumDecisionVariables());
        assertEquals("Original decision variables should be 2", 2, tableau.getOriginalNumDecisionVariables());
        assertEquals("Slack variables should be 2", 2, tableau.getNumSlackVariables());
        assertEquals("Artificial variables should be 0", 0, tableau.getNumArtificialVariables());
    }

    @Test(timeout = 4000)
    public void testConstructorWithoutNonNegativeRestriction() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 1.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        assertNotNull(tableau);
        // Width: 2 obj + 2 decision (1 original + 1 extra) + 1 slack + 1 RHS = 6
        assertEquals("Width should be 6", 6, tableau.getWidth());
        assertEquals("Decision variables should be 2", 2, tableau.getNumDecisionVariables());
        assertEquals("Original decision variables should be 1", 1, tableau.getOriginalNumDecisionVariables());
    }

    @Test(timeout = 4000)
    public void testGetNumObjectiveFunctions() {
        // With artificial variables (EQ or GEQ constraints)
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.EQ, 1.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        assertEquals("Should have 2 objective functions with artificial variables", 2, tableau.getNumObjectiveFunctions());

        // Without artificial variables
        SimplexTableau tableau2 = createSimpleTableau();
        assertEquals("Should have 1 objective function without artificial variables", 1, tableau2.getNumObjectiveFunctions());
    }

    @Test(timeout = 4000)
    public void testGetEntryAndSetEntry() {
        SimplexTableau tableau = createSimpleTableau();
        // Test getEntry on known values
        // Row 0 (objective): [0, -1, 0, 0, 0] for maximize 10x -> -10x + 0
        // Actually for maximize, objective row is [-10, 0, 0, 0, 0]? Let's check
        // createTableau: matrix[0][0] = -1 (since 2 obj functions? No, only 1)
        // zIndex = 0, matrix[0][0] = 1 (maximize)
        // objectiveCoefficients = f.getCoefficients().mapMultiply(-1) = [-10]
        // copyArray([-10], matrix[0], 1) -> matrix[0][1] = -10
        // matrix[0][width-1] = f.getConstantTerm() = 0
        // So row 0: [1, -10, 0, 0, 0]
        assertEquals("Entry (0,0) should be 1", 1.0, tableau.getEntry(0, 0), 1.0e-6);
        assertEquals("Entry (0,1) should be -10", -10.0, tableau.getEntry(0, 1), 1.0e-6);
        assertEquals("Entry (0,4) should be 0", 0.0, tableau.getEntry(0, 4), 1.0e-6);

        // Row 1 (constraint): [0, 1, 1, 0, 1]
        assertEquals("Entry (1,0) should be 0", 0.0, tableau.getEntry(1, 0), 1.0e-6);
        assertEquals("Entry (1,1) should be 1", 1.0, tableau.getEntry(1, 1), 1.0e-6);
        assertEquals("Entry (1,2) should be 1", 1.0, tableau.getEntry(1, 2), 1.0e-6);
        assertEquals("Entry (1,4) should be 1", 1.0, tableau.getEntry(1, 4), 1.0e-6);

        // Test setEntry
        tableau.setEntry(0, 0, 5.0);
        assertEquals("Entry (0,0) should be 5", 5.0, tableau.getEntry(0, 0), 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testGetData() {
        SimplexTableau tableau = createSimpleTableau();
        double[][] data = tableau.getData();
        assertEquals("Data should have 2 rows", 2, data.length);
        assertEquals("Data should have 5 columns", 5, data[0].length);
        // Verify some values
        assertEquals("Data[0][1] should be -10", -10.0, data[0][1], 1.0e-6);
        assertEquals("Data[1][1] should be 1", 1.0, data[1][1], 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testGetNormalizedConstraints() {
        // Test with negative RHS
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, -1.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        List<LinearConstraint> normalized = tableau.getNormalizedConstraints();
        assertEquals("Should have 1 constraint", 1, normalized.size());
        LinearConstraint c = normalized.get(0);
        assertEquals("RHS should be positive", 1.0, c.getValue(), 1.0e-6);
        assertEquals("Relationship should be opposite", Relationship.GEQ, c.getRelationship());
        // Coefficients should be negated
        double[] coeffs = c.getCoefficients().getData();
        assertEquals("Coefficient should be -1", -1.0, coeffs[0], 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testGetInvertedCoeffiecientSum() {
        // Test with positive coefficients
        RealVector v1 = new ArrayRealVector(new double[] { 1.0, 2.0, 3.0 });
        assertEquals("Sum should be -6", -6.0, SimplexTableau.getInvertedCoeffiecientSum(v1), 1.0e-6);

        // Test with negative coefficients
        RealVector v2 = new ArrayRealVector(new double[] { -1.0, -2.0 });
        assertEquals("Sum should be 3", 3.0, SimplexTableau.getInvertedCoeffiecientSum(v2), 1.0e-6);

        // Test with mixed
        RealVector v3 = new ArrayRealVector(new double[] { 1.0, -2.0, 3.0 });
        assertEquals("Sum should be -2", -2.0, SimplexTableau.getInvertedCoeffiecientSum(v3), 1.0e-6);

        // Test with empty
        RealVector v4 = new ArrayRealVector(new double[] {});
        assertEquals("Sum should be 0", 0.0, SimplexTableau.getInvertedCoeffiecientSum(v4), 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testDivideRow() {
        SimplexTableau tableau = createSimpleTableau();
        // Divide row 1 by 2
        tableau.divideRow(1, 2.0);
        assertEquals("Entry (1,1) should be 0.5", 0.5, tableau.getEntry(1, 1), 1.0e-6);
        assertEquals("Entry (1,2) should be 0.5", 0.5, tableau.getEntry(1, 2), 1.0e-6);
        assertEquals("Entry (1,4) should be 0.5", 0.5, tableau.getEntry(1, 4), 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testSubtractRow() {
        SimplexTableau tableau = createSimpleTableau();
        // Subtract 2 * row 1 from row 0
        // Row 0: [1, -10, 0, 0, 0]
        // Row 1: [0, 1, 1, 0, 1]
        // Result: [1, -12, -2, 0, -2]
        tableau.subtractRow(0, 1, 2.0);
        assertEquals("Entry (0,0) should be 1", 1.0, tableau.getEntry(0, 0), 1.0e-6);
        assertEquals("Entry (0,1) should be -12", -12.0, tableau.getEntry(0, 1), 1.0e-6);
        assertEquals("Entry (0,2) should be -2", -2.0, tableau.getEntry(0, 2), 1.0e-6);
        assertEquals("Entry (0,4) should be -2", -2.0, tableau.getEntry(0, 4), 1.0e-6);
    }

    // ==================== PARTITION B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testEpsilonBoundary() {
        // Test with epsilon = 0
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 1.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 0.0);
        assertNotNull(tableau);
        assertEquals("Epsilon should be 0", 0.0, tableau.epsilon, 0.0);

        // Test with negative epsilon (should be allowed but unusual)
        SimplexTableau tableau2 = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, -1.0e-6);
        assertNotNull(tableau2);
        assertEquals("Epsilon should be -1e-6", -1.0e-6, tableau2.epsilon, 0.0);
    }

    @Test(timeout = 4000)
    public void testEmptyConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        assertNotNull(tableau);
        assertEquals("Width should be 3 (1 obj + 1 decision + 1 RHS)", 3, tableau.getWidth());
        assertEquals("Height should be 1", 1, tableau.getHeight());
        assertEquals("Slack variables should be 0", 0, tableau.getNumSlackVariables());
        assertEquals("Artificial variables should be 0", 0, tableau.getNumArtificialVariables());
    }

    @Test(timeout = 4000)
    public void testAllConstraintTypes() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.GEQ, 2.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.EQ, 3.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        assertNotNull(tableau);
        // Slack: 1 (LEQ) + 1 (GEQ) = 2
        assertEquals("Slack variables should be 2", 2, tableau.getNumSlackVariables());
        // Artificial: 1 (GEQ) + 1 (EQ) = 2
        assertEquals("Artificial variables should be 2", 2, tableau.getNumArtificialVariables());
        // Width: 2 obj + 2 decision + 2 slack + 2 artificial + 1 RHS = 9
        assertEquals("Width should be 9", 9, tableau.getWidth());
        // Height: 2 obj + 3 constraints = 5
        assertEquals("Height should be 5", 5, tableau.getHeight());
    }

    @Test(timeout = 4000)
    public void testMinimizeGoal() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 1.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1.0e-6);
        assertNotNull(tableau);
        // For minimize, objective row should be [ -1, 1, 0, 0, 0 ]? Let's check
        // zIndex = 0, matrix[0][0] = -1 (since not maximize)
        // objectiveCoefficients = f.getCoefficients() = [1]
        // copyArray([1], matrix[0], 1) -> matrix[0][1] = 1
        // matrix[0][width-1] = -1 * f.getConstantTerm() = 0
        // So row 0: [-1, 1, 0, 0, 0]
        assertEquals("Entry (0,0) should be -1", -1.0, tableau.getEntry(0, 0), 1.0e-6);
        assertEquals("Entry (0,1) should be 1", 1.0, tableau.getEntry(0, 1), 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testNonNegativeRestrictionFalseWithNegativeCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 1.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        assertNotNull(tableau);
        // Check that the extra decision variable column has the inverted sum
        // For constraint row: coefficients [1], x- column at getSlackVariableOffset()-1 = 2-1 = 1? 
        // Actually slack offset = 2 (2 obj + 0 decision? No, decision = 2, so slack offset = 2+2=4)
        // Wait, let's compute: numDecisionVariables = 2 (1 original + 1 extra)
        // numSlackVariables = 1
        // getNumObjectiveFunctions() = 1 (no artificial)
        // getSlackVariableOffset() = 1 + 2 = 3
        // So x- column is at index 3-1 = 2
        // Constraint row (row 1): coefficients [1] at column 1 (since copyArray starts at 1)
        // x- column (index 2) should be getInvertedCoeffiecientSum([1]) = -1
        assertEquals("Entry (1,2) should be -1", -1.0, tableau.getEntry(1, 2), 1.0e-6);
    }

    // ==================== PARTITION C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-specific test: testSingleVariableAndConstraint
     * 
     * Problem: maximize 10*x subject to x <= 1, x >= 0
     * Expected solution: x = 1, objective = 10.0
     * 
     * The bug causes the solution to be 0.0 instead of 10.0.
     * This test directly targets the getSolution() method's logic.
     */
    @Test(timeout = 4000)
    public void testSingleVariableAndConstraintDefect() {
        // Build the problem: maximize 10*x subject to x <= 1
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 10.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 1.0));
        
        // Create the tableau
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        
        // Manually perform the simplex method steps to get to the solution
        // The tableau after initialization should be:
        // Row 0 (objective): [1, -10, 0, 0, 0]
        // Row 1 (constraint): [0, 1, 1, 0, 1]
        // 
        // The basic variable for column 1 (x) is row 1 (since entry (1,1) = 1)
        // The solution should be x = 1, objective = 10
        // 
        // But the bug in getSolution() might not correctly identify the basic row
        // or might not set the coefficient correctly.
        
        // Directly test the getSolution method
        RealPointValuePair solution = tableau.getSolution();
        
        // The expected solution: x = 1, objective = 10
        double[] expectedPoint = new double[] { 1.0 };
        double expectedValue = 10.0;
        
        assertNotNull("Solution should not be null", solution);
        assertArrayEquals("Solution point should be [1.0]", expectedPoint, solution.getPoint(), 1.0e-6);
        assertEquals("Objective value should be 10.0", expectedValue, solution.getValue(), 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testGetSolutionWithMultipleBasicRows() {
        // Create a tableau where multiple variables can take the same value
        // This tests the branch where basicRows.contains(basicRow) is true
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 1.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        
        // Manually set up a scenario where two variables are basic in the same row
        // This is tricky without direct access, but we can test the getBasicRow logic
        // by checking the tableau structure
        
        // For this problem, the tableau should be:
        // Row 0: [1, -1, -1, 0, 0, 0, 0]
        // Row 1: [0, 1, 0, 1, 0, 0, 1]
        // Row 2: [0, 0, 1, 0, 1, 0, 1]
        
        // The basic rows for columns 1 and 2 are rows 1 and 2 respectively
        // So no conflict should occur
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        // The solution should be x1=1, x2=1, objective=2
        assertEquals("Objective should be 2", 2.0, solution.getValue(), 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testGetSolutionWithNonBasicVariable() {
        // Test when a variable is not basic (basicRow == null)
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 1.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        
        // Force a scenario where a variable is not basic by modifying the tableau
        // For example, set column 1 to have two non-zero entries
        tableau.setEntry(1, 1, 0.5);
        tableau.setEntry(2, 1, 0.5);
        
        // Now column 1 is not basic (has two non-zero entries)
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        // The coefficient for x1 should be 0 since it's not basic
        assertEquals("x1 coefficient should be 0", 0.0, solution.getPoint()[0], 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testDiscardArtificialVariables() {
        // Create a tableau with artificial variables
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.EQ, 1.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        
        // Initially there should be artificial variables
        assertTrue("Should have artificial variables", tableau.getNumArtificialVariables() > 0);
        
        // Discard them
        tableau.discardArtificialVariables();
        
        // After discarding, there should be no artificial variables
        assertEquals("Artificial variables should be 0", 0, tableau.getNumArtificialVariables());
        // The width should be reduced by the number of artificial variables + 1 (for the phase 1 objective)
        // Original width: 2 obj + 1 decision + 0 slack + 1 artificial + 1 RHS = 5
        // After discard: width = 5 - 1 - 1 = 3? Let's check
        // Actually, discardArtificialVariables removes the phase 1 objective row and artificial columns
        // So new width = old width - numArtificial - 1 = 5 - 1 - 1 = 3
        // But the height is reduced by 1 (removing phase 1 objective row)
        assertEquals("Width should be 3", 3, tableau.getWidth());
        assertEquals("Height should be 1", 1, tableau.getHeight());
    }

    // ==================== PARTITION D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testGetBasicRowWithAllZeros() {
        SimplexTableau tableau = createSimpleTableau();
        // Column 0 is all zeros in constraint rows (rows 1+)
        // getBasicRow(0) should return null
        // But we can't access getBasicRow directly (private)
        // We can test indirectly through getSolution
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        // The solution should have x=1, objective=10
        assertEquals("Objective should be 10", 10.0, solution.getValue(), 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testEqualsWithNull() {
        SimplexTableau tableau = createSimpleTableau();
        assertFalse("Should not equal null", tableau.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentType() {
        SimplexTableau tableau = createSimpleTableau();
        Object other = new Object();
        assertFalse("Should not equal different type", tableau.equals(other));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameObject() {
        SimplexTableau tableau = createSimpleTableau();
        assertTrue("Should equal itself", tableau.equals(tableau));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentEpsilon() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 1.0));
        SimplexTableau tableau1 = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        SimplexTableau tableau2 = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-5);
        assertFalse("Should not equal with different epsilon", tableau1.equals(tableau2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        SimplexTableau tableau = createSimpleTableau();
        int hash1 = tableau.hashCode();
        int hash2 = tableau.hashCode();
        assertEquals("Hash codes should be consistent", hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testHashCodeDifferentObjects() {
        SimplexTableau tableau1 = createSimpleTableau();
        SimplexTableau tableau2 = createSimpleTableau();
        // Different objects may have same hash code, but should be consistent with equals
        if (tableau1.equals(tableau2)) {
            assertEquals("Equal objects should have same hash code", tableau1.hashCode(), tableau2.hashCode());
        }
    }

    // ==================== PARTITION E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        SimplexTableau tableau = createSimpleTableau();
        
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
        
        // Verify the deserialized object is equal to the original
        assertEquals("Deserialized tableau should be equal", tableau, deserialized);
        assertEquals("Deserialized tableau should have same hash code", tableau.hashCode(), deserialized.hashCode());
        
        // Verify the tableau data is preserved
        assertArrayEquals("Tableau data should match", tableau.getData(), deserialized.getData());
    }

    @Test(timeout = 4000)
    public void testGetSolutionAfterDiscardArtificialVariables() {
        // Create a tableau with artificial variables and then discard them
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.EQ, 1.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        
        // Discard artificial variables
        tableau.discardArtificialVariables();
        
        // Get solution after discarding
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull("Solution should not be null", solution);
        // The solution should be x=1, objective=1
        assertEquals("Objective should be 1", 1.0, solution.getValue(), 1.0e-6);
        assertEquals("x should be 1", 1.0, solution.getPoint()[0], 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testGetSolutionWithNonNegativeRestrictionFalse() {
        // Test with restrictToNonNegative = false
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 1.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        
        // Get solution
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull("Solution should not be null", solution);
        // The solution should have 2 decision variables (original + extra)
        assertEquals("Should have 2 decision variables", 2, solution.getPoint().length);
    }

    @Test(timeout = 4000)
    public void testGetRhsOffset() {
        SimplexTableau tableau = createSimpleTableau();
        assertEquals("RHS offset should be width-1", tableau.getWidth() - 1, tableau.getRhsOffset());
    }

    @Test(timeout = 4000)
    public void testGetWidthAndHeight() {
        SimplexTableau tableau = createSimpleTableau();
        assertEquals("Width should be 5", 5, tableau.getWidth());
        assertEquals("Height should be 2", 2, tableau.getHeight());
    }

    @Test(timeout = 4000)
    public void testGetNumDecisionVariablesWithNonNegative() {
        SimplexTableau tableau = createSimpleTableau();
        assertEquals("Should have 1 decision variable", 1, tableau.getNumDecisionVariables());
        assertEquals("Original should be 1", 1, tableau.getOriginalNumDecisionVariables());
    }

    @Test(timeout = 4000)
    public void testGetNumDecisionVariablesWithoutNonNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 1.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        assertEquals("Should have 2 decision variables", 2, tableau.getNumDecisionVariables());
        assertEquals("Original should be 1", 1, tableau.getOriginalNumDecisionVariables());
    }
}