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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.util.Precision;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.math.optimization.linear.SimplexTableau
 *
 * Core Decision Branches & Boundary Conditions Targeted:
 * 1. normalizeConstraints / normalize:
 *    - Branch: constraint.getValue() < 0 (negates coeffs, inverts relationship LEQ <-> GEQ, negates value)
 *    - Branch: constraint.getValue() >= 0 (identity copy)
 * 2. getNumObjectiveFunctions:
 *    - Branch: numArtificialVariables > 0 (returns 2 for Phase 1)
 *    - Branch: numArtificialVariables == 0 (returns 1 for Phase 2)
 * 3. initializeColumnLabels & createTableau:
 *    - Branch: getNumObjectiveFunctions() == 2 (adds "W", sets matrix(0,0) = -1)
 *    - Branch: maximize == true vs maximize == false (signs of z-row, constants)
 *    - Branch: restrictToNonNegative == false (adds "x-", computes inverted coeff sums for zIndex and constraints)
 *    - Branch: Relationship types: LEQ (+1 slack), GEQ (-1 excess, +1 artificial, subtract row), EQ (+1 artificial, subtract row)
 * 4. getBasicRow:
 *    - Branch: Precision.equals(entry, 1d, maxUlps) && row == null (sets row)
 *    - Branch: !Precision.equals(entry, 0d, maxUlps) (returns null on non-zero, non-one entries or multiple ones)
 * 5. dropPhase1Objective:
 *    - Branch: getNumObjectiveFunctions() == 1 (early return guard)
 *    - Branch: Precision.compareTo(entry, 0d, maxUlps) > 0 (drops positive-cost non-artificial columns)
 *    - Branch: getBasicRow(col) == null (drops non-basic artificial columns vs retains basic artificial)
 * 6. isOptimal:
 *    - Branch: Precision.compareTo(entry, 0d, epsilon) < 0 (returns false)
 *    - Branch: all entries >= 0 (returns true)
 * 7. getSolution:
 *    - Branch: negativeVarColumn > 0 && negativeVarBasicRow != null (mostNegative extraction)
 *    - Branch: basicRows.contains(basicRow) (duplicate basic row resolution / unconstrained variable handling)
 *    - Branch: basicRow == null vs basicRow != null
 * 8. Known Ground Truth Defect MATH-713:
 *    - Unconstrained or non-negative variable receiving negative value because basic row resolves to the objective function row
 * 9. Object Contracts:
 *    - equals: this == other, !instanceof, variations on all fields (f, constraints, restrictToNonNegative, epsilon, maxUlps, tableau)
 *    - hashCode: consistency with equals
 *    - Serialization: writeObject / readObject round-trip with matrix deserialization
 */
public class SimplexTableauGeminiTest {

