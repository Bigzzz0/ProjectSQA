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

package org.apache.commons.math.optimization.linear;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: SimplexTableau
 * DEFECT TARGET: Math-83 (SimplexSolverTest::testSingleVariableAndConstraint failure)
 *
 * 1. Branch: getBasicRow(col) validation & identification
 *    - In the defective version, getBasicRow() iterated only over constraint rows (ignoring row 0)
 *      and did not verify whether entry == 1.0 (checking only entry != 0.0). For unrestricted variables
 *      (restrictToNonNegative = false), negative coefficients (e.g., -1.0) in non-basic columns
 *      were mistakenly flagged as basic, setting mostNegative = RHS instead of 0.
 *    - Partition C specifically reproduces this condition and asserts correct decision variable values.
 *
 * 2. Branch: discardArtificialVariables()
 *    - Branch: numArtificialVariables == 0 -> early return.
 *    - Branch: numArtificialVariables > 0 -> dimension reduction, stripping phase 1 objective row and cols.
 *
 * 3. Branch: getSolution()
 *    - Branch: basicRows.contains(basicRow) -> sets coefficient to 0 to break ties.
 *    - Branch: basicRow == null -> non-basic variable takes value 0.
 *    - Branch: restrictToNonNegative ? 0 : mostNegative -> variable translation for unrestricted vars.
 *
 * 4. Branch: normalize(constraint)
 *    - Branch: constraint.getValue() < 0 -> negate coefficients, reverse relationship, negate value.
 *    - Branch: constraint.getValue() >= 0 -> preserve constraint as-is.
 *
 * 5. Branch: createTableau(maximize)
 *    - GoalType.MAXIMIZE vs GoalType.MINIMIZE (coefficients negated or unchanged, sign of constant term).
 *    - Constraints relationships: LEQ (+1 slack), GEQ (-1 excess, +1 artificial), EQ (+1 artificial).
 *    - 1 vs 2 objective functions (zIndex = 0 vs 1, matrix[0][0] = -1).
 *
 * 6. Contract Integrity:
 *    - equals(): identity, null check, type check, field comparisons (epsilon, f, constraints, tableau).
 *    - hashCode(): bitwise consistency with equals.
 *    - Serializable: Custom serialization / deserialization with MatrixUtils RealMatrix.
 * ----------------------------------------------------------------------------------------------------
 */
public class SimplexTableauGeminiTest {

