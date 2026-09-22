package org.apache.commons.math.optimization.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;

/* [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------------------------------------------------------
 * Branch / Condition Target                       | Partition | Test Method Targeting Condition
 * ------------------------------------------------------------------------------------------------------------------
 * Defect MATH-286 (Phase 1 / solution extraction) | C         | testMath286Defect
 * Maximize vs Minimize objective branch           | A         | testObjectiveMaximizationVsMinimization
 * RestrictToNonNegative (true vs false)           | A, B      | testRestrictToNonNegativeFlagEffects
 * Normalized constraints (val < 0 vs val >= 0)    | A, B      | testConstraintNormalizationNegativeRhs
 * All relationships (LEQ, GEQ, EQ)                | A         | testConstraintRelationshipsAndVariableCounts
 * DiscardArtificialVariables (numArt == 0 / > 0)  | A, B      | testDiscardArtificialVariables
 * Row operations: divideRow and subtractRow       | A         | testRowElementaryOperations
 * Basic row discovery and basic row collisions    | A, C      | testSolutionBasicRowHandlingAndCollision
 * Non-basic variable in getSolution               | A, C      | testSolutionNonBasicVariables
 * getInvertedCoeffiecientSum static utility       | B         | testInvertedCoefficientSum
 * Empty constraints boundary                      | B         | testEmptyConstraintsTableau
 * Zero and negative constant term in objective    | B         | testObjectiveFunctionConstantTerm
 * Equals & HashCode contract integrity            | E         | testEqualsAndHashCodeContract
 * Serialization and Deserialization round-trip    | E         | testSerializationIntegrity
 * ------------------------------------------------------------------------------------------------------------------
 */
public class SimplexTableauGeminiTest {

