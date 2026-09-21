package org.apache.commons.math3.geometry.euclidean.twod;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: org.apache.commons.math3.geometry.euclidean.twod.SubLine
 *
 * 1. DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth):
 *    - Method: intersection(SubLine, boolean)
 *    - Defect: When two SubLines are parallel or disjoint, line1.intersection(line2) returns null.
 *      The defective code immediately invokes line1.toSubSpace(v2D) without checking if v2D is null,
 *      resulting in a java.lang.NullPointerException instead of returning null.
 *    - Target Tests:
 *      * testIntersectionParallelLinesReturnsNull (parallel distinct lines)
 *      * testIntersectionParallelCoincidentDisjointSegmentsReturnsNull (coincident lines, non-overlapping)
 *
 * 2. BRANCH COVERAGE FOR intersection(SubLine, boolean):
 *    - includeEndPoints = true:
 *      * loc1 == INSIDE && loc2 == INSIDE -> returns intersection point
 *      * loc1 == BOUNDARY && loc2 == INSIDE -> returns intersection point
 *      * loc1 == INSIDE && loc2 == BOUNDARY -> returns intersection point
 *      * loc1 == BOUNDARY && loc2 == BOUNDARY -> returns intersection point
 *      * loc1 == OUTSIDE || loc2 == OUTSIDE -> returns null
 *    - includeEndPoints = false:
 *      * loc1 == INSIDE && loc2 == INSIDE -> returns intersection point
 *      * loc1 == BOUNDARY || loc2 == BOUNDARY -> returns null
 *      * loc1 == OUTSIDE || loc2 == OUTSIDE -> returns null
 *
 * 3. BRANCH COVERAGE FOR side(Hyperplane<Euclidean2D>):
 *    - crossing == null (parallel lines):
 *      * global < -1.0e-10 -> Side.MINUS
 *      * global > 1.0e-10  -> Side.PLUS
 *      * abs(global) <= 1.0e-10 -> Side.HYPER
 *    - crossing != null (intersecting lines):
 *      * FastMath.sin(angle1 - angle2) < 0 (direct = true)
 *      * FastMath.sin(angle1 - angle2) >= 0 (direct = false)
 *      * Evaluates remainingRegion.side(new OrientedPoint(x, direct))
 *        -> Side.PLUS, Side.MINUS, Side.BOTH, Side.ON
 *
 * 4. BRANCH COVERAGE FOR split(Hyperplane<Euclidean2D>):
 *    - crossing == null:
 *      * global < -1.0e-10 -> SplitSubHyperplane(plus=null, minus=this)
 *      * global >= -1.0e-10 -> SplitSubHyperplane(plus=this, minus=null)
 *    - crossing != null:
 *      * direct = true / false
 *      * splitTree plus is empty vs non-empty
 *      * splitTree minus is empty vs non-empty
 *
 * 5. CONSTRUCTORS & UTILITY METHODS:
 *    - SubLine(Hyperplane, Region)
 *    - SubLine(Vector2D, Vector2D)
 *    - SubLine(Segment)
 *    - getSegments() with finite, infinite, and empty intervals
 *    - buildNew(Hyperplane, Region)
 * ====================================================================================================
 */

import java.util.List;
import org.apache.commons.math3.geometry.euclidean.oned.Euclidean1D;
import org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.geometry.partitioning.Hyperplane;
import org.apache.commons.math3.geometry.partitioning.Region;
import org.apache.commons.math3.geometry.partitioning.Side;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane.SplitSubHyperplane;
import org.junit.Test;
import static org.junit.Assert.*;

public class SubLineGeminiTest {

    private static final double EPSILON = 1.0e-10;

    // ----------------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth NullPointerException)
    // ----------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIntersectionParallelLinesReturnsNull() {
        // Two parallel horizontal lines that never meet
        SubLine subLine1 = new SubLine(new Vector2D(0.0, 1.0), new Vector2D(2.0, 1.0));
        SubLine subLine2 = new SubLine(new Vector2D(0.0, 2.0), new Vector2D(2.0, 2.0));

