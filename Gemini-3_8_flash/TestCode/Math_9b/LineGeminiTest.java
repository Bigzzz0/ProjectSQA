/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.apache.commons.math3.geometry.euclidean.twod.Line
 * 
 * 1. Branch: reset(Vector2D p1, Vector2D p2)
 *    - Branch A1: d == 0.0 (identical points) -> angle=0, cos=1, sin=0, originOffset=p1.getY()
 *    - Branch A2: d != 0.0 (distinct points)  -> normal trigonometric initialization & distance normalization
 * 
 * 2. Branch: revertSelf() and getReverse()
 *    - Branch B1: angle < FastMath.PI  -> angle += PI, cos=-cos, sin=-sin, offset=-offset
 *    - Branch B2: angle >= FastMath.PI -> angle -= PI, cos=-cos, sin=-sin, offset=-offset
 *    - Ground Truth Defect Check: Consistency of direction, coordinates, and reversibility invariants
 *      under repeated and crossed reversions (distance/offset/orientation integrity).
 * 
 * 3. Branch: intersection(Line other)
 *    - Branch C1: |sin*other.cos - other.sin*cos| < 1.0e-10 (parallel or collinear lines) -> returns null
 *    - Branch C2: non-zero determinant -> returns exact intersection Vector2D
 * 
 * 4. Branch: getOffset(Line line)
 *    - Branch D1: cos*line.cos + sin*line.sin > 0 (same general orientation)     -> offset = originOffset - line.originOffset
 *    - Branch D2: cos*line.cos + sin*line.sin <= 0 (opposite general orientation) -> offset = originOffset + line.originOffset
 * 
 * 5. Branch: sameOrientationAs(Hyperplane other)
 *    - Branch E1: (sin*other.sin + cos*other.cos) >= 0.0 -> true
 *    - Branch E2: < 0.0 -> false
 * 
 * 6. Branch: contains(Vector2D p) & distance(Vector2D p)
 *    - Branch F1: |getOffset(p)| < 1.0e-10 -> true
 *    - Branch F2: |getOffset(p)| >= 1.0e-10 -> false
 * 
 * 7. Branch: isParallelTo(Line line)
 *    - Branch G1: |sin*other.cos - cos*other.sin| < 1.0e-10 -> true
 *    - Branch G2: >= 1.0e-10 -> false
 * 
 * 8. Branch: LineTransform (AffineTransform)
 *    - Branch H1: |c11| < 1.0e-20 (singular / non-invertible matrix) -> throws MathIllegalArgumentException
 *    - Branch H2: |c11| >= 1.0e-20 (invertible matrix) -> valid LineTransform object created
 *    - Methods: apply(Vector), apply(Hyperplane), apply(SubHyperplane, Hyperplane, Hyperplane)
 * ====================================================================================================
 */
package org.apache.commons.math3.geometry.euclidean.twod;

import java.awt.geom.AffineTransform;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;
import org.apache.commons.math3.geometry.partitioning.Transform;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.*;

public class LineGeminiTest {

