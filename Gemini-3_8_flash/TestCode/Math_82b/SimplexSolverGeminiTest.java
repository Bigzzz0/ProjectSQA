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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target: org.apache.commons.math.optimization.linear.SimplexSolver
 *
 * Decision / Branch Matrix:
 * 1. getPivotColumn(tableau):
 *    - Loop condition: i = tableau.getNumObjectiveFunctions() to width - 1
 *    - Comparison: MathUtils.compareTo(entry, minValue, epsilon) < 0 (true -> update minValue & minPos, false -> skip)
 *    - Return: Integer (null if no entry < 0, or column index)
 * 2. getPivotRow(col, tableau):
 *    - Loop condition: i = tableau.getNumObjectiveFunctions() to height
 *    - Ratio condition: MathUtils.compareTo(entry, 0, epsilon) >= 0 (or > 0 in fixed logic)
 *    - Sub-branch: ratio < minRatio (true -> update minRatio & minRatioPos)
 *    - Return: Integer (null if no valid row found -> triggers UnboundedSolutionException)
 * 3. doIteration(tableau):
 *    - Pivot row validation: pivotRow == null -> throw UnboundedSolutionException
 *    - Tableau elimination loop: i != pivotRow -> subtractRow
 * 4. isPhase1Solved(tableau):
 *    - numArtificialVariables == 0 -> early true
 *    - Loop over columns: entry < 0 (compareTo < 0) -> return false, else complete loop -> true
 * 5. isOptimal(tableau):
 *    - numArtificialVariables > 0 -> return false
 *    - Loop over columns: entry < 0 (compareTo < 0) -> return false, else complete loop -> true
 * 6. solvePhase1(tableau):
 *    - numArtificialVariables == 0 -> early return
 *    - While !isPhase1Solved(tableau) -> doIteration(tableau)
 *    - Infeasibility check: tableau.getEntry(0, rhsOffset) != 0 -> throw NoFeasibleSolutionException
 * 7. doOptimize():
 *    - Two-Phase Simplex execution: solvePhase1 -> discardArtificialVariables -> iterate until optimal -> getSolution
 *
 * Ground Truth Defect Targeted:
 * - MATH-288 / Defects4J Math-82: Incorrect pivot row selection when entry == 0 in getPivotRow.
 *   Minimum Ratio Test (MRT) must not divide by 0 or allow non-positive entries to corrupt row selection.
 * =========================================================================
 */
public class SimplexSolverGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardLinearMaximization() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 5 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 0, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 18));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertEquals(36.0, solution.getValue(), 1e-6);
        assertEquals(2.0, solution.getPoint()[0], 1e-6);
        assertEquals(6.0, solution.getPoint()[1], 1e-6);
    }

    @Test(timeout = 4000)
    public void testStandardLinearMinimization() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, 4);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 8));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        assertEquals(-8.0, solution.getValue(), 1e-6);
        assertEquals(6.0, solution.getPoint()[0], 1e-6);
        assertEquals(0.0, solution.getPoint()[1], 1e-6);
    }

    @Test(timeout = 4000)
    public void testTwoPhaseWithEqualityConstraint() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.EQ, 6));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 12));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertEquals(5.0, solution.getValue(), 1e-6);
        assertEquals(3.0, solution.getPoint()[0], 1e-6);
        assertEquals(1.5, solution.getPoint()[1], 1e-6);
    }

    @Test(timeout = 4000)
    public void testTwoPhaseWithGreaterOrEqualConstraint() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 6));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        assertEquals(8.0, solution.getValue(), 1e-6);
        assertEquals(4.0, solution.getPoint()[0], 1e-6);
        assertEquals(0.0, solution.getPoint()[1], 1e-6);
    }

    @Test(timeout = 4000)
    public void testIterationCountTracked() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 1));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertTrue("Iterations should be recorded and greater than 0", solver.getIterations() > 0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNegativeVariablesAllowed() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, -10));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, -5));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);

        assertEquals(-15.0, solution.getValue(), 1e-6);
        assertEquals(-10.0, solution.getPoint()[0], 1e-6);
        assertEquals(-5.0, solution.getPoint()[1], 1e-6);
    }

    @Test(timeout = 4000)
    public void testAlreadyOptimalInitially() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0, 0 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 5));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertEquals(0.0, solution.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testTrivialZeroConstraintBoundary() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 5 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertEquals(0.0, solution.getValue(), 1e-6);
        assertEquals(0.0, solution.getPoint()[0], 1e-6);
    }

    @Test(timeout = 4000)
    public void testDegeneracyWithRedundantConstraints() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 2, 0 }, Relationship.LEQ, 4)); // Redundant

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertEquals(6.0, solution.getValue(), 1e-6);
        assertEquals(2.0, solution.getPoint()[0], 1e-6);
        assertEquals(2.0, solution.getPoint()[1], 1e-6);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-288 / Defects4J Math-82)
    // =========================================================================

    /**
     * Targets defect MATH-288:
     * When evaluating getPivotRow, entries equal to 0 (or close to 0 within epsilon)
     * must not be considered valid candidates for the Minimum Ratio Test.
     * The buggy implementation used `compareTo(entry, 0, epsilon) >= 0` which incorrectly
     * admitted zero pivot elements, corrupting the ratio test and yielding 11.5 instead of 10.0.
     */
    @Test(timeout = 4000)
    public void testMath288() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 7, 3 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 3, 0 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 0, 2 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 4, 3 }, Relationship.LEQ, 12));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertEquals("Optimal objective value must be 10.0", 10.0, solution.getValue(), 1e-6);
        assertEquals(1.0, solution.getPoint()[0], 1e-6);
        assertEquals(1.0, solution.getPoint()[1], 1e-6);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnboundedSolutionException.class, timeout = 4000)
    public void testUnboundedProblemThrowsException() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, -1 }, Relationship.LEQ, 1));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test(expected = NoFeasibleSolutionException.class, timeout = 4000)
    public void testInfeasibleProblemThrowsException() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 5));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test(expected = OptimizationException.class, timeout = 4000)
    public void testMaxIterationsExceeded() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 1));

        SimplexSolver solver = new SimplexSolver();
        solver.setMaxIterations(0);
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Low-Level Methods & Tableau Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCustomEpsilonConstructor() throws OptimizationException {
        double customEpsilon = 1e-4;
        SimplexSolver solver = new SimplexSolver(customEpsilon);
        assertEquals(customEpsilon, solver.epsilon, 1e-12);

        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2 }, 0);
        Collection<LinearConstraint> constraints = Collections.singletonList(
                new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 3)
        );
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertEquals(6.0, solution.getValue(), 1e-4);
    }

    @Test(timeout = 4000)
    public void testDirectIsOptimalCheck() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);
        SimplexSolver solver = new SimplexSolver();

        assertFalse("Initial tableau for maximization should not be optimal", solver.isOptimal(tableau));
    }

    @Test(timeout = 4000)
    public void testDirectSolvePhase1WhenNoArtificialVariables() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);
        SimplexSolver solver = new SimplexSolver();

        assertEquals(0, tableau.getNumArtificialVariables());
        solver.solvePhase1(tableau);
        assertEquals(0, tableau.getNumArtificialVariables());
    }

    @Test(timeout = 4000)
    public void testDirectDoIterationExecution() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);
        SimplexSolver solver = new SimplexSolver();

        assertFalse(solver.isOptimal(tableau));
        solver.doIteration(tableau);
        assertTrue(solver.isOptimal(tableau));
    }
}