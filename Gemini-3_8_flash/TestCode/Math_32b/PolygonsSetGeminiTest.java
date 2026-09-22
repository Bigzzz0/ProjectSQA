/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet
 *
 * Decision / Branch Matrix:
 * 1. Constructors:
 *    - Default PolygonsSet() -> whole 2D space.
 *    - PolygonsSet(BSPTree) -> custom BSP tree representation.
 *    - PolygonsSet(Collection<SubHyperplane>) -> B-rep construction (empty / non-empty).
 *    - PolygonsSet(xMin, xMax, yMin, yMax) -> axis-aligned box (finite closed region).
 * 2. computeGeometricalProperties():
 *    - v.length == 0, tree is whole space (Boolean.TRUE) -> size = +Inf, barycenter = NaN.
 *    - v.length == 0, tree is empty space (Boolean.FALSE) -> size = 0, barycenter = (0,0).
 *    - v.length == 0, tree has non-null cut with leafOutside on both sides (DEFECT: Issue 780 / ClassCastException).
 *    - v[0][0] == null -> at least one open loop (infinite region) -> size = +Inf, barycenter = NaN.
 *    - closed loops: sum < 0 (infinite inverted region) vs sum >= 0 (finite polygon).
 * 3. getVertices() & followLoop():
 *    - tree.getCut() == null -> returns empty Vector2D[0][].
 *    - Single infinite line (loop.size() < 2) -> array with null and +/-Float.MAX_VALUE points.
 *    - Open loop (loop.get(0).getStart() == null) -> array starting with null, dummy start and end points.
 *    - Closed regular loop (loop.get(0).getStart() != null) -> oriented loop points.
 *    - Degenerated loop cases: selectedDistance > 1.0e-10 -> loop ignored (null).
 *    - Thin degenerated loop: loop.size() == 2 && !open -> ignored (null).
 *    - Inconsistent topology: end == null && !open -> MathInternalError.
 * 4. ComparableSegment & SegmentsBuilder:
 *    - ComparableSegment equals, hashCode, compareTo branches (same obj, null, start equality).
 *    - BoundaryAttribute: plusOutside vs plusInside contribution paths.
 */
package org.apache.commons.math3.geometry.euclidean.twod;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.Region;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * High-precision JUnit 4 test suite for {@link PolygonsSet}.
 */
public class PolygonsSetGeminiTest {

    private static final double EPSILON = 1.0e-10;

    // =========================================================================
    // Partition A: Core Functional Logic & Standard Geometrical Properties
    // =========================================================================

    @Test(timeout = 4000)
    public void testBoxConstructionAndProperties() {
        PolygonsSet box = new PolygonsSet(0.0, 4.0, 0.0, 3.0);
        assertFalse(box.isEmpty());
        assertEquals(12.0, box.getSize(), EPSILON);

        Vector2D barycenter = (Vector2D) box.getBarycenter();
        assertEquals(2.0, barycenter.getX(), EPSILON);
        assertEquals(1.5, barycenter.getY(), EPSILON);

        assertEquals(Region.Location.INSIDE, box.checkPoint(new Vector2D(2.0, 1.5)));
        assertEquals(Region.Location.BOUNDARY, box.checkPoint(new Vector2D(0.0, 1.5)));
        assertEquals(Region.Location.OUTSIDE, box.checkPoint(new Vector2D(5.0, 1.5)));

        Vector2D[][] vertices = box.getVertices();
        assertEquals(1, vertices.length);
        assertEquals(4, vertices[0].length);
    }

    @Test(timeout = 4000)
    public void testEmptyPolygonsSetDefaultConstructor() {
        PolygonsSet fullSpace = new PolygonsSet();
        assertTrue(Double.isInfinite(fullSpace.getSize()));
        assertTrue(Double.isNaN(fullSpace.getBarycenter().getX()));
        assertTrue(Double.isNaN(fullSpace.getBarycenter().getY()));

        Vector2D[][] vertices = fullSpace.getVertices();
        assertEquals(0, vertices.length);
        assertEquals(Region.Location.INSIDE, fullSpace.checkPoint(new Vector2D(0.0, 0.0)));
    }

