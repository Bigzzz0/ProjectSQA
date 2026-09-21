/* [Branch & Defect Analysis Matrix]
 * Target: org.apache.commons.math3.geometry.euclidean.twod.Line
 * Key branches to cover:
 * - Constructors: from two points (reset p1,p2), from point and angle, copy constructor
 * - reset(Vector2D,Vector2D): d == 0.0 branch (coincident points)
 * - reset(Vector2D,double): normalize angle
 * - revertSelf(): angle < PI branch, angle >= PI branch
 * - getReverse(): angle < PI branch, else branch
 * - intersection(): parallel lines (d ~ 0), non-parallel
 * - getOffset(Line): dot product >= 0 branch, else
 * - sameOrientationAs(): sin*sin+cos*cos >= 0 branch (always true because sum of squares >=0, but shows branch)
 * - isParallelTo(): abs(cross product) < epsilon branch
 * - contains(): abs(getOffset(p)) < 1.0e-10 branch
 * - setAngle(): normalization and recalc cos/sin
 * - getAngle(): normalization
 * - translateToPoint(), toSubSpace(), toSpace(), getPointAt()
 *
 * Defect targeting: The known defect from Defects4J (3D Line) suggests precision loss in revert logic.
 * We test revertSelf() and getReverse() twice to verify exact round-trip and consistency.
 */
package org.apache.commons.math3.geometry.euclidean.twod;

import org.junit.Test;
import static org.junit.Assert.*;

public class LineDeepseekTest {

    private static final double EPS = 1.0e-10;
    private static final double PRECISION_EPS = 1.0e-12; // for low-level floating point comparisons

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testConstructorFromTwoPoints() {
        Vector2D p1 = new Vector2D(0.0, 0.0);
        Vector2D p2 = new Vector2D(1.0, 0.0);
        Line line = new Line(p1, p2);
        assertNotNull(line);
        assertEquals(0.0, line.getAngle(), EPS);
        assertEquals(1.0, line.cos, EPS);
        assertEquals(0.0, line.sin, EPS);
        assertEquals(0.0, line.getOriginOffset(), EPS);
    }

    @Test(timeout = 4000)
    public void testConstructorFromPointAndAngle() {
        Vector2D p = new Vector2D(2.0, 3.0);
        double angle = FastMath.PI / 4;
        Line line = new Line(p, angle);
        assertNotNull(line);
        assertEquals(MathUtils.normalizeAngle(angle, FastMath.PI), line.getAngle(), EPS);
        assertEquals(angle, line.getAngle(), EPS); // normalized to same range
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        Line original = new Line(new Vector2D(0,0), new Vector2D(1,1));
        Line copy = new Line(original);
        assertEquals(original.getAngle(), copy.getAngle(), EPS);
        assertEquals(original.getOriginOffset(), copy.getOriginOffset(), EPS);
        assertEquals(original.cos, copy.cos, PRECISION_EPS);
        assertEquals(original.sin, copy.sin, PRECISION_EPS);
    }