    private static final double DEFAULT_EPSILON = 1e-6;
    private static final int DEFAULT_ULPS = 10;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTableauDimensionsAndOffsetsStandardLEQ() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{2, 3}, 10);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 1}, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[]{1, 0}, Relationship.LEQ, 2));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);

        assertEquals(1, tableau.getNumObjectiveFunctions());
        assertEquals(2, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
        assertEquals(2, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());

        // Width = numDecision(2) + numSlack(2) + numArtificial(0) + numObj(1) + RHS(1) = 6
        assertEquals(6, tableau.getWidth());
        // Height = constraints(2) + numObj(1) = 3
        assertEquals(3, tableau.getHeight());

        assertEquals(1 + 2, tableau.getSlackVariableOffset());
        assertEquals(1 + 2 + 2, tableau.getArtificialVariableOffset());
        assertEquals(5, tableau.getRhsOffset());

        // Row 0 is Z: -2, -3, 0, 0, 10
        assertEquals(1.0, tableau.getEntry(0, 0), 1e-9); // Z column entry for maximize is 1
        assertEquals(-2.0, tableau.getEntry(0, 1), 1e-9);
        assertEquals(-3.0, tableau.getEntry(0, 2), 1e-9);
        assertEquals(10.0, tableau.getEntry(0, 5), 1e-9);

        double[][] data = tableau.getData();
        assertEquals(3, data.length);
        assertEquals(6, data[0].length);
    }

    @Test(timeout = 4000)
    public void testTableauWithGEQAndEQArtificialVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 2}, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 1}, Relationship.GEQ, 2));
        constraints.add(new LinearConstraint(new double[]{2, 1}, Relationship.EQ, 3));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, DEFAULT_EPSILON, 15);

        // GEQ has 1 slack + 1 artificial, EQ has 1 artificial -> 2 artificials => 2 objective functions
        assertEquals(2, tableau.getNumObjectiveFunctions());
        assertEquals(2, tableau.getNumDecisionVariables());
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(2, tableau.getNumArtificialVariables());

        // Width = 2(dec) + 1(slack) + 2(art) + 2(obj) + 1(rhs) = 8
        assertEquals(8, tableau.getWidth());
        // Height = 2 constraints + 2 obj = 4
        assertEquals(4, tableau.getHeight());

        assertEquals(-1.0, tableau.getEntry(0, 0), 1e-9); // W entry is -1
        assertEquals(-1.0, tableau.getEntry(1, 1), 1e-9); // Z entry for minimize is -1
    }

    @Test(timeout = 4000)
    public void testDivideRowAndSubtractRow() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 1}, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{2, 4}, Relationship.LEQ, 8));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        int constraintRow = tableau.getNumObjectiveFunctions(); // row 1

        tableau.divideRow(constraintRow, 2.0);
        assertEquals(1.0, tableau.getEntry(constraintRow, 1), 1e-9); // x0
        assertEquals(2.0, tableau.getEntry(constraintRow, 2), 1e-9); // x1
        assertEquals(0.5, tableau.getEntry(constraintRow, 3), 1e-9); // slack s0
        assertEquals(4.0, tableau.getEntry(constraintRow, 4), 1e-9); // RHS

        // Set entry and subtractRow test
        tableau.setEntry(0, 1, 3.0);
        tableau.subtractRow(0, constraintRow, 3.0);
        assertEquals(0.0, tableau.getEntry(0, 1), 1e-9);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstraintNormalizationWithNegativeRHS() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 2}, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // -x0 + x1 <= -5  normalized to  x0 - x1 >= 5
        constraints.add(new LinearConstraint(new double[]{-1, 1}, Relationship.LEQ, -5));
        // x0 - x1 >= -3  normalized to  -x0 + x1 <= 3
        constraints.add(new LinearConstraint(new double[]{1, -1}, Relationship.GEQ, -3));
        // x0 + x1 == -2  normalized to  -x0 - x1 == 2
        constraints.add(new LinearConstraint(new double[]{1, 1}, Relationship.EQ, -2));
        // positive value unchanged
        constraints.add(new LinearConstraint(new double[]{2, 2}, Relationship.LEQ, 10));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        List<LinearConstraint> normalized = tableau.normalizeConstraints(constraints);

        assertEquals(4, normalized.size());
        assertEquals(Relationship.GEQ, normalized.get(0).getRelationship());
        assertEquals(5.0, normalized.get(0).getValue(), 1e-9);
        assertEquals(1.0, normalized.get(0).getCoefficients().getEntry(0), 1e-9);
        assertEquals(-1.0, normalized.get(0).getCoefficients().getEntry(1), 1e-9);

        assertEquals(Relationship.LEQ, normalized.get(1).getRelationship());
        assertEquals(3.0, normalized.get(1).getValue(), 1e-9);

        assertEquals(Relationship.EQ, normalized.get(2).getRelationship());
        assertEquals(2.0, normalized.get(2).getValue(), 1e-9);

        assertEquals(Relationship.LEQ, normalized.get(3).getRelationship());
        assertEquals(10.0, normalized.get(3).getValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testNegativeVariablesAllowedAddsNegativeVarColumn() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{3, -2}, 5);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, -1}, Relationship.LEQ, 10));

        // restrictToNonNegative = false => extra decision variable x- added
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, DEFAULT_EPSILON);

        assertEquals(3, tableau.getNumDecisionVariables()); // 2 original + 1 for "x-"
        assertEquals(2, tableau.getOriginalNumDecisionVariables());

        // Inverted coeff sum for objective: -( -3 + 2 ) = 1, wait: objectiveCoefficients = [-3, 2], sum = -1, inverted sum = 1
        int xMinusCol = tableau.getSlackVariableOffset() - 1;
        assertEquals(1.0, tableau.getEntry(0, xMinusCol), 1e-9);

        // Constraint inverted coeff sum: -(1 + (-1)) = 0
        assertEquals(0.0, tableau.getEntry(1, xMinusCol), 1e-9);
    }

    @Test(timeout = 4000)
    public void testInvertedCoefficientSumStaticMethod() {
        assertEquals(0.0, SimplexTableau.getInvertedCoefficientSum(new ArrayRealVector(new double[]{})), 1e-9);
        assertEquals(-6.0, SimplexTableau.getInvertedCoefficientSum(new ArrayRealVector(new double[]{1.0, 2.0, 3.0})), 1e-9);
        assertEquals(6.0, SimplexTableau.getInvertedCoefficientSum(new ArrayRealVector(new double[]{-1.0, -2.0, -3.0})), 1e-9);
        assertEquals(0.0, SimplexTableau.getInvertedCoefficientSum(new ArrayRealVector(new double[]{-5.0, 5.0})), 1e-9);
    }

    @Test(timeout = 4000)
    public void testGetBasicRowVariations() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 1}, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 0}, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[]{0, 1}, Relationship.LEQ, 1));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        // Slack columns s0 and s1 should be basic in constraint rows
        int s0Col = tableau.getSlackVariableOffset();
        int s1Col = tableau.getSlackVariableOffset() + 1;

        assertEquals(Integer.valueOf(1), tableau.getBasicRow(s0Col));
        assertEquals(Integer.valueOf(2), tableau.getBasicRow(s1Col));

        // Column with two 1.0 entries should NOT be basic
        tableau.setEntry(0, s0Col, 1.0);
        assertNull(tableau.getBasicRow(s0Col));

        // Column with non-zero non-one entry should NOT be basic
        tableau.setEntry(0, s0Col, 0.5);
        assertNull(tableau.getBasicRow(s0Col));

        // Column with all zeros should NOT be basic
        tableau.setEntry(0, s0Col, 0.0);
        tableau.setEntry(1, s0Col, 0.0);
        assertNull(tableau.getBasicRow(s0Col));
    }

    @Test(timeout = 4000)
    public void testIsOptimalDecision() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{2, 3}, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 1}, Relationship.LEQ, 5));

        // Maximize: objective row has -2, -3 in x0, x1 => Not optimal
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        assertFalse(tableau.isOptimal());

        // Manually overwrite objective row entries to non-negative
        tableau.setEntry(0, 1, 0.0);
        tableau.setEntry(0, 2, 0.0);
        assertTrue(tableau.isOptimal());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-713)
    // =========================================================================

    /**
     * Targets Defects4J MATH-713: SimplexSolverTest::testMath713NegativeVariable
     * When solving a linear program with restrictToNonNegative = true, decision
     * variables must never take negative values. In the defective implementation,
     * if the basic row for a decision variable resolves to the objective function row,
     * getSolution() improperly takes the objective function RHS, resulting in negative values.
     */
    @Test(timeout = 4000)
    public void testMath713NegativeVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{-1, 1}, 1);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 1}, Relationship.EQ, 1));

        double epsilon = 1e-6;
        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        assertTrue("x0 must be non-negative: " + solution.getPoint()[0],
                Precision.compareTo(solution.getPoint()[0], 0.0d, epsilon) >= 0);
        assertTrue("x1 must be non-negative: " + solution.getPoint()[1],
                Precision.compareTo(solution.getPoint()[1], 0.0d, epsilon) >= 0);
    }

    @Test(timeout = 4000)
    public void testGetSolutionWithNegativeVariablesAllowedAndNullNegativeVarBasicRow() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{2, -1}, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 0}, Relationship.LEQ, 4));

        // restrictToNonNegative = false
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, DEFAULT_EPSILON);
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
        assertEquals(2, solution.getPoint().length);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testDropPhase1ObjectiveEarlyReturnWhenSingleObjective() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 1}, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 1}, Relationship.LEQ, 2));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        assertEquals(1, tableau.getNumObjectiveFunctions());
        int originalHeight = tableau.getHeight();
        int originalWidth = tableau.getWidth();

        // Dropping phase 1 when getNumObjectiveFunctions == 1 should simply return without mutation
        tableau.dropPhase1Objective();
        assertEquals(originalHeight, tableau.getHeight());
        assertEquals(originalWidth, tableau.getWidth());
    }

    @Test(timeout = 4000)
    public void testDropPhase1ObjectiveDropsColumnsCorrectly() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 1}, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 1}, Relationship.EQ, 2));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        assertEquals(2, tableau.getNumObjectiveFunctions());
        int originalHeight = tableau.getHeight();

        // Mark a decision variable column as positive cost in row 0 to test that branch
        tableau.setEntry(0, 2, 5.0);

        tableau.dropPhase1Objective();
        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(1, tableau.getNumObjectiveFunctions());
        assertEquals(originalHeight - 1, tableau.getHeight());
    }

    @Test(timeout = 4000)
    public void testEmptyConstraintsTableau() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{3, 4}, 7);
        List<LinearConstraint> constraints = Collections.emptyList();

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, DEFAULT_EPSILON);
        assertEquals(1, tableau.getNumObjectiveFunctions());
        assertEquals(1, tableau.getHeight()); // Only Z row
        assertEquals(4, tableau.getWidth());  // Z, x0, x1, RHS
        assertEquals(7.0, tableau.getEntry(0, 3), 1e-9);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[]{1, 2}, 0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[]{1, 3}, 0);
        List<LinearConstraint> c1 = Arrays.asList(new LinearConstraint(new double[]{1, 0}, Relationship.LEQ, 1));
        List<LinearConstraint> c2 = Arrays.asList(new LinearConstraint(new double[]{0, 1}, Relationship.LEQ, 1));

        SimplexTableau t1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, DEFAULT_EPSILON, 10);
        SimplexTableau t1Clone = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, DEFAULT_EPSILON, 10);
        SimplexTableau tDiffF = new SimplexTableau(f2, c1, GoalType.MAXIMIZE, true, DEFAULT_EPSILON, 10);
        SimplexTableau tDiffC = new SimplexTableau(f1, c2, GoalType.MAXIMIZE, true, DEFAULT_EPSILON, 10);
        SimplexTableau tDiffNonNeg = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, DEFAULT_EPSILON, 10);
        SimplexTableau tDiffEps = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1e-4, 10);
        SimplexTableau tDiffUlps = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, DEFAULT_EPSILON, 20);

        // Reflexivity
        assertEquals(t1, t1);
        // Symmetry & Equality
        assertEquals(t1, t1Clone);
        assertEquals(t1Clone, t1);
        assertEquals(t1.hashCode(), t1Clone.hashCode());

        // Incompatible types and null
        assertFalse(t1.equals(null));
        assertFalse(t1.equals("Some String"));

        // Differences
        assertNotEquals(t1, tDiffF);
        assertNotEquals(t1, tDiffC);
        assertNotEquals(t1, tDiffNonNeg);
        assertNotEquals(t1, tDiffEps);
        assertNotEquals(t1, tDiffUlps);

        // Modified tableau matrix data
        SimplexTableau tModifiedMatrix = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, DEFAULT_EPSILON, 10);
        tModifiedMatrix.setEntry(0, 0, 999.0);
        assertNotEquals(t1, tModifiedMatrix);
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{2, 5}, 3);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 2}, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[]{3, 1}, Relationship.GEQ, 2));

        SimplexTableau original = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, 1e-7, 12);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SimplexTableau deserialized = (SimplexTableau) ois.readObject();

        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
        assertEquals(original.getWidth(), deserialized.getWidth());
        assertEquals(original.getHeight(), deserialized.getHeight());
        for (int r = 0; r < original.getHeight(); r++) {
            for (int c = 0; c < original.getWidth(); c++) {
                assertEquals(original.getEntry(r, c), deserialized.getEntry(r, c), 1e-12);
            }
        }
    }
}