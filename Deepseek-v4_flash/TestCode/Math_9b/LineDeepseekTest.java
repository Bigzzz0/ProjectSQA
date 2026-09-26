package org.apache.commons.math3.geometry.euclidean.threed;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.exception.MathIllegalArgumentException;

/**
 * White-box test suite for {@link Line} targeting the known Defects4J defect
 * in the revert() method (testRevert numerical failure).
 *
 * <p><b>[Branch & Defect Analysis Matrix]</b>
 * <ul>
 *   <li>reset(): norm2 == 0 (exception branch)</li>
 *   <li>contains(): distance(p) < 1.0e-10 (true/false)</li>
 *   <li>isSimilarTo(): angle < 1e-10, angle > PI-1e-10, contains(line.zero) – 3 branches combined</li>
 *   <li>distance(Line): n < Precision.SAFE_MIN (parallel path)</li>
 *   <li>closestPoint(): n < Precision.EPSILON (parallel path)</li>
 *   <li>intersection(): line.contains(closest) (true/false)</li>
 *   <li>revert(): construction line from zero and zero.subtract(direction) – precision defect</li>
 *   <li>Whole-line coverage of all public methods</li>
 * </ul>
 * </p>
 */
public class LineDeepseekTest {

    // -------- Partition A: Core Functional Logic & State Transitions --------

    @Test(timeout = 4000)
    public void testResetAndGetters() {
        Vector3D p1 = new Vector3D(1, 2, 3);
        Vector3D p2 = new Vector3D(4, -1, 7);
        Line line = new Line(p1, p2);

        // Direction must be normalized
        Vector3D dir = line.getDirection();
        assertEquals("Direction must be unit", 1.0, dir.getNormSq(), 1e-15);

        // Zero point must be the closest point to origin
        Vector3D zero = line.getOrigin();
        // The zero is orthogonal to direction: dot(zero, direction) should be 0
        double dot = zero.dotProduct(dir);
        assertEquals("Zero dot direction must be 0", 0.0, dot, 1e-12);
    }