    @Test(timeout = 4000)
    public void testCopySelf() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(2,0));
        Line copy = line.copySelf();
        assertNotSame(line, copy);
        assertEquals(line.getAngle(), copy.getAngle(), EPS);
        assertEquals(line.getOriginOffset(), copy.getOriginOffset(), EPS);
    }

    @Test(timeout = 4000)
    public void testResetTwoPoints() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(1,0));
        line.reset(new Vector2D(1,1), new Vector2D(2,2));
        assertEquals(FastMath.PI / 4, line.getAngle(), EPS);
        assertEquals(0.0, line.getOriginOffset(), EPS); // line through origin
    }

    @Test(timeout = 4000)
    public void testResetTwoPointsCoincident() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(1,0));
        Vector2D p = new Vector2D(5.0, 3.0);
        line.reset(p, p); // d == 0.0 branch
        assertEquals(0.0, line.getAngle(), EPS);
        assertEquals(1.0, line.cos, EPS);
        assertEquals(0.0, line.sin, EPS);
        assertEquals(p.getY(), line.getOriginOffset(), EPS);
    }

    @Test(timeout = 4000)
    public void testResetPointAngle() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(1,0));
        Vector2D p = new Vector2D(3.0, -2.0);
        double alpha = FastMath.PI;
        line.reset(p, alpha);
        assertEquals(FastMath.PI, line.getAngle(), EPS);
        assertEquals(-3.0, line.getOriginOffset(), EPS); // originOffset = cos*Py - sin*Px = -1* -2? wait cos= -1, sin=0 => (-1)*(-2) - 0*3 = 2? let's compute: cos= -1, sin=0 => -1 * (-2) - 0*3 = 2. Actually 2. So offset = 2.
        assertEquals(2.0, line.getOriginOffset(), EPS);
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====

    @Test(timeout = 4000)
    public void testAngleNormalizationBoundary() {
        // angle near 2*PI
        double angleNear2Pi = 2 * FastMath.PI - 1e-14;
        Line line = new Line(new Vector2D(0,0), angleNear2Pi);
        assertEquals(angleNear2Pi, line.getAngle(), EPS);
        // After setAngle, check normalization
        line.setAngle(2 * FastMath.PI + 0.1);
        double normalized = MathUtils.normalizeAngle(2 * FastMath.PI + 0.1, FastMath.PI);
        assertEquals(normalized, line.getAngle(), EPS);
    }

    @Test(timeout = 4000)
    public void testResetWithVeryLargeCoordinates() {
        Vector2D p1 = new Vector2D(1e10, 2e10);
        Vector2D p2 = new Vector2D(3e10, 5e10);
        Line line = new Line(p1, p2);
        assertNotNull(line);
        // Should not throw, verify approximate direction
        double expectedAngle = FastMath.atan2(p2.getY()-p1.getY(), p2.getX()-p1.getX());
        assertEquals(expectedAngle, line.getAngle(), EPS);
    }

    @Test(timeout = 4000)
    public void testExtremeAngle() {
        Line line = new Line(new Vector2D(1,1), 1e-12);
        assertEquals(1e-12, line.getAngle(), 1e-12);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // Known defect: precision loss in revert operations (cf. 3D LineTest testRevert)
    @Test(timeout = 4000)
    public void testRevertSelfPrecisionRoundTrip() {
        Line original = new Line(new Vector2D(3, 4), new Vector2D(7, 1)); // random line
        Line afterTwoReverts = new Line(original);
        afterTwoReverts.revertSelf();
        afterTwoReverts.revertSelf();
        // After two reverts, should be identical to original within tight tolerance
        assertEquals("Angle mismatch after double revert", original.getAngle(),
                     afterTwoReverts.getAngle(), PRECISION_EPS);
        assertEquals("Origin offset mismatch after double revert", original.getOriginOffset(),
                     afterTwoReverts.getOriginOffset(), PRECISION_EPS);
        // Also check cos and sin directly
        assertEquals(original.cos, afterTwoReverts.cos, 1e-15);
        assertEquals(original.sin, afterTwoReverts.sin, 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetReverseDoubleRoundTrip() {
        Line original = new Line(new Vector2D(0,0), new Vector2D(0, 5)); // vertical line, angle = PI/2
        Line reversed = original.getReverse();
        Line doubleReversed = reversed.getReverse();
        assertEquals(original.getAngle(), doubleReversed.getAngle(), PRECISION_EPS);
        assertEquals(original.getOriginOffset(), doubleReversed.getOriginOffset(), PRECISION_EPS);
    }

    @Test(timeout = 4000)
    public void testRevertSelfAndGetReverseConsistency() {
        Line original = new Line(new Vector2D(2,3), new Vector2D(5,-1));
        Line reversedViaSelf = original.copySelf();
        reversedViaSelf.revertSelf();
        Line reversedViaMethod = original.getReverse();
        // They should be equal
        assertEquals(reversedViaSelf.getAngle(), reversedViaMethod.getAngle(), PRECISION_EPS);
        assertEquals(reversedViaSelf.getOriginOffset(), reversedViaMethod.getOriginOffset(), PRECISION_EPS);
        assertEquals(reversedViaSelf.cos, reversedViaMethod.cos, 1e-15);
        assertEquals(reversedViaSelf.sin, reversedViaMethod.sin, 1e-15);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testIntersectionParallelLines() {
        Line line1 = new Line(new Vector2D(0,0), new Vector2D(1,0));
        Line line2 = new Line(new Vector2D(0,1), new Vector2D(1,1));
        Vector2D intersection = line1.intersection(line2);
        assertNull("Parallel lines should have no intersection", intersection);
    }

    @Test(timeout = 4000)
    public void testIntersectionNonParallel() {
        Line line1 = new Line(new Vector2D(0,0), new Vector2D(1,0));
        Line line2 = new Line(new Vector2D(0,0), new Vector2D(0,1));
        Vector2D intersection = line1.intersection(line2);
        assertNotNull(intersection);
        assertEquals(0.0, intersection.getX(), EPS);
        assertEquals(0.0, intersection.getY(), EPS);
    }

    @Test(timeout = 4000)
    public void testContainsTrue() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(0,1));
        assertTrue(line.contains(new Vector2D(0, 5)));
        assertTrue(line.contains(new Vector2D(0, -3)));
    }

    @Test(timeout = 4000)
    public void testContainsFalse() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(0,1));
        assertFalse(line.contains(new Vector2D(1, 0)));
        assertFalse(line.contains(new Vector2D(0.001, 0)));
    }

    @Test(timeout = 4000)
    public void testDistance() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(1,0));
        assertEquals(3.0, line.distance(new Vector2D(0, 3)), EPS);
        assertEquals(2.0, line.distance(new Vector2D(0, -2)), EPS);
    }

    @Test(timeout = 4000)
    public void testIsParallelToParallel() {
        Line line1 = new Line(new Vector2D(0,0), new Vector2D(1,0));
        Line line2 = new Line(new Vector2D(0,1), new Vector2D(1,1));
        assertTrue(line1.isParallelTo(line2));
        // opposite orientation also parallel
        Line line3 = line1.getReverse();
        assertTrue(line1.isParallelTo(line3));
    }

    @Test(timeout = 4000)
    public void testIsParallelToNotParallel() {
        Line line1 = new Line(new Vector2D(0,0), new Vector2D(1,0));
        Line line2 = new Line(new Vector2D(0,0), new Vector2D(1,1));
        assertFalse(line1.isParallelTo(line2));
    }

    @Test(timeout = 4000)
    public void testSameOrientationAsTrue() {
        Line line1 = new Line(new Vector2D(0,0), new Vector2D(1,0));
        Line line2 = new Line(new Vector2D(3,4), new Vector2D(5,4));
        assertTrue(line1.sameOrientationAs(line2));
    }

    @Test(timeout = 4000)
    public void testSameOrientationAsFalse() {
        Line line1 = new Line(new Vector2D(0,0), new Vector2D(1,0));
        Line line2 = line1.getReverse();
        assertFalse(line1.sameOrientationAs(line2));
    }

    @Test(timeout = 4000)
    public void testTranslateToPoint() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(1,0));
        line.translateToPoint(new Vector2D(5, -3));
        assertEquals(-3.0, line.getOriginOffset(), EPS); // offset = cos*y - sin*x = 1*(-3) - 0*5 = -3
        // line should now pass through (5,-3)
        assertTrue(line.contains(new Vector2D(5, -3)));
    }

    @Test(timeout = 4000)
    public void testSetAngle() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(1,0));
        line.setAngle(FastMath.PI / 2);
        assertEquals(FastMath.PI / 2, line.getAngle(), EPS);
        assertEquals(0.0, line.cos, EPS);
        assertEquals(1.0, line.sin, EPS);
    }

    @Test(timeout = 4000)
    public void testSetOriginOffset() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(1,0));
        line.setOriginOffset(2.5);
        assertEquals(2.5, line.getOriginOffset(), EPS);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testToSubSpaceAndToSpace() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(1,0));
        Vector2D originalPoint = new Vector2D(3.0, 4.0);
        Vector1D sub = line.toSubSpace(originalPoint);
        assertEquals(3.0, sub.getX(), EPS); // cos*X + sin*Y = 1*3 + 0*4 = 3
        Vector2D restored = line.toSpace(sub);
        assertEquals(3.0, restored.getX(), EPS);
        assertEquals(0.0, restored.getY(), EPS); // because toSpace gives point on line with same abscissa, offset=0
    }

    @Test(timeout = 4000)
    public void testGetPointAt() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(1,0));
        Vector1D abscissa = new Vector1D(2.0);
        double offset = 3.0;
        Vector2D point = line.getPointAt(abscissa, offset);
        // Expected: x = abscissa * cos + (offset - originOffset) * sin = 2*1 + (3-0)*0 = 2
        // y = abscissa * sin - (offset - originOffset) * cos = 2*0 - (3-0)*1 = -3
        assertEquals(2.0, point.getX(), EPS);
        assertEquals(-3.0, point.getY(), EPS);
    }

    @Test(timeout = 4000)
    public void testWholeHyperplane() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(1,0));
        SubLine sub = line.wholeHyperplane();
        assertNotNull(sub);
        assertSame(line, sub.getHyperplane());
    }

    @Test(timeout = 4000)
    public void testWholeSpace() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(1,0));
        PolygonsSet space = line.wholeSpace();
        assertNotNull(space);
    }

    @Test(timeout = 4000)
    public void testGetOffsetLinePositive() {
        Line line1 = new Line(new Vector2D(0,0), new Vector2D(1,0));
        Line line2 = new Line(new Vector2D(0,5), new Vector2D(1,5));
        double offset = line1.getOffset(line2);
        // dot product cos1*cos2 + sin1*sin2 = 1*1 + 0*0 = 1 > 0 => offset = originOffset1 - originOffset2 = 0 - 5 = -5? Wait formula: originOffset + (dot>0 ? -line.originOffset : line.originOffset) = 0 + (-5) = -5. So offset = -5 (line2 is left side?)
        assertEquals(-5.0, offset, EPS);
    }

    @Test(timeout = 4000)
    public void testGetOffsetLineNegative() {
        Line line1 = new Line(new Vector2D(0,0), new Vector2D(1,0));
        Line line2 = line1.getReverse(); // same line but opposite orientation -> dot = -1 < 0 => offset = originOffset1 + line2.originOffset = 0 + 0 = 0
        double offset = line1.getOffset(line2);
        assertEquals(0.0, offset, EPS);
    }

    @Test(timeout = 4000)
    public void testGetOffsetVectorPoint() {
        Line line = new Line(new Vector2D(0,0), new Vector2D(1,0));
        double offset = line.getOffset((Vector<Euclidean2D>) new Vector2D(3.0, 4.0));
        // offset = sin*X - cos*Y + originOffset = 0*3 - 1*4 + 0 = -4
        assertEquals(-4.0, offset, EPS);
    }
}