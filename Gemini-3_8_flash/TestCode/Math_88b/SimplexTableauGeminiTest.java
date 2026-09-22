/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math.optimization.linear.SimplexTableau
 *
 * 1. Branch Coverage Targets:
 * - Constructor:
 *   * restrictToNonNegative: true vs false (affects numDecisionVariables, extra x- variable column).
 *   * GoalType: MAXIMIZE vs MINIMIZE (affects objective row multipliers and signs).
 *   * Constraints count: LEQ (slack), GEQ (slack + artificial), EQ (artificial).
 * - createTableau:
 *   * Phase 1 vs Phase 2 objective rows (numObjectiveFunctions == 1 vs 2).
 *   * Inverted coefficient sum calculation for x- variable when restrictToNonNegative is false.
 *   * Normalized constraints handling: negative RHS (constraint value < 0 -> flip signs & reverse relationship).
 * - discardArtificialVariables:
 *   * numArtificialVariables == 0 (early exit branch) vs numArtificialVariables > 0 (matrix reconstruction).
 * - getBasicRow:
 *   * Column not basic (no non-zero entries, or more than one non-zero entry -> returns null).
 *   * Column basic (exactly one non-zero entry in constraint rows -> returns row index).
 * - getSolution:
 *   * Non-negative restriction (restrictToNonNegative = true vs false with mostNegative basic variable offset).
 *   * Null basic row (variable takes 0 value) vs non-null basic row.
 *   * Multi-variable conflict detection branch (Math-272 bug target).
 * - Row Operations:
 *   * divideRow(row, divisor)
 *   * subtractRow(minuendRow, subtrahendRow, multiple)
 * - Getters & Helpers:
 *   * getNumVariables, getNormalizedConstraints, getWidth, getHeight, getEntry, setEntry.
 *   * getSlackVariableOffset, getArtificialVariableOffset, getRhsOffset.
 *   * getNumDecisionVariables, getOriginalNumDecisionVariables, getNumSlackVariables, getNumArtificialVariables.
 *   * getData, getInvertedCoeffiecientSum.
 * - equals & hashCode:
 *   * Identity equality (this == other).
 *   * Null comparison (other == null).
 *   * Type mismatch (ClassCastException branch via incompatible object).
 *   * Equality of identical objects vs mutated instances (different constraints, epsilon, restrictToNonNegative, etc.).
 *   * HashCode consistency.
 * - Serialization:
 *   * writeObject and readObject via ObjectOutputStream and ObjectInputStream with RealMatrix serialization.
 *
 * 2. Defect Analysis (Defects4J MATH-272):
 * - Target: SimplexTableau.getSolution() incorrectly zeroes out decision variable values when another
 *   non-basic column in the same row contains entry 1.0 (checking `tableau.getEntry(basicRow, j) == 1`).
 * - Trigger: Optimization problem with constraints where a non-basic decision variable shares a basic row with entry 1,
 *   falsely zeroing out basic decision variables (e.g., solution.getPoint()[2] returning 0.0 instead of 1.0).
 */