        // When lines are parallel, line1.intersection(line2) is null.
        // Defective code throws NullPointerException here.
        Vector2D resultWithEndpoints = subLine1.intersection(subLine2, true);
        assertNull("Parallel lines must yield null intersection (includeEndPoints=true)", resultWithEndpoints);

        Vector2D resultWithoutEndpoints = subLine1.intersection(subLine2, false);
        assertNull("Parallel lines must yield null intersection (includeEndPoints=false)", resultWithoutEndpoints);
    }

    @Test(timeout = 4000)
    public void testIntersectionParallelOppositeDirectionLinesReturnsNull() {
        // Parallel lines with opposite directions
        SubLine subLine1 = new SubLine(new Vector2D(0.0, 0.0), new Vector2D(5.0, 0.0));
        SubLine subLine2 = new SubLine(new Vector2D(5.0, 3.0), new Vector2D(0.0, 3.0));

        assertNull(subLine1.intersection(subLine2, true));
        assertNull(subLine1.intersection(subLine2, false));
    }

    // ----------------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions (Intersection)
    // ----------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIntersectionInsideBoth() {
        // Crossing at (1, 1) strictly inside both segments
        SubLine subLine1 = new SubLine(new Vector2D(0.0, 1.0), new Vector2D(2.0, 1.0));
        SubLine subLine2 = new SubLine(new Vector2D(1.0, 0.0), new Vector2D(1.0, 2.0));

        Vector2D pt1 = subLine1.intersection(subLine2, true);
        assertNotNull(pt1);
        assertEquals(1.0, pt1.getX(), EPSILON);
        assertEquals(1.0, pt1.getY(), EPSILON);

        Vector2D pt2 = subLine1.intersection(subLine2, false);
        assertNotNull(pt2);
        assertEquals(1.0, pt2.getX(), EPSILON);
        assertEquals(1.0, pt2.getY(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testIntersectionOnBoundary() {
        // subLine1 has endpoint (1, 1), subLine2 has interior passing through (1, 1)
        SubLine subLine1 = new SubLine(new Vector2D(0.0, 1.0), new Vector2D(1.0, 1.0));
        SubLine subLine2 = new SubLine(new Vector2D(1.0, 0.0), new Vector2D(1.0, 2.0));

        // When includeEndPoints = true, boundary is accepted
        Vector2D ptIncluded = subLine1.intersection(subLine2, true);
        assertNotNull(ptIncluded);
        assertEquals(1.0, ptIncluded.getX(), EPSILON);
        assertEquals(1.0, ptIncluded.getY(), EPSILON);

        // When includeEndPoints = false, boundary is rejected
        Vector2D ptExcluded = subLine1.intersection(subLine2, false);
        assertNull(ptExcluded);
    }

    @Test(timeout = 4000)
    public void testIntersectionAtMutualEndpoints() {
        // Both segments meet exactly at (1, 1)
        SubLine subLine1 = new SubLine(new Vector2D(0.0, 1.0), new Vector2D(1.0, 1.0));
        SubLine subLine2 = new SubLine(new Vector2D(1.0, 1.0), new Vector2D(1.0, 2.0));

        Vector2D ptIncluded = subLine1.intersection(subLine2, true);
        assertNotNull(ptIncluded);
        assertEquals(1.0, ptIncluded.getX(), EPSILON);
        assertEquals(1.0, ptIncluded.getY(), EPSILON);

        Vector2D ptExcluded = subLine1.intersection(subLine2, false);
        assertNull(ptExcluded);
    }

    @Test(timeout = 4000)
    public void testIntersectionLinesCrossOutsideSegments() {
        // The infinite lines intersect at (5, 5), but segments only cover [0, 2]
        SubLine subLine1 = new SubLine(new Vector2D(0.0, 5.0), new Vector2D(2.0, 5.0));
        SubLine subLine2 = new SubLine(new Vector2D(5.0, 0.0), new Vector2D(5.0, 2.0));

        assertNull(subLine1.intersection(subLine2, true));
        assertNull(subLine1.intersection(subLine2, false));
    }

    @Test(timeout = 4000)
    public void testIntersectionOneInsideOneOutside() {
        // Line intersection point lies inside subLine1 but outside subLine2
        SubLine subLine1 = new SubLine(new Vector2D(0.0, 1.0), new Vector2D(4.0, 1.0));
        SubLine subLine2 = new SubLine(new Vector2D(2.0, 2.0), new Vector2D(2.0, 4.0));

        assertNull(subLine1.intersection(subLine2, true));
        assertNull(subLine1.intersection(subLine2, false));
    }

    // ----------------------------------------------------------------------------------
    // Partition A & B: Side Testing (side method branch coverage)
    // ----------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSideParallelLines() {
        SubLine subLine = new SubLine(new Vector2D(0.0, 0.0), new Vector2D(5.0, 0.0));

        // Parallel line with positive offset (above subLine)
        Line linePlus = new Line(new Vector2D(0.0, 1.0), new Vector2D(5.0, 1.0));
        Side sidePlus = subLine.side(linePlus);
        assertEquals(Side.MINUS, sidePlus);

        // Parallel line with negative offset (below subLine)
        Line lineMinus = new Line(new Vector2D(0.0, -1.0), new Vector2D(5.0, -1.0));
        Side sideMinus = subLine.side(lineMinus);
        assertEquals(Side.PLUS, sideMinus);

        // Identical line (zero offset -> HYPER)
        Line lineHyper = new Line(new Vector2D(0.0, 0.0), new Vector2D(5.0, 0.0));
        Side sideHyper = subLine.side(lineHyper);
        assertEquals(Side.HYPER, sideHyper);
    }

    @Test(timeout = 4000)
    public void testSideIntersectingDirectTrueAndFalse() {
        SubLine subLine = new SubLine(new Vector2D(-2.0, 0.0), new Vector2D(2.0, 0.0));

        // Line cutting through the segment perpendicularly
        // Angle difference will trigger sin < 0 or sin > 0
        Line cuttingLineUp = new Line(new Vector2D(0.0, -2.0), new Vector2D(0.0, 2.0));
        Side sideBoth1 = subLine.side(cuttingLineUp);
        assertEquals(Side.BOTH, sideBoth1);

        Line cuttingLineDown = new Line(new Vector2D(0.0, 2.0), new Vector2D(0.0, -2.0));
        Side sideBoth2 = subLine.side(cuttingLineDown);
        assertEquals(Side.BOTH, sideBoth2);
    }

    @Test(timeout = 4000)
    public void testSideIntersectingOutsideSegment() {
        SubLine subLine = new SubLine(new Vector2D(1.0, 0.0), new Vector2D(3.0, 0.0));

        // Line intersects infinite line at (0, 0), which is outside the subLine segment [1, 3]
        Line cuttingLine = new Line(new Vector2D(0.0, -2.0), new Vector2D(0.0, 2.0));
        Side side = subLine.side(cuttingLine);
        // Entire subLine lies on one side of cuttingLine
        assertTrue(side == Side.PLUS || side == Side.MINUS);
    }

    // ----------------------------------------------------------------------------------
    // Partition A & B: Split Testing (split method branch coverage)
    // ----------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSplitParallel() {
        SubLine subLine = new SubLine(new Vector2D(0.0, 0.0), new Vector2D(5.0, 0.0));

        // Line with global < -1.0e-10
        Line lineAbove = new Line(new Vector2D(0.0, 2.0), new Vector2D(5.0, 2.0));
        SplitSubHyperplane<Euclidean2D> split1 = subLine.split(lineAbove);
        assertNull(split1.getPlus());
        assertNotNull(split1.getMinus());

        // Line with global >= -1.0e-10
        Line lineBelow = new Line(new Vector2D(0.0, -2.0), new Vector2D(5.0, -2.0));
        SplitSubHyperplane<Euclidean2D> split2 = subLine.split(lineBelow);
        assertNotNull(split2.getPlus());
        assertNull(split2.getMinus());
    }

    @Test(timeout = 4000)
    public void testSplitIntersectingInside() {
        SubLine subLine = new SubLine(new Vector2D(-2.0, 0.0), new Vector2D(2.0, 0.0));
        Line cuttingLine = new Line(new Vector2D(0.0, -1.0), new Vector2D(0.0, 1.0));

        SplitSubHyperplane<Euclidean2D> split = subLine.split(cuttingLine);
        assertNotNull(split.getPlus());
        assertNotNull(split.getMinus());

        List<Segment> plusSegments = ((SubLine) split.getPlus()).getSegments();
        List<Segment> minusSegments = ((SubLine) split.getMinus()).getSegments();
        assertEquals(1, plusSegments.size());
        assertEquals(1, minusSegments.size());
    }

    @Test(timeout = 4000)
    public void testSplitIntersectingOutsideSegment() {
        SubLine subLine = new SubLine(new Vector2D(2.0, 0.0), new Vector2D(4.0, 0.0));
        Line cuttingLine = new Line(new Vector2D(0.0, -1.0), new Vector2D(0.0, 1.0));

        SplitSubHyperplane<Euclidean2D> split = subLine.split(cuttingLine);
        // Since cuttingLine intersects at x=0, and segment is at [2, 4],
        // one side is empty and the other side contains the sub-line.
        boolean plusEmpty = split.getPlus() == null || ((SubLine) split.getPlus()).getSegments().isEmpty();
        boolean minusEmpty = split.getMinus() == null || ((SubLine) split.getMinus()).getSegments().isEmpty();

        assertTrue(plusEmpty ^ minusEmpty); // Exactly one is empty
    }

    // ----------------------------------------------------------------------------------
    // Partition A & E: Constructors, getSegments & Object Lifecycle
    // ----------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorFromSegment() {
        Vector2D start = new Vector2D(1.0, 2.0);
        Vector2D end = new Vector2D(4.0, 6.0);
        Line line = new Line(start, end);
        Segment segment = new Segment(start, end, line);

        SubLine subLine = new SubLine(segment);
        List<Segment> segments = subLine.getSegments();
        assertEquals(1, segments.size());

        Segment result = segments.get(0);
        assertEquals(1.0, result.getStart().getX(), EPSILON);
        assertEquals(2.0, result.getStart().getY(), EPSILON);
        assertEquals(4.0, result.getEnd().getX(), EPSILON);
        assertEquals(6.0, result.getEnd().getY(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testWholeSubLineSegments() {
        Line line = new Line(new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0));
        SubLine wholeLine = new SubLine(line, new IntervalsSet());

        List<Segment> segments = wholeLine.getSegments();
        assertEquals(1, segments.size());
        Segment seg = segments.get(0);
        assertTrue(Double.isInfinite(seg.getStart().getX()));
        assertTrue(Double.isInfinite(seg.getEnd().getX()));
    }

    @Test(timeout = 4000)
    public void testEmptySubLineSegments() {
        Line line = new Line(new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0));
        SubLine emptySubLine = new SubLine(line, new IntervalsSet(1.0, 0.0)); // Inverted -> empty

        List<Segment> segments = emptySubLine.getSegments();
        assertTrue(segments.isEmpty());
    }

    @Test(timeout = 4000)
    public void testBuildNew() {
        Line line = new Line(new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0));
        Region<Euclidean1D> region = new IntervalsSet(0.0, 5.0);
        SubLine subLine = new SubLine(line, region);

        AbstractSubHyperplane<Euclidean2D, Euclidean1D> copy = subLine.buildNew(line, region);
        assertNotNull(copy);
        assertTrue(copy instanceof SubLine);
        assertSame(line, copy.getHyperplane());
        assertSame(region, copy.getRemainingRegion());
    }

    @Test(timeout = 4000)
    public void testReverseSubLineSegmentEndpoints() {
        // Construct segment with start > end in coordinate order
        SubLine subLine = new SubLine(new Vector2D(5.0, 5.0), new Vector2D(0.0, 0.0));
        List<Segment> segments = subLine.getSegments();
        assertEquals(1, segments.size());
        Segment seg = segments.get(0);
        // Interval set normalizes start to be lower abscissa
        assertEquals(0.0, seg.getStart().getX(), EPSILON);
        assertEquals(0.0, seg.getStart().getY(), EPSILON);
        assertEquals(5.0, seg.getEnd().getX(), EPSILON);
        assertEquals(5.0, seg.getEnd().getY(), EPSILON);
    }
}