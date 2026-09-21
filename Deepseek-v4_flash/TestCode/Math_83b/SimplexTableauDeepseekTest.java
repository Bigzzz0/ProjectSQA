package org.apache.commons.math.optimization.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math.optimization.GoalType;

/**
 * Advanced white-box test for SimplexTableau targeting the known defect MATH-286.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Target branches and conditions:
 * 1. getNumObjectiveFunctions(): returns 2 (when numArtificialVariables>0), else 1.
 * 2. createTableau: maximize vs minimize, restrictToNonNegative false/true.
 * 3. getSolution(): use getBasicRow (ignore objective rows) -> potential bug with extra variable.
 * 4. getBasicRow: ignoreObjectiveRows true vs false (latter not used in buggy code).
 * 5. discardArtificialVariables: boundary when numArtificialVariables=0, else reshape.
 * 6. initialize(): subtractRows for each artificial variable.
 * 7. divideRow, subtractRow: edge cases zero divisor, etc.
 * 8. equals/hashCode consistency.
 * 
 * Known defect: getSolution uses getBasicRow() (ignores objective rows) instead of
 * getBasicRowForSolution() causing wrong adjustment for negative decision variables.
 * This test directly constructs a scenario where the extra variable x- is basic only in
 * an objective row, leading to incorrect mostNegative=0 and thus a wrong solution.
 */
public class SimplexTableauDeepseekTest {