package org.apache.commons.math.optimization.linear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimplexTableauGeminiTest {

    private static final double DEFAULT_EPSILON = 1.0e-6;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTableauInitializationMaximizePhase2() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, 5.0 }, 10.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 2.0 }, Relationship.LEQ, 12.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        assertEquals(2, tableau.getNumVariables());
        assertEquals(2, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
        assertEquals(2, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(1, tableau.getNumObjectiveFunctions());

        // Width: 2 decision + 2 slack + 0 artificial + 1 objective + 1 RHS = 6
        assertEquals(6, tableau.getWidth());
        // Height: 2 constraints + 1 objective = 3
        assertEquals(3, tableau.getHeight());

        assertEquals(3, tableau.getSlackVariableOffset());
        assertEquals(5, tableau.getArtificialVariableOffset());
        assertEquals(5, tableau.getRhsOffset());

        // Check objective row (zIndex = 0 for 1 objective function)
        assertEquals(1.0, tableau.getEntry(0, 0), DEFAULT_EPSILON); // objective self multiplier
        assertEquals(-3.0, tableau.getEntry(0, 1), DEFAULT_EPSILON); // -1 * 3
        assertEquals(-5.0, tableau.getEntry(0, 2), DEFAULT_EPSILON); // -1 * 5
        assertEquals(10.0, tableau.getEntry(0, 5), DEFAULT_EPSILON); // constant term
    }

    @Test(timeout = 4000)
    public void testTableauInitializationMinimizePhase1WithArtificialVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, -1.0 }, 5.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // GEQ adds 1 slack and 1 artificial
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.GEQ, 2.0));
        // EQ adds 1 artificial
        constraints.add(new LinearConstraint(new double[] { 2.0, 1.0 }, Relationship.EQ, 3.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, DEFAULT_EPSILON);

        assertEquals(2, tableau.getNumVariables());
        assertEquals(2, tableau.getNumDecisionVariables());
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(2, tableau.getNumArtificialVariables());
        assertEquals(2, tableau.getNumObjectiveFunctions());

        // Width: 2 decision + 1 slack + 2 artificial + 2 objectives + 1 RHS = 8
        assertEquals(8, tableau.getWidth());
        // Height: 2 constraints + 2 objectives = 4
        assertEquals(4, tableau.getHeight());

        assertEquals(4, tableau.getSlackVariableOffset());
        assertEquals(5, tableau.getArtificialVariableOffset());
        assertEquals(7, tableau.getRhsOffset());

        // Phase 1 objective row index 0 has -1 at (0, 0)
        assertEquals(-1.0, tableau.getEntry(0, 0), DEFAULT_EPSILON);
        // Phase 2 objective row index 1 has -1 at (1, 1) because GoalType.MINIMIZE
        assertEquals(-1.0, tableau.getEntry(1, 1), DEFAULT_EPSILON);
        // Constant term is -1 * constantTerm for MINIMIZE
        assertEquals(-5.0, tableau.getEntry(1, tableau.getRhsOffset()), DEFAULT_EPSILON);
    }

    @Test(timeout = 4000)
    public void testDiscardArtificialVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.EQ, 4.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        assertEquals(1, tableau.getNumArtificialVariables());
        int originalWidth = tableau.getWidth();
        int originalHeight = tableau.getHeight();

        tableau.discardArtificialVariables();

        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(originalWidth - 2, tableau.getWidth());
        assertEquals(originalHeight - 1, tableau.getHeight());

        // Second call should early exit cleanly
        tableau.discardArtificialVariables();
        assertEquals(0, tableau.getNumArtificialVariables());
    }

    @Test(timeout = 4000)
    public void testRowOperationsDivideAndSubtract() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2.0 }, Relationship.LEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 4.0 }, Relationship.LEQ, 20.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        // Divide row 1 (constraint 1) by 2.0
        tableau.divideRow(1, 2.0);
        assertEquals(1.0, tableau.getEntry(1, 1), DEFAULT_EPSILON); // coefficient
        assertEquals(5.0, tableau.getEntry(1, tableau.getRhsOffset()), DEFAULT_EPSILON);

        // Subtract 4 * row 1 from row 2
        tableau.subtractRow(2, 1, 4.0);
        assertEquals(0.0, tableau.getEntry(2, 1), DEFAULT_EPSILON);
        assertEquals(0.0, tableau.getEntry(2, tableau.getRhsOffset()), DEFAULT_EPSILON);

        // Set entry and verify
        tableau.setEntry(2, 1, 9.5);
        assertEquals(9.5, tableau.getEntry(2, 1), DEFAULT_EPSILON);
    }

    @Test(timeout = 4000)
    public void testGetData() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        double[][] data = tableau.getData();

        assertNotNull(data);
        assertEquals(tableau.getHeight(), data.length);
        assertEquals(tableau.getWidth(), data[0].length);
        assertEquals(tableau.getEntry(0, 0), data[0][0], DEFAULT_EPSILON);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Non-Negative Relaxation
    // =========================================================================

    @Test(timeout = 4000)
    public void testRestrictToNonNegativeFalse() {
        // When restrictToNonNegative is false, an additional decision variable (x-) is introduced
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, -2.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, DEFAULT_EPSILON);

        assertEquals(2, tableau.getNumVariables());
        assertEquals(3, tableau.getNumDecisionVariables()); // 2 + 1
        assertEquals(2, tableau.getOriginalNumDecisionVariables());

        // Inverted sum for objective vector [-3.0, 2.0] is -(-3.0 + 2.0) = 1.0
        // Located at getSlackVariableOffset() - 1
        int xMinusCol = tableau.getSlackVariableOffset() - 1;
        assertEquals(1.0, tableau.getEntry(0, xMinusCol), DEFAULT_EPSILON);

        // Inverted sum for constraint vector [1.0, 1.0] is -(1.0 + 1.0) = -2.0
        assertEquals(-2.0, tableau.getEntry(1, xMinusCol), DEFAULT_EPSILON);
    }

    @Test(timeout = 4000)
    public void testInvertedCoefficientSumStaticHelper() {
        RealVector vector = new ArrayRealVector(new double[] { 1.5, -2.5, 4.0 });
        double invertedSum = SimplexTableau.getInvertedCoeffiecientSum(vector);
        // sum = 1.5 - 2.5 + 4.0 = 3.0 -> inverted = -3.0
        assertEquals(-3.0, invertedSum, DEFAULT_EPSILON);

        RealVector emptyVector = new ArrayRealVector(new double[0]);
        assertEquals(0.0, SimplexTableau.getInvertedCoeffiecientSum(emptyVector), DEFAULT_EPSILON);
    }

    @Test(timeout = 4000)
    public void testNegativeRhsNormalization() {
        // Negative constraint value should be normalized:
        // -2x1 + 3x2 <= -4  =>  2x1 - 3x2 >= 4
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { -2.0, 3.0 }, Relationship.LEQ, -4.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        List<LinearConstraint> normalized = tableau.getNormalizedConstraints();

        assertEquals(1, normalized.size());
        LinearConstraint norm = normalized.get(0);
        assertEquals(4.0, norm.getValue(), DEFAULT_EPSILON);
        assertEquals(Relationship.GEQ, norm.getRelationship());
        assertEquals(2.0, norm.getCoefficients().getEntry(0), DEFAULT_EPSILON);
        assertEquals(-3.0, norm.getCoefficients().getEntry(1), DEFAULT_EPSILON);
    }

    @Test(timeout = 4000)
    public void testGetSolutionWithNoNonZeroEntries() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, 3.0 }, 7.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 10.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        // Initially without pivoting, basic columns are slack variables, decision variables are non-basic (0)
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
        assertEquals(0.0, solution.getPoint()[0], DEFAULT_EPSILON);
        assertEquals(0.0, solution.getPoint()[1], DEFAULT_EPSILON);
        assertEquals(7.0, solution.getValue(), DEFAULT_EPSILON); // f(0, 0) = 7.0
    }

    @Test(timeout = 4000)
    public void testGetSolutionWithNegativeBasicRowShift() {
        // Unrestricted variables: solution should subtract mostNegative when x- is basic
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, DEFAULT_EPSILON);

        // Artificially make x- (col 2) basic in row 1
        tableau.setEntry(1, 1, 0.0);
        tableau.setEntry(1, 2, 1.0); // x- is at index 2
        tableau.setEntry(1, tableau.getRhsOffset(), 3.0);

        RealPointValuePair solution = tableau.getSolution();
        assertEquals(1, solution.getPoint().length);
        // x0 is not basic (0), mostNegative is 3.0, so x0 = 0 - 3.0 = -3.0
        assertEquals(-3.0, solution.getPoint()[0], DEFAULT_EPSILON);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-272 Ground Truth)
    // =========================================================================

    /**
     * Exact scenario from MATH-272 / SimplexSolverTest.testMath272.
     * Bug: SimplexTableau.getSolution() zeroes out decision variables when a previous
     * non-basic column in the basic row has an entry of 1.0.
     * Correct behavior: decision variable x2 must be 1.0, not 0.0.
     */
    @Test(timeout = 4000)
    public void testMath272DefectResolution() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, 2.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0, 1.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0, 0.0 }, Relationship.GEQ, 1.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertNotNull(solution);
        assertEquals(0.0, solution.getPoint()[0], 1.0e-7);
        assertEquals(1.0, solution.getPoint()[1], 1.0e-7);
        // FAILS on defective code: returns 0.0 instead of 1.0
        assertEquals(1.0, solution.getPoint()[2], 1.0e-7);
        assertEquals(3.0, solution.getValue(), 1.0e-7);
    }

    /**
     * Direct unit testing of getSolution() triggering the basic row column conflict logic.
     */
    @Test(timeout = 4000)
    public void testGetSolutionMultipleVariablesSameRowConflict() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        // Make variable 0 basic in row 1 with value 5
        tableau.setEntry(1, 1, 1.0);
        // Make variable 1 also basic in row 1 with value 5 (identical column)
        tableau.setEntry(1, 2, 1.0);
        tableau.setEntry(1, tableau.getRhsOffset(), 5.0);

        RealPointValuePair solution = tableau.getSolution();
        // First variable takes the value 5, second variable is discarded (0)
        assertEquals(5.0, solution.getPoint()[0], DEFAULT_EPSILON);
        assertEquals(0.0, solution.getPoint()[1], DEFAULT_EPSILON);
    }

    // =========================================================================
    // Partition D: Contract Integrity, Equals, HashCode & Serialization
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 3.0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 3.0);
        Collection<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 5.0));
        Collection<LinearConstraint> c2 = new ArrayList<LinearConstraint>();
        c2.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tab1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        SimplexTableau tab2 = new SimplexTableau(f2, c2, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        // Reflexive
        assertTrue(tab1.equals(tab1));
        assertEquals(tab1.hashCode(), tab1.hashCode());

        // Symmetric
        assertTrue(tab1.equals(tab2));
        assertTrue(tab2.equals(tab1));
        assertEquals(tab1.hashCode(), tab2.hashCode());

        // Null comparison
        assertFalse(tab1.equals(null));

        // Incompatible type
        assertFalse(tab1.equals("NotATableau"));

        // Different restrictToNonNegative
        SimplexTableau tabDifferentRestrict = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, DEFAULT_EPSILON);
        assertFalse(tab1.equals(tabDifferentRestrict));

        // Different epsilon
        SimplexTableau tabDifferentEpsilon = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1.0e-3);
        assertFalse(tab1.equals(tabDifferentEpsilon));

        // Different objective function
        LinearObjectiveFunction fDiff = new LinearObjectiveFunction(new double[] { 9.0, 9.0 }, 3.0);
        SimplexTableau tabDifferentObj = new SimplexTableau(fDiff, c1, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        assertFalse(tab1.equals(tabDifferentObj));

        // Different constraints
        Collection<LinearConstraint> cDiff = new ArrayList<LinearConstraint>();
        cDiff.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.GEQ, 5.0));
        SimplexTableau tabDifferentConstraints = new SimplexTableau(f1, cDiff, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        assertFalse(tab1.equals(tabDifferentConstraints));

        // Modified tableau matrix data
        SimplexTableau tabModifiedMatrix = new SimplexTableau(f2, c2, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        tabModifiedMatrix.setEntry(0, 0, 999.0);
        assertFalse(tab1.equals(tabModifiedMatrix));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 4.0, -1.0 }, 2.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2.0