    private static final double DEFAULT_EPSILON = 1.0e-6;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTableauDimensionsAndVariableCountsStandardMax() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, -1.0 }, 10.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.GEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.EQ, 2.0));

        // restrictToNonNegative = true -> numDecisionVariables = 2
        // numSlackVariables = 1 (LEQ) + 1 (GEQ) = 2
        // numArtificialVariables = 1 (GEQ) + 1 (EQ) = 2
        // Phase 1 exists -> numObjectiveFunctions = 2
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        assertEquals(2, tableau.getNumVariables());
        assertEquals(2, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
        assertEquals(2, tableau.getNumSlackVariables());
        assertEquals(2, tableau.getNumArtificialVariables());
        assertEquals(2, tableau.getNumObjectiveFunctions());

        // Width: 2 (obj) + 2 (decision) + 2 (slack) + 2 (artificial) + 1 (RHS) = 9
        // Height: 2 (obj) + 3 (constraints) = 5
        assertEquals(9, tableau.getWidth());
        assertEquals(5, tableau.getHeight());

        assertEquals(4, tableau.getSlackVariableOffset());
        assertEquals(6, tableau.getArtificialVariableOffset());
        assertEquals(8, tableau.getRhsOffset());
    }

    @Test(timeout = 4000)
    public void testTableauDimensionsMinimizeWithUnrestrictedVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, 5.0 }, -2.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.LEQ, 6.0));

        // restrictToNonNegative = false -> numDecisionVariables = 2 + 1 = 3 (x1, x2, x-)
        // numSlackVariables = 1 (LEQ)
        // numArtificialVariables = 0
        // numObjectiveFunctions = 1
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, DEFAULT_EPSILON);

        assertEquals(2, tableau.getNumVariables());
        assertEquals(3, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(1, tableau.getNumObjectiveFunctions());

        // Width: 1 (obj) + 3 (decision) + 1 (slack) + 0 (art) + 1 (RHS) = 6
        // Height: 1 (obj) + 1 (constraint) = 2
        assertEquals(6, tableau.getWidth());
        assertEquals(2, tableau.getHeight());
        assertEquals(4, tableau.getSlackVariableOffset());
        assertEquals(5, tableau.getRhsOffset());

        // For MINIMIZE, objective constant term is -1 * constantTerm = 2.0
        assertEquals(2.0, tableau.getEntry(0, tableau.getRhsOffset()), DEFAULT_EPSILON);
    }

    @Test(timeout = 4000)
    public void testDiscardArtificialVariablesWithArtificialVariablesPresent() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.EQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        assertEquals(1, tableau.getNumArtificialVariables());
        int originalWidth = tableau.getWidth();
        int originalHeight = tableau.getHeight();

        tableau.discardArtificialVariables();

        assertEquals(0, tableau.getNumArtificialVariables());
        // Phase 1 objective row removed -> height - 1
        assertEquals(originalHeight - 1, tableau.getHeight());
        // Phase 1 objective col and 1 artificial col removed -> width - 2
        assertEquals(originalWidth - 2, tableau.getWidth());
    }

    @Test(timeout = 4000)
    public void testDiscardArtificialVariablesWhenNonePresent() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 10.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        int widthBefore = tableau.getWidth();
        int heightBefore = tableau.getHeight();

        tableau.discardArtificialVariables(); // Early return branch

        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(widthBefore, tableau.getWidth());
        assertEquals(heightBefore, tableau.getHeight());
    }

    @Test(timeout = 4000)
    public void testRowOperationsDivideAndSubtract() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2.0, 4.0 }, Relationship.LEQ, 8.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        // Divide row 1 (constraint row) by 2.0
        tableau.divideRow(1, 2.0);
        assertEquals(1.0, tableau.getEntry(1, 1), DEFAULT_EPSILON);
        assertEquals(2.0, tableau.getEntry(1, 2), DEFAULT_EPSILON);
        assertEquals(4.0, tableau.getEntry(1, tableau.getRhsOffset()), DEFAULT_EPSILON);

        // Modify an entry directly and verify
        tableau.setEntry(0, 1, 5.0);
        assertEquals(5.0, tableau.getEntry(0, 1), DEFAULT_EPSILON);

        // Subtract 5 * row 1 from row 0
        tableau.subtractRow(0, 1, 5.0);
        assertEquals(0.0, tableau.getEntry(0, 1), DEFAULT_EPSILON);
    }

    @Test(timeout = 4000)
    public void testGetSolutionWithDegenerateDuplicateBasicRows() {
        // Two decision variables having identical columns; the second should be set to 0
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 7.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        RealPointValuePair solution = tableau.getSolution();
        double[] point = solution.getPoint();
        assertEquals(2, point.length);
        // The first variable takes the basic value, the duplicate takes 0
        assertEquals(7.0, point[0], DEFAULT_EPSILON);
        assertEquals(0.0, point[1], DEFAULT_EPSILON);
        assertEquals(7.0, solution.getValue(), DEFAULT_EPSILON);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstraintNormalizationWithNegativeRhs() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, -2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2.0, -3.0 }, Relationship.LEQ, -12.0));
        constraints.add(new LinearConstraint(new double[] { -1.0, 4.0 }, Relationship.GEQ, -5.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.EQ, -8.0));
        constraints.add(new LinearConstraint(new double[] { 3.0, 2.0 }, Relationship.LEQ, 0.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        List<LinearConstraint> normalized = tableau.getNormalizedConstraints();

        assertEquals(4, normalized.size());

        // 2x - 3y <= -12  -->  -2x + 3y >= 12
        LinearConstraint c0 = normalized.get(0);
        assertEquals(-2.0, c0.getCoefficients().getEntry(0), DEFAULT_EPSILON);
        assertEquals(3.0, c0.getCoefficients().getEntry(1), DEFAULT_EPSILON);
        assertEquals(Relationship.GEQ, c0.getRelationship());
        assertEquals(12.0, c0.getValue(), DEFAULT_EPSILON);

        // -x + 4y >= -5  -->  x - 4y <= 5
        LinearConstraint c1 = normalized.get(1);
        assertEquals(1.0, c1.getCoefficients().getEntry(0), DEFAULT_EPSILON);
        assertEquals(-4.0, c1.getCoefficients().getEntry(1), DEFAULT_EPSILON);
        assertEquals(Relationship.LEQ, c1.getRelationship());
        assertEquals(5.0, c1.getValue(), DEFAULT_EPSILON);

        // x + y == -8  -->  -x - y == 8
        LinearConstraint c2 = normalized.get(2);
        assertEquals(-1.0, c2.getCoefficients().getEntry(0), DEFAULT_EPSILON);
        assertEquals(-1.0, c2.getCoefficients().getEntry(1), DEFAULT_EPSILON);
        assertEquals(Relationship.EQ, c2.getRelationship());
        assertEquals(8.0, c2.getValue(), DEFAULT_EPSILON);

        // Zero RHS should remain untouched
        LinearConstraint c3 = normalized.get(3);
        assertEquals(Relationship.LEQ, c3.getRelationship());
        assertEquals(0.0, c3.getValue(), DEFAULT_EPSILON);
    }

    @Test(timeout = 4000)
    public void testInvertedCoefficientSumCalculation() {
        RealVector vector = new ArrayRealVector(new double[] { 2.5, -4.0, 1.5, 0.0 });
        double invertedSum = SimplexTableau.getInvertedCoeffiecientSum(vector);
        // sum = 2.5 - 4.0 + 1.5 = 0.0 -> inverted = 0.0
        assertEquals(0.0, invertedSum, DEFAULT_EPSILON);

        RealVector vector2 = new ArrayRealVector(new double[] { 1.0, 2.0, 3.0 });
        // sum = 6.0 -> inverted = -6.0
        assertEquals(-6.0, SimplexTableau.getInvertedCoeffiecientSum(vector2), DEFAULT_EPSILON);
    }

    @Test(timeout = 4000)
    public void testTableauWithZeroConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 4.0, 5.0 }, 1.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        assertEquals(1, tableau.getHeight());
        // 1 (obj) + 2 (decision) + 0 (slack) + 0 (art) + 1 (RHS) = 4
        assertEquals(4, tableau.getWidth());
        assertEquals(0, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
        assertNotNull(tableau.getData());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Math-83)
    // =========================================================================

    /**
     * TARGET DEFECT: Math-83
     * Description: When variables are unrestricted (restrictToNonNegative = false),
     * an extra decision variable x- is created. In the defective implementation,
     * getBasicRow() checks `!MathUtils.equals(entry, 0.0)` instead of checking
     * that the entry equals 1.0 and that row 0 is 0.0. Consequently, column x-
     * (having entry -1 in the constraint row) is misidentified as basic, leading
     * to mostNegative = RHS and resulting in x1 = RHS - mostNegative = 0.0 instead of 10.0!
     */
    @Test(timeout = 4000)
    public void testDefectMath83SingleVariableAndConstraintDirectTableau() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 10.0));

        // Unrestricted variable: restrictToNonNegative = false
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, DEFAULT_EPSILON);

        // Initial tableau state:
        // Row 0 (Z):  [ 1.0, -3.0,  3.0,  0.0,   0.0 ]
        // Row 1 (c1): [ 0.0,  1.0, -1.0,  1.0,  10.0 ]
        // Pivot operation on pivot element (row 1, col 1):
        tableau.subtractRow(0, 1, -3.0);
        // Post-pivot state:
        // Row 0 (Z):  [ 1.0,  0.0,  0.0,  3.0,  30.0 ]
        // Row 1 (c1): [ 0.0,  1.0, -1.0,  1.0,  10.0 ]

        RealPointValuePair solution = tableau.getSolution();
        assertNotNull("Solution must not be null", solution);
        assertEquals("Decision variable x1 must be 10.0 (reveals Math-83)",
                10.0, solution.getPoint()[0], DEFAULT_EPSILON);
        assertEquals("Objective value must be 30.0",
                30.0, solution.getValue(), DEFAULT_EPSILON);
    }

    /**
     * Targets Math-83 via the complete SimplexSolver execution path.
     */
    @Test(timeout = 4000)
    public void testDefectMath83SingleVariableAndConstraintViaSolver() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 10.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);

        assertEquals("Expected x1 = 10.0 but defective version returns 0.0",
                10.0, solution.getPoint()[0], DEFAULT_EPSILON);
        assertEquals("Expected objective value 30.0",
                30.0, solution.getValue(), DEFAULT_EPSILON);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorWithNullObjectiveFunction() {
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        new SimplexTableau(null, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorWithNullConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        new SimplexTableau(f, null, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testGetEntryOutOfBoundsThrowsException() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        tableau.getEntry(100, 100);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 3.0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 3.0);
        LinearObjectiveFunction fDiff = new LinearObjectiveFunction(new double[] { 9.0, 2.0 }, 3.0);

        List<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 5.0));

        List<LinearConstraint> c2 = new ArrayList<LinearConstraint>();
        c2.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 5.0));

        List<LinearConstraint> cDiff = new ArrayList<LinearConstraint>();
        cDiff.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau t1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        SimplexTableau t1Clone = new SimplexTableau(f2, c2, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        // Reflexivity
        assertEquals(t1, t1);

        // Symmetry & equality
        assertEquals(t1, t1Clone);
        assertEquals(t1Clone, t1);
        assertEquals(t1.hashCode(), t1Clone.hashCode());

        // Null check & type compatibility
        assertFalse(t1.equals(null));
        assertFalse(t1.equals("Some String Object"));

        // Differences across fields
        SimplexTableau tDiffRestrict = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, DEFAULT_EPSILON);
        assertFalse(t1.equals(tDiffRestrict));

        SimplexTableau tDiffEps = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1.0e-3);
        assertFalse(t1.equals(tDiffEps));

        SimplexTableau tDiffF = new SimplexTableau(fDiff, c1, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        assertFalse(t1.equals(tDiffF));

        SimplexTableau tDiffC = new SimplexTableau(f1, cDiff, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        assertFalse(t1.equals(tDiffC));

        // Modified tableau matrix entry causes inequality
        SimplexTableau tDiffMatrix = new SimplexTableau(f2, c2, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        tDiffMatrix.setEntry(0, 0, 999.0);
        assertFalse(t1.equals(tDiffMatrix));
    }

    @Test(timeout = 4000)
    public void testSerializationAndDeserializationIntegrity() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, 2.0 }, 5.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, -1.0 }, Relationship.GEQ, 2.0));

        SimplexTableau original = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        SimplexTableau deserialized = (SimplexTableau) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertEquals(original, deserialized);
        assertEquals(original.getWidth(), deserialized.getWidth());
        assertEquals(original.getHeight(), deserialized.getHeight());
        assertEquals(original.getEntry(0, 0), deserialized.getEntry(0, 0), DEFAULT_EPSILON);
        assertEquals(original.getRhsOffset(), deserialized.getRhsOffset());
    }
}