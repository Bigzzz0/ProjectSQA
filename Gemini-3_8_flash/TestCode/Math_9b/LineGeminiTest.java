package org.apache.commons.math3.geometry.euclidean.threed;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: org.apache.commons.math3.geometry.euclidean.threed.Line
 *
 * Target Defects:
 * - MATH-839 / Defects4J Math-9: Line.revert() loses numeric precision by reconstructing the
 *   reverted line from (zero, zero.subtract(direction)), which undergoes floating-point
 *   cancellation and re-normalization error. Expected behavior: revert() must produce a line
 *   whose direction is bit-for-bit or strictly within numerical epsilon identical to
 *   direction.negate().
 *
 * Branch & Condition Coverage Map:
 * 1. reset(Vector3D, Vector3D) / Line(Vector3D, Vector3D):
 *    - Branch norm2 == 0.0: Throws MathIllegalArgumentException (ZERO_NORM).
 *    - Branch norm2 > 0.0: Correct normalization of direction and computation of orthogonal zero.
 * 2. copy constructor Line(Line):
 *    - Independent deep copy of direction and zero state.
 * 3. revert():
 *    - Defect zone: direction inversion without numerical drift.
 * 4. isSimilarTo(Line):
 *    - Condition 1: angle < 1.0e-10 && contains(line.zero) [True, True -> true]
 *    - Condition 2: angle > (PI - 1.0e-10) && contains(line.zero) [True, True -> true]
 *    - Condition 3: angle < 1.0e-10 && !contains(line.zero) (parallel shifted) [True, False -> false]
 *    - Condition 4: angle > (PI - 1.0e-10) && !contains(line.zero) (anti-parallel shifted) [True, False -> false]
 *    - Condition 5: angle intermediate (intersecting / skew) [False, Any -> false]
 * 5. contains(Vector3D):
 *    - Branch distance < 1.0e-10 (true) vs >= 1.0e-10 (false).
 * 6. distance(Vector3D):
 *    - Point on line (returns 0.0).
 *    - Arbitrary point in space (returns orthogonal distance).
 * 7. distance(Line):
 *    - Branch normal.getNorm() < Precision.SAFE_MIN (parallel / anti-parallel):
 *      -> returns distance(line.zero).
 *    - Branch normal.getNorm() >= Precision.SAFE_MIN (non-parallel):
 *      -> intersecting (returns 0.0) vs skew lines (returns positive shortest separation).
 * 8. closestPoint(Line):
 *    - Branch (1 - cos*cos) < Precision.EPSILON (parallel lines):
 *      -> returns zero.
 *    - Branch (1 - cos*cos) >= Precision.EPSILON (non-parallel):
 *      -> returns exact closest point on instance.
 * 9. intersection(Line):
 *    - Branch line.contains(closest) (true): returns intersection point.
 *    - Branch !line.contains(closest) (false): returns null (skew or parallel disjoint lines).
 * 10. toSubSpace(Vector<Euclidean3D>) / toSpace(Vector<Euclidean1D>):
 *    - Bidirectional isometric 1D/3D projection consistency.
 * 11. wholeLine():
 *    - Returns SubLine covering entire 1D line space (-inf to +inf).
 */

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.geometry.euclidean.oned.Euclidean1D;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.SubLine;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;

public class LineGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testLineCreationAndGetters() {
        final Vector3D p1 = new Vector3D(1.0, 2.0, 3.0);
        final Vector3D p2 = new Vector3D(1.0, 2.0, 10.0);
        final Line line = new Line(p1, p2);

        final Vector3D dir = line.getDirection();
        assertEquals(0.0, dir.getX(), 1.0e-15);
        assertEquals(0.0, dir.getY(), 1.0e-15);
        assertEquals(1.0, dir.getZ(), 1.0e-15);
        assertEquals(1.0, dir.getNorm(), 1.0e-15);

