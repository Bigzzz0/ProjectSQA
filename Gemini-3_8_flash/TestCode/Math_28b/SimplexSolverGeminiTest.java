package org.apache.commons.math3.optimization.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;

/*
 * [Branch & Defect Analysis Matrix]
 * Class under test: SimplexSolver
 *
 * Decision / Branch Points Analyzed:
 * 1. SimplexSolver() vs SimplexSolver(double, int):
 *    - Default vs custom parameters (epsilon, maxUlps).
 * 2. getPivotColumn(SimplexTableau):
 *    - Loop over columns: i from getNumObjectiveFunctions to width - 1.
 *    - Condition: entry < minValue.
 *    - Returns index of most negative value, or null if all >= 0.
 * 3. getPivotRow(SimplexTableau, int col):
 *    - Condition: Precision.compareTo(entry, 0d, maxUlps) > 0.
 *    - cmp == 0 (tie for minimum ratio): add to minRatioPositions.
 *    - cmp < 0 (strictly smaller ratio): reset minRatioPositions and add.
 *    - minRatioPositions.size() == 0 -> returns null (unbounded).
 *    - minRatioPositions.size() == 1 -> returns single candidate row.
 *    - minRatioPositions.size() > 1 -> degeneracy tie-breaker:
 *      * Artificial variable check: Precision.equals(entry, 1d, maxUlps) && row.equals(tableau.getBasicRow(column))
 *      * Bland's rule: find row whose basic variable column has minimum index.
 * 4. doIteration(SimplexTableau):
 *    - Unbounded check: pivotRow == null -> throws UnboundedSolutionException.
 *    - Row elimination: subtractRow for all rows i != pivotRow.
 * 5. solvePhase1(SimplexTableau):
 *    - tableau.getNumArtificialVariables() == 0 -> early return.
 *    - while (!tableau.isOptimal()) -> doIteration().
 *    - Infeasible check: Precision.equals(tableau.getEntry(0, rhsOffset), 0d, epsilon) is false -> throws NoFeasibleSolutionException.
 * 6. doOptimize():
 *    - Construct tableau, solvePhase1, dropPhase1Objective, phase 2 iterations, return solution.
 *
 * Known Defect (MATH-828 / Defects4J Math-28):
 * - Bland's rule tie-breaker can enter an infinite cycle on degenerate problems unless
 *   a fallback/heuristic is used after half of max iterations, resulting in MaxCountExceededException.
 */