    // ---------------------------------------------------------------
    // Partition A: Core functional logic – construction and basic queries
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorWithArtificialVariables() {
        // Set up a problem with one GEQ constraint -> forces phase 1 (artificial variable)
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0, 0.0}, Relationship.GEQ, 2.0));
        constraints.add(new LinearConstraint(new double[]{0.0, 1.0}, Relationship.LEQ, 3.0));
        // restrictToNonNegative true -> no x- column
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);
        
        // Check basic dimensions after construction
        assertEquals("Number of decision variables should be 2", 2, tableau.getNumDecisionVariables());
        assertEquals("Original number of decision variables should be 2", 2, tableau.getOriginalNumDecisionVariables());
        assertEquals("Number of stack variables", 1, tableau.getNumSlackVariables()); // LEQ count=1
        assertEquals("Number of artificial variables", 1, tableau.getNumArtificialVariables()); // GEQ count=1
        assertEquals("Number of objective functions", 2, tableau.getNumObjectiveFunctions());
        // Check column offsets
        int slackOffset = 2 + 2; // obj=2 + dec=2 = 4
        assertEquals("Slack variable offset", 4, tableau.getSlackVariableOffset());
        int artificialOffset = slackOffset + 1; // + numSlack=1 = 5
        assertEquals("Artificial variable offset", 5, tableau.getArtificialVariableOffset());
        assertEquals("RHS offset", tableau.getWidth() - 1, tableau.getRhsOffset());
        // Negative decision variable offset not applicable (restrict true)
        assertEquals("Negative decision variable offset", -1, tableau.getSepativeDecisionVariableOffset()); // actually returns number (not used)
    }

    @Test(timeout = 4000)
    public void testConstructorWithoutArtificialVariables() {
        // Only LEQ constraints -> no artificial variables -> phase 1 not needed -> getNumObjetiveFunctions=1
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0}, Relationship.LEQ, 10.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, 1e-6);
        
        // restrict false -> numDecisionVariables = getNumVariables()+1 = 2
        assertEquals("Number of decision variables should be 2", 2, tableau.getNumDecisionVariables());
        assertEquals("Original number of decision variables should be 1", 1, tableau.getOriginalNumDecisionVariables());
        assertEquals("Number of stack variables", 1, tableau.getNumSlackVariables()); // LEQ=1
        assertEquals("Number of artificial variables", 0, tableau.getNumArtificialVariables());
        assertEquals("Number of objective functions", 1, tableau.getNumObjectiveFunctions());
        // Negative variable offset = getNumObjectiveFunctions() + getOriginalNumDecisionVariables() = 1+1 =2
        assertEquals("Negative decision variable offset", 2, tableau.getNegativeDecisionVariableOffset());
    }

    // ---------------------------------------------------------------
    // Partition B: Boundary Value Analysis
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetNumVariablesFromObjective() {
        // Directly test the getNumVariables method (delegates to f dimensions)
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{2.5, -3.0, 1.0}, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 1, 1}, Relationship.LEQ, 5.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);
        assertEquals("Number of variables from objective coefficient dimension", 3, tableau.getNumVariables());
    }

    @Test(timeout = 4000)
    public void testGetNormalizedConstraintsNegativeRHS() {
        // constraint with negative RHS -> requires normalization
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{2.0}, Relationship.GEQ, -5.0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1e-6);
        List<LinearConstraint> normalized = tableau.getNormalizedConstraints();
        assertEquals("One constraint after normalization", 1, normalized.size());
        LinearConstraint n = normalized.get(0);
        // Original: 2x >= -5 -> multiply by -1: -2x <= 5
        assertEquals("Coefficient should be -2", -2.0, n.getCoefficients().getEntry(0), 1e-12);
        assertEquals("Relationship should be LEQ", Relationship.LEQ, n.getRelationship());
        assertEquals("RHS should be 5", 5.0, n.getValue(), 1e-12);
    }

    // ---------------------------------------------------------------
    // Partition C: Defect-targeted branch zone (MATH-286) 
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetSolutionWithNegativeVariableBasicInObjectiveRow() {
        // This test targets the known bug where getSolution uses getBasicRow (ignores objective rows)
        // to find the extra variable column. We construct a tableau and then manually force the
        // extra variable to be basic in the phase 1 objective row (row 0) with zero on all constraint rows.
        // Buggy getBasicRow returns null → mostNegative = 0 → wrong solution.
        // Expected correct solution would subtract the value from the objective row.
        
        // Create a simple problem: minimize f = x1, with constraint x1 >= 0 (GEQ) to get artificial variable
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0}, Relationship.GEQ, 0.0));
        // restrict false -> extra variable x-
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, 1e-6);
        
        // After construction, the tableau has:
        // Row 0 (phase1 obj) : [ -1, 0, -1, 1, 1, 0, 0 ] (initialized)
        // Row 1 (phase2 obj) : [ 0, 0, 1, -1, -1, 0, 0 ] (z objective for minimization)
        // Row 2 (constraint) : [ 0, 0, 1, -1, -1, 1, 0 ] (RHS=0)
        // Columns: 0:W,1:Z,2:x1,3:x-,4:slack,5:artificial,6:RHS
        
        // Manipulate to force bug:
        // 1. Zero out the x- column in the constraint row (row2) and in row1
        tableau.setEntry(2, 3, 0.0); // row2 col3
        tableau.setEntry(1, 3, 0.0); // row1 col3
        // Now row0 col3 = 1.0, row1/row2 col3 = 0.0 -> column has 1 only in row0 (objective)
        
        // 2. Set RHS of row0 (phase1 objective) to 2.0 to simulate value of x-
        tableau.setEntry(0, 6, 2.0);
        
        // 3. Set the decision variable column (col2) : row2 col2 must be 1 (already is), row0/row1
        // Set row0 col2 = 0.0 (currently -1)
        tableau.setEntry(0, 2, 0.0);
        // Set row1 col2 = 0.0 (currently 1)
        tableau.setEntry(1, 2, 0.0);
        // Now col2 has a single 1 in row2, RHS of row2 set to 5.0
        tableau.setEntry(2, 6, 5.0);
        
        // Additional: ensure artificial variable column does not interfere (row2 col5=1, row0 col5=0, ok)
        
        // Call getSolution()
        RealPointValuePair solution = tableau.getSolution();
        double x1 = solution.getPoint()[0]; // only one original decision variable
        double objValue = solution.getValue();
        
        // Expected: x- = row0 RHS = 2, x1' = row2 RHS = 5, real x1 = 5 - 2 = 3, f = 3
        // Buggy: mostNegative = 0 → x1 = 5, f = 5
        // We assert the correct value (3) to reveal the bug -> will fail on defective version
        assertEquals("x1 value should be 3 (after subtracting x-)", 3.0, x1, 1e-6);
        assertEquals("Objective function value should be 3", 3.0, objValue, 1e-6);
    }

    // ---------------------------------------------------------------
    // Partition D: Exception and defensive guard paths 
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDivideRowByZero() {
        // DivideRow should handle division by zero -> leads to Inf/NaN
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        Collection<LinearConstraint> ct = new ArrayList<LinearConstraint>();
        ct.add(new LinearConstraint(new double[]{1.0}, Relationship.LEQ, 10.0));
        SimplexTableau tableau = new SimplexTableau(f, ct, GoalType.MAXIMIZE, true, 1e-6);
        // Divide row 1 by 0.0
        tableau.divideRow(1, 0.0);
        // Check that entry becomes Infinity (or NaN) – no exception should be thrown
        // This test simply verifies that the code does not throw NullPointerException or similar
        // We accept Infinity/NaN values.
        double val = tableau.getEntry(1, 0);
        assertTrue("Dividing by zero leads to Inf or NaN", Double.isInfinite(val) || Double.isNaN(val));
    }

    @Test(timeout = 4000)
    public void testDiscardArtificialVariablesNoop() {
        // When numArtificialVariables = 0, discardArtificialVariables does nothing.
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        Collection<LinearConstraint> ct = new ArrayList<LinearConstraint>();
        ct.add(new LinearConstraint(new double[]{1.0}, Relationship.LEQ, 10.0));
        SimplexTableau tableau = new SimplexTableau(f, ct, GoalType.MAXIMIZE, true, 1e-6);
        int widthBefore = tableau.getWidth();
        tableau.discardArtificialVariables();
        assertEquals("Width unchanged when no artificial variables", widthBefore, tableau.getWidth());
        assertEquals("height unchanged", 1 + 1, tableau.getHeight()); // 1 objective + 1 constraint
    }

    @Test(timeout = 4000)
    public void testDiscardArtificialVariablesWithArtificial() {
        // discard removes phase1 row and artificial columns
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        Collection<LinearConstraint> ct = new ArrayList<LinearConstraint>();
        ct.add(new LinearConstraint(new double[]{1.0}, Relationship.GEQ, 2.0));
        SimplexTableau tableau = new SimplexTableau(f, ct, GoalType.MAXIMIZE, true, 1e-6);
        int heightBefore = tableau.getHeight();
        int widthBefore = tableau.getWidth();
        tableau.discardArtificialVariables();
        assertEquals("Height reduced by 1 (remove phase1 row)", heightBefore - 1, tableau.getHeight());
        assertEquals("Width reduced by numArtificialColumns (1) + maybe 1? Actually: original width = objectives (2)+decsion(1)+slack(1)+artificial(1)+1 = 6. After discard: left width = width - numArtificial - 1 = 6-1-1=4. But see code: new width = getWidth() - numArtificialVariables - 1 =6-1-1=4. Yes.",
                 tableau.getWidth());
        // Check no more artificial variables
        assertEquals("numArtificialVariables should be 0", 0, tableau.getNumArtificialVariables());
    }

    // ---------------------------------------------------------------
    // Partition E: Object lifecycle and contract integrity
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeConsistency() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[]{1.0, 2.0}, 3.0);
        Collection<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[]{4.0, 5.0}, Relationship.LEQ, 6.0));
        SimplexTableau t1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1e-6);
        // Create identical tableau
        SimplexTableau t2 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1e-6);
        assertTrue("Tableaux should be equal", t1.equals(t2));
        assertEquals("HashCodes should be equal", t1.hashCode(), t2.hashCode());
        
        // Different epsilon
        SimplexTableau t3 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1e-8);
        assertFalse("Different epsilon should not be equal", t1.equals(t3));
        
        // Different restrictToNonNegative
        SimplexTableau t4 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, 1e-6);
        assertFalse("Different restrictToNonNegative should not be equal", t1.equals(t4));
        
        // Different goal type (affects tableau values)
        SimplexTableau t5 = new SimplexTableau(f1, c1, GoalType.MINIMIZE, true, 1e-6);
        assertFalse("Different goal type (tableau difference) should not be equal", t1.equals(t5));
        
        // null
        assertFalse("Equals null should return false", t1.equals(null));
        
        // own class
        assertTrue("Self equality", t1.equals(t1));
        
        // different object type
        assertFalse("Different type", t1.equals("string"));
    }

    @Test(timeout = 4000)
    public void testGetData() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{2.0, 3.0}, 0.0);
        Collection<LinearConstraint> ct = new ArrayList<LinearConstraint>();
        ct.add(new LinearConstraint(new double[]{1.0, 0.0}, Relationship.LEQ, 5.0));
        SimplexTableau tableau = new SimplexTableau(f, ct, GoalType.MAXIMIZE, true, 1e-6);
        double[][] data = tableau.getData();
        assertNotNull("Data array should not be null", data);
        assertEquals("Number of rows", 2, data.length);
        assertEquals("Number of columns", 5, data[0].length); // obj=1, dec=2, slack=1, RHS=1
    }

    // ---------------------------------------------------------------
    // Additional coverage for protected methods
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSubtractRow() {
        // Simple verification of subtractRow on a known tableau
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        Collection<LinearConstraint> ct = new ArrayList<LinearConstraint>();
        ct.add(new LinearConstraint(new double[]{1.0}, Relationship.LEQ, 10.0));
        SimplexTableau tableau = new SimplexTableau(f, ct, GoalType.MAXIMIZE, true, 1e-6);
        // Record entries before
        double entry00before = tableau.getEntry(0, 0);
        double entry10before = tableau.getEntry(1, 0);
        // Subtract row 1 from row 0 with multiple=0.5
        tableau.subtractRow(0, 1, 0.5);
        assertEquals("Row0 col0 after subtract", entry00before - 0.5 * entry10before, tableau.getEntry(0, 0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetBasicRowIndirectly() {
        // getBasicRow is private, but used in getSolution; we can test through getSolution behavior.
        // We can check that a column with a single 1 in constraint rows is recognized as basic.
        // Construct simple tableau with a LEQ constraint (no artificial) -> only phase2 objective row.
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        Collection<LinearConstraint> ct = new ArrayList<LinearConstraint>();
        ct.add(new LinearConstraint(new double[]{1.0}, Relationship.LEQ, 10.0));
        SimplexTableau tableau = new SimplexTableau(f, ct, GoalType.MAXIMIZE, true, 1e-6);
        // Column for slack variable index = getSlackVariableOffset = getNumObjectiveFunctions() + numDecisionVariables =1+1=2
        // Slack column has 1 in row1 (constraint), 0 in row0. So getBasicRow should return row1.
        // We cannot directly call it, but we can verify that getSolution works.
        RealPointValuePair sol = tableau.getSolution();
        // Since slack variable is basic, and decision variable column? Not needed.
        // This test just ensures no exception.
        assertNotNull(sol);
    }

}