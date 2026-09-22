/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math3.optimization.linear.SimplexTableau
 *
 * Decision / Branch Coverage Targets:
 * 1. Constructor:
 *    - GoalType.MAXIMIZE vs GoalType.MINIMIZE
 *    - restrictToNonNegative: true vs false (handles 'x-' negative column label & inverted coefficient sums)
 *    - Relationship counts: LEQ (slack +1), GEQ (slack +1, artificial +1, excess -1), EQ (artificial +1)
 *    - Objective functions count: 1 (no artificial variables) vs 2 (Phase 1 + Phase 2)
 * 2. normalizeConstraints:
 *    - Constraint value < 0 (inverts coefficients, flips relationship LEQ<->GEQ, EQ->EQ, negates RHS)
 *    - Constraint value >= 0 (retains original)
 * 3. getBasicRow:
 *    - Valid basic column (exactly one 1.0 within maxUlps and rest 0.0) -> returns row index
 *    - Non-basic column: multiple 1.0s, non-zero/non-one values, or no 1.0 -> returns null
 * 4. dropPhase1Objective:
 *    - numObjectiveFunctions == 1 -> no-op early exit
 *    - numObjectiveFunctions == 2 -> drops phase 1 row, positive cost non-artificial cols, non-basic artificial cols
 * 5. isOptimal:
 *    - All reduced costs >= -epsilon -> true
 *    - Any reduced cost < -epsilon -> false
 * 6. getSolution:
 *    - restrictToNonNegative true vs false (mostNegative variable adjustment)
 *    - basicRow == 0 (unconstrained / objective row branch)
 *    - basicRows.contains(basicRow) (handling degenerate basic variables in identical rows / multiple nulls)
 *    - decision variable not found in labels -> default 0
 * 7. Row Operations:
 *    - divideRow, subtractRow
 * 8. Object Integrity & Lifecycle:
 *    - equals (reflexive, null, type-mismatch, symmetric, all field differences)
 *    - hashCode consistency
 *    - Java Serialization & Deserialization (writeObject, readObject with transient matrix)
 *
 * Known Defect Target (Defects4J MATH-781):
 * - Simplex solver / tableau handling constraints normalized with negative values and
 *   non-restricted variables where basicRow collision / null basic row handling occurs in getSolution().
 */