public class SimplexSolverGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardLinearMinimization() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.LEQ, 6.0));
        constraints.add(new LinearConstraint(new double[] { 3.0, 2.0 }, Relationship.LEQ, 12.0));

        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        assertNotNull(solution);
        assertEquals(4.0, solution.getPoint()[0], 1e-6);
        assertEquals(0.0, solution.getPoint()[1], 1e-6);
        assertEquals(-8.0, solution.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testStandardLinearMaximization() {
        SimplexSolver solver = new SimplexSolver(1e-7, 10);
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, 5.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 2.0 }, Relationship.LEQ, 12.0));
        constraints.add(new LinearConstraint(new double[] { 3.0, 2.0 }, Relationship.LEQ, 18.0));

        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertNotNull(solution);
        assertEquals(2.0, solution.getPoint()[0], 1e-6);
        assertEquals(6.0, solution.getPoint()[1], 1e-6);
        assertEquals(36.0, solution.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testOptimalSolutionWithoutArtificialVariables() {
        // All LEQ with positive RHS: no Phase 1 artificial variables needed
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2.0, 1.0 }, Relationship.LEQ, 8.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.LEQ, 8.0));

        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertNotNull(solution);
        assertEquals(8.0 / 3.0, solution.getPoint()[0], 1e-6);
        assertEquals(8.0 / 3.0, solution.getPoint()[1], 1e-6);
        assertEquals(16.0 / 3.0, solution.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testPhase1WithArtificialVariablesFeasible() {
        // GEQ and EQUAL constraints trigger artificial variable addition
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, 3.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.GEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.EQ, 6.0));

        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        assertNotNull(solution);
        assertEquals(2.0, solution.getPoint()[0], 1e-6);
        assertEquals(2.0, solution.getPoint()[1], 1e-6);
        assertEquals(10.0, solution.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testAlreadyOptimalInitially() {
        // Minimizing 2x + 3y with x >= 0, y >= 0 -> initial solution (0, 0) is already optimal
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, 3.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 10.0));

        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        assertNotNull(solution);
        assertEquals(0.0, solution.getPoint()[0], 1e-6);
        assertEquals(0.0, solution.getPoint()[1], 1e-6);
        assertEquals(0.0, solution.getValue(), 1e-6);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Degeneracy Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testNegativeVariablesAllowed() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, -1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 5.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, -1.0 }, Relationship.GEQ, -2.0));

        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);
        assertNotNull(solution);
        assertEquals(-2.0, solution.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testDegeneracyWithTieInMRT() {
        // Two constraints produce identical ratio in Minimum Ratio Test
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2.0, 1.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 2.0, 1.0 }, Relationship.LEQ, 4.0));

        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertNotNull(solution);
        assertEquals(4.0, solution.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testDegeneracyForcingArtificialVariableOutOfBasis() {
        // MRT tie involving an artificial variable that can be forced out
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.EQ, 2.0));
        constraints.add(new LinearConstraint(new double[] { 2.0, 1.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.GEQ, 0.0));

        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        assertNotNull(solution);
        assertEquals(2.0, solution.getPoint()[0] + solution.getPoint()[1], 1e-6);
    }

    @Test(timeout = 4000)
    public void testEmptyConstraintsList() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 5.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();

        // With non-negative variables and minimize, optimal is at (0, 0)
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        assertNotNull(solution);
        assertEquals(0.0, solution.getPoint()[0], 1e-6);
        assertEquals(0.0, solution.getPoint()[1], 1e-6);
        assertEquals(5.0, solution.getValue(), 1e-6);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-828 Cycling Bug)
    // =========================================================================

    /**
     * Targets Defects4J known failure MATH-828.
     * Degenerate cycling test where standard Bland's rule without the heuristic
     * leads to an infinite cycle and triggers MaxCountExceededException.
     */
    @Test(timeout = 4000)
    public void testMath828Cycle() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
            new double[] {
                1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
            }, 0.0);

        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {
            1.0, 16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0, 2.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
        }, Relationship.LEQ, 0.0));

        constraints.add(new LinearConstraint(new double[] {
            0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 16.0,
            14.0, 12.0, 10.0, 8.0, 6.0, 4.0, 2.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
        }, Relationship.LEQ, 0.0));

        constraints.add(new LinearConstraint(new double[] {
            0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 16.0, 14.0, 12.0,
            10.0, 8.0, 6.0, 4.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
        }, Relationship.LEQ, 0.0));

        constraints.add(new LinearConstraint(new double[] {
            0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 16.0, 14.0, 12.0, 10.0, 8.0, 6.0
        }, Relationship.LEQ, 0.0));

        constraints.add(new LinearConstraint(new double[] {
            0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0
        }, Relationship.LEQ, 0.0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        assertNotNull(solution);
        assertEquals(0.0, solution.getValue(), 1e-6);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnboundedSolutionException.class, timeout = 4000)
    public void testUnboundedSolution() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // x - y <= 1 with non-negative constraints allows y -> infinity while increasing objective
        constraints.add(new LinearConstraint(new double[] { 1.0, -1.0 }, Relationship.LEQ, 1.0));

        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test(expected = NoFeasibleSolutionException.class, timeout = 4000)
    public void testInfeasibleProblem() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // Contradictory constraints: x + y <= 1 AND x + y >= 2
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.GEQ, 2.0));

        solver.optimize(f, constraints, GoalType.MINIMIZE, true);
    }

    @Test(expected = MaxCountExceededException.class, timeout = 4000)
    public void testIterationLimitExceeded() {
        SimplexSolver solver = new SimplexSolver();
        solver.setMaxIterations(0); // Immediately exceeds max iterations
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 5.0));

        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    // =========================================================================
    // Partition E: Protected Lifecycle Methods & Precision Boundaries
    // =========================================================================

    @Test(timeout = 4000)
    public void testProtectedDoIterationDirectly() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -1.0, -1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 1.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1e-6, 10);
        assertFalse(tableau.isOptimal());
        solver.doIteration(tableau);
        assertEquals(1, solver.getIterations());
    }

    @Test(timeout = 4000)
    public void testProtectedSolvePhase1Directly() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.GEQ, 3.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1e-6, 10);
        assertTrue(tableau.getNumArtificialVariables() > 0);
        solver.solvePhase1(tableau);
        // Phase 1 objective value W should be 0 (feasible)
        assertEquals(0.0, tableau.getEntry(0, tableau.getRhsOffset()), 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolvePhase1WithZeroArtificialVariables() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6, 10);
        assertEquals(0, tableau.getNumArtificialVariables());
        // Should immediately return without running any iteration
        solver.solvePhase1(tableau);
        assertEquals(0, solver.getIterations());
    }
}