    private static final double EPSILON = 1.0e-6;

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-286 Ground Truth)
    // =========================================================================

    /**
     * Directly reproduces the defect reported in Math-286 (SimplexSolverTest::testMath286).
     * When solving a linear program with unbounded decision variables (restrictToNonNegative = false)
     * and multiple bounds, the defective getSolution/tableau handling causes incorrect basic row
     * assignment and returns 4.6 instead of the expected 6.9.
     */
    @Test(timeout = 4000)
    public void testMath286Defect() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, -0.7, -0.8 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0 }, Relationship.LEQ, 23.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 0 }, Relationship.LEQ, 23.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 0 }, Relationship.GEQ, 12.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 0 }, Relationship.LEQ, 12.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 1 }, Relationship.GEQ, 32.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 1 }, Relationship.LEQ, 32.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        assertEquals(6.9, solution.getValue(), 1.0e-6);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testObjectiveMaximizationVsMinimization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, -3.0 }, 5.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 10.0));

        SimplexTableau tableauMax = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau tableauMin = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, EPSILON);

        // Maximize: objective row coefficients are multiplied by -1
        assertEquals(1, tableauMax.getNumObjectiveFunctions());
        assertEquals(1.0, tableauMax.getEntry(0, 0), EPSILON);
        assertEquals(-2.0, tableauMax.getEntry(0, 1), EPSILON);
        assertEquals(3.0, tableauMax.getEntry(0, 2), EPSILON);
        assertEquals(5.0, tableauMax.getEntry(0, tableauMax.getRhsOffset()), EPSILON);

        // Minimize: objective row coefficients kept as is, z-coeff is -1
        assertEquals(1, tableauMin.getNumObjectiveFunctions());
        assertEquals(-1.0, tableauMin.getEntry(0, 0), EPSILON);
        assertEquals(2.0, tableauMin.getEntry(0, 1), EPSILON);
        assertEquals(-3.0, tableauMin.getEntry(0, 2), EPSILON);
        assertEquals(-5.0, tableauMin.getEntry(0, tableauMin.getRhsOffset()), EPSILON);
    }

    @Test(timeout = 4000)
    public void testRestrictToNonNegativeFlagEffects() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 4.0));

        // When restricted to non-negative: originalNumDecisionVariables == numDecisionVariables
        SimplexTableau nonNeg = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        assertEquals(2, nonNeg.getNumDecisionVariables());
        assertEquals(2, nonNeg.getOriginalNumDecisionVariables());
        assertEquals(1, nonNeg.getNumSlackVariables());
        assertEquals(0, nonNeg.getNumArtificialVariables());

        // When not restricted: numDecisionVariables has 1 extra variable (x-)
        SimplexTableau unrestricted = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, EPSILON);
        assertEquals(3, unrestricted.getNumDecisionVariables());
        assertEquals(2, unrestricted.getOriginalNumDecisionVariables());
        assertEquals(3, unrestricted.getNegativeDecisionVariableOffset());
    }

    @Test(timeout = 4000)
    public void testConstraintRelationshipsAndVariableCounts() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 5.0)); // +1 slack
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.GEQ, 2.0)); // +1 surplus, +1 artificial
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.EQ, 4.0));  // +1 artificial

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        // 2 slack/surplus variables (LEQ and GEQ)
        assertEquals(2, tableau.getNumSlackVariables());
        // 2 artificial variables (GEQ and EQ)
        assertEquals(2, tableau.getNumArtificialVariables());
        // Phase 1 has 2 objective functions
        assertEquals(2, tableau.getNumObjectiveFunctions());

        int slackOffset = tableau.getSlackVariableOffset();
        int artOffset = tableau.getArtificialVariableOffset();
        assertEquals(tableau.getNumObjectiveFunctions() + tableau.getNumDecisionVariables(), slackOffset);
        assertEquals(slackOffset + tableau.getNumSlackVariables(), artOffset);
    }

    @Test(timeout = 4000)
    public void testDiscardArtificialVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, 5.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.GEQ, 6.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, EPSILON);
        assertEquals(2, tableau.getNumObjectiveFunctions());
        assertEquals(1, tableau.getNumArtificialVariables());

        int initialHeight = tableau.getHeight();
        int initialWidth = tableau.getWidth();

        tableau.discardArtificialVariables();

        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(initialHeight - 1, tableau.getHeight());
        assertEquals(initialWidth - 2, tableau.getWidth()); // -1 artificial var - 1 phase 1 obj row

        // Subsequent call when numArtificialVariables is 0 should be a no-op
        tableau.discardArtificialVariables();
        assertEquals(initialHeight - 1, tableau.getHeight());
    }

    @Test(timeout = 4000)
    public void testRowElementaryOperations() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2.0, 4.0 }, Relationship.LEQ, 8.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        int targetRow = 1;

        tableau.divideRow(targetRow, 2.0);
        assertEquals(1.0, tableau.getEntry(targetRow, 1), EPSILON);
        assertEquals(2.0, tableau.getEntry(targetRow, 2), EPSILON);
        assertEquals(4.0, tableau.getEntry(targetRow, tableau.getRhsOffset()), EPSILON);

        tableau.setEntry(0, 1, 3.0);
        tableau.subtractRow(0, targetRow, 3.0);
        assertEquals(0.0, tableau.getEntry(0, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSolutionBasicRowHandlingAndCollision() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, 3.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 7.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 9.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
        assertEquals(7.0, solution.getPoint()[0], EPSILON);
        assertEquals(9.0, solution.getPoint()[1], EPSILON);

        // Force column 1 and column 2 to be basic in the same row to test basic row collision branch
        tableau.setEntry(1, 2, 1.0); // Now both col 1 and col 2 are 1.0 in row 1
        RealPointValuePair collidedSolution = tableau.getSolution();
        assertEquals(7.0, collidedSolution.getPoint()[0], EPSILON);
        assertEquals(0.0, collidedSolution.getPoint()[1], EPSILON); // second var set to 0 due to collision
    }

    @Test(timeout = 4000)
    public void testSolutionNonBasicVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2.0, 3.0 }, Relationship.LEQ, 10.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        // Column 1 is non-basic (value is 2.0, not 1.0)
        RealPointValuePair solution = tableau.getSolution();
        assertEquals(0.0, solution.getPoint()[0], EPSILON);
        assertEquals(0.0, solution.getPoint()[1], EPSILON);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstraintNormalizationNegativeRhs() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, -2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // All three relationships with negative RHS
        constraints.add(new LinearConstraint(new double[] { 1.0, -1.0 }, Relationship.LEQ, -5.0));
        constraints.add(new LinearConstraint(new double[] { -2.0, 3.0 }, Relationship.GEQ, -10.0));
        constraints.add(new LinearConstraint(new double[] { 4.0, 1.0 }, Relationship.EQ, -2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        List<LinearConstraint> normalized = tableau.getNormalizedConstraints();

        assertEquals(3, normalized.size());

        // LEQ with -5 becomes GEQ with +5
        assertEquals(Relationship.GEQ, normalized.get(0).getRelationship());
        assertEquals(5.0, normalized.get(0).getValue(), EPSILON);
        assertEquals(-1.0, normalized.get(0).getCoefficients().getEntry(0), EPSILON);
        assertEquals(1.0, normalized.get(0).getCoefficients().getEntry(1), EPSILON);

        // GEQ with -10 becomes LEQ with +10
        assertEquals(Relationship.LEQ, normalized.get(1).getRelationship());
        assertEquals(10.0, normalized.get(1).getValue(), EPSILON);
        assertEquals(2.0, normalized.get(1).getCoefficients().getEntry(0), EPSILON);
        assertEquals(-3.0, normalized.get(1).getCoefficients().getEntry(1), EPSILON);

        // EQ with -2 becomes EQ with +2
        assertEquals(Relationship.EQ, normalized.get(2).getRelationship());
        assertEquals(2.0, normalized.get(2).getValue(), EPSILON);
        assertEquals(-4.0, normalized.get(2).getCoefficients().getEntry(0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testEmptyConstraintsTableau() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, 4.0 }, 0.0);
        List<LinearConstraint> emptyConstraints = Collections.emptyList();

        SimplexTableau tableau = new SimplexTableau(f, emptyConstraints, GoalType.MAXIMIZE, true, EPSILON);
        assertEquals(1, tableau.getHeight()); // Only Phase 2 objective function
        assertEquals(4, tableau.getWidth());  // Z + 2 vars + RHS
        assertEquals(2, tableau.getNumVariables());
        assertEquals(0, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
    }

    @Test(timeout = 4000)
    public void testObjectiveFunctionConstantTerm() {
        LinearObjectiveFunction fPos = new LinearObjectiveFunction(new double[] { 1.0 }, 15.0);
        LinearObjectiveFunction fNeg = new LinearObjectiveFunction(new double[] { 1.0 }, -25.0);
        List<LinearConstraint> emptyConstraints = Collections.emptyList();

        SimplexTableau tabPosMax = new SimplexTableau(fPos, emptyConstraints, GoalType.MAXIMIZE, true, EPSILON);
        assertEquals(15.0, tabPosMax.getEntry(0, tabPosMax.getRhsOffset()), EPSILON);

        SimplexTableau tabPosMin = new SimplexTableau(fPos, emptyConstraints, GoalType.MINIMIZE, true, EPSILON);
        assertEquals(-15.0, tabPosMin.getEntry(0, tabPosMin.getRhsOffset()), EPSILON);

        SimplexTableau tabNegMax = new SimplexTableau(fNeg, emptyConstraints, GoalType.MAXIMIZE, true, EPSILON);
        assertEquals(-25.0, tabNegMax.getEntry(0, tabNegMax.getRhsOffset()), EPSILON);

        SimplexTableau tabNegMin = new SimplexTableau(fNeg, emptyConstraints, GoalType.MINIMIZE, true, EPSILON);
        assertEquals(25.0, tabNegMin.getEntry(0, tabNegMin.getRhsOffset()), EPSILON);
    }

    @Test(timeout = 4000)
    public void testInvertedCoefficientSum() {
        RealVector vector = new ArrayRealVector(new double[] { 2.5, -4.0, 1.5 });
        // sum = 2.5 - 4.0 + 1.5 = 0.0; -sum = 0.0
        assertEquals(0.0, SimplexTableau.getInvertedCoeffiecientSum(vector), EPSILON);

        RealVector vector2 = new ArrayRealVector(new double[] { 1.0, 2.0, 3.0 });
        // sum = 6.0; -sum = -6.0
        assertEquals(-6.0, SimplexTableau.getInvertedCoeffiecientSum(vector2), EPSILON);
    }

    // =========================================================================
    // Partition D: Defensive & Exception Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testTableauAccessorsAndBounds() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        double[][] data = tableau.getData();
        assertNotNull(data);
        assertEquals(tableau.getHeight(), data.length);
        assertEquals(tableau.getWidth(), data[0].length);

        try {
            tableau.getEntry(999, 999);
            fail("Expected MatrixIndexException or IndexOutOfBoundsException");
        } catch (Exception expected) {
            assertTrue(expected instanceof RuntimeException);
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 3.0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 3.0);
        LinearObjectiveFunction fDiff = new LinearObjectiveFunction(new double[] { 9.0, 9.0 }, 0.0);

        List<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));
        List<LinearConstraint> c2 = new ArrayList<LinearConstraint>();
        c2.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));
        List<LinearConstraint> cDiff = new ArrayList<LinearConstraint>();
        cDiff.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.GEQ, 2.0));

        SimplexTableau t1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau t2 = new SimplexTableau(f2, c2, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau tDifferentF = new SimplexTableau(fDiff, c1, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau tDifferentC = new SimplexTableau(f1, cDiff, GoalType.MAXIMIZE, true, EPSILON);
        SimplexTableau tDifferentNonNeg = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, EPSILON);
        SimplexTableau tDifferentEpsilon = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1.0e-3);
        SimplexTableau tDifferentGoal = new SimplexTableau(f1, c1, GoalType.MINIMIZE, true, EPSILON);

        // Reflexivity
        assertEquals(t1, t1);

        // Null and type safety
        assertFalse(t1.equals(null));
        assertFalse(t1.equals("non-simplex-tableau"));

        // Symmetry & HashCode consistency
        assertEquals(t1, t2);
        assertEquals(t2, t1);
        assertEquals(t1.hashCode(), t2.hashCode());

        // Field discrepancies
        assertFalse(t1.equals(tDifferentF));
        assertFalse(t1.equals(tDifferentC));
        assertFalse(t1.equals(tDifferentNonNeg));
        assertFalse(t1.equals(tDifferentEpsilon));
        assertFalse(t1.equals(tDifferentGoal));
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, -1.0 }, 4.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.GEQ, 6.0));
        constraints.add(new LinearConstraint(new double[] { 3.0, 1.0 }, Relationship.LEQ, 12.0));

        SimplexTableau original = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, EPSILON);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SimplexTableau deserialized = (SimplexTableau) ois.readObject();
        ois.close();

        assertEquals(original.getWidth(), deserialized.getWidth());
        assertEquals(original.getHeight(), deserialized.getHeight());
        assertEquals(original.getNumDecisionVariables(), deserialized.getNumDecisionVariables());
        assertEquals(original.getNumSlackVariables(), deserialized.getNumSlackVariables());
        assertEquals(original.getNumArtificialVariables(), deserialized.getNumArtificialVariables());

        for (int i = 0; i < original.getHeight(); i++) {
            for (int j = 0; j < original.getWidth(); j++) {
                assertEquals("Mismatch at entry (" + i + ", " + j + ")",
                             original.getEntry(i, j), deserialized.getEntry(i, j), 1.0e-12);
            }
        }
        assertEquals(original, deserialized);
    }
}