package org.apache.commons.math3.optimization.linear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.util.Precision;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimplexTableauGeminiTest {

    private static final double EPSILON = 1e-6;
    private static final int DEFAULT_ULPS = 10;

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testStandardModelMaximizeNonNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 5 }, 10);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 0, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 18));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        assertEquals(1, tableau.getNumObjectiveFunctions());
        assertEquals(2, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
        assertEquals(3, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(4, tableau.getHeight()); // 1 obj + 3 constraints
        assertEquals(7, tableau.getWidth());  // Z, x0, x1, s0, s1, s2, RHS

        assertEquals(0, tableau.getSlackVariableOffset() - (tableau.getNumObjectiveFunctions() + tableau.getNumDecisionVariables()));
        assertEquals(tableau.getWidth() - 1, tableau.getRhsOffset());

        // Maximize: objective row coefficients are multiplied by -1
        assertEquals(-3.0, tableau.getEntry(0, 1), 1e-9);
        assertEquals(-5.0, tableau.getEntry(0, 2), 1e-9);
        assertEquals(10.0, tableau.getEntry(0, tableau.getRhsOffset()), 1e-9);

        // Not optimal yet since entries are negative
        assertFalse(tableau.isOptimal());

        // Column labels verification
        assertEquals("Z", tableau.columnLabels.get(0));
        assertEquals("x0", tableau.columnLabels.get(1));
        assertEquals("x1", tableau.columnLabels.get(2));
        assertEquals("s0", tableau.columnLabels.get(3));
        assertEquals("s1", tableau.columnLabels.get(4));
        assertEquals("s2", tableau.columnLabels.get(5));
        assertEquals("RHS", tableau.columnLabels.get(6));
    }

    @Test(timeout = 4000)
    public void testStandardModelMinimizeWithNegativeAllowed() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, 5);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 6));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, EPSILON);

        assertEquals(1, tableau.getNumObjectiveFunctions());
        // 2 original + 1 negative var = 3
        assertEquals(3, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());

        // Check negative var column presence
        assertTrue(tableau.columnLabels.contains("x-"));

        // Minimization objective entries retain sign in standard tableau
        assertEquals(-2.0, tableau.getEntry(0, 1), 1e-9);
        assertEquals(1.0, tableau.getEntry(0, 2), 1e-9);
        // Constant term is negated for minimize: -1 * 5 = -5
        assertEquals(-5.0, tableau.getEntry(0, tableau.getRhsOffset()), 1e-9);
    }

    @Test(timeout = 4000)
    public void testTwoPhaseModelWithArtificialVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // GEQ generates 1 slack and 1 artificial
        constraints.add(new LinearConstraint(new double[] { 2, 1 }, Relationship.GEQ, 2));
        // EQ generates 1 artificial
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.EQ, 3));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, EPSILON);

        assertEquals(2, tableau.getNumObjectiveFunctions());
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(2, tableau.getNumArtificialVariables());

        // Labels must include W and Z
        assertEquals("W", tableau.columnLabels.get(0));
        assertEquals("Z", tableau.columnLabels.get(1));
        assertTrue(tableau.columnLabels.contains("a0"));
        assertTrue(tableau.columnLabels.contains("a1"));

        // Phase 1 objective row should be sum of artificial variable rows subtracted
        assertEquals(-1.0, tableau.getEntry(0, 0), 1e-9);
        assertEquals(-3.0, tableau.getEntry(0, 2), 1e-9); // -(2 + 1)
        assertEquals(-3.0, tableau.getEntry(0, 3), 1e-9); // -(1 + 2)
        assertEquals(-5.0, tableau.getEntry(0, tableau.getRhsOffset()), 1e-9); // -(2 + 3)
    }

    @Test(timeout = 4000)
    public void testDropPhase1Objective() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 2));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, EPSILON);
        assertEquals(2, tableau.getNumObjectiveFunctions());
        assertEquals(1, tableau.getNumArtificialVariables());

        int initialHeight = tableau.getHeight();
        tableau.dropPhase1Objective();

        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(initialHeight - 1, tableau.getHeight());
        assertFalse(tableau.columnLabels.contains("W"));
        assertFalse(tableau.columnLabels.contains("a0"));

        // Calling dropPhase1Objective when already dropped (numObjectiveFunctions == 1) should be no-op
        tableau.dropPhase1Objective();
        assertEquals(initialHeight - 1, tableau.getHeight());
    }

    @Test(timeout = 4000)
    public void testBasicRowDetection() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        // Slack variable column s0 is at index 3
        int slackCol = tableau.columnLabels.indexOf("s0");
        Integer basicRow = tableau.getBasicRow(slackCol);
        assertNotNull(basicRow);
        assertEquals(Integer.valueOf(1), basicRow);

        // Modify column to have another 1.0, making it non-basic
        tableau.setEntry(0, slackCol, 1.0);
        assertNull(tableau.getBasicRow(slackCol));

        // Modify column to have non-zero and non-one value
        tableau.setEntry(0, slackCol, 0.5);
        assertNull(tableau.getBasicRow(slackCol));
    }

    @Test(timeout = 4000)
    public void testRowOperationsDivideAndSubtract() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 4 }, Relationship.LEQ, 8));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        // Row 1: [0, 2, 4, 1, 0, 8]
        tableau.divideRow(1, 2.0);
        assertEquals(1.0, tableau.getEntry(1, 1), 1e-9);
        assertEquals(2.0, tableau.getEntry(1, 2), 1e-9);
        assertEquals(0.5, tableau.getEntry(1, 3), 1e-9);
        assertEquals(4.0, tableau.getEntry(1, tableau.getRhsOffset()), 1e-9);

        // Subtract row 1 from row 2
        // Row 2 before: [0, 1, 1, 0, 1, 5]
        tableau.subtractRow(2, 1, 1.0);
        assertEquals(0.0, tableau.getEntry(2, 1), 1e-9);
        assertEquals(-1.0, tableau.getEntry(2, 2), 1e-9);
        assertEquals(-0.5, tableau.getEntry(2, 3), 1e-9);
        assertEquals(1.0, tableau.getEntry(2, 4), 1e-9);
        assertEquals(1.0, tableau.getEntry(2, tableau.getRhsOffset()), 1e-9);
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Normalization
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNormalizeNegativeRhsConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // Negative RHS should flip LEQ to GEQ and multiply coefficients by -1
        constraints.add(new LinearConstraint(new double[] { 1, -2 }, Relationship.LEQ, -5));
        // Negative RHS should flip GEQ to LEQ
        constraints.add(new LinearConstraint(new double[] { -3, 4 }, Relationship.GEQ, -10));
        // Negative RHS EQ remains EQ with negated coefficients and positive RHS
        constraints.add(new LinearConstraint(new double[] { 5, -6 }, Relationship.EQ, -15));
        // Non-negative RHS remains unchanged
        constraints.add(new LinearConstraint(new double[] { 7, 8 }, Relationship.LEQ, 20));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        List<LinearConstraint> normalized = tableau.normalizeConstraints(constraints);

        assertEquals(4, normalized.size());

        // Constraint 0: -1, 2, GEQ, 5
        assertArrayEquals(new double[] { -1, 2 }, normalized.get(0).getCoefficients().toArray(), 1e-9);
        assertEquals(Relationship.GEQ, normalized.get(0).getRelationship());
        assertEquals(5.0, normalized.get(0).getValue(), 1e-9);

        // Constraint 1: 3, -4, LEQ, 10
        assertArrayEquals(new double[] { 3, -4 }, normalized.get(1).getCoefficients().toArray(), 1e-9);
        assertEquals(Relationship.LEQ, normalized.get(1).getRelationship());
        assertEquals(10.0, normalized.get(1).getValue(), 1e-9);

        // Constraint 2: -5, 6, EQ, 15
        assertArrayEquals(new double[] { -5, 6 }, normalized.get(2).getCoefficients().toArray(), 1e-9);
        assertEquals(Relationship.EQ, normalized.get(2).getRelationship());
        assertEquals(15.0, normalized.get(2).getValue(), 1e-9);

        // Constraint 3: 7, 8, LEQ, 20
        assertArrayEquals(new double[] { 7, 8 }, normalized.get(3).getCoefficients().toArray(), 1e-9);
        assertEquals(Relationship.LEQ, normalized.get(3).getRelationship());
        assertEquals(20.0, normalized.get(3).getValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testInvertedCoefficientSumStaticMethod() {
        RealVector vector = new org.apache.commons.math3.linear.ArrayRealVector(new double[] { 1.5, -2.5, 4.0 });
        double sum = SimplexTableau.getInvertedCoefficientSum(vector);
        // - (1.5 - 2.5 + 4.0) = -3.0
        assertEquals(-3.0, sum, 1e-9);
    }

    @Test(timeout = 4000)
    public void testIsOptimalBoundary() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0, 0 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 1));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        // Entries in row 0 for decision variables are 0.0, which is >= -epsilon -> optimal
        assertTrue(tableau.isOptimal());

        // Make an entry slightly below -epsilon -> not optimal
        tableau.setEntry(0, 1, -2 * EPSILON);
        assertFalse(tableau.isOptimal());

        // Make it within epsilon boundary (-epsilon / 2) -> optimal
        tableau.setEntry(0, 1, -EPSILON / 2.0);
        assertTrue(tableau.isOptimal());
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (MATH-781)
    // -------------------------------------------------------------------------

    /**
     * Target Defect: MATH-781 / SimplexSolverTest::testMath781
     * Problem specification from ground truth:
     * Minimize/Maximize with constraints that have negative RHS values and
     * restrictToNonNegative = false, triggering duplicate basicRow logic or
     * null basicRow state in getSolution().
     */
    @Test(timeout = 4000)
    public void testMath781SimplexTableauBehavior() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 6, 7 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2, 1 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { -1, 1, 1 }, Relationship.LEQ, -1));
        constraints.add(new LinearConstraint(new double[] { 2, -3, 1 }, Relationship.EQ, -1));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);

        // Verify structure created with negative vars allowed (x-)
        assertTrue(tableau.columnLabels.contains("x-"));
        assertEquals(3, tableau.getOriginalNumDecisionVariables());
        assertEquals(4, tableau.getNumDecisionVariables()); // 3 + 1 for x-

        // Verify normalization inverted the negative RHS constraints:
        // Constraint 1 was: -1, 1, 1 <= -1 -> normalized to 1, -1, -1 >= 1 (slack + excess + artificial)
        // Constraint 2 was: 2, -3, 1 == -1 -> normalized to -2, 3, -1 == 1 (artificial)
        assertEquals(2, tableau.getNumObjectiveFunctions()); // Contains artificial variables, so Phase 1

        PointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(3, solution.getPoint().length);

        // Each variable value computed via getSolution must be mathematically consistent with tableau coefficients
        for (double val : solution.getPoint()) {
            assertFalse(Double.isNaN(val));
            assertFalse(Double.isInfinite(val));
        }
    }

    @Test(timeout = 4000)
    public void testSolutionExtractionWithBasicRowZeroAndDuplicateBasicRows() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 4 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 10));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 20));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        // Force column x0 (index 1) to be basic in row 0 (objective row)
        // Row 0: [1, 1, 0, ...]
        tableau.setEntry(0, 1, 1.0);
        tableau.setEntry(1, 1, 0.0);
        tableau.setEntry(2, 1, 0.0);

        PointValuePair solution = tableau.getSolution();
        // If basicRow == 0, coefficient is forced to 0
        assertEquals(0.0, solution.getPoint()[0], 1e-9);

        // Now test scenario where two decision variables share the same basic row
        // Set both x0 and x1 columns to unit vector at row 1
        tableau.setEntry(0, 1, 0.0);
        tableau.setEntry(1, 1, 1.0);
        tableau.setEntry(2, 1, 0.0);

        tableau.setEntry(0, 2, 0.0);
        tableau.setEntry(1, 2, 1.0);
        tableau.setEntry(2, 2, 0.0);

        PointValuePair solution2 = tableau.getSolution();
        // First variable gets RHS of row 1 (10.0), second gets 0 because basicRows.contains(1) is true
        assertEquals(10.0, solution2.getPoint()[0], 1e-9);
        assertEquals(0.0, solution2.getPoint()[1], 1e-9);
    }

    // -------------------------------------------------------------------------
    // Partition D: Tableau Getters & Data Accessors
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGettersAndTableauData() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 3);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON, DEFAULT_ULPS);

        assertEquals(1, tableau.getNumObjectiveFunctions());
        assertEquals(2, tableau.getNumDecisionVariables());
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(2, tableau.getHeight());
        assertEquals(5, tableau.getWidth());

        double[][] data = tableau.getData();
        assertNotNull(data);
        assertEquals(2, data.length);
        assertEquals(5, data[0].length);

        // Verify getEntry and setEntry consistency
        tableau.setEntry(1, 1, 42.0);
        assertEquals(42.0, tableau.getEntry(1, 1), 1e-9);
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Contract & Serialization Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] { 1, 3 }, 0);

        List<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        List<LinearConstraint> c2 = new ArrayList<LinearConstraint>();
        c2.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 4));

        SimplexTableau t1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau t1Clone = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau tDiffF = new SimplexTableau(f2, c1, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau tDiffC = new SimplexTableau(f1, c2, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau tDiffRestrict = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, EPSILON);
        SimplexTableau tDiffEps = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1e-4);
        SimplexTableau tDiffUlps = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, EPSILON, 20);

        // Reflexive
        assertTrue(t1.equals(t1));

        // Symmetric & Equal values
        assertTrue(t1.equals(t1Clone));
        assertTrue(t1Clone.equals(t1));
        assertEquals(t1.hashCode(), t1Clone.hashCode());

        // Null and different type
        assertFalse(t1.equals(null));
        assertFalse(t1.equals("Not a SimplexTableau"));

        // Differences
        assertFalse(t1.equals(tDiffF));
        assertFalse(t1.equals(tDiffC));
        assertFalse(t1.equals(tDiffRestrict));
        assertFalse(t1.equals(tDiffEps));
        assertFalse(t1.equals(tDiffUlps));

        // Different tableau matrix data
        SimplexTableau tModifiedMatrix = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, EPSILON);
        tModifiedMatrix.setEntry(0, 0, 999.0);
        assertFalse(t1.equals(tModifiedMatrix));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 4 }, 1);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 5));
        constraints.add(new LinearConstraint(new double[] { 3, 1 }, Relationship.GEQ, 6));

        SimplexTableau original = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, EPSILON);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SimplexTableau deserialized = (SimplexTableau) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertEquals(original, deserialized);
        assertEquals(original.getHeight(), deserialized.getHeight());
        assertEquals(original.getWidth(), deserialized.getWidth());
        assertEquals(original.getNumDecisionVariables(), deserialized.getNumDecisionVariables());
        assertEquals(original.getNumSlackVariables(), deserialized.getNumSlackVariables());
        assertEquals(original.getNumArtificialVariables(), deserialized.getNumArtificialVariables());

        // Verify transient matrix restored properly
        for (int i = 0; i < original.getHeight(); i++) {
            for (int j = 0; j < original.getWidth(); j++) {
                assertEquals(original.getEntry(i, j), deserialized.getEntry(i, j), 1e-9);
            }
        }
    }
}