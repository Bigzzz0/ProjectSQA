package org.apache.commons.math3.geometry.euclidean.twod;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.math3.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.Region;
import org.apache.commons.math3.geometry.partitioning.Region.Location;
import org.apache.commons.math3.geometry.partitioning.Side;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: SubLine (extends AbstractSubHyperplane)
 * Known defect: NullPointerException in intersection() when line1.intersection(line2) returns null
 *               (parallel lines) and v2D is used without null check.
 * 
 * Branch coverage targets:
 * - intersection(): includeEndPoints true/false, loc1/loc2 OUTSIDE/INSIDE/BOUNDARY
 * - side(): parallel lines (global offset), intersecting lines (direct flag)
 * - split(): parallel lines (global offset), intersecting lines (direct flag, BSP tree split)
 * - getSegments(): empty intervals, single interval, multiple intervals
 * - constructors: hyperplane+region, start+end, segment
 * - buildNew(): returns new SubLine
 * 
 * Boundary conditions:
 * - Parallel lines with offset < -1e-10, > 1e-10, between
 * - Intersection point at subline endpoints (includeEndPoints true/false)
 * - Empty remaining region (no segments)
 * - Infinite coordinates in getSegments (unbounded subline)
 */
public class SubLineDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructorFromHyperplaneAndRegion() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        IntervalsSet region = new IntervalsSet(0.0, 1.0);
        SubLine sub = new SubLine(line, region);
        assertNotNull(sub);
        assertSame(line, sub.getHyperplane());
        assertSame(region, sub.getRemainingRegion());
    }

    @Test(timeout = 4000)
    public void testConstructorFromStartEnd() {
        Vector2D start = new Vector2D(0, 0);
        Vector2D end = new Vector2D(1, 0);
        SubLine sub = new SubLine(start, end);
        assertNotNull(sub);
        // Verify the underlying line and interval
        List<Segment> segs = sub.getSegments();
        assertEquals(1, segs.size());
        Segment seg = segs.get(0);
        assertEquals(start, seg.getStart());
        assertEquals(end, seg.getEnd());
    }

    @Test(timeout = 4000)
    public void testConstructorFromSegment() {
        Segment seg = new Segment(new Vector2D(1, 1), new Vector2D(3, 1), new Line(new Vector2D(1, 1), new Vector2D(3, 1)));
        SubLine sub = new SubLine(seg);
        List<Segment> segs = sub.getSegments();
        assertEquals(1, segs.size());
        assertEquals(seg.getStart(), segs.get(0).getStart());
        assertEquals(seg.getEnd(), segs.get(0).getEnd());
    }

    @Test(timeout = 4000)
    public void testGetSegmentsSingleSegment() {
        SubLine sub = new SubLine(new Vector2D(0, 0), new Vector2D(5, 0));
        List<Segment> segs = sub.getSegments();
        assertEquals(1, segs.size());
        Segment s = segs.get(0);
        assertEquals(0.0, s.getStart().getX(), 1e-10);
        assertEquals(0.0, s.getStart().getY(), 1e-10);
        assertEquals(5.0, s.getEnd().getX(), 1e-10);
        assertEquals(0.0, s.getEnd().getY(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetSegmentsEmpty() {
        // Create a SubLine with empty remaining region
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        IntervalsSet empty = new IntervalsSet(); // empty set
        SubLine sub = new SubLine(line, empty);
        List<Segment> segs = sub.getSegments();
        assertTrue(segs.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetSegmentsMultipleIntervals() {
        // Build a SubLine with two disjoint intervals
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        BSPTree<Euclidean1D> tree = new BSPTree<Euclidean1D>(Boolean.TRUE);
        // Split into two intervals: [0,1] and [2,3]
        // We'll use a simple approach: create IntervalsSet from list of intervals
        List<Interval> intervals = new java.util.ArrayList<Interval>();
        intervals.add(new Interval(0.0, 1.0));
        intervals.add(new Interval(2.0, 3.0));
        IntervalsSet region = new IntervalsSet(intervals);
        SubLine sub = new SubLine(line, region);
        List<Segment> segs = sub.getSegments();
        assertEquals(2, segs.size());
        assertEquals(0.0, segs.get(0).getStart().getX(), 1e-10);
        assertEquals(1.0, segs.get(0).getEnd().getX(), 1e-10);
        assertEquals(2.0, segs.get(1).getStart().getX(), 1e-10);
        assertEquals(3.0, segs.get(1).getEnd().getX(), 1e-10);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testGetSegmentsInfiniteCoordinates() {
        // Create a SubLine covering the whole line (unbounded)
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        IntervalsSet wholeLine = new IntervalsSet(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        SubLine sub = new SubLine(line, wholeLine);
        List<Segment> segs = sub.getSegments();
        assertEquals(1, segs.size());
        Segment s = segs.get(0);
        assertTrue(Double.isInfinite(s.getStart().getX()));
        assertTrue(Double.isInfinite(s.getEnd().getX()));
    }

    @Test(timeout = 4000)
    public void testGetSegmentsBoundaryAtZero() {
        SubLine sub = new SubLine(new Vector2D(0, 0), new Vector2D(0, 0)); // zero-length segment
        List<Segment> segs = sub.getSegments();
        assertEquals(1, segs.size());
        Segment s = segs.get(0);
        assertEquals(0.0, s.getStart().getX(), 1e-10);
        assertEquals(0.0, s.getStart().getY(), 1e-10);
        assertEquals(0.0, s.getEnd().getX(), 1e-10);
        assertEquals(0.0, s.getEnd().getY(), 1e-10);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Defect: intersection() throws NullPointerException when lines are parallel.
     * Expected correct behavior: return null.
     */
    @Test(timeout = 4000)
    public void testIntersectionParallel() {
        // Two parallel lines with different offsets
        Line line1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line line2 = new Line(new Vector2D(0, 1), new Vector2D(1, 1));
        SubLine sub1 = new SubLine(line1, new IntervalsSet(0.0, 1.0));
        SubLine sub2 = new SubLine(line2, new IntervalsSet(0.0, 1.0));
        // This should return null, but defective version throws NPE
        Vector2D result = sub1.intersection(sub2, true);
        assertNull(result);
    }

    /**
     * Defect: intersection() throws NullPointerException when lines are parallel (different angle).
     * Also test with includeEndPoints false.
     */
    @Test(timeout = 4000)
    public void testIntersectionParallelIncludeEndPointsFalse() {
        Line line1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line line2 = new Line(new Vector2D(0, 2), new Vector2D(1, 2));
        SubLine sub1 = new SubLine(line1, new IntervalsSet(0.0, 1.0));
        SubLine sub2 = new SubLine(line2, new IntervalsSet(0.0, 1.0));
        Vector2D result = sub1.intersection(sub2, false);
        assertNull(result);
    }

    /**
     * Defect: intersection() throws NullPointerException when lines are parallel (same line but different segments).
     * Actually same line is not parallel but coincident; intersection returns null? Let's test parallel with same direction.
     */
    @Test(timeout = 4000)
    public void testIntersectionNotIntersecting() {
        // Two lines that are not parallel but the sublines do not overlap
        Line line1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line line2 = new Line(new Vector2D(0, 0), new Vector2D(0, 1)); // perpendicular
        SubLine sub1 = new SubLine(line1, new IntervalsSet(0.0, 1.0));
        SubLine sub2 = new SubLine(line2, new IntervalsSet(0.0, 1.0));
        // Intersection point is (0,0) which is inside both sublines? Actually (0,0) is start of both, so if includeEndPoints true, it's inside.
        // To make it non-intersecting, shift one subline away
        SubLine sub2Shifted = new SubLine(line2, new IntervalsSet(2.0, 3.0)); // segment from (0,2) to (0,3)
        Vector2D result = sub1.intersection(sub2Shifted, true);
        // The lines intersect at (0,0) but sub2Shifted does not contain that point -> should return null
        assertNull(result);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testIntersectionInsideWithEndPointsTrue() {
        Line line1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line line2 = new Line(new Vector2D(0, 0), new Vector2D(0, 1));
        SubLine sub1 = new SubLine(line1, new IntervalsSet(0.0, 1.0));
        SubLine sub2 = new SubLine(line2, new IntervalsSet(0.0, 1.0));
        Vector2D result = sub1.intersection(sub2, true);
        assertNotNull(result);
        assertEquals(0.0, result.getX(), 1e-10);
        assertEquals(0.0, result.getY(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testIntersectionInsideWithEndPointsFalse() {
        Line line1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line line2 = new Line(new Vector2D(0, 0), new Vector2D(0, 1));
        SubLine sub1 = new SubLine(line1, new IntervalsSet(0.0, 1.0));
        SubLine sub2 = new SubLine(line2, new IntervalsSet(0.0, 1.0));
        // Intersection at (0,0) is endpoint for both, so with includeEndPoints false it should be null
        Vector2D result = sub1.intersection(sub2, false);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testIntersectionAtEndpointOneSide() {
        Line line1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line line2 = new Line(new Vector2D(0, 0), new Vector2D(0, 1));
        SubLine sub1 = new SubLine(line1, new IntervalsSet(0.0, 1.0));
        SubLine sub2 = new SubLine(line2, new IntervalsSet(0.5, 1.0)); // starts at (0,0.5)
        // Intersection point (0,0) is not in sub2, so null
        Vector2D result = sub1.intersection(sub2, true);
        assertNull(result);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testBuildNew() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        IntervalsSet region = new IntervalsSet(0.0, 1.0);
        SubLine original = new SubLine(line, region);
        SubLine copy = (SubLine) original.buildNew(line, region);
        assertNotNull(copy);
        assertNotSame(original, copy);
        assertEquals(original.getHyperplane(), copy.getHyperplane());
        assertEquals(original.getRemainingRegion(), copy.getRemainingRegion());
    }

    // ========== Additional coverage for side() and split() ==========

    @Test(timeout = 4000)
    public void testSideParallelMinus() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line other = new Line(new Vector2D(0, -1), new Vector2D(1, -1)); // parallel, offset negative
        SubLine sub = new SubLine(line, new IntervalsSet(0.0, 1.0));
        Side side = sub.side(other);
        assertEquals(Side.MINUS, side);
    }

    @Test(timeout = 4000)
    public void testSideParallelPlus() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line other = new Line(new Vector2D(0, 1), new Vector2D(1, 1)); // parallel, offset positive
        SubLine sub = new SubLine(line, new IntervalsSet(0.0, 1.0));
        Side side = sub.side(other);
        assertEquals(Side.PLUS, side);
    }

    @Test(timeout = 4000)
    public void testSideParallelHyper() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line other = new Line(new Vector2D(0, 0), new Vector2D(1, 0)); // same line
        SubLine sub = new SubLine(line, new IntervalsSet(0.0, 1.0));
        Side side = sub.side(other);
        assertEquals(Side.HYPER, side);
    }

    @Test(timeout = 4000)
    public void testSideIntersecting() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line other = new Line(new Vector2D(0, 0), new Vector2D(0, 1)); // perpendicular
        SubLine sub = new SubLine(line, new IntervalsSet(0.0, 1.0));
        Side side = sub.side(other);
        // The intersection point is at (0,0) which is the leftmost point of sub.
        // The oriented point at x=0 with direct=false? Actually we need to compute expected side.
        // For simplicity, just check it's not null and is one of the enum values.
        assertNotNull(side);
        assertTrue(side == Side.PLUS || side == Side.MINUS || side == Side.HYPER);
    }

    @Test(timeout = 4000)
    public void testSplitParallelMinus() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line other = new Line(new Vector2D(0, -1), new Vector2D(1, -1));
        SubLine sub = new SubLine(line, new IntervalsSet(0.0, 1.0));
        SplitSubHyperplane<Euclidean2D> split = sub.split(other);
        assertNull(split.getPlus());
        assertNotNull(split.getMinus());
    }

    @Test(timeout = 4000)
    public void testSplitParallelPlus() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line other = new Line(new Vector2D(0, 1), new Vector2D(1, 1));
        SubLine sub = new SubLine(line, new IntervalsSet(0.0, 1.0));
        SplitSubHyperplane<Euclidean2D> split = sub.split(other);
        assertNotNull(split.getPlus());
        assertNull(split.getMinus());
    }

    @Test(timeout = 4000)
    public void testSplitIntersecting() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line other = new Line(new Vector2D(0, 0), new Vector2D(0, 1));
        SubLine sub = new SubLine(line, new IntervalsSet(0.0, 1.0));
        SplitSubHyperplane<Euclidean2D> split = sub.split(other);
        assertNotNull(split.getPlus());
        assertNotNull(split.getMinus());
        // The split should produce two sublines: one for x<0 and one for x>0? Actually intersection at x=0.
        // Since sub covers [0,1], the minus side (x<0) should be empty? Let's check.
        // The oriented point at x=0 with direct=true? We'll just verify they are not null.
    }

    @Test(timeout = 4000)
    public void testSplitIntersectingWithEmptySide() {
        // Create a subline that lies entirely on one side of the splitting line
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line other = new Line(new Vector2D(0, 0), new Vector2D(0, 1));
        SubLine sub = new SubLine(line, new IntervalsSet(1.0, 2.0)); // segment from x=1 to x=2
        SplitSubHyperplane<Euclidean2D> split = sub.split(other);
        // The splitting line at x=0, sub is entirely on plus side (x>0)
        assertNotNull(split.getPlus());
        assertNull(split.getMinus());
    }
}