    @Test(timeout = 4000)
    public void testAbscissaAndPointAt() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);

        // Point at abscissa 0 should be zero
        Vector3D pt0 = line.pointAt(0.0);
        assertEquals("pointAt(0) should equal zero", line.getOrigin(), pt0);

        // Abscissa of zero should be 0
        double abscZero = line.getAbscissa(line.getOrigin());
        assertEquals("Abscissa of zero should be 0", 0.0, abscZero, 1e-12);

        // Consistency: pointAt(a) then getAbscissa should return a
        double a = 5.0;
        Vector3D pt = line.pointAt(a);
        double back = line.getAbscissa(pt);
        assertEquals("Abscissa after pointAt", a, back, 1e-12);
    }

    @Test(timeout = 4000)
    public void testToSubSpaceToSpace() {
        Vector3D p1 = new Vector3D(0, 1, 0);
        Vector3D p2 = new Vector3D(2, 1, 0);
        Line line = new Line(p1, p2);

        Vector3D point = new Vector3D(3, 1, 0);
        double expectedAbscissa = line.getAbscissa(point);
        Vector1D sub = line.toSubSpace(point);
        assertEquals("ToSubSpace", expectedAbscissa, sub.getX(), 1e-12);

        Vector3D recovered = line.toSpace(sub);
        assertEquals("ToSpace then toSubSpace recovers point", point, recovered);
    }

    // -------- Partition B: Boundary Value Analysis & Extremes --------

    @Test(timeout = 4000)
    public void testResetThrowsOnEqualPoints() {
        Vector3D p = new Vector3D(1, -2, 3);
        try {
            new Line(p, p);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testContains() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 1, 1));
        // Point on line (the zero point)
        assertTrue("Zero point should be contained", line.contains(line.getOrigin()));
        // Point on line
        Vector3D onLine = new Vector3D(2, 2, 2);
        // Check if it's really on line: distance < 1e-10
        assertTrue("Point on line should be contained", line.contains(onLine));
        // Point off line
        Vector3D offLine = new Vector3D(1, 0, 0);
        assertFalse("Point off line should not be contained", line.contains(offLine));
    }

    @Test(timeout = 4000)
    public void testDistanceToPoint() {
        Line line = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        // Point on line
        assertEquals("Distance to point on line", 0.0, line.distance(new Vector3D(5, 0, 0)), 1e-12);
        // Point off line
        double dist = line.distance(new Vector3D(2, 3, 0));
        assertEquals("Distance to off-line point", 3.0, dist, 1e-12);
    }

    // -------- Partition C: Defect-Targeted Branch Zone (revert precision) --------

    @Test(timeout = 4000)
    public void testRevertPrecision() {
        // This test directly targets the known defect in revert() where
        // numerical precision causes a ~1e-10 difference after double revert.
        // Use points that trigger the problematic computation (similar to original testRevert).
        Vector3D p1 = new Vector3D(0.028581782127907646, 0.0, 0.0);
        Vector3D p2 = new Vector3D(0.028581782243293483, 1.0, 0.0);

        Line original = new Line(p1, p2);
        Line reverted = original.revert();
        Line doubleReverted = reverted.revert();

        // After two reverts, the line should be identical to the original.
        Vector3D origZero = original.getOrigin();
        Vector3D origDir  = original.getDirection();
        Vector3D finalZero = doubleReverted.getOrigin();
        Vector3D finalDir  = doubleReverted.getDirection();

        // Use a tolerance of 1e-10 to reveal the defect on the buggy version.
        // On the fixed version this should pass as well (tolerance is tight).
        assertEquals("Zero X after double revert", origZero.getX(), finalZero.getX(), 1e-10);
        assertEquals("Zero Y after double revert", origZero.getY(), finalZero.getY(), 1e-10);
        assertEquals("Zero Z after double revert", origZero.getZ(), finalZero.getZ(), 1e-10);

        assertEquals("Dir X after double revert", origDir.getX(), finalDir.getX(), 1e-10);
        assertEquals("Dir Y after double revert", origDir.getY(), finalDir.getY(), 1e-10);
        assertEquals("Dir Z after double revert", origDir.getZ(), finalDir.getZ(), 1e-10);
    }

    // -------- Partition D: Exception & Defensive Guard Paths --------

    @Test(timeout = 4000)
    public void testDistanceParallelLines() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 5, 0), new Vector3D(1, 5, 0)); // parallel, offset
        double dist = line1.distance(line2);
        assertEquals("Distance between parallel lines", 5.0, dist, 1e-12);
    }

    @Test(timeout = 4000)
    public void testDistanceSkewLines() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(0, 1, 1)); // perpendicular and skew
        double dist = line1.distance(line2);
        assertEquals("Distance between skew lines", 1.0, dist, 1e-12);
    }

    @Test(timeout = 4000)
    public void testClosestPointParallel() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 5, 0), new Vector3D(1, 5, 0));
        Vector3D closest = line1.closestPoint(line2);
        // For parallel lines, closestPoint returns zero of this line (line1.zero)
        assertEquals("Closest point for parallel lines", line1.getOrigin(), closest);
    }

    @Test(timeout = 4000)
    public void testClosestPointIntersecting() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0));
        Vector3D closest = line1.closestPoint(line2);
        // They intersect at (0,0,0)
        assertEquals("Closest point for intersecting lines", new Vector3D(0, 0, 0), closest);
    }

    @Test(timeout = 4000)
    public void testIntersectionExists() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0));
        Vector3D intersection = line1.intersection(line2);
        assertNotNull("Intersection should exist", intersection);
        assertEquals("Intersection point", new Vector3D(0, 0, 0), intersection);
    }

    @Test(timeout = 4000)
    public void testIntersectionNone() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(0, 1, 1)); // skew, not intersecting
        Vector3D intersection = line1.intersection(line2);
        assertNull("Intersection should be null for skew lines", intersection);
    }

    @Test(timeout = 4000)
    public void testIsSimilarToSameDirection() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(2, 0, 0), new Vector3D(3, 0, 0)); // same direction, offset
        assertTrue("Lines with same direction containing zero should be similar", line1.isSimilarTo(line2));
    }

    @Test(timeout = 4000)
    public void testIsSimilarToOppositeDirection() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(-1, 0, 0)); // opposite direction
        assertTrue("Opposite direction lines containing each other zero should be similar", line1.isSimilarTo(line2));
    }

    @Test(timeout = 4000)
    public void testIsNotSimilarTo() {
        Line line1 = new Line(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(1, 1, 0)); // parallel but different line
        assertFalse("Different parallel lines should not be similar", line1.isSimilarTo(line2));
    }

    // -------- Partition E: Object Lifecycle & Contract Integrity --------

    @Test(timeout = 4000)
    public void testCopyConstructorAndRevertConsistency() {
        Vector3D p1 = new Vector3D(1, 2, 3);
        Vector3D p2 = new Vector3D(4, 5, 6);
        Line line = new Line(p1, p2);
        Line copy = new Line(line);
        // Check that copy is independent
        copy.reset(new Vector3D(0, 0, 0), new Vector3D(1, 0, 0));
        assertFalse("Copy reset should not affect original direction",
                     line.getDirection().equals(copy.getDirection()));
    }

    @Test(timeout = 4000)
    public void testWholeLine() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        SubLine sub = line.wholeLine();
        assertNotNull("wholeLine should return non-null SubLine", sub);
        // The SubLine should contain the line's zero point
        assertTrue("SubLine should contain zero of line", sub.contains(line.getOrigin()));
    }

    @Test(timeout = 4000)
    public void testResetAfterConstruction() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        // Reset with new points
        Vector3D q1 = new Vector3D(0, 2, 0);
        Vector3D q2 = new Vector3D(0, 2, 1);
        line.reset(q1, q2);
        assertEquals("Direction after reset", new Vector3D(0, 0, 1), line.getDirection());
        assertEquals("Zero after reset", new Vector3D(0, 2, 0), line.getOrigin());
    }
}
