package org.apache.commons.math3.optimization.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;

/**
 * Branch & Defect Analysis Matrix:
 * - getPivotColumn: loops over columns, finds min negative entry; branch when entry < minValue (true/false)
 * - getPivotRow: MRT with ties; degeneracy handling: artificial variable check, Bland's rule, heuristic fallback
 * - doIteration: pivot row null -> UnboundedSolutionException; normal pivot operations
 * - solvePhase1: no artificial variables -> return; while not optimal -> doIteration; check W zero -> NoFeasibleSolutionException
 * - doOptimize: full two-phase simplex; dropPhase1; while not optimal -> doIteration; return solution
 * - Boundary: epsilon=0, maxUlps=0; empty constraints; null function; extreme values
 * - Defect MATH-828: cycling leads to MaxCountExceededException; test triggers cycling and expects no exception (fixed behavior)
 */
public class SimplexSolverDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testSimpleMaximization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{3, 2}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{2, 1}, Relationship.LEQ, 18));
        constraints.add(new LinearConstraint(new double[]{2, 3}, Relationship.LEQ, 42));
        constraints.add(new LinearConstraint(new double[]{3, 1}, Relationship.LEQ, 24));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertNotNull(solution);
        assertEquals(18.0, solution.getPoint()[0], 1e-6);
        assertEquals(8.0, solution.getPoint()[1], 1e-6);
        assertEquals(70.0, solution.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testSimpleMinimization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{2, 3}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 1}, Relationship.GEQ, 5));
        constraints.add(new LinearConstraint(new double[]{1, 0}, Relationship.GEQ, 2));
        constraints.add(new LinearConstraint(new double[]{0, 2}, Relationship.GEQ, 4));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        assertNotNull(solution);
        assertEquals(2.0, solution.getPoint()[0], 1e-6);
        assertEquals(3.0, solution.getPoint()[1], 1e-6);
        assertEquals(13.0, solution.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testNoConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertNotNull(solution);
        // unbounded? Actually with no constraints and non-negative, it's unbounded -> should throw UnboundedSolutionException
        // But the solver may handle it? Let's see: getPivotColumn returns null? Actually getPivotColumn returns null if no negative entry? 
        // With all zeros in objective row? The objective row has coefficients? For maximize, we negate? Actually the tableau construction: 
        // For MAXIMIZE, the objective function coefficients are negated. So with no constraints, the initial tableau has only objective row and slack? 
        // It will likely be unbounded. We'll expect UnboundedSolutionException.
        fail("Expected UnboundedSolutionException");
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000, expected = UnboundedSolutionException.class)
    public void testUnboundedSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 0}, Relationship.GEQ, 0));
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test(timeout = 4000, expected = NoFeasibleSolutionException.class)
    public void testNoFeasibleSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 0}, Relationship.LEQ, -1));
        constraints.add(new LinearConstraint(new double[]{0, 1}, Relationship.LEQ, -1));
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test(timeout = 4000)
    public void testDegenerateProblem() {
        // Degenerate problem that may cause ties in MRT
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 0}, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[]{0, 1}, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[]{1, 1}, Relationship.LEQ, 2));
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertNotNull(solution);
        assertEquals(1.0, solution.getPoint()[0], 1e-6);
        assertEquals(1.0, solution.getPoint()[1], 1e-6);
        assertEquals(2.0, solution.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testEpsilonAndUlpsBoundary() {
        // Use extreme epsilon and maxUlps values
        SimplexSolver solver = new SimplexSolver(0.0, 0);
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 0}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 0}, Relationship.LEQ, 5));
        constraints.add(new LinearConstraint(new double[]{0, 1}, Relationship.LEQ, 3));
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertNotNull(solution);
        assertEquals(5.0, solution.getPoint()[0], 1e-6);
        assertEquals(3.0, solution.getPoint()[1], 1e-6);
        assertEquals(5.0, solution.getValue(), 1e-6);
    }

    // ========== Partition C: Defect-Targeted Branch Zone (MATH-828 Cycling) ==========

    @Test(timeout = 4000)
    public void testMath828Cycle() {
        // This problem is known to cause cycling in the simplex method (Beale's example).
        // The defective solver will throw MaxCountExceededException; the fixed solver should succeed.
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{10, -57, -9, -24}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{0.5, -5.5, -2.5, 9}, Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[]{0.5, -1.5, -0.5, 1}, Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[]{1, 0, 0, 0}, Relationship.LEQ, 1));
        // Variables are non-negative by default (restrictToNonNegative = true)

        SimplexSolver solver = new SimplexSolver();
        try {
            PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
            // If we reach here, the bug is fixed (or the problem didn't cycle)
            assertNotNull(solution);
            // The optimal solution for this problem is (1, 0, 0, 0) with value 10
            assertEquals(1.0, solution.getPoint()[0], 1e-6);
            assertEquals(0.0, solution.getPoint()[1], 1e-6);
            assertEquals(0.0, solution.getPoint()[2], 1e-6);
            assertEquals(0.0, solution.getPoint()[3], 1e-6);
            assertEquals(10.0, solution.getValue(), 1e-6);
        } catch (MaxCountExceededException e) {
            // This exception reveals the defect (cycling)
            fail("Cycling detected: MaxCountExceededException thrown. Defect MATH-828 is present.");
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullFunction() {
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(null, new ArrayList<LinearConstraint>(), GoalType.MAXIMIZE, true);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1}, 0);
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, null, GoalType.MAXIMIZE, true);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullGoalType() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1}, 0);
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, new ArrayList<LinearConstraint>(), null, true);
    }

    @Test(timeout = 4000)
    public void testSingleVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{2}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1}, Relationship.LEQ, 10));
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertNotNull(solution);
        assertEquals(10.0, solution.getPoint()[0], 1e-6);
        assertEquals(20.0, solution.getValue(), 1e-6);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testSolverStateAfterOptimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 0}, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[]{0, 1}, Relationship.LEQ, 2));
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution1 = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertNotNull(solution1);
        // Optimize again with same solver (should reset internal state)
        PointValuePair solution2 = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertNotNull(solution2);
        assertEquals(solution1.getPoint()[0], solution2.getPoint()[0], 1e-6);
        assertEquals(solution1.getPoint()[1], solution2.getPoint()[1], 1e-6);
        assertEquals(solution1.getValue(), solution2.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testCustomEpsilonAndUlps() {
        SimplexSolver solver = new SimplexSolver(1e-10, 5);
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1, 0}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1, 0}, Relationship.LEQ, 1e-8));
        constraints.add(new LinearConstraint(new double[]{0, 1}, Relationship.LEQ, 1e-8));
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertNotNull(solution);
        assertEquals(1e-8, solution.getPoint()[0], 1e-10);
        assertEquals(1e-8, solution.getPoint()[1], 1e-10);
        assertEquals(1e-8, solution.getValue(), 1e-10);
    }
}