    private static final double EPSILON = 1.0e-10;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructFromTwoPointsStandard() {
        Vector2D p1 = new Vector2D(1.0, 1.0);
        Vector2D p2 = new Vector2D(4.0, 5.0);
        Line line = new Line(p1, p2);

        assertTrue(line.contains(p1));
        assertTrue(line.contains(p2));
        assertEquals(0.0, line.distance(p1), EPSILON);
        assertEquals(0.0, line.distance(p2), EPSILON);

        // Abscissa space roundtrip
        Vector1D sub1 = line.toSubSpace(p1);
        Vector2D restored1 = line.toSpace(sub1);
        assertEquals(p1.getX(), restored1.getX(), EPSILON);
        assertEquals(p1.getY(), restored1.getY(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testConstructFromPointAndAngle() {
        Vector2D p = new Vector2D(2.0, 3.0);
        double angle = FastMath.PI / 4.0;
        Line line = new Line(p, angle);

        assertTrue(line.contains(p));
        assertEquals(FastMath.PI / 4.0, line.getAngle(), EPSILON);

        Vector2D pAlong = new Vector2D(2.0 + FastMath.cos(angle), 3.0 + FastMath.sin(angle));
        assertTrue(line.contains(pAlong));
    }

    @Test(timeout = 4000)
    public void testCopyConstructorAndCopySelf() {
        Line original = new Line(new Vector2D(1.0, 2.0), FastMath.PI / 3.0);
        Line copy1 = new Line(original);
        Line copy2 = original.copySelf();

        assertEquals(original.getAngle(), copy1.getAngle(), EPSILON);
        assertEquals(original.getOriginOffset(), copy1.getOriginOffset(), EPSILON);
        assertEquals(original.getAngle(), copy2.getAngle(), EPSILON);
        assertEquals(original.getOriginOffset(), copy2.getOriginOffset(), EPSILON);

        // Modify original, ensure independent copy
        original.setAngle(FastMath.PI / 2.0);
        original.setOriginOffset(10.0);

        assertFalse(FastMath.abs(original.getAngle() - copy1.getAngle()) < EPSILON);
        assertFalse(FastMath.abs(original.getOriginOffset() - copy1.getOriginOffset()) < EPSILON);
    }

    @Test(timeout = 4000)
    public void testIntersectionObliqueLines() {
        Line line1 = new Line(new Vector2D(0.0, 0.0), new Vector2D(2.0, 2.0));
        Line line2 = new Line(new Vector2D(0.0, 2.0), new Vector2D(2.0, 0.0));

        Vector2D intersection = line1.intersection(line2);
        assertNotNull(intersection);
        assertEquals(1.0, intersection.getX(), EPSILON);
        assertEquals(1.0, intersection.getY(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testIntersectionParallelReturnsNull() {
        Line line1 = new Line(new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0));
        Line line2 = new Line(new Vector2D(0.0, 2.0), new Vector2D(1.0, 2.0));

        assertNull(line1.intersection(line2));
        assertTrue(line1.isParallelTo(line2));
    }

    @Test(timeout = 4000)
    public void testGetPointAt() {
        Line line = new Line(new Vector2D(0.0, 0.0), 0.0); // Line along x-axis, oriented +x
        Vector2D pt = line.getPointAt(new Vector1D(5.0), 3.0);
        // Abscissa 5 along x-axis, offset 3 on the orthogonal direction
        assertEquals(5.0, pt.getX(), EPSILON);
        assertEquals(3.0, pt.getY(), EPSILON);
        assertEquals(3.0, line.getOffset(pt), EPSILON);
    }

    @Test(timeout = 4000)
    public void testTranslateToPoint() {
        Line line = new Line(new Vector2D(0.0, 0.0), FastMath.PI / 2.0); // Line along y-axis
        Vector2D target = new Vector2D(7.5, 3.0);
        line.translateToPoint(target);

        assertTrue(line.contains(target));
        assertEquals(0.0, line.distance(target), EPSILON);
    }

    @Test(timeout = 4000)
    public void testWholeHyperplaneAndWholeSpace() {
        Line line = new Line(new Vector2D(1.0, 2.0), 0.5);
        SubLine subLine = line.wholeHyperplane();
        assertNotNull(subLine);
        assertTrue(subLine.getHyperplane() == line);

        PolygonsSet space = line.wholeSpace();
        assertNotNull(space);
        assertFalse(space.isEmpty());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testResetWithIdenticalPointsBranchA1() {
        // When points are identical, d == 0.0
        Vector2D p = new Vector2D(3.5, 4.2);
        Line line = new Line(p, p);

        assertEquals(0.0, line.getAngle(), EPSILON);
        assertEquals(p.getY(), line.getOriginOffset(), EPSILON);
        assertTrue(line.contains(p));
    }

    @Test(timeout = 4000)
    public void testParallelOffsetSameOrientationBranchD1() {
        Line line1 = new Line(new Vector2D(0.0, 0.0), 0.0); // y = 0
        Line line2 = new Line(new Vector2D(0.0, 3.0), 0.0); // y = 3

        assertTrue(line1.sameOrientationAs(line2));
        assertEquals(3.0, line1.getOffset(line2), EPSILON);
        assertEquals(-3.0, line2.getOffset(line1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testParallelOffsetOppositeOrientationBranchD2() {
        Line line1 = new Line(new Vector2D(0.0, 0.0), 0.0);             // y = 0, dir +x
        Line line2 = new Line(new Vector2D(0.0, 3.0), FastMath.PI);     // y = 3, dir -x

        assertFalse(line1.sameOrientationAs(line2));
        assertTrue(line1.isParallelTo(line2));
        assertEquals(3.0, line1.getOffset(line2), EPSILON);
        assertEquals(3.0, line2.getOffset(line1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSetAngleAndSetOriginOffset() {
        Line line = new Line(new Vector2D(0.0, 0.0), 0.0);
        line.setAngle(3.0 * FastMath.PI / 4.0);
        assertEquals(3.0 * FastMath.PI / 4.0, line.getAngle(), EPSILON);

        line.setOriginOffset(-8.25);
        assertEquals(-8.25, line.getOriginOffset(), EPSILON);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Reversal & Invariance)
    // =========================================================================

    @Test(timeout = 4000)
    public void testRevertSelfAndGetReverseAngleUnderPiBranchB1() {
        // Angle < PI
        double initialAngle = FastMath.PI / 6.0;
        Line line = new Line(new Vector2D(1.0, 2.0), initialAngle);
        Line reverse = line.getReverse();

        assertEquals(initialAngle + FastMath.PI, reverse.getAngle(), EPSILON);
        assertEquals(-line.getOriginOffset(), reverse.getOriginOffset(), EPSILON);
        assertFalse(line.sameOrientationAs(reverse));
        assertTrue(line.isParallelTo(reverse));

        // Reverting in place must match getReverse
        Line copy = line.copySelf();
        copy.revertSelf();
        assertEquals(reverse.getAngle(), copy.getAngle(), EPSILON);
        assertEquals(reverse.getOriginOffset(), copy.getOriginOffset(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testRevertSelfAndGetReverseAngleAbovePiBranchB2() {
        // Angle >= PI
        double initialAngle = 5.0 * FastMath.PI / 4.0;
        Line line = new Line(new Vector2D(2.0, -1.0), initialAngle);
        Line reverse = line.getReverse();

        assertEquals(initialAngle - FastMath.PI, reverse.getAngle(), EPSILON);
        assertEquals(-line.getOriginOffset(), reverse.getOriginOffset(), EPSILON);
        assertFalse(line.sameOrientationAs(reverse));
        assertTrue(line.isParallelTo(reverse));

        Line copy = line.copySelf();
        copy.revertSelf();
        assertEquals(reverse.getAngle(), copy.getAngle(), EPSILON);
        assertEquals(reverse.getOriginOffset(), copy.getOriginOffset(), EPSILON);
    }

    /**
     * Specifically tests reversibility invariants and preservation of geometric points
     * under repeated reversion, exposing any sign/offset/drift defects across opposite directions.
     */
    @Test(timeout = 4000)
    public void testDoubleRevertInvarianceDefectGuard() {
        Vector2D p1 = new Vector2D(-3.1415, 2.71828);
        Vector2D p2 = new Vector2D(1.4142, -0.5772);
        Line line = new Line(p1, p2);

        Line reversed = line.getReverse();
        Line doubleReversed = reversed.getReverse();

        // Direction must be completely preserved after two reversions
        assertEquals(line.getAngle(), doubleReversed.getAngle(), EPSILON);
        assertEquals(line.getOriginOffset(), doubleReversed.getOriginOffset(), EPSILON);
        assertTrue(line.sameOrientationAs(doubleReversed));

        // Offset to arbitrary point must be exactly inverted once, and fully restored twice
        Vector2D testPt = new Vector2D(12.34, -56.78);
        double distOrig = line.getOffset(testPt);
        double distRev = reversed.getOffset(testPt);
        double distDoubleRev = doubleReversed.getOffset(testPt);

        assertEquals(-distOrig, distRev, EPSILON);
        assertEquals(distOrig, distDoubleRev, EPSILON);

        // Subspace abscissa mapping after reversion
        Vector1D subOrig = line.toSubSpace(testPt);
        Vector1D subRev = reversed.toSubSpace(testPt);
        assertEquals(-subOrig.getX(), subRev.getX(), EPSILON);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths (Transforms)
    // =========================================================================

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testNonInvertibleAffineTransformThrowsException() {
        // Singular matrix with determinant 0
        AffineTransform nonInvertible = new AffineTransform(0.0, 0.0, 0.0, 0.0, 1.0, 1.0);
        Line.getTransform(nonInvertible);
    }

    @Test(timeout = 4000)
    public void testAffineTransformApplyToPointAndLine() {
        // Scale by 2 along x, 3 along y, translate (1, -2)
        AffineTransform at = new AffineTransform(2.0, 0.0, 0.0, 3.0, 1.0, -2.0);
        Transform<Euclidean2D, Euclidean1D> transform = Line.getTransform(at);

        Vector2D p = new Vector2D(3.0, 4.0);
        Vector2D transformedP = (Vector2D) transform.apply(p);
        assertEquals(2.0 * 3.0 + 1.0, transformedP.getX(), EPSILON);
        assertEquals(3.0 * 4.0 - 2.0, transformedP.getY(), EPSILON);

        Line line = new Line(new Vector2D(0.0, 0.0), new Vector2D(1.0, 1.0));
        Line transformedLine = (Line) transform.apply(line);

        Vector2D p0Transformed = (Vector2D) transform.apply(new Vector2D(0.0, 0.0));
        Vector2D p1Transformed = (Vector2D) transform.apply(new Vector2D(1.0, 1.0));

        assertTrue(transformedLine.contains(p0Transformed));
        assertTrue(transformedLine.contains(p1Transformed));
    }

    @Test(timeout = 4000)
    public void testAffineTransformApplyToSubHyperplane() {
        AffineTransform at = AffineTransform.getRotateInstance(FastMath.PI / 2.0);
        Transform<Euclidean2D, Euclidean1D> transform = Line.getTransform(at);

        Line originalLine = new Line(new Vector2D(0.0, 0.0), 0.0); // x-axis
        Line transformedLine = (Line) transform.apply(originalLine); // y-axis

        OrientedPoint op = new OrientedPoint(new Vector1D(5.0), true);
        SubHyperplane<Euclidean1D> sub = op.wholeHyperplane();

        SubHyperplane<Euclidean1D> transformedSub = transform.apply(sub, originalLine, transformedLine);
        assertNotNull(transformedSub);

        OrientedPoint transformedOp = (OrientedPoint) transformedSub.getHyperplane();
        assertEquals(5.0, transformedOp.getLocation().getX(), EPSILON);
        assertTrue(transformedOp.isDirect());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDistanceAndContainsDistinction() {
        Line line = new Line(new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0)); // y = 0
        Vector2D pOn = new Vector2D(5.0, 0.0);
        Vector2D pClose = new Vector2D(5.0, 1.0e-11);
        Vector2D pFar = new Vector2D(5.0, 0.5);

        assertTrue(line.contains(pOn));
        assertTrue(line.contains(pClose));
        assertFalse(line.contains(pFar));

        assertEquals(0.0, line.distance(pOn), EPSILON);
        assertEquals(0.5, line.distance(pFar), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAngleNormalizationAcrossFullCircle() {
        Line line1 = new Line(new Vector2D(0.0, 0.0), 0.0);
        Line line2 = new Line(new Vector2D(0.0, 0.0), 2.0 * FastMath.PI);
        Line line3 = new Line(new Vector2D(0.0, 0.0), -2.0 * FastMath.PI);

        assertEquals(line1.getAngle(), line2.getAngle(), EPSILON);
        assertEquals(line1.getAngle(), line3.getAngle(), EPSILON);
        assertTrue(line1.sameOrientationAs(line2));
        assertTrue(line1.sameOrientationAs(line3));
    }
}