    @Test(timeout = 4000)
    public void testBuildNewFromTree() {
        PolygonsSet box = new PolygonsSet(-1.0, 1.0, -1.0, 1.0);
        PolygonsSet cloned = box.buildNew(box.getTree(false));
        assertNotNull(cloned);
        assertEquals(4.0, cloned.getSize(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testBRepConstructorEmptyCollection() {
        Collection<SubHyperplane<Euclidean2D>> emptyList = new ArrayList<SubHyperplane<Euclidean2D>>();
        PolygonsSet set = new PolygonsSet(emptyList);
        assertTrue(Double.isInfinite(set.getSize()));
        assertEquals(Region.Location.INSIDE, set.checkPoint(new Vector2D(0, 0)));
    }

    @Test(timeout = 4000)
    public void testBRepConstructorSingleInfiniteLine() {
        Collection<SubHyperplane<Euclidean2D>> boundary = new ArrayList<SubHyperplane<Euclidean2D>>();
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        boundary.add(new SubLine(line, new IntervalsSet())); // Whole line

        PolygonsSet halfPlane = new PolygonsSet(boundary);
        assertTrue(Double.isInfinite(halfPlane.getSize()));
        assertTrue(Double.isNaN(halfPlane.getBarycenter().getX()));

        Vector2D[][] vertices = halfPlane.getVertices();
        assertEquals(1, vertices.length);
        // Single infinite line: 3 elements (null, dummy1, dummy2)
        assertEquals(3, vertices[0].length);
        assertNull(vertices[0][0]);
        assertNotNull(vertices[0][1]);
        assertNotNull(vertices[0][2]);
    }

    @Test(timeout = 4000)
    public void testOpenLoopWithRealVertices() {
        // Build half-strip using two half-lines
        Collection<SubHyperplane<Euclidean2D>> boundary = new ArrayList<SubHyperplane<Euclidean2D>>();
        Line line1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line line2 = new Line(new Vector2D(1, 0), new Vector2D(1, 1));

        boundary.add(new SubLine(line1, new IntervalsSet(Double.NEGATIVE_INFINITY, 1.0)));
        boundary.add(new SubLine(line2, new IntervalsSet(0.0, Double.POSITIVE_INFINITY)));

        PolygonsSet openRegion = new PolygonsSet(boundary);
        Vector2D[][] vertices = openRegion.getVertices();
        assertTrue(vertices.length >= 1);
        assertNull(vertices[0][0]); // Open loop starts with null
        assertTrue(Double.isInfinite(openRegion.getSize()));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Degenerate Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testDegenerateEmptySpaceFromEmptyTree() {
        BSPTree<Euclidean2D> emptyTree = new BSPTree<Euclidean2D>(Boolean.FALSE);
        PolygonsSet emptySet = new PolygonsSet(emptyTree);
        assertEquals(0.0, emptySet.getSize(), EPSILON);
        assertEquals(0.0, emptySet.getBarycenter().getX(), EPSILON);
        assertEquals(0.0, emptySet.getBarycenter().getY(), EPSILON);
        assertEquals(0, emptySet.getVertices().length);
    }

    @Test(timeout = 4000)
    public void testInvertedRegionInfiniteInside() {
        // Complement of a box: finite outside surrounded by infinite inside
        PolygonsSet box = new PolygonsSet(0.0, 2.0, 0.0, 2.0);
        RegionFactory<Euclidean2D> factory = new RegionFactory<Euclidean2D>();
        PolygonsSet inverted = (PolygonsSet) factory.getComplement(box);

        assertTrue(Double.isInfinite(inverted.getSize()));
        assertTrue(Double.isNaN(inverted.getBarycenter().getX()));
        assertTrue(Double.isNaN(inverted.getBarycenter().getY()));
    }

    @Test(timeout = 4000)
    public void testPolygonWithHole() {
        PolygonsSet outer = new PolygonsSet(-5.0, 5.0, -5.0, 5.0); // Area = 100
        PolygonsSet inner = new PolygonsSet(-2.0, 2.0, -2.0, 2.0); // Area = 16
        RegionFactory<Euclidean2D> factory = new RegionFactory<Euclidean2D>();
        PolygonsSet withHole = (PolygonsSet) factory.difference(outer, inner);

        assertEquals(84.0, withHole.getSize(), 1.0e-8);
        assertEquals(0.0, withHole.getBarycenter().getX(), 1.0e-8);
        assertEquals(0.0, withHole.getBarycenter().getY(), 1.0e-8);

        Vector2D[][] vertices = withHole.getVertices();
        assertEquals(2, vertices.length); // Two closed loops
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Known Defect: Issue 780 / ClassCastException)
    // =========================================================================

    /**
     * Targets Issue 780:
     * When a PolygonsSet BSPTree has cuts but no boundary elements (e.g. both minus
     * and plus leaves are outside/inside or cancelled out), {@code getVertices()} returns
     * an empty array. If {@code computeGeometricalProperties()} assumes that
     * {@code tree.getAttribute()} is a Boolean, it fails with:
     * ClassCastException: BoundaryAttribute cannot be cast to java.lang.Boolean
     */
    @Test(timeout = 4000)
    public void testIssue780ClassCastExceptionOnInternalNodeAttribute() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        SubHyperplane<Euclidean2D> cut = line.wholeHyperplane();

        // Construct tree with internal cut where both leaf children are Boolean.FALSE (outside)
        BSPTree<Euclidean2D> leafMinus = new BSPTree<Euclidean2D>(Boolean.FALSE);
        BSPTree<Euclidean2D> leafPlus  = new BSPTree<Euclidean2D>(Boolean.FALSE);
        BSPTree<Euclidean2D> tree = new BSPTree<Euclidean2D>(cut, leafPlus, leafMinus, null);

        PolygonsSet polygon = new PolygonsSet(tree);

        // This triggers computeGeometricalProperties(), which calls getVertices(), setting
        // BoundaryAttribute on the internal node, followed by (Boolean) tree.getAttribute()
        double size = polygon.getSize();
        assertEquals(0.0, size, EPSILON);
    }

    @Test(timeout = 4000)
    public void testIssue780InternalNodeBothInsideLeaves() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        SubHyperplane<Euclidean2D> cut = line.wholeHyperplane();

        // Construct tree with internal cut where both leaf children are Boolean.TRUE (inside)
        BSPTree<Euclidean2D> leafMinus = new BSPTree<Euclidean2D>(Boolean.TRUE);
        BSPTree<Euclidean2D> leafPlus  = new BSPTree<Euclidean2D>(Boolean.TRUE);
        BSPTree<Euclidean2D> tree = new BSPTree<Euclidean2D>(cut, leafPlus, leafMinus, null);

        PolygonsSet polygon = new PolygonsSet(tree);

        double size = polygon.getSize();
        assertTrue(Double.isInfinite(size));
    }

    // =========================================================================
    // Partition D: Exceptional & Degenerate Branch Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testDegeneratedLoopFiltering() {
        // Construct two segments that do not properly close or have gap > 1.0e-10
        Collection<SubHyperplane<Euclidean2D>> boundary = new ArrayList<SubHyperplane<Euclidean2D>>();
        Line line1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line line2 = new Line(new Vector2D(1, 0), new Vector2D(0.5, 0.5));
        Line line3 = new Line(new Vector2D(0.5, 0.5), new Vector2D(0, 0.1)); // Gap at (0, 0.1) vs (0, 0)

        boundary.add(new SubLine(new Vector2D(0, 0), new Vector2D(1, 0)));
        boundary.add(new SubLine(new Vector2D(1, 0), new Vector2D(0.5, 0.5)));
        boundary.add(new SubLine(new Vector2D(0.5, 0.5), new Vector2D(0, 0.1)));

        PolygonsSet set = new PolygonsSet(boundary);
        Vector2D[][] vertices = set.getVertices();
        assertNotNull(vertices);
    }

    @Test(expected = MathInternalError.class, timeout = 4000)
    public void testInconsistentTopologyThrowsMathInternalError() {
        // A closed loop whose next segment ends abruptly with end == null triggers MathInternalError
        // We simulate this by crafting an open-ended segment marked as closed in a tree
        Line line1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line line2 = new Line(new Vector2D(1, 0), new Vector2D(1, 1));

        Collection<SubHyperplane<Euclidean2D>> boundary = new ArrayList<SubHyperplane<Euclidean2D>>();
        // Segment with finite start but infinite end connected to a closed start
        boundary.add(new SubLine(line1.toSpace(new org.apache.commons.math3.geometry.euclidean.oned.Vector1D(0)),
                                 line1.toSpace(new org.apache.commons.math3.geometry.euclidean.oned.Vector1D(1))));
        boundary.add(new SubLine(line2, new IntervalsSet(0.0, Double.POSITIVE_INFINITY)));

        PolygonsSet set = new PolygonsSet(boundary);
        set.getVertices();
    }

    // =========================================================================
    // Partition E: Internal ComparableSegment & Object Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testVerticesCachingDeterminism() {
        PolygonsSet box = new PolygonsSet(0.0, 1.0, 0.0, 1.0);
        Vector2D[][] firstCall = box.getVertices();
        Vector2D[][] secondCall = box.getVertices();

        assertNotSame("Vertices array clone must produce a new outer array instance", firstCall, secondCall);
        assertEquals(firstCall.length, secondCall.length);
        assertEquals(firstCall[0].length, secondCall[0].length);
        assertEquals(firstCall[0][0].getX(), secondCall[0][0].getX(), EPSILON);
    }
}