        final Vector3D origin = line.getOrigin();
        assertEquals(1.0, origin.getX(), 1.0e-15);
        assertEquals(2.0, origin.getY(), 1.0e-15);
        assertEquals(0.0, origin.getZ(), 1.0e-15);
        assertEquals(0.0, origin.dotProduct(dir), 1.0e-15);
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        final Line original = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(3.0, 4.0, 0.0));
        final Line copy = new Line(original);

        assertEquals(original.getDirection().getX(), copy.getDirection().getX(), 1.0e-15);
        assertEquals(original.getDirection().getY(), copy.getDirection().getY(), 1.0e-15);
        assertEquals(original.getDirection().getZ(), copy.getDirection().getZ(), 1.0e-15);

        assertEquals(original.getOrigin().getX(), copy.getOrigin().getX(), 1.0e-15);
        assertEquals(original.getOrigin().getY(), copy.getOrigin().getY(), 1.0e-15);
        assertEquals(original.getOrigin().getZ(), copy.getOrigin().getZ(), 1.0e-15);

        original.reset(new Vector3D(0.0, 0.0, 0.0), new Vector3D(0.0, 0.0, 5.0));
        assertNotEquals(original.getDirection().getZ(), copy.getDirection().getZ(), 1.0e-15);
    }

    @Test(timeout = 4000)
    public void testAbscissaAndPointAt() {
        final Line line = new Line(new Vector3D(0.0, 2.0, 0.0), new Vector3D(10.0, 2.0, 0.0));

        assertEquals(0.0, line.getAbscissa(line.getOrigin()), 1.0e-15);

        final Vector3D p1 = line.pointAt(5.0);
        assertEquals(5.0, p1.getX(), 1.0e-15);
        assertEquals(2.0, p1.getY(), 1.0e-15);
        assertEquals(0.0, p1.getZ(), 1.0e-15);
        assertEquals(5.0, line.getAbscissa(p1), 1.0e-15);

        final Vector3D pNeg = line.pointAt(-3.5);
        assertEquals(-3.5, pNeg.getX(), 1.0e-15);
        assertEquals(2.0, pNeg.getY(), 1.0e-15);
        assertEquals(0.0, pNeg.getZ(), 1.0e-15);
        assertEquals(-3.5, line.getAbscissa(pNeg), 1.0e-15);
    }

    @Test(timeout = 4000)
    public void testToSubSpaceAndToSpace() {
        final Line line = new Line(new Vector3D(1.0, 1.0, 1.0), new Vector3D(2.0, 2.0, 2.0));

        final Vector3D originalPoint = new Vector3D(5.0, 5.0, 5.0);
        final Vector1D subPoint = line.toSubSpace((Vector<Euclidean3D>) originalPoint);
        final Vector3D projectedPoint = line.toSpace((Vector<Euclidean1D>) subPoint);

        assertEquals(originalPoint.getX(), projectedPoint.getX(), 1.0e-14);
        assertEquals(originalPoint.getY(), projectedPoint.getY(), 1.0e-14);
        assertEquals(originalPoint.getZ(), projectedPoint.getZ(), 1.0e-14);

        final Vector3D offPoint = new Vector3D(5.0, 0.0, 0.0);
        final Vector1D subOff = line.toSubSpace((Vector<Euclidean3D>) offPoint);
        final Vector3D projOff = line.toSpace((Vector<Euclidean1D>) subOff);
        assertTrue(line.contains(projOff));
        assertEquals(line.distance(offPoint), offPoint.distance(projOff), 1.0e-14);
    }

    @Test(timeout = 4000)
    public void testContains() {
        final Line line = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 1.0, 1.0));

        assertTrue(line.contains(new Vector3D(0.0, 0.0, 0.0)));
        assertTrue(line.contains(new Vector3D(2.5, 2.5, 2.5)));
        assertTrue(line.contains(new Vector3D(-10.0, -10.0, -10.0)));

        assertFalse(line.contains(new Vector3D(1.0, 1.0, 1.000001)));
        assertFalse(line.contains(new Vector3D(0.0, 1.0, 0.0)));
    }

    @Test(timeout = 4000)
    public void testPointDistance() {
        final Line line = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));

        assertEquals(0.0, line.distance(new Vector3D(5.0, 0.0, 0.0)), 1.0e-15);
        assertEquals(3.0, line.distance(new Vector3D(2.0, 3.0, 0.0)), 1.0e-15);
        assertEquals(5.0, line.distance(new Vector3D(0.0, 3.0, 4.0)), 1.0e-15);
    }

    @Test(timeout = 4000)
    public void testWholeLine() {
        final Line line = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(0.0, 1.0, 0.0));
        final SubLine wholeLine = line.wholeLine();

        assertNotNull(wholeLine);
        assertNotNull(wholeLine.getSegments());
        assertEquals(1, wholeLine.getSegments().size());
        assertTrue(Double.isInfinite(wholeLine.getSegments().get(0).getStart().getX()));
        assertTrue(Double.isInfinite(wholeLine.getSegments().get(0).getEnd().getX()));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testContainsBoundaryThreshold() {
        final Line line = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));

        final Vector3D pointInside = new Vector3D(0.0, 0.5e-10, 0.0);
        assertTrue(line.contains(pointInside));

        final Vector3D pointOutside = new Vector3D(0.0, 2.0e-10, 0.0);
        assertFalse(line.contains(pointOutside));
    }

    @Test(timeout = 4000)
    public void testIsSimilarToBranches() {
        final Line line1 = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));

        // Exactly identical
        assertTrue(line1.isSimilarTo(line1));

        // Same line, different points generating it
        final Line lineIdentical = new Line(new Vector3D(2.0, 0.0, 0.0), new Vector3D(5.0, 0.0, 0.0));
        assertTrue(line1.isSimilarTo(lineIdentical));

        // Opposite direction (angle == PI)
        final Line lineReversed = new Line(new Vector3D(5.0, 0.0, 0.0), new Vector3D(2.0, 0.0, 0.0));
        assertTrue(line1.isSimilarTo(lineReversed));

        // Parallel same direction but shifted (angle < 1.0e-10, but contains line.zero is false)
        final Line lineParallelShifted = new Line(new Vector3D(0.0, 1.0, 0.0), new Vector3D(1.0, 1.0, 0.0));
        assertFalse(line1.isSimilarTo(lineParallelShifted));

        // Parallel opposite direction but shifted (angle > PI - 1.0e-10, but contains line.zero is false)
        final Line lineAntiParallelShifted = new Line(new Vector3D(1.0, 1.0, 0.0), new Vector3D(0.0, 1.0, 0.0));
        assertFalse(line1.isSimilarTo(lineAntiParallelShifted));

        // Intersecting at 90 degrees
        final Line linePerpendicular = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(0.0, 1.0, 0.0));
        assertFalse(line1.isSimilarTo(linePerpendicular));

        // Skew line
        final Line lineSkew = new Line(new Vector3D(0.0, 0.0, 1.0), new Vector3D(0.0, 1.0, 1.0));
        assertFalse(line1.isSimilarTo(lineSkew));
    }

    @Test(timeout = 4000)
    public void testLineDistanceParallelAndAntiParallel() {
        final Line line1 = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));

        // Parallel (same direction)
        final Line line2 = new Line(new Vector3D(0.0, 4.0, 0.0), new Vector3D(1.0, 4.0, 0.0));
        assertEquals(4.0, line1.distance(line2), 1.0e-15);

        // Anti-parallel (opposite direction)
        final Line line3 = new Line(new Vector3D(0.0, 4.0, 0.0), new Vector3D(-1.0, 4.0, 0.0));
        assertEquals(4.0, line1.distance(line3), 1.0e-15);

        // Coincident
        assertEquals(0.0, line1.distance(line1), 1.0e-15);
    }

    @Test(timeout = 4000)
    public void testLineDistanceIntersectingAndSkew() {
        final Line line1 = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));

        // Intersecting at origin
        final Line line2 = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(0.0, 1.0, 0.0));
        assertEquals(0.0, line1.distance(line2), 1.0e-15);

        // Skew lines: line1 is along x-axis, line3 is parallel to y-axis at z = 5
        final Line line3 = new Line(new Vector3D(0.0, 0.0, 5.0), new Vector3D(0.0, 1.0, 5.0));
        assertEquals(5.0, line1.distance(line3), 1.0e-15);
    }

    @Test(timeout = 4000)
    public void testClosestPointParallelAndAntiParallel() {
        final Line line1 = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));

        // Parallel lines: n < Precision.EPSILON -> returns zero
        final Line lineParallel = new Line(new Vector3D(0.0, 2.0, 0.0), new Vector3D(1.0, 2.0, 0.0));
        final Vector3D closestParallel = line1.closestPoint(lineParallel);
        assertEquals(line1.getOrigin().getX(), closestParallel.getX(), 1.0e-15);
        assertEquals(line1.getOrigin().getY(), closestParallel.getY(), 1.0e-15);
        assertEquals(line1.getOrigin().getZ(), closestParallel.getZ(), 1.0e-15);

        // Anti-parallel lines: cos = -1 -> n < Precision.EPSILON -> returns zero
        final Line lineAntiParallel = new Line(new Vector3D(0.0, 2.0, 0.0), new Vector3D(-1.0, 2.0, 0.0));
        final Vector3D closestAntiParallel = line1.closestPoint(lineAntiParallel);
        assertEquals(line1.getOrigin().getX(), closestAntiParallel.getX(), 1.0e-15);
        assertEquals(line1.getOrigin().getY(), closestAntiParallel.getY(), 1.0e-15);
        assertEquals(line1.getOrigin().getZ(), closestAntiParallel.getZ(), 1.0e-15);
    }

    @Test(timeout = 4000)
    public void testClosestPointIntersectingAndSkew() {
        final Line line1 = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));

        // Intersecting at (3, 0, 0)
        final Line line2 = new Line(new Vector3D(3.0, -1.0, 0.0), new Vector3D(3.0, 1.0, 0.0));
        final Vector3D closestIntersect = line1.closestPoint(line2);
        assertEquals(3.0, closestIntersect.getX(), 1.0e-15);
        assertEquals(0.0, closestIntersect.getY(), 1.0e-15);
        assertEquals(0.0, closestIntersect.getZ(), 1.0e-15);

        // Skew lines: line1 is along x-axis, line3 is along (x=4, z=2) parallel to y-axis
        final Line line3 = new Line(new Vector3D(4.0, 0.0, 2.0), new Vector3D(4.0, 5.0, 2.0));
        final Vector3D closestSkew = line1.closestPoint(line3);
        assertEquals(4.0, closestSkew.getX(), 1.0e-15);
        assertEquals(0.0, closestSkew.getY(), 1.0e-15);
        assertEquals(0.0, closestSkew.getZ(), 1.0e-15);
    }

    @Test(timeout = 4000)
    public void testIntersectionBranches() {
        final Line line1 = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));

        // Intersecting
        final Line lineIntersecting = new Line(new Vector3D(2.0, -1.0, 0.0), new Vector3D(2.0, 1.0, 0.0));
        final Vector3D inter = line1.intersection(lineIntersecting);
        assertNotNull(inter);
        assertEquals(2.0, inter.getX(), 1.0e-15);
        assertEquals(0.0, inter.getY(), 1.0e-15);
        assertEquals(0.0, inter.getZ(), 1.0e-15);

        // Parallel disjoint -> null
        final Line lineParallelDisjoint = new Line(new Vector3D(0.0, 1.0, 0.0), new Vector3D(1.0, 1.0, 0.0));
        assertNull(line1.intersection(lineParallelDisjoint));

        // Parallel coincident -> returns closest point (which is zero)
        final Line lineCoincident = new Line(new Vector3D(5.0, 0.0, 0.0), new Vector3D(10.0, 0.0, 0.0));
        final Vector3D interCoincident = line1.intersection(lineCoincident);
        assertNotNull(interCoincident);
        assertTrue(line1.contains(interCoincident));
        assertTrue(lineCoincident.contains(interCoincident));

        // Skew lines -> null
        final Line lineSkew = new Line(new Vector3D(2.0, -1.0, 1.0), new Vector3D(2.0, 1.0, 1.0));
        assertNull(line1.intersection(lineSkew));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (MATH-839 / Defects4J Math-9)
    // =========================================================================

    /**
     * Targets the defect where revert() re-computed the line from (zero, zero - direction),
     * causing numerical roundoff cancellation in direction and zero position.
     * The test asserts that the reverted direction is strictly identical to negate()
     * with delta = 0, directly triggering the regression on the unpatched version.
     */
    @Test(timeout = 4000)
    public void testRevert() {
        final Vector3D p1 = new Vector3D(1.6515135968193863, 0.22207012099716533, -0.9794917532589679);
        final Vector3D p2 = new Vector3D(2.706716380646502, -0.25568228306609154, -1.9964340801662784);
        final Line line = new Line(p1, p2);
        final Line reverted = line.revert();

        assertArrayEquals(line.getDirection().negate().toArray(), reverted.getDirection().toArray(), 0);
    }

    @Test(timeout = 4000)
    public void testRevertPointConsistency() {
        final Vector3D p1 = new Vector3D(1.6515135968193863, 0.22207012099716533, -0.9794917532589679);
        final Vector3D p2 = new Vector3D(2.706716380646502, -0.25568228306609154, -1.9964340801662784);
        final Line line = new Line(p1, p2);
        final Line reverted = line.revert();

        // The geometrical line is invariant under reversal
        assertTrue(line.isSimilarTo(reverted));
        assertTrue(reverted.isSimilarTo(line));
        assertEquals(0.0, line.distance(reverted), 1.0e-15);

        // Origins must coincide
        assertEquals(line.getOrigin().getX(), reverted.getOrigin().getX(), 1.0e-14);
        assertEquals(line.getOrigin().getY(), reverted.getOrigin().getY(), 1.0e-14);
        assertEquals(line.getOrigin().getZ(), reverted.getOrigin().getZ(), 1.0e-14);

        // Abscissa must be opposite
        final double absP1 = line.getAbscissa(p1);
        final double absRevertedP1 = reverted.getAbscissa(p1);
        assertEquals(-absP1, absRevertedP1, 1.0e-14);

        // Inverting twice restores the original direction exactly
        final Line doubleReverted = reverted.revert();
        assertArrayEquals(line.getDirection().toArray(), doubleReverted.getDirection().toArray(), 0);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testConstructorIdenticalPoints() {
        final Vector3D p = new Vector3D(1.0, 2.0, 3.0);
        new Line(p, p);
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testConstructorZeroNormDifference() {
        final Vector3D p1 = new Vector3D(5.0, -3.0, 2.0);
        final Vector3D p2 = new Vector3D(5.0, -3.0, 2.0);
        new Line(p1, p2);
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testResetIdenticalPoints() {
        final Line line = new Line(new Vector3D(1.0, 0.0, 0.0), new Vector3D(0.0, 1.0, 0.0));
        final Vector3D p = new Vector3D(4.0, 4.0, 4.0);
        line.reset(p, p);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testResetNormalReconfiguration() {
        final Line line = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));
        assertEquals(1.0, line.getDirection().getX(), 1.0e-15);
        assertEquals(0.0, line.getDirection().getY(), 1.0e-15);

        // Reconfigure to y-axis
        line.reset(new Vector3D(0.0, 0.0, 0.0), new Vector3D(0.0, 5.0, 0.0));
        assertEquals(0.0, line.getDirection().getX(), 1.0e-15);
        assertEquals(1.0, line.getDirection().getY(), 1.0e-15);
        assertEquals(0.0, line.getDirection().getZ(), 1.0e-15);
        assertEquals(0.0, line.getOrigin().getNorm(), 1.0e-15);
    }

    @Test(timeout = 4000)
    public void testLargeCoordinatesStability() {
        final Vector3D p1 = new Vector3D(1.0e12, 1.0e12, 1.0e12);
        final Vector3D p2 = new Vector3D(1.0e12 + 3.0, 1.0e12 + 4.0, 1.0e12);
        final Line line = new Line(p1, p2);

        assertEquals(0.6, line.getDirection().getX(), 1.0e-14);
        assertEquals(0.8, line.getDirection().getY(), 1.0e-14);
        assertEquals(0.0, line.getDirection().getZ(), 1.0e-14);

        assertTrue(line.contains(p1));
        assertTrue(line.contains(p2));
        assertEquals(0.0, line.distance(p1), 1.0e-10);
    }
}
